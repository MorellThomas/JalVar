package jalview.bin.argparser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.gui.Desktop;

@Test(singleThreaded = true)
public class ArgParserTest
{
  @AfterClass(alwaysRun = true)
  public static void resetProps()
  {
    Cache.loadProperties("test/jalview/testProps.jvprops");
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @Test(groups = "Functional", dataProvider = "argLines")
  public void parseArgsTest(String commandLineArgs, Arg a, String other)
  {
    String[] args = commandLineArgs.split("\\s+");
    ArgParser argparser = new ArgParser(args);
  }

  @Test(groups = "Functional", dataProvider = "argSubValsAndLinkedIds")
  public void parseSubValsAndLinkedIdsTest(String commandLineArgs,
          String linkedId, Arg a, String subvalKey, String value,
          boolean trueOrFalse)
  {
    String[] args = commandLineArgs.split("\\s+");
    ArgParser argparser = new ArgParser(args);
    ArgValuesMap avm = argparser.getLinkedArgs(linkedId);
    ArgValue av = avm.getArgValue(a);
    SubVals sv = av.getSubVals();
    String testString = null;
    if (subvalKey.equals("GETINDEX"))
    {
      testString = String.valueOf(sv.getIndex());
    }
    else
    {
      testString = sv.get(subvalKey);
    }
    if (trueOrFalse)
    {
      Assert.assertEquals(testString, value);
    }
    else
    {
      Assert.assertNotEquals(testString, value);
    }
  }

  @Test(
    groups = "Functional",
    dataProvider = "argAutoIndexAndSubstitutions")
  public void parseAutoIndexAndSubstitutionsTest(String commandLineArgs,
          String linkedId, Arg a, String filename)
  {
    // { "--append=filename0 --new --append=filename1", "JALVIEW:1",
    // Arg.OPEN, "filename1" },
    String[] args = commandLineArgs.split("\\s+");
    ArgParser argparser = new ArgParser(args);
    ArgValuesMap avm = argparser.getLinkedArgs(linkedId);
    ArgValue av = avm.getArgValue(a);
    Assert.assertEquals(av.getValue(), filename);
  }

  @Test(groups = "Functional", dataProvider = "argLines")
  public void bootstrapArgsTest(String commandLineArgs, Arg a, String other)
  {
    String[] args = commandLineArgs.split("\\s+");
    BootstrapArgs b = BootstrapArgs.getBootstrapArgs(args);

    Assert.assertTrue(b.contains(a));
    if (a == Arg.PROPS)
    {
      Properties bP = Cache.bootstrapProperties(b.getValue(Arg.PROPS));
      Assert.assertNotNull(bP);
      Assert.assertTrue(other.equals(bP.get(Cache.BOOTSTRAP_TEST)));
      Assert.assertFalse(bP.contains("NOT" + Cache.BOOTSTRAP_TEST));
    }
    else if (a == Arg.ARGFILE)
    {
      List<String> filenames = b.getValueList(a);
      boolean found = false;
      for (String s : filenames)
      {
        File f = new File(s);
        File fo = new File(other);
        try
        {
          if (fo.getCanonicalPath().equals(f.getCanonicalPath()))
          {
            found = true;
            break;
          }
        } catch (IOException e)
        {
        }
      }
      Assert.assertTrue(found,
              "File '" + other + "' not found in shell expanded glob '"
                      + commandLineArgs + "'");
    }
  }

  @Test(groups = "Functional", dataProvider = "argFiles")
  public void argFilesTest(String commandLineArgs, Arg a, String other)
  {
    String[] args = commandLineArgs.split("\\s+");
    BootstrapArgs b = BootstrapArgs.getBootstrapArgs(args);

    Assert.assertTrue(b.contains(a));
    Assert.assertFalse(b.contains(Arg.APPEND));
    if (a == Arg.PROPS)
    {
      Properties bP = Cache.bootstrapProperties(b.getValue(Arg.PROPS));
      Assert.assertTrue("true".equals(bP.get(Cache.BOOTSTRAP_TEST)));
    }
  }

  @DataProvider(name = "argLinesNotworking")
  public Object[][] argLinesTest()
  {
    return new Object[][] {
        // can't use this one yet as it doesn't get shell glob expanded by the
        // test
        { "--argfile test/jalview/bin/argparser/testfiles/argfile*.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile0.txt" }, };
  }

  @DataProvider(name = "argLines")
  public Object[][] argLines()
  {
    return new Object[][] { {
        "--append=test/jalview/bin/argparser/testfiles/test1.fa --props=test/jalview/bin/argparser/testfiles/testProps.jvprops",
        Arg.PROPS, "true" },
        { "--debug --append=test/jalview/bin/argparser/testfiles/test1.fa",
            Arg.DEBUG, null },
        { "--append=test/jalview/bin/argparser/testfiles/test1.fa --headless",
            Arg.HEADLESS, null },

        { "--argfile test/jalview/bin/argparser/testfiles/argfile0.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile0.txt" },
        // these next three are what a shell glob expansion would look like
        { "--argfile test/jalview/bin/argparser/testfiles/argfile0.txt test/jalview/bin/argparser/testfiles/argfile1.txt test/jalview/bin/argparser/testfiles/argfile2.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile0.txt" },
        { "--argfile test/jalview/bin/argparser/testfiles/argfile0.txt test/jalview/bin/argparser/testfiles/argfile1.txt test/jalview/bin/argparser/testfiles/argfile2.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile1.txt" },
        { "--argfile test/jalview/bin/argparser/testfiles/argfile0.txt test/jalview/bin/argparser/testfiles/argfile1.txt test/jalview/bin/argparser/testfiles/argfile2.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile2.txt" },
        { "--argfile=test/jalview/bin/argparser/testfiles/argfile*.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile0.txt" },
        { "--argfile=test/jalview/bin/argparser/testfiles/argfile*.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile1.txt" },
        { "--argfile=test/jalview/bin/argparser/testfiles/argfile*.txt",
            Arg.ARGFILE,
            "test/jalview/bin/argparser/testfiles/argfile2.txt" } };
  }

  @DataProvider(name = "argSubValsAndLinkedIds")
  public Object[][] argSubValsAndLinkedIds()
  {
    return new Object[][] {
        //
        /*
         */
        { "--debug --append=[hi]test/jalview/bin/argparser/testfiles/test1.fa",
            "JALVIEW:0", Arg.APPEND, "hi", "true", true },
        { "--append[linkedId1]=[new,hello=world,1]test/jalview/bin/argparser/testfiles/test1.fa --headless",
            "linkedId1", Arg.APPEND, "new", "true", true },
        { "--append[linkedId2]=[new,hello=world,1]test/jalview/bin/argparser/testfiles/test1.fa --headless",
            "linkedId2", Arg.APPEND, "hello", "world", true },
        { "--append[linkedId3]=[new,hello=world,1]test/jalview/bin/argparser/testfiles/test1.fa --headless",
            "linkedId3", Arg.APPEND, "GETINDEX", "1", true },
        { "--append[linkedId4]=[new,hello=world,1]test/jalview/bin/argparser/testfiles/test1.fa --append[linkedId5]=[notnew;hello=world;1]test/jalview/bin/argparser/testfiles/test1.fa --headless",
            "linkedId5", Arg.APPEND, "new", "true", false },
        { "--append[linkedId5]=[new,hello=worlddomination,1]test/jalview/bin/argparser/testfiles/test1.fa --append[linkedId2]=[new;hello=world;1]test/jalview/bin/argparser/testfiles/test1.fa --headless",
            "linkedId5", Arg.APPEND, "hello", "world", false },
        { "--append[linkedId6]=[new,hello=world,0]test/jalview/bin/argparser/testfiles/test1.fa --append[linkedId7]=[new;hello=world;1]test/jalview/bin/argparser/testfiles/test1.fa --headless",
            "linkedId7", Arg.APPEND, "GETINDEX", "0", false },
        /*
         */
        //
    };
  }

  @DataProvider(name = "argAutoIndexAndSubstitutions")
  public Object[][] argAutoIndexAndSubstitutions()
  {
    return new Object[][] {
        //
        /*
         */
        { "--append=filename0 --append=filename1", "JALVIEW:0", Arg.APPEND,
            "filename0" },
        { "--append=filename0 --new --append=filename1", "JALVIEW:1",
            Arg.APPEND, "filename1" },
        { "--append=filename0 --new --new --append=filename2", "JALVIEW:0",
            Arg.APPEND, "filename0" },
        { "--append=filename0 --new --new --append=filename2", "JALVIEW:2",
            Arg.APPEND, "filename2" },
        { "--append[linkA-{n}]=filenameA0 --append[linkA-{++n}]=filenameA1",
            "linkA-0", Arg.APPEND, "filenameA0" },
        { "--append[linkB-{n}]=filenameB0 --append[linkB-{++n}]=filenameB1",
            "linkB-1", Arg.APPEND, "filenameB1" },
        { "--append[linkC-{n}]=filenameC0 --image[linkC-{n}]=outputC{n}.txt",
            "linkC-0", Arg.IMAGE, "outputC{n}.txt" },
        { "--append[linkD-{n}]=filenameD0 --substitutions --image[linkD-{n}]=outputD{n}.txt",
            "linkD-0", Arg.IMAGE, "outputD0.txt" },
        { "--append[linkE-{n}]=filenameE0 --substitutions --image[linkE-{n}]=output-E{n}.txt --nil[{++n}] --image[linkE-{n}]=outputE{n}.txt",
            "linkE-0", Arg.IMAGE, "output-E0.txt" },
        { "--append[linkF-{n}]=filenameF0 --substitutions --image[linkF-{n}]=output-F{n}.txt --nil[{++n}] --image[linkF-{n}]=outputF{n}.txt",
            "linkF-1", Arg.IMAGE, "outputF1.txt" },
        { "--append[linkG-{n}]=filenameG0 --substitutions --image[linkG-{n}]=output-G{n}.txt --nil[{++n}] --nosubstitutions --image[linkG-{n}]=outputG{n}.txt",
            "linkG-1", Arg.IMAGE, "outputG{n}.txt" },
        { "--append[linkH-{n}]=filenameH0 --substitutions --image[linkH-{n}]=output-H{n}.txt --nil[{++n}] --nosubstitutions --image[linkH-{n}]=outputH{n}.txt",
            "linkH-0", Arg.IMAGE, "output-H0.txt" },
        { "--open=filename0 --append=filename1", "JALVIEW:0", Arg.OPEN,
            "filename0" },
        { "--open=filename0 --new --append=filename1", "JALVIEW:1",
            Arg.APPEND, "filename1" },
        { "--open=filename0 --new --new --append=filename2", "JALVIEW:0",
            Arg.OPEN, "filename0" },
        { "--open=filename0 --new --new --append=filename2", "JALVIEW:2",
            Arg.APPEND, "filename2" },
        { "--open[linkA-{n}]=filenameA0 --append[linkA-{++n}]=filenameA1",
            "linkA-0", Arg.OPEN, "filenameA0" },
        { "--open[linkB-{n}]=filenameB0 --append[linkB-{++n}]=filenameB1",
            "linkB-1", Arg.APPEND, "filenameB1" },
        { "--open[linkC-{n}]=filenameC0 --image[linkC-{n}]=outputC{n}.txt",
            "linkC-0", Arg.IMAGE, "outputC{n}.txt" },
        { "--open[linkD-{n}]=filenameD0 --substitutions --image[linkD-{n}]=outputD{n}.txt",
            "linkD-0", Arg.IMAGE, "outputD0.txt" },
        { "--open[linkE-{n}]=filenameE0 --substitutions --image[linkE-{n}]=output-E{n}.txt --nil[{++n}] --image[linkE-{n}]=outputE{n}.txt",
            "linkE-0", Arg.IMAGE, "output-E0.txt" },
        { "--open[linkF-{n}]=filenameF0 --substitutions --image[linkF-{n}]=output-F{n}.txt --nil[{++n}] --image[linkF-{n}]=outputF{n}.txt",
            "linkF-1", Arg.IMAGE, "outputF1.txt" },
        { "--open[linkG-{n}]=filenameG0 --substitutions --image[linkG-{n}]=output-G{n}.txt --nil[{++n}] --nosubstitutions --image[linkG-{n}]=outputG{n}.txt",
            "linkG-1", Arg.IMAGE, "outputG{n}.txt" },
        { "--open[linkH-{n}]=filenameH0 --substitutions --image[linkH-{n}]=output-H{n}.txt --nil[{++n}] --nosubstitutions --image[linkH-{n}]=outputH{n}.txt",
            "linkH-0", Arg.IMAGE, "output-H0.txt" },
        /*
         */

        //
    };
  }

  @DataProvider(name = "argFiles")
  public Object[][] argFiles()
  {
    return new Object[][] { {
        "--argfile=test/jalview/bin/argparser/testfiles/argfile0.txt --open=shouldntbeabootstrap",
        Arg.ARGFILE, "test/jalview/bin/argfiles/testfiles/test1.fa" } };
  }

  @Test(groups = "Functional", dataProvider = "allLinkedIdsData")
  public void allLinkedIdsTest(String commandLineArgs, Arg a,
          String[] values, String[] nonvalues)
  {
    String[] args = commandLineArgs.split("\\s+");
    ArgParser argparser = new ArgParser(args);

    int num = values.length;
    List<String> linkedIds = argparser.getLinkedIds();
    Assert.assertEquals(linkedIds.size(), num,
            "Wrong number of linkedIds: " + linkedIds.toString());
    for (int i = 0; i < num; i++)
    {
      String value = values[i];
      String linkedId = linkedIds.get(i);
      ArgValuesMap avm = argparser.getLinkedArgs(linkedId);
      if (value == null)
      {
        Assert.assertTrue(avm.containsArg(a),
                "Arg value for " + a.argString()
                        + " not applied correctly to linkedId '" + linkedId
                        + "'");
      }
      else
      {
        ArgValues avs = avm.getArgValues(a);
        ArgValue av = avs.getArgValue();
        String v = av.getValue();
        value = new File(value).getAbsolutePath();
        Assert.assertEquals(v, value, "Arg value for " + a.argString()
                + " not applied correctly to linkedId '" + linkedId + "'");
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
        { "--open=test/jalview/bin/argparser/testfiles/*.fa --substitutions --all --image={dirname}/{basename}.png --close",
            Arg.CLOSE, new String[]
            { null, null, null },
            null },
        { "--open=test/jalview/bin/argparser/testfiles/*.fa --substitutions --all --output={dirname}/{basename}.stk --close",
            Arg.OUTPUT, new String[]
            { "test/jalview/bin/argparser/testfiles/test1.stk",
                "test/jalview/bin/argparser/testfiles/test2.stk",
                "test/jalview/bin/argparser/testfiles/test3.stk", },
            null },
        { "--open=test/jalview/bin/argparser/testfiles/*.fa --substitutions --all --image={dirname}/{basename}.png --close",
            Arg.IMAGE, new String[]
            { "test/jalview/bin/argparser/testfiles/test1.png",
                "test/jalview/bin/argparser/testfiles/test2.png",
                "test/jalview/bin/argparser/testfiles/test3.png", },
            null },
        //
    };
  }

  @Test(groups = "Functional", dataProvider = "bootstrapArgsData")
  public void bootstrapArgsValuesAndHeadlessModeTest(String commandLineArgs,
          Arg a, String valS, boolean valB, boolean headlessValue)
  {
    String[] args = commandLineArgs.split("\\s+");
    BootstrapArgs bsa = BootstrapArgs.getBootstrapArgs(args);
    if (a != null)
    {
      if (valS != null)
      {
        Assert.assertEquals(bsa.getValue(a), valS,
                "BootstrapArg " + a.argString()
                        + " value does not match expected '" + valS + "'");
      }
      else
      {
        Assert.assertEquals(bsa.getBoolean(a), valB,
                "Boolean/Unary value of BootstrapArg " + a.argString()
                        + "' is not the expected '" + valB + "'");
      }
    }

    boolean isHeadless = bsa.isHeadless();
    Assert.assertEquals(isHeadless, headlessValue,
            "Assumed headless setting '" + isHeadless + "' is wrong.");
  }

  @DataProvider(name = "bootstrapArgsData")
  public Object[][] bootstrapArgsData()
  {
    return new Object[][] {
        /*
         * cmdline args
         * Arg (null if only testing headless)
         * String value if there is one (null otherwise)
         * boolean value if String value is null
         * expected value of isHeadless()
         */
        /*
        */
        { "--open thisway.fa --output thatway.fa --jabaws https://forwardsandbackwards.com/",
            Arg.JABAWS, "https://forwardsandbackwards.com/", false, true },
        { "--help-all --open thisway.fa --output thatway.fa --jabaws https://forwardsandbackwards.com/",
            Arg.HELP, null, true, true },
        { "--help-all --nonews --open thisway.fa --output thatway.fa --jabaws https://forwardsandbackwards.com/",
            Arg.NEWS, null, false, true },
        { "--help --nonews --open thisway.fa --output thatway.fa --jabaws https://forwardsandbackwards.com/",
            Arg.NEWS, null, false, true },
        { "--help-opening --nonews --open thisway.fa --output thatway.fa --jabaws https://forwardsandbackwards.com/",
            Arg.NEWS, null, false, true },
        { "--nonews --open thisway.fa --output thatway.fa --jabaws https://forwardsandbackwards.com/",
            Arg.NEWS, null, false, true },
        { "--open thisway.fa --image thatway.png", null, null, false,
            true },
        { "--open thisway.fa --output thatway.png", null, null, false,
            true },
        { "--open thisway.fa --image thatway.png --noheadless", null, null,
            false, false },
        { "--open thisway.fa --output thatway.png --noheadless", null, null,
            false, false },
        { "--open thisway.fa --image thatway.png --gui", null, null, false,
            false },
        { "--open thisway.fa --output thatway.png --gui", null, null, false,
            false },
        // --gui takes precedence
        { "--open thisway.fa --image thatway.png --gui --headless", null,
            null, false, false },
        { "--open thisway.fa --output thatway.png --gui --headless", null,
            null, false, false },
        //
    };
  }

}
