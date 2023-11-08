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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CaseInsensitiveStringTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testEquals()
  {
    CaseInsensitiveString s1 = new CaseInsensitiveString(null);
    CaseInsensitiveString s2 = new CaseInsensitiveString("a");
    CaseInsensitiveString s3 = new CaseInsensitiveString("A");
    CaseInsensitiveString s4 = new CaseInsensitiveString("b");

    assertFalse(s1.equals(null));
    assertTrue(s1.equals(s1));
    assertFalse(s1.equals(s2));
    assertTrue(s2.equals(s2));
    assertFalse(s2.equals(s1));
    assertTrue(s2.equals(s3));
    assertTrue(s3.equals(s2));
    assertFalse(s3.equals(s4));
    assertFalse(s4.equals(s3));
  }

  @Test(groups = "Functional")
  public void testHashcode()
  {
    CaseInsensitiveString s1 = new CaseInsensitiveString(null);
    CaseInsensitiveString s2 = new CaseInsensitiveString("a");
    CaseInsensitiveString s3 = new CaseInsensitiveString("A");
    CaseInsensitiveString s4 = new CaseInsensitiveString("b");

    assertNotEquals(s1.hashCode(), s2.hashCode());
    assertEquals(s2.hashCode(), s3.hashCode());
    assertNotEquals(s3.hashCode(), s4.hashCode());
  }
}
