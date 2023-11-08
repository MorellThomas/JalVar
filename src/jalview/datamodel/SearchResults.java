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
import java.util.BitSet;
import java.util.List;

/**
 * Holds a list of search result matches, where each match is a contiguous
 * stretch of a single sequence.
 * 
 * @author gmcarstairs amwaterhouse
 *
 */
public class SearchResults implements SearchResultsI
{
  private int count;

  private List<SearchResultMatchI> matches = new ArrayList<>();

  /**
   * One match consists of a sequence reference, start and end positions.
   * Discontiguous ranges in a sequence require two or more Match objects.
   */
  public class Match implements SearchResultMatchI
  {
    final SequenceI sequence;

    /**
     * Start position of match in sequence (base 1)
     */
    final int start;

    /**
     * End position (inclusive) (base 1)
     */
    final int end;

    /**
     * create a Match on a range of sequence. Match always holds region in
     * forwards order, even if given in reverse order (such as from a mapping to
     * a reverse strand); this avoids trouble for routines that highlight search
     * results etc
     * 
     * @param seq
     *          a sequence
     * @param start
     *          start position of matched range (base 1)
     * @param end
     *          end of matched range (inclusive, base 1)
     */
    public Match(SequenceI seq, int start, int end)
    {
      sequence = seq;

      /*
       * always hold in forwards order, even if given in reverse order
       * (such as from a mapping to a reverse strand); this avoids
       * trouble for routines that highlight search results etc
       */
      if (start <= end)
      {
        this.start = start;
        this.end = end;
      }
      else
      {
        // TODO: JBP could mark match as being specified in reverse direction
        // for use
        // by caller ? e.g. visualizing reverse strand highlight
        this.start = end;
        this.end = start;
      }
    }

    @Override
    public SequenceI getSequence()
    {
      return sequence;
    }

    @Override
    public int getStart()
    {
      return start;
    }

    @Override
    public int getEnd()
    {
      return end;
    }

    /**
     * Returns a representation as "seqid/start-end"
     */
    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      if (sequence != null)
      {
        sb.append(sequence.getName()).append("/");
      }
      sb.append(start).append("-").append(end);
      return sb.toString();
    }

    /**
     * Hashcode is the hashcode of the matched sequence plus a hash of start and
     * end positions. Match objects that pass the test for equals are guaranteed
     * to have the same hashcode.
     */
    @Override
    public int hashCode()
    {
      int hash = sequence == null ? 0 : sequence.hashCode();
      hash += 31 * start;
      hash += 67 * end;
      return hash;
    }

    /**
     * Two Match objects are equal if they are for the same sequence, start and
     * end positions
     */
    @Override
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof SearchResultMatchI))
      {
        return false;
      }
      SearchResultMatchI m = (SearchResultMatchI) obj;
      return (sequence == m.getSequence() && start == m.getStart()
              && end == m.getEnd());
    }

    @Override
    public boolean contains(SequenceI seq, int from, int to)
    {
      return (sequence == seq && start <= from && end >= to);
    }
  }

  @Override
  public SearchResultMatchI addResult(SequenceI seq, int start, int end)
  {
    Match m = new Match(seq, start, end);
    if (!matches.contains(m))
    {
      matches.add(m);
      count++;
    }
    return m;
  }

  @Override
  public void addResult(SequenceI seq, int[] positions)
  {
    /*
     * we only increment the match count by 1 - or not at all,
     * if the matches are all duplicates of existing
     */
    int beforeCount = count;
    for (int i = 0; i < positions.length - 1; i += 2)
    {
      addResult(seq, positions[i], positions[i + 1]);
    }
    if (count > beforeCount)
    {
      count = beforeCount + 1;
    }
  }

  @Override
  public boolean involvesSequence(SequenceI sequence)
  {
    final int start = sequence.getStart();
    final int end = sequence.getEnd();

    SequenceI ds = sequence.getDatasetSequence();
    for (SearchResultMatchI m : matches)
    {
      SequenceI matched = m.getSequence();
      if (matched != null && (matched == sequence || matched == ds)
              && (m.getEnd() >= start) && (m.getStart() <= end))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public int[] getResults(SequenceI sequence, int start, int end)
  {
    if (matches.isEmpty())
    {
      return null;
    }

    int[] result = null;
    int[] tmp = null;
    int resultLength, matchStart = 0, matchEnd = 0;
    boolean mfound;
    Match m;
    for (SearchResultMatchI _m : matches)
    {
      m = (Match) _m;

      mfound = false;
      if (m.sequence == sequence
              || m.sequence == sequence.getDatasetSequence())
      {
        mfound = true;
        matchStart = sequence.findIndex(m.start) - 1;
        matchEnd = m.start == m.end ? matchStart
                : sequence.findIndex(m.end) - 1;
      }

      if (mfound)
      {
        if (matchStart <= end && matchEnd >= start)
        {
          if (matchStart < start)
          {
            matchStart = start;
          }

          if (matchEnd > end)
          {
            matchEnd = end;
          }

          if (result == null)
          {
            result = new int[] { matchStart, matchEnd };
          }
          else
          {
            resultLength = result.length;
            tmp = new int[resultLength + 2];
            System.arraycopy(result, 0, tmp, 0, resultLength);
            result = tmp;
            result[resultLength] = matchStart;
            result[resultLength + 1] = matchEnd;
          }
        }
        else
        {
          // debug
          // System.err.println("Outwith bounds!" + matchStart+">"+end +" or "
          // + matchEnd+"<"+start);
        }
      }
    }
    return result;
  }

  @Override
  public int markColumns(SequenceCollectionI sqcol, BitSet bs)
  {
    int count = 0;
    BitSet mask = new BitSet();
    int startRes = sqcol.getStartRes();
    int endRes = sqcol.getEndRes();

    for (SequenceI s : sqcol.getSequences())
    {
      int[] cols = getResults(s, startRes, endRes);
      if (cols != null)
      {
        for (int pair = 0; pair < cols.length; pair += 2)
        {
          mask.set(cols[pair], cols[pair + 1] + 1);
        }
      }
    }
    // compute columns that were newly selected
    BitSet original = (BitSet) bs.clone();
    original.and(mask);
    count = mask.cardinality() - original.cardinality();
    // and mark ranges not already marked
    bs.or(mask);
    return count;
  }

  @Override
  public int getCount()
  {
    return count;
  }

  @Override
  public boolean isEmpty()
  {
    return matches.isEmpty();
  }

  @Override
  public List<SearchResultMatchI> getResults()
  {
    return matches;
  }

  /**
   * Return the results as a list of matches [seq1/from-to, seq2/from-to, ...]
   * 
   * @return
   */
  @Override
  public String toString()
  {
    return matches == null ? "" : matches.toString();
  }

  /**
   * Hashcode is derived from the list of matches. This ensures that when two
   * SearchResults objects satisfy the test for equals(), then they have the
   * same hashcode.
   * 
   * @see Match#hashCode()
   * @see java.util.AbstractList#hashCode()
   */
  @Override
  public int hashCode()
  {
    return matches.hashCode();
  }

  /**
   * Two SearchResults are considered equal if they contain the same matches
   * (Sequence, start position, end position) in the same order
   * 
   * @see Match#equals(Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof SearchResultsI))
    {
      return false;
    }
    SearchResultsI sr = (SearchResultsI) obj;
    return matches.equals(sr.getResults());
  }

  @Override
  public void addSearchResults(SearchResultsI toAdd)
  {
    matches.addAll(toAdd.getResults());
  }

  @Override
  public List<SequenceI> getMatchingSubSequences()
  {
    List<SequenceI> seqs = new ArrayList<>();

    /*
     * assemble dataset sequences, and template new sequence features,
     * for the amend features dialog
     */
    for (SearchResultMatchI match : matches)
    {
      SequenceI seq = match.getSequence();
      while (seq.getDatasetSequence() != null)
      {
        seq = seq.getDatasetSequence();
      }
      // getSubSequence is index-base0, findIndex returns index-base1
      seqs.add(seq.getSubSequence(seq.findIndex(match.getStart()) - 1,
              seq.findIndex(match.getEnd())));
    }
    return seqs;
  }
}
