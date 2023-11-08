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
package jalview.util.matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Locale;

import org.testng.annotations.Test;

import jalview.util.matcher.Matcher.PatternType;
import junit.extensions.PA;

public class MatcherTest
{
  @Test(groups = "Functional")
  public void testConstructor()
  {
    MatcherI m = new Matcher(Condition.Contains, "foo");
    assertEquals(m.getCondition(), Condition.Contains);
    assertEquals(m.getPattern(), "foo");
    assertEquals(PA.getValue(m, "uppercasePattern"), "FOO");
    assertEquals(PA.getValue(m, "floatValue"), 0f);
    assertEquals(PA.getValue(m, "longValue"), 0L);
    assertSame(PA.getValue(m, "patternType"), PatternType.String);

    m = new Matcher(Condition.GT, -2.1f);
    assertEquals(m.getCondition(), Condition.GT);
    assertEquals(m.getPattern(), "-2.1");
    assertEquals(PA.getValue(m, "floatValue"), -2.1f);
    assertEquals(PA.getValue(m, "longValue"), 0L);
    assertSame(PA.getValue(m, "patternType"), PatternType.Float);

    m = new Matcher(Condition.NotContains, "-1.2f");
    assertEquals(m.getCondition(), Condition.NotContains);
    assertEquals(m.getPattern(), "-1.2f");
    assertEquals(PA.getValue(m, "floatValue"), 0f);
    assertEquals(PA.getValue(m, "longValue"), 0L);
    assertSame(PA.getValue(m, "patternType"), PatternType.String);

    m = new Matcher(Condition.GE, "-1.2f");
    assertEquals(m.getCondition(), Condition.GE);
    assertEquals(m.getPattern(), "-1.2");
    assertEquals(PA.getValue(m, "floatValue"), -1.2f);
    assertEquals(PA.getValue(m, "longValue"), 0L);
    assertSame(PA.getValue(m, "patternType"), PatternType.Float);

    m = new Matcher(Condition.GE, "113890813");
    assertEquals(m.getCondition(), Condition.GE);
    assertEquals(m.getPattern(), "113890813");
    assertEquals(PA.getValue(m, "floatValue"), 0f);
    assertEquals(PA.getValue(m, "longValue"), 113890813L);
    assertSame(PA.getValue(m, "patternType"), PatternType.Integer);

    m = new Matcher(Condition.GE, "-987f");
    assertEquals(m.getCondition(), Condition.GE);
    assertEquals(m.getPattern(), "-987.0");
    assertEquals(PA.getValue(m, "floatValue"), -987f);
    assertEquals(PA.getValue(m, "longValue"), 0L);
    assertSame(PA.getValue(m, "patternType"), PatternType.Float);

    try
    {
      new Matcher(null, 0f);
      fail("Expected exception");
    } catch (NullPointerException e)
    {
      // expected
    }

    try
    {
      new Matcher(Condition.LT, "123,456");
      fail("Expected exception");
    } catch (NumberFormatException e)
    {
      // expected - see Long.valueOf()
    }

    try
    {
      new Matcher(Condition.LT, "123_456");
      fail("Expected exception");
    } catch (NumberFormatException e)
    {
      // expected - see Long.valueOf()
    }

    try
    {
      new Matcher(Condition.LT, "123456L");
      fail("Expected exception");
    } catch (NumberFormatException e)
    {
      // expected - see Long.valueOf()
    }
  }

  /**
   * Tests for float comparison conditions
   */
  @Test(groups = "Functional")
  public void testMatches_float()
  {
    /*
     * EQUALS test
     */
    MatcherI m = new Matcher(Condition.EQ, 2f);
    assertTrue(m.matches("2"));
    assertTrue(m.matches("2.0"));
    assertFalse(m.matches("2.01"));

    /*
     * NOT EQUALS test
     */
    m = new Matcher(Condition.NE, 2f);
    assertFalse(m.matches("2"));
    assertFalse(m.matches("2.0"));
    assertTrue(m.matches("2.01"));

    /*
     * >= test
     */
    m = new Matcher(Condition.GE, "2f");
    assertTrue(m.matches("2"));
    assertTrue(m.matches("2.1"));
    assertFalse(m.matches("1.9"));

    /*
     * > test
     */
    m = new Matcher(Condition.GT, 2f);
    assertFalse(m.matches("2"));
    assertTrue(m.matches("2.1"));
    assertFalse(m.matches("1.9"));

    /*
     * <= test
     */
    m = new Matcher(Condition.LE, "2.0f");
    assertTrue(m.matches("2"));
    assertFalse(m.matches("2.1"));
    assertTrue(m.matches("1.9"));

    /*
     * < test
     */
    m = new Matcher(Condition.LT, 2f);
    assertFalse(m.matches("2"));
    assertFalse(m.matches("2.1"));
    assertTrue(m.matches("1.9"));
  }

  /**
   * Verifies that all numeric match conditions fail when applied to non-numeric
   * or null values
   */
  @Test(groups = "Functional")
  public void testNumericMatch_nullOrInvalidValue()
  {
    for (Condition cond : Condition.values())
    {
      if (cond.isNumeric())
      {
        MatcherI m1 = new Matcher(cond, 2.1f);
        MatcherI m2 = new Matcher(cond, 2345L);
        assertFalse(m1.matches(null));
        assertFalse(m1.matches(""));
        assertFalse(m1.matches("two"));
        assertFalse(m2.matches(null));
        assertFalse(m2.matches(""));
        assertFalse(m2.matches("two"));
      }
    }
  }

  /**
   * Tests for string comparison conditions
   */
  @Test(groups = "Functional")
  public void testMatches_pattern()
  {
    /*
     * Contains
     */
    MatcherI m = new Matcher(Condition.Contains, "benign");
    assertTrue(m.matches("benign"));
    assertTrue(m.matches("MOSTLY BENIGN OBSERVED")); // not case-sensitive
    assertFalse(m.matches("pathogenic"));
    assertFalse(m.matches(null));

    /*
     * does not contain
     */
    m = new Matcher(Condition.NotContains, "benign");
    assertFalse(m.matches("benign"));
    assertFalse(m.matches("MOSTLY BENIGN OBSERVED")); // not case-sensitive
    assertTrue(m.matches("pathogenic"));
    assertTrue(m.matches(null)); // null value passes this condition

    /*
     * matches
     */
    m = new Matcher(Condition.Matches, "benign");
    assertTrue(m.matches("benign"));
    assertTrue(m.matches(" Benign ")); // trim before testing
    assertFalse(m.matches("MOSTLY BENIGN"));
    assertFalse(m.matches("pathogenic"));
    assertFalse(m.matches(null));

    /*
     * does not match
     */
    m = new Matcher(Condition.NotMatches, "benign");
    assertFalse(m.matches("benign"));
    assertFalse(m.matches(" Benign ")); // trimmed before testing
    assertTrue(m.matches("MOSTLY BENIGN"));
    assertTrue(m.matches("pathogenic"));
    assertTrue(m.matches(null));

    /*
     * value is present (is not null)
     */
    m = new Matcher(Condition.Present, null);
    assertTrue(m.matches("benign"));
    assertTrue(m.matches(""));
    assertFalse(m.matches(null));

    /*
     * value is not present (is null)
     */
    m = new Matcher(Condition.NotPresent, null);
    assertFalse(m.matches("benign"));
    assertFalse(m.matches(""));
    assertTrue(m.matches(null));

    /*
     * a number with a string match condition will be treated as string
     * (these cases shouldn't arise as the match() method is coded)
     */
    Matcher m1 = new Matcher(Condition.Contains, "32");
    assertFalse(m1.matchesFloat("-203f", 0f));
    assertTrue(m1.matchesFloat("-4321.0f", 0f));
    assertFalse(m1.matchesFloat("-203", 0f));
    assertTrue(m1.matchesFloat("-4321", 0f));
    assertFalse(m1.matchesLong("-203"));
    assertTrue(m1.matchesLong("-4321"));
    assertFalse(m1.matchesLong("-203f"));
    assertTrue(m1.matchesLong("-4321.0f"));
  }

  /**
   * If a float is passed with a string condition it gets converted to a string
   */
  @Test(groups = "Functional")
  public void testMatches_floatWithStringCondition()
  {
    MatcherI m = new Matcher(Condition.Contains, 1.2e-6f);
    assertEquals(m.getPattern(), "1.2E-6");
    assertEquals(PA.getValue(m, "uppercasePattern"), "1.2E-6");
    assertEquals(PA.getValue(m, "floatValue"), 0f);
    assertEquals(PA.getValue(m, "longValue"), 0L);
    assertSame(PA.getValue(m, "patternType"), PatternType.String);
    assertTrue(m.matches("1.2e-6"));

    m = new Matcher(Condition.Contains, 0.0000001f);
    assertEquals(m.getPattern(), "1.0E-7");
    assertTrue(m.matches("1.0e-7"));
    assertTrue(m.matches("1.0E-7"));
    assertFalse(m.matches("0.0000001f"));
  }

  @Test(groups = "Functional")
  public void testToString()
  {
    Locale.setDefault(Locale.ENGLISH);

    MatcherI m = new Matcher(Condition.LT, 1.2e-6f);
    assertEquals(m.toString(), "< 1.2E-6");

    m = new Matcher(Condition.GE, "20200413");
    assertEquals(m.toString(), ">= 20200413");

    m = new Matcher(Condition.NotMatches, "ABC");
    assertEquals(m.toString(), "Does not match 'ABC'");

    m = new Matcher(Condition.Contains, -1.2f);
    assertEquals(m.toString(), "Contains '-1.2'");
  }

  @Test(groups = "Functional")
  public void testEquals()
  {
    /*
     * string condition
     */
    MatcherI m = new Matcher(Condition.NotMatches, "ABC");
    assertFalse(m.equals(null));
    assertFalse(m.equals("foo"));
    assertTrue(m.equals(m));
    assertTrue(m.equals(new Matcher(Condition.NotMatches, "ABC")));
    // not case-sensitive:
    assertTrue(m.equals(new Matcher(Condition.NotMatches, "abc")));
    assertFalse(m.equals(new Matcher(Condition.Matches, "ABC")));
    assertFalse(m.equals(new Matcher(Condition.NotMatches, "def")));

    /*
     * numeric conditions - float values
     */
    m = new Matcher(Condition.LT, -1f);
    assertFalse(m.equals(null));
    assertFalse(m.equals("foo"));
    assertTrue(m.equals(m));
    assertTrue(m.equals(new Matcher(Condition.LT, -1f)));
    assertTrue(m.equals(new Matcher(Condition.LT, "-1f")));
    assertTrue(m.equals(new Matcher(Condition.LT, "-1.00f")));
    assertFalse(m.equals(new Matcher(Condition.LE, -1f)));
    assertFalse(m.equals(new Matcher(Condition.GE, -1f)));
    assertFalse(m.equals(new Matcher(Condition.NE, -1f)));
    assertFalse(m.equals(new Matcher(Condition.LT, 1f)));
    assertFalse(m.equals(new Matcher(Condition.LT, -1.1f)));

    /*
     * numeric conditions - integer values
     */
    m = new Matcher(Condition.LT, -123456);
    assertFalse(m.equals(null));
    assertFalse(m.equals("foo"));
    assertTrue(m.equals(m));
    assertTrue(m.equals(new Matcher(Condition.LT, -123456)));
    assertFalse(m.equals(new Matcher(Condition.LT, +123456)));
    assertTrue(m.equals(new Matcher(Condition.LT, "-123456")));
    assertFalse(m.equals(new Matcher(Condition.LT, -123456f)));
    assertFalse(m.equals(new Matcher(Condition.LT, "-123456f")));
  }

  @Test(groups = "Functional")
  public void testHashCode()
  {
    MatcherI m1 = new Matcher(Condition.NotMatches, "ABC");
    MatcherI m2 = new Matcher(Condition.NotMatches, "ABC");
    MatcherI m3 = new Matcher(Condition.NotMatches, "AB");
    MatcherI m4 = new Matcher(Condition.Matches, "ABC");
    assertEquals(m1.hashCode(), m2.hashCode());
    assertNotEquals(m1.hashCode(), m3.hashCode());
    assertNotEquals(m1.hashCode(), m4.hashCode());
    assertNotEquals(m3.hashCode(), m4.hashCode());
  }

  /**
   * Tests for integer comparison conditions
   */
  @Test(groups = "Functional")
  public void testMatches_long()
  {
    /*
     * EQUALS test
     */
    MatcherI m = new Matcher(Condition.EQ, 2);
    assertTrue(m.matches("2"));
    assertTrue(m.matches("+2"));
    assertFalse(m.matches("3"));
    // a float value may be passed to an integer matcher
    assertTrue(m.matches("2.0"));
    assertTrue(m.matches("2.000000f"));
    assertFalse(m.matches("2.01"));

    /*
     * NOT EQUALS test
     */
    m = new Matcher(Condition.NE, 123);
    assertFalse(m.matches("123"));
    assertFalse(m.matches("123.0"));
    assertTrue(m.matches("-123"));

    /*
     * >= test
     */
    m = new Matcher(Condition.GE, "113890813");
    assertTrue(m.matches("113890813"));
    assertTrue(m.matches("113890814"));
    assertFalse(m.matches("-113890813"));

    /*
     * > test
     */
    m = new Matcher(Condition.GT, 113890813);
    assertFalse(m.matches("113890813"));
    assertTrue(m.matches("113890814"));

    /*
     * <= test
     */
    m = new Matcher(Condition.LE, "113890813");
    assertTrue(m.matches("113890813"));
    assertFalse(m.matches("113890814"));
    assertTrue(m.matches("113890812"));

    /*
     * < test
     */
    m = new Matcher(Condition.LT, 113890813);
    assertFalse(m.matches("113890813"));
    assertFalse(m.matches("113890814"));
    assertTrue(m.matches("113890812"));
  }

  /**
   * Tests comparing a float value with an integer condition
   */
  @Test(groups = "Functional")
  public void testMatches_floatValueIntegerCondition()
  {
    MatcherI m = new Matcher(Condition.GT, 1234);
    assertEquals(PA.getValue(m, "longValue"), 1234L);
    assertSame(PA.getValue(m, "patternType"), PatternType.Integer);
    assertTrue(m.matches("1235"));
    assertTrue(m.matches("9867.345"));
    assertTrue(m.matches("9867.345f"));
  }
}
