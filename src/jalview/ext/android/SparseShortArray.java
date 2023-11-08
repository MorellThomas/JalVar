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
package jalview.ext.android;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * SparseShortArrays map shorts to shorts. Unlike a normal array of shorts,
 * there can be gaps in the indices. It is intended to be more memory efficient
 * than using a HashMap to map Shorts to Shorts, both because it avoids
 * auto-boxing keys and values and its data structure doesn't rely on an extra
 * entry object for each mapping.
 *
 * <p>
 * Note that this container keeps its mappings in an array data structure, using
 * a binary search to find keys. The implementation is not intended to be
 * appropriate for data structures that may contain large numbers of items. It
 * is generally slower than a traditional HashMap, since lookups require a
 * binary search and adds and removes require inserting and deleting entries in
 * the array. For containers holding up to hundreds of items, the performance
 * difference is not significant, less than 50%.
 * </p>
 *
 * <p>
 * It is possible to iterate over the items in this container using
 * {@link #keyAt(int)} and {@link #valueAt(int)}. Iterating over the keys using
 * <code>keyAt(int)</code> with ascending values of the index will return the
 * keys in ascending order, or the values corresponding to the keys in ascending
 * order in the case of <code>valueAt(int)<code>.
 * </p>
 */

/*
 * Added to Jalview September 2016. A copy of SparseIntArray designed to store
 * short values (to minimise space usage).
 * <p>
 * Note that operations append, put, add throw ArithmeticException if either the
 * key or the resulting value overflows the range of a short. Calling code
 * should trap and handle this, for example by switching to using a
 * SparseIntArray instead.
 */
public class SparseShortArray implements Cloneable
{
  private short[] mKeys;

  private short[] mValues;

  private int mSize;

  /**
   * Creates a new SparseShortArray containing no mappings.
   */
  public SparseShortArray()
  {
    this(10);
  }

  /**
   * Creates a new SparseShortArray containing no mappings that will not require
   * any additional memory allocation to store the specified number of mappings.
   * If you supply an initial capacity of 0, the sparse array will be
   * initialized with a light-weight representation not requiring any additional
   * array allocations.
   */
  public SparseShortArray(int initialCapacity)
  {
    if (initialCapacity == 0)
    {
      mKeys = new short[0];
      mValues = new short[0];
    }
    else
    {
      initialCapacity = idealShortArraySize(initialCapacity);
      mKeys = new short[initialCapacity];
      mValues = new short[initialCapacity];
    }
    mSize = 0;
  }

  @Override
  public SparseShortArray clone()
  {
    SparseShortArray clone = null;
    try
    {
      clone = (SparseShortArray) super.clone();
      clone.mKeys = mKeys.clone();
      clone.mValues = mValues.clone();
    } catch (CloneNotSupportedException cnse)
    {
      /* ignore */
    }
    return clone;
  }

  /**
   * Gets the int mapped from the specified key, or <code>0</code> if no such
   * mapping has been made.
   */
  public int get(int key)
  {
    return get(key, 0);
  }

  /**
   * Gets the int mapped from the specified key, or the specified value if no
   * such mapping has been made.
   * 
   * @throws ArithmeticException
   *           if key is outside the range of a short value
   */
  public int get(int key, int valueIfKeyNotFound)
  {
    checkOverflow(key);
    int i = ContainerHelpers.binarySearch(mKeys, mSize, (short) key);
    if (i < 0)
    {
      return valueIfKeyNotFound;
    }
    else
    {
      return mValues[i];
    }
  }

  /**
   * Removes the mapping from the specified key, if there was any.
   * 
   * @throws ArithmeticException
   *           if key is outside the range of a short value
   */
  public void delete(int key)
  {
    checkOverflow(key);
    int i = ContainerHelpers.binarySearch(mKeys, mSize, (short) key);
    if (i >= 0)
    {
      removeAt(i);
    }
  }

  /**
   * Removes the mapping at the given index.
   */
  public void removeAt(int index)
  {
    System.arraycopy(mKeys, index + 1, mKeys, index, mSize - (index + 1));
    System.arraycopy(mValues, index + 1, mValues, index,
            mSize - (index + 1));
    mSize--;
  }

  /**
   * Adds a mapping from the specified key to the specified value, replacing the
   * previous mapping from the specified key if there was one.
   * 
   * @throws ArithmeticException
   *           if either argument is outside the range of a short value
   */
  public void put(int key, int value)
  {
    checkOverflow(key);
    checkOverflow(value);
    int i = ContainerHelpers.binarySearch(mKeys, mSize, (short) key);
    if (i >= 0)
    {
      mValues[i] = (short) value;
    }
    else
    {
      i = ~i;
      if (mSize >= mKeys.length)
      {
        int n = idealShortArraySize(mSize + 1);
        short[] nkeys = new short[n];
        short[] nvalues = new short[n];
        // Log.e("SparseShortArray", "grow " + mKeys.length + " to " + n);
        System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
        System.arraycopy(mValues, 0, nvalues, 0, mValues.length);
        mKeys = nkeys;
        mValues = nvalues;
      }
      if (mSize - i != 0)
      {
        // Log.e("SparseShortArray", "move " + (mSize - i));
        System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
        System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
      }
      mKeys[i] = (short) key;
      mValues[i] = (short) value;
      mSize++;
    }
  }

  /**
   * Returns the number of key-value mappings that this SparseShortArray
   * currently stores.
   */
  public int size()
  {
    return mSize;
  }

  /**
   * Given an index in the range <code>0...size()-1</code>, returns the key from
   * the <code>index</code>th key-value mapping that this SparseShortArray
   * stores.
   *
   * <p>
   * The keys corresponding to indices in ascending order are guaranteed to be
   * in ascending order, e.g., <code>keyAt(0)</code> will return the smallest
   * key and <code>keyAt(size()-1)</code> will return the largest key.
   * </p>
   */
  public short keyAt(int index)
  {
    return mKeys[index];
  }

  /**
   * Given an index in the range <code>0...size()-1</code>, returns the value
   * from the <code>index</code>th key-value mapping that this SparseShortArray
   * stores.
   *
   * <p>
   * The values corresponding to indices in ascending order are guaranteed to be
   * associated with keys in ascending order, e.g., <code>valueAt(0)</code> will
   * return the value associated with the smallest key and
   * <code>valueAt(size()-1)</code> will return the value associated with the
   * largest key.
   * </p>
   */
  public short valueAt(int index)
  {
    return mValues[index];
  }

  /**
   * Returns the index for which {@link #keyAt} would return the specified key,
   * or a negative number if the specified key is not mapped.
   * 
   * @throws ArithmeticException
   *           if key is outside the range of a short value
   */
  public int indexOfKey(int key)
  {
    checkOverflow(key);
    return ContainerHelpers.binarySearch(mKeys, mSize, (short) key);
  }

  /**
   * Returns an index for which {@link #valueAt} would return the specified key,
   * or a negative number if no keys map to the specified value. Beware that
   * this is a linear search, unlike lookups by key, and that multiple keys can
   * map to the same value and this will find only one of them.
   */
  public int indexOfValue(int value)
  {
    for (int i = 0; i < mSize; i++)
    {
      if (mValues[i] == value)
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Removes all key-value mappings from this SparseShortArray.
   */
  public void clear()
  {
    mSize = 0;
  }

  /**
   * Puts a key/value pair into the array, optimizing for the case where the key
   * is greater than all existing keys in the array.
   */
  public void append(int key, int value)
  {
    if (mSize != 0 && key <= mKeys[mSize - 1])
    {
      put(key, value);
      return;
    }
    int pos = mSize;
    if (pos >= mKeys.length)
    {
      int n = idealShortArraySize(pos + 1);
      short[] nkeys = new short[n];
      short[] nvalues = new short[n];
      // Log.e("SparseShortArray", "grow " + mKeys.length + " to " + n);
      System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
      System.arraycopy(mValues, 0, nvalues, 0, mValues.length);
      mKeys = nkeys;
      mValues = nvalues;
    }
    checkOverflow(key);
    checkOverflow(value);
    mKeys[pos] = (short) key;
    mValues[pos] = (short) value;
    mSize = pos + 1;
  }

  /**
   * Throws an exception if the value is outside the range of a short.
   * 
   * @param value
   * @throws ArithmeticException
   */
  public static void checkOverflow(int value)
  {
    if (value > Short.MAX_VALUE || value < Short.MIN_VALUE)
    {
      throw new ArithmeticException(String.valueOf(value));
    }
  }

  /**
   * Inlined here by copying from com.android.internal.util.ArrayUtils
   * 
   * @param i
   * @return
   */
  public static int idealShortArraySize(int need)
  {
    return idealByteArraySize(need * 2) / 2;
  }

  /**
   * Inlined here by copying from com.android.internal.util.ArrayUtils
   * 
   * @param i
   * @return
   */
  public static int idealByteArraySize(int need)
  {
    for (int i = 4; i < 32; i++)
    {
      if (need <= (1 << i) - 12)
      {
        return (1 << i) - 12;
      }
    }

    return need;
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * This implementation composes a string by iterating over its mappings.
   */
  @Override
  public String toString()
  {
    if (size() <= 0)
    {
      return "{}";
    }
    StringBuilder buffer = new StringBuilder(mSize * 28);
    buffer.append('{');
    for (int i = 0; i < mSize; i++)
    {
      if (i > 0)
      {
        buffer.append(", ");
      }
      int key = keyAt(i);
      buffer.append(key);
      buffer.append('=');
      int value = valueAt(i);
      buffer.append(value);
    }
    buffer.append('}');
    return buffer.toString();
  }

  /**
   * Method (copied from put) added for Jalview to efficiently increment a key's
   * value if present, else add it with the given value. This avoids a double
   * binary search (once to get the value, again to put the updated value).
   * 
   * @param key
   * @oparam toAdd
   * @return the new value of the count for the key
   * @throws ArithmeticException
   *           if key, or result of adding toAdd, is outside the range of a
   *           short value
   */
  public int add(int key, int toAdd)
  {
    int newValue = toAdd;
    checkOverflow(key);
    int i = ContainerHelpers.binarySearch(mKeys, mSize, (short) key);
    if (i >= 0)
    {
      checkOverflow(toAdd + mValues[i]);
      mValues[i] += (short) toAdd;
      newValue = mValues[i];
    }
    else
    {
      checkOverflow(toAdd);
      i = ~i;
      if (mSize >= mKeys.length)
      {
        int n = idealShortArraySize(mSize + 1);
        short[] nkeys = new short[n];
        short[] nvalues = new short[n];
        System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
        System.arraycopy(mValues, 0, nvalues, 0, mValues.length);
        mKeys = nkeys;
        mValues = nvalues;
      }
      if (mSize - i != 0)
      {
        System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
        System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
      }
      mKeys[i] = (short) key;
      mValues[i] = (short) toAdd;
      mSize++;
    }
    return newValue;
  }
}
