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
package mc_view;

import jalview.schemes.ResidueProperties;

import java.awt.Color;

public class Atom
{
  public float x;

  public float y;

  public float z;

  public int number;

  public String name;

  public String resName;

  public int resNumber;

  public char insCode = ' ';

  public String resNumIns = null;

  public int type;

  Color color = Color.lightGray;

  public String chain;

  /**
   * this is a temporary value - designed to store the position in sequence that
   * this atom corresponds to after aligning the chain to a SequenceI object. Do
   * not rely on its value being correct when visualizing sequence colourings on
   * the structure - use the StructureSelectionManager's mapping instead.
   */
  public int alignmentMapping = -1;

  public int atomIndex;

  public float occupancy = 0;

  public float tfactor = 0;

  // need these if we ever want to export Atom data
  // public boolean tfacset=true,occset=true;
  public boolean isSelected = false;

  public Atom(String str)
  {
    atomIndex = Integer.parseInt(str.substring(6, 11).trim());

    name = str.substring(12, 15).trim();

    resName = str.substring(17, 20);
    // JAL-1828 treat MSE Selenomethionine as MET (etc)
    resName = ResidueProperties.getCanonicalAminoAcid(resName);

    chain = str.substring(21, 22);

    resNumber = Integer.parseInt(str.substring(22, 26).trim());
    resNumIns = str.substring(22, 27).trim();
    insCode = str.substring(26, 27).charAt(0);
    this.x = (new Float(str.substring(30, 38).trim()).floatValue());
    this.y = (new Float(str.substring(38, 46).trim()).floatValue());
    this.z = (new Float(str.substring(47, 55).trim()).floatValue());
    // optional entries - see JAL-730
    String tm = str.substring(54, 60).trim();
    if (tm.length() > 0)
    {
      occupancy = (new Float(tm)).floatValue();
    }
    else
    {
      occupancy = 1f; // default occupancy
      // see note above: occset=false;
    }
    tm = str.substring(60, 66).trim();
    if (tm.length() > 0)
    {
      tfactor = (new Float(tm).floatValue());
    }
    else
    {
      tfactor = 1f;
      // see note above: tfacset=false;
    }
  }

  @Override
  public boolean equals(Object that)
  {
    if (this == that || that == null)
    {
      return true;
    }
    if (that instanceof Atom)
    {
      Atom other = (Atom) that;
      return other.resName.equals(this.resName)
              && other.resNumber == this.resNumber
              && other.resNumIns.equals(this.resNumIns)
              && other.chain.equals(this.chain);
    }
    return false;
  }

  public Atom(float x, float y, float z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
