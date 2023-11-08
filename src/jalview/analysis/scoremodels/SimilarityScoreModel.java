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

public abstract class SimilarityScoreModel implements ScoreModelI
{

  /**
   * Computed similarity scores are converted to distance scores by subtracting
   * every value from the maximum value. That is, maximum similarity corresponds
   * to zero distance, and smaller similarities to larger distances.
   */
  @Override
  public MatrixI findDistances(AlignmentView seqData,
          SimilarityParamsI options)
  {
    MatrixI similarities = findSimilarities(seqData, options);

    MatrixI distances = similarityToDistance(similarities);

    return distances;
  }

  /**
   * Converts a matrix of similarity scores to distance scores, by reversing the
   * range of the scores, mapping the maximum to zero. The input matrix is not
   * modified.
   * 
   * @param similarities
   */
  public static MatrixI similarityToDistance(MatrixI similarities)
  {
    MatrixI distances = similarities.copy();

    distances.reverseRange(true);

    return distances;
  }

}
