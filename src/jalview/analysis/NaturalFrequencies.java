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
import jalview.io.EpReferenceFile;
import jalview.math.MiscMath;  // not for pasimap
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;

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
  
  private HashMap<String[], LinkedList<HashMap<Character, Float>>> naturalFrequency;

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
      AlignmentI al = seqs.getAlignment();
      int width = al.getWidth();

      AlignmentAnnotation consensus = seqs.getAlignmentConsensusAnnotation();
      System.out.println(consensus.toString());
      //for (Annotation an : consensus.annotations)
      //{
        //System.out.println(an.toString());
      //}
      
      SequenceI[] aseqs = al.getSequencesArray();
      ProfilesI hconsensus = AAFrequency.calculate(aseqs, width, 0, width, true);   // holding the consensus data
      
      File[] files = new File("temp/").listFiles();
      String referenceFile = "";
      for (File file : files) // look for correct sequence file
      {
        if (file.getName().contains(".ref"))
        {
          EpReferenceFile erf = EpReferenceFile.loadReference(String.format("temp/%s", file.getName()));
          HashMap<String, LinkedList<HashMap<Character, int[]>>> domain = erf.getDomain();
          boolean skip = false;
          for (SequenceI seq : aseqs)
          {
            if (!domain.containsKey(seq.getName()))
              skip = true;
          }
          if (!skip)
            referenceFile = file.getName();
        }
      }
      
      // throws an error if nothing was found
      EpReferenceFile erf = EpReferenceFile.loadReference(String.format("temp/%s", referenceFile)); 
      naturalFrequency = erf.getNaturalFrequency(); // load reference
      
      String[] sequenceNames = Arrays.copyOf(al.getSequenceNames().toArray(), al.getHeight(), String[].class);  // for saving as reference
      
      if (naturalFrequency ==  null)
        naturalFrequency = new HashMap<String[], LinkedList<HashMap<Character, Float>>>();  // create a new map if empty
      
      LinkedList<HashMap<Character, Float>> listofPairs = new LinkedList<HashMap<Character, Float>>(); //for saving referefence
      HashMap<Character, Float> aapcPairs;  // for saving as reference
      
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
      //int position = 0;
      
      //SequenceI label = seqs.getAlignment().findName("I65");

      //calculating the %
      for (ResidueCount rc : residueCountses)
      {
        aapcPairs = new HashMap<Character, Float>();
        csv.append(Integer.toString(position));
        //csv.append(label.getCharAt(position));
        
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
          
          aapcPairs.put(AA, pc);
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
        listofPairs.add(aapcPairs);
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
