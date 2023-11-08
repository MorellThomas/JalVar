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

import jalview.ext.android.SparseDoubleArray;

/**
 * A variant of Matrix intended for use for sparse (mostly zero) matrices. This
 * class uses a SparseDoubleArray to hold each row of the matrix. The sparse
 * array only stores non-zero values. This gives a smaller memory footprint, and
 * fewer matrix calculation operations, for mostly zero matrices.
 * 
 * @author gmcarstairs
 */
public class SparseMatrix extends Matrix
{
  /*
   * we choose columns for the sparse arrays as this allows
   * optimisation of the preMultiply() method used in PCA.run()
   */
  SparseDoubleArray[] sparseColumns;

  /**
   * Constructor given data in [row][column] order
   * 
   * @param v
   */
  public SparseMatrix(double[][] v)
  {
    super(v.length, v.length > 0 ? v[0].length : 0);

    sparseColumns = new SparseDoubleArray[cols];

    /*
     * transpose v[row][col] into [col][row] order
     */
    for (int col = 0; col < cols; col++)
    {
      SparseDoubleArray sparseColumn = new SparseDoubleArray();
      sparseColumns[col] = sparseColumn;
      for (int row = 0; row < rows; row++)
      {
        double value = v[row][col];
        if (value != 0d)
        {
          sparseColumn.put(row, value);
        }
      }
    }
  }

  /**
   * Answers the value at row i, column j
   */
  @Override
  public double getValue(int i, int j)
  {
    return sparseColumns[j].get(i);
  }

  /**
   * Sets the value at row i, column j to val
   */
  @Override
  public void setValue(int i, int j, double val)
  {
    if (val == 0d)
    {
      sparseColumns[j].delete(i);
    }
    else
    {
      sparseColumns[j].put(i, val);
    }
  }

  @Override
  public double[] getColumn(int i)
  {
    double[] col = new double[height()];

    SparseDoubleArray vals = sparseColumns[i];
    for (int nonZero = 0; nonZero < vals.size(); nonZero++)
    {
      col[vals.keyAt(nonZero)] = vals.valueAt(nonZero);
    }
    return col;
  }

  @Override
  public MatrixI copy()
  {
    double[][] vals = new double[height()][width()];
    for (int i = 0; i < height(); i++)
    {
      vals[i] = getRow(i);
    }
    return new SparseMatrix(vals);
  }

  @Override
  public MatrixI transpose()
  {
    double[][] out = new double[cols][rows];

    /*
     * for each column...
     */
    for (int i = 0; i < cols; i++)
    {
      /*
       * put non-zero values into the corresponding row
       * of the transposed matrix
       */
      SparseDoubleArray vals = sparseColumns[i];
      for (int nonZero = 0; nonZero < vals.size(); nonZero++)
      {
        out[i][vals.keyAt(nonZero)] = vals.valueAt(nonZero);
      }
    }

    return new SparseMatrix(out);
  }

  /**
   * Answers a new matrix which is the product in.this. If the product contains
   * less than 20% non-zero values, it is returned as a SparseMatrix, else as a
   * Matrix.
   * <p>
   * This method is optimised for the sparse arrays which store column values
   * for a SparseMatrix. Note that postMultiply is not so optimised. That would
   * require redundantly also storing sparse arrays for the rows, which has not
   * been done. Currently only preMultiply is used in Jalview.
   */
  @Override
  public MatrixI preMultiply(MatrixI in)
  {
    if (in.width() != rows)
    {
      throw new IllegalArgumentException("Can't pre-multiply " + this.rows
              + " rows by " + in.width() + " columns");
    }
    double[][] tmp = new double[in.height()][this.cols];

    long count = 0L;
    for (int i = 0; i < in.height(); i++)
    {
      for (int j = 0; j < this.cols; j++)
      {
        /*
         * result[i][j] is the vector product of 
         * in.row[i] and this.column[j]
         * we only need to use non-zero values from the column
         */
        SparseDoubleArray vals = sparseColumns[j];
        boolean added = false;
        for (int nonZero = 0; nonZero < vals.size(); nonZero++)
        {
          int myRow = vals.keyAt(nonZero);
          double myValue = vals.valueAt(nonZero);
          tmp[i][j] += (in.getValue(i, myRow) * myValue);
          added = true;
        }
        if (added && tmp[i][j] != 0d)
        {
          count++; // non-zero entry in product
        }
      }
    }

    /*
     * heuristic rule - if product is more than 80% zero
     * then construct a SparseMatrix, else a Matrix
     */
    if (count * 5 < in.height() * cols)
    {
      return new SparseMatrix(tmp);
    }
    else
    {
      return new Matrix(tmp);
    }
  }

  @Override
  protected double divideValue(int i, int j, double divisor)
  {
    if (divisor == 0d)
    {
      return getValue(i, j);
    }
    double v = sparseColumns[j].divide(i, divisor);
    return v;
  }

  @Override
  protected double addValue(int i, int j, double addend)
  {
    double v = sparseColumns[j].add(i, addend);
    return v;
  }

  /**
   * Returns the fraction of the whole matrix size that is actually modelled in
   * sparse arrays (normally, the non-zero values)
   * 
   * @return
   */
  public float getFillRatio()
  {
    long count = 0L;
    for (SparseDoubleArray col : sparseColumns)
    {
      count += col.size();
    }
    return count / (float) (height() * width());
  }
}
