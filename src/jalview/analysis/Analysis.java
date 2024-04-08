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
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.SequenceI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceFeature;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.AlignmentPanel;
import jalview.gui.CutAndPasteTransfer;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.OOMWarning;
import jalview.gui.VariantJmol;
import jalview.io.EpReferenceFile;
import jalview.schemes.PlainColourScheme;
import jalview.util.MessageManager;
import jalview.util.MapList;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.knowm.xchart.style.PieStyler.LabelType;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.theme.GGPlot2Theme;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;

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
  final private AlignmentPanel proteinAlignmentPanel;
  
  final private AlignmentViewport proteinViewport;
  
  final private AlignmentViewport geneViewport;
  
  private int residue;
  
  final private String refFile;
  
  private String protSeqName;
  
  private SequenceI protSeq;
  
  private SequenceI geneSeq;
  
  private boolean isReverse;  // true = reverse strand
  
  private HashMap<String, Integer> frameOffset;  //OF of the domain aligned here
  
  private EpReferenceFile erf;
  
  /*
   * others
   */
  final private GeneticCodeI standardTranslationTable;
  
  final private String[] likelyness = new String[]{"Tolerated", "Moderately Tolerated", "Represented", "Rarely Represented", "Not Represented", "CAUTION!"};

  final private String[] verdict = new String[]{"Tolerated", "Rather Tolerated", "Unclassified (Needs manual checking)", "Rather Not Tolerated", "Potentially Harmful"};
  
  final private String[] distribution = new String[]{"Very Conserved", "Rather Conserved", "Moderately Conserved", "Less Conserved"};
  
  final private HashMap<Character, String> aaGroups = new HashMap<Character, String>();
  
  private boolean hasMSApopup = false;

  /*
   * outputs
   */
  //private char newAA;
  
  private HashMap<Character, Float> nfAtThisPosition;
  
  private float[] nFs;
  
  private char[] allAAatPos;
  
  private StringBuffer csv;
  
  private HashMap<String, HashMap<Character, Float>> aaGroupInfo = new HashMap<String, HashMap<Character, Float>>(Map.of("Acidic", new HashMap<Character, Float>(), "Basic", new HashMap<Character, Float>(), "Hydrophilic", new HashMap<Character, Float>(), "Intermediate", new HashMap<Character, Float>(), "Hydrophobic", new HashMap<Character, Float>()));

  private String CsvString;
  
  private HashMap<SequenceI, TreeMap<Integer, String[]>> foundDomains;  // <domain - <residue - AA change>>   //contains only those that match to residue
  
  private HashMap<String, TreeMap<Integer, String[]>> wholeGroupVariants;   // domain - <residue, AA change>> but for each seq individually
  
  private String foundDomainGroup;
  
  private AlignmentViewport avMSA;
  
  private int selectedEP;   //currently selected column
  
  private String selectedSequence;  // currently selected sequence (at column selectedEP)
  
  private HashMap<String, VariantJmol> activeJmols;  // domain name - Jmol
  
  /*
   * for pie chart
   */
  private PieChart pie;
  
  private JFrame frame;
  
  private JPanel piePanel;
  
  private final HashMap<Character, Integer> mapAAtoColourIndex = new HashMap<Character, Integer>();
  
  private final Color[] referenceColourScheme = jalview.schemes.ResidueProperties.ocean;
  
  /**
   * Constructor given the sequences to compute for and the residue position (base 1)
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param res
   */
  public Analysis(AlignmentPanel proteinPanel, int res) // alignment viewport has to only consist of the gene and protein sequence
  {
    this.proteinAlignmentPanel = proteinPanel;
    this.proteinViewport = proteinAlignmentPanel.av;
    this.residue = res - 1;   // convert base 1 input to internal base 0
    
    AlignmentViewport _tmp = proteinViewport;
    for (AlignmentViewport port : Desktop.getViewports(null))
    {
      if (port != proteinViewport)      //should always happen, otherwise error
      {
        _tmp = port;
      }
    }
    this.geneViewport = _tmp;

    this.protSeq = proteinViewport.getAlignment().getSequencesArray()[0];
    this.protSeqName = protSeq.getName();
    this.geneSeq = geneViewport.getAlignment().getSequencesArray()[0];
    
    this.activeJmols = new HashMap<String, VariantJmol>();
    
    this.foundDomains = new HashMap<SequenceI, TreeMap<Integer, String[]>>();
    this.frameOffset = new HashMap<String, Integer>();
    
    this.refFile = String.format("%s.ref", protSeqName);
    GeneticCodes _gc = GeneticCodes.getInstance();
    this.standardTranslationTable = _gc.getStandardCodeTable();

    HashMap<Character, String> acidic = new HashMap<Character, String>(Map.of('D', "Acidic", 'E', "Acidic"));
    HashMap<Character, String> basic = new HashMap<Character, String>(Map.of('H', "Basic", 'K', "Basic", 'R', "Basic"));
    HashMap<Character, String> hydrophilic = new HashMap<Character, String>(Map.of('N', "Hydrophilic", 'Q', "Hydrophilic"));
    HashMap<Character, String> intermediate = new HashMap<Character, String>(Map.of('Y', "Intermediate", 'W', "Intermediate", 'P', "Intermediate", 'G', "Intermediate", 'A', "Intermediate", 'S', "Intermediate", 'T', "Intermediate", 'C', "Intermediate"));
    HashMap<Character, String> hydrophobic = new HashMap<Character, String>(Map.of('F', "Hydrophobic", 'M', "Hydrophobic", 'I', "Hydrophobic", 'L', "Hydrophobic", 'V', "Hydrophobic"));
    this.aaGroups.putAll(acidic);
    this.aaGroups.putAll(basic);
    this.aaGroups.putAll(hydrophilic);
    this.aaGroups.putAll(intermediate);
    this.aaGroups.putAll(hydrophobic);
    
    int i = 0;
    for (char aa : new char[]{'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', 'B', 'Z', 'X', '-', '*', '.'})
    {
      mapAAtoColourIndex.put(aa, i++);
    }
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
      clean();
    
      //check if the reference file exists
      if (!(new File(refFile).exists()))
      {
        //throws a warning dialog saying that no file with the name {refFile} was found
        JvOptionPane.showInternalMessageDialog(Desktop.desktop, String.format("No reference file \"%s\" found. Aborting.", refFile), "No Reference Error", JvOptionPane.WARNING_MESSAGE);
        throw new RuntimeException();
      }
      
      //load the reference
      erf = EpReferenceFile.loadReference(refFile);
      
      // set reverse strand
      this.isReverse = erf.getReverse();
      
      //set frame offset
      this.frameOffset = erf.getDomainOffset();
      
      produceSummary();   // performs the analysis for the summary csv output and calls the addDomainMSApopup

      //starting VariantJmol
      for (SequenceI variantDomain : foundDomains.keySet())
      {
        proteinViewport.getAlignment().addSequence(variantDomain);  // add the found sequence back to the Viewport
        this.proteinViewport.setCodingComplement(this.geneViewport);

        int domainAFindex = 0;
        AlignFrame[] afs = Desktop.getAlignFrames();
        for (AlignFrame af : afs)
        {
          if (af.getTitle().equals(this.foundDomainGroup))
            break;
          domainAFindex++;
        }

        for (String iteratingDomain : wholeGroupVariants.keySet())
        {
          TreeMap<Integer, String[]> variantResiduesOfDomain = wholeGroupVariants.get(iteratingDomain);
          HashMap<Integer, char[][]> varUnsorted = new HashMap<Integer, char[][]>();
          for (int key : variantResiduesOfDomain.keySet())
          {
            char[][] savs = new char[variantResiduesOfDomain.get(key).length][2];
            int i = 0;
            for (String s : variantResiduesOfDomain.get(key))     // A,B
            {
              char[] aaChange = new char[]{s.charAt(0), s.charAt(2)};
              savs[i++] = aaChange;
            }
            varUnsorted.put(key, savs);
          }
          
          varUnsorted = convertResToEp(iteratingDomain, varUnsorted);
          colourVariants(afs[domainAFindex], iteratingDomain, varUnsorted);
        }
        
        VariantJmol structureWindow = new VariantJmol(variantDomain, refFile, proteinAlignmentPanel, foundDomains.get(variantDomain), convertEpToRes(variantDomain.getName(), selectedEP));
        activeJmols.putIfAbsent(variantDomain.getName(), structureWindow);
        new Thread(structureWindow).start();

        proteinViewport.getAlignment().deleteSequence(variantDomain);
        
        List<SequenceI> bigGroupL = new ArrayList<SequenceI>();
        for (SequenceI seq : avMSA.getAlignment().getSequencesArray())
        {
          bigGroupL.add(seq);
        }
        RepeatingVariance.addAlignment(avMSA.getAlignment());
        RepeatingVariance.calculate(avMSA.getAlignment());
        RepeatingVariance.completeVariance(avMSA.getAlignment());
        avMSA.getAlignment().addAnnotation(RepeatingVariance.getAnnotation(avMSA.getAlignment()), 0);
        avMSA.setAnalysis(this);
    
      }
      
    } catch (Exception q)
    {
      Console.error("Error analysing:  " + q.getMessage());
      q.printStackTrace();
    }
  }
  
  /**
   * produces the summary csv output of this analysis
   * @param
   */
  private void produceSummary()
  {
    produceSummary(null);
  }
  private void produceSummary(String target)
  {
      //create the output StringBuffer and add the heading line
      csv = new StringBuffer();
      csv.append(String.format("Information on the domains found at position %d\n\n", residue + 1));
      
      // clean up the AlignmentViewport by removing all added sequences again
      clean();
      
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
        if ((target != null) && (domainName.equals(target)))
        {
          proteinViewport.getAlignment().addSequence(new Sequence(domainName, aas, 1, aas.length));  // adding domain to AlignmentViewport seqs
          break;
        } else if (target == null)
        {
          proteinViewport.getAlignment().addSequence(new Sequence(domainName, aas, 1, aas.length));  // adding domain to AlignmentViewport seqs
        }
      }
      
      AlignSeq[] alignments = new AlignSeq[proteinViewport.getAlignment().getHeight() - 1];  // list of all pairwise alignments of domain to input prot seq
      byte skip = 1;
      // do the alignment
      for ( int i = skip; i < proteinViewport.getAlignment().getHeight(); i++)
      {
      
        SequenceI[] sequences = proteinViewport.getAlignment().getSequencesArray();  // SequenceI array having the input prot seq at 0
        
        SequenceGroup sg = new SequenceGroup();
        sg.addSequence(protSeq, false);
        sg.addSequence(sequences[i], false);
        
        // copying code from gui/PairwiseAlignPanel
        SequenceI[] forAlignment = new SequenceI[2];
        forAlignment[0] = protSeq;
        forAlignment[1] = sequences[i];
        
        sequences = null;
        
        String[] seqStrings = new String[2];
        seqStrings[0] = forAlignment[0].getSequenceAsString();
        seqStrings[1] = forAlignment[1].getSequenceAsString();

        AlignSeq as = new AlignSeq(forAlignment[0], seqStrings[0], forAlignment[1], seqStrings[1], AlignSeq.PEP); // align the 2 sequences
        as.calcScoreMatrix();
        as.traceAlignment();
        as.scoreAlignment();
        as.printAlignment(System.out);
        
        alignments[i-skip] = as; // save the alignment for later
      }

      // clean up the AlignmentViewport by removing all added sequences again
      clean();
      
      HashSet<String> domains = new HashSet<String>();   // holding all domain (names) that were found at residue (should be only 1)
      HashSet<Integer> alignmentNr = new HashSet<Integer>();  // holding the indexes of the pairwise alignment of the domain with the prot seq in alignments
      int k = 0;
      for (AlignSeq al : alignments)  // finding the domains which align at the specified position
      {
        if ((al.getSeq1Start()-1 <= residue) && (al.getSeq1End() > residue)) //if the residue is inside the alignment region
        {
          int posInDomain = al.getSeq2Start() + (residue - al.getSeq1Start()); 
          if(al.astr2.charAt(posInDomain) != '-')     //skip gaps
          {
            domains.add(al.s2.getName());
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
    for (String domainName : domains)  // doing the analysis and outputting to csv
    {
      selectedSequence = domainName;  // save selected sequence for colouring
      
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
      HashMap<Character, int[]> position = aaList.get(posInDomain);
      char aaAtPos = (char) position.keySet().toArray()[0];
      int[] epgp = position.get(aaAtPos);   //is the real ep + gps at the specified position
      selectedEP = epgp[0];

      csv.append(String.format("%s residue %d (%c): %s (%s) EP %d, GPs [%d, %d, %d]\n", protSeqName, residue + 1, protSeq.getCharAt(residue), domainName, aaAtPos, epgp[0], epgp[1], epgp[2], epgp[3]));
      
      //natural frequencies
      LinkedList<HashMap<Character, Float>> nfList = new LinkedList<HashMap<Character, Float>>();   // need to initialize because else error
      for (String[] listofdomains : nF.keySet())
      {
        if (Arrays.asList(listofdomains).contains(domainName))  // has to be true one time
          nfList = nF.get(listofdomains);
      } 
      
      //output the nf header and information
      csv.append("\nNatural frequencies at this position:\n");
      nfAtThisPosition = nfList.get(selectedEP-1);
      nFs = new float[nfAtThisPosition.size()];
      allAAatPos = new char[nfAtThisPosition.size()];
      int l = 0;
      for (char aa : nfAtThisPosition.keySet())
      {
        csv.append(String.format("%c: %1.2f%%, ", aa, nfAtThisPosition.get(aa)));
        allAAatPos[l] = aa;
        nFs[l++] = nfAtThisPosition.get(aa);
      }
      
      char[][] aaChanges = new char[1][1]; // for displaying the variants in the MSA view
      //variants found at this position (epgp[1] - 3)
      if (geneSeq != null)
      {
       MapList mapping = geneSeq.getGeneLoci().getMapping();
       aaChanges = searchAndAnalyse(mapping, epgp[1], epgp[3], true);   

       TreeMap<Integer, String[]> variantResidues = scanDomain(domainName, aaList.peekFirst().get(aas[0])[1], aaList.peekLast().get(aas[aas.length-1])[3]); // all residues of domain that have variant
       SequenceI domainSequence = new Sequence(domainName, aas, 1, aas.length);
       foundDomains.put(domainSequence, variantResidues);    // save the domain as a SequenceI
       
       addDomainDNAtoComplement(domainSequence, aaList.peekFirst().get(aas[0])[1], aaList.peekLast().get(aas[aas.length-1])[3]);

      }
      
      csv.append("\n---------\n");
      
      HashMap<Integer, char[][]> epWithVar = new HashMap<Integer, char[][]>();
      
      epWithVar.put(epgp[0], aaChanges);
      if (!hasMSApopup)
        this.foundDomainGroup = addDomainMSAPopup(domainName, epWithVar);
      
      k++;
    }
    
    
    // wrap up result
    CsvString = csv.toString();
    
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.setText(CsvString);
      Desktop.addInternalFrame(cap, "Outputting Analysis results", 500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("exporting Analysis results", oom);
      cap.dispose();
    }
    pieChart();
  }
  
  /**
   * scan the gene sequence for variants in the domain region
   * @param domain
   * @param start
   * @param end
   * @return positions in domain, where variants
   */
  private TreeMap<Integer, String[]> scanDomain(String domain, int start, int end)
  {
    MapList mapping = geneSeq.getGeneLoci().getMapping();
    int[] featureRange = mapping.locateInFrom(start, end);   // nucleotide numbers of gene seq where mutated codon (!! start @1)
    List<SequenceFeature> variants = geneSeq.findFeatures(featureRange[0], featureRange[1]);   // start@1
    TreeSet<Integer> variantPos = new TreeSet<Integer>();
    for (SequenceFeature variant : variants)
    {
      int starT = variant.getBegin();
      int enD = variant.getEnd();
      do
      {
        variantPos.add(starT++);
      } while (starT <= enD);
    }
    TreeMap<Integer, String[]> result = new TreeMap<Integer, String[]>();
    HashSet<Integer> alreadyChecked = new HashSet<Integer>();
    for (int p : variantPos)
    {
      int nuc1 = p;
      int[] baseOfFrame = new int[]{1, 2, 0};
      while (nuc1 % 3 != baseOfFrame[this.frameOffset.get(domain)])   // nucleotide numbering starts at 1
        nuc1--;
      
      if (alreadyChecked.contains(nuc1))
        continue;

      int[] oldRange = mapping.locateInTo(nuc1, nuc1+2);  // convert back, searchAndAnalyse needs vcf numbering
      char[][] allSAVChanges = searchAndAnalyse(mapping, oldRange[0], oldRange[1], false);
      String[] savAsString = new String[allSAVChanges.length];
      for (int i = 0; i < allSAVChanges.length; i++)
      {
        savAsString[i] = String.format("%c,%c", allSAVChanges[i][0], allSAVChanges[i][1]);
      }
      result.put((int) Math.floor((p - featureRange[0]) / 3), savAsString);
      alreadyChecked.add(nuc1);
    }
    
    return result;
  }
  
  
  /**
   * scan each domain for their variants
   * @param domainGroup
   * @param erf
   * @param ignore
   * 
   * @return domainName -> varRes, exchange
   */
  private HashMap<String, TreeMap<Integer, String[]>> searchVariantsRecursive(String domainGroup)
  {
    HashMap<String, TreeMap<Integer, String[]>> result = new HashMap<String, TreeMap<Integer, String[]>>();
    for (String domain : erf.getDomainGroups().get(domainGroup))
    {

      LinkedList<HashMap<Character, int[]>> aaList = erf.getDomain().get(domain);
      char[] aas = new char[aaList.size()];   // AA sequence of the domain  (has to convert the keys of each map in the list into one array
      for (int i = 0; i < aaList.size(); i++)
      {
        HashMap<Character, int[]> aaepPair = aaList.get(i);
        char c = (char) aaepPair.keySet().toArray()[0]; 
        aas[i] = c;
      }
      TreeMap<Integer, String[]> variantResidues = scanDomain(domain, aaList.peekFirst().get(aas[0])[1], aaList.peekLast().get(aas[aas.length-1])[3]);
      result.put(domain, variantResidues);
    }
    return result;
  }
  
  /**
   * translate a three nucleotide codon
   * @param String codon
   * @return char amino acid
   */
  private char translateCodon(String codon)
  {
    SequenceI threeNucs = new Sequence("threeNucs", codon, 1, 3);
    SequenceI[] _geneSequence = new SequenceI[]{threeNucs};
    AlignmentI _geneAsAlignment = new Alignment(_geneSequence);
    AlignViewport geneSequence = new AlignViewport(_geneAsAlignment);
    Dna dna = new Dna(geneSequence, geneSequence.getViewAsVisibleContigs(true));
    return dna.translateCdna(this.standardTranslationTable).getSequencesArray()[0].getSequence()[0];
  }
  
  /**
   * search the gene for all variants in a certain range and analyse them
   * if output is given, perform a more detailed analysis and output the result
   * @param map
   * @param start ~ 1st nucleotide of codon
   * @param end ~ last nucleotide of codon
   * @param output ~ toggle StringBuffer output
   * @return all SAV changes as "A,B"
   */
  private char[][] searchAndAnalyse(MapList map, int start, int end, boolean output)
  {
    int[] featureRange = map.locateInFrom(start, end);   // nucleotide numbers of gene seq where mutated codon (!! start @1)
    List<SequenceFeature> variants = geneSeq.findFeatures(featureRange[0], featureRange[1]);   // start@1
    
    if (output)
      csv.append(String.format("\n\nVariants found from position %d to %d:\n", start, end));

    HashSet<char[]> result = new HashSet<char[]>();

    for (SequenceFeature feature : variants)
    {
      // translate the three nucleotides to know the AA change
      String[] snvChange = feature.getDescription().split(",");  // [from, to]
      char[] origCodon = new char[3];   //original codon
      for (int i = 0; i < 3; i++)
      {
        origCodon[i] = geneSeq.getCharAt(featureRange[0] - 1 + i);    //!! start @0
      }
      
      int pos = Integer.parseInt(feature.otherDetails.get("POS").toString());  // GP of snv
      // build snv strings eg cgA -> cgG
      String[] snvStrings = new String[]{"",""}; // [orig, new]  ; to not have nullAgt
      for (int i = 0, s = start; i < 3; i++)
      {
        if (s == pos)
        {
          snvStrings[0] += snvChange[0];
          snvStrings[1] += snvChange[1];
        } else {
          snvStrings[0] += Character.toString(Character.toLowerCase(origCodon[i]));
          snvStrings[1] += Character.toString(Character.toLowerCase(origCodon[i]));
        }
        s = isReverse ? s - 1 : s + 1;
      }

      char oldAA = translateCodon(snvStrings[0]);
      char newAA = translateCodon(snvStrings[1]);

      result.add(new char[]{oldAA, newAA});
      
      //####### rest of loop only for creating the output
      if (output)
      {

        csv.append(String.format("GP %d %s: %s -> %s (%c -> %c) - frequency %s\n\n", pos, feature.getType(), snvStrings[0], snvStrings[1], protSeq.getCharAt(residue), newAA, feature.otherDetails.get("AF").toString()));

        // summarizing statement
        csv.append("Brief summary:\n");
   
      float freqFrom = nfAtThisPosition.keySet().contains(protSeq.getCharAt(residue)) ? nfAtThisPosition.get(protSeq.getCharAt(residue)) : 0f;
      float freqTo = nfAtThisPosition.keySet().contains(newAA) ? nfAtThisPosition.get(newAA) : 0f;
     
      //gather nfs for all groups
      for (char aa : aaGroups.keySet())
      {
        for (int i = 0; i < nFs.length; i++)
        {
          if (aa == allAAatPos[i])
          {
            HashMap<Character, Float> aaMap = this.aaGroupInfo.get(aaGroups.get(aa));
            aaMap.put(aa,  nFs[i]);
            break;
          }
        }
      }
      
      TreeMap<String, Float> presentGroups = new TreeMap<String, Float>();   //summ the frequencies for all groups to get the most prominent one
      HashMap<String, Integer> groupDistribution = new HashMap<String, Integer>();   // how many different residues per group present
      for (String group : aaGroupInfo.keySet())
      {
        float sum = 0f;
        int differentAAs = 0;
        for (char aa : aaGroupInfo.get(group).keySet())
        {
          sum += aaGroupInfo.get(group).get(aa);
          if (aaGroupInfo.get(group).get(aa) != 0f)
            differentAAs++;
        }
        if (sum != 0f)
          presentGroups.put(group, sum);
        if (differentAAs != 0)
          groupDistribution.put(group,  differentAAs);
      }
      
      String thisDistribution = "";    // saying how much broadly distributed this position is
      if (presentGroups.size() == 1 && (groupDistribution.get(groupDistribution.keySet().toArray()[0]) <= 2))
        thisDistribution = distribution[0];
      else if (presentGroups.size() == 1)
        thisDistribution = distribution[1];
      else if (presentGroups.size() == 2)
        thisDistribution = distribution[2];
      else if (presentGroups.size() > 2)
        thisDistribution = distribution[3];
      
      String fromLikely = "";
      int fromIndex = 0;
      String toLikely = "";
      int toIndex = 0;
      if (freqFrom <= 0f)
      {
        fromLikely = String.format("%s (%s)", likelyness[4], likelyness[5]);
        fromIndex = 4;
      }
      else if (freqFrom <= 4f)
      {
        fromLikely = likelyness[3];
        fromIndex = 3;
      }
      else if (freqFrom <= 15f)
      {
        fromLikely = likelyness[2];
        fromIndex = 2;
      }
      else if (freqFrom < 50f)
      {
        fromLikely = likelyness[1];
        fromIndex = 1;
      }
      else
      {
        fromLikely = likelyness[0];
        fromIndex = 0;
      }

      if (freqTo <= 0f)
      {
        toLikely = String.format("%s (%s)", likelyness[4], likelyness[5]);
        toIndex = 4;
      }
      else if (freqTo <= 4f)
      {
        toLikely = likelyness[3];
        toIndex = 3;
      }
      else if (freqTo <= 15f)
      {
        toLikely = likelyness[2];
        toIndex = 2;
      }
      else if (freqTo < 50f)
      {
        toLikely = likelyness[1];
        toIndex = 1;
      }
      else
      {
        toLikely = likelyness[0];
        toIndex = 0;
      }

      csv.append(String.format("Residue %d is %s.\n", residue + 1, thisDistribution));
      csv.append(String.format("It changes from a %s %s residue (%c : %2.3f%%)", fromLikely, aaGroups.get(protSeq.getCharAt(residue)), protSeq.getCharAt(residue), freqFrom));
      csv.append(String.format(", to a %s %s residue (%c : %2.3f%%).\n", toLikely, aaGroups.get(newAA), newAA, freqTo));
      int prolinPenalty = 0;
      if ((newAA == 'P') && (freqTo < 4f))
      {
        csv.append("The residue changed to a Prolin which is barely/not represented in this position!\n");
        prolinPenalty = 1;
      }

      int sameGroupFactor = aaGroups.get(protSeq.getCharAt(residue)) == aaGroups.get(newAA) ? 1 : 0;
      int tmpVerdictIndex = toIndex - (sameGroupFactor - prolinPenalty);
      tmpVerdictIndex = tmpVerdictIndex >= verdict.length ? verdict.length - 1 : tmpVerdictIndex;
      String overallLikely = verdict[ (int) (0.5 * (tmpVerdictIndex + Math.sqrt(Math.pow(tmpVerdictIndex, 2)))) ];
      csv.append(String.format("This change is considered %s\n\n", overallLikely));
      }
    }
    char[][] r = new char[result.size()][2];
    int i = 0;
    for (char[] s : result)
    {
      r[i++] = s;
    }
    return r;
  }
  
  /**
   * extracts the DNA seq of the domain and adds it to this.geneViewport
   * @param domain
   * @param start (in gene sequence)
   * @param end (in gene sequence)
   */
  private void addDomainDNAtoComplement(SequenceI domain, int start, int end)
  {
    MapList mapping = geneSeq.getGeneLoci().getMapping();
    int[] domainRegion = mapping.locateInFrom(start, end);   // nucleotide numbers of domain (!! start @1)
    List<SequenceFeature> variants = geneSeq.findFeatures(domainRegion[0], domainRegion[1]);   // start@1
    char[] parsedDNA = geneSeq.getSequence(domainRegion[0], domainRegion[1] + 1);
    SequenceI cDNA = new Sequence(domain.getName(), parsedDNA, 1, parsedDNA.length);
    
    List<SequenceFeature> transformedVariants = new ArrayList<SequenceFeature>();
    for (SequenceFeature var : variants)
    {
      transformedVariants.add(new SequenceFeature(var.getType(), var.getDescription(), (int) Math.floor((var.getBegin() - domainRegion[0])), (int) Math.floor((var.getEnd() - domainRegion[0])), var.getFeatureGroup()));
    }

    cDNA.setSequenceFeatures(transformedVariants);
    cDNA.setDatasetSequence(cDNA.createDatasetSequence());
    
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList dnaAAmap = new MapList(new int[] {cDNA.getStart(), cDNA.getEnd()}, new int[] {domain.getStart(), domain.getEnd()}, 3, 1);
    
    acf.addMap(cDNA, domain, dnaAAmap);

    proteinViewport.getAlignment().addCodonFrame(acf);
    geneViewport.getAlignment().addCodonFrame(acf);

    geneViewport.getAlignment().addSequence(cDNA);  
  }
  
  /**
   * show a new AlignFrame containing the MSA with the found domain
   * highlight the position which has the found variant (colour)
   * @param domain
   * @param var ~ ep -> A to B
   */
  private String addDomainMSAPopup(String domain, HashMap<Integer, char[][]> var)
  {
    HashMap<String, LinkedHashSet<String>> domainGroups = erf.getDomainGroups();
    String thisDomainGroup = "";
    
    for (String dG : domainGroups.keySet())
    {
      for (String dom : domainGroups.get(dG))
      {
        if (dom.equals(domain))
        {
          thisDomainGroup = dG;         // has to happen once
          break;
        }
      }
    }
    wholeGroupVariants = searchVariantsRecursive(thisDomainGroup);
    
    HashSet<String> domainNamesInThisGroup = domainGroups.get(thisDomainGroup);
    SequenceI[] domainsInThisGroup = new SequenceI[domainNamesInThisGroup.size()];  // fetch all sequences
    
    HashMap<String, char[]> allDomains = erf.getAlignedDomains();
    int j = 0;
    for (String domName : domainNamesInThisGroup)
    {
      char[] thisSeqCharArray = allDomains.get(domName);

      domainsInThisGroup[j++] = new Sequence(domName, thisSeqCharArray, 1, thisSeqCharArray.length);
    }
    
    AlignmentI al = new Alignment(domainsInThisGroup);
    AlignFrame af = new AlignFrame(al, al.getWidth(), al.getHeight());
    
    Desktop.addInternalFrame(af, thisDomainGroup, AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);

    AlignmentViewport av = af.getCurrentView();
    AlignmentPanel ap = af.newView(domain, true);
    af.closeView(av);
    
    colourVariants(af, domain, var, true, 1);

    //af.getCurrentView().setAnalysis(this);
    av = af.getCurrentView();
    
    af.getCurrentView().autoCalculateVariance = true;
    af.getCurrentView().setShowVariance(true);
    af.getCurrentView().setShowVarianceHistogram(true);

    //af.getCurrentView().setShowGroupConsensus(false);
    //af.alignPanel.updateAnnotation(true);
    //af.setAnnotationsVisibility(true, true, false);
    //av.updateConsensus(af.getAlignPanels().get(0));

    //List<SequenceI> bigGroupL = new ArrayList<SequenceI>();
    //for (SequenceI seq : av.getAlignment().getSequencesArray())
    //{
      //bigGroupL.add(seq);
    //}
    //SequenceGroup bigGroup = new SequenceGroup(bigGroupL, "All", null, false, true, false, av.getAlignment().getStartRes(), av.getAlignment().getEndRes());
    //av.getAlignment().addGroup(bigGroup);
    //av.getAlignment().addAnnotation(bigGroup.getVariance(), 0);
    //bigGroup.recalcVariance();

    ap.updateAnnotation(true);
    this.avMSA = av;
    this.hasMSApopup = true;

    return thisDomainGroup;
  }
  
  /**
   * colors positions at var.keySet according to their change
   * 
   * @param af  holding the panel
   * @param var ep -> A to B
   * @param isSelected  different colours
   */
  private void colourVariants(AlignFrame af, String domain, HashMap<Integer, char[][]> var)
  {
    colourVariants(af, domain, var, false, 0);
  }
  private void colourVariants(AlignFrame af, String domain, HashMap<Integer, char[][]> var, boolean isSelected, int base)
  {
    AlignmentViewport av = af.getCurrentView();
    
    // color arrays [from, to, other]
    Color[] colSelected = new Color[]{new Color(98, 131, 222), new Color(250, 133, 120), new Color(100, 100, 100)};
    Color[] colRest = new Color[]{new Color(107, 217, 227), new Color(240, 180, 175), new Color(230, 230, 230)};
    
    for (int ep : var.keySet())
    {
      int variantsAtEp = var.get(ep).length;
      char[] fromRes = new char[variantsAtEp];
      char[] toRes = new char[variantsAtEp];
      for (int i = 0; i < variantsAtEp; i++)
      {
        fromRes[i] = var.get(ep)[i][0];
        toRes[i] = var.get(ep)[i][1];
      }

      ep = ep - base;   //correcting ep base to 0

      boolean isSelectedColumn = ep == (selectedEP - 1) ? true : false;     // ep base 0, selectedEP base 1

      StringBuilder newLabel = new StringBuilder();
      newLabel.append("Variant at EP " + (ep+1) + " (");    //display ep as base 1, uniform with column numbering and summary output
      for (int i = 0; i < fromRes.length; i++)
      {
        newLabel.append(String.format("%c,%c", fromRes[i], toRes[i]));
        if (i != fromRes.length - 1)
          newLabel.append("; ");
      }
      newLabel.append(")");
      String label = isSelected ? "" : newLabel.toString();

      PlainColourScheme withColour;
      PlainColourScheme toColour;
      PlainColourScheme notColour;
      if (isSelected)
      {
        withColour = new PlainColourScheme(colSelected[0]);
        toColour = new PlainColourScheme(colSelected[1]);
        notColour = new PlainColourScheme(colSelected[2]);
      } else {
        withColour = new PlainColourScheme(colRest[0]);
        toColour = new PlainColourScheme(colRest[1]);
        notColour = new PlainColourScheme(colRest[2]);
      }

      SequenceGroup withGroup;
      SequenceGroup toGroup;
      SequenceGroup notGroup;
      SequenceGroup selectedGroup = new SequenceGroup();
      if (isSelected)
      {
        PlainColourScheme selectedColour;
        Color selectedOutline;
        boolean selectedIsFrom = false;
        boolean selectedIsTo = false;
        
        SequenceI[] seqArray = af.getCurrentView().getAlignment().getSequencesArray();
        List<SequenceI> sequencesWithRes = new ArrayList<SequenceI>();
        List<SequenceI> sequencesToRes = new ArrayList<SequenceI>();
        List<SequenceI> sequencesNotRes = new ArrayList<SequenceI>();
        for (int i = 0; i < seqArray.length; i++)
        {
          String thisSequence = seqArray[i].getName();
          char thisAA = seqArray[i].getCharAt(ep);
          boolean isFrom = false;
          boolean isTo = false;
          
          if (thisAA == '-')      // skip gaps
            continue;
          
          //skip if variant present
          // -> covered in other loop
          if (wholeGroupVariants.get(thisSequence).containsKey(convertEpToRes(thisSequence, ep+base)-1))
            continue;
          
          for (int j = 0; j < fromRes.length; j++)
          {
            if (fromRes[j] == thisAA)
            {
              isFrom = true;
              break;
            } else if (toRes[j] == thisAA)
            {
              isTo = true;
              break;
            }
          }
          
          if (thisSequence.equals(domain))
          {
            selectedIsFrom = isFrom;
            selectedIsTo = isTo;
            continue;
          }
          if (isFrom)  
            sequencesWithRes.add(seqArray[i]);
          else if (isTo)
            sequencesToRes.add(seqArray[i]);
          else
            sequencesNotRes.add(seqArray[i]);
        }
        withGroup = new SequenceGroup(sequencesWithRes, label, null, true, true, false, ep, ep);
        toGroup = new SequenceGroup(sequencesToRes, label, null, true, true, false, ep, ep);
        notGroup = new SequenceGroup(sequencesNotRes, label, null, true, true, false, ep, ep);

        if (selectedIsFrom)
        {
          selectedColour = new PlainColourScheme(colRest[0]);
          selectedOutline = colSelected[0];
        } else if (selectedIsTo) {
          selectedColour = new PlainColourScheme(colRest[1]);
          selectedOutline = colSelected[1];
        } else {
          selectedColour = new PlainColourScheme(colRest[2]);
          selectedOutline = colSelected[2];
        }

        selectedGroup = new SequenceGroup(new ArrayList<SequenceI>(av.getAlignment().getSequencesByName().get(domain)), label, selectedColour, true, true, false, ep, ep);
        selectedGroup.textColour = new Color(255,0,0);
        selectedGroup.setOutlineColour(selectedOutline);
      } else {
        List<SequenceI> seqList = new ArrayList<SequenceI>(av.getAlignment().getSequencesByName().get(domain));
        withGroup = new SequenceGroup(seqList, label, withColour, true, true, false, ep, ep);
        toGroup = new SequenceGroup(seqList, label, toColour, true, true, false, ep, ep);
        notGroup = new SequenceGroup(seqList, label, notColour, true, true, false, ep, ep);
      }

      if (isSelected)
      {
        withGroup.setOutlineColour(colSelected[0]);
        toGroup.setOutlineColour(colSelected[1]);
        notGroup.setOutlineColour(colSelected[2]);
      } else if (isSelectedColumn) {
        List<SequenceI> thisSeq = av.getAlignment().getSequencesByName().get(domain);
        boolean isFrom = false;
        boolean isTo = false;
        
        /*
        SequenceI selSequence = av.getAlignment().getSequencesByName().get(selectedSequence).get(0);
        for (Object seq : foundDomains.keySet().toArray())
        {
          SequenceI seqq = (SequenceI) seq;
          if (seqq.getName().equals(selectedSequence))
            selSequence = seqq;
        }
        */
        int epOfNongap = convertEpToRes(selectedSequence, ep);
        Color outline;
        if ((wholeGroupVariants.containsKey(selectedSequence)) && (wholeGroupVariants.get(selectedSequence).containsKey(epOfNongap)))
        {
          String[] selVar = wholeGroupVariants.get(selectedSequence).get(epOfNongap);
          
          if (selVar != null)
          {
            for (SequenceI seq : thisSeq)
            {
              for (char from : fromRes)
              {
                for (String vari : selVar)
                {
                  if (seq.getCharAt(ep) == vari.charAt(0))
                  {
                    isFrom = true;
                    break;
                  }
                  if (seq.getCharAt(ep) == vari.charAt(2))
                  {
                    isTo = true;
                    break;
                  }
                }
              }
            }
          }
        }
        if (isFrom)
          outline = colSelected[0];
        else if (isTo)
          outline = colSelected[1];
        else
          outline = colSelected[2];
        
        withGroup.setOutlineColour(outline);
        toGroup.setOutlineColour(outline);
        notGroup.setOutlineColour(outline);
      } else {
        withGroup.setOutlineColour(Color.white);
        toGroup.setOutlineColour(Color.white);
        notGroup.setOutlineColour(Color.white);
      }

      if (isSelected)
      {
        av.getAlignment().addGroup(withGroup);
        av.getAlignment().addGroup(toGroup);
        av.getAlignment().addGroup(notGroup);
        av.getAlignment().addGroup(selectedGroup);
      } else {
        char thisAA = av.getAlignment().getSequencesByName().get(domain).get(0).getCharAt(ep);
        boolean isFrom = false;
        boolean isTo = false;
          
        for (int j = 0; j < fromRes.length; j++)
        {
          if (fromRes[j] == thisAA)
          {
            isFrom = true;
            break;
          } else if (toRes[j] == thisAA)
          {
            isTo = true;
            break;
          }
        }
        if (isFrom)
          av.getAlignment().addGroup(withGroup);
        else if (isTo)
          av.getAlignment().addGroup(toGroup);
        else
          av.getAlignment().addGroup(notGroup);
      }
    }
    af.repaint();
  }
  
  /**
   * convert res numbering in variant set to variant numbering  (nth var in this sequence)
   * @param name  sequence name
   * @param res   set with res numbering
   * @param erf   reference file
   * @return
   */
  private HashMap<Integer, char[][]> convertResToEp(String name, HashMap<Integer, char[][]> res)
  {
    HashMap<Integer, char[][]> ep = new HashMap<Integer, char[][]>();
    int[] keys = new int[res.size()];
    int j = 0;
    for (int i : res.keySet())
    {
      keys[j++] = i;
    }
    Arrays.sort(keys);

    LinkedList<HashMap<Character, int[]>> list = erf.getDomain().get(name);
    char[] domainWithoutGaps = new char[list.size()];
    for (int i = 0; i < domainWithoutGaps.length; i++)
    {
      domainWithoutGaps[i] = (char) list.get(i).keySet().toArray()[0];
    }
    

    char[] domainWithGaps = erf.getAlignedDomains().get(name);
    j = 0;  // index of nongap-sequence
    int k = 0; // index of keys
    for (int i = 0; i < domainWithGaps.length; i++)
    {
      if ((domainWithGaps[i] != '-') && (k < keys.length))
      {
        if (domainWithGaps[i] == domainWithoutGaps[j])
        {
          if (j == keys[k])
            ep.put(i, res.get(keys[k++]));
          j++;
        }
      }
    }
    
    return ep;
  }

  /**
   * converts ep numbering to residue number of non-gapped sequence
   * @param name
   * @param res
   * @return  base 1
   */
  private int convertEpToRes(String name, int res)
  {
    char[] domainWithGaps = erf.getAlignedDomains().get(name);
    LinkedList<HashMap<Character, int[]>> list = erf.getDomain().get(name);
    char[] domainWithoutGaps = new char[list.size()];
    for (int i = 0; i < domainWithoutGaps.length; i++)
    {
      domainWithoutGaps[i] = (char) list.get(i).keySet().toArray()[0];
    }
    
    int j = 0;  //index of nongapped sequence
    for (int i = 0; i < domainWithGaps.length; i++)
    {
      if ((domainWithGaps[i] != '-') && j < (domainWithoutGaps.length))
      {
        if (domainWithGaps[i] == domainWithoutGaps[j])
        {
          j++;
          if (i+1 == res)     // EP (i) base 1, needs to be base 0
            return j;
        }
      }
    }
    return -1;
  }
  
  /**
   * updates the selected position and column to the new selection (domain, column (start @1))
   * @param domain
   * @param ep 
   */
  public void updateFocus(SequenceGroup sg, AlignmentViewport av)
  {
    if (sg.getSequences().size() == 1)
    {
      selectedSequence = sg.getSequences().get(0).getName();
      selectedEP = sg.getStartRes() + 1;
    } else {
      return;
    } 

    AlignmentI al = av.getAlignment();
    al.deleteAllGroups();
    al.deleteAllAnnotations(false);
    
    int epAsRes = convertEpToRes(selectedSequence, selectedEP) -1;

    for (String iteratingDomain : wholeGroupVariants.keySet())
    {
      AlignFrame[] afs = Desktop.getAlignFrames();
      int domainAFindex = 0;
      
      for (AlignFrame af : afs)
      {
        if (af.getTitle().equals(this.foundDomainGroup))
          break;
        domainAFindex++;
      }

      TreeMap<Integer, String[]> variantResiduesOfDomain = wholeGroupVariants.get(iteratingDomain);
      HashMap<Integer, char[][]> varUnsorted = new HashMap<Integer, char[][]>();

      //colour selected column
      if (iteratingDomain.equals(selectedSequence))
      {
        if (variantResiduesOfDomain.containsKey(epAsRes))
        {
          char[][] savs = new char[variantResiduesOfDomain.get(epAsRes).length][2];
          int i = 0;
          for (String s : variantResiduesOfDomain.get(epAsRes))     // A,B
          {
            char[] aaChange = new char[]{s.charAt(0), s.charAt(2)};
            savs[i++] = aaChange;
          }
          varUnsorted.put(selectedEP, savs);
        } else {
          varUnsorted.put(selectedEP, new char[0][0]);
        }
        colourVariants(afs[domainAFindex], selectedSequence, varUnsorted, true, 1);
        //continue;
      }
      // colour rest
      varUnsorted.clear();
      for (int key : variantResiduesOfDomain.keySet())
      {
        if (variantResiduesOfDomain.containsKey(key))
        {
          char[][] savs = new char[variantResiduesOfDomain.get(key).length][2];
          int i = 0;
          for (String s : variantResiduesOfDomain.get(key))     // A,B
          {
            char[] aaChange = new char[]{s.charAt(0), s.charAt(2)};
            savs[i++] = aaChange;
          }
          varUnsorted.put(key, savs);
        }
      }
      varUnsorted = convertResToEp(iteratingDomain, varUnsorted);
      colourVariants(afs[domainAFindex], iteratingDomain, varUnsorted);
    }
    if (RepeatingVariance.isCovered(al))
    {
      al.addAnnotation(RepeatingVariance.getAnnotation(al), 0);
    }
    
    if (activeJmols.containsKey(selectedSequence))
    {
      activeJmols.get(selectedSequence).setSelectedResidue(epAsRes, true);
    }
  }
  
  /**
   * recalcs the analysis for the selected residue and shows a new summary window
   * @param sg
   * @param av
   */
  public void recalc(SequenceGroup sg, AlignmentViewport av)
  {
    if (sg.getSequences().size() == 1)
    {
      selectedSequence = sg.getSequences().get(0).getName();
      selectedEP = sg.getStartRes() + 1;
    } else {
      return;
    } 


    // copying code from gui/PairwiseAlignPanel
    SequenceI[] forAlignment = new SequenceI[2];
    forAlignment[0] = protSeq;
    forAlignment[1] = sg.getSequences().get(0);
    
    String[] seqStrings = new String[2];
    seqStrings[0] = forAlignment[0].getSequenceAsString();
    seqStrings[1] = forAlignment[1].getSequenceAsString();

    AlignSeq as = new AlignSeq(forAlignment[0], seqStrings[0], forAlignment[1], seqStrings[1], AlignSeq.PEP); // align the 2 sequences
    as.calcScoreMatrix();
    as.traceAlignment();
    as.scoreAlignment();
    
    residue = as.getSeq1Start() + convertEpToRes(selectedSequence, selectedEP) -2;  // both base 1, need to be base 0
    as = null;

    produceSummary(selectedSequence);
    
    updateFocus(sg, av);
  }
  
  /**
   * remove all sequences from proteinViewport and geneViewport that are not the reference sequence
   */
  private void clean()
  {
    for (SequenceI seq : proteinViewport.getAlignment().getSequencesArray())
    {
      if (!seq.getName().equals(protSeqName))
        proteinViewport.getAlignment().deleteSequence(seq);
    }
    for (SequenceI seq : geneViewport.getAlignment().getSequencesArray())
    {
      if (!seq.getName().equals(geneSeq.getName())) // == protSeqName
        geneViewport.getAlignment().deleteSequence(seq);
    }
  }
  
  /**
   * TODO
   * maybe outsource this to a separate class to allow for usage outside of this analyssi
   * display a pie chart with the NFs at the position
   */
  private void pieChart()
  {
    String title = String.format("Frequency Distribution of %s at EP %d", foundDomainGroup, selectedEP);
    pie = new PieChartBuilder().width(400).height(300).title(title).theme(ChartTheme.GGPlot2).build();
    
    Color[] seriesColours = new Color[nfAtThisPosition.keySet().size()];
    
    pie.getStyler().setLegendVisible(false);
    pie.getStyler().setPlotContentSize(0.8);
    pie.getStyler().setLabelType(LabelType.NameAndPercentage);
    pie.getStyler().setLabelsDistance(1.12);
    pie.getStyler().setLabelsFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    pie.getStyler().setPlotBackgroundColor(Color.white);
    //pie.getStyler().setStartAngleInDegrees(90);
    
    int i = 0;
    for (char aa : nfAtThisPosition.keySet())
    {
      pie.addSeries(Character.toString(aa), nfAtThisPosition.get(aa));
 System.out.println(String.format("ocean at %d (%c)", mapAAtoColourIndex.get(aa), aa));
      seriesColours[i++] = referenceColourScheme[mapAAtoColourIndex.get(aa)];
    }

    pie.getStyler().setSeriesColors(seriesColours);
    
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        frame = new JFrame(selectedSequence);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        piePanel = new XChartPanel<PieChart>(pie);
        frame.add(piePanel, BorderLayout.CENTER);
        
        //JLabel label = new JLabel("alsjdlf", SwingConstants.CENTER);
        //frame.add(label, BorderLayout.SOUTH);
        
        frame.pack();
        frame.setVisible(true);
      }
    });
    
    //new SwingWrapper(pie).displayChart();
  }
  
}
