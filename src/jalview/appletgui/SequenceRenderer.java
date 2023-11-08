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

import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.ResidueColourFinder;
import jalview.renderer.seqfeatures.FeatureColourFinder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class SequenceRenderer implements jalview.api.SequenceRenderer
{
  final static int CHAR_TO_UPPER = 'A' - 'a';

  AlignViewport av;

  FontMetrics fm;

  boolean renderGaps = true;

  SequenceGroup[] allGroups = null;

  Color resBoxColour;

  Graphics graphics;

  ResidueColourFinder resColourFinder;

  public SequenceRenderer(AlignViewport av)
  {
    this.av = av;
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
    // TODO replace 8 or so code duplications with calls to this method
    // (refactored as needed)
    return resColourFinder.getResidueColour(av.getShowBoxes(),
            av.getResidueShading(), allGroups, seq, position, finder);
  }

  public Color findSequenceColour(SequenceI seq, int i)
  {
    allGroups = av.getAlignment().findAllGroups(seq);
    drawBoxes(seq, i, i, 0);
    return resBoxColour;
  }

  public void drawSequence(SequenceI seq, SequenceGroup[] sg, int start,
          int end, int y1)
  {
    if (seq == null)
    {
      return;
    }

    allGroups = sg;

    drawBoxes(seq, start, end, y1);

    if (av.validCharWidth)
    {
      drawText(seq, start, end, y1);
    }
  }

  public void drawBoxes(SequenceI seq, int start, int end, int y1)
  {
    int i = start;
    int length = seq.getLength();

    int curStart = -1;
    int curWidth = av.getCharWidth(), avCharWidth = av.getCharWidth(),
            avCharHeight = av.getCharHeight();

    Color resBoxColour = Color.white;
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
          graphics.fillRect(avCharWidth * (curStart - start), y1, curWidth,
                  avCharHeight);
        }
        graphics.setColor(resBoxColour);

        curStart = i;
        curWidth = avCharWidth;
        tempColour = resBoxColour;

      }
      else
      {
        curWidth += avCharWidth;
      }

      i++;
    }

    graphics.fillRect(avCharWidth * (curStart - start), y1, curWidth,
            avCharHeight);
  }

  public void drawText(SequenceI seq, int start, int end, int y1)
  {
    int avCharWidth = av.getCharWidth(), avCharHeight = av.getCharHeight();
    Font boldFont = null;
    boolean bold = false;
    if (av.isUpperCasebold())
    {
      boldFont = new Font(av.getFont().getName(), Font.BOLD, avCharHeight);

      graphics.setFont(av.getFont());
    }

    y1 += avCharHeight - avCharHeight / 5; // height/5 replaces pady

    int charOffset = 0;

    // Need to find the sequence position here.
    if (end + 1 >= seq.getLength())
    {
      end = seq.getLength() - 1;
    }

    char s = ' ';
    boolean srep = av.isDisplayReferenceSeq();
    for (int i = start; i <= end; i++)
    {
      graphics.setColor(Color.black);

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

        if (currentSequenceGroup.getColourText())
        {
          resBoxColour = resColourFinder.getBoxColour(
                  currentSequenceGroup.getGroupColourScheme(), seq, i);
          graphics.setColor(resBoxColour.darker());
        }
        if (currentSequenceGroup.getShowNonconserved())
        {
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
        if (av.getShowUnconserved())
        {
          s = getDisplayChar(srep, i, s, '.', null);

        }
      }

      if (av.isUpperCasebold())
      {
        fm = graphics.getFontMetrics();
        if ('A' <= s && s <= 'Z')
        {
          if (!bold)
          {

            graphics.setFont(boldFont);
          }
          bold = true;
        }
        else if (bold)
        {
          graphics.setFont(av.font);
          bold = false;
        }

      }

      charOffset = (avCharWidth - fm.charWidth(s)) / 2;
      graphics.drawString(String.valueOf(s),
              charOffset + avCharWidth * (i - start), y1);
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

  public void drawHighlightedText(SequenceI seq, int start, int end, int x1,
          int y1)
  {
    int avCharWidth = av.getCharWidth(), avCharHeight = av.getCharHeight();
    int pady = avCharHeight / 5;
    int charOffset = 0;
    graphics.setColor(Color.black);
    graphics.fillRect(x1, y1, avCharWidth * (end - start + 1),
            avCharHeight);
    graphics.setColor(Color.white);

    char s = '~';
    // Need to find the sequence position here.
    if (av.validCharWidth)
    {
      for (int i = start; i <= end; i++)
      {
        if (i < seq.getLength())
        {
          s = seq.getCharAt(i);
        }

        charOffset = (avCharWidth - fm.charWidth(s)) / 2;
        graphics.drawString(String.valueOf(s),
                charOffset + x1 + avCharWidth * (i - start),
                y1 + avCharHeight - pady);
      }
    }
  }

  public void drawCursor(SequenceI seq, int res, int x1, int y1)
  {
    int pady = av.getCharHeight() / 5;
    int charOffset = 0;
    graphics.setColor(Color.black);
    graphics.fillRect(x1, y1, av.getCharWidth(), av.getCharHeight());
    graphics.setColor(Color.white);

    graphics.setColor(Color.white);

    char s = seq.getCharAt(res);
    if (av.validCharWidth)
    {

      charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
      graphics.drawString(String.valueOf(s), charOffset + x1,
              (y1 + av.getCharHeight()) - pady);
    }
  }

}
