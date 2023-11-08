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
package jalview.viewmodel.styles;

import jalview.api.ViewStyleI;

import java.awt.Color;

/**
 * A container for holding alignment view properties. View properties are
 * data-independent, which means they can be safely copied between views
 * involving different alignment data without causing exceptions in the
 * rendering system.
 * 
 * @author jprocter
 *
 */
public class ViewStyle implements ViewStyleI
{
  private boolean abovePIDThreshold = false;

  int charHeight;

  int charWidth;

  int idWidth = -1;

  /**
   * gui state - changes to colour scheme propagated to all groups
   */
  private boolean colourAppliesToAllGroups;

  /**
   * centre columnar annotation labels in displayed alignment annotation
   */
  boolean centreColumnLabels = false;

  private boolean showdbrefs;

  private boolean shownpfeats;

  // --------END Structure Conservation

  /**
   * colour according to the reference sequence defined on the alignment
   */
  private boolean colourByReferenceSeq = false;

  boolean conservationColourSelected = false;

  /**
   * show the reference sequence in the alignment view
   */
  private boolean displayReferenceSeq = false;

  private int increment;

  /**
   * display gap characters
   */
  boolean renderGaps = true;

  private boolean rightAlignIds = false;

  boolean scaleAboveWrapped = false;

  boolean scaleLeftWrapped = true;

  boolean scaleRightWrapped = true;

  boolean seqNameItalics;

  /**
   * show annotation tracks on the alignment
   */
  private boolean showAnnotation = true;

  /**
   * render each residue in a coloured box
   */
  boolean showBoxes = true;

  /**
   * Colour sequence text
   */
  boolean showColourText = false;

  /**
   * show blue triangles
   */
  boolean showHiddenMarkers = true;

  /**
   * show /start-end in ID panel
   */
  boolean showJVSuffix = true;

  /**
   * scale features height according to score
   */
  boolean showSeqFeaturesHeight;

  /**
   * display setting for showing/hiding sequence features on alignment view
   */
  boolean showSequenceFeatures = false;

  /**
   * display sequence symbols
   */
  boolean showText = true;

  /**
   * show non-conserved residues only
   */
  protected boolean showUnconserved = false;

  Color textColour = Color.black;

  Color textColour2 = Color.white;

  /**
   * PID or consensus threshold
   */
  int threshold;

  /**
   * threshold for switching between textColour & textColour2
   */
  int thresholdTextColour = 0;

  /**
   * upper case characters in sequence are shown in bold
   */
  boolean upperCasebold = false;

  /**
   * name of base font for view
   */
  private String fontName;

  /**
   * size for base font
   */
  private int fontSize;

  /*
   * If true, scale protein residues to 3 times width of cDNA bases (in
   * SplitFrame views only)
   */
  private boolean scaleProteinAsCdna = true;

  /*
   * if true, font changes to protein or cDNA are applied to both
   * sides of a split screen
   */
  private boolean proteinFontAsCdna = true;

  /**
   * Copy constructor
   * 
   * @param vs
   */
  public ViewStyle(ViewStyleI vs)
  {
    setAbovePIDThreshold(vs.getAbovePIDThreshold());
    setCentreColumnLabels(vs.isCentreColumnLabels());
    setCharHeight(vs.getCharHeight());
    setCharWidth(vs.getCharWidth());
    setColourAppliesToAllGroups(vs.getColourAppliesToAllGroups());
    setColourByReferenceSeq(vs.isColourByReferenceSeq());
    setColourText(vs.getColourText());
    setConservationColourSelected(vs.isConservationColourSelected());
    setConservationSelected(vs.getConservationSelected());
    setDisplayReferenceSeq(vs.isDisplayReferenceSeq());
    setFontName(vs.getFontName());
    setFontSize(vs.getFontSize());
    setFontStyle(vs.getFontStyle());
    setIdWidth(vs.getIdWidth());
    setIncrement(vs.getIncrement());
    setRenderGaps(vs.isRenderGaps());
    setRightAlignIds(vs.isRightAlignIds());
    setScaleAboveWrapped(vs.getScaleAboveWrapped());
    setScaleLeftWrapped(vs.getScaleLeftWrapped());
    setScaleProteinAsCdna(vs.isScaleProteinAsCdna());
    setProteinFontAsCdna(vs.isProteinFontAsCdna());
    setScaleRightWrapped(vs.getScaleRightWrapped());
    setSeqNameItalics(vs.isSeqNameItalics());
    setShowAnnotation(vs.isShowAnnotation());
    setShowBoxes(vs.getShowBoxes());
    setShowColourText(vs.isShowColourText());
    setShowDBRefs(vs.isShowDBRefs());
    setShowHiddenMarkers(vs.getShowHiddenMarkers());
    setShowJVSuffix(vs.getShowJVSuffix());
    setShowNPFeats(vs.isShowNPFeats());
    setShowSequenceFeaturesHeight(vs.isShowSequenceFeaturesHeight());
    setShowSequenceFeatures(vs.isShowSequenceFeatures());
    setShowComplementFeatures(vs.isShowComplementFeatures());
    setShowComplementFeaturesOnTop(vs.isShowComplementFeaturesOnTop());
    setShowText(vs.getShowText());
    setShowUnconserved(vs.getShowUnconserved());
    setTextColour(vs.getTextColour());
    setTextColour2(vs.getTextColour2());
    setThreshold(vs.getThreshold());
    setThresholdTextColour(vs.getThresholdTextColour());
    setUpperCasebold(vs.isUpperCasebold());
    setWrapAlignment(vs.getWrapAlignment());
    setWrappedWidth(vs.getWrappedWidth());
    // ViewStyle.configureFrom(this, viewStyle);
  }

  public ViewStyle()
  {
  }

  /**
   * Returns true if all attributes of the ViewStyles have the same value
   */
  @Override
  public boolean equals(Object other)
  {
    if (other == null || !(other instanceof ViewStyle))
    {
      return false;
    }
    ViewStyle vs = (ViewStyle) other;

    boolean match = (getAbovePIDThreshold() == vs.getAbovePIDThreshold()
            && isCentreColumnLabels() == vs.isCentreColumnLabels()
            && getCharHeight() == vs.getCharHeight()
            && getCharWidth() == vs.getCharWidth()
            && getColourAppliesToAllGroups() == vs
                    .getColourAppliesToAllGroups()
            && isColourByReferenceSeq() == vs.isColourByReferenceSeq()
            && getColourText() == vs.getColourText()
            && isConservationColourSelected() == vs
                    .isConservationColourSelected()
            && getConservationSelected() == vs.getConservationSelected()
            && isDisplayReferenceSeq() == vs.isDisplayReferenceSeq()
            && getFontSize() == vs.getFontSize()
            && getFontStyle() == vs.getFontStyle()
            && getIdWidth() == vs.getIdWidth()
            && getIncrement() == vs.getIncrement()
            && isRenderGaps() == vs.isRenderGaps()
            && isRightAlignIds() == vs.isRightAlignIds()
            && getScaleAboveWrapped() == vs.getScaleAboveWrapped()
            && getScaleLeftWrapped() == vs.getScaleLeftWrapped()
            && isScaleProteinAsCdna() == vs.isScaleProteinAsCdna()
            && isProteinFontAsCdna() == vs.isProteinFontAsCdna()
            && getScaleRightWrapped() == vs.getScaleRightWrapped()
            && isSeqNameItalics() == vs.isSeqNameItalics()
            && isShowAnnotation() == vs.isShowAnnotation()
            && getShowBoxes() == vs.getShowBoxes()
            && isShowColourText() == vs.isShowColourText()
            && isShowDBRefs() == vs.isShowDBRefs()
            && getShowHiddenMarkers() == vs.getShowHiddenMarkers()
            && getShowJVSuffix() == vs.getShowJVSuffix()
            && isShowNPFeats() == vs.isShowNPFeats()
            && isShowSequenceFeaturesHeight() == vs
                    .isShowSequenceFeaturesHeight()
            && isShowSequenceFeatures() == vs.isShowSequenceFeatures()
            && isShowComplementFeatures() == vs.isShowComplementFeatures()
            && isShowComplementFeaturesOnTop() == vs
                    .isShowComplementFeaturesOnTop()
            && getShowText() == vs.getShowText()
            && getShowUnconserved() == vs.getShowUnconserved()
            && getThreshold() == vs.getThreshold()
            && getThresholdTextColour() == vs.getThresholdTextColour()
            && isUpperCasebold() == vs.isUpperCasebold()
            && getWrapAlignment() == vs.getWrapAlignment()
            && getWrappedWidth() == vs.getWrappedWidth());
    /*
     * and compare non-primitive types; syntax below will match null with null
     * values
     */
    match = match && String.valueOf(getFontName())
            .equals(String.valueOf(vs.getFontName()));
    match = match && String.valueOf(getTextColour())
            .equals(String.valueOf(vs.getTextColour()));
    match = match && String.valueOf(getTextColour2())
            .equals(String.valueOf(vs.getTextColour2()));
    return match;
    // return equivalent(this, (ViewStyle) other);
  }

  /**
   * Overridden to ensure that whenever vs1.equals(vs2) then vs1.hashCode() ==
   * vs2.hashCode()
   */
  @Override
  public int hashCode()
  {
    /*
     * No need to include all properties, just a selection...
     */
    int hash = 0;
    int m = 1;
    // Boolean.hashCode returns 1231 or 1237
    hash += m++ * Boolean.valueOf(this.abovePIDThreshold).hashCode();
    hash += m++ * Boolean.valueOf(this.centreColumnLabels).hashCode();
    hash += m++ * Boolean.valueOf(this.colourAppliesToAllGroups).hashCode();
    hash += m++ * Boolean.valueOf(this.displayReferenceSeq).hashCode();
    hash += m++ * Boolean.valueOf(this.renderGaps).hashCode();
    hash += m++ * Boolean.valueOf(this.rightAlignIds).hashCode();
    hash += m++ * Boolean.valueOf(this.scaleProteinAsCdna).hashCode();
    hash += m++ * Boolean.valueOf(this.scaleRightWrapped).hashCode();
    hash += m++ * Boolean.valueOf(this.seqNameItalics).hashCode();
    hash += m++ * Boolean.valueOf(this.showAnnotation).hashCode();
    hash += m++ * Boolean.valueOf(this.showBoxes).hashCode();
    hash += m++ * Boolean.valueOf(this.showdbrefs).hashCode();
    hash += m++ * Boolean.valueOf(this.showJVSuffix).hashCode();
    hash += m++ * Boolean.valueOf(this.showSequenceFeatures).hashCode();
    hash += m++ * Boolean.valueOf(this.showUnconserved).hashCode();
    hash += m++ * Boolean.valueOf(this.wrapAlignment).hashCode();
    hash += m++ * this.charHeight;
    hash += m++ * this.charWidth;
    hash += m++ * fontSize;
    hash += m++ * fontStyle;
    hash += m++ * idWidth;
    hash += String.valueOf(this.fontName).hashCode();
    return hash;
  }

  /**
   * @return the upperCasebold
   */
  @Override
  public boolean isUpperCasebold()
  {
    return upperCasebold;
  }

  /**
   * @param upperCasebold
   *          the upperCasebold to set
   */
  @Override
  public void setUpperCasebold(boolean upperCasebold)
  {
    this.upperCasebold = upperCasebold;
  }

  /**
   * flag for wrapping
   */
  boolean wrapAlignment = false;

  /**
   * number columns in wrapped alignment
   */
  int wrappedWidth;

  private int fontStyle;

  private boolean showComplementFeatures;

  private boolean showComplementFeaturesOnTop;

  /**
   * GUI state
   * 
   * @return true if percent identity threshold is applied to shading
   */
  @Override
  public boolean getAbovePIDThreshold()
  {
    return abovePIDThreshold;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getCharHeight()
  {
    return charHeight;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getCharWidth()
  {
    return charWidth;
  }

  /**
   * 
   * 
   * @return flag indicating if colourchanges propagated to all groups
   */
  @Override
  public boolean getColourAppliesToAllGroups()
  {
    return colourAppliesToAllGroups;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getColourText()
  {
    return showColourText;
  }

  /**
   * GUI state
   * 
   * @return true if conservation based shading is enabled
   */
  @Override
  public boolean getConservationSelected()
  {
    return conservationColourSelected;
  }

  /**
   * GUI State
   * 
   * @return get scalar for bleaching colourschemes by conservation
   */
  @Override
  public int getIncrement()
  {
    return increment;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getScaleAboveWrapped()
  {
    return scaleAboveWrapped;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getScaleLeftWrapped()
  {
    return scaleLeftWrapped;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getScaleRightWrapped()
  {
    return scaleRightWrapped;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getShowBoxes()
  {
    return showBoxes;
  }

  @Override
  public boolean getShowHiddenMarkers()
  {
    return showHiddenMarkers;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getShowJVSuffix()
  {
    return showJVSuffix;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getShowText()
  {
    return showText;
  }

  @Override
  public boolean getShowUnconserved()
  {
    return showUnconserved;
  }

  /**
   * @return the textColour
   */
  @Override
  public Color getTextColour()
  {
    return textColour;
  }

  /**
   * @return the textColour2
   */
  @Override
  public Color getTextColour2()
  {
    return textColour2;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getThreshold()
  {
    return threshold;
  }

  /**
   * @return the thresholdTextColour
   */
  @Override
  public int getThresholdTextColour()
  {
    return thresholdTextColour;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public boolean getWrapAlignment()
  {
    return wrapAlignment;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getWrappedWidth()
  {
    return wrappedWidth;
  }

  @Override
  public boolean isColourByReferenceSeq()
  {
    return colourByReferenceSeq;
  }

  /**
   * @return the conservationColourSelected
   */
  @Override
  public boolean isConservationColourSelected()
  {
    return conservationColourSelected;
  }

  @Override
  public boolean isDisplayReferenceSeq()
  {
    return displayReferenceSeq;
  }

  /**
   * @return the renderGaps
   */
  @Override
  public boolean isRenderGaps()
  {
    return renderGaps;
  }

  @Override
  public boolean isRightAlignIds()
  {
    return rightAlignIds;
  }

  /**
   * @return the seqNameItalics
   */
  @Override
  public boolean isSeqNameItalics()
  {
    return seqNameItalics;
  }

  @Override
  public boolean isShowAnnotation()
  {
    return showAnnotation;
  }

  /**
   * @return the showColourText
   */
  @Override
  public boolean isShowColourText()
  {
    return showColourText;
  }

  /**
   * @return the showSeqFeaturesHeight
   */
  @Override
  public boolean isShowSequenceFeaturesHeight()
  {
    return showSeqFeaturesHeight;
  }

  @Override
  public boolean isShowSequenceFeatures()
  {
    return showSequenceFeatures;
  }

  /**
   * GUI state
   * 
   * 
   * @param b
   *          indicate if percent identity threshold is applied to shading
   */
  @Override
  public void setAbovePIDThreshold(boolean b)
  {
    abovePIDThreshold = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param h
   *          DOCUMENT ME!
   */
  @Override
  public void setCharHeight(int h)
  {
    this.charHeight = h;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param w
   *          DOCUMENT ME!
   */
  @Override
  public void setCharWidth(int w)
  {
    this.charWidth = w;
  }

  /**
   * @param value
   *          indicating if subsequent colourscheme changes will be propagated
   *          to all groups
   */
  @Override
  public void setColourAppliesToAllGroups(boolean b)
  {
    colourAppliesToAllGroups = b;
  }

  @Override
  public void setColourByReferenceSeq(boolean colourByReferenceSeq)
  {
    this.colourByReferenceSeq = colourByReferenceSeq;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  @Override
  public void setColourText(boolean state)
  {
    showColourText = state;
  }

  /**
   * @param conservationColourSelected
   *          the conservationColourSelected to set
   */
  @Override
  public void setConservationColourSelected(
          boolean conservationColourSelected)
  {
    this.conservationColourSelected = conservationColourSelected;
  }

  /**
   * GUI state
   * 
   * @param b
   *          enable conservation based shading
   */
  @Override
  public void setConservationSelected(boolean b)
  {
    conservationColourSelected = b;
  }

  @Override
  public void setDisplayReferenceSeq(boolean displayReferenceSeq)
  {
    this.displayReferenceSeq = displayReferenceSeq;
  }

  /**
   * 
   * @param inc
   *          set the scalar for bleaching colourschemes according to degree of
   *          conservation
   */
  @Override
  public void setIncrement(int inc)
  {
    increment = inc;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  @Override
  public void setRenderGaps(boolean state)
  {
    renderGaps = state;
  }

  @Override
  public void setRightAlignIds(boolean rightAlignIds)
  {
    this.rightAlignIds = rightAlignIds;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  @Override
  public void setScaleAboveWrapped(boolean b)
  {
    scaleAboveWrapped = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  @Override
  public void setScaleLeftWrapped(boolean b)
  {
    scaleLeftWrapped = b;
  }

  /**
   * 
   * 
   * @param scaleRightWrapped
   *          - true or false
   */

  @Override
  public void setScaleRightWrapped(boolean b)
  {
    scaleRightWrapped = b;
  }

  @Override
  public void setSeqNameItalics(boolean italics)
  {
    seqNameItalics = italics;
  }

  @Override
  public void setShowAnnotation(boolean b)
  {
    showAnnotation = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  @Override
  public void setShowBoxes(boolean state)
  {
    showBoxes = state;
  }

  /**
   * @param showColourText
   *          the showColourText to set
   */
  @Override
  public void setShowColourText(boolean showColourText)
  {
    this.showColourText = showColourText;
  }

  @Override
  public void setShowHiddenMarkers(boolean show)
  {
    showHiddenMarkers = show;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  @Override
  public void setShowJVSuffix(boolean b)
  {
    showJVSuffix = b;
  }

  @Override
  public void setShowSequenceFeaturesHeight(boolean selected)
  {
    showSeqFeaturesHeight = selected;

  }

  /**
   * set the flag
   * 
   * @param b
   *          features are displayed if true
   */
  @Override
  public void setShowSequenceFeatures(boolean b)
  {
    showSequenceFeatures = b;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  @Override
  public void setShowText(boolean state)
  {
    showText = state;
  }

  @Override
  public void setShowUnconserved(boolean showunconserved)
  {
    showUnconserved = showunconserved;
  }

  /**
   * @param textColour
   *          the textColour to set
   */
  @Override
  public void setTextColour(Color textColour)
  {
    this.textColour = textColour;
  }

  /**
   * @param textColour2
   *          the textColour2 to set
   */
  @Override
  public void setTextColour2(Color textColour2)
  {
    this.textColour2 = textColour2;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param thresh
   *          DOCUMENT ME!
   */
  @Override
  public void setThreshold(int thresh)
  {
    threshold = thresh;
  }

  /**
   * @param thresholdTextColour
   *          the thresholdTextColour to set
   */
  @Override
  public void setThresholdTextColour(int thresholdTextColour)
  {
    this.thresholdTextColour = thresholdTextColour;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  @Override
  public void setWrapAlignment(boolean state)
  {
    wrapAlignment = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param w
   *          DOCUMENT ME!
   */
  @Override
  public void setWrappedWidth(int w)
  {
    this.wrappedWidth = w;
  }

  @Override
  public boolean sameStyle(ViewStyleI that)
  {
    return this.equals(that);
  }

  @Override
  public String getFontName()
  {
    return fontName;
  }

  @Override
  public int getFontSize()
  {
    return fontSize;
  }

  @Override
  public int getFontStyle()
  {
    return fontStyle;
  }

  @Override
  public void setFontName(String name)
  {
    fontName = name;
  }

  @Override
  public void setFontSize(int size)
  {
    fontSize = size;

  }

  @Override
  public void setFontStyle(int style)
  {
    fontStyle = style;
  }

  @Override
  public int getIdWidth()
  {
    return idWidth;
  }

  /**
   * @param idWidth
   *          the idWidth to set
   */
  @Override
  public void setIdWidth(int idWidth)
  {
    this.idWidth = idWidth;
  }

  /**
   * @return the centreColumnLabels
   */
  @Override
  public boolean isCentreColumnLabels()
  {
    return centreColumnLabels;
  }

  /**
   * @param centreColumnLabels
   *          the centreColumnLabels to set
   */
  @Override
  public void setCentreColumnLabels(boolean centreColumnLabels)
  {
    this.centreColumnLabels = centreColumnLabels;
  }

  /**
   * @return the showdbrefs
   */
  @Override
  public boolean isShowDBRefs()
  {
    return showdbrefs;
  }

  /**
   * @param showdbrefs
   *          the showdbrefs to set
   */
  @Override
  public void setShowDBRefs(boolean showdbrefs)
  {
    this.showdbrefs = showdbrefs;
  }

  /**
   * @return the shownpfeats
   */
  @Override
  public boolean isShowNPFeats()
  {
    return shownpfeats;
  }

  /**
   * @param shownpfeats
   *          the shownpfeats to set
   */
  @Override
  public void setShowNPFeats(boolean shownpfeats)
  {
    this.shownpfeats = shownpfeats;
  }

  @Override
  public boolean isScaleProteinAsCdna()
  {
    return this.scaleProteinAsCdna;
  }

  @Override
  public void setScaleProteinAsCdna(boolean b)
  {
    this.scaleProteinAsCdna = b;
  }

  @Override
  public boolean isProteinFontAsCdna()
  {
    return proteinFontAsCdna;
  }

  @Override
  public void setProteinFontAsCdna(boolean b)
  {
    proteinFontAsCdna = b;
  }

  @Override
  public void setShowComplementFeatures(boolean b)
  {
    showComplementFeatures = b;
  }

  @Override
  public boolean isShowComplementFeatures()
  {
    return showComplementFeatures;
  }

  @Override
  public void setShowComplementFeaturesOnTop(boolean b)
  {
    showComplementFeaturesOnTop = b;
  }

  @Override
  public boolean isShowComplementFeaturesOnTop()
  {
    return showComplementFeaturesOnTop;
  }
}
