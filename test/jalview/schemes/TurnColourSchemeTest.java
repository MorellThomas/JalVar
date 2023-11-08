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

import static org.testng.Assert.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

public class TurnColourSchemeTest
{
  /**
   * Turn colours are based on the scores in ResidueProperties.turn A = 0.66, R
   * = 0.95, N = 1.56, D = 1.46... min = 0.47 max = 1.56
   * <p>
   * scores are scaled to c 0-1 between min and max and colour is (c, 1-c, 1-c)
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ScoreColourScheme scheme = new TurnColourScheme();

    float min = 0.47f;
    float max = 1.56f;
    float a = (0.66f - min) / (max - min);
    assertEquals(scheme.findColour('A', 0, null),
            new Color(a, 1 - a, 1 - a));

    float d = (1.46f - min) / (max - min);
    assertEquals(scheme.findColour('D', 0, null),
            new Color(d, 1 - d, 1 - d));

    assertEquals(scheme.findColour('-', 0, null), Color.WHITE);
  }

}
