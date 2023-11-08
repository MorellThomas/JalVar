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

import java.awt.Color;

import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

public class OverviewResColourFinder extends ResidueColourFinder
{
  /*
   * colour for gaps (unless overridden by colour scheme)
   * - as set in Preferences, _or_ read from a project file
   */
  Color gapColour;

  /*
   * colour for residues if no colour scheme set (before feature colouring)
   * - as set in Preferences, _or_ read from a project file
   */
  Color residueColour;

  /*
   * colour for hidden regions
   * - as set in Preferences, _or_ read from a project file
   */
  Color hiddenColour;

  /**
   * Constructor without colour settings (used by applet)
   * 
   * @deprecated
   */
  @Deprecated
  public OverviewResColourFinder()
  {
    this(Color.lightGray, Color.white, Color.darkGray.darker());
  }

  /**
   * Constructor given default colours for gaps, residues and hidden regions
   * 
   * @param gaps
   * @param residues
   * @param hidden
   */
  public OverviewResColourFinder(Color gaps, Color residues, Color hidden)
  {
    gapColour = gaps;
    residueColour = residues;
    hiddenColour = hidden;
  }

  @Override
  public Color getBoxColour(ResidueShaderI shader, SequenceI seq, int i)
  {
    Color resBoxColour = residueColour;
    char currentChar = seq.getCharAt(i);

    // In the overview window, gaps are coloured grey, unless the colour scheme
    // specifies a gap colour, in which case gaps honour the colour scheme
    // settings
    if (shader.getColourScheme() != null)
    {
      if (Comparison.isGap(currentChar)
              && (!shader.getColourScheme().hasGapColour()))
      {
        resBoxColour = gapColour;
      }
      else
      {
        resBoxColour = shader.findColour(currentChar, i, seq);
      }
    }
    else if (Comparison.isGap(currentChar))
    {
      resBoxColour = gapColour;
    }

    return resBoxColour;
  }

  /**
   * {@inheritDoc} In the overview, the showBoxes setting is ignored, as the
   * overview displays the colours regardless.
   */
  @Override
  protected Color getResidueBoxColour(boolean showBoxes,
          ResidueShaderI shader, SequenceGroup[] allGroups, SequenceI seq,
          int i)
  {
    ResidueShaderI currentShader;
    SequenceGroup currentSequenceGroup = getCurrentSequenceGroup(allGroups,
            i);
    if (currentSequenceGroup != null)
    {
      currentShader = currentSequenceGroup.getGroupColourScheme();
    }
    else
    {
      currentShader = shader;
    }

    return getBoxColour(currentShader, seq, i);
  }

  /**
   * Returns the colour used for hidden regions
   * 
   * @return
   */
  public Color getHiddenColour()
  {
    return hiddenColour;
  }

  /**
   * Returns the colour used for gaps, if not overridden by the alignment colour
   * scheme
   * 
   * @return
   */
  public Color getGapColour()
  {
    return gapColour;
  }

  /**
   * Returns the colour used for residues (before applying any feature
   * colouring) if there is no alignment colour scheme
   * 
   * @return
   */
  public Color getResidueColour()
  {
    return residueColour;
  }
}
