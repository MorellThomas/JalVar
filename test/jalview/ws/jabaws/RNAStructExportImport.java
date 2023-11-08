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
import jalview.project.Jalview2XML;
import jalview.ws.jws2.Jws2Discoverer;
import jalview.ws.jws2.RNAalifoldClient;
import jalview.ws.jws2.SequenceAnnotationWSClient;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.AutoCalcSetting;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import compbio.metadata.Argument;
import compbio.metadata.WrongParameterException;

/*
 * All methods in this class are set to the Network group because setUpBeforeClass will fail
 * if there is no network.
 */
@Test(singleThreaded = true)
public class RNAStructExportImport
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private static final String JAR_FILE_NAME = "testRnalifold_param.jar";

  public static String testseqs = "examples/RF00031_folded.stk";

  public static Jws2Discoverer disc;

  public static Jws2Instance rnaalifoldws;

  jalview.ws.jws2.RNAalifoldClient alifoldClient;

  public static jalview.gui.AlignFrame af = null;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Console.initLogger();
    disc = JalviewJabawsTestUtils.getJabawsDiscoverer(false);

    while (disc.isRunning())
    {
      // don't get services until discoverer has finished
      Thread.sleep(100);
    }

    for (Jws2Instance svc : disc.getServices())
    {

      if (svc.getServiceTypeURI().toLowerCase(Locale.ROOT)
              .contains("rnaalifoldws"))
      {
        rnaalifoldws = svc;
      }
    }

    System.out.println("State of rnaalifoldws: " + rnaalifoldws);

    if (rnaalifoldws == null)
    {
      Assert.fail("no web service");
    }

    jalview.io.FileLoader fl = new jalview.io.FileLoader(false);

    af = fl.LoadFileWaitTillLoaded(testseqs,
            jalview.io.DataSourceType.FILE);

    assertNotNull("Couldn't load test data ('" + testseqs + "')", af);

    // remove any existing annotation
    List<AlignmentAnnotation> aal = new ArrayList<>();
    for (AlignmentAnnotation rna : af.getViewport().getAlignment()
            .getAlignmentAnnotation())
    {
      if (rna.isRNA())
      {
        aal.add(rna);
      }
    }
    for (AlignmentAnnotation rna : aal)
    {
      af.getViewport().getAlignment().deleteAnnotation(rna);
    }
    af.getViewport().alignmentChanged(af.alignPanel); // why is af.alignPanel
                                                      // public?
  }

  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    if (af != null)
    {
      af.setVisible(false);
      af.dispose();
      File f = new File(JAR_FILE_NAME);
      if (f.exists())
      {
        f.delete();
      }
    }
  }

  @Test(groups = { "Network" })
  public void testRNAAliFoldValidStructure()
  {

    alifoldClient = new RNAalifoldClient(rnaalifoldws, af, null, null);

    af.getViewport().getCalcManager().startWorker(alifoldClient);

    do
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {
      }
    } while (af.getViewport().getCalcManager().isWorking());

    AlignmentI orig_alig = af.getViewport().getAlignment();
    for (AlignmentAnnotation aa : orig_alig.getAlignmentAnnotation())
    {
      if (alifoldClient.involves(aa))
      {
        if (aa.isRNA())
        {
          assertTrue(
                  "Did not create valid structure from RNAALiFold prediction",
                  aa.isValidStruc());
        }
      }
    }
  }

  @Test(groups = { "Network" })
  public void testRNAStructExport()
  {

    alifoldClient = new RNAalifoldClient(rnaalifoldws, af, null, null);

    af.getViewport().getCalcManager().startWorker(alifoldClient);

    do
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {
      }
    } while (af.getViewport().getCalcManager().isWorking());

    AlignmentI orig_alig = af.getViewport().getAlignment();
    // JBPNote: this assert fails (2.10.2) because the 'Reference Positions'
    // annotation is mistakenly recognised as an RNA annotation row when read in
    // as an annotation file.
    verifyAnnotationFileIO("Testing RNAalifold Annotation IO", orig_alig);

  }

  static void verifyAnnotationFileIO(String testname, AlignmentI al)
  {
    try
    {
      // what format would be appropriate for RNAalifold annotations?
      String aligfileout = FileFormat.Pfam.getWriter(null)
              .print(al.getSequencesArray(), true);

      String anfileout = new AnnotationFile()
              .printAnnotationsForAlignment(al);
      assertNotNull("Test " + testname
              + "\nAlignment annotation file was not regenerated. Null string",
              anfileout);
      assertTrue("Test " + testname
              + "\nAlignment annotation file was not regenerated. Empty string",
              anfileout.length() > "JALVIEW_ANNOTATION".length());

      System.out.println(
              "Output annotation file:\n" + anfileout + "\n<<EOF\n");

      // again what format would be appropriate?
      AlignmentI al_new = new FormatAdapter().readFile(aligfileout,
              DataSourceType.PASTE, FileFormat.Pfam);
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
            + "\nCouldn't complete Annotation file roundtrip input/output/input test.");
  }

  @Test(groups = { "Network" })
  public void testRnaalifoldSettingsRecovery()
  {
    List<Argument> opts = new ArrayList<>();
    for (Argument rg : (List<Argument>) rnaalifoldws.getRunnerConfig()
            .getArguments())
    {
      if (rg.getDescription().contains("emperature"))
      {
        try
        {
          rg.setValue("292");
        } catch (WrongParameterException q)
        {
          Assert.fail("Couldn't set the temperature parameter "
                  + q.getStackTrace());
        }
        opts.add(rg);
      }
      if (rg.getDescription().contains("max"))
      {
        opts.add(rg);
      }
    }
    alifoldClient = new RNAalifoldClient(rnaalifoldws, af, null, opts);

    af.getViewport().getCalcManager().startWorker(alifoldClient);

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
    AutoCalcSetting oldacs = af.getViewport()
            .getCalcIdSettingsFor(alifoldClient.getCalcId());
    String oldsettings = oldacs.getWsParamFile();
    // write out parameters
    jalview.gui.AlignFrame nalf = null;
    assertTrue("Couldn't write out the Jar file", new Jalview2XML(false)
            .saveAlignment(af, JAR_FILE_NAME, "trial parameter writeout"));
    assertTrue("Couldn't read back the Jar file",
            (nalf = new Jalview2XML(false)
                    .loadJalviewAlign(JAR_FILE_NAME)) != null);
    if (nalf != null)
    {
      AutoCalcSetting acs = af.getViewport()
              .getCalcIdSettingsFor(alifoldClient.getCalcId());
      assertTrue("Calc ID settings not recovered from viewport stash",
              acs.equals(oldacs));
      assertTrue(
              "Serialised Calc ID settings not identical to those recovered from viewport stash",
              acs.getWsParamFile().equals(oldsettings));
      JMenu nmenu = new JMenu();
      new SequenceAnnotationWSClient().attachWSMenuEntry(nmenu,
              rnaalifoldws, af);
      assertTrue("Couldn't get menu entry for service",
              nmenu.getItemCount() > 0);
      for (Component itm : nmenu.getMenuComponents())
      {
        if (itm instanceof JMenuItem)
        {
          JMenuItem i = (JMenuItem) itm;
          if (i.getText().equals(
                  rnaalifoldws.getAlignAnalysisUI().getAAconToggle()))
          {
            i.doClick();
            break;
          }
        }
      }
      while (af.getViewport().isCalcInProgress())
      {
        try
        {
          Thread.sleep(200);
        } catch (Exception x)
        {
        }
        ;
      }
      AutoCalcSetting acs2 = af.getViewport()
              .getCalcIdSettingsFor(alifoldClient.getCalcId());
      assertTrue(
              "Calc ID settings after recalculation has not been recovered.",
              acs2.getWsParamFile().equals(oldsettings));
    }
  }
}
