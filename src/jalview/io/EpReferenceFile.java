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
  
  public static final String REFERENCE_PATH = System.getProperty("user.home") + "/.config/JalviewSNV/";
  
  private final String path;
  
  private boolean isReverse;
  
  private int[] genomicPositions;
  
  private char[] geneSequence;
  
  private HashMap<String, LinkedList<HashMap<Character, int[]>>> domain;  //epgp conversion // int[] = [EP, GP1,GP2,GP3]
  
  private HashMap<String, char[]> domainWithGaps;  // all domains aligned, used to display the domain group

  private HashMap<String, Integer> domainOffset;  // domains with their frame offset
  
  private HashMap<String, LinkedHashSet<String>> domainGroups;
  
  private HashMap<String[], LinkedList<HashMap<Character, Float>>> naturalFrequency;
  
  public EpReferenceFile(String filePath)
  {
    this.path = filePath;
  }
  
  public void saveReference() throws IOException, ClassNotFoundException
  {
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.path));
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
  
  public void setGenomicPositions(int[] gP)
  {
    this.genomicPositions = gP;
  }
  
  public void setGeneSequence(char[] gS)
  {
    this.geneSequence = gS;
  }
  
  public void setDomain(HashMap<String, LinkedList<HashMap<Character, int[]>>> domain)
  {
    this.domain = domain;
  }
  
  public void setNaturalFrequency(HashMap<String[], LinkedList<HashMap<Character, Float>>> nF)
  {
    this.naturalFrequency = nF;
  }
  
  public HashMap<String[], LinkedList<HashMap<Character, Float>>> getNaturalFrequency()
  {
    return this.naturalFrequency;
  }
  
  public HashMap<String, LinkedList<HashMap<Character, int[]>>> getDomain()
  {
    return this.domain;
  }
  
  public char[] getGeneSequence()
  {
    return this.geneSequence;
  }
  
  public int[] getGenomicPositions()
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
  
  public void setReverse(boolean rev)
  {
    this.isReverse = rev;
  }
  
  public boolean getReverse()
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

}