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

import jalview.analysis.AlignmentGenerator;

import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class VisibleRowsIteratorTest
{
  AlignmentI al;

  AlignmentI al2;

  AlignmentI al3;

  Hashtable<SequenceI, SequenceCollectionI> hiddenRepSequences = new Hashtable<SequenceI, SequenceCollectionI>();

  Hashtable<SequenceI, SequenceCollectionI> hiddenRepSequences2 = new Hashtable<SequenceI, SequenceCollectionI>();

  @BeforeClass(groups = { "Functional" })
  public void setup()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    al = gen.generate(20, 15, 123, 5, 5);
    if (!hiddenRepSequences.isEmpty())
    {
      al.getHiddenSequences().showAll(hiddenRepSequences);
    }
    hideSequences(al, hiddenRepSequences, 2, 4);

    al2 = gen.generate(20, 15, 123, 5, 5);
    if (!hiddenRepSequences2.isEmpty())
    {
      al2.getHiddenSequences().showAll(hiddenRepSequences2);
    }
    hideSequences(al2, hiddenRepSequences2, 0, 2);

    al3 = gen.generate(20, 15, 123, 5, 5);
  }

  /*
   * Test iterator iterates correctly through the rows
   * when alignment has hidden rows
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextWithHidden()
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 6, al);
    int count = 0;
    while (it.hasNext())
    {
      it.next();
      count++;
    }
    assertTrue(count == 4, "hasNext() is false after 4 iterations");
  }

  /*
   * Test iterator iterates correctly through the rows
   * when alignment has no hidden rows
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextNoHidden()
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 3, al3);
    int count = 0;
    while (it.hasNext())
    {
      it.next();
      count++;
    }
    assertTrue(count == 4, "hasNext() is false after 4 iterations");
  }

  /*
   * Test iterator iterates correctly through the rows
   * when alignment has hidden rows at start
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextStartHidden()
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 6, al2);
    int count = 0;
    while (it.hasNext())
    {
      it.next();
      count++;
    }
    assertTrue(count == 4, "hasNext() is false after 4 iterations");
  }

  /*
   * Test iterator iterates correctly through the rows
   * when alignment has hidden rows at end
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNextEndHidden()
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 4, al);
    int count = 0;
    while (it.hasNext())
    {
      it.next();
      count++;
    }
    assertTrue(count == 2, "hasNext() is false after 2 iterations");
  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has hidden rows
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextWithHidden() throws NoSuchElementException
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 3, al);
    while (it.hasNext())
    {
      it.next();
    }
    it.next();
  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has no hidden rows
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextNoHidden() throws NoSuchElementException
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 3, al3);
    while (it.hasNext())
    {
      it.next();
    }
    it.next();
  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has hidden rows at start
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextStartHidden() throws NoSuchElementException
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 3, al2);
    while (it.hasNext())
    {
      it.next();
    }
    it.next();
  }

  /*
   * Test iterator always throws NoSuchElementException at end of iteration
   * when alignment has hidden rows at end
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNextEndHidden() throws NoSuchElementException
  {
    VisibleRowsIterator it = new VisibleRowsIterator(0, 4, al);
    while (it.hasNext())
    {
      it.next();
    }
    it.next();
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
    VisibleRowsIterator it = new VisibleRowsIterator(0, 3, al);
    it.remove();
  }

  /*
   * Hide sequences between start and end
   */
  private void hideSequences(AlignmentI alignment,
          Hashtable<SequenceI, SequenceCollectionI> hiddenRepSequences,
          int start, int end)
  {
    SequenceI[] allseqs = alignment.getSequencesArray();
    SequenceGroup theseSeqs = new SequenceGroup();

    for (int i = start; i <= end; i++)
    {
      theseSeqs.addSequence(allseqs[i], false);
      alignment.getHiddenSequences().hideSequence(allseqs[i]);
    }

    hiddenRepSequences.put(allseqs[start], theseSeqs);
  }
}
