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
package jalview.ws.dbsources;

import java.util.Locale;

import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;
import jalview.ws.seqfetcher.DbSourceProxyImpl;

/**
 * Acts as a superclass for the Rfam and Pfam classes
 * 
 * @author Lauren Michelle Lui
 * 
 */
public abstract class Xfam extends DbSourceProxyImpl
{
  public Xfam()
  {
    super();
  }

  /**
   * the base URL for this Xfam-like service
   * 
   * @return
   */
  protected abstract String getURLPrefix();

  @Override
  public abstract String getDbVersion();

  abstract String getXfamSource();

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    // TODO: this is not a perfect implementation. We need to be able to add
    // individual references to each sequence in each family alignment that's
    // retrieved.
    startQuery();
    // TODO: trap HTTP 404 exceptions and return null
    String xfamUrl = getURL(queries);

    Console.debug("XFAM URL for retrieval is: " + xfamUrl);

    AlignmentI rcds = new FormatAdapter().readFile(xfamUrl,
            DataSourceType.URL, FileFormat.Stockholm);

    for (int s = 0, sNum = rcds.getHeight(); s < sNum; s++)
    {
      rcds.getSequenceAt(s).addDBRef(new DBRefEntry(getXfamSource(),
              // getDbSource(),
              getDbVersion(), queries.trim().toUpperCase(Locale.ROOT)));
      if (!getDbSource().equals(getXfamSource()))
      { // add the specific ref too
        rcds.getSequenceAt(s).addDBRef(new DBRefEntry(getDbSource(),
                getDbVersion(), queries.trim().toUpperCase(Locale.ROOT)));
      }
    }
    stopQuery();
    return rcds;
  }

  String getURL(String queries)
  {
    return getURLPrefix() + "/family/"
            + queries.trim().toUpperCase(Locale.ROOT) + getURLSuffix();
  }

  /**
   * Pfam and Rfam provide alignments
   */
  @Override
  public boolean isAlignmentSource()
  {
    return true;
  }

  /**
   * default suffix to append the retrieval URL for this source.
   * 
   * @return "" for most Xfam sources
   */
  public String getURLSuffix()
  {
    return "";
  }

}
