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

import jalview.analysis.AlignmentUtils;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;
import jalview.util.ParseHtmlBodyAndLinks;

import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Collections;

public class AnnotationLabels extends Panel
        implements ActionListener, MouseListener, MouseMotionListener
{
  Image image;

  /**
   * width in pixels within which height adjuster arrows are shown and active
   */
  private static final int HEIGHT_ADJUSTER_WIDTH = 50;

  /**
   * height in pixels for allowing height adjuster to be active
   */
  private static int HEIGHT_ADJUSTER_HEIGHT = 10;

  boolean active = false;

  AlignmentPanel ap;

  AlignViewport av;

  boolean resizing = false;

  int oldY, mouseX;

  static String ADDNEW = "Add New Row";

  static String EDITNAME = "Edit Label/Description";

  static String HIDE = "Hide This Row";

  static String SHOWALL = "Show All Hidden Rows";

  static String OUTPUT_TEXT = "Show Values In Textbox";

  static String COPYCONS_SEQ = "Copy Consensus Sequence";

  int scrollOffset = 0;

  int selectedRow = -1;

  Tooltip tooltip;

  private boolean hasHiddenRows;

  public AnnotationLabels(AlignmentPanel ap)
  {
    this.ap = ap;
    this.av = ap.av;
    setLayout(null);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public AnnotationLabels(AlignViewport av)
  {
    this.av = av;
  }

  public void setScrollOffset(int y, boolean repaint)
  {
    scrollOffset = y;
    if (repaint)
    {
      repaint();
    }
  }

  /**
   * 
   * @param y
   * @return -2 if no rows are visible at all, -1 if no visible rows were
   *         selected
   */
  int getSelectedRow(int y)
  {
    int row = -2;
    AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();

    if (aa == null)
    {
      return row;
    }
    int height = 0;
    for (int i = 0; i < aa.length; i++)
    {
      row = -1;
      if (!aa[i].visible)
      {
        continue;
      }
      height += aa[i].height;
      if (y < height)
      {
        row = i;
        break;
      }
    }

    return row;
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();

    if (evt.getActionCommand().equals(ADDNEW))
    {
      AlignmentAnnotation newAnnotation = new AlignmentAnnotation("", null,
              new Annotation[ap.av.getAlignment().getWidth()]);

      if (!editLabelDescription(newAnnotation))
      {
        return;
      }

      ap.av.getAlignment().addAnnotation(newAnnotation);
      ap.av.getAlignment().setAnnotationIndex(newAnnotation, 0);
    }
    else if (evt.getActionCommand().equals(EDITNAME))
    {
      editLabelDescription(aa[selectedRow]);
    }
    else if (evt.getActionCommand().equals(HIDE))
    {
      aa[selectedRow].visible = false;
    }
    else if (evt.getActionCommand().equals(SHOWALL))
    {
      for (int i = 0; i < aa.length; i++)
      {
        aa[i].visible = (aa[i].annotations == null) ? false : true;
      }
    }
    else if (evt.getActionCommand().equals(OUTPUT_TEXT))
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(false,
              ap.alignFrame);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame,
              ap.alignFrame.getTitle() + " - " + aa[selectedRow].label, 500,
              100);
      cap.setText(aa[selectedRow].toString());
    }
    else if (evt.getActionCommand().equals(COPYCONS_SEQ))
    {
      SequenceGroup group = aa[selectedRow].groupRef;
      SequenceI cons = group == null ? av.getConsensusSeq()
              : group.getConsensusSeq();
      if (cons != null)
      {
        copy_annotseqtoclipboard(cons);
      }

    }
    refresh();
  }

  /**
   * Adjust size and repaint
   */
  protected void refresh()
  {
    ap.annotationPanel.adjustPanelHeight();
    setSize(getSize().width, ap.annotationPanel.getSize().height);
    ap.validate();
    // TODO: only paint if we needed to
    ap.paintAlignment(true, true);
  }

  boolean editLabelDescription(AlignmentAnnotation annotation)
  {
    Checkbox padGaps = new Checkbox(
            "Fill Empty Gaps With \"" + ap.av.getGapCharacter() + "\"",
            annotation.padGaps);

    EditNameDialog dialog = new EditNameDialog(annotation.label,
            annotation.description, "      Annotation Label",
            "Annotation Description", ap.alignFrame,
            "Edit Annotation Name / Description", 500, 180, false);

    Panel empty = new Panel(new FlowLayout());
    empty.add(padGaps);
    dialog.add(empty);
    dialog.pack();

    dialog.setVisible(true);

    if (dialog.accept)
    {
      annotation.label = dialog.getName();
      annotation.description = dialog.getDescription();
      annotation.setPadGaps(padGaps.getState(), av.getGapCharacter());
      repaint();
      return true;
    }
    else
    {
      return false;
    }

  }

  boolean resizePanel = false;

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    resizePanel = evt.getY() < HEIGHT_ADJUSTER_HEIGHT
            && evt.getX() < HEIGHT_ADJUSTER_WIDTH;
    setCursor(Cursor.getPredefinedCursor(
            resizePanel ? Cursor.S_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
    int row = getSelectedRow(evt.getY() + scrollOffset);

    if (row > -1)
    {
      ParseHtmlBodyAndLinks phb = new ParseHtmlBodyAndLinks(
              av.getAlignment().getAlignmentAnnotation()[row]
                      .getDescription(true),
              true, "\n");
      if (tooltip == null)
      {
        tooltip = new Tooltip(phb.getNonHtmlContent(), this);
      }
      else
      {
        tooltip.setTip(phb.getNonHtmlContent());
      }
    }
    else if (tooltip != null)
    {
      tooltip.setTip("");
    }
  }

  /**
   * curent drag position
   */
  MouseEvent dragEvent = null;

  /**
   * flag to indicate drag events should be ignored
   */
  private boolean dragCancelled = false;

  /**
   * clear any drag events in progress
   */
  public void cancelDrag()
  {
    dragEvent = null;
    dragCancelled = true;
  }

  @Override
  public void mouseDragged(MouseEvent evt)
  {
    if (dragCancelled)
    {
      return;
    }
    ;
    dragEvent = evt;

    if (resizePanel)
    {
      Dimension d = ap.annotationPanelHolder.getSize(),
              e = ap.annotationSpaceFillerHolder.getSize(),
              f = ap.seqPanelHolder.getSize();
      int dif = evt.getY() - oldY;

      dif /= ap.av.getCharHeight();
      dif *= ap.av.getCharHeight();

      if ((d.height - dif) > 20 && (f.height + dif) > 20)
      {
        ap.annotationPanel.setSize(d.width, d.height - dif);
        setSize(new Dimension(e.width, d.height - dif));
        ap.annotationSpaceFillerHolder
                .setSize(new Dimension(e.width, d.height - dif));
        ap.annotationPanelHolder
                .setSize(new Dimension(d.width, d.height - dif));
        ap.apvscroll.setValues(ap.apvscroll.getValue(), d.height - dif, 0,
                av.calcPanelHeight());
        f.height += dif;
        ap.seqPanelHolder.setPreferredSize(f);
        ap.setScrollValues(av.getRanges().getStartRes(),
                av.getRanges().getStartSeq());
        ap.validate();
        // ap.paintAlignment(true);
        ap.addNotify();
      }

    }
    else
    {
      int diff;
      if ((diff = 6 - evt.getY()) > 0)
      {
        // nudge scroll up
        ap.apvscroll.setValue(ap.apvscroll.getValue() - diff);
        ap.adjustmentValueChanged(null);

      }
      else if ((0 < (diff = 6
              - ap.annotationSpaceFillerHolder.getSize().height
              + evt.getY())))
      {
        // nudge scroll down
        ap.apvscroll.setValue(ap.apvscroll.getValue() + diff);
        ap.adjustmentValueChanged(null);
      }
      repaint();
    }
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    if (!resizePanel && !dragCancelled)
    {
      int start = selectedRow;

      int end = getSelectedRow(evt.getY() + scrollOffset);

      if (start > -1 && start != end)
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
    }
    resizePanel = false;
    dragEvent = null;
    dragCancelled = false;
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    repaint();
    ap.annotationPanel.repaint();
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
    if (evt.getY() < 10 && evt.getX() < 14)
    {
      resizePanel = true;
      repaint();
    }
    setCursor(Cursor.getPredefinedCursor(
            resizePanel ? Cursor.S_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
    dragCancelled = false;

    if (dragEvent == null)
    {
      resizePanel = false;
    }
    else
    {
      if (!resizePanel)
      {
        dragEvent = null;
      }
    }
    repaint();
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    oldY = evt.getY();
    if (resizePanel)
    {
      return;
    }
    dragCancelled = false;
    // todo: move below to mouseClicked ?
    selectedRow = getSelectedRow(evt.getY() + scrollOffset);

    AlignmentAnnotation[] aa = ap.av.getAlignment()
            .getAlignmentAnnotation();

    // DETECT RIGHT MOUSE BUTTON IN AWT
    if ((evt.getModifiersEx()
            & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
    {

      PopupMenu popup = new PopupMenu(
              MessageManager.getString("label.annotations"));

      MenuItem item = new MenuItem(ADDNEW);
      item.addActionListener(this);
      popup.add(item);
      if (selectedRow < 0)
      {
        // this never happens at moment: - see comment on JAL-563
        if (hasHiddenRows)
        {
          item = new MenuItem(SHOWALL);
          item.addActionListener(this);
          popup.add(item);
        }
        this.add(popup);
        popup.show(this, evt.getX(), evt.getY());
        return;
      }
      // add the rest if there are actually rows to show
      item = new MenuItem(EDITNAME);
      item.addActionListener(this);
      popup.add(item);
      item = new MenuItem(HIDE);
      item.addActionListener(this);
      popup.add(item);

      /*
       * Hide all <label>:
       */
      if (selectedRow < aa.length)
      {
        if (aa[selectedRow].sequenceRef != null)
        {
          final String label = aa[selectedRow].label;
          MenuItem hideType = new MenuItem(
                  MessageManager.getString("label.hide_all") + " " + label);
          hideType.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              AlignmentUtils.showOrHideSequenceAnnotations(
                      ap.av.getAlignment(), Collections.singleton(label),
                      null, false, false);
              refresh();
            }
          });
          popup.add(hideType);
        }
      }

      if (hasHiddenRows)
      {
        item = new MenuItem(SHOWALL);
        item.addActionListener(this);
        popup.add(item);
      }
      this.add(popup);
      item = new MenuItem(OUTPUT_TEXT);
      item.addActionListener(this);
      popup.add(item);
      if (selectedRow < aa.length)
      {
        if (aa[selectedRow].autoCalculated)
        {
          if (aa[selectedRow].label.indexOf("Consensus") > -1)
          {
            popup.addSeparator();
            final CheckboxMenuItem cbmi = new CheckboxMenuItem(
                    MessageManager.getString("label.ignore_gaps_consensus"),
                    (aa[selectedRow].groupRef != null)
                            ? aa[selectedRow].groupRef
                                    .getIgnoreGapsConsensus()
                            : ap.av.isIgnoreGapsConsensus());
            final AlignmentAnnotation aaa = aa[selectedRow];
            cbmi.addItemListener(new ItemListener()
            {
              @Override
              public void itemStateChanged(ItemEvent e)
              {
                if (aaa.groupRef != null)
                {
                  // TODO: pass on reference to ap so the view can be updated.
                  aaa.groupRef.setIgnoreGapsConsensus(cbmi.getState());
                }
                else
                {
                  ap.av.setIgnoreGapsConsensus(cbmi.getState(), ap);
                }
                ap.paintAlignment(true, true);
              }
            });
            popup.add(cbmi);
            if (aaa.groupRef != null)
            {
              final CheckboxMenuItem chist = new CheckboxMenuItem(
                      MessageManager
                              .getString("label.show_group_histogram"),
                      aa[selectedRow].groupRef.isShowConsensusHistogram());
              chist.addItemListener(new ItemListener()
              {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  aaa.groupRef.setShowConsensusHistogram(chist.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(chist);
              final CheckboxMenuItem cprofl = new CheckboxMenuItem(
                      MessageManager.getString("label.show_group_logo"),
                      aa[selectedRow].groupRef.isShowSequenceLogo());
              cprofl.addItemListener(new ItemListener()
              {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  aaa.groupRef.setshowSequenceLogo(cprofl.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });

              popup.add(cprofl);
              final CheckboxMenuItem cprofn = new CheckboxMenuItem(
                      MessageManager
                              .getString("label.normalise_group_logo"),
                      aa[selectedRow].groupRef.isNormaliseSequenceLogo());
              cprofn.addItemListener(new ItemListener()
              {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  aaa.groupRef.setshowSequenceLogo(true);
                  aaa.groupRef.setNormaliseSequenceLogo(cprofn.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(cprofn);
            }
            else
            {
              final CheckboxMenuItem chist = new CheckboxMenuItem(
                      MessageManager.getString("label.show_histogram"),
                      av.isShowConsensusHistogram());
              chist.addItemListener(new ItemListener()
              {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  av.setShowConsensusHistogram(chist.getState());
                  ap.alignFrame.showConsensusHistogram
                          .setState(chist.getState()); // TODO: implement
                                                       // ap.updateGUI()/alignFrame.updateGUI
                                                       // for applet
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(chist);
              final CheckboxMenuItem cprof = new CheckboxMenuItem(
                      MessageManager.getString("label.show_logo"),
                      av.isShowSequenceLogo());
              cprof.addItemListener(new ItemListener()
              {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  av.setShowSequenceLogo(cprof.getState());
                  ap.alignFrame.showSequenceLogo.setState(cprof.getState()); // TODO:
                                                                             // implement
                                                                             // ap.updateGUI()/alignFrame.updateGUI
                                                                             // for
                                                                             // applet
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(cprof);
              final CheckboxMenuItem cprofn = new CheckboxMenuItem(
                      MessageManager.getString("label.normalise_logo"),
                      av.isNormaliseSequenceLogo());
              cprofn.addItemListener(new ItemListener()
              {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                  // TODO: pass on reference
                  // to ap
                  // so the
                  // view
                  // can be
                  // updated.
                  av.setShowSequenceLogo(true);
                  ap.alignFrame.normSequenceLogo
                          .setState(cprofn.getState()); // TODO:
                                                        // implement
                                                        // ap.updateGUI()/alignFrame.updateGUI
                                                        // for
                                                        // applet
                  av.setNormaliseSequenceLogo(cprofn.getState());
                  ap.repaint();
                  // ap.annotationPanel.paint(ap.annotationPanel.getGraphics());
                }
              });
              popup.add(cprofn);
            }

            item = new MenuItem(COPYCONS_SEQ);
            item.addActionListener(this);
            popup.add(item);
          }
        }
      }
      popup.show(this, evt.getX(), evt.getY());
    }
    else
    {
      // selection action.
      if (selectedRow > -1 && selectedRow < aa.length)
      {
        if (aa[selectedRow].groupRef != null)
        {
          if (evt.getClickCount() >= 2)
          {
            // todo: make the ap scroll to the selection - not necessary, first
            // click highlights/scrolls, second selects
            ap.seqPanel.ap.idPanel.highlightSearchResults(null);
            // process modifiers
            SequenceGroup sg = ap.av.getSelectionGroup();
            if (sg == null || sg == aa[selectedRow].groupRef
                    || !(jalview.util.Platform.isControlDown(evt)
                            || evt.isShiftDown()))
            {
              if (jalview.util.Platform.isControlDown(evt)
                      || evt.isShiftDown())
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
            ap.seqPanel.ap.idPanel.highlightSearchResults(
                    aa[selectedRow].groupRef.getSequences(null));
          }
          return;
        }
        else if (aa[selectedRow].sequenceRef != null)
        {
          if (evt.getClickCount() == 1)
          {
            ap.seqPanel.ap.idPanel
                    .highlightSearchResults(Arrays.asList(new SequenceI[]
                    { aa[selectedRow].sequenceRef }));
          }
          else if (evt.getClickCount() >= 2)
          {
            ap.seqPanel.ap.idPanel.highlightSearchResults(null);
            SequenceGroup sg = ap.av.getSelectionGroup();
            if (sg != null)
            {
              // we make a copy rather than edit the current selection if no
              // modifiers pressed
              // see Enhancement JAL-1557
              if (!(jalview.util.Platform.isControlDown(evt)
                      || evt.isShiftDown()))
              {
                sg = new SequenceGroup(sg);
                sg.clear();
                sg.addSequence(aa[selectedRow].sequenceRef, false);
              }
              else
              {
                if (jalview.util.Platform.isControlDown(evt))
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

    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void copy_annotseqtoclipboard(SequenceI sq)
  {
    if (sq == null || sq.getLength() < 1)
    {
      return;
    }
    jalview.appletgui.AlignFrame.copiedSequences = new StringBuffer();
    jalview.appletgui.AlignFrame.copiedSequences
            .append(sq.getName() + "\t" + sq.getStart() + "\t" + sq.getEnd()
                    + "\t" + sq.getSequenceAsString() + "\n");
    if (av.hasHiddenColumns())
    {
      jalview.appletgui.AlignFrame.copiedHiddenColumns = new HiddenColumns(
              av.getAlignment().getHiddenColumns());
    }
  }

  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {
    int w = getSize().width;
    int h = getSize().height;
    if (image == null || w != image.getWidth(this)
            || h != image.getHeight(this))
    {
      image = createImage(w, ap.annotationPanel.getSize().height);
    }

    drawComponent(image.getGraphics(), w);
    g.drawImage(image, 0, 0, this);
  }

  public void drawComponent(Graphics g, int width)
  {
    g.setFont(av.getFont());
    FontMetrics fm = g.getFontMetrics(av.getFont());
    g.setColor(Color.white);
    g.fillRect(0, 0, getSize().width, getSize().height);

    g.translate(0, -scrollOffset);
    g.setColor(Color.black);

    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    int y = 0, fy = g.getFont().getSize();
    int x = 0, offset;

    if (aa != null)
    {
      hasHiddenRows = false;
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible)
        {
          hasHiddenRows = true;
          continue;
        }

        x = width - fm.stringWidth(aa[i].label) - 3;

        y += aa[i].height;
        offset = -(aa[i].height - fy) / 2;

        g.drawString(aa[i].label, x, y + offset);
      }
    }
    g.translate(0, +scrollOffset);

    if (!resizePanel && !dragCancelled && dragEvent != null && aa != null)
    {
      g.setColor(Color.lightGray);
      g.drawString(aa[selectedRow].label, dragEvent.getX(),
              dragEvent.getY());
    }

    if (!av.getWrapAlignment() && ((aa == null) || (aa.length < 1)))
    {
      g.setColor(Color.black);
      g.drawString(MessageManager.getString("label.right_click"), 2, 8);
      g.drawString(MessageManager.getString("label.to_add_annotation"), 2,
              18);
    }
  }
}
