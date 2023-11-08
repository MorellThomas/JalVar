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
package jalview.workers;

import jalview.analysis.Conservation;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;

import java.util.ArrayList;
import java.util.List;

public class ConservationThread extends AlignCalcWorker
{

  private int ConsPercGaps = 25; // JBPNote : This should be a configurable
                                 // property!

  public ConservationThread(AlignViewportI alignViewport,
          AlignmentViewPanel alignPanel)
  {
    super(alignViewport, alignPanel);
    ConsPercGaps = alignViewport.getConsPercGaps();
  }

  private Conservation cons;

  AlignmentAnnotation conservation, quality;

  int alWidth;

  @Override
  public void run()
  {
    try
    {
      calcMan.notifyStart(this); // updatingConservation = true;

      while ((calcMan != null) && (!calcMan.notifyWorking(this)))
      {
        try
        {
          if (ap != null)
          {
            // ap.paintAlignment(false);
          }
          Thread.sleep(200);
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      if ((alignViewport == null) || (calcMan == null)
              || (alignViewport.isClosed()))
      {
        abortAndDestroy();
        return;
      }
      List<AlignmentAnnotation> ourAnnot = new ArrayList<>();
      AlignmentI alignment = alignViewport.getAlignment();
      conservation = alignViewport.getAlignmentConservationAnnotation();
      quality = alignViewport.getAlignmentQualityAnnot();
      ourAnnot.add(conservation);
      ourAnnot.add(quality);
      ourAnnots = ourAnnot;
      ConsPercGaps = alignViewport.getConsPercGaps();
      // AlignViewport.UPDATING_CONSERVATION = true;

      if (alignment == null || (alWidth = alignment.getWidth()) < 0)
      {
        calcMan.workerComplete(this);
        // .updatingConservation = false;
        // AlignViewport.UPDATING_CONSERVATION = false;

        return;
      }
      try
      {
        cons = Conservation.calculateConservation("All",
                alignment.getSequences(), 0, alWidth - 1, false,
                ConsPercGaps, quality != null);
      } catch (IndexOutOfBoundsException x)
      {
        // probable race condition. just finish and return without any fuss.
        calcMan.workerComplete(this);
        return;
      }
      updateResultAnnotation(true);
    } catch (OutOfMemoryError error)
    {
      ap.raiseOOMWarning("calculating conservation", error);
      calcMan.disableWorker(this);
      // alignViewport.conservation = null;
      // this.alignViewport.quality = null;

    }
    calcMan.workerComplete(this);

    if ((alignViewport == null) || (calcMan == null)
            || (alignViewport.isClosed()))
    {
      abortAndDestroy();
      return;
    }
    if (ap != null)
    {
      ap.paintAlignment(true, true);
    }

  }

  private void updateResultAnnotation(boolean b)
  {
    if (b || !calcMan.isWorking(this) && cons != null
            && conservation != null && quality != null)
    {
      alignViewport.setConservation(cons);
      cons.completeAnnotations(conservation, quality, 0, alWidth);
    }
  }

  @Override
  public void updateAnnotation()
  {
    updateResultAnnotation(false);

  }
}
