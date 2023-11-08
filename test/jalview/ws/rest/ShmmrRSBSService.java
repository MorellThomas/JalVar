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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;

import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author jimp
 * 
 */
public class ShmmrRSBSService
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testShmmrService()
  {

    assertTrue(
            "Test Rsd Exchange using using default Shmmr service failed.",
            testRsdExchange("Test using default Shmmr service",
                    RestClient.makeShmmrRestClient().service));
  }

  @Test(groups = { "Functional" })
  public void testShmmrServiceDataprep() throws Exception
  {
    RestClient _rc = RestClient.makeShmmrRestClient();
    assertNotNull(_rc);
    AlignFrame alf = new jalview.io.FileLoader(false)
            .LoadFileWaitTillLoaded("examples/testdata/smad.fa",
                    jalview.io.DataSourceType.FILE);
    assertNotNull("Couldn't find test data.", alf);
    alf.loadJalviewDataFile("examples/testdata/smad_groups.jva",
            jalview.io.DataSourceType.FILE, null, null);
    assertTrue(
            "Couldn't load the test data's annotation file (should be 5 groups but found "
                    + alf.getViewport().getAlignment().getGroups().size()
                    + ").",
            alf.getViewport().getAlignment().getGroups().size() == 5);

    RestClient rc = new RestClient(_rc.service, alf, true);

    assertNotNull("Couldn't creat RestClient job.", rc);
    jalview.bin.Console.initLogger();
    RestJob rjb = new RestJob(0, new RestJobThread(rc),
            rc.av.getAlignment(), null);
    rjb.setAlignmentForInputs(rc.service.getInputParams().values(),
            rc.av.getAlignment());
    for (Map.Entry<String, InputType> e : rc.service.getInputParams()
            .entrySet())
    {
      System.out.println("For Input '" + e.getKey() + ":\n"
              + e.getValue().formatForInput(rjb).getContentLength());
    }
  }

  private static boolean testRsdExchange(String desc, String servicestring)
  {
    try
    {
      RestServiceDescription newService = new RestServiceDescription(
              servicestring);
      if (!newService.isValid())
      {
        throw new Error("Failed to create service from '" + servicestring
                + "'.\n" + newService.getInvalidMessage());
      }
      return testRsdExchange(desc, newService);
    } catch (Throwable x)
    {
      System.err.println(
              "Failed for service (" + desc + "): " + servicestring);
      x.printStackTrace();
      return false;
    }
  }

  private static boolean testRsdExchange(String desc,
          RestServiceDescription service)
  {
    try
    {
      String fromservicetostring = service.toString();
      RestServiceDescription newService = new RestServiceDescription(
              fromservicetostring);
      if (!newService.isValid())
      {
        throw new Error(
                "Failed to create service from '" + fromservicetostring
                        + "'.\n" + newService.getInvalidMessage());
      }

      if (!service.equals(newService))
      {
        System.err.println("Failed for service (" + desc + ").");
        System.err.println("Original service and parsed service differ.");
        System.err.println("Original: " + fromservicetostring);
        System.err.println("Parsed  : " + newService.toString());
        return false;
      }
    } catch (Throwable x)
    {
      System.err.println(
              "Failed for service (" + desc + "): " + service.toString());
      x.printStackTrace();
      return false;
    }
    return true;
  }

}
