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

import static jalview.util.UrlConstants.DB_ACCESSION;
import static jalview.util.UrlConstants.DELIM;
import static jalview.util.UrlConstants.SEP;
import static jalview.util.UrlConstants.SEQUENCE_ID;

import jalview.util.MessageManager;
import jalview.util.UrlConstants;
import jalview.util.UrlLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * 
 * Implements the UrlProviderI interface for a UrlProvider object which serves
 * custom URLs defined by the user
 * 
 * @author $author$
 * @version $Revision$
 */
public class CustomUrlProvider extends UrlProviderImpl
{
  // Default sequence URL link label for SRS
  private static final String SRS_LABEL = "SRS";

  // map of string ids to urlLinks (selected)
  private HashMap<String, UrlLink> selectedUrls;

  // map of string ids to urlLinks (not selected)
  private HashMap<String, UrlLink> nonselectedUrls;

  /**
   * Construct UrlProvider for custom (user-entered) URLs
   * 
   * @param inMenuUrlList
   *          list of URLs set to be displayed in menu, in form stored in Cache.
   *          i.e. SEP delimited string
   * @param storedUrlList
   *          list of custom URLs entered by user but not currently displayed in
   *          menu, in form stored in Cache
   */
  public CustomUrlProvider(String inMenuUrlList, String storedUrlList)
  {
    try
    {
      selectedUrls = parseUrlStrings(inMenuUrlList);
      nonselectedUrls = parseUrlStrings(storedUrlList);
    } catch (Exception ex)
    {
      System.out
              .println(ex.getMessage() + "\nError parsing sequence links");
    }
  }

  /**
   * Construct UrlProvider for custom (user-entered) URLs
   * 
   * @param urlList
   *          list of URLs to be displayed in menu, as (label,url) pairs
   * @param storedUrlList
   *          list of custom URLs entered by user but not currently displayed in
   *          menu, as (label,url) pairs
   */
  public CustomUrlProvider(Map<String, String> inMenuUrlList,
          Map<String, String> storedUrlList)
  {
    try
    {
      selectedUrls = parseUrlList(inMenuUrlList);
      nonselectedUrls = parseUrlList(storedUrlList);
    } catch (Exception ex)
    {
      System.out
              .println(ex.getMessage() + "\nError parsing sequence links");
    }
  }

  private HashMap<String, UrlLink> parseUrlStrings(String urlStrings)
  {
    // cachedUrlList is in form <label>|<url>|<label>|<url>...
    // parse cachedUrlList into labels (used as id) and url links
    HashMap<String, UrlLink> urls = new HashMap<>();

    StringTokenizer st = new StringTokenizer(urlStrings, SEP);
    while (st.hasMoreElements())
    {
      String name = st.nextToken().trim();

      if (!isMiriamId(name))
      {
        // this one of our custom urls
        String url = st.nextToken();
        // check for '|' within a regex
        int rxstart = url.indexOf(DELIM + DB_ACCESSION + DELIM);
        if (rxstart == -1)
        {
          rxstart = url.indexOf(DELIM + SEQUENCE_ID + DELIM);
        }
        while (rxstart == -1 && url.indexOf("/=" + DELIM) == -1
                && st.hasMoreTokens())
        {
          url = url + SEP + st.nextToken();
        }
        url = url.trim();
        urls.put(name, new UrlLink(name, url, name));
      }
    }
    upgradeOldLinks(urls);
    return urls;
  }

  private HashMap<String, UrlLink> parseUrlList(Map<String, String> urlList)
  {
    HashMap<String, UrlLink> urls = new HashMap<>();
    if (urlList == null)
    {
      return urls;
    }

    Iterator<Map.Entry<String, String>> it = urlList.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry<String, String> pair = it.next();
      urls.put(pair.getKey(),
              new UrlLink(pair.getKey(), pair.getValue(), pair.getKey()));
    }
    upgradeOldLinks(urls);
    return urls;
  }

  /*
   * Upgrade any legacy links which may have been left lying around
   */
  private void upgradeOldLinks(HashMap<String, UrlLink> urls)
  {
    boolean upgrade = false;
    // upgrade old SRS link
    if (urls.containsKey(SRS_LABEL))
    {
      urls.remove(SRS_LABEL);
      upgrade = true;
    }
    // upgrade old EBI link - easier just to remove and re-add than faffing
    // around checking exact url
    if (urls.containsKey(UrlConstants.DEFAULT_LABEL))
    {
      // note because this is called separately for selected and nonselected
      // urls, the default url will not always be present
      urls.remove(UrlConstants.DEFAULT_LABEL);
      upgrade = true;
    }
    if (upgrade)
    {
      UrlLink link = new UrlLink(UrlConstants.DEFAULT_STRING);
      link.setLabel(UrlConstants.DEFAULT_LABEL);
      urls.put(UrlConstants.DEFAULT_LABEL, link);
    }
  }

  @Override
  public List<String> getLinksForMenu()
  {
    List<String> links = new ArrayList<>();
    Iterator<Map.Entry<String, UrlLink>> it = selectedUrls.entrySet()
            .iterator();
    while (it.hasNext())
    {
      Map.Entry<String, UrlLink> pair = it.next();
      links.add(pair.getValue().toString());
    }
    return links;
  }

  @Override
  public List<UrlLinkDisplay> getLinksForTable()
  {
    ArrayList<UrlLinkDisplay> displayLinks = new ArrayList<>();
    displayLinks = getLinksForTable(selectedUrls, true);
    displayLinks.addAll(getLinksForTable(nonselectedUrls, false));
    return displayLinks;
  }

  private ArrayList<UrlLinkDisplay> getLinksForTable(
          HashMap<String, UrlLink> urlList, boolean selected)
  {
    return super.getLinksForTable(urlList, null, selected);
  }

  @Override
  public boolean setPrimaryUrl(String id)
  {
    if (selectedUrls.containsKey(id))
    {
      primaryUrl = id;
    }
    else if (nonselectedUrls.containsKey(id))
    {
      primaryUrl = id;
    }
    else
    {
      primaryUrl = null;
    }

    return (primaryUrl != null);
  }

  @Override
  public String writeUrlsAsString(boolean selected)
  {
    StringBuffer links = new StringBuffer();
    HashMap<String, UrlLink> urls;
    if (selected)
    {
      urls = selectedUrls;
    }
    else
    {
      urls = nonselectedUrls;
    }
    if (urls.size() > 0)
    {
      for (Entry<String, UrlLink> entry : urls.entrySet())
      {
        links.append(entry.getValue().toString());
        links.append(SEP);
      }

      // remove last SEP
      links.setLength(links.length() - 1);
    }
    else
    {
      urls.clear();
    }
    return links.toString();
  }

  @Override
  public String getPrimaryUrl(String seqid)
  {
    String result = super.getPrimaryUrl(seqid, selectedUrls);
    if (result == null)
    {
      result = super.getPrimaryUrl(seqid, nonselectedUrls);
    }
    return result;
  }

  @Override
  public String getPrimaryUrlId()
  {
    return primaryUrl;
  }

  @Override
  public String getPrimaryTarget(String seqid)
  {
    return selectedUrls.get(primaryUrl).getTarget();
  }

  @Override
  public void setUrlData(List<UrlLinkDisplay> links)
  {
    HashMap<String, UrlLink> unselurls = new HashMap<>();
    HashMap<String, UrlLink> selurls = new HashMap<>();

    Iterator<UrlLinkDisplay> it = links.iterator();
    while (it.hasNext())
    {
      UrlLinkDisplay link = it.next();

      // MIRIAM ids will be handled by a different UrlProvider class
      if (!isMiriamId(link.getId()))
      {
        // don't allow duplicate key names as entries will be overwritten
        if (unselurls.containsKey(link.getId())
                || selurls.containsKey(link.getId()))
        {
          throw new IllegalArgumentException(MessageManager.formatMessage(
                  "exception.url_cannot_have_duplicate_id", link.getId()));
        }
        if (link.getIsSelected())
        {
          selurls.put(link.getId(), new UrlLink(link.getDescription(),
                  link.getUrl(), link.getDescription()));
        }
        else
        {
          unselurls.put(link.getId(), new UrlLink(link.getDescription(),
                  link.getUrl(), link.getDescription()));
        }
        // sort out primary and selected ids
        if (link.getIsPrimary())
        {
          setPrimaryUrl(link.getId());
        }
      }

    }
    nonselectedUrls = unselurls;
    selectedUrls = selurls;
  }

  @Override
  public String choosePrimaryUrl()
  {
    // unilaterally set the primary id to the EMBL_EBI link
    if ((!nonselectedUrls.containsKey(UrlConstants.DEFAULT_LABEL))
            && (!selectedUrls.containsKey(UrlConstants.DEFAULT_LABEL)))
    {
      UrlLink link = new UrlLink(UrlConstants.DEFAULT_STRING);
      link.setLabel(UrlConstants.DEFAULT_LABEL);
      selectedUrls.put(UrlConstants.DEFAULT_LABEL, link);
    }
    primaryUrl = UrlConstants.DEFAULT_LABEL;
    return UrlConstants.DEFAULT_LABEL;
  }

  @Override
  public boolean contains(String id)
  {
    return (selectedUrls.containsKey(id)
            || nonselectedUrls.containsKey(id));
  }

}
