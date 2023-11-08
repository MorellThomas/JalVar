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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsI.AtomSpecType;

/**
 * Routines for generating ChimeraX commands for Jalview/ChimeraX binding
 */
public class ChimeraXCommands extends ChimeraCommands
{
  // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/info.html#resattr
  private static final StructureCommand LIST_RESIDUE_ATTRIBUTES = new StructureCommand(
          "info resattr");

  // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/exit.html
  private static final StructureCommand CLOSE_CHIMERAX = new StructureCommand(
          "exit");

  // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/info.html#notify
  private static final StructureCommand STOP_NOTIFY_SELECTION = new StructureCommand(
          "info notify stop selection jalview");

  private static final StructureCommand STOP_NOTIFY_MODELS = new StructureCommand(
          "info notify stop models jalview");

  // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/info.html#selection
  private static final StructureCommand GET_SELECTION = new StructureCommand(
          "info selection level residue");

  private static final StructureCommand SHOW_BACKBONE = new StructureCommand(
          "~display all;~ribbon;show @CA|P atoms");

  // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/view.html
  private static final StructureCommand FOCUS_VIEW = new StructureCommand(
          "view");

  private static final StructureCommandI COLOUR_BY_CHARGE = new StructureCommand(
          "color white;color :ASP,GLU red;color :LYS,ARG blue;color :CYS yellow");

  @Override
  public List<StructureCommandI> colourByCharge()
  {
    return Arrays.asList(COLOUR_BY_CHARGE);
  }

  @Override
  public String getResidueSpec(String residue)
  {
    return ":" + residue;
  }

  @Override
  public StructureCommandI colourResidues(String atomSpec, Color colour)
  {
    // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/color.html
    String colourCode = getColourString(colour);

    return new StructureCommand("color " + atomSpec + " " + colourCode);
  }

  @Override
  public StructureCommandI focusView()
  {
    return FOCUS_VIEW;
  }

  /**
   * {@inheritDoc}
   * 
   * @return
   */
  @Override
  public int getModelStartNo()
  {
    return 1;
  }

  /**
   * Returns a viewer command to set the given residue attribute value on
   * residues specified by the AtomSpecModel, for example
   * 
   * <pre>
   * setattr #0/A:3-9,14-20,39-43 res jv_strand 'strand' create true
   * </pre>
   * 
   * @param attributeName
   * @param attributeValue
   * @param atomSpecModel
   * @return
   */
  @Override
  protected StructureCommandI setAttribute(String attributeName,
          String attributeValue, AtomSpecModel atomSpecModel)
  {
    StringBuilder sb = new StringBuilder(128);
    sb.append("setattr ")
            .append(getAtomSpec(atomSpecModel, AtomSpecType.RESIDUE_ONLY));
    sb.append(" res ").append(attributeName).append(" '")
            .append(attributeValue).append("'");
    sb.append(" create true");
    return new StructureCommand(sb.toString());
  }

  @Override
  public StructureCommandI openCommandFile(String path)
  {
    // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/open.html
    return new StructureCommand("open " + path);
  }

  @Override
  public StructureCommandI saveSession(String filepath)
  {
    // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/save.html
    // note ChimeraX will append ".cxs" to the filepath!
    return new StructureCommand("save " + filepath + " format session");
  }

  /**
   * Returns the range(s) formatted as a ChimeraX atomspec, for example
   * <p>
   * #1/A:2-20,30-40/B:10-20|#2/A:12-30
   * <p>
   * Note there is no need to explicitly exclude ALTLOC atoms when
   * {@code alphaOnly == true}, as this is the default behaviour of ChimeraX (a
   * change from Chimera)
   * 
   * @return
   */
  @Override
  public String getAtomSpec(AtomSpecModel atomSpec, AtomSpecType specType)
  {
    StringBuilder sb = new StringBuilder(128);
    boolean firstModel = true;
    for (String model : atomSpec.getModels())
    {
      if (!firstModel)
      {
        sb.append("|");
      }
      firstModel = false;
      appendModel(sb, model, atomSpec);
      if (specType == AtomSpecType.ALPHA)
      {
        sb.append("@CA");
      }
      if (specType == AtomSpecType.PHOSPHATE)
      {
        sb.append("@P");
      }
    }
    return sb.toString();
  }

  /**
   * A helper method to append an atomSpec string for atoms in the given model
   * 
   * @param sb
   * @param model
   * @param atomSpec
   */
  protected void appendModel(StringBuilder sb, String model,
          AtomSpecModel atomSpec)
  {
    sb.append("#").append(model);

    for (String chain : atomSpec.getChains(model))
    {
      boolean firstPositionForChain = true;
      sb.append("/").append(chain.trim()).append(":");
      List<int[]> rangeList = atomSpec.getRanges(model, chain);
      boolean first = true;
      for (int[] range : rangeList)
      {
        if (!first)
        {
          sb.append(",");
        }
        first = false;
        appendRange(sb, range[0], range[1], chain, firstPositionForChain,
                true);
      }
    }
  }

  @Override
  public List<StructureCommandI> showBackbone()
  {
    return Arrays.asList(SHOW_BACKBONE);
  }

  @Override
  public List<StructureCommandI> superposeStructures(AtomSpecModel ref,
          AtomSpecModel spec, AtomSpecType backbone)
  {
    /*
     * Form ChimeraX match command to match spec to ref
     * 
     * match #1/A:2-94 toAtoms #2/A:1-93
     * 
     * @see https://www.cgl.ucsf.edu/chimerax/docs/user/commands/align.html
     */
    StringBuilder cmd = new StringBuilder();
    String atomSpec = getAtomSpec(spec, backbone);
    String refSpec = getAtomSpec(ref, backbone);
    cmd.append("align ").append(atomSpec).append(" toAtoms ")
            .append(refSpec);

    /*
     * show superposed residues as ribbon, others as chain
     */
    cmd.append("; ribbon ");
    cmd.append(getAtomSpec(spec, AtomSpecType.RESIDUE_ONLY)).append("|");
    cmd.append(getAtomSpec(ref, AtomSpecType.RESIDUE_ONLY))
            .append("; view");

    return Arrays.asList(new StructureCommand(cmd.toString()));
  }

  @Override
  public StructureCommandI openSession(String filepath)
  {
    // https://www.cgl.ucsf.edu/chimerax/docs/user/commands/open.html#composite
    // this version of the command has no dependency on file extension
    return new StructureCommand("open " + filepath + " format session");
  }

  @Override
  public StructureCommandI closeViewer()
  {
    return CLOSE_CHIMERAX;
  }

  @Override
  public List<StructureCommandI> startNotifications(String uri)
  {
    List<StructureCommandI> cmds = new ArrayList<>();
    cmds.add(new StructureCommand(
            "info notify start models jalview prefix ModelChanged url "
                    + uri));
    cmds.add(new StructureCommand(
            "info notify start selection jalview prefix SelectionChanged url "
                    + uri));
    return cmds;
  }

  @Override
  public List<StructureCommandI> stopNotifications()
  {
    List<StructureCommandI> cmds = new ArrayList<>();
    cmds.add(STOP_NOTIFY_MODELS);
    cmds.add(STOP_NOTIFY_SELECTION);
    return cmds;
  }

  @Override
  public StructureCommandI getSelectedResidues()
  {
    return GET_SELECTION;
  }

  @Override
  public StructureCommandI listResidueAttributes()
  {
    return LIST_RESIDUE_ATTRIBUTES;
  }
}
