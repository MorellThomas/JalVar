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
package jalview.analysis;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CodingUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testDecodeCodon()
  {
    assertTrue(
            Arrays.equals(new char[]
            { 'A', 'A', 'A' }, CodingUtils.decodeCodon(0)));
    assertTrue(
            Arrays.equals(new char[]
            { 'A', 'A', 'C' }, CodingUtils.decodeCodon(1)));
    assertTrue(
            Arrays.equals(new char[]
            { 'A', 'A', 'G' }, CodingUtils.decodeCodon(2)));
    assertTrue(
            Arrays.equals(new char[]
            { 'A', 'A', 'T' }, CodingUtils.decodeCodon(3)));
    assertTrue(
            Arrays.equals(new char[]
            { 'A', 'C', 'A' }, CodingUtils.decodeCodon(4)));
    assertTrue(
            Arrays.equals(new char[]
            { 'C', 'A', 'A' }, CodingUtils.decodeCodon(16)));
    assertTrue(
            Arrays.equals(new char[]
            { 'G', 'G', 'G' }, CodingUtils.decodeCodon(42)));
    assertTrue(
            Arrays.equals(new char[]
            { 'T', 'T', 'T' }, CodingUtils.decodeCodon(63)));
  }

  @Test(groups = { "Functional" })
  public void testDecodeNucleotide()
  {
    assertEquals('A', CodingUtils.decodeNucleotide(0));
    assertEquals('C', CodingUtils.decodeNucleotide(1));
    assertEquals('G', CodingUtils.decodeNucleotide(2));
    assertEquals('T', CodingUtils.decodeNucleotide(3));
    assertEquals('0', CodingUtils.decodeNucleotide(4));
  }

  @Test(groups = { "Functional" })
  public void testEncodeCodon()
  {
    assertTrue(CodingUtils.encodeCodon('Z') < 0);
    assertEquals(0, CodingUtils.encodeCodon('a'));
    assertEquals(0, CodingUtils.encodeCodon('A'));
    assertEquals(1, CodingUtils.encodeCodon('c'));
    assertEquals(1, CodingUtils.encodeCodon('C'));
    assertEquals(2, CodingUtils.encodeCodon('g'));
    assertEquals(2, CodingUtils.encodeCodon('G'));
    assertEquals(3, CodingUtils.encodeCodon('t'));
    assertEquals(3, CodingUtils.encodeCodon('T'));
    assertEquals(3, CodingUtils.encodeCodon('u'));
    assertEquals(3, CodingUtils.encodeCodon('U'));

    assertEquals(-1, CodingUtils.encodeCodon(null));
    assertEquals(0, CodingUtils.encodeCodon(new char[] { 'A', 'A', 'A' }));
    assertEquals(1, CodingUtils.encodeCodon(new char[] { 'A', 'A', 'C' }));
    assertEquals(2, CodingUtils.encodeCodon(new char[] { 'A', 'A', 'G' }));
    assertEquals(3, CodingUtils.encodeCodon(new char[] { 'A', 'A', 'T' }));
    assertEquals(4, CodingUtils.encodeCodon(new char[] { 'A', 'C', 'A' }));
    assertEquals(16, CodingUtils.encodeCodon(new char[] { 'C', 'A', 'A' }));
    assertEquals(42, CodingUtils.encodeCodon(new char[] { 'G', 'G', 'G' }));
    assertEquals(63, CodingUtils.encodeCodon(new char[] { 'T', 'T', 'T' }));
  }

}
