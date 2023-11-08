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

public class RemoveGapColCommand extends EditCommand
{
  int columnsDeleted;

  public RemoveGapColCommand(String description, SequenceI[] seqs,
          int start, int end, AlignmentI al)
  {
    this.description = description;

    int j, jSize = seqs.length;

    int startCol = -1, endCol = -1;
    columnsDeleted = 0;

    clearEdits();

    boolean delete = true;
    for (int i = start; i <= end; i++)
    {
      delete = true;

      for (j = 0; j < jSize; j++)
      {
        if (seqs[j].getLength() > i)
        {
          if (!jalview.util.Comparison.isGap(seqs[j].getCharAt(i)))
          {
            if (delete)
            {
              endCol = i;
            }

            delete = false;
            break;
          }
        }
      }

      if (delete && startCol == -1)
      {
        startCol = i;
      }

      if (!delete && startCol > -1)
      {
        this.appendEdit(Action.DELETE_GAP, seqs, startCol - columnsDeleted,
                endCol - startCol, al, false, null);

        columnsDeleted += (endCol - startCol);
        startCol = -1;
        endCol = -1;
      }
    }

    if (delete && startCol > -1)
    {
      // This is for empty columns at the
      // end of the alignment

      this.appendEdit(Action.DELETE_GAP, seqs, startCol - columnsDeleted,
              end - startCol + 1, al, false, null);

      columnsDeleted += (end - startCol + 1);
    }

    performEdit(0, null);
  }

  @Override
  public int getSize()
  {
    // We're interested in the number of columns deleted,
    // Not the number of sequence edits.
    return columnsDeleted;
  }

}
