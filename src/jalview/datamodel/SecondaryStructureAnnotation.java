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

import fr.orsay.lri.varna.models.rna.RNA;

public class SecondaryStructureAnnotation extends AlignmentAnnotation
{

  private static RNA _rna = null;

  public SecondaryStructureAnnotation(RNA rna)
  {
    super("Secondary Structure", "Un truc trop cool", getAnnotation(rna));

    _rna = rna;
  }

  public RNA getRNA()
  {
    return _rna;
  }

  public static Annotation[] getAnnotation(RNA rna)
  {
    Annotation[] ann = new Annotation[rna.getSize()];
    for (int i = 0; i < ann.length; i++)
    {
      ann[i] = new Annotation(_rna.getStructDBN(true), "", ' ', 0f);
      ;
    }
    return ann;
  }
}
