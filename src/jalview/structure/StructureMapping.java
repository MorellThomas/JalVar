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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructureMapping
{
  public static final int UNASSIGNED_VALUE = Integer.MIN_VALUE;

  private static final int PDB_RES_NUM_INDEX = 0;

  private static final int PDB_ATOM_NUM_INDEX = 1;

  String mappingDetails;

  SequenceI sequence;

  String pdbfile;

  String pdbid;

  String pdbchain;

  // Mapping key is residue index while value is an array containing PDB resNum,
  // and atomNo
  HashMap<Integer, int[]> mapping;

  jalview.datamodel.Mapping seqToPdbMapping = null;

  /**
   * Constructor
   * 
   * @param seq
   * @param pdbfile
   * @param pdbid
   * @param chain
   * @param mapping
   *          a map from sequence to two values, { resNo, atomNo } in the
   *          structure
   * @param mappingDetails
   */
  public StructureMapping(SequenceI seq, String pdbfile, String pdbid,
          String chain, HashMap<Integer, int[]> mapping,
          String mappingDetails)
  {
    sequence = seq;
    this.pdbfile = pdbfile;
    this.pdbid = pdbid;
    this.pdbchain = chain;
    this.mapping = mapping;
    this.mappingDetails = mappingDetails;
  }

  public StructureMapping(SequenceI seq, String pdbFile2, String pdbId2,
          String chain, HashMap<Integer, int[]> mapping2,
          String mappingOutput, Mapping seqToPdbMapping)
  {
    this(seq, pdbFile2, pdbId2, chain, mapping2, mappingOutput);
    this.seqToPdbMapping = seqToPdbMapping;
  }

  public SequenceI getSequence()
  {
    return sequence;
  }

  public String getChain()
  {
    return pdbchain;
  }

  public String getPdbId()
  {
    return pdbid;
  }

  /**
   * 
   * @param seqpos
   * @return 0 or corresponding atom number for the sequence position
   */
  public int getAtomNum(int seqpos)
  {
    int[] resNumAtomMap = mapping.get(seqpos);
    if (resNumAtomMap != null)
    {
      return resNumAtomMap[PDB_ATOM_NUM_INDEX];
    }
    else
    {
      return UNASSIGNED_VALUE;
    }
  }

  /**
   * 
   * @param seqpos
   * @return UNASSIGNED_VALUE or the corresponding residue number for the
   *         sequence position
   */
  public int getPDBResNum(int seqpos)
  {
    int[] resNumAtomMap = mapping.get(seqpos);
    if (resNumAtomMap != null)
    {
      return resNumAtomMap[PDB_RES_NUM_INDEX];
    }
    else
    {
      return UNASSIGNED_VALUE;
    }
  }

  /**
   * Returns a (possibly empty) list of [start, end] residue positions in the
   * mapped structure, corresponding to the given range of sequence positions
   * 
   * @param fromSeqPos
   * @param toSeqPos
   * @return
   */
  public List<int[]> getPDBResNumRanges(int fromSeqPos, int toSeqPos)
  {
    List<int[]> result = new ArrayList<>();
    int startRes = -1;
    int endRes = -1;

    for (int i = fromSeqPos; i <= toSeqPos; i++)
    {
      int resNo = getPDBResNum(i);
      if (resNo == UNASSIGNED_VALUE)
      {
        continue; // no mapping from this sequence position
      }
      if (startRes == -1)
      {
        startRes = resNo;
        endRes = resNo;
      }
      if (resNo >= startRes && resNo <= endRes)
      {
        // within the current range - no change
        continue;
      }
      if (resNo == startRes - 1)
      {
        // extend beginning of current range
        startRes--;
        continue;
      }
      if (resNo == endRes + 1)
      {
        // extend end of current range
        endRes++;
        continue;
      }

      /*
       * resNo is not within or contiguous with last range,
       * so write out the last range
       */
      result.add(new int[] { startRes, endRes });
      startRes = resNo;
      endRes = resNo;
    }

    /*
     * and add the last range
     */
    if (startRes != -1)
    {
      result.add(new int[] { startRes, endRes });
    }

    return result;
  }

  /**
   * 
   * @param pdbResNum
   * @return -1 or the corresponding sequence position for a pdb residue number
   */
  public int getSeqPos(int pdbResNum)
  {
    for (Integer seqPos : mapping.keySet())
    {
      if (pdbResNum == getPDBResNum(seqPos))
      {
        return seqPos;
      }
    }
    return UNASSIGNED_VALUE;
  }

  /**
   * transfer a copy of an alignment annotation row in the PDB chain coordinate
   * system onto the mapped sequence
   * 
   * @param ana
   * @return the copy that was remapped to the mapped sequence
   * @note this method will create a copy and add it to the dataset sequence for
   *       the mapped sequence as well as the mapped sequence (if it is not a
   *       dataset sequence).
   */
  public AlignmentAnnotation transfer(AlignmentAnnotation ana)
  {
    AlignmentAnnotation ala_copy = new AlignmentAnnotation(ana);
    SequenceI ds = sequence;
    while (ds.getDatasetSequence() != null)
    {
      ds = ds.getDatasetSequence();
    }
    // need to relocate annotation from pdb coordinates to local sequence
    // -1,-1 doesn't look at pdbresnum but fails to remap sequence positions...

    ala_copy.remap(ds, mapping, -1, -1, 0);
    ds.addAlignmentAnnotation(ala_copy);
    if (ds != sequence)
    {
      // mapping wasn't to an original dataset sequence, so we make a copy on
      // the mapped sequence too
      ala_copy = new AlignmentAnnotation(ala_copy);
      sequence.addAlignmentAnnotation(ala_copy);
    }
    return ala_copy;
  }

  public String getMappingDetailsOutput()
  {
    return mappingDetails;
  }

  public HashMap<Integer, int[]> getMapping()
  {
    return mapping;
  }

  public Mapping getSeqToPdbMapping()
  {
    return seqToPdbMapping;
  }

  /**
   * A hash function that satisfies the contract that if two mappings are
   * equal(), they have the same hashCode
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result
            + ((mappingDetails == null) ? 0 : mappingDetails.hashCode());
    result = prime * result
            + ((pdbchain == null) ? 0 : pdbchain.hashCode());
    result = prime * result + ((pdbfile == null) ? 0 : pdbfile.hashCode());
    result = prime * result + ((pdbid == null) ? 0 : pdbid.hashCode());
    result = prime * result
            + ((seqToPdbMapping == null) ? 0 : seqToPdbMapping.hashCode());
    result = prime * result
            + ((sequence == null) ? 0 : sequence.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    StructureMapping other = (StructureMapping) obj;
    if (mappingDetails == null)
    {
      if (other.mappingDetails != null)
      {
        return false;
      }
    }
    else if (!mappingDetails.equals(other.mappingDetails))
    {
      return false;
    }
    if (pdbchain == null)
    {
      if (other.pdbchain != null)
      {
        return false;
      }
    }
    else if (!pdbchain.equals(other.pdbchain))
    {
      return false;
    }
    if (pdbfile == null)
    {
      if (other.pdbfile != null)
      {
        return false;
      }
    }
    else if (!pdbfile.equals(other.pdbfile))
    {
      return false;
    }
    if (pdbid == null)
    {
      if (other.pdbid != null)
      {
        return false;
      }
    }
    else if (!pdbid.equals(other.pdbid))
    {
      return false;
    }
    if (seqToPdbMapping == null)
    {
      if (other.seqToPdbMapping != null)
      {
        return false;
      }
    }
    else if (!seqToPdbMapping.equals(other.seqToPdbMapping))
    {
      return false;
    }
    if (sequence != other.sequence)
    {
      return false;
    }

    return true;
  }
}
