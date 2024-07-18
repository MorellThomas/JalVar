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
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.io.EpReferenceFile;
import jalview.math.MiscMath;  // not for pasimap
import jalview.viewmodel.AlignmentViewport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * Performs Natural Frequencies calculation
 * !has to be run after the Equivalent Positions
 * 
 * called by NfPanel
 */
public class NaturalFrequencies implements Runnable
{
  /*
   * inputs
   */
  final private AlignmentViewport seqs;
  
  final private String refFile;

  /*
   * outputs
   */
  private HashMap<String[], LinkedList<HashMap<Character, Float>>> naturalFrequency;

  /**
   * Constructor given the sequences
   * 
   * @param sequences
   */
  public NaturalFrequencies(AlignmentViewport sequences, String referenceFile)
  {
    this.seqs = sequences;
    this.refFile = referenceFile;
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
      AlignmentI al = seqs.getAlignment();
      int width = al.getWidth();

      //getting the consensus information from the alignment aka the counts of each AA
      AlignmentAnnotation consensus = seqs.getAlignmentConsensusAnnotation();
      System.out.println(consensus.toString());
      
      SequenceI[] aseqs = al.getSequencesArray();
      ProfilesI hconsensus = AAFrequency.calculate(aseqs, width, 0, width, true);   // calculating the consensus data
      
      // throws an error if nothing was found
      //EpReferenceFile erf = EpReferenceFile.loadReference(String.format("%s%s", EpReferenceFile.REFERENCE_PATH, refFile)); 
      EpReferenceFile erf = EpReferenceFile.loadReference(refFile); 
      naturalFrequency = erf.getNaturalFrequency(); // load reference
      
      String[] sequenceNames = Arrays.copyOf(al.getSequenceNames().toArray(), al.getHeight(), String[].class);  // for saving as reference
      
      if (naturalFrequency ==  null)
        naturalFrequency = new HashMap<String[], LinkedList<HashMap<Character, Float>>>();  // create a new map if empty
      
      LinkedList<HashMap<Character, Float>> listofPairs = new LinkedList<HashMap<Character, Float>>(); //for saving referefence
      HashMap<Character, Float> aapcPairs;  // for saving as reference
      
      //get all the counts of all AAs of each position from the consensus information
      ResidueCount[] residueCountses = new ResidueCount[hconsensus.getEndColumn() + 1];
      for (int i = 0; i < hconsensus.getEndColumn() + 1; i++)
      {
        residueCountses[i] = hconsensus.get(i).getCounts();
      }
      
      char[] AaList = new char[]{'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', '-'};
      int nSeqs = aseqs.length;
      
      //calculating the %
      for (ResidueCount rc : residueCountses)
      {
        aapcPairs = new HashMap<Character, Float>();
        
        float[] percentages = new float[AaList.length];

        ResidueCount.SymbolCounts symbolCounts = rc.getSymbolCounts();
        int gapCnt = nSeqs;
        for (int i = 0; i < symbolCounts.symbols.length; i++)
        {
          char AA = symbolCounts.symbols[i];  
          int n = symbolCounts.values[i];     // number of times the AA appears at this position
          float pc = (float) n / (float) nSeqs;               // % of AA appearance at this position
          pc = MiscMath.round(pc * 100, 4);
          gapCnt -= n;
          
          int indexOfAa = new String(AaList).indexOf(AA);
          percentages[indexOfAa] = pc;
          
          aapcPairs.put(AA, pc);
        }
        if (gapCnt > 0)  // % of gaps
        {
          float gapPC = MiscMath.round(((float) gapCnt / (float) nSeqs) * 100, 4);
          percentages[percentages.length - 1] = gapPC;
          aapcPairs.put('-', gapPC);
        }
        listofPairs.add(aapcPairs);
      }
      
      // save data to the corresponding ref file if its existing
      naturalFrequency.putIfAbsent(sequenceNames, listofPairs); 
    
      erf.setNaturalFrequency(naturalFrequency);
      erf.saveReference();
      
      //test
      HashMap<String[], LinkedList<HashMap<Character, Float>>> nf = erf.getNaturalFrequency();
      String[] key = (String[]) nf.keySet().toArray()[0];
      if (Arrays.asList(key).contains("I65"))
      {
        LinkedList<HashMap<Character, Float>> nfs = nf.get(key);
        System.out.println(nfs.get(7).get('P'));
      }

    } catch (Exception q)
    {
      Console.error("Error computing Natural Frequencies:  " + q.getMessage());
      q.printStackTrace();
    }
  }

}
