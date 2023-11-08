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
package jalview.structure;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

public class AtomSpecModelTest
{
  @Test(groups = "Functional")
  public void testGetRanges()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertFalse(model.getModels().iterator().hasNext());
    List<int[]> ranges = model.getRanges("1", "A");
    assertTrue(ranges.isEmpty());

    model.addRange("1", 12, 14, "A");
    assertTrue(model.getRanges("1", "B").isEmpty());
    assertTrue(model.getRanges("2", "A").isEmpty());
    ranges = model.getRanges("1", "A");
    assertEquals(ranges.size(), 1);
    int[] range = ranges.get(0);
    assertEquals(range[0], 12);
    assertEquals(range[1], 14);

    /*
     * add some ranges; they should be coalesced and
     * ordered when retrieved
     */
    model.addRange("1", 25, 25, "A");
    model.addRange("1", 20, 24, "A");
    model.addRange("1", 6, 8, "A");
    model.addRange("1", 13, 18, "A");
    model.addRange("1", 5, 6, "A");
    ranges = model.getRanges("1", "A");
    assertEquals(ranges.size(), 3);
    range = ranges.get(0);
    assertEquals(range[0], 5);
    assertEquals(range[1], 8);
    range = ranges.get(1);
    assertEquals(range[0], 12);
    assertEquals(range[1], 18);
    range = ranges.get(2);
    assertEquals(range[0], 20);
    assertEquals(range[1], 25);
  }
}
