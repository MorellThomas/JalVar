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
package jalview.urls;

import static jalview.util.UrlConstants.SEP;

import jalview.urls.api.UrlProviderI;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 
 * Implements the UrlProviderI interface for a composite UrlProvider object
 * 
 * @author $author$
 * @version $Revision$
 */
public class UrlProvider implements UrlProviderI
{
  // List of actual URL link providers
  private List<UrlProviderI> providers;

  // Specific reference to custom URL link provider
  private UrlProviderI customProvider;

  /**
   * Constructor for UrlProvider composite
   * 
   * @param defaultUrlString
   *          id of default url
   * @param allProviders
   *          list of UrlProviders this provider gives access to
   */
  public UrlProvider(String defaultUrlString,
          List<UrlProviderI> allProviders)
  {
    providers = allProviders;

    customProvider = findCustomProvider();

    // check that the defaultUrl still exists
    if (!contains(defaultUrlString))
    {
      // if the defaultUrl can't be found in any of the providers
      // set up a custom default url
      choosePrimaryUrl();
    }
    else
    {
      setPrimaryUrl(defaultUrlString);
    }
  }

  /*
   * Store ref to custom url provider
   */
  private UrlProviderI findCustomProvider()
  {
    for (UrlProviderI p : providers)
    {
      if (p instanceof CustomUrlProvider)
      {
        return p;
      }
    }

    System.out.println(
            "Error initialising UrlProvider - no custom url provider");
    return null;
  }

  @Override
  public boolean setPrimaryUrl(String id)
  {
    boolean outcome = false;
    for (UrlProviderI p : providers)
    {
      if (p.setPrimaryUrl(id))
      {
        outcome = true;
      }
    }
    if (!outcome)
    {
      throw new IllegalArgumentException();
    }
    return outcome;
  }

  @Override
  public boolean contains(String id)
  {
    boolean outcome = false;
    for (UrlProviderI p : providers)
    {
      if (p.contains(id))
      {
        outcome = true;
      }
    }
    return outcome;
  }

  @Override
  public String writeUrlsAsString(boolean selected)
  {
    String result = "";
    for (UrlProviderI p : providers)
    {
      String next = p.writeUrlsAsString(selected);
      if (!next.isEmpty())
      {
        result += next;
        result += SEP;
      }
    }
    // remove last sep
    if (!result.isEmpty())
    {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }

  @Override
  public Vector<String> getLinksForMenu()
  {
    Vector<String> fullLinks = new Vector<String>();
    for (UrlProviderI p : providers)
    {
      List<String> links = p.getLinksForMenu();
      if (links != null)
      {
        // will obliterate links with same keys from different providers
        // must have checks in place to prevent user from duplicating ids
        fullLinks.addAll(links);
      }
    }
    return fullLinks;
  }

  @Override
  public List<UrlLinkDisplay> getLinksForTable()
  {
    ArrayList<UrlLinkDisplay> displayLinks = new ArrayList<UrlLinkDisplay>();
    for (UrlProviderI p : providers)
    {
      displayLinks.addAll(p.getLinksForTable());
    }
    return displayLinks;
  }

  @Override
  public void setUrlData(List<UrlLinkDisplay> links)
  {
    for (UrlProviderI p : providers)
    {
      p.setUrlData(links);
    }
  }

  @Override
  public String getPrimaryUrl(String seqid)
  {
    String link = null;
    for (UrlProviderI p : providers)
    {
      if (p.getPrimaryUrl(seqid) == null)
      {
        continue;
      }
      else
      {
        link = p.getPrimaryUrl(seqid);
        break;
      }
    }
    return link;
  }

  @Override
  public String getPrimaryUrlId()
  {
    String id = null;
    for (UrlProviderI p : providers)
    {
      if (p.getPrimaryUrlId() == null)
      {
        continue;
      }
      else
      {
        id = p.getPrimaryUrlId();
        break;
      }
    }
    return id;
  }

  @Override
  public String getPrimaryTarget(String seqid)
  {
    String target = null;
    for (UrlProviderI p : providers)
    {
      if (p.getPrimaryTarget(seqid) == null)
      {
        continue;
      }
      else
      {
        target = p.getPrimaryTarget(seqid);
        break;
      }
    }
    return target;
  }

  @Override
  public String choosePrimaryUrl()
  {
    // choose a custom url default
    return customProvider.choosePrimaryUrl();
  }

  @Override
  public boolean isUserEntry(String id)
  {
    return customProvider.isUserEntry(id);
  }
}
