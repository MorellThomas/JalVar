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
package jalview.ws.sifts;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import jalview.api.DBRefEntryI;
import jalview.bin.Cache;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.structure.StructureMapping;
import jalview.xml.binding.sifts.Entry.Entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.testng.Assert;
import org.testng.FileAssert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import mc_view.Atom;
import mc_view.PDBfile;

public class SiftsClientTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  public static final String DEFAULT_SIFTS_DOWNLOAD_DIR = System
          .getProperty("user.home") + File.separatorChar
          + ".sifts_downloads" + File.separatorChar;

  private String testPDBId = "1a70";

  private SiftsClient siftsClient = null;

  SequenceI testSeq = new Sequence("P00221",
          "MAAT..TTTMMG..MATTFVPKPQAPPMMAALPSNTGR..SLFGLKT.GSR..GGRMTMA"
                  + "AYKVTLVTPTGNVEFQCPDDVYILDAAEEEGIDLPYSCRAGSCSSCAGKLKTGSLNQDD"
                  + "QSFLDDDQIDEGWVLTCAAYPVSDVTIETHKEEELTA.",
          1, 147);

  int u = SiftsClient.UNASSIGNED;

  HashMap<Integer, int[]> expectedMapping = new HashMap<Integer, int[]>();

  @BeforeTest(alwaysRun = true)
  public void populateExpectedMapping() throws SiftsException
  {
    expectedMapping.put(51, new int[] { 1, 2, 1 });
    expectedMapping.put(52, new int[] { 2, 7, 2 });
    expectedMapping.put(53, new int[] { 3, 12, 3 });
    expectedMapping.put(54, new int[] { 4, 24, 4 });
    expectedMapping.put(55, new int[] { 5, 33, 5 });
    expectedMapping.put(56, new int[] { 6, 40, 6 });
    expectedMapping.put(57, new int[] { 7, 47, 7 });
    expectedMapping.put(58, new int[] { 8, 55, 8 });
    expectedMapping.put(59, new int[] { 9, 62, 9 });
    expectedMapping.put(60, new int[] { 10, 69, 10 });
    expectedMapping.put(61, new int[] { 11, 76, 11 });
    expectedMapping.put(62, new int[] { 12, 83, 12 });
    expectedMapping.put(63, new int[] { 13, 87, 13 });
    expectedMapping.put(64, new int[] { 14, 95, 14 });
    expectedMapping.put(65, new int[] { 15, 102, 15 });
    expectedMapping.put(66, new int[] { 16, 111, 16 });
    expectedMapping.put(67, new int[] { 17, 122, 17 });
    expectedMapping.put(68, new int[] { 18, 131, 18 });
    expectedMapping.put(69, new int[] { 19, 137, 19 });
    expectedMapping.put(70, new int[] { 20, 144, 20 });
    expectedMapping.put(71, new int[] { 21, 152, 21 });
    expectedMapping.put(72, new int[] { 22, 160, 22 });
    expectedMapping.put(73, new int[] { 23, 167, 23 });
    expectedMapping.put(74, new int[] { 24, 179, 24 });
    expectedMapping.put(75, new int[] { 25, 187, 25 });
    expectedMapping.put(76, new int[] { 26, 195, 26 });
    expectedMapping.put(77, new int[] { 27, 203, 27 });
    expectedMapping.put(78, new int[] { 28, 208, 28 });
    expectedMapping.put(79, new int[] { 29, 213, 29 });
    expectedMapping.put(80, new int[] { 30, 222, 30 });
    expectedMapping.put(81, new int[] { 31, 231, 31 });
    expectedMapping.put(82, new int[] { 32, 240, 32 });
    expectedMapping.put(83, new int[] { 33, 244, 33 });
    expectedMapping.put(84, new int[] { 34, 252, 34 });
    expectedMapping.put(85, new int[] { 35, 260, 35 });
    expectedMapping.put(86, new int[] { 36, 268, 36 });
    expectedMapping.put(87, new int[] { 37, 275, 37 });
    expectedMapping.put(88, new int[] { 38, 287, 38 });
    expectedMapping.put(89, new int[] { 39, 293, 39 });
    expectedMapping.put(90, new int[] { 40, 299, 40 });
    expectedMapping.put(91, new int[] { 41, 310, 41 });
    expectedMapping.put(92, new int[] { 42, 315, 42 });
    expectedMapping.put(93, new int[] { 43, 319, 43 });
    expectedMapping.put(94, new int[] { 44, 325, 44 });
    expectedMapping.put(95, new int[] { 45, 331, 45 });
    expectedMapping.put(96, new int[] { 46, 337, 46 });
    expectedMapping.put(97, new int[] { 47, 343, 47 });
    expectedMapping.put(98, new int[] { 48, 349, 48 });
    expectedMapping.put(99, new int[] { 49, 354, 49 });
    expectedMapping.put(100, new int[] { 50, 358, 50 });
    expectedMapping.put(101, new int[] { 51, 367, 51 });
    expectedMapping.put(102, new int[] { 52, 375, 52 });
    expectedMapping.put(103, new int[] { 53, 384, 53 });
    expectedMapping.put(104, new int[] { 54, 391, 54 });
    expectedMapping.put(105, new int[] { 55, 395, 55 });
    expectedMapping.put(106, new int[] { 56, 401, 56 });
    expectedMapping.put(107, new int[] { 57, 409, 57 });
    expectedMapping.put(108, new int[] { 58, 417, 58 });
    expectedMapping.put(109, new int[] { 59, 426, 59 });
    expectedMapping.put(110, new int[] { 60, 434, 60 });
    expectedMapping.put(111, new int[] { 61, 442, 61 });
    expectedMapping.put(112, new int[] { 62, 451, 62 });
    expectedMapping.put(113, new int[] { 63, 457, 63 });
    expectedMapping.put(114, new int[] { 64, 468, 64 });
    expectedMapping.put(115, new int[] { 65, 476, 65 });
    expectedMapping.put(116, new int[] { 66, 484, 66 });
    expectedMapping.put(117, new int[] { 67, 492, 67 });
    expectedMapping.put(118, new int[] { 68, 500, 68 });
    expectedMapping.put(119, new int[] { 69, 509, 69 });
    expectedMapping.put(120, new int[] { 70, 517, 70 });
    expectedMapping.put(121, new int[] { 71, 525, 71 });
    expectedMapping.put(122, new int[] { 72, 534, 72 });
    expectedMapping.put(123, new int[] { 73, 538, 73 });
    expectedMapping.put(124, new int[] { 74, 552, 74 });
    expectedMapping.put(125, new int[] { 75, 559, 75 });
    expectedMapping.put(126, new int[] { 76, 567, 76 });
    expectedMapping.put(127, new int[] { 77, 574, 77 });
    expectedMapping.put(128, new int[] { 78, 580, 78 });
    expectedMapping.put(129, new int[] { 79, 585, 79 });
    expectedMapping.put(130, new int[] { 80, 590, 80 });
    expectedMapping.put(131, new int[] { 81, 602, 81 });
    expectedMapping.put(132, new int[] { 82, 609, 82 });
    expectedMapping.put(133, new int[] { 83, 616, 83 });
    expectedMapping.put(134, new int[] { 84, 622, 84 });
    expectedMapping.put(135, new int[] { 85, 630, 85 });
    expectedMapping.put(136, new int[] { 86, 637, 86 });
    expectedMapping.put(137, new int[] { 87, 644, 87 });
    expectedMapping.put(138, new int[] { 88, 652, 88 });
    expectedMapping.put(139, new int[] { 89, 661, 89 });
    expectedMapping.put(140, new int[] { 90, 668, 90 });
    expectedMapping.put(141, new int[] { 91, 678, 91 });
    expectedMapping.put(142, new int[] { 92, 687, 92 });
    expectedMapping.put(143, new int[] { 93, 696, 93 });
    expectedMapping.put(144, new int[] { 94, 705, 94 });
    expectedMapping.put(145, new int[] { 95, 714, 95 });
    expectedMapping.put(146, new int[] { 96, 722, 96 });
    expectedMapping.put(147, new int[] { 97, 729, 97 });
  }

  @BeforeTest(alwaysRun = true)
  public void setUpSiftsClient() throws SiftsException, IOException
  {
    // read test props before manipulating config
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    // SIFTs entries are updated weekly - so use saved SIFTs file to enforce
    // test reproducibility
    new SiftsSettings();
    SiftsSettings.setSiftDownloadDirectory(jalview.bin.Cache
            .getDefault("sifts_download_dir", DEFAULT_SIFTS_DOWNLOAD_DIR));
    SiftsSettings.setMapWithSifts(true);
    SiftsSettings.setCacheThresholdInDays("2");
    SiftsSettings.setFailSafePIDThreshold("70");
    PDBfile pdbFile;
    
    pdbFile = new PDBfile(false, false, false,
            "test/jalview/io/" + testPDBId + ".pdb", DataSourceType.FILE);
    // TODO: this uses a network connection - we should mock the sifts testPDBId.xml.gz
    siftsClient = new SiftsClient(pdbFile);
  }

  @AfterTest(alwaysRun = true)
  public void cleanUpSiftsClient()
  {
    siftsClient = null;
  }
  
  @Test(groups= {"Functional"})
  public void testSIFTsDownloadURL() {
    String expectedUrl = "https://ftp.ebi.ac.uk/pub/databases/msd/sifts/split_xml/xy/1xyz.sifts.xml.gz";
    Assert.assertEquals(SiftsClient.getDownloadUrlFor("1xyz.sifts.xml.gz"), expectedUrl);
  }

  @Test(groups = { "Network" })
  public void getSIFTsFileTest() throws SiftsException, IOException
  {
    File siftsFile;
    siftsFile = SiftsClient.downloadSiftsFile(testPDBId);
    FileAssert.assertFile(siftsFile);
    long t1 = siftsFile.lastModified();

    // re-read file should be returned from cache
    siftsFile = SiftsClient.getSiftsFile(testPDBId);
    FileAssert.assertFile(siftsFile);
    long t2 = siftsFile.lastModified();
    assertEquals(t1, t2);

    /*
     * force fetch by having 0 expiry of cache
     * also wait one second, because file timestamp does not
     * give millisecond resolution :-(
     */
    synchronized (this)
    {
      try
      {
        wait(1000);
      } catch (InterruptedException e)
      {
      }
    }
    SiftsSettings.setCacheThresholdInDays("0");
    siftsFile = SiftsClient.getSiftsFile(testPDBId);
    FileAssert.assertFile(siftsFile);
    long t3 = siftsFile.lastModified();
    assertTrue(t3 > t2, "file timestamp unchanged at " + t3);

    SiftsSettings.setCacheThresholdInDays("2");
  }

  @Test(groups = { "Network" })
  public void downloadSiftsFileTest() throws SiftsException, IOException
  {
    // Assert that file isn't yet downloaded - if already downloaded, assert it
    // is deleted
    Assert.assertTrue(SiftsClient.deleteSiftsFileByPDBId(testPDBId));
    File siftsFile;
    siftsFile = SiftsClient.downloadSiftsFile(testPDBId);
    FileAssert.assertFile(siftsFile);
    SiftsClient.downloadSiftsFile(testPDBId);
  }

  @Test(groups = { "Network" })
  public void getAllMappingAccessionTest()
  {
    Assert.assertNotNull(siftsClient);
    Assert.assertNotNull(siftsClient.getAllMappingAccession());
    Assert.assertTrue(siftsClient.getAllMappingAccession().size() > 1);
  }

  @Test(groups = { "Network" })
  public void getGreedyMappingTest()
  {
    Assert.assertNotNull(siftsClient);
    Assert.assertNotNull(testSeq);
    Assert.assertNotNull(expectedMapping);

    // TODO delete when auto-fetching of DBRefEntry is implemented
    DBRefEntry dbRef = new DBRefEntry("uniprot", "", "P00221");
    testSeq.addDBRef(dbRef);
    // testSeq.setSourceDBRef(dbRef);

    try
    {
      HashMap<Integer, int[]> actualMapping = siftsClient
              .getGreedyMapping("A", testSeq, null);
      Assert.assertEquals(testSeq.getStart(), 1);
      Assert.assertEquals(testSeq.getEnd(), 147);
      // Can't do Assert.assertEquals(actualMapping, expectedMapping);
      // because this fails in our version of TestNG
      Assert.assertEquals(actualMapping.size(), expectedMapping.size());
      Iterator<Map.Entry<Integer, int[]>> it = expectedMapping.entrySet()
              .iterator();
      while (it.hasNext())
      {
        Map.Entry<Integer, int[]> pair = it.next();
        Assert.assertTrue(actualMapping.containsKey(pair.getKey()));
        Assert.assertEquals(actualMapping.get(pair.getKey()),
                pair.getValue());
      }

    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while generating mapping...");
    }
  }

  @Test(groups = { "Network" })
  private void getAtomIndexTest()
  {
    ArrayList<Atom> atoms = new ArrayList<Atom>();
    Atom atom = new Atom(u, u, u);
    atom.resNumber = 43;
    atom.atomIndex = 7;
    atoms.add(atom);
    int actualAtomIndex = siftsClient.getAtomIndex(1, atoms);
    Assert.assertEquals(actualAtomIndex, siftsClient.UNASSIGNED);
    actualAtomIndex = siftsClient.getAtomIndex(43, atoms);
    Assert.assertEquals(actualAtomIndex, 7);
  }

  @Test(
    groups =
    { "Network" },
    expectedExceptions = IllegalArgumentException.class)
  private void getAtomIndexNullTest()
  {
    siftsClient.getAtomIndex(1, null);
  }

  @Test(groups = { "Network" })
  private void padWithGapsTest()
  {

  }

  @Test(groups = { "Network" }, expectedExceptions = SiftsException.class)
  private void populateAtomPositionsNullTest1()
          throws IllegalArgumentException, SiftsException
  {
    siftsClient.populateAtomPositions(null, null);
  }

  @Test(groups = { "Network" }, expectedExceptions = SiftsException.class)
  private void populateAtomPositionsNullTest2()
          throws IllegalArgumentException, SiftsException
  {
    siftsClient.populateAtomPositions("A", null);
  }

  @Test(groups = { "Network" })
  public void getValidSourceDBRefTest() throws SiftsException
  {
    DBRefEntryI actualValidSrcDBRef = siftsClient
            .getValidSourceDBRef(testSeq);
    DBRefEntryI expectedDBRef = new DBRefEntry();
    expectedDBRef.setSource(DBRefSource.UNIPROT);
    expectedDBRef.setAccessionId("P00221");
    expectedDBRef.setVersion("");
    Assert.assertEquals(actualValidSrcDBRef, expectedDBRef);
  }

  @Test(groups = { "Network" }, expectedExceptions = SiftsException.class)
  public void getValidSourceDBRefExceptionTest() throws SiftsException
  {
    SequenceI invalidTestSeq = new Sequence("testSeq", "ABCDEFGH");
    siftsClient.getValidSourceDBRef(invalidTestSeq);
  }

  @Test(groups = { "Network" }, expectedExceptions = SiftsException.class)
  public void getValidSourceDBRefExceptionXTest() throws SiftsException
  {
    SequenceI invalidTestSeq = new Sequence("testSeq", "ABCDEFGH");
    DBRefEntry invalidDBRef = new DBRefEntry();
    invalidDBRef.setAccessionId("BLAR"); // note no version is set, so also invalid
    invalidTestSeq.addDBRef(invalidDBRef);
    siftsClient.getValidSourceDBRef(invalidTestSeq);
  }

  @Test(groups = { "Network" })
  public void isValidDBRefEntryTest()
  {
    DBRefEntryI validDBRef = new DBRefEntry();
    validDBRef.setSource(DBRefSource.UNIPROT);
    validDBRef.setAccessionId("P00221");
    validDBRef.setVersion("");
    Assert.assertTrue(siftsClient.isValidDBRefEntry(validDBRef));
  }

  @Test(groups = { "Network" })
  public void getSiftsStructureMappingTest() throws SiftsException
  {
    Assert.assertTrue(SiftsSettings.isMapWithSifts());
    StructureMapping strucMapping = siftsClient
            .getSiftsStructureMapping(testSeq, testPDBId, "A");
    String expectedMappingOutput = "\nSequence ⟷ Structure mapping details\n"
            + "Method: SIFTS\n\n" + "P00221 :  51 - 147 Maps to \n"
            + "1A70|A :  1 - 97\n\n"
            + "P00221 AAYKVTLVTPTGNVEFQCPDDVYILDAAEEEGIDLPYSCRAGSCSSCAGKLKTGSLNQDDQSFLD\n"
            + "       |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n"
            + "1A70|A AAYKVTLVTPTGNVEFQCPDDVYILDAAEEEGIDLPYSCRAGSCSSCAGKLKTGSLNQDDQSFLD\n\n"

            + "P00221 DDQIDEGWVLTCAAYPVSDVTIETHKEEELTA\n"
            + "       |||||||||||||||||||||||||| |||||\n"
            + "1A70|A DDQIDEGWVLTCAAYPVSDVTIETHKKEELTA\n\n" +

            "Length of alignment = 97\n" + "Percentage ID = 98.97\n";

    Assert.assertEquals(strucMapping.getMappingDetailsOutput(),
            expectedMappingOutput);

    // Can't do Assert.assertEquals(strucMapping.getMapping(), expectedMapping);
    // because this fails in our version of TestNG
    Assert.assertEquals(strucMapping.getMapping().size(),
            expectedMapping.size());
    Iterator<Map.Entry<Integer, int[]>> it = expectedMapping.entrySet()
            .iterator();
    while (it.hasNext())
    {
      Map.Entry<Integer, int[]> pair = it.next();
      Assert.assertTrue(
              strucMapping.getMapping().containsKey(pair.getKey()));
      Assert.assertEquals(strucMapping.getMapping().get(pair.getKey()),
              pair.getValue());
    }
  }

  @Test(groups = { "Network" })
  public void getEntityCountTest()
  {
    int actualEntityCount = siftsClient.getEntityCount();
    System.out.println("actual entity count : " + actualEntityCount);
    Assert.assertEquals(actualEntityCount, 1);
  }

  @Test(groups = { "Network" })
  public void getDbAccessionIdTest()
  {
    String actualDbAccId = siftsClient.getDbAccessionId();
    System.out.println("Actual Db Accession Id: " + actualDbAccId);
    Assert.assertEquals(actualDbAccId, "1a70");
  }

  @Test(groups = { "Network" })
  public void getDbCoordSysTest()
  {
    String actualDbCoordSys = siftsClient.getDbCoordSys();
    System.out.println("Actual DbCoordSys: " + actualDbCoordSys);
    Assert.assertEquals(actualDbCoordSys, "PDBe");
  }

  @Test(groups = { "Network" })
  public void getDbSourceTest()
  {
    String actualDbSource = siftsClient.getDbSource();
    System.out.println("Actual DbSource: " + actualDbSource);
    Assert.assertEquals(actualDbSource, "PDBe");
  }

  @Test(groups = { "Network" })
  public void getDbVersionTest()
  {
    String actualDbVersion = siftsClient.getDbVersion();
    System.out.println("Actual DbVersion: " + actualDbVersion);
    Assert.assertEquals(actualDbVersion, "2.0");
  }

  @Test(groups = { "Network" })
  public void getEntityByMostOptimalMatchedIdTest1()
          throws IOException, SiftsException
  {
    SiftsClient siftsClientX = null;
    PDBfile pdbFile;
    pdbFile = new PDBfile(false, false, false,
            "test/jalview/io/2nq2" + ".pdb", DataSourceType.FILE);
    siftsClientX = new SiftsClient(pdbFile);
    Entity entityA = siftsClientX.getEntityByMostOptimalMatchedId("A");
    Assert.assertEquals(entityA.getEntityId(), "A");
    Entity entityB = siftsClientX.getEntityByMostOptimalMatchedId("B");
    Assert.assertEquals(entityB.getEntityId(), "C");
    Entity entityC = siftsClientX.getEntityByMostOptimalMatchedId("C");
    Assert.assertEquals(entityC.getEntityId(), "B");
    Entity entityD = siftsClientX.getEntityByMostOptimalMatchedId("D");
    Assert.assertEquals(entityD.getEntityId(), "D");

  }

  @Test(groups = { "Network" })
  public void getEntityByMostOptimalMatchedIdTest2()
          throws IOException, SiftsException
  {
    // This test is for a SIFTS file in which entity A should map to chain P for
    // the given PDB Id. All the other chains shouldn't be mapped as there are
    // no SIFTS entity records for them.
    SiftsClient siftsClientX = null;
    PDBfile pdbFile;
    pdbFile = new PDBfile(false, false, false, "test/jalview/io/3ucu.cif",
            DataSourceType.FILE);
    siftsClientX = new SiftsClient(pdbFile);
    Entity entityA = siftsClientX.getEntityByMostOptimalMatchedId("P");
    Entity entityP = siftsClientX.getEntityByMostOptimalMatchedId("A");
    Entity entityR = siftsClientX.getEntityByMostOptimalMatchedId("R");
    Assert.assertEquals(entityA.getEntityId(), "A");
    Assert.assertNotEquals(entityR, "A");
    Assert.assertNotEquals(entityP, "A");
    Assert.assertNotEquals(entityR, "R");
    Assert.assertNotEquals(entityP, "P");
    Assert.assertNull(entityR);
    Assert.assertNull(entityP);

  }

  @Test(groups = { "Network" })
  public void getLeadingIntegerFromString()
  {
    Assert.assertEquals(SiftsClient.getLeadingIntegerValue("1234abcd", -1),
            1234);
    Assert.assertEquals(SiftsClient.getLeadingIntegerValue("1234", -1),
            1234);
    Assert.assertEquals(SiftsClient.getLeadingIntegerValue("abcd", -1), -1);
    Assert.assertEquals(SiftsClient.getLeadingIntegerValue("abcd1234", -1),
            -1);
    Assert.assertEquals(SiftsClient.getLeadingIntegerValue("None", -1), -1);
    Assert.assertEquals(SiftsClient.getLeadingIntegerValue("Null", -1), -1);
  }
}
