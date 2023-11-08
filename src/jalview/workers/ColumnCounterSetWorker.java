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
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.renderer.seqfeatures.FeatureRenderer;
import jalview.util.ColorUtils;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to compute alignment annotations with column counts for a set of
 * properties of interest on positions in an alignment. <br>
 * This is designed to be extensible, by supplying to the constructor an object
 * that computes a vector of counts for each residue position, based on the
 * residue and and sequence features at that position.
 * 
 */
class ColumnCounterSetWorker extends AlignCalcWorker
{
  FeatureSetCounterI counter;

  /**
   * Constructor registers the annotation for the given alignment frame
   * 
   * @param af
   * @param counter
   */
  public ColumnCounterSetWorker(AlignViewportI viewport,
          AlignmentViewPanel panel, FeatureSetCounterI counter)
  {
    super(viewport, panel);
    ourAnnots = new ArrayList<>();
    this.counter = counter;
    calcMan.registerWorker(this);
  }

  /**
   * method called under control of AlignCalcManager to recompute the annotation
   * when the alignment changes
   */
  @Override
  public void run()
  {
    boolean annotationAdded = false;
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

      if (alignViewport.getAlignment() != null)
      {
        try
        {
          annotationAdded = computeAnnotations();
        } catch (IndexOutOfBoundsException x)
        {
          // probable race condition. just finish and return without any fuss.
          return;
        }
      }
    } catch (OutOfMemoryError error)
    {
      ap.raiseOOMWarning("calculating feature counts", error);
      calcMan.disableWorker(this);
    } finally
    {
      calcMan.workerComplete(this);
    }

    if (ap != null)
    {
      if (annotationAdded)
      {
        ap.adjustAnnotationHeight();
      }
      ap.paintAlignment(true, true);
    }

  }

  /**
   * Scan each column of the alignment to calculate a count by feature type. Set
   * the count as the value of the alignment annotation for that feature type.
   * 
   * @return
   */
  boolean computeAnnotations()
  {
    FeatureRenderer fr = new FeatureRenderer(alignViewport);
    // TODO use the commented out code once JAL-2075 is fixed
    // to get adequate performance on genomic length sequence
    AlignmentI alignment = alignViewport.getAlignment();
    // AlignmentView alignmentView = alignViewport.getAlignmentView(false);
    // AlignmentI alignment = alignmentView.getVisibleAlignment(' ');

    int rows = counter.getNames().length;

    int width = alignment.getWidth();
    int height = alignment.getHeight();
    int[][] counts = new int[width][rows];
    int max[] = new int[rows];
    for (int crow = 0; crow < rows; crow++)
    {
      max[crow] = 0;
    }

    int[] minC = counter.getMinColour();
    int[] maxC = counter.getMaxColour();
    Color minColour = new Color(minC[0], minC[1], minC[2]);
    Color maxColour = new Color(maxC[0], maxC[1], maxC[2]);

    for (int col = 0; col < width; col++)
    {
      int[] count = counts[col];
      for (int crow = 0; crow < rows; crow++)
      {
        count[crow] = 0;
      }
      for (int row = 0; row < height; row++)
      {
        int[] colcount = countFeaturesAt(alignment, col, row, fr);
        if (colcount != null)
        {
          for (int crow = 0; crow < rows; crow++)
          {
            count[crow] += colcount[crow];
          }
        }
      }
      counts[col] = count;
      for (int crow = 0; crow < rows; crow++)
      {
        max[crow] = Math.max(count[crow], max[crow]);
      }
    }

    boolean annotationAdded = false;

    for (int anrow = 0; anrow < rows; anrow++)
    {
      Annotation[] anns = new Annotation[width];
      long rmax = 0;
      /*
       * add counts as annotations. zeros are needed since select-by-annotation ignores empty annotation positions
       */
      for (int i = 0; i < counts.length; i++)
      {
        int count = counts[i][anrow];

        Color color = ColorUtils.getGraduatedColour(count, 0, minColour,
                max[anrow], maxColour);
        String str = String.valueOf(count);
        anns[i] = new Annotation(str, str, '0', count, color);
        rmax = Math.max(count, rmax);
      }

      /*
       * construct or update the annotation
       */
      String description = counter.getDescriptions()[anrow];
      if (!alignment.findAnnotation(description).iterator().hasNext())
      {
        annotationAdded = true;
      }
      AlignmentAnnotation ann = alignment.findOrCreateAnnotation(
              counter.getNames()[anrow], description, false, null, null);
      ann.description = description;
      ann.showAllColLabels = true;
      ann.scaleColLabel = true;
      ann.graph = AlignmentAnnotation.BAR_GRAPH;
      ann.annotations = anns;
      ann.graphMin = 0f; // minimum always zero count
      ann.graphMax = rmax; // maximum count from loop over feature columns
      ann.validateRangeAndDisplay();
      if (!ourAnnots.contains(ann))
      {
        ourAnnots.add(ann);
      }
    }
    return annotationAdded;
  }

  /**
   * Returns a count of any feature types present at the specified position of
   * the alignment
   * 
   * @param alignment
   * @param col
   *          (0..)
   * @param row
   * @param fr
   */
  int[] countFeaturesAt(AlignmentI alignment, int col, int row,
          FeatureRenderer fr)
  {
    SequenceI seq = alignment.getSequenceAt(row);
    if (seq == null)
    {
      return null;
    }
    if (col >= seq.getLength())
    {
      return null;// sequence doesn't extend this far
    }
    char res = seq.getCharAt(col);
    if (Comparison.isGap(res))
    {
      return null;
    }

    /*
     * compute a count for any displayed features at residue
     */
    // see JAL-2075
    List<SequenceFeature> features = fr.findFeaturesAtColumn(seq, col + 1);
    int[] count = this.counter.count(String.valueOf(res), features);
    return count;
  }

  /**
   * Method called when the user changes display options that may affect how the
   * annotation is rendered, but do not change its values. Currently no such
   * options affect user-defined annotation, so this method does nothing.
   */
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
