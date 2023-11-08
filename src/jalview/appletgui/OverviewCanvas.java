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
package jalview.appletgui;

import jalview.renderer.OverviewRenderer;
import jalview.renderer.OverviewResColourFinder;
import jalview.viewmodel.OverviewDimensions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;

public class OverviewCanvas extends Component
{
  // This is set true if the alignment view changes whilst
  // the overview is being calculated
  private volatile boolean restart = false;

  private volatile boolean updaterunning = false;

  private OverviewDimensions od;

  private OverviewRenderer or = null;

  private Image miniMe;

  private Image offscreen;

  private AlignViewport av;

  private jalview.renderer.seqfeatures.FeatureRenderer fr;

  private Frame nullFrame;

  public OverviewCanvas(OverviewDimensions overviewDims,
          AlignViewport alignvp)
  {
    od = overviewDims;
    av = alignvp;

    nullFrame = new Frame();
    nullFrame.addNotify();

    fr = new jalview.renderer.seqfeatures.FeatureRenderer(av);
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

  public void draw(boolean showSequenceFeatures, boolean showAnnotation,
          FeatureRenderer transferRenderer)
  {
    miniMe = null;

    if (showSequenceFeatures)
    {
      fr.transferSettings(transferRenderer);
    }

    setPreferredSize(new Dimension(od.getWidth(), od.getHeight()));

    or = new OverviewRenderer(fr, od, av.getAlignment(),
            av.getResidueShading(), new OverviewResColourFinder());
    miniMe = nullFrame.createImage(od.getWidth(), od.getHeight());
    offscreen = nullFrame.createImage(od.getWidth(), od.getHeight());

    miniMe = or.draw(od.getRows(av.getAlignment()),
            od.getColumns(av.getAlignment()));

    Graphics mg = miniMe.getGraphics();

    // checks for conservation annotation to make sure overview works for DNA
    // too
    if (showAnnotation)
    {
      mg.translate(0, od.getSequencesHeight());
      or.drawGraph(mg, av.getAlignmentConservationAnnotation(),
              od.getGraphHeight(), od.getColumns(av.getAlignment()));
      mg.translate(0, -od.getSequencesHeight());
    }

    if (restart)
    {
      restart = false;
      draw(showSequenceFeatures, showAnnotation, transferRenderer);
    }
    else
    {
      updaterunning = false;
    }
  }

  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {
    Graphics og = offscreen.getGraphics();
    if (miniMe != null)
    {
      og.drawImage(miniMe, 0, 0, this);
      og.setColor(Color.red);
      od.drawBox(og);
      g.drawImage(offscreen, 0, 0, this);
    }
  }

  /**
   * Nulls references to protect against potential memory leaks
   */
  void dispose()
  {
    od = null;
  }

}
