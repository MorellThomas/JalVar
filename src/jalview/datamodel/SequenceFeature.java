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
package jalview.datamodel;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import jalview.datamodel.features.FeatureAttributeType;
import jalview.datamodel.features.FeatureAttributes;
import jalview.datamodel.features.FeatureLocationI;
import jalview.datamodel.features.FeatureSourceI;
import jalview.datamodel.features.FeatureSources;
import jalview.util.StringUtils;

/**
 * A class that models a single contiguous feature on a sequence. If flag
 * 'contactFeature' is true, the start and end positions are interpreted instead
 * as two contact points.
 */
public class SequenceFeature implements FeatureLocationI
{
  /*
   * score value if none is set; preferably Float.Nan, but see
   * JAL-2060 and JAL-2554 for a couple of blockers to that
   */
  private static final float NO_SCORE = 0f;

  private static final String STATUS = "status";

  public static final String STRAND = "STRAND";

  // key for Phase designed not to conflict with real GFF data
  public static final String PHASE = "!Phase";

  // private key for ENA location designed not to conflict with real GFF data
  private static final String LOCATION = "!Location";

  private static final String ROW_DATA = "<tr><td>%s</td><td>%s</td><td>%s</td></tr>";

  /*
   * type, begin, end, featureGroup, score and contactFeature are final 
   * to ensure that the integrity of SequenceFeatures data store 
   * can't be broken by direct update of these fields
   */
  public final String type;

  public final int begin;

  public final int end;

  public final String featureGroup;

  public final float score;

  private final boolean contactFeature;

  public String description;

  /*
   * a map of key-value pairs; may be populated from GFF 'column 9' data,
   * other data sources (e.g. GenBank file), or programmatically
   */
  public Map<String, Object> otherDetails;

  public Vector<String> links;

  /*
   * the identifier (if known) for the FeatureSource held in FeatureSources,
   * as a provider of metadata about feature attributes 
   */
  private String source;

  /**
   * Constructs a duplicate feature. Note: Uses makes a shallow copy of the
   * otherDetails map, so the new and original SequenceFeature may reference the
   * same objects in the map.
   * 
   * @param cpy
   */
  public SequenceFeature(SequenceFeature cpy)
  {
    this(cpy, cpy.getBegin(), cpy.getEnd(), cpy.getFeatureGroup(),
            cpy.getScore());
  }

  /**
   * Constructor
   * 
   * @param theType
   * @param theDesc
   * @param theBegin
   * @param theEnd
   * @param group
   */
  public SequenceFeature(String theType, String theDesc, int theBegin,
          int theEnd, String group)
  {
    this(theType, theDesc, theBegin, theEnd, NO_SCORE, group);
  }

  /**
   * Constructor including a score value
   * 
   * @param theType
   * @param theDesc
   * @param theBegin
   * @param theEnd
   * @param theScore
   * @param group
   */
  public SequenceFeature(String theType, String theDesc, int theBegin,
          int theEnd, float theScore, String group)
  {
    this.type = theType;
    this.description = theDesc;
    this.begin = theBegin;
    this.end = theEnd;
    this.featureGroup = group;
    this.score = theScore;

    /*
     * for now, only "Disulfide/disulphide bond" is treated as a contact feature
     */
    this.contactFeature = "disulfide bond".equalsIgnoreCase(type)
            || "disulphide bond".equalsIgnoreCase(type);
  }

  /**
   * A copy constructor that allows the value of final fields to be 'modified'
   * 
   * @param sf
   * @param newType
   * @param newBegin
   * @param newEnd
   * @param newGroup
   * @param newScore
   */
  public SequenceFeature(SequenceFeature sf, String newType, int newBegin,
          int newEnd, String newGroup, float newScore)
  {
    this(newType, sf.getDescription(), newBegin, newEnd, newScore,
            newGroup);

    this.source = sf.source;

    if (sf.otherDetails != null)
    {
      otherDetails = new LinkedHashMap<>();
      otherDetails.putAll(sf.otherDetails);
    }
    if (sf.links != null && sf.links.size() > 0)
    {
      links = new Vector<>();
      links.addAll(sf.links);
    }
  }

  /**
   * A copy constructor that allows the value of final fields to be 'modified'
   * 
   * @param sf
   * @param newBegin
   * @param newEnd
   * @param newGroup
   * @param newScore
   */
  public SequenceFeature(SequenceFeature sf, int newBegin, int newEnd,
          String newGroup, float newScore)
  {
    this(sf, sf.getType(), newBegin, newEnd, newGroup, newScore);
  }

  /**
   * Two features are considered equal if they have the same type, group,
   * description, start, end, phase, strand, and (if present) 'Name', ID' and
   * 'Parent' attributes.
   * 
   * Note we need to check Parent to distinguish the same exon occurring in
   * different transcripts (in Ensembl GFF). This allows assembly of transcript
   * sequences from their component exon regions.
   */
  @Override
  public boolean equals(Object o)
  {
    return equals(o, false);
  }

  /**
   * Overloaded method allows the equality test to optionally ignore the
   * 'Parent' attribute of a feature. This supports avoiding adding many
   * superficially duplicate 'exon' or CDS features to genomic or protein
   * sequence.
   * 
   * @param o
   * @param ignoreParent
   * @return
   */
  public boolean equals(Object o, boolean ignoreParent)
  {
    if (o == null || !(o instanceof SequenceFeature))
    {
      return false;
    }

    SequenceFeature sf = (SequenceFeature) o;
    boolean sameScore = Float.isNaN(score) ? Float.isNaN(sf.score)
            : score == sf.score;
    if (begin != sf.begin || end != sf.end || !sameScore)
    {
      return false;
    }

    if (getStrand() != sf.getStrand())
    {
      return false;
    }

    if (!(type + description + featureGroup + getPhase()).equals(
            sf.type + sf.description + sf.featureGroup + sf.getPhase()))
    {
      return false;
    }
    if (!equalAttribute(getValue("ID"), sf.getValue("ID")))
    {
      return false;
    }
    if (!equalAttribute(getValue("Name"), sf.getValue("Name")))
    {
      return false;
    }
    if (!ignoreParent)
    {
      if (!equalAttribute(getValue("Parent"), sf.getValue("Parent")))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if both values are null, are both non-null and equal
   * 
   * @param att1
   * @param att2
   * @return
   */
  protected static boolean equalAttribute(Object att1, Object att2)
  {
    if (att1 == null && att2 == null)
    {
      return true;
    }
    if (att1 != null)
    {
      return att1.equals(att2);
    }
    return att2.equals(att1);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getBegin()
  {
    return begin;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getEnd()
  {
    return end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getType()
  {
    return type;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String desc)
  {
    description = desc;
  }

  public String getFeatureGroup()
  {
    return featureGroup;
  }

  /**
   * Adds a hyperlink for the feature. This should have the format label|url.
   * 
   * @param labelLink
   */
  public void addLink(String labelLink)
  {
    if (links == null)
    {
      links = new Vector<>();
    }

    if (!links.contains(labelLink))
    {
      links.insertElementAt(labelLink, 0);
    }
  }

  public float getScore()
  {
    return score;
  }

  /**
   * Used for getting values which are not in the basic set. eg STRAND, PHASE
   * for GFF file
   * 
   * @param key
   *          String
   */
  public Object getValue(String key)
  {
    if (otherDetails == null)
    {
      return null;
    }
    else
    {
      return otherDetails.get(key);
    }
  }

  /**
   * Answers the value of the specified attribute as string, or null if no such
   * value. If more than one attribute name is provided, tries to resolve as
   * keys to nested maps. For example, if attribute "CSQ" holds a map of
   * key-value pairs, then getValueAsString("CSQ", "Allele") returns the value
   * of "Allele" in that map.
   * 
   * @param key
   * @return
   */
  public String getValueAsString(String... key)
  {
    if (otherDetails == null)
    {
      return null;
    }
    Object value = otherDetails.get(key[0]);
    if (key.length > 1 && value instanceof Map<?, ?>)
    {
      value = ((Map) value).get(key[1]);
    }
    return value == null ? null : value.toString();
  }

  /**
   * Returns a property value for the given key if known, else the specified
   * default value
   * 
   * @param key
   * @param defaultValue
   * @return
   */
  public Object getValue(String key, Object defaultValue)
  {
    Object value = getValue(key);
    return value == null ? defaultValue : value;
  }

  /**
   * Used for setting values which are not in the basic set. eg STRAND, FRAME
   * for GFF file
   * 
   * @param key
   *          eg STRAND
   * @param value
   *          eg +
   */
  public void setValue(String key, Object value)
  {
    if (value != null)
    {
      if (otherDetails == null)
      {
        /*
         * LinkedHashMap preserves insertion order of attributes
         */
        otherDetails = new LinkedHashMap<>();
      }

      otherDetails.put(key, value);
      recordAttribute(key, value);
    }
  }

  /**
   * Notifies the addition of a feature attribute. This lets us keep track of
   * which attributes are present on each feature type, and also the range of
   * numerical-valued attributes.
   * 
   * @param key
   * @param value
   */
  protected void recordAttribute(String key, Object value)
  {
    String attDesc = null;
    if (source != null)
    {
      attDesc = FeatureSources.getInstance().getSource(source)
              .getAttributeName(key);
    }

    FeatureAttributes.getInstance().addAttribute(this.type, attDesc, value,
            key);
  }

  /*
   * The following methods are added to maintain the castor Uniprot mapping file
   * for the moment.
   */
  public void setStatus(String status)
  {
    setValue(STATUS, status);
  }

  public String getStatus()
  {
    return (String) getValue(STATUS);
  }

  /**
   * Return 1 for forward strand ('+' in GFF), -1 for reverse strand ('-' in
   * GFF), and 0 for unknown or not (validly) specified
   * 
   * @return
   */
  public int getStrand()
  {
    int strand = 0;
    if (otherDetails != null)
    {
      Object str = otherDetails.get(STRAND);
      if ("-".equals(str))
      {
        strand = -1;
      }
      else if ("+".equals(str))
      {
        strand = 1;
      }
    }
    return strand;
  }

  /**
   * Set the value of strand
   * 
   * @param strand
   *          should be "+" for forward, or "-" for reverse
   */
  public void setStrand(String strand)
  {
    setValue(STRAND, strand);
  }

  public void setPhase(String phase)
  {
    setValue(PHASE, phase);
  }

  public String getPhase()
  {
    return (String) getValue(PHASE);
  }

  /**
   * Sets the 'raw' ENA format location specifier e.g. join(12..45,89..121)
   * 
   * @param loc
   */
  public void setEnaLocation(String loc)
  {
    setValue(LOCATION, loc);
  }

  /**
   * Gets the 'raw' ENA format location specifier e.g. join(12..45,89..121)
   * 
   * @param loc
   */
  public String getEnaLocation()
  {
    return (String) getValue(LOCATION);
  }

  /**
   * Readable representation, for debug only, not guaranteed not to change
   * between versions
   */
  @Override
  public String toString()
  {
    return String.format("%d %d %s %s", getBegin(), getEnd(), getType(),
            getDescription());
  }

  /**
   * Overridden to ensure that whenever two objects are equal, they have the
   * same hashCode
   */
  @Override
  public int hashCode()
  {
    String s = getType() + getDescription() + getFeatureGroup()
            + getValue("ID") + getValue("Name") + getValue("Parent")
            + getPhase();
    return s.hashCode() + getBegin() + getEnd() + (int) getScore()
            + getStrand();
  }

  /**
   * Answers true if the feature's start/end values represent two related
   * positions, rather than ends of a range. Such features may be visualised or
   * reported differently to features on a range.
   */
  @Override
  public boolean isContactFeature()
  {
    return contactFeature;
  }

  /**
   * Answers true if the sequence has zero start and end position
   * 
   * @return
   */
  public boolean isNonPositional()
  {
    return begin == 0 && end == 0;
  }

  /**
   * Answers an html-formatted report of feature details. If parameter
   * {@code mf} is not null, the feature is a virtual linked feature, and
   * details included both the original location and the mapped location
   * (CDS/peptide).
   * 
   * @param seqName
   * @param mf
   * 
   * @return
   */
  public String getDetailsReport(String seqName, MappedFeatures mf)
  {
    FeatureSourceI metadata = FeatureSources.getInstance()
            .getSource(source);

    StringBuilder sb = new StringBuilder(128);
    sb.append("<br>");
    sb.append("<table>");
    String name = mf == null ? seqName : mf.getLinkedSequenceName();
    sb.append(String.format(ROW_DATA, "Location", name, begin == end ? begin
            : begin + (isContactFeature() ? ":" : "-") + end));

    String consequence = "";
    if (mf != null)
    {
      int[] localRange = mf.getMappedPositions(begin, end);
      int from = localRange[0];
      int to = localRange[localRange.length - 1];
      String s = mf.isFromCds() ? "Peptide Location" : "Coding location";
      sb.append(String.format(ROW_DATA, s, seqName, from == to ? from
              : from + (isContactFeature() ? ":" : "-") + to));
      if (mf.isFromCds())
      {
        consequence = mf.findProteinVariants(this);
      }
    }
    sb.append(String.format(ROW_DATA, "Type", type, ""));
    String desc = StringUtils.stripHtmlTags(description);
    sb.append(String.format(ROW_DATA, "Description", desc, ""));
    if (!Float.isNaN(score) && score != 0f)
    {
      sb.append(String.format(ROW_DATA, "Score", score, ""));
    }
    if (featureGroup != null)
    {
      sb.append(String.format(ROW_DATA, "Group", featureGroup, ""));
    }

    if (!consequence.isEmpty())
    {
      sb.append(String.format(ROW_DATA, "Consequence",
              "<i>Translated by Jalview</i>", consequence));
    }

    if (otherDetails != null)
    {
      TreeMap<String, Object> ordered = new TreeMap<>(
              String.CASE_INSENSITIVE_ORDER);
      ordered.putAll(otherDetails);

      for (Entry<String, Object> entry : ordered.entrySet())
      {
        String key = entry.getKey();

        Object value = entry.getValue();
        if (value instanceof Map<?, ?>)
        {
          /*
           * expand values in a Map attribute across separate lines
           * copy to a TreeMap for alphabetical ordering
           */
          Map<String, Object> values = (Map<String, Object>) value;
          SortedMap<String, Object> sm = new TreeMap<>(
                  String.CASE_INSENSITIVE_ORDER);
          sm.putAll(values);
          for (Entry<?, ?> e : sm.entrySet())
          {
            sb.append(String.format(ROW_DATA, key, e.getKey().toString(),
                    e.getValue().toString()));
          }
        }
        else
        {
          // tried <td title="key"> but it failed to provide a tooltip :-(
          String attDesc = null;
          if (metadata != null)
          {
            attDesc = metadata.getAttributeName(key);
          }
          String s = entry.getValue().toString();
          if (isValueInteresting(key, s, metadata))
          {
            sb.append(String.format(ROW_DATA, key,
                    attDesc == null ? "" : attDesc, s));
          }
        }
      }
    }
    sb.append("</table>");

    String text = sb.toString();
    return text;
  }

  /**
   * Answers true if we judge the value is worth displaying, by some heuristic
   * rules, else false
   * 
   * @param key
   * @param value
   * @param metadata
   * @return
   */
  boolean isValueInteresting(String key, String value,
          FeatureSourceI metadata)
  {
    /*
     * currently suppressing zero values as well as null or empty
     */
    if (value == null || "".equals(value) || ".".equals(value)
            || "0".equals(value))
    {
      return false;
    }

    if (metadata == null)
    {
      return true;
    }

    FeatureAttributeType attType = metadata.getAttributeType(key);
    if (attType != null && (attType == FeatureAttributeType.Float
            || attType.equals(FeatureAttributeType.Integer)))
    {
      try
      {
        float fval = Float.valueOf(value);
        if (fval == 0f)
        {
          return false;
        }
      } catch (NumberFormatException e)
      {
        // ignore
      }
    }

    return true; // default to interesting
  }

  /**
   * Sets the feature source identifier
   * 
   * @param theSource
   */
  public void setSource(String theSource)
  {
    source = theSource;
  }
}

class SFSortByEnd implements Comparator<SequenceFeature>
{
  @Override
  public int compare(SequenceFeature a, SequenceFeature b)
  {
    return a.getEnd() - b.getEnd();
  }
}

class SFSortByBegin implements Comparator<SequenceFeature>
{
  @Override
  public int compare(SequenceFeature a, SequenceFeature b)
  {
    return a.getBegin() - b.getBegin();
  }
}
