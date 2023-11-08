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

import jalview.api.AlignmentColsCollectionI;

import java.util.Iterator;

public class VisibleColsCollection implements AlignmentColsCollectionI
{
  int start;

  int end;

  HiddenColumns hidden;

  public VisibleColsCollection(int s, int e, HiddenColumns h)
  {
    start = s;
    end = e;
    hidden = h;
  }

  @Override
  public Iterator<Integer> iterator()
  {
    return hidden.getVisibleColsIterator(start, end);
  }

  @Override
  public boolean isHidden(int c)
  {
    return false;
  }

  @Override
  public boolean hasHidden()
  {
    return false;
  }

}
