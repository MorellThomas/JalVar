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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.reinert.jjschema.Attributes;

@Attributes(
  title = "BioJSON",
  description = "A specification for the representation and exchange of bioinformatics data")
public class AlignmentPojo
{
  @Attributes(
    required = true,
    description = "Serial version identifier for <b>BioJSON</b> schema")
  private String svid = "1.0";

  @Attributes(
    required = true,
    minItems = 1,
    description = "An array of Sequences which makes up the Alignment")
  private List<SequencePojo> seqs = new ArrayList<SequencePojo>();

  @Attributes(
    required = false,
    minItems = 0,
    exclusiveMaximum = true,
    description = "Alignment annotations stores symbols and graphs usually rendered </br>"
            + "below the alignment and often reflect properties of the alignment </br>as a whole.")
  private List<AlignmentAnnotationPojo> alignAnnotation = new ArrayList<AlignmentAnnotationPojo>();

  @Attributes(
    required = false,
    minItems = 0,
    description = "A sequence group is a rectangular region of an alignment <br>bounded by startRes and endRes positions in the alignment <br>coordinate system for a set of sequences")
  private List<SequenceGrpPojo> seqGroups = new ArrayList<SequenceGrpPojo>();

  @Attributes(
    required = false,
    minItems = 0,
    description = "Sequence features are properties of the individual sequences, <br>they do not change with the alignment, but are shown mapped<br> on to specific residues within the alignment")
  private List<SequenceFeaturesPojo> seqFeatures = new ArrayList<SequenceFeaturesPojo>();

  @Attributes(
    required = false,
    enums =
    { "None", "User Defined", "Clustal", "Zappo", "Taylor", "Nucleotide",
        "Pyrimidine", "Purine", "Turn", "Helix", "Strand", "Buried",
        "Hydro", "T-Coffee Scores", "RNA Interaction type", "Blosum62",
        "RNA Helices", "% Identity" },
    description = "The <a href=\"#colourScheme\">Colour Scheme</a> applied to the alignment")
  private String colourScheme;

  @Attributes(
    required = true,
    maxItems = 0,
    description = "AppSettings stores key=value pairs of custom application specific <br>"
            + "settings (i.e visualisation settings, etc) for different applications<br>"
            + "that consume or generate BioJSON")
  Map<String, Object> appSettings = new HashMap<String, Object>();

  public AlignmentPojo()
  {
  }

  public List<SequencePojo> getSeqs()
  {
    return seqs;
  }

  public void setSeqs(ArrayList<SequencePojo> seqs)
  {
    this.seqs = seqs;
  }

  public Map<String, Object> getAppSettings()
  {
    return appSettings;
  }

  public void setAppSettings(Map<String, Object> appSettings)
  {
    this.appSettings = appSettings;
  }

  public List<AlignmentAnnotationPojo> getAlignAnnotation()
  {
    return alignAnnotation;
  }

  public void setAlignAnnotation(
          List<AlignmentAnnotationPojo> alignAnnotation)
  {
    this.alignAnnotation = alignAnnotation;
  }

  public List<SequenceGrpPojo> getSeqGroups()
  {
    return seqGroups;
  }

  public void setSeqGroups(List<SequenceGrpPojo> seqGroups)
  {
    this.seqGroups = seqGroups;
  }

  public List<SequenceFeaturesPojo> getSeqFeatures()
  {
    return seqFeatures;
  }

  public void setSeqFeatures(List<SequenceFeaturesPojo> seqFeatures)
  {
    this.seqFeatures = seqFeatures;
  }

  public String getSvid()
  {
    return svid;
  }

  public void setGlobalColorScheme(String globalColorScheme)
  {
    this.appSettings.put("globalColorScheme", globalColorScheme);
  }

  public String getColourScheme()
  {
    return colourScheme;
  }

  public void setColourScheme(String colourScheme)
  {
    this.colourScheme = colourScheme;
  }

}
