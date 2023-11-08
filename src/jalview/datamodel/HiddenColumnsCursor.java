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
package jalview.datamodel;

import java.util.ArrayList;
import java.util.List;

public class HiddenColumnsCursor
{
  // absolute position of first hidden column
  private int firstColumn;

  private List<int[]> hiddenColumns = new ArrayList<>();

  private HiddenCursorPosition cursorPos = new HiddenCursorPosition(0, 0);

  protected HiddenColumnsCursor()
  {

  }

  protected HiddenColumnsCursor(List<int[]> hiddenCols)
  {
    resetCursor(hiddenCols, 0, 0);
  }

  protected HiddenColumnsCursor(List<int[]> hiddenCols, int index,
          int hiddencount)
  {
    resetCursor(hiddenCols, index, hiddencount);
  }

  /**
   * Reset the cursor with a new hidden columns collection, where we know in
   * advance the index and hidden columns count of a particular location.
   * 
   * @param hiddenCols
   *          new hidden columns collection
   * @param index
   *          cursor index to reset to
   * @param hiddencount
   *          hidden columns count to reset to
   */
  private void resetCursor(List<int[]> hiddenCols, int index,
          int hiddencount)
  {
    hiddenColumns = hiddenCols;
    if (!hiddenCols.isEmpty())
    {
      firstColumn = hiddenColumns.get(0)[0];
      cursorPos = new HiddenCursorPosition(index, hiddencount);
    }
  }

  /**
   * Get the cursor pointing to the hidden region that column is within (if
   * column is hidden) or which is to the right of column (if column is
   * visible). If no hidden columns are to the right, returns a cursor pointing
   * to an imaginary hidden region beyond the end of the hidden columns
   * collection (this ensures the count of previous hidden columns is correct).
   * If hidden columns is empty returns null.
   * 
   * @param column
   *          index of column in visible or absolute coordinates
   * @param useVisible
   *          true if column is in visible coordinates, false if absolute
   * @return cursor pointing to hidden region containing the column (if hidden)
   *         or to the right of the column (if visible)
   */
  protected HiddenCursorPosition findRegionForColumn(int column,
          boolean useVisible)
  {
    if (hiddenColumns.isEmpty())
    {
      return null;
    }

    // used to add in hiddenColumns offset when working with visible columns
    int offset = (useVisible ? 1 : 0);

    HiddenCursorPosition pos = cursorPos;
    int index = pos.getRegionIndex();
    int hiddenCount = pos.getHiddenSoFar();

    if (column < firstColumn)
    {
      pos = new HiddenCursorPosition(0, 0);
    }

    // column is after current region
    else if ((index < hiddenColumns.size())
            && (hiddenColumns.get(index)[0] <= column
                    + offset * hiddenCount))
    {
      // iterate from where we are now, if we're lucky we'll be close by
      // (but still better than iterating from 0)
      // stop when we find the region *before* column
      // i.e. the next region starts after column or if not, ends after column
      pos = searchForward(pos, column, useVisible);
    }

    // column is before current region
    else
    {
      pos = searchBackward(pos, column, useVisible);
    }
    cursorPos = pos;
    return pos;
  }

  /**
   * Search forwards through the hidden columns collection to find the hidden
   * region immediately before a column
   * 
   * @param pos
   *          current position
   * @param column
   *          column to locate
   * @param useVisible
   *          whether using visible or absolute coordinates
   * @return position of region before column
   */
  private HiddenCursorPosition searchForward(HiddenCursorPosition pos,
          int column, boolean useVisible)
  {
    HiddenCursorPosition p = pos;
    if (useVisible)
    {
      while ((p.getRegionIndex() < hiddenColumns.size())
              && hiddenColumns.get(p.getRegionIndex())[0] <= column
                      + p.getHiddenSoFar())
      {
        p = stepForward(p);
      }
    }
    else
    {
      while ((p.getRegionIndex() < hiddenColumns.size())
              && hiddenColumns.get(p.getRegionIndex())[1] < column)
      {
        p = stepForward(p);
      }
    }
    return p;
  }

  /**
   * Move to the next (rightwards) hidden region after a given cursor position
   * 
   * @param p
   *          current position of cursor
   * @return new position of cursor at next region
   */
  private HiddenCursorPosition stepForward(HiddenCursorPosition p)
  {
    int[] region = hiddenColumns.get(p.getRegionIndex());

    // increment the index, and add this region's hidden columns to the hidden
    // column count
    return new HiddenCursorPosition(p.getRegionIndex() + 1,
            p.getHiddenSoFar() + region[1] - region[0] + 1);
  }

  /**
   * Search backwards through the hidden columns collection to find the hidden
   * region immediately before (left of) a given column
   * 
   * @param pos
   *          current position
   * @param column
   *          column to locate
   * @param useVisible
   *          whether using visible or absolute coordinates
   * @return position of region immediately to left of column
   */
  private HiddenCursorPosition searchBackward(HiddenCursorPosition p,
          int column, boolean useVisible)
  {
    int i = p.getRegionIndex();
    int h = p.getHiddenSoFar();

    // used to add in hiddenColumns offset when working with visible columns
    int offset = (useVisible ? 1 : 0);

    while ((i > 0) && (hiddenColumns.get(i - 1)[1] >= column + offset * h))
    {
      i--;
      int[] region = hiddenColumns.get(i);
      h -= region[1] - region[0] + 1;
    }
    return new HiddenCursorPosition(i, h);
  }

}
