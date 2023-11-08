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
package jalview.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import javax.swing.JPanel;

import jalview.api.AlignViewportI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.renderer.OverviewRenderer;
import jalview.renderer.OverviewResColourFinder;
import jalview.viewmodel.OverviewDimensions;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;

public class OverviewCanvas extends JPanel
{
  public static final Color OVERVIEW_DEFAULT_GAP = Color.lightGray;

  public static final Color OVERVIEW_DEFAULT_LEGACY_GAP = Color.white;

  public static final Color OVERVIEW_DEFAULT_RESIDUE = Color.white;

  public static final Color OVERVIEW_DEFAULT_LEGACY_RESIDUE = Color.lightGray;

  public static final Color OVERVIEW_DEFAULT_HIDDEN = Color.darkGray
          .darker();

  private static final Color TRANS_GREY = new Color(100, 100, 100, 25);

  // This is set true if the alignment view changes whilst
  // the overview is being calculated
  private volatile boolean restart = false;

  private volatile boolean updaterunning = false;

  private boolean dispose = false;

  private BufferedImage miniMe;

  private BufferedImage lastMiniMe = null;

  // Can set different properties in this seqCanvas than
  // main visible SeqCanvas
  private SequenceRenderer sr;

  private jalview.renderer.seqfeatures.FeatureRenderer fr;

  private OverviewDimensions od;

  private OverviewRenderer or = null;

  private AlignViewportI av;

  private OverviewResColourFinder cf;

  private ProgressPanel progressPanel;

  public OverviewCanvas(OverviewDimensions overviewDims,
          AlignViewportI alignvp, ProgressPanel pp)
  {
    od = overviewDims;
    av = alignvp;
    progressPanel = pp;

    sr = new SequenceRenderer(av);
    sr.renderGaps = false;
    fr = new jalview.renderer.seqfeatures.FeatureRenderer(av);

    boolean useLegacy = Cache.getDefault(Preferences.USE_LEGACY_GAP, false);
    Color gapCol = Cache.getDefaultColour(Preferences.GAP_COLOUR,
            OVERVIEW_DEFAULT_GAP);
    Color hiddenCol = Cache.getDefaultColour(Preferences.HIDDEN_COLOUR,
            OVERVIEW_DEFAULT_HIDDEN);
    Color residueCol = useLegacy ? OVERVIEW_DEFAULT_LEGACY_RESIDUE
            : OVERVIEW_DEFAULT_RESIDUE;

    cf = new OverviewResColourFinder(gapCol, residueCol, hiddenCol);

    setSize(od.getWidth(), od.getHeight());
  }

  /**
   * Update the overview dimensions object used by the canvas (e.g. if we change
   * from showing hidden columns to hiding them or vice versa)
   * 
   * @param overviewDims
   */
  public void resetOviewDims(OverviewDimensions overviewDims)
  {
    od = overviewDims;
  }

  /**
   * Signals to drawing code that the associated alignment viewport has changed
   * and a redraw will be required
   */
  public boolean restartDraw()
  {
    synchronized (this)
    {
      if (updaterunning)
      {
        restart = true;
        if (or != null)
        {
          or.setRedraw(true);
        }
      }
      else
      {
        updaterunning = true;
      }
      return restart;
    }
  }

  /**
   * Draw the overview sequences
   * 
   * @param showSequenceFeatures
   *          true if sequence features are to be shown
   * @param showAnnotation
   *          true if the annotation is to be shown
   * @param transferRenderer
   *          the renderer to transfer feature colouring from
   */
  public void draw(boolean showSequenceFeatures, boolean showAnnotation,
          FeatureRendererModel transferRenderer)
  {
    miniMe = null;

    if (showSequenceFeatures)
    {
      fr.transferSettings(transferRenderer);
    }

    setPreferredSize(new Dimension(od.getWidth(), od.getHeight()));

    or = new OverviewRenderer(fr, od, av.getAlignment(),
            av.getResidueShading(), cf);

    or.addPropertyChangeListener(progressPanel);

    miniMe = or.draw(od.getRows(av.getAlignment()),
            od.getColumns(av.getAlignment()));

    Graphics mg = miniMe.getGraphics();

    if (showAnnotation)
    {
      mg.translate(0, od.getSequencesHeight());
      or.drawGraph(mg, av.getAlignmentConservationAnnotation(),
              od.getGraphHeight(), od.getColumns(av.getAlignment()));
      mg.translate(0, -od.getSequencesHeight());
    }

    or.removePropertyChangeListener(progressPanel);
    or = null;
    if (restart)
    {
      restart = false;
      if (!dispose)
      {
        draw(showSequenceFeatures, showAnnotation, transferRenderer);
      }
    }
    else
    {
      updaterunning = false;
      lastMiniMe = miniMe;
    }
  }

  @Override
  public void paintComponent(Graphics g)
  {
    // super.paintComponent(g);

    if (restart)
    {
      if (lastMiniMe == null)
      {
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
      }
      else
      {
        g.drawImage(lastMiniMe, 0, 0, getWidth(), getHeight(), this);
      }
      g.setColor(TRANS_GREY);
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    else if (lastMiniMe != null)
    {
      // is this a resize?
      if ((getWidth() > 0) && (getHeight() > 0)
              && ((getWidth() != od.getWidth())
                      || (getHeight() != od.getHeight())))
      {
        // if there is annotation, scale the alignment and annotation
        // separately
        if (od.getGraphHeight() > 0 && od.getSequencesHeight() > 0 // BH 2019
        )
        {
          try
          {
            BufferedImage topImage = lastMiniMe.getSubimage(0, 0,
                    od.getWidth(), od.getSequencesHeight());
            BufferedImage bottomImage = lastMiniMe.getSubimage(0,
                    od.getSequencesHeight(), od.getWidth(),
                    od.getGraphHeight());

            // must be done at this point as we rely on using old width/height
            // above, and new width/height below
            od.setWidth(getWidth());
            od.setHeight(getHeight());

            // stick the images back together so lastMiniMe is consistent in the
            // event of a repaint - BUT probably not thread safe
            lastMiniMe = new BufferedImage(od.getWidth(), od.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics lg = lastMiniMe.getGraphics();
            lg.drawImage(topImage, 0, 0, od.getWidth(),
                    od.getSequencesHeight(), null);
            lg.drawImage(bottomImage, 0, od.getSequencesHeight(),
                    od.getWidth(), od.getGraphHeight(), this);
            lg.dispose();
          } catch (RasterFormatException e)
          {
            Console.debug(
                    "Scaling miscalculation resizing Overview window");
            od.setWidth(getWidth());
            od.setHeight(getHeight());
          }
        }
        else
        {
          od.setWidth(getWidth());
          od.setHeight(getHeight());
        }

        // make sure the box is in the right place
        od.setBoxPosition(av.getAlignment().getHiddenSequences(),
                av.getAlignment().getHiddenColumns());
      }
      // fall back to normal behaviour
      g.drawImage(lastMiniMe, 0, 0, getWidth(), getHeight(), this);
    }
    else
    {
      g.drawImage(lastMiniMe, 0, 0, getWidth(), getHeight(), this);
    }

    // draw the box
    g.setColor(Color.red);
    od.drawBox(g);
  }

  public void dispose()
  {
    dispose = true;
    od = null;
    synchronized (this)
    {
      restart = true;
      if (or != null)
      {
        or.setRedraw(true);
      }
    }
  }

  public Color getGapColour()
  {
    return cf.getGapColour();
  }

  public Color getHiddenColour()
  {
    return cf.getHiddenColour();
  }

  public Color getResidueColour()
  {
    return cf.getResidueColour();
  }

  /**
   * Sets the colours to use for gaps, residues and hidden regions
   * 
   * @param gaps
   * @param residues
   * @param hidden
   */
  public void setColours(Color gaps, Color residues, Color hidden)
  {
    cf = new OverviewResColourFinder(gaps, residues, hidden);
  }
}
