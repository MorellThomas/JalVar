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

import java.util.Comparator;

/**
 * A comparator to order [from, to] ranges into ascending or descending order of
 * their start position
 */
public class IntRangeComparator implements Comparator<int[]>
{
  public static final Comparator<int[]> ASCENDING = new IntRangeComparator(
          true);

  public static final Comparator<int[]> DESCENDING = new IntRangeComparator(
          false);

  boolean forwards;

  IntRangeComparator(boolean forward)
  {
    forwards = forward;
  }

  @Override
  public int compare(int[] o1, int[] o2)
  {
    int compared = Integer.compare(o1[0], o2[0]);
    return forwards ? compared : -compared;
  }

}