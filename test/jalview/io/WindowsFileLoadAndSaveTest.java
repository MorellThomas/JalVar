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
import java.nio.file.StandardCopyOption;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.AlignmentI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;

/**
 * WindowsFileSaveTest simply opens an alignment file and then tries to save it.
 * This failed in Windows from 2.11.0 to 2.11.1.6 due to a combination of the
 * opening file handle being left open ad infinitum, causing the BackupFiles
 * operation of moving the saved (temp) file onto the original filename to fail,
 * but only in Windows. See: https://issues.jalview.org/browse/JAL-3628
 * https://issues.jalview.org/browse/JAL-3703
 * https://issues.jalview.org/browse/JAL-3935 These issues are really all fixed
 * by JAL-3703 This test is to ensure it doesn't start again, but note that this
 * test will only fail in Windows.
 */
public class WindowsFileLoadAndSaveTest
{

  private final static String fileName = "examples" + File.separator
          + "uniref50.fa";

  private final static String testFileName = fileName + "-TEST";

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test saving and re-reading in a specified format
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void loadAndSaveAlignment() throws IOException
  {
    File file = new File(fileName);
    File testFile = new File(testFileName);
    Files.copy(file.toPath(), testFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
    FormatAdapter fa = new FormatAdapter();
    AlignmentI a = fa.readFile(testFile, DataSourceType.FILE,
            FileFormat.Fasta);

    AlignFrame af = new AlignFrame(a, 500, 500);
    af.saveAlignment(testFileName, FileFormat.Fasta);

    Assert.assertTrue(af.isSaveAlignmentSuccessful());
  }

  @AfterClass(alwaysRun = true)
  private void cleanupTmpFiles()
  {
    BackupFilesPresetEntry bfpe = BackupFilesPresetEntry
            .getSavedBackupEntry();
    BackupFilesTest.cleanupTmpFiles(testFileName, bfpe.suffix, bfpe.digits);
  }

}
