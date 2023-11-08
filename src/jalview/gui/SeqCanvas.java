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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.VisibleContigsIterator;
import jalview.renderer.ScaleRenderer;
import jalview.renderer.ScaleRenderer.ScaleMark;
import jalview.util.Comparison;
import jalview.viewmodel.ViewportListenerI;
import jalview.viewmodel.ViewportRanges;

/**
 * The Swing component on which the alignment sequences, and annotations (if
 * shown), are drawn. This includes scales above, left and right (if shown) in
 * Wrapped mode, but not the scale above in Unwrapped mode.
 * 
 */
@SuppressWarnings("serial")
public class SeqCanvas extends JPanel implements ViewportListenerI
{
  /**
   * vertical gap in pixels between sequences and annotations when in wrapped
   * mode
   */
  static final int SEQS_ANNOTATION_GAP = 3;

  private static final String ZEROS = "0000000000";

  final FeatureRenderer fr;

  BufferedImage img;

  AlignViewport av;

  int cursorX = 0;

  int cursorY = 0;

  private final SequenceRenderer seqRdr;

  boolean fastPaint = false;

  private boolean fastpainting = false;

  private AnnotationPanel annotations;

  /*
   * measurements for drawing a wrapped alignment
   */
  private int labelWidthEast; // label right width in pixels if shown

  private int labelWidthWest; // label left width in pixels if shown

  int wrappedSpaceAboveAlignment; // gap between widths

  int wrappedRepeatHeightPx; // height in pixels of wrapped width

  private int wrappedVisibleWidths; // number of wrapped widths displayed

  // Don't do this! Graphics handles are supposed to be transient
  // private Graphics2D gg;

  /**
   * Creates a new SeqCanvas object.
   * 
   * @param ap
   */
  public SeqCanvas(AlignmentPanel ap)
  {
    this.av = ap.av;
    fr = new FeatureRenderer(ap);
    seqRdr = new SequenceRenderer(av);
    setLayout(new BorderLayout());
    PaintRefresher.Register(this, av.getSequenceSetId());
    setBackground(Color.white);

    av.getRanges().addPropertyChangeListener(this);
  }

  public SequenceRenderer getSequenceRenderer()
  {
    return seqRdr;
  }

  public FeatureRenderer getFeatureRenderer()
  {
    return fr;
  }

  /**
   * Draws the scale above a region of a wrapped alignment, consisting of a
   * column number every major interval (10 columns).
   * 
   * @param g
   *          the graphics context to draw on, positioned at the start (bottom
   *          left) of the line on which to draw any scale marks
   * @param startx
   *          start alignment column (0..)
   * @param endx
   *          end alignment column (0..)
   * @param ypos
   *          y offset to draw at
   */
  private void drawNorthScale(Graphics g, int startx, int endx, int ypos)
  {
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();

    /*
     * white fill the scale space (for the fastPaint case)
     */
    g.setColor(Color.white);
    g.fillRect(0, ypos - charHeight - charHeight / 2, getWidth(),
            charHeight * 3 / 2 + 2);
    g.setColor(Color.black);

    List<ScaleMark> marks = new ScaleRenderer().calculateMarks(av, startx,
            endx);
    for (ScaleMark mark : marks)
    {
      int mpos = mark.column; // (i - startx - 1)
      if (mpos < 0)
      {
        continue;
      }
      String mstring = mark.text;

      if (mark.major)
      {
        if (mstring != null)
        {
          g.drawString(mstring, mpos * charWidth, ypos - (charHeight / 2));
        }

        /*
         * draw a tick mark below the column number, centred on the column;
         * height of tick mark is 4 pixels less than half a character
         */
        int xpos = (mpos * charWidth) + (charWidth / 2);
        g.drawLine(xpos, (ypos + 2) - (charHeight / 2), xpos, ypos - 2);
      }
    }
  }

  /**
   * Draw the scale to the left or right of a wrapped alignment
   * 
   * @param g
   *          graphics context, positioned at the start of the scale to be drawn
   * @param startx
   *          first column of wrapped width (0.. excluding any hidden columns)
   * @param endx
   *          last column of wrapped width (0.. excluding any hidden columns)
   * @param ypos
   *          vertical offset at which to begin the scale
   * @param left
   *          if true, scale is left of residues, if false, scale is right
   */
  void drawVerticalScale(Graphics g, final int startx, final int endx,
          final int ypos, final boolean left)
  {
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();

    int yPos = ypos + charHeight;
    int startX = startx;
    int endX = endx;

    if (av.hasHiddenColumns())
    {
      HiddenColumns hiddenColumns = av.getAlignment().getHiddenColumns();
      startX = hiddenColumns.visibleToAbsoluteColumn(startx);
      endX = hiddenColumns.visibleToAbsoluteColumn(endx);
    }
    FontMetrics fm = getFontMetrics(av.getFont());

    for (int i = 0; i < av.getAlignment().getHeight(); i++)
    {
      SequenceI seq = av.getAlignment().getSequenceAt(i);

      /*
       * find sequence position of first non-gapped position -
       * to the right if scale left, to the left if scale right
       */
      int index = left ? startX : endX;
      int value = -1;
      while (index >= startX && index <= endX)
      {
        if (!Comparison.isGap(seq.getCharAt(index)))
        {
          value = seq.findPosition(index);
          break;
        }
        if (left)
        {
          index++;
        }
        else
        {
          index--;
        }
      }

      /*
       * white fill the space for the scale
       */
      g.setColor(Color.white);
      int y = (yPos + (i * charHeight)) - (charHeight / 5);
      // fillRect origin is top left of rectangle
      g.fillRect(0, y - charHeight, left ? labelWidthWest : labelWidthEast,
              charHeight + 1);

      if (value != -1)
      {
        /*
         * draw scale value, right justified within its width less half a
         * character width padding on the right
         */
        int labelSpace = left ? labelWidthWest : labelWidthEast;
        labelSpace -= charWidth / 2; // leave space to the right
        String valueAsString = String.valueOf(value);
        int labelLength = fm.stringWidth(valueAsString);
        int xOffset = labelSpace - labelLength;
        g.setColor(Color.black);
        g.drawString(valueAsString, xOffset, y);
      }
    }

  }

  /**
   * Does a fast paint of an alignment in response to a scroll. Most of the
   * visible region is simply copied and shifted, and then any newly visible
   * columns or rows are drawn. The scroll may be horizontal or vertical, but
   * not both at once. Scrolling may be the result of
   * <ul>
   * <li>dragging a scroll bar</li>
   * <li>clicking in the scroll bar</li>
   * <li>scrolling by trackpad, middle mouse button, or other device</li>
   * <li>by moving the box in the Overview window</li>
   * <li>programmatically to make a highlighted position visible</li>
   * <li>pasting a block of sequences</li>
   * </ul>
   * 
   * @param horizontal
   *          columns to shift right (positive) or left (negative)
   * @param vertical
   *          rows to shift down (positive) or up (negative)
   */
  public void fastPaint(int horizontal, int vertical)
  {

    // effectively:
    // if (horizontal != 0 && vertical != 0)
    // throw new InvalidArgumentException();
    if (fastpainting || img == null)
    {
      return;
    }
    fastpainting = true;
    fastPaint = true;
    try
    {
      int charHeight = av.getCharHeight();
      int charWidth = av.getCharWidth();

      ViewportRanges ranges = av.getRanges();
      int startRes = ranges.getStartRes();
      int endRes = ranges.getEndRes();
      int startSeq = ranges.getStartSeq();
      int endSeq = ranges.getEndSeq();
      int transX = 0;
      int transY = 0;

      if (horizontal > 0) // scrollbar pulled right, image to the left
      {
        transX = (endRes - startRes - horizontal) * charWidth;
        startRes = endRes - horizontal;
      }
      else if (horizontal < 0)
      {
        endRes = startRes - horizontal;
      }

      if (vertical > 0) // scroll down
      {
        startSeq = endSeq - vertical;

        if (startSeq < ranges.getStartSeq())
        { // ie scrolling too fast, more than a page at a time
          startSeq = ranges.getStartSeq();
        }
        else
        {
          transY = img.getHeight() - ((vertical + 1) * charHeight);
        }
      }
      else if (vertical < 0)
      {
        endSeq = startSeq - vertical;

        if (endSeq > ranges.getEndSeq())
        {
          endSeq = ranges.getEndSeq();
        }
      }

      // System.err.println(">>> FastPaint to " + transX + " " + transY + " "
      // + horizontal + " " + vertical + " " + startRes + " " + endRes
      // + " " + startSeq + " " + endSeq);

      Graphics gg = img.getGraphics();
      gg.copyArea(horizontal * charWidth, vertical * charHeight,
              img.getWidth(), img.getHeight(), -horizontal * charWidth,
              -vertical * charHeight);

      /** @j2sNative xxi = this.img */

      gg.translate(transX, transY);
      drawPanel(gg, startRes, endRes, startSeq, endSeq, 0);
      gg.translate(-transX, -transY);
      gg.dispose();

      // Call repaint on alignment panel so that repaints from other alignment
      // panel components can be aggregated. Otherwise performance of the
      // overview window and others may be adversely affected.
      // System.out.println("SeqCanvas fastPaint() repaint() request...");
      av.getAlignPanel().repaint();
    } finally
    {
      fastpainting = false;
    }
  }

  @Override
  public void paintComponent(Graphics g)
  {

    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();

    int width = getWidth();
    int height = getHeight();

    width -= (width % charWidth);
    height -= (height % charHeight);

    // BH 2019 can't possibly fastPaint if either width or height is 0

    if (width == 0 || height == 0)
    {
      return;
    }

    ViewportRanges ranges = av.getRanges();
    int startRes = ranges.getStartRes();
    int startSeq = ranges.getStartSeq();
    int endRes = ranges.getEndRes();
    int endSeq = ranges.getEndSeq();

    // [JAL-3226] problem that JavaScript (or Java) may consolidate multiple
    // repaint() requests in unpredictable ways. In this case, the issue was
    // that in response to a CTRL-C/CTRL-V paste request, in Java a fast
    // repaint request preceded two full requests, thus resulting
    // in a full request for paint. In constrast, in JavaScript, the three
    // requests were bundled together into one, so the fastPaint flag was
    // still present for the second and third request.
    //
    // This resulted in incomplete painting.
    //
    // The solution was to set seqCanvas.fastPaint and idCanvas.fastPaint false
    // in PaintRefresher when the target to be painted is one of those two
    // components.
    //
    // BH 2019.04.22
    //
    // An initial idea; can be removed once we determine this issue is closed:
    // if (av.isFastPaintDisabled())
    // {
    // fastPaint = false;
    // }

    Rectangle vis, clip;
    if (img != null
            && (fastPaint
                    || (vis = getVisibleRect()).width != (clip = g
                            .getClipBounds()).width
                    || vis.height != clip.height))
    {
      g.drawImage(img, 0, 0, this);
      drawSelectionGroup((Graphics2D) g, startRes, endRes, startSeq,
              endSeq);
      fastPaint = false;
    }
    else
    {
      // img is a cached version of the last view we drew.
      // If we have no img or the size has changed, make a new one.
      //
      if (img == null || width != img.getWidth()
              || height != img.getHeight())
      {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      }

      Graphics2D gg = (Graphics2D) img.getGraphics();
      gg.setFont(av.getFont());

      if (av.antiAlias)
      {
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
      }

      gg.setColor(Color.white);
      gg.fillRect(0, 0, img.getWidth(), img.getHeight());

      if (av.getWrapAlignment())
      {
        drawWrappedPanel(gg, getWidth(), getHeight(), ranges.getStartRes());
      }
      else
      {
        drawPanel(gg, startRes, endRes, startSeq, endSeq, 0);
      }

      drawSelectionGroup(gg, startRes, endRes, startSeq, endSeq);

      g.drawImage(img, 0, 0, this);
      gg.dispose();
    }

    if (av.cursorMode)
    {
      drawCursor(g, startRes, endRes, startSeq, endSeq);
    }
  }

  /**
   * Draw an alignment panel for printing
   * 
   * @param g1
   *          Graphics object to draw with
   * @param startRes
   *          start residue of print area
   * @param endRes
   *          end residue of print area
   * @param startSeq
   *          start sequence of print area
   * @param endSeq
   *          end sequence of print area
   */
  public void drawPanelForPrinting(Graphics g1, int startRes, int endRes,
          int startSeq, int endSeq)
  {
    drawPanel(g1, startRes, endRes, startSeq, endSeq, 0);

    drawSelectionGroup((Graphics2D) g1, startRes, endRes, startSeq, endSeq);
  }

  /**
   * Draw a wrapped alignment panel for printing
   * 
   * @param g
   *          Graphics object to draw with
   * @param canvasWidth
   *          width of drawing area
   * @param canvasHeight
   *          height of drawing area
   * @param startRes
   *          start residue of print area
   */
  public void drawWrappedPanelForPrinting(Graphics g, int canvasWidth,
          int canvasHeight, int startRes)
  {
    drawWrappedPanel(g, canvasWidth, canvasHeight, startRes);

    SequenceGroup group = av.getSelectionGroup();
    if (group != null)
    {
      drawWrappedSelection((Graphics2D) g, group, canvasWidth, canvasHeight,
              startRes);
    }
  }

  /**
   * Returns the visible width of the canvas in residues, after allowing for
   * East or West scales (if shown)
   * 
   * @param canvasWidth
   *          the width in pixels (possibly including scales)
   * 
   * @return
   */
  public int getWrappedCanvasWidth(int canvasWidth)
  {
    int charWidth = av.getCharWidth();

    FontMetrics fm = getFontMetrics(av.getFont());

    int labelWidth = 0;

    if (av.getScaleRightWrapped() || av.getScaleLeftWrapped())
    {
      labelWidth = getLabelWidth(fm);
    }

    labelWidthEast = av.getScaleRightWrapped() ? labelWidth : 0;

    labelWidthWest = av.getScaleLeftWrapped() ? labelWidth : 0;

    return (canvasWidth - labelWidthEast - labelWidthWest) / charWidth;
  }

  /**
   * Returns a pixel width sufficient to show the largest sequence coordinate
   * (end position) in the alignment, calculated as the FontMetrics width of
   * zeroes "0000000" limited to the number of decimal digits to be shown (3 for
   * 1-10, 4 for 11-99 etc). One character width is added to this, to allow for
   * half a character width space on either side.
   * 
   * @param fm
   * @return
   */
  protected int getLabelWidth(FontMetrics fm)
  {
    /*
     * find the biggest sequence end position we need to show
     * (note this is not necessarily the sequence length)
     */
    int maxWidth = 0;
    AlignmentI alignment = av.getAlignment();
    for (int i = 0; i < alignment.getHeight(); i++)
    {
      maxWidth = Math.max(maxWidth, alignment.getSequenceAt(i).getEnd());
    }

    int length = 0;
    for (int i = maxWidth; i > 0; i /= 10)
    {
      length++;
    }

    return fm.stringWidth(ZEROS.substring(0, length)) + av.getCharWidth();
  }

  /**
   * Draws as many widths of a wrapped alignment as can fit in the visible
   * window
   * 
   * @param g
   * @param canvasWidth
   *          available width in pixels
   * @param canvasHeight
   *          available height in pixels
   * @param startColumn
   *          the first column (0...) of the alignment to draw
   */
  public void drawWrappedPanel(Graphics g, int canvasWidth,
          int canvasHeight, final int startColumn)
  {
    int wrappedWidthInResidues = calculateWrappedGeometry(canvasWidth,
            canvasHeight);

    av.setWrappedWidth(wrappedWidthInResidues);

    ViewportRanges ranges = av.getRanges();
    ranges.setViewportStartAndWidth(startColumn, wrappedWidthInResidues);

    // we need to call this again to make sure the startColumn +
    // wrappedWidthInResidues values are used to calculate wrappedVisibleWidths
    // correctly.
    calculateWrappedGeometry(canvasWidth, canvasHeight);

    /*
     * draw one width at a time (excluding any scales shown),
     * until we have run out of either alignment or vertical space available
     */
    int ypos = wrappedSpaceAboveAlignment;
    int maxWidth = ranges.getVisibleAlignmentWidth();

    int start = startColumn;
    int currentWidth = 0;
    while ((currentWidth < wrappedVisibleWidths) && (start < maxWidth))
    {
      int endColumn = Math.min(maxWidth,
              start + wrappedWidthInResidues - 1);
      drawWrappedWidth(g, ypos, start, endColumn, canvasHeight);
      ypos += wrappedRepeatHeightPx;
      start += wrappedWidthInResidues;
      currentWidth++;
    }

    drawWrappedDecorators(g, startColumn);
  }

  /**
   * Calculates and saves values needed when rendering a wrapped alignment.
   * These depend on many factors, including
   * <ul>
   * <li>canvas width and height</li>
   * <li>number of visible sequences, and height of annotations if shown</li>
   * <li>font and character width</li>
   * <li>whether scales are shown left, right or above the alignment</li>
   * </ul>
   * 
   * @param canvasWidth
   * @param canvasHeight
   * @return the number of residue columns in each width
   */
  protected int calculateWrappedGeometry(int canvasWidth, int canvasHeight)
  {
    int charHeight = av.getCharHeight();

    /*
     * vertical space in pixels between wrapped widths of alignment
     * - one character height, or two if scale above is drawn
     */
    wrappedSpaceAboveAlignment = charHeight
            * (av.getScaleAboveWrapped() ? 2 : 1);

    /*
     * compute height in pixels of the wrapped widths
     * - start with space above plus sequences
     */
    wrappedRepeatHeightPx = wrappedSpaceAboveAlignment;
    wrappedRepeatHeightPx += av.getAlignment().getHeight() * charHeight;

    /*
     * add annotations panel height if shown
     * also gap between sequences and annotations
     */
    if (av.isShowAnnotation())
    {
      wrappedRepeatHeightPx += getAnnotationHeight();
      wrappedRepeatHeightPx += SEQS_ANNOTATION_GAP; // 3px
    }

    /*
     * number of visible widths (the last one may be part height),
     * ensuring a part height includes at least one sequence
     */
    ViewportRanges ranges = av.getRanges();
    wrappedVisibleWidths = canvasHeight / wrappedRepeatHeightPx;
    int remainder = canvasHeight % wrappedRepeatHeightPx;
    if (remainder >= (wrappedSpaceAboveAlignment + charHeight))
    {
      wrappedVisibleWidths++;
    }

    /*
     * compute width in residues; this also sets East and West label widths
     */
    int wrappedWidthInResidues = getWrappedCanvasWidth(canvasWidth);
    av.setWrappedWidth(wrappedWidthInResidues); // update model accordingly
    /*
     *  limit visibleWidths to not exceed width of alignment
     */
    int xMax = ranges.getVisibleAlignmentWidth();
    int startToEnd = xMax - ranges.getStartRes();
    int maxWidths = startToEnd / wrappedWidthInResidues;
    if (startToEnd % wrappedWidthInResidues > 0)
    {
      maxWidths++;
    }
    wrappedVisibleWidths = Math.min(wrappedVisibleWidths, maxWidths);

    return wrappedWidthInResidues;
  }

  /**
   * Draws one width of a wrapped alignment, including sequences and
   * annnotations, if shown, but not scales or hidden column markers
   * 
   * @param g
   * @param ypos
   * @param startColumn
   * @param endColumn
   * @param canvasHeight
   */
  protected void drawWrappedWidth(Graphics g, final int ypos,
          final int startColumn, final int endColumn,
          final int canvasHeight)
  {
    ViewportRanges ranges = av.getRanges();
    int viewportWidth = ranges.getViewportWidth();

    int endx = Math.min(startColumn + viewportWidth - 1, endColumn);

    /*
     * move right before drawing by the width of the scale left (if any)
     * plus column offset from left margin (usually zero, but may be non-zero
     * when fast painting is drawing just a few columns)
     */
    int charWidth = av.getCharWidth();
    int xOffset = labelWidthWest
            + ((startColumn - ranges.getStartRes()) % viewportWidth)
                    * charWidth;

    g.translate(xOffset, 0);

    /*
     * white fill the region to be drawn (so incremental fast paint doesn't
     * scribble over an existing image)
     */
    g.setColor(Color.white);
    g.fillRect(0, ypos, (endx - startColumn + 1) * charWidth,
            wrappedRepeatHeightPx);

    drawPanel(g, startColumn, endx, 0, av.getAlignment().getHeight() - 1,
            ypos);

    int cHeight = av.getAlignment().getHeight() * av.getCharHeight();

    if (av.isShowAnnotation())
    {
      final int yShift = cHeight + ypos + SEQS_ANNOTATION_GAP;
      g.translate(0, yShift);
      if (annotations == null)
      {
        annotations = new AnnotationPanel(av);
      }

      annotations.renderer.drawComponent(annotations, av, g, -1,
              startColumn, endx + 1);
      g.translate(0, -yShift);
    }
    g.translate(-xOffset, 0);
  }

  /**
   * Draws scales left, right and above (if shown), and any hidden column
   * markers, on all widths of the wrapped alignment
   * 
   * @param g
   * @param startColumn
   */
  protected void drawWrappedDecorators(Graphics g, final int startColumn)
  {
    int charWidth = av.getCharWidth();

    g.setFont(av.getFont());

    g.setColor(Color.black);

    int ypos = wrappedSpaceAboveAlignment;
    ViewportRanges ranges = av.getRanges();
    int viewportWidth = ranges.getViewportWidth();
    int maxWidth = ranges.getVisibleAlignmentWidth();
    int widthsDrawn = 0;
    int startCol = startColumn;

    while (widthsDrawn < wrappedVisibleWidths)
    {
      int endColumn = Math.min(maxWidth, startCol + viewportWidth - 1);

      if (av.getScaleLeftWrapped())
      {
        drawVerticalScale(g, startCol, endColumn - 1, ypos, true);
      }

      if (av.getScaleRightWrapped())
      {
        int x = labelWidthWest + viewportWidth * charWidth;

        g.translate(x, 0);
        drawVerticalScale(g, startCol, endColumn, ypos, false);
        g.translate(-x, 0);
      }

      /*
       * white fill region of scale above and hidden column markers
       * (to support incremental fast paint of image)
       */
      g.translate(labelWidthWest, 0);
      g.setColor(Color.white);
      g.fillRect(0, ypos - wrappedSpaceAboveAlignment,
              viewportWidth * charWidth + labelWidthWest,
              wrappedSpaceAboveAlignment);
      g.setColor(Color.black);
      g.translate(-labelWidthWest, 0);

      g.translate(labelWidthWest, 0);

      if (av.getScaleAboveWrapped())
      {
        drawNorthScale(g, startCol, endColumn, ypos);
      }

      if (av.hasHiddenColumns() && av.getShowHiddenMarkers())
      {
        drawHiddenColumnMarkers(g, ypos, startCol, endColumn);
      }

      g.translate(-labelWidthWest, 0);

      ypos += wrappedRepeatHeightPx;
      startCol += viewportWidth;
      widthsDrawn++;
    }
  }

  /**
   * Draws markers (triangles) above hidden column positions between startColumn
   * and endColumn.
   * 
   * @param g
   * @param ypos
   * @param startColumn
   * @param endColumn
   */
  protected void drawHiddenColumnMarkers(Graphics g, int ypos,
          int startColumn, int endColumn)
  {
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();

    g.setColor(Color.blue);
    int res;
    HiddenColumns hidden = av.getAlignment().getHiddenColumns();

    Iterator<Integer> it = hidden.getStartRegionIterator(startColumn,
            endColumn);
    while (it.hasNext())
    {
      res = it.next() - startColumn;

      if (res < 0 || res > endColumn - startColumn + 1)
      {
        continue;
      }

      /*
       * draw a downward-pointing triangle at the hidden columns location
       * (before the following visible column)
       */
      int xMiddle = res * charWidth;
      int[] xPoints = new int[] { xMiddle - charHeight / 4,
          xMiddle + charHeight / 4, xMiddle };
      int yTop = ypos - (charHeight / 2);
      int[] yPoints = new int[] { yTop, yTop, yTop + 8 };
      g.fillPolygon(xPoints, yPoints, 3);
    }
  }

  /*
   * Draw a selection group over a wrapped alignment
   */
  private void drawWrappedSelection(Graphics2D g, SequenceGroup group,
          int canvasWidth, int canvasHeight, int startRes)
  {
    // chop the wrapped alignment extent up into panel-sized blocks and treat
    // each block as if it were a block from an unwrapped alignment
    g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 3f, new float[]
            { 5f, 3f }, 0f));
    g.setColor(Color.RED);

    int charWidth = av.getCharWidth();
    int cWidth = (canvasWidth - labelWidthEast - labelWidthWest)
            / charWidth;
    int startx = startRes;
    int maxwidth = av.getAlignment().getVisibleWidth();
    int ypos = wrappedSpaceAboveAlignment;

    while ((ypos <= canvasHeight) && (startx < maxwidth))
    {
      // set end value to be start + width, or maxwidth, whichever is smaller
      int endx = startx + cWidth - 1;

      if (endx > maxwidth)
      {
        endx = maxwidth;
      }

      g.translate(labelWidthWest, 0);
      drawUnwrappedSelection(g, group, startx, endx, 0,
              av.getAlignment().getHeight() - 1, ypos);
      g.translate(-labelWidthWest, 0);

      ypos += wrappedRepeatHeightPx;

      startx += cWidth;
    }
    g.setStroke(new BasicStroke());
  }

  /**
   * Answers zero if annotations are not shown, otherwise recalculates and
   * answers the total height of all annotation rows in pixels
   * 
   * @return
   */
  int getAnnotationHeight()
  {
    if (!av.isShowAnnotation())
    {
      return 0;
    }

    if (annotations == null)
    {
      annotations = new AnnotationPanel(av);
    }

    return annotations.adjustPanelHeight();
  }

  /**
   * Draws the visible region of the alignment on the graphics context. If there
   * are hidden column markers in the visible region, then each sub-region
   * between the markers is drawn separately, followed by the hidden column
   * marker.
   * 
   * @param g1
   *          the graphics context, positioned at the first residue to be drawn
   * @param startRes
   *          offset of the first column to draw (0..)
   * @param endRes
   *          offset of the last column to draw (0..)
   * @param startSeq
   *          offset of the first sequence to draw (0..)
   * @param endSeq
   *          offset of the last sequence to draw (0..)
   * @param yOffset
   *          vertical offset at which to draw (for wrapped alignments)
   */
  public void drawPanel(Graphics g1, final int startRes, final int endRes,
          final int startSeq, final int endSeq, final int yOffset)
  {
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();

    if (!av.hasHiddenColumns())
    {
      draw(g1, startRes, endRes, startSeq, endSeq, yOffset);
    }
    else
    {
      int screenY = 0;
      int blockStart;
      int blockEnd;

      HiddenColumns hidden = av.getAlignment().getHiddenColumns();
      VisibleContigsIterator regions = hidden
              .getVisContigsIterator(startRes, endRes + 1, true);

      while (regions.hasNext())
      {
        int[] region = regions.next();
        blockEnd = region[1];
        blockStart = region[0];

        /*
         * draw up to just before the next hidden region, or the end of
         * the visible region, whichever comes first
         */
        g1.translate(screenY * charWidth, 0);

        draw(g1, blockStart, blockEnd, startSeq, endSeq, yOffset);

        /*
         * draw the downline of the hidden column marker (ScalePanel draws the
         * triangle on top) if we reached it
         */
        if (av.getShowHiddenMarkers()
                && (regions.hasNext() || regions.endsAtHidden()))
        {
          g1.setColor(Color.blue);

          g1.drawLine((blockEnd - blockStart + 1) * charWidth - 1,
                  0 + yOffset, (blockEnd - blockStart + 1) * charWidth - 1,
                  (endSeq - startSeq + 1) * charHeight + yOffset);
        }

        g1.translate(-screenY * charWidth, 0);
        screenY += blockEnd - blockStart + 1;
      }
    }

  }

  /**
   * Draws a region of the visible alignment
   * 
   * @param g1
   * @param startRes
   *          offset of the first column in the visible region (0..)
   * @param endRes
   *          offset of the last column in the visible region (0..)
   * @param startSeq
   *          offset of the first sequence in the visible region (0..)
   * @param endSeq
   *          offset of the last sequence in the visible region (0..)
   * @param yOffset
   *          vertical offset at which to draw (for wrapped alignments)
   */
  private void draw(Graphics g, int startRes, int endRes, int startSeq,
          int endSeq, int offset)
  {
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();

    g.setFont(av.getFont());
    seqRdr.prepare(g, av.isRenderGaps());

    SequenceI nextSeq;

    // / First draw the sequences
    // ///////////////////////////
    for (int i = startSeq; i <= endSeq; i++)
    {
      nextSeq = av.getAlignment().getSequenceAt(i);
      if (nextSeq == null)
      {
        // occasionally, a race condition occurs such that the alignment row is
        // empty
        continue;
      }
      seqRdr.drawSequence(nextSeq, av.getAlignment().findAllGroups(nextSeq),
              startRes, endRes, offset + ((i - startSeq) * charHeight));

      if (av.isShowSequenceFeatures())
      {
        fr.drawSequence(g, nextSeq, startRes, endRes,
                offset + ((i - startSeq) * charHeight), false);
      }

      /*
       * highlight search Results once sequence has been drawn
       */
      if (av.hasSearchResults())
      {
        SearchResultsI searchResults = av.getSearchResults();
        int[] visibleResults = searchResults.getResults(nextSeq, startRes,
                endRes);
        if (visibleResults != null)
        {
          for (int r = 0; r < visibleResults.length; r += 2)
          {
            seqRdr.drawHighlightedText(nextSeq, visibleResults[r],
                    visibleResults[r + 1],
                    (visibleResults[r] - startRes) * charWidth,
                    offset + ((i - startSeq) * charHeight));
          }
        }
      }
    }

    if (av.getSelectionGroup() != null
            || av.getAlignment().getGroups().size() > 0)
    {
      drawGroupsBoundaries(g, startRes, endRes, startSeq, endSeq, offset);
    }

  }

  /**
   * Draws the outlines of any groups defined on the alignment (excluding the
   * current selection group, if any)
   * 
   * @param g1
   * @param startRes
   * @param endRes
   * @param startSeq
   * @param endSeq
   * @param offset
   */
  void drawGroupsBoundaries(Graphics g1, int startRes, int endRes,
          int startSeq, int endSeq, int offset)
  {
    Graphics2D g = (Graphics2D) g1;

    SequenceGroup group = null;
    int groupIndex = -1;

    if (av.getAlignment().getGroups().size() > 0)
    {
      group = av.getAlignment().getGroups().get(0);
      groupIndex = 0;
    }

    if (group != null)
    {
      do
      {
        g.setColor(group.getOutlineColour());
        drawPartialGroupOutline(g, group, startRes, endRes, startSeq,
                endSeq, offset);

        groupIndex++;
        if (groupIndex >= av.getAlignment().getGroups().size())
        {
          break;
        }
        group = av.getAlignment().getGroups().get(groupIndex);
      } while (groupIndex < av.getAlignment().getGroups().size());
    }
  }

  /**
   * Draws the outline of the current selection group (if any)
   * 
   * @param g
   * @param startRes
   * @param endRes
   * @param startSeq
   * @param endSeq
   */
  private void drawSelectionGroup(Graphics2D g, int startRes, int endRes,
          int startSeq, int endSeq)
  {
    SequenceGroup group = av.getSelectionGroup();
    if (group == null)
    {
      return;
    }

    g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 3f, new float[]
            { 5f, 3f }, 0f));
    g.setColor(Color.RED);
    if (!av.getWrapAlignment())
    {
      drawUnwrappedSelection(g, group, startRes, endRes, startSeq, endSeq,
              0);
    }
    else
    {
      drawWrappedSelection(g, group, getWidth(), getHeight(),
              av.getRanges().getStartRes());
    }
    g.setStroke(new BasicStroke());
  }

  /**
   * Draw the cursor as a separate image and overlay
   * 
   * @param startRes
   *          start residue of area to draw cursor in
   * @param endRes
   *          end residue of area to draw cursor in
   * @param startSeq
   *          start sequence of area to draw cursor in
   * @param endSeq
   *          end sequence of are to draw cursor in
   * @return a transparent image of the same size as the sequence canvas, with
   *         the cursor drawn on it, if any
   */
  private void drawCursor(Graphics g, int startRes, int endRes,
          int startSeq, int endSeq)
  {
    // convert the cursorY into a position on the visible alignment
    int cursor_ypos = cursorY;

    // don't do work unless we have to
    if (cursor_ypos >= startSeq && cursor_ypos <= endSeq)
    {
      int yoffset = 0;
      int xoffset = 0;
      int startx = startRes;
      int endx = endRes;

      // convert the cursorX into a position on the visible alignment
      int cursor_xpos = av.getAlignment().getHiddenColumns()
              .absoluteToVisibleColumn(cursorX);

      if (av.getAlignment().getHiddenColumns().isVisible(cursorX))
      {

        if (av.getWrapAlignment())
        {
          // work out the correct offsets for the cursor
          int charHeight = av.getCharHeight();
          int charWidth = av.getCharWidth();
          int canvasWidth = getWidth();
          int canvasHeight = getHeight();

          // height gap above each panel
          int hgap = charHeight;
          if (av.getScaleAboveWrapped())
          {
            hgap += charHeight;
          }

          int cWidth = (canvasWidth - labelWidthEast - labelWidthWest)
                  / charWidth;
          int cHeight = av.getAlignment().getHeight() * charHeight;

          endx = startx + cWidth - 1;
          int ypos = hgap; // vertical offset

          // iterate down the wrapped panels
          while ((ypos <= canvasHeight) && (endx < cursor_xpos))
          {
            // update vertical offset
            ypos += cHeight + getAnnotationHeight() + hgap;

            // update horizontal offset
            startx += cWidth;
            endx = startx + cWidth - 1;
          }
          yoffset = ypos;
          xoffset = labelWidthWest;
        }

        // now check if cursor is within range for x values
        if (cursor_xpos >= startx && cursor_xpos <= endx)
        {
          // get the character the cursor is drawn at
          SequenceI seq = av.getAlignment().getSequenceAt(cursorY);
          char s = seq.getCharAt(cursorX);

          seqRdr.drawCursor(g, s,
                  xoffset + (cursor_xpos - startx) * av.getCharWidth(),
                  yoffset + (cursor_ypos - startSeq) * av.getCharHeight());
        }
      }
    }
  }

  /**
   * Draw a selection group over an unwrapped alignment
   * 
   * @param g
   *          graphics object to draw with
   * @param group
   *          selection group
   * @param startRes
   *          start residue of area to draw
   * @param endRes
   *          end residue of area to draw
   * @param startSeq
   *          start sequence of area to draw
   * @param endSeq
   *          end sequence of area to draw
   * @param offset
   *          vertical offset (used when called from wrapped alignment code)
   */
  private void drawUnwrappedSelection(Graphics2D g, SequenceGroup group,
          int startRes, int endRes, int startSeq, int endSeq, int offset)
  {
    int charWidth = av.getCharWidth();

    if (!av.hasHiddenColumns())
    {
      drawPartialGroupOutline(g, group, startRes, endRes, startSeq, endSeq,
              offset);
    }
    else
    {
      // package into blocks of visible columns
      int screenY = 0;
      int blockStart;
      int blockEnd;

      HiddenColumns hidden = av.getAlignment().getHiddenColumns();
      VisibleContigsIterator regions = hidden
              .getVisContigsIterator(startRes, endRes + 1, true);
      while (regions.hasNext())
      {
        int[] region = regions.next();
        blockEnd = region[1];
        blockStart = region[0];

        g.translate(screenY * charWidth, 0);
        drawPartialGroupOutline(g, group, blockStart, blockEnd, startSeq,
                endSeq, offset);

        g.translate(-screenY * charWidth, 0);
        screenY += blockEnd - blockStart + 1;
      }
    }
  }

  /**
   * Draws part of a selection group outline
   * 
   * @param g
   * @param group
   * @param startRes
   * @param endRes
   * @param startSeq
   * @param endSeq
   * @param verticalOffset
   */
  private void drawPartialGroupOutline(Graphics2D g, SequenceGroup group,
          int startRes, int endRes, int startSeq, int endSeq,
          int verticalOffset)
  {
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();
    int visWidth = (endRes - startRes + 1) * charWidth;

    int oldY = -1;
    int i = 0;
    boolean inGroup = false;
    int top = -1;
    int bottom = -1;
    int sy = -1;

    List<SequenceI> seqs = group.getSequences(null);

    // position of start residue of group relative to startRes, in pixels
    int sx = (group.getStartRes() - startRes) * charWidth;

    // width of group in pixels
    int xwidth = (((group.getEndRes() + 1) - group.getStartRes())
            * charWidth) - 1;

    if (!(sx + xwidth < 0 || sx > visWidth))
    {
      for (i = startSeq; i <= endSeq; i++)
      {
        sy = verticalOffset + (i - startSeq) * charHeight;

        if ((sx <= (endRes - startRes) * charWidth)
                && seqs.contains(av.getAlignment().getSequenceAt(i)))
        {
          if ((bottom == -1)
                  && !seqs.contains(av.getAlignment().getSequenceAt(i + 1)))
          {
            bottom = sy + charHeight;
          }

          if (!inGroup)
          {
            if (((top == -1) && (i == 0)) || !seqs
                    .contains(av.getAlignment().getSequenceAt(i - 1)))
            {
              top = sy;
            }

            oldY = sy;
            inGroup = true;
          }
        }
        else if (inGroup)
        {
          drawVerticals(g, sx, xwidth, visWidth, oldY, sy);
          drawHorizontals(g, sx, xwidth, visWidth, top, bottom);

          // reset top and bottom
          top = -1;
          bottom = -1;
          inGroup = false;
        }
      }
      if (inGroup)
      {
        sy = verticalOffset + ((i - startSeq) * charHeight);
        drawVerticals(g, sx, xwidth, visWidth, oldY, sy);
        drawHorizontals(g, sx, xwidth, visWidth, top, bottom);
      }
    }
  }

  /**
   * Draw horizontal selection group boundaries at top and bottom positions
   * 
   * @param g
   *          graphics object to draw on
   * @param sx
   *          start x position
   * @param xwidth
   *          width of gap
   * @param visWidth
   *          visWidth maximum available width
   * @param top
   *          position to draw top of group at
   * @param bottom
   *          position to draw bottom of group at
   */
  private void drawHorizontals(Graphics2D g, int sx, int xwidth,
          int visWidth, int top, int bottom)
  {
    int width = xwidth;
    int startx = sx;
    if (startx < 0)
    {
      width += startx;
      startx = 0;
    }

    // don't let width extend beyond current block, or group extent
    // fixes JAL-2672
    if (startx + width >= visWidth)
    {
      width = visWidth - startx;
    }

    if (top != -1)
    {
      g.drawLine(startx, top, startx + width, top);
    }

    if (bottom != -1)
    {
      g.drawLine(startx, bottom - 1, startx + width, bottom - 1);
    }
  }

  /**
   * Draw vertical lines at sx and sx+xwidth providing they lie within
   * [0,visWidth)
   * 
   * @param g
   *          graphics object to draw on
   * @param sx
   *          start x position
   * @param xwidth
   *          width of gap
   * @param visWidth
   *          visWidth maximum available width
   * @param oldY
   *          top y value
   * @param sy
   *          bottom y value
   */
  private void drawVerticals(Graphics2D g, int sx, int xwidth, int visWidth,
          int oldY, int sy)
  {
    // if start position is visible, draw vertical line to left of
    // group
    if (sx >= 0 && sx < visWidth)
    {
      g.drawLine(sx, oldY, sx, sy);
    }

    // if end position is visible, draw vertical line to right of
    // group
    if (sx + xwidth < visWidth)
    {
      g.drawLine(sx + xwidth, oldY, sx + xwidth, sy);
    }
  }

  /**
   * Highlights search results in the visible region by rendering as white text
   * on a black background. Any previous highlighting is removed. Answers true
   * if any highlight was left on the visible alignment (so status bar should be
   * set to match), else false. This method does _not_ set the 'fastPaint' flag,
   * so allows the next repaint to update the whole display.
   * 
   * @param results
   * @return
   */
  public boolean highlightSearchResults(SearchResultsI results)
  {
    return highlightSearchResults(results, false);

  }

  /**
   * Highlights search results in the visible region by rendering as white text
   * on a black background. Any previous highlighting is removed. Answers true
   * if any highlight was left on the visible alignment (so status bar should be
   * set to match), else false.
   * <p>
   * Optionally, set the 'fastPaint' flag for a faster redraw if only the
   * highlighted regions are modified. This speeds up highlighting across linked
   * alignments.
   * <p>
   * Currently fastPaint is not implemented for scrolled wrapped alignments. If
   * a wrapped alignment had to be scrolled to show the highlighted region, then
   * it should be fully redrawn, otherwise a fast paint can be performed. This
   * argument could be removed if fast paint of scrolled wrapped alignment is
   * coded in future (JAL-2609).
   * 
   * @param results
   * @param doFastPaint
   *          if true, sets a flag so the next repaint only redraws the modified
   *          image
   * @return
   */
  public boolean highlightSearchResults(SearchResultsI results,
          boolean doFastPaint)
  {
    if (fastpainting)
    {
      return false;
    }
    boolean wrapped = av.getWrapAlignment();
    try
    {
      fastPaint = doFastPaint;
      fastpainting = fastPaint;

      /*
       * to avoid redrawing the whole visible region, we instead
       * redraw just the minimal regions to remove previous highlights
       * and add new ones
       */
      SearchResultsI previous = av.getSearchResults();
      av.setSearchResults(results);
      boolean redrawn = false;
      boolean drawn = false;
      if (wrapped)
      {
        redrawn = drawMappedPositionsWrapped(previous);
        drawn = drawMappedPositionsWrapped(results);
        redrawn |= drawn;
      }
      else
      {
        redrawn = drawMappedPositions(previous);
        drawn = drawMappedPositions(results);
        redrawn |= drawn;
      }

      /*
       * if highlights were either removed or added, repaint
       */
      if (redrawn)
      {
        repaint();
      }

      /*
       * return true only if highlights were added
       */
      return drawn;

    } finally
    {
      fastpainting = false;
    }
  }

  /**
   * Redraws the minimal rectangle in the visible region (if any) that includes
   * mapped positions of the given search results. Whether or not positions are
   * highlighted depends on the SearchResults set on the Viewport. This allows
   * this method to be called to either clear or set highlighting. Answers true
   * if any positions were drawn (in which case a repaint is still required),
   * else false.
   * 
   * @param results
   * @return
   */
  protected boolean drawMappedPositions(SearchResultsI results)
  {
    if ((results == null) || (img == null)) // JAL-2784 check gg is not null
    {
      return false;
    }

    /*
     * calculate the minimal rectangle to redraw that 
     * includes both new and existing search results
     */
    int firstSeq = Integer.MAX_VALUE;
    int lastSeq = -1;
    int firstCol = Integer.MAX_VALUE;
    int lastCol = -1;
    boolean matchFound = false;

    ViewportRanges ranges = av.getRanges();
    int firstVisibleColumn = ranges.getStartRes();
    int lastVisibleColumn = ranges.getEndRes();
    AlignmentI alignment = av.getAlignment();
    if (av.hasHiddenColumns())
    {
      firstVisibleColumn = alignment.getHiddenColumns()
              .visibleToAbsoluteColumn(firstVisibleColumn);
      lastVisibleColumn = alignment.getHiddenColumns()
              .visibleToAbsoluteColumn(lastVisibleColumn);
    }

    for (int seqNo = ranges.getStartSeq(); seqNo <= ranges
            .getEndSeq(); seqNo++)
    {
      SequenceI seq = alignment.getSequenceAt(seqNo);

      int[] visibleResults = results.getResults(seq, firstVisibleColumn,
              lastVisibleColumn);
      if (visibleResults != null)
      {
        for (int i = 0; i < visibleResults.length - 1; i += 2)
        {
          int firstMatchedColumn = visibleResults[i];
          int lastMatchedColumn = visibleResults[i + 1];
          if (firstMatchedColumn <= lastVisibleColumn
                  && lastMatchedColumn >= firstVisibleColumn)
          {
            /*
             * found a search results match in the visible region - 
             * remember the first and last sequence matched, and the first
             * and last visible columns in the matched positions
             */
            matchFound = true;
            firstSeq = Math.min(firstSeq, seqNo);
            lastSeq = Math.max(lastSeq, seqNo);
            firstMatchedColumn = Math.max(firstMatchedColumn,
                    firstVisibleColumn);
            lastMatchedColumn = Math.min(lastMatchedColumn,
                    lastVisibleColumn);
            firstCol = Math.min(firstCol, firstMatchedColumn);
            lastCol = Math.max(lastCol, lastMatchedColumn);
          }
        }
      }
    }

    if (matchFound)
    {
      if (av.hasHiddenColumns())
      {
        firstCol = alignment.getHiddenColumns()
                .absoluteToVisibleColumn(firstCol);
        lastCol = alignment.getHiddenColumns()
                .absoluteToVisibleColumn(lastCol);
      }
      int transX = (firstCol - ranges.getStartRes()) * av.getCharWidth();
      int transY = (firstSeq - ranges.getStartSeq()) * av.getCharHeight();
      Graphics gg = img.getGraphics();
      gg.translate(transX, transY);
      drawPanel(gg, firstCol, lastCol, firstSeq, lastSeq, 0);
      gg.translate(-transX, -transY);
      gg.dispose();
    }

    return matchFound;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    String eventName = evt.getPropertyName();
    // System.err.println(">>SeqCanvas propertyChange " + eventName);
    if (eventName.equals(SequenceGroup.SEQ_GROUP_CHANGED))
    {
      fastPaint = true;
      repaint();
      return;
    }
    else if (eventName.equals(ViewportRanges.MOVE_VIEWPORT))
    {
      fastPaint = false;
      // System.err.println("!!!! fastPaint false from MOVE_VIEWPORT");
      repaint();
      return;
    }

    int scrollX = 0;
    if (eventName.equals(ViewportRanges.STARTRES)
            || eventName.equals(ViewportRanges.STARTRESANDSEQ))
    {
      // Make sure we're not trying to draw a panel
      // larger than the visible window
      if (eventName.equals(ViewportRanges.STARTRES))
      {
        scrollX = (int) evt.getNewValue() - (int) evt.getOldValue();
      }
      else
      {
        scrollX = ((int[]) evt.getNewValue())[0]
                - ((int[]) evt.getOldValue())[0];
      }
      ViewportRanges vpRanges = av.getRanges();

      int range = vpRanges.getEndRes() - vpRanges.getStartRes() + 1;
      if (scrollX > range)
      {
        scrollX = range;
      }
      else if (scrollX < -range)
      {
        scrollX = -range;
      }
    }
    // Both scrolling and resizing change viewport ranges: scrolling changes
    // both start and end points, but resize only changes end values.
    // Here we only want to fastpaint on a scroll, with resize using a normal
    // paint, so scroll events are identified as changes to the horizontal or
    // vertical start value.
    if (eventName.equals(ViewportRanges.STARTRES))
    {
      if (av.getWrapAlignment())
      {
        fastPaintWrapped(scrollX);
      }
      else
      {
        fastPaint(scrollX, 0);
      }
    }
    else if (eventName.equals(ViewportRanges.STARTSEQ))
    {
      // scroll
      fastPaint(0, (int) evt.getNewValue() - (int) evt.getOldValue());
    }
    else if (eventName.equals(ViewportRanges.STARTRESANDSEQ))
    {
      if (av.getWrapAlignment())
      {
        fastPaintWrapped(scrollX);
      }
      else
      {
        fastPaint(scrollX, 0);
      }
    }
    else if (eventName.equals(ViewportRanges.STARTSEQ))
    {
      // scroll
      fastPaint(0, (int) evt.getNewValue() - (int) evt.getOldValue());
    }
    else if (eventName.equals(ViewportRanges.STARTRESANDSEQ))
    {
      if (av.getWrapAlignment())
      {
        fastPaintWrapped(scrollX);
      }
    }
  }

  /**
   * Does a minimal update of the image for a scroll movement. This method
   * handles scroll movements of up to one width of the wrapped alignment (one
   * click in the vertical scrollbar). Larger movements (for example after a
   * scroll to highlight a mapped position) trigger a full redraw instead.
   * 
   * @param scrollX
   *          number of positions scrolled (right if positive, left if negative)
   */
  protected void fastPaintWrapped(int scrollX)
  {
    ViewportRanges ranges = av.getRanges();

    if (Math.abs(scrollX) >= ranges.getViewportWidth())
    {
      /*
       * shift of one view width or more is 
       * overcomplicated to handle in this method
       */
      fastPaint = false;
      repaint();
      return;
    }

    if (fastpainting || img == null)
    {
      return;
    }

    fastPaint = true;
    fastpainting = true;

    try
    {

      Graphics gg = img.getGraphics();

      calculateWrappedGeometry(getWidth(), getHeight());

      /*
       * relocate the regions of the alignment that are still visible
       */
      shiftWrappedAlignment(-scrollX);

      /*
       * add new columns (sequence, annotation)
       * - at top left if scrollX < 0 
       * - at right of last two widths if scrollX > 0
       */
      if (scrollX < 0)
      {
        int startRes = ranges.getStartRes();
        drawWrappedWidth(gg, wrappedSpaceAboveAlignment, startRes,
                startRes - scrollX - 1, getHeight());
      }
      else
      {
        fastPaintWrappedAddRight(scrollX);
      }

      /*
       * draw all scales (if  shown) and hidden column markers
       */
      drawWrappedDecorators(gg, ranges.getStartRes());

      gg.dispose();

      repaint();
    } finally
    {
      fastpainting = false;
    }
  }

  /**
   * Draws the specified number of columns at the 'end' (bottom right) of a
   * wrapped alignment view, including sequences and annotations if shown, but
   * not scales. Also draws the same number of columns at the right hand end of
   * the second last width shown, if the last width is not full height (so
   * cannot simply be copied from the graphics image).
   * 
   * @param columns
   */
  protected void fastPaintWrappedAddRight(int columns)
  {
    if (columns == 0)
    {
      return;
    }

    Graphics gg = img.getGraphics();

    ViewportRanges ranges = av.getRanges();
    int viewportWidth = ranges.getViewportWidth();
    int charWidth = av.getCharWidth();

    /**
     * draw full height alignment in the second last row, last columns, if the
     * last row was not full height
     */
    int visibleWidths = wrappedVisibleWidths;
    int canvasHeight = getHeight();
    boolean lastWidthPartHeight = (wrappedVisibleWidths
            * wrappedRepeatHeightPx) > canvasHeight;

    if (lastWidthPartHeight)
    {
      int widthsAbove = Math.max(0, visibleWidths - 2);
      int ypos = wrappedRepeatHeightPx * widthsAbove
              + wrappedSpaceAboveAlignment;
      int endRes = ranges.getEndRes();
      endRes += widthsAbove * viewportWidth;
      int startRes = endRes - columns;
      int xOffset = ((startRes - ranges.getStartRes()) % viewportWidth)
              * charWidth;

      /*
       * white fill first to erase annotations
       */

      gg.translate(xOffset, 0);
      gg.setColor(Color.white);
      gg.fillRect(labelWidthWest, ypos, (endRes - startRes + 1) * charWidth,
              wrappedRepeatHeightPx);
      gg.translate(-xOffset, 0);

      drawWrappedWidth(gg, ypos, startRes, endRes, canvasHeight);

    }

    /*
     * draw newly visible columns in last wrapped width (none if we
     * have reached the end of the alignment)
     * y-offset for drawing last width is height of widths above,
     * plus one gap row
     */
    int widthsAbove = visibleWidths - 1;
    int ypos = wrappedRepeatHeightPx * widthsAbove
            + wrappedSpaceAboveAlignment;
    int endRes = ranges.getEndRes();
    endRes += widthsAbove * viewportWidth;
    int startRes = endRes - columns + 1;

    /*
     * white fill first to erase annotations
     */
    int xOffset = ((startRes - ranges.getStartRes()) % viewportWidth)
            * charWidth;
    gg.translate(xOffset, 0);
    gg.setColor(Color.white);
    int width = viewportWidth * charWidth - xOffset;
    gg.fillRect(labelWidthWest, ypos, width, wrappedRepeatHeightPx);
    gg.translate(-xOffset, 0);

    gg.setFont(av.getFont());
    gg.setColor(Color.black);

    if (startRes < ranges.getVisibleAlignmentWidth())
    {
      drawWrappedWidth(gg, ypos, startRes, endRes, canvasHeight);
    }

    /*
     * and finally, white fill any space below the visible alignment
     */
    int heightBelow = canvasHeight - visibleWidths * wrappedRepeatHeightPx;
    if (heightBelow > 0)
    {
      gg.setColor(Color.white);
      gg.fillRect(0, canvasHeight - heightBelow, getWidth(), heightBelow);
    }
    gg.dispose();
  }

  /**
   * Shifts the visible alignment by the specified number of columns - left if
   * negative, right if positive. Copies and moves sequences and annotations (if
   * shown). Scales, hidden column markers and any newly visible columns must be
   * drawn separately.
   * 
   * @param positions
   */
  protected void shiftWrappedAlignment(int positions)
  {
    if (positions == 0)
    {
      return;
    }

    Graphics gg = img.getGraphics();

    int charWidth = av.getCharWidth();

    int canvasHeight = getHeight();
    ViewportRanges ranges = av.getRanges();
    int viewportWidth = ranges.getViewportWidth();
    int widthToCopy = (ranges.getViewportWidth() - Math.abs(positions))
            * charWidth;
    int heightToCopy = wrappedRepeatHeightPx - wrappedSpaceAboveAlignment;
    int xMax = ranges.getVisibleAlignmentWidth();

    if (positions > 0)
    {
      /*
       * shift right (after scroll left)
       * for each wrapped width (starting with the last), copy (width-positions) 
       * columns from the left margin to the right margin, and copy positions 
       * columns from the right margin of the row above (if any) to the 
       * left margin of the current row
       */

      /*
       * get y-offset of last wrapped width, first row of sequences
       */
      int y = canvasHeight / wrappedRepeatHeightPx * wrappedRepeatHeightPx;
      y += wrappedSpaceAboveAlignment;
      int copyFromLeftStart = labelWidthWest;
      int copyFromRightStart = copyFromLeftStart + widthToCopy;

      while (y >= 0)
      {
        /*
         * shift 'widthToCopy' residues by 'positions' places to the right
         */
        gg.copyArea(copyFromLeftStart, y, widthToCopy, heightToCopy,
                positions * charWidth, 0);
        if (y > 0)
        {
          /*
           * copy 'positions' residue from the row above (right hand end)
           * to this row's left hand end
           */
          gg.copyArea(copyFromRightStart, y - wrappedRepeatHeightPx,
                  positions * charWidth, heightToCopy, -widthToCopy,
                  wrappedRepeatHeightPx);
        }

        y -= wrappedRepeatHeightPx;
      }
    }
    else
    {
      /*
       * shift left (after scroll right)
       * for each wrapped width (starting with the first), copy (width-positions) 
       * columns from the right margin to the left margin, and copy positions 
       * columns from the left margin of the row below (if any) to the 
       * right margin of the current row
       */
      int xpos = av.getRanges().getStartRes();
      int y = wrappedSpaceAboveAlignment;
      int copyFromRightStart = labelWidthWest - positions * charWidth;

      while (y < canvasHeight)
      {
        gg.copyArea(copyFromRightStart, y, widthToCopy, heightToCopy,
                positions * charWidth, 0);
        if (y + wrappedRepeatHeightPx < canvasHeight - wrappedRepeatHeightPx
                && (xpos + viewportWidth <= xMax))
        {
          gg.copyArea(labelWidthWest, y + wrappedRepeatHeightPx,
                  -positions * charWidth, heightToCopy, widthToCopy,
                  -wrappedRepeatHeightPx);
        }
        y += wrappedRepeatHeightPx;
        xpos += viewportWidth;
      }
    }
    gg.dispose();
  }

  /**
   * Redraws any positions in the search results in the visible region of a
   * wrapped alignment. Any highlights are drawn depending on the search results
   * set on the Viewport, not the <code>results</code> argument. This allows
   * this method to be called either to clear highlights (passing the previous
   * search results), or to draw new highlights.
   * 
   * @param results
   * @return
   */
  protected boolean drawMappedPositionsWrapped(SearchResultsI results)
  {
    if ((results == null) || (img == null)) // JAL-2784 check gg is not null
    {
      return false;
    }
    int charHeight = av.getCharHeight();

    boolean matchFound = false;

    calculateWrappedGeometry(getWidth(), getHeight());
    int wrappedWidth = av.getWrappedWidth();
    int wrappedHeight = wrappedRepeatHeightPx;

    ViewportRanges ranges = av.getRanges();
    int canvasHeight = getHeight();
    int repeats = canvasHeight / wrappedHeight;
    if (canvasHeight / wrappedHeight > 0)
    {
      repeats++;
    }

    int firstVisibleColumn = ranges.getStartRes();
    int lastVisibleColumn = ranges.getStartRes()
            + repeats * ranges.getViewportWidth() - 1;

    AlignmentI alignment = av.getAlignment();
    if (av.hasHiddenColumns())
    {
      firstVisibleColumn = alignment.getHiddenColumns()
              .visibleToAbsoluteColumn(firstVisibleColumn);
      lastVisibleColumn = alignment.getHiddenColumns()
              .visibleToAbsoluteColumn(lastVisibleColumn);
    }

    int gapHeight = charHeight * (av.getScaleAboveWrapped() ? 2 : 1);

    Graphics gg = img.getGraphics();

    for (int seqNo = ranges.getStartSeq(); seqNo <= ranges
            .getEndSeq(); seqNo++)
    {
      SequenceI seq = alignment.getSequenceAt(seqNo);

      int[] visibleResults = results.getResults(seq, firstVisibleColumn,
              lastVisibleColumn);
      if (visibleResults != null)
      {
        for (int i = 0; i < visibleResults.length - 1; i += 2)
        {
          int firstMatchedColumn = visibleResults[i];
          int lastMatchedColumn = visibleResults[i + 1];
          if (firstMatchedColumn <= lastVisibleColumn
                  && lastMatchedColumn >= firstVisibleColumn)
          {
            /*
             * found a search results match in the visible region
             */
            firstMatchedColumn = Math.max(firstMatchedColumn,
                    firstVisibleColumn);
            lastMatchedColumn = Math.min(lastMatchedColumn,
                    lastVisibleColumn);

            /*
             * draw each mapped position separately (as contiguous positions may
             * wrap across lines)
             */
            for (int mappedPos = firstMatchedColumn; mappedPos <= lastMatchedColumn; mappedPos++)
            {
              int displayColumn = mappedPos;
              if (av.hasHiddenColumns())
              {
                displayColumn = alignment.getHiddenColumns()
                        .absoluteToVisibleColumn(displayColumn);
              }

              /*
               * transX: offset from left edge of canvas to residue position
               */
              int transX = labelWidthWest
                      + ((displayColumn - ranges.getStartRes())
                              % wrappedWidth) * av.getCharWidth();

              /*
               * transY: offset from top edge of canvas to residue position
               */
              int transY = gapHeight;
              transY += (displayColumn - ranges.getStartRes())
                      / wrappedWidth * wrappedHeight;
              transY += (seqNo - ranges.getStartSeq()) * av.getCharHeight();

              /*
               * yOffset is from graphics origin to start of visible region
               */
              int yOffset = 0;// (displayColumn / wrappedWidth) * wrappedHeight;
              if (transY < getHeight())
              {
                matchFound = true;
                gg.translate(transX, transY);
                drawPanel(gg, displayColumn, displayColumn, seqNo, seqNo,
                        yOffset);
                gg.translate(-transX, -transY);
              }
            }
          }
        }
      }
    }

    gg.dispose();

    return matchFound;
  }

  /**
   * Answers the width in pixels of the left scale labels (0 if not shown)
   * 
   * @return
   */
  int getLabelWidthWest()
  {
    return labelWidthWest;
  }

}
