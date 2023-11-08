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

import static org.testng.Assert.assertEquals;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignViewport;
import jalview.renderer.seqfeatures.FeatureRenderer;
import jalview.schemes.FeatureColour;
import jalview.schemes.ZappoColourScheme;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.OverviewDimensions;
import jalview.viewmodel.OverviewDimensionsShowHidden;
import jalview.viewmodel.ViewportRanges;

import java.awt.Color;

import org.testng.annotations.Test;

public class OverviewRendererTest
{

  @Test
  public void testGetColumnColourFromSequence()
  {
    // gapColour, residueColour, hiddenColour
    OverviewResColourFinder cf = new OverviewResColourFinder(Color.PINK,
            Color.white, Color.green);
    Sequence seq1 = new Sequence("seq1", "PQ-RL-");
    Sequence seq2 = new Sequence("seq2", "FVE");
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });
    AlignmentViewport av = new AlignViewport(al);
    OverviewDimensions od = new OverviewDimensionsShowHidden(
            new ViewportRanges(al), false);
    ResidueShaderI rs = new ResidueShader(new ZappoColourScheme());
    FeatureRenderer fr = new FeatureRenderer(av);
    OverviewRenderer or = new OverviewRenderer(fr, od, al, rs, cf);

    // P is magenta (see ResidueProperties.zappo)
    assertEquals(or.getColumnColourFromSequence(null, seq1, 0),
            Color.magenta.getRGB());
    // Q is green
    assertEquals(or.getColumnColourFromSequence(null, seq1, 1),
            Color.green.getRGB());
    // gap is pink (specified in OverviewResColourFinder constructor above)
    assertEquals(or.getColumnColourFromSequence(null, seq1, 2),
            Color.pink.getRGB());
    // F is orange
    assertEquals(or.getColumnColourFromSequence(null, seq2, 0),
            Color.orange.getRGB());
    // E is red
    assertEquals(or.getColumnColourFromSequence(null, seq2, 2),
            Color.red.getRGB());
    // past end of sequence colour as gap (JAL-2929)
    assertEquals(or.getColumnColourFromSequence(null, seq2, 3),
            Color.pink.getRGB());

    /*
     * now add a feature on seq1
     */
    seq1.addSequenceFeature(
            new SequenceFeature("Pfam", "desc", 1, 4, null));
    fr.findAllFeatures(true);
    av.setShowSequenceFeatures(true);
    fr.setColour("Pfam", new FeatureColour(Color.yellow));
    assertEquals(or.getColumnColourFromSequence(null, seq1, 0),
            Color.yellow.getRGB());

    // don't show sequence features
    av.setShowSequenceFeatures(false);
    assertEquals(or.getColumnColourFromSequence(null, seq1, 0),
            Color.magenta.getRGB());
  }
}
