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

import org.testng.annotations.Test;

public class MessageManagerTest
{
  @Test(groups = "Functional")
  public void testFormatMessage_invalid()
  {
    String msg = MessageManager.formatMessage("label.rubbish", "goodbye",
            "world");
    assertEquals(msg, "[missing key] label.rubbish 'goodbye' 'world'");
  }

  @Test(groups = "Functional")
  public void testGetString_invalid()
  {
    String msg = MessageManager.getString("label.rubbish");
    assertEquals(msg, "[missing key] label.rubbish");
  }

  @Test(groups = "Functional")
  public void testGetStringOrReturn()
  {
    String msg = MessageManager.getStringOrReturn("label.rubbish",
            "rubbishdefault");
    assertEquals(msg, "rubbishdefault");
  }
}
