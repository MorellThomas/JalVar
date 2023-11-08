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

import jalview.analysis.scoremodels.PIDModel;
import jalview.analysis.scoremodels.ScoreMatrix;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.analysis.scoremodels.SimilarityParams;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.math.MiscMath;
import jalview.util.Comparison;
import jalview.util.Format;
import jalview.util.MapList;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.Graphics;
import java.io.PrintStream;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 
 * 
 * @author $author$
 * @version $Revision$
 */
public class AlignSeq
{
  private static final int MAX_NAME_LENGTH = 30;

  //&!
  //private static final int GAP_OPEN_COST = 120;
  private static final int GAP_OPEN_COST = 100;

  //private static final int GAP_EXTEND_COST = 20;
  private static final int GAP_EXTEND_COST = 5;

  private static final int GAP_INDEX = -1;

  public static final String PEP = "pep";

  public static final String DNA = "dna";

  private static final String NEWLINE = System.lineSeparator();

  float[][] score;

  float alignmentScore;

  float[][] E;

  float[][] F;

  int[][] traceback; // todo is this actually used?

  int[] seq1;

  int[] seq2;

  SequenceI s1;

  SequenceI s2;

  public String s1str;

  public String s2str;

  int maxi;

  int maxj;

  int[] aseq1;

  int[] aseq2;

  public String astr1 = "";

  public String astr2 = "";

  public String indelfreeAstr1 = "";

  public String indelfreeAstr2 = "";

  /** DOCUMENT ME!! */
  public int seq1start;

  /** DOCUMENT ME!! */
  public int seq1end;

  /** DOCUMENT ME!! */
  public int seq2start;

  public int seq2end;

  int count;

  public float maxscore;

  public float meanScore;	//needed for PaSiMap

  public int hypotheticMaxScore; 	// needed for PaSiMap

  int prev = 0;

  StringBuffer output = new StringBuffer();

  String type; // AlignSeq.PEP or AlignSeq.DNA

  private ScoreMatrix scoreMatrix;

  /**
   * Creates a new AlignSeq object.
   * 
   * @param s1
   *          first sequence for alignment
   * @param s2
   *          second sequence for alignment
   * @param type
   *          molecule type, either AlignSeq.PEP or AlignSeq.DNA
   */
  public AlignSeq(SequenceI s1, SequenceI s2, String type)
  {
    seqInit(s1, s1.getSequenceAsString(), s2, s2.getSequenceAsString(),
            type);
  }

  /**
   * Creates a new AlignSeq object.
   * 
   * @param s1
   *          DOCUMENT ME!
   * @param s2
   *          DOCUMENT ME!
   * @param type
   *          DOCUMENT ME!
   */
  public AlignSeq(SequenceI s1, String string1, SequenceI s2,
          String string2, String type)
  {
    seqInit(s1, string1.toUpperCase(Locale.ROOT), s2,
            string2.toUpperCase(Locale.ROOT), type);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public float getMaxScore()
  {
    return maxscore;
  }

  /**
  * returns the overall score of the alignment
  *
  * @return
  */
  public float getAlignmentScore()
  {
    return alignmentScore;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq2Start()
  {
    return seq2start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq2End()
  {
    return seq2end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq1Start()
  {
    return seq1start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getSeq1End()
  {
    return seq1end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getOutput()
  {
    return output.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getAStr1()
  {
    return astr1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getAStr2()
  {
    return astr2;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int[] getASeq1()
  {
    return aseq1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int[] getASeq2()
  {
    return aseq2;
  }

  /**
   * 
   * @return aligned instance of Seq 1
   */
  public SequenceI getAlignedSeq1()
  {
    SequenceI alSeq1 = new Sequence(s1.getName(), getAStr1());
    alSeq1.setStart(s1.getStart() + getSeq1Start() - 1);
    alSeq1.setEnd(s1.getStart() + getSeq1End() - 1);
    alSeq1.setDatasetSequence(
            s1.getDatasetSequence() == null ? s1 : s1.getDatasetSequence());
    return alSeq1;
  }

  /**
   * 
   * @return aligned instance of Seq 2
   */
  public SequenceI getAlignedSeq2()
  {
    SequenceI alSeq2 = new Sequence(s2.getName(), getAStr2());
    alSeq2.setStart(s2.getStart() + getSeq2Start() - 1);
    alSeq2.setEnd(s2.getStart() + getSeq2End() - 1);
    alSeq2.setDatasetSequence(
            s2.getDatasetSequence() == null ? s2 : s2.getDatasetSequence());
    return alSeq2;
  }

  /**
   * Construct score matrix for sequences with standard DNA or PEPTIDE matrix
   * 
   * @param s1
   *          - sequence 1
   * @param string1
   *          - string to use for s1
   * @param s2
   *          - sequence 2
   * @param string2
   *          - string to use for s2
   * @param type
   *          DNA or PEPTIDE
   */
  public void seqInit(SequenceI s1, String string1, SequenceI s2,
          String string2, String type)
  {
    this.s1 = s1;
    this.s2 = s2;
    setDefaultParams(type);
    seqInit(string1, string2);
  }

  /**
   * construct score matrix for string1 and string2 (after removing any existing
   * gaps
   * 
   * @param string1
   * @param string2
   */
  private void seqInit(String string1, String string2)
  {
    s1str = extractGaps(jalview.util.Comparison.GapChars, string1);
    s2str = extractGaps(jalview.util.Comparison.GapChars, string2);

    if (s1str.length() == 0 || s2str.length() == 0)
    {
      output.append(
              "ALL GAPS: " + (s1str.length() == 0 ? s1.getName() : " ")
                      + (s2str.length() == 0 ? s2.getName() : ""));
      return;
    }

    score = new float[s1str.length()][s2str.length()];

    E = new float[s1str.length()][s2str.length()];

    F = new float[s1str.length()][s2str.length()];
    traceback = new int[s1str.length()][s2str.length()];

    seq1 = indexEncode(s1str);

    seq2 = indexEncode(s2str);
  }

  private void setDefaultParams(String moleculeType)
  {
    if (!PEP.equals(moleculeType) && !DNA.equals(moleculeType))
    {
      output.append("Wrong type = dna or pep only");
      throw new Error(MessageManager
              .formatMessage("error.unknown_type_dna_or_pep", new String[]
              { moleculeType }));
    }

    type = moleculeType;
    scoreMatrix = ScoreModels.getInstance()
            .getDefaultModel(PEP.equals(type));
  }

  /**
   * DOCUMENT ME!
   */
  public void traceAlignment()
  {
    // Find the maximum score along the rhs or bottom row
    float max = -Float.MAX_VALUE;

    for (int i = 0; i < seq1.length; i++)
    {
      if (score[i][seq2.length - 1] > max)
      {
        max = score[i][seq2.length - 1];
        maxi = i;
        maxj = seq2.length - 1;
      }
    }

    for (int j = 0; j < seq2.length; j++)
    {
      if (score[seq1.length - 1][j] > max)
      {
        max = score[seq1.length - 1][j];
        maxi = seq1.length - 1;
        maxj = j;
      }
    }

    int i = maxi;
    int j = maxj;
    int trace;
    maxscore = score[i][j] / 10f;


    aseq1 = new int[seq1.length + seq2.length];
    aseq2 = new int[seq1.length + seq2.length];

    StringBuilder sb1 = new StringBuilder(aseq1.length);
    StringBuilder sb2 = new StringBuilder(aseq2.length);

    count = (seq1.length + seq2.length) - 1;


    while (i > 0 && j > 0)
    {
      aseq1[count] = seq1[i];
      sb1.append(s1str.charAt(i));
      aseq2[count] = seq2[j];
      sb2.append(s2str.charAt(j));

      trace = findTrace(i, j);

      if (trace == 0)
      {
        i--;
        j--;
      }
      else if (trace == 1)
      {
        j--;
        aseq1[count] = GAP_INDEX;
        sb1.replace(sb1.length() - 1, sb1.length(), "-");
      }
      else if (trace == -1)
      {
        i--;
        aseq2[count] = GAP_INDEX;
        sb2.replace(sb2.length() - 1, sb2.length(), "-");
      }

      count--;
    }

    seq1start = i + 1;
    seq2start = j + 1;

    if (aseq1[count] != GAP_INDEX)
    {
      aseq1[count] = seq1[i];
      sb1.append(s1str.charAt(i));
    }

    if (aseq2[count] != GAP_INDEX)
    {
      aseq2[count] = seq2[j];
      sb2.append(s2str.charAt(j));
    }


    /*
     * we built the character strings backwards, so now
     * reverse them to convert to sequence strings
     */
    astr1 = sb1.reverse().toString();
    astr2 = sb2.reverse().toString();
  }

  /**
   * DOCUMENT ME!
   */
  public void traceAlignmentWithEndGaps()
  {
    // Find the maximum score along the rhs or bottom row
    float max = -Float.MAX_VALUE;

    for (int i = 0; i < seq1.length; i++)
    {
      if (score[i][seq2.length - 1] > max)
      {
        max = score[i][seq2.length - 1];
        maxi = i;
        maxj = seq2.length - 1;
      }
    }

    for (int j = 0; j < seq2.length; j++)
    {
      if (score[seq1.length - 1][j] > max)
      {
        max = score[seq1.length - 1][j];
        maxi = seq1.length - 1;
        maxj = j;
      }
    }

    int i = maxi;
    int j = maxj;
    int trace;
    maxscore = score[i][j] / 10f;

    //prepare trailing gaps
    while ((i < seq1.length - 1) || (j < seq2.length - 1))
    {
      i++;
      j++;
    }
    seq1end = i + 1;
    seq2end = j + 1;

    aseq1 = new int[seq1.length + seq2.length];
    aseq2 = new int[seq1.length + seq2.length];

    StringBuilder sb1 = new StringBuilder(aseq1.length);
    StringBuilder sb2 = new StringBuilder(aseq2.length);

    count = (seq1.length + seq2.length) - 1;

    //get trailing gaps
    while ((i >= seq1.length) || (j >= seq2.length))
    {
      if (i >= seq1.length)
      {
	aseq1[count] = GAP_INDEX;
	sb1.append("-");
	aseq2[count] = seq2[j];
	sb2.append(s2str.charAt(j));
      } else if (j >= seq2.length) {
	aseq1[count] = seq1[i];
	sb1.append(s1str.charAt(i));
	aseq2[count] = GAP_INDEX;
	sb2.append("-");
      }
      i--;
      j--;
    }

    while (i > 0 && j > 0)
    {
      aseq1[count] = seq1[i];
      sb1.append(s1str.charAt(i));
      aseq2[count] = seq2[j];
      sb2.append(s2str.charAt(j));

      trace = findTrace(i, j);

      if (trace == 0)
      {
        i--;
        j--;
      }
      else if (trace == 1)
      {
        j--;
        aseq1[count] = GAP_INDEX;
        sb1.replace(sb1.length() - 1, sb1.length(), "-");
      }
      else if (trace == -1)
      {
        i--;
        aseq2[count] = GAP_INDEX;
        sb2.replace(sb2.length() - 1, sb2.length(), "-");
      }

      count--;
    }

    seq1start = i + 1;
    seq2start = j + 1;

    aseq1[count] = seq1[i];
    sb1.append(s1str.charAt(i));
    aseq2[count] = seq2[j];
    sb2.append(s2str.charAt(j));

    //get initial gaps
    while (j > 0 || i > 0)
    {
      if (j > 0)
      {
	j--;
	sb1.append("-");
	sb2.append(s2str.charAt(j));
      } else if (i > 0) {
	i--;
	sb1.append(s1str.charAt(i));
	sb2.append("-");
      }
    }

    /*
     * we built the character strings backwards, so now
     * reverse them to convert to sequence strings
     */
    astr1 = sb1.reverse().toString();
    astr2 = sb2.reverse().toString();
  }

  /**
   * DOCUMENT ME!
   */
  public void printAlignment(PrintStream os)
  {
    // TODO: Use original sequence characters rather than re-translated
    // characters in output
    // Find the biggest id length for formatting purposes
    String s1id = getAlignedSeq1().getDisplayId(true);
    String s2id = getAlignedSeq2().getDisplayId(true);
    int nameLength = Math.max(s1id.length(), s2id.length());
    if (nameLength > MAX_NAME_LENGTH)
    {
      int truncateBy = nameLength - MAX_NAME_LENGTH;
      nameLength = MAX_NAME_LENGTH;
      // JAL-527 - truncate the sequence ids
      if (s1id.length() > nameLength)
      {
        int slashPos = s1id.lastIndexOf('/');
        s1id = s1id.substring(0, slashPos - truncateBy)
                + s1id.substring(slashPos);
      }
      if (s2id.length() > nameLength)
      {
        int slashPos = s2id.lastIndexOf('/');
        s2id = s2id.substring(0, slashPos - truncateBy)
                + s2id.substring(slashPos);
      }
    }
    int len = 72 - nameLength - 1;
    int nochunks = ((aseq1.length - count) / len)
            + ((aseq1.length - count) % len > 0 ? 1 : 0);
    float pid = 0f;

    output.append("Score = ").append(score[maxi][maxj]).append(NEWLINE);
    output.append("Length of alignment = ")
            .append(String.valueOf(aseq1.length - count)).append(NEWLINE);
    output.append("Sequence ");
    Format nameFormat = new Format("%" + nameLength + "s");
    output.append(nameFormat.form(s1id));
    output.append(" (Sequence length = ")
            .append(String.valueOf(s1str.length())).append(")")
            .append(NEWLINE);
    output.append("Sequence ");
    output.append(nameFormat.form(s2id));
    output.append(" (Sequence length = ")
            .append(String.valueOf(s2str.length())).append(")")
            .append(NEWLINE).append(NEWLINE);

    ScoreMatrix pam250 = ScoreModels.getInstance().getPam250();

    for (int j = 0; j < nochunks; j++)
    {
      // Print the first aligned sequence
      output.append(nameFormat.form(s1id)).append(" ");

      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < astr1.length())
        {
          output.append(astr1.charAt(i + (j * len)));
        }
      }

      output.append(NEWLINE);
      output.append(nameFormat.form(" ")).append(" ");

      /*
       * Print out the match symbols:
       * | for exact match (ignoring case)
       * . if PAM250 score is positive
       * else a space
       */
      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < astr1.length())
        {
          char c1 = astr1.charAt(i + (j * len));
          char c2 = astr2.charAt(i + (j * len));
          boolean sameChar = Comparison.isSameResidue(c1, c2, false);
          if (sameChar && !Comparison.isGap(c1))
          {
            pid++;
            output.append("|");
          }
          else if (PEP.equals(type))
          {
            if (pam250.getPairwiseScore(c1, c2) > 0)
            {
              output.append(".");
            }
            else
            {
              output.append(" ");
            }
          }
          else
          {
            output.append(" ");
          }
        }
      }

      // Now print the second aligned sequence
      output = output.append(NEWLINE);
      output = output.append(nameFormat.form(s2id)).append(" ");

      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < astr2.length())
        {
          output.append(astr2.charAt(i + (j * len)));
        }
      }

      output.append(NEWLINE).append(NEWLINE);
    }

    pid = pid / (aseq1.length - count) * 100;
    output.append(new Format("Percentage ID = %3.2f\n").form(pid));
    output.append(NEWLINE);
    try
    {
      os.print(output.toString());
    } catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int findTrace(int i, int j)
  {
    int t = 0;
    float pairwiseScore = scoreMatrix.getPairwiseScore(s1str.charAt(i),
            s2str.charAt(j));
    float max = score[i - 1][j - 1] + (pairwiseScore * 10);

    if (F[i][j] > max)
    {
      max = F[i][j];
      t = -1;
    }
    else if (F[i][j] == max)
    {
      if (prev == -1)
      {
        max = F[i][j];
        t = -1;
      }
    }

    if (E[i][j] >= max)
    {
      max = E[i][j];
      t = 1;
    }
    else if (E[i][j] == max)
    {
      if (prev == 1)
      {
        max = E[i][j];
        t = 1;
      }
    }

    prev = t;

    return t;
  }

  /**
   * DOCUMENT ME!
   */
  public void calcScoreMatrix()
  {
    int n = seq1.length;
    int m = seq2.length;

    // top left hand element
    score[0][0] = scoreMatrix.getPairwiseScore(s1str.charAt(0),
            s2str.charAt(0)) * 10;
    E[0][0] = -GAP_EXTEND_COST;
    F[0][0] = 0;

    // Calculate the top row first
    for (int j = 1; j < m; j++)
    {
      // What should these values be? 0 maybe
      E[0][j] = max(score[0][j - 1] - GAP_OPEN_COST,
              E[0][j - 1] - GAP_EXTEND_COST);
      F[0][j] = -GAP_EXTEND_COST;

      float pairwiseScore = scoreMatrix.getPairwiseScore(s1str.charAt(0),
              s2str.charAt(j));
      score[0][j] = max(pairwiseScore * 10, -GAP_OPEN_COST,
              -GAP_EXTEND_COST);

      traceback[0][j] = 1;
    }

    // Now do the left hand column
    for (int i = 1; i < n; i++)
    {
      E[i][0] = -GAP_OPEN_COST;
      F[i][0] = max(score[i - 1][0] - GAP_OPEN_COST,
              F[i - 1][0] - GAP_EXTEND_COST);

      float pairwiseScore = scoreMatrix.getPairwiseScore(s1str.charAt(i),
              s2str.charAt(0));
      score[i][0] = max(pairwiseScore * 10, E[i][0], F[i][0]);
      traceback[i][0] = -1;
    }

    // Now do all the other rows
    for (int i = 1; i < n; i++)
    {
      for (int j = 1; j < m; j++)
      {
        E[i][j] = max(score[i][j - 1] - GAP_OPEN_COST,
                E[i][j - 1] - GAP_EXTEND_COST);
        F[i][j] = max(score[i - 1][j] - GAP_OPEN_COST,
                F[i - 1][j] - GAP_EXTEND_COST);

        float pairwiseScore = scoreMatrix.getPairwiseScore(s1str.charAt(i),
                s2str.charAt(j));
        score[i][j] = max(score[i - 1][j - 1] + (pairwiseScore * 10),
                E[i][j], F[i][j]);
        traceback[i][j] = findTrace(i, j);
      }
    }
  }

  /**
   * Returns the given sequence with all of the given gap characters removed.
   * 
   * @param gapChars
   *          a string of characters to be treated as gaps
   * @param seq
   *          the input sequence
   * 
   * @return
   */
  public static String extractGaps(String gapChars, String seq)
  {
    if (gapChars == null || seq == null)
    {
      return null;
    }
    StringTokenizer str = new StringTokenizer(seq, gapChars);
    StringBuilder newString = new StringBuilder(seq.length());

    while (str.hasMoreTokens())
    {
      newString.append(str.nextToken());
    }

    return newString.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param f1
   *          DOCUMENT ME!
   * @param f2
   *          DOCUMENT ME!
   * @param f3
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  private static float max(float f1, float f2, float f3)
  {
    float max = f1;

    if (f2 > f1)
    {
      max = f2;
    }

    if (f3 > max)
    {
      max = f3;
    }

    return max;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param f1
   *          DOCUMENT ME!
   * @param f2
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  private static float max(float f1, float f2)
  {
    float max = f1;

    if (f2 > f1)
    {
      max = f2;
    }

    return max;
  }

  /**
   * Converts the character string to an array of integers which are the
   * corresponding indices to the characters in the score matrix
   * 
   * @param s
   * 
   * @return
   */
  int[] indexEncode(String s)
  {
    int[] encoded = new int[s.length()];

    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      encoded[i] = scoreMatrix.getMatrixIndex(c);
    }

    return encoded;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param mat
   *          DOCUMENT ME!
   * @param n
   *          DOCUMENT ME!
   * @param m
   *          DOCUMENT ME!
   * @param psize
   *          DOCUMENT ME!
   */
  public static void displayMatrix(Graphics g, int[][] mat, int n, int m,
          int psize)
  {
    // TODO method doesn't seem to be referenced anywhere delete??
    int max = -1000;
    int min = 1000;

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < m; j++)
      {
        if (mat[i][j] >= max)
        {
          max = mat[i][j];
        }

        if (mat[i][j] <= min)
        {
          min = mat[i][j];
        }
      }
    }

    System.out.println(max + " " + min);

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < m; j++)
      {
        int x = psize * i;
        int y = psize * j;

        // System.out.println(mat[i][j]);
        float score = (float) (mat[i][j] - min) / (float) (max - min);
        g.setColor(new Color(score, 0, 0));
        g.fillRect(x, y, psize, psize);

        // System.out.println(x + " " + y + " " + score);
      }
    }
  }

  /**
   * Compute a globally optimal needleman and wunsch alignment between two
   * sequences
   * 
   * @param s1
   * @param s2
   * @param type
   *          AlignSeq.DNA or AlignSeq.PEP
   */
  public static AlignSeq doGlobalNWAlignment(SequenceI s1, SequenceI s2,
          String type)
  {
    AlignSeq as = new AlignSeq(s1, s2, type);

    as.calcScoreMatrix();
    as.traceAlignment();
    return as;
  }

  /**
   * 
   * @return mapping from positions in S1 to corresponding positions in S2
   */
  public jalview.datamodel.Mapping getMappingFromS1(boolean allowmismatch)
  {
    ArrayList<Integer> as1 = new ArrayList<Integer>(),
            as2 = new ArrayList<Integer>();
    int pdbpos = s2.getStart() + getSeq2Start() - 2;
    int alignpos = s1.getStart() + getSeq1Start() - 2;
    int lp2 = pdbpos - 3, lp1 = alignpos - 3;
    boolean lastmatch = false;
    // and now trace the alignment onto the atom set.
    for (int i = 0; i < astr1.length(); i++)
    {
      char c1 = astr1.charAt(i), c2 = astr2.charAt(i);
      if (c1 != '-')
      {
        alignpos++;
      }

      if (c2 != '-')
      {
        pdbpos++;
      }

      // ignore case differences
      if (allowmismatch || (c1 == c2) || (Math.abs(c2-c1)==('a'-'A')))
      {
        // extend mapping interval
        if (lp1 + 1 != alignpos || lp2 + 1 != pdbpos)
        {
          as1.add(Integer.valueOf(alignpos));
          as2.add(Integer.valueOf(pdbpos));
        }
        lastmatch = true;
        lp1 = alignpos;
        lp2 = pdbpos;
      }
      else
      {
        // extend mapping interval
        if (lastmatch)
        {
          as1.add(Integer.valueOf(lp1));
          as2.add(Integer.valueOf(lp2));
        }
        lastmatch = false;
      }
    }
    // construct range pairs

    int[] mapseq1 = new int[as1.size() + (lastmatch ? 1 : 0)],
            mapseq2 = new int[as2.size() + (lastmatch ? 1 : 0)];
    int i = 0;
    for (Integer ip : as1)
    {
      mapseq1[i++] = ip;
    }
    ;
    i = 0;
    for (Integer ip : as2)
    {
      mapseq2[i++] = ip;
    }
    ;
    if (lastmatch)
    {
      mapseq1[mapseq1.length - 1] = alignpos;
      mapseq2[mapseq2.length - 1] = pdbpos;
    }
    MapList map = new MapList(mapseq1, mapseq2, 1, 1);

    jalview.datamodel.Mapping mapping = new Mapping(map);
    mapping.setTo(s2);
    return mapping;
  }

  /**
   * matches ochains against al and populates seqs with the best match between
   * each ochain and the set in al
   * 
   * @param ochains
   * @param al
   * @param dnaOrProtein
   * @param removeOldAnnots
   *          when true, old annotation is cleared before new annotation
   *          transferred
   * @return List<List<SequenceI> originals, List<SequenceI> replacement,
   *         List<AlignSeq> alignment between each>
   */
  public static List<List<? extends Object>> replaceMatchingSeqsWith(
          List<SequenceI> seqs, List<AlignmentAnnotation> annotations,
          List<SequenceI> ochains, AlignmentI al, String dnaOrProtein,
          boolean removeOldAnnots)
  {
    List<SequenceI> orig = new ArrayList<SequenceI>(),
            repl = new ArrayList<SequenceI>();
    List<AlignSeq> aligs = new ArrayList<AlignSeq>();
    if (al != null && al.getHeight() > 0)
    {
      ArrayList<SequenceI> matches = new ArrayList<SequenceI>();
      ArrayList<AlignSeq> aligns = new ArrayList<AlignSeq>();

      for (SequenceI sq : ochains)
      {
        SequenceI bestm = null;
        AlignSeq bestaseq = null;
        float bestscore = 0;
        for (SequenceI msq : al.getSequences())
        {
          AlignSeq aseq = doGlobalNWAlignment(msq, sq, dnaOrProtein);
          if (bestm == null || aseq.getMaxScore() > bestscore)
          {
            bestscore = aseq.getMaxScore();
            bestaseq = aseq;
            bestm = msq;
          }
        }
        // System.out.println("Best Score for " + (matches.size() + 1) + " :"
        // + bestscore);
        matches.add(bestm);
        aligns.add(bestaseq);
        al.deleteSequence(bestm);
      }
      for (int p = 0, pSize = seqs.size(); p < pSize; p++)
      {
        SequenceI sq, sp = seqs.get(p);
        int q;
        if ((q = ochains.indexOf(sp)) > -1)
        {
          seqs.set(p, sq = matches.get(q));
          orig.add(sp);
          repl.add(sq);
          sq.setName(sp.getName());
          sq.setDescription(sp.getDescription());
          Mapping sp2sq;
          sq.transferAnnotation(sp,
                  sp2sq = aligns.get(q).getMappingFromS1(false));
          aligs.add(aligns.get(q));
          int inspos = -1;
          for (int ap = 0; ap < annotations.size();)
          {
            if (annotations.get(ap).sequenceRef == sp)
            {
              if (inspos == -1)
              {
                inspos = ap;
              }
              if (removeOldAnnots)
              {
                annotations.remove(ap);
              }
              else
              {
                AlignmentAnnotation alan = annotations.remove(ap);
                alan.liftOver(sq, sp2sq);
                alan.setSequenceRef(sq);
                sq.addAlignmentAnnotation(alan);
              }
            }
            else
            {
              ap++;
            }
          }
          if (sq.getAnnotation() != null && sq.getAnnotation().length > 0)
          {
            annotations.addAll(inspos == -1 ? annotations.size() : inspos,
                    Arrays.asList(sq.getAnnotation()));
          }
        }
      }
    }
    return Arrays.asList(orig, repl, aligs);
  }

  /**
   * compute the PID vector used by the redundancy filter.
   * 
   * @param originalSequences
   *          - sequences in alignment that are to filtered
   * @param omitHidden
   *          - null or strings to be analysed (typically, visible portion of
   *          each sequence in alignment)
   * @param start
   *          - first column in window for calculation
   * @param end
   *          - last column in window for calculation
   * @param ungapped
   *          - if true then use ungapped sequence to compute PID
   * @return vector containing maximum PID for i-th sequence and any sequences
   *         longer than that seuqence
   */
  public static float[] computeRedundancyMatrix(
          SequenceI[] originalSequences, String[] omitHidden, int start,
          int end, boolean ungapped)
  {
    int height = originalSequences.length;
    float[] redundancy = new float[height];
    int[] lngth = new int[height];
    for (int i = 0; i < height; i++)
    {
      redundancy[i] = 0f;
      lngth[i] = -1;
    }

    // long start = System.currentTimeMillis();

    SimilarityParams pidParams = new SimilarityParams(true, true, true,
            true);
    float pid;
    String seqi, seqj;
    for (int i = 0; i < height; i++)
    {

      for (int j = 0; j < i; j++)
      {
        if (i == j)
        {
          continue;
        }

        if (omitHidden == null)
        {
          seqi = originalSequences[i].getSequenceAsString(start, end);
          seqj = originalSequences[j].getSequenceAsString(start, end);
        }
        else
        {
          seqi = omitHidden[i];
          seqj = omitHidden[j];
        }
        if (lngth[i] == -1)
        {
          String ug = AlignSeq.extractGaps(Comparison.GapChars, seqi);
          lngth[i] = ug.length();
          if (ungapped)
          {
            seqi = ug;
          }
        }
        if (lngth[j] == -1)
        {
          String ug = AlignSeq.extractGaps(Comparison.GapChars, seqj);
          lngth[j] = ug.length();
          if (ungapped)
          {
            seqj = ug;
          }
        }
        pid = (float) PIDModel.computePID(seqi, seqj, pidParams);

        // use real sequence length rather than string length
        if (lngth[j] < lngth[i])
        {
          redundancy[j] = Math.max(pid, redundancy[j]);
        }
        else
        {
          redundancy[i] = Math.max(pid, redundancy[i]);
        }

      }
    }
    return redundancy;
  }

  /**
  * calculate the mean score of the alignment
  * mean score is equal to the score of an alignmenet of two sequences with randomly shuffled AA sequence composited of the same AA as the two original sequences
  *
  */
  public void meanScore()
  {
    int length = indelfreeAstr1.length();	//both have the same length
    //create HashMap for counting residues in each sequence
    HashMap<Character, Integer> seq1ResCount = new HashMap<Character, Integer>();
    HashMap<Character, Integer> seq2ResCount = new HashMap<Character, Integer>();

    // for both sequences (String indelfreeAstr1 or 2) create a key for the residue and add 1 each time its encountered
    for (char residue: indelfreeAstr1.toCharArray())
    {
      seq1ResCount.putIfAbsent(residue, 0);
      seq1ResCount.replace(residue, seq1ResCount.get(residue) + 1);
    }
    for (char residue: indelfreeAstr2.toCharArray())
    {
      seq2ResCount.putIfAbsent(residue, 0);
      seq2ResCount.replace(residue, seq2ResCount.get(residue) + 1);
    }

    // meanscore = for each residue pair get the number of appearance and add (countA * countB * pairwiseScore(AB))
    // divide the meanscore by the sequence length afterwards
    float _meanscore = 0;
    for (char resA : seq1ResCount.keySet())
    {
      for (char resB : seq2ResCount.keySet())
      {
	int countA = seq1ResCount.get(resA);
	int countB = seq2ResCount.get(resB);

        float scoreAB = scoreMatrix.getPairwiseScore(resA, resB);

	_meanscore += countA *  countB * scoreAB;
      }
    }
    _meanscore /= length;
    this.meanScore = _meanscore;
  }

  public float getMeanScore()
  {
    return this.meanScore;
  }

  /**
  * calculate the hypothetic max score using the self-alignment of the sequences
  */
  public void hypotheticMaxScore()
  {
    int _hmsA = 0;
    int _hmsB = 0;
    for (char residue: indelfreeAstr1.toCharArray())
    {
      _hmsA += scoreMatrix.getPairwiseScore(residue, residue);
    }
    for (char residue: indelfreeAstr2.toCharArray())
    {
      _hmsB += scoreMatrix.getPairwiseScore(residue, residue);
    }
    this.hypotheticMaxScore = (_hmsA < _hmsB) ? _hmsA : _hmsB;	// take the lower self alignment

  }

  public int getHypotheticMaxScore()
  {
    return this.hypotheticMaxScore;
  }

  /**
  * create strings based of astr1 and astr2 but without gaps
  */
  public void getIndelfreeAstr()
  {
    int n = astr1.length();	// both have the same length
    for (int i = 0; i < n; i++)
    {
      if (Character.isLetter(astr1.charAt(i)) && Character.isLetter(astr2.charAt(i)))	// if both sequences dont have a gap -> add to indelfreeAstr
      {
	this.indelfreeAstr1 += astr1.charAt(i);
	this.indelfreeAstr2 += astr2.charAt(i);
      }
    }
  }

  /**
  * calculates the overall score of the alignment
  * preprescore = sum of all scores - all penalties
  * if preprescore < 1 ~ alignmentScore = Float.NaN 	>
  * alignmentScore = ((preprescore - meanScore) / (hypotheticMaxScore - meanScore)) * coverage
  */
  public void scoreAlignment()
  {

    getIndelfreeAstr();
    meanScore();
    hypotheticMaxScore();
    // cannot calculate score because denominator would be zero
    if (this.hypotheticMaxScore == this.meanScore)
    {
      this.alignmentScore = Float.NaN;
      return;
    }
    int n = indelfreeAstr1.length();

    float score = 0;
    boolean aGapOpen = false;
    boolean bGapOpen = false;
    for (int i = 0; i < n; i++)
    {
      char char1 = indelfreeAstr1.charAt(i);
      char char2 = indelfreeAstr2.charAt(i);
      boolean aIsLetter = Character.isLetter(char1);
      boolean bIsLetter = Character.isLetter(char2);
      if (aIsLetter && bIsLetter)	// if pair -> get score
      {
        score += scoreMatrix.getPairwiseScore(char1, char2);
      } else if (!aIsLetter && !bIsLetter) {	// both are gap -> skip
      } else if ((!aIsLetter && aGapOpen) || (!bIsLetter && bGapOpen)) {	// one side gapopen -> score - gap_extend
	score -= GAP_EXTEND_COST;
      } else {		// no gap open -> score - gap_open
	score -= GAP_OPEN_COST;
      }
      // adjust GapOpen status in both sequences
      aGapOpen = (!aIsLetter) ? true : false;
      bGapOpen = (!bIsLetter) ? true : false;
    }

    float preprescore = score;	// if this score < 1 --> alignment score = Float.NaN
    score = (score - this.meanScore) / (this.hypotheticMaxScore - this.meanScore);
    int[] _max = MiscMath.findMax(new int[]{astr1.replace("-","").length(), astr2.replace("-","").length()});	// {index of max, max}
    float coverage = (float) n / (float) _max[1];	// indelfreeAstr length / longest sequence length
    float prescore = score;	// only debug
    score *= coverage;

    //System.out.println(String.format("prepre-score: %f, pre-score: %f, longlength: %d\nscore: %1.16f, mean: %f, max: %d", preprescore, prescore, _max[1], score, this.meanScore, this.hypotheticMaxScore));
    float minScore = 1f;
    this.alignmentScore = (preprescore < minScore) ? Float.NaN : score;
  }
}
