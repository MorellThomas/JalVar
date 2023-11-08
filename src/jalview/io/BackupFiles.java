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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.util.MessageManager;
import jalview.util.Platform;

/*
 * BackupFiles used for manipulating (naming rolling/deleting) backup/version files when an alignment or project file is saved.
 * User configurable options are:
 * BACKUPFILES_ENABLED - boolean flag as to whether to use this mechanism or act as before, including overwriting files as saved.
 * The rest of the options are now saved as BACKUPFILES_PRESET, BACKUPFILES_SAVED and BACKUPFILES_CUSTOM
 * (see BackupFilesPresetEntry)
 */

public class BackupFiles
{

  // labels for saved params in Cache and .jalview_properties
  public static final String NS = "BACKUPFILES";

  public static final String ENABLED = NS + "_ENABLED";

  public static final String NUM_PLACEHOLDER = "%n";

  private static final String DEFAULT_TEMP_FILE = "jalview_temp_file_" + NS;

  private static final String TEMP_FILE_EXT = ".tmp";

  // file - File object to be backed up and then updated (written over)
  private File file;

  // enabled - default flag as to whether to do the backup file roll (if not
  // defined in preferences)
  private static boolean enabled;

  // confirmDelete - default flag as to whether to confirm with the user before
  // deleting old backup/version files
  private static boolean confirmDelete;

  // defaultSuffix - default template to use to append to basename of file
  private String suffix;

  // noMax - flag to turn off a maximum number of files
  private boolean noMax;

  // defaultMax - default max number of backup files
  private int max;

  // defaultDigits - number of zero-led digits to use in the filename
  private int digits;

  // reverseOrder - set to true to make newest (latest) files lowest number
  // (like rolled log files)
  private boolean reverseOrder;

  // temp saved file to become new saved file
  private File tempFile;

  // flag set to see if file save to temp file was successful
  private boolean tempFileWriteSuccess;

  // array of files to be deleted, with extra information
  private ArrayList<File> deleteFiles = new ArrayList<>();

  // date formatting for modification times
  private static final SimpleDateFormat sdf = new SimpleDateFormat(
          "yyyy-MM-dd HH:mm:ss");

  private static final String newTempFileSuffix = "_newfile";

  private static final String oldTempFileSuffix = "_oldfile_tobedeleted";

  // thread pool used for completablefutures
  private static final ExecutorService executorService = Executors
          .newFixedThreadPool(3);

  private static List<BackupFiles> savesInProgress = new ArrayList<>();

  private CompletableFuture<Boolean> myFuture = null;

  private boolean addSaveInProgress()
  {
    if (savesInProgress.contains(this))
    {
      return false;
    }
    else
    {
      this.setMyFuture();
      savesInProgress.add(this);
      return true;
    }
  }

  private boolean removeSaveInProgress(boolean ret)
  {
    if (savesInProgress.contains(this))
    {
      this.getMyFuture().complete(ret);
      // remove all occurrences
      while (savesInProgress.remove(this))
      {
      }
      return true;
    }
    return false;
  }

  private static CompletableFuture<Boolean> getNewFuture()
  {
    return new CompletableFuture<Boolean>()
    {
    };
  }

  private CompletableFuture<Boolean> getMyFuture()
  {
    return this.myFuture;
  }

  private void setMyFuture()
  {
    this.myFuture = getNewFuture();
  }

  public static boolean hasSavesInProgress()
  {
    boolean has = false;
    for (CompletableFuture cf : savesInProgressCompletableFutures(true))
    {
      has |= !cf.isDone();
    }
    return has;
  }

  public static List<File> savesInProgressFiles(boolean all)
  {
    List<File> files = new ArrayList<>();
    for (BackupFiles bfile : savesInProgress)
    {
      if (all || !bfile.getMyFuture().isDone())
        files.add(bfile.getFile());
    }
    return files;
  }

  public static List<CompletableFuture<Boolean>> savesInProgressCompletableFutures(
          boolean all)
  {
    List<CompletableFuture<Boolean>> cfs = new ArrayList<>();
    for (BackupFiles bfile : savesInProgress)
    {
      if (all || !bfile.getMyFuture().isDone())
        cfs.add(bfile.getMyFuture());
    }
    return cfs;
  }

  public static Future<Boolean> allSaved()
  {
    CompletableFuture<Boolean> f = new CompletableFuture<>();

    executorService.submit(() -> {
      for (BackupFiles buf : savesInProgress)
      {
        boolean allSaved = true;
        try
        {
          allSaved &= buf.getMyFuture().get();
        } catch (InterruptedException e)
        {
          Console.debug("InterruptedException waiting for files to save",
                  e);
        } catch (ExecutionException e)
        {
          Console.debug("ExecutionException waiting for files to save", e);
        }
        f.complete(allSaved);
      }
    });
    return f;
  }

  public BackupFiles(String filename)
  {
    this(new File(filename));
  }

  // first time defaults for SUFFIX, NO_MAX, ROLL_MAX, SUFFIX_DIGITS and
  // REVERSE_ORDER
  public BackupFiles(File file)
  {
    classInit();
    if (file.getParentFile() == null)
    {
      // filename probably in pwd represented with no parent -- fix it before
      // it's a problem
      file = file.getAbsoluteFile();
    }
    this.file = file;

    // add this file from the save in progress stack
    addSaveInProgress();

    BackupFilesPresetEntry bfpe = BackupFilesPresetEntry
            .getSavedBackupEntry();
    this.suffix = bfpe.suffix;
    this.noMax = bfpe.keepAll;
    this.max = bfpe.rollMax;
    this.digits = bfpe.digits;
    this.reverseOrder = bfpe.reverse;

    // create a temp file to save new data in
    File temp = null;
    try
    {
      if (file != null)
      {
        String tempfilename = file.getName();
        File tempdir = file.getAbsoluteFile().getParentFile();
        tempdir.mkdirs();
        Console.trace(
                "BACKUPFILES [file!=null] attempting to create temp file for "
                        + tempfilename + " in dir " + tempdir);
        temp = File.createTempFile(tempfilename,
                TEMP_FILE_EXT + newTempFileSuffix, tempdir);
        Console.debug(
                "BACKUPFILES using temp file " + temp.getAbsolutePath());
      }
      else
      {
        Console.trace(
                "BACKUPFILES [file==null] attempting to create default temp file "
                        + DEFAULT_TEMP_FILE + " with extension "
                        + TEMP_FILE_EXT);
        temp = File.createTempFile(DEFAULT_TEMP_FILE, TEMP_FILE_EXT);
      }
    } catch (IOException e)
    {
      Console.error("Could not create temp file to save to (IOException)");
      Console.error(e.getMessage());
      Console.debug(Cache.getStackTraceString(e));
    } catch (Exception e)
    {
      Console.error("Exception creating temp file for saving");
      Console.debug(Cache.getStackTraceString(e));
    }
    this.setTempFile(temp);
  }

  private static void classInit()
  {
    Console.initLogger();
    Console.trace("BACKUPFILES classInit");
    boolean e = Cache.getDefault(ENABLED, !Platform.isJS());
    setEnabled(e);
    Console.trace("BACKUPFILES " + (e ? "enabled" : "disabled"));
    BackupFilesPresetEntry bfpe = BackupFilesPresetEntry
            .getSavedBackupEntry();
    Console.trace("BACKUPFILES preset scheme " + bfpe.toString());
    setConfirmDelete(bfpe.confirmDelete);
    Console.trace("BACKUPFILES confirm delete " + bfpe.confirmDelete);
  }

  public static void setEnabled(boolean flag)
  {
    enabled = flag;
  }

  public static boolean getEnabled()
  {
    classInit();
    return enabled;
  }

  public static void setConfirmDelete(boolean flag)
  {
    confirmDelete = flag;
  }

  public static boolean getConfirmDelete()
  {
    classInit();
    return confirmDelete;
  }

  // set, get and rename temp file into place
  public void setTempFile(File temp)
  {
    this.tempFile = temp;
  }

  public File getTempFile()
  {
    return tempFile;
  }

  public String getTempFilePath()
  {
    String path = null;
    try
    {
      path = this.getTempFile().getCanonicalPath();
    } catch (IOException e)
    {
      Console.error("IOException when getting Canonical Path of temp file '"
              + this.getTempFile().getName() + "'");
      Console.debug(Cache.getStackTraceString(e));
    }
    return path;
  }

  public boolean setWriteSuccess(boolean flag)
  {
    boolean old = this.tempFileWriteSuccess;
    this.tempFileWriteSuccess = flag;
    return old;
  }

  public boolean getWriteSuccess()
  {
    return this.tempFileWriteSuccess;
  }

  public boolean renameTempFile()
  {
    return moveFileToFile(tempFile, file);
  }

  // roll the backupfiles
  public boolean rollBackupFiles()
  {
    return this.rollBackupFiles(true);
  }

  public boolean rollBackupFiles(boolean tidyUp)
  {
    // file doesn't yet exist or backups are not enabled or template is null or
    // empty
    if ((!file.exists()) || (!enabled) || max < 0 || suffix == null
            || suffix.length() == 0)
    {
      // nothing to do
      Console.debug("BACKUPFILES rollBackupFiles nothing to do." + ", "
              + "filename: " + (file != null ? file.getName() : "null")
              + ", " + "file exists: " + file.exists() + ", " + "enabled: "
              + enabled + ", " + "max: " + max + ", " + "suffix: '" + suffix
              + "'");
      return true;
    }

    Console.trace("BACKUPFILES rollBackupFiles starting");

    String dir = "";
    File dirFile;
    try
    {
      dirFile = file.getParentFile();
      dir = dirFile.getCanonicalPath();
      Console.trace("BACKUPFILES dir: " + dir);
    } catch (Exception e)
    {
      Console.error("Could not get canonical path for file '" + file + "'");
      Console.error(e.getMessage());
      Console.debug(Cache.getStackTraceString(e));
      return false;
    }
    String filename = file.getName();
    String basename = filename;

    Console.trace("BACKUPFILES filename is " + filename);
    boolean ret = true;
    // Create/move backups up one

    deleteFiles.clear();

    // find existing backup files
    BackupFilenameFilter bff = new BackupFilenameFilter(basename, suffix,
            digits);
    File[] backupFiles = dirFile.listFiles(bff);
    int nextIndexNum = 0;

    Console.trace("BACKUPFILES backupFiles.length: " + backupFiles.length);
    if (backupFiles.length == 0)
    {
      // No other backup files. Just need to move existing file to backupfile_1
      Console.trace(
              "BACKUPFILES no existing backup files, setting index to 1");
      nextIndexNum = 1;
    }
    else
    {
      TreeMap<Integer, File> bfTreeMap = sortBackupFilesAsTreeMap(
              backupFiles, basename);
      // bfTreeMap now a sorted list of <Integer index>,<File backupfile>
      // mappings

      if (reverseOrder)
      {
        // backup style numbering
        Console.trace("BACKUPFILES rolling files in reverse order");

        int tempMax = noMax ? -1 : max;
        // noMax == true means no limits
        // look for first "gap" in backupFiles
        // if tempMax is -1 at this stage just keep going until there's a gap,
        // then hopefully tempMax gets set to the right index (a positive
        // integer so the loop breaks)...
        // why do I feel a little uneasy about this loop?..
        for (int i = 1; tempMax < 0 || i <= max; i++)
        {
          if (!bfTreeMap.containsKey(i)) // first index without existent
                                         // backupfile
          {
            tempMax = i;
          }
        }

        File previousFile = null;
        File fileToBeDeleted = null;
        for (int n = tempMax; n > 0; n--)
        {
          String backupfilename = dir + File.separatorChar
                  + BackupFilenameParts.getBackupFilename(n, basename,
                          suffix, digits);
          File backupfile_n = new File(backupfilename);

          if (!backupfile_n.exists())
          {
            // no "oldest" file to delete
            previousFile = backupfile_n;
            fileToBeDeleted = null;
            Console.trace("BACKUPFILES No oldest file to delete");
            continue;
          }

          // check the modification time of this (backupfile_n) and the previous
          // file (fileToBeDeleted) if the previous file is going to be deleted
          if (fileToBeDeleted != null)
          {
            File replacementFile = backupfile_n;
            long fileToBeDeletedLMT = fileToBeDeleted.lastModified();
            long replacementFileLMT = replacementFile.lastModified();
            Console.trace("BACKUPFILES fileToBeDeleted is "
                    + fileToBeDeleted.getAbsolutePath());
            Console.trace("BACKUPFILES replacementFile is "
                    + backupfile_n.getAbsolutePath());

            try
            {
              File oldestTempFile = nextTempFile(fileToBeDeleted.getName(),
                      dirFile);

              if (fileToBeDeletedLMT > replacementFileLMT)
              {
                String fileToBeDeletedLMTString = sdf
                        .format(fileToBeDeletedLMT);
                String replacementFileLMTString = sdf
                        .format(replacementFileLMT);
                Console.warn("WARNING! I am set to delete backupfile "
                        + fileToBeDeleted.getName()
                        + " has modification time "
                        + fileToBeDeletedLMTString
                        + " which is newer than its replacement "
                        + replacementFile.getName()
                        + " with modification time "
                        + replacementFileLMTString);

                boolean delete = confirmNewerDeleteFile(fileToBeDeleted,
                        replacementFile, true);
                Console.trace("BACKUPFILES "
                        + (delete ? "confirmed" : "not") + " deleting file "
                        + fileToBeDeleted.getAbsolutePath()
                        + " which is newer than "
                        + replacementFile.getAbsolutePath());

                if (delete)
                {
                  // User has confirmed delete -- no need to add it to the list
                  fileToBeDeleted.delete();
                }
                else
                {
                  Console.debug("BACKUPFILES moving "
                          + fileToBeDeleted.getAbsolutePath() + " to "
                          + oldestTempFile.getAbsolutePath());
                  moveFileToFile(fileToBeDeleted, oldestTempFile);
                }
              }
              else
              {
                Console.debug("BACKUPFILES going to move "
                        + fileToBeDeleted.getAbsolutePath() + " to "
                        + oldestTempFile.getAbsolutePath());
                moveFileToFile(fileToBeDeleted, oldestTempFile);
                addDeleteFile(oldestTempFile);
              }

            } catch (Exception e)
            {
              Console.error(
                      "Error occurred, probably making new temp file for '"
                              + fileToBeDeleted.getName() + "'");
              Console.error(Cache.getStackTraceString(e));
            }

            // reset
            fileToBeDeleted = null;
          }

          if (!noMax && n == tempMax && backupfile_n.exists())
          {
            fileToBeDeleted = backupfile_n;
          }
          else
          {
            if (previousFile != null)
            {
              // using boolean '&' instead of '&&' as don't want moveFileToFile
              // attempt to be conditional (short-circuit)
              ret = ret & moveFileToFile(backupfile_n, previousFile);
            }
          }

          previousFile = backupfile_n;
        }

        // index to use for the latest backup
        nextIndexNum = 1;
      }
      else // not reverse numbering
      {
        // version style numbering (with earliest file deletion if max files
        // reached)

        bfTreeMap.values().toArray(backupFiles);
        StringBuilder bfsb = new StringBuilder();
        for (int i = 0; i < backupFiles.length; i++)
        {
          if (bfsb.length() > 0)
          {
            bfsb.append(", ");
          }
          bfsb.append(backupFiles[i].getName());
        }
        Console.trace("BACKUPFILES backupFiles: " + bfsb.toString());

        // noMax == true means keep all backup files
        if ((!noMax) && bfTreeMap.size() >= max)
        {
          Console.trace("BACKUPFILES noMax: " + noMax + ", " + "max: " + max
                  + ", " + "bfTreeMap.size(): " + bfTreeMap.size());
          // need to delete some files to keep number of backups to designated
          // max.
          // Note that if the suffix is not numbered then do not delete any
          // backup files later or we'll delete the new backup file (there can
          // be only one).
          int numToDelete = suffix.indexOf(NUM_PLACEHOLDER) > -1
                  ? bfTreeMap.size() - max + 1
                  : 0;
          Console.trace("BACKUPFILES numToDelete: " + numToDelete);
          // the "replacement" file is the latest backup file being kept (it's
          // not replacing though)
          File replacementFile = numToDelete < backupFiles.length
                  ? backupFiles[numToDelete]
                  : null;
          for (int i = 0; i < numToDelete; i++)
          {
            // check the deletion files for modification time of the last
            // backupfile being saved
            File fileToBeDeleted = backupFiles[i];
            boolean delete = true;

            Console.trace(
                    "BACKUPFILES fileToBeDeleted: " + fileToBeDeleted);

            boolean newer = false;
            if (replacementFile != null)
            {
              long fileToBeDeletedLMT = fileToBeDeleted.lastModified();
              long replacementFileLMT = replacementFile != null
                      ? replacementFile.lastModified()
                      : Long.MAX_VALUE;
              if (fileToBeDeletedLMT > replacementFileLMT)
              {
                String fileToBeDeletedLMTString = sdf
                        .format(fileToBeDeletedLMT);
                String replacementFileLMTString = sdf
                        .format(replacementFileLMT);

                Console.warn("WARNING! I am set to delete backupfile '"
                        + fileToBeDeleted.getName()
                        + "' has modification time "
                        + fileToBeDeletedLMTString
                        + " which is newer than the oldest backupfile being kept '"
                        + replacementFile.getName()
                        + "' with modification time "
                        + replacementFileLMTString);

                delete = confirmNewerDeleteFile(fileToBeDeleted,
                        replacementFile, false);
                if (delete)
                {
                  // User has confirmed delete -- no need to add it to the list
                  fileToBeDeleted.delete();
                  Console.debug("BACKUPFILES deleting fileToBeDeleted: "
                          + fileToBeDeleted);
                  delete = false;
                }
                else
                {
                  // keeping file, nothing to do!
                  Console.debug("BACKUPFILES keeping fileToBeDeleted: "
                          + fileToBeDeleted);
                }
              }
            }
            if (delete)
            {
              addDeleteFile(fileToBeDeleted);
              Console.debug("BACKUPFILES addDeleteFile(fileToBeDeleted): "
                      + fileToBeDeleted);
            }

          }

        }

        nextIndexNum = bfTreeMap.lastKey() + 1;
      }
    }

    // Let's make the new backup file!! yay, got there at last!
    String latestBackupFilename = dir + File.separatorChar
            + BackupFilenameParts.getBackupFilename(nextIndexNum, basename,
                    suffix, digits);
    Console.trace("BACKUPFILES Moving old file [" + file
            + "] to latestBackupFilename [" + latestBackupFilename + "]");
    // using boolean '&' instead of '&&' as don't want moveFileToFile attempt to
    // be conditional (short-circuit)
    ret = ret & moveFileToFile(file, new File(latestBackupFilename));
    Console.debug(
            "BACKUPFILES moving " + file + " to " + latestBackupFilename
                    + " was " + (ret ? "" : "NOT ") + "successful");
    if (tidyUp)
    {
      Console.debug("BACKUPFILES tidying up files");
      tidyUpFiles();
    }

    return ret;
  }

  private static File nextTempFile(String filename, File dirFile)
          throws IOException
  {
    File temp = null;
    COUNT: for (int i = 1; i < 1000; i++)
    {
      File trythis = new File(dirFile,
              filename + '~' + Integer.toString(i));
      if (!trythis.exists())
      {
        temp = trythis;
        break COUNT;
      }

    }
    if (temp == null)
    {
      temp = File.createTempFile(filename, TEMP_FILE_EXT, dirFile);
    }
    return temp;
  }

  private void tidyUpFiles()
  {
    deleteOldFiles();
  }

  private static boolean confirmNewerDeleteFile(File fileToBeDeleted,
          File replacementFile, boolean replace)
  {
    StringBuilder messageSB = new StringBuilder();

    File ftbd = fileToBeDeleted;
    String ftbdLMT = sdf.format(ftbd.lastModified());
    String ftbdSize = Long.toString(ftbd.length());

    File rf = replacementFile;
    String rfLMT = sdf.format(rf.lastModified());
    String rfSize = Long.toString(rf.length());

    int confirmButton = JvOptionPane.NO_OPTION;
    if (replace)
    {
      File saveFile = null;
      try
      {
        saveFile = nextTempFile(ftbd.getName(), ftbd.getParentFile());
      } catch (Exception e)
      {
        Console.error(
                "Error when confirming to keep backup file newer than other backup files.");
        e.printStackTrace();
      }
      messageSB.append(MessageManager.formatMessage(
              "label.newerdelete_replacement_line", new String[]
              { ftbd.getName(), rf.getName(), ftbdLMT, rfLMT, ftbdSize,
                  rfSize }));
      // "Backup file\n''{0}''\t(modified {2}, size {4})\nis to be deleted and
      // replaced by apparently older file \n''{1}''\t(modified {3}, size
      // {5}).""
      messageSB.append("\n\n");
      messageSB.append(MessageManager.formatMessage(
              "label.confirm_deletion_or_rename", new String[]
              { ftbd.getName(), saveFile.getName() }));
      // "Confirm deletion of ''{0}'' or rename to ''{1}''?"
      String[] options = new String[] {
          MessageManager.getString("label.delete"),
          MessageManager.getString("label.rename") };

      confirmButton = Platform.isHeadless() ? JvOptionPane.YES_OPTION
              : JvOptionPane.showOptionDialog(Desktop.desktop,
                      messageSB.toString(),
                      MessageManager.getString(
                              "label.backupfiles_confirm_delete"),
                      // "Confirm delete"
                      JvOptionPane.YES_NO_OPTION,
                      JvOptionPane.WARNING_MESSAGE, null, options,
                      options[0]);
    }
    else
    {
      messageSB.append(MessageManager
              .formatMessage("label.newerdelete_line", new String[]
              { ftbd.getName(), rf.getName(), ftbdLMT, rfLMT, ftbdSize,
                  rfSize }));
      // "Backup file\n''{0}''\t(modified {2}, size {4})\nis to be deleted but
      // is newer than the oldest remaining backup file \n''{1}''\t(modified
      // {3}, size {5})."
      messageSB.append("\n\n");
      messageSB.append(MessageManager
              .formatMessage("label.confirm_deletion", new String[]
              { ftbd.getName() }));
      // "Confirm deletion of ''{0}''?"
      String[] options = new String[] {
          MessageManager.getString("label.delete"),
          MessageManager.getString("label.keep") };

      confirmButton = Platform.isHeadless() ? JvOptionPane.YES_OPTION
              : JvOptionPane.showOptionDialog(Desktop.desktop,
                      messageSB.toString(),
                      MessageManager.getString(
                              "label.backupfiles_confirm_delete"),
                      // "Confirm delete"
                      JvOptionPane.YES_NO_OPTION,
                      JvOptionPane.WARNING_MESSAGE, null, options,
                      options[0]);
    }

    // return should be TRUE if file is to be deleted
    return (confirmButton == JvOptionPane.YES_OPTION);
  }

  private void deleteOldFiles()
  {
    if (deleteFiles != null && !deleteFiles.isEmpty())
    {
      boolean doDelete = false;
      StringBuilder messageSB = null;
      if (confirmDelete && deleteFiles.size() > 0)
      {
        messageSB = new StringBuilder();
        messageSB.append(MessageManager
                .getString("label.backupfiles_confirm_delete_old_files"));
        // "Delete the following older backup files? (see the Backups tab in
        // Preferences for more options)"
        for (int i = 0; i < deleteFiles.size(); i++)
        {
          File df = deleteFiles.get(i);
          messageSB.append("\n");
          messageSB.append(df.getName());
          messageSB.append(" ");
          messageSB.append(MessageManager.formatMessage("label.file_info",
                  new String[]
                  { sdf.format(df.lastModified()),
                      Long.toString(df.length()) }));
          // "(modified {0}, size {1})"
        }

        int confirmButton = Platform.isHeadless() ? JvOptionPane.YES_OPTION
                : JvOptionPane.showConfirmDialog(Desktop.desktop,
                        messageSB.toString(),
                        MessageManager.getString(
                                "label.backupfiles_confirm_delete"),
                        // "Confirm delete"
                        JvOptionPane.YES_NO_OPTION,
                        JvOptionPane.WARNING_MESSAGE);

        doDelete = (confirmButton == JvOptionPane.YES_OPTION);
      }
      else
      {
        doDelete = true;
      }

      if (doDelete)
      {
        for (int i = 0; i < deleteFiles.size(); i++)
        {
          File fileToDelete = deleteFiles.get(i);
          Console.trace("BACKUPFILES about to delete fileToDelete:"
                  + fileToDelete);
          fileToDelete.delete();
          Console.warn("deleted '" + fileToDelete.getName() + "'");
        }
      }

    }

    deleteFiles.clear();
  }

  private TreeMap<Integer, File> sortBackupFilesAsTreeMap(
          File[] backupFiles, String basename)
  {
    // sort the backup files (based on integer found in the suffix) using a
    // precomputed Hashmap for speed
    Map<Integer, File> bfHashMap = new HashMap<>();
    for (int i = 0; i < backupFiles.length; i++)
    {
      File f = backupFiles[i];
      BackupFilenameParts bfp = new BackupFilenameParts(f, basename, suffix,
              digits);
      bfHashMap.put(bfp.indexNum(), f);
    }
    TreeMap<Integer, File> bfTreeMap = new TreeMap<>();
    bfTreeMap.putAll(bfHashMap);
    return bfTreeMap;
  }

  public boolean rollBackupsAndRenameTempFile()
  {
    boolean write = this.getWriteSuccess();

    boolean roll = false;
    boolean rename = false;
    if (write)
    {
      roll = this.rollBackupFiles(false); // tidyUpFiles at the end
      rename = this.renameTempFile();
    }

    /*
     * Not sure that this confirmation is desirable.  By this stage the new file is
     * already written successfully, but something (e.g. disk full) has happened while 
     * trying to roll the backup files, and most likely the filename needed will already
     * be vacant so renaming the temp file is nearly always correct!
     */
    boolean okay = roll && rename;
    if (!okay)
    {
      StringBuilder messageSB = new StringBuilder();
      messageSB.append(MessageManager.getString(
              "label.backupfiles_confirm_save_file_backupfiles_roll_wrong"));
      // "Something possibly went wrong with the backups of this file."
      if (rename)
      {
        if (messageSB.length() > 0)
        {
          messageSB.append("\n");
        }
        messageSB.append(MessageManager.getString(
                "label.backupfiles_confirm_save_new_saved_file_ok"));
        // "The new saved file seems okay."
      }
      else
      {
        if (messageSB.length() > 0)
        {
          messageSB.append("\n");
        }
        messageSB.append(MessageManager.getString(
                "label.backupfiles_confirm_save_new_saved_file_not_ok"));
        // "The new saved file might not be okay."
      }
      if (messageSB.length() > 0)
      {
        messageSB.append("\n");
      }
      messageSB
              .append(MessageManager.getString("label.continue_operation"));

      int confirmButton = Platform.isHeadless() ? JvOptionPane.OK_OPTION
              : JvOptionPane.showConfirmDialog(Desktop.desktop,
                      messageSB.toString(),
                      MessageManager.getString(
                              "label.backupfiles_confirm_save_file"),
                      // "Confirm save file"
                      JvOptionPane.OK_OPTION, JvOptionPane.WARNING_MESSAGE);
      okay = confirmButton == JvOptionPane.OK_OPTION;
    }
    if (okay)
    {
      tidyUpFiles();
    }

    // remove this file from the save in progress stack
    removeSaveInProgress(rename);

    return rename;
  }

  public static TreeMap<Integer, File> getBackupFilesAsTreeMap(
          String fileName, String suffix, int digits)
  {
    File[] backupFiles = null;

    File file = new File(fileName);

    File dirFile;
    try
    {
      dirFile = file.getParentFile();
    } catch (Exception e)
    {
      Console.error("Could not get canonical path for file '" + file + "'");
      return new TreeMap<>();
    }

    String filename = file.getName();
    String basename = filename;

    // find existing backup files
    BackupFilenameFilter bff = new BackupFilenameFilter(basename, suffix,
            digits);
    backupFiles = dirFile.listFiles(bff); // is clone needed?

    // sort the backup files (based on integer found in the suffix) using a
    // precomputed Hashmap for speed
    Map<Integer, File> bfHashMap = new HashMap<>();
    for (int i = 0; i < backupFiles.length; i++)
    {
      File f = backupFiles[i];
      BackupFilenameParts bfp = new BackupFilenameParts(f, basename, suffix,
              digits);
      bfHashMap.put(bfp.indexNum(), f);
    }
    TreeMap<Integer, File> bfTreeMap = new TreeMap<>();
    bfTreeMap.putAll(bfHashMap);

    return bfTreeMap;
  }

  /*
  private boolean addDeleteFile(File fileToBeDeleted, File originalFile,
          boolean delete, boolean newer)
  {
    return addDeleteFile(fileToBeDeleted, originalFile, null, delete, newer);
  }
  */
  private boolean addDeleteFile(File fileToBeDeleted)
  {
    boolean ret = false;
    int pos = deleteFiles.indexOf(fileToBeDeleted);
    if (pos > -1)
    {
      Console.debug("BACKUPFILES not adding file "
              + fileToBeDeleted.getAbsolutePath()
              + " to the delete list (already at index" + pos + ")");
      return true;
    }
    else
    {
      Console.debug("BACKUPFILES adding file "
              + fileToBeDeleted.getAbsolutePath() + " to the delete list");
      deleteFiles.add(fileToBeDeleted);
    }
    return ret;
  }

  public File getFile()
  {
    return file;
  }

  public static boolean moveFileToFile(File oldFile, File newFile)
  {
    Console.initLogger();
    boolean ret = false;
    Path oldPath = Paths.get(oldFile.getAbsolutePath());
    Path newPath = Paths.get(newFile.getAbsolutePath());
    try
    {
      // delete destination file - not usually necessary but Just In Case...
      Console.trace("BACKUPFILES deleting " + newFile.getAbsolutePath());
      newFile.delete();
      Console.trace("BACKUPFILES moving " + oldFile.getAbsolutePath()
              + " to " + newFile.getAbsolutePath());
      Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
      ret = true;
      Console.trace("BACKUPFILES move seems to have succeeded");
    } catch (IOException e)
    {
      Console.warn("Could not move file '" + oldPath.toString() + "' to '"
              + newPath.toString() + "'");
      Console.error(e.getMessage());
      Console.debug(Cache.getStackTraceString(e));
      ret = false;
    } catch (Exception e)
    {
      Console.error(e.getMessage());
      Console.debug(Cache.getStackTraceString(e));
      ret = false;
    }
    return ret;
  }
}
