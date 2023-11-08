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

import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcherSetI;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Map;

/**
 * Abstract feature renderer interface
 * 
 * @author JimP
 * 
 */
public interface FeatureRenderer
{

  /**
   * Computes the feature colour for a given sequence and column position,
   * taking into account sequence feature locations, feature colour schemes,
   * render ordering, feature and feature group visibility, and transparency.
   * <p>
   * The graphics argument should be provided if transparency is applied
   * (getTransparency() < 1). With feature transparency, visible features are
   * written to the graphics context and the composite colour may be read off
   * from it. In this case, the returned feature colour is not the composite
   * colour but that of the last feature drawn.
   * <p>
   * If no transparency applies, then the graphics argument may be null, and the
   * returned colour is the one that would be drawn for the feature.
   * <p>
   * Returns null if there is no visible feature at the position.
   * <p>
   * This is provided to support rendering of feature colours other than on the
   * sequence alignment, including by structure viewers and the overview window.
   * Note this method takes no account of whether the sequence or column is
   * hidden.
   * 
   * @param sequence
   * @param column
   *          aligned column position (1..)
   * @param g
   * @return
   */
  Color findFeatureColour(SequenceI sequence, int column, Graphics g);

  /**
   * trigger the feature discovery process for a newly created feature renderer.
   */
  void featuresAdded();

  /**
   * 
   * @param ft
   * @return display style for a feature
   */
  FeatureColourI getFeatureStyle(String ft);

  /**
   * update the feature style for a particular feature
   * 
   * @param ft
   * @param ggc
   */
  void setColour(String ft, FeatureColourI ggc);

  AlignViewportI getViewport();

  /**
   * 
   * @return container managing list of feature types and their visibility
   */
  FeaturesDisplayedI getFeaturesDisplayed();

  /**
   * get display style for all features types - visible or invisible
   * 
   * @return
   */
  Map<String, FeatureColourI> getFeatureColours();

  /**
   * query the alignment view to find all features
   * 
   * @param newMadeVisible
   *          - when true, automatically make newly discovered types visible
   */
  void findAllFeatures(boolean newMadeVisible);

  /**
   * get display style for all features types currently visible
   * 
   * @return
   */
  Map<String, FeatureColourI> getDisplayedFeatureCols();

  /**
   * get all registered groups
   * 
   * @return
   */
  List<String> getFeatureGroups();

  /**
   * get groups that are visible/invisible
   * 
   * @param visible
   * @return
   */
  List<String> getGroups(boolean visible);

  /**
   * Set visibility for a list of groups
   * 
   * @param toset
   * @param visible
   */
  void setGroupVisibility(List<String> toset, boolean visible);

  /**
   * Set visibility of the given feature group
   * 
   * @param group
   * @param visible
   */
  void setGroupVisibility(String group, boolean visible);

  /**
   * Returns visible features at the specified aligned column on the given
   * sequence. Non-positional features are not included. If the column has a
   * gap, then enclosing features are included (but not contact features).
   * 
   * @param sequence
   * @param column
   *          aligned column position (1..)
   * @return
   */
  List<SequenceFeature> findFeaturesAtColumn(SequenceI sequence,
          int column);

  /**
   * Returns features at the specified residue positions on the given sequence.
   * Non-positional features are not included. Features are returned in render
   * order of their feature type (last is on top). Within feature type, ordering
   * is undefined.
   * 
   * @param sequence
   * @param fromResNo
   * @param toResNo
   * @return
   */
  List<SequenceFeature> findFeaturesAtResidue(SequenceI sequence,
          int fromResNo, int toResNo);

  /**
   * get current displayed types, in ordering of rendering (on top last)
   * 
   * @return a (possibly empty) list of feature types
   */

  List<String> getDisplayedFeatureTypes();

  /**
   * Returns a (possibly empty) list of currently visible feature groups
   * 
   * @return
   */
  List<String> getDisplayedFeatureGroups();

  /**
   * display all features of these types
   * 
   * @param featureTypes
   */
  void setAllVisible(List<String> featureTypes);

  /**
   * display featureType
   * 
   * @param featureType
   */
  void setVisible(String featureType);

  /**
   * Sets the transparency value, between 0 (full transparency) and 1 (no
   * transparency)
   * 
   * @param value
   */
  void setTransparency(float value);

  /**
   * Returns the transparency value, between 0 (full transparency) and 1 (no
   * transparency)
   * 
   * @return
   */
  float getTransparency();

  /**
   * Answers the filters applied to the given feature type, or null if none is
   * set
   * 
   * @param featureType
   * @return
   */
  FeatureMatcherSetI getFeatureFilter(String featureType);

  /**
   * Answers the feature filters map
   * 
   * @return
   */
  public Map<String, FeatureMatcherSetI> getFeatureFilters();

  /**
   * Sets the filters for the feature type, or removes them if a null or empty
   * filter is passed
   * 
   * @param featureType
   * @param filter
   */
  void setFeatureFilter(String featureType, FeatureMatcherSetI filter);

  /**
   * Replaces all feature filters with the given map
   * 
   * @param filters
   */
  void setFeatureFilters(Map<String, FeatureMatcherSetI> filters);

  /**
   * Returns the colour for a particular feature instance. This includes
   * calculation of 'colour by label', or of a graduated score colour, if
   * applicable.
   * <p>
   * Returns null if
   * <ul>
   * <li>feature group is not visible, or</li>
   * <li>feature values lie outside any colour threshold, or</li>
   * <li>feature is excluded by filter conditions</li>
   * </ul>
   * This method does not check feature type visibility.
   * 
   * @param feature
   * @return
   */
  Color getColour(SequenceFeature feature);

  /**
   * Answers true if feature would be shown, else false. A feature is shown if
   * <ul>
   * <li>its feature type is set to visible</li>
   * <li>its feature group is either null, or set to visible</li>
   * <li>it is not excluded by a colour threshold on score or other numeric
   * attribute</li>
   * <li>it is not excluded by a filter condition</li>
   * </ul>
   * 
   * @param feature
   * @return
   */
  boolean isVisible(SequenceFeature feature);

  /**
   * Answers a bean containing a mapping, and a list of visible features in this
   * alignment at a position (or range) which is mappable from the given
   * sequence residue position in a mapped alignment. Features are returned in
   * render order of feature type (on top last), with order within feature type
   * undefined. If no features or mapping are found, answers null.
   * 
   * @param sequence
   * @param pos
   * @return
   */
  MappedFeatures findComplementFeaturesAtResidue(SequenceI sequence,
          int pos);

  /**
   * Sends a message to let any registered parties know that something about
   * feature rendering has changed
   */
  void notifyFeaturesChanged();

}
