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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator over each element in a set of ranges i.e. if ranges is {[3,6],
 * [12,15]} it will iterate over {3,4,5,6,12,13,14,15}. Uses a local copy of the
 * set of ranges.
 * 
 * @author kmourao
 *
 */
public class RangeElementsIterator implements Iterator<Integer>
{
  private int last;

  private int current;

  private int next;

  private Iterator<int[]> rangeIterator;

  private int[] nextRange = null;

  RangeElementsIterator(Iterator<int[]> it)
  {
    rangeIterator = it;
    if (rangeIterator.hasNext())
    {
      nextRange = rangeIterator.next();
      next = nextRange[0];
      last = nextRange[1];
    }
  }

  @Override
  public boolean hasNext()
  {
    return rangeIterator.hasNext() || next <= last;
  }

  @Override
  public Integer next()
  {
    if (!hasNext())
    {
      throw new NoSuchElementException();
    }

    current = next;

    // recalculate next
    next++;

    // if there are more ranges need to check if next is in a range
    checkNextRange();
    return current;
  }

  /**
   * Check how next position relates to next range, and update next position if
   * necessary
   */
  private void checkNextRange()
  {
    if (nextRange != null && next > nextRange[1])
    {
      if (rangeIterator.hasNext())
      {
        nextRange = rangeIterator.next();
        next = nextRange[0];
        last = nextRange[1];
      }
      else
      {
        nextRange = null;
      }

    }
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}
