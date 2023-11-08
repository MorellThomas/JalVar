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
package jalview.analysis;

public class ConnectivityException extends RuntimeException
{
  private String sequence;
  private int connection;
  private byte dim;

  public ConnectivityException(String sequence, int connection, byte dim)
  {
    this("Insufficient number of connections", sequence, connection, dim);
  }

  public ConnectivityException(String message, String sequence, int connection, byte dim)
  {
    super(String.format("%s for %s (%d, should be %d or more)", message, sequence, connection, dim));
    this.sequence = sequence;
    this.connection = connection;
    this.dim = dim;
  }

  public String getSequence()
  {
    return sequence;
  }

  public int getConnection()
  {
    return connection;
  }

  public byte getDim()
  {
    return dim;
  }

}
