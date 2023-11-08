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

import java.util.Collection;
import java.util.Set;

public interface FeaturesDisplayedI
{

  /**
   * answers an unmodifiable view of the set of visible feature types
   */
  Set<String> getVisibleFeatures();

  boolean isVisible(String featureType);

  boolean areVisible(Collection<String> featureTypes);

  void clear();

  void setVisible(String featureType);

  void setHidden(String featureType);

  /**
   * Sets all the specified feature types to visible. Visibility of other
   * feature types is not changed.
   * 
   * @param featureTypes
   */
  void setAllVisible(Collection<String> featureTypes);

  boolean isRegistered(String type);

  void setAllRegisteredVisible();

  int getVisibleFeatureCount();

  int getRegisteredFeaturesCount();

}
