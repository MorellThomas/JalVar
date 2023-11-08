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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import jalview.analysis.Rna;
import jalview.analysis.SecStrConsensus.SimpleBP;
import jalview.analysis.WUSSParseException;
import jalview.structure.StructureImportSettings;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class AlignmentAnnotation
{

  private static final String ANNOTATION_ID_PREFIX = "ann";

  /*
   * Identifers for different types of profile data
   */
  public static final int SEQUENCE_PROFILE = 0;

  public static final int STRUCTURE_PROFILE = 1;

  public static final int CDNA_PROFILE = 2;

  private static long counter = 0;

  /**
   * If true, this annotations is calculated every edit, eg consensus, quality
   * or conservation graphs
   */
  public boolean autoCalculated = false;

  /**
   * unique ID for this annotation, used to match up the same annotation row
   * shown in multiple views and alignments
   */
  public String annotationId;

  /**
   * the sequence this annotation is associated with (or null)
   */
  public SequenceI sequenceRef;

  /** label shown in dropdown menus and in the annotation label area */
  public String label;

  /** longer description text shown as a tooltip */
  public String description;

  /** Array of annotations placed in the current coordinate system */
  public Annotation[] annotations;

  public List<SimpleBP> bps = null;

  /**
   * RNA secondary structure contact positions
   */
  public SequenceFeature[] _rnasecstr = null;

  /**
   * position of annotation resulting in invalid WUSS parsing or -1. -2 means
   * there was no RNA structure in this annotation
   */
  private long invalidrnastruc = -2;

  /**
   * the type of temperature factor plot (if it is one)
   */
  private StructureImportSettings.TFType tfType = StructureImportSettings.TFType.DEFAULT;

  public void setTFType(StructureImportSettings.TFType t)
  {
    tfType = t;
  }

  public StructureImportSettings.TFType getTFType()
  {
    return tfType;
  }

  /**
   * Updates the _rnasecstr field Determines the positions that base pair and
   * the positions of helices based on secondary structure from a Stockholm file
   * 
   * @param rnaAnnotation
   */
  private void _updateRnaSecStr(CharSequence rnaAnnotation)
  {
    try
    {
      _rnasecstr = Rna.getHelixMap(rnaAnnotation);
      invalidrnastruc = -1;
    } catch (WUSSParseException px)
    {
      // DEBUG System.out.println(px);
      invalidrnastruc = px.getProblemPos();
    }
    if (invalidrnastruc > -1)
    {
      return;
    }

    if (_rnasecstr != null && _rnasecstr.length > 0)
    {
      // show all the RNA secondary structure annotation symbols.
      isrna = true;
      showAllColLabels = true;
      scaleColLabel = true;
      _markRnaHelices();
    }
    // System.out.println("featuregroup " + _rnasecstr[0].getFeatureGroup());

  }

  private void _markRnaHelices()
  {
    int mxval = 0;
    // Figure out number of helices
    // Length of rnasecstr is the number of pairs of positions that base pair
    // with each other in the secondary structure
    for (int x = 0; x < _rnasecstr.length; x++)
    {

      /*
       * System.out.println(this.annotation._rnasecstr[x] + " Begin" +
       * this.annotation._rnasecstr[x].getBegin());
       */
      // System.out.println(this.annotation._rnasecstr[x].getFeatureGroup());
      int val = 0;
      try
      {
        val = Integer.valueOf(_rnasecstr[x].getFeatureGroup());
        if (mxval < val)
        {
          mxval = val;
        }
      } catch (NumberFormatException q)
      {
      }
      ;

      annotations[_rnasecstr[x].getBegin()].value = val;
      annotations[_rnasecstr[x].getEnd()].value = val;

      // annotations[_rnasecstr[x].getBegin()].displayCharacter = "" + val;
      // annotations[_rnasecstr[x].getEnd()].displayCharacter = "" + val;
    }
    setScore(mxval);
  }

  /**
   * Get the RNA Secondary Structure SequenceFeature Array if present
   */
  public SequenceFeature[] getRnaSecondaryStructure()
  {
    return this._rnasecstr;
  }

  /**
   * Check the RNA Secondary Structure is equivalent to one in given
   * AlignmentAnnotation param
   */
  public boolean rnaSecondaryStructureEquivalent(AlignmentAnnotation that)
  {
    return rnaSecondaryStructureEquivalent(that, true);
  }

  public boolean rnaSecondaryStructureEquivalent(AlignmentAnnotation that,
          boolean compareType)
  {
    SequenceFeature[] thisSfArray = this.getRnaSecondaryStructure();
    SequenceFeature[] thatSfArray = that.getRnaSecondaryStructure();
    if (thisSfArray == null || thatSfArray == null)
    {
      return thisSfArray == null && thatSfArray == null;
    }
    if (thisSfArray.length != thatSfArray.length)
    {
      return false;
    }
    Arrays.sort(thisSfArray, new SFSortByEnd()); // probably already sorted
                                                 // like this
    Arrays.sort(thatSfArray, new SFSortByEnd()); // probably already sorted
                                                 // like this
    for (int i = 0; i < thisSfArray.length; i++)
    {
      SequenceFeature thisSf = thisSfArray[i];
      SequenceFeature thatSf = thatSfArray[i];
      if (compareType)
      {
        if (thisSf.getType() == null || thatSf.getType() == null)
        {
          if (thisSf.getType() == null && thatSf.getType() == null)
          {
            continue;
          }
          else
          {
            return false;
          }
        }
        if (!thisSf.getType().equals(thatSf.getType()))
        {
          return false;
        }
      }
      if (!(thisSf.getBegin() == thatSf.getBegin()
              && thisSf.getEnd() == thatSf.getEnd()))
      {
        return false;
      }
    }
    return true;

  }

  /**
   * map of positions in the associated annotation
   */
  private Map<Integer, Annotation> sequenceMapping;

  /**
   * lower range for quantitative data
   */
  public float graphMin;

  /**
   * Upper range for quantitative data
   */
  public float graphMax;

  /**
   * Score associated with label and description.
   */
  public double score = Double.NaN;

  /**
   * flag indicating if annotation has a score.
   */
  public boolean hasScore = false;

  public GraphLine threshold;

  // Graphical hints and tips

  /** Can this row be edited by the user ? */
  public boolean editable = false;

  /** Indicates if annotation has a graphical symbol track */
  public boolean hasIcons; //

  /** Indicates if annotation has a text character label */
  public boolean hasText;

  /** is the row visible */
  public boolean visible = true;

  public int graphGroup = -1;

  /** Displayed height of row in pixels */
  public int height = 0;

  public int graph = 0;

  public int graphHeight = 40;

  public boolean padGaps = false;

  public static final int NO_GRAPH = 0;

  public static final int BAR_GRAPH = 1;

  public static final int LINE_GRAPH = 2;

  public static final int CONTACT_MAP = 4;

  /**
   * property that when set to non-empty string disables display of column groups defined on the contact matrix
   */
  public static final String CONTACT_MAP_NOGROUPS = "CMNOGRPS";

  public boolean belowAlignment = true;

  public SequenceGroup groupRef = null;

  /**
   * display every column label, even if there is a row of identical labels
   */
  public boolean showAllColLabels = false;

  /**
   * scale the column label to fit within the alignment column.
   */
  public boolean scaleColLabel = false;

  /**
   * centre the column labels relative to the alignment column
   */
  public boolean centreColLabels = false;

  private boolean isrna;

  public static int getGraphValueFromString(String string)
  {
    if (string.equalsIgnoreCase("BAR_GRAPH"))
    {
      return BAR_GRAPH;
    }
    else if (string.equalsIgnoreCase("LINE_GRAPH"))
    {
      return LINE_GRAPH;
    }
    else
    {
      return NO_GRAPH;
    }
  }

  /**
   * Creates a new AlignmentAnnotation object.
   * 
   * @param label
   *          short label shown under sequence labels
   * @param description
   *          text displayed on mouseover
   * @param annotations
   *          set of positional annotation elements
   */
  public AlignmentAnnotation(String label, String description,
          Annotation[] annotations)
  {
    setAnnotationId();
    // always editable?
    editable = true;
    this.label = label;
    this.description = description;
    this.annotations = annotations;

    validateRangeAndDisplay();
  }

  /**
   * Checks if annotation labels represent secondary structures
   * 
   */
  void areLabelsSecondaryStructure()
  {
    boolean nonSSLabel = false;
    isrna = false;
    StringBuffer rnastring = new StringBuffer();

    char firstChar = 0;
    for (int i = 0; i < annotations.length; i++)
    {
      // DEBUG System.out.println(i + ": " + annotations[i]);
      if (annotations[i] == null)
      {
        continue;
      }
      if (annotations[i].secondaryStructure == 'H'
              || annotations[i].secondaryStructure == 'E')
      {
        // DEBUG System.out.println( "/H|E/ '" +
        // annotations[i].secondaryStructure + "'");
        hasIcons |= true;
      }
      else
      // Check for RNA secondary structure
      {
        // DEBUG System.out.println( "/else/ '" +
        // annotations[i].secondaryStructure + "'");
        // TODO: 2.8.2 should this ss symbol validation check be a function in
        // RNA/ResidueProperties ?
        // allow for DSSP extended code:
        // https://www.wikidoc.org/index.php/Secondary_structure#The_DSSP_code
        // GHITEBS as well as C and X (for missing?)
        if (annotations[i].secondaryStructure == '('
                || annotations[i].secondaryStructure == '['
                || annotations[i].secondaryStructure == '<'
                || annotations[i].secondaryStructure == '{'
                || annotations[i].secondaryStructure == 'A'
                // || annotations[i].secondaryStructure == 'B'
                // || annotations[i].secondaryStructure == 'C'
                || annotations[i].secondaryStructure == 'D'
                // || annotations[i].secondaryStructure == 'E' // ambiguous on
                // its own -- already checked above
                || annotations[i].secondaryStructure == 'F'
                // || annotations[i].secondaryStructure == 'G'
                // || annotations[i].secondaryStructure == 'H' // ambiguous on
                // its own -- already checked above
                // || annotations[i].secondaryStructure == 'I'
                || annotations[i].secondaryStructure == 'J'
                || annotations[i].secondaryStructure == 'K'
                || annotations[i].secondaryStructure == 'L'
                || annotations[i].secondaryStructure == 'M'
                || annotations[i].secondaryStructure == 'N'
                || annotations[i].secondaryStructure == 'O'
                || annotations[i].secondaryStructure == 'P'
                || annotations[i].secondaryStructure == 'Q'
                || annotations[i].secondaryStructure == 'R'
                // || annotations[i].secondaryStructure == 'S'
                // || annotations[i].secondaryStructure == 'T'
                || annotations[i].secondaryStructure == 'U'
                || annotations[i].secondaryStructure == 'V'
                || annotations[i].secondaryStructure == 'W'
                // || annotations[i].secondaryStructure == 'X'
                || annotations[i].secondaryStructure == 'Y'
                || annotations[i].secondaryStructure == 'Z')
        {
          hasIcons |= true;
          isrna |= true;
        }
      }

      // System.out.println("displaychar " + annotations[i].displayCharacter);

      if (annotations[i].displayCharacter == null
              || annotations[i].displayCharacter.length() == 0)
      {
        rnastring.append('.');
        continue;
      }
      if (annotations[i].displayCharacter.length() == 1)
      {
        firstChar = annotations[i].displayCharacter.charAt(0);
        // check to see if it looks like a sequence or is secondary structure
        // labelling.
        if (annotations[i].secondaryStructure != ' ' && !hasIcons &&
        // Uncomment to only catch case where
        // displayCharacter==secondary
        // Structure
        // to correctly redisplay SS annotation imported from Stockholm,
        // exported to JalviewXML and read back in again.
        // &&
        // annotations[i].displayCharacter.charAt(0)==annotations[i].secondaryStructure
                firstChar != ' ' && firstChar != '$' && firstChar != 0xCE
                && firstChar != '(' && firstChar != '[' && firstChar != '<'
                && firstChar != '{' && firstChar != 'A' && firstChar != 'B'
                && firstChar != 'C' && firstChar != 'D' && firstChar != 'E'
                && firstChar != 'F' && firstChar != 'G' && firstChar != 'H'
                && firstChar != 'I' && firstChar != 'J' && firstChar != 'K'
                && firstChar != 'L' && firstChar != 'M' && firstChar != 'N'
                && firstChar != 'O' && firstChar != 'P' && firstChar != 'Q'
                && firstChar != 'R' && firstChar != 'S' && firstChar != 'T'
                && firstChar != 'U' && firstChar != 'V' && firstChar != 'W'
                && firstChar != 'X' && firstChar != 'Y' && firstChar != 'Z'
                && firstChar != '-'
                && firstChar < jalview.schemes.ResidueProperties.aaIndex.length)
        {
          if (jalview.schemes.ResidueProperties.aaIndex[firstChar] < 23) // TODO:
                                                                         // parameterise
                                                                         // to
                                                                         // gap
                                                                         // symbol
                                                                         // number
          {
            nonSSLabel = true;
          }
        }
      }
      else
      {
        rnastring.append(annotations[i].displayCharacter.charAt(1));
      }

      if (annotations[i].displayCharacter.length() > 0)
      {
        hasText = true;
      }
    }

    if (nonSSLabel)
    {
      hasIcons = false;
      for (int j = 0; j < annotations.length; j++)
      {
        if (annotations[j] != null
                && annotations[j].secondaryStructure != ' ')
        {
          annotations[j].displayCharacter = String
                  .valueOf(annotations[j].secondaryStructure);
          annotations[j].secondaryStructure = ' ';
        }

      }
    }
    else
    {
      if (isrna)
      {
        _updateRnaSecStr(new AnnotCharSequence());
      }
    }
  }

  /**
   * flyweight access to positions in the alignment annotation row for RNA
   * processing
   * 
   * @author jimp
   * 
   */
  private class AnnotCharSequence implements CharSequence
  {
    int offset = 0;

    int max = 0;

    public AnnotCharSequence()
    {
      this(0, annotations.length);
    }

    AnnotCharSequence(int start, int end)
    {
      offset = start;
      max = end;
    }

    @Override
    public CharSequence subSequence(int start, int end)
    {
      return new AnnotCharSequence(offset + start, offset + end);
    }

    @Override
    public int length()
    {
      return max - offset;
    }

    @Override
    public char charAt(int index)
    {
      return ((index + offset < 0) || (index + offset) >= max
              || annotations[index + offset] == null
              || (annotations[index + offset].secondaryStructure <= ' ')
                      ? ' '
                      : annotations[index + offset].displayCharacter == null
                              || annotations[index
                                      + offset].displayCharacter
                                              .length() == 0
                                                      ? annotations[index
                                                              + offset].secondaryStructure
                                                      : annotations[index
                                                              + offset].displayCharacter
                                                                      .charAt(0));
    }

    @Override
    public String toString()
    {
      char[] string = new char[max - offset];
      int mx = annotations.length;

      for (int i = offset; i < mx; i++)
      {
        string[i] = (annotations[i] == null
                || (annotations[i].secondaryStructure <= 32))
                        ? ' '
                        : (annotations[i].displayCharacter == null
                                || annotations[i].displayCharacter
                                        .length() == 0
                                                ? annotations[i].secondaryStructure
                                                : annotations[i].displayCharacter
                                                        .charAt(0));
      }
      return new String(string);
    }
  };

  private long _lastrnaannot = -1;

  public String getRNAStruc()
  {
    if (isrna)
    {
      String rnastruc = new AnnotCharSequence().toString();
      if (_lastrnaannot != rnastruc.hashCode())
      {
        // ensure rna structure contacts are up to date
        _lastrnaannot = rnastruc.hashCode();
        _updateRnaSecStr(rnastruc);
      }
      return rnastruc;
    }
    return null;
  }

  /**
   * Creates a new AlignmentAnnotation object.
   * 
   * @param label
   *          DOCUMENT ME!
   * @param description
   *          DOCUMENT ME!
   * @param annotations
   *          DOCUMENT ME!
   * @param min
   *          DOCUMENT ME!
   * @param max
   *          DOCUMENT ME!
   * @param winLength
   *          DOCUMENT ME!
   */
  public AlignmentAnnotation(String label, String description,
          Annotation[] annotations, float min, float max, int graphType)
  {
    setAnnotationId();
    // graphs are not editable
    editable = graphType == 0;

    this.label = label;
    this.description = description;
    this.annotations = annotations;
    graph = graphType;
    graphMin = min;
    graphMax = max;
    validateRangeAndDisplay();
  }

  /**
   * checks graphMin and graphMax, secondary structure symbols, sets graphType
   * appropriately, sets null labels to the empty string if appropriate.
   */
  public void validateRangeAndDisplay()
  {

    if (annotations == null)
    {
      visible = false; // try to prevent renderer from displaying.
      invalidrnastruc = -1;
      return; // this is a non-annotation row annotation - ie a sequence score.
    }

    int graphType = graph;
    float min = graphMin;
    float max = graphMax;
    boolean drawValues = true;
    _linecolour = null;
    if (min == max)
    {
      min = 999999999;
      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] == null)
        {
          continue;
        }

        if (drawValues && annotations[i].displayCharacter != null
                && annotations[i].displayCharacter.length() > 1)
        {
          drawValues = false;
        }

        if (annotations[i].value > max)
        {
          max = annotations[i].value;
        }

        if (annotations[i].value < min)
        {
          min = annotations[i].value;
        }
        if (_linecolour == null && annotations[i].colour != null)
        {
          _linecolour = annotations[i].colour;
        }
      }
      // ensure zero is origin for min/max ranges on only one side of zero
      if (min > 0)
      {
        min = 0;
      }
      else
      {
        if (max < 0)
        {
          max = 0;
        }
      }
    }

    graphMin = min;
    graphMax = max;

    areLabelsSecondaryStructure();

    if (!drawValues && graphType != NO_GRAPH)
    {
      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] != null)
        {
          annotations[i].displayCharacter = "";
        }
      }
    }
  }

  /**
   * Copy constructor creates a new independent annotation row with the same
   * associated sequenceRef
   * 
   * @param annotation
   */
  public AlignmentAnnotation(AlignmentAnnotation annotation)
  {
    setAnnotationId();
    this.label = new String(annotation.label);
    if (annotation.description != null)
    {
      this.description = new String(annotation.description);
    }
    this.graphMin = annotation.graphMin;
    this.graphMax = annotation.graphMax;
    this.graph = annotation.graph;
    this.graphHeight = annotation.graphHeight;
    this.graphGroup = annotation.graphGroup;
    this.groupRef = annotation.groupRef;
    this.editable = annotation.editable;
    this.autoCalculated = annotation.autoCalculated;
    this.hasIcons = annotation.hasIcons;
    this.hasText = annotation.hasText;
    this.height = annotation.height;
    this.label = annotation.label;
    this.padGaps = annotation.padGaps;
    this.visible = annotation.visible;
    this.centreColLabels = annotation.centreColLabels;
    this.scaleColLabel = annotation.scaleColLabel;
    this.showAllColLabels = annotation.showAllColLabels;
    this.calcId = annotation.calcId;
    if (annotation.properties != null)
    {
      properties = new HashMap<>();
      for (Map.Entry<String, String> val : annotation.properties.entrySet())
      {
        properties.put(val.getKey(), val.getValue());
      }
    }
    if (this.hasScore = annotation.hasScore)
    {
      this.score = annotation.score;
    }
    if (annotation.threshold != null)
    {
      threshold = new GraphLine(annotation.threshold);
    }
    Annotation[] ann = annotation.annotations;
    if (annotation.annotations != null)
    {
      this.annotations = new Annotation[ann.length];
      for (int i = 0; i < ann.length; i++)
      {
        if (ann[i] != null)
        {
          annotations[i] = new Annotation(ann[i]);
          if (_linecolour != null)
          {
            _linecolour = annotations[i].colour;
          }
        }
      }
    }
    if (annotation.sequenceRef != null)
    {
      this.sequenceRef = annotation.sequenceRef;
      if (annotation.sequenceMapping != null)
      {
        Integer p = null;
        sequenceMapping = new HashMap<>();
        Iterator<Integer> pos = annotation.sequenceMapping.keySet()
                .iterator();
        while (pos.hasNext())
        {
          // could optimise this!
          p = pos.next();
          Annotation a = annotation.sequenceMapping.get(p);
          if (a == null)
          {
            continue;
          }
          if (ann != null)
          {
            for (int i = 0; i < ann.length; i++)
            {
              if (ann[i] == a)
              {
                sequenceMapping.put(p, annotations[i]);
              }
            }
          }
        }
      }
      else
      {
        this.sequenceMapping = null;
      }

    }
    // TODO: check if we need to do this: JAL-952
    // if (this.isrna=annotation.isrna)
    {
      // _rnasecstr=new SequenceFeature[annotation._rnasecstr];
    }
    validateRangeAndDisplay(); // construct hashcodes, etc.
  }

  /**
   * clip the annotation to the columns given by startRes and endRes (inclusive)
   * and prune any existing sequenceMapping to just those columns.
   * 
   * @param startRes
   * @param endRes
   */
  public void restrict(int startRes, int endRes)
  {
    if (annotations == null)
    {
      // non-positional
      return;
    }
    if (startRes < 0)
    {
      startRes = 0;
    }
    if (startRes >= annotations.length)
    {
      startRes = annotations.length - 1;
    }
    if (endRes >= annotations.length)
    {
      endRes = annotations.length - 1;
    }
    if (annotations == null)
    {
      return;
    }
    Annotation[] temp = new Annotation[endRes - startRes + 1];
    if (startRes < annotations.length)
    {
      System.arraycopy(annotations, startRes, temp, 0,
              endRes - startRes + 1);
    }
    if (sequenceRef != null)
    {
      // Clip the mapping, if it exists.
      int spos = sequenceRef.findPosition(startRes);
      int epos = sequenceRef.findPosition(endRes);
      if (sequenceMapping != null)
      {
        Map<Integer, Annotation> newmapping = new HashMap<>();
        Iterator<Integer> e = sequenceMapping.keySet().iterator();
        while (e.hasNext())
        {
          Integer pos = e.next();
          if (pos.intValue() >= spos && pos.intValue() <= epos)
          {
            newmapping.put(pos, sequenceMapping.get(pos));
          }
        }
        sequenceMapping.clear();
        sequenceMapping = newmapping;
      }
    }
    annotations = temp;
  }

  /**
   * set the annotation row to be at least length Annotations
   * 
   * @param length
   *          minimum number of columns required in the annotation row
   * @return false if the annotation row is greater than length
   */
  public boolean padAnnotation(int length)
  {
    if (annotations == null)
    {
      return true; // annotation row is correct - null == not visible and
      // undefined length
    }
    if (annotations.length < length)
    {
      Annotation[] na = new Annotation[length];
      System.arraycopy(annotations, 0, na, 0, annotations.length);
      annotations = na;
      return true;
    }
    return annotations.length > length;

  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public String toString()
  {
    if (annotations == null)
    {
      return "";
    }
    StringBuilder buffer = new StringBuilder(256);

    for (int i = 0; i < annotations.length; i++)
    {
      if (annotations[i] != null)
      {
        if (graph != 0)
        {
          buffer.append(annotations[i].value);
        }
        else if (hasIcons)
        {
          buffer.append(annotations[i].secondaryStructure);
        }
        else
        {
          buffer.append(annotations[i].displayCharacter);
        }
      }

      buffer.append(", ");
    }
    // TODO: remove disgusting hack for 'special' treatment of consensus line.
    if (label.indexOf("Consensus") == 0)
    {
      buffer.append("\n");

      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] != null)
        {
          buffer.append(annotations[i].description);
        }

        buffer.append(", ");
      }
    }

    return buffer.toString();
  }

  public void setThreshold(GraphLine line)
  {
    threshold = line;
  }

  public GraphLine getThreshold()
  {
    return threshold;
  }

  /**
   * Attach the annotation to seqRef, starting from startRes position. If
   * alreadyMapped is true then the indices of the annotation[] array are
   * sequence positions rather than alignment column positions.
   * 
   * @param seqRef
   * @param startRes
   * @param alreadyMapped
   */
  public void createSequenceMapping(SequenceI seqRef, int startRes,
          boolean alreadyMapped)
  {

    if (seqRef == null)
    {
      return;
    }
    sequenceRef = seqRef;
    if (annotations == null)
    {
      return;
    }
    sequenceMapping = new HashMap<>();

    int seqPos;

    for (int i = 0; i < annotations.length; i++)
    {
      if (annotations[i] != null)
      {
        if (alreadyMapped)
        {
          seqPos = seqRef.findPosition(i);
        }
        else
        {
          seqPos = i + startRes;
        }

        sequenceMapping.put(Integer.valueOf(seqPos), annotations[i]);
      }
    }

  }

  /**
   * When positional annotation and a sequence reference is present, clears and
   * resizes the annotations array to the current alignment width, and adds
   * annotation according to aligned positions of the sequenceRef given by
   * sequenceMapping.
   */
  public void adjustForAlignment()
  {
    if (sequenceRef == null)
    {
      return;
    }

    if (annotations == null)
    {
      return;
    }

    int a = 0, aSize = sequenceRef.getLength();

    if (aSize == 0)
    {
      // Its been deleted
      return;
    }

    int position;
    Annotation[] temp = new Annotation[aSize];
    Integer index;
    if (sequenceMapping != null)
    {
      for (a = sequenceRef.getStart(); a <= sequenceRef.getEnd(); a++)
      {
        index = Integer.valueOf(a);
        Annotation annot = sequenceMapping.get(index);
        if (annot != null)
        {
          position = sequenceRef.findIndex(a) - 1;

          temp[position] = annot;
        }
      }
    }
    annotations = temp;
  }

  /**
   * remove any null entries in annotation row and return the number of non-null
   * annotation elements.
   * 
   * @return
   */
  public int compactAnnotationArray()
  {
    int i = 0, iSize = annotations.length;
    while (i < iSize)
    {
      if (annotations[i] == null)
      {
        if (i + 1 < iSize)
        {
          System.arraycopy(annotations, i + 1, annotations, i,
                  iSize - i - 1);
        }
        iSize--;
      }
      else
      {
        i++;
      }
    }
    Annotation[] ann = annotations;
    annotations = new Annotation[i];
    System.arraycopy(ann, 0, annotations, 0, i);
    ann = null;
    return iSize;
  }

  /**
   * Associate this annotation with the aligned residues of a particular
   * sequence. sequenceMapping will be updated in the following way: null
   * sequenceI - existing mapping will be discarded but annotations left in
   * mapped positions. valid sequenceI not equal to current sequenceRef: mapping
   * is discarded and rebuilt assuming 1:1 correspondence TODO: overload with
   * parameter to specify correspondence between current and new sequenceRef
   * 
   * @param sequenceI
   */
  public void setSequenceRef(SequenceI sequenceI)
  {
    if (sequenceI != null)
    {
      if (sequenceRef != null)
      {
        boolean rIsDs = sequenceRef.getDatasetSequence() == null,
                tIsDs = sequenceI.getDatasetSequence() == null;
        if (sequenceRef != sequenceI
                && (rIsDs && !tIsDs
                        && sequenceRef != sequenceI.getDatasetSequence())
                && (!rIsDs && tIsDs
                        && sequenceRef.getDatasetSequence() != sequenceI)
                && (!rIsDs && !tIsDs
                        && sequenceRef.getDatasetSequence() != sequenceI
                                .getDatasetSequence())
                && !sequenceRef.equals(sequenceI))
        {
          // if sequenceRef isn't intersecting with sequenceI
          // throw away old mapping and reconstruct.
          sequenceRef = null;
          if (sequenceMapping != null)
          {
            sequenceMapping = null;
            // compactAnnotationArray();
          }
          createSequenceMapping(sequenceI, 1, true);
          adjustForAlignment();
        }
        else
        {
          // Mapping carried over
          sequenceRef = sequenceI;
        }
      }
      else
      {
        // No mapping exists
        createSequenceMapping(sequenceI, 1, true);
        adjustForAlignment();
      }
    }
    else
    {
      // throw away the mapping without compacting.
      sequenceMapping = null;
      sequenceRef = null;
    }
  }

  /**
   * @return the score
   */
  public double getScore()
  {
    return score;
  }

  /**
   * @param score
   *          the score to set
   */
  public void setScore(double score)
  {
    hasScore = true;
    this.score = score;
  }

  /**
   * 
   * @return true if annotation has an associated score
   */
  public boolean hasScore()
  {
    return hasScore || !Double.isNaN(score);
  }

  /**
   * Score only annotation
   * 
   * @param label
   * @param description
   * @param score
   */
  public AlignmentAnnotation(String label, String description, double score)
  {
    this(label, description, null);
    setScore(score);
  }

  /**
   * copy constructor with edit based on the hidden columns marked in colSel
   * 
   * @param alignmentAnnotation
   * @param colSel
   */
  public AlignmentAnnotation(AlignmentAnnotation alignmentAnnotation,
          HiddenColumns hidden)
  {
    this(alignmentAnnotation);
    if (annotations == null)
    {
      return;
    }
    makeVisibleAnnotation(hidden);
  }

  public void setPadGaps(boolean padgaps, char gapchar)
  {
    this.padGaps = padgaps;
    if (padgaps)
    {
      hasText = true;
      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] == null)
        {
          annotations[i] = new Annotation(String.valueOf(gapchar), null,
                  ' ', 0f, null);
        }
        else if (annotations[i].displayCharacter == null
                || annotations[i].displayCharacter.equals(" "))
        {
          annotations[i].displayCharacter = String.valueOf(gapchar);
        }
      }
    }
  }

  /**
   * format description string for display
   * 
   * @param seqname
   * @return Get the annotation description string optionally prefixed by
   *         associated sequence name (if any)
   */
  public String getDescription(boolean seqname)
  {
    if (seqname && this.sequenceRef != null)
    {
      int i = description.toLowerCase(Locale.ROOT).indexOf("<html>");
      if (i > -1)
      {
        // move the html tag to before the sequence reference.
        return "<html>" + sequenceRef.getName() + " : "
                + description.substring(i + 6);
      }
      return sequenceRef.getName() + " : " + description;
    }
    return description;
  }

  public boolean isValidStruc()
  {
    return invalidrnastruc == -1;
  }

  public long getInvalidStrucPos()
  {
    return invalidrnastruc;
  }

  /**
   * machine readable ID string indicating what generated this annotation
   */
  protected String calcId = "";

  /**
   * properties associated with the calcId
   */
  protected Map<String, String> properties = new HashMap<>();

  /**
   * base colour for line graphs. If null, will be set automatically by
   * searching the alignment annotation
   */
  public java.awt.Color _linecolour;

  public String getCalcId()
  {
    return calcId;
  }

  public void setCalcId(String calcId)
  {
    this.calcId = calcId;
  }

  public boolean isRNA()
  {
    return isrna;
  }

  /**
   * transfer annotation to the given sequence using the given mapping from the
   * current positions or an existing sequence mapping
   * 
   * @param sq
   * @param sp2sq
   *          map involving sq as To or From
   */
  public void liftOver(SequenceI sq, Mapping sp2sq)
  {
    if (sp2sq.getMappedWidth() != sp2sq.getWidth())
    {
      // TODO: employ getWord/MappedWord to transfer annotation between cDNA and
      // Protein reference frames
      throw new Error(
              "liftOver currently not implemented for transfer of annotation between different types of seqeunce");
    }
    boolean mapIsTo = (sp2sq != null)
            ? (sp2sq.getTo() == sq
                    || sp2sq.getTo() == sq.getDatasetSequence())
            : false;

    // TODO build a better annotation element map and get rid of annotations[]
    Map<Integer, Annotation> mapForsq = new HashMap<>();
    if (sequenceMapping != null)
    {
      if (sp2sq != null)
      {
        for (Entry<Integer, Annotation> ie : sequenceMapping.entrySet())
        {
          Integer mpos = Integer
                  .valueOf(mapIsTo ? sp2sq.getMappedPosition(ie.getKey())
                          : sp2sq.getPosition(ie.getKey()));
          if (mpos >= sq.getStart() && mpos <= sq.getEnd())
          {
            mapForsq.put(mpos, ie.getValue());
          }
        }
        sequenceMapping = mapForsq;
        sequenceRef = sq;
        adjustForAlignment();
      }
      else
      {
        // trim positions
      }
    }
  }

  /**
   * like liftOver but more general.
   * 
   * Takes an array of int pairs that will be used to update the internal
   * sequenceMapping and so shuffle the annotated positions
   * 
   * @param newref
   *          - new sequence reference for the annotation row - if null,
   *          sequenceRef is left unchanged
   * @param mapping
   *          array of ints containing corresponding positions
   * @param from
   *          - column for current coordinate system (-1 for index+1)
   * @param to
   *          - column for destination coordinate system (-1 for index+1)
   * @param idxoffset
   *          - offset added to index when referencing either coordinate system
   * @note no checks are made as to whether from and/or to are sensible
   * @note caller should add the remapped annotation to newref if they have not
   *       already
   */
  public void remap(SequenceI newref, HashMap<Integer, int[]> mapping,
          int from, int to, int idxoffset)
  {
    if (mapping != null)
    {
      Map<Integer, Annotation> old = sequenceMapping;
      Map<Integer, Annotation> remap = new HashMap<>();
      int index = -1;
      for (int mp[] : mapping.values())
      {
        if (index++ < 0)
        {
          continue;
        }
        Annotation ann = null;
        if (from == -1)
        {
          ann = sequenceMapping.get(Integer.valueOf(idxoffset + index));
        }
        else
        {
          if (mp != null && mp.length > from)
          {
            ann = sequenceMapping.get(Integer.valueOf(mp[from]));
          }
        }
        if (ann != null)
        {
          if (to == -1)
          {
            remap.put(Integer.valueOf(idxoffset + index), ann);
          }
          else
          {
            if (to > -1 && to < mp.length)
            {
              remap.put(Integer.valueOf(mp[to]), ann);
            }
          }
        }
      }
      sequenceMapping = remap;
      old.clear();
      if (newref != null)
      {
        sequenceRef = newref;
      }
      adjustForAlignment();
    }
  }

  public String getProperty(String property)
  {
    if (properties == null)
    {
      return null;
    }
    return properties.get(property);
  }

  public void setProperty(String property, String value)
  {
    if (properties == null)
    {
      properties = new HashMap<>();
    }
    properties.put(property, value);
  }

  public boolean hasProperties()
  {
    return properties != null && properties.size() > 0;
  }

  public Collection<String> getProperties()
  {
    if (properties == null)
    {
      return Collections.emptyList();
    }
    return properties.keySet();
  }

  /**
   * Returns the Annotation for the given sequence position (base 1) if any,
   * else null
   * 
   * @param position
   * @return
   */
  public Annotation getAnnotationForPosition(int position)
  {
    return sequenceMapping == null ? null : sequenceMapping.get(position);

  }

  /**
   * Set the id to "ann" followed by a counter that increments so as to be
   * unique for the lifetime of the JVM
   */
  protected final void setAnnotationId()
  {
    this.annotationId = ANNOTATION_ID_PREFIX + Long.toString(nextId());
  }

  /**
   * Returns the match for the last unmatched opening RNA helix pair symbol
   * preceding the given column, or '(' if nothing found to match.
   * 
   * @param column
   * @return
   */
  public String getDefaultRnaHelixSymbol(int column)
  {
    String result = "(";
    if (annotations == null)
    {
      return result;
    }

    /*
     * for each preceding column, if it contains an open bracket, 
     * count whether it is still unmatched at column, if so return its pair
     * (likely faster than the fancy alternative using stacks)
     */
    for (int col = column - 1; col >= 0; col--)
    {
      Annotation annotation = annotations[col];
      if (annotation == null)
      {
        continue;
      }
      String displayed = annotation.displayCharacter;
      if (displayed == null || displayed.length() != 1)
      {
        continue;
      }
      char symbol = displayed.charAt(0);
      if (!Rna.isOpeningParenthesis(symbol))
      {
        continue;
      }

      /*
       * found an opening bracket symbol
       * count (closing-opening) symbols of this type that follow it,
       * up to and excluding the target column; if the count is less
       * than 1, the opening bracket is unmatched, so return its match
       */
      String closer = String
              .valueOf(Rna.getMatchingClosingParenthesis(symbol));
      String opener = String.valueOf(symbol);
      int count = 0;
      for (int j = col + 1; j < column; j++)
      {
        if (annotations[j] != null)
        {
          String s = annotations[j].displayCharacter;
          if (closer.equals(s))
          {
            count++;
          }
          else if (opener.equals(s))
          {
            count--;
          }
        }
      }
      if (count < 1)
      {
        return closer;
      }
    }
    return result;
  }

  protected static synchronized long nextId()
  {
    return counter++;
  }

  /**
   * 
   * @return true for rows that have a range of values in their annotation set
   */
  public boolean isQuantitative()
  {
    return graphMin < graphMax;
  }

  /**
   * delete any columns in alignmentAnnotation that are hidden (including
   * sequence associated annotation).
   * 
   * @param hiddenColumns
   *          the set of hidden columns
   */
  public void makeVisibleAnnotation(HiddenColumns hiddenColumns)
  {
    if (annotations != null)
    {
      makeVisibleAnnotation(0, annotations.length, hiddenColumns);
    }
  }

  /**
   * delete any columns in alignmentAnnotation that are hidden (including
   * sequence associated annotation).
   * 
   * @param start
   *          remove any annotation to the right of this column
   * @param end
   *          remove any annotation to the left of this column
   * @param hiddenColumns
   *          the set of hidden columns
   */
  public void makeVisibleAnnotation(int start, int end,
          HiddenColumns hiddenColumns)
  {
    if (annotations != null)
    {
      if (hiddenColumns.hasHiddenColumns())
      {
        removeHiddenAnnotation(start, end, hiddenColumns);
      }
      else
      {
        restrict(start, end);
      }
    }
  }

  /**
   * The actual implementation of deleting hidden annotation columns
   * 
   * @param start
   *          remove any annotation to the right of this column
   * @param end
   *          remove any annotation to the left of this column
   * @param hiddenColumns
   *          the set of hidden columns
   */
  private void removeHiddenAnnotation(int start, int end,
          HiddenColumns hiddenColumns)
  {
    // mangle the alignmentAnnotation annotation array
    ArrayList<Annotation[]> annels = new ArrayList<>();
    Annotation[] els = null;

    int w = 0;

    Iterator<int[]> blocks = hiddenColumns.getVisContigsIterator(start,
            end + 1, false);

    int copylength;
    int annotationLength;
    while (blocks.hasNext())
    {
      int[] block = blocks.next();
      annotationLength = block[1] - block[0] + 1;

      if (blocks.hasNext())
      {
        // copy just the visible segment of the annotation row
        copylength = annotationLength;
      }
      else
      {
        if (annotationLength + block[0] <= annotations.length)
        {
          // copy just the visible segment of the annotation row
          copylength = annotationLength;
        }
        else
        {
          // copy to the end of the annotation row
          copylength = annotations.length - block[0];
        }
      }

      els = new Annotation[annotationLength];
      annels.add(els);
      System.arraycopy(annotations, block[0], els, 0, copylength);
      w += annotationLength;
    }

    if (w != 0)
    {
      annotations = new Annotation[w];

      w = 0;
      for (Annotation[] chnk : annels)
      {
        System.arraycopy(chnk, 0, annotations, w, chnk.length);
        w += chnk.length;
      }
    }
  }

  public static Iterable<AlignmentAnnotation> findAnnotations(
          Iterable<AlignmentAnnotation> list, SequenceI seq, String calcId,
          String label)
  {

    ArrayList<AlignmentAnnotation> aa = new ArrayList<>();
    for (AlignmentAnnotation ann : list)
    {
      if ((calcId == null || (ann.getCalcId() != null
              && ann.getCalcId().equals(calcId)))
              && (seq == null || (ann.sequenceRef != null
                      && ann.sequenceRef == seq))
              && (label == null
                      || (ann.label != null && ann.label.equals(label))))
      {
        aa.add(ann);
      }
    }
    return aa;
  }

  /**
   * Answer true if any annotation matches the calcId passed in (if not null).
   * 
   * @param list
   *          annotation to search
   * @param calcId
   * @return
   */
  public static boolean hasAnnotation(List<AlignmentAnnotation> list,
          String calcId)
  {

    if (calcId != null && !"".equals(calcId))
    {
      for (AlignmentAnnotation a : list)
      {
        if (a.getCalcId() == calcId)
        {
          return true;
        }
      }
    }
    return false;
  }

  public static Iterable<AlignmentAnnotation> findAnnotation(
          List<AlignmentAnnotation> list, String calcId)
  {

    List<AlignmentAnnotation> aa = new ArrayList<>();
    if (calcId == null)
    {
      return aa;
    }
    for (AlignmentAnnotation a : list)
    {

      if (a.getCalcId() == calcId || (a.getCalcId() != null
              && calcId != null && a.getCalcId().equals(calcId)))
      {
        aa.add(a);
      }
    }
    return aa;
  }
  
  /**
   * convenience method to check for the 'CONTACT_MAP_NOGROUPS' property for this alignment annotation row
   * @return true if no CONTACT_MAP_NOGROUPS property is found, or it is set to ""
   */
  public boolean isShowGroupsForContactMatrix()
  {
    return getProperty(AlignmentAnnotation.CONTACT_MAP_NOGROUPS)==null || "".equals(getProperty(AlignmentAnnotation.CONTACT_MAP_NOGROUPS));
  }
  /**
   * set the 'CONTACT_MAP_NOGROUPS' property for this alignment annotation row
   * @see isShowGroupsForContactMatrix
   */
  public void setShowGroupsForContactMatrix(boolean showGroups)
  {
    setProperty(AlignmentAnnotation.CONTACT_MAP_NOGROUPS, showGroups ? "" : "nogroups");
  }


}
