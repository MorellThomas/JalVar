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

import java.awt.Color;

public interface ViewStyleI
{
  void setShowComplementFeatures(boolean b);

  boolean isShowComplementFeatures();

  void setShowComplementFeaturesOnTop(boolean b);

  boolean isShowComplementFeaturesOnTop();

  void setColourAppliesToAllGroups(boolean b);

  boolean getColourAppliesToAllGroups();

  boolean getAbovePIDThreshold();

  void setIncrement(int inc);

  int getIncrement();

  boolean getConservationSelected();

  void setConservationSelected(boolean b);

  void setShowHiddenMarkers(boolean show);

  boolean getShowHiddenMarkers();

  void setScaleRightWrapped(boolean b);

  void setScaleLeftWrapped(boolean b);

  void setScaleAboveWrapped(boolean b);

  boolean getScaleLeftWrapped();

  boolean getScaleAboveWrapped();

  boolean getScaleRightWrapped();

  void setAbovePIDThreshold(boolean b);

  void setThreshold(int thresh);

  int getThreshold();

  boolean getShowJVSuffix();

  void setShowJVSuffix(boolean b);

  void setWrapAlignment(boolean state);

  void setShowText(boolean state);

  void setRenderGaps(boolean state);

  boolean getColourText();

  void setColourText(boolean state);

  void setShowBoxes(boolean state);

  boolean getWrapAlignment();

  boolean getShowText();

  int getWrappedWidth();

  void setWrappedWidth(int w);

  int getCharHeight();

  void setCharHeight(int h);

  int getCharWidth();

  void setCharWidth(int w);

  boolean getShowBoxes();

  boolean getShowUnconserved();

  void setShowUnconserved(boolean showunconserved);

  /**
   * @return true if a reference sequence is set and should be displayed
   */
  boolean isDisplayReferenceSeq();

  /**
   * @return set the flag for displaying reference sequences when they are
   *         available
   */
  void setDisplayReferenceSeq(boolean displayReferenceSeq);

  /**
   * @return true if colourschemes should render according to reference sequence
   *         rather than consensus if available
   */
  boolean isColourByReferenceSeq();

  void setSeqNameItalics(boolean default1);

  void setShowSequenceFeatures(boolean b);

  boolean isShowSequenceFeatures();

  boolean isRightAlignIds();

  void setRightAlignIds(boolean rightAlignIds);

  /**
   * Returns true if annotation panel should be shown below alignment
   * 
   * @return
   */
  boolean isShowAnnotation();

  /**
   * Set flag for whether annotation panel should be shown below alignment
   * 
   * @param b
   */
  void setShowAnnotation(boolean b);

  void setShowSequenceFeaturesHeight(boolean selected);

  /**
   * @return true set flag for deciding if colourschemes should render according
   *         to reference sequence rather than consensus if available
   */
  void setColourByReferenceSeq(boolean colourByReferenceSeq);

  Color getTextColour();

  Color getTextColour2();

  int getThresholdTextColour();

  boolean isConservationColourSelected();

  boolean isRenderGaps();

  boolean isShowColourText();

  boolean isShowSequenceFeaturesHeight();

  void setConservationColourSelected(boolean conservationColourSelected);

  void setShowColourText(boolean showColourText);

  void setTextColour(Color textColour);

  void setThresholdTextColour(int thresholdTextColour);

  void setTextColour2(Color textColour2);

  boolean isSeqNameItalics();

  void setUpperCasebold(boolean upperCasebold);

  boolean isUpperCasebold();

  boolean sameStyle(ViewStyleI them);

  void setFontName(String name);

  void setFontStyle(int style);

  void setFontSize(int size);

  int getFontStyle();

  String getFontName();

  int getFontSize();

  /**
   * @return width of Sequence and Annotation ID margin. If less than zero, then
   *         width will be autocalculated
   */
  int getIdWidth();

  /**
   * Set width if
   * 
   * @param i
   */

  void setIdWidth(int i);

  /**
   * centre columnar annotation labels in displayed alignment annotation
   */
  boolean isCentreColumnLabels();

  /**
   * centre columnar annotation labels in displayed alignment annotation
   */
  void setCentreColumnLabels(boolean centreColumnLabels);

  /**
   * enable or disable the display of Database Cross References in the sequence
   * ID tooltip
   */
  void setShowDBRefs(boolean showdbrefs);

  /**
   * 
   * @return true if Database References are to be displayed on tooltips.
   */
  boolean isShowDBRefs();

  /**
   * 
   * @return true if Non-positional features are to be displayed on tooltips.
   */
  boolean isShowNPFeats();

  /**
   * enable or disable the display of Non-Positional sequence features in the
   * sequence ID tooltip
   * 
   * @param show
   */
  void setShowNPFeats(boolean shownpfeats);

  /**
   * Get flag to scale protein residues 3 times the width of cDNA bases (only
   * applicable in SplitFrame views)
   * 
   * @return
   */
  boolean isScaleProteinAsCdna();

  /**
   * Set flag to scale protein residues 3 times the width of cDNA bases (only
   * applicable in SplitFrame views)
   * 
   * @return
   */
  void setScaleProteinAsCdna(boolean b);

  /**
   * Answers true if split screen protein and cDNA use the same font
   * 
   * @return
   */
  boolean isProteinFontAsCdna();

  /**
   * Set the flag for whether split screen protein and cDNA use the same font
   * 
   * @return
   */
  void setProteinFontAsCdna(boolean b);
}
