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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Order preserving Set based on System.identityHashCode() for an object, which
 * also supports Object->index lookup.
 * 
 * @author Jim Procter (2016) based on Evgeniy Dorofeev's response: via
 *         https://stackoverflow.com/questions/17276658/linkedidentityhashset
 * 
 */
public class LinkedIdentityHashSet<E> extends AbstractSet<E>
{
  LinkedHashMap<IdentityWrapper, IdentityWrapper> set = new LinkedHashMap<>();

  static class IdentityWrapper
  {
    Object obj;

    public int p;

    IdentityWrapper(Object obj, int p)
    {
      this.obj = obj;
      this.p = p;
    }

    @Override
    public boolean equals(Object obj)
    {
      return this.obj == ((IdentityWrapper) obj).obj;
    }

    @Override
    public int hashCode()
    {
      return System.identityHashCode(obj);
    }
  }

  @Override
  public boolean add(E e)
  {
    IdentityWrapper el = (new IdentityWrapper(e, set.size()));
    // Map.putIfAbsent() from Java 8
    // return set.putIfAbsent(el, el) == null;
    return putIfAbsent(el, el) == null;
  }

  /**
   * If the specified key is not already associated with a value (or is mapped
   * to null) associates it with the given value and returns null, else returns
   * the current value.
   * 
   * Method added for Java 7 (can remove for Java 8)
   * 
   * @param key
   * @param value
   * @return
   * @see https
   *      ://docs.oracle.com/javase/8/docs/api/java/util/Map.html#putIfAbsent
   *      -K-V-
   */
  private IdentityWrapper putIfAbsent(IdentityWrapper key,
          IdentityWrapper value)
  {
    IdentityWrapper v = set.get(key);
    if (v == null)
    {
      v = set.put(key, value);
    }
    return v;
  }

  @Override
  public Iterator<E> iterator()
  {
    return new Iterator<E>()
    {
      final Iterator<IdentityWrapper> se = set.keySet().iterator();

      @Override
      public boolean hasNext()
      {
        return se.hasNext();
      }

      @SuppressWarnings("unchecked")
      @Override
      public E next()
      {
        return (E) se.next().obj;
      }

      @Override
      public void remove()
      {
        // Java 8 default behaviour
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public int size()
  {
    return set.size();
  }

  /**
   * Lookup the index for e in the set
   * 
   * @param e
   * @return position of e in the set when it was added.
   */
  public int indexOf(E e)
  {
    return set.get(e).p;
  }
}
