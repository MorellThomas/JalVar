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
package jalview.schemes;

import static org.testng.AssertJUnit.assertEquals;

import jalview.gui.JvOptionPane;

import java.awt.Color;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UserColourSchemeTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testParseAppletParameter()
  {
    UserColourScheme cs = new UserColourScheme("white");
    cs.parseAppletParameter("D,E=red; K,R,H=0022FF; c=10 , 20,30");
    assertEquals(Color.RED, cs.findColour('D'));
    assertEquals(Color.RED, cs.findColour('d'));
    assertEquals(Color.RED, cs.findColour('E'));
    assertEquals(Color.RED, cs.findColour('e'));
    Color c1 = new Color(0x0022ff);
    assertEquals(c1, cs.findColour('K'));
    assertEquals(c1, cs.findColour('R'));
    assertEquals(c1, cs.findColour('h'));
    Color c2 = new Color(10, 20, 30);
    assertEquals(c2, cs.findColour('c'));
    assertEquals(Color.WHITE, cs.findColour('G'));
    assertEquals(Color.WHITE, cs.findColour('-'));
    assertEquals(Color.WHITE, cs.findColour('.'));
    assertEquals(Color.WHITE, cs.findColour(' '));

    cs = new UserColourScheme("white");
    cs.parseAppletParameter(
            "D,E=red; K,R,H=0022FF; c=10 , 20,30;t=orange;lowercase=blue;s=pink");
    assertEquals(Color.RED, cs.findColour('D'));
    assertEquals(Color.blue, cs.findColour('d'));
    assertEquals(Color.RED, cs.findColour('E'));
    assertEquals(Color.blue, cs.findColour('e'));
    assertEquals(c1, cs.findColour('K'));
    assertEquals(c1, cs.findColour('R'));
    assertEquals(Color.blue, cs.findColour('h'));
    assertEquals(c2, cs.findColour('c'));
    // 'lowercase' sets all lower-case not already set to the given colour
    assertEquals(Color.orange, cs.findColour('t'));
    assertEquals(Color.blue, cs.findColour('k'));
    assertEquals(Color.blue, cs.findColour('a'));
    assertEquals(Color.pink, cs.findColour('s'));
  }

  @Test(groups = "Functional")
  public void testToAppletParameter()
  {
    UserColourScheme cs = new UserColourScheme(
            "E,D=red; K,R,H=0022FF; c=10 , 20,30");
    String param = cs.toAppletParameter();
    assertEquals("D,E=ff0000;H,K,R=0022ff;c=0a141e", param);
  }

  /**
   * Test for user colour scheme constructed with a colour per residue,
   * including gap. Note this can currently be done from the User Defined
   * Colours dialog, but not by parsing a colours parameter, as
   * parseAppletParameter only recognises amino acid codes.
   */
  @Test(groups = "Functional")
  public void testConstructor_coloursArray()
  {
    Color g = Color.green;
    Color y = Color.yellow;
    Color b = Color.blue;
    Color r = Color.red;
    // colours for ARNDCQEGHILKMFPSTWYVBZ and gap
    Color[] colours = new Color[] { g, y, b, r, g, y, r, b, g, y, r, b, g,
        y, r, b, g, y, r, b, g, y, r, g };
    UserColourScheme cs = new UserColourScheme(colours);

    assertEquals(g, cs.findColour('A'));
    assertEquals(b, cs.findColour('n'));
    assertEquals(g, cs.findColour('-'));
    assertEquals(g, cs.findColour('.'));
    assertEquals(g, cs.findColour(' '));
  }
}
