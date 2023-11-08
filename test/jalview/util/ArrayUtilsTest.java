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

import jalview.gui.JvOptionPane;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ArrayUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testReverseIntArray()
  {

    // null value: should be no exception
    ArrayUtils.reverseIntArray((int[]) null);

    // empty array: should be no exception
    int[] arr = new int[] {};
    ArrayUtils.reverseIntArray(arr);

    // even length array
    arr = new int[] { 1, 2, 3, 4 };
    ArrayUtils.reverseIntArray(arr);
    assertEquals("[4, 3, 2, 1]", Arrays.toString(arr));

    // odd length array
    arr = new int[] { 1, 2, 3, 4, 5 };
    ArrayUtils.reverseIntArray(arr);
    assertEquals("[5, 4, 3, 2, 1]", Arrays.toString(arr));
  }
}
