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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.gui.JvOptionPane;
import jalview.util.MapList;

public class SequenceFeatureTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testCopyConstructors()
  {
    SequenceFeature sf1 = new SequenceFeature("type", "desc", 22, 33, 12.5f,
            "group");
    sf1.setValue("STRAND", "+");
    sf1.setValue("Note", "Testing");
    Integer count = Integer.valueOf(7);
    sf1.setValue("Count", count);

    SequenceFeature sf2 = new SequenceFeature(sf1);
    assertEquals("type", sf2.getType());
    assertEquals("desc", sf2.getDescription());
    assertEquals(22, sf2.getBegin());
    assertEquals(33, sf2.getEnd());
    assertEquals(12.5f, sf2.getScore());
    assertEquals("+", sf2.getValue("STRAND"));
    assertEquals("Testing", sf2.getValue("Note"));
    // shallow clone of otherDetails map - contains the same object values!
    assertSame(count, sf2.getValue("Count"));

    /*
     * copy constructor modifying begin/end/group/score
     */
    SequenceFeature sf3 = new SequenceFeature(sf1, 11, 14, "group2", 17.4f);
    assertEquals("type", sf3.getType());
    assertEquals("desc", sf3.getDescription());
    assertEquals(11, sf3.getBegin());
    assertEquals(14, sf3.getEnd());
    assertEquals(17.4f, sf3.getScore());
    assertEquals("+", sf3.getValue("STRAND"));
    assertEquals("Testing", sf3.getValue("Note"));
    // shallow clone of otherDetails map - contains the same object values!
    assertSame(count, sf3.getValue("Count"));

    /*
     * copy constructor modifying type/begin/end/group/score
     */
    SequenceFeature sf4 = new SequenceFeature(sf1, "Disulfide bond", 12, 15,
            "group3", -9.1f);
    assertEquals("Disulfide bond", sf4.getType());
    assertTrue(sf4.isContactFeature());
    assertEquals("desc", sf4.getDescription());
    assertEquals(12, sf4.getBegin());
    assertEquals(15, sf4.getEnd());
    assertEquals(-9.1f, sf4.getScore());
    assertEquals("+", sf4.getValue("STRAND"));
    assertEquals("Testing", sf4.getValue("Note"));
    // shallow clone of otherDetails map - contains the same object values!
    assertSame(count, sf4.getValue("Count"));
  }

  /**
   * Tests for retrieving a 'miscellaneous details' property value, with or
   * without a supplied default
   */
  @Test(groups = { "Functional" })
  public void testGetValue()
  {
    SequenceFeature sf1 = new SequenceFeature("type", "desc", 22, 33, 12.5f,
            "group");
    sf1.setValue("STRAND", "+");
    assertEquals("+", sf1.getValue("STRAND"));
    assertNull(sf1.getValue("strand")); // case-sensitive
    assertEquals(".", sf1.getValue("unknown", "."));
    Integer i = Integer.valueOf(27);
    assertSame(i, sf1.getValue("Unknown", i));
  }

  /**
   * Tests the method that returns 1 / -1 / 0 for strand "+" / "-" / other
   */
  @Test(groups = { "Functional" })
  public void testGetStrand()
  {
    SequenceFeature sf = new SequenceFeature("type", "desc", 22, 33, 12.5f,
            "group");
    assertEquals(0, sf.getStrand());
    sf.setValue("STRAND", "+");
    assertEquals(1, sf.getStrand());
    sf.setValue("STRAND", "-");
    assertEquals(-1, sf.getStrand());
    sf.setValue("STRAND", ".");
    assertEquals(0, sf.getStrand());
  }

  /**
   * Tests for equality, and that equal objects have the same hashCode
   */
  @Test(groups = { "Functional" })
  public void testEqualsAndHashCode()
  {
    SequenceFeature sf1 = new SequenceFeature("type", "desc", 22, 33, 12.5f,
            "group");
    sf1.setValue("ID", "id");
    sf1.setValue("Name", "name");
    sf1.setValue("Parent", "parent");
    sf1.setStrand("+");
    sf1.setPhase("1");
    SequenceFeature sf2 = new SequenceFeature("type", "desc", 22, 33, 12.5f,
            "group");
    sf2.setValue("ID", "id");
    sf2.setValue("Name", "name");
    sf2.setValue("Parent", "parent");
    sf2.setStrand("+");
    sf2.setPhase("1");

    assertFalse(sf1.equals(null));
    assertTrue(sf1.equals(sf2));
    assertTrue(sf2.equals(sf1));
    assertEquals(sf1.hashCode(), sf2.hashCode());

    // changing type breaks equals:
    SequenceFeature sf3 = new SequenceFeature("type", "desc", 22, 33, 12.5f,
            "group");
    SequenceFeature sf4 = new SequenceFeature("Type", "desc", 22, 33, 12.5f,
            "group");
    assertFalse(sf3.equals(sf4));

    // changing description breaks equals:
    String restores = sf2.getDescription();
    sf2.setDescription("Desc");
    assertFalse(sf1.equals(sf2));
    sf2.setDescription(restores);

    // changing score breaks equals:
    float restoref = sf2.getScore();
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), sf2.getEnd(),
            sf2.getFeatureGroup(), 10f);
    assertFalse(sf1.equals(sf2));
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), sf2.getEnd(),
            sf2.getFeatureGroup(), restoref);

    // NaN doesn't match a number
    restoref = sf2.getScore();
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), sf2.getEnd(),
            sf2.getFeatureGroup(), Float.NaN);
    assertFalse(sf1.equals(sf2));

    // NaN matches NaN
    sf1 = new SequenceFeature(sf1, sf1.getBegin(), sf1.getEnd(),
            sf1.getFeatureGroup(), Float.NaN);
    assertTrue(sf1.equals(sf2));
    sf1 = new SequenceFeature(sf1, sf1.getBegin(), sf1.getEnd(),
            sf1.getFeatureGroup(), restoref);
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), sf2.getEnd(),
            sf2.getFeatureGroup(), restoref);

    // changing start position breaks equals:
    int restorei = sf2.getBegin();
    sf2 = new SequenceFeature(sf2, 21, sf2.getEnd(), sf2.getFeatureGroup(),
            sf2.getScore());
    assertFalse(sf1.equals(sf2));
    sf2 = new SequenceFeature(sf2, restorei, sf2.getEnd(),
            sf2.getFeatureGroup(), sf2.getScore());

    // changing end position breaks equals:
    restorei = sf2.getEnd();
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), 32,
            sf2.getFeatureGroup(), sf2.getScore());
    assertFalse(sf1.equals(sf2));
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), restorei,
            sf2.getFeatureGroup(), sf2.getScore());

    // changing feature group breaks equals:
    restores = sf2.getFeatureGroup();
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), sf2.getEnd(), "Group",
            sf2.getScore());
    assertFalse(sf1.equals(sf2));
    sf2 = new SequenceFeature(sf2, sf2.getBegin(), sf2.getEnd(), restores,
            sf2.getScore());

    // changing ID breaks equals:
    restores = (String) sf2.getValue("ID");
    sf2.setValue("ID", "id2");
    assertFalse(sf1.equals(sf2));
    sf2.setValue("ID", restores);

    // changing Name breaks equals:
    restores = (String) sf2.getValue("Name");
    sf2.setValue("Name", "Name");
    assertFalse(sf1.equals(sf2));
    sf2.setValue("Name", restores);

    // changing Parent breaks equals:
    restores = (String) sf1.getValue("Parent");
    sf1.setValue("Parent", "Parent");
    assertFalse(sf1.equals(sf2));
    sf1.setValue("Parent", restores);

    // changing strand breaks equals:
    restorei = sf2.getStrand();
    sf2.setStrand("-");
    assertFalse(sf1.equals(sf2));
    sf2.setStrand(restorei == 1 ? "+" : "-");

    // changing phase breaks equals:
    restores = sf1.getPhase();
    sf1.setPhase("2");
    assertFalse(sf1.equals(sf2));
    sf1.setPhase(restores);

    // restore equality as sanity check:
    assertTrue(sf1.equals(sf2));
    assertTrue(sf2.equals(sf1));
    assertEquals(sf1.hashCode(), sf2.hashCode());

    // changing status doesn't change equals:
    sf1.setStatus("new");
    assertTrue(sf1.equals(sf2));
  }

  @Test(groups = { "Functional" })
  public void testIsContactFeature()
  {
    SequenceFeature sf = new SequenceFeature("type", "desc", 22, 33, 12.5f,
            "group");
    assertFalse(sf.isContactFeature());
    sf = new SequenceFeature("", "desc", 22, 33, 12.5f, "group");
    assertFalse(sf.isContactFeature());
    sf = new SequenceFeature(null, "desc", 22, 33, 12.5f, "group");
    assertFalse(sf.isContactFeature());
    sf = new SequenceFeature("Disulfide Bond", "desc", 22, 33, 12.5f,
            "group");
    assertTrue(sf.isContactFeature());
    sf = new SequenceFeature("disulfide bond", "desc", 22, 33, 12.5f,
            "group");
    assertTrue(sf.isContactFeature());
    sf = new SequenceFeature("Disulphide Bond", "desc", 22, 33, 12.5f,
            "group");
    assertTrue(sf.isContactFeature());
    sf = new SequenceFeature("disulphide bond", "desc", 22, 33, 12.5f,
            "group");
    assertTrue(sf.isContactFeature());
  }

  @Test(groups = { "Functional" })
  public void testGetDetailsReport()
  {
    SequenceI seq = new Sequence("TestSeq", "PLRFQMD");
    String seqName = seq.getName();

    // single locus, no group, no score
    SequenceFeature sf = new SequenceFeature("variant", "G,C", 22, 22,
            null);
    String expected = "<br><table><tr><td>Location</td><td>TestSeq</td><td>22</td></tr>"
            + "<tr><td>Type</td><td>variant</td><td></td></tr>"
            + "<tr><td>Description</td><td>G,C</td><td></td></tr></table>";
    assertEquals(expected, sf.getDetailsReport(seqName, null));

    // contact feature
    sf = new SequenceFeature("Disulphide Bond", "a description", 28, 31,
            null);
    expected = "<br><table><tr><td>Location</td><td>TestSeq</td><td>28:31</td></tr>"
            + "<tr><td>Type</td><td>Disulphide Bond</td><td></td></tr>"
            + "<tr><td>Description</td><td>a description</td><td></td></tr></table>";
    assertEquals(expected, sf.getDetailsReport(seqName, null));

    sf = new SequenceFeature("variant", "G,C", 22, 33, 12.5f, "group");
    sf.setValue("Parent", "ENSG001");
    sf.setValue("Child", "ENSP002");
    expected = "<br><table><tr><td>Location</td><td>TestSeq</td><td>22-33</td></tr>"
            + "<tr><td>Type</td><td>variant</td><td></td></tr>"
            + "<tr><td>Description</td><td>G,C</td><td></td></tr>"
            + "<tr><td>Score</td><td>12.5</td><td></td></tr>"
            + "<tr><td>Group</td><td>group</td><td></td></tr>"
            + "<tr><td>Child</td><td></td><td>ENSP002</td></tr>"
            + "<tr><td>Parent</td><td></td><td>ENSG001</td></tr></table>";
    assertEquals(expected, sf.getDetailsReport(seqName, null));

    /*
     * feature with embedded html link in description
     */
    String desc = "<html>Fer2 Status: True Positive <a href=\"http://pfam.xfam.org/family/PF00111\">Pfam 8_8</a></html>";
    sf = new SequenceFeature("Pfam", desc, 8, 83, "Uniprot");
    expected = "<br><table><tr><td>Location</td><td>TestSeq</td><td>8-83</td></tr>"
            + "<tr><td>Type</td><td>Pfam</td><td></td></tr>"
            + "<tr><td>Description</td><td>Fer2 Status: True Positive <a href=\"http://pfam.xfam.org/family/PF00111\">Pfam 8_8</a></td><td></td></tr>"
            + "<tr><td>Group</td><td>Uniprot</td><td></td></tr></table>";
    assertEquals(expected, sf.getDetailsReport(seqName, null));
  }

  /**
   * Feature details report for a virtual feature should include original and
   * mapped locations, and also derived peptide consequence if it can be
   * determined
   */
  @Test(groups = { "Functional" })
  public void testGetDetailsReport_virtualFeature()
  {
    SequenceI cds = new Sequence("Cds/101-121", "CCTttgAGAtttCAAatgGAT");
    SequenceI seq = new Sequence("TestSeq/8-14", "PLRFQMD");
    MapList map = new MapList(new int[] { 101, 118 }, new int[] { 8, 13 },
            3, 1);
    Mapping mapping = new Mapping(seq, map);
    List<SequenceFeature> features = new ArrayList<>();
    // vary ttg (Leu) to ttc (Phe)
    SequenceFeature sf = new SequenceFeature("variant", "G,C", 106, 106,
            null);
    sf.setValue("alleles", "G,C"); // needed to compute peptide consequence!
    features.add(sf);

    MappedFeatures mf = new MappedFeatures(mapping, cds, 9, 'L', features);

    String expected = "<br><table><tr><td>Location</td><td>Cds</td><td>106</td></tr>"
            + "<tr><td>Peptide Location</td><td>TestSeq</td><td>9</td></tr>"
            + "<tr><td>Type</td><td>variant</td><td></td></tr>"
            + "<tr><td>Description</td><td>G,C</td><td></td></tr>"
            + "<tr><td>Consequence</td><td><i>Translated by Jalview</i></td><td>p.Leu9Phe</td></tr>"
            + "<tr><td>alleles</td><td></td><td>G,C</td></tr>" + "</table>";

    assertEquals(expected, sf.getDetailsReport(seq.getName(), mf));

    /*
     * exon feature extending beyond mapped range; mapped location should be
     * restricted to peptide mapped range limit i.e. 10-13
     */
    SequenceFeature sf2 = new SequenceFeature("exon", "exon 1", 109, 230,
            null);
    features.add(sf2);
    expected = "<br><table><tr><td>Location</td><td>Cds</td><td>109-230</td></tr>"
            + "<tr><td>Peptide Location</td><td>TestSeq</td><td>10-13</td></tr>"
            + "<tr><td>Type</td><td>exon</td><td></td></tr>"
            + "<tr><td>Description</td><td>exon 1</td><td></td></tr>"
            + "</table>";
    assertEquals(expected, sf2.getDetailsReport(seq.getName(), mf));
  }
}
