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

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;
import jalview.schemes.UserColourScheme;
import jalview.schemes.ZappoColourScheme;

import java.awt.Color;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResidueColourFinderTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testGetResidueColour_zappo()
  {
    SequenceI seq = new Sequence("name", "MATVLGSPRAPAFF"); // FER1_MAIZE...
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    final AlignViewport av = new AlignViewport(al);
    ResidueColourFinder rcf = new ResidueColourFinder();
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

    // everything is white if showBoxes is false
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 0, null)); // M
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 2, null)); // T
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 5, null)); // G
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 12, null)); // F
  }

  @Test(groups = { "Functional" })
  public void testGetResidueColour_none()
  {
    SequenceI seq = new Sequence("name", "MA--TVLGSPRAPAFF");
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    final AlignViewport av = new AlignViewport(al);
    ResidueColourFinder rcf = new ResidueColourFinder();

    assertEquals(Color.white, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 0, null));
    assertEquals(Color.white, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 2, null));

    // no change if showBoxes is false
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 0, null));
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 2, null));
  }

  @Test(groups = { "Functional" })
  public void testGetResidueColour_userdef()
  {
    SequenceI seq = new Sequence("name", "MAT--GSPRAPAFF"); // FER1_MAIZE... + a
                                                            // gap
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    final AlignViewport av = new AlignViewport(al);
    ResidueColourFinder rcf = new ResidueColourFinder();

    Color[] newColours = new Color[24];
    for (int i = 0; i < 24; i++)
    {
      newColours[i] = null;
    }

    av.setGlobalColourScheme(new UserColourScheme(newColours));

    // gap colour not specified so gap colour is null
    // this is consistent with previous behaviour, but may not be correct?
    assertEquals(null, rcf.getResidueColour(true, av.getResidueShading(),
            null, seq, 3, null));

    newColours[23] = Color.pink;
    av.setGlobalColourScheme(new UserColourScheme(newColours));

    // gap colour specified as pink
    assertEquals(Color.pink, rcf.getResidueColour(true,
            av.getResidueShading(), null, seq, 3, null));

    // everything is white if showBoxes is false
    newColours[23] = null;
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 3, null));

    newColours[23] = Color.pink;
    av.setGlobalColourScheme(new UserColourScheme(newColours));

    // gap colour specified as pink
    assertEquals(Color.white, rcf.getResidueColour(false,
            av.getResidueShading(), null, seq, 3, null));
  }

  // TODO more tests for getResidueColour covering groups, feature rendering...
}
