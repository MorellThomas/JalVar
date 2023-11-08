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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceGroup;
import jalview.renderer.ScaleRenderer;
import jalview.renderer.ScaleRenderer.ScaleMark;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.ViewportListenerI;
import jalview.viewmodel.ViewportRanges;

/**
 * The panel containing the sequence ruler (when not in wrapped mode), and
 * supports a range of mouse operations to select, hide or reveal columns.
 */
public class ScalePanel extends JPanel
        implements MouseMotionListener, MouseListener, ViewportListenerI
{
  protected int offy = 4;

  public int width;

  protected AlignViewport av;

  AlignmentPanel ap;

  boolean stretchingGroup = false;

  /*
   * min, max hold the extent of a mouse drag action
   */
  int min;

  int max;

  boolean mouseDragging = false;

  /*
   * holds a hidden column range when the mouse is over an adjacent column
   */
  int[] reveal;

  /**
   * Constructor
   * 
   * @param av
   * @param ap
   */
  public ScalePanel(AlignViewport av, AlignmentPanel ap)
  {
    this.av = av;
    this.ap = ap;

    addMouseListener(this);
    addMouseMotionListener(this);

    av.getRanges().addPropertyChangeListener(this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mousePressed(MouseEvent evt)
  {
    int x = (evt.getX() / av.getCharWidth()) + av.getRanges().getStartRes();
    final int res;

    if (av.hasHiddenColumns())
    {
      x = av.getAlignment().getHiddenColumns().visibleToAbsoluteColumn(x);
    }
    res = Math.min(x, av.getAlignment().getWidth() - 1);

    min = res;
    max = res;

    if (evt.isPopupTrigger()) // Mac: mousePressed
    {
      rightMouseButtonPressed(evt, res);
      return;
    }
    if (Platform.isWinRightButton(evt))
    {
      /*
       * defer right-mouse click handling to mouse up on Windows
       * (where isPopupTrigger() will answer true)
       * but accept Cmd-click on Mac which passes isRightMouseButton
       */
      return;
    }
    leftMouseButtonPressed(evt, res);
  }

  /**
   * Handles right mouse button press. If pressed in a selected column, opens
   * context menu for 'Hide Columns'. If pressed on a hidden columns marker,
   * opens context menu for 'Reveal / Reveal All'. Else does nothing.
   * 
   * @param evt
   * @param res
   */
  protected void rightMouseButtonPressed(MouseEvent evt, final int res)
  {
    JPopupMenu pop = buildPopupMenu(res);
    if (pop.getSubElements().length > 0)
    {
      pop.show(this, evt.getX(), evt.getY());
    }
  }

  /**
   * Builds a popup menu with 'Hide' or 'Reveal' options, or both, or neither
   * 
   * @param res
   *          column number (0..)
   * @return
   */
  protected JPopupMenu buildPopupMenu(final int res)
  {
    JPopupMenu pop = new JPopupMenu();

    /*
     * logic here depends on 'reveal', set in mouseMoved;
     * grab the hidden range in case mouseMoved nulls it later
     */
    final int[] hiddenRange = reveal;
    if (hiddenRange != null)
    {
      JMenuItem item = new JMenuItem(
              MessageManager.getString("label.reveal"));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          av.showColumn(hiddenRange[0]);
          reveal = null;
          ap.updateLayout();
          ap.paintAlignment(true, true);
          av.sendSelection();
        }
      });
      pop.add(item);

      if (av.getAlignment().getHiddenColumns()
              .hasMultiHiddenColumnRegions())
      {
        item = new JMenuItem(MessageManager.getString("action.reveal_all"));
        item.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            av.showAllHiddenColumns();
            reveal = null;
            ap.updateLayout();
            ap.paintAlignment(true, true);
            av.sendSelection();
          }
        });
        pop.add(item);
      }
    }

    if (av.getColumnSelection().contains(res))
    {
      JMenuItem item = new JMenuItem(
              MessageManager.getString("label.hide_columns"));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          av.hideColumns(res, res);
          if (av.getSelectionGroup() != null && av.getSelectionGroup()
                  .getSize() == av.getAlignment().getHeight())
          {
            av.setSelectionGroup(null);
          }

          ap.updateLayout();
          ap.paintAlignment(true, true);
          av.sendSelection();
        }
      });
      pop.add(item);
    }
    return pop;
  }

  /**
   * Handles left mouse button press
   * 
   * @param evt
   * @param res
   */
  protected void leftMouseButtonPressed(MouseEvent evt, final int res)
  {
    /*
     * Ctrl-click/Cmd-click adds to the selection
     * Shift-click extends the selection
     */
    // TODO Problem: right-click on Windows not reported until mouseReleased?!?
    if (!Platform.isControlDown(evt) && !evt.isShiftDown())
    {
      av.getColumnSelection().clear();
    }

    av.getColumnSelection().addElement(res);
    SequenceGroup sg = new SequenceGroup(av.getAlignment().getSequences());
    sg.setStartRes(res);
    sg.setEndRes(res);

    if (evt.isShiftDown())
    {
      int min = Math.min(av.getColumnSelection().getMin(), res);
      int max = Math.max(av.getColumnSelection().getMax(), res);
      for (int i = min; i < max; i++)
      {
        av.getColumnSelection().addElement(i);
      }
      sg.setStartRes(min);
      sg.setEndRes(max);
    }
    av.setSelectionGroup(sg);
    ap.paintAlignment(false, false);
    PaintRefresher.Refresh(this,av.getSequenceSetId());
    av.sendSelection();
  }

  /**
   * Action on mouseUp is to set the limit of the current selection group (if
   * there is one) and broadcast the selection
   * 
   * @param evt
   */
  @Override
  public void mouseReleased(MouseEvent evt)
  {
    boolean wasDragging = mouseDragging;
    mouseDragging = false;
    ap.getSeqPanel().stopScrolling();

    // todo res calculation should be a method on AlignViewport
    int xCords = Math.max(0, evt.getX()); // prevent negative X coordinates
    ViewportRanges ranges = av.getRanges();
    int res = (xCords / av.getCharWidth()) + ranges.getStartRes();
    res = Math.min(res, ranges.getEndRes());
    if (av.hasHiddenColumns())
    {
      res = av.getAlignment().getHiddenColumns()
              .visibleToAbsoluteColumn(res);
    }
    res = Math.max(0, res);

    if (!stretchingGroup)
    {
      if (evt.isPopupTrigger()) // Windows: mouseReleased
      {
        rightMouseButtonPressed(evt, res);
      }
      else
      {
        ap.paintAlignment(false, false);
      }
      return;
    }

    SequenceGroup sg = av.getSelectionGroup();

    if (sg != null)
    {
      if (res > sg.getStartRes())
      {
        sg.setEndRes(res);
      }
      else if (res < sg.getStartRes())
      {
        sg.setStartRes(res);
      }
      if (wasDragging)
      {
        min = Math.min(res, min);
        max = Math.max(res, max);
        av.getColumnSelection().stretchGroup(res, sg, min, max);
      }
    }
    stretchingGroup = false;
    ap.paintAlignment(false, false);
    av.isSelectionGroupChanged(true);
    av.isColSelChanged(true);
    PaintRefresher.Refresh(ap, av.getSequenceSetId());
    av.sendSelection();
  }

  /**
   * Action on dragging the mouse in the scale panel is to expand or shrink the
   * selection group range (including any hidden columns that it spans). Note
   * that the selection is only broadcast at the start of the drag (on
   * mousePressed) and at the end (on mouseReleased), to avoid overload
   * redrawing of other views.
   * 
   * @param evt
   */
  @Override
  public void mouseDragged(MouseEvent evt)
  {
    mouseDragging = true;
    ColumnSelection cs = av.getColumnSelection();
    HiddenColumns hidden = av.getAlignment().getHiddenColumns();

    int res = (evt.getX() / av.getCharWidth())
            + av.getRanges().getStartRes();
    res = Math.max(0, res);
    res = hidden.visibleToAbsoluteColumn(res);
    res = Math.min(res, av.getAlignment().getWidth() - 1);
    min = Math.min(res, min);
    max = Math.max(res, max);

    SequenceGroup sg = av.getSelectionGroup();
    if (sg != null)
    {
      stretchingGroup = true;
      cs.stretchGroup(res, sg, min, max);
      ap.paintAlignment(false, false);
      PaintRefresher.Refresh(ap, av.getSequenceSetId());
    }
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
    if (mouseDragging)
    {
      mouseDragging = false;
      ap.getSeqPanel().stopScrolling();
    }
  }

  /**
   * Action on leaving the panel bounds with mouse drag in progress is to start
   * scrolling the alignment in the direction of the mouse. To restrict
   * scrolling to left-right (not up-down), the y-value of the mouse position is
   * replaced with zero.
   */
  @Override
  public void mouseExited(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.getSeqPanel().startScrolling(new Point(evt.getX(), 0));
    }
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  /**
   * Creates a tooltip when the mouse is over a hidden columns marker
   */
  @Override
  public void mouseMoved(MouseEvent evt)
  {
    this.setToolTipText(null);
    reveal = null;
    if (!av.hasHiddenColumns())
    {
      int res = (evt.getX() / av.getCharWidth())
              + av.getRanges().getStartRes();
      highlightAllStructPos(res);
      return;
    }

    int res = (evt.getX() / av.getCharWidth())
            + av.getRanges().getStartRes();

    reveal = av.getAlignment().getHiddenColumns()
            .getRegionWithEdgeAtRes(res);

    res = av.getAlignment().getHiddenColumns().visibleToAbsoluteColumn(res);
    highlightAllStructPos(res);
    ToolTipManager.sharedInstance().registerComponent(this);
    this.setToolTipText(
            MessageManager.getString("label.reveal_hidden_columns"));
    repaint();
  }

  public void highlightAllStructPos(int col)
  {
    ap.getStructureSelectionManager().highlightPositionsOnMany(
            ap.av.getAlignment().getSequencesArray(), new int[]
            { col, col }, ap);

  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {
    // super.paintComponent(g); // BH 2019

    /*
     * shouldn't get called in wrapped mode as the scale above is
     * drawn instead by SeqCanvas.drawNorthScale
     */
    if (!av.getWrapAlignment())
    {
      drawScale(g, av.getRanges().getStartRes(), av.getRanges().getEndRes(),
              getWidth(), getHeight());
    }
  }

  // scalewidth will normally be screenwidth,
  public void drawScale(Graphics g, int startx, int endx, int width,
          int height)
  {
    Graphics2D gg = (Graphics2D) g;
    gg.setFont(av.getFont());

    if (av.antiAlias)
    {
      gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    }

    // Fill in the background
    gg.setColor(Color.white);
    gg.fillRect(0, 0, width, height);
    gg.setColor(Color.black);

    // Fill the selected columns
    ColumnSelection cs = av.getColumnSelection();
    HiddenColumns hidden = av.getAlignment().getHiddenColumns();
    int avCharWidth = av.getCharWidth();
    int avCharHeight = av.getCharHeight();

    if (cs != null)
    {
      gg.setColor(new Color(220, 0, 0));

      for (int sel : cs.getSelected())
      {
        // TODO: JAL-2001 - provide a fast method to list visible selected in a
        // given range

        if (av.hasHiddenColumns())
        {
          if (hidden.isVisible(sel))
          {
            sel = hidden.absoluteToVisibleColumn(sel);
          }
          else
          {
            continue;
          }
        }

        if ((sel >= startx) && (sel <= endx))
        {
          gg.fillRect((sel - startx) * avCharWidth, 0, avCharWidth,
                  getHeight());
        }
      }
    }

    int widthx = 1 + endx - startx;

    FontMetrics fm = gg.getFontMetrics(av.getFont());
    int y = avCharHeight;
    int yOf = fm.getDescent();
    y -= yOf;
    if (av.hasHiddenColumns())
    {
      // draw any hidden column markers
      gg.setColor(Color.blue);
      int res;

      if (av.getShowHiddenMarkers())
      {
        Iterator<Integer> it = hidden.getStartRegionIterator(startx,
                startx + widthx + 1);
        while (it.hasNext())
        {
          res = it.next() - startx;

          gg.fillPolygon(
                  new int[]
                  { -1 + res * avCharWidth - avCharHeight / 4,
                      -1 + res * avCharWidth + avCharHeight / 4,
                      -1 + res * avCharWidth },
                  new int[]
                  { y, y, y + 2 * yOf }, 3);
        }
      }
    }
    // Draw the scale numbers
    gg.setColor(Color.black);

    int maxX = 0;
    List<ScaleMark> marks = new ScaleRenderer().calculateMarks(av, startx,
            endx);

    for (ScaleMark mark : marks)
    {
      boolean major = mark.major;
      int mpos = mark.column; // (i - startx - 1)
      String mstring = mark.text;
      if (mstring != null)
      {
        if (mpos * avCharWidth > maxX)
        {
          gg.drawString(mstring, mpos * avCharWidth, y);
          maxX = (mpos + 2) * avCharWidth + fm.stringWidth(mstring);
        }
      }
      if (major)
      {
        gg.drawLine((mpos * avCharWidth) + (avCharWidth / 2), y + 2,
                (mpos * avCharWidth) + (avCharWidth / 2), y + (yOf * 2));
      }
      else
      {
        gg.drawLine((mpos * avCharWidth) + (avCharWidth / 2), y + yOf,
                (mpos * avCharWidth) + (avCharWidth / 2), y + (yOf * 2));
      }
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    // Respond to viewport change events (e.g. alignment panel was scrolled)
    // Both scrolling and resizing change viewport ranges: scrolling changes
    // both start and end points, but resize only changes end values.
    // Here we only want to fastpaint on a scroll, with resize using a normal
    // paint, so scroll events are identified as changes to the horizontal or
    // vertical start value.
    if (evt.getPropertyName().equals(ViewportRanges.STARTRES)
            || evt.getPropertyName().equals(ViewportRanges.STARTRESANDSEQ)
            || evt.getPropertyName().equals(ViewportRanges.MOVE_VIEWPORT))
    {
      // scroll event, repaint panel

      // Call repaint on alignment panel so that repaints from other alignment
      // panel components can be aggregated. Otherwise performance of the
      // overview
      // window and others may be adversely affected.
      av.getAlignPanel().repaint();
    }
  }

}
