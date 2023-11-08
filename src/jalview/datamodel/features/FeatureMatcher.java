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
import jalview.util.matcher.Condition;
import jalview.util.matcher.Matcher;
import jalview.util.matcher.MatcherI;

/**
 * An immutable class that models one or more match conditions, each of which is
 * applied to the value obtained by lookup given the match key.
 * <p>
 * For example, the value provider could be a SequenceFeature's attributes map,
 * and the conditions might be
 * <ul>
 * <li>CSQ contains "pathological"</li>
 * <li>AND</li>
 * <li>AF <= 1.0e-5</li>
 * </ul>
 * 
 * @author gmcarstairs
 *
 */
public class FeatureMatcher implements FeatureMatcherI
{
  private static final String SCORE = "Score";

  private static final String LABEL = "Label";

  private static final String SPACE = " ";

  private static final String QUOTE = "'";

  /*
   * a dummy matcher that comes in useful for the 'add a filter' gui row
   */
  public static final FeatureMatcherI NULL_MATCHER = FeatureMatcher
          .byLabel(Condition.values()[0], "");

  private static final String COLON = ":";

  /*
   * if true, match is against feature description
   */
  final private boolean byLabel;

  /*
   * if true, match is against feature score
   */
  final private boolean byScore;

  /*
   * if not null, match is against feature attribute [sub-attribute]
   */
  final private String[] key;

  final private MatcherI matcher;

  /**
   * A helper method that converts a 'compound' attribute name from its display
   * form, e.g. CSQ:PolyPhen to array form, e.g. { "CSQ", "PolyPhen" }
   * 
   * @param attribute
   * @return
   */
  public static String[] fromAttributeDisplayName(String attribute)
  {
    return attribute == null ? null : attribute.split(COLON);
  }

  /**
   * A helper method that converts a 'compound' attribute name to its display
   * form, e.g. CSQ:PolyPhen from its array form, e.g. { "CSQ", "PolyPhen" }
   * 
   * @param attName
   * @return
   */
  public static String toAttributeDisplayName(String[] attName)
  {
    return attName == null ? "" : String.join(COLON, attName);
  }

  /**
   * A factory constructor that converts a stringified object (as output by
   * toStableString) to an object instance. Returns null if parsing fails.
   * <p>
   * Leniency in parsing (for manually created feature files):
   * <ul>
   * <li>keywords Score and Label, and the condition, are not
   * case-sensitive</li>
   * <li>quotes around value and pattern are optional if string does not include
   * a space</li>
   * </ul>
   * 
   * @param descriptor
   * @return
   */
  public static FeatureMatcher fromString(final String descriptor)
  {
    String invalidFormat = "Invalid matcher format: " + descriptor;

    /*
     * expect 
     * value condition pattern
     * where value is Label or Space or attributeName or attName1:attName2
     * and pattern is a float value as string, or a text string
     * attribute names or patterns may be quoted (must be if include space)
     */
    String attName = null;
    boolean byScore = false;
    boolean byLabel = false;
    Condition cond = null;
    String pattern = null;

    /*
     * parse first field (Label / Score / attribute)
     * optionally in quotes (required if attName includes space)
     */
    String leftToParse = descriptor;
    String firstField = null;

    if (descriptor.startsWith(QUOTE))
    {
      // 'Label' / 'Score' / 'attName'
      int nextQuotePos = descriptor.indexOf(QUOTE, 1);
      if (nextQuotePos == -1)
      {
        System.err.println(invalidFormat);
        return null;
      }
      firstField = descriptor.substring(1, nextQuotePos);
      leftToParse = descriptor.substring(nextQuotePos + 1).trim();
    }
    else
    {
      // Label / Score / attName (unquoted)
      int nextSpacePos = descriptor.indexOf(SPACE);
      if (nextSpacePos == -1)
      {
        System.err.println(invalidFormat);
        return null;
      }
      firstField = descriptor.substring(0, nextSpacePos);
      leftToParse = descriptor.substring(nextSpacePos + 1).trim();
    }
    String lower = firstField.toLowerCase(Locale.ROOT);
    if (lower.startsWith(LABEL.toLowerCase(Locale.ROOT)))
    {
      byLabel = true;
    }
    else if (lower.startsWith(SCORE.toLowerCase(Locale.ROOT)))
    {
      byScore = true;
    }
    else
    {
      attName = firstField;
    }

    /*
     * next field is the comparison condition
     * most conditions require a following pattern (optionally quoted)
     * although some conditions e.g. Present do not
     */
    int nextSpacePos = leftToParse.indexOf(SPACE);
    if (nextSpacePos == -1)
    {
      /*
       * no value following condition - only valid for some conditions
       */
      cond = Condition.fromString(leftToParse);
      if (cond == null || cond.needsAPattern())
      {
        System.err.println(invalidFormat);
        return null;
      }
    }
    else
    {
      /*
       * condition and pattern
       */
      cond = Condition.fromString(leftToParse.substring(0, nextSpacePos));
      leftToParse = leftToParse.substring(nextSpacePos + 1).trim();
      if (leftToParse.startsWith(QUOTE))
      {
        // pattern in quotes
        if (leftToParse.endsWith(QUOTE))
        {
          pattern = leftToParse.substring(1, leftToParse.length() - 1);
        }
        else
        {
          // unbalanced quote
          System.err.println(invalidFormat);
          return null;
        }
      }
      else
      {
        // unquoted pattern
        pattern = leftToParse;
      }
    }

    /*
     * we have parsed out value, condition and pattern
     * so can now make the FeatureMatcher
     */
    try
    {
      if (byLabel)
      {
        return FeatureMatcher.byLabel(cond, pattern);
      }
      else if (byScore)
      {
        return FeatureMatcher.byScore(cond, pattern);
      }
      else
      {
        String[] attNames = FeatureMatcher
                .fromAttributeDisplayName(attName);
        return FeatureMatcher.byAttribute(cond, pattern, attNames);
      }
    } catch (NumberFormatException e)
    {
      // numeric condition with non-numeric pattern
      return null;
    }
  }

  /**
   * A factory constructor method for a matcher that applies its match condition
   * to the feature label (description)
   * 
   * @param cond
   * @param pattern
   * @return
   * @throws NumberFormatException
   *           if an invalid numeric pattern is supplied
   */
  public static FeatureMatcher byLabel(Condition cond, String pattern)
  {
    return new FeatureMatcher(new Matcher(cond, pattern), true, false,
            null);
  }

  /**
   * A factory constructor method for a matcher that applies its match condition
   * to the feature score
   * 
   * @param cond
   * @param pattern
   * @return
   * @throws NumberFormatException
   *           if an invalid numeric pattern is supplied
   */
  public static FeatureMatcher byScore(Condition cond, String pattern)
  {
    return new FeatureMatcher(new Matcher(cond, pattern), false, true,
            null);
  }

  /**
   * A factory constructor method for a matcher that applies its match condition
   * to the named feature attribute [and optional sub-attribute]
   * 
   * @param cond
   * @param pattern
   * @param attName
   * @return
   * @throws NumberFormatException
   *           if an invalid numeric pattern is supplied
   */
  public static FeatureMatcher byAttribute(Condition cond, String pattern,
          String... attName)
  {
    return new FeatureMatcher(new Matcher(cond, pattern), false, false,
            attName);
  }

  private FeatureMatcher(Matcher m, boolean forLabel, boolean forScore,
          String[] theKey)
  {
    key = theKey;
    matcher = m;
    byLabel = forLabel;
    byScore = forScore;
  }

  @Override
  public boolean matches(SequenceFeature feature)
  {
    String value = byLabel ? feature.getDescription()
            : (byScore ? String.valueOf(feature.getScore())
                    : feature.getValueAsString(key));
    return matcher.matches(value);
  }

  @Override
  public String[] getAttribute()
  {
    return key;
  }

  @Override
  public MatcherI getMatcher()
  {
    return matcher;
  }

  /**
   * Answers a string description of this matcher, suitable for display,
   * debugging or logging. The format may change in future.
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    if (byLabel)
    {
      sb.append(MessageManager.getString("label.label"));
    }
    else if (byScore)
    {
      sb.append(MessageManager.getString("label.score"));
    }
    else
    {
      sb.append(String.join(COLON, key));
    }

    Condition condition = matcher.getCondition();
    sb.append(SPACE).append(condition.toString().toLowerCase(Locale.ROOT));
    if (condition.isNumeric())
    {
      sb.append(SPACE).append(matcher.getPattern());
    }
    else if (condition.needsAPattern())
    {
      sb.append(" '").append(matcher.getPattern()).append(QUOTE);
    }

    return sb.toString();
  }

  @Override
  public boolean isByLabel()
  {
    return byLabel;
  }

  @Override
  public boolean isByScore()
  {
    return byScore;
  }

  @Override
  public boolean isByAttribute()
  {
    return getAttribute() != null;
  }

  /**
   * {@inheritDoc} The output of this method should be parseable by method
   * <code>fromString<code> to restore the original object.
   */
  @Override
  public String toStableString()
  {
    StringBuilder sb = new StringBuilder();
    if (byLabel)
    {
      sb.append(LABEL); // no i18n here unlike toString() !
    }
    else if (byScore)
    {
      sb.append(SCORE);
    }
    else
    {
      /*
       * enclose attribute name in quotes if it includes space
       */
      String displayName = toAttributeDisplayName(key);
      if (displayName.contains(SPACE))
      {
        sb.append(QUOTE).append(displayName).append(QUOTE);
      }
      else
      {
        sb.append(displayName);
      }
    }

    Condition condition = matcher.getCondition();
    sb.append(SPACE).append(condition.getStableName());
    String pattern = matcher.getPattern();
    if (condition.needsAPattern())
    {
      /*
       * enclose pattern in quotes if it includes space
       */
      if (pattern.contains(SPACE))
      {
        sb.append(SPACE).append(QUOTE).append(pattern).append(QUOTE);
      }
      else
      {
        sb.append(SPACE).append(pattern);
      }
    }

    return sb.toString();
  }
}
