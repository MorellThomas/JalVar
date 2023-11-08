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
package jalview.ws.dbsources;

import jalview.datamodel.AlignmentI;
import jalview.gui.JvOptionPane;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class XfamFetcherTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "External" })
  public void testRfamSeed() throws Exception
  {
    // RfamFull rff = new RfamFull();
    RfamSeed rfs = new RfamSeed();

    AlignmentI seedrf = rfs.getSequenceRecords(rfs.getTestQuery());
    Assert.assertNotNull(seedrf, "Seed Alignment for " + rfs.getTestQuery()
            + " didn't retrieve.");
    Assert.assertTrue(seedrf.getHeight() > 1,
            "Seed Alignment for " + rfs.getTestQuery()
                    + " didn't contain more than one sequence.");
    Assert.assertTrue(seedrf.getProperties().size() > 0,
            "Seed Alignment for " + rfs.getTestQuery()
                    + " didn't have any properties.");

  }

  @Test(groups = { "External" })
  public void testPfamFullAndSeed() throws Exception
  {
    Pfam pff = new PfamFull();
    PfamSeed pfseed = new PfamSeed();

    AlignmentI fullpf = pff.getSequenceRecords(pff.getTestQuery());
    Assert.assertNotNull(fullpf, "Full Alignment for " + pff.getTestQuery()
            + " didn't retrieve.");
    Assert.assertTrue(fullpf.getHeight() > 1, "Full Alignment for "
            + pff.getTestQuery() + " didn't have more than one sequence.");
    AlignmentI seedpf = pfseed.getSequenceRecords(pff.getTestQuery());
    Assert.assertNotNull(seedpf, "Seed Alignment for " + pff.getTestQuery()
            + " didn't retrieve.");
    Assert.assertTrue(seedpf.getProperties().size() > 0,
            "Seed Alignment for " + pfseed.getTestQuery()
                    + " didn't have any properties.");

    Assert.assertTrue(seedpf.getHeight() < fullpf.getHeight(),
            "Expected Full alignment to have more sequences than seed for "
                    + pff.getTestQuery());
    Assert.assertTrue(fullpf.getProperties().size() > 0,
            "Full Alignment for " + pff.getTestQuery()
                    + " didn't have any properties.");

  }
}
