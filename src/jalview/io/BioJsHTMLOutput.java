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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.TreeMap;

import jalview.bin.Cache;
import jalview.gui.AlignmentPanel;
import jalview.gui.OOMWarning;
import jalview.json.binding.biojs.BioJSReleasePojo;
import jalview.json.binding.biojs.BioJSRepositoryPojo;
import jalview.util.MessageManager;

public class BioJsHTMLOutput extends HTMLOutput
{
  private static File currentBJSTemplateFile;

  private static TreeMap<String, File> bioJsMSAVersions;

  public static final String DEFAULT_DIR = System.getProperty("user.home")
          + File.separatorChar + ".biojs_templates" + File.separatorChar;

  public static final String BJS_TEMPLATES_LOCAL_DIRECTORY = Cache
          .getDefault("biojs_template_directory", DEFAULT_DIR);

  public static final String BJS_TEMPLATE_GIT_REPO = Cache.getDefault(
          "biojs_template_git_repo",
          "https://raw.githubusercontent.com/jalview/exporter-templates/master/biojs/package.json");

  public BioJsHTMLOutput(AlignmentPanel ap)
  {
    super(ap, "BioJS MSA");
  }

  public static void refreshVersionInfo(String dirName)
          throws URISyntaxException
  {
    File directory = new File(BJS_TEMPLATES_LOCAL_DIRECTORY);
    Objects.requireNonNull(dirName, "dirName MUST not be null!");
    Objects.requireNonNull(directory, "directory MUST not be null!");
    TreeMap<String, File> versionFileMap = new TreeMap<String, File>();

    for (File file : directory.listFiles())
    {
      if (file.isFile())
      {
        String fileName = file.getName().substring(0,
                file.getName().lastIndexOf("."));
        String fileMeta[] = fileName.split("_");
        if (fileMeta.length > 2)
        {
          setCurrentBJSTemplateFile(file);
          versionFileMap.put(fileMeta[2], file);
        }
        else if (fileMeta.length > 1)
        {
          versionFileMap.put(fileMeta[1], file);
        }
      }
    }
    if (getCurrentBJSTemplateFile() == null && versionFileMap.size() > 0)
    {
      setCurrentBJSTemplateFile(versionFileMap.lastEntry().getValue());
    }
    setBioJsMSAVersions(versionFileMap);
  }

  public static void updateBioJS()
  {
    Thread updateThread = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          String gitRepoPkgJson = getURLContentAsString(
                  BJS_TEMPLATE_GIT_REPO);
          if (gitRepoPkgJson != null)
          {
            BioJSRepositoryPojo release = new BioJSRepositoryPojo(
                    gitRepoPkgJson);
            syncUpdates(BJS_TEMPLATES_LOCAL_DIRECTORY, release);
            refreshVersionInfo(BJS_TEMPLATES_LOCAL_DIRECTORY);
          }
        } catch (URISyntaxException e)
        {
          e.printStackTrace();
        }
      }
    };
    updateThread.start();

  }

  public static void syncUpdates(String localDir, BioJSRepositoryPojo repo)
  {
    for (BioJSReleasePojo bjsRelease : repo.getReleases())
    {
      String releaseUrl = bjsRelease.getUrl();
      String releaseVersion = bjsRelease.getVersion();
      String releaseFile = "BioJsMSA_" + releaseVersion + ".txt";
      if (releaseVersion.equals(repo.getLatestReleaseVersion()))
      {
        releaseFile = "Latest_BioJsMSA_" + releaseVersion + ".txt";
      }

      File biojsDirectory = new File(BJS_TEMPLATES_LOCAL_DIRECTORY);
      if (!biojsDirectory.exists())
      {
        if (!biojsDirectory.mkdirs())
        {
          System.out.println("Couldn't create local directory : "
                  + BJS_TEMPLATES_LOCAL_DIRECTORY);
          return;
        }
      }

      File file = new File(BJS_TEMPLATES_LOCAL_DIRECTORY + releaseFile);
      if (!file.exists())
      {

        PrintWriter out = null;
        try
        {
          out = new java.io.PrintWriter(new java.io.FileWriter(file));
          out.print(getURLContentAsString(releaseUrl));
        } catch (IOException e)
        {
          e.printStackTrace();
        } finally
        {
          if (out != null)
          {
            out.flush();
            out.close();
          }
        }
      }
    }

  }

  public static String getURLContentAsString(String url)
          throws OutOfMemoryError
  {
    StringBuilder responseStrBuilder = null;
    InputStream is = null;
    try
    {
      URL resourceUrl = new URL(url);
      is = new BufferedInputStream(resourceUrl.openStream());
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      responseStrBuilder = new StringBuilder();
      String lineContent;

      while ((lineContent = br.readLine()) != null)
      {
        responseStrBuilder.append(lineContent).append("\n");
      }
    } catch (OutOfMemoryError er)
    {
      er.printStackTrace();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    } finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    return responseStrBuilder == null ? null
            : responseStrBuilder.toString();
  }

  public static File getCurrentBJSTemplateFile()
  {
    return currentBJSTemplateFile;
  }

  public static void setCurrentBJSTemplateFile(File currentBJSTemplateFile)
  {
    BioJsHTMLOutput.currentBJSTemplateFile = currentBJSTemplateFile;
  }

  public static TreeMap<String, File> getBioJsMSAVersions()
  {
    return bioJsMSAVersions;
  }

  public static void setBioJsMSAVersions(
          TreeMap<String, File> bioJsMSAVersions)
  {
    BioJsHTMLOutput.bioJsMSAVersions = bioJsMSAVersions;
  }

  @Override
  public boolean isEmbedData()
  {
    return true;
  }

  @Override
  public boolean isLaunchInBrowserAfterExport()
  {
    return true;
  }

  @Override
  public void run()
  {
    try
    {
      String bioJSON = getBioJSONData();
      String bioJSTemplateString = HTMLOutput
              .readFileAsString(getCurrentBJSTemplateFile());
      String generatedBioJsWithJalviewAlignmentAsJson = bioJSTemplateString
              .replaceAll("#sequenceData#", bioJSON).toString();

      PrintWriter out = new java.io.PrintWriter(
              new java.io.FileWriter(generatedFile));
      out.print(generatedBioJsWithJalviewAlignmentAsJson);
      out.flush();
      out.close();
      setProgressMessage(MessageManager
              .formatMessage("status.export_complete", getDescription()));
      exportCompleted();

    } catch (OutOfMemoryError err)
    {
      System.out.println("########################\n" + "OUT OF MEMORY "
              + generatedFile + "\n" + "########################");
      new OOMWarning("Creating Image for " + generatedFile, err);
    } catch (Exception e)
    {
      setProgressMessage(MessageManager
              .formatMessage("info.error_creating_file", getDescription()));
      e.printStackTrace();
    }

  }

  @Override
  public void run(String s)
  {
    run();
  }

}
