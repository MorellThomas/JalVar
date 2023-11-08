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

import java.util.LinkedHashSet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AppCacheTest
{
  private AppCache appCache;

  private static final String TEST_CACHE_KEY = "CACHE.UNIT_TEST";

  private static final String TEST_FAKE_CACHE_KEY = "CACHE.UNIT_TEST_FAKE";

  @BeforeClass(alwaysRun = true)
  public void setUpCache()
  {
    appCache = AppCache.getInstance();
  }

  public void generateTestCacheItems()
  {
    LinkedHashSet<String> testCacheItems = new LinkedHashSet<String>();
    for (int x = 0; x < 10; x++)
    {
      testCacheItems.add("TestCache" + x);
    }
    appCache.putCache(TEST_CACHE_KEY, testCacheItems);
    appCache.persistCache(TEST_CACHE_KEY);
  }

  @Test(groups = { "Functional" })
  public void appCacheTest()
  {
    LinkedHashSet<String> cacheItems = appCache
            .getAllCachedItemsFor(TEST_FAKE_CACHE_KEY);
    Assert.assertEquals(cacheItems.size(), 0);
    generateTestCacheItems();
    cacheItems = appCache.getAllCachedItemsFor(TEST_CACHE_KEY);
    Assert.assertEquals(cacheItems.size(), 10);
    appCache.deleteCacheItems(TEST_CACHE_KEY);
    cacheItems = appCache.getAllCachedItemsFor(TEST_CACHE_KEY);
    Assert.assertEquals(cacheItems.size(), 0);
  }

  @Test(groups = { "Functional" })
  public void appCacheLimitTest()
  {
    String limit = appCache.getCacheLimit(TEST_CACHE_KEY);
    Assert.assertEquals(limit, "99");
    limit = String.valueOf(appCache.updateCacheLimit(TEST_CACHE_KEY, 20));
    Assert.assertEquals(limit, "20");
    limit = appCache.getCacheLimit(TEST_CACHE_KEY);
    Assert.assertEquals(limit, "20");
    appCache.updateCacheLimit(TEST_CACHE_KEY, 99);
  }

}
