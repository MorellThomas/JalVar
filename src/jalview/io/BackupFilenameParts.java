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

import java.io.File;

public class BackupFilenameParts
{
  private String base;

  private String templateStart;

  private int num;

  private int digits;

  private String templateEnd;

  private boolean isBackupFile;

  private BackupFilenameParts()
  {
    this.isBackupFile = false;
  }

  public BackupFilenameParts(File file, String base, String template,
          int digits)
  {
    this(file.getName(), base, template, digits);
  }

  public BackupFilenameParts(String filename, String base, String template,
          int suggesteddigits)
  {
    this(filename, base, template, suggesteddigits, false);
  }

  public BackupFilenameParts(String filename, String base, String template,
          int suggesteddigits, boolean extensionMatch)
  {
    this.isBackupFile = false;

    int numcharstart = template.indexOf(BackupFiles.NUM_PLACEHOLDER);
    int digits = 0;
    String templateStart = template;
    String templateEnd = "";
    if (numcharstart > -1)
    {
      templateStart = template.substring(0, numcharstart);
      templateEnd = template.substring(
              numcharstart + BackupFiles.NUM_PLACEHOLDER.length());
      digits = suggesteddigits;
    }

    String savedFilename = "";
    // if extensionOnly is set then reset the filename to the last occurrence of
    // the extension+templateStart and try the match
    if (extensionMatch)
    {
      // only trying to match from extension onwards

      int extensioncharstart = filename
              .lastIndexOf('.' + base + templateStart);
      if (extensioncharstart == -1)
      {
        return;
      }

      savedFilename = filename.substring(0, extensioncharstart + 1); // include
                                                                     // the "."
      filename = filename.substring(extensioncharstart + 1);
    }

    // full filename match

    // calculate minimum length of a backup filename
    int minlength = base.length() + template.length()
            - BackupFiles.NUM_PLACEHOLDER.length() + digits;

    if (!(filename.startsWith(base + templateStart)
            && filename.endsWith(templateEnd)
            && filename.length() >= minlength))
    {
      // non-starter
      return;
    }

    int startLength = base.length() + templateStart.length();
    int endLength = templateEnd.length();
    String numString = numcharstart > -1
            ? filename.substring(startLength, filename.length() - endLength)
            : "";

    if (filename.length() >= startLength + digits + endLength
            && filename.startsWith(base + templateStart)
            && filename.endsWith(templateEnd)
            // match exactly digits number of number-characters (numString
            // should be all digits and at least the right length), or more than
            // digits long with proviso it's not zero-leading.
            && (numString.matches("[0-9]{" + digits + "}")
                    || numString.matches("[1-9][0-9]{" + digits + ",}")))
    {
      this.base = extensionMatch ? savedFilename + base : base;
      this.templateStart = templateStart;
      this.num = numString.length() > 0 ? Integer.parseInt(numString) : 0;
      this.digits = digits;
      this.templateEnd = templateEnd;
      this.isBackupFile = true;
    }

  }

  public static BackupFilenameParts currentBackupFilenameParts(
          String filename, String base, boolean extensionMatch)
  {
    BackupFilenameParts bfp = new BackupFilenameParts();
    BackupFilesPresetEntry bfpe = BackupFilesPresetEntry
            .getSavedBackupEntry();
    String template = bfpe.suffix;
    if (template == null)
    {
      return bfp;
    }
    int digits;
    try
    {
      digits = bfpe.digits;
    } catch (Exception e)
    {
      return bfp;
    }
    return new BackupFilenameParts(filename, base, template, digits,
            extensionMatch);
  }

  public boolean isBackupFile()
  {
    return this.isBackupFile;
  }

  public int indexNum()
  {
    return this.num;
  }

  public static String getBackupFilename(int index, String base,
          String template, int digits)
  {
    String numString = String.format("%0" + digits + "d", index);
    String backupSuffix = template.replaceFirst(BackupFiles.NUM_PLACEHOLDER,
            numString);
    String backupfilename = base + backupSuffix;
    return backupfilename;
  }
}
