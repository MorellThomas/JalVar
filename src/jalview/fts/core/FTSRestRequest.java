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

package jalview.fts.core;

import jalview.bin.Cache;
import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSDataColumnI;

import java.util.Collection;

/**
 * Represents the FTS request to be consumed by the FTSRestClient
 * 
 * @author tcnofoegbu
 *
 */
public class FTSRestRequest
{
  private String fieldToSearchBy;

  private String searchTerm;

  private String fieldToSortBy;

  private SequenceI associatedSequence;

  private boolean allowEmptySequence;

  private boolean allowUnpublishedEntries = Cache
          .getDefault("ALLOW_UNPUBLISHED_PDB_QUERYING", false);

  private boolean facet;

  private String facetPivot;

  private int facetPivotMinCount;

  private int responseSize;

  private int offSet;

  private boolean isSortAscending;

  private Collection<FTSDataColumnI> wantedFields;

  public String getFieldToSearchBy()
  {
    return fieldToSearchBy;
  }

  public void setFieldToSearchBy(String fieldToSearchBy)
  {
    this.fieldToSearchBy = fieldToSearchBy;
  }

  public String getSearchTerm()
  {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm)
  {
    this.searchTerm = searchTerm;
  }

  public boolean isAllowEmptySeq()
  {
    return allowEmptySequence;
  }

  public void setAllowEmptySeq(boolean allowEmptySeq)
  {
    this.allowEmptySequence = allowEmptySeq;
  }

  public int getResponseSize()
  {
    return responseSize;
  }

  public void setResponseSize(int responseSize)
  {
    this.responseSize = responseSize;
  }

  public Collection<FTSDataColumnI> getWantedFields()
  {
    return wantedFields;
  }

  public void setWantedFields(Collection<FTSDataColumnI> wantedFields)
  {
    this.wantedFields = wantedFields;
  }

  public String getFieldToSortBy()
  {
    return fieldToSortBy;
  }

  public void setFieldToSortBy(String fieldToSortBy,
          boolean isSortAscending)
  {
    this.fieldToSortBy = fieldToSortBy;
    this.isSortAscending = isSortAscending;
  }

  public boolean isAscending()
  {
    return isSortAscending;
  }

  public SequenceI getAssociatedSequence()
  {
    return associatedSequence;
  }

  public void setAssociatedSequence(SequenceI associatedSequence)
  {
    this.associatedSequence = associatedSequence;
  }

  public boolean isAllowUnpublishedEntries()
  {
    return allowUnpublishedEntries;
  }

  public void setAllowUnpublishedEntries(boolean allowUnpublishedEntries)
  {
    this.allowUnpublishedEntries = allowUnpublishedEntries;
  }

  public boolean isFacet()
  {
    return facet;
  }

  public void setFacet(boolean facet)
  {
    this.facet = facet;
  }

  public String getFacetPivot()
  {
    return facetPivot;
  }

  public void setFacetPivot(String facetPivot)
  {
    this.facetPivot = facetPivot;
  }

  public int getFacetPivotMinCount()
  {
    return facetPivotMinCount;
  }

  public void setFacetPivotMinCount(int facetPivotMinCount)
  {
    this.facetPivotMinCount = facetPivotMinCount;
  }

  public int getOffSet()
  {
    return offSet;
  }

  public void setOffSet(int offSet)
  {
    this.offSet = offSet;
  }

  /**
   * locate column given field name
   * 
   * @param string
   *          - field name
   * @return -1 if not located
   */
  public int getFieldIndex(String string)
  {
    int i = associatedSequence != null ? 1 : 0;
    for (FTSDataColumnI field : wantedFields)
    {
      if (field.getName().equals(string))
      {
        return i;
      }
      i++;
    }
    return -1;
  }
}
