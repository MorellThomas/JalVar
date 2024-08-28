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
package jalview.io;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.util.Comparison;
import jalview.util.MessageManager;

/**
 * helper class dealing with the reference file and holding all necessary information
 * implements serializable to be able to safe it as file
 * the gene sequence for EquivalentPositions and the protein sequence for Analysis have to be called the same, refrence is called the same
 * 
 * required by Analysis, EquivalentPositions and NaturalFrequencies
 */
public class EpReferenceFile 
  implements Serializable
{
  private static final long serialVersionUID = 6529685098267757690L;  // need this to be able to load the file again
  
  public static final String REFERENCE_PATH = Files.exists(System.getProperty("user.home") + "/JalVar/") ? System.getProperty("user.home") + "/JalVar/" : "sample/;
  
  public static final char GAP_CHARACTER = '-';
  
  public static final char[] ALLAMINOACIDS = new char[]{'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', '-', 'B', 'Z', 'X', '*'};
  
  private final String path;
  
  /*
   * index of first protein sequence corresponding to a new gene sequence
   * determined either by 
   *    - only one gene existing -> all proteins belong to that -> geneSequenceMapping = [0]
   *    - names of protein sequences contain the name of the gene they belong to -> custom index mapping
   *    - names don't match, as many genes as proteins -> = [0, 1, 2, ... length-1]
   *    - names don't match, unequal amount -> error
   */
  private int[] geneSequenceMapping;
  
  private HashMap<String, Boolean> isReverse;
  
  private HashMap<String, char[]> geneSequence;
  
  private HashMap<String, int[]> genomicPositions;
  
  private HashMap<String, LinkedList<HashMap<Character, int[]>>> domain;  //epgp conversion // int[] = [EP, GP1,GP2,GP3]
  
  private HashMap<String, char[]> domainWithGaps;  // all domains aligned, used to display the domain group

  private HashMap<String, Integer> domainOffset;  // domains with their frame offset
  
  private HashMap<String, LinkedHashSet<String>> domainGroups;
  
  private HashMap<String[], LinkedList<HashMap<Character, float[]>>> naturalFrequency;  // [seqNames], <AA, [%, total]>
  
  public EpReferenceFile(String filePath)
  {
    this.path = filePath;
  }
  
  public void saveReference() throws IOException, ClassNotFoundException
  {
    File file = new File(this.path);
    file.createNewFile();
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
    oos.writeObject(this);
    oos.flush();
    oos.close();
  }
  
  public static EpReferenceFile loadReference(String path) throws IOException, ClassNotFoundException
  {
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
    EpReferenceFile erf = (EpReferenceFile) ois.readObject();
    ois.close();
    
    return erf;
  }
  
  public boolean doVariant()
  {
    return (genomicPositions != null) || (geneSequence != null || geneSequence.size() == 0);
  }

  public void setGenomicPositions(HashMap<String, int[]> gP)
  {
    this.genomicPositions = gP;
  }
  
  public void setGeneSequence(HashMap<String, char[]> gS)
  {
    this.geneSequence = gS;
  }
  
  public void setDomain(HashMap<String, LinkedList<HashMap<Character, int[]>>> domain)
  {
    this.domain = domain;
  }
  
  public void setNaturalFrequency(HashMap<String[], LinkedList<HashMap<Character, float[]>>> nF)
  {
    this.naturalFrequency = nF;
  }
  
  public HashMap<String[], LinkedList<HashMap<Character, float[]>>> getNaturalFrequency()
  {
    return this.naturalFrequency;
  }
  
  public HashMap<String, LinkedList<HashMap<Character, int[]>>> getDomain()
  {
    return this.domain;
  }
  
  public HashMap<String, char[]> getGeneSequence()
  {
    return this.geneSequence;
  }
  
  public HashMap<String, int[]> getGenomicPositions()
  {
    return this.genomicPositions;
  }
  
  public String getPath()
  {
    return this.path;
  }
  
  public void setDomainGroups(HashMap<String, LinkedHashSet<String>> dGroup)
  {
    this.domainGroups = dGroup;
  }
  
  public HashMap<String, LinkedHashSet<String>> getDomainGroups()
  {
    return this.domainGroups;
  }
  
  public void setAlignedDomains(HashMap<String, char[]> alDom)
  {
    this.domainWithGaps = alDom;
  }
  
  public HashMap<String, char[]> getAlignedDomains()
  {
    return this.domainWithGaps;
  }
  
  public void setReverse(HashMap<String, Boolean> rev)
  {
    this.isReverse = rev;
  }
  
  public HashMap<String, Boolean> getReverse()
  {
    return this.isReverse;
  }
  
  public void setDomainOffset(HashMap<String, Integer> dof)
  {
    this.domainOffset = dof;
  }
  
  public HashMap<String, Integer> getDomainOffset()
  {
    return this.domainOffset;
  }
  
  public void delete()
  {
    File file = new File(path);
    file.delete();
  }
  
  /*
   * returns the reference file (without prepended reference path) that contains ALL sequences in seqs
   */
  public static String findFittingReference(SequenceI seq) throws ClassNotFoundException, IOException
  {
    return findFittingReference(new SequenceI[]{seq}, false);
  }
  public static String findFittingReference(SequenceI seq, boolean doVar) throws ClassNotFoundException, IOException
  {
    return findFittingReference(new SequenceI[]{seq}, doVar);
  }
  public static String findFittingReference(SequenceI[] seqs) throws ClassNotFoundException, IOException
  {
    return findFittingReference(seqs, false);
  }
  public static String findFittingReference(SequenceI[] seqs, boolean doVar) throws ClassNotFoundException, IOException
  {
    HashSet<String> allReferences = new HashSet<String>();
    String referenceFileName = null;
    File[] files = new File(EpReferenceFile.REFERENCE_PATH).listFiles();
    for (File file : files) // look for correct sequence file
    {
      if (file.getName().contains(".ref"))  // load each file that ends with ref
      {
        EpReferenceFile erf = EpReferenceFile.loadReference(String.format("%s%s", EpReferenceFile.REFERENCE_PATH, file.getName()));

        //skip ref file if does not contain variants but it should
        if (doVar && !erf.doVariant())
          continue;
        
        HashMap<String, LinkedList<HashMap<Character, int[]>>> domain = erf.getDomain();
        boolean skip = false;
        for (SequenceI seq : seqs)
        {
          //remove gaps from sequence
          char[] seq1 = seq.getUngappedSequence();
          
          float highest = -1;
          for (String seq2Name : domain.keySet())
          {
            LinkedList<HashMap<Character, int[]>> list = domain.get(seq2Name);
            char[] seq2 = new char[list.size()];
            for (int i = 0; i < list.size(); i++)
            {
              seq2[i] = (char) list.get(i).keySet().toArray()[0];
            }
            seq2 = new Sequence("s2", seq2, 0, seq2.length-1).getUngappedSequence();
            float pid = new String(seq1).contains(new String(seq2)) ? 100 : Comparison.compare(new Sequence("s1", seq1, 0, seq1.length-1), new Sequence("s2", seq2, 0, seq2.length-1));
            highest = highest < pid ? pid : highest;
            if (highest > 90)
            {
              break;
            }
          }
          if (highest < 90)
          {
              skip = true;
              break;
          }
        }
        if (!skip)
          allReferences.add(file.getName());
      }
    }
    
    if (allReferences.size() < 2)
    {
      referenceFileName = allReferences.size() == 1 ? (String) allReferences.toArray()[0] : null;
    } else {  // create an option dialog showing all found fitting references, the chosen one will be used
      referenceFileName = createReferenceDialog(allReferences, "label.choose_reference");
    }
    
    if (referenceFileName == null)
      JvOptionPane.showInternalMessageDialog(Desktop.desktop, "No reference file found.", "No Reference Error", JvOptionPane.ERROR_MESSAGE);

    return referenceFileName;
  }
  
  public static String selectReference(String titleLabel) throws ClassNotFoundException, IOException
  {
    
    HashSet<String> allReferences = new HashSet<String>();
    
    File[] files = new File(EpReferenceFile.REFERENCE_PATH).listFiles();
    for (File file : files) // look for correct sequence file
    {
      if (file.getName().contains(".ref"))  // load each file that ends with ref
      {
        allReferences.add(file.getName());
      }
    }
    
    if (allReferences.size() == 0)
      allReferences.add("None");
    
    return createReferenceDialog(allReferences, titleLabel);
  }
  
  private static String createReferenceDialog(HashSet<String> allRefs, String titleLabel) throws ClassNotFoundException, IOException
  {
    Object[] allRefsArray = allRefs.toArray();
    String[] allDisplayOptions = new String[allRefsArray.length];
    for (int i = 0; i < allRefsArray.length; i++)
    {
      String ref = (String) allRefsArray[i];
      
      if (ref.equals("None"))
      {
        allDisplayOptions[i] = ref;
        continue;
      }
      
      EpReferenceFile erp = EpReferenceFile.loadReference(String.format("%s%s", EpReferenceFile.REFERENCE_PATH, ref));
      HashMap<String, LinkedHashSet<String>> groups = erp.getDomainGroups();
      int nGroups = groups.size();
      Object[] groupNames = groups.keySet().toArray();
      String firstGroup = (String) groupNames[0];
      String lastGroup = (String) groupNames[groupNames.length - 1];
      allDisplayOptions[i] = String.format("%s: %d group(s) (%s, ...)", ref, nGroups, firstGroup, lastGroup);
    }

    JComboBox optionDialog = new JComboBox(allDisplayOptions);
    optionDialog.setSelectedIndex(0);
    int closed = JvOptionPane.showInternalConfirmDialog(Desktop.desktop, optionDialog, MessageManager.getString(titleLabel), JOptionPane.OK_CANCEL_OPTION);
    int refIndex = optionDialog.getSelectedIndex();
    if (closed != JOptionPane.CANCEL_OPTION && closed != JOptionPane.CLOSED_OPTION)  //refIndex in right to left order
    {
      return (String) allRefsArray[refIndex];
    } else {
      return null;
    }
  }

  public String chooseDomainGroup()
  {
    Object[] allDomainGroups = domainGroups.keySet().toArray();
    String[] allDisplayOptions = new String[domainGroups.size()];
    for (int i = 0; i < allDomainGroups.length; i++)
    {
      String domainGroup = (String) allDomainGroups[i];
      allDisplayOptions[i] = domainGroup;
    }

    JComboBox optionDialog = new JComboBox(allDisplayOptions);
    optionDialog.setSelectedIndex(0);
    int closed = JvOptionPane.showInternalConfirmDialog(Desktop.desktop, optionDialog, MessageManager.getString("label.choose_msa"), JOptionPane.OK_CANCEL_OPTION);
    int refIndex = optionDialog.getSelectedIndex();
    if (closed != JOptionPane.CANCEL_OPTION && closed != JOptionPane.CLOSED_OPTION)  //refIndex in right to left order
    {
      return (String) allDomainGroups[refIndex];
    } else {
      return null;
    }
  }
  
  public SequenceI getGaplessSequence(String name)
  {
  //private HashMap<String, LinkedList<HashMap<Character, int[]>>> domain;  //epgp conversion // int[] = [EP, GP1,GP2,GP3]
    char[] seq = new char[domain.get(name).size()];
    int i = 0;
    for (HashMap<Character, int[]> map : domain.get(name))
    {
      seq[i++] = (char) map.keySet().toArray()[0];
    }
    
    return new Sequence(name, seq, 1, seq.length);
  }
  
  /*
   * construct a reference file name from all gene sequence names
   * assumes a sequence array of protein sequences followed by gene sequence names
   * uses the gene sequence names for the reference file name
   */
  public static String constructRefFileName(SequenceI[] seqs, int proteins, int genes)
  {
    StringBuilder refString = new StringBuilder();
    for (int g = proteins; g < proteins + genes; g++)
    {
      refString.append(seqs[g].getName());
      if (g < proteins + genes - 1)
        refString.append("-");
    }
    
    return refString.toString();
  }
  
  public int[] getGeneSequenceMapping()
  {
    return geneSequenceMapping;
  }
  
  public void setGeneSequenceMapping(int[] map)
  {
    this.geneSequenceMapping = map;
  }
}
