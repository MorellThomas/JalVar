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

import java.util.Arrays;

public class ArrayUtils
{
  /**
   * Reverse the given array 'in situ'
   * 
   * @param arr
   */
  public static void reverseIntArray(int[] arr)
  {
    if (arr != null)
    {
      /*
       * swap [k] with [end-k] up to the half way point in the array
       * if length is odd, the middle entry is left untouched by the excitement
       */
      int last = arr.length - 1;
      for (int k = 0; k < arr.length / 2; k++)
      {
        int temp = arr[k];
        arr[k] = arr[last - k];
        arr[last - k] = temp;
      }
    }
  }

  public static <T> T[] concatArrays(T[]... arrays)
  {
    if (arrays == null)
      return null;
    if (arrays.length == 1)
      return arrays[0];

    T[] result = arrays[0];
    for (int i = 1; i < arrays.length; i++)
    {
      result = concatTwoArrays(result, arrays[i]);
    }
    return result;
  }

  private static <T> T[] concatTwoArrays(T[] array1, T[] array2)
  {
    T[] result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }

}
