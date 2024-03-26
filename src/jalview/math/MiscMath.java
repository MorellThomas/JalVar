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
package jalview.math;

import jalview.util.Format;

import java.lang.Math;
import java.util.Arrays;

/**
 * A collection of miscellaneous mathematical operations
 * @AUTHOR MorellThomas
 */
public class MiscMath
{
  /**
  * prints an array
  * @param m ~ array
  */
  public static void print(double[] m, String format)
  {
    System.out.print("[ ");
    for (double a : m)
    {
      Format.print(System.out, format + " ", a);
    }
    System.out.println("]");
  }
  
  /**
   * rounds the number to a specified precision
   * 
   * @param n ~ number
   * @param p ~ precision
   * 
   * @return
   */
  public static float round(float n, int p)
  {
    return (float) Math.round(n * 10f * p) / ( 10f * p);
  }
  public static double round(double n, int p)
  {
    return (double) Math.round(n * 10d * p) / ( 10d * p);
  }

  /**
  * calculates the mean of an array 
  *
  * @param m ~ array
  * @return
  */
  public static double mean(double[] m)
  {
    double sum = 0;
    int nanCount = 0;
    for (int i = 0; i < m.length; i++)
    {
      if (!Double.isNaN(m[i]))	// ignore NaN values in the array
      {
        sum += m[i];
      } else {
	nanCount++;
      }
    }
    return sum / (double) (m.length - nanCount);
  }

  /**
  * calculates the sum of an array 
  *
  * @param m ~ array
  * @return
  */
  public static double sum(double[] m)
  {
    double sum = 0;
    for (int i = 0; i < m.length; i++)
    {
      if (!Double.isNaN(m[i]))	// ignore NaN values in the array
      {
        sum += m[i];
      }
    }
    return sum;
  }

  /**
  * calculates the square root of each element in an array
  *
  * @param m ~ array
  *
  * @return
  * TODO
  * make general with function passed -> apply function to each element
  */
  public static double[] sqrt(double[] m)
  {
    double[] sqrts = new double[m.length];
    for (int i = 0; i < m.length; i++)
    {
      sqrts[i] = Math.sqrt(m[i]);
    }
    return sqrts;
  }

  /**
  * calculate element wise multiplication of two arrays with the same length
  *
  * @param a ~ array
  * @param b ~ array
  *
  * @return
  */
  public static double[] elementwiseMultiply(byte[] a, double[] b) throws RuntimeException
  {
    if (a.length != b.length)	// throw exception if the arrays do not have the same length
    {
      throw new SameLengthException(a.length, b.length);
    }
    double[] result = new double[a.length];
    for (int i = 0; i < a.length; i++)
    {
      result[i] = a[i] * b[i];
    }
    return result;
  }
  public static double[] elementwiseMultiply(double[] a, double[] b) throws RuntimeException
  {
    if (a.length != b.length)	// throw exception if the arrays do not have the same length
    {
      throw new SameLengthException(a.length, b.length);
    }
    double[] result = new double[a.length];
    for (int i = 0; i < a.length; i++)
    {
      result[i] = a[i] * b[i];
    }
    return result;
  }
  public static byte[] elementwiseMultiply(byte[] a, byte[] b) throws RuntimeException
  {
    if (a.length != b.length)	// throw exception if the arrays do not have the same length
    {
      throw new SameLengthException(a.length, b.length);
    }
    byte[] result = new byte[a.length];
    for (int i = 0; i < a.length; i++)
    {
      result[i] = (byte) (a[i] * b[i]);
    }
    return result;
  }
  public static double[] elementwiseMultiply(double[] a, double b)
  {
    double[] result = new double[a.length];
    for (int i = 0; i < a.length; i++)
    {
      result[i] = a[i] * b;
    }
    return result;
  }

  /**
  * calculate element wise division of two arrays ~ a / b
  *
  * @param a ~ array
  * @param b ~ array
  *
  * @return
  */
  public static double[] elementwiseDivide(double[] a, double[] b) throws RuntimeException
  {
    if (a.length != b.length)	// throw exception if the arrays do not have the same length
    {
      throw new SameLengthException(a.length, b.length);
    }
    double[] result = new double[a.length];
    for (int i = 0; i < a.length; i++)
    {
      result[i] = a[i] / b[i];
    }
    return result;
  }

  /**
  * calculate element wise addition of two arrays
  *
  * @param a ~ array
  * @param b ~ array
  *
  * @return
  */
  public static double[] elementwiseAdd(double[] a, double[] b) throws RuntimeException
  {
    if (a.length != b.length)	// throw exception if the arrays do not have the same length
    {
      throw new SameLengthException(a.length, b.length);
    }
    double[] result = new double[a.length];

    for (int i = 0; i < a.length; i++)
    {
      result[i] += a[i] + b[i];
    }
    return result;
  }
  public static double[] elementwiseAdd(double[] a, double b)
  {
    double[] result = new double[a.length];
    for (int i = 0; i < a.length; i++)
    {
      result[i] = a[i] + b;
    }
    return result;
  }

  /**
  * returns true if two arrays are element wise within a tolerance
  *
  * @param a ~ array
  * @param b ~ array
  * @param rtol ~ relative tolerance
  * @param atol ~ absolute tolerance
  * @param equalNAN ~ whether NaN at the same position return true
  *
  * @return
  */
  public static boolean allClose(double[] a, double[] b, double rtol, double atol, boolean equalNAN)
  {
    boolean areEqual = true;
    for (int i = 0; i < a.length; i++)
    {
      if (equalNAN && (Double.isNaN(a[i]) && Double.isNaN(b[i])))	// if equalNAN == true -> skip the NaN pair
      {
	continue;
      }
      if (Math.abs(a[i] - b[i]) > (atol + rtol * Math.abs(b[i])))	// check for the similarity condition -> if not met -> break and return false
      {
	areEqual = false;
	break;
      }
    }
    return areEqual;
  }

  /**
  * returns the index of the maximum and the maximum value of an array
  * 
  * @param a ~ array
  *
  * @return
  */
  public static int[] findMax(int[] a)
  {
    int max = 0;
    int maxIndex = 0;
    for (int i = 0; i < a.length; i++)
    {
      if (a[i] > max)
      {
	max = a[i];
	maxIndex = i;
      }
    }
    return new int[]{maxIndex, max};
  }
  public static float[] findMax(float[] a)
  {
    float max = 0;
    float maxIndex = 0f;
    for (int i = 0; i < a.length; i++)
    {
      if (a[i] > max)
      {
	max = a[i];
	maxIndex = i;
      }
    }
    return new float[]{maxIndex, max};
  }

  /**
  * returns the dot product of two arrays
  * @param a ~ array a
  * @param b ~ array b
  *
  * @return
  */
  public static double dot(double[] a, double[] b)
  {
    if (a.length != b.length)
    {
      throw new IllegalArgumentException(String.format("Vectors do not have the same length (%d, %d)!", a.length, b.length));
    }

    double aibi = 0;
    for (int i = 0; i < a.length; i++)
    {
      aibi += a[i] * b[i];
    }
    return aibi;
  }

  /**
  * returns the euklidian norm of the vector
  * @param v ~ vector
  *
  * @return
  */
  public static double norm(double[] v)
  {
    double result = 0;
    for (double i : v)
    {
      result += Math.pow(i, 2);
    }
  return Math.sqrt(result);
  }

  /**
  * returns the number of NaN in the vector
  * @param v ~ vector
  *
  * @return
  */
  public static int countNaN(double[] v)
  {
    int cnt = 0;
    for (double i : v)
    {
      if (Double.isNaN(i))
      {
	cnt++;
      }
    }
    return cnt;
  }
  
}
