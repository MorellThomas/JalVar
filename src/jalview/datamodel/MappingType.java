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
 * An enumeration of the kinds of mapping (from nucleotide or peptide, to
 * nucleotide or peptide), and the corresponding word lengths
 */
public enum MappingType
{
  NucleotideToPeptide(3, 1)
  {
    @Override
    public MappingType getInverse()
    {
      return PeptideToNucleotide;
    }
  },
  PeptideToNucleotide(1, 3)
  {
    @Override
    public MappingType getInverse()
    {
      return NucleotideToPeptide;
    }
  },
  NucleotideToNucleotide(1, 1)
  {
    @Override
    public MappingType getInverse()
    {
      return NucleotideToNucleotide;
    }
  },
  PeptideToPeptide(1, 1)
  {
    @Override
    public MappingType getInverse()
    {
      return PeptideToPeptide;
    }
  };

  private int fromRatio;

  private int toRatio;

  private MappingType(int fromSize, int toSize)
  {
    fromRatio = fromSize;
    toRatio = toSize;
  }

  public abstract MappingType getInverse();

  public int getFromRatio()
  {
    return fromRatio;
  }

  public int getToRatio()
  {
    return toRatio;
  }
}
