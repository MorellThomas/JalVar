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
package jalview.datamodel;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data structure to hold and manipulate a multiple sequence alignment
 */
public interface AlignmentI extends AnnotatedCollectionI
{
  /**
   * Calculates the number of sequences in an alignment, excluding hidden
   * sequences
   * 
   * @return Number of sequences in alignment
   */
  int getHeight();

  /**
   * Calculates the number of sequences in an alignment, including hidden
   * sequences
   * 
   * @return Number of sequences in alignment
   */
  int getAbsoluteHeight();

  /**
   * 
   * Answers the width of the alignment, including gaps, that is, the length of
   * the longest sequence, or -1 if there are no sequences. Avoid calling this
   * method repeatedly where possible, as it has to perform a calculation. Note
   * that this width includes any hidden columns.
   * 
   * @return
   * @see AlignmentI#getVisibleWidth()
   */
  @Override
  int getWidth();

  /**
   * 
   * Answers the visible width of the alignment, including gaps, that is, the
   * length of the longest sequence, excluding any hidden columns. Answers -1 if
   * there are no sequences. Avoid calling this method repeatedly where
   * possible, as it has to perform a calculation.
   * 
   * @return
   */
  int getVisibleWidth();

  /**
   * Calculates if this set of sequences (visible and invisible) are all the
   * same length
   * 
   * @return true if all sequences in alignment are the same length
   */
  boolean isAligned();

  /**
   * Calculates if this set of sequences is all the same length
   * 
   * @param includeHidden
   *          optionally exclude hidden sequences from test
   * @return true if all (or just visible) sequences are the same length
   */
  boolean isAligned(boolean includeHidden);

  /**
   * Answers if the sequence at alignmentIndex is hidden
   * 
   * @param alignmentIndex
   *          the index to check
   * @return true if the sequence is hidden
   */
  boolean isHidden(int alignmentIndex);

  /**
   * Gets sequences as a Synchronized collection
   * 
   * @return All sequences in alignment.
   */
  @Override
  List<SequenceI> getSequences();

  /**
   * Gets sequences as a SequenceI[]
   * 
   * @return All sequences in alignment.
   */
  SequenceI[] getSequencesArray();

  /**
   * Find a specific sequence in this alignment.
   * 
   * @param i
   *          Index of required sequence.
   * 
   * @return SequenceI at given index.
   */
  SequenceI getSequenceAt(int i);

  /**
   * Find a specific sequence in this alignment.
   * 
   * @param i
   *          Index of required sequence in full alignment, i.e. if all columns
   *          were visible
   * 
   * @return SequenceI at given index.
   */
  SequenceI getSequenceAtAbsoluteIndex(int i);

  /**
   * Returns a map of lists of sequences keyed by sequence name.
   * 
   * @return
   */
  Map<String, List<SequenceI>> getSequencesByName();

  /**
   * Add a new sequence to this alignment.
   * 
   * @param seq
   *          New sequence will be added at end of alignment.
   */
  void addSequence(SequenceI seq);

  /**
   * Used to set a particular index of the alignment with the given sequence.
   * 
   * @param i
   *          Index of sequence to be updated. if i>length, sequence will be
   *          added to end, with no intervening positions.
   * @param seq
   *          New sequence to be inserted. The existing sequence at position i
   *          will be replaced.
   * @return existing sequence (or null if i>current length)
   */
  SequenceI replaceSequenceAt(int i, SequenceI seq);

  /**
   * Deletes a sequence from the alignment. Updates hidden sequences to account
   * for the removed sequence. Do NOT use this method to delete sequences which
   * are just hidden.
   * 
   * @param s
   *          Sequence to be deleted.
   */
  void deleteSequence(SequenceI s);

  /**
   * Deletes a sequence from the alignment. Updates hidden sequences to account
   * for the removed sequence. Do NOT use this method to delete sequences which
   * are just hidden.
   * 
   * @param i
   *          Index of sequence to be deleted.
   */
  void deleteSequence(int i);

  /**
   * Deletes a sequence in the alignment which has been hidden.
   * 
   * @param i
   *          Index of sequence to be deleted
   */
  void deleteHiddenSequence(int i);

  /**
   * Finds sequence in alignment using sequence name as query.
   * 
   * @param name
   *          Id of sequence to search for.
   * 
   * @return Sequence matching query, if found. If not found returns null.
   */
  SequenceI findName(String name);

  SequenceI[] findSequenceMatch(String name);

  /**
   * Finds index of a given sequence in the alignment.
   * 
   * @param s
   *          Sequence to look for.
   * 
   * @return Index of sequence within the alignment or -1 if not found
   */
  int findIndex(SequenceI s);

  /**
   * Returns the first group (in the order in which groups were added) that
   * includes the given sequence instance and aligned position (base 0), or null
   * if none found
   * 
   * @param seq
   *          - must be contained in the alignment (not a dataset sequence)
   * @param position
   * 
   * @return
   */
  SequenceGroup findGroup(SequenceI seq, int position);

  /**
   * Finds all groups that a given sequence is part of.
   * 
   * @param s
   *          Sequence in alignment.
   * 
   * @return All groups containing given sequence.
   */
  SequenceGroup[] findAllGroups(SequenceI s);

  /**
   * Adds a new SequenceGroup to this alignment.
   * 
   * @param sg
   *          New group to be added.
   */
  void addGroup(SequenceGroup sg);

  /**
   * Deletes a specific SequenceGroup
   * 
   * @param g
   *          Group will be deleted from alignment.
   */
  void deleteGroup(SequenceGroup g);

  /**
   * Get all the groups associated with this alignment.
   * 
   * @return All groups as a list.
   */
  List<SequenceGroup> getGroups();

  /**
   * Deletes all groups from this alignment.
   */
  void deleteAllGroups();

  /**
   * Adds a new AlignmentAnnotation to this alignment
   * 
   * @note Care should be taken to ensure that annotation is at least as wide as
   *       the longest sequence in the alignment for rendering purposes.
   */
  void addAnnotation(AlignmentAnnotation aa);

  /**
   * moves annotation to a specified index in alignment annotation display stack
   * 
   * @param aa
   *          the annotation object to be moved
   * @param index
   *          the destination position
   */
  void setAnnotationIndex(AlignmentAnnotation aa, int index);

  /**
   * Delete all annotations, including auto-calculated if the flag is set true.
   * Returns true if at least one annotation was deleted, else false.
   * 
   * @param includingAutoCalculated
   * @return
   */
  boolean deleteAllAnnotations(boolean includingAutoCalculated);

  /**
   * Deletes a specific AlignmentAnnotation from the alignment, and removes its
   * reference from any SequenceI or SequenceGroup object's annotation if and
   * only if aa is contained within the alignment's annotation vector.
   * Otherwise, it will do nothing.
   * 
   * @param aa
   *          the annotation to delete
   * @return true if annotation was deleted from this alignment.
   */
  boolean deleteAnnotation(AlignmentAnnotation aa);

  /**
   * Deletes a specific AlignmentAnnotation from the alignment, and optionally
   * removes any reference from any SequenceI or SequenceGroup object's
   * annotation if and only if aa is contained within the alignment's annotation
   * vector. Otherwise, it will do nothing.
   * 
   * @param aa
   *          the annotation to delete
   * @param unhook
   *          flag indicating if any references should be removed from
   *          annotation - use this if you intend to add the annotation back
   *          into the alignment
   * @return true if annotation was deleted from this alignment.
   */
  boolean deleteAnnotation(AlignmentAnnotation aa, boolean unhook);

  /**
   * Get the annotation associated with this alignment (this can be null if no
   * annotation has ever been created on the alignment)
   * 
   * @return array of AlignmentAnnotation objects
   */
  @Override
  AlignmentAnnotation[] getAlignmentAnnotation();

  /**
   * Change the gap character used in this alignment to 'gc'
   * 
   * @param gc
   *          the new gap character.
   */
  void setGapCharacter(char gc);

  /**
   * Get the gap character used in this alignment
   * 
   * @return gap character
   */
  char getGapCharacter();

  /**
   * Test if alignment contains RNA structure
   * 
   * @return true if RNA structure AligmnentAnnotation was added to alignment
   */
  boolean hasRNAStructure();

  /**
   * Get the associated dataset for the alignment.
   * 
   * @return Alignment containing dataset sequences or null of this is a
   *         dataset.
   */
  AlignmentI getDataset();

  /**
   * Set the associated dataset for the alignment, or create one.
   * 
   * @param dataset
   *          The dataset alignment or null to construct one.
   */
  void setDataset(AlignmentI dataset);

  /**
   * pads sequences with gaps (to ensure the set looks like an alignment)
   * 
   * @return boolean true if alignment was modified
   */
  boolean padGaps();

  HiddenSequences getHiddenSequences();

  HiddenColumns getHiddenColumns();

  /**
   * Compact representation of alignment
   * 
   * @return CigarArray
   */
  CigarArray getCompactAlignment();

  /**
   * Set an arbitrary key value pair for an alignment. Note: both key and value
   * objects should return a meaningful, human readable response to .toString()
   * 
   * @param key
   * @param value
   */
  void setProperty(Object key, Object value);

  /**
   * Get a named property from the alignment.
   * 
   * @param key
   * @return value of property
   */
  Object getProperty(Object key);

  /**
   * Get the property hashtable.
   * 
   * @return hashtable of alignment properties (or null if none are defined)
   */
  Hashtable getProperties();

  /**
   * add a reference to a frame of aligned codons for this alignment
   * 
   * @param codons
   */
  void addCodonFrame(AlignedCodonFrame codons);

  /**
   * remove a particular codon frame reference from this alignment
   * 
   * @param codons
   * @return true if codon frame was removed.
   */
  boolean removeCodonFrame(AlignedCodonFrame codons);

  /**
   * get all codon frames associated with this alignment
   * 
   * @return
   */
  List<AlignedCodonFrame> getCodonFrames();

  /**
   * Set the codon frame mappings (replacing any existing list).
   */
  void setCodonFrames(List<AlignedCodonFrame> acfs);

  /**
   * get codon frames involving sequenceI
   */
  List<AlignedCodonFrame> getCodonFrame(SequenceI seq);

  /**
   * find sequence with given name in alignment
   * 
   * @param token
   *          name to find
   * @param b
   *          true implies that case insensitive matching will <em>also</em> be
   *          tried
   * @return matched sequence or null
   */
  SequenceI findName(String token, boolean b);

  /**
   * find next sequence with given name in alignment starting after a given
   * sequence
   * 
   * @param startAfter
   *          the sequence after which the search will be started (usually the
   *          result of the last call to findName)
   * @param token
   *          name to find
   * @param b
   *          true implies that case insensitive matching will <em>also</em> be
   *          tried
   * @return matched sequence or null
   */
  SequenceI findName(SequenceI startAfter, String token, boolean b);

  /**
   * find first sequence in alignment which is involved in the given search
   * result object
   * 
   * @param results
   * @return -1 or index of sequence in alignment
   */
  int findIndex(SearchResultsI results);

  /**
   * append sequences and annotation from another alignment object to this one.
   * Note: this is a straight transfer of object references, and may result in
   * toappend's dependent data being transformed to fit the alignment (changing
   * gap characters, etc...). If you are uncertain, use the copy Alignment copy
   * constructor to create a new version which can be appended without side
   * effect.
   * 
   * @param toappend
   *          - the alignment to be appended.
   */
  void append(AlignmentI toappend);

  /**
   * Justify the sequences to the left or right by deleting and inserting gaps
   * before the initial residue or after the terminal residue
   * 
   * @param right
   *          true if alignment padded to right, false to justify to left
   * @return true if alignment was changed TODO: return undo object
   */
  boolean justify(boolean right);

  /**
   * add given annotation row at given position (0 is start, -1 is end)
   * 
   * @param consensus
   * @param i
   */
  void addAnnotation(AlignmentAnnotation consensus, int i);

  /**
   * search for or create a specific annotation row on the alignment
   * 
   * @param name
   *          name for annotation (must match)
   * @param calcId
   *          calcId for the annotation (null or must match)
   * @param autoCalc
   *          - value of autocalc flag for the annotation
   * @param seqRef
   *          - null or specific sequence reference
   * @param groupRef
   *          - null or specific group reference
   * @param method
   *          - CalcId for the annotation (must match)
   * 
   * @return existing annotation matching the given attributes
   */
  AlignmentAnnotation findOrCreateAnnotation(String name, String calcId,
          boolean autoCalc, SequenceI seqRef, SequenceGroup groupRef);

  /**
   * move the given group up or down in the alignment by the given number of
   * rows. Implementor assumes given group is already present on alignment - no
   * recalculations are triggered.
   * 
   * @param sg
   * @param map
   * @param up
   * @param i
   */
  void moveSelectedSequencesByOne(SequenceGroup sg,
          Map<SequenceI, SequenceCollectionI> map, boolean up);

  /**
   * validate annotation after an edit and update any alignment state flags
   * accordingly
   * 
   * @param alignmentAnnotation
   */
  void validateAnnotation(AlignmentAnnotation alignmentAnnotation);

  /**
   * Align this alignment the same as the given one. If both of the same type
   * (nucleotide/protein) then align both identically. If this is nucleotide and
   * the other is protein, make 3 gaps for each gap in the protein sequences. If
   * this is protein and the other is nucleotide, insert a gap for each 3 gaps
   * (or part thereof) between nucleotide bases. Returns the number of mapped
   * sequences that were realigned .
   * 
   * @param al
   * @return
   */
  int alignAs(AlignmentI al);

  /**
   * Returns the set of distinct sequence names in the alignment.
   * 
   * @return
   */
  Set<String> getSequenceNames();

  /**
   * Checks if the alignment has at least one sequence with one non-gaped
   * residue
   * 
   * @return
   */
  public boolean hasValidSequence();

  /**
   * Update any mappings to 'virtual' sequences to compatible real ones, if
   * present in the added sequences. Returns a count of mappings updated.
   * 
   * @param seqs
   * @return
   */
  int realiseMappings(List<SequenceI> seqs);

  /**
   * Returns the first AlignedCodonFrame that has a mapping between the given
   * dataset sequences
   * 
   * @param mapFrom
   * @param mapTo
   * @return
   */
  AlignedCodonFrame getMapping(SequenceI mapFrom, SequenceI mapTo);

  /**
   * Set the hidden columns collection on the alignment. Answers true if the
   * hidden column selection changed, else false.
   * 
   * @param cols
   * @return
   */
  public boolean setHiddenColumns(HiddenColumns cols);

  /**
   * Set the first sequence as representative and hide its insertions. Typically
   * used when loading JPred files.
   */
  public void setupJPredAlignment();

  /**
   * Add gaps into the sequences aligned to profileseq under the given
   * AlignmentView
   * 
   * @param profileseq
   *          sequence in al which sequences are aligned to
   * @param input
   *          alignment view where sequence corresponding to profileseq is first
   *          entry
   * @return new HiddenColumns for new alignment view, with insertions into
   *         profileseq marked as hidden.
   */
  public HiddenColumns propagateInsertions(SequenceI profileseq,
          AlignmentView input);
}
