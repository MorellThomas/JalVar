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

import jalview.ws.seqfetcher.DbSourceProxyImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public abstract class EbiFileRetrievedProxy extends DbSourceProxyImpl
{

  /**
   * temp path to retrieved file
   */
  protected String file = null;

  public StringBuffer getRawRecords()
  {
    if (file == null)
    {
      return null;
    }
    StringBuffer bf = null;
    try
    {
      File f = new File(file);
      if (f.exists())
      {
        bf = new StringBuffer();
        BufferedReader breader = new BufferedReader(new FileReader(f));
        String line = null;
        while (breader.ready() && (line = breader.readLine()) != null)
        {
          bf.append(line);
        }
        breader.close();
      }
    } catch (Exception e)
    {
      System.err.println("Warning: problems reading temp file " + file);
      return null;
    }
    return bf;
  }

}
