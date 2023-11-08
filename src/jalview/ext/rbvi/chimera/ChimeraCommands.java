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
import java.util.Locale;
import java.util.Map;

import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsBase;
import jalview.util.ColorUtils;

/**
 * Routines for generating Chimera commands for Jalview/Chimera binding
 * 
 * @author JimP
 * 
 */
public class ChimeraCommands extends StructureCommandsBase
{
  // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/focus.html
  private static final StructureCommand FOCUS_VIEW = new StructureCommand(
          "focus");

  // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/listen.html#listresattr
  private static final StructureCommand LIST_RESIDUE_ATTRIBUTES = new StructureCommand(
          "list resattr");

  // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/stop.html
  private static final StructureCommand CLOSE_CHIMERA = new StructureCommand(
          "stop really");

  // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/listen.html
  private static final StructureCommand STOP_NOTIFY_SELECTION = new StructureCommand(
          "listen stop selection");

  private static final StructureCommand STOP_NOTIFY_MODELS = new StructureCommand(
          "listen stop models");

  // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/listen.html#listselection
  private static final StructureCommand GET_SELECTION = new StructureCommand(
          "list selection level residue");

  private static final StructureCommand SHOW_BACKBONE = new StructureCommand(
          "~display all;~ribbon;chain @CA|P");

  private static final StructureCommandI COLOUR_BY_CHARGE = new StructureCommand(
          "color white;color red ::ASP,GLU;color blue ::LYS,ARG;color yellow ::CYS");

  // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/rainbow.html
  private static final StructureCommandI COLOUR_BY_CHAIN = new StructureCommand(
          "rainbow chain");

  // Chimera clause to exclude alternate locations in atom selection
  private static final String NO_ALTLOCS = "&~@.B-Z&~@.2-9";

  @Override
  public StructureCommandI colourResidues(String atomSpec, Color colour)
  {
    // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/color.html
    String colourCode = getColourString(colour);
    return new StructureCommand("color " + colourCode + " " + atomSpec);
  }

  /**
   * Returns a colour formatted suitable for use in viewer command syntax
   * 
   * @param colour
   * @return
   */
  protected String getColourString(Color colour)
  {
    return ColorUtils.toTkCode(colour);
  }

  /**
   * Traverse the map of features/values/models/chains/positions to construct a
   * list of 'setattr' commands (one per distinct feature type and value).
   * <p>
   * The format of each command is
   * 
   * <pre>
   * <blockquote> setattr r <featureName> " " #modelnumber:range.chain 
   * e.g. setattr r jv_chain &lt;value&gt; #0:2.B,4.B,9-12.B|#1:1.A,2-6.A,...
   * </blockquote>
   * </pre>
   * 
   * @param featureMap
   * @return
   */
  @Override
  public List<StructureCommandI> setAttributes(
          Map<String, Map<Object, AtomSpecModel>> featureMap)
  {
    List<StructureCommandI> commands = new ArrayList<>();
    for (String featureType : featureMap.keySet())
    {
      String attributeName = makeAttributeName(featureType);

      /*
       * clear down existing attributes for this feature
       */
      // 'problem' - sets attribute to None on all residues - overkill?
      // commands.add("~setattr r " + attributeName + " :*");

      Map<Object, AtomSpecModel> values = featureMap.get(featureType);
      for (Object value : values.keySet())
      {
        /*
         * for each distinct value recorded for this feature type,
         * add a command to set the attribute on the mapped residues
         * Put values in single quotes, encoding any embedded single quotes
         */
        AtomSpecModel atomSpecModel = values.get(value);
        String featureValue = value.toString();
        featureValue = featureValue.replaceAll("\\'", "&#39;");
        StructureCommandI cmd = setAttribute(attributeName, featureValue,
                atomSpecModel);
        commands.add(cmd);
      }
    }

    return commands;
  }

  /**
   * Returns a viewer command to set the given residue attribute value on
   * residues specified by the AtomSpecModel, for example
   * 
   * <pre>
   * setatr res jv_chain 'primary' #1:12-34,48-55.B
   * </pre>
   * 
   * @param attributeName
   * @param attributeValue
   * @param atomSpecModel
   * @return
   */
  protected StructureCommandI setAttribute(String attributeName,
          String attributeValue, AtomSpecModel atomSpecModel)
  {
    StringBuilder sb = new StringBuilder(128);
    sb.append("setattr res ").append(attributeName).append(" '")
            .append(attributeValue).append("' ");
    sb.append(getAtomSpec(atomSpecModel, AtomSpecType.RESIDUE_ONLY));
    return new StructureCommand(sb.toString());
  }

  /**
   * Makes a prefixed and valid Chimera attribute name. A jv_ prefix is applied
   * for a 'Jalview' namespace, and any non-alphanumeric character is converted
   * to an underscore.
   * 
   * @param featureType
   * @return
   * @see https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/setattr.html
   */
  @Override
  protected String makeAttributeName(String featureType)
  {
    String attName = super.makeAttributeName(featureType);

    /*
     * Chimera treats an attribute name ending in 'color' as colour-valued;
     * Jalview doesn't, so prevent this by appending an underscore
     */
    if (attName.toUpperCase(Locale.ROOT).endsWith("COLOR"))
    {
      attName += "_";
    }

    return attName;
  }

  @Override
  public StructureCommandI colourByChain()
  {
    return COLOUR_BY_CHAIN;
  }

  @Override
  public List<StructureCommandI> colourByCharge()
  {
    return Arrays.asList(COLOUR_BY_CHARGE);
  }

  @Override
  public String getResidueSpec(String residue)
  {
    return "::" + residue;
  }

  @Override
  public StructureCommandI setBackgroundColour(Color col)
  {
    // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/set.html#bgcolor
    return new StructureCommand("set bgColor " + ColorUtils.toTkCode(col));
  }

  @Override
  public StructureCommandI focusView()
  {
    return FOCUS_VIEW;
  }

  @Override
  public List<StructureCommandI> showChains(List<String> toShow)
  {
    /*
     * Construct a chimera command like
     * 
     * ~display #*;~ribbon #*;ribbon :.A,:.B
     */
    StringBuilder cmd = new StringBuilder(64);
    boolean first = true;
    for (String chain : toShow)
    {
      String[] tokens = chain.split(":");
      if (tokens.length == 2)
      {
        String showChainCmd = tokens[0] + ":." + tokens[1];
        if (!first)
        {
          cmd.append(",");
        }
        cmd.append(showChainCmd);
        first = false;
      }
    }

    /*
     * could append ";focus" to this command to resize the display to fill the
     * window, but it looks more helpful not to (easier to relate chains to the
     * whole)
     */
    final String command = "~display #*; ~ribbon #*; ribbon :"
            + cmd.toString();
    return Arrays.asList(new StructureCommand(command));
  }

  @Override
  public List<StructureCommandI> superposeStructures(AtomSpecModel ref,
          AtomSpecModel spec, AtomSpecType backbone)
  {
    /*
     * Form Chimera match command to match spec to ref
     * (the first set of atoms are moved on to the second)
     * 
     * match #1:1-30.B,81-100.B@CA #0:21-40.A,61-90.A@CA
     * 
     * @see https://www.cgl.ucsf.edu/chimera/docs/UsersGuide/midas/match.html
     */
    StringBuilder cmd = new StringBuilder();
    String atomSpecAlphaOnly = getAtomSpec(spec, backbone);
    String refSpecAlphaOnly = getAtomSpec(ref, backbone);
    cmd.append("match ").append(atomSpecAlphaOnly).append(" ")
            .append(refSpecAlphaOnly);

    /*
     * show superposed residues as ribbon
     */
    String atomSpec = getAtomSpec(spec, AtomSpecType.RESIDUE_ONLY);
    String refSpec = getAtomSpec(ref, AtomSpecType.RESIDUE_ONLY);
    cmd.append("; ribbon ");
    cmd.append(atomSpec).append("|").append(refSpec).append("; focus");

    return Arrays.asList(new StructureCommand(cmd.toString()));
  }

  @Override
  public StructureCommandI openCommandFile(String path)
  {
    // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/filetypes.html
    return new StructureCommand("open cmd:" + path);
  }

  @Override
  public StructureCommandI saveSession(String filepath)
  {
    // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/save.html
    return new StructureCommand("save " + filepath);
  }

  /**
   * Returns the range(s) modelled by {@code atomSpec} formatted as a Chimera
   * atomspec string, e.g.
   * 
   * <pre>
   * #0:15.A,28.A,54.A,70-72.A|#1:2.A,6.A,11.A,13-14.A
   * </pre>
   * 
   * where
   * <ul>
   * <li>#0 is a model number</li>
   * <li>15 or 70-72 is a residue number, or range of residue numbers</li>
   * <li>.A is a chain identifier</li>
   * <li>residue ranges are separated by comma</li>
   * <li>atomspecs for distinct models are separated by | (or)</li>
   * </ul>
   * 
   * <pre>
   * 
   * @param model
   * @param specType
   * @return
   * @see https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/frameatom_spec.html
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
      appendModel(sb, model, atomSpec, specType);
    }
    return sb.toString();
  }

  /**
   * A helper method to append an atomSpec string for atoms in the given model
   * 
   * @param sb
   * @param model
   * @param atomSpec
   * @param alphaOnly
   */
  protected void appendModel(StringBuilder sb, String model,
          AtomSpecModel atomSpec, AtomSpecType specType)
  {
    sb.append("#").append(model).append(":");

    boolean firstPositionForModel = true;

    for (String chain : atomSpec.getChains(model))
    {
      chain = " ".equals(chain) ? chain : chain.trim();

      List<int[]> rangeList = atomSpec.getRanges(model, chain);
      for (int[] range : rangeList)
      {
        appendRange(sb, range[0], range[1], chain, firstPositionForModel,
                false);
        firstPositionForModel = false;
      }
    }
    if (specType == AtomSpecType.ALPHA)
    {
      /*
       * restrict to alpha carbon, no alternative locations
       * (needed to ensuring matching atom counts for superposition)
       */
      sb.append("@CA").append(NO_ALTLOCS);
    }
    if (specType == AtomSpecType.PHOSPHATE)
    {
      sb.append("@P").append(NO_ALTLOCS);
    }
  }

  @Override
  public List<StructureCommandI> showBackbone()
  {
    return Arrays.asList(SHOW_BACKBONE);
  }

  @Override
  public StructureCommandI loadFile(String file)
  {
    return new StructureCommand("open " + file);
  }

  @Override
  public StructureCommandI openSession(String filepath)
  {
    // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/filetypes.html
    // this version of the command has no dependency on file extension
    return new StructureCommand("open chimera:" + filepath);
  }

  @Override
  public StructureCommandI closeViewer()
  {
    return CLOSE_CHIMERA;
  }

  @Override
  public List<StructureCommandI> startNotifications(String uri)
  {
    // https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/listen.html
    List<StructureCommandI> cmds = new ArrayList<>();
    cmds.add(new StructureCommand("listen start models url " + uri));
    cmds.add(new StructureCommand(
            "listen start select prefix SelectionChanged url " + uri));
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

  @Override
  public StructureCommandI getResidueAttributes(String attName)
  {
    // this alternative command
    // list residues spec ':*/attName' attr attName
    // doesn't report 'None' values (which is good), but
    // fails for 'average.bfactor' (which is bad):
    return new StructureCommand("list residues attr '" + attName + "'");
  }

  @Override
  public List<StructureCommandI> centerViewOn(List<AtomSpecModel> residues)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
