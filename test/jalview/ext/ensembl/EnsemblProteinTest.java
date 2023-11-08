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

import jalview.gui.JvOptionPane;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnsemblProteinTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testIsValidReference() throws Exception
  {
    EnsemblSequenceFetcher esq = new EnsemblProtein();
    Assert.assertTrue(esq.isValidReference("CCDS5863.1"));
    Assert.assertTrue(esq.isValidReference("ENSP00000288602"));
    Assert.assertFalse(esq.isValidReference("ENST00000288602"));
    Assert.assertFalse(esq.isValidReference("ENSG00000288602"));
    // non-human species having a 3 character identifier included:
    Assert.assertTrue(esq.isValidReference("ENSMUSP00000099398"));
  }

  @Test(groups = "Functional")
  public void testGetAccesionIdFromQuery() throws Exception
  {
    EnsemblSequenceFetcher esq = new EnsemblProtein();
    assertEquals("ENSP00000288602",
            esq.getAccessionIdFromQuery("ENSP00000288602"));
    assertEquals("ENSMUSP00000288602",
            esq.getAccessionIdFromQuery("ENSMUSP00000288602"));

    // ENST converted to ENSP
    assertEquals("ENSP00000288602",
            esq.getAccessionIdFromQuery("ENST00000288602"));
    assertEquals("ENSMUSP00000288602",
            esq.getAccessionIdFromQuery("ENSMUST00000288602"));

    // with valid separator:
    assertEquals("ENSP00000288604",
            esq.getAccessionIdFromQuery("ENSP00000288604 ENSP00000288602"));

    // with wrong separator:
    assertEquals("ENSP00000288604,ENSP00000288602",
            esq.getAccessionIdFromQuery("ENSP00000288604,ENSP00000288602"));
  }

}
