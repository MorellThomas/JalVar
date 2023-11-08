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

import jalview.commands.CommandI;
import jalview.schemes.ColourSchemeI;

import java.awt.Rectangle;

/**
 * Interface implemented by gui implementations managing a Jalview Alignment
 * View
 * 
 * @author jimp
 * 
 */
public interface AlignViewControllerGuiI
{

  /**
   * display the given string in the GUI's status bar
   * 
   * @param string
   */
  void setStatus(String string);

  void addHistoryItem(CommandI command);

  void setShowSeqFeatures(boolean show);

  void setMenusForViewport();

  void changeColour(ColourSchemeI cs);

  /**
   * trigger an update of the UI in response to a model data change, and if
   * necessary enable the display of sequence feature annotation on the view.
   * 
   * @param enableIfNecessary
   */
  void refreshFeatureUI(boolean enableIfNecessary);

  /**
   * get the Feature Settings control panel for the alignment view if one exists
   * 
   * @return
   */
  FeatureSettingsControllerI getFeatureSettingsUI();

  /**
   * displays the Feature Settigns control panel for the alignment view - if one
   * exists it is closed and re-opened.
   * 
   * @return the current feature settings controller
   */
  FeatureSettingsControllerI showFeatureSettingsUI();

  /**
   * record the last position of a feature settings dialog before it was closed
   * 
   * @param bounds
   */
  void setFeatureSettingsGeometry(Rectangle bounds);

  /**
   * 
   * @return last position of feature settings for this alignment view GUI
   */
  Rectangle getFeatureSettingsGeometry();
}
