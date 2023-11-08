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

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import jalview.gui.JvOptionPane;

import java.util.Vector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResidueTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testFindAtom()
  {
    Atom a1 = new Atom(1f, 2f, 3f);
    a1.name = "C";
    Atom a2 = new Atom(1f, 2f, 3f);
    a2.name = "A";
    Atom a3 = new Atom(1f, 2f, 3f);
    a3.name = "P";
    Atom a4 = new Atom(1f, 2f, 3f);
    a4.name = "C";
    Vector<Atom> v = new Vector<Atom>();
    v.add(a1);
    v.add(a2);
    v.add(a3);
    v.add(a4);
    Residue r = new Residue(v, 293, 12);

    assertSame(a1, r.findAtom("C"));
    assertSame(a2, r.findAtom("A"));
    assertSame(a3, r.findAtom("P"));
    assertNull(r.findAtom("S"));
  }
}
