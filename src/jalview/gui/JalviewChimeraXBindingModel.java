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

import java.util.List;

import ext.edu.ucsf.rbvi.strucviz2.ChimeraModel;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager.ModelType;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.rbvi.chimera.ChimeraXCommands;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.structure.AtomSpec;
import jalview.structure.StructureCommand;
import jalview.structure.StructureSelectionManager;

public class JalviewChimeraXBindingModel extends JalviewChimeraBindingModel
{
  public static final String CHIMERAX_SESSION_EXTENSION = ".cxs";

  public JalviewChimeraXBindingModel(ChimeraViewFrame chimeraViewFrame,
          StructureSelectionManager ssm, PDBEntry[] pdbentry,
          SequenceI[][] sequenceIs, DataSourceType protocol)
  {
    super(chimeraViewFrame, ssm, pdbentry, sequenceIs, protocol);
    setStructureCommands(new ChimeraXCommands());
  }

  @Override
  protected List<String> getChimeraPaths()
  {
    return StructureManager.getChimeraPaths(true);
  }

  @Override
  protected void addChimeraModel(PDBEntry pe,
          List<ChimeraModel> modelsToMap)
  {
    /*
     * ChimeraX hack: force chimera model name to pdbId here
     */
    int modelNumber = chimeraMaps.size() + 1;
    String command = "setattr #" + modelNumber + " models name "
            + pe.getId();
    executeCommand(new StructureCommand(command), false);
    modelsToMap.add(new ChimeraModel(pe.getId(), ModelType.PDB_MODEL,
            modelNumber, 0));
  }

  /**
   * {@inheritDoc}
   * 
   * @return
   */
  @Override
  protected String getCommandFileExtension()
  {
    return ".cxc";
  }

  /**
   * Returns the file extension to use for a saved viewer session file (.cxs)
   * 
   * @return
   * @see https://www.cgl.ucsf.edu/chimerax/docs/user/commands/save.html#sesformat
   */
  @Override
  public String getSessionFileExtension()
  {
    return CHIMERAX_SESSION_EXTENSION;
  }

  @Override
  public String getHelpURL()
  {
    return "http://www.rbvi.ucsf.edu/chimerax/docs/user/index.html";
  }

  @Override
  protected ViewerType getViewerType()
  {
    return ViewerType.CHIMERAX;
  }

  @Override
  protected String getModelId(int pdbfnum, String file)
  {
    return String.valueOf(pdbfnum + 1);
  }

  /**
   * Returns a model of the structure positions described by the ChimeraX format
   * atomspec
   * 
   * @param atomSpec
   * @return
   */
  @Override
  protected AtomSpec parseAtomSpec(String atomSpec)
  {
    return AtomSpec.fromChimeraXAtomspec(atomSpec);
  }

}
