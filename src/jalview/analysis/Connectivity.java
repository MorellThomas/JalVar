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

//import jalview.datamodel.AlignmentView;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.viewmodel.AlignmentViewport;

import java.util.Hashtable;

/**
 * @Author MorellThomas
 */

public class Connectivity
{

  /**
   * Returns the number of unique connections for each sequence
   * only connections with a score of above 0 count
   * 
   * @param av sequences
   * @param scores alignment scores
   *
   * @return connectivity
   */
  public static Hashtable<SequenceI, Integer> getConnectivity(AlignmentViewport av, float[][] scores, byte dim) throws RuntimeException
  {
    SequenceI[] sequences = av.getAlignment().getSequencesArray();

    Hashtable<SequenceI, Integer> connectivity = new Hashtable<SequenceI, Integer>();
    // for each unique connection
    for (int i = 0; i < sequences.length; i++)
    {
      connectivity.putIfAbsent(sequences[i], 0);
      for (int j = 0; j < i; j++)
      {
        connectivity.putIfAbsent(sequences[j], 0);
	int iOld = connectivity.get(sequences[i]);
	int jOld = connectivity.get(sequences[j]); 
        // count the connection if its score is not NaN
	if (!Float.isNaN(scores[i][j]))
	{
	  connectivity.put(sequences[i], ++iOld);
	  connectivity.put(sequences[j], ++jOld);
	}
      }
    }

    // if a sequence has too few connections, abort
    connectivity.forEach((sequence, connection) ->
    {
      System.out.println(String.format("%s: %d", sequence.getName(), connection));
      if (connection < dim)
      {
	// a popup saying that it failed would be nice
	throw new ConnectivityException(sequence.getName(), connection, dim);
      }
    } );

    return connectivity;
  }

}
