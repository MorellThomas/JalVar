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

/**
 * An interface that describes one matched region of an alignment, as one
 * contiguous portion of a single dataset sequence
 */
public interface SearchResultMatchI
{
  /**
   * Returns the matched sequence
   * 
   * @return
   */
  SequenceI getSequence();

  /**
   * Returns the start position of the match in the sequence (base 1)
   * 
   * @return
   */
  int getStart();

  /**
   * Returns the end position of the match in the sequence (base 1)
   * 
   * @return
   */
  int getEnd();

  /**
   * Answers true if this match is for the given sequence and includes (matches
   * or encloses) the given start-end range
   * 
   * @param seq
   * @param start
   * @param end
   * @return
   */
  boolean contains(SequenceI seq, int start, int end);
}