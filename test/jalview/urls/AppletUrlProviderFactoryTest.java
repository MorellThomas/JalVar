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

import jalview.urls.api.UrlProviderFactoryI;
import jalview.urls.api.UrlProviderI;
import jalview.urls.applet.AppletUrlProviderFactory;
import jalview.util.UrlConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AppletUrlProviderFactoryTest
{

  @Test(groups = { "Functional" })
  public void testCreateUrlProvider()
  {
    final String defaultUrl = UrlConstants.DEFAULT_STRING.substring(
            UrlConstants.DEFAULT_STRING.indexOf(UrlConstants.SEP) + 1,
            UrlConstants.DEFAULT_STRING.length());
    Map<String, String> urlList = new HashMap<String, String>()
    {
      {
        put("Test1", "http://identifiers.org/uniprot/$DB_ACCESSION$");
        put("Test2", defaultUrl);
      }
    };

    UrlProviderFactoryI factory = new AppletUrlProviderFactory("Test2",
            urlList);
    UrlProviderI prov = factory.createUrlProvider();

    // default url correctly set
    Assert.assertEquals(prov.getPrimaryUrlId(), "Test2");
    Assert.assertEquals(prov.getPrimaryUrl("FER_CAPAN"),
            defaultUrl.replace("$SEQUENCE_ID$", "FER_CAPAN"));

    List<UrlLinkDisplay> allLinks = prov.getLinksForTable();

    // 2 links in provider
    Assert.assertEquals(allLinks.size(), 2);

    // first link set correctly
    Assert.assertEquals(allLinks.get(0).getId(), "Test1");
    Assert.assertEquals(allLinks.get(0).getDescription(), "Test1");
    Assert.assertEquals(allLinks.get(0).getUrl(),
            "http://identifiers.org/uniprot/$DB_ACCESSION$");
    Assert.assertFalse(allLinks.get(0).getIsPrimary());
    Assert.assertTrue(allLinks.get(0).getIsSelected());

    // second link set correctly
    Assert.assertEquals(allLinks.get(1).getId(), "Test2");
    Assert.assertEquals(allLinks.get(1).getDescription(), "Test2");
    Assert.assertEquals(allLinks.get(1).getUrl(), defaultUrl);
    Assert.assertTrue(allLinks.get(1).getIsPrimary());
    Assert.assertTrue(allLinks.get(1).getIsSelected());
  }
}
