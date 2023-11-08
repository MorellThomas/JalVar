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
package jalview.structures.models;

import jalview.api.SequenceStructureBinding;

public class SequenceStructureBindingModel
        implements SequenceStructureBinding
{

  /**
   * set if Structure Viewer state is being restored from some source -
   * instructs binding not to apply default display style when structure set is
   * updated for first time.
   */
  private boolean loadingFromArchive = false;

  /**
   * second flag to indicate if the Structure viewer should ignore sequence
   * colouring events from the structure manager because the GUI is still
   * setting up
   */
  private boolean loadingFinished = true;

  @Override
  public void setLoadingFromArchive(boolean loadingFromArchive)
  {
    this.loadingFromArchive = loadingFromArchive;
  }

  /**
   * 
   * @return true if viewer is still restoring state or loading is still going
   *         on (see setFinishedLoadingFromArchive)
   */
  @Override
  public boolean isLoadingFromArchive()
  {
    return loadingFromArchive && !loadingFinished;
  }

  @Override
  public boolean isLoadingFinished()
  {
    return loadingFinished;
  }

  @Override
  public void setFinishedLoadingFromArchive(boolean finishedLoading)
  {
    loadingFinished = finishedLoading;
  }

}
