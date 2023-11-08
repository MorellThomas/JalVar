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
package vamsas.objects.simple;

public class MsaResult extends vamsas.objects.simple.Result
        implements java.io.Serializable
{
  private vamsas.objects.simple.Alignment msa;

  private java.lang.Object __equalsCalc = null;

  private boolean __hashCodeCalc = false;

  public MsaResult()
  {
  }

  public MsaResult(vamsas.objects.simple.Alignment msa)
  {
    this.msa = msa;
  }

  /**
   * Gets the msa value for this MsaResult.
   * 
   * @return msa
   */
  public vamsas.objects.simple.Alignment getMsa()
  {
    return msa;
  }

  /**
   * Sets the msa value for this MsaResult.
   * 
   * @param msa
   */
  public void setMsa(vamsas.objects.simple.Alignment msa)
  {
    this.msa = msa;
  }

  @Override
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (!(obj instanceof MsaResult))
    {
      return false;
    }

    MsaResult other = (MsaResult) obj;

    if (this == obj)
    {
      return true;
    }

    if (__equalsCalc != null)
    {
      return (__equalsCalc == obj);
    }

    __equalsCalc = obj;

    boolean _equals;
    _equals = super.equals(obj) && (((this.msa == null)
            && (other.getMsa() == null))
            || ((this.msa != null) && this.msa.equals(other.getMsa())));
    __equalsCalc = null;

    return _equals;
  }

  @Override
  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }

    __hashCodeCalc = true;

    int _hashCode = super.hashCode();

    if (getMsa() != null)
    {
      _hashCode += getMsa().hashCode();
    }

    __hashCodeCalc = false;

    return _hashCode;
  }
}
