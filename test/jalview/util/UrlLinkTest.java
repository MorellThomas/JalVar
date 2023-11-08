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
package jalview.util;

import static jalview.util.UrlConstants.DB_ACCESSION;
import static jalview.util.UrlConstants.SEQUENCE_ID;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.Sequence;
import jalview.gui.JvOptionPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UrlLinkTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  final static String DB = "Test";

  final static String URL_PREFIX = "http://www.jalview.org/";

  final static String URL_SUFFIX = "/blah";

  final static String SEP = "|";

  final static String DELIM = "$";

  final static String REGEX_NESTED = "=/^(?:Label:)?(?:(?:gi\\|(\\d+))|([^:]+))/=";

  final static String REGEX_RUBBISH = "=/[0-9]++/=";

  /**
   * Test URL link creation when the input string has no regex
   */
  @Test(groups = { "Functional" })
  public void testUrlLinkCreationNoRegex()
  {
    // SEQUENCE_ID
    UrlLink ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + SEQUENCE_ID
            + DELIM + URL_SUFFIX);
    assertEquals(DB, ul.getTarget());
    assertEquals(DB, ul.getLabel());
    assertEquals(URL_PREFIX, ul.getUrlPrefix());
    assertEquals(URL_SUFFIX, ul.getUrlSuffix());
    assertTrue(ul.isDynamic());
    assertFalse(ul.usesDBAccession());
    assertNull(ul.getRegexReplace());
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    // DB_ACCESSION
    ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + DB_ACCESSION + DELIM
            + URL_SUFFIX);
    assertEquals(DB, ul.getTarget());
    assertEquals(DB, ul.getLabel());
    assertEquals(URL_PREFIX, ul.getUrlPrefix());
    assertEquals(URL_SUFFIX, ul.getUrlSuffix());
    assertTrue(ul.isDynamic());
    assertTrue(ul.usesDBAccession());
    assertNull(ul.getRegexReplace());
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    // Not dynamic
    ul = new UrlLink(DB + SEP + URL_PREFIX + URL_SUFFIX.substring(1));
    assertEquals(DB, ul.getTarget());
    assertEquals(DB, ul.getLabel());
    assertEquals(URL_PREFIX + URL_SUFFIX.substring(1), ul.getUrlPrefix());
    assertFalse(ul.isDynamic());
    assertFalse(ul.usesDBAccession());
    assertNull(ul.getRegexReplace());
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());
  }

  /**
   * Test URL link creation when the input string has regex
   */
  @Test(groups = { "Functional" })
  public void testUrlLinkCreationWithRegex()
  {
    // SEQUENCE_ID
    UrlLink ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + SEQUENCE_ID
            + REGEX_NESTED + DELIM + URL_SUFFIX);
    assertEquals(DB, ul.getTarget());
    assertEquals(DB, ul.getLabel());
    assertEquals(URL_PREFIX, ul.getUrlPrefix());
    assertEquals(URL_SUFFIX, ul.getUrlSuffix());
    assertTrue(ul.isDynamic());
    assertFalse(ul.usesDBAccession());
    assertEquals(REGEX_NESTED.substring(2, REGEX_NESTED.length() - 2),
            ul.getRegexReplace());
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    // DB_ACCESSION
    ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + DB_ACCESSION
            + REGEX_NESTED + DELIM + URL_SUFFIX);
    assertEquals(DB, ul.getTarget());
    assertEquals(DB, ul.getLabel());
    assertEquals(URL_PREFIX, ul.getUrlPrefix());
    assertEquals(URL_SUFFIX, ul.getUrlSuffix());
    assertTrue(ul.isDynamic());
    assertTrue(ul.usesDBAccession());
    assertEquals(REGEX_NESTED.substring(2, REGEX_NESTED.length() - 2),
            ul.getRegexReplace());
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    // invalid regex
    ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + DB_ACCESSION
            + REGEX_RUBBISH + DELIM + URL_SUFFIX);
    assertEquals(DB, ul.getTarget());
    assertEquals(DB, ul.getLabel());
    assertEquals(URL_PREFIX, ul.getUrlPrefix());
    assertEquals(URL_SUFFIX, ul.getUrlSuffix());
    assertTrue(ul.isDynamic());
    assertTrue(ul.usesDBAccession());
    assertEquals(REGEX_RUBBISH.substring(2, REGEX_RUBBISH.length() - 2),
            ul.getRegexReplace());
    assertFalse(ul.isValid());
    assertEquals("Invalid Regular Expression : '"
            + REGEX_RUBBISH.substring(2, REGEX_RUBBISH.length() - 2)
            + "'\n", ul.getInvalidMessage());
  }

  /**
   * Test construction of link by substituting sequence id or name
   */
  @Test(groups = { "Functional" })
  public void testMakeUrlNoRegex()
  {
    // Single non-regex
    UrlLink ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + SEQUENCE_ID
            + DELIM + URL_SUFFIX);
    String idstring = "FER_CAPAA";
    String[] urls = ul.makeUrls(idstring, true);

    assertEquals(2, urls.length);
    assertEquals(idstring, urls[0]);
    assertEquals(URL_PREFIX + idstring + URL_SUFFIX, urls[1]);

    urls = ul.makeUrls(idstring, false);

    assertEquals(2, urls.length);
    assertEquals(idstring, urls[0]);
    assertEquals(URL_PREFIX + idstring + URL_SUFFIX, urls[1]);
  }

  /**
   * Test construction of link by substituting sequence id or name using regular
   * expression
   */
  @Test(groups = { "Functional" })
  public void testMakeUrlWithRegex()
  {
    // Unused regex
    UrlLink ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + DB_ACCESSION
            + REGEX_NESTED + DELIM + URL_SUFFIX);
    String idstring = "FER_CAPAA";
    String[] urls = ul.makeUrls(idstring, true);

    assertEquals(2, urls.length);
    assertEquals(idstring, urls[0]);
    assertEquals(URL_PREFIX + idstring + URL_SUFFIX, urls[1]);
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    urls = ul.makeUrls(idstring, false);

    assertEquals(2, urls.length);
    assertEquals(idstring, urls[0]);
    assertEquals(URL_PREFIX + idstring + URL_SUFFIX, urls[1]);
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    // nested regex
    idstring = "Label:gi|9234|pdb|102L|A";
    urls = ul.makeUrls(idstring, true);

    assertEquals(2, urls.length);
    assertEquals("9234", urls[0]);
    assertEquals(URL_PREFIX + "9234" + URL_SUFFIX, urls[1]);
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    urls = ul.makeUrls(idstring, false);

    assertEquals(2, urls.length);
    assertEquals("9234", urls[0]);
    assertEquals(URL_PREFIX + "9234" + URL_SUFFIX, urls[1]);
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    // unmatched regex
    idstring = "this does not match";
    urls = ul.makeUrls(idstring, true);

    assertEquals(2, urls.length);
    assertEquals(idstring, urls[0]);
    assertEquals(URL_PREFIX + idstring + URL_SUFFIX, urls[1]);
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    urls = ul.makeUrls(idstring, false);

    assertEquals(2, urls.length);
    assertEquals(idstring, urls[0]);
    assertEquals(URL_PREFIX + idstring + URL_SUFFIX, urls[1]);
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());

    // empty idstring
    idstring = "";
    urls = ul.makeUrls(idstring, true);

    assertNull(urls);

    urls = ul.makeUrls(idstring, false);

    assertEquals(2, urls.length);
    assertEquals("", urls[0]);
    assertEquals(URL_PREFIX + URL_SUFFIX, urls[1]);
    assertTrue(ul.isValid());
    assertNull(ul.getInvalidMessage());
  }

  /**
   * Test creating links with null sequence
   */
  @Test(groups = { "Functional" })
  public void testCreateLinksFromNullSequence()
  {
    UrlLink ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + SEQUENCE_ID
            + DELIM + URL_SUFFIX);

    Map<String, List<String>> linkset = new LinkedHashMap<>();
    ul.createLinksFromSeq(null, linkset);

    String key = DB + SEP + URL_PREFIX;
    assertEquals(1, linkset.size());
    assertTrue(linkset.containsKey(key));
    assertEquals(DB, linkset.get(key).get(0));
    assertEquals(DB, linkset.get(key).get(1));
    assertEquals(null, linkset.get(key).get(2));
    assertEquals(URL_PREFIX, linkset.get(key).get(3));
  }

  /**
   * Test creating links with non-dynamic urlLink
   */
  @Test(groups = { "Functional" })
  public void testCreateLinksForNonDynamic()
  {
    UrlLink ul = new UrlLink(DB + SEP + URL_PREFIX + URL_SUFFIX);

    Map<String, List<String>> linkset = new LinkedHashMap<>();
    ul.createLinksFromSeq(null, linkset);

    String key = DB + SEP + URL_PREFIX + URL_SUFFIX;
    assertEquals(1, linkset.size());
    assertTrue(linkset.containsKey(key));
    assertEquals(DB, linkset.get(key).get(0));
    assertEquals(DB, linkset.get(key).get(1));
    assertEquals(null, linkset.get(key).get(2));
    assertEquals(URL_PREFIX + URL_SUFFIX, linkset.get(key).get(3));
  }

  /**
   * Test creating links
   */
  @Test(groups = { "Functional" })
  public void testCreateLinksFromSequence()
  {

    // create list of links and list of DBRefs
    List<String> links = new ArrayList<>();
    List<DBRefEntry> refs = new ArrayList<>();

    // links as might be added into Preferences | Connections dialog
    links.add(
            "EMBL-EBI Search | http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$"
                    + SEQUENCE_ID + "$");
    links.add("UNIPROT | http://www.uniprot.org/uniprot/$" + DB_ACCESSION
            + "$");
    links.add("INTERPRO | http://www.ebi.ac.uk/interpro/entry/$"
            + DB_ACCESSION + "$");

    // make seq0 dbrefs
    refs.add(new DBRefEntry(DBRefSource.UNIPROT, "1", "P83527"));
    refs.add(new DBRefEntry("INTERPRO", "1", "IPR001041"));
    refs.add(new DBRefEntry("INTERPRO", "1", "IPR006058"));
    refs.add(new DBRefEntry("INTERPRO", "1", "IPR012675"));

    Sequence seq0 = new Sequence("FER1", "AKPNGVL");

    // add all the dbrefs to the sequence
    seq0.addDBRef(refs.get(0));
    seq0.addDBRef(refs.get(1));
    seq0.addDBRef(refs.get(2));
    seq0.addDBRef(refs.get(3));
    seq0.createDatasetSequence();

    // Test where link takes a sequence id as replacement
    UrlLink ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + SEQUENCE_ID
            + DELIM + URL_SUFFIX);

    Map<String, List<String>> linkset = new LinkedHashMap<>();
    ul.createLinksFromSeq(seq0, linkset);

    String key = seq0.getName() + SEP + URL_PREFIX + seq0.getName()
            + URL_SUFFIX;
    assertEquals(1, linkset.size());
    assertTrue(linkset.containsKey(key));
    assertEquals(DB, linkset.get(key).get(0));
    assertEquals(DB, linkset.get(key).get(1));
    assertEquals(seq0.getName(), linkset.get(key).get(2));
    assertEquals(URL_PREFIX + seq0.getName() + URL_SUFFIX,
            linkset.get(key).get(3));

    // Test where link takes a db annotation id and only has one dbref
    ul = new UrlLink(links.get(1));
    linkset = new LinkedHashMap<>();
    ul.createLinksFromSeq(seq0, linkset);

    key = "P83527|http://www.uniprot.org/uniprot/P83527";
    assertEquals(linkset.size(), 1);
    assertTrue(linkset.containsKey(key));
    assertEquals(DBRefSource.UNIPROT, linkset.get(key).get(0));
    assertEquals(DBRefSource.UNIPROT + SEP + "P83527",
            linkset.get(key).get(1));
    assertEquals("P83527", linkset.get(key).get(2));
    assertEquals("http://www.uniprot.org/uniprot/P83527",
            linkset.get(key).get(3));

    // Test where link takes a db annotation id and has multiple dbrefs
    ul = new UrlLink(links.get(2));
    linkset = new LinkedHashMap<>();
    ul.createLinksFromSeq(seq0, linkset);
    assertEquals(3, linkset.size());

    // check each link made it in correctly
    key = "IPR001041|http://www.ebi.ac.uk/interpro/entry/IPR001041";
    assertTrue(linkset.containsKey(key));
    assertEquals("INTERPRO", linkset.get(key).get(0));
    assertEquals("INTERPRO" + SEP + "IPR001041", linkset.get(key).get(1));
    assertEquals("IPR001041", linkset.get(key).get(2));
    assertEquals("http://www.ebi.ac.uk/interpro/entry/IPR001041",
            linkset.get(key).get(3));

    key = "IPR006058|http://www.ebi.ac.uk/interpro/entry/IPR006058";
    assertTrue(linkset.containsKey(key));
    assertEquals("INTERPRO", linkset.get(key).get(0));
    assertEquals("INTERPRO" + SEP + "IPR006058", linkset.get(key).get(1));
    assertEquals("IPR006058", linkset.get(key).get(2));
    assertEquals("http://www.ebi.ac.uk/interpro/entry/IPR006058",
            linkset.get(key).get(3));

    key = "IPR012675|http://www.ebi.ac.uk/interpro/entry/IPR012675";
    assertTrue(linkset.containsKey(key));
    assertEquals("INTERPRO", linkset.get(key).get(0));
    assertEquals("INTERPRO" + SEP + "IPR012675", linkset.get(key).get(1));
    assertEquals("IPR012675", linkset.get(key).get(2));
    assertEquals("http://www.ebi.ac.uk/interpro/entry/IPR012675",
            linkset.get(key).get(3));

    // Test where there are no matching dbrefs for the link
    ul = new UrlLink(DB + SEP + URL_PREFIX + DELIM + DB_ACCESSION + DELIM
            + URL_SUFFIX);
    linkset = new LinkedHashMap<>();
    ul.createLinksFromSeq(seq0, linkset);
    assertTrue(linkset.isEmpty());
  }

  /**
   * Test links where label and target are both included
   */
  @Test(groups = { "Functional" })
  public void testLinksWithTargets()
  {
    UrlLink ul = new UrlLink(
            "Protein Data Bank | http://www.identifiers.org/pdb/$"
                    + DB_ACCESSION + "$" + " | pdb");

    assertEquals("Protein Data Bank", ul.getLabel());
    assertEquals("pdb", ul.getTarget());
    assertEquals("http://www.identifiers.org/pdb/$" + DB_ACCESSION + "$",
            ul.getUrlWithToken());

    assertEquals("Protein Data Bank|http://www.identifiers.org/pdb/$"
            + DB_ACCESSION + "$" + "|pdb", ul.toStringWithTarget());

    ul = new UrlLink("Protein Data Bank",
            "http://www.identifiers.org/pdb/$" + DB_ACCESSION + "$", "pdb");

    assertEquals("Protein Data Bank", ul.getLabel());
    assertEquals("pdb", ul.getTarget());
    assertEquals("http://www.identifiers.org/pdb/$" + DB_ACCESSION + "$",
            ul.getUrlWithToken());

    assertEquals("Protein Data Bank|http://www.identifiers.org/pdb/$"
            + DB_ACCESSION + "$" + "|pdb", ul.toStringWithTarget());

  }

  @Test(groups = { "Functional" })
  public void testLinkComparator()
  {
    Comparator<String> c = UrlLink.LINK_COMPARATOR;
    assertEquals(0, c.compare(null, null));
    assertEquals(0, c.compare(null, "x"));
    assertEquals(0, c.compare("y", null));

    /*
     * SEQUENCE_ID templates should come before DB_ACCESSION templates
     */
    String dbRefUrl = "Cath|http://www.cathdb.info/version/v4_2_0/superfamily/$DB_ACCESSION$";
    String seqIdUrl = "EBI|https://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$SEQUENCE_ID$";
    assertTrue(c.compare(seqIdUrl, dbRefUrl) < 0);
    assertTrue(c.compare(dbRefUrl, seqIdUrl) > 0);

    String interpro = "Interpro|https://www.ebi.ac.uk/interpro/entry/$DB_ACCESSION$";
    String prosite = "ProSite|https://prosite.expasy.org/PS00197";
    assertTrue(c.compare(interpro, prosite) < 0);
    assertTrue(c.compare(prosite, interpro) > 0);
  }
}
