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

import jalview.api.analysis.SimilarityParamsI;

/**
 * A class to hold parameters that configure the pairwise similarity
 * calculation. Based on the paper
 * 
 * <pre>
 * Quantification of the variation in percentage identity for protein sequence alignments
 * Raghava, GP and Barton, GJ
 * BMC Bioinformatics. 2006 Sep 19;7:415
 * </pre>
 * 
 * @see https://www.ncbi.nlm.nih.gov/pubmed/16984632
 */
public class SimilarityParams implements SimilarityParamsI
{
  /**
   * Based on Jalview's Comparison.PID method, which includes gaps and counts
   * them as matching; it counts over the length of the shorter sequence
   */
  public static final SimilarityParamsI Jalview = new SimilarityParams(true,
          true, true, true);

  /**
   * 'SeqSpace' mode PCA calculation includes gaps but does not count them as
   * matching; it uses the longest sequence length
   */
  public static final SimilarityParamsI SeqSpace = new SimilarityParams(
          true, false, true, true);

  /**
   * as described in the Raghava-Barton paper
   * <ul>
   * <li>ignores gap-gap</li>
   * <li>does not score gap-residue</li>
   * <li>includes gap-residue in lengths</li>
   * <li>matches on longer of two sequences</li>
   * </ul>
   */
  public static final SimilarityParamsI PID1 = new SimilarityParams(false,
          false, true, false);

  /**
   * as described in the Raghava-Barton paper
   * <ul>
   * <li>ignores gap-gap</li>
   * <li>ignores gap-residue</li>
   * <li>matches on longer of two sequences</li>
   * </ul>
   */
  public static final SimilarityParamsI PID2 = new SimilarityParams(false,
          false, false, false);

  /**
   * as described in the Raghava-Barton paper
   * <ul>
   * <li>ignores gap-gap</li>
   * <li>ignores gap-residue</li>
   * <li>matches on shorter of sequences only</li>
   * </ul>
   */
  public static final SimilarityParamsI PID3 = new SimilarityParams(false,
          false, false, true);

  /**
   * as described in the Raghava-Barton paper
   * <ul>
   * <li>ignores gap-gap</li>
   * <li>does not score gap-residue</li>
   * <li>includes gap-residue in lengths</li>
   * <li>matches on shorter of sequences only</li>
   * </ul>
   */
  public static final SimilarityParamsI PID4 = new SimilarityParams(false,
          false, true, true);

  private boolean includeGappedColumns;

  private boolean matchGaps;

  private boolean includeGaps;

  private boolean denominateByShortestLength;

  /**
   * Constructor
   * 
   * @param includeGapGap
   * @param matchGapResidue
   * @param includeGapResidue
   *          if true, gapped positions are counted for normalisation by length
   * @param shortestLength
   *          if true, the denominator is the shorter sequence length (possibly
   *          including gaps)
   */
  public SimilarityParams(boolean includeGapGap, boolean matchGapResidue,
          boolean includeGapResidue, boolean shortestLength)
  {
    includeGappedColumns = includeGapGap;
    matchGaps = matchGapResidue;
    includeGaps = includeGapResidue;
    denominateByShortestLength = shortestLength;
  }

  @Override
  public boolean includeGaps()
  {
    return includeGaps;
  }

  @Override
  public boolean denominateByShortestLength()
  {
    return denominateByShortestLength;
  }

  @Override
  public boolean includeGappedColumns()
  {
    return includeGappedColumns;
  }

  @Override
  public boolean matchGaps()
  {
    return matchGaps;
  }

  /**
   * IDE-generated hashCode method
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (denominateByShortestLength ? 1231 : 1237);
    result = prime * result + (includeGappedColumns ? 1231 : 1237);
    result = prime * result + (includeGaps ? 1231 : 1237);
    result = prime * result + (matchGaps ? 1231 : 1237);
    return result;
  }

  /**
   * IDE-generated equals method
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    SimilarityParams other = (SimilarityParams) obj;
    if (denominateByShortestLength != other.denominateByShortestLength)
    {
      return false;
    }
    if (includeGappedColumns != other.includeGappedColumns)
    {
      return false;
    }
    if (includeGaps != other.includeGaps)
    {
      return false;
    }
    if (matchGaps != other.matchGaps)
    {
      return false;
    }
    return true;
  }
}
