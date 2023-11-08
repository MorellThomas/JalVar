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

import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.VisibleContigsIterator;
import jalview.renderer.ScaleRenderer;
import jalview.renderer.ScaleRenderer.ScaleMark;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.ViewportListenerI;
import jalview.viewmodel.ViewportRanges;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;

@SuppressWarnings("serial")
public class SeqCanvas extends Panel implements ViewportListenerI
{
  FeatureRenderer fr;

  SequenceRenderer sr;

  Image img;

  Graphics gg;

  int imgWidth;

  int imgHeight;

  AlignViewport av;

  boolean fastPaint = false;

  int cursorX = 0;

  int cursorY = 0;

  public SeqCanvas(AlignViewport av)
  {
    this.av = av;
    fr = new FeatureRenderer(av);
    sr = new SequenceRenderer(av);
    PaintRefresher.Register(this, av.getSequenceSetId());
    updateViewport();

    av.getRanges().addPropertyChangeListener(this);
  }

  int avcharHeight = 0, avcharWidth = 0;

  private void updateViewport()
  {
    avcharHeight = av.getCharHeight();
    avcharWidth = av.getCharWidth();
  }

  public AlignmentViewport getViewport()
  {
    return av;
  }

  public FeatureRenderer getFeatureRenderer()
  {
    return fr;
  }

  public SequenceRenderer getSequenceRenderer()
  {
    return sr;
  }

  private void drawNorthScale(Graphics g, int startx, int endx, int ypos)
  {
    updateViewport();
    g.setColor(Color.black);
    for (ScaleMark mark : new ScaleRenderer().calculateMarks(av, startx,
            endx))
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
          g.drawString(mstring, mpos * avcharWidth,
                  ypos - (avcharHeight / 2));
        }
        g.drawLine((mpos * avcharWidth) + (avcharWidth / 2),
                (ypos + 2) - (avcharHeight / 2),
                (mpos * avcharWidth) + (avcharWidth / 2), ypos - 2);
      }
    }
  }

  private void drawWestScale(Graphics g, int startx, int endx, int ypos)
  {
    FontMetrics fm = getFontMetrics(av.getFont());
    ypos += avcharHeight;
    if (av.hasHiddenColumns())
    {
      startx = av.getAlignment().getHiddenColumns()
              .visibleToAbsoluteColumn(startx);
      endx = av.getAlignment().getHiddenColumns()
              .visibleToAbsoluteColumn(endx);
    }

    // WEST SCALE
    for (int i = 0; i < av.getAlignment().getHeight(); i++)
    {
      SequenceI seq = av.getAlignment().getSequenceAt(i);
      int index = startx;
      int value = -1;

      while (index < endx)
      {
        if (jalview.util.Comparison.isGap(seq.getCharAt(index)))
        {
          index++;

          continue;
        }

        value = av.getAlignment().getSequenceAt(i).findPosition(index);

        break;
      }

      if (value != -1)
      {
        int x = LABEL_WEST - fm.stringWidth(String.valueOf(value))
                - avcharWidth / 2;
        g.drawString(value + "", x,
                (ypos + (i * avcharHeight)) - (avcharHeight / 5));
      }
    }
  }

  private void drawEastScale(Graphics g, int startx, int endx, int ypos)
  {
    ypos += avcharHeight;

    if (av.hasHiddenColumns())
    {
      endx = av.getAlignment().getHiddenColumns()
              .visibleToAbsoluteColumn(endx);
    }

    SequenceI seq;
    // EAST SCALE
    for (int i = 0; i < av.getAlignment().getHeight(); i++)
    {
      seq = av.getAlignment().getSequenceAt(i);
      int index = endx;
      int value = -1;

      while (index > startx)
      {
        if (jalview.util.Comparison.isGap(seq.getCharAt(index)))
        {
          index--;

          continue;
        }

        value = seq.findPosition(index);

        break;
      }

      if (value != -1)
      {
        g.drawString(String.valueOf(value), 0,
                (ypos + (i * avcharHeight)) - (avcharHeight / 5));
      }
    }
  }

  int lastsr = 0;

  void fastPaint(int horizontal, int vertical)
  {
    if (fastPaint || gg == null)
    {
      return;
    }

    ViewportRanges ranges = av.getRanges();

    updateViewport();

    // Its possible on certain browsers that the call to fastpaint
    // is faster than it can paint, so this check here catches
    // this possibility
    if (lastsr + horizontal != ranges.getStartRes())
    {
      horizontal = ranges.getStartRes() - lastsr;
    }

    lastsr = ranges.getStartRes();

    fastPaint = true;
    gg.copyArea(horizontal * avcharWidth, vertical * avcharHeight,
            imgWidth - horizontal * avcharWidth,
            imgHeight - vertical * avcharHeight, -horizontal * avcharWidth,
            -vertical * avcharHeight);

    int sr = ranges.getStartRes(), er = ranges.getEndRes(),
            ss = ranges.getStartSeq(), es = ranges.getEndSeq(), transX = 0,
            transY = 0;

    if (horizontal > 0) // scrollbar pulled right, image to the left
    {
      transX = (er - sr - horizontal) * avcharWidth;
      sr = er - horizontal;
    }
    else if (horizontal < 0)
    {
      er = sr - horizontal;
    }

    else if (vertical > 0) // scroll down
    {
      ss = es - vertical;
      if (ss < ranges.getStartSeq()) // ie scrolling too fast, more than a page
                                     // at a
      // time
      {
        ss = ranges.getStartSeq();
      }
      else
      {
        transY = imgHeight - ((vertical + 1) * avcharHeight);
      }
    }
    else if (vertical < 0)
    {
      es = ss - vertical;
      if (es > ranges.getEndSeq())
      {
        es = ranges.getEndSeq();
      }
    }

    gg.translate(transX, transY);

    drawPanel(gg, sr, er, ss, es, 0);
    gg.translate(-transX, -transY);

    repaint();

  }

  /**
   * Definitions of startx and endx (hopefully): SMJS This is what I'm working
   * towards! startx is the first residue (starting at 0) to display. endx is
   * the last residue to display (starting at 0). starty is the first sequence
   * to display (starting at 0). endy is the last sequence to display (starting
   * at 0). NOTE 1: The av limits are set in setFont in this class and in the
   * adjustment listener in SeqPanel when the scrollbars move.
   */
  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {

    if (img != null
            && (fastPaint || (getSize().width != g.getClipBounds().width)
                    || (getSize().height != g.getClipBounds().height)))
    {
      g.drawImage(img, 0, 0, this);
      fastPaint = false;
      return;
    }

    if (fastPaint)
    {
      g.drawImage(img, 0, 0, this);
      fastPaint = false;
      return;
    }

    updateViewport();
    // this draws the whole of the alignment
    imgWidth = this.getSize().width;
    imgHeight = this.getSize().height;

    imgWidth -= imgWidth % avcharWidth;
    imgHeight -= imgHeight % avcharHeight;

    if (imgWidth < 1 || imgHeight < 1)
    {
      return;
    }

    if (img == null || imgWidth != img.getWidth(this)
            || imgHeight != img.getHeight(this))
    {
      img = createImage(imgWidth, imgHeight);
      gg = img.getGraphics();
      gg.setFont(av.getFont());
    }

    gg.setColor(Color.white);
    gg.fillRect(0, 0, imgWidth, imgHeight);

    ViewportRanges ranges = av.getRanges();

    if (av.getWrapAlignment())
    {
      drawWrappedPanel(gg, imgWidth, imgHeight, ranges.getStartRes());
    }
    else
    {
      drawPanel(gg, ranges.getStartRes(), ranges.getEndRes(),
              ranges.getStartSeq(), ranges.getEndSeq(), 0);
    }

    g.drawImage(img, 0, 0, this);

  }

  int LABEL_WEST, LABEL_EAST;

  public int getWrappedCanvasWidth(int cwidth)
  {
    cwidth -= cwidth % av.getCharWidth();

    FontMetrics fm = getFontMetrics(av.getFont());

    LABEL_EAST = 0;
    LABEL_WEST = 0;

    if (av.getScaleRightWrapped())
    {
      LABEL_EAST = fm.stringWidth(getMask());
    }

    if (av.getScaleLeftWrapped())
    {
      LABEL_WEST = fm.stringWidth(getMask());
    }

    return (cwidth - LABEL_EAST - LABEL_WEST) / av.getCharWidth();
  }

  /**
   * Generates a string of zeroes.
   * 
   * @return String
   */
  String getMask()
  {
    String mask = "0";
    int maxWidth = 0;
    int tmp;
    AlignmentI alignment = av.getAlignment();
    for (int i = 0; i < alignment.getHeight(); i++)
    {
      tmp = alignment.getSequenceAt(i).getEnd();
      if (tmp > maxWidth)
      {
        maxWidth = tmp;
      }
    }

    for (int i = maxWidth; i > 0; i /= 10)
    {
      mask += "0";
    }
    return mask;
  }

  private void drawWrappedPanel(Graphics g, int canvasWidth,
          int canvasHeight, int startRes)
  {
    AlignmentI al = av.getAlignment();

    FontMetrics fm = getFontMetrics(av.getFont());

    LABEL_EAST = 0;
    LABEL_WEST = 0;

    if (av.getScaleRightWrapped())
    {
      LABEL_EAST = fm.stringWidth(getMask());
    }

    if (av.getScaleLeftWrapped())
    {
      LABEL_WEST = fm.stringWidth(getMask());
    }

    int hgap = avcharHeight;
    if (av.getScaleAboveWrapped())
    {
      hgap += avcharHeight;
    }

    int cWidth = (canvasWidth - LABEL_EAST - LABEL_WEST) / avcharWidth;
    int cHeight = av.getAlignment().getHeight() * avcharHeight;

    av.setWrappedWidth(cWidth);

    av.getRanges().setViewportStartAndWidth(startRes, cWidth);

    int endx;
    int ypos = hgap;

    int maxwidth = av.getAlignment().getVisibleWidth();

    while ((ypos <= canvasHeight) && (startRes < maxwidth))
    {
      endx = startRes + cWidth - 1;

      if (endx > maxwidth)
      {
        endx = maxwidth;
      }

      g.setColor(Color.black);

      if (av.getScaleLeftWrapped())
      {
        drawWestScale(g, startRes, endx, ypos);
      }

      if (av.getScaleRightWrapped())
      {
        g.translate(canvasWidth - LABEL_EAST, 0);
        drawEastScale(g, startRes, endx, ypos);
        g.translate(-(canvasWidth - LABEL_EAST), 0);
      }

      g.translate(LABEL_WEST, 0);

      if (av.getScaleAboveWrapped())
      {
        drawNorthScale(g, startRes, endx, ypos);
      }
      if (av.hasHiddenColumns() && av.getShowHiddenMarkers())
      {
        HiddenColumns hidden = av.getAlignment().getHiddenColumns();
        g.setColor(Color.blue);
        int res;
        Iterator<Integer> it = hidden.getStartRegionIterator(startRes,
                endx + 1);
        while (it.hasNext())
        {
          res = it.next() - startRes;
          gg.fillPolygon(
                  new int[]
                  { res * avcharWidth - avcharHeight / 4,
                      res * avcharWidth + avcharHeight / 4,
                      res * avcharWidth },
                  new int[]
                  { ypos - (avcharHeight / 2), ypos - (avcharHeight / 2),
                      ypos - (avcharHeight / 2) + 8 },
                  3);
        }
      }

      if (g.getClip() == null)
      {
        g.setClip(0, 0, cWidth * avcharWidth, canvasHeight);
      }

      drawPanel(g, startRes, endx, 0, al.getHeight() - 1, ypos);
      g.setClip(null);

      if (av.isShowAnnotation())
      {
        g.translate(0, cHeight + ypos + 4);
        if (annotations == null)
        {
          annotations = new AnnotationPanel(av);
        }

        annotations.drawComponent(g, startRes, endx + 1);
        g.translate(0, -cHeight - ypos - 4);
      }
      g.translate(-LABEL_WEST, 0);

      ypos += cHeight + getAnnotationHeight() + hgap;

      startRes += cWidth;
    }

  }

  AnnotationPanel annotations;

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

  private void drawPanel(Graphics g1, final int startRes, final int endRes,
          final int startSeq, final int endSeq, final int offset)
  {

    if (!av.hasHiddenColumns())
    {
      draw(g1, startRes, endRes, startSeq, endSeq, offset);
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
        g1.translate(screenY * avcharWidth, 0);

        draw(g1, blockStart, blockEnd, startSeq, endSeq, offset);

        /*
         * draw the downline of the hidden column marker (ScalePanel draws the
         * triangle on top) if we reached it
         */
        if (av.getShowHiddenMarkers()
                && (regions.hasNext() || regions.endsAtHidden()))
        {
          g1.setColor(Color.blue);
          g1.drawLine((blockEnd - blockStart + 1) * avcharWidth - 1,
                  0 + offset, (blockEnd - blockStart + 1) * avcharWidth - 1,
                  (endSeq - startSeq + 1) * avcharHeight + offset);
        }

        g1.translate(-screenY * avcharWidth, 0);
        screenY += blockEnd - blockStart + 1;
      }
    }
  }

  // int startRes, int endRes, int startSeq, int endSeq, int x, int y,
  // int x1, int x2, int y1, int y2, int startx, int starty,
  void draw(Graphics g, int startRes, int endRes, int startSeq, int endSeq,
          int offset)
  {
    g.setFont(av.getFont());
    sr.prepare(g, av.isRenderGaps());
    updateViewport();
    SequenceI nextSeq;

    // / First draw the sequences
    // ///////////////////////////
    for (int i = startSeq; i <= endSeq; i++)
    {
      nextSeq = av.getAlignment().getSequenceAt(i);

      if (nextSeq == null)
      {
        continue;
      }

      sr.drawSequence(nextSeq, av.getAlignment().findAllGroups(nextSeq),
              startRes, endRes, offset + ((i - startSeq) * avcharHeight));

      if (av.isShowSequenceFeatures())
      {
        fr.drawSequence(g, nextSeq, startRes, endRes,
                offset + ((i - startSeq) * avcharHeight), false);
      }

      // / Highlight search Results once all sequences have been drawn
      // ////////////////////////////////////////////////////////
      if (av.hasSearchResults())
      {
        int[] visibleResults = av.getSearchResults().getResults(nextSeq,
                startRes, endRes);
        if (visibleResults != null)
        {
          for (int r = 0; r < visibleResults.length; r += 2)
          {
            sr.drawHighlightedText(nextSeq, visibleResults[r],
                    visibleResults[r + 1],
                    (visibleResults[r] - startRes) * avcharWidth,
                    offset + ((i - startSeq) * avcharHeight));
          }
        }
      }

      if (av.cursorMode && cursorY == i && cursorX >= startRes
              && cursorX <= endRes)
      {
        sr.drawCursor(nextSeq, cursorX, (cursorX - startRes) * avcharWidth,
                offset + ((i - startSeq) * avcharHeight));
      }
    }

    if (av.getSelectionGroup() != null
            || av.getAlignment().getGroups().size() > 0)
    {
      drawGroupsBoundaries(g, startRes, endRes, startSeq, endSeq, offset);
    }

  }

  private void drawGroupsBoundaries(Graphics g, int startRes, int endRes,
          int startSeq, int endSeq, int offset)
  {
    //
    // ///////////////////////////////////
    // Now outline any areas if necessary
    // ///////////////////////////////////
    SequenceGroup group = av.getSelectionGroup();

    int sx = -1;
    int sy = -1;
    int ex = -1;
    int groupIndex = -1;

    if ((group == null) && (av.getAlignment().getGroups().size() > 0))
    {
      group = av.getAlignment().getGroups().get(0);
      groupIndex = 0;
    }

    if (group != null)
    {
      do
      {
        int oldY = -1;
        int i = 0;
        boolean inGroup = false;
        int top = -1;
        int bottom = -1;
        int alHeight = av.getAlignment().getHeight() - 1;

        for (i = startSeq; i <= endSeq; i++)
        {
          sx = (group.getStartRes() - startRes) * avcharWidth;
          sy = offset + ((i - startSeq) * avcharHeight);
          ex = (((group.getEndRes() + 1) - group.getStartRes())
                  * avcharWidth) - 1;

          if (sx + ex < 0 || sx > imgWidth)
          {
            continue;
          }

          if ((sx <= (endRes - startRes) * avcharWidth)
                  && group.getSequences(null)
                          .contains(av.getAlignment().getSequenceAt(i)))
          {
            if ((bottom == -1)
                    && (i >= alHeight || !group.getSequences(null).contains(
                            av.getAlignment().getSequenceAt(i + 1))))
            {
              bottom = sy + avcharHeight;
            }

            if (!inGroup)
            {
              if (((top == -1) && (i == 0)) || !group.getSequences(null)
                      .contains(av.getAlignment().getSequenceAt(i - 1)))
              {
                top = sy;
              }

              oldY = sy;
              inGroup = true;

              if (group == av.getSelectionGroup())
              {
                g.setColor(Color.red);
              }
              else
              {
                g.setColor(group.getOutlineColour());
              }
            }
          }
          else
          {
            if (inGroup)
            {
              if (sx >= 0 && sx < imgWidth)
              {
                g.drawLine(sx, oldY, sx, sy);
              }

              if (sx + ex < imgWidth)
              {
                g.drawLine(sx + ex, oldY, sx + ex, sy);
              }

              if (sx < 0)
              {
                ex += sx;
                sx = 0;
              }

              if (sx + ex > imgWidth)
              {
                ex = imgWidth;
              }

              else if (sx + ex >= (endRes - startRes + 1) * avcharWidth)
              {
                ex = (endRes - startRes + 1) * avcharWidth;
              }

              if (top != -1)
              {
                g.drawLine(sx, top, sx + ex, top);
                top = -1;
              }

              if (bottom != -1)
              {
                g.drawLine(sx, bottom, sx + ex, bottom);
                bottom = -1;
              }

              inGroup = false;
            }
          }
        }

        if (inGroup)
        {
          sy = offset + ((i - startSeq) * avcharHeight);
          if (sx >= 0 && sx < imgWidth)
          {
            g.drawLine(sx, oldY, sx, sy);
          }

          if (sx + ex < imgWidth)
          {
            g.drawLine(sx + ex, oldY, sx + ex, sy);
          }

          if (sx < 0)
          {
            ex += sx;
            sx = 0;
          }

          if (sx + ex > imgWidth)
          {
            ex = imgWidth;
          }
          else if (sx + ex >= (endRes - startRes + 1) * avcharWidth)
          {
            ex = (endRes - startRes + 1) * avcharWidth;
          }

          if (top != -1)
          {
            g.drawLine(sx, top, sx + ex, top);
            top = -1;
          }

          if (bottom != -1)
          {
            g.drawLine(sx, bottom - 1, sx + ex, bottom - 1);
            bottom = -1;
          }

          inGroup = false;
        }

        groupIndex++;

        if (groupIndex >= av.getAlignment().getGroups().size())
        {
          break;
        }

        group = av.getAlignment().getGroups().get(groupIndex);
      } while (groupIndex < av.getAlignment().getGroups().size());

    }
  }

  public void highlightSearchResults(SearchResultsI results)
  {
    av.setSearchResults(results);
    repaint();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    String eventName = evt.getPropertyName();

    if (eventName.equals(SequenceGroup.SEQ_GROUP_CHANGED))
    {
      fastPaint = true;
      repaint();
      return;
    }
    else if (eventName.equals(ViewportRanges.MOVE_VIEWPORT))
    {
      fastPaint = false;
      repaint();
      return;
    }

    if (!av.getWrapAlignment())
    {
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
        int range = vpRanges.getEndRes() - vpRanges.getStartRes();
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
        // scroll - startres and endres both change
        fastPaint(scrollX, 0);
      }
      else if (eventName.equals(ViewportRanges.STARTSEQ))
      {
        // scroll
        fastPaint(0, (int) evt.getNewValue() - (int) evt.getOldValue());
      }
      else if (eventName.equals(ViewportRanges.STARTRESANDSEQ))
      {
        fastPaint(scrollX, 0);
      }
    }
  }

  /**
   * Ensure that a full paint is done next, for whatever reason. This was
   * necessary for JavaScript; apparently in Java the timing is just right on
   * multiple threads (EventQueue-0, Consensus, Conservation) that we can get
   * away with one fast paint before the others, but this ensures that in the
   * end we get a full paint. Problem arose in relation to copy/paste, where the
   * paste was not finalized with a full paint.
   * 
   * @author hansonr 2019.04.17
   */
  public void clearFastPaint()
  {
    fastPaint = false;
  }

}
