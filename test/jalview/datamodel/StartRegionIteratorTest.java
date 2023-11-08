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
package jalview.datamodel;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

public class StartRegionIteratorTest
{
  /**
   * Test the start region iterator
   */
  @Test(groups = { "Functional" })
  public void testBasicBoundsIterator()
  {
    List<int[]> hiddenColumns = null;

    // null hidden columns
    Iterator<Integer> it = new StartRegionIterator(3, 10, hiddenColumns);
    assertFalse(it.hasNext());

    hiddenColumns = new ArrayList<>();

    // no hidden columns
    it = new StartRegionIterator(3, 10, hiddenColumns);
    assertFalse(it.hasNext());

    // add some hidden columns
    hiddenColumns.add(new int[] { 5, 10 });
    hiddenColumns.add(new int[] { 25, 40 });

    it = new StartRegionIterator(3, 10, hiddenColumns);
    assertTrue(it.hasNext());
    Integer result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(3, 15, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(3, 18, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(3, 19, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(19, (int) result);
    assertFalse(it.hasNext());

    hiddenColumns.add(new int[] { 47, 50 });

    it = new StartRegionIterator(15, 60, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(19, (int) result);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(25, (int) result);
    assertFalse(it.hasNext());
  }

  /**
   * Test the start region iterator with null cursor
   */
  @Test(groups = { "Functional" })
  public void testBoundsIteratorUsingNullCursor()
  {
    List<int[]> hiddenColumns = null;
    HiddenCursorPosition pos = null;

    // null hidden columns
    Iterator<Integer> it = new StartRegionIterator(pos, 3, 10,
            hiddenColumns);
    assertFalse(it.hasNext());

    hiddenColumns = new ArrayList<>();

    // no hidden columns
    it = new StartRegionIterator(pos, 3, 10, hiddenColumns);
    assertFalse(it.hasNext());

    // add some hidden columns
    hiddenColumns.add(new int[] { 5, 10 });
    hiddenColumns.add(new int[] { 25, 40 });

    it = new StartRegionIterator(pos, 3, 10, hiddenColumns);
    assertTrue(it.hasNext());
    Integer result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(pos, 3, 15, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(pos, 3, 18, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(pos, 3, 19, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(19, (int) result);
    assertFalse(it.hasNext());

    hiddenColumns.add(new int[] { 47, 50 });

    it = new StartRegionIterator(pos, 15, 60, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(19, (int) result);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(25, (int) result);
    assertFalse(it.hasNext());
  }

  /**
   * Test the start region iterator with nonnull cursor
   */
  @Test(groups = { "Functional" })
  public void testBoundsIteratorUsingCursor()
  {
    List<int[]> hiddenColumns = new ArrayList<>();

    // add some hidden columns
    hiddenColumns.add(new int[] { 5, 10 });
    hiddenColumns.add(new int[] { 25, 40 });

    HiddenCursorPosition pos = new HiddenCursorPosition(0, 0);

    Iterator<Integer> it = new StartRegionIterator(pos, 3, 10,
            hiddenColumns);
    assertTrue(it.hasNext());
    Integer result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(pos, 3, 15, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(pos, 3, 18, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertFalse(it.hasNext());

    it = new StartRegionIterator(pos, 3, 19, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(5, (int) result);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(19, (int) result);
    assertFalse(it.hasNext());

    pos = new HiddenCursorPosition(1, 6);
    hiddenColumns.add(new int[] { 47, 50 });

    it = new StartRegionIterator(pos, 15, 60, hiddenColumns);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(19, (int) result);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(25, (int) result);
    assertFalse(it.hasNext());
  }
}
