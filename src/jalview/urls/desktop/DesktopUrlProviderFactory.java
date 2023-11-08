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
package jalview.urls.desktop;

import jalview.urls.CustomUrlProvider;
import jalview.urls.IdentifiersUrlProvider;
import jalview.urls.UrlProvider;
import jalview.urls.api.UrlProviderFactoryI;
import jalview.urls.api.UrlProviderI;

import java.util.ArrayList;
import java.util.List;

/**
 * UrlProvider factory for desktop code
 * 
 * @author $author$
 * @version $Revision$
 */

public class DesktopUrlProviderFactory implements UrlProviderFactoryI
{

  private String provDefaultUrl;

  private String menuUrlList;

  private String nonMenuUrlList;

  public DesktopUrlProviderFactory(String defaultUrlString,
          String cachedUrlList, String userUrlList)
  {
    provDefaultUrl = defaultUrlString;
    menuUrlList = cachedUrlList;
    nonMenuUrlList = userUrlList;
  }

  @Override
  public UrlProviderI createUrlProvider()
  {
    // create all the UrlProviders we need
    List<UrlProviderI> providers = new ArrayList<UrlProviderI>();

    UrlProviderI idProvider = new IdentifiersUrlProvider(menuUrlList);
    UrlProviderI customProvider = new CustomUrlProvider(menuUrlList,
            nonMenuUrlList);
    providers.add(idProvider);
    providers.add(customProvider);

    return new UrlProvider(provDefaultUrl, providers);
  }

}
