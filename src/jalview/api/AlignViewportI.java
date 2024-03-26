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

import jalview.analysis.Conservation;
import jalview.analysis.TreeModel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentExportData;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.ColourSchemeI;
import jalview.viewmodel.ViewportRanges;

import java.awt.Color;
import java.awt.Font;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author jimp
 * 
 */
public interface AlignViewportI extends ViewStyleI
{

  /**
   * Get the ranges object containing details of the start and end sequences and
   * residues
   * 
   * @return
   */
  public ViewportRanges getRanges();

  /**
   * calculate the height for visible annotation, revalidating bounds where
   * necessary ABSTRACT GUI METHOD
   * 
   * @return total height of annotation
   */
  public int calcPanelHeight();

  /**
   * Answers true if the viewport has at least one column selected
   * 
   * @return
   */
  boolean hasSelectedColumns();

  /**
   * Answers true if the viewport has at least one hidden column
   * 
   * @return
   */
  boolean hasHiddenColumns();

  boolean isValidCharWidth();

  boolean isShowConsensusHistogram();

  boolean isShowSequenceLogo();

  boolean isNormaliseSequenceLogo();

  ColourSchemeI getGlobalColourScheme();

  /**
   * Returns an object that describes colouring (including any thresholding or
   * fading) of the alignment
   * 
   * @return
   */
  ResidueShaderI getResidueShading();

  AlignmentI getAlignment();

  ColumnSelection getColumnSelection();

  ProfilesI getSequenceConsensusHash();

  ProfilesI getSequenceVarianceHash();

  /**
   * Get consensus data table for the cDNA complement of this alignment (if any)
   * 
   * @return
   */
  Hashtable<String, Object>[] getComplementConsensusHash();

  Hashtable<String, Object>[] getRnaStructureConsensusHash();

  boolean isIgnoreGapsConsensus();

  boolean isIgnoreGapsVariance();

  boolean isCalculationInProgress(AlignmentAnnotation alignmentAnnotation);

  AlignmentAnnotation getAlignmentQualityAnnot();

  AlignmentAnnotation getAlignmentConservationAnnotation();

  /**
   * get the container for alignment consensus annotation
   * 
   * @return
   */
  AlignmentAnnotation getAlignmentConsensusAnnotation();

  AlignmentAnnotation getAlignmentVarianceAnnotation();

  /**
   * get the container for alignment gap annotation
   * 
   * @return
   */
  AlignmentAnnotation getAlignmentGapAnnotation();

  /**
   * get the container for cDNA complement consensus annotation
   * 
   * @return
   */
  AlignmentAnnotation getComplementConsensusAnnotation();

  /**
   * Test to see if viewport is still open and active
   * 
   * @return true indicates that all references to viewport should be dropped
   */
  boolean isClosed();

  /**
   * Dispose of all references or resources held by the viewport
   */
  void dispose();

  /**
   * get the associated calculation thread manager for the view
   * 
   * @return
   */
  AlignCalcManagerI getCalcManager();

  /**
   * get the percentage gaps allowed in a conservation calculation
   * 
   */
  public int getConsPercGaps();

  /**
   * set the consensus result object for the viewport
   * 
   * @param hconsensus
   */
  void setSequenceConsensusHash(ProfilesI hconsensus);
  
  /**
   * set the variance result object for the viewport
   * 
   * @param hvariance
   */
  void setSequenceVarianceHash(ProfilesI hvariance);

  /**
   * Set the cDNA complement consensus for the viewport
   * 
   * @param hconsensus
   */
  void setComplementConsensusHash(Hashtable<String, Object>[] hconsensus);

  /**
   * 
   * @return the alignment annotation row for the structure consensus
   *         calculation
   */
  AlignmentAnnotation getAlignmentStrucConsensusAnnotation();

  /**
   * set the Rna structure consensus result object for the viewport
   * 
   * @param hStrucConsensus
   */
  void setRnaStructureConsensusHash(
          Hashtable<String, Object>[] hStrucConsensus);

  /**
   * Sets the colour scheme for the background alignment (as distinct from
   * sub-groups, which may have their own colour schemes). A null value is used
   * for no residue colour (white).
   * 
   * @param cs
   */
  void setGlobalColourScheme(ColourSchemeI cs);

  Map<SequenceI, SequenceCollectionI> getHiddenRepSequences();

  void setHiddenRepSequences(
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences);

  /**
   * hides or shows dynamic annotation rows based on groups and group and
   * alignment associated auto-annotation state flags apply the current
   * group/autoannotation settings to the alignment view. Usually you should
   * call the AlignmentViewPanel.adjustAnnotationHeight() method afterwards to
   * ensure the annotation panel bounds are set correctly.
   * 
   * @param applyGlobalSettings
   *          - apply to all autoannotation rows or just the ones associated
   *          with the current visible region
   * @param preserveNewGroupSettings
   *          - don't apply global settings to groups which don't already have
   *          group associated annotation
   */
  void updateGroupAnnotationSettings(boolean applyGlobalSettings,
          boolean preserveNewGroupSettings);

  void setSequenceColour(SequenceI seq, Color col);

  Color getSequenceColour(SequenceI seq);

  void updateSequenceIdColours();

  SequenceGroup getSelectionGroup();

  /**
   * get the currently selected sequence objects or all the sequences in the
   * alignment. TODO: change to List<>
   * 
   * @return array of references to sequence objects
   */
  SequenceI[] getSequenceSelection();

  void clearSequenceColours();

  /**
   * return a compact representation of the current alignment selection to pass
   * to an analysis function
   * 
   * @param selectedOnly
   *          boolean true to just return the selected view
   * @return AlignmentView
   */
  AlignmentView getAlignmentView(boolean selectedOnly);

  /**
   * return a compact representation of the current alignment selection to pass
   * to an analysis function
   * 
   * @param selectedOnly
   *          boolean true to just return the selected view
   * @param markGroups
   *          boolean true to annotate the alignment view with groups on the
   *          alignment (and intersecting with selected region if selectedOnly
   *          is true)
   * @return AlignmentView
   */
  AlignmentView getAlignmentView(boolean selectedOnly, boolean markGroups);

  /**
   * This method returns the visible alignment as text, as seen on the GUI, ie
   * if columns are hidden they will not be returned in the result. Use this for
   * calculating trees, PCA, redundancy etc on views which contain hidden
   * columns. This method doesn't exclude hidden sequences from the output.
   *
   * @param selectedRegionOnly
   *          - determines if only the selected region or entire alignment is
   *          exported
   * @return String[]
   */
  String[] getViewAsString(boolean selectedRegionOnly);

  /**
   * This method returns the visible alignment as text, as seen on the GUI, ie
   * if columns are hidden they will not be returned in the result. Use this for
   * calculating trees, PCA, redundancy etc on views which contain hidden
   * columns.
   * 
   * @param selectedRegionOnly
   *          - determines if only the selected region or entire alignment is
   *          exported
   * @param isExportHiddenSeqs
   *          - determines if hidden sequences would be exported or not.
   * 
   * @return String[]
   */
  String[] getViewAsString(boolean selectedRegionOnly,
          boolean isExportHiddenSeqs);

  void setSelectionGroup(SequenceGroup sg);

  char getGapCharacter();

  void setColumnSelection(ColumnSelection cs);

  void setConservation(Conservation cons);

  /**
   * get a copy of the currently visible alignment annotation
   * 
   * @param selectedOnly
   *          if true - trim to selected regions on the alignment
   * @return an empty list or new alignment annotation objects shown only
   *         visible columns trimmed to selected region only
   */
  List<AlignmentAnnotation> getVisibleAlignmentAnnotation(
          boolean selectedOnly);

  FeaturesDisplayedI getFeaturesDisplayed();

  String getSequenceSetId();

  boolean areFeaturesDisplayed();

  void setFeaturesDisplayed(FeaturesDisplayedI featuresDisplayedI);

  void alignmentChanged(AlignmentViewPanel ap);

  /**
   * @return the padGaps
   */
  boolean isPadGaps();

  /**
   * @param padGaps
   *          the padGaps to set
   */
  void setPadGaps(boolean padGaps);

  /**
   * return visible region boundaries within given column range
   * 
   * @param min
   *          first column (inclusive, from 0)
   * @param max
   *          last column (exclusive)
   * @return int[][] range of {start,end} visible positions
   */
  List<int[]> getVisibleRegionBoundaries(int min, int max);

  /**
   * This method returns an array of new SequenceI objects derived from the
   * whole alignment or just the current selection with start and end points
   * adjusted
   * 
   * @note if you need references to the actual SequenceI objects in the
   *       alignment or currently selected then use getSequenceSelection()
   * @return selection as new sequenceI objects
   */
  SequenceI[] getSelectionAsNewSequence();

  void invertColumnSelection();

  /**
   * broadcast selection to any interested parties
   */
  void sendSelection();

  /**
   * calculate the row position for alignmentIndex if all hidden sequences were
   * shown
   * 
   * @param alignmentIndex
   * @return adjusted row position
   */
  int adjustForHiddenSeqs(int alignmentIndex);

  boolean hasHiddenRows();

  /**
   * 
   * @return a copy of this view's current display settings
   */
  public ViewStyleI getViewStyle();

  /**
   * update the view's display settings with the given style set
   * 
   * @param settingsForView
   */
  public void setViewStyle(ViewStyleI settingsForView);

  /**
   * Returns a viewport which holds the cDna for this (protein), or vice versa,
   * or null if none is set.
   * 
   * @return
   */
  AlignViewportI getCodingComplement();

  /**
   * Sets the viewport which holds the cDna for this (protein), or vice versa.
   * Implementation should guarantee that the reciprocal relationship is always
   * set, i.e. each viewport is the complement of the other.
   */
  void setCodingComplement(AlignViewportI sl);

  /**
   * Answers true if viewport hosts DNA/RNA, else false.
   * 
   * @return
   */
  boolean isNucleotide();

  /**
   * Returns an id guaranteed to be unique for this viewport.
   * 
   * @return
   */
  String getViewId();

  /**
   * Return true if view should scroll to show the highlighted region of a
   * sequence
   * 
   * @return
   */
  boolean isFollowHighlight();

  /**
   * Set whether view should scroll to show the highlighted region of a sequence
   */
  void setFollowHighlight(boolean b);

  /**
   * configure the feature renderer with predefined feature settings
   * 
   * @param featureSettings
   */
  public void applyFeaturesStyle(FeatureSettingsModelI featureSettings);

  /**
   * Apply the given feature settings on top of existing feature settings.
   */
  public void mergeFeaturesStyle(FeatureSettingsModelI featureSettings);

  /**
   * check if current selection group is defined on the view, or is simply a
   * temporary group.
   * 
   * @return true if group is defined on the alignment
   */
  boolean isSelectionDefinedGroup();

  /**
   * 
   * @return true if there are search results on the view
   */
  boolean hasSearchResults();

  /**
   * set the search results for the view
   * 
   * @param results
   *          - or null to clear current results
   */
  void setSearchResults(SearchResultsI results);

  /**
   * get search results for this view (if any)
   * 
   * @return search results or null
   */
  SearchResultsI getSearchResults();

  /**
   * Retrieve a ContactListI corresponding to column in an annotation row in an
   * alignment.
   * 
   * @param _aa
   *          - annotation with associated matrix data
   * @param column
   *          - column in alignment where _aa is associated
   */
  ContactListI getContactList(AlignmentAnnotation _aa, int column);

  /**
   * Updates view settings with the given font. You may need to call
   * AlignmentPanel.fontChanged to update the layout geometry.
   * 
   * @param setGrid
   *          when true, charWidth/height is set according to font metrics
   */
  void setFont(Font newFont, boolean b);

  /**
   * Answers true if split screen protein and cDNA use the same font
   * 
   * @return
   */
  @Override
  boolean isProteinFontAsCdna();

  /**
   * Set the flag for whether split screen protein and cDNA use the same font
   * 
   * @return
   */
  @Override
  void setProteinFontAsCdna(boolean b);

  TreeModel getCurrentTree();

  void setCurrentTree(TreeModel tree);

  /**
   * Answers a data bean containing data for export as configured by the
   * supplied options
   * 
   * @param options
   * @return
   */
  AlignmentExportData getAlignExportData(AlignExportSettingsI options);

  /**
   * @param update
   *          - set the flag for updating structures on next repaint
   */
  void setUpdateStructures(boolean update);

  /**
   *
   * @return true if structure views will be updated on next refresh
   */
  boolean isUpdateStructures();

  /**
   * check if structure views need to be updated, and clear the flag afterwards.
   * 
   * @return if an update is needed
   */
  boolean needToUpdateStructureViews();

  /**
   * Adds sequencegroup to the alignment in the view. Also adds a group to the
   * complement view if one is defined.
   * 
   * @param sequenceGroup
   *          - a group defined on sequences in the alignment held by the view
   */
  void addSequenceGroup(SequenceGroup sequenceGroup);

  /**
   * Returns an interator over the [start, end] column positions of the visible
   * regions of the alignment
   * 
   * @param selectedRegionOnly
   *          if true, and the view has a selection region, then only the
   *          intersection of visible columns with the selection region is
   *          returned
   * @return
   */
  Iterator<int[]> getViewAsVisibleContigs(boolean selectedRegionOnly);

  ContactMatrixI getContactMatrix(AlignmentAnnotation alignmentAnnotation);
}
