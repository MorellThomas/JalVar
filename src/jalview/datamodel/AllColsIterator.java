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
 * An iterator which iterates over all columns or rows in an alignment, whether
 * hidden or visible.
 * 
 * @author kmourao
 *
 */
public class AllColsIterator implements Iterator<Integer>
{
  private int last;

  private int next;

  private int current;

  public AllColsIterator(int firstcol, int lastcol,
          HiddenColumns hiddenCols)
  {
    last = lastcol;
    next = firstcol;
    current = firstcol;
  }

  @Override
  public boolean hasNext()
  {
    return next <= last;
  }

  @Override
  public Integer next()
  {
    if (next > last)
    {
      throw new NoSuchElementException();
    }
    current = next;
    next++;

    return current;
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}
