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
package jalview.api;

import java.util.List;

import jalview.io.DataSourceType;

/**
 * prototype abstract controller for a Jalview alignment view
 * 
 * @author jimp
 * 
 *         All operations should return true if the view has changed as a result
 *         of the operation
 * 
 *         The controller holds methods that operate on an alignment view,
 *         modifying its state in some way that may result in side effects
 *         reflected in an associated GUI
 * 
 */
public interface AlignViewControllerI
{

  public boolean makeGroupsFromSelection();

  public boolean createGroup();

  public boolean unGroup();

  public boolean deleteGroups();

  public void setViewportAndAlignmentPanel(AlignViewportI viewport,
          AlignmentViewPanel alignPanel);

  /**
   * Mark columns in the current column selection according to positions of
   * sequence features
   * 
   * @param invert
   *          - when set, mark all but columns containing given type
   * @param extendCurrent
   *          - when set, do not clear existing column selection
   * @param toggle
   *          - rather than explicitly set, toggle selection state
   * @param featureType
   *          - feature type string
   * @return true if operation affected state
   */
  boolean markColumnsContainingFeatures(boolean invert,
          boolean extendCurrent, boolean toggle, String featureType);

  /**
   * sort the alignment or current selection by average score over the given set
   * of features
   * 
   * @param typ
   *          list of feature names or null to use currently displayed features
   */
  void sortAlignmentByFeatureScore(List<String> typ);

  /**
   * sort the alignment or current selection by distribution of the given set of
   * features
   * 
   * @param typ
   *          list of feature names or null to use currently displayed features
   */
  void sortAlignmentByFeatureDensity(List<String> typ);

  /**
   * add a features file of some kind to the current view
   * 
   * @param file
   * @param sourceType
   * @param relaxedIdMatching
   *          if true, try harder to match up IDs with local sequence data
   * @return true if parsing resulted in something being imported to the view or
   *         dataset
   */
  public boolean parseFeaturesFile(Object file, DataSourceType sourceType,
          boolean relaxedIdMatching);

  /**
   * mark columns containing highlighted regions (e.g. from search, structure
   * highlight, or a mouse over event in another viewer)
   * 
   * @param invert
   * @param extendCurrent
   * @param toggle
   * @return
   */
  boolean markHighlightedColumns(boolean invert, boolean extendCurrent,
          boolean toggle);

  /**
   * copies each distinct highlighted region on the current view as a new
   * sequence on the clipboard
   * 
   * @return
   */
  boolean copyHighlightedRegionsToClipboard();

}
