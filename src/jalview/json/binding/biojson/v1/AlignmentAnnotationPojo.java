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
import java.util.List;

import com.github.reinert.jjschema.Attributes;

public class AlignmentAnnotationPojo
{

  @Attributes(
    required = false,
    description = "Label for the alignment annotation")
  private String label;

  @Attributes(
    required = false,
    description = "Description for the alignment annotation")
  private String description;

  @Attributes(required = false)
  private List<AnnotationPojo> annotations = new ArrayList<AnnotationPojo>();

  @Attributes(
    required = false,
    enums =
    { "0", "1", "2" },
    description = "Determines the rendering for the annotation<br><ul><li>0 - No graph</li><li>1 - Bar Graph</li><li>2 - Line graph</li></ul>")
  private int graphType;

  @Attributes(
    required = false,
    description = "Reference to the sequence in the alignment<br> if per-sequence annotation")
  private String sequenceRef;

  @Attributes(
    required = false,
    description = "Stores display settings for an annotation")
  private AnnotationDisplaySettingPojo annotationSettings;

  @Attributes(required = false, description = "Score of the annotation")
  private double score;

  @Attributes(
    required = false,
    description = "The annotation generation source")
  private String calcId;

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String annotationId)
  {
    this.description = annotationId;
  }

  public List<AnnotationPojo> getAnnotations()
  {
    return annotations;
  }

  public void setAnnotations(List<AnnotationPojo> annotations)
  {
    this.annotations = annotations;
  }

  public String getSequenceRef()
  {
    return sequenceRef;
  }

  public void setSequenceRef(String sequenceRef)
  {
    this.sequenceRef = sequenceRef;
  }

  public int getGraphType()
  {
    return graphType;
  }

  public void setGraphType(int graphType)
  {
    this.graphType = graphType;
  }

  public AnnotationDisplaySettingPojo getAnnotationSettings()
  {
    return annotationSettings;
  }

  public void setAnnotationSettings(
          AnnotationDisplaySettingPojo annotationSettings)
  {
    this.annotationSettings = annotationSettings;
  }

  public double getScore()
  {
    return score;
  }

  public void setScore(double score)
  {
    this.score = score;
  }

  public String getCalcId()
  {
    return calcId;
  }

  public void setCalcId(String calcId)
  {
    this.calcId = calcId;
  }

}
