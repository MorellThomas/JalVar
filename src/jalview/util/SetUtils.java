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

import java.util.Set;

public class SetUtils
{
  /**
   * Returns the count of things that are in one or other of two sets but not in
   * both. The sets are not modified.
   * 
   * @param set1
   * @param set2
   * @return
   */
  public static int countDisjunction(Set<? extends Object> set1,
          Set<? extends Object> set2)
  {
    if (set1 == null)
    {
      return set2 == null ? 0 : set2.size();
    }
    if (set2 == null)
    {
      return set1.size();
    }

    int size1 = set1.size();
    int size2 = set2.size();
    Set<? extends Object> smallerSet = size1 < size2 ? set1 : set2;
    Set<? extends Object> largerSet = (smallerSet == set1 ? set2 : set1);
    int inCommon = 0;
    for (Object k : smallerSet)
    {
      if (largerSet.contains(k))
      {
        inCommon++;
      }
    }

    int notInCommon = (size1 - inCommon) + (size2 - inCommon);
    return notInCommon;
  }
}
