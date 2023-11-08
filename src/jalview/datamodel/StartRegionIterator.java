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
import java.util.Iterator;
import java.util.List;

/**
 * An iterator which iterates over visible start positions of hidden column
 * regions in a range.
 */
public class StartRegionIterator implements Iterator<Integer>
{
  // start position to iterate from
  private int start;

  // end position to iterate to
  private int end;

  // current index in hiddenColumns
  private int currentPosition = 0;

  // local copy or reference to hiddenColumns
  private List<Integer> positions = null;

  /**
   * Construct an iterator over hiddenColums bounded at [lowerBound,upperBound]
   * 
   * @param lowerBound
   *          lower bound to iterate from
   * @param upperBound
   *          upper bound to iterate to
   * @param useCopyCols
   *          whether to make a local copy of hiddenColumns for iteration (set
   *          to true if calling from outwith the HiddenColumns class)
   */
  StartRegionIterator(int lowerBound, int upperBound,
          List<int[]> hiddenColumns)
  {
    this(null, lowerBound, upperBound, hiddenColumns);
  }

  /**
   * Construct an iterator over hiddenColums bounded at [lowerBound,upperBound]
   * 
   * @param pos
   *          a hidden cursor position to start from - may be null
   * @param lowerBound
   *          lower bound to iterate from - will be ignored if pos != null
   * @param upperBound
   *          upper bound to iterate to
   * @param hiddenColumns
   *          the hidden columns collection to use
   */
  StartRegionIterator(HiddenCursorPosition pos, int lowerBound,
          int upperBound, List<int[]> hiddenColumns)
  {
    start = lowerBound;
    end = upperBound;

    if (hiddenColumns != null)
    {
      positions = new ArrayList<>(hiddenColumns.size());

      // navigate to start, keeping count of hidden columns
      int i = 0;
      int hiddenSoFar = 0;

      if (pos != null)
      {
        // use the cursor position provided
        i = pos.getRegionIndex();
        hiddenSoFar = pos.getHiddenSoFar();
      }
      else
      {
        // navigate to start
        while ((i < hiddenColumns.size())
                && (hiddenColumns.get(i)[0] < start + hiddenSoFar))
        {
          int[] region = hiddenColumns.get(i);
          hiddenSoFar += region[1] - region[0] + 1;
          i++;
        }
      }

      // iterate from start to end, adding start positions of each
      // hidden region. Positions are visible columns count, not absolute
      while (i < hiddenColumns.size()
              && (hiddenColumns.get(i)[0] <= end + hiddenSoFar))
      {
        int[] region = hiddenColumns.get(i);
        positions.add(region[0] - hiddenSoFar);
        hiddenSoFar += region[1] - region[0] + 1;
        i++;
      }
    }
    else
    {
      positions = new ArrayList<>();
    }

  }

  @Override
  public boolean hasNext()
  {
    return (currentPosition < positions.size());
  }

  /**
   * Get next hidden region start position
   * 
   * @return the start position in *visible* coordinates
   */
  @Override
  public Integer next()
  {
    int result = positions.get(currentPosition);
    currentPosition++;
    return result;
  }
}
