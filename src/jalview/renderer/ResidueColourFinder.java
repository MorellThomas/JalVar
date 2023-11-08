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
package jalview.renderer;

import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.seqfeatures.FeatureColourFinder;

import java.awt.Color;

public class ResidueColourFinder
{
  public ResidueColourFinder()
  {
  }

  /**
   * Get the colour of a residue in a sequence
   * 
   * @param showBoxes
   *          true if the viewport's Show Boxes setting is true
   * @param shader
   *          the viewport's colour scheme
   * @param allGroups
   *          all the groups which seq participates in
   * @param seq
   *          the sequence containing the residue
   * @param position
   *          the position of the residue in the sequence
   * @param finder
   *          FeatureColourFinder for the viewport
   * @return colour of the residue
   */
  public Color getResidueColour(boolean showBoxes, ResidueShaderI shader,
          SequenceGroup[] allGroups, final SequenceI seq, int position,
          FeatureColourFinder finder)
  {
    Color col = getResidueBoxColour(showBoxes, shader, allGroups, seq,
            position);

    // if there's a FeatureColourFinder we might override the residue colour
    // here with feature colouring
    if (finder != null)
    {
      col = finder.findFeatureColour(col, seq, position);
    }
    return col;
  }

  /**
   * Get the residue colour without accounting for any features
   * 
   * @param showBoxes
   *          true if the viewport's Show Boxes setting is true
   * @param shader
   *          the viewport's colour scheme
   * @param allGroups
   *          all the groups which seq participates in
   * @param seq
   *          the sequence containing the residue
   * @param i
   *          the position of the residue in the sequence
   * @return
   */
  protected Color getResidueBoxColour(boolean showBoxes,
          ResidueShaderI shader, SequenceGroup[] allGroups, SequenceI seq,
          int i)
  {
    SequenceGroup currentSequenceGroup = getCurrentSequenceGroup(allGroups,
            i);
    if (currentSequenceGroup != null)
    {
      if (currentSequenceGroup.getDisplayBoxes())
      {
        return getBoxColour(currentSequenceGroup.getGroupColourScheme(),
                seq, i);
      }
    }
    else if (showBoxes)
    {
      return getBoxColour(shader, seq, i);
    }

    return Color.white;
  }

  /**
   * Search all the groups for a sequence to find the one which a given res
   * falls into
   * 
   * @param allGroups
   *          all the groups a sequence participates in
   * @param res
   *          the residue to search for
   * @return a sequence group for res, or null if no sequence group applies
   */
  public SequenceGroup getCurrentSequenceGroup(SequenceGroup[] allGroups,
          int res)
  {
    if (allGroups == null)
    {
      return null;
    }

    for (int i = 0; i < allGroups.length; i++)
    {
      if ((allGroups[i].getStartRes() <= res)
              && (allGroups[i].getEndRes() >= res))
      {
        return (allGroups[i]);
      }
    }

    return null;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param shader
   *          the viewport's colour scheme
   * @param seq
   *          the sequence containing the residue
   * @param i
   *          the position of the residue in the sequence
   */
  public Color getBoxColour(ResidueShaderI shader, SequenceI seq, int i)
  {
    Color resBoxColour = Color.white;
    if (shader.getColourScheme() != null)
    {
      resBoxColour = shader.findColour(seq.getCharAt(i), i, seq);
    }
    return resBoxColour;
  }

}
