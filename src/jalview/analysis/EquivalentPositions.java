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
import jalview.gui.AlignViewport;
import jalview.gui.CutAndPasteTransfer;
import jalview.gui.Desktop;
import jalview.gui.OOMWarning;
import jalview.io.EpReferenceFile;
import jalview.math.MiscMath;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


/**
 * Performs Principal Component Analysis on given sequences
 * @AUTHOR MorellThomas 
 */
public class EquivalentPositions implements Runnable
{
  /*
   * inputs
   */
  
  final private String refDir;
  
  final private AlignmentViewport seqs;
  
  final private int startingPosition;
  
  final char FoR;
  
  final int width;

  /*
   * outputs
   */
  private int correspondingBase;
  
  /**
   * Constructor given the sequences to compute for, the similarity model to
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param sm
   * @param options
   */
  public EquivalentPositions(AlignmentViewport sequences, int startingPosition, char FoR, int width)
  {
    this.seqs = sequences;
    this.startingPosition = startingPosition;
    this.FoR = FoR;
    this.width = width;
    
    SequenceI[] _tmp = seqs.getAlignment().getSequencesArray();
    this.refDir = String.format("temp/%s.ref", _tmp[_tmp.length - 1].getName());
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
      //checking if reference file already exits
      EpReferenceFile erf;

      // create the domain hash set for storage
      HashMap<String, LinkedList<HashMap<Character, int[]>>> domain;

      if (new File(refDir).exists())
      {
        erf = EpReferenceFile.loadReference(refDir);
        domain = erf.getDomain();
      } else {
        erf = new EpReferenceFile(refDir);
        domain = new HashMap<String,LinkedList<HashMap<Character, int[]>>>();
      }
      
      //creating dna sequence copies of the inputed one with frameshifts by deleting the first n bases
      SequenceI[] _tmp = seqs.getAlignment().getSequencesArray();
      SequenceI base = _tmp[_tmp.length - 1];
      SequenceI _one = base.deriveSequence();
      _one.deleteChars(0, 1);
      SequenceI _two = base.deriveSequence();
      _two.deleteChars(0, 2);
      SequenceI[] _geneSequence = new SequenceI[]{base, _one, _two};
      AlignmentI _geneAsAlignment = new Alignment(_geneSequence);
      
      String[] geneSequenceNames = new String[4];
      geneSequenceNames[0] = base.getName();

      AlignViewport geneSequence = new AlignViewport(_geneAsAlignment);
      Dna dna = new Dna(geneSequence, geneSequence.getViewAsVisibleContigs(true)); 

      GeneticCodes _gc = GeneticCodes.getInstance();
      GeneticCodeI _standardTranslationTable = _gc.getStandardCodeTable();
      SequenceI[] translatedSequence = dna.translateCdna(_standardTranslationTable).getSequencesArray();  //translate all 3 dnas
      
      int _i = 1;
      for (SequenceI protSeq : translatedSequence)    // add all 3 translations to the AlignmentViewport
      {
        geneSequenceNames[_i] = protSeq.getName();
        seqs.getAlignment().addSequence(protSeq);
      }
      
      _standardTranslationTable = null;
      _gc = null;
      _one = null;
      _two = null;
      _tmp = null;
      _geneSequence = null;
      _geneAsAlignment = null;
      
      List<SequenceI> sequencesList = seqs.getAlignment().getSequences();   // needed for checking if there is a gap 

      StringBuffer csv = new StringBuffer();
      csv.append("name");
      for (int i = 1; i <= width; i++)
      {
        csv.append(String.format(", %d", i));
        csv.append(String.format(", %d", i));
        csv.append(String.format(", %d", i));
      }
      csv.append("\n");

      for ( int i = 0; i < seqs.getAlignment().getSequencesArray().length - 1; i++)
      {
      
        SequenceI[] sequences = seqs.getAlignment().getSequencesArray();
        
        if (Arrays.asList(geneSequenceNames).contains(sequences[i].getName()) )   //not aling the parent sequences with eachother
          continue;

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
        int frameOffset = 2 - bestFrame;  // reverse order
        
        LinkedHashSet<Integer> genomicCorrespondingPositions = new LinkedHashSet<Integer>(sequences[i].getLength()*3);  // no duplicates
        
        //for saving reference
        LinkedList<HashMap<Character, int[]>> sequencePlusInfoList = new LinkedList<HashMap<Character, int[]>>(); //middle component for keeping the correct order of the AAs
        HashMap<Character, int[]> aaepgpPairs = new HashMap<Character, int[]>();  //inner component of domain map -- pairs AA to its EP and GPs (int[4] = [EP, GP1, GP2, GP3])

        int gapmarker = -1;

        int ep = 1;
        for (int k = 0; k < width; k++) // k is actually real ep; ep for calculation does not increase at gap to not skip a GP by accident
        {
          if (sequencesList.get(i).getCharAt(k) == '-')
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
            _basePosition = startingPosition + (ep + correspondingBase - 2) * 3 + frameOffset;     
            genomicCorrespondingPositions.add(_basePosition);
            genomicCorrespondingPositions.add(_basePosition + 1);
            genomicCorrespondingPositions.add(_basePosition + 2);
            
            currentEPandGPs = new int[]{k+1, _basePosition, _basePosition + 1, _basePosition + 2};
          } else {
            _basePosition = startingPosition - (ep + correspondingBase - 2) * 3 - frameOffset;
            genomicCorrespondingPositions.add(_basePosition);
            genomicCorrespondingPositions.add(_basePosition - 1);
            genomicCorrespondingPositions.add(_basePosition - 2);

            currentEPandGPs = new int[]{k+1, _basePosition, _basePosition - 1, _basePosition - 2};
          }
          aaepgpPairs = new HashMap<Character, int[]>();
          aaepgpPairs.put(sequencesList.get(i).getCharAt(k), currentEPandGPs);  // gaps will be skipped!!!!!
          sequencePlusInfoList.add(aaepgpPairs);
          
          ep++;
        }
        
        csv.append(sequences[i].getName());
        for (int k : genomicCorrespondingPositions) // k = GP
        {
          if (k < 0)
          {
            csv.append(", -");
          } else {
            csv.append(String.format(", %d", k));
          }
        }
        csv.append("\n");
        domain.putIfAbsent(sequences[i].getName(), sequencePlusInfoList);
      }
      
      String CsvString = csv.toString();
        
      CutAndPasteTransfer cap = new CutAndPasteTransfer();
      try
      {
        cap.setText(CsvString);
        Desktop.addInternalFrame(cap, MessageManager
                .formatMessage("label.points_for_params", "Outputting Equivalent Positions"), 500, 500);
      } catch (OutOfMemoryError oom)
      {
        new OOMWarning("exporting Natural Frequencies", oom);
        cap.dispose();
      }
      
      // start preparing the reference
      erf.setGeneSequence(base.getSequence());          // saves the DNA sequences as char[]
      
      int[] allGenomicPositions = new int[base.getLength()];
      for (int i = 0; i < allGenomicPositions.length; i++)
      {
        allGenomicPositions[i] = i + startingPosition;
      }
      erf.setGenomicPositions(allGenomicPositions);   // save the array of all genomic positions
        
      erf.setDomain(domain);    // saves the domain as a HashMap
      
      
      //save the reference
      erf.saveReference();
      
      
      //just for checking
      erf = null;
      erf = EpReferenceFile.loadReference(refDir);
      HashMap<Character, int[]> first = erf.getDomain().get("I65").get(3);
      char key = (char) first.keySet().toArray()[0];
      System.out.println(String.format("%s: %d -- %d, %d, %d", key, first.get(key)[0], first.get(key)[1], first.get(key)[2], first.get(key)[3]));
      
    } catch (Exception q)
    {
      Console.error("Error computing Equivalent Positions:  " + q.getMessage());
      q.printStackTrace();
    }
  }

}
