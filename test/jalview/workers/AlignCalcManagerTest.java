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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.api.AlignCalcManagerI;
import jalview.api.AlignCalcWorkerI;
import jalview.api.FeatureRenderer;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AlignCalcManagerTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private AlignFrame alignFrame;

  /**
   * Test the method that removes a worker associated with an annotation,
   * provided the worker is marked as 'deletable' (some workers should continue
   * to run even when their results are no longer displayed)
   */
  @Test(groups = "Functional")
  public void testRemoveWorkerForAnnotation()
  {
    AlignCalcManagerI acm = alignFrame.getViewport().getCalcManager();
    final AlignmentAnnotation ann1 = new AlignmentAnnotation("Ann1", "desc",
            new Annotation[] {});
    final AlignmentAnnotation ann2 = new AlignmentAnnotation("Ann2", "desc",
            new Annotation[] {});

    /*
     * make two workers for ann1, one deletable, one not
     * and no worker for ann2
     */
    AlignCalcWorkerI worker1 = makeWorker(ann1, true);
    AlignCalcWorkerI worker2 = makeWorker(ann1, false);

    /*
     * The new workers will get run each in their own thread.
     * We can't tell here whether they have finished, or not yet started.
     * They have to finish to be 'seen' by getRegisteredWorkersOfClass()
     *   registerWorker adds to the 'restartable' list but
     *   getRegisteredWorkers reads from the 'canUpdate' list
     *   (which is only updated after a worker has run) - why?
     * So just give workers time to start and finish
     */
    synchronized (this)
    {
      try
      {
        wait(100);
      } catch (InterruptedException e)
      {
        //
      }
    }

    List<AlignCalcWorkerI> workers = acm
            .getRegisteredWorkersOfClass(worker1.getClass());
    assertEquals(2, workers.size());
    assertTrue(workers.contains(worker1));
    assertTrue(workers.contains(worker2));
    assertFalse(acm.isDisabled(worker1));
    assertFalse(acm.isDisabled(worker2));

    /*
     * remove workers for ann2 (there aren't any)
     */
    acm.removeWorkerForAnnotation(ann2);
    assertTrue(acm.getRegisteredWorkersOfClass(worker1.getClass())
            .contains(worker1));
    assertTrue(acm.getRegisteredWorkersOfClass(worker1.getClass())
            .contains(worker2));
    assertFalse(acm.isDisabled(worker1));
    assertFalse(acm.isDisabled(worker2));

    /*
     * remove worker2 for ann1
     * - should delete worker1 but not worker2
     */
    acm.removeWorkerForAnnotation(ann1);
    assertEquals(1,
            acm.getRegisteredWorkersOfClass(worker1.getClass()).size());
    assertTrue(acm.getRegisteredWorkersOfClass(worker1.getClass())
            .contains(worker2));
    assertFalse(acm.isDisabled(worker1));
    assertFalse(acm.isDisabled(worker2));
  }

  /**
   * Make a worker linked to the given annotation
   * 
   * @param ann
   * @param deletable
   * @return
   */
  AnnotationWorker makeWorker(final AlignmentAnnotation ann,
          final boolean deletable)
  {
    AnnotationProviderI annotationProvider = new AnnotationProviderI()
    {
      @Override
      public List<AlignmentAnnotation> calculateAnnotation(AlignmentI al,
              FeatureRenderer fr)
      {
        return Collections.singletonList(ann);
      }
    };
    return new AnnotationWorker(alignFrame.getViewport(),
            alignFrame.alignPanel, annotationProvider)
    {
      @Override
      public boolean isDeletable()
      {
        return deletable;
      }

      @Override
      public boolean involves(AlignmentAnnotation ann1)
      {
        return ann == ann1;
      }
    };
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    AlignmentI al = new Alignment(
            new SequenceI[]
            { new Sequence("Seq1", "ABC") });
    al.setDataset(null);
    alignFrame = new AlignFrame(al, 3, 1);
  }
}
