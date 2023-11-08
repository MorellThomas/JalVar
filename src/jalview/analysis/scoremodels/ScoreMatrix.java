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
package jalview.analysis.scoremodels;

import jalview.api.AlignmentViewPanel;
import jalview.api.analysis.PairwiseScoreModelI;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.AlignmentView;
import jalview.math.Matrix;
import jalview.math.MatrixI;
import jalview.util.Comparison;

import java.util.Arrays;

/**
 * A class that models a substitution score matrix for any given alphabet of
 * symbols. Instances of this class are immutable and thread-safe, so the same
 * object is returned from calls to getInstance().
 */
public class ScoreMatrix extends SimilarityScoreModel
        implements PairwiseScoreModelI
{
  private static final char GAP_CHARACTER = Comparison.GAP_DASH;

  /*
   * an arbitrary score to assign for identity of an unknown symbol
   * (this is the value on the diagonal in the * column of the NCBI matrix)
   * (though a case could be made for using the minimum diagonal value)
   */
  private static final int UNKNOWN_IDENTITY_SCORE = 1;

  /*
   * Jalview 2.10.1 treated gaps as X (peptide) or N (nucleotide)
   * for pairwise scoring; 2.10.2 uses gap score (last column) in
   * score matrix (JAL-2397)
   * Set this flag to true (via Groovy) for 2.10.1 behaviour
   */
  private static boolean scoreGapAsAny = false;

  public static final short UNMAPPED = (short) -1;

  private static final String BAD_ASCII_ERROR = "Unexpected character %s in getPairwiseScore";

  private static final int MAX_ASCII = 127;

  /*
   * the name of the model as shown in menus
   * each score model in use should have a unique name
   */
  private String name;

  /*
   * a description for the model as shown in tooltips
   */
  private String description;

  /*
   * the characters that the model provides scores for
   */
  private char[] symbols;

  /*
   * the score matrix; both dimensions must equal the number of symbols
   * matrix[i][j] is the substitution score for replacing symbols[i] with symbols[j]
   */
  private float[][] matrix;

  /*
   * quick lookup to convert from an ascii character value to the index
   * of the corresponding symbol in the score matrix 
   */
  private short[] symbolIndex;

  /*
   * true for Protein Score matrix, false for dna score matrix
   */
  private boolean peptide;

  private float minValue;

  private float maxValue;

  private boolean symmetric;

  /**
   * Constructor given a name, symbol alphabet, and matrix of scores for pairs
   * of symbols. The matrix should be square and of the same size as the
   * alphabet, for example 20x20 for a 20 symbol alphabet.
   * 
   * @param theName
   *          Unique, human readable name for the matrix
   * @param alphabet
   *          the symbols to which scores apply
   * @param values
   *          Pairwise scores indexed according to the symbol alphabet
   */
  public ScoreMatrix(String theName, char[] alphabet, float[][] values)
  {
    this(theName, null, alphabet, values);
  }

  /**
   * Constructor given a name, description, symbol alphabet, and matrix of
   * scores for pairs of symbols. The matrix should be square and of the same
   * size as the alphabet, for example 20x20 for a 20 symbol alphabet.
   * 
   * @param theName
   *          Unique, human readable name for the matrix
   * @param theDescription
   *          descriptive display name suitable for use in menus
   * @param alphabet
   *          the symbols to which scores apply
   * @param values
   *          Pairwise scores indexed according to the symbol alphabet
   */
  public ScoreMatrix(String theName, String theDescription, char[] alphabet,
          float[][] values)
  {
    if (alphabet.length != values.length)
    {
      throw new IllegalArgumentException(
              "score matrix size must match alphabet size");
    }
    for (float[] row : values)
    {
      if (row.length != alphabet.length)
      {
        throw new IllegalArgumentException(
                "score matrix size must be square");
      }
    }

    this.matrix = values;
    this.name = theName;
    this.description = theDescription;
    this.symbols = alphabet;

    symbolIndex = buildSymbolIndex(alphabet);

    findMinMax();

    symmetric = checkSymmetry();

    /*
     * crude heuristic for now...
     */
    peptide = alphabet.length >= 20;
  }

  /**
   * Answers true if the matrix is symmetric, else false. Usually, substitution
   * matrices are symmetric, which allows calculations to be short cut.
   * 
   * @return
   */
  private boolean checkSymmetry()
  {
    for (int i = 0; i < matrix.length; i++)
    {
      for (int j = i; j < matrix.length; j++)
      {
        if (matrix[i][j] != matrix[j][i])
        {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Record the minimum and maximum score values
   */
  protected void findMinMax()
  {
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    if (matrix != null)
    {
      for (float[] row : matrix)
      {
        if (row != null)
        {
          for (float f : row)
          {
            min = Math.min(min, f);
            max = Math.max(max, f);
          }
        }
      }
    }
    minValue = min;
    maxValue = max;
  }

  /**
   * Returns an array A where A[i] is the position in the alphabet array of the
   * character whose value is i. For example if the alphabet is { 'A', 'D', 'X'
   * } then A['D'] = A[68] = 1.
   * <p>
   * Unmapped characters (not in the alphabet) get an index of -1.
   * <p>
   * Mappings are added automatically for lower case symbols (for non case
   * sensitive scoring), unless they are explicitly present in the alphabet (are
   * scored separately in the score matrix).
   * <p>
   * the gap character (space, dash or dot) included in the alphabet (if any) is
   * recorded in a field
   * 
   * @param alphabet
   * @return
   */
  short[] buildSymbolIndex(char[] alphabet)
  {
    short[] index = new short[MAX_ASCII + 1];
    Arrays.fill(index, UNMAPPED);
    short pos = 0;
    for (char c : alphabet)
    {
      if (c <= MAX_ASCII)
      {
        index[c] = pos;
      }

      /*
       * also map lower-case character (unless separately mapped)
       */
      if (c >= 'A' && c <= 'Z')
      {
        short lowerCase = (short) (c + ('a' - 'A'));
        if (index[lowerCase] == UNMAPPED)
        {
          index[lowerCase] = pos;
        }
      }
      pos++;
    }
    return index;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

  @Override
  public boolean isDNA()
  {
    return !peptide;
  }

  @Override
  public boolean isProtein()
  {
    return peptide;
  }

  /**
   * Returns a copy of the score matrix as used in getPairwiseScore. If using
   * this matrix directly, callers <em>must</em> also call
   * <code>getMatrixIndex</code> in order to get the matrix index for each
   * character (symbol).
   * 
   * @return
   * @see #getMatrixIndex(char)
   */
  public float[][] getMatrix()
  {
    float[][] v = new float[matrix.length][matrix.length];
    for (int i = 0; i < matrix.length; i++)
    {
      v[i] = Arrays.copyOf(matrix[i], matrix[i].length);
    }
    return v;
  }

  /**
   * Answers the matrix index for a given character, or -1 if unmapped in the
   * matrix. Use this method only if using <code>getMatrix</code> in order to
   * compute scores directly (without symbol lookup) for efficiency.
   * 
   * @param c
   * @return
   * @see #getMatrix()
   */
  public int getMatrixIndex(char c)
  {
    if (c < symbolIndex.length)
    {
      return symbolIndex[c];
    }
    else
    {
      return UNMAPPED;
    }
  }

  /**
   * Returns the pairwise score for substituting c with d. If either c or d is
   * an unexpected character, returns 1 for identity (c == d), else the minimum
   * score value in the matrix.
   */
  @Override
  public float getPairwiseScore(char c, char d)
  {
    if (c >= symbolIndex.length)
    {
      System.err.println(String.format(BAD_ASCII_ERROR, c));
      return 0;
    }
    if (d >= symbolIndex.length)
    {
      System.err.println(String.format(BAD_ASCII_ERROR, d));
      return 0;
    }

    int cIndex = symbolIndex[c];
    int dIndex = symbolIndex[d];
    if (cIndex != UNMAPPED && dIndex != UNMAPPED)
    {
      return matrix[cIndex][dIndex];
    }

    /*
     * one or both symbols not found in the matrix
     * currently scoring as 1 (for identity) or the minimum
     * matrix score value (otherwise)
     * (a case could be made for using minimum row/column value instead)
     */
    return c == d ? UNKNOWN_IDENTITY_SCORE : getMinimumScore();
  }

  /**
   * pretty print the matrix
   */
  @Override
  public String toString()
  {
    return outputMatrix(false);
  }

  /**
   * Print the score matrix, optionally formatted as html, with the alphabet
   * symbols as column headings and at the start of each row.
   * <p>
   * The non-html format should give an output which can be parsed as a score
   * matrix file
   * 
   * @param html
   * @return
   */
  public String outputMatrix(boolean html)
  {
    StringBuilder sb = new StringBuilder(512);

    /*
     * heading row with alphabet
     */
    if (html)
    {
      sb.append("<table border=\"1\">");
      sb.append(html ? "<tr><th></th>" : "");
    }
    else
    {
      sb.append("ScoreMatrix ").append(getName()).append("\n");
    }
    for (char sym : symbols)
    {
      if (html)
      {
        sb.append("<th>&nbsp;").append(sym).append("&nbsp;</th>");
      }
      else
      {
        sb.append("\t").append(sym);
      }
    }
    sb.append(html ? "</tr>\n" : "\n");

    /*
     * table of scores
     */
    for (char c1 : symbols)
    {
      if (html)
      {
        sb.append("<tr><td>");
      }
      sb.append(c1).append(html ? "</td>" : "");
      for (char c2 : symbols)
      {
        sb.append(html ? "<td>" : "\t")
                .append(matrix[symbolIndex[c1]][symbolIndex[c2]])
                .append(html ? "</td>" : "");
      }
      sb.append(html ? "</tr>\n" : "\n");
    }
    if (html)
    {
      sb.append("</table>");
    }
    return sb.toString();
  }

  /**
   * Answers the number of symbols coded for (also equal to the number of rows
   * and columns of the score matrix)
   * 
   * @return
   */
  public int getSize()
  {
    return symbols.length;
  }

  /**
   * Computes an NxN matrix where N is the number of sequences, and entry [i, j]
   * is sequence[i] pairwise multiplied with sequence[j], as a sum of scores
   * computed using the current score matrix. For example
   * <ul>
   * <li>Sequences:</li>
   * <li>FKL</li>
   * <li>R-D</li>
   * <li>QIA</li>
   * <li>GWC</li>
   * <li>Score matrix is BLOSUM62</li>
   * <li>Gaps treated same as X (unknown)</li>
   * <li>product [0, 0] = F.F + K.K + L.L = 6 + 5 + 4 = 15</li>
   * <li>product [1, 1] = R.R + -.- + D.D = 5 + -1 + 6 = 10</li>
   * <li>product [2, 2] = Q.Q + I.I + A.A = 5 + 4 + 4 = 13</li>
   * <li>product [3, 3] = G.G + W.W + C.C = 6 + 11 + 9 = 26</li>
   * <li>product[0, 1] = F.R + K.- + L.D = -3 + -1 + -3 = -8
   * <li>and so on</li>
   * </ul>
   * This method is thread-safe.
   */
  @Override
  public MatrixI findSimilarities(AlignmentView seqstrings,
          SimilarityParamsI options)
  {
    char gapChar = scoreGapAsAny ? (seqstrings.isNa() ? 'N' : 'X')
            : GAP_CHARACTER;
    String[] seqs = seqstrings.getSequenceStrings(gapChar);
    return findSimilarities(seqs, options);
  }

  /**
   * Computes pairwise similarities of a set of sequences using the given
   * parameters
   * 
   * @param seqs
   * @param params
   * @return
   */
  protected MatrixI findSimilarities(String[] seqs,
          SimilarityParamsI params)
  {
    double[][] values = new double[seqs.length][seqs.length];
    for (int row = 0; row < seqs.length; row++)
    {
      for (int col = symmetric ? row : 0; col < seqs.length; col++)
      {
        double total = computeSimilarity(seqs[row], seqs[col], params);
        values[row][col] = total;
        if (symmetric)
        {
          values[col][row] = total;
        }
      }
    }
    return new Matrix(values);
  }

  /**
   * Calculates the pairwise similarity of two strings using the given
   * calculation parameters
   * 
   * @param seq1
   * @param seq2
   * @param params
   * @return
   */
  protected double computeSimilarity(String seq1, String seq2,
          SimilarityParamsI params)
  {
    int len1 = seq1.length();
    int len2 = seq2.length();
    double total = 0;

    int width = Math.max(len1, len2);
    for (int i = 0; i < width; i++)
    {
      if (i >= len1 || i >= len2)
      {
        /*
         * off the end of one sequence; stop if we are only matching
         * on the shorter sequence length, else treat as trailing gap
         */
        if (params.denominateByShortestLength())
        {
          break;
        }
      }

      char c1 = i >= len1 ? GAP_CHARACTER : seq1.charAt(i);
      char c2 = i >= len2 ? GAP_CHARACTER : seq2.charAt(i);
      boolean gap1 = Comparison.isGap(c1);
      boolean gap2 = Comparison.isGap(c2);

      if (gap1 && gap2)
      {
        /*
         * gap-gap: include if options say so, else ignore
         */
        if (!params.includeGappedColumns())
        {
          continue;
        }
      }
      else if (gap1 || gap2)
      {
        /*
         * gap-residue: score if options say so
         */
        if (!params.includeGaps())
        {
          continue;
        }
      }
      float score = getPairwiseScore(c1, c2);
      total += score;
    }
    return total;
  }

  /**
   * Answers a hashcode computed from the symbol alphabet and the matrix score
   * values
   */
  @Override
  public int hashCode()
  {
    int hs = Arrays.hashCode(symbols);
    for (float[] row : matrix)
    {
      hs = hs * 31 + Arrays.hashCode(row);
    }
    return hs;
  }

  /**
   * Answers true if the argument is a ScoreMatrix with the same symbol alphabet
   * and score values, else false
   */
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ScoreMatrix))
    {
      return false;
    }
    ScoreMatrix sm = (ScoreMatrix) obj;
    if (Arrays.equals(symbols, sm.symbols)
            && Arrays.deepEquals(matrix, sm.matrix))
    {
      return true;
    }
    return false;
  }

  /**
   * Returns the alphabet the matrix scores for, as a string of characters
   * 
   * @return
   */
  String getSymbols()
  {
    return new String(symbols);
  }

  public float getMinimumScore()
  {
    return minValue;
  }

  public float getMaximumScore()
  {
    return maxValue;
  }

  @Override
  public ScoreModelI getInstance(AlignmentViewPanel avp)
  {
    return this;
  }

  public boolean isSymmetric()
  {
    return symmetric;
  }
}
