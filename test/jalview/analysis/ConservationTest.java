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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ConservationTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testRecordConservation()
  {
    Map<String, Integer> resultMap = new HashMap<String, Integer>();

    // V is hydrophobic, aliphatic, small
    Conservation.recordConservation(resultMap, "V");
    assertEquals(resultMap.get("hydrophobic").intValue(), 1);
    assertEquals(resultMap.get("aliphatic").intValue(), 1);
    assertEquals(resultMap.get("small").intValue(), 1);
    assertEquals(resultMap.get("tiny").intValue(), 0);
    assertEquals(resultMap.get("polar").intValue(), 0);
    assertEquals(resultMap.get("charged").intValue(), 0);

    // now add S: not hydrophobic, small, tiny, polar, not aliphatic
    Conservation.recordConservation(resultMap, "s");
    assertEquals(resultMap.get("hydrophobic").intValue(), -1);
    assertEquals(resultMap.get("aliphatic").intValue(), -1);
    assertEquals(resultMap.get("small").intValue(), 1);
    assertEquals(resultMap.get("tiny").intValue(), -1);
    assertEquals(resultMap.get("polar").intValue(), -1);
    assertEquals(resultMap.get("charged").intValue(), 0);
  }

  @Test(groups = "Functional")
  public void testCountConservationAndGaps()
  {
    List<SequenceI> seqs = new ArrayList<SequenceI>();
    seqs.add(new Sequence("seq1", "VGnY")); // not case sensitive
    seqs.add(new Sequence("seq2", "-G-y"));
    seqs.add(new Sequence("seq3", "VG-Y"));
    seqs.add(new Sequence("seq4", "VGNW"));

    Conservation cons = new Conservation("", seqs, 0, 50);
    int[] counts = cons.countConservationAndGaps(0);
    assertEquals(counts[0], 1); // conserved
    assertEquals(counts[1], 1); // gap count
    counts = cons.countConservationAndGaps(1);
    assertEquals(counts[0], 1);
    assertEquals(counts[1], 0);
    counts = cons.countConservationAndGaps(2);
    assertEquals(counts[0], 1);
    assertEquals(counts[1], 2);
    counts = cons.countConservationAndGaps(3);
    assertEquals(counts[0], 0); // not conserved
    assertEquals(counts[1], 0);
  }

  @Test(groups = "Functional")
  public void testCalculate_noThreshold()
  {
    List<SequenceI> seqs = new ArrayList<SequenceI>();
    seqs.add(new Sequence("seq1", "VGIV-N"));
    seqs.add(new Sequence("seq2", "V-iL-N")); // not case sensitive
    seqs.add(new Sequence("seq3", "V-IW-N"));
    seqs.add(new Sequence("seq4", "VGLH-L"));

    Conservation cons = new Conservation("", 0, seqs, 0, 5);
    cons.calculate();

    /*
     * column 0: all V (hydrophobic/aliphatic/small)
     */
    Map<String, Integer> colCons = cons.total[0];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), 1);
    assertEquals(colCons.get("small").intValue(), 1);
    assertEquals(colCons.get("tiny").intValue(), 0);
    assertEquals(colCons.get("proline").intValue(), 0);
    assertEquals(colCons.get("charged").intValue(), 0);
    assertEquals(colCons.get("negative").intValue(), 0);
    assertEquals(colCons.get("polar").intValue(), 0);
    assertEquals(colCons.get("positive").intValue(), 0);
    assertEquals(colCons.get("aromatic").intValue(), 0);

    /*
     * column 1: all G (hydrophobic/small/tiny)
     * gaps take default value of property present
     */
    colCons = cons.total[1];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), -1);
    assertEquals(colCons.get("small").intValue(), 1);
    assertEquals(colCons.get("tiny").intValue(), 1);
    assertEquals(colCons.get("proline").intValue(), -1);
    assertEquals(colCons.get("charged").intValue(), -1);
    assertEquals(colCons.get("negative").intValue(), -1);
    assertEquals(colCons.get("polar").intValue(), -1);
    assertEquals(colCons.get("positive").intValue(), -1);
    assertEquals(colCons.get("aromatic").intValue(), -1);

    /*
     * column 2: I/L (aliphatic/hydrophobic), all others negatively conserved
     */
    colCons = cons.total[2];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), 1);
    assertEquals(colCons.get("small").intValue(), 0);
    assertEquals(colCons.get("tiny").intValue(), 0);
    assertEquals(colCons.get("proline").intValue(), 0);
    assertEquals(colCons.get("charged").intValue(), 0);
    assertEquals(colCons.get("negative").intValue(), 0);
    assertEquals(colCons.get("polar").intValue(), 0);
    assertEquals(colCons.get("positive").intValue(), 0);
    assertEquals(colCons.get("aromatic").intValue(), 0);

    /*
     * column 3: VLWH all hydrophobic, none is tiny, negative or proline
     */
    colCons = cons.total[3];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), -1);
    assertEquals(colCons.get("small").intValue(), -1);
    assertEquals(colCons.get("tiny").intValue(), 0);
    assertEquals(colCons.get("proline").intValue(), 0);
    assertEquals(colCons.get("charged").intValue(), -1);
    assertEquals(colCons.get("negative").intValue(), 0);
    assertEquals(colCons.get("polar").intValue(), -1);
    assertEquals(colCons.get("positive").intValue(), -1);
    assertEquals(colCons.get("aromatic").intValue(), -1);

    /*
     * column 4: all gaps - counted as having all properties
     */
    colCons = cons.total[4];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), 1);
    assertEquals(colCons.get("small").intValue(), 1);
    assertEquals(colCons.get("tiny").intValue(), 1);
    assertEquals(colCons.get("proline").intValue(), 1);
    assertEquals(colCons.get("charged").intValue(), 1);
    assertEquals(colCons.get("negative").intValue(), 1);
    assertEquals(colCons.get("polar").intValue(), 1);
    assertEquals(colCons.get("positive").intValue(), 1);
    assertEquals(colCons.get("aromatic").intValue(), 1);

    /*
     * column 5: N (small polar) and L (aliphatic hydrophobic) 
     * have nothing in common!
     */
    colCons = cons.total[5];
    assertEquals(colCons.get("hydrophobic").intValue(), -1);
    assertEquals(colCons.get("aliphatic").intValue(), -1);
    assertEquals(colCons.get("small").intValue(), -1);
    assertEquals(colCons.get("tiny").intValue(), 0);
    assertEquals(colCons.get("proline").intValue(), 0);
    assertEquals(colCons.get("charged").intValue(), 0);
    assertEquals(colCons.get("negative").intValue(), 0);
    assertEquals(colCons.get("polar").intValue(), -1);
    assertEquals(colCons.get("positive").intValue(), 0);
    assertEquals(colCons.get("aromatic").intValue(), 0);
  }

  /**
   * Test for the case whether the number of non-gapped sequences in a column
   * has to be above a threshold
   */
  @Test(groups = "Functional")
  public void testCalculate_threshold()
  {
    List<SequenceI> seqs = new ArrayList<SequenceI>();
    seqs.add(new Sequence("seq1", "VGIV-"));
    seqs.add(new Sequence("seq2", "V-iL-")); // not case sensitive
    seqs.add(new Sequence("seq3", "V-IW-"));
    seqs.add(new Sequence("seq4", "VGLH-"));
    seqs.add(new Sequence("seq5", "VGLH-"));

    /*
     * threshold 50% means a residue has to occur 3 or more times
     * in a column to be counted for conservation
     */
    // TODO: ConservationThread uses a value of 3
    // calculateConservation states it is the minimum number of sequences
    // but it is treated as percentage threshold in calculate() ?
    Conservation cons = new Conservation("", 50, seqs, 0, 4);
    cons.calculate();

    /*
     * column 0: all V (hydrophobic/aliphatic/small)
     */
    Map<String, Integer> colCons = cons.total[0];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), 1);
    assertEquals(colCons.get("small").intValue(), 1);
    assertEquals(colCons.get("tiny").intValue(), 0);
    assertEquals(colCons.get("proline").intValue(), 0);
    assertEquals(colCons.get("charged").intValue(), 0);
    assertEquals(colCons.get("negative").intValue(), 0);
    assertEquals(colCons.get("polar").intValue(), 0);
    assertEquals(colCons.get("positive").intValue(), 0);
    assertEquals(colCons.get("aromatic").intValue(), 0);

    /*
     * column 1: all G (hydrophobic/small/tiny)
     * gaps are ignored as not above threshold
     */
    colCons = cons.total[1];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), 0);
    assertEquals(colCons.get("small").intValue(), 1);
    assertEquals(colCons.get("tiny").intValue(), 1);
    assertEquals(colCons.get("proline").intValue(), 0);
    assertEquals(colCons.get("charged").intValue(), 0);
    assertEquals(colCons.get("negative").intValue(), 0);
    assertEquals(colCons.get("polar").intValue(), 0);
    assertEquals(colCons.get("positive").intValue(), 0);
    assertEquals(colCons.get("aromatic").intValue(), 0);

    /*
     * column 2: I/L (aliphatic/hydrophobic), all others negatively conserved
     */
    colCons = cons.total[2];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), 1);
    assertEquals(colCons.get("small").intValue(), 0);
    assertEquals(colCons.get("tiny").intValue(), 0);
    assertEquals(colCons.get("proline").intValue(), 0);
    assertEquals(colCons.get("charged").intValue(), 0);
    assertEquals(colCons.get("negative").intValue(), 0);
    assertEquals(colCons.get("polar").intValue(), 0);
    assertEquals(colCons.get("positive").intValue(), 0);
    assertEquals(colCons.get("aromatic").intValue(), 0);

    /*
     * column 3: nothing above threshold
     */
    colCons = cons.total[3];
    assertTrue(colCons.isEmpty());

    /*
     * column 4: all gaps - counted as having all properties
     */
    colCons = cons.total[4];
    assertEquals(colCons.get("hydrophobic").intValue(), 1);
    assertEquals(colCons.get("aliphatic").intValue(), 1);
    assertEquals(colCons.get("small").intValue(), 1);
    assertEquals(colCons.get("tiny").intValue(), 1);
    assertEquals(colCons.get("proline").intValue(), 1);
    assertEquals(colCons.get("charged").intValue(), 1);
    assertEquals(colCons.get("negative").intValue(), 1);
    assertEquals(colCons.get("polar").intValue(), 1);
    assertEquals(colCons.get("positive").intValue(), 1);
    assertEquals(colCons.get("aromatic").intValue(), 1);
  }

  /**
   * Test the method that derives the conservation 'sequence' and the mouseover
   * tooltips from the computed conservation
   */
  @Test(groups = "Functional")
  public void testVerdict()
  {
    List<SequenceI> seqs = new ArrayList<SequenceI>();
    seqs.add(new Sequence("seq1", "VGIVV-H"));
    seqs.add(new Sequence("seq2", "VGILL-H"));
    seqs.add(new Sequence("seq3", "VGIW--R"));
    seqs.add(new Sequence("seq4", "VGLHH--"));
    seqs.add(new Sequence("seq5", "VGLHH-R"));
    seqs.add(new Sequence("seq6", "VGLHH--"));
    seqs.add(new Sequence("seq7", "VGLHH-R"));
    seqs.add(new Sequence("seq8", "VGLHH-R"));

    // calculate with no threshold
    Conservation cons = new Conservation("", 0, seqs, 0, 6);
    cons.calculate();
    // positive and negative conservation where <25% gaps in columns
    cons.verdict(false, 25);

    /*
     * verify conservation 'sequence'
     * cols 0 fully conserved and above threshold (*)
     * col 2 properties fully conserved (+)
     * col 3 VLWH 1 positively and 3 negatively conserved properties
     * col 4 has 1 positively conserved property, but because gap contributes a
     * 'positive' for all properties, no negative conservation is counted
     * col 5 is all gaps
     * col 6 has 25% gaps so fails threshold test
     */
    assertEquals(cons.getConsSequence().getSequenceAsString(), "**+41--");

    /*
     * verify tooltips; conserved properties are sorted alphabetically within
     * positive followed by negative
     */
    assertEquals(cons.getTooltip(0),
            "aliphatic hydrophobic small !aromatic !charged !negative !polar !positive !proline !tiny");
    assertEquals(cons.getTooltip(1),
            "hydrophobic small tiny !aliphatic !aromatic !charged !negative !polar !positive !proline");
    assertEquals(cons.getTooltip(2),
            "aliphatic hydrophobic !aromatic !charged !negative !polar !positive !proline !small !tiny");
    assertEquals(cons.getTooltip(3),
            "hydrophobic !negative !proline !tiny");
    assertEquals(cons.getTooltip(4), "hydrophobic");
    assertEquals(cons.getTooltip(5), "");
    assertEquals(cons.getTooltip(6), "");
  }
}
