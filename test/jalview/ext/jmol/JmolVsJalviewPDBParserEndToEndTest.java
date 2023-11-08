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
package jalview.ext.jmol;

import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.testng.annotations.BeforeClass;

import mc_view.PDBfile;

/**
 * This is not a unit test, rather it is a bulk End-to-End scan for sequences
 * consistency for PDB files parsed with JmolParser vs. Jalview's PDBfile
 * parser. The directory of PDB files to test must be provided in the launch
 * args.
 * 
 * @author tcnofoegbu
 *
 */
public class JmolVsJalviewPDBParserEndToEndTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * 
   * @param args
   * @j2sIgnore
   */
  public static void main(String[] args)
  {
    if (args == null || args[0] == null)
    {
      System.err.println(
              "You must provide a PDB directory in the launch argument");
      return;
    }

    // args[0] must provide the directory of PDB files to run the test with
    String testDir = args[0];
    System.out.println("PDB directory : " + testDir);
    File pdbDir = new File(testDir);
    String testFiles[] = pdbDir.list();
    testFileParser(testDir, testFiles);
  }

  public static void testFileParser(String testDir, String[] testFiles)
  {
    Set<String> failedFiles = new HashSet<>();
    int totalSeqScanned = 0, totalFail = 0;
    for (String pdbStr : testFiles)
    {
      String testFile = testDir + "/" + pdbStr;
      PDBfile mctest = null;
      JmolParser jtest = null;
      try
      {
        mctest = new PDBfile(false, false, false, testFile,
                DataSourceType.FILE);
        jtest = new JmolParser(testFile, DataSourceType.FILE);
      } catch (IOException e)
      {
        System.err.println("Exception thrown while parsing : " + pdbStr);
      }
      Vector<SequenceI> seqs = jtest.getSeqs();
      Vector<SequenceI> mcseqs = mctest.getSeqs();

      for (SequenceI sq : seqs)
      {
        try
        {
          String testSeq = mcseqs.remove(0).getSequenceAsString();
          if (!sq.getSequenceAsString().equals(testSeq))
          {
            ++totalFail;
            System.err.println("Test Failed for " + pdbStr + ". Diff:");
            System.err.println(sq.getSequenceAsString());
            System.err.println(testSeq);
            failedFiles.add(pdbStr);
          }
          ++totalSeqScanned;
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    int count = 0;

    System.out.println("\n\nTotal sequence Scanned : " + totalSeqScanned);
    System.out.println(
            "Total sequence passed : " + (totalSeqScanned - totalFail));
    System.out.println("Total sequence failed : " + totalFail);
    System.out.println("Success rate: "
            + ((totalSeqScanned - totalFail) * 100) / totalSeqScanned
            + "%");
    System.out.println("\nList of " + failedFiles.size()
            + " file(s) with sequence diffs:");
    for (String problemFile : failedFiles)
    {
      System.out.println(++count + ". " + problemFile);
    }
  }
}
