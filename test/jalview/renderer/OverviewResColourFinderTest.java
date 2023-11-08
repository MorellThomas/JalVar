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
package jalview.renderer;

import static org.testng.AssertJUnit.assertEquals;

import java.awt.Color;
import java.util.ArrayList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;
import jalview.gui.OverviewCanvas;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.UserColourScheme;
import jalview.schemes.ZappoColourScheme;

public class OverviewResColourFinderTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
  }

  @Test(groups = { "Functional" })
  public void testGetResidueBoxColour_none()
  {
    SequenceI seq = new Sequence("name", "MA--TVLGSPRAPAFF");
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    final AlignViewport av = new AlignViewport(al);
    ResidueColourFinder rcf = new OverviewResColourFinder();

    // gaps are grey, residues white
    assertEquals(Color.white, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 0, null));
    assertEquals(Color.lightGray, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 2, null));

    // unaffected by showBoxes setting
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 0, null));
    assertEquals(Color.lightGray, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 2, null));
  }

  @Test(groups = { "Functional" })
  public void testGetResidueBoxColour_zappo()
  {
    SequenceI seq = new Sequence("name", "MAT--GSPRAPAFF"); // FER1_MAIZE... + a
                                                            // gap
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    final AlignViewport av = new AlignViewport(al);
    ResidueColourFinder rcf = new OverviewResColourFinder();
    av.setGlobalColourScheme(new ZappoColourScheme());

    // @see ResidueProperties.zappo
    assertEquals(Color.pink, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 0, null)); // M
    assertEquals(Color.green, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 2, null)); // T
    assertEquals(Color.magenta, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 5, null)); // G
    assertEquals(Color.orange, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 12, null)); // F

    // gap colour not specified so gaps are lightGray
    assertEquals(Color.lightGray, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 3, null));

    // unaffected by showBoxes setting
    assertEquals(Color.pink, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 0, null)); // M
    assertEquals(Color.green, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 2, null)); // T
    assertEquals(Color.magenta, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 5, null)); // G
    assertEquals(Color.orange, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 12, null)); // F

    // gap colour not specified so gaps are lightGray
    assertEquals(Color.lightGray, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 3, null));

  }

  @Test(groups = { "Functional" })
  public void testGetResidueBoxColour_userdef()
  {
    SequenceI seq = new Sequence("name", "MAT--GSPRAPAFF"); // FER1_MAIZE... + a
                                                            // gap
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    final AlignViewport av = new AlignViewport(al);
    ResidueColourFinder rcf = new OverviewResColourFinder();

    Color[] newColours = new Color[24];
    for (int i = 0; i < 24; i++)
    {
      newColours[i] = null;
    }

    av.setGlobalColourScheme(new UserColourScheme(newColours));

    // gap colour not specified so gaps are lightGray
    assertEquals(Color.lightGray, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 3, null));

    newColours[23] = Color.pink;
    av.setGlobalColourScheme(new UserColourScheme(newColours));

    // gap colour specified as pink
    assertEquals(Color.pink, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 3, null));

    // unaffected by showBoxes setting
    // gap colour not specified so gaps are lightGray
    newColours[23] = null;
    assertEquals(Color.lightGray, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 3, null));

    newColours[23] = Color.pink;
    av.setGlobalColourScheme(new UserColourScheme(newColours));

    // gap colour specified as pink
    assertEquals(Color.pink, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 3, null));
  }

  @Test
  public void testGetResidueBoxColour_group()
  {
    SequenceI seq = new Sequence("name", "MA--TVLGSPRAPAFF");
    AlignmentI al = new Alignment(new SequenceI[] { seq });

    ColourSchemeI cs = new ZappoColourScheme();
    ArrayList<SequenceI> seqlist = new ArrayList<>();
    seqlist.add(seq);
    SequenceGroup sg = new SequenceGroup(seqlist, "testgroup", cs, true,
            true, true, 5, 9);
    al.addGroup(sg);
    SequenceGroup[] groups = new SequenceGroup[1];
    groups[0] = sg;

    final AlignViewport av = new AlignViewport(al);
    ResidueColourFinder rcf = new OverviewResColourFinder();

    // G in group specified as magenta in Zappo
    assertEquals(Color.magenta, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 7, null));

    // Residue outside group coloured white
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 0, null));

    // Gap outside group coloured lightgray
    assertEquals(Color.lightGray, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 2, null));

    // use legacy colouring
    rcf = new OverviewResColourFinder(Color.white, Color.lightGray,
            Color.red);

    // G in group specified as magenta in Zappo
    assertEquals(Color.magenta, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 7, null));

    // Residue outside group coloured lightgray
    assertEquals(Color.lightGray, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 0, null));

    // Gap outside group coloured white
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 2, null));

    // use new colouring
    rcf = new OverviewResColourFinder(Color.blue, Color.white, Color.red);

    // G in group specified as magenta in Zappo
    assertEquals(Color.magenta, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 7, null));

    // Residue outside group coloured white
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 0, null));

    // Gap outside group coloured blue
    assertEquals(Color.blue, rcf.getResidueColour(false,
            av.getResidueShading(), groups, seq, 2, null));
  }

  @Test
  public void testGetBoxColour()
  {
    SequenceI seq = new Sequence("name", "MAT--GSPRAPAFF"); // FER1_MAIZE... + a
                                                            // gap
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    final AlignViewport av = new AlignViewport(al);

    // non-legacy colouring
    ResidueColourFinder rcf = new OverviewResColourFinder();
    ResidueShaderI shader = new ResidueShader();

    // residues white
    Color c = rcf.getBoxColour(shader, seq, 0);
    assertEquals(Color.white, c);

    // gaps gap colour
    c = rcf.getBoxColour(shader, seq, 3);
    assertEquals(OverviewCanvas.OVERVIEW_DEFAULT_GAP, c);

    // legacy colouring set explicitly via constructor
    rcf = new OverviewResColourFinder(Color.white, Color.lightGray,
            Color.red);
    shader = new ResidueShader();

    // residues light gray
    c = rcf.getBoxColour(shader, seq, 0);
    assertEquals(Color.lightGray, c);

    // gaps white
    c = rcf.getBoxColour(shader, seq, 3);
    assertEquals(Color.white, c);

    // legacy colouring off
    rcf = new OverviewResColourFinder();
    shader = new ResidueShader();

    // residues white
    c = rcf.getBoxColour(shader, seq, 0);
    assertEquals(Color.white, c);

    // gaps gap colour
    c = rcf.getBoxColour(shader, seq, 3);
    assertEquals(OverviewCanvas.OVERVIEW_DEFAULT_GAP, c);

    // non legacy colouring with colour scheme
    rcf = new OverviewResColourFinder(Color.blue, Color.white, Color.red);
    shader = new ResidueShader(new ZappoColourScheme());

    // M residue pink
    c = rcf.getBoxColour(shader, seq, 0);
    assertEquals(Color.pink, c);

    // gaps blue
    c = rcf.getBoxColour(shader, seq, 3);
    assertEquals(Color.blue, c);

    // legacy colouring with colour scheme
    rcf = new OverviewResColourFinder(Color.white, Color.lightGray,
            Color.red);

    // M residue pink
    c = rcf.getBoxColour(shader, seq, 0);
    assertEquals(Color.pink, c);

    // gaps white
    c = rcf.getBoxColour(shader, seq, 3);
    assertEquals(Color.white, c);
  }
}
