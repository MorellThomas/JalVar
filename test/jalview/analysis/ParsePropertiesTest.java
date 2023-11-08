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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ParsePropertiesTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private Alignment al;

  private ParseProperties pp;

  /**
   * Construct an alignment with 4 sequences with varying description format
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    SequenceI[] seqs = new SequenceI[] {
        new Sequence("sq1", "THISISAPLACEHOLDER"),
        new Sequence("sq2", "THISISAPLACEHOLDER"),
        new Sequence("sq3", "THISISAPLACEHOLDER"),
        new Sequence("sq4", "THISISAPLACEHOLDER") };
    seqs[0].setDescription("1 mydescription1");
    seqs[1].setDescription("mydescription2");
    seqs[2].setDescription("2. 0.1 mydescription+3");
    seqs[3].setDescription("3 0.01 mydescription4");
    al = new Alignment(seqs);

    pp = new ParseProperties(al);

  }

  /**
   * Test with a description pattern that matches any string ending in one or
   * more 'number characters' (0-9+.), i.e. greedily matches any trailing
   * numeric part of the string
   */
  @Test(groups = { "Functional" })
  public void testGetScoresFromDescription()
  {
    String regex = ".*([-0-9.+]+)";
    final int count = pp.getScoresFromDescription("my Score",
            "my Score Description", regex, true);
    System.out.println("Matched " + count + " for " + regex);
    assertEquals(4, count);

    /*
     * Verify values 1/2/3/4 have been parsed from sequence descriptions
     */
    AlignmentAnnotation[] anns = al.getSequenceAt(0).getAnnotation();
    assertEquals(1, anns.length);
    assertEquals(1d, anns[0].getScore(), 0.001d);
    assertEquals("my Score Description", anns[0].description);
    assertEquals("my Score", anns[0].label);
    anns = al.getSequenceAt(1).getAnnotation();
    assertEquals(1, anns.length);
    assertEquals(2d, anns[0].getScore(), 0.001d);
    assertEquals("my Score Description", anns[0].description);
    assertEquals("my Score", anns[0].label);
    anns = al.getSequenceAt(2).getAnnotation();
    assertEquals(1, anns.length);
    assertEquals(3d, anns[0].getScore(), 0.001d);
    anns = al.getSequenceAt(3).getAnnotation();
    assertEquals(1, anns.length);
    assertEquals(4d, anns[0].getScore(), 0.001d);
  }

  /**
   * Test with a description pattern that matches any string (or none), followed
   * by a 'number character' (0-9+.), followed by at least one separator
   * character, followed by at least one 'number character', then any trailing
   * characters.
   */
  @Test(groups = { "Functional" })
  public void testGetScoresFromDescription_twoScores()
  {
    String regex = ".*([-0-9.+]+).+([-0-9.+]+).*";
    final int count = pp.getScoresFromDescription("my Score",
            "my Score Description", regex, true);
    System.out.println("Matched " + count + " for " + regex);
    assertEquals(3, count);

    /*
     * Seq1 has two score values parsed out
     */
    AlignmentAnnotation[] anns = al.getSequenceAt(0).getAnnotation();
    assertEquals(2, anns.length);
    assertEquals(1d, anns[0].getScore(), 0.001d);
    assertEquals("my Score Description", anns[0].description);
    assertEquals("my Score", anns[0].label);
    assertEquals(1d, anns[1].getScore(), 0.001d);
    assertEquals("my Score Description (column 1)", anns[1].description);
    assertEquals("my Score_1", anns[1].label);

    /*
     * Seq2 has no score parsed out (is this right?)
     */
    assertNull(al.getSequenceAt(1).getAnnotation());

    /*
     * Seq3 has two score values parsed out
     */
    // TODO parsed values (1.0 and 3.0) look wrong v description
    // would expect 2.0 and 0.1
    // undesired 'greedy' behaviour of regex?
    anns = al.getSequenceAt(2).getAnnotation();
    assertEquals(2, anns.length);
    assertEquals(1d, anns[0].getScore(), 0.001d);
    assertEquals("my Score Description", anns[0].description);
    assertEquals("my Score", anns[0].label);
    assertEquals(3d, anns[1].getScore(), 0.001d);
    assertEquals("my Score Description (column 1)", anns[1].description);
    assertEquals("my Score_1", anns[1].label);

    /*
     * Seq3 has two score values parsed out
     */
    // TODO parsed values (1.0 and 4.0) look wrong v description
    // would expect 3 and 0.01
    anns = al.getSequenceAt(3).getAnnotation();
    assertEquals(2, anns.length);
    assertEquals(1d, anns[0].getScore(), 0.001d);
    assertEquals("my Score Description", anns[0].description);
    assertEquals("my Score", anns[0].label);
    assertEquals(4d, anns[1].getScore(), 0.001d);
    assertEquals("my Score Description (column 1)", anns[1].description);
    assertEquals("my Score_1", anns[1].label);
  }

  /**
   * Test with a regex that looks for numbers separated by words - as currently
   * used in Jalview (May 2015)
   * 
   * @see AlignFrame.extractScores_actionPerformed
   */
  @Test(groups = { "Functional" })
  public void testGetScoresFromDescription_wordBoundaries()
  {
    String regex = "\\W*([-+eE0-9.]+)";
    List<SequenceI> seqs = al.getSequences();
    seqs.get(0).setDescription("Ferredoxin");
    seqs.get(1).setDescription(" Ferredoxin-1, chloroplast precursor");
    seqs.get(2).setDescription("GH28E30p");
    seqs.get(3).setDescription("At1g10960/T19D16_12");
    final int count = pp.getScoresFromDescription("description column",
            "score in description column ", regex, true);
    assertEquals(3, count);

    /*
     * No score parsable from seq1 description
     */
    AlignmentAnnotation[] anns = al.getSequenceAt(0).getAnnotation();
    assertNull(anns);

    /*
     * Seq2 description has a '1' in it
     */
    anns = al.getSequenceAt(1).getAnnotation();
    assertEquals(1, anns.length);
    assertEquals(1d, anns[0].getScore(), 0.001d);

    /*
     * Seq3 description has '28E30' in it
     * 
     * Note: 1.8E308 or larger would result in 'Infinity'
     */
    anns = al.getSequenceAt(2).getAnnotation();
    assertEquals(1, anns.length);
    assertEquals(2.8E31d, anns[0].getScore(), 0.001d);

    /*
     * Seq4 description has several numbers in it
     */
    anns = al.getSequenceAt(3).getAnnotation();
    assertEquals(5, anns.length);
    assertEquals(1d, anns[0].getScore(), 0.001d);
    assertEquals(10960d, anns[1].getScore(), 0.001d);
    assertEquals(19d, anns[2].getScore(), 0.001d);
    assertEquals(16d, anns[3].getScore(), 0.001d);
    assertEquals(12d, anns[4].getScore(), 0.001d);
  }
}
