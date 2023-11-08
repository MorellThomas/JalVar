/*******************************************************************************
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
 ******************************************************************************/
package jalview.bin;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jalview.gui.JvOptionPane;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JalviewLiteTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testSeparatorListToArray()
  {
    assertNull(JalviewLite.separatorListToArray(null, "|"));
    assertNull(JalviewLite.separatorListToArray("", "|"));
    assertNull(JalviewLite.separatorListToArray("|", "|"));
    assertNull(JalviewLite.separatorListToArray("abc", "abc"));

    String[] array = JalviewLite.separatorListToArray("abc|def|ghi|", "|");
    assertEquals(3, array.length);
    assertEquals("abc", array[0]);
    assertEquals("def", array[1]);
    assertEquals("ghi", array[2]);

    assertEquals("[abc]",
            Arrays.toString(JalviewLite.separatorListToArray("abc|", "|")));
    assertEquals("[abc]", Arrays
            .toString(JalviewLite.separatorListToArray("abcxy", "xy")));

    // these fail:
    // assertEquals("[abc]",
    // Arrays.toString(JalviewLite.separatorListToArray("|abc", "|")));
    // assertEquals("[abc]", Arrays.toString(JalviewLite.separatorListToArray(
    // "abc|||", "|")));
  }
}
