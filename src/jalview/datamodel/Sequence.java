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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import fr.orsay.lri.varna.models.rna.RNA;
import jalview.analysis.AlignSeq;
import jalview.analysis.AlignmentUtils;
import jalview.analysis.SeqsetUtils;
import jalview.datamodel.features.SequenceFeatures;
import jalview.datamodel.features.SequenceFeaturesI;
import jalview.util.Comparison;
import jalview.util.DBRefUtils;
import jalview.util.MapList;
import jalview.util.StringUtils;
import jalview.ws.datamodel.alphafold.MappableContactMatrix;

/**
 * 
 * Implements the SequenceI interface for a char[] based sequence object
 */
public class Sequence extends ASequence implements SequenceI
{

  /**
   * A subclass that gives us access to modCount, which tracks whether there
   * have been any changes. We use this to update
   * 
   * @author hansonr
   *
   * @param <T>
   */
  @SuppressWarnings("serial")
  public class DBModList<T> extends ArrayList<DBRefEntry>
  {

    protected int getModCount()
    {
      return modCount;
    }

  }

  SequenceI datasetSequence;

  private String name;

  private char[] sequence;

  private String description;

  private int start;

  private int end;

  private Vector<PDBEntry> pdbIds;

  private String vamsasId;

  private DBModList<DBRefEntry> dbrefs; // controlled access

  /**
   * a flag to let us know that elements have changed in dbrefs
   * 
   * @author Bob Hanson
   */
  private int refModCount = 0;

  private RNA rna;

  /**
   * This annotation is displayed below the alignment but the positions are tied
   * to the residues of this sequence
   *
   * TODO: change to List<>
   */
  private Vector<AlignmentAnnotation> annotation;

  private SequenceFeaturesI sequenceFeatureStore;

  /*
   * A cursor holding the approximate current view position to the sequence,
   * as determined by findIndex or findPosition or findPositions.
   * Using a cursor as a hint allows these methods to be more performant for
   * large sequences.
   */
  private SequenceCursor cursor;

  /*
   * A number that should be incremented whenever the sequence is edited.
   * If the value matches the cursor token, then we can trust the cursor,
   * if not then it should be recomputed. 
   */
  private int changeCount;

  /**
   * Creates a new Sequence object.
   * 
   * @param name
   *          display name string
   * @param sequence
   *          string to form a possibly gapped sequence out of
   * @param start
   *          first position of non-gap residue in the sequence
   * @param end
   *          last position of ungapped residues (nearly always only used for
   *          display purposes)
   */
  public Sequence(String name, String sequence, int start, int end)
  {
    this();
    initSeqAndName(name, sequence.toCharArray(), start, end);
  }

  public Sequence(String name, char[] sequence, int start, int end)
  {
    this();
    initSeqAndName(name, sequence, start, end);
  }

  /**
   * Stage 1 constructor - assign name, sequence, and set start and end fields.
   * start and end are updated values from name2 if it ends with /start-end
   * 
   * @param name2
   * @param sequence2
   * @param start2
   * @param end2
   */
  protected void initSeqAndName(String name2, char[] sequence2, int start2,
          int end2)
  {
    this.name = name2;
    this.sequence = sequence2;
    this.start = start2;
    this.end = end2;
    parseId();
    checkValidRange();
  }

  /**
   * If 'name' ends in /i-j, where i >= j > 0 are integers, extracts i and j as
   * start and end respectively and removes the suffix from the name
   */
  void parseId()
  {
    if (name == null)
    {
      System.err.println(
              "POSSIBLE IMPLEMENTATION ERROR: null sequence name passed to constructor.");
      name = "";
    }
    int slashPos = name.lastIndexOf('/');
    if (slashPos > -1 && slashPos < name.length() - 1)
    {
      String suffix = name.substring(slashPos + 1);
      String[] range = suffix.split("-");
      if (range.length == 2)
      {
        try
        {
          int from = Integer.valueOf(range[0]);
          int to = Integer.valueOf(range[1]);
          if (from > 0 && to >= from)
          {
            name = name.substring(0, slashPos);
            setStart(from);
            setEnd(to);
            checkValidRange();
          }
        } catch (NumberFormatException e)
        {
          // leave name unchanged if suffix is invalid
        }
      }
    }
  }

  /**
   * Ensures that 'end' is not before the end of the sequence, that is,
   * (end-start+1) is at least as long as the count of ungapped positions. Note
   * that end is permitted to be beyond the end of the sequence data.
   */
  void checkValidRange()
  {
    // Note: JAL-774 :
    // http://issues.jalview.org/browse/JAL-774?focusedCommentId=11239&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-11239
    {
      int endRes = 0;
      for (int j = 0; j < sequence.length; j++)
      {
        if (!Comparison.isGap(sequence[j]))
        {
          endRes++;
        }
      }
      if (endRes > 0)
      {
        endRes += start - 1;
      }

      if (end < endRes)
      {
        end = endRes;
      }
    }

  }

  /**
   * default constructor
   */
  private Sequence()
  {
    sequenceFeatureStore = new SequenceFeatures();
  }

  /**
   * Creates a new Sequence object.
   * 
   * @param name
   *          DOCUMENT ME!
   * @param sequence
   *          DOCUMENT ME!
   */
  public Sequence(String name, String sequence)
  {
    this(name, sequence, 1, -1);
  }

  /**
   * Creates a new Sequence object with new AlignmentAnnotations but inherits
   * any existing dataset sequence reference. If non exists, everything is
   * copied.
   * 
   * @param seq
   *          if seq is a dataset sequence, behaves like a plain old copy
   *          constructor
   */
  public Sequence(SequenceI seq)
  {
    this(seq, seq.getAnnotation());
  }

  /**
   * Create a new sequence object with new features, DBRefEntries, and PDBIds
   * but inherits any existing dataset sequence reference, and duplicate of any
   * annotation that is present in the given annotation array.
   * 
   * @param seq
   *          the sequence to be copied
   * @param alAnnotation
   *          an array of annotation including some associated with seq
   */
  public Sequence(SequenceI seq, AlignmentAnnotation[] alAnnotation)
  {
    this();
    initSeqFrom(seq, alAnnotation);
  }

  /**
   * does the heavy lifting when cloning a dataset sequence, or coping data from
   * dataset to a new derived sequence.
   * 
   * @param seq
   *          - source of attributes.
   * @param alAnnotation
   *          - alignment annotation present on seq that should be copied onto
   *          this sequence
   */
  protected void initSeqFrom(SequenceI seq,
          AlignmentAnnotation[] alAnnotation)
  {
    char[] oseq = seq.getSequence(); // returns a copy of the array
    initSeqAndName(seq.getName(), oseq, seq.getStart(), seq.getEnd());

    description = seq.getDescription();
    if (seq != datasetSequence)
    {
      setDatasetSequence(seq.getDatasetSequence());
    }

    /*
     * only copy DBRefs and seqfeatures if we really are a dataset sequence
     */
    if (datasetSequence == null)
    {
      List<DBRefEntry> dbr = seq.getDBRefs();
      if (dbr != null)
      {
        for (int i = 0, n = dbr.size(); i < n; i++)
        {
          addDBRef(new DBRefEntry(dbr.get(i)));
        }
      }

      /*
       * make copies of any sequence features
       */
      for (SequenceFeature sf : seq.getSequenceFeatures())
      {
        addSequenceFeature(new SequenceFeature(sf));
      }
    }

    if (seq.getAnnotation() != null)
    {
      AlignmentAnnotation[] sqann = seq.getAnnotation();
      for (int i = 0; i < sqann.length; i++)
      {
        if (sqann[i] == null)
        {
          continue;
        }
        boolean found = (alAnnotation == null);
        if (!found)
        {
          for (int apos = 0; !found && apos < alAnnotation.length; apos++)
          {
            found = (alAnnotation[apos] == sqann[i]);
          }
        }
        if (found)
        {
          // only copy the given annotation
          AlignmentAnnotation newann = new AlignmentAnnotation(sqann[i]);
          ContactMatrixI cm = seq.getContactMatrixFor(sqann[i]);
          if (cm!=null)
          {
            addContactListFor(newann, cm);
          }
          addAlignmentAnnotation(newann);
        }
      }
    }
    if (seq.getAllPDBEntries() != null)
    {
      Vector<PDBEntry> ids = seq.getAllPDBEntries();
      for (PDBEntry pdb : ids)
      {
        this.addPDBId(new PDBEntry(pdb));
      }
    }
  }

  @Override
  public void setSequenceFeatures(List<SequenceFeature> features)
  {
    if (datasetSequence != null)
    {
      datasetSequence.setSequenceFeatures(features);
      return;
    }
    sequenceFeatureStore = new SequenceFeatures(features);
  }

  @Override
  public synchronized boolean addSequenceFeature(SequenceFeature sf)
  {
    if (sf.getType() == null)
    {
      System.err.println(
              "SequenceFeature type may not be null: " + sf.toString());
      return false;
    }

    if (datasetSequence != null)
    {
      return datasetSequence.addSequenceFeature(sf);
    }

    return sequenceFeatureStore.add(sf);
  }

  @Override
  public void deleteFeature(SequenceFeature sf)
  {
    if (datasetSequence != null)
    {
      datasetSequence.deleteFeature(sf);
    }
    else
    {
      sequenceFeatureStore.delete(sf);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @return
   */
  @Override
  public List<SequenceFeature> getSequenceFeatures()
  {
    if (datasetSequence != null)
    {
      return datasetSequence.getSequenceFeatures();
    }
    return sequenceFeatureStore.getAllFeatures();
  }

  @Override
  public SequenceFeaturesI getFeatures()
  {
    return datasetSequence != null ? datasetSequence.getFeatures()
            : sequenceFeatureStore;
  }

  @Override
  public boolean addPDBId(PDBEntry entry)
  {
    if (pdbIds == null)
    {
      pdbIds = new Vector<>();
      pdbIds.add(entry);
      return true;
    }

    for (PDBEntry pdbe : pdbIds)
    {
      if (pdbe.updateFrom(entry))
      {
        return false;
      }
    }
    pdbIds.addElement(entry);
    return true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param id
   *          DOCUMENT ME!
   */
  @Override
  public void setPDBId(Vector<PDBEntry> id)
  {
    pdbIds = id;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public Vector<PDBEntry> getAllPDBEntries()
  {
    return pdbIds == null ? new Vector<>() : pdbIds;
  }

  /**
   * Answers the sequence name, with '/start-end' appended if jvsuffix is true
   * 
   * @return
   */
  @Override
  public String getDisplayId(boolean jvsuffix)
  {
    if (!jvsuffix)
    {
      return name;
    }
    StringBuilder result = new StringBuilder(name);
    result.append("/").append(start).append("-").append(end);

    return result.toString();
  }

  /**
   * Sets the sequence name. If the name ends in /start-end, then the start-end
   * values are parsed out and set, and the suffix is removed from the name.
   * 
   * @param theName
   */
  @Override
  public void setName(String theName)
  {
    this.name = theName;
    this.parseId();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public String getName()
  {
    return this.name;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param start
   *          DOCUMENT ME!
   */
  @Override
  public void setStart(int start)
  {
    this.start = start;
    sequenceChanged();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getStart()
  {
    return this.start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param end
   *          DOCUMENT ME!
   */
  @Override
  public void setEnd(int end)
  {
    this.end = end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getEnd()
  {
    return this.end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getLength()
  {
    return this.sequence.length;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   */
  @Override
  public void setSequence(String seq)
  {
    this.sequence = seq.toCharArray();
    checkValidRange();
    sequenceChanged();
  }

  @Override
  public String getSequenceAsString()
  {
    return new String(sequence);
  }

  @Override
  public String getSequenceAsString(int start, int end)
  {
    return new String(getSequence(start, end));
  }

  @Override
  public char[] getSequence()
  {
    // return sequence;
    return sequence == null ? null
            : Arrays.copyOf(sequence, sequence.length);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.SequenceI#getSequence(int, int)
   */
  @Override
  public char[] getSequence(int start, int end)
  {
    if (start < 0)
    {
      start = 0;
    }
    // JBPNote - left to user to pad the result here (TODO:Decide on this
    // policy)
    if (start >= sequence.length)
    {
      return new char[0];
    }

    if (end >= sequence.length)
    {
      end = sequence.length;
    }

    char[] reply = new char[end - start];
    System.arraycopy(sequence, start, reply, 0, end - start);

    return reply;
  }

  @Override
  public SequenceI getSubSequence(int start, int end)
  {
    if (start < 0)
    {
      start = 0;
    }
    char[] seq = getSequence(start, end);
    if (seq.length == 0)
    {
      return null;
    }
    int nstart = findPosition(start);
    int nend = findPosition(end) - 1;
    // JBPNote - this is an incomplete copy.
    SequenceI nseq = new Sequence(this.getName(), seq, nstart, nend);
    nseq.setDescription(description);
    if (datasetSequence != null)
    {
      nseq.setDatasetSequence(datasetSequence);
    }
    else
    {
      nseq.setDatasetSequence(this);
    }
    return nseq;
  }

  /**
   * Returns the character of the aligned sequence at the given position (base
   * zero), or space if the position is not within the sequence's bounds
   * 
   * @return
   */
  @Override
  public char getCharAt(int i)
  {
    if (i >= 0 && i < sequence.length)
    {
      return sequence[i];
    }
    else
    {
      return ' ';
    }
  }

  /**
   * Sets the sequence description, and also parses out any special formats of
   * interest
   * 
   * @param desc
   */
  @Override
  public void setDescription(String desc)
  {
    this.description = desc;
  }

  @Override
  public void setGeneLoci(String speciesId, String assemblyId,
          String chromosomeId, MapList map)
  {
    addDBRef(new GeneLocus(speciesId, assemblyId, chromosomeId,
            new Mapping(map)));
  }

  /**
   * Returns the gene loci mapping for the sequence (may be null)
   * 
   * @return
   */
  @Override
  public GeneLociI getGeneLoci()
  {
    List<DBRefEntry> refs = getDBRefs();
    if (refs != null)
    {
      for (final DBRefEntry ref : refs)
      {
        if (ref instanceof GeneLociI)
        {
          return (GeneLociI) ref;
        }
      }
    }
    return null;
  }

  /**
   * Answers the description
   * 
   * @return
   */
  @Override
  public String getDescription()
  {
    return this.description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int findIndex(int pos)
  {
    /*
     * use a valid, hopefully nearby, cursor if available
     */
    if (isValidCursor(cursor))
    {
      return findIndex(pos, cursor);
    }

    int j = start;
    int i = 0;
    int startColumn = 0;

    /*
     * traverse sequence from the start counting gaps; make a note of
     * the column of the first residue to save in the cursor
     */
    while ((i < sequence.length) && (j <= end) && (j <= pos))
    {
      if (!Comparison.isGap(sequence[i]))
      {
        if (j == start)
        {
          startColumn = i;
        }
        j++;
      }
      i++;
    }

    if (j == end && j < pos)
    {
      return end + 1;
    }

    updateCursor(pos, i, startColumn);
    return i;
  }

  /**
   * Updates the cursor to the latest found residue and column position
   * 
   * @param residuePos
   *          (start..)
   * @param column
   *          (1..)
   * @param startColumn
   *          column position of the first sequence residue
   */
  protected void updateCursor(int residuePos, int column, int startColumn)
  {
    /*
     * preserve end residue column provided cursor was valid
     */
    int endColumn = isValidCursor(cursor) ? cursor.lastColumnPosition : 0;

    if (residuePos == this.end)
    {
      endColumn = column;
    }

    cursor = new SequenceCursor(this, residuePos, column, startColumn,
            endColumn, this.changeCount);
  }

  /**
   * Answers the aligned column position (1..) for the given residue position
   * (start..) given a 'hint' of a residue/column location in the neighbourhood.
   * The hint may be left of, at, or to the right of the required position.
   * 
   * @param pos
   * @param curs
   * @return
   */
  protected int findIndex(final int pos, SequenceCursor curs)
  {
    if (!isValidCursor(curs))
    {
      /*
       * wrong or invalidated cursor, compute de novo
       */
      return findIndex(pos);
    }

    if (curs.residuePosition == pos)
    {
      return curs.columnPosition;
    }

    /*
     * move left or right to find pos from hint.position
     */
    int col = curs.columnPosition - 1; // convert from base 1 to base 0
    int newPos = curs.residuePosition;
    int delta = newPos > pos ? -1 : 1;

    while (newPos != pos)
    {
      col += delta; // shift one column left or right
      if (col < 0)
      {
        break;
      }
      if (col == sequence.length)
      {
        col--; // return last column if we failed to reach pos
        break;
      }
      if (!Comparison.isGap(sequence[col]))
      {
        newPos += delta;
      }
    }

    col++; // convert back to base 1

    /*
     * only update cursor if we found the target position
     */
    if (newPos == pos)
    {
      updateCursor(pos, col, curs.firstColumnPosition);
    }

    return col;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int findPosition(final int column)
  {
    /*
     * use a valid, hopefully nearby, cursor if available
     */
    if (isValidCursor(cursor))
    {
      return findPosition(column + 1, cursor);
    }

    // TODO recode this more naturally i.e. count residues only
    // as they are found, not 'in anticipation'

    /*
     * traverse the sequence counting gaps; note the column position
     * of the first residue, to save in the cursor
     */
    int firstResidueColumn = 0;
    int lastPosFound = 0;
    int lastPosFoundColumn = 0;
    int seqlen = sequence.length;

    if (seqlen > 0 && !Comparison.isGap(sequence[0]))
    {
      lastPosFound = start;
      lastPosFoundColumn = 0;
    }

    int j = 0;
    int pos = start;

    while (j < column && j < seqlen)
    {
      if (!Comparison.isGap(sequence[j]))
      {
        lastPosFound = pos;
        lastPosFoundColumn = j;
        if (pos == this.start)
        {
          firstResidueColumn = j;
        }
        pos++;
      }
      j++;
    }
    if (j < seqlen && !Comparison.isGap(sequence[j]))
    {
      lastPosFound = pos;
      lastPosFoundColumn = j;
      if (pos == this.start)
      {
        firstResidueColumn = j;
      }
    }

    /*
     * update the cursor to the last residue position found (if any)
     * (converting column position to base 1)
     */
    if (lastPosFound != 0)
    {
      updateCursor(lastPosFound, lastPosFoundColumn + 1,
              firstResidueColumn + 1);
    }

    return pos;
  }

  /**
   * Answers true if the given cursor is not null, is for this sequence object,
   * and has a token value that matches this object's changeCount, else false.
   * This allows us to ignore a cursor as 'stale' if the sequence has been
   * modified since the cursor was created.
   * 
   * @param curs
   * @return
   */
  protected boolean isValidCursor(SequenceCursor curs)
  {
    if (curs == null || curs.sequence != this || curs.token != changeCount)
    {
      return false;
    }
    /*
     * sanity check against range
     */
    if (curs.columnPosition < 0 || curs.columnPosition > sequence.length)
    {
      return false;
    }
    if (curs.residuePosition < start || curs.residuePosition > end)
    {
      return false;
    }
    return true;
  }

  /**
   * Answers the sequence position (start..) for the given aligned column
   * position (1..), given a hint of a cursor in the neighbourhood. The cursor
   * may lie left of, at, or to the right of the column position.
   * 
   * @param col
   * @param curs
   * @return
   */
  protected int findPosition(final int col, SequenceCursor curs)
  {
    if (!isValidCursor(curs))
    {
      /*
       * wrong or invalidated cursor, compute de novo
       */
      return findPosition(col - 1);// ugh back to base 0
    }

    if (curs.columnPosition == col)
    {
      cursor = curs; // in case this method becomes public
      return curs.residuePosition; // easy case :-)
    }

    if (curs.lastColumnPosition > 0 && curs.lastColumnPosition < col)
    {
      /*
       * sequence lies entirely to the left of col
       * - return last residue + 1
       */
      return end + 1;
    }

    if (curs.firstColumnPosition > 0 && curs.firstColumnPosition > col)
    {
      /*
       * sequence lies entirely to the right of col
       * - return first residue
       */
      return start;
    }

    // todo could choose closest to col out of column,
    // firstColumnPosition, lastColumnPosition as a start point

    /*
     * move left or right to find pos from cursor position
     */
    int firstResidueColumn = curs.firstColumnPosition;
    int column = curs.columnPosition - 1; // to base 0
    int newPos = curs.residuePosition;
    int delta = curs.columnPosition > col ? -1 : 1;
    boolean gapped = false;
    int lastFoundPosition = curs.residuePosition;
    int lastFoundPositionColumn = curs.columnPosition;

    while (column != col - 1)
    {
      column += delta; // shift one column left or right
      if (column < 0 || column == sequence.length)
      {
        break;
      }
      gapped = Comparison.isGap(sequence[column]);
      if (!gapped)
      {
        newPos += delta;
        lastFoundPosition = newPos;
        lastFoundPositionColumn = column + 1;
        if (lastFoundPosition == this.start)
        {
          firstResidueColumn = column + 1;
        }
      }
    }

    if (cursor == null || lastFoundPosition != cursor.residuePosition)
    {
      updateCursor(lastFoundPosition, lastFoundPositionColumn,
              firstResidueColumn);
    }

    /*
     * hack to give position to the right if on a gap
     * or beyond the length of the sequence (see JAL-2562)
     */
    if (delta > 0 && (gapped || column >= sequence.length))
    {
      newPos++;
    }

    return newPos;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ContiguousI findPositions(int fromColumn, int toColumn)
  {
    if (toColumn < fromColumn || fromColumn < 1)
    {
      return null;
    }

    /*
     * find the first non-gapped position, if any
     */
    int firstPosition = 0;
    int col = fromColumn - 1;
    int length = sequence.length;
    while (col < length && col < toColumn)
    {
      if (!Comparison.isGap(sequence[col]))
      {
        firstPosition = findPosition(col++);
        break;
      }
      col++;
    }

    if (firstPosition == 0)
    {
      return null;
    }

    /*
     * find the last non-gapped position
     */
    int lastPosition = firstPosition;
    while (col < length && col < toColumn)
    {
      if (!Comparison.isGap(sequence[col++]))
      {
        lastPosition++;
      }
    }

    return new Range(firstPosition, lastPosition);
  }

  /**
   * Returns an int array where indices correspond to each residue in the
   * sequence and the element value gives its position in the alignment
   * 
   * @return int[SequenceI.getEnd()-SequenceI.getStart()+1] or null if no
   *         residues in SequenceI object
   */
  @Override
  public int[] gapMap()
  {
    String seq = jalview.analysis.AlignSeq.extractGaps(
            jalview.util.Comparison.GapChars, new String(sequence));
    int[] map = new int[seq.length()];
    int j = 0;
    int p = 0;

    while (j < sequence.length)
    {
      if (!jalview.util.Comparison.isGap(sequence[j]))
      {
        map[p++] = j;
      }

      j++;
    }

    return map;
  }

  /**
   * Build a bitset corresponding to sequence gaps
   * 
   * @return a BitSet where set values correspond to gaps in the sequence
   */
  @Override
  public BitSet gapBitset()
  {
    BitSet gaps = new BitSet(sequence.length);
    int j = 0;
    while (j < sequence.length)
    {
      if (jalview.util.Comparison.isGap(sequence[j]))
      {
        gaps.set(j);
      }
      j++;
    }
    return gaps;
  }

  @Override
  public int[] findPositionMap()
  {
    int map[] = new int[sequence.length];
    int j = 0;
    int pos = start;
    int seqlen = sequence.length;
    while ((j < seqlen))
    {
      map[j] = pos;
      if (!jalview.util.Comparison.isGap(sequence[j]))
      {
        pos++;
      }

      j++;
    }
    return map;
  }

  @Override
  public List<int[]> getInsertions()
  {
    ArrayList<int[]> map = new ArrayList<>();
    int lastj = -1, j = 0;
    // int pos = start;
    int seqlen = sequence.length;
    while ((j < seqlen))
    {
      if (jalview.util.Comparison.isGap(sequence[j]))
      {
        if (lastj == -1)
        {
          lastj = j;
        }
      }
      else
      {
        if (lastj != -1)
        {
          map.add(new int[] { lastj, j - 1 });
          lastj = -1;
        }
      }
      j++;
    }
    if (lastj != -1)
    {
      map.add(new int[] { lastj, j - 1 });
      lastj = -1;
    }
    return map;
  }

  @Override
  public BitSet getInsertionsAsBits()
  {
    BitSet map = new BitSet();
    int lastj = -1, j = 0;
    // int pos = start;
    int seqlen = sequence.length;
    while ((j < seqlen))
    {
      if (jalview.util.Comparison.isGap(sequence[j]))
      {
        if (lastj == -1)
        {
          lastj = j;
        }
      }
      else
      {
        if (lastj != -1)
        {
          map.set(lastj, j);
          lastj = -1;
        }
      }
      j++;
    }
    if (lastj != -1)
    {
      map.set(lastj, j);
      lastj = -1;
    }
    return map;
  }

  @Override
  public void deleteChars(final int i, final int j)
  {
    int newstart = start, newend = end;
    if (i >= sequence.length || i < 0)
    {
      return;
    }

    char[] tmp = StringUtils.deleteChars(sequence, i, j);
    boolean createNewDs = false;
    // TODO: take a (second look) at the dataset creation validation method for
    // the very large sequence case

    int startIndex = findIndex(start) - 1;
    int endIndex = findIndex(end) - 1;
    int startDeleteColumn = -1; // for dataset sequence deletions
    int deleteCount = 0;

    for (int s = i; s < j && s < sequence.length; s++)
    {
      if (Comparison.isGap(sequence[s]))
      {
        continue;
      }
      deleteCount++;
      if (startDeleteColumn == -1)
      {
        startDeleteColumn = findPosition(s) - start;
      }
      if (createNewDs)
      {
        newend--;
      }
      else
      {
        if (startIndex == s)
        {
          /*
           * deleting characters from start of sequence; new start is the
           * sequence position of the next column (position to the right
           * if the column position is gapped)
           */
          newstart = findPosition(j);
          break;
        }
        else
        {
          if (endIndex < j)
          {
            /*
             * deleting characters at end of sequence; new end is the sequence
             * position of the column before the deletion; subtract 1 if this is
             * gapped since findPosition returns the next sequence position
             */
            newend = findPosition(i - 1);
            if (Comparison.isGap(sequence[i - 1]))
            {
              newend--;
            }
            break;
          }
          else
          {
            createNewDs = true;
            newend--;
          }
        }
      }
    }

    if (createNewDs && this.datasetSequence != null)
    {
      /*
       * if deletion occured in the middle of the sequence,
       * construct a new dataset sequence and delete the residues
       * that were deleted from the aligned sequence
       */
      Sequence ds = new Sequence(datasetSequence);
      ds.deleteChars(startDeleteColumn, startDeleteColumn + deleteCount);
      datasetSequence = ds;
      // TODO: remove any non-inheritable properties ?
      // TODO: create a sequence mapping (since there is a relation here ?)
    }
    start = newstart;
    end = newend;
    sequence = tmp;
    sequenceChanged();
  }

  @Override
  public void insertCharAt(int i, int length, char c)
  {
    char[] tmp = new char[sequence.length + length];

    if (i >= sequence.length)
    {
      System.arraycopy(sequence, 0, tmp, 0, sequence.length);
      i = sequence.length;
    }
    else
    {
      System.arraycopy(sequence, 0, tmp, 0, i);
    }

    int index = i;
    while (length > 0)
    {
      tmp[index++] = c;
      length--;
    }

    if (i < sequence.length)
    {
      System.arraycopy(sequence, i, tmp, index, sequence.length - i);
    }

    sequence = tmp;
    sequenceChanged();
  }

  @Override
  public void insertCharAt(int i, char c)
  {
    insertCharAt(i, 1, c);
  }

  @Override
  public String getVamsasId()
  {
    return vamsasId;
  }

  @Override
  public void setVamsasId(String id)
  {
    vamsasId = id;
  }

  @Deprecated
  @Override
  public void setDBRefs(DBModList<DBRefEntry> newDBrefs)
  {
    if (dbrefs == null && datasetSequence != null
            && this != datasetSequence)
    {
      datasetSequence.setDBRefs(newDBrefs);
      return;
    }
    dbrefs = newDBrefs;
    refModCount = 0;
  }

  @Override
  public DBModList<DBRefEntry> getDBRefs()
  {
    if (dbrefs == null && datasetSequence != null
            && this != datasetSequence)
    {
      return datasetSequence.getDBRefs();
    }
    return dbrefs;
  }

  @Override
  public void addDBRef(DBRefEntry entry)
  {
    // TODO JAL-3980 maintain as sorted list
    if (datasetSequence != null)
    {
      datasetSequence.addDBRef(entry);
      return;
    }

    if (dbrefs == null)
    {
      dbrefs = new DBModList<>();
    }
    // TODO JAL-3979 LOOK UP RATHER THAN SWEEP FOR EFFICIENCY

    for (int ib = 0, nb = dbrefs.size(); ib < nb; ib++)
    {
      if (dbrefs.get(ib).updateFrom(entry))
      {
        /*
         * found a dbref that either matched, or could be
         * updated from, the new entry - no need to add it
         */
        return;
      }
    }

    // /// BH OUCH!
    // /*
    // * extend the array to make room for one more
    // */
    // // TODO use an ArrayList instead
    // int j = dbrefs.length;
    // List<DBRefEntry> temp = new DBRefEntry[j + 1];
    // System.arraycopy(dbrefs, 0, temp, 0, j);
    // temp[temp.length - 1] = entry;
    //
    // dbrefs = temp;

    dbrefs.add(entry);
  }

  @Override
  public void setDatasetSequence(SequenceI seq)
  {
    if (seq == this)
    {
      throw new IllegalArgumentException(
              "Implementation Error: self reference passed to SequenceI.setDatasetSequence");
    }
    if (seq != null && seq.getDatasetSequence() != null)
    {
      throw new IllegalArgumentException(
              "Implementation error: cascading dataset sequences are not allowed.");
    }
    datasetSequence = seq;
  }

  @Override
  public SequenceI getDatasetSequence()
  {
    return datasetSequence;
  }

  @Override
  public AlignmentAnnotation[] getAnnotation()
  {
    return annotation == null ? null
            : annotation
                    .toArray(new AlignmentAnnotation[annotation.size()]);
  }

  @Override
  public boolean hasAnnotation(AlignmentAnnotation ann)
  {
    return annotation == null ? false : annotation.contains(ann);
  }

  @Override
  public void addAlignmentAnnotation(AlignmentAnnotation annotation)
  {
    if (this.annotation == null)
    {
      this.annotation = new Vector<>();
    }
    if (!this.annotation.contains(annotation))
    {
      this.annotation.addElement(annotation);
    }
    annotation.setSequenceRef(this);
  }

  @Override
  public void removeAlignmentAnnotation(AlignmentAnnotation annotation)
  {
    if (this.annotation != null)
    {
      this.annotation.removeElement(annotation);
      if (this.annotation.size() == 0)
      {
        this.annotation = null;
      }
    }
  }

  /**
   * test if this is a valid candidate for another sequence's dataset sequence.
   * 
   */
  private boolean isValidDatasetSequence()
  {
    if (datasetSequence != null)
    {
      return false;
    }
    for (int i = 0; i < sequence.length; i++)
    {
      if (jalview.util.Comparison.isGap(sequence[i]))
      {
        return false;
      }
    }
    return true;
  }

  @Override
  public SequenceI deriveSequence()
  {
    Sequence seq = null;
    if (datasetSequence == null)
    {
      if (isValidDatasetSequence())
      {
        // Use this as dataset sequence
        seq = new Sequence(getName(), "", 1, -1);
        seq.setDatasetSequence(this);
        seq.initSeqFrom(this, getAnnotation());
        return seq;
      }
      else
      {
        // Create a new, valid dataset sequence
        createDatasetSequence();
      }
    }
    return new Sequence(this);
  }

  private boolean _isNa;

  private int _seqhash = 0;

  private List<DBRefEntry> primaryRefs;

  /**
   * Answers false if the sequence is more than 85% nucleotide (ACGTU), else
   * true
   */
  @Override
  public boolean isProtein()
  {
    if (datasetSequence != null)
    {
      return datasetSequence.isProtein();
    }
    if (_seqhash != sequence.hashCode())
    {
      _seqhash = sequence.hashCode();
      _isNa = Comparison.isNucleotide(this);
    }
    return !_isNa;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.SequenceI#createDatasetSequence()
   */
  @Override
  public SequenceI createDatasetSequence()
  {
    if (datasetSequence == null)
    {
      Sequence dsseq = new Sequence(getName(),
              AlignSeq.extractGaps(jalview.util.Comparison.GapChars,
                      getSequenceAsString()),
              getStart(), getEnd());

      datasetSequence = dsseq;

      dsseq.setDescription(description);
      // move features and database references onto dataset sequence
      dsseq.sequenceFeatureStore = sequenceFeatureStore;
      sequenceFeatureStore = null;
      dsseq.dbrefs = dbrefs;
      dbrefs = null;
      // TODO: search and replace any references to this sequence with
      // references to the dataset sequence in Mappings on dbref
      dsseq.pdbIds = pdbIds;
      pdbIds = null;
      datasetSequence.updatePDBIds();
      if (annotation != null)
      {
        // annotation is cloned rather than moved, to preserve what's currently
        // on the alignment
        for (AlignmentAnnotation aa : annotation)
        {
          AlignmentAnnotation _aa = new AlignmentAnnotation(aa);
          _aa.sequenceRef = datasetSequence;
          _aa.adjustForAlignment(); // uses annotation's own record of
                                    // sequence-column mapping
          datasetSequence.addAlignmentAnnotation(_aa);

          if (_cmholder != null)
          { // transfer contact matrices
            ContactMatrixI cm = _cmholder.getContactMatrixFor(aa);
            if (cm != null)
            {
              datasetSequence.addContactListFor(_aa, cm);
              datasetSequence.addContactListFor(aa, cm);
            }
          }
        }
      }
      // all matrices should have been transferred. so we clear the local holder
      _cmholder=null;
    }
    return datasetSequence;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.SequenceI#setAlignmentAnnotation(AlignmmentAnnotation[]
   * annotations)
   */
  @Override
  public void setAlignmentAnnotation(AlignmentAnnotation[] annotations)
  {
    if (annotation != null)
    {
      annotation.removeAllElements();
    }
    if (annotations != null)
    {
      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] != null)
        {
          addAlignmentAnnotation(annotations[i]);
        }
      }
    }
  }

  @Override
  public AlignmentAnnotation[] getAnnotation(String label)
  {
    if (annotation == null || annotation.size() == 0)
    {
      return null;
    }

    Vector<AlignmentAnnotation> subset = new Vector<>();
    Enumeration<AlignmentAnnotation> e = annotation.elements();
    while (e.hasMoreElements())
    {
      AlignmentAnnotation ann = e.nextElement();
      if (ann.label != null && ann.label.equals(label))
      {
        subset.addElement(ann);
      }
    }
    if (subset.size() == 0)
    {
      return null;
    }
    AlignmentAnnotation[] anns = new AlignmentAnnotation[subset.size()];
    int i = 0;
    e = subset.elements();
    while (e.hasMoreElements())
    {
      anns[i++] = e.nextElement();
    }
    subset.removeAllElements();
    return anns;
  }

  @Override
  public boolean updatePDBIds()
  {
    if (datasetSequence != null)
    {
      // TODO: could merge DBRefs
      return datasetSequence.updatePDBIds();
    }
    if (dbrefs == null || dbrefs.size() == 0)
    {
      return false;
    }
    boolean added = false;
    for (int ib = 0, nb = dbrefs.size(); ib < nb; ib++)
    {
      DBRefEntry dbr = dbrefs.get(ib);
      if (DBRefSource.PDB.equals(dbr.getSource()))
      {
        /*
         * 'Add' any PDB dbrefs as a PDBEntry - add is only performed if the
         * PDB id is not already present in a 'matching' PDBEntry
         * Constructor parses out a chain code if appended to the accession id
         * (a fudge used to 'store' the chain code in the DBRef)
         */
        PDBEntry pdbe = new PDBEntry(dbr);
        added |= addPDBId(pdbe);
      }
    }
    return added;
  }

  @Override
  public void transferAnnotation(SequenceI entry, Mapping mp)
  {
    if (datasetSequence != null)
    {
      datasetSequence.transferAnnotation(entry, mp);
      return;
    }
    if (entry.getDatasetSequence() != null)
    {
      transferAnnotation(entry.getDatasetSequence(), mp);
      return;
    }
    // transfer from entry to sequence
    // if entry has a description and sequence doesn't, then transfer
    if (entry.getDescription()!=null && (description==null || description.trim().length()==0))
    {
      description = entry.getDescription();
    }
    
    // transfer any new features from entry onto sequence
    if (entry.getSequenceFeatures() != null)
    {

      List<SequenceFeature> sfs = entry.getSequenceFeatures();
      for (SequenceFeature feature : sfs)
      {
        SequenceFeature sf[] = (mp != null) ? mp.locateFeature(feature)
                : new SequenceFeature[]
                { new SequenceFeature(feature) };
        if (sf != null)
        {
          for (int sfi = 0; sfi < sf.length; sfi++)
          {
            addSequenceFeature(sf[sfi]);
          }
        }
      }
    }

    // transfer PDB entries
    if (entry.getAllPDBEntries() != null)
    {
      Enumeration<PDBEntry> e = entry.getAllPDBEntries().elements();
      while (e.hasMoreElements())
      {
        PDBEntry pdb = e.nextElement();
        addPDBId(pdb);
      }
    }
    // transfer database references
    List<DBRefEntry> entryRefs = entry.getDBRefs();
    if (entryRefs != null)
    {
      for (int r = 0, n = entryRefs.size(); r < n; r++)
      {
        DBRefEntry newref = new DBRefEntry(entryRefs.get(r));
        if (newref.getMap() != null && mp != null)
        {
          // remap ref using our local mapping
        }
        // we also assume all version string setting is done by dbSourceProxy
        /*
         * if (!newref.getSource().equalsIgnoreCase(dbSource)) {
         * newref.setSource(dbSource); }
         */
        addDBRef(newref);
      }
    }
  }

  @Override
  public void setRNA(RNA r)
  {
    rna = r;
  }

  @Override
  public RNA getRNA()
  {
    return rna;
  }

  @Override
  public List<AlignmentAnnotation> getAlignmentAnnotations(String calcId,
          String label)
  {
    return getAlignmentAnnotations(calcId, label, null, true);
  }

  @Override
  public List<AlignmentAnnotation> getAlignmentAnnotations(String calcId,
          String label, String description)
  {
    return getAlignmentAnnotations(calcId, label, description, false);
  }

  private List<AlignmentAnnotation> getAlignmentAnnotations(String calcId,
          String label, String description, boolean ignoreDescription)
  {
    List<AlignmentAnnotation> result = new ArrayList<>();
    if (this.annotation != null)
    {
      for (AlignmentAnnotation ann : annotation)
      {
        if ((ann.calcId != null && ann.calcId.equals(calcId))
                && (ann.label != null && ann.label.equals(label))
                && ((ignoreDescription && description == null)
                        || (ann.description != null
                                && ann.description.equals(description))))

        {
          result.add(ann);
        }
      }
    }
    return result;
  }

  @Override
  public String toString()
  {
    return getDisplayId(false);
  }

  @Override
  public PDBEntry getPDBEntry(String pdbIdStr)
  {
    if (getDatasetSequence() != null)
    {
      return getDatasetSequence().getPDBEntry(pdbIdStr);
    }
    if (pdbIds == null)
    {
      return null;
    }
    List<PDBEntry> entries = getAllPDBEntries();
    for (PDBEntry entry : entries)
    {
      if (entry.getId().equalsIgnoreCase(pdbIdStr))
      {
        return entry;
      }
    }
    return null;
  }

  private List<DBRefEntry> tmpList;

  @Override
  public List<DBRefEntry> getPrimaryDBRefs()
  {
    if (datasetSequence != null)
    {
      return datasetSequence.getPrimaryDBRefs();
    }
    if (dbrefs == null || dbrefs.size() == 0)
    {
      return Collections.emptyList();
    }
    synchronized (dbrefs)
    {
      if (refModCount == dbrefs.getModCount() && primaryRefs != null)
      {
        return primaryRefs; // no changes
      }
      refModCount = dbrefs.getModCount();
      List<DBRefEntry> primaries = (primaryRefs == null
              ? (primaryRefs = new ArrayList<>())
              : primaryRefs);
      primaries.clear();
      if (tmpList == null)
      {
        tmpList = new ArrayList<>();
        tmpList.add(null); // for replacement
      }
      for (int i = 0, n = dbrefs.size(); i < n; i++)
      {
        DBRefEntry ref = dbrefs.get(i);
        if (!ref.isPrimaryCandidate())
        {
          continue;
        }
        if (ref.hasMap())
        {
          MapList mp = ref.getMap().getMap();
          if (mp.getFromLowest() > start || mp.getFromHighest() < end)
          {
            // map only involves a subsequence, so cannot be primary
            continue;
          }
        }
        // whilst it looks like it is a primary ref, we also sanity check type
        if (DBRefSource.PDB_CANONICAL_NAME
                .equals(ref.getCanonicalSourceName()))
        {
          // PDB dbrefs imply there should be a PDBEntry associated
          // TODO: tighten PDB dbrefs
          // formally imply Jalview has actually downloaded and
          // parsed the pdb file. That means there should be a cached file
          // handle on the PDBEntry, and a real mapping between sequence and
          // extracted sequence from PDB file
          PDBEntry pdbentry = getPDBEntry(ref.getAccessionId());
          if (pdbentry == null || pdbentry.getFile() == null)
          {
            continue;
          }
        }
        else
        {
          // check standard protein or dna sources
          tmpList.set(0, ref);
          List<DBRefEntry> res = DBRefUtils.selectDbRefs(!isProtein(),
                  tmpList);
          if (res == null || res.get(0) != tmpList.get(0))
          {
            continue;
          }
        }
        primaries.add(ref);
      }

      // version must be not null, as otherwise it will not be a candidate,
      // above
      DBRefUtils.ensurePrimaries(this, primaries);
      return primaries;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> findFeatures(int fromColumn, int toColumn,
          String... types)
  {
    int startPos = findPosition(fromColumn - 1); // convert base 1 to base 0
    int endPos = fromColumn == toColumn ? startPos
            : findPosition(toColumn - 1);

    List<SequenceFeature> result = getFeatures().findFeatures(startPos,
            endPos, types);

    /*
     * if end column is gapped, endPos may be to the right, 
     * and we may have included adjacent or enclosing features;
     * remove any that are not enclosing, non-contact features
     */
    boolean endColumnIsGapped = toColumn > 0 && toColumn <= sequence.length
            && Comparison.isGap(sequence[toColumn - 1]);
    if (endPos > this.end || endColumnIsGapped)
    {
      ListIterator<SequenceFeature> it = result.listIterator();
      while (it.hasNext())
      {
        SequenceFeature sf = it.next();
        int sfBegin = sf.getBegin();
        int sfEnd = sf.getEnd();
        int featureStartColumn = findIndex(sfBegin);
        if (featureStartColumn > toColumn)
        {
          it.remove();
        }
        else if (featureStartColumn < fromColumn)
        {
          int featureEndColumn = sfEnd == sfBegin ? featureStartColumn
                  : findIndex(sfEnd);
          if (featureEndColumn < fromColumn)
          {
            it.remove();
          }
          else if (featureEndColumn > toColumn && sf.isContactFeature())
          {
            /*
             * remove an enclosing feature if it is a contact feature
             */
            it.remove();
          }
        }
      }
    }

    return result;
  }

  /**
   * Invalidates any stale cursors (forcing recalculation) by incrementing the
   * token that has to match the one presented by the cursor
   */
  @Override
  public void sequenceChanged()
  {
    changeCount++;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int replace(char c1, char c2)
  {
    if (c1 == c2)
    {
      return 0;
    }
    int count = 0;
    synchronized (sequence)
    {
      for (int c = 0; c < sequence.length; c++)
      {
        if (sequence[c] == c1)
        {
          sequence[c] = c2;
          count++;
        }
      }
    }
    if (count > 0)
    {
      sequenceChanged();
    }

    return count;
  }

  @Override
  public String getSequenceStringFromIterator(Iterator<int[]> it)
  {
    StringBuilder newSequence = new StringBuilder();
    while (it.hasNext())
    {
      int[] block = it.next();
      if (it.hasNext())
      {
        newSequence.append(getSequence(block[0], block[1] + 1));
      }
      else
      {
        newSequence.append(getSequence(block[0], block[1]));
      }
    }

    return newSequence.toString();
  }

  @Override
  public int firstResidueOutsideIterator(Iterator<int[]> regions)
  {
    int start = 0;

    if (!regions.hasNext())
    {
      return findIndex(getStart()) - 1;
    }

    // Simply walk along the sequence whilst watching for region
    // boundaries
    int hideStart = getLength();
    int hideEnd = -1;
    boolean foundStart = false;

    // step through the non-gapped positions of the sequence
    for (int i = getStart(); i <= getEnd() && (!foundStart); i++)
    {
      // get alignment position of this residue in the sequence
      int p = findIndex(i) - 1;

      // update region start/end
      while (hideEnd < p && regions.hasNext())
      {
        int[] region = regions.next();
        hideStart = region[0];
        hideEnd = region[1];
      }
      if (hideEnd < p)
      {
        hideStart = getLength();
      }
      // update boundary for sequence
      if (p < hideStart)
      {
        start = p;
        foundStart = true;
      }
    }

    if (foundStart)
    {
      return start;
    }
    // otherwise, sequence was completely hidden
    return 0;
  }

  ////
  //// Contact Matrix Holder Boilerplate
  ////
  ContactMapHolderI _cmholder = null;

  private ContactMapHolderI getContactMapHolder()
  {
    if (datasetSequence!=null) {
      return ((Sequence)datasetSequence).getContactMapHolder();
    }
    if (_cmholder==null)
    {
      _cmholder=new ContactMapHolder();
    }
    return _cmholder;
  }
  @Override
  public Collection<ContactMatrixI> getContactMaps()
  {
    return getContactMapHolder().getContactMaps();
  }

  @Override
  public ContactMatrixI getContactMatrixFor(AlignmentAnnotation ann)
  {
    return getContactMapHolder().getContactMatrixFor(ann);
  }

  @Override
  public ContactListI getContactListFor(AlignmentAnnotation _aa, int column)
  {
    return getContactMapHolder().getContactListFor(_aa, column);
  }

  @Override
  public AlignmentAnnotation addContactList(ContactMatrixI cm)
  {
    AlignmentAnnotation aa;
    
    if (datasetSequence != null)
    {
      aa = datasetSequence.addContactList(cm);
      // clone the annotation for the local sequence
      aa = new AlignmentAnnotation(aa);
      aa.restrict(start, end);
      aa.adjustForAlignment();
      getContactMapHolder().addContactListFor(aa,cm);
      addAlignmentAnnotation(aa);
      return aa;
    }
    
    // construct new annotation for matrix on dataset sequence
    aa = getContactMapHolder().addContactList(cm);

    Annotation _aa[] = new Annotation[getLength()];

    for (int i = 0; i < _aa.length; _aa[i++] = new Annotation(0.0f))
    {
      ;
    }
    aa.annotations = _aa;
    aa.setSequenceRef(this);
    if (cm instanceof MappableContactMatrix
            && !((MappableContactMatrix) cm).hasReferenceSeq())
    {
      ((MappableContactMatrix) cm).setRefSeq(this);
    }
    aa.createSequenceMapping(this, getStart(), false);
    addAlignmentAnnotation(aa);
    return aa;
  }

  @Override
  public void addContactListFor(AlignmentAnnotation annotation,
          ContactMatrixI cm)
  {
    getContactMapHolder().addContactListFor(annotation, cm);
  }
}
