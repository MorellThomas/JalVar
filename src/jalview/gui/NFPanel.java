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

import jalview.analysis.NaturalFrequencies;
import jalview.api.AlignViewportI;
import jalview.viewmodel.AlignmentViewport;

/**
 * The panel starts the Natural Frequencies calculation
 * called by CustomChooser
 */
public class NFPanel implements Runnable
{
  AlignmentPanel ap;

  AlignmentViewport av;
  
  private final String refFile;

  private boolean working;
  
  private NaturalFrequencies nf;

  /**
   * Constructor given sequence data
   * 
   * @param alignPanel
   */
  public NFPanel(AlignmentPanel alignPanel, String referenceFile)
  {
    this.av = alignPanel.av;
    this.ap = alignPanel;
    this.refFile = referenceFile;

  }

  /**
   * Calculates the Natural Frequencies and displays the results
   */
  @Override
  public void run()
  {
    working = true;
    try
    {
      nf = new NaturalFrequencies(av, refFile);
      nf.run(); // executes in same thread, wait for completion

    } catch (OutOfMemoryError er)
    {
      new OOMWarning("calculating Natural Frequencies", er);
      working = false;
      return;
    }

    working = false;
  }


  /**
   * Answers true if PaSiMap calculation is in progress, else false
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
