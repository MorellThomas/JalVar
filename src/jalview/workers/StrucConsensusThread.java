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

import jalview.analysis.StructureFrequency;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceI;

import java.util.Hashtable;

public class StrucConsensusThread extends AlignCalcWorker
{
  public StrucConsensusThread(AlignViewportI alignViewport,
          AlignmentViewPanel alignPanel)
  {
    super(alignViewport, alignPanel);
  }

  AlignmentAnnotation strucConsensus;

  Hashtable[] hStrucConsensus;

  private long nseq = -1;

  @Override
  public void run()
  {
    try
    {
      if (calcMan.isPending(this))
      {
        return;
      }
      calcMan.notifyStart(this);
      while (!calcMan.notifyWorking(this))
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
      if (alignViewport.isClosed())
      {
        abortAndDestroy();
        return;
      }
      AlignmentI alignment = alignViewport.getAlignment();

      int aWidth = -1;

      if (alignment == null || (aWidth = alignment.getWidth()) < 0)
      {
        calcMan.workerComplete(this);
        return;
      }
      strucConsensus = alignViewport.getAlignmentStrucConsensusAnnotation();
      hStrucConsensus = alignViewport.getRnaStructureConsensusHash();
      strucConsensus.annotations = null;
      strucConsensus.annotations = new Annotation[aWidth];

      hStrucConsensus = new Hashtable[aWidth];

      AlignmentAnnotation[] aa = alignViewport.getAlignment()
              .getAlignmentAnnotation();
      AlignmentAnnotation rnaStruc = null;
      // select rna struct to use for calculation
      if (aa != null)
      {
        for (int i = 0; i < aa.length; i++)
        {
          if (aa[i].visible && aa[i].isRNA() && aa[i].isValidStruc())
          {
            rnaStruc = aa[i];
            break;
          }
        }
      }
      // check to see if its valid

      if (rnaStruc == null || !rnaStruc.isValidStruc())
      {
        calcMan.workerComplete(this);
        return;
      }

      try
      {
        final SequenceI[] arr = alignment.getSequencesArray();
        nseq = arr.length;
        jalview.analysis.StructureFrequency.calculate(arr, 0,
                alignment.getWidth(), hStrucConsensus, true, rnaStruc);
      } catch (ArrayIndexOutOfBoundsException x)
      {
        calcMan.workerComplete(this);
        return;
      }
      alignViewport.setRnaStructureConsensusHash(hStrucConsensus);
      // TODO AlignmentAnnotation rnaStruc!!!
      updateResultAnnotation(true);
    } catch (OutOfMemoryError error)
    {
      calcMan.disableWorker(this);

      // consensus = null;
      // hconsensus = null;
      ap.raiseOOMWarning("calculating RNA structure consensus", error);
    } finally
    {
      calcMan.workerComplete(this);
      if (ap != null)
      {
        ap.paintAlignment(true, true);
      }
    }

  }

  /**
   * update the consensus annotation from the sequence profile data using
   * current visualization settings.
   */
  @Override
  public void updateAnnotation()
  {
    updateResultAnnotation(false);
  }

  public void updateResultAnnotation(boolean immediate)
  {
    if (immediate || !calcMan.isWorking(this) && strucConsensus != null
            && hStrucConsensus != null)
    {
      StructureFrequency.completeConsensus(strucConsensus, hStrucConsensus,
              0, hStrucConsensus.length,
              alignViewport.isIgnoreGapsConsensus(),
              alignViewport.isShowSequenceLogo(), nseq);
    }
  }

}
