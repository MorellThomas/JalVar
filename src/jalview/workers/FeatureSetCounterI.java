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

package jalview.workers;

import jalview.datamodel.SequenceFeature;

import java.util.List;

/**
 * An interface for a type that returns counts (per computed annotation type) of
 * any value of interest at a sequence position that can be determined from the
 * sequence character and any features present at that position
 * 
 */
public interface FeatureSetCounterI
{
  /**
   * Returns counts (per annotation type) of some properties of interest, for
   * example
   * <ul>
   * <li>the number of variant features at the position</li>
   * <li>the number of Cath features of status 'True Positive'</li>
   * <li>1 if the residue is hydrophobic, else 0</li>
   * <li>etc</li>
   * </ul>
   * 
   * @param residue
   *          the residue (or gap) at the position
   * @param a
   *          list of any sequence features which include the position
   */
  int[] count(String residue, List<SequenceFeature> features);

  /**
   * Returns names for the annotations that this is counting, for use as the
   * displayed labels
   * 
   * @return
   */
  String[] getNames();

  /**
   * Returns descriptions for the annotations, for display as tooltips
   * 
   * @return
   */
  String[] getDescriptions();

  /**
   * Returns the colour (as [red, green, blue] values in the range 0-255) to use
   * for the minimum value on histogram bars. If this is different to
   * getMaxColour(), then bars will have a graduated colour.
   * 
   * @return
   */
  int[] getMinColour();

  /**
   * Returns the colour (as [red, green, blue] values in the range 0-255) to use
   * for the maximum value on histogram bars. If this is the same as
   * getMinColour(), then bars will have a single colour (not graduated).
   * 
   * @return
   */
  int[] getMaxColour();
}
