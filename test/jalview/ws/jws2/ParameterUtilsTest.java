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
package jalview.ws.jws2;

import java.util.Locale;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.gui.JvOptionPane;
import jalview.ws.jabaws.JalviewJabawsTestUtils;
import jalview.ws.jws2.jabaws2.Jws2Instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import compbio.metadata.Option;
import compbio.metadata.Parameter;
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.WrongParameterException;

/*
 * All methods in this class are set to the Network group because setUpBeforeClass will fail
 * if there is no network.
 */
@Test(singleThreaded = true)
public class ParameterUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /*
   * To limit tests to specify services, add them to this list; leave list empty
   * to test all
   */
  private static List<String> serviceTests = new ArrayList<String>();

  private static Jws2Discoverer disc = null;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    serviceTests.add("AAConWS".toLowerCase(Locale.ROOT));
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Console.initLogger();
    disc = JalviewJabawsTestUtils.getJabawsDiscoverer();
  }

  @Test(groups = { "Network" })
  public void testWriteParameterSet() throws WrongParameterException
  {
    for (Jws2Instance service : disc.getServices())
    {
      if (isForTesting(service))
      {

        List<Preset> prl = null;
        PresetManager prman = service.getPresets();
        if (prman == null)
        {
          continue;
        }
        prl = prman.getPresets();
        if (prl == null)
        {
          continue;
        }
        for (Preset pr : prl)
        {
          List<String> writeparam = null;
          List<String> readparam = null;
          writeparam = ParameterUtils.writeParameterSet(
                  pr.getArguments(service.getRunnerConfig()), " ");
          List<Option> pset = ParameterUtils.processParameters(writeparam,
                  service.getRunnerConfig(), " ");
          readparam = ParameterUtils.writeParameterSet(pset, " ");
          Iterator<String> o = pr.getOptions().iterator();
          Iterator<String> s = writeparam.iterator();
          Iterator<String> t = readparam.iterator();
          boolean failed = false;
          while (s.hasNext() && t.hasNext())
          {
            String on = o.next();
            String sn = s.next();
            String st = t.next();
            final String errorMsg = "Original was " + on + " Phase 1 wrote "
                    + sn + "\tPhase 2 wrote " + st;
            assertEquals(errorMsg, sn, st);
            assertEquals(errorMsg, sn, on);
          }
        }
      }
    }
  }

  /**
   * Returns true if the service is in the list of the ones we chose to test,
   * _or_ the list is empty (test all)
   * 
   * @param service
   * @return
   */
  public boolean isForTesting(Jws2Instance service)
  {
    return serviceTests.size() == 0 || serviceTests
            .contains(service.serviceType.toLowerCase(Locale.ROOT));
  }

  @Test(groups = { "Network" })
  public void testCopyOption()
  {
    for (Jws2Instance service : disc.getServices())
    {
      if (isForTesting(service))
      {
        List<Option<?>> options = service.getRunnerConfig().getOptions();
        for (Option<?> o : options)
        {
          System.out.println("Testing copyOption for option " + o.getName()
                  + " of " + service.getActionText());
          Option<?> cpy = ParameterUtils.copyOption(o);
          assertTrue(cpy.equals(o));
          assertEquals(cpy.getName(), o.getName());
          assertFalse(cpy == o);
          // todo more assertions?
        }
      }
    }
  }

  /**
   */
  @Test(groups = { "Network" })
  public void testCopyParameter()
  {
    for (Jws2Instance service : disc.getServices())
    {
      if (isForTesting(service))
      {
        List<Parameter> parameters = service.getRunnerConfig()
                .getParameters();
        for (Parameter o : parameters)
        {
          System.out.println("Testing copyParameter for parameter "
                  + o.getName() + " of " + service.getActionText());
          Parameter cpy = ParameterUtils.copyParameter(o);
          assertTrue(cpy.equals(o));
          assertFalse(cpy == o);
          assertEquals(cpy.getName(), o.getName());
          // todo more assertions?
        }
      }
    }
  }
}
