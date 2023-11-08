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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Random;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

public class SparseMatrixTest
{
  final static double DELTA = 0.0001d;

  Random r = new Random(1729);

  @Test(groups = "Functional")
  public void testConstructor()
  {
    MatrixI m1 = new SparseMatrix(
            new double[][]
            { { 2, 0, 4 }, { 0, 6, 0 } });
    assertEquals(m1.getValue(0, 0), 2d);
    assertEquals(m1.getValue(0, 1), 0d);
    assertEquals(m1.getValue(0, 2), 4d);
    assertEquals(m1.getValue(1, 0), 0d);
    assertEquals(m1.getValue(1, 1), 6d);
    assertEquals(m1.getValue(1, 2), 0d);
  }

  @Test(groups = "Functional")
  public void testTranspose()
  {
    MatrixI m1 = new SparseMatrix(
            new double[][]
            { { 2, 0, 4 }, { 5, 6, 0 } });
    MatrixI m2 = m1.transpose();
    assertTrue(m2 instanceof SparseMatrix);
    assertEquals(m2.height(), 3);
    assertEquals(m2.width(), 2);
    assertEquals(m2.getValue(0, 0), 2d);
    assertEquals(m2.getValue(0, 1), 5d);
    assertEquals(m2.getValue(1, 0), 0d);
    assertEquals(m2.getValue(1, 1), 6d);
    assertEquals(m2.getValue(2, 0), 4d);
    assertEquals(m2.getValue(2, 1), 0d);
  }

  @Test(groups = "Functional")
  public void testPreMultiply()
  {
    MatrixI m1 = new SparseMatrix(new double[][] { { 2, 3, 4 } }); // 1x3
    MatrixI m2 = new SparseMatrix(new double[][] { { 5 }, { 6 }, { 7 } }); // 3x1

    /*
     * 1x3 times 3x1 is 1x1
     * 2x5 + 3x6 + 4*7 =  56
     */
    MatrixI m3 = m2.preMultiply(m1);
    assertFalse(m3 instanceof SparseMatrix);
    assertEquals(m3.height(), 1);
    assertEquals(m3.width(), 1);
    assertEquals(m3.getValue(0, 0), 56d);

    /*
     * 3x1 times 1x3 is 3x3
     */
    m3 = m1.preMultiply(m2);
    assertEquals(m3.height(), 3);
    assertEquals(m3.width(), 3);
    assertEquals(m3.getValue(0, 0), 10d);
    assertEquals(m3.getValue(0, 1), 15d);
    assertEquals(m3.getValue(0, 2), 20d);
    assertEquals(m3.getValue(1, 0), 12d);
    assertEquals(m3.getValue(1, 1), 18d);
    assertEquals(m3.getValue(1, 2), 24d);
    assertEquals(m3.getValue(2, 0), 14d);
    assertEquals(m3.getValue(2, 1), 21d);
    assertEquals(m3.getValue(2, 2), 28d);
  }

  @Test(
    groups = "Functional",
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testPreMultiply_tooManyColumns()
  {
    Matrix m1 = new SparseMatrix(
            new double[][]
            { { 2, 3, 4 }, { 3, 4, 5 } }); // 2x3

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
    Matrix m1 = new SparseMatrix(
            new double[][]
            { { 2, 3, 4 }, { 3, 4, 5 } }); // 2x3

    /*
     * 3x2 times 3x2 invalid operation - 
     * multiplier has more columns than multiplicand has row
     */
    m1.preMultiply(m1);
    fail("Expected exception");
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
    MatrixI m1 = new SparseMatrix(new double[][] { { 2, 3 }, { 4, 5 } });
    MatrixI m2 = new SparseMatrix(
            new double[][]
            { { 10, 100 }, { 1000, 10000 } });
    MatrixI m3 = m1.postMultiply(m2);
    assertEquals(m3.getValue(0, 0), 3020d);
    assertEquals(m3.getValue(0, 1), 30200d);
    assertEquals(m3.getValue(1, 0), 5040d);
    assertEquals(m3.getValue(1, 1), 50400d);

    /*
     * also check m2.preMultiply(m1) - should be same as m1.postMultiply(m2) 
     */
    MatrixI m4 = m2.preMultiply(m1);
    assertMatricesMatch(m3, m4, 0.00001d);

    /*
     * m1 has more rows than columns
     * (2).(10 100 1000) = (20 200 2000)
     * (3)                 (30 300 3000)
     */
    m1 = new SparseMatrix(new double[][] { { 2 }, { 3 } });
    m2 = new SparseMatrix(new double[][] { { 10, 100, 1000 } });
    m3 = m1.postMultiply(m2);
    assertEquals(m3.height(), 2);
    assertEquals(m3.width(), 3);
    assertEquals(m3.getValue(0, 0), 20d);
    assertEquals(m3.getValue(0, 1), 200d);
    assertEquals(m3.getValue(0, 2), 2000d);
    assertEquals(m3.getValue(1, 0), 30d);
    assertEquals(m3.getValue(1, 1), 300d);
    assertEquals(m3.getValue(1, 2), 3000d);

    m4 = m2.preMultiply(m1);
    assertMatricesMatch(m3, m4, 0.00001d);

    /*
     * m1 has more columns than rows
     * (2 3 4) . (5 4) = (56 25)
     *           (6 3) 
     *           (7 2)
     * [0, 0] = 2*5 + 3*6 + 4*7 = 56
     * [0, 1] = 2*4 + 3*3 + 4*2 = 25  
     */
    m1 = new SparseMatrix(new double[][] { { 2, 3, 4 } });
    m2 = new SparseMatrix(new double[][] { { 5, 4 }, { 6, 3 }, { 7, 2 } });
    m3 = m1.postMultiply(m2);
    assertEquals(m3.height(), 1);
    assertEquals(m3.width(), 2);
    assertEquals(m3.getValue(0, 0), 56d);
    assertEquals(m3.getValue(0, 1), 25d);

    /*
     * and check premultiply equivalent
     */
    m4 = m2.preMultiply(m1);
    assertMatricesMatch(m3, m4, 0.00001d);
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
   * Verify that the results of method tred() are the same for SparseMatrix as
   * they are for Matrix (i.e. a regression test rather than an absolute test of
   * correctness of results)
   */
  @Test(groups = "Functional")
  public void testTred_matchesMatrix()
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
    Matrix m2 = new SparseMatrix(d1);
    assertMatricesMatch(m1, m2, 0.00001d); // sanity check
    m1.tred();
    m2.tred();
    assertMatricesMatch(m1, m2, 0.00001d);
  }

  private void assertMatricesMatch(MatrixI m1, MatrixI m2, double delta)
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
    ArrayAsserts.assertArrayEquals(m1.getD(), m2.getD(), delta);
    ArrayAsserts.assertArrayEquals(m1.getE(), m2.getE(), 0.00001d);
  }

  @Test
  public void testGetValue()
  {
    double[][] d = new double[][] { { 0, 0, 1, 0, 0 }, { 2, 3, 0, 0, 0 },
        { 4, 0, 0, 0, 5 } };
    MatrixI m = new SparseMatrix(d);
    for (int row = 0; row < 3; row++)
    {
      for (int col = 0; col < 5; col++)
      {
        assertEquals(m.getValue(row, col), d[row][col],
                String.format("At [%d, %d]", row, col));
      }
    }
  }

  /**
   * Verify that the results of method tqli() are the same for SparseMatrix as
   * they are for Matrix (i.e. a regression test rather than an absolute test of
   * correctness of results)
   * 
   * @throws Exception
   */
  @Test(groups = "Functional")
  public void testTqli_matchesMatrix() throws Exception
  {
    /*
     * make a pseudo-random symmetric matrix as required for tred
     */
    int rows = 6;
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
    Matrix m2 = new SparseMatrix(d1);

    // have to do tred() before doing tqli()
    m1.tred();
    m2.tred();
    assertMatricesMatch(m1, m2, 0.00001d);

    m1.tqli();
    m2.tqli();
    assertMatricesMatch(m1, m2, 0.00001d);
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
   * Test that verifies that the result of preMultiply is a SparseMatrix if more
   * than 80% zeroes, else a Matrix
   */
  @Test(groups = "Functional")
  public void testPreMultiply_sparseProduct()
  {
    MatrixI m1 = new SparseMatrix(
            new double[][]
            { { 1 }, { 0 }, { 0 }, { 0 }, { 0 } }); // 5x1
    MatrixI m2 = new SparseMatrix(new double[][] { { 1, 1, 1, 1 } }); // 1x4

    /*
     * m1.m2 makes a row of 4 1's, and 4 rows of zeros
     * 20% non-zero so not 'sparse'
     */
    MatrixI m3 = m2.preMultiply(m1);
    assertFalse(m3 instanceof SparseMatrix);

    /*
     * replace a 1 with a 0 in the product:
     * it is now > 80% zero so 'sparse'
     */
    m2 = new SparseMatrix(new double[][] { { 1, 1, 1, 0 } });
    m3 = m2.preMultiply(m1);
    assertTrue(m3 instanceof SparseMatrix);
  }

  @Test(groups = "Functional")
  public void testFillRatio()
  {
    SparseMatrix m1 = new SparseMatrix(
            new double[][]
            { { 2, 0, 4, 1, 0 }, { 0, 6, 0, 0, 0 } });
    assertEquals(m1.getFillRatio(), 0.4f);
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
    Matrix m1 = new SparseMatrix(d);
    Matrix m2 = new SparseMatrix(d1);
    assertMatricesMatch(m1, m2, 1.0e16); // sanity check
    m1.tred();
    m2.tred();
    assertMatricesMatch(m1, m2, 0.00001d);
  }
}