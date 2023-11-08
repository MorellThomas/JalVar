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

import jalview.datamodel.SequenceI;

public class SlideSequencesCommand extends EditCommand
{
  boolean gapsInsertedBegin = false;

  public SlideSequencesCommand(String description, SequenceI[] seqsLeft,
          SequenceI[] seqsRight, int slideSize, char gapChar)
  {
    this.description = description;

    int lSize = seqsLeft.length;
    gapsInsertedBegin = false;
    int i, j;
    for (i = 0; i < lSize; i++)
    {
      for (j = 0; j < slideSize; j++)
      {
        if (!jalview.util.Comparison.isGap(seqsLeft[i].getCharAt(j)))
        {
          gapsInsertedBegin = true;
          break;
        }
      }
    }

    Edit e = null;

    if (!gapsInsertedBegin)
    {
      e = new Edit(Action.DELETE_GAP, seqsLeft, 0, slideSize, gapChar);
      setEdit(e);
    }
    else
    {
      e = new Edit(Action.INSERT_GAP, seqsRight, 0, slideSize, gapChar);
      setEdit(e);
    }

    performEdit(e, null);
  }

  public boolean getGapsInsertedBegin()
  {
    return gapsInsertedBegin;
  }

  public boolean appendSlideCommand(SlideSequencesCommand command)
  {
    boolean same = false;

    if (command.getEdit(0).seqs.length == getEdit(0).seqs.length)
    {
      same = true;
      for (int i = 0; i < command.getEdit(0).seqs.length; i++)
      {
        if (getEdit(0).seqs[i] != command.getEdit(0).seqs[i])
        {
          same = false;
        }
      }
    }

    if (same)
    {
      command.addEdit(getEdit(0));
    }

    return same;
  }
}
