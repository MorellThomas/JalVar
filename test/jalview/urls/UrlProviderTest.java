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
package jalview.urls;

import jalview.urls.api.UrlProviderI;
import jalview.urls.desktop.DesktopUrlProviderFactory;
import jalview.util.UrlConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UrlProviderTest
{

  // Test identifiers.org download file
  private static final String testIdOrgString = "{\"Local\": [{\"id\":\"MIR:00000002\",\"name\":\"ChEBI\",\"pattern\":\"^CHEBI:\\d+$\","
          + "\"definition\":\"Chemical Entities of Biological Interest (ChEBI)\",\"prefix\":\"chebi\","
          + "\"url\":\"http://identifiers.org/chebi\"},{\"id\":\"MIR:00000005\",\"name\":\"UniProt Knowledgebase\","
          + "\"pattern\":\"^([A-N,R-Z][0-9]([A-Z][A-Z, 0-9][A-Z, 0-9][0-9]){1,2})|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])(\\.\\d+)?$\","
          + "\"definition\":\"The UniProt Knowledgebase (UniProtKB)\",\"prefix\":\"uniprot\",\"url\":\"http://identifiers.org/uniprot\"},"
          + "{\"id\":\"MIR:00000011\",\"name\":\"InterPro\",\"pattern\":\"^IPR\\d{6}$\",\"definition\":\"InterPro\",\"prefix\":\"interpro\","
          + "\"url\":\"http://identifiers.org/interpro\"},"
          + "{\"id\":\"MIR:00000372\",\"name\":\"ENA\",\"pattern\":\"^[A-Z]+[0-9]+(\\.\\d+)?$\",\"definition\":\"The European Nucleotide Archive (ENA),\""
          + "\"prefix\":\"ena.embl\",\"url\":\"http://identifiers.org/ena.embl\"}]}";

  private UrlProviderI prov;

  @BeforeMethod(alwaysRun = true)
  public void setup()
  {
    // make a dummy identifiers.org download file
    File temp = null;

    try
    {
      temp = File.createTempFile("tempfile", ".tmp");
      temp.deleteOnExit();
      BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
      bw.write(testIdOrgString);
      bw.close();
    } catch (IOException e)
    {
      System.out.println(
              "Error initialising UrlProviderTest test: " + e.getMessage());
    }

    IdOrgSettings.setDownloadLocation(temp.getPath());

    String defaultUrlString = "No default";
    String cachedUrlList = "MIR:00000005|MIR:00000011|Test1|http://blah.blah/$SEQUENCE_ID$|"
            + "Test2|http://test2/$DB_ACCESSION$|Test3|http://test3/$SEQUENCE_ID$";
    String userUrlList = "MIR:00000372|Test4|httpL//another.url/$SEQUENCE_ID$";

    DesktopUrlProviderFactory factory = new DesktopUrlProviderFactory(
            defaultUrlString, cachedUrlList, userUrlList);
    prov = factory.createUrlProvider();
  }

  @Test(groups = { "Functional" })
  public void testInitUrlProvider()
  {
    String emblUrl = UrlConstants.DEFAULT_STRING.substring(
            UrlConstants.DEFAULT_STRING.indexOf(UrlConstants.SEP) + 1,
            UrlConstants.DEFAULT_STRING.length());

    // chooses EMBL url when default Url id does not exist in provided url lists
    Assert.assertEquals(prov.getPrimaryUrlId(), UrlConstants.DEFAULT_LABEL);
    Assert.assertEquals(prov.getPrimaryUrl("FER_CAPAN"),
            emblUrl.replace("$SEQUENCE_ID$", "FER_CAPAN"));

    List<String> menulinks = prov.getLinksForMenu();
    List<UrlLinkDisplay> allLinks = prov.getLinksForTable();

    // 9 links in provider - 4 from id file, 4 custom links, 1 additional
    // default
    Assert.assertEquals(allLinks.size(), 9);

    // 6 links in menu (cachedUrlList) + new default
    Assert.assertEquals(menulinks.size(), 6);

    Assert.assertTrue(
            menulinks.contains("Test1|http://blah.blah/$SEQUENCE_ID$"));
    Assert.assertTrue(
            menulinks.contains("Test2|http://test2/$DB_ACCESSION$"));
    Assert.assertTrue(
            menulinks.contains("Test3|http://test3/$SEQUENCE_ID$"));
    Assert.assertTrue(menulinks.contains(
            "UniProt Knowledgebase|http://identifiers.org/uniprot/$DB_ACCESSION$|uniprot"));
    Assert.assertTrue(menulinks.contains(
            "InterPro|http://identifiers.org/interpro/$DB_ACCESSION$|interpro"));
    Assert.assertTrue(menulinks.contains(
            UrlConstants.DEFAULT_LABEL + UrlConstants.SEP + emblUrl));
  }

  @Test(groups = { "Functional" })
  public void testSetDefaultUrl()
  {
    // set custom url as default
    Assert.assertTrue(prov.setPrimaryUrl("Test1"));
    Assert.assertEquals(prov.getPrimaryUrlId(), "Test1");

    // set identifiers url as default
    Assert.assertTrue(prov.setPrimaryUrl("MIR:00000011"));
    Assert.assertEquals(prov.getPrimaryUrlId(), "MIR:00000011");
  }

  @Test(
    groups =
    { "Functional" },
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testSetDefaultUrlWrongly()
  {
    // don't allow default to be a non-key
    prov.setPrimaryUrl("not-a-key");
  }
}
