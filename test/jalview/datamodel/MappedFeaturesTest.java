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
package jalview.datamodel;

import static org.testng.Assert.assertEquals;

import jalview.util.MapList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

public class MappedFeaturesTest
{
  @Test(groups = "Functional")
  public void testFindProteinVariants()
  {
    /*
     * scenario: 
     * dna/10-20 aCGTaGctGAa (codons CGT=R, GGA = G)
     * mapping: 3:1 from [11-13,15,18-19] to peptide/1-2 RG 
     */
    SequenceI from = new Sequence("dna/10-20", "acgTAGCTGAA");
    SequenceI to = new Sequence("peptide", "RG");
    MapList map = new MapList(new int[] { 11, 13, 15, 15, 18, 19 },
            new int[]
            { 1, 2 }, 3, 1);
    Mapping mapping = new Mapping(to, map);

    /*
     * variants
     * C>T at dna11, consequence CGT>TGT=C
     * T>C at dna13, consequence CGT>CGC synonymous
     */
    List<SequenceFeature> features = new ArrayList<>();
    SequenceFeature sf1 = new SequenceFeature("sequence_variant", "C,T", 11,
            11, null);
    sf1.setValue("alleles", "C,T");
    features.add(sf1);
    SequenceFeature sf2 = new SequenceFeature("sequence_variant", "T,C", 13,
            13, null);
    sf2.setValue("alleles", "T,C");
    features.add(sf2);

    /*
     * missense variant in first codon
     */
    MappedFeatures mf = new MappedFeatures(mapping, from, 1, 'R', features);
    String variant = mf.findProteinVariants(sf1);
    assertEquals(variant, "p.Arg1Cys");

    /*
     * more than one alternative allele
     * C>G consequence is GGT=G
     * peptide variants as a comma-separated list
     */
    sf1.setValue("alleles", "C,T,G");
    variant = mf.findProteinVariants(sf1);
    assertEquals(variant, "p.Arg1Cys,p.Arg1Gly");

    /*
     * synonymous variant in first codon
     * shown in HGVS notation on peptide
     */
    variant = mf.findProteinVariants(sf2);
    assertEquals(variant, "c.13T>C(p.=)");

    /*
     * CSQ:HGVSp value is used if present 
     * _and_ it contains "p." following a colon
     */
    Map<String, String> csq = new HashMap<>();
    csq.put("HGVSp", "hello:world");
    sf2.setValue("CSQ", csq);
    variant = mf.findProteinVariants(sf2);
    assertEquals(variant, "c.13T>C(p.=)");
    csq.put("HGVSp", "p.HelloWorld");
    variant = mf.findProteinVariants(sf2);
    assertEquals(variant, "c.13T>C(p.=)");
    csq.put("HGVSp", "try this:hellop.world");
    variant = mf.findProteinVariants(sf2);
    assertEquals(variant, "hellop.world");

    /*
     * missense and indel variants in second codon
     * - codon is GGA spliced from dna positions 15,18,19
     * - SNP G>T in second position mutates GGA>G to GTA>V
     * - indel variants are not computed or reported
     */
    mf = new MappedFeatures(mapping, from, 2, 'G', features);
    features.clear();
    SequenceFeature sf3 = new SequenceFeature("sequence_variant",
            "G,-,CG,T", 18, 18, null);
    sf3.setValue("alleles", "G,-,CG,T");
    features.add(sf3);
    variant = mf.findProteinVariants(sf3);
    assertEquals(variant, "p.Gly2Val");

    /*
     * G>T in first position gives TGA Stop
     * shown with HGVS notation as 'Ter'
     */
    SequenceFeature sf4 = new SequenceFeature("sequence_variant", "G,T", 15,
            15, null);
    sf4.setValue("alleles", "G,-,CG,T");
    features.add(sf4);
    variant = mf.findProteinVariants(sf4);
    assertEquals(variant, "p.Gly2Ter");

    /*
     * feature must be one of those in MappedFeatures
     */
    SequenceFeature sf9 = new SequenceFeature("sequence_variant", "G,C", 15,
            15, null);
    variant = mf.findProteinVariants(sf9);
    assertEquals(variant, "");
  }
}
