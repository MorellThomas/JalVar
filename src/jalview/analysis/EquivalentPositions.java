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
  
  final private String refFile;  
  
  final private AlignFrame af;
  
  final private AlignmentViewport seqs;
  
  final private int[] startingPosition; //of each gene
  
  final char[] FoR; // Forward or reverse strand for each gene
  
  final int width;  //MSA width ~ max EP
  
  final int sampleSeqs; //number of protein sequences to be analysed
  
  final int refSeqs;  //number of sequences in the loaded gene file
  
  /*
   * index of first protein sequence corresponding to a new gene sequence
   * determined either by 
   *    - only one gene existing -> all proteins belong to that -> geneSequenceMapping = [0]
   *    - names of protein sequences contain the name of the gene they belong to -> custom index mapping
   *    - names don't match, as many genes as proteins -> = [0, 1, 2, ... length-1]
   *    - names don't match, unequal amount -> error
   */
  final int[] geneSequenceMapping; // index of first protein sequence corresponding to a new gene sequence
  
  final private String[] geneSequenceNames;
  
  final private String refSequenceName; //filename of ref file

  /**
   * Constructor given the sequences to compute for, the starting gene position, the strand and the sequence length
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param startingPosition
   * @param FoR
   * @param width
   */
  public EquivalentPositions(AlignFrame sequences, int[] startingPosition, char[] FoR, int width)
  {
    this.af = sequences;
    this.seqs = af.getViewport();
    this.startingPosition = startingPosition;
    this.FoR = FoR;
    this.width = width;
    this.frequenciesonly = startingPosition[0] == -1 ? true : false;
    
    int i = 0;
    boolean geneFound = false;
    for (SequenceI s : seqs.getAlignment().getSequences())
    {
      if (!s.isProtein())
      {
        geneFound = true;
        break;
      }
      i++;
    }
    sampleSeqs = geneFound ? i : seqs.getAlignment().getHeight();
    refSeqs = geneFound ? seqs.getAlignment().getHeight() - i : 0;
    
    geneSequenceNames = new String[refSeqs];
    for (int s = sampleSeqs; s < sampleSeqs+refSeqs; s++)
    {
      geneSequenceNames[s-sampleSeqs] = seqs.getAlignment().getSequenceAt(s).getName();
    }
    
    geneSequenceMapping = createGeneSequenceMapping();

    
    this.refSequenceName = frequenciesonly ?  Long.toString(System.currentTimeMillis()) : EpReferenceFile.constructRefFileName(seqs.getAlignment().getSequencesArray(), sampleSeqs, refSeqs);  //random name if fonly
    this.refFile = String.format("%s%s.ref", EpReferenceFile.REFERENCE_PATH, refSequenceName);
    
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
      if (new File(refFile).exists())
      {
        erf = EpReferenceFile.loadReference(refFile);
        domain = erf.getDomain();
        domainGroups = erf.getDomainGroups();
        alignedDomain = erf.getAlignedDomains();
        domainOffset = erf.getDomainOffset();
      } else {    //else create a new one
        erf = new EpReferenceFile(refFile);
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
      
      String[] geneSequenceNames = new String[4*refSeqs];
      int geneNamesIndex = 0;
      SequenceI base = seqs.getAlignment().getSequenceAt(0);  // changed for !frequenciesonly
      if (!frequenciesonly)
      {
        for (int i = 0; i < refSeqs; i++)
        {
          //creating dna sequence copies of the inputed one with frameshifts by deleting the first n bases
          SequenceI[] _tmp = seqs.getAlignment().getSequencesArray();
          base = _tmp[sampleSeqs + i];
          SequenceI _one = base.deriveSequence();
          _one.deleteChars(0, 1);
          SequenceI _two = base.deriveSequence();
          _two.deleteChars(0, 2);
          SequenceI[] _geneSequence = new SequenceI[]{base, _one, _two};
          AlignmentI _geneAsAlignment = new Alignment(_geneSequence);
          
          geneSequenceNames[geneNamesIndex++] = base.getName();

          AlignViewport geneSequence = new AlignViewport(_geneAsAlignment);
          Dna dna = new Dna(geneSequence, geneSequence.getViewAsVisibleContigs(true));  //create a DNA object of the sequences

          GeneticCodes _gc = GeneticCodes.getInstance();
          GeneticCodeI _standardTranslationTable = _gc.getStandardCodeTable();
          SequenceI[] translatedSequence = dna.translateCdna(_standardTranslationTable).getSequencesArray();  //translate all 3 dnas
          
          for (SequenceI protSeq : translatedSequence)    // add all 3 translations to the AlignmentViewport
          {
            geneSequenceNames[geneNamesIndex++] = protSeq.getName();
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
      }
      
      int currentGeneGroup = 0; // index of geneSequenceMapping
      SequenceI[] sequences = seqs.getAlignment().getSequencesArray();
      int frameOffset = 0;

      //for each sequence in the alignment
      for ( int i = 0; i < sampleSeqs; i++)
      {
        //for saving reference
        LinkedList<HashMap<Character, int[]>> sequencePlusInfoList = new LinkedList<HashMap<Character, int[]>>(); //middle component for keeping the correct order of the AAs
        HashMap<Character, int[]> aaepgpPairs = new HashMap<Character, int[]>();  //inner component of domain map -- pairs AA to its EP and GPs (int[4] = [EP, GP1, GP2, GP3])
          
        // go to next gene group
        if (currentGeneGroup < geneSequenceMapping.length -1 && i == geneSequenceMapping[currentGeneGroup+1])
          currentGeneGroup++;

        if (!frequenciesonly)
        {

          if (Arrays.asList(geneSequenceNames).contains(sequences[i].getName()) )   //not aling the parent sequences with eachother
            continue;

          //get the scores of the alignments and their CBs (3 because all frames)
          float[] alignmentScores = new float[3];
          int[] correspondingBases = new int[3];

          for (int j = 1; j < 4; j++)   
          {
            int currentFrameIndex = sampleSeqs + refSeqs - 1 + (currentGeneGroup * 3) + j;   // index of the current translation frame of the sequence at i
            
            // copying code from gui/PairwiseAlignPanel
            SequenceI[] forAlignment = new SequenceI[2];
            forAlignment[0] = sequences[i];
            forAlignment[1] = sequences[currentFrameIndex];
            
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
            
          frameOffset = (int) MiscMath.findMax(alignmentScores)[0]; //find the best alignement
          int correspondingBase = correspondingBases[frameOffset];
          
          int epCalc = 1;
          for (int ep = 0; ep < width; ep++) // ep is actually real ep; ep for calculation (epCalc) does not increase at gap to not skip a GP by accident
          {
            if (sequences[i].getCharAt(ep) == '-')
              continue;

            int _basePosition;
            int[] currentEPandGPs;
            if (FoR[currentGeneGroup] == 'F')
            {
              _basePosition = startingPosition[currentGeneGroup] + (epCalc + correspondingBase - 2) * 3 + frameOffset;     
              
              currentEPandGPs = new int[]{ep+1, _basePosition, _basePosition + 1, _basePosition + 2};
            } else {
              _basePosition = startingPosition[currentGeneGroup] - (epCalc + correspondingBase - 2) * 3 - frameOffset;
              currentEPandGPs = new int[]{ep+1, _basePosition, _basePosition - 1, _basePosition - 2};
            }
            //output information to reference
            aaepgpPairs = new HashMap<Character, int[]>();
            aaepgpPairs.put(sequences[i].getCharAt(ep), currentEPandGPs);  // gaps will be skipped!!!!!
            sequencePlusInfoList.add(aaepgpPairs);
            
            epCalc++;
          }
          
        } else {    // if frequenciesonly
          for (int ep = 0; ep < width; ep++)
          {
            if (sequences[i].getCharAt(ep) == '-')
              continue;

            aaepgpPairs = new HashMap<Character, int[]>();
            aaepgpPairs.put(sequences[i].getCharAt(ep), new int[]{ep+1});
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
        HashMap<String, char[]> geneSeqs = new HashMap<String, char[]>();
        HashMap<String, int[]> gps = new HashMap<String, int[]>();
        HashMap<String, Boolean> isReverse = new HashMap<String, Boolean>();
        
        for (int i = sampleSeqs; i < sampleSeqs + refSeqs; i++)
        {
          String name = sequences[i].getName();
          geneSeqs.put(name, sequences[i].getSequence());
          
          
          int[] allGenomicPositions = new int[sequences[i].getLength()];
          for (int j = 0; j < allGenomicPositions.length; j++)
          {
            allGenomicPositions[j] = j + startingPosition[i-sampleSeqs];
          }
          gps.put(name, allGenomicPositions);
          isReverse.put(name, FoR[i-sampleSeqs] == 'R');

        }

        erf.setGeneSequence(geneSeqs);          // saves the DNA sequences as char[]
        erf.setGenomicPositions(gps);   // save the array of all genomic positions
        
        erf.setDomainOffset(domainOffset);  // saves domains and their frame offset
        
        erf.setReverse(isReverse);
      }

      erf.setDomain(domain);    // saves the domain as a HashMap
      
      String sep = System.getProperty("os.name").split(" ")[0] == "Windows" ? "\\" : "/";
      String[] file = Desktop.getAlignFrameFor(seqs).getFileName().split(sep);
      domainGroups.put(file[file.length-1].split("\\.")[0], dGroup);
      erf.setDomainGroups(domainGroups);  // saves domain Groups
      
      erf.setAlignedDomains(alignedDomain); // saves domains with gaps as char[]
      
      erf.setGeneSequenceMapping(geneSequenceMapping);
      
      
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
      for (String name : geneSequenceNames)
      {
        if (seq.getName().equals(name))
          seqs.getAlignment().deleteSequence(seq);
      }
    }
    
    /*
     * construct the panel and kick off its custom thread
     */
    NFPanel nfPanel = new NFPanel(af.alignPanel, refFile);
    new Thread(nfPanel).start();
  }
  
  /*
   * index of first protein sequence corresponding to a new gene sequence
   * determined either by 
   *    - only one gene existing -> all proteins belong to that -> geneSequenceMapping = [0]
   *    - names of protein sequences contain the name of the gene they belong to -> custom index mapping
   *    - names don't match, as many genes as proteins -> = [0, 1, 2, ... length-1]
   *    - names don't match, unequal amount -> error
   */
  private int[] createGeneSequenceMapping()
  {
    if (refSeqs == 0)
    {
      return new int[]{0};
    }
    
    LinkedHashSet<Integer> mappingSet = new LinkedHashSet<Integer>();
    LinkedHashSet<String> geneNamesSet = new LinkedHashSet<String>();
    SequenceI[] sequences = seqs.getAlignment().getSequencesArray();
    
    for (int i = sampleSeqs; i < sampleSeqs + refSeqs; i++)
    {
      geneNamesSet.add(sequences[i].getName());  // ignores duplicates
    }
    
    String[] geneNames = new String[geneNamesSet.size()];
    int l = 0;
    for (String name : geneNamesSet)
    {
      geneNames[l++] = name;
    }
    
    int groupNumber = 0;    // number of geneSequence
    geneNamesSet.clear();
    for (int i = 0; i < sampleSeqs; i++)
    {
      if ((groupNumber < geneNames.length) && (sequences[i].getName().contains(geneNames[groupNumber])))
      {
        if ((groupNumber == 0) || (groupNumber != 0 && !geneNamesSet.contains(geneNames[groupNumber])))
        {
          mappingSet.add(i);
          geneNamesSet.add(geneNames[groupNumber]);
          groupNumber++;
        }
      }
    }
    
    if (groupNumber == 0 && sampleSeqs == refSeqs)   // if names dont match, but equal amount
    {
      for ( int i = 0; i < sampleSeqs; i++)
      {
        mappingSet.add(i);
      }
    } else if (groupNumber == 0 && refSeqs == 1) {
      mappingSet.add(0);
    } else if (groupNumber == 0 && sampleSeqs != refSeqs) { // if names dont match and unequal amount
      JvOptionPane.showInternalMessageDialog(Desktop.desktop, "Protein sequences do not fit to gene Sequences!", "Reference Creation Error", JvOptionPane.ERROR_MESSAGE);
      throw new RuntimeException();
    }
    
    int[] mapping = new int[mappingSet.size()];
    l = 0;
    for (Integer n : mappingSet)
    {
      mapping[l++] = n;
    }
    
    //check correctness of name assignment
    for (int i = 0; i < geneNames.length; i++)
    {
      int start = mapping[i];
      int end = i == geneNames.length - 1 ? sampleSeqs : mapping[i+1];
      String name = geneNames[i];
      for (int s = start; s < end; s++)
      {
        if (!sequences[s].getName().contains(name))
        {
          JvOptionPane.showInternalMessageDialog(Desktop.desktop, "Protein sequences do not fit to gene Sequences!", "Reference Creation Error", JvOptionPane.ERROR_MESSAGE);
          throw new RuntimeException();
        }
      }
    }
    
    return mapping;
  }

}
