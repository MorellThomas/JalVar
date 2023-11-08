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
package jalview.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGHints;
import org.jibble.epsgraphics.EpsGraphics2D;

import jalview.bin.Console;
import jalview.io.JalviewFileChooser;
import jalview.util.imagemaker.BitmapImageSizing;

public class ImageMaker
{
  public static final String SVG_DESCRIPTION = "Scalable Vector Graphics";

  public static final String SVG_EXTENSION = "svg";

  public static final String EPS_DESCRIPTION = "Encapsulated Postscript";

  public static final String EPS_EXTENSION = "eps";

  public static final String PNG_EXTENSION = "png";

  public static final String PNG_DESCRIPTION = "Portable  network graphics";

  EpsGraphics2D pg;

  Graphics graphics;

  FileOutputStream out;

  BufferedImage bi;

  TYPE type;

  public enum TYPE
  {
    EPS("EPS", MessageManager.getString("label.eps_file"), EPS_EXTENSION,
            EPS_DESCRIPTION),
    PNG("PNG", MessageManager.getString("label.png_image"), PNG_EXTENSION,
            PNG_DESCRIPTION),
    SVG("SVG", "SVG", SVG_EXTENSION, SVG_DESCRIPTION);

    public final String name;

    public final String label;

    public final String extension;

    public final String description;

    TYPE(String name, String label, String ext, String desc)
    {
      this.name = name;
      this.label = label;
      this.extension = ext;
      this.description = desc;
    }

    public String getName()
    {
      return name;
    }

    public JalviewFileChooser getFileChooser()
    {
      return new JalviewFileChooser(extension, description);
    }

    public String getLabel()
    {
      return label;
    }

  }

  /**
   * Constructor configures the graphics context ready for writing to
   * 
   * @param imageType
   * @param width
   * @param height
   * @param file
   * @param fileTitle
   * @param useLineart
   * @param bitmapscale
   * @throws IOException
   */
  public ImageMaker(TYPE imageType, int width, int height, File file,
          String fileTitle, boolean useLineart, BitmapImageSizing userBis)
          throws IOException
  {
    this.type = imageType;

    out = new FileOutputStream(file);
    switch (imageType)
    {
    case SVG:
      setupSVG(width, height, useLineart);
      break;
    case EPS:
      setupEPS(width, height, fileTitle, useLineart);
      break;
    case PNG:
      setupPNG(width, height, userBis);
      break;
    default:
    }
  }

  public Graphics getGraphics()
  {
    return graphics;
  }

  /**
   * For SVG or PNG, writes the generated graphics data to the file output
   * stream. For EPS, flushes the output graphics (which is written to file as
   * it is generated).
   */
  public void writeImage()
  {
    try
    {
      switch (type)
      {
      case EPS:
        pg.flush();
        pg.close();
        break;
      case SVG:
        String svgData = ((SVGGraphics2D) getGraphics()).getSVGDocument();
        out.write(svgData.getBytes());
        out.flush();
        out.close();
        break;
      case PNG:
        ImageIO.write(bi, PNG_EXTENSION, out);
        out.flush();
        out.close();
        break;
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Sets up a graphics object for the PNG image to be written on
   * 
   * @param width
   * @param height
   * @param scale
   */
  protected void setupPNG(int width, int height, BitmapImageSizing userBis)
  {
    if (width == 0 || height == 0)
      return;

    BitmapImageSizing bis = ImageMaker.getScaleWidthHeight(width, height,
            userBis);
    float usescale = bis.scale;
    int usewidth = bis.width;
    int useheight = bis.height;

    bi = new BufferedImage(usewidth, useheight, BufferedImage.TYPE_INT_RGB);
    graphics = bi.getGraphics();
    Graphics2D ig2 = (Graphics2D) graphics;
    ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    if (usescale > 0.0f)
    {
      ig2.scale(usescale, usescale);
    }
  }

  /**
   * A helper method to configure the SVG output graphics, with choice of Text
   * or Lineart character rendering
   * 
   * @param width
   * @param height
   * @param useLineart
   *          true for Lineart character rendering, false for Text
   */
  protected void setupSVG(int width, int height, boolean useLineart)
  {
    SVGGraphics2D g2 = new SVGGraphics2D(width, height);
    if (useLineart)
    {
      g2.setRenderingHint(SVGHints.KEY_DRAW_STRING_TYPE,
              SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR);
    }
    graphics = g2;
  }

  /**
   * A helper method that sets up the EPS graphics output with user choice of
   * Text or Lineart character rendering
   * 
   * @param width
   * @param height
   * @param title
   * @param useLineart
   *          true for Lineart character rendering, false for Text
   * @throws IOException
   */
  protected void setupEPS(int width, int height, String title,
          boolean useLineart) throws IOException
  {
    pg = new EpsGraphics2D(title, out, 0, 0, width, height);
    Graphics2D ig2 = pg;
    ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    pg.setAccurateTextMode(useLineart);
    graphics = pg;
  }

  /**
   * Takes suggested float scale, int width, int height and create a bounding
   * box returned as a BitmapImageSizing object with consistent scale, width,
   * height fields.
   * 
   * @param scale
   * @param bitmapwidth
   * @param bitmapheight
   * @return BitmapImageSizing
   */
  public static BitmapImageSizing getScaleWidthHeight(int width, int height,
          float scale, int bitmapwidth, int bitmapheight)
  {
    float usescale = 0.0f;
    int usewidth = width;
    int useheight = height;

    // use the smallest positive scale (i.e. fit in the box)
    if (scale > 0.0f)
    {
      usescale = scale;
      usewidth = Math.round(scale * width);
      useheight = Math.round(scale * height);
    }
    if (bitmapwidth > 0)
    {
      float wscale = (float) bitmapwidth / width;
      if (wscale > 0.0f && (usescale == 0.0f || wscale < usescale))
      {
        usescale = wscale;
        usewidth = bitmapwidth;
        useheight = Math.round(usescale * height);
      }
    }
    if (bitmapheight > 0)
    {
      float hscale = (float) bitmapheight / height;
      if (hscale > 0.0f && (usescale == 0.0f || hscale < usescale))
      {
        usescale = hscale;
        usewidth = Math.round(usescale * width);
        useheight = bitmapheight;
      }
    }
    return new BitmapImageSizing(usescale, usewidth, useheight);
  }

  /**
   * Takes suggested scale, width, height as a BitmapImageSizing object and
   * create a bounding box returned as a BitmapImageSizing object with
   * consistent scale, width, height fields.
   * 
   * @param bis
   * @return BitmapImageSizing
   */
  public static BitmapImageSizing getScaleWidthHeight(int width, int height,
          BitmapImageSizing bis)
  {
    return ImageMaker.getScaleWidthHeight(width, height, bis.scale,
            bis.width, bis.height);
  }

  /**
   * Takes String versions of suggested float scale, int width, int height and
   * create a bounding box returned as a BitmapImageSizing object with
   * consistent scale, width, height fields.
   * 
   * @param scaleS
   * @param widthS
   * @param heightS
   * @return BitmapImageSizing
   */
  public static BitmapImageSizing parseScaleWidthHeightStrings(
          String scaleS, String widthS, String heightS)
  {
    float scale = 0.0f;
    int width = 0;
    int height = 0;

    if (scaleS != null)
    {
      try
      {
        scale = Float.parseFloat(scaleS);
      } catch (NumberFormatException e)
      {
        Console.warn("Did not understand scale '" + scaleS
                + "', won't be used.");
      }
    }
    if (widthS != null)
    {
      try
      {
        width = Integer.parseInt(widthS);
      } catch (NumberFormatException e)
      {
        Console.warn("Did not understand width '" + widthS
                + "', won't be used.");
      }
    }
    if (heightS != null)
    {
      try
      {
        height = Integer.parseInt(heightS);
      } catch (NumberFormatException e)
      {
        Console.warn("Did not understand height '" + heightS
                + "', won't be used.");
      }
    }

    return new BitmapImageSizing(scale, width, height);
  }
}
