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
import jalview.datamodel.SequenceI;

import java.awt.Color;

public class RNAInteractionColourScheme extends ResidueColourScheme
{
  public RNAInteractionColourScheme()
  {
    super();
  }

  @Override
  public Color findColour(char c)
  {
    // FIXME this is just a copy of NucleotideColourScheme
    return colors[ResidueProperties.nucleotideIndex[c]];
  }

  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    // FIXME this is just a copy of NucleotideColourScheme
    Color currentColour = Color.white;
    try
    {
      currentColour = colors[ResidueProperties.nucleotideIndex[c]];
    } catch (Exception ex)
    {
    }

    return currentColour;
  }

  @Override
  public boolean isNucleotideSpecific()
  {
    return true;
  }

  @Override
  public String getSchemeName()
  {
    return "RNA Interaction type";
  }

  /**
   * Returns a new instance of this colour scheme with which the given data may
   * be coloured
   */
  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new RNAInteractionColourScheme();
  }
}
