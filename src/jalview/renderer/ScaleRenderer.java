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
package jalview.renderer;

import jalview.api.AlignViewportI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Calculate and display alignment rulers
 * 
 * @author jprocter
 *
 */
public class ScaleRenderer
{
  /**
   * Represents one major or minor scale mark
   */
  public final class ScaleMark
  {
    /**
     * true for a major scale mark, false for minor
     */
    public final boolean major;

    /**
     * visible column position (0..) e.g. 19
     */
    public final int column;

    /**
     * text (if any) to show e.g. "20"
     */
    public final String text;

    ScaleMark(boolean isMajor, int col, String txt)
    {
      major = isMajor;
      column = col;
      text = txt;
    }
  }

  /**
   * calculate positions markers on the alignment ruler
   * 
   * @param av
   * @param startx
   *          left-most column in visible view
   * @param endx
   *          - right-most column in visible view
   * @return List of ScaleMark holding boolean: true/false for major/minor mark,
   *         marker position in alignment column coords, a String to be rendered
   *         at the position (or null)
   */
  public List<ScaleMark> calculateMarks(AlignViewportI av, int startx,
          int endx)
  {
    int scalestartx = (startx / 10) * 10;

    SequenceI refSeq = av.getAlignment().getSeqrep();
    int refSp = 0;
    int refStartI = 0;
    int refEndI = -1;

    HiddenColumns hc = av.getAlignment().getHiddenColumns();

    if (refSeq != null)
    {
      // find bounds and set origin appropriately
      // locate first residue in sequence which is not hidden
      Iterator<int[]> it = hc.iterator();
      int index = refSeq.firstResidueOutsideIterator(it);
      refSp = hc.absoluteToVisibleColumn(index);

      refStartI = refSeq.findIndex(refSeq.getStart()) - 1;

      int seqlength = refSeq.getLength();
      // get sequence position past the end of the sequence
      int pastEndPos = refSeq.findPosition(seqlength + 1);
      refEndI = refSeq.findIndex(pastEndPos - 1) - 1;

      scalestartx = refSp + ((scalestartx - refSp) / 10) * 10;
    }

    if (refSeq == null && scalestartx % 10 == 0)
    {
      scalestartx += 5;
    }
    List<ScaleMark> marks = new ArrayList<>();
    String string;
    int refN, iadj;
    // todo: add a 'reference origin column' to set column number relative to
    for (int i = scalestartx; i <= endx; i += 5)
    {
      if (((i - refSp) % 10) == 0)
      {
        if (refSeq == null)
        {
          iadj = hc.visibleToAbsoluteColumn(i - 1) + 1;
          string = String.valueOf(iadj);
        }
        else
        {
          iadj = hc.visibleToAbsoluteColumn(i - 1);
          refN = refSeq.findPosition(iadj);
          // TODO show bounds if position is a gap
          // - ie L--R -> "1L|2R" for
          // marker
          if (iadj < refStartI)
          {
            string = String.valueOf(iadj - refStartI);
          }
          else if (iadj > refEndI)
          {
            string = "+" + String.valueOf(iadj - refEndI);
          }
          else
          {
            string = String.valueOf(refN) + refSeq.getCharAt(iadj);
          }
        }
        marks.add(new ScaleMark(true, i - startx - 1, string));
      }
      else
      {
        marks.add(new ScaleMark(false, i - startx - 1, null));
      }
    }
    return marks;
  }

}
