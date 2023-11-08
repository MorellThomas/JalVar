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
package jalview.io.gff;

import jalview.analysis.SequenceIdMatcher;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.MappingType;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.MapList;
import jalview.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class with common functionality for flavours of GFF handler (GFF2 or
 * GFF3)
 */
public abstract class GffHelperBase implements GffHelperI
{
  private static final String INVALID_GFF_ATTRIBUTE_FORMAT = "Invalid GFF attribute format: ";

  protected static final String COMMA = ",";

  protected static final String EQUALS = "=";

  protected static final String NOTE = "Note";

  /*
   * GFF columns 1-9 (zero-indexed):
   */
  protected static final int SEQID_COL = 0;

  protected static final int SOURCE_COL = 1;

  protected static final int TYPE_COL = 2;

  protected static final int START_COL = 3;

  protected static final int END_COL = 4;

  protected static final int SCORE_COL = 5;

  protected static final int STRAND_COL = 6;

  protected static final int PHASE_COL = 7;

  protected static final int ATTRIBUTES_COL = 8;

  private AlignmentI lastmatchedAl = null;

  private SequenceIdMatcher matcher = null;

  /**
   * Constructs and returns a mapping, or null if data appear invalid
   * 
   * @param fromStart
   * @param fromEnd
   * @param toStart
   * @param toEnd
   * @param mappingType
   *          type of mapping (e.g. protein to nucleotide)
   * @return
   */
  protected MapList constructMappingFromAlign(int fromStart, int fromEnd,
          int toStart, int toEnd, MappingType mappingType)
  {
    int[] from = new int[] { fromStart, fromEnd };
    int[] to = new int[] { toStart, toEnd };

    /*
     * Jalview always models from dna to protein, so switch values if the
     * GFF mapping is from protein to dna
     */
    if (mappingType == MappingType.PeptideToNucleotide)
    {
      int[] temp = from;
      from = to;
      to = temp;
      mappingType = mappingType.getInverse();
    }

    int fromRatio = mappingType.getFromRatio();
    int toRatio = mappingType.getToRatio();

    /*
     * sanity check that mapped residue counts match
     * TODO understand why PASA generates such cases...
     */
    if (!trimMapping(from, to, fromRatio, toRatio))
    {
      System.err.println("Ignoring mapping from " + Arrays.toString(from)
              + " to " + Arrays.toString(to) + " as counts don't match!");
      return null;
    }

    /*
     * If a codon has an intron gap, there will be contiguous 'toRanges';
     * this is handled for us by the MapList constructor. 
     * (It is not clear that exonerate ever generates this case)  
     */

    return new MapList(from, to, fromRatio, toRatio);
  }

  /**
   * Checks that the 'from' and 'to' ranges have equivalent lengths. If not,
   * tries to trim the end of the longer so they do. Returns true if the
   * mappings could be made equivalent, else false. Note the range array values
   * may be modified by this method.
   * 
   * @param from
   * @param to
   * @param fromRatio
   * @param toRatio
   * @return
   */
  protected static boolean trimMapping(int[] from, int[] to, int fromRatio,
          int toRatio)
  {
    int fromLength = Math.abs(from[1] - from[0]) + 1;
    int toLength = Math.abs(to[1] - to[0]) + 1;
    int fromOverlap = fromLength * toRatio - toLength * fromRatio;
    if (fromOverlap == 0)
    {
      return true;
    }
    if (fromOverlap > 0 && fromOverlap % toRatio == 0)
    {
      /*
       * restrict from range to make them match up
       * it's kind of arbitrary which end we truncate - here it is the end
       */
      System.err.print(
              "Truncating mapping from " + Arrays.toString(from) + " to ");
      if (from[1] > from[0])
      {
        from[1] -= fromOverlap / toRatio;
      }
      else
      {
        from[1] += fromOverlap / toRatio;
      }
      System.err.println(Arrays.toString(from));
      return true;
    }
    else if (fromOverlap < 0 && fromOverlap % fromRatio == 0)
    {
      fromOverlap = -fromOverlap; // > 0
      /*
       * restrict to range to make them match up
       */
      System.err.print(
              "Truncating mapping to " + Arrays.toString(to) + " to ");
      if (to[1] > to[0])
      {
        to[1] -= fromOverlap / fromRatio;
      }
      else
      {
        to[1] += fromOverlap / fromRatio;
      }
      System.err.println(Arrays.toString(to));
      return true;
    }

    /*
     * Couldn't truncate to an exact match..
     */
    return false;
  }

  /**
   * Returns a sequence matching the given id, as follows
   * <ul>
   * <li>strict matching is on exact sequence name</li>
   * <li>relaxed matching allows matching on a token within the sequence name,
   * or a dbxref</li>
   * <li>first tries to find a match in the alignment sequences</li>
   * <li>else tries to find a match in the new sequences already generated while
   * parsing the features file</li>
   * <li>else creates a new placeholder sequence, adds it to the new sequences
   * list, and returns it</li>
   * </ul>
   * 
   * @param seqId
   * @param align
   * @param newseqs
   * @param relaxedIdMatching
   * 
   * @return
   */
  protected SequenceI findSequence(String seqId, AlignmentI align,
          List<SequenceI> newseqs, boolean relaxedIdMatching)
  {
    if (seqId == null)
    {
      return null;
    }
    SequenceI match = null;
    if (relaxedIdMatching)
    {
      if (lastmatchedAl != align)
      {
        lastmatchedAl = align;
        matcher = new SequenceIdMatcher(align.getSequencesArray());
        if (newseqs != null)
        {
          matcher.addAll(newseqs);
        }
      }
      match = matcher.findIdMatch(seqId);
    }
    else
    {
      match = align.findName(seqId, true);
      if (match == null && newseqs != null)
      {
        for (SequenceI m : newseqs)
        {
          if (seqId.equals(m.getName()))
          {
            return m;
          }
        }
      }

    }
    if (match == null && newseqs != null)
    {
      match = new SequenceDummy(seqId);
      if (relaxedIdMatching)
      {
        matcher.addAll(Arrays.asList(new SequenceI[] { match }));
      }
      // add dummy sequence to the newseqs list
      newseqs.add(match);
    }
    return match;
  }

  /**
   * Parses the input line to a map of name / value(s) pairs. For example the
   * line
   * 
   * <pre>
   * Notes=Fe-S;Method=manual curation, prediction; source = Pfam; Notes = Metal
   * </pre>
   * 
   * if parsed with delimiter=";" and separators {' ', '='} <br>
   * would return a map with { Notes={Fe=S, Metal}, Method={manual curation,
   * prediction}, source={Pfam}} <br>
   * 
   * This method supports parsing of either GFF2 format (which uses space ' ' as
   * the name/value delimiter, and allows multiple occurrences of the same
   * name), or GFF3 format (which uses '=' as the name/value delimiter, and
   * strictly does not allow repeat occurrences of the same name - but does
   * allow a comma-separated list of values).
   * <p>
   * Returns a (possibly empty) map of lists of values by attribute name.
   * 
   * @param text
   * @param namesDelimiter
   *          the major delimiter between name-value pairs
   * @param nameValueSeparator
   *          separator used between name and value
   * @param valuesDelimiter
   *          delimits a list of more than one value
   * @return
   */
  public static Map<String, List<String>> parseNameValuePairs(String text,
          String namesDelimiter, char nameValueSeparator,
          String valuesDelimiter)
  {
    Map<String, List<String>> map = new HashMap<>();
    if (text == null || text.trim().length() == 0)
    {
      return map;
    }

    /*
     * split by major delimiter (; for GFF3)
     */
    for (String nameValuePair : text.trim().split(namesDelimiter))
    {
      nameValuePair = nameValuePair.trim();
      if (nameValuePair.length() == 0)
      {
        continue;
      }

      /*
       * find name/value separator (= for GFF3)
       */
      int sepPos = nameValuePair.indexOf(nameValueSeparator);
      if (sepPos == -1)
      {
        // no name=value found
        continue;
      }

      String name = nameValuePair.substring(0, sepPos).trim();
      String values = nameValuePair.substring(sepPos + 1).trim();
      if (values.isEmpty())
      {
        continue;
      }

      List<String> vals = map.get(name);
      if (vals == null)
      {
        vals = new ArrayList<>();
        map.put(name, vals);
      }

      /*
       * if 'values' contains more name/value separators, parse as a map
       * (nested sub-attribute values)
       */
      if (values.indexOf(nameValueSeparator) != -1)
      {
        vals.add(values);
      }
      else
      {
        for (String val : values.split(valuesDelimiter))
        {
          vals.add(val);
        }
      }
    }

    return map;
  }

  /**
   * Constructs a SequenceFeature from the GFF column data. Subclasses may wish
   * to call this method then adjust the SequenceFeature depending on the
   * particular usage of different tools that generate GFF.
   * 
   * @param gff
   * @param attributes
   * @return
   */
  protected SequenceFeature buildSequenceFeature(String[] gff,
          Map<String, List<String>> attributes)
  {
    return buildSequenceFeature(gff, TYPE_COL, gff[SOURCE_COL], attributes);
  }

  /**
   * @param gff
   * @param typeColumn
   * @param group
   * @param attributes
   * @return
   */
  protected SequenceFeature buildSequenceFeature(String[] gff,
          int typeColumn, String group,
          Map<String, List<String>> attributes)
  {
    try
    {
      int start = Integer.parseInt(gff[START_COL]);
      int end = Integer.parseInt(gff[END_COL]);

      /*
       * default 'score' is 0 rather than Float.NaN - see JAL-2554
       */
      float score = 0f;
      try
      {
        score = Float.parseFloat(gff[SCORE_COL]);
      } catch (NumberFormatException nfe)
      {
        // e.g. '.' - leave as zero
      }

      SequenceFeature sf = new SequenceFeature(gff[typeColumn],
              gff[SOURCE_COL], start, end, score, group);

      sf.setStrand(gff[STRAND_COL]);

      sf.setPhase(gff[PHASE_COL]);

      if (attributes != null)
      {
        /*
         * Add attributes in column 9 to the sequence feature's 
         * 'otherData' table; use Note as a best proxy for description;
         * decode any encoded comma, equals, semi-colon as per GFF3 spec
         */
        for (Entry<String, List<String>> attr : attributes.entrySet())
        {
          String key = attr.getKey();
          List<String> values = attr.getValue();
          if (values.size() == 1 && values.get(0).contains(EQUALS))
          {
            /*
             * 'value' is actually nested subattributes as x=a,y=b,z=c
             */
            Map<String, String> valueMap = parseAttributeMap(values.get(0));
            sf.setValue(key, valueMap);
          }
          else
          {
            String csvValues = StringUtils.listToDelimitedString(values,
                    COMMA);
            csvValues = StringUtils.urlDecode(csvValues, GFF_ENCODABLE);
            sf.setValue(key, csvValues);
            if (NOTE.equals(key))
            {
              sf.setDescription(csvValues);
            }
          }
        }
      }

      return sf;
    } catch (NumberFormatException nfe)
    {
      System.err.println("Invalid number in gff: " + nfe.getMessage());
      return null;
    }
  }

  /**
   * Parses a (GFF3 format) list of comma-separated key=value pairs into a Map
   * of {@code key,
   * value} <br>
   * An input string like {@code a=b,c,d=e,f=g,h} is parsed to
   * 
   * <pre>
   * a = "b,c"
   * d = "e"
   * f = "g,h"
   * </pre>
   * 
   * @param s
   * 
   * @return
   */
  protected static Map<String, String> parseAttributeMap(String s)
  {
    Map<String, String> map = new HashMap<>();
    String[] fields = s.split(EQUALS);

    /*
     * format validation
     */
    boolean valid = true;
    if (fields.length < 2)
    {
      /*
       * need at least A=B here
       */
      valid = false;
    }
    else if (fields[0].isEmpty() || fields[0].contains(COMMA))
    {
      /*
       * A,B=C is not a valid start, nor is =C
       */
      valid = false;
    }
    else
    {
      for (int i = 1; i < fields.length - 1; i++)
      {
        if (fields[i].isEmpty() || !fields[i].contains(COMMA))
        {
          /*
           * intermediate tokens must include value,name
           */
          valid = false;
        }
      }
    }

    if (!valid)
    {
      System.err.println(INVALID_GFF_ATTRIBUTE_FORMAT + s);
      return map;
    }

    int i = 0;
    while (i < fields.length - 1)
    {
      boolean lastPair = i == fields.length - 2;
      String before = fields[i];
      String after = fields[i + 1];

      /*
       * if 'key' looks like a,b,c then the last token is the
       * key
       */
      String theKey = before.contains(COMMA)
              ? before.substring(before.lastIndexOf(COMMA) + 1)
              : before;

      theKey = theKey.trim();
      if (theKey.isEmpty())
      {
        System.err.println(INVALID_GFF_ATTRIBUTE_FORMAT + s);
        map.clear();
        return map;
      }

      /*
       * if 'value' looks like a,b,c then all but the last token is the value,
       * unless this is the last field (no more = to follow), in which case
       * all of it makes up the value
       */
      String theValue = after.contains(COMMA) && !lastPair
              ? after.substring(0, after.lastIndexOf(COMMA))
              : after;
      map.put(StringUtils.urlDecode(theKey, GFF_ENCODABLE),
              StringUtils.urlDecode(theValue, GFF_ENCODABLE));
      i += 1;
    }

    return map;
  }

  /**
   * Returns any existing mapping held on the alignment between the given
   * dataset sequences, or a new one if none found. This is a convenience method
   * to facilitate processing multiple GFF lines that make up a single 'spliced'
   * mapping, by extending the first mapping as the others are read.
   * 
   * @param align
   * @param fromSeq
   * @param toSeq
   * @return
   */
  protected AlignedCodonFrame getMapping(AlignmentI align,
          SequenceI fromSeq, SequenceI toSeq)
  {
    AlignedCodonFrame acf = align.getMapping(fromSeq, toSeq);
    if (acf == null)
    {
      acf = new AlignedCodonFrame();
    }
    return acf;
  }

}
