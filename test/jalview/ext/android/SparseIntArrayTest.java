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

/*
 * Tests for SparseIntArray. Unlike SparseShortArray, SparseIntArray does not throw
 * any exception for overflow.
 */
public class SparseIntArrayTest
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
    SparseIntArray counter = new SparseIntArray();

    /*
     * either key or value may be in the range of int
     */
    counter.put(Integer.MAX_VALUE, Integer.MIN_VALUE);
    counter.put(Integer.MIN_VALUE, Integer.MAX_VALUE);
    assertEquals(counter.get(Integer.MAX_VALUE), Integer.MIN_VALUE);
    assertEquals(counter.get(Integer.MIN_VALUE), Integer.MAX_VALUE);
  }

  @Test(groups = "Functional")
  public void testAdd()
  {
    SparseIntArray counter = new SparseIntArray();

    assertEquals(counter.add('P', 2), 2);
    assertEquals(counter.add('P', 3), 5);
    counter.put('Q', 7);
    assertEquals(counter.add('Q', 4), 11);

    counter.put('x', Integer.MAX_VALUE);
    try
    {
      counter.add('x', 1);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }

    counter.put('y', Integer.MIN_VALUE);
    try
    {
      counter.add('y', -1);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }
  }

  @Test(groups = "Functional")
  public void testCheckOverflow()
  {
    // things that don't overflow:
    SparseIntArray.checkOverflow(Integer.MAX_VALUE, 0);
    SparseIntArray.checkOverflow(Integer.MAX_VALUE, -1);
    SparseIntArray.checkOverflow(Integer.MAX_VALUE, Integer.MIN_VALUE);
    SparseIntArray.checkOverflow(Integer.MAX_VALUE, -Integer.MAX_VALUE);
    SparseIntArray.checkOverflow(0, -Integer.MAX_VALUE);
    SparseIntArray.checkOverflow(0, Integer.MIN_VALUE);
    SparseIntArray.checkOverflow(Integer.MIN_VALUE, 0);
    SparseIntArray.checkOverflow(Integer.MIN_VALUE, 1);
    SparseIntArray.checkOverflow(Integer.MIN_VALUE, Integer.MAX_VALUE);

    // and some that do
    try
    {
      SparseIntArray.checkOverflow(Integer.MAX_VALUE, 1);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }
    try
    {
      SparseIntArray.checkOverflow(Integer.MAX_VALUE - 1, 2);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }
    try
    {
      SparseIntArray.checkOverflow(1, Integer.MAX_VALUE);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }
    try
    {
      SparseIntArray.checkOverflow(Integer.MIN_VALUE, -1);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }
    try
    {
      SparseIntArray.checkOverflow(Integer.MIN_VALUE + 1, -2);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }
    try
    {
      SparseIntArray.checkOverflow(-1, Integer.MIN_VALUE);
      fail("expected exception");
    } catch (ArithmeticException e)
    {
      // expected
    }
  }

}
