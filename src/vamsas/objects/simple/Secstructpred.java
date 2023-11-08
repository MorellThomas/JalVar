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

public class Secstructpred implements java.io.Serializable
{
  private java.lang.String output;

  public Secstructpred()
  {
  }

  public Secstructpred(java.lang.String output)
  {
    this.output = output;
  }

  /**
   * Gets the output value for this Secstructpred.
   * 
   * @return output
   */
  public java.lang.String getOutput()
  {
    return output;
  }

  /**
   * Sets the output value for this Secstructpred.
   * 
   * @param output
   */
  public void setOutput(java.lang.String output)
  {
    this.output = output;
  }

  private java.lang.Object __equalsCalc = null;

  public synchronized boolean equals(java.lang.Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (!(obj instanceof Secstructpred))
    {
      return false;
    }
    Secstructpred other = (Secstructpred) obj;
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
    _equals = true && ((this.output == null && other.getOutput() == null)
            || (this.output != null
                    && this.output.equals(other.getOutput())));
    __equalsCalc = null;
    return _equals;
  }

  private boolean __hashCodeCalc = false;

  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getOutput() != null)
    {
      _hashCode += getOutput().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

}
