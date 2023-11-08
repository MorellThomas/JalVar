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
/**
 * author: Lauren Michelle Lui
 */

package jalview.util;

import java.util.Locale;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ColorUtils
{
  private static final int MAX_CACHE_SIZE = 1729;

  /*
   * a cache for colours generated from text strings
   */
  static Map<String, Color> myColours = new HashMap<>();

  /**
   * Generates a random color, will mix with input color. Code taken from
   * http://stackoverflow
   * .com/questions/43044/algorithm-to-randomly-generate-an-aesthetically
   * -pleasing-color-palette
   * 
   * @param mix
   * @return Random color in RGB
   */
  public static final Color generateRandomColor(Color mix)
  {
    Random random = new Random();
    int red = random.nextInt(256);
    int green = random.nextInt(256);
    int blue = random.nextInt(256);

    // mix the color
    if (mix != null)
    {
      red = (red + mix.getRed()) / 2;
      green = (green + mix.getGreen()) / 2;
      blue = (blue + mix.getBlue()) / 2;
    }

    Color color = new Color(red, green, blue);
    return color;

  }
  
  /**
   * 
   * @return random color
   */
  public static final Color getARandomColor()
  {

    Color col = new Color((int) (Math.random() * 255),
            (int) (Math.random() * 255), (int) (Math.random() * 255));
    return col;
  }
  /**
   * Convert to Tk colour code format
   * 
   * @param colour
   * @return
   * @see http
   *      ://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/colortool.html#
   *      tkcode
   */
  public static final String toTkCode(Color colour)
  {
    String colstring = "#" + ((colour.getRed() < 16) ? "0" : "")
            + Integer.toHexString(colour.getRed())
            + ((colour.getGreen() < 16) ? "0" : "")
            + Integer.toHexString(colour.getGreen())
            + ((colour.getBlue() < 16) ? "0" : "")
            + Integer.toHexString(colour.getBlue());
    return colstring;
  }

  /**
   * Returns a colour three shades darker. Note you can't guarantee that
   * brighterThan reverses this, as darkerThan may result in black.
   * 
   * @param col
   * @return
   */
  public static Color darkerThan(Color col)
  {
    return col == null ? null : col.darker().darker().darker();
  }

  /**
   * Returns a colour three shades brighter. Note you can't guarantee that
   * darkerThan reverses this, as brighterThan may result in white.
   * 
   * @param col
   * @return
   */
  public static Color brighterThan(Color col)
  {
    return col == null ? null : col.brighter().brighter().brighter();
  }

  /**
   * Returns a color between minColour and maxColour; the RGB values are in
   * proportion to where 'value' lies between minValue and maxValue
   * 
   * @param value
   * @param minValue
   * @param minColour
   * @param maxValue
   * @param maxColour
   * @return
   */
  public static Color getGraduatedColour(float value, float minValue,
          Color minColour, float maxValue, Color maxColour)
  {
    if (minValue == maxValue)
    {
      return minColour;
    }
    if (value < minValue)
    {
      value = minValue;
    }
    if (value > maxValue)
    {
      value = maxValue;
    }

    /*
     * prop = proportion of the way value is from minValue to maxValue
     */
    float prop = (value - minValue) / (maxValue - minValue);
    float r = minColour.getRed()
            + prop * (maxColour.getRed() - minColour.getRed());
    float g = minColour.getGreen()
            + prop * (maxColour.getGreen() - minColour.getGreen());
    float b = minColour.getBlue()
            + prop * (maxColour.getBlue() - minColour.getBlue());
    return new Color(r / 255, g / 255, b / 255);
  }

  /**
   * 'Fades' the given colour towards white by the specified proportion. A
   * factor of 1 or more results in White, a factor of 0 leaves the colour
   * unchanged, and a factor between 0 and 1 results in a proportionate change
   * of RGB values towards (255, 255, 255).
   * <p>
   * A negative bleachFactor can be specified to darken the colour towards Black
   * (0, 0, 0).
   * 
   * @param colour
   * @param bleachFactor
   * @return
   */
  public static Color bleachColour(Color colour, float bleachFactor)
  {
    if (bleachFactor >= 1f)
    {
      return Color.WHITE;
    }
    if (bleachFactor <= -1f)
    {
      return Color.BLACK;
    }
    if (bleachFactor == 0f)
    {
      return colour;
    }

    int red = colour.getRed();
    int green = colour.getGreen();
    int blue = colour.getBlue();

    if (bleachFactor > 0)
    {
      red += (255 - red) * bleachFactor;
      green += (255 - green) * bleachFactor;
      blue += (255 - blue) * bleachFactor;
      return new Color(red, green, blue);
    }
    else
    {
      float factor = 1 + bleachFactor;
      red *= factor;
      green *= factor;
      blue *= factor;
      return new Color(red, green, blue);
    }
  }

  /**
   * Parses a string into a Color, where the accepted formats are
   * <ul>
   * <li>an AWT colour name e.g. white</li>
   * <li>a hex colour value (without prefix) e.g. ff0000</li>
   * <li>an rgb triple e.g. 100,50,150</li>
   * </ul>
   * 
   * @param colour
   * @return the parsed colour, or null if parsing fails
   */
  public static Color parseColourString(String colour)
  {
    if (colour == null)
    {
      return null;
    }
    colour = colour.trim();

    Color col = null;
    try
    {
      int value = Integer.parseInt(colour, 16);
      col = new Color(value);
    } catch (NumberFormatException ex)
    {
    }

    if (col == null)
    {
      col = ColorUtils.getAWTColorFromName(colour);
    }

    if (col == null)
    {
      try
      {
        String[] tokens = colour.split(",");
        if (tokens.length == 3)
        {
          int r = Integer.parseInt(tokens[0].trim());
          int g = Integer.parseInt(tokens[1].trim());
          int b = Integer.parseInt(tokens[2].trim());
          col = new Color(r, g, b);
        }
      } catch (Exception ex)
      {
        // non-numeric token or out of 0-255 range
      }
    }

    return col;
  }

  /**
   * Constructs a colour from a text string. The hashcode of the whole string is
   * scaled to the range 0-135. This is added to RGB values made from the
   * hashcode of each third of the string, and scaled to the range 20-229.
   * 
   * @param name
   * @return
   */
  public static Color createColourFromName(String name)
  {
    if (name == null)
    {
      return Color.white;
    }
    if (myColours.containsKey(name))
    {
      return myColours.get(name);
    }
    int lsize = name.length();
    int start = 0;
    int end = lsize / 3;

    int rgbOffset = Math.abs(name.hashCode() % 10) * 15; // 0-135

    /*
     * red: first third
     */
    int r = Math.abs(name.substring(start, end).hashCode() + rgbOffset)
            % 210 + 20;
    start = end;
    end += lsize / 3;
    if (end > lsize)
    {
      end = lsize;
    }

    /*
     * green: second third
     */
    int g = Math.abs(name.substring(start, end).hashCode() + rgbOffset)
            % 210 + 20;

    /*
     * blue: third third
     */
    int b = Math.abs(name.substring(end).hashCode() + rgbOffset) % 210 + 20;

    Color color = new Color(r, g, b);

    if (myColours.size() < MAX_CACHE_SIZE)
    {
      myColours.put(name, color);
    }

    return color;
  }

  /**
   * Returns the Color constant for a given colour name e.g. "pink", or null if
   * the name is not recognised
   * 
   * @param name
   * @return
   */
  public static Color getAWTColorFromName(String name)
  {
    if (name == null)
    {
      return null;
    }
    Color col = null;
    name = name.toLowerCase(Locale.ROOT);

    // or make a static map; or use reflection on the field name
    switch (name)
    {
    case "black":
      col = Color.black;
      break;
    case "blue":
      col = Color.blue;
      break;
    case "cyan":
      col = Color.cyan;
      break;
    case "darkgray":
      col = Color.darkGray;
      break;
    case "gray":
      col = Color.gray;
      break;
    case "green":
      col = Color.green;
      break;
    case "lightgray":
      col = Color.lightGray;
      break;
    case "magenta":
      col = Color.magenta;
      break;
    case "orange":
      col = Color.orange;
      break;
    case "pink":
      col = Color.pink;
      break;
    case "red":
      col = Color.red;
      break;
    case "white":
      col = Color.white;
      break;
    case "yellow":
      col = Color.yellow;
      break;
    }

    return col;
  }
}
