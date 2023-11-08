package jalview.gui;

import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.gui.QuitHandler.QResponse;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileLoader;
import jalview.project.Jalview2XML;

@Test(singleThreaded = true)
public class QuitHandlerTest
{
  private static String saveProjectFile = "test-output/tempSaveFile.jvp";

  private static String saveFastaFile = "test-output/tempSaveFile.fa";

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
    Jalview2XML.setDebugDelaySave(3);
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Cache.loadProperties("test/jalview/gui/quitProps.jvprops");

    /*
     * set news feed last read to a future time to ensure no
     * 'unread' news item is displayed
     */
    Date oneHourFromNow = new Date(
            System.currentTimeMillis() + 3600 * 1000);
    Cache.setDateProperty("JALVIEW_NEWS_RSS_LASTMODIFIED", oneHourFromNow);

    Jalview.main(
            new String[]
            { "-nowebservicediscovery", "-nosplash", "-nonews" });
  }

  @AfterClass(alwaysRun = true)
  public static void resetProps()
  {
    // reset quit response
    QuitHandler.setResponse(QResponse.NULL);
    // reset mock response
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
    // close desktop windows/frames
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
    // reset debug delay
    Jalview2XML.setDebugDelaySave(20);
    // load normal testprops
    Cache.loadProperties("test/jalview/testProps.jvprops");
  }

  @BeforeMethod(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    // reset quit response
    QuitHandler.setResponse(QResponse.NULL);
    // reset mock response
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
    // close desktop windows/frames
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
    // reset debug delay
    Cache.setProperty("DEBUG_DELAY_SAVE", "false");
    Jalview2XML.setDebugDelaySave(3);
    // set the project file
    Desktop.instance.setProjectFile(new File(saveProjectFile));
  }

  @AfterMethod(alwaysRun = true)
  public static void cleanup()
  {
    // delete save files
    List<String> files = new ArrayList<>();
    files.add(saveProjectFile);
    files.add(saveFastaFile);
    for (String filename : files)
    {
      File file = new File(filename);
      if (file.exists())
      {
        file.delete();
      }
    }
  }

  @Test(groups = { "Functional" }, singleThreaded = true, priority = 1)
  public void testInstantQuit() throws Exception
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);

    long start = System.currentTimeMillis();

    // if a save is attempted it will delay 3s
    Jalview2XML.setDebugDelaySave(3);
    Cache.setProperty("DEBUG_DELAY_SAVE", "true");

    // loaded file but haven't done anything, should just quit
    QResponse response = QuitHandler.getQuitResponse(true);
    long end = System.currentTimeMillis();

    Assert.assertEquals(response, QResponse.QUIT);
    Assert.assertTrue(end - start < 500,
            "Quit-with-no-save-needed took too long (" + (end - start)
                    + "ms)");
  }

  @Test(groups = { "Functional" }, singleThreaded = true, priority = 10)
  public void testWaitForSaveQuit() throws Exception
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);

    long start = System.currentTimeMillis();

    // start a long save (3s)
    Jalview2XML.setDebugDelaySave(3);
    Cache.setProperty("DEBUG_DELAY_SAVE", "true");
    Desktop.instance.saveState_actionPerformed(false);

    // give the saveState thread time to start!
    Thread.sleep(500);

    // af.saveAlignment(saveProjectFile, FileFormat.Jalview);
    QResponse response = QuitHandler.getQuitResponse(true);
    long end = System.currentTimeMillis();

    Assert.assertEquals(response, QResponse.QUIT);
    Assert.assertTrue(end - start > 2900,
            "Quit-whilst-saving was too short (" + (end - start) + "ms)");
  }

  @Test(groups = { "Functional" }, singleThreaded = true, priority = 9)
  public void testSavedProjectChanges() throws Exception
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);
    AlignViewport viewport = af.getViewport();
    // pretend something has happened
    viewport.setSavedUpToDate(false);
    Jalview2XML.setStateSavedUpToDate(false);

    // don't want to hang around here
    Cache.setProperty("DEBUG_DELAY_SAVE", "false");
    af.saveAlignment(saveProjectFile, FileFormat.Jalview);

    // this is only a two button dialog [Quit] [Cancel] so use NO_OPTION (to
    // mean [CANCEL] -- file should already be saved so this doesn't happen and
    // we get a QUIT response)
    JvOptionPane.setMockResponse(JvOptionPane.NO_OPTION);
    QResponse response = QuitHandler.getQuitResponse(true);

    // if not saved this would be CANCEL_QUIT
    Assert.assertEquals(response, QResponse.QUIT);
  }

  @Test(groups = { "Functional" }, singleThreaded = true, priority = 9)
  public void testSavedAlignmentChanges() throws Exception
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);
    AlignViewport viewport = af.getViewport();
    // pretend something has happened
    viewport.setSavedUpToDate(false);
    Jalview2XML.setStateSavedUpToDate(false);

    // no hanging around needed here
    Cache.setProperty("DEBUG_DELAY_SAVE", "false");
    af.saveAlignment(saveFastaFile, FileFormat.Fasta);

    // this is only a two button dialog [Quit] [Cancel] so use NO_OPTION
    JvOptionPane.setMockResponse(JvOptionPane.NO_OPTION);
    QResponse response = QuitHandler.getQuitResponse(true);

    // if not saved this would be CANCEL_QUIT
    Assert.assertEquals(response, QResponse.QUIT);
  }

  @Test(groups = { "Functional" }, singleThreaded = true, priority = 1)
  public void testUnsavedChanges() throws Exception
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);
    AlignViewport viewport = af.getViewport();
    // pretend something has happened
    viewport.setSavedUpToDate(false);
    Jalview2XML.setStateSavedUpToDate(false);

    // this is only a two button dialog [Quit] [Cancel] so use NO_OPTION
    JvOptionPane.setMockResponse(JvOptionPane.NO_OPTION);
    QResponse response = QuitHandler.getQuitResponse(true);

    Assert.assertEquals(response, QResponse.CANCEL_QUIT);
  }

  @Test(groups = { "Functional" }, singleThreaded = true, priority = 1)
  public void testNoGUIUnsavedChanges() throws Exception
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);
    AlignViewport viewport = af.getViewport();
    // pretend something has happened
    viewport.setSavedUpToDate(false);
    Jalview2XML.setStateSavedUpToDate(false);

    // this is only a two button dialog [Quit] [Cancel] so use NO_OPTION
    JvOptionPane.setMockResponse(JvOptionPane.NO_OPTION);
    /*
    QResponse response = QuitHandler.getQuitResponse(false,
            QuitHandler.defaultOkQuit, () -> {
              // set FORCE_QUIT without the force quit
              QuitHandler.setResponse(QResponse.FORCE_QUIT);
              return null;
            }, QuitHandler.defaultCancelQuit);
            */
    QResponse response = QuitHandler.getQuitResponse(false);

    Assert.assertEquals(response, QResponse.QUIT);
  }

  @Test(groups = { "Functional" }, singleThreaded = true, priority = 11)
  public void testForceQuit() throws Exception
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);

    long start = System.currentTimeMillis();

    // start a long save (10s)
    Jalview2XML.setDebugDelaySave(10);
    Cache.setProperty("DEBUG_DELAY_SAVE", "true");
    Desktop.instance.saveState_actionPerformed(false);

    // give the saveState thread time to start!
    Thread.sleep(100);

    // this will select "Force Quit"
    JvOptionPane.setMockResponse(JvOptionPane.YES_OPTION);
    QResponse response = QuitHandler.getQuitResponse(true,
            QuitHandler.defaultOkQuit, () -> {
              // set FORCE_QUIT without the force quit
              jalview.bin.Console.debug(
                      "Setting FORCE_QUIT without actually quitting");
              QuitHandler.setResponse(QResponse.FORCE_QUIT);
            }, QuitHandler.defaultCancelQuit);
    long end = System.currentTimeMillis();

    Assert.assertEquals(response, QResponse.FORCE_QUIT);
    // if the wait (min wait is 1s) wasn't long enough...
    Assert.assertTrue(end - start > 1000,
            "Force-Quit-whilst-saving was too short (" + (end - start)
                    + "ms)");
    // if the wait was too long (probably waited for file to save)
    Assert.assertTrue(end - start < 9090,
            "Force-Quit-whilst-saving was too long (" + (end - start)
                    + "ms)");

  }

}
