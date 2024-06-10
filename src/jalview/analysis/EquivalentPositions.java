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
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Alignment;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceGroup;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.NFPanel;
import jalview.io.EpReferenceFile;
import jalview.math.MiscMath;
import jalview.viewmodel.AlignmentViewport;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


/**
 * Performs equivalent position to genomic position conversion
 * !has to be run before analysis and natural frequencies
 * 
 * called by EpPanel
 */
public class EquivalentPositions implements Runnable
{
  /*
   * inputs
   */
  final private boolean frequenciesonly;  // if true, ignore all gene seq and variance analysis. Used for protein family conservation analysis
  
  final private String refDir;
  
  final private AlignFrame af;
  
  final private AlignmentViewport seqs;
  
  final private int startingPosition; //of the gene
  
  final char FoR;
  
  final int width;
  
  final private String refSequenceName;

  /**
   * Constructor given the sequences to compute for, the starting gene position, the strand and the sequence length
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param startingPosition
   * @param FoR
   * @param width
   */
  public EquivalentPositions(AlignFrame sequences, int startingPosition, char FoR, int width)
  {
    this.af = sequences;
    this.seqs = af.getViewport();
    this.startingPosition = startingPosition;
    this.FoR = FoR;
    this.width = width;
    this.frequenciesonly = startingPosition == -1 ? true : false;
    
    SequenceI[] _tmp = seqs.getAlignment().getSequencesArray();
    this.refSequenceName = frequenciesonly ?  Long.toString(System.currentTimeMillis()) : _tmp[_tmp.length - 1].getName();  //random name if fonly
    this.refDir = String.format("%s%s.ref", EpReferenceFile.REFERENCE_PATH, refSequenceName);
  }

  /**
   * Performs the Equivalent Positions calculation
   *
   */
  @Override
  public void run()
  {
    try
    {
      EpReferenceFile erf;

      // create the domain hash set for storage
      HashMap<String, LinkedList<HashMap<Character, int[]>>> domain;
      HashMap<String, LinkedHashSet<String>> domainGroups;
      HashMap<String, char[]> alignedDomain;
      HashMap<String, Integer> domainOffset;

      //checking if reference file already exits
      //!TODO #################
      if (new File(refDir).exists())
      {
        erf = EpReferenceFile.loadReference(refDir);
        domain = erf.getDomain();
        domainGroups = erf.getDomainGroups();
        alignedDomain = erf.getAlignedDomains();
        domainOffset = erf.getDomainOffset();
      } else {    //else create a new one
        erf = new EpReferenceFile(refDir);
        domain = new HashMap<String,LinkedList<HashMap<Character, int[]>>>();
        domainGroups = new HashMap<String, LinkedHashSet<String>>();
        alignedDomain = new HashMap<String, char[]>();
        domainOffset = new HashMap<String, Integer>();
      }
      
      // create set of sequences in this MSA group for storage
      // or take the already existing one
      LinkedHashSet<String> dGroup = new LinkedHashSet<String>();
      for (String dG : domainGroups.keySet())
      {
        AlignFrame afr = Desktop.getAlignFrameFor(seqs);
        if (dG == afr.getName())
          dGroup = domainGroups.get(dG);
      }
      
      String[] geneSequenceNames = new String[4];
      SequenceI base = seqs.getAlignment().getSequenceAt(0);  // changed for !frequenciesonly
      if (!frequenciesonly)
      {
        //creating dna sequence copies of the inputed one with frameshifts by deleting the first n bases
        SequenceI[] _tmp = seqs.getAlignment().getSequencesArray();
        base = _tmp[_tmp.length - 1];
        SequenceI _one = base.deriveSequence();
        _one.deleteChars(0, 1);
        SequenceI _two = base.deriveSequence();
        _two.deleteChars(0, 2);
        SequenceI[] _geneSequence = new SequenceI[]{base, _one, _two};
        AlignmentI _geneAsAlignment = new Alignment(_geneSequence);
        
        geneSequenceNames[0] = base.getName();

        AlignViewport geneSequence = new AlignViewport(_geneAsAlignment);
        Dna dna = new Dna(geneSequence, geneSequence.getViewAsVisibleContigs(true));  //create a DNA object of the sequences

        GeneticCodes _gc = GeneticCodes.getInstance();
        GeneticCodeI _standardTranslationTable = _gc.getStandardCodeTable();
        SequenceI[] translatedSequence = dna.translateCdna(_standardTranslationTable).getSequencesArray();  //translate all 3 dnas
        
        int _i = 1;
        for (SequenceI protSeq : translatedSequence)    // add all 3 translations to the AlignmentViewport
        {
          geneSequenceNames[_i] = protSeq.getName();
          seqs.getAlignment().addSequence(protSeq);
        }
        
        //cleanup
        _standardTranslationTable = null;
        _gc = null;
        _one = null;
        _two = null;
        _tmp = null;
        _geneSequence = null;
        _geneAsAlignment = null;
      }
      
      List<SequenceI> sequencesList = seqs.getAlignment().getSequences();   // needed for checking if there is a gap 

      //for each sequence in the alignment
      int skip = frequenciesonly ? 0 : 1;
      for ( int i = 0; i < seqs.getAlignment().getSequencesArray().length - skip; i++)
      {
        SequenceI[] sequences = seqs.getAlignment().getSequencesArray();
        int frameOffset = 0;

        //for saving reference
        LinkedList<HashMap<Character, int[]>> sequencePlusInfoList = new LinkedList<HashMap<Character, int[]>>(); //middle component for keeping the correct order of the AAs
        HashMap<Character, int[]> aaepgpPairs = new HashMap<Character, int[]>();  //inner component of domain map -- pairs AA to its EP and GPs (int[4] = [EP, GP1, GP2, GP3])
          
        if (!frequenciesonly)
        {

          if (Arrays.asList(geneSequenceNames).contains(sequences[i].getName()) )   //not aling the parent sequences with eachother
            continue;

          //get the scores of the alignments and their CBs (3 because all frames)
          float[] alignmentScores = new float[3];
          int[] correspondingBases = new int[3];

          for (int j = 1; j < 4; j++)   //!!! looping in reverse order
          {
            SequenceGroup sg = new SequenceGroup();
            sg.addSequence(sequences[i], false);
            sg.addSequence(sequences[sequences.length - j], false);
            
            seqs.setSelectionGroup(sg);   // select one sample and one reference 
            
            // copying code from gui/PairwiseAlignPanel
            SequenceI[] forAlignment = new SequenceI[2];
            forAlignment[0] = sequences[i];
            forAlignment[1] = sequences[sequences.length-j];
            
            String[] seqStrings = new String[2];
            seqStrings[0] = forAlignment[0].getSequenceAsString();
            seqStrings[1] = forAlignment[1].getSequenceAsString();

            AlignSeq as = new AlignSeq(forAlignment[0], seqStrings[0], forAlignment[1], seqStrings[1], AlignSeq.PEP); // align the 2 sequences
            as.calcScoreMatrix();
            as.traceAlignment();
            as.scoreAlignment();
            as.printAlignment(System.out);
            correspondingBases[j-1] = as.getSeq2Start();
            
            alignmentScores[j-1] = as.getAlignmentScore();
          }
            
          int bestFrame = (int) MiscMath.findMax(alignmentScores)[0]; //find the best alignement
          int correspondingBase = correspondingBases[bestFrame];
          frameOffset = 2 - bestFrame;  // reverse order
          
          LinkedHashSet<Integer> genomicCorrespondingPositions = new LinkedHashSet<Integer>(sequences[i].getLength()*3);  // no duplicates
          
          //a gap position will have an increasingly negative number as its genomic position (no duplicates)
          int gapmarker = -1;

          int epCalc = 1;
          for (int ep = 0; ep < width; ep++) // ep is actually real ep; ep for calculation (epCalc) does not increase at gap to not skip a GP by accident
          {
            if (sequencesList.get(i).getCharAt(ep) == '-')
            {
              genomicCorrespondingPositions.add(gapmarker--);
              genomicCorrespondingPositions.add(gapmarker--);
              genomicCorrespondingPositions.add(gapmarker--);
              continue;
            }

            int _basePosition;
            int[] currentEPandGPs;
            if (FoR == 'F')
            {
              _basePosition = startingPosition + (epCalc + correspondingBase - 2) * 3 + frameOffset;     
              genomicCorrespondingPositions.add(_basePosition);
              genomicCorrespondingPositions.add(_basePosition + 1);
              genomicCorrespondingPositions.add(_basePosition + 2);
              
              currentEPandGPs = new int[]{ep+1, _basePosition, _basePosition + 1, _basePosition + 2};
            } else {
              _basePosition = startingPosition - (epCalc + correspondingBase - 2) * 3 - frameOffset;
              genomicCorrespondingPositions.add(_basePosition);
              genomicCorrespondingPositions.add(_basePosition - 1);
              genomicCorrespondingPositions.add(_basePosition - 2);

              currentEPandGPs = new int[]{ep+1, _basePosition, _basePosition - 1, _basePosition - 2};
            }
            //output information to reference
            aaepgpPairs = new HashMap<Character, int[]>();
            aaepgpPairs.put(sequencesList.get(i).getCharAt(ep), currentEPandGPs);  // gaps will be skipped!!!!!
            sequencePlusInfoList.add(aaepgpPairs);
            
            epCalc++;
          }
          
        } else {    // if frequenciesonly
          for (int ep = 0; ep < width; ep++)
          {
            if (sequencesList.get(i).getCharAt(ep) == '-')
              continue;

            aaepgpPairs = new HashMap<Character, int[]>();
            aaepgpPairs.put(sequencesList.get(i).getCharAt(ep), new int[]{ep+1});
            sequencePlusInfoList.add(aaepgpPairs);
          }
        }
        dGroup.add(sequences[i].getName());
        domainOffset.putIfAbsent(sequences[i].getName(), frameOffset);
        domain.putIfAbsent(sequences[i].getName(), sequencePlusInfoList);
        alignedDomain.putIfAbsent(sequences[i].getName(), sequences[i].getSequence());
      }
      
      // start preparing the reference
      if (!frequenciesonly)
      {
        erf.setGeneSequence(base.getSequence());          // saves the DNA sequences as char[]
        
        int[] allGenomicPositions = new int[base.getLength()];
        for (int i = 0; i < allGenomicPositions.length; i++)
        {
          allGenomicPositions[i] = i + startingPosition;
        }
        erf.setGenomicPositions(allGenomicPositions);   // save the array of all genomic positions
        
        erf.setDomainOffset(domainOffset);  // saves domains and their frame offset
        
        boolean isReverse = FoR == 'F' ? false : true;
        erf.setReverse(isReverse);
      }

      erf.setDomain(domain);    // saves the domain as a HashMap
      
      String sep = System.getProperty("os.name").split(" ")[0] == "Windows" ? "\\" : "/";
      String[] file = Desktop.getAlignFrameFor(seqs).getFileName().split(sep);
      domainGroups.put(file[file.length-1].split("\\.")[0], dGroup);
      erf.setDomainGroups(domainGroups);  // saves domain Groups
      
      erf.setAlignedDomains(alignedDomain); // saves domains with gaps as char[]
      
      
      //save the reference
      erf.saveReference();
      
      // run natural Frequencies
      runNf();
      
      JvOptionPane.showInternalMessageDialog(Desktop.desktop, "Finished creating the Reference", "Reference Finished", JvOptionPane.INFORMATION_MESSAGE);
      
    } catch (Exception q)
    {
      Console.error("Error computing Equivalent Positions:  " + q.getMessage());
      q.printStackTrace();
    }
  }
  
  /**
   * performs the Natural Frequencies Analysis
   */
  private void runNf()
  {
    for (SequenceI seq : seqs.getAlignment().getSequencesArray())
    {
      if (seq.getName().equals(refSequenceName))
        seqs.getAlignment().deleteSequence(seq);
    }
    
    /*
     * construct the panel and kick off its custom thread
     */
    NFPanel nfPanel = new NFPanel(af.alignPanel);
    new Thread(nfPanel).start();
  }

}
