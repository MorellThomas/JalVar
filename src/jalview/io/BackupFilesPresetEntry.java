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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.util.MessageManager;

public class BackupFilesPresetEntry
{

  public String suffix;

  public static final int DIGITSMIN = 1;

  public static final int DIGITSMAX = 6;

  public int digits;

  public boolean reverse;

  public boolean keepAll;

  public static final int ROLLMAXMIN = 1;

  public static final int ROLLMAXMAX = 999;

  public int rollMax;

  public boolean confirmDelete;

  public static final String SAVEDCONFIG = BackupFiles.NS + "_SAVED";

  public static final String CUSTOMCONFIG = BackupFiles.NS + "_CUSTOM";

  private static final String stringDelim = "\t";

  public static final int BACKUPFILESSCHEMECUSTOM = 0;

  public static final int BACKUPFILESSCHEMEDEFAULT = 1;

  public BackupFilesPresetEntry(String suffix, int digits, boolean reverse,
          boolean keepAll, int rollMax, boolean confirmDelete)
  {
    this.suffix = suffix == null ? "" : suffix;
    this.digits = digits < DIGITSMIN ? DIGITSMIN
            : (digits > DIGITSMAX ? DIGITSMAX : digits);
    this.reverse = reverse;
    this.keepAll = keepAll;
    this.rollMax = rollMax < ROLLMAXMIN ? ROLLMAXMIN
            : (rollMax > ROLLMAXMAX ? ROLLMAXMAX : rollMax);
    this.confirmDelete = confirmDelete;
  }

  public boolean equals(BackupFilesPresetEntry compare)
  {
    return suffix.equals(compare.suffix) && digits == compare.digits
            && reverse == compare.reverse && keepAll == compare.keepAll
            && rollMax == compare.rollMax
            && confirmDelete == compare.confirmDelete;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(suffix);
    sb.append(stringDelim);
    sb.append(digits);
    sb.append(stringDelim);
    sb.append(reverse);
    sb.append(stringDelim);
    sb.append(keepAll);
    sb.append(stringDelim);
    sb.append(rollMax);
    sb.append(stringDelim);
    sb.append(confirmDelete);
    return sb.toString();
  }

  public static BackupFilesPresetEntry createBackupFilesPresetEntry(
          String line)
  {
    if (line == null)
    {
      return null;
    }
    StringTokenizer st = new StringTokenizer(line, stringDelim);
    String suffix = null;
    int digits = 0;
    boolean reverse = false;
    boolean keepAll = false;
    int rollMax = 0;
    boolean confirmDelete = false;

    try
    {
      suffix = st.nextToken();
      digits = Integer.valueOf(st.nextToken());
      reverse = Boolean.valueOf(st.nextToken());
      keepAll = Boolean.valueOf(st.nextToken());
      rollMax = Integer.valueOf(st.nextToken());
      confirmDelete = Boolean.valueOf(st.nextToken());
    } catch (Exception e)
    {
      Console.error("Error parsing backupfiles scheme '" + line + "'");
    }

    return new BackupFilesPresetEntry(suffix, digits, reverse, keepAll,
            rollMax, confirmDelete);
  }

  public static BackupFilesPresetEntry getSavedBackupEntry()
  {
    String savedPresetString = Cache
            .getDefault(BackupFilesPresetEntry.SAVEDCONFIG, null);
    BackupFilesPresetEntry savedPreset = BackupFilesPresetEntry
            .createBackupFilesPresetEntry(savedPresetString);
    if (savedPreset == null)
    {
      savedPreset = backupfilesPresetEntriesValues
              .get(BACKUPFILESSCHEMEDEFAULT);
    }
    return savedPreset;
  }

  public static final IntKeyStringValueEntry[] backupfilesPresetEntries = {
      new IntKeyStringValueEntry(BACKUPFILESSCHEMEDEFAULT,
              MessageManager.getString("label.default")),
      new IntKeyStringValueEntry(2,
              MessageManager.getString("label.single_file")),
      new IntKeyStringValueEntry(3,
              MessageManager.getString("label.keep_all_versions")),
      new IntKeyStringValueEntry(4,
              MessageManager.getString("label.rolled_backups")),
      // ...
      // IMPORTANT, keep "Custom" entry with key 0 (even though it appears last)
      new IntKeyStringValueEntry(BACKUPFILESSCHEMECUSTOM,
              MessageManager.getString("label.custom")) };

  public static final String[] backupfilesPresetEntryDescriptions = {
      MessageManager.getString("label.default_description"),
      MessageManager.getString("label.single_file_description"),
      MessageManager.getString("label.keep_all_versions_description"),
      MessageManager.getString("label.rolled_backups_description"),
      MessageManager.getString("label.custom_description") };

  public static final Map<Integer, BackupFilesPresetEntry> backupfilesPresetEntriesValues = new HashMap<Integer, BackupFilesPresetEntry>()
  {
    /**
     * 
     */
    private static final long serialVersionUID = 125L;

    {
      put(1, new BackupFilesPresetEntry(
              ".bak" + BackupFiles.NUM_PLACEHOLDER, 3, false, false, 3,
              false));
      put(2, new BackupFilesPresetEntry("~", 1, false, false, 1, false));
      put(3, new BackupFilesPresetEntry(".v" + BackupFiles.NUM_PLACEHOLDER,
              3, false, true, 10, true));
      put(4, new BackupFilesPresetEntry(
              "_bak." + BackupFiles.NUM_PLACEHOLDER, 1, true, false, 9,
              false));

      // This gets replaced by GPreferences
      put(BACKUPFILESSCHEMECUSTOM,
              new BackupFilesPresetEntry("", 0, false, false, 0, false));
    }
  };

}
