package jalview.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FastaFile;
import jalview.io.FileLoader;
import jalview.io.FormatAdapter;
import jalview.util.Platform;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;

public class AverageDistanceEngineTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void loadProperties()
  {
    Cache.loadProperties("test/jalview/bin/TestProps.jvprops");
  }

  @Test(groups = { "Functional" })
  public void testUPGMAEngine() throws Exception
  {
    AlignFrame af = new FileLoader(false).LoadFileWaitTillLoaded(
            "examples/test_fab41.result/sample.a3m", DataSourceType.FILE);
    AlignmentI seqs = af.getViewport().getAlignment();
    SequenceI target = seqs.getSequenceAt(0);
    File testPAE = new File(
            "examples/test_fab41.result/test_fab41_predicted_aligned_error_v1.json");
    List<Object> pae_obj = (List<Object>) Platform
            .parseJSON(new FileInputStream(testPAE));
    if (pae_obj == null)
    {
      Assert.fail("JSON PAE file did not parse properly.");
    }
    ContactMatrixI matrix = new PAEContactMatrix(target,
            (Map<String, Object>) pae_obj.get(0));
    AlignmentAnnotation aa = target.addContactList(matrix);
    System.out.println("Matrix has max=" + matrix.getMax() + " and min="
            + matrix.getMin());
    long start = System.currentTimeMillis();
    AverageDistanceEngine clusterer = new AverageDistanceEngine(
            af.getViewport(), null, matrix, false);
    System.out.println("built a tree in "
            + (System.currentTimeMillis() - start) * 0.001 + " seconds.");
    StringBuffer sb = new StringBuffer();
    System.out.println("Newick string\n"
            + new jalview.io.NewickFile(clusterer.getTopNode(), true, true)
                    .print());

    double height = clusterer.findHeight(clusterer.getTopNode());
    // compute height fraction to cut
    // PAE matrixes are absolute measure in angstrom, so
    // cluster all regions within threshold (e.g. 2A) - if height above
    // threshold. Otherwise all nodes are in one cluster
    double thr = .2;
    List<BinaryNode> groups;
    if (height > thr)
    {
      float cut = (float) (thr / height);
      System.out.println("Threshold " + cut + " for height=" + height);
      groups = clusterer.groupNodes(cut);
    }
    else
    {
      groups = new ArrayList<BinaryNode>();
      groups.add(clusterer.getTopNode());
    }
    int n = 1;
    for (BinaryNode root : groups)
    {
      System.out.println("Cluster " + n++);
      for (BinaryNode leaf : clusterer.findLeaves(root))
      {
        System.out.print(" " + leaf.getName());
      }
      System.out.println("\\");
    }
  }

}
