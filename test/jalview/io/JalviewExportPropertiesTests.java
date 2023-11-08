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

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.SequenceGroup;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;

/**
 * tests which verify that properties and preferences are correctly interpreted
 * when exporting/importing data
 * 
 * @author jprocter
 *
 */
public class JalviewExportPropertiesTests
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    jalview.bin.Jalview
            .main(new String[]
            { "-props", "test/jalview/io/testProps.jvprops" });
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);

  }

  @Test(groups = { "Functional" })
  public void testImportExportPeriodGaps() throws Exception
  {
    jalview.bin.Cache.setProperty("GAP_SYMBOL", ".");
    assertTrue("Couldn't set gap character to '.'",
            ".".equals("" + jalview.bin.Cache.getProperty("GAP_SYMBOL")));
    AlignFrame af = new jalview.io.FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    assertTrue("Didn't read in the example file correctly.", af != null);
    assertTrue("Didn't set the gap character correctly", af.getViewport()
            .getAlignment().getSequenceAt(0).getCharAt(5) == '.');

    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(af.getViewport().getAlignment().getSequenceAt(0), false);
    sg.addSequence(af.getViewport().getAlignment().getSequenceAt(1), false);
    sg.setStartRes(1);
    sg.setEndRes(7);
    af.getViewport().setSelectionGroup(sg);
    String fseqs = new FormatAdapter(af.alignPanel)
            .formatSequences(FileFormat.Fasta, af.alignPanel, true);
    assertTrue("Couldn't find '.' in the exported region\n" + fseqs,
            fseqs.indexOf(".") > -1);
  }
}
