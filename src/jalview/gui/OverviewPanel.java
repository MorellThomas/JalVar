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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import jalview.renderer.OverviewRenderer;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.OverviewDimensions;
import jalview.viewmodel.OverviewDimensionsHideHidden;
import jalview.viewmodel.OverviewDimensionsShowHidden;
import jalview.viewmodel.ViewportListenerI;

/**
 * Panel displaying an overview of the full alignment, with an interactive box
 * representing the viewport onto the alignment.
 * 
 * @author $author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class OverviewPanel extends JPanel
        implements Runnable, ViewportListenerI
{
  protected OverviewDimensions od;

  private OverviewCanvas oviewCanvas;

  protected AlignViewport av;

  private AlignmentPanel ap;

  private JInternalFrame internalFrame;

  protected JCheckBoxMenuItem displayToggle;

  protected boolean showHidden = true;

  protected boolean draggingBox = false;

  protected ProgressPanel progressPanel;

  /**
   * Creates a new OverviewPanel object.
   * 
   * @param alPanel
   *          The alignment panel which is shown in the overview panel
   * @param frame
   * @param isShowHidden
   *          TODO
   */
  public OverviewPanel(AlignmentPanel alPanel, JInternalFrame frame,
          boolean isShowHidden)
  {
    this.av = alPanel.av;
    this.ap = alPanel;
    this.internalFrame = frame;

    showHidden = isShowHidden;
    if (showHidden)
    {
      od = new OverviewDimensionsShowHidden(av.getRanges(),
              (av.isShowAnnotation()
                      && av.getAlignmentConservationAnnotation() != null));
    }
    else
    {
      od = new OverviewDimensionsHideHidden(av.getRanges(),
              (av.isShowAnnotation()
                      && av.getAlignmentConservationAnnotation() != null));
    }

    setLayout(new BorderLayout());
    progressPanel = new ProgressPanel(OverviewRenderer.UPDATE,
            MessageManager.getString("label.oview_calc"), getWidth());
    this.add(progressPanel, BorderLayout.SOUTH);
    oviewCanvas = new OverviewCanvas(od, av, progressPanel);

    add(oviewCanvas, BorderLayout.CENTER);

    av.getRanges().addPropertyChangeListener(this);

    // without this the overview window does not size to fit the overview canvas
    setPreferredSize(new Dimension(od.getWidth(), od.getHeight()));

    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent evt)
      {
        // Resize is called on the initial display of the overview.
        // This code adjusts sizes to account for the progress bar if it has not
        // already been accounted for, which triggers another resize call for
        // the correct sizing, at which point the overview image is updated.
        // (This avoids a double recalculation of the image.)
        if (getWidth() == od.getWidth() && getHeight() == od.getHeight()
                + progressPanel.getHeight())
        {
          updateOverviewImage();
        }
        else
        {
          if ((getWidth() > 0) && (getHeight() > 0))
          {
            od.setWidth(getWidth());
            od.setHeight(getHeight() - progressPanel.getHeight());
          }

          setPreferredSize(new Dimension(od.getWidth(),
                  od.getHeight() + progressPanel.getHeight()));
        }
      }

    });

    addMouseMotionListener(new MouseMotionAdapter()
    {
      @Override
      public void mouseDragged(MouseEvent evt)
      {
        if (!SwingUtilities.isRightMouseButton(evt))
        {
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
        }
      }

      @Override
      public void mouseMoved(MouseEvent evt)
      {
        if (od.isPositionInBox(evt.getX(), evt.getY()))
        {
          /*
           * using HAND_CURSOR rather than DRAG_CURSOR 
           * as the latter is not supported on Mac
           */
          getParent().setCursor(
                  Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else
        {
          // reset cursor
          getParent().setCursor(
                  Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
      }

    });

    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent evt)
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
          getParent().setCursor(
                  Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else
        {
          draggingBox = true;
          od.setDragPoint(evt.getX(), evt.getY(),
                  av.getAlignment().getHiddenSequences(),
                  av.getAlignment().getHiddenColumns());
        }
      }

      @Override
      public void mouseClicked(MouseEvent evt)
      {
        if (SwingUtilities.isRightMouseButton(evt))
        {
          showPopupMenu(evt);
        }
      }

      @Override
      public void mouseReleased(MouseEvent evt)
      {
        draggingBox = false;
      }

    });

    /*
     * Javascript does not call componentResized on initial display,
     * so do the update here
     */
    if (Platform.isJS())
    {
      updateOverviewImage();
    }
  }

  /*
   * Displays the popup menu and acts on user input
   */
  protected void showPopupMenu(MouseEvent e)
  {
    JPopupMenu popup = new JPopupMenu();
    ActionListener menuListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent event)
      {
        // switch on/off the hidden columns view
        toggleHiddenColumns();
        displayToggle.setSelected(showHidden);
      }
    };
    displayToggle = new JCheckBoxMenuItem(
            MessageManager.getString("label.togglehidden"));
    displayToggle.setEnabled(true);
    displayToggle.setSelected(showHidden);
    popup.add(displayToggle);
    displayToggle.addActionListener(menuListener);
    popup.show(this, e.getX(), e.getY());
  }

  /*
   * Toggle overview display between showing hidden columns and hiding hidden columns
   */
  protected void toggleHiddenColumns()
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
    setBoxPosition();
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

    if ((getWidth() > 0) && (getHeight() > 0))
    {
      od.setWidth(getWidth());
      od.setHeight(getHeight() - progressPanel.getHeight());
    }

    setPreferredSize(new Dimension(od.getWidth(),
            od.getHeight() + progressPanel.getHeight()));

    if (oviewCanvas.restartDraw())
    {
      return;
    }

    Thread thread = new Thread(this);
    thread.start();
    repaint();

  }

  @Override
  public void run()
  {
    if (oviewCanvas != null)
    {
      oviewCanvas.draw(av.isShowSequenceFeatures(),
              (av.isShowAnnotation()
                      && av.getAlignmentConservationAnnotation() != null),
              ap.getSeqPanel().seqCanvas.getFeatureRenderer());
      setBoxPosition();
    }
  }

  /**
   * Update the overview panel box when the associated alignment panel is
   * changed
   * 
   */
  private void setBoxPositionOnly()
  {
    if (od != null)
    {
      int oldX = od.getBoxX();
      int oldY = od.getBoxY();
      int oldWidth = od.getBoxWidth();
      int oldHeight = od.getBoxHeight();
      od.setBoxPosition(av.getAlignment().getHiddenSequences(),
              av.getAlignment().getHiddenColumns());
      repaint(oldX - 1, oldY - 1, oldWidth + 2, oldHeight + 2);
      repaint(od.getBoxX(), od.getBoxY(), od.getBoxWidth(),
              od.getBoxHeight());
    }
  }

  private void setBoxPosition()
  {
    if (od != null)
    {
      od.setBoxPosition(av.getAlignment().getHiddenSequences(),
              av.getAlignment().getHiddenColumns());
      repaint();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    setBoxPositionOnly();
  }

  /**
   * Removes this object as a property change listener, and nulls references
   */
  protected void dispose()
  {
    try
    {
      if (av != null)
      {
        av.getRanges().removePropertyChangeListener(this);
      }

      oviewCanvas.dispose();

      /*
       * close the parent frame (which also removes it from the
       * Desktop Windows menu)
       */
      ((JInternalFrame) SwingUtilities
              .getAncestorOfClass(JInternalFrame.class, (this)))
                      .setClosed(true);
    } catch (PropertyVetoException e)
    {
      // ignore
    } finally
    {
      progressPanel = null;
      av = null;
      oviewCanvas = null;
      ap = null;
      od = null;
      internalFrame = null;
    }
  }

  public boolean isShowHiddenRegions()
  {
    return showHidden;
  }

  public OverviewCanvas getCanvas()
  {
    return oviewCanvas;
  }

  /**
   * Sets the title of the enclosing frame
   * 
   * @param title
   */
  public void setTitle(String title)
  {
    internalFrame.setTitle(title);
  }

  /**
   * Returns the title of the enclosing frame
   * 
   * @return title
   */
  public String getTitle()
  {
    return internalFrame.getTitle();
  }

  /**
   * Sets the bounds of the frame holding the Overview panel
   * 
   * @param xpos
   * @param ypos
   * @param width
   * @param height
   */
  public void setFrameBounds(int xpos, int ypos, int width, int height)
  {
    internalFrame.setBounds(xpos, ypos, width, height);
  }

  /**
   * Returns the bounds of the enclosing frame
   * 
   * @return
   */
  public Rectangle getFrameBounds()
  {
    return internalFrame.getBounds();
  }

  /**
   * Closes the frame containing the overview panel
   */
  public void close()
  {
    internalFrame.dispose();
  }
}
