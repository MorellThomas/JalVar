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
package jalview.ws.jabaws;

import jalview.gui.JvOptionPane;
import jalview.ws.jws2.Jws2Discoverer;

import java.util.Vector;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JalviewJabawsTestUtils
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
  }

  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
  }

  /**
   * test servers
   */
  private static String[] serviceUrls = new String[] {
      "http://localhost:8080/jabaws",
      "http://www.compbio.dundee.ac.uk/jabaws" };

  @Test(groups = { "Functional" }, enabled = false)
  public void testAnnotExport()
  {
    Assert.fail("Not yet implemented");
  }

  public static jalview.ws.jws2.Jws2Discoverer getJabawsDiscoverer()
  {
    return getJabawsDiscoverer(true);
  }

  /**
   * Returns a service discoverer that queries localhost and compbio urls.
   * <p>
   * If using this method, be sure to have read-only Jalview properties, to
   * avoid writing the test urls to .jalview_properties. This can be done by
   * either
   * <ul>
   * <li>running Jalview main with arguments -props propFileName</li>
   * <li>calling Cache.loadProperties(filename)</li>
   * <ul>
   * 
   * @param localhost
   * @return
   */
  public static Jws2Discoverer getJabawsDiscoverer(boolean localhost)
  {
    jalview.ws.jws2.Jws2Discoverer disc = jalview.ws.jws2.Jws2Discoverer
            .getDiscoverer();
    String svcurls = "";
    if (localhost)
    {
      int p = 0;
      Vector<String> services = new Vector<String>();
      for (String url : JalviewJabawsTestUtils.serviceUrls)
      {
        svcurls += url + "; ";
        services.add(url);
      }
      ;
      Jws2Discoverer.getDiscoverer().setServiceUrls(services);
    }
    try
    {
      disc.run();
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail(
              "Aborting. Problem discovering services. Tried " + svcurls);
    }
    Assert.assertTrue(disc.getServices().size() > 0,
            "Failed to discover any services at ");
    return disc;
  }

}
