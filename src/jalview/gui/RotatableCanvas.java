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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import jalview.api.RotatableCanvasI;
import jalview.datamodel.Point;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequencePoint;
import jalview.math.RotatableMatrix;
import jalview.math.RotatableMatrix.Axis;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

/**
 * Models a Panel on which a set of points, and optionally x/y/z axes, can be
 * drawn, and rotated or zoomed with the mouse
 */
public class RotatableCanvas extends JPanel
        implements MouseListener, MouseMotionListener, KeyListener,
        RotatableCanvasI, MouseWheelListener
{
  private static final float ZOOM_OUT = 0.9f;

  private static final float ZOOM_IN = 1.1f;

  /*
   * pixels distance within which tooltip shows sequence name
   */
  private static final int NEARBY = 3;

  private static final List<String> AXES = Arrays.asList("x", "y", "z");

  private static final Color AXIS_COLOUR = Color.yellow;

  private static final int DIMS = 3;

  boolean drawAxes = true;

  int mouseX;

  int mouseY;

  Image img;

  Graphics ig;

  Dimension prefSize;

  /*
   * the min-max [x, y, z] values of sequence points when the points
   * were set on the object, or when the view is reset; 
   * x and y ranges are not recomputed as points are rotated, as this
   * would make scaling (zoom) unstable, but z ranges are (for correct
   * graduated colour brightness based on z-coordinate)
   */
  float[] seqMin;

  float[] seqMax;

  /*
   * a scale factor used in drawing; when equal to 1, the points span
   * half the available width or height (whichever is less); increase this
   * factor to zoom in, decrease it to zoom out
   */
  private float scaleFactor;

  int npoint;

  /*
   * sequences and their (x, y, z) PCA dimension values
   */
  List<SequencePoint> sequencePoints;

  /*
   * x, y, z axis end points (PCA dimension values)
   */
  private Point[] axisEndPoints;

  // fields for 'select rectangle' (JAL-1124)
  // int rectx1;
  // int recty1;
  // int rectx2;
  // int recty2;

  AlignmentViewport av;

  AlignmentPanel ap;

  private boolean showLabels;

  private Color bgColour;

  private boolean applyToAllViews;

  /**
   * Constructor
   * 
   * @param panel
   */
  public RotatableCanvas(AlignmentPanel panel)
  {
    this.av = panel.av;
    this.ap = panel;
    setAxisEndPoints(new Point[DIMS]);
    setShowLabels(false);
    setApplyToAllViews(false);
    setBgColour(Color.BLACK);
    resetAxes();

    ToolTipManager.sharedInstance().registerComponent(this);

    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  }

  /**
   * Refreshes the display with labels shown (or not)
   * 
   * @param show
   */
  public void showLabels(boolean show)
  {
    setShowLabels(show);
    repaint();
  }

  @Override
  public void setPoints(List<SequencePoint> points, int np)
  {
    this.sequencePoints = points;
    this.npoint = np;
    prefSize = getPreferredSize();

    findWidths();

    setScaleFactor(1f);
  }

  /**
   * Resets axes to the initial state: x-axis to the right, y-axis up, z-axis to
   * back (so obscured in a 2-D display)
   */
  protected void resetAxes()
  {
    getAxisEndPoints()[0] = new Point(1f, 0f, 0f);
    getAxisEndPoints()[1] = new Point(0f, 1f, 0f);
    getAxisEndPoints()[2] = new Point(0f, 0f, 1f);
  }

  /**
   * Computes and saves the min-max ranges of x/y/z positions of the sequence
   * points
   */
  protected void findWidths()
  {
    float[] max = new float[DIMS];
    float[] min = new float[DIMS];

    max[0] = -Float.MAX_VALUE;
    max[1] = -Float.MAX_VALUE;
    max[2] = -Float.MAX_VALUE;

    min[0] = Float.MAX_VALUE;
    min[1] = Float.MAX_VALUE;
    min[2] = Float.MAX_VALUE;

    for (SequencePoint sp : sequencePoints)
    {
      max[0] = Math.max(max[0], sp.coord.x);
      max[1] = Math.max(max[1], sp.coord.y);
      max[2] = Math.max(max[2], sp.coord.z);
      min[0] = Math.min(min[0], sp.coord.x);
      min[1] = Math.min(min[1], sp.coord.y);
      min[2] = Math.min(min[2], sp.coord.z);
    }

    seqMin = min;
    seqMax = max;
  }

  /**
   * Answers the preferred size if it has been set, else 400 x 400
   * 
   * @return
   */
  @Override
  public Dimension getPreferredSize()
  {
    if (prefSize != null)
    {
      return prefSize;
    }
    else
    {
      return new Dimension(400, 400);
    }
  }

  /**
   * Answers the preferred size
   * 
   * @return
   * @see RotatableCanvas#getPreferredSize()
   */
  @Override
  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }

  /**
   * Repaints the panel
   * 
   * @param g
   */
  @Override
  public void paintComponent(Graphics g1)
  {

    Graphics2D g = (Graphics2D) g1;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    if (sequencePoints == null)
    {
      g.setFont(new Font("Verdana", Font.PLAIN, 18));
      g.drawString(
              MessageManager.getString("label.calculating_pca") + "....",
              20, getHeight() / 2);
    }
    else
    {
      /*
       * create the image at the beginning or after a resize
       */
      boolean resized = prefSize.width != getWidth()
              || prefSize.height != getHeight();
      if (img == null || resized)
      {
        prefSize.width = getWidth();
        prefSize.height = getHeight();

        img = createImage(getWidth(), getHeight());
        ig = img.getGraphics();
      }

      drawBackground(ig);
      drawScene(ig);

      if (drawAxes)
      {
        drawAxes(ig);
      }

      g.drawImage(img, 0, 0, this);
    }
  }

  /**
   * Resets the rotation and choice of axes to the initial state (without change
   * of scale factor)
   */
  public void resetView()
  {
    img = null;
    findWidths();
    resetAxes();
    repaint();
  }

  /**
   * Draws lines for the x, y, z axes
   * 
   * @param g
   */
  public void drawAxes(Graphics g)
  {
    g.setColor(AXIS_COLOUR);

    int midX = getWidth() / 2;
    int midY = getHeight() / 2;
    // float maxWidth = Math.max(Math.abs(seqMax[0] - seqMin[0]),
    // Math.abs(seqMax[1] - seqMin[1]));
    int pix = Math.min(getWidth(), getHeight());
    float scaleBy = pix * getScaleFactor() / (2f);

    for (int i = 0; i < DIMS; i++)
    {
      g.drawLine(midX, midY,
              midX + (int) (getAxisEndPoints()[i].x * scaleBy * 0.25),
              midY + (int) (getAxisEndPoints()[i].y * scaleBy * 0.25));
    }
  }

  /**
   * Fills the background with the currently configured background colour
   * 
   * @param g
   */
  public void drawBackground(Graphics g)
  {
    g.setColor(getBgColour());
    g.fillRect(0, 0, prefSize.width, prefSize.height);
  }

  /**
   * Draws points (6x6 squares) for the sequences of the PCA, and labels
   * (sequence names) if configured to do so. The sequence points colours are
   * taken from the sequence ids in the alignment (converting black to white).
   * Sequences 'at the back' (z-coordinate is negative) are shaded slightly
   * darker to help give a 3-D sensation.
   * 
   * @param g
   */
  public void drawScene(Graphics g1)
  {
    Graphics2D g = (Graphics2D) g1;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    int pix = Math.min(getWidth(), getHeight());
    float xWidth = Math.abs(seqMax[0] - seqMin[0]);
    float yWidth = Math.abs(seqMax[1] - seqMin[1]);
    float maxWidth = Math.max(xWidth, yWidth);
    float scaleBy = pix * getScaleFactor() / (2f * maxWidth);

    float[] centre = getCentre();

    for (int i = 0; i < npoint; i++)
    {
      /*
       * sequence point colour as sequence id, but
       * gray if sequence is currently selected
       */
      SequencePoint sp = sequencePoints.get(i);
      Color sequenceColour = getSequencePointColour(sp);
      g.setColor(sequenceColour);

      int halfwidth = getWidth() / 2;
      int halfheight = getHeight() / 2;
      int x = (int) ((sp.coord.x - centre[0]) * scaleBy) + halfwidth;
      int y = (int) ((sp.coord.y - centre[1]) * scaleBy) + halfheight;
      g.fillRect(x - 3, y - 3, 6, 6);

      if (isShowLabels())
      {
        g.setColor(Color.red);
        g.drawString(sp.getSequence().getName(), x - 3, y - 4);
      }
    }
    if (isShowLabels())
    {
      g.setColor(AXIS_COLOUR);
      int midX = getWidth() / 2;
      int midY = getHeight() / 2;
      Iterator<String> axes = AXES.iterator();
      for (Point p : getAxisEndPoints())
      {
        int x = midX + (int) (p.x * scaleBy * seqMax[0]);
        int y = midY + (int) (p.y * scaleBy * seqMax[1]);
        g.drawString(axes.next(), x - 3, y - 4);
      }
    }
    // //Now the rectangle
    // if (rectx2 != -1 && recty2 != -1) {
    // g.setColor(Color.white);
    //
    // g.drawRect(rectx1,recty1,rectx2-rectx1,recty2-recty1);
    // }
  }

  /**
   * Determines the colour to use when drawing a sequence point. The colour is
   * taken from the sequence id, with black converted to white, and then
   * graduated from darker (at the back) to brighter (at the front) based on the
   * z-axis coordinate of the point.
   * 
   * @param sp
   * @return
   */
  protected Color getSequencePointColour(SequencePoint sp)
  {
    SequenceI sequence = sp.getSequence();
    Color sequenceColour = av.getSequenceColour(sequence);
    if (sequenceColour == Color.black)
    {
      sequenceColour = Color.white;
    }
    if (av.getSelectionGroup() != null)
    {
      if (av.getSelectionGroup().getSequences(null).contains(sequence))
      {
        sequenceColour = Color.gray;
      }
    }

    /*
     * graduate brighter for point in front of centre, darker if behind centre
     */
    float zCentre = (seqMin[2] + seqMax[2]) / 2f;
    if (sp.coord.z > zCentre)
    {
      sequenceColour = ColorUtils.getGraduatedColour(sp.coord.z, 0,
              sequenceColour, seqMax[2], sequenceColour.brighter());
    }
    else if (sp.coord.z < zCentre)
    {
      sequenceColour = ColorUtils.getGraduatedColour(sp.coord.z, seqMin[2],
              sequenceColour.darker(), 0, sequenceColour);
    }

    return sequenceColour;
  }

  @Override
  public void keyTyped(KeyEvent evt)
  {
  }

  @Override
  public void keyReleased(KeyEvent evt)
  {
  }

  /**
   * Responds to up or down arrow key by zooming in or out, respectively
   * 
   * @param evt
   */
  @Override
  public void keyPressed(KeyEvent evt)
  {
    int keyCode = evt.getKeyCode();
    boolean shiftDown = evt.isShiftDown();

    if (keyCode == KeyEvent.VK_UP)
    {
      if (shiftDown)
      {
        rotate(0f, -1f);
      }
      else
      {
        zoom(ZOOM_IN);
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
        zoom(ZOOM_OUT);
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
      // Cache.warn("DEBUG: Rectangle selection");
      // todo not yet enabled as rectx2, recty2 are always -1
      // need to set them in mouseDragged; JAL-1124
      // if ((rectx2 != -1) && (recty2 != -1))
      // {
      // rectSelect(rectx1, recty1, rectx2, recty2);
      // }
    }

    repaint();
  }

  @Override
  public void zoom(float factor)
  {
    if (factor > 0f)
    {
      setScaleFactor(getScaleFactor() * factor);
    }
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

  /**
   * If the mouse press is at (within 2 pixels of) a sequence point, toggles
   * (adds or removes) the corresponding sequence as a member of the viewport
   * selection group. This supports configuring a group in the alignment by
   * clicking on points in the PCA display.
   */
  @Override
  public void mousePressed(MouseEvent evt)
  {
    int x = evt.getX();
    int y = evt.getY();

    mouseX = x;
    mouseY = y;

    // rectx1 = x;
    // recty1 = y;
    // rectx2 = -1;
    // recty2 = -1;

    SequenceI found = findSequenceAtPoint(x, y);

    if (found != null)
    {
      AlignmentPanel[] aps = getAssociatedPanels();

      for (int a = 0; a < aps.length; a++)
      {
        if (aps[a].av.getSelectionGroup() != null)
        {
          aps[a].av.getSelectionGroup().addOrRemove(found, true);
        }
        else
        {
          aps[a].av.setSelectionGroup(new SequenceGroup());
          aps[a].av.getSelectionGroup().addOrRemove(found, true);
          aps[a].av.getSelectionGroup()
                  .setEndRes(aps[a].av.getAlignment().getWidth() - 1);
        }
      }
      PaintRefresher.Refresh(this, av.getSequenceSetId());
      // canonical selection is sent to other listeners
      av.sendSelection();
    }

    repaint();
  }

  /**
   * Sets the tooltip to the name of the sequence within 2 pixels of the mouse
   * position, or clears the tooltip if none found
   */
  @Override
  public void mouseMoved(MouseEvent evt)
  {
    SequenceI found = findSequenceAtPoint(evt.getX(), evt.getY());

    this.setToolTipText(found == null ? null : found.getName());
  }

  /**
   * Action handler for a mouse drag. Rotates the display around the X axis (for
   * up/down mouse movement) and/or the Y axis (for left/right mouse movement).
   * 
   * @param evt
   */
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

    // Check if this is a rectangle drawing drag
    if ((evt.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0)
    {
      // rectx2 = evt.getX();
      // recty2 = evt.getY();
    }
    else
    {
      rotate(xDelta, yDelta);

      mouseX = xPos;
      mouseY = yPos;

      // findWidths();

      repaint();
    }
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
     * apply the composite transformation to sequence points;
     * update z min-max range (affects colour graduation), but not
     * x or y min-max (as this would affect axis scaling)
     */
    float[] centre = getCentre();
    float zMin = Float.MAX_VALUE;
    float zMax = -Float.MAX_VALUE;

    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = sequencePoints.get(i);
      sp.translate(-centre[0], -centre[1], -centre[2]);

      // Now apply the rotation matrix
      sp.coord = rotmat.vectorMultiply(sp.coord);

      // Now translate back again
      sp.translate(centre[0], centre[1], centre[2]);

      zMin = Math.min(zMin, sp.coord.z);
      zMax = Math.max(zMax, sp.coord.z);
    }

    seqMin[2] = zMin;
    seqMax[2] = zMax;

    /*
     * rotate the x/y/z axis positions
     */
    for (int i = 0; i < DIMS; i++)
    {
      getAxisEndPoints()[i] = rotmat.vectorMultiply(getAxisEndPoints()[i]);
    }
  }

  /**
   * Answers the x/y/z coordinates that are midway between the maximum and
   * minimum sequence point values
   * 
   * @return
   */
  private float[] getCentre()
  {
    float xCentre = (seqMin[0] + seqMax[0]) / 2f;
    float yCentre = (seqMin[1] + seqMax[1]) / 2f;
    float zCentre = (seqMin[2] + seqMax[2]) / 2f;

    return new float[] { xCentre, yCentre, zCentre };
  }

  /**
   * Adds any sequences whose displayed points are within the given rectangle to
   * the viewport's current selection. Intended for key 's' after dragging to
   * select a region of the PCA.
   * 
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   */
  protected void rectSelect(int x1, int y1, int x2, int y2)
  {
    float[] centre = getCentre();

    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = sequencePoints.get(i);
      int tmp1 = (int) (((sp.coord.x - centre[0]) * getScaleFactor())
              + (getWidth() / 2.0));
      int tmp2 = (int) (((sp.coord.y - centre[1]) * getScaleFactor())
              + (getHeight() / 2.0));

      if ((tmp1 > x1) && (tmp1 < x2) && (tmp2 > y1) && (tmp2 < y2))
      {
        if (av != null)
        {
          SequenceI sequence = sp.getSequence();
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
  protected SequenceI findSequenceAtPoint(int x, int y)
  {
    int halfwidth = getWidth() / 2;
    int halfheight = getHeight() / 2;

    int found = -1;
    int pix = Math.min(getWidth(), getHeight());
    float xWidth = Math.abs(seqMax[0] - seqMin[0]);
    float yWidth = Math.abs(seqMax[1] - seqMin[1]);
    float maxWidth = Math.max(xWidth, yWidth);
    float scaleBy = pix * getScaleFactor() / (2f * maxWidth);

    float[] centre = getCentre();

    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = sequencePoints.get(i);
      int px = (int) ((sp.coord.x - centre[0]) * scaleBy) + halfwidth;
      int py = (int) ((sp.coord.y - centre[1]) * scaleBy) + halfheight;

      if ((Math.abs(px - x) < NEARBY) && (Math.abs(py - y) < NEARBY))
      {
        found = i;
        break;
      }
    }

    if (found != -1)
    {
      return sequencePoints.get(found).getSequence();
    }
    else
    {
      return null;
    }
  }

  /**
   * Answers the panel the PCA is associated with (all panels for this alignment
   * if 'associate with all panels' is selected).
   * 
   * @return
   */
  AlignmentPanel[] getAssociatedPanels()
  {
    if (isApplyToAllViews())
    {
      return PaintRefresher.getAssociatedPanels(av.getSequenceSetId());
    }
    else
    {
      return new AlignmentPanel[] { ap };
    }
  }

  public Color getBackgroundColour()
  {
    return getBgColour();
  }

  /**
   * Zooms in or out in response to mouse wheel movement
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    double wheelRotation = e.getPreciseWheelRotation();
    if (wheelRotation > 0)
    {
      zoom(ZOOM_IN);
      repaint();
    }
    else if (wheelRotation < 0)
    {
      zoom(ZOOM_OUT);
      repaint();
    }
  }

  /**
   * Answers the sequence point minimum [x, y, z] values. Note these are derived
   * when sequence points are set, but x and y values are not updated on
   * rotation (because this would result in changes to scaling).
   * 
   * @return
   */
  public float[] getSeqMin()
  {
    return seqMin;
  }

  /**
   * Answers the sequence point maximum [x, y, z] values. Note these are derived
   * when sequence points are set, but x and y values are not updated on
   * rotation (because this would result in changes to scaling).
   * 
   * @return
   */
  public float[] getSeqMax()
  {
    return seqMax;
  }

  /**
   * Sets the minimum and maximum [x, y, z] positions for sequence points. For
   * use when restoring a saved PCA from state data.
   * 
   * @param min
   * @param max
   */
  public void setSeqMinMax(float[] min, float[] max)
  {
    seqMin = min;
    seqMax = max;
  }

  public float getScaleFactor()
  {
    return scaleFactor;
  }

  public void setScaleFactor(float scaleFactor)
  {
    this.scaleFactor = scaleFactor;
  }

  public boolean isShowLabels()
  {
    return showLabels;
  }

  public void setShowLabels(boolean showLabels)
  {
    this.showLabels = showLabels;
  }

  public boolean isApplyToAllViews()
  {
    return applyToAllViews;
  }

  public void setApplyToAllViews(boolean applyToAllViews)
  {
    this.applyToAllViews = applyToAllViews;
  }

  public Point[] getAxisEndPoints()
  {
    return axisEndPoints;
  }

  public void setAxisEndPoints(Point[] axisEndPoints)
  {
    this.axisEndPoints = axisEndPoints;
  }

  public Color getBgColour()
  {
    return bgColour;
  }

  public void setBgColour(Color bgColour)
  {
    this.bgColour = bgColour;
  }
}
