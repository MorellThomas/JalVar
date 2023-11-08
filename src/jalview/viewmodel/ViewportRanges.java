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

import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;

/**
 * Supplies and updates viewport properties relating to position such as: start
 * and end residues and sequences; ideally will serve hidden columns/rows too.
 * Intention also to support calculations for positioning, scrolling etc. such
 * as finding the middle of the viewport, checking for scrolls off screen
 */
public class ViewportRanges extends ViewportProperties
{
  public static final String STARTRES = "startres";

  public static final String ENDRES = "endres";

  public static final String STARTSEQ = "startseq";

  public static final String ENDSEQ = "endseq";

  public static final String STARTRESANDSEQ = "startresandseq";

  public static final String MOVE_VIEWPORT = "move_viewport";

  private boolean wrappedMode = false;

  // start residue of viewport
  private int startRes;

  // end residue of viewport
  private int endRes;

  // start sequence of viewport
  private int startSeq;

  // end sequence of viewport
  private int endSeq;

  // alignment
  private AlignmentI al;

  /**
   * Constructor
   * 
   * @param alignment
   *          the viewport's alignment
   */
  public ViewportRanges(AlignmentI alignment)
  {
    // initial values of viewport settings
    this.startRes = 0;
    this.endRes = alignment.getWidth() - 1;
    this.startSeq = 0;
    this.endSeq = alignment.getHeight() - 1;
    this.al = alignment;
  }

  /**
   * Get alignment width in cols, including hidden cols
   */
  public int getAbsoluteAlignmentWidth()
  {
    return al.getWidth();
  }

  /**
   * Get alignment height in rows, including hidden rows
   */
  public int getAbsoluteAlignmentHeight()
  {
    return al.getHeight() + al.getHiddenSequences().getSize();
  }

  /**
   * Get alignment width in cols, excluding hidden cols
   */
  public int getVisibleAlignmentWidth()
  {
    return al.getVisibleWidth();
  }

  /**
   * Get alignment height in rows, excluding hidden rows
   */
  public int getVisibleAlignmentHeight()
  {
    return al.getHeight();
  }

  /**
   * Set first residue visible in the viewport, and retain the current width.
   * Fires a property change event.
   * 
   * @param res
   *          residue position
   */
  public void setStartRes(int res)
  {
    int width = getViewportWidth();
    setStartEndRes(res, res + width - 1);
  }

  /**
   * Set start and end residues at the same time. This method only fires one
   * event for the two changes, and should be used in preference to separate
   * calls to setStartRes and setEndRes.
   * 
   * @param start
   *          the start residue
   * @param end
   *          the end residue
   */
  public void setStartEndRes(int start, int end)
  {
    int[] oldvalues = updateStartEndRes(start, end);
    int oldstartres = oldvalues[0];
    int oldendres = oldvalues[1];

    changeSupport.firePropertyChange(STARTRES, oldstartres, startRes);
    if (oldstartres == startRes)
    {
      // event won't be fired if start positions are same
      // fire an event for the end positions in case they changed
      changeSupport.firePropertyChange(ENDRES, oldendres, endRes);
    }
  }

  /**
   * Update start and end residue values, adjusting for width constraints if
   * necessary
   * 
   * @param start
   *          start residue
   * @param end
   *          end residue
   * @return array containing old start and end residue values
   */
  private int[] updateStartEndRes(int start, int end)
  {
    int oldstartres = this.startRes;

    /*
     * if not wrapped, don't leave white space at the right margin
     */
    int lastColumn = getVisibleAlignmentWidth() - 1;
    if (!wrappedMode && (start > lastColumn))
    {
      startRes = Math.max(lastColumn, 0);
    }
    else if (start < 0)
    {
      startRes = 0;
    }
    else
    {
      startRes = start;
    }

    int oldendres = this.endRes;
    if (end < 0)
    {
      endRes = 0;
    }
    else if (!wrappedMode && (end > lastColumn))
    {
      endRes = Math.max(lastColumn, 0);
    }
    else
    {
      endRes = end;
    }
    return new int[] { oldstartres, oldendres };
  }

  /**
   * Set the first sequence visible in the viewport, maintaining the height. If
   * the viewport would extend past the last sequence, sets the viewport so it
   * sits at the bottom of the alignment. Fires a property change event.
   * 
   * @param seq
   *          sequence position
   */
  public void setStartSeq(int seq)
  {
    int startseq = seq;
    int height = getViewportHeight();
    if (startseq + height - 1 > getVisibleAlignmentHeight() - 1)
    {
      startseq = getVisibleAlignmentHeight() - height;
    }
    setStartEndSeq(startseq, startseq + height - 1);
  }

  /**
   * Set start and end sequences at the same time. The viewport height may
   * change. This method only fires one event for the two changes, and should be
   * used in preference to separate calls to setStartSeq and setEndSeq.
   * 
   * @param start
   *          the start sequence
   * @param end
   *          the end sequence
   */
  public void setStartEndSeq(int start, int end)
  {
    // System.out.println("ViewportRange setStartEndSeq " + start + " " + end);
    int[] oldvalues = updateStartEndSeq(start, end);
    int oldstartseq = oldvalues[0];
    int oldendseq = oldvalues[1];

    changeSupport.firePropertyChange(STARTSEQ, oldstartseq, startSeq);
    if (oldstartseq == startSeq)
    {
      // event won't be fired if start positions are the same
      // fire in case the end positions changed
      changeSupport.firePropertyChange(ENDSEQ, oldendseq, endSeq);
    }
  }

  /**
   * Update start and end sequence values, adjusting for height constraints if
   * necessary
   * 
   * @param start
   *          start sequence
   * @param end
   *          end sequence
   * @return array containing old start and end sequence values
   */
  private int[] updateStartEndSeq(int start, int end)
  {
    int oldstartseq = this.startSeq;
    int visibleHeight = getVisibleAlignmentHeight();
    if (start > visibleHeight - 1)
    {
      startSeq = Math.max(visibleHeight - 1, 0);
    }
    else if (start < 0)
    {
      startSeq = 0;
    }
    else
    {
      startSeq = start;
    }

    int oldendseq = this.endSeq;
    if (end >= visibleHeight)
    {
      endSeq = Math.max(visibleHeight - 1, 0);
    }
    else if (end < 0)
    {
      endSeq = 0;
    }
    else
    {
      endSeq = end;
    }
    return new int[] { oldstartseq, oldendseq };
  }

  /**
   * Set the last sequence visible in the viewport. Fires a property change
   * event.
   * 
   * @param seq
   *          sequence position in the range [0, height)
   */
  public void setEndSeq(int seq)
  {
    // BH 2018.04.18 added safety for seq < 0; comment about not being >= height
    setStartEndSeq(Math.max(0, seq + 1 - getViewportHeight()), seq);
  }

  /**
   * Set start residue and start sequence together (fires single event). The
   * event supplies a pair of old values and a pair of new values: [old start
   * residue, old start sequence] and [new start residue, new start sequence]
   * 
   * @param res
   *          the start residue
   * @param seq
   *          the start sequence
   */
  public void setStartResAndSeq(int res, int seq)
  {
    int width = getViewportWidth();
    int[] oldresvalues = updateStartEndRes(res, res + width - 1);

    int startseq = seq;
    int height = getViewportHeight();
    if (startseq + height - 1 > getVisibleAlignmentHeight() - 1)
    {
      startseq = getVisibleAlignmentHeight() - height;
    }
    int[] oldseqvalues = updateStartEndSeq(startseq, startseq + height - 1);

    int[] old = new int[] { oldresvalues[0], oldseqvalues[0] };
    int[] newresseq = new int[] { startRes, startSeq };
    changeSupport.firePropertyChange(STARTRESANDSEQ, old, newresseq);
  }

  /**
   * Get start residue of viewport
   */
  public int getStartRes()
  {
    return startRes;
  }

  /**
   * Get end residue of viewport
   */
  public int getEndRes()
  {
    return endRes;
  }

  /**
   * Get start sequence of viewport
   */
  public int getStartSeq()
  {
    return startSeq;
  }

  /**
   * Get end sequence of viewport
   */
  public int getEndSeq()
  {
    return endSeq;
  }

  /**
   * Set viewport width in residues, without changing startRes. Use in
   * preference to calculating endRes from the width, to avoid out by one
   * errors! Fires a property change event.
   * 
   * @param w
   *          width in residues
   */
  public void setViewportWidth(int w)
  {
    setStartEndRes(startRes, startRes + w - 1);
  }

  /**
   * Set viewport height in residues, without changing startSeq. Use in
   * preference to calculating endSeq from the height, to avoid out by one
   * errors! Fires a property change event.
   * 
   * @param h
   *          height in sequences
   */
  public void setViewportHeight(int h)
  {
    setStartEndSeq(startSeq, startSeq + h - 1);
  }

  /**
   * Set viewport horizontal start position and width. Use in preference to
   * calculating endRes from the width, to avoid out by one errors! Fires a
   * property change event.
   * 
   * @param start
   *          start residue
   * @param w
   *          width in residues
   */
  public void setViewportStartAndWidth(int start, int w)
  {
    int vpstart = start;
    if (vpstart < 0)
    {
      vpstart = 0;
    }

    /*
     * if not wrapped, don't leave white space at the right margin
     */
    if (!wrappedMode)
    {
      if ((w <= getVisibleAlignmentWidth())
              && (vpstart + w - 1 > getVisibleAlignmentWidth() - 1))
      {
        vpstart = getVisibleAlignmentWidth() - w;
      }

    }
    setStartEndRes(vpstart, vpstart + w - 1);
  }

  /**
   * Set viewport vertical start position and height. Use in preference to
   * calculating endSeq from the height, to avoid out by one errors! Fires a
   * property change event.
   * 
   * @param start
   *          start sequence
   * @param h
   *          height in sequences
   */
  public void setViewportStartAndHeight(int start, int h)
  {
    int vpstart = start;

    int visHeight = getVisibleAlignmentHeight();
    if (vpstart < 0)
    {
      vpstart = 0;
    }
    else if (h <= visHeight && vpstart + h > visHeight)
    // viewport height is less than the full alignment and we are running off
    // the bottom
    {
      vpstart = visHeight - h;
    }
    // System.out.println("ViewportRanges setviewportStartAndHeight " + vpstart
    // + " " + start + " " + h + " " + getVisibleAlignmentHeight());

    setStartEndSeq(vpstart, vpstart + h - 1);
  }

  /**
   * Get width of viewport in residues
   * 
   * @return width of viewport
   */
  public int getViewportWidth()
  {
    return (endRes - startRes + 1);
  }

  /**
   * Get height of viewport in residues
   * 
   * @return height of viewport
   */
  public int getViewportHeight()
  {
    return (endSeq - startSeq + 1);
  }

  /**
   * Scroll the viewport range vertically. Fires a property change event.
   * 
   * @param up
   *          true if scrolling up, false if down
   * 
   * @return true if the scroll is valid
   */
  public boolean scrollUp(boolean up)
  {
    /*
     * if in unwrapped mode, scroll up or down one sequence row;
     * if in wrapped mode, scroll by one visible width of columns
     */
    if (up)
    {
      if (wrappedMode)
      {
        pageUp();
      }
      else
      {
        if (startSeq < 1)
        {
          return false;
        }
        setStartSeq(startSeq - 1);
      }
    }
    else
    {
      if (wrappedMode)
      {
        pageDown();
      }
      else
      {
        if (endSeq >= getVisibleAlignmentHeight() - 1)
        {
          return false;
        }
        setStartSeq(startSeq + 1);
      }
    }
    return true;
  }

  /**
   * Scroll the viewport range horizontally. Fires a property change event.
   * 
   * @param right
   *          true if scrolling right, false if left
   * 
   * @return true if the scroll is valid
   */
  public boolean scrollRight(boolean right)
  {
    if (!right)
    {
      if (startRes < 1)
      {
        return false;
      }

      setStartRes(startRes - 1);
    }
    else
    {
      if (endRes >= getVisibleAlignmentWidth() - 1)
      {
        return false;
      }

      setStartRes(startRes + 1);
    }

    return true;
  }

  /**
   * Scroll a wrapped alignment so that the specified residue is in the first
   * repeat of the wrapped view. Fires a property change event. Answers true if
   * the startRes changed, else false.
   * 
   * @param res
   *          residue position to scroll to NB visible position not absolute
   *          alignment position
   * @return
   */
  public boolean scrollToWrappedVisible(int res)
  {
    int newStartRes = calcWrappedStartResidue(res);
    if (newStartRes == startRes)
    {
      return false;
    }
    setStartRes(newStartRes);

    return true;
  }

  /**
   * Calculate wrapped start residue from visible start residue
   * 
   * @param res
   *          visible start residue
   * @return left column of panel res will be located in
   */
  private int calcWrappedStartResidue(int res)
  {
    int oldStartRes = startRes;
    int width = getViewportWidth();

    boolean up = res < oldStartRes;
    int widthsToScroll = Math.abs((res - oldStartRes) / width);
    if (up)
    {
      widthsToScroll++;
    }

    int residuesToScroll = width * widthsToScroll;
    int newStartRes = up ? oldStartRes - residuesToScroll
            : oldStartRes + residuesToScroll;
    if (newStartRes < 0)
    {
      newStartRes = 0;
    }
    return newStartRes;
  }

  /**
   * Scroll so that (x,y) is visible. Fires a property change event.
   * 
   * @param x
   *          x position in alignment (absolute position)
   * @param y
   *          y position in alignment (absolute position)
   */
  public void scrollToVisible(int x, int y)
  {
    while (y < startSeq)
    {
      scrollUp(true);
    }
    while (y > endSeq)
    {
      scrollUp(false);
    }

    HiddenColumns hidden = al.getHiddenColumns();
    while (x < hidden.visibleToAbsoluteColumn(startRes))
    {
      if (!scrollRight(false))
      {
        break;
      }
    }
    while (x > hidden.visibleToAbsoluteColumn(endRes))
    {
      if (!scrollRight(true))
      {
        break;
      }
    }
  }

  /**
   * Set the viewport location so that a position is visible
   * 
   * @param x
   *          column to be visible: absolute position in alignment
   * @param y
   *          row to be visible: absolute position in alignment
   */
  public boolean setViewportLocation(int x, int y)
  {
    boolean changedLocation = false;

    // convert the x,y location to visible coordinates
    int visX = al.getHiddenColumns().absoluteToVisibleColumn(x);
    int visY = al.getHiddenSequences().findIndexWithoutHiddenSeqs(y);

    // if (vis_x,vis_y) is already visible don't do anything
    if (startRes > visX || visX > endRes
            || startSeq > visY && visY > endSeq)
    {
      int[] old = new int[] { startRes, startSeq };
      int[] newresseq;
      if (wrappedMode)
      {
        int newstartres = calcWrappedStartResidue(visX);
        setStartRes(newstartres);
        newresseq = new int[] { startRes, startSeq };
      }
      else
      {
        // set the viewport x location to contain vis_x
        int newstartres = visX;
        int width = getViewportWidth();
        if (newstartres + width - 1 > getVisibleAlignmentWidth() - 1)
        {
          newstartres = getVisibleAlignmentWidth() - width;
        }
        updateStartEndRes(newstartres, newstartres + width - 1);

        // set the viewport y location to contain vis_y
        int newstartseq = visY;
        int height = getViewportHeight();
        if (newstartseq + height - 1 > getVisibleAlignmentHeight() - 1)
        {
          newstartseq = getVisibleAlignmentHeight() - height;
        }
        updateStartEndSeq(newstartseq, newstartseq + height - 1);

        newresseq = new int[] { startRes, startSeq };
      }
      changedLocation = true;
      changeSupport.firePropertyChange(MOVE_VIEWPORT, old, newresseq);
    }
    return changedLocation;
  }

  /**
   * Adjust sequence position for page up. Fires a property change event.
   */
  public void pageUp()
  {
    if (wrappedMode)
    {
      setStartRes(Math.max(0, getStartRes() - getViewportWidth()));
    }
    else
    {
      setViewportStartAndHeight(startSeq - (endSeq - startSeq),
              getViewportHeight());
    }
  }

  /**
   * Adjust sequence position for page down. Fires a property change event.
   */
  public void pageDown()
  {
    if (wrappedMode)
    {
      /*
       * if height is more than width (i.e. not all sequences fit on screen),
       * increase page down to height
       */
      int newStart = getStartRes()
              + Math.max(getViewportHeight(), getViewportWidth());

      /*
       * don't page down beyond end of alignment, or if not all
       * sequences fit in the visible height
       */
      if (newStart < getVisibleAlignmentWidth())
      {
        setStartRes(newStart);
      }
    }
    else
    {
      setViewportStartAndHeight(endSeq, getViewportHeight());
    }
  }

  public void setWrappedMode(boolean wrapped)
  {
    wrappedMode = wrapped;
  }

  public boolean isWrappedMode()
  {
    return wrappedMode;
  }

  /**
   * Answers the vertical scroll position (0..) to set, given the visible column
   * that is at top left.
   * 
   * <pre>
   * Example:
   *    viewport width 40 columns (0-39, 40-79, 80-119...)
   *    column 0 returns scroll position 0
   *    columns 1-40 return scroll position 1
   *    columns 41-80 return scroll position 2
   *    etc
   * </pre>
   * 
   * @param topLeftColumn
   *          (0..)
   * @return
   */
  public int getWrappedScrollPosition(final int topLeftColumn)
  {
    int w = getViewportWidth();

    /*
     * visible whole widths
     */
    int scroll = topLeftColumn / w;

    /*
     * add 1 for a part width if there is one
     */
    scroll += topLeftColumn % w > 0 ? 1 : 0;

    return scroll;
  }

  /**
   * Answers the maximum wrapped vertical scroll value, given the column
   * position (0..) to show at top left of the visible region.
   * 
   * @param topLeftColumn
   * @return
   */
  public int getWrappedMaxScroll(int topLeftColumn)
  {
    int scrollPosition = getWrappedScrollPosition(topLeftColumn);

    /*
     * how many more widths could be drawn after this one?
     */
    int columnsRemaining = getVisibleAlignmentWidth() - topLeftColumn;
    int width = getViewportWidth();
    int widthsRemaining = columnsRemaining / width
            + (columnsRemaining % width > 0 ? 1 : 0) - 1;
    int maxScroll = scrollPosition + widthsRemaining;

    return maxScroll;
  }
}
