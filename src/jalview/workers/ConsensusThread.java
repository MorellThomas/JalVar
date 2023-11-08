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

import jalview.analysis.AAFrequency;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SequenceI;
import jalview.renderer.ResidueShaderI;

public class ConsensusThread extends AlignCalcWorker
{
  public ConsensusThread(AlignViewportI alignViewport,
          AlignmentViewPanel alignPanel)
  {
    super(alignViewport, alignPanel);
  }

  @Override
  public void run()
  {
    if (calcMan.isPending(this))
    {
      return;
    }
    calcMan.notifyStart(this);
    // long started = System.currentTimeMillis();
    try
    {
      AlignmentAnnotation consensus = getConsensusAnnotation();
      AlignmentAnnotation gap = getGapAnnotation();
      if ((consensus == null && gap == null) || calcMan.isPending(this))
      {
        calcMan.workerComplete(this);
        return;
      }
      while (!calcMan.notifyWorking(this))
      {
        // System.err.println("Thread
        // (Consensus"+Thread.currentThread().getName()+") Waiting around.");
        try
        {
          if (ap != null)
          {
            ap.paintAlignment(false, false);
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

      eraseConsensus(aWidth);
      computeConsensus(alignment);
      updateResultAnnotation(true);

      if (ap != null)
      {
        ap.paintAlignment(true, true);
      }
    } catch (OutOfMemoryError error)
    {
      calcMan.disableWorker(this);
      ap.raiseOOMWarning("calculating consensus", error);
    } finally
    {
      /*
       * e.g. ArrayIndexOutOfBoundsException can happen due to a race condition
       * - alignment was edited at same time as calculation was running
       */
      calcMan.workerComplete(this);
    }
  }

  /**
   * Clear out any existing consensus annotations
   * 
   * @param aWidth
   *          the width (number of columns) of the annotated alignment
   */
  protected void eraseConsensus(int aWidth)
  {
    AlignmentAnnotation consensus = getConsensusAnnotation();
    if (consensus != null)
    {
      consensus.annotations = new Annotation[aWidth];
    }
    AlignmentAnnotation gap = getGapAnnotation();
    if (gap != null)
    {
      gap.annotations = new Annotation[aWidth];
    }
  }

  /**
   * @param alignment
   */
  protected void computeConsensus(AlignmentI alignment)
  {

    SequenceI[] aseqs = getSequences();
    int width = alignment.getWidth();
    ProfilesI hconsensus = AAFrequency.calculate(aseqs, width, 0, width,
            true);

    alignViewport.setSequenceConsensusHash(hconsensus);
    setColourSchemeConsensus(hconsensus);
  }

  /**
   * @return
   */
  protected SequenceI[] getSequences()
  {
    return alignViewport.getAlignment().getSequencesArray();
  }

  /**
   * @param hconsensus
   */
  protected void setColourSchemeConsensus(ProfilesI hconsensus)
  {
    ResidueShaderI cs = alignViewport.getResidueShading();
    if (cs != null)
    {
      cs.setConsensus(hconsensus);
    }
  }

  /**
   * Get the Consensus annotation for the alignment
   * 
   * @return
   */
  protected AlignmentAnnotation getConsensusAnnotation()
  {
    return alignViewport.getAlignmentConsensusAnnotation();
  }

  /**
   * Get the Gap annotation for the alignment
   * 
   * @return
   */
  protected AlignmentAnnotation getGapAnnotation()
  {
    return alignViewport.getAlignmentGapAnnotation();
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
    AlignmentAnnotation consensus = getConsensusAnnotation();
    ProfilesI hconsensus = (ProfilesI) getViewportConsensus();
    if (immediate || !calcMan.isWorking(this) && consensus != null
            && hconsensus != null)
    {
      deriveConsensus(consensus, hconsensus);
      AlignmentAnnotation gap = getGapAnnotation();
      if (gap != null)
      {
        deriveGap(gap, hconsensus);
      }
    }
  }

  /**
   * Convert the computed consensus data into the desired annotation for
   * display.
   * 
   * @param consensusAnnotation
   *          the annotation to be populated
   * @param hconsensus
   *          the computed consensus data
   */
  protected void deriveConsensus(AlignmentAnnotation consensusAnnotation,
          ProfilesI hconsensus)
  {

    long nseq = getSequences().length;
    AAFrequency.completeConsensus(consensusAnnotation, hconsensus,
            hconsensus.getStartColumn(), hconsensus.getEndColumn() + 1,
            alignViewport.isIgnoreGapsConsensus(),
            alignViewport.isShowSequenceLogo(), nseq);
  }

  /**
   * Convert the computed consensus data into a gap annotation row for display.
   * 
   * @param gapAnnotation
   *          the annotation to be populated
   * @param hconsensus
   *          the computed consensus data
   */
  protected void deriveGap(AlignmentAnnotation gapAnnotation,
          ProfilesI hconsensus)
  {
    long nseq = getSequences().length;
    AAFrequency.completeGapAnnot(gapAnnotation, hconsensus,
            hconsensus.getStartColumn(), hconsensus.getEndColumn() + 1,
            nseq);
  }

  /**
   * Get the consensus data stored on the viewport.
   * 
   * @return
   */
  protected Object getViewportConsensus()
  {
    // TODO convert ComplementConsensusThread to use Profile
    return alignViewport.getSequenceConsensusHash();
  }
}
