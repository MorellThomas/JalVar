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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.analysis.CrossRef;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentTest;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.CrossRefAction;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.SequenceFetcher;
import jalview.project.Jalview2XML;
import jalview.util.DBRefUtils;
import junit.extensions.PA;

@Test(singleThreaded = true)
public class CrossRef2xmlTests extends Jalview2xmlBase
{

  @Override
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" }, enabled = true)
  public void openCrossrefsForEnsemblTwice()
  {
    AlignFrame af = new FileLoader(false).LoadFileWaitTillLoaded(
            "examples/testdata/CantShowEnsemblCrossrefsTwice.jvp",
            DataSourceType.FILE);
    assertNotNull(af, "Couldn't load test's project.");
    AlignmentI origAlig = af.getViewport().getAlignment();
    List<String> source = new CrossRef(origAlig.getSequencesArray(),
            origAlig.getDataset()).findXrefSourcesForSequences(true);
    assertEquals(source.size(), 1, "Expected just one crossref to show.");
    List<AlignmentViewPanel> views;
    {
      // try to show once - in a code block so handler is forgotten about
      CrossRefAction xref1 = CrossRefAction.getHandlerFor(
              origAlig.getSequencesArray(), true, source.get(0), af);
      try
      {
        xref1.run();
        views = (List<AlignmentViewPanel>) PA.getValue(xref1, "xrefViews");
        assertTrue(views.size() > 0,
                "Couldn't get cross ref on first attempt (SERIOUS FAIL).");
      } catch (Exception ex)
      {
        Assert.fail("Unexpected Exception for first xref action", ex);
      }
    }

    views = null;
    // now just try it again
    CrossRefAction xref2 = CrossRefAction.getHandlerFor(
            origAlig.getSequencesArray(), true, source.get(0), af);
    try
    {
      xref2.run();
      views = (List<AlignmentViewPanel>) PA.getValue(xref2, "xrefViews");
      assertTrue(views.size() > 0,
              "Couldn't get cross ref on second attempt (SERIOUS FAIL).");
    } catch (Exception ex)
    {
      Assert.fail("Unexpected Exception for second xref action", ex);
    }
    // TODO : check that both views contain the same data
  }

  @DataProvider(name = "initialAccessions")
  static Object[][] getAccessions()
  {
    return new String[][] { { "UNIPROT", "P00338" },
        { "UNIPROT", "Q8Z9G6" },
        { "ENSEMBLGENOMES", "CAD01290" } };
  }

  /**
   * test store and recovery of all reachable cross refs from all reachable
   * crossrefs for one or more fetched db refs. Currently, this test has a known
   * failure case.
   * 
   * @throws Exception
   */
  @Test(
    groups =
    { "Operational" },
    dataProvider = "initialAccessions",
    enabled = true)
  public void testRetrieveAndShowCrossref(String forSource,
          String forAccession) throws Exception
  {

    List<String> failedDBRetr = new ArrayList<>();
    List<String> failedXrefMenuItems = new ArrayList<>();
    List<String> failedProjectRecoveries = new ArrayList<>();
    // only search for ensembl or Uniprot crossrefs
    List<String> limit = Arrays
            .asList(new String[]
            { DBRefUtils.getCanonicalName("ENSEMBL"),
                DBRefUtils.getCanonicalName("Uniprot") });
    // for every set of db queries
    // retrieve db query
    // verify presence of expected xrefs
    // show xrefs - verify expected type of frame is shown for each xref
    // show xrefs again
    // - verify original -> xref -> xref(original) recovers frame containing at
    // least the first retrieved sequence
    // store
    // 1. whole project
    // 2. individual frames
    // 3. load each one back and verify
    // . aligned sequences (.toString() )
    // . xrefs (.toString() )
    // . codonframes
    //
    //
    Map<String, String> dbtoviewBit = new HashMap<>();
    List<String> keyseq = new ArrayList<>();
    Map<String, File> savedProjects = new HashMap<>();

    // for (String[] did : new String[][] { { "UNIPROT", "P00338" } })
    // {
    // pass counters - 0 - first pass, 1 means retrieve project rather than
    // perform action
    int pass1 = 0, pass2 = 0, pass3 = 0;
    // each do loop performs two iterations in the first outer loop pass, but
    // only performs one iteration on the second outer loop
    // ie. pass 1 = 0 {pass 2= 0 { pass 3 = 0,1 }, pass 2=1 { pass 3 = 0 }}, 1
    // { pass 2 = 0 { pass 3 = 0 } }
    do
    {
      String first = forSource + " " + forAccession;// did[0] + " " + did[1];
      AlignFrame af = null;
      boolean dna;
      AlignmentI retral;
      AlignmentI dataset;
      SequenceI[] seqs;
      List<String> ptypes = null;
      if (pass1 == 0)
      {
        // retrieve dbref

        SequenceFetcher sf = new SequenceFetcher(Desktop.instance,
                forSource, forAccession);
        sf.run();
        AlignFrame[] afs = Desktop.getAlignFrames();
        if (afs.length == 0)
        {
          failedDBRetr.add("Didn't retrieve " + first);
          break;
        }
        keyseq.add(first);
        af = afs[0];

        // verify references for retrieved data
        AlignmentTest.assertAlignmentDatasetRefs(
                af.getViewport().getAlignment(), "Pass (" + pass1 + ","
                        + pass2 + "," + pass3 + "): Fetch " + first + ":");
        assertDatasetIsNormalisedKnownDefect(
                af.getViewport().getAlignment(), "Pass (" + pass1 + ","
                        + pass2 + "," + pass3 + "): Fetch " + first + ":");
        dna = af.getViewport().getAlignment().isNucleotide();
        retral = af.getViewport().getAlignment();
        dataset = retral.getDataset();
        seqs = retral.getSequencesArray();

      }
      else
      {
        if (Desktop.instance != null)
          Desktop.instance.closeAll_actionPerformed(null);
        // recover stored project
        af = new FileLoader(false).LoadFileWaitTillLoaded(
                savedProjects.get(first).toString(), DataSourceType.FILE);
        System.out.println("Recovered view for '" + first + "' from '"
                + savedProjects.get(first).toString() + "'");
        dna = af.getViewport().getAlignment().isNucleotide();
        retral = af.getViewport().getAlignment();
        dataset = retral.getDataset();
        seqs = retral.getSequencesArray();

        // verify references for recovered data
        AlignmentTest.assertAlignmentDatasetRefs(
                af.getViewport().getAlignment(),
                "Pass (" + pass1 + "," + pass2 + "," + pass3 + "): Recover "
                        + first + ":");
        assertDatasetIsNormalisedKnownDefect(
                af.getViewport().getAlignment(),
                "Pass (" + pass1 + "," + pass2 + "," + pass3 + "): Recover "
                        + first + ":");

      }

      // store project on first pass, compare next pass
      stringify(dbtoviewBit, savedProjects, first, af.alignPanel);

      ptypes = (seqs == null || seqs.length == 0) ? null
              : new CrossRef(seqs, dataset)
                      .findXrefSourcesForSequences(dna);
      filterDbRefs(ptypes, limit);

      // start of pass2: retrieve each cross-ref for fetched or restored
      // project.
      do // first cross ref and recover crossref loop
      {

        for (String db : ptypes)
        {
          // counter for splitframe views retrieved via crossref
          int firstcr_ap = 0;
          // build next key so we an retrieve all views
          String nextxref = first + " -> " + db + "{" + firstcr_ap + "}";
          // perform crossref action, or retrieve stored project
          List<AlignmentViewPanel> cra_views = new ArrayList<>();
          CrossRefAction cra = null;

          if (pass2 == 0)
          { // retrieve and show cross-refs in this thread
            cra = CrossRefAction.getHandlerFor(seqs, dna, db, af);
            cra.run();
            cra_views = (List<AlignmentViewPanel>) PA.getValue(cra,
                    "xrefViews");
            if (cra_views.size() == 0)
            {
              failedXrefMenuItems.add(
                      "No crossrefs retrieved for " + first + " -> " + db);
              continue;
            }
            assertNucleotide(cra_views.get(0),
                    "Nucleotide panel included proteins for " + first
                            + " -> " + db);
            assertProtein(cra_views.get(1),
                    "Protein panel included nucleotides for " + first
                            + " -> " + db);
          }
          else
          {
            if (Desktop.instance != null)
              Desktop.instance.closeAll_actionPerformed(null);
            pass3 = 0;
            // recover stored project
            File storedProject = savedProjects.get(nextxref);
            if (storedProject == null)
            {
              failedProjectRecoveries
                      .add("Failed to store a view for '" + nextxref + "'");
              continue;
            }

            // recover stored project
            AlignFrame af2 = new FileLoader(false).LoadFileWaitTillLoaded(
                    savedProjects.get(nextxref).toString(),
                    DataSourceType.FILE);
            System.out
                    .println("Recovered view for '" + nextxref + "' from '"
                            + savedProjects.get(nextxref).toString() + "'");
            // gymnastics to recover the alignPanel/Complementary alignPanel
            if (af2.getViewport().isNucleotide())
            {
              // top view, then bottom
              cra_views.add(af2.getViewport().getAlignPanel());
              cra_views.add(((jalview.gui.AlignViewport) af2.getViewport()
                      .getCodingComplement()).getAlignPanel());

            }
            else
            {
              // bottom view, then top
              cra_views.add(((jalview.gui.AlignViewport) af2.getViewport()
                      .getCodingComplement()).getAlignPanel());
              cra_views.add(af2.getViewport().getAlignPanel());

            }
          }
          HashMap<String, List<String>> xrptypes = new HashMap<>();
          // first save/verify views.
          for (AlignmentViewPanel avp : cra_views)
          {
            nextxref = first + " -> " + db + "{" + firstcr_ap++ + "}";
            // verify references for this panel
            AlignmentTest.assertAlignmentDatasetRefs(avp.getAlignment(),
                    "Pass (" + pass1 + "," + pass2 + "," + pass3
                            + "): before start of pass3: " + nextxref
                            + ":");
            assertDatasetIsNormalisedKnownDefect(avp.getAlignment(),
                    "Pass (" + pass1 + "," + pass2 + "," + pass3
                            + "): before start of pass3: " + nextxref
                            + ":");

            SequenceI[] xrseqs = avp.getAlignment().getSequencesArray();

            List<String> _xrptypes = (seqs == null || seqs.length == 0)
                    ? null
                    : new CrossRef(xrseqs, dataset)
                            .findXrefSourcesForSequences(
                                    avp.getAlignViewport().isNucleotide());

            stringify(dbtoviewBit, savedProjects, nextxref, avp);
            xrptypes.put(nextxref, _xrptypes);

          }

          // now do the second xref pass starting from either saved or just
          // recovered split pane, in sequence
          do // retrieve second set of cross refs or recover and verify
          {
            firstcr_ap = 0;
            for (AlignmentViewPanel avp : cra_views)
            {
              nextxref = first + " -> " + db + "{" + firstcr_ap++ + "}";
              for (String xrefdb : xrptypes.get(nextxref))
              {
                List<AlignmentViewPanel> cra_views2 = new ArrayList<>();
                int q = 0;
                String nextnextxref = nextxref + " -> " + xrefdb + "{" + q
                        + "}";

                if (pass3 == 0)
                {
                  SequenceI[] xrseqs = avp.getAlignment()
                          .getSequencesArray();
                  AlignFrame nextaf = Desktop
                          .getAlignFrameFor(avp.getAlignViewport());

                  cra = CrossRefAction.getHandlerFor(xrseqs,
                          avp.getAlignViewport().isNucleotide(), xrefdb,
                          nextaf);
                  cra.run();
                  cra_views2 = (List<AlignmentViewPanel>) PA.getValue(cra,
                          "xrefViews");
                  if (cra_views2.size() == 0)
                  {
                    failedXrefMenuItems.add("No crossrefs retrieved for '"
                            + nextxref + "' to " + xrefdb + " via '"
                            + nextaf.getTitle() + "'");
                    continue;
                  }
                  assertNucleotide(cra_views2.get(0),
                          "Nucleotide panel included proteins for '"
                                  + nextxref + "' to " + xrefdb + " via '"
                                  + nextaf.getTitle() + "'");
                  assertProtein(cra_views2.get(1),
                          "Protein panel included nucleotides for '"
                                  + nextxref + "' to " + xrefdb + " via '"
                                  + nextaf.getTitle() + "'");

                }
                else
                {
                  if (Desktop.instance != null)
                    Desktop.instance.closeAll_actionPerformed(null);
                  // recover stored project
                  File storedProject = savedProjects.get(nextnextxref);
                  if (storedProject == null)
                  {
                    failedProjectRecoveries
                            .add("Failed to store a view for '"
                                    + nextnextxref + "'");
                    continue;
                  }
                  AlignFrame af2 = new FileLoader(false)
                          .LoadFileWaitTillLoaded(savedProjects
                                  .get(nextnextxref).toString(),
                                  DataSourceType.FILE);
                  System.out
                          .println("Recovered view for '" + nextnextxref
                                  + "' from '" + savedProjects
                                          .get(nextnextxref).toString()
                                  + "'");
                  // gymnastics to recover the alignPanel/Complementary
                  // alignPanel
                  if (af2.getViewport().isNucleotide())
                  {
                    // top view, then bottom
                    cra_views2.add(af2.getViewport().getAlignPanel());
                    cra_views2.add(((jalview.gui.AlignViewport) af2
                            .getViewport().getCodingComplement())
                                    .getAlignPanel());

                  }
                  else
                  {
                    // bottom view, then top
                    cra_views2.add(((jalview.gui.AlignViewport) af2
                            .getViewport().getCodingComplement())
                                    .getAlignPanel());
                    cra_views2.add(af2.getViewport().getAlignPanel());
                  }
                  Assert.assertEquals(cra_views2.size(), 2);
                  Assert.assertNotNull(cra_views2.get(0));
                  Assert.assertNotNull(cra_views2.get(1));
                }

                for (AlignmentViewPanel nextavp : cra_views2)
                {
                  nextnextxref = nextxref + " -> " + xrefdb + "{" + q++
                          + "}";

                  // verify references for this panel
                  AlignmentTest.assertAlignmentDatasetRefs(
                          nextavp.getAlignment(),
                          "" + "Pass (" + pass1 + "," + pass2 + "): For "
                                  + nextnextxref + ":");
                  assertDatasetIsNormalisedKnownDefect(
                          nextavp.getAlignment(),
                          "" + "Pass (" + pass1 + "," + pass2 + "): For "
                                  + nextnextxref + ":");

                  stringify(dbtoviewBit, savedProjects, nextnextxref,
                          nextavp);
                  keyseq.add(nextnextxref);
                }
              } // end of loop around showing all xrefdb for crossrf2

            } // end of loop around all viewpanels from crossrf1
          } while (pass2 == 2 && pass3++ < 2);
          // fetchdb->crossref1->crossref-2->verify for xrefs we
          // either loop twice when pass2=0, or just once when pass2=1
          // (recovered project from previous crossref)

        } // end of loop over db-xrefs for crossref-2

        // fetchdb-->crossref1
        // for each xref we try to retrieve xref, store and verify when
        // pass1=0, or just retrieve and verify when pass1=1
      } while (pass1 == 1 && pass2++ < 2);
      // fetchdb
      // for each ref we
      // loop twice: first, do the retrieve, second recover from saved project

      // increment pass counters, so we repeat traversal starting from the
      // oldest saved project first.
      if (pass1 == 0)
      {
        // verify stored projects for first set of cross references
        pass1 = 1;
        // and verify cross-references retrieved from stored projects
        pass2 = 0;
        pass3 = 0;
      }
      else
      {
        pass1++;
      }
    } while (pass1 < 3);

    if (failedXrefMenuItems.size() > 0)
    {
      for (String s : failedXrefMenuItems)
      {
        System.err.println(s);
      }
      Assert.fail("Faulty xref menu (" + failedXrefMenuItems.size()
              + " counts)");
    }
    if (failedProjectRecoveries.size() > 0)
    {

      for (String s : failedProjectRecoveries)
      {
        System.err.println(s);
      }
      Assert.fail(
              "Didn't recover projects for some retrievals (did they retrieve ?) ("
                      + failedProjectRecoveries.size() + " counts)");
    }
    if (failedDBRetr.size() > 0)
    {
      for (String s : failedProjectRecoveries)
      {
        System.err.println(s);
      }
      Assert.fail("Didn't retrieve some db refs for checking cross-refs ("
              + failedDBRetr.size() + " counts)");
    }
  }

  private void filterDbRefs(List<String> ptypes, List<String> limit)
  {
    if (limit != null)
    {
      int p = 0;
      while (ptypes.size() > p)
      {
        if (!limit.contains(ptypes.get(p)))
        {
          ptypes.remove(p);
        }
        else
        {
          p++;
        }
      }
    }
  }

  /**
   * wrapper to trap known defect for AH002001 testcase
   * 
   * @param alignment
   * @param string
   */
  private void assertDatasetIsNormalisedKnownDefect(AlignmentI al,
          String message)
  {
    try
    {
      AlignmentTest.assertDatasetIsNormalised(al, message);
    } catch (AssertionError ae)
    {
      if (!ae.getMessage().endsWith("EMBL|AH002001"))
      {
        throw ae;
      }
      else
      {
        System.out.println("Ignored exception for known defect: JAL-2179 : "
                + message);
      }

    }
  }

  private void assertProtein(AlignmentViewPanel alignmentViewPanel,
          String message)
  {
    assertType(true, alignmentViewPanel, message);
  }

  private void assertNucleotide(AlignmentViewPanel alignmentViewPanel,
          String message)
  {
    assertType(false, alignmentViewPanel, message);
  }

  private void assertType(boolean expectProtein,
          AlignmentViewPanel alignmentViewPanel, String message)
  {
    List<SequenceI> nonType = new ArrayList<>();
    for (SequenceI sq : alignmentViewPanel.getAlignViewport().getAlignment()
            .getSequences())
    {
      if (sq.isProtein() != expectProtein)
      {
        nonType.add(sq);
      }
    }
    if (nonType.size() > 0)
    {
      Assert.fail(message + " [ "
              + (expectProtein ? "nucleotides were " : "proteins were ")
              + nonType.toString() + " ]");
    }
  }

  /**
   * first time called, record strings derived from alignment and
   * alignedcodonframes, and save view to a project file. Second time called,
   * compare strings to existing ones. org.testng.Assert.assertTrue on
   * stringmatch
   * 
   * @param dbtoviewBit
   *          map between xrefpath and view string
   * @param savedProjects
   *          - map from xrefpath to saved project filename (createTempFile)
   * @param xrefpath
   *          - xrefpath - unique ID for this context (composed of sequence of
   *          db-fetch/cross-ref actions preceeding state)
   * @param avp
   *          - viewpanel to store (for viewpanels in splitframe, the same
   *          project should be written for both panels, only one needs
   *          recovering for comparison on the next stringify call, but each
   *          viewpanel needs to be called with a distinct xrefpath to ensure
   *          each one's strings are compared)
   */
  private void stringify(Map<String, String> dbtoviewBit,
          Map<String, File> savedProjects, String xrefpath,
          AlignmentViewPanel avp)
  {
    if (savedProjects != null)
    {
      if (savedProjects.get(xrefpath) == null)
      {
        // write a project file for this view. On the second pass, this will be
        // recovered and cross-references verified
        try
        {
          File prfile = File.createTempFile("crossRefTest", ".jvp");
          AlignFrame af = Desktop.getAlignFrameFor(avp.getAlignViewport());
          new Jalview2XML(false).saveAlignment(af, prfile.toString(),
                  af.getTitle());
          System.out.println("Written view from '" + xrefpath + "' as '"
                  + prfile.getAbsolutePath() + "'");
          savedProjects.put(xrefpath, prfile);
        } catch (IOException q)
        {
          Assert.fail("Unexpected IO Exception", q);
        }
      }
      else
      {
        System.out.println("Stringify check on view from '" + xrefpath
                + "' [ possibly retrieved from '"
                + savedProjects.get(xrefpath).getAbsolutePath() + "' ]");

      }
    }

    StringBuilder sbr = new StringBuilder();
    sbr.append(avp.getAlignment().toString());
    sbr.append("\n");
    sbr.append("<End of alignment>");
    sbr.append("\n");
    sbr.append(avp.getAlignment().getDataset());
    sbr.append("\n");
    sbr.append("<End of dataset>");
    sbr.append("\n");
    int p = 0;
    if (avp.getAlignment().getCodonFrames() != null)
    {
      for (AlignedCodonFrame ac : avp.getAlignment().getCodonFrames())
      {
        sbr.append("<AlignedCodonFrame " + p++ + ">");
        sbr.append("\n");
        sbr.append(ac.toString());
        sbr.append("\n");
      }
    }
    String dbt = dbtoviewBit.get(xrefpath);
    if (dbt == null)
    {
      dbtoviewBit.put(xrefpath, sbr.toString());
    }
    else
    {
      Assert.assertEquals(sbr.toString(), dbt,
              "stringify mismatch for " + xrefpath);
    }
  }
}
