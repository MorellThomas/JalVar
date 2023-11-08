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
package jalview.ws.sifts;

import java.util.Objects;

public class SiftsSettings
{
  private static boolean mapWithSifts = false;

  private static String siftDownloadDirectory;

  private static int cacheThresholdInDays;

  private static int failSafePIDThreshold;

  public static boolean isMapWithSifts()
  {
    return mapWithSifts;
  }

  public static void setMapWithSifts(boolean mapWithSifts)
  {
    SiftsSettings.mapWithSifts = mapWithSifts;
  }

  public static String getSiftDownloadDirectory()
  {
    return siftDownloadDirectory;
  }

  public static void setSiftDownloadDirectory(String siftDownloadDirectory)
  {
    SiftsSettings.siftDownloadDirectory = siftDownloadDirectory;
  }

  public static int getCacheThresholdInDays()
  {
    return cacheThresholdInDays;
  }

  public static void setCacheThresholdInDays(String cacheThresholdInDays)
  {
    Objects.requireNonNull(cacheThresholdInDays);
    SiftsSettings.cacheThresholdInDays = Integer
            .valueOf(cacheThresholdInDays);
  }

  public static int getFailSafePIDThreshold()
  {
    return failSafePIDThreshold;
  }

  public static void setFailSafePIDThreshold(String failSafePIDThreshold)
  {
    Objects.requireNonNull(failSafePIDThreshold);
    SiftsSettings.failSafePIDThreshold = Integer
            .valueOf(failSafePIDThreshold);
  }
}
