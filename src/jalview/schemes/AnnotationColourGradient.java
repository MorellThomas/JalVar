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

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.Annotation;
import jalview.datamodel.GraphLine;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.renderer.AnnotationRenderer;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.IdentityHashMap;
import java.util.Map;

public class AnnotationColourGradient extends FollowerColourScheme
{
  public static final int NO_THRESHOLD = -1;

  public static final int BELOW_THRESHOLD = 0;

  public static final int ABOVE_THRESHOLD = 1;

  private final AlignmentAnnotation annotation;

  private final int aboveAnnotationThreshold;

  public boolean thresholdIsMinMax = false;

  private GraphLine annotationThreshold;

  private int redMin;

  private int greenMin;

  private int blueMin;

  private int redRange;

  private int greenRange;

  private int blueRange;

  private boolean predefinedColours = false;

  private boolean seqAssociated = false;

  /**
   * false if the scheme was constructed without a minColour and maxColour used
   * to decide if existing colours should be taken from annotation elements when
   * they exist
   */
  private boolean noGradient = false;

  private IdentityHashMap<SequenceI, AlignmentAnnotation> seqannot = null;

  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI sg)
  {
    AnnotationColourGradient acg = new AnnotationColourGradient(annotation,
            getColourScheme(), aboveAnnotationThreshold);
    acg.thresholdIsMinMax = thresholdIsMinMax;
    acg.annotationThreshold = (annotationThreshold == null) ? null
            : new GraphLine(annotationThreshold);
    acg.redMin = redMin;
    acg.greenMin = greenMin;
    acg.blueMin = blueMin;
    acg.redRange = redRange;
    acg.greenRange = greenRange;
    acg.blueRange = blueRange;
    acg.predefinedColours = predefinedColours;
    acg.seqAssociated = seqAssociated;
    acg.noGradient = noGradient;
    return acg;
  }

  /**
   * Creates a new AnnotationColourGradient object.
   */
  public AnnotationColourGradient(AlignmentAnnotation annotation,
          ColourSchemeI originalColour, int aboveThreshold)
  {
    if (originalColour instanceof AnnotationColourGradient)
    {
      setColourScheme(((AnnotationColourGradient) originalColour)
              .getColourScheme());
    }
    else
    {
      setColourScheme(originalColour);
    }

    this.annotation = annotation;

    aboveAnnotationThreshold = aboveThreshold;

    if (aboveThreshold != NO_THRESHOLD && annotation.threshold != null)
    {
      annotationThreshold = annotation.threshold;
    }
    // clear values so we don't get weird black bands...
    redMin = 254;
    greenMin = 254;
    blueMin = 254;
    redRange = 0;
    greenRange = 0;
    blueRange = 0;

    noGradient = true;
    checkLimits();
  }

  /**
   * Creates a new AnnotationColourGradient object.
   */
  public AnnotationColourGradient(AlignmentAnnotation annotation,
          Color minColour, Color maxColour, int aboveThreshold)
  {
    this.annotation = annotation;

    aboveAnnotationThreshold = aboveThreshold;

    if (aboveThreshold != NO_THRESHOLD && annotation.threshold != null)
    {
      annotationThreshold = annotation.threshold;
    }

    redMin = minColour.getRed();
    greenMin = minColour.getGreen();
    blueMin = minColour.getBlue();

    redRange = maxColour.getRed() - redMin;
    greenRange = maxColour.getGreen() - greenMin;
    blueRange = maxColour.getBlue() - blueMin;

    noGradient = false;
    checkLimits();
  }

  private void checkLimits()
  {
    aamax = annotation.graphMax;
    aamin = annotation.graphMin;
    if (annotation.isRNA())
    {
      // reset colour palette
      ColourSchemeProperty.resetRnaHelicesShading();
      ColourSchemeProperty.initRnaHelicesShading(1 + (int) aamax);
    }
  }

  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
    super.alignmentChanged(alignment, hiddenReps);

    if (seqAssociated && annotation.getCalcId() != null)
    {
      if (seqannot != null)
      {
        seqannot.clear();
      }
      else
      {
        seqannot = new IdentityHashMap<>();
      }
      // resolve the context containing all the annotation for the sequence
      AnnotatedCollectionI alcontext = alignment instanceof AlignmentI
              ? alignment
              : alignment.getContext();
      boolean f = true, rna = false;
      for (AlignmentAnnotation alan : alcontext
              .findAnnotation(annotation.getCalcId()))
      {
        if (alan.sequenceRef != null
                && (alan.label != null && annotation != null
                        && alan.label.equals(annotation.label)))
        {
          if (!rna && alan.isRNA())
          {
            rna = true;
          }
          seqannot.put(alan.sequenceRef, alan);
          if (f || alan.graphMax > aamax)
          {
            aamax = alan.graphMax;
          }
          if (f || alan.graphMin < aamin)
          {
            aamin = alan.graphMin;
          }
          f = false;
        }
      }
      if (rna)
      {
        ColourSchemeProperty.initRnaHelicesShading(1 + (int) aamax);
      }
    }
  }

  float aamin = 0f, aamax = 0f;

  public AlignmentAnnotation getAnnotation()
  {
    return annotation;
  }

  public int getAboveThreshold()
  {
    return aboveAnnotationThreshold;
  }

  public float getAnnotationThreshold()
  {
    if (annotationThreshold == null)
    {
      return 0;
    }
    else
    {
      return annotationThreshold.value;
    }
  }

  public Color getMinColour()
  {
    return new Color(redMin, greenMin, blueMin);
  }

  public Color getMaxColour()
  {
    return new Color(redMin + redRange, greenMin + greenRange,
            blueMin + blueRange);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public Color findColour(char c)
  {
    return Color.red;
  }

  /**
   * Returns the colour for a given character and position in a sequence
   * 
   * @param c
   *          the residue character
   * @param j
   *          the aligned position
   * @param seq
   *          the sequence
   * @return
   */
  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    /*
     * locate the annotation we are configured to colour by
     */
    AlignmentAnnotation ann = (seqAssociated && seqannot != null
            ? seqannot.get(seq)
            : this.annotation);

    /*
     * if gap or no annotation at position, no colour (White)
     */
    if (ann == null || ann.annotations == null
            || j >= ann.annotations.length || ann.annotations[j] == null
            || Comparison.isGap(c))
    {
      return Color.white;
    }

    Annotation aj = ann.annotations[j];
    // 'use original colours' => colourScheme != null
    // -> look up colour to be used
    // predefined colours => preconfigured shading
    // -> only use original colours reference if thresholding enabled &
    // minmax exists
    // annotation.hasIcons => null or black colours replaced with glyph
    // colours
    // -> reuse original colours if present
    // -> if thresholding enabled then return colour on non-whitespace glyph

    /*
     * if threshold applies, and annotation fails the test - no colour (white)
     */
    if (annotationThreshold != null)
    {
      if ((aboveAnnotationThreshold == ABOVE_THRESHOLD
              && aj.value <= annotationThreshold.value)
              || (aboveAnnotationThreshold == BELOW_THRESHOLD
                      && aj.value >= annotationThreshold.value))
      {
        return Color.white;
      }
    }

    /*
     * If 'use original colours' then return the colour of the annotation
     * at the aligned position - computed using the background colour scheme
     */
    if (predefinedColours && aj.colour != null
            && !aj.colour.equals(Color.black))
    {
      return aj.colour;
    }

    Color result = Color.white;
    if (ann.hasIcons && ann.graph == AlignmentAnnotation.NO_GRAPH)
    {
      /*
       * secondary structure symbol colouring
       */
      if (aj.secondaryStructure > ' ' && aj.secondaryStructure != '.'
              && aj.secondaryStructure != '-')
      {
        if (getColourScheme() != null)
        {
          result = getColourScheme().findColour(c, j, seq, null, 0f);
        }
        else
        {
          if (ann.isRNA())
          {
            result = ColourSchemeProperty.rnaHelices[(int) aj.value];
          }
          else
          {
            result = ann.annotations[j].secondaryStructure == 'H'
                    ? AnnotationRenderer.HELIX_COLOUR
                    : ann.annotations[j].secondaryStructure == 'E'
                            ? AnnotationRenderer.SHEET_COLOUR
                            : AnnotationRenderer.STEM_COLOUR;
          }
        }
      }
      else
      {
        return Color.white;
      }
    }
    else if (noGradient)
    {
      if (getColourScheme() != null)
      {
        result = getColourScheme().findColour(c, j, seq, null, 0f);
      }
      else
      {
        if (aj.colour != null)
        {
          result = aj.colour;
        }
      }
    }
    else
    {
      result = shadeCalculation(ann, j);
    }

    return result;
  }

  /**
   * Returns a graduated colour for the annotation at the given column. If there
   * is a threshold value, and it is used as the top/bottom of the colour range,
   * and the value satisfies the threshold condition, then a colour
   * proportionate to the range from the threshold is calculated. For all other
   * cases, a colour proportionate to the annotation's min-max range is
   * calulated. Note that thresholding is _not_ done here (a colour is computed
   * even if threshold is not passed).
   * 
   * @param ann
   * @param col
   * @return
   */
  Color shadeCalculation(AlignmentAnnotation ann, int col)
  {
    float range = 1f;
    float value = ann.annotations[col].value;
    if (thresholdIsMinMax && ann.threshold != null
            && aboveAnnotationThreshold == ABOVE_THRESHOLD
            && value >= ann.threshold.value)
    {
      range = ann.graphMax == ann.threshold.value ? 1f
              : (value - ann.threshold.value)
                      / (ann.graphMax - ann.threshold.value);
    }
    else if (thresholdIsMinMax && ann.threshold != null
            && aboveAnnotationThreshold == BELOW_THRESHOLD
            && value <= ann.threshold.value)
    {
      range = ann.graphMin == ann.threshold.value ? 0f
              : (value - ann.graphMin)
                      / (ann.threshold.value - ann.graphMin);
    }
    else
    {
      if (ann.graphMax != ann.graphMin)
      {
        range = (value - ann.graphMin) / (ann.graphMax - ann.graphMin);
      }
      else
      {
        range = 0f;
      }
    }

    int dr = (int) (redRange * range + redMin);
    int dg = (int) (greenRange * range + greenMin);
    int db = (int) (blueRange * range + blueMin);

    return new Color(dr, dg, db);
  }

  public boolean isPredefinedColours()
  {
    return predefinedColours;
  }

  public void setPredefinedColours(boolean predefinedColours)
  {
    this.predefinedColours = predefinedColours;
  }

  public boolean isSeqAssociated()
  {
    return seqAssociated;
  }

  public void setSeqAssociated(boolean sassoc)
  {
    seqAssociated = sassoc;
  }

  public boolean isThresholdIsMinMax()
  {
    return thresholdIsMinMax;
  }

  public void setThresholdIsMinMax(boolean minMax)
  {
    this.thresholdIsMinMax = minMax;
  }

  @Override
  public String getSchemeName()
  {
    return ANNOTATION_COLOUR;
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
