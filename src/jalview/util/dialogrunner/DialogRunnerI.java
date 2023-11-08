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
package jalview.util.dialogrunner;

/**
 * An interface for blocking dialog response handling. This is motivated by
 * JalviewJS - when running as Javascript, there is only a single thread, and
 * blocking dialogs have to be responsible for performing any actions required
 * for user responses.
 * 
 * @author jprocter
 *
 */
public interface DialogRunnerI
{

  /**
   * Sets the action to be performed when the dialog returns the given response.
   * Note this also handles <code>int</code>-valued responses, which will be
   * converted to <code>Integer</code> when this method is invoked.
   * 
   * @param response
   * @param action
   * @return
   */
  DialogRunnerI setResponseHandler(Object response, Runnable action);

  // DialogRunnerI setResponseHandler(Object response, Runnable action);

  /**
   * Runs the registered handler (if any) for the given response. The default
   * action is to do nothing. Typically an action will be need on 'OK' or other
   * positive selection in the dialog. An action might in some cases also be
   * needed for a 'Cancel' response.
   * 
   * @param response
   * @return
   */
  default void handleResponse(Object response)
  {
  }
}
