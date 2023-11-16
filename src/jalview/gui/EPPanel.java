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
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;

/**
 * The panel holding the Pairwise Similarity Map 3-D visualisation
 */
public class EPPanel 
        implements Runnable
{

  AlignViewport av;

  private boolean working;
  
  private EquivalentPositions ep;
  
  private int startingPosition;
  
  private char FoR;

  /**
   * Constructor given sequence data, a similarity (or distance) score model
   * name, and score calculation parameters
   * 
   * @param alignPanel
   * @param modelName
   * @param params
   */
  public EPPanel(AlignViewport alignViewport, int startingPosition, char FoR)
  {
    this.av = alignViewport;
    this.startingPosition = startingPosition;
    this.FoR = FoR;
  
    //addInternalFrameListener(new InternalFrameAdapter()
    //{
      //@Override
      //public void internalFrameClosed(InternalFrameEvent e)
      //{
        //close_actionPerformed();
      //}
    //});

    boolean selected = av.getSelectionGroup() != null
            && av.getSelectionGroup().getSize() > 0;
    SequenceI[] seqs;
    if (!selected)
    {
      seqs = av.getAlignment().getSequencesArray();
    }
    else
    {
      seqs = av.getSelectionGroup().getSequencesInOrder(av.getAlignment());
    }

    //setNfModel(
            //new NfModel(av, seqs, nucleotide, scoreModel));
    //PaintRefresher.Register(this, av.getSequenceSetId());
  }

  /**
   * Ensure references to potentially very large objects (the PaSiMap matrices) are
   * nulled when the frame is closed
   */
  //protected void close_actionPerformed()
  //{
    //setNfModel(null);
  //}

  /**
   * Calculates the PaSiMap and displays the results
   */
  @Override
  public void run()
  {
    working = true;
    String message = MessageManager.getString("label.ep_recalculating");
    try
    {
      //getNfModel().calculate();
      ep = new EquivalentPositions(av, startingPosition, FoR);
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
   * Answers true if PaSiMap calculation is in progress, else false
   * 
   * @return
   */
  public boolean isWorking()
  {
    return working;
  }

  /**
   * Sets the input data used to calculate the PaSiMap. This is provided for
   * 'restore from project', which does not currently support this (AL-2647), so
   * sets the value to null, and hides the menu option for "Input Data...". J
   * 
   * @param data
   */
  //public void setInputData(AlignmentViewport data)
  //{
    //getPasimapModel().setInputData(data);
    //originalSeqData.setVisible(data != null);
  //}

  public AlignViewportI getAlignViewport()
  {
    return av;
  }

}
