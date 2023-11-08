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
package jalview.ext.varna;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceI;

import fr.orsay.lri.varna.models.rna.RNA;

/**
 * Data bean wrapping the data items that define one RNA view
 */
public class RnaModel
{
  public final String title;

  public final AlignmentAnnotation ann;

  public final SequenceI seq;

  public final boolean gapped;

  public final RNA rna;

  public RnaModel(String t, AlignmentAnnotation aa, SequenceI s, RNA r,
          boolean g)
  {
    title = t;
    ann = aa;
    seq = s;
    rna = r;
    gapped = g;
  }
}
