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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ext.vamsas.ServiceHandle;
import jalview.analysis.AlignmentSorter;
import jalview.analysis.AlignmentUtils;
import jalview.analysis.CrossRef;
import jalview.analysis.Dna;
import jalview.analysis.GeneticCodeI;
import jalview.analysis.ParseProperties;
import jalview.analysis.SequenceIdMatcher;
import jalview.api.AlignExportSettingsI;
import jalview.api.AlignViewControllerGuiI;
import jalview.api.AlignViewControllerI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureSettingsControllerI;
import jalview.api.FeatureSettingsModelI;
import jalview.api.SplitContainerI;
import jalview.api.ViewStyleI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.bin.Jalview;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.OrderCommand;
import jalview.commands.RemoveGapColCommand;
import jalview.commands.RemoveGapsCommand;
import jalview.commands.SlideSequencesCommand;
import jalview.commands.TrimRegionCommand;
import jalview.datamodel.AlignExportSettingsAdapter;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentExportData;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentOrder;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.ColourMenuHelper.ColourChangeListener;
import jalview.gui.ViewSelectionMenu.ViewSetProvider;
import jalview.io.AlignmentProperties;
import jalview.io.AnnotationFile;
import jalview.io.BackupFiles;
import jalview.io.BioJsHTMLOutput;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileLoader;
import jalview.io.FileParse;
import jalview.io.FormatAdapter;
import jalview.io.HtmlSvgOutput;
import jalview.io.IdentifyFile;
import jalview.io.JPredFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.io.JnetAnnotationMaker;
import jalview.io.NewickFile;
import jalview.io.ScoreMatrixFile;
import jalview.io.TCoffeeScoreFile;
import jalview.io.exceptions.ImageOutputException;
import jalview.io.vcf.VCFLoader;
import jalview.jbgui.GAlignFrame;
import jalview.project.Jalview2XML;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemes;
import jalview.schemes.ResidueColourScheme;
import jalview.schemes.TCoffeeColourScheme;
import jalview.util.HttpUtils;
import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.imagemaker.BitmapImageSizing;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.ViewportRanges;
import jalview.ws.DBRefFetcher;
import jalview.ws.DBRefFetcher.FetchFinishedListenerI;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;
import jalview.ws.jws1.Discoverer;
import jalview.ws.jws2.Jws2Discoverer;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.seqfetcher.DbSourceProxy;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class AlignFrame extends GAlignFrame implements DropTargetListener,
        IProgressIndicator, AlignViewControllerGuiI, ColourChangeListener
{

  public static final int DEFAULT_WIDTH = 700;

  public static final int DEFAULT_HEIGHT = 500;

  /*
   * The currently displayed panel (selected tabbed view if more than one)
   */
  public AlignmentPanel alignPanel;

  AlignViewport viewport;

  public AlignViewControllerI avc;

  List<AlignmentPanel> alignPanels = new ArrayList<>();

  /**
   * Last format used to load or save alignments in this window
   */
  FileFormatI currentFileFormat = null;

  /**
   * Current filename for this alignment
   */
  String fileName = null;

  File fileObject;

  /**
   * Creates a new AlignFrame object with specific width and height.
   * 
   * @param al
   * @param width
   * @param height
   */
  public AlignFrame(AlignmentI al, int width, int height)
  {
    this(al, null, width, height);
  }

  /**
   * Creates a new AlignFrame object with specific width, height and
   * sequenceSetId
   * 
   * @param al
   * @param width
   * @param height
   * @param sequenceSetId
   */
  public AlignFrame(AlignmentI al, int width, int height,
          String sequenceSetId)
  {
    this(al, null, width, height, sequenceSetId);
  }

  /**
   * Creates a new AlignFrame object with specific width, height and
   * sequenceSetId
   * 
   * @param al
   * @param width
   * @param height
   * @param sequenceSetId
   * @param viewId
   */
  public AlignFrame(AlignmentI al, int width, int height,
          String sequenceSetId, String viewId)
  {
    this(al, null, width, height, sequenceSetId, viewId);
  }

  /**
   * new alignment window with hidden columns
   * 
   * @param al
   *          AlignmentI
   * @param hiddenColumns
   *          ColumnSelection or null
   * @param width
   *          Width of alignment frame
   * @param height
   *          height of frame.
   */
  public AlignFrame(AlignmentI al, HiddenColumns hiddenColumns, int width,
          int height)
  {
    this(al, hiddenColumns, width, height, null);
  }

  /**
   * Create alignment frame for al with hiddenColumns, a specific width and
   * height, and specific sequenceId
   * 
   * @param al
   * @param hiddenColumns
   * @param width
   * @param height
   * @param sequenceSetId
   *          (may be null)
   */
  public AlignFrame(AlignmentI al, HiddenColumns hiddenColumns, int width,
          int height, String sequenceSetId)
  {
    this(al, hiddenColumns, width, height, sequenceSetId, null);
  }

  /**
   * Create alignment frame for al with hiddenColumns, a specific width and
   * height, and specific sequenceId
   * 
   * @param al
   * @param hiddenColumns
   * @param width
   * @param height
   * @param sequenceSetId
   *          (may be null)
   * @param viewId
   *          (may be null)
   */
  public AlignFrame(AlignmentI al, HiddenColumns hiddenColumns, int width,
          int height, String sequenceSetId, String viewId)
  {
    setSize(width, height);

    if (al.getDataset() == null)
    {
      al.setDataset(null);
    }

    viewport = new AlignViewport(al, hiddenColumns, sequenceSetId, viewId);

    alignPanel = new AlignmentPanel(this, viewport);

    addAlignmentPanel(alignPanel, true);
    init();
  }

  public AlignFrame(AlignmentI al, SequenceI[] hiddenSeqs,
          HiddenColumns hiddenColumns, int width, int height)
  {
    setSize(width, height);

    if (al.getDataset() == null)
    {
      al.setDataset(null);
    }

    viewport = new AlignViewport(al, hiddenColumns);

    if (hiddenSeqs != null && hiddenSeqs.length > 0)
    {
      viewport.hideSequence(hiddenSeqs);
    }
    alignPanel = new AlignmentPanel(this, viewport);
    addAlignmentPanel(alignPanel, true);
    init();
  }

  /**
   * Make a new AlignFrame from existing alignmentPanels
   * 
   * @param ap
   *          AlignmentPanel
   * @param av
   *          AlignViewport
   */
  public AlignFrame(AlignmentPanel ap)
  {
    viewport = ap.av;
    alignPanel = ap;
    addAlignmentPanel(ap, false);
    init();
  }

  /**
   * initalise the alignframe from the underlying viewport data and the
   * configurations
   */
  void init()
  {
    setFrameIcon(null);

    // setBackground(Color.white); // BH 2019

    if (!Jalview.isHeadlessMode())
    {
      progressBar = new ProgressBar(this.statusPanel, this.statusBar);
    }

    avc = new jalview.controller.AlignViewController(this, viewport,
            alignPanel);
    if (viewport.getAlignmentConservationAnnotation() == null)
    {
      // BLOSUM62Colour.setEnabled(false);
      conservationMenuItem.setEnabled(false);
      modifyConservation.setEnabled(false);
      // PIDColour.setEnabled(false);
      // abovePIDThreshold.setEnabled(false);
      // modifyPID.setEnabled(false);
    }

    String sortby = Cache.getDefault("SORT_ALIGNMENT", "No sort");

    if (sortby.equals("Id"))
    {
      sortIDMenuItem_actionPerformed(null);
    }
    else if (sortby.equals("Pairwise Identity"))
    {
      sortPairwiseMenuItem_actionPerformed(null);
    }

    this.alignPanel.av
            .setShowAutocalculatedAbove(isShowAutoCalculatedAbove());

    setMenusFromViewport(viewport);
    buildSortByAnnotationScoresMenu();
    calculateTree.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        openTreePcaDialog();
      }
    });
    
    calculateCustom.addActionListener(new ActionListener()
    {
           @Override
           public void actionPerformed(ActionEvent e)
           {
             openCustomDialog();
           }
    });
    buildColourMenu();

    if (Desktop.desktop != null)
    {
      this.setDropTarget(new java.awt.dnd.DropTarget(this, this));
      if (!Platform.isJS())
      {
        addServiceListeners();
      }
      setGUINucleotide();
    }

    if (viewport.getWrapAlignment())
    {
      wrapMenuItem_actionPerformed(null);
    }

    if (Cache.getDefault("SHOW_OVERVIEW", false))
    {
      this.overviewMenuItem_actionPerformed(null);
    }

    addKeyListener();

    final List<AlignmentViewPanel> selviews = new ArrayList<>();
    final List<AlignmentPanel> origview = new ArrayList<>();
    final String menuLabel = MessageManager
            .getString("label.copy_format_from");
    ViewSelectionMenu vsel = new ViewSelectionMenu(menuLabel,
            new ViewSetProvider()
            {

              @Override
              public AlignmentPanel[] getAllAlignmentPanels()
              {
                origview.clear();
                origview.add(alignPanel);
                // make an array of all alignment panels except for this one
                List<AlignmentPanel> aps = new ArrayList<>(
                        Arrays.asList(Desktop.getAlignmentPanels(null)));
                aps.remove(AlignFrame.this.alignPanel);
                return aps.toArray(new AlignmentPanel[aps.size()]);
              }
            }, selviews, new ItemListener()
            {

              @Override
              public void itemStateChanged(ItemEvent e)
              {
                if (origview.size() > 0)
                {
                  final AlignmentPanel ap = origview.get(0);

                  /*
                   * Copy the ViewStyle of the selected panel to 'this one'.
                   * Don't change value of 'scaleProteinAsCdna' unless copying
                   * from a SplitFrame.
                   */
                  ViewStyleI vs = selviews.get(0).getAlignViewport()
                          .getViewStyle();
                  boolean fromSplitFrame = selviews.get(0)
                          .getAlignViewport().getCodingComplement() != null;
                  if (!fromSplitFrame)
                  {
                    vs.setScaleProteinAsCdna(ap.getAlignViewport()
                            .getViewStyle().isScaleProteinAsCdna());
                  }
                  ap.getAlignViewport().setViewStyle(vs);

                  /*
                   * Also rescale ViewStyle of SplitFrame complement if there is
                   * one _and_ it is set to 'scaledProteinAsCdna'; we don't copy
                   * the whole ViewStyle (allow cDNA protein to have different
                   * fonts)
                   */
                  AlignViewportI complement = ap.getAlignViewport()
                          .getCodingComplement();
                  if (complement != null && vs.isScaleProteinAsCdna())
                  {
                    AlignFrame af = Desktop.getAlignFrameFor(complement);
                    ((SplitFrame) af.getSplitViewContainer())
                            .adjustLayout();
                    af.setMenusForViewport();
                  }

                  ap.updateLayout();
                  ap.setSelected(true);
                  ap.alignFrame.setMenusForViewport();

                }
              }
            });
    if (Cache.getDefault("VERSION", "DEVELOPMENT").toLowerCase(Locale.ROOT)
            .indexOf("devel") > -1
            || Cache.getDefault("VERSION", "DEVELOPMENT")
                    .toLowerCase(Locale.ROOT).indexOf("test") > -1)
    {
      formatMenu.add(vsel);
    }
    addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        Jalview.setCurrentAlignFrame(AlignFrame.this);
      }
    });

  }

  /**
   * Change the filename and format for the alignment, and enable the 'reload'
   * button functionality.
   * 
   * @param file
   *          valid filename
   * @param format
   *          format of file
   */
  public void setFileName(String file, FileFormatI format)
  {
    fileName = file;
    setFileFormat(format);
    reload.setEnabled(true);
  }

  /**
   * JavaScript will have this, maybe others. More dependable than a file name
   * and maintains a reference to the actual bytes loaded.
   * 
   * @param file
   */
  public void setFileObject(File file)
  {
    this.fileObject = file;
  }

  /**
   * Add a KeyListener with handlers for various KeyPressed and KeyReleased
   * events
   */
  void addKeyListener()
  {
    addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (viewport.cursorMode
                && ((evt.getKeyCode() >= KeyEvent.VK_0
                        && evt.getKeyCode() <= KeyEvent.VK_9)
                        || (evt.getKeyCode() >= KeyEvent.VK_NUMPAD0
                                && evt.getKeyCode() <= KeyEvent.VK_NUMPAD9))
                && Character.isDigit(evt.getKeyChar()))
        {
          alignPanel.getSeqPanel().numberPressed(evt.getKeyChar());
        }

        switch (evt.getKeyCode())
        {

        case 27: // escape key
          deselectAllSequenceMenuItem_actionPerformed(null);

          break;

        case KeyEvent.VK_DOWN:
          if (evt.isAltDown() || !viewport.cursorMode)
          {
            moveSelectedSequences(false);
          }
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().moveCursor(0, 1, evt.isShiftDown());
          }
          break;

        case KeyEvent.VK_UP:
          if (evt.isAltDown() || !viewport.cursorMode)
          {
            moveSelectedSequences(true);
          }
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().moveCursor(0, -1, evt.isShiftDown());
          }

          break;

        case KeyEvent.VK_LEFT:
          if (evt.isAltDown() || !viewport.cursorMode)
          {
            slideSequences(false,
                    alignPanel.getSeqPanel().getKeyboardNo1());
          }
          else
          {
            alignPanel.getSeqPanel().moveCursor(-1, 0, evt.isShiftDown());
          }

          break;

        case KeyEvent.VK_RIGHT:
          if (evt.isAltDown() || !viewport.cursorMode)
          {
            slideSequences(true, alignPanel.getSeqPanel().getKeyboardNo1());
          }
          else
          {
            alignPanel.getSeqPanel().moveCursor(1, 0, evt.isShiftDown());
          }
          break;

        case KeyEvent.VK_SPACE:
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().insertGapAtCursor(evt.isControlDown()
                    || evt.isShiftDown() || evt.isAltDown());
          }
          break;

        // case KeyEvent.VK_A:
        // if (viewport.cursorMode)
        // {
        // alignPanel.seqPanel.insertNucAtCursor(false,"A");
        // //System.out.println("A");
        // }
        // break;
        /*
         * case KeyEvent.VK_CLOSE_BRACKET: if (viewport.cursorMode) {
         * System.out.println("closing bracket"); } break;
         */
        case KeyEvent.VK_DELETE:
        case KeyEvent.VK_BACK_SPACE:
          if (!viewport.cursorMode)
          {
            cut_actionPerformed();
          }
          else
          {
            alignPanel.getSeqPanel().deleteGapAtCursor(evt.isControlDown()
                    || evt.isShiftDown() || evt.isAltDown());
          }

          break;

        case KeyEvent.VK_S:
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().setCursorRow();
          }
          break;
        case KeyEvent.VK_C:
          if (viewport.cursorMode && !evt.isControlDown())
          {
            alignPanel.getSeqPanel().setCursorColumn();
          }
          break;
        case KeyEvent.VK_P:
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().setCursorPosition();
          }
          break;

        case KeyEvent.VK_ENTER:
        case KeyEvent.VK_COMMA:
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().setCursorRowAndColumn();
          }
          break;

        case KeyEvent.VK_Q:
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().setSelectionAreaAtCursor(true);
          }
          break;
        case KeyEvent.VK_M:
          if (viewport.cursorMode)
          {
            alignPanel.getSeqPanel().setSelectionAreaAtCursor(false);
          }
          break;

        case KeyEvent.VK_F2:
          viewport.cursorMode = !viewport.cursorMode;
          setStatus(MessageManager
                  .formatMessage("label.keyboard_editing_mode", new String[]
                  { (viewport.cursorMode ? "on" : "off") }));
          if (viewport.cursorMode)
          {
            ViewportRanges ranges = viewport.getRanges();
            alignPanel.getSeqPanel().seqCanvas.cursorX = ranges
                    .getStartRes();
            alignPanel.getSeqPanel().seqCanvas.cursorY = ranges
                    .getStartSeq();
          }
          alignPanel.getSeqPanel().seqCanvas.repaint();
          break;

        case KeyEvent.VK_F1:
          try
          {
            Help.showHelpWindow();
          } catch (Exception ex)
          {
            ex.printStackTrace();
          }
          break;
        case KeyEvent.VK_H:
        {
          boolean toggleSeqs = !evt.isControlDown();
          boolean toggleCols = !evt.isShiftDown();
          toggleHiddenRegions(toggleSeqs, toggleCols);
          break;
        }
        case KeyEvent.VK_B:
        {
          boolean toggleSel = evt.isControlDown() || evt.isMetaDown();
          boolean modifyExisting = true; // always modify, don't clear
                                         // evt.isShiftDown();
          boolean invertHighlighted = evt.isAltDown();
          avc.markHighlightedColumns(invertHighlighted, modifyExisting,
                  toggleSel);
          break;
        }
        case KeyEvent.VK_PAGE_UP:
          viewport.getRanges().pageUp();
          break;
        case KeyEvent.VK_PAGE_DOWN:
          viewport.getRanges().pageDown();
          break;
        }
      }

      @Override
      public void keyReleased(KeyEvent evt)
      {
        switch (evt.getKeyCode())
        {
        case KeyEvent.VK_LEFT:
          if (evt.isAltDown() || !viewport.cursorMode)
          {
            viewport.firePropertyChange("alignment", null,
                    viewport.getAlignment().getSequences());
          }
          break;

        case KeyEvent.VK_RIGHT:
          if (evt.isAltDown() || !viewport.cursorMode)
          {
            viewport.firePropertyChange("alignment", null,
                    viewport.getAlignment().getSequences());
          }
          break;
        }
      }
    });
  }

  public void addAlignmentPanel(final AlignmentPanel ap, boolean newPanel)
  {
    ap.alignFrame = this;
    avc = new jalview.controller.AlignViewController(this, viewport,
            alignPanel);

    alignPanels.add(ap);

    PaintRefresher.Register(ap, ap.av.getSequenceSetId());

    int aSize = alignPanels.size();

    tabbedPane.setVisible(aSize > 1 || ap.av.getViewName() != null);

    if (aSize == 1 && ap.av.getViewName() == null)
    {
      this.getContentPane().add(ap, BorderLayout.CENTER);
    }
    else
    {
      if (aSize == 2)
      {
        setInitialTabVisible();
      }

      expandViews.setEnabled(true);
      gatherViews.setEnabled(true);
      tabbedPane.addTab(ap.av.getViewName(), ap);

      ap.setVisible(false);
    }

    if (newPanel)
    {
      if (ap.av.isPadGaps())
      {
        ap.av.getAlignment().padGaps();
      }
      ap.av.updateConservation(ap);
      ap.av.updateConsensus(ap);
      ap.av.updateStrucConsensus(ap);
    }
  }

  public void setInitialTabVisible()
  {
    expandViews.setEnabled(true);
    gatherViews.setEnabled(true);
    tabbedPane.setVisible(true);
    AlignmentPanel first = alignPanels.get(0);
    tabbedPane.addTab(first.av.getViewName(), first);
    this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
  }

  public AlignViewport getViewport()
  {
    return viewport;
  }

  /* Set up intrinsic listeners for dynamically generated GUI bits. */
  private void addServiceListeners()
  {
    final java.beans.PropertyChangeListener thisListener;
    Desktop.instance.addJalviewPropertyChangeListener("services",
            thisListener = new java.beans.PropertyChangeListener()
            {
              @Override
              public void propertyChange(PropertyChangeEvent evt)
              {
                // // System.out.println("Discoverer property change.");
                // if (evt.getPropertyName().equals("services"))
                {
                  SwingUtilities.invokeLater(new Runnable()
                  {

                    @Override
                    public void run()
                    {
                      System.err.println(
                              "Rebuild WS Menu for service change");
                      BuildWebServiceMenu();
                    }

                  });
                }
              }
            });
    addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosed(
              javax.swing.event.InternalFrameEvent evt)
      {
        // System.out.println("deregistering discoverer listener");
        Desktop.instance.removeJalviewPropertyChangeListener("services",
                thisListener);
        closeMenuItem_actionPerformed(true);
      }
    });
    // Finally, build the menu once to get current service state
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        BuildWebServiceMenu();
      }
    }).start();
  }

  /**
   * Configure menu items that vary according to whether the alignment is
   * nucleotide or protein
   */
  public void setGUINucleotide()
  {
    AlignmentI al = getViewport().getAlignment();
    boolean nucleotide = al.isNucleotide();

    loadVcf.setVisible(nucleotide);
    showTranslation.setVisible(nucleotide);
    showReverse.setVisible(nucleotide);
    showReverseComplement.setVisible(nucleotide);
    conservationMenuItem.setEnabled(!nucleotide);
    modifyConservation
            .setEnabled(!nucleotide && conservationMenuItem.isSelected());
    showGroupConservation.setEnabled(!nucleotide);

    showComplementMenuItem
            .setText(nucleotide ? MessageManager.getString("label.protein")
                    : MessageManager.getString("label.nucleotide"));
  }

  /**
   * set up menus for the current viewport. This may be called after any
   * operation that affects the data in the current view (selection changed,
   * etc) to update the menus to reflect the new state.
   */
  @Override
  public void setMenusForViewport()
  {
    setMenusFromViewport(viewport);
  }

  /**
   * Need to call this method when tabs are selected for multiple views, or when
   * loading from Jalview2XML.java
   * 
   * @param av
   *          AlignViewport
   */
  public void setMenusFromViewport(AlignViewport av)
  {
    padGapsMenuitem.setSelected(av.isPadGaps());
    colourTextMenuItem.setSelected(av.isShowColourText());
    abovePIDThreshold.setSelected(av.getAbovePIDThreshold());
    modifyPID.setEnabled(abovePIDThreshold.isSelected());
    conservationMenuItem.setSelected(av.getConservationSelected());
    modifyConservation.setEnabled(conservationMenuItem.isSelected());
    seqLimits.setSelected(av.getShowJVSuffix());
    idRightAlign.setSelected(av.isRightAlignIds());
    centreColumnLabelsMenuItem.setState(av.isCentreColumnLabels());
    renderGapsMenuItem.setSelected(av.isRenderGaps());
    wrapMenuItem.setSelected(av.getWrapAlignment());
    scaleAbove.setVisible(av.getWrapAlignment());
    scaleLeft.setVisible(av.getWrapAlignment());
    scaleRight.setVisible(av.getWrapAlignment());
    annotationPanelMenuItem.setState(av.isShowAnnotation());
    /*
     * Show/hide annotations only enabled if annotation panel is shown
     */
    showAllSeqAnnotations.setEnabled(annotationPanelMenuItem.getState());
    hideAllSeqAnnotations.setEnabled(annotationPanelMenuItem.getState());
    showAllAlAnnotations.setEnabled(annotationPanelMenuItem.getState());
    hideAllAlAnnotations.setEnabled(annotationPanelMenuItem.getState());
    viewBoxesMenuItem.setSelected(av.getShowBoxes());
    viewTextMenuItem.setSelected(av.getShowText());
    showNonconservedMenuItem.setSelected(av.getShowUnconserved());
    showGroupConsensus.setSelected(av.isShowGroupConsensus());
    showGroupConservation.setSelected(av.isShowGroupConservation());
    showConsensusHistogram.setSelected(av.isShowConsensusHistogram());
    showSequenceLogo.setSelected(av.isShowSequenceLogo());
    normaliseSequenceLogo.setSelected(av.isNormaliseSequenceLogo());

    ColourMenuHelper.setColourSelected(colourMenu,
            av.getGlobalColourScheme());

    showSeqFeatures.setSelected(av.isShowSequenceFeatures());
    hiddenMarkers.setState(av.getShowHiddenMarkers());
    applyToAllGroups.setState(av.getColourAppliesToAllGroups());
    showNpFeatsMenuitem.setSelected(av.isShowNPFeats());
    showDbRefsMenuitem.setSelected(av.isShowDBRefs());
    autoCalculate.setSelected(av.autoCalculateConsensus);
    sortByTree.setSelected(av.sortByTree);
    listenToViewSelections.setSelected(av.followSelection);

    showProducts.setEnabled(canShowProducts());
    setGroovyEnabled(Desktop.getGroovyConsole() != null);

    updateEditMenuBar();
  }

  /**
   * Set the enabled state of the 'Run Groovy' option in the Calculate menu
   * 
   * @param b
   */
  public void setGroovyEnabled(boolean b)
  {
    runGroovy.setEnabled(b);
  }

  private IProgressIndicator progressBar;

  /*
   * (non-Javadoc)
   * 
   * @see jalview.gui.IProgressIndicator#setProgressBar(java.lang.String, long)
   */
  @Override
  public void setProgressBar(String message, long id)
  {
    if (!Platform.isHeadless() && progressBar != null)
      progressBar.setProgressBar(message, id);
  }

  @Override
  public void registerHandler(final long id,
          final IProgressIndicatorHandler handler)
  {
    if (progressBar != null)
      progressBar.registerHandler(id, handler);
  }

  /**
   * 
   * @return true if any progress bars are still active
   */
  @Override
  public boolean operationInProgress()
  {
    return progressBar == null ? false : progressBar.operationInProgress();
  }

  /**
   * Sets the text of the status bar. Note that setting a null or empty value
   * will cause the status bar to be hidden, with possibly undesirable flicker
   * of the screen layout.
   */
  @Override
  public void setStatus(String text)
  {
    statusBar.setText(text == null || text.isEmpty() ? " " : text);
  }

  /*
   * Added so Castor Mapping file can obtain Jalview Version
   */
  public String getVersion()
  {
    return Cache.getProperty("VERSION");
  }

  public FeatureRenderer getFeatureRenderer()
  {
    return alignPanel.getSeqPanel().seqCanvas.getFeatureRenderer();
  }

  @Override
  public void fetchSequence_actionPerformed()
  {
    new SequenceFetcher(this);
  }

  @Override
  public void addFromFile_actionPerformed(ActionEvent e)
  {
    Desktop.instance.inputLocalFileMenuItem_actionPerformed(viewport);
  }

  @Override
  public void reload_actionPerformed(ActionEvent e)
  {
    if (fileName != null)
    {
      // TODO: JAL-1108 - ensure all associated frames are closed regardless of
      // originating file's format
      // TODO: work out how to recover feature settings for correct view(s) when
      // file is reloaded.
      if (FileFormat.Jalview.equals(currentFileFormat))
      {
        JInternalFrame[] frames = Desktop.desktop.getAllFrames();
        for (int i = 0; i < frames.length; i++)
        {
          if (frames[i] instanceof AlignFrame && frames[i] != this
                  && ((AlignFrame) frames[i]).fileName != null
                  && ((AlignFrame) frames[i]).fileName.equals(fileName))
          {
            try
            {
              frames[i].setSelected(true);
              Desktop.instance.closeAssociatedWindows();
            } catch (java.beans.PropertyVetoException ex)
            {
            }
          }

        }
        Desktop.instance.closeAssociatedWindows();

        FileLoader loader = new FileLoader();
        DataSourceType protocol = HttpUtils.startsWithHttpOrHttps(fileName)
                ? DataSourceType.URL
                : DataSourceType.FILE;
        loader.LoadFile(viewport, fileName, protocol, currentFileFormat);
      }
      else
      {
        Rectangle bounds = this.getBounds();

        FileLoader loader = new FileLoader();

        AlignFrame newframe = null;

        if (fileObject == null)
        {

          DataSourceType protocol = HttpUtils.startsWithHttpOrHttps(
                  fileName) ? DataSourceType.URL : DataSourceType.FILE;
          newframe = loader.LoadFileWaitTillLoaded(fileName, protocol,
                  currentFileFormat);
        }
        else
        {
          newframe = loader.LoadFileWaitTillLoaded(fileObject,
                  DataSourceType.FILE, currentFileFormat);
        }

        newframe.setBounds(bounds);
        if (featureSettings != null && featureSettings.isShowing())
        {
          final Rectangle fspos = featureSettings.frame.getBounds();
          // TODO: need a 'show feature settings' function that takes bounds -
          // need to refactor Desktop.addFrame
          newframe.featureSettings_actionPerformed(null);
          final FeatureSettings nfs = newframe.featureSettings;
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              nfs.frame.setBounds(fspos);
            }
          });
          this.featureSettings.close();
          this.featureSettings = null;
        }
        this.closeMenuItem_actionPerformed(true);
      }
    }
  }

  @Override
  public void addFromText_actionPerformed(ActionEvent e)
  {
    Desktop.instance
            .inputTextboxMenuItem_actionPerformed(viewport.getAlignPanel());
  }

  @Override
  public void addFromURL_actionPerformed(ActionEvent e)
  {
    Desktop.instance.inputURLMenuItem_actionPerformed(viewport);
  }

  @Override
  public void save_actionPerformed(ActionEvent e)
  {
    if (fileName == null || (currentFileFormat == null)
            || HttpUtils.startsWithHttpOrHttps(fileName))
    {
      saveAs_actionPerformed();
    }
    else
    {
      saveAlignment(fileName, currentFileFormat);
    }
  }

  /**
   * Saves the alignment to a file with a name chosen by the user, if necessary
   * warning if a file would be overwritten
   */
  @Override
  public void saveAs_actionPerformed()
  {
    String format = currentFileFormat == null ? null
            : currentFileFormat.getName();
    JalviewFileChooser chooser = JalviewFileChooser
            .forWrite(Cache.getProperty("LAST_DIRECTORY"), format);

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.save_alignment_to_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value != JalviewFileChooser.APPROVE_OPTION)
    {
      return;
    }
    currentFileFormat = chooser.getSelectedFormat();
    // todo is this (2005) test now obsolete - value is never null?
    while (currentFileFormat == null)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager
                      .getString("label.select_file_format_before_saving"),
              MessageManager.getString("label.file_format_not_specified"),
              JvOptionPane.WARNING_MESSAGE);
      currentFileFormat = chooser.getSelectedFormat();
      value = chooser.showSaveDialog(this);
      if (value != JalviewFileChooser.APPROVE_OPTION)
      {
        return;
      }
    }

    fileName = chooser.getSelectedFile().getPath();

    Cache.setProperty("DEFAULT_FILE_FORMAT", currentFileFormat.getName());
    Cache.setProperty("LAST_DIRECTORY", fileName);
    saveAlignment(fileName, currentFileFormat);
  }

  boolean lastSaveSuccessful = false;

  FileFormatI lastFormatSaved;

  String lastFilenameSaved;

  /**
   * Raise a dialog or status message for the last call to saveAlignment.
   *
   * @return true if last call to saveAlignment(file, format) was successful.
   */
  public boolean isSaveAlignmentSuccessful()
  {

    if (!lastSaveSuccessful)
    {
      if (!Platform.isHeadless())
      {
        JvOptionPane.showInternalMessageDialog(this, MessageManager
                .formatMessage("label.couldnt_save_file", new Object[]
                { lastFilenameSaved }),
                MessageManager.getString("label.error_saving_file"),
                JvOptionPane.WARNING_MESSAGE);
      }
      else
      {
        Console.error(MessageManager
                .formatMessage("label.couldnt_save_file", new Object[]
                { lastFilenameSaved }));
      }
    }
    else
    {

      setStatus(MessageManager.formatMessage(
              "label.successfully_saved_to_file_in_format", new Object[]
              { lastFilenameSaved, lastFormatSaved }));

    }
    return lastSaveSuccessful;
  }

  /**
   * Saves the alignment to the specified file path, in the specified format,
   * which may be an alignment format, or Jalview project format. If the
   * alignment has hidden regions, or the format is one capable of including
   * non-sequence data (features, annotations, groups), then the user may be
   * prompted to specify what to include in the output.
   * 
   * @param file
   * @param format
   */
  public void saveAlignment(String file, FileFormatI format)
  {
    lastSaveSuccessful = true;
    lastFilenameSaved = file;
    lastFormatSaved = format;

    if (FileFormat.Jalview.equals(format))
    {
      String shortName = title;
      if (shortName.indexOf(File.separatorChar) > -1)
      {
        shortName = shortName
                .substring(shortName.lastIndexOf(File.separatorChar) + 1);
      }
      lastSaveSuccessful = new Jalview2XML().saveAlignment(this, file,
              shortName);

      Console.debug("lastSaveSuccessful=" + lastSaveSuccessful);
      if (lastSaveSuccessful)
      {
        this.getViewport().setSavedUpToDate(true);
      }

      statusBar.setText(MessageManager.formatMessage(
              "label.successfully_saved_to_file_in_format", new Object[]
              { file, format }));

      return;
    }

    AlignExportSettingsI options = new AlignExportSettingsAdapter(false);
    Runnable cancelAction = () -> {
      lastSaveSuccessful = false;
    };
    Runnable outputAction = () -> {
      // todo defer this to inside formatSequences (or later)
      AlignmentExportData exportData = viewport.getAlignExportData(options);
      String output = new FormatAdapter(alignPanel, options)
              .formatSequences(format, exportData.getAlignment(),
                      exportData.getOmitHidden(),
                      exportData.getStartEndPostions(),
                      viewport.getAlignment().getHiddenColumns());
      if (output == null)
      {
        lastSaveSuccessful = false;
      }
      else
      {
        // create backupfiles object and get new temp filename destination
        boolean doBackup = BackupFiles.getEnabled();
        BackupFiles backupfiles = null;
        if (doBackup)
        {
          Console.trace("ALIGNFRAME making backupfiles object for " + file);
          backupfiles = new BackupFiles(file);
        }
        try
        {
          String tempFilePath = doBackup ? backupfiles.getTempFilePath()
                  : file;
          Console.trace("ALIGNFRAME setting PrintWriter");
          PrintWriter out = new PrintWriter(new FileWriter(tempFilePath));

          if (backupfiles != null)
          {
            Console.trace("ALIGNFRAME about to write to temp file "
                    + backupfiles.getTempFilePath());
          }

          out.print(output);
          Console.trace("ALIGNFRAME about to close file");
          out.close();
          Console.trace("ALIGNFRAME closed file");
          AlignFrame.this.setTitle(file);
          statusBar.setText(MessageManager.formatMessage(
                  "label.successfully_saved_to_file_in_format", new Object[]
                  { fileName, format.getName() }));
          lastSaveSuccessful = true;
        } catch (IOException e)
        {
          lastSaveSuccessful = false;
          Console.error(
                  "ALIGNFRAME Something happened writing the temp file");
          Console.error(e.getMessage());
          Console.debug(Cache.getStackTraceString(e));
        } catch (Exception ex)
        {
          lastSaveSuccessful = false;
          Console.error(
                  "ALIGNFRAME Something unexpected happened writing the temp file");
          Console.error(ex.getMessage());
          Console.debug(Cache.getStackTraceString(ex));
        }

        if (doBackup)
        {
          backupfiles.setWriteSuccess(lastSaveSuccessful);
          Console.debug("ALIGNFRAME writing temp file was "
                  + (lastSaveSuccessful ? "" : "NOT ") + "successful");
          // do the backup file roll and rename the temp file to actual file
          Console.trace("ALIGNFRAME about to rollBackupsAndRenameTempFile");
          lastSaveSuccessful = backupfiles.rollBackupsAndRenameTempFile();
          Console.debug("ALIGNFRAME performed rollBackupsAndRenameTempFile "
                  + (lastSaveSuccessful ? "" : "un") + "successfully");
        }

        Console.debug("lastSaveSuccessful=" + lastSaveSuccessful);
        if (lastSaveSuccessful)
        {
          AlignFrame.this.getViewport().setSavedUpToDate(true);
        }
      }
    };

    /*
     * show dialog with export options if applicable; else just do it
     */
    if (AlignExportOptions.isNeeded(viewport, format))
    {
      AlignExportOptions choices = new AlignExportOptions(
              alignPanel.getAlignViewport(), format, options);
      choices.setResponseAction(0, outputAction);
      choices.setResponseAction(1, cancelAction);
      choices.showDialog();
    }
    else
    {
      try
      {
        outputAction.run();
      } catch (Exception e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Outputs the alignment to textbox in the requested format, if necessary
   * first prompting the user for whether to include hidden regions or
   * non-sequence data
   * 
   * @param fileFormatName
   */
  @Override
  protected void outputText_actionPerformed(String fileFormatName)
  {
    FileFormatI fileFormat = FileFormats.getInstance()
            .forName(fileFormatName);
    AlignExportSettingsI options = new AlignExportSettingsAdapter(false);
    Runnable outputAction = () -> {
      // todo defer this to inside formatSequences (or later)
      AlignmentExportData exportData = viewport.getAlignExportData(options);
      CutAndPasteTransfer cap = new CutAndPasteTransfer();
      cap.setForInput(null);
      try
      {
        FileFormatI format = fileFormat;
        cap.setText(new FormatAdapter(alignPanel, options).formatSequences(
                format, exportData.getAlignment(),
                exportData.getOmitHidden(),
                exportData.getStartEndPostions(),
                viewport.getAlignment().getHiddenColumns()));
        Desktop.addInternalFrame(cap, MessageManager.formatMessage(
                "label.alignment_output_command", new Object[]
                { fileFormat.getName() }), 600, 500);
      } catch (OutOfMemoryError oom)
      {
        new OOMWarning("Outputting alignment as " + fileFormat.getName(),
                oom);
        cap.dispose();
      }
    };

    /*
     * show dialog with export options if applicable; else just do it
     */
    if (AlignExportOptions.isNeeded(viewport, fileFormat))
    {
      AlignExportOptions choices = new AlignExportOptions(
              alignPanel.getAlignViewport(), fileFormat, options);
      choices.setResponseAction(0, outputAction);
      choices.showDialog();
    }
    else
    {
      try
      {
        outputAction.run();
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void htmlMenuItem_actionPerformed(ActionEvent e)
  {
    HtmlSvgOutput htmlSVG = new HtmlSvgOutput(alignPanel);
    try {
      htmlSVG.exportHTML(null);
    } catch (ImageOutputException x) {
      // report problem to console and raise dialog
    }
  }

  @Override
  public void bioJSMenuItem_actionPerformed(ActionEvent e)
  {
    BioJsHTMLOutput bjs = new BioJsHTMLOutput(alignPanel);
    try {
    bjs.exportHTML(null);
  } catch (ImageOutputException x) {
    // report problem to console and raise dialog
  }
  }

  public void createImageMap(File file, String image)
  {
    try {
    alignPanel.makePNGImageMap(file, image);
    } catch (ImageOutputException x) {
      // report problem to console and raise dialog
    }
  }

  @Override
  public void createPNG_actionPerformed(ActionEvent e) {
    try{
      createPNG(null);
    } catch (ImageOutputException ioex)
    {
      // raise dialog, and report via console
    }
  }
  @Override
  public void createEPS_actionPerformed(ActionEvent e) {
    try{
      createEPS(null);
    } catch (ImageOutputException ioex)
    {
      // raise dialog, and report via console
    }
    
  }
  @Override
  public void createSVG_actionPerformed(ActionEvent e) {
    try{
      createSVG(null);
    } catch (ImageOutputException ioex)
    {
      // raise dialog, and report via console
    }
    
  }
  /**
   * Creates a PNG image of the alignment and writes it to the given file. If
   * the file is null, the user is prompted to choose a file.
   * 
   * @param f
   */
  public void createPNG(File f) throws ImageOutputException
  {
    createPNG(f, null, BitmapImageSizing.nullBitmapImageSizing());
  }

  public void createPNG(File f, String renderer, BitmapImageSizing userBis) throws ImageOutputException
  {
    alignPanel.makeAlignmentImage(TYPE.PNG, f, renderer, userBis);
  }

  /**
   * Creates an EPS image of the alignment and writes it to the given file. If
   * the file is null, the user is prompted to choose a file.
   * 
   * @param f
   */
  public void createEPS(File f)  throws ImageOutputException
  {
    createEPS(f, null);
  }

  public void createEPS(File f, String renderer) throws ImageOutputException
  {
    alignPanel.makeAlignmentImage(TYPE.EPS, f, renderer);
  }

  /**
   * Creates an SVG image of the alignment and writes it to the given file. If
   * the file is null, the user is prompted to choose a file.
   * 
   * @param f
   */
  public void createSVG(File f)  throws ImageOutputException
  {
    createSVG(f, null);
  }

  public void createSVG(File f, String renderer) throws ImageOutputException
  {
    alignPanel.makeAlignmentImage(TYPE.SVG, f, renderer);
  }

  @Override
  public void pageSetup_actionPerformed(ActionEvent e)
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    PrintThread.pf = printJob.pageDialog(printJob.defaultPage());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void printMenuItem_actionPerformed(ActionEvent e)
  {
    // Putting in a thread avoids Swing painting problems
    PrintThread thread = new PrintThread(alignPanel);
    thread.start();
  }

  @Override
  public void exportFeatures_actionPerformed(ActionEvent e)
  {
    new AnnotationExporter(alignPanel).exportFeatures();
  }

  @Override
  public void exportAnnotations_actionPerformed(ActionEvent e)
  {
    new AnnotationExporter(alignPanel).exportAnnotations();
  }

  @Override
  public void associatedData_actionPerformed(ActionEvent e)
  {
    final JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    String tooltip = MessageManager
            .getString("label.load_jalview_annotations");
    chooser.setDialogTitle(tooltip);
    chooser.setToolTipText(tooltip);
    chooser.setResponseHandler(0, () -> {
      String choice = chooser.getSelectedFile().getPath();
      Cache.setProperty("LAST_DIRECTORY", choice);
      loadJalviewDataFile(chooser.getSelectedFile(), null, null, null);
    });

    chooser.showOpenDialog(this);
  }

  /**
   * Close the current view or all views in the alignment frame. If the frame
   * only contains one view then the alignment will be removed from memory.
   * 
   * @param closeAllTabs
   */
  @Override
  public void closeMenuItem_actionPerformed(boolean closeAllTabs)
  {
    if (alignPanels != null && alignPanels.size() < 2)
    {
      closeAllTabs = true;
    }

    try
    {
      if (alignPanels != null)
      {
        if (closeAllTabs)
        {
          if (this.isClosed())
          {
            // really close all the windows - otherwise wait till
            // setClosed(true) is called
            for (int i = 0; i < alignPanels.size(); i++)
            {
              AlignmentPanel ap = alignPanels.get(i);
              ap.closePanel();
            }
          }
        }
        else
        {
          closeView(alignPanel);
        }
      }
      if (closeAllTabs)
      {
        if (featureSettings != null && featureSettings.isOpen())
        {
          featureSettings.close();
          featureSettings = null;
        }
        /*
         * this will raise an INTERNAL_FRAME_CLOSED event and this method will
         * be called recursively, with the frame now in 'closed' state
         */
        this.setClosed(true);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Close the specified panel and close up tabs appropriately.
   * 
   * @param panelToClose
   */
  public void closeView(AlignmentPanel panelToClose)
  {
    int index = tabbedPane.getSelectedIndex();
    int closedindex = tabbedPane.indexOfComponent(panelToClose);
    alignPanels.remove(panelToClose);
    panelToClose.closePanel();
    panelToClose = null;

    tabbedPane.removeTabAt(closedindex);
    tabbedPane.validate();

    if (index > closedindex || index == tabbedPane.getTabCount())
    {
      // modify currently selected tab index if necessary.
      index--;
    }

    this.tabSelectionChanged(index);
  }

  /**
   * DOCUMENT ME!
   */
  void updateEditMenuBar()
  {

    if (viewport.getHistoryList().size() > 0)
    {
      undoMenuItem.setEnabled(true);
      CommandI command = viewport.getHistoryList().peek();
      undoMenuItem.setText(MessageManager
              .formatMessage("label.undo_command", new Object[]
              { command.getDescription() }));
    }
    else
    {
      undoMenuItem.setEnabled(false);
      undoMenuItem.setText(MessageManager.getString("action.undo"));
    }

    if (viewport.getRedoList().size() > 0)
    {
      redoMenuItem.setEnabled(true);

      CommandI command = viewport.getRedoList().peek();
      redoMenuItem.setText(MessageManager
              .formatMessage("label.redo_command", new Object[]
              { command.getDescription() }));
    }
    else
    {
      redoMenuItem.setEnabled(false);
      redoMenuItem.setText(MessageManager.getString("action.redo"));
    }
  }

  @Override
  public void addHistoryItem(CommandI command)
  {
    if (command.getSize() > 0)
    {
      viewport.addToHistoryList(command);
      viewport.clearRedoList();
      updateEditMenuBar();
      viewport.updateHiddenColumns();
      // viewport.hasHiddenColumns = (viewport.getColumnSelection() != null
      // && viewport.getColumnSelection().getHiddenColumns() != null &&
      // viewport.getColumnSelection()
      // .getHiddenColumns().size() > 0);
    }
  }

  /**
   * 
   * @return alignment objects for all views
   */
  AlignmentI[] getViewAlignments()
  {
    if (alignPanels != null)
    {
      AlignmentI[] als = new AlignmentI[alignPanels.size()];
      int i = 0;
      for (AlignmentPanel ap : alignPanels)
      {
        als[i++] = ap.av.getAlignment();
      }
      return als;
    }
    if (viewport != null)
    {
      return new AlignmentI[] { viewport.getAlignment() };
    }
    return null;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void undoMenuItem_actionPerformed(ActionEvent e)
  {
    if (viewport.getHistoryList().isEmpty())
    {
      return;
    }
    CommandI command = viewport.getHistoryList().pop();
    viewport.addToRedoList(command);
    command.undoCommand(getViewAlignments());

    AlignmentViewport originalSource = getOriginatingSource(command);
    updateEditMenuBar();

    if (originalSource != null)
    {
      if (originalSource != viewport)
      {
        Console.warn(
                "Implementation worry: mismatch of viewport origin for undo");
      }
      originalSource.updateHiddenColumns();
      // originalSource.hasHiddenColumns = (viewport.getColumnSelection() !=
      // null
      // && viewport.getColumnSelection().getHiddenColumns() != null &&
      // viewport.getColumnSelection()
      // .getHiddenColumns().size() > 0);
      originalSource.firePropertyChange("alignment", null,
              originalSource.getAlignment().getSequences());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void redoMenuItem_actionPerformed(ActionEvent e)
  {
    if (viewport.getRedoList().size() < 1)
    {
      return;
    }

    CommandI command = viewport.getRedoList().pop();
    viewport.addToHistoryList(command);
    command.doCommand(getViewAlignments());

    AlignmentViewport originalSource = getOriginatingSource(command);
    updateEditMenuBar();

    if (originalSource != null)
    {

      if (originalSource != viewport)
      {
        Console.warn(
                "Implementation worry: mismatch of viewport origin for redo");
      }
      originalSource.updateHiddenColumns();
      // originalSource.hasHiddenColumns = (viewport.getColumnSelection() !=
      // null
      // && viewport.getColumnSelection().getHiddenColumns() != null &&
      // viewport.getColumnSelection()
      // .getHiddenColumns().size() > 0);
      originalSource.firePropertyChange("alignment", null,
              originalSource.getAlignment().getSequences());
    }
  }

  AlignmentViewport getOriginatingSource(CommandI command)
  {
    AlignmentViewport originalSource = null;
    // For sequence removal and addition, we need to fire
    // the property change event FROM the viewport where the
    // original alignment was altered
    AlignmentI al = null;
    if (command instanceof EditCommand)
    {
      EditCommand editCommand = (EditCommand) command;
      al = editCommand.getAlignment();
      List<Component> comps = PaintRefresher.components
              .get(viewport.getSequenceSetId());

      for (Component comp : comps)
      {
        if (comp instanceof AlignmentPanel)
        {
          if (al == ((AlignmentPanel) comp).av.getAlignment())
          {
            originalSource = ((AlignmentPanel) comp).av;
            break;
          }
        }
      }
    }

    if (originalSource == null)
    {
      // The original view is closed, we must validate
      // the current view against the closed view first
      if (al != null)
      {
        PaintRefresher.validateSequences(al, viewport.getAlignment());
      }

      originalSource = viewport;
    }

    return originalSource;
  }

  /**
   * Calls AlignmentI.moveSelectedSequencesByOne with current sequence selection
   * or the sequence under cursor in keyboard mode
   * 
   * @param up
   *          or down (if !up)
   */
  public void moveSelectedSequences(boolean up)
  {
    SequenceGroup sg = viewport.getSelectionGroup();

    if (sg == null)
    {
      if (viewport.cursorMode)
      {
        sg = new SequenceGroup();
        sg.addSequence(viewport.getAlignment().getSequenceAt(
                alignPanel.getSeqPanel().seqCanvas.cursorY), false);
      }
      else
      {
        return;
      }
    }

    if (sg.getSize() < 1)
    {
      return;
    }

    // TODO: JAL-3733 - add an event to the undo buffer for this !

    viewport.getAlignment().moveSelectedSequencesByOne(sg,
            viewport.getHiddenRepSequences(), up);
    alignPanel.paintAlignment(true, false);
  }

  synchronized void slideSequences(boolean right, int size)
  {
    List<SequenceI> sg = new ArrayList<>();
    if (viewport.cursorMode)
    {
      sg.add(viewport.getAlignment()
              .getSequenceAt(alignPanel.getSeqPanel().seqCanvas.cursorY));
    }
    else if (viewport.getSelectionGroup() != null
            && viewport.getSelectionGroup().getSize() != viewport
                    .getAlignment().getHeight())
    {
      sg = viewport.getSelectionGroup()
              .getSequences(viewport.getHiddenRepSequences());
    }

    if (sg.size() < 1)
    {
      return;
    }

    List<SequenceI> invertGroup = new ArrayList<>();

    for (SequenceI seq : viewport.getAlignment().getSequences())
    {
      if (!sg.contains(seq))
      {
        invertGroup.add(seq);
      }
    }

    SequenceI[] seqs1 = sg.toArray(new SequenceI[0]);

    SequenceI[] seqs2 = new SequenceI[invertGroup.size()];
    for (int i = 0; i < invertGroup.size(); i++)
    {
      seqs2[i] = invertGroup.get(i);
    }

    SlideSequencesCommand ssc;
    if (right)
    {
      ssc = new SlideSequencesCommand("Slide Sequences", seqs2, seqs1, size,
              viewport.getGapCharacter());
    }
    else
    {
      ssc = new SlideSequencesCommand("Slide Sequences", seqs1, seqs2, size,
              viewport.getGapCharacter());
    }

    int groupAdjustment = 0;
    if (ssc.getGapsInsertedBegin() && right)
    {
      if (viewport.cursorMode)
      {
        alignPanel.getSeqPanel().moveCursor(size, 0);
      }
      else
      {
        groupAdjustment = size;
      }
    }
    else if (!ssc.getGapsInsertedBegin() && !right)
    {
      if (viewport.cursorMode)
      {
        alignPanel.getSeqPanel().moveCursor(-size, 0);
      }
      else
      {
        groupAdjustment = -size;
      }
    }

    if (groupAdjustment != 0)
    {
      viewport.getSelectionGroup().setStartRes(
              viewport.getSelectionGroup().getStartRes() + groupAdjustment);
      viewport.getSelectionGroup().setEndRes(
              viewport.getSelectionGroup().getEndRes() + groupAdjustment);
    }

    /*
     * just extend the last slide command if compatible; but not if in
     * SplitFrame mode (to ensure all edits are broadcast - JAL-1802)
     */
    boolean appendHistoryItem = false;
    Deque<CommandI> historyList = viewport.getHistoryList();
    boolean inSplitFrame = getSplitViewContainer() != null;
    if (!inSplitFrame && historyList != null && historyList.size() > 0
            && historyList.peek() instanceof SlideSequencesCommand)
    {
      appendHistoryItem = ssc.appendSlideCommand(
              (SlideSequencesCommand) historyList.peek());
    }

    if (!appendHistoryItem)
    {
      addHistoryItem(ssc);
    }

    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void copy_actionPerformed()
  {
    if (viewport.getSelectionGroup() == null)
    {
      return;
    }
    // TODO: preserve the ordering of displayed alignment annotation in any
    // internal paste (particularly sequence associated annotation)
    SequenceI[] seqs = viewport.getSelectionAsNewSequence();
    String[] omitHidden = null;

    if (viewport.hasHiddenColumns())
    {
      omitHidden = viewport.getViewAsString(true);
    }

    String output = new FormatAdapter().formatSequences(FileFormat.Fasta,
            seqs, omitHidden, null);

    StringSelection ss = new StringSelection(output);

    try
    {
      jalview.gui.Desktop.internalCopy = true;
      // Its really worth setting the clipboard contents
      // to empty before setting the large StringSelection!!
      Toolkit.getDefaultToolkit().getSystemClipboard()
              .setContents(new StringSelection(""), null);

      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss,
              Desktop.instance);
    } catch (OutOfMemoryError er)
    {
      new OOMWarning("copying region", er);
      return;
    }

    HiddenColumns hiddenColumns = null;
    if (viewport.hasHiddenColumns())
    {
      int hiddenOffset = viewport.getSelectionGroup().getStartRes();
      int hiddenCutoff = viewport.getSelectionGroup().getEndRes();

      // create new HiddenColumns object with copy of hidden regions
      // between startRes and endRes, offset by startRes
      hiddenColumns = new HiddenColumns(
              viewport.getAlignment().getHiddenColumns(), hiddenOffset,
              hiddenCutoff, hiddenOffset);
    }

    Desktop.jalviewClipboard = new Object[] { seqs,
        viewport.getAlignment().getDataset(), hiddenColumns };
    setStatus(MessageManager.formatMessage(
            "label.copied_sequences_to_clipboard", new Object[]
            { Integer.valueOf(seqs.length).toString() }));
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void pasteNew_actionPerformed(ActionEvent e)
  {
    paste(true);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void pasteThis_actionPerformed(ActionEvent e)
  {
    paste(false);
  }

  /**
   * Paste contents of Jalview clipboard
   * 
   * @param newAlignment
   *          true to paste to a new alignment, otherwise add to this.
   */
  void paste(boolean newAlignment)
  {
    boolean externalPaste = true;
    try
    {
      Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable contents = c.getContents(this);

      if (contents == null)
      {
        return;
      }

      String str;
      FileFormatI format;
      try
      {
        str = (String) contents.getTransferData(DataFlavor.stringFlavor);
        if (str.length() < 1)
        {
          return;
        }

        format = new IdentifyFile().identify(str, DataSourceType.PASTE);

      } catch (OutOfMemoryError er)
      {
        new OOMWarning("Out of memory pasting sequences!!", er);
        return;
      }

      SequenceI[] sequences;
      boolean annotationAdded = false;
      AlignmentI alignment = null;

      if (Desktop.jalviewClipboard != null)
      {
        // The clipboard was filled from within Jalview, we must use the
        // sequences
        // And dataset from the copied alignment
        SequenceI[] newseq = (SequenceI[]) Desktop.jalviewClipboard[0];
        // be doubly sure that we create *new* sequence objects.
        sequences = new SequenceI[newseq.length];
        for (int i = 0; i < newseq.length; i++)
        {
          sequences[i] = new Sequence(newseq[i]);
        }
        alignment = new Alignment(sequences);
        externalPaste = false;
      }
      else
      {
        // parse the clipboard as an alignment.
        alignment = new FormatAdapter().readFile(str, DataSourceType.PASTE,
                format);
        sequences = alignment.getSequencesArray();
      }

      int alwidth = 0;
      ArrayList<Integer> newGraphGroups = new ArrayList<>();
      int fgroup = -1;

      if (newAlignment)
      {

        if (Desktop.jalviewClipboard != null)
        {
          // dataset is inherited
          alignment.setDataset((Alignment) Desktop.jalviewClipboard[1]);
        }
        else
        {
          // new dataset is constructed
          alignment.setDataset(null);
        }
        alwidth = alignment.getWidth() + 1;
      }
      else
      {
        AlignmentI pastedal = alignment; // preserve pasted alignment object
        // Add pasted sequences and dataset into existing alignment.
        alignment = viewport.getAlignment();
        alwidth = alignment.getWidth() + 1;
        // decide if we need to import sequences from an existing dataset
        boolean importDs = Desktop.jalviewClipboard != null
                && Desktop.jalviewClipboard[1] != alignment.getDataset();
        // importDs==true instructs us to copy over new dataset sequences from
        // an existing alignment
        Vector<SequenceI> newDs = (importDs) ? new Vector<>() : null; // used to
                                                                      // create
        // minimum dataset set

        for (int i = 0; i < sequences.length; i++)
        {
          if (importDs)
          {
            newDs.addElement(null);
          }
          SequenceI ds = sequences[i].getDatasetSequence(); // null for a simple
          // paste
          if (importDs && ds != null)
          {
            if (!newDs.contains(ds))
            {
              newDs.setElementAt(ds, i);
              ds = new Sequence(ds);
              // update with new dataset sequence
              sequences[i].setDatasetSequence(ds);
            }
            else
            {
              ds = sequences[newDs.indexOf(ds)].getDatasetSequence();
            }
          }
          else
          {
            // copy and derive new dataset sequence
            sequences[i] = sequences[i].deriveSequence();
            alignment.getDataset()
                    .addSequence(sequences[i].getDatasetSequence());
            // TODO: avoid creation of duplicate dataset sequences with a
            // 'contains' method using SequenceI.equals()/SequenceI.contains()
          }
          alignment.addSequence(sequences[i]); // merges dataset
        }
        if (newDs != null)
        {
          newDs.clear(); // tidy up
        }
        if (alignment.getAlignmentAnnotation() != null)
        {
          for (AlignmentAnnotation alan : alignment
                  .getAlignmentAnnotation())
          {
            if (alan.graphGroup > fgroup)
            {
              fgroup = alan.graphGroup;
            }
          }
        }
        if (pastedal.getAlignmentAnnotation() != null)
        {
          // Add any annotation attached to alignment.
          AlignmentAnnotation[] alann = pastedal.getAlignmentAnnotation();
          for (int i = 0; i < alann.length; i++)
          {
            annotationAdded = true;
            if (alann[i].sequenceRef == null && !alann[i].autoCalculated)
            {
              AlignmentAnnotation newann = new AlignmentAnnotation(
                      alann[i]);
              if (newann.graphGroup > -1)
              {
                if (newGraphGroups.size() <= newann.graphGroup
                        || newGraphGroups.get(newann.graphGroup) == null)
                {
                  for (int q = newGraphGroups
                          .size(); q <= newann.graphGroup; q++)
                  {
                    newGraphGroups.add(q, null);
                  }
                  newGraphGroups.set(newann.graphGroup,
                          Integer.valueOf(++fgroup));
                }
                newann.graphGroup = newGraphGroups.get(newann.graphGroup)
                        .intValue();
              }

              newann.padAnnotation(alwidth);
              alignment.addAnnotation(newann);
            }
          }
        }
      }
      if (!newAlignment)
      {
        // /////
        // ADD HISTORY ITEM
        //
        addHistoryItem(new EditCommand(
                MessageManager.getString("label.add_sequences"),
                Action.PASTE, sequences, 0, alignment.getWidth(),
                alignment));
      }
      // Add any annotations attached to sequences
      for (int i = 0; i < sequences.length; i++)
      {
        if (sequences[i].getAnnotation() != null)
        {
          AlignmentAnnotation newann;
          for (int a = 0; a < sequences[i].getAnnotation().length; a++)
          {
            annotationAdded = true;
            newann = sequences[i].getAnnotation()[a];
            newann.adjustForAlignment();
            newann.padAnnotation(alwidth);
            if (newann.graphGroup > -1)
            {
              if (newann.graphGroup > -1)
              {
                if (newGraphGroups.size() <= newann.graphGroup
                        || newGraphGroups.get(newann.graphGroup) == null)
                {
                  for (int q = newGraphGroups
                          .size(); q <= newann.graphGroup; q++)
                  {
                    newGraphGroups.add(q, null);
                  }
                  newGraphGroups.set(newann.graphGroup,
                          Integer.valueOf(++fgroup));
                }
                newann.graphGroup = newGraphGroups.get(newann.graphGroup)
                        .intValue();
              }
            }
            // annotation was duplicated earlier
            alignment.addAnnotation(sequences[i].getAnnotation()[a]);
            // take care of contact matrix too
            ContactMatrixI cm=sequences[i].getContactMatrixFor(sequences[i].getAnnotation()[a]);
            if (cm!=null)
            {
              alignment.addContactListFor(sequences[i].getAnnotation()[a], cm);
            }
            
            alignment.setAnnotationIndex(sequences[i].getAnnotation()[a],
                    a);
          }
        }
      }
      if (!newAlignment)
      {

        // propagate alignment changed.
        viewport.getRanges().setEndSeq(alignment.getHeight() - 1);
        if (annotationAdded)
        {
          // Duplicate sequence annotation in all views.
          AlignmentI[] alview = this.getViewAlignments();
          for (int i = 0; i < sequences.length; i++)
          {
            AlignmentAnnotation sann[] = sequences[i].getAnnotation();
            if (sann == null)
            {
              continue;
            }
            for (int avnum = 0; avnum < alview.length; avnum++)
            {
              if (alview[avnum] != alignment)
              {
                // duplicate in a view other than the one with input focus
                int avwidth = alview[avnum].getWidth() + 1;
                // this relies on sann being preserved after we
                // modify the sequence's annotation array for each duplication
                for (int a = 0; a < sann.length; a++)
                {
                  AlignmentAnnotation newann = new AlignmentAnnotation(
                          sann[a]);
                  sequences[i].addAlignmentAnnotation(newann);
                  newann.padAnnotation(avwidth);
                  alview[avnum].addAnnotation(newann); // annotation was
                  // duplicated earlier
                  // TODO JAL-1145 graphGroups are not updated for sequence
                  // annotation added to several views. This may cause
                  // strangeness
                  alview[avnum].setAnnotationIndex(newann, a);
                }
              }
            }
          }
          buildSortByAnnotationScoresMenu();
        }
        viewport.firePropertyChange("alignment", null,
                alignment.getSequences());
        if (alignPanels != null)
        {
          for (AlignmentPanel ap : alignPanels)
          {
            ap.validateAnnotationDimensions(false);
          }
        }
        else
        {
          alignPanel.validateAnnotationDimensions(false);
        }

      }
      else
      {
        AlignFrame af = new AlignFrame(alignment, DEFAULT_WIDTH,
                DEFAULT_HEIGHT);
        String newtitle = new String("Copied sequences");

        if (Desktop.jalviewClipboard != null
                && Desktop.jalviewClipboard[2] != null)
        {
          HiddenColumns hc = (HiddenColumns) Desktop.jalviewClipboard[2];
          af.viewport.setHiddenColumns(hc);
        }

        // >>>This is a fix for the moment, until a better solution is
        // found!!<<<
        af.alignPanel.getSeqPanel().seqCanvas.getFeatureRenderer()
                .transferSettings(alignPanel.getSeqPanel().seqCanvas
                        .getFeatureRenderer());

        // TODO: maintain provenance of an alignment, rather than just make the
        // title a concatenation of operations.
        if (!externalPaste)
        {
          if (title.startsWith("Copied sequences"))
          {
            newtitle = title;
          }
          else
          {
            newtitle = newtitle.concat("- from " + title);
          }
        }
        else
        {
          newtitle = new String("Pasted sequences");
        }

        Desktop.addInternalFrame(af, newtitle, DEFAULT_WIDTH,
                DEFAULT_HEIGHT);

      }

    } catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println("Exception whilst pasting: " + ex);
      // could be anything being pasted in here
    }

  }

  @Override
  protected void expand_newalign(ActionEvent e)
  {
    try
    {
      AlignmentI alignment = AlignmentUtils
              .expandContext(getViewport().getAlignment(), -1);
      AlignFrame af = new AlignFrame(alignment, DEFAULT_WIDTH,
              DEFAULT_HEIGHT);
      String newtitle = new String("Flanking alignment");

      if (Desktop.jalviewClipboard != null
              && Desktop.jalviewClipboard[2] != null)
      {
        HiddenColumns hc = (HiddenColumns) Desktop.jalviewClipboard[2];
        af.viewport.setHiddenColumns(hc);
      }

      // >>>This is a fix for the moment, until a better solution is
      // found!!<<<
      af.alignPanel.getSeqPanel().seqCanvas.getFeatureRenderer()
              .transferSettings(alignPanel.getSeqPanel().seqCanvas
                      .getFeatureRenderer());

      // TODO: maintain provenance of an alignment, rather than just make the
      // title a concatenation of operations.
      {
        if (title.startsWith("Copied sequences"))
        {
          newtitle = title;
        }
        else
        {
          newtitle = newtitle.concat("- from " + title);
        }
      }

      Desktop.addInternalFrame(af, newtitle, DEFAULT_WIDTH, DEFAULT_HEIGHT);

    } catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println("Exception whilst pasting: " + ex);
      // could be anything being pasted in here
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("Viewing flanking region of alignment", oom);
    }
  }

  /**
   * Action Cut (delete and copy) the selected region
   */
  @Override
  protected void cut_actionPerformed()
  {
    copy_actionPerformed();
    delete_actionPerformed();
  }

  /**
   * Performs menu option to Delete the currently selected region
   */
  @Override
  protected void delete_actionPerformed()
  {

    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg == null)
    {
      return;
    }

    Runnable okAction = () -> {
      SequenceI[] cut = sg.getSequences()
              .toArray(new SequenceI[sg.getSize()]);

      addHistoryItem(new EditCommand(
              MessageManager.getString("label.cut_sequences"), Action.CUT,
              cut, sg.getStartRes(), sg.getEndRes() - sg.getStartRes() + 1,
              viewport.getAlignment()));

      viewport.setSelectionGroup(null);
      viewport.sendSelection();
      viewport.getAlignment().deleteGroup(sg);

      viewport.firePropertyChange("alignment", null,
              viewport.getAlignment().getSequences());
      if (viewport.getAlignment().getHeight() < 1)
      {
        try
        {
          AlignFrame.this.setClosed(true);
        } catch (Exception ex)
        {
        }
      }
    };

    /*
     * If the cut affects all sequences, prompt for confirmation
     */
    boolean wholeHeight = sg.getSize() == viewport.getAlignment()
            .getHeight();
    boolean wholeWidth = (((sg.getEndRes() - sg.getStartRes())
            + 1) == viewport.getAlignment().getWidth()) ? true : false;
    if (wholeHeight && wholeWidth)
    {
      JvOptionPane dialog = JvOptionPane.newOptionDialog(Desktop.desktop);
      dialog.setResponseHandler(0, okAction); // 0 = OK_OPTION
      Object[] options = new Object[] {
          MessageManager.getString("action.ok"),
          MessageManager.getString("action.cancel") };
      dialog.showDialog(MessageManager.getString("warn.delete_all"),
              MessageManager.getString("label.delete_all"),
              JvOptionPane.DEFAULT_OPTION, JvOptionPane.PLAIN_MESSAGE, null,
              options, options[0]);
    }
    else
    {
      try
      {
        okAction.run();
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void deleteGroups_actionPerformed(ActionEvent e)
  {
    if (avc.deleteGroups())
    {
      PaintRefresher.Refresh(this, viewport.getSequenceSetId());
      alignPanel.updateAnnotation();
      alignPanel.paintAlignment(true, true);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void selectAllSequenceMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceGroup sg = new SequenceGroup(
            viewport.getAlignment().getSequences());

    sg.setEndRes(viewport.getAlignment().getWidth() - 1);
    viewport.setSelectionGroup(sg);
    viewport.isSelectionGroupChanged(true);
    viewport.sendSelection();
    // JAL-2034 - should delegate to
    // alignPanel to decide if overview needs
    // updating.
    alignPanel.paintAlignment(false, false);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void deselectAllSequenceMenuItem_actionPerformed(ActionEvent e)
  {
    if (viewport.cursorMode)
    {
      alignPanel.getSeqPanel().keyboardNo1 = null;
      alignPanel.getSeqPanel().keyboardNo2 = null;
    }
    viewport.setSelectionGroup(null);
    viewport.getColumnSelection().clear();
    viewport.setSearchResults(null);
    alignPanel.getIdPanel().getIdCanvas().searchResults = null;
    // JAL-2034 - should delegate to
    // alignPanel to decide if overview needs
    // updating.
    alignPanel.paintAlignment(false, false);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
    viewport.sendSelection();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void invertSequenceMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceGroup sg = viewport.getSelectionGroup();

    if (sg == null)
    {
      selectAllSequenceMenuItem_actionPerformed(null);

      return;
    }

    for (int i = 0; i < viewport.getAlignment().getSequences().size(); i++)
    {
      sg.addOrRemove(viewport.getAlignment().getSequenceAt(i), false);
    }
    // JAL-2034 - should delegate to
    // alignPanel to decide if overview needs
    // updating.

    alignPanel.paintAlignment(true, false);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
    viewport.sendSelection();
  }

  @Override
  public void invertColSel_actionPerformed(ActionEvent e)
  {
    viewport.invertColumnSelection();
    alignPanel.paintAlignment(true, false);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
    viewport.sendSelection();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void remove2LeftMenuItem_actionPerformed(ActionEvent e)
  {
    trimAlignment(true);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void remove2RightMenuItem_actionPerformed(ActionEvent e)
  {
    trimAlignment(false);
  }

  void trimAlignment(boolean trimLeft)
  {
    ColumnSelection colSel = viewport.getColumnSelection();
    int column;

    if (!colSel.isEmpty())
    {
      if (trimLeft)
      {
        column = colSel.getMin();
      }
      else
      {
        column = colSel.getMax();
      }

      SequenceI[] seqs;
      if (viewport.getSelectionGroup() != null)
      {
        seqs = viewport.getSelectionGroup()
                .getSequencesAsArray(viewport.getHiddenRepSequences());
      }
      else
      {
        seqs = viewport.getAlignment().getSequencesArray();
      }

      TrimRegionCommand trimRegion;
      if (trimLeft)
      {
        trimRegion = new TrimRegionCommand("Remove Left", true, seqs,
                column, viewport.getAlignment());
        viewport.getRanges().setStartRes(0);
      }
      else
      {
        trimRegion = new TrimRegionCommand("Remove Right", false, seqs,
                column, viewport.getAlignment());
      }

      setStatus(MessageManager.formatMessage("label.removed_columns",
              new String[]
              { Integer.valueOf(trimRegion.getSize()).toString() }));

      addHistoryItem(trimRegion);

      for (SequenceGroup sg : viewport.getAlignment().getGroups())
      {
        if ((trimLeft && !sg.adjustForRemoveLeft(column))
                || (!trimLeft && !sg.adjustForRemoveRight(column)))
        {
          viewport.getAlignment().deleteGroup(sg);
        }
      }

      viewport.firePropertyChange("alignment", null,
              viewport.getAlignment().getSequences());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void removeGappedColumnMenuItem_actionPerformed(ActionEvent e)
  {
    int start = 0, end = viewport.getAlignment().getWidth() - 1;

    SequenceI[] seqs;
    if (viewport.getSelectionGroup() != null)
    {
      seqs = viewport.getSelectionGroup()
              .getSequencesAsArray(viewport.getHiddenRepSequences());
      start = viewport.getSelectionGroup().getStartRes();
      end = viewport.getSelectionGroup().getEndRes();
    }
    else
    {
      seqs = viewport.getAlignment().getSequencesArray();
    }

    RemoveGapColCommand removeGapCols = new RemoveGapColCommand(
            "Remove Gapped Columns", seqs, start, end,
            viewport.getAlignment());

    addHistoryItem(removeGapCols);

    setStatus(MessageManager.formatMessage("label.removed_empty_columns",
            new Object[]
            { Integer.valueOf(removeGapCols.getSize()).toString() }));

    // This is to maintain viewport position on first residue
    // of first sequence
    SequenceI seq = viewport.getAlignment().getSequenceAt(0);
    ViewportRanges ranges = viewport.getRanges();
    int startRes = seq.findPosition(ranges.getStartRes());
    // ShiftList shifts;
    // viewport.getAlignment().removeGaps(shifts=new ShiftList());
    // edit.alColumnChanges=shifts.getInverse();
    // if (viewport.hasHiddenColumns)
    // viewport.getColumnSelection().compensateForEdits(shifts);
    ranges.setStartRes(seq.findIndex(startRes) - 1);
    viewport.firePropertyChange("alignment", null,
            viewport.getAlignment().getSequences());

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void removeAllGapsMenuItem_actionPerformed(ActionEvent e)
  {
    int start = 0, end = viewport.getAlignment().getWidth() - 1;

    SequenceI[] seqs;
    if (viewport.getSelectionGroup() != null)
    {
      seqs = viewport.getSelectionGroup()
              .getSequencesAsArray(viewport.getHiddenRepSequences());
      start = viewport.getSelectionGroup().getStartRes();
      end = viewport.getSelectionGroup().getEndRes();
    }
    else
    {
      seqs = viewport.getAlignment().getSequencesArray();
    }

    // This is to maintain viewport position on first residue
    // of first sequence
    SequenceI seq = viewport.getAlignment().getSequenceAt(0);
    int startRes = seq.findPosition(viewport.getRanges().getStartRes());

    addHistoryItem(new RemoveGapsCommand("Remove Gaps", seqs, start, end,
            viewport.getAlignment()));

    viewport.getRanges().setStartRes(seq.findIndex(startRes) - 1);

    viewport.firePropertyChange("alignment", null,
            viewport.getAlignment().getSequences());

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void padGapsMenuitem_actionPerformed(ActionEvent e)
  {
    viewport.setPadGaps(padGapsMenuitem.isSelected());
    viewport.firePropertyChange("alignment", null,
            viewport.getAlignment().getSequences());
  }

  /**
   * Opens a Finder dialog
   * 
   * @param e
   */
  @Override
  public void findMenuItem_actionPerformed(ActionEvent e)
  {
    new Finder(alignPanel, false, null);
  }

  /**
   * Create a new view of the current alignment.
   */
  @Override
  public void newView_actionPerformed(ActionEvent e)
  {
    newView(null, true);
  }

  /**
   * Creates and shows a new view of the current alignment.
   * 
   * @param viewTitle
   *          title of newly created view; if null, one will be generated
   * @param copyAnnotation
   *          if true then duplicate all annnotation, groups and settings
   * @return new alignment panel, already displayed.
   */
  public AlignmentPanel newView(String viewTitle, boolean copyAnnotation)
  {
    /*
     * Create a new AlignmentPanel (with its own, new Viewport)
     */
    AlignmentPanel newap = new jalview.project.Jalview2XML()
            .copyAlignPanel(alignPanel);
    if (!copyAnnotation)
    {
      /*
       * remove all groups and annotation except for the automatic stuff
       */
      newap.av.getAlignment().deleteAllGroups();
      newap.av.getAlignment().deleteAllAnnotations(false);
    }

    newap.av.setGatherViewsHere(false);

    if (viewport.getViewName() == null)
    {
      viewport.setViewName(
              MessageManager.getString("label.view_name_original"));
    }

    /*
     * Views share the same edits undo and redo stacks
     */
    newap.av.setHistoryList(viewport.getHistoryList());
    newap.av.setRedoList(viewport.getRedoList());

    /*
     * copy any visualisation settings that are not saved in the project
     */
    newap.av.setColourAppliesToAllGroups(
            viewport.getColourAppliesToAllGroups());

    /*
     * Views share the same mappings; need to deregister any new mappings
     * created by copyAlignPanel, and register the new reference to the shared
     * mappings
     */
    newap.av.replaceMappings(viewport.getAlignment());

    /*
     * start up cDNA consensus (if applicable) now mappings are in place
     */
    if (newap.av.initComplementConsensus())
    {
      newap.refresh(true); // adjust layout of annotations
    }

    newap.av.setViewName(getNewViewName(viewTitle));

    addAlignmentPanel(newap, true);
    newap.alignmentChanged();

    if (alignPanels.size() == 2)
    {
      viewport.setGatherViewsHere(true);
    }
    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

    return newap;
  }

  /**
   * Make a new name for the view, ensuring it is unique within the current
   * sequenceSetId. (This used to be essential for Jalview Project archives, but
   * these now use viewId. Unique view names are still desirable for usability.)
   * 
   * @param viewTitle
   * @return
   */
  protected String getNewViewName(String viewTitle)
  {
    int index = Desktop.getViewCount(viewport.getSequenceSetId());
    boolean addFirstIndex = false;
    if (viewTitle == null || viewTitle.trim().length() == 0)
    {
      viewTitle = MessageManager.getString("action.view");
      addFirstIndex = true;
    }
    else
    {
      index = 1;// we count from 1 if given a specific name
    }
    String newViewName = viewTitle + ((addFirstIndex) ? " " + index : "");

    List<Component> comps = PaintRefresher.components
            .get(viewport.getSequenceSetId());

    List<String> existingNames = getExistingViewNames(comps);

    while (existingNames.contains(newViewName))
    {
      newViewName = viewTitle + " " + (++index);
    }
    return newViewName;
  }

  /**
   * Returns a list of distinct view names found in the given list of
   * components. View names are held on the viewport of an AlignmentPanel.
   * 
   * @param comps
   * @return
   */
  protected List<String> getExistingViewNames(List<Component> comps)
  {
    List<String> existingNames = new ArrayList<>();
    for (Component comp : comps)
    {
      if (comp instanceof AlignmentPanel)
      {
        AlignmentPanel ap = (AlignmentPanel) comp;
        if (!existingNames.contains(ap.av.getViewName()))
        {
          existingNames.add(ap.av.getViewName());
        }
      }
    }
    return existingNames;
  }

  /**
   * Explode tabbed views into separate windows.
   */
  @Override
  public void expandViews_actionPerformed(ActionEvent e)
  {
    Desktop.explodeViews(this);
  }

  /**
   * Gather views in separate windows back into a tabbed presentation.
   */
  @Override
  public void gatherViews_actionPerformed(ActionEvent e)
  {
    Desktop.instance.gatherViews(this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void font_actionPerformed(ActionEvent e)
  {
    new FontChooser(alignPanel);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void seqLimit_actionPerformed(ActionEvent e)
  {
    viewport.setShowJVSuffix(seqLimits.isSelected());

    alignPanel.getIdPanel().getIdCanvas()
            .setPreferredSize(alignPanel.calculateIdWidth());
    alignPanel.paintAlignment(true, false);
  }

  @Override
  public void idRightAlign_actionPerformed(ActionEvent e)
  {
    viewport.setRightAlignIds(idRightAlign.isSelected());
    alignPanel.paintAlignment(false, false);
  }

  @Override
  public void centreColumnLabels_actionPerformed(ActionEvent e)
  {
    viewport.setCentreColumnLabels(centreColumnLabelsMenuItem.getState());
    alignPanel.paintAlignment(false, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.jbgui.GAlignFrame#followHighlight_actionPerformed()
   */
  @Override
  protected void followHighlight_actionPerformed()
  {
    /*
     * Set the 'follow' flag on the Viewport (and scroll to position if now
     * true).
     */
    final boolean state = this.followHighlightMenuItem.getState();
    viewport.setFollowHighlight(state);
    if (state)
    {
      alignPanel.scrollToPosition(viewport.getSearchResults());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void colourTextMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setColourText(colourTextMenuItem.isSelected());
    alignPanel.paintAlignment(false, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void wrapMenuItem_actionPerformed(ActionEvent e)
  {
    scaleAbove.setVisible(wrapMenuItem.isSelected());
    scaleLeft.setVisible(wrapMenuItem.isSelected());
    scaleRight.setVisible(wrapMenuItem.isSelected());
    viewport.setWrapAlignment(wrapMenuItem.isSelected());
    alignPanel.updateLayout();
  }

  @Override
  public void showAllSeqs_actionPerformed(ActionEvent e)
  {
    viewport.showAllHiddenSeqs();
  }

  @Override
  public void showAllColumns_actionPerformed(ActionEvent e)
  {
    viewport.showAllHiddenColumns();
    alignPanel.paintAlignment(true, true);
    viewport.sendSelection();
  }

  @Override
  public void hideSelSequences_actionPerformed(ActionEvent e)
  {
    viewport.hideAllSelectedSeqs();
  }

  /**
   * called by key handler and the hide all/show all menu items
   * 
   * @param toggleSeqs
   * @param toggleCols
   */
  protected void toggleHiddenRegions(boolean toggleSeqs, boolean toggleCols)
  {

    boolean hide = false;
    SequenceGroup sg = viewport.getSelectionGroup();
    if (!toggleSeqs && !toggleCols)
    {
      // Hide everything by the current selection - this is a hack - we do the
      // invert and then hide
      // first check that there will be visible columns after the invert.
      if (viewport.hasSelectedColumns() || (sg != null && sg.getSize() > 0
              && sg.getStartRes() <= sg.getEndRes()))
      {
        // now invert the sequence set, if required - empty selection implies
        // that no hiding is required.
        if (sg != null)
        {
          invertSequenceMenuItem_actionPerformed(null);
          sg = viewport.getSelectionGroup();
          toggleSeqs = true;

        }
        viewport.expandColSelection(sg, true);
        // finally invert the column selection and get the new sequence
        // selection.
        invertColSel_actionPerformed(null);
        toggleCols = true;
      }
    }

    if (toggleSeqs)
    {
      if (sg != null && sg.getSize() != viewport.getAlignment().getHeight())
      {
        hideSelSequences_actionPerformed(null);
        hide = true;
      }
      else if (!(toggleCols && viewport.hasSelectedColumns()))
      {
        showAllSeqs_actionPerformed(null);
      }
    }

    if (toggleCols)
    {
      if (viewport.hasSelectedColumns())
      {
        hideSelColumns_actionPerformed(null);
        if (!toggleSeqs)
        {
          viewport.setSelectionGroup(sg);
        }
      }
      else if (!hide)
      {
        showAllColumns_actionPerformed(null);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#hideAllButSelection_actionPerformed(java.awt.
   * event.ActionEvent)
   */
  @Override
  public void hideAllButSelection_actionPerformed(ActionEvent e)
  {
    toggleHiddenRegions(false, false);
    viewport.sendSelection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#hideAllSelection_actionPerformed(java.awt.event
   * .ActionEvent)
   */
  @Override
  public void hideAllSelection_actionPerformed(ActionEvent e)
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    viewport.expandColSelection(sg, false);
    viewport.hideAllSelectedSeqs();
    viewport.hideSelectedColumns();
    alignPanel.updateLayout();
    alignPanel.paintAlignment(true, true);
    viewport.sendSelection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#showAllhidden_actionPerformed(java.awt.event.
   * ActionEvent)
   */
  @Override
  public void showAllhidden_actionPerformed(ActionEvent e)
  {
    viewport.showAllHiddenColumns();
    viewport.showAllHiddenSeqs();
    alignPanel.paintAlignment(true, true);
    viewport.sendSelection();
  }

  @Override
  public void hideSelColumns_actionPerformed(ActionEvent e)
  {
    viewport.hideSelectedColumns();
    alignPanel.updateLayout();
    alignPanel.paintAlignment(true, true);
    viewport.sendSelection();
  }

  @Override
  public void hiddenMarkers_actionPerformed(ActionEvent e)
  {
    viewport.setShowHiddenMarkers(hiddenMarkers.isSelected());
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void scaleAbove_actionPerformed(ActionEvent e)
  {
    viewport.setScaleAboveWrapped(scaleAbove.isSelected());
    alignPanel.updateLayout();
    alignPanel.paintAlignment(true, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void scaleLeft_actionPerformed(ActionEvent e)
  {
    viewport.setScaleLeftWrapped(scaleLeft.isSelected());
    alignPanel.updateLayout();
    alignPanel.paintAlignment(true, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void scaleRight_actionPerformed(ActionEvent e)
  {
    viewport.setScaleRightWrapped(scaleRight.isSelected());
    alignPanel.updateLayout();
    alignPanel.paintAlignment(true, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void viewBoxesMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setShowBoxes(viewBoxesMenuItem.isSelected());
    alignPanel.paintAlignment(false, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void viewTextMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setShowText(viewTextMenuItem.isSelected());
    alignPanel.paintAlignment(false, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void renderGapsMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setRenderGaps(renderGapsMenuItem.isSelected());
    alignPanel.paintAlignment(false, false);
  }

  public FeatureSettings featureSettings;

  @Override
  public FeatureSettingsControllerI getFeatureSettingsUI()
  {
    return featureSettings;
  }

  @Override
  public void featureSettings_actionPerformed(ActionEvent e)
  {
    showFeatureSettingsUI();
  }

  @Override
  public FeatureSettingsControllerI showFeatureSettingsUI()
  {
    if (featureSettings != null)
    {
      featureSettings.closeOldSettings();
      featureSettings = null;
    }
    if (!showSeqFeatures.isSelected())
    {
      // make sure features are actually displayed
      showSeqFeatures.setSelected(true);
      showSeqFeatures_actionPerformed(null);
    }
    featureSettings = new FeatureSettings(this);
    return featureSettings;
  }

  /**
   * Set or clear 'Show Sequence Features'
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void showSeqFeatures_actionPerformed(ActionEvent evt)
  {
    viewport.setShowSequenceFeatures(showSeqFeatures.isSelected());
    alignPanel.paintAlignment(true, true);
  }

  /**
   * Action on toggle of the 'Show annotations' menu item. This shows or hides
   * the annotations panel as a whole.
   * 
   * The options to show/hide all annotations should be enabled when the panel
   * is shown, and disabled when the panel is hidden.
   * 
   * @param e
   */
  @Override
  public void annotationPanelMenuItem_actionPerformed(ActionEvent e)
  {
    final boolean setVisible = annotationPanelMenuItem.isSelected();
    viewport.setShowAnnotation(setVisible);
    this.showAllSeqAnnotations.setEnabled(setVisible);
    this.hideAllSeqAnnotations.setEnabled(setVisible);
    this.showAllAlAnnotations.setEnabled(setVisible);
    this.hideAllAlAnnotations.setEnabled(setVisible);
    alignPanel.updateLayout();
  }

  @Override
  public void alignmentProperties()
  {
    JComponent pane;
    StringBuffer contents = new AlignmentProperties(viewport.getAlignment())

            .formatAsHtml();
    String content = MessageManager.formatMessage("label.html_content",
            new Object[]
            { contents.toString() });
    contents = null;

    if (Platform.isJS())
    {
      JLabel textLabel = new JLabel();
      textLabel.setText(content);
      textLabel.setBackground(Color.WHITE);

      pane = new JPanel(new BorderLayout());
      ((JPanel) pane).setOpaque(true);
      pane.setBackground(Color.WHITE);
      ((JPanel) pane).add(textLabel, BorderLayout.NORTH);
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      JEditorPane editPane = new JEditorPane("text/html", "");
      editPane.setEditable(false);
      editPane.setText(content);
      pane = editPane;
    }

    JInternalFrame frame = new JInternalFrame();

    frame.getContentPane().add(new JScrollPane(pane));

    Desktop.addInternalFrame(frame, MessageManager
            .formatMessage("label.alignment_properties", new Object[]
            { getTitle() }), 500, 400);
  }

  /**
   * Opens an Overview panel for the alignment, unless one is open already
   * 
   * @param e
   */
  @Override
  public void overviewMenuItem_actionPerformed(ActionEvent e)
  {
    boolean showHiddenRegions = Cache
            .getDefault(Preferences.SHOW_OV_HIDDEN_AT_START, false);
    openOverviewPanel(showHiddenRegions);
  }

  public OverviewPanel openOverviewPanel(boolean showHidden)
  {
    if (alignPanel.overviewPanel != null)
    {
      return alignPanel.overviewPanel;
    }
    JInternalFrame frame = new JInternalFrame();
    final OverviewPanel overview = new OverviewPanel(alignPanel, frame,
            showHidden);
    frame.setContentPane(overview);
    Desktop.addInternalFrame(frame, "", true, frame.getWidth(),
            frame.getHeight(), true, true);
    frame.setFrameIcon(null);
    frame.pack();
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    final AlignmentPanel thePanel = this.alignPanel;
    frame.addInternalFrameListener(
            new javax.swing.event.InternalFrameAdapter()
            {
              @Override
              public void internalFrameClosed(
                      javax.swing.event.InternalFrameEvent evt)
              {
                overview.dispose();
                thePanel.setOverviewPanel(null);
              }
            });
    if (getKeyListeners().length > 0)
    {
      frame.addKeyListener(getKeyListeners()[0]);
    }

    alignPanel.setOverviewPanel(overview);
    alignPanel.setOverviewTitle(this);

    return overview;
  }

  @Override
  public void textColour_actionPerformed()
  {
    new TextColourChooser().chooseColour(alignPanel, null);
  }

  /*
   * public void covariationColour_actionPerformed() {
   * changeColour(new
   * CovariationColourScheme(viewport.getAlignment().getAlignmentAnnotation
   * ()[0])); }
   */
  @Override
  public void annotationColour_actionPerformed()
  {
    new AnnotationColourChooser(viewport, alignPanel);
  }

  @Override
  public void annotationColumn_actionPerformed(ActionEvent e)
  {
    new AnnotationColumnChooser(viewport, alignPanel);
  }

  /**
   * Action on the user checking or unchecking the option to apply the selected
   * colour scheme to all groups. If unchecked, groups may have their own
   * independent colour schemes.
   * 
   * @param selected
   */
  @Override
  public void applyToAllGroups_actionPerformed(boolean selected)
  {
    viewport.setColourAppliesToAllGroups(selected);
  }

  /**
   * Action on user selecting a colour from the colour menu
   * 
   * @param name
   *          the name (not the menu item label!) of the colour scheme
   */
  @Override
  public void changeColour_actionPerformed(String name)
  {
    /*
     * 'User Defined' opens a panel to configure or load a
     * user-defined colour scheme
     */
    if (ResidueColourScheme.USER_DEFINED_MENU.equals(name))
    {
      new UserDefinedColours(alignPanel);
      return;
    }

    /*
     * otherwise set the chosen colour scheme (or null for 'None')
     */
    ColourSchemeI cs = ColourSchemes.getInstance().getColourScheme(name,
            viewport, viewport.getAlignment(),
            viewport.getHiddenRepSequences());
    changeColour(cs);
  }

  /**
   * Actions on setting or changing the alignment colour scheme
   * 
   * @param cs
   */
  @Override
  public void changeColour(ColourSchemeI cs)
  {
    // TODO: pull up to controller method
    ColourMenuHelper.setColourSelected(colourMenu, cs);

    viewport.setGlobalColourScheme(cs);

    alignPanel.paintAlignment(true, true);
  }

  /**
   * Show the PID threshold slider panel
   */
  @Override
  protected void modifyPID_actionPerformed()
  {
    SliderPanel.setPIDSliderSource(alignPanel, viewport.getResidueShading(),
            alignPanel.getViewName());
    SliderPanel.showPIDSlider();
  }

  /**
   * Show the Conservation slider panel
   */
  @Override
  protected void modifyConservation_actionPerformed()
  {
    SliderPanel.setConservationSlider(alignPanel,
            viewport.getResidueShading(), alignPanel.getViewName());
    SliderPanel.showConservationSlider();
  }

  /**
   * Action on selecting or deselecting (Colour) By Conservation
   */
  @Override
  public void conservationMenuItem_actionPerformed(boolean selected)
  {
    modifyConservation.setEnabled(selected);
    viewport.setConservationSelected(selected);
    viewport.getResidueShading().setConservationApplied(selected);

    changeColour(viewport.getGlobalColourScheme());
    if (selected)
    {
      modifyConservation_actionPerformed();
    }
    else
    {
      SliderPanel.hideConservationSlider();
    }
  }

  /**
   * Action on selecting or deselecting (Colour) Above PID Threshold
   */
  @Override
  public void abovePIDThreshold_actionPerformed(boolean selected)
  {
    modifyPID.setEnabled(selected);
    viewport.setAbovePIDThreshold(selected);
    if (!selected)
    {
      viewport.getResidueShading().setThreshold(0,
              viewport.isIgnoreGapsConsensus());
    }

    changeColour(viewport.getGlobalColourScheme());
    if (selected)
    {
      modifyPID_actionPerformed();
    }
    else
    {
      SliderPanel.hidePIDSlider();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void sortPairwiseMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByPID(viewport.getAlignment(),
            viewport.getAlignment().getSequenceAt(0));
    addHistoryItem(new OrderCommand("Pairwise Sort", oldOrder,
            viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void sortIDMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByID(viewport.getAlignment());
    addHistoryItem(
            new OrderCommand("ID Sort", oldOrder, viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void sortLengthMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByLength(viewport.getAlignment());
    addHistoryItem(new OrderCommand("Length Sort", oldOrder,
            viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void sortGroupMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByGroup(viewport.getAlignment());
    addHistoryItem(new OrderCommand("Group Sort", oldOrder,
            viewport.getAlignment()));

    alignPanel.paintAlignment(true, false);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void removeRedundancyMenuItem_actionPerformed(ActionEvent e)
  {
    new RedundancyPanel(alignPanel, this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void pairwiseAlignmentMenuItem_actionPerformed(ActionEvent e)
  {
    if ((viewport.getSelectionGroup() == null)
            || (viewport.getSelectionGroup().getSize() < 2))
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.getString(
                      "label.you_must_select_least_two_sequences"),
              MessageManager.getString("label.invalid_selection"),
              JvOptionPane.WARNING_MESSAGE);
    }
    else
    {
      JInternalFrame frame = new JInternalFrame();
      frame.setContentPane(new PairwiseAlignPanel(viewport, false));
      Desktop.addInternalFrame(frame,
              MessageManager.getString("action.pairwise_alignment"), 600,
              500);
    }
  }

  @Override
  public void autoCalculate_actionPerformed(ActionEvent e)
  {
    viewport.autoCalculateConsensus = autoCalculate.isSelected();
    if (viewport.autoCalculateConsensus)
    {
      viewport.firePropertyChange("alignment", null,
              viewport.getAlignment().getSequences());
    }
  }

  @Override
  public void sortByTreeOption_actionPerformed(ActionEvent e)
  {
    viewport.sortByTree = sortByTree.isSelected();
  }

  @Override
  protected void listenToViewSelections_actionPerformed(ActionEvent e)
  {
    viewport.followSelection = listenToViewSelections.isSelected();
  }

  /**
   * Constructs a tree panel and adds it to the desktop
   * 
   * @param type
   *          tree type (NJ or AV)
   * @param modelName
   *          name of score model used to compute the tree
   * @param options
   *          parameters for the distance or similarity calculation
   */
  void newTreePanel(String type, String modelName,
          SimilarityParamsI options)
  {
    String frameTitle = "";
    TreePanel tp;

    boolean onSelection = false;
    if (viewport.getSelectionGroup() != null
            && viewport.getSelectionGroup().getSize() > 0)
    {
      SequenceGroup sg = viewport.getSelectionGroup();

      /* Decide if the selection is a column region */
      for (SequenceI _s : sg.getSequences())
      {
        if (_s.getLength() < sg.getEndRes())
        {
          JvOptionPane.showMessageDialog(Desktop.desktop,
                  MessageManager.getString(
                          "label.selected_region_to_tree_may_only_contain_residues_or_gaps"),
                  MessageManager.getString(
                          "label.sequences_selection_not_aligned"),
                  JvOptionPane.WARNING_MESSAGE);

          return;
        }
      }
      onSelection = true;
    }
    else
    {
      if (viewport.getAlignment().getHeight() < 2)
      {
        return;
      }
    }

    tp = new TreePanel(alignPanel, type, modelName, options);
    frameTitle = tp.getPanelTitle() + (onSelection ? " on region" : "");

    frameTitle += " from ";

    if (viewport.getViewName() != null)
    {
      frameTitle += viewport.getViewName() + " of ";
    }

    frameTitle += this.title;

    Desktop.addInternalFrame(tp, frameTitle, 600, 500);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param title
   *          DOCUMENT ME!
   * @param order
   *          DOCUMENT ME!
   */
  public void addSortByOrderMenuItem(String title,
          final AlignmentOrder order)
  {
    final JMenuItem item = new JMenuItem(MessageManager
            .formatMessage("action.by_title_param", new Object[]
            { title }));
    sort.add(item);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();

        // TODO: JBPNote - have to map order entries to curent SequenceI
        // pointers
        AlignmentSorter.sortBy(viewport.getAlignment(), order);

        addHistoryItem(new OrderCommand(order.getName(), oldOrder,
                viewport.getAlignment()));

        alignPanel.paintAlignment(true, false);
      }
    });
  }

  /**
   * Add a new sort by annotation score menu item
   * 
   * @param sort
   *          the menu to add the option to
   * @param scoreLabel
   *          the label used to retrieve scores for each sequence on the
   *          alignment
   */
  public void addSortByAnnotScoreMenuItem(JMenu sort,
          final String scoreLabel)
  {
    final JMenuItem item = new JMenuItem(scoreLabel);
    sort.add(item);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
        AlignmentSorter.sortByAnnotationScore(scoreLabel,
                viewport.getAlignment());// ,viewport.getSelectionGroup());
        addHistoryItem(new OrderCommand("Sort by " + scoreLabel, oldOrder,
                viewport.getAlignment()));
        alignPanel.paintAlignment(true, false);
      }
    });
  }

  /**
   * last hash for alignment's annotation array - used to minimise cost of
   * rebuild.
   */
  protected int _annotationScoreVectorHash;

  /**
   * search the alignment and rebuild the sort by annotation score submenu the
   * last alignment annotation vector hash is stored to minimize cost of
   * rebuilding in subsequence calls.
   * 
   */
  @Override
  public void buildSortByAnnotationScoresMenu()
  {
    if (viewport.getAlignment().getAlignmentAnnotation() == null)
    {
      return;
    }

    if (viewport.getAlignment().getAlignmentAnnotation()
            .hashCode() != _annotationScoreVectorHash)
    {
      sortByAnnotScore.removeAll();
      // almost certainly a quicker way to do this - but we keep it simple
      Hashtable<String, String> scoreSorts = new Hashtable<>();
      AlignmentAnnotation aann[];
      for (SequenceI sqa : viewport.getAlignment().getSequences())
      {
        aann = sqa.getAnnotation();
        for (int i = 0; aann != null && i < aann.length; i++)
        {
          if (aann[i].hasScore() && aann[i].sequenceRef != null)
          {
            scoreSorts.put(aann[i].label, aann[i].label);
          }
        }
      }
      Enumeration<String> labels = scoreSorts.keys();
      while (labels.hasMoreElements())
      {
        addSortByAnnotScoreMenuItem(sortByAnnotScore, labels.nextElement());
      }
      sortByAnnotScore.setVisible(scoreSorts.size() > 0);
      scoreSorts.clear();

      _annotationScoreVectorHash = viewport.getAlignment()
              .getAlignmentAnnotation().hashCode();
    }
  }

  /**
   * Maintain the Order by->Displayed Tree menu. Creates a new menu item for a
   * TreePanel with an appropriate <code>jalview.analysis.AlignmentSorter</code>
   * call. Listeners are added to remove the menu item when the treePanel is
   * closed, and adjust the tree leaf to sequence mapping when the alignment is
   * modified.
   */
  @Override
  public void buildTreeSortMenu()
  {
    sortByTreeMenu.removeAll();

    List<Component> comps = PaintRefresher.components
            .get(viewport.getSequenceSetId());
    List<TreePanel> treePanels = new ArrayList<>();
    for (Component comp : comps)
    {
      if (comp instanceof TreePanel)
      {
        treePanels.add((TreePanel) comp);
      }
    }

    if (treePanels.size() < 1)
    {
      sortByTreeMenu.setVisible(false);
      return;
    }

    sortByTreeMenu.setVisible(true);

    for (final TreePanel tp : treePanels)
    {
      final JMenuItem item = new JMenuItem(tp.getTitle());
      item.addActionListener(new java.awt.event.ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          tp.sortByTree_actionPerformed();
          addHistoryItem(tp.sortAlignmentIn(alignPanel));

        }
      });

      sortByTreeMenu.add(item);
    }
  }

  public boolean sortBy(AlignmentOrder alorder, String undoname)
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortBy(viewport.getAlignment(), alorder);
    if (undoname != null)
    {
      addHistoryItem(new OrderCommand(undoname, oldOrder,
              viewport.getAlignment()));
    }
    alignPanel.paintAlignment(true, false);
    return true;
  }

  /**
   * Work out whether the whole set of sequences or just the selected set will
   * be submitted for multiple alignment.
   * 
   */
  public jalview.datamodel.AlignmentView gatherSequencesForAlignment()
  {
    // Now, check we have enough sequences
    AlignmentView msa = null;

    if ((viewport.getSelectionGroup() != null)
            && (viewport.getSelectionGroup().getSize() > 1))
    {
      // JBPNote UGLY! To prettify, make SequenceGroup and Alignment conform to
      // some common interface!
      /*
       * SequenceGroup seqs = viewport.getSelectionGroup(); int sz; msa = new
       * SequenceI[sz = seqs.getSize(false)];
       * 
       * for (int i = 0; i < sz; i++) { msa[i] = (SequenceI)
       * seqs.getSequenceAt(i); }
       */
      msa = viewport.getAlignmentView(true);
    }
    else if (viewport.getSelectionGroup() != null
            && viewport.getSelectionGroup().getSize() == 1)
    {
      int option = JvOptionPane.showConfirmDialog(this,
              MessageManager.getString("warn.oneseq_msainput_selection"),
              MessageManager.getString("label.invalid_selection"),
              JvOptionPane.OK_CANCEL_OPTION);
      if (option == JvOptionPane.OK_OPTION)
      {
        msa = viewport.getAlignmentView(false);
      }
    }
    else
    {
      msa = viewport.getAlignmentView(false);
    }
    return msa;
  }

  /**
   * Decides what is submitted to a secondary structure prediction service: the
   * first sequence in the alignment, or in the current selection, or, if the
   * alignment is 'aligned' (ie padded with gaps), then the currently selected
   * region or the whole alignment. (where the first sequence in the set is the
   * one that the prediction will be for).
   */
  public AlignmentView gatherSeqOrMsaForSecStrPrediction()
  {
    AlignmentView seqs = null;

    if ((viewport.getSelectionGroup() != null)
            && (viewport.getSelectionGroup().getSize() > 0))
    {
      seqs = viewport.getAlignmentView(true);
    }
    else
    {
      seqs = viewport.getAlignmentView(false);
    }
    // limit sequences - JBPNote in future - could spawn multiple prediction
    // jobs
    // TODO: viewport.getAlignment().isAligned is a global state - the local
    // selection may well be aligned - we preserve 2.0.8 behaviour for moment.
    if (!viewport.getAlignment().isAligned(false))
    {
      seqs.setSequences(new SeqCigar[] { seqs.getSequences()[0] });
      // TODO: if seqs.getSequences().length>1 then should really have warned
      // user!

    }
    return seqs;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void loadTreeMenuItem_actionPerformed(ActionEvent e)
  {
    // Pick the tree file
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.select_newick_like_tree_file"));
    chooser.setToolTipText(
            MessageManager.getString("label.load_tree_file"));

    chooser.setResponseHandler(0, () -> {
      String filePath = chooser.getSelectedFile().getPath();
      Cache.setProperty("LAST_DIRECTORY", filePath);
      NewickFile fin = null;
      try
      {
        fin = new NewickFile(new FileParse(chooser.getSelectedFile(),
                DataSourceType.FILE));
        viewport.setCurrentTree(showNewickTree(fin, filePath).getTree());
      } catch (Exception ex)
      {
        JvOptionPane.showMessageDialog(Desktop.desktop, ex.getMessage(),
                MessageManager.getString("label.problem_reading_tree_file"),
                JvOptionPane.WARNING_MESSAGE);
        ex.printStackTrace();
      }
      if (fin != null && fin.hasWarningMessage())
      {
        JvOptionPane.showMessageDialog(Desktop.desktop,
                fin.getWarningMessage(),
                MessageManager
                        .getString("label.possible_problem_with_tree_file"),
                JvOptionPane.WARNING_MESSAGE);
      }
    });
    chooser.showOpenDialog(this);
  }

  public TreePanel showNewickTree(NewickFile nf, String treeTitle)
  {
    return showNewickTree(nf, treeTitle, 600, 500, 4, 5);
  }

  public TreePanel showNewickTree(NewickFile nf, String treeTitle, int w,
          int h, int x, int y)
  {
    return showNewickTree(nf, treeTitle, null, w, h, x, y);
  }

  /**
   * Add a treeviewer for the tree extracted from a Newick file object to the
   * current alignment view
   * 
   * @param nf
   *          the tree
   * @param title
   *          tree viewer title
   * @param input
   *          Associated alignment input data (or null)
   * @param w
   *          width
   * @param h
   *          height
   * @param x
   *          position
   * @param y
   *          position
   * @return TreePanel handle
   */
  public TreePanel showNewickTree(NewickFile nf, String treeTitle,
          AlignmentView input, int w, int h, int x, int y)
  {
    TreePanel tp = null;

    try
    {
      nf.parse();

      if (nf.getTree() != null)
      {
        tp = new TreePanel(alignPanel, nf, treeTitle, input);

        tp.setSize(w, h);

        if (x > 0 && y > 0)
        {
          tp.setLocation(x, y);
        }

        Desktop.addInternalFrame(tp, treeTitle, w, h);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return tp;
  }

  public void showContactMapTree(AlignmentAnnotation aa,
          ContactMatrixI cm)
  {
    int x = 4, y = 5;
    int w = 400, h = 500;

    try
    {
      NewickFile fin = new NewickFile(
              new FileParse(cm.getNewick(), DataSourceType.PASTE));
      String title = aa.label + " "
              + cm.getTreeMethod() + " tree" + (aa.sequenceRef != null
                      ? (" for " + aa.sequenceRef.getDisplayId(false))
                      : "");

      showColumnWiseTree(fin, aa, title, w, h, x, y);
    } catch (Throwable xx)
    {
      Console.error("Unexpected exception showing tree for contact matrix",
              xx);
    }
  }

  public TreePanel showColumnWiseTree(NewickFile nf, AlignmentAnnotation aa,
          String treeTitle, int w, int h, int x, int y)
  {
    try
    {
      nf.parse();
      if (nf.getTree() == null)
      {
        return null;
      }
      TreePanel tp = new TreePanel(alignPanel, nf, aa, treeTitle);

      tp.setSize(w, h);

      if (x > 0 && y > 0)
      {
        tp.setLocation(x, y);
      }

      Desktop.addInternalFrame(tp, treeTitle, w, h);
      return tp;
    } catch (Throwable xx)
    {
      Console.error("Unexpected exception showing tree for contact matrix",
              xx);
    }
    return null;
  }

  private boolean buildingMenu = false;

  /**
   * Generates menu items and listener event actions for web service clients
   * 
   */
  public void BuildWebServiceMenu()
  {
    while (buildingMenu)
    {
      try
      {
        System.err.println("Waiting for building menu to finish.");
        Thread.sleep(10);
      } catch (Exception e)
      {
      }
    }
    final AlignFrame me = this;
    buildingMenu = true;
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        final List<JMenuItem> legacyItems = new ArrayList<>();
        try
        {
          // System.err.println("Building ws menu again "
          // + Thread.currentThread());
          // TODO: add support for context dependent disabling of services based
          // on
          // alignment and current selection
          // TODO: add additional serviceHandle parameter to specify abstract
          // handler
          // class independently of AbstractName
          // TODO: add in rediscovery GUI function to restart discoverer
          // TODO: group services by location as well as function and/or
          // introduce
          // object broker mechanism.
          final Vector<JMenu> wsmenu = new Vector<>();
          final IProgressIndicator af = me;

          /*
           * do not i18n these strings - they are hard-coded in class
           * compbio.data.msa.Category, Jws2Discoverer.isRecalculable() and
           * SequenceAnnotationWSClient.initSequenceAnnotationWSClient()
           */
          final JMenu msawsmenu = new JMenu("Alignment");
          final JMenu secstrmenu = new JMenu(
                  "Secondary Structure Prediction");
          final JMenu seqsrchmenu = new JMenu("Sequence Database Search");
          final JMenu analymenu = new JMenu("Analysis");
          final JMenu dismenu = new JMenu("Protein Disorder");
          // JAL-940 - only show secondary structure prediction services from
          // the legacy server
          if (// Cache.getDefault("SHOW_JWS1_SERVICES", true)
              // &&
          Discoverer.services != null && (Discoverer.services.size() > 0))
          {
            // TODO: refactor to allow list of AbstractName/Handler bindings to
            // be
            // stored or retrieved from elsewhere
            // No MSAWS used any more:
            // Vector msaws = null; // (Vector)
            // Discoverer.services.get("MsaWS");
            Vector<ServiceHandle> secstrpr = Discoverer.services
                    .get("SecStrPred");
            if (secstrpr != null)
            {
              // Add any secondary structure prediction services
              for (int i = 0, j = secstrpr.size(); i < j; i++)
              {
                final ext.vamsas.ServiceHandle sh = secstrpr.get(i);
                jalview.ws.WSMenuEntryProviderI impl = jalview.ws.jws1.Discoverer
                        .getServiceClient(sh);
                int p = secstrmenu.getItemCount();
                impl.attachWSMenuEntry(secstrmenu, me);
                int q = secstrmenu.getItemCount();
                for (int litm = p; litm < q; litm++)
                {
                  legacyItems.add(secstrmenu.getItem(litm));
                }
              }
            }
          }

          // Add all submenus in the order they should appear on the web
          // services menu
          wsmenu.add(msawsmenu);
          wsmenu.add(secstrmenu);
          wsmenu.add(dismenu);
          wsmenu.add(analymenu);
          // No search services yet
          // wsmenu.add(seqsrchmenu);

          javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              try
              {
                webService.removeAll();
                // first, add discovered services onto the webservices menu
                if (wsmenu.size() > 0)
                {
                  for (int i = 0, j = wsmenu.size(); i < j; i++)
                  {
                    webService.add(wsmenu.get(i));
                  }
                }
                else
                {
                  webService.add(me.webServiceNoServices);
                }
                // TODO: move into separate menu builder class.
                {
                  // logic for 2.11.1.4 is
                  // always look to see if there is a discover. if there isn't
                  // we can't show any Jws2 services
                  // if there are services available, show them - regardless of
                  // the 'show JWS2 preference'
                  // if the discoverer is running then say so
                  // otherwise offer to trigger discovery if 'show JWS2' is not
                  // enabled
                  Jws2Discoverer jws2servs = Jws2Discoverer.getDiscoverer();
                  if (jws2servs != null)
                  {
                    if (jws2servs.hasServices())
                    {
                      jws2servs.attachWSMenuEntry(webService, me);
                      for (Jws2Instance sv : jws2servs.getServices())
                      {
                        if (sv.description.toLowerCase(Locale.ROOT)
                                .contains("jpred"))
                        {
                          for (JMenuItem jmi : legacyItems)
                          {
                            jmi.setVisible(false);
                          }
                        }
                      }
                    }

                    if (jws2servs.isRunning())
                    {
                      JMenuItem tm = new JMenuItem(
                              "Still discovering JABA Services");
                      tm.setEnabled(false);
                      webService.add(tm);
                    }
                    else if (!Cache.getDefault("SHOW_JWS2_SERVICES", true))
                    {
                      JMenuItem enableJws2 = new JMenuItem(
                              "Discover Web Services");
                      enableJws2.setToolTipText(
                              "Select to start JABA Web Service discovery (or enable option in Web Service preferences)");
                      enableJws2.setEnabled(true);
                      enableJws2.addActionListener(new ActionListener()
                      {

                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                          // start service discoverer, but ignore preference
                          Desktop.instance.startServiceDiscovery(false,
                                  true);
                        }
                      });
                      webService.add(enableJws2);
                    }
                  }
                }
                build_urlServiceMenu(me.webService);
                build_fetchdbmenu(webService);
                for (JMenu item : wsmenu)
                {
                  if (item.getItemCount() == 0)
                  {
                    item.setEnabled(false);
                  }
                  else
                  {
                    item.setEnabled(true);
                  }
                }
              } catch (Exception e)
              {
                Console.debug(
                        "Exception during web service menu building process.",
                        e);
              }
            }
          });
        } catch (Exception e)
        {
        }
        buildingMenu = false;
      }
    }).start();

  }

  /**
   * construct any groupURL type service menu entries.
   * 
   * @param webService
   */
  protected void build_urlServiceMenu(JMenu webService)
  {
    // TODO: remove this code when 2.7 is released
    // DEBUG - alignmentView
    /*
     * JMenuItem testAlView = new JMenuItem("Test AlignmentView"); final
     * AlignFrame af = this; testAlView.addActionListener(new ActionListener() {
     * 
     * @Override public void actionPerformed(ActionEvent e) {
     * jalview.datamodel.AlignmentView
     * .testSelectionViews(af.viewport.getAlignment(),
     * af.viewport.getColumnSelection(), af.viewport.selectionGroup); }
     * 
     * }); webService.add(testAlView);
     */
    // TODO: refactor to RestClient discoverer and merge menu entries for
    // rest-style services with other types of analysis/calculation service
    // SHmmr test client - still being implemented.
    // DEBUG - alignmentView

    for (jalview.ws.rest.RestClient client : jalview.ws.rest.RestClient
            .getRestClients())
    {
      client.attachWSMenuEntry(
              JvSwingUtils.findOrCreateMenu(webService, client.getAction()),
              this);
    }
  }

  /**
   * Searches the alignment sequences for xRefs and builds the Show
   * Cross-References menu (formerly called Show Products), with database
   * sources for which cross-references are found (protein sources for a
   * nucleotide alignment and vice versa)
   * 
   * @return true if Show Cross-references menu should be enabled
   */
  public boolean canShowProducts()
  {
    SequenceI[] seqs = viewport.getAlignment().getSequencesArray();
    AlignmentI dataset = viewport.getAlignment().getDataset();

    showProducts.removeAll();
    final boolean dna = viewport.getAlignment().isNucleotide();

    if (seqs == null || seqs.length == 0)
    {
      // nothing to see here.
      return false;
    }

    boolean showp = false;
    try
    {
      List<String> ptypes = new CrossRef(seqs, dataset)
              .findXrefSourcesForSequences(dna);

      for (final String source : ptypes)
      {
        showp = true;
        final AlignFrame af = this;
        JMenuItem xtype = new JMenuItem(source);
        xtype.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            showProductsFor(af.viewport.getSequenceSelection(), dna,
                    source);
          }
        });
        showProducts.add(xtype);
      }
      showProducts.setVisible(showp);
      showProducts.setEnabled(showp);
    } catch (Exception e)
    {
      Console.warn(
              "canShowProducts threw an exception - please report to help@jalview.org",
              e);
      return false;
    }
    return showp;
  }

  /**
   * Finds and displays cross-references for the selected sequences (protein
   * products for nucleotide sequences, dna coding sequences for peptides).
   * 
   * @param sel
   *          the sequences to show cross-references for
   * @param dna
   *          true if from a nucleotide alignment (so showing proteins)
   * @param source
   *          the database to show cross-references for
   */
  protected void showProductsFor(final SequenceI[] sel, final boolean _odna,
          final String source)
  {
    new Thread(CrossRefAction.getHandlerFor(sel, _odna, source, this))
            .start();
  }

  /**
   * Construct and display a new frame containing the translation of this
   * frame's DNA sequences to their aligned protein (amino acid) equivalents.
   */
  @Override
  public void showTranslation_actionPerformed(GeneticCodeI codeTable)
  {
    AlignmentI al = null;
    try
    {
      Dna dna = new Dna(viewport, viewport.getViewAsVisibleContigs(true));

      al = dna.translateCdna(codeTable);
    } catch (Exception ex)
    {
      Console.error("Exception during translation. Please report this !",
              ex);
      final String msg = MessageManager.getString(
              "label.error_when_translating_sequences_submit_bug_report");
      final String errorTitle = MessageManager
              .getString("label.implementation_error")
              + MessageManager.getString("label.translation_failed");
      JvOptionPane.showMessageDialog(Desktop.desktop, msg, errorTitle,
              JvOptionPane.ERROR_MESSAGE);
      return;
    }
    if (al == null || al.getHeight() == 0)
    {
      final String msg = MessageManager.getString(
              "label.select_at_least_three_bases_in_at_least_one_sequence_to_cDNA_translation");
      final String errorTitle = MessageManager
              .getString("label.translation_failed");
      JvOptionPane.showMessageDialog(Desktop.desktop, msg, errorTitle,
              JvOptionPane.WARNING_MESSAGE);
    }
    else
    {
      AlignFrame af = new AlignFrame(al, DEFAULT_WIDTH, DEFAULT_HEIGHT);
      af.setFileFormat(this.currentFileFormat);
      final String newTitle = MessageManager
              .formatMessage("label.translation_of_params", new Object[]
              { this.getTitle(), codeTable.getId() });
      af.setTitle(newTitle);
      if (Cache.getDefault(Preferences.ENABLE_SPLIT_FRAME, true))
      {
        final SequenceI[] seqs = viewport.getSelectionAsNewSequence();
        viewport.openSplitFrame(af, new Alignment(seqs));
      }
      else
      {
        Desktop.addInternalFrame(af, newTitle, DEFAULT_WIDTH,
                DEFAULT_HEIGHT);
      }
    }
  }

  /**
   * Set the file format
   * 
   * @param format
   */
  public void setFileFormat(FileFormatI format)
  {
    this.currentFileFormat = format;
  }

  /**
   * Try to load a features file onto the alignment.
   * 
   * @param file
   *          contents or path to retrieve file or a File object
   * @param sourceType
   *          access mode of file (see jalview.io.AlignFile)
   * @return true if features file was parsed correctly.
   */
  public boolean parseFeaturesFile(Object file, DataSourceType sourceType)
  {
    // BH 2018
    return avc.parseFeaturesFile(file, sourceType,
            Cache.getDefault("RELAXEDSEQIDMATCHING", false));

  }

  @Override
  public void refreshFeatureUI(boolean enableIfNecessary)
  {
    // note - currently this is only still here rather than in the controller
    // because of the featureSettings hard reference that is yet to be
    // abstracted
    if (enableIfNecessary)
    {
      viewport.setShowSequenceFeatures(true);
      showSeqFeatures.setSelected(true);
    }

  }

  @Override
  public void dragEnter(DropTargetDragEvent evt)
  {
  }

  @Override
  public void dragExit(DropTargetEvent evt)
  {
  }

  @Override
  public void dragOver(DropTargetDragEvent evt)
  {
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent evt)
  {
  }

  @Override
  public void drop(DropTargetDropEvent evt)
  {
    // JAL-1552 - acceptDrop required before getTransferable call for
    // Java's Transferable for native dnd
    evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    Transferable t = evt.getTransferable();

    final AlignFrame thisaf = this;
    final List<Object> files = new ArrayList<>();
    List<DataSourceType> protocols = new ArrayList<>();

    try
    {
      Desktop.transferFromDropTarget(files, protocols, evt, t);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    if (files != null)
    {
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            // check to see if any of these files have names matching sequences
            // in
            // the alignment
            SequenceIdMatcher idm = new SequenceIdMatcher(
                    viewport.getAlignment().getSequencesArray());
            /**
             * Object[] { String,SequenceI}
             */
            ArrayList<Object[]> filesmatched = new ArrayList<>();
            ArrayList<Object> filesnotmatched = new ArrayList<>();
            for (int i = 0; i < files.size(); i++)
            {
              // BH 2018
              Object file = files.get(i);
              String fileName = file.toString();
              String pdbfn = "";
              DataSourceType protocol = (file instanceof File
                      ? DataSourceType.FILE
                      : FormatAdapter.checkProtocol(fileName));
              if (protocol == DataSourceType.FILE)
              {
                File fl;
                if (file instanceof File)
                {
                  fl = (File) file;
                  Platform.cacheFileData(fl);
                }
                else
                {
                  fl = new File(fileName);
                }
                pdbfn = fl.getName();
              }
              else if (protocol == DataSourceType.URL)
              {
                URL url = new URL(fileName);
                pdbfn = url.getFile();
              }
              if (pdbfn.length() > 0)
              {
                // attempt to find a match in the alignment
                SequenceI[] mtch = idm.findAllIdMatches(pdbfn);
                int l = 0, c = pdbfn.indexOf(".");
                while (mtch == null && c != -1)
                {
                  do
                  {
                    l = c;
                  } while ((c = pdbfn.indexOf(".", l)) > l);
                  if (l > -1)
                  {
                    pdbfn = pdbfn.substring(0, l);
                  }
                  mtch = idm.findAllIdMatches(pdbfn);
                }
                if (mtch != null)
                {
                  FileFormatI type;
                  try
                  {
                    type = new IdentifyFile().identify(file, protocol);
                  } catch (Exception ex)
                  {
                    type = null;
                  }
                  if (type != null && type.isStructureFile())
                  {
                    filesmatched.add(new Object[] { file, protocol, mtch });
                    continue;
                  }
                }
                // File wasn't named like one of the sequences or wasn't a PDB
                // file.
                filesnotmatched.add(file);
              }
            }
            int assocfiles = 0;
            if (filesmatched.size() > 0)
            {
              boolean autoAssociate = Cache
                      .getDefault("AUTOASSOCIATE_PDBANDSEQS", false);
              if (!autoAssociate)
              {
                String msg = MessageManager.formatMessage(
                        "label.automatically_associate_structure_files_with_sequences_same_name",
                        new Object[]
                        { Integer.valueOf(filesmatched.size())
                                .toString() });
                String ttl = MessageManager.getString(
                        "label.automatically_associate_structure_files_by_name");
                int choice = JvOptionPane.showConfirmDialog(thisaf, msg,
                        ttl, JvOptionPane.YES_NO_OPTION);
                autoAssociate = choice == JvOptionPane.YES_OPTION;
              }
              if (autoAssociate)
              {
                for (Object[] fm : filesmatched)
                {
                  // try and associate
                  // TODO: may want to set a standard ID naming formalism for
                  // associating PDB files which have no IDs.
                  for (SequenceI toassoc : (SequenceI[]) fm[2])
                  {
                    PDBEntry pe = new AssociatePdbFileWithSeq()
                            .associatePdbWithSeq(fm[0].toString(),
                                    (DataSourceType) fm[1], toassoc, false,
                                    Desktop.instance);
                    if (pe != null)
                    {
                      System.err.println("Associated file : "
                              + (fm[0].toString()) + " with "
                              + toassoc.getDisplayId(true));
                      assocfiles++;
                    }
                  }
                  // TODO: do we need to update overview ? only if features are
                  // shown I guess
                  alignPanel.paintAlignment(true, false);
                }
              }
              else
              {
                /*
                 * add declined structures as sequences
                 */
                for (Object[] o : filesmatched)
                {
                  filesnotmatched.add(o[0]);
                }
              }
            }
            if (filesnotmatched.size() > 0)
            {
              if (assocfiles > 0 && (Cache.getDefault(
                      "AUTOASSOCIATE_PDBANDSEQS_IGNOREOTHERS", false)
                      || JvOptionPane.showConfirmDialog(thisaf,
                              "<html>" + MessageManager.formatMessage(
                                      "label.ignore_unmatched_dropped_files_info",
                                      new Object[]
                                      { Integer.valueOf(
                                              filesnotmatched.size())
                                              .toString() })
                                      + "</html>",
                              MessageManager.getString(
                                      "label.ignore_unmatched_dropped_files"),
                              JvOptionPane.YES_NO_OPTION) == JvOptionPane.YES_OPTION))
              {
                return;
              }
              for (Object fn : filesnotmatched)
              {
                loadJalviewDataFile(fn, null, null, null);
              }

            }
          } catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
      }).start();
    }
  }

  /**
   * Attempt to load a "dropped" file or URL string, by testing in turn for
   * <ul>
   * <li>an Annotation file</li>
   * <li>a JNet file</li>
   * <li>a features file</li>
   * <li>else try to interpret as an alignment file</li>
   * </ul>
   * 
   * @param file
   *          either a filename or a URL string.
   */
  public void loadJalviewDataFile(Object file, DataSourceType sourceType,
          FileFormatI format, SequenceI assocSeq)
  {
    // BH 2018 was String file
    try
    {
      if (sourceType == null)
      {
        sourceType = FormatAdapter.checkProtocol(file);
      }
      // if the file isn't identified, or not positively identified as some
      // other filetype (PFAM is default unidentified alignment file type) then
      // try to parse as annotation.
      boolean isAnnotation = (format == null
              || FileFormat.Pfam.equals(format))
                      ? new AnnotationFile().annotateAlignmentView(viewport,
                              file, sourceType)
                      : false;

      if (!isAnnotation)
      {
        // first see if its a T-COFFEE score file
        TCoffeeScoreFile tcf = null;
        try
        {
          tcf = new TCoffeeScoreFile(file, sourceType);
          if (tcf.isValid())
          {
            if (tcf.annotateAlignment(viewport.getAlignment(), true))
            {
              buildColourMenu();
              changeColour(
                      new TCoffeeColourScheme(viewport.getAlignment()));
              isAnnotation = true;
              setStatus(MessageManager.getString(
                      "label.successfully_pasted_tcoffee_scores_to_alignment"));
            }
            else
            {
              // some problem - if no warning its probable that the ID matching
              // process didn't work
              JvOptionPane.showMessageDialog(Desktop.desktop,
                      tcf.getWarningMessage() == null
                              ? MessageManager.getString(
                                      "label.check_file_matches_sequence_ids_alignment")
                              : tcf.getWarningMessage(),
                      MessageManager.getString(
                              "label.problem_reading_tcoffee_score_file"),
                      JvOptionPane.WARNING_MESSAGE);
            }
          }
          else
          {
            tcf = null;
          }
        } catch (Exception x)
        {
          Console.debug(
                  "Exception when processing data source as T-COFFEE score file",
                  x);
          tcf = null;
        }
        if (tcf == null)
        {
          // try to see if its a JNet 'concise' style annotation file *before*
          // we
          // try to parse it as a features file
          if (format == null)
          {
            format = new IdentifyFile().identify(file, sourceType);
          }
          if (FileFormat.ScoreMatrix == format)
          {
            ScoreMatrixFile sm = new ScoreMatrixFile(
                    new FileParse(file, sourceType));
            sm.parse();
            // todo: i18n this message
            setStatus(MessageManager.formatMessage(
                    "label.successfully_loaded_matrix",
                    sm.getMatrixName()));
          }
          else if (FileFormat.Jnet.equals(format))
          {
            JPredFile predictions = new JPredFile(file, sourceType);
            new JnetAnnotationMaker();
            JnetAnnotationMaker.add_annotation(predictions,
                    viewport.getAlignment(), 0, false);
            viewport.getAlignment().setupJPredAlignment();
            isAnnotation = true;
          }
          // else if (IdentifyFile.FeaturesFile.equals(format))
          else if (FileFormat.Features.equals(format))
          {
            if (parseFeaturesFile(file, sourceType))
            {
              SplitFrame splitFrame = (SplitFrame) getSplitViewContainer();
              if (splitFrame != null)
              {
                splitFrame.repaint();
              }
              else
              {
                alignPanel.paintAlignment(true, true);
              }
            }
          }
          else
          {
            new FileLoader().LoadFile(viewport, file, sourceType, format);
          }
        }
      }
      if (isAnnotation)
      {

        alignPanel.adjustAnnotationHeight();
        viewport.updateSequenceIdColours();
        buildSortByAnnotationScoresMenu();
        alignPanel.paintAlignment(true, true);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    } catch (OutOfMemoryError oom)
    {
      try
      {
        System.gc();
      } catch (Exception x)
      {
      }
      new OOMWarning(
              "loading data "
                      + (sourceType != null
                              ? (sourceType == DataSourceType.PASTE
                                      ? "from clipboard."
                                      : "using " + sourceType + " from "
                                              + file)
                              : ".")
                      + (format != null
                              ? "(parsing as '" + format + "' file)"
                              : ""),
              oom, Desktop.desktop);
    }
  }

  /**
   * Method invoked by the ChangeListener on the tabbed pane, in other words
   * when a different tabbed pane is selected by the user or programmatically.
   */
  @Override
  public void tabSelectionChanged(int index)
  {
    if (index > -1)
    {
      /*
       * update current Overview window title (if there is one)
       * to add view name "Original" if necessary
       */
      alignPanel.setOverviewTitle(this);

      /*
       * switch panels and set Overview title (if there is one
       * because it was opened automatically)
       */
      alignPanel = alignPanels.get(index);
      alignPanel.setOverviewTitle(this);

      viewport = alignPanel.av;
      avc.setViewportAndAlignmentPanel(viewport, alignPanel);
      setMenusFromViewport(viewport);
      if (featureSettings != null && featureSettings.isOpen()
              && featureSettings.fr.getViewport() != viewport)
      {
        if (viewport.isShowSequenceFeatures())
        {
          // refresh the featureSettings to reflect UI change
          showFeatureSettingsUI();
        }
        else
        {
          // close feature settings for this view.
          featureSettings.close();
        }
      }

    }

    /*
     * 'focus' any colour slider that is open to the selected viewport
     */
    if (viewport.getConservationSelected())
    {
      SliderPanel.setConservationSlider(alignPanel,
              viewport.getResidueShading(), alignPanel.getViewName());
    }
    else
    {
      SliderPanel.hideConservationSlider();
    }
    if (viewport.getAbovePIDThreshold())
    {
      SliderPanel.setPIDSliderSource(alignPanel,
              viewport.getResidueShading(), alignPanel.getViewName());
    }
    else
    {
      SliderPanel.hidePIDSlider();
    }

    /*
     * If there is a frame linked to this one in a SplitPane, switch it to the
     * same view tab index. No infinite recursion of calls should happen, since
     * tabSelectionChanged() should not get invoked on setting the selected
     * index to an unchanged value. Guard against setting an invalid index
     * before the new view peer tab has been created.
     */
    final AlignViewportI peer = viewport.getCodingComplement();
    if (peer != null)
    {
      AlignFrame linkedAlignFrame = ((AlignViewport) peer)
              .getAlignPanel().alignFrame;
      if (linkedAlignFrame.tabbedPane.getTabCount() > index)
      {
        linkedAlignFrame.tabbedPane.setSelectedIndex(index);
      }
    }
  }

  /**
   * On right mouse click on view tab, prompt for and set new view name.
   */
  @Override
  public void tabbedPane_mousePressed(MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      String msg = MessageManager.getString("label.enter_view_name");
      String ttl = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
      String reply = JvOptionPane.showInputDialog(msg, ttl);

      if (reply != null)
      {
        viewport.setViewName(reply);
        // TODO warn if reply is in getExistingViewNames()?
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), reply);
      }
    }
  }

  public AlignViewport getCurrentView()
  {
    return viewport;
  }

  /**
   * Open the dialog for regex description parsing.
   */
  @Override
  protected void extractScores_actionPerformed(ActionEvent e)
  {
    ParseProperties pp = new jalview.analysis.ParseProperties(
            viewport.getAlignment());
    // TODO: verify regex and introduce GUI dialog for version 2.5
    // if (pp.getScoresFromDescription("col", "score column ",
    // "\\W*([-+]?\\d*\\.?\\d*e?-?\\d*)\\W+([-+]?\\d*\\.?\\d*e?-?\\d*)",
    // true)>0)
    if (pp.getScoresFromDescription("description column",
            "score in description column ", "\\W*([-+eE0-9.]+)", true) > 0)
    {
      buildSortByAnnotationScoresMenu();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#showDbRefs_actionPerformed(java.awt.event.ActionEvent
   * )
   */
  @Override
  protected void showDbRefs_actionPerformed(ActionEvent e)
  {
    viewport.setShowDBRefs(showDbRefsMenuitem.isSelected());
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.jbgui.GAlignFrame#showNpFeats_actionPerformed(java.awt.event.
   * ActionEvent)
   */
  @Override
  protected void showNpFeats_actionPerformed(ActionEvent e)
  {
    viewport.setShowNPFeats(showNpFeatsMenuitem.isSelected());
  }

  /**
   * find the viewport amongst the tabs in this alignment frame and close that
   * tab
   * 
   * @param av
   */
  public boolean closeView(AlignViewportI av)
  {
    if (viewport == av)
    {
      this.closeMenuItem_actionPerformed(false);
      return true;
    }
    Component[] comp = tabbedPane.getComponents();
    for (int i = 0; comp != null && i < comp.length; i++)
    {
      if (comp[i] instanceof AlignmentPanel)
      {
        if (((AlignmentPanel) comp[i]).av == av)
        {
          // close the view.
          closeView((AlignmentPanel) comp[i]);
          return true;
        }
      }
    }
    return false;
  }

  protected void build_fetchdbmenu(JMenu webService)
  {
    // Temporary hack - DBRef Fetcher always top level ws entry.
    // TODO We probably want to store a sequence database checklist in
    // preferences and have checkboxes.. rather than individual sources selected
    // here
    final JMenu rfetch = new JMenu(
            MessageManager.getString("action.fetch_db_references"));
    rfetch.setToolTipText(MessageManager.getString(
            "label.retrieve_parse_sequence_database_records_alignment_or_selected_sequences"));
    webService.add(rfetch);

    final JCheckBoxMenuItem trimrs = new JCheckBoxMenuItem(
            MessageManager.getString("option.trim_retrieved_seqs"));
    trimrs.setToolTipText(
            MessageManager.getString("label.trim_retrieved_sequences"));
    trimrs.setSelected(
            Cache.getDefault(DBRefFetcher.TRIM_RETRIEVED_SEQUENCES, true));
    trimrs.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        trimrs.setSelected(trimrs.isSelected());
        Cache.setProperty(DBRefFetcher.TRIM_RETRIEVED_SEQUENCES,
                Boolean.valueOf(trimrs.isSelected()).toString());
      }
    });
    rfetch.add(trimrs);
    JMenuItem fetchr = new JMenuItem(
            MessageManager.getString("label.standard_databases"));
    fetchr.setToolTipText(
            MessageManager.getString("label.fetch_embl_uniprot"));
    fetchr.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            boolean isNucleotide = alignPanel.alignFrame.getViewport()
                    .getAlignment().isNucleotide();
            DBRefFetcher dbRefFetcher = new DBRefFetcher(
                    alignPanel.av.getSequenceSelection(),
                    alignPanel.alignFrame, null,
                    alignPanel.alignFrame.featureSettings, isNucleotide);
            dbRefFetcher.addListener(new FetchFinishedListenerI()
            {
              @Override
              public void finished()
              {

                for (FeatureSettingsModelI srcSettings : dbRefFetcher
                        .getFeatureSettingsModels())
                {

                  alignPanel.av.mergeFeaturesStyle(srcSettings);
                }
                AlignFrame.this.setMenusForViewport();
              }
            });
            dbRefFetcher.fetchDBRefs(false);
          }
        }).start();

      }

    });
    rfetch.add(fetchr);
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        final jalview.ws.SequenceFetcher sf = jalview.gui.SequenceFetcher
                .getSequenceFetcherSingleton();
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            String[] dbclasses = sf.getNonAlignmentSources();
            List<DbSourceProxy> otherdb;
            JMenu dfetch = new JMenu();
            JMenu ifetch = new JMenu();
            JMenuItem fetchr = null;
            int comp = 0, icomp = 0, mcomp = 15;
            String mname = null;
            int dbi = 0;
            for (String dbclass : dbclasses)
            {
              otherdb = sf.getSourceProxy(dbclass);
              // add a single entry for this class, or submenu allowing 'fetch
              // all' or pick one
              if (otherdb == null || otherdb.size() < 1)
              {
                continue;
              }
              if (mname == null)
              {
                mname = "From " + dbclass;
              }
              if (otherdb.size() == 1)
              {
                final DbSourceProxy[] dassource = otherdb
                        .toArray(new DbSourceProxy[0]);
                DbSourceProxy src = otherdb.get(0);
                fetchr = new JMenuItem(src.getDbSource());
                fetchr.addActionListener(new ActionListener()
                {

                  @Override
                  public void actionPerformed(ActionEvent e)
                  {
                    new Thread(new Runnable()
                    {

                      @Override
                      public void run()
                      {
                        boolean isNucleotide = alignPanel.alignFrame
                                .getViewport().getAlignment()
                                .isNucleotide();
                        DBRefFetcher dbRefFetcher = new DBRefFetcher(
                                alignPanel.av.getSequenceSelection(),
                                alignPanel.alignFrame, dassource,
                                alignPanel.alignFrame.featureSettings,
                                isNucleotide);
                        dbRefFetcher
                                .addListener(new FetchFinishedListenerI()
                                {
                                  @Override
                                  public void finished()
                                  {
                                    FeatureSettingsModelI srcSettings = dassource[0]
                                            .getFeatureColourScheme();
                                    alignPanel.av.mergeFeaturesStyle(
                                            srcSettings);
                                    AlignFrame.this.setMenusForViewport();
                                  }
                                });
                        dbRefFetcher.fetchDBRefs(false);
                      }
                    }).start();
                  }

                });
                fetchr.setToolTipText(JvSwingUtils.wrapTooltip(true,
                        MessageManager.formatMessage(
                                "label.fetch_retrieve_from", new Object[]
                                { src.getDbName() })));
                dfetch.add(fetchr);
                comp++;
              }
              else
              {
                final DbSourceProxy[] dassource = otherdb
                        .toArray(new DbSourceProxy[0]);
                // fetch all entry
                DbSourceProxy src = otherdb.get(0);
                fetchr = new JMenuItem(MessageManager
                        .formatMessage("label.fetch_all_param", new Object[]
                        { src.getDbSource() }));
                fetchr.addActionListener(new ActionListener()
                {
                  @Override
                  public void actionPerformed(ActionEvent e)
                  {
                    new Thread(new Runnable()
                    {

                      @Override
                      public void run()
                      {
                        boolean isNucleotide = alignPanel.alignFrame
                                .getViewport().getAlignment()
                                .isNucleotide();
                        DBRefFetcher dbRefFetcher = new DBRefFetcher(
                                alignPanel.av.getSequenceSelection(),
                                alignPanel.alignFrame, dassource,
                                alignPanel.alignFrame.featureSettings,
                                isNucleotide);
                        dbRefFetcher
                                .addListener(new FetchFinishedListenerI()
                                {
                                  @Override
                                  public void finished()
                                  {
                                    AlignFrame.this.setMenusForViewport();
                                  }
                                });
                        dbRefFetcher.fetchDBRefs(false);
                      }
                    }).start();
                  }
                });

                fetchr.setToolTipText(JvSwingUtils.wrapTooltip(true,
                        MessageManager.formatMessage(
                                "label.fetch_retrieve_from_all_sources",
                                new Object[]
                                { Integer.valueOf(otherdb.size())
                                        .toString(),
                                    src.getDbSource(), src.getDbName() })));
                dfetch.add(fetchr);
                comp++;
                // and then build the rest of the individual menus
                ifetch = new JMenu(MessageManager.formatMessage(
                        "label.source_from_db_source", new Object[]
                        { src.getDbSource() }));
                icomp = 0;
                String imname = null;
                int i = 0;
                for (DbSourceProxy sproxy : otherdb)
                {
                  String dbname = sproxy.getDbName();
                  String sname = dbname.length() > 5
                          ? dbname.substring(0, 5) + "..."
                          : dbname;
                  String msname = dbname.length() > 10
                          ? dbname.substring(0, 10) + "..."
                          : dbname;
                  if (imname == null)
                  {
                    imname = MessageManager
                            .formatMessage("label.from_msname", new Object[]
                            { sname });
                  }
                  fetchr = new JMenuItem(msname);
                  final DbSourceProxy[] dassrc = { sproxy };
                  fetchr.addActionListener(new ActionListener()
                  {

                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                      new Thread(new Runnable()
                      {

                        @Override
                        public void run()
                        {
                          boolean isNucleotide = alignPanel.alignFrame
                                  .getViewport().getAlignment()
                                  .isNucleotide();
                          DBRefFetcher dbRefFetcher = new DBRefFetcher(
                                  alignPanel.av.getSequenceSelection(),
                                  alignPanel.alignFrame, dassrc,
                                  alignPanel.alignFrame.featureSettings,
                                  isNucleotide);
                          dbRefFetcher
                                  .addListener(new FetchFinishedListenerI()
                                  {
                                    @Override
                                    public void finished()
                                    {
                                      AlignFrame.this.setMenusForViewport();
                                    }
                                  });
                          dbRefFetcher.fetchDBRefs(false);
                        }
                      }).start();
                    }

                  });
                  fetchr.setToolTipText(
                          "<html>" + MessageManager.formatMessage(
                                  "label.fetch_retrieve_from", new Object[]
                                  { dbname }));
                  ifetch.add(fetchr);
                  ++i;
                  if (++icomp >= mcomp || i == (otherdb.size()))
                  {
                    ifetch.setText(MessageManager.formatMessage(
                            "label.source_to_target", imname, sname));
                    dfetch.add(ifetch);
                    ifetch = new JMenu();
                    imname = null;
                    icomp = 0;
                    comp++;
                  }
                }
              }
              ++dbi;
              if (comp >= mcomp || dbi >= (dbclasses.length))
              {
                dfetch.setText(MessageManager.formatMessage(
                        "label.source_to_target", mname, dbclass));
                rfetch.add(dfetch);
                dfetch = new JMenu();
                mname = null;
                comp = 0;
              }
            }
          }
        });
      }
    }).start();

  }

  /**
   * Left justify the whole alignment.
   */
  @Override
  protected void justifyLeftMenuItem_actionPerformed(ActionEvent e)
  {
    AlignmentI al = viewport.getAlignment();
    al.justify(false);
    viewport.firePropertyChange("alignment", null, al);
  }

  /**
   * Right justify the whole alignment.
   */
  @Override
  protected void justifyRightMenuItem_actionPerformed(ActionEvent e)
  {
    AlignmentI al = viewport.getAlignment();
    al.justify(true);
    viewport.firePropertyChange("alignment", null, al);
  }

  @Override
  public void setShowSeqFeatures(boolean b)
  {
    showSeqFeatures.setSelected(b);
    viewport.setShowSequenceFeatures(b);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#showUnconservedMenuItem_actionPerformed(java.
   * awt.event.ActionEvent)
   */
  @Override
  protected void showUnconservedMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setShowUnconserved(showNonconservedMenuItem.getState());
    alignPanel.paintAlignment(false, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#showGroupConsensus_actionPerformed(java.awt.event
   * .ActionEvent)
   */
  @Override
  protected void showGroupConsensus_actionPerformed(ActionEvent e)
  {
    viewport.setShowGroupConsensus(showGroupConsensus.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#showGroupConservation_actionPerformed(java.awt
   * .event.ActionEvent)
   */
  @Override
  protected void showGroupConservation_actionPerformed(ActionEvent e)
  {
    viewport.setShowGroupConservation(showGroupConservation.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#showConsensusHistogram_actionPerformed(java.awt
   * .event.ActionEvent)
   */
  @Override
  protected void showConsensusHistogram_actionPerformed(ActionEvent e)
  {
    viewport.setShowConsensusHistogram(showConsensusHistogram.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#showConsensusProfile_actionPerformed(java.awt
   * .event.ActionEvent)
   */
  @Override
  protected void showSequenceLogo_actionPerformed(ActionEvent e)
  {
    viewport.setShowSequenceLogo(showSequenceLogo.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  @Override
  protected void normaliseSequenceLogo_actionPerformed(ActionEvent e)
  {
    showSequenceLogo.setState(true);
    viewport.setShowSequenceLogo(true);
    viewport.setNormaliseSequenceLogo(normaliseSequenceLogo.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  @Override
  protected void applyAutoAnnotationSettings_actionPerformed(ActionEvent e)
  {
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GAlignFrame#makeGrpsFromSelection_actionPerformed(java.awt
   * .event.ActionEvent)
   */
  @Override
  protected void makeGrpsFromSelection_actionPerformed(ActionEvent e)
  {
    if (avc.makeGroupsFromSelection())
    {
      PaintRefresher.Refresh(this, viewport.getSequenceSetId());
      alignPanel.updateAnnotation();
      alignPanel.paintAlignment(true,
              viewport.needToUpdateStructureViews());
    }
  }

  public void clearAlignmentSeqRep()
  {
    // TODO refactor alignmentseqrep to controller
    if (viewport.getAlignment().hasSeqrep())
    {
      viewport.getAlignment().setSeqrep(null);
      PaintRefresher.Refresh(this, viewport.getSequenceSetId());
      alignPanel.updateAnnotation();
      alignPanel.paintAlignment(true, true);
    }
  }

  @Override
  protected void createGroup_actionPerformed(ActionEvent e)
  {
    if (avc.createGroup())
    {
      if (applyAutoAnnotationSettings.isSelected())
      {
        alignPanel.updateAnnotation(true, false);
      }
      alignPanel.alignmentChanged();
    }
  }

  @Override
  protected void unGroup_actionPerformed(ActionEvent e)
  {
    if (avc.unGroup())
    {
      alignPanel.alignmentChanged();
    }
  }

  /**
   * make the given alignmentPanel the currently selected tab
   * 
   * @param alignmentPanel
   */
  public void setDisplayedView(AlignmentPanel alignmentPanel)
  {
    if (!viewport.getSequenceSetId()
            .equals(alignmentPanel.av.getSequenceSetId()))
    {
      throw new Error(MessageManager.getString(
              "error.implementation_error_cannot_show_view_alignment_frame"));
    }
    if (tabbedPane != null && tabbedPane.getTabCount() > 0 && alignPanels
            .indexOf(alignmentPanel) != tabbedPane.getSelectedIndex())
    {
      tabbedPane.setSelectedIndex(alignPanels.indexOf(alignmentPanel));
    }
  }

  /**
   * Action on selection of menu options to Show or Hide annotations.
   * 
   * @param visible
   * @param forSequences
   *          update sequence-related annotations
   * @param forAlignment
   *          update non-sequence-related annotations
   */
  @Override
  public void setAnnotationsVisibility(boolean visible,
          boolean forSequences, boolean forAlignment)
  {
    AlignmentAnnotation[] anns = alignPanel.getAlignment()
            .getAlignmentAnnotation();
    if (anns == null)
    {
      return;
    }
    for (AlignmentAnnotation aa : anns)
    {
      /*
       * don't display non-positional annotations on an alignment
       */
      if (aa.annotations == null)
      {
        continue;
      }
      boolean apply = (aa.sequenceRef == null && forAlignment)
              || (aa.sequenceRef != null && forSequences);
      if (apply)
      {
        aa.visible = visible;
      }
    }
    alignPanel.validateAnnotationDimensions(true);
    alignPanel.alignmentChanged();
  }

  /**
   * Store selected annotation sort order for the view and repaint.
   */
  @Override
  protected void sortAnnotations_actionPerformed()
  {
    this.alignPanel.av.setSortAnnotationsBy(getAnnotationSortOrder());
    this.alignPanel.av
            .setShowAutocalculatedAbove(isShowAutoCalculatedAbove());
    alignPanel.paintAlignment(false, false);
  }

  /**
   * 
   * @return alignment panels in this alignment frame
   */
  public List<? extends AlignmentViewPanel> getAlignPanels()
  {
    // alignPanels is never null
    // return alignPanels == null ? Arrays.asList(alignPanel) : alignPanels;
    return alignPanels;
  }

  /**
   * Open a new alignment window, with the cDNA associated with this (protein)
   * alignment, aligned as is the protein.
   */
  protected void viewAsCdna_actionPerformed()
  {
    // TODO no longer a menu action - refactor as required
    final AlignmentI alignment = getViewport().getAlignment();
    List<AlignedCodonFrame> mappings = alignment.getCodonFrames();
    if (mappings == null)
    {
      return;
    }
    List<SequenceI> cdnaSeqs = new ArrayList<>();
    for (SequenceI aaSeq : alignment.getSequences())
    {
      for (AlignedCodonFrame acf : mappings)
      {
        SequenceI dnaSeq = acf.getDnaForAaSeq(aaSeq.getDatasetSequence());
        if (dnaSeq != null)
        {
          /*
           * There is a cDNA mapping for this protein sequence - add to new
           * alignment. It will share the same dataset sequence as other mapped
           * cDNA (no new mappings need to be created).
           */
          final Sequence newSeq = new Sequence(dnaSeq);
          newSeq.setDatasetSequence(dnaSeq);
          cdnaSeqs.add(newSeq);
        }
      }
    }
    if (cdnaSeqs.size() == 0)
    {
      // show a warning dialog no mapped cDNA
      return;
    }
    AlignmentI cdna = new Alignment(
            cdnaSeqs.toArray(new SequenceI[cdnaSeqs.size()]));
    GAlignFrame alignFrame = new AlignFrame(cdna, AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);
    cdna.alignAs(alignment);
    String newtitle = "cDNA " + MessageManager.getString("label.for") + " "
            + this.title;
    Desktop.addInternalFrame(alignFrame, newtitle, AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);
  }

  /**
   * Set visibility of dna/protein complement view (available when shown in a
   * split frame).
   * 
   * @param show
   */
  @Override
  protected void showComplement_actionPerformed(boolean show)
  {
    SplitContainerI sf = getSplitViewContainer();
    if (sf != null)
    {
      sf.setComplementVisible(this, show);
    }
  }

  /**
   * Generate the reverse (optionally complemented) of the selected sequences,
   * and add them to the alignment
   */
  @Override
  protected void showReverse_actionPerformed(boolean complement)
  {
    AlignmentI al = null;
    try
    {
      Dna dna = new Dna(viewport, viewport.getViewAsVisibleContigs(true));
      al = dna.reverseCdna(complement);
      viewport.addAlignment(al, "");
      addHistoryItem(new EditCommand(
              MessageManager.getString("label.add_sequences"), Action.PASTE,
              al.getSequencesArray(), 0, al.getWidth(),
              viewport.getAlignment()));
    } catch (Exception ex)
    {
      System.err.println(ex.getMessage());
      return;
    }
  }

  /**
   * Try to run a script in the Groovy console, having first ensured that this
   * AlignFrame is set as currentAlignFrame in Desktop, to allow the script to
   * be targeted at this alignment.
   */
  @Override
  protected void runGroovy_actionPerformed()
  {
    Jalview.setCurrentAlignFrame(this);
    groovy.ui.Console console = Desktop.getGroovyConsole();
    if (console != null)
    {
      try
      {
        console.runScript();
      } catch (Exception ex)
      {
        System.err.println((ex.toString()));
        JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                MessageManager.getString("label.couldnt_run_groovy_script"),
                MessageManager.getString("label.groovy_support_failed"),
                JvOptionPane.ERROR_MESSAGE);
      }
    }
    else
    {
      System.err.println("Can't run Groovy script as console not found");
    }
  }

  /**
   * Hides columns containing (or not containing) a specified feature, provided
   * that would not leave all columns hidden
   * 
   * @param featureType
   * @param columnsContaining
   * @return
   */
  public boolean hideFeatureColumns(String featureType,
          boolean columnsContaining)
  {
    boolean notForHiding = avc.markColumnsContainingFeatures(
            columnsContaining, false, false, featureType);
    if (notForHiding)
    {
      if (avc.markColumnsContainingFeatures(!columnsContaining, false,
              false, featureType))
      {
        getViewport().hideSelectedColumns();
        return true;
      }
    }
    return false;
  }

  @Override
  protected void selectHighlightedColumns_actionPerformed(
          ActionEvent actionEvent)
  {
    // include key modifier check in case user selects from menu
    avc.markHighlightedColumns(
            (actionEvent.getModifiers() & ActionEvent.ALT_MASK) != 0, true,
            (actionEvent.getModifiers() & (ActionEvent.META_MASK
                    | ActionEvent.CTRL_MASK)) != 0);
  }

  @Override
  protected void copyHighlightedColumns_actionPerformed(
          ActionEvent actionEvent)
  {
    avc.copyHighlightedRegionsToClipboard();
  }

  /**
   * Rebuilds the Colour menu, including any user-defined colours which have
   * been loaded either on startup or during the session
   */
  public void buildColourMenu()
  {
    colourMenu.removeAll();

    colourMenu.add(applyToAllGroups);
    colourMenu.add(textColour);
    colourMenu.addSeparator();

    ButtonGroup bg = ColourMenuHelper.addMenuItems(colourMenu, this,
            viewport.getAlignment(), false);

    colourMenu.add(annotationColour);
    bg.add(annotationColour);
    colourMenu.addSeparator();
    colourMenu.add(conservationMenuItem);
    colourMenu.add(modifyConservation);
    colourMenu.add(abovePIDThreshold);
    colourMenu.add(modifyPID);

    ColourSchemeI colourScheme = viewport.getGlobalColourScheme();
    ColourMenuHelper.setColourSelected(colourMenu, colourScheme);
  }

  /**
   * Open a dialog (if not already open) that allows the user to select and
   * calculate PCA or Tree analysis
   */
  protected void openTreePcaDialog()
  {
    if (alignPanel.getCalculationDialog() == null)
    {
      new CalculationChooser(AlignFrame.this);
    }
  }
  
  /**
   * Open the dialog (if not already open) of the custom calculation
   */
  protected void openCustomDialog()
  {
    if (alignPanel.getCustomDialog() == null)
    {
      new CustomChooser(AlignFrame.this);
    }
  }

  @Override
  protected void loadVcf_actionPerformed()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.getString("label.load_vcf_file"));
    chooser.setToolTipText(MessageManager.getString("label.load_vcf_file"));
    final AlignFrame us = this;
    chooser.setResponseHandler(0, () -> {
      String choice = chooser.getSelectedFile().getPath();
      Cache.setProperty("LAST_DIRECTORY", choice);
      SequenceI[] seqs = viewport.getAlignment().getSequencesArray();
      new VCFLoader(choice).loadVCF(seqs, us);
    });
    chooser.showOpenDialog(null);

  }
  
  //&!
  @Override
  protected void loadVcfWithInfo_actionPerformed()
  {
    new VcfInfoInput(this);
  }
  
  private Rectangle lastFeatureSettingsBounds = null;

  @Override
  public void setFeatureSettingsGeometry(Rectangle bounds)
  {
    lastFeatureSettingsBounds = bounds;
  }

  @Override
  public Rectangle getFeatureSettingsGeometry()
  {
    return lastFeatureSettingsBounds;
  }

}

class PrintThread extends Thread
{
  AlignmentPanel ap;

  public PrintThread(AlignmentPanel ap)
  {
    this.ap = ap;
  }

  static PageFormat pf;

  @Override
  public void run()
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();

    if (pf != null)
    {
      printJob.setPrintable(ap, pf);
    }
    else
    {
      printJob.setPrintable(ap);
    }

    if (printJob.printDialog())
    {
      try
      {
        printJob.print();
      } catch (Exception PrintException)
      {
        PrintException.printStackTrace();
      }
    }
  }
}
