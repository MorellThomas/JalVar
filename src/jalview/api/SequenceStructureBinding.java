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
package jalview.api;

/**
 * methods for interacting with the binding between a structure visualization
 * and one or more Jalview views.
 * 
 * @author JimP prototype class - this may disappear !
 */
public interface SequenceStructureBinding
{

  /**
   * 
   * @return true if Jalview or the Viewer is still restoring state or loading
   *         is still going on (see setFinishedLoadingFromArchive)
   */
  void setLoadingFromArchive(boolean loadingFromArchive);

  boolean isLoadingFromArchive();

  /**
   * modify flag which controls if sequence colouring events are honoured by the
   * binding. Should be true for normal operation
   * 
   * @param finishedLoading
   */
  void setFinishedLoadingFromArchive(boolean finishedLoading);

  boolean isLoadingFinished();

}
