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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.urls.api.UrlProviderI;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IdentifiersUrlProviderTest
{
  private static final String testIdOrgFile = "{\"Local\": [{\"id\":\"MIR:00000002\",\"name\":\"ChEBI\",\"pattern\":\"^CHEBI:\\d+$\","
          + "\"definition\":\"Chemical Entities of Biological Interest (ChEBI)\",\"prefix\":\"chebi\","
          + "\"url\":\"http://identifiers.org/chebi\"},{\"id\":\"MIR:00000005\",\"name\":\"UniProt Knowledgebase\","
          + "\"pattern\":\"^([A-N,R-Z][0-9]([A-Z][A-Z, 0-9][A-Z, 0-9][0-9]){1,2})|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])(\\.\\d+)?$\","
          + "\"definition\":\"The UniProt Knowledgebase (UniProtKB)\",\"prefix\":\"uniprot\",\"url\":\"http://identifiers.org/uniprot\"},"
          + "{\"id\":\"MIR:00000011\",\"name\":\"InterPro\",\"pattern\":\"^IPR\\d{6}$\",\"definition\":\"InterPro\",\"prefix\":\"interpro\","
          + "\"url\":\"http://identifiers.org/interpro\"},"
          + "{\"id\":\"MIR:00000372\",\"name\":\"ENA\",\"pattern\":\"^[A-Z]+[0-9]+(\\.\\d+)?$\",\"definition\":\"The European Nucleotide Archive (ENA),\""
          + "\"prefix\":\"ena.embl\",\"url\":\"http://identifiers.org/ena.embl\"}]}";

  private static final String[] dlinks = {
      "UniProt Knowledgebase|http://identifiers.org/uniprot/$DB_ACCESSION$|uniprot",
      "InterPro|http://identifiers.org/interpro/$DB_ACCESSION$|interpro",
      "ENA|http://identifiers.org/ena.embl/$DB_ACCESSION$|ena.embl" };

  private static final String[] dlinks1 = {
      "MIR:00000011|http://identifiers.org/interpro/$DB_ACCESSION$",
      "MIR:00000372|http://identifiers.org/ena.embl/$DB_ACCESSION$" };

  private static final String[] dlinks2 = {
      "MIR:00000005|http://identifiers.org/uniprot/$DB_ACCESSION$",
      "MIR:00000011|http://identifiers.org/interpro/$DB_ACCESSION$" };

  private static final String stringLinks = "MIR:00000005|http://identifiers.org/uniprot/$DB_ACCESSION$"
          + "MIR:00000011|http://identifiers.org/interpro/$DB_ACCESSION$"
          + "MIR:00000372|http://identifiers.org/ena.embl/$DB_ACCESSION$";

  private static final String[] unselDlinks = {
      "ChEBI|http://identifiers.org/chebi/$DB_ACCESSION$" };

  private static final Vector<String> displayLinks = new Vector<String>(
          Arrays.asList(dlinks));

  private static final Vector<String> unselDisplayLinks = new Vector<String>(
          Arrays.asList(unselDlinks));

  private static final Vector<String> displayLinks1 = new Vector<String>(
          Arrays.asList(dlinks1));

  private static final Vector<String> displayLinks2 = new Vector<String>(
          Arrays.asList(dlinks2));

  private static final HashMap<String, String> urlMap = new HashMap<String, String>()
  {
    {
      put("MIR:00000005", "http://identifiers.org/uniprot/$DB_ACCESSION$");
      put("MIR:00000011", "http://identifiers.org/interpro/$DB_ACCESSION$");
      put("MIR:00000372", "http://identifiers.org/ena.embl/$DB_ACCESSION$");
    }
  };

  private String testfile = "";

  @BeforeClass(alwaysRun = true)
  public void setup()
  {
    // setup test ids in a file
    File outFile = null;
    try
    {
      outFile = File.createTempFile("testidsfile", "txt");
      outFile.deleteOnExit();

      FileWriter fw = new FileWriter(outFile);
      fw.write(testIdOrgFile);
      fw.close();

      testfile = outFile.getAbsolutePath();

    } catch (Exception ex)
    {
      System.err.println(ex);
    }

    IdOrgSettings.setDownloadLocation(testfile);
  }

  /*
   * Test urls are set and returned correctly
   */
  @Test(groups = { "Functional" })
  public void testUrlLinks()
  {
    // creation from cached id list
    String idList = "MIR:00000005|MIR:00000011|MIR:00000372";
    UrlProviderI idProv = new IdentifiersUrlProvider(idList);

    assertTrue(displayLinks.containsAll(idProv.getLinksForMenu()));

    // because UrlProvider does not guarantee order of links, we can't just
    // compare the output of writeUrlsAsString to a string, hence the hoops here
    String result = idProv.writeUrlsAsString(true);
    UrlProviderI up = new IdentifiersUrlProvider(result);
    assertTrue(displayLinks.containsAll(up.getLinksForMenu()));

    result = idProv.writeUrlsAsString(false);
    up = new IdentifiersUrlProvider(result);
    assertTrue(unselDisplayLinks.containsAll(up.getLinksForMenu()));

  }

  /*
   * Test default is set and returned correctly
   */
  @Test(groups = { "Functional" })
  public void testDefaultUrl()
  {
    // creation from cached id list
    String idList = "MIR:00000005|MIR:00000011|MIR:00000372";
    UrlProviderI idProv = new IdentifiersUrlProvider(idList);

    // initially no default
    assertEquals(null, idProv.getPrimaryUrl("seqid"));

    // set and then retrieve default
    assertTrue(idProv.setPrimaryUrl("MIR:00000005"));
    assertEquals("http://identifiers.org/uniprot/seqid",
            idProv.getPrimaryUrl("seqid"));

    // ids less than length 4 return null
    assertEquals(null, idProv.getPrimaryUrl("123"));

    // attempt to set bad default
    assertFalse(idProv.setPrimaryUrl("MIR:00001234"));
    // default set to null (as default should have been set elsewhere)
    assertEquals(null, idProv.getPrimaryUrl("seqid"));

    // chooseDefaultUrl not implemented for IdentifiersUrlProvider
    assertEquals(null, idProv.choosePrimaryUrl());
  }
}
