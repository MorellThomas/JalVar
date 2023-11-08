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
package jalview.gui;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProgressBarTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private JPanel statusPanel;

  private JLabel statusBar;

  @Test(groups = { "Functional" })
  public void testConstructor_prematureInstantiation()
  {
    try
    {
      new ProgressBar(null, null);
      Assert.fail("Expected exception");
    } catch (NullPointerException e)
    {
      // expected
    }
  }

  @Test(groups = { "Functional" })
  public void testConstructor_wrongLayout()
  {
    statusPanel = new JPanel();
    statusPanel.setLayout(new FlowLayout());
    try
    {
      new ProgressBar(statusPanel, null);
      Assert.fail("expected exception");
    } catch (IllegalArgumentException e)
    {
      // expected
    }
  }

  @Test(groups = { "Functional" })
  public void testSetProgressBar()
  {
    statusPanel = new JPanel();
    GridLayout layout = new GridLayout(1, 1);
    statusPanel.setLayout(layout);
    statusBar = new JLabel("nothing");
    ProgressBar pb = new ProgressBar(statusPanel, statusBar);

    /*
     * Add 'hello'
     */
    pb.setProgressBar("hello", 1L);
    verifyProgress(layout, new String[] { "hello" });

    /*
     * Add 'world'
     */
    pb.setProgressBar("world", 2L);
    verifyProgress(layout, new String[] { "hello", "world" });

    /*
     * Remove 'hello' with no status bar update
     */
    pb.setProgressBar(null, 1L);
    verifyProgress(layout, new String[] { "world" });
    assertEquals("nothing", statusBar.getText());

    /*
     * Remove 'world' with status bar update
     */
    pb.setProgressBar("goodbye", 2L);
    verifyProgress(layout, new String[] {});
    assertEquals("goodbye", statusBar.getText());
  }

  /**
   * Verify the right number of progress bars containing the expected messages
   * respectively
   * 
   * @param layout
   * @param msgs
   */
  private void verifyProgress(final GridLayout layout, final String[] msgs)
  {
    try
    {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        @Override
        public void run()
        {
          int msgCount = msgs.length;
          assertEquals(1 + msgCount, layout.getRows());
          assertEquals(msgCount, statusPanel.getComponentCount());
          int i = 0;
          for (Component c : statusPanel.getComponents())
          {
            assertTrue(c instanceof JPanel);
            assertTrue(((JPanel) c).getComponent(0) instanceof JLabel);
            assertEquals(msgs[i++],
                    ((JLabel) ((JPanel) c).getComponent(0)).getText());
          }
        }
      });
    } catch (Exception e)
    {
      throw new AssertionError(
              "Unexpected exception waiting for progress bar validation",
              e);
    }
  }
}
