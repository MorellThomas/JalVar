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

public class AllRowsIteratorTest
{
  AlignmentI al;

  Hashtable<SequenceI, SequenceCollectionI> hiddenRepSequences = new Hashtable<>();

  @BeforeClass(alwaysRun = true)
  public void setup()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    al = gen.generate(20, 15, 123, 5, 5);
    if (!hiddenRepSequences.isEmpty())
    {
      al.getHiddenSequences().showAll(hiddenRepSequences);
    }
    hideSequences(2, 4);
  }

  /*
   * Test iterator iterates through collection correctly
   */
  @Test(groups = { "Functional" })
  public void testHasNextAndNext()
  {
    AllRowsIterator it = new AllRowsIterator(0, 3, al);
    int count = 0;
    while (it.hasNext())
    {
      it.next();
      count++;
    }
    assertTrue(count == 4, "hasNext() is false after 4 iterations");
  }

  /*
   * Test iterator throws NoSuchElementException at end of iteration
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { NoSuchElementException.class })
  public void testLastNext() throws NoSuchElementException
  {
    AllRowsIterator it = new AllRowsIterator(0, 3, al);
    while (it.hasNext())
    {
      it.next();
    }
    it.next();
  }

  /*
   * Test iterator throws UnsupportedOperationException on call to remove
   */
  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { UnsupportedOperationException.class })
  public void testRemove() throws UnsupportedOperationException
  {
    AllRowsIterator it = new AllRowsIterator(0, 3, al);
    it.remove();
  }

  /*
   * Hide sequences between start and end
   */
  private void hideSequences(int start, int end)
  {
    SequenceI[] allseqs = al.getSequencesArray();
    SequenceGroup theseSeqs = new SequenceGroup();

    for (int i = start; i <= end; i++)
    {
      theseSeqs.addSequence(allseqs[i], false);
      al.getHiddenSequences().hideSequence(allseqs[i]);
    }

    hiddenRepSequences.put(allseqs[start], theseSeqs);
  }

  /*
   * Test iterator behaves correctly when there is only one element in the collection
   */
  @Test(groups = { "Functional" })
  public void testOneElement()
  {
    AllRowsIterator it = new AllRowsIterator(0, 0, al);
    int count = 0;
    while (it.hasNext())
    {
      it.next();
      count++;
    }
    assertTrue(count == 1, "hasNext() is false after 1 iteration");
  }

}
