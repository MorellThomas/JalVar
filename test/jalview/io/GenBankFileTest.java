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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.bin.Console;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.util.MapList;

public class GenBankFileTest
{
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Console.initLogger();
  }

  /**
   * A fairly tough test, using J03321 (circular DNA), which has 8 CDS features,
   * one of them reverse strand
   * 
   * @throws MalformedURLException
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testParse() throws MalformedURLException, IOException
  {
    File dataFile = new File("test/jalview/io/J03321.gb");
    FileParse fp = new FileParse(dataFile.getAbsolutePath(),
            DataSourceType.FILE);
    EMBLLikeFlatFile parser = new GenBankFile(fp, "GenBankTest");
    List<SequenceI> seqs = parser.getSeqs();

    assertEquals(seqs.size(), 1);
    SequenceI seq = seqs.get(0);
    assertEquals(seq.getName(), "GenBankTest|J03321");
    assertEquals(seq.getLength(), 7502);
    assertEquals(seq.getDescription(),
            "Chlamydia trachomatis plasmid pCHL1, complete sequence");

    /*
     * should be 9 CDS features (one is a 'join' of two exons)
     */
    Set<String> featureTypes = seq.getFeatures().getFeatureTypes();
    assertEquals(featureTypes.size(), 1);
    assertTrue(featureTypes.contains("CDS"));

    /*
     * inspect some features (sorted just for convenience of test assertions)
     */
    List<SequenceFeature> features = seq.getFeatures()
            .getAllFeatures("CDS");
    SequenceFeatures.sortFeatures(features, true);
    assertEquals(features.size(), 9);

    SequenceFeature sf = features.get(0);
    assertEquals(sf.getBegin(), 1);
    assertEquals(sf.getEnd(), 437);
    assertEquals(sf.getDescription(),
            "Exon 2 for protein EMBLCDS:AAA91567.1");
    assertEquals(sf.getFeatureGroup(), "GenBankTest");
    assertEquals(sf.getEnaLocation(), "join(7022..7502,1..437)");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), 1);
    assertEquals(sf.getValue("note"), "pGP7-D");
    // this is the second exon of circular CDS!
    assertEquals(sf.getValue("exon number"), 2);
    assertEquals(sf.getValue("product"), "hypothetical protein");
    assertEquals(sf.getValue("transl_table"), "11");

    sf = features.get(1);
    assertEquals(sf.getBegin(), 488);
    assertEquals(sf.getEnd(), 1480);
    assertEquals(sf.getDescription(),
            "Exon 1 for protein EMBLCDS:AAA91568.1");
    assertEquals(sf.getFeatureGroup(), "GenBankTest");
    assertEquals(sf.getEnaLocation(), "complement(488..1480)");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), -1); // reverse strand!
    assertEquals(sf.getValue("note"), "pGP8-D");
    assertEquals(sf.getValue("exon number"), 1);
    assertEquals(sf.getValue("product"), "hypothetical protein");

    sf = features.get(7);
    assertEquals(sf.getBegin(), 6045);
    assertEquals(sf.getEnd(), 6788);
    assertEquals(sf.getDescription(),
            "Exon 1 for protein EMBLCDS:AAA91574.1");
    assertEquals(sf.getFeatureGroup(), "GenBankTest");
    assertEquals(sf.getEnaLocation(), "6045..6788");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), 1);
    assertEquals(sf.getValue("note"), "pGP6-D (gtg start codon)");
    assertEquals(sf.getValue("exon number"), 1);
    assertEquals(sf.getValue("product"), "hypothetical protein");

    /*
     * CDS at 7022-7502 is the first exon of the circular CDS
     */
    sf = features.get(8);
    assertEquals(sf.getBegin(), 7022);
    assertEquals(sf.getEnd(), 7502);
    assertEquals(sf.getDescription(),
            "Exon 1 for protein EMBLCDS:AAA91567.1");
    assertEquals(sf.getFeatureGroup(), "GenBankTest");
    assertEquals(sf.getEnaLocation(), "join(7022..7502,1..437)");
    assertEquals(sf.getPhase(), "0");
    assertEquals(sf.getStrand(), 1);
    assertEquals(sf.getValue("note"), "pGP7-D");
    assertEquals(sf.getValue("exon number"), 1);
    assertEquals(sf.getValue("product"), "hypothetical protein");

    /*
     * GenBank doesn't declare accession or CDS xrefs;
     * dbrefs are added by Jalview for 
     * xref to self : 1
     * protein products: 8
     */
    List<DBRefEntry> dbrefs = seq.getDBRefs();

    assertEquals(dbrefs.size(), 9);
    // xref to 'self':
    DBRefEntry selfRef = new DBRefEntry("GENBANKTEST", "1", "J03321");
    int[] range = new int[] { 1, seq.getLength() };
    selfRef.setMap(new Mapping(null, range, range, 1, 1));
    assertTrue(dbrefs.contains(selfRef));

    /*
     * dna should have dbref to itself, and to EMBLCDSPROTEIN
     * for each /protein_id (synthesized as no UNIPROT xref)
     */
    // TODO check if we should synthesize EMBLCDSPROTEIN dbrefs
    DBRefEntry dbref = dbrefs.get(0);
    assertEquals(dbref.getSource(), "GENBANKTEST");
    assertEquals(dbref.getAccessionId(), "J03321");
    Mapping mapping = dbref.getMap();
    assertNull(mapping.getTo());
    MapList map = mapping.getMap();
    assertEquals(map.getFromLowest(), 1);
    assertEquals(map.getFromHighest(), 7502);
    assertEquals(map.getToLowest(), 1);
    assertEquals(map.getToHighest(), 7502);
    assertEquals(map.getFromRatio(), 1);
    assertEquals(map.getToRatio(), 1);

    // dbref to inferred EMBLCDSPROTEIN for first CDS
    dbref = dbrefs.get(1);
    assertEquals(dbref.getSource(), "EMBLCDSPROTEIN");
    assertEquals(dbref.getAccessionId(), "AAA91567.1");
    mapping = dbref.getMap();
    SequenceI mapTo = mapping.getTo();
    assertEquals(mapTo.getName(), "AAA91567.1");
    // the /product qualifier transfers to protein product description
    assertEquals(mapTo.getDescription(), "hypothetical protein");
    String seqString = mapTo.getSequenceAsString();
    assertEquals(seqString.length(), 305);
    assertTrue(seqString.startsWith("MGSMAF"));
    assertTrue(seqString.endsWith("QTPTIL"));
    map = mapping.getMap();
    assertEquals(map.getFromLowest(), 1);
    assertEquals(map.getFromHighest(), 7502);
    assertEquals(map.getToLowest(), 1);
    assertEquals(map.getToHighest(), 305);
    assertEquals(map.getFromRatio(), 3);
    assertEquals(map.getToRatio(), 1);

    // dbref to inferred EMBLCDSPROTEIN for last CDS
    dbref = dbrefs.get(8);
    assertEquals(dbref.getSource(), "EMBLCDSPROTEIN");
    assertEquals(dbref.getAccessionId(), "AAA91574.1");
    mapping = dbref.getMap();
    mapTo = mapping.getTo();
    assertEquals(mapTo.getName(), "AAA91574.1");
    // the /product qualifier transfers to protein product description
    assertEquals(mapTo.getDescription(), "hypothetical protein");
    seqString = mapTo.getSequenceAsString();
    assertEquals(seqString.length(), 247);
    assertTrue(seqString.startsWith("MNKLK"));
    assertTrue(seqString.endsWith("FKQKS"));
    map = mapping.getMap();
    assertEquals(map.getFromLowest(), 6045);
    assertEquals(map.getFromHighest(), 6785); // excludes stop at 6788
    assertEquals(map.getToLowest(), 1);
    assertEquals(map.getToHighest(), 247);
    assertEquals(map.getFromRatio(), 3);
    assertEquals(map.getToRatio(), 1);
  }
}
