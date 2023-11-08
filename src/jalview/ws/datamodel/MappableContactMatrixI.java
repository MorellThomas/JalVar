package jalview.ws.datamodel;

import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceI;
import jalview.util.MapList;

public interface MappableContactMatrixI extends ContactMatrixI
{

  boolean hasReferenceSeq();

  SequenceI getReferenceSeq();

  /**
   * remaps the matrix to a new reference sequence
   * 
   * @param dsq
   * @param sqmpping
   *          - mapping from current reference to new reference - 1:1 only
   * @return new ContactMatrixI instance with updated mapping
   */
  MappableContactMatrixI liftOver(SequenceI dsq, Mapping sqmpping);

  /**
   * like ContactMatrixI.getContactList(int column) but
   * 
   * @param localFrame
   *          - sequence or other object that this contact matrix is associated
   *          with
   * @param column
   *          - position in localFrame
   * @return ContactListI that returns contacts w.r.t. localFrame
   */

  ContactListI getMappableContactList(SequenceI localFrame, int column);

  /**
   * 
   * Similar to AlignedCodonFrame.getMappingBetween
   * 
   * @param sequenceRef
   *          - a reference sequence mappable to this contactMatrix - may be
   *          null
   * @return null or the MapList mapping to the coordinates of the reference
   *         sequence (or if hasReferenceSeq() is false, and sequenceRef is
   *         null, any mapping present)
   * 
   */
  MapList getMapFor(SequenceI sequenceRef);

  /**
   * Locate a position in the mapped sequence for a single column in the matrix. 
   * this to resolve positions corresponding to column clusters
   * 
   * @param localFrame
   *          - sequence derivced from reference sequence
   * @param column
   *          - matrix row/column
   * @return sequence position(s) corresponding to column in contact matrix
   */
  int[] getMappedPositionsFor(SequenceI localFrame, int column);

  /**
   * Locate a position in the mapped sequence for a contiguous range of columns in the matrix 
   * use this to resolve positions corresponding to column clusters
   * 
   * @param localFrame
   *          - sequence derivced from reference sequence
   * @param column
   *          - matrix row/column
   * @return sequence position(s) corresponding to column in contact matrix
   */
  int[] getMappedPositionsFor(SequenceI localFrame, int from, int to);
}
