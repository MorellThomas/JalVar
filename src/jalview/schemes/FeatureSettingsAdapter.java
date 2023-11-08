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
package jalview.schemes;

import jalview.api.FeatureColourI;
import jalview.api.FeatureSettingsModelI;

/**
 * An adapter class that may be extended to instantiate feature colour schemes
 */
public class FeatureSettingsAdapter implements FeatureSettingsModelI
{

  @Override
  public boolean isFeatureDisplayed(String type)
  {
    return false;
  }

  @Override
  public boolean isFeatureHidden(String type)
  {
    return false;
  }

  @Override
  public boolean isGroupDisplayed(String group)
  {
    return true;
  }

  @Override
  public FeatureColourI getFeatureColour(String type)
  {
    return null;
  }

  @Override
  public float getTransparency()
  {
    return 1f;
  }

  @Override
  public int compare(String feature1, String feature2)
  {
    return 0;
  }

  @Override
  public boolean optimiseOrder()
  {
    return false;
  }

}
