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
package jalview.fts.threedbeacons;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import jalview.fts.service.pdb.PDBFTSPanel;
import jalview.fts.service.threedbeacons.TDBeaconsFTSPanel;
import jalview.gui.JvOptionPane;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import junit.extensions.PA;

public class TDBeaconsPanelTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
  }

  @Test(groups = { "Functional" })
  public void populateCmbSearchTargetOptionsTest()
  {
    TDBeaconsFTSPanel searchPanel = new TDBeaconsFTSPanel(null);
    assertTrue(searchPanel.getCmbSearchTarget().getItemCount() > 0);
    searchPanel.populateCmbSearchTargetOptions();
  }

  @Test
  public void getFTSframeTitleTest()
  {
    TDBeaconsFTSPanel searchPanel = new TDBeaconsFTSPanel(null);
    System.out.println(searchPanel.getFTSFrameTitle());
  }

  @Test
  public void testgetUNIPROTid()
  {
    String outcome = TDBeaconsFTSPanel.decodeSearchTerm("P01308");
    System.out.println(outcome);
  }
  //
  // @Test
  // public void queryTest() {
  // int outcome = TDBeaconsFTSPanel.executeParse("P01308");
  // //System.out.println("query outcome :" + outcome);
  // int expected_length = 110;
  // assertEquals(outcome, expected_length);
  // }
}
