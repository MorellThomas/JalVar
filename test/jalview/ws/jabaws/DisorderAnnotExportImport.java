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
package jalview.ws.jabaws;

import java.util.Locale;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.gui.JvOptionPane;
import jalview.io.AnnotationFile;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;
import jalview.io.StockholmFileTest;
import jalview.ws.jws2.AADisorderClient;
import jalview.ws.jws2.Jws2Discoverer;
import jalview.ws.jws2.jabaws2.Jws2Instance;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/*
 * All methods in this class are set to the Network group because setUpBeforeClass will fail
 * if there is no network.
 */
@Test(singleThreaded = true)
public class DisorderAnnotExportImport
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  public static String testseqs = "examples/uniref50.fa";

  public static Jws2Discoverer disc;

  public static List<Jws2Instance> iupreds;

  jalview.ws.jws2.AADisorderClient disorderClient;

  public static jalview.gui.AlignFrame af = null;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Console.initLogger();
    disc = JalviewJabawsTestUtils.getJabawsDiscoverer();

    while (disc.isRunning())
    {
      // don't get services until discoverer has finished
      Thread.sleep(100);
    }

    iupreds = new ArrayList<Jws2Instance>();
    for (Jws2Instance svc : disc.getServices())
    {
      if (svc.getServiceTypeURI().toLowerCase(Locale.ROOT)
              .contains("iupredws"))
      {
        iupreds.add(svc);
      }
    }
    assertTrue("Couldn't discover any IUPred services to use to test.",
            iupreds.size() > 0);
    jalview.io.FileLoader fl = new jalview.io.FileLoader(false);
    af = fl.LoadFileWaitTillLoaded(testseqs,
            jalview.io.DataSourceType.FILE);
    assertNotNull("Couldn't load test data ('" + testseqs + "')", af);
  }

  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    if (af != null)
    {
      af.setVisible(false);
      af.dispose();
      af = null;
    }
  }

  /**
   * test for patches to JAL-1294
   */
  @Test(groups = { "External", "Network" })
  public void testDisorderAnnotExport()
  {
    disorderClient = new AADisorderClient(iupreds.get(0), af, null, null);
    af.getViewport().getCalcManager().startWorker(disorderClient);
    do
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {
      }
      ;
    } while (af.getViewport().getCalcManager().isWorking());
    AlignmentI orig_alig = af.getViewport().getAlignment();
    // NOTE: Consensus annotation row cannot be exported and reimported
    // faithfully - so we remove them
    List<AlignmentAnnotation> toremove = new ArrayList<AlignmentAnnotation>();
    for (AlignmentAnnotation aa : orig_alig.getAlignmentAnnotation())
    {
      if (aa.autoCalculated)
      {
        toremove.add(aa);
      }
    }
    for (AlignmentAnnotation aa : toremove)
    {
      orig_alig.deleteAnnotation(aa);
    }
    checkAnnotationFileIO("Testing IUPred Annotation IO", orig_alig);

  }

  static void checkAnnotationFileIO(String testname, AlignmentI al)
  {
    try
    {
      String aligfileout = FileFormat.Pfam.getWriter(al)
              .print(al.getSequencesArray(), true);
      String anfileout = new AnnotationFile()
              .printAnnotationsForAlignment(al);
      assertTrue("Test " + testname
              + "\nAlignment annotation file was not regenerated. Null string",
              anfileout != null);
      assertTrue("Test " + testname
              + "\nAlignment annotation file was not regenerated. Empty string",
              anfileout.length() > "JALVIEW_ANNOTATION".length());

      System.out.println(
              "Output annotation file:\n" + anfileout + "\n<<EOF\n");

      AlignmentI al_new = new FormatAdapter().readFile(aligfileout,
              DataSourceType.PASTE, FileFormat.Pfam);
      assertTrue("Test " + testname
              + "\nregenerated annotation file did not annotate alignment.",
              new AnnotationFile().readAnnotationFile(al_new, anfileout,
                      DataSourceType.PASTE));

      // test for consistency in io
      StockholmFileTest.testAlignmentEquivalence(al, al_new, true, false,
              false);
      return;
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    Assert.fail("Test " + testname
            + "\nCouldn't complete Annotation file roundtrip input/output/input test.");
  }

}
