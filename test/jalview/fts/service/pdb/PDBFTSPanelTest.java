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
package jalview.fts.service.pdb;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import junit.extensions.PA;

public class PDBFTSPanelTest
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
    PDBFTSPanel searchPanel = new PDBFTSPanel(null);
    assertTrue(searchPanel.getCmbSearchTarget().getItemCount() > 0);
    searchPanel.populateCmbSearchTargetOptions();
  }

  @Test(groups = { "Functional" })
  public void testDecodeSearchTerm()
  {
    String expectedString = "1xyz OR text:2xyz OR text:3xyz";
    String outcome = PDBFTSPanel.decodeSearchTerm("1xyz:A;2xyz;3xyz",
            "text");
    System.out.println("1 >>>>>>>>>>> " + outcome);
    assertEquals(expectedString, outcome);

    expectedString = "1xyz";
    outcome = PDBFTSPanel.decodeSearchTerm("1xyz", "text");
    // System.out.println("2 >>>>>>>>>>> " + outcome);
    assertEquals(expectedString, outcome);
  }

  @Test(groups = { "Functional" })
  public void testgetPDBIdwithSpecifiedChain()
  {

    String expectedString = "1xyz:A";
    String outcome = PDBFTSPanel.getPDBIdwithSpecifiedChain("1xyz",
            "2xyz;3xyz;1xyz:A");
    System.out.println("1 >>>>>>>>>>> " + outcome);
    assertEquals(expectedString, outcome);

    expectedString = "2xyz";
    outcome = PDBFTSPanel.getPDBIdwithSpecifiedChain("2xyz",
            "1xyz:A;2xyz;3xyz");
    System.out.println("2 >>>>>>>>>>> " + outcome);
    assertEquals(expectedString, outcome);

    expectedString = "2xyz:A";
    outcome = PDBFTSPanel.getPDBIdwithSpecifiedChain("2xyz", "2xyz:A");
    System.out.println("3 >>>>>>>>>>> " + outcome);
    assertEquals(expectedString, outcome);
  }

  @Test(groups = { "External" }, timeOut = 8000)
  public void txt_search_ActionPerformedTest()
  {
    PDBFTSPanel searchPanel = new PDBFTSPanel(null);
    JInternalFrame mainFrame = searchPanel.getMainFrame();
    // JComboBox<String> txt_search = PA.gsearchPanel.getTxtSearch();

    assertTrue(mainFrame.getTitle().length() == 20);
    assertTrue(
            mainFrame.getTitle().equalsIgnoreCase("PDB Sequence Fetcher"));
    PA.invokeMethod(PA.getValue(searchPanel, "txt_search"),
            "setSelectedItem(java.lang.String)", "ABC");
    // txt_search.setSelectedItem("ABC");
    try
    {
      // wait for web-service to handle response
      Thread.sleep(3000);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    assertTrue(mainFrame.getTitle().length() > 20);
    assertTrue(
            !mainFrame.getTitle().equalsIgnoreCase("PDB Sequence Fetcher"));
  }

  @Test
  public void getFTSframeTitleTest()
  {
    PDBFTSPanel searchPanel = new PDBFTSPanel(null);
    String outcome = searchPanel.getFTSFrameTitle();
    // System.out.println("FTS Frame title :" + outcome);
    assertEquals(outcome, "PDB Sequence Fetcher");
  }

}
