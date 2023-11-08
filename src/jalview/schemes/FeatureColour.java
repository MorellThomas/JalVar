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

import java.util.Locale;

import jalview.api.FeatureColourI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.features.FeatureMatcher;
import jalview.util.ColorUtils;
import jalview.util.Format;
import jalview.util.MessageManager;

import java.awt.Color;
import java.util.StringTokenizer;

/**
 * A class that represents a colour scheme for a feature type. Options supported
 * are currently
 * <ul>
 * <li>a simple colour e.g. Red</li>
 * <li>colour by label - a colour is generated from the feature description</li>
 * <li>graduated colour by feature score</li>
 * <ul>
 * <li>minimum and maximum score range must be provided</li>
 * <li>minimum and maximum value colours should be specified</li>
 * <li>a colour for 'no value' may optionally be provided</li>
 * <li>colours for intermediate scores are interpolated RGB values</li>
 * <li>there is an optional threshold above/below which to colour values</li>
 * <li>the range may be the full value range, or may be limited by the threshold
 * value</li>
 * </ul>
 * <li>colour by (text) value of a named attribute</li>
 * <li>graduated colour by (numeric) value of a named attribute</li>
 * </ul>
 */
public class FeatureColour implements FeatureColourI
{
  private static final String I18N_LABEL = MessageManager
          .getString("label.label");

  private static final String I18N_SCORE = MessageManager
          .getString("label.score");

  private static final String ABSOLUTE = "abso";

  private static final String ABOVE = "above";

  private static final String BELOW = "below";

  /*
   * constants used to read or write a Jalview Features file
   */
  private static final String LABEL = "label";

  private static final String SCORE = "score";

  private static final String ATTRIBUTE = "attribute";

  private static final String NO_VALUE_MIN = "noValueMin";

  private static final String NO_VALUE_MAX = "noValueMax";

  private static final String NO_VALUE_NONE = "noValueNone";

  static final Color DEFAULT_NO_COLOUR = null;

  private static final String BAR = "|";

  final private Color colour;

  final private Color minColour;

  final private Color maxColour;

  /*
   * colour to use for colour by attribute when the 
   * attribute value is absent
   */
  final private Color noColour;

  /*
   * if true, then colour has a gradient based on a numerical 
   * range (either feature score, or an attribute value)
   */
  private boolean graduatedColour;

  /*
   * if true, colour values are generated from a text string,
   * either feature description, or an attribute value
   */
  private boolean colourByLabel;

  /*
   * if not null, the value of [attribute, [sub-attribute] ...]
   *  is used for colourByLabel or graduatedColour
   */
  private String[] attributeName;

  private float threshold;

  private float base;

  private float range;

  private boolean belowThreshold;

  private boolean aboveThreshold;

  private boolean isHighToLow;

  private boolean autoScaled;

  final private float minRed;

  final private float minGreen;

  final private float minBlue;

  final private float deltaRed;

  final private float deltaGreen;

  final private float deltaBlue;

  /**
   * Parses a Jalview features file format colour descriptor
   * <p>
   * <code>
   * [label|score|[attribute|attributeName]|][mincolour|maxcolour|
   * [absolute|]minvalue|maxvalue|[noValueOption|]thresholdtype|thresholdvalue]</code>
   * <p>
   * 'Score' is optional (default) for a graduated colour. An attribute with
   * sub-attribute should be written as (for example) CSQ:Consequence.
   * noValueOption is one of <code>noValueMin, noValueMax, noValueNone</code>
   * with default noValueMin.
   * <p>
   * Examples:
   * <ul>
   * <li>red</li>
   * <li>a28bbb</li>
   * <li>25,125,213</li>
   * <li>label</li>
   * <li>attribute|CSQ:PolyPhen</li>
   * <li>label|||0.0|0.0|above|12.5</li>
   * <li>label|||0.0|0.0|below|12.5</li>
   * <li>red|green|12.0|26.0|none</li>
   * <li>score|red|green|12.0|26.0|none</li>
   * <li>attribute|AF|red|green|12.0|26.0|none</li>
   * <li>attribute|AF|red|green|noValueNone|12.0|26.0|none</li>
   * <li>a28bbb|3eb555|12.0|26.0|above|12.5</li>
   * <li>a28bbb|3eb555|abso|12.0|26.0|below|12.5</li>
   * </ul>
   * 
   * @param descriptor
   * @return
   * @throws IllegalArgumentException
   *           if not parseable
   */
  public static FeatureColourI parseJalviewFeatureColour(String descriptor)
  {
    StringTokenizer gcol = new StringTokenizer(descriptor, BAR, true);
    float min = Float.MIN_VALUE;
    float max = Float.MAX_VALUE;
    boolean byLabel = false;
    boolean byAttribute = false;
    String attName = null;
    String mincol = null;
    String maxcol = null;

    /*
     * first token should be 'label', or 'score', or an
     * attribute name, or simple colour, or minimum colour
     */
    String nextToken = gcol.nextToken();
    if (nextToken == BAR)
    {
      throw new IllegalArgumentException(
              "Expected either 'label' or a colour specification in the line: "
                      + descriptor);
    }
    if (nextToken.toLowerCase(Locale.ROOT).startsWith(LABEL))
    {
      byLabel = true;
      // get the token after the next delimiter:
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
    }
    else if (nextToken.toLowerCase(Locale.ROOT).startsWith(SCORE))
    {
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
    }
    else if (nextToken.toLowerCase(Locale.ROOT).startsWith(ATTRIBUTE))
    {
      byAttribute = true;
      attName = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
      attName = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
      mincol = (gcol.hasMoreTokens() ? gcol.nextToken() : null);
    }
    else
    {
      mincol = nextToken;
    }

    /*
     * if only one token, it can validly be label, attributeName,
     * or a plain colour value
     */
    if (!gcol.hasMoreTokens())
    {
      if (byLabel || byAttribute)
      {
        FeatureColourI fc = new FeatureColour();
        fc.setColourByLabel(true);
        if (byAttribute)
        {
          fc.setAttributeName(
                  FeatureMatcher.fromAttributeDisplayName(attName));
        }
        return fc;
      }

      Color colour = ColorUtils.parseColourString(descriptor);
      if (colour == null)
      {
        throw new IllegalArgumentException(
                "Invalid colour descriptor: " + descriptor);
      }
      return new FeatureColour(colour);
    }

    /*
     * continue parsing for min/max/no colour (if graduated)
     * and for threshold (colour by text or graduated)
     */

    /*
     * autoScaled == true: colours range over actual score range
     * autoScaled == false ('abso'): colours range over min/max range
     */
    boolean autoScaled = true;
    String tok = null, minval, maxval;
    String noValueColour = NO_VALUE_MIN;

    if (mincol != null)
    {
      // at least four more tokens
      if (mincol.equals(BAR))
      {
        mincol = null;
      }
      else
      {
        gcol.nextToken(); // skip next '|'
      }
      maxcol = gcol.nextToken();
      if (maxcol.equals(BAR))
      {
        maxcol = null;
      }
      else
      {
        gcol.nextToken(); // skip next '|'
      }
      tok = gcol.nextToken();

      /*
       * check for specifier for colour for no attribute value
       * (new in 2.11, defaults to minColour if not specified)
       */
      if (tok.equalsIgnoreCase(NO_VALUE_MIN))
      {
        tok = gcol.nextToken();
        tok = gcol.nextToken();
      }
      else if (tok.equalsIgnoreCase(NO_VALUE_MAX))
      {
        noValueColour = NO_VALUE_MAX;
        tok = gcol.nextToken();
        tok = gcol.nextToken();
      }
      else if (tok.equalsIgnoreCase(NO_VALUE_NONE))
      {
        noValueColour = NO_VALUE_NONE;
        tok = gcol.nextToken();
        tok = gcol.nextToken();
      }

      gcol.nextToken(); // skip next '|'
      if (tok.toLowerCase(Locale.ROOT).startsWith(ABSOLUTE))
      {
        minval = gcol.nextToken();
        gcol.nextToken(); // skip next '|'
        autoScaled = false;
      }
      else
      {
        minval = tok;
      }
      maxval = gcol.nextToken();
      if (gcol.hasMoreTokens())
      {
        gcol.nextToken(); // skip next '|'
      }
      try
      {
        if (minval.length() > 0)
        {
          min = Float.valueOf(minval).floatValue();
        }
      } catch (Exception e)
      {
        throw new IllegalArgumentException(
                "Couldn't parse the minimum value for graduated colour ('"
                        + minval + "')");
      }
      try
      {
        if (maxval.length() > 0)
        {
          max = Float.valueOf(maxval).floatValue();
        }
      } catch (Exception e)
      {
        throw new IllegalArgumentException(
                "Couldn't parse the maximum value for graduated colour ("
                        + descriptor + ")");
      }
    }
    else
    {
      /*
       * dummy min/max colours for colour by text
       * (label or attribute value)
       */
      mincol = "white";
      maxcol = "black";
      byLabel = true;
    }

    /*
     * construct the FeatureColour!
     */
    FeatureColour featureColour;
    try
    {
      Color minColour = ColorUtils.parseColourString(mincol);
      Color maxColour = ColorUtils.parseColourString(maxcol);
      Color noColour = noValueColour.equals(NO_VALUE_MAX) ? maxColour
              : (noValueColour.equals(NO_VALUE_NONE) ? null : minColour);
      featureColour = new FeatureColour(maxColour, minColour, maxColour,
              noColour, min, max);
      featureColour.setColourByLabel(minColour == null);
      featureColour.setAutoScaled(autoScaled);
      if (byAttribute)
      {
        featureColour.setAttributeName(
                FeatureMatcher.fromAttributeDisplayName(attName));
      }
      // add in any additional parameters
      String ttype = null, tval = null;
      if (gcol.hasMoreTokens())
      {
        // threshold type and possibly a threshold value
        ttype = gcol.nextToken();
        if (ttype.toLowerCase(Locale.ROOT).startsWith(BELOW))
        {
          featureColour.setBelowThreshold(true);
        }
        else if (ttype.toLowerCase(Locale.ROOT).startsWith(ABOVE))
        {
          featureColour.setAboveThreshold(true);
        }
        else
        {
          if (!ttype.toLowerCase(Locale.ROOT).startsWith("no"))
          {
            System.err.println(
                    "Ignoring unrecognised threshold type : " + ttype);
          }
        }
      }
      if (featureColour.hasThreshold())
      {
        try
        {
          gcol.nextToken();
          tval = gcol.nextToken();
          featureColour.setThreshold(Float.valueOf(tval).floatValue());
        } catch (Exception e)
        {
          System.err.println("Couldn't parse threshold value as a float: ("
                  + tval + ")");
        }
      }
      if (gcol.hasMoreTokens())
      {
        System.err.println(
                "Ignoring additional tokens in parameters in graduated colour specification\n");
        while (gcol.hasMoreTokens())
        {
          System.err.println(BAR + gcol.nextToken());
        }
        System.err.println("\n");
      }
      return featureColour;
    } catch (Exception e)
    {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Default constructor
   */
  public FeatureColour()
  {
    this((Color) null);
  }

  /**
   * Constructor given a simple colour. This also 'primes' a graduated colour
   * range, where the maximum colour is the given simple colour, and the minimum
   * colour a paler shade of it. This is for convenience when switching from a
   * simple colour to a graduated colour scheme.
   * 
   * @param c
   */
  public FeatureColour(Color c)
  {
    /*
     * set max colour to the simple colour, min colour to a paler shade of it
     */
    this(c, c == null ? Color.white : ColorUtils.bleachColour(c, 0.9f),
            c == null ? Color.black : c, DEFAULT_NO_COLOUR, 0, 0);

    /*
     * but enforce simple colour for now!
     */
    setGraduatedColour(false);
  }

  /**
   * Copy constructor
   * 
   * @param fc
   */
  public FeatureColour(FeatureColour fc)
  {
    graduatedColour = fc.graduatedColour;
    colour = fc.colour;
    minColour = fc.minColour;
    maxColour = fc.maxColour;
    noColour = fc.noColour;
    minRed = fc.minRed;
    minGreen = fc.minGreen;
    minBlue = fc.minBlue;
    deltaRed = fc.deltaRed;
    deltaGreen = fc.deltaGreen;
    deltaBlue = fc.deltaBlue;
    base = fc.base;
    range = fc.range;
    isHighToLow = fc.isHighToLow;
    attributeName = fc.attributeName;
    setAboveThreshold(fc.isAboveThreshold());
    setBelowThreshold(fc.isBelowThreshold());
    setThreshold(fc.getThreshold());
    setAutoScaled(fc.isAutoScaled());
    setColourByLabel(fc.isColourByLabel());
  }

  /**
   * Constructor that sets both simple and graduated colour values. This allows
   * alternative colour schemes to be 'preserved' while switching between them
   * to explore their effects on the visualisation.
   * <p>
   * This sets the colour scheme to 'graduated' by default. Override this if
   * wanted by calling <code>setGraduatedColour(false)</code> for a simple
   * colour, or <code>setColourByLabel(true)</code> for colour by label.
   * 
   * @param myColour
   * @param low
   * @param high
   * @param noValueColour
   * @param min
   * @param max
   */
  public FeatureColour(Color myColour, Color low, Color high,
          Color noValueColour, float min, float max)
  {
    if (low == null)
    {
      low = Color.white;
    }
    if (high == null)
    {
      high = Color.black;
    }
    colour = myColour;
    minColour = low;
    maxColour = high;
    setGraduatedColour(true);
    noColour = noValueColour;
    threshold = Float.NaN;
    isHighToLow = min >= max;
    minRed = low.getRed() / 255f;
    minGreen = low.getGreen() / 255f;
    minBlue = low.getBlue() / 255f;
    deltaRed = (high.getRed() / 255f) - minRed;
    deltaGreen = (high.getGreen() / 255f) - minGreen;
    deltaBlue = (high.getBlue() / 255f) - minBlue;
    if (isHighToLow)
    {
      base = max;
      range = min - max;
    }
    else
    {
      base = min;
      range = max - min;
    }
  }

  @Override
  public boolean isGraduatedColour()
  {
    return graduatedColour;
  }

  /**
   * Sets the 'graduated colour' flag. If true, also sets 'colour by label' to
   * false.
   */
  public void setGraduatedColour(boolean b)
  {
    graduatedColour = b;
    if (b)
    {
      setColourByLabel(false);
    }
  }

  @Override
  public Color getColour()
  {
    return colour;
  }

  @Override
  public Color getMinColour()
  {
    return minColour;
  }

  @Override
  public Color getMaxColour()
  {
    return maxColour;
  }

  @Override
  public Color getNoColour()
  {
    return noColour;
  }

  @Override
  public boolean isColourByLabel()
  {
    return colourByLabel;
  }

  /**
   * Sets the 'colour by label' flag. If true, also sets 'graduated colour' to
   * false.
   */
  @Override
  public void setColourByLabel(boolean b)
  {
    colourByLabel = b;
    if (b)
    {
      setGraduatedColour(false);
    }
  }

  @Override
  public boolean isBelowThreshold()
  {
    return belowThreshold;
  }

  @Override
  public void setBelowThreshold(boolean b)
  {
    belowThreshold = b;
    if (b)
    {
      setAboveThreshold(false);
    }
  }

  @Override
  public boolean isAboveThreshold()
  {
    return aboveThreshold;
  }

  @Override
  public void setAboveThreshold(boolean b)
  {
    aboveThreshold = b;
    if (b)
    {
      setBelowThreshold(false);
    }
  }

  @Override
  public float getThreshold()
  {
    return threshold;
  }

  @Override
  public void setThreshold(float f)
  {
    threshold = f;
  }

  @Override
  public boolean isAutoScaled()
  {
    return autoScaled;
  }

  @Override
  public void setAutoScaled(boolean b)
  {
    this.autoScaled = b;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateBounds(float min, float max)
  {
    if (max < min)
    {
      base = max;
      range = min - max;
      isHighToLow = true;
    }
    else
    {
      base = min;
      range = max - min;
      isHighToLow = false;
    }
  }

  /**
   * Returns the colour for the given instance of the feature. This may be a
   * simple colour, a colour generated from the feature description or other
   * attribute (if isColourByLabel()), or a colour derived from the feature
   * score or other attribute (if isGraduatedColour()).
   * <p>
   * Answers null if feature score (or attribute) value lies outside a
   * configured threshold.
   * 
   * @param feature
   * @return
   */
  @Override
  public Color getColor(SequenceFeature feature)
  {
    if (isColourByLabel())
    {
      String label = attributeName == null ? feature.getDescription()
              : feature.getValueAsString(attributeName);
      return label == null ? noColour
              : ColorUtils.createColourFromName(label);
    }

    if (!isGraduatedColour())
    {
      return getColour();
    }

    /*
     * graduated colour case, optionally with threshold
     * may be based on feature score on an attribute value
     * Float.NaN, or no value, is assigned the 'no value' colour
     */
    float scr = feature.getScore();
    if (attributeName != null)
    {
      try
      {
        String attVal = feature.getValueAsString(attributeName);
        scr = Float.valueOf(attVal);
      } catch (Throwable e)
      {
        scr = Float.NaN;
      }
    }
    if (Float.isNaN(scr))
    {
      return noColour;
    }

    if (isAboveThreshold() && scr <= threshold)
    {
      return null;
    }

    if (isBelowThreshold() && scr >= threshold)
    {
      return null;
    }
    if (range == 0.0)
    {
      return getMaxColour();
    }
    float scl = (scr - base) / range;
    if (isHighToLow)
    {
      scl = -scl;
    }
    if (scl < 0f)
    {
      scl = 0f;
    }
    if (scl > 1f)
    {
      scl = 1f;
    }
    return new Color(minRed + scl * deltaRed, minGreen + scl * deltaGreen,
            minBlue + scl * deltaBlue);
  }

  /**
   * Returns the maximum score of the graduated colour range
   * 
   * @return
   */
  @Override
  public float getMax()
  {
    // regenerate the original values passed in to the constructor
    return (isHighToLow) ? base : (base + range);
  }

  /**
   * Returns the minimum score of the graduated colour range
   * 
   * @return
   */
  @Override
  public float getMin()
  {
    // regenerate the original value passed in to the constructor
    return (isHighToLow) ? (base + range) : base;
  }

  @Override
  public boolean isSimpleColour()
  {
    return (!isColourByLabel() && !isGraduatedColour());
  }

  @Override
  public boolean hasThreshold()
  {
    return isAboveThreshold() || isBelowThreshold();
  }

  @Override
  public String toJalviewFormat(String featureType)
  {
    String colourString = null;
    if (isSimpleColour())
    {
      colourString = Format.getHexString(getColour());
    }
    else
    {
      StringBuilder sb = new StringBuilder(32);
      if (isColourByAttribute())
      {
        sb.append(ATTRIBUTE).append(BAR);
        sb.append(
                FeatureMatcher.toAttributeDisplayName(getAttributeName()));
      }
      else if (isColourByLabel())
      {
        sb.append(LABEL);
      }
      else
      {
        sb.append(SCORE);
      }
      if (isGraduatedColour())
      {
        sb.append(BAR).append(Format.getHexString(getMinColour()))
                .append(BAR);
        sb.append(Format.getHexString(getMaxColour())).append(BAR);

        /*
         * 'no value' colour should be null, min or max colour;
         * if none of these, coerce to minColour
         */
        String noValue = NO_VALUE_MIN;
        if (maxColour.equals(noColour))
        {
          noValue = NO_VALUE_MAX;
        }
        if (noColour == null)
        {
          noValue = NO_VALUE_NONE;
        }
        sb.append(noValue).append(BAR);
        if (!isAutoScaled())
        {
          sb.append(ABSOLUTE).append(BAR);
        }
      }
      else
      {
        /*
         * colour by text with score threshold: empty fields for
         * minColour and maxColour (not used)
         */
        if (hasThreshold())
        {
          sb.append(BAR).append(BAR).append(BAR);
        }
      }
      if (hasThreshold() || isGraduatedColour())
      {
        sb.append(getMin()).append(BAR);
        sb.append(getMax()).append(BAR);
        if (isBelowThreshold())
        {
          sb.append(BELOW).append(BAR).append(getThreshold());
        }
        else if (isAboveThreshold())
        {
          sb.append(ABOVE).append(BAR).append(getThreshold());
        }
        else
        {
          sb.append("none");
        }
      }
      colourString = sb.toString();
    }
    return String.format("%s\t%s", featureType, colourString);
  }

  @Override
  public boolean isColourByAttribute()
  {
    return attributeName != null;
  }

  @Override
  public String[] getAttributeName()
  {
    return attributeName;
  }

  @Override
  public void setAttributeName(String... name)
  {
    attributeName = name;
  }

  @Override
  public boolean isOutwithThreshold(SequenceFeature feature)
  {
    if (!isGraduatedColour())
    {
      return false;
    }
    float scr = feature.getScore();
    if (attributeName != null)
    {
      try
      {
        String attVal = feature.getValueAsString(attributeName);
        scr = Float.valueOf(attVal);
      } catch (Throwable e)
      {
        scr = Float.NaN;
      }
    }
    if (Float.isNaN(scr))
    {
      return false;
    }

    return ((isAboveThreshold() && scr <= threshold)
            || (isBelowThreshold() && scr >= threshold));
  }

  @Override
  public String getDescription()
  {
    if (isSimpleColour())
    {
      return "r=" + colour.getRed() + ",g=" + colour.getGreen() + ",b="
              + colour.getBlue();
    }
    StringBuilder tt = new StringBuilder();
    String by = null;

    if (getAttributeName() != null)
    {
      by = FeatureMatcher.toAttributeDisplayName(getAttributeName());
    }
    else if (isColourByLabel())
    {
      by = I18N_LABEL;
    }
    else
    {
      by = I18N_SCORE;
    }
    tt.append(MessageManager.formatMessage("action.by_title_param", by));

    /*
     * add threshold if any
     */
    if (isAboveThreshold() || isBelowThreshold())
    {
      tt.append(" (");
      if (isColourByLabel())
      {
        /*
         * Jalview features file supports the combination of 
         * colour by label or attribute text with score threshold
         */
        tt.append(I18N_SCORE).append(" ");
      }
      tt.append(isAboveThreshold() ? "> " : "< ");
      tt.append(getThreshold()).append(")");
    }

    return tt.toString();
  }

}
