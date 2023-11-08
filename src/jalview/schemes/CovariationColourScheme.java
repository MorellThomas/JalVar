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
import jalview.datamodel.AnnotatedCollectionI;
import jalview.util.ColorUtils;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Map;

/**
 * Became RNAHelicesColour.java. Placeholder for true covariation color scheme
 * 
 * @author Lauren Michelle Lui
 * @version 2.5
 */
public class CovariationColourScheme extends ResidueColourScheme
{
  public Map<String, Color> helixcolorhash = new Hashtable<>();

  public Map<Integer, String> positionsToHelix = new Hashtable<>();

  int numHelix = 0;

  public AlignmentAnnotation annotation;

  /**
   * Returns a new instance of this colour scheme with which the given data may
   * be coloured
   */
  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new CovariationColourScheme(coll.getAlignmentAnnotation()[0]);
  }

  /**
   * Creates a new CovariationColourScheme object.
   */
  public CovariationColourScheme(AlignmentAnnotation annotation)
  {
    this.annotation = annotation;

    for (int x = 0; x < this.annotation._rnasecstr.length; x++)
    {
      // System.out.println(this.annotation._rnasecstr[x] + " Begin" +
      // this.annotation._rnasecstr[x].getBegin());
      // System.out.println(this.annotation._rnasecstr[x].getFeatureGroup());
      // pairs.put(this.annotation._rnasecstr[x].getBegin(),
      // this.annotation._rnasecstr[x].getEnd());

      positionsToHelix.put(this.annotation._rnasecstr[x].getBegin(),
              this.annotation._rnasecstr[x].getFeatureGroup());
      positionsToHelix.put(this.annotation._rnasecstr[x].getEnd(),
              this.annotation._rnasecstr[x].getFeatureGroup());

      if (Integer.parseInt(
              this.annotation._rnasecstr[x].getFeatureGroup()) > numHelix)
      {
        numHelix = Integer
                .parseInt(this.annotation._rnasecstr[x].getFeatureGroup());
      }

    }

    for (int j = 0; j <= numHelix; j++)
    {
      helixcolorhash.put(String.valueOf(j),
              ColorUtils.generateRandomColor(Color.white));
    }

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
    // System.out.println("called"); log.debug
    // Generate a random pastel color

    return ResidueProperties.purinepyrimidine[ResidueProperties.purinepyrimidineIndex[c]];// jalview.util.ColorUtils.generateRandomColor(Color.white);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Color findColour(char c, int j)
  {
    Color currentColour = Color.white;
    String currentHelix = null;
    // System.out.println(c + " " + j);
    currentHelix = positionsToHelix.get(j);
    // System.out.println(positionsToHelix.get(j));

    if (currentHelix != null)
    {
      currentColour = helixcolorhash.get(currentHelix);
    }

    // System.out.println(c + " " + j + " helix " + currentHelix + " " +
    // currentColour);
    return currentColour;
  }

  @Override
  public boolean isNucleotideSpecific()
  {
    return true;
  }

  @Override
  public String getSchemeName()
  {
    return "Covariation";
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
