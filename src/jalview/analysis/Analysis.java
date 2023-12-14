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
import jalview.datamodel.SequenceI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.gui.CutAndPasteTransfer;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.OOMWarning;
import jalview.io.EpReferenceFile;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Performs residue analysis
 * !only works if there is only the whole protein sequence present in the AlignmentViewport
 * !protein sequence, and gene sequence (thus also the reference file) all have to have the same name
 * 
 * called by AnalysisPanel
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
   * Constructor given the sequences to compute for and the residue position (base 1)
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param res
   */
  public Analysis(AlignmentViewport sequences, int res)
  {
    this.seqs = sequences;
    this.residue = res - 1;   // convert base 1 input to internal base 0

    this.protSeqName = seqs.getAlignment().getSequencesArray()[0].getName();    // get the name of the inputted protein Sequence -> to know the name of the reference file and for using the name in the output textbox
    this.refFile = String.format("%s.ref", protSeqName);
  }

  /**
   * Performs the residue analysis
   *
   */
  @Override
  public void run()
  {
    try
    {
    
      //create the output StringBuffer and add the heading line
      StringBuffer csv = new StringBuffer();
      csv.append(String.format("Information on the domains found at position %d\n\n", residue + 1));
      
      //check if the reference file exists
      if (!(new File(refFile).exists()))
      {
        //throws a warning dialog saying that no file with the name {refFile} was found
        JvOptionPane.showInternalMessageDialog(Desktop.desktop, String.format("No reference file \"%s\" found. Aborting.", refFile), "No Reference Error", JvOptionPane.WARNING_MESSAGE);
        throw new RuntimeException();
      }
      
      //load the reference
      EpReferenceFile erf = EpReferenceFile.loadReference(refFile);
      
      //load the EP-GP HashMap
      HashMap<String, LinkedList<HashMap<Character, int[]>>> domain = erf.getDomain();

      for (String domainName : domain.keySet())   // adding all domains from reference to AlignmentViewport
      {
        LinkedList<HashMap<Character, int[]>> aaList = domain.get(domainName);
        char[] aas = new char[aaList.size()];   // domain sequence
        for (int i = 0; i < aaList.size(); i++)
        {
          HashMap<Character, int[]> aaepPair = aaList.get(i); 
          char c = (char) aaepPair.keySet().toArray()[0]; 
          aas[i] = c;
        }
        seqs.getAlignment().addSequence(new Sequence(domainName, aas, 1, aas.length));  // adding domain to AlignmentViewport seqs
      }
      
      AlignSeq[] alignments = new AlignSeq[domain.size()];  // list of all pairwise alignments of domain to input prot seq
      // do the alignment
      for ( int i = 1; i < seqs.getAlignment().getSequencesArray().length; i++)
      {
      
        SequenceI[] sequences = seqs.getAlignment().getSequencesArray();  // SequenceI array having the input prot seq at 0
        
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
        
        alignments[i-1] = as; // save the alignment for later
      }
      
      HashSet<String> foundDomains = new HashSet<String>();   // holding all domain (names) that were found at residue (should be only 1)
      HashSet<Integer> alignmentNr = new HashSet<Integer>();  // holding the indexes of the pairwise alignment of the domain with the prot seq in alignments
      int k = 0;
      for (AlignSeq al : alignments)  // finding the domains which align at the specified position
      {
        if ((al.getSeq1Start() < residue) && (al.getSeq1End() > residue)) //if the residue is inside the alignment region
        {
          int posInDomain = al.getSeq2Start() + (residue - al.getSeq1Start()); 
          if(al.astr2.charAt(posInDomain) != '-')     //skip gaps
          {
            System.out.println(al.s2.getName());
            foundDomains.add(al.s2.getName());
            alignmentNr.add(k);    // add the alignment to the list (should only be 1)
          }
        }
        k++;
      }
      int[] alignmentNrs = new int[alignmentNr.size()];   //holds the index(es) of the sequence(s) in the AlignmentViewport that successfully aligned in the specified position
      k = 0;
      for (int anr : alignmentNr)   // converting the HashSet of indexes to an array
      {
        alignmentNrs[k++] = anr;
      }
        
      alignmentNr = null;
      
      // get the natural frequency map from the reference file
      HashMap<String[], LinkedList<HashMap<Character, Float>>> nF = erf.getNaturalFrequency();

      k = 0;
      for (String domainName : foundDomains)  // doing the analysis and outputting to csv
      {
        int alignmentIndex = alignmentNrs[k]; // index of pairwise alignment of domainName with prot seq
        
        LinkedList<HashMap<Character, int[]>> aaList = domain.get(domainName);  // in AA sequence order lists AA -> [EP, GP1, GP2, GP3]
        char[] aas = new char[aaList.size()];   // AA sequence of the domain  (has to convert the keys of each map in the list into one array
        for (int i = 0; i < aaList.size(); i++)
        {
          HashMap<Character, int[]> aaepPair = aaList.get(i);
          char c = (char) aaepPair.keySet().toArray()[0]; 
          aas[i] = c;
        }
        //create and format the outputs
        csv.append(String.format("Domain: %s\t(EPs %d - %d; GPs %d - %d)\n", domainName, aaList.peekFirst().get(aas[0])[0], aaList.peekLast().get(aas[aas.length-1])[0], aaList.peekFirst().get(aas[0])[1], aaList.peekLast().get(aas[aas.length-1])[3]));
        
        csv.append(String.format("aligns to %s from %d - %d\n", protSeqName, alignments[alignmentIndex].getSeq1Start(), alignments[alignmentIndex].getSeq1End()));
        
        int posInDomain = alignments[alignmentIndex].getSeq2Start() + (residue - alignments[alignmentIndex].getSeq1Start());  // residue number in the domain without gaps !! not the EP
        System.out.println(String.format("%d = %d + (%d - %d)",posInDomain, alignments[alignmentIndex].getSeq2Start(), residue, alignments[alignmentIndex].getSeq1Start()));
        HashMap<Character, int[]> position = aaList.get(posInDomain);
        char aaAtPos = (char) position.keySet().toArray()[0];
        int[] epgp = position.get(aaAtPos);   //is the real ep + gps at the specified position

        csv.append(String.format("%s residue %d (%c): %s (%s) EP %d, GPs [%d, %d, %d]\n", protSeqName, residue + 1, seqs.getAlignment().getSequencesArray()[0].getCharAt(residue), domainName, aaAtPos, epgp[0], epgp[1], epgp[2], epgp[3]));
        
        //natural frequencies
        LinkedList<HashMap<Character, Float>> nfList = new LinkedList<HashMap<Character, Float>>();   // need to initialize because else error
        for (String[] listofdomains : nF.keySet())
        {
          if (Arrays.asList(listofdomains).contains(domainName))  // has to be true one time
            nfList = nF.get(listofdomains);
        } 
        
        //output the nf header and information
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
