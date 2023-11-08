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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import jalview.analysis.AlignSeq;
import jalview.analysis.AlignmentUtils;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.GroupSet;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;
import jalview.util.Comparison;
import jalview.util.MessageManager;
import jalview.util.Platform;

/**
 * The panel that holds the labels for alignment annotations, providing
 * tooltips, context menus, drag to reorder rows, and drag to adjust panel
 * height
 */
public class AnnotationLabels extends JPanel
        implements MouseListener, MouseMotionListener, ActionListener
{
  private static final String HTML_END_TAG = "</html>";

  private static final String HTML_START_TAG = "<html>";

  /**
   * width in pixels within which height adjuster arrows are shown and active
   */
  private static final int HEIGHT_ADJUSTER_WIDTH = 50;

  /**
   * height in pixels for allowing height adjuster to be active
   */
  private static int HEIGHT_ADJUSTER_HEIGHT = 10;

  private static final Font font = new Font("Arial", Font.PLAIN, 11);

  private static final String TOGGLE_LABELSCALE = MessageManager
          .getString("label.scale_label_to_column");

  private static final String ADDNEW = MessageManager
          .getString("label.add_new_row");

  private static final String EDITNAME = MessageManager
          .getString("label.edit_label_description");

  private static final String HIDE = MessageManager
          .getString("label.hide_row");

  private static final String DELETE = MessageManager
          .getString("label.delete_row");

  private static final String SHOWALL = MessageManager
          .getString("label.show_all_hidden_rows");

  private static final String OUTPUT_TEXT = MessageManager
          .getString("label.export_annotation");

  private static final String COPYCONS_SEQ = MessageManager
          .getString("label.copy_consensus_sequence");

  private final boolean debugRedraw = false;

  private AlignmentPanel ap;

  AlignViewport av;

  private MouseEvent dragEvent;

  private int oldY;

  private int selectedRow;

  private int scrollOffset = 0;

  private boolean hasHiddenRows;

  private boolean resizePanel = false;

  /**
   * Creates a new AnnotationLabels object
   * 
   * @param ap
   */
  public AnnotationLabels(AlignmentPanel ap)
  {

    this.ap = ap;
    av = ap.av;
    ToolTipManager.sharedInstance().registerComponent(this);

    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(ap.getAnnotationPanel());
  }

  public AnnotationLabels(AlignViewport av)
  {
    this.av = av;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param y
   *          DOCUMENT ME!
   */
  public void setScrollOffset(int y)
  {
    scrollOffset = y;
    repaint();
  }

  /**
   * sets selectedRow to -2 if no annotation preset, -1 if no visible row is at
   * y
   * 
   * @param y
   *          coordinate position to search for a row
   */
  void getSelectedRow(int y)
  {
    int height = 0;
    AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();
    selectedRow = -2;
    if (aa != null)
    {
      for (int i = 0; i < aa.length; i++)
      {
        selectedRow = -1;
        if (!aa[i].visible)
        {
          continue;
        }

        height += aa[i].height;

        if (y < height)
        {
          selectedRow = i;

          break;
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();

    String action = evt.getActionCommand();
    if (ADDNEW.equals(action))
    {
      /*
       * non-returning dialog
       */
      AlignmentAnnotation newAnnotation = new AlignmentAnnotation(null,
              null, new Annotation[ap.av.getAlignment().getWidth()]);
      editLabelDescription(newAnnotation, true);
    }
    else if (EDITNAME.equals(action))
    {
      /*
       * non-returning dialog
       */
      editLabelDescription(aa[selectedRow], false);
    }
    else if (HIDE.equals(action))
    {
      aa[selectedRow].visible = false;
    }
    else if (DELETE.equals(action))
    {
      ap.av.getAlignment().deleteAnnotation(aa[selectedRow]);
      ap.av.getCalcManager().removeWorkerForAnnotation(aa[selectedRow]);
    }
    else if (SHOWALL.equals(action))
    {
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible && aa[i].annotations != null)
        {
          aa[i].visible = true;
        }
      }
    }
    else if (OUTPUT_TEXT.equals(action))
    {
      new AnnotationExporter(ap).exportAnnotation(aa[selectedRow]);
    }
    else if (COPYCONS_SEQ.equals(action))
    {
      SequenceI cons = null;
      if (aa[selectedRow].groupRef != null)
      {
        cons = aa[selectedRow].groupRef.getConsensusSeq();
      }
      else
      {
        cons = av.getConsensusSeq();
      }
      if (cons != null)
      {
        copy_annotseqtoclipboard(cons);
      }
    }
    else if (TOGGLE_LABELSCALE.equals(action))
    {
      aa[selectedRow].scaleColLabel = !aa[selectedRow].scaleColLabel;
    }

    ap.refresh(true);
  }

  /**
   * Shows a dialog where the annotation name and description may be edited. If
   * parameter addNew is true, then on confirmation, a new AlignmentAnnotation
   * is added, else an existing annotation is updated.
   * 
   * @param annotation
   * @param addNew
   */
  void editLabelDescription(AlignmentAnnotation annotation, boolean addNew)
  {
    String name = MessageManager.getString("label.annotation_name");
    String description = MessageManager
            .getString("label.annotation_description");
    String title = MessageManager
            .getString("label.edit_annotation_name_description");
    EditNameDialog dialog = new EditNameDialog(annotation.label,
            annotation.description, name, description);

    dialog.showDialog(ap.alignFrame, title, () -> {
      annotation.label = dialog.getName();
      String text = dialog.getDescription();
      if (text != null && text.length() == 0)
      {
        text = null;
      }
      annotation.description = text;
      if (addNew)
      {
        ap.av.getAlignment().addAnnotation(annotation);
        ap.av.getAlignment().setAnnotationIndex(annotation, 0);
      }
      ap.refresh(true);
    });
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    getSelectedRow(evt.getY() - getScrollOffset());
    oldY = evt.getY();
    if (evt.isPopupTrigger())
    {
      showPopupMenu(evt);
    }
  }

  /**
   * Build and show the Pop-up menu at the right-click mouse position
   * 
   * @param evt
   */
  void showPopupMenu(MouseEvent evt)
  {
    evt.consume();
    final AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();

    JPopupMenu pop = new JPopupMenu(
            MessageManager.getString("label.annotations"));
    JMenuItem item = new JMenuItem(ADDNEW);
    item.addActionListener(this);
    pop.add(item);
    if (selectedRow < 0)
    {
      if (hasHiddenRows)
      { // let the user make everything visible again
        item = new JMenuItem(SHOWALL);
        item.addActionListener(this);
        pop.add(item);
      }
      pop.show(this, evt.getX(), evt.getY());
      return;
    }
    item = new JMenuItem(EDITNAME);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(HIDE);
    item.addActionListener(this);
    pop.add(item);
    // JAL-1264 hide all sequence-specific annotations of this type
    if (selectedRow < aa.length)
    {
      if (aa[selectedRow].sequenceRef != null)
      {
        final String label = aa[selectedRow].label;
        JMenuItem hideType = new JMenuItem();
        String text = MessageManager.getString("label.hide_all") + " "
                + label;
        hideType.setText(text);
        hideType.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            AlignmentUtils.showOrHideSequenceAnnotations(
                    ap.av.getAlignment(), Collections.singleton(label),
                    null, false, false);
            ap.refresh(true);
          }
        });
        pop.add(hideType);
      }
    }
    item = new JMenuItem(DELETE);
    item.addActionListener(this);
    pop.add(item);
    if (hasHiddenRows)
    {
      item = new JMenuItem(SHOWALL);
      item.addActionListener(this);
      pop.add(item);
    }
    item = new JMenuItem(OUTPUT_TEXT);
    item.addActionListener(this);
    pop.add(item);
    // TODO: annotation object should be typed for autocalculated/derived
    // property methods
    if (selectedRow < aa.length)
    {
      final String label = aa[selectedRow].label;
      if (!aa[selectedRow].autoCalculated)
      {
        if (aa[selectedRow].graph == AlignmentAnnotation.NO_GRAPH)
        {
          // display formatting settings for this row.
          pop.addSeparator();
          // av and sequencegroup need to implement same interface for
          item = new JCheckBoxMenuItem(TOGGLE_LABELSCALE,
                  aa[selectedRow].scaleColLabel);
          item.addActionListener(this);
          pop.add(item);
        }
      }
      else if (label.indexOf("Consensus") > -1)
      {
        addConsensusMenuOptions(ap, aa[selectedRow], pop);

        final JMenuItem consclipbrd = new JMenuItem(COPYCONS_SEQ);
        consclipbrd.addActionListener(this);
        pop.add(consclipbrd);
      }

      addColourOrFilterByOptions(ap,aa[selectedRow],pop);
      
      if (aa[selectedRow].graph == AlignmentAnnotation.CONTACT_MAP)
      {
        addContactMatrixOptions(ap,aa[selectedRow],pop);
          // Set/adjust threshold for grouping ?
          // colour alignment by this [type]
          // select/hide columns by this row
          
        }
      }
    
    pop.show(this, evt.getX(), evt.getY());
  }

  static void addColourOrFilterByOptions(final AlignmentPanel ap,
          final AlignmentAnnotation alignmentAnnotation, final JPopupMenu pop)
  {
    JMenuItem item;
    item = new JMenuItem(MessageManager.getString("label.colour_by_annotation"));
    item.addActionListener(new ActionListener()
    {
      
      @Override
      public void actionPerformed(ActionEvent e)
      {
        AnnotationColourChooser.displayFor(ap.av, ap,alignmentAnnotation,false);
      };
    });
    pop.add(item);
    if (alignmentAnnotation.sequenceRef!=null)
    {
      item = new JMenuItem(MessageManager.getString("label.colour_by_annotation")+" ("+MessageManager.getString("label.per_seq")+")");
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          AnnotationColourChooser.displayFor(ap.av, ap,alignmentAnnotation,true);
        };
      });
      pop.add(item);
    }
    item = new JMenuItem(MessageManager.getString("action.select_by_annotation"));
    item.addActionListener(new ActionListener()
    {
      
      @Override
      public void actionPerformed(ActionEvent e)
      {
        AnnotationColumnChooser.displayFor(ap.av,ap,alignmentAnnotation);
      };
    });
    pop.add(item);
  }
  static void addContactMatrixOptions(final AlignmentPanel ap,
          final AlignmentAnnotation alignmentAnnotation, final JPopupMenu pop)
  {
    
    final ContactMatrixI cm = ap.av.getContactMatrix(alignmentAnnotation);
    JMenuItem item;
    if (cm != null)
    {
      pop.addSeparator();

      if (cm.hasGroups())
      {
        JCheckBoxMenuItem chitem = new JCheckBoxMenuItem(MessageManager.getString("action.show_groups_on_matrix"));
        chitem.setToolTipText(MessageManager.getString("action.show_groups_on_matrix_tooltip"));
        boolean showGroups = alignmentAnnotation.isShowGroupsForContactMatrix();
        final AlignmentAnnotation sel_row=alignmentAnnotation;
        chitem.setState(showGroups);
        chitem.addActionListener(new ActionListener()
        {

          @Override
          public void actionPerformed(ActionEvent e)
          {
            sel_row.setShowGroupsForContactMatrix(chitem.getState());
            // so any annotation colour changes are propagated - though they
            // probably won't be unless the annotation row colours are removed
            // too!
            ap.alignmentChanged();
          }
        });
        pop.add(chitem);
      }
      if (cm.hasTree())
      {
        item = new JMenuItem(MessageManager.getString("action.show_tree_for_matrix"));
        item.setToolTipText(MessageManager.getString("action.show_tree_for_matrix_tooltip"));
        item.addActionListener(new ActionListener()
        {

          @Override
          public void actionPerformed(ActionEvent e)
          {

            ap.alignFrame.showContactMapTree(alignmentAnnotation, cm);

          }
        });
        pop.add(item);
      }
      else
      {
        item = new JMenuItem(MessageManager.getString("action.cluster_matrix"));
        item.setToolTipText(MessageManager.getString("action.cluster_matrix_tooltip"));
        item.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            new Thread(new Runnable()
            {
              @Override
              public void run()
              {
                final long progBar;
                ap.alignFrame.setProgressBar(MessageManager.formatMessage("action.clustering_matrix_for",cm.getAnnotDescr(),5f), progBar = System.currentTimeMillis());
                cm.setGroupSet(GroupSet.makeGroups(cm, true));
                cm.randomlyReColourGroups();
                cm.transferGroupColorsTo(alignmentAnnotation);
                ap.alignmentChanged();
                ap.alignFrame.showContactMapTree(alignmentAnnotation, cm);
                ap.alignFrame.setProgressBar(null, progBar);
              }
            }).start();
          }
        });
        pop.add(item);
      }
    }
  }

  /**
   * A helper method that adds menu options for calculation and visualisation of
   * group and/or alignment consensus annotation to a popup menu. This is
   * designed to be reusable for either unwrapped mode (popup menu is shown on
   * component AnnotationLabels), or wrapped mode (popup menu is shown on
   * IdPanel when the mouse is over an annotation label).
   * 
   * @param ap
   * @param ann
   * @param pop
   */
  static void addConsensusMenuOptions(AlignmentPanel ap,
          AlignmentAnnotation ann, JPopupMenu pop)
  {
    pop.addSeparator();

    final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(
            MessageManager.getString("label.ignore_gaps_consensus"),
            (ann.groupRef != null) ? ann.groupRef.getIgnoreGapsConsensus()
                    : ap.av.isIgnoreGapsConsensus());
    final AlignmentAnnotation aaa = ann;
    cbmi.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (aaa.groupRef != null)
        {
          aaa.groupRef.setIgnoreGapsConsensus(cbmi.getState());
          ap.getAnnotationPanel()
                  .paint(ap.getAnnotationPanel().getGraphics());
        }
        else
        {
          ap.av.setIgnoreGapsConsensus(cbmi.getState(), ap);
        }
        ap.alignmentChanged();
      }
    });
    pop.add(cbmi);

    if (aaa.groupRef != null)
    {
      /*
       * group consensus options
       */
      final JCheckBoxMenuItem chist = new JCheckBoxMenuItem(
              MessageManager.getString("label.show_group_histogram"),
              ann.groupRef.isShowConsensusHistogram());
      chist.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          aaa.groupRef.setShowConsensusHistogram(chist.getState());
          ap.repaint();
        }
      });
      pop.add(chist);
      final JCheckBoxMenuItem cprofl = new JCheckBoxMenuItem(
              MessageManager.getString("label.show_group_logo"),
              ann.groupRef.isShowSequenceLogo());
      cprofl.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          aaa.groupRef.setshowSequenceLogo(cprofl.getState());
          ap.repaint();
        }
      });
      pop.add(cprofl);
      final JCheckBoxMenuItem cproflnorm = new JCheckBoxMenuItem(
              MessageManager.getString("label.normalise_group_logo"),
              ann.groupRef.isNormaliseSequenceLogo());
      cproflnorm.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          aaa.groupRef.setNormaliseSequenceLogo(cproflnorm.getState());
          // automatically enable logo display if we're clicked
          aaa.groupRef.setshowSequenceLogo(true);
          ap.repaint();
        }
      });
      pop.add(cproflnorm);
    }
    else
    {
      /*
       * alignment consensus options
       */
      final JCheckBoxMenuItem chist = new JCheckBoxMenuItem(
              MessageManager.getString("label.show_histogram"),
              ap.av.isShowConsensusHistogram());
      chist.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          ap.av.setShowConsensusHistogram(chist.getState());
          ap.alignFrame.setMenusForViewport();
          ap.repaint();
        }
      });
      pop.add(chist);
      final JCheckBoxMenuItem cprof = new JCheckBoxMenuItem(
              MessageManager.getString("label.show_logo"),
              ap.av.isShowSequenceLogo());
      cprof.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          ap.av.setShowSequenceLogo(cprof.getState());
          ap.alignFrame.setMenusForViewport();
          ap.repaint();
        }
      });
      pop.add(cprof);
      final JCheckBoxMenuItem cprofnorm = new JCheckBoxMenuItem(
              MessageManager.getString("label.normalise_logo"),
              ap.av.isNormaliseSequenceLogo());
      cprofnorm.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          ap.av.setShowSequenceLogo(true);
          ap.av.setNormaliseSequenceLogo(cprofnorm.getState());
          ap.alignFrame.setMenusForViewport();
          ap.repaint();
        }
      });
      pop.add(cprofnorm);
    }
  }

  /**
   * Reorders annotation rows after a drag of a label
   * 
   * @param evt
   */
  @Override
  public void mouseReleased(MouseEvent evt)
  {
    if (evt.isPopupTrigger())
    {
      showPopupMenu(evt);
      return;
    }

    int start = selectedRow;
    getSelectedRow(evt.getY() - getScrollOffset());
    int end = selectedRow;

    /*
     * if dragging to resize instead, start == end
     */
    if (start != end)
    {
      // Swap these annotations
      AlignmentAnnotation startAA = ap.av.getAlignment()
              .getAlignmentAnnotation()[start];
      if (end == -1)
      {
        end = ap.av.getAlignment().getAlignmentAnnotation().length - 1;
      }
      AlignmentAnnotation endAA = ap.av.getAlignment()
              .getAlignmentAnnotation()[end];

      ap.av.getAlignment().getAlignmentAnnotation()[end] = startAA;
      ap.av.getAlignment().getAlignmentAnnotation()[start] = endAA;
    }

    resizePanel = false;
    dragEvent = null;
    repaint();
    ap.getAnnotationPanel().repaint();
  }

  /**
   * Removes the height adjuster image on leaving the panel, unless currently
   * dragging it
   */
  @Override
  public void mouseExited(MouseEvent evt)
  {
    if (resizePanel && dragEvent == null)
    {
      resizePanel = false;
      repaint();
    }
  }

  /**
   * A mouse drag may be either an adjustment of the panel height (if flag
   * resizePanel is set on), or a reordering of the annotation rows. The former
   * is dealt with by this method, the latter in mouseReleased.
   * 
   * @param evt
   */
  @Override
  public void mouseDragged(MouseEvent evt)
  {
    dragEvent = evt;

    if (resizePanel)
    {
      Dimension d = ap.annotationScroller.getPreferredSize();
      int dif = evt.getY() - oldY;

      dif /= ap.av.getCharHeight();
      dif *= ap.av.getCharHeight();

      if ((d.height - dif) > 20)
      {
        ap.annotationScroller
                .setPreferredSize(new Dimension(d.width, d.height - dif));
        d = ap.annotationSpaceFillerHolder.getPreferredSize();
        ap.annotationSpaceFillerHolder
                .setPreferredSize(new Dimension(d.width, d.height - dif));
        ap.paintAlignment(true, false);
      }

      ap.addNotify();
    }
    else
    {
      repaint();
    }
  }

  /**
   * Updates the tooltip as the mouse moves over the labels
   * 
   * @param evt
   */
  @Override
  public void mouseMoved(MouseEvent evt)
  {
    showOrHideAdjuster(evt);

    getSelectedRow(evt.getY() - getScrollOffset());

    if (selectedRow > -1 && ap.av.getAlignment()
            .getAlignmentAnnotation().length > selectedRow)
    {
      AlignmentAnnotation[] anns = ap.av.getAlignment()
              .getAlignmentAnnotation();
      AlignmentAnnotation aa = anns[selectedRow];

      String desc = getTooltip(aa);
      this.setToolTipText(desc);
      String msg = getStatusMessage(aa, anns);
      ap.alignFrame.setStatus(msg);
    }
  }

  /**
   * Constructs suitable text to show in the status bar when over an annotation
   * label, containing the associated sequence name (if any), and the annotation
   * labels (or all labels for a graph group annotation)
   * 
   * @param aa
   * @param anns
   * @return
   */
  static String getStatusMessage(AlignmentAnnotation aa,
          AlignmentAnnotation[] anns)
  {
    if (aa == null)
    {
      return null;
    }

    StringBuilder msg = new StringBuilder(32);
    if (aa.sequenceRef != null)
    {
      msg.append(aa.sequenceRef.getName()).append(" : ");
    }

    if (aa.graphGroup == -1)
    {
      msg.append(aa.label);
    }
    else if (anns != null)
    {
      boolean first = true;
      for (int i = anns.length - 1; i >= 0; i--)
      {
        if (anns[i].graphGroup == aa.graphGroup)
        {
          if (!first)
          {
            msg.append(", ");
          }
          msg.append(anns[i].label);
          first = false;
        }
      }
    }

    return msg.toString();
  }

  /**
   * Answers a tooltip, formatted as html, containing the annotation description
   * (prefixed by associated sequence id if applicable), and the annotation
   * (non-positional) score if it has one. Answers null if neither description
   * nor score is found.
   * 
   * @param aa
   * @return
   */
  static String getTooltip(AlignmentAnnotation aa)
  {
    if (aa == null)
    {
      return null;
    }
    StringBuilder tooltip = new StringBuilder();
    if (aa.description != null && !aa.description.equals("New description"))
    {
      // TODO: we could refactor and merge this code with the code in
      // jalview.gui.SeqPanel.mouseMoved(..) that formats sequence feature
      // tooltips
      String desc = aa.getDescription(true).trim();
      if (!desc.toLowerCase(Locale.ROOT).startsWith(HTML_START_TAG))
      {
        tooltip.append(HTML_START_TAG);
        desc = desc.replace("<", "&lt;");
      }
      else if (desc.toLowerCase(Locale.ROOT).endsWith(HTML_END_TAG))
      {
        desc = desc.substring(0, desc.length() - HTML_END_TAG.length());
      }
      tooltip.append(desc);
    }
    else
    {
      // begin the tooltip's html fragment
      tooltip.append(HTML_START_TAG);
    }
    if (aa.hasScore())
    {
      if (tooltip.length() > HTML_START_TAG.length())
      {
        tooltip.append("<br/>");
      }
      // TODO: limit precision of score to avoid noise from imprecise
      // doubles
      // (64.7 becomes 64.7+/some tiny value).
      tooltip.append(" Score: ").append(String.valueOf(aa.score));
    }

    if (tooltip.length() > HTML_START_TAG.length())
    {
      return tooltip.append(HTML_END_TAG).toString();
    }

    /*
     * nothing in the tooltip (except "<html>")
     */
    return null;
  }

  /**
   * Shows the height adjuster image if the mouse moves into the top left
   * region, or hides it if the mouse leaves the regio
   * 
   * @param evt
   */
  protected void showOrHideAdjuster(MouseEvent evt)
  {
    boolean was = resizePanel;
    resizePanel = evt.getY() < HEIGHT_ADJUSTER_HEIGHT
            && evt.getX() < HEIGHT_ADJUSTER_WIDTH;

    if (resizePanel != was)
    {
      setCursor(Cursor
              .getPredefinedCursor(resizePanel ? Cursor.S_RESIZE_CURSOR
                      : Cursor.DEFAULT_CURSOR));
      repaint();
    }
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
    final AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();
    if (!evt.isPopupTrigger() && SwingUtilities.isLeftMouseButton(evt))
    {
      if (selectedRow > -1 && selectedRow < aa.length)
      {
        if (aa[selectedRow].groupRef != null)
        {
          if (evt.getClickCount() >= 2)
          {
            // todo: make the ap scroll to the selection - not necessary, first
            // click highlights/scrolls, second selects
            ap.getSeqPanel().ap.getIdPanel().highlightSearchResults(null);
            // process modifiers
            SequenceGroup sg = ap.av.getSelectionGroup();
            if (sg == null || sg == aa[selectedRow].groupRef
                    || !(Platform.isControlDown(evt) || evt.isShiftDown()))
            {
              if (Platform.isControlDown(evt) || evt.isShiftDown())
              {
                // clone a new selection group from the associated group
                ap.av.setSelectionGroup(
                        new SequenceGroup(aa[selectedRow].groupRef));
              }
              else
              {
                // set selection to the associated group so it can be edited
                ap.av.setSelectionGroup(aa[selectedRow].groupRef);
              }
            }
            else
            {
              // modify current selection with associated group
              int remainToAdd = aa[selectedRow].groupRef.getSize();
              for (SequenceI sgs : aa[selectedRow].groupRef.getSequences())
              {
                if (jalview.util.Platform.isControlDown(evt))
                {
                  sg.addOrRemove(sgs, --remainToAdd == 0);
                }
                else
                {
                  // notionally, we should also add intermediate sequences from
                  // last added sequence ?
                  sg.addSequence(sgs, --remainToAdd == 0);
                }
              }
            }

            ap.paintAlignment(false, false);
            PaintRefresher.Refresh(ap, ap.av.getSequenceSetId());
            ap.av.sendSelection();
          }
          else
          {
            ap.getSeqPanel().ap.getIdPanel().highlightSearchResults(
                    aa[selectedRow].groupRef.getSequences(null));
          }
          return;
        }
        else if (aa[selectedRow].sequenceRef != null)
        {
          if (evt.getClickCount() == 1)
          {
            ap.getSeqPanel().ap.getIdPanel()
                    .highlightSearchResults(Arrays.asList(new SequenceI[]
                    { aa[selectedRow].sequenceRef }));
          }
          else if (evt.getClickCount() >= 2)
          {
            ap.getSeqPanel().ap.getIdPanel().highlightSearchResults(null);
            SequenceGroup sg = ap.av.getSelectionGroup();
            if (sg != null)
            {
              // we make a copy rather than edit the current selection if no
              // modifiers pressed
              // see Enhancement JAL-1557
              if (!(Platform.isControlDown(evt) || evt.isShiftDown()))
              {
                sg = new SequenceGroup(sg);
                sg.clear();
                sg.addSequence(aa[selectedRow].sequenceRef, false);
              }
              else
              {
                if (Platform.isControlDown(evt))
                {
                  sg.addOrRemove(aa[selectedRow].sequenceRef, true);
                }
                else
                {
                  // notionally, we should also add intermediate sequences from
                  // last added sequence ?
                  sg.addSequence(aa[selectedRow].sequenceRef, true);
                }
              }
            }
            else
            {
              sg = new SequenceGroup();
              sg.setStartRes(0);
              sg.setEndRes(ap.av.getAlignment().getWidth() - 1);
              sg.addSequence(aa[selectedRow].sequenceRef, false);
            }
            ap.av.setSelectionGroup(sg);
            ap.paintAlignment(false, false);
            PaintRefresher.Refresh(ap, ap.av.getSequenceSetId());
            ap.av.sendSelection();
          }

        }
      }
      return;
    }
  }

  /**
   * do a single sequence copy to jalview and the system clipboard
   * 
   * @param sq
   *          sequence to be copied to clipboard
   */
  protected void copy_annotseqtoclipboard(SequenceI sq)
  {
    SequenceI[] seqs = new SequenceI[] { sq };
    String[] omitHidden = null;
    SequenceI[] dseqs = new SequenceI[] { sq.getDatasetSequence() };
    if (dseqs[0] == null)
    {
      dseqs[0] = new Sequence(sq);
      dseqs[0].setSequence(AlignSeq.extractGaps(Comparison.GapChars,
              sq.getSequenceAsString()));

      sq.setDatasetSequence(dseqs[0]);
    }
    Alignment ds = new Alignment(dseqs);
    if (av.hasHiddenColumns())
    {
      Iterator<int[]> it = av.getAlignment().getHiddenColumns()
              .getVisContigsIterator(0, sq.getLength(), false);
      omitHidden = new String[] { sq.getSequenceStringFromIterator(it) };
    }

    int[] alignmentStartEnd = new int[] { 0, ds.getWidth() - 1 };
    if (av.hasHiddenColumns())
    {
      alignmentStartEnd = av.getAlignment().getHiddenColumns()
              .getVisibleStartAndEndIndex(av.getAlignment().getWidth());
    }

    String output = new FormatAdapter().formatSequences(FileFormat.Fasta,
            seqs, omitHidden, alignmentStartEnd);

    Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(output), Desktop.instance);

    HiddenColumns hiddenColumns = null;

    if (av.hasHiddenColumns())
    {
      hiddenColumns = new HiddenColumns(
              av.getAlignment().getHiddenColumns());
    }

    Desktop.jalviewClipboard = new Object[] { seqs, ds, // what is the dataset
                                                        // of a consensus
                                                        // sequence ? need to
                                                        // flag
        // sequence as special.
        hiddenColumns };
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g1
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {

    int width = getWidth();
    if (width == 0)
    {
      width = ap.calculateIdWidth().width;
    }

    Graphics2D g2 = (Graphics2D) g;
    if (av.antiAlias)
    {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    }

    drawComponent(g2, true, width);

  }

  /**
   * Draw the full set of annotation Labels for the alignment at the given
   * cursor
   * 
   * @param g
   *          Graphics2D instance (needed for font scaling)
   * @param width
   *          Width for scaling labels
   * 
   */
  public void drawComponent(Graphics g, int width)
  {
    drawComponent(g, false, width);
  }

  /**
   * Draw the full set of annotation Labels for the alignment at the given
   * cursor
   * 
   * @param g
   *          Graphics2D instance (needed for font scaling)
   * @param clip
   *          - true indicates that only current visible area needs to be
   *          rendered
   * @param width
   *          Width for scaling labels
   */
  public void drawComponent(Graphics g, boolean clip, int width)
  {
    if (av.getFont().getSize() < 10)
    {
      g.setFont(font);
    }
    else
    {
      g.setFont(av.getFont());
    }

    FontMetrics fm = g.getFontMetrics(g.getFont());
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    g.translate(0, getScrollOffset());
    g.setColor(Color.black);
    SequenceI lastSeqRef = null;
    String lastLabel = null;
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    int fontHeight = g.getFont().getSize();
    int y = 0;
    int x = 0;
    int graphExtras = 0;
    int offset = 0;
    Font baseFont = g.getFont();
    FontMetrics baseMetrics = fm;
    int ofontH = fontHeight;
    int sOffset = 0;
    int visHeight = 0;
    int[] visr = (ap != null && ap.getAnnotationPanel() != null)
            ? ap.getAnnotationPanel().getVisibleVRange()
            : null;
    if (clip && visr != null)
    {
      sOffset = visr[0];
      visHeight = visr[1];
    }
    boolean visible = true, before = false, after = false;
    if (aa != null)
    {
      hasHiddenRows = false;
      int olY = 0;
      int nexAA = 0;
      for (int i = 0; i < aa.length; i++)
      {
        visible = true;
        if (!aa[i].visible)
        {
          hasHiddenRows = true;
          continue;
        }
        olY = y;
        // look ahead to next annotation
        for (nexAA = i + 1; nexAA < aa.length
                && !aa[nexAA].visible; nexAA++)
          ;
        y += aa[i].height;
        if (clip)
        {
          if (y < sOffset)
          {
            if (!before)
            {
              if (debugRedraw)
              {
                System.out.println("before vis: " + i);
              }
              before = true;
            }
            // don't draw what isn't visible
            continue;
          }
          if (olY > visHeight)
          {

            if (!after)
            {
              if (debugRedraw)
              {
                System.out.println(
                        "Scroll offset: " + sOffset + " after vis: " + i);
              }
              after = true;
            }
            // don't draw what isn't visible
            continue;
          }
        }
        g.setColor(Color.black);

        offset = -aa[i].height / 2;

        if (aa[i].hasText)
        {
          offset += fm.getHeight() / 2;
          offset -= fm.getDescent();
        }
        else
        {
          offset += fm.getDescent();
        }
        String label = aa[i].label;
        boolean vertBar = false;
        if ((lastLabel != null && lastLabel.equals(label)))
        {
          label = aa[i].description;
        }
        else
        {
          if (nexAA < aa.length && label.equals(aa[nexAA].label)) // &&
                                                                  // aa[nexY].sequenceRef==aa[i].sequenceRef)
          {
            lastLabel = label;
            // next label is the same as this label
            label = aa[i].description;
          }
          else
          {
            lastLabel = label;
          }
        }
        if (aa[i].sequenceRef != null)
        {
          if (aa[i].sequenceRef != lastSeqRef)
          {
            label = aa[i].sequenceRef.getName() + " " + label;
            // TODO record relationship between sequence and this annotation and
            // display it here
          }
          else
          {
            vertBar = true;
          }
        }
        x = width - fm.stringWidth(label) - 3;

        if (aa[i].graphGroup > -1)
        {
          int groupSize = 0;
          // TODO: JAL-1291 revise rendering model so the graphGroup map is
          // computed efficiently for all visible labels
          for (int gg = 0; gg < aa.length; gg++)
          {
            if (aa[gg].graphGroup == aa[i].graphGroup)
            {
              groupSize++;
            }
          }
          if (groupSize * (fontHeight + 8) < aa[i].height)
          {
            graphExtras = (aa[i].height - (groupSize * (fontHeight + 8)))
                    / 2;
          }
          else
          {
            // scale font to fit
            float h = aa[i].height / (float) groupSize, s;
            if (h < 9)
            {
              visible = false;
            }
            else
            {
              fontHeight = -8 + (int) h;
              s = ((float) fontHeight) / (float) ofontH;
              Font f = baseFont
                      .deriveFont(AffineTransform.getScaleInstance(s, s));
              g.setFont(f);
              fm = g.getFontMetrics();
              graphExtras = (aa[i].height - (groupSize * (fontHeight + 8)))
                      / 2;
            }
          }
          if (visible)
          {
            for (int gg = 0; gg < aa.length; gg++)
            {
              if (aa[gg].graphGroup == aa[i].graphGroup)
              {
                x = width - fm.stringWidth(aa[gg].label) - 3;
                g.drawString(aa[gg].label, x, y - graphExtras);

                if (aa[gg]._linecolour != null)
                {

                  g.setColor(aa[gg]._linecolour);
                  g.drawLine(x, y - graphExtras + 3,
                          x + fm.stringWidth(aa[gg].label),
                          y - graphExtras + 3);
                }

                g.setColor(Color.black);
                graphExtras += fontHeight + 8;
              }
            }
          }
          g.setFont(baseFont);
          fm = baseMetrics;
          fontHeight = ofontH;
        }
        else
        {
          if (vertBar)
          {
            g.drawLine(width - 3, y + offset - fontHeight, width - 3,
                    (int) (y - 1.5 * aa[i].height - offset - fontHeight));
            // g.drawLine(20, y + offset, x - 20, y + offset);

          }
          g.drawString(label, x, y + offset);
        }
        lastSeqRef = aa[i].sequenceRef;
      }
    }

    if (!resizePanel && dragEvent != null && aa != null)
    {
      g.setColor(Color.lightGray);
      g.drawString(
              (aa[selectedRow].sequenceRef == null ? ""
                      : aa[selectedRow].sequenceRef.getName())
                      + aa[selectedRow].label,
              dragEvent.getX(), dragEvent.getY() - getScrollOffset());
    }

    if (!av.getWrapAlignment() && ((aa == null) || (aa.length < 1)))
    {
      g.drawString(MessageManager.getString("label.right_click"), 2, 8);
      g.drawString(MessageManager.getString("label.to_add_annotation"), 2,
              18);
    }
  }

  public int getScrollOffset()
  {
    return scrollOffset;
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }
}
