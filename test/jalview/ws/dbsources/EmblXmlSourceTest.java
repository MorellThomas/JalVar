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
package jalview.ws.dbsources;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.util.MapList;
import jalview.xml.binding.embl.EntryType;
import jalview.xml.binding.embl.EntryType.Feature;
import jalview.xml.binding.embl.EntryType.Feature.Qualifier;
import jalview.xml.binding.embl.XrefType;

public class EmblXmlSourceTest
{

  // adapted from http://www.ebi.ac.uk/ena/data/view/X07547&display=xml
  // dna and translations truncated for convenience
  static final String TESTDATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
          + "<ROOT>"
          + "<entry accession=\"X07547\" version=\"1\" entryVersion=\"8\""
          + " dataClass=\"STD\" taxonomicDivision=\"PRO\""
          + " moleculeType=\"genomic DNA\" sequenceLength=\"7499\" topology=\"linear\""
          + " firstPublic=\"1988-11-10\" firstPublicRelease=\"18\""
          + " lastUpdated=\"1999-02-10\" lastUpdatedRelease=\"58\">"
          + "<secondaryAccession>X07574</secondaryAccession>"
          + "<description>C. trachomatis plasmid</description>"
          + "<keyword>plasmid</keyword><keyword>unidentified reading frame</keyword>"
          + "<xref db=\"EuropePMC\" id=\"PMC107176\" secondaryId=\"9573186\" />"
          + "<xref db=\"MD5\" id=\"ac73317\" />"
          /*
           * first CDS (range and translation changed to keep test data manageable)
           */
          + "<feature name=\"CDS\" location=\"complement(46..57)\">"
          // test the case of >1 cross-ref to the same database (JAL-2029)
          + "<xref db=\"UniProtKB/Swiss-Prot\" id=\"B0BCM4\" secondaryId=\"2.1\" />"
          + "<xref db=\"UniProtKB/Swiss-Prot\" id=\"P0CE20\" />"
          + "<qualifier name=\"note\"><value>ORF 8 (AA 1-330)</value></qualifier>"
          + "<qualifier name=\"protein_id\"><value>CAA30420.1</value></qualifier>"
          + "<qualifier name=\"translation\"><value>MLCF</value></qualifier>"
          + "</feature>"
          /*
           * second CDS (range and translation changed to keep test data manageable)
           */
          + "<feature name=\"CDS\" location=\"4..15\">"
          + "<xref db=\"UniProtKB/Swiss-Prot\" id=\"B0BCM3\" />"
          + "<qualifier name=\"protein_id\"><value>CAA30421.1</value></qualifier>"
          + "<qualifier name=\"translation\"><value>MSSS</value></qualifier>"
          + "</feature>"
          /*
           * third CDS is made up - has no xref - code should synthesize 
           * one to an assumed EMBLCDSPROTEIN accession
           */
          + "<feature name=\"CDS\" location=\"join(4..6,10..15)\">"
          + "<qualifier name=\"protein_id\"><value>CAA12345.6</value></qualifier>"
          + "<qualifier name=\"translation\"><value>MSS</value></qualifier>"
          + "</feature>"
          /*
           * sequence (modified for test purposes)
           * emulates EMBL XML 1.2 which splits sequence data every 60 characters
           * see EmblSequence.setSequence
           */
          + "<sequence>GGTATGTCCTCTAGTACAAAC\n"
          + "ACCCCCAATATTGTGATATAATTAAAAACATAGCAT"
          + "</sequence></entry></ROOT>";

  private EmblXmlSource testee;

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    testee = new EmblXmlSource()
    {

      @Override
      public String getDbSource()
      {
        return null;
      }

      @Override
      public String getDbName()
      {
        return null;
      }

      @Override
      public String getTestQuery()
      {
        return null;
      }

      @Override
      public AlignmentI getSequenceRecords(String queries) throws Exception
      {
        return null;
      }
    };
  }

  @Test(groups = "Functional")
  public void testGetCdsRanges()
  {
    /*
     * Make a (CDS) Feature with 5 locations
     */
    Feature cds = new Feature();
    cds.setLocation(
            "join(10..20,complement(30..40),50..60,70..80,complement(110..120))");

    int[] exons = testee.getCdsRanges("EMBL", cds);
    assertEquals("[10, 20, 40, 30, 50, 60, 70, 80, 120, 110]",
            Arrays.toString(exons));
  }

  @Test(groups = "Functional")
  public void testGetSequence()
  {
    // not the whole sequence but enough for this test...
    List<SequenceI> peptides = new ArrayList<>();
    List<EntryType> entries = getEmblEntries();
    assertEquals(1, entries.size());
    EntryType entry = entries.get(0);
    String sourceDb = "EMBL";
    SequenceI dna = testee.getSequence(sourceDb, entry, peptides);

    /*
     * newline has been removed from sequence
     */
    String seq = dna.getSequenceAsString();
    assertEquals(
            "GGTATGTCCTCTAGTACAAACACCCCCAATATTGTGATATAATTAAAAACATAGCAT",
            seq);

    /*
     * peptides should now have five entries:
     * EMBL product and two Uniprot accessions for the first CDS / translation
     * EMBL product and one Uniprot accession for the second CDS / "
     * EMBL product only for the third
     */
    assertEquals(6, peptides.size());
    assertEquals("CAA30420.1", peptides.get(0).getName());
    assertEquals("MLCF", peptides.get(0).getSequenceAsString());
    assertEquals("UNIPROT|B0BCM4", peptides.get(1).getName());
    assertEquals("MLCF", peptides.get(1).getSequenceAsString());
    assertEquals("UNIPROT|P0CE20", peptides.get(2).getName());
    assertEquals("MLCF", peptides.get(2).getSequenceAsString());
    assertEquals("CAA30421.1", peptides.get(3).getName());
    assertEquals("MSSS", peptides.get(3).getSequenceAsString());
    assertEquals("UNIPROT|B0BCM3", peptides.get(4).getName());
    assertEquals("MSSS", peptides.get(4).getSequenceAsString());
    assertEquals("CAA12345.6", peptides.get(5).getName());
    assertEquals("MSS", peptides.get(5).getSequenceAsString());

    /*
     * verify dna sequence has dbrefs
     * - to 'self' (synthesized dbref)
     * - to EuropePMC
     * - to MD5 (with null version as "0") 
     * - with CDS mappings to the peptide 'products'
     */
    MapList mapToSelf = new MapList(new int[] { 1, 57 },
            new int[]
            { 1, 57 }, 1, 1);
    MapList cds1Map = new MapList(new int[] { 57, 46 }, new int[] { 1, 4 },
            3, 1);
    MapList cds2Map = new MapList(new int[] { 4, 15 }, new int[] { 1, 4 },
            3, 1);
    MapList cds3Map = new MapList(new int[] { 4, 6, 10, 15 },
            new int[]
            { 1, 3 }, 3, 1);

    List<DBRefEntry> dbrefs = dna.getDBRefs();
    assertEquals(7, dbrefs.size());

    DBRefEntry dbRefEntry = dbrefs.get(0);
    assertEquals("EMBL", dbRefEntry.getSource());
    assertEquals("X07547", dbRefEntry.getAccessionId());
    assertEquals("1", dbRefEntry.getVersion());
    assertNotNull(dbRefEntry.getMap());
    assertNull(dbRefEntry.getMap().getTo());
    assertEquals(mapToSelf, dbRefEntry.getMap().getMap());

    dbRefEntry = dbrefs.get(1);
    // DBRefEntry constructor puts dbSource in upper case
    assertEquals("EUROPEPMC", dbRefEntry.getSource());
    assertEquals("PMC107176", dbRefEntry.getAccessionId());
    assertEquals("9573186", dbRefEntry.getVersion());
    assertNull(dbRefEntry.getMap());

    dbRefEntry = dbrefs.get(2);
    assertEquals("MD5", dbRefEntry.getSource());
    assertEquals("ac73317", dbRefEntry.getAccessionId());
    assertEquals("0", dbRefEntry.getVersion());
    assertNull(dbRefEntry.getMap());

    dbRefEntry = dbrefs.get(3);
    assertEquals("UNIPROT", dbRefEntry.getSource());
    assertEquals("B0BCM4", dbRefEntry.getAccessionId());
    assertSame(peptides.get(1), dbRefEntry.getMap().getTo());
    assertEquals(cds1Map, dbRefEntry.getMap().getMap());

    dbRefEntry = dbrefs.get(4);
    assertEquals("UNIPROT", dbRefEntry.getSource());
    assertEquals("P0CE20", dbRefEntry.getAccessionId());
    assertSame(peptides.get(2), dbRefEntry.getMap().getTo());
    assertEquals(cds1Map, dbRefEntry.getMap().getMap());

    dbRefEntry = dbrefs.get(5);
    assertEquals("UNIPROT", dbRefEntry.getSource());
    assertEquals("B0BCM3", dbRefEntry.getAccessionId());
    assertSame(peptides.get(4), dbRefEntry.getMap().getTo());
    assertEquals(cds2Map, dbRefEntry.getMap().getMap());

    dbRefEntry = dbrefs.get(6);
    assertEquals("EMBLCDSPROTEIN", dbRefEntry.getSource());
    assertEquals("CAA12345.6", dbRefEntry.getAccessionId());
    assertSame(peptides.get(5), dbRefEntry.getMap().getTo());
    assertEquals(cds3Map, dbRefEntry.getMap().getMap());

    /*
     * verify peptides have dbrefs
     * - to EMBL sequence (with inverse 1:3 cds mapping)
     * - to EMBLCDS (with 1:3 mapping)
     * - direct (no mapping) to other protein accessions
     */
    MapList proteinToCdsMap1 = new MapList(new int[] { 1, 4 },
            new int[]
            { 1, 12 }, 1, 3);
    MapList proteinToCdsMap2 = new MapList(new int[] { 1, 3 },
            new int[]
            { 1, 9 }, 1, 3);

    // dbrefs for first CDS EMBL product CAA30420.1
    dbrefs = peptides.get(0).getDBRefs();
    assertEquals(5, dbrefs.size());
    assertEquals(DBRefSource.EMBL, dbrefs.get(0).getSource());
    assertEquals("CAA30420.1", dbrefs.get(0).getAccessionId());
    // TODO: verify getPrimaryDBRefs() for peptide products
    assertEquals(cds1Map.getInverse(), dbrefs.get(0).getMap().getMap());
    assertEquals(DBRefSource.EMBLCDS, dbrefs.get(1).getSource());
    assertEquals("CAA30420.1", dbrefs.get(1).getAccessionId());
    assertEquals(proteinToCdsMap1, dbrefs.get(1).getMap().getMap());
    assertEquals(DBRefSource.EMBLCDSProduct, dbrefs.get(2).getSource());
    assertEquals("CAA30420.1", dbrefs.get(2).getAccessionId());
    assertNull(dbrefs.get(2).getMap());
    assertEquals(new DBRefEntry(DBRefSource.UNIPROT, "2.1", "B0BCM4"),
            dbrefs.get(3));
    assertNull(dbrefs.get(3).getMap());
    assertEquals(new DBRefEntry(DBRefSource.UNIPROT, "0", "P0CE20"),
            dbrefs.get(4));
    assertNull(dbrefs.get(4).getMap());

    // dbrefs for first CDS first Uniprot xref
    dbrefs = peptides.get(1).getDBRefs();
    assertEquals(2, dbrefs.size());
    assertEquals(new DBRefEntry(DBRefSource.UNIPROT, "2.1", "B0BCM4"),
            dbrefs.get(0));
    assertNull(dbrefs.get(0).getMap());
    assertEquals(DBRefSource.EMBL, dbrefs.get(1).getSource());
    assertEquals("X07547", dbrefs.get(1).getAccessionId());
    assertEquals(cds1Map.getInverse(), dbrefs.get(1).getMap().getMap());

    // dbrefs for first CDS second Uniprot xref
    dbrefs = peptides.get(2).getDBRefs();
    assertEquals(2, dbrefs.size());
    assertEquals(new DBRefEntry(DBRefSource.UNIPROT, "0", "P0CE20"),
            dbrefs.get(0));
    assertNull(dbrefs.get(0).getMap());
    assertEquals(DBRefSource.EMBL, dbrefs.get(1).getSource());
    assertEquals("X07547", dbrefs.get(1).getAccessionId());
    assertEquals(cds1Map.getInverse(), dbrefs.get(1).getMap().getMap());

    // dbrefs for second CDS EMBL product CAA30421.1
    dbrefs = peptides.get(3).getDBRefs();
    assertEquals(4, dbrefs.size());
    assertEquals(DBRefSource.EMBL, dbrefs.get(0).getSource());
    assertEquals("CAA30421.1", dbrefs.get(0).getAccessionId());
    assertEquals(cds2Map.getInverse(), dbrefs.get(0).getMap().getMap());
    assertEquals(DBRefSource.EMBLCDS, dbrefs.get(1).getSource());
    assertEquals("CAA30421.1", dbrefs.get(1).getAccessionId());
    assertEquals(proteinToCdsMap1, dbrefs.get(1).getMap().getMap());
    assertEquals(DBRefSource.EMBLCDSProduct, dbrefs.get(2).getSource());
    assertEquals("CAA30421.1", dbrefs.get(2).getAccessionId());
    assertNull(dbrefs.get(2).getMap());
    assertEquals(new DBRefEntry(DBRefSource.UNIPROT, "0", "B0BCM3"),
            dbrefs.get(3));
    assertNull(dbrefs.get(3).getMap());

    // dbrefs for second CDS second Uniprot xref
    dbrefs = peptides.get(4).getDBRefs();
    assertEquals(2, dbrefs.size());
    assertEquals(new DBRefEntry(DBRefSource.UNIPROT, "0", "B0BCM3"),
            dbrefs.get(0));
    assertNull(dbrefs.get(0).getMap());
    assertEquals(DBRefSource.EMBL, dbrefs.get(1).getSource());
    assertEquals("X07547", dbrefs.get(1).getAccessionId());
    assertEquals(cds2Map.getInverse(), dbrefs.get(1).getMap().getMap());

    // dbrefs for third CDS inferred EMBL product CAA12345.6
    dbrefs = peptides.get(5).getDBRefs();
    assertEquals(3, dbrefs.size());
    assertEquals(DBRefSource.EMBL, dbrefs.get(0).getSource());
    assertEquals("CAA12345.6", dbrefs.get(0).getAccessionId());
    assertEquals(cds3Map.getInverse(), dbrefs.get(0).getMap().getMap());
    assertEquals(DBRefSource.EMBLCDS, dbrefs.get(1).getSource());
    assertEquals("CAA12345.6", dbrefs.get(1).getAccessionId());
    assertEquals(proteinToCdsMap2, dbrefs.get(1).getMap().getMap());
    assertEquals(DBRefSource.EMBLCDSProduct, dbrefs.get(2).getSource());
    assertEquals("CAA12345.6", dbrefs.get(2).getAccessionId());
    assertNull(dbrefs.get(2).getMap());
  }

  @Test(groups = "Functional")
  public void testAdjustForProteinLength()
  {
    int[] exons = new int[] { 11, 15, 21, 25, 31, 38 }; // 18 bp

    // exact length match:
    assertSame(exons, EmblXmlSource.adjustForProteinLength(6, exons));

    // truncate last exon by 3bp (e.g. stop codon)
    int[] truncated = EmblXmlSource.adjustForProteinLength(5, exons);
    assertEquals("[11, 15, 21, 25, 31, 35]", Arrays.toString(truncated));

    // truncate last exon by 6bp
    truncated = EmblXmlSource.adjustForProteinLength(4, exons);
    assertEquals("[11, 15, 21, 25, 31, 32]", Arrays.toString(truncated));

    // remove last exon and truncate preceding by 1bp
    truncated = EmblXmlSource.adjustForProteinLength(3, exons);
    assertEquals("[11, 15, 21, 24]", Arrays.toString(truncated));

    // exact removal of exon case:
    exons = new int[] { 11, 15, 21, 27, 33, 38 }; // 18 bp
    truncated = EmblXmlSource.adjustForProteinLength(4, exons);
    assertEquals("[11, 15, 21, 27]", Arrays.toString(truncated));

    // what if exons are too short for protein?
    truncated = EmblXmlSource.adjustForProteinLength(7, exons);
    assertSame(exons, truncated);
  }

  @Test(groups = { "Functional" })
  public void testGetEmblEntries()
  {
    List<EntryType> entries = getEmblEntries();
    assertEquals(1, entries.size());
    EntryType entry = entries.get(0);

    assertEquals("X07547", entry.getAccession());
    assertEquals("C. trachomatis plasmid", entry.getDescription());
    assertEquals("STD", entry.getDataClass());
    assertEquals("PRO", entry.getTaxonomicDivision());
    assertEquals("1999-02-10", entry.getLastUpdated().toString());
    assertEquals(58, entry.getLastUpdatedRelease().intValue());
    assertEquals("1988-11-10", entry.getFirstPublic().toString());
    assertEquals(18, entry.getFirstPublicRelease().intValue());
    assertEquals("genomic DNA", entry.getMoleculeType());
    assertEquals(1, entry.getVersion().intValue());
    assertEquals(8, entry.getEntryVersion().intValue());
    assertEquals("linear", entry.getTopology());
    assertEquals(7499, entry.getSequenceLength().intValue());
    assertEquals(2, entry.getKeyword().size());
    assertEquals("plasmid", entry.getKeyword().get(0));
    assertEquals("unidentified reading frame", entry.getKeyword().get(1));

    /*
     * dbrefs
     */
    assertEquals(2, entry.getXref().size());
    XrefType dbref = entry.getXref().get(0);
    assertEquals("EuropePMC", dbref.getDb());
    assertEquals("PMC107176", dbref.getId());
    assertEquals("9573186", dbref.getSecondaryId());
    dbref = entry.getXref().get(1);
    assertEquals("MD5", dbref.getDb());
    assertEquals("ac73317", dbref.getId());
    assertNull(dbref.getSecondaryId());

    /*
     * three sequence features for CDS
     */
    assertEquals(3, entry.getFeature().size());
    /*
     * first CDS
     */
    Feature ef = entry.getFeature().get(0);
    assertEquals("CDS", ef.getName());
    assertEquals("complement(46..57)", ef.getLocation());
    assertEquals(2, ef.getXref().size());
    dbref = ef.getXref().get(0);
    assertEquals("UniProtKB/Swiss-Prot", dbref.getDb());
    assertEquals("B0BCM4", dbref.getId());
    assertEquals("2.1", dbref.getSecondaryId());
    dbref = ef.getXref().get(1);
    assertEquals("UniProtKB/Swiss-Prot", dbref.getDb());
    assertEquals("P0CE20", dbref.getId());
    assertNull(dbref.getSecondaryId());
    // CDS feature qualifiers
    assertEquals(3, ef.getQualifier().size());
    Qualifier q = ef.getQualifier().get(0);
    assertEquals("note", q.getName());
    assertEquals("ORF 8 (AA 1-330)", q.getValue());
    q = ef.getQualifier().get(1);
    assertEquals("protein_id", q.getName());
    assertEquals("CAA30420.1", q.getValue());
    q = ef.getQualifier().get(2);
    assertEquals("translation", q.getName());
    assertEquals("MLCF", q.getValue());

    /*
     * second CDS
     */
    ef = entry.getFeature().get(1);
    assertEquals("CDS", ef.getName());
    assertEquals("4..15", ef.getLocation());
    assertEquals(1, ef.getXref().size());
    dbref = ef.getXref().get(0);
    assertEquals("UniProtKB/Swiss-Prot", dbref.getDb());
    assertEquals("B0BCM3", dbref.getId());
    assertNull(dbref.getSecondaryId());
    assertEquals(2, ef.getQualifier().size());
    q = ef.getQualifier().get(0);
    assertEquals("protein_id", q.getName());
    assertEquals("CAA30421.1", q.getValue());
    q = ef.getQualifier().get(1);
    assertEquals("translation", q.getName());
    assertEquals("MSSS", q.getValue());

    /*
     * third CDS
     */
    ef = entry.getFeature().get(2);
    assertEquals("CDS", ef.getName());
    assertEquals("join(4..6,10..15)", ef.getLocation());
    assertNotNull(ef.getXref());
    assertTrue(ef.getXref().isEmpty());
    assertEquals(2, ef.getQualifier().size());
    q = ef.getQualifier().get(0);
    assertEquals("protein_id", q.getName());
    assertEquals("CAA12345.6", q.getValue());
    q = ef.getQualifier().get(1);
    assertEquals("translation", q.getName());
    assertEquals("MSS", q.getValue());

    /*
     * Sequence - raw data before removal of newlines
     */
    String seq = entry.getSequence();
    assertEquals("GGTATGTCCTCTAGTACAAAC\n"
            + "ACCCCCAATATTGTGATATAATTAAAAACATAGCAT", seq);

    /*
     * getSequence() converts empty DBRefEntry.version to "0"
     */
    assertNull(entry.getXref().get(1).getSecondaryId());
    assertNull(entry.getFeature().get(0).getXref().get(1).getSecondaryId());
  }

  List<EntryType> getEmblEntries()
  {
    return testee
            .getEmblEntries(new ByteArrayInputStream(TESTDATA.getBytes()));
  }
}
