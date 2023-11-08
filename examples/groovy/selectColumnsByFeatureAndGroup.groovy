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
import java.util.BitSet;
import javax.swing.JOptionPane;
import groovy.swing.SwingBuilder;
def toselect = getFeatureInput(); // change this to select the desired feature type

def nal=0;
def nfeat=0;
def nseq=0;

for (ala in Jalview.getAlignFrames()) {
  def al = ala.viewport.alignment;
    if (al!=null)
    {
      BitSet bs = new BitSet();
      SequenceI[] seqs = al.getSequencesArray();
      for (sq in seqs)
      {
          def tfeat=0;
        if (sq!=null) {
          SequenceFeature[] sf = sq.getSequenceFeatures();
          for (sfpos in sf)
          {
            if (sfpos!=null && sfpos.getType().equals(toselect))
            {
              tfeat++;
              int i=sq.findIndex(sfpos.getBegin());
              int ist=sq.findIndex(sq.getStart());
              if (i<ist)
              {
                i=ist;
              }
              int j=sq.findIndex(sfpos.getEnd());
              if (j>al.getWidth())
              {
                j = al.getWidth();
              }
              for (; i<=j; i++)
              {
                bs.set(i-1);
              }
            }
          }
        }
        if (tfeat>0) {
            nseq++;
            nfeat+=tfeat;
        }
      }
      if (bs.cardinality()>0)
      {
        nal ++;
        ColumnSelection cs = ala.viewport.getColumnSelection();
        if (cs == null) {
          cs = new ColumnSelection();
        }
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
        cs.addElement(i);
        }
      ala.viewport.setColumnSelection(cs);
      ala.alignPanel.paintAlignment(true);
      ala.statusBar.setText("Marked "+bs.cardinality()+" columns containing features of type "+toselect)
      } else {
        ala.statusBar.setText("No features of type "+toselect+" found.");
      }
    }
}
return "Found a total of ${nfeat} features across ${nseq} sequences in ${nal} alignments.";
    
String getFeatureInput(){
        def swingBuilder = new SwingBuilder();
        def response = JvOptionPane.showInputDialog(
                   null, 'Select columns by feature by type','Enter type of feature', JvOptionPane.OK_OPTION)

        return response
    }