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

import org.testng.annotations.Test;

public class StructureCommandTest
{
  @Test(groups = "Functional")
  public void testEquals()
  {
    StructureCommand sc1 = new StructureCommand("open");
    assertTrue(sc1.equals(sc1));
    assertTrue(sc1.equals(new StructureCommand("open")));
    assertFalse(sc1.equals(null));
    assertFalse(sc1.equals(new StructureCommand("Open")));
    assertFalse(sc1.equals("Open"));

    StructureCommand sc3 = new StructureCommand("Open", "file",
            "/some/path");
    StructureCommand sc2 = new StructureCommand("Open", "file",
            "/some/path");
    assertFalse(sc1.equals(sc2));
    assertTrue(sc3.equals(sc2));
    assertEquals(sc2.hashCode(), sc3.hashCode());
    assertFalse(
            new StructureCommand("Open file", "/some/path").equals(sc2));
  }
}
