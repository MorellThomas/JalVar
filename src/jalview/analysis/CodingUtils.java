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

/**
 * A utility class to provide encoding/decoding schemes for data.
 * 
 * @author gmcarstairs
 *
 */
public class CodingUtils
{

  /*
   * Number of bits used when encoding codon characters. 2 is enough for ACGT.
   * To accommodate more (e.g. ambiguity codes), simply increase this number
   * (and adjust unit tests to match).
   */
  private static final int CODON_ENCODING_BITSHIFT = 2;

  /**
   * Encode a codon from e.g. ['A', 'G', 'C'] to a number in the range 0 - 63.
   * Converts lower to upper case, U to T, then assembles a binary value by
   * encoding A/C/G/T as 00/01/10/11 respectively and shifting.
   * 
   * @param codon
   * @return the encoded codon, or a negative number if unexpected characters
   *         found
   */
  public static int encodeCodon(char[] codon)
  {
    if (codon == null)
    {
      return -1;
    }
    return encodeCodon(codon[2])
            + (encodeCodon(codon[1]) << CODON_ENCODING_BITSHIFT)
            + (encodeCodon(codon[0]) << (2 * CODON_ENCODING_BITSHIFT));
  }

  /**
   * Encodes aA/cC/gG/tTuU as 0/1/2/3 respectively. Returns Integer.MIN_VALUE (a
   * large negative value) for any other character.
   * 
   * @param c
   * @return
   */
  public static int encodeCodon(char c)
  {
    int result = Integer.MIN_VALUE;
    switch (c)
    {
    case 'A':
    case 'a':
      result = 0;
      break;
    case 'C':
    case 'c':
      result = 1;
      break;
    case 'G':
    case 'g':
      result = 2;
      break;
    case 'T':
    case 't':
    case 'U':
    case 'u':
      result = 3;
      break;
    }
    return result;
  }

  /**
   * Converts a binary encoded codon into an ['A', 'C', 'G'] (or 'T') triplet.
   * 
   * The two low-order bits encode for A/C/G/T as 0/1/2/3, etc.
   * 
   * @param encoded
   * @return
   */
  public static char[] decodeCodon(int encoded)
  {
    char[] result = new char[3];
    result[2] = decodeNucleotide(encoded & 3);
    encoded = encoded >>> CODON_ENCODING_BITSHIFT;
    result[1] = decodeNucleotide(encoded & 3);
    encoded = encoded >>> CODON_ENCODING_BITSHIFT;
    result[0] = decodeNucleotide(encoded & 3);
    return result;
  }

  public static void decodeCodon2(int encoded, char[] result)
  {
    result[2] = decodeNucleotide(encoded & 3);
    encoded = encoded >>> CODON_ENCODING_BITSHIFT;
    result[1] = decodeNucleotide(encoded & 3);
    encoded = encoded >>> CODON_ENCODING_BITSHIFT;
    result[0] = decodeNucleotide(encoded & 3);
  }

  /**
   * Convert value 0/1/2/3 to 'A'/'C'/'G'/'T'
   * 
   * @param i
   * @return
   */
  public static char decodeNucleotide(int i)
  {
    char result = '0';
    switch (i)
    {
    case 0:
      result = 'A';
      break;
    case 1:
      result = 'C';
      break;
    case 2:
      result = 'G';
      break;
    case 3:
      result = 'T';
      break;
    }
    return result;
  }

}
