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

import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import fr.orsay.lri.varna.utils.RNAMLParser;
import groovy.lang.Sequence;

public class RNAMLfileTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
  }

  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
  }

  @Test(groups = { "Functional" })
  public void testRnamlToStockholmIO()
  {
    StockholmFileTest.testFileIOwithFormat(
            new File("examples/testdata/rna-alignment.xml"),
            FileFormat.Stockholm, -1, -1, true, true, true);

  }

  @Test(groups= {"Functional"})
  public void testRnamlSeqImport() throws IOException
  {
    RnamlFile parser = new RnamlFile("examples/testdata/7WKP-rna1.xml", DataSourceType.FILE);
    SequenceI[] seqs  = parser.getSeqsAsArray();
    assertNotNull(seqs);
    assertEquals(seqs.length,1);
    assertEquals(seqs[0].getEnd()-seqs[0].getStart()+1,seqs[0].getSequence().length);
  }

}
