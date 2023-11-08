/* vim: set ts=2: */
/**
 * Copyright (c) 2006 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package ext.edu.ucsf.rbvi.strucviz2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import ext.edu.ucsf.rbvi.strucviz2.StructureManager.ModelType;

/**
 * This class provides the implementation for the ChimeraModel, ChimeraChain,
 * and ChimeraResidue objects
 * 
 * @author scooter
 * 
 */
public class ChimeraModel implements ChimeraStructuralObject
{

  private String name; // The name of this model

  private ModelType type; // The type of the model

  private int modelNumber; // The model number

  private int subModelNumber; // The sub-model number

  private Color modelColor = null; // The color of this model (from Chimera)

  private Object userData = null; // User data associated with this model

  private boolean selected = false; // The selected state of this model

  private TreeMap<String, ChimeraChain> chainMap; // The list of chains

  // private TreeMap<String, ChimeraResidue> residueMap; // The list of residues
  private HashSet<ChimeraResidue> funcResidues; // List of functional residues

  /**
   * Constructor to create a model
   * 
   * @param name
   *          the name of this model
   * @param color
   *          the model Color
   * @param modelNumber
   *          the model number
   * @param subModelNumber
   *          the sub-model number
   */
  public ChimeraModel(String name, ModelType type, int modelNumber,
          int subModelNumber)
  {
    this.name = name;
    this.type = type;
    this.modelNumber = modelNumber;
    this.subModelNumber = subModelNumber;

    this.chainMap = new TreeMap<String, ChimeraChain>();
    this.funcResidues = new HashSet<ChimeraResidue>();
  }

  /**
   * Constructor to create a model from the Chimera input line
   * 
   * @param inputLine
   *          Chimera input line from which to construct this model
   */
  // TODO: [Optional] How to distinguish between PDB and MODBASE?
  // invoked when listing models: listm type molecule; lists level molecule
  // line = model id #0 type Molecule name 1ert
  public ChimeraModel(String inputLine)
  {
    this.name = ChimUtils.parseModelName(inputLine);
    // TODO: [Optional] Write a separate method to get model type
    if (name.startsWith("smiles"))
    {
      this.type = ModelType.SMILES;
    }
    else
    {
      this.type = ModelType.PDB_MODEL;
    }
    this.modelNumber = ChimUtils.parseModelNumber(inputLine)[0];
    this.subModelNumber = ChimUtils.parseModelNumber(inputLine)[1];

    this.chainMap = new TreeMap<String, ChimeraChain>();
    this.funcResidues = new HashSet<ChimeraResidue>();
  }

  /**
   * Add a residue to this model
   * 
   * @param residue
   *          to add to the model
   */
  public void addResidue(ChimeraResidue residue)
  {
    residue.setChimeraModel(this);
    // residueMap.put(residue.getIndex(), residue);
    String chainId = residue.getChainId();
    if (chainId != null)
    {
      addResidue(chainId, residue);
    }
    else
    {
      addResidue("_", residue);
    }
    // Put it in our map so that we can return it in order
    // residueMap.put(residue.getIndex(), residue);
  }

  /**
   * Add a residue to a chain in this model. If the chain associated with
   * chainId doesn't exist, it will be created.
   * 
   * @param chainId
   *          to add the residue to
   * @param residue
   *          to add to the chain
   */
  public void addResidue(String chainId, ChimeraResidue residue)
  {
    ChimeraChain chain = null;
    if (!chainMap.containsKey(chainId))
    {
      chain = new ChimeraChain(this.modelNumber, this.subModelNumber,
              chainId);
      chain.setChimeraModel(this);
      chainMap.put(chainId, chain);
    }
    else
    {
      chain = chainMap.get(chainId);
    }
    chain.addResidue(residue);
  }

  /**
   * Get the ChimeraModel (required for ChimeraStructuralObject interface)
   * 
   * @return ChimeraModel
   */
  @Override
  public ChimeraModel getChimeraModel()
  {
    return this;
  }

  /**
   * Get the model color of this model
   * 
   * @return model color of this model
   */
  public Color getModelColor()
  {
    return this.modelColor;
  }

  /**
   * Set the color of this model
   * 
   * @param color
   *          Color of this model
   */
  public void setModelColor(Color color)
  {
    this.modelColor = color;
  }

  /**
   * Return the name of this model
   * 
   * @return model name
   */
  public String getModelName()
  {
    return name;
  }

  /**
   * Set the name of this model
   * 
   * @param name
   *          model name
   */
  public void setModelName(String name)
  {
    this.name = name;
  }

  /**
   * Get the model number of this model
   * 
   * @return integer model number
   */
  public int getModelNumber()
  {
    return modelNumber;
  }

  /**
   * Set the model number of this model
   * 
   * @param modelNumber
   *          integer model number
   */
  public void setModelNumber(int modelNumber)
  {
    this.modelNumber = modelNumber;
  }

  /**
   * Get the sub-model number of this model
   * 
   * @return integer sub-model number
   */
  public int getSubModelNumber()
  {
    return subModelNumber;
  }

  /**
   * Set the sub-model number of this model
   * 
   * @param subModelNumber
   *          integer model number
   */
  public void setSubModelNumber(int subModelNumber)
  {
    this.subModelNumber = subModelNumber;
  }

  public ModelType getModelType()
  {
    return type;
  }

  public void setModelType(ModelType type)
  {
    this.type = type;
  }

  public HashSet<ChimeraResidue> getFuncResidues()
  {
    return funcResidues;
  }

  public void setFuncResidues(List<String> residues)
  {
    for (String residue : residues)
    {
      for (ChimeraChain chain : getChains())
      {
        if (residue.indexOf("-") > 0)
        {
          funcResidues.addAll(chain.getResidueRange(residue));
        }
        else
        {
          funcResidues.add(chain.getResidue(residue));
        }
      }
    }
  }

  /**
   * Get the user data for this model
   * 
   * @return user data
   */
  @Override
  public Object getUserData()
  {
    return userData;
  }

  /**
   * Set the user data for this model
   * 
   * @param data
   *          user data to associate with this model
   */
  @Override
  public void setUserData(Object data)
  {
    this.userData = data;
  }

  /**
   * Return the selected state of this model
   * 
   * @return the selected state
   */
  @Override
  public boolean isSelected()
  {
    return selected;
  }

  /**
   * Set the selected state of this model
   * 
   * @param selected
   *          a boolean to set the selected state to
   */
  @Override
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }

  /**
   * Return the chains in this model as a List
   * 
   * @return the chains in this model as a list
   */
  @Override
  public List<ChimeraStructuralObject> getChildren()
  {
    return new ArrayList<ChimeraStructuralObject>(chainMap.values());
  }

  /**
   * Return the chains in this model as a colleciton
   * 
   * @return the chains in this model
   */
  public Collection<ChimeraChain> getChains()
  {
    return chainMap.values();
  }

  /**
   * Get the number of chains in this model
   * 
   * @return integer chain count
   */
  public int getChainCount()
  {
    return chainMap.size();
  }

  /**
   * Get the list of chain names associated with this model
   * 
   * @return return the list of chain names for this model
   */
  public Collection<String> getChainNames()
  {
    return chainMap.keySet();
  }

  /**
   * Get the residues associated with this model
   * 
   * @return the list of residues in this model
   */
  public Collection<ChimeraResidue> getResidues()
  {
    Collection<ChimeraResidue> residues = new ArrayList<ChimeraResidue>();
    for (ChimeraChain chain : getChains())
    {
      residues.addAll(chain.getResidues());
    }
    return residues;
  }

  /**
   * Get the number of residues in this model
   * 
   * @return integer residues count
   */
  public int getResidueCount()
  {
    int count = 0;
    for (ChimeraChain chain : getChains())
    {
      count += chain.getResidueCount();
    }
    return count;
  }

  /**
   * Get a specific chain from the model
   * 
   * @param chain
   *          the ID of the chain to return
   * @return ChimeraChain associated with the chain
   */
  public ChimeraChain getChain(String chain)
  {
    if (chainMap.containsKey(chain))
    {
      return chainMap.get(chain);
    }
    return null;
  }

  /**
   * Return a specific residue based on its index
   * 
   * @param index
   *          of the residue to return
   * @return the residue associated with that index
   */
  public ChimeraResidue getResidue(String chainId, String index)
  {
    if (chainMap.containsKey(chainId))
    {
      return chainMap.get(chainId).getResidue(index);
    }
    return null;
  }

  /**
   * Checks if this model has selected children.
   */
  @Override
  public boolean hasSelectedChildren()
  {
    if (selected)
    {
      return true;
    }
    else
    {
      for (ChimeraChain chain : getChains())
      {
        if (chain.hasSelectedChildren())
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return the list of selected residues
   * 
   * @return all selected residues
   */
  public List<ChimeraResidue> getSelectedResidues()
  {
    List<ChimeraResidue> residueList = new ArrayList<ChimeraResidue>();
    for (ChimeraChain chain : getChains())
    {
      if (selected)
      {
        residueList.addAll(chain.getSelectedResidues());
      }
      else
      {
        residueList.addAll(getResidues());
      }
    }
    return residueList;
  }

  /**
   * Return the Chimera specification for this model.
   */
  @Override
  public String toSpec()
  {
    if (subModelNumber == 0)
    {
      return ("#" + modelNumber);
    }
    return ("#" + modelNumber + "." + subModelNumber);
  }

  /**
   * Return a string representation for the model. Shorten if longer than 100
   * characters.
   */
  @Override
  public String toString()
  {
    String modelName = "";
    // TODO: [Optional] Change cutoff for shortening model names in the
    // structure naviagator dialog
    if (getChainCount() > 0)
    {
      modelName = "Model " + toSpec() + " " + name + " (" + getChainCount()
              + " chains, " + getResidueCount() + " residues)";
    }
    else if (getResidueCount() > 0)
    {
      modelName = "Model " + toSpec() + " " + name + " ("
              + getResidueCount() + " residues)";
    }
    else
    {
      modelName = "Model " + toSpec() + " " + name + "";
    }

    Set<String> networkNames = new HashSet<String>();
    Set<String> nodeNames = new HashSet<String>();
    Set<String> edgeNames = new HashSet<String>();

    String cytoName = " [";
    if (networkNames.size() > 0)
    {
      if (networkNames.size() == 1)
      {
        cytoName += "Network {";
      }
      else if (networkNames.size() > 1)
      {
        cytoName += "Networks {";
      }
      for (String cName : networkNames)
      {
        cytoName += cName + ",";
      }
      cytoName = cytoName.substring(0, cytoName.length() - 1) + "}, ";
    }
    if (nodeNames.size() > 0)
    {
      if (nodeNames.size() == 1)
      {
        cytoName += "Node {";
      }
      else if (nodeNames.size() > 1)
      {
        cytoName += "Nodes {";
      }
      for (String cName : nodeNames)
      {
        cytoName += cName + ",";
      }
      cytoName = cytoName.substring(0, cytoName.length() - 1) + "}, ";
    }
    if (edgeNames.size() > 0)
    {
      if (edgeNames.size() == 1)
      {
        cytoName += "Edge {";
      }
      else if (edgeNames.size() > 1)
      {
        cytoName += "Edges {";
      }
      for (String cName : edgeNames)
      {
        cytoName += cName + ",";
      }
      cytoName = cytoName.substring(0, cytoName.length() - 1) + "}, ";
    }
    if (cytoName.endsWith(", "))
    {
      cytoName = cytoName.substring(0, cytoName.length() - 2);
    }
    cytoName += "]";
    String nodeName = modelName + cytoName;
    if (nodeName.length() > 100)
    {
      nodeName = nodeName.substring(0, 100) + "...";
    }
    return nodeName;
  }

  @Override
  public boolean equals(Object otherChimeraModel)
  {
    if (!(otherChimeraModel instanceof ChimeraModel))
    {
      return false;
    }
    ChimeraModel otherCM = ((ChimeraModel) otherChimeraModel);
    return this.name.equals(otherCM.name)
            && this.modelNumber == otherCM.modelNumber
            && this.type == otherCM.type;
  }

  @Override
  public int hashCode()
  {
    int hashCode = 1;
    hashCode = hashCode * 37 + this.name.hashCode();
    hashCode = hashCode * 37 + this.type.hashCode();
    hashCode = (hashCode * 37) + modelNumber;
    return hashCode;
  }
}
