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
package jalview.ext.htsjdk;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.testng.annotations.Test;

public class VCFReaderTest
{
  private static final String[] VCF = new String[] { "##fileformat=VCFv4.2",
      "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO",
      "20\t3\t.\tC\tG\t.\tPASS\tDP=100", // SNP C/G
      "20\t7\t.\tG\tGA\t.\tPASS\tDP=100", // insertion G/GA
      "18\t2\t.\tACG\tA\t.\tPASS\tDP=100" }; // deletion ACG/A

  // gnomAD exome variant dataset
  private static final String VCF_PATH = "/Volumes/gjb/smacgowan/NOBACK/resources/gnomad/gnomad.exomes.r2.0.1.sites.vcf.gz";

  // "https://storage.cloud.google.com/gnomad-public/release/2.0.1/vcf/exomes/gnomad.exomes.r2.0.1.sites.vcf.gz";

  /**
   * A test to exercise some basic functionality of the htsjdk VCF reader,
   * reading from a non-index VCF file
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testReadVcf_plain() throws IOException
  {
    File f = writeVcfFile();
    VCFReader reader = new VCFReader(f.getAbsolutePath());
    CloseableIterator<VariantContext> variants = reader.iterator();

    /*
     * SNP C/G variant
     */
    VariantContext vc = variants.next();
    assertTrue(vc.isSNP());
    Allele ref = vc.getReference();
    assertEquals(ref.getBaseString(), "C");
    List<Allele> alleles = vc.getAlleles();
    assertEquals(alleles.size(), 2);
    assertTrue(alleles.get(0).isReference());
    assertEquals(alleles.get(0).getBaseString(), "C");
    assertFalse(alleles.get(1).isReference());
    assertEquals(alleles.get(1).getBaseString(), "G");

    /*
     * Insertion G -> GA
     */
    vc = variants.next();
    assertFalse(vc.isSNP());
    assertTrue(vc.isSimpleInsertion());
    ref = vc.getReference();
    assertEquals(ref.getBaseString(), "G");
    alleles = vc.getAlleles();
    assertEquals(alleles.size(), 2);
    assertTrue(alleles.get(0).isReference());
    assertEquals(alleles.get(0).getBaseString(), "G");
    assertFalse(alleles.get(1).isReference());
    assertEquals(alleles.get(1).getBaseString(), "GA");

    /*
     * Deletion ACG -> A
     */
    vc = variants.next();
    assertFalse(vc.isSNP());
    assertTrue(vc.isSimpleDeletion());
    ref = vc.getReference();
    assertEquals(ref.getBaseString(), "ACG");
    alleles = vc.getAlleles();
    assertEquals(alleles.size(), 2);
    assertTrue(alleles.get(0).isReference());
    assertEquals(alleles.get(0).getBaseString(), "ACG");
    assertFalse(alleles.get(1).isReference());
    assertEquals(alleles.get(1).getBaseString(), "A");

    assertFalse(variants.hasNext());

    variants.close();
    reader.close();
  }

  /**
   * Creates a temporary file to be read by the htsjdk VCF reader
   * 
   * @return
   * @throws IOException
   */
  protected File writeVcfFile() throws IOException
  {
    File f = File.createTempFile("Test", "vcf");
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
   * A 'test' that demonstrates querying an indexed VCF file for features in a
   * specified interval
   * 
   * @throws IOException
   */
  @Test
  public void testQuery_indexed() throws IOException
  {
    /*
     * if not specified, assumes index file is filename.tbi
     */
    VCFReader reader = new VCFReader(VCF_PATH);

    /*
     * gene NMT1 (human) is on chromosome 17
     * GCHR38 (Ensembl): 45051610-45109016
     * GCHR37 (gnoMAD): 43128978-43186384
     * CDS begins at offset 9720, first CDS variant at offset 9724
     */
    CloseableIterator<VariantContext> features = reader.query("17",
            43128978 + 9724, 43128978 + 9734); // first 11 CDS positions

    assertEquals(printNext(features), 43138702);
    assertEquals(printNext(features), 43138704);
    assertEquals(printNext(features), 43138707);
    assertEquals(printNext(features), 43138708);
    assertEquals(printNext(features), 43138710);
    assertEquals(printNext(features), 43138711);
    assertFalse(features.hasNext());

    features.close();
    reader.close();
  }

  /**
   * Prints the toString value of the next variant, and returns its start
   * location
   * 
   * @param features
   * @return
   */
  protected int printNext(CloseableIterator<VariantContext> features)
  {
    VariantContext next = features.next();
    System.out.println(next.toString());
    return next.getStart();
  }

  // "https://storage.cloud.google.com/gnomad-public/release/2.0.1/vcf/exomes/gnomad.exomes.r2.0.1.sites.vcf.gz";

  /**
   * Test the query method that wraps a non-indexed VCF file
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testQuery_plain() throws IOException
  {
    File f = writeVcfFile();
    VCFReader reader = new VCFReader(f.getAbsolutePath());

    /*
     * query for overlap of 5-8 - should find variant at 7
     */
    CloseableIterator<VariantContext> variants = reader.query("20", 5, 8);

    /*
     * INDEL G/GA variant
     */
    VariantContext vc = variants.next();
    assertTrue(vc.isIndel());
    assertEquals(vc.getStart(), 7);
    assertEquals(vc.getEnd(), 7);
    Allele ref = vc.getReference();
    assertEquals(ref.getBaseString(), "G");
    List<Allele> alleles = vc.getAlleles();
    assertEquals(alleles.size(), 2);
    assertTrue(alleles.get(0).isReference());
    assertEquals(alleles.get(0).getBaseString(), "G");
    assertFalse(alleles.get(1).isReference());
    assertEquals(alleles.get(1).getBaseString(), "GA");

    assertFalse(variants.hasNext());

    variants.close();
    reader.close();
  }
}
