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

/**
 * A class to provide sequence pairwise similarity based on residue identity.
 * Instances of this class are immutable and thread-safe, so the same object is
 * returned from calls to getInstance().
 */
public class PIDModel extends SimilarityScoreModel
        implements PairwiseScoreModelI
{
  private static final String NAME = "PID";

  /**
   * Constructor
   */
  public PIDModel()
  {
  }

  @Override
  public String getName()
  {
    return NAME;
  }

  /**
   * Answers null for description. If a display name is needed, use getName() or
   * an internationalized string built from the name.
   */
  @Override
  public String getDescription()
  {
    return null;
  }

  @Override
  public boolean isDNA()
  {
    return true;
  }

  @Override
  public boolean isProtein()
  {
    return true;
  }

  /**
   * Answers 1 if c and d are the same residue (ignoring case), and not gap
   * characters. Answers 0 for non-matching or gap characters.
   */
  @Override
  public float getPairwiseScore(char c, char d)
  {
    c = toUpper(c);
    d = toUpper(d);
    if (c == d && !Comparison.isGap(c))
    {
      return 1f;
    }
    return 0f;
  }

  /**
   * @param c
   */
  protected static char toUpper(char c)
  {
    if ('a' <= c && c <= 'z')
    {
      c += 'A' - 'a';
    }
    return c;
  }

  /**
   * Computes similarity scores based on pairwise percentage identity of
   * sequences. For consistency with Jalview 2.10.1's SeqSpace mode PCA
   * calculation, the percentage scores are rescaled to the width of the
   * sequences (as if counts of identical residues). This method is thread-safe.
   */
  @Override
  public MatrixI findSimilarities(AlignmentView seqData,
          SimilarityParamsI options)
  {
    String[] seqs = seqData.getSequenceStrings(Comparison.GAP_DASH);

    MatrixI result = findSimilarities(seqs, options);

    result.multiply(seqData.getWidth() / 100d);

    return result;
  }

  /**
   * A distance score is computed in the usual way (by reversing the range of
   * the similarity score results), and then rescaled to percentage values
   * (reversing the rescaling to count values done in findSimilarities). This
   * method is thread-safe.
   */
  @Override
  public MatrixI findDistances(AlignmentView seqData,
          SimilarityParamsI options)
  {
    MatrixI result = super.findDistances(seqData, options);

    if (seqData.getWidth() != 0)
    {
      result.multiply(100d / seqData.getWidth());
    }

    return result;
  }

  /**
   * Compute percentage identity scores, using the gap treatment and
   * normalisation specified by the options parameter
   * 
   * @param seqs
   * @param options
   * @return
   */
  protected MatrixI findSimilarities(String[] seqs,
          SimilarityParamsI options)
  {
    /*
     * calculation is symmetric so just compute lower diagonal
     */
    double[][] values = new double[seqs.length][seqs.length];
    for (int row = 0; row < seqs.length; row++)
    {
      for (int col = row; col < seqs.length; col++)
      {
        double total = computePID(seqs[row], seqs[col], options);
        values[row][col] = total;
        values[col][row] = total;
      }
    }
    return new Matrix(values);
  }

  /**
   * Computes a percentage identity for two sequences, using the algorithm
   * choices specified by the options parameter
   * 
   * @param seq1
   * @param seq2
   * @param options
   * @return
   */
  public static double computePID(String seq1, String seq2,
          SimilarityParamsI options)
  {
    int len1 = seq1.length();
    int len2 = seq2.length();
    int width = Math.max(len1, len2);
    int total = 0;
    int divideBy = 0;

    for (int i = 0; i < width; i++)
    {
      if (i >= len1 || i >= len2)
      {
        /*
         * off the end of one sequence; stop if we are only matching
         * on the shorter sequence length, else treat as trailing gap
         */
        if (options.denominateByShortestLength())
        {
          break;
        }
        if (options.includeGaps())
        {
          divideBy++;
        }
        if (options.matchGaps())
        {
          total++;
        }
        continue;
      }
      char c1 = seq1.charAt(i);
      char c2 = seq2.charAt(i);
      boolean gap1 = Comparison.isGap(c1);
      boolean gap2 = Comparison.isGap(c2);

      if (gap1 && gap2)
      {
        /*
         * gap-gap: include if options say so, if so
         * have to score as identity; else ignore
         */
        if (options.includeGappedColumns())
        {
          divideBy++;
          total++;
        }
        continue;
      }

      if (gap1 || gap2)
      {
        /*
         * gap-residue: include if options say so, 
         * count as match if options say so
         */
        if (options.includeGaps())
        {
          divideBy++;
        }
        if (options.matchGaps())
        {
          total++;
        }
        continue;
      }

      /*
       * remaining case is gap-residue
       */
      if (toUpper(c1) == toUpper(c2))
      {
        total++;
      }
      divideBy++;
    }

    return divideBy == 0 ? 0D : 100D * total / divideBy;
  }

  @Override
  public ScoreModelI getInstance(AlignmentViewPanel avp)
  {
    return this;
  }
}
