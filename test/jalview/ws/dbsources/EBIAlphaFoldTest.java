package jalview.ws.dbsources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.FileAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;

public class EBIAlphaFoldTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @DataProvider(name = "getExamplePAEfiles")
  public Object[][] getExamplePAEfiles()
  {
    return new String[][] {
        //
        { "examples/test_fab41.result/test_fab41_predicted_aligned_error_v1.json" },
        { "examples/AlphaFold/AF-A0A1U8FD60-F1-predicted_aligned_error_v4.json" },
        { "examples/AlphaFold/AF-Q5VSL9-F1-predicted_aligned_error_v4.json" },
        //
    };
  }

  @Test(groups = { "Functional" }, dataProvider = "getExamplePAEfiles")
  public void checkPAEimport(String paeFile) throws Exception
  {
    PAEContactMatrix cm = new PAEContactMatrix(
            new Sequence("Dummy/1-2000", "ASDASDA"),
            EBIAlfaFold.parseJSONtoPAEContactMatrix(
                    new FileInputStream(paeFile)));
    Assert.assertNotEquals(cm.getMax(), 0.0f, "No data from " + paeFile);
  }

  @Test(groups = { "Functional" }, dataProvider = "getPDBandPAEfiles")
  public void checkImportPAEToStructure(String pdbFile, String paeFile)
  {
    FileInputStream paeInput = null;
    try
    {
      paeInput = new FileInputStream(paeFile);
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
      FileAssert.assertFile(new File(paeFile),
              "Test file '" + paeFile + "' doesn't seem to exist");
    }
    SequenceI seq = new Sequence("Dummy/1-2000", "ASDASDA");
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    StructureMapping sm = new StructureMapping(seq, pdbFile, null, null,
            null, null);
    ssm.addStructureMapping(sm);

    StructureMapping[] smArray = ssm.getMapping(pdbFile);

    try
    {
      boolean done = EBIAlfaFold.importPaeJSONAsContactMatrixToStructure(
              smArray, paeInput, "label");
      Assert.assertTrue(done,
              "Import of '" + paeFile + "' didn't complete successfully");
    } catch (IOException | ParseException e)
    {
      Assert.fail("Exception importing paefile '" + paeFile + "'", e);
    }
  }

  @DataProvider(name = "getPDBandPAEfiles")
  public Object[][] getPDBandPAEfiles()
  {
    return new String[][] {
        //
        /*
         */
        { "examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3.pdb",
            "examples/test_fab41.result/test_fab41_unrelaxed_rank_1_model_3_scores.json" },
        { "examples/AlphaFold/AF-A0A1U8FD60-F1-model_v4.pdb",
            "examples/AlphaFold/AF-A0A1U8FD60-F1-predicted_aligned_error_v4.json" },
        { "examples/AlphaFold/AF-Q5VSL9-F1-model_v4.pdb",
            "examples/AlphaFold/AF-Q5VSL9-F1-predicted_aligned_error_v4.json" },
        /*
         */
    };
  }

}
