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

import java.util.List;

import ext.edu.ucsf.rbvi.strucviz2.ChimeraManager;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager;

/**
 * A class to help Jalview start, stop and send commands to ChimeraX.
 * <p>
 * Much of the functionality is common with Chimera, so for convenience we
 * extend ChimeraManager, however note this class is <em>not</em> based on the
 * Cytoscape class at
 * {@code https://github.com/RBVI/structureVizX/blob/master/src/main/java/edu/ucsf/rbvi/structureVizX/internal/model/ChimeraManager.java}.
 * 
 * @author gmcarstairs
 *
 */
public class ChimeraXManager extends ChimeraManager
{

  public ChimeraXManager(StructureManager structureManager)
  {
    super(structureManager);
  }

  public boolean isChimeraX()
  {
    return true;
  }

  /**
   * Returns "POST" as the HTTP request method to use for REST service calls to
   * ChimeraX
   * 
   * @return
   */
  protected String getHttpRequestMethod()
  {
    return "GET";
  }

  /**
   * Adds command-line arguments to start the REST server
   */
  protected void addLaunchArguments(List<String> args)
  {
    args.add("--cmd");
    args.add("remote rest start");
  }

}
