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

import java.util.Locale;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A singleton registry of alignment file formats known to Jalview. On startup,
 * the 'built-in' formats are added (from the FileFormat enum). Additional
 * formats can be registered (or formats deregistered) programmatically, for
 * example with a Groovy script.
 * 
 * @author gmcarstairs
 *
 */
public class FileFormats
{
  private static FileFormats instance = new FileFormats();

  /*
   * A lookup map of file formats by upper-cased name
   */
  private static Map<String, FileFormatI> formats;

  /*
   * Formats in this set are capable of being identified by IdentifyFile 
   */
  private static Set<FileFormatI> identifiable;

  public static FileFormats getInstance()
  {
    return instance;
  }

  /**
   * Private constructor registers Jalview's built-in file formats
   */
  private FileFormats()
  {
    reset();
  }

  /**
   * Reset to just the built-in file formats packaged with Jalview. These are
   * added (and will be shown in menus) in the order of their declaration in the
   * FileFormat enum.
   */
  public synchronized void reset()
  {
    formats = new LinkedHashMap<String, FileFormatI>();
    identifiable = new HashSet<FileFormatI>();
    for (FileFormat format : FileFormat.values())
    {
      registerFileFormat(format, format.isIdentifiable());
    }
  }

  /**
   * Answers true if the format is one that can be identified by IdentifyFile.
   * Answers false for a null value.
   */
  public boolean isIdentifiable(FileFormatI f)
  {
    return identifiable.contains(f);
  }

  /**
   * Registers a file format for case-insensitive lookup by name
   * 
   * @param format
   */
  public void registerFileFormat(FileFormatI format)
  {
    boolean isIdentifiable = format instanceof FileFormat
            && ((FileFormat) format).isIdentifiable();
    registerFileFormat(format, isIdentifiable);
  }

  protected void registerFileFormat(FileFormatI format,
          boolean isIdentifiable)
  {
    String name = format.getName().toUpperCase(Locale.ROOT);
    if (formats.containsKey(name))
    {
      System.err.println("Overwriting file format: " + format.getName());
    }
    formats.put(name, format);
    if (isIdentifiable)
    {
      identifiable.add(format);
    }
  }

  /**
   * Deregisters a file format so it is no longer shown in menus
   * 
   * @param name
   */
  public void deregisterFileFormat(String name)
  {
    FileFormatI ff = formats.remove(name.toUpperCase(Locale.ROOT));
    identifiable.remove(ff);
  }

  /**
   * Answers a list of writeable file formats (as strings, corresponding to the
   * getName() and forName() methods)
   * 
   * @param textOnly
   *          if true, only text (not binary) formats are included
   * @return
   */
  public List<String> getWritableFormats(boolean textOnly)
  {
    List<String> l = new ArrayList<String>();
    for (FileFormatI ff : formats.values())
    {
      if (ff.isWritable() && (!textOnly || ff.isTextFormat()))
      {
        l.add(ff.getName());
      }
    }
    return l;
  }

  /**
   * Answers a list of readable file formats (as strings, corresponding to the
   * getName() and forName() methods)
   * 
   * @return
   */
  public List<String> getReadableFormats()
  {
    List<String> l = new ArrayList<String>();
    for (FileFormatI ff : formats.values())
    {
      if (ff.isReadable())
      {
        l.add(ff.getName());
      }
    }
    return l;
  }

  /**
   * Returns the file format with the given name, or null if format is null or
   * invalid. This is not case-sensitive.
   * 
   * @param format
   * @return
   */
  public FileFormatI forName(String format)
  {
    return format == null ? null
            : formats.get(format.toUpperCase(Locale.ROOT));
  }

  /**
   * Returns an iterable collection of registered file formats (in the order in
   * which they were registered)
   * 
   * @return
   */
  public Iterable<FileFormatI> getFormats()
  {
    return formats.values();
  }
}
