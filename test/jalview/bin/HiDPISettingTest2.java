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
import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import jalview.gui.Desktop;
import jalview.util.Platform;

public class HiDPISettingTest2
{
  private static final int TIMEOUT = 10;

  private static class Worker extends Thread
  {
    private final Process process;

    private BufferedReader outputReader;

    private BufferedReader errorReader;

    private boolean exited;

    private Worker(Process process)
    {
      this.process = process;
    }

    @Override
    public void run()
    {
      try
      {
        exited = process.waitFor(TIMEOUT, TimeUnit.SECONDS);
      } catch (InterruptedException ignore)
      {
        return;
      }
      this.interrupt();
      this.process.destroy();
    }

    public BufferedReader getOutputReader()
    {
      return outputReader;
    }

    public void setOutputReader(BufferedReader outputReader)
    {
      this.outputReader = outputReader;
    }

    public BufferedReader getErrorReader()
    {
      return errorReader;
    }

    public void setErrorReader(BufferedReader errorReader)
    {
      this.errorReader = errorReader;
    }
  }

  private static ClassGraph scanner = null;

  private static String classpath = null;

  private static String java_exe = null;

  public synchronized static String getClassPath()
  {
    if (scanner == null)
    {
      scanner = new ClassGraph();
      ScanResult scan = scanner.scan();
      classpath = scan.getClasspath();
      java_exe = System.getProperty("java.home") + File.separator + "bin"
              + File.separator + "java";

    }
    while (classpath == null)
    {
      try
      {
        Thread.sleep(10);
      } catch (InterruptedException x)
      {

      }
    }
    return classpath;
  }

  private Worker getJalviewDesktopRunner(String jvmArgs, String appArgs)
  {
    String classpath = getClassPath();
    String cmd = java_exe + " " + " -classpath " + classpath + " " + jvmArgs
            + " jalview.bin.Jalview " + " "
            + "--props=test/jalview/bin/hidpiTestProps.jvprops " + appArgs;
    Process proc = null;
    Worker worker = null;
    try
    {
      proc = Runtime.getRuntime().exec(cmd);
    } catch (Throwable e)
    {
      e.printStackTrace();
    }
    if (proc != null)
    {
      BufferedReader outputReader = new BufferedReader(
              new InputStreamReader(proc.getInputStream()));
      BufferedReader errorReader = new BufferedReader(
              new InputStreamReader(proc.getErrorStream()));
      worker = new Worker(proc);
      worker.start();
      worker.setOutputReader(outputReader);
      worker.setErrorReader(errorReader);
    }
    return worker;
  }

  @BeforeTest(alwaysRun = true)
  public void initialize()
  {
    new HiDPISettingTest2();
  }

  @Test(groups = { "Functional" }, dataProvider = "hidpiScaleArguments")
  public void testHiDPISettings(int scale)
  {
    if (!Platform.isLinux())
    {
      throw new SkipException(
              "Not linux platform, not testing actual scaling with "
                      + HiDPISetting.scalePropertyName);
    }

    String jvmArgs = HiDPISetting.getScalePropertyArg(scale);

    String appArgs = " -open examples/uniref50.fa -nosplash -nonews -noquestionnaire -nousagestats -nowebservicediscovery";

    Worker worker = getJalviewDesktopRunner(jvmArgs, appArgs);
    assertNotNull(worker, "worker is null");

    String ln = null;
    int count = 0;
    boolean scaleFound = false;
    try
    {
      while ((ln = worker.getErrorReader().readLine()) != null)
      {
        if (++count > 100)
        {
          break;
        }
        if (ln.contains(Desktop.debugScaleMessage))
        {
          String number = ln.substring(ln.indexOf(Desktop.debugScaleMessage)
                  + Desktop.debugScaleMessage.length());
          number = number.substring(0, number.indexOf(' '));
          try
          {
            double d = Double.valueOf(number);

            assertEquals(d, scale * 1.0);
            scaleFound = true;
          } catch (NumberFormatException e)
          {
            e.printStackTrace();
            Assert.fail(
                    "Debug scale message line '" + ln + "' gives number '"
                            + number + "' which could not be parsed");
          }
          break;
        }
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    if (worker != null && worker.exited == false)
    {
      worker.interrupt();
      worker.process.destroy();
    }
    if (!scaleFound)
    {
      Assert.fail("Did not find Debug scale message line '"
              + Desktop.debugScaleMessage + "'");
    }
  }

  @DataProvider(name = "hidpiScaleArguments")
  public static Object[][] getHiDPIScaleArguments()
  {
    return new Object[][] { { 1 }, { 2 }, { 4 }, { 10 } };
  }
}
