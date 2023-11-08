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

import static org.junit.Assert.assertNotEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.api.FeatureColourI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.project.Jalview2xmlTests;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.FeatureColour;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.JalviewColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.util.MessageManager;

public class DesktopTests
{

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    setUpJvOptionPane();
    /*
     * use read-only test properties file
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Jalview.main(new String[] { "-nonews" });
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    Desktop.instance.closeAll_actionPerformed(null);
  }

  /**
   * 
   * configure (read-only) properties for test to ensure Consensus is computed
   * for colour Above PID testing
   */
  public AlignFrame loadTestFile()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.TRUE.toString());
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);

    /*
     * wait for Consensus thread to complete
     */
    do
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {
      }
    } while (af.getViewport().getCalcManager().isWorking());
    return af;
  }

  public static void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testInternalCopyPaste()
  {
    AlignFrame internalSource = loadTestFile();

    try
    {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        @Override
        public void run()
        {
          internalSource.selectAllSequenceMenuItem_actionPerformed(null);
          internalSource.copy_actionPerformed();
          Desktop.instance.paste();
        }
      });
    } catch (Exception x)
    {
      Assert.fail("Unexpected exception " + x);
    }
    AlignFrame[] alfs = Desktop.getAlignFrames();
    Assert.assertEquals("Expect just 2 alignment frames", 2, alfs.length);
    // internal paste should yield a new alignment window with shared dataset
    AlignmentI dataset = internalSource.getViewport().getAlignment()
            .getDataset();
    Assert.assertNotNull(dataset);

    for (AlignFrame alf : alfs)
    {
      Assert.assertTrue(
              "Internal paste should yield alignment with same dataset.",
              dataset == alf.getViewport().getAlignment().getDataset());
    }

  }
}
