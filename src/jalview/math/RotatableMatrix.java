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

import jalview.datamodel.Point;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Model for a 3x3 matrix which provides methods for rotation in 3-D space
 */
public class RotatableMatrix
{
  private static final int DIMS = 3;

  /*
   * cache the most used rotations: +/- 1, 2, 3, 4 degrees around x or y axis
   */
  private static Map<Axis, Map<Float, float[][]>> cachedRotations;

  static
  {
    cachedRotations = new HashMap<>();
    for (Axis axis : Axis.values())
    {
      HashMap<Float, float[][]> map = new HashMap<>();
      cachedRotations.put(axis, map);
      for (int deg = 1; deg < 5; deg++)
      {
        float[][] rotation = getRotation(deg, axis);
        map.put(Float.valueOf(deg), rotation);
        rotation = getRotation(-deg, axis);
        map.put(Float.valueOf(-deg), rotation);
      }
    }
  }

  public enum Axis
  {
    X, Y, Z
  }

  float[][] matrix;

  /**
   * Constructor creates a new identity matrix (all values zero except for 1 on
   * the diagonal)
   */
  public RotatableMatrix()
  {
    matrix = new float[DIMS][DIMS];
    for (int j = 0; j < DIMS; j++)
    {
      matrix[j][j] = 1f;
    }
  }

  /**
   * Sets the value at position (i, j) of the matrix
   * 
   * @param i
   * @param j
   * @param value
   */
  public void setValue(int i, int j, float value)
  {
    matrix[i][j] = value;
  }

  /**
   * Answers the value at position (i, j) of the matrix
   * 
   * @param i
   * @param j
   * @return
   */
  public float getValue(int i, int j)
  {
    return matrix[i][j];
  }

  /**
   * Prints the matrix in rows of space-delimited values
   */
  public void print(PrintStream ps)
  {
    ps.println(matrix[0][0] + " " + matrix[0][1] + " " + matrix[0][2]);
    ps.println(matrix[1][0] + " " + matrix[1][1] + " " + matrix[1][2]);
    ps.println(matrix[2][0] + " " + matrix[2][1] + " " + matrix[2][2]);
  }

  /**
   * Rotates the matrix through the specified number of degrees around the
   * specified axis
   * 
   * @param degrees
   * @param axis
   */
  public void rotate(float degrees, Axis axis)
  {
    float[][] rot = getRotation(degrees, axis);

    preMultiply(rot);
  }

  /**
   * Answers a matrix which, when it pre-multiplies another matrix, applies a
   * rotation of the specified number of degrees around the specified axis
   * 
   * @param degrees
   * @param axis
   * @return
   * @see https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations
   */
  protected static float[][] getRotation(float degrees, Axis axis)
  {
    Float floatValue = Float.valueOf(degrees);
    if (cachedRotations.get(axis).containsKey(floatValue))
    {
      // System.out.println("getRotation from cache: " + (int) degrees);
      return cachedRotations.get(axis).get(floatValue);
    }

    float costheta = (float) Math.cos(degrees * Math.PI / 180f);

    float sintheta = (float) Math.sin(degrees * Math.PI / 180f);

    float[][] rot = new float[DIMS][DIMS];

    switch (axis)
    {
    case X:
      rot[0][0] = 1f;
      rot[1][1] = costheta;
      rot[1][2] = sintheta;
      rot[2][1] = -sintheta;
      rot[2][2] = costheta;
      break;
    case Y:
      rot[0][0] = costheta;
      rot[0][2] = -sintheta;
      rot[1][1] = 1f;
      rot[2][0] = sintheta;
      rot[2][2] = costheta;
      break;
    case Z:
      rot[0][0] = costheta;
      rot[0][1] = -sintheta;
      rot[1][0] = sintheta;
      rot[1][1] = costheta;
      rot[2][2] = 1f;
      break;
    }
    return rot;
  }

  /**
   * Answers a new array of float values which is the result of pre-multiplying
   * this matrix by the given vector. Each value of the result is the dot
   * product of the vector with one column of this matrix. The matrix and input
   * vector are not modified.
   * 
   * @param vect
   * 
   * @return
   */
  public float[] vectorMultiply(float[] vect)
  {
    float[] result = new float[DIMS];

    for (int i = 0; i < DIMS; i++)
    {
      result[i] = (matrix[i][0] * vect[0]) + (matrix[i][1] * vect[1])
              + (matrix[i][2] * vect[2]);
    }

    return result;
  }

  /**
   * Performs pre-multiplication of this matrix by the given one. Value (i, j)
   * of the result is the dot product of the i'th row of <code>mat</code> with
   * the j'th column of this matrix.
   * 
   * @param mat
   */
  public void preMultiply(float[][] mat)
  {
    float[][] tmp = new float[DIMS][DIMS];

    for (int i = 0; i < DIMS; i++)
    {
      for (int j = 0; j < DIMS; j++)
      {
        tmp[i][j] = (mat[i][0] * matrix[0][j]) + (mat[i][1] * matrix[1][j])
                + (mat[i][2] * matrix[2][j]);
      }
    }

    matrix = tmp;
  }

  /**
   * Performs post-multiplication of this matrix by the given one. Value (i, j)
   * of the result is the dot product of the i'th row of this matrix with the
   * j'th column of <code>mat</code>.
   * 
   * @param mat
   */
  public void postMultiply(float[][] mat)
  {
    float[][] tmp = new float[DIMS][DIMS];

    for (int i = 0; i < DIMS; i++)
    {
      for (int j = 0; j < DIMS; j++)
      {
        tmp[i][j] = (matrix[i][0] * mat[0][j]) + (matrix[i][1] * mat[1][j])
                + (matrix[i][2] * mat[2][j]);
      }
    }

    matrix = tmp;
  }

  /**
   * Performs a vector multiplication whose result is the Point representing the
   * input point's value vector post-multiplied by this matrix.
   * 
   * @param coord
   * @return
   */
  public Point vectorMultiply(Point coord)
  {
    float[] v = vectorMultiply(new float[] { coord.x, coord.y, coord.z });
    return new Point(v[0], v[1], v[2]);
  }
}
