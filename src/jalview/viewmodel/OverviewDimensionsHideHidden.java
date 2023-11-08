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
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.HiddenSequences;
import jalview.datamodel.VisibleColsCollection;
import jalview.datamodel.VisibleRowsCollection;

public class OverviewDimensionsHideHidden extends OverviewDimensions
{
  private ViewportRanges ranges;

  private int xdiff; // when dragging, difference in alignment units between
                     // start residue and original mouse click position

  private int ydiff; // when dragging, difference in alignment units between
                     // start sequence and original mouse click position

  public OverviewDimensionsHideHidden(ViewportRanges vpranges,
          boolean showAnnotationPanel)
  {
    super(vpranges, showAnnotationPanel);
    ranges = vpranges;
    resetAlignmentDims();
  }

  @Override
  public void updateViewportFromMouse(int mousex, int mousey,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols)
  {
    resetAlignmentDims();

    int xAsRes = getLeftXFromCentreX(mousex, hiddenCols);
    int yAsSeq = getTopYFromCentreY(mousey, hiddenSeqs);

    updateViewportFromTopLeft(xAsRes, yAsSeq, hiddenSeqs, hiddenCols);

  }

  @Override
  public void adjustViewportFromMouse(int mousex, int mousey,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols)
  {
    resetAlignmentDims();

    // calculate translation in pixel terms:
    // get mouse location in viewport coords, add translation in viewport
    // coords, and update viewport as usual
    int vpx = Math.round(mousex * widthRatio);
    int vpy = Math.round(mousey * heightRatio);

    updateViewportFromTopLeft(vpx + xdiff, vpy + ydiff, hiddenSeqs,
            hiddenCols);

  }

  /**
   * {@inheritDoc} Callers should have already called resetAlignmentDims to
   * refresh alwidth, alheight and width/height ratios
   */
  @Override
  protected void updateViewportFromTopLeft(int leftx, int topy,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols)
  {
    int xAsRes = leftx;
    int yAsSeq = topy;

    if (xAsRes < 0)
    {
      xAsRes = 0;
    }

    if (yAsSeq < 0)
    {
      yAsSeq = 0;
    }

    if (ranges.isWrappedMode())
    {
      yAsSeq = 0; // sorry, no vertical scroll when wrapped
    }

    // get viewport width in residues
    int vpwidth = ranges.getViewportWidth();

    if (xAsRes + vpwidth > alwidth)
    {
      // went past the end of the alignment, adjust backwards

      // if last position was before the end of the alignment, need to update
      if (ranges.getStartRes() < alwidth)
      {
        xAsRes = alwidth - vpwidth;
      }
      else
      {
        xAsRes = ranges.getStartRes();
      }
    }

    // Determine where scrollRow should be, given visYAsSeq

    // get viewport height in sequences
    // add 1 because height includes both endSeq and startSeq
    int vpheight = ranges.getViewportHeight();

    if (yAsSeq + vpheight > alheight)
    {
      // went past the end of the alignment, adjust backwards
      if (ranges.getEndSeq() < alheight)
      {
        yAsSeq = alheight - vpheight;
      }
      else
      {
        yAsSeq = ranges.getStartSeq();
      }
    }

    ranges.setStartResAndSeq(xAsRes, yAsSeq);
  }

  @Override
  public void setBoxPosition(HiddenSequences hiddenSeqs,
          HiddenColumns hiddenCols)
  {
    setBoxPosition(ranges.getStartRes(), ranges.getStartSeq(),
            ranges.getViewportWidth(), ranges.getViewportHeight());
  }

  @Override
  public AlignmentColsCollectionI getColumns(AlignmentI al)
  {
    return new VisibleColsCollection(0,
            ranges.getAbsoluteAlignmentWidth() - 1, al.getHiddenColumns());
  }

  @Override
  public AlignmentRowsCollectionI getRows(AlignmentI al)
  {
    return new VisibleRowsCollection(0,
            ranges.getAbsoluteAlignmentHeight() - 1, al);
  }

  @Override
  protected void resetAlignmentDims()
  {
    alwidth = ranges.getVisibleAlignmentWidth();
    alheight = ranges.getVisibleAlignmentHeight();

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
    int vpx = Math.round(mousex * widthRatio);
    return vpx - ranges.getViewportWidth() / 2;
  }

  /**
   * {@inheritDoc} Callers should have already called resetAlignmentDims to
   * refresh heightRatio
   */
  @Override
  protected int getTopYFromCentreY(int mousey, HiddenSequences hidden)
  {
    int vpy = Math.round(mousey * heightRatio);
    return vpy - ranges.getViewportHeight() / 2;
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

    xdiff = ranges.getStartRes() - vpx;
    ydiff = ranges.getStartSeq() - vpy;
  }

}
