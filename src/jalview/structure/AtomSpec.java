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

/**
 * Java bean representing an atom in a PDB (or similar) structure model or
 * viewer
 * 
 * @author gmcarstairs
 *
 */
public class AtomSpec
{
  int modelNo;

  private String pdbFile;

  private String chain;

  private int pdbResNum;

  private int atomIndex;

  /**
   * Parses a Chimera atomspec e.g. #1:12.A to construct an AtomSpec model (with
   * null pdb file name)
   * 
   * <pre>
   * Chimera format: 
   *    #1.2:12-20.A     model 1, submodel 2, chain A, atoms 12-20
   * </pre>
   * 
   * @param spec
   * @return
   * @throw IllegalArgumentException if the spec cannot be parsed, or represents
   *        more than one residue
   * @see https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/midas/frameatom_spec.html
   */
  public static AtomSpec fromChimeraAtomspec(String spec)
  {
    int modelSeparatorPos = spec.indexOf(":");
    if (modelSeparatorPos == -1)
    {
      throw new IllegalArgumentException(spec);
    }

    int hashPos = spec.indexOf("#");
    if (hashPos == -1 && modelSeparatorPos != 0)
    {
      // # is missing but something precedes : - reject
      throw new IllegalArgumentException(spec);
    }

    String modelSubmodel = spec.substring(hashPos + 1, modelSeparatorPos);
    int modelId = 0;
    try
    {
      int subModelPos = modelSubmodel.indexOf(".");
      modelId = Integer.valueOf(
              subModelPos > 0 ? modelSubmodel.substring(0, subModelPos)
                      : modelSubmodel);
    } catch (NumberFormatException e)
    {
      // ignore, default to model 0
    }

    /*
     * now process what follows the model, either
     * Chimera:  atoms.chain
     * ChimeraX: chain:atoms
     */
    String atomsAndChain = spec.substring(modelSeparatorPos + 1);
    String[] tokens = atomsAndChain.split("\\.");
    String atoms = tokens.length == 1 ? atomsAndChain : (tokens[0]);
    int resNum = 0;
    try
    {
      resNum = Integer.parseInt(atoms);
    } catch (NumberFormatException e)
    {
      // could be a range e.g. #1:4-7.B
      throw new IllegalArgumentException(spec);
    }

    String chainId = tokens.length == 1 ? "" : (tokens[1]);

    return new AtomSpec(modelId, chainId, resNum, 0);
  }

  /**
   * Constructor
   * 
   * @param pdbFile
   * @param chain
   * @param resNo
   * @param atomNo
   */
  public AtomSpec(String pdbFile, String chain, int resNo, int atomNo)
  {
    this.pdbFile = pdbFile;
    this.chain = chain;
    this.pdbResNum = resNo;
    this.atomIndex = atomNo;
  }

  /**
   * Constructor
   * 
   * @param modelId
   * @param chainId
   * @param resNo
   * @param atomNo
   */
  public AtomSpec(int modelId, String chainId, int resNo, int atomNo)
  {
    this.modelNo = modelId;
    this.chain = chainId;
    this.pdbResNum = resNo;
    this.atomIndex = atomNo;
  }

  public String getPdbFile()
  {
    return pdbFile;
  }

  public String getChain()
  {
    return chain;
  }

  public int getPdbResNum()
  {
    return pdbResNum;
  }

  public int getAtomIndex()
  {
    return atomIndex;
  }

  public int getModelNumber()
  {
    return modelNo;
  }

  public void setPdbFile(String file)
  {
    pdbFile = file;
  }

  @Override
  public String toString()
  {
    return "pdbFile: " + pdbFile + ", chain: " + chain + ", res: "
            + pdbResNum + ", atom: " + atomIndex;
  }

  /**
   * Parses a ChimeraX atomspec to construct an AtomSpec model (with null pdb
   * file name)
   * 
   * <pre>
   * ChimeraX format:
   *    #1.2/A:12-20     model 1, submodel 2, chain A, atoms 12-20
   * </pre>
   * 
   * @param spec
   * @return
   * @throw IllegalArgumentException if the spec cannot be parsed, or represents
   *        more than one residue
   * @see http://rbvi.ucsf.edu/chimerax/docs/user/commands/atomspec.html
   */
  public static AtomSpec fromChimeraXAtomspec(String spec)
  {
    int modelSeparatorPos = spec.indexOf("/");
    if (modelSeparatorPos == -1)
    {
      throw new IllegalArgumentException(spec);
    }

    int hashPos = spec.indexOf("#");
    if (hashPos == -1 && modelSeparatorPos != 0)
    {
      // # is missing but something precedes : - reject
      throw new IllegalArgumentException(spec);
    }

    String modelSubmodel = spec.substring(hashPos + 1, modelSeparatorPos);
    int modelId = 0;
    try
    {
      int subModelPos = modelSubmodel.indexOf(".");
      modelId = Integer.valueOf(
              subModelPos > 0 ? modelSubmodel.substring(0, subModelPos)
                      : modelSubmodel);
    } catch (NumberFormatException e)
    {
      // ignore, default to model 0
    }

    /*
     * now process what follows the model, either
     * Chimera:  atoms.chain
     * ChimeraX: chain:atoms
     */
    String atomsAndChain = spec.substring(modelSeparatorPos + 1);
    String[] tokens = atomsAndChain.split("\\:");
    String atoms = tokens.length == 1 ? atomsAndChain : (tokens[1]);
    int resNum = 0;
    try
    {
      resNum = Integer.parseInt(atoms);
    } catch (NumberFormatException e)
    {
      // could be a range e.g. #1:4-7.B
      throw new IllegalArgumentException(spec);
    }

    String chainId = tokens.length == 1 ? "" : (tokens[0]);

    return new AtomSpec(modelId, chainId, resNum, 0);
  }
}
