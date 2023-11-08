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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.FileAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ModuleRef;
import io.github.classgraph.ScanResult;
import jalview.gui.JvOptionPane;

public class CommandLineOperationsNG
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // Note longer timeout needed on full test run than on individual tests
  private static final int TEST_TIMEOUT = 13000;

  private static final int SETUP_TIMEOUT = 9500;

  private static final int MINFILESIZE_SMALL = 2096;

  private static final int MINFILESIZE_BIG = 4096;

  private List<String> successfulCMDs = new ArrayList<>();

  /***
   * from
   * http://stackoverflow.com/questions/808276/how-to-add-a-timeout-value-when
   * -using-javas-runtime-exec
   * 
   * @author jimp
   * 
   */
  private static class Worker extends Thread
  {
    private final Process process;

    private BufferedReader outputReader;

    private BufferedReader errorReader;

    private Integer exit;

    private Worker(Process process)
    {
      this.process = process;
    }

    @Override
    public void run()
    {
      try
      {
        exit = process.waitFor();
      } catch (InterruptedException ignore)
      {
        return;
      }
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

  private static String modules = null;

  private static String java_exe = null;

  public synchronized static String getClassPath()
  {
    if (scanner == null)
    {
      scanner = new ClassGraph();
      ScanResult scan = scanner.scan();
      classpath = scan.getClasspath();
      modules = "";
      for (ModuleRef mr : scan.getModules())
      {
        modules.concat(mr.getName());
      }
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

  private Worker getJalviewDesktopRunner(boolean withAwt, String cmd,
          int timeout)
  {
    /*
    boolean win = System.getProperty("os.name").indexOf("Win") >= 0;
    String pwd = "";
    try
    {
      Path currentRelativePath = Paths.get("");
      pwd = currentRelativePath.toAbsolutePath().toString();
    } catch (Exception q)
    {
      q.printStackTrace();
    }
    if (pwd == null || pwd.length() == 0)
      pwd = ".";
    String[] classpaths = new String[] { pwd + "/bin/main",
        pwd + "/j11lib/*", pwd + "/resources", pwd + "/help" };
    String classpath = String.join(win ? ";" : ":", classpaths);
    getClassPath();
    */
    // Note: JAL-3065 - don't include quotes for lib/* because the arguments are
    // not expanded by the shell
    String classpath = getClassPath();
    String _cmd = java_exe + " "
            + (withAwt ? "-Djava.awt.headless=true" : "") + " -classpath "
            + classpath
            + ((modules != null && modules.length() > 2)
                    ? "--add-modules=\"" + modules + "\""
                    : "")
            + " jalview.bin.Jalview ";
    Process ls2_proc = null;
    Worker worker = null;
    try
    {
      cmd = " --testoutput " + cmd;
      System.out.println("Running '" + _cmd + cmd + "'");
      ls2_proc = Runtime.getRuntime().exec(_cmd + cmd);
    } catch (Throwable e1)
    {
      e1.printStackTrace();
    }
    if (ls2_proc != null)
    {
      BufferedReader outputReader = new BufferedReader(
              new InputStreamReader(ls2_proc.getInputStream()));
      BufferedReader errorReader = new BufferedReader(
              new InputStreamReader(ls2_proc.getErrorStream()));
      worker = new Worker(ls2_proc);
      worker.start();
      try
      {
        worker.join(timeout);
      } catch (InterruptedException e)
      {
        System.err.println("Thread interrupted");
      }
      worker.setOutputReader(outputReader);
      worker.setErrorReader(errorReader);
    }
    return worker;
  }

  @Test(groups = { "Functional", "testTask1" })
  public void reportCurrentWorkingDirectory()
  {
    try
    {
      Path currentRelativePath = Paths.get("");
      String s = currentRelativePath.toAbsolutePath().toString();
      System.out.println("Test CWD is " + s);
    } catch (Exception q)
    {
      q.printStackTrace();
    }
  }

  @BeforeClass(alwaysRun = true)
  public void initialize()
  {
    new CommandLineOperationsNG();
  }

  @BeforeClass(alwaysRun = true)
  public void setUpForHeadlessCommandLineInputOperations()
          throws IOException
  {
    String cmds = "--headless " + "--open examples/uniref50.fa "
            + "--sortbytree "
            + "--props test/jalview/bin/testProps.jvprops "
            + "--colour zappo "
            + "--jabaws http://www.compbio.dundee.ac.uk/jabaws "
            + "--features examples/testdata/plantfdx.features "
            + "--annotations examples/testdata/plantfdx.annotations "
            + "--tree examples/testdata/uniref50_test_tree "
            + "--nousagestats ";
    Worker worker = getJalviewDesktopRunner(true, cmds, SETUP_TIMEOUT);
    String ln = null;
    while ((ln = worker.getOutputReader().readLine()) != null)
    {
      System.out.println(ln);
      successfulCMDs.add(ln);
    }
    while ((ln = worker.getErrorReader().readLine()) != null)
    {
      System.err.println(ln);
    }
  }

  @BeforeClass(alwaysRun = true)
  public void setUpForCommandLineInputOperations() throws IOException
  {
    String cmds = "--open examples/uniref50.fa --noquestionnaire --nousagestats";
    final Worker worker = getJalviewDesktopRunner(false, cmds,
            SETUP_TIMEOUT);

    // number of lines expected on STDERR when Jalview starts up normally
    // may need to adjust this if Jalview is excessively noisy ?
    final int STDERR_SETUPLINES = 50;

    // thread monitors stderr - bails after SETUP_TIMEOUT or when
    // STDERR_SETUPLINES have been read
    Thread runner = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        String ln = null;
        int count = 0;
        try
        {
          while ((ln = worker.getOutputReader().readLine()) != null)
          {
            System.out.println(ln);
            successfulCMDs.add(ln);
            if (++count > STDERR_SETUPLINES)
            {
              break;
            }
          }
        } catch (Exception e)
        {
          System.err.println(
                  "Unexpected Exception reading stderr from the Jalview process");
          e.printStackTrace();
        }
      }
    });
    long t = System.currentTimeMillis() + SETUP_TIMEOUT;
    runner.start();
    while (!runner.isInterrupted() && System.currentTimeMillis() < t)
    {
      try
      {
        Thread.sleep(500);
      } catch (InterruptedException e)
      {
      }
    }
    runner.interrupt();
    if (worker != null && worker.exit == null)
    {
      worker.interrupt();
      Thread.currentThread().interrupt();
      worker.process.destroy();
    }
  }

  @Test(
    groups =
    { "Functional", "testTask1" },
    dataProvider = "allInputOperationsData")
  public void testAllInputOperations(String expectedString,
          String failureMsg)
  {
    Assert.assertTrue(successfulCMDs.contains(expectedString),
            failureMsg + "; was expecting '" + expectedString + "'");
  }

  @Test(
    groups =
    { "Functional", "testTask1" },
    dataProvider = "headlessModeOutputOperationsData")
  public void testHeadlessModeOutputOperations(String harg, String type,
          String fileName, boolean withAWT, int expectedMinFileSize,
          int timeout)
  {
    String cmd = harg + type + " " + fileName;
    // System.out.println(">>>>>>>>>>>>>>>> Command : " + cmd);
    File file = new File(fileName);
    file.deleteOnExit();
    Worker worker = getJalviewDesktopRunner(withAWT, cmd, timeout);
    assertNotNull(worker, "worker is null");
    String msg = "Didn't create an output" + type + " file '" + fileName
            + "'. [" + cmd + "]";
    assertTrue(file.exists(), msg);
    FileAssert.assertFile(file, msg);
    FileAssert.assertMinLength(file, expectedMinFileSize);
    if (worker != null && worker.exit == null)
    {
      worker.interrupt();
      Thread.currentThread().interrupt();
      worker.process.destroy();
      Assert.fail("Jalview did not exit after " + type
              + " generation (try running test again to verify - timeout at "
              + timeout + "ms). [" + harg + "]");
    }
    file.delete();
  }

  @DataProvider(name = "allInputOperationsData")
  public Object[][] getHeadlessModeInputParams()
  {
    return new Object[][] {
        // headless mode input operations
        { "[TESTOUTPUT] arg --colour='zappo' was set",
            "Failed setting arg --colour" },
        { "[TESTOUTPUT] arg --props='test/jalview/bin/testProps.jvprops' was set",
            "Failed setting arg --props" },
        { "[TESTOUTPUT] arg --sortbytree was set",
            "Failed setting arg --sortbytree" },
        { "[TESTOUTPUT] arg --jabaws='http://www.compbio.dundee.ac.uk/jabaws' was set",
            "Failed setting arg --jabaws" },
        { "[TESTOUTPUT] arg --open='examples/uniref50.fa' was set",
            "Failed setting arg --open" },
        { "[TESTOUTPUT] arg --features='examples/testdata/plantfdx.features' was set",
            "Failed setting arg --features" },
        { "[TESTOUTPUT] arg --annotations='examples/testdata/plantfdx.annotations' was set",
            "Failed setting arg --annotations" },
        { "[TESTOUTPUT] arg --tree='examples/testdata/uniref50_test_tree' was set",
            "Failed setting arg --tree" },
        // non headless mode input operations
        { "[TESTOUTPUT] arg --nousagestats was set",
            "Failed setting arg --nousagestats" },
        { "[TESTOUTPUT] arg --noquestionnaire was set",
            "Failed setting arg --noquestionnaire" }
        //
    };
  }

  @DataProvider(name = "headlessModeOutputOperationsData")
  public static Object[][] getHeadlessModeOutputParams()
  {
    // JBPNote: I'm not clear why need to specify full path for output file
    // when running tests on build server, but we will keep this patch for now
    // since it works.
    // https://issues.jalview.org/browse/JAL-1889?focusedCommentId=21609&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-21609
    String workingDir = "test/jalview/bin/";
    return new Object[][] {
        //
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.eps", true, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.eps", false, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.eps", true, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.eps", false, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.eps", true, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.svg", false, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.png", true, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --image",
            workingDir + "test_uniref50_out.html", true, MINFILESIZE_BIG,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.mfa", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.aln", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.msf", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.aln", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.pir", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.pfam", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.blc", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        { "--headless --open examples/uniref50.fa", " --output",
            workingDir + "test_uniref50_out.jvp", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT },
        //
    };
  }
}
