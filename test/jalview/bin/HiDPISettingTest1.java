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
package jalview.bin;

import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.util.Platform;

/*
 * Testing a HiDPI display is difficult without running in a HiDPI display.
 * The gathering of screen size and resolution performed by jalview.bin.HiDPISetting
 * is now farmed out into a separate class jalview.bin.ScreenInfo so it can be more
 * easily Mocked in tests.
 * Two sets of tests are performed.
 * 1) testLinuxScalePropertyToActualTransform() sets the property that HiDPISetting
 * uses (via jalview.bin.Launcher or getdown) to scale up Jalview, and then looks at
 * the alignment panel graphics2d transform to see if it's been scaled by the same
 * amount (in this case 4 -- unlikely to happen by accident!).
 * 2) testHiDPISettingInit() which tests the calculation that HiDPISetting uses to
 * decide from apparent screen information (mocked using Mockito in the tests) what
 * scaling factor to set (it doesn't actually set it, just suggests it to
 * jalview.bin.Launcher)
 */
public class HiDPISettingTest1
{

  AlignFrame af = null;

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
    Cache.loadProperties("test/jalview/bin/hidpiTestProps.jvprops");
    Jalview.main(
            new String[]
            { "-nosplash", "-nonews", "-noquestionnaire",
                "-nowebservicediscovery" });

    af = new FileLoader().LoadFileWaitTillLoaded("examples/uniref50.fa",
            DataSourceType.FILE);

    /*
     * wait for Consensus thread to complete
     */
    do
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {
      }
    } while (af.getViewport().getCalcManager().isWorking());
  }

  @AfterClass(alwaysRun = true)
  public void tearDown()
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @Test(groups = { "Functional" })
  public void testHiDPISettingInit()
  {
    String scalePropertyName = "sun.java2d.uiScale";
    // ensure scale property is cleared (otherwise it will be re-used)
    System.clearProperty(HiDPISetting.scalePropertyName);

    { // keep the mock under lock
      // Ancient monitor -- no property change set
      setMockScreen(1024, 768, 72);
      assertEquals(HiDPISetting.getScalePropertyArg(), null);

      // Old monitor -- no property change set
      setMockScreen(1200, 800, 96);
      assertEquals(HiDPISetting.getScalePropertyArg(), null);

      // HD screen -- no property change set
      setMockScreen(1920, 1080, 96);
      assertEquals(HiDPISetting.getScalePropertyArg(), null);

      // currently HiDPISetting only operates for Linux

      // 4K screen -- scale by 2
      setMockScreen(3180, 2160, 80);
      assertEquals(HiDPISetting.getScalePropertyArg(),
              Platform.isLinux() ? "-D" + scalePropertyName + "=2" : null);

      // 4K screen with high dpi -- scale by 3
      setMockScreen(3180, 2160, 450);
      assertEquals(HiDPISetting.getScalePropertyArg(),
              Platform.isLinux() ? "-D" + scalePropertyName + "=3" : null);

      // stupidly big screen -- scale by 8
      setMockScreen(19200, 10800, 72);
      assertEquals(HiDPISetting.getScalePropertyArg(),
              Platform.isLinux() ? "-D" + scalePropertyName + "=8" : null);
    }
  }

  private void setMockScreen(int width, int height, int dpi)
  {
    HiDPISetting.clear();
    ScreenInfo mockScreenInfo = Mockito.spy(HiDPISetting.getScreenInfo());
    Mockito.doReturn(height).when(mockScreenInfo).getScreenHeight();
    Mockito.doReturn(width).when(mockScreenInfo).getScreenWidth();
    Mockito.doReturn(dpi).when(mockScreenInfo).getScreenResolution();
    HiDPISetting.setScreenInfo(mockScreenInfo);
  }
}
