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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FormatTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testAppendPercentage()
  {
    StringBuilder sb = new StringBuilder();
    Format.appendPercentage(sb, 123.436f, 0);
    assertEquals(sb.toString(), "123");

    sb.setLength(0);
    Format.appendPercentage(sb, 123.536f, 0);
    assertEquals(sb.toString(), "124");

    sb.setLength(0);
    Format.appendPercentage(sb, 799.536f, 0);
    assertEquals(sb.toString(), "800");

    sb.setLength(0);
    Format.appendPercentage(sb, 123.436f, 1);
    assertEquals(sb.toString(), "123.4");

    sb.setLength(0);
    Format.appendPercentage(sb, 123.436f, 2);
    assertEquals(sb.toString(), "123.44");

    sb.setLength(0);
    Format.appendPercentage(sb, 123.436f, 3);
    assertEquals(sb.toString(), "123.436");

    sb.setLength(0);
    Format.appendPercentage(sb, 123.436f, 4);
    assertEquals(sb.toString(), "123.4360");
  }

  @Test(groups = "Functional")
  public void testForm_float()
  {
    Format f = new Format("%3.2f");
    assertEquals(f.form(123f), "123.00");
    assertEquals(f.form(123.1f), "123.10");
    assertEquals(f.form(123.12f), "123.12");
    assertEquals(f.form(123.124f), "123.12");
    assertEquals(f.form(123.125f), "123.13");
    assertEquals(f.form(123.126f), "123.13");

    f = new Format("%3.0f");
    assertEquals(f.form(123f), "123.");
    assertEquals(f.form(12f), "12.");
    assertEquals(f.form(123.4f), "123.");
    assertEquals(f.form(123.5f), "124.");
    assertEquals(f.form(123.6f), "124.");
    assertEquals(f.form(129.6f), "130.");
  }

  @Test(groups = "Functional")
  public void testRepeat()
  {
    assertEquals(Format.repeat('a', 3), "aaa");
    assertEquals(Format.repeat('b', 0), "");
    assertEquals(Format.repeat('c', -1), "");
  }

  @Test(groups = "Functional")
  public void testFormat_scientific()
  {
    Format f = new Format("%3.4e");
    double d = 1d;
    assertEquals(f.form(d), "1.0000e+000");
    assertEquals(String.format("%3.4e", d), "1.0000e+00");

    d = 12345678.12345678d;
    assertEquals(f.form(d), "1.2346e+007");
    assertEquals(String.format("%3.4e", d), "1.2346e+07");
  }

  /**
   * Test that fails (in 2.10.1) with timeout as there is an infinite loop in
   * Format.exp_format()
   */
  @Test(groups = "Functional", timeOut = 500)
  public void testFormat_scientific_overflow()
  {
    Format f = new Format("%3.4e");
    double d = 1.12E-310;
    /*
     * problem: exp_format() scales up 'd' to the range 1-10
     * while computing a scaling factor, but this factor acquires
     * the value Double.POSITIVE_INFINITY instead of 1.0E+309
     * the value to be formatted is multipled by factor and becomes Infinity
     * resulting in an infinite loop in the recursive call to exp_format()
     */
    assertEquals(f.form(d), "1.1200e-310");
  }

  /**
   * This test shows that Format.form() is faster for this case than
   * String.format()
   */
  @Test(groups = "Timing")
  public void testFormat_scientificTiming()
  {
    Format f = new Format("%3.4e");
    double d = 12345678.12345678d;

    int iterations = 1000;
    long start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++)
    {
      f.form(d);
    }
    long stop = System.currentTimeMillis();
    long elapsed1 = stop - start;
    System.out
            .println(iterations + " x Format.form took " + elapsed1 + "ms");

    start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++)
    {
      String.format("%3.4e", d);
    }
    stop = System.currentTimeMillis();
    long elapsed2 = stop - start;
    System.out.println(
            iterations + " x String.format took " + elapsed2 + "ms");
    assertTrue(elapsed2 > elapsed1);
  }
}
