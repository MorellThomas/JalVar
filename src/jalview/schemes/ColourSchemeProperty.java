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
import jalview.datamodel.AnnotatedCollectionI;
import jalview.util.ColorUtils;

import java.awt.Color;

/**
 * ColourSchemeProperty binds names to hardwired colourschemes and tries to deal
 * intelligently with mapping unknown names to user defined colourschemes (that
 * exist or can be created from the string representation of the colourscheme
 * name - either a hex RGB triplet or a named colour under java.awt.color ). The
 * values of the colourscheme constants is important for callers of
 * getColourName(int i), since it can be used to enumerate the set of built in
 * colours. The FIRST_COLOUR and LAST_COLOUR symbols are provided for this.
 * 
 * @author $author$
 * @version $Revision$
 */
public class ColourSchemeProperty
{

  /**
   * Returns a colour scheme for the given name, with which the given data may
   * be coloured. The name is not case-sensitive, and may be one of
   * <ul>
   * <li>any currently registered colour scheme; Jalview by default
   * provides</li>
   * <ul>
   * <li>Clustal</li>
   * <li>Blosum62</li>
   * <li>% Identity</li>
   * <li>Hydrophobic</li>
   * <li>Zappo</li>
   * <li>Taylor</li>
   * <li>Helix Propensity</li>
   * <li>Strand Propensity</li>
   * <li>Turn Propensity</li>
   * <li>Buried Index</li>
   * <li>Nucleotide</li>
   * <li>Purine/Pyrimidine</li>
   * <li>T-Coffee Scores</li>
   * <li>RNA Helices</li>
   * </ul>
   * <li>the name of a programmatically added colour scheme</li>
   * <li>an AWT colour name e.g. red</li>
   * <li>an AWT hex rgb colour e.g. ff2288</li>
   * <li>residue colours list e.g. D,E=red;K,R,H=0022FF;c=yellow</li>
   * </ul>
   * 
   * If none of these formats is matched, the string is converted to a colour
   * using a hashing algorithm. For name "None", returns null.
   * 
   * @param forData
   * @param name
   * @return
   */
  public static ColourSchemeI getColourScheme(AlignViewportI view,
          AnnotatedCollectionI forData, String name)
  {
    if (ResidueColourScheme.NONE.equalsIgnoreCase(name))
    {
      return null;

    }

    /*
     * if this is the name of a registered colour scheme, just
     * create a new instance of it
     */
    ColourSchemeI scheme = ColourSchemes.getInstance().getColourScheme(name,
            view, forData, null);
    if (scheme != null)
    {
      return scheme;
    }

    /*
     * try to parse the string as a residues colour scheme
     * e.g. A=red;T,G=blue etc
     * else parse the name as a colour specification
     * e.g. "red" or "ff00ed",
     * or failing that hash the name to a colour
     */
    UserColourScheme ucs = new UserColourScheme(name);
    return ucs;
  }

  public static Color rnaHelices[] = null;

  public static void initRnaHelicesShading(int n)
  {
    int j = 0;
    if (rnaHelices == null)
    {
      rnaHelices = new Color[n + 1];
    }
    else if (rnaHelices != null && rnaHelices.length <= n)
    {
      Color[] t = new Color[n + 1];
      System.arraycopy(rnaHelices, 0, t, 0, rnaHelices.length);
      j = rnaHelices.length;
      rnaHelices = t;
    }
    else
    {
      return;
    }
    // Generate random colors and store
    for (; j <= n; j++)
    {
      rnaHelices[j] = ColorUtils.generateRandomColor(Color.white);
    }
  }

  /**
   * delete the existing cached RNA helices colours
   */
  public static void resetRnaHelicesShading()
  {
    rnaHelices = null;
  }

  /**
   * Returns the name of the colour scheme (or "None" if it is null)
   * 
   * @param cs
   * @return
   */
  public static String getColourName(ColourSchemeI cs)
  {
    return cs == null ? ResidueColourScheme.NONE : cs.getSchemeName();
  }

}
