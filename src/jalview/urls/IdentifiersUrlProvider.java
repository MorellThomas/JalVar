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

import jalview.util.JSONUtils;
import jalview.util.UrlLink;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.simple.parser.ParseException;

/**
 * 
 * Implements the UrlProviderI interface for a UrlProvider object which serves
 * URLs from identifiers.org
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdentifiersUrlProvider extends UrlProviderImpl
{

  private static final String LOCAL_KEY = "Local";

  private static final String ID_ORG_KEY = "identifiers.org";

  // map of string ids to urls
  private HashMap<String, UrlLink> urls;

  // list of selected urls
  private ArrayList<String> selectedUrls;

  public IdentifiersUrlProvider(String cachedUrlList)
  {
    urls = readIdentifiers(IdOrgSettings.getDownloadLocation());
    selectedUrls = new ArrayList<>();
    checkSelectionMatchesUrls(cachedUrlList);
  }

  /**
   * Read data from an identifiers.org download file
   * 
   * @param idFileName
   *          name of identifiers.org download file
   * @return hashmap of identifiers.org data, keyed by MIRIAM id
   */
  @SuppressWarnings("unchecked")
  private HashMap<String, UrlLink> readIdentifiers(String idFileName)
  {
    // identifiers.org data
    HashMap<String, UrlLink> idData = new HashMap<>();

    String errorMessage = null;
    try
    {
      // NOTE: THIS WILL FAIL IN SWINGJS BECAUSE IT INVOLVES A FILE READER

      FileReader reader = new FileReader(idFileName);
      String key = "";
      Map<String, Object> obj = (Map<String, Object>) JSONUtils
              .parse(reader);
      if (obj.containsKey(ID_ORG_KEY))
      {
        key = ID_ORG_KEY;
      }
      else if (obj.containsKey(LOCAL_KEY))
      {
        key = LOCAL_KEY;
      }
      else
      {
        System.out.println(
                "Unexpected key returned from identifiers jalview service");
        return idData;
      }

      List<Object> jsonarray = (List<Object>) obj.get(key);

      // loop over each entry in JSON array and build HashMap entry
      for (int i = 0; i < jsonarray.size(); i++)
      {
        Map<String, Object> item = (Map<String, Object>) jsonarray.get(i);

        String url = (String) item.get("url") + "/" + DELIM + DB_ACCESSION
                + DELIM;
        UrlLink link = new UrlLink((String) item.get("name"), url,
                (String) item.get("prefix"));
        idData.put((String) item.get("id"), link);
      }
    } catch (IOException | ParseException e)
    {
      // unnecessary e.printStackTrace();
      // Note how in JavaScript we can grab the first bytes from any file
      // reader.
      // Typical report here is "NetworkError" because the file does not exist.
      // "https://." is coming from System.getProperty("user.home"), but this
      // could
      // be set by the page developer to anything, of course.
      errorMessage = e.toString();
      idData.clear();
    }
    // BH 2018 -- added more valuable report
    if (errorMessage != null)
    {
      System.err.println("IdentifiersUrlProvider: cannot read " + idFileName
              + ": " + errorMessage);
    }
    return idData;
  }

  private void checkSelectionMatchesUrls(String cachedUrlList)
  {
    StringTokenizer st = new StringTokenizer(cachedUrlList, SEP);
    while (st.hasMoreElements())
    {
      String id = st.nextToken();

      if (isMiriamId(id))
      {
        // this is an identifiers.org MIRIAM id
        if (urls.containsKey(id))
        {
          selectedUrls.add(id);
        }
      }
    }

    // reset defaultUrl in case it is no longer selected
    setPrimaryUrl(primaryUrl);
  }

  @Override
  public boolean setPrimaryUrl(String id)
  {
    if (urls.containsKey(id))
    {
      primaryUrl = id;
    }
    else
    {
      primaryUrl = null;
    }

    return urls.containsKey(id);
  }

  @Override
  public String writeUrlsAsString(boolean selected)
  {
    if (!selected)
    {
      return ""; // we don't cache unselected identifiers.org urls
    }

    StringBuffer links = new StringBuffer();
    if (!selectedUrls.isEmpty())
    {
      for (String k : selectedUrls)
      {
        links.append(k);
        links.append(SEP);
      }
      // remove last SEP
      links.setLength(links.length() - 1);
    }
    return links.toString();
  }

  @Override
  public List<String> getLinksForMenu()
  {
    List<String> links = new ArrayList<>();
    for (String key : selectedUrls)
    {
      links.add(urls.get(key).toStringWithTarget());
    }
    return links;
  }

  @Override
  public List<UrlLinkDisplay> getLinksForTable()
  {
    return super.getLinksForTable(urls, selectedUrls, false);
  }

  @Override
  public void setUrlData(List<UrlLinkDisplay> links)
  {
    selectedUrls = new ArrayList<>();

    Iterator<UrlLinkDisplay> it = links.iterator();
    while (it.hasNext())
    {
      UrlLinkDisplay link = it.next();

      // Handle links with MIRIAM ids only
      if (isMiriamId(link.getId()))
      {
        // select/deselect links accordingly and set default url
        if (urls.containsKey(link.getId()))
        {
          if (link.getIsSelected())
          {
            selectedUrls.add(link.getId());
          }
          if (link.getIsPrimary())
          {
            setPrimaryUrl(link.getId());
          }
        }
      }
    }
  }

  @Override
  public String getPrimaryUrl(String seqid)
  {
    return super.getPrimaryUrl(seqid, urls);
  }

  @Override
  public String getPrimaryUrlId()
  {
    return primaryUrl;
  }

  @Override
  public String getPrimaryTarget(String seqid)
  {
    return null;
  }

  @Override
  public String choosePrimaryUrl()
  {
    return null;
  }

  @Override
  public boolean contains(String id)
  {
    return (urls.containsKey(id));
  }
}
