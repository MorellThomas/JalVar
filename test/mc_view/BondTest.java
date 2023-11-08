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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BondTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testTranslate()
  {
    Atom a1 = new Atom(1f, 2f, 3f);
    Atom a2 = new Atom(7f, 6f, 5f);
    Bond b = new Bond(a1, a2);
    b.translate(8f, 9f, 10f);
    assertEquals(9f, b.start[0], 0.0001f);
    assertEquals(11f, b.start[1], 0.0001f);
    assertEquals(13f, b.start[2], 0.0001f);
    assertEquals(15f, b.end[0], 0.0001f);
    assertEquals(15f, b.end[1], 0.0001f);
    assertEquals(15f, b.end[2], 0.0001f);
  }
}
