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

import jalview.datamodel.AlignmentI;
import jalview.gui.JvOptionPane;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PfamFormatInputTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testPfamFormatNoLimits() throws IOException
  {
    AlignmentI al = new AppletFormatAdapter().readFile(
            "ASEQ" + '\t' + "...--FFAFAFF--", DataSourceType.PASTE,
            FileFormat.Pfam);
    Assert.assertEquals(1, al.getHeight(), "Wrong number of sequences");
    Assert.assertTrue(al.hasValidSequence(),
            "Didn't extract limits from PFAM ID");
  }

  @Test(groups = "Functional")
  public void testPfamFormatValidLimits() throws IOException
  {
    AlignmentI al = new AppletFormatAdapter().readFile(
            "ASEQ/15-25" + '\t' + "...--FFAFAFF--", DataSourceType.PASTE,
            FileFormat.Pfam);
    Assert.assertEquals(1, al.getHeight(), "Wrong number of sequences");
    Assert.assertTrue(al.hasValidSequence(),
            "Didn't extract limits from PFAM ID");
  }
}
