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
import java.util.List;
import java.util.Map;

/**
 * Methods that generate commands that can be sent to a molecular structure
 * viewer program (e.g. Jmol, Chimera, ChimeraX)
 * 
 * @author gmcarstairs
 *
 */
public interface StructureCommandsI
{
  /**
   * Returns the command to colour by chain
   * 
   * @return
   */
  StructureCommandI colourByChain();

  /**
   * Returns the command to colour residues using a charge-based scheme:
   * <ul>
   * <li>Aspartic acid and Glutamic acid (negative charge) red</li>
   * <li>Lysine and Arginine (positive charge) blue</li>
   * <li>Cysteine - yellow</li>
   * <li>all others - white</li>
   * </ul>
   * 
   * @return
   */
  List<StructureCommandI> colourByCharge();

  /**
   * Returns the command to colour residues with the colours provided in the
   * map, one per three letter residue code
   * 
   * @param colours
   * @return
   */
  List<StructureCommandI> colourByResidues(Map<String, Color> colours);

  /**
   * Returns the command to set the background colour of the structure viewer
   * 
   * @param col
   * @return
   */
  StructureCommandI setBackgroundColour(Color col);

  /**
   * Returns commands to colour mapped residues of structures according to
   * Jalview's colouring (including feature colouring if applied). Parameter is
   * a map from Color to a model of all residues assigned that colour.
   * 
   * @param colourMap
   * @return
   */

  List<StructureCommandI> colourBySequence(
          Map<Object, AtomSpecModel> colourMap);

  /**
   * Returns a command to centre the display in the structure viewer
   * 
   * @return
   */
  StructureCommandI focusView();

  /**
   * Returns a command to show only the selected chains. The items in the input
   * list should be formatted as "modelid:chainid".
   * 
   * @param toShow
   * @return
   */
  List<StructureCommandI> showChains(List<String> toShow);

  /**
   * Returns a command to superpose structures by closest positioning of
   * residues in {@code atomSpec} to the corresponding residues in
   * {@code refAtoms}. If wanted, this may include commands to visually
   * highlight the residues that were used for the superposition.
   * 
   * @param refAtoms
   * @param atomSpec
   * @param backbone
   *          - superpose based on which kind of atomType
   * @return
   */
  List<StructureCommandI> superposeStructures(AtomSpecModel refAtoms,
          AtomSpecModel atomSpec, AtomSpecType backbone);

  /**
   * Returns a command to open a file of commands at the given path
   * 
   * @param path
   * @return
   */
  StructureCommandI openCommandFile(String path);

  /**
   * Returns a command to save the current viewer session state to the given
   * file
   * 
   * @param filepath
   * @return
   */
  StructureCommandI saveSession(String filepath);

  enum AtomSpecType
  {
    RESIDUE_ONLY, ALPHA, PHOSPHATE
  };

  /**
   * Returns a representation of the atom set represented by the model, in
   * viewer syntax format. If {@code alphaOnly} is true, this is restricted to
   * Alpha Carbon (peptide) or Phosphorous (rna) only
   * 
   * @param model
   * @param specType
   * @return
   */
  String getAtomSpec(AtomSpecModel model, AtomSpecType specType);

  /**
   * Returns the lowest model number used by the structure viewer (likely 0 or
   * 1)
   * 
   * @return
   */
  // TODO remove by refactoring so command generation is purely driven by
  // AtomSpecModel objects derived in the binding classes?
  int getModelStartNo();

  /**
   * Returns command(s) to show only the backbone of the peptide (cartoons in
   * Jmol, chain in Chimera)
   * 
   * @return
   */
  List<StructureCommandI> showBackbone();

  /**
   * Returns a command to open a file at the given path
   * 
   * @param file
   * @return
   */
  // refactor if needed to distinguish loading data or session files
  StructureCommandI loadFile(String file);

  /**
   * Returns commands to set atom attributes or properties, given a map of
   * Jalview features as {featureType, {featureValue, AtomSpecModel}}. The
   * assumption is that one command can be constructed for each feature type and
   * value combination, to apply it to one or more residues.
   * 
   * @param featureValues
   * @return
   */
  List<StructureCommandI> setAttributes(
          Map<String, Map<Object, AtomSpecModel>> featureValues);

  /**
   * Returns command to open a saved structure viewer session file, or null if
   * not supported
   * 
   * @param filepath
   * @return
   */
  StructureCommandI openSession(String filepath);

  /**
   * Returns a command to ask the viewer to close down
   * 
   * @return
   */
  StructureCommandI closeViewer();

  /**
   * Returns one or more commands to ask the viewer to notify model or selection
   * changes to the given uri. Returns null if this is not supported by the
   * structure viewer.
   * 
   * @param uri
   * @return
   */
  List<StructureCommandI> startNotifications(String uri);

  /**
   * Returns one or more commands to ask the viewer to stop notifying model or
   * selection changes. Returns null if this is not supported by the structure
   * viewer.
   * 
   * @return
   */
  List<StructureCommandI> stopNotifications();

  /**
   * Returns a command to ask the viewer for its current residue selection, or
   * null if no such command is supported
   * 
   * @return
   */
  StructureCommandI getSelectedResidues();

  /**
   * Returns a command to list the unique names of residue attributes, or null
   * if no such command is supported
   * 
   * @return
   */
  StructureCommandI listResidueAttributes();

  /**
   * Returns a command to list residues with an attribute of the given name,
   * with attribute value, or null if no such command is supported
   * 
   * @return
   */
  StructureCommandI getResidueAttributes(String attName);

  List<StructureCommandI> centerViewOn(List<AtomSpecModel> residues);
}
