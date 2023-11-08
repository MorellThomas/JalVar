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

import jalview.api.AlignViewportI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.ResidueColourFinder;
import jalview.renderer.seqfeatures.FeatureColourFinder;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jibble.epsgraphics.EpsGraphics2D;

public class SequenceRenderer implements jalview.api.SequenceRenderer
{
  final static int CHAR_TO_UPPER = 'A' - 'a';

  AlignViewportI av;

  FontMetrics fm;

  boolean renderGaps = true;

  SequenceGroup[] allGroups = null;

  // Color resBoxColour;

  Graphics graphics;

  boolean monospacedFont;

  ResidueColourFinder resColourFinder;

  /**
   * Creates a new SequenceRenderer object
   * 
   * @param viewport
   */
  public SequenceRenderer(AlignViewportI viewport)
  {
    this.av = viewport;
    resColourFinder = new ResidueColourFinder();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void prepare(Graphics g, boolean renderGaps)
  {
    graphics = g;
    fm = g.getFontMetrics();

    // If EPS graphics, stringWidth will be a double, not an int
    double dwidth = fm.getStringBounds("M", g).getWidth();

    monospacedFont = (dwidth == fm.getStringBounds("|", g).getWidth()
            && av.getCharWidth() == dwidth);

    this.renderGaps = renderGaps;
  }

  /**
   * Get the residue colour at the given sequence position - as determined by
   * the sequence group colour (if any), else the colour scheme, possibly
   * overridden by a feature colour.
   * 
   * @param seq
   * @param position
   * @param finder
   * @return
   */
  @Override
  public Color getResidueColour(final SequenceI seq, int position,
          FeatureColourFinder finder)
  {
    allGroups = av.getAlignment().findAllGroups(seq);
    return resColourFinder.getResidueColour(av.getShowBoxes(),
            av.getResidueShading(), allGroups, seq, position, finder);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param seq
   *          DOCUMENT ME!
   * @param sg
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void drawSequence(SequenceI seq, SequenceGroup[] sg, int start,
          int end, int y1)
  {
    allGroups = sg;

    drawBoxes(seq, start, end, y1);

    if (av.isValidCharWidth())
    {
      drawText(seq, start, end, y1);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public synchronized void drawBoxes(SequenceI seq, int start, int end,
          int y1)
  {
    Color resBoxColour = Color.white;

    if (seq == null)
    {
      return; // fix for racecondition
    }
    int i = start;
    int length = seq.getLength();

    int curStart = -1;
    int curWidth = av.getCharWidth(), avWidth = av.getCharWidth(),
            avHeight = av.getCharHeight();

    Color tempColour = null;

    while (i <= end)
    {
      resBoxColour = Color.white;

      if (i < length)
      {
        SequenceGroup currentSequenceGroup = resColourFinder
                .getCurrentSequenceGroup(allGroups, i);
        if (currentSequenceGroup != null)
        {
          if (currentSequenceGroup.getDisplayBoxes())
          {
            resBoxColour = resColourFinder.getBoxColour(
                    currentSequenceGroup.getGroupColourScheme(), seq, i);
          }
        }
        else if (av.getShowBoxes())
        {
          resBoxColour = resColourFinder
                  .getBoxColour(av.getResidueShading(), seq, i);
        }
      }

      if (resBoxColour != tempColour)
      {
        if (tempColour != null)
        {
          graphics.fillRect(avWidth * (curStart - start), y1, curWidth,
                  avHeight);
        }

        graphics.setColor(resBoxColour);

        curStart = i;
        curWidth = avWidth;
        tempColour = resBoxColour;
      }
      else
      {
        curWidth += avWidth;
      }

      i++;
    }

    graphics.fillRect(avWidth * (curStart - start), y1, curWidth, avHeight);

  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void drawText(SequenceI seq, int start, int end, int y1)
  {
    y1 += av.getCharHeight() - av.getCharHeight() / 5; // height/5 replaces pady
    int charOffset = 0;
    char s;

    if (end + 1 >= seq.getLength())
    {
      end = seq.getLength() - 1;
    }
    graphics.setColor(av.getTextColour());

    boolean drawAllText = monospacedFont && av.getShowText()
            && allGroups.length == 0 && !av.getColourText()
            && av.getThresholdTextColour() == 0;

    /*
     * EPS or SVG misaligns monospaced strings (JAL-3239)
     * so always draw these one character at a time
     */
    if (graphics instanceof EpsGraphics2D
            || graphics instanceof SVGGraphics2D)
    {
      drawAllText = false;
    }
    if (drawAllText)
    {
      if (av.isRenderGaps())
      {
        graphics.drawString(seq.getSequenceAsString(start, end + 1), 0, y1);
      }
      else
      {
        char gap = av.getGapCharacter();
        graphics.drawString(
                seq.getSequenceAsString(start, end + 1).replace(gap, ' '),
                0, y1);
      }
    }
    else
    {
      boolean srep = av.isDisplayReferenceSeq();
      boolean getboxColour = false;
      boolean isarep = av.getAlignment().getSeqrep() == seq;
      Color resBoxColour = Color.white;

      for (int i = start; i <= end; i++)
      {

        graphics.setColor(av.getTextColour());
        getboxColour = false;
        s = seq.getCharAt(i);

        if (!renderGaps && jalview.util.Comparison.isGap(s))
        {
          continue;
        }

        SequenceGroup currentSequenceGroup = resColourFinder
                .getCurrentSequenceGroup(allGroups, i);
        if (currentSequenceGroup != null)
        {
          if (!currentSequenceGroup.getDisplayText())
          {
            continue;
          }

          if (currentSequenceGroup.thresholdTextColour > 0
                  || currentSequenceGroup.getColourText())
          {
            getboxColour = true;
            resBoxColour = resColourFinder.getBoxColour(
                    currentSequenceGroup.getGroupColourScheme(), seq, i);

            if (currentSequenceGroup.getColourText())
            {
              graphics.setColor(resBoxColour.darker());
            }

            if (currentSequenceGroup.thresholdTextColour > 0)
            {
              if (resBoxColour.getRed() + resBoxColour.getBlue()
                      + resBoxColour
                              .getGreen() < currentSequenceGroup.thresholdTextColour)
              {
                graphics.setColor(currentSequenceGroup.textColour2);
              }
            }
          }
          else
          {
            graphics.setColor(currentSequenceGroup.textColour);
          }
          boolean isgrep = currentSequenceGroup != null
                  ? currentSequenceGroup.getSeqrep() == seq
                  : false;
          if (!isarep && !isgrep
                  && currentSequenceGroup.getShowNonconserved()) // todo
                                                                 // optimize
          {
            // todo - use sequence group consensus
            s = getDisplayChar(srep, i, s, '.', currentSequenceGroup);

          }

        }
        else
        {
          if (!av.getShowText())
          {
            continue;
          }

          if (av.getColourText())
          {
            getboxColour = true;
            resBoxColour = resColourFinder
                    .getBoxColour(av.getResidueShading(), seq, i);

            if (av.getShowBoxes())
            {
              graphics.setColor(resBoxColour.darker());
            }
            else
            {
              graphics.setColor(resBoxColour);
            }
          }

          if (av.getThresholdTextColour() > 0)
          {
            if (!getboxColour)
            {
              resBoxColour = resColourFinder
                      .getBoxColour(av.getResidueShading(), seq, i);
            }

            if (resBoxColour.getRed() + resBoxColour.getBlue()
                    + resBoxColour.getGreen() < av.getThresholdTextColour())
            {
              graphics.setColor(av.getTextColour2());
            }
          }
          if (!isarep && av.getShowUnconserved())
          {
            s = getDisplayChar(srep, i, s, '.', null);

          }

        }

        charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
        graphics.drawString(String.valueOf(s),
                charOffset + av.getCharWidth() * (i - start), y1);

      }
    }
  }

  /**
   * Returns 'conservedChar' to represent the given position if the sequence
   * character at that position is equal to the consensus (ignoring case), else
   * returns the sequence character
   * 
   * @param usesrep
   * @param position
   * @param sequenceChar
   * @param conservedChar
   * @return
   */
  private char getDisplayChar(final boolean usesrep, int position,
          char sequenceChar, char conservedChar, SequenceGroup currentGroup)
  {
    // TODO - use currentSequenceGroup rather than alignment
    // currentSequenceGroup.getConsensus()
    char conschar = (usesrep) ? (currentGroup == null
            || position < currentGroup.getStartRes()
            || position > currentGroup.getEndRes()
                    ? av.getAlignment().getSeqrep().getCharAt(position)
                    : (currentGroup.getSeqrep() != null
                            ? currentGroup.getSeqrep().getCharAt(position)
                            : av.getAlignment().getSeqrep()
                                    .getCharAt(position)))
            : (currentGroup != null && currentGroup.getConsensus() != null
                    && position >= currentGroup.getStartRes()
                    && position <= currentGroup.getEndRes()
                    && currentGroup
                            .getConsensus().annotations.length > position)
                                    ? currentGroup
                                            .getConsensus().annotations[position].displayCharacter
                                                    .charAt(0)
                                    : av.getAlignmentConsensusAnnotation().annotations[position].displayCharacter
                                            .charAt(0);
    if (!jalview.util.Comparison.isGap(conschar)
            && (sequenceChar == conschar
                    || sequenceChar + CHAR_TO_UPPER == conschar))
    {
      sequenceChar = conservedChar;
    }
    return sequenceChar;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void drawHighlightedText(SequenceI seq, int start, int end, int x1,
          int y1)
  {
    int pady = av.getCharHeight() / 5;
    int charOffset = 0;
    graphics.setColor(Color.BLACK);
    graphics.fillRect(x1, y1, av.getCharWidth() * (end - start + 1),
            av.getCharHeight());
    graphics.setColor(Color.white);

    char s = '~';

    // Need to find the sequence position here.
    if (av.isValidCharWidth())
    {
      for (int i = start; i <= end; i++)
      {
        if (i < seq.getLength())
        {
          s = seq.getCharAt(i);
        }

        charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
        graphics.drawString(String.valueOf(s),
                charOffset + x1 + (av.getCharWidth() * (i - start)),
                (y1 + av.getCharHeight()) - pady);
      }
    }
  }

  /**
   * Draw a sequence canvas cursor
   * 
   * @param g
   *          graphics context to draw on
   * @param s
   *          character to draw at cursor
   * @param x1
   *          x position of cursor in graphics context
   * @param y1
   *          y position of cursor in graphics context
   */
  public void drawCursor(Graphics g, char s, int x1, int y1)
  {
    int pady = av.getCharHeight() / 5;
    int charOffset = 0;
    g.setColor(Color.black);
    g.fillRect(x1, y1, av.getCharWidth(), av.getCharHeight());

    if (av.isValidCharWidth())
    {
      g.setColor(Color.white);
      charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
      g.drawString(String.valueOf(s), charOffset + x1,
              (y1 + av.getCharHeight()) - pady);
    }

  }
}
