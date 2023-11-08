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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

public class HiddenColumnsCursorTest
{

  @Test(groups = { "Functional" })
  public void testConstructor()
  {
    HiddenColumnsCursor cursor = new HiddenColumnsCursor();
    assertNull(cursor.findRegionForColumn(0, false));

    List<int[]> hlist = new ArrayList<>();
    cursor = new HiddenColumnsCursor(hlist);
    assertNull(cursor.findRegionForColumn(0, false));

    cursor = new HiddenColumnsCursor(hlist, 3, 12);
    assertNull(cursor.findRegionForColumn(0, false));

    hlist.add(new int[] { 3, 7 });
    hlist.add(new int[] { 15, 25 });
    cursor = new HiddenColumnsCursor(hlist);
    HiddenCursorPosition p = cursor.findRegionForColumn(8, false);
    assertEquals(1, p.getRegionIndex());

    cursor = new HiddenColumnsCursor(hlist, 1, 5);
    p = cursor.findRegionForColumn(8, false);
    assertEquals(1, p.getRegionIndex());
  }

  /**
   * Test the method which finds the corresponding region given a column
   */
  @Test(groups = { "Functional" })
  public void testFindRegionForColumn()
  {
    HiddenColumnsCursor cursor = new HiddenColumnsCursor();

    HiddenCursorPosition pos = cursor.findRegionForColumn(20, false);
    assertNull(pos);

    List<int[]> hidden = new ArrayList<>();
    hidden.add(new int[] { 53, 76 });
    hidden.add(new int[] { 104, 125 });

    cursor = new HiddenColumnsCursor(hidden);

    int regionIndex = cursor.findRegionForColumn(126, false)
            .getRegionIndex();
    assertEquals(2, regionIndex);

    regionIndex = cursor.findRegionForColumn(125, false).getRegionIndex();
    assertEquals(1, regionIndex);

    regionIndex = cursor.findRegionForColumn(108, false).getRegionIndex();
    assertEquals(1, regionIndex);

    regionIndex = cursor.findRegionForColumn(104, false).getRegionIndex();
    assertEquals(1, regionIndex);

    regionIndex = cursor.findRegionForColumn(103, false).getRegionIndex();
    assertEquals(1, regionIndex);

    regionIndex = cursor.findRegionForColumn(77, false).getRegionIndex();
    assertEquals(1, regionIndex);

    regionIndex = cursor.findRegionForColumn(76, false).getRegionIndex();
    assertEquals(0, regionIndex);

    regionIndex = cursor.findRegionForColumn(53, false).getRegionIndex();
    assertEquals(0, regionIndex);

    regionIndex = cursor.findRegionForColumn(52, false).getRegionIndex();
    assertEquals(0, regionIndex);

    regionIndex = cursor.findRegionForColumn(0, false).getRegionIndex();
    assertEquals(0, regionIndex);

    hidden.add(new int[] { 138, 155 });

    cursor = new HiddenColumnsCursor(hidden);

    regionIndex = cursor.findRegionForColumn(160, false).getRegionIndex();
    assertEquals(3, regionIndex);

    regionIndex = cursor.findRegionForColumn(100, false).getRegionIndex();
    assertEquals(1, regionIndex);
  }

  /**
   * Test the method which counts the number of hidden columns before a column
   */
  @Test(groups = { "Functional" })
  public void testFindRegionForColumn_Visible()
  {
    HiddenColumnsCursor cursor = new HiddenColumnsCursor();

    HiddenCursorPosition pos = cursor.findRegionForColumn(20, true);
    assertNull(pos);

    List<int[]> hidden = new ArrayList<>();
    hidden.add(new int[] { 53, 76 });
    hidden.add(new int[] { 104, 125 });

    cursor = new HiddenColumnsCursor(hidden);

    int offset = cursor.findRegionForColumn(80, true).getHiddenSoFar();
    assertEquals(46, offset);

    offset = cursor.findRegionForColumn(79, true).getHiddenSoFar();
    assertEquals(24, offset);

    offset = cursor.findRegionForColumn(53, true).getHiddenSoFar();
    assertEquals(24, offset);

    offset = cursor.findRegionForColumn(52, true).getHiddenSoFar();
    assertEquals(0, offset);

    offset = cursor.findRegionForColumn(10, true).getHiddenSoFar();
    assertEquals(0, offset);

    offset = cursor.findRegionForColumn(0, true).getHiddenSoFar();
    assertEquals(0, offset);

    offset = cursor.findRegionForColumn(79, true).getHiddenSoFar();
    assertEquals(24, offset);

    offset = cursor.findRegionForColumn(80, true).getHiddenSoFar();
    assertEquals(46, offset);
  }
}
