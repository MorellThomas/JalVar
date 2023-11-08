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

def todie = "PDBFile"; // about to delete this type of group
for (ala in af)
{
	def al = ala.viewport.alignment.getDataset();
	if (al!=null)
	{
		SequenceI[] seqs = al.getSequencesArray();
		for (sq in seqs)
		{
			if (sq!=null) {
				SequenceFeature[] sf = sq.getSequenceFeatures();
				for (sfpos in sf)
				{
				    if (sfpos!=null && sfpos.getFeatureGroup().equals(todie))
					{
						sq.deleteFeature(sfpos);
					}
				}
			}
		}
	}
}
	

