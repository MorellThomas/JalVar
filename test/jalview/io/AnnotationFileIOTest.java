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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.Color;
import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceGroup;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.io.AnnotationFile.ViewDef;

public class AnnotationFileIOTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  static String TestFiles[][] = { { "Test example annotation import/export",
      "examples/uniref50.fa", "examples/testdata/example_annot_file.jva" },
      { "Test multiple combine annotation statements import/export",
          "examples/uniref50.fa",
          "examples/testdata/test_combine_annot.jva" },
      { "Test multiple combine annotation statements with sequence_ref import/export",
          "examples/uniref50.fa", "examples/testdata/uniref50_iupred.jva" },
      { "Test group only annotation file parsing results in parser indicating annotation was parsed",
          "examples/uniref50.fa", "examples/testdata/test_grpannot.jva" },
      { "Test hiding/showing of insertions on sequence_ref",
          "examples/uniref50.fa",
          "examples/testdata/uniref50_seqref.jva" } };

  @Test(groups = { "Functional" })
  public void exampleAnnotationFileIO() throws Exception
  {
    for (String[] testPair : TestFiles)
    {
      testAnnotationFileIO(testPair[0], new File(testPair[1]),
              new File(testPair[2]));
    }
  }

  protected AlignmentI readAlignmentFile(File f)
  {
    System.out.println("Reading file: " + f);
    String ff = f.getPath();
    try
    {
      FormatAdapter rf = new FormatAdapter();

      AlignmentI al = rf.readFile(ff, DataSourceType.FILE,
              new IdentifyFile().identify(ff, DataSourceType.FILE));

      // make sure dataset is initialised ? not sure about this
      for (int i = 0; i < al.getSequencesArray().length; ++i)
      {
        al.getSequenceAt(i).createDatasetSequence();
      }
      assertNotNull("Couldn't read supplied alignment data.", al);
      return al;
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    Assert.fail(
            "Couln't read the alignment in file '" + f.toString() + "'");
    return null;
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
   */
  void testAnnotationFileIO(String testname, File f, File annotFile)
  {
    System.out.println("Test: " + testname + "\nReading annotation file '"
            + annotFile + "' onto : " + f);
    String af = annotFile.getPath();
    try
    {
      AlignmentI al = readAlignmentFile(f);
      HiddenColumns cs = new HiddenColumns();
      assertTrue("Test " + testname
              + "\nAlignment was not annotated - annotation file not imported.",
              new AnnotationFile().readAnnotationFile(al, cs, af,
                      DataSourceType.FILE));

      AnnotationFile aff = new AnnotationFile();
      // ViewDef is not used by Jalview
      ViewDef v = aff.new ViewDef(null, al.getHiddenSequences(), cs,
              new Hashtable());
      String anfileout = new AnnotationFile().printAnnotations(
              al.getAlignmentAnnotation(), al.getGroups(),
              al.getProperties(), null, al, v);
      assertTrue("Test " + testname
              + "\nAlignment annotation file was not regenerated. Null string",
              anfileout != null);
      assertTrue("Test " + testname
              + "\nAlignment annotation file was not regenerated. Empty string",
              anfileout.length() > "JALVIEW_ANNOTATION".length());

      System.out.println(
              "Output annotation file:\n" + anfileout + "\n<<EOF\n");

      AlignmentI al_new = readAlignmentFile(f);
      assertTrue("Test " + testname
              + "\nregenerated annotation file did not annotate alignment.",
              new AnnotationFile().readAnnotationFile(al_new, anfileout,
                      DataSourceType.PASTE));

      // test for consistency in io
      StockholmFileTest.testAlignmentEquivalence(al, al_new, false, false,
              false);
      return;
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    Assert.fail("Test " + testname
            + "\nCouldn't complete Annotation file roundtrip input/output/input test for '"
            + annotFile + "'.");
  }

  @Test(groups = "Functional")
  public void testAnnotateAlignmentView()
  {
    long t1 = System.currentTimeMillis();
    /*
     * JAL-3779 test multiple groups of the same name get annotated
     */
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            ">Seq1\nQRSIL\n>Seq2\nFTHND\n>Seq3\nRPVSL\n",
            DataSourceType.PASTE);
    long t2 = System.currentTimeMillis();
    System.err.println("t0: " + (t2 - t1));
    // seq1 and seq3 are in distinct groups both named Group1
    String annotationFile = "JALVIEW_ANNOTATION\nSEQUENCE_GROUP\tGroup1\t*\t*\t1\n"
            + "SEQUENCE_GROUP\tGroup2\t*\t*\t2\n"
            + "SEQUENCE_GROUP\tGroup1\t*\t*\t3\n"
            + "PROPERTIES\tGroup1\toutlineColour=blue\tidColour=red\n";
    new AnnotationFile().annotateAlignmentView(af.getViewport(),
            annotationFile, DataSourceType.PASTE);

    AlignmentI al = af.getViewport().getAlignment();
    List<SequenceGroup> groups = al.getGroups();
    assertEquals(3, groups.size());
    SequenceGroup sg = groups.get(0);
    assertEquals("Group1", sg.getName());
    assertTrue(sg.contains(al.getSequenceAt(0)));
    assertEquals(Color.BLUE, sg.getOutlineColour());
    assertEquals(Color.RED, sg.getIdColour());
    sg = groups.get(1);
    assertEquals("Group2", sg.getName());
    assertTrue(sg.contains(al.getSequenceAt(1)));

    /*
     * the bug fix: a second group of the same name is also given properties
     */
    sg = groups.get(2);
    assertEquals("Group1", sg.getName());
    assertTrue(sg.contains(al.getSequenceAt(2)));
    assertEquals(Color.BLUE, sg.getOutlineColour());
    assertEquals(Color.RED, sg.getIdColour());
  }
}
