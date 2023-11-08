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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import jalview.analysis.Conservation;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import junit.extensions.PA;

public class SequenceGroupTest
{
  @Test(groups = { "Functional" })
  public void testAddSequence()
  {
    SequenceGroup sg = new SequenceGroup();
    assertTrue(sg.getSequences().isEmpty());

    SequenceI seq1 = new Sequence("seq1", "abc");
    SequenceI seq2 = new Sequence("seq2", "abc");
    SequenceI seq3 = new Sequence(seq1);

    sg.addSequence(null, false);
    assertTrue(sg.getSequences().isEmpty());
    sg.addSequence(seq1, false);
    assertEquals(sg.getSequences().size(), 1);
    assertTrue(sg.getSequences().contains(seq1));
    // adding the same sequence again does nothing
    sg.addSequence(seq1, false);
    assertEquals(sg.getSequences().size(), 1);
    assertTrue(sg.getSequences().contains(seq1));
    sg.addSequence(seq2, false);
    sg.addSequence(seq2, false);
    sg.addSequence(seq3, false);
    assertEquals(sg.getSequences().size(), 3);
    assertTrue(sg.getSequences().contains(seq1));
    assertTrue(sg.getSequences().contains(seq2));
    assertTrue(sg.getSequences().contains(seq3));
  }

  @Test(groups = { "Functional" })
  public void testAddOrRemove()
  {
    SequenceGroup sg = new SequenceGroup();
    assertTrue(sg.getSequences().isEmpty());

    SequenceI seq1 = new Sequence("seq1", "abc");
    SequenceI seq2 = new Sequence("seq2", "abc");
    SequenceI seq3 = new Sequence(seq1);

    sg.addOrRemove(seq1, false);
    assertEquals(sg.getSequences().size(), 1);
    sg.addOrRemove(seq2, false);
    assertEquals(sg.getSequences().size(), 2);
    sg.addOrRemove(seq3, false);
    assertEquals(sg.getSequences().size(), 3);
    assertTrue(sg.getSequences().contains(seq1));
    assertTrue(sg.getSequences().contains(seq2));
    assertTrue(sg.getSequences().contains(seq3));
    sg.addOrRemove(seq1, false);
    assertEquals(sg.getSequences().size(), 2);
    assertFalse(sg.getSequences().contains(seq1));
  }

  @Test(groups = { "Functional" })
  public void testGetColourScheme()
  {
    SequenceGroup sg = new SequenceGroup();
    assertNotNull(sg.getGroupColourScheme());
    assertNull(sg.getColourScheme());

    sg.setGroupColourScheme(null);
    assertNull(sg.getColourScheme());

    NucleotideColourScheme scheme = new NucleotideColourScheme();
    sg.setColourScheme(scheme);
    assertSame(scheme, sg.getColourScheme());
  }

  @Test(groups = { "Functional" })
  public void testSetContext()
  {
    SequenceGroup sg1 = new SequenceGroup();
    SequenceGroup sg2 = new SequenceGroup();
    SequenceGroup sg3 = new SequenceGroup();
    assertNull(sg1.getContext());
    sg1.setContext(null);
    assertNull(sg1.getContext());
    try
    {
      sg1.setContext(sg1); // self-reference :-O
      fail("Expected exception");
    } catch (IllegalArgumentException e)
    {
      // expected
      assertNull(sg1.getContext());
    }
    sg1.setContext(sg2);
    assertSame(sg2, sg1.getContext());
    sg2.setContext(sg3);
    try
    {
      sg3.setContext(sg1); // circular reference :-O
      fail("Expected exception");
    } catch (IllegalArgumentException e)
    {
      // expected
      assertNull(sg3.getContext());
    }

    /*
     * use PrivilegedAccessor to 'force' a SequenceGroup with
     * a circular context reference
     */
    PA.setValue(sg2, "context", sg2);
    try
    {
      sg3.setContext(sg2, false); // circular reference in sg2
      fail("Expected exception");
    } catch (IllegalArgumentException e)
    {
      // expected
      assertNull(sg3.getContext());
    }

    // test isDefined setting behaviour
    sg2 = new SequenceGroup();
    sg1.setContext(null, false);
    assertFalse(sg1.isDefined());

    sg1.setContext(sg2, false);
    assertFalse(sg1.isDefined());

    sg1.setContext(sg2, true);
    assertTrue(sg1.isDefined());

    // setContext without defined parameter does not change isDefined
    sg1.setContext(null);
    assertTrue(sg1.isDefined());

    sg1.setContext(null, false);
    sg1.setContext(sg2);
    assertFalse(sg1.isDefined());
  }

  @Test(groups = { "Functional" })
  public void testContains()
  {
    /* 
     * essentially the same tests as AlignmentI.findGroup 
     * but from a particular group's perspective
     */

    SequenceI seq1 = new Sequence("seq1", "ABCDEF---GHI");
    SequenceI seq2 = new Sequence("seq2", "---JKLMNO---");
    AlignmentI a = new Alignment(new SequenceI[] { seq1, seq2 });
    /*
     * add a group consisting of just "DEF"
     */
    SequenceGroup sg1 = new SequenceGroup();
    sg1.addSequence(seq1, false);
    sg1.setStartRes(3);
    sg1.setEndRes(5);

    /*
     * test sequence membership
     */
    assertTrue(sg1.contains(seq1));
    assertFalse(sg1.contains(seq2));

    /*
     * test sequence+position
     */

    assertFalse(sg1.contains(seq1, 2)); // position not in group
    assertFalse(sg1.contains(seq1, 6)); // position not in group
    assertFalse(sg1.contains(seq2, 5)); // sequence not in group
    assertTrue(sg1.contains(seq1, 3)); // yes
    assertTrue(sg1.contains(seq1, 4));
    assertTrue(sg1.contains(seq1, 5));

    /*
     * add a group consisting of 
     * EF--
     * KLMN
     */
    SequenceGroup sg2 = new SequenceGroup();
    sg2.addSequence(seq1, false);
    sg2.addSequence(seq2, false);
    sg2.setStartRes(4);
    sg2.setEndRes(7);
    a.addGroup(sg2);

    /*
     * if a residue is in more than one group, method returns
     * the first found (in order groups were added)
     */
    assertTrue(sg2.contains(seq1, 4));
    assertTrue(sg2.contains(seq1, 5));

    /*
     * seq2 only belongs to the second group
     */
    assertTrue(sg2.contains(seq2, 4));
    assertTrue(sg2.contains(seq2, 5));
    assertTrue(sg2.contains(seq2, 6));
    assertTrue(sg2.contains(seq2, 7));
    assertFalse(sg2.contains(seq2, 3));
    assertFalse(sg2.contains(seq2, 8));
    sg2.setEndRes(8);
    assertTrue(sg2.contains(seq2, 8));
    sg2.deleteSequence(seq2, false);
    assertFalse(sg2.contains(seq2));
  }

  @Test(groups = { "Functional" })
  public void testCopyConstructor()
  {
    SequenceI seq = new Sequence("seq", "ABC");
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(seq, false);
    sg.setName("g1");
    sg.setDescription("desc");
    sg.setColourScheme(new PIDColourScheme());
    Conservation cons = new Conservation("Cons", 2,
            Collections.<SequenceI> emptyList(), 3, 12);
    PA.setValue(cons, "consSequence", new Sequence("s", "abc"));
    sg.getGroupColourScheme().setConservation(cons);
    sg.getGroupColourScheme().setConsensus(new Profiles(null));
    sg.setDisplayBoxes(false);
    sg.setDisplayText(false);
    sg.setColourText(true);
    sg.isDefined = true;
    sg.setShowNonconserved(true);
    sg.setOutlineColour(Color.red);
    sg.setIdColour(Color.blue);
    sg.thresholdTextColour = 1;
    sg.textColour = Color.orange;
    sg.textColour2 = Color.yellow;
    sg.setIgnoreGapsConsensus(false);
    sg.setshowSequenceLogo(true);
    sg.setNormaliseSequenceLogo(true);
    sg.setHidereps(true);
    sg.setHideCols(true);
    sg.setShowConsensusHistogram(true);
    sg.setContext(new SequenceGroup());

    SequenceGroup sg2 = new SequenceGroup(sg);
    assertEquals(sg2.getName(), sg.getName());
    assertEquals(sg2.getDescription(), sg.getDescription());
    assertNotSame(sg2.getGroupColourScheme(), sg.getGroupColourScheme());
    assertSame(sg2.getColourScheme(), sg.getColourScheme());
    assertSame(PA.getValue(sg2.getGroupColourScheme(), "consensus"),
            PA.getValue(sg.getGroupColourScheme(), "consensus"));
    assertSame(PA.getValue(sg2.getGroupColourScheme(), "conservation"),
            PA.getValue(sg.getGroupColourScheme(), "conservation"));
    assertEquals(sg2.getDisplayBoxes(), sg.getDisplayBoxes());
    assertEquals(sg2.getDisplayText(), sg.getDisplayText());
    assertEquals(sg2.getColourText(), sg.getColourText());
    assertEquals(sg2.getShowNonconserved(), sg.getShowNonconserved());
    assertEquals(sg2.getOutlineColour(), sg.getOutlineColour());
    assertEquals(sg2.getIdColour(), sg.getIdColour());
    assertEquals(sg2.thresholdTextColour, sg.thresholdTextColour);
    assertEquals(sg2.textColour, sg.textColour);
    assertEquals(sg2.textColour2, sg.textColour2);
    assertEquals(sg2.getIgnoreGapsConsensus(), sg.getIgnoreGapsConsensus());
    assertEquals(sg2.isShowSequenceLogo(), sg.isShowSequenceLogo());
    assertEquals(sg2.isNormaliseSequenceLogo(),
            sg.isNormaliseSequenceLogo());
    assertEquals(sg2.isHidereps(), sg.isHidereps());
    assertEquals(sg2.isHideCols(), sg.isHideCols());
    assertEquals(sg2.isShowConsensusHistogram(),
            sg.isShowConsensusHistogram());

    /*
     * copy of sequences
     */
    assertNotSame(sg2.getSequences(), sg.getSequences());
    assertEquals(sg2.getSequences(), sg.getSequences());

    /*
     * isDefined should only be set true when a new group is added to
     * an alignment, not in the copy constructor
     */
    assertFalse(sg2.isDefined());

    /*
     * context should be set explicitly, not by copy
     */
    assertNull(sg2.getContext());
  }

  @Test(groups = { "Functional" })
  public void testConstructor_list()
  {
    SequenceI s1 = new Sequence("abcde", "fg");
    SequenceI s2 = new Sequence("foo", "bar");
    List<SequenceI> seqs = new ArrayList<SequenceI>();
    seqs.add(s1);
    seqs.add(s2);
    SequenceGroup sg = new SequenceGroup(seqs);

    /*
     * verify sg has a copy of the original list
     */
    List<SequenceI> sgList = sg.getSequences();
    assertNotSame(sgList, seqs);
    assertEquals(sgList, seqs);

    /*
     * add to sgList, original is unchanged
     */
    sg.addSequence(new Sequence("bar", "foo"), false);
    assertEquals(sgList.size(), 3);
    assertEquals(seqs.size(), 2);

    /*
     * delete from original, sgList is unchanged
     */
    seqs.remove(s1);
    assertEquals(sgList.size(), 3);
    assertEquals(seqs.size(), 1);
  }
}
