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

import jalview.api.AlignCalcManagerI;
import jalview.api.AlignCalcWorkerI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;

import java.util.List;

/**
 * Base class for alignment calculation workers
 * 
 * @author jimp
 * 
 */
public abstract class AlignCalcWorker implements AlignCalcWorkerI
{
  /**
   * manager and data source for calculations
   */
  protected AlignViewportI alignViewport;

  protected AlignCalcManagerI calcMan;

  protected AlignmentViewPanel ap;

  protected List<AlignmentAnnotation> ourAnnots;

  public AlignCalcWorker(AlignViewportI alignViewport,
          AlignmentViewPanel alignPanel)
  {
    this.alignViewport = alignViewport;
    calcMan = alignViewport.getCalcManager();
    ap = alignPanel;
  }

  protected void abortAndDestroy()
  {
    if (calcMan != null)
    {
      calcMan.workerComplete(this);
    }
    alignViewport = null;
    calcMan = null;
    ap = null;

  }

  @Override
  public boolean involves(AlignmentAnnotation i)
  {
    return ourAnnots != null && ourAnnots.contains(i);
  }

  /**
   * Permanently removes from the alignment all annotation rows managed by this
   * worker
   */
  @Override
  public void removeAnnotation()
  {
    if (ourAnnots != null && alignViewport != null)
    {
      AlignmentI alignment = alignViewport.getAlignment();
      synchronized (ourAnnots)
      {
        for (AlignmentAnnotation aa : ourAnnots)
        {
          alignment.deleteAnnotation(aa, true);
        }
      }
      ourAnnots.clear();
    }
  }

  // TODO: allow GUI to query workers associated with annotation to add items to
  // annotation label panel popup menu

  @Override
  public boolean isDeletable()
  {
    return false;
  }

  /**
   * Calculate min and max values of annotations and set as graphMin, graphMax
   * on the AlignmentAnnotation. This is needed because otherwise, well, bad
   * things happen.
   * 
   * @param ann
   * @param anns
   */
  protected void setGraphMinMax(AlignmentAnnotation ann, Annotation[] anns)
  {
    // TODO feels like this belongs inside AlignmentAnnotation!
    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;
    boolean set = false;
    for (Annotation a : anns)
    {
      if (a != null)
      {
        set = true;
        float val = a.value;
        max = Math.max(max, val);
        min = Math.min(min, val);
      }
    }
    if (set)
    {
      ann.graphMin = min;
      ann.graphMax = max;
    }
  }

}
