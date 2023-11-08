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
package jalview.jbgui;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JInternalFrame;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import jalview.util.Platform;

public class GSplitFrame extends JInternalFrame
{
  protected static final int DIVIDER_SIZE = 5;

  private static final long serialVersionUID = 1L;

  private GAlignFrame topFrame;

  private GAlignFrame bottomFrame;

  private JSplitPane splitPane;

  /*
   * proportional position of split divider; saving this allows it to be
   * restored after hiding one half and resizing
   */
  private double dividerRatio;

  /**
   * Constructor
   * 
   * @param top
   * @param bottom
   */
  public GSplitFrame(GAlignFrame top, GAlignFrame bottom)
  {
    setFrameIcon(null);
    setName("jalview-splitframe");
    this.topFrame = top;
    this.bottomFrame = bottom;

    hideTitleBars();

    addSplitPane();
  }

  /**
   * Create and add the split pane containing the top and bottom components.
   */
  protected void addSplitPane()
  {
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topFrame,
            bottomFrame);
    splitPane.setVisible(true);

    /*
     * set divider split at 50:50, or restore saved split if loading from
     * project
     */
    int topFrameHeight = topFrame.getHeight();
    splitPane.setDividerSize(DIVIDER_SIZE);
    if (topFrameHeight == 0)
    {
      setRelativeDividerLocation(0.5d); // as a proportion
    }
    else
    {
      int dividerPosition = topFrameHeight + DIVIDER_SIZE / 2;
      splitPane.setDividerLocation(dividerPosition); // absolute position
    }
    splitPane.setResizeWeight(0.5d);
    add(splitPane);
  }

  /**
   * Try to hide the title bars as a waste of precious space.
   * 
   * @see http
   *      ://stackoverflow.com/questions/7218971/java-method-works-on-windows
   *      -but-not-macintosh -java
   */
  protected void hideTitleBars()
  {
    if (Platform.isAMacAndNotJS())
    {
      // this saves some space - but doesn't hide the title bar
      topFrame.putClientProperty("JInternalFrame.isPalette", true);
      // topFrame.getRootPane().putClientProperty("Window.style", "small");
      bottomFrame.putClientProperty("JInternalFrame.isPalette", true);
    }
    else
    {
      ((BasicInternalFrameUI) topFrame.getUI()).setNorthPane(null);
      ((BasicInternalFrameUI) bottomFrame.getUI()).setNorthPane(null);
    }
  }

  public GAlignFrame getTopFrame()
  {
    return topFrame;
  }

  public GAlignFrame getBottomFrame()
  {
    return bottomFrame;
  }

  /**
   * Returns the split pane component the mouse is in, or null if neither.
   * 
   * @return
   */
  protected GAlignFrame getFrameAtMouse()
  {
    Point loc = MouseInfo.getPointerInfo().getLocation();

    if (isIn(loc, splitPane.getTopComponent()))
    {
      return getTopFrame();
    }
    else if (isIn(loc, splitPane.getBottomComponent()))
    {
      return getBottomFrame();
    }
    return null;
  }

  private boolean isIn(Point loc, Component comp)
  {
    if (!comp.isVisible())
    {
      return false;
    }
    Point p = comp.getLocationOnScreen();
    Rectangle r = new Rectangle(p.x, p.y, comp.getWidth(),
            comp.getHeight());
    return r.contains(loc);
  }

  /**
   * Makes the complement of the specified split component visible or hidden,
   * restoring or saving the position of the split divide.
   */
  public void setComplementVisible(Object alignFrame, boolean show)
  {
    /*
     * save divider ratio on hide, restore on show
     */
    if (show)
    {
      setRelativeDividerLocation(dividerRatio);
    }
    else
    {
      this.dividerRatio = splitPane.getDividerLocation()
              / (double) (splitPane.getHeight()
                      - splitPane.getDividerSize());
    }

    if (alignFrame == this.topFrame)
    {
      this.bottomFrame.setVisible(show);
    }
    else if (alignFrame == this.bottomFrame)
    {
      this.topFrame.setVisible(show);
    }

    validate();
  }

  /**
   * Set the divider location as a proportion (0 <= r <= 1) of the height <br>
   * Warning: this overloads setDividerLocation(int), and getDividerLocation()
   * returns the int (pixel count) value
   * 
   * @param r
   */
  public void setRelativeDividerLocation(double r)
  {
    this.dividerRatio = r;
    splitPane.setDividerLocation(r);
  }

  /**
   * Sets the divider location (in pixels from top)
   * 
   * @return
   */
  protected void setDividerLocation(int p)
  {
    splitPane.setDividerLocation(p);
  }

  /**
   * Returns the divider location (in pixels from top)
   * 
   * @return
   */
  protected int getDividerLocation()
  {
    return splitPane.getDividerLocation();
  }
}
