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
package jalview.analysis;

import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation; // not for pasimap
import jalview.datamodel.AlignmentI; // not for pasimap
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SequenceI;
import jalview.datamodel.ResidueCount; // not for pasimap
import jalview.gui.CutAndPasteTransfer;
import jalview.gui.Desktop;
import jalview.gui.OOMWarning;
import jalview.math.MiscMath;  // not for pasimap
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;


/**
 * Performs Principal Component Analysis on given sequences
 * @AUTHOR MorellThomas 
 */
public class NaturalFrequencies implements Runnable
{
  /*
   * inputs
   */
  final private AlignmentViewport seqs;

  /*
   * outputs
   */
  private String CsvString;

  /**
   * Constructor given the sequences to compute for, the similarity model to
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param sm
   * @param options
   */
  public NaturalFrequencies(AlignmentViewport sequences)
  {
    this.seqs = sequences;
  }

  /**
   * Performs the Natural Frequencies calculation
   *
   */
  @Override
  public void run()
  {
    try
    {

      AlignmentAnnotation consensus = seqs.getAlignmentConsensusAnnotation();
      System.out.println(consensus.toString());
      //for (Annotation an : consensus.annotations)
      //{
        //System.out.println(an.toString());
      //}
      
      AlignmentI al = seqs.getAlignment();
      int width = al.getWidth();
      SequenceI[] aseqs = al.getSequencesArray();
      ProfilesI hconsensus = AAFrequency.calculate(aseqs, width, 0, width, true);
      
      ResidueCount[] residueCountses = new ResidueCount[hconsensus.getEndColumn() + 1];
      for (int i = 0; i < hconsensus.getEndColumn() + 1; i++)
      {
        residueCountses[i] = hconsensus.get(i).getCounts();
      }
      
      char[] AaList = new char[]{'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', '-'};
      StringBuffer csv = new StringBuffer();
      csv.append("\"Position\"");
      for (char Aa : AaList)
      {
        csv.append("," + Aa);
      }
      csv.append("\n");
      int nSeqs = aseqs.length;
      int position = 1;
      for (ResidueCount rc : residueCountses)
      {
        csv.append(Integer.toString(position));
        
        float[] percentages = new float[AaList.length];

        ResidueCount.SymbolCounts symbolCounts = rc.getSymbolCounts();
        float gapPC = 1f;
        for (int i = 0; i < symbolCounts.symbols.length; i++)
        {
          char AA = symbolCounts.symbols[i];  
          int n = symbolCounts.values[i];     // number of times the AA appears at this position
          float pc = (float) n / (float) nSeqs;               // % of AA appearance at this position
          pc = MiscMath.round(pc, 4);
          gapPC -= pc;
          
          int indexOfAa = new String(AaList).indexOf(AA);
          percentages[indexOfAa] = pc;
        }
        if (gapPC > 0)
        {
          gapPC = MiscMath.round(gapPC, 4);
          percentages[percentages.length - 1] = gapPC;
        }
        for (float pc : percentages)
        {
          csv.append(",").append(pc);
        }
        csv.append("\n");
        position++;
      }
      
      CsvString = csv.toString();
      
      CutAndPasteTransfer cap = new CutAndPasteTransfer();
      try
      {
        cap.setText(CsvString);
        Desktop.addInternalFrame(cap, MessageManager
                .formatMessage("label.points_for_params", "Outputting Natural Frequencies"), 500, 500);
      } catch (OutOfMemoryError oom)
      {
        new OOMWarning("exporting Natural Frequencies", oom);
        cap.dispose();
      }

    } catch (Exception q)
    {
      Console.error("Error computing Natural Frequencies:  " + q.getMessage());
      q.printStackTrace();
    }
  }

}
