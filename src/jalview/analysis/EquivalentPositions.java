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
package jalview.analysis;

import jalview.bin.Console;
import jalview.viewmodel.AlignmentViewport;

import java.util.Set;

/**
 * Performs Principal Component Analysis on given sequences
 * @AUTHOR MorellThomas 
 */
public class EquivalentPositions implements Runnable
{
  /*
   * inputs
   */
  final private AlignmentViewport seqs;
  
  final private int startingPosition;
  
  final char FoR;

  /*
   * outputs
   */

  /**
   * Constructor given the sequences to compute for, the similarity model to
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param sm
   * @param options
   */
  public EquivalentPositions(AlignmentViewport sequences, int startingPosition, char FoR)
  {
    this.seqs = sequences;
    this.startingPosition = startingPosition;
    this.FoR = FoR;
  }

  /**
   * Performs the Natural Frequencies calculation
   *
   */
  @Override
  public void run()
  {
    try
    {

      System.out.println(String.format("EP: %s, %d, %c", startingPosition, FoR));
      Set <String> names = seqs.getAlignment().getSequenceNames();
      for (String name : names)
      {
        System.out.println(name);
      }
      
    } catch (Exception q)
    {
      Console.error("Error computing Equivalent Positions:  " + q.getMessage());
      q.printStackTrace();
    }
  }

}
