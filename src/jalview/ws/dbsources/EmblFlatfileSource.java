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

import java.io.File;
import java.io.IOException;

import com.stevesoft.pat.Regex;

import jalview.bin.Console;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.EmblFlatFile;
import jalview.io.FileParse;
import jalview.ws.ebi.EBIFetchClient;

/**
 * A class that does partial parsing of an EMBL flatfile.
 * 
 * @author gmcarstairs
 *
 */
public abstract class EmblFlatfileSource extends EbiFileRetrievedProxy
{
  private static final Regex ACCESSION_REGEX = new Regex("^[A-Z]+[0-9]+");

  @Override
  public String getDbVersion()
  {
    return "0";
  }

  @Override
  public String getAccessionSeparator()
  {
    return null;
  }

  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  @Override
  public boolean isValidReference(String accession)
  {
    if (accession == null || accession.length() < 2)
    {
      return false;
    }
    return getAccessionValidator().search(accession);
  }

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    return null;
  }

  @Override
  public int getTier()
  {
    return 0;
  }

  protected AlignmentI getEmblSequenceRecords(String dbName, String query)
          throws Exception
  {
    startQuery();
    EBIFetchClient dbFetch = new EBIFetchClient();
    File reply;
    try
    {
      reply = dbFetch.fetchDataAsFile(
              dbName.toLowerCase(Locale.ROOT) + ":" + query.trim(), null,
              "gz");
    } catch (Exception e)
    {
      stopQuery();
      throw new Exception(
              String.format("EBI EMBL retrieval failed for %s:%s",
                      dbName.toLowerCase(Locale.ROOT), query.trim()),
              e);
    }
    return getEmblSequenceRecords(dbName, query, reply);
  }

  private AlignmentI getEmblSequenceRecords(String dbName, String query,
          File reply) throws IOException
  {
    AlignmentI al = null;

    if (reply != null && reply.exists())
    {
      file = reply.getAbsolutePath();
      FileParse fp = new FileParse(file, DataSourceType.FILE);
      EmblFlatFile emblParser = new EmblFlatFile(fp, getDbSource());
      SequenceI[] seqs = emblParser.getSeqsAsArray();
      if (seqs.length > 0)
      {
        al = new Alignment(seqs);
      }

      if (al == null)
      {
        Console.error("No record found for '" + dbName + ":" + query + "'");
      }
    }

    stopQuery();
    return al;
  }

  @Override
  public boolean isDnaCoding()
  {
    return true;
  }
}
