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
package jalview.renderer.seqfeatures;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.datamodel.ContiguousI;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.util.Comparison;
import jalview.util.ReverseListIterator;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;

public class FeatureRenderer extends FeatureRendererModel
{
  private static final AlphaComposite NO_TRANSPARENCY = AlphaComposite
          .getInstance(AlphaComposite.SRC_OVER, 1.0f);

  /**
   * Constructor given a viewport
   * 
   * @param viewport
   */
  public FeatureRenderer(AlignViewportI viewport)
  {
    this.av = viewport;
  }

  /**
   * Renders the sequence using the given feature colour between the given start
   * and end columns. Returns true if at least one column is drawn, else false
   * (the feature range does not overlap the start and end positions).
   * 
   * @param g
   * @param seq
   * @param featureStart
   * @param featureEnd
   * @param featureColour
   * @param start
   * @param end
   * @param y1
   * @param colourOnly
   * @return
   */
  boolean renderFeature(Graphics g, SequenceI seq, int featureStart,
          int featureEnd, Color featureColour, int start, int end, int y1,
          boolean colourOnly)
  {
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();
    boolean validCharWidth = av.isValidCharWidth();

    if (featureStart > end || featureEnd < start)
    {
      return false;
    }

    if (featureStart < start)
    {
      featureStart = start;
    }
    if (featureEnd >= end)
    {
      featureEnd = end;
    }
    int pady = (y1 + charHeight) - charHeight / 5;

    FontMetrics fm = g.getFontMetrics();
    for (int i = featureStart; i <= featureEnd; i++)
    {
      char s = seq.getCharAt(i);

      if (Comparison.isGap(s))
      {
        continue;
      }

      g.setColor(featureColour);

      g.fillRect((i - start) * charWidth, y1, charWidth, charHeight);

      if (colourOnly || !validCharWidth)
      {
        continue;
      }

      /*
       * JAL-3045 text is always drawn over features, even if
       * 'Show Text' is unchecked in the format menu
       */
      g.setColor(Color.white);
      int charOffset = (charWidth - fm.charWidth(s)) / 2;
      g.drawString(String.valueOf(s),
              charOffset + (charWidth * (i - start)), pady);
    }
    return true;
  }

  /**
   * Renders the sequence using the given SCORE feature colour between the given
   * start and end columns. Returns true if at least one column is drawn, else
   * false (the feature range does not overlap the start and end positions).
   * 
   * @param g
   * @param seq
   * @param fstart
   * @param fend
   * @param featureColour
   * @param start
   * @param end
   * @param y1
   * @param bs
   * @param colourOnly
   * @return
   */
  boolean renderScoreFeature(Graphics g, SequenceI seq, int fstart,
          int fend, Color featureColour, int start, int end, int y1,
          byte[] bs, boolean colourOnly)
  {
    if (fstart > end || fend < start)
    {
      return false;
    }

    if (fstart < start)
    { // fix for if the feature we have starts before the sequence start,
      fstart = start; // but the feature end is still valid!!
    }

    if (fend >= end)
    {
      fend = end;
    }
    int charHeight = av.getCharHeight();
    int pady = (y1 + charHeight) - charHeight / 5;
    int ystrt = 0, yend = charHeight;
    if (bs[0] != 0)
    {
      // signed - zero is always middle of residue line.
      if (bs[1] < 128)
      {
        yend = charHeight * (128 - bs[1]) / 512;
        ystrt = charHeight - yend / 2;
      }
      else
      {
        ystrt = charHeight / 2;
        yend = charHeight * (bs[1] - 128) / 512;
      }
    }
    else
    {
      yend = charHeight * bs[1] / 255;
      ystrt = charHeight - yend;

    }

    FontMetrics fm = g.getFontMetrics();
    int charWidth = av.getCharWidth();

    for (int i = fstart; i <= fend; i++)
    {
      char s = seq.getCharAt(i);

      if (Comparison.isGap(s))
      {
        continue;
      }

      g.setColor(featureColour);
      int x = (i - start) * charWidth;
      g.drawRect(x, y1, charWidth, charHeight);
      g.fillRect(x, y1 + ystrt, charWidth, yend);

      if (colourOnly || !av.isValidCharWidth())
      {
        continue;
      }

      g.setColor(Color.black);
      int charOffset = (charWidth - fm.charWidth(s)) / 2;
      g.drawString(String.valueOf(s),
              charOffset + (charWidth * (i - start)), pady);
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Color findFeatureColour(SequenceI seq, int column, Graphics g)
  {
    if (!av.isShowSequenceFeatures())
    {
      return null;
    }

    // column is 'base 1' but getCharAt is an array index (ie from 0)
    if (Comparison.isGap(seq.getCharAt(column - 1)))
    {
      /*
       * returning null allows the colour scheme to provide gap colour
       * - normally white, but can be customised
       */
      return null;
    }

    Color renderedColour = null;
    if (transparency == 1.0f)
    {
      /*
       * simple case - just find the topmost rendered visible feature colour
       */
      renderedColour = findFeatureColour(seq, column);
    }
    else
    {
      /*
       * transparency case - draw all visible features in render order to
       * build up a composite colour on the graphics context
       */
      renderedColour = drawSequence(g, seq, column, column, 0, true);
    }
    return renderedColour;
  }

  /**
   * Draws the sequence features on the graphics context, or just determines the
   * colour that would be drawn (if flag colourOnly is true). Returns the last
   * colour drawn (which may not be the effective colour if transparency
   * applies), or null if no feature is drawn in the range given.
   * 
   * @param g
   *          the graphics context to draw on (may be null if colourOnly==true)
   * @param seq
   * @param start
   *          start column
   * @param end
   *          end column
   * @param y1
   *          vertical offset at which to draw on the graphics
   * @param colourOnly
   *          if true, only do enough to determine the colour for the position,
   *          do not draw the character
   * @return
   */
  public synchronized Color drawSequence(final Graphics g,
          final SequenceI seq, int start, int end, int y1,
          boolean colourOnly)
  {
    /*
     * if columns are all gapped, or sequence has no features, nothing to do
     */
    ContiguousI visiblePositions = seq.findPositions(start + 1, end + 1);
    if (visiblePositions == null || !seq.getFeatures().hasFeatures()
            && !av.isShowComplementFeatures())
    {
      return null;
    }

    updateFeatures();

    if (transparency != 1f && g != null)
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
              transparency));
    }

    Color drawnColour = null;

    /*
     * draw 'complement' features below ours if configured to do so
     */
    if (av.isShowComplementFeatures()
            && !av.isShowComplementFeaturesOnTop())
    {
      drawnColour = drawComplementFeatures(g, seq, start, end, y1,
              colourOnly, visiblePositions, drawnColour);
    }

    /*
     * iterate over features in ordering of their rendering (last is on top)
     */
    for (int renderIndex = 0; renderIndex < renderOrder.length; renderIndex++)
    {
      String type = renderOrder[renderIndex];
      if (!showFeatureOfType(type))
      {
        continue;
      }

      FeatureColourI fc = getFeatureStyle(type);
      List<SequenceFeature> overlaps = seq.getFeatures().findFeatures(
              visiblePositions.getBegin(), visiblePositions.getEnd(), type);

      if (overlaps.size() > 1 && fc.isSimpleColour())
      {
        filterFeaturesForDisplay(overlaps);
      }

      for (SequenceFeature sf : overlaps)
      {
        Color featureColour = getColor(sf, fc);
        if (featureColour == null)
        {
          /*
           * feature excluded by filters, or colour threshold
           */
          continue;
        }

        /*
         * if feature starts/ends outside the visible range,
         * restrict to visible positions (or if a contact feature,
         * to a single position)
         */
        int visibleStart = sf.getBegin();
        if (visibleStart < visiblePositions.getBegin())
        {
          visibleStart = sf.isContactFeature() ? sf.getEnd()
                  : visiblePositions.getBegin();
        }
        int visibleEnd = sf.getEnd();
        if (visibleEnd > visiblePositions.getEnd())
        {
          visibleEnd = sf.isContactFeature() ? sf.getBegin()
                  : visiblePositions.getEnd();
        }

        int featureStartCol = seq.findIndex(visibleStart);
        int featureEndCol = sf.begin == sf.end ? featureStartCol
                : seq.findIndex(visibleEnd);

        // Color featureColour = getColour(sequenceFeature);

        boolean isContactFeature = sf.isContactFeature();

        if (isContactFeature)
        {
          boolean drawn = renderFeature(g, seq, featureStartCol - 1,
                  featureStartCol - 1, featureColour, start, end, y1,
                  colourOnly);
          drawn |= renderFeature(g, seq, featureEndCol - 1,
                  featureEndCol - 1, featureColour, start, end, y1,
                  colourOnly);
          if (drawn)
          {
            drawnColour = featureColour;
          }
        }
        else
        {
          /*
           * showing feature score by height of colour
           * is not implemented as a selectable option 
           *
          if (av.isShowSequenceFeaturesHeight()
                  && !Float.isNaN(sequenceFeature.score))
          {
            boolean drawn = renderScoreFeature(g, seq,
                    seq.findIndex(sequenceFeature.begin) - 1,
                    seq.findIndex(sequenceFeature.end) - 1, featureColour,
                    start, end, y1, normaliseScore(sequenceFeature),
                    colourOnly);
            if (drawn)
            {
              drawnColour = featureColour;
            }
          }
          else
          {
          */
          boolean drawn = renderFeature(g, seq, featureStartCol - 1,
                  featureEndCol - 1, featureColour, start, end, y1,
                  colourOnly);
          if (drawn)
          {
            drawnColour = featureColour;
          }
          /*}*/
        }
      }
    }

    /*
     * draw 'complement' features above ours if configured to do so
     */
    if (av.isShowComplementFeatures() && av.isShowComplementFeaturesOnTop())
    {
      drawnColour = drawComplementFeatures(g, seq, start, end, y1,
              colourOnly, visiblePositions, drawnColour);
    }

    if (transparency != 1.0f && g != null)
    {
      /*
       * reset transparency
       */
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(NO_TRANSPARENCY);
    }

    return drawnColour;
  }

  /**
   * Find any features on the CDS/protein complement of the sequence region and
   * draw them, with visibility and colouring as configured in the complementary
   * viewport
   * 
   * @param g
   * @param seq
   * @param start
   * @param end
   * @param y1
   * @param colourOnly
   * @param visiblePositions
   * @param drawnColour
   * @return
   */
  Color drawComplementFeatures(final Graphics g, final SequenceI seq,
          int start, int end, int y1, boolean colourOnly,
          ContiguousI visiblePositions, Color drawnColour)
  {
    AlignViewportI comp = av.getCodingComplement();
    FeatureRenderer fr2 = Desktop.getAlignFrameFor(comp)
            .getFeatureRenderer();

    final int visibleStart = visiblePositions.getBegin();
    final int visibleEnd = visiblePositions.getEnd();

    for (int pos = visibleStart; pos <= visibleEnd; pos++)
    {
      int column = seq.findIndex(pos);
      MappedFeatures mf = fr2.findComplementFeaturesAtResidue(seq, pos);
      if (mf != null)
      {
        for (SequenceFeature sf : mf.features)
        {
          FeatureColourI fc = fr2.getFeatureStyle(sf.getType());
          Color featureColour = fr2.getColor(sf, fc);
          renderFeature(g, seq, column - 1, column - 1, featureColour,
                  start, end, y1, colourOnly);
          drawnColour = featureColour;
        }
      }
    }
    return drawnColour;
  }

  /**
   * Called when alignment in associated view has new/modified features to
   * discover and display.
   * 
   */
  @Override
  public void featuresAdded()
  {
    findAllFeatures();
  }

  /**
   * Returns the sequence feature colour rendered at the given column position,
   * or null if none found. The feature of highest render order (i.e. on top) is
   * found, subject to both feature type and feature group being visible, and
   * its colour returned. This method is suitable when no feature transparency
   * applied (only the topmost visible feature colour is rendered).
   * <p>
   * Note this method does not check for a gap in the column so would return the
   * colour for features enclosing a gapped column. Check for gap before calling
   * if different behaviour is wanted.
   * 
   * @param seq
   * @param column
   *          (1..)
   * @return
   */
  Color findFeatureColour(SequenceI seq, int column)
  {
    /*
     * check for new feature added while processing
     */
    updateFeatures();

    /*
     * show complement features on top (if configured to show them)
     */
    if (av.isShowComplementFeatures() && av.isShowComplementFeaturesOnTop())
    {
      Color col = findComplementFeatureColour(seq, column);
      if (col != null)
      {
        return col;
      }
    }

    /*
     * inspect features in reverse renderOrder (the last in the array is 
     * displayed on top) until we find one that is rendered at the position
     */
    for (int renderIndex = renderOrder.length
            - 1; renderIndex >= 0; renderIndex--)
    {
      String type = renderOrder[renderIndex];
      if (!showFeatureOfType(type))
      {
        continue;
      }

      /*
       * find features of this type, and the colour of the _last_ one
       * (the one that would be drawn on top) that has a colour
       */
      List<SequenceFeature> overlaps = seq.findFeatures(column, column,
              type);
      for (int i = overlaps.size() - 1; i >= 0; i--)
      {
        SequenceFeature sequenceFeature = overlaps.get(i);
        if (!featureGroupNotShown(sequenceFeature))
        {
          Color col = getColour(sequenceFeature);
          if (col != null)
          {
            return col;
          }
        }
      }
    }

    /*
     * show complement features underneath (if configured to show them)
     */
    Color col = null;
    if (av.isShowComplementFeatures()
            && !av.isShowComplementFeaturesOnTop())
    {
      col = findComplementFeatureColour(seq, column);
    }

    return col;
  }

  Color findComplementFeatureColour(SequenceI seq, int column)
  {
    AlignViewportI complement = av.getCodingComplement();
    AlignFrame af = Desktop.getAlignFrameFor(complement);
    FeatureRendererModel fr2 = af.getFeatureRenderer();
    MappedFeatures mf = fr2.findComplementFeaturesAtResidue(seq,
            seq.findPosition(column - 1));
    if (mf == null)
    {
      return null;
    }
    ReverseListIterator<SequenceFeature> it = new ReverseListIterator<>(
            mf.features);
    while (it.hasNext())
    {
      SequenceFeature sf = it.next();
      if (!fr2.featureGroupNotShown(sf))
      {
        Color col = fr2.getColour(sf);
        if (col != null)
        {
          return col;
        }
      }
    }
    return null;
  }
}
