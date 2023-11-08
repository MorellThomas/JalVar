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
package jalview.controller;

import java.awt.Color;
import java.util.BitSet;
import java.util.List;

import jalview.analysis.AlignmentSorter;
import jalview.api.AlignViewControllerGuiI;
import jalview.api.AlignViewControllerI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.commands.OrderCommand;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.Desktop;
import jalview.io.DataSourceType;
import jalview.io.FeaturesFile;
import jalview.schemes.ColourSchemeI;
import jalview.util.MessageManager;

public class AlignViewController implements AlignViewControllerI
{
  AlignViewportI viewport = null;

  AlignmentViewPanel alignPanel = null;

  /**
   * the GUI container that is handling interactions with the user
   */
  private AlignViewControllerGuiI avcg;

  public AlignViewController(AlignViewControllerGuiI alignFrame,
          AlignViewportI vp, AlignmentViewPanel ap)
  {
    this.avcg = alignFrame;
    this.viewport = vp;
    this.alignPanel = ap;
  }

  @Override
  public void setViewportAndAlignmentPanel(AlignViewportI vp,
          AlignmentViewPanel ap)
  {
    this.alignPanel = ap;
    this.viewport = vp;
  }

  @Override
  public boolean makeGroupsFromSelection()
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    ColumnSelection cs = viewport.getColumnSelection();
    SequenceGroup[] gps = null;
    if (sg != null && (cs == null || cs.isEmpty()))
    {
      gps = jalview.analysis.Grouping.makeGroupsFrom(
              viewport.getSequenceSelection(),
              viewport.getAlignmentView(true)
                      .getSequenceStrings(viewport.getGapCharacter()),
              viewport.getAlignment().getGroups());
    }
    else
    {
      if (cs != null)
      {
        gps = jalview.analysis.Grouping.makeGroupsFromCols(
                (sg == null) ? viewport.getAlignment().getSequencesArray()
                        : sg.getSequences().toArray(new SequenceI[0]),
                cs, viewport.getAlignment().getGroups());
      }
    }
    if (gps != null)
    {
      viewport.getAlignment().deleteAllGroups();
      viewport.clearSequenceColours();
      viewport.setSelectionGroup(null);
      ColourSchemeI colours = viewport.getGlobalColourScheme();
      // set view properties for each group
      for (int g = 0; g < gps.length; g++)
      {
        // gps[g].setShowunconserved(viewport.getShowUnconserved());
        gps[g].setshowSequenceLogo(viewport.isShowSequenceLogo());
        viewport.getAlignment().addGroup(gps[g]);
        if (colours != null)
        {
          gps[g].setColourScheme(colours.getInstance(viewport, gps[g]));
        }
        Color col = new Color((int) (Math.random() * 255),
                (int) (Math.random() * 255), (int) (Math.random() * 255));
        gps[g].idColour = col;
        viewport.setUpdateStructures(true);
        viewport.addSequenceGroup(gps[g]);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean createGroup()
  {

    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg != null)
    {
      viewport.getAlignment().addGroup(sg);
      return true;
    }
    return false;
  }

  @Override
  public boolean unGroup()
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg != null)
    {
      viewport.getAlignment().deleteGroup(sg);
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteGroups()
  {
    if (viewport.getAlignment().getGroups() != null
            && viewport.getAlignment().getGroups().size() > 0)
    {
      viewport.getAlignment().deleteAllGroups();
      viewport.clearSequenceColours();
      viewport.setSelectionGroup(null);
      return true;
    }
    return false;
  }

  @Override
  public boolean markColumnsContainingFeatures(boolean invert,
          boolean extendCurrent, boolean toggle, String featureType)
  {
    // JBPNote this routine could also mark rows, not just columns.
    // need a decent query structure to allow all types of feature searches
    BitSet bs = new BitSet();
    boolean searchSelection = viewport.getSelectionGroup() != null
            && !extendCurrent;
    SequenceCollectionI sqcol = searchSelection
            ? viewport.getSelectionGroup()
            : viewport.getAlignment();

    int nseq = findColumnsWithFeature(featureType, sqcol, bs);

    ColumnSelection cs = viewport.getColumnSelection();
    if (cs == null)
    {
      cs = new ColumnSelection();
    }

    if (bs.cardinality() > 0 || invert)
    {
      boolean changed = cs.markColumns(bs, sqcol.getStartRes(),
              sqcol.getEndRes(), invert, extendCurrent, toggle);
      if (changed)
      {
        viewport.setColumnSelection(cs);
        alignPanel.paintAlignment(false, false);
        int columnCount = invert
                ? (sqcol.getEndRes() - sqcol.getStartRes() + 1)
                        - bs.cardinality()
                : bs.cardinality();
        avcg.setStatus(MessageManager.formatMessage(
                "label.view_controller_toggled_marked", new String[]
                { toggle ? MessageManager.getString("label.toggled")
                        : MessageManager.getString("label.marked"),
                    String.valueOf(columnCount),
                    invert ? MessageManager
                            .getString("label.not_containing")
                            : MessageManager.getString("label.containing"),
                    featureType, Integer.valueOf(nseq).toString() }));
        return true;
      }
    }
    else
    {
      String key = searchSelection ? "label.no_feature_found_selection"
              : "label.no_feature_of_type_found";
      avcg.setStatus(
              MessageManager.formatMessage(key, new String[]
              { featureType }));
      if (!extendCurrent)
      {
        cs.clear();
        alignPanel.paintAlignment(false, false);
      }
    }
    return false;
  }

  /**
   * Sets a bit in the BitSet for each column (base 0) in the sequence
   * collection which includes a visible feature of the specified feature type.
   * Returns the number of sequences which have the feature visible in the
   * selected range.
   * 
   * @param featureType
   * @param sqcol
   * @param bs
   * @return
   */
  int findColumnsWithFeature(String featureType, SequenceCollectionI sqcol,
          BitSet bs)
  {
    FeatureRenderer fr = alignPanel == null ? null
            : alignPanel.getFeatureRenderer();

    final int startColumn = sqcol.getStartRes() + 1; // converted to base 1
    final int endColumn = sqcol.getEndRes() + 1;
    List<SequenceI> seqs = sqcol.getSequences();
    int nseq = 0;
    for (SequenceI sq : seqs)
    {
      if (sq != null)
      {
        // int ist = sq.findPosition(sqcol.getStartRes());
        List<SequenceFeature> sfs = sq.findFeatures(startColumn, endColumn,
                featureType);

        boolean found = false;
        for (SequenceFeature sf : sfs)
        {
          if (fr.getColour(sf) == null)
          {
            continue;
          }
          if (!found)
          {
            nseq++;
          }
          found = true;

          int sfStartCol = sq.findIndex(sf.getBegin());
          int sfEndCol = sq.findIndex(sf.getEnd());

          if (sf.isContactFeature())
          {
            /*
             * 'contact' feature - check for 'start' or 'end'
             * position within the selected region
             */
            if (sfStartCol >= startColumn && sfStartCol <= endColumn)
            {
              bs.set(sfStartCol - 1);
            }
            if (sfEndCol >= startColumn && sfEndCol <= endColumn)
            {
              bs.set(sfEndCol - 1);
            }
            continue;
          }

          /*
           * contiguous feature - select feature positions (if any) 
           * within the selected region
           */
          if (sfStartCol < startColumn)
          {
            sfStartCol = startColumn;
          }
          // not sure what the point of this is
          // if (sfStartCol < ist)
          // {
          // sfStartCol = ist;
          // }
          if (sfEndCol > endColumn)
          {
            sfEndCol = endColumn;
          }
          for (; sfStartCol <= sfEndCol; sfStartCol++)
          {
            bs.set(sfStartCol - 1); // convert to base 0
          }
        }
      }
    }
    return nseq;
  }

  @Override
  public void sortAlignmentByFeatureDensity(List<String> typ)
  {
    String methodText = MessageManager.getString("label.sort_by_density");
    sortByFeatures(typ, methodText, AlignmentSorter.FEATURE_DENSITY);
  }

  /**
   * Sorts the alignment (or current selection) by either average score or
   * density of the specified feature types, and adds to the command history. If
   * {@code types} is null, all visible feature types are used for the sort. If
   * no feature types apply, does nothing.
   * 
   * @param types
   * @param methodText
   *          - text shown in Undo/Redo command
   * @param method
   *          - passed to jalview.analysis.AlignmentSorter.sortByFeatures()
   */
  protected void sortByFeatures(List<String> types, String methodText,
          final String method)
  {
    FeatureRenderer fr = alignPanel.getFeatureRenderer();
    if (types == null && fr != null)
    {
      types = fr.getDisplayedFeatureTypes();
    }
    if (types.isEmpty())
    {
      return; // nothing to do
    }
    List<String> gps = null;
    if (fr != null)
    {
      gps = fr.getDisplayedFeatureGroups();
    }
    AlignmentI al = viewport.getAlignment();

    int start, stop;
    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg != null)
    {
      start = sg.getStartRes();
      stop = sg.getEndRes();
    }
    else
    {
      start = 0;
      stop = al.getWidth();
    }
    SequenceI[] oldOrder = al.getSequencesArray();
    AlignmentSorter.sortByFeature(types, gps, start, stop, al, method);
    avcg.addHistoryItem(new OrderCommand(methodText, oldOrder,
            viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);

  }

  @Override
  public void sortAlignmentByFeatureScore(List<String> typ)
  {
    String methodText = MessageManager.getString("label.sort_by_score");
    sortByFeatures(typ, methodText, AlignmentSorter.FEATURE_SCORE);
  }

  @Override
  public boolean parseFeaturesFile(Object file, DataSourceType protocol,
          boolean relaxedIdMatching)
  {
    boolean featuresAdded = false;
    FeatureRenderer fr = alignPanel.getFeatureRenderer();
    try
    {
      featuresAdded = new FeaturesFile(false, file, protocol).parse(
              viewport.getAlignment().getDataset(), fr.getFeatureColours(),
              fr.getFeatureFilters(), false, relaxedIdMatching);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    if (featuresAdded)
    {
      avcg.refreshFeatureUI(true);
      if (fr != null)
      {
        // update the min/max ranges where necessary
        fr.findAllFeatures(true);
      }
      if (avcg.getFeatureSettingsUI() != null)
      {
        avcg.getFeatureSettingsUI().discoverAllFeatureData();
      }
      alignPanel.paintAlignment(true, true);
    }

    return featuresAdded;

  }

  @Override
  public boolean markHighlightedColumns(boolean invert,
          boolean extendCurrent, boolean toggle)
  {
    if (!viewport.hasSearchResults())
    {
      // do nothing if no selection exists
      return false;
    }
    // JBPNote this routine could also mark rows, not just columns.
    BitSet bs = new BitSet();
    SequenceCollectionI sqcol = (viewport.getSelectionGroup() == null
            || extendCurrent) ? viewport.getAlignment()
                    : viewport.getSelectionGroup();

    // this could be a lambda... - the remains of the method is boilerplate,
    // except for the different messages for reporting selection.
    int nseq = viewport.getSearchResults().markColumns(sqcol, bs);

    ColumnSelection cs = viewport.getColumnSelection();
    if (cs == null)
    {
      cs = new ColumnSelection();
    }

    if (bs.cardinality() > 0 || invert)
    {
      boolean changed = cs.markColumns(bs, sqcol.getStartRes(),
              sqcol.getEndRes(), invert, extendCurrent, toggle);
      if (changed)
      {
        viewport.setColumnSelection(cs);
        alignPanel.paintAlignment(false, false);
        int columnCount = invert
                ? (sqcol.getEndRes() - sqcol.getStartRes() + 1)
                        - bs.cardinality()
                : bs.cardinality();
        avcg.setStatus(MessageManager.formatMessage(
                "label.view_controller_toggled_marked", new String[]
                { toggle ? MessageManager.getString("label.toggled")
                        : MessageManager.getString("label.marked"),
                    String.valueOf(columnCount),
                    invert ? MessageManager
                            .getString("label.not_containing")
                            : MessageManager.getString("label.containing"),
                    "Highlight", Integer.valueOf(nseq).toString() }));
        return true;
      }
    }
    else
    {
      avcg.setStatus(MessageManager
              .getString("label.no_highlighted_regions_marked"));
      if (!extendCurrent)
      {
        cs.clear();
        alignPanel.paintAlignment(false, false);
      }
    }
    return false;
  }

  @Override
  public boolean copyHighlightedRegionsToClipboard()
  {
    if (!viewport.hasSearchResults())
    {
      // do nothing if no selection exists
      return false;
    }

    SearchResultsI searchResults = viewport.getSearchResults();
    if (searchResults.isEmpty())
    {
      return false; // shouldn't happen
    }
    List<SequenceI> seqs = searchResults.getMatchingSubSequences();

    // TODO: pass in hiddenColumns according to intersection of searchResults
    // and visible columns. Currently this isn't done, since each contig becomes
    // a single subsequence
    Desktop.jalviewClipboard = new Object[] {
        seqs.toArray(new SequenceI[0]),
        alignPanel.getAlignment().getDataset(), null };
    avcg.setStatus(MessageManager.formatMessage(
            "label.copied_sequences_to_clipboard", seqs.size()));
    // Technically we should return false, since view has not changed
    return false;
  }
}
