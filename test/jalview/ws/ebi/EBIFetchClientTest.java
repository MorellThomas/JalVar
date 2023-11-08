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
package jalview.ws.ebi;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EBIFetchClientTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test method that constructs URL to fetch from
   */
  @Test(groups = "Functional")
  public void testBuildUrl()
  {
    /*
     * EMBL
     */
    assertEquals(
            "https://www.ebi.ac.uk/ena/browser/api/embl/x53838?download=true&gzip=true",
            EBIFetchClient.buildUrl("X53838", "EMBL", "display=xml"));

    /*
     * EMBLCDS
     */
    assertEquals(
            "https://www.ebi.ac.uk/ena/browser/api/embl/caa37824?download=true&gzip=true",
            EBIFetchClient.buildUrl("CAA37824", "EMBL", "display=xml"));

    /*
     * PDB / pdb
     */
    assertEquals("https://www.ebi.ac.uk/Tools/dbfetch/dbfetch/pdb/3a6s/pdb",
            EBIFetchClient.buildUrl("3A6S", "PDB", "pdb"));

    /*
     * PDB / mmCIF
     */
    assertEquals(
            "https://www.ebi.ac.uk/Tools/dbfetch/dbfetch/pdb/3a6s/mmCIF",
            EBIFetchClient.buildUrl("3A6S", "PDB", "mmCIF"));
  }

  /**
   * Test method that parses db:id;id;id
   */
  @Test(groups = "Functional")
  public void testParseIds()
  {
    /*
     * pdb, two accessions
     */
    StringBuilder queries = new StringBuilder();
    String db = EBIFetchClient.parseIds("pdb:3a6s;1A70", queries);
    assertEquals("pdb", db);
    assertEquals("3a6s,1A70", queries.toString());

    /*
     * pdb specified on second accession
     */
    queries.setLength(0);
    queries = new StringBuilder();
    db = EBIFetchClient.parseIds("3a6s;pdb:1A70", queries);
    assertEquals("pdb", db);
    assertEquals("3a6s,1A70", queries.toString());

    /*
     * uniprot, one accession
     */
    queries.setLength(0);
    db = EBIFetchClient.parseIds("uniprot:P00340", queries);
    assertEquals("uniprot", db);
    assertEquals("P00340", queries.toString());

    /*
     * uniprot, one accession, appending to existing queries
     */
    queries.setLength(0);
    queries.append("P30419");
    db = EBIFetchClient.parseIds("uniprot:P00340", queries);
    assertEquals("uniprot", db);
    assertEquals("P30419,P00340", queries.toString());

    /*
     * pdb and uniprot mixed - rejected
     */
    queries.setLength(0);
    db = EBIFetchClient.parseIds("pdb:3a6s;1a70;uniprot:P00340", queries);
    assertNull(db);
    assertEquals("3a6s,1a70", queries.toString());

    /*
     * pdb and PDB mixed - ok
     */
    queries.setLength(0);
    db = EBIFetchClient.parseIds("pdb:3a6s;pdb:1a70;PDB:1QIP", queries);
    assertEquals("PDB", db);
    assertEquals("3a6s,1a70,1QIP", queries.toString());

    /*
     * no database (improper format)
     */
    queries.setLength(0);
    db = EBIFetchClient.parseIds("P00340", queries);
    assertNull(db);
    assertEquals("P00340", queries.toString());
  }
}
