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
package jalview.fts.service.alphafold;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jalview.bin.Console;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.core.FTSRestRequest;
import jalview.util.DBRefUtils;
import jalview.util.HttpUtils;
import jalview.ws.dbsources.EBIAlfaFold;

public class AlphafoldRestClient
{

  /**
   * turns a uniprot ID into a fake alphafold entry for the structure chooser -
   * fakes PDB fields in response
   * 
   * @param UniprotID
   * @return null or an FTS Record (if alphafold thinks it has a structure)
   */
  public static List<FTSData> getFTSData(// Map<String, Object> pdbJsonDoc,
          FTSRestRequest request)
  {
    List<FTSData> records = new ArrayList<FTSData>();
    String primaryKey = null;

    Object[] summaryRowData;

    SequenceI associatedSequence;

    Collection<FTSDataColumnI> diplayFields = request.getWantedFields();
    SequenceI associatedSeq = request.getAssociatedSequence();

    for (DBRefEntry upref : DBRefUtils
            .selectRefs(associatedSeq.getPrimaryDBRefs(), new String[]
            { DBRefSource.UNIPROT }))
    {
      String alphaFoldId = "AF-" + upref.getAccessionId() + "-F1";
      try
      {
        String urls = EBIAlfaFold.getAlphaFoldCifDownloadUrl(alphaFoldId,
                null);
        URL url = new URL(urls);
        if (!HttpUtils.checkUrlAvailable(url, 50))
        {
          continue;
        }
      } catch (Exception mfe)
      {
        Console.debug("Exception accessing urls", mfe);
        continue;
      }
      int colCounter = 0;
      summaryRowData = new Object[(associatedSeq != null)
              ? diplayFields.size() + 1
              : diplayFields.size()];
      if (associatedSeq != null)
      {
        associatedSequence = associatedSeq;
        summaryRowData[0] = associatedSequence;
        colCounter = 1;
      }

      for (FTSDataColumnI field : diplayFields)
      {
        String fieldData = "alphafold";// (pdbJsonDoc.get(field.getCode()) ==
                                       // null) ? ""
        // : pdbJsonDoc.get(field.getCode()).toString();
        if (field.isPrimaryKeyColumn())
        {
          primaryKey = alphaFoldId;
          summaryRowData[colCounter++] = alphaFoldId;
        }
        else if (fieldData == null || fieldData.isEmpty())
        {
          summaryRowData[colCounter++] = null;
        }
        else
        {
          try
          {
            summaryRowData[colCounter++] = (field.getDataType()
                    .getDataTypeClass() == Integer.class)
                            ? 1
                            : (field.getDataType()
                                    .getDataTypeClass() == Double.class)
                                            ? 1.3131313
                                            : "AlphaFold clarity";
          } catch (Exception e)
          {
            e.printStackTrace();
            System.out.println("offending value:" + fieldData);
          }
        }
      }

      final String primaryKey1 = primaryKey;

      final Object[] summaryRowData1 = summaryRowData;
      records.add(new FTSData()
      {
        @Override
        public Object[] getSummaryData()
        {
          return summaryRowData1;
        }

        @Override
        public Object getPrimaryKey()
        {
          return primaryKey1;
        }

        /**
         * Returns a string representation of this object;
         */
        @Override
        public String toString()
        {
          StringBuilder summaryFieldValues = new StringBuilder();
          for (Object summaryField : summaryRowData1)
          {
            summaryFieldValues.append(
                    summaryField == null ? " " : summaryField.toString())
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
          return Objects.hash(primaryKey1, this.toString());
        }

        @Override
        public boolean equals(Object that)
        {
          return this.toString().equals(that.toString());
        }
      });
    }
    return records;
  }
}
