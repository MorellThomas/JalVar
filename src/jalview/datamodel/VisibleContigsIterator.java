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
 * An iterator which iterates over visible regions in a range. Provides a
 * special "endsAtHidden" indicator to allow callers to determine if the final
 * visible column is adjacent to a hidden region.
 */
public class VisibleContigsIterator implements Iterator<int[]>
{
  private List<int[]> vcontigs = new ArrayList<>();

  private int currentPosition = 0;

  private boolean endsAtHidden = false;

  VisibleContigsIterator(int start, int end, List<int[]> hiddenColumns)
  {
    if (hiddenColumns != null && hiddenColumns.size() > 0)
    {
      int vstart = start;
      int hideStart;
      int hideEnd;

      for (int[] region : hiddenColumns)
      {
        endsAtHidden = false;
        hideStart = region[0];
        hideEnd = region[1];

        // navigate to start
        if (hideEnd < vstart)
        {
          continue;
        }
        if (hideStart > vstart)
        {
          if (end - 1 > hideStart - 1)
          {
            int[] contig = new int[] { vstart, hideStart - 1 };
            vcontigs.add(contig);
            endsAtHidden = true;
          }
          else
          {
            int[] contig = new int[] { vstart, end - 1 };
            vcontigs.add(contig);
          }
        }
        vstart = hideEnd + 1;

        // exit if we're past the end
        if (vstart >= end)
        {
          break;
        }
      }

      if (vstart < end)
      {
        int[] contig = new int[] { vstart, end - 1 };
        vcontigs.add(contig);
        endsAtHidden = false;
      }
    }
    else
    {
      int[] contig = new int[] { start, end - 1 };
      vcontigs.add(contig);
    }
  }

  @Override
  public boolean hasNext()
  {
    return (currentPosition < vcontigs.size());
  }

  @Override
  public int[] next()
  {
    int[] result = vcontigs.get(currentPosition);
    currentPosition++;
    return result;
  }

  public boolean endsAtHidden()
  {
    return endsAtHidden;
  }
}
