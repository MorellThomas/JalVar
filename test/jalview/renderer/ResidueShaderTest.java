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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.analysis.Conservation;
import jalview.datamodel.Profile;
import jalview.datamodel.ProfileI;
import jalview.datamodel.Profiles;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.ResidueCount;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.ResidueProperties;
import jalview.schemes.UserColourScheme;
import jalview.schemes.ZappoColourScheme;

import java.awt.Color;
import java.util.Collections;

import org.testng.annotations.Test;

public class ResidueShaderTest
{

  @Test(groups = "Functional")
  public void testAboveThreshold()
  {
    /*
     * make up profiles for this alignment:
     * AR-Q
     * AR--
     * SR-T
     * SR-T
     */
    ProfileI[] profiles = new ProfileI[4];
    profiles[0] = new Profile(4, 0, 2, "AS");
    profiles[1] = new Profile(4, 0, 4, "R");
    profiles[2] = new Profile(4, 4, 0, "");
    profiles[3] = new Profile(4, 1, 2, "T");
    ResidueShader ccs = new ResidueShader(new PIDColourScheme());
    ccs.setConsensus(new Profiles(profiles));

    /*
     * no threshold
     */
    ccs.setThreshold(0, true);
    assertTrue(ccs.aboveThreshold('a', 0));
    assertTrue(ccs.aboveThreshold('S', 0));
    assertTrue(ccs.aboveThreshold('W', 0));
    assertTrue(ccs.aboveThreshold('R', 1));
    assertTrue(ccs.aboveThreshold('W', 2));
    assertTrue(ccs.aboveThreshold('t', 3));
    assertTrue(ccs.aboveThreshold('Q', 3));

    /*
     * with threshold, include gaps
     */
    ccs.setThreshold(60, false);
    assertFalse(ccs.aboveThreshold('a', 0));
    assertFalse(ccs.aboveThreshold('S', 0));
    assertTrue(ccs.aboveThreshold('R', 1));
    assertFalse(ccs.aboveThreshold('W', 2));
    assertFalse(ccs.aboveThreshold('t', 3)); // 50% < 60%

    /*
     * with threshold, ignore gaps
     */
    ccs.setThreshold(60, true);
    assertFalse(ccs.aboveThreshold('a', 0));
    assertFalse(ccs.aboveThreshold('S', 0));
    assertTrue(ccs.aboveThreshold('R', 1));
    assertFalse(ccs.aboveThreshold('W', 2));
    assertTrue(ccs.aboveThreshold('t', 3)); // 67% > 60%
  }

  /**
   * Test colour bleaching based on conservation score and conservation slider.
   * Scores of 10 or 11 should leave colours unchanged. Gap is always white.
   */
  @Test(groups = "Functional")
  public void testApplyConservation()
  {
    ResidueShader ccs = new ResidueShader(new PIDColourScheme());

    // no conservation present - no fading
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 12));

    /*
     * stub Conservation to return a given consensus string
     */
    final String consSequence = "0123456789+*-";
    Conservation cons = new Conservation(null,
            Collections.<SequenceI> emptyList(), 0, 0)
    {
      @Override
      public SequenceI getConsSequence()
      {
        return new Sequence("seq", consSequence);
      }
    };
    ccs.setConservation(cons);

    // column out of range:
    assertEquals(Color.RED,
            ccs.applyConservation(Color.RED, consSequence.length()));

    /*
     * with 100% threshold, 'fade factor' is 
     * (11-score)/10 * 100/20 = (11-score)/2
     * which is >= 1 for all scores i.e. all fade to white except +, *
     */
    ccs.setConservationInc(100);
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 0));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 1));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 2));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 3));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 4));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 5));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 6));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 7));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 8));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 9));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 10));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 11));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 12));

    /*
     * with 0% threshold, there should be no fading
     */
    ccs.setConservationInc(0);
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 0));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 1));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 2));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 3));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 4));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 5));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 6));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 7));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 8));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 9));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 10));
    assertEquals(Color.RED, ccs.applyConservation(Color.RED, 11));
    assertEquals(Color.WHITE, ccs.applyConservation(Color.RED, 12)); // gap

    /*
     * with 40% threshold, 'fade factor' is 
     * (11-score)/10 * 40/20 = (11-score)/5
     * which is {>1, >1, >1, >1, >1, >1, 1, 0.8, 0.6, 0.4} for score 0-9
     * e.g. score 7 colour fades 80% of the way to white (255, 255, 255)
     */
    ccs.setConservationInc(40);
    Color colour = new Color(155, 105, 55);
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 0));
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 1));
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 2));
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 3));
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 4));
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 5));
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 6));
    assertEquals(new Color(235, 225, 215),
            ccs.applyConservation(colour, 7));
    assertEquals(new Color(215, 195, 175),
            ccs.applyConservation(colour, 8));
    assertEquals(new Color(195, 165, 135),
            ccs.applyConservation(colour, 9));
    assertEquals(colour, ccs.applyConservation(colour, 10));
    assertEquals(colour, ccs.applyConservation(colour, 11));
    assertEquals(Color.WHITE, ccs.applyConservation(colour, 12));
  }

  @Test(groups = "Functional")
  public void testFindColour_gapColour()
  {
    /*
     * normally, a gap is coloured white
     */
    ResidueShader rs = new ResidueShader(new ZappoColourScheme());
    assertEquals(Color.white, rs.findColour(' ', 7, null));

    /*
     * a User Colour Scheme may specify a bespoke gap colour
     */
    Color[] colours = new Color[ResidueProperties.maxProteinIndex + 1];
    colours[5] = Color.blue; // Q colour
    colours[23] = Color.red; // gap colour
    ColourSchemeI cs = new UserColourScheme(colours);
    rs = new ResidueShader(cs);

    assertEquals(Color.red, rs.findColour(' ', 7, null));
    assertEquals(Color.blue, rs.findColour('Q', 7, null));

    /*
     * stub Conservation to return a given consensus string
     */
    final String consSequence = "0123456789+*-";
    Conservation cons = new Conservation(null,
            Collections.<SequenceI> emptyList(), 0, 0)
    {
      @Override
      public SequenceI getConsSequence()
      {
        return new Sequence("seq", consSequence);
      }
    };
    rs.setConservation(cons);

    /*
     * with 0% threshold, there should be no fading
     */
    rs.setConservationInc(0);
    assertEquals(Color.red, rs.findColour(' ', 7, null));
    assertEquals(Color.blue, rs.findColour('Q', 7, null));

    /*
     * with 40% threshold, 'fade factor' is 
     * (11-score)/10 * 40/20 = (11-score)/5
     * so position 7, score 7 fades 80% of the way to white (255, 255, 255)
     */
    rs.setConservationInc(40);

    /*
     * gap colour is unchanged for Conservation
     */
    assertEquals(Color.red, rs.findColour(' ', 7, null));
    assertEquals(Color.red, rs.findColour('-', 7, null));
    assertEquals(Color.red, rs.findColour('.', 7, null));

    /*
     * residue colour is faded 80% of the way from
     * blue(0, 0, 255) to white(255, 255, 255)
     * making (204, 204, 255)
     */
    assertEquals(new Color(204, 204, 255), rs.findColour('Q', 7, null));

    /*
     * turn off By Conservation, apply Above Identity Threshold
     * providing a stub Consensus that has modal residue "Q" with pid 60%
     */
    rs.setConservationApplied(false);
    ProfilesI consensus = getStubConsensus("Q", 60f);
    rs.setConsensus(consensus);

    // with consensus pid (60) above threshold (50), colours are unchanged
    rs.setThreshold(50, false);
    assertEquals(Color.blue, rs.findColour('Q', 7, null));
    assertEquals(Color.red, rs.findColour('-', 7, null));

    // with consensus pid (60) below threshold (70),
    // residue colour becomes white, gap colour is unchanged
    rs.setThreshold(70, false);
    assertEquals(Color.white, rs.findColour('Q', 7, null));
    assertEquals(Color.red, rs.findColour('-', 7, null));
  }

  /**
   * @param modalResidue
   * @param pid
   * @return
   */
  protected ProfilesI getStubConsensus(final String modalResidue,
          final float pid)
  {
    ProfilesI consensus = new ProfilesI()
    {

      @Override
      public ProfileI get(int i)
      {
        return new ProfileI()
        {
          @Override
          public void setCounts(ResidueCount residueCounts)
          {
          }

          @Override
          public float getPercentageIdentity(boolean ignoreGaps)
          {
            return pid;
          }
          
          @Override
          public float getPercentageIdentity(int count, boolean ignoreGaps)
          {
            return pid;
          }

          @Override
          public ResidueCount getCounts()
          {
            return null;
          }

          @Override
          public int getHeight()
          {
            return 0;
          }

          @Override
          public int getGapped()
          {
            return 0;
          }

          @Override
          public int getMaxCount()
          {
            return 0;
          }

          @Override
          public String getModalResidue()
          {
            return modalResidue;
          }

          @Override
          public int getNonGapped()
          {
            return 0;
          }
          
          @Override
          public String getOccuranceFraction(int count, boolean ignoreGaps)
          {
            return null;
          }
          
          @Override
          public void setIndividCounts(int[] ic)
          {
          }
          
          @Override
          public int[] getIndividCounts()
          {
            return null;
          }
          
        };
      }

      @Override
      public int getStartColumn()
      {
        return 0;
      }

      @Override
      public int getEndColumn()
      {
        return 0;
      }

    };
    return consensus;
  }
}
