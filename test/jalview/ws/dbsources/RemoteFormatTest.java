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
package jalview.ws.dbsources;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import jalview.analysis.AlignSeq;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.ext.ensembl.EnsemblGenomes;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.service.uniprot.UniProtFTSRestClient;
import jalview.ws.SequenceFetcher;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A class to verify that remotely fetched data has an expected format and can
 * be successfully processed by Jalview. This is intended as a first line of
 * defence and early warning of service affecting changes to data fetched
 * externally.
 * <p>
 * This is class is not intended to cover remote services e.g. alignment. Nor
 * should it duplicate tests already provided by other classes (such as
 * PDBFTSRestClientTest). Or maybe we will relocate those tests here...
 */
public class RemoteFormatTest
{
  SequenceFetcher sf;

  @BeforeTest(alwaysRun = true)
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

  @DataProvider(name = "AccessionData")
  protected Object[][] getAccessions()
  {
    return new Object[][] { { DBRefSource.UNIPROT, "P30419" },
        { DBRefSource.PDB, "1QIP" },
        { DBRefSource.EMBL, "X53828" },
        { DBRefSource.EMBLCDS, "CAA37824" },
        { DBRefSource.ENSEMBL, "ENSG00000157764" },
        { new EnsemblGenomes().getDbSource(), "DDB_G0283883" },
        { new PfamFull().getDbSource(), "PF03760" },
        { new PfamSeed().getDbSource(), "PF03760" },
        { new RfamSeed().getDbSource(), "RF00014" } };
  }

  @Test(groups = "Network", dataProvider = "AccessionData")
  public void testFetchAccession(String dbSource, String accessionId)
          throws Exception
  {
    System.out.println("Fetching " + accessionId + " from " + dbSource);
    List<DbSourceProxy> sps = sf.getSourceProxy(dbSource);
    assertFalse(sps.isEmpty());
    AlignmentI al = sps.get(0).getSequenceRecords(accessionId);
    assertNotNull(al);
    assertTrue(al.getHeight() > 0);
    SequenceI sq = al.getSequenceAt(0);
    // suppress this check as only Uniprot and PDB acquire PDB refs
    // assertTrue(sq.getAllPDBEntries().size() > 0, "No PDBEntry on sequence.");
    assertTrue(sq.getDBRefs().size() > 0, "No DBRef on sequence.");
    // suppress this test as only certain databases provide 'primary' dbrefs
    // assertFalse(sq.getPrimaryDBRefs().isEmpty());
    int length = AlignSeq.extractGaps("-. ", sq.getSequenceAsString())
            .length();
    assertEquals(sq.getEnd() - sq.getStart() + 1, length,
            "Sequence start/end doesn't match number of residues in sequence");
  }

  @Test(groups = { "Network" })
  public void testUniprotFreeTextSearch() throws Exception
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<>();
    FTSRestClientI client = UniProtFTSRestClient.getInstance();
    wantedFields.add(client.getDataColumnByNameOrCode("id"));
    wantedFields.add(client.getDataColumnByNameOrCode("entry name"));
    wantedFields.add(client.getDataColumnByNameOrCode("organism"));
    wantedFields.add(client.getDataColumnByNameOrCode("reviewed")); // Status
    wantedFields.add(client.getDataColumnByNameOrCode("length"));

    FTSRestRequest request = new FTSRestRequest();
    request.setAllowEmptySeq(false);
    request.setResponseSize(100);
    request.setFieldToSearchBy("Search All");
    request.setSearchTerm("metanephrops"); // lobster!
    request.setWantedFields(wantedFields);

    FTSRestResponse response;
    response = client.executeRequest(request);
    assertTrue(response.getNumberOfItemsFound() > 20);
    assertTrue(response.getSearchSummary() != null);
    assertTrue(response.getSearchSummary().size() > 20);
    // verify we successfully filtered out the header row (JAL-2485)
    FTSData header = response.getSearchSummary().iterator().next();
    assertFalse(
            header.getSummaryData()[0].toString().equalsIgnoreCase("Entry"),
            "Failed to filter out summary header row");
  }
}
