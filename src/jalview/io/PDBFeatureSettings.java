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
package jalview.io;

import jalview.api.FeatureColourI;
import jalview.schemes.FeatureColour;
import jalview.schemes.FeatureSettingsAdapter;

import java.awt.Color;

import mc_view.PDBChain;

public class PDBFeatureSettings extends FeatureSettingsAdapter
{
  // TODO find one central place to define feature names
  private static final String FEATURE_INSERTION = "INSERTION";

  private static final String FEATURE_RES_NUM = PDBChain.RESNUM_FEATURE;

  @Override
  public boolean isFeatureHidden(String type)
  {
    return type.equalsIgnoreCase(FEATURE_RES_NUM);
  }

  @Override
  public FeatureColourI getFeatureColour(String type)
  {
    if (type.equalsIgnoreCase(FEATURE_INSERTION))
    {
      return new FeatureColour()
      {

        @Override
        public Color getColour()
        {
          return Color.RED;
        }
      };
    }
    return null;
  }

  /**
   * Order to render insertion after ResNum
   */
  @Override
  public int compare(String feature1, String feature2)
  {
    if (feature1.equalsIgnoreCase(FEATURE_INSERTION))
    {
      return +1;
    }
    if (feature2.equalsIgnoreCase(FEATURE_INSERTION))
    {
      return -1;
    }
    if (feature1.equalsIgnoreCase(FEATURE_RES_NUM))
    {
      return +1;
    }
    if (feature2.equalsIgnoreCase(FEATURE_RES_NUM))
    {
      return -1;
    }
    return 0;
  }
}
