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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jalview.bin.Cache;
import jalview.gui.Preferences;

/**
 * This object maintains the relationship between Chimera objects and Cytoscape
 * objects.
 */

public class StructureManager
{
  /*
   * Version numbers to build Windows installation paths for 
   * Chimera  https://www.cgl.ucsf.edu/chimera/download.html
   * ChimeraX http://www.rbvi.ucsf.edu/chimerax/download.html#release
   *          https://www.rbvi.ucsf.edu/trac/ChimeraX/wiki/ChangeLog
   * These are a fallback for Jalview users who don't save path in Preferences;
   * these will need to be updated as new versions are released;
   * deliberately not 'final' (so modifiable using Groovy).
   * 
   * May 2020: 1.14 is Chimera latest, anticipating a few more...
   * 0.93 is ChimeraX latest, 1.0 expected soon
   */
  private static String[] CHIMERA_VERSIONS = new String[] { "1.16.2",
      "1.16.1", "1.16", "1.15.2", "1.15.1", "1.15", "1.14.2", "1.14.1",
      "1.14", "1.13.1", "1.13", "1.12.2", "1.12.1", "1.12", "1.11.2",
      "1.11.2", "1.11.1", "1.11" };

  // Missing 1.1 as this has known bug see JAL-2422
  private static String[] CHIMERAX_VERSIONS = new String[] { "1.3", "1.2.5",
      "1.0", "0.93", "0.92", "0.91", "0.9" };

  static final String[] defaultStructureKeys = { "Structure", "pdb",
      "pdbFileName", "PDB ID", "structure", "biopax.xref.PDB", "pdb_ids",
      "ModelName", "ModelNumber" };

  static final String[] defaultChemStructKeys = { "Smiles", "smiles",
      "SMILES" };

  static final String[] defaultResidueKeys = { "FunctionalResidues",
      "ResidueList", "Residues" };

  public enum ModelType
  {
    PDB_MODEL, MODBASE_MODEL, SMILES
  };

  public static Properties pathProps;

  private String chimeraCommandAttr = "ChimeraCommand";

  private String chimeraOutputTable = "ChimeraTable";

  private String chimeraOutputAttr = "ChimeraOutput";

  private boolean haveGUI = true;

  private ChimeraManager chimeraManager = null;

  static private List<ChimeraStructuralObject> chimSelectionList;

  private boolean ignoreCySelection = false;

  private File configurationDirectory = null;

  private static Logger logger = LoggerFactory
          .getLogger(ext.edu.ucsf.rbvi.strucviz2.StructureManager.class);

  public StructureManager(boolean haveGUI)
  {
    this.haveGUI = haveGUI;
    // Create the Chimera interface
    chimeraManager = new ChimeraManager(this);
    chimSelectionList = new ArrayList<>();
    pathProps = new Properties();
  }

  public ChimeraManager getChimeraManager()
  {
    return chimeraManager;
  }

  public boolean openStructures(Collection<List<String>> chimObjNames,
          ModelType type)
  {
    // new models
    Map<String, List<ChimeraModel>> newModels = new HashMap<>();
    if (chimObjNames.size() > 0)
    {
      List<String> names = chimObjNames.iterator().next();
      if (names == null)
      {
        return false;
      }
      for (String chimObjName : names)
      {
        // get or open the corresponding models if they already exist
        List<ChimeraModel> currentModels = chimeraManager
                .getChimeraModels(chimObjName, type);
        if (currentModels.size() == 0)
        {
          // open and return models
          currentModels = chimeraManager.openModel(chimObjName, type);
          if (currentModels == null)
          {
            // failed to open model, continue with next
            continue;
          }
          // if (type == ModelType.SMILES) {
          // newModels.put("smiles:" + chimObjName, currentModels);
          // } else {
          newModels.put(chimObjName, currentModels);
          // }
          // for each model
          for (ChimeraModel currentModel : currentModels)
          {
            // if not RIN then associate new model with the Cytoscape
            // node
            // if (!currentChimMap.containsKey(currentModel)) {
            // currentChimMap.put(currentModel, new HashSet<CyIdentifiable>());
            // }
          }
        }
      }
    }
    else
    {
      return false;
    }
    // update dialog
    // if (mnDialog != null) {
    // mnDialog.modelChanged();
    // }
    // aTask.associate();
    return true;

  }

  // TODO: [Release] Handle case where one network is associated with two models
  // that are opened
  // at the same time
  /*
   * public boolean openStructures(CyNetwork network, Map<CyIdentifiable,
   * List<String>> chimObjNames, ModelType type) { if
   * (!chimeraManager.isChimeraLaunched() &&
   * !chimeraManager.launchChimera(getChimeraPaths(network))) {
   * logger.error("Chimera could not be launched."); return false; } else if
   * (chimObjNames.size() == 0) { return false; } else if (network == null) {
   * return openStructures(chimObjNames.values(), type); }
   * 
   * // potential rins Set<CyNetwork> potentialRINs = new HashSet<CyNetwork>();
   * // attributes List<String> attrsFound = new ArrayList<String>();
   * attrsFound.
   * addAll(CytoUtils.getMatchingAttributes(network.getDefaultNodeTable(),
   * getCurrentStructureKeys(network)));
   * attrsFound.addAll(CytoUtils.getMatchingAttributes
   * (network.getDefaultNodeTable(), getCurrentChemStructKeys(network))); // new
   * models Map<String, List<ChimeraModel>> newModels = new HashMap<String,
   * List<ChimeraModel>>(); // for each node that has an associated structure
   * for (CyIdentifiable cyObj : chimObjNames.keySet()) { // get possible res
   * specs List<String> specsFound = null; if (cyObj instanceof CyNode) {
   * specsFound = ChimUtils.getResidueKeys(network.getDefaultNodeTable(), cyObj,
   * attrsFound); } // save node to track its selection and mapping to chimera
   * objects if (!currentCyMap.containsKey(cyObj)) { currentCyMap.put(cyObj, new
   * HashSet<ChimeraStructuralObject>()); } // save node to network mapping to
   * keep track of selection events if (!networkMap.containsKey(cyObj)) {
   * networkMap.put(cyObj, new HashSet<CyNetwork>()); }
   * networkMap.get(cyObj).add(network); // for each structure that has to be
   * opened for (String chimObjName : chimObjNames.get(cyObj)) { // get or open
   * the corresponding models if they already exist List<ChimeraModel>
   * currentModels = chimeraManager.getChimeraModels(chimObjName, type); if
   * (currentModels.size() == 0) { // open and return models currentModels =
   * chimeraManager.openModel(chimObjName, type); if (currentModels == null) {
   * // failed to open model, continue with next continue; } // if (type ==
   * ModelType.SMILES) { // newModels.put("smiles:" + chimObjName,
   * currentModels); // } else { newModels.put(chimObjName, currentModels); // }
   * // for each model for (ChimeraModel currentModel : currentModels) { //
   * check if it is a RIN boolean foundRIN = false; if
   * (currentModel.getModelType().equals(ModelType.PDB_MODEL)) { // go through
   * all node annotations and check if any of them is a residue // or a chain if
   * (cyObj instanceof CyNode && network.containsNode((CyNode) cyObj) &&
   * specsFound != null && specsFound.size() > 0) { for (String resSpec :
   * specsFound) { ChimeraStructuralObject res =
   * ChimUtils.fromAttribute(resSpec, chimeraManager); if (res != null && (res
   * instanceof ChimeraResidue || res instanceof ChimeraChain)) { // if so,
   * assume it might be a RIN potentialRINs.add(network); foundRIN = true;
   * break; } } } else if (cyObj instanceof CyNetwork) { // if cyObj is a
   * network, check for residue/chain annotations in an // arbitrary node
   * CyNetwork rinNet = (CyNetwork) cyObj; if (rinNet.getNodeList().size() > 0)
   * { specsFound = ChimUtils.getResidueKeys( rinNet.getDefaultNodeTable(),
   * rinNet.getNodeList().get(0), attrsFound); for (String resSpec : specsFound)
   * { ChimeraStructuralObject res = ChimUtils.fromAttribute( resSpec,
   * chimeraManager); if (res != null && (res instanceof ChimeraResidue || res
   * instanceof ChimeraChain)) { potentialRINs.add(network); foundRIN = true;
   * break; } } } } } if (foundRIN) { continue; } // if not RIN then associate
   * new model with the Cytoscape // node if
   * (!currentChimMap.containsKey(currentModel)) {
   * currentChimMap.put(currentModel, new HashSet<CyIdentifiable>()); } String
   * cyObjName = network.getRow(cyObj).get(CyNetwork.NAME, String.class); if
   * (cyObjName != null && cyObjName.endsWith(currentModel.getModelName())) { //
   * it is a modbase model, associate directly
   * currentCyMap.get(cyObj).add(currentModel);
   * currentChimMap.get(currentModel).add(cyObj);
   * currentModel.addCyObject(cyObj, network); } else if (specsFound != null &&
   * specsFound.size() > 0) { for (String resSpec : specsFound) {
   * ChimeraStructuralObject specModel = ChimUtils.fromAttribute( resSpec,
   * chimeraManager); if (specModel == null &&
   * resSpec.equals(currentModel.getModelName())) { specModel =
   * chimeraManager.getChimeraModel( currentModel.getModelNumber(),
   * currentModel.getSubModelNumber()); } if (specModel != null &&
   * currentModel.toSpec().equals(specModel.toSpec()) ||
   * currentModel.getModelName().equals("smiles:" + resSpec)) {
   * currentCyMap.get(cyObj).add(currentModel);
   * currentChimMap.get(currentModel).add(cyObj);
   * currentModel.addCyObject(cyObj, network);
   * currentModel.setFuncResidues(ChimUtils.parseFuncRes(
   * getResidueList(network, cyObj), chimObjName)); } } } } } } } // networks
   * that contain nodes associated to newly opened models // this will usually
   * be of length 1 for (CyNetwork net : potentialRINs) {
   * addStructureNetwork(net); } // update dialog if (mnDialog != null) {
   * mnDialog.modelChanged(); } aTask.associate(); return true; }
   */
  public void closeStructures(Set<String> chimObjNames)
  {
    // for each cytoscape object and chimera model pair
    for (String modelName : chimObjNames)
    {
      List<ChimeraModel> models = chimeraManager
              .getChimeraModels(modelName);
      for (ChimeraModel model : models)
      {
        closeModel(model);
      }
    }
    // if (mnDialog != null) {
    // mnDialog.modelChanged();
    // }
  }

  // TODO: [Optional] Can we make a screenshot of a single molecule?
  public File saveChimeraImage()
  {
    File tmpFile = null;
    try
    {
      // Create the temp file name
      tmpFile = File.createTempFile("structureViz", ".png");
      chimeraManager.sendChimeraCommand("set bgTransparency", false);
      chimeraManager.sendChimeraCommand(
              "copy file " + tmpFile.getAbsolutePath() + " png", true);
      chimeraManager.sendChimeraCommand("unset bgTransparency", false);
    } catch (IOException ioe)
    {
      // Log error
      logger.error("Error writing image", ioe);
    }
    return tmpFile;
  }

  public void closeModel(ChimeraModel model)
  {
    // close model in Chimera
    chimeraManager.closeModel(model);
    // remove all associations
    // if (currentChimMap.containsKey(model)) {
    // for (CyIdentifiable cyObj : model.getCyObjects().keySet()) {
    // if (cyObj == null) {
    // continue;
    // } else if (currentCyMap.containsKey(cyObj)) {
    // currentCyMap.get(cyObj).remove(model);
    // } else if (cyObj instanceof CyNetwork) {
    // for (ChimeraResidue residue : model.getResidues()) {
    // if (currentChimMap.containsKey(residue)) {
    // for (CyIdentifiable cyObjRes : currentChimMap.get(residue)) {
    // if (currentCyMap.containsKey(cyObjRes)) {
    // currentCyMap.get(cyObjRes).remove(residue);
    // }
    // }
    // currentChimMap.remove(residue);
    // }
    // }
    // }
    // }
    // currentChimMap.remove(model);
    // }
  }

  // public void addStructureNetwork(CyNetwork rin) {
  // if (rin == null) {
  // return;
  // }
  // ChimeraModel model = null;
  // // the network is not added to the model in the currentChimMap
  // List<String> attrsFound =
  // CytoUtils.getMatchingAttributes(rin.getDefaultNodeTable(),
  // getCurrentStructureKeys(rin));
  // for (CyNode node : rin.getNodeList()) {
  // if (!networkMap.containsKey(node)) {
  // networkMap.put(node, new HashSet<CyNetwork>());
  // }
  // networkMap.get(node).add(rin);
  // List<String> specsFound =
  // ChimUtils.getResidueKeys(rin.getDefaultNodeTable(), node,
  // attrsFound);
  // for (String residueSpec : specsFound) {
  // // if (!rin.getRow(node).isSet(ChimUtils.RESIDUE_ATTR)) {
  // // continue;
  // // }
  // // String residueSpec = rin.getRow(node).get(ChimUtils.RESIDUE_ATTR,
  // String.class);
  // ChimeraStructuralObject chimObj = ChimUtils.fromAttribute(residueSpec,
  // chimeraManager);
  // // chimObj.getChimeraModel().addCyObject(node, rin);
  // if (chimObj == null || chimObj instanceof ChimeraModel) {
  // continue;
  // }
  // model = chimObj.getChimeraModel();
  // if (!currentCyMap.containsKey(node)) {
  // currentCyMap.put(node, new HashSet<ChimeraStructuralObject>());
  // }
  // currentCyMap.get(node).add(chimObj);
  // if (!currentChimMap.containsKey(chimObj)) {
  // currentChimMap.put(chimObj, new HashSet<CyIdentifiable>());
  // }
  // currentChimMap.get(chimObj).add(node);
  // }
  // }
  // if (model != null) {
  // model.addCyObject(rin, rin);
  // if (!currentCyMap.containsKey(rin)) {
  // currentCyMap.put(rin, new HashSet<ChimeraStructuralObject>());
  // }
  // currentCyMap.get(rin).add(model);
  // }
  // }

  public void exitChimera()
  {
    // // exit chimera, invokes clearOnExitChimera
    // if (mnDialog != null) {
    // mnDialog.setVisible(false);
    // mnDialog = null;
    // }
    // if (alDialog != null) {
    // alDialog.setVisible(false);
    // }
    chimeraManager.exitChimera();
  }

  // invoked by ChimeraManager whenever Chimera exits
  public void clearOnChimeraExit()
  {
    // // clear structures
    // currentCyMap.clear();
    // currentChimMap.clear();
    // networkMap.clear();
    chimSelectionList.clear();
    // if (chimTable != null) {
    // ((CyTableManager)
    // getService(CyTableManager.class)).deleteTable(chimTable.getSUID());
    // }
    // if (mnDialog != null) {
    // if (mnDialog.isVisible()) {
    // mnDialog.lostChimera();
    // mnDialog.setVisible(false);
    // }
    // mnDialog = null;
    // if (alDialog != null) {
    // alDialog.setVisible(false);
    // }
    // }
  }

  // We need to do this in two passes since some parts of a structure might be
  // selected and some might not. Our selection model (unfortunately) only
  // tells
  // us that something has changed, not what...
  public void updateCytoscapeSelection()
  {
    // List<ChimeraStructuralObject> selectedChimObj
    ignoreCySelection = true;
    // System.out.println("update Cytoscape selection");
    // find all possibly selected Cytoscape objects and unselect them
    // Set<CyNetwork> networks = new HashSet<CyNetwork>();
    // for (CyIdentifiable currentCyObj : currentCyMap.keySet()) {
    // if (!networkMap.containsKey(currentCyObj)) {
    // continue;
    // }
    // Set<CyNetwork> currentCyNetworks = networkMap.get(currentCyObj);
    // if (currentCyNetworks == null || currentCyNetworks.size() == 0) {
    //
    // continue;
    // }
    // for (CyNetwork network : currentCyNetworks) {
    // if ((currentCyObj instanceof CyNode && network.containsNode((CyNode)
    // currentCyObj))
    // || (currentCyObj instanceof CyEdge && network
    // .containsEdge((CyEdge) currentCyObj))) {
    // network.getRow(currentCyObj).set(CyNetwork.SELECTED, false);
    // networks.add(network);
    // }
    // }
    // }
    //
    // // select only those associated with selected Chimera objects
    // Set<CyIdentifiable> currentCyObjs = new HashSet<CyIdentifiable>();
    // for (ChimeraStructuralObject chimObj : chimSelectionList) {
    // ChimeraModel currentSelModel = chimObj.getChimeraModel();
    // if (currentChimMap.containsKey(currentSelModel)) {
    // currentCyObjs.addAll(currentChimMap.get(currentSelModel));
    // }
    // if (currentChimMap.containsKey(chimObj)) {
    // currentCyObjs.addAll(currentChimMap.get(chimObj));
    // }
    // // System.out.println(chimObj.toSpec() + ": " +
    // // currentCyObjs.size());
    // }
    // for (CyIdentifiable cyObj : currentCyObjs) {
    // // System.out.println(cyObj.toString());
    // if (cyObj == null || !networkMap.containsKey(cyObj)) {
    // continue;
    // }
    // Set<CyNetwork> currentCyNetworks = networkMap.get(cyObj);
    // if (currentCyNetworks == null || currentCyNetworks.size() == 0) {
    // continue;
    // }
    // for (CyNetwork network : currentCyNetworks) {
    // if ((cyObj instanceof CyNode && network.containsNode((CyNode) cyObj))
    // || (cyObj instanceof CyEdge && network.containsEdge((CyEdge) cyObj))) {
    // network.getRow(cyObj).set(CyNetwork.SELECTED, true);
    // networks.add(network);
    // }
    // }
    // }
    //
    // CyNetworkViewManager cyNetViewManager = (CyNetworkViewManager)
    // getService(CyNetworkViewManager.class);
    // // Update network views
    // for (CyNetwork network : networks) {
    // Collection<CyNetworkView> views =
    // cyNetViewManager.getNetworkViews(network);
    // for (CyNetworkView view : views) {
    // view.updateView();
    // }
    // }
    ignoreCySelection = false;
  }

  public void cytoscapeSelectionChanged(Map<Long, Boolean> selectedRows)
  {
    // if (ignoreCySelection || currentCyMap.size() == 0) {
    // return;
    // }
    // // clearSelectionList();
    // // System.out.println("cytoscape selection changed");
    // // iterate over all cy objects with associated models
    // for (CyIdentifiable cyObj : currentCyMap.keySet()) {
    // if (cyObj instanceof CyNetwork ||
    // !selectedRows.containsKey(cyObj.getSUID())) {
    // continue;
    // }
    // for (ChimeraStructuralObject chimObj : currentCyMap.get(cyObj)) {
    // if (selectedRows.get(cyObj.getSUID())) {
    // addChimSelection(chimObj);
    // if (chimObj instanceof ChimeraResidue) {
    // if (chimObj.getChimeraModel().isSelected()) {
    // removeChimSelection(chimObj.getChimeraModel());
    // } else if (chimObj.getChimeraModel()
    // .getChain(((ChimeraResidue) chimObj).getChainId()).isSelected()) {
    // removeChimSelection(chimObj.getChimeraModel().getChain(
    // ((ChimeraResidue) chimObj).getChainId()));
    // }
    // }
    // } else {
    // removeChimSelection(chimObj);
    // if (chimObj.hasSelectedChildren() && chimObj instanceof ChimeraModel) {
    // for (ChimeraResidue residue : ((ChimeraModel) chimObj)
    // .getSelectedResidues()) {
    // removeChimSelection(residue);
    // }
    // }
    // }
    // }
    // }
    // System.out.println("selection list: " + getChimSelectionCount());
    updateChimeraSelection();
    selectionChanged();
  }

  // Save models in a HashMap/Set for better performance?
  public void updateChimeraSelection()
  {
    // System.out.println("update Chimera selection");
    String selSpec = "";
    for (int i = 0; i < chimSelectionList.size(); i++)
    {
      ChimeraStructuralObject nodeInfo = chimSelectionList.get(i);
      // we do not care about the model anymore
      selSpec = selSpec.concat(nodeInfo.toSpec());
      if (i < chimSelectionList.size() - 1)
      {
        selSpec.concat("|");
      }
    }
    if (selSpec.length() > 0)
    {
      chimeraManager.select("sel " + selSpec);
    }
    else
    {
      chimeraManager.select("~sel");
    }
  }

  /**
   * This is called by the selectionListener to let us know that the user has
   * changed their selection in Chimera. We need to go back to Chimera to find
   * out what is currently selected and update our list.
   */
  public void chimeraSelectionChanged()
  {
    // System.out.println("Chimera selection changed");
    clearSelectionList();
    // Execute the command to get the list of models with selections
    Map<Integer, ChimeraModel> selectedModelsMap = chimeraManager
            .getSelectedModels();
    // Now get the residue-level data
    chimeraManager.getSelectedResidues(selectedModelsMap);
    // Get the selected objects
    try
    {
      for (ChimeraModel selectedModel : selectedModelsMap.values())
      {
        int modelNumber = selectedModel.getModelNumber();
        int subModelNumber = selectedModel.getSubModelNumber();
        // Get the corresponding "real" model
        if (chimeraManager.hasChimeraModel(modelNumber, subModelNumber))
        {
          ChimeraModel dataModel = chimeraManager
                  .getChimeraModel(modelNumber, subModelNumber);
          if (dataModel.getResidueCount() == selectedModel.getResidueCount()
                  || dataModel
                          .getModelType() == StructureManager.ModelType.SMILES)
          {
            // Select the entire model
            addChimSelection(dataModel);
            // dataModel.setSelected(true);
          }
          else
          {
            for (ChimeraChain selectedChain : selectedModel.getChains())
            {
              ChimeraChain dataChain = dataModel
                      .getChain(selectedChain.getChainId());
              if (selectedChain.getResidueCount() == dataChain
                      .getResidueCount())
              {
                addChimSelection(dataChain);
                // dataChain.setSelected(true);
              }
              // else {
              // Need to select individual residues
              for (ChimeraResidue res : selectedChain.getResidues())
              {
                String residueIndex = res.getIndex();
                ChimeraResidue residue = dataChain.getResidue(residueIndex);
                if (residue == null)
                {
                  continue;
                }
                addChimSelection(residue);
                // residue.setSelected(true);
              } // resIter.hasNext
                // }
            } // chainIter.hasNext()
          }
        }
      } // modelIter.hasNext()
    } catch (Exception ex)
    {
      logger.warn("Could not update selection", ex);
    }
    // System.out.println("selection list: " + getChimSelectionCount());
    // Finally, update the navigator panel
    selectionChanged();
    updateCytoscapeSelection();
  }

  public void selectFunctResidues(Collection<ChimeraModel> models)
  {
    clearSelectionList();
    for (ChimeraModel model : models)
    {
      for (ChimeraResidue residue : model.getFuncResidues())
      {
        addChimSelection(residue);
      }
    }
    updateChimeraSelection();
    updateCytoscapeSelection();
    selectionChanged();
  }

  // public void selectFunctResidues(CyNode node, CyNetwork network) {
  // clearSelectionList();
  // if (currentCyMap.containsKey(node)) {
  // Set<ChimeraStructuralObject> chimObjects = currentCyMap.get(node);
  // for (ChimeraStructuralObject obj : chimObjects) {
  // if (obj instanceof ChimeraModel) {
  // ChimeraModel model = (ChimeraModel) obj;
  // for (ChimeraResidue residue : model.getFuncResidues()) {
  // addChimSelection(residue);
  // }
  // }
  // }
  // }
  // updateChimeraSelection();
  // updateCytoscapeSelection();
  // selectionChanged();
  // }

  public List<ChimeraStructuralObject> getChimSelectionList()
  {
    return chimSelectionList;
  }

  public int getChimSelectionCount()
  {
    return chimSelectionList.size();
  }

  /**
   * Add a selection to the selection list. This is called primarily by the
   * Model Navigator Dialog to keep the selections in sync
   * 
   * @param selectionToAdd
   *          the selection to add to our list
   */
  public void addChimSelection(ChimeraStructuralObject selectionToAdd)
  {
    if (selectionToAdd != null
            && !chimSelectionList.contains(selectionToAdd))
    {
      chimSelectionList.add(selectionToAdd);
      selectionToAdd.setSelected(true);
    }
  }

  /**
   * Remove a selection from the selection list. This is called primarily by the
   * Model Navigator Dialog to keep the selections in sync
   * 
   * @param selectionToRemove
   *          the selection to remove from our list
   */
  public void removeChimSelection(ChimeraStructuralObject selectionToRemove)
  {
    if (selectionToRemove != null
            && chimSelectionList.contains(selectionToRemove))
    {
      chimSelectionList.remove(selectionToRemove);
      selectionToRemove.setSelected(false);
    }
  }

  /**
   * Clear the list of selected objects
   */
  public void clearSelectionList()
  {
    for (ChimeraStructuralObject cso : chimSelectionList)
    {
      if (cso != null)
      {
        cso.setSelected(false);
      }
    }
    chimSelectionList.clear();
  }

  /**
   * Associate a new network with the corresponding Chimera objects.
   * 
   * @param network
   */

  /**
   * Dump and refresh all of our model/chain/residue info
   */
  public void updateModels()
  {
    // Stop all of our listeners while we try to handle this
    chimeraManager.stopListening();

    // Get all of the open models
    List<ChimeraModel> newModelList = chimeraManager.getModelList();

    // Match them up -- assume that the model #'s haven't changed
    for (ChimeraModel newModel : newModelList)
    {
      // Get the color (for our navigator)
      newModel.setModelColor(chimeraManager.getModelColor(newModel));

      // Get our model info
      int modelNumber = newModel.getModelNumber();
      int subModelNumber = newModel.getSubModelNumber();

      // If we already know about this model number, get the Structure,
      // which tells us about the associated CyNode
      if (chimeraManager.hasChimeraModel(modelNumber, subModelNumber))
      {
        ChimeraModel oldModel = chimeraManager.getChimeraModel(modelNumber,
                subModelNumber);
        chimeraManager.removeChimeraModel(modelNumber, subModelNumber);
        newModel.setModelType(oldModel.getModelType());
        if (oldModel.getModelType() == ModelType.SMILES)
        {
          newModel.setModelName(oldModel.getModelName());
        }
        // re-assign associations to cytoscape objects
        // Map<CyIdentifiable, CyNetwork> oldModelCyObjs =
        // oldModel.getCyObjects();
        // for (CyIdentifiable cyObj : oldModelCyObjs.keySet()) {
        // // add cy objects to the new model
        // newModel.addCyObject(cyObj, oldModelCyObjs.get(cyObj));
        // if (currentCyMap.containsKey(cyObj)) {
        // currentCyMap.get(cyObj).add(newModel);
        // if (currentCyMap.get(cyObj).contains(oldModel)) {
        // currentCyMap.get(cyObj).remove(oldModel);
        // }
        // }
        // }
        // // add new model to the chimera objects map and remove old model
        // if (currentChimMap.containsKey(oldModel)) {
        // currentChimMap.put(newModel, currentChimMap.get(oldModel));
        // currentChimMap.remove(oldModel);
        // }
      }
      // add new model to ChimeraManager
      chimeraManager.addChimeraModel(modelNumber, subModelNumber, newModel);

      // Get the residue information
      if (newModel.getModelType() != ModelType.SMILES)
      {
        chimeraManager.addResidues(newModel);
      }
      // for (CyIdentifiable cyObj : newModel.getCyObjects().keySet()) {
      // if (cyObj != null && cyObj instanceof CyNetwork) {
      // addStructureNetwork((CyNetwork) cyObj);
      // } else if (cyObj != null && cyObj instanceof CyNode) {
      // newModel.setFuncResidues(ChimUtils.parseFuncRes(
      // getResidueList(newModel.getCyObjects().get(cyObj), cyObj),
      // newModel.getModelName()));
      // }
      // }
    }

    // associate all models with any node or network
    // aTask.associate();

    // Restart all of our listeners
    chimeraManager.startListening();
    // Done
  }

  public void launchModelNavigatorDialog()
  {
    // TODO: [Optional] Use haveGUI flag
    // if (!haveGUI) {
    // return;
    // }
    // if (mnDialog == null) {
    // CySwingApplication cyApplication = (CySwingApplication)
    // getService(CySwingApplication.class);
    // mnDialog = new ModelNavigatorDialog(cyApplication.getJFrame(), this);
    // mnDialog.pack();
    // }
    // mnDialog.setVisible(true);
  }

  public boolean isMNDialogOpen()
  {
    // if (mnDialog != null && mnDialog.isVisible()) {
    // return true;
    // }
    return false;
  }

  /**
   * Invoked by the listener thread.
   */
  public void modelChanged()
  {
    // if (mnDialog != null) {
    // mnDialog.modelChanged();
    // }
  }

  /**
   * Inform our interface that the selection has changed
   */
  public void selectionChanged()
  {
    // if (mnDialog != null) {
    // // System.out.println("update dialog selection");
    // mnDialog.updateSelection(new
    // ArrayList<ChimeraStructuralObject>(chimSelectionList));
    // }
  }

  public void launchAlignDialog(boolean useChains)
  {
    // TODO: [Optional] Use haveGUI flag
    // Sometimes it does not appear in Windows
    // if (!haveGUI) {
    // return;
    // }
    // if (alDialog != null) {
    // alDialog.setVisible(false);
    // alDialog.dispose();
    // }
    // System.out.println("launch align dialog");
    List<ChimeraStructuralObject> chimObjectList = new ArrayList<>();
    for (ChimeraModel model : chimeraManager.getChimeraModels())
    {
      if (useChains)
      {
        for (ChimeraChain chain : model.getChains())
        {
          chimObjectList.add(chain);
        }
      }
      else
      {
        chimObjectList.add(model);
      }
    }
    // Bring up the dialog
    // CySwingApplication cyApplication = (CySwingApplication)
    // getService(CySwingApplication.class);
    // alDialog = new AlignStructuresDialog(cyApplication.getJFrame(), this,
    // chimObjectList);
    // alDialog.pack();
    // alDialog.setVisible(true);
  }

  public List<String> getAllStructureKeys()
  {
    return Arrays.asList(defaultStructureKeys);
  }

  public List<String> getAllChemStructKeys()
  {
    return Arrays.asList(defaultChemStructKeys);
  }

  public List<String> getAllResidueKeys()
  {
    return Arrays.asList(defaultResidueKeys);
  }

  public List<String> getAllChimeraResidueAttributes()
  {
    List<String> attributes = new ArrayList<>();
    // attributes.addAll(rinManager.getResAttrs());
    attributes.addAll(chimeraManager.getAttrList());
    return attributes;
  }

  StructureSettings defaultSettings = null;

  // TODO: [Optional] Change priority of Chimera paths
  public static List<String> getChimeraPaths(boolean isChimeraX)
  {
    List<String> pathList = new ArrayList<>();

    // if no network is available and the settings have been modified by the
    // user, check for a
    // path to chimera
    //
    // For Jalview, Preferences/Cache plays this role instead
    // if (defaultSettings != null)
    // {
    // String defaultPath = defaultSettings.getChimeraPath();
    // if (defaultPath != null && !defaultPath.equals(""))
    // {
    // pathList.add(defaultPath);
    // return pathList;
    // }
    // }

    String os = System.getProperty("os.name");
    String userPath = Cache
            .getDefault(isChimeraX ? Preferences.CHIMERAX_PATH
                    : Preferences.CHIMERA_PATH, null);

    /*
     * paths are based on getChimeraPaths() in
     * Chimera:
     * https://github.com/RBVI/structureViz2/blob/master/src/main/java/edu/ucsf/rbvi/structureViz2/internal/model/StructureManager.java
     * ChimeraX:
     * https://github.com/RBVI/structureVizX/blob/master/src/main/java/edu/ucsf/rbvi/structureVizX/internal/model/StructureManager.java
     */
    String chimera = isChimeraX ? "ChimeraX" : "Chimera";
    String chimeraExe = isChimeraX ? "ChimeraX" : "chimera";

    /*
     * Jalview addition: check if path set in user preferences
     */
    if (userPath != null)
    {
      // in macos, deal with the user selecting the .app folder
      boolean adjusted = false;
      if (os.startsWith("Mac") && userPath.endsWith((".app")))
      {
        String possiblePath = String.format("%s/Contents/MacOS/%s",
                userPath, chimeraExe);
        if (new File(possiblePath).exists())
        {
          pathList.add(possiblePath);
          adjusted = true;
        }
      }
      if (!adjusted)
      {
        pathList.add(userPath);
      }
    }

    // Add default installation paths
    if (os.startsWith("Linux"))
    {
      // ChimeraX .deb and .rpm packages put symbolic link from
      // /usr/bin/chimerax
      pathList.add(String.format("/usr/bin/%s",
              chimeraExe.toLowerCase(Locale.ROOT)));
      pathList.add(String.format("/usr/bin/%s", chimeraExe));

      pathList.add(String.format("/usr/local/bin/%s",
              chimeraExe.toLowerCase(Locale.ROOT)));
      pathList.add(String.format("/usr/local/bin/%s", chimeraExe));

      // these paths also used by .deb and .rpm
      pathList.add(String.format("/usr/lib/ucsf-%s/bin/%s",
              chimera.toLowerCase(Locale.ROOT), chimeraExe));
      pathList.add(String.format("/usr/libexec/UCSF-%s/bin/%s", chimera,
              chimeraExe));

      pathList.add(String.format("/usr/local/chimera/bin/%s", chimeraExe));

      // user home paths
      pathList.add(
              String.format("%s/bin/%s", System.getProperty("user.home"),
                      chimeraExe.toLowerCase(Locale.ROOT)));
      pathList.add(String.format("%s/bin/%s",
              System.getProperty("user.home"), chimeraExe));
      pathList.add(String.format("%s/opt/bin/%s",
              System.getProperty("user.home"),
              chimeraExe.toLowerCase(Locale.ROOT)));
      pathList.add(String.format("%s/opt/bin/%s",
              System.getProperty("user.home"), chimeraExe));
      pathList.add(String.format("%s/local/bin/%s",
              System.getProperty("user.home"),
              chimeraExe.toLowerCase(Locale.ROOT)));
      pathList.add(String.format("%s/local/bin/%s",
              System.getProperty("user.home"), chimeraExe));
    }
    else if (os.startsWith("Windows"))
    {
      for (String root : new String[] { "\\Program Files",
          "C:\\Program Files", "\\Program Files (x86)",
          "C:\\Program Files (x86)", String.format("%s\\AppData\\Local",
                  System.getProperty("user.home")) })
      {
        String[] candidates = isChimeraX ? CHIMERAX_VERSIONS
                : CHIMERA_VERSIONS;
        for (String version : candidates)
        {
          // TODO original code doesn't include version in path; which is right?
          String path = String.format("%s\\%s %s\\bin\\%s", root, chimera,
                  version, chimeraExe);
          pathList.add(path);
          pathList.add(path + ".exe");
        }
        // try without a version number too
        String path = String.format("%s\\%s\\bin\\%s", root, chimera,
                chimeraExe);
        pathList.add(path);
        pathList.add(path + ".exe");
      }
    }
    else if (os.startsWith("Mac"))
    {
      // check for installations with version numbers first
      String[] candidates = isChimeraX ? CHIMERAX_VERSIONS
              : CHIMERA_VERSIONS;
      for (String version : candidates)
      {
        pathList.add(
                String.format("/Applications/%s-%s.app/Contents/MacOS/%s",
                        chimera, version, chimeraExe));
        pathList.add(
                String.format("%s/Applications/%s-%s.app/Contents/MacOS/%s",
                        System.getProperty("user.home"), chimera, version,
                        chimeraExe));
      }
      pathList.add(String.format("/Applications/%s.app/Contents/MacOS/%s",
              chimera, chimeraExe));
      pathList.add(String.format("%s/Applications/%s.app/Contents/MacOS/%s",
              System.getProperty("user.home"), chimera, chimeraExe));
    }
    return pathList;
  }

  public void setChimeraPathProperty(String path)
  {
    // CytoUtils.setDefaultChimeraPath(registrar, chimeraPropertyName,
    // chimeraPathPropertyKey,
    // path);
  }

  public void setStructureSettings(StructureSettings structureSettings)
  {
    this.defaultSettings = structureSettings;
  }

  public String getCurrentChimeraPath(Object object)
  {
    if (defaultSettings != null)
    {
      return defaultSettings.getChimeraPath();
    }
    else
    {
      return "";
    }
  }

  // public void initChimTable() {
  // CyTableManager manager = (CyTableManager) getService(CyTableManager.class);
  // CyTableFactory factory = (CyTableFactory) getService(CyTableFactory.class);
  // for (CyTable table : manager.getGlobalTables()) {
  // if (table.getTitle().equals(chimeraOutputTable)) {
  // manager.deleteTable(table.getSUID());
  // }
  // }
  // chimTable = factory.createTable(chimeraOutputTable, chimeraCommandAttr,
  // String.class,
  // false, true);
  // manager.addTable(chimTable);
  // if (chimTable.getColumn(chimeraOutputAttr) == null) {
  // chimTable.createListColumn(chimeraOutputAttr, String.class, false);
  // }
  // }

  // public void addChimReply(String command, List<String> reply) {
  // chimTable.getRow(command).set(chimeraOutputAttr, reply);
  // }

}
