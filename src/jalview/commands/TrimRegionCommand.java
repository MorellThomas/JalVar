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
package jalview.commands;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;

public class TrimRegionCommand extends EditCommand
{
  int columnsDeleted;

  /**
   * Constructs and performs a trim alignment command
   * 
   * @param description
   *          (to show in Undo/Redo menu)
   * @param trimLeft
   *          if true trim to left of column, else to right
   * @param seqs
   *          the sequences to trim
   * @param column
   *          the alignment column (base 0) from which to trim
   * @param al
   */
  public TrimRegionCommand(String description, boolean trimLeft,
          SequenceI[] seqs, int column, AlignmentI al)
  {
    this.description = description;
    if (trimLeft)
    {
      if (column == 0)
      {
        return;
      }

      columnsDeleted = column;

      setEdit(new Edit(Action.CUT, seqs, 0, column, al));
    }
    else
    {
      int width = al.getWidth() - column - 1;
      if (width < 1)
      {
        return;
      }

      columnsDeleted = width;

      setEdit(new Edit(Action.CUT, seqs, column + 1, width, al));
    }

    performEdit(0, null);
  }

  @Override
  public int getSize()
  {
    return columnsDeleted;
  }

}
