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
package jalview.ext.rbvi.chimera;

import jalview.httpserver.AbstractRequestHandler;
import jalview.structure.SelectionSource;

import java.net.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a simple Http handler that can listen for selections in Chimera.
 * <p/>
 * Lifecycle:
 * <ul>
 * <li>Start the Chimera process</li>
 * <li>Start the REST service on Chimera, get the port number it is listening on
 * </li>
 * <li>Start the ChimeraListener, get the URL it is listening on</li>
 * <li>The first listener started will start the singleton HttpServer</li>
 * <li>Send a 'listen' command to Chimera with the URL of the listener</li>
 * <li>When Jalview's Chimera window is closed, shut down the
 * ChimeraListener</li>
 * <li>Multiple linked Chimera instances will each have a separate listener (but
 * share one Http server)</li>
 * </ul>
 * 
 * @author gmcarstairs
 *
 */
public class ChimeraListener extends AbstractRequestHandler
        implements SelectionSource
{
  /*
   * Chimera notification parameter name
   */
  private static final String CHIMERA_NOTIFICATION = "chimeraNotification";

  /*
   * Chimera model changed notifications start with this
   */
  private static final String MODEL_CHANGED = "ModelChanged: ";

  /*
   * Chimera selection changed notification message
   */
  private static final String SELECTION_CHANGED = "SelectionChanged: selection changed\n";

  /*
   * A static counter so each listener can be associated with a distinct context
   * root (/chimera0,1,2,3...). This is needed so we can fetch selections from
   * multiple Chimera instances without confusion.
   */
  private static int chimeraId = 0;

  /*
   * Prefix for path below context root (myChimeraId is appended)
   */
  private static final String PATH_PREFIX = "chimera";

  /*
   * Value of chimeraId (0, 1, 2...) for this instance
   */
  private int myChimeraId = 0;

  /*
   * A reference to the object by which we can talk to Chimera
   */
  private JalviewChimeraBinding chimeraBinding;

  /**
   * Constructor that registers this as an Http request handler on path
   * /chimeraN, where N is incremented for each instance. Call getUri to get the
   * resulting URI for this handler.
   * 
   * @param chimeraBinding
   * @throws BindException
   *           if no free port can be assigned
   */
  public ChimeraListener(JalviewChimeraBinding binding) throws BindException
  {
    myChimeraId = chimeraId++;
    this.chimeraBinding = binding;
    setPath(PATH_PREFIX + myChimeraId);
    registerHandler();
  }

  /**
   * Process a message from Chimera
   */
  @Override
  protected void processRequest(HttpServletRequest request,
          HttpServletResponse response)
  {
    // dumpRequest(request);
    String message = request.getParameter(CHIMERA_NOTIFICATION);
    if (message == null)
    {
      message = request.getParameter("chimerax_notification");
    }
    if (message != null)
    {
      if (message.startsWith("SelectionChanged"))
      {
        this.chimeraBinding.highlightChimeraSelection();
      }
      else if (message.startsWith(MODEL_CHANGED))
      {
        System.err.println(message);
        processModelChanged(message.substring(MODEL_CHANGED.length()));
      }
      else
      {
        System.err.println("Unexpected chimeraNotification: " + message);
      }
    }
  }

  /**
   * Handle a ModelChanged notification from Chimera
   * 
   * @param substring
   */
  protected void processModelChanged(String message)
  {
    // System.out.println(message + " (not implemented in Jalview)");
  }

  /**
   * Returns a display name for this service
   */
  @Override
  public String getName()
  {
    return "ChimeraListener";
  }
}
