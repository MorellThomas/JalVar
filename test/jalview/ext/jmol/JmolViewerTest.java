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
package jalview.ext.jmol;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.Preferences;
import jalview.gui.StructureViewer;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileLoader;

@Test(singleThreaded = true)
public class JmolViewerTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Jalview.main(
            new String[]
            { "-noquestionnaire", "-nonews", "-props",
                "test/jalview/ext/rbvi/chimera/testProps.jvprops" });
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @Test(groups = { "Functional" })
  public void testSingleSeqViewJMol()
  {
    Cache.setProperty(Preferences.STRUCTURE_DISPLAY,
            ViewerType.JMOL.name());
    String inFile = "examples/1gaq.txt";
    AlignFrame af = new jalview.io.FileLoader()
            .LoadFileWaitTillLoaded(inFile, DataSourceType.FILE);
    assertTrue("Didn't read input file " + inFile, af != null);
    for (SequenceI sq : af.getViewport().getAlignment().getSequences())
    {
      SequenceI dsq = sq.getDatasetSequence();
      while (dsq.getDatasetSequence() != null)
      {
        dsq = dsq.getDatasetSequence();
      }
      if (dsq.getAllPDBEntries() != null
              && dsq.getAllPDBEntries().size() > 0)
      {
        for (int q = 0; q < dsq.getAllPDBEntries().size(); q++)
        {
          final StructureViewer structureViewer = new StructureViewer(
                  af.getViewport().getStructureSelectionManager());
          structureViewer.setViewerType(ViewerType.JMOL);
          JalviewStructureDisplayI jmolViewer = structureViewer
                  .viewStructures(dsq.getAllPDBEntries().elementAt(q),
                          new SequenceI[]
                          { sq }, af.getCurrentView().getAlignPanel());
          /*
           * Wait for viewer load thread to complete
           */
          try
          {
            while (!jmolViewer.getBinding().isFinishedInit())
            {
              Thread.sleep(500);
            }
          } catch (InterruptedException e)
          {
          }

          jmolViewer.closeViewer(true);
          // todo: break here means only once through this loop?
          break;
        }
        break;
      }
    }
  }

  @Test(groups = { "Functional" })
  public void testAddStrToSingleSeqViewJMol()
          throws InvocationTargetException, InterruptedException
  {
    Cache.setProperty(Preferences.STRUCTURE_DISPLAY,
            ViewerType.JMOL.name());
    String inFile = "examples/1gaq.txt";
    AlignFrame af = new jalview.io.FileLoader(true)
            .LoadFileWaitTillLoaded(inFile, DataSourceType.FILE);
    assertTrue("Didn't read input file " + inFile, af != null);
    // show a structure for 4th Sequence
    SequenceI sq1 = af.getViewport().getAlignment().getSequences().get(0);
    final StructureViewer structureViewer = new StructureViewer(
            af.getViewport().getStructureSelectionManager());
    structureViewer.setViewerType(ViewerType.JMOL);
    JalviewStructureDisplayI jmolViewer = structureViewer.viewStructures(
            sq1.getDatasetSequence().getAllPDBEntries().elementAt(0),
            new SequenceI[]
            { sq1 }, af.getCurrentView().getAlignPanel());
    /*
     * Wait for viewer load thread to complete
     */
    try
    {
      while (!jmolViewer.getBinding().isFinishedInit())
      {
        Thread.sleep(500);
      }
    } catch (InterruptedException e)
    {
    }

    assertTrue(jmolViewer.isVisible());

    // add another pdb file and add it to view
    final String _inFile = "examples/3W5V.pdb";
    inFile = _inFile;
    FileLoader fl = new FileLoader();
    fl.LoadFile(af.getCurrentView(), _inFile, DataSourceType.FILE,
            FileFormat.PDB);
    try
    {
      int time = 0;
      do
      {
        Thread.sleep(50); // hope we can avoid race condition

      } while (++time < 30
              && af.getViewport().getAlignment().getHeight() == 3);
    } catch (Exception q)
    {
    }
    ;
    assertTrue("Didn't paste additional structure" + inFile,
            af.getViewport().getAlignment().getHeight() > 3);
    SequenceI sq2 = af.getViewport().getAlignment().getSequenceAt(3);
    PDBEntry pdbe = sq2.getDatasetSequence().getAllPDBEntries().get(0);
    assertTrue(pdbe.getFile().contains(inFile));
    structureViewer.viewStructures(pdbe, new SequenceI[] { sq2 },
            af.alignPanel);
    /*
     * Wait for viewer load thread to complete
     */
    try
    {
      while (structureViewer.isBusy())
      {
        Thread.sleep(500);
      }
    } catch (InterruptedException e)
    {
    }
    assertEquals(jmolViewer.getBinding().getPdbCount(), 2);
    String mouseOverTest = "[GLY]293:A.CA/2.1 #2164";
    ((JalviewJmolBinding) jmolViewer.getBinding()).mouseOverStructure(2164,
            mouseOverTest);
    SearchResultsI highlight = af.alignPanel.getSeqPanel()
            .getLastSearchResults();
    assertNotNull("Didn't find highlight from second structure mouseover",
            highlight.getResults(sq2, sq2.getStart(), sq2.getEnd()));
  }

}
