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

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.util.DBRefUtils;
import jalview.xml.binding.uniprot.DbReferenceType;
import jalview.xml.binding.uniprot.Entry;
import jalview.xml.binding.uniprot.FeatureType;
import jalview.xml.binding.uniprot.LocationType;
import jalview.xml.binding.uniprot.PositionType;

public class UniprotTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // adapted from http://www.uniprot.org/uniprot/A9CKP4.xml
  private static final String UNIPROT_XML = "<?xml version='1.0' encoding='UTF-8'?>"
          + "<uniprot xmlns=\"http://uniprot.org/uniprot\">"
          + "<entry dataset=\"TrEMBL\" created=\"2008-01-15\" modified=\"2015-03-04\" version=\"38\">"
          + "<accession>A9CKP4</accession>"
          + "<accession>A9CKP5</accession>" + "<name>A9CKP4_AGRT5</name>"
          + "<name>A9CKP4_AGRT6</name>"
          + "<protein><recommendedName><fullName>Mitogen-activated protein kinase 13</fullName></recommendedName></protein>"
          + "<dbReference type=\"PDB\" id=\"2FSQ\"><property type=\"method\" value=\"X-ray\"/><property type=\"resolution\" value=\"1.40\"/></dbReference>"
          + "<dbReference type=\"PDBsum\" id=\"2FSR\"/>"
          + "<dbReference type=\"EMBL\" id=\"AE007869\"><property type=\"protein sequence ID\" value=\"AAK85932.1\"/><property type=\"molecule type\" value=\"Genomic_DNA\"/></dbReference>"
          + "<feature type=\"signal peptide\" evidence=\"7\"><location><begin position=\"1\"/><end position=\"18\"/></location></feature>"
          + "<feature type=\"propeptide\" description=\"Activation peptide\" id=\"PRO_0000027399\" evidence=\"9 16 17 18\"><location><begin position=\"19\"/><end position=\"20\"/></location></feature>"
          + "<feature type=\"chain\" description=\"Granzyme B\" id=\"PRO_0000027400\"><location><begin position=\"21\"/><end position=\"247\"/></location></feature>"
          + "<feature type=\"sequence variant\"><original>M</original><variation>L</variation><location><position position=\"41\"/></location></feature>"
          + "<feature type=\"sequence variant\" description=\"Pathogenic\"><original>M</original><variation>L</variation><location><position position=\"41\"/></location></feature>"
          + "<feature type=\"sequence variant\" description=\"Pathogenic\"><original>M</original><location><position position=\"41\"/></location></feature>"
          + "<feature type=\"sequence variant\" description=\"Foo\"><variation>L</variation><variation>LMV</variation><original>M</original><location><position position=\"42\"/></location></feature>"
          + "<feature type=\"sequence variant\" description=\"Foo\"><variation>LL</variation><variation>LMV</variation><original>ML</original><location><begin position=\"42\"/><end position=\"43\"/></location></feature>"
          + "<feature type=\"sequence variant\" description=\"Foo Too\"><variation>LL</variation><variation>LMVK</variation><original>MLML</original><location><begin position=\"42\"/><end position=\"45\"/></location></feature>"
          + "<sequence length=\"10\" mass=\"27410\" checksum=\"8CB760AACF88FE6C\" modified=\"2008-01-15\" version=\"1\">MHAPL VSKDL</sequence></entry>"
          + "</uniprot>";

  /**
   * Test the method that unmarshals XML to a Uniprot model
   * 
   * @throws UnsupportedEncodingException
   */
  @Test(groups = { "Functional" })
  public void testGetUniprotEntries() throws UnsupportedEncodingException
  {
    Uniprot u = new Uniprot();
    InputStream is = new ByteArrayInputStream(UNIPROT_XML.getBytes());
    List<Entry> entries = u.getUniprotEntries(is);
    assertEquals(1, entries.size());
    Entry entry = entries.get(0);
    assertEquals(2, entry.getName().size());
    assertEquals("A9CKP4_AGRT5", entry.getName().get(0));
    assertEquals("A9CKP4_AGRT6", entry.getName().get(1));
    assertEquals(2, entry.getAccession().size());
    assertEquals("A9CKP4", entry.getAccession().get(0));
    assertEquals("A9CKP5", entry.getAccession().get(1));

    assertEquals("MHAPL VSKDL", entry.getSequence().getValue());

    assertEquals("Mitogen-activated protein kinase 13", entry.getProtein()
            .getRecommendedName().getFullName().getValue());

    /*
     * Check sequence features
     */
    List<FeatureType> features = entry.getFeature();
    assertEquals(9, features.size());
    FeatureType sf = features.get(0);
    assertEquals("signal peptide", sf.getType());
    assertNull(sf.getDescription());
    assertNull(sf.getStatus());
    assertNull(sf.getLocation().getPosition());
    assertEquals(1, sf.getLocation().getBegin().getPosition().intValue());
    assertEquals(18, sf.getLocation().getEnd().getPosition().intValue());
    sf = features.get(1);
    assertEquals("propeptide", sf.getType());
    assertEquals("Activation peptide", sf.getDescription());
    assertNull(sf.getLocation().getPosition());
    assertEquals(19, sf.getLocation().getBegin().getPosition().intValue());
    assertEquals(20, sf.getLocation().getEnd().getPosition().intValue());
    sf = features.get(2);
    assertEquals("chain", sf.getType());
    assertEquals("Granzyme B", sf.getDescription());
    assertNull(sf.getLocation().getPosition());
    assertEquals(21, sf.getLocation().getBegin().getPosition().intValue());
    assertEquals(247, sf.getLocation().getEnd().getPosition().intValue());

    sf = features.get(3);
    assertEquals("sequence variant", sf.getType());
    assertNull(sf.getDescription());
    assertEquals(41,
            sf.getLocation().getPosition().getPosition().intValue());
    assertNull(sf.getLocation().getBegin());
    assertNull(sf.getLocation().getEnd());

    sf = features.get(4);
    assertEquals("sequence variant", sf.getType());
    assertEquals("Pathogenic", sf.getDescription());
    assertEquals(41,
            sf.getLocation().getPosition().getPosition().intValue());
    assertNull(sf.getLocation().getBegin());
    assertNull(sf.getLocation().getEnd());

    sf = features.get(5);
    assertEquals("sequence variant", sf.getType());
    assertEquals("Pathogenic", sf.getDescription());
    assertEquals(41,
            sf.getLocation().getPosition().getPosition().intValue());
    assertNull(sf.getLocation().getBegin());
    assertNull(sf.getLocation().getEnd());

    sf = features.get(6);
    assertEquals("sequence variant", sf.getType());
    assertEquals("Foo", sf.getDescription());
    assertEquals(42,
            sf.getLocation().getPosition().getPosition().intValue());
    assertNull(sf.getLocation().getBegin());
    assertNull(sf.getLocation().getEnd());
    Assert.assertEquals(Uniprot.getDescription(sf), "<html>p.Met42Leu"
            + "<br/>&nbsp;&nbsp;" + "p.Met42LeuMetVal Foo</html>");

    sf = features.get(7);
    assertNull(sf.getLocation().getPosition());
    assertEquals(42, sf.getLocation().getBegin().getPosition().intValue());
    assertEquals(43, sf.getLocation().getEnd().getPosition().intValue());
    Assert.assertEquals(Uniprot.getDescription(sf), "<html>p.MetLeu42LeuLeu"
            + "<br/>&nbsp;&nbsp;" + "p.MetLeu42LeuMetVal Foo</html>");

    sf = features.get(8);
    assertNull(sf.getLocation().getPosition());
    assertEquals(42, sf.getLocation().getBegin().getPosition().intValue());
    assertEquals(45, sf.getLocation().getEnd().getPosition().intValue());
    Assert.assertEquals(Uniprot.getDescription(sf), "<html>p.MLML42LeuLeu"
            + "<br/>&nbsp;&nbsp;" + "p.MLML42LMVK Foo Too</html>");

    /*
     * Check cross-references
     */
    List<DbReferenceType> xrefs = entry.getDbReference();
    assertEquals(3, xrefs.size());

    DbReferenceType xref = xrefs.get(0);
    assertEquals("2FSQ", xref.getId());
    assertEquals("PDB", xref.getType());
    assertEquals("X-ray",
            Uniprot.getProperty(xref.getProperty(), "method"));
    assertEquals("1.40",
            Uniprot.getProperty(xref.getProperty(), "resolution"));

    xref = xrefs.get(1);
    assertEquals("2FSR", xref.getId());
    assertEquals("PDBsum", xref.getType());
    assertTrue(xref.getProperty().isEmpty());

    xref = xrefs.get(2);
    assertEquals("AE007869", xref.getId());
    assertEquals("EMBL", xref.getType());
    assertEquals("AAK85932.1",
            Uniprot.getProperty(xref.getProperty(), "protein sequence ID"));
    assertEquals("Genomic_DNA",
            Uniprot.getProperty(xref.getProperty(), "molecule type"));
  }

  @Test(groups = { "Functional" })
  public void testGetUniprotSequence() throws UnsupportedEncodingException
  {
    InputStream is = new ByteArrayInputStream(UNIPROT_XML.getBytes());
    Entry entry = new Uniprot().getUniprotEntries(is).get(0);
    SequenceI seq = new Uniprot().uniprotEntryToSequence(entry);
    assertNotNull(seq);
    assertEquals(6, seq.getDBRefs().size()); // 2*Uniprot, PDB, PDBsum, 2*EMBL
    assertEquals(seq.getSequenceAsString(),
            seq.createDatasetSequence().getSequenceAsString());
    assertEquals(2, seq.getPrimaryDBRefs().size());
    List<DBRefEntry> res = DBRefUtils.searchRefs(seq.getPrimaryDBRefs(),
            "A9CKP4");
    assertEquals(1, res.size());
    assertTrue(res.get(0).isCanonical());
    res = DBRefUtils.searchRefsForSource(seq.getDBRefs(),
            DBRefSource.UNIPROT);
    assertEquals(2, res.size());
    /*
     * NB this test fragile - relies on ordering being preserved
     */
    assertTrue(res.get(0).isCanonical());
    assertFalse(res.get(1).isCanonical());

    // check version is preserved for EMBLCDS
    res = DBRefUtils.searchRefs(seq.getDBRefs(), "AAK85932");
    assertEquals(1, res.size());
    // Ideally we would expect AAK85932.1 -> AAK85932
    // assertTrue("1".equals(res.get(0).getVersion()));
    // but it also passes through DBrefUtils.ensurePrimaries which adds
    // (promoted) to the version string
    // FIXME: Jim needs to specify what (promoted) means !! - or perhaps we just
    // ignore it !
    assertEquals("1 (promoted)", (res.get(0).getVersion()));
  }

  /**
   * Test the method that formats the sequence id
   * 
   * @throws UnsupportedEncodingException
   */
  @Test(groups = { "Functional" })
  public void testGetUniprotEntryId() throws UnsupportedEncodingException
  {
    InputStream is = new ByteArrayInputStream(UNIPROT_XML.getBytes());
    Entry entry = new Uniprot().getUniprotEntries(is).get(0);

    /*
     * name formatted with Uniprot Entry name
     */
    String expectedName = "A9CKP4_AGRT5|A9CKP4_AGRT6";
    assertEquals(expectedName, Uniprot.getUniprotEntryId(entry));
  }

  /**
   * Test the method that formats the sequence description
   * 
   * @throws UnsupportedEncodingException
   */
  @Test(groups = { "Functional" })
  public void testGetUniprotEntryDescription()
          throws UnsupportedEncodingException
  {
    InputStream is = new ByteArrayInputStream(UNIPROT_XML.getBytes());
    Entry entry = new Uniprot().getUniprotEntries(is).get(0);

    assertEquals("Mitogen-activated protein kinase 13",
            Uniprot.getUniprotEntryDescription(entry));
  }

  @Test(groups = { "Functional" })
  public void testGetDescription()
  {
    FeatureType ft = new FeatureType();
    assertEquals("", Uniprot.getDescription(ft));

    ft.setDescription("Hello");
    assertEquals("Hello", Uniprot.getDescription(ft));

    ft.setLocation(new LocationType());
    ft.getLocation().setPosition(new PositionType());
    ft.getLocation().getPosition().setPosition(BigInteger.valueOf(23));
    ft.setOriginal("K");
    ft.getVariation().add("y");
    assertEquals("p.Lys23Tyr Hello", Uniprot.getDescription(ft));

    // multiple variants generate an html description over more than one line
    ft.getVariation().add("W");
    assertEquals("<html>p.Lys23Tyr<br/>&nbsp;&nbsp;p.Lys23Trp Hello</html>",
            Uniprot.getDescription(ft));

    /*
     * indel cases
     * up to 3 bases (original or variant) are shown using 3 letter code
     */
    ft.getVariation().clear();
    ft.getVariation().add("KWE");
    ft.setOriginal("KLS");
    assertEquals("p.LysLeuSer23LysTrpGlu Hello",
            Uniprot.getDescription(ft));

    // adding a fourth original base switches to single letter code
    ft.setOriginal("KLST");
    assertEquals("p.KLST23LysTrpGlu Hello", Uniprot.getDescription(ft));

    // adding a fourth variant switches to single letter code
    ft.getVariation().clear();
    ft.getVariation().add("KWES");
    assertEquals("p.KLST23KWES Hello", Uniprot.getDescription(ft));

    ft.getVariation().clear();
    ft.getVariation().add("z"); // unknown variant - fails gracefully
    ft.setOriginal("K");
    assertEquals("p.Lys23z Hello", Uniprot.getDescription(ft));

    ft.getVariation().clear(); // variant missing - is ignored
    assertEquals("Hello", Uniprot.getDescription(ft));
  }
}
