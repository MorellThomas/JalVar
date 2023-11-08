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

public class SequenceSet implements java.io.Serializable
{
  private vamsas.objects.simple.Sequence[] seqs;

  public SequenceSet()
  {
  }

  public SequenceSet(vamsas.objects.simple.Sequence[] seqs)
  {
    this.seqs = seqs;
  }

  /**
   * Gets the seqs value for this SequenceSet.
   * 
   * @return seqs
   */
  public vamsas.objects.simple.Sequence[] getSeqs()
  {
    return seqs;
  }

  /**
   * Sets the seqs value for this SequenceSet.
   * 
   * @param seqs
   */
  public void setSeqs(vamsas.objects.simple.Sequence[] seqs)
  {
    this.seqs = seqs;
  }

  private java.lang.Object __equalsCalc = null;

  @Override
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (!(obj instanceof SequenceSet))
    {
      return false;
    }
    SequenceSet other = (SequenceSet) obj;
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
    _equals = true && ((this.seqs == null && other.getSeqs() == null)
            || (this.seqs != null && java.util.Arrays.equals(this.seqs,
                    other.getSeqs())));
    __equalsCalc = null;
    return _equals;
  }

  private boolean __hashCodeCalc = false;

  @Override
  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getSeqs() != null)
    {
      for (int i = 0; i < java.lang.reflect.Array.getLength(getSeqs()); i++)
      {
        java.lang.Object obj = java.lang.reflect.Array.get(getSeqs(), i);
        if (obj != null && !obj.getClass().isArray())
        {
          _hashCode += obj.hashCode();
        }
      }
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

}
