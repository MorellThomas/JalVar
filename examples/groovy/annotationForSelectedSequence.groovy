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
// Requested by David M Garcia (v3)
// very messy script to output the scores in annotation rows
// for the first sequence in a selection on the topmost alignment

// thanks to this thread for cut'n'paste code
// http://comments.gmane.org/gmane.comp.lang.groovy.user/53010

// Jalview issue at http://issues.jalview.org/browse/JAL-1516

import java.awt.datatransfer.StringSelection
import static java.awt.Toolkit.*


def curviewport = Jalview.getAlignFrames()[Jalview.getAlignFrames().length-1].getViewport();

// TSV output by default.
// change "\t" to "," to output CSV file
def sep = "\t"; 

if (curviewport.getSelectionGroup()) {
  // gets selection for topmost alignment
  def selreg = curviewport.getSelectionGroup();
  // get aligned positions of first sequence selected
  def gaps = selreg.getSequenceAt(0).gapMap(); 
  String csvfile="";
  String sseq=""

  curviewport.getAlignment().getAlignmentAnnotation().eachWithIndex{ aa, apos -> 
    def count=1
    String csv=""
    gaps.eachWithIndex{col,spos -> if (col>=selreg.getStartRes() && col<=selreg.getEndRes()) { 
      // add sequence for debugging
      if (count>sseq.length()) { 
          sseq+=sep+selreg.getSequenceAt(0).getCharAt(col); count=sseq.length()+1;
      };
      // output height of histogram
      csv+=sep;
      def annot = aa.annotations[col];
      if (annot != null) {
          csv+=aa.annotations[col].value; 
      }
      // Uncomment to output string shown in tooltip
      // csv+=sep+aa.annotations[col].description; 
    }}
    if (csv.length()>0) {
        csvfile+=aa.label+csv+"\n"
    }
  }
  defaultToolkit.systemClipboard.setContents(new StringSelection(selreg.getSequenceAt(0).getName()+sseq+"\n"+csvfile), null)
  print "Sequence"+sseq+"\n";
  print csvfile;
  print "\nAlignment Annotation for first selected sequence copied to the clipboard.\n"
} else {
    "Select a region in the alignment window.";
}
