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
package jalview.renderer;

import jalview.analysis.Conservation;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeI;

import java.awt.Color;
import java.util.Map;

public interface ResidueShaderI
{

  public abstract void setConsensus(ProfilesI cons);
  
  public abstract void setVariance(ProfilesI cons);

  public abstract boolean conservationApplied();

  public abstract void setConservationApplied(boolean conservationApplied);

  public abstract void setConservation(Conservation cons);

  public abstract void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps);

  /**
   * Sets the percentage consensus threshold value, and whether gaps are ignored
   * in percentage identity calculation
   * 
   * @param consensusThreshold
   * @param ignoreGaps
   */
  public abstract void setThreshold(int consensusThreshold,
          boolean ignoreGaps);

  public abstract void setConservationInc(int i);

  public abstract int getConservationInc();

  /**
   * Get the percentage threshold for this colour scheme
   * 
   * @return Returns the percentage threshold
   */
  public abstract int getThreshold();

  /**
   * Returns the possibly context dependent colour for the given symbol at the
   * aligned position in the given sequence. For example, the colour may depend
   * on the symbol's relationship to the consensus residue for the column.
   * 
   * @param symbol
   * @param position
   * @param seq
   * @return
   */
  public abstract Color findColour(char symbol, int position,
          SequenceI seq);

  public abstract ColourSchemeI getColourScheme();

  public abstract void setColourScheme(ColourSchemeI cs);

}