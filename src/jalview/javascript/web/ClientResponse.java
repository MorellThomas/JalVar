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
package jalview.javascript.web;

import jalview.javascript.json.JSON;
import jalview.util.Platform;

import java.net.URL;

/**
 * minimal implementation of com.sun.jersey.api.client.ClientResponse
 * 
 * @author hansonr
 *
 */
public class ClientResponse
{

  private String response;

  private boolean isJSON;

  private Object jsonData;

  int responseCode = -1;

  public ClientResponse(URL url, String[] encoding)
  {
    // OK, so it turns out that ajax "json" format - or for that matter, any
    // format for json is still just text. There is no point in getting this
    // using special jQuery "json" formats. Duh. BH wasted a whole day try to
    // "do it right".
    response = Platform.getFileAsString(url.toString());
    responseCode = (response == null || response == "" ? 404 : 200);
    isJSON = encoding[0].equals("application/json");
    if (isJSON)
    {
      try
      {
        jsonData = JSON.parse(response);
      } catch (Exception e)
      {
        jsonData = null;
      }
      if (jsonData == null)
      {
        responseCode = 400;
      }
    }
  }

  public Object getEntity(Class<?> c)
  {

    if (c == java.util.Map.class)
    {
      return jsonData;
    }
    return response;
  }

  // https://www.ebi.ac.uk/pdbe/search/pdb/select?wt=json&fl=pdb_id,title,experimental_method,resolution&rows=500&start=0&q=(text:q93xj9_soltu)+AND+molecule_sequence:%5B%27%27+TO+*%5D+AND+status:REL&sort=overall_quality+desc

  // {
  // "responseHeader":{
  // "status":0,
  // "QTime":0,
  // "params":{
  // "q":"(text:q93xj9_soltu) AND molecule_sequence:['' TO *] AND status:REL",
  // "fl":"pdb_id,title,experimental_method,resolution",
  // "start":"0",
  // "sort":"overall_quality desc",
  // "rows":"500",
  // "wt":"json"}},
  // "response":{"numFound":1,"start":0,"docs":[
  // {
  // "experimental_method":["X-ray diffraction"],
  // "pdb_id":"4zhp",
  // "resolution":2.46,
  // "title":"The crystal structure of Potato ferredoxin I with 2Fe-2S
  // cluster"}]
  // }}
  //

  public int getStatus()
  {
    return responseCode;
  }

  public Object getJSONData()
  {
    return jsonData;
  }

}
