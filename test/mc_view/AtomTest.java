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
package mc_view;

import static org.testng.AssertJUnit.assertEquals;

import jalview.gui.JvOptionPane;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AtomTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test the constructor that parses a PDB file format ATOM line. Fields are in
   * fixed column positions
   */
  @Test(groups = { "Functional" })
  public void testStringConstructor()
  {
    Atom a = new Atom(
            "ATOM    349  NE2 GLN A  48      22.290   8.595  17.680  1.00 14.30           N");
    assertEquals(349, a.atomIndex);
    assertEquals("NE", a.name);
    assertEquals("GLN", a.resName);
    assertEquals("A", a.chain);
    assertEquals(48, a.resNumber);
    assertEquals("48", a.resNumIns);
    assertEquals(' ', a.insCode);
    assertEquals(22.290, a.x, 0.00001);
    assertEquals(8.595, a.y, 0.00001);
    assertEquals(17.680, a.z, 0.00001);
    assertEquals(1f, a.occupancy, 0.00001);
    assertEquals(14.3, a.tfactor, 0.00001);
  }

  /**
   * Test the case where occupancy and temp factor are blank - should default to
   * 1
   */
  @Test(groups = { "Functional" })
  public void testStringConstructor_blankOccupancyTempFactor()
  {
    Atom a = new Atom(
            "ATOM    349  NE2 GLN A  48      22.290   8.595  17.680                       N");
    assertEquals(1f, a.occupancy, 0.00001);
    assertEquals(1f, a.tfactor, 0.00001);
  }

  /**
   * Parsing non-numeric data as Atom throws an exception
   */
  @Test(groups = { "Functional" })
  public void testStringConstructor_malformed()
  {
    try
    {
      new Atom(
              "ATOM    34N  NE2 GLN A  48      22.290   8.595  17.680  1.00 14.30           N");
      Assert.fail("Expected exception");
    } catch (NumberFormatException e)
    {
      // expected
    }
  }
}
