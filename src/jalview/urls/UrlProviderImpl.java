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

import jalview.urls.api.UrlProviderI;
import jalview.util.UrlLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Leaf node of UrlProvider composite
 * 
 * @author $author$
 * @version $Revision$
 */

public abstract class UrlProviderImpl implements UrlProviderI
{
  // minimum length of substitution in url link string
  protected static final int MIN_SUBST_LENGTH = 4;

  private static final Pattern MIRIAM_PATTERN = Pattern
          .compile("^MIR:\\d{8}$");

  protected String primaryUrl;

  protected String getPrimaryUrl(String seqid,
          HashMap<String, UrlLink> urls)
  {
    if (seqid.length() < MIN_SUBST_LENGTH)
    {
      return null;
    }
    else if (primaryUrl == null)
    {
      return null;
    }
    else if (!urls.containsKey(primaryUrl))
    {
      return null;
    }
    else
    {
      String url = null;
      UrlLink urlLink = urls.get(primaryUrl);
      String[] primaryUrls = urlLink.makeUrls(seqid, true);
      if (primaryUrls == null || primaryUrls[0] == null
              || primaryUrls[0].length() < MIN_SUBST_LENGTH)
      {
        url = null;
      }
      else
      {
        // just take first URL made from regex
        url = primaryUrls[1];
      }
      return url;
    }
  }

  @Override
  public List<UrlLinkDisplay> getLinksForTable()
  {
    return null;
  }

  protected ArrayList<UrlLinkDisplay> getLinksForTable(
          HashMap<String, UrlLink> urls, ArrayList<String> selectedUrls,
          boolean selected)
  {
    ArrayList<UrlLinkDisplay> displayLinks = new ArrayList<UrlLinkDisplay>();
    for (Entry<String, UrlLink> entry : urls.entrySet())
    {
      String key = entry.getKey();
      boolean isPrimary = (key.equals(primaryUrl));
      boolean isSelected;
      if (selectedUrls != null)
      {
        isSelected = selectedUrls.contains(key);
      }
      else
      {
        isSelected = selected;
      }
      displayLinks.add(new UrlLinkDisplay(key, entry.getValue(), isSelected,
              isPrimary));
    }
    return displayLinks;
  }

  protected boolean isMiriamId(String id)
  {
    return MIRIAM_PATTERN.matcher(id).matches();
  }

  @Override
  public boolean isUserEntry(String id)
  {
    return !isMiriamId(id);
  }
}
