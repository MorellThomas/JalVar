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

import jalview.gui.WebserviceInfo;

public abstract class WSClient // implements WSMenuEntryProviderI
{
  /**
   * WSClient holds the basic attributes that are displayed to the user for all
   * jalview web service clients
   */
  /**
   * displayed name for this web service
   */
  protected String WebServiceName;

  /**
   * specific job title (e.g. 'ClustalW Alignment of Selection from Aligment
   * from Cut and Paste input')
   */
  protected String WebServiceJobTitle;

  /**
   * String giving additional information such as method citations for this
   * service
   */
  protected String WebServiceReference;

  /**
   * Service endpoint
   */
  protected String WsURL;

  /**
   * Web service information used to initialise the WSClient attributes
   */
  protected WebserviceInfo wsInfo;

  /**
   * total number of jobs managed by this web service client instance.
   */
  int jobsRunning = 0;

  /**
   * TODO: this is really service metadata, and should be moved elsewhere.
   * mappings between abstract interface names and menu entries
   */
  protected java.util.Hashtable ServiceActions;
  {
    ServiceActions = new java.util.Hashtable();
    ServiceActions.put("MsaWS", "Multiple Sequence Alignment");
    ServiceActions.put("SecStrPred", "Secondary Structure Prediction");
  };

  public WSClient()
  {
  }
}
