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
package jalview.analysis;

import java.util.Locale;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to sort all annotations associated with an alignment in
 * various ways.
 * 
 * @author gmcarstairs
 *
 */
public class AnnotationSorter
{

  /**
   * enum for annotation sort options. The text description is used in the
   * Preferences drop-down options. The enum name is saved in the preferences
   * file.
   * 
   * @author gmcarstairs
   *
   */
  public enum SequenceAnnotationOrder
  {
    // Text descriptions surface in the Preferences Sort by... options
    SEQUENCE_AND_LABEL("Sequence"), LABEL_AND_SEQUENCE("Label"),
    NONE("No sort");

    private String description;

    private SequenceAnnotationOrder(String s)
    {
      description = s;
    }

    @Override
    public String toString()
    {
      return description;
    }

    public static SequenceAnnotationOrder forDescription(String d)
    {
      for (SequenceAnnotationOrder order : values())
      {
        if (order.toString().equals(d))
        {
          return order;
        }
      }
      return null;
    }
  }

  // the alignment with respect to which annotations are sorted
  private final AlignmentI alignment;

  // user preference for placement of non-sequence annotations
  private boolean showAutocalcAbove;

  // working map of sequence index in alignment
  private final Map<SequenceI, Integer> sequenceIndices = new HashMap<SequenceI, Integer>();

  /**
   * Constructor given an alignment and the location (top or bottom) of
   * Consensus and similar.
   * 
   * @param alignmentI
   * @param showAutocalculatedAbove
   */
  public AnnotationSorter(AlignmentI alignmentI,
          boolean showAutocalculatedAbove)
  {
    this.alignment = alignmentI;
    this.showAutocalcAbove = showAutocalculatedAbove;
  }

  /**
   * Default comparator sorts as follows by annotation type within sequence
   * order:
   * <ul>
   * <li>annotations with a reference to a sequence in the alignment are sorted
   * on sequence ordering</li>
   * <li>other annotations go 'at the end', with their mutual order
   * unchanged</li>
   * <li>within the same sequence ref, sort by label (non-case-sensitive)</li>
   * </ul>
   */
  private final Comparator<? super AlignmentAnnotation> bySequenceAndLabel = new Comparator<AlignmentAnnotation>()
  {
    @Override
    public int compare(AlignmentAnnotation o1, AlignmentAnnotation o2)
    {
      if (o1 == null && o2 == null)
      {
        return 0;
      }
      if (o1 == null)
      {
        return -1;
      }
      if (o2 == null)
      {
        return 1;
      }

      // TODO how to treat sequence-related autocalculated annotation
      boolean o1auto = o1.autoCalculated && o1.sequenceRef == null;
      boolean o2auto = o2.autoCalculated && o2.sequenceRef == null;
      /*
       * Ignore label (keep existing ordering) for
       * Conservation/Quality/Consensus etc
       */
      if (o1auto && o2auto)
      {
        return 0;
      }

      /*
       * Sort autocalculated before or after sequence-related.
       */
      if (o1auto)
      {
        return showAutocalcAbove ? -1 : 1;
      }
      if (o2auto)
      {
        return showAutocalcAbove ? 1 : -1;
      }
      int computedOrder = compareSequences(o1, o2);
      if (computedOrder == 0)
      {
        computedOrder = compareLabels(o1, o2);
      }
      if (computedOrder == 0)
      {
        computedOrder = compareDescriptions(o1, o2);
      }
      return computedOrder;
    }

    @Override
    public String toString()
    {
      return "Sort by sequence and label";
    }
  };

  /**
   * This comparator sorts as follows by sequence order within annotation type
   * <ul>
   * <li>annotations with a reference to a sequence in the alignment are sorted
   * on label (non-case-sensitive)</li>
   * <li>other annotations go 'at the end', with their mutual order
   * unchanged</li>
   * <li>within the same label, sort by order of the related sequences</li>
   * </ul>
   */
  private final Comparator<? super AlignmentAnnotation> byLabelAndSequence = new Comparator<AlignmentAnnotation>()
  {
    @Override
    public int compare(AlignmentAnnotation o1, AlignmentAnnotation o2)
    {
      if (o1 == null && o2 == null)
      {
        return 0;
      }
      if (o1 == null)
      {
        return -1;
      }
      if (o2 == null)
      {
        return 1;
      }

      // TODO how to treat sequence-related autocalculated annotation
      boolean o1auto = o1.autoCalculated && o1.sequenceRef == null;
      boolean o2auto = o2.autoCalculated && o2.sequenceRef == null;
      /*
       * Ignore label (keep existing ordering) for
       * Conservation/Quality/Consensus etc
       */
      if (o1auto && o2auto)
      {
        return 0;
      }

      /*
       * Sort autocalculated before or after sequence-related.
       */
      if (o1auto)
      {
        return showAutocalcAbove ? -1 : 1;
      }
      if (o2auto)
      {
        return showAutocalcAbove ? 1 : -1;
      }
      int labelOrder = compareLabels(o1, o2);
      return labelOrder == 0 ? compareSequences(o1, o2) : labelOrder;
    }

    @Override
    public String toString()
    {
      return "Sort by label and sequence";
    }
  };

  /**
   * noSort leaves sort order unchanged, within sequence- and autocalculated
   * annotations, but may switch the ordering of these groups. Note this is
   * guaranteed (at least in Java 7) as Arrays.sort() is guaranteed to be
   * 'stable' (not change ordering of equal items).
   */
  private Comparator<? super AlignmentAnnotation> noSort = new Comparator<AlignmentAnnotation>()
  {
    @Override
    public int compare(AlignmentAnnotation o1, AlignmentAnnotation o2)
    {
      // TODO how to treat sequence-related autocalculated annotation
      boolean o1auto = o1.autoCalculated && o1.sequenceRef == null;
      boolean o2auto = o2.autoCalculated && o2.sequenceRef == null;
      // TODO skip this test to allow customised ordering of all annotations
      // - needs a third option: place autocalculated first / last / none
      if (o1 != null && o2 != null)
      {
        if (o1auto && !o2auto)
        {
          return showAutocalcAbove ? -1 : 1;
        }
        if (!o1auto && o2auto)
        {
          return showAutocalcAbove ? 1 : -1;
        }
      }
      return 0;
    }

    @Override
    public String toString()
    {
      return "No sort";
    }
  };

  /**
   * Sort by the specified ordering of sequence-specific annotations.
   * 
   * @param alignmentAnnotations
   * @param order
   */
  public void sort(AlignmentAnnotation[] alignmentAnnotations,
          SequenceAnnotationOrder order)
  {
    if (alignmentAnnotations == null)
    {
      return;
    }
    // cache 'alignment sequence position' for the annotations
    saveSequenceIndices(alignmentAnnotations);

    Comparator<? super AlignmentAnnotation> comparator = getComparator(
            order);

    if (alignmentAnnotations != null)
    {
      synchronized (alignmentAnnotations)
      {
        Arrays.sort(alignmentAnnotations, comparator);
      }
    }
  }

  /**
   * Calculate and save in a temporary map the position of each annotation's
   * sequence (if it has one) in the alignment. Faster to do this once than for
   * every annotation comparison.
   * 
   * @param alignmentAnnotations
   */
  private void saveSequenceIndices(
          AlignmentAnnotation[] alignmentAnnotations)
  {
    sequenceIndices.clear();
    for (AlignmentAnnotation ann : alignmentAnnotations)
    {
      SequenceI seq = ann.sequenceRef;
      if (seq != null)
      {
        int index = AlignmentUtils.getSequenceIndex(alignment, seq);
        sequenceIndices.put(seq, index);
      }
    }
  }

  /**
   * Get the comparator for the specified sort order.
   * 
   * @param order
   * @return
   */
  private Comparator<? super AlignmentAnnotation> getComparator(
          SequenceAnnotationOrder order)
  {
    if (order == null)
    {
      return noSort;
    }
    switch (order)
    {
    case NONE:
      return this.noSort;
    case SEQUENCE_AND_LABEL:
      return this.bySequenceAndLabel;
    case LABEL_AND_SEQUENCE:
      return this.byLabelAndSequence;
    default:
      throw new UnsupportedOperationException(order.toString());
    }
  }

  /**
   * Non-case-sensitive comparison of annotation labels. Returns zero if either
   * argument is null.
   * 
   * @param o1
   * @param o2
   * @return
   */
  private int compareLabels(AlignmentAnnotation o1, AlignmentAnnotation o2)
  {
    if (o1 == null || o2 == null)
    {
      return 0;
    }
    String label1 = o1.label;
    String label2 = o2.label;
    return compareString(label1, label2);
  }

  /**
   * Non-case-sensitive comparison of annotation descriptions. Returns zero if
   * either argument is null.
   * 
   * @param o1
   * @param o2
   * @return
   */
  private int compareDescriptions(AlignmentAnnotation o1,
          AlignmentAnnotation o2)
  {
    if (o1 == null || o2 == null)
    {
      return 0;
    }
    String label1 = o1.description;
    String label2 = o2.description;
    return compareString(label1, label2);
  }

  private int compareString(String label1, String label2)
  {
    if (label1 == null && label2 == null)
    {
      return 0;
    }
    if (label1 == null)
    {
      return -1;
    }
    if (label2 == null)
    {
      return 1;
    }
    return label1.toUpperCase(Locale.ROOT)
            .compareTo(label2.toUpperCase(Locale.ROOT));
  }

  /**
   * Comparison based on position of associated sequence (if any) in the
   * alignment. Returns zero if either argument is null.
   * 
   * @param o1
   * @param o2
   * @return
   */
  private int compareSequences(AlignmentAnnotation o1,
          AlignmentAnnotation o2)
  {
    SequenceI seq1 = o1.sequenceRef;
    SequenceI seq2 = o2.sequenceRef;
    if (seq1 == null && seq2 == null)
    {
      return 0;
    }
    /*
     * Sort non-sequence-related before or after sequence-related.
     */
    if (seq1 == null)
    {
      return showAutocalcAbove ? -1 : 1;
    }
    if (seq2 == null)
    {
      return showAutocalcAbove ? 1 : -1;
    }
    // get sequence index - but note -1 means 'at end' so needs special handling
    int index1 = sequenceIndices.get(seq1);
    int index2 = sequenceIndices.get(seq2);
    if (index1 == index2)
    {
      return 0;
    }
    if (index1 == -1)
    {
      return -1;
    }
    if (index2 == -1)
    {
      return 1;
    }
    return Integer.compare(index1, index2);
  }
}
