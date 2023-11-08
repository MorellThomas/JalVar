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
package jalview.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ShiftList Simple way of mapping a linear series to a new linear range with
 * new points introduced. Use at your own risk! Now growing to be used for
 * interval ranges (position, offset) storing deletions/insertions
 */
public class ShiftList
{
  private List<int[]> shifts;

  public ShiftList()
  {
    shifts = new ArrayList<int[]>();
  }

  /**
   * addShift
   * 
   * @param pos
   *          start position for shift (in original reference frame)
   * @param shift
   *          length of shift
   */
  public void addShift(int pos, int shift)
  {
    synchronized (shifts)
    {
      int sidx = 0;
      int[] rshift = null;
      while (sidx < shifts.size() && (rshift = shifts.get(sidx))[0] < pos)
      {
        sidx++;
      }
      if (sidx == shifts.size())
      {
        shifts.add(sidx, new int[] { pos, shift });
      }
      else
      {
        rshift[1] += shift;
      }
    }
  }

  /**
   * shift
   * 
   * @param pos
   *          int
   * @return int shifted position
   */
  public int shift(int pos)
  {
    if (shifts.size() == 0)
    {
      return pos;
    }
    int shifted = pos;
    int sidx = 0;
    int rshift[];
    while (sidx < shifts.size()
            && (rshift = (shifts.get(sidx++)))[0] <= pos)
    {
      shifted += rshift[1];
    }
    return shifted;
  }

  /**
   * clear all shifts
   */
  public synchronized void clear()
  {
    shifts.clear();
  }

  /**
   * invert the shifts
   * 
   * @return ShiftList with inverse shift operations
   */
  public ShiftList getInverse()
  {
    ShiftList inverse = new ShiftList();
    synchronized (shifts)
    {
      if (shifts != null)
      {
        for (int[] sh : shifts)
        {
          if (sh != null)
          {
            inverse.shifts.add(new int[] { sh[0], -sh[1] });
          }
        }
      }
    }
    return inverse;
  }

  /**
   * parse a 1d map of position 1&lt;i&lt;n to L&lt;pos[i]&lt;N such as that
   * returned from SequenceI.gapMap()
   * 
   * @param gapMap
   * @return shifts from map index to mapped position
   */
  public static ShiftList parseMap(int[] gapMap)
  {
    ShiftList shiftList = null;
    if (gapMap != null && gapMap.length > 0)
    {
      shiftList = new ShiftList();
      for (int i = 0, p = 0; i < gapMap.length; p++, i++)
      {
        if (p != gapMap[i])
        {
          shiftList.addShift(p, gapMap[i] - p);
          p = gapMap[i];
        }
      }
    }
    return shiftList;
  }

  public List<int[]> getShifts()
  {
    return shifts;
  }
}
