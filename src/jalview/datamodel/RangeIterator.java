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
 * An iterator which iterates over a list of ranges. Works with a copy of the
 * collection of ranges.
 */
public class RangeIterator implements Iterator<int[]>
{
  // current index in rangeList
  private int currentPosition = 0;

  // current range in rangeList
  private int[] currentRange;

  // local copy or reference to rangeList
  private List<int[]> localRanges;

  /**
   * Unbounded constructor
   * 
   * @param rangeList
   *          list of ranges to iterate over
   */
  RangeIterator(List<int[]> rangeList)
  {
    if (!rangeList.isEmpty())
    {
      int last = rangeList.get(rangeList.size() - 1)[1];
      init(0, last, rangeList);
    }
    else
    {
      init(0, 0, rangeList);
    }
  }

  /**
   * Construct an iterator over rangeList bounded at [lowerBound,upperBound]
   * 
   * @param lowerBound
   *          lower bound to iterate from
   * @param upperBound
   *          upper bound to iterate to
   * @param rangeList
   *          list of ranges to iterate over
   */
  RangeIterator(int lowerBound, int upperBound, List<int[]> rangeList)
  {
    init(lowerBound, upperBound, rangeList);
  }

  /**
   * Construct an iterator over rangeList bounded at [lowerBound,upperBound]
   * 
   * @param lowerBound
   *          lower bound to iterate from
   * @param upperBound
   *          upper bound to iterate to
   */
  private void init(int lowerBound, int upperBound, List<int[]> rangeList)
  {
    int start = lowerBound;
    int end = upperBound;

    if (rangeList != null)
    {
      localRanges = new ArrayList<>();

      // iterate until a range overlaps with [start,end]
      int i = 0;
      while ((i < rangeList.size()) && (rangeList.get(i)[1] < start))
      {
        i++;
      }

      // iterate from start to end, adding each range. Positions are
      // absolute, and all ranges which *overlap* [start,end] are added.
      while (i < rangeList.size() && (rangeList.get(i)[0] <= end))
      {
        int[] rh = rangeList.get(i);
        int[] cp = new int[2];
        System.arraycopy(rh, 0, cp, 0, rh.length);
        localRanges.add(cp);
        i++;
      }
    }
  }

  @Override
  public boolean hasNext()
  {
    return (localRanges != null) && (currentPosition < localRanges.size());
  }

  @Override
  public int[] next()
  {
    currentRange = localRanges.get(currentPosition);
    currentPosition++;
    return currentRange;
  }

  @Override
  public void remove()
  {
    localRanges.remove(--currentPosition);
  }
}
