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

import jalview.api.AlignViewportI;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdwidthAdjuster extends JPanel
        implements MouseListener, MouseMotionListener
{
  public static final int MIN_ID_WIDTH = 20;

  int oldX = 0;

  AlignmentPanel ap;

  /**
   * Creates a new IdwidthAdjuster object.
   * 
   * @param ap
   *          DOCUMENT ME!
   */
  public IdwidthAdjuster(AlignmentPanel ap)
  {
    this.ap = ap;
    setBackground(Color.white);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   * Action on mouse pressed is to save the start position for any drag
   * 
   * @param evt
   */
  @Override
  public void mousePressed(MouseEvent evt)
  {
    oldX = evt.getX();
  }

  /**
   * On release of mouse drag to resize the width, if there is a complementary
   * alignment in a split frame, sets the complement to the same id width and
   * repaints the split frame. Note this is done whether or not the protein
   * characters are scaled to codon width.
   * 
   * @param evt
   */
  @Override
  public void mouseReleased(MouseEvent evt)
  {
    repaint();

    /*
     * If in a SplitFrame, set the other's id width to match
     */
    final AlignViewportI viewport = ap.getAlignViewport();
    if (viewport.getCodingComplement() != null)
    {
      viewport.getCodingComplement().setIdWidth(viewport.getIdWidth());
      SplitFrame sf = (SplitFrame) ap.alignFrame.getSplitViewContainer();
      sf.repaint();
    }
  }

  /**
   * When this region is entered, repaints to show a left-right move cursor
   * 
   * @param evt
   */
  @Override
  public void mouseEntered(MouseEvent evt)
  {
    repaint();
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
  }

  /**
   * Adjusts the id panel width for a mouse drag left or right (subject to a
   * minimum of 20 pixels) and repaints the alignment
   * 
   * @param evt
   */
  @Override
  public void mouseDragged(MouseEvent evt)
  {
    int mouseX = evt.getX();
    final AlignViewportI viewport = ap.getAlignViewport();
    int curwidth = viewport.getIdWidth();
    int dif = mouseX - oldX;

    final int newWidth = curwidth + dif;

    /*
     * don't drag below minimum width
     */
    if (newWidth < MIN_ID_WIDTH)
    {
      return;
    }

    oldX = evt.getX();

    /*
     * don't drag right if mouse is to the left of the region
     */
    if (dif > 0 && mouseX < 0)
    {
      return;
    }
    viewport.setIdWidth(newWidth);
    ap.paintAlignment(true, false);
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  /**
   * Paints this region, showing a left-right move cursor if currently 'active'
   * 
   * @param g
   */
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());
    setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
  }
}
