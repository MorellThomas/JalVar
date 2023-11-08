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
package jalview.ext.paradise;

import java.util.Locale;

import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FastaFile;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.Iterator;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import compbio.util.FileUtil;
import mc_view.PDBfile;

public class TestAnnotate3D
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Network" }, enabled = true)
  public void test1GIDbyId() throws Exception
  {
    // use same ID as standard tests given at
    // https://bitbucket.org/fjossinet/pyrna-rest-clients
    Iterator<Reader> ids = Annotate3D.getRNAMLForPDBId("1GID");
    assertTrue("Didn't retrieve 1GID by id.", ids != null);
    testRNAMLcontent(ids, null);
  }

  @Test(groups = { "Network" }, enabled = true)
  public void testIdVsContent2GIS() throws Exception
  {
    Iterator<Reader> ids = Annotate3D.getRNAMLForPDBId("2GIS");
    assertTrue("Didn't retrieve 2GIS by id.", ids != null);
    Iterator<Reader> files = Annotate3D.getRNAMLForPDBFileAsString(
            FileUtil.readFileToString(new File("examples/2GIS.pdb")));
    assertTrue("Didn't retrieve using examples/2GIS.pdb.", files != null);
    int i = 0;
    while (ids.hasNext() && files.hasNext())
    {
      BufferedReader file = new BufferedReader(files.next()),
              id = new BufferedReader(ids.next());
      String iline, fline;
      do
      {
        iline = id.readLine();
        fline = file.readLine();
        if (iline != null)
        {
          System.out.println(iline);
        }
        if (fline != null)
        {
          System.out.println(fline);
        }
        // next assert fails for latest RNAview - because the XMLID entries
        // change between file and ID based RNAML generation.
        assertTrue(
                "Results differ for ID and file upload based retrieval (chain entry "
                        + (++i) + ")",
                ((iline == fline && iline == null) || (iline != null
                        && fline != null && iline.equals(fline))));

      } while (iline != null);
    }
  }

  /**
   * test to demonstrate JAL-1142 - compare sequences in RNAML returned from
   * Annotate3d vs those extracted by Jalview from the originl PDB file
   * 
   * @throws Exception
   */
  @Test(groups = { "Network" }, enabled = true)
  public void testPDBfileVsRNAML() throws Exception
  {
    PDBfile pdbf = new PDBfile(true, false, true, "examples/2GIS.pdb",
            DataSourceType.FILE);
    Assert.assertTrue(pdbf.isValid());
    // Comment - should add new FileParse constructor like new FileParse(Reader
    // ..). for direct reading
    Iterator<Reader> readers = Annotate3D.getRNAMLForPDBFileAsString(
            FileUtil.readFileToString(new File("examples/2GIS.pdb")));
    testRNAMLcontent(readers, pdbf);
  }

  private void testRNAMLcontent(Iterator<Reader> readers, PDBfile pdbf)
          throws Exception
  {
    StringBuffer sb = new StringBuffer();
    int r = 0;
    while (readers.hasNext())
    {
      System.out.println("Testing RNAML input number " + (++r));
      BufferedReader br = new BufferedReader(readers.next());
      String line;
      while ((line = br.readLine()) != null)
      {
        sb.append(line + "\n");
      }
      assertTrue("No data returned by Annotate3D", sb.length() > 0);
      final String lines = sb.toString();
      AlignmentI al = new FormatAdapter().readFile(lines,
              DataSourceType.PASTE, FileFormat.Rnaml);
      if (al == null || al.getHeight() == 0)
      {
        System.out.println(lines);
      }
      assertTrue("No alignment returned.", al != null);
      assertTrue("No sequences in returned alignment.", al.getHeight() > 0);
      if (pdbf != null)
      {
        for (SequenceI sq : al.getSequences())
        {
          {
            SequenceI struseq = null;
            String sq_ = sq.getSequenceAsString().toLowerCase(Locale.ROOT);
            for (SequenceI _struseq : pdbf.getSeqsAsArray())
            {
              final String lowerCase = _struseq.getSequenceAsString()
                      .toLowerCase(Locale.ROOT);
              if (lowerCase.equals(sq_))
              {
                struseq = _struseq;
                break;
              }
            }
            if (struseq == null)
            {
              AssertJUnit.fail(
                      "Couldn't find this sequence in original input:\n"
                              + new FastaFile().print(new SequenceI[]
                              { sq }, true) + "\n\nOriginal input:\n"
                              + new FastaFile().print(pdbf.getSeqsAsArray(),
                                      true)
                              + "\n");
            }
          }
        }
      }
    }
  }
}
