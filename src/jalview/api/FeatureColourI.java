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

import jalview.datamodel.SequenceFeature;

import java.awt.Color;

public interface FeatureColourI
{

  /**
   * Answers true when the feature colour varies across the score range
   * 
   * @return
   */
  boolean isGraduatedColour();

  /**
   * Returns the feature colour (when isGraduatedColour answers false)
   * 
   * @return
   */
  Color getColour();

  /**
   * Returns the minimum colour (when isGraduatedColour answers true)
   * 
   * @return
   */
  Color getMinColour();

  /**
   * Returns the maximum colour (when isGraduatedColour answers true)
   * 
   * @return
   */
  Color getMaxColour();

  /**
   * Returns the 'no value' colour (used when a feature lacks score, or the
   * attribute, being used for colouring)
   * 
   * @return
   */
  Color getNoColour();

  /**
   * Answers true if the feature has a single colour, i.e. if isColourByLabel()
   * and isGraduatedColour() both answer false
   * 
   * @return
   */
  boolean isSimpleColour();

  /**
   * Answers true if the feature is coloured by label (description) or by text
   * value of an attribute
   * 
   * @return
   */
  boolean isColourByLabel();

  void setColourByLabel(boolean b);

  /**
   * Answers true if the feature is coloured below a threshold value; only
   * applicable when isGraduatedColour answers true
   * 
   * @return
   */
  boolean isBelowThreshold();

  void setBelowThreshold(boolean b);

  /**
   * Answers true if the feature is coloured above a threshold value; only
   * applicable when isGraduatedColour answers true
   * 
   * @return
   */
  boolean isAboveThreshold();

  void setAboveThreshold(boolean b);

  /**
   * Returns the threshold value (if any), else zero
   * 
   * @return
   */
  float getThreshold();

  void setThreshold(float f);

  /**
   * Answers true if the colour varies between the actual minimum and maximum
   * score values of the feature, or false if between absolute minimum and
   * maximum values (or if not a graduated colour).
   * 
   * @return
   */
  boolean isAutoScaled();

  void setAutoScaled(boolean b);

  /**
   * Returns the maximum score of the graduated colour range
   * 
   * @return
   */
  float getMax();

  /**
   * Returns the minimum score of the graduated colour range
   * 
   * @return
   */
  float getMin();

  /**
   * Answers true if either isAboveThreshold or isBelowThreshold answers true
   * 
   * @return
   */
  boolean hasThreshold();

  /**
   * Returns the computed colour for the given sequence feature. Answers null if
   * the score of this feature instance is outside the range to render (if any),
   * i.e. lies below or above a configured threshold.
   * 
   * @param feature
   * @return
   */
  Color getColor(SequenceFeature feature);

  /**
   * Update the min-max range for a graduated colour scheme. Note that the
   * colour scheme may be configured to colour by feature score, or a
   * (numeric-valued) attribute - the caller should ensure that the correct
   * range is being set.
   * 
   * @param min
   * @param max
   */
  void updateBounds(float min, float max);

  /**
   * Returns the colour in Jalview features file format
   * 
   * @return
   */
  String toJalviewFormat(String featureType);

  /**
   * Answers true if colour is by attribute text or numerical value
   * 
   * @return
   */
  boolean isColourByAttribute();

  /**
   * Answers the name of the attribute (and optional sub-attribute...) used for
   * colouring if any, or null
   * 
   * @return
   */
  String[] getAttributeName();

  /**
   * Sets the name of the attribute (and optional sub-attribute...) used for
   * colouring if any, or null to remove this property
   * 
   * @return
   */
  void setAttributeName(String... name);

  /**
   * Answers true if colour has a threshold set, and the feature score (or other
   * attribute selected for colouring) is outwith the threshold.
   * <p>
   * Answers false if not a graduated colour, or no threshold is set, or value
   * is not outwith the threshold, or value is null or non-numeric.
   * 
   * @param sf
   * @return
   */
  boolean isOutwithThreshold(SequenceFeature sf);

  /**
   * Answers a human-readable text description of the colour, suitable for
   * display as a tooltip, possibly internationalised for the user's locale.
   * 
   * @return
   */
  String getDescription();
}
