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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

public class MatrixTest
{
  final static double DELTA = 0.000001d;

  @Test(groups = "Timing")
  public void testPreMultiply_timing()
  {
    int rows = 50; // increase to stress test timing
    int cols = 100;
    double[][] d1 = new double[rows][cols];
    double[][] d2 = new double[cols][rows];
    Matrix m1 = new Matrix(d1);
    Matrix m2 = new Matrix(d2);
    long start = System.currentTimeMillis();
    m1.preMultiply(m2);
    long elapsed = System.currentTimeMillis() - start;
    System.out.println(rows + "x" + cols
            + " multiplications of double took " + elapsed + "ms");
  }

  @Test(groups = "Functional")
  public void testPreMultiply()
  {
    Matrix m1 = new Matrix(new double[][] { { 2, 3, 4 } }); // 1x3
    Matrix m2 = new Matrix(new double[][] { { 5 }, { 6 }, { 7 } }); // 3x1

    /*
     * 1x3 times 3x1 is 1x1
     * 2x5 + 3x6 + 4*7 =  56
     */
    MatrixI m3 = m2.preMultiply(m1);
    assertEquals(m3.height(), 1);
    assertEquals(m3.width(), 1);
    assertEquals(m3.getValue(0, 0), 56d);

    /*
     * 3x1 times 1x3 is 3x3
     */
    m3 = m1.preMultiply(m2);
    assertEquals(m3.height(), 3);
    assertEquals(m3.width(), 3);
    assertEquals(Arrays.toString(m3.getRow(0)), "[10.0, 15.0, 20.0]");
    assertEquals(Arrays.toString(m3.getRow(1)), "[12.0, 18.0, 24.0]");
    assertEquals(Arrays.toString(m3.getRow(2)), "[14.0, 21.0, 28.0]");
  }

  @Test(
    groups = "Functional",
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testPreMultiply_tooManyColumns()
  {
    Matrix m1 = new Matrix(new double[][] { { 2, 3, 4 }, { 3, 4, 5 } }); // 2x3

    /*
     * 2x3 times 2x3 invalid operation - 
     * multiplier has more columns than multiplicand has rows
     */
    m1.preMultiply(m1);
    fail("Expected exception");
  }

  @Test(
    groups = "Functional",
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testPreMultiply_tooFewColumns()
  {
    Matrix m1 = new Matrix(new double[][] { { 2, 3, 4 }, { 3, 4, 5 } }); // 2x3

    /*
     * 3x2 times 3x2 invalid operation - 
     * multiplier has more columns than multiplicand has row
     */
    m1.preMultiply(m1);
    fail("Expected exception");
  }

  private boolean matrixEquals(Matrix m1, Matrix m2)
  {
    if (m1.width() != m2.width() || m1.height() != m2.height())
    {
      return false;
    }
    for (int i = 0; i < m1.height(); i++)
    {
      if (!Arrays.equals(m1.getRow(i), m2.getRow(i)))
      {
        return false;
      }
    }
    return true;
  }

  @Test(groups = "Functional")
  public void testPostMultiply()
  {
    /*
     * Square matrices
     * (2 3) . (10   100)
     * (4 5)   (1000 10000)
     * =
     * (3020 30200)
     * (5040 50400)
     */
    MatrixI m1 = new Matrix(new double[][] { { 2, 3 }, { 4, 5 } });
    MatrixI m2 = new Matrix(
            new double[][]
            { { 10, 100 }, { 1000, 10000 } });
    MatrixI m3 = m1.postMultiply(m2);
    assertEquals(Arrays.toString(m3.getRow(0)), "[3020.0, 30200.0]");
    assertEquals(Arrays.toString(m3.getRow(1)), "[5040.0, 50400.0]");

    /*
     * also check m2.preMultiply(m1) - should be same as m1.postMultiply(m2) 
     */
    m3 = m2.preMultiply(m1);
    assertEquals(Arrays.toString(m3.getRow(0)), "[3020.0, 30200.0]");
    assertEquals(Arrays.toString(m3.getRow(1)), "[5040.0, 50400.0]");

    /*
     * m1 has more rows than columns
     * (2).(10 100 1000) = (20 200 2000)
     * (3)                 (30 300 3000)
     */
    m1 = new Matrix(new double[][] { { 2 }, { 3 } });
    m2 = new Matrix(new double[][] { { 10, 100, 1000 } });
    m3 = m1.postMultiply(m2);
    assertEquals(m3.height(), 2);
    assertEquals(m3.width(), 3);
    assertEquals(Arrays.toString(m3.getRow(0)), "[20.0, 200.0, 2000.0]");
    assertEquals(Arrays.toString(m3.getRow(1)), "[30.0, 300.0, 3000.0]");
    m3 = m2.preMultiply(m1);
    assertEquals(m3.height(), 2);
    assertEquals(m3.width(), 3);
    assertEquals(Arrays.toString(m3.getRow(0)), "[20.0, 200.0, 2000.0]");
    assertEquals(Arrays.toString(m3.getRow(1)), "[30.0, 300.0, 3000.0]");

    /*
     * m1 has more columns than rows
     * (2 3 4) . (5 4) = (56 25)
     *           (6 3) 
     *           (7 2)
     * [0, 0] = 2*5 + 3*6 + 4*7 = 56
     * [0, 1] = 2*4 + 3*3 + 4*2 = 25  
     */
    m1 = new Matrix(new double[][] { { 2, 3, 4 } });
    m2 = new Matrix(new double[][] { { 5, 4 }, { 6, 3 }, { 7, 2 } });
    m3 = m1.postMultiply(m2);
    assertEquals(m3.height(), 1);
    assertEquals(m3.width(), 2);
    assertEquals(m3.getRow(0)[0], 56d);
    assertEquals(m3.getRow(0)[1], 25d);

    /*
     * and check premultiply equivalent
     */
    m3 = m2.preMultiply(m1);
    assertEquals(m3.height(), 1);
    assertEquals(m3.width(), 2);
    assertEquals(m3.getRow(0)[0], 56d);
    assertEquals(m3.getRow(0)[1], 25d);
  }

  @Test(groups = "Functional")
  public void testCopy()
  {
    Random r = new Random();
    int rows = 5;
    int cols = 11;
    double[][] in = new double[rows][cols];

    for (int i = 0; i < rows; i++)
    {
      for (int j = 0; j < cols; j++)
      {
        in[i][j] = r.nextDouble();
      }
    }
    Matrix m1 = new Matrix(in);

    Matrix m2 = (Matrix) m1.copy();
    assertNotSame(m1, m2);
    assertTrue(matrixEquals(m1, m2));
    assertNull(m2.d);
    assertNull(m2.e);

    /*
     * now add d and e vectors and recopy
     */
    m1.d = Arrays.copyOf(in[2], in[2].length);
    m1.e = Arrays.copyOf(in[4], in[4].length);
    m2 = (Matrix) m1.copy();
    assertNotSame(m2.d, m1.d);
    assertNotSame(m2.e, m1.e);
    assertEquals(m2.d, m1.d);
    assertEquals(m2.e, m1.e);
  }

  /**
   * main method extracted from Matrix
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    int n = Integer.parseInt(args[0]);
    double[][] in = new double[n][n];

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < n; j++)
      {
        in[i][j] = Math.random();
      }
    }

    Matrix origmat = new Matrix(in);

    // System.out.println(" --- Original matrix ---- ");
    // / origmat.print(System.out);
    // System.out.println();
    // System.out.println(" --- transpose matrix ---- ");
    MatrixI trans = origmat.transpose();

    // trans.print(System.out);
    // System.out.println();
    // System.out.println(" --- OrigT * Orig ---- ");
    MatrixI symm = trans.postMultiply(origmat);

    // symm.print(System.out);
    // System.out.println();
    // Copy the symmetric matrix for later
    // Matrix origsymm = symm.copy();

    // This produces the tridiagonal transformation matrix
    // long tstart = System.currentTimeMillis();
    symm.tred();

    // long tend = System.currentTimeMillis();

    // System.out.println("Time take for tred = " + (tend-tstart) + "ms");
    // System.out.println(" ---Tridiag transform matrix ---");
    // symm.print(System.out);
    // System.out.println();
    // System.out.println(" --- D vector ---");
    // symm.printD(System.out);
    // System.out.println();
    // System.out.println(" --- E vector ---");
    // symm.printE(System.out);
    // System.out.println();
    // Now produce the diagonalization matrix
    // tstart = System.currentTimeMillis();
    symm.tqli();
    // tend = System.currentTimeMillis();

    // System.out.println("Time take for tqli = " + (tend-tstart) + " ms");
    // System.out.println(" --- New diagonalization matrix ---");
    // symm.print(System.out);
    // System.out.println();
    // System.out.println(" --- D vector ---");
    // symm.printD(System.out);
    // System.out.println();
    // System.out.println(" --- E vector ---");
    // symm.printE(System.out);
    // System.out.println();
    // System.out.println(" --- First eigenvector --- ");
    // double[] eigenv = symm.getColumn(0);
    // for (int i=0; i < eigenv.length;i++) {
    // Format.print(System.out,"%15.4f",eigenv[i]);
    // }
    // System.out.println();
    // double[] neigenv = origsymm.vectorPostMultiply(eigenv);
    // for (int i=0; i < neigenv.length;i++) {
    // Format.print(System.out,"%15.4f",neigenv[i]/symm.d[0]);
    // }
    // System.out.println();
  }

  @Test(groups = "Timing")
  public void testSign()
  {
    assertEquals(Matrix.sign(-1, -2), -1d);
    assertEquals(Matrix.sign(-1, 2), 1d);
    assertEquals(Matrix.sign(-1, 0), 1d);
    assertEquals(Matrix.sign(1, -2), -1d);
    assertEquals(Matrix.sign(1, 2), 1d);
    assertEquals(Matrix.sign(1, 0), 1d);
  }

  /**
   * Helper method to make values for a sparse, pseudo-random symmetric matrix
   * 
   * @param rows
   * @param cols
   * @param occupancy
   *          one in 'occupancy' entries will be non-zero
   * @return
   */
  public double[][] getSparseValues(int rows, int cols, int occupancy)
  {
    Random r = new Random(1729);

    /*
     * generate whole number values between -12 and +12
     * (to mimic score matrices used in Jalview)
     */
    double[][] d = new double[rows][cols];
    int m = 0;
    for (int i = 0; i < rows; i++)
    {
      if (++m % occupancy == 0)
      {
        d[i][i] = r.nextInt() % 13; // diagonal
      }
      for (int j = 0; j < i; j++)
      {
        if (++m % occupancy == 0)
        {
          d[i][j] = r.nextInt() % 13;
          d[j][i] = d[i][j];
        }
      }
    }
    return d;

  }

  /**
   * Verify that the results of method tred() are the same if the calculation is
   * redone
   */
  @Test(groups = "Functional")
  public void testTred_reproducible()
  {
    /*
     * make a pseudo-random symmetric matrix as required for tred/tqli
     */
    int rows = 10;
    int cols = rows;
    double[][] d = getSparseValues(rows, cols, 3);

    /*
     * make a copy of the values so m1, m2 are not
     * sharing arrays!
     */
    double[][] d1 = new double[rows][cols];
    for (int row = 0; row < rows; row++)
    {
      for (int col = 0; col < cols; col++)
      {
        d1[row][col] = d[row][col];
      }
    }
    Matrix m1 = new Matrix(d);
    Matrix m2 = new Matrix(d1);
    assertMatricesMatch(m1, m2); // sanity check
    m1.tred();
    m2.tred();
    assertMatricesMatch(m1, m2);
  }

  public static void assertMatricesMatch(MatrixI m1, MatrixI m2)
  {
    if (m1.height() != m2.height())
    {
      fail("height mismatch");
    }
    if (m1.width() != m2.width())
    {
      fail("width mismatch");
    }
    for (int row = 0; row < m1.height(); row++)
    {
      for (int col = 0; col < m1.width(); col++)
      {
        double v2 = m2.getValue(row, col);
        double v1 = m1.getValue(row, col);
        if (Math.abs(v1 - v2) > DELTA)
        {
          fail(String.format("At [%d, %d] %f != %f", row, col, v1, v2));
        }
      }
    }
    ArrayAsserts.assertArrayEquals("D vector", m1.getD(), m2.getD(),
            0.00001d);
    ArrayAsserts.assertArrayEquals("E vector", m1.getE(), m2.getE(),
            0.00001d);
  }

  @Test(groups = "Functional")
  public void testFindMinMax()
  {
    /*
     * empty matrix case
     */
    Matrix m = new Matrix(new double[][] { {} });
    assertNull(m.findMinMax());

    /*
     * normal case
     */
    double[][] vals = new double[2][];
    vals[0] = new double[] { 7d, 1d, -2.3d };
    vals[1] = new double[] { -12d, 94.3d, -102.34d };
    m = new Matrix(vals);
    double[] minMax = m.findMinMax();
    assertEquals(minMax[0], -102.34d);
    assertEquals(minMax[1], 94.3d);
  }

  @Test(groups = { "Functional", "Timing" })
  public void testFindMinMax_timing()
  {
    Random r = new Random();
    int size = 1000; // increase to stress test timing
    double[][] vals = new double[size][size];
    double max = -Double.MAX_VALUE;
    double min = Double.MAX_VALUE;
    for (int i = 0; i < size; i++)
    {
      vals[i] = new double[size];
      for (int j = 0; j < size; j++)
      {
        // use nextLong rather than nextDouble to include negative values
        double d = r.nextLong();
        if (d > max)
        {
          max = d;
        }
        if (d < min)
        {
          min = d;
        }
        vals[i][j] = d;
      }
    }
    Matrix m = new Matrix(vals);
    long now = System.currentTimeMillis();
    double[] minMax = m.findMinMax();
    System.out.println(String.format("findMinMax for %d x %d took %dms",
            size, size, (System.currentTimeMillis() - now)));
    assertEquals(minMax[0], min);
    assertEquals(minMax[1], max);
  }

  /**
   * Test range reversal with maximum value becoming zero
   */
  @Test(groups = "Functional")
  public void testReverseRange_maxToZero()
  {
    Matrix m1 = new Matrix(
            new double[][]
            { { 2, 3.5, 4 }, { -3.4, 4, 15 } });

    /*
     * subtract all from max: range -3.4 to 15 becomes 18.4 to 0
     */
    m1.reverseRange(true);
    assertEquals(m1.getValue(0, 0), 13d, DELTA);
    assertEquals(m1.getValue(0, 1), 11.5d, DELTA);
    assertEquals(m1.getValue(0, 2), 11d, DELTA);
    assertEquals(m1.getValue(1, 0), 18.4d, DELTA);
    assertEquals(m1.getValue(1, 1), 11d, DELTA);
    assertEquals(m1.getValue(1, 2), 0d, DELTA);

    /*
     * repeat operation - range is now 0 to 18.4
     */
    m1.reverseRange(true);
    assertEquals(m1.getValue(0, 0), 5.4d, DELTA);
    assertEquals(m1.getValue(0, 1), 6.9d, DELTA);
    assertEquals(m1.getValue(0, 2), 7.4d, DELTA);
    assertEquals(m1.getValue(1, 0), 0d, DELTA);
    assertEquals(m1.getValue(1, 1), 7.4d, DELTA);
    assertEquals(m1.getValue(1, 2), 18.4d, DELTA);
  }

  /**
   * Test range reversal with minimum and maximum values swapped
   */
  @Test(groups = "Functional")
  public void testReverseRange_swapMinMax()
  {
    Matrix m1 = new Matrix(
            new double[][]
            { { 2, 3.5, 4 }, { -3.4, 4, 15 } });

    /*
     * swap all values in min-max range
     * = subtract from (min + max = 11.6) 
     * range -3.4 to 15 becomes 18.4 to -3.4
     */
    m1.reverseRange(false);
    assertEquals(m1.getValue(0, 0), 9.6d, DELTA);
    assertEquals(m1.getValue(0, 1), 8.1d, DELTA);
    assertEquals(m1.getValue(0, 2), 7.6d, DELTA);
    assertEquals(m1.getValue(1, 0), 15d, DELTA);
    assertEquals(m1.getValue(1, 1), 7.6d, DELTA);
    assertEquals(m1.getValue(1, 2), -3.4d, DELTA);

    /*
     * repeat operation - original values restored
     */
    m1.reverseRange(false);
    assertEquals(m1.getValue(0, 0), 2d, DELTA);
    assertEquals(m1.getValue(0, 1), 3.5d, DELTA);
    assertEquals(m1.getValue(0, 2), 4d, DELTA);
    assertEquals(m1.getValue(1, 0), -3.4d, DELTA);
    assertEquals(m1.getValue(1, 1), 4d, DELTA);
    assertEquals(m1.getValue(1, 2), 15d, DELTA);
  }

  @Test(groups = "Functional")
  public void testMultiply()
  {
    Matrix m = new Matrix(
            new double[][]
            { { 2, 3.5, 4 }, { -3.4, 4, 15 } });
    m.multiply(2d);
    assertEquals(m.getValue(0, 0), 4d, DELTA);
    assertEquals(m.getValue(0, 1), 7d, DELTA);
    assertEquals(m.getValue(0, 2), 8d, DELTA);
    assertEquals(m.getValue(1, 0), -6.8d, DELTA);
    assertEquals(m.getValue(1, 1), 8d, DELTA);
    assertEquals(m.getValue(1, 2), 30d, DELTA);
  }

  @Test(groups = "Functional")
  public void testConstructor()
  {
    double[][] values = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };
    Matrix m = new Matrix(values);
    assertEquals(m.getValue(0, 0), 1d, DELTA);

    /*
     * verify the matrix has a copy of the original array
     */
    assertNotSame(values[0], m.getRow(0));
    values[0][0] = -1d;
    assertEquals(m.getValue(0, 0), 1d, DELTA); // unchanged
  }

  @Test(groups = "Functional")
  public void testEquals()
  {
    double[][] values = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };
    Matrix m1 = new Matrix(values);
    double[][] values2 = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };
    Matrix m2 = new Matrix(values2);

    double delta = 0.0001d;
    assertTrue(m1.equals(m1, delta));
    assertTrue(m1.equals(m2, delta));
    assertTrue(m2.equals(m1, delta));

    double[][] values3 = new double[][] { { 1, 2, 3 }, { 4, 5, 7 } };
    m2 = new Matrix(values3);
    assertFalse(m1.equals(m2, delta));
    assertFalse(m2.equals(m1, delta));

    // must be same shape
    values2 = new double[][] { { 1, 2, 3 } };
    m2 = new Matrix(values2);
    assertFalse(m2.equals(m1, delta));

    assertFalse(m1.equals(null, delta));
  }
}
