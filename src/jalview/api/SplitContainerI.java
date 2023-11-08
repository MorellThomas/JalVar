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

import jalview.controller.FeatureSettingsControllerGuiI;
import jalview.datamodel.AlignmentI;

/**
 * Describes a visual container that can show two alignments.
 * 
 * @author gmcarstairs
 *
 */
public interface SplitContainerI
{

  /**
   * Set visibility of the specified split view component.
   * 
   * @param alignFrame
   * @param show
   */
  // TODO need an interface for AlignFrame?
  void setComplementVisible(Object alignFrame, boolean show);

  /**
   * Returns the alignment that is complementary to the one in the given
   * AlignFrame, or null.
   */
  AlignmentI getComplement(Object af);

  /**
   * Returns the frame title for the alignment that is complementary to the one
   * in the given AlignFrame, or null.
   * 
   * @param af
   * @return
   */
  String getComplementTitle(Object af);

  /**
   * get the 'other' alignFrame in the SplitFrame
   * 
   * @param alignFrame
   * @return the complement alignFrame - or null if alignFrame wasn't held by
   *         this frame
   */
  AlignViewControllerGuiI getComplementAlignFrame(
          AlignViewControllerGuiI alignFrame);

  /**
   * add the given UI to the splitframe's feature settings UI holder
   * 
   * @param featureSettings
   * @return
   */
  void addFeatureSettingsUI(FeatureSettingsControllerGuiI featureSettings);

  /**
   * Request to close all feature settings originating from a particular panel.
   * 
   * @param featureSettings
   * @param closeContainingFrame
   *          - if false then the tab containing the feature settings will be
   *          'reset' ready for a new feature settings
   */
  void closeFeatureSettings(FeatureSettingsControllerI featureSettings,
          boolean closeContainingFrame);

  /**
   * 
   * @return true if a feature settings panel is currently open
   */
  boolean isFeatureSettingsOpen();

}
