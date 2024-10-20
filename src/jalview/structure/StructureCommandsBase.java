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
package jalview.structure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A base class holding methods useful to all classes that implement commands
 * for structure viewers
 * 
 * @author gmcarstairs
 *
 */
public abstract class StructureCommandsBase implements StructureCommandsI
{
  public static final String NAMESPACE_PREFIX = "jv_";

  private static final String CMD_SEPARATOR = ";";

  /**
   * Returns something that separates concatenated commands
   * 
   * @return
   */
  protected String getCommandSeparator()
  {
    return CMD_SEPARATOR;
  }

  /**
   * Returns the lowest model number used by the structure viewer
   * 
   * @return
   */
  @Override
  public int getModelStartNo()
  {
    return 0;
  }

  /**
   * Helper method to add one contiguous range to the AtomSpec model for the
   * given value (creating the model if necessary). As used by Jalview,
   * {@code value} is
   * <ul>
   * <li>a colour, when building a 'colour structure by sequence' command</li>
   * <li>a feature value, when building a 'set Chimera attributes from features'
   * command</li>
   * </ul>
   * 
   * @param map
   * @param value
   * @param model
   * @param startPos
   * @param endPos
   * @param chain
   */
  public static final void addAtomSpecRange(Map<Object, AtomSpecModel> map,
          Object value, String model, int startPos, int endPos,
          String chain)
  {
    /*
     * Get/initialize map of data for the colour
     */
    AtomSpecModel atomSpec = map.get(value);
    if (atomSpec == null)
    {
      atomSpec = new AtomSpecModel();
      map.put(value, atomSpec);
    }

    atomSpec.addRange(model, startPos, endPos, chain);
  }

  /**
   * Makes a structure viewer attribute name for a Jalview feature type by
   * prefixing it with "jv_", and replacing any non-alphanumeric characters with
   * an underscore
   * 
   * @param featureType
   * @return
   */
  protected String makeAttributeName(String featureType)
  {
    StringBuilder sb = new StringBuilder();
    if (featureType != null)
    {
      for (char c : featureType.toCharArray())
      {
        sb.append(Character.isLetterOrDigit(c) ? c : '_');
      }
    }
    String attName = NAMESPACE_PREFIX + sb.toString();
    return attName;
  }

  /**
   * Traverse the map of colours/models/chains/positions to construct a list of
   * 'color' commands (one per distinct colour used). The format of each command
   * is specific to the structure viewer.
   * <p>
   * The default implementation returns a single command containing one command
   * per colour, concatenated.
   * 
   * @param colourMap
   * @return
   */
  @Override
  public List<StructureCommandI> colourBySequence(
          Map<Object, AtomSpecModel> colourMap)
  {
    List<StructureCommandI> commands = new ArrayList<>();
    StringBuilder sb = new StringBuilder(colourMap.size() * 20);
    boolean first = true;
    for (Object key : colourMap.keySet())
    {
      Color colour = (Color) key;
      final AtomSpecModel colourData = colourMap.get(colour);
      StructureCommandI command = getColourCommand(colourData, colour);
      if (!first)
      {
        sb.append(getCommandSeparator());
      }
      first = false;
      sb.append(command.getCommand());
    }

    commands.add(new StructureCommand(sb.toString()));
    return commands;
  }

  /**
   * Returns a command to colour the atoms represented by {@code atomSpecModel}
   * with the colour specified by {@code colourCode}.
   * 
   * @param atomSpecModel
   * @param colour
   * @return
   */
  protected StructureCommandI getColourCommand(AtomSpecModel atomSpecModel,
          Color colour)
  {
    String atomSpec = getAtomSpec(atomSpecModel, AtomSpecType.RESIDUE_ONLY);
    return colourResidues(atomSpec, colour);
  }

  /**
   * Returns a command to colour the atoms described (in viewer command syntax)
   * by {@code atomSpec} with the colour specified by {@code colourCode}
   * 
   * @param atomSpec
   * @param colour
   * @return
   */
  protected abstract StructureCommandI colourResidues(String atomSpec,
          Color colour);

  @Override
  public List<StructureCommandI> colourByResidues(
          Map<String, Color> colours)
  {
    List<StructureCommandI> commands = new ArrayList<>();
    for (Entry<String, Color> entry : colours.entrySet())
    {
      commands.add(colourResidue(entry.getKey(), entry.getValue()));
    }
    return commands;
  }

  private StructureCommandI colourResidue(String resName, Color col)
  {
    String atomSpec = getResidueSpec(resName);
    return colourResidues(atomSpec, col);
  }

  /**
   * Helper method to append one start-end range to an atomspec string
   * 
   * @param sb
   * @param start
   * @param end
   * @param chain
   * @param firstPositionForModel
   */
  protected void appendRange(StringBuilder sb, int start, int end,
          String chain, boolean firstPositionForModel, boolean isChimeraX)
  {
    if (!firstPositionForModel)
    {
      sb.append(",");
    }
    if (end == start)
    {
      sb.append(start);
    }
    else
    {
      sb.append(start).append("-").append(end);
    }

    if (!isChimeraX)
    {
      sb.append(".");
      if (!" ".equals(chain))
      {
        sb.append(chain);
      }
    }
  }

  /**
   * Returns the atom specifier meaning all occurrences of the given residue
   * 
   * @param residue
   * @return
   */
  protected abstract String getResidueSpec(String residue);

  @Override
  public List<StructureCommandI> setAttributes(
          Map<String, Map<Object, AtomSpecModel>> featureValues)
  {
    // default does nothing, override where this is implemented
    return null;
  }

  @Override
  public List<StructureCommandI> startNotifications(String uri)
  {
    return null;
  }

  @Override
  public List<StructureCommandI> stopNotifications()
  {
    return null;
  }

  @Override
  public StructureCommandI getSelectedResidues()
  {
    return null;
  }

  @Override
  public StructureCommandI listResidueAttributes()
  {
    return null;
  }

  @Override
  public StructureCommandI getResidueAttributes(String attName)
  {
    return null;
  }
}
