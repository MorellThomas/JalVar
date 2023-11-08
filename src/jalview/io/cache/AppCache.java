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
package jalview.io.cache;

import jalview.bin.Cache;

import java.util.Hashtable;
import java.util.LinkedHashSet;

/**
 * A singleton class used for querying and persisting cache items.
 * 
 * @author tcnofoegbu
 *
 */
public class AppCache
{
  public static final String DEFAULT_LIMIT = "99";

  public static final String CACHE_DELIMITER = ";";

  private static AppCache instance = null;

  private static final String DEFAULT_LIMIT_KEY = ".DEFAULT_LIMIT";

  private Hashtable<String, LinkedHashSet<String>> cacheItems;

  private AppCache()
  {
    cacheItems = new Hashtable<String, LinkedHashSet<String>>();
  }

  /**
   * Method to obtain all the cache items for a given cache key
   * 
   * @param cacheKey
   * @return
   */
  public LinkedHashSet<String> getAllCachedItemsFor(String cacheKey)
  {
    LinkedHashSet<String> foundCache = cacheItems.get(cacheKey);
    if (foundCache == null)
    {
      foundCache = new LinkedHashSet<String>();
      cacheItems.put(cacheKey, foundCache);
    }
    return foundCache;
  }

  /**
   * Returns a singleton instance of AppCache
   * 
   * @return
   */
  public static AppCache getInstance()
  {
    if (instance == null)
    {
      instance = new AppCache();
    }
    return instance;
  }

  /**
   * Method for persisting cache items for a given cache key
   * 
   * @param cacheKey
   */
  public void persistCache(String cacheKey)
  {
    LinkedHashSet<String> foundCacheItems = getAllCachedItemsFor(cacheKey);
    StringBuffer delimitedCacheBuf = new StringBuffer();
    for (String cacheItem : foundCacheItems)
    {
      delimitedCacheBuf.append(CACHE_DELIMITER).append(cacheItem);
    }
    if (delimitedCacheBuf.length() > 0)
    {
      delimitedCacheBuf.deleteCharAt(0);
    }
    String delimitedCacheString = delimitedCacheBuf.toString();

    Cache.setProperty(cacheKey, delimitedCacheString);
  }

  /**
   * Method for deleting cached items for a given cache key
   * 
   * @param cacheKey
   *          the cache key
   */
  public void deleteCacheItems(String cacheKey)
  {
    cacheItems.put(cacheKey, new LinkedHashSet<String>());
    persistCache(cacheKey);
  }

  /**
   * Method for obtaining the preset maximum cache limit for a given cache key
   * 
   * @param cacheKey
   *          the cache key
   * @return the max number of items that could be cached
   */
  public String getCacheLimit(String cacheKey)
  {
    String uniqueKey = cacheKey + DEFAULT_LIMIT_KEY;
    return Cache.getDefault(uniqueKey, DEFAULT_LIMIT);
  }

  /**
   * Method for updating the preset maximum cache limit for a given cache key
   * 
   * @param cacheKey
   *          the cache key
   * @param newLimit
   *          the max number of items that could be cached for the given cache
   *          key
   * @return
   */
  public int updateCacheLimit(String cacheKey, int newUserLimit)
  {
    String newLimit = String.valueOf(newUserLimit);
    String uniqueKey = cacheKey + DEFAULT_LIMIT_KEY;
    String formerLimit = getCacheLimit(cacheKey);
    if (newLimit != null && !newLimit.isEmpty()
            && !formerLimit.equals(newLimit))
    {
      Cache.setProperty(uniqueKey, newLimit);
      formerLimit = newLimit;
    }
    return Integer.valueOf(formerLimit);
  }

  /**
   * Method for inserting cache items for given cache key into the cache data
   * structure
   * 
   * @param cacheKey
   *          the cache key
   * @param cacheItems
   *          the items to add to the cache
   */
  public void putCache(String cacheKey, LinkedHashSet<String> newCacheItems)
  {
    cacheItems.put(cacheKey, newCacheItems);
  }

}
