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
package jalview.ext.rbvi.chimera;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ext.edu.ucsf.rbvi.strucviz2.ChimeraManager;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager;

public class ChimeraConnect
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "External" })
  public void testLaunchAndExit()
  {
    final StructureManager structureManager = new StructureManager(true);
    ChimeraManager cm = new ChimeraManager(structureManager);
    assertTrue("Couldn't launch chimera",
            cm.launchChimera(StructureManager.getChimeraPaths(false)));
    assertTrue(cm.isChimeraLaunched()); // Chimera process is alive
    // int n=0;
    // not sure of the point of this is unless the tester is loading models
    // manually?
    // while (n++ < 100)
    // {
    // try {
    // Thread.sleep(1000);
    // } catch (Exception q)
    // {
    //
    // }
    // Collection<ChimeraModel> cms = cm.getChimeraModels();
    // for (ChimeraModel cmod :cms) {
    // System.out.println(cmod.getModelName());
    // }
    // }
    cm.exitChimera();
    assertFalse(cm.isChimeraLaunched()); // Chimera process has ended
  }

}
