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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jalview.gui.JvOptionPane;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ShiftListTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testParseMap()
  {
    assertNull(ShiftList.parseMap(null));
    assertNull(ShiftList.parseMap(new int[] {}));

    /*
     * Gap map showing residues in aligned positions 2,3,6,8,9,10,12
     */
    int[] gm = new int[] { 2, 3, 6, 8, 9, 10, 12 };
    List<int[]> shifts = ShiftList.parseMap(gm).getShifts();
    assertEquals(4, shifts.size());

    // TODO are these results (which pass) correct??
    assertEquals("[0, 2]", Arrays.toString(shifts.get(0)));
    assertEquals("[4, 2]", Arrays.toString(shifts.get(1)));
    assertEquals("[7, 1]", Arrays.toString(shifts.get(2)));
    assertEquals("[11, 1]", Arrays.toString(shifts.get(3)));
  }
}
