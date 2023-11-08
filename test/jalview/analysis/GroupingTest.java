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
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GroupingTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Sequence s1 = new Sequence("s1", "AAAADDDDEEEE");

  Sequence s2 = new Sequence("s2", "AAAADDDDEEEE");

  Sequence s3 = new Sequence("s3", "ACAADDEDEEEE");

  Sequence s4 = new Sequence("s4", "AAAADDEDEEEE");

  Sequence s5 = new Sequence("s5", "AAAADDEDTTEE");

  SequenceGroup sg_12 = new SequenceGroup(
          Arrays.asList(new SequenceI[]
          { s1, s2 }), "Group1", null, false, false, false, 0, 5);

  SequenceGroup sg_345 = new SequenceGroup(
          Arrays.asList(new SequenceI[]
          { s3, s4, s5 }), "Group2", null, false, false, false, 0, 5);

  AlignmentI alignment = new Alignment(
          new SequenceI[]
          { s1, s2, s3, s4, s5 });

  /*
   * test for the case where column selections are not added in
   * left to right order
   */
  int[] positions = new int[] { 7, 9, 1 };

  @Test(groups = { "Functional" })
  public void testMakeGroupsWithBoth()
  {
    String[] str = new String[alignment.getHeight()];
    int seq = 0;
    for (SequenceI s : alignment.getSequences())
    {
      StringBuilder sb = new StringBuilder();
      for (int p : positions)
      {
        sb.append(s.getCharAt(p));
      }
      str[seq++] = sb.toString();
    }
    SequenceGroup[] seqgroupsString = Grouping.makeGroupsFrom(
            alignment.getSequencesArray(), str,
            Arrays.asList(new SequenceGroup[]
            { sg_12, sg_345 }));

    ColumnSelection cs = new ColumnSelection();
    for (int p : positions)
    {
      cs.addElement(p);
    }
    SequenceGroup[] seqgroupsColSel = Grouping.makeGroupsFromCols(
            alignment.getSequencesArray(), cs,
            Arrays.asList(new SequenceGroup[]
            { sg_12, sg_345 }));
    AssertJUnit.assertEquals(seqgroupsString.length,
            seqgroupsColSel.length);
    for (int p = 0; p < seqgroupsString.length; p++)
    {
      AssertJUnit.assertEquals(seqgroupsString[p].getName(),
              seqgroupsColSel[p].getName());
      AssertJUnit.assertArrayEquals(
              seqgroupsString[p].getSequencesInOrder(alignment),
              seqgroupsColSel[p].getSequencesInOrder(alignment));
      if (seqgroupsString[p].getSequences().contains(s2))
      {
        AssertJUnit.assertTrue(seqgroupsString[p].getSize() == 2);
      }
    }
  }

}
