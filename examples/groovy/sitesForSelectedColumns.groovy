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
// Requested by Hari Jayaram 
// script to output residue positions and values for selected region

// thanks to this thread for cut'n'paste code
// http://comments.gmane.org/gmane.comp.lang.groovy.user/53010

// Jalview issue at http://issues.jalview.org/browse/JAL-1542

import java.awt.datatransfer.StringSelection
import static java.awt.Toolkit.*

def curviewport = Jalview.getAlignFrames()[Jalview.getAlignFrames().length-1].getViewport()

def debug = false

// TSV output by default.
// change "\t" to "," to output CSV file
def sep = "\t"
def gapChar = "-"

if (curviewport.getSelectionGroup()) {
  // gets selection for topmost alignment
  def selreg = curviewport.getSelectionGroup()
  def groupStartCol = selreg.getStartRes()
  def groupEndCol = selreg.getEndRes()

    if (debug) {println "groupStartCol: " + groupStartCol + ", groupEndCol: " + groupEndCol}
      
  def csv=new StringBuilder(512)

  // for each sequence in the current selection
  selreg.getSequences().eachWithIndex{ seq, seqNo ->
    csv.append(seq.getDisplayId(false).padRight(20,' '))
    // get map of sequence sites to alignment positions 
    def gaps = seq.gapMap()
    if (debug) {println "gaps: " + gaps}

    // initialise loop variable to 'not quite shown first column'    
    def lastColShown = groupStartCol-1
    
    for (mapPos=0 ; ; mapPos++) {
        def nextResiduePos = gaps[mapPos]
        // skip over sites that precede selected columns
        if (nextResiduePos < groupStartCol) {
            continue;
        }

        if (debug) {println "mapPos: " + mapPos + ", lastColShown: " + lastColShown + ", nextResiduePos: " + nextResiduePos + ", csv: " + csv}

        // fill in any gaps
        while (lastColShown < groupEndCol && lastColShown+1 < nextResiduePos) {
            csv.append(sep+gapChar)
            lastColShown++
        }
        if (lastColShown >= groupEndCol) {
            break
        }
        lastColShown = nextResiduePos
        def residue = seq.getDatasetSequence().getCharAt(mapPos)
        csv.append(sep+(mapPos+1) + " (" + residue + ")")    // user output is base 1
    }
    csv.append("\n")      
  }
  def result = csv.toString()
  defaultToolkit.systemClipboard.setContents(new StringSelection(result), null)
  print result
  println "Sites for selected region copied to the clipboard"
} else {
    "Select a region in the alignment window."
}
