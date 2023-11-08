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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to check help file cross-references, and external URLs if internet
 * access is available
 * 
 * @author gmcarstairs
 *
 */
public class HelpLinksChecker implements BufferedLineReader.LineCleaner
{
  private static final String HELP_HS = "help.hs";

  private static final String HELP_TOC_XML = "helpTOC.xml";

  private static final String HELP_JHM = "help.jhm";

  private static boolean internetAvailable = true;

  private int targetCount = 0;

  private int mapCount = 0;

  private int internalHrefCount = 0;

  private int anchorRefCount = 0;

  private int invalidAnchorRefCount = 0;

  private int externalHrefCount = 0;

  private int invalidMapUrlCount = 0;

  private int invalidTargetCount = 0;

  private int invalidImageCount = 0;

  private int invalidInternalHrefCount = 0;

  private int invalidExternalHrefCount = 0;

  /**
   * The only parameter should be a path to the root of the help directory in
   * the workspace
   * 
   * @param args
   *          [0] path to the /html folder in the workspace
   * @param args
   *          [1] (optional) -nointernet to suppress external link checking for
   *          a fast check of internal links only
   * @throws IOException
   */
  public static void main(String[] args) throws IOException
  {
    if (args.length == 0 || args.length > 2
            || (args.length == 2 && !args[1].equals("-nointernet")))
    {
      log("Usage: <pathToHelpFolder> [-nointernet]");
      return;
    }

    if (args.length == 2)
    {
      internetAvailable = false;
    }

    new HelpLinksChecker().checkLinks(args[0]);
  }

  /**
   * Checks help links and reports results
   * 
   * @param helpDirectoryPath
   * @throws IOException
   */
  void checkLinks(String helpDirectoryPath) throws IOException
  {
    log("Checking help file links");
    File helpFolder = new File(helpDirectoryPath).getCanonicalFile();
    if (!helpFolder.exists())
    {
      log("Can't find " + helpDirectoryPath);
      return;
    }

    internetAvailable &= connectToUrl("http://www.example.org");

    Map<String, String> tocTargets = checkHelpMappings(helpFolder);

    Map<String, String> unusedTargets = new HashMap<String, String>(
            tocTargets);

    checkTableOfContents(helpFolder, tocTargets, unusedTargets);

    checkHelpSet(helpFolder, tocTargets, unusedTargets);

    checkHtmlFolder(new File(helpFolder, "html"));

    reportResults(unusedTargets);
  }

  /**
   * Checks all html files in the given directory or its sub-directories
   * 
   * @param folder
   * @throws IOException
   */
  private void checkHtmlFolder(File folder) throws IOException
  {
    File[] files = folder.listFiles();
    for (File f : files)
    {
      if (f.isDirectory())
      {
        checkHtmlFolder(f);
      }
      else
      {
        if (f.getAbsolutePath().endsWith(".html"))
        {
          checkHtmlFile(f, folder);
        }
      }
    }
  }

  /**
   * Checks that any image attribute in help.hs is a valid target
   * 
   * @param helpFolder
   * @param tocTargets
   * @param unusedTargets
   *          used targets are removed from here
   */
  private void checkHelpSet(File helpFolder,
          Map<String, String> tocTargets, Map<String, String> unusedTargets)
          throws IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(new File(
            helpFolder, HELP_HS)));
    String data = br.readLine();
    int lineNo = 0;

    while (data != null)
    {
      lineNo++;
      String image = getAttribute(data, "image");
      if (image != null)
      {
        unusedTargets.remove(image);
        if (!tocTargets.containsKey(image))
        {
          log(String.format("Invalid image '%s' at line %d of %s", image,
                  lineNo, HELP_HS));
          invalidImageCount++;
        }
      }
      data = br.readLine();
    }
    br.close();
  }

  /**
   * Print counts to sysout
   * 
   * @param unusedTargets
   */
  private void reportResults(Map<String, String> unusedTargets)
  {
    log("\nResults:");
    log(targetCount + " distinct help targets");
    log(mapCount + " help mappings");
    log(invalidTargetCount + " invalid targets");
    log(unusedTargets.size() + " unused targets");
    for (String target : unusedTargets.keySet())
    {
      log(String.format("    %s: %s", target, unusedTargets.get(target)));
    }
    log(invalidMapUrlCount + " invalid map urls");
    log(invalidImageCount + " invalid image attributes");
    log(String.format("%d internal href links (%d with anchors)",
            internalHrefCount, anchorRefCount));
    log(invalidInternalHrefCount + " invalid internal href links");
    log(invalidAnchorRefCount + " invalid internal anchor links");
    log(externalHrefCount + " external href links");
    if (internetAvailable)
    {
      log(invalidExternalHrefCount + " invalid external href links");
    }
    else
    {
      System.out
              .println("External links not verified as internet not available");
    }
    if (invalidInternalHrefCount > 0 || invalidExternalHrefCount > 0
            || invalidImageCount > 0 || invalidAnchorRefCount > 0)
    {
      log("*** Failed ***");
      System.exit(1);
    }
    log("*** Success ***");
  }

  /**
   * @param s
   */
  static void log(String s)
  {
    System.out.println(s);
  }

  /**
   * Reads the given html file and checks any href attibute values are either
   * <ul>
   * <li>a valid relative file path, or</li>
   * <li>a valid absolute URL (if external link checking is enabled)</li>
   * </ul>
   * 
   * @param htmlFile
   * @param htmlFolder
   *          the parent folder (for validation of relative paths)
   */
  private void checkHtmlFile(File htmlFile, File htmlFolder)
          throws IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(htmlFile));
    String data = br.readLine();
    int lineNo = 0;
    while (data != null)
    {
      lineNo++;
      String href = getAttribute(data, "href");
      if (href != null)
      {
        String anchor = null;
        int anchorPos = href.indexOf("#");
        if (anchorPos != -1)
        {
          anchor = href.substring(anchorPos + 1);
          href = href.substring(0, anchorPos);
        }
        boolean badLink = false;
        if (href.startsWith("http"))
        {
          externalHrefCount++;
          if (internetAvailable)
          {
            if (!connectToUrl(href))
            {
              badLink = true;
              invalidExternalHrefCount++;
            }
          }
        }
        else
        {
          internalHrefCount++;
          String relFile = System.getProperty("os.name").indexOf("Win") > -1 ? href.replace("/", File.separator) : href;
          File hrefFile = href.equals("") ? htmlFile : new File(htmlFolder,
                  href);
          if (hrefFile != htmlFile && !fileExists(hrefFile, relFile))
          {
            badLink = true;
            invalidInternalHrefCount++;
          }
          if (anchor != null)
          {
            anchorRefCount++;
            if (!badLink)
            {
              if (!checkAnchorExists(hrefFile, anchor))
              {
                log(String.format("Invalid anchor: %s at line %d of %s",
                        anchor, lineNo, getPath(htmlFile)));
                invalidAnchorRefCount++;
              }
            }
          }
        }
        if (badLink)
        {
          log(String.format("Invalid href %s at line %d of %s", href,
                  lineNo, getPath(htmlFile)));
        }
      }
      data = br.readLine();
    }
    br.close();
  }

  /**
   * Performs a case-sensitive check that the href'd file exists
   * 
   * @param hrefFile
   * @return
   * @throws IOException
   */
  boolean fileExists(File hrefFile, String href) throws IOException
  {
    if (!hrefFile.exists())
    {
      return false;
    }

    /*
     * On Mac or Windows, file.exists() is not case sensitive, so do an
     * additional check with case sensitivity 
     */
    int slashPos = href.lastIndexOf(File.separator);
    String expectedFileName = slashPos == -1 ? href : href
            .substring(slashPos + 1);
    String cp = hrefFile.getCanonicalPath();
    slashPos = cp.lastIndexOf(File.separator);
    String actualFileName = slashPos == -1 ? cp : cp
            .substring(slashPos + 1);

    return expectedFileName.equals(actualFileName);
  }

  /**
   * Reads the file and checks for the presence of the given html anchor
   * 
   * @param hrefFile
   * @param anchor
   * @return true if anchor is found else false
   */
  private boolean checkAnchorExists(File hrefFile, String anchor)
  {
    String nameAnchor = "<a name=\"" + anchor + "\"";
    String idAnchor = "<a id=\"" + anchor + "\"";
    boolean found = false;
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(hrefFile));
      BufferedLineReader blr = new BufferedLineReader(br, 3, this);
      String data = blr.read();
      while (data != null)
      {
        if (data.contains(nameAnchor) || data.contains(idAnchor))
        {
          found = true;
          break;
        }
        data = blr.read();
      }
      br.close();
    } catch (IOException e)
    {
      // ignore
    }
    return found;
  }

  /**
   * Returns the part of the file path starting from /help/
   * 
   * @param helpFile
   * @return
   */
  private String getPath(File helpFile)
  {
    String path = helpFile.getPath();
    int helpPos = path.indexOf("/help/");
    return helpPos == -1 ? path : path.substring(helpPos);
  }

  /**
   * Returns true if the URL returns an input stream, or false if the URL
   * returns an error code or we cannot connect to it (e.g. no internet
   * available)
   * 
   * @param url
   * @return
   */
  private boolean connectToUrl(String url)
  {
    try
    {
      URL u = new URL(url);
      InputStream connection = u.openStream();
      connection.close();
      return true;
    } catch (Throwable t)
    {
      return false;
    }
  }

  /**
   * Reads file help.jhm and checks that
   * <ul>
   * <li>each target attribute is in tocTargets</li>
   * <li>each url attribute is a valid relative file link</li>
   * </ul>
   * 
   * @param helpFolder
   */
  private Map<String, String> checkHelpMappings(File helpFolder)
          throws IOException
  {
    Map<String, String> targets = new HashMap<String, String>();
    BufferedReader br = new BufferedReader(new FileReader(new File(
            helpFolder, HELP_JHM)));
    String data = br.readLine();
    int lineNo = 0;
    while (data != null)
    {
      lineNo++;

      /*
       * record target, check for duplicates
       */
      String target = getAttribute(data, "target");
      if (target != null)
      {
        mapCount++;
        if (targets.containsKey(target))
        {
          log(String.format(
                  "Duplicate target mapping to %s at line %d of %s",
                  target, lineNo, HELP_JHM));
        }
        else
        {
          targetCount++;
        }
      }

      /*
       * validate url
       */
      String url = getAttribute(data, "url");
      if (url != null)
      {
        targets.put(target, url);
        int anchorPos = url.indexOf("#");
        if (anchorPos != -1)
        {
          url = url.substring(0, anchorPos);
        }
        if (!new File(helpFolder, url).exists())
        {
          log(String.format("Invalid url path '%s' at line %d of %s", url,
                  lineNo, HELP_JHM));
          invalidMapUrlCount++;
        }
      }
      data = br.readLine();
    }
    br.close();
    return targets;
  }

  /**
   * Reads file helpTOC.xml and reports any invalid targets
   * 
   * @param helpFolder
   * @param tocTargets
   * @param unusedTargets
   *          used targets are removed from this map
   * 
   * @return
   * @throws IOException
   */
  private void checkTableOfContents(File helpFolder,
          Map<String, String> tocTargets, Map<String, String> unusedTargets)
          throws IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(new File(
            helpFolder, HELP_TOC_XML)));
    String data = br.readLine();
    int lineNo = 0;
    while (data != null)
    {
      lineNo++;
      /*
       * assuming no more than one "target" per line of file here
       */
      String target = getAttribute(data, "target");
      if (target != null)
      {
        unusedTargets.remove(target);
        if (!tocTargets.containsKey(target))
        {
          log(String.format("Invalid target '%s' at line %d of %s", target,
                  lineNo, HELP_TOC_XML));
          invalidTargetCount++;
        }
      }
      data = br.readLine();
    }
    br.close();
  }

  /**
   * Returns the value of an attribute if found in the data, else null
   * 
   * @param data
   * @param attName
   * @return
   */
  private static String getAttribute(String data, String attName)
  {
    /*
     * make a partial attempt at ignoring within <!-- html comments -->
     * (doesn't work if multi-line)
     */
    int commentStartPos = data.indexOf("<!--");
    int commentEndPos = commentStartPos == -1 ? -1 : data.substring(
            commentStartPos + 4).indexOf("-->");
    String value = null;
    String match = attName + "=\"";
    int attPos = data.indexOf(match);
    if (attPos > 0
            && (commentStartPos == -1 || attPos < commentStartPos || attPos > commentEndPos))
    {
      data = data.substring(attPos + match.length());
      value = data.substring(0, data.indexOf("\""));
    }
    return value;
  }

  /**
   * Trim whitespace from concatenated lines but preserve one space for valid
   * parsing
   */
  @Override
  public String cleanLine(String l)
  {
    return l.trim() + " ";
  }
}
