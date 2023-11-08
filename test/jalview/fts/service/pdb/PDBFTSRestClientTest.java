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
package jalview.fts.service.pdb;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.core.FTSRestClient;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.gui.JvOptionPane;

public class PDBFTSRestClientTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
  }

  @Test(groups = { "External", "Network" })
  public void executeRequestTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }
    System.out.println("wantedFields >>" + wantedFields);

    FTSRestRequest request = new FTSRestRequest();
    request.setAllowEmptySeq(false);
    request.setResponseSize(100);
    request.setFieldToSearchBy("text:");
    request.setSearchTerm("abc");
    request.setWantedFields(wantedFields);

    FTSRestResponse response;
    try
    {
      response = PDBFTSRestClient.getInstance().executeRequest(request);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Couldn't execute webservice call!");
      return;
    }
    assertTrue(response.getNumberOfItemsFound() > 99);
    assertTrue(response.getSearchSummary() != null);
    assertTrue(response.getSearchSummary().size() > 99);
  }

  @Test(groups = { "Functional" })
  public void getPDBDocFieldsAsCommaDelimitedStringTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    String expectedResult = "molecule_type,pdb_id,genus,gene_name,title";
    String actualResult = PDBFTSRestClient.getInstance()
            .getDataColumnsFieldsAsCommaDelimitedString(wantedFields);

    assertEquals("", expectedResult, actualResult);
  }

  @Test(groups = { "External, Network" })
  public void parsePDBJsonExceptionStringTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }

    FTSRestRequest request = new FTSRestRequest();
    request.setAllowEmptySeq(false);
    request.setResponseSize(100);
    request.setFieldToSearchBy("text:");
    request.setSearchTerm("abc");
    request.setWantedFields(wantedFields);

    String jsonErrorResponse = "";
    try
    {
      jsonErrorResponse = readJsonStringFromFile(
              "test/jalview/io/pdb_request_json_error.txt");
    } catch (IOException e)
    {
      e.printStackTrace();
    }

    String parsedErrorResponse = PDBFTSRestClient
            .parseJsonExceptionString(jsonErrorResponse);

    String expectedErrorMsg = "\n============= PDB Rest Client RunTime error =============\n"
            + "Status: 400\n"
            + "Message: org.apache.solr.search.SyntaxError: Cannot parse 'text:abc OR text:go:abc AND molecule_sequence:['' TO *]': Encountered \" \":\" \": \"\" at line 1, column 19.\n"
            + "query: text:abc OR text:go:abc AND molecule_sequence:['' TO *]\n"
            + "fl: pdb_id\n";

    assertEquals(expectedErrorMsg, parsedErrorResponse);
  }

  @Test(
    groups =
    { "External" },
    enabled = false,
    expectedExceptions = Exception.class)
  public void testForExpectedRuntimeException() throws Exception
  {
    // FIXME JBPNote: looks like this test fails for no good reason - what
    // exception was supposed to be raised ?
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    wantedFields.add(PDBFTSRestClient.getInstance()
            .getDataColumnByNameOrCode("pdb_id"));

    FTSRestRequest request = new FTSRestRequest();
    request.setFieldToSearchBy("text:");
    request.setSearchTerm("abc OR text:go:abc");
    request.setWantedFields(wantedFields);
    PDBFTSRestClient.getInstance().executeRequest(request);
  }

  // JBP: Is this actually external ? Looks like it is mocked
  // JBP looks like the mock is not up to date for this test
  @Test(groups = { "External" })
  public void parsePDBJsonResponseTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }

    FTSRestRequest request = new FTSRestRequest();
    request.setAllowEmptySeq(false);
    request.setWantedFields(wantedFields);

    String jsonString = "";
    try
    {
      jsonString = readJsonStringFromFile(
              "test/jalview/io/pdb_response_json.txt");
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    FTSRestResponse response = PDBFTSRestClient
            .parsePDBJsonResponse(jsonString, request);
    assertTrue(response.getSearchSummary() != null);
    assertTrue(response.getNumberOfItemsFound() == 931);
    assertTrue(response.getSearchSummary().size() == 14);
    System.out.println("Search summary : " + response.getSearchSummary());
  }

  @Test(groups = { "Functional" })
  public void getPDBIdColumIndexTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      assertEquals(5, PDBFTSRestClient.getInstance()
              .getPrimaryKeyColumIndex(wantedFields, true));
      assertEquals(4, PDBFTSRestClient.getInstance()
              .getPrimaryKeyColumIndex(wantedFields, false));
    } catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test(groups = { "External" })
  public void externalServiceIntegrationTest()
  {
    ClientConfig clientConfig = new DefaultClientConfig();
    Client client = Client.create(clientConfig);

    // Build request parameters for the REST Request
    WebResource webResource = client
            .resource(PDBFTSRestClient.PDB_SEARCH_ENDPOINT)
            .queryParam("wt", "json").queryParam("rows", String.valueOf(1))
            .queryParam("q", "text:abc AND molecule_sequence:['' TO *]");

    // Execute the REST request
    ClientResponse clientResponse = webResource
            .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    // Get the JSON string from the response object
    String pdbJsonResponseString = clientResponse.getEntity(String.class);

    // Check the response status and report exception if one occurs
    if (clientResponse.getStatus() != 200)
    {
      Assert.fail("Webservice call failed!!!");
    }
    else
    {
      try
      {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonParser
                .parse(pdbJsonResponseString);
        JSONObject pdbResponse = (JSONObject) jsonObj.get("response");
        String queryTime = ((JSONObject) jsonObj.get("responseHeader"))
                .get("QTime").toString();
        String numFound = pdbResponse.get("numFound").toString();
        JSONArray docs = (JSONArray) pdbResponse.get("docs");
        Iterator<JSONObject> docIter = docs.iterator();

        assertTrue("Couldn't Retrieve 'response' object",
                pdbResponse != null);
        assertTrue("Couldn't Retrieve 'QTime' value", queryTime != null);
        assertTrue("Couldn't Retrieve 'numFound' value", numFound != null);
        assertTrue("Couldn't Retrieve 'docs' object",
                docs != null || !docIter.hasNext());

        JSONObject pdbJsonDoc = docIter.next();

        for (FTSDataColumnI field : PDBFTSRestClient.getInstance()
                .getAllFTSDataColumns())
        {
          if (field.getName().equalsIgnoreCase("ALL"))
          {
            continue;
          }
          if (pdbJsonDoc.get(field.getCode()) == null)
          {
            // System.out.println(">>>\t" + field.getCode());
            assertTrue(
                    field.getCode()
                            + " has been removed from PDB doc Entity",
                    !pdbJsonResponseString.contains(field.getCode()));
          }
        }
      } catch (ParseException e)
      {
        Assert.fail(
                ">>>  Test failed due to exception while parsing pdb response json !!!");
        e.printStackTrace();
      }
    }
  }

  /**
   * reads any string from filePath
   * 
   * @param filePath
   * @return
   * @throws IOException
   */
  public static String readJsonStringFromFile(String filePath)
          throws IOException
  {
    String fileContent;
    BufferedReader br = new BufferedReader(new FileReader(filePath));
    try
    {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null)
      {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }
      fileContent = sb.toString();
    } finally
    {
      br.close();
    }
    return fileContent;
  }

  public static void setMock()
  {
    List<String[]> mocks = new ArrayList<String[]>();
    mocks.add(
            new String[]
            { "https://www.ebi.ac.uk/pdbe/search/pdb/select?wt=json&fl=pdb_id,title,experimental_method,resolution&rows=500&start=0&q=(4igk+OR+7lyb+OR+3k0h+OR+3k0k+OR+1t15+OR+3pxc+OR+3pxd+OR+3pxe+OR+1jm7+OR+7jzv+OR+3pxa+OR+3pxb+OR+1y98+OR+1n5o+OR+4ifi+OR+4y2g+OR+3k15+OR+3k16+OR+4jlu+OR+2ing+OR+4ofb+OR+6g2i+OR+3coj+OR+1jnx+OR+4y18+OR+4u4a+OR+1oqa+OR+1t29+OR+1t2u+OR+1t2v)+AND+molecule_sequence:%5B''+TO+*%5D+AND+status:REL&sort=",
                "{\n" + "  \"responseHeader\":{\n" + "    \"status\":0,\n"
                        + "    \"QTime\":0,\n" + "    \"params\":{\n"
                        + "      \"q\":\"(4igk OR 7lyb OR 3k0h OR 3k0k OR 1t15 OR 3pxc OR 3pxd OR 3pxe OR 1jm7 OR 7jzv OR 3pxa OR 3pxb OR 1y98 OR 1n5o OR 4ifi OR 4y2g OR 3k15 OR 3k16 OR 4jlu OR 2ing OR 4ofb OR 6g2i OR 3coj OR 1jnx OR 4y18 OR 4u4a OR 1oqa OR 1t29 OR 1t2u OR 1t2v) AND molecule_sequence:['' TO *] AND status:REL\",\n"
                        + "      \"fl\":\"pdb_id,title,experimental_method,resolution\",\n"
                        + "      \"start\":\"0\",\n"
                        + "      \"sort\":\"\",\n"
                        + "      \"rows\":\"500\",\n"
                        + "      \"wt\":\"json\"}},\n"
                        + "  \"response\":{\"numFound\":64,\"start\":0,\"docs\":[\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4ofb\",\n"
                        + "        \"resolution\":3.05,\n"
                        + "        \"title\":\"Crystal structure of human BRCA1 BRCT in complex with nonphosphopeptide inhibitor\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3pxe\",\n"
                        + "        \"resolution\":2.85,\n"
                        + "        \"title\":\"Impact of BRCA1 BRCT domain missense substitutions on phospho-peptide recognition: E1836K\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4jlu\",\n"
                        + "        \"resolution\":3.5,\n"
                        + "        \"title\":\"Crystal structure of BRCA1 BRCT with doubly phosphorylated Abraxas\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4y2g\",\n"
                        + "        \"resolution\":2.5,\n"
                        + "        \"title\":\"Structure of BRCA1 BRCT domains in complex with Abraxas single phosphorylated peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Solution NMR\"],\n"
                        + "        \"pdb_id\":\"1oqa\",\n"
                        + "        \"title\":\"Solution structure of the BRCT-c domain from human BRCA1\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4u4a\",\n"
                        + "        \"resolution\":3.51,\n"
                        + "        \"title\":\"Complex Structure of BRCA1 BRCT with singly phospho Abraxas\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k16\",\n"
                        + "        \"resolution\":3.0,\n"
                        + "        \"title\":\"Crystal Structure of BRCA1 BRCT D1840T in complex with a minimal recognition tetrapeptide with a free carboxy C-terminus\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1t15\",\n"
                        + "        \"resolution\":1.85,\n"
                        + "        \"title\":\"Crystal Structure of the Brca1 BRCT Domains in Complex with the Phosphorylated Interacting Region from Bach1 Helicase\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k15\",\n"
                        + "        \"resolution\":2.8,\n"
                        + "        \"title\":\"Crystal Structure of BRCA1 BRCT D1840T in complex with a minimal recognition tetrapeptide with an amidated C-terminus\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1t2v\",\n"
                        + "        \"resolution\":3.3,\n"
                        + "        \"title\":\"Structural basis of phospho-peptide recognition by the BRCT domain of BRCA1, structure with phosphopeptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1y98\",\n"
                        + "        \"resolution\":2.5,\n"
                        + "        \"title\":\"Structure of the BRCT repeats of BRCA1 bound to a CtIP phosphopeptide.\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1t29\",\n"
                        + "        \"resolution\":2.3,\n"
                        + "        \"title\":\"Crystal structure of the BRCA1 BRCT repeats bound to a phosphorylated BACH1 peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k0k\",\n"
                        + "        \"resolution\":2.7,\n"
                        + "        \"title\":\"Crystal Structure of BRCA1 BRCT in complex with a minimal recognition tetrapeptide with a free carboxy C-terminus.\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k0h\",\n"
                        + "        \"resolution\":2.7,\n"
                        + "        \"title\":\"The crystal structure of BRCA1 BRCT in complex with a minimal recognition tetrapeptide with an amidated C-terminus\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4ifi\",\n"
                        + "        \"resolution\":2.2,\n"
                        + "        \"title\":\"Structure of human BRCA1 BRCT in complex with BAAT peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3pxd\",\n"
                        + "        \"resolution\":2.8,\n"
                        + "        \"title\":\"Impact of BRCA1 BRCT domain missense substitutions on phospho-peptide recognition: R1835P\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3pxc\",\n"
                        + "        \"resolution\":2.8,\n"
                        + "        \"title\":\"Impact of BRCA1 BRCT domain missense substitutions on phospho-peptide recognition: R1699Q\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1jnx\",\n"
                        + "        \"resolution\":2.5,\n"
                        + "        \"title\":\"Crystal structure of the BRCT repeat region from the breast cancer associated protein, BRCA1\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3pxa\",\n"
                        + "        \"resolution\":2.55,\n"
                        + "        \"title\":\"Impact of BRCA1 BRCT domain missense substitutions on phospho-peptide recognition: G1656D\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Solution NMR\"],\n"
                        + "        \"pdb_id\":\"1jm7\",\n"
                        + "        \"title\":\"Solution structure of the BRCA1/BARD1 RING-domain heterodimer\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4igk\",\n"
                        + "        \"resolution\":1.75,\n"
                        + "        \"title\":\"Structure of human BRCA1 BRCT in complex with ATRIP peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1t2u\",\n"
                        + "        \"resolution\":2.8,\n"
                        + "        \"title\":\"Structural basis of phosphopeptide recognition by the BRCT domain of BRCA1: structure of BRCA1 missense variant V1809F\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3pxb\",\n"
                        + "        \"resolution\":2.5,\n"
                        + "        \"title\":\"Impact of BRCA1 BRCT domain missense substitutions on phospho-peptide recognition: T1700A\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1n5o\",\n"
                        + "        \"resolution\":2.8,\n"
                        + "        \"title\":\"Structural consequences of a cancer-causing BRCA1-BRCT missense mutation\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3coj\",\n"
                        + "        \"resolution\":3.21,\n"
                        + "        \"title\":\"Crystal Structure of the BRCT Domains of Human BRCA1 in Complex with a Phosphorylated Peptide from Human Acetyl-CoA Carboxylase 1\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"6g2i\",\n"
                        + "        \"resolution\":5.9,\n"
                        + "        \"title\":\"Filament of acetyl-CoA carboxylase and BRCT domains of BRCA1 (ACC-BRCT) at 5.9 A resolution\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4jlu\",\n"
                        + "        \"resolution\":3.5,\n"
                        + "        \"title\":\"Crystal structure of BRCA1 BRCT with doubly phosphorylated Abraxas\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4ofb\",\n"
                        + "        \"resolution\":3.05,\n"
                        + "        \"title\":\"Crystal structure of human BRCA1 BRCT in complex with nonphosphopeptide inhibitor\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3pxe\",\n"
                        + "        \"resolution\":2.85,\n"
                        + "        \"title\":\"Impact of BRCA1 BRCT domain missense substitutions on phospho-peptide recognition: E1836K\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4u4a\",\n"
                        + "        \"resolution\":3.51,\n"
                        + "        \"title\":\"Complex Structure of BRCA1 BRCT with singly phospho Abraxas\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4y2g\",\n"
                        + "        \"resolution\":2.5,\n"
                        + "        \"title\":\"Structure of BRCA1 BRCT domains in complex with Abraxas single phosphorylated peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4y18\",\n"
                        + "        \"resolution\":3.5,\n"
                        + "        \"title\":\"Structure of BRCA1 BRCT domains in complex with Abraxas double phosphorylated peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"2ing\",\n"
                        + "        \"resolution\":3.6,\n"
                        + "        \"title\":\"X-ray Structure of the BRCA1 BRCT mutant M1775K\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1t15\",\n"
                        + "        \"resolution\":1.85,\n"
                        + "        \"title\":\"Crystal Structure of the Brca1 BRCT Domains in Complex with the Phosphorylated Interacting Region from Bach1 Helicase\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1t29\",\n"
                        + "        \"resolution\":2.3,\n"
                        + "        \"title\":\"Crystal structure of the BRCA1 BRCT repeats bound to a phosphorylated BACH1 peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Solution NMR\"],\n"
                        + "        \"pdb_id\":\"1jm7\",\n"
                        + "        \"title\":\"Solution structure of the BRCA1/BARD1 RING-domain heterodimer\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1t2v\",\n"
                        + "        \"resolution\":3.3,\n"
                        + "        \"title\":\"Structural basis of phospho-peptide recognition by the BRCT domain of BRCA1, structure with phosphopeptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4ifi\",\n"
                        + "        \"resolution\":2.2,\n"
                        + "        \"title\":\"Structure of human BRCA1 BRCT in complex with BAAT peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4igk\",\n"
                        + "        \"resolution\":1.75,\n"
                        + "        \"title\":\"Structure of human BRCA1 BRCT in complex with ATRIP peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k0k\",\n"
                        + "        \"resolution\":2.7,\n"
                        + "        \"title\":\"Crystal Structure of BRCA1 BRCT in complex with a minimal recognition tetrapeptide with a free carboxy C-terminus.\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k16\",\n"
                        + "        \"resolution\":3.0,\n"
                        + "        \"title\":\"Crystal Structure of BRCA1 BRCT D1840T in complex with a minimal recognition tetrapeptide with a free carboxy C-terminus\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k15\",\n"
                        + "        \"resolution\":2.8,\n"
                        + "        \"title\":\"Crystal Structure of BRCA1 BRCT D1840T in complex with a minimal recognition tetrapeptide with an amidated C-terminus\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3k0h\",\n"
                        + "        \"resolution\":2.7,\n"
                        + "        \"title\":\"The crystal structure of BRCA1 BRCT in complex with a minimal recognition tetrapeptide with an amidated C-terminus\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"1y98\",\n"
                        + "        \"resolution\":2.5,\n"
                        + "        \"title\":\"Structure of the BRCT repeats of BRCA1 bound to a CtIP phosphopeptide.\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"3coj\",\n"
                        + "        \"resolution\":3.21,\n"
                        + "        \"title\":\"Crystal Structure of the BRCT Domains of Human BRCA1 in Complex with a Phosphorylated Peptide from Human Acetyl-CoA Carboxylase 1\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                        + "        \"pdb_id\":\"4y18\",\n"
                        + "        \"resolution\":3.5,\n"
                        + "        \"title\":\"Structure of BRCA1 BRCT domains in complex with Abraxas double phosphorylated peptide\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"6g2i\",\n"
                        + "        \"resolution\":5.9,\n"
                        + "        \"title\":\"Filament of acetyl-CoA carboxylase and BRCT domains of BRCA1 (ACC-BRCT) at 5.9 A resolution\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7lyb\",\n"
                        + "        \"resolution\":3.28,\n"
                        + "        \"title\":\"Cryo-EM structure of the human nucleosome core particle in complex with BRCA1-BARD1-UbcH5c\"},\n"
                        + "      {\n"
                        + "        \"experimental_method\":[\"Electron Microscopy\"],\n"
                        + "        \"pdb_id\":\"7jzv\",\n"
                        + "        \"resolution\":3.9,\n"
                        + "        \"title\":\"Cryo-EM structure of the BRCA1-UbcH5c/BARD1 E3-E2 module bound to a nucleosome\"}]\n"
                        + "  }}" });

    try
    {
      for (int i = 1; i < 3; i++)
      {
        mocks.add(
                new String[]
                { readJsonStringFromFile(
                        "test/jalview/fts/threedbeacons/p01308_pdbfts_query_pt"
                                + i + ".txt").trim(),
                    readJsonStringFromFile(
                            "test/jalview/fts/threedbeacons/p01308_pdbfts_query_pt"
                                    + i + "_resp.txt").trim() });
      }
      for (int i = 1; i < 6; i++)
      {
        mocks.add(

                new String[]
                { readJsonStringFromFile(
                        "test/jalview/fts/threedbeacons/p0dtd1_pdbfts_fts_query_pt"
                                + i + ".txt").trim(),
                    readJsonStringFromFile(
                            "test/jalview/fts/threedbeacons/p0dtd1_pdbfts_fts_query_pt"
                                    + i + "_resp.txt").trim() });
      }
      // maize
      mocks.add(
              new String[]
              { "https://www.ebi.ac.uk/pdbe/search/pdb/select?wt=json&fl=pdb_id,title,experimental_method,resolution&rows=500&start=0&q=(1gaq+OR+5h92+OR+3b2f+OR+3w5u+OR+5h8y+OR+3w5v)+AND+molecule_sequence:%5B''+TO+*%5D+AND+status:REL&sort=",
                  "{\n" + "  \"responseHeader\":{\n" + "    \"status\":0,\n"
                          + "    \"QTime\":0,\n" + "    \"params\":{\n"
                          + "      \"q\":\"(1gaq OR 5h92 OR 3b2f OR 3w5u OR 5h8y OR 3w5v) AND molecule_sequence:['' TO *] AND status:REL\",\n"
                          + "      \"fl\":\"pdb_id,title,experimental_method,resolution\",\n"
                          + "      \"start\":\"0\",\n"
                          + "      \"sort\":\"\",\n"
                          + "      \"rows\":\"500\",\n"
                          + "      \"wt\":\"json\"}},\n"
                          + "  \"response\":{\"numFound\":11,\"start\":0,\"docs\":[\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"3b2f\",\n"
                          + "        \"resolution\":1.7,\n"
                          + "        \"title\":\"Maize Ferredoxin 1\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"5h92\",\n"
                          + "        \"resolution\":2.08,\n"
                          + "        \"title\":\"Crystal structure of the complex between maize Sulfite Reductase and ferredoxin in the form-3 crystal\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"5h8y\",\n"
                          + "        \"resolution\":2.2,\n"
                          + "        \"title\":\"Crystal structure of the complex between maize sulfite reductase and ferredoxin in the form-2 crystal\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"1gaq\",\n"
                          + "        \"resolution\":2.59,\n"
                          + "        \"title\":\"CRYSTAL STRUCTURE OF THE COMPLEX BETWEEN FERREDOXIN AND FERREDOXIN-NADP+ REDUCTASE\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"3w5u\",\n"
                          + "        \"resolution\":2.7,\n"
                          + "        \"title\":\"Cross-linked complex between Ferredoxin and Ferredoxin-NADP+ reductase\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"3w5v\",\n"
                          + "        \"resolution\":3.81,\n"
                          + "        \"title\":\"Cross-linked complex between Ferredoxin and Ferredoxin-NADP+ reductase\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"3w5u\",\n"
                          + "        \"resolution\":2.7,\n"
                          + "        \"title\":\"Cross-linked complex between Ferredoxin and Ferredoxin-NADP+ reductase\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"3w5v\",\n"
                          + "        \"resolution\":3.81,\n"
                          + "        \"title\":\"Cross-linked complex between Ferredoxin and Ferredoxin-NADP+ reductase\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"1gaq\",\n"
                          + "        \"resolution\":2.59,\n"
                          + "        \"title\":\"CRYSTAL STRUCTURE OF THE COMPLEX BETWEEN FERREDOXIN AND FERREDOXIN-NADP+ REDUCTASE\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"5h92\",\n"
                          + "        \"resolution\":2.08,\n"
                          + "        \"title\":\"Crystal structure of the complex between maize Sulfite Reductase and ferredoxin in the form-3 crystal\"},\n"
                          + "      {\n"
                          + "        \"experimental_method\":[\"X-ray diffraction\"],\n"
                          + "        \"pdb_id\":\"5h8y\",\n"
                          + "        \"resolution\":2.2,\n"
                          + "        \"title\":\"Crystal structure of the complex between maize sulfite reductase and ferredoxin in the form-2 crystal\"}]\n"
                          + "  }}" });
    } catch (Throwable e)
    {
      Assert.fail("Couldn't read mock data.", e);
    }
    /*
     * updating mocks for p0dtd1 require very long URLs to be queried 
     for i in test/jalview/fts/threedbeacons/p0dtd1_pdbfts_fts_query_pt?.txt; do wget -i $i -O ${i/.txt/_resp.txt}; done
     */
    FTSRestClient.createMockFTSRestClient(
            (FTSRestClient) PDBFTSRestClient.getInstance(),
            mocks.toArray(new String[0][2]));
  }
}
