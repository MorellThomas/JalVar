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

import jalview.api.RotatableCanvasI;
import jalview.datamodel.Point;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequencePoint;
import jalview.math.RotatableMatrix;
import jalview.math.RotatableMatrix.Axis;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class RotatableCanvas extends Panel implements MouseListener,
        MouseMotionListener, KeyListener, RotatableCanvasI
{
  private static final int DIMS = 3;

  String tooltip;

  int toolx;

  int tooly;

  // RubberbandRectangle rubberband;

  boolean drawAxes = true;

  int mouseX = 0;

  int mouseY = 0;

  Image img;

  Graphics ig;

  Dimension prefsize;

  Point centre;

  float[] width = new float[DIMS];

  float[] max = new float[DIMS];

  float[] min = new float[DIMS];

  float maxwidth;

  float scale;

  int npoint;

  List<SequencePoint> points;

  Point[] orig;

  Point[] axisEndPoints;

  int startx;

  int starty;

  int lastx;

  int lasty;

  int rectx1;

  int recty1;

  int rectx2;

  int recty2;

  float scalefactor = 1;

  AlignmentViewport av;

  boolean showLabels = false;

  public RotatableCanvas(AlignmentViewport viewport)
  {
    this.av = viewport;
    axisEndPoints = new Point[DIMS];
  }

  public void showLabels(boolean b)
  {
    showLabels = b;
    repaint();
  }

  @Override
  public void setPoints(List<SequencePoint> points, int npoint)
  {
    this.points = points;
    this.npoint = npoint;
    PaintRefresher.Register(this, av.getSequenceSetId());

    prefsize = getPreferredSize();
    orig = new Point[npoint];

    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = points.get(i);
      orig[i] = sp.coord;
    }

    resetAxes();

    findCentre();
    findWidth();

    scale = findScale();

    // System.out.println("Scale factor = " + scale);

    addMouseListener(this);
    addKeyListener(this);
    // if (getParent() != null) {
    // getParent().addKeyListener(this);
    // }
    addMouseMotionListener(this);

    // Add rubberband
    // rubberband = new RubberbandRectangle(this);
    // rubberband.setActive(true);
    // rubberband.addListener(this);
  }

  /*
   * public boolean handleSequenceSelectionEvent(SequenceSelectionEvent evt) {
   * redrawneeded = true; repaint(); return true; }
   * 
   * public void removeNotify() { controller.removeListener(this);
   * super.removeNotify(); }
   */

  /**
   * Resets axes to the initial state: x-axis to the right, y-axis up, z-axis to
   * back (so obscured in a 2-D display)
   */
  public void resetAxes()
  {
    axisEndPoints[0] = new Point(1f, 0f, 0f);
    axisEndPoints[1] = new Point(0f, 1f, 0f);
    axisEndPoints[2] = new Point(0f, 0f, 1f);
  }

  /**
   * Computes and saves the maximum and minimum (x, y, z) positions of any
   * sequence point, and also the min-max range (width) for each dimension, and
   * the maximum width for all dimensions
   */
  public void findWidth()
  {
    max = new float[3];
    min = new float[3];

    max[0] = Float.MIN_VALUE;
    max[1] = Float.MIN_VALUE;
    max[2] = Float.MIN_VALUE;

    min[0] = Float.MAX_VALUE;
    min[1] = Float.MAX_VALUE;
    min[2] = Float.MAX_VALUE;

    for (SequencePoint sp : points)
    {
      max[0] = Math.max(max[0], sp.coord.x);
      max[1] = Math.max(max[1], sp.coord.y);
      max[2] = Math.max(max[2], sp.coord.z);
      min[0] = Math.min(min[0], sp.coord.x);
      min[1] = Math.min(min[1], sp.coord.y);
      min[2] = Math.min(min[2], sp.coord.z);
    }

    width[0] = Math.abs(max[0] - min[0]);
    width[1] = Math.abs(max[1] - min[1]);
    width[2] = Math.abs(max[2] - min[2]);

    maxwidth = Math.max(width[0], Math.max(width[1], width[2]));
  }

  public float findScale()
  {
    int dim, w, height;
    if (getSize().width != 0)
    {
      w = getSize().width;
      height = getSize().height;
    }
    else
    {
      w = prefsize.width;
      height = prefsize.height;
    }

    if (w < height)
    {
      dim = w;
    }
    else
    {
      dim = height;
    }

    return dim * scalefactor / (2 * maxwidth);
  }

  /**
   * Computes and saves the position of the centre of the view
   */
  public void findCentre()
  {
    findWidth();

    float x = (max[0] + min[0]) / 2;
    float y = (max[1] + min[1]) / 2;
    float z = (max[2] + min[2]) / 2;

    centre = new Point(x, y, z);
  }

  @Override
  public Dimension getPreferredSize()
  {
    if (prefsize != null)
    {
      return prefsize;
    }
    else
    {
      return new Dimension(400, 400);
    }
  }

  @Override
  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }

  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {
    if (points == null)
    {
      g.setFont(new Font("Verdana", Font.PLAIN, 18));
      g.drawString(
              MessageManager.getString("label.calculating_pca") + "....",
              20, getSize().height / 2);
    }
    else
    {

      // Only create the image at the beginning -
      if ((img == null) || (prefsize.width != getSize().width)
              || (prefsize.height != getSize().height))
      {
        prefsize.width = getSize().width;
        prefsize.height = getSize().height;

        scale = findScale();

        // System.out.println("New scale = " + scale);
        img = createImage(getSize().width, getSize().height);
        ig = img.getGraphics();

      }

      drawBackground(ig, Color.black);
      drawScene(ig);
      if (drawAxes)
      {
        drawAxes(ig);
      }

      if (tooltip != null)
      {
        ig.setColor(Color.red);
        ig.drawString(tooltip, toolx, tooly);
      }

      g.drawImage(img, 0, 0, this);
    }
  }

  public void drawAxes(Graphics g)
  {

    g.setColor(Color.yellow);
    for (int i = 0; i < 3; i++)
    {
      g.drawLine(getSize().width / 2, getSize().height / 2,
              (int) (axisEndPoints[i].x * scale * max[0]
                      + getSize().width / 2),
              (int) (axisEndPoints[i].y * scale * max[1]
                      + getSize().height / 2));
    }
  }

  public void drawBackground(Graphics g, Color col)
  {
    g.setColor(col);
    g.fillRect(0, 0, prefsize.width, prefsize.height);
  }

  public void drawScene(Graphics g)
  {
    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = points.get(i);
      SequenceI sequence = sp.getSequence();
      Color sequenceColour = av.getSequenceColour(sequence);
      g.setColor(
              sequenceColour == Color.black ? Color.white : sequenceColour);
      if (av.getSelectionGroup() != null)
      {
        if (av.getSelectionGroup().getSequences(null).contains(sequence))
        {
          g.setColor(Color.gray);
        }
      }

      if (sp.coord.z < centre.z)
      {
        g.setColor(g.getColor().darker());
      }

      int halfwidth = getSize().width / 2;
      int halfheight = getSize().height / 2;
      int x = (int) ((sp.coord.x - centre.x) * scale) + halfwidth;
      int y = (int) ((sp.coord.y - centre.y) * scale) + halfheight;
      g.fillRect(x - 3, y - 3, 6, 6);

      if (showLabels)
      {
        g.setColor(Color.red);
        g.drawString(sequence.getName(), x - 3, y - 4);
      }
    }
  }

  @Override
  public void keyTyped(KeyEvent evt)
  {
  }

  @Override
  public void keyReleased(KeyEvent evt)
  {
  }

  @Override
  public void keyPressed(KeyEvent evt)
  {
    boolean shiftDown = evt.isShiftDown();
    int keyCode = evt.getKeyCode();
    if (keyCode == KeyEvent.VK_UP)
    {
      if (shiftDown)
      {
        rotate(0f, -1f);
      }
      else
      {
        zoom(1.1f);
      }
    }
    else if (keyCode == KeyEvent.VK_DOWN)
    {
      if (shiftDown)
      {
        rotate(0f, 1f);
      }
      else
      {
        zoom(0.9f);
      }
    }
    else if (shiftDown && keyCode == KeyEvent.VK_LEFT)
    {
      rotate(1f, 0f);
    }
    else if (shiftDown && keyCode == KeyEvent.VK_RIGHT)
    {
      rotate(-1f, 0f);
    }
    else if (evt.getKeyChar() == 's')
    {
      System.err.println("DEBUG: Rectangle selection"); // log.debug
      if (rectx2 != -1 && recty2 != -1)
      {
        rectSelect(rectx1, recty1, rectx2, recty2);

      }
    }
    repaint();
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
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
  public void mouseReleased(MouseEvent evt)
  {
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    int x = evt.getX();
    int y = evt.getY();

    mouseX = x;
    mouseY = y;

    startx = x;
    starty = y;

    rectx1 = x;
    recty1 = y;

    rectx2 = -1;
    recty2 = -1;

    SequenceI found = findSequenceAtPoint(x, y);

    if (found != null)
    {
      // TODO: applet PCA is not associatable with multi-panels - only parent
      // view
      if (av.getSelectionGroup() != null)
      {
        av.getSelectionGroup().addOrRemove(found, true);
        av.getSelectionGroup().setEndRes(av.getAlignment().getWidth() - 1);
      }
      else
      {
        av.setSelectionGroup(new SequenceGroup());
        av.getSelectionGroup().addOrRemove(found, true);
        av.getSelectionGroup().setEndRes(av.getAlignment().getWidth() - 1);

      }
      PaintRefresher.Refresh(this, av.getSequenceSetId());
      av.sendSelection();
    }
    repaint();
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    SequenceI found = findSequenceAtPoint(evt.getX(), evt.getY());
    if (found == null)
    {
      tooltip = null;
    }
    else
    {
      tooltip = found.getName();
      toolx = evt.getX();
      tooly = evt.getY();
    }
    repaint();
  }

  @Override
  public void mouseDragged(MouseEvent evt)
  {
    int xPos = evt.getX();
    int yPos = evt.getY();

    if (xPos == mouseX && yPos == mouseY)
    {
      return;
    }

    int xDelta = xPos - mouseX;
    int yDelta = yPos - mouseY;

    rotate(xDelta, yDelta);
    repaint();
  }

  public void rectSelect(int x1, int y1, int x2, int y2)
  {
    // boolean changedSel = false;
    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = points.get(i);
      int tmp1 = (int) ((sp.coord.x - centre.x) * scale
              + getSize().width / 2.0);
      int tmp2 = (int) ((sp.coord.y - centre.y) * scale
              + getSize().height / 2.0);

      SequenceI sequence = sp.getSequence();
      if (tmp1 > x1 && tmp1 < x2 && tmp2 > y1 && tmp2 < y2)
      {
        if (av != null)
        {
          if (!av.getSelectionGroup().getSequences(null).contains(sequence))
          {
            av.getSelectionGroup().addSequence(sequence, true);
          }
        }
      }
    }
  }

  /**
   * Answers the first sequence found whose point on the display is within 2
   * pixels of the given coordinates, or null if none is found
   * 
   * @param x
   * @param y
   * 
   * @return
   */
  public SequenceI findSequenceAtPoint(int x, int y)
  {
    int halfwidth = getSize().width / 2;
    int halfheight = getSize().height / 2;

    int found = -1;

    for (int i = 0; i < npoint; i++)
    {

      SequencePoint sp = points.get(i);
      int px = (int) ((sp.coord.x - centre.x) * scale) + halfwidth;
      int py = (int) ((sp.coord.y - centre.y) * scale) + halfheight;

      if (Math.abs(px - x) < 3 && Math.abs(py - y) < 3)
      {
        found = i;
        break;
      }
    }

    if (found != -1)
    {
      return points.get(found).getSequence();
    }
    else
    {
      return null;
    }
  }

  /**
   * Resets the view to initial state (no rotation)
   */
  public void resetView()
  {
    img = null;
    resetAxes();
  }

  @Override
  public void zoom(float factor)
  {
    if (factor > 0f)
    {
      scalefactor *= factor;
    }
    scale = findScale();
  }

  @Override
  public void rotate(float x, float y)
  {
    if (x == 0f && y == 0f)
    {
      return;
    }

    /*
     * get the identity transformation...
     */
    RotatableMatrix rotmat = new RotatableMatrix();

    /*
     * rotate around the X axis for change in Y
     * (mouse movement up/down); note we are equating a
     * number of pixels with degrees of rotation here!
     */
    if (y != 0)
    {
      rotmat.rotate(y, Axis.X);
    }

    /*
     * rotate around the Y axis for change in X
     * (mouse movement left/right)
     */
    if (x != 0)
    {
      rotmat.rotate(x, Axis.Y);
    }

    /*
     * apply the composite transformation to sequence points
     */
    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = points.get(i);
      sp.translate(-centre.x, -centre.y, -centre.z);

      // Now apply the rotation matrix
      sp.coord = rotmat.vectorMultiply(sp.coord);

      // Now translate back again
      sp.translate(centre.x, centre.y, centre.z);
    }

    /*
     * rotate the x/y/z axis positions
     */
    for (int i = 0; i < DIMS; i++)
    {
      axisEndPoints[i] = rotmat.vectorMultiply(axisEndPoints[i]);
    }
  }

}
