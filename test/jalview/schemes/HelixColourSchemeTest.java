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

public class HelixColourSchemeTest
{
  /**
   * Turn colours are based on the scores in ResidueProperties.helix A = 1.42, R
   * = 0.98, N = 0.67, D = 1.01... min = 0.57 max = 1.51
   * <p>
   * scores are scaled to c 0-1 between min and max and colour is (c, 1-c, c)
   */
  @Test(groups = "Functional")
  public void testFindColour()
  {
    ScoreColourScheme scheme = new HelixColourScheme();

    float min = 0.57f;
    float max = 1.51f;
    float a = (1.42f - min) / (max - min);
    assertEquals(scheme.findColour('A', 0, null), new Color(a, 1 - a, a));

    float d = (1.01f - min) / (max - min);
    assertEquals(scheme.findColour('D', 0, null), new Color(d, 1 - d, d));

    assertEquals(scheme.findColour('-', 0, null), Color.WHITE);
  }

}
