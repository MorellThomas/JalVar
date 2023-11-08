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

import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.OverviewDimensions;
import jalview.viewmodel.OverviewDimensionsHideHidden;
import jalview.viewmodel.OverviewDimensionsShowHidden;
import jalview.viewmodel.ViewportListenerI;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.SwingUtilities;

public class OverviewPanel extends Panel implements Runnable,
        MouseMotionListener, MouseListener, ViewportListenerI
{
  private OverviewDimensions od;

  private OverviewCanvas oviewCanvas;

  private AlignViewport av;

  private AlignmentPanel ap;

  private boolean showHidden = true;

  private boolean updateRunning = false;

  private boolean draggingBox = false;

  public OverviewPanel(AlignmentPanel alPanel)
  {
    this.av = alPanel.av;
    this.ap = alPanel;
    setLayout(null);

    od = new OverviewDimensionsShowHidden(av.getRanges(),
            (av.isShowAnnotation()
                    && av.getSequenceConsensusHash() != null));

    oviewCanvas = new OverviewCanvas(od, av);
    setLayout(new BorderLayout());
    add(oviewCanvas, BorderLayout.CENTER);

    setSize(new Dimension(od.getWidth(), od.getHeight()));

    av.getRanges().addPropertyChangeListener(this);

    addComponentListener(new ComponentAdapter()
    {

      @Override
      public void componentResized(ComponentEvent evt)
      {
        if ((getWidth() != od.getWidth())
                || (getHeight() != (od.getHeight())))
        {
          updateOverviewImage();
        }
      }
    });

    addMouseMotionListener(this);

    addMouseListener(this);

    updateOverviewImage();

  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
    if ((evt.getModifiersEx()
            & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
    {
      showPopupMenu(evt);
    }
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    if (od.isPositionInBox(evt.getX(), evt.getY()))
    {
      this.getParent()
              .setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    else
    {
      this.getParent().setCursor(
              Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    if ((evt.getModifiersEx()
            & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
    {
      if (!Platform.isMac()) // BH was excluding JavaScript
      {
        showPopupMenu(evt);
      }
    }
    else
    {
      // don't do anything if the mouse press is in the overview's box
      // (wait to see if it's a drag instead)
      // otherwise update the viewport
      if (!od.isPositionInBox(evt.getX(), evt.getY()))
      {
        draggingBox = false;

        // display drag cursor at mouse position
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        od.updateViewportFromMouse(evt.getX(), evt.getY(),
                av.getAlignment().getHiddenSequences(),
                av.getAlignment().getHiddenColumns());
        getParent()
                .setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
      else
      {
        draggingBox = true;
        od.setDragPoint(evt.getX(), evt.getY(),
                av.getAlignment().getHiddenSequences(),
                av.getAlignment().getHiddenColumns());
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    draggingBox = false;
  }

  @Override
  public void mouseDragged(MouseEvent evt)
  {
    if (Platform.isWinRightButton(evt))
    {
      showPopupMenu(evt);
      return;
    }

    if (SwingUtilities.isRightMouseButton(evt))
    {
      return;
    }

    if (draggingBox)
    {
      // set the mouse position as a fixed point in the box
      // and drag relative to that position
      od.adjustViewportFromMouse(evt.getX(), evt.getY(),
              av.getAlignment().getHiddenSequences(),
              av.getAlignment().getHiddenColumns());
    }
    else
    {
      od.updateViewportFromMouse(evt.getX(), evt.getY(),
              av.getAlignment().getHiddenSequences(),
              av.getAlignment().getHiddenColumns());
    }
    ap.paintAlignment(false, false);
  }

  /**
   * Updates the overview image when the related alignment panel is updated
   */
  public void updateOverviewImage()
  {
    if (oviewCanvas == null)
    {
      /*
       * panel has been disposed
       */
      return;
    }

    if ((getSize().width > 0) && (getSize().height > 0))
    {
      od.setWidth(getSize().width);
      od.setHeight(getSize().height);
    }
    setSize(new Dimension(od.getWidth(), od.getHeight()));

    synchronized (this)
    {
      if (updateRunning)
      {
        oviewCanvas.restartDraw();
        return;
      }

      updateRunning = true;
    }
    Thread thread = new Thread(this);
    thread.start();
    repaint();
    updateRunning = false;
  }

  @Override
  public void run()
  {
    oviewCanvas.draw(av.isShowSequenceFeatures(),
            (av.isShowAnnotation()
                    && av.getAlignmentConservationAnnotation() != null),
            ap.seqPanel.seqCanvas.getFeatureRenderer());
    setBoxPosition();
  }

  /**
   * Update the overview panel box when the associated alignment panel is
   * changed
   * 
   */
  private void setBoxPosition()
  {
    od.setBoxPosition(av.getAlignment().getHiddenSequences(),
            av.getAlignment().getHiddenColumns());
    repaint();
  }

  /*
   * Displays the popup menu and acts on user input
   */
  private void showPopupMenu(MouseEvent e)
  {
    PopupMenu popup = new PopupMenu();
    ItemListener menuListener = new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        toggleHiddenColumns();
      }
    };
    CheckboxMenuItem item = new CheckboxMenuItem(
            MessageManager.getString("label.togglehidden"));
    item.setState(showHidden);
    popup.add(item);
    item.addItemListener(menuListener);
    this.add(popup);
    popup.show(this, e.getX(), e.getY());
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    setBoxPosition();
  }

  /*
   * Toggle overview display between showing hidden columns and hiding hidden columns
   */
  private void toggleHiddenColumns()
  {
    if (showHidden)
    {
      showHidden = false;
      od = new OverviewDimensionsHideHidden(av.getRanges(),
              (av.isShowAnnotation()
                      && av.getAlignmentConservationAnnotation() != null));
    }
    else
    {
      showHidden = true;
      od = new OverviewDimensionsShowHidden(av.getRanges(),
              (av.isShowAnnotation()
                      && av.getAlignmentConservationAnnotation() != null));
    }
    oviewCanvas.resetOviewDims(od);
    updateOverviewImage();
  }

  /**
   * Removes this object as a property change listener, and nulls references
   */
  protected void dispose()
  {
    try
    {
      av.getRanges().removePropertyChangeListener(this);
      Frame parent = (Frame) getParent();
      parent.dispose();
      parent.setVisible(false);
    } finally
    {
      av = null;
      if (oviewCanvas != null)
      {
        oviewCanvas.dispose();
      }
      oviewCanvas = null;
      ap = null;
      od = null;
    }
  }
}
