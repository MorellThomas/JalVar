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
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Map;

/**
 * Looks at the information computed from an RNA Stockholm format file on the
 * secondary structure of the alignment. Extracts the information on the
 * positions of the helices present and assigns colors.
 * 
 * @author Lauren Michelle Lui
 * @version 2.5
 */
public class RNAHelicesColour extends ResidueColourScheme
{

  /**
   * Maps sequence positions to the RNA helix they belong to. Key: position,
   * Value: helix TODO: Revise or drop in favour of annotation position numbers
   */
  public Hashtable<Integer, String> positionsToHelix = new Hashtable<>();

  /**
   * Number of helices in the RNA secondary structure
   */
  int numHelix = 0;

  public AlignmentAnnotation annotation;

  /**
   * Default constructor (required for ColourSchemes cache)
   */
  public RNAHelicesColour()
  {

  }

  /**
   * Creates a new RNAHelicesColour object.
   */
  public RNAHelicesColour(AlignmentAnnotation annotation)
  {
    super(ResidueProperties.nucleotideIndex);
    this.annotation = annotation;
    ColourSchemeProperty.resetRnaHelicesShading();
    refresh();
  }

  public RNAHelicesColour(AnnotatedCollectionI alignment)
  {
    super(ResidueProperties.nucleotideIndex);
    ColourSchemeProperty.resetRnaHelicesShading();
    alignmentChanged(alignment, null);
  }

  /**
   * clones colour settings and annotation row data
   * 
   * @param rnaHelicesColour
   */
  public RNAHelicesColour(RNAHelicesColour rnaHelicesColour)
  {
    super(ResidueProperties.nucleotideIndex);
    annotation = rnaHelicesColour.annotation;
    refresh();
  }

  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {

    // This loop will find the first rna structure annotation by which to colour
    // the sequences.
    AlignmentAnnotation[] annotations = alignment.getAlignmentAnnotation();
    if (annotations == null)
    {
      return;
    }
    for (int i = 0; i < annotations.length; i++)
    {

      // is this a sensible way of determining type of annotation?
      if (annotations[i].visible && annotations[i].isRNA()
              && annotations[i].isValidStruc())
      {
        annotation = annotations[i];
        break;
      }
    }

    refresh();

  }

  private long lastrefresh = -1;

  public void refresh()
  {

    if (annotation != null && ((annotation._rnasecstr == null
            || lastrefresh != annotation._rnasecstr.hashCode())
            && annotation.isValidStruc()))
    {
      annotation.getRNAStruc();
      lastrefresh = annotation._rnasecstr.hashCode();
      numHelix = 0;
      positionsToHelix = new Hashtable<>();

      // Figure out number of helices
      // Length of rnasecstr is the number of pairs of positions that base pair
      // with each other in the secondary structure
      for (int x = 0; x < this.annotation._rnasecstr.length; x++)
      {

        /*
         * System.out.println(this.annotation._rnasecstr[x] + " Begin" +
         * this.annotation._rnasecstr[x].getBegin());
         */
        // System.out.println(this.annotation._rnasecstr[x].getFeatureGroup());

        positionsToHelix.put(this.annotation._rnasecstr[x].getBegin(),
                this.annotation._rnasecstr[x].getFeatureGroup());
        positionsToHelix.put(this.annotation._rnasecstr[x].getEnd(),
                this.annotation._rnasecstr[x].getFeatureGroup());

        if (Integer.parseInt(
                this.annotation._rnasecstr[x].getFeatureGroup()) > numHelix)
        {
          numHelix = Integer.parseInt(
                  this.annotation._rnasecstr[x].getFeatureGroup());
        }

      }
      ColourSchemeProperty.initRnaHelicesShading(numHelix);
    }
  }

  /**
   * Returns default color base on purinepyrimidineIndex in
   * jalview.schemes.ResidueProperties (Allows coloring in sequence logo)
   * 
   * @param c
   *          Character in sequence
   * 
   * @return color in RGB
   */
  @Override
  public Color findColour(char c)
  {
    return ResidueProperties.purinepyrimidine[ResidueProperties.purinepyrimidineIndex[c]];
    // random colors for all positions
    // jalview.util.ColorUtils.generateRandomColor(Color.white); If you want
  }

  /**
   * Returns color based on helices
   * 
   * @param c
   *          Character in sequence
   * @param j
   *          position in sequence - used to locate helix
   * 
   * @return Color in RGB
   */
  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    refresh();
    Color currentColour = Color.white;
    String currentHelix = null;
    currentHelix = positionsToHelix.get(j);
    if (currentHelix != null)
    {
      currentColour = ColourSchemeProperty.rnaHelices[Integer
              .parseInt(currentHelix)];
    }
    return currentColour;
  }

  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI sg)
  {
    return new RNAHelicesColour(sg);
  }

  @Override
  public boolean isNucleotideSpecific()
  {
    return true;
  }

  /**
   * Answers true if the data has RNA secondary structure annotation
   */
  @Override
  public boolean isApplicableTo(AnnotatedCollectionI ac)
  {
    if (ac instanceof AlignmentI && ((AlignmentI) ac).hasRNAStructure())
    {
      return true;
    }

    /*
     * not currently supporting this option for group annotation / colouring
     */
    return false;
  }

  @Override
  public String getSchemeName()
  {
    return JalviewColourScheme.RNAHelices.toString();
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
