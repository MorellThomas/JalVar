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

import jalview.analysis.RepeatingVariance;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SequenceI;
import jalview.renderer.ResidueShaderI;

public class VarianceThread extends AlignCalcWorker
{
  public VarianceThread(AlignViewportI alignViewport,
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
      AlignmentAnnotation variance = getVarianceAnnotation();
      AlignmentAnnotation gap = getGapAnnotation();
      if ((variance == null && gap == null) || calcMan.isPending(this))
      {
        calcMan.workerComplete(this);
        return;
      }
      while (!calcMan.notifyWorking(this))
      {
        // System.err.println("Thread
        // (Variance"+Thread.currentThread().getName()+") Waiting around.");
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

      eraseVariance(aWidth);
      computeVariance(alignment);
      updateResultAnnotation(true);

      if (ap != null)
      {
        ap.paintAlignment(true, true);
      }
    } catch (OutOfMemoryError error)
    {
      calcMan.disableWorker(this);
      ap.raiseOOMWarning("calculating variance", error);
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
   * Clear out any existing variance annotations
   * 
   * @param aWidth
   *          the width (number of columns) of the annotated alignment
   */
  protected void eraseVariance(int aWidth)
  {
    AlignmentAnnotation variance = getVarianceAnnotation();
    if (variance != null)
    {
      variance.annotations = new Annotation[aWidth];
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
  protected void computeVariance(AlignmentI alignment)
  {

    int width = alignment.getWidth();
    ProfilesI hvariance = RepeatingVariance.calculate(alignment);

    alignViewport.setSequenceVarianceHash(hvariance);
    setColourSchemeVariance(hvariance);
  }

  /**
   * @return
   */
  protected AlignmentI getAlignment()
  {
    return alignViewport.getAlignment();
  }

  /**
   * @param hvariance
   */
  protected void setColourSchemeVariance(ProfilesI hvariance)
  {
    ResidueShaderI cs = alignViewport.getResidueShading();
    if (cs != null)
    {
      cs.setVariance(hvariance);
    }
  }

  /**
   * Get the Variance annotation for the alignment
   * 
   * @return
   */
  public AlignmentAnnotation getVarianceAnnotation()
  {
    return alignViewport.getAlignmentVarianceAnnotation();
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
   * update the variance annotation from the sequence profile data using
   * current visualization settings.
   */
  @Override
  public void updateAnnotation()
  {
    updateResultAnnotation(false);
  }

  public void updateResultAnnotation(boolean immediate)
  {
    AlignmentAnnotation variance = getVarianceAnnotation();
    ProfilesI hvariance = (ProfilesI) getViewportVariance();
    if (immediate || !calcMan.isWorking(this) && variance != null
            && hvariance != null)
    {
      deriveVariance();
      AlignmentAnnotation gap = getGapAnnotation();
      if (gap != null)
      {
        deriveGap(gap, hvariance);
      }
    }
  }

  /**
   * Convert the computed variance data into the desired annotation for
   * display.
   * 
   * @param varianceAnnotation
   *          the annotation to be populated
   * @param hvariance
   *          the computed variance data
   */
  protected void deriveVariance()
  {
    RepeatingVariance.completeVariance(getAlignment());
  }

  /**
   * Convert the computed variance data into a gap annotation row for display.
   * 
   * @param gapAnnotation
   *          the annotation to be populated
   * @param hvariance
   *          the computed variance data
   */
  protected void deriveGap(AlignmentAnnotation gapAnnotation,
          ProfilesI hvariance)
  {
    long nseq = getAlignment().getHeight();
    RepeatingVariance.completeGapAnnot(gapAnnotation, hvariance,
            hvariance.getStartColumn(), hvariance.getEndColumn() + 1,
            nseq);
  }

  /**
   * Get the variance data stored on the viewport.
   * 
   * @return
   */
  protected Object getViewportVariance()
  {
    // TODO convert ComplementVarianceThread to use Profile
    return alignViewport.getSequenceVarianceHash();
  }
}
