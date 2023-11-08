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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsBase;

/**
 * A class that generates commands to send to PyMol over its XML-RPC interface.
 * <p>
 * Note that because the xml-rpc interface can only accept one command at a
 * time, we can't concatenate commands, and must instead form and send them
 * individually.
 * 
 * @see https://pymolwiki.org/index.php/Category:Commands
 * @see https://pymolwiki.org/index.php/RPC
 */
public class PymolCommands extends StructureCommandsBase
{
  // https://pymol.org/dokuwiki/doku.php?id=command:zoom
  // not currently documented on
  // https://pymolwiki.org/index.php/Category:Commands
  private static final StructureCommand FOCUS_VIEW = new StructureCommand(
          "zoom");

  // https://pymolwiki.org/index.php/Quit
  private static final StructureCommand CLOSE_PYMOL = new StructureCommand(
          "quit");

  // not currently documented on
  // https://pymolwiki.org/index.php/Category:Commands
  private static final StructureCommand COLOUR_BY_CHAIN = new StructureCommand(
          "spectrum", "chain");

  private static final List<StructureCommandI> COLOR_BY_CHARGE = Arrays
          .asList(new StructureCommand("color", "white", "*"),
                  new StructureCommand("color", "red", "resn ASP resn GLU"),
                  new StructureCommand("color", "blue",
                          "resn LYS resn ARG"),
                  new StructureCommand("color", "yellow", "resn CYS"));

  private static final List<StructureCommandI> SHOW_BACKBONE = Arrays
          .asList(new StructureCommand("hide", "everything"),
                  new StructureCommand("show", "ribbon"));

  @Override
  public StructureCommandI colourByChain()
  {
    return COLOUR_BY_CHAIN;
  }

  @Override
  public List<StructureCommandI> colourByCharge()
  {
    return COLOR_BY_CHARGE;
  }

  @Override
  public StructureCommandI setBackgroundColour(Color col)
  {
    // https://pymolwiki.org/index.php/Bg_Color
    return new StructureCommand("bg_color", getColourString(col));
  }

  /**
   * Returns a colour formatted suitable for use in viewer command syntax. For
   * example, red is {@code "0xff0000"}.
   * 
   * @param c
   * @return
   */
  protected String getColourString(Color c)
  {
    return String.format("0x%02x%02x%02x", c.getRed(), c.getGreen(),
            c.getBlue());
  }

  @Override
  public StructureCommandI focusView()
  {
    return FOCUS_VIEW;
  }

  @Override
  public List<StructureCommandI> showChains(List<String> toShow)
  {
    // https://pymolwiki.org/index.php/Show
    List<StructureCommandI> commands = new ArrayList<>();
    commands.add(new StructureCommand("hide", "everything"));
    commands.add(new StructureCommand("show", "lines"));
    StringBuilder chains = new StringBuilder();
    for (String chain : toShow)
    {
      chains.append(" chain ").append(chain);
    }
    commands.add(
            new StructureCommand("show", "cartoon", chains.toString()));
    return commands;
  }

  @Override
  public List<StructureCommandI> superposeStructures(AtomSpecModel refAtoms,
          AtomSpecModel atomSpec, AtomSpecType specType)
  {

    // https://pymolwiki.org/index.php/Super
    List<StructureCommandI> commands = new ArrayList<>();
    String refAtomsAlphaOnly = "(" + getAtomSpec(refAtoms, specType)
            + " and (altloc '' or altloc 'a'))";
    String atomSpec2AlphaOnly = "(" + getAtomSpec(atomSpec, specType)
            + " and (altloc '' or altloc 'a'))";
    // pair_fit mobile -> reference
    // crashes when undo is enabled on 2.5.2 (incentive)
    commands.add(new StructureCommand("undo_disable"));
    commands.add(new StructureCommand("pair_fit", atomSpec2AlphaOnly,
            refAtomsAlphaOnly));
    commands.add(new StructureCommand("undo_enable"));

    /*
     * and show superposed residues as cartoon
     */
    String refAtomsAll = getAtomSpec(refAtoms, AtomSpecType.RESIDUE_ONLY);
    String atomSpec2All = getAtomSpec(atomSpec, AtomSpecType.RESIDUE_ONLY);
    commands.add(new StructureCommand("show", "cartoon",
            refAtomsAll + " " + atomSpec2All));

    return commands;
  }

  @Override
  public StructureCommandI openCommandFile(String path)
  {
    // https://pymolwiki.org/index.php/Run
    return new StructureCommand("run", path); // should be .pml
  }

  @Override
  public StructureCommandI saveSession(String filepath)
  {
    // https://pymolwiki.org/index.php/Save#EXAMPLES
    return new StructureCommand("save", filepath); // should be .pse
  }

  /**
   * Returns a selection string in PyMOL 'selection macro' format:
   * 
   * <pre>
   * modelId// chain/residues/
   * </pre>
   * 
   * If more than one chain, makes a selection expression for each, and they are
   * separated by spaces.
   * 
   * @see https://pymolwiki.org/index.php/Selection_Macros
   */
  @Override
  public String getAtomSpec(AtomSpecModel model, AtomSpecType specType)
  {
    StringBuilder sb = new StringBuilder(64);
    boolean first = true;
    for (String modelId : model.getModels())
    {
      for (String chain : model.getChains(modelId))
      {
        if (!first)
        {
          sb.append(" ");
        }
        first = false;
        List<int[]> rangeList = model.getRanges(modelId, chain);
        chain = chain.trim();
        sb.append(modelId).append("//").append(chain).append("/");
        boolean firstRange = true;
        for (int[] range : rangeList)
        {
          if (!firstRange)
          {
            sb.append("+");
          }
          firstRange = false;
          sb.append(String.valueOf(range[0]));
          if (range[0] != range[1])
          {
            sb.append("-").append(String.valueOf(range[1]));
          }
        }
        sb.append("/");
        if (specType == AtomSpecType.ALPHA)
        {
          sb.append("CA");
        }
        if (specType == AtomSpecType.PHOSPHATE)
        {
          sb.append("P");
        }
      }
    }
    return sb.toString();
  }

  @Override
  public List<StructureCommandI> showBackbone()
  {
    return SHOW_BACKBONE;
  }

  @Override
  protected StructureCommandI colourResidues(String atomSpec, Color colour)
  {
    // https://pymolwiki.org/index.php/Color
    return new StructureCommand("color", getColourString(colour), atomSpec);
  }

  @Override
  protected String getResidueSpec(String residue)
  {
    // https://pymolwiki.org/index.php/Selection_Algebra
    return "resn " + residue;
  }

  @Override
  public StructureCommandI loadFile(String file)
  {
    return new StructureCommand("load", file);
  }

  /**
   * Overrides the default implementation (which generates concatenated
   * commands) to generate one per colour (because the XML-RPC interface to
   * PyMOL only accepts one command at a time)
   * 
   * @param colourMap
   * @return
   */
  @Override
  public List<StructureCommandI> colourBySequence(
          Map<Object, AtomSpecModel> colourMap)
  {
    List<StructureCommandI> commands = new ArrayList<>();
    for (Object key : colourMap.keySet())
    {
      Color colour = (Color) key;
      final AtomSpecModel colourData = colourMap.get(colour);
      commands.add(getColourCommand(colourData, colour));
    }

    return commands;
  }

  /**
   * Returns a viewer command to set the given atom property value on atoms
   * specified by the AtomSpecModel, for example
   * 
   * <pre>
   * iterate 4zho//B/12-34,48-55/CA,jv_chain='primary'
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
    sb.append("p.").append(attributeName).append("='")
            .append(attributeValue).append("'");
    String atomSpec = getAtomSpec(atomSpecModel, AtomSpecType.RESIDUE_ONLY);
    return new StructureCommand("iterate", atomSpec, sb.toString());
  }

  /**
   * Traverse the map of features/values/models/chains/positions to construct a
   * list of 'set property' commands (one per distinct feature type and value).
   * The values are stored in the 'p' dictionary of user-defined properties of
   * each atom.
   * <p>
   * The format of each command is
   * 
   * <pre>
   * <blockquote> iterate atomspec, p.featureName='value' 
   * e.g. iterate 4zho//A/23,28-29/CA, p.jv_Metal='Fe'
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
       * todo: clear down existing attributes for this feature?
       */
      // commands.add(new StructureCommand("iterate", "all",
      // "p."+attributeName+"='None'"); //?

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

  @Override
  public StructureCommandI openSession(String filepath)
  {
    // https://pymolwiki.org/index.php/Load
    // this version of the command has no dependency on file extension
    return new StructureCommand("load", filepath, "", "0", "pse");
  }

  @Override
  public StructureCommandI closeViewer()
  {
    // https://pymolwiki.org/index.php/Quit
    return CLOSE_PYMOL;
  }

  @Override
  public List<StructureCommandI> centerViewOn(List<AtomSpecModel> residues)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
