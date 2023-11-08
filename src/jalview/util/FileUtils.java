package jalview.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import jalview.bin.Console;

public class FileUtils
{
  /*
   * Given string glob pattern (see
   * https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)
   * ) return a List of Files that match the pattern.
   * Note this is a java style glob, not necessarily a bash-style glob, though there are sufficient similarities. 
   */
  public static List<File> getFilesFromGlob(String pattern)
  {
    return getFilesFromGlob(pattern, true);
  }

  public static List<File> getFilesFromGlob(String pattern,
          boolean allowSingleFilenameThatDoesNotExist)
  {
    pattern = substituteHomeDir(pattern);
    List<File> files = new ArrayList<>();
    /*
     * For efficiency of the Files.walkFileTree(), let's find the longest path that doesn't need globbing.
     * We look for the first glob character * { ? and then look for the last File.separator before that.
     * Then we can reset the path to look at and shorten the globbing pattern.
     * Relative paths can be used in pattern, which work from the pwd (though these are converted into
     * full paths in the match). 
     */
    int firstGlobChar = -1;
    boolean foundGlobChar = false;
    for (char c : new char[] { '*', '{', '?' })
    {
      if (pattern.indexOf(c) > -1
              && (pattern.indexOf(c) < firstGlobChar || !foundGlobChar))
      {
        firstGlobChar = pattern.indexOf(c);
        foundGlobChar = true;
      }
    }
    int lastFS = pattern.lastIndexOf(File.separatorChar, firstGlobChar);
    if (foundGlobChar)
    {
      String pS = pattern.substring(0, lastFS + 1);
      String rest = pattern.substring(lastFS + 1);
      Path parentDir = Paths.get(pS).toAbsolutePath();
      if (parentDir.toFile().exists())
      {
        try
        {
          String glob = "glob:" + parentDir.toString() + File.separator
                  + rest;
          PathMatcher pm = FileSystems.getDefault().getPathMatcher(glob);
          int maxDepth = rest.contains("**") ? 1028
                  : (int) (rest.chars()
                          .filter(ch -> ch == File.separatorChar).count())
                          + 1;

          Files.walkFileTree(parentDir,
                  EnumSet.of(FileVisitOption.FOLLOW_LINKS), maxDepth,
                  new SimpleFileVisitor<Path>()
                  {
                    @Override
                    public FileVisitResult visitFile(Path path,
                            BasicFileAttributes attrs) throws IOException
                    {
                      if (pm.matches(path))
                      {
                        files.add(path.toFile());
                      }
                      return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file,
                            IOException exc) throws IOException
                    {
                      return FileVisitResult.CONTINUE;
                    }
                  });
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    else
    {
      // no wildcards
      File f = new File(pattern);
      if (allowSingleFilenameThatDoesNotExist || f.exists())
      {
        files.add(f);
      }
    }
    Collections.sort(files);

    return files;
  }

  public static List<String> getFilenamesFromGlob(String pattern)
  {
    // convert list of Files to list of File.getPath() Strings
    return getFilesFromGlob(pattern).stream().map(f -> f.getPath())
            .collect(Collectors.toList());
  }

  public static String substituteHomeDir(String path)
  {
    return path.startsWith("~" + File.separator)
            ? System.getProperty("user.home") + path.substring(1)
            : path;
  }

  /*
   * This method returns the basename of File file
   */
  public static String getBasename(File file)
  {
    return getBasenameOrExtension(file, false);
  }

  /*
   * This method returns the extension of File file.
   */
  public static String getExtension(File file)
  {
    return getBasenameOrExtension(file, true);
  }

  public static String getBasenameOrExtension(File file, boolean extension)
  {
    if (file == null)
      return null;

    String value = null;
    String filename = file.getName();
    int lastDot = filename.lastIndexOf('.');
    if (lastDot > 0) // don't truncate if starts with '.'
    {
      value = extension ? filename.substring(lastDot + 1)
              : filename.substring(0, lastDot);
    }
    else
    {
      value = extension ? "" : filename;
    }
    return value;
  }

  /*
   * This method returns the dirname of the first --append or --open value. 
   * Used primarily for substitutions in output filenames.
   */
  public static String getDirname(File file)
  {
    if (file == null)
      return null;

    String dirname = null;
    try
    {
      File p = file.getParentFile();
      File d = new File(substituteHomeDir(p.getPath()));
      dirname = d.getCanonicalPath();
    } catch (IOException e)
    {
      Console.debug(
              "Exception when getting dirname of '" + file.getPath() + "'",
              e);
      dirname = "";
    }
    return dirname;
  }
}
