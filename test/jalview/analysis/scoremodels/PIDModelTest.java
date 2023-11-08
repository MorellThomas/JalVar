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

import jalview.api.analysis.SimilarityParamsI;
import jalview.util.Comparison;

import org.testng.annotations.Test;

public class PIDModelTest
{
  private static final double DELTA = 0.00001D;

  @Test(groups = "Functional")
  public void testGetPairwiseScore()
  {
    PIDModel sm = new PIDModel();
    assertEquals(sm.getPairwiseScore('A', 'A'), 1f);
    assertEquals(sm.getPairwiseScore('A', 'a'), 1f);
    assertEquals(sm.getPairwiseScore('a', 'A'), 1f);
    assertEquals(sm.getPairwiseScore('A', 'B'), 0f);
    assertEquals(sm.getPairwiseScore('A', ' '), 0f);
    assertEquals(sm.getPairwiseScore(' ', ' '), 0f);
    assertEquals(sm.getPairwiseScore('.', '.'), 0f);
    assertEquals(sm.getPairwiseScore('-', '-'), 0f);
  }

  /**
   * Regression test to verify that a (suitably configured) PIDModel computes
   * the same percentage identities as the Comparison.PID method
   */
  @Test(groups = "Functional")
  public void testComputePID_matchesComparisonPID()
  {
    SimilarityParamsI params = new SimilarityParams(true, true, true, true);

    /*
     * same length, no gaps
     */
    String s1 = "ARFNQDWSGI";
    String s2 = "ARKNQDQSGI";

    new PIDModel();
    double newScore = PIDModel.computePID(s1, s2, params);
    double oldScore = Comparison.PID(s1, s2);
    assertEquals(newScore, oldScore, DELTA);
    // and verify PIDModel calculation is symmetric
    assertEquals(newScore, PIDModel.computePID(s2, s1, params));

    /*
     * same length, with gaps
     */
    s1 = "-RFNQDWSGI";
    s2 = "ARKNQ-QSGI";
    new PIDModel();
    newScore = PIDModel.computePID(s1, s2, params);
    oldScore = Comparison.PID(s1, s2);
    assertEquals(newScore, oldScore, DELTA);
    assertEquals(newScore, PIDModel.computePID(s2, s1, params));

    /*
     * s2 longer than s1, with gaps
     */
    s1 = "ARK-";
    s2 = "-RFNQ";
    new PIDModel();
    newScore = PIDModel.computePID(s1, s2, params);
    oldScore = Comparison.PID(s1, s2);
    assertEquals(newScore, oldScore, DELTA);
    assertEquals(newScore, PIDModel.computePID(s2, s1, params));

    /*
     * s1 longer than s2, with gaps
     */
    s1 = "-RFNQ";
    s2 = "ARK-";
    new PIDModel();
    newScore = PIDModel.computePID(s1, s2, params);
    oldScore = Comparison.PID(s1, s2);
    assertEquals(newScore, oldScore, DELTA);
    assertEquals(newScore, PIDModel.computePID(s2, s1, params));

    /*
     * same but now also with gapped columns
     */
    s1 = "-R-F-NQ";
    s2 = "AR-K--";
    new PIDModel();
    newScore = PIDModel.computePID(s1, s2, params);
    oldScore = Comparison.PID(s1, s2);
    assertEquals(newScore, oldScore, DELTA);
    assertEquals(newScore, PIDModel.computePID(s2, s1, params));
  }

  /**
   * Tests for percentage identity variants where only the shorter length of two
   * sequences is used
   */
  @Test(groups = "Functional")
  public void testComputePID_matchShortestSequence()
  {
    String s1 = "FR-K-S";
    String s2 = "FS--L";

    /*
     * match gap-gap and gap-char
     * PID = 4/5 = 80%
     */
    SimilarityParamsI params = new SimilarityParams(true, true, true, true);
    assertEquals(PIDModel.computePID(s1, s2, params), 80d);
    assertEquals(PIDModel.computePID(s2, s1, params), 80d);

    /*
     * match gap-char but not gap-gap
     * PID = 3/4 = 75%
     */
    params = new SimilarityParams(false, true, true, true);
    assertEquals(PIDModel.computePID(s1, s2, params), 75d);
    assertEquals(PIDModel.computePID(s2, s1, params), 75d);

    /*
     * include gaps but don't match them
     * include gap-gap, counted as identity
     * PID = 2/5 = 40%
     */
    params = new SimilarityParams(true, false, true, true);
    assertEquals(PIDModel.computePID(s1, s2, params), 40d);
    assertEquals(PIDModel.computePID(s2, s1, params), 40d);

    /*
     * include gaps but don't match them
     * exclude gap-gap
     * PID = 1/4 = 25%
     */
    params = new SimilarityParams(false, false, true, true);
    assertEquals(PIDModel.computePID(s1, s2, params), 25d);
    assertEquals(PIDModel.computePID(s2, s1, params), 25d);
  }

  /**
   * Tests for percentage identity variants where the longer length of two
   * sequences is used
   */
  @Test(groups = "Functional")
  public void testComputePID_matchLongestSequence()
  {
    String s1 = "FR-K-S";
    String s2 = "FS--L";

    /*
     * match gap-gap and gap-char
     * shorter sequence treated as if with trailing gaps
     * PID = 5/6 = 83.333...%
     */
    SimilarityParamsI params = new SimilarityParams(true, true, true,
            false);
    assertEquals(PIDModel.computePID(s1, s2, params), 500d / 6);
    assertEquals(PIDModel.computePID(s2, s1, params), 500d / 6);

    /*
     * match gap-char but not gap-gap
     * PID = 4/5 = 80%
     */
    params = new SimilarityParams(false, true, true, false);
    assertEquals(PIDModel.computePID(s1, s2, params), 80d);
    assertEquals(PIDModel.computePID(s2, s1, params), 80d);

    /*
     * include gaps but don't match them
     * include gap-gap, counted as identity
     * PID = 2/6 = 33.333...%
     */
    params = new SimilarityParams(true, false, true, false);
    assertEquals(PIDModel.computePID(s1, s2, params), 100d / 3);
    assertEquals(PIDModel.computePID(s2, s1, params), 100d / 3);

    /*
     * include gaps but don't match them
     * exclude gap-gap
     * PID = 1/5 = 25%
     */
    params = new SimilarityParams(false, false, true, false);
    assertEquals(PIDModel.computePID(s1, s2, params), 20d);
    assertEquals(PIDModel.computePID(s2, s1, params), 20d);

    /*
     * no tests for matchGaps=true, includeGaps=false
     * as it don't make sense
     */
  }
}
