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
package jalview.schemes;

import static org.testng.Assert.assertEquals;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.GraphLine;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;

import java.awt.Color;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AnnotationColourGradientTest
{
  final static int WIDTH = 11;

  final static int THRESHOLD_FIVE = 5;

  private AlignmentAnnotation ann;

  private SequenceI seq;

  private AlignmentI al;

  Color minColour = new Color(50, 200, 150);

  Color maxColour = new Color(150, 100, 250);

  /**
   * Setup creates an annotation over 11 columns with values 0-10 and threshold
   * 5
   */
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Annotation[] anns = new Annotation[WIDTH];
    /*
     * set annotations with values 0-10, graded colours
     */
    for (int col = 0; col < WIDTH; col++)
    {
      int hue = col * 20;
      Color colour = new Color(hue, hue, hue);
      anns[col] = new Annotation("a", "a", 'a', col, colour);
    }

    seq = new Sequence("Seq", "");
    al = new Alignment(new SequenceI[] { seq });

    /*
     * AlignmentAnnotation constructor works out min-max range
     */
    ann = new AlignmentAnnotation("", "", anns);
    ann.setThreshold(new GraphLine(THRESHOLD_FIVE, "", Color.RED));
    seq.addAlignmentAnnotation(ann);
  }

  @Test(groups = "Functional")
  public void testShadeCalculation_noThreshold()
  {
    AnnotationColourGradient testee = new AnnotationColourGradient(ann,
            minColour, maxColour, AnnotationColourGradient.NO_THRESHOLD);
    for (int col = 0; col < WIDTH; col++)
    {
      Color result = testee.shadeCalculation(ann, col);
      /*
       * column <n> is n/10 of the way from minCol to maxCol
       */
      Color expected = new Color(50 + 10 * col, 200 - 10 * col,
              150 + 10 * col);
      assertEquals(result, expected, "for column " + col);
    }
  }

  /**
   * Test the 'colour above threshold' case
   */
  @Test(groups = "Functional")
  public void testShadeCalculation_aboveThreshold()
  {
    AnnotationColourGradient testee = new AnnotationColourGradient(ann,
            minColour, maxColour, AnnotationColourGradient.ABOVE_THRESHOLD);
    for (int col = 0; col < WIDTH; col++)
    {
      Color result = testee.shadeCalculation(ann, col);
      /*
       * colour is derived regardless of the threshold value 
       * (the renderer will suppress colouring if above/below threshold)
       */
      Color expected = new Color(50 + 10 * col, 200 - 10 * col,
              150 + 10 * col);
      assertEquals(result, expected, "for column " + col);
    }

    /*
     * now make 6-10 the span of the colour range
     * (annotation value == column number in this test)
     */
    testee.setThresholdIsMinMax(true);
    for (int col = 0; col < THRESHOLD_FIVE; col++)
    {
      /*
       * colours below the threshold are computed as before
       */
      Color expected = new Color(50 + 10 * col, 200 - 10 * col,
              150 + 10 * col);
      Color result = testee.shadeCalculation(ann, col);
      assertEquals(result, expected, "for column " + col);
    }
    for (int col = THRESHOLD_FIVE; col < WIDTH; col++)
    {
      /*
       * colours for values >= threshold are graduated
       * range is 6-10 so steps of 100/5 = 20
       */
      int factor = col - THRESHOLD_FIVE;
      Color expected = new Color(50 + 20 * factor, 200 - 20 * factor,
              150 + 20 * factor);
      Color result = testee.shadeCalculation(ann, col);
      assertEquals(result, expected, "for column " + col);
    }

    /*
     * test for boundary case threshold == graphMax (JAL-3206)
     */
    float thresh = ann.threshold.value;
    ann.threshold.value = ann.graphMax;
    Color result = testee.shadeCalculation(ann, WIDTH - 1);
    assertEquals(result, maxColour);
    testee.setThresholdIsMinMax(false);
    result = testee.shadeCalculation(ann, WIDTH - 1);
    assertEquals(result, maxColour);
    ann.threshold.value = thresh; // reset
  }

  /**
   * Test the 'colour below threshold' case
   */
  @Test(groups = "Functional")
  public void testShadeCalculation_belowThreshold()
  {
    AnnotationColourGradient testee = new AnnotationColourGradient(ann,
            minColour, maxColour, AnnotationColourGradient.BELOW_THRESHOLD);

    for (int col = 0; col < WIDTH; col++)
    {
      Color result = testee.shadeCalculation(ann, col);
      /*
       * colour is derived regardless of the threshold value 
       * (the renderer will suppress colouring if above/below threshold)
       */
      Color expected = new Color(50 + 10 * col, 200 - 10 * col,
              150 + 10 * col);
      assertEquals(result, expected, "for column " + col);
    }

    /*
     * now make 0-5 the span of the colour range
     * (annotation value == column number in this test)
     */
    testee.setThresholdIsMinMax(true);
    for (int col = THRESHOLD_FIVE + 1; col < WIDTH; col++)
    {
      /*
       * colours above the threshold are computed as before
       */
      Color expected = new Color(50 + 10 * col, 200 - 10 * col,
              150 + 10 * col);
      Color result = testee.shadeCalculation(ann, col);
      assertEquals(result, expected, "for column " + col);
    }

    for (int col = 0; col <= THRESHOLD_FIVE; col++)
    {
      /*
       * colours for values <= threshold are graduated
       * range is 0-5 so steps of 100/5 = 20
       */
      Color expected = new Color(50 + 20 * col, 200 - 20 * col,
              150 + 20 * col);
      Color result = testee.shadeCalculation(ann, col);
      assertEquals(result, expected, "for column " + col);
    }

    /*
     * test for boundary case threshold == graphMin (JAL-3206)
     */
    float thresh = ann.threshold.value;
    ann.threshold.value = ann.graphMin;
    Color result = testee.shadeCalculation(ann, 0);
    assertEquals(result, minColour);
    testee.setThresholdIsMinMax(false);
    result = testee.shadeCalculation(ann, 0);
    assertEquals(result, minColour);
    ann.threshold.value = thresh; // reset
  }

  /**
   * Test the 'colour above threshold' case
   */
  @Test(groups = "Functional")
  public void testFindColour_aboveThreshold()
  {
    AnnotationColourGradient testee = new AnnotationColourGradient(ann,
            minColour, maxColour, AnnotationColourGradient.ABOVE_THRESHOLD);
    testee = (AnnotationColourGradient) testee.getInstance(null, al);

    for (int col = 0; col < WIDTH; col++)
    {
      Color result = testee.findColour('a', col, seq);
      /*
       * expect white at or below threshold of 5
       */
      Color expected = col <= 5 ? Color.white
              : new Color(50 + 10 * col, 200 - 10 * col, 150 + 10 * col);
      assertEquals(result, expected, "for column " + col);
    }

    /*
     * now make 6-10 the span of the colour range
     * (annotation value == column number in this test)
     */
    testee.setThresholdIsMinMax(true);
    for (int col = 0; col < WIDTH; col++)
    {
      /*
       * colours for values > threshold are graduated
       * range is 6-10 so steps of 100/5 = 20
       */
      int factor = col - THRESHOLD_FIVE;
      Color expected = col <= 5 ? Color.white
              : new Color(50 + 20 * factor, 200 - 20 * factor,
                      150 + 20 * factor);
      Color result = testee.findColour('a', col, seq);
      assertEquals(result, expected, "for column " + col);
    }
  }

  /**
   * Test the 'colour below threshold' case
   */
  @Test(groups = "Functional")
  public void testFindColour_belowThreshold()
  {
    AnnotationColourGradient testee = new AnnotationColourGradient(ann,
            minColour, maxColour, AnnotationColourGradient.BELOW_THRESHOLD);
    testee = (AnnotationColourGradient) testee.getInstance(null, al);

    for (int col = 0; col < WIDTH; col++)
    {
      Color result = testee.findColour('a', col, seq);
      Color expected = col >= 5 ? Color.white
              : new Color(50 + 10 * col, 200 - 10 * col, 150 + 10 * col);
      assertEquals(result, expected, "for column " + col);
    }

    /*
     * now make 0-5 the span of the colour range
     * (annotation value == column number in this test)
     */
    testee.setThresholdIsMinMax(true);
    for (int col = 0; col < WIDTH; col++)
    {
      /*
       * colours for values < threshold are graduated
       * range is 0-5 so steps of 100/5 = 20
       */
      Color expected = col >= 5 ? Color.white
              : new Color(50 + 20 * col, 200 - 20 * col, 150 + 20 * col);
      Color result = testee.findColour('a', col, seq);
      assertEquals(result, expected, "for column " + col);
    }
  }

  @Test(groups = "Functional")
  public void testFindColour_noThreshold()
  {
    AnnotationColourGradient testee = new AnnotationColourGradient(ann,
            minColour, maxColour, AnnotationColourGradient.NO_THRESHOLD);
    testee = (AnnotationColourGradient) testee.getInstance(null, al);

    for (int col = 0; col < WIDTH; col++)
    {
      Color result = testee.findColour('a', col, seq);
      /*
       * column <n> is n/10 of the way from minCol to maxCol
       */
      Color expected = new Color(50 + 10 * col, 200 - 10 * col,
              150 + 10 * col);
      assertEquals(result, expected, "for column " + col);
    }
  }

  @Test(groups = "Functional")
  public void testFindColour_originalColours()
  {
    AnnotationColourGradient testee = new AnnotationColourGradient(ann,
            minColour, maxColour, AnnotationColourGradient.NO_THRESHOLD);
    testee = (AnnotationColourGradient) testee.getInstance(null, al);

    /*
     * flag corresponding to 'use original colours' checkbox
     * - just use the individual annotation colours
     */
    testee.setPredefinedColours(true);

    /*
     * the annotation colour is returned, except for column 0 where it is
     * black - in this case the colour scheme colour overrides it
     */
    for (int col = 0; col < WIDTH; col++)
    {
      int hue = col * 20;
      Color c = col == 0 ? minColour : new Color(hue, hue, hue);
      assertEquals(testee.findColour('a', col, seq), c,
              "for column " + col);
    }
  }
}
