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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class QuickSortTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private static final String c1 = "Blue";

  private static final String c2 = "Yellow";

  private static final String c3 = "Orange";

  private static final String c4 = "Green";

  private static final String c5 = "Pink";

  @Test(groups = { "Functional" })
  public void testSort_byIntValues()
  {
    int[] values = new int[] { 3, 0, 4, 3, -1 };
    Object[] things = new Object[] { c1, c2, c3, c4, c5 };

    QuickSort.sort(values, things);
    assertTrue(Arrays.equals(new int[] { -1, 0, 3, 3, 4 }, values));
    // note sort is not stable: c1/c4 are equal but get reordered
    Object[] expect = new Object[] { c5, c2, c4, c1, c3 };
    assertTrue(Arrays.equals(expect, things));
  }

  /**
   * Test the alternative sort objects by integer method
   */
  @Test(groups = { "Functional" })
  public void testSortByInt()
  {
    int[] values = new int[] { 3, 0, 4, 3, -1 };
    Object[] things = new Object[] { c1, c2, c3, c4, c5 };

    /*
     * sort ascending
     */
    QuickSort.sortByInt(values, things, true);
    assertTrue(Arrays.equals(new int[] { -1, 0, 3, 3, 4 }, values));
    assertTrue(Arrays.equals(new Object[] { c5, c2, c1, c4, c3 }, things));

    /*
     * resort descending; c1/c4 should not change order
     */
    QuickSort.sortByInt(values, things, false);
    assertTrue(Arrays.equals(new int[] { 4, 3, 3, 0, -1 }, values));
    assertTrue(Arrays.equals(new Object[] { c3, c1, c4, c2, c5 }, things));
  }

  @Test(groups = { "Functional" })
  public void testSort_byFloatValues()
  {
    float[] values = new float[] { 3f, 0f, 4f, 3f, -1f };
    Object[] things = new Object[] { c1, c2, c3, c4, c5 };
    QuickSort.sort(values, things);
    assertTrue(Arrays.equals(new float[] { -1f, 0f, 3f, 3f, 4f }, values));
    // note sort is not stable: c1/c4 are equal but get reordered
    assertTrue(Arrays.equals(new Object[] { c5, c2, c4, c1, c3 }, things));
  }

  @Test(groups = { "Functional" })
  public void testSort_byDoubleValues()
  {
    double[] values = new double[] { 3d, 0d, 4d, 3d, -1d };
    Object[] things = new Object[] { c1, c2, c3, c4, c5 };
    QuickSort.sort(values, things);
    assertTrue(Arrays.equals(new double[] { -1d, 0d, 3d, 3d, 4d }, values));
    // note sort is not stable: c1/c4 are equal but get reordered
    assertTrue(Arrays.equals(new Object[] { c5, c2, c4, c1, c3 }, things));
  }

  /**
   * Sort by String is descending order, case-sensitive
   */
  @Test(groups = { "Functional" })
  public void testSort_byStringValues()
  {
    Object[] things = new Object[] { c1, c2, c3, c4, c5 };
    String[] values = new String[] { "JOHN", "henry", "lucy", "henry",
        "ALISON" };
    QuickSort.sort(values, things);
    assertTrue(
            Arrays.equals(new String[]
            { "lucy", "henry", "henry", "JOHN", "ALISON" }, values));
    assertTrue(Arrays.equals(new Object[] { c3, c2, c4, c1, c5 }, things));
  }

  /**
   * Test whether sort is stable i.e. equal values retain their mutual ordering.
   */
  @Test(groups = { "Functional" }, enabled = false)
  public void testSort_withDuplicates()
  {
    int[] values = new int[] { 3, 4, 2, 4, 1 };
    Object[] letters = new Object[] { "A", "X", "Y", "B", "Z" };
    QuickSort.sort(values, letters);
    assertTrue(Arrays.equals(new int[] { 1, 2, 3, 4, 4 }, values));
    // this fails - do we care?
    assertTrue(
            Arrays.equals(new Object[]
            { "Z", "Y", "A", "X", "B" }, letters));
  }

  /**
   * Test of method that sorts chars by a float array
   */
  @Test(groups = { "Functional" })
  public void testSort_charSortByFloat_mostlyZeroValues()
  {
    char[] residues = new char[64];
    for (int i = 0; i < 64; i++)
    {
      residues[i] = (char) i;
    }
    float[] counts = new float[64];
    counts[43] = 16;
    counts[59] = 7;
    counts[62] = -2;
    QuickSort.sort(counts, residues);
    assertEquals(62, residues[0]); // negative sorts to front
    assertEquals(59, residues[62]); // 7 sorts to next-to-end
    assertEquals(43, residues[63]); // 16 sorts to end
  }

  /**
   * Timing test - to be run manually as needed, not part of the automated
   * suite. <br>
   * It shows that the optimised sort is 3-4 times faster than the simple
   * external sort if the data to be sorted is mostly zero, but slightly slower
   * if the data is fully populated with non-zero values. Worst case for an
   * array of size 256 is about 100 sorts per millisecond.
   */
  @Test(groups = { "Timing" }, enabled = false)
  public void testSort_timingTest()
  {
    char[] residues = new char[256];
    for (int i = 0; i < residues.length; i++)
    {
      residues[i] = (char) i;
    }
    float[] counts = new float[residues.length];

    int iterations = 1000000;

    /*
     * time it using optimised sort (of a mostly zero-filled array)
     */
    long start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++)
    {
      Arrays.fill(counts, 0f);
      counts[43] = 16;
      counts[59] = 7;
      counts[62] = -2;
      QuickSort.sort(counts, residues);
    }
    long elapsed = System.currentTimeMillis() - start;
    System.out.println(String.format(
            "Time for %d optimised sorts of mostly zeros array length %d was %dms",
            iterations, counts.length, elapsed));

    /*
     * time it using unoptimised external sort
     */
    start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++)
    {
      Arrays.fill(counts, 0f);
      counts[43] = 16;
      counts[59] = 7;
      counts[62] = -2;
      QuickSort.charSortByFloat(counts, residues, true);
    }
    elapsed = System.currentTimeMillis() - start;
    System.out.println(String.format(
            "Time for %d external sorts of mostly zeros array length %d was %dms",
            iterations, counts.length, elapsed));

    /*
     * optimised external sort, well-filled array
     */
    Random random = new Random();
    float[] randoms = new float[counts.length];
    for (int i = 0; i < randoms.length; i++)
    {
      randoms[i] = random.nextFloat();
    }

    start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++)
    {
      System.arraycopy(randoms, 0, counts, 0, randoms.length);
      QuickSort.sort(counts, residues);
    }
    elapsed = System.currentTimeMillis() - start;
    System.out.println(String.format(
            "Time for %d optimised sorts of non-zeros array length %d was %dms",
            iterations, counts.length, elapsed));

    /*
     * time unoptimised external sort, filled array
     */
    start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++)
    {
      System.arraycopy(randoms, 0, counts, 0, randoms.length);
      QuickSort.charSortByFloat(counts, residues, true);
    }
    elapsed = System.currentTimeMillis() - start;
    System.out.println(String.format(
            "Time for %d external sorts of non-zeros array length %d was %dms",
            iterations, counts.length, elapsed));
  }

  /**
   * Test that exercises sort without any attempt at fancy optimisation
   */
  @Test(groups = { "Functional" })
  public void testCharSortByFloat()
  {
    char[] residues = new char[64];
    for (int i = 0; i < 64; i++)
    {
      residues[i] = (char) i;
    }
    float[] counts = new float[64];
    counts[43] = 16;
    counts[59] = 7;
    counts[62] = -2;

    /*
     * sort ascending
     */
    QuickSort.charSortByFloat(counts, residues, true);
    assertEquals(62, residues[0]);
    assertEquals(59, residues[62]);
    assertEquals(43, residues[63]);

    /*
     * resort descending
     */
    QuickSort.charSortByFloat(counts, residues, false);
    assertEquals(62, residues[63]);
    assertEquals(59, residues[1]);
    assertEquals(43, residues[0]);
  }

  /**
   * Test of method that sorts chars by an int array
   */
  @Test(groups = { "Functional" })
  public void testSort_charSortByInt_mostlyZeroValues()
  {
    char[] residues = new char[64];
    for (int i = 0; i < 64; i++)
    {
      residues[i] = (char) i;
    }
    int[] counts = new int[64];
    counts[43] = 16;
    counts[59] = 7;
    counts[62] = -2;
    QuickSort.sort(counts, residues);
    assertEquals(62, residues[0]); // negative sorts to front
    assertEquals(59, residues[62]); // 7 sorts to next-to-end
    assertEquals(43, residues[63]); // 16 sorts to end
  }

  /**
   * Test that exercises sorting without any attempt at fancy optimisation.
   */
  @Test(groups = { "Functional" })
  public void testCharSortByInt()
  {
    char[] residues = new char[64];
    for (int i = 0; i < 64; i++)
    {
      residues[i] = (char) i;
    }
    int[] counts = new int[64];
    counts[43] = 16;
    counts[59] = 7;
    counts[62] = -2;

    /*
     * sort ascending
     */
    QuickSort.charSortByInt(counts, residues, true);
    assertEquals(62, residues[0]);
    assertEquals(59, residues[62]);
    assertEquals(43, residues[63]);

    /*
     * resort descending
     */
    QuickSort.charSortByInt(counts, residues, false);
    assertEquals(62, residues[63]);
    assertEquals(59, residues[1]);
    assertEquals(43, residues[0]);
  }

  /**
   * Tests the alternative method to sort bby String in descending order,
   * case-sensitive
   */
  @Test(groups = { "Functional" })
  public void testSortByString()
  {
    Object[] things = new Object[] { c1, c2, c3, c4, c5 };
    String[] values = new String[] { "JOHN", "henry", "lucy", "henry",
        "ALISON" };

    /*
     * sort descending
     */
    QuickSort.sortByString(values, things, false);
    assertTrue(
            Arrays.equals(new String[]
            { "lucy", "henry", "henry", "JOHN", "ALISON" }, values));
    assertTrue(Arrays.equals(new Object[] { c3, c2, c4, c1, c5 }, things));

    /*
     * resort ascending
     */
    QuickSort.sortByString(values, things, true);
    assertTrue(
            Arrays.equals(new String[]
            { "ALISON", "JOHN", "henry", "henry", "lucy" }, values));
    // sort is stable: c2/c4 do not swap order
    assertTrue(Arrays.equals(new Object[] { c5, c1, c2, c4, c3 }, things));
  }
}
