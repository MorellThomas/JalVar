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
package jalview.datamodel;

public interface AnnotatedCollectionI
        extends SequenceCollectionI, ContactMapHolderI
{

  /**
   * TODO: decide if null is a valid response if there is no annotation on the
   * object
   * 
   * @return null
   */
  AlignmentAnnotation[] getAlignmentAnnotation();

  /**
   * Returns a list of annotations matching the given calc id, or an empty list
   * if calcId is null
   * 
   * @param calcId
   * @return
   */
  Iterable<AlignmentAnnotation> findAnnotation(String calcId);

  /**
   * Returns an iterable collection of any annotations that match on given
   * sequence ref, calcId and label (ignoring null values).
   * 
   * @param seq
   *          null or reference sequence to select annotation for
   * @param calcId
   *          null or the calcId to select annotation for
   * @param label
   *          null or the label to select annotation for
   */
  Iterable<AlignmentAnnotation> findAnnotations(SequenceI seq,
          String calcId, String label);

  /**
   * context for this annotated collection
   * 
   * @return null or the collection upon which this collection is defined (e.g.
   *         alignment, parent group).
   */
  AnnotatedCollectionI getContext();
}
