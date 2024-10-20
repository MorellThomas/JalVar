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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jalview.api.AlignmentViewPanel;
import jalview.bin.Console;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.pymol.PymolCommands;
import jalview.ext.pymol.PymolManager;
import jalview.gui.StructureViewer.ViewerType;
import jalview.structure.AtomSpec;
import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;

public class PymolBindingModel extends AAStructureBindingModel
{
  /*
   * format for labels shown on structures when mousing over sequence;
   * see https://pymolwiki.org/index.php/Label#examples
   * left not final so customisable e.g. with a Groovy script
   */
  private static String LABEL_FORMAT = "\"%s %s\" % (resn,resi)";

  private PymolManager pymolManager;

  /*
   * full paths to structure files opened in PyMOL
   */
  List<String> structureFiles = new ArrayList<>();

  /*
   * lookup from file path to PyMOL object name
   */
  Map<String, String> pymolObjects = new HashMap<>();

  private String lastLabelSpec;

  /**
   * Constructor
   * 
   * @param viewer
   * @param ssm
   * @param pdbentry
   * @param sequenceIs
   */
  public PymolBindingModel(StructureViewerBase viewer,
          StructureSelectionManager ssm, PDBEntry[] pdbentry,
          SequenceI[][] sequenceIs)
  {
    super(ssm, pdbentry, sequenceIs, null);
    pymolManager = new PymolManager();
    setStructureCommands(new PymolCommands());
    setViewer(viewer);
  }

  @Override
  public String[] getStructureFiles()
  {
    return structureFiles.toArray(new String[structureFiles.size()]);
  }

  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    /*
     * https://pymolwiki.org/index.php/indicate#examples
     */
    StringBuilder sb = new StringBuilder();
    for (AtomSpec atom : atoms)
    {
      // todo promote to StructureCommandsI.showLabel()
      String modelId = getModelIdForFile(atom.getPdbFile());
      sb.append(String.format(" %s//%s/%d/*", modelId, atom.getChain(),
              atom.getPdbResNum()));
    }
    String labelSpec = sb.toString();
    if (labelSpec.equals(lastLabelSpec))
    {
      return;
    }
    StructureCommandI command = new StructureCommand("indicate", labelSpec);
    executeCommand(command, false);

    lastLabelSpec = labelSpec;
  }

  @Override
  public SequenceRenderer getSequenceRenderer(AlignmentViewPanel avp)
  {
    return new SequenceRenderer(avp.getAlignViewport());
  }

  @Override
  protected List<String> executeCommand(StructureCommandI command,
          boolean getReply)
  {
    // System.out.println(command.toString()); // debug
    return pymolManager.sendCommand(command, getReply);
  }

  @Override
  protected String getModelIdForFile(String file)
  {
    return pymolObjects.containsKey(file) ? pymolObjects.get(file) : "";
  }

  @Override
  protected ViewerType getViewerType()
  {
    return ViewerType.PYMOL;
  }

  @Override
  public boolean isViewerRunning()
  {
    return pymolManager != null && pymolManager.isPymolLaunched();
  }

  @Override
  public void closeViewer(boolean closePymol)
  {
    super.closeViewer(closePymol);
    pymolManager = null;
  }

  public boolean launchPymol()
  {
    if (pymolManager.isPymolLaunched())
    {
      return true;
    }

    Process pymol = pymolManager.launchPymol();
    if (pymol != null)
    {
      // start listening for PyMOL selections - how??
      startExternalViewerMonitor(pymol);
    }
    else
    {
      Console.error("Failed to launch PyMOL!");
    }
    return pymol != null;
  }

  public void openFile(PDBEntry pe)
  {
    // todo : check not already open, remap / rename, etc
    String file = pe.getFile();
    StructureCommandI cmd = getCommandGenerator().loadFile(file);

    /*
     * a second parameter sets the pdbid as the loaded PyMOL object name
     */
    String pdbId = pe.getId();
    try
    {
      String safePDBId = java.net.URLEncoder.encode(pdbId, "UTF-8");
      pdbId = safePDBId.replace('%', '_');
      pdbId = pdbId.replace("-", "__");
      char fc = pdbId.charAt(0);
      // put an 's' before any numerics
      if (fc >= '0' && fc <= '9')
      {
        pdbId = 's' + pdbId;
      }
      // pdbId.replace('-', 0)
    } catch (Exception x)
    {
      Console.error("Unxpected encoding exception for '" + pdbId + "'", x);
    }
    cmd.addParameter(pdbId);

    executeCommand(cmd, false);

    pymolObjects.put(file, pdbId);
    if (!structureFiles.contains(file))
    {
      structureFiles.add(file);
    }
    if (getSsm() != null)
    {
      getSsm().addStructureViewerListener(this);
    }

  }

  @Override
  protected String getModelId(int pdbfnum, String file)
  {
    return file;
  }

  /**
   * Returns the file extension to use for a saved viewer session file (.pse)
   * 
   * @return
   * @see https://pymolwiki.org/index.php/Save
   */
  @Override
  public String getSessionFileExtension()
  {
    return ".pse";
  }

  @Override
  public String getHelpURL()
  {
    return "https://pymolwiki.org/";
  }

  /**
   * Constructs and sends commands to set atom properties for visible Jalview
   * features on residues mapped to structure
   * 
   * @param avp
   * @return
   */
  public int sendFeaturesToViewer(AlignmentViewPanel avp)
  {
    // todo pull up this and JalviewChimeraBinding variant
    Map<String, Map<Object, AtomSpecModel>> featureValues = buildFeaturesMap(
            avp);
    List<StructureCommandI> commands = getCommandGenerator()
            .setAttributes(featureValues);
    executeCommands(commands, false, null);
    return commands.size();
  }

}
