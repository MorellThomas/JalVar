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

import jalview.datamodel.SequenceI;

import java.util.List;

public interface StructureListener
{
  /**
   * Returns a list of structure files (unique IDs/filenames) that this listener
   * handles messages for, or null if generic listener (only used by
   * removeListener method)
   */
  public String[] getStructureFiles();

  /**
   * Called by StructureSelectionManager to inform viewer to highlight given
   * atom positions
   * 
   * @param atoms
   */
  public void highlightAtoms(List<AtomSpec> atoms);

  /**
   * Called by StructureSelectionManager when the colours of a sequence
   * associated with a structure have changed.
   * 
   * @param source
   *          (untyped) usually an alignPanel
   */
  public void updateColours(Object source);

  /**
   * Called by structureSelectionManager to instruct implementor to release any
   * direct references it may hold to the given object (typically, these are
   * Jalview alignment panels).
   * 
   * @param svl
   */
  public void releaseReferences(Object svl);

  /**
   * Answers true if this listener is interested in the given sequence
   * 
   * @param seq
   * @return
   */
  public boolean isListeningFor(SequenceI seq);
}
