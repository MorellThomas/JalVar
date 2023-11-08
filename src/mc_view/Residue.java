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

import java.util.Vector;

public class Residue
{
  Vector<Atom> atoms;

  int number;

  int count;

  public Residue(Vector<Atom> resAtoms, int number, int count)
  {
    this.atoms = resAtoms;
    this.number = number;
    this.count = count;
  }

  public Atom findAtom(String name)
  {
    for (Atom atom : atoms)
    {
      if (atom.name.equals(name))
      {
        return atom;
      }
    }

    return null;
  }

  public Vector<Atom> getAtoms()
  {
    return this.atoms;
  }
}
