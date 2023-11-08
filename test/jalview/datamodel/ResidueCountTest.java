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
package jalview.datamodel;

import java.util.Locale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.ResidueCount.SymbolCounts;
import jalview.gui.JvOptionPane;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResidueCountTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test a mix of add and put for nucleotide counting
   */
  @Test(groups = "Functional")
  public void test_countNucleotide()
  {
    ResidueCount rc = new ResidueCount(true);
    assertEquals(rc.getCount('A'), 0);
    assertEquals(rc.getGapCount(), 0);
    // add then add
    assertEquals(rc.add('A'), 1);
    assertEquals(rc.add('a'), 2);
    // put then add
    rc.put('g', 3);
    assertEquals(rc.add('G'), 4);
    // add then put
    assertEquals(rc.add('c'), 1);
    rc.put('C', 4);
    assertEquals(rc.add('N'), 1);

    assertEquals(rc.getCount('a'), 2);
    assertEquals(rc.getCount('A'), 2);
    assertEquals(rc.getCount('G'), 4);
    assertEquals(rc.getCount('c'), 4);
    assertEquals(rc.getCount('T'), 0); // never seen
    assertEquals(rc.getCount('N'), 1);
    assertEquals(rc.getCount('?'), 0);
    assertEquals(rc.getCount('-'), 0);

    assertFalse(rc.isCountingInts());
    assertFalse(rc.isUsingOtherData());
  }

  /**
   * Test adding to gap count (either using addGap or add)
   */
  @Test(groups = "Functional")
  public void testAddGap()
  {
    ResidueCount rc = new ResidueCount(true);
    rc.addGap();
    rc.add('-');
    rc.add('.');
    rc.add(' ');

    assertEquals(rc.getGapCount(), 4);
    assertEquals(rc.getCount(' '), 4);
    assertEquals(rc.getCount('-'), 4);
    assertEquals(rc.getCount('.'), 4);
    assertFalse(rc.isUsingOtherData());
    assertFalse(rc.isCountingInts());

    rc.set(ResidueCount.GAP_COUNT, Short.MAX_VALUE - 2);
    assertEquals(rc.getGapCount(), Short.MAX_VALUE - 2);
    assertFalse(rc.isCountingInts());
    rc.addGap();
    assertEquals(rc.getGapCount(), Short.MAX_VALUE - 1);
    assertFalse(rc.isCountingInts());
    rc.addGap();
    assertEquals(rc.getGapCount(), Short.MAX_VALUE);
    rc.addGap();
    assertTrue(rc.isCountingInts());
    assertEquals(rc.getGapCount(), Short.MAX_VALUE + 1);
  }

  @Test(groups = "Functional")
  public void testOverflow()
  {
    /*
     * overflow from add
     */
    ResidueCount rc = new ResidueCount(true);
    rc.addGap();
    rc.put('A', Short.MAX_VALUE - 1);
    assertFalse(rc.isCountingInts());
    rc.add('A');
    assertFalse(rc.isCountingInts());
    rc.add('A');
    assertTrue(rc.isCountingInts());
    assertEquals(rc.getCount('a'), Short.MAX_VALUE + 1);
    rc.add('A');
    assertTrue(rc.isCountingInts());
    assertEquals(rc.getCount('a'), Short.MAX_VALUE + 2);
    assertEquals(rc.getGapCount(), 1);
    rc.addGap();
    assertEquals(rc.getGapCount(), 2);

    /*
     * overflow from put
     */
    rc = new ResidueCount(true);
    rc.put('G', Short.MAX_VALUE + 1);
    assertTrue(rc.isCountingInts());
    assertEquals(rc.getCount('g'), Short.MAX_VALUE + 1);
    rc.put('G', 1);
    assertTrue(rc.isCountingInts());
    assertEquals(rc.getCount('g'), 1);

    /*
     * underflow from put
     */
    rc = new ResidueCount(true);
    rc.put('G', Short.MIN_VALUE - 1);
    assertTrue(rc.isCountingInts());
    assertEquals(rc.getCount('g'), Short.MIN_VALUE - 1);
  }

  /**
   * Test a mix of add and put for peptide counting
   */
  @Test(groups = "Functional")
  public void test_countPeptide()
  {
    ResidueCount rc = new ResidueCount(false);
    rc.put('q', 4);
    rc.add('Q');
    rc.add('X');
    rc.add('x');
    rc.add('W');
    rc.put('w', 7);
    rc.put('m', 12);
    rc.put('M', 13);

    assertEquals(rc.getCount('q'), 5);
    assertEquals(rc.getCount('X'), 2);
    assertEquals(rc.getCount('W'), 7);
    assertEquals(rc.getCount('m'), 13);
    assertEquals(rc.getCount('G'), 0);
    assertEquals(rc.getCount('-'), 0);

    assertFalse(rc.isCountingInts());
    assertFalse(rc.isUsingOtherData());
  }

  @Test(groups = "Functional")
  public void test_unexpectedPeptide()
  {
    ResidueCount rc = new ResidueCount(false);
    // expected characters (upper or lower case):
    String aas = "ACDEFGHIKLMNPQRSTVWXY";
    String lower = aas.toLowerCase(Locale.ROOT);
    for (int i = 0; i < aas.length(); i++)
    {
      rc.put(aas.charAt(i), i);
      rc.add(lower.charAt(i));
    }
    for (int i = 0; i < aas.length(); i++)
    {
      assertEquals(rc.getCount(aas.charAt(i)), i + 1);
    }
    assertFalse(rc.isUsingOtherData());

    rc.put('J', 4);
    assertTrue(rc.isUsingOtherData());
    assertEquals(rc.getCount('J'), 4);
    rc.add('j');
    assertEquals(rc.getCount('J'), 5);
  }

  @Test(groups = "Functional")
  public void test_unexpectedNucleotide()
  {
    ResidueCount rc = new ResidueCount(true);
    // expected characters (upper or lower case):
    String nucs = "ACGTUN";
    String lower = nucs.toLowerCase(Locale.ROOT);
    for (int i = 0; i < nucs.length(); i++)
    {
      rc.put(nucs.charAt(i), i);
      rc.add(lower.charAt(i));
    }
    for (int i = 0; i < nucs.length(); i++)
    {
      assertEquals(rc.getCount(nucs.charAt(i)), i + 1);
    }
    assertFalse(rc.isUsingOtherData());

    rc.add('J');
    assertTrue(rc.isUsingOtherData());
  }

  @Test(groups = "Functional")
  public void testGetModalCount()
  {
    ResidueCount rc = new ResidueCount(true);
    rc.add('c');
    rc.add('g');
    rc.add('c');
    assertEquals(rc.getModalCount(), 2);

    // modal count is in the 'short overflow' counts
    rc = new ResidueCount();
    rc.add('c');
    rc.put('g', Short.MAX_VALUE);
    rc.add('G');
    assertEquals(rc.getModalCount(), Short.MAX_VALUE + 1);

    // modal count is in the 'other data' counts
    rc = new ResidueCount(false);
    rc.add('Q');
    rc.add('{');
    rc.add('{');
    assertEquals(rc.getModalCount(), 2);

    // verify modal count excludes gap
    rc = new ResidueCount();
    rc.add('Q');
    rc.add('P');
    rc.add('Q');
    rc.addGap();
    rc.addGap();
    rc.addGap();
    assertEquals(rc.getModalCount(), 2);
  }

  @Test(groups = "Functional")
  public void testGetResiduesForCount()
  {
    ResidueCount rc = new ResidueCount(true);
    rc.add('c');
    rc.add('g');
    rc.add('c');
    assertEquals(rc.getResiduesForCount(2), "C");
    assertEquals(rc.getResiduesForCount(1), "G");
    assertEquals(rc.getResiduesForCount(3), "");
    assertEquals(rc.getResiduesForCount(0), "");
    assertEquals(rc.getResiduesForCount(-1), "");

    // modal count is in the 'short overflow' counts
    rc = new ResidueCount(true);
    rc.add('c');
    rc.put('g', Short.MAX_VALUE);
    rc.add('G');
    assertEquals(rc.getResiduesForCount(Short.MAX_VALUE + 1), "G");
    assertEquals(rc.getResiduesForCount(1), "C");

    // peptide modal count is in the 'short overflow' counts
    rc = new ResidueCount(false);
    rc.add('c');
    rc.put('p', Short.MAX_VALUE);
    rc.add('P');
    assertEquals(rc.getResiduesForCount(Short.MAX_VALUE + 1), "P");
    assertEquals(rc.getResiduesForCount(1), "C");

    // modal count is in the 'other data' counts
    rc = new ResidueCount();
    rc.add('Q');
    rc.add('{');
    rc.add('{');
    assertEquals(rc.getResiduesForCount(1), "Q");
    assertEquals(rc.getResiduesForCount(2), "{");

    // residues share modal count
    rc = new ResidueCount();
    rc.add('G');
    rc.add('G');
    rc.add('c');
    rc.add('C');
    rc.add('U');
    assertEquals(rc.getResiduesForCount(1), "U");
    assertEquals(rc.getResiduesForCount(2), "CG");

    // expected and unexpected symbols share modal count
    rc = new ResidueCount();
    rc.add('G');
    rc.add('t');
    rc.add('[');
    rc.add('[');
    rc.add('t');
    rc.add('G');
    rc.add('c');
    rc.add('C');
    rc.add('U');
    assertEquals(rc.getResiduesForCount(1), "U");
    assertEquals(rc.getResiduesForCount(2), "CGT[");
  }

  @Test(groups = "Functional")
  public void testGetSymbolCounts_nucleotide()
  {
    ResidueCount rc = new ResidueCount(true);
    rc.add('g');
    rc.add('c');
    rc.add('G');
    rc.add('J'); // 'otherData'
    rc.add('g');
    rc.add('N');
    rc.put('[', 0); // 'otherdata'

    SymbolCounts sc = rc.getSymbolCounts();
    Assert.assertArrayEquals(new char[] { 'C', 'G', 'N', 'J', '[' },
            sc.symbols);
    Assert.assertArrayEquals(new int[] { 1, 3, 1, 1, 0 }, sc.values);

    // now with overflow to int counts
    rc.put('U', Short.MAX_VALUE);
    rc.add('u');
    sc = rc.getSymbolCounts();
    Assert.assertArrayEquals(new char[] { 'C', 'G', 'N', 'U', 'J', '[' },
            sc.symbols);
    Assert.assertArrayEquals(new int[] { 1, 3, 1, 32768, 1, 0 }, sc.values);
  }

  @Test(groups = "Functional")
  public void testGetSymbolCounts_peptide()
  {
    ResidueCount rc = new ResidueCount(false);
    rc.add('W');
    rc.add('q');
    rc.add('W');
    rc.add('Z'); // 'otherData'
    rc.add('w');
    rc.add('L');

    SymbolCounts sc = rc.getSymbolCounts();
    Assert.assertArrayEquals(new char[] { 'L', 'Q', 'W', 'Z' }, sc.symbols);
    Assert.assertArrayEquals(new int[] { 1, 1, 3, 1 }, sc.values);

    // now with overflow to int counts
    rc.put('W', Short.MAX_VALUE);
    rc.add('W');
    sc = rc.getSymbolCounts();
    Assert.assertArrayEquals(new char[] { 'L', 'Q', 'W', 'Z' }, sc.symbols);
    Assert.assertArrayEquals(new int[] { 1, 1, 32768, 1 }, sc.values);
  }

  @Test(groups = "Functional")
  public void testToString()
  {
    ResidueCount rc = new ResidueCount();
    rc.add('q');
    rc.add('c');
    rc.add('Q');
    assertEquals(rc.toString(), "[ C:1 Q:2 ]");

    // add 'other data'
    rc.add('{');
    assertEquals(rc.toString(), "[ C:1 Q:2 {:1 ]");

    // switch from short to int counting:
    rc.put('G', Short.MAX_VALUE);
    rc.add('g');
    assertEquals(rc.toString(), "[ C:1 G:32768 Q:2 {:1 ]");
  }

  @Test(groups = "Functional")
  public void testGetTooltip()
  {
    ResidueCount rc = new ResidueCount();

    // no counts!
    assertEquals(rc.getTooltip(20, 1), "");

    /*
     * count 7 C, 6 K, 7 Q, 10 P, 9 W, 1 F (total 40)
     */
    for (int i = 0; i < 7; i++)
    {
      rc.add('c');
      rc.add('q');
    }
    for (int i = 0; i < 10; i++)
    {
      rc.add('p');
    }
    for (int i = 0; i < 9; i++)
    {
      rc.add('W');
    }
    for (int i = 0; i < 6; i++)
    {
      rc.add('K');
    }
    rc.add('F');

    /*
     * percentages are rounded (0.5 rounded up)
     * 10/40 9/40 7/40 6/40 1/40
     */
    assertEquals(rc.getTooltip(40, 0),
            "P 25%; W 23%; C 18%; Q 18%; K 15%; F 3%");

    rc.add('Q');
    /*
     * 10/30 9/30 8/30 7/30 6/30 1/30
     */
    assertEquals(rc.getTooltip(30, 1),
            "P 33.3%; W 30.0%; Q 26.7%; C 23.3%; K 20.0%; F 3.3%");
  }

  @Test(groups = "Functional")
  public void testPut()
  {
    ResidueCount rc = new ResidueCount();
    rc.put('q', 3);
    assertEquals(rc.getCount('Q'), 3);
    rc.put(' ', 4);
    assertEquals(rc.getGapCount(), 4);
    rc.put('.', 5);
    assertEquals(rc.getGapCount(), 5);
    rc.put('-', 6);
    assertEquals(rc.getGapCount(), 6);

    rc.put('?', 5);
    assertEquals(rc.getCount('?'), 5);
    rc.put('?', 6);
    rc.put('!', 7);
    assertEquals(rc.getCount('?'), 6);
    assertEquals(rc.getCount('!'), 7);
  }
}
