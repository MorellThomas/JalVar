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

import jalview.commands.CommandI;

/**
 * Defines a listener for commands performed on another alignment. This is to
 * support linked editing of two alternative representations of an alignment (in
 * particular, cDNA and protein).
 * 
 * @author gmcarstairs
 *
 */
public interface CommandListener
{
  /**
   * The listener may attempt to perform the specified command; the region acted
   * on is determined by a callback to the StructureSelectionManager (which
   * holds mappings between alignments).
   * 
   * @param command
   * @param undo
   * @param ssm
   * @param source
   *          the originator of the command
   */
  public void mirrorCommand(CommandI command, boolean undo,
          StructureSelectionManager ssm, VamsasSource source);

  /**
   * Temporary workaround to make check for source == listener work.
   * 
   * @return
   */
  public VamsasSource getVamsasSource();
}
