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

import jalview.analysis.AlignSeq;
import jalview.analysis.SeqsetUtils;
import jalview.util.MessageManager;
import jalview.util.ShiftList;

import java.util.Enumeration;
import java.util.Hashtable;

public class SeqCigar extends CigarSimple
{
  /**
   * start(inclusive) and end(exclusive) of subsequence on refseq
   */
  private int start, end;

  private SequenceI refseq = null;

  private Hashtable seqProps;

  /**
   * Reference dataset sequence for the cigar string
   * 
   * @return SequenceI
   */
  public SequenceI getRefSeq()
  {
    return refseq;
  }

  /**
   * 
   * @return int start index of cigar ops on refSeq
   */
  public int getStart()
  {
    return start;
  }

  /**
   * 
   * @return int end index (exclusive) of cigar ops on refSeq
   */
  public int getEnd()
  {
    return end;
  }

  /**
   * 
   * @param column
   * @return position in sequence for column (or -1 if no match state exists)
   */
  public int findPosition(int column)
  {
    int w = 0, ew, p = refseq.findPosition(start);
    if (column < 0)
    {
      return -1;
    }
    if (range != null)
    {
      for (int i = 0; i < length; i++)
      {
        if (operation[i] == M || operation[i] == D)
        {
          p += range[i];
        }
        if (operation[i] == M || operation[i] == I)
        {
          ew = w + range[i];
          if (column < ew)
          {
            if (operation[i] == I)
            {
              return -1;
            }
            return p - (ew - column);
          }
          w = ew;
        }
      }
    }
    return -1;
  }

  /**
   * Returns sequence as a string with cigar operations applied to it
   * 
   * @return String
   */
  @Override
  public String getSequenceString(char GapChar)
  {
    return (length == 0) ? ""
            : (String) getSequenceAndDeletions(
                    refseq.getSequenceAsString(start, end), GapChar)[0];
  }

  /**
   * recreates a gapped and edited version of RefSeq or null for an empty cigar
   * string
   * 
   * @return SequenceI
   */
  public SequenceI getSeq(char GapChar)
  {
    Sequence seq;
    if (refseq == null || length == 0)
    {
      return null;
    }
    Object[] edit_result = getSequenceAndDeletions(
            refseq.getSequenceAsString(start, end), GapChar);
    if (edit_result == null)
    {
      throw new Error(MessageManager.getString(
              "error.implementation_error_unexpected_null_from_get_sequence_and_deletions"));
    }
    int bounds[] = (int[]) edit_result[1];
    seq = new Sequence(refseq.getName(), (String) edit_result[0],
            refseq.getStart() + start + bounds[0], refseq.getStart() + start
                    + ((bounds[2] == 0) ? -1 : bounds[2]));
    seq.setDescription(refseq.getDescription());
    int sstart = seq.getStart(), send = seq.getEnd();
    // seq.checkValidRange(); probably not needed
    // recover local properties if present
    if (seqProps != null)
    {
      // this recovers dataset sequence reference as well as local features,
      // names, start/end settings.
      SeqsetUtils.SeqCharacterUnhash(seq, seqProps);
    }
    // ensure dataset sequence is up to date from local reference
    seq.setDatasetSequence(refseq);
    seq.setStart(sstart);
    seq.setEnd(send);
    return seq;
  }

  /*
   * We don't allow this - refseq is given at construction time only public void
   * setSeq(SequenceI seq) { this.seq = seq; }
   */
  /**
   * internal constructor - sets seq to a gapless sequence derived from seq and
   * prepends any 'D' operations needed to get to the first residue of seq.
   * 
   * @param seq
   *          SequenceI
   * @param initialDeletion
   *          true to mark initial dataset sequence residues as deleted in
   *          subsequence
   * @param _s
   *          index of first position in seq
   * @param _e
   *          index after last position in (possibly gapped) seq
   * @return true if gaps are present in seq
   */
  private boolean _setSeq(SequenceI seq, boolean initialDeletion, int _s,
          int _e)
  {
    boolean hasgaps = false;
    if (seq == null)
    {
      throw new Error(MessageManager
              .getString("error.implementation_error_set_seq_null"));
    }
    if (_s < 0)
    {
      throw new Error(MessageManager
              .formatMessage("error.implementation_error_s", new String[]
              { Integer.valueOf(_s).toString() }));
    }
    String seq_string = seq.getSequenceAsString();
    if (_e == 0 || _e < _s || _e > seq_string.length())
    {
      _e = seq_string.length();
    }
    // resolve start and end positions relative to ungapped reference sequence
    start = seq.findPosition(_s) - seq.getStart();
    end = seq.findPosition(_e) - seq.getStart();
    int l_ungapped = end - start;
    // Find correct sequence to reference and correct start and end - if
    // necessary
    SequenceI ds = seq.getDatasetSequence();
    if (ds == null)
    {
      // make a new dataset sequence
      String ungapped = AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars, new String(seq_string));
      l_ungapped = ungapped.length();
      // check that we haven't just duplicated an ungapped sequence.
      if (l_ungapped == seq.getLength())
      {
        ds = seq;
      }
      else
      {
        ds = new Sequence(seq.getName(), ungapped, seq.getStart(),
                seq.getStart() + ungapped.length() - 1);
        // JBPNote: this would be consistent but may not be useful
        // seq.setDatasetSequence(ds);
      }
    }
    // add in offset between seq and the dataset sequence
    if (ds.getStart() < seq.getStart())
    {
      int offset = seq.getStart() - ds.getStart();
      if (initialDeletion)
      {
        // absolute cigar string
        addDeleted(_s + offset);
        start = 0;
        end += offset;
      }
      else
      {
        // normal behaviour - just mark start and end subsequence
        start += offset;
        end += offset;

      }

    }

    // any gaps to process ?
    if (l_ungapped != (_e - _s))
    {
      hasgaps = true;
    }

    refseq = ds;
    // copy over local properties for the sequence instance of the refseq
    seqProps = SeqsetUtils.SeqCharacterHash(seq);
    // Check offsets
    if (end > ds.getLength())
    {
      throw new Error(MessageManager
              .getString("error.implementation_error_seqcigar_possible"));
      // end = ds.getLength();
    }

    return hasgaps;
  }

  /**
   * directly initialise a cigar object with a sequence of range, operation
   * pairs and a sequence to apply it to. operation and range should be relative
   * to the seq.getStart()'th residue of the dataset seq resolved from seq.
   * 
   * @param seq
   *          SequenceI
   * @param operation
   *          char[]
   * @param range
   *          int[]
   */
  public SeqCigar(SequenceI seq, char operation[], int range[])
  {
    super();
    if (seq == null)
    {
      throw new Error(
              MessageManager.getString("error.implmentation_bug_seq_null"));
    }
    if (operation.length != range.length)
    {
      throw new Error(MessageManager.getString(
              "error.implementation_bug_cigar_operation_list_range_list"));
    }

    if (operation != null)
    {
      this.operation = new char[operation.length + _inc_length];
      this.range = new int[operation.length + _inc_length];

      if (_setSeq(seq, false, 0, 0))
      {
        throw new Error(MessageManager.getString(
                "error.not_yet_implemented_cigar_object_from_cigar_string"));
      }
      for (int i = this.length, j = 0; j < operation.length; i++, j++)
      {
        char op = operation[j];
        if (op != M && op != I && op != D)
        {
          throw new Error(MessageManager.formatMessage(
                  "error.implementation_bug_cigar_operation", new String[]
                  { Integer.valueOf(j).toString(),
                      Integer.valueOf(op).toString(),
                      Integer.valueOf(M).toString(),
                      Integer.valueOf(I).toString(),
                      Integer.valueOf(D).toString() }));
        }
        this.operation[i] = op;
        this.range[i] = range[j];
      }
      this.length += operation.length;
    }
    else
    {
      this.operation = null;
      this.range = null;
      this.length = 0;
      if (_setSeq(seq, false, 0, 0))
      {
        throw new Error(MessageManager.getString(
                "error.not_yet_implemented_cigar_object_from_cigar_string"));
      }
    }
  }

  /**
   * add range matched residues to cigar string
   * 
   * @param range
   *          int
   */
  public void addMatch(int range)
  {
    this.addOperation(M, range);
  }

  /**
   * Adds insertion and match operations based on seq to the cigar up to the
   * endpos column of seq.
   * 
   * @param cigar
   *          CigarBase
   * @param seq
   *          SequenceI
   * @param startpos
   *          int
   * @param endpos
   *          int
   * @param initialDeletions
   *          if true then initial deletions will be added from start of seq to
   *          startpos
   */
  protected static void addSequenceOps(CigarBase cigar, SequenceI seq,
          int startpos, int endpos, boolean initialDeletions)
  {
    char op = '\0';
    int range = 0;
    int p = 0, res = seq.getLength();

    if (!initialDeletions)
    {
      p = startpos;
    }

    while (p <= endpos)
    {
      boolean isGap = (p < res)
              ? jalview.util.Comparison.isGap(seq.getCharAt(p))
              : true;
      if ((startpos <= p) && (p <= endpos))
      {
        if (isGap)
        {
          if (range > 0 && op != I)
          {
            cigar.addOperation(op, range);
            range = 0;
          }
          op = I;
          range++;
        }
        else
        {
          if (range > 0 && op != M)
          {
            cigar.addOperation(op, range);
            range = 0;
          }
          op = M;
          range++;
        }
      }
      else
      {
        if (!isGap)
        {
          if (range > 0 && op != D)
          {
            cigar.addOperation(op, range);
            range = 0;
          }
          op = D;
          range++;
        }
        else
        {
          // do nothing - insertions are not made in flanking regions
        }
      }
      p++;
    }
    if (range > 0)
    {
      cigar.addOperation(op, range);
    }
  }

  /**
   * create a cigar string for given sequence
   * 
   * @param seq
   *          SequenceI
   */
  public SeqCigar(SequenceI seq)
  {
    super();
    if (seq == null)
    {
      throw new Error(MessageManager
              .getString("error.implementation_error_for_new_cigar"));
    }
    _setSeq(seq, false, 0, 0);
    // there is still work to do
    addSequenceOps(this, seq, 0, seq.getLength() - 1, false);
  }

  /**
   * Create Cigar from a range of gaps and residues on a sequence object
   * 
   * @param seq
   *          SequenceI
   * @param start
   *          int - first column in range
   * @param end
   *          int - last column in range
   */
  public SeqCigar(SequenceI seq, int start, int end)
  {
    super();
    if (seq == null)
    {
      throw new Error(MessageManager
              .getString("error.implementation_error_for_new_cigar"));
    }
    _setSeq(seq, false, start, end + 1);
    // there is still work to do
    addSequenceOps(this, seq, start, end, false);
  }

  /**
   * Create a cigar object from a cigar string like '[<I|D|M><range>]+' Will
   * fail if the given seq already contains gaps (JBPNote: future implementation
   * will fix)
   * 
   * @param seq
   *          SequenceI object resolvable to a dataset sequence
   * @param cigarString
   *          String
   * @return Cigar
   */
  public static SeqCigar parseCigar(SequenceI seq, String cigarString)
          throws Exception
  {
    Object[] opsandrange = parseCigarString(cigarString);
    return new SeqCigar(seq, (char[]) opsandrange[0],
            (int[]) opsandrange[1]);
  }

  /**
   * create an alignment from the given array of cigar sequences and gap
   * character, and marking the given segments as visible in the given
   * hiddenColumns.
   * 
   * @param alseqs
   * @param gapCharacter
   * @param hidden
   *          - hiddenColumns where hidden regions are marked
   * @param segments
   *          - visible regions of alignment
   * @return SequenceI[]
   */
  public static SequenceI[] createAlignmentSequences(SeqCigar[] alseqs,
          char gapCharacter, HiddenColumns hidden, int[] segments)
  {
    SequenceI[] seqs = new SequenceI[alseqs.length];
    StringBuffer[] g_seqs = new StringBuffer[alseqs.length];
    String[] alseqs_string = new String[alseqs.length];
    Object[] gs_regions = new Object[alseqs.length];
    for (int i = 0; i < alseqs.length; i++)
    {
      alseqs_string[i] = alseqs[i].getRefSeq()
              .getSequenceAsString(alseqs[i].start, alseqs[i].end);
      gs_regions[i] = alseqs[i].getSequenceAndDeletions(alseqs_string[i],
              gapCharacter); // gapped sequence, {start, start col, end.
      // endcol}, hidden regions {{start, end, col}})
      if (gs_regions[i] == null)
      {
        throw new Error(MessageManager.formatMessage(
                "error.implementation_error_cigar_seq_no_operations",
                new String[]
                { Integer.valueOf(i).toString() }));
      }
      g_seqs[i] = new StringBuffer((String) ((Object[]) gs_regions[i])[0]); // the
      // visible
      // gapped
      // sequence
    }
    // Now account for insertions. (well - deletions)
    // this is complicated because we must keep track of shifted positions in
    // each sequence
    ShiftList shifts = new ShiftList();
    for (int i = 0; i < alseqs.length; i++)
    {
      Object[] gs_region = ((Object[]) ((Object[]) gs_regions[i])[2]);
      if (gs_region != null)

      {
        for (int hr = 0; hr < gs_region.length; hr++)
        {
          int[] region = (int[]) gs_region[hr];
          char[] insert = new char[region[1] - region[0] + 1];
          for (int s = 0; s < insert.length; s++)
          {
            insert[s] = gapCharacter;
          }
          int inspos = shifts.shift(region[2]); // resolve insertion position in
          // current alignment frame of
          // reference
          for (int s = 0; s < alseqs.length; s++)
          {
            if (s != i)
            {
              if (g_seqs[s].length() <= inspos)
              {
                // prefix insertion with more gaps.
                for (int l = inspos - g_seqs[s].length(); l > 0; l--)
                {
                  g_seqs[s].append(gapCharacter); // to debug - use a diffferent
                  // gap character here
                }
              }
              g_seqs[s].insert(inspos, insert);
            }
            else
            {
              g_seqs[s].insert(inspos,
                      alseqs_string[i].substring(region[0], region[1] + 1));
            }
          }
          shifts.addShift(region[2], insert.length); // update shift in
          // alignment frame of
          // reference
          if (segments == null)
          {
            // add a hidden column for this deletion
            hidden.hideColumns(inspos, inspos + insert.length - 1);
          }
        }
      }
    }
    for (int i = 0; i < alseqs.length; i++)
    {
      int[] bounds = ((int[]) ((Object[]) gs_regions[i])[1]);
      SequenceI ref = alseqs[i].getRefSeq();
      seqs[i] = new Sequence(ref.getName(), g_seqs[i].toString(),
              ref.getStart() + alseqs[i].start + bounds[0],
              ref.getStart() + alseqs[i].start
                      + (bounds[2] == 0 ? -1 : bounds[2]));
      seqs[i].setDatasetSequence(ref);
      seqs[i].setDescription(ref.getDescription());
    }
    if (segments != null)
    {
      for (int i = 0; i < segments.length; i += 3)
      {
        // int start=shifts.shift(segments[i]-1)+1;
        // int end=shifts.shift(segments[i]+segments[i+1]-1)-1;
        hidden.hideColumns(segments[i + 1],
                segments[i + 1] + segments[i + 2] - 1);
      }
    }
    return seqs;
  }

  /**
   * references to entities that this sequence cigar is associated with.
   */
  private Hashtable selGroups = null;

  public void setGroupMembership(Object group)
  {
    if (selGroups == null)
    {
      selGroups = new Hashtable();
    }
    selGroups.put(group, new int[0]);
  }

  /**
   * Test for and if present remove association to group.
   * 
   * @param group
   * @return true if group was associated and it was removed
   */
  public boolean removeGroupMembership(Object group)
  {
    if (selGroups != null && selGroups.containsKey(group))
    {
      selGroups.remove(group);
      return true;
    }
    return false;
  }

  /**
   * forget all associations for this sequence.
   */
  public void clearMemberships()
  {
    if (selGroups != null)
    {
      selGroups.clear();
    }
    selGroups = null;
  }

  /**
   * 
   * @return null or array of all associated entities
   */
  public Object[] getAllMemberships()
  {
    if (selGroups == null)
    {
      return null;
    }
    Object[] mmbs = new Object[selGroups.size()];
    Enumeration en = selGroups.keys();
    for (int i = 0; en.hasMoreElements(); i++)
    {
      mmbs[i] = en.nextElement();
    }
    return mmbs;
  }

  /**
   * Test for group membership
   * 
   * @param sgr
   *          - a selection group or some other object that may be associated
   *          with seqCigar
   * @return true if sgr is associated with this seqCigar
   */
  public boolean isMemberOf(Object sgr)
  {
    return (selGroups != null) && selGroups.get(sgr) != null;
  }
}
