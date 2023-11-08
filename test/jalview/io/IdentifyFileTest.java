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
package jalview.io;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.gui.JvOptionPane;

public class IdentifyFileTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" }, dataProvider = "identifyFiles")
  public void testIdentify(String data, FileFormatI expectedFileType)
          throws FileFormatException
  {
    DataSourceType protocol = DataSourceType.FILE;
    IdentifyFile ider = new IdentifyFile();
    FileFormatI actualFiletype = ider.identify(data, protocol);
    Assert.assertSame(actualFiletype, expectedFileType,
            "File identification Failed!");
  }

  /**
   * Additional tests for Jalview features file
   * 
   * @throws FileFormatException
   */
  @Test(groups = "Functional")
  public void testIdentify_featureFile() throws FileFormatException
  {
    IdentifyFile ider = new IdentifyFile();

    /*
     * Jalview format with features only, no feature colours
     */
    String data = "Iron-sulfur (2Fe-2S)\tFER_CAPAA\t-1\t39\t39\tMETAL\n"
            + "Iron-phosphorus (2Fe-P)\tID_NOT_SPECIFIED\t2\t86\t87\tMETALLIC\n";
    assertSame(FileFormat.Features,
            ider.identify(data, DataSourceType.PASTE));

    /*
     * Jalview feature colour followed by GFF format feature data
     */
    data = "METAL\tcc9900\n" + "GFF\n"
            + "FER_CAPAA\tuniprot\tMETAL\t44\t45\t4.0\t.\t.\n";
    assertSame(FileFormat.Features,
            ider.identify(data, DataSourceType.PASTE));

    /*
     * Feature with '<' in the name (JAL-2098)
     */
    data = "kD < 3\tred\n" + "Low kD\tFER_CAPAA\t-1\t39\t39\tkD < 3\n";
    assertSame(FileFormat.Features,
            ider.identify(data, DataSourceType.PASTE));
  }

  @DataProvider(name = "identifyFiles")
  public Object[][] IdentifyFileDP()
  {
    return new Object[][] { { "examples/example.json", FileFormat.Json },
        { "examples/plantfdx.fa", FileFormat.Fasta },
        { "examples/dna_interleaved.phy", FileFormat.Phylip },
        { "examples/2GIS.pdb", FileFormat.PDB },
        { "examples/RF00031_folded.stk", FileFormat.Stockholm },
        { "examples/testdata/test.rnaml", FileFormat.Rnaml },
        { "examples/testdata/test.aln", FileFormat.Clustal },
        { "examples/testdata/test.pfam", FileFormat.Pfam },
        { "examples/testdata/test.msf", FileFormat.MSF },
        { "examples/testdata/test.pir", FileFormat.PIR },
        { "examples/testdata/test.html", FileFormat.Html },
        { "examples/testdata/test.pileup", FileFormat.Pileup },
        { "examples/testdata/test.blc", FileFormat.BLC },
        { "test/jalview/io/J03321.embl.txt", FileFormat.Embl },
        { "test/jalview/io/J03321.gb", FileFormat.GenBank },
        { "examples/exampleFeatures.txt", FileFormat.Features },
        { "examples/testdata/simpleGff3.gff", FileFormat.Features },
        { "examples/testdata/test.jvp", FileFormat.Jalview },
        { "examples/testdata/test.cif", FileFormat.MMCif },
        { "examples/testdata/cullpdb_pc25_res3.0_R0.3_d150729_chains9361.fasta.15316",
            FileFormat.Fasta },
        { "resources/scoreModel/pam250.scm", FileFormat.ScoreMatrix },
        { "resources/scoreModel/blosum80.scm", FileFormat.ScoreMatrix }
        // { "examples/testdata/test.amsa", "AMSA" },
        // { "examples/test.jnet", "JnetFile" },
    };
  }

  @Test(groups = "Functional")
  public void testLooksLikeFeatureData()
  {
    IdentifyFile id = new IdentifyFile();
    assertFalse(id.looksLikeFeatureData(null));
    assertFalse(id.looksLikeFeatureData(""));
    // too few columns:
    assertFalse(id.looksLikeFeatureData("1 \t 2 \t 3 \t 4 \t 5"));
    // GFF format:
    assertTrue(
            id.looksLikeFeatureData("Seq1\tlocal\tHelix\t2456\t2462\tss"));
    // Jalview format:
    assertTrue(id.looksLikeFeatureData("Helix\tSeq1\t-1\t2456\t2462\tss"));
    // non-numeric start column:
    assertFalse(id.looksLikeFeatureData("Helix\tSeq1\t-1\t.\t2462\tss"));
    // non-numeric start column:
    assertFalse(id.looksLikeFeatureData("Helix\tSeq1\t-1\t2456\t.\tss"));
  }
}
