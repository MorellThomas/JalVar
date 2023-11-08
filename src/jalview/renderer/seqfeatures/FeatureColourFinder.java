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
package jalview.renderer.seqfeatures;

import jalview.api.AlignViewportI;
import jalview.api.FeatureRenderer;
import jalview.api.FeaturesDisplayedI;
import jalview.datamodel.SequenceI;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * A helper class to find feature colour using an associated FeatureRenderer
 * 
 * @author gmcarstairs
 *
 */
public class FeatureColourFinder
{
  /*
   * the class we delegate feature finding to
   */
  private FeatureRenderer featureRenderer;

  /*
   * a 1-pixel image on which features can be drawn, for the case where
   * transparency allows 'see-through' of multiple feature colours
   */
  private BufferedImage offscreenImage;

  /**
   * Constructor
   * 
   * @param fr
   */
  public FeatureColourFinder(FeatureRenderer fr)
  {
    featureRenderer = fr;
    offscreenImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
  }

  /**
   * Answers the feature colour to show for the given sequence and column
   * position. This delegates to the FeatureRenderer to find the colour, which
   * will depend on feature location, visibility, ordering, colour scheme, and
   * whether or not transparency is applied. For feature rendering with
   * transparency, this class provides a dummy 'offscreen' graphics context
   * where multiple feature colours can be overlaid and the combined colour read
   * back.
   * <p>
   * This method is not thread-safe when transparency is applied, since a shared
   * BufferedImage would be used by all threads to hold the composite colour at
   * a position. Each thread should use a separate instance of this class.
   * 
   * @param defaultColour
   * @param seq
   * @param column
   *          alignment column position (0..)
   * @return
   */
  public Color findFeatureColour(Color defaultColour, SequenceI seq,
          int column)
  {
    if (noFeaturesDisplayed())
    {
      return defaultColour;
    }

    Graphics g = null;

    /*
     * if transparency applies, provide a notional 1x1 graphics context 
     * that has been primed with the default colour
     */
    if (featureRenderer.getTransparency() != 1f)
    {
      g = offscreenImage.getGraphics();
      if (defaultColour != null)
      {
        offscreenImage.setRGB(0, 0, defaultColour.getRGB());
      }
    }

    Color c = featureRenderer.findFeatureColour(seq, column + 1, g);
    if (c == null)
    {
      return defaultColour;
    }

    if (g != null)
    {
      c = new Color(offscreenImage.getRGB(0, 0));
    }
    return c;
  }

  /**
   * Answers true if feature display is turned off, or there are no features
   * configured to be visible
   * 
   * @return
   */
  boolean noFeaturesDisplayed()
  {
    if (featureRenderer == null)
    {
      return true;
    }
    AlignViewportI av = featureRenderer.getViewport();
    if (av.isShowComplementFeatures())
    {
      return false;
    }
    if (!av.isShowSequenceFeatures())
    {
      return true;
    }

    if (!((FeatureRendererModel) featureRenderer).hasRenderOrder())
    {
      return true;
    }

    FeaturesDisplayedI displayed = featureRenderer.getFeaturesDisplayed();
    if (displayed == null || displayed.getVisibleFeatureCount() == 0)
    {
      return true;
    }

    return false;
  }
}
