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
package jalview.datamodel;

import jalview.analysis.scoremodels.ScoreMatrix;
import jalview.schemes.ResidueProperties;

/**
 * Encode a sequence as a numeric vector using either classic residue binary
 * encoding or convolved with residue substitution matrix.
 * 
 * @author $author$
 * @version $Revision$
 */
public class BinarySequence extends Sequence
{
  public class InvalidSequenceTypeException extends Exception
  {

    public InvalidSequenceTypeException(String string)
    {
      super(string);
    }

  }

  int[] binary;

  double[] dbinary;

  boolean isNa = false;

  /**
   * Creates a new BinarySequence object.
   * 
   * @param s
   *          DOCUMENT ME!
   */
  public BinarySequence(String s, boolean isNa)
  {
    super("", s, 0, s.length());
    this.isNa = isNa;
  }

  /**
   * clear the dbinary matrix
   * 
   * @return nores - dimension of sequence symbol encoding for this sequence
   */
  private int initMatrixGetNoRes()
  {
    int nores = (isNa) ? ResidueProperties.maxNucleotideIndex
            : ResidueProperties.maxProteinIndex;

    dbinary = new double[getLength() * nores];

    return nores;
  }

  private int[] getSymbolmatrix()
  {
    return (isNa) ? ResidueProperties.nucleotideIndex
            : ResidueProperties.aaIndex;
  }

  /**
   * DOCUMENT ME!
   */
  public void encode()
  {
    int nores = initMatrixGetNoRes();
    final int[] sindex = getSymbolmatrix();
    for (int i = 0; i < getLength(); i++)
    {
      int aanum = nores - 1;

      try
      {
        aanum = sindex[getCharAt(i)];
      } catch (NullPointerException e)
      {
        aanum = nores - 1;
      }

      if (aanum >= nores)
      {
        aanum = nores - 1;
      }

      dbinary[(i * nores) + aanum] = 1.0;
    }
  }

  /**
   * ancode using substitution matrix given in matrix
   * 
   * @param smtrx
   */
  public void matrixEncode(final ScoreMatrix smtrx)
          throws InvalidSequenceTypeException
  {
    if (isNa != smtrx.isDNA())
    {
      throw new InvalidSequenceTypeException(
              "matrix " + smtrx.getClass().getCanonicalName()
                      + " is not a valid matrix for "
                      + (isNa ? "nucleotide" : "protein") + "sequences");
    }
    matrixEncode(smtrx.isDNA() ? ResidueProperties.nucleotideIndex
            : ResidueProperties.aaIndex, smtrx.getMatrix());
  }

  private void matrixEncode(final int[] aaIndex, final float[][] matrix)
  {
    int nores = initMatrixGetNoRes();

    for (int i = 0, iSize = getLength(); i < iSize; i++)
    {
      int aanum = nores - 1;

      try
      {
        aanum = aaIndex[getCharAt(i)];
      } catch (NullPointerException e)
      {
        aanum = nores - 1;
      }

      if (aanum >= nores)
      {
        aanum = nores - 1;
      }

      // Do the blosum^H^H^H^H^H score matrix summation thing

      for (int j = 0; j < nores; j++)
      {
        dbinary[(i * nores) + j] = matrix[aanum][j];
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String toBinaryString()
  {
    String out = "";

    for (int i = 0; i < binary.length; i++)
    {
      out += (Integer.valueOf(binary[i])).toString();

      if (i < (binary.length - 1))
      {
        out += " ";
      }
    }

    return out;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public double[] getDBinary()
  {
    return dbinary;
  }

}
