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
package jalview.schemes;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceGroup;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Helps generate the colors for RNA secondary structure. Future: add option to
 * change colors based on covariation.
 * 
 * @author Lauren Michelle Lui
 * @deprecated this seems to be unfinished - just use RNAHelicesColour
 */
@Deprecated
public class RNAHelicesColourChooser
{

  AlignViewportI av;

  AlignmentViewPanel ap;

  ColourSchemeI oldcs;

  Map<SequenceGroup, ColourSchemeI> oldgroupColours;

  AlignmentAnnotation currentAnnotation;

  boolean adjusting = false;

  public RNAHelicesColourChooser(AlignViewportI av,
          final AlignmentViewPanel ap)
  {
    oldcs = av.getGlobalColourScheme();
    if (av.getAlignment().getGroups() != null)
    {
      oldgroupColours = new Hashtable<>();
      for (SequenceGroup sg : ap.getAlignment().getGroups())
      {
        if (sg.getColourScheme() != null)
        {
          oldgroupColours.put(sg, sg.getColourScheme());
        }
      }
    }
    this.av = av;
    this.ap = ap;

    adjusting = true;
    Vector<String> list = new Vector<>();
    int index = 1;
    AlignmentAnnotation[] anns = av.getAlignment().getAlignmentAnnotation();
    if (anns != null)
    {
      for (int i = 0; i < anns.length; i++)
      {
        String label = anns[i].label;
        if (!list.contains(label))
        {
          list.addElement(label);
        }
        else
        {
          list.addElement(label + "_" + (index++));
        }
      }
    }

    adjusting = false;
    changeColour();
  }

  void changeColour()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }
    RNAHelicesColour rhc = new RNAHelicesColour(av.getAlignment());

    av.setGlobalColourScheme(rhc);

    ap.paintAlignment(true, true);
  }
}
