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
package jalview.json.binding.biojson.v1;

import java.util.ArrayList;

import com.github.reinert.jjschema.Attributes;

public class SequenceGrpPojo
{
  @Attributes(
    required = false,
    description = "The <a href=\"#colourScheme\">Colour Scheme</a> applied to the Sequence Group")
  private String colourScheme;

  @Attributes(
    required = true,
    description = "The name assigned to the seqGroup")
  private String groupName;

  @Attributes(
    required = false,
    description = "Serial version identifier for the <b>seqGroup</b> object model")
  private String description;

  @Attributes(
    required = false,
    description = "Determines if the seqGroup border should be visible or not")
  private boolean displayBoxes;

  @Attributes(
    required = false,
    description = "Determines if the texts of the group is displayed or not")
  private boolean displayText;

  @Attributes(
    required = false,
    description = "Determines if the residues text for the group is coloured")
  private boolean colourText;

  @Attributes(
    required = false,
    description = "Boolean value indicating whether residues should only be shown <br/>that are different from current reference or consensus sequence")
  private boolean showNonconserved;

  @Attributes(
    required = true,
    description = "The index of the group’s first residue in the alignment space")
  private int startRes;

  @Attributes(
    required = true,
    description = "The index of the group’s last residue in the alignment space")
  private int endRes;

  @Attributes(
    required = true,
    minItems = 1,
    uniqueItems = true,
    description = "An array of the unique id's for the sequences belonging to the group")
  private ArrayList<String> sequenceRefs = new ArrayList<String>();

  public String getColourScheme()
  {
    return colourScheme;
  }

  public void setColourScheme(String colourScheme)
  {
    this.colourScheme = colourScheme;
  }

  public String getGroupName()
  {
    return groupName;
  }

  public void setGroupName(String groupName)
  {
    this.groupName = groupName;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public boolean isDisplayBoxes()
  {
    return displayBoxes;
  }

  public void setDisplayBoxes(boolean displayBoxes)
  {
    this.displayBoxes = displayBoxes;
  }

  public boolean isDisplayText()
  {
    return displayText;
  }

  public void setDisplayText(boolean displayText)
  {
    this.displayText = displayText;
  }

  public boolean isColourText()
  {
    return colourText;
  }

  public void setColourText(boolean colourText)
  {
    this.colourText = colourText;
  }

  public boolean isShowNonconserved()
  {
    return showNonconserved;
  }

  public void setShowNonconserved(boolean showNonconserved)
  {
    this.showNonconserved = showNonconserved;
  }

  public int getStartRes()
  {
    return startRes;
  }

  public void setStartRes(int startRes)
  {
    this.startRes = startRes;
  }

  public int getEndRes()
  {
    return endRes;
  }

  public void setEndRes(int endRes)
  {
    this.endRes = endRes;
  }

  public ArrayList<String> getSequenceRefs()
  {
    return sequenceRefs;
  }

  public void setSequenceRefs(ArrayList<String> sequenceRefs)
  {
    this.sequenceRefs = sequenceRefs;
  }

}
