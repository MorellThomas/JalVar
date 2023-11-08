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
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;

public class BackupFilesTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private static boolean actuallyDeleteTmpFiles = true;

  private static String testDir = "test/jalview/io";

  private static String testBasename = "backupfilestest";

  private static String testExt = ".fa";

  private static String testFilename = testBasename + testExt;

  private static String testFile = testDir + File.separatorChar
          + testFilename;

  private static String newBasename = testBasename + "Temp";

  private static String newFilename = newBasename + testExt;

  private static String newFile = testDir + File.separatorChar
          + newFilename;

  private static String sequenceName = "BACKUP_FILES";

  private static String sequenceDescription = "backupfiles";

  private static String sequenceData = "AAAARG";

  private static String suffix = "_BACKUPTEST-%n";

  private static int digits = 6;

  private static int rollMax = 2;

  private AlignFrame af;

  // read and save with backupfiles disabled
  @Test(groups = { "Functional" })
  public void noBackupsEnabledTest() throws Exception
  {
    // set BACKUPFILES_ENABLED to false (i.e. turn off BackupFiles feature -- no
    // backup files to be made when saving)
    setBackupFilesOptions(false, true, true);

    // init the newFile and backups (i.e. make sure newFile exists on its own
    // and has no backups
    initNewFileForTesting();

    // now save again
    save();

    // check no backup files
    File[] backupFiles = getBackupFiles();
    Assert.assertEquals(backupFiles.length, 0, "Number of backup files is "
            + backupFiles.length + ", not " + 0);
  }

  // save with no numbers in the backup file names
  @Test(groups = { "Functional" })
  public void backupsEnabledSingleFileBackupTest() throws Exception
  {
    // Enable BackupFiles and set noMax so all backupfiles get kept
    String mysuffix = "~";
    BackupFilesPresetEntry bfpe = new BackupFilesPresetEntry(mysuffix, 1,
            false, true, 1, false);
    setBackupFilesOptions(true, false, true,
            "test/jalview/io/testProps_singlefilebackup.jvprops", bfpe);

    // init the newFile and backups (i.e. make sure newFile exists on its own
    // and has no backups)
    initNewFileForTesting();
    HashMap<Integer, String> correctindexmap = new HashMap<>();
    correctindexmap.put(0, "backupfilestestTemp.fa~");

    save();
    Assert.assertTrue(checkBackupFiles(correctindexmap, newFile, "~", 1));

    // and a second time -- see JAL-3628
    save();
    Assert.assertTrue(checkBackupFiles(correctindexmap, newFile, "~", 1));

    cleanupTmpFiles(newFile, "~", 1);
  }

  // save keeping all backup files
  @Test(groups = { "Functional" })
  public void backupsEnabledNoRollMaxTest() throws Exception
  {
    // Enable BackupFiles and set noMax so all backupfiles get kept
    setBackupFilesOptions(true, false, true);

    // init the newFile and backups (i.e. make sure newFile exists on its own
    // and has no backups)
    initNewFileForTesting();

    // now save a few times again. No rollMax so should have more than two
    // backup files
    int numSaves = 10;
    for (int i = 0; i < numSaves; i++)
    {
      save();
    }

    // check 10 backup files
    HashMap<Integer, String> correctindexmap = new HashMap<>();
    correctindexmap.put(1, "backupfilestestTemp.fa_BACKUPTEST-000001");
    correctindexmap.put(2, "backupfilestestTemp.fa_BACKUPTEST-000002");
    correctindexmap.put(3, "backupfilestestTemp.fa_BACKUPTEST-000003");
    correctindexmap.put(4, "backupfilestestTemp.fa_BACKUPTEST-000004");
    correctindexmap.put(5, "backupfilestestTemp.fa_BACKUPTEST-000005");
    correctindexmap.put(6, "backupfilestestTemp.fa_BACKUPTEST-000006");
    correctindexmap.put(7, "backupfilestestTemp.fa_BACKUPTEST-000007");
    correctindexmap.put(8, "backupfilestestTemp.fa_BACKUPTEST-000008");
    correctindexmap.put(9, "backupfilestestTemp.fa_BACKUPTEST-000009");
    correctindexmap.put(10, "backupfilestestTemp.fa_BACKUPTEST-000010");
    HashMap<Integer, String> wrongindexmap = new HashMap<>();
    wrongindexmap.put(1, "backupfilestestTemp.fa_BACKUPTEST-1");
    wrongindexmap.put(2, "backupfilestestTemp.fa_BACKUPTEST-000002");
    wrongindexmap.put(3, "backupfilestestTemp.fa_BACKUPTEST-000003");
    wrongindexmap.put(4, "backupfilestestTemp.fa_BACKUPTEST-000004");
    wrongindexmap.put(5, "backupfilestestTemp.fa_BACKUPTEST-000005");
    wrongindexmap.put(6, "backupfilestestTemp.fa_BACKUPTEST-000006");
    wrongindexmap.put(7, "backupfilestestTemp.fa_BACKUPTEST-000007");
    wrongindexmap.put(8, "backupfilestestTemp.fa_BACKUPTEST-000008");
    wrongindexmap.put(9, "backupfilestestTemp.fa_BACKUPTEST-000009");
    wrongindexmap.put(10, "backupfilestestTemp.fa_BACKUPTEST-000010");
    int[] indexes2 = { 3, 4, 5, 6, 7, 8, 9, 10 };
    int[] indexes3 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    Assert.assertTrue(checkBackupFiles(correctindexmap));
    Assert.assertFalse(checkBackupFiles(wrongindexmap));
    Assert.assertFalse(checkBackupFiles(indexes2));
    Assert.assertFalse(checkBackupFiles(indexes3));
  }

  // save keeping only the last rollMax (2) backup files
  @Test(groups = { "Functional" })
  public void backupsEnabledRollMaxTest() throws Exception
  {
    // Enable BackupFiles and set noMax so all backupfiles get kept
    setBackupFilesOptions(true, false, false);

    // init the newFile and backups (i.e. make sure newFile exists on its own
    // and has no backups)
    initNewFileForTesting();

    // now save a few times again. No rollMax so should have more than two
    // backup files
    int numSaves = 10;
    for (int i = 0; i < numSaves; i++)
    {
      save();
    }

    // check there are "rollMax" backup files and they are all saved correctly
    // check 10 backup files
    HashMap<Integer, String> correctindexmap = new HashMap<>();
    correctindexmap.put(9, "backupfilestestTemp.fa_BACKUPTEST-000009");
    correctindexmap.put(10, "backupfilestestTemp.fa_BACKUPTEST-000010");
    int[] indexes2 = { 10 };
    int[] indexes3 = { 8, 9, 10 };
    Assert.assertTrue(checkBackupFiles(correctindexmap));
    Assert.assertFalse(checkBackupFiles(indexes2));
    Assert.assertFalse(checkBackupFiles(indexes3));
  }

  // save keeping only the last rollMax (2) backup files
  @Test(groups = { "Functional" })
  public void backupsEnabledReverseRollMaxTest() throws Exception
  {
    // Enable BackupFiles and set noMax so all backupfiles get kept
    setBackupFilesOptions(true, true, false);

    // init the newFile and backups (i.e. make sure newFile exists on its own
    // and has no backups)
    initNewFileForTesting();

    // now save a few times again. No rollMax so should have more than two
    // backup files
    int numSaves = 10;
    for (int i = 0; i < numSaves; i++)
    {
      save();
    }

    // check there are "rollMax" backup files and they are all saved correctly
    // check 10 backup files
    HashMap<Integer, String> correctindexmap = new HashMap<>();
    correctindexmap.put(1, "backupfilestestTemp.fa_BACKUPTEST-000001");
    correctindexmap.put(2, "backupfilestestTemp.fa_BACKUPTEST-000002");
    int[] indexes2 = { 1 };
    int[] indexes3 = { 1, 2, 3 };
    Assert.assertTrue(checkBackupFiles(correctindexmap));
    Assert.assertFalse(checkBackupFiles(indexes2));
    Assert.assertFalse(checkBackupFiles(indexes3));
  }

  private void setBackupFilesOptions()
  {
    setBackupFilesOptions(true, false, false);
  }

  private void setBackupFilesOptions(boolean enabled, boolean reverse,
          boolean noMax)
  {
    BackupFilesPresetEntry bfpe = new BackupFilesPresetEntry(suffix, digits,
            reverse, noMax, rollMax, false);
    setBackupFilesOptions(enabled, reverse, noMax,
            "test/jalview/io/testProps.jvprops", bfpe);
  }

  private void setBackupFilesOptions(boolean enabled, boolean reverse,
          boolean noMax, String propsFile, BackupFilesPresetEntry bfpe)
  {
    Cache.loadProperties(propsFile);

    Cache.applicationProperties.setProperty(BackupFiles.ENABLED,
            Boolean.toString(enabled));
    Cache.applicationProperties.setProperty(
            BackupFilesPresetEntry.SAVEDCONFIG, bfpe.toString());
    /*
    Cache.applicationProperties.setProperty(BackupFiles.ENABLED,
            Boolean.toString(enabled));
    Cache.applicationProperties.setProperty(BackupFiles.SUFFIX, suffix);
    Cache.applicationProperties.setProperty(BackupFiles.SUFFIX_DIGITS,
            Integer.toString(digits));
    Cache.applicationProperties.setProperty(BackupFiles.REVERSE_ORDER,
            Boolean.toString(reverse));
    Cache.applicationProperties.setProperty(BackupFiles.NO_MAX,
            Boolean.toString(noMax));
    Cache.applicationProperties.setProperty(BackupFiles.ROLL_MAX,
            Integer.toString(rollMax));
    Cache.applicationProperties.setProperty(BackupFiles.CONFIRM_DELETE_OLD,
            "false");
            */
  }

  private void save()
  {
    if (af != null)
    {
      af.saveAlignment(newFile, jalview.io.FileFormat.Fasta);
    }
  }

  // this runs cleanTmpFiles and then writes the newFile once as a starting
  // point for all tests
  private void initNewFileForTesting() throws Exception
  {
    cleanupTmpFiles();

    AppletFormatAdapter afa = new AppletFormatAdapter();
    AlignmentI al = afa.readFile(testFile, DataSourceType.FILE,
            jalview.io.FileFormat.Fasta);
    List<SequenceI> l = al.getSequences();

    // check this is right
    if (l.size() != 1)
    {
      throw new Exception("single sequence from '" + testFile
              + "' not read in correctly (should be a single short sequence). List<SequenceI> size is wrong.");
    }
    SequenceI s = l.get(0);
    Sequence ref = new Sequence(sequenceName, sequenceData);
    ref.setDescription(sequenceDescription);
    if (!sequencesEqual(s, ref))
    {
      throw new Exception("single sequence from '" + testFile
              + "' not read in correctly (should be a single short sequence). SequenceI name, description or data is wrong.");
    }
    // save alignment file to new filename -- this doesn't test backups disabled
    // yet as this file shouldn't already exist
    af = new AlignFrame(al, 0, 0);
    af.saveAlignment(newFile, jalview.io.FileFormat.Fasta);
  }

  // this deletes the newFile (if it exists) and any saved backup file for it
  @AfterClass(alwaysRun = true)
  private void cleanupTmpFiles()
  {
    cleanupTmpFiles(newFile, suffix, digits);
  }

  protected static void cleanupTmpFiles(String file, String mysuffix,
          int mydigits)
  {
    File newfile = new File(file);
    if (newfile.exists())
    {
      newfile.delete();
    }
    File[] tmpFiles = getBackupFiles(file, mysuffix, mydigits);
    for (int i = 0; i < tmpFiles.length; i++)
    {
      if (actuallyDeleteTmpFiles)
      {
        tmpFiles[i].delete();
      }
      else
      {
        System.out.println("Pretending to delete " + tmpFiles[i].getPath());
      }
    }
  }

  private static File[] getBackupFiles(String f, String s, int i)
  {
    TreeMap<Integer, File> bfTreeMap = BackupFiles
            .getBackupFilesAsTreeMap(f, s, i);
    File[] backupFiles = new File[bfTreeMap.size()];
    bfTreeMap.values().toArray(backupFiles);
    return backupFiles;
  }

  private static File[] getBackupFiles()
  {
    return getBackupFiles(newFile, suffix, digits);
  }

  private static boolean checkBackupFiles(HashMap<Integer, String> indexmap)
          throws IOException
  {
    return checkBackupFiles(indexmap, newFile, suffix, digits);
  }

  private static boolean checkBackupFiles(HashMap<Integer, String> indexmap,
          String file, String mysuffix, int mydigits) throws IOException
  {
    TreeMap<Integer, File> map = BackupFiles.getBackupFilesAsTreeMap(file,
            mysuffix, mydigits);
    Enumeration<Integer> indexesenum = Collections
            .enumeration(indexmap.keySet());
    while (indexesenum.hasMoreElements())
    {
      int i = indexesenum.nextElement();
      String indexfilename = indexmap.get(i);
      if (!map.containsKey(i))
      {
        return false;
      }
      File f = map.get(i);
      if (!filesContentEqual(newFile, f.getPath()))
      {
        return false;
      }
      map.remove(i);
      if (f == null)
      {
        return false;
      }
      if (!f.getName().equals(indexfilename))
      {
        return false;
      }
    }
    // should be nothing left in map
    if (map.size() > 0)
    {
      return false;
    }

    return true;
  }

  private static boolean checkBackupFiles(int[] indexes) throws IOException
  {
    TreeMap<Integer, File> map = BackupFiles
            .getBackupFilesAsTreeMap(newFile, suffix, digits);
    for (int m = 0; m < indexes.length; m++)
    {
      int i = indexes[m];
      if (!map.containsKey(i))
      {
        return false;
      }
      File f = map.get(i);
      if (!filesContentEqual(newFile, f.getPath()))
      {
        return false;
      }
      map.remove(i);
      if (f == null)
      {
        return false;
      }
      // check the filename -- although this uses the same code to forumulate
      // the filename so not much of a test!
      String filename = BackupFilenameParts.getBackupFilename(i,
              newBasename + testExt, suffix, digits);
      if (!filename.equals(f.getName()))
      {
        System.out.println("Supposed filename '" + filename
                + "' not equal to actual filename '" + f.getName() + "'");
        return false;
      }
    }
    // should be nothing left in map
    if (map.size() > 0)
    {
      return false;
    }

    return true;
  }

  private static String[] getBackupFilesAsStrings()
  {
    File[] files = getBackupFiles(newFile, suffix, digits);
    String[] filenames = new String[files.length];
    for (int i = 0; i < files.length; i++)
    {
      filenames[i] = files[i].getPath();
    }
    return filenames;
  }

  public static boolean sequencesEqual(SequenceI s1, SequenceI s2)
  {
    if (s1 == null && s2 == null)
    {
      return true;
    }
    else if (s1 == null || s2 == null)
    {
      return false;
    }
    return (s1.getName().equals(s2.getName())
            && s1.getDescription().equals(s2.getDescription())
            && Arrays.equals(s1.getSequence(), s2.getSequence()));
  }

  public static boolean filesContentEqual(String fileName1,
          String fileName2) throws IOException
  {
    Path file1 = Paths.get(fileName1);
    Path file2 = Paths.get(fileName2);
    byte[] bytes1 = Files.readAllBytes(file1);
    byte[] bytes2 = Files.readAllBytes(file2);
    return Arrays.equals(bytes1, bytes2);
  }

}
