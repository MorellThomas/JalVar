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
package jalview.ext.ensembl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import jalview.datamodel.DBRefEntry;
import jalview.gui.JvOptionPane;
import jalview.util.JSONUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnsemblXrefTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  //@formatter:off
  private static final String JSON = 
          "[{\"primary_id\":\"CCDS5863\",\"dbname\":\"CCDS\"}," +
           "{\"primary_id\":\"P15056\",\"dbname\":\"Uniprot/SWISSPROT\",\"synonyms\":[\"C21\"]}," +
           "{\"primary_id\":\"GO:0000165\",\"dbname\":\"GO\"}]";
  //@formatter:on

  @Test(groups = "Functional")
  public void testGetCrossReferences()
  {
    String dbName = "ENSEMBL";
    String dbVers = "0.6.2b1";
    System.out.println(JSON);
    EnsemblXref testee = new EnsemblXref("http://rest.ensembl.org", dbName,
            dbVers)
    {
      @SuppressWarnings("unchecked")
      @Override
      protected Object getJSON(URL url, List<String> ids, int msDelay,
              int mode, String mapKey) throws IOException, ParseException
      {
        return ((List<Object>) JSONUtils.parse(JSON)).iterator();
      }

    };

    // synonyms and GO terms are not returned
    List<DBRefEntry> dbrefs = testee.getCrossReferences("ABCDE");
    assertEquals(2, dbrefs.size());
    assertEquals("CCDS", dbrefs.get(0).getSource());
    assertEquals("CCDS5863", dbrefs.get(0).getAccessionId());
    assertFalse(dbrefs.get(0).isPrimaryCandidate());
    assertEquals(dbName + ":" + dbVers, dbrefs.get(0).getVersion());
    // Uniprot name should get converted to Jalview canonical form
    assertEquals("UNIPROT", dbrefs.get(1).getSource());
    assertEquals("P15056", dbrefs.get(1).getAccessionId());
    assertEquals(dbName + ":" + dbVers, dbrefs.get(1).getVersion());
    assertFalse(dbrefs.get(1).isPrimaryCandidate());
  }
}
