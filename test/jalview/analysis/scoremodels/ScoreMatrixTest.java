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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import jalview.api.analysis.SimilarityParamsI;
import jalview.io.DataSourceType;
import jalview.io.FileParse;
import jalview.io.ScoreMatrixFile;
import jalview.math.Matrix;
import jalview.math.MatrixI;
import jalview.schemes.ResidueProperties;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.testng.annotations.Test;

import junit.extensions.PA;

public class ScoreMatrixTest
{
  @Test(groups = "Functional")
  public void testConstructor()
  {
    // note score matrix does not have to be symmetric (though it should be!)
    float[][] scores = new float[3][];
    scores[0] = new float[] { 1f, 2f, 3f };
    scores[1] = new float[] { -4f, 5f, 6f };
    scores[2] = new float[] { 7f, 8f, 9f };
    ScoreMatrix sm = new ScoreMatrix("Test", "ABC".toCharArray(), scores);
    assertFalse(sm.isSymmetric());
    assertEquals(sm.getSize(), 3);
    assertArrayEquals(scores, sm.getMatrix());
    assertEquals(sm.getPairwiseScore('A', 'a'), 1f);
    assertEquals(sm.getPairwiseScore('b', 'c'), 6f);
    assertEquals(sm.getPairwiseScore('c', 'b'), 8f);
    assertEquals(sm.getMatrixIndex('c'), 2);
    assertEquals(sm.getMatrixIndex(' '), -1);

    // substitution to or from unknown symbol gets minimum score
    assertEquals(sm.getPairwiseScore('A', 'D'), -4f);
    assertEquals(sm.getPairwiseScore('D', 'A'), -4f);
    // unknown-to-self gets a score of 1
    assertEquals(sm.getPairwiseScore('D', 'D'), 1f);
  }

  @Test(
    groups = "Functional",
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testConstructor_matrixTooSmall()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { 3f, 4f };
    new ScoreMatrix("Test", "ABC".toCharArray(), scores);
  }

  @Test(
    groups = "Functional",
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testConstructor_matrixTooBig()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { 3f, 4f };
    new ScoreMatrix("Test", "A".toCharArray(), scores);
  }

  @Test(
    groups = "Functional",
    expectedExceptions =
    { IllegalArgumentException.class })
  public void testConstructor_matrixNotSquare()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { 3f };
    new ScoreMatrix("Test", "AB".toCharArray(), scores);
  }

  @Test(groups = "Functional")
  public void testBuildSymbolIndex()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { 3f, 4f };
    ScoreMatrix sm = new ScoreMatrix("Test", new char[] { 'A', '.' },
            scores);
    short[] index = sm.buildSymbolIndex("AX-yxYp".toCharArray());

    assertEquals(index.length, 128); // ASCII character set size

    assertEquals(index['A'], 0);
    assertEquals(index['a'], 0); // lower-case mapping added
    assertEquals(index['X'], 1);
    assertEquals(index['-'], 2);
    assertEquals(index['y'], 3); // lower-case override
    assertEquals(index['x'], 4); // lower-case override
    assertEquals(index['Y'], 5);
    assertEquals(index['p'], 6);
    assertEquals(index['P'], -1); // lower-case doesn't map upper-case

    /*
     * check all unmapped symbols have index for unmapped
     */
    for (int c = 0; c < index.length; c++)
    {
      if (!"AaXx-. Yyp".contains(String.valueOf((char) c)))
      {
        assertEquals(index[c], -1);
      }
    }
  }

  /**
   * check that characters not in the basic ASCII set are simply ignored
   */
  @Test(groups = "Functional")
  public void testBuildSymbolIndex_nonAscii()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { 3f, 4f };
    ScoreMatrix sm = new ScoreMatrix("Test", new char[] { 'A', '.' },
            scores);
    char[] weird = new char[] { 128, 245, 'P' };
    short[] index = sm.buildSymbolIndex(weird);
    assertEquals(index.length, 128);
    assertEquals(index['P'], 2);
    assertEquals(index['p'], 2);
    for (int c = 0; c < index.length; c++)
    {
      if (c != 'P' && c != 'p')
      {
        assertEquals(index[c], -1);
      }
    }
  }

  @Test(groups = "Functional")
  public void testGetMatrix()
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();
    float[][] m = sm.getMatrix();
    assertEquals(m.length, sm.getSize());
    assertEquals(m[2][4], -3f);
    // verify a defensive copy is returned
    float[][] m2 = sm.getMatrix();
    assertNotSame(m, m2);
    assertTrue(Arrays.deepEquals(m, m2));
  }

  @Test(groups = "Functional")
  public void testGetMatrixIndex()
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();
    assertEquals(sm.getMatrixIndex('A'), 0);
    assertEquals(sm.getMatrixIndex('R'), 1);
    assertEquals(sm.getMatrixIndex('r'), 1);
    assertEquals(sm.getMatrixIndex('N'), 2);
    assertEquals(sm.getMatrixIndex('D'), 3);
    assertEquals(sm.getMatrixIndex('X'), 22);
    assertEquals(sm.getMatrixIndex('x'), 22);
    assertEquals(sm.getMatrixIndex('-'), -1);
    assertEquals(sm.getMatrixIndex('*'), 23);
    assertEquals(sm.getMatrixIndex('.'), -1);
    assertEquals(sm.getMatrixIndex(' '), -1);
    assertEquals(sm.getMatrixIndex('?'), -1);
    assertEquals(sm.getMatrixIndex((char) 128), -1);
  }

  @Test(groups = "Functional")
  public void testGetSize()
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();
    assertEquals(sm.getMatrix().length, sm.getSize());
  }

  @Test(groups = "Functional")
  public void testComputePairwiseScores()
  {
    /*
     * NB score matrix expects '-' for gap
     */
    String[] seqs = new String[] { "FKL", "R-D", "QIA", "GWC" };
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();

    MatrixI pairwise = sm.findSimilarities(seqs, SimilarityParams.Jalview);

    /*
     * should be NxN where N = number of sequences
     */
    assertEquals(pairwise.height(), 4);
    assertEquals(pairwise.width(), 4);

    /*
     * should be symmetrical (because BLOSUM62 is)
     */
    for (int i = 0; i < pairwise.height(); i++)
    {
      for (int j = i + 1; j < pairwise.width(); j++)
      {
        assertEquals(pairwise.getValue(i, j), pairwise.getValue(j, i),
                String.format("Not symmetric at [%d, %d]", i, j));
      }
    }
    /*
     * verify expected BLOSUM dot product scores
     */
    // F.F + K.K + L.L = 6 + 5 + 4 = 15
    assertEquals(pairwise.getValue(0, 0), 15d);
    // R.R + -.- + D.D = 5 + 1 + 6 = 12
    assertEquals(pairwise.getValue(1, 1), 12d);
    // Q.Q + I.I + A.A = 5 + 4 + 4 = 13
    assertEquals(pairwise.getValue(2, 2), 13d);
    // G.G + W.W + C.C = 6 + 11 + 9 = 26
    assertEquals(pairwise.getValue(3, 3), 26d);
    // F.R + K.- + L.D = -3 + -4 + -4 = -11
    assertEquals(pairwise.getValue(0, 1), -11d);
    // F.Q + K.I + L.A = -3 + -3 + -1 = -7
    assertEquals(pairwise.getValue(0, 2), -7d);
    // F.G + K.W + L.C = -3 + -3 + -1 = -7
    assertEquals(pairwise.getValue(0, 3), -7d);
    // R.Q + -.I + D.A = 1 + -4 + -2 = -5
    assertEquals(pairwise.getValue(1, 2), -5d);
    // R.G + -.W + D.C = -2 + -4 + -3 = -9
    assertEquals(pairwise.getValue(1, 3), -9d);
    // Q.G + I.W + A.C = -2 + -3 + 0 = -5
    assertEquals(pairwise.getValue(2, 3), -5d);
  }

  /**
   * Test that the result of outputMatrix can be reparsed to give an identical
   * ScoreMatrix
   * 
   * @throws IOException
   * @throws MalformedURLException
   */
  @Test(groups = "Functional")
  public void testOutputMatrix_roundTrip()
          throws MalformedURLException, IOException
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();
    String output = sm.outputMatrix(false);
    FileParse fp = new FileParse(output, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    ScoreMatrix sm2 = parser.parseMatrix();
    assertNotNull(sm2);
    assertTrue(sm2.equals(sm));
  }

  @Test(groups = "Functional")
  public void testEqualsAndHashCode()
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();
    ScoreMatrix sm2 = new ScoreMatrix(sm.getName(),
            sm.getSymbols().toCharArray(), sm.getMatrix());
    assertTrue(sm.equals(sm2));
    assertEquals(sm.hashCode(), sm2.hashCode());

    sm2 = ScoreModels.getInstance().getPam250();
    assertFalse(sm.equals(sm2));
    assertNotEquals(sm.hashCode(), sm2.hashCode());

    assertFalse(sm.equals("hello"));
  }

  /**
   * Tests for scoring options where the longer length of two sequences is used
   */
  @Test(groups = "Functional")
  public void testcomputeSimilarity_matchLongestSequence()
  {
    /*
     * ScoreMatrix expects '-' for gaps
     */
    String s1 = "FR-K-S";
    String s2 = "FS--L";
    ScoreMatrix blosum = ScoreModels.getInstance().getBlosum62();

    /*
     * score gap-gap and gap-char
     * shorter sequence treated as if with trailing gaps
     * score = F^F + R^S + -^- + K^- + -^L + S^-
     * = 6 + -1 + 1 + -4 + -4 + -4 = -6
     */
    SimilarityParamsI params = new SimilarityParams(true, true, true,
            false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -6d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(true, false, true, false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -6d);

    /*
     * score gap-char but not gap-gap
     * score = F^F + R^S + 0 + K^- + -^L + S^-
     * = 6 + -1 + 0 + -4 + -4 + -4 = -7
     */
    params = new SimilarityParams(false, true, true, false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -7d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(false, false, true, false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -7d);

    /*
     * score gap-gap but not gap-char
     * score = F^F + R^S + -^- + 0 + 0 + 0
     * = 6 + -1 + 1 = 6
     */
    params = new SimilarityParams(true, false, false, false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 6d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(true, true, false, false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 6d);

    /*
     * score neither gap-gap nor gap-char
     * score = F^F + R^S + 0 + 0 + 0 + 0
     * = 6 + -1  = 5
     */
    params = new SimilarityParams(false, false, false, false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 5d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(false, true, false, false);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 5d);
  }

  /**
   * Tests for scoring options where only the shorter length of two sequences is
   * used
   */
  @Test(groups = "Functional")
  public void testcomputeSimilarity_matchShortestSequence()
  {
    /*
     * ScoreMatrix expects '-' for gaps
     */
    String s1 = "FR-K-S";
    String s2 = "FS--L";
    ScoreMatrix blosum = ScoreModels.getInstance().getBlosum62();

    /*
     * score gap-gap and gap-char
     * match shorter sequence only
     * score = F^F + R^S + -^- + K^- + -^L
     * = 6 + -1 + 1 + -4 + -4 = -2
     */
    SimilarityParamsI params = new SimilarityParams(true, true, true, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -2d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(true, false, true, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -2d);

    /*
     * score gap-char but not gap-gap
     * score = F^F + R^S + 0 + K^- + -^L
     * = 6 + -1 + 0 + -4 + -4 = -3
     */
    params = new SimilarityParams(false, true, true, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -3d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(false, false, true, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), -3d);

    /*
     * score gap-gap but not gap-char
     * score = F^F + R^S + -^- + 0 + 0
     * = 6 + -1 + 1 = 6
     */
    params = new SimilarityParams(true, false, false, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 6d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(true, true, false, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 6d);

    /*
     * score neither gap-gap nor gap-char
     * score = F^F + R^S + 0 + 0 + 0
     * = 6 + -1  = 5
     */
    params = new SimilarityParams(false, false, false, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 5d);
    // matchGap (arg2) is ignored:
    params = new SimilarityParams(false, true, false, true);
    assertEquals(blosum.computeSimilarity(s1, s2, params), 5d);
  }

  @Test(groups = "Functional")
  public void testSymmetric()
  {
    verifySymmetric(ScoreModels.getInstance().getBlosum62());
    verifySymmetric(ScoreModels.getInstance().getPam250());
    verifySymmetric(ScoreModels.getInstance().getDefaultModel(false)); // dna
  }

  /**
   * A helper method that inspects a loaded matrix and reports any asymmetry as
   * a test failure
   * 
   * @param sm
   */
  private void verifySymmetric(ScoreMatrix sm)
  {
    float[][] m = sm.getMatrix();
    int rows = m.length;
    for (int row = 0; row < rows; row++)
    {
      assertEquals(m[row].length, rows);
      for (int col = 0; col < rows; col++)
      {
        assertEquals(m[row][col], m[col][row],
                String.format("%s [%s, %s]", sm.getName(),
                        ResidueProperties.aa[row],
                        ResidueProperties.aa[col]));
      }
    }
  }

  /**
   * A test that just asserts the expected values in the Blosum62 score matrix
   */
  @Test(groups = "Functional")
  public void testBlosum62_values()
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();

    assertTrue(sm.isProtein());
    assertFalse(sm.isDNA());
    assertNull(sm.getDescription());

    /*
     * verify expected scores against ARNDCQEGHILKMFPSTWYVBZX
     * scraped from https://www.ncbi.nlm.nih.gov/Class/FieldGuide/BLOSUM62.txt
     */
    verifyValues(sm, 'A',
            new float[]
            { 4, -1, -2, -2, 0, -1, -1, 0, -2, -1, -1, -1, -1, -2, -1, 1, 0,
                -3, -2, 0, -2, -1, 0 });
    verifyValues(sm, 'R',
            new float[]
            { -1, 5, 0, -2, -3, 1, 0, -2, 0, -3, -2, 2, -1, -3, -2, -1, -1,
                -3, -2, -3, -1, 0, -1 });
    verifyValues(sm, 'N',
            new float[]
            { -2, 0, 6, 1, -3, 0, 0, 0, 1, -3, -3, 0, -2, -3, -2, 1, 0, -4,
                -2, -3, 3, 0, -1 });
    verifyValues(sm, 'D',
            new float[]
            { -2, -2, 1, 6, -3, 0, 2, -1, -1, -3, -4, -1, -3, -3, -1, 0, -1,
                -4, -3, -3, 4, 1, -1 });
    verifyValues(sm, 'C',
            new float[]
            { 0, -3, -3, -3, 9, -3, -4, -3, -3, -1, -1, -3, -1, -2, -3, -1,
                -1, -2, -2, -1, -3, -3, -2 });
    verifyValues(sm, 'Q',
            new float[]
            { -1, 1, 0, 0, -3, 5, 2, -2, 0, -3, -2, 1, 0, -3, -1, 0, -1, -2,
                -1, -2, 0, 3, -1 });
    verifyValues(sm, 'E',
            new float[]
            { -1, 0, 0, 2, -4, 2, 5, -2, 0, -3, -3, 1, -2, -3, -1, 0, -1,
                -3, -2, -2, 1, 4, -1 });
    verifyValues(sm, 'G',
            new float[]
            { 0, -2, 0, -1, -3, -2, -2, 6, -2, -4, -4, -2, -3, -3, -2, 0,
                -2, -2, -3, -3, -1, -2, -1 });
    verifyValues(sm, 'H',
            new float[]
            { -2, 0, 1, -1, -3, 0, 0, -2, 8, -3, -3, -1, -2, -1, -2, -1, -2,
                -2, 2, -3, 0, 0, -1 });
    verifyValues(sm, 'I',
            new float[]
            { -1, -3, -3, -3, -1, -3, -3, -4, -3, 4, 2, -3, 1, 0, -3, -2,
                -1, -3, -1, 3, -3, -3, -1 });
    verifyValues(sm, 'L',
            new float[]
            { -1, -2, -3, -4, -1, -2, -3, -4, -3, 2, 4, -2, 2, 0, -3, -2,
                -1, -2, -1, 1, -4, -3, -1 });
    verifyValues(sm, 'K',
            new float[]
            { -1, 2, 0, -1, -3, 1, 1, -2, -1, -3, -2, 5, -1, -3, -1, 0, -1,
                -3, -2, -2, 0, 1, -1 });
    verifyValues(sm, 'M',
            new float[]
            { -1, -1, -2, -3, -1, 0, -2, -3, -2, 1, 2, -1, 5, 0, -2, -1, -1,
                -1, -1, 1, -3, -1, -1 });
    verifyValues(sm, 'F',
            new float[]
            { -2, -3, -3, -3, -2, -3, -3, -3, -1, 0, 0, -3, 0, 6, -4, -2,
                -2, 1, 3, -1, -3, -3, -1 });
    verifyValues(sm, 'P',
            new float[]
            { -1, -2, -2, -1, -3, -1, -1, -2, -2, -3, -3, -1, -2, -4, 7, -1,
                -1, -4, -3, -2, -2, -1, -2 });
    verifyValues(sm, 'S',
            new float[]
            { 1, -1, 1, 0, -1, 0, 0, 0, -1, -2, -2, 0, -1, -2, -1, 4, 1, -3,
                -2, -2, 0, 0, 0 });
    verifyValues(sm, 'T',
            new float[]
            { 0, -1, 0, -1, -1, -1, -1, -2, -2, -1, -1, -1, -1, -2, -1, 1,
                5, -2, -2, 0, -1, -1, 0 });
    verifyValues(sm, 'W',
            new float[]
            { -3, -3, -4, -4, -2, -2, -3, -2, -2, -3, -2, -3, -1, 1, -4, -3,
                -2, 11, 2, -3, -4, -3, -2 });
    verifyValues(sm, 'Y',
            new float[]
            { -2, -2, -2, -3, -2, -1, -2, -3, 2, -1, -1, -2, -1, 3, -3, -2,
                -2, 2, 7, -1, -3, -2, -1 });
    verifyValues(sm, 'V',
            new float[]
            { 0, -3, -3, -3, -1, -2, -2, -3, -3, 3, 1, -2, 1, -1, -2, -2, 0,
                -3, -1, 4, -3, -2, -1 });
    verifyValues(sm, 'B',
            new float[]
            { -2, -1, 3, 4, -3, 0, 1, -1, 0, -3, -4, 0, -3, -3, -2, 0, -1,
                -4, -3, -3, 4, 1, -1 });
    verifyValues(sm, 'Z',
            new float[]
            { -1, 0, 0, 1, -3, 3, 4, -2, 0, -3, -3, 1, -1, -3, -1, 0, -1,
                -3, -2, -2, 1, 4, -1 });
    verifyValues(sm, 'X',
            new float[]
            { 0, -1, -1, -1, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, 0,
                0, -2, -1, -1, -1, -1, -1 });
  }

  /**
   * Helper method to check pairwise scores for one residue
   * 
   * @param sm
   * @param res
   * @param expected
   *          score values against 'res', in ResidueProperties.aaIndex order
   */
  private void verifyValues(ScoreMatrix sm, char res, float[] expected)
  {
    for (int j = 0; j < expected.length; j++)
    {
      char c2 = ResidueProperties.aa[j].charAt(0);
      assertEquals(sm.getPairwiseScore(res, c2), expected[j],
              String.format("%s->%s", res, c2));
    }
  }

  @Test(groups = "Functional")
  public void testConstructor_gapDash()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { 4f, 5f };
    ScoreMatrix sm = new ScoreMatrix("Test", new char[] { 'A', '-' },
            scores);
    assertEquals(sm.getSize(), 2);
    assertArrayEquals(scores, sm.getMatrix());
    assertEquals(sm.getPairwiseScore('A', 'a'), 1f);
    assertEquals(sm.getPairwiseScore('A', 'A'), 1f);
    assertEquals(sm.getPairwiseScore('a', '-'), 2f);
    assertEquals(sm.getPairwiseScore('-', 'A'), 4f);
    assertEquals(sm.getMatrixIndex('a'), 0);
    assertEquals(sm.getMatrixIndex('A'), 0);
    assertEquals(sm.getMatrixIndex('-'), 1);
    assertEquals(sm.getMatrixIndex(' '), -1);
    assertEquals(sm.getMatrixIndex('.'), -1);
  }

  @Test(groups = "Functional")
  public void testGetPairwiseScore()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { -4f, 5f };
    ScoreMatrix sm = new ScoreMatrix("Test", new char[] { 'A', 'B' },
            scores);
    assertEquals(sm.getPairwiseScore('A', 'A'), 1f);
    assertEquals(sm.getPairwiseScore('A', 'a'), 1f);
    assertEquals(sm.getPairwiseScore('A', 'B'), 2f);
    assertEquals(sm.getPairwiseScore('b', 'a'), -4f);
    assertEquals(sm.getPairwiseScore('B', 'b'), 5f);

    /*
     * unknown symbols currently score minimum score
     * or 1 for identity with self
     */
    assertEquals(sm.getPairwiseScore('A', '-'), -4f);
    assertEquals(sm.getPairwiseScore('-', 'A'), -4f);
    assertEquals(sm.getPairwiseScore('-', '-'), 1f);
    assertEquals(sm.getPairwiseScore('Q', 'W'), -4f);
    assertEquals(sm.getPairwiseScore('Q', 'Q'), 1f);

    /*
     * symbols not in basic ASCII set score zero
     */
    char c = (char) 200;
    assertEquals(sm.getPairwiseScore('Q', c), 0f);
    assertEquals(sm.getPairwiseScore(c, 'Q'), 0f);
  }

  @Test(groups = "Functional")
  public void testGetMinimumScore()
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();
    assertEquals(sm.getMinimumScore(), -4f);
  }

  @Test(groups = "Functional")
  public void testGetMaximumScore()
  {
    ScoreMatrix sm = ScoreModels.getInstance().getBlosum62();
    assertEquals(sm.getMaximumScore(), 11f);
  }

  @Test(groups = "Functional")
  public void testOutputMatrix_html()
  {
    float[][] scores = new float[2][];
    scores[0] = new float[] { 1f, 2f };
    scores[1] = new float[] { 4f, -5.3E-10f };
    ScoreMatrix sm = new ScoreMatrix("Test", "AB".toCharArray(), scores);
    String html = sm.outputMatrix(true);
    String expected = "<table border=\"1\"><tr><th></th><th>&nbsp;A&nbsp;</th><th>&nbsp;B&nbsp;</th></tr>\n"
            + "<tr><td>A</td><td>1.0</td><td>2.0</td></tr>\n"
            + "<tr><td>B</td><td>4.0</td><td>-5.3E-10</td></tr>\n"
            + "</table>";
    assertEquals(html, expected);
  }

  @Test(groups = "Functional")
  public void testIsSymmetric()
  {
    double delta = 0.0001d;
    float[][] scores = new float[][] { { 1f, -2f }, { -2f, 3f } };
    ScoreMatrix sm = new ScoreMatrix("Test", "AB".toCharArray(), scores);
    assertTrue(sm.isSymmetric());

    /*
     * verify that with a symmetric score matrix,
     * pairwise similarity matrix is also symmetric
     * seq1.seq1 = 5*A.A + 3*B.B = 5+9 = 14
     * seq1.seq2 = 3*A.A + 2*A.B + B.A + 2*B.B = 3 + -4 + -2 + 6 = 3
     * seq2.seq1 = 3*A.A + A.B + 2*B.A + 2*B.B = 3 + -2 + -4 + 6 = 3   
     * seq2.seq2 = 4*A.A + 4*B.B = 4 + 12 = 16   
     */
    SimilarityParamsI params = new SimilarityParams(true, true, true,
            false);
    String seq1 = "AAABBBAA";
    String seq2 = "AABBABBA";
    String[] seqs1 = new String[] { seq1, seq2 };
    MatrixI res1 = sm.findSimilarities(seqs1, params);
    assertTrue(
            res1.equals(new Matrix(new double[][]
            { { 14d, 3d }, { 3d, 16d } }), delta));

    /*
     * order of sequences affects diagonal, but not off-diagonal values
     * [0, 0] is now seq2.seq2, [1, 1] is seq1.seq1
     * [0, 1] is now seq2.seq1 = seq1.seq2 by symmetry
     */
    String[] seqs2 = new String[] { seq2, seq1 };
    MatrixI res2 = sm.findSimilarities(seqs2, params);
    assertFalse(res1.equals(res2));
    assertTrue(
            res2.equals(new Matrix(new double[][]
            { { 16d, 3d }, { 3d, 14d } }), delta));

    /*
     * now make the score matrix asymmetric
     * seq1.seq1 = 5*A.A + 3*B.B = 5+9 = 14
     * seq1.seq2 = 3*A.A + 2*A.B + B.A + 2*B.B = 3 + -4 + 2 + 6 = 7
     * seq2.seq1 = 3*A.A + A.B + 2*B.A + 2*B.B = 3 + -2 + 4 + 6 = 11  
     * seq2.seq2 = 4*A.A + 4*B.B = 4 + 12 = 16   
     */
    scores = new float[][] { { 1f, -2f }, { 2f, 3f } };
    sm = new ScoreMatrix("Test", "AB".toCharArray(), scores);
    assertFalse(sm.isSymmetric()); // [0, 1] != [1, 0]
    res1 = sm.findSimilarities(seqs1, params);
    assertTrue(
            res1.equals(new Matrix(new double[][]
            { { 14d, 7d }, { 11d, 16d } }), delta));

    /*
     * reverse order of sequences
     * - reverses order of main diagonal
     * - reflects off-diagonal values
     */
    res2 = sm.findSimilarities(seqs2, params);
    assertFalse(res1.equals(res2));
    assertTrue(
            res2.equals(new Matrix(new double[][]
            { { 16d, 11d }, { 7d, 14d } }), delta));

    /*
     * verify that forcing an asymmetric matrix to use
     * symmetric calculation gives a different (wrong) result
     */
    PA.setValue(sm, "symmetric", true);
    assertTrue(sm.isSymmetric()); // it's not true!
    res2 = sm.findSimilarities(seqs1, params);
    assertFalse(res1.equals(res2, delta));
  }
}
