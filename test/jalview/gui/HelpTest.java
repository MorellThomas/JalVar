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
package jalview.gui;

import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.Help.HelpId;

import java.net.URL;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HelpTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void checkHelpTargets() throws HelpSetException
  {
    ClassLoader cl = Desktop.class.getClassLoader();
    URL url = HelpSet.findHelpSet(cl, "help/help"); // $NON-NLS-$
    HelpSet hs = new HelpSet(cl, url);
    Map targets = hs.getLocalMap();

    for (HelpId id : HelpId.values())
    {
      String target = id.getId();
      assertTrue("Unmatched target enum: " + target,
              targets.isValidID(target, hs));
    }

  }
}
