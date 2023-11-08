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
import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;

def af = Jalview.getAlignFrames();

// walk through  all alignments, stripping off all text prior to and including last '|' symbol in sequence IDs

for (ala in af)
{
	def al = ala.viewport.alignment;
	if (al!=null)
	{
		SequenceI[] seqs = al.getSequencesArray();
		for (sq in seqs)
		{
			if (sq!=null) {
				if (sq.getName().indexOf("|")>-1)
				{
					sq.setName(sq.getName().substring(sq.getName().lastIndexOf("|")+1));
					if (sq.getDatasetSequence()!=null)
					{
						sq.getDatasetSequence().setName(sq.getName());
					}
				}
			}
		}
	}
}
	