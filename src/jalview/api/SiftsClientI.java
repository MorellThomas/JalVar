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
package jalview.api;

import jalview.datamodel.SequenceI;
import jalview.structure.StructureMapping;
import jalview.ws.sifts.MappingOutputPojo;
import jalview.ws.sifts.SiftsException;
import jalview.xml.binding.sifts.Entry.Entity;

import java.util.HashMap;
import java.util.HashSet;

// JBPComment: this isn't a top-level Jalview API - should be in its own package api

public interface SiftsClientI
{
  /**
   * Get the DB Accession Id for the SIFTs Entry
   * 
   * @return
   */
  public String getDbAccessionId();

  /**
   * Get DB Coordinate system for the SIFTs Entry
   * 
   * @return
   */
  public String getDbCoordSys();

  /**
   * Get DB Source for the SIFTs Entry
   * 
   * @return
   */
  public String getDbSource();

  /**
   * Get DB version for the SIFTs Entry
   * 
   * @return
   */
  public String getDbVersion();

  /**
   * Get Number of Entities available in the SIFTs Entry
   * 
   * @return
   */
  public int getEntityCount();

  /**
   * Get a unique Entity by its Id
   * 
   * @param id
   *          ID of the entity to fetch
   * @return Entity
   * @throws Exception
   */
  public Entity getEntityById(String id) throws SiftsException;

  /**
   * Get all accession Ids available in the current SIFTs entry
   * 
   * @return a unique set of discovered accession strings
   */
  public HashSet<String> getAllMappingAccession();

  /**
   * Check if the accessionId is available in current SIFTs Entry
   * 
   * @param accessionId
   * @return
   */
  public boolean isAccessionMatched(String accessionId);

  /**
   * 
   * @param mop
   *          MappingOutputPojo
   * @return Sequence<->Structure mapping as int[][]
   * @throws SiftsException
   */
  public StringBuilder getMappingOutput(MappingOutputPojo mop)
          throws SiftsException;

  /**
   * 
   * @param seq
   *          sequence to generate mapping against the structure
   * @param pdbFile
   *          PDB file for the mapping
   * @param chain
   *          the chain of the entry to use for mapping
   * @return StructureMapping
   * @throws SiftsException
   */
  public StructureMapping getSiftsStructureMapping(SequenceI seq,
          String pdbFile, String chain) throws SiftsException;

  /**
   * Get residue by residue mapping for a given Sequence and SIFTs entity
   * 
   * @param entityId
   *          Id of the target entity in the SIFTs entry
   * @param seq
   *          SequenceI
   * @return generated mapping
   * @throws Exception
   */
  public HashMap<Integer, int[]> getGreedyMapping(String entityId,
          SequenceI seq, java.io.PrintStream os) throws SiftsException;
}