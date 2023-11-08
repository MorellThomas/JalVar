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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FormatAdapterTest
{

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
  @Test(groups = { "Functional" }, dataProvider = "formats")
  public void testRoundTrip(FileFormatI format) throws IOException
  {
    try
    {
      AlignmentI al = new FormatAdapter().readFile("examples/uniref50.fa",
              DataSourceType.FILE, FileFormat.Fasta);

      /*
       * 'gap' is the gap character used in the alignment data file here,
       * not the user preferred gap character
       */
      char gap = al.getGapCharacter();
      assertNotNull(al);

      SequenceI[] seqs = al.getSequencesArray();
      String formatted = new FormatAdapter().formatSequences(format, al,
              false);

      AlignmentI reloaded = new FormatAdapter().readFile(formatted,
              DataSourceType.PASTE, format);
      List<SequenceI> reread = reloaded.getSequences();
      assertEquals("Wrong number of reloaded sequences", seqs.length,
              reread.size());

      int i = 0;
      for (SequenceI seq : reread)
      {
        String sequenceString = seq.getSequenceAsString();

        /*
         * special case: MSF always uses '.' as gap character
         */
        sequenceString = adjustForGapTreatment(sequenceString, gap, format);
        assertEquals(String.format("Sequence %d: %s", i, seqs[i].getName()),
                seqs[i].getSequenceAsString(), sequenceString);
        i++;
      }
    } catch (IOException e)
    {
      fail(String.format("Format %s failed with %s", format,
              e.getMessage()));
    }
  }

  /**
   * Optionally change the gap character in the string to the given character,
   * depending on the sequence file format
   * 
   * @param sequenceString
   *          a sequence (as written in 'format' format)
   * @param gap
   *          the sequence's original gap character
   * @param format
   * @return
   */
  String adjustForGapTreatment(String sequenceString, char gap,
          FileFormatI format)
  {
    if (FileFormat.MSF.equals(format))
    {
      /*
       * MSF forces gap character to '.', so change it back
       * for comparison purposes
       */
      sequenceString = sequenceString.replace('.', gap);
    }
    return sequenceString;
  }

  /**
   * Data provider that serves alignment formats that are both readable and
   * (text) writable
   * 
   * @return
   */
  @DataProvider(name = "formats")
  static Object[][] getFormats()
  {
    List<FileFormatI> both = new ArrayList<FileFormatI>();
    for (FileFormatI format : FileFormats.getInstance().getFormats())
    {
      if (format.isReadable() && format.isWritable()
              && format.isTextFormat())
      {
        both.add(format);
      }
    }

    Object[][] formats = new Object[both.size()][];
    int i = 0;
    for (FileFormatI format : both)
    {
      formats[i] = new Object[] { format };
      i++;
    }
    return formats;
  }

  /**
   * Enable this to isolate testing to a single file format
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" }, enabled = false)
  public void testOneFormatRoundTrip() throws IOException
  {
    testRoundTrip(FileFormat.Json);
  }
}
