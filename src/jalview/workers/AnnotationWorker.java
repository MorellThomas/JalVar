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

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.renderer.seqfeatures.FeatureRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to create and update one or more alignment annotations, given a
 * 'calculator'. Intended to support a 'plug-in' annotation worker which
 * implements the AnnotationProviderI interface.
 */
class AnnotationWorker extends AlignCalcWorker
{
  /*
   * the provider of the annotation calculations
   */
  AnnotationProviderI counter;

  /**
   * Constructor
   * 
   * @param af
   * @param counter
   */
  public AnnotationWorker(AlignViewportI viewport, AlignmentViewPanel panel,
          AnnotationProviderI counter)
  {
    super(viewport, panel);
    ourAnnots = new ArrayList<>();
    this.counter = counter;
    calcMan.registerWorker(this);
  }

  @Override
  public void run()
  {
    try
    {
      calcMan.notifyStart(this);

      while (!calcMan.notifyWorking(this))
      {
        try
        {
          Thread.sleep(200);
        } catch (InterruptedException ex)
        {
          ex.printStackTrace();
        }
      }
      if (alignViewport.isClosed())
      {
        abortAndDestroy();
        return;
      }

      // removeAnnotation();
      AlignmentI alignment = alignViewport.getAlignment();
      if (alignment != null)
      {
        try
        {
          List<AlignmentAnnotation> anns = counter.calculateAnnotation(
                  alignment, new FeatureRenderer(alignViewport));
          for (AlignmentAnnotation ann : anns)
          {
            AlignmentAnnotation theAnn = alignment.findOrCreateAnnotation(
                    ann.label, ann.description, false, null, null);
            theAnn.showAllColLabels = true;
            theAnn.graph = AlignmentAnnotation.BAR_GRAPH;
            theAnn.scaleColLabel = true;
            theAnn.annotations = ann.annotations;
            setGraphMinMax(theAnn, theAnn.annotations);
            theAnn.validateRangeAndDisplay();
            if (!ourAnnots.contains(theAnn))
            {
              ourAnnots.add(theAnn);
            }
            // alignment.addAnnotation(ann);
          }
        } catch (IndexOutOfBoundsException x)
        {
          // probable race condition. just finish and return without any fuss.
          return;
        }
      }
    } catch (OutOfMemoryError error)
    {
      ap.raiseOOMWarning("calculating annotations", error);
      calcMan.disableWorker(this);
    } finally
    {
      calcMan.workerComplete(this);
    }

    if (ap != null)
    {
      ap.adjustAnnotationHeight();
      // TODO: only need to update colour and geometry if panel height changes
      // and view is coloured by annotation, and the annotation is actually
      // changed!
      ap.paintAlignment(true, true);
    }
  }

  @Override
  public void updateAnnotation()
  {
    // do nothing
  }

  /**
   * Answers true to indicate that if this worker's annotation is deleted from
   * the display, the worker should also be removed. This prevents it running
   * and recreating the annotation when the alignment changes.
   */
  @Override
  public boolean isDeletable()
  {
    return true;
  }
}
