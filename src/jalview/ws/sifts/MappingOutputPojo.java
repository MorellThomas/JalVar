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
package jalview.ws.sifts;

public class MappingOutputPojo
{
  private String seqName;

  private String seqResidue;

  private int seqStart;

  private int seqEnd;

  private String strName;

  private String strResidue;

  private int strStart;

  private int strEnd;

  private String type;

  private static final int MAX_ID_LENGTH = 30;

  public String getSeqName()
  {
    return seqName;
  }

  public void setSeqName(String seqName)
  {
    this.seqName = (seqName.length() > MAX_ID_LENGTH)
            ? seqName.substring(0, MAX_ID_LENGTH)
            : seqName;
  }

  public String getSeqResidue()
  {
    return seqResidue;
  }

  public void setSeqResidue(String seqResidue)
  {
    this.seqResidue = seqResidue;
  }

  public int getSeqStart()
  {
    return seqStart;
  }

  public void setSeqStart(int seqStart)
  {
    this.seqStart = seqStart;
  }

  public int getSeqEnd()
  {
    return seqEnd;
  }

  public void setSeqEnd(int seqEnd)
  {
    this.seqEnd = seqEnd;
  }

  public String getStrName()
  {
    return strName;
  }

  public void setStrName(String strName)
  {
    this.strName = (strName.length() > MAX_ID_LENGTH)
            ? strName.substring(0, MAX_ID_LENGTH)
            : strName;
  }

  public String getStrResidue()
  {
    return strResidue;
  }

  public void setStrResidue(String strResidue)
  {
    this.strResidue = strResidue;
  }

  public int getStrStart()
  {
    return strStart;
  }

  public void setStrStart(int strStart)
  {
    this.strStart = strStart;
  }

  public int getStrEnd()
  {
    return strEnd;
  }

  public void setStrEnd(int strEnd)
  {
    this.strEnd = strEnd;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

}
