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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class IdwidthAdjuster extends Panel
        implements MouseListener, MouseMotionListener
{
  boolean active = false;

  int oldX = 0;

  AlignmentPanel ap;

  public IdwidthAdjuster(AlignmentPanel ap)
  {
    setLayout(null);
    this.ap = ap;
    setBackground(Color.WHITE);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    oldX = evt.getX();
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    active = false;
    repaint();

    /*
     * If in a SplitFrame with co-scaled alignments, set the other's id width to
     * match; note applet does not (yet) store this in ViewStyle
     */
    /*
     * Code disabled for now as it doesn't work, don't know why; idCanvas width
     * keeps resetting to a previous value (actually two alternating values!)
     */
    // final AlignViewportI viewport = ap.getAlignViewport();
    // if (viewport.getCodingComplement() != null
    // && viewport.isScaleProteinAsCdna())
    // {
    // Dimension d = ap.idPanel.idCanvas.getSize();
    // SplitFrame sf = ap.alignFrame.getSplitFrame();
    // final AlignmentPanel otherPanel =
    // sf.getComplement(ap.alignFrame).alignPanel;
    // otherPanel.setIdWidth(d.width, d.height);
    // otherPanel.repaint();
    // }
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
    active = true;
    setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));

    repaint();
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
    active = false;
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    repaint();
  }

  @Override
  public void mouseDragged(MouseEvent evt)
  {
    active = true;
    Dimension d = ap.idPanel.idCanvas.getSize();
    int dif = evt.getX() - oldX;

    final int newWidth = d.width + dif;
    if (newWidth > 20 || dif > 0)
    {
      ap.setIdWidth(newWidth, d.height);
      this.setSize(newWidth, getSize().height);
      oldX = evt.getX();
    }
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }
}
