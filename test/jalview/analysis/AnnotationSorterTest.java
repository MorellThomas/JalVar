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
package jalview.analysis;

import static org.testng.AssertJUnit.assertEquals;

import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnotationSorterTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private static final int NUM_SEQS = 6;

  private static final int NUM_ANNS = 7;

  private static final String SS = "secondary structure";

  AlignmentAnnotation[] anns = new AlignmentAnnotation[0];

  Alignment al = null;

  /*
   * Set up 6 sequences and 7 annotations.
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    al = buildAlignment(NUM_SEQS);
    anns = buildAnnotations(NUM_ANNS);
  }

  /**
   * Construct an array of numAnns annotations
   * 
   * @param numAnns
   * 
   * @return
   */
  protected AlignmentAnnotation[] buildAnnotations(int numAnns)
  {
    List<AlignmentAnnotation> annlist = new ArrayList<AlignmentAnnotation>();
    for (int i = 0; i < numAnns; i++)
    {
      AlignmentAnnotation ann = new AlignmentAnnotation(SS + i, "", 0);
      annlist.add(ann);
    }
    return annlist.toArray(anns);
  }

  /**
   * Make an alignment with numSeqs sequences in it.
   * 
   * @param numSeqs
   * 
   * @return
   */
  private Alignment buildAlignment(int numSeqs)
  {
    SequenceI[] seqs = new Sequence[numSeqs];
    for (int i = 0; i < numSeqs; i++)
    {
      seqs[i] = new Sequence("Sequence" + i, "axrdkfp");
    }
    return new Alignment(seqs);
  }

  /**
   * Test sorting by annotation type (label) within sequence order, including
   * <ul>
   * <li>annotations with no sequence reference - sort to end keeping mutual
   * ordering</li>
   * <li>annotations with sequence ref = sort in sequence order</li>
   * <li>multiple annotations for same sequence ref - sort by label
   * non-case-specific</li>
   * <li>annotations with reference to sequence not in alignment - treat like no
   * sequence ref</li>
   * </ul>
   */
  @Test(groups = { "Functional" })
  public void testSortBySequenceAndType_autocalcLast()
  {
    // @formatter:off
    anns[0].sequenceRef = al.getSequenceAt(1); anns[0].label = "label0";
    anns[1].sequenceRef = al.getSequenceAt(3); anns[1].label = "structure";
    anns[2].sequenceRef = al.getSequenceAt(3); anns[2].label = "iron";
    anns[3].autoCalculated = true;             anns[3].label = "Quality";
    anns[4].autoCalculated = true;             anns[4].label = "Consensus";
    anns[5].sequenceRef = al.getSequenceAt(0); anns[5].label = "label5";
    anns[6].sequenceRef = al.getSequenceAt(3); anns[6].label = "IRP";
    // @formatter:on

    AnnotationSorter testee = new AnnotationSorter(al, false);
    testee.sort(anns, SequenceAnnotationOrder.SEQUENCE_AND_LABEL);
    assertEquals("label5", anns[0].label); // for sequence 0
    assertEquals("label0", anns[1].label); // for sequence 1
    assertEquals("iron", anns[2].label); // sequence 3 /iron
    assertEquals("IRP", anns[3].label); // sequence 3/IRP
    assertEquals("structure", anns[4].label); // sequence 3/structure
    assertEquals("Quality", anns[5].label); // autocalc annotations
    assertEquals("Consensus", anns[6].label); // retain ordering
  }

  /**
   * Variant with autocalculated annotations sorting to front
   */
  @Test(groups = { "Functional" })
  public void testSortBySequenceAndType_autocalcFirst()
  {
    // @formatter:off
    anns[0].sequenceRef = al.getSequenceAt(1); anns[0].label = "label0";
    anns[1].sequenceRef = al.getSequenceAt(3); anns[1].label = "structure";
    anns[2].sequenceRef = al.getSequenceAt(3); anns[2].label = "iron";
    anns[3].autoCalculated = true;             anns[3].label = "Quality";
    anns[4].autoCalculated = true;             anns[4].label = "Consensus";
    anns[5].sequenceRef = al.getSequenceAt(0); anns[5].label = "label5";
    anns[6].sequenceRef = al.getSequenceAt(3); anns[6].label = "IRP";
    // @formatter:on

    AnnotationSorter testee = new AnnotationSorter(al, true);
    testee.sort(anns, SequenceAnnotationOrder.SEQUENCE_AND_LABEL);
    assertEquals("Quality", anns[0].label); // autocalc annotations
    assertEquals("Consensus", anns[1].label); // retain ordering
    assertEquals("label5", anns[2].label); // for sequence 0
    assertEquals("label0", anns[3].label); // for sequence 1
    assertEquals("iron", anns[4].label); // sequence 3 /iron
    assertEquals("IRP", anns[5].label); // sequence 3/IRP
    assertEquals("structure", anns[6].label); // sequence 3/structure
  }

  /**
   * Test sorting by annotation type (label) within sequence order, including
   * <ul>
   * <li>annotations with no sequence reference - sort to end keeping mutual
   * ordering</li>
   * <li>annotations with sequence ref = sort in sequence order</li>
   * <li>multiple annotations for same sequence ref - sort by label
   * non-case-specific</li>
   * <li>annotations with reference to sequence not in alignment - treat like no
   * sequence ref</li>
   * </ul>
   */
  @Test(groups = { "Functional" })
  public void testSortByTypeAndSequence_autocalcLast()
  {
    // @formatter:off
    anns[0].sequenceRef = al.getSequenceAt(1); anns[0].label = "label0";
    anns[1].sequenceRef = al.getSequenceAt(3); anns[1].label = "structure";
    anns[2].sequenceRef = al.getSequenceAt(3); anns[2].label = "iron";
    anns[3].autoCalculated = true;             anns[3].label = "Quality";
    anns[4].autoCalculated = true;             anns[4].label = "Consensus";
    anns[5].sequenceRef = al.getSequenceAt(0); anns[5].label = "IRON";
    anns[6].sequenceRef = al.getSequenceAt(2); anns[6].label = "Structure";
    // @formatter:on

    AnnotationSorter testee = new AnnotationSorter(al, false);
    testee.sort(anns, SequenceAnnotationOrder.LABEL_AND_SEQUENCE);
    assertEquals("IRON", anns[0].label); // IRON / sequence 0
    assertEquals("iron", anns[1].label); // iron / sequence 3
    assertEquals("label0", anns[2].label); // label0 / sequence 1
    assertEquals("Structure", anns[3].label); // Structure / sequence 2
    assertEquals("structure", anns[4].label); // structure / sequence 3
    assertEquals("Quality", anns[5].label); // autocalc annotations
    assertEquals("Consensus", anns[6].label); // retain ordering
  }

  /**
   * Variant of test with autocalculated annotations sorted to front
   */
  @Test(groups = { "Functional" })
  public void testSortByTypeAndSequence_autocalcFirst()
  {
    // @formatter:off
    anns[0].sequenceRef = al.getSequenceAt(1); anns[0].label = "label0";
    anns[1].sequenceRef = al.getSequenceAt(3); anns[1].label = "structure";
    anns[2].sequenceRef = al.getSequenceAt(3); anns[2].label = "iron";
    anns[3].autoCalculated = true;             anns[3].label = "Quality";
    anns[4].autoCalculated = true;             anns[4].label = "Consensus";
    anns[5].sequenceRef = al.getSequenceAt(0); anns[5].label = "IRON";
    anns[6].sequenceRef = al.getSequenceAt(2); anns[6].label = "Structure";
    // @formatter:on

    AnnotationSorter testee = new AnnotationSorter(al, true);
    testee.sort(anns, SequenceAnnotationOrder.LABEL_AND_SEQUENCE);
    assertEquals("Quality", anns[0].label); // autocalc annotations
    assertEquals("Consensus", anns[1].label); // retain ordering
    assertEquals("IRON", anns[2].label); // IRON / sequence 0
    assertEquals("iron", anns[3].label); // iron / sequence 3
    assertEquals("label0", anns[4].label); // label0 / sequence 1
    assertEquals("Structure", anns[5].label); // Structure / sequence 2
    assertEquals("structure", anns[6].label); // structure / sequence 3
  }

  /**
   * Variant of test with autocalculated annotations sorted to front but
   * otherwise no change.
   */
  @Test(groups = { "Functional" })
  public void testNoSort_autocalcFirst()
  {
    // @formatter:off
    anns[0].sequenceRef = al.getSequenceAt(1); anns[0].label = "label0";
    anns[1].sequenceRef = al.getSequenceAt(3); anns[1].label = "structure";
    anns[2].sequenceRef = al.getSequenceAt(3); anns[2].label = "iron";
    anns[3].autoCalculated = true;             anns[3].label = "Quality";
    anns[4].autoCalculated = true;             anns[4].label = "Consensus";
    anns[5].sequenceRef = al.getSequenceAt(0); anns[5].label = "IRON";
    anns[6].sequenceRef = al.getSequenceAt(2); anns[6].label = "Structure";
    // @formatter:on

    AnnotationSorter testee = new AnnotationSorter(al, true);
    testee.sort(anns, SequenceAnnotationOrder.NONE);
    assertEquals("Quality", anns[0].label); // autocalc annotations
    assertEquals("Consensus", anns[1].label); // retain ordering
    assertEquals("label0", anns[2].label);
    assertEquals("structure", anns[3].label);
    assertEquals("iron", anns[4].label);
    assertEquals("IRON", anns[5].label);
    assertEquals("Structure", anns[6].label);
  }

  @Test(groups = { "Functional" })
  public void testSort_timingPresorted()
  {
    testTiming_presorted(50, 100);
    testTiming_presorted(500, 1000);
    testTiming_presorted(5000, 10000);
  }

  /**
   * Test timing to sort annotations already in the sort order.
   * 
   * @param numSeqs
   * @param numAnns
   */
  private void testTiming_presorted(final int numSeqs, final int numAnns)
  {
    Alignment alignment = buildAlignment(numSeqs);
    AlignmentAnnotation[] annotations = buildAnnotations(numAnns);

    /*
     * Set the annotations presorted by label
     */
    Random r = new Random();
    final SequenceI[] sequences = alignment.getSequencesArray();
    for (int i = 0; i < annotations.length; i++)
    {
      SequenceI randomSequenceRef = sequences[r.nextInt(sequences.length)];
      annotations[i].sequenceRef = randomSequenceRef;
      annotations[i].label = "label" + i;
    }
    long startTime = System.currentTimeMillis();
    AnnotationSorter testee = new AnnotationSorter(alignment, false);
    testee.sort(annotations, SequenceAnnotationOrder.LABEL_AND_SEQUENCE);
    long endTime = System.currentTimeMillis();
    final long elapsed = endTime - startTime;
    System.out.println(
            "Timing test for presorted " + numSeqs + " sequences and "
                    + numAnns + " annotations took " + elapsed + "ms");
  }

  /**
   * Timing tests for sorting randomly sorted annotations for various sizes.
   */
  @Test(groups = { "Functional" })
  public void testSort_timingUnsorted()
  {
    testTiming_unsorted(50, 100);
    testTiming_unsorted(500, 1000);
    testTiming_unsorted(5000, 10000);
  }

  /**
   * Generate annotations randomly sorted with respect to sequences, and time
   * sorting.
   * 
   * @param numSeqs
   * @param numAnns
   */
  private void testTiming_unsorted(final int numSeqs, final int numAnns)
  {
    Alignment alignment = buildAlignment(numSeqs);
    AlignmentAnnotation[] annotations = buildAnnotations(numAnns);

    /*
     * Set the annotations in random order with respect to the sequences
     */
    Random r = new Random();
    final SequenceI[] sequences = alignment.getSequencesArray();
    for (int i = 0; i < annotations.length; i++)
    {
      SequenceI randomSequenceRef = sequences[r.nextInt(sequences.length)];
      annotations[i].sequenceRef = randomSequenceRef;
      annotations[i].label = "label" + i;
    }
    long startTime = System.currentTimeMillis();
    AnnotationSorter testee = new AnnotationSorter(alignment, false);
    testee.sort(annotations, SequenceAnnotationOrder.SEQUENCE_AND_LABEL);
    long endTime = System.currentTimeMillis();
    final long elapsed = endTime - startTime;
    System.out.println(
            "Timing test for unsorted " + numSeqs + " sequences and "
                    + numAnns + " annotations took " + elapsed + "ms");
  }

  /**
   * Timing test for sorting annotations with a limited range of types (labels).
   */
  @Test(groups = { "Functional" })
  public void testSort_timingSemisorted()
  {
    testTiming_semiSorted(50, 100);
    testTiming_semiSorted(500, 1000);
    testTiming_semiSorted(5000, 10000);
  }

  /**
   * Mimic 'semi-sorted' annotations:
   * <ul>
   * <li>set up in sequence order, with randomly assigned labels from a limited
   * range</li>
   * <li>sort by label and sequence order, report timing</li>
   * <li>resort by sequence and label, report timing</li>
   * <li>resort by label and sequence, report timing</li>
   * </ul>
   * 
   * @param numSeqs
   * @param numAnns
   */
  private void testTiming_semiSorted(final int numSeqs, final int numAnns)
  {
    Alignment alignment = buildAlignment(numSeqs);
    AlignmentAnnotation[] annotations = buildAnnotations(numAnns);

    String[] labels = new String[] { "label1", "label2", "label3", "label4",
        "label5", "label6" };

    /*
     * Set the annotations in sequence order with randomly assigned labels.
     */
    Random r = new Random();
    final SequenceI[] sequences = alignment.getSequencesArray();
    for (int i = 0; i < annotations.length; i++)
    {
      SequenceI sequenceRef = sequences[i % sequences.length];
      annotations[i].sequenceRef = sequenceRef;
      annotations[i].label = labels[r.nextInt(labels.length)];
    }
    long startTime = System.currentTimeMillis();
    AnnotationSorter testee = new AnnotationSorter(alignment, false);
    testee.sort(annotations, SequenceAnnotationOrder.LABEL_AND_SEQUENCE);
    long endTime = System.currentTimeMillis();
    long elapsed = endTime - startTime;
    System.out.println(
            "Sort by label for semisorted " + numSeqs + " sequences and "
                    + numAnns + " annotations took " + elapsed + "ms");

    // now resort by sequence
    startTime = System.currentTimeMillis();
    testee.sort(annotations, SequenceAnnotationOrder.SEQUENCE_AND_LABEL);
    endTime = System.currentTimeMillis();
    elapsed = endTime - startTime;
    System.out.println("Resort by sequence for semisorted " + numSeqs
            + " sequences and " + numAnns + " annotations took " + elapsed
            + "ms");

    // now resort by label
    startTime = System.currentTimeMillis();
    testee.sort(annotations, SequenceAnnotationOrder.LABEL_AND_SEQUENCE);
    endTime = System.currentTimeMillis();
    elapsed = endTime - startTime;
    System.out.println(
            "Resort by label for semisorted " + numSeqs + " sequences and "
                    + numAnns + " annotations took " + elapsed + "ms");
  }
}
