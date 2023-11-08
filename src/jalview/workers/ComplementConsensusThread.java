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
package jalview.workers;

import jalview.analysis.AAFrequency;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;

import java.util.ConcurrentModificationException;
import java.util.Hashtable;

/**
 * A thread to recompute the consensus of the cDNA complement for a linked
 * protein alignment.
 * 
 * @author gmcarstairs
 *
 */
public class ComplementConsensusThread extends ConsensusThread
{

  public ComplementConsensusThread(AlignViewportI alignViewport,
          AlignmentViewPanel alignPanel)
  {
    super(alignViewport, alignPanel);
  }

  @Override
  protected AlignmentAnnotation getConsensusAnnotation()
  {
    return alignViewport.getComplementConsensusAnnotation();
  }

  @Override
  protected Hashtable<String, Object>[] getViewportConsensus()
  {
    return alignViewport.getComplementConsensusHash();
  }

  /**
   * Calculate the cDNA consensus and store it on the Viewport
   */
  @Override
  protected void computeConsensus(AlignmentI alignment)
  {
    @SuppressWarnings("unchecked")
    Hashtable<String, Object>[] hconsensus = new Hashtable[alignment
            .getWidth()];

    // SequenceI[] aseqs = getSequences();

    /*
     * Allow 3 tries at this, since this thread can start up while we are still
     * modifying protein-codon mappings on the alignment
     */
    for (int i = 0; i < 3; i++)
    {
      try
      {
        AAFrequency.calculateCdna(alignment, hconsensus);
        break;
      } catch (ConcurrentModificationException e)
      {
        // try again
      }
    }

    alignViewport.setComplementConsensusHash(hconsensus);
  }

  /**
   * Convert the computed consensus data into the desired annotation for
   * display.
   * 
   * @param consensusAnnotation
   *          the annotation to be populated
   * @param consensusData
   *          the computed consensus data
   */
  protected void deriveConsensus(AlignmentAnnotation consensusAnnotation,
          Hashtable<String, Object>[] consensusData)
  {
    AAFrequency.completeCdnaConsensus(consensusAnnotation, consensusData,
            alignViewport.isShowSequenceLogo(), getSequences().length);
  }

  @Override
  public void updateResultAnnotation(boolean immediate)
  {
    AlignmentAnnotation consensus = getConsensusAnnotation();
    Hashtable<String, Object>[] hconsensus = getViewportConsensus();
    if (immediate || !calcMan.isWorking(this) && consensus != null
            && hconsensus != null)
    {
      deriveConsensus(consensus, hconsensus);
    }
  }

}
