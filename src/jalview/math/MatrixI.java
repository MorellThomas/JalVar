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

import java.io.PrintStream;

/**
 * An interface that describes a rectangular matrix of double values and
 * operations on it
 */
public interface MatrixI
{
  /**
   * Answers the number of columns
   * 
   * @return
   */
  int width();

  /**
   * Answers the number of rows
   * 
   * @return
   */
  int height();

  /**
   * Answers the value at row i, column j
   * 
   * @param i
   * @param j
   * @return
   */
  double getValue(int i, int j);

  /**
   * Sets the value at row i, colum j
   * 
   * @param i
   * @param j
   * @param d
   */
  void setValue(int i, int j, double d);

  /**
  * Returns the matrix as a double[][] array
  *
  * @return
  */
  double[][] asArray();

  /**
   * Answers a copy of the values in the i'th row
   * 
   * @return
   */
  double[] getRow(int i);

  /**
   * Answers a copy of the values in the i'th column
   * 
   * @return
   */
  double[] getColumn(int i);

  /**
   * Answers a new matrix with a copy of the values in this one
   * 
   * @return
   */
  MatrixI copy();

  /**
   * Returns a new matrix which is the transpose of this one
   * 
   * @return
   */
  MatrixI transpose();

  /**
   * Returns a new matrix which is the result of premultiplying this matrix by
   * the supplied argument. If this of size AxB (A rows and B columns), and the
   * argument is CxA (C rows and A columns), the result is of size CxB.
   * 
   * @param in
   * 
   * @return
   * @throws IllegalArgumentException
   *           if the number of columns in the pre-multiplier is not equal to
   *           the number of rows in the multiplicand (this)
   */
  MatrixI preMultiply(MatrixI m);

  /**
   * Returns a new matrix which is the result of postmultiplying this matrix by
   * the supplied argument. If this of size AxB (A rows and B columns), and the
   * argument is BxC (B rows and C columns), the result is of size AxC.
   * <p>
   * This method simply returns the result of in.preMultiply(this)
   * 
   * @param in
   * 
   * @return
   * @throws IllegalArgumentException
   *           if the number of rows in the post-multiplier is not equal to the
   *           number of columns in the multiplicand (this)
   * @see #preMultiply(Matrix)
   */
  MatrixI postMultiply(MatrixI m);

  double[] getD();

  double[] getE();

  void setD(double[] v);

  void setE(double[] v);

  void print(PrintStream ps, String format);

  void printD(PrintStream ps, String format);

  void printE(PrintStream ps, String format);

  void tqli() throws Exception;

  void tred();

  /**
   * Reverses the range of the matrix values, so that the smallest values become
   * the largest, and the largest become the smallest. This operation supports
   * using a distance measure as a similarity measure, or vice versa.
   * <p>
   * If parameter <code>maxToZero</code> is true, then the maximum value becomes
   * zero, i.e. all values are subtracted from the maximum. This is consistent
   * with converting an identity similarity score to a distance score - the most
   * similar (identity) corresponds to zero distance. However note that the
   * operation is not reversible (unless the original minimum value is zero).
   * For example a range of 10-40 would become 30-0, which would reverse a
   * second time to 0-30. Also note that a general similarity measure (such as
   * BLOSUM) may give different 'identity' scores for different sequences, so
   * they cannot all convert to zero distance.
   * <p>
   * If parameter <code>maxToZero</code> is false, then the values are reflected
   * about the average of {min, max} (effectively swapping min and max). This
   * operation <em>is</em> reversible.
   * 
   * @param maxToZero
   */
  void reverseRange(boolean maxToZero);

  /**
   * Multiply all entries by the given value
   * 
   * @param d
   */
  void multiply(double d);

  /**
  * Add d to all entries of this matrix
  *
  * @param d ~ value to add
  */
  void add(double d);

  /**
   * Answers true if the two matrices have the same dimensions, and
   * corresponding values all differ by no more than delta (which should be a
   * positive value), else false
   * 
   * @param m2
   * @param delta
   * @return
   */
  boolean equals(MatrixI m2, double delta);

  /**
   * Returns a copy in which  every value in the matrix is its absolute
   */
  MatrixI absolute();

  /**
   * Returns the mean of each row
   */
  double[] meanRow();

  /**
   * Returns the mean of each column
   */
  double[] meanCol();

  /**
  * Returns a flattened matrix containing the sum of each column
  *
  * @return
  */
  double[] sumCol();

  /**
  * returns the mean value of the complete matrix
  */
  double mean();

  /**
  * fills up a diagonal matrix with its transposed copy
  * !other side should be filled with either 0 or Double.NaN
  */
  void fillDiagonal();

  /**
  * counts the number of Double.NaN in the matrix
  *
  * @return
  */
  int countNaN();

  /**
  * performs an element-wise addition of this matrix by another matrix
  * !matrices have to be the same size
  * @param m ~ other matrix
  * 
  * @return
  * @throws IllegalArgumentException
  *           if this and m do not have the same dimensions
  */
  MatrixI add(MatrixI m);

  /**
  * performs an element-wise subtraction of this matrix by another matrix
  * !matrices have to be the same size
  * @param m ~ other matrix
  * 
  * @return
  * @throws IllegalArgumentException
  *           if this and m do not have the same dimensions
  */
  MatrixI subtract(MatrixI m);
 
  /**
  * performs an element-wise multiplication of this matrix by another matrix ~ this * m
  * !matrices have to be the same size
  * @param m ~ other matrix
  *
  * @return
  * @throws IllegalArgumentException
  *	if this and m do not have the same dimensions
  */
  MatrixI elementwiseMultiply(MatrixI m);

  /**
  * performs an element-wise division of this matrix by another matrix ~ this / m
  * !matrices have to be the same size
  * @param m ~ other matrix
  *
  * @return
  * @throws IllegalArgumentException
  *	if this and m do not have the same dimensions
  */
  MatrixI elementwiseDivide(MatrixI m);

  /**
  * calculates the root-mean-square for two matrices
  * @param m ~ other matrix
  *  
  * @return
  */
  double rmsd(MatrixI m);

  /**
  * calculates the Frobenius norm of this matrix
  *
  * @return
  */
  double norm();
  
  /**
  * returns the sum of all values in this matrix
  *
  * @return
  */
  double sum();

  /**
  * returns the sum-product of this matrix with vector v
  * @param v ~ vector
  *
  * @return
  * @throws IllegalArgumentException
  *	if this.cols and v do not have the same length
  */
  double[] sumProduct(double[] v);
}
