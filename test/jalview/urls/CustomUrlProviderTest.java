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
package jalview.urls;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.urls.api.UrlProviderI;
import jalview.util.UrlConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.testng.annotations.Test;

public class CustomUrlProviderTest
{

  private static final String cachedList = "TEST|http://someurl.blah/$DB_ACCESSION$|"
          + "ANOTHER|http://test/t$SEQUENCE_ID$|"
          + "TEST2|http://address/$SEQUENCE_ID$|SRS|"
          + "http://theSRSlink/$SEQUENCE_ID$";

  private static final String unselectedList = "NON1|http://x/y/$DB_ACCESSION$|"
          + "NON2|http://a/b/c/$DB_ACCESSION";

  private static final HashMap<String, String> urlMap = new HashMap<String, String>()
  {
    {
      put("TEST", "http://someurl.blah/$DB_ACCESSION$");
      put("ANOTHER", "http://test/t$SEQUENCE_ID$");
      put("TEST2", "http://address/$SEQUENCE_ID$");
      put("SRS", "http://theSRSlink/$SEQUENCE_ID$");
    }
  };

  private static final HashMap<String, String> unselUrlMap = new HashMap<String, String>()
  {
    {
      put("NON1", "http://x/y/$DB_ACCESSION$");
      put("NON2", "http://a/b/c/$DB_ACCESSION");
    }
  };

  private static final String[] dlinks = {
      "TEST|http://someurl.blah/$DB_ACCESSION$",
      "ANOTHER|http://test/t$SEQUENCE_ID$",
      "TEST2|http://address/$SEQUENCE_ID$", UrlConstants.DEFAULT_STRING };

  private static final String[] unselDlinks = {
      "NON1|http://x/y/$DB_ACCESSION$", "NON2|http://a/b/c/$DB_ACCESSION" };

  private static final Vector<String> displayLinks = new Vector<String>(
          Arrays.asList(dlinks));

  private static final Vector<String> unselDisplayLinks = new Vector<String>(
          Arrays.asList(unselDlinks));

  private static final String[] dlinks2 = {
      "a|http://x.y.z/$SEQUENCE_ID$" };

  private static final Vector<String> displayLinks2 = new Vector<String>(
          Arrays.asList(dlinks2));

  private static final String[] list1 = { "a" };

  private static final String[] list2 = { "http://x.y.z/$SEQUENCE_ID$" };

  private static final Vector<String> names = new Vector<String>(
          Arrays.asList(list1));

  private static final Vector<String> urls = new Vector<String>(
          Arrays.asList(list2));

  /*
   * Test default url is set and returned correctly
   */
  @Test(groups = { "Functional" })
  public void testDefaultUrl()
  {
    UrlProviderI customProv = new CustomUrlProvider(cachedList,
            unselectedList);

    // default url can be set
    assertTrue(customProv.setPrimaryUrl("ANOTHER"));

    // supplied replacement id must be more than 4 chars
    String result = customProv.getPrimaryUrl("123");
    assertEquals(null, result);

    // default url can be retrieved given a sequence id
    result = customProv.getPrimaryUrl("seqid");
    assertEquals("http://test/tseqid", result);

    // if there is no default url it sets the default to null
    assertFalse(customProv.setPrimaryUrl("No default"));
    result = customProv.getPrimaryUrl("testid");
    assertEquals(null, result);

    // choosing the default picks the DEFAULT_STRING option
    customProv.choosePrimaryUrl();
    result = customProv.getPrimaryUrl("seqid");
    assertEquals(UrlConstants.DEFAULT_STRING.split("\\|")[1].split("\\$")[0]
            + "seqid", result);
  }

  /*
   * Test urls are set and returned correctly
   */
  @Test(groups = { "Functional" })
  public void testUrlLinks()
  {
    // creation from cached url list works + old links upgraded
    UrlProviderI customProv = new CustomUrlProvider(cachedList,
            unselectedList);
    assertTrue(displayLinks.containsAll(customProv.getLinksForMenu()));

    // creation from map works + old links upgraded
    UrlProviderI customProv2 = new CustomUrlProvider(urlMap, unselUrlMap);
    assertTrue(displayLinks.containsAll(customProv2.getLinksForMenu()));

    // writing url links as a string works
    // because UrlProvider does not guarantee order of links, we can't just
    // compare the output of writeUrlsAsString to a string, hence the hoops here
    String result = customProv.writeUrlsAsString(true);
    UrlProviderI up = new CustomUrlProvider(result, "");
    assertTrue(displayLinks.containsAll(up.getLinksForMenu()));

    result = customProv.writeUrlsAsString(false);
    up = new CustomUrlProvider("", result);
    assertTrue(unselDisplayLinks.containsAll(up.getLinksForMenu()));

    result = customProv2.writeUrlsAsString(true);
    UrlProviderI up2 = new CustomUrlProvider(result, "");
    assertTrue(displayLinks.containsAll(up2.getLinksForMenu()));

    result = customProv2.writeUrlsAsString(false);
    up2 = new CustomUrlProvider("", result);
    assertTrue(displayLinks.containsAll(up2.getLinksForMenu()));
  }
}
