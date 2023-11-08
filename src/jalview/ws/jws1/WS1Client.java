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
package jalview.ws.jws1;

import jalview.gui.AlignFrame;
import jalview.gui.WebserviceInfo;
import jalview.util.MessageManager;
import jalview.ws.WSClient;
import jalview.ws.WSMenuEntryProviderI;

import javax.swing.JMenu;

import ext.vamsas.ServiceHandle;

/**
 * JWS1 Specific UI attributes and methods
 * 
 * @author JimP
 * 
 */
public abstract class WS1Client extends WSClient
        implements WSMenuEntryProviderI
{

  /**
   * original service handle that this client was derived from
   */
  ServiceHandle serviceHandle = null;

  /**
   * default constructor
   */
  public WS1Client()
  {
    super();
  }

  /**
   * initialise WSClient service information attributes from the service handle
   * 
   * @param sh
   * @return the service instance information GUI for this client and job.
   */
  protected WebserviceInfo setWebService(ServiceHandle sh)
  {
    return setWebService(sh, false);
  }

  /**
   * initialise WSClient service information attributes from the service handle
   * 
   * @param sh
   * @param headless
   *          true implies no GUI objects will be created.
   * @return the service instance information GUI for this client and job.
   */
  protected WebserviceInfo setWebService(ServiceHandle sh, boolean headless)
  {
    WebServiceName = sh.getName();
    if (ServiceActions.containsKey(sh.getAbstractName()))
    {
      WebServiceJobTitle = sh.getName(); // TODO: control sh.Name specification
      // properly
      // add this for short names. +(String)
      // ServiceActions.get(sh.getAbstractName());
    }
    else
    {
      WebServiceJobTitle = sh.getAbstractName() + " using " + sh.getName();

    }
    WebServiceReference = sh.getDescription();
    WsURL = sh.getEndpointURL();
    WebserviceInfo wsInfo = null;
    if (!headless)
    {
      wsInfo = new WebserviceInfo(WebServiceJobTitle, WebServiceReference,
              true);
    }
    return wsInfo;
  }

  /**
   * convenience method to pass the serviceHandle reference that instantiated
   * this service on to the menu entry constructor
   * 
   * @param wsmenu
   *          the menu to which any menu entries/sub menus are to be attached
   * @param alignFrame
   *          the alignFrame instance that provides input data for the service
   */
  public void attachWSMenuEntry(JMenu wsmenu, final AlignFrame alignFrame)
  {
    if (serviceHandle == null)
    {
      throw new Error(MessageManager.getString(
              "error.implementation_error_cannot_attach_ws_menu_entry"));
    }
    attachWSMenuEntry(wsmenu, serviceHandle, alignFrame);
  }

  /**
   * method implemented by each concrete WS1Client implementation that creates
   * menu entries that enact their service using data from alignFrame.
   * 
   * @param wsmenu
   *          where new menu entries (and submenus) are to be attached
   * @param serviceHandle
   *          the serviceHandle document for the service that entries are
   *          created for
   * @param alignFrame
   */
  public abstract void attachWSMenuEntry(JMenu wsmenu,
          final ServiceHandle serviceHandle, final AlignFrame alignFrame);

}
