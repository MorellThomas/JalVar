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
import static org.testng.Assert.fail;

import jalview.datamodel.SequenceFeature;
import jalview.util.matcher.Condition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.testng.annotations.Test;

public class FeatureMatcherSetTest
{
  @Test(groups = "Functional")
  public void testMatches_byAttribute()
  {
    /*
     * a numeric matcher - MatcherTest covers more conditions
     */
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.GE, "-2",
            "AF");
    FeatureMatcherSetI fms = new FeatureMatcherSet();
    fms.and(fm);
    SequenceFeature sf = new SequenceFeature("Cath", "desc", 11, 12, "grp");
    assertFalse(fms.matches(sf));
    sf.setValue("AF", "foobar");
    assertFalse(fms.matches(sf));
    sf.setValue("AF", "-2");
    assertTrue(fms.matches(sf));
    sf.setValue("AF", "-1");
    assertTrue(fms.matches(sf));
    sf.setValue("AF", "-3");
    assertFalse(fms.matches(sf));
    sf.setValue("AF", "");
    assertFalse(fms.matches(sf));

    /*
     * a string pattern matcher
     */
    fm = FeatureMatcher.byAttribute(Condition.Contains, "Cat", "AF");
    fms = new FeatureMatcherSet();
    fms.and(fm);
    assertFalse(fms.matches(sf));
    sf.setValue("AF", "raining cats and dogs");
    assertTrue(fms.matches(sf));
  }

  @Test(groups = "Functional")
  public void testAnd()
  {
    // condition1: AF value contains "dog" (matches)
    FeatureMatcherI fm1 = FeatureMatcher.byAttribute(Condition.Contains,
            "dog", "AF");
    // condition 2: CSQ value does not contain "how" (does not match)
    FeatureMatcherI fm2 = FeatureMatcher.byAttribute(Condition.NotContains,
            "how", "CSQ");

    SequenceFeature sf = new SequenceFeature("Cath", "helix domain", 11, 12,
            6.2f, "grp");
    sf.setValue("AF", "raining cats and dogs");
    sf.setValue("CSQ", "showers");

    assertTrue(fm1.matches(sf));
    assertFalse(fm2.matches(sf));

    FeatureMatcherSetI fms = new FeatureMatcherSet();
    assertTrue(fms.matches(sf)); // if no conditions, then 'all' pass
    fms.and(fm1);
    assertTrue(fms.matches(sf));
    fms.and(fm2);
    assertFalse(fms.matches(sf));

    /*
     * OR a failed attribute condition with a matched label condition
     */
    fms = new FeatureMatcherSet();
    fms.and(fm2);
    assertFalse(fms.matches(sf));
    FeatureMatcher byLabelPass = FeatureMatcher.byLabel(Condition.Contains,
            "Helix");
    fms.or(byLabelPass);
    assertTrue(fms.matches(sf));

    /*
     * OR a failed attribute condition with a failed score condition
     */
    fms = new FeatureMatcherSet();
    fms.and(fm2);
    assertFalse(fms.matches(sf));
    FeatureMatcher byScoreFail = FeatureMatcher.byScore(Condition.LT,
            "5.9");
    fms.or(byScoreFail);
    assertFalse(fms.matches(sf));

    /*
     * OR failed attribute and score conditions with matched label condition
     */
    fms = new FeatureMatcherSet();
    fms.or(fm2);
    fms.or(byScoreFail);
    assertFalse(fms.matches(sf));
    fms.or(byLabelPass);
    assertTrue(fms.matches(sf));
  }

  @Test(groups = "Functional")
  public void testToString()
  {
    Locale.setDefault(Locale.ENGLISH);
    FeatureMatcherI fm1 = FeatureMatcher.byAttribute(Condition.LT, "1.2",
            "AF");
    assertEquals(fm1.toString(), "AF < 1.2");

    FeatureMatcher fm2 = FeatureMatcher.byAttribute(Condition.NotContains,
            "path", "CLIN_SIG");
    assertEquals(fm2.toString(), "CLIN_SIG does not contain 'path'");

    /*
     * AND them
     */
    FeatureMatcherSetI fms = new FeatureMatcherSet();
    assertEquals(fms.toString(), "");
    fms.and(fm1);
    assertEquals(fms.toString(), "AF < 1.2");
    fms.and(fm2);
    assertEquals(fms.toString(),
            "(AF < 1.2) and (CLIN_SIG does not contain 'path')");

    /*
     * OR them
     */
    fms = new FeatureMatcherSet();
    assertEquals(fms.toString(), "");
    fms.or(fm1);
    assertEquals(fms.toString(), "AF < 1.2");
    fms.or(fm2);
    assertEquals(fms.toString(),
            "(AF < 1.2) or (CLIN_SIG does not contain 'path')");

    try
    {
      fms.and(fm1);
      fail("Expected exception");
    } catch (IllegalStateException e)
    {
      // expected
    }
  }

  @Test(groups = "Functional")
  public void testOr()
  {
    // condition1: AF value contains "dog" (matches)
    FeatureMatcherI fm1 = FeatureMatcher.byAttribute(Condition.Contains,
            "dog", "AF");
    // condition 2: CSQ value does not contain "how" (does not match)
    FeatureMatcherI fm2 = FeatureMatcher.byAttribute(Condition.NotContains,
            "how", "CSQ");

    SequenceFeature sf = new SequenceFeature("Cath", "desc", 11, 12, "grp");
    sf.setValue("AF", "raining cats and dogs");
    sf.setValue("CSQ", "showers");

    assertTrue(fm1.matches(sf));
    assertFalse(fm2.matches(sf));

    FeatureMatcherSetI fms = new FeatureMatcherSet();
    assertTrue(fms.matches(sf)); // if no conditions, then 'all' pass
    fms.or(fm1);
    assertTrue(fms.matches(sf));
    fms.or(fm2);
    assertTrue(fms.matches(sf)); // true or false makes true

    fms = new FeatureMatcherSet();
    fms.or(fm2);
    assertFalse(fms.matches(sf));
    fms.or(fm1);
    assertTrue(fms.matches(sf)); // false or true makes true

    try
    {
      fms.and(fm2);
      fail("Expected exception");
    } catch (IllegalStateException e)
    {
      // expected
    }
  }

  @Test(groups = "Functional")
  public void testIsEmpty()
  {
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.GE, "-2.0",
            "AF");
    FeatureMatcherSetI fms = new FeatureMatcherSet();
    assertTrue(fms.isEmpty());
    fms.and(fm);
    assertFalse(fms.isEmpty());
  }

  @Test(groups = "Functional")
  public void testGetMatchers()
  {
    FeatureMatcherSetI fms = new FeatureMatcherSet();

    /*
     * empty iterable:
     */
    Iterator<FeatureMatcherI> iterator = fms.getMatchers().iterator();
    assertFalse(iterator.hasNext());

    /*
     * one matcher:
     */
    FeatureMatcherI fm1 = FeatureMatcher.byAttribute(Condition.GE, "-2",
            "AF");
    fms.and(fm1);
    iterator = fms.getMatchers().iterator();
    assertSame(fm1, iterator.next());
    assertFalse(iterator.hasNext());

    /*
     * two matchers:
     */
    FeatureMatcherI fm2 = FeatureMatcher.byAttribute(Condition.LT, "8f",
            "AF");
    fms.and(fm2);
    iterator = fms.getMatchers().iterator();
    assertSame(fm1, iterator.next());
    assertSame(fm2, iterator.next());
    assertFalse(iterator.hasNext());
  }

  /**
   * Tests for the 'compound attribute' key i.e. where first key's value is a
   * map from which we take the value for the second key, e.g. CSQ : Consequence
   */
  @Test(groups = "Functional")
  public void testMatches_compoundKey()
  {
    /*
     * a numeric matcher - MatcherTest covers more conditions
     */
    FeatureMatcherI fm = FeatureMatcher.byAttribute(Condition.GE, "-2",
            "CSQ", "Consequence");
    SequenceFeature sf = new SequenceFeature("Cath", "desc", 2, 10, "grp");
    FeatureMatcherSetI fms = new FeatureMatcherSet();
    fms.and(fm);
    assertFalse(fms.matches(sf));
    Map<String, String> csq = new HashMap<>();
    sf.setValue("CSQ", csq);
    assertFalse(fms.matches(sf));
    csq.put("Consequence", "-2");
    assertTrue(fms.matches(sf));
    csq.put("Consequence", "-1");
    assertTrue(fms.matches(sf));
    csq.put("Consequence", "-3");
    assertFalse(fms.matches(sf));
    csq.put("Consequence", "");
    assertFalse(fms.matches(sf));
    csq.put("Consequence", "junk");
    assertFalse(fms.matches(sf));

    /*
     * a string pattern matcher
     */
    fm = FeatureMatcher.byAttribute(Condition.Contains, "Cat", "CSQ",
            "Consequence");
    fms = new FeatureMatcherSet();
    fms.and(fm);
    assertFalse(fms.matches(sf));
    csq.put("PolyPhen", "damaging");
    assertFalse(fms.matches(sf));
    csq.put("Consequence", "damaging");
    assertFalse(fms.matches(sf));
    csq.put("Consequence", "Catastrophic");
    assertTrue(fms.matches(sf));
  }

  /**
   * Tests for toStableString which (unlike toString) does not i18n the
   * conditions
   * 
   * @see FeatureMatcherTest#testToStableString()
   */
  @Test(groups = "Functional")
  public void testToStableString()
  {
    FeatureMatcherI fm1 = FeatureMatcher.byAttribute(Condition.LT, "1.2",
            "AF");
    assertEquals(fm1.toStableString(), "AF LT 1.2");

    FeatureMatcher fm2 = FeatureMatcher.byAttribute(Condition.NotContains,
            "path", "CLIN_SIG");
    assertEquals(fm2.toStableString(), "CLIN_SIG NotContains path");

    /*
     * AND them
     */
    FeatureMatcherSetI fms = new FeatureMatcherSet();
    assertEquals(fms.toStableString(), "");
    fms.and(fm1);
    // no brackets needed if a single condition
    assertEquals(fms.toStableString(), "AF LT 1.2");
    // brackets if more than one condition
    fms.and(fm2);
    assertEquals(fms.toStableString(),
            "(AF LT 1.2) AND (CLIN_SIG NotContains path)");

    /*
     * OR them
     */
    fms = new FeatureMatcherSet();
    assertEquals(fms.toStableString(), "");
    fms.or(fm1);
    assertEquals(fms.toStableString(), "AF LT 1.2");
    fms.or(fm2);
    assertEquals(fms.toStableString(),
            "(AF LT 1.2) OR (CLIN_SIG NotContains path)");

    /*
     * attribute or value including space is quoted
     */
    FeatureMatcher fm3 = FeatureMatcher.byAttribute(Condition.NotMatches,
            "foo bar", "CSQ", "Poly Phen");
    assertEquals(fm3.toStableString(),
            "'CSQ:Poly Phen' NotMatches 'foo bar'");
    fms.or(fm3);
    assertEquals(fms.toStableString(),
            "(AF LT 1.2) OR (CLIN_SIG NotContains path) OR ('CSQ:Poly Phen' NotMatches 'foo bar')");

    try
    {
      fms.and(fm1);
      fail("Expected exception");
    } catch (IllegalStateException e)
    {
      // expected
    }
  }

  /**
   * Tests for parsing a string representation of a FeatureMatcherSet
   * 
   * @see FeatureMatcherSetTest#testToStableString()
   */
  @Test(groups = "Functional")
  public void testFromString()
  {
    String descriptor = "AF LT 1.2";
    FeatureMatcherSetI fms = FeatureMatcherSet.fromString(descriptor);

    /*
     * shortcut asserts by verifying a 'roundtrip', 
     * which we trust if other tests pass :-)
     */
    assertEquals(fms.toStableString(), descriptor);

    // brackets optional, quotes optional, condition case insensitive
    fms = FeatureMatcherSet.fromString("('AF' lt '1.2')");
    assertEquals(fms.toStableString(), descriptor);

    descriptor = "(AF LT 1.2) AND (CLIN_SIG NotContains path)";
    fms = FeatureMatcherSet.fromString(descriptor);
    assertEquals(fms.toStableString(), descriptor);

    // AND is not case-sensitive
    fms = FeatureMatcherSet
            .fromString("(AF LT 1.2) and (CLIN_SIG NotContains path)");
    assertEquals(fms.toStableString(), descriptor);

    descriptor = "(AF LT 1.2) OR (CLIN_SIG NotContains path)";
    fms = FeatureMatcherSet.fromString(descriptor);
    assertEquals(fms.toStableString(), descriptor);

    // OR is not case-sensitive
    fms = FeatureMatcherSet
            .fromString("(AF LT 1.2) or (CLIN_SIG NotContains path)");
    assertEquals(fms.toStableString(), descriptor);

    // can get away without brackets on last match condition
    fms = FeatureMatcherSet
            .fromString("(AF LT 1.2) or CLIN_SIG NotContains path");
    assertEquals(fms.toStableString(), descriptor);

    descriptor = "(AF LT 1.2) OR (CLIN_SIG NotContains path) OR ('CSQ:Poly Phen' NotMatches 'foo bar')";
    fms = FeatureMatcherSet.fromString(descriptor);
    assertEquals(fms.toStableString(), descriptor);

    // can't mix OR and AND
    descriptor = "(AF LT 1.2) OR (CLIN_SIG NotContains path) AND ('CSQ:Poly Phen' NotMatches 'foo bar')";
    assertNull(FeatureMatcherSet.fromString(descriptor));

    // can't mix AND and OR
    descriptor = "(AF LT 1.2) and (CLIN_SIG NotContains path) or ('CSQ:Poly Phen' NotMatches 'foo bar')";
    assertNull(FeatureMatcherSet.fromString(descriptor));

    // brackets missing
    assertNull(FeatureMatcherSet
            .fromString("AF LT 1.2 or CLIN_SIG NotContains path"));

    // invalid conjunction
    assertNull(FeatureMatcherSet.fromString("(AF LT 1.2) but (AF GT -2)"));

    // unbalanced quote (1)
    assertNull(FeatureMatcherSet.fromString("('AF lt '1.2')"));

    // unbalanced quote (2)
    assertNull(FeatureMatcherSet.fromString("('AF' lt '1.2)"));
  }
}
