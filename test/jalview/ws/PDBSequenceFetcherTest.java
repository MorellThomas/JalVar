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
package jalview.ws;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.structure.StructureImportSettings;
import jalview.structure.StructureImportSettings.StructureParser;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PDBSequenceFetcherTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  SequenceFetcher sf;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    // ensure 'add annotation from structure' is selected
    Cache.applicationProperties.setProperty("STRUCT_FROM_PDB",
            Boolean.TRUE.toString());
    Cache.applicationProperties.setProperty("ADD_SS_ANN",
            Boolean.TRUE.toString());

    sf = new SequenceFetcher();
  }

  /**
   * Test that RNA structure can be added by a call to the RNAML service.
   * 
   * Note this test depends on http://arn-ibmc.in2p3.fr/api/compute/2d which is
   * not always reliable.
   * 
   * @throws Exception
   */
  @Test(groups = { "Network" }, enabled = true)
  public void testRnaSeqRetrieve() throws Exception
  {
    Cache.applicationProperties.setProperty("PDB_DOWNLOAD_FORMAT", "PDB");
    List<DbSourceProxy> sps = sf.getSourceProxy("PDB");
    AlignmentI response = sps.get(0).getSequenceRecords("2GIS");
    assertTrue(response != null);
    assertTrue(response.getHeight() == 1);
    for (SequenceI sq : response.getSequences())
    {
      assertTrue("No annotation transfered to sequence.",
              sq.getAnnotation().length > 0);
      assertTrue("No PDBEntry on sequence.",
              sq.getAllPDBEntries().size() > 0);
      assertTrue(
              "No RNA annotation on sequence, possibly http://arn-ibmc.in2p3.fr/api/compute/2d not available?",
              sq.getRNA() != null);
    }
  }

  @Test(groups = { "Network" }, enabled = true)
  public void testPdbSeqRetrieve() throws Exception
  {
    StructureImportSettings.setDefaultStructureFileFormat("PDB");
    StructureImportSettings
            .setDefaultPDBFileParser(StructureParser.JALVIEW_PARSER);

    testRetrieveProteinSeqFromPDB();
  }

  @Test(groups = { "Network" }, enabled = true)
  public void testmmCifSeqRetrieve() throws Exception
  {
    StructureImportSettings.setDefaultStructureFileFormat("mmCIF");
    testRetrieveProteinSeqFromPDB();
  }

  private class TestRetrieveObject
  {
    String id;

    int expectedHeight;

    public TestRetrieveObject(String id, int expectedHeight)
    {
      super();
      this.id = id;
      this.expectedHeight = expectedHeight;
    }

  }

  private List<TestRetrieveObject> toRetrieve = Arrays.asList(
          new TestRetrieveObject("1QIP", 4),
          new TestRetrieveObject("4IM2", 1));

  private void testRetrieveProteinSeqFromPDB() throws Exception
  {
    List<DbSourceProxy> sps = sf.getSourceProxy("PDB");
    StringBuilder errors = new StringBuilder();
    for (TestRetrieveObject str : toRetrieve)
    {
      AlignmentI response = sps.get(0).getSequenceRecords(str.id);
      assertTrue("No aligment for " + str.id, response != null);
      assertEquals(response.getHeight(), str.expectedHeight,
              "Number of chains for " + str.id);
      for (SequenceI sq : response.getSequences())
      {
        assertTrue("No annotation transfered to sequence " + sq.getName(),
                sq.getAnnotation().length > 0);
        assertTrue("No PDBEntry on sequence " + sq.getName(),
                sq.getAllPDBEntries().size() > 0);
        // FIXME: should test that all residues extracted as sequences from
        // chains in structure have a mapping to data in the structure
        List<SequenceFeature> prev = null;
        int lastp = -1;
        for (int col = 1; col <= sq.getLength(); col++)
        {
          List<SequenceFeature> sf = sq.findFeatures(col, col, "RESNUM");
          if (sf.size() != 1)
          {
            errors.append(str.id + ": "
                    + "Expected one feature at column (position): "
                    + (col - 1) + " (" + sq.findPosition(col - 1) + ")"
                    + ": saw " + sf.size());
            errors.append("\n");
            if (prev != null)
            {
              errors.append("Last Feature was at position " + lastp + ": "
                      + prev.get(0).toString());
              errors.append("\n");
            }
          }
          else
          {
            prev = sf;
            lastp = sq.findPosition(col - 1);
          }
        }
      }
    }
    if (errors.length() > 0)
    {
      Assert.fail(errors.toString());
    }
  }
}
