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
package jalview.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JInternalFrame;

import jalview.analysis.AlignmentUtils;
import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureColourI;
import jalview.api.FeatureSettingsModelI;
import jalview.api.FeaturesDisplayedI;
import jalview.api.ViewStyleI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.commands.CommandI;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileLoader;
import jalview.io.IdentifyFile;
import jalview.renderer.ResidueShader;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.ResidueColourScheme;
import jalview.schemes.UserColourScheme;
import jalview.structure.SelectionSource;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;
import jalview.ws.params.AutoCalcSetting;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.141 $
 */
public class AlignViewport extends AlignmentViewport
        implements SelectionSource
{
  Font font;

  boolean cursorMode = false;

  boolean antiAlias = false;

  private Rectangle explodedGeometry = null;

  private String viewName = null;

  /*
   * Flag set true on the view that should 'gather' multiple views of the same
   * sequence set id when a project is reloaded. Set false on all views when
   * they are 'exploded' into separate windows. Set true on the current view
   * when 'Gather' is performed, and also on the first tab when the first new
   * view is created.
   */
  private boolean gatherViewsHere = false;

  private AnnotationColumnChooser annotationColumnSelectionState;

  /**
   * Creates a new AlignViewport object.
   * 
   * @param al
   *          alignment to view
   */
  public AlignViewport(AlignmentI al)
  {
    super(al);
    init();
  }

  /**
   * Create a new AlignViewport object with a specific sequence set ID
   * 
   * @param al
   * @param seqsetid
   *          (may be null - but potential for ambiguous constructor exception)
   */
  public AlignViewport(AlignmentI al, String seqsetid)
  {
    this(al, seqsetid, null);
  }

  public AlignViewport(AlignmentI al, String seqsetid, String viewid)
  {
    super(al);
    sequenceSetID = seqsetid;
    viewId = viewid;
    // TODO remove these once 2.4.VAMSAS release finished
    if (seqsetid != null)
    {
      Console.debug(
              "Setting viewport's sequence set id : " + sequenceSetID);
    }
    if (viewId != null)
    {
      Console.debug("Setting viewport's view id : " + viewId);
    }
    init();

  }

  /**
   * Create a new AlignViewport with hidden regions
   * 
   * @param al
   *          AlignmentI
   * @param hiddenColumns
   *          ColumnSelection
   */
  public AlignViewport(AlignmentI al, HiddenColumns hiddenColumns)
  {
    super(al);
    if (hiddenColumns != null)
    {
      al.setHiddenColumns(hiddenColumns);
    }
    init();
  }

  /**
   * New viewport with hidden columns and an existing sequence set id
   * 
   * @param al
   * @param hiddenColumns
   * @param seqsetid
   *          (may be null)
   */
  public AlignViewport(AlignmentI al, HiddenColumns hiddenColumns,
          String seqsetid)
  {
    this(al, hiddenColumns, seqsetid, null);
  }

  /**
   * New viewport with hidden columns and an existing sequence set id and viewid
   * 
   * @param al
   * @param hiddenColumns
   * @param seqsetid
   *          (may be null)
   * @param viewid
   *          (may be null)
   */
  public AlignViewport(AlignmentI al, HiddenColumns hiddenColumns,
          String seqsetid, String viewid)
  {
    super(al);
    sequenceSetID = seqsetid;
    viewId = viewid;
    // TODO remove these once 2.4.VAMSAS release finished
    if (seqsetid != null)
    {
      Console.debug(
              "Setting viewport's sequence set id : " + sequenceSetID);
    }
    if (viewId != null)
    {
      Console.debug("Setting viewport's view id : " + viewId);
    }

    if (hiddenColumns != null)
    {
      al.setHiddenColumns(hiddenColumns);
    }
    init();
  }

  /**
   * Apply any settings saved in user preferences
   */
  private void applyViewProperties()
  {
    antiAlias = Cache.getDefault("ANTI_ALIAS", true);

    viewStyle.setShowJVSuffix(Cache.getDefault("SHOW_JVSUFFIX", true));
    setShowAnnotation(Cache.getDefault("SHOW_ANNOTATIONS", true));

    setRightAlignIds(Cache.getDefault("RIGHT_ALIGN_IDS", false));
    setCentreColumnLabels(Cache.getDefault("CENTRE_COLUMN_LABELS", false));
    autoCalculateConsensus = Cache.getDefault("AUTO_CALC_CONSENSUS", true);
    autoCalculateVariance = false;

    setPadGaps(Cache.getDefault("PAD_GAPS", true));
    setShowNPFeats(Cache.getDefault("SHOW_NPFEATS_TOOLTIP", true));
    setShowDBRefs(Cache.getDefault("SHOW_DBREFS_TOOLTIP", true));
    viewStyle.setSeqNameItalics(Cache.getDefault("ID_ITALICS", true));
    viewStyle.setWrapAlignment(Cache.getDefault("WRAP_ALIGNMENT", false));
    viewStyle.setShowUnconserved(
            Cache.getDefault("SHOW_UNCONSERVED", false));
    sortByTree = Cache.getDefault("SORT_BY_TREE", false);
    followSelection = Cache.getDefault("FOLLOW_SELECTIONS", true);
    sortAnnotationsBy = SequenceAnnotationOrder
            .valueOf(Cache.getDefault(Preferences.SORT_ANNOTATIONS,
                    SequenceAnnotationOrder.NONE.name()));
    showAutocalculatedAbove = Cache
            .getDefault(Preferences.SHOW_AUTOCALC_ABOVE, false);
    viewStyle.setScaleProteinAsCdna(
            Cache.getDefault(Preferences.SCALE_PROTEIN_TO_CDNA, true));
  }

  void init()
  {
    applyViewProperties();

    String fontName = Cache.getDefault("FONT_NAME", "SansSerif");
    String fontStyle = Cache.getDefault("FONT_STYLE", Font.PLAIN + "");
    String fontSize = Cache.getDefault("FONT_SIZE", "10");

    int style = 0;

    if (fontStyle.equals("bold"))
    {
      style = 1;
    }
    else if (fontStyle.equals("italic"))
    {
      style = 2;
    }

    setFont(new Font(fontName, style, Integer.parseInt(fontSize)), true);

    alignment
            .setGapCharacter(Cache.getDefault("GAP_SYMBOL", "-").charAt(0));

    // We must set conservation and consensus before setting colour,
    // as Blosum and Clustal require this to be done
    if (hconsensus == null && !isDataset)
    {
      if (!alignment.isNucleotide())
      {
        showConservation = Cache.getDefault("SHOW_CONSERVATION", true);
        showQuality = Cache.getDefault("SHOW_QUALITY", true);
        showGroupConservation = Cache.getDefault("SHOW_GROUP_CONSERVATION",
                false);
      }
      showConsensusHistogram = Cache.getDefault("SHOW_CONSENSUS_HISTOGRAM",
              true);
      showSequenceLogo = Cache.getDefault("SHOW_CONSENSUS_LOGO", false);
      normaliseSequenceLogo = Cache.getDefault("NORMALISE_CONSENSUS_LOGO",
              false);
      showGroupConsensus = Cache.getDefault("SHOW_GROUP_CONSENSUS", false);
      showConsensus = Cache.getDefault("SHOW_IDENTITY", true);

      showOccupancy = Cache.getDefault(Preferences.SHOW_OCCUPANCY, true);
    }
    initAutoAnnotation();
    String colourProperty = alignment.isNucleotide()
            ? Preferences.DEFAULT_COLOUR_NUC
            : Preferences.DEFAULT_COLOUR_PROT;
    String schemeName = Cache.getProperty(colourProperty);
    if (schemeName == null)
    {
      // only DEFAULT_COLOUR available in Jalview before 2.9
      schemeName = Cache.getDefault(Preferences.DEFAULT_COLOUR,
              ResidueColourScheme.NONE);
    }
    ColourSchemeI colourScheme = ColourSchemeProperty.getColourScheme(this,
            alignment, schemeName);
    residueShading = new ResidueShader(colourScheme);

    if (colourScheme instanceof UserColourScheme)
    {
      residueShading = new ResidueShader(
              UserDefinedColours.loadDefaultColours());
      residueShading.setThreshold(0, isIgnoreGapsConsensus());
    }

    if (residueShading != null)
    {
      residueShading.setConsensus(hconsensus);
      //&!
      residueShading.setVariance(hvariance);
    }
    setColourAppliesToAllGroups(true);
  }

  boolean validCharWidth;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFont(Font f, boolean setGrid)
  {
    font = f;

    Container c = new Container();

    if (setGrid)
    {
      FontMetrics fm = c.getFontMetrics(font);
      int ww = fm.charWidth('M');
      setCharHeight(fm.getHeight());
      setCharWidth(ww);
    }
    viewStyle.setFontName(font.getName());
    viewStyle.setFontStyle(font.getStyle());
    viewStyle.setFontSize(font.getSize());

    validCharWidth = true;
  }

  @Override
  public void setViewStyle(ViewStyleI settingsForView)
  {
    super.setViewStyle(settingsForView);
    setFont(new Font(viewStyle.getFontName(), viewStyle.getFontStyle(),
            viewStyle.getFontSize()), false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Font getFont()
  {
    return font;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param align
   *          DOCUMENT ME!
   */
  @Override
  public void setAlignment(AlignmentI align)
  {
    replaceMappings(align);
    super.setAlignment(align);
  }

  /**
   * Replace any codon mappings for this viewport with those for the given
   * viewport
   * 
   * @param align
   */
  public void replaceMappings(AlignmentI align)
  {

    /*
     * Deregister current mappings (if any)
     */
    deregisterMappings();

    /*
     * Register new mappings (if any)
     */
    if (align != null)
    {
      StructureSelectionManager ssm = StructureSelectionManager
              .getStructureSelectionManager(Desktop.instance);
      ssm.registerMappings(align.getCodonFrames());
    }

    /*
     * replace mappings on our alignment
     */
    if (alignment != null && align != null)
    {
      alignment.setCodonFrames(align.getCodonFrames());
    }
  }

  protected void deregisterMappings()
  {
    AlignmentI al = getAlignment();
    if (al != null)
    {
      List<AlignedCodonFrame> mappings = al.getCodonFrames();
      if (mappings != null)
      {
        StructureSelectionManager ssm = StructureSelectionManager
                .getStructureSelectionManager(Desktop.instance);
        for (AlignedCodonFrame acf : mappings)
        {
          if (noReferencesTo(acf))
          {
            ssm.deregisterMapping(acf);
          }
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public char getGapCharacter()
  {
    return getAlignment().getGapCharacter();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param gap
   *          DOCUMENT ME!
   */
  public void setGapCharacter(char gap)
  {
    if (getAlignment() != null)
    {
      getAlignment().setGapCharacter(gap);
    }
  }

  /**
   * get hash of undo and redo list for the alignment
   * 
   * @return long[] { historyList.hashCode, redoList.hashCode };
   */
  public long[] getUndoRedoHash()
  {
    // TODO: JAL-1126
    if (historyList == null || redoList == null)
    {
      return new long[] { -1, -1 };
    }
    return new long[] { historyList.hashCode(), this.redoList.hashCode() };
  }

  /**
   * test if a particular set of hashcodes are different to the hashcodes for
   * the undo and redo list.
   * 
   * @param undoredo
   *          the stored set of hashcodes as returned by getUndoRedoHash
   * @return true if the hashcodes differ (ie the alignment has been edited) or
   *         the stored hashcode array differs in size
   */
  public boolean isUndoRedoHashModified(long[] undoredo)
  {
    if (undoredo == null)
    {
      return true;
    }
    long[] cstate = getUndoRedoHash();
    if (cstate.length != undoredo.length)
    {
      return true;
    }

    for (int i = 0; i < cstate.length; i++)
    {
      if (cstate[i] != undoredo[i])
      {
        return true;
      }
    }
    return false;
  }

  public boolean followSelection = true;

  /**
   * @return true if view selection should always follow the selections
   *         broadcast by other selection sources
   */
  public boolean getFollowSelection()
  {
    return followSelection;
  }

  /**
   * Send the current selection to be broadcast to any selection listeners.
   */
  @Override
  public void sendSelection()
  {
    jalview.structure.StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance)
            .sendSelection(new SequenceGroup(getSelectionGroup()),
                    new ColumnSelection(getColumnSelection()),
                    new HiddenColumns(getAlignment().getHiddenColumns()),
                    this);
  }

  /**
   * return the alignPanel containing the given viewport. Use this to get the
   * components currently handling the given viewport.
   * 
   * @param av
   * @return null or an alignPanel guaranteed to have non-null alignFrame
   *         reference
   */
  public AlignmentPanel getAlignPanel()
  {
    AlignmentPanel[] aps = PaintRefresher
            .getAssociatedPanels(this.getSequenceSetId());
    for (int p = 0; aps != null && p < aps.length; p++)
    {
      if (aps[p].av == this)
      {
        return aps[p];
      }
    }
    return null;
  }

  public boolean getSortByTree()
  {
    return sortByTree;
  }

  public void setSortByTree(boolean sort)
  {
    sortByTree = sort;
  }

  /**
   * Returns the (Desktop) instance of the StructureSelectionManager
   */
  @Override
  public StructureSelectionManager getStructureSelectionManager()
  {
    return StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
  }

  @Override
  public boolean isNormaliseSequenceLogo()
  {
    return normaliseSequenceLogo;
  }

  public void setNormaliseSequenceLogo(boolean state)
  {
    normaliseSequenceLogo = state;
  }

  /**
   * 
   * @return true if alignment characters should be displayed
   */
  @Override
  public boolean isValidCharWidth()
  {
    return validCharWidth;
  }

  private Hashtable<String, AutoCalcSetting> calcIdParams = new Hashtable<>();

  public AutoCalcSetting getCalcIdSettingsFor(String calcId)
  {
    return calcIdParams.get(calcId);
  }

  public void setCalcIdSettingsFor(String calcId, AutoCalcSetting settings,
          boolean needsUpdate)
  {
    calcIdParams.put(calcId, settings);
    // TODO: create a restart list to trigger any calculations that need to be
    // restarted after load
    // calculator.getRegisteredWorkersOfClass(settings.getWorkerClass())
    if (needsUpdate)
    {
      Console.debug("trigger update for " + calcId);
    }
  }

  /**
   * Method called when another alignment's edit (or possibly other) command is
   * broadcast to here.
   *
   * To allow for sequence mappings (e.g. protein to cDNA), we have to first
   * 'unwind' the command on the source sequences (in simulation, not in fact),
   * and then for each edit in turn:
   * <ul>
   * <li>compute the equivalent edit on the mapped sequences</li>
   * <li>apply the mapped edit</li>
   * <li>'apply' the source edit to the working copy of the source
   * sequences</li>
   * </ul>
   * 
   * @param command
   * @param undo
   * @param ssm
   */
  @Override
  public void mirrorCommand(CommandI command, boolean undo,
          StructureSelectionManager ssm, VamsasSource source)
  {
    /*
     * Do nothing unless we are a 'complement' of the source. May replace this
     * with direct calls not via SSM.
     */
    if (source instanceof AlignViewportI
            && ((AlignViewportI) source).getCodingComplement() == this)
    {
      // ok to continue;
    }
    else
    {
      return;
    }

    CommandI mappedCommand = ssm.mapCommand(command, undo, getAlignment(),
            getGapCharacter());
    if (mappedCommand != null)
    {
      AlignmentI[] views = getAlignPanel().alignFrame.getViewAlignments();
      mappedCommand.doCommand(views);
      getAlignPanel().alignmentChanged();
    }
  }

  /**
   * Add the sequences from the given alignment to this viewport. Optionally,
   * may give the user the option to open a new frame, or split panel, with cDNA
   * and protein linked.
   * 
   * @param toAdd
   * @param title
   */
  public void addAlignment(AlignmentI toAdd, String title)
  {
    // TODO: promote to AlignViewportI? applet CutAndPasteTransfer is different

    // JBPComment: title is a largely redundant parameter at the moment
    // JBPComment: this really should be an 'insert/pre/append' controller
    // JBPComment: but the DNA/Protein check makes it a bit more complex

    // refactored from FileLoader / CutAndPasteTransfer / SequenceFetcher with
    // this comment:
    // TODO: create undo object for this JAL-1101

    /*
     * Ensure datasets are created for the new alignment as
     * mappings operate on dataset sequences
     */
    toAdd.setDataset(null);

    /*
     * Check if any added sequence could be the object of a mapping or
     * cross-reference; if so, make the mapping explicit 
     */
    getAlignment().realiseMappings(toAdd.getSequences());

    /*
     * If any cDNA/protein mappings exist or can be made between the alignments, 
     * offer to open a split frame with linked alignments
     */
    if (Cache.getDefault(Preferences.ENABLE_SPLIT_FRAME, true))
    {
      if (AlignmentUtils.isMappable(toAdd, getAlignment()))
      {
        openLinkedAlignment(toAdd, title);
        return;
      }
    }
    addDataToAlignment(toAdd);
  }

  /**
   * adds sequences to this alignment
   * 
   * @param toAdd
   */
  void addDataToAlignment(AlignmentI toAdd)
  {
    // TODO: JAL-407 regardless of above - identical sequences (based on ID and
    // provenance) should share the same dataset sequence

    AlignmentI al = getAlignment();
    String gap = String.valueOf(al.getGapCharacter());
    for (int i = 0; i < toAdd.getHeight(); i++)
    {
      SequenceI seq = toAdd.getSequenceAt(i);
      /*
       * experimental!
       * - 'align' any mapped sequences as per existing 
       *    e.g. cdna to genome, domain hit to protein sequence
       * very experimental! (need a separate menu option for this)
       * - only add mapped sequences ('select targets from a dataset')
       */
      if (true /*AlignmentUtils.alignSequenceAs(seq, al, gap, true, true)*/)
      {
        al.addSequence(seq);
      }
    }
    for (ContactMatrixI cm : toAdd.getContactMaps())
    {
      al.addContactList(cm);
    }
    ranges.setEndSeq(getAlignment().getHeight() - 1); // BH 2019.04.18
    firePropertyChange("alignment", null, getAlignment().getSequences());
  }

  /**
   * Load a File into this AlignViewport attempting to detect format if not
   * given or given as null.
   * 
   * @param file
   * @param format
   */
  public void addFile(File file, FileFormatI format)
  {
    addFile(file, format, true);
  }

  public void addFile(File file, FileFormatI format, boolean async)
  {
    DataSourceType protocol = AppletFormatAdapter.checkProtocol(file);

    if (format == null)
    {
      try
      {
        format = new IdentifyFile().identify(file, protocol);
      } catch (FileFormatException e1)
      {
        jalview.bin.Console.error("Unknown file format for '" + file + "'");
      }
    }
    else if (FileFormats.getInstance().isIdentifiable(format))
    {
      try
      {
        format = new IdentifyFile().identify(file, protocol);
      } catch (FileFormatException e)
      {
        jalview.bin.Console.error("Unknown file format for '" + file + "'",
                e);
      }
    }

    new FileLoader().LoadFile(this, file, DataSourceType.FILE, format,
            async);
  }

  public void addFile(File file)
  {
    addFile(file, null);
  }

  /**
   * Show a dialog with the option to open and link (cDNA <-> protein) as a new
   * alignment, either as a standalone alignment or in a split frame. Returns
   * true if the new alignment was opened, false if not, because the user
   * declined the offer.
   * 
   * @param al
   * @param title
   */
  protected void openLinkedAlignment(AlignmentI al, String title)
  {
    String[] options = new String[] { MessageManager.getString("action.no"),
        MessageManager.getString("label.split_window"),
        MessageManager.getString("label.new_window"), };
    final String question = JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.open_split_window?"));
    final AlignViewport us = this;

    /*
     * options No, Split Window, New Window correspond to
     * dialog responses 0, 1, 2 (even though JOptionPane shows them
     * in reverse order)
     */
    JvOptionPane dialog = JvOptionPane.newOptionDialog(Desktop.desktop)
            .setResponseHandler(0, () -> {
              addDataToAlignment(al);
            }).setResponseHandler(1, () -> {
              us.openLinkedAlignmentAs(al, title, true);
            }).setResponseHandler(2, () -> {
              us.openLinkedAlignmentAs(al, title, false);
            });
    dialog.showDialog(question,
            MessageManager.getString("label.open_split_window"),
            JvOptionPane.DEFAULT_OPTION, JvOptionPane.PLAIN_MESSAGE, null,
            options, options[0]);
  }

  protected void openLinkedAlignmentAs(AlignmentI al, String title,
          boolean newWindowOrSplitPane)
  {
    /*
     * Identify protein and dna alignments. Make a copy of this one if opening
     * in a new split pane.
     */
    AlignmentI thisAlignment = newWindowOrSplitPane
            ? new Alignment(getAlignment())
            : getAlignment();
    AlignmentI protein = al.isNucleotide() ? thisAlignment : al;
    final AlignmentI cdna = al.isNucleotide() ? al : thisAlignment;

    /*
     * Map sequences. At least one should get mapped as we have already passed
     * the test for 'mappability'. Any mappings made will be added to the
     * protein alignment. Note creating dataset sequences on the new alignment
     * is a pre-requisite for building mappings.
     */
    al.setDataset(null);
    AlignmentUtils.mapProteinAlignmentToCdna(protein, cdna);

    /*
     * Create the AlignFrame for the added alignment. If it is protein, mappings
     * are registered with StructureSelectionManager as a side-effect.
     */
    AlignFrame newAlignFrame = new AlignFrame(al, AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);
    newAlignFrame.setTitle(title);
    newAlignFrame.setStatus(MessageManager
            .formatMessage("label.successfully_loaded_file", new Object[]
            { title }));

    // TODO if we want this (e.g. to enable reload of the alignment from file),
    // we will need to add parameters to the stack.
    // if (!protocol.equals(DataSourceType.PASTE))
    // {
    // alignFrame.setFileName(file, format);
    // }

    if (!newWindowOrSplitPane)
    {
      Desktop.addInternalFrame(newAlignFrame, title,
              AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
    }

    try
    {
      newAlignFrame.setMaximum(Cache.getDefault("SHOW_FULLSCREEN", false));
    } catch (java.beans.PropertyVetoException ex)
    {
    }

    if (newWindowOrSplitPane)
    {
      al.alignAs(thisAlignment);
      protein = openSplitFrame(newAlignFrame, thisAlignment);
    }
  }

  /**
   * Helper method to open a new SplitFrame holding linked dna and protein
   * alignments.
   * 
   * @param newAlignFrame
   *          containing a new alignment to be shown
   * @param complement
   *          cdna/protein complement alignment to show in the other split half
   * @return the protein alignment in the split frame
   */
  protected AlignmentI openSplitFrame(AlignFrame newAlignFrame,
          AlignmentI complement)
  {
    /*
     * Make a new frame with a copy of the alignment we are adding to. If this
     * is protein, the mappings to cDNA will be registered with
     * StructureSelectionManager as a side-effect.
     */
    AlignFrame copyMe = new AlignFrame(complement, AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);
    copyMe.setTitle(getAlignPanel().alignFrame.getTitle());

    AlignmentI al = newAlignFrame.viewport.getAlignment();
    final AlignFrame proteinFrame = al.isNucleotide() ? copyMe
            : newAlignFrame;
    final AlignFrame cdnaFrame = al.isNucleotide() ? newAlignFrame : copyMe;
    cdnaFrame.setVisible(true);
    proteinFrame.setVisible(true);
    String linkedTitle = MessageManager
            .getString("label.linked_view_title");

    /*
     * Open in split pane. DNA sequence above, protein below.
     */
    JInternalFrame splitFrame = new SplitFrame(cdnaFrame, proteinFrame);
    Desktop.addInternalFrame(splitFrame, linkedTitle, -1, -1);

    return proteinFrame.viewport.getAlignment();
  }

  public AnnotationColumnChooser getAnnotationColumnSelectionState()
  {
    return annotationColumnSelectionState;
  }

  public void setAnnotationColumnSelectionState(
          AnnotationColumnChooser currentAnnotationColumnSelectionState)
  {
    this.annotationColumnSelectionState = currentAnnotationColumnSelectionState;
  }

  @Override
  public void setIdWidth(int i)
  {
    super.setIdWidth(i);
    AlignmentPanel ap = getAlignPanel();
    if (ap != null)
    {
      // modify GUI elements to reflect geometry change
      Dimension idw = ap.getIdPanel().getIdCanvas().getPreferredSize();
      idw.width = i;
      ap.getIdPanel().getIdCanvas().setPreferredSize(idw);
    }
  }

  public Rectangle getExplodedGeometry()
  {
    return explodedGeometry;
  }

  public void setExplodedGeometry(Rectangle explodedPosition)
  {
    this.explodedGeometry = explodedPosition;
  }

  public boolean isGatherViewsHere()
  {
    return gatherViewsHere;
  }

  public void setGatherViewsHere(boolean gatherViewsHere)
  {
    this.gatherViewsHere = gatherViewsHere;
  }

  /**
   * If this viewport has a (Protein/cDNA) complement, then scroll the
   * complementary alignment to match this one.
   */
  public void scrollComplementaryAlignment()
  {
    /*
     * Populate a SearchResults object with the mapped location to scroll to. If
     * there is no complement, or it is not following highlights, or no mapping
     * is found, the result will be empty.
     */
    SearchResultsI sr = new SearchResults();
    int verticalOffset = findComplementScrollTarget(sr);
    if (!sr.isEmpty())
    {
      // TODO would like next line without cast but needs more refactoring...
      final AlignmentPanel complementPanel = ((AlignViewport) getCodingComplement())
              .getAlignPanel();
      complementPanel.setToScrollComplementPanel(false);
      complementPanel.scrollToCentre(sr, verticalOffset);
      complementPanel.setToScrollComplementPanel(true);
    }
  }

  /**
   * Answers true if no alignment holds a reference to the given mapping
   * 
   * @param acf
   * @return
   */
  protected boolean noReferencesTo(AlignedCodonFrame acf)
  {
    AlignFrame[] frames = Desktop.getAlignFrames();
    if (frames == null)
    {
      return true;
    }
    for (AlignFrame af : frames)
    {
      if (!af.isClosed())
      {
        for (AlignmentViewPanel ap : af.getAlignPanels())
        {
          AlignmentI al = ap.getAlignment();
          if (al != null && al.getCodonFrames().contains(acf))
          {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Applies the supplied feature settings descriptor to currently known
   * features. This supports an 'initial configuration' of feature colouring
   * based on a preset or user favourite. This may then be modified in the usual
   * way using the Feature Settings dialogue.
   * 
   * @param featureSettings
   */
  @Override
  public void applyFeaturesStyle(FeatureSettingsModelI featureSettings)
  {
    transferFeaturesStyles(featureSettings, false);
  }

  /**
   * Applies the supplied feature settings descriptor to currently known
   * features. This supports an 'initial configuration' of feature colouring
   * based on a preset or user favourite. This may then be modified in the usual
   * way using the Feature Settings dialogue.
   * 
   * @param featureSettings
   */
  @Override
  public void mergeFeaturesStyle(FeatureSettingsModelI featureSettings)
  {
    transferFeaturesStyles(featureSettings, true);
  }

  /**
   * when mergeOnly is set, then group and feature visibility or feature colours
   * are not modified for features and groups already known to the feature
   * renderer. Feature ordering is always adjusted, and transparency is always
   * set regardless.
   * 
   * @param featureSettings
   * @param mergeOnly
   */
  private void transferFeaturesStyles(FeatureSettingsModelI featureSettings,
          boolean mergeOnly)
  {
    if (featureSettings == null)
    {
      return;
    }

    FeatureRenderer fr = getAlignPanel().getSeqPanel().seqCanvas
            .getFeatureRenderer();
    List<String> origRenderOrder = new ArrayList<>();
    List<String> origGroups = new ArrayList<>();
    // preserve original render order - allows differentiation between user
    // configured colours and autogenerated ones
    origRenderOrder.addAll(fr.getRenderOrder());
    origGroups.addAll(fr.getFeatureGroups());

    fr.findAllFeatures(true);
    List<String> renderOrder = fr.getRenderOrder();
    FeaturesDisplayedI displayed = fr.getFeaturesDisplayed();
    if (!mergeOnly)
    {
      // only clear displayed features if we are mergeing
      // displayed.clear();
    }
    // TODO this clears displayed.featuresRegistered - do we care?
    //
    // JAL-3330 - JBP - yes we do - calling applyFeatureStyle to a view where
    // feature visibility has already been configured is not very friendly
    /*
     * set feature colour if specified by feature settings
     * set visibility of all features
     */
    for (String type : renderOrder)
    {
      FeatureColourI preferredColour = featureSettings
              .getFeatureColour(type);
      FeatureColourI origColour = fr.getFeatureStyle(type);
      if (!mergeOnly || (!origRenderOrder.contains(type)
              || origColour == null
              || (!origColour.isGraduatedColour()
                      && origColour.getColour() != null
                      && origColour.getColour().equals(
                              ColorUtils.createColourFromName(type)))))
      {
        // if we are merging, only update if there wasn't already a colour
        // defined for
        // this type
        if (preferredColour != null)
        {
          fr.setColour(type, preferredColour);
        }
        if (featureSettings.isFeatureDisplayed(type))
        {
          displayed.setVisible(type);
        }
        else if (featureSettings.isFeatureHidden(type))
        {
          displayed.setHidden(type);
        }
      }
    }

    /*
     * set visibility of feature groups
     */
    for (String group : fr.getFeatureGroups())
    {
      if (!mergeOnly || !origGroups.contains(group))
      {
        // when merging, display groups only if the aren't already marked as not
        // visible
        fr.setGroupVisibility(group,
                featureSettings.isGroupDisplayed(group));
      }
    }

    /*
     * order the features
     */
    if (featureSettings.optimiseOrder())
    {
      // TODO not supported (yet?)
    }
    else
    {
      fr.orderFeatures(featureSettings);
    }
    fr.setTransparency(featureSettings.getTransparency());

    fr.notifyFeaturesChanged();
  }

  public String getViewName()
  {
    return viewName;
  }

  public void setViewName(String viewName)
  {
    this.viewName = viewName;
  }

}
