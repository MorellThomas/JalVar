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
package jalview.analysis;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.util.Hashtable;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author jprocter
 *
 */
public class SeqsetUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * test for JAL-2046 bug - duplication of sequence features on reconstructed
   * alignment
   */
  @Test(groups = { "Functional" })
  public void testSeqFeatureAddition()
  {
    SequenceI[] sqset = new SequenceI[] { new Sequence("Aseq1", "AREALSEQ"),
        new Sequence("Aseq2", "AREALSEQ") };

    AlignmentI al = new Alignment(sqset);
    al.setDataset(null);
    AlignmentI ds = al.getDataset();
    SequenceFeature sf1 = new SequenceFeature("f1", "foo", 2, 3, "far");
    SequenceFeature sf2 = new SequenceFeature("f2", "foo", 2, 3, "far");
    ds.getSequenceAt(0).addSequenceFeature(sf1);
    Hashtable unq = SeqsetUtils.uniquify(sqset, true);
    SequenceI[] sqset2 = new SequenceI[] {
        new Sequence(sqset[0].getName(), sqset[0].getSequenceAsString()),
        new Sequence(sqset[1].getName(), sqset[1].getSequenceAsString()) };
    Assert.assertSame(sqset[0].getSequenceFeatures().get(0), sf1);
    Assert.assertTrue(sqset2[0].getSequenceFeatures().isEmpty());
    ds.getSequenceAt(0).addSequenceFeature(sf2);
    Assert.assertEquals(sqset[0].getSequenceFeatures().size(), 2);
    SeqsetUtils.deuniquify(unq, sqset2);
    // explicitly test that original sequence features still exist because they
    // are on the shared dataset sequence
    Assert.assertEquals(sqset[0].getSequenceFeatures().size(), 2);
    Assert.assertEquals(sqset2[0].getSequenceFeatures().size(), 2);
    Assert.assertSame(sqset[0].getSequenceFeatures().get(0),
            sqset2[0].getSequenceFeatures().get(0));
    Assert.assertSame(sqset[0].getSequenceFeatures().get(1),
            sqset2[0].getSequenceFeatures().get(1));
  }
}
