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
package jalview.ext.pymol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import jalview.structure.StructureCommand;

public class PymolManagerTest
{
  @Test(groups = "Functional")
  public void testGetPostRequest()
  {
    String req = PymolManager
            .getPostRequest(new StructureCommand("foobar"));
    assertEquals(req,
            "<methodCall><methodName>foobar</methodName><params></params></methodCall>");

    req = PymolManager
            .getPostRequest(new StructureCommand("foobar", "blue", "all"));
    assertEquals(req,
            "<methodCall><methodName>foobar</methodName><params>"
                    + "<parameter><value>blue</value></parameter>"
                    + "<parameter><value>all</value></parameter>"
                    + "</params></methodCall>");
  }

  @Test(groups = "Functional")
  public void testGetPymolPaths()
  {
    /*
     * OSX
     */
    List<String> paths = PymolManager.getPymolPaths("Mac OS X");
    assertEquals(paths.size(), 1);
    assertTrue(
            paths.contains("/Applications/PyMOL.app/Contents/MacOS/PyMOL"));

    /*
     * Linux
     */
    paths = PymolManager.getPymolPaths("Linux i386 1.5.0");
    assertTrue(paths.contains("/usr/local/pymol/bin/PyMOL"));
    assertTrue(paths.contains("/usr/local/bin/PyMOL"));
    assertTrue(paths.contains("/usr/bin/PyMOL"));
    assertTrue(paths.contains("/usr/local/pymol/bin/PyMOL"));
    assertTrue(paths
            .contains(System.getProperty("user.home") + "/opt/bin/PyMOL"));

    /*
     * Windows
     */
    paths = PymolManager.getPymolPaths("Windows 10");
    assertTrue(paths.contains(System.getProperty("user.home")
            + "\\AppData\\Local\\Schrodinger\\PyMOL2\\PyMOLWinWithConsole.bat"));
    assertTrue(paths.contains(System.getProperty("user.home")
            + "\\PyMOL\\PyMOLWinWithConsole.bat"));
    assertTrue(paths
            .contains("C:\\ProgramData\\PyMOL\\PyMOLWinWithConsole.bat"));

    /*
     * Other
     */
    paths = PymolManager.getPymolPaths("VAX/VMS");
    assertTrue(paths.isEmpty());
  }
}
