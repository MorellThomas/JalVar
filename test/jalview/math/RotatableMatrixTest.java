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

import jalview.math.RotatableMatrix.Axis;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RotatableMatrixTest
{
  private RotatableMatrix rm;

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    rm = new RotatableMatrix();

    /*
     * 0.5 1.0 1.5
     * 1.0 2.0 3.0
     * 1.5 3.0 4.5
     */
    for (int i = 1; i <= 3; i++)
    {
      for (int j = 1; j <= 3; j++)
      {
        rm.setValue(i - 1, j - 1, i * j / 2f);
      }
    }
  }

  @Test(groups = "Functional")
  public void testPrint()
  {
    String expected = "0.5 1.0 1.5\n1.0 2.0 3.0\n1.5 3.0 4.5\n";
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(os, true);
    rm.print(ps);
    String result = new String(os.toByteArray());
    assertEquals(result, expected);
  }

  @Test(groups = "Functional")
  public void testPreMultiply()
  {
    float[][] pre = new float[3][3];
    int i = 1;
    for (int j = 0; j < 3; j++)
    {
      for (int k = 0; k < 3; k++)
      {
        pre[j][k] = i++;
      }
    }

    rm.preMultiply(pre);

    /*
     * check rm[i, j] is now the product of the i'th row of pre
     * and the j'th column of (original) rm
     */
    for (int j = 0; j < 3; j++)
    {
      for (int k = 0; k < 3; k++)
      {
        float expected = 0f;
        for (int l = 0; l < 3; l++)
        {
          float rm_l_k = (l + 1) * (k + 1) / 2f;
          expected += pre[j][l] * rm_l_k;
        }
        assertEquals(rm.getValue(j, k), expected,
                String.format("[%d, %d]", j, k));
      }
    }
  }

  @Test(groups = "Functional")
  public void testVectorMultiply()
  {
    float[] result = rm.vectorMultiply(new float[] { 2f, 3f, 4.5f });

    // vector times first column of matrix
    assertEquals(result[0], 2f * 0.5f + 3f * 1f + 4.5f * 1.5f);

    // vector times second column of matrix
    assertEquals(result[1], 2f * 1.0f + 3f * 2f + 4.5f * 3f);

    // vector times third column of matrix
    assertEquals(result[2], 2f * 1.5f + 3f * 3f + 4.5f * 4.5f);
  }

  @Test(groups = "Functional")
  public void testGetRotation()
  {
    float theta = 60f;
    double cosTheta = Math.cos((theta * Math.PI / 180f));
    double sinTheta = Math.sin((theta * Math.PI / 180f));

    /*
     * sanity check that sin(60) = sqrt(3) / 2, cos(60) = 1/2
     */
    double delta = 0.0001d;
    assertEquals(cosTheta, 0.5f, delta);
    assertEquals(sinTheta, Math.sqrt(3d) / 2d, delta);

    /*
     * so far so good, now verify rotations
     * @see https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations
     */

    /*
     * 60 degrees about X axis should be
     *  1   0   0 
     *  0  cos -sin
     *  0  sin cos
     *  but code applies the negative of this
     *  nb cos(-x) = cos(x), sin(-x) = -sin(x)
     */
    float[][] rot = RotatableMatrix.getRotation(theta, Axis.X);
    assertEquals(rot[0][0], 1f, delta);
    assertEquals(rot[0][1], 0f, delta);
    assertEquals(rot[0][2], 0f, delta);
    assertEquals(rot[1][0], 0f, delta);
    assertEquals(rot[1][1], cosTheta, delta);
    assertEquals(rot[1][2], sinTheta, delta);
    assertEquals(rot[2][0], 0f, delta);
    assertEquals(rot[2][1], -sinTheta, delta);
    assertEquals(rot[2][2], cosTheta, delta);

    /*
     * 60 degrees about Y axis should be
     *   cos 0 sin
     *    0  1  0
     *  -sin 0 cos
     *  but code applies the negative of this
     */
    rot = RotatableMatrix.getRotation(theta, Axis.Y);
    assertEquals(rot[0][0], cosTheta, delta);
    assertEquals(rot[0][1], 0f, delta);
    assertEquals(rot[0][2], -sinTheta, delta);
    assertEquals(rot[1][0], 0f, delta);
    assertEquals(rot[1][1], 1f, delta);
    assertEquals(rot[1][2], 0f, delta);
    assertEquals(rot[2][0], sinTheta, delta);
    assertEquals(rot[2][1], 0f, delta);
    assertEquals(rot[2][2], cosTheta, delta);

    /*
     * 60 degrees about Z axis should be
     *  cos -sin 0
     *  sin  cos 0
     *   0    0  1
     * - and it is!
     */
    rot = RotatableMatrix.getRotation(theta, Axis.Z);
    assertEquals(rot[0][0], cosTheta, delta);
    assertEquals(rot[0][1], -sinTheta, delta);
    assertEquals(rot[0][2], 0f, delta);
    assertEquals(rot[1][0], sinTheta, delta);
    assertEquals(rot[1][1], cosTheta, delta);
    assertEquals(rot[1][2], 0f, delta);
    assertEquals(rot[2][0], 0f, delta);
    assertEquals(rot[2][1], 0f, delta);
    assertEquals(rot[2][2], 1f, delta);
  }
}
