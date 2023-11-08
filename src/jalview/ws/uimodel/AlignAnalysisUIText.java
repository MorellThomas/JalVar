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
package jalview.ws.uimodel;

public class AlignAnalysisUIText
{

  private String serviceType;

  public String getServiceType()
  {
    return serviceType;
  }

  private Class client;

  private String calcId;

  public String getCalcId()
  {
    return calcId;
  }

  private String AAconToggle, AAconToggleTooltip, AAeditSettings,
          AAeditSettingsTooltip;

  private boolean isNa;

  public boolean isNa()
  {
    return isNa;
  }

  public boolean isPr()
  {
    return isPr;
  }

  public boolean isAA()
  {
    return isAA;
  }

  private boolean isPr;

  private boolean isAA;

  public AlignAnalysisUIText(String serviceType, Class<?> client,
          String calcId, boolean acceptNucl, boolean acceptProt,
          boolean acceptGaps, String toggle, String toggleTooltip,
          String settings, String settingsTooltip)
  {
    this.serviceType = serviceType;
    this.calcId = calcId;
    isNa = acceptNucl;
    isPr = acceptProt;
    isAA = acceptGaps;
    this.client = client;
    this.AAconToggle = toggle;
    this.AAconToggleTooltip = toggleTooltip;
    this.AAeditSettings = settings;
    this.AAeditSettingsTooltip = settingsTooltip;
  }

  public Class getClient()
  {
    return client;
  }

  public void setClient(Class client)
  {
    this.client = client;
  }

  public String getAAconToggle()
  {
    return AAconToggle;
  }

  public void setAAconToggle(String aAconToggle)
  {
    AAconToggle = aAconToggle;
  }

  public String getAAconToggleTooltip()
  {
    return AAconToggleTooltip;
  }

  public void setAAconToggleTooltip(String aAconToggleTooltip)
  {
    AAconToggleTooltip = aAconToggleTooltip;
  }

  public String getAAeditSettings()
  {
    return AAeditSettings;
  }

  public void setAAeditSettings(String aAeditSettings)
  {
    AAeditSettings = aAeditSettings;
  }

  public String getAAeditSettingsTooltip()
  {
    return AAeditSettingsTooltip;
  }

  public void setAAeditSettingsTooltip(String aAeditSettingsTooltip)
  {
    AAeditSettingsTooltip = aAeditSettingsTooltip;
  }

}
