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

import jalview.analysis.scoremodels.ScoreModels;
import jalview.api.AlignViewportI;
import jalview.api.analysis.PairwiseScoreModelI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

import java.awt.Color;

public class Blosum62ColourScheme extends ResidueColourScheme
{
  private static final Color LIGHT_BLUE = new Color(204, 204, 255);

  private static final Color DARK_BLUE = new Color(154, 154, 255);

  public Blosum62ColourScheme()
  {
    super();
  }

  /**
   * Returns a new instance of this colour scheme with which the given data may
   * be coloured
   */
  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new Blosum62ColourScheme();
  }

  @Override
  public Color findColour(char res, int j, SequenceI seq,
          String consensusResidue, float pid)
  {
    PairwiseScoreModelI sm = ScoreModels.getInstance().getBlosum62();

    /*
     * compare as upper case; note consensusResidue is 
     * always computed as uppercase
     */
    if ('a' <= res && res <= 'z')
    {
      res -= ('a' - 'A');
    }

    if (Comparison.isGap(res) || consensusResidue == null)
    {
      return Color.white;
    }

    Color colour;

    if (consensusResidue.indexOf(res) > -1)
    {
      colour = DARK_BLUE;
    }
    else
    {
      float score = 0;

      for (char consensus : consensusResidue.toCharArray())
      {
        score += sm.getPairwiseScore(consensus, res);
      }

      if (score > 0)
      {
        colour = LIGHT_BLUE;
      }
      else
      {
        colour = Color.white;
      }
    }
    return colour;
  }

  @Override
  public boolean isPeptideSpecific()
  {
    return true;
  }

  @Override
  public String getSchemeName()
  {
    return JalviewColourScheme.Blosum62.toString();
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
