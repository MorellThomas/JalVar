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

public class SequenceDummy extends Sequence
{
  public SequenceDummy(String sequenceId)
  {
    super(sequenceId, "THISAPLACEHOLDER");
  }

  private boolean dummy = true;

  /**
   * become a proxy for mseq, merging any existing annotation on this sequence
   * 
   * @param mseq
   */
  public void become(SequenceI mseq)
  {
    initSeqFrom(mseq, null);
    dummy = false;
  }

  /**
   * Test if the SequenceDummy has been promoted to a real sequence via
   * SequenceDummy.become
   * 
   * @return true if this is a placeholder and contains no actual sequence data
   */
  public boolean isDummy()
  {
    return dummy;
  }

  /**
   * Always suppress /start-end for display name as we don't know it
   */
  @Override
  public String getDisplayId(boolean jvsuffix)
  {
    // required for correct behaviour of SequenceIdMatcher
    return super.getDisplayId(false);
  }
}
