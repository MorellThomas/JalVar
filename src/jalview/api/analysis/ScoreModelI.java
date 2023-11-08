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

import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentView;
import jalview.math.MatrixI;

public interface ScoreModelI
{
  /**
   * Answers a name for the score model, suitable for display in menus. Names
   * should be unique across score models in use.
   * 
   * @return
   * @see jalview.analysis.scoremodels.ScoreModels#forName(String)
   */
  String getName();

  /**
   * Answers an informative description of the model, suitable for use in
   * tooltips. Descriptions may be internationalised, and need not be unique
   * (but should be).
   * 
   * @return
   */
  String getDescription();

  /**
   * Answers true if this model is applicable for nucleotide data (so should be
   * shown in menus in that context)
   * 
   * @return
   */
  boolean isDNA();

  /**
   * Answers true if this model is applicable for peptide data (so should be
   * shown in menus in that context)
   * 
   * @return
   */
  boolean isProtein();

  // TODO getName, isDNA, isProtein can be static methods in Java 8

  /**
   * Returns a distance score for the given sequence regions, that is, a matrix
   * whose value [i][j] is the distance of sequence i from sequence j by some
   * measure. The options parameter provides configuration choices for how the
   * similarity score is calculated.
   * 
   * @param seqData
   * @param options
   * @return
   */

  MatrixI findDistances(AlignmentView seqData, SimilarityParamsI options);

  /**
   * Returns a similarity score for the given sequence regions, that is, a
   * matrix whose value [i][j] is the similarity of sequence i to sequence j by
   * some measure. The options parameter provides configuration choices for how
   * the similarity score is calculated.
   * 
   * @param seqData
   * @param options
   * @return
   */
  MatrixI findSimilarities(AlignmentView seqData,
          SimilarityParamsI options);

  /**
   * Returns a score model object configured for the given alignment view.
   * Depending on the score model, this may just be a singleton instance, or a
   * new instance configured with data from the view.
   * 
   * @param avp
   * @return
   */
  ScoreModelI getInstance(AlignmentViewPanel avp);
}
