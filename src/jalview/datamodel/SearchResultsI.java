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

import java.util.BitSet;
import java.util.List;

/**
 * An interface describing the result of a search or other operation which
 * highlights matched regions of an alignment
 */
public interface SearchResultsI
{

  /**
   * Adds one region to the results (unless already added, to avoid duplicates)
   * 
   * @param seq
   * @param start
   * @param end
   * @return
   */
  SearchResultMatchI addResult(SequenceI seq, int start, int end);

  /**
   * Adds one ore more [start, end] ranges to the results (unless already added
   * to avoid duplicates). This method only increments the match count by 1.
   * This is for the case where a match spans ignored hidden residues - it is
   * formally two or more contiguous matches, but only counted as one match.
   * 
   * @param seq
   * @param positions
   */
  void addResult(SequenceI seq, int[] positions);

  /**
   * adds all match results in the argument to this set
   * 
   * @param toAdd
   */
  void addSearchResults(SearchResultsI toAdd);

  /**
   * Answers true if the search results include the given sequence (or its
   * dataset sequence), else false
   * 
   * @param sequence
   * @return
   */
  boolean involvesSequence(SequenceI sequence);

  /**
   * Returns an array of [from, to, from, to..] matched columns (base 0) between
   * the given start and end columns of the given sequence. Returns null if no
   * matches overlap the specified region.
   * <p>
   * Implementations should provide an optimised method to return locations to
   * highlight on a visible portion of an alignment.
   * 
   * @param sequence
   * @param start
   *          first column of range (base 0, inclusive)
   * @param end
   *          last column of range base 0, inclusive)
   * @return int[]
   */
  int[] getResults(SequenceI sequence, int start, int end);

  /**
   * Returns the number of matches found. Note that if a match straddles ignored
   * hidden residues, it is counted as one match, although formally recorded as
   * two (or more) contiguous matched sequence regions
   * 
   * @return
   */
  int getCount();

  /**
   * Returns true if no search result matches are held.
   * 
   * @return
   */
  boolean isEmpty();

  /**
   * Returns the list of matches.
   * 
   * @return
   */
  List<SearchResultMatchI> getResults();

  /**
   * Set bits in a bitfield for all columns in the given sequence collection
   * that are highlighted
   * 
   * @param sqcol
   *          the set of sequences to search for highlighted regions
   * @param bs
   *          bitset to set
   * @return number of bits set
   */
  int markColumns(SequenceCollectionI sqcol, BitSet bs);

  /**
   * Return sub-sequences corresponding to distinct contiguous ranges in the
   * matching set
   * 
   * @return list of sequence objects
   */
  List<SequenceI> getMatchingSubSequences();
}