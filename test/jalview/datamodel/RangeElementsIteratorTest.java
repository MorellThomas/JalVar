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

import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RangeElementsIteratorTest
{
  HiddenColumns hiddenCols;

  HiddenColumns hiddenColsAtStart;

  @BeforeClass(groups = { "Functional" })
  public void setup()
  {
    hiddenCols = new HiddenColumns();
    hiddenCols.hideColumns(2, 4);

    hiddenColsAtStart = new HiddenColumns();
    hiddenColsAtStart.hideColumns(0, 2);
  }

  /*
   * Test iterator iterates correctly through the columns
   * when alignment has hidden cols
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextWithHidden()
  {
    Iterator<Integer> it = hiddenCols.getVisibleColsIterator(0, 6);
    int count = 0;
    while (it.hasNext())
    {
      int result = it.next();
      System.out.println(result);
      count++;
    }
    assertTrue(count == 4, "hasNext() is false after 4 iterations");
  }

  /*
   * Test iterator iterates correctly through the columns
   * when alignment has no hidden cols
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextNoHidden()
  {
    HiddenColumns test = new HiddenColumns();
    Iterator<Integer> it2 = test.getVisibleColsIterator(0, 3);
    int count = 0;
    while (it2.hasNext())
    {
      it2.next();
      count++;
    }
    assertTrue(count == 4, "hasNext() is false after 4 iterations");
  }

  /*
   * Test iterator iterates correctly through the columns
   * when alignment has hidden cols at start
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextStartHidden()
  {
    Iterator<Integer> it3 = hiddenColsAtStart.getVisibleColsIterator(0, 6);
    int count = 0;
    while (it3.hasNext())
    {
      it3.next();
      count++;
    }
    assertTrue(count == 4, "hasNext() is false after 4 iterations");
  }

  /*
   * Test iterator iterates correctly through the columns
   * when alignment has hidden cols at end
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextEndHidden()
  {
    Iterator<Integer> it4 = hiddenCols.getVisibleColsIterator(0, 4);
    int count = 0;
    while (it4.hasNext())
    {
      it4.next();
      count++;
    }
    assertTrue(count == 2, "hasNext() is false after 2 iterations");

  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has hidden cols
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextWithHidden() throws NoSuchElementException
  {
    Iterator<Integer> it = hiddenCols.getVisibleColsIterator(0, 3);
    while (it.hasNext())
    {
      it.next();
    }
    it.next();
  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has no hidden cols
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextNoHidden() throws NoSuchElementException
  {
    HiddenColumns test = new HiddenColumns();
    Iterator<Integer> it2 = test.getVisibleColsIterator(0, 3);
    while (it2.hasNext())
    {
      it2.next();
    }
    it2.next();
  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has hidden cols at start
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextStartHidden() throws NoSuchElementException
  {
    Iterator<Integer> it3 = hiddenColsAtStart.getVisibleColsIterator(0, 6);
    while (it3.hasNext())
    {
      it3.next();
    }
    it3.next();
  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has hidden cols at end
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextEndHidden() throws NoSuchElementException
  {
    Iterator<Integer> it4 = hiddenCols.getVisibleColsIterator(0, 4);
    while (it4.hasNext())
    {
      it4.next();
    }
    it4.next();
  }

  /*
   * Test calls to remove throw UnsupportedOperationException
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { UnsupportedOperationException.class })
  public void testRemove() throws UnsupportedOperationException
  {
    Iterator<Integer> it = hiddenCols.getVisibleColsIterator(0, 3);
    it.remove();
  }
}
