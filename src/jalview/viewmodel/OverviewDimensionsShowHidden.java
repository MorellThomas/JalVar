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
package jalview.viewmodel;

import jalview.api.AlignmentColsCollectionI;
import jalview.api.AlignmentRowsCollectionI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AllColsCollection;
import jalview.datamodel.AllRowsCollection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.HiddenSequences;

public class OverviewDimensionsShowHidden extends OverviewDimensions
{
  private ViewportRanges ranges;

  private int xdiff; // when dragging, difference in alignment units between
                     // start residue and original mouse click position

  private int ydiff; // when dragging, difference in alignment units between
                     // start sequence and original mouse click position

  /**
   * Create an OverviewDimensions object
   * 
   * @param ranges
   *          positional properties of the viewport
   * @param showAnnotationPanel
   *          true if the annotation panel is to be shown, false otherwise
   */
  public OverviewDimensionsShowHidden(ViewportRanges vpranges,
          boolean showAnnotationPanel)
  {
    super(vpranges, showAnnotationPanel);
    ranges = vpranges;
    resetAlignmentDims();
  }

  /**
   * Check box dimensions and scroll positions and correct if necessary
   * 
   * @param mousex
   *          x position in overview panel
   * @param mousey
   *          y position in overview panel
   * @param hiddenSeqs
   *          hidden sequences
   * @param hiddenCols
   *          hidden columns
   * @param ranges
   *          viewport position properties
   */
  @Override
  public void updateViewportFromMouse(int mousex, int mousey,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols)
  {
    resetAlignmentDims();

    // convert mousex and mousey to alignment units as well as
    // translating to top left corner of viewport - this is an absolute position
    int xAsRes = getLeftXFromCentreX(mousex, hiddenCols);
    int yAsSeq = getTopYFromCentreY(mousey, hiddenSeqs);

    // convert to visible positions
    int visXAsRes = hiddenCols.absoluteToVisibleColumn(xAsRes);
    yAsSeq = hiddenSeqs.adjustForHiddenSeqs(
            hiddenSeqs.findIndexWithoutHiddenSeqs(yAsSeq));
    yAsSeq = Math.max(yAsSeq, 0); // -1 if before first visible sequence
    int visYAsSeq = hiddenSeqs.findIndexWithoutHiddenSeqs(yAsSeq);
    visYAsSeq = Math.max(visYAsSeq, 0); // -1 if before first visible sequence

    // update viewport accordingly
    updateViewportFromTopLeft(visXAsRes, visYAsSeq, hiddenSeqs, hiddenCols);
  }

  @Override
  public void adjustViewportFromMouse(int mousex, int mousey,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols)
  {
    resetAlignmentDims();

    // calculate translation in pixel terms:
    // get mouse location in viewport coords, add translation in viewport
    // coords,
    // convert back to pixel coords
    int vpx = Math.round((float) mousex * alwidth / width);
    int visXAsRes = hiddenCols.absoluteToVisibleColumn(vpx) + xdiff;

    int vpy = Math.round(mousey * heightRatio);
    int visYAsRes = hiddenSeqs.findIndexWithoutHiddenSeqs(vpy) + ydiff;

    // update viewport accordingly
    updateViewportFromTopLeft(visXAsRes, visYAsRes, hiddenSeqs, hiddenCols);
  }

  /**
   * {@inheritDoc} Callers should have already called resetAlignmentDims to
   * refresh alwidth, alheight and width/height ratios
   */
  @Override
  protected void updateViewportFromTopLeft(int leftx, int topy,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols)
  {
    int visXAsRes = leftx;
    int visYAsSeq = topy;

    if (visXAsRes < 0)
    {
      visXAsRes = 0;
    }

    if (visYAsSeq < 0)
    {
      visYAsSeq = 0;
    }

    if (ranges.isWrappedMode())
    {
      visYAsSeq = 0; // sorry, no vertical scroll when wrapped
    }

    // Determine where scrollCol should be, given visXAsRes

    // get viewport width in residues
    int vpwidth = ranges.getViewportWidth();

    // check in case we went off the edge of the alignment
    int visAlignWidth = hiddenCols.absoluteToVisibleColumn(alwidth - 1);
    if (visXAsRes + vpwidth - 1 > visAlignWidth)
    {
      // went past the end of the alignment, adjust backwards

      // if last position was before the end of the alignment, need to update
      if (ranges.getEndRes() < visAlignWidth)
      {
        visXAsRes = hiddenCols.absoluteToVisibleColumn(hiddenCols
                .offsetByVisibleColumns(-(vpwidth - 1), alwidth - 1));
      }
      else
      {
        visXAsRes = ranges.getStartRes();
      }
    }

    // Determine where scrollRow should be, given visYAsSeq

    // get viewport height in sequences
    int vpheight = ranges.getViewportHeight();

    // check in case we went off the edge of the alignment
    int visAlignHeight = hiddenSeqs.findIndexWithoutHiddenSeqs(alheight);

    if (visYAsSeq + vpheight - 1 > visAlignHeight)
    {
      // went past the end of the alignment, adjust backwards
      if (ranges.getEndSeq() < visAlignHeight)
      {
        visYAsSeq = hiddenSeqs.findIndexWithoutHiddenSeqs(
                hiddenSeqs.subtractVisibleRows(vpheight - 1, alheight - 1));
      }
      else
      {
        visYAsSeq = ranges.getStartSeq();
      }
    }

    // update viewport
    ranges.setStartResAndSeq(visXAsRes, visYAsSeq);
  }

  /**
   * Update the overview panel box when the associated alignment panel is
   * changed
   * 
   * @param hiddenSeqs
   *          hidden sequences
   * @param hiddenCols
   *          hidden columns
   * @param ranges
   *          viewport position properties
   */
  @Override
  public void setBoxPosition(HiddenSequences hiddenSeqs,
          HiddenColumns hiddenCols)
  {
    // work with absolute values of startRes and endRes
    int startRes = hiddenCols.visibleToAbsoluteColumn(ranges.getStartRes());
    int endRes = hiddenCols.visibleToAbsoluteColumn(ranges.getEndRes());

    // work with absolute values of startSeq and endSeq
    int startSeq = hiddenSeqs.adjustForHiddenSeqs(ranges.getStartSeq());
    int endSeq = hiddenSeqs.adjustForHiddenSeqs(ranges.getEndSeq());

    setBoxPosition(startRes, startSeq, endRes - startRes + 1,
            endSeq - startSeq + 1);
  }

  @Override
  public AlignmentColsCollectionI getColumns(AlignmentI al)
  {
    return new AllColsCollection(0, ranges.getAbsoluteAlignmentWidth() - 1,
            al);
  }

  @Override
  public AlignmentRowsCollectionI getRows(AlignmentI al)
  {
    return new AllRowsCollection(0, ranges.getAbsoluteAlignmentHeight() - 1,
            al);
  }

  @Override
  protected void resetAlignmentDims()
  {
    alwidth = ranges.getAbsoluteAlignmentWidth();
    alheight = ranges.getAbsoluteAlignmentHeight();

    widthRatio = (float) alwidth / width;
    heightRatio = (float) alheight / sequencesHeight;
  }

  /**
   * {@inheritDoc} Callers should have already called resetAlignmentDims to
   * refresh widthRatio
   */
  @Override
  protected int getLeftXFromCentreX(int mousex, HiddenColumns hidden)
  {
    int vpx = Math.round((float) mousex * alwidth / width);
    return hidden.offsetByVisibleColumns(-ranges.getViewportWidth() / 2,
            vpx);
  }

  /**
   * {@inheritDoc} Callers should have already called resetAlignmentDims to
   * refresh heightRatio
   */
  @Override
  protected int getTopYFromCentreY(int mousey, HiddenSequences hidden)
  {
    int vpy = Math.round(mousey * heightRatio);
    return hidden.subtractVisibleRows(ranges.getViewportHeight() / 2, vpy);
  }

  @Override
  public void setDragPoint(int x, int y, HiddenSequences hiddenSeqs,
          HiddenColumns hiddenCols)
  {
    resetAlignmentDims();

    // get alignment position of x and box (can get directly from vpranges) and
    // calculate difference between the positions
    int vpx = Math.round(x * widthRatio);
    int vpy = Math.round(y * heightRatio);

    xdiff = ranges.getStartRes() - hiddenCols.absoluteToVisibleColumn(vpx);
    ydiff = ranges.getStartSeq()
            - hiddenSeqs.findIndexWithoutHiddenSeqs(vpy);
  }

}
