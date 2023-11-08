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
package jalview.io.gff;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GffHelperFactoryTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testGetHelper()
  {
    assertNull(GffHelperFactory.getHelper(null));

    String tabRegex = "\\t";

    /*
     * column 3 = 'similarity' indicates exonerate GFF alignment data
     */
    String gff = "submitted\taffine:local\tsimilarity\t20\t30\t99\t+\t.\t";
    // no attributes (column 9 data):
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof Gff2Helper);

    // attributes set but unhandled featureGroup - get generic handler
    gff = "submitted\taffine:local\tsimilarity\t20\t30\t99\t+\t.\tID=$1";
    assertSame(GffHelperFactory.getHelper(gff.split(tabRegex)).getClass(),
            Gff3Helper.class);

    // handled featureGroup (exonerate model) values
    gff = "submitted\texonerate:protein2dna:local\tsimilarity\t20\t30\t99\t+\t.\tID=$1";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof ExonerateHelper);

    gff = "submitted\tprotein2genome\tsimilarity\t20\t30\t99\t+\t.\tID=$1";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof ExonerateHelper);

    gff = "submitted\tcoding2coding\tsimilarity\t20\t30\t99\t+\t.\tID=$1";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof ExonerateHelper);

    gff = "submitted\tcoding2genome\tsimilarity\t20\t30\t99\t+\t.\tID=$1";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof ExonerateHelper);

    gff = "submitted\tcdna2genome\tsimilarity\t20\t30\t99\t+\t.\tID=$1";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof ExonerateHelper);

    gff = "submitted\tgenome2genome\tsimilarity\t20\t30\t99\t+\t.\tID=$1";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof ExonerateHelper);

    // not case-sensitive:
    gff = "submitted\tgenome2genome\tSIMILARITY\t20\t30\t99\t+\t.\tID=$1";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof ExonerateHelper);

    /*
     * InterProScan has 'protein_match' in column 3
     */
    gff = "Submitted\tPANTHER\tprotein_match\t1\t1174\t0.0\t+\t.\tName=PTHR32154";
    assertTrue(GffHelperFactory
            .getHelper(gff.split(tabRegex)) instanceof InterProScanHelper);

    /*
     * nothing specific - return the generic GFF3 class if Name=Value is present in col9
     */
    gff = "nothing\tinteresting\there\t20\t30\t99\t+\t.\tID=1";
    GffHelperI helper = GffHelperFactory.getHelper(gff.split(tabRegex));
    assertSame(helper.getClass(), Gff3Helper.class);

    // return the generic GFF2 class if "Name Value" is present in col9
    gff = "nothing\tinteresting\there\t20\t30\t99\t+\t.\tID 1";
    helper = GffHelperFactory.getHelper(gff.split(tabRegex));
    assertSame(helper.getClass(), Gff2Helper.class);
  }
}
