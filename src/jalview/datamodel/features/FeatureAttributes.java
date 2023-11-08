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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A singleton class to hold the set of attributes known for each feature type
 */
public class FeatureAttributes
{
  public enum Datatype
  {
    Character, Number, Mixed
  }

  private static FeatureAttributes instance = new FeatureAttributes();

  /*
   * map, by feature type, of a map, by attribute name, of
   * attribute description and min-max range (if known)
   */
  private Map<String, Map<String[], AttributeData>> attributes;

  /*
   * a case-insensitive comparator so that attributes are ordered e.g.
   * AC
   * af
   * CSQ:AFR_MAF
   * CSQ:Allele
   */
  private Comparator<String[]> comparator = new Comparator<String[]>()
  {
    @Override
    public int compare(String[] o1, String[] o2)
    {
      int i = 0;
      while (i < o1.length || i < o2.length)
      {
        if (o2.length <= i)
        {
          return o1.length <= i ? 0 : 1;
        }
        if (o1.length <= i)
        {
          return -1;
        }
        int comp = String.CASE_INSENSITIVE_ORDER.compare(o1[i], o2[i]);
        if (comp != 0)
        {
          return comp;
        }
        i++;
      }
      return 0; // same length and all matched
    }
  };

  private class AttributeData
  {
    /*
     * description(s) for this attribute, if known
     * (different feature source might have differing descriptions)
     */
    List<String> description;

    /*
     * minimum value (of any numeric values recorded)
     */
    float min = 0f;

    /*
     * maximum value (of any numeric values recorded)
     */
    float max = 0f;

    /*
     * flag is set true if any numeric value is detected for this attribute
     */
    boolean hasValue = false;

    Datatype type;

    /**
     * Note one instance of this attribute, recording unique, non-null
     * descriptions, and the min/max of any numerical values
     * 
     * @param desc
     * @param value
     */
    void addInstance(String desc, String value)
    {
      addDescription(desc);

      if (value != null)
      {
        value = value.trim();

        /*
         * Parse numeric value unless we have previously
         * seen text data for this attribute type
         */
        if (type == null || type == Datatype.Number)
        {
          try
          {
            float f = Float.valueOf(value);
            min = hasValue ? Math.min(min, f) : f;
            max = hasValue ? Math.max(max, f) : f;
            hasValue = true;
            type = (type == null || type == Datatype.Number)
                    ? Datatype.Number
                    : Datatype.Mixed;
          } catch (NumberFormatException e)
          {
            /*
             * non-numeric data: treat attribute as Character (or Mixed)
             */
            type = (type == null || type == Datatype.Character)
                    ? Datatype.Character
                    : Datatype.Mixed;
            min = 0f;
            max = 0f;
            hasValue = false;
          }
        }
      }
    }

    /**
     * Answers the description of the attribute, if recorded and unique, or null
     * if either no, or more than description is recorded
     * 
     * @return
     */
    public String getDescription()
    {
      if (description != null && description.size() == 1)
      {
        return description.get(0);
      }
      return null;
    }

    public Datatype getType()
    {
      return type;
    }

    /**
     * Adds the given description to the list of known descriptions (without
     * duplication)
     * 
     * @param desc
     */
    public void addDescription(String desc)
    {
      if (desc != null)
      {
        if (description == null)
        {
          description = new ArrayList<>();
        }
        if (!description.contains(desc))
        {
          description.add(desc);
        }
      }
    }
  }

  /**
   * Answers the singleton instance of this class
   * 
   * @return
   */
  public static FeatureAttributes getInstance()
  {
    return instance;
  }

  private FeatureAttributes()
  {
    attributes = new HashMap<>();
  }

  /**
   * Answers the attribute names known for the given feature type, in
   * alphabetical order (not case sensitive), or an empty set if no attributes
   * are known. An attribute name is typically 'simple' e.g. "AC", but may be
   * 'compound' e.g. {"CSQ", "Allele"} where a feature has map-valued attributes
   * 
   * @param featureType
   * @return
   */
  public List<String[]> getAttributes(String featureType)
  {
    if (!attributes.containsKey(featureType))
    {
      return Collections.<String[]> emptyList();
    }

    return new ArrayList<>(attributes.get(featureType).keySet());
  }

  /**
   * Answers true if at least one attribute is known for the given feature type,
   * else false
   * 
   * @param featureType
   * @return
   */
  public boolean hasAttributes(String featureType)
  {
    if (attributes.containsKey(featureType))
    {
      if (!attributes.get(featureType).isEmpty())
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Records the given attribute name and description for the given feature
   * type, and updates the min-max for any numeric value
   * 
   * @param featureType
   * @param description
   * @param value
   * @param attName
   */
  public void addAttribute(String featureType, String description,
          Object value, String... attName)
  {
    if (featureType == null || attName == null)
    {
      return;
    }

    /*
     * if attribute value is a map, drill down one more level to
     * record its sub-fields
     */
    if (value instanceof Map<?, ?>)
    {
      for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet())
      {
        String[] attNames = new String[attName.length + 1];
        System.arraycopy(attName, 0, attNames, 0, attName.length);
        attNames[attName.length] = entry.getKey().toString();
        addAttribute(featureType, description, entry.getValue(), attNames);
      }
      return;
    }

    String valueAsString = value.toString();
    Map<String[], AttributeData> atts = attributes.get(featureType);
    if (atts == null)
    {
      atts = new TreeMap<>(comparator);
      attributes.put(featureType, atts);
    }
    AttributeData attData = atts.get(attName);
    if (attData == null)
    {
      attData = new AttributeData();
      atts.put(attName, attData);
    }
    attData.addInstance(description, valueAsString);
  }

  /**
   * Answers the description of the given attribute for the given feature type,
   * if known and unique, else null
   * 
   * @param featureType
   * @param attName
   * @return
   */
  public String getDescription(String featureType, String... attName)
  {
    String desc = null;
    Map<String[], AttributeData> atts = attributes.get(featureType);
    if (atts != null)
    {
      AttributeData attData = atts.get(attName);
      if (attData != null)
      {
        desc = attData.getDescription();
      }
    }
    return desc;
  }

  /**
   * Answers the [min, max] value range of the given attribute for the given
   * feature type, if known, else null. Attributes with a mixture of text and
   * numeric values are considered text (do not return a min-max range).
   * 
   * @param featureType
   * @param attName
   * @return
   */
  public float[] getMinMax(String featureType, String... attName)
  {
    Map<String[], AttributeData> atts = attributes.get(featureType);
    if (atts != null)
    {
      AttributeData attData = atts.get(attName);
      if (attData != null && attData.hasValue)
      {
        return new float[] { attData.min, attData.max };
      }
    }
    return null;
  }

  /**
   * Records the given attribute description for the given feature type
   * 
   * @param featureType
   * @param attName
   * @param description
   */
  public void addDescription(String featureType, String description,
          String... attName)
  {
    if (featureType == null || attName == null)
    {
      return;
    }

    Map<String[], AttributeData> atts = attributes.get(featureType);
    if (atts == null)
    {
      atts = new TreeMap<>(comparator);
      attributes.put(featureType, atts);
    }
    AttributeData attData = atts.get(attName);
    if (attData == null)
    {
      attData = new AttributeData();
      atts.put(attName, attData);
    }
    attData.addDescription(description);
  }

  /**
   * Answers the datatype of the feature, which is one of Character, Number or
   * Mixed (or null if not known), as discovered from values recorded.
   * 
   * @param featureType
   * @param attName
   * @return
   */
  public Datatype getDatatype(String featureType, String... attName)
  {
    Map<String[], AttributeData> atts = attributes.get(featureType);
    if (atts != null)
    {
      AttributeData attData = atts.get(attName);
      if (attData != null)
      {
        return attData.getType();
      }
    }
    return null;
  }

  /**
   * Resets all attribute metadata
   */
  public void clear()
  {
    attributes.clear();
  }

  /**
   * Resets attribute metadata for one feature type
   * 
   * @param featureType
   */
  public void clear(String featureType)
  {
    Map<String[], AttributeData> map = attributes.get(featureType);
    if (map != null)
    {
      map.clear();
    }

  }
}
