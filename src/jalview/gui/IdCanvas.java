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
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.JPanel;

import jalview.datamodel.SequenceI;
import jalview.viewmodel.ViewportListenerI;
import jalview.viewmodel.ViewportRanges;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdCanvas extends JPanel implements ViewportListenerI
{
  protected AlignViewport av;

  protected boolean showScores = true;

  protected int maxIdLength = -1;

  protected String maxIdStr = null;

  BufferedImage image;

  // Graphics2D gg;

  int imgHeight = 0;

  boolean fastPaint = false;

  List<SequenceI> searchResults;

  AnnotationPanel ap;

  private Font idfont;

  /**
   * Creates a new IdCanvas object.
   * 
   * @param av
   *          DOCUMENT ME!
   */
  public IdCanvas(AlignViewport av)
  {
    setLayout(new BorderLayout());
    this.av = av;
    PaintRefresher.Register(this, av.getSequenceSetId());
    av.getRanges().addPropertyChangeListener(this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param hiddenRows
   *          true - check and display hidden row marker if need be
   * @param s
   *          DOCUMENT ME!
   * @param i
   *          DOCUMENT ME!
   * @param starty
   *          DOCUMENT ME!
   * @param ypos
   *          DOCUMENT ME!
   */
  public void drawIdString(Graphics2D g, boolean hiddenRows, SequenceI s,
          int i, int starty, int ypos)
  {
    int xPos = 0;
    int panelWidth = getWidth();
    int charHeight = av.getCharHeight();

    if ((searchResults != null) && searchResults.contains(s))
    {
      g.setColor(Color.black);
      g.fillRect(0, ((i - starty) * charHeight) + ypos, getWidth(),
              charHeight);
      g.setColor(Color.white);
    }
    else if ((av.getSelectionGroup() != null)
            && av.getSelectionGroup().getSequences(null).contains(s))
    {
      g.setColor(Color.lightGray);
      g.fillRect(0, ((i - starty) * charHeight) + ypos, getWidth(),
              charHeight);
      g.setColor(Color.white);
    }
    else
    {
      g.setColor(av.getSequenceColour(s));
      g.fillRect(0, ((i - starty) * charHeight) + ypos, getWidth(),
              charHeight);
      g.setColor(Color.black);
    }

    if (av.isRightAlignIds())
    {
      FontMetrics fm = g.getFontMetrics();
      xPos = panelWidth
              - fm.stringWidth(s.getDisplayId(av.getShowJVSuffix())) - 4;
    }

    g.drawString(s.getDisplayId(av.getShowJVSuffix()), xPos,
            (((i - starty + 1) * charHeight) + ypos) - (charHeight / 5));

    if (hiddenRows && av.getShowHiddenMarkers())
    {
      drawMarker(g, av, i, starty, ypos);
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param vertical
   *          DOCUMENT ME!
   */
  public void fastPaint(int vertical)
  {

    /*
     * for now, not attempting fast paint of wrapped ids...
     */
    if (image == null || av.getWrapAlignment())
    {
      repaint();

      return;
    }

    ViewportRanges ranges = av.getRanges();

    Graphics2D gg = image.createGraphics();
    gg.copyArea(0, 0, getWidth(), imgHeight, 0,
            -vertical * av.getCharHeight());

    int ss = ranges.getStartSeq();
    int es = ranges.getEndSeq();
    int transY = 0;

    if (vertical > 0) // scroll down
    {
      ss = es - vertical;

      if (ss < ranges.getStartSeq())
      { // ie scrolling too fast, more than a page at a time
        ss = ranges.getStartSeq();
      }
      else
      {
        transY = imgHeight - ((vertical + 1) * av.getCharHeight());
      }
    }
    else if (vertical < 0) // scroll up
    {
      es = ss - vertical;

      if (es > ranges.getEndSeq())
      {
        es = ranges.getEndSeq();
      }
    }

    gg.translate(0, transY);

    drawIds(gg, av, ss, es, searchResults);

    gg.translate(0, -transY);

    gg.dispose();

    fastPaint = true;

    // Call repaint on alignment panel so that repaints from other alignment
    // panel components can be aggregated. Otherwise performance of the overview
    // window and others may be adversely affected.
    av.getAlignPanel().repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    if (fastPaint)
    {
      fastPaint = false;
      g.drawImage(image, 0, 0, this);

      return;
    }

    int oldHeight = imgHeight;

    imgHeight = getHeight();
    imgHeight -= (imgHeight % av.getCharHeight());

    if (imgHeight < 1)
    {
      return;
    }

    if (oldHeight != imgHeight || image.getWidth(this) != getWidth())
    {
      image = new BufferedImage(getWidth(), imgHeight,
              BufferedImage.TYPE_INT_RGB);
    }

    Graphics2D gg = image.createGraphics();

    // Fill in the background
    gg.setColor(Color.white);
    gg.fillRect(0, 0, getWidth(), imgHeight);

    drawIds(gg, av, av.getRanges().getStartSeq(),
            av.getRanges().getEndSeq(), searchResults);

    gg.dispose();

    g.drawImage(image, 0, 0, this);
  }

  /**
   * Draws sequence ids from sequence index startSeq to endSeq (inclusive), with
   * the font and other display settings configured on the viewport. Ids of
   * sequences included in the selection are coloured grey, otherwise the
   * current id colour for the sequence id is used.
   * 
   * @param g
   * @param alignViewport
   * @param startSeq
   * @param endSeq
   * @param selection
   */
  void drawIds(Graphics2D g, AlignViewport alignViewport,
          final int startSeq, final int endSeq, List<SequenceI> selection)
  {
    Font font = alignViewport.getFont();
    if (alignViewport.isSeqNameItalics())
    {
      setIdfont(new Font(font.getName(), Font.ITALIC, font.getSize()));
    }
    else
    {
      setIdfont(font);
    }

    g.setFont(getIdfont());
    FontMetrics fm = g.getFontMetrics();

    if (alignViewport.antiAlias)
    {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    }

    Color currentColor = Color.white;
    Color currentTextColor = Color.black;

    boolean hasHiddenRows = alignViewport.hasHiddenRows();

    if (alignViewport.getWrapAlignment())
    {
      drawIdsWrapped(g, alignViewport, startSeq, getHeight());
      return;
    }

    // Now draw the id strings
    int panelWidth = getWidth();
    int xPos = 0;

    // Now draw the id strings
    for (int i = startSeq; i <= endSeq; i++)
    {
      SequenceI sequence = alignViewport.getAlignment().getSequenceAt(i);

      if (sequence == null)
      {
        continue;
      }

      if (hasHiddenRows || alignViewport.isDisplayReferenceSeq())
      {
        g.setFont(getHiddenFont(sequence, alignViewport));
        fm = g.getFontMetrics();
      }

      // Selected sequence colours
      if (selection != null && selection.contains(sequence))
      {
        currentColor = Color.black;
        currentTextColor = Color.white;
      }
      else if ((alignViewport.getSelectionGroup() != null) && alignViewport
              .getSelectionGroup().getSequences(null).contains(sequence))
      {
        currentColor = Color.lightGray;
        currentTextColor = Color.black;
      }
      else
      {
        currentColor = alignViewport.getSequenceColour(sequence);
        currentTextColor = Color.black;
      }

      g.setColor(currentColor);

      int charHeight = alignViewport.getCharHeight();
      g.fillRect(0, (i - startSeq) * charHeight, getWidth(), charHeight);

      g.setColor(currentTextColor);

      String string = sequence
              .getDisplayId(alignViewport.getShowJVSuffix());

      if (alignViewport.isRightAlignIds())
      {
        xPos = panelWidth - fm.stringWidth(string) - 4;
      }

      g.drawString(string, xPos,
              (((i - startSeq) * charHeight) + charHeight)
                      - (charHeight / 5));

      if (hasHiddenRows && av.getShowHiddenMarkers())
      {
        drawMarker(g, alignViewport, i, startSeq, 0);
      }
    }
  }

  /**
   * Draws sequence ids, and annotation labels if annotations are shown, in
   * wrapped mode
   * 
   * @param g
   * @param alignViewport
   * @param startSeq
   */
  void drawIdsWrapped(Graphics2D g, AlignViewport alignViewport,
          int startSeq, int pageHeight)
  {
    int alignmentWidth = alignViewport.getAlignment().getWidth();
    final int alheight = alignViewport.getAlignment().getHeight();

    /*
     * assumption: SeqCanvas.calculateWrappedGeometry has been called
     */
    SeqCanvas seqCanvas = alignViewport.getAlignPanel()
            .getSeqPanel().seqCanvas;

    final int charHeight = alignViewport.getCharHeight();

    AnnotationLabels labels = null;
    if (alignViewport.isShowAnnotation())
    {
      labels = new AnnotationLabels(alignViewport);
    }

    ViewportRanges ranges = alignViewport.getRanges();

    int rowSize = ranges.getViewportWidth();

    /*
     * draw repeating sequence ids until out of sequence data or
     * out of visible space, whichever comes first
     */
    boolean hasHiddenRows = alignViewport.hasHiddenRows();
    int ypos = seqCanvas.wrappedSpaceAboveAlignment;
    int rowStartRes = ranges.getStartRes();
    while ((ypos <= pageHeight) && (rowStartRes < alignmentWidth))
    {
      for (int i = startSeq; i < alheight; i++)
      {
        SequenceI s = alignViewport.getAlignment().getSequenceAt(i);
        if (hasHiddenRows || alignViewport.isDisplayReferenceSeq())
        {
          g.setFont(getHiddenFont(s, alignViewport));
        }
        else
        {
          g.setFont(getIdfont());
        }
        drawIdString(g, hasHiddenRows, s, i, 0, ypos);
      }

      if (labels != null && alignViewport.isShowAnnotation())
      {
        g.translate(0, ypos + (alheight * charHeight));
        labels.drawComponent(g, getWidth());
        g.translate(0, -ypos - (alheight * charHeight));
      }

      ypos += seqCanvas.wrappedRepeatHeightPx;
      rowStartRes += rowSize;
    }
  }

  /**
   * Draws a marker (a blue right-pointing triangle) between sequences to
   * indicate hidden sequences.
   * 
   * @param g
   * @param alignViewport
   * @param seqIndex
   * @param starty
   * @param yoffset
   */
  void drawMarker(Graphics2D g, AlignViewport alignViewport, int seqIndex,
          int starty, int yoffset)
  {
    SequenceI[] hseqs = alignViewport.getAlignment()
            .getHiddenSequences().hiddenSequences;
    // Use this method here instead of calling hiddenSeq adjust
    // 3 times.
    int hSize = hseqs.length;

    int hiddenIndex = seqIndex;
    int lastIndex = seqIndex - 1;
    int nextIndex = seqIndex + 1;

    for (int j = 0; j < hSize; j++)
    {
      if (hseqs[j] != null)
      {
        if (j - 1 < hiddenIndex)
        {
          hiddenIndex++;
        }
        if (j - 1 < lastIndex)
        {
          lastIndex++;
        }
        if (j - 1 < nextIndex)
        {
          nextIndex++;
        }
      }
    }

    /*
     * are we below or above the hidden sequences?
     */
    boolean below = (hiddenIndex > lastIndex + 1);
    boolean above = (nextIndex > hiddenIndex + 1);

    g.setColor(Color.blue);
    int charHeight = av.getCharHeight();

    /*
     * vertices of the triangle, below or above hidden seqs
     */
    int[] xPoints = new int[] { getWidth() - charHeight,
        getWidth() - charHeight, getWidth() };
    int yShift = seqIndex - starty;

    if (below)
    {
      int[] yPoints = new int[] { yShift * charHeight + yoffset,
          yShift * charHeight + yoffset + charHeight / 4,
          yShift * charHeight + yoffset };
      g.fillPolygon(xPoints, yPoints, 3);
    }
    if (above)
    {
      yShift++;
      int[] yPoints = new int[] { yShift * charHeight + yoffset,
          yShift * charHeight + yoffset - charHeight / 4,
          yShift * charHeight + yoffset };
      g.fillPolygon(xPoints, yPoints, 3);
    }
  }

  /**
   * Answers the standard sequence id font, or a bold font if the sequence is
   * set as reference or a hidden group representative
   * 
   * @param seq
   * @param alignViewport
   * @return
   */
  private Font getHiddenFont(SequenceI seq, AlignViewport alignViewport)
  {
    if (av.isReferenceSeq(seq) || av.isHiddenRepSequence(seq))
    {
      return new Font(av.getFont().getName(), Font.BOLD,
              av.getFont().getSize());
    }
    return getIdfont();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param list
   *          DOCUMENT ME!
   */
  public void setHighlighted(List<SequenceI> list)
  {
    searchResults = list;
    repaint();
  }

  public Font getIdfont()
  {
    return idfont;
  }

  public void setIdfont(Font idfont)
  {
    this.idfont = idfont;
  }

  /**
   * Respond to viewport range changes (e.g. alignment panel was scrolled). Both
   * scrolling and resizing change viewport ranges. Scrolling changes both start
   * and end points, but resize only changes end values. Here we only want to
   * fastpaint on a scroll, with resize using a normal paint, so scroll events
   * are identified as changes to the horizontal or vertical start value.
   * <p>
   * In unwrapped mode, only responds to a vertical scroll, as horizontal scroll
   * leaves sequence ids unchanged. In wrapped mode, only vertical scroll is
   * provided, but it generates a change of "startres" which does require an
   * update here.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    String propertyName = evt.getPropertyName();
    if (propertyName.equals(ViewportRanges.STARTSEQ)
            || (av.getWrapAlignment()
                    && propertyName.equals(ViewportRanges.STARTRES)))
    {
      fastPaint((int) evt.getNewValue() - (int) evt.getOldValue());
    }
    else if (propertyName.equals(ViewportRanges.STARTRESANDSEQ))
    {
      fastPaint(((int[]) evt.getNewValue())[1]
              - ((int[]) evt.getOldValue())[1]);
    }
    else if (propertyName.equals(ViewportRanges.MOVE_VIEWPORT))
    {
      repaint();
    }
  }
}
