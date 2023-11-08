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

import java.util.Map;
import java.util.Vector;

import com.github.reinert.jjschema.Attributes;

public class SequenceFeaturesPojo
{
  @Attributes(
    required = true,
    description = "Start residue position for the sequence feature")
  private int xStart;

  @Attributes(
    required = true,
    description = "End residue position for the sequence feature")
  private int xEnd;

  @Attributes(
    required = true,
    minItems = 1,
    maxItems = 2147483647,
    description = "Reference to the sequence in the alignment<br> (more like a foreign key)")
  private String sequenceRef;

  @Attributes(
    required = true,
    description = "The name or type of the SequenceFeature")
  private String type;

  @Attributes(required = false, description = "Score")
  private Float score;

  @Attributes(required = false, description = "Description for the feature")
  private String description;

  @Attributes(
    required = false,
    description = "Additional metadata for the feature")
  private Map<String, Object> otherDetails;

  @Attributes(required = false, description = "Fill colour")
  private String fillColor;

  @Attributes(required = true, description = "Feature group")
  private String featureGroup;

  @Attributes(
    required = false,
    description = "URL links associated to the feature")
  private Vector<String> links;

  public SequenceFeaturesPojo()
  {
  }

  public SequenceFeaturesPojo(String sequenceRef)
  {
    this.sequenceRef = sequenceRef;
  }

  public String getFillColor()
  {
    return "#" + fillColor;
  }

  public void setFillColor(String fillColor)
  {
    this.fillColor = fillColor;
  }

  public int getXstart()
  {
    return xStart;
  }

  public void setXstart(int xStart)
  {
    this.xStart = xStart;
  }

  public int getXend()
  {
    return xEnd;
  }

  public void setXend(int xend)
  {
    this.xEnd = xend;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public Float getScore()
  {
    return score;
  }

  public void setScore(Float score)
  {
    this.score = score;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public Map<String, Object> getOtherDetails()
  {
    return otherDetails;
  }

  public void setOtherDetails(Map<String, Object> otherDetails)
  {
    this.otherDetails = otherDetails;
  }

  public Vector<String> getLinks()
  {
    return links;
  }

  public void setLinks(Vector<String> links)
  {
    this.links = links;
  }

  public String getFeatureGroup()
  {
    return featureGroup;
  }

  public void setFeatureGroup(String featureGroup)
  {
    this.featureGroup = featureGroup;
  }

  public String getSequenceRef()
  {
    return sequenceRef;
  }

  public void setSequenceRef(String sequenceRef)
  {
    this.sequenceRef = sequenceRef;
  }

}
