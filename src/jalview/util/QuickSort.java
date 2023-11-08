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
import java.util.Comparator;

/**
 * A class to perform efficient sorting of arrays of objects based on arrays of
 * scores or other attributes. For example, residues by percentage frequency.
 * 
 * @author gmcarstairs
 *
 */
public class QuickSort
{
  /**
   * A comparator that compares two integers by comparing their respective
   * indexed values in an array of floats
   */
  static class FloatComparator implements Comparator<Integer>
  {
    private final float[] values;

    private boolean ascending;

    FloatComparator(float[] v, boolean asc)
    {
      values = v;
      ascending = asc;
    }

    @Override
    public int compare(Integer o1, Integer o2)
    {
      return ascending
              ? Float.compare(values[o1.intValue()], values[o2.intValue()])
              : Float.compare(values[o2.intValue()], values[o1.intValue()]);
    }
  }

  /**
   * A comparator that compares two integers by comparing their respective
   * indexed values in an array of doubles
   */
  static class DoubleComparator implements Comparator<Integer>
  {
    private final double[] values;

    private boolean ascending;

    DoubleComparator(double[] v, boolean asc)
    {
      values = v;
      ascending = asc;
    }

    @Override
    public int compare(Integer o1, Integer o2)
    {
      if (ascending)
      {
        return Double.compare(values[o1.intValue()], values[o2.intValue()]);
      }
      else
      {
        return Double.compare(values[o2.intValue()], values[o1.intValue()]);
      }
    }
  }

  /**
   * A comparator that compares two integers by comparing their respective
   * indexed values in an array of ints
   */
  static class IntComparator implements Comparator<Integer>
  {
    private final int[] values;

    private boolean ascending;

    IntComparator(int[] v, boolean asc)
    {
      values = v;
      ascending = asc;
    }

    @Override
    public int compare(Integer o1, Integer o2)
    {
      return ascending
              ? Integer.compare(values[o1.intValue()],
                      values[o2.intValue()])
              : Integer.compare(values[o2.intValue()],
                      values[o1.intValue()]);
    }
  }

  /**
   * A comparator that compares two integers by comparing their respective
   * indexed values in an array of comparable objects.
   */
  static class ExternalComparator implements Comparator<Integer>
  {
    private final Comparable[] values;

    private boolean ascending;

    ExternalComparator(Comparable[] v, boolean asc)
    {
      values = v;
      ascending = asc;
    }

    @Override
    public int compare(Integer o1, Integer o2)
    {
      return ascending
              ? values[o1.intValue()].compareTo(values[o2.intValue()])
              : values[o2.intValue()].compareTo(values[o1.intValue()]);
    }
  }

  /**
   * Sorts both arrays with respect to ascending order of the items in the first
   * array.
   * 
   * @param arr
   * @param s
   */
  public static void sort(int[] arr, Object[] s)
  {
    sort(arr, 0, arr.length - 1, s);
  }

  /**
   * Sorts both arrays with respect to ascending order of the items in the first
   * array.
   * 
   * @param arr
   * @param s
   */
  public static void sort(float[] arr, Object[] s)
  {
    sort(arr, 0, arr.length - 1, s);
  }

  /**
   * Sorts both arrays with respect to ascending order of the items in the first
   * array.
   * 
   * @param arr
   * @param s
   */
  public static void sort(double[] arr, Object[] s)
  {
    sort(arr, 0, arr.length - 1, s);
  }

  /**
   * Sorts both arrays with respect to descending order of the items in the
   * first array. The sorting is case-sensitive.
   * 
   * @param arr
   * @param s
   */
  public static void sort(String[] arr, Object[] s)
  {
    stringSort(arr, 0, arr.length - 1, s);
  }

  static void stringSort(String[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = stringPartition(arr, p, r, s);
      stringSort(arr, p, q, s);
      stringSort(arr, q + 1, r, s);
    }
  }

  static void sort(float[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  static void sort(double[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  static void sort(int[] arr, int p, int r, Object[] s)
  {
    int q;

    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  static int partition(float[] arr, int p, int r, Object[] s)
  {
    float x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        float tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int partition(float[] arr, int p, int r, char[] s)
  {
    float x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        float tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        char tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int partition(int[] arr, int p, int r, Object[] s)
  {
    int x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int partition(double[] arr, int p, int r, Object[] s)
  {
    double x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j] > x);

      do
      {
        i = i + 1;
      } while (arr[i] < x);

      if (i < j)
      {
        double tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  static int stringPartition(String[] arr, int p, int r, Object[] s)
  {
    String x = arr[p];
    int i = p - 1;
    int j = r + 1;

    while (true)
    {
      do
      {
        j = j - 1;
      } while (arr[j].compareTo(x) < 0);

      do
      {
        i = i + 1;
      } while (arr[i].compareTo(x) > 0);

      if (i < j)
      {
        String tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        Object tmp2 = s[i];
        s[i] = s[j];
        s[j] = tmp2;
      }
      else
      {
        return j;
      }
    }
  }

  /**
   * Sorts both arrays to give ascending order by the first array, by first
   * partitioning into zero and non-zero values before sorting the latter. This
   * is faster than a direct call to charSortByFloat in the case where most of
   * the array to be sorted is zero.
   * 
   * @param arr
   * @param s
   */
  public static void sort(float[] arr, char[] s)
  {
    /*
     * Move all zero values to the front, non-zero to the back, while counting
     * negative values
     */
    float[] f1 = new float[arr.length];
    char[] s1 = new char[s.length];
    int negativeCount = 0;
    int zerosCount = 0;
    int nextNonZeroValue = arr.length - 1;
    for (int i = 0; i < arr.length; i++)
    {
      float val = arr[i];
      if (val != 0f)
      {
        f1[nextNonZeroValue] = val;
        s1[nextNonZeroValue] = s[i];
        nextNonZeroValue--;
        if (val < 0f)
        {
          negativeCount++;
        }
      }
      else
      {
        f1[zerosCount] = val;
        s1[zerosCount] = s[i];
        zerosCount++;
      }
    }
    int positiveCount = arr.length - zerosCount - negativeCount;

    if (zerosCount == arr.length)
    {
      return; // all zero
    }

    /*
     * sort the non-zero values
     */
    float[] nonZeroFloats = Arrays.copyOfRange(f1, zerosCount, f1.length);
    char[] nonZeroChars = Arrays.copyOfRange(s1, zerosCount, s1.length);
    charSortByFloat(nonZeroFloats, nonZeroChars, true);

    /*
     * Backfill zero values to original arrays, after the space reserved for
     * negatives
     */
    System.arraycopy(f1, 0, arr, negativeCount, zerosCount);
    System.arraycopy(s1, 0, s, negativeCount, zerosCount);

    /*
     * Copy sorted negative values to the front of arr, s
     */
    System.arraycopy(nonZeroFloats, 0, arr, 0, negativeCount);
    System.arraycopy(nonZeroChars, 0, s, 0, negativeCount);

    /*
     * Copy sorted positive values after the negatives and zeros
     */
    System.arraycopy(nonZeroFloats, negativeCount, arr,
            negativeCount + zerosCount, positiveCount);
    System.arraycopy(nonZeroChars, negativeCount, s,
            negativeCount + zerosCount, positiveCount);
  }

  /**
   * Sorts arrays of float and char by the float values, by making an array of
   * indices, and sorting it using a comparator that refers to the float values.
   * 
   * @see http
   *      ://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-
   *      after-sorting
   * @param arr
   * @param s
   * @param ascending
   */
  public static void charSortByFloat(float[] arr, char[] s,
          boolean ascending)
  {
    final int length = arr.length;
    Integer[] indices = makeIndexArray(length);
    Arrays.sort(indices, new FloatComparator(arr, ascending));

    /*
     * Copy the array values as per the sorted indices
     */
    float[] sortedFloats = new float[length];
    char[] sortedChars = new char[s.length];
    for (int i = 0; i < length; i++)
    {
      sortedFloats[i] = arr[indices[i]];
      sortedChars[i] = s[indices[i]];
    }

    /*
     * And copy the sorted values back into the arrays
     */
    System.arraycopy(sortedFloats, 0, arr, 0, length);
    System.arraycopy(sortedChars, 0, s, 0, s.length);
  }

  /**
   * Make an array whose values are 0...length.
   * 
   * @param length
   * @return
   */
  protected static Integer[] makeIndexArray(final int length)
  {
    Integer[] indices = new Integer[length];
    for (int i = 0; i < length; i++)
    {
      indices[i] = i;
    }
    return indices;
  }

  static void sort(float[] arr, int p, int r, char[] s)
  {
    int q;
    if (p < r)
    {
      q = partition(arr, p, r, s);
      sort(arr, p, q, s);
      sort(arr, q + 1, r, s);
    }
  }

  /**
   * Sorts both arrays to give ascending order in the first array, by first
   * partitioning into zero and non-zero values before sorting the latter. This
   * is faster than a direct call to charSortByInt in the case where most of the
   * array to be sorted is zero.
   * 
   * @param arr
   * @param s
   */
  public static void sort(int[] arr, char[] s)
  { /*
     * Move all zero values to the front, non-zero to the back, while counting
     * negative values
     */
    int[] f1 = new int[arr.length];
    char[] s1 = new char[s.length];
    int negativeCount = 0;
    int zerosCount = 0;
    int nextNonZeroValue = arr.length - 1;
    for (int i = 0; i < arr.length; i++)
    {
      int val = arr[i];
      if (val != 0f)
      {
        f1[nextNonZeroValue] = val;
        s1[nextNonZeroValue] = s[i];
        nextNonZeroValue--;
        if (val < 0)
        {
          negativeCount++;
        }
      }
      else
      {
        f1[zerosCount] = val;
        s1[zerosCount] = s[i];
        zerosCount++;
      }
    }
    int positiveCount = arr.length - zerosCount - negativeCount;

    if (zerosCount == arr.length)
    {
      return; // all zero
    }

    /*
     * sort the non-zero values
     */
    int[] nonZeroInts = Arrays.copyOfRange(f1, zerosCount, f1.length);
    char[] nonZeroChars = Arrays.copyOfRange(s1, zerosCount, s1.length);
    charSortByInt(nonZeroInts, nonZeroChars, true);

    /*
     * Backfill zero values to original arrays, after the space reserved for
     * negatives
     */
    System.arraycopy(f1, 0, arr, negativeCount, zerosCount);
    System.arraycopy(s1, 0, s, negativeCount, zerosCount);

    /*
     * Copy sorted negative values to the front of arr, s
     */
    System.arraycopy(nonZeroInts, 0, arr, 0, negativeCount);
    System.arraycopy(nonZeroChars, 0, s, 0, negativeCount);

    /*
     * Copy sorted positive values after the negatives and zeros
     */
    System.arraycopy(nonZeroInts, negativeCount, arr,
            negativeCount + zerosCount, positiveCount);
    System.arraycopy(nonZeroChars, negativeCount, s,
            negativeCount + zerosCount, positiveCount);
  }

  /**
   * Sorts arrays of int and char, by making an array of indices, and sorting it
   * using a comparator that refers to the int values.
   * 
   * @see http
   *      ://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-
   *      after-sorting
   * @param arr
   * @param s
   * @param ascending
   */
  public static void charSortByInt(int[] arr, char[] s, boolean ascending)
  {
    final int length = arr.length;
    Integer[] indices = makeIndexArray(length);
    Arrays.sort(indices, new IntComparator(arr, ascending));

    /*
     * Copy the array values as per the sorted indices
     */
    int[] sortedInts = new int[length];
    char[] sortedChars = new char[s.length];
    for (int i = 0; i < length; i++)
    {
      sortedInts[i] = arr[indices[i]];
      sortedChars[i] = s[indices[i]];
    }

    /*
     * And copy the sorted values back into the arrays
     */
    System.arraycopy(sortedInts, 0, arr, 0, length);
    System.arraycopy(sortedChars, 0, s, 0, s.length);
  }

  /**
   * Sorts arrays of int and Object, by making an array of indices, and sorting
   * it using a comparator that refers to the int values.
   * 
   * @see http
   *      ://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-
   *      after-sorting
   * @param arr
   * @param s
   * @param ascending
   */
  public static void sortByInt(int[] arr, Object[] s, boolean ascending)
  {
    final int length = arr.length;
    Integer[] indices = makeIndexArray(length);
    Arrays.sort(indices, new IntComparator(arr, ascending));

    /*
     * Copy the array values as per the sorted indices
     */
    int[] sortedInts = new int[length];
    Object[] sortedObjects = new Object[s.length];
    for (int i = 0; i < length; i++)
    {
      sortedInts[i] = arr[indices[i]];
      sortedObjects[i] = s[indices[i]];
    }

    /*
     * And copy the sorted values back into the arrays
     */
    System.arraycopy(sortedInts, 0, arr, 0, length);
    System.arraycopy(sortedObjects, 0, s, 0, s.length);
  }

  /**
   * Sorts arrays of String and Object, by making an array of indices, and
   * sorting it using a comparator that refers to the String values. Both arrays
   * are sorted by case-sensitive order of the string array values.
   * 
   * @see http
   *      ://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-
   *      after-sorting
   * @param arr
   * @param s
   * @param ascending
   */
  public static void sortByString(String[] arr, Object[] s,
          boolean ascending)
  {
    final int length = arr.length;
    Integer[] indices = makeIndexArray(length);
    Arrays.sort(indices, new ExternalComparator(arr, ascending));

    /*
     * Copy the array values as per the sorted indices
     */
    String[] sortedStrings = new String[length];
    Object[] sortedObjects = new Object[s.length];
    for (int i = 0; i < length; i++)
    {
      sortedStrings[i] = arr[indices[i]];
      sortedObjects[i] = s[indices[i]];
    }

    /*
     * And copy the sorted values back into the arrays
     */
    System.arraycopy(sortedStrings, 0, arr, 0, length);
    System.arraycopy(sortedObjects, 0, s, 0, s.length);
  }

  /**
   * Sorts arrays of double and Object, by making an array of indices, and
   * sorting it using a comparator that refers to the double values.
   * 
   * @see http
   *      ://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-
   *      after-sorting
   * @param arr
   * @param s
   * @param ascending
   */
  public static void sortByDouble(double[] arr, Object[] s,
          boolean ascending)
  {
    final int length = arr.length;
    Integer[] indices = makeIndexArray(length);
    Arrays.sort(indices, new DoubleComparator(arr, ascending));

    /*
     * Copy the array values as per the sorted indices
     */
    double[] sortedDoubles = new double[length];
    Object[] sortedObjects = new Object[s.length];
    for (int i = 0; i < length; i++)
    {
      sortedDoubles[i] = arr[indices[i]];
      sortedObjects[i] = s[indices[i]];
    }

    /*
     * And copy the sorted values back into the arrays
     */
    System.arraycopy(sortedDoubles, 0, arr, 0, length);
    System.arraycopy(sortedObjects, 0, s, 0, s.length);
  }
}
