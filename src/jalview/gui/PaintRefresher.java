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
package jalview.gui;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Route datamodel/view update events for a sequence set to any display
 * components involved TODO: JV3 refactor to abstract gui/view package
 * 
 * @author $author$
 * @version $Revision$
 */
public class PaintRefresher
{
  static Map<String, List<Component>> components = new HashMap<>();

  /**
   * Add the given component to those registered under the given sequence set
   * id. Does nothing if already added.
   * 
   * @param comp
   * @param al
   */
  public static void Register(Component comp, String seqSetId)
  {
    if (components.containsKey(seqSetId))
    {
      List<Component> comps = components.get(seqSetId);
      if (!comps.contains(comp))
      {
        comps.add(comp);
      }
    }
    else
    {
      List<Component> vcoms = new ArrayList<>();
      vcoms.add(comp);
      components.put(seqSetId, vcoms);
    }
  }

  /**
   * Remove this component from all registrations. Also removes a registered
   * sequence set id if there are no remaining components registered against it.
   * 
   * @param comp
   */
  public static void RemoveComponent(Component comp)
  {
    if (components == null)
    {
      return;
    }

    Iterator<String> it = components.keySet().iterator();
    while (it.hasNext())
    {
      List<Component> comps = components.get(it.next());
      comps.remove(comp);
      if (comps.isEmpty())
      {
        it.remove();
      }
    }
  }

  public static void Refresh(Component source, String id)
  {
    Refresh(source, id, false, false);
  }

  public static void Refresh(Component source, String id,
          boolean alignmentChanged, boolean validateSequences)
  {
    List<Component> comps = components.get(id);

    if (comps == null)
    {
      return;
    }

    for (Component comp : comps)
    {
      if (comp == source)
      {
        continue;
      }
      if (comp instanceof AlignmentPanel)
      {
        if (validateSequences && source instanceof AlignmentPanel)
        {
          validateSequences(((AlignmentPanel) source).av.getAlignment(),
                  ((AlignmentPanel) comp).av.getAlignment());
        }
        if (alignmentChanged)
        {
          ((AlignmentPanel) comp).alignmentChanged();
        }
      }
      else if (comp instanceof IdCanvas)
      {
        // BH 2019.04.22 fixes JS problem of repaint() consolidation
        // that occurs in JavaScript but not Java [JAL-3226]
        ((IdCanvas) comp).fastPaint = false;
      }
      else if (comp instanceof SeqCanvas)
      {
        // BH 2019.04.22 fixes JS problem of repaint() consolidation
        // that occurs in JavaScript but not Java [JAL-3226]
        ((SeqCanvas) comp).fastPaint = false;
      }
      comp.repaint();
    }
  }

  static void validateSequences(AlignmentI source, AlignmentI comp)
  {
    SequenceI[] a1;
    if (source.getHiddenSequences().getSize() > 0)
    {
      a1 = source.getHiddenSequences().getFullAlignment()
              .getSequencesArray();
    }
    else
    {
      a1 = source.getSequencesArray();
    }

    SequenceI[] a2;
    if (comp.getHiddenSequences().getSize() > 0)
    {
      a2 = comp.getHiddenSequences().getFullAlignment().getSequencesArray();
    }
    else
    {
      a2 = comp.getSequencesArray();
    }

    int i, iSize = a1.length, j, jSize = a2.length;

    if (iSize == jSize)
    {
      return;
    }

    boolean exists = false;
    for (i = 0; i < iSize; i++)
    {
      exists = false;

      for (j = 0; j < jSize; j++)
      {
        if (a2[j] == a1[i])
        {
          exists = true;
          break;
        }
      }

      if (!exists)
      {
        if (i < comp.getHeight())
        {
          // TODO: the following does not trigger any recalculation of
          // height/etc, or maintain the dataset
          if (comp.getDataset() != source.getDataset())
          {
            // raise an implementation warning here - not sure if this situation
            // will ever occur
            System.err.println(
                    "IMPLEMENTATION PROBLEM: DATASET out of sync due to an insert whilst calling PaintRefresher.validateSequences(AlignmentI, ALignmentI)");
          }
          List<SequenceI> alsq = comp.getSequences();
          synchronized (alsq)
          {
            alsq.add(i, a1[i]);
          }
        }
        else
        {
          comp.addSequence(a1[i]);
        }

        if (comp.getHiddenSequences().getSize() > 0)
        {
          a2 = comp.getHiddenSequences().getFullAlignment()
                  .getSequencesArray();
        }
        else
        {
          a2 = comp.getSequencesArray();
        }

        jSize = a2.length;
      }
    }

    iSize = a1.length;
    jSize = a2.length;

    for (j = 0; j < jSize; j++)
    {
      exists = false;
      for (i = 0; i < iSize; i++)
      {
        if (a2[j] == a1[i])
        {
          exists = true;
          break;
        }
      }

      if (!exists)
      {
        comp.deleteSequence(a2[j]);
      }
    }
  }

  static AlignmentPanel[] getAssociatedPanels(String id)
  {
    List<Component> comps = components.get(id);
    if (comps == null)
    {
      return new AlignmentPanel[0];
    }
    List<AlignmentPanel> tmp = new ArrayList<>();
    for (Component comp : comps)
    {
      if (comp instanceof AlignmentPanel)
      {
        tmp.add((AlignmentPanel) comp);
      }
    }
    return tmp.toArray(new AlignmentPanel[tmp.size()]);
  }

}
