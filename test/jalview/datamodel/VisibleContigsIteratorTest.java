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
import java.util.List;

import org.testng.annotations.Test;

public class VisibleContigsIteratorTest
{
  /**
   * Test the iterator with single visible regions
   */
  @Test(groups = { "Functional" })
  public void testSimpleVisibleRegions()
  {
    List<int[]> hiddenColumns = null;

    // null hidden columns
    VisibleContigsIterator it = new VisibleContigsIterator(3, 10,
            hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    int[] result = it.next();
    assertEquals(3, result[0]);
    assertEquals(9, result[1]);
    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());

    hiddenColumns = new ArrayList<>();

    // no hidden columns
    it = new VisibleContigsIterator(3, 10, hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(3, result[0]);
    assertEquals(9, result[1]);
    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());

    // hidden columns, but not where we are looking
    hiddenColumns.add(new int[] { 5, 10 });
    hiddenColumns.add(new int[] { 25, 40 });

    it = new VisibleContigsIterator(2, 3, hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(2, result[0]);
    assertEquals(2, result[1]);
    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());

    it = new VisibleContigsIterator(5, 7, hiddenColumns);
    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());

    it = new VisibleContigsIterator(11, 15, hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(11, result[0]);
    assertEquals(14, result[1]);
    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());

    it = new VisibleContigsIterator(50, 60, hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(50, result[0]);
    assertEquals(59, result[1]);
    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());
  }

  /**
   * Test the iterator with multiple visible regions
   */
  @Test(groups = { "Functional" })
  public void testMultipleVisibleRegions()
  {
    List<int[]> hiddenColumns = new ArrayList<>();
    hiddenColumns.add(new int[] { 5, 10 });
    hiddenColumns.add(new int[] { 25, 40 });

    // all hidden columns covered
    VisibleContigsIterator it = new VisibleContigsIterator(3, 50,
            hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    int[] result = it.next();
    assertEquals(3, result[0]);
    assertEquals(4, result[1]);

    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(11, result[0]);
    assertEquals(24, result[1]);

    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(41, result[0]);
    assertEquals(49, result[1]);

    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());
  }

  /**
   * Test the iterator with regions which start/end at hidden region edges
   */
  @Test(groups = { "Functional" })
  public void testVisibleRegionsAtHiddenEdges()
  {
    List<int[]> hiddenColumns = new ArrayList<>();
    hiddenColumns.add(new int[] { 5, 10 });
    hiddenColumns.add(new int[] { 25, 40 });

    VisibleContigsIterator it = new VisibleContigsIterator(0, 10,
            hiddenColumns);
    assertTrue(it.hasNext());
    assertTrue(it.endsAtHidden());
    int[] result = it.next();
    assertEquals(0, result[0]);
    assertEquals(4, result[1]);
    assertFalse(it.hasNext());
    assertTrue(it.endsAtHidden());

    it = new VisibleContigsIterator(2, 11, hiddenColumns);
    assertTrue(it.hasNext());
    assertTrue(it.endsAtHidden());
    result = it.next();
    assertEquals(2, result[0]);
    assertEquals(4, result[1]);
    assertFalse(it.hasNext());
    assertTrue(it.endsAtHidden());

    it = new VisibleContigsIterator(2, 12, hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(2, result[0]);
    assertEquals(4, result[1]);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(11, result[0]);
    assertEquals(11, result[1]);
    assertFalse(it.hasNext());
    assertFalse(it.endsAtHidden());

    it = new VisibleContigsIterator(13, 25, hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(13, result[0]);
    assertEquals(24, result[1]);
    assertFalse(it.hasNext());

    it = new VisibleContigsIterator(13, 26, hiddenColumns);
    assertTrue(it.hasNext());
    assertTrue(it.endsAtHidden());
    result = it.next();
    assertEquals(13, result[0]);
    assertEquals(24, result[1]);
    assertFalse(it.hasNext());

    it = new VisibleContigsIterator(13, 27, hiddenColumns);
    assertTrue(it.hasNext());
    assertTrue(it.endsAtHidden());
    result = it.next();
    assertEquals(13, result[0]);
    assertEquals(24, result[1]);
    assertFalse(it.hasNext());

    it = new VisibleContigsIterator(13, 41, hiddenColumns);
    assertTrue(it.hasNext());
    assertTrue(it.endsAtHidden());
    result = it.next();
    assertEquals(13, result[0]);
    assertEquals(24, result[1]);
    assertFalse(it.hasNext());

    it = new VisibleContigsIterator(13, 42, hiddenColumns);
    assertTrue(it.hasNext());
    assertFalse(it.endsAtHidden());
    result = it.next();
    assertEquals(13, result[0]);
    assertEquals(24, result[1]);
    assertTrue(it.hasNext());
    result = it.next();
    assertEquals(41, result[0]);
    assertEquals(41, result[1]);
  }
}
