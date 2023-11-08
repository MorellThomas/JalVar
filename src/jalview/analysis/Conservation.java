/*
 * Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
 * Copyright (C) $$Year-Rel$$ The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.analysis;

import java.util.Locale;

import jalview.analysis.scoremodels.ScoreMatrix;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.ResidueCount;
import jalview.datamodel.ResidueCount.SymbolCounts;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.util.Comparison;
import jalview.util.Format;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Calculates conservation values for a given set of sequences
 */
public class Conservation
{
  /*
   * need to have a minimum of 3% of sequences with a residue
   * for it to be included in the conservation calculation
   */
  private static final int THRESHOLD_PERCENT = 3;

  private static final int TOUPPERCASE = 'a' - 'A';

  private static final int GAP_INDEX = -1;

  private static final Format FORMAT_3DP = new Format("%2.5f");

  SequenceI[] sequences;

  int start;

  int end;

  /*
   * a list whose i'th element is an array whose first entry is the checksum
   * of the i'th sequence, followed by residues encoded to score matrix index
   */
  Vector<int[]> seqNums;

  int maxLength = 0; // used by quality calcs

  boolean seqNumsChanged = false; // updated after any change via calcSeqNum;

  /*
   * a map per column with {property, conservation} where conservation value is
   * 1 (property is conserved), 0 (absence of property is conserved) or -1
   * (property is not conserved i.e. column has residues with and without it)
   */
  Map<String, Integer>[] total;

  /*
   * if true then conservation calculation will map all symbols to canonical aa
   * numbering rather than consider conservation of that symbol
   */
  boolean canonicaliseAa = true;

  private Vector<Double> quality;

  private double qualityMinimum;

  private double qualityMaximum;

  private Sequence consSequence;

  /*
   * percentage of residues in a column to qualify for counting conservation
   */
  private int threshold;

  private String name = "";

  /*
   * an array, for each column, of counts of symbols (by score matrix index)
   */
  private int[][] cons2;

  /*
   * gap counts for each column
   */
  private int[] cons2GapCounts;

  private String[] consSymbs;

  /**
   * Constructor using default threshold of 3%
   * 
   * @param name
   *          Name of conservation
   * @param sequences
   *          sequences to be used in calculation
   * @param start
   *          start residue position
   * @param end
   *          end residue position
   */
  public Conservation(String name, List<SequenceI> sequences, int start,
          int end)
  {
    this(name, THRESHOLD_PERCENT, sequences, start, end);
  }

  /**
   * Constructor
   * 
   * @param name
   *          Name of conservation
   * @param threshold
   *          percentage of sequences at or below which property conservation is
   *          ignored
   * @param sequences
   *          sequences to be used in calculation
   * @param start
   *          start column position
   * @param end
   *          end column position
   */
  public Conservation(String name, int threshold, List<SequenceI> sequences,
          int start, int end)
  {
    this.name = name;
    this.threshold = threshold;
    this.start = start;
    this.end = end;

    maxLength = end - start + 1; // default width includes bounds of
    // calculation

    int s, sSize = sequences.size();
    SequenceI[] sarray = new SequenceI[sSize];
    this.sequences = sarray;
    try
    {
      for (s = 0; s < sSize; s++)
      {
        sarray[s] = sequences.get(s);
        if (sarray[s].getLength() > maxLength)
        {
          maxLength = sarray[s].getLength();
        }
      }
    } catch (ArrayIndexOutOfBoundsException ex)
    {
      // bail - another thread has modified the sequence array, so the current
      // calculation is probably invalid.
      this.sequences = new SequenceI[0];
      maxLength = 0;
    }
  }

  /**
   * Translate sequence i into score matrix indices and store it in the i'th
   * position of the seqNums array.
   * 
   * @param i
   * @param sm
   */
  private void calcSeqNum(int i, ScoreMatrix sm)
  {
    int sSize = sequences.length;

    if ((i > -1) && (i < sSize))
    {
      String sq = sequences[i].getSequenceAsString();

      if (seqNums.size() <= i)
      {
        seqNums.addElement(new int[sq.length() + 1]);
      }

      /*
       * the first entry in the array is the sequence's hashcode,
       * following entries are matrix indices of sequence characters
       */
      if (sq.hashCode() != seqNums.elementAt(i)[0])
      {
        int j;
        int len;
        seqNumsChanged = true;
        len = sq.length();

        if (maxLength < len)
        {
          maxLength = len;
        }

        int[] sqnum = new int[len + 1]; // better to always make a new array -
        // sequence can change its length
        sqnum[0] = sq.hashCode();

        for (j = 1; j <= len; j++)
        {
          // sqnum[j] = ResidueProperties.aaIndex[sq.charAt(j - 1)];
          char residue = sq.charAt(j - 1);
          if (Comparison.isGap(residue))
          {
            sqnum[j] = GAP_INDEX;
          }
          else
          {
            sqnum[j] = sm.getMatrixIndex(residue);
            if (sqnum[j] == -1)
            {
              sqnum[j] = GAP_INDEX;
            }
          }
        }

        seqNums.setElementAt(sqnum, i);
      }
      else
      {
        System.out.println("SEQUENCE HAS BEEN DELETED!!!");
      }
    }
    else
    {
      // JBPNote INFO level debug
      System.err.println(
              "ERROR: calcSeqNum called with out of range sequence index for Alignment\n");
    }
  }

  /**
   * Calculates the conservation values for given set of sequences
   */
  public void calculate()
  {
    int height = sequences.length;

    total = new Map[maxLength];

    for (int column = start; column <= end; column++)
    {
      ResidueCount values = countResidues(column);

      /*
       * percentage count at or below which we ignore residues
       */
      int thresh = (threshold * height) / 100;

      /*
       * check observed residues in column and record whether each 
       * physico-chemical property is conserved (+1), absence conserved (0),
       * or not conserved (-1)
       * Using TreeMap means properties are displayed in alphabetical order
       */
      SortedMap<String, Integer> resultHash = new TreeMap<>();
      SymbolCounts symbolCounts = values.getSymbolCounts();
      char[] symbols = symbolCounts.symbols;
      int[] counts = symbolCounts.values;
      for (int j = 0; j < symbols.length; j++)
      {
        char c = symbols[j];
        if (counts[j] > thresh)
        {
          recordConservation(resultHash, String.valueOf(c));
        }
      }
      if (values.getGapCount() > thresh)
      {
        recordConservation(resultHash, "-");
      }

      if (total.length > 0)
      {
        total[column - start] = resultHash;
      }
    }
  }

  /**
   * Updates the conservation results for an observed residue
   * 
   * @param resultMap
   *          a map of {property, conservation} where conservation value is +1
   *          (all residues have the property), 0 (no residue has the property)
   *          or -1 (some do, some don't)
   * @param res
   */
  protected static void recordConservation(Map<String, Integer> resultMap,
          String res)
  {
    res = res.toUpperCase(Locale.ROOT);
    for (Entry<String, Map<String, Integer>> property : ResidueProperties.propHash
            .entrySet())
    {
      String propertyName = property.getKey();
      Integer residuePropertyValue = property.getValue().get(res);

      if (!resultMap.containsKey(propertyName))
      {
        /*
         * first time we've seen this residue - note whether it has this property
         */
        if (residuePropertyValue != null)
        {
          resultMap.put(propertyName, residuePropertyValue);
        }
        else
        {
          /*
           * unrecognised residue - use default value for property
           */
          resultMap.put(propertyName, property.getValue().get("-"));
        }
      }
      else
      {
        Integer currentResult = resultMap.get(propertyName);
        if (currentResult.intValue() != -1
                && !currentResult.equals(residuePropertyValue))
        {
          /*
           * property is unconserved - residues seen both with and without it
           */
          resultMap.put(propertyName, Integer.valueOf(-1));
        }
      }
    }
  }

  /**
   * Counts residues (upper-cased) and gaps in the given column
   * 
   * @param column
   * @return
   */
  protected ResidueCount countResidues(int column)
  {
    ResidueCount values = new ResidueCount(false);

    for (int row = 0; row < sequences.length; row++)
    {
      if (sequences[row].getLength() > column)
      {
        char c = sequences[row].getCharAt(column);
        if (canonicaliseAa)
        {
          int index = ResidueProperties.aaIndex[c];
          c = index > 20 ? '-' : ResidueProperties.aa[index].charAt(0);
        }
        else
        {
          c = toUpperCase(c);
        }
        if (Comparison.isGap(c))
        {
          values.addGap();
        }
        else
        {
          values.add(c);
        }
      }
      else
      {
        values.addGap();
      }
    }
    return values;
  }

  /**
   * Counts conservation and gaps for a column of the alignment
   * 
   * @return { 1 if fully conserved, else 0, gap count }
   */
  public int[] countConservationAndGaps(int column)
  {
    int gapCount = 0;
    boolean fullyConserved = true;
    int iSize = sequences.length;

    if (iSize == 0)
    {
      return new int[] { 0, 0 };
    }

    char lastRes = '0';
    for (int i = 0; i < iSize; i++)
    {
      if (column >= sequences[i].getLength())
      {
        gapCount++;
        continue;
      }

      char c = sequences[i].getCharAt(column); // gaps do not have upper/lower
                                               // case

      if (Comparison.isGap((c)))
      {
        gapCount++;
      }
      else
      {
        c = toUpperCase(c);
        if (lastRes == '0')
        {
          lastRes = c;
        }
        if (c != lastRes)
        {
          fullyConserved = false;
        }
      }
    }

    int[] r = new int[] { fullyConserved ? 1 : 0, gapCount };
    return r;
  }

  /**
   * Returns the upper-cased character if between 'a' and 'z', else the
   * unchanged value
   * 
   * @param c
   * @return
   */
  char toUpperCase(char c)
  {
    if ('a' <= c && c <= 'z')
    {
      c -= TOUPPERCASE;
    }
    return c;
  }

  /**
   * Calculates the conservation sequence
   * 
   * @param positiveOnly
   *          if true, calculate positive conservation; else calculate both
   *          positive and negative conservation
   * @param maxPercentageGaps
   *          the percentage of gaps in a column, at or above which no
   *          conservation is asserted
   */
  public void verdict(boolean positiveOnly, float maxPercentageGaps)
  {
    // TODO call this at the end of calculate(), should not be a public method

    StringBuilder consString = new StringBuilder(end);

    // NOTE THIS SHOULD CHECK IF THE CONSEQUENCE ALREADY
    // EXISTS AND NOT OVERWRITE WITH '-', BUT THIS CASE
    // DOES NOT EXIST IN JALVIEW 2.1.2
    for (int i = 0; i < start; i++)
    {
      consString.append('-');
    }
    consSymbs = new String[end - start + 1];
    for (int i = start; i <= end; i++)
    {
      int[] gapcons = countConservationAndGaps(i);
      boolean fullyConserved = gapcons[0] == 1;
      int totGaps = gapcons[1];
      float pgaps = (totGaps * 100f) / sequences.length;

      if (maxPercentageGaps > pgaps)
      {
        Map<String, Integer> resultHash = total[i - start];
        int count = 0;
        StringBuilder positives = new StringBuilder(64);
        StringBuilder negatives = new StringBuilder(32);
        for (String type : resultHash.keySet())
        {
          int result = resultHash.get(type).intValue();
          if (result == -1)
          {
            /*
             * not conserved (present or absent)
             */
            continue;
          }
          count++;
          if (result == 1)
          {
            /*
             * positively conserved property (all residues have it)
             */
            positives.append(positives.length() == 0 ? "" : " ");
            positives.append(type);
          }
          if (result == 0 && !positiveOnly)
          {
            /*
             * absense of property is conserved (all residues lack it)
             */
            negatives.append(negatives.length() == 0 ? "" : " ");
            negatives.append("!").append(type);
          }
        }
        if (negatives.length() > 0)
        {
          positives.append(" ").append(negatives);
        }
        consSymbs[i - start] = positives.toString();

        if (count < 10)
        {
          consString.append(count); // Conserved props!=Identity
        }
        else
        {
          consString.append(fullyConserved ? "*" : "+");
        }
      }
      else
      {
        consString.append('-');
      }
    }

    consSequence = new Sequence(name, consString.toString(), start, end);
  }

  /**
   * 
   * 
   * @return Conservation sequence
   */
  public SequenceI getConsSequence()
  {
    return consSequence;
  }

  // From Alignment.java in jalview118
  public void findQuality()
  {
    findQuality(0, maxLength - 1, ScoreModels.getInstance().getBlosum62());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param sm
   */
  private void percentIdentity(ScoreMatrix sm)
  {
    seqNums = new Vector<>();
    int i = 0, iSize = sequences.length;
    // Do we need to calculate this again?
    for (i = 0; i < iSize; i++)
    {
      calcSeqNum(i, sm);
    }

    if ((cons2 == null) || seqNumsChanged)
    {
      // FIXME remove magic number 24 without changing calc
      // sm.getSize() returns 25 so doesn't quite do it...
      cons2 = new int[maxLength][24];
      cons2GapCounts = new int[maxLength];

      int j = 0;

      while (j < sequences.length)
      {
        int[] sqnum = seqNums.elementAt(j);

        for (i = 1; i < sqnum.length; i++)
        {
          int index = sqnum[i];
          if (index == GAP_INDEX)
          {
            cons2GapCounts[i - 1]++;
          }
          else
          {
            cons2[i - 1][index]++;
          }
        }

        // TODO should this start from sqnum.length?
        for (i = sqnum.length - 1; i < maxLength; i++)
        {
          cons2GapCounts[i]++;
        }
        j++;
      }
    }
  }

  /**
   * Calculates the quality of the set of sequences over the given inclusive
   * column range, using the specified substitution score matrix
   * 
   * @param startCol
   * @param endCol
   * @param scoreMatrix
   */
  protected void findQuality(int startCol, int endCol,
          ScoreMatrix scoreMatrix)
  {
    quality = new Vector<>();

    double max = -Double.MAX_VALUE;
    float[][] scores = scoreMatrix.getMatrix();

    percentIdentity(scoreMatrix);

    int size = seqNums.size();
    int[] lengths = new int[size];

    for (int l = 0; l < size; l++)
    {
      lengths[l] = seqNums.elementAt(l).length - 1;
    }

    final int symbolCount = scoreMatrix.getSize();

    for (int j = startCol; j <= endCol; j++)
    {
      double bigtot = 0;

      // First Xr = depends on column only
      double[] x = new double[symbolCount];

      for (int ii = 0; ii < symbolCount; ii++)
      {
        x[ii] = 0;

        /*
         * todo JAL-728 currently assuming last symbol in matrix is * for gap
         * (which we ignore as counted separately); true for BLOSUM62 but may
         * not be once alternative matrices are supported
         */
        for (int i2 = 0; i2 < symbolCount - 1; i2++)
        {
          x[ii] += (((double) cons2[j][i2] * scores[ii][i2]) + 4D);
        }
        x[ii] += 4D + cons2GapCounts[j] * scoreMatrix.getMinimumScore();

        x[ii] /= size;
      }

      // Now calculate D for each position and sum
      for (int k = 0; k < size; k++)
      {
        double tot = 0;
        double[] xx = new double[symbolCount];
        // sequence character index, or implied gap if sequence too short
        int seqNum = (j < lengths[k]) ? seqNums.elementAt(k)[j + 1]
                : GAP_INDEX;

        for (int i = 0; i < symbolCount - 1; i++)
        {
          double sr = 4D;
          if (seqNum == GAP_INDEX)
          {
            sr += scoreMatrix.getMinimumScore();
          }
          else
          {
            sr += scores[i][seqNum];
          }

          xx[i] = x[i] - sr;

          tot += (xx[i] * xx[i]);
        }

        bigtot += Math.sqrt(tot);
      }

      max = Math.max(max, bigtot);

      quality.addElement(Double.valueOf(bigtot));
    }

    double newmax = -Double.MAX_VALUE;

    for (int j = startCol; j <= endCol; j++)
    {
      double tmp = quality.elementAt(j).doubleValue();
      // tmp = ((max - tmp) * (size - cons2[j][23])) / size;
      tmp = ((max - tmp) * (size - cons2GapCounts[j])) / size;

      // System.out.println(tmp+ " " + j);
      quality.setElementAt(Double.valueOf(tmp), j);

      if (tmp > newmax)
      {
        newmax = tmp;
      }
    }

    qualityMinimum = 0D;
    qualityMaximum = newmax;
  }

  /**
   * Complete the given consensus and quuality annotation rows. Note: currently
   * this method will reallocate the given annotation row if it is different to
   * the calculated width, otherwise will leave its length unchanged.
   * 
   * @param conservation
   *          conservation annotation row
   * @param quality2
   *          (optional - may be null)
   * @param istart
   *          first column for conservation
   * @param alWidth
   *          extent of conservation
   */
  public void completeAnnotations(AlignmentAnnotation conservation,
          AlignmentAnnotation quality2, int istart, int alWidth)
  {
    SequenceI cons = getConsSequence();

    /*
     * colour scale for Conservation and Quality;
     */
    float minR = 0.3f;
    float minG = 0.0f;
    float minB = 0f;
    float maxR = 1.0f - minR;
    float maxG = 0.9f - minG;
    float maxB = 0f - minB;

    float min = 0f;
    float max = 11f;
    float qmin = 0f;
    float qmax = 0f;

    if (conservation != null && conservation.annotations != null
            && conservation.annotations.length != alWidth)
    {
      conservation.annotations = new Annotation[alWidth];
    }

    if (quality2 != null)
    {
      quality2.graphMax = (float) qualityMaximum;
      if (quality2.annotations != null
              && quality2.annotations.length != alWidth)
      {
        quality2.annotations = new Annotation[alWidth];
      }
      qmin = (float) qualityMinimum;
      qmax = (float) qualityMaximum;
    }

    for (int i = istart; i < alWidth; i++)
    {
      float value = 0;

      char c = cons.getCharAt(i);

      if (Character.isDigit(c))
      {
        value = c - '0';
      }
      else if (c == '*')
      {
        value = 11;
      }
      else if (c == '+')
      {
        value = 10;
      }

      if (conservation != null)
      {
        float vprop = value - min;
        vprop /= max;
        int consp = i - start;
        String conssym = (value > 0 && consp > -1
                && consp < consSymbs.length) ? consSymbs[consp] : "";
        conservation.annotations[i] = new Annotation(String.valueOf(c),
                conssym, ' ', value, new Color(minR + (maxR * vprop),
                        minG + (maxG * vprop), minB + (maxB * vprop)));
      }

      // Quality calc
      if (quality2 != null)
      {
        value = quality.elementAt(i).floatValue();
        float vprop = value - qmin;
        vprop /= qmax;
        String description = FORMAT_3DP.form(value);
        quality2.annotations[i] = new Annotation(" ", description, ' ',
                value, new Color(minR + (maxR * vprop),
                        minG + (maxG * vprop), minB + (maxB * vprop)));
      }
    }
  }

  /**
   * construct and call the calculation methods on a new Conservation object
   * 
   * @param name
   *          - name of conservation
   * @param seqs
   * @param start
   *          first column in calculation window
   * @param end
   *          last column in calculation window
   * @param positiveOnly
   *          calculate positive (true) or positive and negative (false)
   *          conservation
   * @param maxPercentGaps
   *          percentage of gaps tolerated in column
   * @param calcQuality
   *          flag indicating if alignment quality should be calculated
   * @return Conservation object ready for use in visualization
   */
  public static Conservation calculateConservation(String name,
          List<SequenceI> seqs, int start, int end, boolean positiveOnly,
          int maxPercentGaps, boolean calcQuality)
  {
    Conservation cons = new Conservation(name, seqs, start, end);
    cons.calculate();
    cons.verdict(positiveOnly, maxPercentGaps);

    if (calcQuality)
    {
      cons.findQuality();
    }

    return cons;
  }

  /**
   * Returns the computed tooltip (annotation description) for a given column.
   * The tip is empty if the conservation score is zero, otherwise holds the
   * conserved properties (and, optionally, properties whose absence is
   * conserved).
   * 
   * @param column
   * @return
   */
  String getTooltip(int column)
  {
    SequenceI cons = getConsSequence();
    char val = column < cons.getLength() ? cons.getCharAt(column) : '-';
    boolean hasConservation = val != '-' && val != '0';
    int consp = column - start;
    String tip = (hasConservation && consp > -1 && consp < consSymbs.length)
            ? consSymbs[consp]
            : "";
    return tip;
  }
}
