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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.analysis.AlignmentGenerator;
import jalview.bin.Cache;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.PDBEntry.Type;
import jalview.gui.JvOptionPane;
import jalview.util.MapList;
import junit.extensions.PA;

public class SequenceTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void loadProperties()
  {
    Cache.loadProperties("test/jalview/util/comparisonTestProps.jvprops");
  }

  Sequence seq;

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    seq = new Sequence("FER1", "AKPNGVL");
  }

  @Test(groups = { "Functional" })
  public void testInsertGapsAndGapmaps()
  {
    SequenceI aseq = seq.deriveSequence();
    aseq.insertCharAt(2, 3, '-');
    aseq.insertCharAt(6, 3, '-');
    assertEquals("Gap insertions not correct", "AK---P---NGVL",
            aseq.getSequenceAsString());
    List<int[]> gapInt = aseq.getInsertions();
    assertEquals("Gap interval 1 start wrong", 2, gapInt.get(0)[0]);
    assertEquals("Gap interval 1 end wrong", 4, gapInt.get(0)[1]);
    assertEquals("Gap interval 2 start wrong", 6, gapInt.get(1)[0]);
    assertEquals("Gap interval 2 end wrong", 8, gapInt.get(1)[1]);

    BitSet gapfield = aseq.getInsertionsAsBits();
    BitSet expectedgaps = new BitSet();
    expectedgaps.set(2, 5);
    expectedgaps.set(6, 9);

    assertEquals(6, expectedgaps.cardinality());

    assertEquals("getInsertionsAsBits didn't mark expected number of gaps",
            6, gapfield.cardinality());

    assertEquals("getInsertionsAsBits not correct.", expectedgaps,
            gapfield);
  }

  @Test(groups = ("Functional"))
  public void testIsProtein()
  {
    // test Protein
    assertTrue(new Sequence("prot", "ASDFASDFASDF").isProtein());
    // test DNA
    assertFalse(new Sequence("prot", "ACGTACGTACGT").isProtein());
    // test RNA
    SequenceI sq = new Sequence("prot", "ACGUACGUACGU");
    assertFalse(sq.isProtein());
    // change sequence, should trigger an update of cached result
    sq.setSequence("ASDFASDFADSF");
    assertTrue(sq.isProtein());
  }

  @Test(groups = ("Functional"))
  public void testIsProteinWithXorNAmbiguityCodes()
  {
    // test Protein with N - poly asparagine
    assertTrue(new Sequence("prot", "ASDFASDFASDFNNNNNNNNN").isProtein());
    assertTrue(new Sequence("prot", "NNNNNNNNNNNNNNNNNNNNN").isProtein());
    // test Protein with X
    assertTrue(new Sequence("prot", "ASDFASDFASDFXXXXXXXXX").isProtein());
    // test DNA with X
    assertFalse(new Sequence("prot", "ACGTACGTACGTXXXXXXXX").isProtein());
    // short sequence is nucleotide only if 50% is nucleotide and remaining N/X
    // is either N or X only
    assertTrue(new Sequence("prot", "ACGTACGTACGTXN").isProtein());
    // test DNA with N
    assertFalse(new Sequence("prot", "ACGTACGTACGTNNNNNNNN").isProtein());
    // test RNA with X
    assertFalse(new Sequence("prot", "ACGUACGUACGUACTGACAXX").isProtein());
    assertFalse(new Sequence("prot", "ACGUACGUACGUXXXXXXXXX").isProtein());
    assertFalse(new Sequence("prot", "ACGUACGUACGUNNNNNNNNN").isProtein());
  }

  @Test(groups = { "Functional" })
  public void testGetAnnotation()
  {
    // initial state returns null not an empty array
    assertNull(seq.getAnnotation());
    AlignmentAnnotation ann = addAnnotation("label1", "desc1", "calcId1",
            1f);
    AlignmentAnnotation[] anns = seq.getAnnotation();
    assertEquals(1, anns.length);
    assertSame(ann, anns[0]);

    // removing all annotations reverts array to null
    seq.removeAlignmentAnnotation(ann);
    assertNull(seq.getAnnotation());
  }

  @Test(groups = { "Functional" })
  public void testGetAnnotation_forLabel()
  {
    AlignmentAnnotation ann1 = addAnnotation("label1", "desc1", "calcId1",
            1f);
    addAnnotation("label2", "desc2", "calcId2", 1f);
    AlignmentAnnotation ann3 = addAnnotation("label1", "desc3", "calcId3",
            1f);
    AlignmentAnnotation[] anns = seq.getAnnotation("label1");
    assertEquals(2, anns.length);
    assertSame(ann1, anns[0]);
    assertSame(ann3, anns[1]);
  }

  private AlignmentAnnotation addAnnotation(String label,
          String description, String calcId, float value)
  {
    final AlignmentAnnotation annotation = new AlignmentAnnotation(label,
            description, value);
    annotation.setCalcId(calcId);
    seq.addAlignmentAnnotation(annotation);
    return annotation;
  }

  @Test(groups = { "Functional" })
  public void testGetAlignmentAnnotations_forCalcIdAndLabel()
  {
    addAnnotation("label1", "desc1", "calcId1", 1f);
    AlignmentAnnotation ann2 = addAnnotation("label2", "desc2", "calcId2",
            1f);
    addAnnotation("label2", "desc3", "calcId3", 1f);
    AlignmentAnnotation ann4 = addAnnotation("label2", "desc3", "calcId2",
            1f);
    addAnnotation("label5", "desc3", null, 1f);
    addAnnotation(null, "desc3", "calcId3", 1f);

    List<AlignmentAnnotation> anns = seq.getAlignmentAnnotations("calcId2",
            "label2");
    assertEquals(2, anns.size());
    assertSame(ann2, anns.get(0));
    assertSame(ann4, anns.get(1));

    assertTrue(seq.getAlignmentAnnotations("calcId2", "label3").isEmpty());
    assertTrue(seq.getAlignmentAnnotations("calcId3", "label5").isEmpty());
    assertTrue(seq.getAlignmentAnnotations("calcId2", null).isEmpty());
    assertTrue(seq.getAlignmentAnnotations(null, "label3").isEmpty());
    assertTrue(seq.getAlignmentAnnotations(null, null).isEmpty());
  }

  @Test(groups = { "Functional" })
  public void testGetAlignmentAnnotations_forCalcIdLabelAndDescription()
  {
    addAnnotation("label1", "desc1", "calcId1", 1f);
    AlignmentAnnotation ann2 = addAnnotation("label2", "desc2", "calcId2",
            1f);
    addAnnotation("label2", "desc3", "calcId3", 1f);
    AlignmentAnnotation ann4 = addAnnotation("label2", "desc3", "calcId2",
            1f);
    addAnnotation("label5", "desc3", null, 1f);
    addAnnotation(null, "desc3", "calcId3", 1f);

    List<AlignmentAnnotation> anns = seq.getAlignmentAnnotations("calcId2",
            "label2", "desc3");
    assertEquals(1, anns.size());
    assertSame(ann4, anns.get(0));
    /**
     * null matching should fail
     */
    assertTrue(seq.getAlignmentAnnotations("calcId3", "label2", null)
            .isEmpty());

    assertTrue(seq.getAlignmentAnnotations("calcId2", "label3", null)
            .isEmpty());
    assertTrue(seq.getAlignmentAnnotations("calcId3", "label5", null)
            .isEmpty());
    assertTrue(
            seq.getAlignmentAnnotations("calcId2", null, null).isEmpty());
    assertTrue(seq.getAlignmentAnnotations(null, "label3", null).isEmpty());
    assertTrue(seq.getAlignmentAnnotations(null, null, null).isEmpty());
  }

  /**
   * Tests for addAlignmentAnnotation. Note this method has the side-effect of
   * setting the sequenceRef on the annotation. Adding the same annotation twice
   * should be ignored.
   */
  @Test(groups = { "Functional" })
  public void testAddAlignmentAnnotation()
  {
    assertNull(seq.getAnnotation());
    final AlignmentAnnotation annotation = new AlignmentAnnotation("a", "b",
            2d);
    assertNull(annotation.sequenceRef);
    seq.addAlignmentAnnotation(annotation);
    assertSame(seq, annotation.sequenceRef);
    AlignmentAnnotation[] anns = seq.getAnnotation();
    assertEquals(1, anns.length);
    assertSame(annotation, anns[0]);

    // re-adding does nothing
    seq.addAlignmentAnnotation(annotation);
    anns = seq.getAnnotation();
    assertEquals(1, anns.length);
    assertSame(annotation, anns[0]);

    // an identical but different annotation can be added
    final AlignmentAnnotation annotation2 = new AlignmentAnnotation("a",
            "b", 2d);
    seq.addAlignmentAnnotation(annotation2);
    anns = seq.getAnnotation();
    assertEquals(2, anns.length);
    assertSame(annotation, anns[0]);
    assertSame(annotation2, anns[1]);
  }

  @Test(groups = { "Functional" })
  public void testGetStartGetEnd()
  {
    SequenceI sq = new Sequence("test", "ABCDEF");
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());

    sq = new Sequence("test", "--AB-C-DEF--");
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());

    sq = new Sequence("test", "----");
    assertEquals(1, sq.getStart());
    assertEquals(0, sq.getEnd()); // ??
  }

  /**
   * Tests for the method that returns an alignment column position (base 1) for
   * a given sequence position (base 1).
   */
  @Test(groups = { "Functional" })
  public void testFindIndex()
  {
    /* 
     * call sequenceChanged() after each test to invalidate any cursor,
     * forcing the 1-arg findIndex to be executed
     */
    SequenceI sq = new Sequence("test", "ABCDEF");
    assertEquals(0, sq.findIndex(0));
    sq.sequenceChanged();
    assertEquals(1, sq.findIndex(1));
    sq.sequenceChanged();
    assertEquals(5, sq.findIndex(5));
    sq.sequenceChanged();
    assertEquals(6, sq.findIndex(6));
    sq.sequenceChanged();
    assertEquals(6, sq.findIndex(9));

    final String aligned = "-A--B-C-D-E-F--";
    assertEquals(15, aligned.length());
    sq = new Sequence("test/8-13", aligned);
    assertEquals(2, sq.findIndex(8));
    sq.sequenceChanged();
    assertEquals(5, sq.findIndex(9));
    sq.sequenceChanged();
    assertEquals(7, sq.findIndex(10));

    // before start returns 0
    sq.sequenceChanged();
    assertEquals(0, sq.findIndex(0));
    sq.sequenceChanged();
    assertEquals(0, sq.findIndex(-1));

    // beyond end returns last residue column
    sq.sequenceChanged();
    assertEquals(13, sq.findIndex(99));

    /*
     * residue before sequence 'end' but beyond end of sequence returns 
     * length of sequence (last column) (rightly or wrongly!)
     */
    sq = new Sequence("test/8-15", "A-B-C-"); // trailing gap case
    assertEquals(6, sq.getLength());
    sq.sequenceChanged();
    assertEquals(sq.getLength(), sq.findIndex(14));
    sq = new Sequence("test/8-99", "-A--B-C-D"); // trailing residue case
    sq.sequenceChanged();
    assertEquals(sq.getLength(), sq.findIndex(65));

    /*
     * residue after sequence 'start' but before first residue returns 
     * zero (before first column) (rightly or wrongly!)
     */
    sq = new Sequence("test/8-15", "-A-B-C-"); // leading gap case
    sq.sequenceChanged();
    assertEquals(0, sq.findIndex(3));
    sq = new Sequence("test/8-15", "A-B-C-"); // leading residue case
    sq.sequenceChanged();
    assertEquals(0, sq.findIndex(2));
  }

  @Test(groups = { "Functional" })
  public void testFindPositions()
  {
    SequenceI sq = new Sequence("test/8-13", "-ABC---DE-F--");

    /*
     * invalid inputs
     */
    assertNull(sq.findPositions(6, 5));
    assertNull(sq.findPositions(0, 5));
    assertNull(sq.findPositions(-1, 5));

    /*
     * all gapped ranges
     */
    assertNull(sq.findPositions(1, 1)); // 1-based columns
    assertNull(sq.findPositions(5, 5));
    assertNull(sq.findPositions(5, 6));
    assertNull(sq.findPositions(5, 7));

    /*
     * all ungapped ranges
     */
    assertEquals(new Range(8, 8), sq.findPositions(2, 2)); // A
    assertEquals(new Range(8, 9), sq.findPositions(2, 3)); // AB
    assertEquals(new Range(8, 10), sq.findPositions(2, 4)); // ABC
    assertEquals(new Range(9, 10), sq.findPositions(3, 4)); // BC

    /*
     * gap to ungapped range
     */
    assertEquals(new Range(8, 10), sq.findPositions(1, 4)); // ABC
    assertEquals(new Range(11, 12), sq.findPositions(6, 9)); // DE

    /*
     * ungapped to gapped range
     */
    assertEquals(new Range(10, 10), sq.findPositions(4, 5)); // C
    assertEquals(new Range(9, 13), sq.findPositions(3, 11)); // BCDEF

    /*
     * ungapped to ungapped enclosing gaps
     */
    assertEquals(new Range(10, 11), sq.findPositions(4, 8)); // CD
    assertEquals(new Range(8, 13), sq.findPositions(2, 11)); // ABCDEF

    /*
     * gapped to gapped enclosing ungapped
     */
    assertEquals(new Range(8, 10), sq.findPositions(1, 5)); // ABC
    assertEquals(new Range(11, 12), sq.findPositions(5, 10)); // DE
    assertEquals(new Range(8, 13), sq.findPositions(1, 13)); // the lot
    assertEquals(new Range(8, 13), sq.findPositions(1, 99));
  }

  /**
   * Tests for the method that returns a dataset sequence position (start..) for
   * an aligned column position (base 0).
   */
  @Test(groups = { "Functional" })
  public void testFindPosition()
  {
    /* 
     * call sequenceChanged() after each test to invalidate any cursor,
     * forcing the 1-arg findPosition to be executed
     */
    SequenceI sq = new Sequence("test/8-13", "ABCDEF");
    assertEquals(8, sq.findPosition(0));
    // Sequence should now hold a cursor at [8, 0]
    assertEquals("test:Pos8:Col1:startCol1:endCol0:tok1",
            PA.getValue(sq, "cursor").toString());
    SequenceCursor cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    int token = (int) PA.getValue(sq, "changeCount");
    assertEquals(new SequenceCursor(sq, 8, 1, token), cursor);

    sq.sequenceChanged();

    /*
     * find F13 at column offset 5, cursor should update to [13, 6]
     * endColumn is found and saved in cursor
     */
    assertEquals(13, sq.findPosition(5));
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(++token, (int) PA.getValue(sq, "changeCount"));
    assertEquals(new SequenceCursor(sq, 13, 6, token), cursor);
    assertEquals("test:Pos13:Col6:startCol1:endCol6:tok2",
            PA.getValue(sq, "cursor").toString());

    // assertEquals(-1, seq.findPosition(6)); // fails

    sq = new Sequence("test/8-11", "AB-C-D--");
    token = (int) PA.getValue(sq, "changeCount"); // 1 for setStart
    assertEquals(8, sq.findPosition(0));
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 8, 1, token), cursor);
    assertEquals("test:Pos8:Col1:startCol1:endCol0:tok1",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(9, sq.findPosition(1));
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 9, 2, ++token), cursor);
    assertEquals("test:Pos9:Col2:startCol1:endCol0:tok2",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    // gap position 'finds' residue to the right (not the left as per javadoc)
    // cursor is set to the last residue position found [B 2]
    assertEquals(10, sq.findPosition(2));
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 9, 2, ++token), cursor);
    assertEquals("test:Pos9:Col2:startCol1:endCol0:tok3",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(10, sq.findPosition(3));
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 10, 4, ++token), cursor);
    assertEquals("test:Pos10:Col4:startCol1:endCol0:tok4",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    // column[4] is the gap after C - returns D11
    // cursor is set to [C 4]
    assertEquals(11, sq.findPosition(4));
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 10, 4, ++token), cursor);
    assertEquals("test:Pos10:Col4:startCol1:endCol0:tok5",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(11, sq.findPosition(5)); // D
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 11, 6, ++token), cursor);
    // lastCol has been found and saved in the cursor
    assertEquals("test:Pos11:Col6:startCol1:endCol6:tok6",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    // returns 1 more than sequence length if off the end ?!?
    assertEquals(12, sq.findPosition(6));

    sq.sequenceChanged();
    assertEquals(12, sq.findPosition(7));

    /*
     * first findPosition should also set firstResCol in cursor
     */
    sq = new Sequence("test/8-13", "--AB-C-DEF--");
    assertEquals(8, sq.findPosition(0));
    assertNull(PA.getValue(sq, "cursor"));
    assertEquals(1, PA.getValue(sq, "changeCount"));

    sq.sequenceChanged();
    assertEquals(8, sq.findPosition(1));
    assertNull(PA.getValue(sq, "cursor"));

    sq.sequenceChanged();
    assertEquals(8, sq.findPosition(2));
    assertEquals("test:Pos8:Col3:startCol3:endCol0:tok3",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(9, sq.findPosition(3));
    assertEquals("test:Pos9:Col4:startCol3:endCol0:tok4",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    // column[4] is a gap, returns next residue pos (C10)
    // cursor is set to last residue found [B]
    assertEquals(10, sq.findPosition(4));
    assertEquals("test:Pos9:Col4:startCol3:endCol0:tok5",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(10, sq.findPosition(5));
    assertEquals("test:Pos10:Col6:startCol3:endCol0:tok6",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    // column[6] is a gap, returns next residue pos (D11)
    // cursor is set to last residue found [C]
    assertEquals(11, sq.findPosition(6));
    assertEquals("test:Pos10:Col6:startCol3:endCol0:tok7",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(11, sq.findPosition(7));
    assertEquals("test:Pos11:Col8:startCol3:endCol0:tok8",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(12, sq.findPosition(8));
    assertEquals("test:Pos12:Col9:startCol3:endCol0:tok9",
            PA.getValue(sq, "cursor").toString());

    /*
     * when the last residue column is found, it is set in the cursor
     */
    sq.sequenceChanged();
    assertEquals(13, sq.findPosition(9));
    assertEquals("test:Pos13:Col10:startCol3:endCol10:tok10",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(14, sq.findPosition(10));
    assertEquals("test:Pos13:Col10:startCol3:endCol10:tok11",
            PA.getValue(sq, "cursor").toString());

    /*
     * findPosition for column beyond sequence length
     * returns 1 more than last residue position
     */
    sq.sequenceChanged();
    assertEquals(14, sq.findPosition(11));
    assertEquals("test:Pos13:Col10:startCol3:endCol10:tok12",
            PA.getValue(sq, "cursor").toString());

    sq.sequenceChanged();
    assertEquals(14, sq.findPosition(99));
    assertEquals("test:Pos13:Col10:startCol3:endCol10:tok13",
            PA.getValue(sq, "cursor").toString());

    /*
     * gapped sequence ending in non-gap
     */
    sq = new Sequence("test/8-13", "--AB-C-DEF");
    assertEquals(13, sq.findPosition(9));
    assertEquals("test:Pos13:Col10:startCol3:endCol10:tok1",
            PA.getValue(sq, "cursor").toString());
    sq.sequenceChanged();
    assertEquals(12, sq.findPosition(8)); // E12
    // sequenceChanged() invalidates cursor.lastResidueColumn
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals("test:Pos12:Col9:startCol3:endCol0:tok2",
            cursor.toString());
    // findPosition with cursor accepts base 1 column values
    assertEquals(13, ((Sequence) sq).findPosition(10, cursor));
    assertEquals(13, sq.findPosition(9)); // F13
    // lastResidueColumn has now been found and saved in cursor
    assertEquals("test:Pos13:Col10:startCol3:endCol10:tok2",
            PA.getValue(sq, "cursor").toString());
  }

  @Test(groups = { "Functional" })
  public void testDeleteChars()
  {
    /*
     * internal delete
     */
    SequenceI sq = new Sequence("test", "ABCDEF");
    assertNull(PA.getValue(sq, "datasetSequence"));
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());
    sq.deleteChars(2, 3);
    assertEquals("ABDEF", sq.getSequenceAsString());
    assertEquals(1, sq.getStart());
    assertEquals(5, sq.getEnd());
    assertNull(PA.getValue(sq, "datasetSequence"));

    /*
     * delete at start
     */
    sq = new Sequence("test", "ABCDEF");
    sq.deleteChars(0, 2);
    assertEquals("CDEF", sq.getSequenceAsString());
    assertEquals(3, sq.getStart());
    assertEquals(6, sq.getEnd());
    assertNull(PA.getValue(sq, "datasetSequence"));

    sq = new Sequence("test", "ABCDE");
    sq.deleteChars(0, 3);
    assertEquals("DE", sq.getSequenceAsString());
    assertEquals(4, sq.getStart());
    assertEquals(5, sq.getEnd());
    assertNull(PA.getValue(sq, "datasetSequence"));

    /*
     * delete at end
     */
    sq = new Sequence("test", "ABCDEF");
    sq.deleteChars(4, 6);
    assertEquals("ABCD", sq.getSequenceAsString());
    assertEquals(1, sq.getStart());
    assertEquals(4, sq.getEnd());
    assertNull(PA.getValue(sq, "datasetSequence"));

    /*
     * delete more positions than there are
     */
    sq = new Sequence("test/8-11", "ABCD");
    sq.deleteChars(0, 99);
    assertEquals("", sq.getSequenceAsString());
    assertEquals(12, sq.getStart()); // = findPosition(99) ?!?
    assertEquals(11, sq.getEnd());

    sq = new Sequence("test/8-11", "----");
    sq.deleteChars(0, 99); // ArrayIndexOutOfBoundsException <= 2.10.2
    assertEquals("", sq.getSequenceAsString());
    assertEquals(8, sq.getStart());
    assertEquals(11, sq.getEnd());
  }

  @Test(groups = { "Functional" })
  public void testDeleteChars_withDbRefsAndFeatures()
  {
    /*
     * internal delete - new dataset sequence created
     * gets a copy of any dbrefs
     */
    SequenceI sq = new Sequence("test", "ABCDEF");
    sq.createDatasetSequence();
    DBRefEntry dbr1 = new DBRefEntry("Uniprot", "0", "a123");
    sq.addDBRef(dbr1);
    Object ds = PA.getValue(sq, "datasetSequence");
    assertNotNull(ds);
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());
    sq.deleteChars(2, 3);
    assertEquals("ABDEF", sq.getSequenceAsString());
    assertEquals(1, sq.getStart());
    assertEquals(5, sq.getEnd());
    Object newDs = PA.getValue(sq, "datasetSequence");
    assertNotNull(newDs);
    assertNotSame(ds, newDs);
    assertNotNull(sq.getDBRefs());
    assertEquals(1, sq.getDBRefs().size());
    assertNotSame(dbr1, sq.getDBRefs().get(0));
    assertEquals(dbr1, sq.getDBRefs().get(0));

    /*
     * internal delete with sequence features
     * (failure case for JAL-2541)
     */
    sq = new Sequence("test", "ABCDEF");
    sq.createDatasetSequence();
    SequenceFeature sf1 = new SequenceFeature("Cath", "desc", 2, 4, 2f,
            "CathGroup");
    sq.addSequenceFeature(sf1);
    ds = PA.getValue(sq, "datasetSequence");
    assertNotNull(ds);
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());
    sq.deleteChars(2, 4);
    assertEquals("ABEF", sq.getSequenceAsString());
    assertEquals(1, sq.getStart());
    assertEquals(4, sq.getEnd());
    newDs = PA.getValue(sq, "datasetSequence");
    assertNotNull(newDs);
    assertNotSame(ds, newDs);
    List<SequenceFeature> sfs = sq.getSequenceFeatures();
    assertEquals(1, sfs.size());
    assertNotSame(sf1, sfs.get(0));
    assertEquals(sf1, sfs.get(0));

    /*
     * delete at start - no new dataset sequence created
     * any sequence features remain as before
     */
    sq = new Sequence("test", "ABCDEF");
    sq.createDatasetSequence();
    ds = PA.getValue(sq, "datasetSequence");
    sf1 = new SequenceFeature("Cath", "desc", 2, 4, 2f, "CathGroup");
    sq.addSequenceFeature(sf1);
    sq.deleteChars(0, 2);
    assertEquals("CDEF", sq.getSequenceAsString());
    assertEquals(3, sq.getStart());
    assertEquals(6, sq.getEnd());
    assertSame(ds, PA.getValue(sq, "datasetSequence"));
    sfs = sq.getSequenceFeatures();
    assertNotNull(sfs);
    assertEquals(1, sfs.size());
    assertSame(sf1, sfs.get(0));

    /*
     * delete at end - no new dataset sequence created
     * any dbrefs remain as before
     */
    sq = new Sequence("test", "ABCDEF");
    sq.createDatasetSequence();
    ds = PA.getValue(sq, "datasetSequence");
    dbr1 = new DBRefEntry("Uniprot", "0", "a123");
    sq.addDBRef(dbr1);
    sq.deleteChars(4, 6);
    assertEquals("ABCD", sq.getSequenceAsString());
    assertEquals(1, sq.getStart());
    assertEquals(4, sq.getEnd());
    assertSame(ds, PA.getValue(sq, "datasetSequence"));
    assertNotNull(sq.getDBRefs());
    assertEquals(1, sq.getDBRefs().size());
    assertSame(dbr1, sq.getDBRefs().get(0));
  }

  @Test(groups = { "Functional" })
  public void testInsertCharAt()
  {
    // non-static methods:
    SequenceI sq = new Sequence("test", "ABCDEF");
    sq.insertCharAt(0, 'z');
    assertEquals("zABCDEF", sq.getSequenceAsString());
    sq.insertCharAt(2, 2, 'x');
    assertEquals("zAxxBCDEF", sq.getSequenceAsString());

    // for static method see StringUtilsTest
  }

  /**
   * Test the method that returns an array of aligned sequence positions where
   * the array index is the data sequence position (both base 0).
   */
  @Test(groups = { "Functional" })
  public void testGapMap()
  {
    SequenceI sq = new Sequence("test", "-A--B-CD-E--F-");
    sq.createDatasetSequence();
    assertEquals("[1, 4, 6, 7, 9, 12]", Arrays.toString(sq.gapMap()));
  }

  /**
   * Test the method that gets sequence features, either from the sequence or
   * its dataset.
   */
  @Test(groups = { "Functional" })
  public void testGetSequenceFeatures()
  {
    SequenceI sq = new Sequence("test", "GATCAT");
    sq.createDatasetSequence();

    assertTrue(sq.getSequenceFeatures().isEmpty());

    /*
     * SequenceFeature on sequence
     */
    SequenceFeature sf = new SequenceFeature("Cath", "desc", 2, 4, 2f,
            null);
    sq.addSequenceFeature(sf);
    List<SequenceFeature> sfs = sq.getSequenceFeatures();
    assertEquals(1, sfs.size());
    assertSame(sf, sfs.get(0));

    /*
     * SequenceFeature on sequence and dataset sequence; returns that on
     * sequence
     * 
     * Note JAL-2046: spurious: we have no use case for this at the moment.
     * This test also buggy - as sf2.equals(sf), no new feature is added
     */
    SequenceFeature sf2 = new SequenceFeature("Cath", "desc", 2, 4, 2f,
            null);
    sq.getDatasetSequence().addSequenceFeature(sf2);
    sfs = sq.getSequenceFeatures();
    assertEquals(1, sfs.size());
    assertSame(sf, sfs.get(0));

    /*
     * SequenceFeature on dataset sequence only
     * Note JAL-2046: spurious: we have no use case for setting a non-dataset sequence's feature array to null at the moment.
     */
    sq.setSequenceFeatures(null);
    assertTrue(sq.getDatasetSequence().getSequenceFeatures().isEmpty());

    /*
     * Corrupt case - no SequenceFeature, dataset's dataset is the original
     * sequence. Test shows no infinite loop results.
     */
    sq.getDatasetSequence().setSequenceFeatures(null);
    /**
     * is there a usecase for this ? setDatasetSequence should throw an error if
     * this actually occurs.
     */
    try
    {
      sq.getDatasetSequence().setDatasetSequence(sq); // loop!
      Assert.fail(
              "Expected Error to be raised when calling setDatasetSequence with self reference");
    } catch (IllegalArgumentException e)
    {
      // TODO Jalview error/exception class for raising implementation errors
      assertTrue(e.getMessage().toLowerCase(Locale.ROOT)
              .contains("implementation error"));
    }
    assertTrue(sq.getSequenceFeatures().isEmpty());
  }

  /**
   * Test the method that returns an array, indexed by sequence position, whose
   * entries are the residue positions at the sequence position (or to the right
   * if a gap)
   */
  @Test(groups = { "Functional" })
  public void testFindPositionMap()
  {
    /*
     * Note: Javadoc for findPosition says it returns the residue position to
     * the left of a gapped position; in fact it returns the position to the
     * right. Also it returns a non-existent residue position for a gap beyond
     * the sequence.
     */
    Sequence sq = new Sequence("TestSeq", "AB.C-D E.");
    int[] map = sq.findPositionMap();
    assertEquals(Arrays.toString(new int[] { 1, 2, 3, 3, 4, 4, 5, 5, 6 }),
            Arrays.toString(map));
  }

  /**
   * Test for getSubsequence
   */
  @Test(groups = { "Functional" })
  public void testGetSubsequence()
  {
    SequenceI sq = new Sequence("TestSeq", "ABCDEFG");
    sq.createDatasetSequence();

    // positions are base 0, end position is exclusive
    SequenceI subseq = sq.getSubSequence(2, 4);

    assertEquals("CD", subseq.getSequenceAsString());
    // start/end are base 1 positions
    assertEquals(3, subseq.getStart());
    assertEquals(4, subseq.getEnd());
    // subsequence shares the full dataset sequence
    assertSame(sq.getDatasetSequence(), subseq.getDatasetSequence());
  }

  /**
   * test createDatasetSequence behaves to doc
   */
  @Test(groups = { "Functional" })
  public void testCreateDatasetSequence()
  {
    SequenceI sq = new Sequence("my", "ASDASD");
    sq.addSequenceFeature(
            new SequenceFeature("type", "desc", 1, 10, 1f, "group"));
    sq.addDBRef(new DBRefEntry("source", "version", "accession"));
    assertNull(sq.getDatasetSequence());
    assertNotNull(PA.getValue(sq, "sequenceFeatureStore"));
    assertNotNull(PA.getValue(sq, "dbrefs"));

    SequenceI rds = sq.createDatasetSequence();
    assertNotNull(rds);
    assertNull(rds.getDatasetSequence());
    assertSame(sq.getDatasetSequence(), rds);

    // sequence features and dbrefs transferred to dataset sequence
    assertNull(PA.getValue(sq, "sequenceFeatureStore"));
    assertNull(PA.getValue(sq, "dbrefs"));
    assertNotNull(PA.getValue(rds, "sequenceFeatureStore"));
    assertNotNull(PA.getValue(rds, "dbrefs"));
  }

  /**
   * Test for deriveSequence applied to a sequence with a dataset
   */
  @Test(groups = { "Functional" })
  public void testDeriveSequence_existingDataset()
  {
    Sequence sq = new Sequence("Seq1", "CD");
    sq.setDatasetSequence(new Sequence("Seq1", "ABCDEF"));
    sq.getDatasetSequence().addSequenceFeature(
            new SequenceFeature("", "", 1, 2, 0f, null));
    sq.setStart(3);
    sq.setEnd(4);

    sq.setDescription("Test sequence description..");
    sq.setVamsasId("TestVamsasId");
    sq.addDBRef(new DBRefEntry("PDB", "version0", "1TST"));

    sq.addDBRef(new DBRefEntry("PDB", "version1", "1PDB"));
    sq.addDBRef(new DBRefEntry("PDB", "version2", "2PDB"));
    sq.addDBRef(new DBRefEntry("PDB", "version3", "3PDB"));
    sq.addDBRef(new DBRefEntry("PDB", "version4", "4PDB"));

    sq.addPDBId(new PDBEntry("1PDB", "A", Type.PDB, "filePath/test1"));
    sq.addPDBId(new PDBEntry("1PDB", "B", Type.PDB, "filePath/test1"));
    sq.addPDBId(new PDBEntry("2PDB", "A", Type.MMCIF, "filePath/test2"));
    sq.addPDBId(new PDBEntry("2PDB", "B", Type.MMCIF, "filePath/test2"));

    // these are the same as ones already added
    DBRefEntry pdb1pdb = new DBRefEntry("PDB", "version1", "1PDB");
    DBRefEntry pdb2pdb = new DBRefEntry("PDB", "version2", "2PDB");

    List<DBRefEntry> primRefs = Arrays
            .asList(new DBRefEntry[]
            { pdb1pdb, pdb2pdb });

    sq.getDatasetSequence().addDBRef(pdb1pdb); // should do nothing
    sq.getDatasetSequence().addDBRef(pdb2pdb); // should do nothing
    sq.getDatasetSequence()
            .addDBRef(new DBRefEntry("PDB", "version3", "3PDB")); // should do
                                                                  // nothing
    sq.getDatasetSequence()
            .addDBRef(new DBRefEntry("PDB", "version4", "4PDB")); // should do
                                                                  // nothing

    PDBEntry pdbe1a = new PDBEntry("1PDB", "A", Type.PDB, "filePath/test1");
    PDBEntry pdbe1b = new PDBEntry("1PDB", "B", Type.PDB, "filePath/test1");
    PDBEntry pdbe2a = new PDBEntry("2PDB", "A", Type.MMCIF,
            "filePath/test2");
    PDBEntry pdbe2b = new PDBEntry("2PDB", "B", Type.MMCIF,
            "filePath/test2");
    sq.getDatasetSequence().addPDBId(pdbe1a);
    sq.getDatasetSequence().addPDBId(pdbe1b);
    sq.getDatasetSequence().addPDBId(pdbe2a);
    sq.getDatasetSequence().addPDBId(pdbe2b);

    /*
     * test we added pdb entries to the dataset sequence
     */
    Assert.assertEquals(sq.getDatasetSequence().getAllPDBEntries(),
            Arrays.asList(new PDBEntry[]
            { pdbe1a, pdbe1b, pdbe2a, pdbe2b }),
            "PDB Entries were not found on dataset sequence.");

    /*
     * we should recover a pdb entry that is on the dataset sequence via PDBEntry
     */
    Assert.assertEquals(pdbe1a, sq.getDatasetSequence().getPDBEntry("1PDB"),
            "PDB Entry '1PDB' not found on dataset sequence via getPDBEntry.");
    ArrayList<Annotation> annotsList = new ArrayList<>();
    System.out.println(">>>>>> " + sq.getSequenceAsString().length());
    annotsList.add(new Annotation("A", "A", 'X', 0.1f));
    annotsList.add(new Annotation("A", "A", 'X', 0.1f));
    Annotation[] annots = annotsList.toArray(new Annotation[0]);
    sq.addAlignmentAnnotation(new AlignmentAnnotation("Test annot",
            "Test annot description", annots));
    sq.getDatasetSequence().addAlignmentAnnotation(new AlignmentAnnotation(
            "Test annot", "Test annot description", annots));
    Assert.assertEquals(sq.getDescription(), "Test sequence description..");
    Assert.assertEquals(sq.getDBRefs().size(), 5); // DBRefs are on dataset
                                                   // sequence
    Assert.assertEquals(sq.getAllPDBEntries().size(), 4);
    Assert.assertNotNull(sq.getAnnotation());
    Assert.assertEquals(sq.getAnnotation()[0].annotations.length, 2);
    Assert.assertEquals(sq.getDatasetSequence().getDBRefs().size(), 5); // same
                                                                        // as
                                                                        // sq.getDBRefs()
    Assert.assertEquals(sq.getDatasetSequence().getAllPDBEntries().size(),
            4);
    Assert.assertNotNull(sq.getDatasetSequence().getAnnotation());

    Sequence derived = (Sequence) sq.deriveSequence();

    Assert.assertEquals(derived.getDescription(),
            "Test sequence description..");
    Assert.assertEquals(derived.getDBRefs().size(), 5); // come from dataset
    Assert.assertEquals(derived.getAllPDBEntries().size(), 4);
    Assert.assertNotNull(derived.getAnnotation());
    Assert.assertEquals(derived.getAnnotation()[0].annotations.length, 2);
    Assert.assertEquals(derived.getDatasetSequence().getDBRefs().size(), 5);
    Assert.assertEquals(
            derived.getDatasetSequence().getAllPDBEntries().size(), 4);
    Assert.assertNotNull(derived.getDatasetSequence().getAnnotation());

    assertEquals("CD", derived.getSequenceAsString());
    assertSame(sq.getDatasetSequence(), derived.getDatasetSequence());

    // derived sequence should access dataset sequence features
    assertNotNull(sq.getSequenceFeatures());
    assertEquals(sq.getSequenceFeatures(), derived.getSequenceFeatures());

    /*
     *  verify we have primary db refs *just* for PDB IDs with associated
     *  PDBEntry objects
     */

    assertEquals(primRefs, sq.getPrimaryDBRefs());
    assertEquals(primRefs, sq.getDatasetSequence().getPrimaryDBRefs());

    assertEquals(sq.getPrimaryDBRefs(), derived.getPrimaryDBRefs());

  }

  /**
   * Test for deriveSequence applied to an ungapped sequence with no dataset
   */
  @Test(groups = { "Functional" })
  public void testDeriveSequence_noDatasetUngapped()
  {
    SequenceI sq = new Sequence("Seq1", "ABCDEF");
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());
    SequenceI derived = sq.deriveSequence();
    assertEquals("ABCDEF", derived.getSequenceAsString());
    assertEquals("ABCDEF",
            derived.getDatasetSequence().getSequenceAsString());
  }

  /**
   * Test for deriveSequence applied to a gapped sequence with no dataset
   */
  @Test(groups = { "Functional" })
  public void testDeriveSequence_noDatasetGapped()
  {
    SequenceI sq = new Sequence("Seq1", "AB-C.D EF");
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());
    assertNull(sq.getDatasetSequence());
    SequenceI derived = sq.deriveSequence();
    assertEquals("AB-C.D EF", derived.getSequenceAsString());
    assertEquals("ABCDEF",
            derived.getDatasetSequence().getSequenceAsString());
  }
  
  /**
   * test that creating a copy of an existing sequence with dataset sequence and
   * associated contact matrix yields annotation associated with the same
   * contact matrix in the copy
   */
  @Test(groups= {"Functional"})
  public void testCopyPasteStyleDerivesequence_withcontactMatrixAnn()
  {
    SequenceI seq1=new Sequence("seq1","ACDACDACD");
    seq1.createDatasetSequence();
    ContactMatrixI cm = new SeqDistanceContactMatrix(seq1.getLength());
    // addContactList needs to return annotation addable to the sequence reference it was called from
    AlignmentAnnotation aann = seq1.addContactList(cm);
    assertTrue(aann.sequenceRef==seq1);
    assertEquals(1,seq1.getAnnotation().length);
    assertNotNull(seq1.getContactListFor(seq1.getAnnotation()[0], 1));
    
    SequenceI seq_derived=seq1.deriveSequence();
    assertEquals(1,seq_derived.getAnnotation().length);
    assertTrue(cm==seq_derived.getContactMatrixFor(seq_derived.getAnnotation()[0]));
    assertNotNull(seq_derived.getContactListFor(seq_derived.getAnnotation()[0], 1));
    
    // copy paste actually uses the copy constructor .. so
    
    SequenceI seq_copied=new Sequence((Sequence)seq_derived);
    assertEquals(1,seq_copied.getAnnotation().length);
    assertTrue(cm==seq_copied.getContactMatrixFor(seq_copied.getAnnotation()[0]));
    assertNotNull(seq_copied.getContactListFor(seq_copied.getAnnotation()[0], 1));
    
    
  }

  @Test(groups = { "Functional" })
  public void testCopyConstructor_noDataset()
  {
    SequenceI seq1 = new Sequence("Seq1", "AB-C.D EF");
    seq1.setDescription("description");
    seq1.addAlignmentAnnotation(
            new AlignmentAnnotation("label", "desc", 1.3d));
    seq1.addSequenceFeature(
            new SequenceFeature("type", "desc", 22, 33, 12.4f, "group"));
    seq1.addPDBId(new PDBEntry("1A70", "B", Type.PDB, "File"));
    seq1.addDBRef(new DBRefEntry("EMBL", "1.2", "AZ12345"));

    SequenceI copy = new Sequence(seq1);

    assertNull(copy.getDatasetSequence());

    verifyCopiedSequence(seq1, copy);

    // copy has a copy of the DBRefEntry
    // this is murky - DBrefs are only copied for dataset sequences
    // where the test for 'dataset sequence' is 'dataset is null'
    // but that doesn't distinguish it from an aligned sequence
    // which has not yet generated a dataset sequence
    // NB getDBRef looks inside dataset sequence if not null
    List<DBRefEntry> dbrefs = copy.getDBRefs();
    assertEquals(1, dbrefs.size());
    assertFalse(dbrefs.get(0) == seq1.getDBRefs().get(0));
    assertTrue(dbrefs.get(0).equals(seq1.getDBRefs().get(0)));
  }

  @Test(groups = { "Functional" })
  public void testCopyConstructor_withDataset()
  {
    SequenceI seq1 = new Sequence("Seq1", "AB-C.D EF");
    seq1.createDatasetSequence();
    seq1.setDescription("description");
    seq1.addAlignmentAnnotation(
            new AlignmentAnnotation("label", "desc", 1.3d));
    // JAL-2046 - what is the contract for using a derived sequence's
    // addSequenceFeature ?
    seq1.addSequenceFeature(
            new SequenceFeature("type", "desc", 22, 33, 12.4f, "group"));
    seq1.addPDBId(new PDBEntry("1A70", "B", Type.PDB, "File"));
    // here we add DBRef to the dataset sequence:
    seq1.getDatasetSequence()
            .addDBRef(new DBRefEntry("EMBL", "1.2", "AZ12345"));

    SequenceI copy = new Sequence(seq1);

    assertNotNull(copy.getDatasetSequence());
    assertSame(copy.getDatasetSequence(), seq1.getDatasetSequence());

    verifyCopiedSequence(seq1, copy);

    // getDBRef looks inside dataset sequence and this is shared,
    // so holds the same dbref objects
    List<DBRefEntry> dbrefs = copy.getDBRefs();
    assertEquals(1, dbrefs.size());
    assertSame(dbrefs.get(0), seq1.getDBRefs().get(0));
  }

  /**
   * Helper to make assertions about a copied sequence
   * 
   * @param seq1
   * @param copy
   */
  protected void verifyCopiedSequence(SequenceI seq1, SequenceI copy)
  {
    // verify basic properties:
    assertEquals(copy.getName(), seq1.getName());
    assertEquals(copy.getDescription(), seq1.getDescription());
    assertEquals(copy.getStart(), seq1.getStart());
    assertEquals(copy.getEnd(), seq1.getEnd());
    assertEquals(copy.getSequenceAsString(), seq1.getSequenceAsString());

    // copy has a copy of the annotation:
    AlignmentAnnotation[] anns = copy.getAnnotation();
    assertEquals(1, anns.length);
    assertFalse(anns[0] == seq1.getAnnotation()[0]);
    assertEquals(anns[0].label, seq1.getAnnotation()[0].label);
    assertEquals(anns[0].description, seq1.getAnnotation()[0].description);
    assertEquals(anns[0].score, seq1.getAnnotation()[0].score);

    // copy has a copy of the sequence feature:
    List<SequenceFeature> sfs = copy.getSequenceFeatures();
    assertEquals(1, sfs.size());
    if (seq1.getDatasetSequence() != null
            && copy.getDatasetSequence() == seq1.getDatasetSequence())
    {
      assertSame(sfs.get(0), seq1.getSequenceFeatures().get(0));
    }
    else
    {
      assertNotSame(sfs.get(0), seq1.getSequenceFeatures().get(0));
    }
    assertEquals(sfs.get(0), seq1.getSequenceFeatures().get(0));

    // copy has a copy of the PDB entry
    Vector<PDBEntry> pdbs = copy.getAllPDBEntries();
    assertEquals(1, pdbs.size());
    assertFalse(pdbs.get(0) == seq1.getAllPDBEntries().get(0));
    assertTrue(pdbs.get(0).equals(seq1.getAllPDBEntries().get(0)));
  }

  @Test(groups = "Functional")
  public void testGetCharAt()
  {
    SequenceI sq = new Sequence("", "abcde");
    assertEquals('a', sq.getCharAt(0));
    assertEquals('e', sq.getCharAt(4));
    assertEquals(' ', sq.getCharAt(5));
    assertEquals(' ', sq.getCharAt(-1));
  }

  @Test(groups = { "Functional" })
  public void testAddSequenceFeatures()
  {
    SequenceI sq = new Sequence("", "abcde");
    // type may not be null
    assertFalse(sq.addSequenceFeature(
            new SequenceFeature(null, "desc", 4, 8, 0f, null)));
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Cath", "desc", 4, 8, 0f, null)));
    // can't add a duplicate feature
    assertFalse(sq.addSequenceFeature(
            new SequenceFeature("Cath", "desc", 4, 8, 0f, null)));
    // can add a different feature
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Scop", "desc", 4, 8, 0f, null))); // different
                                                                   // type
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Cath", "description", 4, 8, 0f, null)));// different
                                                                         // description
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Cath", "desc", 3, 8, 0f, null))); // different
                                                                   // start
                                                                   // position
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Cath", "desc", 4, 9, 0f, null))); // different
                                                                   // end
                                                                   // position
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Cath", "desc", 4, 8, 1f, null))); // different
                                                                   // score
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Cath", "desc", 4, 8, Float.NaN, null))); // score
                                                                          // NaN
    assertTrue(sq.addSequenceFeature(
            new SequenceFeature("Cath", "desc", 4, 8, 0f, "Metal"))); // different
                                                                      // group
    assertEquals(8, sq.getFeatures().getAllFeatures().size());
  }

  /**
   * Tests for adding (or updating) dbrefs
   * 
   * @see DBRefEntry#updateFrom(DBRefEntry)
   */
  @Test(groups = { "Functional" })
  public void testAddDBRef()
  {
    SequenceI sq = new Sequence("", "abcde");
    assertNull(sq.getDBRefs());
    DBRefEntry dbref = new DBRefEntry("Uniprot", "1", "P00340");
    sq.addDBRef(dbref);
    assertEquals(1, sq.getDBRefs().size());
    assertSame(dbref, sq.getDBRefs().get(0));

    /*
     * change of version - new entry
     */
    DBRefEntry dbref2 = new DBRefEntry("Uniprot", "2", "P00340");
    sq.addDBRef(dbref2);
    assertEquals(2, sq.getDBRefs().size());
    assertSame(dbref, sq.getDBRefs().get(0));
    assertSame(dbref2, sq.getDBRefs().get(1));

    /*
     * matches existing entry - not added
     */
    sq.addDBRef(new DBRefEntry("UNIPROT", "1", "p00340"));
    assertEquals(2, sq.getDBRefs().size());

    /*
     * different source = new entry
     */
    DBRefEntry dbref3 = new DBRefEntry("UniRef", "1", "p00340");
    sq.addDBRef(dbref3);
    assertEquals(3, sq.getDBRefs().size());
    assertSame(dbref3, sq.getDBRefs().get(2));

    /*
     * different ref = new entry
     */
    DBRefEntry dbref4 = new DBRefEntry("UniRef", "1", "p00341");
    sq.addDBRef(dbref4);
    assertEquals(4, sq.getDBRefs().size());
    assertSame(dbref4, sq.getDBRefs().get(3));

    /*
     * matching ref with a mapping - map updated
     */
    DBRefEntry dbref5 = new DBRefEntry("UniRef", "1", "p00341");
    Mapping map = new Mapping(
            new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 3, 1));
    dbref5.setMap(map);
    sq.addDBRef(dbref5);
    assertEquals(4, sq.getDBRefs().size());
    assertSame(dbref4, sq.getDBRefs().get(3));
    assertSame(map, dbref4.getMap());

    /*
     * 'real' version replaces "0" version
     */
    dbref2.setVersion("0");
    DBRefEntry dbref6 = new DBRefEntry(dbref2.getSource(), "3",
            dbref2.getAccessionId());
    sq.addDBRef(dbref6);
    assertEquals(4, sq.getDBRefs().size());
    assertSame(dbref2, sq.getDBRefs().get(1));
    assertEquals("3", dbref2.getVersion());

    /*
     * 'real' version replaces "source:0" version
     */
    dbref3.setVersion("Uniprot:0");
    DBRefEntry dbref7 = new DBRefEntry(dbref3.getSource(), "3",
            dbref3.getAccessionId());
    sq.addDBRef(dbref7);
    assertEquals(4, sq.getDBRefs().size());
    assertSame(dbref3, sq.getDBRefs().get(2));
    assertEquals("3", dbref2.getVersion());
  }

  @Test(groups = { "Functional" })
  public void testGetPrimaryDBRefs_peptide()
  {
    SequenceI sq = new Sequence("aseq", "ASDFKYLMQPRST", 10, 22);

    // no dbrefs
    List<DBRefEntry> primaryDBRefs = sq.getPrimaryDBRefs();
    assertTrue(primaryDBRefs.isEmpty());

    // empty dbrefs
    sq.setDBRefs(null);
    primaryDBRefs = sq.getPrimaryDBRefs();
    assertTrue(primaryDBRefs.isEmpty());

    // primary - uniprot
    DBRefEntry upentry1 = new DBRefEntry("UNIPROT", "0", "Q04760");
    sq.addDBRef(upentry1);

    // primary - uniprot with congruent map
    DBRefEntry upentry2 = new DBRefEntry("UNIPROT", "0", "Q04762");
    upentry2.setMap(
            new Mapping(null, new MapList(new int[]
            { 10, 22 }, new int[] { 10, 22 }, 1, 1)));
    sq.addDBRef(upentry2);

    // primary - uniprot with map of enclosing sequence
    DBRefEntry upentry3 = new DBRefEntry("UNIPROT", "0", "Q04763");
    upentry3.setMap(
            new Mapping(null, new MapList(new int[]
            { 8, 24 }, new int[] { 8, 24 }, 1, 1)));
    sq.addDBRef(upentry3);

    // not primary - uniprot with map of sub-sequence (5')
    DBRefEntry upentry4 = new DBRefEntry("UNIPROT", "0", "Q04764");
    upentry4.setMap(
            new Mapping(null, new MapList(new int[]
            { 10, 18 }, new int[] { 10, 18 }, 1, 1)));
    sq.addDBRef(upentry4);

    // not primary - uniprot with map that overlaps 3'
    DBRefEntry upentry5 = new DBRefEntry("UNIPROT", "0", "Q04765");
    upentry5.setMap(
            new Mapping(null, new MapList(new int[]
            { 12, 22 }, new int[] { 12, 22 }, 1, 1)));
    sq.addDBRef(upentry5);

    // not primary - uniprot with map to different coordinates frame
    DBRefEntry upentry6 = new DBRefEntry("UNIPROT", "0", "Q04766");
    upentry6.setMap(
            new Mapping(null, new MapList(new int[]
            { 12, 18 }, new int[] { 112, 118 }, 1, 1)));
    sq.addDBRef(upentry6);

    // not primary - dbref to 'non-core' database
    DBRefEntry upentry7 = new DBRefEntry("Pfam", "0", "PF00903");
    sq.addDBRef(upentry7);

    // primary - type is PDB
    DBRefEntry pdbentry = new DBRefEntry("PDB", "0", "1qip");
    sq.addDBRef(pdbentry);

    // not primary - PDBEntry has no file
    sq.addDBRef(new DBRefEntry("PDB", "0", "1AAA"));

    // not primary - no PDBEntry
    sq.addDBRef(new DBRefEntry("PDB", "0", "1DDD"));

    // add corroborating PDB entry for primary DBref -
    // needs to have a file as well as matching ID
    // note PDB ID is not treated as case sensitive
    sq.addPDBId(new PDBEntry("1QIP", null, Type.PDB,
            new File("/blah").toString()));

    // not valid DBRef - no file..
    sq.addPDBId(new PDBEntry("1AAA", null, null, null));

    primaryDBRefs = sq.getPrimaryDBRefs();
    assertEquals(4, primaryDBRefs.size());
    assertTrue("Couldn't find simple primary reference (UNIPROT)",
            primaryDBRefs.contains(upentry1));
    assertTrue("Couldn't find mapped primary reference (UNIPROT)",
            primaryDBRefs.contains(upentry2));
    assertTrue("Couldn't find mapped context reference (UNIPROT)",
            primaryDBRefs.contains(upentry3));
    assertTrue("Couldn't find expected PDB primary reference",
            primaryDBRefs.contains(pdbentry));
  }

  @Test(groups = { "Functional" })
  public void testGetPrimaryDBRefs_nucleotide()
  {
    SequenceI sq = new Sequence("aseq", "TGATCACTCGACTAGCATCAGCATA", 10,
            34);

    // primary - Ensembl
    DBRefEntry dbr1 = new DBRefEntry("ENSEMBL", "0", "ENSG1234");
    sq.addDBRef(dbr1);

    // not primary - Ensembl 'transcript' mapping of sub-sequence
    DBRefEntry dbr2 = new DBRefEntry("ENSEMBL", "0", "ENST1234");
    dbr2.setMap(
            new Mapping(null, new MapList(new int[]
            { 15, 25 }, new int[] { 1, 11 }, 1, 1)));
    sq.addDBRef(dbr2);

    // primary - EMBL with congruent map
    DBRefEntry dbr3 = new DBRefEntry("EMBL", "0", "J1234");
    dbr3.setMap(
            new Mapping(null, new MapList(new int[]
            { 10, 34 }, new int[] { 10, 34 }, 1, 1)));
    sq.addDBRef(dbr3);

    // not primary - to non-core database
    DBRefEntry dbr4 = new DBRefEntry("CCDS", "0", "J1234");
    sq.addDBRef(dbr4);

    // not primary - to protein
    DBRefEntry dbr5 = new DBRefEntry("UNIPROT", "0", "Q87654");
    sq.addDBRef(dbr5);

    List<DBRefEntry> primaryDBRefs = sq.getPrimaryDBRefs();
    assertEquals(2, primaryDBRefs.size());
    assertTrue(primaryDBRefs.contains(dbr1));
    assertTrue(primaryDBRefs.contains(dbr3));
  }

  /**
   * Test the method that updates the list of PDBEntry from any new DBRefEntry
   * for PDB
   */
  @Test(groups = { "Functional" })
  public void testUpdatePDBIds()
  {
    PDBEntry pdbe1 = new PDBEntry("3A6S", null, null, null);
    seq.addPDBId(pdbe1);
    seq.addDBRef(new DBRefEntry("Ensembl", "8", "ENST1234"));
    seq.addDBRef(new DBRefEntry("PDB", "0", "1A70"));
    seq.addDBRef(new DBRefEntry("PDB", "0", "4BQGa"));
    seq.addDBRef(new DBRefEntry("PDB", "0", "3a6sB"));
    // 7 is not a valid chain code:
    seq.addDBRef(new DBRefEntry("PDB", "0", "2GIS7"));

    seq.updatePDBIds();
    List<PDBEntry> pdbIds = seq.getAllPDBEntries();
    assertEquals(4, pdbIds.size());
    assertSame(pdbe1, pdbIds.get(0));
    // chain code got added to 3A6S:
    assertEquals("B", pdbe1.getChainCode());
    assertEquals("1A70", pdbIds.get(1).getId());
    // 4BQGA is parsed into id + chain
    assertEquals("4BQG", pdbIds.get(2).getId());
    assertEquals("a", pdbIds.get(2).getChainCode());
    assertEquals("2GIS7", pdbIds.get(3).getId());
    assertNull(pdbIds.get(3).getChainCode());
  }

  /**
   * Test the method that either adds a pdbid or updates an existing one
   */
  @Test(groups = { "Functional" })
  public void testAddPDBId()
  {
    PDBEntry pdbe = new PDBEntry("3A6S", null, null, null);
    seq.addPDBId(pdbe);
    assertEquals(1, seq.getAllPDBEntries().size());
    assertSame(pdbe, seq.getPDBEntry("3A6S"));
    assertSame(pdbe, seq.getPDBEntry("3a6s")); // case-insensitive

    // add the same entry
    seq.addPDBId(pdbe);
    assertEquals(1, seq.getAllPDBEntries().size());
    assertSame(pdbe, seq.getPDBEntry("3A6S"));

    // add an identical entry
    seq.addPDBId(new PDBEntry("3A6S", null, null, null));
    assertEquals(1, seq.getAllPDBEntries().size());
    assertSame(pdbe, seq.getPDBEntry("3A6S"));

    // add a different entry
    PDBEntry pdbe2 = new PDBEntry("1A70", null, null, null);
    seq.addPDBId(pdbe2);
    assertEquals(2, seq.getAllPDBEntries().size());
    assertSame(pdbe, seq.getAllPDBEntries().get(0));
    assertSame(pdbe2, seq.getAllPDBEntries().get(1));

    // update pdbe with chain code, file, type
    PDBEntry pdbe3 = new PDBEntry("3a6s", "A", Type.PDB, "filepath");
    seq.addPDBId(pdbe3);
    assertEquals(2, seq.getAllPDBEntries().size());
    assertSame(pdbe, seq.getAllPDBEntries().get(0)); // updated in situ
    assertEquals("3A6S", pdbe.getId()); // unchanged
    assertEquals("A", pdbe.getChainCode()); // updated
    assertEquals(Type.PDB.toString(), pdbe.getType()); // updated
    assertEquals("filepath", pdbe.getFile()); // updated
    assertSame(pdbe2, seq.getAllPDBEntries().get(1));

    // add with a different file path
    PDBEntry pdbe4 = new PDBEntry("3a6s", "A", Type.PDB, "filepath2");
    seq.addPDBId(pdbe4);
    assertEquals(3, seq.getAllPDBEntries().size());
    assertSame(pdbe4, seq.getAllPDBEntries().get(2));

    // add with a different chain code
    PDBEntry pdbe5 = new PDBEntry("3a6s", "B", Type.PDB, "filepath");
    seq.addPDBId(pdbe5);
    assertEquals(4, seq.getAllPDBEntries().size());
    assertSame(pdbe5, seq.getAllPDBEntries().get(3));

    // add with a fake pdbid
    // (models don't have an embedded ID)
    String realId = "RealIDQ";
    PDBEntry pdbe6 = new PDBEntry(realId, null, Type.PDB, "real/localpath");
    PDBEntry pdbe7 = new PDBEntry("RealID/real/localpath", "C", Type.MMCIF,
            "real/localpath");
    pdbe7.setFakedPDBId(true);
    seq.addPDBId(pdbe6);
    assertEquals(5, seq.getAllPDBEntries().size());
    seq.addPDBId(pdbe7);
    assertEquals(5, seq.getAllPDBEntries().size());
    assertFalse(pdbe6.fakedPDBId());
    assertSame(pdbe6, seq.getAllPDBEntries().get(4));
    assertEquals("C", pdbe6.getChainCode());
    assertEquals(realId, pdbe6.getId());
  }

  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testSetDatasetSequence_toSelf()
  {
    seq.setDatasetSequence(seq);
  }

  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testSetDatasetSequence_cascading()
  {
    SequenceI seq2 = new Sequence("Seq2", "xyz");
    seq2.createDatasetSequence();
    seq.setDatasetSequence(seq2);
  }

  @Test(groups = { "Functional" })
  public void testFindFeatures()
  {
    SequenceI sq = new Sequence("test/8-16", "-ABC--DEF--GHI--");
    sq.createDatasetSequence();

    assertTrue(sq.findFeatures(1, 99).isEmpty());

    // add non-positional feature
    SequenceFeature sf0 = new SequenceFeature("Cath", "desc", 0, 0, 2f,
            null);
    sq.addSequenceFeature(sf0);
    // add feature on BCD
    SequenceFeature sfBCD = new SequenceFeature("Cath", "desc", 9, 11, 2f,
            null);
    sq.addSequenceFeature(sfBCD);
    // add feature on DE
    SequenceFeature sfDE = new SequenceFeature("Cath", "desc", 11, 12, 2f,
            null);
    sq.addSequenceFeature(sfDE);
    // add contact feature at [B, H]
    SequenceFeature sfContactBH = new SequenceFeature("Disulphide bond",
            "desc", 9, 15, 2f, null);
    sq.addSequenceFeature(sfContactBH);
    // add contact feature at [F, G]
    SequenceFeature sfContactFG = new SequenceFeature("Disulfide Bond",
            "desc", 13, 14, 2f, null);
    sq.addSequenceFeature(sfContactFG);
    // add single position feature at [I]
    SequenceFeature sfI = new SequenceFeature("Disulfide Bond", "desc", 16,
            16, null);
    sq.addSequenceFeature(sfI);

    // no features in columns 1-2 (-A)
    List<SequenceFeature> found = sq.findFeatures(1, 2);
    assertTrue(found.isEmpty());

    // columns 1-6 (-ABC--) includes BCD and B/H feature but not DE
    found = sq.findFeatures(1, 6);
    assertEquals(2, found.size());
    assertTrue(found.contains(sfBCD));
    assertTrue(found.contains(sfContactBH));

    // columns 5-6 (--) includes (enclosing) BCD but not (contact) B/H feature
    found = sq.findFeatures(5, 6);
    assertEquals(1, found.size());
    assertTrue(found.contains(sfBCD));

    // columns 7-10 (DEF-) includes BCD, DE, F/G but not B/H feature
    found = sq.findFeatures(7, 10);
    assertEquals(3, found.size());
    assertTrue(found.contains(sfBCD));
    assertTrue(found.contains(sfDE));
    assertTrue(found.contains(sfContactFG));

    // columns 10-11 (--) should find nothing
    found = sq.findFeatures(10, 11);
    assertEquals(0, found.size());

    // columns 14-14 (I) should find variant feature
    found = sq.findFeatures(14, 14);
    assertEquals(1, found.size());
    assertTrue(found.contains(sfI));
  }

  @Test(groups = { "Functional" })
  public void testFindIndex_withCursor()
  {
    Sequence sq = new Sequence("test/8-13", "-A--BCD-EF--");
    final int tok = (int) PA.getValue(sq, "changeCount");
    assertEquals(1, tok);

    // find F given A, check cursor is now at the found position
    assertEquals(10, sq.findIndex(13, new SequenceCursor(sq, 8, 2, tok)));
    SequenceCursor cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(13, cursor.residuePosition);
    assertEquals(10, cursor.columnPosition);

    // find A given F
    assertEquals(2, sq.findIndex(8, new SequenceCursor(sq, 13, 10, tok)));
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(8, cursor.residuePosition);
    assertEquals(2, cursor.columnPosition);

    // find C given C (no cursor update is done for this case)
    assertEquals(6, sq.findIndex(10, new SequenceCursor(sq, 10, 6, tok)));
    SequenceCursor cursor2 = (SequenceCursor) PA.getValue(sq, "cursor");
    assertSame(cursor2, cursor);

    /*
     * sequence 'end' beyond end of sequence returns length of sequence 
     *  (for compatibility with pre-cursor code)
     *  - also verify the cursor is left in a valid state
     */
    sq = new Sequence("test/8-99", "-A--B-C-D-E-F--"); // trailing gap case
    assertEquals(7, sq.findIndex(10)); // establishes a cursor
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(10, cursor.residuePosition);
    assertEquals(7, cursor.columnPosition);
    assertEquals(sq.getLength(), sq.findIndex(65));
    cursor2 = (SequenceCursor) PA.getValue(sq, "cursor");
    assertSame(cursor, cursor2); // not updated for this case!

    sq = new Sequence("test/8-99", "-A--B-C-D-E-F"); // trailing residue case
    sq.findIndex(10); // establishes a cursor
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(sq.getLength(), sq.findIndex(65));
    cursor2 = (SequenceCursor) PA.getValue(sq, "cursor");
    assertSame(cursor, cursor2); // not updated for this case!

    /*
     * residue after sequence 'start' but before first residue should return 
     * zero (for compatibility with pre-cursor code)
     */
    sq = new Sequence("test/8-15", "-A-B-C-"); // leading gap case
    sq.findIndex(10); // establishes a cursor
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(0, sq.findIndex(3));
    cursor2 = (SequenceCursor) PA.getValue(sq, "cursor");
    assertSame(cursor, cursor2); // not updated for this case!

    sq = new Sequence("test/8-15", "A-B-C-"); // leading residue case
    sq.findIndex(10); // establishes a cursor
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(0, sq.findIndex(2));
    cursor2 = (SequenceCursor) PA.getValue(sq, "cursor");
    assertSame(cursor, cursor2); // not updated for this case!
  }

  @Test(groups = { "Functional" })
  public void testFindPosition_withCursor()
  {
    Sequence sq = new Sequence("test/8-13", "-A--BCD-EF--");
    final int tok = (int) PA.getValue(sq, "changeCount");
    assertEquals(1, tok);

    // find F pos given A - lastCol gets set in cursor
    assertEquals(13,
            sq.findPosition(10, new SequenceCursor(sq, 8, 2, tok)));
    assertEquals("test:Pos13:Col10:startCol0:endCol10:tok1",
            PA.getValue(sq, "cursor").toString());

    // find A pos given F - first residue column is saved in cursor
    assertEquals(8,
            sq.findPosition(2, new SequenceCursor(sq, 13, 10, tok)));
    assertEquals("test:Pos8:Col2:startCol2:endCol10:tok1",
            PA.getValue(sq, "cursor").toString());

    // find C pos given C (neither startCol nor endCol is set)
    assertEquals(10,
            sq.findPosition(6, new SequenceCursor(sq, 10, 6, tok)));
    assertEquals("test:Pos10:Col6:startCol0:endCol0:tok1",
            PA.getValue(sq, "cursor").toString());

    // now the grey area - what residue position for a gapped column? JAL-2562

    // find 'residue' for column 3 given cursor for D (so working left)
    // returns B9; cursor is updated to [B 5]
    assertEquals(9, sq.findPosition(3, new SequenceCursor(sq, 11, 7, tok)));
    assertEquals("test:Pos9:Col5:startCol0:endCol0:tok1",
            PA.getValue(sq, "cursor").toString());

    // find 'residue' for column 8 given cursor for D (so working right)
    // returns E12; cursor is updated to [D 7]
    assertEquals(12,
            sq.findPosition(8, new SequenceCursor(sq, 11, 7, tok)));
    assertEquals("test:Pos11:Col7:startCol0:endCol0:tok1",
            PA.getValue(sq, "cursor").toString());

    // find 'residue' for column 12 given cursor for B
    // returns 1 more than last residue position; cursor is updated to [F 10]
    // lastCol position is saved in cursor
    assertEquals(14,
            sq.findPosition(12, new SequenceCursor(sq, 9, 5, tok)));
    assertEquals("test:Pos13:Col10:startCol0:endCol10:tok1",
            PA.getValue(sq, "cursor").toString());

    /*
     * findPosition for column beyond length of sequence
     * returns 1 more than the last residue position
     * cursor is set to last real residue position [F 10]
     */
    assertEquals(14,
            sq.findPosition(99, new SequenceCursor(sq, 8, 2, tok)));
    assertEquals("test:Pos13:Col10:startCol0:endCol10:tok1",
            PA.getValue(sq, "cursor").toString());

    /*
     * and the case without a trailing gap
     */
    sq = new Sequence("test/8-13", "-A--BCD-EF");
    // first find C from A
    assertEquals(10, sq.findPosition(6, new SequenceCursor(sq, 8, 2, tok)));
    SequenceCursor cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals("test:Pos10:Col6:startCol0:endCol0:tok1",
            cursor.toString());
    // now 'find' 99 from C
    // cursor is set to [F 10] and saved lastCol
    assertEquals(14, sq.findPosition(99, cursor));
    assertEquals("test:Pos13:Col10:startCol0:endCol10:tok1",
            PA.getValue(sq, "cursor").toString());
  }

  @Test
  public void testIsValidCursor()
  {
    Sequence sq = new Sequence("Seq", "ABC--DE-F", 8, 13);
    assertFalse(sq.isValidCursor(null));

    /*
     * cursor is valid if it has valid sequence ref and changeCount token
     * and positions within the range of the sequence
     */
    int changeCount = (int) PA.getValue(sq, "changeCount");
    SequenceCursor cursor = new SequenceCursor(sq, 13, 1, changeCount);
    assertTrue(sq.isValidCursor(cursor));

    /*
     * column position outside [0 - length] is rejected
     */
    cursor = new SequenceCursor(sq, 13, -1, changeCount);
    assertFalse(sq.isValidCursor(cursor));
    cursor = new SequenceCursor(sq, 13, 10, changeCount);
    assertFalse(sq.isValidCursor(cursor));
    cursor = new SequenceCursor(sq, 7, 8, changeCount);
    assertFalse(sq.isValidCursor(cursor));
    cursor = new SequenceCursor(sq, 14, 2, changeCount);
    assertFalse(sq.isValidCursor(cursor));

    /*
     * wrong sequence is rejected
     */
    cursor = new SequenceCursor(null, 13, 1, changeCount);
    assertFalse(sq.isValidCursor(cursor));
    cursor = new SequenceCursor(new Sequence("Seq", "abc"), 13, 1,
            changeCount);
    assertFalse(sq.isValidCursor(cursor));

    /*
     * wrong token value is rejected
     */
    cursor = new SequenceCursor(sq, 13, 1, changeCount + 1);
    assertFalse(sq.isValidCursor(cursor));
    cursor = new SequenceCursor(sq, 13, 1, changeCount - 1);
    assertFalse(sq.isValidCursor(cursor));
  }

  @Test(groups = { "Functional" })
  public void testFindPosition_withCursorAndEdits()
  {
    Sequence sq = new Sequence("test/8-13", "-A--BCD-EF--");

    // find F pos given A
    assertEquals(13, sq.findPosition(10, new SequenceCursor(sq, 8, 2, 0)));
    int token = (int) PA.getValue(sq, "changeCount"); // 0
    SequenceCursor cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 13, 10, token), cursor);

    /*
     * setSequence should invalidate the cursor cached by the sequence
     */
    sq.setSequence("-A-BCD-EF---"); // one gap removed
    assertEquals(8, sq.getStart()); // sanity check
    assertEquals(11, sq.findPosition(5)); // D11
    // cursor should now be at [D 6]
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 11, 6, ++token), cursor);
    assertEquals(0, cursor.lastColumnPosition); // not yet found
    assertEquals(13, sq.findPosition(8)); // E13
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(9, cursor.lastColumnPosition); // found

    /*
     * deleteChars should invalidate the cached cursor
     */
    sq.deleteChars(2, 5); // delete -BC
    assertEquals("-AD-EF---", sq.getSequenceAsString());
    assertEquals(8, sq.getStart()); // sanity check
    assertEquals(10, sq.findPosition(4)); // E10
    // cursor should now be at [E 5]
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 10, 5, ++token), cursor);

    /*
     * Edit to insert gaps should invalidate the cached cursor
     * insert 2 gaps at column[3] to make -AD---EF---
     */
    SequenceI[] seqs = new SequenceI[] { sq };
    AlignmentI al = new Alignment(seqs);
    new EditCommand().appendEdit(Action.INSERT_GAP, seqs, 3, 2, al, true);
    assertEquals("-AD---EF---", sq.getSequenceAsString());
    assertEquals(10, sq.findPosition(4)); // E10
    // cursor should now be at [D 3]
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 9, 3, ++token), cursor);

    /*
     * insertCharAt should invalidate the cached cursor
     * insert CC at column[4] to make -AD-CC--EF---
     */
    sq.insertCharAt(4, 2, 'C');
    assertEquals("-AD-CC--EF---", sq.getSequenceAsString());
    assertEquals(13, sq.findPosition(9)); // F13
    // cursor should now be at [F 10]
    cursor = (SequenceCursor) PA.getValue(sq, "cursor");
    assertEquals(new SequenceCursor(sq, 13, 10, ++token), cursor);

    /*
     * changing sequence start should invalidate cursor
     */
    sq = new Sequence("test/8-13", "-A--BCD-EF--");
    assertEquals(8, sq.getStart());
    assertEquals(9, sq.findPosition(4)); // B(9)
    sq.setStart(7);
    assertEquals(8, sq.findPosition(4)); // is now B(8)
    sq.setStart(10);
    assertEquals(11, sq.findPosition(4)); // is now B(11)
  }

  @Test(groups = { "Functional" })
  public void testGetSequence()
  {
    String seqstring = "-A--BCD-EF--";
    Sequence sq = new Sequence("test/8-13", seqstring);
    sq.createDatasetSequence();
    assertTrue(Arrays.equals(sq.getSequence(), seqstring.toCharArray()));
    assertTrue(Arrays.equals(sq.getDatasetSequence().getSequence(),
            "ABCDEF".toCharArray()));

    // verify a copy of the sequence array is returned
    char[] theSeq = (char[]) PA.getValue(sq, "sequence");
    assertNotSame(theSeq, sq.getSequence());
    theSeq = (char[]) PA.getValue(sq.getDatasetSequence(), "sequence");
    assertNotSame(theSeq, sq.getDatasetSequence().getSequence());
  }

  @Test(groups = { "Functional" })
  public void testReplace()
  {
    String seqstring = "-A--BCD-EF--";
    SequenceI sq = new Sequence("test/8-13", seqstring);
    // changeCount is incremented for setStart
    assertEquals(1, PA.getValue(sq, "changeCount"));

    assertEquals(0, sq.replace('A', 'A')); // same char
    assertEquals(seqstring, sq.getSequenceAsString());
    assertEquals(1, PA.getValue(sq, "changeCount"));

    assertEquals(0, sq.replace('X', 'Y')); // not there
    assertEquals(seqstring, sq.getSequenceAsString());
    assertEquals(1, PA.getValue(sq, "changeCount"));

    assertEquals(1, sq.replace('A', 'K'));
    assertEquals("-K--BCD-EF--", sq.getSequenceAsString());
    assertEquals(2, PA.getValue(sq, "changeCount"));

    assertEquals(6, sq.replace('-', '.'));
    assertEquals(".K..BCD.EF..", sq.getSequenceAsString());
    assertEquals(3, PA.getValue(sq, "changeCount"));
  }

  @Test(groups = { "Functional" })
  public void testGapBitset()
  {
    SequenceI sq = new Sequence("test/8-13", "-ABC---DE-F--");
    BitSet bs = sq.gapBitset();
    BitSet expected = new BitSet();
    expected.set(0);
    expected.set(4, 7);
    expected.set(9);
    expected.set(11, 13);

    assertTrue(bs.equals(expected));

  }

  public void testFindFeatures_largeEndPos()
  {
    /*
     * imitate a PDB sequence where end is larger than end position
     */
    SequenceI sq = new Sequence("test", "-ABC--DEF--", 1, 20);
    sq.createDatasetSequence();

    assertTrue(sq.findFeatures(1, 9).isEmpty());
    // should be no array bounds exception - JAL-2772
    assertTrue(sq.findFeatures(1, 15).isEmpty());

    // add feature on BCD
    SequenceFeature sfBCD = new SequenceFeature("Cath", "desc", 2, 4, 2f,
            null);
    sq.addSequenceFeature(sfBCD);

    // no features in columns 1-2 (-A)
    List<SequenceFeature> found = sq.findFeatures(1, 2);
    assertTrue(found.isEmpty());

    // columns 1-6 (-ABC--) includes BCD
    found = sq.findFeatures(1, 6);
    assertEquals(1, found.size());
    assertTrue(found.contains(sfBCD));

    // columns 10-11 (--) should find nothing
    found = sq.findFeatures(10, 11);
    assertEquals(0, found.size());
  }

  @Test(groups = { "Functional" })
  public void testSetName()
  {
    SequenceI sq = new Sequence("test", "-ABC---DE-F--");
    assertEquals("test", sq.getName());
    assertEquals(1, sq.getStart());
    assertEquals(6, sq.getEnd());

    sq.setName("testing");
    assertEquals("testing", sq.getName());

    sq.setName("test/8-10");
    assertEquals("test", sq.getName());
    assertEquals(8, sq.getStart());
    assertEquals(13, sq.getEnd()); // note end is recomputed

    sq.setName("testing/7-99");
    assertEquals("testing", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd()); // end may be beyond physical end

    sq.setName("/2-3");
    assertEquals("", sq.getName());
    assertEquals(2, sq.getStart());
    assertEquals(7, sq.getEnd());

    sq.setName("test/"); // invalid
    assertEquals("test/", sq.getName());
    assertEquals(2, sq.getStart());
    assertEquals(7, sq.getEnd());

    sq.setName("test/6-13/7-99");
    assertEquals("test/6-13", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName("test/0-5"); // 0 is invalid - ignored
    assertEquals("test/0-5", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName("test/a-5"); // a is invalid - ignored
    assertEquals("test/a-5", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName("test/6-5"); // start > end is invalid - ignored
    assertEquals("test/6-5", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName("test/5"); // invalid - ignored
    assertEquals("test/5", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName("test/-5"); // invalid - ignored
    assertEquals("test/-5", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName("test/5-"); // invalid - ignored
    assertEquals("test/5-", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName("test/5-6-7"); // invalid - ignored
    assertEquals("test/5-6-7", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());

    sq.setName(null); // invalid, gets converted to space
    assertEquals("", sq.getName());
    assertEquals(7, sq.getStart());
    assertEquals(99, sq.getEnd());
  }

  @Test(groups = { "Functional" })
  public void testCheckValidRange()
  {
    Sequence sq = new Sequence("test/7-12", "-ABC---DE-F--");
    assertEquals(7, sq.getStart());
    assertEquals(12, sq.getEnd());

    /*
     * checkValidRange ensures end is at least the last residue position
     */
    PA.setValue(sq, "end", 2);
    sq.checkValidRange();
    assertEquals(12, sq.getEnd());

    /*
     * end may be beyond the last residue position
     */
    PA.setValue(sq, "end", 22);
    sq.checkValidRange();
    assertEquals(22, sq.getEnd());
  }

  @Test(groups = { "Functional" })
  public void testDeleteChars_withGaps()
  {
    /*
     * delete gaps only
     */
    SequenceI sq = new Sequence("test/8-10", "A-B-C");
    sq.createDatasetSequence();
    assertEquals("ABC", sq.getDatasetSequence().getSequenceAsString());
    sq.deleteChars(1, 2); // delete first gap
    assertEquals("AB-C", sq.getSequenceAsString());
    assertEquals(8, sq.getStart());
    assertEquals(10, sq.getEnd());
    assertEquals("ABC", sq.getDatasetSequence().getSequenceAsString());

    /*
     * delete gaps and residues at start (no new dataset sequence)
     */
    sq = new Sequence("test/8-10", "A-B-C");
    sq.createDatasetSequence();
    sq.deleteChars(0, 3); // delete A-B
    assertEquals("-C", sq.getSequenceAsString());
    assertEquals(10, sq.getStart());
    assertEquals(10, sq.getEnd());
    assertEquals("ABC", sq.getDatasetSequence().getSequenceAsString());

    /*
     * delete gaps and residues at end (no new dataset sequence)
     */
    sq = new Sequence("test/8-10", "A-B-C");
    sq.createDatasetSequence();
    sq.deleteChars(2, 5); // delete B-C
    assertEquals("A-", sq.getSequenceAsString());
    assertEquals(8, sq.getStart());
    assertEquals(8, sq.getEnd());
    assertEquals("ABC", sq.getDatasetSequence().getSequenceAsString());

    /*
     * delete gaps and residues internally (new dataset sequence)
     * first delete from gap to residue
     */
    sq = new Sequence("test/8-10", "A-B-C");
    sq.createDatasetSequence();
    sq.deleteChars(1, 3); // delete -B
    assertEquals("A-C", sq.getSequenceAsString());
    assertEquals(8, sq.getStart());
    assertEquals(9, sq.getEnd());
    assertEquals("AC", sq.getDatasetSequence().getSequenceAsString());
    assertEquals(8, sq.getDatasetSequence().getStart());
    assertEquals(9, sq.getDatasetSequence().getEnd());

    /*
     * internal delete from gap to gap
     */
    sq = new Sequence("test/8-10", "A-B-C");
    sq.createDatasetSequence();
    sq.deleteChars(1, 4); // delete -B-
    assertEquals("AC", sq.getSequenceAsString());
    assertEquals(8, sq.getStart());
    assertEquals(9, sq.getEnd());
    assertEquals("AC", sq.getDatasetSequence().getSequenceAsString());
    assertEquals(8, sq.getDatasetSequence().getStart());
    assertEquals(9, sq.getDatasetSequence().getEnd());

    /*
     * internal delete from residue to residue
     */
    sq = new Sequence("test/8-10", "A-B-C");
    sq.createDatasetSequence();
    sq.deleteChars(2, 3); // delete B
    assertEquals("A--C", sq.getSequenceAsString());
    assertEquals(8, sq.getStart());
    assertEquals(9, sq.getEnd());
    assertEquals("AC", sq.getDatasetSequence().getSequenceAsString());
    assertEquals(8, sq.getDatasetSequence().getStart());
    assertEquals(9, sq.getDatasetSequence().getEnd());
  }

  /**
   * Test the code used to locate the reference sequence ruler origin
   */
  @Test(groups = { "Functional" })
  public void testLocateVisibleStartofSequence()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI al = gen.generate(50, 20, 123, 5, 5);

    HiddenColumns cs = al.getHiddenColumns();
    ColumnSelection colsel = new ColumnSelection();

    SequenceI seq = new Sequence("RefSeq", "-A-SD-ASD--E---");
    assertEquals(2, seq.findIndex(seq.getStart()));

    // no hidden columns
    assertEquals(seq.findIndex(seq.getStart()) - 1,
            seq.firstResidueOutsideIterator(cs.iterator()));

    // hidden column on gap after end of sequence - should not affect bounds
    colsel.hideSelectedColumns(13, al.getHiddenColumns());
    assertEquals(seq.findIndex(seq.getStart()) - 1,
            seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    // hidden column on gap before beginning of sequence - should vis bounds by
    // one
    colsel.hideSelectedColumns(0, al.getHiddenColumns());
    assertEquals(seq.findIndex(seq.getStart()) - 2,
            cs.absoluteToVisibleColumn(
                    seq.firstResidueOutsideIterator(cs.iterator())));

    cs.revealAllHiddenColumns(colsel);
    // hide columns around most of sequence - leave one residue remaining
    cs.hideColumns(1, 3);
    cs.hideColumns(6, 11);

    Iterator<int[]> it = cs.getVisContigsIterator(0, 6, false);

    assertEquals("-D", seq.getSequenceStringFromIterator(it));
    // cs.getVisibleSequenceStrings(0, 5, new SequenceI[]
    // { seq })[0]);

    assertEquals(4, seq.firstResidueOutsideIterator(cs.iterator()));
    cs.revealAllHiddenColumns(colsel);

    // hide whole sequence - should just get location of hidden region
    // containing sequence
    cs.hideColumns(1, 11);
    assertEquals(0, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 15);
    assertEquals(0, seq.firstResidueOutsideIterator(cs.iterator()));

    SequenceI seq2 = new Sequence("RefSeq2", "-------A-SD-ASD--E---");

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(7, 17);
    assertEquals(0, seq2.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(3, 17);
    assertEquals(0, seq2.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(3, 19);
    assertEquals(0, seq2.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 0);
    assertEquals(1, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 1);
    assertEquals(3, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 2);
    assertEquals(3, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(1, 1);
    assertEquals(3, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(1, 2);
    assertEquals(3, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(1, 3);
    assertEquals(4, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 2);
    cs.hideColumns(5, 6);
    assertEquals(3, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 2);
    cs.hideColumns(5, 6);
    cs.hideColumns(9, 10);
    assertEquals(3, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 2);
    cs.hideColumns(7, 11);
    assertEquals(3, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(2, 4);
    cs.hideColumns(7, 11);
    assertEquals(1, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(2, 4);
    cs.hideColumns(7, 12);
    assertEquals(1, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(1, 11);
    assertEquals(0, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 12);
    assertEquals(0, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 4);
    cs.hideColumns(6, 12);
    assertEquals(0, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 1);
    cs.hideColumns(3, 12);
    assertEquals(0, seq.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(3, 14);
    cs.hideColumns(17, 19);
    assertEquals(0, seq2.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(3, 7);
    cs.hideColumns(9, 14);
    cs.hideColumns(17, 19);
    assertEquals(0, seq2.firstResidueOutsideIterator(cs.iterator()));

    cs.revealAllHiddenColumns(colsel);
    cs.hideColumns(0, 1);
    cs.hideColumns(3, 4);
    cs.hideColumns(6, 8);
    cs.hideColumns(10, 12);
    assertEquals(0, seq.firstResidueOutsideIterator(cs.iterator()));

  }

  @Test(groups = { "Functional" })
  public void testTransferAnnotation()
  {
    Sequence origSeq = new Sequence("MYSEQ", "THISISASEQ");
    Sequence toSeq = new Sequence("MYSEQ", "THISISASEQ");
    origSeq.setDescription("DESCRIPTION");
    origSeq.addDBRef(new DBRefEntry("UNIPROT", "0", "Q12345", null, true));

    toSeq.transferAnnotation(origSeq, null);
    assertEquals("DESCRIPTION",toSeq.getDescription());
    toSeq = new Sequence("MYSEQ", "THISISASEQ");
    toSeq.setDescription("unchanged");
    toSeq.transferAnnotation(origSeq, null);
    assertEquals("unchanged",toSeq.getDescription());
    
    assertTrue(toSeq.getDBRefs().size() == 1);

    assertTrue(toSeq.getDBRefs().get(0).isCanonical());

    // check for promotion of non-canonical
    // to canonical (e.g. fetch-db-refs on a jalview project pre 2.11.2)
    toSeq.setDBRefs(null);
    toSeq.addDBRef(new DBRefEntry("UNIPROT", "0", "Q12345", null, false));
    toSeq.transferAnnotation(origSeq, null);
    assertTrue(toSeq.getDBRefs().size() == 1);

    assertTrue("Promotion of non-canonical DBRefEntry failed",
            toSeq.getDBRefs().get(0).isCanonical());

  }
}
