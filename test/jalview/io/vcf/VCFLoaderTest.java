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
package jalview.io.vcf;

import static jalview.io.gff.SequenceOntologyI.SEQUENCE_VARIANT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureAttributes;
import jalview.datamodel.features.SequenceFeatures;
import jalview.gui.AlignFrame;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.io.gff.Gff3Helper;
import jalview.util.MapList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class VCFLoaderTest
{
  private static final float DELTA = 0.00001f;

  // columns 9717- of gene P30419 from Ensembl (much modified)
  private static final String FASTA = "" +
  /*
   * forward strand 'gene' and 'transcript' with two exons
   */
          ">gene1/1-25 chromosome:GRCh38:17:45051610:45051634:1\n"
          + "CAAGCTGGCGGACGAGAGTGTGACA\n"
          + ">transcript1/1-18\n--AGCTGGCG----AGAGTGTGAC-\n"

          /*
           * reverse strand gene and transcript (reverse complement alleles!)
           */
          + ">gene2/1-25 chromosome:GRCh38:17:45051610:45051634:-1\n"
          + "TGTCACACTCTCGTCCGCCAGCTTG\n" + ">transcript2/1-18\n"
          + "-GTCACACTCT----CGCCAGCT--\n"

          /*
           * 'gene' on chromosome 5 with two transcripts
           */
          + ">gene3/1-25 chromosome:GRCh38:5:45051610:45051634:1\n"
          + "CAAGCTGGCGGACGAGAGTGTGACA\n"
          + ">transcript3/1-18\n--AGCTGGCG----AGAGTGTGAC-\n"
          + ">transcript4/1-18\n-----TGG-GGACGAGAGTGTGA-A\n";

  private static final String[] VCF = { "##fileformat=VCFv4.2",
      // fields other than AF are ignored when parsing as they have no INFO
      // definition
      "##INFO=<ID=AF,Number=A,Type=Float,Description=\"Allele Frequency, for each ALT allele, in the same order as listed\">",
      "##INFO=<ID=AC_Female,Number=A,Type=Integer,Description=\"Allele count in Female genotypes\"",
      "##INFO=<ID=AF_AFR,Number=A,Type=Float,Description=\"Allele Frequency among African/African American genotypes\"",
      "##reference=Homo_sapiens/GRCh38",
      "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO",
      // A/T,C variants in position 2 of gene sequence (precedes transcript)
      // should create 2 variant features with respective AF values
      // malformed values for AC_Female and AF_AFR should be ignored
      "17\t45051611\trs384765\tA\tT,C\t1666.64\tRF;XYZ\tAC=15;AF=5.0e-03,4.0e-03;AC_Female=12,3d;AF_AFR=low,2.3e-4",
      // SNP G/C in position 4 of gene sequence, position 2 of transcript
      // insertion G/GA is transferred to nucleotide but not to peptide
      "17\t45051613\t.\tG\tGA,C\t1666.65\t.\tAC=15;AF=3.0e-03,2.0e-03",
      // '.' in INFO field should be ignored
      "17\t45051615\t.\tG\tC\t1666.66\tRF\tAC=16;AF=." };

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    /*
     * configure to capture all available VCF and VEP (CSQ) fields
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.setProperty("VCF_FIELDS", ".*");
    Cache.setProperty("VEP_FIELDS", ".*");
    Cache.setProperty("VCF_ASSEMBLY", "GRCh38=GRCh38");
    Console.initLogger();
  }

  @BeforeTest(alwaysRun = true)
  public void setUpBeforeTest()
  {
    /*
     * clear down feature attributes metadata
     */
    FeatureAttributes.getInstance().clear();
  }

  @Test(groups = "Functional")
  public void testDoLoad() throws IOException
  {
    AlignmentI al = buildAlignment();

    File f = makeVcfFile();
    VCFLoader loader = new VCFLoader(f.getPath());

    loader.doLoad(al.getSequencesArray(), null);

    /*
     * verify variant feature(s) added to gene
     * NB alleles at a locus may not be processed, and features added,
     * in the order in which they appear in the VCF record as method
     * VariantContext.getAlternateAlleles() does not guarantee order
     * - order of assertions here matches what we find (is not important) 
     */
    List<SequenceFeature> geneFeatures = al.getSequenceAt(0)
            .getSequenceFeatures();
    SequenceFeatures.sortFeatures(geneFeatures, true);
    assertEquals(geneFeatures.size(), 5);
    SequenceFeature sf = geneFeatures.get(0);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 2);
    assertEquals(sf.getEnd(), 2);
    assertEquals(sf.getScore(), 0f);
    assertEquals(sf.getValue("AF"), "4.0e-03");
    assertEquals(sf.getValue("AF_AFR"), "2.3e-4");
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "A,C");
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getValue("POS"), "45051611");
    assertEquals(sf.getValue("ID"), "rs384765");
    assertEquals(sf.getValue("QUAL"), "1666.64");
    assertEquals(sf.getValue("FILTER"), "RF;XYZ");
    // malformed integer for AC_Female is ignored (JAL-3375)
    assertNull(sf.getValue("AC_Female"));

    sf = geneFeatures.get(1);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 2);
    assertEquals(sf.getEnd(), 2);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 5.0e-03,
            DELTA);
    assertEquals(sf.getValue("AC_Female"), "12");
    // malformed float for AF_AFR is ignored (JAL-3375)
    assertNull(sf.getValue("AC_AFR"));
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "A,T");

    sf = geneFeatures.get(2);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 4);
    assertEquals(sf.getEnd(), 4);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 2.0e-03,
            DELTA);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "G,C");

    sf = geneFeatures.get(3);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 4);
    assertEquals(sf.getEnd(), 4);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 3.0e-03,
            DELTA);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "G,GA");
    assertNull(sf.getValue("ID")); // '.' is ignored
    assertNull(sf.getValue("FILTER")); // '.' is ignored

    sf = geneFeatures.get(4);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 6);
    assertEquals(sf.getEnd(), 6);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    // AF=. should not have been captured
    assertNull(sf.getValue("AF"));
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "G,C");

    /*
     * verify variant feature(s) added to transcript
     */
    List<SequenceFeature> transcriptFeatures = al.getSequenceAt(1)
            .getSequenceFeatures();
    assertEquals(transcriptFeatures.size(), 3);
    sf = transcriptFeatures.get(0);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 2);
    assertEquals(sf.getEnd(), 2);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 2.0e-03,
            DELTA);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "G,C");
    sf = transcriptFeatures.get(1);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 2);
    assertEquals(sf.getEnd(), 2);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 3.0e-03,
            DELTA);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "G,GA");

    /*
     * verify SNP variant feature(s) computed and added to protein
     * first codon AGC varies to ACC giving S/T
     */
    List<DBRefEntry> dbRefs = al.getSequenceAt(1).getDBRefs();
    SequenceI peptide = null;
    for (DBRefEntry dbref : dbRefs)
    {
      if (dbref.getMap().getMap().getFromRatio() == 3)
      {
        peptide = dbref.getMap().getTo();
      }
    }
    List<SequenceFeature> proteinFeatures = peptide.getSequenceFeatures();

    /*
     * JAL-3187 don't precompute protein features, do dynamically instead
     */
    assertTrue(proteinFeatures.isEmpty());
  }

  private File makeVcfFile() throws IOException
  {
    File f = File.createTempFile("Test", ".vcf");
    f.deleteOnExit();
    PrintWriter pw = new PrintWriter(f);
    for (String vcfLine : VCF)
    {
      pw.println(vcfLine);
    }
    pw.close();
    return f;
  }

  /**
   * Make a simple alignment with one 'gene' and one 'transcript'
   * 
   * @return
   */
  private AlignmentI buildAlignment()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(FASTA,
            DataSourceType.PASTE);

    /*
     * map gene1 sequence to chromosome (normally done when the sequence is fetched
     * from Ensembl and transcripts computed)
     */
    AlignmentI alignment = af.getViewport().getAlignment();
    SequenceI gene1 = alignment.findName("gene1");
    int[] to = new int[] { 45051610, 45051634 };
    int[] from = new int[] { gene1.getStart(), gene1.getEnd() };
    gene1.setGeneLoci("homo_sapiens", "GRCh38", "17",
            new MapList(from, to, 1, 1));

    /*
     * map 'transcript1' to chromosome via 'gene1'
     * transcript1/1-18 is gene1/3-10,15-24
     * which is chromosome 45051612-45051619,45051624-45051633
     */
    to = new int[] { 45051612, 45051619, 45051624, 45051633 };
    SequenceI transcript1 = alignment.findName("transcript1");
    from = new int[] { transcript1.getStart(), transcript1.getEnd() };
    transcript1.setGeneLoci("homo_sapiens", "GRCh38", "17",
            new MapList(from, to, 1, 1));

    /*
     * map gene2 to chromosome reverse strand
     */
    SequenceI gene2 = alignment.findName("gene2");
    to = new int[] { 45051634, 45051610 };
    from = new int[] { gene2.getStart(), gene2.getEnd() };
    gene2.setGeneLoci("homo_sapiens", "GRCh38", "17",
            new MapList(from, to, 1, 1));

    /*
     * map 'transcript2' to chromosome via 'gene2'
     * transcript2/1-18 is gene2/2-11,16-23
     * which is chromosome 45051633-45051624,45051619-45051612
     */
    to = new int[] { 45051633, 45051624, 45051619, 45051612 };
    SequenceI transcript2 = alignment.findName("transcript2");
    from = new int[] { transcript2.getStart(), transcript2.getEnd() };
    transcript2.setGeneLoci("homo_sapiens", "GRCh38", "17",
            new MapList(from, to, 1, 1));

    /*
     * add a protein product as a DBRef on transcript1
     */
    SequenceI peptide1 = new Sequence("ENSP001", "SWRECD");
    MapList mapList = new MapList(new int[] { 1, 18 }, new int[] { 1, 6 },
            3, 1);
    Mapping map = new Mapping(peptide1, mapList);
    DBRefEntry product = new DBRefEntry("", "", "ENSP001", map);
    transcript1.addDBRef(product);

    /*
     * add a protein product as a DBRef on transcript2
     */
    SequenceI peptide2 = new Sequence("ENSP002", "VTLSPA");
    mapList = new MapList(new int[] { 1, 18 }, new int[] { 1, 6 }, 3, 1);
    map = new Mapping(peptide2, mapList);
    product = new DBRefEntry("", "", "ENSP002", map);
    transcript2.addDBRef(product);

    /*
     * map gene3 to chromosome 
     */
    SequenceI gene3 = alignment.findName("gene3");
    to = new int[] { 45051610, 45051634 };
    from = new int[] { gene3.getStart(), gene3.getEnd() };
    gene3.setGeneLoci("homo_sapiens", "GRCh38", "5",
            new MapList(from, to, 1, 1));

    /*
     * map 'transcript3' to chromosome
     */
    SequenceI transcript3 = alignment.findName("transcript3");
    to = new int[] { 45051612, 45051619, 45051624, 45051633 };
    from = new int[] { transcript3.getStart(), transcript3.getEnd() };
    transcript3.setGeneLoci("homo_sapiens", "GRCh38", "5",
            new MapList(from, to, 1, 1));

    /*
     * map 'transcript4' to chromosome
     */
    SequenceI transcript4 = alignment.findName("transcript4");
    to = new int[] { 45051615, 45051617, 45051619, 45051632, 45051634,
        45051634 };
    from = new int[] { transcript4.getStart(), transcript4.getEnd() };
    transcript4.setGeneLoci("homo_sapiens", "GRCh38", "5",
            new MapList(from, to, 1, 1));

    /*
     * add a protein product as a DBRef on transcript3
     */
    SequenceI peptide3 = new Sequence("ENSP003", "SWRECD");
    mapList = new MapList(new int[] { 1, 18 }, new int[] { 1, 6 }, 3, 1);
    map = new Mapping(peptide3, mapList);
    product = new DBRefEntry("", "", "ENSP003", map);
    transcript3.addDBRef(product);

    return alignment;
  }

  /**
   * Test with 'gene' and 'transcript' mapped to the reverse strand of the
   * chromosome. The VCF variant positions (in forward coordinates) should get
   * correctly located on sequence positions.
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testDoLoad_reverseStrand() throws IOException
  {
    AlignmentI al = buildAlignment();

    File f = makeVcfFile();

    VCFLoader loader = new VCFLoader(f.getPath());

    loader.doLoad(al.getSequencesArray(), null);

    /*
     * verify variant feature(s) added to gene2
     * gene2/1-25 maps to chromosome 45051634- reverse strand
     */
    List<SequenceFeature> geneFeatures = al.getSequenceAt(2)
            .getSequenceFeatures();
    SequenceFeatures.sortFeatures(geneFeatures, true);
    assertEquals(geneFeatures.size(), 5);
    SequenceFeature sf;

    /*
     * insertion G/GA at 45051613 maps to an insertion at
     * the preceding position (21) on reverse strand gene
     * reference: CAAGC -> GCTTG/21-25
     * genomic variant: CAAGAC (G/GA)
     * gene variant: GTCTTG (G/GT at 21)
     */
    sf = geneFeatures.get(1);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 21);
    assertEquals(sf.getEnd(), 21);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "G,GT");
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 3.0e-03,
            DELTA);

    /*
     * variant G/C at 45051613 maps to C/G at gene position 22
     */
    sf = geneFeatures.get(2);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 22);
    assertEquals(sf.getEnd(), 22);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "C,G");
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 2.0e-03,
            DELTA);

    /*
     * variant A/C at 45051611 maps to T/G at gene position 24
     */
    sf = geneFeatures.get(3);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 24);
    assertEquals(sf.getEnd(), 24);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "T,G");
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 4.0e-03,
            DELTA);

    /*
     * variant A/T at 45051611 maps to T/A at gene position 24
     */
    sf = geneFeatures.get(4);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 24);
    assertEquals(sf.getEnd(), 24);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "T,A");
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 5.0e-03,
            DELTA);

    /*
     * verify 3 variant features added to transcript2
     */
    List<SequenceFeature> transcriptFeatures = al.getSequenceAt(3)
            .getSequenceFeatures();
    assertEquals(transcriptFeatures.size(), 3);

    /*
     * insertion G/GT at position 21 of gene maps to position 16 of transcript
     */
    sf = transcriptFeatures.get(1);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 16);
    assertEquals(sf.getEnd(), 16);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "G,GT");
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 3.0e-03,
            DELTA);

    /*
     * SNP C/G at position 22 of gene maps to position 17 of transcript
     */
    sf = transcriptFeatures.get(2);
    assertEquals(sf.getFeatureGroup(), "VCF");
    assertEquals(sf.getBegin(), 17);
    assertEquals(sf.getEnd(), 17);
    assertEquals(sf.getType(), SEQUENCE_VARIANT);
    assertEquals(sf.getScore(), 0f);
    assertEquals(sf.getValue(Gff3Helper.ALLELES), "C,G");
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 2.0e-03,
            DELTA);

    /*
     * verify variant feature(s) computed and added to protein
     * last codon GCT varies to GGT giving A/G in the last peptide position
     */
    List<DBRefEntry> dbRefs = al.getSequenceAt(3).getDBRefs();
    SequenceI peptide = null;
    for (DBRefEntry dbref : dbRefs)
    {
      if (dbref.getMap().getMap().getFromRatio() == 3)
      {
        peptide = dbref.getMap().getTo();
      }
    }
    List<SequenceFeature> proteinFeatures = peptide.getSequenceFeatures();

    /*
     * JAL-3187 don't precompute protein features, do dynamically instead
     */
    assertTrue(proteinFeatures.isEmpty());
  }

  /**
   * Tests that if VEP consequence (CSQ) data is present in the VCF data, then
   * it is added to the variant feature, but restricted where possible to the
   * consequences for a specific transcript
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testDoLoad_vepCsq() throws IOException
  {
    AlignmentI al = buildAlignment();

    VCFLoader loader = new VCFLoader("test/jalview/io/vcf/testVcf.vcf");

    /*
     * VCF data file with variants at gene3 positions
     * 1 C/A
     * 5 C/T
     * 9 CGT/C (deletion)
     * 13 C/G, C/T
     * 17 A/AC (insertion), A/G
     */
    loader.doLoad(al.getSequencesArray(), null);

    /*
     * verify variant feature(s) added to gene3
     */
    List<SequenceFeature> geneFeatures = al.findName("gene3")
            .getSequenceFeatures();
    SequenceFeatures.sortFeatures(geneFeatures, true);
    assertEquals(geneFeatures.size(), 7);
    SequenceFeature sf = geneFeatures.get(0);
    assertEquals(sf.getBegin(), 1);
    assertEquals(sf.getEnd(), 1);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.1f, DELTA);
    assertEquals(sf.getValue("alleles"), "C,A");
    // gene features include Consequence for all transcripts
    Map map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);
    assertEquals(map.get("PolyPhen"), "Bad");

    sf = geneFeatures.get(1);
    assertEquals(sf.getBegin(), 5);
    assertEquals(sf.getEnd(), 5);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.2f, DELTA);
    assertEquals(sf.getValue("alleles"), "C,T");
    map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);
    assertEquals(map.get("PolyPhen"), "Bad;;"); // %3B%3B decoded

    sf = geneFeatures.get(2);
    assertEquals(sf.getBegin(), 9);
    assertEquals(sf.getEnd(), 11); // deletion over 3 positions
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.3f, DELTA);
    assertEquals(sf.getValue("alleles"), "CGG,C");
    map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);

    sf = geneFeatures.get(3);
    assertEquals(sf.getBegin(), 13);
    assertEquals(sf.getEnd(), 13);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.5f, DELTA);
    assertEquals(sf.getValue("alleles"), "C,T");
    map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);

    sf = geneFeatures.get(4);
    assertEquals(sf.getBegin(), 13);
    assertEquals(sf.getEnd(), 13);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.4f, DELTA);
    assertEquals(sf.getValue("alleles"), "C,G");
    map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);

    sf = geneFeatures.get(5);
    assertEquals(sf.getBegin(), 17);
    assertEquals(sf.getEnd(), 17);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.7f, DELTA);
    assertEquals(sf.getValue("alleles"), "A,G");
    map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);

    sf = geneFeatures.get(6);
    assertEquals(sf.getBegin(), 17);
    assertEquals(sf.getEnd(), 17); // insertion
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.6f, DELTA);
    assertEquals(sf.getValue("alleles"), "A,AC");
    map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);

    /*
     * verify variant feature(s) added to transcript3
     * at columns 5 (1), 17 (2), positions 3, 11
     * note the deletion at columns 9-11 is not transferred since col 11
     * has no mapping to transcript 3 
     */
    List<SequenceFeature> transcriptFeatures = al.findName("transcript3")
            .getSequenceFeatures();
    SequenceFeatures.sortFeatures(transcriptFeatures, true);
    assertEquals(transcriptFeatures.size(), 3);
    sf = transcriptFeatures.get(0);
    assertEquals(sf.getBegin(), 3);
    assertEquals(sf.getEnd(), 3);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.2f, DELTA);
    assertEquals(sf.getValue("alleles"), "C,T");
    // transcript features only have Consequence for that transcripts
    map = (Map) sf.getValue("CSQ");
    assertEquals(map.size(), 9);
    assertEquals(sf.getValueAsString("CSQ", "Feature"), "transcript3");

    sf = transcriptFeatures.get(1);
    assertEquals(sf.getBegin(), 11);
    assertEquals(sf.getEnd(), 11);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.7f, DELTA);
    assertEquals(sf.getValue("alleles"), "A,G");
    assertEquals(map.size(), 9);
    assertEquals(sf.getValueAsString("CSQ", "Feature"), "transcript3");

    sf = transcriptFeatures.get(2);
    assertEquals(sf.getBegin(), 11);
    assertEquals(sf.getEnd(), 11);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.6f, DELTA);
    assertEquals(sf.getValue("alleles"), "A,AC");
    assertEquals(map.size(), 9);
    assertEquals(sf.getValueAsString("CSQ", "Feature"), "transcript3");

    /*
     * verify variants computed on protein product for transcript3
     * peptide is SWRECD
     * codon variants are AGC/AGT position 1 which is synonymous
     * and GAG/GGG which is E/G in position 4
     * the insertion variant is not transferred to the peptide
     */
    List<DBRefEntry> dbRefs = al.findName("transcript3").getDBRefs();
    SequenceI peptide = null;
    for (DBRefEntry dbref : dbRefs)
    {
      if (dbref.getMap().getMap().getFromRatio() == 3)
      {
        peptide = dbref.getMap().getTo();
      }
    }
    List<SequenceFeature> proteinFeatures = peptide.getSequenceFeatures();
    /*
     * JAL-3187 don't precompute protein features, do dynamically instead
     */
    assertTrue(proteinFeatures.isEmpty());
    // SequenceFeatures.sortFeatures(proteinFeatures, true);
    // assertEquals(proteinFeatures.size(), 2);
    // sf = proteinFeatures.get(0);
    // assertEquals(sf.getFeatureGroup(), "VCF");
    // assertEquals(sf.getBegin(), 1);
    // assertEquals(sf.getEnd(), 1);
    // assertEquals(sf.getType(), SequenceOntologyI.SYNONYMOUS_VARIANT);
    // assertEquals(sf.getDescription(), "agC/agT");
    // sf = proteinFeatures.get(1);
    // assertEquals(sf.getFeatureGroup(), "VCF");
    // assertEquals(sf.getBegin(), 4);
    // assertEquals(sf.getEnd(), 4);
    // assertEquals(sf.getType(), SequenceOntologyI.NONSYNONYMOUS_VARIANT);
    // assertEquals(sf.getDescription(), "p.Glu4Gly");

    /*
     * verify variant feature(s) added to transcript4
     * at columns 13 (2) and 17 (2), positions 7 and 11
     */
    transcriptFeatures = al.findName("transcript4").getSequenceFeatures();
    SequenceFeatures.sortFeatures(transcriptFeatures, true);
    assertEquals(transcriptFeatures.size(), 4);
    sf = transcriptFeatures.get(0);
    assertEquals(sf.getBegin(), 7);
    assertEquals(sf.getEnd(), 7);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.5f, DELTA);
    assertEquals(sf.getValue("alleles"), "C,T");
    assertEquals(map.size(), 9);
    assertEquals(sf.getValueAsString("CSQ", "Feature"), "transcript4");

    sf = transcriptFeatures.get(1);
    assertEquals(sf.getBegin(), 7);
    assertEquals(sf.getEnd(), 7);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.4f, DELTA);
    assertEquals(sf.getValue("alleles"), "C,G");
    assertEquals(map.size(), 9);
    assertEquals(sf.getValueAsString("CSQ", "Feature"), "transcript4");

    sf = transcriptFeatures.get(2);
    assertEquals(sf.getBegin(), 11);
    assertEquals(sf.getEnd(), 11);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.7f, DELTA);
    assertEquals(sf.getValue("alleles"), "A,G");
    assertEquals(map.size(), 9);
    assertEquals(sf.getValueAsString("CSQ", "Feature"), "transcript4");

    sf = transcriptFeatures.get(3);
    assertEquals(sf.getBegin(), 11);
    assertEquals(sf.getEnd(), 11);
    assertEquals(sf.getScore(), 0f);
    assertEquals(Float.parseFloat((String) sf.getValue("AF")), 0.6f, DELTA);
    assertEquals(sf.getValue("alleles"), "A,AC");
    assertEquals(map.size(), 9);
    assertEquals(sf.getValueAsString("CSQ", "Feature"), "transcript4");
  }

  /**
   * A test that demonstrates loading a contig sequence from an indexed sequence
   * database which is the reference for a VCF file
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testLoadVCFContig() throws IOException
  {
    VCFLoader loader = new VCFLoader("test/jalview/io/vcf/testVcf2.vcf");

    SequenceI seq = loader.loadVCFContig("contig123");
    assertEquals(seq.getLength(), 15);
    assertEquals(seq.getSequenceAsString(), "AAAAACCCCCGGGGG");
    List<SequenceFeature> features = seq.getSequenceFeatures();
    SequenceFeatures.sortFeatures(features, true);
    assertEquals(features.size(), 2);
    SequenceFeature sf = features.get(0);
    assertEquals(sf.getBegin(), 8);
    assertEquals(sf.getEnd(), 8);
    assertEquals(sf.getDescription(), "C,A");
    sf = features.get(1);
    assertEquals(sf.getBegin(), 12);
    assertEquals(sf.getEnd(), 12);
    assertEquals(sf.getDescription(), "G,T");

    seq = loader.loadVCFContig("contig789");
    assertEquals(seq.getLength(), 25);
    assertEquals(seq.getSequenceAsString(), "GGGGGTTTTTAAAAACCCCCGGGGG");
    features = seq.getSequenceFeatures();
    SequenceFeatures.sortFeatures(features, true);
    assertEquals(features.size(), 2);
    sf = features.get(0);
    assertEquals(sf.getBegin(), 2);
    assertEquals(sf.getEnd(), 2);
    assertEquals(sf.getDescription(), "G,T");
    sf = features.get(1);
    assertEquals(sf.getBegin(), 21);
    assertEquals(sf.getEnd(), 21);
    assertEquals(sf.getDescription(), "G,A");

    seq = loader.loadVCFContig("contig456");
    assertEquals(seq.getLength(), 20);
    assertEquals(seq.getSequenceAsString(), "CCCCCGGGGGTTTTTAAAAA");
    features = seq.getSequenceFeatures();
    SequenceFeatures.sortFeatures(features, true);
    assertEquals(features.size(), 1);
    sf = features.get(0);
    assertEquals(sf.getBegin(), 15);
    assertEquals(sf.getEnd(), 15);
    assertEquals(sf.getDescription(), "T,C");
  }
}