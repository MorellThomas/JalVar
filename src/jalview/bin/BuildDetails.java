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
package jalview.bin;

import jalview.api.BuildDetailsI;

public class BuildDetails implements BuildDetailsI
{
  private static String buildDate;

  private static String version;

  private static String installation;

  public BuildDetails()
  {

  }

  /**
   * update singleton Strings with these build details
   * 
   * @param version
   * @param buildDate
   * @param installation
   */
  public BuildDetails(String version, String buildDate, String installation)
  {
    BuildDetails.version = version;
    BuildDetails.buildDate = buildDate;
    BuildDetails.installation = installation;
  }

  @Override
  public String getBuildDate()
  {
    return buildDate;
  }

  public static void setBuilddate(String buildDate)
  {
    BuildDetails.buildDate = buildDate;
  }

  @Override
  public String getVersion()
  {
    return version;
  }

  public static void setVersion(String version)
  {
    BuildDetails.version = version;
  }

  @Override
  public String getInstallation()
  {
    return installation;
  }

  public static void setInstallation(String installation)
  {
    BuildDetails.installation = installation;
  }

}
