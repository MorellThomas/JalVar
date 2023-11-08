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
package jalview.analysis.scoremodels;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.math.MatrixI;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FeatureDistanceModelTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  public static String alntestFile = "FER1_MESCR/72-76 DVYIL\nFER1_SPIOL/71-75 DVYIL\nFER3_RAPSA/21-25 DVYVL\nFER1_MAIZE/73-77 DVYIL\n";

  int[] sf1 = new int[] { 74, 74, 73, 73, 23, 23, -1, -1 };

  int[] sf2 = new int[] { -1, -1, 74, 75, -1, -1, 76, 77 };

  int[] sf3 = new int[] { -1, -1, -1, -1, -1, -1, 76, 77 };

  /**
   * <pre>
   * Load test alignment and add features to sequences: 
   *      FER1_MESCR FER1_SPIOL FER3_RAPSA FER1_MAIZE 
   *  sf1     X          X          X  
   *  sf2                X                     X 
   *  sf3                                      X
   * </pre>
   * 
   * @return
   */
  public AlignFrame getTestAlignmentFrame()
  {
    AlignFrame alf = new FileLoader(false)
            .LoadFileWaitTillLoaded(alntestFile, DataSourceType.PASTE);
    AlignmentI al = alf.getViewport().getAlignment();
    Assert.assertEquals(al.getHeight(), 4);
    Assert.assertEquals(al.getWidth(), 5);
    for (int i = 0; i < 4; i++)
    {
      SequenceI ds = al.getSequenceAt(i).getDatasetSequence();
      if (sf1[i * 2] > 0)
      {
        ds.addSequenceFeature(new SequenceFeature("sf1", "sf1", sf1[i * 2],
                sf1[i * 2 + 1], "sf1"));
      }
      if (sf2[i * 2] > 0)
      {
        ds.addSequenceFeature(new SequenceFeature("sf2", "sf2", sf2[i * 2],
                sf2[i * 2 + 1], "sf2"));
      }
      if (sf3[i * 2] > 0)
      {
        ds.addSequenceFeature(new SequenceFeature("sf3", "sf3", sf3[i * 2],
                sf3[i * 2 + 1], "sf3"));
      }
    }
    alf.setShowSeqFeatures(true);
    alf.getFeatureRenderer().setVisible("sf1");
    alf.getFeatureRenderer().setVisible("sf2");
    alf.getFeatureRenderer().setVisible("sf3");
    alf.getFeatureRenderer().findAllFeatures(true);
    Assert.assertEquals(
            alf.getFeatureRenderer().getDisplayedFeatureTypes().size(), 3,
            "Number of feature types");
    assertTrue(alf.getCurrentView().areFeaturesDisplayed());
    return alf;
  }

  @Test(groups = { "Functional" })
  public void testFeatureScoreModel() throws Exception
  {
    AlignFrame alf = getTestAlignmentFrame();
    ScoreModelI sm = new FeatureDistanceModel();
    sm = ScoreModels.getInstance().getScoreModel(sm.getName(),
            alf.getCurrentView().getAlignPanel());
    alf.selectAllSequenceMenuItem_actionPerformed(null);

    MatrixI dm = sm.findDistances(alf.getViewport().getAlignmentView(true),
            SimilarityParams.Jalview);
    assertEquals(dm.getValue(0, 2), 0d,
            "FER1_MESCR (0) should be identical with RAPSA (2)");
    assertTrue(dm.getValue(0, 1) > dm.getValue(0, 2),
            "FER1_MESCR (0) should be further from SPIOL (1) than it is from RAPSA (2)");
  }

  @Test(groups = { "Functional" })
  public void testFeatureScoreModel_hiddenFirstColumn() throws Exception
  {
    AlignFrame alf = getTestAlignmentFrame();
    // hiding first two columns shouldn't affect the tree
    alf.getViewport().hideColumns(0, 1);
    ScoreModelI sm = new FeatureDistanceModel();
    sm = ScoreModels.getInstance().getScoreModel(sm.getName(),
            alf.getCurrentView().getAlignPanel());
    alf.selectAllSequenceMenuItem_actionPerformed(null);
    MatrixI dm = sm.findDistances(alf.getViewport().getAlignmentView(true),
            SimilarityParams.Jalview);
    assertEquals(dm.getValue(0, 2), 0d,
            "FER1_MESCR (0) should be identical with RAPSA (2)");
    assertTrue(dm.getValue(0, 1) > dm.getValue(0, 2),
            "FER1_MESCR (0) should be further from SPIOL (1) than it is from RAPSA (2)");
  }

  @Test(groups = { "Functional" })
  public void testFeatureScoreModel_HiddenColumns() throws Exception
  {
    AlignFrame alf = getTestAlignmentFrame();
    // hide columns and check tree changes
    alf.getViewport().hideColumns(3, 4);
    alf.getViewport().hideColumns(0, 1);
    // getName() can become static in Java 8
    ScoreModelI sm = new FeatureDistanceModel();
    sm = ScoreModels.getInstance().getScoreModel(sm.getName(),
            alf.getCurrentView().getAlignPanel());
    alf.selectAllSequenceMenuItem_actionPerformed(null);
    MatrixI dm = sm.findDistances(alf.getViewport().getAlignmentView(true),
            SimilarityParams.Jalview);
    assertEquals(dm.getValue(0, 2), 0d,
            "After hiding last two columns FER1_MESCR (0) should still be identical with RAPSA (2)");
    assertEquals(dm.getValue(0, 1), 0d,
            "After hiding last two columns FER1_MESCR (0) should now also be identical with SPIOL (1)");
    for (int s = 0; s < 3; s++)
    {
      assertTrue(dm.getValue(s, 3) > 0d,
              "After hiding last two columns "
                      + alf.getViewport().getAlignment().getSequenceAt(s)
                              .getName()
                      + "(" + s
                      + ") should still be distinct from FER1_MAIZE (3)");
    }
  }

  /**
   * Check findFeatureAt doesn't return contact features except at contact
   * points TODO:move to under the FeatureRendererModel test suite
   */
  @Test(groups = { "Functional" })
  public void testFindFeatureAt_PointFeature() throws Exception
  {
    String alignment = "a CCCCCCGGGGGGCCCCCC\n" + "b CCCCCCGGGGGGCCCCCC\n"
            + "c CCCCCCGGGGGGCCCCCC\n";
    AlignFrame af = new jalview.io.FileLoader(false)
            .LoadFileWaitTillLoaded(alignment, DataSourceType.PASTE);
    SequenceI aseq = af.getViewport().getAlignment().getSequenceAt(0);
    SequenceFeature sf = null;
    sf = new SequenceFeature("disulphide bond", "", 2, 5, Float.NaN, "");
    aseq.addSequenceFeature(sf);
    assertTrue(sf.isContactFeature());
    af.refreshFeatureUI(true);
    af.getFeatureRenderer().setAllVisible(Arrays.asList("disulphide bond"));
    Assert.assertEquals(
            af.getFeatureRenderer().getDisplayedFeatureTypes().size(), 1,
            "Should be just one feature type displayed");
    // step through and check for pointwise feature presence/absence
    Assert.assertEquals(
            af.getFeatureRenderer().findFeaturesAtColumn(aseq, 1).size(),
            0);
    // step through and check for pointwise feature presence/absence
    Assert.assertEquals(
            af.getFeatureRenderer().findFeaturesAtColumn(aseq, 2).size(),
            1);
    // step through and check for pointwise feature presence/absence
    Assert.assertEquals(
            af.getFeatureRenderer().findFeaturesAtColumn(aseq, 3).size(),
            0);
    // step through and check for pointwise feature presence/absence
    Assert.assertEquals(
            af.getFeatureRenderer().findFeaturesAtColumn(aseq, 4).size(),
            0);
    // step through and check for pointwise feature presence/absence
    Assert.assertEquals(
            af.getFeatureRenderer().findFeaturesAtColumn(aseq, 5).size(),
            1);
    // step through and check for pointwise feature presence/absence
    Assert.assertEquals(
            af.getFeatureRenderer().findFeaturesAtColumn(aseq, 6).size(),
            0);
  }

  @Test(groups = { "Functional" })
  public void testFindDistances() throws Exception
  {
    String seqs = ">s1\nABCDE\n>seq2\nABCDE\n";
    AlignFrame alf = new FileLoader().LoadFileWaitTillLoaded(seqs,
            DataSourceType.PASTE);
    SequenceI s1 = alf.getViewport().getAlignment().getSequenceAt(0);
    SequenceI s2 = alf.getViewport().getAlignment().getSequenceAt(1);

    /*
     * set domain and variant features thus:
     *     ----5
     *  s1 ddd..
     *  s1 .vvv.
     *  s1 ..vvv    
     *  s2 .ddd. 
     *  s2 vv..v
     *  The number of unshared feature types per column is
     *     20120 (two features of the same type doesn't affect score)
     *  giving an average (pairwise distance) of 5/5 or 1.0 
     */
    s1.addSequenceFeature(
            new SequenceFeature("domain", null, 1, 3, 0f, null));
    s1.addSequenceFeature(
            new SequenceFeature("variant", null, 2, 4, 0f, null));
    s1.addSequenceFeature(
            new SequenceFeature("variant", null, 3, 5, 0f, null));
    s2.addSequenceFeature(
            new SequenceFeature("domain", null, 2, 4, 0f, null));
    s2.addSequenceFeature(
            new SequenceFeature("variant", null, 1, 2, 0f, null));
    s2.addSequenceFeature(
            new SequenceFeature("variant", null, 5, 5, 0f, null));
    alf.setShowSeqFeatures(true);
    alf.getFeatureRenderer().findAllFeatures(true);

    ScoreModelI sm = new FeatureDistanceModel();
    sm = ScoreModels.getInstance().getScoreModel(sm.getName(),
            alf.getCurrentView().getAlignPanel());
    alf.selectAllSequenceMenuItem_actionPerformed(null);

    AlignmentView alignmentView = alf.getViewport().getAlignmentView(true);
    MatrixI distances = sm.findDistances(alignmentView,
            SimilarityParams.Jalview);
    assertEquals(distances.width(), 2);
    assertEquals(distances.height(), 2);
    assertEquals(distances.getValue(0, 0), 0d);
    assertEquals(distances.getValue(1, 1), 0d);

    assertEquals(distances.getValue(0, 1), 1d,
            "expected identical pairs. (check normalisation for similarity score)");
    assertEquals(distances.getValue(1, 0), 1d);
  }

  /**
   * Verify computed distances with varying parameter options
   */
  @Test(groups = "Functional")
  public void testFindDistances_withParams()
  {
    AlignFrame af = setupAlignmentView();
    AlignViewport viewport = af.getViewport();
    AlignmentView view = viewport.getAlignmentView(false);

    ScoreModelI sm = new FeatureDistanceModel();
    sm = ScoreModels.getInstance().getScoreModel(sm.getName(),
            af.alignPanel);

    /*
     * feature distance model always normalises by region width
     * gap-gap is always included (but scores zero)
     * the only variable parameter is 'includeGaps'
     */

    /*
     * include gaps
     * score = 3 + 3 + 0 + 2 + 3 + 2 = 13/6
     */
    SimilarityParamsI params = new SimilarityParams(true, true, true, true);
    MatrixI distances = sm.findDistances(view, params);
    assertEquals(distances.getValue(0, 0), 0d);
    assertEquals(distances.getValue(1, 1), 0d);
    assertEquals(distances.getValue(0, 1), 13d / 6); // should be 13d/6
    assertEquals(distances.getValue(1, 0), 13d / 6);

    /*
     * exclude gaps
     * score = 3 + 3 + 0 + 0 + 0 + 0 = 6/6
     */
    params = new SimilarityParams(true, true, false, true);
    distances = sm.findDistances(view, params);
    assertEquals(distances.getValue(0, 1), 6d / 6);// should be 6d/6
  }

  /**
   * <pre>
   * Set up
   *   column      1 2 3 4 5 6
   *        seq s1 F R - K - S
   *        seq s2 F S - - L
   *   s1 chain    c c   c   c
   *   s1 domain   d d   d   d
   *   s2 chain    c c     c
   *   s2 metal    m m     m
   *   s2 Pfam     P P     P
   *      scores:  3 3 0 2 3 2
   * </pre>
   * 
   * @return
   */
  protected AlignFrame setupAlignmentView()
  {
    /*
     * for now, using space for gap to match callers of
     * AlignmentView.getSequenceStrings()
     * may change this to '-' (with corresponding change to matrices)
     */
    SequenceI s1 = new Sequence("s1", "FR K S");
    SequenceI s2 = new Sequence("s2", "FS  L");

    s1.addSequenceFeature(
            new SequenceFeature("chain", null, 1, 4, 0f, null));
    s1.addSequenceFeature(
            new SequenceFeature("domain", null, 1, 4, 0f, null));
    s2.addSequenceFeature(
            new SequenceFeature("chain", null, 1, 3, 0f, null));
    s2.addSequenceFeature(
            new SequenceFeature("metal", null, 1, 3, 0f, null));
    s2.addSequenceFeature(
            new SequenceFeature("Pfam", null, 1, 3, 0f, null));
    AlignmentI al = new Alignment(new SequenceI[] { s1, s2 });
    AlignFrame af = new AlignFrame(al, 300, 300);
    af.setShowSeqFeatures(true);
    af.getFeatureRenderer().findAllFeatures(true);
    return af;
  }

}
