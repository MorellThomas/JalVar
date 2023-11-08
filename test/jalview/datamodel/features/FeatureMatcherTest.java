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
package jalview.datamodel.features;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Locale;

import org.testng.annotations.Test;

import jalview.datamodel.SequenceFeature;
import jalview.util.MessageManager;
import jalview.util.matcher.Condition;
import jalview.util.matcher.Matcher;
import jalview.util.matcher.MatcherI;
import junit.extensions.PA;

public class FeatureMatcherTest
{
  @Test(groups = "Functional")
  public void testMatches_byLabel()
  {
    SequenceFeature sf = new SequenceFeature("Cath", "this is my label", 11,
            12, "grp");

    /*
     * contains - not case sensitive
     */
    assertTrue(
            FeatureMatcher.byLabel(Condition.Contains, "IS").matches(sf));
    assertTrue(FeatureMatcher.byLabel(Condition.Contains, "").matches(sf));
    assertFalse(
            FeatureMatcher.byLabel(Condition.Contains, "ISNT").matches(sf));

    /*
     * does not contain
     */
    assertTrue(FeatureMatcher.byLabel(Condition.NotContains, "isnt")
            .matches(sf));
    assertFalse(FeatureMatcher.byLabel(Condition.NotContains, "is")
            .matches(sf));

    /*
     * matches
     */
    assertTrue(FeatureMatcher.byLabel(Condition.Matches, "THIS is MY label")
            .matches(sf));
    assertFalse(FeatureMatcher.byLabel(Condition.Matches, "THIS is MY")
            .matches(sf));

    /*
     * does not match
     */
    assertFalse(FeatureMatcher
            .byLabel(Condition.NotMatches, "THIS is MY label").matches(sf));
    assertTrue(FeatureMatcher.byLabel(Condition.NotMatches, "THIS is MY")
            .matches(sf));

    /*
     * is present / not present
     */
    assertTrue(FeatureMatcher.byLabel(Condition.Present, "").matches(sf));
    assertFalse(
            FeatureMatcher.byLabel(Condition.NotPresent, "").matches(sf));
  }

  @Test(groups = "Functional")
  public void testMatches_byScore()
  {
    SequenceFeature sf = new SequenceFeature("Cath", "this is my label", 11,
            12, 3.2f, "grp");

    assertTrue(FeatureMatcher.byScore(Condition.LT, "3.3").matches(sf));
    assertFalse(FeatureMatcher.byScore(Condition.LT, "3.2").matches(sf));
    assertFalse(FeatureMatcher.byScore(Condition.LT, "2.2").matches(sf));

    assertTrue(FeatureMatcher.byScore(Condition.LE, "3.3").matches(sf));
    assertTrue(FeatureMatcher.byScore(Condition.LE, "3.2").matches(sf));
    assertFalse(FeatureMatcher.byScore(Condition.LE, "2.2").matches(sf));

    assertFalse(FeatureMatcher.byScore(Condition.EQ, "3.3").matches(sf));
    assertTrue(FeatureMatcher.byScore(Condition.EQ, "3.2").matches(sf));

    assertFalse(FeatureMatcher.byScore(Condition.GE, "3.3").matches(sf));
    assertTrue(FeatureMatcher.byScore(Condition.GE, "3.2").matches(sf));
    assertTrue(FeatureMatcher.byScore(Condition.GE, "2.2").matches(sf));

    assertFalse(FeatureMatcher.byScore(Condition.GT, "3.3").matches(sf));
    assertFalse(FeatureMatcher.byScore(Condition.GT, "3.2").matches(sf));
    assertTrue(FeatureMatcher.byScore(Condition.GT, "2.2").matches(sf));
  }

  @Test(groups = "Functional")
  public void testMatches_byAttribute()
  {
    /*
     * a numeric matcher - MatcherTest covers more conditions
     */
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.GE, "-2",
            "AF");
    SequenceFeature sf = new SequenceFeature("Cath", "desc", 11, 12, "grp");
    assertFalse(fm.matches(sf));
    sf.setValue("AF", "foobar");
    assertFalse(fm.matches(sf));
    sf.setValue("AF", "-2");
    assertTrue(fm.matches(sf));
    sf.setValue("AF", "-1");
    assertTrue(fm.matches(sf));
    sf.setValue("AF", "-3");
    assertFalse(fm.matches(sf));
    sf.setValue("AF", "");
    assertFalse(fm.matches(sf));

    /*
     * a string pattern matcher
     */
    fm = FeatureMatcher.byAttribute(Condition.Contains, "Cat", "AF");
    assertFalse(fm.matches(sf));
    sf.setValue("AF", "raining cats and dogs");
    assertTrue(fm.matches(sf));

    fm = FeatureMatcher.byAttribute(Condition.Present, "", "AC");
    assertFalse(fm.matches(sf));
    sf.setValue("AC", "21");
    assertTrue(fm.matches(sf));

    fm = FeatureMatcher.byAttribute(Condition.NotPresent, "", "AC_Females");
    assertTrue(fm.matches(sf));
    sf.setValue("AC_Females", "21");
    assertFalse(fm.matches(sf));
  }

  @Test(groups = "Functional")
  public void testToString()
  {
    Locale.setDefault(Locale.ENGLISH);

    /*
     * toString uses the i18n translation of the enum conditions
     */
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.LT, "1.2",
            "AF");
    assertEquals(fm.toString(), "AF < 1.2");

    /*
     * Present / NotPresent omit the value pattern
     */
    fm = FeatureMatcher.byAttribute(Condition.Present, "", "AF");
    assertEquals(fm.toString(), "AF is present");
    fm = FeatureMatcher.byAttribute(Condition.NotPresent, "", "AF");
    assertEquals(fm.toString(), "AF is not present");

    /*
     * by Label
     */
    fm = FeatureMatcher.byLabel(Condition.Matches, "foobar");
    assertEquals(fm.toString(),
            MessageManager.getString("label.label") + " matches 'foobar'");

    /*
     * by Score
     */
    fm = FeatureMatcher.byScore(Condition.GE, "12.2");
    assertEquals(fm.toString(),
            MessageManager.getString("label.score") + " >= 12.2");
  }

  @Test(groups = "Functional")
  public void testGetAttribute()
  {
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.GE, "-2",
            "AF");
    assertEquals(fm.getAttribute(), new String[] { "AF" });

    /*
     * compound key (attribute / subattribute)
     */
    fm = FeatureMatcher.byAttribute(Condition.GE, "-2F", "CSQ",
            "Consequence");
    assertEquals(fm.getAttribute(), new String[] { "CSQ", "Consequence" });

    /*
     * answers null if match is by Label or by Score
     */
    assertNull(FeatureMatcher.byLabel(Condition.NotContains, "foo")
            .getAttribute());
    assertNull(FeatureMatcher.byScore(Condition.LE, "-1").getAttribute());
  }

  @Test(groups = "Functional")
  public void testIsByAttribute()
  {
    assertFalse(FeatureMatcher.byLabel(Condition.NotContains, "foo")
            .isByAttribute());
    assertFalse(FeatureMatcher.byScore(Condition.LE, "-1").isByAttribute());
    assertTrue(FeatureMatcher.byAttribute(Condition.LE, "-1", "AC")
            .isByAttribute());
  }

  @Test(groups = "Functional")
  public void testIsByLabel()
  {
    assertTrue(FeatureMatcher.byLabel(Condition.NotContains, "foo")
            .isByLabel());
    assertFalse(FeatureMatcher.byScore(Condition.LE, "-1").isByLabel());
    assertFalse(FeatureMatcher.byAttribute(Condition.LE, "-1", "AC")
            .isByLabel());
  }

  @Test(groups = "Functional")
  public void testIsByScore()
  {
    assertFalse(FeatureMatcher.byLabel(Condition.NotContains, "foo")
            .isByScore());
    assertTrue(FeatureMatcher.byScore(Condition.LE, "-1").isByScore());
    assertFalse(FeatureMatcher.byAttribute(Condition.LE, "-1", "AC")
            .isByScore());
  }

  @Test(groups = "Functional")
  public void testGetMatcher()
  {
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.GE, "-2f",
            "AF");
    assertEquals(fm.getMatcher().getCondition(), Condition.GE);
    assertEquals(PA.getValue(fm.getMatcher(), "floatValue"), -2F);
    assertEquals(fm.getMatcher().getPattern(), "-2.0");
  }

  @Test(groups = "Functional")
  public void testFromString()
  {
    FeatureMatcherI fm = FeatureMatcher.fromString("'AF' LT 1.2");
    assertFalse(fm.isByLabel());
    assertFalse(fm.isByScore());
    assertEquals(fm.getAttribute(), new String[] { "AF" });
    MatcherI matcher = fm.getMatcher();
    assertSame(Condition.LT, matcher.getCondition());
    assertEquals(PA.getValue(matcher, "floatValue"), 1.2f);
    assertSame(PA.getValue(matcher, "patternType"),
            Matcher.PatternType.Float);
    assertEquals(matcher.getPattern(), "1.2");

    // quotes are optional, condition is not case sensitive
    fm = FeatureMatcher.fromString("AF lt '1.2'");
    matcher = fm.getMatcher();
    assertFalse(fm.isByLabel());
    assertFalse(fm.isByScore());
    assertEquals(fm.getAttribute(), new String[] { "AF" });
    assertSame(Condition.LT, matcher.getCondition());
    assertEquals(PA.getValue(matcher, "floatValue"), 1.2F);
    assertEquals(matcher.getPattern(), "1.2");

    fm = FeatureMatcher.fromString("'AF' Present");
    matcher = fm.getMatcher();
    assertFalse(fm.isByLabel());
    assertFalse(fm.isByScore());
    assertEquals(fm.getAttribute(), new String[] { "AF" });
    assertSame(Condition.Present, matcher.getCondition());
    assertSame(PA.getValue(matcher, "patternType"),
            Matcher.PatternType.String);

    fm = FeatureMatcher.fromString("CSQ:Consequence contains damaging");
    matcher = fm.getMatcher();
    assertFalse(fm.isByLabel());
    assertFalse(fm.isByScore());
    assertEquals(fm.getAttribute(), new String[] { "CSQ", "Consequence" });
    assertSame(Condition.Contains, matcher.getCondition());
    assertEquals(matcher.getPattern(), "damaging");

    // keyword Label is not case sensitive
    fm = FeatureMatcher.fromString("LABEL Matches 'foobar'");
    matcher = fm.getMatcher();
    assertTrue(fm.isByLabel());
    assertFalse(fm.isByScore());
    assertNull(fm.getAttribute());
    assertSame(Condition.Matches, matcher.getCondition());
    assertEquals(matcher.getPattern(), "foobar");

    fm = FeatureMatcher.fromString("'Label' matches 'foo bar'");
    matcher = fm.getMatcher();
    assertTrue(fm.isByLabel());
    assertFalse(fm.isByScore());
    assertNull(fm.getAttribute());
    assertSame(Condition.Matches, matcher.getCondition());
    assertEquals(matcher.getPattern(), "foo bar");

    // quotes optional on pattern
    fm = FeatureMatcher.fromString("'Label' matches foo bar");
    matcher = fm.getMatcher();
    assertTrue(fm.isByLabel());
    assertFalse(fm.isByScore());
    assertNull(fm.getAttribute());
    assertSame(Condition.Matches, matcher.getCondition());
    assertEquals(matcher.getPattern(), "foo bar");

    // integer condition
    fm = FeatureMatcher.fromString("Score GE 12");
    matcher = fm.getMatcher();
    assertFalse(fm.isByLabel());
    assertTrue(fm.isByScore());
    assertNull(fm.getAttribute());
    assertSame(Condition.GE, matcher.getCondition());
    assertEquals(matcher.getPattern(), "12");
    assertEquals(PA.getValue(matcher, "floatValue"), 0f);
    assertEquals(PA.getValue(matcher, "longValue"), 12L);
    assertSame(PA.getValue(matcher, "patternType"),
            Matcher.PatternType.Integer);

    // keyword Score is not case sensitive
    fm = FeatureMatcher.fromString("'SCORE' ge '12.2'");
    matcher = fm.getMatcher();
    assertFalse(fm.isByLabel());
    assertTrue(fm.isByScore());
    assertNull(fm.getAttribute());
    assertSame(Condition.GE, matcher.getCondition());
    assertEquals(matcher.getPattern(), "12.2");
    assertEquals(PA.getValue(matcher, "floatValue"), 12.2F);

    // invalid numeric pattern
    assertNull(FeatureMatcher.fromString("Score eq twelve"));
    // unbalanced opening quote
    assertNull(FeatureMatcher.fromString("'Score ge 12.2"));
    // unbalanced pattern quote
    assertNull(FeatureMatcher.fromString("'Score' ge '12.2"));
    // pattern missing
    assertNull(FeatureMatcher.fromString("Score ge"));
    // condition and pattern missing
    assertNull(FeatureMatcher.fromString("Score"));
    // everything missing
    assertNull(FeatureMatcher.fromString(""));
  }

  /**
   * Tests for toStableString which (unlike toString) does not i18n the
   * conditions
   */
  @Test(groups = "Functional")
  public void testToStableString()
  {
    // attribute name not quoted unless it contains space
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.LT, "1.2",
            "AF");
    assertEquals(fm.toStableString(), "AF LT 1.2");

    /*
     * Present / NotPresent omit the value pattern
     */
    fm = FeatureMatcher.byAttribute(Condition.Present, "", "AF");
    assertEquals(fm.toStableString(), "AF Present");
    fm = FeatureMatcher.byAttribute(Condition.NotPresent, "", "AF");
    assertEquals(fm.toStableString(), "AF NotPresent");

    /*
     * by Label
     * pattern not quoted unless it contains space
     */
    fm = FeatureMatcher.byLabel(Condition.Matches, "foobar");
    assertEquals(fm.toStableString(), "Label Matches foobar");

    fm = FeatureMatcher.byLabel(Condition.Matches, "foo bar");
    assertEquals(fm.toStableString(), "Label Matches 'foo bar'");

    /*
     * by Score
     */
    fm = FeatureMatcher.byScore(Condition.GE, "12.2");
    assertEquals(fm.toStableString(), "Score GE 12.2");
  }
}
