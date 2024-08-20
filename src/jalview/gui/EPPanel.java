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

import jalview.analysis.EquivalentPositions;
import jalview.api.AlignViewportI;

/**
 * The panel calculates the equivalent position to genomic position conversion
 * called by EpInput
 */
public class EPPanel 
        implements Runnable
{

  AlignFrame af;
  
  AlignViewport av;

  private boolean working;
  
  private EquivalentPositions ep;
  
  private int[] startingPosition;
  
  private char[] FoR;
  
  private int width;

  /**
   * Constructor given sequence data, the starting gene position, the strand and the sequence length
   * name, and score calculation parameters
   * 
   * @param alignViewport
   * @param startingPosition 
   * @param FoR
   * @param width
   */
  public EPPanel(AlignFrame alignFrame)
  {
    this(alignFrame, new int[]{-1}, new char[]{' '}, alignFrame.getViewport().getAlignment().getWidth());
  }
  public EPPanel(AlignFrame alignFrame, int[] startingPosition, char[] FoR, int width)
  {
    this.af = alignFrame;
    this.av = af.getViewport();
    this.startingPosition = startingPosition;
    this.FoR = FoR;
    this.width = width;
  }

  /**
   * Calculates the equivalent positions and displays the results
   */
  @Override
  public void run()
  {
    working = true;
    try
    {
      ep = new EquivalentPositions(af, startingPosition, FoR, width);
      ep.run(); // executes in same thread, wait for completion

    } catch (OutOfMemoryError er)
    {
      new OOMWarning("calculating Equivalent Positions", er);
      working = false;
      return;
    }

    working = false;
  }


  /**
   * Answers true if EP calculation is in progress, else false
   * 
   * @return
   */
  public boolean isWorking()
  {
    return working;
  }

  public AlignViewportI getAlignViewport()
  {
    return av;
  }

}
