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
package jalview.ext.ensembl;

import static org.testng.AssertJUnit.assertEquals;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FastaFile;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyLite;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EnsemblSeqProxyTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private static final Object[][] allSeqs = new Object[][] { {
      new EnsemblProtein(), "CCDS5863.1",
      ">CCDS5863.1\n"
              + "MAALSGGGGGGAEPGQALFNGDMEPEAGAGAGAAASSAADPAIPEEVWNIKQMIKLTQEH\n"
              + "IEALLDKFGGEHNPPSIYLEAYEEYTSKLDALQQREQQLLESLGNGTDFSVSSSASMDTV\n"
              + "TSSSSSSLSVLPSSLSVFQNPTDVARSNPKSPQKPIVRVFLPNKQRTVVPARCGVTVRDS\n"
              + "LKKALMMRGLIPECCAVYRIQDGEKKPIGWDTDISWLTGEELHVEVLENVPLTTHNFVRK\n"
              + "TFFTLAFCDFCRKLLFQGFRCQTCGYKFHQRCSTEVPLMCVNYDQLDLLFVSKFFEHHPI\n"
              + "PQEEASLAETALTSGSSPSAPASDSIGPQILTSPSPSKSIPIPQPFRPADEDHRNQFGQR\n"
              + "DRSSSAPNVHINTIEPVNIDDLIRDQGFRGDGGSTTGLSATPPASLPGSLTNVKALQKSP\n"
              + "GPQRERKSSSSSEDRNRMKTLGRRDSSDDWEIPDGQITVGQRIGSGSFGTVYKGKWHGDV\n"
              + "AVKMLNVTAPTPQQLQAFKNEVGVLRKTRHVNILLFMGYSTKPQLAIVTQWCEGSSLYHH\n"
              + "LHIIETKFEMIKLIDIARQTAQGMDYLHAKSIIHRDLKSNNIFLHEDLTVKIGDFGLATV\n"
              + "KSRWSGSHQFEQLSGSILWMAPEVIRMQDKNPYSFQSDVYAFGIVLYELMTGQLPYSNIN\n"
              + "NRDQIIFMVGRGYLSPDLSKVRSNCPKAMKRLMAECLKKKRDERPLFPQILASIELLARS\n"
              + "LPKIHRSASEPSLNRAGFQTEDFSLYACASPKTPIQAGGYGAFPVH\n" },
      { new EnsemblCdna(), "CCDS5863.1", ">CCDS5863.1\n"
              + "ATGGCGGCGCTGAGCGGTGGCGGTGGTGGCGGCGCGGAGCCGGGCCAGGCTCTGTTCAAC\n"
              + "GGGGACATGGAGCCCGAGGCCGGCGCCGGCGCCGGCGCCGCGGCCTCTTCGGCTGCGGAC\n"
              + "CCTGCCATTCCGGAGGAGGTGTGGAATATCAAACAAATGATTAAGTTGACACAGGAACAT\n"
              + "ATAGAGGCCCTATTGGACAAATTTGGTGGGGAGCATAATCCACCATCAATATATCTGGAG\n"
              + "GCCTATGAAGAATACACCAGCAAGCTAGATGCACTCCAACAAAGAGAACAACAGTTATTG\n"
              + "GAATCTCTGGGGAACGGAACTGATTTTTCTGTTTCTAGCTCTGCATCAATGGATACCGTT\n"
              + "ACATCTTCTTCCTCTTCTAGCCTTTCAGTGCTACCTTCATCTCTTTCAGTTTTTCAAAAT\n"
              + "CCCACAGATGTGGCACGGAGCAACCCCAAGTCACCACAAAAACCTATCGTTAGAGTCTTC\n"
              + "CTGCCCAACAAACAGAGGACAGTGGTACCTGCAAGGTGTGGAGTTACAGTCCGAGACAGT\n"
              + "CTAAAGAAAGCACTGATGATGAGAGGTCTAATCCCAGAGTGCTGTGCTGTTTACAGAATT\n"
              + "CAGGATGGAGAGAAGAAACCAATTGGTTGGGACACTGATATTTCCTGGCTTACTGGAGAA\n"
              + "GAATTGCATGTGGAAGTGTTGGAGAATGTTCCACTTACAACACACAACTTTGTACGAAAA\n"
              + "ACGTTTTTCACCTTAGCATTTTGTGACTTTTGTCGAAAGCTGCTTTTCCAGGGTTTCCGC\n"
              + "TGTCAAACATGTGGTTATAAATTTCACCAGCGTTGTAGTACAGAAGTTCCACTGATGTGT\n"
              + "GTTAATTATGACCAACTTGATTTGCTGTTTGTCTCCAAGTTCTTTGAACACCACCCAATA\n"
              + "CCACAGGAAGAGGCGTCCTTAGCAGAGACTGCCCTAACATCTGGATCATCCCCTTCCGCA\n"
              + "CCCGCCTCGGACTCTATTGGGCCCCAAATTCTCACCAGTCCGTCTCCTTCAAAATCCATT\n"
              + "CCAATTCCACAGCCCTTCCGACCAGCAGATGAAGATCATCGAAATCAATTTGGGCAACGA\n"
              + "GACCGATCCTCATCAGCTCCCAATGTGCATATAAACACAATAGAACCTGTCAATATTGAT\n"
              + "GACTTGATTAGAGACCAAGGATTTCGTGGTGATGGAGGATCAACCACAGGTTTGTCTGCT\n"
              + "ACCCCCCCTGCCTCATTACCTGGCTCACTAACTAACGTGAAAGCCTTACAGAAATCTCCA\n"
              + "GGACCTCAGCGAGAAAGGAAGTCATCTTCATCCTCAGAAGACAGGAATCGAATGAAAACA\n"
              + "CTTGGTAGACGGGACTCGAGTGATGATTGGGAGATTCCTGATGGGCAGATTACAGTGGGA\n"
              + "CAAAGAATTGGATCTGGATCATTTGGAACAGTCTACAAGGGAAAGTGGCATGGTGATGTG\n"
              + "GCAGTGAAAATGTTGAATGTGACAGCACCTACACCTCAGCAGTTACAAGCCTTCAAAAAT\n"
              + "GAAGTAGGAGTACTCAGGAAAACACGACATGTGAATATCCTACTCTTCATGGGCTATTCC\n"
              + "ACAAAGCCACAACTGGCTATTGTTACCCAGTGGTGTGAGGGCTCCAGCTTGTATCACCAT\n"
              + "CTCCATATCATTGAGACCAAATTTGAGATGATCAAACTTATAGATATTGCACGACAGACT\n"
              + "GCACAGGGCATGGATTACTTACACGCCAAGTCAATCATCCACAGAGACCTCAAGAGTAAT\n"
              + "AATATATTTCTTCATGAAGACCTCACAGTAAAAATAGGTGATTTTGGTCTAGCTACAGTG\n"
              + "AAATCTCGATGGAGTGGGTCCCATCAGTTTGAACAGTTGTCTGGATCCATTTTGTGGATG\n"
              + "GCACCAGAAGTCATCAGAATGCAAGATAAAAATCCATACAGCTTTCAGTCAGATGTATAT\n"
              + "GCATTTGGAATTGTTCTGTATGAATTGATGACTGGACAGTTACCTTATTCAAACATCAAC\n"
              + "AACAGGGACCAGATAATTTTTATGGTGGGACGAGGATACCTGTCTCCAGATCTCAGTAAG\n"
              + "GTACGGAGTAACTGTCCAAAAGCCATGAAGAGATTAATGGCAGAGTGCCTCAAAAAGAAA\n"
              + "AGAGATGAGAGACCACTCTTTCCCCAAATTCTCGCCTCTATTGAGCTGCTGGCCCGCTCA\n"
              + "TTGCCAAAAATTCACCGCAGTGCATCAGAACCCTCCTTGAATCGGGCTGGTTTCCAAACA\n"
              + "GAGGATTTTAGTCTATATGCTTGTGCTTCTCCAAAAACACCCATCCAGGCAGGGGGATAT\n"
              + "GGTGCGTTTCCTGTCCACTGA\n" },
      { new EnsemblProtein(), "ENSP00000288602", ">ENSP00000288602\n"
              + "MAALSGGGGGGAEPGQALFNGDMEPEAGAGAGAAASSAADPAIPEEVWNIKQMIKLTQEH\n"
              + "IEALLDKFGGEHNPPSIYLEAYEEYTSKLDALQQREQQLLESLGNGTDFSVSSSASMDTV\n"
              + "TSSSSSSLSVLPSSLSVFQNPTDVARSNPKSPQKPIVRVFLPNKQRTVVPARCGVTVRDS\n"
              + "LKKALMMRGLIPECCAVYRIQDGEKKPIGWDTDISWLTGEELHVEVLENVPLTTHNFVRK\n"
              + "TFFTLAFCDFCRKLLFQGFRCQTCGYKFHQRCSTEVPLMCVNYDQLDLLFVSKFFEHHPI\n"
              + "PQEEASLAETALTSGSSPSAPASDSIGPQILTSPSPSKSIPIPQPFRPADEDHRNQFGQR\n"
              + "DRSSSAPNVHINTIEPVNIDDLIRDQGFRGDG\n"
              // ? insertion added in ENSP00000288602.11, not in P15056
              + "APLNQLMRCLRKYQSRTPSPLLHSVPSEIVFDFEPGPVFR\n"
              // end insertion
              + "GSTTGLSATPPASLPGSLTNVKALQKSP\n"
              + "GPQRERKSSSSSEDRNRMKTLGRRDSSDDWEIPDGQITVGQRIGSGSFGTVYKGKWHGDV\n"
              + "AVKMLNVTAPTPQQLQAFKNEVGVLRKTRHVNILLFMGYSTKPQLAIVTQWCEGSSLYHH\n"
              + "LHIIETKFEMIKLIDIARQTAQGMDYLHAKSIIHRDLKSNNIFLHEDLTVKIGDFGLATV\n"
              + "KSRWSGSHQFEQLSGSILWMAPEVIRMQDKNPYSFQSDVYAFGIVLYELMTGQLPYSNIN\n"
              + "NRDQIIFMVGRGYLSPDLSKVRSNCPKAMKRLMAECLKKKRDERPLFPQILASIELLARS\n"
              + "LPKIHRSASEPSLNRAGFQTEDFSLYACASPKTPIQAGGYGAFPVH" } };

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    SequenceOntologyFactory.setInstance(new SequenceOntologyLite());
  }

  @AfterClass(alwaysRun = true)
  public void tearDown()
  {
    SequenceOntologyFactory.setInstance(null);
  }

  @DataProvider(name = "ens_seqs")
  public Object[][] createData(Method m)
  {
    System.out.println(m.getName());
    return allSeqs;
  }

  @Test(dataProvider = "ens_seqs", suiteName = "live")
  public void testGetSequenceRecords(EnsemblSeqProxy proxy, String sq,
          String fastasq) throws Exception
  {
    FastaFile trueRes = new FastaFile(fastasq, DataSourceType.PASTE);
    SequenceI[] expected = trueRes.getSeqsAsArray();
    AlignmentI retrieved = proxy.getSequenceRecords(sq);

    Assert.assertEquals(retrieved.getHeight(), expected.length,
            "Different number of sequences retrieved for query " + sq);

    for (SequenceI tr : expected)
    {
      SequenceI[] rseq;
      Assert.assertNotNull(rseq = retrieved.findSequenceMatch(tr.getName()),
              "Couldn't find sequences matching expected sequence "
                      + tr.getName());
      Assert.assertEquals(rseq.length, 1,
              "Expected only one sequence for sequence ID " + tr.getName());
      Assert.assertEquals(rseq[0].getSequenceAsString(),
              tr.getSequenceAsString(),
              "Sequences differ for " + tr.getName() + "\n" + "Exp:"
                      + tr.getSequenceAsString() + "\n" + "Got:"
                      + rseq[0].getSequenceAsString());
    }
  }

  @Test(groups = "Functional")
  public void getGenomicRangesFromFeatures()
  {

  }

  /**
   * Test the method that appends a single allele's reverse complement to a
   * string buffer
   */
  @Test(groups = "Functional")
  public void testReverseComplementAllele()
  {
    StringBuilder sb = new StringBuilder();
    EnsemblSeqProxy.reverseComplementAllele(sb, "G"); // comp=C
    EnsemblSeqProxy.reverseComplementAllele(sb, "g"); // comp=c
    EnsemblSeqProxy.reverseComplementAllele(sb, "C"); // comp=G
    EnsemblSeqProxy.reverseComplementAllele(sb, "T"); // comp=A
    EnsemblSeqProxy.reverseComplementAllele(sb, "A"); // comp=T
    assertEquals("C,c,G,A,T", sb.toString());

    sb = new StringBuilder();
    EnsemblSeqProxy.reverseComplementAllele(sb, "-GATt"); // revcomp=aATC-
    EnsemblSeqProxy.reverseComplementAllele(sb, "hgmd_mutation");
    EnsemblSeqProxy.reverseComplementAllele(sb, "PhenCode_variation");
    assertEquals("aATC-,hgmd_mutation,PhenCode_variation", sb.toString());
  }

  /**
   * Test the method that computes the reverse complement of the alleles in a
   * sequence_variant feature
   */
  @Test(groups = "Functional")
  public void testReverseComplementAlleles()
  {
    String alleles = "C,G,-TAC,HGMD_MUTATION,gac";
    SequenceFeature sf = new SequenceFeature("sequence_variant", alleles, 1,
            2, 0f, null);
    sf.setValue("alleles", alleles);

    EnsemblSeqProxy.reverseComplementAlleles(sf);
    String revcomp = "G,C,GTA-,HGMD_MUTATION,gtc";
    // verify description is updated with reverse complement
    assertEquals(revcomp, sf.getDescription());
    // verify alleles attribute is updated with reverse complement
    assertEquals(revcomp, sf.getValue("alleles"));
  }
}
