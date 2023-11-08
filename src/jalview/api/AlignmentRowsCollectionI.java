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

public interface AlignmentRowsCollectionI extends Iterable<Integer>
{
  /**
   * Answers if the sequence at the given position is hidden.
   * 
   * @param r
   *          the row index to check
   * @return true if the sequence at r is hidden
   */
  public boolean isHidden(int r);

  /**
   * Answers if any row in this collection is hidden
   * 
   * @return true if there is at least 1 hidden row
   */
  public boolean hasHidden();

  /**
   * Answers the sequence at the given position in the alignment
   * 
   * @param r
   *          the row index to locate
   * @return the sequence
   */
  public SequenceI getSequence(int r);
}
