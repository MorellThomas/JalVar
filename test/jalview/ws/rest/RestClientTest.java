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
package jalview.ws.rest;

import static org.testng.AssertJUnit.assertEquals;

import jalview.bin.Cache;
import jalview.gui.JvOptionPane;

import java.util.Vector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RestClientTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Refactored 'as is' from main method
   */
  @Test(groups = { "Functional" })
  public void testGetRestClient()
  {
    /*
     * Load test properties file (readonly) so as not to overwrite the real one
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");

    RestClient[] clients = RestClient.getRestClients();
    System.out.println("Got " + clients.length + " clients.");
    int i = 0;
    Vector<String> urls = new Vector<String>();
    for (RestClient cl : clients)
    {
      System.out.println("" + (++i) + ": " + cl.service.toString());
      urls.add(cl.service.toString());
    }
    RestClient.setRsbsServices(urls);

    RestClient[] restClients = RestClient.getRestClients();
    assertEquals("", clients.length, restClients.length);

    /*
     * Check the two lists hold 'equal' (albeit different) objects. Ordering
     * should be the same as getRestClients returns the list in the same order
     * as setRsbsServices sets it.
     */
    for (i = 0; i < clients.length; i++)
    {
      /*
       * RestServiceDescription.equals() compares numerous fields
       */
      assertEquals(clients[i].getRestDescription(),
              restClients[i].getRestDescription());
    }
  }
}
