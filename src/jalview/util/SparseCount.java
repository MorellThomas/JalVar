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

import jalview.ext.android.SparseIntArray;
import jalview.ext.android.SparseShortArray;

/**
 * A class to count occurrences of characters with minimal memory footprint.
 * Sparse arrays of short values are used to hold the counts, with automatic
 * promotion to arrays of int if any count exceeds the maximum value for a
 * short.
 * 
 * @author gmcarstairs
 *
 */
public class SparseCount
{
  private static final int DEFAULT_PROFILE_SIZE = 2;

  /*
   * array of keys (chars) and values (counts)
   * held either as shorts or (if shorts overflow) as ints 
   */
  private SparseShortArray shortProfile;

  private SparseIntArray intProfile;

  /*
   * flag is set true after short overflow occurs
   */
  private boolean useInts;

  /**
   * Constructor which initially creates a new sparse array of short values to
   * hold counts.
   * 
   * @param profileSize
   */
  public SparseCount(int profileSize)
  {
    this.shortProfile = new SparseShortArray(profileSize);
  }

  /**
   * Constructor which allocates an initial count array for only two distinct
   * values (the array will grow if needed)
   */
  public SparseCount()
  {
    this(DEFAULT_PROFILE_SIZE);
  }

  /**
   * Adds the given value for the given key (or sets the initial value), and
   * returns the new value
   * 
   * @param key
   * @param value
   */
  public int add(int key, int value)
  {
    int newValue = 0;
    if (useInts)
    {
      newValue = intProfile.add(key, value);
    }
    else
    {
      try
      {
        newValue = shortProfile.add(key, value);
      } catch (ArithmeticException e)
      {
        handleOverflow();
        newValue = intProfile.add(key, value);
      }
    }
    return newValue;
  }

  /**
   * Switch from counting shorts to counting ints
   */
  synchronized void handleOverflow()
  {
    int size = shortProfile.size();
    intProfile = new SparseIntArray(size);
    for (int i = 0; i < size; i++)
    {
      short key = shortProfile.keyAt(i);
      short value = shortProfile.valueAt(i);
      intProfile.put(key, value);
    }
    shortProfile = null;
    useInts = true;
  }

  /**
   * Returns the size of the profile (number of distinct items counted)
   * 
   * @return
   */
  public int size()
  {
    return useInts ? intProfile.size() : shortProfile.size();
  }

  /**
   * Returns the value for the key (zero if no such key)
   * 
   * @param key
   * @return
   */
  public int get(int key)
  {
    return useInts ? intProfile.get(key) : shortProfile.get(key);
  }

  /**
   * Sets the value for the given key
   * 
   * @param key
   * @param value
   */
  public void put(int key, int value)
  {
    if (useInts)
    {
      intProfile.put(key, value);
    }
    else
    {
      shortProfile.put(key, value);
    }
  }

  public int keyAt(int k)
  {
    return useInts ? intProfile.keyAt(k) : shortProfile.keyAt(k);
  }

  public int valueAt(int k)
  {
    return useInts ? intProfile.valueAt(k) : shortProfile.valueAt(k);
  }

  /**
   * Answers true if this object wraps arrays of int values, false if using
   * short values
   * 
   * @return
   */
  boolean isUsingInt()
  {
    return useInts;
  }
}
