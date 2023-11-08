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
 * Copyright (C) 2013 The Android Open Source Project
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

/*
 * Copied to Jalview September 2016.
 * Only the members of this class required for SparseIntArray were copied.
 * Change Log:
 * Sep 2016: Method binarySearch(short[] array, int size, short value) added to support
 * SparseShortArray.
 * Jan 2017: EMPTY_DOUBLES added
 */
class ContainerHelpers
{
  static final boolean[] EMPTY_BOOLEANS = new boolean[0];

  static final int[] EMPTY_INTS = new int[0];

  static final double[] EMPTY_DOUBLES = new double[0];

  static final long[] EMPTY_LONGS = new long[0];

  static final Object[] EMPTY_OBJECTS = new Object[0];

  // This is Arrays.binarySearch(), but doesn't do any argument validation.
  static int binarySearch(int[] array, int size, int value)
  {
    int lo = 0;
    int hi = size - 1;
    while (lo <= hi)
    {
      final int mid = (lo + hi) >>> 1;
      final int midVal = array[mid];
      if (midVal < value)
      {
        lo = mid + 1;
      }
      else if (midVal > value)
      {
        hi = mid - 1;
      }
      else
      {
        return mid; // value found
      }
    }
    return ~lo; // value not present
  }

  static int binarySearch(long[] array, int size, long value)
  {
    int lo = 0;
    int hi = size - 1;
    while (lo <= hi)
    {
      final int mid = (lo + hi) >>> 1;
      final long midVal = array[mid];
      if (midVal < value)
      {
        lo = mid + 1;
      }
      else if (midVal > value)
      {
        hi = mid - 1;
      }
      else
      {
        return mid; // value found
      }
    }
    return ~lo; // value not present
  }

  // This is Arrays.binarySearch(), but doesn't do any argument validation.
  static int binarySearch(short[] array, int size, short value)
  {
    int lo = 0;
    int hi = size - 1;
    while (lo <= hi)
    {
      final int mid = (lo + hi) >>> 1;
      final short midVal = array[mid];
      if (midVal < value)
      {
        lo = mid + 1;
      }
      else if (midVal > value)
      {
        hi = mid - 1;
      }
      else
      {
        return mid; // value found
      }
    }
    return ~lo; // value not present
  }
}
