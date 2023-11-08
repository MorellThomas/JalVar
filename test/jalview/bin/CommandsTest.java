package jalview.bin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.util.ArrayUtils;

public class CommandsTest
{
  private static final String testfiles = "test/jalview/bin/argparser/testfiles";

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Cache.loadProperties("test/jalview/gui/quitProps.jvprops");
    Date oneHourFromNow = new Date(
            System.currentTimeMillis() + 3600 * 1000);
    Cache.setDateProperty("JALVIEW_NEWS_RSS_LASTMODIFIED", oneHourFromNow);
  }

  @AfterClass(alwaysRun = true)
  public static void resetProps()
  {
    Cache.loadProperties("test/jalview/testProps.jvprops");
  }

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    Desktop.closeDesktop();
  }
  
  public static void callJalviewMain(String[] args) {
    if (Jalview.getInstance()!=null) {
      Jalview.getInstance().doMain(args);
    } else {
      Jalview.main(args);
    }
  }

  /* --setprops is currently disabled so this test won't work
  @Test(groups = "Functional")
  public void setpropsTest()
  {
    final String MOSTLY_HARMLESS = "MOSTLY_HARMLESS";
    String cmdLine = "--setprop=" + MOSTLY_HARMLESS + "=Earth";
    String[] args = cmdLine.split("\\s+");
    Jalview.main(args);
    Assert.assertEquals(Cache.getDefault(MOSTLY_HARMLESS, "Magrathea"),
            "Earth");
  }
  */

  @Test(groups = "Functional", dataProvider = "cmdLines")
  public void commandsOpenTest(String cmdLine, boolean cmdArgs,
          int numFrames, String[] sequences)
  {
    try
    {
      String[] args = (cmdLine + " --gui").split("\\s+");
      callJalviewMain(args);
      Commands cmds = Jalview.getInstance().getCommands();
      Assert.assertNotNull(cmds);
      Assert.assertEquals(cmds.commandArgsProvided(), cmdArgs,
              "Commands were not provided in the args");
      Assert.assertEquals(cmds.argsWereParsed(), cmdArgs,
              "Overall command parse and operation is false");

      Assert.assertEquals(Desktop.getAlignFrames().length, numFrames,
              "Wrong number of AlignFrames");

      if (sequences != null)
      {
        Set<String> openedSequenceNames = new HashSet<>();
        AlignFrame[] afs = Desktop.getAlignFrames();
        for (AlignFrame af : afs)
        {
          openedSequenceNames.addAll(
                  af.getViewport().getAlignment().getSequenceNames());
        }
        for (String sequence : sequences)
        {
          Assert.assertTrue(openedSequenceNames.contains(sequence),
                  "Sequence '" + sequence
                          + "' was not found in opened alignment files: "
                          + cmdLine + ".\nOpened sequence names are:\n"
                          + String.join("\n", openedSequenceNames));
        }
      }

      Assert.assertFalse(
              lookForSequenceName("THIS_SEQUENCE_ID_DOESN'T_EXIST"));
    } catch (Exception x)
    {
      Assert.fail("Unexpected exception during commandsOpenTest", x);
    } finally
    {
      tearDown();

    }
  }

  @Test(groups = {"Functional","testTask1"}, dataProvider = "structureImageOutputFiles")
  public void structureImageOutputTest(String cmdLine, String[] filenames)
          throws IOException
  {
    cleanupFiles(filenames);
    String[] args = (cmdLine + " --gui").split("\\s+");
    try
    {
      callJalviewMain(args);
      Commands cmds = Jalview.getInstance().getCommands();
      Assert.assertNotNull(cmds);
      File lastFile = null;
      for (String filename : filenames)
      {
        File file = new File(filename);
        Assert.assertTrue(file.exists(), "File '" + filename
                + "' was not created by '" + cmdLine + "'");
        Assert.assertTrue(file.isFile(), "File '" + filename
                + "' is not a file from '" + cmdLine + "'");
        Assert.assertTrue(Files.size(file.toPath()) > 0, "File '" + filename
                + "' has no content from '" + cmdLine + "'");
        // make sure the successive output files get bigger!
        if (lastFile != null)
          Assert.assertTrue(Files.size(file.toPath()) > Files
                  .size(lastFile.toPath()));
      }
    } catch (Exception x)
    {
      Assert.fail("Unexpected exception during structureImageOutputTest",
              x);
    } finally
    {
      cleanupFiles(filenames);
      tearDown();
    }
  }

  @Test(groups = "Functional", dataProvider = "argfileOutputFiles")
  public void argFilesGlobAndSubstitutionsTest(String cmdLine,
          String[] filenames) throws IOException
  {
    cleanupFiles(filenames);
    String[] args = (cmdLine + " --gui").split("\\s+");
    try {
    callJalviewMain(args);
    Commands cmds = Jalview.getInstance().getCommands();
    Assert.assertNotNull(cmds);
    File lastFile = null;
    for (String filename : filenames)
    {
      File file = new File(filename);
      Assert.assertTrue(file.exists(), "File '" + filename
              + "' was not created by '" + cmdLine + "'");
      Assert.assertTrue(file.isFile(), "File '" + filename
              + "' is not a file from '" + cmdLine + "'");
      Assert.assertTrue(Files.size(file.toPath()) > 0, "File '" + filename
              + "' has no content from '" + cmdLine + "'");
      // make sure the successive output files get bigger!
      if (lastFile != null)
        Assert.assertTrue(
                Files.size(file.toPath()) > Files.size(lastFile.toPath()));
    }
    } catch (Exception x)
    {
      Assert.fail("Unexpected exception during argFilesGlobAndSubstitutions",
              x);
    } finally
    {
      cleanupFiles(filenames);
      tearDown();
    }
  }

  @DataProvider(name = "structureImageOutputFiles")
  public Object[][] structureImageOutputFiles()
  {
    return new Object[][] {
        //
        { "--gui --nonews --nosplash --open=./examples/test_fab41.result/sample.a2m "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--structureimage=" + testfiles + "/structureimage1.png "
                + "--open=./examples/test_fab41.result/sample.a2m "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--structureimage=" + testfiles
                + "/structureimage2.png --structureimagescale=1.5"
                + "--open=./examples/test_fab41.result/sample.a2m "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--structureimage=" + testfiles
                + "/structureimage3.png --structureimagescale=2.0",
            new String[]
            { testfiles + "/structureimage1.png",
                testfiles + "/structureimage2.png",
                testfiles + "/structureimage3.png" } },
        /*
        { "--headless --noquit --open=./examples/test_fab41.result/sample.a2m "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--structureimage=" + testfiles + "/structureimage1.png "
                + "--open=./examples/test_fab41.result/sample.a2m "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--structureimage=" + testfiles
                + "/structureimage2.png --structureimagescale=1.5"
                + "--open=./examples/test_fab41.result/sample.a2m "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--structureimage=" + testfiles
                + "/structureimage3.png --structureimagescale=2.0",
            new String[]
            { testfiles + "/structureimage1.png",
                testfiles + "/structureimage2.png",
                testfiles + "/structureimage3.png" } },
                */
        //
    };

  }

  @DataProvider(name = "argfileOutputFiles")
  public Object[][] argfileOutputFiles()
  {
    return new Object[][] {
        //
        { "--gui --argfile=" + testfiles + "/**/*.txt", new String[]
        { testfiles + "/dir1/test1.png", testfiles + "/dir2/test1.png",
            testfiles + "/dir3/subdir/test0.png" } },
        { "--gui --argfile=" + testfiles + "/**/argfile.txt", new String[]
        { testfiles + "/dir1/test1.png", testfiles + "/dir2/test1.png" } },
        { "--gui --argfile=" + testfiles + "/dir*/argfile.txt", new String[]
        { testfiles + "/dir1/test1.png", testfiles + "/dir2/test1.png" } },
        { "--gui --initsubstitutions --append examples/uniref50.fa --image "
                + testfiles + "/{basename}.png",
            new String[]
            { testfiles + "/uniref50.png" } },
        { "--gui --append examples/uniref50.fa --nosubstitutions --image "
                + testfiles + "/{basename}.png",
            new String[]
            { testfiles + "/{basename}.png" } }
        //
    };

  }

  @DataProvider(name = "cmdLines")
  public Object[][] cmdLines()
  {
    String[] someUniref50Seqs = new String[] { "FER_CAPAA", "FER_CAPAN",
        "FER1_MAIZE", "FER1_SPIOL", "O80429_MAIZE" };
    String[] t1 = new String[] { "TEST1" };
    String[] t2 = new String[] { "TEST2" };
    String[] t3 = new String[] { "TEST3" };
    return new Object[][] {
        /*
        */
        { "--append=examples/uniref50.fa", true, 1, someUniref50Seqs },
        { "--append examples/uniref50.fa", true, 1, someUniref50Seqs },
        { "--append=examples/uniref50*.fa", true, 1, someUniref50Seqs },
        // NOTE we cannot use shell expansion in tests, so list all files!
        { "--append examples/uniref50.fa examples/uniref50_mz.fa", true, 1,
            someUniref50Seqs },
        { "--append=[new]examples/uniref50*.fa", true, 2,
            someUniref50Seqs },
        { "--open=examples/uniref50*.fa", true, 2, someUniref50Seqs },
        { "examples/uniref50.fa", true, 1, someUniref50Seqs },
        { "examples/uniref50.fa " + testfiles + "/test1.fa", true, 2,
            ArrayUtils.concatArrays(someUniref50Seqs, t1) },
        { "examples/uniref50.fa " + testfiles + "/test1.fa", true, 2, t1 },
        { "--gui --argfile=" + testfiles + "/argfile0.txt", true, 1,
            ArrayUtils.concatArrays(t1, t3) },
        { "--gui --argfile=" + testfiles + "/argfile*.txt", true, 5,
            ArrayUtils.concatArrays(t1, t2, t3) },
        { "--gui --argfile=" + testfiles + "/argfile.autocounter", true, 3,
            ArrayUtils.concatArrays(t1, t2) } };

  }

  public static boolean lookForSequenceName(String sequenceName)
  {
    AlignFrame[] afs = Desktop.getAlignFrames();
    for (AlignFrame af : afs)
    {
      for (String name : af.getViewport().getAlignment().getSequenceNames())
      {
        if (sequenceName.equals(name))
        {
          return true;
        }
      }
    }
    return false;
  }

  public static void cleanupFiles(String[] filenames)
  {
    for (String filename : filenames)
    {
      File file = new File(filename);
      if (file.exists())
      {
        file.delete();
      }
    }
  }

  @Test(
    groups = "Functional",
    dataProvider = "allLinkedIdsData",
    singleThreaded = true)
  public void allLinkedIdsTest(String cmdLine, String[] filenames,
          String[] nonfilenames)
  {
    String[] args = (cmdLine + " --gui").split("\\s+");
    callJalviewMain(args);
    Commands cmds = Jalview.getInstance().getCommands();
    Assert.assertNotNull(cmds);
    for (String filename : filenames)
    {
      Assert.assertTrue(new File(filename).exists(),
              "File '" + filename + "' was not created");
    }
    cleanupFiles(filenames);
    if (nonfilenames != null)
    {
      for (String nonfilename : nonfilenames)
      {
        File nonfile = new File(nonfilename);
        Assert.assertFalse(nonfile.exists(),
                "File " + nonfilename + " exists when it shouldn't!");
      }
    }
  }

  @DataProvider(name = "allLinkedIdsData")
  public Object[][] allLinkedIdsData()
  {
    return new Object[][] {
        //
        /*
         */
        { "--gui --open=test/jalview/bin/argparser/testfiles/*.fa --substitutions --all --output={dirname}/{basename}.stk --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk", },
            null },
        { "--gui --open=test/jalview/bin/argparser/testfiles/*.fa --substitutions --all --image={dirname}/{basename}.png --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.png",
                "test/jalview/bin/argparser/testfiles/test2.png",
                "test/jalview/bin/argparser/testfiles/test3.png", },
            null },
        { "--gui --open=test/jalview/bin/argparser/testfiles/*.fa --all --output={dirname}/{basename}.stk --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk", },
            new String[]
            { "test/jalview/bin/argparser/testfiles/dir1/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.stk", }, },
        { "--gui --open=test/jalview/bin/argparser/**/*.fa --all --output={dirname}/{basename}.stk --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.stk", },
            null },
        { "--gui --open=test/jalview/bin/argparser/**/*.fa --output=*.stk --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.stk", },
            null },
        { "--gui --open=test/jalview/bin/argparser/testfiles/dir1/*.fa --open=test/jalview/bin/argparser/testfiles/dir2/*.fa --output=*.stk --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/dir1/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test3.stk", },
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.stk", }, },
        { "--gui --open=test/jalview/bin/argparser/testfiles/dir1/*.fa --open=test/jalview/bin/argparser/testfiles/dir2/*.fa --output=open*.stk --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/dir2/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test3.stk", },
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.stk", }, },
        { "--gui --open=test/jalview/bin/argparser/testfiles/dir1/*.fa --open=test/jalview/bin/argparser/testfiles/dir2/*.fa --opened --output={dirname}/{basename}.stk --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/dir2/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test3.stk", },
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.stk", }, },
        { "--gui --open=test/jalview/bin/argparser/testfiles/dir1/*.fa --output open*.stk --open=test/jalview/bin/argparser/testfiles/dir2/*.fa --output=open*.aln --close",
            new String[]
            { "test/jalview/bin/argparser/testfiles/dir1/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir1/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test1.aln",
                "test/jalview/bin/argparser/testfiles/dir2/test2.aln",
                "test/jalview/bin/argparser/testfiles/dir2/test3.aln", },
            new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir2/test3.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.stk",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.stk",
                "test/jalview/bin/argparser/testfiles/test1.aln",
                "test/jalview/bin/argparser/testfiles/test2.aln",
                "test/jalview/bin/argparser/testfiles/test3.aln",
                "test/jalview/bin/argparser/testfiles/dir1/test1.aln",
                "test/jalview/bin/argparser/testfiles/dir1/test2.aln",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test0.aln",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test1.aln",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test2.aln",
                "test/jalview/bin/argparser/testfiles/dir3/subdir/test3.aln", }, },
        //
    };
  }

}
