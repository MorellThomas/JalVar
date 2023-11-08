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
package jalview.io;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.util.DBRefUtils;

public class StockholmFileTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  static String PfamFile = "examples/PF00111_seed.stk",
          RfamFile = "examples/RF00031_folded.stk",
          RnaSSTestFile = "examples/rna_ss_test.stk";

  @Test(groups = { "Functional" })
  public void pfamFileIO() throws Exception
  {
    testFileIOwithFormat(new File(PfamFile), FileFormat.Stockholm, -1, 0,
            false, false, false);
  }

  @Test(groups = { "Functional" })
  public void pfamFileDataExtraction() throws Exception
  {
    AppletFormatAdapter af = new AppletFormatAdapter();
    AlignmentI al = af.readFile(PfamFile, DataSourceType.FILE,
            new IdentifyFile().identify(PfamFile, DataSourceType.FILE));
    int numpdb = 0;
    for (SequenceI sq : al.getSequences())
    {
      if (sq.getAllPDBEntries() != null)
      {
        numpdb += sq.getAllPDBEntries().size();
      }
    }
    assertTrue(
            "PF00111 seed alignment has at least 1 PDB file, but the reader found none.",
            numpdb > 0);
  }

  @Test(groups = { "Functional" })
  public void rfamFileIO() throws Exception
  {
    testFileIOwithFormat(new File(RfamFile), FileFormat.Stockholm, 2, 1,
            false, false, false);
  }

  /**
   * JAL-3529 - verify uniprot refs for sequences are output for sequences
   * retrieved via Pfam
   */
  @Test(groups = { "Functional" })
  public void dbrefOutput() throws Exception
  {
    // sequences retrieved in a Pfam domain alignment also have a PFAM database
    // reference
    SequenceI sq = new Sequence("FER2_SPIOL", "AASSDDDFFF");
    sq.addDBRef(new DBRefEntry("UNIPROT", "1", "P00224"));
    sq.addDBRef(new DBRefEntry("PFAM", "1", "P00224.1"));
    sq.addDBRef(new DBRefEntry("PFAM", "1", "PF00111"));
    AppletFormatAdapter af = new AppletFormatAdapter();
    String toStockholm = af.formatSequences(FileFormat.Stockholm,
            new Alignment(new SequenceI[]
            { sq }), false);
    System.out.println(toStockholm);
    // bleh - java.util.Regex sucks
    assertTrue(
            Pattern.compile(
                    "^#=GS\\s+FER2_SPIOL(/\\d+-\\d+)?\\s+AC\\s+P00224$",
                    Pattern.MULTILINE).matcher(toStockholm).find(),
            "Couldn't locate UNIPROT Accession in generated Stockholm file.");
    AlignmentI fromStockholm = af.readFile(toStockholm,
            DataSourceType.PASTE, FileFormat.Stockholm);
    SequenceI importedSeq = fromStockholm.getSequenceAt(0);
    assertTrue(importedSeq.getDBRefs().size() == 1,
            "Expected just one database reference to be added to sequence.");
    assertTrue(
            importedSeq.getDBRefs().get(0).getAccessionId()
                    .indexOf(" ") == -1,
            "Spaces were found in accession ID.");
    List<DBRefEntry> dbrefs = DBRefUtils.searchRefs(importedSeq.getDBRefs(),
            "P00224");
    assertTrue(dbrefs.size() == 1,
            "Couldn't find Uniprot DBRef on re-imported sequence.");

  }

  @Test(groups = { "Functional" })
  public void descriptionLineOutput() throws Exception
  {
    // quick test that sequence description is exported & reimported

    SequenceI sq = new Sequence("FER2_SPIOL", "AASSDDDFFF");
    String expected_descr = "This is a description !@&^%@£@";
    sq.setDescription(expected_descr);
    AppletFormatAdapter af = new AppletFormatAdapter();
    String toStockholm = af.formatSequences(FileFormat.Stockholm,
            new Alignment(new SequenceI[]
            { sq }), false);
    System.out.println(toStockholm);

    // bleh - java.util.Regex sucks
    assertTrue(toStockholm.contains(expected_descr),
            "Couldn't locate expected description srting in generated Stockholm file.");

    AlignmentI fromStockholm = af.readFile(toStockholm,
            DataSourceType.PASTE, FileFormat.Stockholm);
    SequenceI importedSeq = fromStockholm.getSequenceAt(0);
    assertEquals("Description did not get reimported.", expected_descr,
            importedSeq.getDescription());
  }

  /**
   * test alignment data in given file can be imported, exported and reimported
   * with no dataloss
   * 
   * @param f
   *          - source datafile (IdentifyFile.identify() should work with it)
   * @param ioformat
   *          - label for IO class used to write and read back in the data from
   *          f
   * @param ignoreFeatures
   * @param ignoreRowVisibility
   * @param allowNullAnnotations
   */

  public static void testFileIOwithFormat(File f, FileFormatI ioformat,
          int naliannot, int nminseqann, boolean ignoreFeatures,
          boolean ignoreRowVisibility, boolean allowNullAnnotations)
  {
    System.out.println("Reading file: " + f);
    String ff = f.getPath();
    try
    {
      AppletFormatAdapter rf = new AppletFormatAdapter();

      AlignmentI al = rf.readFile(ff, DataSourceType.FILE,
              new IdentifyFile().identify(ff, DataSourceType.FILE));

      assertNotNull("Couldn't read supplied alignment data.", al);

      // make sure dataset is initialised ? not sure about this
      for (int i = 0; i < al.getSequencesArray().length; ++i)
      {
        al.getSequenceAt(i).createDatasetSequence();
      }
      String outputfile = rf.formatSequences(ioformat, al, true);
      System.out.println("Output file in '" + ioformat + "':\n" + outputfile
              + "\n<<EOF\n");
      // test for consistency in io
      AlignmentI al_input = new AppletFormatAdapter().readFile(outputfile,
              DataSourceType.PASTE, ioformat);
      assertNotNull("Couldn't parse reimported alignment data.", al_input);

      FileFormatI identifyoutput = new IdentifyFile().identify(outputfile,
              DataSourceType.PASTE);
      assertNotNull("Identify routine failed for outputformat " + ioformat,
              identifyoutput);
      assertTrue(
              "Identify routine could not recognise output generated by '"
                      + ioformat + "' writer",
              ioformat.equals(identifyoutput));
      testAlignmentEquivalence(al, al_input, ignoreFeatures,
              ignoreRowVisibility, allowNullAnnotations);
      int numaliannot = 0, numsqswithali = 0;
      for (AlignmentAnnotation ala : al_input.getAlignmentAnnotation())
      {
        if (ala.sequenceRef == null)
        {
          numaliannot++;
        }
        else
        {
          numsqswithali++;
        }
      }
      if (naliannot > -1)
      {
        assertEquals("Number of alignment annotations", naliannot,
                numaliannot);
      }

      assertTrue(
              "Number of sequence associated annotations wasn't at least "
                      + nminseqann,
              numsqswithali >= nminseqann);

    } catch (Exception e)
    {
      e.printStackTrace();
      assertTrue("Couln't format the alignment for output file.", false);
    }
  }

  /**
   * assert alignment equivalence
   * 
   * @param al
   *          'original'
   * @param al_input
   *          'secondary' or generated alignment from some datapreserving
   *          transformation
   * @param ignoreFeatures
   *          when true, differences in sequence feature annotation are ignored
   */
  public static void testAlignmentEquivalence(AlignmentI al,
          AlignmentI al_input, boolean ignoreFeatures)
  {
    testAlignmentEquivalence(al, al_input, ignoreFeatures, false, false);
  }

  /**
   * assert alignment equivalence - uses special comparators for RNA structure
   * annotation rows.
   * 
   * @param al
   *          'original'
   * @param al_input
   *          'secondary' or generated alignment from some datapreserving
   *          transformation
   * @param ignoreFeatures
   *          when true, differences in sequence feature annotation are ignored
   * 
   * @param ignoreRowVisibility
   *          when true, do not fail if there are differences in the visibility
   *          of annotation rows
   * @param allowNullAnnotation
   *          when true, positions in alignment annotation that are null will be
   *          considered equal to positions containing annotation where
   *          Annotation.isWhitespace() returns true.
   * 
   */
  public static void testAlignmentEquivalence(AlignmentI al,
          AlignmentI al_input, boolean ignoreFeatures,
          boolean ignoreRowVisibility, boolean allowNullAnnotation)
  {
    assertNotNull("Original alignment was null", al);
    assertNotNull("Generated alignment was null", al_input);

    assertTrue(
            "Alignment dimension mismatch: original: " + al.getHeight()
                    + "x" + al.getWidth() + ", generated: "
                    + al_input.getHeight() + "x" + al_input.getWidth(),
            al.getHeight() == al_input.getHeight()
                    && al.getWidth() == al_input.getWidth());

    // check Alignment annotation
    AlignmentAnnotation[] aa_new = al_input.getAlignmentAnnotation();
    AlignmentAnnotation[] aa_original = al.getAlignmentAnnotation();
    boolean expectProteinSS = !al.isNucleotide();
    assertTrue(
            "Alignments not both "
                    + (al.isNucleotide() ? "nucleotide" : "protein"),
            al_input.isNucleotide() == al.isNucleotide());

    // note - at moment we do not distinguish between alignment without any
    // annotation rows and alignment with no annotation row vector
    // we might want to revise this in future
    int aa_new_size = (aa_new == null ? 0 : aa_new.length);
    int aa_original_size = (aa_original == null ? 0 : aa_original.length);
    Map<Integer, BitSet> orig_groups = new HashMap<>();
    Map<Integer, BitSet> new_groups = new HashMap<>();

    if (aa_new != null && aa_original != null)
    {
      for (int i = 0; i < aa_original.length; i++)
      {
        if (aa_new.length > i)
        {
          assertEqualSecondaryStructure(
                  "Different alignment annotation at position " + i,
                  aa_original[i], aa_new[i], allowNullAnnotation);
          if (aa_original[i].hasIcons)
          {
            assertTrue(
                    "Secondary structure expected to be "
                            + (expectProteinSS ? "protein" : "nucleotide"),
                    expectProteinSS == !aa_original[i].isRNA());
          }
          // compare graphGroup or graph properties - needed to verify JAL-1299
          assertEquals("Graph type not identical.", aa_original[i].graph,
                  aa_new[i].graph);
          if (!ignoreRowVisibility)
          {
            assertEquals("Visibility not identical.",
                    aa_original[i].visible, aa_new[i].visible);
          }
          assertEquals("Threshold line not identical.",
                  aa_original[i].threshold, aa_new[i].threshold);
          // graphGroup may differ, but pattern should be the same
          Integer o_ggrp = Integer.valueOf(aa_original[i].graphGroup + 2);
          Integer n_ggrp = Integer.valueOf(aa_new[i].graphGroup + 2);
          BitSet orig_g = orig_groups.get(o_ggrp);
          BitSet new_g = new_groups.get(n_ggrp);
          if (orig_g == null)
          {
            orig_groups.put(o_ggrp, orig_g = new BitSet());
          }
          if (new_g == null)
          {
            new_groups.put(n_ggrp, new_g = new BitSet());
          }
          assertEquals("Graph Group pattern differs at annotation " + i,
                  orig_g, new_g);
          orig_g.set(i);
          new_g.set(i);
        }
        else
        {
          System.err.println("No matching annotation row for "
                  + aa_original[i].toString());
        }
      }
    }
    assertEquals(
            "Generated and imported alignment have different annotation sets",
            aa_original_size, aa_new_size);

    // check sequences, annotation and features
    SequenceI[] seq_original = new SequenceI[al.getSequencesArray().length];
    seq_original = al.getSequencesArray();
    SequenceI[] seq_new = new SequenceI[al_input
            .getSequencesArray().length];
    seq_new = al_input.getSequencesArray();
    List<SequenceFeature> sequenceFeatures_original;
    List<SequenceFeature> sequenceFeatures_new;
    AlignmentAnnotation annot_original, annot_new;
    //
    for (int i = 0; i < al.getSequencesArray().length; i++)
    {
      String name = seq_original[i].getName();
      int start = seq_original[i].getStart();
      int end = seq_original[i].getEnd();
      System.out
              .println("Check sequence: " + name + "/" + start + "-" + end);

      // search equal sequence
      for (int in = 0; in < al_input.getSequencesArray().length; in++)
      {
        if (name.equals(seq_new[in].getName())
                && start == seq_new[in].getStart()
                && end == seq_new[in].getEnd())
        {
          String ss_original = seq_original[i].getSequenceAsString();
          String ss_new = seq_new[in].getSequenceAsString();
          assertEquals("The sequences " + name + "/" + start + "-" + end
                  + " are not equal", ss_original, ss_new);

          assertTrue(
                  "Sequence Features were not equivalent"
                          + (ignoreFeatures ? " ignoring." : ""),
                  ignoreFeatures
                          || (seq_original[i].getSequenceFeatures() == null
                                  && seq_new[in]
                                          .getSequenceFeatures() == null)
                          || (seq_original[i].getSequenceFeatures() != null
                                  && seq_new[in]
                                          .getSequenceFeatures() != null));
          // compare sequence features
          if (seq_original[i].getSequenceFeatures() != null
                  && seq_new[in].getSequenceFeatures() != null)
          {
            System.out.println("There are feature!!!");
            sequenceFeatures_original = seq_original[i]
                    .getSequenceFeatures();
            sequenceFeatures_new = seq_new[in].getSequenceFeatures();

            assertEquals("different number of features",
                    seq_original[i].getSequenceFeatures().size(),
                    seq_new[in].getSequenceFeatures().size());

            for (int feat = 0; feat < seq_original[i].getSequenceFeatures()
                    .size(); feat++)
            {
              assertEquals("Different features",
                      sequenceFeatures_original.get(feat),
                      sequenceFeatures_new.get(feat));
            }
          }
          // compare alignment annotation
          if (al.getSequenceAt(i).getAnnotation() != null
                  && al_input.getSequenceAt(in).getAnnotation() != null)
          {
            for (int j = 0; j < al.getSequenceAt(i)
                    .getAnnotation().length; j++)
            {
              if (al.getSequenceAt(i).getAnnotation()[j] != null && al_input
                      .getSequenceAt(in).getAnnotation()[j] != null)
              {
                annot_original = al.getSequenceAt(i).getAnnotation()[j];
                annot_new = al_input.getSequenceAt(in).getAnnotation()[j];
                assertEqualSecondaryStructure(
                        "Different annotation elements", annot_original,
                        annot_new, allowNullAnnotation);
              }
            }
          }
          else if (al.getSequenceAt(i).getAnnotation() == null
                  && al_input.getSequenceAt(in).getAnnotation() == null)
          {
            System.out.println("No annotations");
          }
          else if (al.getSequenceAt(i).getAnnotation() != null
                  && al_input.getSequenceAt(in).getAnnotation() == null)
          {
            fail("Annotations differed between sequences ("
                    + al.getSequenceAt(i).getName() + ") and ("
                    + al_input.getSequenceAt(i).getName() + ")");
          }
          break;
        }
      }
    }
  }

  /**
   * compare two annotation rows, with special support for secondary structure
   * comparison. With RNA, only the value and the secondaryStructure symbols are
   * compared, displayCharacter and description are ignored. Annotations where
   * Annotation.isWhitespace() is true are always considered equal.
   * 
   * @param message
   *          - not actually used yet..
   * @param annot_or
   *          - the original annotation
   * @param annot_new
   *          - the one compared to the original annotation
   * @param allowNullEquivalence
   *          when true, positions in alignment annotation that are null will be
   *          considered equal to non-null positions for which
   *          Annotation.isWhitespace() is true.
   */
  private static void assertEqualSecondaryStructure(String message,
          AlignmentAnnotation annot_or, AlignmentAnnotation annot_new,
          boolean allowNullEqivalence)
  {
    // TODO: test to cover this assert behaves correctly for all allowed
    // variations of secondary structure annotation row equivalence
    if (annot_or.annotations.length != annot_new.annotations.length)
    {
      fail("Different lengths for annotation row elements: "
              + annot_or.annotations.length + "!="
              + annot_new.annotations.length);
    }
    boolean isRna = annot_or.isRNA();
    assertTrue(
            "Expected " + (isRna ? " valid RNA " : " no RNA ")
                    + " secondary structure in the row.",
            isRna == annot_new.isRNA());
    for (int i = 0; i < annot_or.annotations.length; i++)
    {
      Annotation an_or = annot_or.annotations[i],
              an_new = annot_new.annotations[i];
      if (an_or != null && an_new != null)
      {

        if (isRna)
        {
          if (an_or.secondaryStructure != an_new.secondaryStructure
                  || ((Float.isNaN(an_or.value) != Float
                          .isNaN(an_new.value))
                          || an_or.value != an_new.value))
          {
            fail("Different RNA secondary structure at column " + i
                    + " expected: [" + annot_or.annotations[i].toString()
                    + "] but got: [" + annot_new.annotations[i].toString()
                    + "]");
          }
        }
        else
        {
          // not RNA secondary structure, so expect all elements to match...
          if ((an_or.isWhitespace() != an_new.isWhitespace())
                  || !an_or.displayCharacter.trim()
                          .equals(an_new.displayCharacter.trim())
                  || !("" + an_or.secondaryStructure).trim()
                          .equals(("" + an_new.secondaryStructure).trim())
                  || (an_or.description != an_new.description
                          && !((an_or.description == null
                                  && an_new.description.trim()
                                          .length() == 0)
                                  || (an_new.description == null
                                          && an_or.description.trim()
                                                  .length() == 0)
                                  || an_or.description.trim().equals(
                                          an_new.description.trim())))
                  || !((Float.isNaN(an_or.value)
                          && Float.isNaN(an_new.value))
                          || an_or.value == an_new.value))
          {
            fail("Annotation Element Mismatch\nElement " + i
                    + " in original: " + annot_or.annotations[i].toString()
                    + "\nElement " + i + " in new: "
                    + annot_new.annotations[i].toString());
          }
        }
      }
      else if (annot_or.annotations[i] == null
              && annot_new.annotations[i] == null)
      {
        continue;
      }
      else
      {
        if (allowNullEqivalence)
        {
          if (an_or != null && an_or.isWhitespace())

          {
            continue;
          }
          if (an_new != null && an_new.isWhitespace())
          {
            continue;
          }
        }
        // need also to test for null in one, non-SS annotation in other...
        fail("Annotation Element Mismatch\nElement " + i + " in original: "
                + (an_or == null ? "is null" : an_or.toString())
                + "\nElement " + i + " in new: "
                + (an_new == null ? "is null" : an_new.toString()));
      }
    }
  }

  /**
   * @see assertEqualSecondaryStructure - test if two secondary structure
   *      annotations are not equal
   * @param message
   * @param an_orig
   * @param an_new
   * @param allowNullEquivalence
   */
  public static void assertNotEqualSecondaryStructure(String message,
          AlignmentAnnotation an_orig, AlignmentAnnotation an_new,
          boolean allowNullEquivalence)
  {
    boolean thrown = false;
    try
    {
      assertEqualSecondaryStructure("", an_orig, an_new,
              allowNullEquivalence);
    } catch (AssertionError af)
    {
      thrown = true;
    }
    if (!thrown)
    {
      fail("Expected difference for [" + an_orig + "] and [" + an_new
              + "]");
    }
  }

  private AlignmentAnnotation makeAnnot(Annotation ae)
  {
    return new AlignmentAnnotation("label", "description",
            new Annotation[]
            { ae });
  }

  @Test(groups = { "Functional" })
  public void testAnnotationEquivalence()
  {
    AlignmentAnnotation one = makeAnnot(new Annotation("", "", ' ', 1));
    AlignmentAnnotation anotherOne = makeAnnot(
            new Annotation("", "", ' ', 1));
    AlignmentAnnotation sheet = makeAnnot(new Annotation("", "", 'E', 0f));
    AlignmentAnnotation anotherSheet = makeAnnot(
            new Annotation("", "", 'E', 0f));
    AlignmentAnnotation sheetWithLabel = makeAnnot(
            new Annotation("1", "", 'E', 0f));
    AlignmentAnnotation anotherSheetWithLabel = makeAnnot(
            new Annotation("1", "", 'E', 0f));
    AlignmentAnnotation rnaNoDC = makeAnnot(
            new Annotation("", "", '<', 0f));
    AlignmentAnnotation anotherRnaNoDC = makeAnnot(
            new Annotation("", "", '<', 0f));
    AlignmentAnnotation rnaWithDC = makeAnnot(
            new Annotation("B", "", '<', 0f));
    AlignmentAnnotation anotherRnaWithDC = makeAnnot(
            new Annotation("B", "", '<', 0f));

    // check self equivalence
    for (boolean allowNull : new boolean[] { true, false })
    {
      assertEqualSecondaryStructure("Should be equal", one, anotherOne,
              allowNull);
      assertEqualSecondaryStructure("Should be equal", sheet, anotherSheet,
              allowNull);
      assertEqualSecondaryStructure("Should be equal", sheetWithLabel,
              anotherSheetWithLabel, allowNull);
      assertEqualSecondaryStructure("Should be equal", rnaNoDC,
              anotherRnaNoDC, allowNull);
      assertEqualSecondaryStructure("Should be equal", rnaWithDC,
              anotherRnaWithDC, allowNull);
      // display character doesn't matter for RNA structure (for 2.10.2)
      assertEqualSecondaryStructure("Should be equal", rnaWithDC, rnaNoDC,
              allowNull);
      assertEqualSecondaryStructure("Should be equal", rnaNoDC, rnaWithDC,
              allowNull);
    }

    // verify others are different
    List<AlignmentAnnotation> aaSet = Arrays.asList(one, sheet,
            sheetWithLabel, rnaWithDC);
    for (int p = 0; p < aaSet.size(); p++)
    {
      for (int q = 0; q < aaSet.size(); q++)
      {
        if (p != q)
        {
          assertNotEqualSecondaryStructure("Should be different",
                  aaSet.get(p), aaSet.get(q), false);
        }
        else
        {
          assertEqualSecondaryStructure("Should be same", aaSet.get(p),
                  aaSet.get(q), false);
          assertEqualSecondaryStructure("Should be same", aaSet.get(p),
                  aaSet.get(q), true);
          assertNotEqualSecondaryStructure(
                  "Should be different to empty anot", aaSet.get(p),
                  makeAnnot(Annotation.EMPTY_ANNOTATION), false);
          assertNotEqualSecondaryStructure(
                  "Should be different to empty annot",
                  makeAnnot(Annotation.EMPTY_ANNOTATION), aaSet.get(q),
                  true);
          assertNotEqualSecondaryStructure("Should be different to null",
                  aaSet.get(p), makeAnnot(null), false);
          assertNotEqualSecondaryStructure("Should be different to null",
                  makeAnnot(null), aaSet.get(q), true);
        }
      }
    }

    // test null

  }

  String aliFile = ">Dm\nAAACCCUUUUACACACGGGAAAGGG";

  String annFile = "JALVIEW_ANNOTATION\n# Created: Thu May 04 11:16:52 BST 2017\n\n"
          + "SEQUENCE_REF\tDm\nNO_GRAPH\tsecondary structure\tsecondary structure\t"
          + "(|(|(|(|, .|, .|, .|, .|)|)|)|)|\t0.0\nROWPROPERTIES\t"
          + "secondary structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false";

  String annFileCurlyWuss = "JALVIEW_ANNOTATION\n# Created: Thu May 04 11:16:52 BST 2017\n\n"
          + "SEQUENCE_REF\tDm\nNO_GRAPH\tsecondary structure\tsecondary structure\t"
          + "(|(|(|(||{|{||{|{||)|)|)|)||}|}|}|}|\t0.0\nROWPROPERTIES\t"
          + "secondary structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false";

  String annFileFullWuss = "JALVIEW_ANNOTATION\n# Created: Thu May 04 11:16:52 BST 2017\n\n"
          + "SEQUENCE_REF\tDm\nNO_GRAPH\tsecondary structure\tsecondary structure\t"
          + "(|(|(|(||{|{||[|[||)|)|)|)||}|}|]|]|\t0.0\nROWPROPERTIES\t"
          + "secondary structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false";

  @Test(groups = { "Functional" })
  public void secondaryStructureForRNASequence() throws Exception
  {
    roundTripSSForRNA(aliFile, annFile);
  }

  @Test(groups = { "Functional" })
  public void curlyWUSSsecondaryStructureForRNASequence() throws Exception
  {
    roundTripSSForRNA(aliFile, annFileCurlyWuss);
  }

  @Test(groups = { "Functional" })
  public void fullWUSSsecondaryStructureForRNASequence() throws Exception
  {
    roundTripSSForRNA(aliFile, annFileFullWuss);
  }

  @Test(groups = { "Functional" })
  public void detectWussBrackets()
  {
    for (char ch : new char[] { '{', '}', '[', ']', '(', ')', '<', '>' })
    {
      Assert.assertTrue(StockholmFile.RNASS_BRACKETS.indexOf(ch) >= 0,
              "Didn't recognise '" + ch + "' as a WUSS bracket");
    }
    for (char ch : new char[] { '@', '!', '*', ' ', '-', '.' })
    {
      Assert.assertFalse(StockholmFile.RNASS_BRACKETS.indexOf(ch) >= 0,
              "Shouldn't recognise '" + ch + "' as a WUSS bracket");
    }
  }

  private static void roundTripSSForRNA(String aliFile, String annFile)
          throws Exception
  {
    AlignmentI al = new AppletFormatAdapter().readFile(aliFile,
            DataSourceType.PASTE, jalview.io.FileFormat.Fasta);
    AnnotationFile aaf = new AnnotationFile();
    aaf.readAnnotationFile(al, annFile, DataSourceType.PASTE);
    al.getAlignmentAnnotation()[0].visible = true;

    // TODO: create a better 'save as <format>' pattern
    StockholmFile sf = new StockholmFile(al);

    String stockholmFile = sf.print(al.getSequencesArray(), true);

    AlignmentI newAl = new AppletFormatAdapter().readFile(stockholmFile,
            DataSourceType.PASTE, jalview.io.FileFormat.Stockholm);
    // AlignmentUtils.showOrHideSequenceAnnotations(newAl.getViewport()
    // .getAlignment(), Arrays.asList("Secondary Structure"), newAl
    // .getViewport().getAlignment().getSequences(), true, true);
    testAlignmentEquivalence(al, newAl, true, true, true);

  }

  // this is the single sequence alignment and the SS annotations equivalent to
  // the ones in file RnaSSTestFile
  String aliFileRnaSS = ">Test.sequence/1-14\n" + "GUACAAAAAAAAAA";

  String annFileRnaSSAlphaChars = "JALVIEW_ANNOTATION\n"
          + "# Created: Thu Aug 02 14:54:57 BST 2018\n" + "\n"
          + "NO_GRAPH\tSecondary Structure\tSecondary Structure\t<,<|(,(|E,E|H,H|B,B|h,h|e,e|b,b|(,(|E,E|),)|e,e|),)|>,>|\t2.0\n"
          + "\n"
          + "ROWPROPERTIES\tSecondary Structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false\n"
          + "\n" + "\n" + "ALIGNMENT\tID=RNA.SS.TEST\tTP=RNA;";

  String wrongAnnFileRnaSSAlphaChars = "JALVIEW_ANNOTATION\n"
          + "# Created: Thu Aug 02 14:54:57 BST 2018\n" + "\n"
          + "NO_GRAPH\tSecondary Structure\tSecondary Structure\t<,<|(,(|H,H|E,E|B,B|h,h|e,e|b,b|(,(|E,E|),)|e,e|),)|>,>|\t2.0\n"
          + "\n"
          + "ROWPROPERTIES\tSecondary Structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false\n"
          + "\n" + "\n" + "ALIGNMENT\tID=RNA.SS.TEST\tTP=RNA;";

  @Test(groups = { "Functional" })
  public void stockholmFileRnaSSAlphaChars() throws Exception
  {
    AppletFormatAdapter af = new AppletFormatAdapter();
    AlignmentI al = af.readFile(RnaSSTestFile, DataSourceType.FILE,
            jalview.io.FileFormat.Stockholm);
    Iterable<AlignmentAnnotation> aai = al.findAnnotations(null, null,
            "Secondary Structure");
    AlignmentAnnotation aa = aai.iterator().next();
    Assert.assertTrue(aa.isRNA(),
            "'" + RnaSSTestFile + "' not recognised as RNA SS");
    Assert.assertTrue(aa.isValidStruc(),
            "'" + RnaSSTestFile + "' not recognised as valid structure");
    Annotation[] as = aa.annotations;
    char[] As = new char[as.length];
    for (int i = 0; i < as.length; i++)
    {
      As[i] = as[i].secondaryStructure;
    }
    char[] shouldBe = { '<', '(', 'E', 'H', 'B', 'h', 'e', 'b', '(', 'E',
        ')', 'e', ')', '>' };
    Assert.assertTrue(Arrays.equals(As, shouldBe), "Annotation is "
            + new String(As) + " but should be " + new String(shouldBe));

    // this should result in the same RNA SS Annotations
    AlignmentI newAl = new AppletFormatAdapter().readFile(aliFileRnaSS,
            DataSourceType.PASTE, jalview.io.FileFormat.Fasta);
    AnnotationFile aaf = new AnnotationFile();
    aaf.readAnnotationFile(newAl, annFileRnaSSAlphaChars,
            DataSourceType.PASTE);

    Assert.assertTrue(
            testRnaSSAnnotationsEquivalent(al.getAlignmentAnnotation()[0],
                    newAl.getAlignmentAnnotation()[0]),
            "RNA SS Annotations SHOULD be pair-wise equivalent (but apparently aren't): \n"
                    + "RNA SS A 1:" + al.getAlignmentAnnotation()[0] + "\n"
                    + "RNA SS A 2:" + newAl.getAlignmentAnnotation()[0]);

    // this should NOT result in the same RNA SS Annotations
    newAl = new AppletFormatAdapter().readFile(aliFileRnaSS,
            DataSourceType.PASTE, jalview.io.FileFormat.Fasta);
    aaf = new AnnotationFile();
    aaf.readAnnotationFile(newAl, wrongAnnFileRnaSSAlphaChars,
            DataSourceType.PASTE);

    boolean mismatch = testRnaSSAnnotationsEquivalent(
            al.getAlignmentAnnotation()[0],
            newAl.getAlignmentAnnotation()[0]);
    Assert.assertFalse(mismatch,
            "RNA SS Annotations SHOULD NOT be pair-wise equivalent (but apparently are): \n"
                    + "RNA SS A 1:" + al.getAlignmentAnnotation()[0] + "\n"
                    + "RNA SS A 2:" + newAl.getAlignmentAnnotation()[0]);
  }

  private static boolean testRnaSSAnnotationsEquivalent(
          AlignmentAnnotation a1, AlignmentAnnotation a2)
  {
    return a1.rnaSecondaryStructureEquivalent(a2);
  }

  String annFileRnaSSWithSpaceChars = "JALVIEW_ANNOTATION\n"
          + "# Created: Thu Aug 02 14:54:57 BST 2018\n" + "\n"
          + "NO_GRAPH\tSecondary Structure\tSecondary Structure\t<,<|.,.|H,H| , |B,B|h,h| , |b,b|(,(|E,E|.,.|e,e|),)|>,>|\t2.0\n"
          + "\n"
          + "ROWPROPERTIES\tSecondary Structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false\n"
          + "\n" + "\n" + "ALIGNMENT\tID=RNA.SS.TEST\tTP=RNA;";

  String annFileRnaSSWithoutSpaceChars = "JALVIEW_ANNOTATION\n"
          + "# Created: Thu Aug 02 14:54:57 BST 2018\n" + "\n"
          + "NO_GRAPH\tSecondary Structure\tSecondary Structure\t<,<|.,.|H,H|.,.|B,B|h,h|.,.|b,b|(,(|E,E|.,.|e,e|),)|>,>|\t2.0\n"
          + "\n"
          + "ROWPROPERTIES\tSecondary Structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false\n"
          + "\n" + "\n" + "ALIGNMENT\tID=RNA.SS.TEST\tTP=RNA;";

  String wrongAnnFileRnaSSWithoutSpaceChars = "JALVIEW_ANNOTATION\n"
          + "# Created: Thu Aug 02 14:54:57 BST 2018\n" + "\n"
          + "NO_GRAPH\tSecondary Structure\tSecondary Structure\t<,<|.,.|H,H|Z,Z|B,B|h,h|z,z|b,b|(,(|E,E|.,.|e,e|),)|>,>|\t2.0\n"
          + "\n"
          + "ROWPROPERTIES\tSecondary Structure\tscaletofit=true\tshowalllabs=true\tcentrelabs=false\n"
          + "\n" + "\n" + "ALIGNMENT\tID=RNA.SS.TEST\tTP=RNA;";

  @Test(groups = { "Functional" })
  public void stockholmFileRnaSSSpaceChars() throws Exception
  {
    AlignmentI alWithSpaces = new AppletFormatAdapter().readFile(
            aliFileRnaSS, DataSourceType.PASTE,
            jalview.io.FileFormat.Fasta);
    AnnotationFile afWithSpaces = new AnnotationFile();
    afWithSpaces.readAnnotationFile(alWithSpaces,
            annFileRnaSSWithSpaceChars, DataSourceType.PASTE);

    Iterable<AlignmentAnnotation> aaiWithSpaces = alWithSpaces
            .findAnnotations(null, null, "Secondary Structure");
    AlignmentAnnotation aaWithSpaces = aaiWithSpaces.iterator().next();
    Assert.assertTrue(aaWithSpaces.isRNA(),
            "'" + aaWithSpaces + "' not recognised as RNA SS");
    Assert.assertTrue(aaWithSpaces.isValidStruc(),
            "'" + aaWithSpaces + "' not recognised as valid structure");
    Annotation[] annWithSpaces = aaWithSpaces.annotations;
    char[] As = new char[annWithSpaces.length];
    for (int i = 0; i < annWithSpaces.length; i++)
    {
      As[i] = annWithSpaces[i].secondaryStructure;
    }
    // check all spaces and dots are spaces in the internal representation
    char[] shouldBe = { '<', ' ', 'H', ' ', 'B', 'h', ' ', 'b', '(', 'E',
        ' ', 'e', ')', '>' };
    Assert.assertTrue(Arrays.equals(As, shouldBe), "Annotation is "
            + new String(As) + " but should be " + new String(shouldBe));

    // this should result in the same RNA SS Annotations
    AlignmentI alWithoutSpaces = new AppletFormatAdapter().readFile(
            aliFileRnaSS, DataSourceType.PASTE,
            jalview.io.FileFormat.Fasta);
    AnnotationFile afWithoutSpaces = new AnnotationFile();
    afWithoutSpaces.readAnnotationFile(alWithoutSpaces,
            annFileRnaSSWithoutSpaceChars, DataSourceType.PASTE);

    Assert.assertTrue(
            testRnaSSAnnotationsEquivalent(
                    alWithSpaces.getAlignmentAnnotation()[0],
                    alWithoutSpaces.getAlignmentAnnotation()[0]),
            "RNA SS Annotations SHOULD be pair-wise equivalent (but apparently aren't): \n"
                    + "RNA SS A 1:"
                    + alWithSpaces.getAlignmentAnnotation()[0]
                            .getRnaSecondaryStructure()
                    + "\n" + "RNA SS A 2:"
                    + alWithoutSpaces.getAlignmentAnnotation()[0]
                            .getRnaSecondaryStructure());

    // this should NOT result in the same RNA SS Annotations
    AlignmentI wrongAlWithoutSpaces = new AppletFormatAdapter().readFile(
            aliFileRnaSS, DataSourceType.PASTE,
            jalview.io.FileFormat.Fasta);
    AnnotationFile wrongAfWithoutSpaces = new AnnotationFile();
    wrongAfWithoutSpaces.readAnnotationFile(wrongAlWithoutSpaces,
            wrongAnnFileRnaSSWithoutSpaceChars, DataSourceType.PASTE);

    Assert.assertFalse(
            testRnaSSAnnotationsEquivalent(
                    alWithSpaces.getAlignmentAnnotation()[0],
                    wrongAlWithoutSpaces.getAlignmentAnnotation()[0]),
            "RNA SS Annotations SHOULD NOT be pair-wise equivalent (but apparently are): \n"
                    + "RNA SS A 1:"
                    + alWithSpaces.getAlignmentAnnotation()[0]
                            .getRnaSecondaryStructure()
                    + "\n" + "RNA SS A 2:"
                    + wrongAlWithoutSpaces.getAlignmentAnnotation()[0]
                            .getRnaSecondaryStructure());

    // check no spaces in the output
    // TODO: create a better 'save as <format>' pattern
    alWithSpaces.getAlignmentAnnotation()[0].visible = true;
    StockholmFile sf = new StockholmFile(alWithSpaces);

    String stockholmFile = sf.print(alWithSpaces.getSequencesArray(), true);
    Pattern noSpacesInRnaSSAnnotation = Pattern
            .compile("\\n#=GC SS_cons\\s+\\S{14}\\n");
    Matcher m = noSpacesInRnaSSAnnotation.matcher(stockholmFile);
    boolean matches = m.find();
    Assert.assertTrue(matches,
            "StockholmFile output does not contain expected output (may contain spaces):\n"
                    + stockholmFile);

  }
}
