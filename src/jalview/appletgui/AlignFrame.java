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
package jalview.appletgui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jmol.viewer.Viewer;

import jalview.analysis.AlignmentSorter;
import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.analysis.TreeBuilder;
import jalview.analysis.scoremodels.PIDModel;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.api.AlignViewControllerGuiI;
import jalview.api.AlignViewControllerI;
import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.api.FeatureRenderer;
import jalview.api.FeatureSettingsControllerI;
import jalview.api.SequenceStructureBinding;
import jalview.bin.JalviewLite;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.OrderCommand;
import jalview.commands.RemoveGapColCommand;
import jalview.commands.RemoveGapsCommand;
import jalview.commands.SlideSequencesCommand;
import jalview.commands.TrimRegionCommand;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentOrder;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.AnnotationFile;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.FeaturesFile;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.TCoffeeScoreFile;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ClustalxColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.PurinePyrimidineColourScheme;
import jalview.schemes.RNAHelicesColour;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TCoffeeColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.ViewportRanges;

public class AlignFrame extends EmbmenuFrame implements ActionListener,
        ItemListener, KeyListener, AlignViewControllerGuiI
{
  public AlignViewControllerI avc;

  public AlignmentPanel alignPanel;

  public AlignViewport viewport;

  // width and height may be overridden by applet parameters
  int frameWidth = 700;

  int frameHeight = 500;

  String jalviewServletURL;

  /*
   * Flag for showing autocalculated consensus above or below other consensus
   * rows
   */
  private boolean showAutoCalculatedAbove;

  private SequenceAnnotationOrder annotationSortOrder;

  /**
   * Constructor that creates the frame and adds it to the display.
   * 
   * @param al
   * @param applet
   * @param title
   * @param embedded
   */
  public AlignFrame(AlignmentI al, JalviewLite applet, String title,
          boolean embedded)
  {
    this(al, applet, title, embedded, true);
  }

  /**
   * Constructor that optionally allows the frame to be displayed or only
   * created.
   * 
   * @param al
   * @param applet
   * @param title
   * @param embedded
   * @param addToDisplay
   */
  public AlignFrame(AlignmentI al, JalviewLite applet, String title,
          boolean embedded, boolean addToDisplay)
  {
    this(al, null, null, applet, title, embedded, addToDisplay);
  }

  public AlignFrame(AlignmentI al, SequenceI[] hiddenSeqs,
          HiddenColumns hidden, JalviewLite applet, String title,
          boolean embedded)
  {
    this(al, hiddenSeqs, hidden, applet, title, embedded, true);
  }

  public AlignFrame(AlignmentI al, SequenceI[] hiddenSeqs,
          HiddenColumns hidden, JalviewLite applet, String title,
          boolean embedded, boolean addToDisplay)
  {
    if (applet != null)
    {
      jalviewServletURL = applet.getParameter("APPLICATION_URL");
    }

    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
    // need to get window geometry before we calculate alignment layout
    if (applet != null)
    {
      String param;
      try
      {
        param = applet.getParameter("windowWidth");
        if (param != null)
        {
          int width = Integer.parseInt(param);
          frameWidth = width;
        }
        param = applet.getParameter("windowHeight");
        if (param != null)
        {
          int height = Integer.parseInt(param);
          frameHeight = height;
        }
      } catch (Exception ex)
      {
      }
    }
    viewport = new AlignViewport(al, applet);

    if (hiddenSeqs != null && hiddenSeqs.length > 0)
    {
      viewport.hideSequence(hiddenSeqs);
    }
    if (hidden != null)
    {
      viewport.getAlignment().setHiddenColumns(hidden);
    }
    viewport.setScaleAboveWrapped(scaleAbove.getState());

    alignPanel = new AlignmentPanel(this, viewport);
    avc = new jalview.controller.AlignViewController(this, viewport,
            alignPanel);
    viewport.updateConservation(alignPanel);
    viewport.updateConsensus(alignPanel);

    displayNonconservedMenuItem.setState(viewport.getShowUnconserved());
    followMouseOverFlag.setState(viewport.isFollowHighlight());
    showGroupConsensus.setState(viewport.isShowGroupConsensus());
    showGroupConservation.setState(viewport.isShowGroupConservation());
    showConsensusHistogram.setState(viewport.isShowConsensusHistogram());
    showSequenceLogo.setState(viewport.isShowSequenceLogo());
    normSequenceLogo.setState(viewport.isNormaliseSequenceLogo());
    applyToAllGroups.setState(viewport.getColourAppliesToAllGroups());
    annotationPanelMenuItem.setState(viewport.isShowAnnotation());
    showAlignmentAnnotations.setEnabled(annotationPanelMenuItem.getState());
    showSequenceAnnotations.setEnabled(annotationPanelMenuItem.getState());
    showAlignmentAnnotations.setState(true);
    showSequenceAnnotations.setState(false);

    seqLimits.setState(viewport.getShowJVSuffix());

    if (applet != null)
    {
      String param = applet.getParameter("sortBy");
      if (param != null)
      {
        if (param.equalsIgnoreCase("Id"))
        {
          sortIDMenuItem_actionPerformed();
        }
        else if (param.equalsIgnoreCase("Pairwise Identity"))
        {
          sortPairwiseMenuItem_actionPerformed();
        }
        else if (param.equalsIgnoreCase("Length"))
        {
          sortLengthMenuItem_actionPerformed();
        }
      }

      param = applet.getParameter("wrap");
      if (param != null)
      {
        if (param.equalsIgnoreCase("true"))
        {
          wrapMenuItem.setState(true);
          wrapMenuItem_actionPerformed();
        }
      }
      param = applet.getParameter("centrecolumnlabels");
      if (param != null)
      {
        centreColumnLabelFlag.setState(true);
        centreColumnLabelFlag_stateChanged();
      }

    }
    if (viewport.getAlignment().isNucleotide())
    {
      conservationMenuItem.setEnabled(false);
      clustalColour.setEnabled(false);
      BLOSUM62Colour.setEnabled(false);
      zappoColour.setEnabled(false);
      taylorColour.setEnabled(false);
      hydrophobicityColour.setEnabled(false);
      helixColour.setEnabled(false);
      strandColour.setEnabled(false);
      turnColour.setEnabled(false);
      buriedColour.setEnabled(false);
      viewport.updateStrucConsensus(alignPanel);
      if (viewport.getAlignment().hasRNAStructure())
      {
        RNAHelixColour.setEnabled(true);
      }
      else
      {
        RNAHelixColour.setEnabled(false);
      }
    }
    else
    {
      RNAHelixColour.setEnabled(false);
      purinePyrimidineColour.setEnabled(false);
      nucleotideColour.setEnabled(false);
    }
    // Some JVMS send keyevents to Top frame or lowest panel,
    // Havent worked out why yet. So add to both this frame and seqCanvas for
    // now
    this.addKeyListener(this);
    alignPanel.seqPanel.seqCanvas.addKeyListener(this);
    alignPanel.idPanel.idCanvas.addKeyListener(this);
    alignPanel.scalePanel.addKeyListener(this);
    alignPanel.annotationPanel.addKeyListener(this);
    alignPanel.annotationPanelHolder.addKeyListener(this);
    alignPanel.annotationSpaceFillerHolder.addKeyListener(this);
    alignPanel.alabels.addKeyListener(this);

    setAnnotationsVisibility();

    if (addToDisplay)
    {
      addToDisplay(embedded);
    }
  }

  /**
   * @param embedded
   */
  public void addToDisplay(boolean embedded)
  {
    createAlignFrameWindow(embedded);
    validate();
    alignPanel.adjustAnnotationHeight();
    alignPanel.paintAlignment(true, true);
  }

  public AlignViewport getAlignViewport()
  {
    return viewport;
  }

  public SeqCanvas getSeqcanvas()
  {
    return alignPanel.seqPanel.seqCanvas;
  }

  /**
   * Load a features file onto the alignment
   * 
   * @param file
   *          file URL, content, or other resolvable path
   * @param type
   *          is protocol for accessing data referred to by file
   */

  public boolean parseFeaturesFile(String file, DataSourceType type)
  {
    return parseFeaturesFile(file, type, true);
  }

  /**
   * Load a features file onto the alignment
   * 
   * @param file
   *          file URL, content, or other resolvable path
   * @param sourceType
   *          is protocol for accessing data referred to by file
   * @param autoenabledisplay
   *          when true, display features flag will be automatically enabled if
   *          features are loaded
   * @return true if data parsed as a features file
   */
  public boolean parseFeaturesFile(String file, DataSourceType sourceType,
          boolean autoenabledisplay)
  {
    boolean featuresFile = false;
    try
    {
      Map<String, FeatureColourI> colours = alignPanel.seqPanel.seqCanvas
              .getFeatureRenderer().getFeatureColours();
      boolean relaxedIdMatching = viewport.applet
              .getDefaultParameter("relaxedidmatch", false);
      featuresFile = new FeaturesFile(file, sourceType).parse(
              viewport.getAlignment(), colours, true, relaxedIdMatching);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    if (featuresFile)
    {
      if (autoenabledisplay)
      {
        viewport.setShowSequenceFeatures(true);
        sequenceFeatures.setState(true);
      }
      if (alignPanel.seqPanel.seqCanvas.fr != null)
      {
        // update the min/max ranges where necessary
        alignPanel.seqPanel.seqCanvas.fr.findAllFeatures(true);
      }
      if (viewport.featureSettings != null)
      {
        viewport.featureSettings.refreshTable();
      }
      alignPanel.paintAlignment(true, true);
      setStatus(MessageManager
              .getString("label.successfully_added_features_alignment"));
    }
    return featuresFile;
  }

  @Override
  public void keyPressed(KeyEvent evt)
  {
    ViewportRanges ranges = viewport.getRanges();

    if (viewport.cursorMode
            && ((evt.getKeyCode() >= KeyEvent.VK_0
                    && evt.getKeyCode() <= KeyEvent.VK_9)
                    || (evt.getKeyCode() >= KeyEvent.VK_NUMPAD0
                            && evt.getKeyCode() <= KeyEvent.VK_NUMPAD9))
            && Character.isDigit(evt.getKeyChar()))
    {
      alignPanel.seqPanel.numberPressed(evt.getKeyChar());
    }

    switch (evt.getKeyCode())
    {
    case 27: // escape key
      deselectAllSequenceMenuItem_actionPerformed();

      alignPanel.alabels.cancelDrag();
      break;
    case KeyEvent.VK_X:
      if (evt.isControlDown() || evt.isMetaDown())
      {
        cut_actionPerformed();
      }
      break;
    case KeyEvent.VK_C:
      if (viewport.cursorMode && !evt.isControlDown())
      {
        alignPanel.seqPanel.setCursorColumn();
      }
      if (evt.isControlDown() || evt.isMetaDown())
      {
        copy_actionPerformed();
      }
      break;
    case KeyEvent.VK_V:
      if (evt.isControlDown())
      {
        paste(evt.isShiftDown());
      }
      break;
    case KeyEvent.VK_A:
      if (evt.isControlDown() || evt.isMetaDown())
      {
        selectAllSequenceMenuItem_actionPerformed();
      }
      break;
    case KeyEvent.VK_DOWN:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.moveCursor(0, 1);
      }
      else
      {
        moveSelectedSequences(false);
      }
      break;

    case KeyEvent.VK_UP:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.moveCursor(0, -1);
      }
      else
      {
        moveSelectedSequences(true);
      }
      break;

    case KeyEvent.VK_LEFT:
      if (evt.isAltDown() || !viewport.cursorMode)
      {
        slideSequences(false, alignPanel.seqPanel.getKeyboardNo1());
      }
      else
      {
        alignPanel.seqPanel.moveCursor(-1, 0);
      }
      break;

    case KeyEvent.VK_RIGHT:
      if (evt.isAltDown() || !viewport.cursorMode)
      {
        slideSequences(true, alignPanel.seqPanel.getKeyboardNo1());
      }
      else
      {
        alignPanel.seqPanel.moveCursor(1, 0);
      }
      break;

    case KeyEvent.VK_SPACE:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.insertGapAtCursor(evt.isControlDown()
                || evt.isShiftDown() || evt.isAltDown());
      }
      break;

    case KeyEvent.VK_DELETE:
    case KeyEvent.VK_BACK_SPACE:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.deleteGapAtCursor(evt.isControlDown()
                || evt.isShiftDown() || evt.isAltDown());
      }
      else
      {
        cut_actionPerformed();
        alignPanel.seqPanel.seqCanvas.repaint();
      }
      break;

    case KeyEvent.VK_S:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.setCursorRow();
      }
      break;
    case KeyEvent.VK_P:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.setCursorPosition();
      }
      break;

    case KeyEvent.VK_ENTER:
    case KeyEvent.VK_COMMA:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.setCursorRowAndColumn();
      }
      break;

    case KeyEvent.VK_Q:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.setSelectionAreaAtCursor(true);
      }
      break;
    case KeyEvent.VK_M:
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.setSelectionAreaAtCursor(false);
      }
      break;

    case KeyEvent.VK_F2:
      viewport.cursorMode = !viewport.cursorMode;
      setStatus(MessageManager.formatMessage("label.keyboard_editing_mode",
              new String[]
              { (viewport.cursorMode ? "on" : "off") }));
      if (viewport.cursorMode)
      {
        alignPanel.seqPanel.seqCanvas.cursorX = ranges.getStartRes();
        alignPanel.seqPanel.seqCanvas.cursorY = ranges.getStartSeq();
      }
      break;

    case KeyEvent.VK_F:
      if (evt.isControlDown())
      {
        findMenuItem_actionPerformed();
      }
      break;

    case KeyEvent.VK_H:
    {
      boolean toggleSeqs = !evt.isControlDown();
      boolean toggleCols = !evt.isShiftDown();
      toggleHiddenRegions(toggleSeqs, toggleCols);
      break;
    }

    case KeyEvent.VK_PAGE_UP:
      ranges.pageUp();
      break;

    case KeyEvent.VK_PAGE_DOWN:
      ranges.pageDown();
      break;

    case KeyEvent.VK_Z:
      if (evt.isControlDown())
      {
        undoMenuItem_actionPerformed();
      }
      break;

    case KeyEvent.VK_Y:
      if (evt.isControlDown())
      {
        redoMenuItem_actionPerformed();
      }
      break;

    case KeyEvent.VK_L:
      if (evt.isControlDown())
      {
        trimAlignment(true);
      }
      break;

    case KeyEvent.VK_R:
      if (evt.isControlDown())
      {
        trimAlignment(false);
      }
      break;

    case KeyEvent.VK_E:
      if (evt.isControlDown())
      {
        if (evt.isShiftDown())
        {
          this.removeAllGapsMenuItem_actionPerformed();
        }
        else
        {
          removeGappedColumnMenuItem_actionPerformed();
        }
      }
      break;
    case KeyEvent.VK_I:
      if (evt.isControlDown())
      {
        if (evt.isAltDown())
        {
          invertColSel_actionPerformed();
        }
        else
        {
          invertSequenceMenuItem_actionPerformed();
        }
      }
      break;

    case KeyEvent.VK_G:
      if (evt.isControlDown())
      {
        if (evt.isShiftDown())
        {
          this.unGroup_actionPerformed();
        }
        else
        {
          this.createGroup_actionPerformed();
        }
      }
      break;

    case KeyEvent.VK_U:
      if (evt.isControlDown())
      {
        this.deleteGroups_actionPerformed();
      }
      break;

    case KeyEvent.VK_T:
      if (evt.isControlDown())
      {
        newView(null);
      }
      break;

    }
    // TODO: repaint flags set only if the keystroke warrants it
    alignPanel.paintAlignment(true, true);
  }

  /**
   * called by key handler and the hide all/show all menu items
   * 
   * @param toggleSeqs
   * @param toggleCols
   */
  private void toggleHiddenRegions(boolean toggleSeqs, boolean toggleCols)
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
          invertSequenceMenuItem_actionPerformed();
          sg = viewport.getSelectionGroup();
          toggleSeqs = true;

        }
        viewport.expandColSelection(sg, true);
        // finally invert the column selection and get the new sequence
        // selection and indicate it should be hidden.
        invertColSel_actionPerformed();
        toggleCols = true;
      }
    }

    if (toggleSeqs)
    {
      if (sg != null && sg.getSize() != viewport.getAlignment().getHeight())
      {
        hide = true;
        viewport.hideAllSelectedSeqs();
      }
      else if (!(toggleCols && viewport.hasSelectedColumns()))
      {
        viewport.showAllHiddenSeqs();
      }
    }

    if (toggleCols)
    {
      if (viewport.hasSelectedColumns())
      {
        viewport.hideSelectedColumns();
        if (!toggleSeqs)
        {
          viewport.setSelectionGroup(sg);
        }
      }
      else if (!hide)
      {
        viewport.showAllHiddenColumns();
      }
      viewport.sendSelection();
    }
  }

  @Override
  public void keyReleased(KeyEvent evt)
  {
  }

  @Override
  public void keyTyped(KeyEvent evt)
  {
  }

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    final Object source = evt.getSource();
    if (source == displayNonconservedMenuItem)
    {
      displayNonconservedMenuItem_actionPerformed();
    }
    else if (source == colourTextMenuItem)
    {
      colourTextMenuItem_actionPerformed();
    }
    else if (source == wrapMenuItem)
    {
      wrapMenuItem_actionPerformed();
    }
    else if (source == scaleAbove)
    {
      viewport.setScaleAboveWrapped(scaleAbove.getState());
    }
    else if (source == scaleLeft)
    {
      viewport.setScaleLeftWrapped(scaleLeft.getState());
    }
    else if (source == scaleRight)
    {
      viewport.setScaleRightWrapped(scaleRight.getState());
    }
    else if (source == seqLimits)
    {
      seqLimits_itemStateChanged();
    }
    else if (source == viewBoxesMenuItem)
    {
      viewport.setShowBoxes(viewBoxesMenuItem.getState());
    }
    else if (source == viewTextMenuItem)
    {
      viewport.setShowText(viewTextMenuItem.getState());
    }
    else if (source == renderGapsMenuItem)
    {
      viewport.setRenderGaps(renderGapsMenuItem.getState());
    }
    else if (source == annotationPanelMenuItem)
    {
      boolean showAnnotations = annotationPanelMenuItem.getState();
      showAlignmentAnnotations.setEnabled(showAnnotations);
      showSequenceAnnotations.setEnabled(showAnnotations);
      viewport.setShowAnnotation(showAnnotations);
      alignPanel.setAnnotationVisible(showAnnotations);
    }
    else if (source == sequenceFeatures)
    {
      viewport.setShowSequenceFeatures(sequenceFeatures.getState());
      alignPanel.seqPanel.seqCanvas.repaint();
    }
    else if (source == showAlignmentAnnotations)
    {
      setAnnotationsVisibility();
    }
    else if (source == showSequenceAnnotations)
    {
      setAnnotationsVisibility();
    }
    else if (source == sortAnnBySequence)
    {
      boolean newState = sortAnnBySequence.getState();
      sortAnnByLabel.setState(false);
      setAnnotationSortOrder(
              newState ? SequenceAnnotationOrder.SEQUENCE_AND_LABEL
                      : SequenceAnnotationOrder.NONE);
      setViewportAnnotationOrder();
    }
    else if (source == sortAnnByLabel)
    {
      boolean newState = sortAnnByLabel.getState();
      sortAnnBySequence.setState(false);
      setAnnotationSortOrder(
              newState ? SequenceAnnotationOrder.LABEL_AND_SEQUENCE
                      : SequenceAnnotationOrder.NONE);
      setViewportAnnotationOrder();
    }
    else if (source == showAutoFirst)
    {
      showAutoLast.setState(!showAutoFirst.getState());
      setShowAutoCalculatedAbove(showAutoFirst.getState());
      setViewportAnnotationOrder();
    }
    else if (source == showAutoLast)
    {
      showAutoFirst.setState(!showAutoLast.getState());
      setShowAutoCalculatedAbove(showAutoFirst.getState());
      setViewportAnnotationOrder();
    }
    else if (source == conservationMenuItem)
    {
      conservationMenuItem_actionPerformed();
    }
    else if (source == abovePIDThreshold)
    {
      abovePIDThreshold_actionPerformed();
    }
    else if (source == applyToAllGroups)
    {
      viewport.setColourAppliesToAllGroups(applyToAllGroups.getState());
    }
    else if (source == autoCalculate)
    {
      viewport.autoCalculateConsensus = autoCalculate.getState();
    }
    else if (source == sortByTree)
    {
      viewport.sortByTree = sortByTree.getState();
    }
    else if (source == this.centreColumnLabelFlag)
    {
      centreColumnLabelFlag_stateChanged();
    }
    else if (source == this.followMouseOverFlag)
    {
      mouseOverFlag_stateChanged();
    }
    else if (source == showGroupConsensus)
    {
      showGroupConsensus_actionPerformed();
    }
    else if (source == showGroupConservation)
    {
      showGroupConservation_actionPerformed();
    }
    else if (source == showSequenceLogo)
    {
      showSequenceLogo_actionPerformed();
    }
    else if (source == normSequenceLogo)
    {
      normSequenceLogo_actionPerformed();
    }
    else if (source == showConsensusHistogram)
    {
      showConsensusHistogram_actionPerformed();
    }
    else if (source == applyAutoAnnotationSettings)
    {
      applyAutoAnnotationSettings_actionPerformed();
    }
    // TODO: repaint flags set only if warranted
    alignPanel.paintAlignment(true, true);
  }

  /**
   * Set the visibility state of sequence-related and/or alignment-related
   * annotations depending on checkbox selections, and repaint.
   * 
   * @param visible
   */
  private void setAnnotationsVisibility()
  {
    boolean showForAlignment = showAlignmentAnnotations.getState();
    boolean showForSequences = showSequenceAnnotations.getState();
    if (alignPanel.getAlignment().getAlignmentAnnotation() != null)
    {
      for (AlignmentAnnotation aa : alignPanel.getAlignment()
              .getAlignmentAnnotation())
      {
        boolean visible = (aa.sequenceRef == null ? showForAlignment
                : showForSequences);
        aa.visible = visible;
      }
    }
    alignPanel.validateAnnotationDimensions(true);
    validate();
    repaint();
  }

  private void setAnnotationSortOrder(SequenceAnnotationOrder order)
  {
    this.annotationSortOrder = order;
  }

  /**
   * Set flags on the viewport that control annotation ordering
   */
  private void setViewportAnnotationOrder()
  {
    this.alignPanel.av.setSortAnnotationsBy(this.annotationSortOrder);
    this.alignPanel.av
            .setShowAutocalculatedAbove(this.showAutoCalculatedAbove);
  }

  private void setShowAutoCalculatedAbove(boolean showAbove)
  {
    this.showAutoCalculatedAbove = showAbove;
  }

  private void mouseOverFlag_stateChanged()
  {
    viewport.setFollowHighlight(followMouseOverFlag.getState());
    // TODO: could kick the scrollTo mechanism to reset view for current
    // searchresults.
  }

  private void centreColumnLabelFlag_stateChanged()
  {
    viewport.centreColumnLabels = centreColumnLabelFlag.getState();
    this.alignPanel.annotationPanel.repaint();
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    viewport.applet.currentAlignFrame = this;

    Object source = evt.getSource();

    if (source == inputText)
    {
      inputText_actionPerformed();
    }
    else if (source == loadTree)
    {
      loadTree_actionPerformed();
    }
    else if (source == loadApplication)
    {
      launchFullApplication();
    }
    else if (source == loadAnnotations)
    {
      loadAnnotations();
    }
    else if (source == outputAnnotations)
    {
      outputAnnotations(true);
    }
    else if (source == outputFeatures)
    {
      outputFeatures(true, "Jalview");
    }
    else if (source == closeMenuItem)
    {
      closeMenuItem_actionPerformed();
    }
    else if (source == copy)
    {
      copy_actionPerformed();
    }
    else if (source == undoMenuItem)
    {
      undoMenuItem_actionPerformed();
    }
    else if (source == redoMenuItem)
    {
      redoMenuItem_actionPerformed();
    }
    else if (source == inputText)
    {
      inputText_actionPerformed();
    }
    else if (source == closeMenuItem)
    {
      closeMenuItem_actionPerformed();
    }
    else if (source == undoMenuItem)
    {
      undoMenuItem_actionPerformed();
    }
    else if (source == redoMenuItem)
    {
      redoMenuItem_actionPerformed();
    }
    else if (source == copy)
    {
      copy_actionPerformed();
    }
    else if (source == pasteNew)
    {
      pasteNew_actionPerformed();
    }
    else if (source == pasteThis)
    {
      pasteThis_actionPerformed();
    }
    else if (source == cut)
    {
      cut_actionPerformed();
    }
    else if (source == delete)
    {
      delete_actionPerformed();
    }
    else if (source == createGroup)
    {
      createGroup_actionPerformed();
    }
    else if (source == unGroup)
    {
      unGroup_actionPerformed();
    }
    else if (source == grpsFromSelection)
    {
      makeGrpsFromSelection_actionPerformed();
    }
    else if (source == deleteGroups)
    {
      deleteGroups_actionPerformed();
    }
    else if (source == selectAllSequenceMenuItem)
    {
      selectAllSequenceMenuItem_actionPerformed();
    }
    else if (source == deselectAllSequenceMenuItem)
    {
      deselectAllSequenceMenuItem_actionPerformed();
    }
    else if (source == invertSequenceMenuItem)
    {
      invertSequenceMenuItem_actionPerformed();
      // uncomment to slave sequence selections in split frame
      // viewport.sendSelection();
    }
    else if (source == invertColSel)
    {
      viewport.invertColumnSelection();
      alignPanel.paintAlignment(false, false);
      viewport.sendSelection();
    }
    else if (source == remove2LeftMenuItem)
    {
      trimAlignment(true);
    }
    else if (source == remove2RightMenuItem)
    {
      trimAlignment(false);
    }
    else if (source == removeGappedColumnMenuItem)
    {
      removeGappedColumnMenuItem_actionPerformed();
    }
    else if (source == removeAllGapsMenuItem)
    {
      removeAllGapsMenuItem_actionPerformed();
    }
    else if (source == findMenuItem)
    {
      findMenuItem_actionPerformed();
    }
    else if (source == font)
    {
      new FontChooser(alignPanel);
    }
    else if (source == newView)
    {
      newView(null);
    }
    else if (source == showColumns)
    {
      viewport.showAllHiddenColumns();
      alignPanel.paintAlignment(true, true);
      viewport.sendSelection();
    }
    else if (source == showSeqs)
    {
      viewport.showAllHiddenSeqs();
      alignPanel.paintAlignment(true, true);
      // uncomment if we want to slave sequence selections in split frame
      // viewport.sendSelection();
    }
    else if (source == hideColumns)
    {
      viewport.hideSelectedColumns();
      alignPanel.paintAlignment(true, true);
      viewport.sendSelection();
    }
    else if (source == hideSequences
            && viewport.getSelectionGroup() != null)
    {
      viewport.hideAllSelectedSeqs();
      alignPanel.paintAlignment(true, true);
      // uncomment if we want to slave sequence selections in split frame
      // viewport.sendSelection();
    }
    else if (source == hideAllButSelection)
    {
      toggleHiddenRegions(false, false);
      alignPanel.paintAlignment(true, true);
      viewport.sendSelection();
    }
    else if (source == hideAllSelection)
    {
      SequenceGroup sg = viewport.getSelectionGroup();
      viewport.expandColSelection(sg, false);
      viewport.hideAllSelectedSeqs();
      viewport.hideSelectedColumns();
      alignPanel.paintAlignment(true, true);
      viewport.sendSelection();
    }
    else if (source == showAllHidden)
    {
      viewport.showAllHiddenColumns();
      viewport.showAllHiddenSeqs();
      alignPanel.paintAlignment(true, true);
      viewport.sendSelection();
    }
    else if (source == showGroupConsensus)
    {
      showGroupConsensus_actionPerformed();
    }
    else if (source == showGroupConservation)
    {
      showGroupConservation_actionPerformed();
    }
    else if (source == showSequenceLogo)
    {
      showSequenceLogo_actionPerformed();
    }
    else if (source == normSequenceLogo)
    {
      normSequenceLogo_actionPerformed();
    }
    else if (source == showConsensusHistogram)
    {
      showConsensusHistogram_actionPerformed();
    }
    else if (source == applyAutoAnnotationSettings)
    {
      applyAutoAnnotationSettings_actionPerformed();
    }
    else if (source == featureSettings)
    {
      showFeatureSettingsUI();
    }
    else if (source == alProperties)
    {
      StringBuffer contents = new jalview.io.AlignmentProperties(
              viewport.getAlignment()).formatAsString();
      CutAndPasteTransfer cap = new CutAndPasteTransfer(false, this);
      cap.setText(contents.toString());
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame, MessageManager
              .formatMessage("label.alignment_properties", new String[]
              { getTitle() }), 400, 250);
    }
    else if (source == overviewMenuItem)
    {
      overviewMenuItem_actionPerformed();
    }
    else if (source == noColourmenuItem)
    {
      changeColour(null);
    }
    else if (source == clustalColour)
    {
      abovePIDThreshold.setState(false);
      changeColour(new ClustalxColourScheme(viewport.getAlignment(), null));
    }
    else if (source == zappoColour)
    {
      changeColour(new ZappoColourScheme());
    }
    else if (source == taylorColour)
    {
      changeColour(new TaylorColourScheme());
    }
    else if (source == hydrophobicityColour)
    {
      changeColour(new HydrophobicColourScheme());
    }
    else if (source == helixColour)
    {
      changeColour(new HelixColourScheme());
    }
    else if (source == strandColour)
    {
      changeColour(new StrandColourScheme());
    }
    else if (source == turnColour)
    {
      changeColour(new TurnColourScheme());
    }
    else if (source == buriedColour)
    {
      changeColour(new BuriedColourScheme());
    }
    else if (source == nucleotideColour)
    {
      changeColour(new NucleotideColourScheme());
    }
    else if (source == purinePyrimidineColour)
    {
      changeColour(new PurinePyrimidineColourScheme());
    }
    // else if (source == RNAInteractionColour)
    // {
    // changeColour(new RNAInteractionColourScheme());
    // }
    else if (source == RNAHelixColour)
    {
      changeColour(new RNAHelicesColour(viewport.getAlignment()));
      // new RNAHelicesColourChooser(viewport, alignPanel);
    }
    else if (source == modifyPID)
    {
      modifyPID_actionPerformed();
    }
    else if (source == modifyConservation)
    {
      modifyConservation_actionPerformed();
    }
    else if (source == userDefinedColour)
    {
      new UserDefinedColours(alignPanel, null);
    }
    else if (source == PIDColour)
    {
      changeColour(new PIDColourScheme());
    }
    else if (source == BLOSUM62Colour)
    {
      changeColour(new Blosum62ColourScheme());
    }
    else if (source == tcoffeeColour)
    {
      changeColour(new TCoffeeColourScheme(alignPanel.getAlignment()));
    }
    else if (source == annotationColour)
    {
      new AnnotationColourChooser(viewport, alignPanel);
    }
    else if (source == annotationColumnSelection)
    {
      new AnnotationColumnChooser(viewport, alignPanel);
    }
    else if (source == sortPairwiseMenuItem)
    {
      sortPairwiseMenuItem_actionPerformed();
    }
    else if (source == sortIDMenuItem)
    {
      sortIDMenuItem_actionPerformed();
    }
    else if (source == sortLengthMenuItem)
    {
      sortLengthMenuItem_actionPerformed();
    }
    else if (source == sortGroupMenuItem)
    {
      sortGroupMenuItem_actionPerformed();
    }
    else if (source == removeRedundancyMenuItem)
    {
      removeRedundancyMenuItem_actionPerformed();
    }
    else if (source == pairwiseAlignmentMenuItem)
    {
      pairwiseAlignmentMenuItem_actionPerformed();
    }
    else if (source == PCAMenuItem)
    {
      PCAMenuItem_actionPerformed();
    }
    else if (source == averageDistanceTreeMenuItem)
    {
      averageDistanceTreeMenuItem_actionPerformed();
    }
    else if (source == neighbourTreeMenuItem)
    {
      neighbourTreeMenuItem_actionPerformed();
    }
    else if (source == njTreeBlosumMenuItem)
    {
      njTreeBlosumMenuItem_actionPerformed();
    }
    else if (source == avDistanceTreeBlosumMenuItem)
    {
      avTreeBlosumMenuItem_actionPerformed();
    }
    else if (source == documentation)
    {
      documentation_actionPerformed();
    }
    else if (source == about)
    {
      about_actionPerformed();
    }

  }

  public void inputText_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.input_cut_paste"), 500, 500);
  }

  protected void outputText_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    Frame frame = new Frame();
    frame.add(cap);
    JalviewLite.addFrame(frame, MessageManager
            .formatMessage("label.alignment_output_command", new Object[]
            { e.getActionCommand() }), 600, 500);

    FileFormatI fileFormat = FileFormats.getInstance()
            .forName(e.getActionCommand());
    cap.setText(
            new AppletFormatAdapter(alignPanel).formatSequences(fileFormat,
                    viewport.getAlignment(), viewport.getShowJVSuffix()));
  }

  public void loadAnnotations()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    cap.setText(MessageManager
            .getString("label.paste_features_annotations_Tcoffee_here"));
    cap.setAnnotationImport();
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("action.paste_annotations"), 400, 300);

  }

  public String outputAnnotations(boolean displayTextbox)
  {
    String annotation = new AnnotationFile()
            .printAnnotationsForView(viewport);

    if (displayTextbox)
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(false, this);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame,
              MessageManager.getString("label.annotations"), 600, 500);
      cap.setText(annotation);
    }

    return annotation;
  }

  private Map<String, FeatureColourI> getDisplayedFeatureCols()
  {
    if (alignPanel.getFeatureRenderer() != null
            && viewport.getFeaturesDisplayed() != null)
    {
      return alignPanel.getFeatureRenderer().getDisplayedFeatureCols();

    }
    return null;
  }

  private List<String> getDisplayedFeatureGroups()
  {
    if (alignPanel.getFeatureRenderer() != null
            && viewport.getFeaturesDisplayed() != null)
    {
      return alignPanel.getFeatureRenderer().getDisplayedFeatureGroups();

    }
    return null;
  }

  public String outputFeatures(boolean displayTextbox, String format)
  {
    String features;
    FeaturesFile formatter = new FeaturesFile();
    if (format.equalsIgnoreCase("Jalview"))
    {
      features = formatter.printJalviewFormat(
              viewport.getAlignment().getSequencesArray(),
              alignPanel.getFeatureRenderer(), true, false);
    }
    else
    {
      features = formatter.printGffFormat(
              viewport.getAlignment().getSequencesArray(),
              alignPanel.getFeatureRenderer(), true, false);
    }

    if (displayTextbox)
    {
      boolean frimport = false;
      if (features == null || features.equals("No Features Visible"))
      {
        features = "# No features visible - paste some and import them here.";
        frimport = true;
      }

      CutAndPasteTransfer cap = new CutAndPasteTransfer(frimport, this);
      if (frimport)
      {
        cap.setAnnotationImport();
      }
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame,
              MessageManager.getString("label.features"), 600, 500);
      cap.setText(features);
    }
    else
    {
      if (features == null)
      {
        features = "";
      }
    }

    return features;
  }

  void launchFullApplication()
  {
    StringBuffer url = new StringBuffer(jalviewServletURL);

    // allow servlet parameters to be passed in applet parameter
    String firstSep = url.lastIndexOf("?") > url.lastIndexOf("/") ? "&"
            : "?";
    url.append(firstSep);

    url.append(
            "open=" + appendProtocol(viewport.applet.getParameter("file")));

    if (viewport.applet.getParameter("features") != null)
    {
      url.append("&features=");
      url.append(appendProtocol(viewport.applet.getParameter("features")));
    }

    if (viewport.applet.getParameter("annotations") != null)
    {
      url.append("&annotations=");
      url.append(
              appendProtocol(viewport.applet.getParameter("annotations")));
    }

    if (viewport.applet.getParameter("jnetfile") != null
            || viewport.applet.getParameter("jpredfile") != null)
    {
      url.append("&annotations=");
      url.append(appendProtocol(
              viewport.applet.getParameter("jnetfile") != null
                      ? viewport.applet.getParameter("jnetfile")
                      : viewport.applet.getParameter("jpredfile")));
    }

    if (viewport.applet.getParameter("defaultColour") != null)
    {
      url.append("&colour=" + removeWhiteSpace(
              viewport.applet.getParameter("defaultColour")));
    }

    if (viewport.applet.getParameter("userDefinedColour") != null)
    {
      url.append("&colour=" + removeWhiteSpace(
              viewport.applet.getParameter("userDefinedColour")));
    }
    if (viewport.applet.getParameter("tree") != null)
    {
      url.append("&tree="
              + appendProtocol(viewport.applet.getParameter("tree")));
    }
    if (viewport.applet.getParameter("treeFile") != null)
    {
      url.append("&tree="
              + appendProtocol(viewport.applet.getParameter("treeFile")));
    }

    showURL(url.toString(), "FULL_APP");
  }

  String removeWhiteSpace(String colour)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < colour.length(); i++)
    {
      if (Character.isWhitespace(colour.charAt(i)))
      {
        sb.append("%20");
      }
      else
      {
        sb.append(colour.charAt(i));
      }
    }

    return sb.toString();
  }

  String appendProtocol(String url)
  {
    try
    {
      new URL(url);
      url = URLEncoder.encode(url, "UTF-8");
    }
    /*
     * When we finally deprecate 1.1 compatibility, we can start to use
     * URLEncoder.encode(url,"UTF-8") and then we'll need this catch: catch
     * (UnsupportedEncodingException ex) { System.err.println("WARNING -
     * IMPLEMENTATION ERROR - UNSUPPORTED ENCODING EXCEPTION FOR "+url);
     * ex.printStackTrace(); }
     */
    catch (java.net.MalformedURLException ex)
    {
      url = viewport.applet.getCodeBase() + url;
    } catch (UnsupportedEncodingException ex)
    {
      System.err.println(
              "WARNING = IMPLEMENTATION ERROR - UNSUPPORTED ENCODING EXCEPTION FOR "
                      + url);
      ex.printStackTrace();
    }
    return url;
  }

  public void closeMenuItem_actionPerformed()
  {
    PaintRefresher.RemoveComponent(alignPanel);
    if (alignPanel.seqPanel != null
            && alignPanel.seqPanel.seqCanvas != null)
    {
      PaintRefresher.RemoveComponent(alignPanel.seqPanel.seqCanvas);
    }
    if (alignPanel.idPanel != null && alignPanel.idPanel.idCanvas != null)
    {
      PaintRefresher.RemoveComponent(alignPanel.idPanel.idCanvas);
    }

    if (PaintRefresher.components.size() == 0 && viewport.applet == null)
    {
      System.exit(0);
    }

    viewport = null;
    if (alignPanel != null && alignPanel.overviewPanel != null)
    {
      alignPanel.overviewPanel.dispose();
    }
    alignPanel = null;
    this.dispose();
  }

  /**
   * TODO: JAL-1104
   */
  void updateEditMenuBar()
  {

    if (viewport.getHistoryList().size() > 0)
    {
      undoMenuItem.setEnabled(true);
      CommandI command = viewport.getHistoryList().peek();
      undoMenuItem.setLabel(MessageManager
              .formatMessage("label.undo_command", new Object[]
              { command.getDescription() }));
    }
    else
    {
      undoMenuItem.setEnabled(false);
      undoMenuItem.setLabel(MessageManager.getString("action.undo"));
    }

    if (viewport.getRedoList().size() > 0)
    {
      redoMenuItem.setEnabled(true);

      CommandI command = viewport.getRedoList().peek();
      redoMenuItem.setLabel(MessageManager
              .formatMessage("label.redo_command", new Object[]
              { command.getDescription() }));
    }
    else
    {
      redoMenuItem.setEnabled(false);
      redoMenuItem.setLabel(MessageManager.getString("action.redo"));
    }
  }

  /**
   * TODO: JAL-1104
   */
  @Override
  public void addHistoryItem(CommandI command)
  {
    if (command.getSize() > 0)
    {
      viewport.addToHistoryList(command);
      viewport.clearRedoList();
      updateEditMenuBar();
      viewport.updateHiddenColumns();
    }
  }

  /**
   * TODO: JAL-1104 DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void undoMenuItem_actionPerformed()
  {
    if (viewport.getHistoryList().isEmpty())
    {
      return;
    }

    CommandI command = viewport.getHistoryList().pop();
    viewport.addToRedoList(command);
    command.undoCommand(null);

    AlignmentViewport originalSource = getOriginatingSource(command);
    // JBPNote Test
    if (originalSource != viewport)
    {
      System.err
              .println("Warning: Viewport object mismatch whilst undoing");
    }
    originalSource.updateHiddenColumns(); // originalSource.hasHiddenColumns =
                                          // viewport.getColumnSelection().getHiddenColumns()
                                          // != null;
    updateEditMenuBar();
    originalSource.firePropertyChange("alignment", null,
            originalSource.getAlignment().getSequences());
  }

  /**
   * TODO: JAL-1104 DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void redoMenuItem_actionPerformed()
  {
    if (viewport.getRedoList().isEmpty())
    {
      return;
    }

    CommandI command = viewport.getRedoList().pop();
    viewport.addToHistoryList(command);
    command.doCommand(null);

    AlignmentViewport originalSource = getOriginatingSource(command);
    // JBPNote Test
    if (originalSource != viewport)
    {
      System.err
              .println("Warning: Viewport object mismatch whilst re-doing");
    }
    originalSource.updateHiddenColumns(); // sethasHiddenColumns(); =
                                          // viewport.getColumnSelection().getHiddenColumns()
                                          // != null;

    updateEditMenuBar();
    originalSource.firePropertyChange("alignment", null,
            originalSource.getAlignment().getSequences());
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
      Vector comps = PaintRefresher.components
              .get(viewport.getSequenceSetId());
      for (int i = 0; i < comps.size(); i++)
      {
        if (comps.elementAt(i) instanceof AlignmentPanel)
        {
          if (al == ((AlignmentPanel) comps.elementAt(i)).av.getAlignment())
          {
            originalSource = ((AlignmentPanel) comps.elementAt(i)).av;
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
   * Move the currently selected sequences up or down one position in the
   * alignment
   * 
   * @param up
   */
  public void moveSelectedSequences(boolean up)
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg == null)
    {
      return;
    }
    viewport.getAlignment().moveSelectedSequencesByOne(sg,
            up ? null : viewport.getHiddenRepSequences(), up);
    alignPanel.paintAlignment(true, false);

    /*
     * Also move cDNA/protein complement sequences
     */
    AlignViewportI complement = viewport.getCodingComplement();
    if (complement != null)
    {
      SequenceGroup mappedSelection = MappingUtils.mapSequenceGroup(sg,
              viewport, complement);
      complement.getAlignment().moveSelectedSequencesByOne(mappedSelection,
              up ? null : complement.getHiddenRepSequences(), up);
      getSplitFrame().getComplement(this).alignPanel.paintAlignment(true,
              false);
    }
  }

  synchronized void slideSequences(boolean right, int size)
  {
    List<SequenceI> sg = new Vector<>();
    if (viewport.cursorMode)
    {
      sg.add(viewport.getAlignment()
              .getSequenceAt(alignPanel.seqPanel.seqCanvas.cursorY));
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

    Vector<SequenceI> invertGroup = new Vector();

    for (int i = 0; i < viewport.getAlignment().getHeight(); i++)
    {
      if (!sg.contains(viewport.getAlignment().getSequenceAt(i)))
      {
        invertGroup.addElement(viewport.getAlignment().getSequenceAt(i));
      }
    }

    SequenceI[] seqs1 = sg.toArray(new SequenceI[sg.size()]);

    SequenceI[] seqs2 = invertGroup
            .toArray(new SequenceI[invertGroup.size()]);
    for (int i = 0; i < invertGroup.size(); i++)
    {
      seqs2[i] = invertGroup.elementAt(i);
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
        alignPanel.seqPanel.moveCursor(size, 0);
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
        alignPanel.seqPanel.moveCursor(-size, 0);
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

    boolean appendHistoryItem = false;
    Deque<CommandI> historyList = viewport.getHistoryList();
    if (historyList != null && historyList.size() > 0
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

  static StringBuffer copiedSequences;

  static HiddenColumns copiedHiddenColumns;

  protected void copy_actionPerformed()
  {
    if (viewport.getSelectionGroup() == null)
    {
      return;
    }

    SequenceGroup sg = viewport.getSelectionGroup();
    copiedSequences = new StringBuffer();
    Map<Integer, SequenceI> orderedSeqs = new HashMap<>();
    for (int i = 0; i < sg.getSize(); i++)
    {
      SequenceI seq = sg.getSequenceAt(i);
      int index = viewport.getAlignment().findIndex(seq);
      orderedSeqs.put(index, seq);
    }

    int index = 0, startRes, endRes;
    char ch;

    if (viewport.hasHiddenColumns() && viewport.getSelectionGroup() != null)
    {
      int hiddenOffset = viewport.getSelectionGroup().getStartRes();
      int hiddenCutoff = viewport.getSelectionGroup().getEndRes();

      // create new HiddenColumns object with copy of hidden regions
      // between startRes and endRes, offset by startRes
      copiedHiddenColumns = new HiddenColumns(
              viewport.getAlignment().getHiddenColumns(), hiddenOffset,
              hiddenCutoff, hiddenOffset);
    }
    else
    {
      copiedHiddenColumns = null;
    }

    for (int i = 0; i < sg.getSize(); i++)
    {
      SequenceI seq = null;

      while (seq == null)
      {
        if (orderedSeqs.containsKey(index))
        {
          seq = orderedSeqs.get(index);
          index++;
          break;
        }
        else
        {
          index++;
        }
      }

      // FIND START RES
      // Returns residue following index if gap
      startRes = seq.findPosition(sg.getStartRes());

      // FIND END RES
      // Need to find the residue preceeding index if gap
      endRes = 0;

      for (int j = 0; j < sg.getEndRes() + 1 && j < seq.getLength(); j++)
      {
        ch = seq.getCharAt(j);
        if (!jalview.util.Comparison.isGap((ch)))
        {
          endRes++;
        }
      }

      if (endRes > 0)
      {
        endRes += seq.getStart() - 1;
      }

      copiedSequences.append(seq.getName() + "\t" + startRes + "\t" + endRes
              + "\t" + seq.getSequenceAsString(sg.getStartRes(),
                      sg.getEndRes() + 1)
              + "\n");
    }

  }

  protected void pasteNew_actionPerformed()
  {
    paste(true);
  }

  protected void pasteThis_actionPerformed()
  {
    paste(false);
  }

  void paste(boolean newAlignment)
  {
    try
    {
      if (copiedSequences == null)
      {
        return;
      }

      StringTokenizer st = new StringTokenizer(copiedSequences.toString(),
              "\t");
      Vector seqs = new Vector();
      while (st.hasMoreElements())
      {
        String name = st.nextToken();
        int start = Integer.parseInt(st.nextToken());
        int end = Integer.parseInt(st.nextToken());
        seqs.addElement(new Sequence(name, st.nextToken(), start, end));
      }
      SequenceI[] newSeqs = new SequenceI[seqs.size()];
      for (int i = 0; i < seqs.size(); i++)
      {
        newSeqs[i] = (SequenceI) seqs.elementAt(i);
      }

      if (newAlignment)
      {
        String newtitle = MessageManager
                .getString("label.copied_sequences");
        if (getTitle().startsWith(
                MessageManager.getString("label.copied_sequences")))
        {
          newtitle = getTitle();
        }
        else
        {
          newtitle = newtitle.concat(MessageManager
                  .formatMessage("label.from_msname", new String[]
                  { getTitle() }));
        }
        AlignFrame af = new AlignFrame(new Alignment(newSeqs),
                viewport.applet, newtitle, false);
        af.viewport.setHiddenColumns(copiedHiddenColumns);

        jalview.bin.JalviewLite.addFrame(af, newtitle, frameWidth,
                frameHeight);
      }
      else
      {
        addSequences(newSeqs);
      }

    } catch (Exception ex)
    {
    } // could be anything being pasted in here

  }

  void addSequences(SequenceI[] seqs)
  {
    for (int i = 0; i < seqs.length; i++)
    {
      viewport.getAlignment().addSequence(seqs[i]);
    }

    // !newAlignment
    addHistoryItem(new EditCommand(
            MessageManager.getString("label.add_sequences"), Action.PASTE,
            seqs, 0, viewport.getAlignment().getWidth(),
            viewport.getAlignment()));

    viewport.getRanges().setEndSeq(viewport.getAlignment().getHeight() - 1); // BH
                                                                             // 2019.04.18
    viewport.getAlignment().getWidth();
    viewport.firePropertyChange("alignment", null,
            viewport.getAlignment().getSequences());

  }

  protected void cut_actionPerformed()
  {
    copy_actionPerformed();
    delete_actionPerformed();
  }

  protected void delete_actionPerformed()
  {

    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg == null)
    {
      return;
    }

    Vector seqs = new Vector();
    SequenceI seq;
    for (int i = 0; i < sg.getSize(); i++)
    {
      seq = sg.getSequenceAt(i);
      seqs.addElement(seq);
    }

    /*
     * If the cut affects all sequences, warn, remove highlighted columns
     */
    if (sg.getSize() == viewport.getAlignment().getHeight())
    {
      boolean isEntireAlignWidth = (((sg.getEndRes() - sg.getStartRes())
              + 1) == viewport.getAlignment().getWidth()) ? true : false;
      if (isEntireAlignWidth)
      {
        String title = MessageManager.getString("label.delete_all");
        Panel infoPanel = new Panel();
        infoPanel.setLayout(new FlowLayout());
        infoPanel.add(
                new Label(MessageManager.getString("warn.delete_all")));

        final JVDialog dialog = new JVDialog(this, title, true, 400, 200);
        dialog.setMainPanel(infoPanel);
        dialog.ok.setLabel(MessageManager.getString("action.ok"));
        dialog.cancel.setLabel(MessageManager.getString("action.cancel"));
        dialog.setVisible(true);

        if (!dialog.accept)
        {
          return;
        }
      }
      viewport.getColumnSelection().removeElements(sg.getStartRes(),
              sg.getEndRes() + 1);
    }

    SequenceI[] cut = new SequenceI[seqs.size()];
    for (int i = 0; i < seqs.size(); i++)
    {
      cut[i] = (SequenceI) seqs.elementAt(i);
    }

    /*
     * //ADD HISTORY ITEM
     */
    addHistoryItem(new EditCommand(
            MessageManager.getString("label.cut_sequences"), Action.CUT,
            cut, sg.getStartRes(), sg.getEndRes() - sg.getStartRes() + 1,
            viewport.getAlignment()));

    viewport.setSelectionGroup(null);
    viewport.getAlignment().deleteGroup(sg);

    viewport.firePropertyChange("alignment", null,
            viewport.getAlignment().getSequences());

    if (viewport.getAlignment().getHeight() < 1)
    {
      this.setVisible(false);
    }
    viewport.sendSelection();
  }

  /**
   * group consensus toggled
   * 
   */
  protected void showGroupConsensus_actionPerformed()
  {
    viewport.setShowGroupConsensus(showGroupConsensus.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());

  }

  /**
   * group conservation toggled.
   */
  protected void showGroupConservation_actionPerformed()
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
  protected void showConsensusHistogram_actionPerformed()
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
  protected void showSequenceLogo_actionPerformed()
  {
    viewport.setShowSequenceLogo(showSequenceLogo.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  protected void normSequenceLogo_actionPerformed()
  {
    showSequenceLogo.setState(true);
    viewport.setShowSequenceLogo(true);
    viewport.setNormaliseSequenceLogo(normSequenceLogo.getState());
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  protected void applyAutoAnnotationSettings_actionPerformed()
  {
    alignPanel.updateAnnotation(applyAutoAnnotationSettings.getState());
  }

  protected void makeGrpsFromSelection_actionPerformed()
  {
    if (avc.makeGroupsFromSelection())
    {
      PaintRefresher.Refresh(this, viewport.getSequenceSetId());
      alignPanel.updateAnnotation();
      alignPanel.paintAlignment(true, true);
    }
  }

  protected void createGroup_actionPerformed()
  {
    avc.createGroup();
  }

  protected void unGroup_actionPerformed()
  {
    if (avc.unGroup())
    {
      alignPanel.alignmentChanged();
    }
  }

  protected void deleteGroups_actionPerformed()
  {
    if (avc.deleteGroups())
    {
      alignPanel.alignmentChanged();
    }
  }

  public void selectAllSequenceMenuItem_actionPerformed()
  {
    SequenceGroup sg = new SequenceGroup();
    for (int i = 0; i < viewport.getAlignment().getSequences().size(); i++)
    {
      sg.addSequence(viewport.getAlignment().getSequenceAt(i), false);
    }
    sg.setEndRes(viewport.getAlignment().getWidth() - 1);
    viewport.setSelectionGroup(sg);
    // JAL-2034 - should delegate to
    // alignPanel to decide if overview needs
    // updating.
    alignPanel.paintAlignment(false, false);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
    viewport.sendSelection();
  }

  public void deselectAllSequenceMenuItem_actionPerformed()
  {
    if (viewport.cursorMode)
    {
      alignPanel.seqPanel.keyboardNo1 = null;
      alignPanel.seqPanel.keyboardNo2 = null;
    }
    viewport.setSelectionGroup(null);
    viewport.getColumnSelection().clear();
    viewport.setSelectionGroup(null);
    alignPanel.idPanel.idCanvas.searchResults = null;
    alignPanel.seqPanel.seqCanvas.highlightSearchResults(null);
    // JAL-2034 - should delegate to
    // alignPanel to decide if overview needs
    // updating.
    alignPanel.paintAlignment(false, false);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
    viewport.sendSelection();
  }

  public void invertSequenceMenuItem_actionPerformed()
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    for (int i = 0; i < viewport.getAlignment().getSequences().size(); i++)
    {
      sg.addOrRemove(viewport.getAlignment().getSequenceAt(i), false);
    }

    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
    viewport.sendSelection();
  }

  public void invertColSel_actionPerformed()
  {
    viewport.invertColumnSelection();
    alignPanel.paintAlignment(true, false);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
    viewport.sendSelection();
  }

  void trimAlignment(boolean trimLeft)
  {
    AlignmentI al = viewport.getAlignment();
    ViewportRanges ranges = viewport.getRanges();
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
        seqs = al.getSequencesArray();
      }

      TrimRegionCommand trimRegion;
      if (trimLeft)
      {
        trimRegion = new TrimRegionCommand("Remove Left", true, seqs,
                column, al);
        ranges.setStartRes(0);
      }
      else
      {
        trimRegion = new TrimRegionCommand("Remove Right", false, seqs,
                column, al);
      }

      setStatus(MessageManager.formatMessage("label.removed_columns",
              new String[]
              { Integer.valueOf(trimRegion.getSize()).toString() }));
      addHistoryItem(trimRegion);

      for (SequenceGroup sg : al.getGroups())
      {
        if ((trimLeft && !sg.adjustForRemoveLeft(column))
                || (!trimLeft && !sg.adjustForRemoveRight(column)))
        {
          al.deleteGroup(sg);
        }
      }

      viewport.firePropertyChange("alignment", null, al.getSequences());
    }
  }

  public void removeGappedColumnMenuItem_actionPerformed()
  {
    AlignmentI al = viewport.getAlignment();
    ViewportRanges ranges = viewport.getRanges();
    int start = 0;
    int end = ranges.getAbsoluteAlignmentWidth() - 1;

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
            new String[]
            { Integer.valueOf(removeGapCols.getSize()).toString() }));

    // This is to maintain viewport position on first residue
    // of first sequence
    SequenceI seq = al.getSequenceAt(0);
    int startRes = seq.findPosition(ranges.getStartRes());
    // ShiftList shifts;
    // viewport.getAlignment().removeGaps(shifts=new ShiftList());
    // edit.alColumnChanges=shifts.getInverse();
    // if (viewport.hasHiddenColumns)
    // viewport.getColumnSelection().compensateForEdits(shifts);
    ranges.setStartRes(seq.findIndex(startRes) - 1);
    viewport.firePropertyChange("alignment", null, al.getSequences());

  }

  public void removeAllGapsMenuItem_actionPerformed()
  {
    AlignmentI al = viewport.getAlignment();
    ViewportRanges ranges = viewport.getRanges();
    int start = 0;
    int end = ranges.getAbsoluteAlignmentWidth() - 1;

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
    SequenceI seq = al.getSequenceAt(0);
    int startRes = seq.findPosition(ranges.getStartRes());

    addHistoryItem(
            new RemoveGapsCommand("Remove Gaps", seqs, start, end, al));

    ranges.setStartRes(seq.findIndex(startRes) - 1);

    viewport.firePropertyChange("alignment", null, al.getSequences());

  }

  public void findMenuItem_actionPerformed()
  {
    new Finder(alignPanel);
  }

  /**
   * create a new view derived from the current view
   * 
   * @param viewtitle
   * @return frame for the new view
   */
  public AlignFrame newView(String viewtitle)
  {
    AlignmentI newal;
    if (viewport.hasHiddenRows())
    {
      newal = new Alignment(viewport.getAlignment().getHiddenSequences()
              .getFullAlignment().getSequencesArray());
    }
    else
    {
      newal = new Alignment(viewport.getAlignment().getSequencesArray());
    }

    if (viewport.getAlignment().getAlignmentAnnotation() != null)
    {
      for (int i = 0; i < viewport.getAlignment()
              .getAlignmentAnnotation().length; i++)
      {
        if (!viewport.getAlignment()
                .getAlignmentAnnotation()[i].autoCalculated)
        {
          newal.addAnnotation(
                  viewport.getAlignment().getAlignmentAnnotation()[i]);
        }
      }
    }

    AlignFrame newaf = new AlignFrame(newal, viewport.applet, "", false);

    newaf.viewport.setSequenceSetId(alignPanel.av.getSequenceSetId());
    PaintRefresher.Register(alignPanel, alignPanel.av.getSequenceSetId());
    PaintRefresher.Register(newaf.alignPanel,
            newaf.alignPanel.av.getSequenceSetId());

    PaintRefresher.Register(newaf.alignPanel.idPanel.idCanvas,
            newaf.alignPanel.av.getSequenceSetId());
    PaintRefresher.Register(newaf.alignPanel.seqPanel.seqCanvas,
            newaf.alignPanel.av.getSequenceSetId());

    Vector comps = PaintRefresher.components
            .get(viewport.getSequenceSetId());
    int viewSize = -1;
    for (int i = 0; i < comps.size(); i++)
    {
      if (comps.elementAt(i) instanceof AlignmentPanel)
      {
        viewSize++;
      }
    }

    String title = new String(this.getTitle());
    if (viewtitle != null)
    {
      title = viewtitle + " ( " + title + ")";
    }
    else
    {
      if (title.indexOf("(View") > -1)
      {
        title = title.substring(0, title.indexOf("(View"));
      }
      title += "(View " + viewSize + ")";
    }

    newaf.setTitle(title.toString());

    newaf.viewport.setHistoryList(viewport.getHistoryList());
    newaf.viewport.setRedoList(viewport.getRedoList());
    return newaf;
  }

  /**
   * 
   * @return list of feature groups on the view
   */
  public String[] getFeatureGroups()
  {
    FeatureRenderer fr = null;
    if (alignPanel != null
            && (fr = alignPanel.getFeatureRenderer()) != null)
    {
      List<String> gps = fr.getFeatureGroups();
      String[] _gps = gps.toArray(new String[gps.size()]);
      return _gps;
    }
    return null;
  }

  /**
   * get sequence feature groups that are hidden or shown
   * 
   * @param visible
   *          true is visible
   * @return list
   */
  public String[] getFeatureGroupsOfState(boolean visible)
  {
    FeatureRenderer fr = null;
    if (alignPanel != null
            && (fr = alignPanel.getFeatureRenderer()) != null)
    {
      List<String> gps = fr.getGroups(visible);
      String[] _gps = gps.toArray(new String[gps.size()]);
      return _gps;
    }
    return null;
  }

  /**
   * Change the display state for the given feature groups
   * 
   * @param groups
   *          list of group strings
   * @param state
   *          visible or invisible
   */
  public void setFeatureGroupState(String[] groups, boolean state)
  {
    FeatureRenderer fr = null;
    this.sequenceFeatures.setState(true);
    viewport.setShowSequenceFeatures(true);
    if (alignPanel != null
            && (fr = alignPanel.getFeatureRenderer()) != null)
    {

      fr.setGroupVisibility(Arrays.asList(groups), state);
      alignPanel.seqPanel.seqCanvas.repaint();
      if (alignPanel.overviewPanel != null)
      {
        alignPanel.overviewPanel.updateOverviewImage();
      }
    }
  }

  public void seqLimits_itemStateChanged()
  {
    viewport.setShowJVSuffix(seqLimits.getState());
    alignPanel.fontChanged();
    alignPanel.paintAlignment(true, false);
  }

  protected void colourTextMenuItem_actionPerformed()
  {
    viewport.setColourText(colourTextMenuItem.getState());
    alignPanel.paintAlignment(false, false);
  }

  protected void displayNonconservedMenuItem_actionPerformed()
  {
    viewport.setShowUnconserved(displayNonconservedMenuItem.getState());
    alignPanel.paintAlignment(false, false);
  }

  protected void wrapMenuItem_actionPerformed()
  {
    viewport.setWrapAlignment(wrapMenuItem.getState());
    alignPanel.setWrapAlignment(wrapMenuItem.getState());
    scaleAbove.setEnabled(wrapMenuItem.getState());
    scaleLeft.setEnabled(wrapMenuItem.getState());
    scaleRight.setEnabled(wrapMenuItem.getState());
    alignPanel.paintAlignment(true, false);
  }

  public void overviewMenuItem_actionPerformed()
  {
    if (alignPanel.overviewPanel != null)
    {
      return;
    }

    Frame frame = new Frame();
    final OverviewPanel overview = new OverviewPanel(alignPanel);
    frame.add(overview);
    // +50 must allow for applet frame window
    jalview.bin.JalviewLite.addFrame(frame, MessageManager
            .formatMessage("label.overview_params", new String[]
            { this.getTitle() }), overview.getPreferredSize().width,
            overview.getPreferredSize().height + 50);

    frame.pack();
    final AlignmentPanel ap = alignPanel;
    frame.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        overview.dispose();
        if (ap != null)
        {
          ap.setOverviewPanel(null);
        }
      };
    });

    alignPanel.setOverviewPanel(overview);

  }

  @Override
  public void changeColour(ColourSchemeI cs)
  {
    viewport.setGlobalColourScheme(cs);

    alignPanel.paintAlignment(true, true);
  }

  protected void modifyPID_actionPerformed()
  {
    if (viewport.getAbovePIDThreshold()
            && viewport.getGlobalColourScheme() != null)
    {
      SliderPanel.setPIDSliderSource(alignPanel,
              viewport.getResidueShading(), alignPanel.getViewName());
      SliderPanel.showPIDSlider();
    }
  }

  protected void modifyConservation_actionPerformed()
  {
    if (viewport.getConservationSelected()
            && viewport.getGlobalColourScheme() != null)
    {
      SliderPanel.setConservationSlider(alignPanel,
              viewport.getResidueShading(), alignPanel.getViewName());
      SliderPanel.showConservationSlider();
    }
  }

  protected void conservationMenuItem_actionPerformed()
  {
    boolean selected = conservationMenuItem.getState();
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

  public void abovePIDThreshold_actionPerformed()
  {
    boolean selected = abovePIDThreshold.getState();
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

  public void sortPairwiseMenuItem_actionPerformed()
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByPID(viewport.getAlignment(),
            viewport.getAlignment().getSequenceAt(0));

    addHistoryItem(new OrderCommand("Pairwise Sort", oldOrder,
            viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);
  }

  public void sortIDMenuItem_actionPerformed()
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByID(viewport.getAlignment());
    addHistoryItem(
            new OrderCommand("ID Sort", oldOrder, viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);
  }

  public void sortLengthMenuItem_actionPerformed()
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByLength(viewport.getAlignment());
    addHistoryItem(new OrderCommand("Length Sort", oldOrder,
            viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);
  }

  public void sortGroupMenuItem_actionPerformed()
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByGroup(viewport.getAlignment());
    addHistoryItem(new OrderCommand("Group Sort", oldOrder,
            viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);

  }

  public void removeRedundancyMenuItem_actionPerformed()
  {
    new RedundancyPanel(alignPanel);
  }

  public void pairwiseAlignmentMenuItem_actionPerformed()
  {
    if (viewport.getSelectionGroup() != null
            && viewport.getSelectionGroup().getSize() > 1)
    {
      Frame frame = new Frame();
      frame.add(new PairwiseAlignPanel(alignPanel));
      jalview.bin.JalviewLite.addFrame(frame,
              MessageManager.getString("action.pairwise_alignment"), 600,
              500);
    }
  }

  public void PCAMenuItem_actionPerformed()
  {
    // are the sequences aligned?
    if (!viewport.getAlignment().isAligned(false))
    {
      SequenceI current;
      int Width = viewport.getAlignment().getWidth();

      for (int i = 0; i < viewport.getAlignment().getSequences()
              .size(); i++)
      {
        current = viewport.getAlignment().getSequenceAt(i);

        if (current.getLength() < Width)
        {
          current.insertCharAt(Width - 1, viewport.getGapCharacter());
        }
      }
      alignPanel.paintAlignment(false, false);
    }

    if ((viewport.getSelectionGroup() != null
            && viewport.getSelectionGroup().getSize() < 4
            && viewport.getSelectionGroup().getSize() > 0)
            || viewport.getAlignment().getHeight() < 4)
    {
      return;
    }

    try
    {
      new PCAPanel(viewport);
    } catch (java.lang.OutOfMemoryError ex)
    {
    }

  }

  public void averageDistanceTreeMenuItem_actionPerformed()
  {
    newTreePanel(TreeBuilder.AVERAGE_DISTANCE, new PIDModel().getName(),
            "Average distance tree using PID");
  }

  public void neighbourTreeMenuItem_actionPerformed()
  {
    newTreePanel(TreeBuilder.NEIGHBOUR_JOINING, new PIDModel().getName(),
            "Neighbour joining tree using PID");
  }

  protected void njTreeBlosumMenuItem_actionPerformed()
  {
    newTreePanel(TreeBuilder.NEIGHBOUR_JOINING,
            ScoreModels.getInstance().getBlosum62().getName(),
            "Neighbour joining tree using BLOSUM62");
  }

  protected void avTreeBlosumMenuItem_actionPerformed()
  {
    newTreePanel(TreeBuilder.AVERAGE_DISTANCE,
            ScoreModels.getInstance().getBlosum62().getName(),
            "Average distance tree using BLOSUM62");
  }

  void newTreePanel(String type, String pwType, String title)
  {
    // are the sequences aligned?
    if (!viewport.getAlignment().isAligned(false))
    {
      SequenceI current;
      int Width = viewport.getAlignment().getWidth();

      for (int i = 0; i < viewport.getAlignment().getSequences()
              .size(); i++)
      {
        current = viewport.getAlignment().getSequenceAt(i);

        if (current.getLength() < Width)
        {
          current.insertCharAt(Width - 1, viewport.getGapCharacter());
        }
      }
      alignPanel.paintAlignment(false, false);

    }

    if ((viewport.getSelectionGroup() != null
            && viewport.getSelectionGroup().getSize() > 1)
            || (viewport.getAlignment().getHeight() > 1))
    {
      final TreePanel tp = new TreePanel(alignPanel, type, pwType);

      addTreeMenuItem(tp, title);

      jalview.bin.JalviewLite.addFrame(tp, title, 600, 500);
    }
  }

  void loadTree_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    cap.setText(MessageManager.getString("label.paste_newick_tree_file"));
    cap.setTreeImport();
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.paste_newick_file"), 400, 300);
  }

  public void loadTree(jalview.io.NewickFile tree, String treeFile)
  {
    TreePanel tp = new TreePanel(alignPanel, treeFile,
            MessageManager.getString("label.load_tree_from_file"), tree);
    jalview.bin.JalviewLite.addFrame(tp, treeFile, 600, 500);
    addTreeMenuItem(tp, treeFile);
  }

  /**
   * sort the alignment using the given treePanel
   * 
   * @param treePanel
   *          tree used to sort view
   * @param title
   *          string used for undo event name
   */
  public void sortByTree(TreePanel treePanel, String title)
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByTree(viewport.getAlignment(),
            treePanel.getTree());
    // addHistoryItem(new HistoryItem("Sort", viewport.alignment,
    // HistoryItem.SORT));
    addHistoryItem(new OrderCommand(MessageManager
            .formatMessage("label.order_by_params", new String[]
            { title }), oldOrder, viewport.getAlignment()));
    alignPanel.paintAlignment(true, false);
  }

  /**
   * Do any automatic reordering of the alignment and add the necessary bits to
   * the menu structure for the new tree
   * 
   * @param treePanel
   * @param title
   */
  protected void addTreeMenuItem(final TreePanel treePanel,
          final String title)
  {
    final MenuItem item = new MenuItem(title);
    sortByTreeMenu.add(item);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        sortByTree(treePanel, title); // treePanel.getTitle());
      }
    });

    treePanel.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowOpened(WindowEvent e)
      {
        if (viewport.sortByTree)
        {
          sortByTree(treePanel, title);
        }
        super.windowOpened(e);
      }

      @Override
      public void windowClosing(WindowEvent e)
      {
        sortByTreeMenu.remove(item);
      };
    });
  }

  public boolean sortBy(AlignmentOrder alorder, String undoname)
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    if (viewport.applet.debug)
    {
      System.err.println("Sorting " + alorder.getOrder().size()
              + " in alignment '" + getTitle() + "'");
    }
    AlignmentSorter.sortBy(viewport.getAlignment(), alorder);
    if (undoname != null)
    {
      addHistoryItem(new OrderCommand(undoname, oldOrder,
              viewport.getAlignment()));
    }
    alignPanel.paintAlignment(true, false);
    return true;
  }

  protected void documentation_actionPerformed()
  {
    alignPanel.av.applet.openJalviewHelpUrl();
  }

  protected void about_actionPerformed()
  {

    class AboutPanel extends Canvas
    {
      String version;

      String builddate;

      public AboutPanel(String version, String builddate)
      {
        this.version = version;
        this.builddate = builddate;
      }

      @Override
      public void paint(Graphics g)
      {
        g.setColor(Color.white);
        g.fillRect(0, 0, getSize().width, getSize().height);
        g.setFont(new Font("Helvetica", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int fh = fm.getHeight();
        int y = 5, x = 7;
        g.setColor(Color.black);
        // TODO: update this text for each release or centrally store it for
        // lite and application
        g.setFont(new Font("Helvetica", Font.BOLD, 14));
        g.drawString(MessageManager
                .formatMessage("label.jalviewLite_release", new String[]
                { version }), x, y += fh);
        g.setFont(new Font("Helvetica", Font.BOLD, 12));
        g.drawString(MessageManager.formatMessage("label.jaview_build_date",
                new String[]
                { builddate }), x, y += fh);
        g.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g.drawString(MessageManager.getString("label.jalview_authors_1"), x,
                y += fh * 1.5);
        g.drawString(MessageManager.getString("label.jalview_authors_2"),
                x + 50, y += fh + 8);
        g.drawString(MessageManager.getString("label.jalview_dev_managers"),
                x, y += fh);
        g.drawString(MessageManager
                .getString("label.jalview_distribution_lists"), x, y += fh);
        g.drawString(MessageManager.getString("label.jalview_please_cite"),
                x, y += fh + 8);
        g.drawString(
                MessageManager.getString("label.jalview_cite_1_authors"), x,
                y += fh);
        g.drawString(MessageManager.getString("label.jalview_cite_1_title"),
                x, y += fh);
        g.drawString(MessageManager.getString("label.jalview_cite_1_ref"),
                x, y += fh);
      }
    }

    Frame frame = new Frame();
    frame.add(new AboutPanel(JalviewLite.getVersion(),
            JalviewLite.getBuildDate()));
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.jalview"), 580, 220);

  }

  public void showURL(String url, String target)
  {
    if (viewport.applet == null)
    {
      System.out.println("Not running as applet - no browser available.");
    }
    else
    {
      viewport.applet.showURL(url, target);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////
  // JBuilder Graphics here

  MenuBar alignFrameMenuBar = new MenuBar();

  Menu fileMenu = new Menu(MessageManager.getString("action.file"));

  MenuItem loadApplication = new MenuItem(
          MessageManager.getString("label.view_full_application"));

  MenuItem loadTree = new MenuItem(
          MessageManager.getString("label.load_associated_tree"));

  MenuItem loadAnnotations = new MenuItem(
          MessageManager.getString("label.load_features_annotations"));

  MenuItem outputFeatures = new MenuItem(
          MessageManager.getString("label.export_features"));

  MenuItem outputAnnotations = new MenuItem(
          MessageManager.getString("label.export_annotations"));

  MenuItem closeMenuItem = new MenuItem(
          MessageManager.getString("action.close"));

  MenuItem selectAllSequenceMenuItem = new MenuItem(
          MessageManager.getString("action.select_all"));

  MenuItem deselectAllSequenceMenuItem = new MenuItem(
          MessageManager.getString("action.deselect_all"));

  MenuItem invertSequenceMenuItem = new MenuItem(
          MessageManager.getString("action.invert_selection"));

  MenuItem remove2LeftMenuItem = new MenuItem();

  MenuItem remove2RightMenuItem = new MenuItem();

  MenuItem removeGappedColumnMenuItem = new MenuItem();

  MenuItem removeAllGapsMenuItem = new MenuItem();

  CheckboxMenuItem viewBoxesMenuItem = new CheckboxMenuItem();

  CheckboxMenuItem viewTextMenuItem = new CheckboxMenuItem();

  MenuItem sortPairwiseMenuItem = new MenuItem();

  MenuItem sortIDMenuItem = new MenuItem();

  MenuItem sortLengthMenuItem = new MenuItem();

  MenuItem sortGroupMenuItem = new MenuItem();

  MenuItem removeRedundancyMenuItem = new MenuItem();

  MenuItem pairwiseAlignmentMenuItem = new MenuItem();

  MenuItem PCAMenuItem = new MenuItem();

  MenuItem averageDistanceTreeMenuItem = new MenuItem();

  MenuItem neighbourTreeMenuItem = new MenuItem();

  BorderLayout borderLayout1 = new BorderLayout();

  public Label statusBar = new Label();

  MenuItem clustalColour = new MenuItem();

  MenuItem zappoColour = new MenuItem();

  MenuItem taylorColour = new MenuItem();

  MenuItem hydrophobicityColour = new MenuItem();

  MenuItem helixColour = new MenuItem();

  MenuItem strandColour = new MenuItem();

  MenuItem turnColour = new MenuItem();

  MenuItem buriedColour = new MenuItem();

  MenuItem purinePyrimidineColour = new MenuItem();

  // MenuItem RNAInteractionColour = new MenuItem();

  MenuItem RNAHelixColour = new MenuItem();

  MenuItem userDefinedColour = new MenuItem();

  MenuItem PIDColour = new MenuItem();

  MenuItem BLOSUM62Colour = new MenuItem();

  MenuItem tcoffeeColour = new MenuItem();

  MenuItem njTreeBlosumMenuItem = new MenuItem();

  MenuItem avDistanceTreeBlosumMenuItem = new MenuItem();

  CheckboxMenuItem annotationPanelMenuItem = new CheckboxMenuItem();

  CheckboxMenuItem colourTextMenuItem = new CheckboxMenuItem();

  CheckboxMenuItem displayNonconservedMenuItem = new CheckboxMenuItem();

  MenuItem alProperties = new MenuItem(
          MessageManager.getString("label.alignment_props"));

  MenuItem overviewMenuItem = new MenuItem();

  MenuItem undoMenuItem = new MenuItem();

  MenuItem redoMenuItem = new MenuItem();

  CheckboxMenuItem conservationMenuItem = new CheckboxMenuItem();

  MenuItem noColourmenuItem = new MenuItem();

  CheckboxMenuItem wrapMenuItem = new CheckboxMenuItem();

  CheckboxMenuItem renderGapsMenuItem = new CheckboxMenuItem();

  MenuItem findMenuItem = new MenuItem();

  CheckboxMenuItem abovePIDThreshold = new CheckboxMenuItem();

  MenuItem nucleotideColour = new MenuItem();

  MenuItem deleteGroups = new MenuItem();

  MenuItem grpsFromSelection = new MenuItem();

  MenuItem createGroup = new MenuItem();

  MenuItem unGroup = new MenuItem();

  MenuItem delete = new MenuItem();

  MenuItem copy = new MenuItem();

  MenuItem cut = new MenuItem();

  Menu pasteMenu = new Menu();

  MenuItem pasteNew = new MenuItem();

  MenuItem pasteThis = new MenuItem();

  CheckboxMenuItem applyToAllGroups = new CheckboxMenuItem();

  MenuItem font = new MenuItem();

  CheckboxMenuItem scaleAbove = new CheckboxMenuItem();

  CheckboxMenuItem scaleLeft = new CheckboxMenuItem();

  CheckboxMenuItem scaleRight = new CheckboxMenuItem();

  MenuItem modifyPID = new MenuItem();

  MenuItem modifyConservation = new MenuItem();

  CheckboxMenuItem autoCalculate = null;

  CheckboxMenuItem sortByTree = new CheckboxMenuItem(
          "Sort Alignment With New Tree", true);

  Menu sortByTreeMenu = new Menu();

  MenuItem inputText = new MenuItem();

  MenuItem documentation = new MenuItem();

  MenuItem about = new MenuItem();

  CheckboxMenuItem seqLimits = new CheckboxMenuItem();

  CheckboxMenuItem centreColumnLabelFlag = new CheckboxMenuItem();

  CheckboxMenuItem followMouseOverFlag = new CheckboxMenuItem();

  CheckboxMenuItem showSequenceLogo = new CheckboxMenuItem();

  CheckboxMenuItem applyAutoAnnotationSettings = new CheckboxMenuItem();

  CheckboxMenuItem showConsensusHistogram = new CheckboxMenuItem();

  CheckboxMenuItem showGroupConsensus = new CheckboxMenuItem();

  CheckboxMenuItem showGroupConservation = new CheckboxMenuItem();

  CheckboxMenuItem normSequenceLogo = new CheckboxMenuItem();

  /**
   * Initialise menus and other items
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    setMenuBar(alignFrameMenuBar);

    /*
     * Configure File menu items and actions
     */
    inputText
            .setLabel(MessageManager.getString("label.input_from_textbox"));
    inputText.addActionListener(this);
    Menu outputTextboxMenu = new Menu(
            MessageManager.getString("label.out_to_textbox"));
    for (String ff : FileFormats.getInstance().getWritableFormats(true))
    {
      MenuItem item = new MenuItem(ff);

      item.addActionListener(new java.awt.event.ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          outputText_actionPerformed(e);
        }
      });

      outputTextboxMenu.add(item);
    }
    closeMenuItem.addActionListener(this);
    loadApplication.addActionListener(this);
    loadTree.addActionListener(this);
    loadAnnotations.addActionListener(this);
    outputFeatures.addActionListener(this);
    outputAnnotations.addActionListener(this);

    /*
     * Configure Edit menu items and actions
     */
    undoMenuItem.setEnabled(false);
    undoMenuItem.setLabel(MessageManager.getString("action.undo"));
    undoMenuItem.addActionListener(this);
    redoMenuItem.setEnabled(false);
    redoMenuItem.setLabel(MessageManager.getString("action.redo"));
    redoMenuItem.addActionListener(this);
    copy.setLabel(MessageManager.getString("action.copy"));
    copy.addActionListener(this);
    cut.setLabel(MessageManager.getString("action.cut"));
    cut.addActionListener(this);
    delete.setLabel(MessageManager.getString("action.delete"));
    delete.addActionListener(this);
    pasteMenu.setLabel(MessageManager.getString("action.paste"));
    pasteNew.setLabel(MessageManager.getString("label.to_new_alignment"));
    pasteNew.addActionListener(this);
    pasteThis.setLabel(MessageManager.getString("label.to_this_alignment"));
    pasteThis.addActionListener(this);
    remove2LeftMenuItem
            .setLabel(MessageManager.getString("action.remove_left"));
    remove2LeftMenuItem.addActionListener(this);
    remove2RightMenuItem
            .setLabel(MessageManager.getString("action.remove_right"));
    remove2RightMenuItem.addActionListener(this);
    removeGappedColumnMenuItem.setLabel(
            MessageManager.getString("action.remove_empty_columns"));
    removeGappedColumnMenuItem.addActionListener(this);
    removeAllGapsMenuItem
            .setLabel(MessageManager.getString("action.remove_all_gaps"));
    removeAllGapsMenuItem.addActionListener(this);
    removeRedundancyMenuItem
            .setLabel(MessageManager.getString("action.remove_redundancy"));
    removeRedundancyMenuItem.addActionListener(this);

    /*
     * Configure Select menu items and actions
     */
    findMenuItem.setLabel(MessageManager.getString("action.find"));
    findMenuItem.addActionListener(this);
    selectAllSequenceMenuItem.addActionListener(this);
    deselectAllSequenceMenuItem.addActionListener(this);
    invertSequenceMenuItem.setLabel(
            MessageManager.getString("action.invert_sequence_selection"));
    invertSequenceMenuItem.addActionListener(this);
    invertColSel.setLabel(
            MessageManager.getString("action.invert_column_selection"));
    invertColSel.addActionListener(this);
    deleteGroups
            .setLabel(MessageManager.getString("action.undefine_groups"));
    deleteGroups.addActionListener(this);
    grpsFromSelection.setLabel(
            MessageManager.getString("action.make_groups_selection"));
    grpsFromSelection.addActionListener(this);
    createGroup.setLabel(MessageManager.getString("action.create_group"));
    createGroup.addActionListener(this);
    unGroup.setLabel(MessageManager.getString("action.remove_group"));
    unGroup.addActionListener(this);

    annotationColumnSelection.setLabel(
            MessageManager.getString("action.select_by_annotation"));
    annotationColumnSelection.addActionListener(this);

    /*
     * Configure View menu items and actions
     */
    newView.setLabel(MessageManager.getString("action.new_view"));
    newView.addActionListener(this);
    Menu showMenu = new Menu(MessageManager.getString("action.show"));
    showColumns.setLabel(MessageManager.getString("label.all_columns"));
    showSeqs.setLabel(MessageManager.getString("label.all_sequences"));
    Menu hideMenu = new Menu(MessageManager.getString("action.hide"));
    hideColumns
            .setLabel(MessageManager.getString("label.selected_columns"));
    hideSequences
            .setLabel(MessageManager.getString("label.selected_sequences"));
    hideAllButSelection.setLabel(
            MessageManager.getString("label.all_but_selected_region"));
    hideAllSelection
            .setLabel(MessageManager.getString("label.selected_region"));
    showAllHidden.setLabel(
            MessageManager.getString("label.all_sequences_columns"));
    showColumns.addActionListener(this);
    showSeqs.addActionListener(this);
    hideColumns.addActionListener(this);
    hideSequences.addActionListener(this);
    hideAllButSelection.addActionListener(this);
    hideAllSelection.addActionListener(this);
    showAllHidden.addActionListener(this);
    featureSettings
            .setLabel(MessageManager.getString("action.feature_settings"));
    featureSettings.addActionListener(this);
    sequenceFeatures.setLabel(
            MessageManager.getString("label.show_sequence_features"));
    sequenceFeatures.addItemListener(this);
    sequenceFeatures.setState(false);
    followMouseOverFlag.setLabel(
            MessageManager.getString("label.automatic_scrolling"));
    followMouseOverFlag.addItemListener(this);
    alProperties.addActionListener(this);
    overviewMenuItem
            .setLabel(MessageManager.getString("label.overview_window"));
    overviewMenuItem.addActionListener(this);

    /*
     * Configure Annotations menu items and actions
     */
    annotationPanelMenuItem
            .setLabel(MessageManager.getString("label.show_annotations"));
    annotationPanelMenuItem.addItemListener(this);
    showGroupConsensus
            .setLabel(MessageManager.getString("label.group_consensus"));
    showGroupConservation
            .setLabel(MessageManager.getString("label.group_conservation"));
    showConsensusHistogram.setLabel(
            MessageManager.getString("label.show_consensus_histogram"));
    showSequenceLogo.setLabel(
            MessageManager.getString("label.show_consensus_logo"));
    normSequenceLogo.setLabel(
            MessageManager.getString("label.norm_consensus_logo"));
    applyAutoAnnotationSettings
            .setLabel(MessageManager.getString("label.apply_all_groups"));
    applyAutoAnnotationSettings.setState(true);
    Menu autoAnnMenu = new Menu(
            MessageManager.getString("label.autocalculated_annotation"));
    showGroupConsensus.addItemListener(this);
    showGroupConservation.addItemListener(this);
    showConsensusHistogram.addItemListener(this);
    showSequenceLogo.addItemListener(this);
    normSequenceLogo.addItemListener(this);
    applyAutoAnnotationSettings.addItemListener(this);
    showAlignmentAnnotations = new CheckboxMenuItem(
            MessageManager.getString("label.show_all_al_annotations"));
    showSequenceAnnotations = new CheckboxMenuItem(
            MessageManager.getString("label.show_all_seq_annotations"));
    sortAnnBySequence = new CheckboxMenuItem(
            MessageManager.getString("label.sort_annotations_by_sequence"));
    sortAnnByLabel = new CheckboxMenuItem(
            MessageManager.getString("label.sort_annotations_by_label"));
    showAutoFirst = new CheckboxMenuItem(
            MessageManager.getString("label.show_first"));
    showAutoFirst.setState(false); // pending applet parameter
    setShowAutoCalculatedAbove(showAutoFirst.getState());
    showAutoLast = new CheckboxMenuItem(
            MessageManager.getString("label.show_last"));
    showAutoLast.setState(!showAutoFirst.getState());
    showAlignmentAnnotations.addItemListener(this);
    showSequenceAnnotations.addItemListener(this);
    sortAnnBySequence.addItemListener(this);
    sortAnnByLabel.addItemListener(this);
    showAutoFirst.addItemListener(this);
    showAutoLast.addItemListener(this);

    /*
     * Configure Format menu items and actions
     */
    font.setLabel(MessageManager.getString("action.font"));
    font.addActionListener(this);
    scaleAbove.setLabel(MessageManager.getString("action.scale_above"));
    scaleAbove.setState(true);
    scaleAbove.setEnabled(false);
    scaleAbove.addItemListener(this);
    scaleLeft.setEnabled(false);
    scaleLeft.setState(true);
    scaleLeft.setLabel(MessageManager.getString("action.scale_left"));
    scaleLeft.addItemListener(this);
    scaleRight.setEnabled(false);
    scaleRight.setState(true);
    scaleRight.setLabel(MessageManager.getString("action.scale_right"));
    scaleRight.addItemListener(this);
    viewBoxesMenuItem.setLabel(MessageManager.getString("action.boxes"));
    viewBoxesMenuItem.setState(true);
    viewBoxesMenuItem.addItemListener(this);
    viewTextMenuItem.setLabel(MessageManager.getString("action.text"));
    viewTextMenuItem.setState(true);
    viewTextMenuItem.addItemListener(this);
    colourTextMenuItem
            .setLabel(MessageManager.getString("label.colour_text"));
    colourTextMenuItem.addItemListener(this);
    displayNonconservedMenuItem
            .setLabel(MessageManager.getString("label.show_non_conserved"));
    displayNonconservedMenuItem.addItemListener(this);
    wrapMenuItem.setLabel(MessageManager.getString("action.wrap"));
    wrapMenuItem.addItemListener(this);
    renderGapsMenuItem
            .setLabel(MessageManager.getString("action.show_gaps"));
    renderGapsMenuItem.setState(true);
    renderGapsMenuItem.addItemListener(this);
    centreColumnLabelFlag.setLabel(
            MessageManager.getString("label.centre_column_labels"));
    centreColumnLabelFlag.addItemListener(this);
    seqLimits.setState(true);
    seqLimits.setLabel(
            MessageManager.getString("label.show_sequence_limits"));
    seqLimits.addItemListener(this);

    /*
     * Configure Colour menu items and actions
     */
    applyToAllGroups.setLabel(
            MessageManager.getString("label.apply_colour_to_all_groups"));
    applyToAllGroups.setState(true);
    applyToAllGroups.addItemListener(this);
    clustalColour.setLabel(
            MessageManager.getString("label.colourScheme_clustal"));
    clustalColour.addActionListener(this);
    zappoColour
            .setLabel(MessageManager.getString("label.colourScheme_zappo"));
    zappoColour.addActionListener(this);
    taylorColour.setLabel(
            MessageManager.getString("label.colourScheme_taylor"));
    taylorColour.addActionListener(this);
    hydrophobicityColour.setLabel(
            MessageManager.getString("label.colourScheme_hydrophobic"));
    hydrophobicityColour.addActionListener(this);
    helixColour.setLabel(
            MessageManager.getString("label.colourScheme_helixpropensity"));
    helixColour.addActionListener(this);
    strandColour.setLabel(MessageManager
            .getString("label.colourScheme_strandpropensity"));
    strandColour.addActionListener(this);
    turnColour.setLabel(
            MessageManager.getString("label.colourScheme_turnpropensity"));
    turnColour.addActionListener(this);
    buriedColour.setLabel(
            MessageManager.getString("label.colourScheme_buriedindex"));
    buriedColour.addActionListener(this);
    purinePyrimidineColour.setLabel(MessageManager
            .getString("label.colourScheme_purine/pyrimidine"));
    purinePyrimidineColour.addActionListener(this);
    // RNAInteractionColour.setLabel(MessageManager
    // .getString("label.rna_interaction"));
    // RNAInteractionColour.addActionListener(this);
    RNAHelixColour.setLabel(
            MessageManager.getString("label.colourScheme_rnahelices"));
    RNAHelixColour.addActionListener(this);
    userDefinedColour
            .setLabel(MessageManager.getString("action.user_defined"));
    userDefinedColour.addActionListener(this);
    PIDColour.setLabel(
            MessageManager.getString("label.colourScheme_%identity"));
    PIDColour.addActionListener(this);
    BLOSUM62Colour.setLabel(
            MessageManager.getString("label.colourScheme_blosum62"));
    BLOSUM62Colour.addActionListener(this);
    tcoffeeColour.setLabel(
            MessageManager.getString("label.colourScheme_t-coffeescores"));
    // it will be enabled only if a score file is provided
    tcoffeeColour.setEnabled(false);
    tcoffeeColour.addActionListener(this);
    conservationMenuItem
            .setLabel(MessageManager.getString("action.by_conservation"));
    conservationMenuItem.addItemListener(this);
    noColourmenuItem.setLabel(MessageManager.getString("label.none"));
    noColourmenuItem.addActionListener(this);
    abovePIDThreshold.setLabel(
            MessageManager.getString("label.above_identity_threshold"));
    abovePIDThreshold.addItemListener(this);
    nucleotideColour.setLabel(
            MessageManager.getString("label.colourScheme_nucleotide"));
    nucleotideColour.addActionListener(this);
    modifyPID.setLabel(
            MessageManager.getString("label.modify_identity_threshold"));
    modifyPID.setEnabled(abovePIDThreshold.getState());
    modifyPID.addActionListener(this);
    modifyConservation.setLabel(MessageManager
            .getString("label.modify_conservation_threshold"));
    modifyConservation.setEnabled(conservationMenuItem.getState());
    modifyConservation.addActionListener(this);
    annotationColour
            .setLabel(MessageManager.getString("action.by_annotation"));
    annotationColour.addActionListener(this);

    /*
     * Configure Calculate menu items and actions
     */
    sortPairwiseMenuItem
            .setLabel(MessageManager.getString("action.by_pairwise_id"));
    sortPairwiseMenuItem.addActionListener(this);
    sortIDMenuItem.setLabel(MessageManager.getString("action.by_id"));
    sortIDMenuItem.addActionListener(this);
    sortLengthMenuItem
            .setLabel(MessageManager.getString("action.by_length"));
    sortLengthMenuItem.addActionListener(this);
    sortGroupMenuItem.setLabel(MessageManager.getString("action.by_group"));
    sortGroupMenuItem.addActionListener(this);
    pairwiseAlignmentMenuItem.setLabel(
            MessageManager.getString("action.pairwise_alignment"));
    pairwiseAlignmentMenuItem.addActionListener(this);
    PCAMenuItem.setLabel(
            MessageManager.getString("label.principal_component_analysis"));
    PCAMenuItem.addActionListener(this);
    autoCalculate = new CheckboxMenuItem(
            MessageManager.getString("label.autocalculate_consensus"),
            true);
    averageDistanceTreeMenuItem.setLabel(
            MessageManager.getString("label.average_distance_identity"));
    averageDistanceTreeMenuItem.addActionListener(this);
    neighbourTreeMenuItem.setLabel(
            MessageManager.getString("label.neighbour_joining_identity"));
    neighbourTreeMenuItem.addActionListener(this);
    avDistanceTreeBlosumMenuItem.setLabel(
            MessageManager.getString("label.average_distance_blosum62"));
    avDistanceTreeBlosumMenuItem.addActionListener(this);
    njTreeBlosumMenuItem
            .setLabel(MessageManager.getString("label.neighbour_blosum62"));
    njTreeBlosumMenuItem.addActionListener(this);
    sortByTreeMenu
            .setLabel(MessageManager.getString("action.by_tree_order"));
    Menu sortMenu = new Menu(MessageManager.getString("action.sort"));
    Menu calculateTreeMenu = new Menu(
            MessageManager.getString("action.calculate_tree"));
    autoCalculate.addItemListener(this);
    sortByTree.addItemListener(this);

    /*
     * Configure Help menu items and actions
     */
    Menu helpMenu = new Menu(MessageManager.getString("action.help"));
    documentation.setLabel(MessageManager.getString("label.documentation"));
    documentation.addActionListener(this);
    about.setLabel(MessageManager.getString("label.about"));
    about.addActionListener(this);

    /*
     * Add top level menus to frame
     */
    alignFrameMenuBar.add(fileMenu);
    Menu editMenu = new Menu(MessageManager.getString("action.edit"));
    alignFrameMenuBar.add(editMenu);
    Menu selectMenu = new Menu(MessageManager.getString("action.select"));
    alignFrameMenuBar.add(selectMenu);
    Menu viewMenu = new Menu(MessageManager.getString("action.view"));
    alignFrameMenuBar.add(viewMenu);
    Menu annotationsMenu = new Menu(
            MessageManager.getString("action.annotations"));
    alignFrameMenuBar.add(annotationsMenu);
    Menu formatMenu = new Menu(MessageManager.getString("action.format"));
    alignFrameMenuBar.add(formatMenu);
    Menu colourMenu = new Menu(MessageManager.getString("action.colour"));
    alignFrameMenuBar.add(colourMenu);
    Menu calculateMenu = new Menu(
            MessageManager.getString("action.calculate"));
    alignFrameMenuBar.add(calculateMenu);
    alignFrameMenuBar.add(helpMenu);

    /*
     * File menu
     */
    fileMenu.add(inputText);
    fileMenu.add(loadTree);
    fileMenu.add(loadAnnotations);
    fileMenu.addSeparator();
    fileMenu.add(outputTextboxMenu);
    fileMenu.add(outputFeatures);
    fileMenu.add(outputAnnotations);
    if (jalviewServletURL != null)
    {
      fileMenu.add(loadApplication);
    }
    fileMenu.addSeparator();
    fileMenu.add(closeMenuItem);

    /*
     * Edit menu
     */
    editMenu.add(undoMenuItem);
    editMenu.add(redoMenuItem);
    editMenu.add(cut);
    editMenu.add(copy);
    pasteMenu.add(pasteNew);
    pasteMenu.add(pasteThis);
    editMenu.add(pasteMenu);
    editMenu.add(delete);
    editMenu.addSeparator();
    editMenu.add(remove2LeftMenuItem);
    editMenu.add(remove2RightMenuItem);
    editMenu.add(removeGappedColumnMenuItem);
    editMenu.add(removeAllGapsMenuItem);
    editMenu.add(removeRedundancyMenuItem);

    /*
     * Select menu
     */
    selectMenu.add(findMenuItem);
    selectMenu.addSeparator();
    selectMenu.add(selectAllSequenceMenuItem);
    selectMenu.add(deselectAllSequenceMenuItem);
    selectMenu.add(invertSequenceMenuItem);
    selectMenu.add(invertColSel);
    selectMenu.add(createGroup);
    selectMenu.add(unGroup);
    selectMenu.add(grpsFromSelection);
    selectMenu.add(deleteGroups);
    selectMenu.add(annotationColumnSelection);

    /*
     * View menu
     */
    viewMenu.add(newView);
    viewMenu.addSeparator();
    showMenu.add(showColumns);
    showMenu.add(showSeqs);
    showMenu.add(showAllHidden);
    viewMenu.add(showMenu);
    hideMenu.add(hideColumns);
    hideMenu.add(hideSequences);
    hideMenu.add(hideAllSelection);
    hideMenu.add(hideAllButSelection);
    viewMenu.add(hideMenu);
    viewMenu.addSeparator();
    viewMenu.add(followMouseOverFlag);
    viewMenu.addSeparator();
    viewMenu.add(sequenceFeatures);
    viewMenu.add(featureSettings);
    viewMenu.addSeparator();
    viewMenu.add(alProperties);
    viewMenu.addSeparator();
    viewMenu.add(overviewMenuItem);

    /*
     * Annotations menu
     */
    annotationsMenu.add(annotationPanelMenuItem);
    annotationsMenu.addSeparator();
    annotationsMenu.add(showAlignmentAnnotations);
    annotationsMenu.add(showSequenceAnnotations);
    annotationsMenu.add(sortAnnBySequence);
    annotationsMenu.add(sortAnnByLabel);
    annotationsMenu.addSeparator();
    autoAnnMenu.add(showAutoFirst);
    autoAnnMenu.add(showAutoLast);
    autoAnnMenu.addSeparator();
    autoAnnMenu.add(applyAutoAnnotationSettings);
    autoAnnMenu.add(showConsensusHistogram);
    autoAnnMenu.add(showSequenceLogo);
    autoAnnMenu.add(normSequenceLogo);
    autoAnnMenu.addSeparator();
    autoAnnMenu.add(showGroupConservation);
    autoAnnMenu.add(showGroupConsensus);
    annotationsMenu.add(autoAnnMenu);

    /*
     * Format menu
     */
    formatMenu.add(font);
    formatMenu.add(seqLimits);
    formatMenu.add(wrapMenuItem);
    formatMenu.add(scaleAbove);
    formatMenu.add(scaleLeft);
    formatMenu.add(scaleRight);
    formatMenu.add(viewBoxesMenuItem);
    formatMenu.add(viewTextMenuItem);
    formatMenu.add(colourTextMenuItem);
    formatMenu.add(displayNonconservedMenuItem);
    formatMenu.add(renderGapsMenuItem);
    formatMenu.add(centreColumnLabelFlag);

    /*
     * Colour menu
     */
    colourMenu.add(applyToAllGroups);
    colourMenu.addSeparator();
    colourMenu.add(noColourmenuItem);
    colourMenu.add(clustalColour);
    colourMenu.add(BLOSUM62Colour);
    colourMenu.add(PIDColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydrophobicityColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(nucleotideColour);
    colourMenu.add(purinePyrimidineColour);
    // colourMenu.add(RNAInteractionColour);
    colourMenu.add(tcoffeeColour);
    colourMenu.add(userDefinedColour);
    colourMenu.addSeparator();
    colourMenu.add(conservationMenuItem);
    colourMenu.add(modifyConservation);
    colourMenu.add(abovePIDThreshold);
    colourMenu.add(modifyPID);
    colourMenu.add(annotationColour);
    colourMenu.add(RNAHelixColour);

    /*
     * Calculate menu
     */
    sortMenu.add(sortIDMenuItem);
    sortMenu.add(sortLengthMenuItem);
    sortMenu.add(sortByTreeMenu);
    sortMenu.add(sortGroupMenuItem);
    sortMenu.add(sortPairwiseMenuItem);
    calculateMenu.add(sortMenu);
    calculateTreeMenu.add(averageDistanceTreeMenuItem);
    calculateTreeMenu.add(neighbourTreeMenuItem);
    calculateTreeMenu.add(avDistanceTreeBlosumMenuItem);
    calculateTreeMenu.add(njTreeBlosumMenuItem);
    calculateMenu.add(calculateTreeMenu);
    calculateMenu.addSeparator();
    calculateMenu.add(pairwiseAlignmentMenuItem);
    calculateMenu.add(PCAMenuItem);
    calculateMenu.add(autoCalculate);
    calculateMenu.add(sortByTree);

    /*
     * Help menu
     */
    helpMenu.add(documentation);
    helpMenu.add(about);

    /*
     * Status bar
     */
    statusBar.setBackground(Color.white);
    statusBar.setFont(new java.awt.Font("Verdana", 0, 11));
    setStatus(MessageManager.getString("label.status_bar"));
    this.add(statusBar, BorderLayout.SOUTH);
  }

  @Override
  public void setStatus(String string)
  {
    statusBar.setText(string);
  };

  MenuItem featureSettings = new MenuItem();

  CheckboxMenuItem sequenceFeatures = new CheckboxMenuItem();

  MenuItem annotationColour = new MenuItem();

  MenuItem annotationColumnSelection = new MenuItem();

  MenuItem invertColSel = new MenuItem();

  MenuItem showColumns = new MenuItem();

  MenuItem showSeqs = new MenuItem();

  MenuItem hideColumns = new MenuItem();

  MenuItem hideSequences = new MenuItem();

  MenuItem hideAllButSelection = new MenuItem();

  MenuItem hideAllSelection = new MenuItem();

  MenuItem showAllHidden = new MenuItem();

  MenuItem newView = new MenuItem();

  private CheckboxMenuItem showAlignmentAnnotations;

  private CheckboxMenuItem showSequenceAnnotations;

  private CheckboxMenuItem sortAnnBySequence;

  private CheckboxMenuItem sortAnnByLabel;

  private CheckboxMenuItem showAutoFirst;

  private CheckboxMenuItem showAutoLast;

  private SplitFrame splitFrame;

  /**
   * Attach the alignFrame panels after embedding menus, if necessary. This used
   * to be called setEmbedded, but is now creates the dropdown menus in a
   * platform independent manner to avoid OSX/Mac menu appendage daftness.
   * 
   * @param reallyEmbedded
   *          true to attach the view to the applet area on the page rather than
   *          in a new window
   */
  public void createAlignFrameWindow(boolean reallyEmbedded)
  {
    if (reallyEmbedded)
    {
      embedAlignFrameInApplet(viewport.applet);
    }
    else
    {
      // //////
      // test and embed menu bar if necessary.
      //
      if (embedMenuIfNeeded(alignPanel))
      {
        /*
         * adjust for status bar height too. ? pointless as overridden by layout
         * manager
         */
        alignPanel.setSize(getSize().width,
                getSize().height - statusBar.getHeight());
      }
      add(statusBar, BorderLayout.SOUTH);
      add(alignPanel, BorderLayout.CENTER);
      // and register with the applet so it can pass external API calls to us
      jalview.bin.JalviewLite.addFrame(this, this.getTitle(), frameWidth,
              frameHeight);
    }
  }

  /**
   * Add the components of this AlignFrame to the applet container.
   * 
   * @param theApplet
   */
  public void embedAlignFrameInApplet(final JalviewLite theApplet)
  {
    // ////
    // Explicitly build the embedded menu panel for the on-page applet
    //
    // view cannot be closed if its actually on the page
    fileMenu.remove(closeMenuItem);
    fileMenu.remove(3); // Remove Separator
    // construct embedded menu, using default font
    embeddedMenu = makeEmbeddedPopupMenu(alignFrameMenuBar, false, false);
    // and actually add the components to the applet area
    theApplet.setLayout(new BorderLayout());
    theApplet.add(embeddedMenu, BorderLayout.NORTH);
    theApplet.add(statusBar, BorderLayout.SOUTH);
    // TODO should size be left to the layout manager?
    alignPanel.setSize(theApplet.getSize().width, theApplet.getSize().height
            - embeddedMenu.getHeight() - statusBar.getHeight());
    theApplet.add(alignPanel, BorderLayout.CENTER);
    final AlignFrame me = this;
    theApplet.addFocusListener(new FocusListener()
    {

      @Override
      public void focusLost(FocusEvent e)
      {
        if (theApplet.currentAlignFrame == me)
        {
          theApplet.currentAlignFrame = null;
        }
      }

      @Override
      public void focusGained(FocusEvent e)
      {
        theApplet.currentAlignFrame = me;
      }
    });
    theApplet.validate();
  }

  /**
   * create a new binding between structures in an existing jmol viewer instance
   * and an alignpanel with sequences that have existing PDBFile entries. Note,
   * this does not open a new Jmol window, or modify the display of the
   * structures in the original jmol window. Note This method doesn't work
   * without an additional javascript library to exchange messages between the
   * distinct applets. See http://issues.jalview.org/browse/JAL-621
   * 
   * @param jmolViewer
   *          JmolViewer instance
   * @param sequenceIds
   *          - sequence Ids to search for associations
   */
  public SequenceStructureBinding addStructureViewInstance(
          Object jmolviewer, String[] sequenceIds)
  {
    Viewer viewer = null;
    try
    {
      viewer = (Viewer) jmolviewer;
    } catch (ClassCastException ex)
    {
      System.err.println(
              "Unsupported viewer object :" + jmolviewer.getClass());
    }
    if (viewer == null)
    {
      System.err.println("Can't use this object as a structure viewer:"
              + jmolviewer.getClass());
      return null;
    }
    SequenceI[] seqs = null;
    if (sequenceIds == null || sequenceIds.length == 0)
    {
      seqs = viewport.getAlignment().getSequencesArray();
    }
    else
    {
      Vector sqi = new Vector();
      AlignmentI al = viewport.getAlignment();
      for (int sid = 0; sid < sequenceIds.length; sid++)
      {
        SequenceI sq = al.findName(sequenceIds[sid]);
        if (sq != null)
        {
          sqi.addElement(sq);
        }
      }
      if (sqi.size() > 0)
      {
        seqs = new SequenceI[sqi.size()];
        for (int sid = 0, sSize = sqi.size(); sid < sSize; sid++)
        {
          seqs[sid] = (SequenceI) sqi.elementAt(sid);
        }
      }
      else
      {
        return null;
      }
    }
    AAStructureBindingModel jmv = null;
    // TODO: search for a jmv that involves viewer
    if (jmv == null)
    { // create a new viewer/jalview binding.
      jmv = new ExtJmol(viewer, alignPanel, new SequenceI[][] { seqs });
    }
    return jmv;

  }

  /**
   * bind a pdb file to a sequence in the current view
   * 
   * @param sequenceId
   *          - sequenceId within the dataset.
   * @param pdbEntryString
   *          - the short name for the PDB file
   * @param pdbFile
   *          - pdb file - either a URL or a valid PDB file.
   * @return true if binding was as success TODO: consider making an exception
   *         structure for indicating when PDB parsing or sequenceId location
   *         fails.
   */
  public boolean addPdbFile(String sequenceId, String pdbEntryString,
          String pdbFile)
  {
    SequenceI toaddpdb = viewport.getAlignment().findName(sequenceId);
    boolean needtoadd = false;
    if (toaddpdb != null)
    {
      Vector pdbe = toaddpdb.getAllPDBEntries();
      PDBEntry pdbentry = null;
      if (pdbe != null && pdbe.size() > 0)
      {
        for (int pe = 0, peSize = pdbe.size(); pe < peSize; pe++)
        {
          pdbentry = (PDBEntry) pdbe.elementAt(pe);
          if (!pdbentry.getId().equals(pdbEntryString)
                  && !pdbentry.getFile().equals(pdbFile))
          {
            pdbentry = null;
          }
          else
          {
            continue;
          }
        }
      }
      if (pdbentry == null)
      {
        pdbentry = new PDBEntry();
        pdbentry.setId(pdbEntryString);
        pdbentry.setFile(pdbFile);
        needtoadd = true; // add this new entry to sequence.
      }
      // resolve data source
      // TODO: this code should be a refactored to an io package
      DataSourceType protocol = AppletFormatAdapter.resolveProtocol(pdbFile,
              FileFormat.PDB);
      if (protocol == null)
      {
        return false;
      }
      if (needtoadd)
      {
        pdbentry.setProperty("protocol", protocol);
        toaddpdb.addPDBId(pdbentry);
        alignPanel.getStructureSelectionManager()
                .registerPDBEntry(pdbentry);
      }
    }
    return true;
  }

  private Object[] cleanSeqChainArrays(SequenceI[] seqs, String[] chains)
  {
    if (seqs != null)
    {
      Vector sequences = new Vector();
      for (int i = 0; i < seqs.length; i++)
      {
        if (seqs[i] != null)
        {
          sequences
                  .addElement(new Object[]
                  { seqs[i], (chains != null) ? chains[i] : null });
        }
      }
      seqs = new SequenceI[sequences.size()];
      chains = new String[sequences.size()];
      for (int i = 0, isize = sequences.size(); i < isize; i++)
      {
        Object[] oj = (Object[]) sequences.elementAt(i);

        seqs[i] = (SequenceI) oj[0];
        chains[i] = (String) oj[1];
      }
    }
    return new Object[] { seqs, chains };

  }

  public void newStructureView(JalviewLite applet, PDBEntry pdb,
          SequenceI[] seqs, String[] chains, DataSourceType protocol)
  {
    // Scrub any null sequences from the array
    Object[] sqch = cleanSeqChainArrays(seqs, chains);
    seqs = (SequenceI[]) sqch[0];
    chains = (String[]) sqch[1];
    if (seqs == null || seqs.length == 0)
    {
      System.err.println(
              "JalviewLite.AlignFrame:newStructureView: No sequence to bind structure to.");
    }
    if (protocol == null)
    {
      String sourceType = (String) pdb.getProperty("protocol");
      try
      {
        protocol = DataSourceType.valueOf(sourceType);
      } catch (IllegalArgumentException e)
      {
        // ignore
      }
      if (protocol == null)
      {
        System.err.println("Couldn't work out protocol to open structure: "
                + pdb.getId());
        return;
      }
    }
    if (applet.useXtrnalSviewer)
    {
      // register the association(s) and quit, don't create any windows.
      if (StructureSelectionManager.getStructureSelectionManager(applet)
              .setMapping(seqs, chains, pdb.getFile(), protocol,
                      null) == null)
      {
        System.err.println("Failed to map " + pdb.getFile() + " ("
                + protocol + ") to any sequences");
      }
      return;
    }
    if (applet.isAlignPdbStructures() && applet.jmolAvailable)
    {
      // can only do alignments with Jmol
      // find the last jmol window assigned to this alignment
      AppletJmol ajm = null, tajm;
      Vector jmols = applet.getAppletWindow(AppletJmol.class);
      for (int i = 0, iSize = jmols.size(); i < iSize; i++)
      {
        tajm = (AppletJmol) jmols.elementAt(i);
        if (tajm.ap.alignFrame == this)
        {
          ajm = tajm;
          break;
        }
      }
      if (ajm != null)
      {
        System.err.println(
                "Incremental adding and aligning structure to existing Jmol view not yet implemented.");
        // try and add the pdb structure
        // ajm.addS
        ajm = null;
      }
    }
    // otherwise, create a new window
    if (applet.jmolAvailable)
    {
      new AppletJmol(pdb, seqs, chains, alignPanel, protocol);
      applet.lastFrameX += 40;
      applet.lastFrameY += 40;
    }
    else
    {
      new mc_view.AppletPDBViewer(pdb, seqs, chains, alignPanel, protocol);
    }

  }

  public void alignedStructureView(JalviewLite applet, PDBEntry[] pdb,
          SequenceI[][] seqs, String[][] chains, String[] protocols)
  {
    // TODO Auto-generated method stub
    System.err.println("Aligned Structure View: Not yet implemented.");
  }

  /**
   * modify the current selection, providing the user has not made a selection
   * already.
   * 
   * @param sel
   *          - sequences from this alignment
   * @param csel
   *          - columns to be selected on the alignment
   */
  public void select(SequenceGroup sel, ColumnSelection csel,
          HiddenColumns hidden)
  {
    alignPanel.seqPanel.selection(sel, csel, hidden, null);
  }

  public void scrollTo(int row, int column)
  {
    alignPanel.seqPanel.scrollTo(row, column);
  }

  public void scrollToRow(int row)
  {
    alignPanel.seqPanel.scrollToRow(row);
  }

  public void scrollToColumn(int column)
  {
    alignPanel.seqPanel.scrollToColumn(column);
  }

  /**
   * @return the alignments unique ID.
   */
  public String getSequenceSetId()
  {
    return viewport.getSequenceSetId();
  }

  /**
   * Load the (T-Coffee) score file from the specified url
   * 
   * @param source
   *          File/URL/T-COFFEE score file contents
   * @throws IOException
   * @return true if alignment was annotated with data from source
   */
  public boolean loadScoreFile(String source) throws IOException
  {

    TCoffeeScoreFile file = new TCoffeeScoreFile(source,
            AppletFormatAdapter.checkProtocol(source));
    if (!file.isValid())
    {
      // TODO: raise dialog for gui
      System.err.println("Problems parsing T-Coffee scores: "
              + file.getWarningMessage());
      System.err.println("Origin was:\n" + source);
      return false;
    }

    /*
     * check that the score matrix matches the alignment dimensions
     */
    AlignmentI aln;
    if ((aln = viewport.getAlignment()) != null
            && (aln.getHeight() != file.getHeight()
                    || aln.getWidth() != file.getWidth()))
    {
      // TODO: raise a dialog box here rather than bomb out.
      System.err.println(
              "The scores matrix does not match the alignment dimensions");

    }

    // TODO add parameter to indicate if matching should be done
    if (file.annotateAlignment(alignPanel.getAlignment(), false))
    {
      alignPanel.fontChanged();
      tcoffeeColour.setEnabled(true);
      // switch to this color
      changeColour(new TCoffeeColourScheme(alignPanel.getAlignment()));
      return true;
    }
    else
    {
      System.err.println("Problems resolving T-Coffee scores:");
      if (file.getWarningMessage() != null)
      {
        System.err.println(file.getWarningMessage());
      }
    }
    return false;
  }

  public SplitFrame getSplitFrame()
  {
    return this.splitFrame;
  }

  public void setSplitFrame(SplitFrame sf)
  {
    this.splitFrame = sf;
  }

  // may not need this
  @Override
  public void setShowSeqFeatures(boolean b)
  {
    // showSeqFeatures.setSelected(b);
    viewport.setShowSequenceFeatures(b);

  }

  @Override
  public void setMenusForViewport()
  {
    // setMenusFromViewport(viewport);

  }

  @Override
  public void refreshFeatureUI(boolean enableIfNecessary)
  {
    if (enableIfNecessary)
    {
      sequenceFeatures.setState(true);
      alignPanel.av.setShowSequenceFeatures(true);
    }
  }

  @Override
  public FeatureSettingsControllerI getFeatureSettingsUI()
  {
    return alignPanel.av.featureSettings;
  }

  @Override
  public FeatureSettingsControllerI showFeatureSettingsUI()
  {
    return new FeatureSettings(alignPanel);
  }

  private Rectangle fs_bounds = null;

  @Override
  public void setFeatureSettingsGeometry(Rectangle bounds)
  {
    fs_bounds = bounds;
  }

  @Override
  public Rectangle getFeatureSettingsGeometry()
  {
    return fs_bounds;
  }

}
