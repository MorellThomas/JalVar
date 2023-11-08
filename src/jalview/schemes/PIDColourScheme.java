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
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

import java.awt.Color;

public class PIDColourScheme extends ResidueColourScheme
{
  private static final Color[] pidColours = { new Color(100, 100, 255),
      new Color(153, 153, 255), new Color(204, 204, 255), };

  private static final float[] thresholds = { 80, 60, 40, };

  SequenceGroup group;

  public PIDColourScheme()
  {
  }

  @Override
  public Color findColour(char c, int j, SequenceI seq,
          String consensusResidue, float pid)
  {
    /*
     * compare as upper case; note consensusResidue is 
     * always computed as uppercase
     */
    if ('a' <= c && c <= 'z')
    {
      c -= ('a' - 'A');
    }

    if (consensusResidue == null || Comparison.isGap(c))
    {
      return Color.white;
    }

    Color colour = Color.white;

    /*
     * test whether this is the consensus (or joint consensus) residue
     */
    boolean matchesConsensus = consensusResidue.contains(String.valueOf(c));
    if (matchesConsensus)
    {
      for (int i = 0; i < thresholds.length; i++)
      {
        if (pid > thresholds[i])
        {
          colour = pidColours[i];
          break;
        }
      }
    }

    return colour;
  }

  @Override
  public String getSchemeName()
  {
    return JalviewColourScheme.PID.toString();
  }

  /**
   * Returns a new instance of this colour scheme with which the given data may
   * be coloured
   */
  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new PIDColourScheme();
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
