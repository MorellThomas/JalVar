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
package jalview.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.api.AlignViewportI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.core.FTSRestClient;
import jalview.fts.service.pdb.PDBFTSRestClient;
import jalview.fts.service.pdb.PDBFTSRestClientTest;
import jalview.fts.service.threedbeacons.TDBeaconsFTSRestClient;
import jalview.fts.threedbeacons.TDBeaconsFTSRestClientTest;
import jalview.gui.StructureViewer.ViewerType;
import jalview.gui.structurechooser.PDBStructureChooserQuerySource;
import jalview.io.DataSourceType;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileLoader;
import jalview.io.IdentifyFile;
import jalview.jbgui.FilterOption;
import jalview.structure.StructureImportSettings.TFType;
import junit.extensions.PA;

@Test(singleThreaded = true)
public class StructureChooserTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Sequence seq, upSeq, upSeq_nocanonical;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
    seq = new Sequence("PDB|4kqy|4KQY|A", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1,
            26);
    seq.createDatasetSequence();
    for (int x = 1; x < 5; x++)
    {
      DBRefEntry dbRef = new DBRefEntry();
      dbRef.setAccessionId("XYZ_" + x);
      seq.addDBRef(dbRef);
    }

    PDBEntry dbRef = new PDBEntry();
    dbRef.setId("1tim");

    Vector<PDBEntry> pdbIds = new Vector<>();
    pdbIds.add(dbRef);

    seq.setPDBId(pdbIds);

    // Uniprot sequence for 3D-Beacons mocks
    upSeq = new Sequence("P38398",
            "MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQCPLCKNDITKRS\n"
                    + "LQESTRFSQLVEELLKIICAFQLDTGLEYANSYNFAKKENNSPEHLKDEVSIIQSMGYRNRAKRLLQSEPEN\n"
                    + "PSLQETSLSVQLSNLGTVRTLRTKQRIQPQKTSVYIELGSDSSEDTVNKATYCSVGDQELLQITPQGTRDEI\n"
                    + "SLDSAKKAACEFSETDVTNTEHHQPSNNDLNTTEKRAAERHPEKYQGSSVSNLHVEPCGTNTHASSLQHENS\n"
                    + "SLLLTKDRMNVEKAEFCNKSKQPGLARSQHNRWAGSKETCNDRRTPSTEKKVDLNADPLCERKEWNKQKLPC\n"
                    + "SENPRDTEDVPWITLNSSIQKVNEWFSRSDELLGSDDSHDGESESNAKVADVLDVLNEVDEYSGSSEKIDLL\n"
                    + "ASDPHEALICKSERVHSKSVESNIEDKIFGKTYRKKASLPNLSHVTENLIIGAFVTEPQIIQERPLTNKLKR\n"
                    + "KRRPTSGLHPEDFIKKADLAVQKTPEMINQGTNQTEQNGQVMNITNSGHENKTKGDSIQNEKNPNPIESLEK\n"
                    + "ESAFKTKAEPISSSISNMELELNIHNSKAPKKNRLRRKSSTRHIHALELVVSRNLSPPNCTELQIDSCSSSE\n"
                    + "EIKKKKYNQMPVRHSRNLQLMEGKEPATGAKKSNKPNEQTSKRHDSDTFPELKLTNAPGSFTKCSNTSELKE\n"
                    + "FVNPSLPREEKEEKLETVKVSNNAEDPKDLMLSGERVLQTERSVESSSISLVPGTDYGTQESISLLEVSTLG\n"
                    + "KAKTEPNKCVSQCAAFENPKGLIHGCSKDNRNDTEGFKYPLGHEVNHSRETSIEMEESELDAQYLQNTFKVS\n"
                    + "KRQSFAPFSNPGNAEEECATFSAHSGSLKKQSPKVTFECEQKEENQGKNESNIKPVQTVNITAGFPVVGQKD\n"
                    + "KPVDNAKCSIKGGSRFCLSSQFRGNETGLITPNKHGLLQNPYRIPPLFPIKSFVKTKCKKNLLEENFEEHSM\n"
                    + "SPEREMGNENIPSTVSTISRNNIRENVFKEASSSNINEVGSSTNEVGSSINEIGSSDENIQAELGRNRGPKL\n"
                    + "NAMLRLGVLQPEVYKQSLPGSNCKHPEIKKQEYEEVVQTVNTDFSPYLISDNLEQPMGSSHASQVCSETPDD\n"
                    + "LLDDGEIKEDTSFAENDIKESSAVFSKSVQKGELSRSPSPFTHTHLAQGYRRGAKKLESSEENLSSEDEELP\n"
                    + "CFQHLLFGKVNNIPSQSTRHSTVATECLSKNTEENLLSLKNSLNDCSNQVILAKASQEHHLSEETKCSASLF\n"
                    + "SSQCSELEDLTANTNTQDPFLIGSSKQMRHQSESQGVGLSDKELVSDDEERGTGLEENNQEEQSMDSNLGEA\n"
                    + "ASGCESETSVSEDCSGLSSQSDILTTQQRDTMQHNLIKLQQEMAELEAVLEQHGSQPSNSYPSIISDSSALE\n"
                    + "DLRNPEQSTSEKAVLTSQKSSEYPISQNPEGLSADKFEVSADSSTSKNKEPGVERSSPSKCPSLDDRWYMHS\n"
                    + "CSGSLQNRNYPSQEELIKVVDVEEQQLEESGPHDLTETSYLPRQDLEGTPYLESGISLFSDDPESDPSEDRA\n"
                    + "PESARVGNIPSSTSALKVPQLKVAESAQSPAAAHTTDTAGYNAMEESVSREKPELTASTERVNKRMSMVVSG\n"
                    + "LTPEEFMLVYKFARKHHITLTNLITEETTHVVMKTDAEFVCERTLKYFLGIAGGKWVVSYFWVTQSIKERKM\n"
                    + "LNEHDFEVRGDVVNGRNHQGPKRARESQDRKIFRGLEICCYGPFTNMPTDQLEWMVQLCGASVVKELSSFTL\n"
                    + "GTGVHPIVVVQPDAWTEDNGFHAIGQMCEAPVVTREWVLDSVALYQCQELDTYLIPQIPHSHY\n"
                    + "",
            1, 1863);
    upSeq.setDescription("Breast cancer type 1 susceptibility protein");
    upSeq_nocanonical = new Sequence(upSeq);
    upSeq.createDatasetSequence();
    upSeq.addDBRef(new DBRefEntry("UNIPROT", "0", "P38398", null, true));

    upSeq_nocanonical.createDatasetSequence();
    // not a canonical reference
    upSeq_nocanonical.addDBRef(
            new DBRefEntry("UNIPROT", "0", "P38398", null, false));

  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
    seq = null;
    upSeq = null;
    upSeq_nocanonical = null;
  }

  @Test(groups = { "Functional" })
  public void populateFilterComboBoxTest() throws InterruptedException
  {
    TDBeaconsFTSRestClientTest.setMock();
    PDBFTSRestClientTest.setMock();

    SequenceI[] selectedSeqs = new SequenceI[] { seq };
    StructureChooser sc = new StructureChooser(selectedSeqs, seq, null);
    ThreadwaitFor(200, sc);

    // if structures are not discovered then don't
    // populate filter options
    sc.populateFilterComboBox(false, false);
    int optionsSize = sc.getCmbFilterOption().getItemCount();
    System.out.println("Items (no data, no cache): ");
    StringBuilder items = new StringBuilder();
    for (int p = 0; p < optionsSize; p++)
    {
      items.append("- ")
              .append(sc.getCmbFilterOption().getItemAt(p).getName())
              .append("\n");

    }
    // report items when this fails - seems to be a race condition
    Assert.assertEquals(items.toString(), optionsSize, 2);

    sc.populateFilterComboBox(true, false);
    optionsSize = sc.getCmbFilterOption().getItemCount();
    assertTrue(optionsSize > 3); // if structures are found, filter options
                                 // should be populated

    sc.populateFilterComboBox(true, true);
    assertTrue(sc.getCmbFilterOption().getSelectedItem() != null);
    FilterOption filterOpt = (FilterOption) sc.getCmbFilterOption()
            .getSelectedItem();
    assertEquals("Cached Structures", filterOpt.getName());
    FTSRestClient
            .unMock((FTSRestClient) TDBeaconsFTSRestClient.getInstance());
    FTSRestClient.unMock((FTSRestClient) PDBFTSRestClient.getInstance());

  }

  @Test(groups = { "Functional" })
  public void displayTDBQueryTest() throws InterruptedException
  {
    TDBeaconsFTSRestClientTest.setMock();
    PDBFTSRestClientTest.setMock();

    SequenceI[] selectedSeqs = new SequenceI[] { upSeq_nocanonical };
    StructureChooser sc = new StructureChooser(selectedSeqs,
            upSeq_nocanonical, null);
    // mock so should be quick. Exceptions from mocked PDBFTS are expected too
    ThreadwaitFor(500, sc);

    assertTrue(sc.isCanQueryTDB() && sc.isNotQueriedTDBYet());
  }

  @Test(groups = { "Network" })
  public void fetchStructuresInfoTest()
  {
    FTSRestClient
            .unMock((FTSRestClient) TDBeaconsFTSRestClient.getInstance());
    PDBFTSRestClient.unMock((FTSRestClient) PDBFTSRestClient.getInstance());
    SequenceI[] selectedSeqs = new SequenceI[] { seq };
    StructureChooser sc = new StructureChooser(selectedSeqs, seq, null);
    // not mocked, wait for 2s
    ThreadwaitFor(2000, sc);

    sc.fetchStructuresMetaData();
    Collection<FTSData> ss = (Collection<FTSData>) PA.getValue(sc,
            "discoveredStructuresSet");
    assertNotNull(ss);
    assertTrue(ss.size() > 0);
  }

  @Test(groups = { "Functional" })
  public void fetchStructuresInfoMockedTest()
  {
    TDBeaconsFTSRestClientTest.setMock();
    PDBFTSRestClientTest.setMock();
    SequenceI[] selectedSeqs = new SequenceI[] { upSeq };
    StructureChooser sc = new StructureChooser(selectedSeqs, seq, null);
    ThreadwaitFor(500, sc);

    sc.fetchStructuresMetaData();
    Collection<FTSData> ss = (Collection<FTSData>) PA.getValue(sc,
            "discoveredStructuresSet");
    assertNotNull(ss);
    assertTrue(ss.size() > 0);
  }

  private void ThreadwaitFor(int i, StructureChooser sc)
  {
    long timeout = i + System.currentTimeMillis();
    while (!sc.isDialogVisible() && timeout > System.currentTimeMillis())
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {

      }
    }

  }

  @Test(groups = { "Functional" })
  public void sanitizeSeqNameTest()
  {
    String name = "ab_cdEF|fwxyz012349";
    assertEquals(name,
            PDBStructureChooserQuerySource.sanitizeSeqName(name));

    // remove a [nn] substring
    name = "abcde12[345]fg";
    assertEquals("abcde12fg",
            PDBStructureChooserQuerySource.sanitizeSeqName(name));

    // remove characters other than a-zA-Z0-9 | or _
    name = "ab[cd],.\t£$*!- \\\"@:e";
    assertEquals("abcde",
            PDBStructureChooserQuerySource.sanitizeSeqName(name));

    name = "abcde12[345a]fg";
    assertEquals("abcde12345afg",
            PDBStructureChooserQuerySource.sanitizeSeqName(name));
  }

  @Test(groups = { "Functional" }, dataProvider = "openStructureFileParams")
  public void openStructureFileForSequenceTest(String alfile, String seqid,
          String sFilename, TFType tft, String paeFilename,
          boolean showRefAnnotations, boolean doXferSettings,
          ViewerType viewerType, int seqNum, int annNum, int viewerNum,
          String propsFile)
  {
    Cache.loadProperties(
            propsFile == null ? "test/jalview/io/testProps.jvprops"
                    : propsFile);

    Jalview.main(
            propsFile == null ? null : new String[]
            { "--props", propsFile });
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.OK_OPTION);

    FileLoader fileLoader = new FileLoader(true);
    FileFormatI format = null;
    File alFile = new File(alfile);
    try
    {
      format = new IdentifyFile().identify(alFile, DataSourceType.FILE);
    } catch (FileFormatException e1)
    {
      Assert.fail(
              "Unknown file format for '" + alFile.getAbsolutePath() + "'");
    }

    AlignFrame af = fileLoader.LoadFileWaitTillLoaded(alFile,
            DataSourceType.FILE, format);
    AlignmentPanel ap = af.alignPanel;
    Assert.assertNotNull("No alignPanel", ap);

    AlignmentI al = ap.getAlignment();
    Assert.assertNotNull(al);

    SequenceI seq = al.findName(seqid);
    Assert.assertNotNull("Sequence '" + seqid + "' not found in alignment",
            seq);

    StructureChooser.openStructureFileForSequence(null, null, ap, seq,
            false, sFilename, tft, paeFilename, false, showRefAnnotations,
            doXferSettings, viewerType);

    List<SequenceI> seqs = al.getSequences();
    Assert.assertNotNull(seqs);

    Assert.assertEquals("Wrong number of sequences", seqNum, seqs.size());

    AlignViewportI av = ap.getAlignViewport();
    Assert.assertNotNull(av);

    AlignmentAnnotation[] aas = al.getAlignmentAnnotation();
    int visibleAnn = 0;
    for (AlignmentAnnotation aa : aas)
    {
      if (aa.visible)
        visibleAnn++;
    }
    Assert.assertEquals("Wrong number of viewed annotations", annNum,
            visibleAnn);

    if (viewerNum > -1)
    {
      try
      {
        Thread.sleep(100);
      } catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      List<StructureViewerBase> openViewers = Desktop.instance
              .getStructureViewers(ap, null);
      Assert.assertNotNull(openViewers);
      int count = 0;
      for (StructureViewerBase svb : openViewers)
      {
        if (svb.isVisible())
          count++;
      }
      Assert.assertEquals("Wrong number of structure viewers opened",
              viewerNum, count);

    }

    if (af != null)
    {
      af.setVisible(false);
      af.dispose();
    }
  }

  @DataProvider(name = "openStructureFileParams")
  public Object[][] openStructureFileParams()
  {
    /*
        String alFile,
        String seqid,
        String structureFilename,
        TFType tft,
        String paeFilename,
        boolean showRefAnnotations,
        boolean doXferSettings, // false for Commands
        ViewerType viewerType,
        int seqNum,
        int annNum,
        int viewerNum,
        String propsFile
     */
    return new Object[][] {
        /*
        */
        { "examples/uniref50.fa", "FER1_SPIOL",
            "examples/AlphaFold/AF-P00221-F1-model_v4.cif", TFType.DEFAULT,
            "examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json",
            true, false, null, 15, 7, 0, null },
        { "examples/uniref50.fa", "FER1_SPIOL",
            "examples/AlphaFold/AF-P00221-F1-model_v4.cif", TFType.PLDDT,
            "examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json",
            true, false, null, 15, 7, 0, null },
        { "examples/uniref50.fa", "FER1_SPIOL",
            "examples/AlphaFold/AF-P00221-F1-model_v4.cif", TFType.PLDDT,
            "examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json",
            false, false, null, 15, 4, 0, null },
        { "examples/uniref50.fa", "FER1_SPIOL",
            "examples/AlphaFold/AF-P00221-F1-model_v4.cif", TFType.DEFAULT,
            "examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json",
            true, false, ViewerType.JMOL, 15, 7, 1, null },
        { "examples/uniref50.fa", "FER1_SPIOL",
            "examples/AlphaFold/AF-P00221-F1-model_v4.cif", TFType.PLDDT,
            "examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json",
            true, false, ViewerType.JMOL, 15, 7, 1, null },
        { "examples/uniref50.fa", "FER1_SPIOL",
            "examples/AlphaFold/AF-P00221-F1-model_v4.cif", TFType.PLDDT,
            "examples/AlphaFold/AF-P00221-F1-predicted_aligned_error_v4.json",
            false, false, ViewerType.JMOL, 15, 4, 1, null }, };
  }

}
