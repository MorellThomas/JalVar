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
package jalview.ws.seqfetcher;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.analysis.CrossRef;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.FeatureProperties;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.util.DBRefUtils;
import jalview.ws.DBRefFetcher;
import jalview.ws.SequenceFetcher;
import jalview.ws.dbsources.EBIAlfaFold;
import jalview.ws.dbsources.Pdb;
import jalview.ws.dbsources.Uniprot;

/**
 * @author jimp
 * 
 */
public class DbRefFetcherTest
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
    jalview.bin.Console.initLogger();
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
  }

  @Test(groups = { "Network" })
  public void checkUniprotCanonicalFlagSet()
  {
    // TODO - mock this - for moment it is a live request.
    SequenceI uniprotSeq = new Sequence("FER1_SPIOL",
            "MAATTTTMMGMATTFVPKPQAPPMMAALPSNTGRSLFGLKTGSRGGRMTMAAYKVTLVTPTGNVEFQCPDDV"
                    + "YILDAAEEEGIDLPYSCRAGSCSSCAGKLKTGSLNQDDQSFLDDDQIDEGWVLTCAAYPVSDVTIETHKEEE"
                    + "LTA");
    DBRefFetcher dbr = new DBRefFetcher(new SequenceI[] { uniprotSeq });
    dbr.fetchDBRefs(true);
    List<DBRefEntry> primRefs = uniprotSeq.getPrimaryDBRefs();
    assertNotNull(primRefs);
    assertTrue(primRefs.size() > 0);
    boolean canonicalUp = false;
    for (DBRefEntry ref : primRefs)
    {
      assertEquals(DBRefSource.UNIPROT, ref.getCanonicalSourceName());
      canonicalUp |= ref.isCanonical();
    }
    assertTrue("No Canonical Uniprot reference detected", canonicalUp);
  }

  /**
   * Tests that standard protein database sources include Uniprot (as the first)
   * and also PDB. (Additional sources are dependent on availability of DAS
   * services.)
   */
  @Test(groups = { "Functional" })
  public void testStandardProtDbs()
  {
    List<String> defdb = new ArrayList<String>();
    defdb.addAll(Arrays.asList(DBRefSource.PROTEINDBS));
    defdb.add(DBRefSource.PDB);
    List<DbSourceProxy> srces = new ArrayList<DbSourceProxy>();
    SequenceFetcher sfetcher = new SequenceFetcher();
    boolean pdbFound = false;

    for (String ddb : defdb)
    {
      List<DbSourceProxy> srcesfordb = sfetcher.getSourceProxy(ddb);

      if (srcesfordb != null)
      {
        // TODO is this right? get duplicate entries
        srces.addAll(srcesfordb);
      }
    }

    int i = 0;
    int uniprotPos = -1;
    for (DbSourceProxy s : srces)
    {
      if (s instanceof Uniprot && uniprotPos == -1)
      {
        uniprotPos = i;
      }
      if (s instanceof Pdb)
      {
        pdbFound = true;
      }
      i++;
    }

    assertTrue("Failed to find Uniprot source as first source amongst "
            + srces.size() + " sources (source was at position "
            + uniprotPos + ")", uniprotPos == 0);
    assertTrue("Failed to find PDB source amongst " + srces.size()
            + " sources", pdbFound);
  }

  /**
   * Tests retrieval of one entry from EMBL. Test is dependent on availability
   * of network and the EMBL service.
   * 
   * @throws Exception
   */
  @Test(groups = { "External" })
  public void testEmblUniprotProductRecovery() throws Exception
  {
    String retrievalId = "V00488";
    DbSourceProxy embl = new SequenceFetcher()
            .getSourceProxy(DBRefSource.EMBL).get(0);
    assertNotNull("Couldn't find the EMBL retrieval client", embl);
    verifyProteinNucleotideXref(retrievalId, embl);
  }

  /**
   * Tests retrieval of one entry from EMBLCDS. Test is dependent on
   * availability of network and the EMBLCDS service.
   * 
   * @throws Exception
   */
  @Test(groups = { "External" })
  public void testEmblCDSUniprotProductRecovery() throws Exception
  {
    String retrievalId = "AAH29712";
    DbSourceProxy embl = new SequenceFetcher()
            .getSourceProxy(DBRefSource.EMBLCDS).get(0);
    assertNotNull("Couldn't find the EMBL retrieval client", embl);
    verifyProteinNucleotideXref(retrievalId, embl);
  }

  /**
   * Helper method to perform database retrieval and verification of results.
   * 
   * @param retrievalId
   * @param embl
   * @throws Exception
   */
  private void verifyProteinNucleotideXref(String retrievalId,
          DbSourceProxy embl) throws Exception
  {
    AlignmentI alsq = embl.getSequenceRecords(retrievalId);
    assertNotNull("Couldn't find the EMBL record " + retrievalId, alsq);
    assertEquals("Didn't retrieve right number of records", 1,
            alsq.getHeight());
    SequenceI seq = alsq.getSequenceAt(0);
    assertEquals("Wrong sequence name",
            embl.getDbSource() + "|" + retrievalId, seq.getName());
    List<SequenceFeature> sfs = seq.getSequenceFeatures();
    assertFalse("Sequence features missing", sfs.isEmpty());
    assertTrue("Feature not CDS", FeatureProperties
            .isCodingFeature(embl.getDbSource(), sfs.get(0).getType()));
    assertEquals(embl.getDbSource(), sfs.get(0).getFeatureGroup());
    List<DBRefEntry> dr = DBRefUtils.selectRefs(seq.getDBRefs(),
            new String[]
            { DBRefSource.UNIPROT });
    assertNotNull(dr);
    assertEquals("Expected a single Uniprot cross reference", 1, dr.size());
    assertEquals("Expected cross reference map to be one amino acid",
            dr.get(0).getMap().getMappedWidth(), 1);
    assertEquals("Expected local reference map to be 3 nucleotides",
            dr.get(0).getMap().getWidth(), 3);
    AlignmentI sprods = new CrossRef(alsq.getSequencesArray(), alsq)
            .findXrefSequences(dr.get(0).getSource(), true);
    assertNotNull(
            "Couldn't recover cross reference sequence from dataset. Was it ever added ?",
            sprods);
    assertEquals("Didn't xref right number of records", 1,
            sprods.getHeight());
    SequenceI proteinSeq = sprods.getSequenceAt(0);
    assertEquals(proteinSeq.getSequenceAsString(),
            dr.get(0).getMap().getTo().getSequenceAsString());
    assertEquals(dr.get(0).getSource() + "|" + dr.get(0).getAccessionId(),
            proteinSeq.getName());
  }

  /**
   * Tests retrieval of one entry from EMBLCDS. Test is dependent on
   * availability of network and the EMBLCDS service.
   * 
   * @throws Exception
   */
  @Test(groups = { "External" })
  public void testAlphaFoldClien() throws Exception
  {
    DbSourceProxy alphafold = new EBIAlfaFold();
    AlignmentI resp = alphafold
            .getSequenceRecords(alphafold.getTestQuery());
    assertNotNull(resp);
    assertEquals("One sequence only", resp.getHeight(), 1);
    for (AlignmentAnnotation aa : resp.getAlignmentAnnotation())
    {
      if (aa.graph == AlignmentAnnotation.CONTACT_MAP)
      {
        assertTrue("Contact map didn't provide valid contact",
                resp.getContactListFor(aa, 1).getContactAt(1) != -1d);
        // test passes
        return;
      }
    }
    Assert.fail("No pAE matrix found for alphafold structure.");
  }

}
