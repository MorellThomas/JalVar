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

/**
 * Holds the aligned column positions (base 0) for one codon in a nucleotide
 * sequence, and (optionally) its peptide translation. The object is immutable
 * once created.
 * 
 * Example: in "G-AT-C-GA" the aligned codons are (0, 2, 3) and (5, 7, 8).
 * 
 * @author gmcarstairs
 *
 */
public final class AlignedCodon
{
  // base 1 aligned sequence position (base 0)
  public final int pos1;

  // base 2 aligned sequence position (base 0)
  public final int pos2;

  // base 3 aligned sequence position (base 0)
  public final int pos3;

  // peptide aligned sequence position (base 0)
  public final int peptideCol;

  // peptide coded for by this codon
  public final String product;

  public AlignedCodon(int i, int j, int k)
  {
    this(i, j, k, null, 0);
  }

  public AlignedCodon(int i, int j, int k, String prod, int prodCol)
  {
    pos1 = i;
    pos2 = j;
    pos3 = k;
    product = prod;
    peptideCol = prodCol;
  }

  /**
   * Returns the column position for the given base (1, 2, 3).
   * 
   * @param base
   * @return
   * @throws IllegalArgumentException
   *           if an argument value other than 1, 2 or 3 is supplied
   */
  public int getBaseColumn(int base)
  {
    if (base < 1 || base > 3)
    {
      throw new IllegalArgumentException(Integer.toString(base));
    }
    return base == 1 ? pos1 : (base == 2 ? pos2 : pos3);
  }

  /**
   * Two aligned codons are equal if all their base positions are the same. We
   * don't care about the protein product. This test is required for correct
   * alignment of translated gapped dna alignments (the same codon positions in
   * different sequences occupy the same column in the translated alignment).
   */
  @Override
  public boolean equals(Object o)
  {
    /*
     * Equality with null value required for consistency with
     * Dna.compareCodonPos
     */
    if (o == null)
    {
      return true;
    }
    if (!(o instanceof AlignedCodon))
    {
      return false;
    }
    AlignedCodon ac = (AlignedCodon) o;
    return (pos1 == ac.pos1 && pos2 == ac.pos2 && pos3 == ac.pos3);
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("[").append(pos1).append(", ").append(pos2).append(", ")
            .append(pos3).append("]");
    return sb.toString();
  }
}
