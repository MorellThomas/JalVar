package jalview.bin;

import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignmentPanel;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.StructureViewerBase;

@Test
public class CommandsTest2
{
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Cache.loadProperties("test/jalview/bin/commandsTest.jvprops");
    Date oneHourFromNow = new Date(
            System.currentTimeMillis() + 3600 * 1000);
    Cache.setDateProperty("JALVIEW_NEWS_RSS_LASTMODIFIED", oneHourFromNow);
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);

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

  @Test(
    groups =
    { "Functional", "testTask1" },
    dataProvider = "structureOpeningArgsParams",
    singleThreaded = true)
  public void structureOpeningArgsTest(String cmdLine, int seqNum,
          int annNum, int viewerNum)
  {
    String[] args = cmdLine.split("\\s+");

    CommandsTest.callJalviewMain(args);
    while (Desktop.instance!=null && Desktop.instance.operationsAreInProgress())
    {
      try
      {
        // sleep for slow build server to open annotations and viewer windows
        Thread.sleep(viewerNum * 50);
      } catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
    ;

    AlignFrame[] afs = Desktop.getAlignFrames();
    Assert.assertNotNull(afs);
    Assert.assertTrue(afs.length > 0);

    AlignFrame af = afs[0];
    Assert.assertNotNull(af);

    AlignmentPanel ap = af.alignPanel;
    Assert.assertNotNull(ap);

    AlignmentI al = ap.getAlignment();
    Assert.assertNotNull(al);

    List<SequenceI> seqs = al.getSequences();
    Assert.assertNotNull(seqs);

    Assert.assertEquals(seqs.size(), seqNum, "Wrong number of sequences");

    AlignViewportI av = ap.getAlignViewport();
    Assert.assertNotNull(av);

    AlignmentAnnotation[] aas = al.getAlignmentAnnotation();
    int visibleAnn = 0;
    int dcount = 0;
    for (AlignmentAnnotation aa : aas)
    {
      if (aa.visible)
        visibleAnn++;
    }

    Assert.assertEquals(visibleAnn, annNum,
            "Wrong number of visible annotations");

    if (viewerNum > -1)
    {
      List<StructureViewerBase> openViewers = Desktop.instance
              .getStructureViewers(ap, null);
      Assert.assertNotNull(openViewers);
      int count = 0;
      for (StructureViewerBase svb : openViewers)
      {
        if (svb.isVisible())
          count++;
      }
      Assert.assertEquals(count, viewerNum,
              "Wrong number of structure viewers opened");
    }
  }

  @DataProvider(name = "structureOpeningArgsParams")
  public Object[][] structureOpeningArgsParams()
  {
    /*
      String cmdLine,
      int seqNum,
      int annNum,
      int structureViewerNum,
     */
    return new Object[][] {
        //
        /*
         */
        { "--gui --nonews --nosplash --debug "
                + "--append=examples/uniref50.fa "
                + "--colour=gecos-flower "
                + "--structure=[seqid=FER1_SPIOL]examples/AlphaFold/AF-P00221-F1-model_v4.cif "
                + "--paematrix=examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json "
                + "--props=test/jalview/bin/commandsTest2.jvprops1 ",
            15, 7, 1 },
        { "--gui --nonews --nosplash --debug "
                + "--append=examples/uniref50.fa "
                + "--colour=gecos-flower "
                + "--structure=[seqid=FER1_SPIOL]examples/AlphaFold/AF-P00221-F1-model_v4.cif "
                + "--paematrix=examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json "
                + "--props=test/jalview/bin/commandsTest2.jvprops2 ",
            15, 4, 1 },
        { "--gui --nonews --nosplash --debug "
                + "--append=examples/uniref50.fa "
                + "--colour=gecos-flower "
                + "--structure=[seqid=FER1_SPIOL]examples/AlphaFold/AF-P00221-F1-model_v4.cif "
                + "--paematrix=examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json "
                + "--noshowssannotations "
                + "--props=test/jalview/bin/commandsTest2.jvprops1 ",
            15, 4, 1 },
        { "--gui --nonews --nosplash --debug "
                + "--append=examples/uniref50.fa "
                + "--colour=gecos-flower "
                + "--structure=[seqid=FER1_SPIOL]examples/AlphaFold/AF-P00221-F1-model_v4.cif "
                + "--paematrix=examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json "
                + "--noshowannotations "
                + "--props=test/jalview/bin/commandsTest2.jvprops1 ",
            15, 3, 1 },
        { "--gui --nonews --nosplash --debug "
                + "--append=examples/uniref50.fa "
                + "--colour=gecos-flower "
                + "--structure=[seqid=FER1_SPIOL]examples/AlphaFold/AF-P00221-F1-model_v4.cif "
                + "--paematrix=examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json "
                + "--noshowannotations " + "--noshowssannotations "
                + "--props=test/jalview/bin/commandsTest2.jvprops1 ",
            15, 0, 1 },
        { "--gui --nonews --nosplash --debug "
                + "--append=examples/uniref50.fa "
                + "--colour=gecos-flower "
                + "--structure=[seqid=FER1_SPIOL]examples/AlphaFold/AF-P00221-F1-model_v4.cif "
                + "--paematrix=examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json "
                + "--noshowannotations " + "--noshowssannotations "
                + "--props=test/jalview/bin/commandsTest2.jvprops1 ",
            15, 0, 1 },
        { "--gui --nonews --nosplash --debug --nowebservicediscovery --props=test/jalview/bin/commandsTest.jvprops --argfile=test/jalview/bin/commandsTest2.argfile1 ",
            16, 19, 3 },
        { "--gui --nonews --nosplash --debug --nowebservicediscovery --props=test/jalview/bin/commandsTest.jvprops --argfile=test/jalview/bin/commandsTest2.argfile2 ",
            16, 0, 2 },
        { "--gui --nonews --nosplash --debug --nowebservicediscovery --props=test/jalview/bin/commandsTest.jvprops --open=./examples/test_fab41.result/sample.a2m "
                + "--allstructures "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--structureviewer=none "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_2_model_4.pdb "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_3_model_2.pdb",
            16, 10, 0 },
        { "--gui --nonews --nosplash --debug --nowebservicediscovery --props=test/jalview/bin/commandsTest.jvprops --open=./examples/test_fab41.result/sample.a2m "
                + "--allstructures "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb "
                + "--noallstructures " + "--structureviewer=none "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_2_model_4.pdb "
                + "--structure=./examples/test_fab41.result/test_fab41_unrelaxed_rank_3_model_2.pdb",
            16, 10, 2 },
        /*
         */
        //
    };
  }
}
