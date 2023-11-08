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
package jalview.ws.gui;

import java.util.Locale;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.gui.JvOptionPane;
import jalview.gui.WsJobParameters;
import jalview.util.MessageManager;
import jalview.ws.jabaws.JalviewJabawsTestUtils;
import jalview.ws.jws2.JabaPreset;
import jalview.ws.jws2.Jws2Discoverer;
import jalview.ws.jws2.jabaws2.Jws2Instance;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import compbio.metadata.Preset;
import compbio.metadata.PresetManager;

public class Jws2ParamView
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * which services to test
   */
  public static List<String> serviceTests = new ArrayList<String>();

  /**
   * which presets to test for services
   */
  public static List<String> presetTests = new ArrayList<String>();
  static
  {
    serviceTests.add("AAConWS".toLowerCase(Locale.ROOT));
  }

  public static Jws2Discoverer disc = null;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Console.initLogger();
    disc = JalviewJabawsTestUtils.getJabawsDiscoverer();
  }

  /**
   * This test marked Interactive as it appears to need user action to complete
   * rather than hang
   */

  @Test(groups = { "Interactive" }, enabled = true)
  public void testJws2Gui()
  {
    Iterator<String> presetEnum = presetTests.iterator();
    for (Jws2Instance service : disc.getServices())
    {
      if (serviceTests.size() == 0 || serviceTests
              .contains(service.serviceType.toLowerCase(Locale.ROOT)))
      {
        List<Preset> prl = null;
        Preset pr = null;
        if (presetEnum.hasNext())
        {
          PresetManager prman = service.getPresets();
          if (prman != null)
          {
            pr = prman.getPresetByName(presetEnum.next());
            if (pr == null)
            {
              // just grab the last preset.
              prl = prman.getPresets();
            }
          }
        }
        else
        {
          PresetManager prman = service.getPresets();
          if (prman != null)
          {
            prl = prman.getPresets();
          }
        }
        Iterator<Preset> en = (prl == null) ? null : prl.iterator();
        while (en != null && en.hasNext())
        {
          if (en != null)
          {
            if (!en.hasNext())
            {
              en = prl.iterator();
            }
            pr = en.next();
          }
          WsJobParameters pgui = new WsJobParameters(service,
                  new JabaPreset(service, pr));
          JFrame jf = new JFrame(MessageManager
                  .formatMessage("label.ws_parameters_for", new String[]
                  { service.getActionText() }));
          jf.setSize(700, 800);
          JPanel cont = new JPanel(new BorderLayout());
          pgui.validate();
          cont.setPreferredSize(pgui.getPreferredSize());
          cont.add(pgui, BorderLayout.CENTER);
          jf.setLayout(new BorderLayout());
          jf.add(cont, BorderLayout.CENTER);
          jf.validate();

          final Thread thr = Thread.currentThread();

          /*
           * This seems to need a user to manually inspect / test / close the
           * GUI for each service tested. Not standalone JUnit.
           */
          jf.addWindowListener(new WindowAdapter()
          {
            @Override
            public void windowClosing(WindowEvent e)
            {
              thr.interrupt();
            }
          });
          jf.setVisible(true);
          boolean inter = false;
          while (!inter)
          {
            try
            {
              Thread.sleep(10 * 1000);
            } catch (InterruptedException e)
            {
              inter = true;
            }
          }
          jf.dispose();
        }
      }
    }
  }
}
