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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;
import jalview.util.Format;

import java.util.Hashtable;

/**
 * Takes in a vector or array of sequences and column start and column end and
 * returns a new Hashtable[] of size maxSeqLength, if Hashtable not supplied.
 * This class is used extensively in calculating alignment colourschemes that
 * depend on the amount of conservation in each alignment column.
 * 
 * @author $author$
 * @version $Revision$
 */
public class StructureFrequency
{
  public static final int STRUCTURE_PROFILE_LENGTH = 74;

  // No need to store 1000s of strings which are not
  // visible to the user.
  public static final String MAXCOUNT = "C";

  public static final String MAXRESIDUE = "R";

  public static final String PID_GAPS = "G";

  public static final String PID_NOGAPS = "N";

  public static final String PROFILE = "P";

  public static final String PAIRPROFILE = "B";

  /**
   * Returns the 3' position of a base pair
   * 
   * @param pairs
   *          Secondary structure annotation
   * @param indice
   *          5' position of a base pair
   * @return 3' position of a base pair
   */
  public static int findPair(SequenceFeature[] pairs, int indice)
  {

    for (int i = 0; i < pairs.length; i++)
    {
      if (pairs[i].getBegin() == indice)

      {

        return pairs[i].getEnd();

      }
    }
    return -1;
  }

  /**
   * Method to calculate a 'base pair consensus row', very similar to nucleotide
   * consensus but takes into account a given structure
   * 
   * @param sequences
   * @param start
   * @param end
   * @param result
   * @param profile
   * @param rnaStruc
   */
  public static final void calculate(SequenceI[] sequences, int start,
          int end, Hashtable<String, Object>[] result, boolean profile,
          AlignmentAnnotation rnaStruc)
  {

    Hashtable<String, Object> residueHash;
    String maxResidue;
    char[] struc = rnaStruc.getRNAStruc().toCharArray();

    SequenceFeature[] rna = rnaStruc._rnasecstr;
    char c, s, cEnd;
    int bpEnd = -1;
    int jSize = sequences.length;
    int[] values;
    int[][] pairs;
    float percentage;

    for (int i = start; i < end; i++) // foreach column
    {
      int canonicalOrWobblePairCount = 0, canonical = 0;
      int otherPairCount = 0;
      int nongap = 0;
      maxResidue = "-";
      values = new int[255];
      pairs = new int[255][255];
      bpEnd = -1;
      if (i < struc.length)
      {
        s = struc[i];
      }
      else
      {
        s = '-';
      }
      if (s == '.' || s == ' ')
      {
        s = '-';
      }

      if (!Rna.isOpeningParenthesis(s))
      {
        if (s == '-')
        {
          values['-']++;
        }
      }
      else
      {
        bpEnd = findPair(rna, i);

        if (bpEnd > -1)
        {
          for (int j = 0; j < jSize; j++) // foreach row
          {
            if (sequences[j] == null)
            {
              System.err.println(
                      "WARNING: Consensus skipping null sequence - possible race condition.");
              continue;
            }

            c = sequences[j].getCharAt(i);
            cEnd = sequences[j].getCharAt(bpEnd);

            if (Comparison.isGap(c) || Comparison.isGap(cEnd))
            {
              values['-']++;
              continue;
            }
            nongap++;
            /*
             * ensure upper-case for counting purposes
             */
            if ('a' <= c && 'z' >= c)
            {
              c += 'A' - 'a';
            }
            if ('a' <= cEnd && 'z' >= cEnd)
            {
              cEnd += 'A' - 'a';
            }
            if (Rna.isCanonicalOrWobblePair(c, cEnd))
            {
              canonicalOrWobblePairCount++;
              if (Rna.isCanonicalPair(c, cEnd))
              {
                canonical++;
              }
            }
            else
            {
              otherPairCount++;
            }
            pairs[c][cEnd]++;
          }
        }
      }

      residueHash = new Hashtable<>();
      if (profile)
      {
        // TODO 1-dim array with jsize in [0], nongapped in [1]; or Pojo
        residueHash.put(PROFILE,
                new int[][]
                { values, new int[] { jSize, (jSize - values['-']) } });

        residueHash.put(PAIRPROFILE, pairs);
      }
      values['('] = canonicalOrWobblePairCount;
      values['['] = canonical;
      values['{'] = otherPairCount;
      /*
       * the count is the number of valid pairs (as a percentage, determines
       * the relative size of the profile logo)
       */
      int count = canonicalOrWobblePairCount;

      /*
       * display '(' if most pairs are canonical, or as
       * '[' if there are more wobble pairs. 
       */
      if (canonicalOrWobblePairCount > 0 || otherPairCount > 0)
      {
        if (canonicalOrWobblePairCount >= otherPairCount)
        {
          maxResidue = (canonicalOrWobblePairCount - canonical) < canonical
                  ? "("
                  : "[";
        }
        else
        {
          maxResidue = "{";
        }
      }
      residueHash.put(MAXCOUNT, Integer.valueOf(count));
      residueHash.put(MAXRESIDUE, maxResidue);

      percentage = ((float) count * 100) / jSize;
      residueHash.put(PID_GAPS, Float.valueOf(percentage));

      percentage = ((float) count * 100) / nongap;
      residueHash.put(PID_NOGAPS, Float.valueOf(percentage));

      if (result[i] == null)
      {
        result[i] = residueHash;
      }
      if (bpEnd > 0)
      {
        values[')'] = values['('];
        values[']'] = values['['];
        values['}'] = values['{'];
        values['('] = 0;
        values['['] = 0;
        values['{'] = 0;
        maxResidue = maxResidue.equals("(") ? ")"
                : maxResidue.equals("[") ? "]" : "}";

        residueHash = new Hashtable<>();
        if (profile)
        {
          residueHash.put(PROFILE,
                  new int[][]
                  { values, new int[] { jSize, (jSize - values['-']) } });

          residueHash.put(PAIRPROFILE, pairs);
        }

        residueHash.put(MAXCOUNT, Integer.valueOf(count));
        residueHash.put(MAXRESIDUE, maxResidue);

        percentage = ((float) count * 100) / jSize;
        residueHash.put(PID_GAPS, Float.valueOf(percentage));

        percentage = ((float) count * 100) / nongap;
        residueHash.put(PID_NOGAPS, Float.valueOf(percentage));

        result[bpEnd] = residueHash;
      }
    }
  }

  /**
   * Compute all or part of the annotation row from the given consensus
   * hashtable
   * 
   * @param consensus
   *          - pre-allocated annotation row
   * @param hconsensus
   * @param iStart
   * @param width
   * @param ignoreGapsInConsensusCalculation
   * @param includeAllConsSymbols
   */
  public static void completeConsensus(AlignmentAnnotation consensus,
          Hashtable<String, Object>[] hconsensus, int iStart, int width,
          boolean ignoreGapsInConsensusCalculation,
          boolean includeAllConsSymbols, long nseq)
  {
    float tval, value;
    if (consensus == null || consensus.annotations == null
            || consensus.annotations.length < width)
    {
      // called with a bad alignment annotation row - wait for it to be
      // initialised properly
      return;
    }
    String fmtstr = "%3.1f";
    int precision = 2;
    while (nseq > 100)
    {
      precision++;
      nseq /= 10;
    }
    if (precision > 2)
    {
      fmtstr = "%" + (2 + precision) + "." + precision + "f";
    }
    Format fmt = new Format(fmtstr);

    for (int i = iStart; i < width; i++)
    {
      Hashtable<String, Object> hci;
      if (i >= hconsensus.length || ((hci = hconsensus[i]) == null))
      {
        // happens if sequences calculated over were shorter than alignment
        // width
        consensus.annotations[i] = null;
        continue;
      }
      value = 0;
      Float fv;
      if (ignoreGapsInConsensusCalculation)
      {
        fv = (Float) hci.get(StructureFrequency.PID_NOGAPS);
      }
      else
      {
        fv = (Float) hci.get(StructureFrequency.PID_GAPS);
      }
      if (fv == null)
      {
        consensus.annotations[i] = null;
        // data has changed below us .. give up and
        continue;
      }
      value = fv.floatValue();
      String maxRes = hci.get(StructureFrequency.MAXRESIDUE).toString();
      String mouseOver = hci.get(StructureFrequency.MAXRESIDUE) + " ";
      if (maxRes.length() > 1)
      {
        mouseOver = "[" + maxRes + "] ";
        maxRes = "+";
      }
      int[][] profile = (int[][]) hci.get(StructureFrequency.PROFILE);
      int[][] pairs = (int[][]) hci.get(StructureFrequency.PAIRPROFILE);

      if (pairs != null && includeAllConsSymbols) // Just responsible for the
      // tooltip
      // TODO Update tooltips for Structure row
      {
        mouseOver = "";

        /*
         * TODO It's not sure what is the purpose of the alphabet and wheter it
         * is useful for structure?
         * 
         * if (alphabet != null) { for (int c = 0; c < alphabet.length; c++) {
         * tval = ((float) profile[0][alphabet[c]]) 100f / (float)
         * profile[1][ignoreGapsInConsensusCalculation ? 1 : 0]; mouseOver +=
         * ((c == 0) ? "" : "; ") + alphabet[c] + " " + ((int) tval) + "%"; } }
         * else {
         */
        int[][] ca = new int[625][];
        float[] vl = new float[625];
        int x = 0;
        for (int c = 65; c < 90; c++)
        {
          for (int d = 65; d < 90; d++)
          {
            ca[x] = new int[] { c, d };
            vl[x] = pairs[c][d];
            x++;
          }
        }
        jalview.util.QuickSort.sort(vl, ca);
        int p = 0;

        /*
         * profile[1] is {total, ungappedTotal}
         */
        final int divisor = profile[1][ignoreGapsInConsensusCalculation ? 1
                : 0];
        for (int c = 624; c > 0; c--)
        {
          if (vl[c] > 0)
          {
            tval = (vl[c] * 100f / divisor);
            mouseOver += ((p == 0) ? "" : "; ") + (char) ca[c][0]
                    + (char) ca[c][1] + " " + fmt.form(tval) + "%";
            p++;

          }
        }

        // }
      }
      else
      {
        mouseOver += (fmt.form(value) + "%");
      }
      consensus.annotations[i] = new Annotation(maxRes, mouseOver, ' ',
              value);
    }
  }

  /**
   * get the sorted base-pair profile for the given position of the consensus
   * 
   * @param hconsensus
   * @return profile of the given column
   */
  public static int[] extractProfile(Hashtable<String, Object> hconsensus,
          boolean ignoreGapsInConsensusCalculation)
  {
    int[] rtnval = new int[STRUCTURE_PROFILE_LENGTH]; // 2*(5*5)+2
    int[][] profile = (int[][]) hconsensus.get(StructureFrequency.PROFILE);
    int[][] pairs = (int[][]) hconsensus
            .get(StructureFrequency.PAIRPROFILE);

    if (profile == null)
    {
      return null;
    }

    // TODO fix the object length, also do it in completeConsensus
    // Object[] ca = new Object[625];
    int[][] ca = new int[625][];
    float[] vl = new float[625];
    int x = 0;
    for (int c = 65; c < 90; c++)
    {
      for (int d = 65; d < 90; d++)
      {
        ca[x] = new int[] { c, d };
        vl[x] = pairs[c][d];
        x++;
      }
    }
    jalview.util.QuickSort.sort(vl, ca);

    int valuesCount = 0;
    rtnval[1] = 0;
    int offset = 2;
    final int divisor = profile[1][ignoreGapsInConsensusCalculation ? 1
            : 0];
    for (int c = 624; c > 0; c--)
    {
      if (vl[c] > 0)
      {
        rtnval[offset++] = ca[c][0];
        rtnval[offset++] = ca[c][1];
        rtnval[offset] = (int) (vl[c] * 100f / divisor);
        rtnval[1] += rtnval[offset++];
        valuesCount++;
      }
    }
    rtnval[0] = valuesCount;

    // insert profile type code in position 0
    int[] result = new int[rtnval.length + 1];
    result[0] = AlignmentAnnotation.STRUCTURE_PROFILE;
    System.arraycopy(rtnval, 0, result, 1, rtnval.length);
    return result;
  }
}
