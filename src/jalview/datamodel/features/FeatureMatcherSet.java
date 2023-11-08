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

import java.util.Locale;

import jalview.datamodel.SequenceFeature;
import jalview.util.MessageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that models one or more match conditions, which may be combined with
 * AND or OR (but not a mixture)
 * 
 * @author gmcarstairs
 */
public class FeatureMatcherSet implements FeatureMatcherSetI
{
  private static final String OR = "OR";

  private static final String AND = "AND";

  private static final String SPACE = " ";

  private static final String CLOSE_BRACKET = ")";

  private static final String OPEN_BRACKET = "(";

  private static final String OR_I18N = MessageManager
          .getString("label.or");

  private static final String AND_18N = MessageManager
          .getString("label.and");

  List<FeatureMatcherI> matchConditions;

  boolean andConditions;

  /**
   * A factory constructor that converts a stringified object (as output by
   * toStableString) to an object instance.
   * 
   * Format:
   * <ul>
   * <li>(condition1) AND (condition2) AND (condition3)</li>
   * <li>or</li>
   * <li>(condition1) OR (condition2) OR (condition3)</li>
   * </ul>
   * where OR and AND are not case-sensitive, and may not be mixed. Brackets are
   * optional if there is only one condition.
   * 
   * @param descriptor
   * @return
   * @see FeatureMatcher#fromString(String)
   */
  public static FeatureMatcherSet fromString(final String descriptor)
  {
    String invalid = "Invalid descriptor: " + descriptor;
    boolean firstCondition = true;
    FeatureMatcherSet result = new FeatureMatcherSet();

    String leftToParse = descriptor.trim();

    while (leftToParse.length() > 0)
    {
      /*
       * inspect AND or OR condition, check not mixed
       */
      boolean and = true;
      if (!firstCondition)
      {
        int spacePos = leftToParse.indexOf(SPACE);
        if (spacePos == -1)
        {
          // trailing junk after a match condition
          System.err.println(invalid);
          return null;
        }
        String conjunction = leftToParse.substring(0, spacePos);
        leftToParse = leftToParse.substring(spacePos + 1).trim();
        if (conjunction.equalsIgnoreCase(AND))
        {
          and = true;
        }
        else if (conjunction.equalsIgnoreCase(OR))
        {
          and = false;
        }
        else
        {
          // not an AND or an OR - invalid
          System.err.println(invalid);
          return null;
        }
      }

      /*
       * now extract the next condition and AND or OR it
       */
      String nextCondition = leftToParse;
      if (leftToParse.startsWith(OPEN_BRACKET))
      {
        int closePos = leftToParse.indexOf(CLOSE_BRACKET);
        if (closePos == -1)
        {
          System.err.println(invalid);
          return null;
        }
        nextCondition = leftToParse.substring(1, closePos);
        leftToParse = leftToParse.substring(closePos + 1).trim();
      }
      else
      {
        leftToParse = "";
      }

      FeatureMatcher fm = FeatureMatcher.fromString(nextCondition);
      if (fm == null)
      {
        System.err.println(invalid);
        return null;
      }
      try
      {
        if (and)
        {
          result.and(fm);
        }
        else
        {
          result.or(fm);
        }
        firstCondition = false;
      } catch (IllegalStateException e)
      {
        // thrown if OR and AND are mixed
        System.err.println(invalid);
        return null;
      }

    }
    return result;
  }

  /**
   * Constructor
   */
  public FeatureMatcherSet()
  {
    matchConditions = new ArrayList<>();
  }

  @Override
  public boolean matches(SequenceFeature feature)
  {
    /*
     * no conditions matches anything
     */
    if (matchConditions.isEmpty())
    {
      return true;
    }

    /*
     * AND until failure
     */
    if (andConditions)
    {
      for (FeatureMatcherI m : matchConditions)
      {
        if (!m.matches(feature))
        {
          return false;
        }
      }
      return true;
    }

    /*
     * OR until match
     */
    for (FeatureMatcherI m : matchConditions)
    {
      if (m.matches(feature))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public void and(FeatureMatcherI m)
  {
    if (!andConditions && matchConditions.size() > 1)
    {
      throw new IllegalStateException("Can't add an AND to OR conditions");
    }
    matchConditions.add(m);
    andConditions = true;
  }

  @Override
  public void or(FeatureMatcherI m)
  {
    if (andConditions && matchConditions.size() > 1)
    {
      throw new IllegalStateException("Can't add an OR to AND conditions");
    }
    matchConditions.add(m);
    andConditions = false;
  }

  @Override
  public boolean isAnded()
  {
    return andConditions;
  }

  @Override
  public Iterable<FeatureMatcherI> getMatchers()
  {
    return matchConditions;
  }

  /**
   * Answers a string representation of this object suitable for display, and
   * possibly internationalized. The format is not guaranteed stable and may
   * change in future.
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    boolean multiple = matchConditions.size() > 1;
    for (FeatureMatcherI matcher : matchConditions)
    {
      if (!first)
      {
        String joiner = andConditions ? AND_18N : OR_I18N;
        sb.append(SPACE).append(joiner.toLowerCase(Locale.ROOT))
                .append(SPACE);
      }
      first = false;
      if (multiple)
      {
        sb.append(OPEN_BRACKET).append(matcher.toString())
                .append(CLOSE_BRACKET);
      }
      else
      {
        sb.append(matcher.toString());
      }
    }
    return sb.toString();
  }

  @Override
  public boolean isEmpty()
  {
    return matchConditions == null || matchConditions.isEmpty();
  }

  /**
   * {@inheritDoc} The output of this method should be parseable by method
   * <code>fromString<code> to restore the original object.
   */
  @Override
  public String toStableString()
  {
    StringBuilder sb = new StringBuilder();
    boolean moreThanOne = matchConditions.size() > 1;
    boolean first = true;

    for (FeatureMatcherI matcher : matchConditions)
    {
      if (!first)
      {
        String joiner = andConditions ? AND : OR;
        sb.append(SPACE).append(joiner).append(SPACE);
      }
      first = false;
      if (moreThanOne)
      {
        sb.append(OPEN_BRACKET).append(matcher.toStableString())
                .append(CLOSE_BRACKET);
      }
      else
      {
        sb.append(matcher.toStableString());
      }
    }
    return sb.toString();
  }

}
