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

import java.util.Locale;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.api.FeatureSettingsModelI;
import jalview.bin.Cache;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyLite;
import jalview.util.MapList;

import java.awt.Color;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnsemblGeneTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    SequenceOntologyFactory.setInstance(new SequenceOntologyLite());
  }

  @AfterClass(alwaysRun = true)
  public void tearDown()
  {
    SequenceOntologyFactory.setInstance(null);
  }

  /**
   * Test that the gene part of genomic sequence is uniquely identified by a
   * 'gene' features (or subtype) with the correct gene ID
   */
  @Test(groups = "Functional")
  public void testGetGenomicRangesFromFeatures()
  {
    EnsemblGene testee = new EnsemblGene();
    SequenceI genomic = new SequenceDummy("chr7");
    genomic.setStart(10000);
    genomic.setEnd(50000);
    String geneId = "ABC123";

    // gene at (start + 10500) length 101
    SequenceFeature sf = new SequenceFeature("gene", "", 10500, 10600, 0f,
            null);
    sf.setValue("id", geneId);
    sf.setStrand("+");
    genomic.addSequenceFeature(sf);

    MapList ranges = testee.getGenomicRangesFromFeatures(genomic, geneId,
            23);
    List<int[]> fromRanges = ranges.getFromRanges();
    assertEquals(1, fromRanges.size());
    assertEquals(10500, fromRanges.get(0)[0]);
    assertEquals(10600, fromRanges.get(0)[1]);
    // to range should start from given start numbering
    List<int[]> toRanges = ranges.getToRanges();
    assertEquals(1, toRanges.size());
    assertEquals(23, toRanges.get(0)[0]);
    assertEquals(123, toRanges.get(0)[1]);
  }

  /**
   * Test variant using a sub-type of gene from the Sequence Ontology
   */
  @Test(groups = "Functional")
  public void testGetGenomicRangesFromFeatures_ncRNA_gene_reverseStrand()
  {
    EnsemblGene testee = new EnsemblGene();
    SequenceI genomic = new SequenceDummy("chr7");
    genomic.setStart(10000);
    genomic.setEnd(50000);
    String geneId = "ABC123";

    // gene at (start + 10500) length 101
    SequenceFeature sf = new SequenceFeature("gene", "", 10500, 10600, 0f,
            null);
    sf.setValue("id", geneId);
    sf.setStrand("+");
    genomic.addSequenceFeature(sf);

    MapList ranges = testee.getGenomicRangesFromFeatures(genomic, geneId,
            23);
    List<int[]> fromRanges = ranges.getFromRanges();
    assertEquals(1, fromRanges.size());
    // from range on reverse strand:
    assertEquals(10500, fromRanges.get(0)[0]);
    assertEquals(10600, fromRanges.get(0)[1]);
    // to range should start from given start numbering
    List<int[]> toRanges = ranges.getToRanges();
    assertEquals(1, toRanges.size());
    assertEquals(23, toRanges.get(0)[0]);
    assertEquals(123, toRanges.get(0)[1]);
  }

  /**
   * Test the method that extracts transcript (or subtype) features with a
   * specified gene as parent
   */
  @Test(groups = "Functional")
  public void testGetTranscriptFeatures()
  {
    SequenceI genomic = new SequenceDummy("chr7");
    genomic.setStart(10000);
    genomic.setEnd(50000);
    String geneId = "ABC123";

    // transcript feature
    SequenceFeature sf1 = new SequenceFeature("transcript", "", 20000,
            20500, 0f, null);
    sf1.setValue("Parent", geneId);
    sf1.setValue("id", "transcript1");
    genomic.addSequenceFeature(sf1);

    // transcript sub-type feature
    SequenceFeature sf2 = new SequenceFeature("snRNA", "", 21000, 21500, 0f,
            null);
    sf2.setValue("Parent", geneId);
    sf2.setValue("id", "transcript2");
    genomic.addSequenceFeature(sf2);

    // NMD_transcript_variant treated like transcript in Ensembl
    SequenceFeature sf3 = new SequenceFeature("NMD_transcript_variant", "",
            22000, 22500, 0f, null);
    // id matching should not be case-sensitive
    sf3.setValue("Parent", geneId.toLowerCase(Locale.ROOT));
    sf3.setValue("id", "transcript3");
    genomic.addSequenceFeature(sf3);

    // transcript for a different gene - ignored
    SequenceFeature sf4 = new SequenceFeature("snRNA", "", 23000, 23500, 0f,
            null);
    sf4.setValue("Parent", "XYZ");
    sf4.setValue("id", "transcript4");
    genomic.addSequenceFeature(sf4);

    EnsemblGene testee = new EnsemblGene();

    /*
     * with no filter
     */
    List<SequenceFeature> features = testee.getTranscriptFeatures(geneId,
            genomic);
    assertEquals(3, features.size());
    assertTrue(features.contains(sf1));
    assertTrue(features.contains(sf2));
    assertTrue(features.contains(sf3));
  }

  /**
   * Test the method that retains features except for 'gene', or 'transcript'
   * with parent other than the given id
   */
  @Test(groups = "Functional")
  public void testRetainFeature()
  {
    String geneId = "ABC123";
    EnsemblGene testee = new EnsemblGene();
    SequenceFeature sf = new SequenceFeature("gene", "", 20000, 20500, 0f,
            null);
    sf.setValue("id", geneId);
    assertFalse(testee.retainFeature(sf, geneId));

    sf = new SequenceFeature("transcript", "", 20000, 20500, 0f, null);
    sf.setValue("Parent", geneId);
    assertTrue(testee.retainFeature(sf, geneId));

    sf = new SequenceFeature("mature_transcript", "", 20000, 20500, 0f,
            null);
    sf.setValue("Parent", geneId);
    assertTrue(testee.retainFeature(sf, geneId));

    sf = new SequenceFeature("NMD_transcript_variant", "", 20000, 20500, 0f,
            null);
    sf.setValue("Parent", geneId);
    assertTrue(testee.retainFeature(sf, geneId));

    sf.setValue("Parent", "ßXYZ");
    assertFalse(testee.retainFeature(sf, geneId));

    sf = new SequenceFeature("anything", "", 20000, 20500, 0f, null);
    assertTrue(testee.retainFeature(sf, geneId));
  }

  /**
   * Test the method that picks out 'gene' (or subtype) features with the
   * accession id as ID
   */
  @Test(groups = "Functional")
  public void testGetIdentifyingFeatures()
  {
    String accId = "ABC123";
    SequenceI seq = new Sequence(accId, "HIBEES");

    // gene with no ID not valid
    SequenceFeature sf1 = new SequenceFeature("gene", "", 1, 2, 0f, null);
    seq.addSequenceFeature(sf1);

    // gene with wrong ID not valid
    SequenceFeature sf2 = new SequenceFeature("gene", "a", 1, 2, 0f, null);
    sf2.setValue("id", "XYZ");
    seq.addSequenceFeature(sf2);

    // gene with right ID is valid
    SequenceFeature sf3 = new SequenceFeature("gene", "b", 1, 2, 0f, null);
    sf3.setValue("id", accId);
    seq.addSequenceFeature(sf3);

    // gene sub-type with right ID is valid
    SequenceFeature sf4 = new SequenceFeature("snRNA_gene", "", 1, 2, 0f,
            null);
    sf4.setValue("id", accId);
    seq.addSequenceFeature(sf4);

    // transcript not valid:
    SequenceFeature sf5 = new SequenceFeature("transcript", "", 1, 2, 0f,
            null);
    sf5.setValue("id", accId);
    seq.addSequenceFeature(sf5);

    // exon not valid:
    SequenceFeature sf6 = new SequenceFeature("exon", "", 1, 2, 0f, null);
    sf6.setValue("id", accId);
    seq.addSequenceFeature(sf6);

    List<SequenceFeature> sfs = new EnsemblGene()
            .getIdentifyingFeatures(seq, accId);
    assertFalse(sfs.contains(sf1));
    assertFalse(sfs.contains(sf2));
    assertTrue(sfs.contains(sf3));
    assertTrue(sfs.contains(sf4));
    assertFalse(sfs.contains(sf5));
    assertFalse(sfs.contains(sf6));
  }

  /**
   * Check behaviour of feature colour scheme for EnsemblGene sequences.
   * Currently coded to hide all except exon and sequence_variant (or sub-types)
   * only, with sequence_variant in red above exon coloured by label.
   */
  @Test(groups = "Functional")
  public void testGetFeatureColourScheme()
  {
    FeatureSettingsModelI fc = new EnsemblGene().getFeatureColourScheme();
    assertFalse(fc.isFeatureDisplayed("exon"));
    assertFalse(fc.isFeatureHidden("exon"));
    assertFalse(fc.isFeatureDisplayed("coding_exon")); // subtype of exon
    assertFalse(fc.isFeatureHidden("coding_exon")); // subtype of exon
    assertFalse(fc.isFeatureDisplayed("sequence_variant"));
    assertFalse(fc.isFeatureHidden("sequence_variant"));
    assertFalse(fc.isFeatureDisplayed("feature_variant")); // subtype
    assertFalse(fc.isFeatureHidden("feature_variant")); // subtype
    assertTrue(fc.isFeatureHidden("transcript"));
    assertTrue(fc.isFeatureHidden("CDS"));

    assertEquals(Color.RED,
            fc.getFeatureColour("sequence_variant").getColour());
    assertEquals(Color.RED,
            fc.getFeatureColour("feature_variant").getColour());
    assertTrue(fc.getFeatureColour("exon").isColourByLabel());
    assertTrue(fc.getFeatureColour("coding_exon").isColourByLabel());
    assertEquals(1, fc.compare("sequence_variant", "exon"));
    assertEquals(-1, fc.compare("exon", "sequence_variant"));
    assertEquals(1, fc.compare("feature_variant", "coding_exon"));
    assertEquals(-1, fc.compare("coding_exon", "feature_variant"));
    assertEquals(1f, fc.getTransparency());
  }

  @Test(groups = "Network")
  public void testGetGeneIds()
  {
    /*
     * ENSG00000158828 gene id PINK1 human
     * ENST00000321556 transcript for the same gene - should not be duplicated
     * P30419 Uniprot identifier for ENSG00000136448
     * ENST00000592782 transcript for Uniprot gene - should not be duplicated
     * BRAF - gene name resolvabe (at time of writing) for 6 model species
     */
    String ids = "ENSG00000158828 ENST00000321556 P30419 ENST00000592782 BRAF";
    EnsemblGene testee = new EnsemblGene();
    List<String> geneIds = testee.getGeneIds(ids);
    assertTrue(geneIds.contains("ENSG00000158828"));
    assertTrue(geneIds.contains("ENSG00000136448"));
    assertTrue(geneIds.contains("ENSG00000157764")); // BRAF human
    assertTrue(geneIds.contains("ENSMUSG00000002413")); // mouse
    assertTrue(geneIds.contains("ENSRNOG00000010957")); // rat
    assertTrue(geneIds.contains("ENSXETG00000004845")); // xenopus
    assertTrue(geneIds.contains("ENSDARG00000017661")); // zebrafish
    assertTrue(geneIds.contains("ENSGALG00000012865")); // chicken
    assertEquals(8, geneIds.size());

  }
}
