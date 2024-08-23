/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.3.2)
 * Copyright (C) 2023 The Jalview Authors
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
package jalview.fts.service.threedbeacons;

import java.util.Map;
import java.util.Objects;

import jalview.fts.api.FTSData;

/**
 * TDB result bean - holds filtered fields for GUI and essential metadata fields
 * for back end
 * 
 * @author jprocter
 *
 */
public class TDB_FTSData implements FTSData
{
  String primaryKey;

  Object[] summaryRowData;

  /*
   * fields in the JSON object 
   */
  public static String Uniprot_Id = "id";

  public static String Uniprot_Start = "uniprot_start";

  public static String Uniprot_End = "uniprot_end";

  public static String Provider = "provider";

  public static String Model_id = "model_identifier";

  public static String Model_Category = "model_category";

  public static String Model_Type = "model_type";

  public static String Model_Title = "model_title";

  public static String Resolution = "resolution";

  public static String Confidence = "confidence_avg_local_score";

  public static String Confidence_Score_Type = "confidence_type";

  public static String Confidence_Score_Version = "confidence_version";

  public static String Coverage = "coverage";

  public static String Sequence_Identity = "sequence_identity";

  public static String Created_Date = "created";

  public static String UniProt_Accession = "uniprot_accession";

  public static String Url = "model_url";

  public static String Page_URL = "model_page_url";

  public static String Ensemble_Sample_Url = "ensembl_sample_url";

  /**
   * original response from server
   */
  Map<String, Object> tdb_entry;

  public TDB_FTSData(String primaryKey,
          Map<String, Object> tdbJsonStructure, Object[] summaryData)
  {
    this.primaryKey = primaryKey;
    tdb_entry = tdbJsonStructure;
    this.summaryRowData = summaryData;
  }

  public Object getField(String key)
  {
    return tdb_entry.get(key);
  }

  @Override
  public Object[] getSummaryData()
  {
    return summaryRowData;
  }

  @Override
  public Object getPrimaryKey()
  {
    return primaryKey;
  }

  /**
   * Returns a string representation of this object;
   */
  @Override
  public String toString()
  {
    StringBuilder summaryFieldValues = new StringBuilder();
    for (Object summaryField : summaryRowData)
    {
      summaryFieldValues
              .append(summaryField == null ? " " : summaryField.toString())
              .append("\t");
    }
    return summaryFieldValues.toString();
  }

  /**
   * Returns hash code value for this object
   */
  @Override
  public int hashCode()
  {
    return Objects.hash(primaryKey, this.toString());
  }

  @Override
  public boolean equals(Object that)
  {
    return this.toString().equals(that.toString());
  }

  public String getProvider()
  {
    return (String) getField(Provider);
  }

  public String getModelViewUrl()
  {
    return (String) getField(Page_URL);
  }

  public String getModelId()
  {
    return (String) getField(Model_id);
  }

  public String getConfidenceScoreType()
  {
    return (String) getField(Confidence_Score_Type);
  }

  public String getConfidenceScoreVersion()
  {
    return (String) getField(Confidence_Score_Version);
  }

}
