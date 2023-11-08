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
import jalview.util.Comparison;

import java.awt.Color;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class ScoreColourScheme extends ResidueColourScheme
{
  public double min;

  public double max;

  public double[] scores;

  /**
   * Creates a new ScoreColourScheme object.
   * 
   * @param scores
   *          DOCUMENT ME!
   * @param min
   *          DOCUMENT ME!
   * @param max
   *          DOCUMENT ME!
   */
  public ScoreColourScheme(int symbolIndex[], double[] scores, double min,
          double max)
  {
    super(symbolIndex);

    this.scores = scores;
    this.min = min;
    this.max = max;

    // Make colours in constructor
    // Why wasn't this done earlier?
    int iSize = scores.length;
    colors = new Color[scores.length];
    for (int i = 0; i < iSize; i++)
    {
      /*
       * scale score between min and max to the range 0.0 - 1.0
       */
      float score = (float) (scores[i] - (float) min) / (float) (max - min);

      if (score > 1.0f)
      {
        score = 1.0f;
      }

      if (score < 0.0f)
      {
        score = 0.0f;
      }
      colors[i] = makeColour(score);
    }
  }

  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    if (Comparison.isGap(c))
    {
      return Color.white;
    }
    return super.findColour(c);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param c
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Color makeColour(float c)
  {
    return new Color(c, (float) 0.0, (float) 1.0 - c);
  }

  @Override
  public String getSchemeName()
  {
    return "Score";
  }

  /**
   * Returns a new instance of this colour scheme with which the given data may
   * be coloured
   */
  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new ScoreColourScheme(symbolIndex, scores, min, max);
  }
}
