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
package jalview.ws.seqfetcher;

import jalview.api.FeatureSettingsModelI;
import jalview.datamodel.AlignmentI;
import jalview.io.DataSourceType;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;
import jalview.io.IdentifyFile;

/**
 * common methods for implementations of the DbSourceProxy interface.
 * 
 * @author JimP
 * 
 */
public abstract class DbSourceProxyImpl implements DbSourceProxy
{

  boolean queryInProgress = false;

  protected StringBuffer results = null;

  public DbSourceProxyImpl()
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getRawRecords()
   */
  @Override
  public StringBuffer getRawRecords()
  {
    return results;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#queryInProgress()
   */
  @Override
  public boolean queryInProgress()
  {
    return queryInProgress;
  }

  /**
   * call to set the queryInProgress flag
   * 
   */
  protected void startQuery()
  {
    queryInProgress = true;
  }

  /**
   * call to clear the queryInProgress flag
   * 
   */
  protected void stopQuery()
  {
    queryInProgress = false;
  }

  /**
   * create an alignment from raw text file...
   * 
   * @param result
   * @return null or a valid alignment
   * @throws Exception
   */
  protected AlignmentI parseResult(String result) throws Exception
  {
    AlignmentI sequences = null;
    FileFormatI format = new IdentifyFile().identify(result,
            DataSourceType.PASTE);
    if (format != null)
    {
      sequences = new FormatAdapter().readFile(result.toString(),
              DataSourceType.PASTE, format);
    }
    return sequences;
  }

  /**
   * Returns the first accession id in the query (up to the first accession id
   * separator), or the whole query if there is no separator or it is not found
   */
  @Override
  public String getAccessionIdFromQuery(String query)
  {
    String sep = getAccessionSeparator();
    if (sep == null)
    {
      return query;
    }
    int sepPos = query.indexOf(sep);
    return sepPos == -1 ? query : query.substring(0, sepPos);
  }

  /**
   * Default is only one accession id per query - override if more are allowed.
   */
  @Override
  public int getMaximumQueryCount()
  {
    return 1;
  }

  /**
   * Returns false - override to return true for DNA coding data sources
   */
  @Override
  public boolean isDnaCoding()
  {
    return false;
  }

  /**
   * Answers false - override as required in subclasses
   */
  @Override
  public boolean isAlignmentSource()
  {
    return false;
  }

  @Override
  public String getDescription()
  {
    return "";
  }

  @Override
  public FeatureSettingsModelI getFeatureColourScheme()
  {
    return null;
  }
}
