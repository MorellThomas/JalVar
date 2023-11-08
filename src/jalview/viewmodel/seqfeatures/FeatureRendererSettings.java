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
package jalview.viewmodel.seqfeatures;

import jalview.api.FeatureColourI;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.schemes.FeatureColour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureRendererSettings implements Cloneable
{
  String[] renderOrder;

  /*
   * map of {groupName, isDisplayed}
   */
  Map<String, Boolean> featureGroups;

  /*
   * map of {featureType, colourScheme}
   */
  Map<String, FeatureColourI> featureColours;

  /*
   * map of {featureType, filters}
   */
  Map<String, FeatureMatcherSetI> featureFilters;

  float transparency;

  Map<String, Float> featureOrder;

  public FeatureRendererSettings(String[] renderOrder,
          Map<String, Boolean> featureGroups,
          Map<String, FeatureColourI> featureColours, float transparency,
          Map<String, Float> featureOrder)
  {
    super();
    this.renderOrder = Arrays.copyOf(renderOrder, renderOrder.length);
    this.featureGroups = new ConcurrentHashMap<String, Boolean>(
            featureGroups);
    this.featureColours = new ConcurrentHashMap<String, FeatureColourI>(
            featureColours);
    this.transparency = transparency;
    this.featureOrder = new ConcurrentHashMap<String, Float>(featureOrder);
  }

  /**
   * create an independent instance of the feature renderer settings
   * 
   * @param fr
   */
  public FeatureRendererSettings(
          jalview.viewmodel.seqfeatures.FeatureRendererModel fr)
  {
    renderOrder = null;
    featureGroups = new ConcurrentHashMap<String, Boolean>();
    featureColours = new ConcurrentHashMap<String, FeatureColourI>();
    featureFilters = new HashMap<>();
    featureOrder = new ConcurrentHashMap<String, Float>();

    if (fr.renderOrder != null)
    {
      this.renderOrder = new String[fr.renderOrder.length];
      System.arraycopy(fr.renderOrder, 0, renderOrder, 0,
              fr.renderOrder.length);
    }
    if (fr.featureGroups != null)
    {
      this.featureGroups = new ConcurrentHashMap<String, Boolean>(
              fr.featureGroups);
    }
    if (fr.featureColours != null)
    {
      this.featureColours = new ConcurrentHashMap<String, FeatureColourI>(
              fr.featureColours);
    }
    Iterator<String> en = fr.featureColours.keySet().iterator();
    while (en.hasNext())
    {
      String next = en.next();
      FeatureColourI val = featureColours.get(next);
      // if (val instanceof GraduatedColor)
      if (val.isGraduatedColour() || val.isColourByLabel()) // why this test?
      {
        featureColours.put(next, new FeatureColour((FeatureColour) val));
      }
    }

    if (fr.featureFilters != null)
    {
      this.featureFilters.putAll(fr.featureFilters);
    }

    this.transparency = fr.transparency;
    if (fr.featureOrder != null)
    {
      this.featureOrder = new ConcurrentHashMap<String, Float>(
              fr.featureOrder);
    }
  }
}
