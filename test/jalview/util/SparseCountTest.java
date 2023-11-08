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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SparseCountTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testAdd()
  {
    SparseCount p = new SparseCount(8);
    p.add('a', 1);
    p.add('b', 2);
    p.add('a', 3);
    p.add('b', -4);
    assertEquals(p.size(), 2);
    assertEquals(p.get('a'), 4);
    assertEquals(p.get('b'), -2);
  }

  @Test(groups = "Functional")
  public void testPut()
  {
    SparseCount p = new SparseCount(8);
    p.put('a', 3);
    p.add('b', 2);
    p.put('b', 4);
    assertEquals(p.size(), 2);
    assertEquals(p.get('a'), 3);
    assertEquals(p.get('b'), 4);
  }

  /**
   * Test handling overflow of short by switching to counting ints
   */
  @Test(groups = "Functional")
  public void testOverflow()
  {
    SparseCount p = new SparseCount(8);
    p.put('a', Short.MAX_VALUE - 1);
    p.add('a', 1);
    assertFalse(p.isUsingInt());
    p.add('a', 1);
    assertTrue(p.isUsingInt());
  }

  /**
   * Test handling underflow of short by switching to counting ints
   */
  @Test(groups = "Functional")
  public void testUnderflow()
  {
    SparseCount p = new SparseCount(8);
    p.put('a', Short.MIN_VALUE + 1);
    p.add('a', -1);
    assertFalse(p.isUsingInt());
    p.add('a', -1);
    assertTrue(p.isUsingInt());
  }

  @Test(groups = "Functional")
  public void testKeyAt_ValueAt()
  {
    SparseCount p = new SparseCount(8);
    p.put('W', 12);
    p.put('K', 9);
    p.put('R', 6);
    assertEquals(p.size(), 3);
    assertEquals(p.keyAt(0), 'K');
    assertEquals(p.valueAt(0), 9);
    assertEquals(p.keyAt(1), 'R');
    assertEquals(p.valueAt(1), 6);
    assertEquals(p.keyAt(2), 'W');
    assertEquals(p.valueAt(2), 12);
  }

}
