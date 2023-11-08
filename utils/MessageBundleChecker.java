
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * This class scans Java source files for calls to MessageManager and reports
 * <ul>
 * <li>calls using keys not found in Messages.properties</li>
 * <li>any unused keys in Messages.properties</li>
 * </ul>
 * It does not handle dynamically constructed keys, these are reported as
 * possible errors for manual inspection. <br>
 * For comparing translated bundles with Messages.properties, see i18nAnt.xml
 * 
 * @author gmcarstairs
 *
 */
public class MessageBundleChecker implements BufferedLineReader.LineCleaner
{
  /*
   * regex ^"[^"]*"$
   * opening quote, closing quote, no quotes in between
   */
  static Pattern STRING_PATTERN = Pattern.compile("^\"[^\"]*\"$");

  /*
   * number of text lines to read at a time in order to parse
   * code that is split over several lines
   */
  static int bufferSize = 3;

  /*
   * resource bundle key is arg0 for these methods
   */
  static final String METHOD1 = "MessageManager.getString(";

  static final String METHOD2 = "MessageManager.formatMessage(";

  static final String METHOD3 = "MessageManager.getStringOrReturn(";

  /*
   * resource bundle key is arg1 for this method
   */
  static final String JVINIT = "JvSwingUtils.jvInitComponent(";

  static final String[] METHODS = { METHOD1, METHOD2, METHOD3, JVINIT };

  /*
   * root of the Java source folders we want to scan
   */
  String sourcePath;

  /*
   * contents of Messages.properties
   */
  private Properties messages;

  /*
   * keys from Messages.properties
   * we remove entries from here as they are found to be used
   * any left over are unused entries
   */
  private TreeSet<String> messageKeys;

  private int javaCount;

  private Set<String> invalidKeys;

  private Set<String> dynamicKeys;

  /**
   * Runs the scan given the path to the root of Java source directories
   * 
   * @param args
   *          [0] path to the source folder to scan
   * @param args
   *          [1] (optional) read buffer size (default is 3); increasing this
   *          may detect more results but will give higher error counts due to
   *          double counting of the same code
   * @throws IOException
   */
  public static void main(String[] args) throws IOException
  {
    if (args.length != 1 && args.length != 2)
    {
      System.out.println("Usage: <pathToSourceFolder> [readBufferSize]");
      return;
    }
    if (args.length == 2)
    {
      bufferSize = Integer.valueOf(args[1]);
    }
    new MessageBundleChecker().doMain(args[0]);
  }

  /**
   * Main method to perform the work
   * 
   * @param srcPath
   * @throws IOException
   */
  private void doMain(String srcPath) throws IOException
  {
    System.out.println(
            "Scanning " + srcPath + " for calls to MessageManager\n");

    sourcePath = srcPath;
    loadMessages();
    File dir = new File(srcPath);
    if (!dir.exists())
    {
      System.out.println(srcPath + " not found");
      return;
    }

    invalidKeys = new HashSet<>();
    dynamicKeys = new HashSet<>();

    if (dir.isDirectory())
    {
      scanDirectory(dir);
    }
    else
    {
      scanFile(dir);
    }
    reportResults();
  }

  /**
   * Prints out counts to sysout
   */
  private void reportResults()
  {
    System.out.println(
            "\nMessages.properties has " + messages.size() + " keys");
    System.out.println("Scanned " + javaCount + " source files\n");

    if (!invalidKeys.isEmpty())
    {
      System.out.println("Found " + invalidKeys.size()
              + " possibly invalid unmatched key(s) in source code"
              + (invalidKeys.size() > 1 ? "s" : ""));
    }

    System.out.println(
            "Keys not found in source code, assumed constructed dynamically:");
    int dynamicCount = 0;
    for (String key : messageKeys)
    {
      if (isDynamic(key))
      {
        System.out.println("    " + key);
        dynamicCount++;
      }
    }

    if (dynamicCount < messageKeys.size())
    {
      System.out.println((messageKeys.size() - dynamicCount)
              + " key(s) not found, possibly unused, or used indirectly (check code manually!)");
      for (String key : messageKeys)
      {
        if (!isDynamic(key))
        {
          System.out.println("    " + key);
        }
      }
    }
    System.out
            .println("\nRun i18nAnt.xml to compare other message bundles");
  }

  /**
   * Answers true if the key starts with one of the recorded dynamic key stubs,
   * else false
   * 
   * @param key
   * @return
   */
  private boolean isDynamic(String key)
  {
    for (String dynamic : dynamicKeys)
    {
      if (key.startsWith(dynamic))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Scan all files within a directory
   * 
   * @param dir
   * @throws IOException
   */
  private void scanDirectory(File dir) throws IOException
  {
    File[] files = dir.listFiles();
    if (files != null)
    {
      for (File f : files)
      {
        if (f.isDirectory())
        {
          scanDirectory(f);
        }
        else
        {
          scanFile(f);
        }
      }
    }
  }

  /**
   * Scan a Java file
   * 
   * @param f
   */
  private void scanFile(File f) throws IOException
  {
    String path = f.getPath();
    if (!path.endsWith(".java"))
    {
      return;
    }
    javaCount++;

    /*
     * skip class with designed dynamic lookup call
     */
    if (path.endsWith("gui/JvSwingUtils.java"))
    {
      return;
    }

    BufferedReader br = new BufferedReader(new FileReader(f));
    BufferedLineReader blr = new BufferedLineReader(br, bufferSize, this);

    int lineNo = 0;
    String line = blr.read();
    while (line != null)
    {
      lineNo++;
      inspectSourceLines(path, lineNo, line);
      line = blr.read();
    }
    br.close();

  }

  /**
   * Look for calls to MessageManager methods, possibly split over two or more
   * lines that have been concatenated while parsing the file
   * 
   * @param path
   * @param lineNo
   * @param line
   */
  private void inspectSourceLines(String path, int lineNo,
          final String line)
  {
    String lineNos = String.format("%d-%d", lineNo,
            lineNo + bufferSize - 1);
    for (String method : METHODS)
    {
      int pos = line.indexOf(method);
      if (pos == -1)
      {
        continue;
      }

      /*
       * extract what follows the opening bracket of the method call
       */
      String methodArgs = line.substring(pos + method.length()).trim();
      if ("".equals(methodArgs))
      {
        /*
         * arguments are on next line - catch in the next read loop iteration
         */
        continue;
      }
      if (methodArgs.indexOf(",") == -1 && methodArgs.indexOf(")") == -1)
      {
        /*
         * arguments continue on next line - catch in the next read loop iteration
         */
        continue;
      }

      if (JVINIT == method && methodArgs.indexOf(",") == -1)
      {
        /*
         * not interested in 1-arg calls to jvInitComponent
         */
        continue;
      }

      String messageKey = getMessageKey(method, methodArgs);

      if (METHOD3 == method)
      {
        String key = messageKey.substring(1, messageKey.length() - 1);
        if (!dynamicKeys.contains(key))
        {
//          System.out.println(String.format(
//                  "Dynamic key \"" + key + "\" at %s line %s %s",
//                  path.substring(sourcePath.length()), lineNos, line));
          dynamicKeys.add(key);
        }
        continue;
      }

      if (messageKey == null)
      {
        System.out.println(String.format("Trouble parsing %s line %s %s",
                path.substring(sourcePath.length()), lineNos, line));
        continue;
      }

      if (!(STRING_PATTERN.matcher(messageKey).matches()))
      {
//        System.out.println(String.format("Dynamic key at %s line %s %s",
//                path.substring(sourcePath.length()), lineNos, line));
        continue;
      }

      /*
       * strip leading and trailing quote
       */
      messageKey = messageKey.substring(1, messageKey.length() - 1);

      if (!this.messages.containsKey(messageKey))
      {
        if (!invalidKeys.contains(messageKey))
        {
          // report each key the first time found only
          System.out.println(String.format(
                  "Unmatched key '%s' at line %s of %s", messageKey,
                  lineNos, path.substring(sourcePath.length())));
          invalidKeys.add(messageKey);
        }
      }
      messageKeys.remove(messageKey);
    }

    /*
     * and a brute force scan for _any_ as yet unseen message key, to catch the
     * cases where it is in a variable or a condition
     */
    if (line.contains("\""))
    {
      Iterator<String> it = messageKeys.iterator();
      while (it.hasNext())
      {
        if (line.contains(it.next()))
        {
          it.remove(); // remove as 'seen'
        }
      }
    }
  }

  /**
   * Helper method to parse out the resource bundle key parameter of a method
   * call
   * 
   * @param method
   * @param methodArgs
   *          the rest of the source line starting with arguments to method
   * @return
   */
  private String getMessageKey(String method, String methodArgs)
  {
    String key = methodArgs;

    /*
     * locate second argument if calling jvInitComponent()
     */
    if (method == JVINIT)
    {
      int commaLoc = methodArgs.indexOf(",");
      if (commaLoc == -1)
      {
        return null;
      }
      key = key.substring(commaLoc + 1).trim();
    }

    /*
     * take up to next comma or ) or end of line
     */
    int commaPos = key.indexOf(",");
    int bracePos = key.indexOf(")");
    int endPos = commaPos == -1 ? bracePos
            : (bracePos == -1 ? commaPos : Math.min(commaPos, bracePos));
    if (endPos == -1 && key.length() > 1 && key.endsWith("\""))
    {
      endPos = key.length();
    }

    return endPos == -1 ? null : key.substring(0, endPos);
  }

  /**
   * Loads properties from Message.properties
   * 
   * @throws IOException
   */
  void loadMessages() throws IOException
  {
    messages = new Properties();
    FileReader reader = new FileReader(
            new File(sourcePath, "../resources/lang/Messages.properties"));
    messages.load(reader);
    reader.close();

    messageKeys = new TreeSet<>();
    for (Object key : messages.keySet())
    {
      messageKeys.add((String) key);
    }

  }

  /**
   * Remove any trailing comments, change tabs to space, and trim
   */
  @Override
  public String cleanLine(String l)
  {
    if (l != null)
    {
      int pos = l.indexOf("//");
      if (pos != -1)
      {
        l = l.substring(0, pos);
      }
      l = l.replace("\t", " ").trim();
    }
    return l;
  }

}
