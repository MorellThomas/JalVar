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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class HiddenSequencesTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  static int SEQ_COUNT = 25;

  SequenceI[] seqs;

  /**
   * Set up an alignment of 10 sequences
   */
  @BeforeTest(alwaysRun = true)
  public void setUp()
  {
    seqs = new SequenceI[SEQ_COUNT];
    for (int i = 0; i < SEQ_COUNT; i++)
    {
      // sequence lengths are 1, 2, ... 25
      seqs[i] = new Sequence("Seq" + i,
              "abcdefghijklmnopqrstuvwxy".substring(0, i + 1));
    }
  }

  /**
   * Test the method that converts sequence alignment index to what it would be
   * if all sequences were unhidden
   */
  @Test(groups = "Functional")
  public void testAdjustForHiddenSeqs()
  {
    AlignmentI al = new Alignment(seqs);
    HiddenSequences hs = al.getHiddenSequences();
    for (int i = 0; i < SEQ_COUNT; i++)
    {
      assertEquals(i, hs.adjustForHiddenSeqs(i));
    }

    // hide seq1 and seq5 and seq6
    hs.hideSequence(seqs[1]);
    hs.hideSequence(seqs[5]);
    hs.hideSequence(seqs[6]);

    /*
     * alignment is now seq0/2/3/4/7/8/9
     */
    assertEquals(SEQ_COUNT - 3, al.getHeight());
    assertEquals(0, hs.adjustForHiddenSeqs(0));
    assertEquals(2, hs.adjustForHiddenSeqs(1));
    assertEquals(3, hs.adjustForHiddenSeqs(2));
    assertEquals(4, hs.adjustForHiddenSeqs(3));
    assertEquals(7, hs.adjustForHiddenSeqs(4));
    assertEquals(8, hs.adjustForHiddenSeqs(5));
    assertEquals(9, hs.adjustForHiddenSeqs(6));
  }

  /**
   * Test the method that increments the internal array size if a sequence is
   * added to the alignment (ugh this should not be exposed to the light of day)
   */
  @Test(groups = "Functional")
  public void testAdjustHeightSequenceAdded()
  {
    AlignmentI al = new Alignment(seqs);
    assertEquals(SEQ_COUNT, al.getHeight());

    HiddenSequences hs = al.getHiddenSequences();
    // initially does nothing
    hs.adjustHeightSequenceAdded();
    assertNull(hs.hiddenSequences);

    // hide one sequence
    hs.hideSequence(seqs[3]);
    assertEquals(1, hs.getSize());
    assertEquals(SEQ_COUNT - 1, al.getHeight());
    assertEquals(SEQ_COUNT, hs.hiddenSequences.length);

    /*
     * add a sequence to the alignment
     * - the safe way to call hs.adjustHeightSequenceAdded!
     * (implementation depends on alignment height having
     * been already updated for the added sequence)
     */
    al.addSequence(new Sequence("a", "b"));
    assertEquals(1, hs.getSize());
    assertEquals(SEQ_COUNT, al.getHeight());
    assertEquals(SEQ_COUNT + 1, hs.hiddenSequences.length);
  }

  /**
   * Test the method that decrements the internal array size if a sequence is
   * deleted from the alignment (ugh this should not be exposed to the light of
   * day)
   */
  @Test(groups = "Functional")
  public void testAdjustHeightSequenceDeleted()
  {
    AlignmentI al = new Alignment(seqs);
    assertEquals(SEQ_COUNT, al.getHeight());

    HiddenSequences hs = al.getHiddenSequences();
    // initially does nothing
    hs.adjustHeightSequenceAdded();
    assertNull(hs.hiddenSequences);

    // hide two sequences
    hs.hideSequence(seqs[3]);
    hs.hideSequence(seqs[5]);
    assertEquals(2, hs.getSize());
    assertTrue(hs.isHidden(seqs[3]));
    assertTrue(hs.isHidden(seqs[5]));
    assertEquals(SEQ_COUNT - 2, al.getHeight());
    assertEquals(SEQ_COUNT, hs.hiddenSequences.length);

    /*
     * delete a visible sequence from the alignment
     * - the safe way to call hs.adjustHeightSequenceDeleted!
     * (implementation depends on alignment height having
     * been already updated for the removed sequence)
     */
    al.deleteSequence(seqs[2]);
    assertEquals(2, hs.getSize());
    // the visible alignment is unchanged:
    assertEquals(SEQ_COUNT - 3, al.getHeight());
    // sequences array size has decremented:
    assertEquals(SEQ_COUNT - 1, hs.hiddenSequences.length);
  }

  /**
   * Test the method that converts a 'full alignment' sequence index into the
   * equivalent in the alignment with sequences hidden
   */
  @Test(groups = "Functional")
  public void testFindIndexWithoutHiddenSeqs()
  {
    AlignmentI al = new Alignment(seqs);
    HiddenSequences hs = al.getHiddenSequences();
    int height = al.getHeight();
    for (int i = 0; i < height; i++)
    {
      assertEquals(i, hs.findIndexWithoutHiddenSeqs(i));
    }

    // hide seq1 and seq5 and seq6
    hs.hideSequence(seqs[1]);
    hs.hideSequence(seqs[5]);
    hs.hideSequence(seqs[6]);

    /*
     * alignment is now seq0/2/3/4/7/8/9
     */
    assertEquals(height - 3, al.getHeight());
    assertEquals(0, hs.findIndexWithoutHiddenSeqs(0));
    assertEquals(0, hs.findIndexWithoutHiddenSeqs(1));
    assertEquals(1, hs.findIndexWithoutHiddenSeqs(2));
    assertEquals(2, hs.findIndexWithoutHiddenSeqs(3));
    assertEquals(3, hs.findIndexWithoutHiddenSeqs(4));
    assertEquals(3, hs.findIndexWithoutHiddenSeqs(5));
    assertEquals(3, hs.findIndexWithoutHiddenSeqs(6));
    assertEquals(4, hs.findIndexWithoutHiddenSeqs(7));
    assertEquals(5, hs.findIndexWithoutHiddenSeqs(8));
    assertEquals(6, hs.findIndexWithoutHiddenSeqs(9));

    /*
     * hide first two sequences
     */
    hs.showAll(null);
    hs.hideSequence(seqs[0]);
    hs.hideSequence(seqs[1]);
    assertEquals(-1, hs.findIndexWithoutHiddenSeqs(0));
    assertEquals(-1, hs.findIndexWithoutHiddenSeqs(1));
    for (int i = 2; i < height; i++)
    {
      assertEquals(i - 2, hs.findIndexWithoutHiddenSeqs(i));
    }
  }

  /**
   * Test the method that finds the visible row position a given distance before
   * another row
   */
  @Test(groups = { "Functional" })
  public void testFindIndexNFromRow()
  {
    AlignmentI al = new Alignment(seqs);
    HiddenSequences hs = new HiddenSequences(al);

    // test that without hidden rows, findIndexNFromRow returns
    // position n above provided position
    int pos = hs.subtractVisibleRows(3, 10);
    assertEquals(7, pos);

    // 0 returns same position
    pos = hs.subtractVisibleRows(0, 10);
    assertEquals(10, pos);

    // overflow to top returns negative number
    pos = hs.subtractVisibleRows(3, 0);
    assertEquals(-3, pos);

    // test that with hidden rows above result row
    // behaviour is the same as above
    hs.hideSequence(seqs[1]);
    hs.hideSequence(seqs[2]);
    hs.hideSequence(seqs[3]);

    // position n above provided position
    pos = hs.subtractVisibleRows(3, 10);
    assertEquals(7, pos);

    // 0 returns same position
    pos = hs.subtractVisibleRows(0, 10);
    assertEquals(10, pos);

    // test with one set of hidden rows between start and required position
    hs.hideSequence(seqs[12]);
    hs.hideSequence(seqs[13]);
    hs.hideSequence(seqs[14]);
    hs.hideSequence(seqs[15]);
    pos = hs.subtractVisibleRows(8, 17);
    assertEquals(5, pos);

    // test with two sets of hidden rows between start and required position
    hs.hideSequence(seqs[20]);
    hs.hideSequence(seqs[21]);
    pos = hs.subtractVisibleRows(8, 23);
    assertEquals(9, pos);

    // repeat last 2 tests with no hidden columns to left of required position
    hs.showAll(null);

    // test with one set of hidden rows between start and required position
    hs.hideSequence(seqs[12]);
    hs.hideSequence(seqs[13]);
    hs.hideSequence(seqs[14]);
    hs.hideSequence(seqs[15]);
    pos = hs.subtractVisibleRows(8, 17);
    assertEquals(5, pos);

    // test with two sets of hidden rows between start and required position
    hs.hideSequence(seqs[20]);
    hs.hideSequence(seqs[21]);
    pos = hs.subtractVisibleRows(8, 23);
    assertEquals(9, pos);

  }

  /**
   * Test the method that reconstructs (sort of) the full alignment including
   * hidden sequences
   */
  @Test(groups = "Functional")
  public void testGetFullAlignment()
  {
    AlignmentI al = new Alignment(seqs);
    assertArrayEquals(seqs, al.getSequencesArray());
    al.setProperty("a", "b");
    al.addAnnotation(new AlignmentAnnotation("ann", "label", 12f));
    al.setSeqrep(seqs[4]);
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(seqs[8], false);
    al.addGroup(sg);
    ((Alignment) al).hasRNAStructure = true;

    HiddenSequences hs = al.getHiddenSequences();
    AlignmentI al2 = hs.getFullAlignment();
    // new alignment but with original sequences
    assertNotSame(al, al2);
    assertArrayEquals(al.getSequencesArray(), al2.getSequencesArray());

    hs.hideSequence(seqs[4]);
    hs.hideSequence(seqs[9]);
    al2 = hs.getFullAlignment();
    assertNotSame(al, al2);
    assertArrayEquals(seqs, al2.getSequencesArray());
    assertNotNull(al2.getProperties());
    assertSame(al.getProperties(), al2.getProperties());
    assertNotNull(al2.getAlignmentAnnotation());
    assertSame(al.getAlignmentAnnotation(), al2.getAlignmentAnnotation());
    assertSame(seqs[4], al2.getSeqrep());
    assertNotNull(al2.getGroups());
    assertSame(al.getGroups(), al2.getGroups());
    assertTrue(al2.hasRNAStructure());
  }

  /**
   * Test the method that returns the hidden sequence at a given index in the
   * full alignment
   * 
   * @return either the sequence (if hidden) or null (if not hidden)
   */
  @Test(groups = "Functional")
  public void testGetHiddenSequence()
  {
    AlignmentI al = new Alignment(seqs);
    HiddenSequences hs = al.getHiddenSequences();
    assertNull(hs.getHiddenSequence(0));
    hs.hideSequence(seqs[3]);
    assertSame(seqs[3], hs.getHiddenSequence(3));
    assertNull(hs.getHiddenSequence(2));
    assertNull(hs.getHiddenSequence(4));
  }

  @Test(groups = "Functional")
  public void testGetSize()
  {
  }

  @Test(groups = "Functional")
  public void testGetWidth()
  {
    AlignmentI al = new Alignment(seqs);
    HiddenSequences hs = al.getHiddenSequences();
    assertEquals(0, hs.getWidth());
    hs.hideSequence(seqs[6]);
    hs.hideSequence(seqs[8]);
    assertEquals(9, hs.getWidth());
  }

  /**
   * Test the method that adds a sequence to the hidden sequences and deletes it
   * from the alignment, and its converse
   */
  @Test(groups = "Functional")
  public void testHideShowSequence()
  {
    AlignmentI al = new Alignment(seqs);
    assertTrue(al.getSequences().contains(seqs[1]));
    HiddenSequences hs = al.getHiddenSequences();
    assertEquals(0, hs.getSize());
    assertEquals(SEQ_COUNT, al.getHeight());

    /*
     * hide the second sequence in the alignment
     */
    hs.hideSequence(seqs[1]);
    assertFalse(hs.isHidden(seqs[0]));
    assertTrue(hs.isHidden(seqs[1]));
    assertFalse(al.getSequences().contains(seqs[1]));
    assertEquals(1, hs.getSize());
    assertEquals(SEQ_COUNT - 1, al.getHeight());
    assertSame(seqs[2], al.getSequenceAt(1));

    /*
     * hide what is now the second sequence in the alignment
     */
    hs.hideSequence(seqs[2]);
    assertFalse(hs.isHidden(seqs[0]));
    assertTrue(hs.isHidden(seqs[1]));
    assertTrue(hs.isHidden(seqs[2]));
    assertFalse(al.getSequences().contains(seqs[1]));
    assertFalse(al.getSequences().contains(seqs[2]));
    assertEquals(2, hs.getSize());
    assertEquals(SEQ_COUNT - 2, al.getHeight());

    /*
     * perform 'reveal' on what is now the second sequence in the alignment
     * this should unhide the two sequences that precede it
     */
    List<SequenceI> revealed = hs.showSequence(1, null);
    assertEquals(2, revealed.size());
    assertTrue(revealed.contains(seqs[1]));
    assertTrue(revealed.contains(seqs[2]));
    assertEquals(0, hs.getSize());
    assertEquals(SEQ_COUNT, al.getHeight());
  }

  /**
   * Test the method that adds a sequence to the hidden sequences and deletes it
   * from the alignment, and its converse, where the first hidden sequences are
   * at the bottom of the alignment (JAL-2437)
   */
  @Test(groups = "Functional")
  public void testHideShowLastSequences()
  {
    AlignmentI al = new Alignment(seqs);
    assertTrue(al.getSequences().contains(seqs[1]));
    HiddenSequences hs = al.getHiddenSequences();
    assertEquals(0, hs.getSize());
    assertEquals(SEQ_COUNT, al.getHeight());

    /*
     * hide the last sequence in the alignment
     */
    hs.hideSequence(seqs[SEQ_COUNT - 1]);
    assertFalse(hs.isHidden(seqs[SEQ_COUNT - 2]));
    assertTrue(hs.isHidden(seqs[SEQ_COUNT - 1]));
    assertFalse(al.getSequences().contains(seqs[SEQ_COUNT - 1]));
    assertEquals(1, hs.getSize());
    assertEquals(SEQ_COUNT - 1, al.getHeight());

    /*
     * hide the third last sequence in the alignment
     */
    hs.hideSequence(seqs[SEQ_COUNT - 3]);
    assertFalse(hs.isHidden(seqs[SEQ_COUNT - 2]));
    assertTrue(hs.isHidden(seqs[SEQ_COUNT - 3]));
    assertFalse(al.getSequences().contains(seqs[SEQ_COUNT - 3]));
    assertEquals(2, hs.getSize());
    assertEquals(SEQ_COUNT - 2, al.getHeight());

    /*
     * reveal all the sequences, which should be reinstated in the same order as they started in
     */
    hs.showAll(null);
    assertFalse(hs.isHidden(seqs[SEQ_COUNT - 3]));
    assertFalse(hs.isHidden(seqs[SEQ_COUNT - 1]));
    assertEquals(seqs[SEQ_COUNT - 3], al.getSequences().get(SEQ_COUNT - 3));
    assertEquals(seqs[SEQ_COUNT - 2], al.getSequences().get(SEQ_COUNT - 2));
    assertEquals(seqs[SEQ_COUNT - 1], al.getSequences().get(SEQ_COUNT - 1));
    assertEquals(0, hs.getSize());
    assertEquals(SEQ_COUNT, al.getHeight());
  }

  @Test(groups = "Functional")
  public void testIsHidden()
  {
    AlignmentI al = new Alignment(seqs);
    HiddenSequences hs = al.getHiddenSequences();
    hs.hideSequence(seqs[7]);
    hs.hideSequence(seqs[4]);
    assertTrue(hs.isHidden(seqs[4]));
    assertFalse(hs.isHidden(seqs[5]));
    assertFalse(hs.isHidden(seqs[6]));
    assertTrue(hs.isHidden(seqs[7]));
    assertFalse(hs.isHidden(null));
    assertFalse(hs.isHidden(new Sequence("", "")));
  }

  /**
   * Test hiding and unhiding a group with a representative sequence. The
   * representative should be left visible when the group is hidden, and
   * included in the selected group when it is unhidden.
   */
  @Test(groups = "Functional")
  public void testHideShowSequence_withHiddenRepSequence()
  {
    AlignmentI al = new Alignment(seqs);

    /*
     * represent seqs 2-4 with seq3
     * this hides seq2 and seq4 but not seq3
     */
    AlignViewport av = new AlignViewport(al);
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(seqs[1], false);
    sg.addSequence(seqs[2], false);
    sg.addSequence(seqs[3], false);
    av.setSelectionGroup(sg);

    /*
     * hiding group with reference sequence is done via AlignViewport
     */
    av.hideSequences(seqs[2], true);
    HiddenSequences hs = al.getHiddenSequences();
    assertEquals(2, hs.getSize());
    assertTrue(hs.isHidden(seqs[1]));
    assertFalse(hs.isHidden(seqs[2]));
    assertTrue(hs.isHidden(seqs[3]));

    /*
     * should now be no sequences selected in the alignment
     */
    assertNull(av.getSelectionGroup());

    /*
     * visible alignment is now seq0/2/4/5/6/7/8/9
     * 'reveal sequences' at the representative sequence (index = 1)
     * this should unhide the one above i.e. seq1
     * and return a selection list including seq2
     * 
     * note have to call via AlignViewport to get the expected
     * resulting sequence selection
     */
    av.showSequence(1);

    /*
     * only seq3 is now hidden
     */
    assertEquals(1, hs.getSize());
    assertTrue(hs.isHidden(seqs[3]));
    assertEquals(SEQ_COUNT - 1, al.getHeight());
    sg = av.getSelectionGroup();

    /*
     * unhidden and representative sequence selected
     * (this behaviour may change! JAL-2133)
     */
    assertEquals(2, sg.getSize());
    assertTrue(sg.getSequences().contains(seqs[1]));
    assertTrue(sg.getSequences().contains(seqs[2]));
    assertFalse(sg.getSequences().contains(seqs[3]));
  }
}
