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
package jalview.viewmodel.annotationfilter;

import java.util.ArrayList;
import java.util.List;

public class AnnotationFilterParameter
{
  public enum ThresholdType
  {
    NO_THRESHOLD, BELOW_THRESHOLD, ABOVE_THRESHOLD;
  }

  public enum SearchableAnnotationField
  {
    DISPLAY_STRING, DESCRIPTION;
  }

  private ThresholdType thresholdType;

  private float thresholdValue;

  private boolean filterAlphaHelix = false;

  private boolean filterBetaSheet = false;

  private boolean filterTurn = false;

  private String regexString;

  private List<SearchableAnnotationField> regexSearchFields = new ArrayList<SearchableAnnotationField>();

  public ThresholdType getThresholdType()
  {
    return thresholdType;
  }

  public void setThresholdType(ThresholdType thresholdType)
  {
    this.thresholdType = thresholdType;
  }

  public float getThresholdValue()
  {
    return thresholdValue;
  }

  public void setThresholdValue(float thresholdValue)
  {
    this.thresholdValue = thresholdValue;
  }

  public String getRegexString()
  {
    return regexString;
  }

  public void setRegexString(String regexString)
  {
    this.regexString = regexString;
  }

  public List<SearchableAnnotationField> getRegexSearchFields()
  {
    return regexSearchFields;
  }

  public void addRegexSearchField(
          SearchableAnnotationField regexSearchField)
  {
    this.regexSearchFields.add(regexSearchField);
  }

  public boolean isFilterAlphaHelix()
  {
    return filterAlphaHelix;
  }

  public void setFilterAlphaHelix(boolean alphaHelix)
  {
    this.filterAlphaHelix = alphaHelix;
  }

  public boolean isFilterBetaSheet()
  {
    return filterBetaSheet;
  }

  public void setFilterBetaSheet(boolean betaSheet)
  {
    this.filterBetaSheet = betaSheet;
  }

  public boolean isFilterTurn()
  {
    return filterTurn;
  }

  public void setFilterTurn(boolean turn)
  {
    this.filterTurn = turn;
  }

}
