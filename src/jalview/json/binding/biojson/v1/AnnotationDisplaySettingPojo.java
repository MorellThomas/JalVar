/*******************************************************************************
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
 ******************************************************************************/
package jalview.json.binding.biojson.v1;

import com.github.reinert.jjschema.Attributes;

public class AnnotationDisplaySettingPojo
{

  @Attributes(
    required = false,
    description = "Indicates if column label is scaled to fit within the <br>alignment column")
  private boolean scaleColLabel;

  @Attributes(
    required = false,
    description = "Indicates if every column label is displayed.")
  private boolean showAllColLabels;

  @Attributes(
    required = false,
    description = "Indicates if column labels is centred relative to the <br>alignment column")
  private boolean centreColLabels;

  @Attributes(
    required = false,
    description = "Indicates if the Annotation is shown below the alignment")
  private boolean belowAlignment;

  @Attributes(
    required = false,
    description = "Indicates if the annotation row is visible")
  private boolean visible;

  @Attributes(
    required = false,
    description = "Indicates if annotation has a graphical symbol track")
  private boolean hasIcon;

  public boolean isScaleColLabel()
  {
    return scaleColLabel;
  }

  public void setScaleColLabel(boolean scaleColLabel)
  {
    this.scaleColLabel = scaleColLabel;
  }

  public boolean isShowAllColLabels()
  {
    return showAllColLabels;
  }

  public void setShowAllColLabels(boolean showAllColLabels)
  {
    this.showAllColLabels = showAllColLabels;
  }

  public boolean isCentreColLabels()
  {
    return centreColLabels;
  }

  public void setCentreColLabels(boolean centreColLabels)
  {
    this.centreColLabels = centreColLabels;
  }

  public boolean isBelowAlignment()
  {
    return belowAlignment;
  }

  public void setBelowAlignment(boolean belowAlignment)
  {
    this.belowAlignment = belowAlignment;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  public boolean isHasIcon()
  {
    return hasIcon;
  }

  public void setHasIcon(boolean hasIcon)
  {
    this.hasIcon = hasIcon;
  }

}
