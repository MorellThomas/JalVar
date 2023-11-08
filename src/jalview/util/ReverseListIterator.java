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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An iterator that traverses a list backwards.
 * 
 * @author gmcarstairs (and checked against
 *         org.codehaus.groovey.runtime.ReverseListIterator)
 *
 * @param <E>
 */
public class ReverseListIterator<E> implements Iterator<E>
{

  private ListIterator<E> iterator;

  public ReverseListIterator(List<E> stuff)
  {
    this.iterator = stuff.listIterator(stuff.size());
  }

  @Override
  public boolean hasNext()
  {
    return iterator.hasPrevious();
  }

  @Override
  public E next()
  {
    return iterator.previous();
  }

  @Override
  public void remove()
  {
    iterator.remove();
  }

}
