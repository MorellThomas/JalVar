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
package jalview.util;

import static org.testng.Assert.assertEquals;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

public class SetUtilsTest
{
  @Test(groups = "Functional")
  public void testCountDisjunction()
  {
    Set<Color> s1 = new HashSet<Color>();
    assertEquals(SetUtils.countDisjunction(null, null), 0);
    assertEquals(SetUtils.countDisjunction(s1, null), 0);
    assertEquals(SetUtils.countDisjunction(null, s1), 0);
    s1.add(Color.white);
    assertEquals(SetUtils.countDisjunction(s1, null), 1);
    assertEquals(SetUtils.countDisjunction(null, s1), 1);
    assertEquals(SetUtils.countDisjunction(s1, null), 1);
    assertEquals(SetUtils.countDisjunction(s1, s1), 0);

    Set<Object> s2 = new HashSet<Object>();
    assertEquals(SetUtils.countDisjunction(s2, s2), 0);
    assertEquals(SetUtils.countDisjunction(s1, s2), 1);
    assertEquals(SetUtils.countDisjunction(s2, s1), 1);

    s1.add(Color.yellow);
    s1.add(Color.blue);
    s2.add(new Color(Color.yellow.getRGB()));

    /*
     * now s1 is {white, yellow, blue}
     *     s2 is {yellow'}
     */
    assertEquals(SetUtils.countDisjunction(s1, s2), 2);
    s2.add(Color.blue);
    assertEquals(SetUtils.countDisjunction(s1, s2), 1);
    s2.add(Color.pink);
    assertEquals(SetUtils.countDisjunction(s1, s2), 2);

  }
}
