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
package jalview.urls.applet;

import jalview.urls.CustomUrlProvider;
import jalview.urls.UrlProvider;
import jalview.urls.api.UrlProviderFactoryI;
import jalview.urls.api.UrlProviderI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UrlProvider factory for applet code
 * 
 * @author $author$
 * @version $Revision$
 */

public class AppletUrlProviderFactory implements UrlProviderFactoryI
{
  private String provDefaultUrl;

  private Map<String, String> provUrlList;

  public AppletUrlProviderFactory(String defaultUrlString,
          Map<String, String> urlList)
  {
    provDefaultUrl = defaultUrlString;
    provUrlList = urlList;
  }

  @Override
  public UrlProviderI createUrlProvider()
  {
    // create all the UrlProviders we need
    List<UrlProviderI> providers = new ArrayList<UrlProviderI>();
    UrlProviderI customProvider = new CustomUrlProvider(provUrlList, null);
    providers.add(customProvider);

    UrlProviderI prov = new UrlProvider(provDefaultUrl, providers);
    return prov;
  }

}
