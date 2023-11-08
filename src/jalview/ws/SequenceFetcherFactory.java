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
package jalview.ws;

import jalview.ws.seqfetcher.ASequenceFetcher;

public class SequenceFetcherFactory
{

  private static SequenceFetcher instance;

  /**
   * Returns a new SequenceFetcher object, or a mock object if one has been set
   * 
   * @return
   */
  public static ASequenceFetcher getSequenceFetcher()
  {
    return instance == null ? new SequenceFetcher() : instance;
  }

  /**
   * Set the instance object to use (intended for unit testing with mock
   * objects).
   * 
   * Be sure to reset to null in the tearDown method of any tests!
   * 
   * @param sf
   */
  public static void setSequenceFetcher(SequenceFetcher sf)
  {
    instance = sf;
  }
}
