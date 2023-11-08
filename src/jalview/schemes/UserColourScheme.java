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

import jalview.api.AlignViewportI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.util.ColorUtils;
import jalview.util.StringUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class UserColourScheme extends ResidueColourScheme
{
  /*
   * lookup (by symbol index) of lower case colours (if configured)
   */
  Color[] lowerCaseColours;

  protected String schemeName;

  public UserColourScheme()
  {
    super(ResidueProperties.aaIndex);
  }

  public UserColourScheme(Color[] newColors)
  {
    super(ResidueProperties.aaIndex);
    colors = newColors;
  }

  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI sg)
  {
    return new UserColourScheme(this);
  }

  /**
   * Copy constructor
   * 
   * @return
   */
  protected UserColourScheme(UserColourScheme from)
  {
    this(from.colors);
    schemeName = from.schemeName;
    if (from.lowerCaseColours != null)
    {
      lowerCaseColours = new Color[from.lowerCaseColours.length];
      System.arraycopy(from.lowerCaseColours, 0, lowerCaseColours, 0,
              from.lowerCaseColours.length);
    }
  }

  /**
   * Constructor for an animino acid colour scheme. The colour specification may
   * be one of
   * <ul>
   * <li>an AWT colour name e.g. red</li>
   * <li>an AWT hex rgb colour e.g. ff2288</li>
   * <li>residue colours list e.g. D,E=red;K,R,H=0022FF;c=yellow</li>
   * </ul>
   * 
   * @param colour
   */
  public UserColourScheme(String colour)
  {
    super(ResidueProperties.aaIndex);

    if (colour.contains("="))
    {
      /*
       * a list of colours per residue(s)
       */
      parseAppletParameter(colour);
      return;
    }

    Color col = ColorUtils.parseColourString(colour);

    if (col == null)
    {
      System.out.println("Making colour from name: " + colour);
      col = ColorUtils.createColourFromName(colour);
    }

    setAll(col);
    schemeName = colour;
  }

  /**
   * Sets all symbols to the specified colour
   * 
   * @param col
   */
  protected void setAll(Color col)
  {
    if (symbolIndex == null)
    {
      return;
    }
    int max = 0;
    for (int index : symbolIndex)
    {
      max = Math.max(max, index);
    }
    colors = new Color[max + 1];
    for (int i = 0; i <= max; i++)
    {
      colors[i] = col;
    }
  }

  public Color[] getColours()
  {
    return colors;
  }

  public Color[] getLowerCaseColours()
  {
    return lowerCaseColours;
  }

  public void setName(String name)
  {
    schemeName = name;
  }

  public String getName()
  {
    return schemeName;
  }

  /**
   * Parse and save residue colours specified as (for example)
   * 
   * <pre>
   *     D,E=red; K,R,H=0022FF; c=100,50,75
   * </pre>
   * 
   * This should be a semi-colon separated list of colours, which may be defined
   * by colour name, hex value or comma-separated RGB triple. Each colour is
   * defined for a comma-separated list of amino acid single letter codes. (Note
   * that this also allows a colour scheme to be defined for ACGT, but not for
   * U.)
   * 
   * @param paramValue
   */
  void parseAppletParameter(String paramValue)
  {
    setAll(Color.white);

    StringTokenizer st = new StringTokenizer(paramValue, ";");
    StringTokenizer st2;
    String token = null, colour, residues;
    try
    {
      while (st.hasMoreElements())
      {
        token = st.nextToken().trim();
        residues = token.substring(0, token.indexOf("="));
        colour = token.substring(token.indexOf("=") + 1);

        st2 = new StringTokenizer(residues, " ,");
        while (st2.hasMoreTokens())
        {
          String residue = st2.nextToken();

          int colIndex = ResidueProperties.aaIndex[residue.charAt(0)];
          if (colIndex == -1)
          {
            continue;
          }

          if (residue.equalsIgnoreCase("lowerCase"))
          {
            if (lowerCaseColours == null)
            {
              lowerCaseColours = new Color[colors.length];
            }
            for (int i = 0; i < lowerCaseColours.length; i++)
            {
              if (lowerCaseColours[i] == null)
              {
                lowerCaseColours[i] = ColorUtils.parseColourString(colour);
              }
            }

            continue;
          }

          if (residue.equals(residue.toLowerCase(Locale.ROOT)))
          {
            if (lowerCaseColours == null)
            {
              lowerCaseColours = new Color[colors.length];
            }
            lowerCaseColours[colIndex] = ColorUtils
                    .parseColourString(colour);
          }
          else
          {
            colors[colIndex] = ColorUtils.parseColourString(colour);
          }
        }
      }
    } catch (Exception ex)
    {
      System.out.println(
              "Error parsing userDefinedColours:\n" + token + "\n" + ex);
    }

  }

  public void setLowerCaseColours(Color[] lcolours)
  {
    lowerCaseColours = lcolours;
  }

  /**
   * Returns the colour for the given residue character. If the residue is
   * lower-case, and there is a specific colour defined for lower case, that
   * colour is returned, else the colour for the upper case residue.
   */
  @Override
  public Color findColour(char c)
  {
    if ('a' <= c && c <= 'z' && lowerCaseColours != null)
    {
      Color colour = lowerCaseColours[symbolIndex[c]];
      if (colour != null)
      {
        return colour;
      }
    }
    return super.findColour(c);
  }

  /**
   * Answers the customised name of the colour scheme, if it has one, else "User
   * Defined"
   */
  @Override
  public String getSchemeName()
  {
    if (schemeName != null && schemeName.length() > 0)
    {
      return schemeName;
    }
    return ResidueColourScheme.USER_DEFINED;
  }

  /**
   * Generate an applet colour parameter like A,C,D=12ffe9;Q,W=2393fd;w=9178dd
   * 
   * @return
   */
  public String toAppletParameter()
  {
    /*
     * step 1: build a map from colours to the symbol(s) that have the colour
     */
    Map<Color, List<String>> colours = new HashMap<>();

    for (char symbol = 'A'; symbol <= 'Z'; symbol++)
    {
      String residue = String.valueOf(symbol);
      int index = symbolIndex[symbol];
      Color c = colors[index];
      if (c != null && !c.equals(Color.white))
      {
        if (colours.get(c) == null)
        {
          colours.put(c, new ArrayList<String>());
        }
        colours.get(c).add(residue);
      }
      if (lowerCaseColours != null)
      {
        c = lowerCaseColours[index];
        if (c != null && !c.equals(Color.white))
        {
          residue = residue.toLowerCase(Locale.ROOT);
          if (colours.get(c) == null)
          {
            colours.put(c, new ArrayList<String>());
          }
          colours.get(c).add(residue);
        }
      }
    }

    /*
     * step 2: make a list of { A,G,R=12f9d6 } residues/colour specs
     */
    List<String> residueColours = new ArrayList<>();
    for (Entry<Color, List<String>> cols : colours.entrySet())
    {
      boolean first = true;
      StringBuilder sb = new StringBuilder();
      for (String residue : cols.getValue())
      {
        if (!first)
        {
          sb.append(",");
        }
        sb.append(residue);
        first = false;
      }
      sb.append("=");
      /*
       * get color as hex value, dropping the alpha (ff) part
       */
      String hexString = Integer.toHexString(cols.getKey().getRGB())
              .substring(2);
      sb.append(hexString);
      residueColours.add(sb.toString());
    }

    /*
     * sort and output
     */
    Collections.sort(residueColours);
    return StringUtils.listToDelimitedString(residueColours, ";");
  }

  @Override
  public boolean hasGapColour()
  {
    return (findColour(' ') != null);
  }
}
