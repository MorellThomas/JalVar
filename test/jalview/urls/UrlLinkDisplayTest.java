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

import jalview.util.UrlLink;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UrlLinkDisplayTest
{

  @Test(groups = { "Functional" })
  public void testDisplayColumnNames()
  {
    // 5 column names returned although 6 names internal to UrlLinkDisplay
    List<String> names = UrlLinkDisplay.getDisplayColumnNames();
    Assert.assertEquals(names.size(), 5);
  }

  @Test(groups = { "Functional" })
  public void getValue()
  {
    UrlLink link = new UrlLink("Test Name",
            "http://identifiers.org/$DB_ACCESSION$", "TestDB");
    UrlLinkDisplay u = new UrlLinkDisplay("Test", link, false, false);

    Assert.assertFalse((boolean) u.getValue(UrlLinkDisplay.PRIMARY));
    Assert.assertEquals(u.getValue(UrlLinkDisplay.ID), "Test");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.DATABASE), "TestDB");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.NAME), "Test Name");
    Assert.assertFalse((boolean) u.getValue(UrlLinkDisplay.SELECTED));
    Assert.assertEquals(u.getValue(UrlLinkDisplay.URL),
            "http://identifiers.org/$DB_ACCESSION$");
  }

  @Test(groups = { "Functional" })
  public void testIsEditable()
  {
    // only default and selected columns are editable ever
    // default only editable if link contains $SEQUENCE_ID$

    UrlLink link = new UrlLink("Test Url",
            "http://identifiers.org/$DB_ACCESSION$", "TestName");
    UrlLinkDisplay u = new UrlLinkDisplay("Test", link, false, false);

    Assert.assertFalse(u.isEditable(UrlLinkDisplay.PRIMARY));
    Assert.assertTrue(u.isEditable(UrlLinkDisplay.SELECTED));
    Assert.assertFalse(u.isEditable(UrlLinkDisplay.ID));
    Assert.assertFalse(u.isEditable(UrlLinkDisplay.URL));
    Assert.assertFalse(u.isEditable(UrlLinkDisplay.NAME));
    Assert.assertFalse(u.isEditable(UrlLinkDisplay.DATABASE));

    UrlLink vlink = new UrlLink("Test Sequence ID Url",
            "http://myurl/$SEQUENCE_ID$", "TestName");
    UrlLinkDisplay v = new UrlLinkDisplay("Test", vlink, false, false);

    Assert.assertTrue(v.isEditable(UrlLinkDisplay.PRIMARY));
    Assert.assertTrue(v.isEditable(UrlLinkDisplay.SELECTED));
    Assert.assertFalse(v.isEditable(UrlLinkDisplay.ID));
    Assert.assertFalse(v.isEditable(UrlLinkDisplay.URL));
    Assert.assertFalse(v.isEditable(UrlLinkDisplay.NAME));
    Assert.assertFalse(v.isEditable(UrlLinkDisplay.DATABASE));
  }

  @Test(groups = { "Functional" })
  public void testName()
  {
    UrlLink link = new UrlLink("Test Url",
            "http://identifiers.org/$DB_ACCESSION$", "TestName");
    UrlLinkDisplay u = new UrlLinkDisplay("Test", link, false, false);

    // Name initially as input in link
    Assert.assertEquals(u.getDBName(), "TestName");

    // Setting updates name
    u.setDBName("NewName");
    Assert.assertEquals(u.getDBName(), "NewName");
  }

  @Test(groups = { "Functional" })
  public void testDescription()
  {
    UrlLink link = new UrlLink("Test Name",
            "http://identifiers.org/$DB_ACCESSION$", "TestDB");
    UrlLinkDisplay u = new UrlLinkDisplay("Test", link, false, false);

    // Desc initially as input in link
    Assert.assertEquals(u.getDescription(), "Test Name");

    // Setting updates name
    u.setDescription("New Desc");
    Assert.assertEquals(u.getDescription(), "New Desc");
  }

  @Test(groups = { "Functional" })
  public void testUrl()
  {
    UrlLink link = new UrlLink("Test Name",
            "http://identifiers.org/$DB_ACCESSION$", "TestDB");
    UrlLinkDisplay u = new UrlLinkDisplay("Test", link, false, false);

    // Url initially as input in link
    Assert.assertEquals(u.getUrl(),
            "http://identifiers.org/$DB_ACCESSION$");

    // Setting updates url
    u.setUrl("http://something.new/$SEQUENCE_ID$");
    Assert.assertEquals(u.getUrl(), "http://something.new/$SEQUENCE_ID$");
  }

  @Test(groups = { "Functional" })
  public void testGetSetValue()
  {
    UrlLink link = new UrlLink("Test Name",
            "http://identifiers.org/$DB_ACCESSION$", "TestDB");
    UrlLinkDisplay u = new UrlLinkDisplay("Test", link, false, false);

    Assert.assertFalse((boolean) u.getValue(UrlLinkDisplay.PRIMARY));
    Assert.assertFalse((boolean) u.getValue(UrlLinkDisplay.SELECTED));
    Assert.assertEquals(u.getValue(UrlLinkDisplay.DATABASE), "TestDB");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.NAME), "Test Name");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.ID), "Test");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.URL),
            "http://identifiers.org/$DB_ACCESSION$");

    u.setValue(UrlLinkDisplay.PRIMARY, true);
    Assert.assertTrue((boolean) u.getValue(UrlLinkDisplay.PRIMARY));

    u.setValue(UrlLinkDisplay.SELECTED, true);
    Assert.assertTrue((boolean) u.getValue(UrlLinkDisplay.SELECTED));

    u.setValue(UrlLinkDisplay.NAME, "New Desc");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.NAME), "New Desc");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.DATABASE), "New Desc");

    u.setValue(UrlLinkDisplay.DATABASE, "NewName");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.DATABASE), "NewName");

    u.setValue(UrlLinkDisplay.ID, "New ID");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.ID), "New ID");

    u.setValue(UrlLinkDisplay.URL, "http://something.new/$SEQUENCE_ID$");
    Assert.assertEquals(u.getValue(UrlLinkDisplay.URL),
            "http://something.new/$SEQUENCE_ID$");
  }
}
