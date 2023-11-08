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
package jalview.renderer;

import jalview.api.AlignmentColsCollectionI;
import jalview.api.AlignmentRowsCollectionI;
import jalview.api.RendererListenerI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.seqfeatures.FeatureColourFinder;
import jalview.renderer.seqfeatures.FeatureRenderer;
import jalview.viewmodel.OverviewDimensions;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;

public class OverviewRenderer
{
  // transparency of hidden cols/seqs overlay
  private final float TRANSPARENCY = 0.5f;

  public static final String UPDATE = "OverviewUpdate";

  private static final int MAX_PROGRESS = 100;

  private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
          this);

  private FeatureColourFinder finder;

  // image to render on
  private BufferedImage miniMe;

  // raw number of pixels to allocate to each column
  private float pixelsPerCol;

  // raw number of pixels to allocate to each row
  private float pixelsPerSeq;

  // height in pixels of graph
  private int graphHeight;

  // flag to indicate whether to halt drawing
  private volatile boolean redraw = false;

  // reference to alignment, needed to get sequence groups
  private AlignmentI al;

  private ResidueShaderI shader;

  private OverviewResColourFinder resColFinder;

  public OverviewRenderer(FeatureRenderer fr, OverviewDimensions od,
          AlignmentI alignment, ResidueShaderI resshader,
          OverviewResColourFinder colFinder)
  {
    finder = new FeatureColourFinder(fr);
    resColFinder = colFinder;

    al = alignment;
    shader = resshader;

    pixelsPerCol = od.getPixelsPerCol();
    pixelsPerSeq = od.getPixelsPerSeq();
    graphHeight = od.getGraphHeight();
    miniMe = new BufferedImage(od.getWidth(), od.getHeight(),
            BufferedImage.TYPE_INT_RGB);
  }

  /**
   * Draw alignment rows and columns onto an image
   * 
   * @param rit
   *          Iterator over rows to be drawn
   * @param cit
   *          Iterator over columns to be drawn
   * @return image containing the drawing
   */
  public BufferedImage draw(AlignmentRowsCollectionI rows,
          AlignmentColsCollectionI cols)
  {
    int rgbcolor = Color.white.getRGB();
    int seqIndex = 0;
    int pixelRow = 0;
    int alignmentHeight = miniMe.getHeight() - graphHeight;
    int totalPixels = miniMe.getWidth() * alignmentHeight;

    int lastRowUpdate = 0;
    int lastUpdate = 0;
    changeSupport.firePropertyChange(UPDATE, -1, 0);

    for (int alignmentRow : rows)
    {
      if (redraw)
      {
        break;
      }

      // get details of this alignment row
      SequenceI seq = rows.getSequence(alignmentRow);

      // rate limiting step when rendering overview for lots of groups
      SequenceGroup[] allGroups = al.findAllGroups(seq);

      // calculate where this row extends to in pixels
      int endRow = Math.min(Math.round((seqIndex + 1) * pixelsPerSeq) - 1,
              miniMe.getHeight() - 1);

      int colIndex = 0;
      int pixelCol = 0;
      for (int alignmentCol : cols)
      {
        if (redraw)
        {
          break;
        }

        // calculate where this column extends to in pixels
        int endCol = Math.min(Math.round((colIndex + 1) * pixelsPerCol) - 1,
                miniMe.getWidth() - 1);

        // don't do expensive colour determination if we're not going to use it
        // NB this is important to avoid performance issues in the overview
        // panel
        if (pixelCol <= endCol)
        {
          rgbcolor = getColumnColourFromSequence(allGroups, seq,
                  alignmentCol);

          // fill in the appropriate number of pixels
          for (int row = pixelRow; row <= endRow; ++row)
          {
            for (int col = pixelCol; col <= endCol; ++col)
            {
              miniMe.setRGB(col, row, rgbcolor);
            }
          }

          // store last update value
          lastUpdate = sendProgressUpdate(
                  (pixelCol + 1) * (endRow - pixelRow), totalPixels,
                  lastRowUpdate, lastUpdate);

          pixelCol = endCol + 1;
        }
        colIndex++;
      }

      if (pixelRow != endRow + 1)
      {
        // store row offset and last update value
        lastRowUpdate = sendProgressUpdate(endRow + 1, alignmentHeight, 0,
                lastUpdate);
        lastUpdate = lastRowUpdate;
        pixelRow = endRow + 1;
      }
      seqIndex++;
    }

    overlayHiddenRegions(rows, cols);
    // final update to progress bar if present
    if (redraw)
    {
      sendProgressUpdate(pixelRow - 1, alignmentHeight, 0, 0);
    }
    else
    {
      sendProgressUpdate(alignmentHeight, miniMe.getHeight(), 0, 0);
    }
    return miniMe;
  }

  /*
   * Calculate progress update value and fire event
   * @param rowOffset number of rows to offset calculation by
   * @return new rowOffset - return value only to be used when at end of a row
   */
  private int sendProgressUpdate(int position, int maximum, int rowOffset,
          int lastUpdate)
  {
    int newUpdate = rowOffset
            + Math.round(MAX_PROGRESS * ((float) position / maximum));
    if (newUpdate > lastUpdate)
    {
      changeSupport.firePropertyChange(UPDATE, rowOffset, newUpdate);
      return newUpdate;
    }
    return newUpdate;
  }

  /*
   * Find the RGB value of the colour of a sequence at a specified column position
   * 
   * @param seq
   *          sequence to get colour for
   * @param lastcol
   *          column position to get colour for
   * @return colour of sequence at this position, as RGB
   */
  int getColumnColourFromSequence(SequenceGroup[] allGroups, SequenceI seq,
          int lastcol)
  {
    Color color = resColFinder.gapColour;

    if ((seq != null) && (seq.getLength() > lastcol))
    {
      color = resColFinder.getResidueColour(true, shader, allGroups, seq,
              lastcol, finder);
    }

    return color.getRGB();
  }

  /**
   * Overlay the hidden regions on the overview image
   * 
   * @param rows
   *          collection of rows the overview is built over
   * @param cols
   *          collection of columns the overview is built over
   */
  private void overlayHiddenRegions(AlignmentRowsCollectionI rows,
          AlignmentColsCollectionI cols)
  {
    if (cols.hasHidden() || rows.hasHidden())
    {
      BufferedImage mask = buildHiddenImage(rows, cols, miniMe.getWidth(),
              miniMe.getHeight());

      Graphics2D g = (Graphics2D) miniMe.getGraphics();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
              TRANSPARENCY));
      g.drawImage(mask, 0, 0, miniMe.getWidth(), miniMe.getHeight(), null);
    }
  }

  /**
   * Build a masking image of hidden columns and rows to be applied on top of
   * the main overview image.
   * 
   * @param rows
   *          collection of rows the overview is built over
   * @param cols
   *          collection of columns the overview is built over
   * @param width
   *          width of overview in pixels
   * @param height
   *          height of overview in pixels
   * @return BufferedImage containing mask of hidden regions
   */
  private BufferedImage buildHiddenImage(AlignmentRowsCollectionI rows,
          AlignmentColsCollectionI cols, int width, int height)
  {
    // new masking image
    BufferedImage hiddenImage = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_ARGB);

    int colIndex = 0;
    int pixelCol = 0;

    Color hidden = resColFinder.getHiddenColour();

    Graphics2D g2d = (Graphics2D) hiddenImage.getGraphics();

    // set background to transparent
    // g2d.setComposite(AlphaComposite.Clear);
    // g2d.fillRect(0, 0, width, height);

    // set next colour to opaque
    g2d.setComposite(AlphaComposite.Src);

    for (int alignmentCol : cols)
    {
      if (redraw)
      {
        break;
      }

      // calculate where this column extends to in pixels
      int endCol = Math.min(Math.round((colIndex + 1) * pixelsPerCol) - 1,
              hiddenImage.getWidth() - 1);

      if (pixelCol <= endCol)
      {
        // determine the colour based on the sequence and column position
        if (cols.isHidden(alignmentCol))
        {
          g2d.setColor(hidden);
          g2d.fillRect(pixelCol, 0, endCol - pixelCol + 1, height);
        }

        pixelCol = endCol + 1;
      }
      colIndex++;

    }

    int seqIndex = 0;
    int pixelRow = 0;
    for (int alignmentRow : rows)
    {
      if (redraw)
      {
        break;
      }

      // calculate where this row extends to in pixels
      int endRow = Math.min(Math.round((seqIndex + 1) * pixelsPerSeq) - 1,
              miniMe.getHeight() - 1);

      // get details of this alignment row
      if (rows.isHidden(alignmentRow))
      {
        g2d.setColor(hidden);
        g2d.fillRect(0, pixelRow, width, endRow - pixelRow + 1);
      }
      pixelRow = endRow + 1;
      seqIndex++;
    }

    return hiddenImage;
  }

  /**
   * Draw the alignment annotation in the overview panel
   * 
   * @param g
   *          the graphics object to draw on
   * @param anno
   *          alignment annotation information
   * @param y
   *          y-position for the annotation graph
   * @param cols
   *          the collection of columns used in the overview panel
   */
  public void drawGraph(Graphics g, AlignmentAnnotation anno, int y,
          AlignmentColsCollectionI cols)
  {
    Annotation[] annotations = anno.annotations;
    g.setColor(Color.white);
    g.fillRect(0, 0, miniMe.getWidth(), y);

    int height;
    int colIndex = 0;
    int pixelCol = 0;
    for (int alignmentCol : cols)
    {
      if (redraw)
      {
        changeSupport.firePropertyChange(UPDATE, MAX_PROGRESS - 1, 0);
        break;
      }

      if (alignmentCol >= annotations.length)
      {
        break; // no more annotations to draw here
      }
      else
      {
        int endCol = Math.min(Math.round((colIndex + 1) * pixelsPerCol) - 1,
                miniMe.getWidth() - 1);

        if (annotations[alignmentCol] != null)
        {
          if (annotations[alignmentCol].colour == null)
          {
            g.setColor(Color.black);
          }
          else
          {
            g.setColor(annotations[alignmentCol].colour);
          }

          height = (int) ((annotations[alignmentCol].value / anno.graphMax)
                  * y);
          if (height > y)
          {
            height = y;
          }

          g.fillRect(pixelCol, y - height, endCol - pixelCol + 1, height);
        }

        pixelCol = endCol + 1;
        colIndex++;
      }
    }
    changeSupport.firePropertyChange(UPDATE, MAX_PROGRESS - 1,
            MAX_PROGRESS);
  }

  /**
   * Allows redraw flag to be set
   * 
   * @param b
   *          value to set redraw to: true = redraw is occurring, false = no
   *          redraw
   */
  public void setRedraw(boolean b)
  {
    synchronized (this)
    {
      redraw = b;
    }
  }

  public void addPropertyChangeListener(RendererListenerI listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(RendererListenerI listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }
}
