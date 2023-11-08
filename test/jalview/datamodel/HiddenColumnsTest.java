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

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.analysis.AlignmentGenerator;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import org.testng.annotations.Test;

public class HiddenColumnsTest
{
  /**
   * Test the method which counts the number of hidden columns
   */
  @Test(groups = { "Functional" })
  public void testGetSize()
  {
    HiddenColumns hidden = new HiddenColumns();
    assertEquals(0, hidden.getSize());

    hidden.hideColumns(3, 5);
    assertEquals(3, hidden.getSize());

    hidden.hideColumns(8, 8);
    assertEquals(4, hidden.getSize());

    hidden.hideColumns(9, 14);
    assertEquals(10, hidden.getSize());

    ColumnSelection cs = new ColumnSelection();
    hidden.revealAllHiddenColumns(cs);
    assertEquals(0, hidden.getSize());
  }

  /**
   * Test the method that finds the visible column position of an alignment
   * column, allowing for hidden columns.
   */
  @Test(groups = { "Functional" })
  public void testFindColumnPosition()
  {
    HiddenColumns cs = new HiddenColumns();
    assertEquals(5, cs.absoluteToVisibleColumn(5));

    // hiding column 6 makes no difference
    cs.hideColumns(6, 6);
    assertEquals(5, cs.absoluteToVisibleColumn(5));

    // hiding column 4 moves column 5 to column 4
    cs.hideColumns(4, 4);
    assertEquals(4, cs.absoluteToVisibleColumn(5));

    // hiding column 4 moves column 4 to position 3
    assertEquals(3, cs.absoluteToVisibleColumn(4));

    // hiding columns 1 and 2 moves column 5 to column 2
    cs.hideColumns(1, 2);
    assertEquals(2, cs.absoluteToVisibleColumn(5));

    // check with > 1 hidden column regions
    // where some columns are in the hidden regions
    HiddenColumns cs2 = new HiddenColumns();
    cs2.hideColumns(5, 10);
    cs2.hideColumns(20, 27);
    cs2.hideColumns(40, 44);

    // hiding columns 5-10 and 20-27 moves column 8 to column 4
    assertEquals(4, cs2.absoluteToVisibleColumn(8));

    // and moves column 24 to 13
    assertEquals(13, cs2.absoluteToVisibleColumn(24));

    // and moves column 28 to 14
    assertEquals(14, cs2.absoluteToVisibleColumn(28));

    // and moves column 40 to 25
    assertEquals(25, cs2.absoluteToVisibleColumn(40));

    // check when hidden columns start at 0 that the visible column
    // is returned as 0
    HiddenColumns cs3 = new HiddenColumns();
    cs3.hideColumns(0, 4);
    assertEquals(0, cs3.absoluteToVisibleColumn(2));

    // check that column after the last hidden region doesn't crash
    assertEquals(46, cs2.absoluteToVisibleColumn(65));
  }

  @Test(groups = { "Functional" })
  public void testVisibleContigsIterator()
  {
    HiddenColumns cs = new HiddenColumns();

    Iterator<int[]> visible = cs.getVisContigsIterator(3, 10, false);
    int[] region = visible.next();
    assertEquals("[3, 9]", Arrays.toString(region));
    assertFalse(visible.hasNext());

    cs.hideColumns(3, 6);
    cs.hideColumns(8, 9);
    cs.hideColumns(12, 12);

    // Test both ends visible region

    // start position is inclusive, end position exclusive
    visible = cs.getVisContigsIterator(1, 13, false);
    region = visible.next();
    assertEquals("[1, 2]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[7, 7]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[10, 11]", Arrays.toString(region));
    assertFalse(visible.hasNext());

    // Test start hidden, end visible
    visible = cs.getVisContigsIterator(4, 14, false);
    region = visible.next();
    assertEquals("[7, 7]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[10, 11]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[13, 13]", Arrays.toString(region));
    assertFalse(visible.hasNext());

    // Test start hidden, end hidden
    visible = cs.getVisContigsIterator(3, 10, false);
    region = visible.next();
    assertEquals("[7, 7]", Arrays.toString(region));
    assertFalse(visible.hasNext());

    // Test start visible, end hidden
    visible = cs.getVisContigsIterator(0, 13, false);
    region = visible.next();
    assertEquals("[0, 2]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[7, 7]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[10, 11]", Arrays.toString(region));
    assertFalse(visible.hasNext());

    // Test empty result
    visible = cs.getVisContigsIterator(4, 6, false);
    assertFalse(visible.hasNext());
  }

  @Test(groups = { "Functional" })
  public void testEquals()
  {
    HiddenColumns cs = new HiddenColumns();
    cs.hideColumns(5, 9);

    // a different set of hidden columns
    HiddenColumns cs2 = new HiddenColumns();

    // with no hidden columns
    assertFalse(cs.equals(cs2));
    assertFalse(cs2.equals(cs));

    // with the wrong kind of object
    assertFalse(cs.equals(new HiddenColumnsCursor()));

    // with a different hiddenColumns object - by size
    HiddenColumns cs3 = new HiddenColumns();
    cs3.hideColumns(2, 3);
    assertFalse(cs.equals(cs3));

    // with hidden columns added in a different order
    cs2.hideColumns(6, 9);
    assertFalse(cs.equals(cs2));
    assertFalse(cs2.equals(cs));

    cs2.hideColumns(5, 8);

    assertTrue(cs.equals(cs2));
    assertTrue(cs.equals(cs));
    assertTrue(cs2.equals(cs));
    assertTrue(cs2.equals(cs2));

    // different ranges, same size
    cs.hideColumns(10, 12);
    cs2.hideColumns(10, 15);
    assertFalse(cs.equals(cs2));

  }

  @Test(groups = "Functional")
  public void testCopyConstructor()
  {
    HiddenColumns cs = new HiddenColumns();
    cs.hideColumns(10, 11);
    cs.hideColumns(5, 7);
    Iterator<int[]> regions = cs.iterator();
    assertEquals("[5, 7]", Arrays.toString(regions.next()));

    HiddenColumns cs2 = new HiddenColumns(cs);
    regions = cs2.iterator();
    assertTrue(cs2.hasHiddenColumns());
    assertEquals(2, cs2.getNumberOfRegions());
    // hidden columns are held in column order
    assertEquals("[5, 7]", Arrays.toString(regions.next()));
    assertEquals("[10, 11]", Arrays.toString(regions.next()));
  }

  @Test(groups = "Functional")
  public void testCopyConstructor2()
  {
    HiddenColumns cs = new HiddenColumns();
    cs.hideColumns(10, 11);
    cs.hideColumns(5, 7);

    HiddenColumns cs2 = new HiddenColumns(cs, 3, 9, 1);
    assertTrue(cs2.hasHiddenColumns());
    Iterator<int[]> regions = cs2.iterator();

    // only [5,7] returned, offset by 1
    assertEquals("[4, 6]", Arrays.toString(regions.next()));
    assertEquals(3, cs2.getSize());

    cs2 = new HiddenColumns(cs, 8, 15, 4);
    regions = cs2.iterator();
    assertTrue(cs2.hasHiddenColumns());

    // only [10,11] returned, offset by 4
    assertEquals("[6, 7]", Arrays.toString(regions.next()));
    assertEquals(2, cs2.getSize());

    cs2 = new HiddenColumns(cs, 6, 10, 4);
    assertFalse(cs2.hasHiddenColumns());
  }

  @Test(groups = { "Functional" })
  public void testHideColumns()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI al = gen.generate(50, 20, 123, 5, 5);

    ColumnSelection colsel = new ColumnSelection();
    HiddenColumns cs = al.getHiddenColumns();
    colsel.hideSelectedColumns(5, al.getHiddenColumns());
    Iterator<int[]> regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[5, 5]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 1);

    colsel.hideSelectedColumns(3, al.getHiddenColumns());
    regions = cs.iterator();
    assertEquals(2, cs.getNumberOfRegions());
    // two hidden ranges, in order:
    assertEquals("[3, 3]", Arrays.toString(regions.next()));
    assertEquals("[5, 5]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 2);

    // hiding column 4 expands [3, 3] to [3, 4]
    // and merges to [5, 5] to make [3, 5]
    colsel.hideSelectedColumns(4, al.getHiddenColumns());
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[3, 5]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 3);

    // clear hidden columns (note they are added to selected)
    cs.revealAllHiddenColumns(colsel);
    // it is now actually null but getter returns an empty list
    assertEquals(0, cs.getNumberOfRegions());
    assertEquals(cs.getSize(), 0);

    cs.hideColumns(3, 6);
    regions = cs.iterator();
    int[] firstHiddenRange = regions.next();
    assertEquals("[3, 6]", Arrays.toString(firstHiddenRange));
    assertEquals(cs.getSize(), 4);

    // adding a subrange of already hidden should do nothing
    cs.hideColumns(4, 5);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[3, 6]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 4);
    cs.hideColumns(3, 5);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[3, 6]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 4);
    cs.hideColumns(4, 6);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[3, 6]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 4);
    cs.hideColumns(3, 6);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[3, 6]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 4);

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(2, 4);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[2, 4]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 3);

    // extend contiguous with 2 positions overlap
    cs.hideColumns(3, 5);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[2, 5]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 4);

    // extend contiguous with 1 position overlap
    cs.hideColumns(5, 6);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[2, 6]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 5);

    // extend contiguous with overlap both ends:
    cs.hideColumns(1, 7);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[1, 7]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 7);

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(15, 18);
    cs.hideColumns(2, 4);
    cs.hideColumns(7, 9);
    regions = cs.iterator();
    assertEquals(3, cs.getNumberOfRegions());
    assertEquals("[2, 4]", Arrays.toString(regions.next()));
    assertEquals("[7, 9]", Arrays.toString(regions.next()));
    assertEquals("[15, 18]", Arrays.toString(regions.next()));
    assertEquals(cs.getSize(), 10);
  }

  /**
   * Test the method that reveals a range of hidden columns given the start
   * column of the range
   */
  @Test(groups = { "Functional" })
  public void testRevealHiddenColumns()
  {
    ColumnSelection colsel = new ColumnSelection();
    HiddenColumns cs = new HiddenColumns();

    // test with null hidden columns
    cs.revealHiddenColumns(5, colsel);
    assertTrue(colsel.getSelected().isEmpty());

    cs.hideColumns(5, 8);
    colsel.addElement(10);
    cs.revealHiddenColumns(5, colsel);

    // hiddenColumns now empty
    assertEquals(0, cs.getSize());

    // revealed columns are marked as selected (added to selection):
    assertEquals("[10, 5, 6, 7, 8]", colsel.getSelected().toString());

    // calling with a column other than the range start does nothing:
    colsel = new ColumnSelection();
    cs = new HiddenColumns();
    cs.hideColumns(5, 8);

    int prevSize = cs.getSize();
    cs.revealHiddenColumns(6, colsel);
    assertEquals(prevSize, cs.getSize());
    assertTrue(colsel.getSelected().isEmpty());

    // reveal hidden columns when there is more than one region
    cs.hideColumns(20, 23);
    // now there are 2 hidden regions
    assertEquals(2, cs.getNumberOfRegions());

    cs.revealHiddenColumns(20, colsel);

    // hiddenColumns now has one region
    assertEquals(1, cs.getNumberOfRegions());

    // revealed columns are marked as selected (added to selection):
    assertEquals("[20, 21, 22, 23]", colsel.getSelected().toString());

    // call with a column past the end of the hidden column ranges
    colsel.clear();
    cs.revealHiddenColumns(20, colsel);
    // hiddenColumns still has 1 region
    assertEquals(1, cs.getNumberOfRegions());
    assertTrue(colsel.getSelected().isEmpty());
  }

  @Test(groups = { "Functional" })
  public void testRevealAllHiddenColumns()
  {
    HiddenColumns hidden = new HiddenColumns();
    ColumnSelection colsel = new ColumnSelection();

    // test with null hidden columns
    hidden.revealAllHiddenColumns(colsel);
    assertTrue(colsel.getSelected().isEmpty());

    hidden.hideColumns(5, 8);
    hidden.hideColumns(2, 3);
    colsel.addElement(11);
    colsel.addElement(1);
    hidden.revealAllHiddenColumns(colsel);

    /*
     * revealing hidden columns adds them (in order) to the (unordered)
     * selection list
     */

    // hiddenColumns now empty
    assertEquals(0, hidden.getSize());

    assertEquals("[11, 1, 2, 3, 5, 6, 7, 8]",
            colsel.getSelected().toString());
  }

  @Test(groups = { "Functional" })
  public void testIsVisible()
  {
    HiddenColumns cs = new HiddenColumns();
    assertTrue(cs.isVisible(5));

    cs.hideColumns(2, 4);
    cs.hideColumns(6, 7);
    assertTrue(cs.isVisible(0));
    assertTrue(cs.isVisible(-99));
    assertTrue(cs.isVisible(1));
    assertFalse(cs.isVisible(2));
    assertFalse(cs.isVisible(3));
    assertFalse(cs.isVisible(4));
    assertTrue(cs.isVisible(5));
    assertFalse(cs.isVisible(6));
    assertFalse(cs.isVisible(7));
    assertTrue(cs.isVisible(8));
  }

  /**
   * Test for the case when a hidden range encloses more one already hidden
   * range
   */
  @Test(groups = { "Functional" })
  public void testHideColumns_subsumingHidden()
  {
    /*
     * JAL-2370 bug scenario:
     * two hidden ranges subsumed by a third
     */
    HiddenColumns cs = new HiddenColumns();
    cs.hideColumns(49, 59);
    cs.hideColumns(69, 79);
    Iterator<int[]> regions = cs.iterator();
    assertEquals(2, cs.getNumberOfRegions());
    assertEquals("[49, 59]", Arrays.toString(regions.next()));
    assertEquals("[69, 79]", Arrays.toString(regions.next()));
    assertEquals(22, cs.getSize());

    cs.hideColumns(48, 80);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[48, 80]", Arrays.toString(regions.next()));
    assertEquals(33, cs.getSize());

    /*
     * another...joining hidden ranges
     */
    cs = new HiddenColumns();
    cs.hideColumns(10, 20);
    cs.hideColumns(30, 40);
    cs.hideColumns(50, 60);
    // hiding 21-49 should merge to one range
    cs.hideColumns(21, 49);
    regions = cs.iterator();
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals("[10, 60]", Arrays.toString(regions.next()));
    assertEquals(51, cs.getSize());

    /*
     * another...left overlap, subsumption, right overlap,
     * no overlap of existing hidden ranges
     */
    cs = new HiddenColumns();
    cs.hideColumns(10, 20);
    cs.hideColumns(10, 20);
    cs.hideColumns(30, 35);
    cs.hideColumns(40, 50);
    cs.hideColumns(60, 70);

    cs.hideColumns(15, 45);
    regions = cs.iterator();
    assertEquals(2, cs.getNumberOfRegions());
    assertEquals("[10, 50]", Arrays.toString(regions.next()));
    assertEquals("[60, 70]", Arrays.toString(regions.next()));
    assertEquals(52, cs.getSize());
  }

  @Test(groups = { "Functional" })
  public void testHideColumns_BitSet()
  {
    HiddenColumns cs;

    BitSet one = new BitSet();

    // one hidden range
    one.set(1);
    cs = new HiddenColumns();
    cs.hideColumns(one);
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals(1, cs.getSize());

    one.set(2);
    cs = new HiddenColumns();
    cs.hideColumns(one);
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals(2, cs.getSize());

    one.set(3);
    cs = new HiddenColumns();
    cs.hideColumns(one);
    assertEquals(1, cs.getNumberOfRegions());
    assertEquals(3, cs.getSize());

    // split
    one.clear(2);
    cs = new HiddenColumns();
    cs.hideColumns(one);
    assertEquals(2, cs.getNumberOfRegions());
    assertEquals(2, cs.getSize());

    assertEquals(0, cs.visibleToAbsoluteColumn(0));
    assertEquals(2, cs.visibleToAbsoluteColumn(1));
    assertEquals(4, cs.visibleToAbsoluteColumn(2));

    // one again
    one.clear(1);
    cs = new HiddenColumns();
    cs.hideColumns(one);
    assertEquals(1, cs.getSize());

    assertEquals(1, cs.getNumberOfRegions());

    assertEquals(0, cs.visibleToAbsoluteColumn(0));
    assertEquals(1, cs.visibleToAbsoluteColumn(1));
    assertEquals(2, cs.visibleToAbsoluteColumn(2));
    assertEquals(4, cs.visibleToAbsoluteColumn(3));
  }

  @Test(groups = { "Functional" })
  public void testRegionsToString()
  {
    HiddenColumns hc = new HiddenColumns();

    String result = hc.regionsToString(",", "--");
    assertEquals("", result);

    hc.hideColumns(3, 7);
    hc.hideColumns(10, 10);
    hc.hideColumns(14, 15);

    result = hc.regionsToString(",", "--");
    assertEquals("3--7,10--10,14--15", result);
  }

  @Test(groups = "Functional")
  public void testGetVisibleStartAndEndIndex()
  {
    Sequence seq = new Sequence("testSeq", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    AlignmentI align = new Alignment(new SequenceI[] { seq });
    HiddenColumns hc = new HiddenColumns();

    int[] startEnd = hc.getVisibleStartAndEndIndex(align.getWidth());
    assertEquals(0, startEnd[0]);
    assertEquals(25, startEnd[1]);

    hc.hideColumns(0, 0);
    startEnd = hc.getVisibleStartAndEndIndex(align.getWidth());
    assertEquals(1, startEnd[0]);
    assertEquals(25, startEnd[1]);

    hc.hideColumns(6, 9);
    hc.hideColumns(11, 12);
    startEnd = hc.getVisibleStartAndEndIndex(align.getWidth());
    assertEquals(1, startEnd[0]);
    assertEquals(25, startEnd[1]);

    hc.hideColumns(24, 25);
    startEnd = hc.getVisibleStartAndEndIndex(align.getWidth());
    System.out.println(startEnd[0] + " : " + startEnd[1]);
    assertEquals(1, startEnd[0]);
    assertEquals(23, startEnd[1]);

    // force lowest range to start of alignment
    hc = new HiddenColumns();
    hc.hideColumns(3, 4);
    startEnd = hc.getVisibleStartAndEndIndex(align.getWidth());
    assertEquals(0, startEnd[0]);
    assertEquals(25, startEnd[1]);
  }

  @Test(groups = "Functional")
  public void testGetRegionWithEdgeAtRes()
  {
    HiddenColumns hc = new HiddenColumns();

    int[] result = hc.getRegionWithEdgeAtRes(5);
    assertNull(result);

    hc.hideColumns(3, 7);
    hc.hideColumns(10, 10);
    hc.hideColumns(14, 15);

    result = hc.getRegionWithEdgeAtRes(2);
    assertEquals(3, result[0]);
    assertEquals(7, result[1]);

    result = hc.getRegionWithEdgeAtRes(4);
    assertEquals(10, result[0]);
    assertEquals(10, result[1]);

    result = hc.getRegionWithEdgeAtRes(5);
    assertEquals(10, result[0]);
    assertEquals(10, result[1]);

    result = hc.getRegionWithEdgeAtRes(6);
    assertNull(result);

    result = hc.getRegionWithEdgeAtRes(0);
    assertNull(result);

    result = hc.getRegionWithEdgeAtRes(7);
    assertEquals(14, result[0]);
    assertEquals(15, result[1]);

    result = hc.getRegionWithEdgeAtRes(8);
    assertEquals(14, result[0]);
    assertEquals(15, result[1]);

    result = hc.getRegionWithEdgeAtRes(16);
    assertNull(result);
  }

  @Test(groups = "Functional")
  public void testHasHiddenColumns()
  {
    HiddenColumns h = new HiddenColumns();

    // new HiddenColumns2 has no hidden cols
    assertFalse(h.hasHiddenColumns());

    // some columns hidden, returns true
    h.hideColumns(5, 10);
    assertTrue(h.hasHiddenColumns());

    // reveal columns, no hidden cols again
    ColumnSelection sel = new ColumnSelection();
    h.revealAllHiddenColumns(sel);
    assertFalse(h.hasHiddenColumns());
  }

  @Test(groups = "Functional")
  public void testHasManyHiddenColumns()
  {
    HiddenColumns h = new HiddenColumns();

    // h has no hidden cols
    assertFalse(h.hasMultiHiddenColumnRegions());

    // one set of columns hidden, returns false
    h.hideColumns(5, 10);
    assertFalse(h.hasMultiHiddenColumnRegions());

    // two sets hidden, returns true
    h.hideColumns(15, 17);
    assertTrue(h.hasMultiHiddenColumnRegions());

    // back to one block, asserts false
    h.hideColumns(11, 14);
    assertFalse(h.hasMultiHiddenColumnRegions());
  }

  @Test(groups = "Functional")
  public void testAdjustForHiddenColumns()
  {
    HiddenColumns h = new HiddenColumns();
    // returns input value when there are no hidden columns
    assertEquals(10, h.visibleToAbsoluteColumn(10));

    h.hideColumns(20, 30);
    assertEquals(10, h.visibleToAbsoluteColumn(10));
    assertEquals(20 + 11, h.visibleToAbsoluteColumn(20));
    assertEquals(35 + 11, h.visibleToAbsoluteColumn(35));

    h.hideColumns(5, 7);
    assertEquals(10 + 3, h.visibleToAbsoluteColumn(10));
    assertEquals(20 + 14, h.visibleToAbsoluteColumn(20));
    assertEquals(35 + 14, h.visibleToAbsoluteColumn(35));

    ColumnSelection sel = new ColumnSelection();
    h.revealAllHiddenColumns(sel);
    h.hideColumns(0, 1);
    assertEquals(4, h.visibleToAbsoluteColumn(2));
  }

  @Test(groups = "Functional")
  public void testGetNextHiddenBoundary_Left()
  {
    HiddenColumns h = new HiddenColumns();

    // returns same value if no hidden cols
    assertEquals(3, h.getNextHiddenBoundary(true, 3));

    h.hideColumns(5, 10);
    assertEquals(10, h.getNextHiddenBoundary(true, 15));
    assertEquals(3, h.getNextHiddenBoundary(true, 3));
    assertEquals(7, h.getNextHiddenBoundary(true, 7));

    h.hideColumns(15, 20);
    assertEquals(10, h.getNextHiddenBoundary(true, 15));
    assertEquals(20, h.getNextHiddenBoundary(true, 21));
  }

  @Test(groups = "Functional")
  public void testGetNextHiddenBoundary_Right()
  {
    HiddenColumns h = new HiddenColumns();

    // returns same value if no hidden cols
    assertEquals(3, h.getNextHiddenBoundary(false, 3));

    h.hideColumns(5, 10);
    assertEquals(5, h.getNextHiddenBoundary(false, 3));
    assertEquals(15, h.getNextHiddenBoundary(false, 15));
    assertEquals(7, h.getNextHiddenBoundary(false, 7));

    h.hideColumns(15, 20);
    assertEquals(15, h.getNextHiddenBoundary(false, 7));
    assertEquals(15, h.getNextHiddenBoundary(false, 14));

    // returns same value if there is no next hidden column
    assertEquals(22, h.getNextHiddenBoundary(false, 22));
  }

  @Test(groups = "Functional")
  public void testIterator()
  {
    HiddenColumns h = new HiddenColumns();
    Iterator<int[]> result = h.iterator();
    assertFalse(result.hasNext());

    h.hideColumns(5, 10);
    result = h.iterator();
    int[] next = result.next();
    assertEquals(5, next[0]);
    assertEquals(10, next[1]);
    assertFalse(result.hasNext());

    h.hideColumns(22, 23);
    result = h.iterator();
    next = result.next();
    assertEquals(5, next[0]);
    assertEquals(10, next[1]);
    next = result.next();
    assertEquals(22, next[0]);
    assertEquals(23, next[1]);
    assertFalse(result.hasNext());

    // test for only one hidden region at start of alignment
    ColumnSelection sel = new ColumnSelection();
    h.revealAllHiddenColumns(sel);
    h.hideColumns(0, 1);
    result = h.iterator();
    next = result.next();
    assertEquals(0, next[0]);
    assertEquals(1, next[1]);
    assertFalse(result.hasNext());
  }

  /* @Test(groups = "Functional")
  public void testGetVisibleSequenceStrings()
  {
    HiddenColumns h = new HiddenColumns();
    SequenceI seq1 = new Sequence("TEST1", "GALMFWKQESPVICYHRNDT");
    SequenceI seq2 = new Sequence("TEST2", "VICYHRNDTGA");
    SequenceI[] seqs = new SequenceI[2];
    seqs[0] = seq1;
    seqs[1] = seq2;
    String[] result = h.getVisibleSequenceStrings(5, 10, seqs);
    assertEquals(2, result.length);
    assertEquals("WKQES", result[0]);
    assertEquals("RNDTG", result[1]);
  
    h.hideColumns(6, 8);
    result = h.getVisibleSequenceStrings(5, 10, seqs);
    assertEquals(2, result.length);
    assertEquals("WS", result[0]);
    assertEquals("RG", result[1]);
  
    SequenceI seq = new Sequence("RefSeq", "-A-SD-ASD--E---");
    ColumnSelection sel = new ColumnSelection();
    h.revealAllHiddenColumns(sel);
    h.hideColumns(1, 3);
    h.hideColumns(6, 11);
    assertEquals("-D",
            h.getVisibleSequenceStrings(0, 5, new SequenceI[]
    { seq })[0]);
  }*/

  @Test(groups = "Functional")
  public void testHideInsertionsFor()
  {
    HiddenColumns h = new HiddenColumns();
    HiddenColumns h2 = new HiddenColumns();
    SequenceI seq1 = new Sequence("TEST1", "GAL---MFW-KQESPVICY--HRNDT");
    SequenceI seq2 = new Sequence("TEST1", "GALMFWKQESPVICYHRNDT");

    h.hideList(seq2.getInsertions());
    assertTrue(h.equals(h2));
    assertEquals(0, h.getSize());

    h.hideList(seq1.getInsertions());
    h2.hideColumns(3, 5);
    h2.hideColumns(9, 9);
    h2.hideColumns(19, 20);
    assertTrue(h.equals(h2));
    assertEquals(6, h.getSize());
  }

  @Test(groups = "Functional")
  public void testHideColumns_BitSet_range()
  {
    HiddenColumns h = new HiddenColumns();
    HiddenColumns h2 = new HiddenColumns();

    BitSet tohide = new BitSet(25);
    h.hideColumns(tohide);
    assertTrue(h.equals(h2));

    // when setting bitset, first param is inclusive, second exclusive
    tohide.set(3, 6);
    tohide.set(9);
    tohide.set(15, 21);
    h.clearAndHideColumns(tohide, 5, 23);

    h2.hideColumns(5, 5);
    h2.hideColumns(9, 9);
    h2.hideColumns(15, 20);
    assertTrue(h.equals(h2));
    assertEquals(h.getSize(), h2.getSize());

    tohide.clear();
    tohide.set(41);
    h.clearAndHideColumns(tohide, 23, 30);
    assertTrue(h.equals(h2));
    assertEquals(h.getSize(), h2.getSize());

    tohide.set(41);
    h.clearAndHideColumns(tohide, 30, 45);
    h2.hideColumns(41, 41);
    assertTrue(h.equals(h2));
    assertEquals(h.getSize(), h2.getSize());

    tohide.clear();
    tohide.set(25, 28);
    h.clearAndHideColumns(tohide, 17, 50);
    h2 = new HiddenColumns();
    h2.hideColumns(5, 5);
    h2.hideColumns(9, 9);
    h2.hideColumns(15, 16);
    h2.hideColumns(25, 27);
    assertTrue(h.equals(h2));
    assertEquals(h.getSize(), h2.getSize());

    HiddenColumns hc = new HiddenColumns();
    hc.hideColumns(3, 5);
    hc.hideColumns(15, 20);
    hc.hideColumns(45, 60);

    tohide = new BitSet();

    // all unhidden if tohide is empty and range covers hidden
    hc.clearAndHideColumns(tohide, 1, 70);
    assertTrue(!hc.hasHiddenColumns());
    assertEquals(0, hc.getSize());

    hc.hideColumns(3, 5);
    hc.hideColumns(15, 20);
    hc.hideColumns(45, 60);
    assertEquals(25, hc.getSize());

    // but not if range does not cover hidden
    hc.clearAndHideColumns(tohide, 23, 40);
    assertTrue(hc.hasHiddenColumns());
    assertEquals(25, hc.getSize());

    // and partial unhide if range partially covers
    hc.clearAndHideColumns(tohide, 1, 17);
    Iterator<int[]> it = hc.iterator();
    assertTrue(it.hasNext());
    int[] region = it.next();

    assertEquals(18, region[0]);
    assertEquals(20, region[1]);

    assertTrue(it.hasNext());
    region = it.next();

    assertEquals(45, region[0]);
    assertEquals(60, region[1]);

    assertFalse(it.hasNext());
    assertEquals(19, hc.getSize());
  }

  @Test(groups = "Functional")
  public void testOffsetByVisibleColumns()
  {
    HiddenColumns h = new HiddenColumns();
    int result = h.offsetByVisibleColumns(-1, 10);
    assertEquals(9, result);

    h.hideColumns(7, 9);
    result = h.offsetByVisibleColumns(-4, 10);
    assertEquals(3, result);

    h.hideColumns(14, 15);
    result = h.offsetByVisibleColumns(-4, 10);
    assertEquals(3, result);

    result = h.offsetByVisibleColumns(-10, 17);
    assertEquals(2, result);

    result = h.offsetByVisibleColumns(-1, 7);
    assertEquals(5, result);

    result = h.offsetByVisibleColumns(-1, 8);
    assertEquals(5, result);

    result = h.offsetByVisibleColumns(-3, 15);
    assertEquals(10, result);

    ColumnSelection sel = new ColumnSelection();
    h.revealAllHiddenColumns(sel);
    h.hideColumns(0, 30);
    result = h.offsetByVisibleColumns(-31, 0);
    assertEquals(-31, result);

    HiddenColumns cs = new HiddenColumns();

    // test that without hidden columns, offsetByVisibleColumns returns
    // position n to left of provided position
    long pos = cs.offsetByVisibleColumns(-3, 10);
    assertEquals(7, pos);

    // 0 returns same position
    pos = cs.offsetByVisibleColumns(0, 10);
    assertEquals(10, pos);

    // overflow to left returns negative number
    pos = cs.offsetByVisibleColumns(-3, 0);
    assertEquals(-3, pos);

    // test that with hidden columns to left of result column
    // behaviour is the same as above
    cs.hideColumns(1, 3);

    // position n to left of provided position
    pos = cs.offsetByVisibleColumns(-3, 10);
    assertEquals(7, pos);

    // 0 returns same position
    pos = cs.offsetByVisibleColumns(0, 10);
    assertEquals(10, pos);

    // test with one set of hidden columns between start and required position
    cs.hideColumns(12, 15);
    pos = cs.offsetByVisibleColumns(-8, 17);
    assertEquals(5, pos);

    // test with two sets of hidden columns between start and required position
    cs.hideColumns(20, 21);
    pos = cs.offsetByVisibleColumns(-8, 23);
    assertEquals(9, pos);

    // repeat last 2 tests with no hidden columns to left of required position
    ColumnSelection colsel = new ColumnSelection();
    cs.revealAllHiddenColumns(colsel);

    // test with one set of hidden columns between start and required position
    cs.hideColumns(12, 15);
    pos = cs.offsetByVisibleColumns(-8, 17);
    assertEquals(5, pos);

    // test with two sets of hidden columns between start and required position
    cs.hideColumns(20, 21);
    pos = cs.offsetByVisibleColumns(-8, 23);
    assertEquals(9, pos);

    // test with right (positive) offsets

    // test that without hidden columns, offsetByVisibleColumns returns
    // position n to right of provided position
    pos = cs.offsetByVisibleColumns(3, 7);
    assertEquals(10, pos);

    // test that with hidden columns to left of result column
    // behaviour is the same as above
    cs.hideColumns(1, 3);

    // test with one set of hidden columns between start and required position
    cs.hideColumns(12, 15);
    pos = cs.offsetByVisibleColumns(8, 5);
    assertEquals(17, pos);

    // test with two sets of hidden columns between start and required position
    cs.hideColumns(20, 21);
    pos = cs.offsetByVisibleColumns(8, 9);
    assertEquals(23, pos);

    // repeat last 2 tests with no hidden columns to left of required position
    colsel = new ColumnSelection();
    cs.revealAllHiddenColumns(colsel);

    // test with one set of hidden columns between start and required position
    cs.hideColumns(12, 15);
    pos = cs.offsetByVisibleColumns(8, 5);
    assertEquals(17, pos);

    // test with two sets of hidden columns between start and required position
    cs.hideColumns(20, 21);
    pos = cs.offsetByVisibleColumns(8, 9);
    assertEquals(23, pos);
  }

  @Test(groups = "Functional")
  public void testBoundedIterator()
  {
    HiddenColumns h = new HiddenColumns();
    Iterator<int[]> it = h.getBoundedIterator(0, 10);

    // no hidden columns = nothing to iterate over
    assertFalse(it.hasNext());

    // [start,end] contains all hidden columns
    // all regions are returned
    h.hideColumns(3, 10);
    h.hideColumns(14, 16);
    it = h.getBoundedIterator(0, 20);
    assertTrue(it.hasNext());
    int[] next = it.next();
    assertEquals(3, next[0]);
    assertEquals(10, next[1]);
    next = it.next();
    assertEquals(14, next[0]);
    assertEquals(16, next[1]);
    assertFalse(it.hasNext());

    // [start,end] overlaps a region
    // 1 region returned
    it = h.getBoundedIterator(5, 7);
    assertTrue(it.hasNext());
    next = it.next();
    assertEquals(3, next[0]);
    assertEquals(10, next[1]);
    assertFalse(it.hasNext());

    // [start,end] fully contains 1 region and start of last
    // - 2 regions returned
    it = h.getBoundedIterator(3, 15);
    assertTrue(it.hasNext());
    next = it.next();
    assertEquals(3, next[0]);
    assertEquals(10, next[1]);
    next = it.next();
    assertEquals(14, next[0]);
    assertEquals(16, next[1]);
    assertFalse(it.hasNext());

    // [start,end] contains end of first region and whole of last region
    // - 2 regions returned
    it = h.getBoundedIterator(4, 20);
    assertTrue(it.hasNext());
    next = it.next();
    assertEquals(3, next[0]);
    assertEquals(10, next[1]);
    next = it.next();
    assertEquals(14, next[0]);
    assertEquals(16, next[1]);
    assertFalse(it.hasNext());
  }

  @Test(groups = "Functional")
  public void testBoundedStartIterator()
  {
    HiddenColumns h = new HiddenColumns();
    Iterator<Integer> it = h.getStartRegionIterator(0, 10);

    // no hidden columns = nothing to iterate over
    assertFalse(it.hasNext());

    // [start,end] contains all hidden columns
    // all regions are returned
    h.hideColumns(3, 10);
    h.hideColumns(14, 16);
    it = h.getStartRegionIterator(0, 20);
    assertTrue(it.hasNext());
    int next = it.next();
    assertEquals(3, next);
    next = it.next();
    assertEquals(6, next);
    assertFalse(it.hasNext());

    // [start,end] does not contain a start of a region
    // no regions to iterate over
    it = h.getStartRegionIterator(4, 5);
    assertFalse(it.hasNext());

    // [start,end] fully contains 1 region and start of last
    // - 2 regions returned
    it = h.getStartRegionIterator(3, 7);
    assertTrue(it.hasNext());
    next = it.next();
    assertEquals(3, next);
    next = it.next();
    assertEquals(6, next);
    assertFalse(it.hasNext());

    // [start,end] contains whole of last region
    // - 1 region returned
    it = h.getStartRegionIterator(4, 20);
    assertTrue(it.hasNext());
    next = it.next();
    assertEquals(6, next);
    assertFalse(it.hasNext());
  }

  @Test(groups = "Functional")
  public void testVisibleBlocksVisBoundsIterator()
  {
    HiddenColumns h = new HiddenColumns();
    Iterator<int[]> regions = h.getVisContigsIterator(0, 31, true);

    // only 1 visible region spanning 0-30 if nothing is hidden
    assertTrue(regions.hasNext());
    int[] region = regions.next();
    assertEquals(0, region[0]);
    assertEquals(30, region[1]);
    assertFalse(regions.hasNext());

    // hide 1 region in middle
    // 2 regions one on either side
    // second region boundary accounts for hidden columns
    h.hideColumns(10, 15);
    regions = h.getVisContigsIterator(0, 31, true);

    assertTrue(regions.hasNext());
    region = regions.next();
    assertEquals(0, region[0]);
    assertEquals(9, region[1]);
    region = regions.next();
    assertEquals(16, region[0]);
    assertEquals(36, region[1]);
    assertFalse(regions.hasNext());

    // single hidden region at left
    h = new HiddenColumns();
    h.hideColumns(0, 5);
    regions = h.getVisContigsIterator(0, 31, true);

    assertTrue(regions.hasNext());
    region = regions.next();
    assertEquals(6, region[0]);
    assertEquals(36, region[1]);
    assertFalse(regions.hasNext());

    // single hidden region at right
    h = new HiddenColumns();
    h.hideColumns(27, 30);
    regions = h.getVisContigsIterator(0, 31, true);

    assertTrue(regions.hasNext());
    region = regions.next();
    assertEquals(0, region[0]);
    assertEquals(26, region[1]);
    region = regions.next();
    assertEquals(31, region[0]);
    assertEquals(34, region[1]);
    assertFalse(regions.hasNext());

    // hidden region at left + hidden region in middle
    h = new HiddenColumns();
    h.hideColumns(0, 5);
    h.hideColumns(23, 25);
    regions = h.getVisContigsIterator(0, 31, true);

    assertTrue(regions.hasNext());
    region = regions.next();
    assertEquals(6, region[0]);
    assertEquals(22, region[1]);
    region = regions.next();
    assertEquals(26, region[0]);
    assertEquals(39, region[1]);
    assertFalse(regions.hasNext());

    // hidden region at right + hidden region in middle
    h = new HiddenColumns();
    h.hideColumns(27, 30);
    h.hideColumns(11, 14);
    regions = h.getVisContigsIterator(0, 31, true);

    assertTrue(regions.hasNext());
    region = regions.next();
    assertEquals(0, region[0]);
    assertEquals(10, region[1]);
    region = regions.next();
    assertEquals(15, region[0]);
    assertEquals(26, region[1]);
    region = regions.next();
    assertEquals(31, region[0]);
    assertEquals(38, region[1]);
    assertFalse(regions.hasNext());

    // hidden region at left and right
    h = new HiddenColumns();
    h.hideColumns(27, 35);
    h.hideColumns(0, 4);
    regions = h.getVisContigsIterator(0, 31, true);

    assertTrue(regions.hasNext());
    region = regions.next();
    assertEquals(5, region[0]);
    assertEquals(26, region[1]);
    region = regions.next();
    assertEquals(36, region[0]);
    assertEquals(44, region[1]);
    assertFalse(regions.hasNext());

    // multiple hidden regions
    h = new HiddenColumns();
    h.hideColumns(1, 1);
    h.hideColumns(3, 5);
    h.hideColumns(9, 11);
    h.hideColumns(22, 26);

    regions = h.getVisContigsIterator(0, 31, true);

    assertTrue(regions.hasNext());
    region = regions.next();
    assertEquals(0, region[0]);
    assertEquals(0, region[1]);
    region = regions.next();
    assertEquals(2, region[0]);
    assertEquals(2, region[1]);
    region = regions.next();
    assertEquals(6, region[0]);
    assertEquals(8, region[1]);
    region = regions.next();
    assertEquals(12, region[0]);
    assertEquals(21, region[1]);
    region = regions.next();
    assertEquals(27, region[0]);
    assertEquals(42, region[1]);
    assertFalse(regions.hasNext());
  }

  /*
   * the VisibleColsIterator is tested elsewhere, this just tests that 
   * it can be retrieved from HiddenColumns
   */
  @Test(groups = "Functional")
  public void testGetVisibleColsIterator()
  {
    HiddenColumns h = new HiddenColumns();
    Iterator<Integer> it = h.getVisibleColsIterator(0, 10);

    assertTrue(it instanceof RangeElementsIterator);
  }

  @Test(groups = "Functional")
  public void testHashCode()
  {
    HiddenColumns h = new HiddenColumns();
    h.hideColumns(0, 25);

    int result = h.hashCode();
    assertTrue(result > 0);

    h.hideColumns(30, 50);
    assertTrue(h.hashCode() > 0);
    assertTrue(result != h.hashCode());
  }
}
