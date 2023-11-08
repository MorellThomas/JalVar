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
package jalview.datamodel;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class SequenceDummyTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * test for become method
   */
  @Test(groups = { "Functional" })
  public void testBecome()
  {
    SequenceI seq = new Sequence("OrigSeq", "ASEQUENCE");
    SequenceFeature ofeat = new SequenceFeature("NewFeat", "somedesc", 3,
            12, 2.3f, "none");

    SequenceDummy dummySeq = new SequenceDummy("OrigSeq");
    dummySeq.addSequenceFeature(ofeat);
    dummySeq.become(seq);
    assertFalse("Dummy sequence did not become a full sequence",
            dummySeq.isDummy());
    assertTrue("Sequence was not updated from template", seq
            .getSequenceAsString().equals(dummySeq.getSequenceAsString()));
    boolean found = false;
    for (SequenceFeature sf : dummySeq.getSequenceFeatures())
    {
      if (sf == ofeat)
      {
        found = true;
        break;
      }
    }
    assertTrue("Didn't retain original sequence feature", found);

    // todo - should test all aspect of copy constructor
  }
}
