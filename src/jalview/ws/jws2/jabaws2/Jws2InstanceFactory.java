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
package jalview.ws.jws2.jabaws2;

import jalview.ws.jws2.AAConClient;
import jalview.ws.jws2.RNAalifoldClient;
import jalview.ws.uimodel.AlignAnalysisUIText;

import java.util.HashMap;
import java.util.HashSet;

import compbio.data.msa.JABAService;

public class Jws2InstanceFactory
{
  private static HashMap<String, AlignAnalysisUIText> aaConGUI;

  private static HashSet<String> ignoreGUI;

  private static String category_rewrite(String cat_name)
  {
    return (cat_name != null && cat_name.equals("Prediction"))
            ? "Secondary Structure Prediction"
            : cat_name;
  }

  private static void init()
  {
    if (aaConGUI == null)
    {
      aaConGUI = new HashMap<String, AlignAnalysisUIText>();
      aaConGUI.put(compbio.ws.client.Services.AAConWS.toString(),
              AAConClient.getAlignAnalysisUITest());
      aaConGUI.put(compbio.ws.client.Services.RNAalifoldWS.toString(),
              RNAalifoldClient.getAlignAnalysisUITest());
      // ignore list for JABAWS services not supported in jalview ...
      ignoreGUI = new HashSet<String>();
    }
  }

  /**
   * exclusion list to avoid creating GUI elements for services we don't fully
   * support
   * 
   * @param serviceType
   * @return
   */
  public static boolean ignoreService(String serviceType)
  {
    init();
    return (ignoreGUI.contains(serviceType.toString()));
  }

  /**
   * construct a service instance and configure it with any additional
   * properties needed so Jalview can access it correctly
   * 
   * @param jwsservers
   * @param serviceType
   * @param name
   * @param description
   * @param service
   * @return
   */
  public static Jws2Instance newJws2Instance(String jwsservers,
          String serviceType, String name, String description,
          JABAService service)
  {
    init();
    Jws2Instance svc = new Jws2Instance(jwsservers, serviceType,
            category_rewrite(name), description, service);
    svc.aaui = aaConGUI.get(serviceType.toString());
    return svc;
  }

}
