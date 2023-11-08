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
package jalview.ext.jmol;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;
import jalview.renderer.seqfeatures.FeatureColourFinder;
import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsBase;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.util.Comparison;
import jalview.util.Platform;

/**
 * Routines for generating Jmol commands for Jalview/Jmol binding
 * 
 * @author JimP
 * 
 */
public class JmolCommands extends StructureCommandsBase
{
  private static final StructureCommand SHOW_BACKBONE = new StructureCommand(
          "select *; cartoons off; backbone");

  private static final StructureCommand FOCUS_VIEW = new StructureCommand(
          "zoom 0");

  private static final StructureCommand COLOUR_ALL_WHITE = new StructureCommand(
          "select *;color white;");

  private static final StructureCommandI COLOUR_BY_CHARGE = new StructureCommand(
          "select *;color white;select ASP,GLU;color red;"
                  + "select LYS,ARG;color blue;select CYS;color yellow");

  private static final StructureCommandI COLOUR_BY_CHAIN = new StructureCommand(
          "select *;color chain");

  private static final String PIPE = "|";

  private static final String HYPHEN = "-";

  private static final String COLON = ":";

  private static final String SLASH = "/";

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
   * Returns a string representation of the given colour suitable for inclusion
   * in Jmol commands
   * 
   * @param c
   * @return
   */
  protected String getColourString(Color c)
  {
    return c == null ? null
            : String.format("[%d,%d,%d]", c.getRed(), c.getGreen(),
                    c.getBlue());
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
  public List<StructureCommandI> colourByResidues(
          Map<String, Color> colours)
  {
    List<StructureCommandI> cmds = super.colourByResidues(colours);
    cmds.add(0, COLOUR_ALL_WHITE);
    return cmds;
  }

  @Override
  public StructureCommandI setBackgroundColour(Color col)
  {
    return new StructureCommand("background " + getColourString(col));
  }

  @Override
  public StructureCommandI focusView()
  {
    return FOCUS_VIEW;
  }

  @Override
  public List<StructureCommandI> showChains(List<String> toShow)
  {
    StringBuilder atomSpec = new StringBuilder(128);
    boolean first = true;
    for (String chain : toShow)
    {
      String[] tokens = chain.split(":");
      if (tokens.length == 2)
      {
        if (!first)
        {
          atomSpec.append(" or ");
        }
        first = false;
        atomSpec.append(":").append(tokens[1]).append(" /")
                .append(tokens[0]);
      }
    }

    String spec = atomSpec.toString();
    String command = "select *;restrict " + spec + ";cartoon;center "
            + spec;
    return Arrays.asList(new StructureCommand(command));
  }

  /**
   * Returns a command to superpose atoms in {@code atomSpec} to those in
   * {@code refAtoms}, restricted to alpha carbons only (Phosphorous for rna).
   * For example
   * 
   * <pre>
   * compare {2.1} {1.1} SUBSET {(*.CA | *.P) and conformation=1} 
   *         ATOMS {1-87:A}{2-54:A|61-94:A} ROTATE TRANSLATE 1.0;
   * </pre>
   * 
   * where {@code conformation=1} excludes ALTLOC atom locations, and 1.0 is the
   * time in seconds to animate the action. For this example, atoms in model 2
   * are moved towards atoms in model 1.
   * <p>
   * The two atomspecs should each be for one model only, but may have more than
   * one chain. The number of atoms specified should be the same for both
   * models, though if not, Jmol may make a 'best effort' at superposition.
   * 
   * @see https://chemapps.stolaf.edu/jmol/docs/#compare
   */
  @Override
  public List<StructureCommandI> superposeStructures(AtomSpecModel refAtoms,
          AtomSpecModel atomSpec, AtomSpecType backbone)
  {
    StringBuilder sb = new StringBuilder(64);
    String refModel = refAtoms.getModels().iterator().next();
    String model2 = atomSpec.getModels().iterator().next();
    sb.append(String.format("compare {%s.1} {%s.1}", model2, refModel));
    sb.append(" SUBSET {(*.CA | *.P) and conformation=1} ATOMS {");

    /*
     * command examples don't include modelspec with atoms, getAtomSpec does;
     * it works, so leave it as it is for simplicity
     */
    sb.append(getAtomSpec(atomSpec, backbone)).append("}{");
    sb.append(getAtomSpec(refAtoms, backbone)).append("}");
    sb.append(" ROTATE TRANSLATE ");
    sb.append(getCommandSeparator());

    /*
     * show residues used for superposition as ribbon
     */
    sb.append("select ")
            .append(getAtomSpec(atomSpec, AtomSpecType.RESIDUE_ONLY))
            .append("|");
    sb.append(getAtomSpec(refAtoms, AtomSpecType.RESIDUE_ONLY))
            .append(getCommandSeparator()).append("cartoons");
    return Arrays.asList(new StructureCommand(sb.toString()));
  }

  @Override
  public List<StructureCommandI> centerViewOn(List<AtomSpecModel> residues)
  {
    StringBuilder sb = new StringBuilder(64);
    sb.append("center ");
    for (AtomSpecModel ranges : residues)
    {
      if (sb.length() > 9)
      {
        sb.append(" or ");
      }
      sb.append(getAtomSpec(ranges, AtomSpecType.RESIDUE_ONLY));
    }
    return Arrays.asList(new StructureCommand(sb.toString()));
  }

  @Override
  public StructureCommandI openCommandFile(String path)
  {
    /*
     * https://chemapps.stolaf.edu/jmol/docs/#script
     * not currently used in Jalview
     */
    return new StructureCommand("script " + path);
  }

  @Override
  public StructureCommandI saveSession(String filepath)
  {
    /*
     * https://chemapps.stolaf.edu/jmol/docs/#writemodel
     */
    return new StructureCommand("write STATE \"" + filepath + "\"");
  }

  @Override
  protected StructureCommandI colourResidues(String atomSpec, Color colour)
  {
    StringBuilder sb = new StringBuilder(atomSpec.length() + 20);
    sb.append("select ").append(atomSpec).append(getCommandSeparator())
            .append("color").append(getColourString(colour));
    return new StructureCommand(sb.toString());
  }

  @Override
  protected String getResidueSpec(String residue)
  {
    return residue;
  }

  /**
   * Generates a Jmol atomspec string like
   * 
   * <pre>
   * 2-5:A/1.1,8:A/1.1,5-10:B/2.1
   * </pre>
   * 
   * Parameter {@code alphaOnly} is not used here - this restriction is made by
   * a separate clause in the {@code compare} (superposition) command.
   */
  @Override
  public String getAtomSpec(AtomSpecModel model, AtomSpecType specType)
  {
    StringBuilder sb = new StringBuilder(128);

    boolean first = true;
    for (String modelNo : model.getModels())
    {
      for (String chain : model.getChains(modelNo))
      {
        for (int[] range : model.getRanges(modelNo, chain))
        {
          if (!first)
          {
            sb.append(PIPE);
          }
          first = false;
          if (range[0] == range[1])
          {
            sb.append(range[0]);
          }
          else
          {
            sb.append(range[0]).append(HYPHEN).append(range[1]);
          }
          sb.append(COLON).append(chain.trim()).append(SLASH);
          sb.append(String.valueOf(modelNo)).append(".1");
        }
      }
    }

    return sb.toString();
  }

  @Override
  public List<StructureCommandI> showBackbone()
  {
    return Arrays.asList(SHOW_BACKBONE);
  }

  @Override
  public StructureCommandI loadFile(String file)
  {
    // https://chemapps.stolaf.edu/jmol/docs/#loadfiles
    return new StructureCommand(
            "load FILES \"" + Platform.escapeBackslashes(file) + "\"");
  }

  /**
   * Obsolete method, only referenced from
   * jalview.javascript.MouseOverStructureListener
   * 
   * @param ssm
   * @param files
   * @param sequence
   * @param sr
   * @param viewPanel
   * @return
   */
  @Deprecated
  public String[] colourBySequence(StructureSelectionManager ssm,
          String[] files, SequenceI[][] sequence, SequenceRenderer sr,
          AlignmentViewPanel viewPanel)
  {
    // TODO delete method

    FeatureRenderer fr = viewPanel.getFeatureRenderer();
    FeatureColourFinder finder = new FeatureColourFinder(fr);
    AlignViewportI viewport = viewPanel.getAlignViewport();
    HiddenColumns cs = viewport.getAlignment().getHiddenColumns();
    AlignmentI al = viewport.getAlignment();
    List<String> cset = new ArrayList<>();

    for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
    {
      StructureMapping[] mapping = ssm.getMapping(files[pdbfnum]);
      StringBuilder command = new StringBuilder(128);
      List<String> str = new ArrayList<>();

      if (mapping == null || mapping.length < 1)
      {
        continue;
      }

      for (int s = 0; s < sequence[pdbfnum].length; s++)
      {
        for (int sp, m = 0; m < mapping.length; m++)
        {
          if (mapping[m].getSequence() == sequence[pdbfnum][s]
                  && (sp = al.findIndex(sequence[pdbfnum][s])) > -1)
          {
            int lastPos = StructureMapping.UNASSIGNED_VALUE;
            SequenceI asp = al.getSequenceAt(sp);
            for (int r = 0; r < asp.getLength(); r++)
            {
              // no mapping to gaps in sequence
              if (Comparison.isGap(asp.getCharAt(r)))
              {
                continue;
              }
              int pos = mapping[m].getPDBResNum(asp.findPosition(r));

              if (pos == lastPos)
              {
                continue;
              }
              if (pos == StructureMapping.UNASSIGNED_VALUE)
              {
                // terminate current colour op
                if (command.length() > 0
                        && command.charAt(command.length() - 1) != ';')
                {
                  command.append(";");
                }
                // reset lastPos
                lastPos = StructureMapping.UNASSIGNED_VALUE;
                continue;
              }

              lastPos = pos;

              Color col = sr.getResidueColour(sequence[pdbfnum][s], r,
                      finder);

              /*
               * shade hidden regions darker
               */
              if (!cs.isVisible(r))
              {
                col = Color.GRAY;
              }

              String newSelcom = (mapping[m].getChain() != " "
                      ? ":" + mapping[m].getChain()
                      : "") + "/" + (pdbfnum + 1) + ".1" + ";color"
                      + getColourString(col);
              if (command.length() > newSelcom.length() && command
                      .substring(command.length() - newSelcom.length())
                      .equals(newSelcom))
              {
                command = JmolCommands.condenseCommand(command, pos);
                continue;
              }
              // TODO: deal with case when buffer is too large for Jmol to parse
              // - execute command and flush

              if (command.length() > 0
                      && command.charAt(command.length() - 1) != ';')
              {
                command.append(";");
              }

              if (command.length() > 51200)
              {
                // add another chunk
                str.add(command.toString());
                command.setLength(0);
              }
              command.append("select " + pos);
              command.append(newSelcom);
            }
            // break;
          }
        }
      }
      {
        // add final chunk
        str.add(command.toString());
        command.setLength(0);
      }
      cset.addAll(str);

    }
    return cset.toArray(new String[cset.size()]);
  }

  /**
   * Helper method
   * 
   * @param command
   * @param pos
   * @return
   */
  @Deprecated
  private static StringBuilder condenseCommand(StringBuilder command,
          int pos)
  {

    // work back to last 'select'
    int p = command.length(), q = p;
    do
    {
      p -= 6;
      if (p < 1)
      {
        p = 0;
      }
      ;
    } while ((q = command.indexOf("select", p)) == -1 && p > 0);

    StringBuilder sb = new StringBuilder(command.substring(0, q + 7));

    command = command.delete(0, q + 7);

    String start;

    if (command.indexOf("-") > -1)
    {
      start = command.substring(0, command.indexOf("-"));
    }
    else
    {
      start = command.substring(0, command.indexOf(":"));
    }

    sb.append(start + "-" + pos + command.substring(command.indexOf(":")));

    return sb;
  }

  @Override
  public StructureCommandI openSession(String filepath)
  {
    return loadFile(filepath);
  }

  @Override
  public StructureCommandI closeViewer()
  {
    return null; // not an external viewer
  }
}
