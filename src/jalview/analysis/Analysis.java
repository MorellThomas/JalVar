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
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
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
import java.util.HashSet;

/**
 * Performs Principal Component Analysis on given sequences
 * @AUTHOR MorellThomas 
 */
public class Analysis implements Runnable
{
  /*
   * inputs
   */
  final private AlignmentViewport seqs;
  
  final private int residue;
  
  final private String refFile;
  
  final private String protSeqName;

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
  public Analysis(AlignmentViewport sequences, int res)
  {
    this.seqs = sequences;
    this.residue = res;

    this.protSeqName = seqs.getAlignment().getSequencesArray()[0].getName();
    this.refFile = String.format("temp/%s.ref", protSeqName);
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
    
      StringBuffer csv = new StringBuffer();
      csv.append(String.format("Information on the domains found at position %d\n\n", residue));
      
      if (!(new File(refFile).exists()))
      {
        // TODO dialog saying oops
        System.out.println(String.format("File %s not found", refFile));
        throw new RuntimeException();
      }
      
      EpReferenceFile erf = EpReferenceFile.loadReference(refFile);
      
      HashMap<String, LinkedList<HashMap<Character, int[]>>> domain = erf.getDomain();

      for (String domainName : domain.keySet())   // adding all domains from reference to AlignmentViewport
      {
        LinkedList<HashMap<Character, int[]>> aaList = domain.get(domainName);
        char[] aas = new char[aaList.size()];
        for (int i = 0; i < aaList.size(); i++)
        {
          HashMap<Character, int[]> aaepPair = aaList.get(i);
          char c = (char) aaepPair.keySet().toArray()[0]; 
          aas[i] = c;
        }
        seqs.getAlignment().addSequence(new Sequence(domainName, aas, 1, aas.length));
      }
      
      AlignSeq[] alignments = new AlignSeq[domain.size()];
      // do the alignment
      for ( int i = 1; i < seqs.getAlignment().getSequencesArray().length; i++)
      {
      
        SequenceI[] sequences = seqs.getAlignment().getSequencesArray();
        
        SequenceGroup sg = new SequenceGroup();
        sg.addSequence(sequences[0], false);
        sg.addSequence(sequences[i], false);
        
        seqs.setSelectionGroup(sg);   // select one sample and one reference 
        
        // copying code from gui/PairwiseAlignPanel
        SequenceI[] forAlignment = new SequenceI[2];
        forAlignment[0] = sequences[0];
        forAlignment[1] = sequences[i];
        
        String[] seqStrings = new String[2];
        seqStrings[0] = forAlignment[0].getSequenceAsString();
        seqStrings[1] = forAlignment[1].getSequenceAsString();

        AlignSeq as = new AlignSeq(forAlignment[0], seqStrings[0], forAlignment[1], seqStrings[1], AlignSeq.PEP); // align the 2 sequences
        as.calcScoreMatrix();
        as.traceAlignment();
        as.scoreAlignment();
        as.printAlignment(System.out);
        
        alignments[i-1] = as;
      }
      
      HashSet<String> foundDomains = new HashSet<String>();
      HashSet<Integer> alignmentNr = new HashSet<Integer>();
      int k = 0;
      for (AlignSeq al : alignments)  // finding the domains which align at the specified position
      {
        if ((al.getSeq1Start() < residue) && (al.getSeq1End() > residue))
        {
          int posInDomain = al.getSeq2Start() + (residue - al.getSeq1Start());
          if(al.astr2.charAt(posInDomain) != '-')
          {
            System.out.println(al.s2.getName());
            foundDomains.add(al.s2.getName());
            alignmentNr.add(k);
          }
        }
        k++;
      }
      int[] alignmentNrs = new int[alignmentNr.size()];
      k = 0;
      for (int anr : alignmentNr)
      {
        alignmentNrs[k++] = anr;
      }
        
      alignmentNr = null;
      
      HashMap<String[], LinkedList<HashMap<Character, Float>>> nF = erf.getNaturalFrequency();

      k = 0;
      for (String domainName : foundDomains)  // doing the analysis and outputting to csv
      {
        int alignmentIndex = alignmentNrs[k];
        
        LinkedList<HashMap<Character, int[]>> aaList = domain.get(domainName);
        char[] aas = new char[aaList.size()];
        for (int i = 0; i < aaList.size(); i++)
        {
          HashMap<Character, int[]> aaepPair = aaList.get(i);
          char c = (char) aaepPair.keySet().toArray()[0]; 
          aas[i] = c;
        }
        csv.append(String.format("Domain: %s\t(EPs %d - %d; GPs %d - %d)\n", domainName, aaList.peekFirst().get(aas[0])[0], aaList.peekLast().get(aas[aas.length-1])[0], aaList.peekFirst().get(aas[0])[1], aaList.peekLast().get(aas[aas.length-1])[3]));
        
        csv.append(String.format("aligns to %s from %d - %d\n", protSeqName, alignments[alignmentIndex].getSeq1Start(), alignments[alignmentIndex].getSeq1End()));
        
        int posInDomain = alignments[alignmentIndex].getSeq2Start() + (residue - alignments[alignmentIndex].getSeq1Start());
        System.out.println(String.format("%d = %d + (%d - %d)",posInDomain, alignments[alignmentIndex].getSeq2Start(), residue, alignments[alignmentIndex].getSeq1Start()));
        HashMap<Character, int[]> position = aaList.get(posInDomain);
        char aaAtPos = (char) position.keySet().toArray()[0];
        int[] epgp = position.get(aaAtPos);

        csv.append(String.format("%s residue %d (%c): %s (%s) EP %d, GPs [%d, %d, %d]\n", protSeqName, residue, seqs.getAlignment().getSequencesArray()[0].getCharAt(residue), domainName, aaAtPos, epgp[0], epgp[1], epgp[2], epgp[3]));
        
        //natural frequencies
        LinkedList<HashMap<Character, Float>> nfList = new LinkedList<HashMap<Character, Float>>();   // need to initialise because else error
        for (String[] listofdomains : nF.keySet())
        {
          if (Arrays.asList(listofdomains).contains(domainName))
            nfList = nF.get(listofdomains);
        } 
        
        csv.append("Natural frequencies at this position:\n");
        HashMap<Character, Float> nfAtThisPosition = nfList.get(epgp[0]-1);
        for (char aa : nfAtThisPosition.keySet())
        {
          csv.append(String.format("%c: %1.2f, ", aa, nfAtThisPosition.get(aa)));
        }
        
        csv.append("\n---------\n");
        
        k++;
      }
      
      
      // wrap up result
      CsvString = csv.toString();
      
      CutAndPasteTransfer cap = new CutAndPasteTransfer();
      try
      {
        cap.setText(CsvString);
        Desktop.addInternalFrame(cap, MessageManager
                .formatMessage("label.points_for_params", "Outputting Analysis results"), 500, 500);
      } catch (OutOfMemoryError oom)
      {
        new OOMWarning("exporting Analysis results", oom);
        cap.dispose();
      }
      
      // clean up the AlignmentViewport by removing all added sequences again
      for (SequenceI seq : seqs.getAlignment().getSequencesArray())
      {
        if (seq.getName() != protSeqName)
          seqs.getAlignment().deleteSequence(seq);
      }
      
    } catch (Exception q)
    {
      Console.error("Error analysing:  " + q.getMessage());
      q.printStackTrace();
    }
  }

}
