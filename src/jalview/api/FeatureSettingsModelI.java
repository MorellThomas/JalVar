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

import java.util.Comparator;

/**
 * An interface that describes the settings configurable in the Feature Settings
 * dialog.
 * 
 * @author gmcarstairs
 */
public interface FeatureSettingsModelI extends Comparator<String>
{

  // note Java 8 will allow default implementations of these methods in the
  // interface, simplifying instantiating classes

  /**
   * Answers true if the specified feature type is to be displayed, false if no
   * preference
   * 
   * @param type
   * @return
   */
  boolean isFeatureDisplayed(String type);

  /**
   * Answers true if the specified feature type is to be hidden, false if no
   * preference
   * 
   * @param type
   * @return
   */
  boolean isFeatureHidden(String type);

  /**
   * Answers true if the specified feature group is displayed
   * 
   * @param group
   * @return
   */
  boolean isGroupDisplayed(String group);

  /**
   * Returns the colour (or graduated colour) for the feature type, or null if
   * not known
   * 
   * @param type
   * @return
   */
  FeatureColourI getFeatureColour(String type);

  /**
   * Returns the transparency value, from 0 (fully transparent) to 1 (fully
   * opaque)
   * 
   * @return
   */
  float getTransparency();

  /**
   * Returns -1 if feature1 is displayed before (below) feature 2, +1 if
   * feature2 is displayed after (on top of) feature1, or 0 if we don't care.
   * 
   * <br>
   * Note that this is the opposite ordering to how features are displayed in
   * the feature settings dialogue. FeatureRendererModel.setFeaturePriority
   * takes care of converting between the two.
   * 
   * @param feature1
   * @param feature2
   * @return
   */
  @Override
  int compare(String feature1, String feature2);

  /**
   * Answers true if features should be initially sorted so that features with a
   * shorter average length are displayed on top of those with a longer average
   * length
   * 
   * @return
   */
  boolean optimiseOrder();

}
