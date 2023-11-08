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
// do something groovy in jalview
println "Hello World.\n"
println "First sequence is " + currentAlFrame.viewport.alignment.getSequenceAt(0).getDisplayId(true)

def alf = Jalview.getAlignFrames()
for (ala in alf)
{
	// ala is an jalview.gui.AlignFrame object 
	println ala.getTitle()
	// get the parent jalview.datamodel.Alignment from the alignment viewport
	def alignment = ala.viewport.alignment
	// get the first sequence from the jalview.datamodel.Alignment object
	def seq = alignment.getSequenceAt(0) 
}
Jalview.quit()
