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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import jalview.analysis.AlignSeq;
import jalview.gui.JvOptionPane;
import jalview.io.AppletFormatAdapter;
import jalview.io.FileFormat;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AlignmentAnnotationTests
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testCopyConstructor()
  {
    SequenceI sq = new Sequence("Foo", "ARAARARARAWEAWEAWRAWEAWE");
    createAnnotation(sq);
    AlignmentAnnotation alc, alo = sq.getAnnotation()[0];
    alc = new AlignmentAnnotation(alo);
    for (String key : alo.getProperties())
    {
      assertEquals("Property mismatch", alo.getProperty(key),
              alc.getProperty(key));
    }
  }

  /**
   * create some dummy annotation derived from the sequence
   * 
   * @param sq
   */
  public static void createAnnotation(SequenceI sq)
  {
    Annotation[] al = new Annotation[sq.getLength()];
    for (int i = 0; i < al.length; i++)
    {
      al[i] = new Annotation(new Annotation("" + sq.getCharAt(i), "",
              (char) 0, sq.findPosition(i)));
    }
    AlignmentAnnotation alan = new AlignmentAnnotation(
            "For " + sq.getName(), "Fake alignment annot", al);
    // create a sequence mapping for the annotation vector in its current state
    alan.createSequenceMapping(sq, sq.getStart(), false);
    alan.setProperty("CreatedBy", "createAnnotation");
    sq.addAlignmentAnnotation(alan);
  }

  /**
   * use this to test annotation derived from method above as it is transferred
   * across different sequences derived from same dataset coordinate frame
   * 
   * @param ala
   */
  public static void testAnnotTransfer(AlignmentAnnotation ala)
  {
    assertEquals(
            "Failed - need annotation created by createAnnotation method",
            ala.description, "Fake alignment annot");
    ala.adjustForAlignment();
    for (int p = 0; p < ala.annotations.length; p++)
    {
      if (ala.annotations[p] != null)
      {
        assertEquals(
                "Mismatch at position " + p
                        + " between annotation position value and sequence"
                        + ala.annotations[p],
                (int) ala.annotations[p].value,
                ala.sequenceRef.findPosition(p));
      }
    }
  }

  /**
   * Tests the liftOver method and also exercises the functions for remapping
   * annotation across different reference sequences. Here, the test is between
   * different dataset frames (annotation transferred by mapping between
   * sequences)
   */
  @Test(groups = { "Functional" })
  public void testLiftOver()
  {
    SequenceI sqFrom = new Sequence("fromLong", "QQQCDEWGH");
    sqFrom.setStart(10);
    sqFrom.setEnd(sqFrom.findPosition(sqFrom.getLength() - 1));
    SequenceI sqTo = new Sequence("toShort", "RCDEW");
    sqTo.setStart(20);
    sqTo.setEnd(sqTo.findPosition(sqTo.getLength() - 1));
    createAnnotation(sqTo);
    AlignmentAnnotation origTo = sqTo.getAnnotation()[0];
    createAnnotation(sqFrom);
    AlignmentAnnotation origFrom = sqFrom.getAnnotation()[0];
    AlignSeq align = AlignSeq.doGlobalNWAlignment(sqFrom, sqTo,
            AlignSeq.PEP);
    SequenceI alSeq1 = new Sequence(sqFrom.getName(), align.getAStr1());
    alSeq1.setStart(sqFrom.getStart() + align.getSeq1Start() - 1);
    alSeq1.setEnd(sqFrom.getStart() + align.getSeq1End() - 1);
    alSeq1.setDatasetSequence(sqFrom);
    SequenceI alSeq2 = new Sequence(sqTo.getName(), align.getAStr2());
    alSeq2.setStart(sqTo.getStart() + align.getSeq2Start() - 1);
    alSeq2.setEnd(sqTo.getStart() + align.getSeq2End() - 1);
    alSeq2.setDatasetSequence(sqTo);
    System.out.println(new AppletFormatAdapter().formatSequences(
            FileFormat.Stockholm, new Alignment(new SequenceI[]
            { sqFrom, alSeq1, sqTo, alSeq2 }), true));

    Mapping mp = align.getMappingFromS1(false);

    AlignmentAnnotation almap1 = new AlignmentAnnotation(
            sqTo.getAnnotation()[0]);
    almap1.liftOver(sqFrom, mp);
    assertEquals(almap1.sequenceRef, sqFrom);
    alSeq1.addAlignmentAnnotation(almap1);
    almap1.setSequenceRef(alSeq1);
    almap1.adjustForAlignment();
    AlignmentAnnotation almap2 = new AlignmentAnnotation(
            sqFrom.getAnnotation()[0]);
    almap2.liftOver(sqTo, mp);
    assertEquals(almap2.sequenceRef, sqTo);

    alSeq2.addAlignmentAnnotation(almap2);
    almap2.setSequenceRef(alSeq2);
    almap2.adjustForAlignment();

    AlignmentI all = new Alignment(new SequenceI[] { alSeq1, alSeq2 });
    all.addAnnotation(almap1);
    all.addAnnotation(almap2);
    System.out.println(new AppletFormatAdapter()
            .formatSequences(FileFormat.Stockholm, all, true));

    for (int p = 0; p < alSeq1.getLength(); p++)
    {
      Annotation orig1, trans1, orig2, trans2;
      trans2 = almap2.annotations[p];
      orig2 = origFrom.annotations[alSeq1.findPosition(p)
              - sqFrom.getStart()];
      orig1 = origTo.annotations[alSeq2.findPosition(p) - sqTo.getStart()];
      trans1 = almap1.annotations[p];
      if (trans1 == trans2)
      {
        System.out.println("Pos " + p + " mismatch");
        continue;
      }
      assertEquals(
              "Mismatch on Original From and transferred annotation on 2",
              (orig2 != null) ? orig2.toString() : null,
              (trans2 != null) ? trans2.toString() : null);
      assertEquals(
              "Mismatch on Original To and transferred annotation on 1",
              (orig1 != null) ? orig1.toString() : null,
              (trans1 != null) ? trans1.toString() : null);
      String alm1 = "" + (almap1.annotations.length > p
              ? almap1.annotations[p].displayCharacter
              : "Out of range");
      String alm2 = "" + (almap2.annotations.length > p
              ? almap2.annotations[p].displayCharacter
              : "Out of range");
      assertEquals("Position " + p + " " + alm1 + " " + alm2, alm1, alm2);
    }
  }

  @Test(groups = { "Functional" })
  public void testAdjustForAlignment()
  {
    SequenceI seq = new Sequence("TestSeq", "ABCDEFG");
    seq.createDatasetSequence();

    /*
     * Annotate positions 3/4/5 (CDE) with values 1/2/3
     */
    Annotation[] anns = new Annotation[] { null, null, new Annotation(1),
        new Annotation(2), new Annotation(3) };
    AlignmentAnnotation ann = new AlignmentAnnotation("SS",
            "secondary structure", anns);
    seq.addAlignmentAnnotation(ann);

    /*
     * Check annotation map before modifying aligned sequence
     */
    assertNull(ann.getAnnotationForPosition(1));
    assertNull(ann.getAnnotationForPosition(2));
    assertNull(ann.getAnnotationForPosition(6));
    assertNull(ann.getAnnotationForPosition(7));
    assertEquals(1, ann.getAnnotationForPosition(3).value, 0.001d);
    assertEquals(2, ann.getAnnotationForPosition(4).value, 0.001d);
    assertEquals(3, ann.getAnnotationForPosition(5).value, 0.001d);

    /*
     * Trim the displayed sequence to BCD and adjust annotations
     */
    seq.setSequence("BCD");
    seq.setStart(2);
    seq.setEnd(4);
    ann.adjustForAlignment();

    /*
     * Should now have annotations for aligned positions 2, 3Q (CD) only
     */
    assertEquals(3, ann.annotations.length);
    assertNull(ann.annotations[0]);
    assertEquals(1, ann.annotations[1].value, 0.001);
    assertEquals(2, ann.annotations[2].value, 0.001);
  }

  /**
   * Test the method that defaults rna symbol to the one matching the preceding
   * unmatched opening bracket (if any)
   */
  @Test(groups = { "Functional" })
  public void testGetDefaultRnaHelixSymbol()
  {
    AlignmentAnnotation ann = new AlignmentAnnotation("SS",
            "secondary structure", null);
    assertEquals("(", ann.getDefaultRnaHelixSymbol(4));

    Annotation[] anns = new Annotation[20];
    ann.annotations = anns;
    assertEquals("(", ann.getDefaultRnaHelixSymbol(4));

    anns[1] = new Annotation("(", "S", '(', 0f);
    assertEquals("(", ann.getDefaultRnaHelixSymbol(0));
    assertEquals("(", ann.getDefaultRnaHelixSymbol(1));
    assertEquals(")", ann.getDefaultRnaHelixSymbol(2));
    assertEquals(")", ann.getDefaultRnaHelixSymbol(3));

    /*
     * .(.[.{.<.}.>.).].
     */
    anns[1] = new Annotation("(", "S", '(', 0f);
    anns[3] = new Annotation("[", "S", '[', 0f);
    anns[5] = new Annotation("{", "S", '{', 0f);
    anns[7] = new Annotation("<", "S", '<', 0f);
    anns[9] = new Annotation("}", "S", '}', 0f);
    anns[11] = new Annotation(">", "S", '>', 0f);
    anns[13] = new Annotation(")", "S", ')', 0f);
    anns[15] = new Annotation("]", "S", ']', 0f);

    String expected = "(())]]}}>>>>]]]](";
    for (int i = 0; i < expected.length(); i++)
    {
      assertEquals("column " + i, String.valueOf(expected.charAt(i)),
              ann.getDefaultRnaHelixSymbol(i));
    }

    /*
     * .(.[.(.).{.}.<.].D.
     */
    anns[1] = new Annotation("(", "S", '(', 0f);
    anns[3] = new Annotation("[", "S", '[', 0f);
    anns[5] = new Annotation("(", "S", '(', 0f);
    anns[7] = new Annotation(")", "S", ')', 0f);
    anns[9] = new Annotation("{", "S", '{', 0f);
    anns[11] = new Annotation("}", "S", '}', 0f);
    anns[13] = new Annotation("<", "S", '>', 0f);
    anns[15] = new Annotation("]", "S", ']', 0f);
    anns[17] = new Annotation("D", "S", 'D', 0f);

    expected = "(())]]))]]}}]]>>>>dd";
    for (int i = 0; i < expected.length(); i++)
    {
      assertEquals("column " + i, String.valueOf(expected.charAt(i)),
              ann.getDefaultRnaHelixSymbol(i));
    }
  }

  public static Annotation newAnnotation(String ann)
  {
    float val = 0f;
    try
    {
      val = Float.parseFloat(ann);
    } catch (NumberFormatException q)
    {
    }
    ;
    return new Annotation(ann, ann, '\0', val);
  }

  @Test(groups = { "Functional" })
  public void testIsQuantitative()
  {
    AlignmentAnnotation ann = null;

    ann = new AlignmentAnnotation("an", "some an", null);
    Assert.assertFalse(ann.isQuantitative(),
            "Empty annotation set should not be quantitative.");

    ann = new AlignmentAnnotation("an", "some an",
            new Annotation[]
            { newAnnotation("4"), newAnnotation("1"), newAnnotation("1"),
                newAnnotation("0.1"), newAnnotation("0.3") });
    Assert.assertTrue(ann.isQuantitative(),
            "All numbers annotation set should be quantitative.");

    ann = new AlignmentAnnotation("an", "some an",
            new Annotation[]
            { newAnnotation("E"), newAnnotation("E"), newAnnotation("E"),
                newAnnotation("E"), newAnnotation("E") });
    Assert.assertFalse(ann.isQuantitative(),
            "All 'E' annotation set should not be quantitative.");

    ann = new AlignmentAnnotation("an", "some an",
            new Annotation[]
            { newAnnotation("E"), newAnnotation("1"), newAnnotation("2"),
                newAnnotation("3"), newAnnotation("E") });
    Assert.assertTrue(ann.isQuantitative(),
            "Mixed 'E' annotation set should be quantitative.");
  }

  @Test(groups = "Functional")
  public void testMakeVisibleAnnotation()
  {
    HiddenColumns h = new HiddenColumns();
    Annotation[] anns = new Annotation[] { null, null, new Annotation(1),
        new Annotation(2), new Annotation(3), null, null, new Annotation(4),
        new Annotation(5), new Annotation(6), new Annotation(7),
        new Annotation(8) };
    AlignmentAnnotation ann = new AlignmentAnnotation("an", "some an",
            anns);

    // null annotations
    AlignmentAnnotation emptyann = new AlignmentAnnotation("an", "some ann",
            null);
    emptyann.makeVisibleAnnotation(h);
    assertNull(emptyann.annotations);

    emptyann.makeVisibleAnnotation(3, 4, h);
    assertNull(emptyann.annotations);

    // without bounds, does everything
    ann.makeVisibleAnnotation(h);
    assertEquals(12, ann.annotations.length);
    assertNull(ann.annotations[0]);
    assertNull(ann.annotations[1]);
    assertEquals(1.0f, ann.annotations[2].value);
    assertEquals(2.0f, ann.annotations[3].value);
    assertEquals(3.0f, ann.annotations[4].value);
    assertNull(ann.annotations[5]);
    assertNull(ann.annotations[6]);
    assertEquals(4.0f, ann.annotations[7].value);
    assertEquals(5.0f, ann.annotations[8].value);
    assertEquals(6.0f, ann.annotations[9].value);
    assertEquals(7.0f, ann.annotations[10].value);
    assertEquals(8.0f, ann.annotations[11].value);

    // without hidden cols, just truncates
    ann.makeVisibleAnnotation(3, 5, h);
    assertEquals(3, ann.annotations.length);
    assertEquals(2.0f, ann.annotations[0].value);
    assertEquals(3.0f, ann.annotations[1].value);
    assertNull(ann.annotations[2]);

    anns = new Annotation[] { null, null, new Annotation(1),
        new Annotation(2), new Annotation(3), null, null, new Annotation(4),
        new Annotation(5), new Annotation(6), new Annotation(7),
        new Annotation(8) };
    ann = new AlignmentAnnotation("an", "some an", anns);
    h.hideColumns(4, 7);
    ann.makeVisibleAnnotation(1, 9, h);
    assertEquals(5, ann.annotations.length);
    assertNull(ann.annotations[0]);
    assertEquals(1.0f, ann.annotations[1].value);
    assertEquals(2.0f, ann.annotations[2].value);
    assertEquals(5.0f, ann.annotations[3].value);
    assertEquals(6.0f, ann.annotations[4].value);

    anns = new Annotation[] { null, null, new Annotation(1),
        new Annotation(2), new Annotation(3), null, null, new Annotation(4),
        new Annotation(5), new Annotation(6), new Annotation(7),
        new Annotation(8) };
    ann = new AlignmentAnnotation("an", "some an", anns);
    h.hideColumns(1, 2);
    ann.makeVisibleAnnotation(1, 9, h);
    assertEquals(3, ann.annotations.length);
    assertEquals(2.0f, ann.annotations[0].value);
    assertEquals(5.0f, ann.annotations[1].value);
    assertEquals(6.0f, ann.annotations[2].value);

    anns = new Annotation[] { null, null, new Annotation(1),
        new Annotation(2), new Annotation(3), null, null, new Annotation(4),
        new Annotation(5), new Annotation(6), new Annotation(7),
        new Annotation(8), new Annotation(9), new Annotation(10),
        new Annotation(11), new Annotation(12), new Annotation(13),
        new Annotation(14), new Annotation(15) };
    ann = new AlignmentAnnotation("an", "some an", anns);
    h = new HiddenColumns();
    h.hideColumns(5, 18);
    h.hideColumns(20, 21);
    ann.makeVisibleAnnotation(1, 21, h);
    assertEquals(5, ann.annotations.length);
    assertEquals(1.0f, ann.annotations[1].value);
    assertEquals(2.0f, ann.annotations[2].value);
    assertEquals(3.0f, ann.annotations[3].value);
    assertNull(ann.annotations[0]);
    assertNull(ann.annotations[4]);
  }
  /**
   * test the contact matrix nogroups property methods
   */
  @Test(groups= {"Functional"})
  public void test_contactMatrixGroups()
  {
    AlignmentAnnotation aa = new AlignmentAnnotation("foo","foo desc",null);
    assertTrue(aa.isShowGroupsForContactMatrix());
    aa.setShowGroupsForContactMatrix(false);
    assertFalse(aa.isShowGroupsForContactMatrix());
    AlignmentAnnotation copy = new AlignmentAnnotation(aa);
    assertFalse(copy.isShowGroupsForContactMatrix());
    aa.setShowGroupsForContactMatrix(true);
    assertTrue(aa.isShowGroupsForContactMatrix());
    // copy should not be updated
    assertFalse(copy.isShowGroupsForContactMatrix());
    
    
  }
}
