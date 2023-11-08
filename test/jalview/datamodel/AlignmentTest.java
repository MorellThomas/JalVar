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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.analysis.AlignmentGenerator;
import jalview.datamodel.AlignedCodonFrame.SequenceToSequenceMapping;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;
import jalview.util.Comparison;
import jalview.util.MapList;

/**
 * Unit tests for Alignment datamodel.
 * 
 * @author gmcarstairs
 *
 */
public class AlignmentTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // @formatter:off
  private static final String TEST_DATA = 
          "# STOCKHOLM 1.0\n" +
          "#=GS D.melanogaster.1 AC AY119185.1/838-902\n" +
          "#=GS D.melanogaster.2 AC AC092237.1/57223-57161\n" +
          "#=GS D.melanogaster.3 AC AY060611.1/560-627\n" +
          "D.melanogaster.1          G.AGCC.CU...AUGAUCGA\n" +
          "#=GR D.melanogaster.1 SS  ................((((\n" +
          "D.melanogaster.2          C.AUUCAACU.UAUGAGGAU\n" +
          "#=GR D.melanogaster.2 SS  ................((((\n" +
          "D.melanogaster.3          G.UGGCGCU..UAUGACGCA\n" +
          "#=GR D.melanogaster.3 SS  (.(((...(....(((((((\n" +
          "//";

  private static final String AA_SEQS_1 = 
          ">Seq1Name/5-8\n" +
          "K-QY--L\n" +
          ">Seq2Name/12-15\n" +
          "-R-FP-W-\n";

  private static final String CDNA_SEQS_1 = 
          ">Seq1Name/100-111\n" +
          "AC-GG--CUC-CAA-CT\n" +
          ">Seq2Name/200-211\n" +
          "-CG-TTA--ACG---AAGT\n";

  private static final String CDNA_SEQS_2 = 
          ">Seq1Name/50-61\n" +
          "GCTCGUCGTACT\n" +
          ">Seq2Name/60-71\n" +
          "GGGTCAGGCAGT\n";
  // @formatter:on

  private AlignmentI al;

  /**
   * Helper method to load an alignment and ensure dataset sequences are set up.
   * 
   * @param data
   * @param format
   *          TODO
   * @return
   * @throws IOException
   */
  protected AlignmentI loadAlignment(final String data, FileFormatI format)
          throws IOException
  {
    AlignmentI a = new FormatAdapter().readFile(data, DataSourceType.PASTE,
            format);
    a.setDataset(null);
    return a;
  }

  /**
   * assert wrapper: tests all references in the given alignment are consistent
   * 
   * @param alignment
   */
  public static void assertAlignmentDatasetRefs(AlignmentI alignment)
  {
    verifyAlignmentDatasetRefs(alignment, true, null);
  }

  /**
   * assert wrapper: tests all references in the given alignment are consistent
   * 
   * @param alignment
   * @param message
   *          - prefixed to any assert failed messages
   */
  public static void assertAlignmentDatasetRefs(AlignmentI alignment,
          String message)
  {
    verifyAlignmentDatasetRefs(alignment, true, message);
  }

  /**
   * verify sequence and dataset references are properly contained within
   * dataset
   * 
   * @param alignment
   *          - the alignmentI object to verify (either alignment or dataset)
   * @param raiseAssert
   *          - when set, testng assertions are raised.
   * @param message
   *          - null or a string message to prepend to the assert failed
   *          messages.
   * @return true if alignment references were in order, otherwise false.
   */
  public static boolean verifyAlignmentDatasetRefs(AlignmentI alignment,
          boolean raiseAssert, String message)
  {
    if (message == null)
    {
      message = "";
    }
    if (alignment == null)
    {
      if (raiseAssert)
      {
        Assert.fail(message + "Alignment for verification was null.");
      }
      return false;
    }
    if (alignment.getDataset() != null)
    {
      AlignmentI dataset = alignment.getDataset();
      // check all alignment sequences have their dataset within the dataset
      for (SequenceI seq : alignment.getSequences())
      {
        SequenceI seqds = seq.getDatasetSequence();
        if (seqds.getDatasetSequence() != null)
        {
          if (raiseAssert)
          {
            Assert.fail(message
                    + " Alignment contained a sequence who's dataset sequence has a second dataset reference.");
          }
          return false;
        }
        if (dataset.findIndex(seqds) == -1)
        {
          if (raiseAssert)
          {
            Assert.fail(message
                    + " Alignment contained a sequence who's dataset sequence was not in the dataset.");
          }
          return false;
        }
      }
      return verifyAlignmentDatasetRefs(alignment.getDataset(), raiseAssert,
              message);
    }
    else
    {
      int dsp = -1;
      // verify all dataset sequences
      for (SequenceI seqds : alignment.getSequences())
      {
        dsp++;
        if (seqds.getDatasetSequence() != null)
        {
          if (raiseAssert)
          {
            Assert.fail(message
                    + " Dataset contained a sequence with non-null dataset reference (ie not a dataset sequence!)");
          }
          return false;
        }
        int foundp = alignment.findIndex(seqds);
        if (foundp != dsp)
        {
          if (raiseAssert)
          {
            Assert.fail(message
                    + " Dataset sequence array contains a reference at "
                    + dsp + " to a sequence first seen at " + foundp + " ("
                    + seqds.toString() + ")");
          }
          return false;
        }
        if (seqds.getDBRefs() != null)
        {
          for (DBRefEntry dbr : seqds.getDBRefs())
          {
            if (dbr.getMap() != null)
            {
              SequenceI seqdbrmapto = dbr.getMap().getTo();
              if (seqdbrmapto != null)
              {
                if (seqdbrmapto.getDatasetSequence() != null)
                {
                  if (raiseAssert)
                  {
                    Assert.fail(message
                            + " DBRefEntry for sequence in alignment had map to sequence which was not a dataset sequence");
                  }
                  return false;

                }
                if (alignment.findIndex(dbr.getMap().getTo()) == -1)
                {
                  if (raiseAssert)
                  {
                    Assert.fail(message + " DBRefEntry " + dbr
                            + " for sequence " + seqds
                            + " in alignment has map to sequence not in dataset");
                  }
                  return false;
                }
              }
            }
          }
        }
      }
      // finally, verify codonmappings involve only dataset sequences.
      if (alignment.getCodonFrames() != null)
      {
        for (AlignedCodonFrame alc : alignment.getCodonFrames())
        {
          for (SequenceToSequenceMapping ssm : alc.getMappings())
          {
            if (ssm.getFromSeq().getDatasetSequence() != null)
            {
              if (raiseAssert)
              {
                Assert.fail(message
                        + " CodonFrame-SSM-FromSeq is not a dataset sequence");
              }
              return false;
            }
            if (alignment.findIndex(ssm.getFromSeq()) == -1)
            {

              if (raiseAssert)
              {
                Assert.fail(message
                        + " CodonFrame-SSM-FromSeq is not contained in dataset");
              }
              return false;
            }
            if (ssm.getMapping().getTo().getDatasetSequence() != null)
            {
              if (raiseAssert)
              {
                Assert.fail(message
                        + " CodonFrame-SSM-Mapping-ToSeq is not a dataset sequence");
              }
              return false;
            }
            if (alignment.findIndex(ssm.getMapping().getTo()) == -1)
            {

              if (raiseAssert)
              {
                Assert.fail(message
                        + " CodonFrame-SSM-Mapping-ToSeq is not contained in dataset");
              }
              return false;
            }
          }
        }
      }
    }
    return true; // all relationships verified!
  }

  /**
   * call verifyAlignmentDatasetRefs with and without assertion raising enabled,
   * to check expected pass/fail actually occurs in both conditions
   * 
   * @param al
   * @param expected
   * @param msg
   */
  private void assertVerifyAlignment(AlignmentI al, boolean expected,
          String msg)
  {
    if (expected)
    {
      try
      {

        Assert.assertTrue(verifyAlignmentDatasetRefs(al, true, null),
                "Valid test alignment failed when raiseAsserts enabled:"
                        + msg);
      } catch (AssertionError ae)
      {
        ae.printStackTrace();
        Assert.fail(
                "Valid test alignment raised assertion errors when raiseAsserts enabled: "
                        + msg,
                ae);
      }
      // also check validation passes with asserts disabled
      Assert.assertTrue(verifyAlignmentDatasetRefs(al, false, null),
              "Valid test alignment tested false when raiseAsserts disabled:"
                      + msg);
    }
    else
    {
      boolean assertRaised = false;
      try
      {
        verifyAlignmentDatasetRefs(al, true, null);
      } catch (AssertionError ae)
      {
        // expected behaviour
        assertRaised = true;
      }
      if (!assertRaised)
      {
        Assert.fail(
                "Invalid test alignment passed when raiseAsserts enabled:"
                        + msg);
      }
      // also check validation passes with asserts disabled
      Assert.assertFalse(verifyAlignmentDatasetRefs(al, false, null),
              "Invalid test alignment tested true when raiseAsserts disabled:"
                      + msg);
    }
  }

  @Test(groups = { "Functional" })
  public void testVerifyAlignmentDatasetRefs()
  {
    SequenceI sq1 = new Sequence("sq1", "ASFDD"),
            sq2 = new Sequence("sq2", "TTTTTT");

    // construct simple valid alignment dataset
    Alignment al = new Alignment(new SequenceI[] { sq1, sq2 });
    // expect this to pass
    assertVerifyAlignment(al, true, "Simple valid alignment didn't verify");

    // check test for sequence->datasetSequence validity
    sq1.setDatasetSequence(sq2);
    assertVerifyAlignment(al, false,
            "didn't detect dataset sequence with a dataset sequence reference.");

    sq1.setDatasetSequence(null);
    assertVerifyAlignment(al, true,
            "didn't reinstate validity after nulling dataset sequence dataset reference");

    // now create dataset and check again
    al.createDatasetAlignment();
    assertNotNull(al.getDataset());

    assertVerifyAlignment(al, true,
            "verify failed after createDatasetAlignment");

    // create a dbref on sq1 with a sequence ref to sq2
    DBRefEntry dbrs1tos2 = new DBRefEntry("UNIPROT", "1", "Q111111");
    dbrs1tos2
            .setMap(new Mapping(sq2.getDatasetSequence(), new int[]
            { 1, 5 }, new int[] { 2, 6 }, 1, 1));
    sq1.getDatasetSequence().addDBRef(dbrs1tos2);
    assertVerifyAlignment(al, true,
            "verify failed after addition of valid DBRefEntry/map");
    // now create a dbref on a new sequence which maps to another sequence
    // outside of the dataset
    SequenceI sqout = new Sequence("sqout", "ututututucagcagcag"),
            sqnew = new Sequence("sqnew", "EEERRR");
    DBRefEntry sqnewsqout = new DBRefEntry("ENAFOO", "1", "R000001");
    sqnewsqout
            .setMap(new Mapping(sqout, new int[]
            { 1, 6 }, new int[] { 1, 18 }, 1, 3));
    al.getDataset().addSequence(sqnew);

    assertVerifyAlignment(al, true,
            "verify failed after addition of new sequence to dataset");
    // now start checking exception conditions
    sqnew.addDBRef(sqnewsqout);
    assertVerifyAlignment(al, false,
            "verify passed when a dbref with map to sequence outside of dataset was added");
    // make the verify pass by adding the outsider back in
    al.getDataset().addSequence(sqout);
    assertVerifyAlignment(al, true,
            "verify should have passed after adding dbref->to sequence in to dataset");
    // and now the same for a codon mapping...
    SequenceI sqanotherout = new Sequence("sqanotherout",
            "aggtutaggcagcagcag");

    AlignedCodonFrame alc = new AlignedCodonFrame();
    alc.addMap(sqanotherout, sqnew,
            new MapList(new int[]
            { 1, 6 }, new int[] { 1, 18 }, 3, 1));

    al.addCodonFrame(alc);
    Assert.assertEquals(al.getDataset().getCodonFrames().size(), 1);

    assertVerifyAlignment(al, false,
            "verify passed when alCodonFrame mapping to sequence outside of dataset was added");
    // make the verify pass by adding the outsider back in
    al.getDataset().addSequence(sqanotherout);
    assertVerifyAlignment(al, true,
            "verify should have passed once all sequences involved in alCodonFrame were added to dataset");
    al.getDataset().addSequence(sqanotherout);
    assertVerifyAlignment(al, false,
            "verify should have failed when a sequence was added twice to the dataset");
    al.getDataset().deleteSequence(sqanotherout);
    assertVerifyAlignment(al, true,
            "verify should have passed after duplicate entry for sequence was removed");
  }

  /**
   * checks that the sequence data for an alignment's dataset is non-redundant.
   * Fails if there are sequences with same id, sequence, start, and.
   */

  public static void assertDatasetIsNormalised(AlignmentI al)
  {
    assertDatasetIsNormalised(al, null);
  }

  /**
   * checks that the sequence data for an alignment's dataset is non-redundant.
   * Fails if there are sequences with same id, sequence, start, and.
   * 
   * @param al
   *          - alignment to verify
   * @param message
   *          - null or message prepended to exception message.
   */
  public static void assertDatasetIsNormalised(AlignmentI al,
          String message)
  {
    if (al.getDataset() != null)
    {
      assertDatasetIsNormalised(al.getDataset(), message);
      return;
    }
    /*
     * look for pairs of sequences with same ID, start, end, and sequence
     */
    List<SequenceI> seqSet = al.getSequences();
    for (int p = 0; p < seqSet.size(); p++)
    {
      SequenceI pSeq = seqSet.get(p);
      for (int q = p + 1; q < seqSet.size(); q++)
      {
        SequenceI qSeq = seqSet.get(q);
        if (pSeq.getStart() != qSeq.getStart())
        {
          continue;
        }
        if (pSeq.getEnd() != qSeq.getEnd())
        {
          continue;
        }
        if (!pSeq.getName().equals(qSeq.getName()))
        {
          continue;
        }
        if (!Arrays.equals(pSeq.getSequence(), qSeq.getSequence()))
        {
          continue;
        }
        Assert.fail((message == null ? "" : message + " :")
                + "Found similar sequences at position " + p + " and " + q
                + "\n" + pSeq.toString());
      }
    }
  }

  @Test(groups = { "Functional", "Asserts" })
  public void testAssertDatasetIsNormalised()
  {
    Sequence sq1 = new Sequence("s1/1-4", "asdf");
    Sequence sq1shift = new Sequence("s1/2-5", "asdf");
    Sequence sq1seqd = new Sequence("s1/1-4", "asdt");
    Sequence sq2 = new Sequence("s2/1-4", "asdf");
    Sequence sq1dup = new Sequence("s1/1-4", "asdf");

    Alignment al = new Alignment(new SequenceI[] { sq1 });
    al.setDataset(null);

    try
    {
      assertDatasetIsNormalised(al);
    } catch (AssertionError ae)
    {
      Assert.fail("Single sequence should be valid normalised dataset.");
    }
    al.addSequence(sq2);
    try
    {
      assertDatasetIsNormalised(al);
    } catch (AssertionError ae)
    {
      Assert.fail(
              "Two different sequences should be valid normalised dataset.");
    }
    /*
     * now change sq2's name in the alignment. should still be valid
     */
    al.findName(sq2.getName()).setName("sq1");
    try
    {
      assertDatasetIsNormalised(al);
    } catch (AssertionError ae)
    {
      Assert.fail(
              "Two different sequences in dataset, but same name in alignment, should be valid normalised dataset.");
    }

    al.addSequence(sq1seqd);
    try
    {
      assertDatasetIsNormalised(al);
    } catch (AssertionError ae)
    {
      Assert.fail(
              "sq1 and sq1 with different sequence should be distinct.");
    }

    al.addSequence(sq1shift);
    try
    {
      assertDatasetIsNormalised(al);
    } catch (AssertionError ae)
    {
      Assert.fail(
              "sq1 and sq1 with different start/end should be distinct.");
    }
    /*
     * finally, the failure case
     */
    al.addSequence(sq1dup);
    boolean ssertRaised = false;
    try
    {
      assertDatasetIsNormalised(al);

    } catch (AssertionError ae)
    {
      ssertRaised = true;
    }
    if (!ssertRaised)
    {
      Assert.fail("Expected identical sequence to raise exception.");
    }
  }

  /*
   * Read in Stockholm format test data including secondary structure
   * annotations.
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp() throws IOException
  {
    al = loadAlignment(TEST_DATA, FileFormat.Stockholm);
    int i = 0;
    for (AlignmentAnnotation ann : al.getAlignmentAnnotation())
    {
      ann.setCalcId("CalcIdFor" + al.getSequenceAt(i).getName());
      i++;
    }
  }

  /**
   * Test method that returns annotations that match on calcId.
   */
  @Test(groups = { "Functional" })
  public void testFindAnnotation_byCalcId()
  {
    Iterable<AlignmentAnnotation> anns = al
            .findAnnotation("CalcIdForD.melanogaster.2");
    Iterator<AlignmentAnnotation> iter = anns.iterator();
    assertTrue(iter.hasNext());
    AlignmentAnnotation ann = iter.next();
    assertEquals("D.melanogaster.2", ann.sequenceRef.getName());
    assertFalse(iter.hasNext());

    // invalid id
    anns = al.findAnnotation("CalcIdForD.melanogaster.?");
    assertFalse(iter.hasNext());
    anns = al.findAnnotation(null);
    assertFalse(iter.hasNext());
  }

  /**
   * Test method that returns annotations that match on reference sequence,
   * label, or calcId.
   */
  @Test(groups = { "Functional" })
  public void testFindAnnotations_bySeqLabelandorCalcId()
  {
    // TODO: finish testFindAnnotations_bySeqLabelandorCalcId test
    /* Note - this is an incomplete test - need to check null or
     * non-null [ matches, not matches ] behaviour for each of the three
     * parameters..*/

    // search for a single, unique calcId with wildcards on other params
    Iterable<AlignmentAnnotation> anns = al.findAnnotations(null,
            "CalcIdForD.melanogaster.2", null);
    Iterator<AlignmentAnnotation> iter = anns.iterator();
    assertTrue(iter.hasNext());
    AlignmentAnnotation ann = iter.next();
    assertEquals("D.melanogaster.2", ann.sequenceRef.getName());
    assertFalse(iter.hasNext());

    // save reference to test sequence reference parameter
    SequenceI rseq = ann.sequenceRef;

    // search for annotation associated with a single sequence
    anns = al.findAnnotations(rseq, null, null);
    iter = anns.iterator();
    assertTrue(iter.hasNext());
    ann = iter.next();
    assertEquals("D.melanogaster.2", ann.sequenceRef.getName());
    assertFalse(iter.hasNext());

    // search for annotation with a non-existant calcId
    anns = al.findAnnotations(null, "CalcIdForD.melanogaster.?", null);
    iter = anns.iterator();
    assertFalse(iter.hasNext());

    // search for annotation with a particular label - expect three
    anns = al.findAnnotations(null, null, "Secondary Structure");
    iter = anns.iterator();
    assertTrue(iter.hasNext());
    iter.next();
    assertTrue(iter.hasNext());
    iter.next();
    assertTrue(iter.hasNext());
    iter.next();
    // third found.. so
    assertFalse(iter.hasNext());

    // search for annotation on one sequence with a particular label - expect
    // one
    SequenceI sqfound;
    anns = al.findAnnotations(sqfound = al.getSequenceAt(1), null,
            "Secondary Structure");
    iter = anns.iterator();
    assertTrue(iter.hasNext());
    // expect reference to sequence 1 in the alignment
    assertTrue(sqfound == iter.next().sequenceRef);
    assertFalse(iter.hasNext());

    // null on all parameters == find all annotations
    anns = al.findAnnotations(null, null, null);
    iter = anns.iterator();
    int n = al.getAlignmentAnnotation().length;
    while (iter.hasNext())
    {
      n--;
      iter.next();
    }
    assertTrue("Found " + n + " fewer annotations from search.", n == 0);
  }

  @Test(groups = { "Functional" })
  public void testDeleteAllAnnotations_includingAutocalculated()
  {
    AlignmentAnnotation aa = new AlignmentAnnotation("Consensus",
            "Consensus", 0.5);
    aa.autoCalculated = true;
    al.addAnnotation(aa);
    AlignmentAnnotation[] anns = al.getAlignmentAnnotation();
    assertEquals("Wrong number of annotations before deleting", 4,
            anns.length);
    al.deleteAllAnnotations(true);
    assertEquals("Not all deleted", 0, al.getAlignmentAnnotation().length);
  }

  @Test(groups = { "Functional" })
  public void testDeleteAllAnnotations_excludingAutocalculated()
  {
    AlignmentAnnotation aa = new AlignmentAnnotation("Consensus",
            "Consensus", 0.5);
    aa.autoCalculated = true;
    al.addAnnotation(aa);
    AlignmentAnnotation[] anns = al.getAlignmentAnnotation();
    assertEquals("Wrong number of annotations before deleting", 4,
            anns.length);
    al.deleteAllAnnotations(false);
    assertEquals("Not just one annotation left", 1,
            al.getAlignmentAnnotation().length);
  }

  /**
   * Tests for realigning as per a supplied alignment: Dna as Dna.
   * 
   * Note: AlignedCodonFrame's state variables are named for protein-to-cDNA
   * mapping, but can be exploited for a general 'sequence-to-sequence' mapping
   * as here.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testAlignAs_dnaAsDna() throws IOException
  {
    // aligned cDNA:
    AlignmentI al1 = loadAlignment(CDNA_SEQS_1, FileFormat.Fasta);
    // unaligned cDNA:
    AlignmentI al2 = loadAlignment(CDNA_SEQS_2, FileFormat.Fasta);

    /*
     * Make mappings between sequences. The 'aligned cDNA' is playing the role
     * of what would normally be protein here.
     */
    makeMappings(al1, al2);

    ((Alignment) al2).alignAs(al1, false, true);
    assertEquals("GC-TC--GUC-GTACT",
            al2.getSequenceAt(0).getSequenceAsString());
    assertEquals("-GG-GTC--AGG--CAGT",
            al2.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Aligning protein from cDNA.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testAlignAs_proteinAsCdna() throws IOException
  {
    // see also AlignmentUtilsTests
    AlignmentI al1 = loadAlignment(CDNA_SEQS_1, FileFormat.Fasta);
    AlignmentI al2 = loadAlignment(AA_SEQS_1, FileFormat.Fasta);
    makeMappings(al1, al2);

    // Fudge - alignProteinAsCdna expects mappings to be on protein
    al2.getCodonFrames().addAll(al1.getCodonFrames());

    ((Alignment) al2).alignAs(al1, false, true);
    assertEquals("K-Q-Y-L-", al2.getSequenceAt(0).getSequenceAsString());
    assertEquals("-R-F-P-W", al2.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Test aligning cdna as per protein alignment.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" }, enabled = true)
  // TODO review / update this test after redesign of alignAs method
  public void testAlignAs_cdnaAsProtein() throws IOException
  {
    /*
     * Load alignments and add mappings for cDNA to protein
     */
    AlignmentI al1 = loadAlignment(CDNA_SEQS_1, FileFormat.Fasta);
    AlignmentI al2 = loadAlignment(AA_SEQS_1, FileFormat.Fasta);
    makeMappings(al1, al2);

    /*
     * Realign DNA; currently keeping existing gaps in introns only
     */
    ((Alignment) al1).alignAs(al2, false, true);
    assertEquals("ACG---GCUCCA------ACT---",
            al1.getSequenceAt(0).getSequenceAsString());
    assertEquals("---CGT---TAACGA---AGT---",
            al1.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Test aligning cdna as per protein - single sequences
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" }, enabled = true)
  // TODO review / update this test after redesign of alignAs method
  public void testAlignAs_cdnaAsProtein_singleSequence() throws IOException
  {
    /*
     * simple case insert one gap
     */
    verifyAlignAs(">dna\nCAAaaa\n", ">protein\nQ-K\n", "CAA---aaa");

    /*
     * simple case but with sequence offsets
     */
    verifyAlignAs(">dna/5-10\nCAAaaa\n", ">protein/20-21\nQ-K\n",
            "CAA---aaa");

    /*
     * insert gaps as per protein, drop gaps within codons
     */
    verifyAlignAs(">dna/10-18\nCA-Aa-aa--AGA\n", ">aa/6-8\n-Q-K--R\n",
            "---CAA---aaa------AGA");
  }

  /**
   * Helper method that makes mappings and then aligns the first alignment as
   * the second
   * 
   * @param fromSeqs
   * @param toSeqs
   * @param expected
   * @throws IOException
   */
  public void verifyAlignAs(String fromSeqs, String toSeqs, String expected)
          throws IOException
  {
    /*
     * Load alignments and add mappings from nucleotide to protein (or from
     * first to second if both the same type)
     */
    AlignmentI al1 = loadAlignment(fromSeqs, FileFormat.Fasta);
    AlignmentI al2 = loadAlignment(toSeqs, FileFormat.Fasta);
    makeMappings(al1, al2);

    /*
     * Realign DNA; currently keeping existing gaps in introns only
     */
    ((Alignment) al1).alignAs(al2, false, true);
    assertEquals(expected, al1.getSequenceAt(0).getSequenceAsString());
  }

  /**
   * Helper method to make mappings between sequences, and add the mappings to
   * the 'mapped from' alignment
   * 
   * @param alFrom
   * @param alTo
   */
  public void makeMappings(AlignmentI alFrom, AlignmentI alTo)
  {
    int ratio = (alFrom.isNucleotide() == alTo.isNucleotide() ? 1 : 3);

    AlignedCodonFrame acf = new AlignedCodonFrame();

    for (int i = 0; i < alFrom.getHeight(); i++)
    {
      SequenceI seqFrom = alFrom.getSequenceAt(i);
      SequenceI seqTo = alTo.getSequenceAt(i);
      MapList ml = new MapList(
              new int[]
              { seqFrom.getStart(), seqFrom.getEnd() },
              new int[]
              { seqTo.getStart(), seqTo.getEnd() }, ratio, 1);
      acf.addMap(seqFrom, seqTo, ml);
    }

    /*
     * not sure whether mappings 'belong' or protein or nucleotide
     * alignment, so adding to both ;~)
     */
    alFrom.addCodonFrame(acf);
    alTo.addCodonFrame(acf);
  }

  /**
   * Test aligning dna as per protein alignment, for the case where there are
   * introns (i.e. some dna sites have no mapping from a peptide).
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" }, enabled = false)
  // TODO review / update this test after redesign of alignAs method
  public void testAlignAs_dnaAsProtein_withIntrons() throws IOException
  {
    /*
     * Load alignments and add mappings for cDNA to protein
     */
    String dna1 = "A-Aa-gG-GCC-cT-TT";
    String dna2 = "c--CCGgg-TT--T-AA-A";
    AlignmentI al1 = loadAlignment(
            ">Dna1/6-17\n" + dna1 + "\n>Dna2/20-31\n" + dna2 + "\n",
            FileFormat.Fasta);
    AlignmentI al2 = loadAlignment(
            ">Pep1/7-9\n-P--YK\n>Pep2/11-13\nG-T--F\n", FileFormat.Fasta);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    // Seq1 has intron at dna positions 3,4,9 so splice is AAG GCC TTT
    // Seq2 has intron at dna positions 1,5,6 so splice is CCG TTT AAA
    MapList ml1 = new MapList(new int[] { 6, 7, 10, 13, 15, 17 },
            new int[]
            { 7, 9 }, 3, 1);
    acf.addMap(al1.getSequenceAt(0), al2.getSequenceAt(0), ml1);
    MapList ml2 = new MapList(new int[] { 21, 23, 26, 31 },
            new int[]
            { 11, 13 }, 3, 1);
    acf.addMap(al1.getSequenceAt(1), al2.getSequenceAt(1), ml2);
    al2.addCodonFrame(acf);

    /*
     * Align ignoring gaps in dna introns and exons
     */
    ((Alignment) al1).alignAs(al2, false, false);
    assertEquals("---AAagG------GCCcTTT",
            al1.getSequenceAt(0).getSequenceAsString());
    // note 1 gap in protein corresponds to 'gg-' in DNA (3 positions)
    assertEquals("cCCGgg-TTT------AAA",
            al1.getSequenceAt(1).getSequenceAsString());

    /*
     * Reset and realign, preserving gaps in dna introns and exons
     */
    al1.getSequenceAt(0).setSequence(dna1);
    al1.getSequenceAt(1).setSequence(dna2);
    ((Alignment) al1).alignAs(al2, true, true);
    // String dna1 = "A-Aa-gG-GCC-cT-TT";
    // String dna2 = "c--CCGgg-TT--T-AA-A";
    // assumption: we include 'the greater of' protein/dna gap lengths, not both
    assertEquals("---A-Aa-gG------GCC-cT-TT",
            al1.getSequenceAt(0).getSequenceAsString());
    assertEquals("c--CCGgg-TT--T------AA-A",
            al1.getSequenceAt(1).getSequenceAsString());
  }

  @Test(groups = "Functional")
  public void testCopyConstructor() throws IOException
  {
    AlignmentI protein = loadAlignment(AA_SEQS_1, FileFormat.Fasta);
    // create sequence and alignment datasets
    protein.setDataset(null);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });
    protein.getDataset().setCodonFrames(acfList);
    AlignmentI copy = new Alignment(protein);

    /*
     * copy has different aligned sequences but the same dataset sequences
     */
    assertFalse(copy.getSequenceAt(0) == protein.getSequenceAt(0));
    assertFalse(copy.getSequenceAt(1) == protein.getSequenceAt(1));
    assertSame(copy.getSequenceAt(0).getDatasetSequence(),
            protein.getSequenceAt(0).getDatasetSequence());
    assertSame(copy.getSequenceAt(1).getDatasetSequence(),
            protein.getSequenceAt(1).getDatasetSequence());

    // TODO should the copy constructor copy the dataset?
    // or make a new one referring to the same dataset sequences??
    assertNull(copy.getDataset());
    // TODO test metadata is copied when AlignmentI is a dataset

    // assertArrayEquals(copy.getDataset().getSequencesArray(), protein
    // .getDataset().getSequencesArray());
  }

  /**
   * Test behaviour of createDataset
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testCreateDatasetAlignment() throws IOException
  {
    AlignmentI protein = new FormatAdapter().readFile(AA_SEQS_1,
            DataSourceType.PASTE, FileFormat.Fasta);
    /*
     * create a dataset sequence on first sequence
     * leave the second without one
     */
    protein.getSequenceAt(0).createDatasetSequence();
    assertNotNull(protein.getSequenceAt(0).getDatasetSequence());
    assertNull(protein.getSequenceAt(1).getDatasetSequence());

    /*
     * add a mapping to the alignment
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    protein.addCodonFrame(acf);
    assertNull(protein.getDataset());
    assertTrue(protein.getCodonFrames().contains(acf));

    /*
     * create the alignment dataset
     * note this creates sequence datasets where missing
     * as a side-effect (in this case, on seq2
     */
    // TODO promote this method to AlignmentI
    ((Alignment) protein).createDatasetAlignment();

    AlignmentI ds = protein.getDataset();

    // side-effect: dataset created on second sequence
    assertNotNull(protein.getSequenceAt(1).getDatasetSequence());
    // dataset alignment has references to dataset sequences
    assertEquals(ds.getSequenceAt(0),
            protein.getSequenceAt(0).getDatasetSequence());
    assertEquals(ds.getSequenceAt(1),
            protein.getSequenceAt(1).getDatasetSequence());

    // codon frames should have been moved to the dataset
    // getCodonFrames() should delegate to the dataset:
    assertTrue(protein.getCodonFrames().contains(acf));
    // prove the codon frames are indeed on the dataset:
    assertTrue(ds.getCodonFrames().contains(acf));
  }

  /**
   * tests the addition of *all* sequences referred to by a sequence being added
   * to the dataset
   */
  @Test(groups = "Functional")
  public void testCreateDatasetAlignmentWithMappedToSeqs()
  {
    // Alignment with two sequences, gapped.
    SequenceI sq1 = new Sequence("sq1", "A--SDF");
    SequenceI sq2 = new Sequence("sq2", "G--TRQ");

    // cross-references to two more sequences.
    DBRefEntry dbr = new DBRefEntry("SQ1", "", "sq3");
    SequenceI sq3 = new Sequence("sq3", "VWANG");
    dbr.setMap(
            new Mapping(sq3, new MapList(new int[]
            { 1, 4 }, new int[] { 2, 5 }, 1, 1)));
    sq1.addDBRef(dbr);

    SequenceI sq4 = new Sequence("sq4", "ERKWI");
    DBRefEntry dbr2 = new DBRefEntry("SQ2", "", "sq4");
    dbr2.setMap(
            new Mapping(sq4, new MapList(new int[]
            { 1, 4 }, new int[] { 2, 5 }, 1, 1)));
    sq2.addDBRef(dbr2);
    // and a 1:1 codonframe mapping between them.
    AlignedCodonFrame alc = new AlignedCodonFrame();
    alc.addMap(sq1, sq2,
            new MapList(new int[]
            { 1, 4 }, new int[] { 1, 4 }, 1, 1));

    AlignmentI protein = new Alignment(new SequenceI[] { sq1, sq2 });

    /*
     * create the alignment dataset
     * note this creates sequence datasets where missing
     * as a side-effect (in this case, on seq2
     */

    // TODO promote this method to AlignmentI
    ((Alignment) protein).createDatasetAlignment();

    AlignmentI ds = protein.getDataset();

    // should be 4 sequences in dataset - two materialised, and two propagated
    // from dbref
    assertEquals(4, ds.getHeight());
    assertTrue(ds.getSequences().contains(sq1.getDatasetSequence()));
    assertTrue(ds.getSequences().contains(sq2.getDatasetSequence()));
    assertTrue(ds.getSequences().contains(sq3));
    assertTrue(ds.getSequences().contains(sq4));
    // Should have one codon frame mapping between sq1 and sq2 via dataset
    // sequences
    assertEquals(ds.getCodonFrame(sq1.getDatasetSequence()),
            ds.getCodonFrame(sq2.getDatasetSequence()));
  }

  @Test(groups = "Functional")
  public void testAddCodonFrame()
  {
    AlignmentI align = new Alignment(new SequenceI[] {});
    AlignedCodonFrame acf = new AlignedCodonFrame();
    align.addCodonFrame(acf);
    assertEquals(1, align.getCodonFrames().size());
    assertTrue(align.getCodonFrames().contains(acf));
    // can't add the same object twice:
    align.addCodonFrame(acf);
    assertEquals(1, align.getCodonFrames().size());

    // create dataset alignment - mappings move to dataset
    ((Alignment) align).createDatasetAlignment();
    assertSame(align.getCodonFrames(), align.getDataset().getCodonFrames());
    assertEquals(1, align.getCodonFrames().size());

    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    align.addCodonFrame(acf2);
    assertTrue(align.getDataset().getCodonFrames().contains(acf));
  }

  @Test(groups = "Functional")
  public void testAddSequencePreserveDatasetIntegrity()
  {
    Sequence seq = new Sequence("testSeq", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    Alignment align = new Alignment(new SequenceI[] { seq });
    align.createDatasetAlignment();
    AlignmentI ds = align.getDataset();
    SequenceI copy = new Sequence(seq);
    copy.insertCharAt(3, 5, '-');
    align.addSequence(copy);
    Assert.assertEquals(align.getDataset().getHeight(), 1,
            "Dataset shouldn't have more than one sequence.");

    Sequence seq2 = new Sequence("newtestSeq",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    align.addSequence(seq2);
    Assert.assertEquals(align.getDataset().getHeight(), 2,
            "Dataset should now have two sequences.");

    assertAlignmentDatasetRefs(align,
            "addSequence broke dataset reference integrity");
  }

  /**
   * Tests that dbrefs with mappings to sequence get updated if the sequence
   * acquires a dataset sequence
   */
  @Test(groups = "Functional")
  public void testCreateDataset_updateDbrefMappings()
  {
    SequenceI pep = new Sequence("pep", "ASD");
    SequenceI dna = new Sequence("dna", "aaaGCCTCGGATggg");
    SequenceI cds = new Sequence("cds", "GCCTCGGAT");

    // add dbref from dna to peptide
    DBRefEntry dbr = new DBRefEntry("UNIPROT", "", "pep");
    dbr.setMap(
            new Mapping(pep, new MapList(new int[]
            { 4, 15 }, new int[] { 1, 4 }, 3, 1)));
    dna.addDBRef(dbr);

    // add dbref from dna to peptide
    DBRefEntry dbr2 = new DBRefEntry("UNIPROT", "", "pep");
    dbr2.setMap(
            new Mapping(pep, new MapList(new int[]
            { 1, 12 }, new int[] { 1, 4 }, 3, 1)));
    cds.addDBRef(dbr2);

    // add dbref from peptide to dna
    DBRefEntry dbr3 = new DBRefEntry("EMBL", "", "dna");
    dbr3.setMap(
            new Mapping(dna, new MapList(new int[]
            { 1, 4 }, new int[] { 4, 15 }, 1, 3)));
    pep.addDBRef(dbr3);

    // add dbref from peptide to cds
    DBRefEntry dbr4 = new DBRefEntry("EMBLCDS", "", "cds");
    dbr4.setMap(
            new Mapping(cds, new MapList(new int[]
            { 1, 4 }, new int[] { 1, 12 }, 1, 3)));
    pep.addDBRef(dbr4);

    AlignmentI protein = new Alignment(new SequenceI[] { pep });

    /*
     * create the alignment dataset
     */
    ((Alignment) protein).createDatasetAlignment();

    AlignmentI ds = protein.getDataset();

    // should be 3 sequences in dataset
    assertEquals(3, ds.getHeight());
    assertTrue(ds.getSequences().contains(pep.getDatasetSequence()));
    assertTrue(ds.getSequences().contains(dna));
    assertTrue(ds.getSequences().contains(cds));

    /*
     * verify peptide.cdsdbref.peptidedbref is now mapped to peptide dataset
     */
    List<DBRefEntry> dbRefs = pep.getDBRefs();
    assertEquals(2, dbRefs.size());
    assertSame(dna, dbRefs.get(0).map.to);
    assertSame(cds, dbRefs.get(1).map.to);
    assertEquals(1, dna.getDBRefs().size());
    assertSame(pep.getDatasetSequence(), dna.getDBRefs().get(0).map.to);
    assertEquals(1, cds.getDBRefs().size());
    assertSame(pep.getDatasetSequence(), cds.getDBRefs().get(0).map.to);
  }

  @Test(groups = { "Functional" })
  public void testFindGroup()
  {
    SequenceI seq1 = new Sequence("seq1", "ABCDEF---GHI");
    SequenceI seq2 = new Sequence("seq2", "---JKLMNO---");
    AlignmentI a = new Alignment(new SequenceI[] { seq1, seq2 });

    assertNull(a.findGroup(null, 0));
    assertNull(a.findGroup(seq1, 1));
    assertNull(a.findGroup(seq1, -1));

    /*
     * add a group consisting of just "DEF"
     */
    SequenceGroup sg1 = new SequenceGroup();
    sg1.addSequence(seq1, false);
    sg1.setStartRes(3);
    sg1.setEndRes(5);
    a.addGroup(sg1);

    assertNull(a.findGroup(seq1, 2)); // position not in group
    assertNull(a.findGroup(seq1, 6)); // position not in group
    assertNull(a.findGroup(seq2, 5)); // sequence not in group
    assertSame(a.findGroup(seq1, 3), sg1); // yes
    assertSame(a.findGroup(seq1, 4), sg1);
    assertSame(a.findGroup(seq1, 5), sg1);

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

    assertNull(a.findGroup(seq1, 2)); // unchanged
    assertSame(a.findGroup(seq1, 3), sg1); // unchanged
    /*
     * if a residue is in more than one group, method returns
     * the first found (in order groups were added)
     */
    assertSame(a.findGroup(seq1, 4), sg1);
    assertSame(a.findGroup(seq1, 5), sg1);

    /*
     * seq2 only belongs to the second group
     */
    assertSame(a.findGroup(seq2, 4), sg2);
    assertSame(a.findGroup(seq2, 5), sg2);
    assertSame(a.findGroup(seq2, 6), sg2);
    assertSame(a.findGroup(seq2, 7), sg2);
    assertNull(a.findGroup(seq2, 3));
    assertNull(a.findGroup(seq2, 8));
  }

  @Test(groups = { "Functional" })
  public void testDeleteSequenceByIndex()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI a = gen.generate(20, 15, 123, 5, 5);

    // delete sequence 10, alignment reduced by 1
    int height = a.getAbsoluteHeight();
    a.deleteSequence(10);
    assertEquals(a.getAbsoluteHeight(), height - 1);

    // try to delete -ve index, nothing happens
    a.deleteSequence(-1);
    assertEquals(a.getAbsoluteHeight(), height - 1);

    // try to delete beyond end of alignment, nothing happens
    a.deleteSequence(14);
    assertEquals(a.getAbsoluteHeight(), height - 1);
  }

  @Test(groups = { "Functional" })
  public void testDeleteSequenceBySeq()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI a = gen.generate(20, 15, 123, 5, 5);

    // delete sequence 10, alignment reduced by 1
    int height = a.getAbsoluteHeight();
    SequenceI seq = a.getSequenceAt(10);
    a.deleteSequence(seq);
    assertEquals(a.getAbsoluteHeight(), height - 1);

    // try to delete non-existent sequence, nothing happens
    seq = new Sequence("cds", "GCCTCGGAT");
    assertEquals(a.getAbsoluteHeight(), height - 1);
  }

  @Test(groups = { "Functional" })
  public void testDeleteHiddenSequence()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI a = gen.generate(20, 15, 123, 5, 5);

    // delete a sequence which is hidden, check it is NOT removed from hidden
    // sequences
    int height = a.getAbsoluteHeight();
    SequenceI seq = a.getSequenceAt(2);
    a.getHiddenSequences().hideSequence(seq);
    assertEquals(a.getHiddenSequences().getSize(), 1);
    a.deleteSequence(2);
    assertEquals(a.getAbsoluteHeight(), height - 1);
    assertEquals(a.getHiddenSequences().getSize(), 1);

    // delete a sequence which is not hidden, check hiddenSequences are not
    // affected
    a.deleteSequence(10);
    assertEquals(a.getAbsoluteHeight(), height - 2);
    assertEquals(a.getHiddenSequences().getSize(), 1);
  }

  @Test(
    groups = "Functional",
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testSetDataset_selfReference()
  {
    SequenceI seq = new Sequence("a", "a");
    AlignmentI alignment = new Alignment(new SequenceI[] { seq });
    alignment.setDataset(alignment);
  }

  @Test(groups = "Functional")
  public void testAppend()
  {
    SequenceI seq = new Sequence("seq1", "FRMLPSRT-A--L-");
    AlignmentI alignment = new Alignment(new SequenceI[] { seq });
    alignment.setGapCharacter('-');
    SequenceI seq2 = new Sequence("seq1", "KP..L.FQII.");
    AlignmentI alignment2 = new Alignment(new SequenceI[] { seq2 });
    alignment2.setGapCharacter('.');

    alignment.append(alignment2);

    assertEquals('-', alignment.getGapCharacter());
    assertSame(seq, alignment.getSequenceAt(0));
    assertEquals("KP--L-FQII-",
            alignment.getSequenceAt(1).getSequenceAsString());

    // todo test coverage for annotations, mappings, groups,
    // hidden sequences, properties
  }

  /**
   * test that calcId == null on findOrCreate doesn't raise an NPE, and yields
   * an annotation with a null calcId
   * 
   */
  @Test(groups = "Functional")
  public void testFindOrCreateForNullCalcId()
  {
    SequenceI seq = new Sequence("seq1", "FRMLPSRT-A--L-");
    AlignmentI alignment = new Alignment(new SequenceI[] { seq });

    AlignmentAnnotation ala = alignment.findOrCreateAnnotation(
            "Temperature Factor", null, false, seq, null);
    assertNotNull(ala);
    assertEquals(seq, ala.sequenceRef);
    assertEquals("", ala.calcId);
  }

  @Test(groups = "Functional")
  public void testPropagateInsertions()
  {
    // create an alignment with no gaps - this will be the profile seq and other
    // JPRED seqs
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI al = gen.generate(25, 10, 1234, 0, 0);

    // get the profileseq
    SequenceI profileseq = al.getSequenceAt(0);
    SequenceI gappedseq = new Sequence(profileseq);
    gappedseq.insertCharAt(5, al.getGapCharacter());
    gappedseq.insertCharAt(6, al.getGapCharacter());
    gappedseq.insertCharAt(7, al.getGapCharacter());
    gappedseq.insertCharAt(8, al.getGapCharacter());

    // force different kinds of padding
    al.getSequenceAt(3).deleteChars(2, 23);
    al.getSequenceAt(4).deleteChars(2, 27);
    al.getSequenceAt(5).deleteChars(10, 27);

    // create an alignment view with the gapped sequence
    SequenceI[] seqs = new SequenceI[1];
    seqs[0] = gappedseq;
    AlignmentI newal = new Alignment(seqs);
    HiddenColumns hidden = new HiddenColumns();
    hidden.hideColumns(15, 17);

    AlignmentView view = new AlignmentView(newal, hidden, null, true, false,
            false);

    // confirm that original contigs are as expected
    Iterator<int[]> visible = hidden.getVisContigsIterator(0, 25, false);
    int[] region = visible.next();
    assertEquals("[0, 14]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[18, 24]", Arrays.toString(region));

    // propagate insertions
    HiddenColumns result = al.propagateInsertions(profileseq, view);

    // confirm that the contigs have changed to account for the gaps
    visible = result.getVisContigsIterator(0, 25, false);
    region = visible.next();
    assertEquals("[0, 10]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[14, 24]", Arrays.toString(region));

    // confirm the alignment has been changed so that the other sequences have
    // gaps inserted where the columns are hidden
    assertFalse(Comparison.isGap(al.getSequenceAt(1).getSequence()[10]));
    assertTrue(Comparison.isGap(al.getSequenceAt(1).getSequence()[11]));
    assertTrue(Comparison.isGap(al.getSequenceAt(1).getSequence()[12]));
    assertTrue(Comparison.isGap(al.getSequenceAt(1).getSequence()[13]));
    assertFalse(Comparison.isGap(al.getSequenceAt(1).getSequence()[14]));

  }

  @Test(groups = "Functional")
  public void testPropagateInsertionsOverlap()
  {
    // test propagateInsertions where gaps and hiddenColumns overlap

    // create an alignment with no gaps - this will be the profile seq and other
    // JPRED seqs
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI al = gen.generate(20, 10, 1234, 0, 0);

    // get the profileseq
    SequenceI profileseq = al.getSequenceAt(0);
    SequenceI gappedseq = new Sequence(profileseq);
    gappedseq.insertCharAt(5, al.getGapCharacter());
    gappedseq.insertCharAt(6, al.getGapCharacter());
    gappedseq.insertCharAt(7, al.getGapCharacter());
    gappedseq.insertCharAt(8, al.getGapCharacter());

    // create an alignment view with the gapped sequence
    SequenceI[] seqs = new SequenceI[1];
    seqs[0] = gappedseq;
    AlignmentI newal = new Alignment(seqs);

    // hide columns so that some overlap with the gaps
    HiddenColumns hidden = new HiddenColumns();
    hidden.hideColumns(7, 10);

    AlignmentView view = new AlignmentView(newal, hidden, null, true, false,
            false);

    // confirm that original contigs are as expected
    Iterator<int[]> visible = hidden.getVisContigsIterator(0, 20, false);
    int[] region = visible.next();
    assertEquals("[0, 6]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[11, 19]", Arrays.toString(region));
    assertFalse(visible.hasNext());

    // propagate insertions
    HiddenColumns result = al.propagateInsertions(profileseq, view);

    // confirm that the contigs have changed to account for the gaps
    visible = result.getVisContigsIterator(0, 20, false);
    region = visible.next();
    assertEquals("[0, 4]", Arrays.toString(region));
    region = visible.next();
    assertEquals("[7, 19]", Arrays.toString(region));
    assertFalse(visible.hasNext());

    // confirm the alignment has been changed so that the other sequences have
    // gaps inserted where the columns are hidden
    assertFalse(Comparison.isGap(al.getSequenceAt(1).getSequence()[4]));
    assertTrue(Comparison.isGap(al.getSequenceAt(1).getSequence()[5]));
    assertTrue(Comparison.isGap(al.getSequenceAt(1).getSequence()[6]));
    assertFalse(Comparison.isGap(al.getSequenceAt(1).getSequence()[7]));
  }

  @Test(groups = { "Functional" })
  public void testPadGaps()
  {
    SequenceI seq1 = new Sequence("seq1", "ABCDEF--");
    SequenceI seq2 = new Sequence("seq2", "-JKLMNO--");
    SequenceI seq3 = new Sequence("seq2", "-PQR");
    AlignmentI a = new Alignment(new SequenceI[] { seq1, seq2, seq3 });
    a.setGapCharacter('.'); // this replaces existing gaps
    assertEquals("ABCDEF..", seq1.getSequenceAsString());
    a.padGaps();
    // trailing gaps are pruned, short sequences padded with gap character
    assertEquals("ABCDEF.", seq1.getSequenceAsString());
    assertEquals(".JKLMNO", seq2.getSequenceAsString());
    assertEquals(".PQR...", seq3.getSequenceAsString());
  }

  /**
   * Test for setHiddenColumns, to check it returns true if the hidden columns
   * have changed, else false
   */
  @Test(groups = { "Functional" })
  public void testSetHiddenColumns()
  {
    AlignmentI al = new Alignment(new SequenceI[] {});
    assertFalse(al.getHiddenColumns().hasHiddenColumns());

    HiddenColumns hc = new HiddenColumns();
    assertFalse(al.setHiddenColumns(hc)); // no change
    assertSame(hc, al.getHiddenColumns());

    hc.hideColumns(2, 4);
    assertTrue(al.getHiddenColumns().hasHiddenColumns());

    /*
     * set a different object but with the same columns hidden
     */
    HiddenColumns hc2 = new HiddenColumns();
    hc2.hideColumns(2, 4);
    assertFalse(al.setHiddenColumns(hc2)); // no change
    assertSame(hc2, al.getHiddenColumns());

    assertTrue(al.setHiddenColumns(null));
    assertNull(al.getHiddenColumns());
    assertTrue(al.setHiddenColumns(hc));
    assertSame(hc, al.getHiddenColumns());

    al.getHiddenColumns().hideColumns(10, 12);
    hc2.hideColumns(10, 12);
    assertFalse(al.setHiddenColumns(hc2)); // no change

    /*
     * hide columns 15-16 then 17-18 in hc
     * hide columns 15-18 in hc2
     * these are not now 'equal' objects even though they
     * represent the same set of columns
     */
    assertSame(hc2, al.getHiddenColumns());
    hc.hideColumns(15, 16);
    hc.hideColumns(17, 18);
    hc2.hideColumns(15, 18);
    assertFalse(hc.equals(hc2));
    assertTrue(al.setHiddenColumns(hc)); // 'changed'
  }

  @Test(groups = { "Functional" })
  public void testGetWidth()
  {
    SequenceI seq1 = new Sequence("seq1", "ABCDEF--");
    SequenceI seq2 = new Sequence("seq2", "-JKLMNO--");
    SequenceI seq3 = new Sequence("seq2", "-PQR");
    AlignmentI a = new Alignment(new SequenceI[] { seq1, seq2, seq3 });

    assertEquals(9, a.getWidth());

    // width includes hidden columns
    a.getHiddenColumns().hideColumns(2, 5);
    assertEquals(9, a.getWidth());
  }

  @Test(groups = { "Functional" })
  public void testGetVisibleWidth()
  {
    SequenceI seq1 = new Sequence("seq1", "ABCDEF--");
    SequenceI seq2 = new Sequence("seq2", "-JKLMNO--");
    SequenceI seq3 = new Sequence("seq2", "-PQR");
    AlignmentI a = new Alignment(new SequenceI[] { seq1, seq2, seq3 });

    assertEquals(9, a.getVisibleWidth());

    // width excludes hidden columns
    a.getHiddenColumns().hideColumns(2, 5);
    assertEquals(5, a.getVisibleWidth());
  }

  @Test(groups = { "Functional" })
  public void testGetContactMap()
  {
    // TODO
    // 1. test adding/removing/manipulating contact maps with/without associated
    // sequence(s) or groups
    // 2. For sequence associated - ensure that inserting a gap in sequence
    // results in the contact map being relocated accordingly
    // 3. RENDERER QUESTION - should contact maps reflect gaps in the alignment
    // ?

  }

  @Test(groups = { "Functional" })
  public void testEquals()
  {
    SequenceI seq1 = new Sequence("seq1", "ABCDEF--");
    SequenceI seq2 = new Sequence("seq2", "-JKLMNO--");
    SequenceI seq3 = new Sequence("seq2", "-PQR");
    AlignmentI a = new Alignment(new SequenceI[] { seq1, seq2, seq3 });
    a.setDataset(null);
    assertEquals(a.getDataset(), a.getDataset());
  }
}
