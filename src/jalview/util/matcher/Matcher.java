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

import java.util.Locale;

import java.util.Objects;

/**
 * A bean to describe one attribute-based filter
 */
public class Matcher implements MatcherI
{
  public enum PatternType
  {
    String, Integer, Float
  }

  /*
   * the comparison condition
   */
  private final Condition condition;

  /*
   * the string pattern as entered, to compare to
   */
  private String pattern;

  /*
   * the pattern in upper case, for non-case-sensitive matching
   */
  private final String uppercasePattern;

  /*
   * the compiled regex if using a pattern match condition
   * (possible future enhancement)
   */
  // private Pattern regexPattern;

  /*
   * the value to compare to for a numerical condition with a float pattern
   */
  private float floatValue = 0F;

  /*
   * the value to compare to for a numerical condition with an integer pattern
   */
  private long longValue = 0L;

  private PatternType patternType;

  /**
   * Constructor
   * 
   * @param cond
   * @param compareTo
   * @return
   * @throws NumberFormatException
   *           if a numerical condition is specified with a non-numeric
   *           comparison value
   * @throws NullPointerException
   *           if a null condition or comparison string is specified
   */
  public Matcher(Condition cond, String compareTo)
  {
    Objects.requireNonNull(cond);
    condition = cond;

    if (cond.isNumeric())
    {
      try
      {
        longValue = Long.valueOf(compareTo);
        pattern = String.valueOf(longValue);
        patternType = PatternType.Integer;
      } catch (NumberFormatException e)
      {
        floatValue = Float.valueOf(compareTo);
        pattern = String.valueOf(floatValue);
        patternType = PatternType.Float;
      }
    }
    else
    {
      pattern = compareTo;
      patternType = PatternType.String;
    }

    uppercasePattern = pattern == null ? null
            : pattern.toUpperCase(Locale.ROOT);

    // if we add regex conditions (e.g. matchesPattern), then
    // pattern should hold the raw regex, and
    // regexPattern = Pattern.compile(compareTo);
  }

  /**
   * Constructor for a float-valued numerical match condition. Note that if a
   * string comparison condition is specified, this will be converted to a
   * comparison with the float value as string
   * 
   * @param cond
   * @param compareTo
   */
  public Matcher(Condition cond, float compareTo)
  {
    this(cond, String.valueOf(compareTo));
  }

  /**
   * Constructor for an integer-valued numerical match condition. Note that if a
   * string comparison condition is specified, this will be converted to a
   * comparison with the integer value as string
   * 
   * @param cond
   * @param compareTo
   */
  public Matcher(Condition cond, long compareTo)
  {
    this(cond, String.valueOf(compareTo));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean matches(String compareTo)
  {
    if (compareTo == null)
    {
      return matchesNull();
    }

    boolean matched = false;
    switch (patternType)
    {
    case Float:
      matched = matchesFloat(compareTo, floatValue);
      break;
    case Integer:
      matched = matchesLong(compareTo);
      break;
    default:
      matched = matchesString(compareTo);
      break;
    }
    return matched;
  }

  /**
   * Executes a non-case-sensitive string comparison to the given value, after
   * trimming it. Returns true if the test passes, false if it fails.
   * 
   * @param compareTo
   * @return
   */
  boolean matchesString(String compareTo)
  {
    boolean matched = false;
    String upper = compareTo.toUpperCase(Locale.ROOT).trim();
    switch (condition)
    {
    case Matches:
      matched = upper.equals(uppercasePattern);
      break;
    case NotMatches:
      matched = !upper.equals(uppercasePattern);
      break;
    case Contains:
      matched = upper.indexOf(uppercasePattern) > -1;
      break;
    case NotContains:
      matched = upper.indexOf(uppercasePattern) == -1;
      break;
    case Present:
      matched = true;
      break;
    default:
      break;
    }
    return matched;
  }

  /**
   * Performs a numerical comparison match condition test against a float value
   * 
   * @param testee
   * @param compareTo
   * @return
   */
  boolean matchesFloat(String testee, float compareTo)
  {
    if (!condition.isNumeric())
    {
      // failsafe, shouldn't happen
      return matches(testee);
    }

    float f = 0f;
    try
    {
      f = Float.valueOf(testee);
    } catch (NumberFormatException e)
    {
      return false;
    }

    boolean matched = false;
    switch (condition)
    {
    case LT:
      matched = f < compareTo;
      break;
    case LE:
      matched = f <= compareTo;
      break;
    case EQ:
      matched = f == compareTo;
      break;
    case NE:
      matched = f != compareTo;
      break;
    case GT:
      matched = f > compareTo;
      break;
    case GE:
      matched = f >= compareTo;
      break;
    default:
      break;
    }

    return matched;
  }

  /**
   * A simple hash function that guarantees that when two objects are equal,
   * they have the same hashcode
   */
  @Override
  public int hashCode()
  {
    return pattern.hashCode() + condition.hashCode() + (int) floatValue;
  }

  /**
   * equals is overridden so that we can safely remove Matcher objects from
   * collections (e.g. delete an attribute match condition for a feature colour)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof Matcher))
    {
      return false;
    }
    Matcher m = (Matcher) obj;
    if (condition != m.condition || floatValue != m.floatValue
            || longValue != m.longValue)
    {
      return false;
    }
    if (pattern == null)
    {
      return m.pattern == null;
    }
    return uppercasePattern.equals(m.uppercasePattern);
  }

  @Override
  public Condition getCondition()
  {
    return condition;
  }

  @Override
  public String getPattern()
  {
    return pattern;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(condition.toString()).append(" ");
    if (condition.isNumeric())
    {
      sb.append(pattern);
    }
    else
    {
      sb.append("'").append(pattern).append("'");
    }

    return sb.toString();
  }

  /**
   * Performs a numerical comparison match condition test against an integer
   * value
   * 
   * @param compareTo
   * @return
   */
  boolean matchesLong(String compareTo)
  {
    if (!condition.isNumeric())
    {
      // failsafe, shouldn't happen
      return matches(String.valueOf(compareTo));
    }

    long val = 0L;
    try
    {
      val = Long.valueOf(compareTo);
    } catch (NumberFormatException e)
    {
      /*
       * try the presented value as a float instead
       */
      return matchesFloat(compareTo, longValue);
    }

    boolean matched = false;
    switch (condition)
    {
    case LT:
      matched = val < longValue;
      break;
    case LE:
      matched = val <= longValue;
      break;
    case EQ:
      matched = val == longValue;
      break;
    case NE:
      matched = val != longValue;
      break;
    case GT:
      matched = val > longValue;
      break;
    case GE:
      matched = val >= longValue;
      break;
    default:
      break;
    }

    return matched;
  }

  /**
   * Tests whether a null value matches the condition. The rule is that any
   * numeric condition is failed, and only 'negative' string conditions are
   * matched. So for example <br>
   * {@code null contains "damaging"}<br>
   * fails, but <br>
   * {@code null does not contain "damaging"}</br>
   * passes.
   */
  boolean matchesNull()
  {
    if (condition.isNumeric())
    {
      return false;
    }
    else
    {
      return condition == Condition.NotContains
              || condition == Condition.NotMatches
              || condition == Condition.NotPresent;
    }
  }
}
