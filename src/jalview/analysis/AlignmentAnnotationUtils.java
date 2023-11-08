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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceI;
import jalview.renderer.AnnotationRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlignmentAnnotationUtils
{

  /**
   * Helper method to populate lists of annotation types for the Show/Hide
   * Annotations menus. If sequenceGroup is not null, this is restricted to
   * annotations which are associated with sequences in the selection group.
   * <p/>
   * If an annotation row is currently visible, its type (label) is added (once
   * only per type), to the shownTypes list. If it is currently hidden, it is
   * added to the hiddenTypesList.
   * <p/>
   * For rows that belong to a line graph group, so are always rendered
   * together:
   * <ul>
   * <li>Treat all rows in the group as visible, if at least one of them is</li>
   * <li>Build a list of all the annotation types that belong to the group</li>
   * </ul>
   * 
   * @param shownTypes
   *          a map, keyed by calcId (annotation source), whose entries are the
   *          lists of annotation types found for the calcId; each annotation
   *          type in turn may be a list (in the case of grouped annotations)
   * @param hiddenTypes
   *          a map, similar to shownTypes, but for hidden annotation types
   * @param annotations
   *          the annotations on the alignment to scan
   * @param forSequences
   *          the sequences to restrict search to
   */
  public static void getShownHiddenTypes(
          Map<String, List<List<String>>> shownTypes,
          Map<String, List<List<String>>> hiddenTypes,
          List<AlignmentAnnotation> annotations,
          List<SequenceI> forSequences)
  {
    BitSet visibleGraphGroups = AlignmentAnnotationUtils
            .getVisibleLineGraphGroups(annotations);

    /*
     * Build a lookup, by calcId (annotation source), of all annotation types in
     * each graph group.
     */
    Map<String, Map<Integer, List<String>>> groupLabels = new HashMap<String, Map<Integer, List<String>>>();

    // trackers for which calcId!label combinations we have dealt with
    List<String> addedToShown = new ArrayList<String>();
    List<String> addedToHidden = new ArrayList<String>();

    for (AlignmentAnnotation aa : annotations)
    {
      /*
       * Ignore non-positional annotations, can't render these against an
       * alignment
       */
      if (aa.annotations == null)
      {
        continue;
      }
      if (forSequences != null && (aa.sequenceRef != null
              && forSequences.contains(aa.sequenceRef)))
      {
        String calcId = aa.getCalcId();

        /*
         * Build a 'composite label' for types in line graph groups.
         */
        final List<String> labelAsList = new ArrayList<String>();
        final String displayLabel = aa.label;
        labelAsList.add(displayLabel);
        if (aa.graph == AlignmentAnnotation.LINE_GRAPH
                && aa.graphGroup > -1)
        {
          if (!groupLabels.containsKey(calcId))
          {
            groupLabels.put(calcId, new HashMap<Integer, List<String>>());
          }
          Map<Integer, List<String>> groupLabelsForCalcId = groupLabels
                  .get(calcId);
          if (groupLabelsForCalcId.containsKey(aa.graphGroup))
          {
            if (!groupLabelsForCalcId.get(aa.graphGroup)
                    .contains(displayLabel))
            {
              groupLabelsForCalcId.get(aa.graphGroup).add(displayLabel);
            }
          }
          else
          {
            groupLabelsForCalcId.put(aa.graphGroup, labelAsList);
          }
        }
        else
        /*
         * 'Simple case' - not a grouped annotation type - list of one label
         * only
         */
        {
          String rememberAs = calcId + "!" + displayLabel;
          if (aa.visible && !addedToShown.contains(rememberAs))
          {
            if (!shownTypes.containsKey(calcId))
            {
              shownTypes.put(calcId, new ArrayList<List<String>>());
            }
            shownTypes.get(calcId).add(labelAsList);
            addedToShown.add(rememberAs);
          }
          else
          {
            if (!aa.visible && !addedToHidden.contains(rememberAs))
            {
              if (!hiddenTypes.containsKey(calcId))
              {
                hiddenTypes.put(calcId, new ArrayList<List<String>>());
              }
              hiddenTypes.get(calcId).add(labelAsList);
              addedToHidden.add(rememberAs);
            }
          }
        }
      }
    }
    /*
     * Finally add the 'composite group labels' to the appropriate lists,
     * depending on whether the group is identified as visible or hidden. Don't
     * add the same label more than once (there may be many graph groups that
     * generate it).
     */
    for (String calcId : groupLabels.keySet())
    {
      for (int group : groupLabels.get(calcId).keySet())
      {
        final List<String> groupLabel = groupLabels.get(calcId).get(group);
        // don't want to duplicate 'same types in different order'
        Collections.sort(groupLabel);
        if (visibleGraphGroups.get(group))
        {
          if (!shownTypes.containsKey(calcId))
          {
            shownTypes.put(calcId, new ArrayList<List<String>>());
          }
          if (!shownTypes.get(calcId).contains(groupLabel))
          {
            shownTypes.get(calcId).add(groupLabel);
          }
        }
        else
        {
          if (!hiddenTypes.containsKey(calcId))
          {
            hiddenTypes.put(calcId, new ArrayList<List<String>>());
          }
          if (!hiddenTypes.get(calcId).contains(groupLabel))
          {
            hiddenTypes.get(calcId).add(groupLabel);
          }
        }
      }
    }
  }

  /**
   * Returns a BitSet (possibly empty) of those graphGroups for line graph
   * annotations, which have at least one member annotation row marked visible.
   * <p/>
   * Only one row in each visible group is marked visible, but when it is drawn,
   * so are all the other rows in the same group.
   * <p/>
   * This lookup set allows us to check whether rows apparently marked not
   * visible are in fact shown.
   * 
   * @see AnnotationRenderer#drawComponent
   * @param annotations
   * @return
   */
  public static BitSet getVisibleLineGraphGroups(
          List<AlignmentAnnotation> annotations)
  {
    BitSet result = new BitSet();
    for (AlignmentAnnotation ann : annotations)
    {
      if (ann.graph == AlignmentAnnotation.LINE_GRAPH && ann.visible)
      {
        int gg = ann.graphGroup;
        if (gg > -1)
        {
          result.set(gg);
        }
      }
    }
    return result;
  }

  /**
   * Converts an array of AlignmentAnnotation into a List of
   * AlignmentAnnotation. A null array is converted to an empty list.
   * 
   * @param anns
   * @return
   */
  public static List<AlignmentAnnotation> asList(AlignmentAnnotation[] anns)
  {
    // TODO use AlignmentAnnotationI instead when it exists
    return (anns == null ? Collections.<AlignmentAnnotation> emptyList()
            : Arrays.asList(anns));
  }
}
