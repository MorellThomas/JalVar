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

import static org.testng.Assert.assertTrue;

import jalview.datamodel.AlignmentI;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EnsemblRestClientTest
{
  private EnsemblRestClient sf;

  @Test(suiteName = "live")
  public void testIsEnsemblAvailable()
  {
    boolean isAvailable = sf.isEnsemblAvailable();
    if (isAvailable)
    {
      System.out.println("Ensembl is UP!");
    }
    else
    {
      System.err.println(
              "Ensembl is DOWN or unreachable ******************* BAD!");
    }
  }

  @BeforeMethod(alwaysRun = true)
  protected void setUp()
  {
    sf = new EnsemblRestClient()
    {

      @Override
      public String getDbName()
      {
        return null;
      }

      @Override
      public AlignmentI getSequenceRecords(String queries) throws Exception
      {
        return null;
      }

      @Override
      protected URL getUrl(List<String> ids) throws MalformedURLException
      {
        return null;
      }

      @Override
      protected boolean useGetRequest()
      {
        return false;
      }
    };
  }

  @Test(groups = "Network")
  public void testCheckEnsembl_overload()
  {
    for (int i = 0; i < 20; i++)
    {
      assertTrue(sf.checkEnsembl(), "Error on " + (i + 1) + "th ping");
    }
  }
}
