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
package jalview.api;

import jalview.datamodel.AlignmentAnnotation;

/**
 * Interface describing a worker that calculates alignment annotation(s). The
 * main (re-)calculation should be performed by the inherited run() method.
 */
public interface AlignCalcWorkerI extends Runnable
{
  /**
   * Answers true if this worker updates the given annotation (regardless of its
   * current state)
   * 
   * @param annot
   * @return
   */
  boolean involves(AlignmentAnnotation annot);

  /**
   * Updates the display of calculated annotation values (does not recalculate
   * the values). This allows ßquick redraw of annotations when display settings
   * are changed.
   */
  void updateAnnotation();

  /**
   * Removes any annotation(s) managed by this worker from the alignment
   */
  void removeAnnotation();

  /**
   * Answers true if the worker should be deleted entirely when its annotation
   * is deleted from the display, or false if it should continue to run. Some
   * workers are required to run for their side-effects.
   * 
   * @return
   */
  boolean isDeletable();
}
