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
package jalview.bin;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import jalview.bin.ArgsParser;
import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ArgsParserTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testGetValue()
  {
    ArgsParser ap = new ArgsParser(
            new String[]
            { "-name", "Henry", "-job", "Tester" });
    assertEquals(4, ap.getSize());
    assertNull(ap.getValue("rubbish"));
    assertEquals("Tester", ap.getValue("job"));
    // call to getValue removes the argument and its value
    assertEquals(2, ap.getSize());
    assertNull(ap.getValue("job"));
    assertFalse(ap.contains("job"));
    assertFalse(ap.contains("Tester"));

    assertEquals("Henry", ap.getValue("name"));
    assertEquals(0, ap.getSize());
    assertNull(ap.getValue("name"));
  }

  @Test(groups = "Functional")
  public void testGetValue_decoded()
  {
    ArgsParser ap = new ArgsParser(
            new String[]
            { "-name%241", "Henry", "-job", "Test%203%2a" });
    // parameter value is decoded
    assertEquals("Test 3*", ap.getValue("job", true));
    // parameter name is not decoded
    assertNull(ap.getValue("name$1", true));
    assertEquals("Henry", ap.getValue("name%241", true));
  }

  @Test(groups = "Functional")
  public void testNextValue()
  {
    ArgsParser ap = new ArgsParser(
            new String[]
            { "-name", "Henry", "-job", "Tester" });
    assertEquals("name", ap.nextValue());
    assertEquals("Henry", ap.nextValue());
    assertEquals("job", ap.nextValue());
    assertEquals("Tester", ap.nextValue());
  }

  @Test(groups = "Functional")
  public void testContains()
  {
    ArgsParser ap = new ArgsParser(
            new String[]
            { "-name", "Henry", "-job", "Tester" });
    assertFalse(ap.contains("Susan"));
    assertFalse(ap.contains("-name"));
    assertTrue(ap.contains("name"));
    // testing for contains removes the argument
    assertFalse(ap.contains("name"));
  }
}
