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
package jalview.api.analysis;

/**
 * A description of options when computing percentage identity of two aligned
 * sequences
 */
public interface SimilarityParamsI
{
  /**
   * Answers true if gap-gap aligned positions should be included in the
   * calculation
   * 
   * @return
   */
  boolean includeGappedColumns();

  /**
   * Answers true if gap-residue alignment is considered a match
   * 
   * @return
   */
  // TODO is this specific to a PID score only?
  // score matrix will compute whatever is configured for gap-residue
  boolean matchGaps();

  /**
   * Answers true if gaps are included in the calculation. This may affect the
   * calculated score, the denominator (normalisation factor) of the score, or
   * both. Gap-gap positions are included if this and includeGappedColumns both
   * answer true.
   * 
   * @return
   */
  boolean includeGaps();

  /**
   * Answers true if only the shortest sequence length is used to divide the
   * total score, false if the longest sequence length
   * 
   * @return
   */
  boolean denominateByShortestLength();
}
