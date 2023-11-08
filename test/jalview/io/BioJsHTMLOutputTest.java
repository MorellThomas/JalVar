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

import jalview.gui.JvOptionPane;
import jalview.json.binding.biojs.BioJSReleasePojo;
import jalview.json.binding.biojs.BioJSRepositoryPojo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.TreeMap;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BioJsHTMLOutputTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void getJalviewAlignmentAsJsonString()
  {
    String bjsTemplate = null;
    try
    {
      BioJsHTMLOutput.updateBioJS();
      try
      {
        // allow the update some three seconds to complete before getting latest
        // version of BioJS template
        Thread.sleep(1000 * 3);
      } catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      bjsTemplate = HTMLOutput.readFileAsString(
              BioJsHTMLOutput.getCurrentBJSTemplateFile());
      // System.out.println(bjsTemplate);
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    Assert.assertNotNull(bjsTemplate);
  }

  @Test(
    groups =
    { "Functional" },
    expectedExceptions = NullPointerException.class)
  public void expectedNullPointerException()
  {
    try
    {
      BioJsHTMLOutput.refreshVersionInfo(null);
    } catch (URISyntaxException e)
    {
      AssertJUnit.fail("Expception occured while testing!");
      e.printStackTrace();
    }
  }

  @Test(groups = { "Functional" })
  public void getBioJsMSAVersions()
  {
    TreeMap<String, File> versions = null;
    try
    {
      BioJsHTMLOutput.refreshVersionInfo(
              BioJsHTMLOutput.BJS_TEMPLATES_LOCAL_DIRECTORY);
      versions = BioJsHTMLOutput.getBioJsMSAVersions();
    } catch (URISyntaxException e)
    {
      AssertJUnit.fail("Expception occured while testing!");
      e.printStackTrace();
    }
    AssertJUnit.assertNotNull("No versions found", versions);
    AssertJUnit.assertTrue("One or more Templates required",
            versions.size() > 0);
    System.out
            .println("Number of discovered versions : " + versions.size());
    for (String v : versions.keySet())
    {
      System.out.println("version : " + v);
      System.out.println("File : " + versions.get(v));
    }

    System.out.println("\nCurrent latest version : "
            + BioJsHTMLOutput.getCurrentBJSTemplateFile());
    AssertJUnit.assertNotNull("Latest BioJsMSA version NOT found!",
            BioJsHTMLOutput.getCurrentBJSTemplateFile());

  }

  @Test(groups = { "Network" })
  public void testBioJsUpdate()
  {
    String url = BioJsHTMLOutput.BJS_TEMPLATE_GIT_REPO;
    AssertJUnit.assertTrue("URL not reacable : " + url,
            urlIsReachable(url));
    String response = BioJsHTMLOutput.getURLContentAsString(url);
    AssertJUnit.assertNotNull("Null response read from url!", response);
    BioJSRepositoryPojo repository = new BioJSRepositoryPojo(response);
    System.out.println(">>> description : " + repository.getDescription());
    System.out.println(
            ">>> latest version : " + repository.getLatestReleaseVersion());
    System.out
            .println(">>> repo count : " + repository.getReleases().size());
    for (BioJSReleasePojo release : repository.getReleases())
    {
      System.out.println("repo type : " + release.getType());
      System.out.println("url : " + release.getUrl());
      System.out.println("release version : " + release.getVersion());
    }
  }

  private static boolean urlIsReachable(String urlString)
  {
    try
    {
      final URL url = new URL(urlString);
      final URLConnection conn = url.openConnection();
      conn.connect();
      return true;
    } catch (MalformedURLException e)
    {
      throw new RuntimeException(e);
    } catch (IOException e)
    {
      return false;
    }
  }
}
