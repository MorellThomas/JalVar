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

import static org.testng.Assert.assertEquals;

import jalview.bin.Jalview;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.ResidueShader;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.ZappoColourScheme;

import java.awt.Color;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SequenceRendererTest
{
  AlignmentI al;

  AlignViewport av;

  SequenceI seq1;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Jalview.main(
            new String[]
            { "-nonews", "-props", "test/jalview/testProps.jvprops" });
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    seq1 = new Sequence("Seq1", "ABCEEEABCABC");
    SequenceI seq2 = new Sequence("Seq2", "ABCABCABCABC");
    SequenceI seq3 = new Sequence("Seq3", "ABCABCABCABC");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3 };
    al = new Alignment(seqs);
    al.setDataset(null);
    av = new AlignViewport(al);
  }

  @Test(groups = "Functional")
  public void testGetResidueColour_WithGroup()
  {
    SequenceRenderer sr = new SequenceRenderer(av);
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(seq1, false);
    sg.setStartRes(3);
    sg.setEndRes(5);

    ResidueShaderI rs = new ResidueShader();
    rs.setColourScheme(new ZappoColourScheme());
    sg.setGroupColourScheme(rs);

    av.getAlignment().addGroup(sg);

    // outside group residues are white
    assertEquals(Color.white, sr.getResidueColour(seq1, 1, null));

    // within group use Zappo scheme - E = red
    assertEquals(Color.red, sr.getResidueColour(seq1, 3, null));
  }
}
