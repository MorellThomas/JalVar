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
package jalview.ext.android;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SparseShortArrayTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testPut()
  {
    SparseShortArray counter = new SparseShortArray();

    /*
     * either key or value may be in the range of short
     */
    counter.put(Short.MAX_VALUE, Short.MIN_VALUE);
    counter.put(Short.MIN_VALUE, Short.MAX_VALUE);

    // put a too large value
    try
    {
      counter.put(0, Short.MAX_VALUE + 1);
      fail("Expected exception");
    } catch (ArithmeticException e)
    {
      // expected;
    }

    // put a too small value
    try
    {
      counter.put(1, Short.MIN_VALUE - 1);
      fail("Expected exception");
    } catch (ArithmeticException e)
    {
      // expected;
    }

    // put a too large key
    try
    {
      counter.put(Short.MAX_VALUE + 1, 0);
      fail("Expected exception");
    } catch (ArithmeticException e)
    {
      // expected;
    }

    // put a too small key
    try
    {
      counter.put(Short.MIN_VALUE - 1, 2);
      fail("Expected exception");
    } catch (ArithmeticException e)
    {
      // expected;
    }
  }

  @Test(groups = "Functional")
  public void testAdd()
  {
    SparseShortArray counter = new SparseShortArray();

    assertEquals(counter.add('P', 2), 2);
    assertEquals(counter.add('P', 3), 5);
    counter.put('Q', 7);
    assertEquals(counter.add('Q', 4), 11);

    // increment giving overflow
    counter.put('x', Short.MAX_VALUE);
    try
    {
      counter.add('x', 1);
      fail("Expected exception");
    } catch (ArithmeticException e)
    {
      // expected;
    }

    // decrement giving underflow
    counter.put('y', Short.MIN_VALUE);
    try
    {
      counter.add('y', -1);
      fail("Expected exception");
    } catch (ArithmeticException e)
    {
      // expected;
    }
  }
}
