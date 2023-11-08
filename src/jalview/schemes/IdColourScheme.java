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
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.util.Map;

/**
 * shade sequences using the colour shown in the ID panel. Useful to map
 * sequence groupings onto residue data (eg tree subgroups visualised on
 * structures or overview window)
 * 
 * @author jprocter
 */
public class IdColourScheme implements ColourSchemeI
{
  AlignViewportI view = null;

  public IdColourScheme()
  {

  }

  public IdColourScheme(AlignViewportI view, AnnotatedCollectionI coll)
  {
    this.view = view;
  }

  @Override
  public String getSchemeName()
  {
    return JalviewColourScheme.IdColour.toString();
  }

  /**
   * Returns a new instance of this colour scheme with which the given data may
   * be coloured
   */
  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new IdColourScheme(view, coll);
  }

  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
  }

  @Override
  public Color findColour(char symbol, int position, SequenceI seq,
          String consensusResidue, float pid)
  {
    // rather than testing if coll is a sequence group, and if so looking at
    // ((SequenceGroup)coll).idColour
    // we always return the sequence ID colour, in case the user has customised
    // the displayed Id colour by right-clicking an internal node in the tree.
    if (view == null)
    {
      return Color.WHITE;
    }
    Color col = view.getSequenceColour(seq);
    return Color.WHITE.equals(col) ? Color.WHITE : col.darker();
  }

  @Override
  public boolean hasGapColour()
  {
    return false;
  }

  @Override
  public boolean isApplicableTo(AnnotatedCollectionI ac)
  {
    return true;
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
