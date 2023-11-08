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
package jalview.fts.threedbeacons;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSDataColumnI.FTSDataColumnGroupI;
import jalview.fts.core.FTSRestClient;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.service.pdb.PDBFTSRestClientTest;
import jalview.fts.service.threedbeacons.TDBeaconsFTSRestClient;
import jalview.gui.JvOptionPane;

public class TDBeaconsFTSRestClientTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private FTSRestClient ftsRestClient;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
    ftsRestClient = new FTSRestClient()
    {

      @Override
      public String getColumnDataConfigFileName()
      {
        return "/fts/tdbeacons_data_columns.txt";
      }

      @Override
      public FTSRestResponse executeRequest(FTSRestRequest ftsRequest)
              throws Exception
      {
        return null;
      }
    };
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
  }

  @Test
  public void getAllDefaulDisplayedDataColumns()
  {
    // to change when resources.tdbeacons_data_columns.txt is changed
    Assert.assertNotNull(
            ftsRestClient.getAllDefaultDisplayedFTSDataColumns());
    System.out
            .println(ftsRestClient.getAllDefaultDisplayedFTSDataColumns());
    Assert.assertTrue(!ftsRestClient.getAllDefaultDisplayedFTSDataColumns()
            .isEmpty());
    Assert.assertEquals(
            ftsRestClient.getAllDefaultDisplayedFTSDataColumns().size(),
            15);
  }

  @Test(groups = { "Functional" })
  public void getPrimaryKeyColumIndexTest()
  {
    Collection<FTSDataColumnI> wantedFields = ftsRestClient
            .getAllDefaultDisplayedFTSDataColumns();
    int foundIndex = -1;
    try
    {
      Assert.assertEquals(foundIndex, -1);
      foundIndex = ftsRestClient.getPrimaryKeyColumIndex(wantedFields,
              false);
      Assert.assertEquals(foundIndex, 12);
      foundIndex = ftsRestClient.getPrimaryKeyColumIndex(wantedFields,
              true);
      // 1+primary key index
      Assert.assertEquals(foundIndex, 13);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getDataColumnsFieldsAsCommaDelimitedString()
  {
    // to change when resources.tdbeacons_data_columns.txt is changed
    Collection<FTSDataColumnI> wantedFields = ftsRestClient
            .getAllDefaultDisplayedFTSDataColumns();
    String actual = ftsRestClient
            .getDataColumnsFieldsAsCommaDelimitedString(wantedFields);
    Assert.assertEquals(actual,
            "uniprot_start,uniprot_end,provider,model_identifier,model_category,model_title,resolution,confidence_avg_local_score,confidence_type,confidence_version,coverage,created,model_url,model_format,model_page_url");
  }

  @Test(groups = { "Functional" })
  public void getAllFTSDataColumns()
  {
    Collection<FTSDataColumnI> allFields = ftsRestClient
            .getAllFTSDataColumns();
    Assert.assertNotNull(allFields);
    // System.out.println(allFields.size());
    Assert.assertEquals(allFields.size(), 20);
  }

  @Test(groups = { "Functional" })
  public void getSearchableDataColumns()
  {
    // to change when resources.tdbeacons_data_columns.txt is changed
    Collection<FTSDataColumnI> searchableFields = ftsRestClient
            .getSearchableDataColumns();
    Assert.assertNotNull(searchableFields);
    // System.out.println(searchableFields.size());
    Assert.assertEquals(searchableFields.size(), 1); // only 1: uniprot
                                                     // accession
  }

  @Test(groups = { "Functional" })
  public void getPrimaryKeyColumn()
  {
    // to change when resources.tdbeacons_data_columns.txt is changed
    FTSDataColumnI expectedPKColumn;
    try
    {
      expectedPKColumn = ftsRestClient.getDataColumnByNameOrCode("Url");
      Assert.assertNotNull(ftsRestClient.getPrimaryKeyColumn());
      Assert.assertEquals(ftsRestClient.getPrimaryKeyColumn(),
              expectedPKColumn);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getDataColumnByNameOrCode()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("uniprot_accession");
      Assert.assertNotNull(foundDataCol);
      Assert.assertEquals(foundDataCol.getName(), "UniProt Accession");
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getDataColumnGroupById()
  {
    FTSDataColumnGroupI foundDataColGroup;
    try
    {
      foundDataColGroup = ftsRestClient.getDataColumnGroupById("g2");
      Assert.assertNotNull(foundDataColGroup);
      Assert.assertEquals(foundDataColGroup.getName(), "Quality");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Test(groups = { "Functional" })
  public void getDefaultResponsePageSize()
  {
    int defaultResSize = ftsRestClient.getDefaultResponsePageSize();
    Assert.assertEquals(defaultResSize, 100); // why 100 or 500 ? pdb is 100,
                                              // uniprot 500
  }

  @Test(groups = { "Functional" })
  public void getColumnMinWidthTest()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("uniprot_accession");
      Assert.assertNotNull(foundDataCol);
      int actualColMinWidth = foundDataCol.getMinWidth();
      Assert.assertEquals(actualColMinWidth, 50);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }
  // could add test for MaxWidth & PreferedWith

  @Test(groups = { "Functional" })
  public void getColumnClassTest()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("uniprot_accession");
      Assert.assertNotNull(foundDataCol);
      Assert.assertEquals(foundDataCol.getDataType().getDataTypeClass(),
              String.class);
      foundDataCol = ftsRestClient.getDataColumnByNameOrCode("id");
      Assert.assertNotNull(foundDataCol);
      Assert.assertEquals(foundDataCol.getDataType().getDataTypeClass(),
              String.class);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void coverageForEqualsAndHashFunction()
  {
    Set<FTSDataColumnI> uniqueSet = new HashSet<FTSDataColumnI>();
    Collection<FTSDataColumnI> searchableCols = ftsRestClient
            .getSearchableDataColumns();
    System.out.println(searchableCols);
    for (FTSDataColumnI foundCol : searchableCols)
    {
      System.out.println(foundCol.toString());
      uniqueSet.add(foundCol);
      uniqueSet.add(foundCol);
    }
    Assert.assertTrue(!uniqueSet.isEmpty());
    // Assert.assertEquals(uniqueSet.size(), 22); -> 1 or 2 currently for 3DB
  }

  @Test(groups = { "Functional" })
  public void getTDBIdColumIndexTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("Model id"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("uniprot_accession"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("Title"));
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      assertEquals(4, TDBeaconsFTSRestClient.getInstance()
              .getPrimaryKeyColumIndex(wantedFields, true));
      // assertEquals(3, TDBeaconsFTSRestClient.getInstance()
      // .getPrimaryKeyColumIndex(wantedFields, true));
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private static String[][] mocks = { { "P38398.json", null },
      { "P01308.json", null },
      { "P0DTD1.json", null },
      { "P27787.json", null }

      // , { "P0DTD3.json", "{}" } actually results in 404, but {} is in body
  };

  private static void setMockData()
  {
    try
    {
      mocks[0][1] = PDBFTSRestClientTest.readJsonStringFromFile(
              "test/jalview/fts/threedbeacons/p38398_tdb_fts_query_resp.txt");

      mocks[1][1] = PDBFTSRestClientTest.readJsonStringFromFile(
              "test/jalview/fts/threedbeacons/p01308_tdb_fts_query_resp.txt");

      mocks[2][1] = PDBFTSRestClientTest.readJsonStringFromFile(
              "test/jalview/fts/threedbeacons/p0dtd1_tdb_fts_query_resp.txt");

      mocks[3][1] = PDBFTSRestClientTest.readJsonStringFromFile(
              "test/jalview/fts/threedbeacons/p27787_tdb_fts_query_resp.txt");

    } catch (IOException e)
    {
      Assert.fail("Couldn't read mock response data", e);
    }
  }

  public static void setMock()
  {
    setMockData();
    FTSRestClient.createMockFTSRestClient(
            (FTSRestClient) TDBeaconsFTSRestClient.getInstance(), mocks);
  }

  private static String dev_url = "https://wwwdev.ebi.ac.uk/pdbe/pdbe-kb/3dbeacons/api/uniprot/summary/";

  private static String prod_url = "https://www.ebi.ac.uk/pdbe/pdbe-kb/3dbeacons/api/uniprot/summary/";

  /**
   * check that the mock request and response are the same as the response from
   * a live 3D-beacons endpoint
   * 
   * Note - servers often have rapidly changing ids / URIs so this might fail,
   * but the overall structure will remain.
   * 
   * @throws Exception
   */
  @Test(groups = { "Network", "Integration" })
  public void verifyMockTDBRequest() throws Exception
  {
    setMockData();
    for (String[] otherMock : mocks)
    {
      verifyMockTDBRequest(otherMock[0], otherMock[1]);
    }
  }

  private void verifyMockTDBRequest(String mockRequest,
          String _mockResponse) throws Exception
  {
    URL tdb_req = new URL(prod_url + mockRequest);
    byte[] resp = tdb_req.openStream().readAllBytes();
    String tresp = new String(resp, StandardCharsets.UTF_8);
    // this simple test fails for responses containing multi-chain structures -
    // since chain order in the json elements is arbitrary and varies between
    // queries.
    assertEquals(_mockResponse.trim(), tresp.trim());
  }

  @Test(groups = { "Functional" })
  public void testMockTDBRequest()
  {

    setMock();
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("Model Id"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("model_url"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("provider"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("model_category"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("qmean_avg_local_score"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("uniprot_start"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("uniprot_end"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }
    System.out.println("wantedFields >>" + wantedFields);

    FTSRestRequest request = new FTSRestRequest();
    FTSRestResponse response;

    request.setResponseSize(100);
    request.setFieldToSearchBy("");
    request.setWantedFields(wantedFields);
    // check 404 behaviour
    request.setSearchTerm("P00000.json");

    try
    {
      response = TDBeaconsFTSRestClient.getInstance()
              .executeRequest(request);

      assertNull(response);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Unexpected failure during mock 3DBeacons 404 test");
    }

    // check 200 behaviour
    request.setSearchTerm("P38398.json");
    System.out.println("request : " + request.getFieldToSearchBy());
    // System.out.println(request.toString());

    try
    {
      response = TDBeaconsFTSRestClient.getInstance()
              .executeRequest(request);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Couldn't execute webservice call!");
      return;
    }
    assertTrue(response.getSearchSummary() != null);
    assertTrue(response.getNumberOfItemsFound() > 3); // 4 atm
    System.out.println("Search summary : \n" + response.getSearchSummary());

    // System.out.println(response.getSearchSummary().size());
  }

  @Test(groups = { "External", "Network" })
  public void executeRequestTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("Model Id"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("model_url"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("provider"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("model_category"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("confidence_avg_local_score"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("uniprot_start"));
      wantedFields.add(TDBeaconsFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("uniprot_end"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }
    System.out.println("wantedFields >>" + wantedFields);

    FTSRestRequest request = new FTSRestRequest();
    request.setResponseSize(100);
    request.setFieldToSearchBy("");
    request.setSearchTerm("P01318.json");
    request.setWantedFields(wantedFields);
    System.out.println("request : " + request.getFieldToSearchBy());
    // System.out.println(request.toString());

    FTSRestResponse response;
    try
    {
      response = TDBeaconsFTSRestClient.getInstance()
              .executeRequest(request);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Couldn't execute webservice call!");
      return;
    }
    assertTrue(response.getSearchSummary() != null);
    assertTrue(response.getNumberOfItemsFound() > 3); // 4 atm
    System.out.println("Search summary : \n" + response.getSearchSummary());
    // System.out.println(response.getSearchSummary().size());
  }
}
