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

import static org.testng.Assert.assertSame;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.util.Arrays;
import java.util.List;

import junit.extensions.PA;

import org.testng.annotations.Test;

public class AlignmentSorterTest
{
  @Test(groups = "Functional")
  public void testSortByFeature_score()
  {
    SequenceI seq1 = new Sequence("Seq1", "ABC--D-EFGHIJ");
    SequenceI seq2 = new Sequence("Seq2", "ABCDEFGHIJ");
    SequenceI seq3 = new Sequence("Seq3", "ABCDE-FGHIJ");
    SequenceI seq4 = new Sequence("Seq4", "ABCDEFGHIJ");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3, seq4 };
    AlignmentI al = new Alignment(seqs);
    al.setDataset(null);

    /*
     * sort with no score features does nothing
     */
    PA.setValue(AlignmentSorter.class, "sortByFeatureCriteria", null);

    AlignmentSorter.sortByFeature(null, null, 0, al.getWidth(), al,
            AlignmentSorter.FEATURE_SCORE);
    assertSame(al.getSequenceAt(0), seq1);
    assertSame(al.getSequenceAt(1), seq2);
    assertSame(al.getSequenceAt(2), seq3);
    assertSame(al.getSequenceAt(3), seq4);

    /*
     * add score and non-score features
     * seq1 Cath(2.0) Pfam(4.0) average 3.0
     * seq2 Cath(2.5) Metal(NaN) average 2.5
     * seq3 KD(-4), KD(3.0) average -0.5
     * seq4 Helix(NaN) - should sort as if largest score
     */
    seq1.addSequenceFeature(
            new SequenceFeature("Cath", "", 2, 3, 2.0f, "g1"));
    seq1.addSequenceFeature(
            new SequenceFeature("Pfam", "", 4, 5, 4.0f, "g2"));
    seq2.addSequenceFeature(
            new SequenceFeature("Cath", "", 2, 3, 2.5f, "g3"));
    seq2.addSequenceFeature(
            new SequenceFeature("Metal", "", 2, 3, Float.NaN, "g4"));
    seq3.addSequenceFeature(new SequenceFeature("kD", "", 2, 3, -4f, "g5"));
    seq3.addSequenceFeature(
            new SequenceFeature("kD", "", 5, 6, 3.0f, "g6"));
    seq4.addSequenceFeature(
            new SequenceFeature("Helix", "", 2, 3, Float.NaN, "g7"));

    /*
     * sort by ascending score, no filter on feature type or group
     * NB sort order for the same feature set (none) gets toggled, so descending
     */
    PA.setValue(AlignmentSorter.class, "sortByFeatureAscending", true);
    AlignmentSorter.sortByFeature(null, null, 0, al.getWidth(), al,
            AlignmentSorter.FEATURE_SCORE);
    assertSame(al.getSequenceAt(3), seq3); // -0.5
    assertSame(al.getSequenceAt(2), seq2); // 2.5
    assertSame(al.getSequenceAt(1), seq1); // 3.0
    assertSame(al.getSequenceAt(0), seq4); // maximum 'score'

    /*
     * repeat sort toggles order - now ascending
     */
    AlignmentSorter.sortByFeature(null, null, 0, al.getWidth(), al,
            AlignmentSorter.FEATURE_SCORE);
    assertSame(al.getSequenceAt(0), seq3); // -0.5
    assertSame(al.getSequenceAt(1), seq2); // 2.5
    assertSame(al.getSequenceAt(2), seq1); // 3.0
    assertSame(al.getSequenceAt(3), seq4);

    /*
     * specify features, excluding Pfam
     * seq1 average is now 2.0
     * next sort is ascending (not toggled) as for a different feature set
     */
    List<String> types = Arrays.asList(new String[] { "Cath", "kD" });
    AlignmentSorter.sortByFeature(types, null, 0, al.getWidth(), al,
            AlignmentSorter.FEATURE_SCORE);
    assertSame(al.getSequenceAt(0), seq3); // -0.5
    assertSame(al.getSequenceAt(1), seq1); // 2.0
    assertSame(al.getSequenceAt(2), seq2); // 2.5
    assertSame(al.getSequenceAt(3), seq4);

    /*
     * specify groups, excluding g5 (kD -4 score)
     * seq3 average is now 3.0
     * next sort is ascending (not toggled) as for a different group spec
     */
    List<String> groups = Arrays
            .asList(new String[]
            { "g1", "g2", "g3", "g6" });
    AlignmentSorter.sortByFeature(types, groups, 0, al.getWidth(), al,
            AlignmentSorter.FEATURE_SCORE);
    assertSame(al.getSequenceAt(0), seq1); // 2.0
    assertSame(al.getSequenceAt(1), seq2); // 2.5
    assertSame(al.getSequenceAt(2), seq3); // 3.0
    assertSame(al.getSequenceAt(3), seq4);

    /*
     * limit to columns 0-4, excluding 2nd feature of seq1 and seq3
     * seq1 is now 2.0, seq3 is now -4
     */
    // fails because seq1.findPosition(4) returns 4
    // although residue 4 is in column 5! - JAL-2544
    AlignmentSorter.sortByFeature(null, null, 0, 4, al,
            AlignmentSorter.FEATURE_SCORE);
    assertSame(al.getSequenceAt(0), seq3); // -4
    assertSame(al.getSequenceAt(1), seq1); // 2.0
    assertSame(al.getSequenceAt(2), seq2); // 2.5
    assertSame(al.getSequenceAt(3), seq4);
  }

  @Test(groups = "Functional")
  public void testSortByFeature_density()
  {
    // TODO
  }
}
