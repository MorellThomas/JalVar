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

import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.AlignmentView;
import jalview.math.MatrixI;

public abstract class DistanceScoreModel implements ScoreModelI
{
  /**
   * A similarity score is calculated by first computing a distance score, and
   * then reversing the min-max range of the score values
   */
  @Override
  public MatrixI findSimilarities(AlignmentView seqData,
          SimilarityParamsI options)
  {
    MatrixI distances = findDistances(seqData, options);

    MatrixI similarities = distanceToSimilarity(distances);

    return similarities;
  }

  /**
   * Converts distance scores to similarity scores, by reversing the range of
   * score values so that max becomes min and vice versa. The input matrix is
   * not modified.
   * 
   * @param distances
   */
  public static MatrixI distanceToSimilarity(MatrixI distances)
  {
    MatrixI similarities = distances.copy();

    similarities.reverseRange(false);

    return similarities;
  }
}
