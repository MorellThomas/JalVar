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
import jalview.util.MessageManager;

import java.io.PrintStream;
import java.lang.Math;
import java.util.Arrays;

/**
 * A class to model rectangular matrices of double values and operations on them
 */
public class Matrix implements MatrixI
{
  /*
   * maximum number of iterations for tqli
   */
  private static final int MAX_ITER = 45;
  // fudge - add 15 iterations, just in case

  /*
   * the number of rows
   */
  final protected int rows;

  /*
   * the number of columns
   */
  final protected int cols;

  /*
   * the cell values in row-major order
   */
  private double[][] value;

  protected double[] d; // Diagonal

  protected double[] e; // off diagonal

  /**
   * Constructor given number of rows and columns
   * 
   * @param colCount
   * @param rowCount
   */
  protected Matrix(int rowCount, int colCount)
  {
    rows = rowCount;
    cols = colCount;
  }

  /**
   * Creates a new Matrix object containing a copy of the supplied array values.
   * For example
   * 
   * <pre>
   *   new Matrix(new double[][] {{2, 3, 4}, {5, 6, 7})
   * constructs
   *   (2 3 4)
   *   (5 6 7)
   * </pre>
   * 
   * Note that ragged arrays (with not all rows, or columns, of the same
   * length), are not supported by this class. They can be constructed, but
   * results of operations on them are undefined and may throw exceptions.
   * 
   * @param values
   *          the matrix values in row-major order
   */
  public Matrix(double[][] values)
  {
    this.rows = values.length;
    this.cols = this.rows == 0 ? 0 : values[0].length;

    /*
     * make a copy of the values array, for immutability
     */
    this.value = new double[rows][];
    int i = 0;
    for (double[] row : values)
    {
      if (row != null)
      {
        value[i] = new double[row.length];
        System.arraycopy(row, 0, value[i], 0, row.length);
      }
      i++;
    }
  }

  public Matrix(float[][] values)
  {
    this.rows = values.length;
    this.cols = this.rows == 0 ? 0 : values[0].length;

    /*
     * make a copy of the values array, for immutability
     */
    this.value = new double[rows][];
    int i = 0;
    for (float[] row : values)
    {
      if (row != null)
      {
        value[i] = new double[row.length];
	int j = 0;
	for (float oldValue : row)
	{
	  value[i][j] = oldValue;
	  j++;
	}
      }
      i++;
    }
  }

  @Override
  public MatrixI transpose()
  {
    double[][] out = new double[cols][rows];

    for (int i = 0; i < cols; i++)
    {
      for (int j = 0; j < rows; j++)
      {
        out[i][j] = value[j][i];
      }
    }

    return new Matrix(out);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param ps
   *          DOCUMENT ME!
   * @param format
   */
  @Override
  public void print(PrintStream ps, String format)
  {
    for (int i = 0; i < rows; i++)
    {
      for (int j = 0; j < cols; j++)
      {
        Format.print(ps, format, getValue(i, j));
      }

      ps.println();
    }
  }

  @Override
  public MatrixI preMultiply(MatrixI in)
  {
    if (in.width() != rows)
    {
      throw new IllegalArgumentException("Can't pre-multiply " + this.rows
              + " rows by " + in.width() + " columns");
    }
    double[][] tmp = new double[in.height()][this.cols];

    for (int i = 0; i < in.height(); i++)
    {
      for (int j = 0; j < this.cols; j++)
      {
        /*
         * result[i][j] is the vector product of 
         * in.row[i] and this.column[j]
         */
        for (int k = 0; k < in.width(); k++)
        {
	  if (!Double.isNaN(in.getValue(i,k)) && !Double.isNaN(this.value[k][j]))
	  {
            tmp[i][j] += (in.getValue(i, k) * this.value[k][j]);
	  }
        }
      }
    }

    return new Matrix(tmp);
  }

  /**
   * 
   * @param in
   * 
   * @return
   */
  public double[] vectorPostMultiply(double[] in)
  {
    double[] out = new double[in.length];

    for (int i = 0; i < in.length; i++)
    {
      out[i] = 0.0;

      for (int k = 0; k < in.length; k++)
      {
        out[i] += (value[i][k] * in[k]);
      }
    }

    return out;
  }

  @Override
  public MatrixI postMultiply(MatrixI in)
  {
    if (in.height() != this.cols)
    {
      throw new IllegalArgumentException("Can't post-multiply " + this.cols
              + " columns by " + in.height() + " rows");
    }
    return in.preMultiply(this);
  }

  @Override
  public MatrixI copy()
  {
    double[][] newmat = new double[rows][cols];

    for (int i = 0; i < rows; i++)
    {
      System.arraycopy(value[i], 0, newmat[i], 0, value[i].length);
    }

    Matrix m = new Matrix(newmat);
    if (this.d != null)
    {
      m.d = Arrays.copyOf(this.d, this.d.length);
    }
    if (this.e != null)
    {
      m.e = Arrays.copyOf(this.e, this.e.length);
    }

    return m;
  }

  /**
   * DOCUMENT ME!
   */
  @Override
  public void tred()
  {
    int n = rows;
    int k;
    int j;
    int i;

    double scale;
    double hh;
    double h;
    double g;
    double f;

    this.d = new double[rows];
    this.e = new double[rows];

    for (i = n; i >= 2; i--)
    {
      final int l = i - 1;
      h = 0.0;
      scale = 0.0;

      if (l > 1)
      {
        for (k = 1; k <= l; k++)
        {
          double v = Math.abs(getValue(i - 1, k - 1));
          scale += v;
        }

        if (scale == 0.0)
        {
          e[i - 1] = getValue(i - 1, l - 1);
        }
        else
        {
          for (k = 1; k <= l; k++)
          {
            double v = divideValue(i - 1, k - 1, scale);
            h += v * v;
          }

          f = getValue(i - 1, l - 1);

          if (f > 0)
          {
            g = -1.0 * Math.sqrt(h);
          }
          else
          {
            g = Math.sqrt(h);
          }

          e[i - 1] = scale * g;
          h -= (f * g);
          setValue(i - 1, l - 1, f - g);
          f = 0.0;

          for (j = 1; j <= l; j++)
          {
            double val = getValue(i - 1, j - 1) / h;
            setValue(j - 1, i - 1, val);
            g = 0.0;

            for (k = 1; k <= j; k++)
            {
              g += (getValue(j - 1, k - 1) * getValue(i - 1, k - 1));
            }

            for (k = j + 1; k <= l; k++)
            {
              g += (getValue(k - 1, j - 1) * getValue(i - 1, k - 1));
            }

            e[j - 1] = g / h;
            f += (e[j - 1] * getValue(i - 1, j - 1));
          }

          hh = f / (h + h);

          for (j = 1; j <= l; j++)
          {
            f = getValue(i - 1, j - 1);
            g = e[j - 1] - (hh * f);
            e[j - 1] = g;

            for (k = 1; k <= j; k++)
            {
              double val = (f * e[k - 1]) + (g * getValue(i - 1, k - 1));
              addValue(j - 1, k - 1, -val);
            }
          }
        }
      }
      else
      {
        e[i - 1] = getValue(i - 1, l - 1);
      }

      d[i - 1] = h;
    }

    d[0] = 0.0;
    e[0] = 0.0;

    for (i = 1; i <= n; i++)
    {
      final int l = i - 1;

      if (d[i - 1] != 0.0)
      {
        for (j = 1; j <= l; j++)
        {
          g = 0.0;

          for (k = 1; k <= l; k++)
          {
            g += (getValue(i - 1, k - 1) * getValue(k - 1, j - 1));
          }

          for (k = 1; k <= l; k++)
          {
            addValue(k - 1, j - 1, -(g * getValue(k - 1, i - 1)));
          }
        }
      }

      d[i - 1] = getValue(i - 1, i - 1);
      setValue(i - 1, i - 1, 1.0);

      for (j = 1; j <= l; j++)
      {
        setValue(j - 1, i - 1, 0.0);
        setValue(i - 1, j - 1, 0.0);
      }
    }
  }

  /**
   * Adds f to the value at [i, j] and returns the new value
   * 
   * @param i
   * @param j
   * @param f
   */
  protected double addValue(int i, int j, double f)
  {
    double v = value[i][j] + f;
    value[i][j] = v;
    return v;
  }

  /**
   * Divides the value at [i, j] by divisor and returns the new value. If d is
   * zero, returns the unchanged value.
   * 
   * @param i
   * @param j
   * @param divisor
   * @return
   */
  protected double divideValue(int i, int j, double divisor)
  {
    if (divisor == 0d)
    {
      return getValue(i, j);
    }
    double v = value[i][j];
    v = v / divisor;
    value[i][j] = v;
    return v;
  }

  /**
   * DOCUMENT ME!
   */
  @Override
  public void tqli() throws Exception
  {
    int n = rows;

    int m;
    int l;
    int iter;
    int i;
    int k;
    double s;
    double r;
    double p;

    double g;
    double f;
    double dd;
    double c;
    double b;

    for (i = 2; i <= n; i++)
    {
      e[i - 2] = e[i - 1];
    }

    e[n - 1] = 0.0;

    for (l = 1; l <= n; l++)
    {
      iter = 0;

      do
      {
        for (m = l; m <= (n - 1); m++)
        {
          dd = Math.abs(d[m - 1]) + Math.abs(d[m]);

          if ((Math.abs(e[m - 1]) + dd) == dd)
          {
            break;
          }
        }

        if (m != l)
        {
          iter++;

          if (iter == MAX_ITER)
          {
            throw new Exception(MessageManager.formatMessage(
                    "exception.matrix_too_many_iteration", new String[]
                    { "tqli", Integer.valueOf(MAX_ITER).toString() }));
          }
          else
          {
            // System.out.println("Iteration " + iter);
          }

          g = (d[l] - d[l - 1]) / (2.0 * e[l - 1]);
          r = Math.sqrt((g * g) + 1.0);
          g = d[m - 1] - d[l - 1] + (e[l - 1] / (g + sign(r, g)));
          c = 1.0;
          s = c;
          p = 0.0;

          for (i = m - 1; i >= l; i--)
          {
            f = s * e[i - 1];
            b = c * e[i - 1];

            if (Math.abs(f) >= Math.abs(g))
            {
              c = g / f;
              r = Math.sqrt((c * c) + 1.0);
              e[i] = f * r;
              s = 1.0 / r;
              c *= s;
            }
            else
            {
              s = f / g;
              r = Math.sqrt((s * s) + 1.0);
              e[i] = g * r;
              c = 1.0 / r;
              s *= c;
            }

            g = d[i] - p;
            r = ((d[i - 1] - g) * s) + (2.0 * c * b);
            p = s * r;
            d[i] = g + p;
            g = (c * r) - b;

            for (k = 1; k <= n; k++)
            {
              f = getValue(k - 1, i);
              setValue(k - 1, i, (s * getValue(k - 1, i - 1)) + (c * f));
              setValue(k - 1, i - 1,
                      (c * getValue(k - 1, i - 1)) - (s * f));
            }
          }

          d[l - 1] = d[l - 1] - p;
          e[l - 1] = g;
          e[m - 1] = 0.0;
        }
      } while (m != l);
    }
  }

  @Override
  public double getValue(int i, int j)
  {
    return value[i][j];
  }

  @Override
  public void setValue(int i, int j, double val)
  {
    value[i][j] = val;
  }

  /**
   * DOCUMENT ME!
   */
  public void tred2()
  {
    int n = rows;
    int l;
    int k;
    int j;
    int i;

    double scale;
    double hh;
    double h;
    double g;
    double f;

    this.d = new double[rows];
    this.e = new double[rows];

    for (i = n - 1; i >= 1; i--)
    {
      l = i - 1;
      h = 0.0;
      scale = 0.0;

      if (l > 0)
      {
        for (k = 0; k < l; k++)
        {
          scale += Math.abs(value[i][k]);
        }

        if (scale == 0.0)
        {
          e[i] = value[i][l];
        }
        else
        {
          for (k = 0; k < l; k++)
          {
            value[i][k] /= scale;
            h += (value[i][k] * value[i][k]);
          }

          f = value[i][l];

          if (f > 0)
          {
            g = -1.0 * Math.sqrt(h);
          }
          else
          {
            g = Math.sqrt(h);
          }

          e[i] = scale * g;
          h -= (f * g);
          value[i][l] = f - g;
          f = 0.0;

          for (j = 0; j < l; j++)
          {
            value[j][i] = value[i][j] / h;
            g = 0.0;

            for (k = 0; k < j; k++)
            {
              g += (value[j][k] * value[i][k]);
            }

            for (k = j; k < l; k++)
            {
              g += (value[k][j] * value[i][k]);
            }

            e[j] = g / h;
            f += (e[j] * value[i][j]);
          }

          hh = f / (h + h);

          for (j = 0; j < l; j++)
          {
            f = value[i][j];
            g = e[j] - (hh * f);
            e[j] = g;

            for (k = 0; k < j; k++)
            {
              value[j][k] -= ((f * e[k]) + (g * value[i][k]));
            }
          }
        }
      }
      else
      {
        e[i] = value[i][l];
      }

      d[i] = h;
    }

    d[0] = 0.0;
    e[0] = 0.0;

    for (i = 0; i < n; i++)
    {
      l = i - 1;

      if (d[i] != 0.0)
      {
        for (j = 0; j < l; j++)
        {
          g = 0.0;

          for (k = 0; k < l; k++)
          {
            g += (value[i][k] * value[k][j]);
          }

          for (k = 0; k < l; k++)
          {
            value[k][j] -= (g * value[k][i]);
          }
        }
      }

      d[i] = value[i][i];
      value[i][i] = 1.0;

      for (j = 0; j < l; j++)
      {
        value[j][i] = 0.0;
        value[i][j] = 0.0;
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void tqli2() throws Exception
  {
    int n = rows;

    int m;
    int l;
    int iter;
    int i;
    int k;
    double s;
    double r;
    double p;
    ;

    double g;
    double f;
    double dd;
    double c;
    double b;

    for (i = 2; i <= n; i++)
    {
      e[i - 2] = e[i - 1];
    }

    e[n - 1] = 0.0;

    for (l = 1; l <= n; l++)
    {
      iter = 0;

      do
      {
        for (m = l; m <= (n - 1); m++)
        {
          dd = Math.abs(d[m - 1]) + Math.abs(d[m]);

          if ((Math.abs(e[m - 1]) + dd) == dd)
          {
            break;
          }
        }

        if (m != l)
        {
          iter++;

          if (iter == MAX_ITER)
          {
            throw new Exception(MessageManager.formatMessage(
                    "exception.matrix_too_many_iteration", new String[]
                    { "tqli2", Integer.valueOf(MAX_ITER).toString() }));
          }
          else
          {
            // System.out.println("Iteration " + iter);
          }

          g = (d[l] - d[l - 1]) / (2.0 * e[l - 1]);
          r = Math.sqrt((g * g) + 1.0);
          g = d[m - 1] - d[l - 1] + (e[l - 1] / (g + sign(r, g)));
          c = 1.0;
          s = c;
          p = 0.0;

          for (i = m - 1; i >= l; i--)
          {
            f = s * e[i - 1];
            b = c * e[i - 1];

            if (Math.abs(f) >= Math.abs(g))
            {
              c = g / f;
              r = Math.sqrt((c * c) + 1.0);
              e[i] = f * r;
              s = 1.0 / r;
              c *= s;
            }
            else
            {
              s = f / g;
              r = Math.sqrt((s * s) + 1.0);
              e[i] = g * r;
              c = 1.0 / r;
              s *= c;
            }

            g = d[i] - p;
            r = ((d[i - 1] - g) * s) + (2.0 * c * b);
            p = s * r;
            d[i] = g + p;
            g = (c * r) - b;

            for (k = 1; k <= n; k++)
            {
              f = value[k - 1][i];
              value[k - 1][i] = (s * value[k - 1][i - 1]) + (c * f);
              value[k - 1][i - 1] = (c * value[k - 1][i - 1]) - (s * f);
            }
          }

          d[l - 1] = d[l - 1] - p;
          e[l - 1] = g;
          e[m - 1] = 0.0;
        }
      } while (m != l);
    }
  }

  /**
   * Answers the first argument with the sign of the second argument
   * 
   * @param a
   * @param b
   * 
   * @return
   */
  static double sign(double a, double b)
  {
    if (b < 0)
    {
      return -Math.abs(a);
    }
    else
    {
      return Math.abs(a);
    }
  }

  /**
  * returns the matrix as a double[][] array
  *
  * @return
  */
  @Override
  public double[][] asArray()
  {
    return value;
  }

  /**
   * Returns an array containing the values in the specified column
   * 
   * @param col
   * 
   * @return
   */
  @Override
  public double[] getColumn(int col)
  {
    double[] out = new double[rows];

    for (int i = 0; i < rows; i++)
    {
      out[i] = value[i][col];
    }

    return out;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param ps
   *          DOCUMENT ME!
   * @param format
   */
  @Override
  public void printD(PrintStream ps, String format)
  {
    for (int j = 0; j < d.length; j++)
    {
      Format.print(ps, format, d[j]);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param ps
   *          DOCUMENT ME!
   * @param format
   *          TODO
   */
  @Override
  public void printE(PrintStream ps, String format)
  {
    for (int j = 0; j < rows; j++)
    {
      Format.print(ps, format, e[j]);
    }
  }

  @Override
  public double[] getD()
  {
    return d;
  }

  @Override
  public double[] getE()
  {
    return e;
  }

  @Override
  public int height()
  {
    return rows;
  }

  @Override
  public int width()
  {
    return cols;
  }

  @Override
  public double[] getRow(int i)
  {
    double[] row = new double[cols];
    System.arraycopy(value[i], 0, row, 0, cols);
    return row;
  }

  /**
   * Returns a length 2 array of {minValue, maxValue} of all values in the
   * matrix. Returns null if the matrix is null or empty.
   * 
   * @return
   */
  double[] findMinMax()
  {
    if (value == null)
    {
      return null;
    }
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    boolean empty = true;
    for (double[] row : value)
    {
      if (row != null)
      {
        for (double x : row)
        {
          empty = false;
          if (x > max)
          {
            max = x;
          }
          if (x < min)
          {
            min = x;
          }
        }
      }
    }
    return empty ? null : new double[] { min, max };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reverseRange(boolean maxToZero)
  {
    if (value == null)
    {
      return;
    }
    double[] minMax = findMinMax();
    if (minMax == null)
    {
      return; // empty matrix
    }
    double subtractFrom = maxToZero ? minMax[1] : minMax[0] + minMax[1];

    for (double[] row : value)
    {
      if (row != null)
      {
        int j = 0;
        for (double x : row)
        {
          row[j] = subtractFrom - x;
          j++;
        }
      }
    }
  }

  /**
   * Multiplies every entry in the matrix by the given value.
   * 
   * @param
   */
  @Override
  public void multiply(double by)
  {
    for (double[] row : value)
    {
      if (row != null)
      {
        for (int i = 0; i < row.length; i++)
        {
          row[i] *= by;
        }
      }
    }
  }

  /**
   * Add d to all entries of this matrix
   * 
   * @param d ~ value to add
   */
  @Override
  public void add(double d)
  {
    for (double[] row : value)
    {
      if (row != null)
      {
        for (int i = 0; i < row.length; i++)
        {
          row[i] += d;
        }
      }
    }
  }

  @Override
  public void setD(double[] v)
  {
    d = v;
  }

  @Override
  public void setE(double[] v)
  {
    e = v;
  }

  public double getTotal()
  {
    double d = 0d;
    for (int i = 0; i < this.height(); i++)
    {
      for (int j = 0; j < this.width(); j++)
      {
        d += value[i][j];
      }
    }
    return d;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(MatrixI m2, double delta)
  {
    if (m2 == null || this.height() != m2.height()
            || this.width() != m2.width())
    {
      return false;
    }
    for (int i = 0; i < this.height(); i++)
    {
      for (int j = 0; j < this.width(); j++)
      {
        double diff = this.getValue(i, j) - m2.getValue(i, j);
        if (Math.abs(diff) > delta)
        {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns a copy in which  every value in the matrix is its absolute
   * 
   * @return
   */
  @Override
  public MatrixI absolute()
  {
    MatrixI copy = this.copy();
    for (int i = 0; i < copy.width(); i++)
    {
      double[] row = copy.getRow(i);
      if (row != null)
      {
        for (int j = 0; j < row.length; j++)
        {
          row[j] = Math.abs(row[j]);
        }
      }
    }
    return copy;
  }

  /**
   * Returns the mean of each row
   * 
   * @return
   */
  @Override
  public double[] meanRow()
  {
    double[] mean = new double[rows];
    int i = 0;
    for (double[] row : value)
    {
      if (row != null)
      {
	mean[i++] = MiscMath.mean(row);
      }
    }
    return mean;
  }

  /**
   * Returns the mean of each column
   * 
   * @return
   */
  @Override
  public double[] meanCol()
  {
    double[] mean = new double[cols];
    for (int j = 0; j < cols; j++)
    {
      double[] column = getColumn(j);
      if (column != null)
      {
	mean[j] = MiscMath.mean(column);
      }
    }
    return mean;
  }

  /**
  * return a flattened matrix containing the sum of each column
  *
  * @return
  */
  @Override
  public double[] sumCol()
  {
    double[] sum = new double[cols];
    for (int j = 0; j < cols; j++)
    {
      double[] column = getColumn(j);
      if (column != null)
      {
	sum[j] = MiscMath.sum(column);
      }
    } 
    return sum;
  }

  /**
  * returns the mean value of the complete matrix
  *
  * @return
  */
  @Override
  public double mean()
  {
    double sum = 0;
    int nanCount = 0;
    for (double[] row : value)
    {
      for (double col : row)
      {
	if (!Double.isNaN(col))
	{
	  sum += col;
	} else {
	  nanCount++;
	}
      }
    }
    return sum / (double) (this.rows * this.cols - nanCount);
  }

  /**
  * fills up a diagonal matrix with its transposed copy
  * !other side should be filled with 0
  * !keeps Double.NaN found in either side
  *
  * TODO check on which side it was diagonal and only do calculations for the other side
  */
  @Override
  public void fillDiagonal()
  {
    int n = this.rows;
    int m = this.cols;
    MatrixI copy = this.transpose();	// goes through each element in the matrix and
    for (int i = 0; i < n; i++)		// adds the value in the transposed copy to the original value
    {
      for (int j = 0; j < m; j++)
      {
	if (i != j)
	{
	  this.addValue(i, j, copy.getValue(i,j));
	}
      }
    }
  }

  /**
  * counts the number of Double.NaN in the matrix
  *
  * @return
  */
  @Override
  public int countNaN()
  {
    int NaN = 0;
    for (int i = 0; i < this.rows; i++)
    {
      for (int j = 0; j < this.cols; j++)
      {
	if (Double.isNaN(this.getValue(i,j)))
 	{
	  NaN++;
	}
      }
    }
    return NaN;
  }

  /**
  * performs an element-wise addition of this matrix by another matrix ~ this - m
  * @param m ~ other matrix
  *
  * @return
  */
  @Override
  public MatrixI add(MatrixI m)
  {
    if (m.width() != cols || m.height() != rows)
    {
      throw new IllegalArgumentException("Can't add a " + m.height() + "x" + m.width() + " to a " + this.rows + "x" + this.cols + " matrix");
    }
    double[][] tmp = new double[this.rows][this.cols];
    for (int i = 0; i < this.rows; i++)
    {
      for (int j = 0; j < this.cols; j++)
      {
	tmp[i][j] = this.getValue(i,j) + m.getValue(i,j);
      }
    }
    return new Matrix(tmp);
  }

  /**
  * performs an element-wise subtraction of this matrix by another matrix ~ this - m
  * @param m ~ other matrix
  *
  * @return
  */
  @Override
  public MatrixI subtract(MatrixI m)
  {
    if (m.width() != cols || m.height() != rows)
    {
      throw new IllegalArgumentException("Can't subtract a " + m.height() + "x" + m.width() + " from a " + this.rows + "x" + this.cols + " matrix");
    }
    double[][] tmp = new double[this.rows][this.cols];
    for (int i = 0; i < this.rows; i++)
    {
      for (int j = 0; j < this.cols; j++)
      {
	tmp[i][j] = this.getValue(i,j) -  m.getValue(i,j);
      }
    }
    return new Matrix(tmp);
  }

  /**
  * performs an element-wise multiplication of this matrix by another matrix ~ this * m
  * @param m ~ other matrix
  *
  * @return
  */
  @Override
  public MatrixI elementwiseMultiply(MatrixI m)
  {
    if (m.width() != cols || m.height() != rows)
    {
      throw new IllegalArgumentException("Can't multiply a " + this.rows + "x" + this.cols + " by a " + m.height() + "x" + m.width() + " matrix");
    }
    double[][] tmp = new double[this.rows][this.cols];
    for (int i = 0; i < this.rows; i++)
    {
      for (int j = 0; j < this.cols; j++)
      {
        tmp[i][j] = this.getValue(i, j) * m.getValue(i,j);
      }
    }
    return new Matrix(tmp);
  }

  /**
  * performs an element-wise division of this matrix by another matrix ~ this / m
  * @param m ~ other matrix
  *
  * @return
  */
  @Override
  public MatrixI elementwiseDivide(MatrixI m)
  {
    if (m.width() != cols || m.height() != rows)
    {
      throw new IllegalArgumentException("Can't divide a " + this.rows + "x" + this.cols + " by a " + m.height() + "x" + m.width() + " matrix");
    }
    double[][] tmp = new double[this.rows][this.cols];
    for (int i = 0; i < this.rows; i++)
    {
      for (int j = 0; j < this.cols; j++)
      {
        tmp[i][j] = this.getValue(i, j) / m.getValue(i,j);
      }
    }
    return new Matrix(tmp);
  }

  /**
  * calculate the root-mean-square for tow matrices
  * @param m ~ other matrix
  *
  * @return
  */
  @Override
  public double rmsd(MatrixI m)
  {
    MatrixI squaredDeviates = this.subtract(m);
    squaredDeviates = squaredDeviates.preMultiply(squaredDeviates);
    return Math.sqrt(squaredDeviates.mean());
  }

  /**
  * calculates the Frobenius norm of this matrix
  *
  * @return
  */
  @Override
  public double norm()
  {
    double result = 0;
    for (double[] row : value)
    {
      for (double val : row)
      {
	result += Math.pow(val, 2);
      }
    }
    return Math.sqrt(result);
  }

  /**
  * returns the sum of all values in this matrix
  *
  * @return
  */
  @Override
  public double sum()
  {
    double sum = 0;
    for (double[] row : value)
    {
      for (double val : row)
      {
	sum += (Double.isNaN(val)) ? 0.0 : val;
      }
    }
    return sum;
  }

  /**
  * returns the sum-product of this matrix with vector v
  * @param v ~ vector
  *
  * @return
  */
  @Override
  public double[] sumProduct(double[] v)
  {
    if (v.length != cols)
    {
      throw new IllegalArgumentException("Vector and matrix do not have the same dimension! (" + v.length + " != " + cols + ")");
    }
    double[] result = new double[rows];
    for (int i = 0; i < rows; i++)
    {
      double[] row = value[i];
      double sum = 0;
      for (int j = 0; j < row.length; j++)
      {
	sum += row[j] * v[j];
      }
      result[i] = sum;
    }
    return result;
  }

}
