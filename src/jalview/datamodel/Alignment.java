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
package jalview.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jalview.analysis.AlignmentUtils;
import jalview.datamodel.AlignedCodonFrame.SequenceToSequenceMapping;
import jalview.io.FastaFile;
import jalview.util.Comparison;
import jalview.util.LinkedIdentityHashSet;
import jalview.util.MessageManager;

/**
 * Data structure to hold and manipulate a multiple sequence alignment
 */
/**
 * @author JimP
 * 
 */
public class Alignment implements AlignmentI, AutoCloseable
{
  private Alignment dataset;

  private List<SequenceI> sequences;

  protected List<SequenceGroup> groups;

  protected char gapCharacter = '-';

  private boolean nucleotide = true;

  public boolean hasRNAStructure = false;

  public AlignmentAnnotation[] annotations;

  HiddenSequences hiddenSequences;

  HiddenColumns hiddenCols;

  public Hashtable alignmentProperties;

  private List<AlignedCodonFrame> codonFrameList;

  private void initAlignment(SequenceI[] seqs)
  {
    groups = Collections.synchronizedList(new ArrayList<SequenceGroup>());
    hiddenSequences = new HiddenSequences(this);
    hiddenCols = new HiddenColumns();
    codonFrameList = new ArrayList<>();

    nucleotide = Comparison.isNucleotide(seqs);

    sequences = Collections.synchronizedList(new ArrayList<SequenceI>());

    for (int i = 0; i < seqs.length; i++)
    {
      sequences.add(seqs[i]);
    }

  }

  /**
   * Make a 'copy' alignment - sequences have new copies of features and
   * annotations, but share the original dataset sequences.
   */
  public Alignment(AlignmentI al)
  {
    SequenceI[] seqs = al.getSequencesArray();
    for (int i = 0; i < seqs.length; i++)
    {
      seqs[i] = new Sequence(seqs[i]);
    }

    initAlignment(seqs);

    /*
     * Share the same dataset sequence mappings (if any). 
     */
    if (dataset == null && al.getDataset() == null)
    {
      this.setCodonFrames(al.getCodonFrames());
    }
  }

  /**
   * Make an alignment from an array of Sequences.
   * 
   * @param sequences
   */
  public Alignment(SequenceI[] seqs)
  {
    initAlignment(seqs);
  }

  /**
   * Make a new alignment from an array of SeqCigars
   * 
   * @param seqs
   *          SeqCigar[]
   */
  public Alignment(SeqCigar[] alseqs)
  {
    SequenceI[] seqs = SeqCigar.createAlignmentSequences(alseqs,
            gapCharacter, new HiddenColumns(), null);
    initAlignment(seqs);
  }

  /**
   * Make a new alignment from an CigarArray JBPNote - can only do this when
   * compactAlignment does not contain hidden regions. JBPNote - must also check
   * that compactAlignment resolves to a set of SeqCigars - or construct them
   * appropriately.
   * 
   * @param compactAlignment
   *          CigarArray
   */
  public static AlignmentI createAlignment(CigarArray compactAlignment)
  {
    throw new Error(MessageManager
            .getString("error.alignment_cigararray_not_implemented"));
    // this(compactAlignment.refCigars);
  }

  @Override
  public List<SequenceI> getSequences()
  {
    return sequences;
  }

  @Override
  public List<SequenceI> getSequences(
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
    // TODO: in jalview 2.8 we don't do anything with hiddenreps - fix design to
    // work on this.
    return sequences;
  }

  @Override
  public SequenceI[] getSequencesArray()
  {
    if (sequences == null)
    {
      return null;
    }
    synchronized (sequences)
    {
      return sequences.toArray(new SequenceI[sequences.size()]);
    }
  }

  /**
   * Returns a map of lists of sequences keyed by sequence name.
   * 
   * @return
   */
  @Override
  public Map<String, List<SequenceI>> getSequencesByName()
  {
    return AlignmentUtils.getSequencesByName(this);
  }

  @Override
  public SequenceI getSequenceAt(int i)
  {
    synchronized (sequences)
    {

      if (i > -1 && i < sequences.size())
      {
        return sequences.get(i);
      }
    }

    return null;
  }

  @Override
  public SequenceI getSequenceAtAbsoluteIndex(int i)
  {
    SequenceI seq = null;
    if (getHiddenSequences().getSize() > 0)
    {
      seq = getHiddenSequences().getHiddenSequence(i);
      if (seq == null)
      {
        // didn't find the sequence in the hidden sequences, get it from the
        // alignment
        int index = getHiddenSequences().findIndexWithoutHiddenSeqs(i);
        seq = getSequenceAt(index);
      }
    }
    else
    {
      seq = getSequenceAt(i);
    }
    return seq;
  }

  /**
   * Adds a sequence to the alignment. Recalculates maxLength and size. Note
   * this currently does not recalculate whether or not the alignment is
   * nucleotide, so mixed alignments may have undefined behaviour.
   * 
   * @param snew
   */
  @Override
  public void addSequence(SequenceI snew)
  {
    if (dataset != null)
    {

      // maintain dataset integrity
      SequenceI dsseq = snew.getDatasetSequence();
      if (dsseq == null)
      {
        // derive new sequence
        SequenceI adding = snew.deriveSequence();
        snew = adding;
        dsseq = snew.getDatasetSequence();
      }
      if (getDataset().findIndex(dsseq) == -1)
      {
        getDataset().addSequence(dsseq);
      }

    }
    if (sequences == null)
    {
      initAlignment(new SequenceI[] { snew });
    }
    else
    {
      synchronized (sequences)
      {
        sequences.add(snew);
      }
    }
    if (hiddenSequences != null)
    {
      hiddenSequences.adjustHeightSequenceAdded();
    }
  }

  @Override
  public SequenceI replaceSequenceAt(int i, SequenceI snew)
  {
    synchronized (sequences)
    {
      if (sequences.size() > i)
      {
        return sequences.set(i, snew);

      }
      else
      {
        sequences.add(snew);
        hiddenSequences.adjustHeightSequenceAdded();
      }
      return null;
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public List<SequenceGroup> getGroups()
  {
    return groups;
  }

  @Override
  public void close()
  {
    if (getDataset() != null)
    {
      try
      {
        getDataset().removeAlignmentRef();
      } catch (Throwable e)
      {
        e.printStackTrace();
      }
    }

    nullReferences();
  }

  /**
   * Defensively nulls out references in case this object is not garbage
   * collected
   */
  void nullReferences()
  {
    dataset = null;
    sequences = null;
    groups = null;
    annotations = null;
    hiddenSequences = null;
  }

  /**
   * decrement the alignmentRefs counter by one and null references if it goes
   * to zero.
   * 
   * @throws Throwable
   */
  private void removeAlignmentRef() throws Throwable
  {
    if (--alignmentRefs == 0)
    {
      nullReferences();
    }
  }

  @Override
  public void deleteSequence(SequenceI s)
  {
    synchronized (sequences)
    {
      deleteSequence(findIndex(s));
    }
  }

  @Override
  public void deleteSequence(int i)
  {
    synchronized (sequences)
    {
      if (i > -1 && i < getHeight())
      {
        sequences.remove(i);
        hiddenSequences.adjustHeightSequenceDeleted(i);
      }
    }
  }

  @Override
  public void deleteHiddenSequence(int i)
  {
    synchronized (sequences)
    {
      if (i > -1 && i < getHeight())
      {
        sequences.remove(i);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#findGroup(jalview.datamodel.SequenceI)
   */
  @Override
  public SequenceGroup findGroup(SequenceI seq, int position)
  {
    synchronized (groups)
    {
      for (SequenceGroup sg : groups)
      {
        if (sg.getSequences(null).contains(seq))
        {
          if (position >= sg.getStartRes() && position <= sg.getEndRes())
          {
            return sg;
          }
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.AlignmentI#findAllGroups(jalview.datamodel.SequenceI)
   */
  @Override
  public SequenceGroup[] findAllGroups(SequenceI s)
  {
    ArrayList<SequenceGroup> temp = new ArrayList<>();

    synchronized (groups)
    {
      int gSize = groups.size();
      for (int i = 0; i < gSize; i++)
      {
        SequenceGroup sg = groups.get(i);
        if (sg == null || sg.getSequences() == null)
        {
          this.deleteGroup(sg);
          gSize--;
          continue;
        }

        if (sg.getSequences().contains(s))
        {
          temp.add(sg);
        }
      }
    }
    SequenceGroup[] ret = new SequenceGroup[temp.size()];
    return temp.toArray(ret);
  }

  /**    */
  @Override
  public void addGroup(SequenceGroup sg)
  {
    synchronized (groups)
    {
      if (!groups.contains(sg))
      {
        if (hiddenSequences.getSize() > 0)
        {
          int i, iSize = sg.getSize();
          for (i = 0; i < iSize; i++)
          {
            if (!sequences.contains(sg.getSequenceAt(i)))
            {
              sg.deleteSequence(sg.getSequenceAt(i), false);
              iSize--;
              i--;
            }
          }

          if (sg.getSize() < 1)
          {
            return;
          }
        }
        sg.setContext(this, true);
        groups.add(sg);
      }
    }
  }

  /**
   * remove any annotation that references gp
   * 
   * @param gp
   *          (if null, removes all group associated annotation)
   */
  private void removeAnnotationForGroup(SequenceGroup gp)
  {
    if (annotations == null || annotations.length == 0)
    {
      return;
    }
    // remove annotation very quickly
    AlignmentAnnotation[] t,
            todelete = new AlignmentAnnotation[annotations.length],
            tokeep = new AlignmentAnnotation[annotations.length];
    int i, p, k;
    if (gp == null)
    {
      for (i = 0, p = 0, k = 0; i < annotations.length; i++)
      {
        if (annotations[i].groupRef != null)
        {
          todelete[p++] = annotations[i];
        }
        else
        {
          tokeep[k++] = annotations[i];
        }
      }
    }
    else
    {
      for (i = 0, p = 0, k = 0; i < annotations.length; i++)
      {
        if (annotations[i].groupRef == gp)
        {
          todelete[p++] = annotations[i];
        }
        else
        {
          tokeep[k++] = annotations[i];
        }
      }
    }
    if (p > 0)
    {
      // clear out the group associated annotation.
      for (i = 0; i < p; i++)
      {
        unhookAnnotation(todelete[i]);
        todelete[i] = null;
      }
      t = new AlignmentAnnotation[k];
      for (i = 0; i < k; i++)
      {
        t[i] = tokeep[i];
      }
      annotations = t;
    }
  }

  @Override
  public void deleteAllGroups()
  {
    synchronized (groups)
    {
      if (annotations != null)
      {
        removeAnnotationForGroup(null);
      }
      for (SequenceGroup sg : groups)
      {
        sg.setContext(null, false);
      }
      groups.clear();
    }
  }

  /**    */
  @Override
  public void deleteGroup(SequenceGroup g)
  {
    synchronized (groups)
    {
      if (groups.contains(g))
      {
        removeAnnotationForGroup(g);
        groups.remove(g);
        g.setContext(null, false);
      }
    }
  }

  /**    */
  @Override
  public SequenceI findName(String name)
  {
    return findName(name, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#findName(java.lang.String, boolean)
   */
  @Override
  public SequenceI findName(String token, boolean b)
  {
    return findName(null, token, b);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#findName(SequenceI, java.lang.String,
   * boolean)
   */
  @Override
  public SequenceI findName(SequenceI startAfter, String token, boolean b)
  {

    int i = 0;
    SequenceI sq = null;
    String sqname = null;
    int nseq = sequences.size();
    if (startAfter != null)
    {
      // try to find the sequence in the alignment
      boolean matched = false;
      while (i < nseq)
      {
        if (getSequenceAt(i++) == startAfter)
        {
          matched = true;
          break;
        }
      }
      if (!matched)
      {
        i = 0;
      }
    }
    while (i < nseq)
    {
      sq = getSequenceAt(i);
      sqname = sq.getName();
      if (sqname.equals(token) // exact match
              || (b && // allow imperfect matches - case varies
                      (sqname.equalsIgnoreCase(token))))
      {
        return getSequenceAt(i);
      }

      i++;
    }

    return null;
  }

  @Override
  public SequenceI[] findSequenceMatch(String name)
  {
    Vector matches = new Vector();
    int i = 0;

    while (i < sequences.size())
    {
      if (getSequenceAt(i).getName().equals(name))
      {
        matches.addElement(getSequenceAt(i));
      }
      i++;
    }

    SequenceI[] result = new SequenceI[matches.size()];
    for (i = 0; i < result.length; i++)
    {
      result[i] = (SequenceI) matches.elementAt(i);
    }

    return result;

  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#findIndex(jalview.datamodel.SequenceI)
   */
  @Override
  public int findIndex(SequenceI s)
  {
    int i = 0;

    while (i < sequences.size())
    {
      if (s == getSequenceAt(i))
      {
        return i;
      }

      i++;
    }

    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.AlignmentI#findIndex(jalview.datamodel.SearchResults)
   */
  @Override
  public int findIndex(SearchResultsI results)
  {
    int i = 0;

    while (i < sequences.size())
    {
      if (results.involvesSequence(getSequenceAt(i)))
      {
        return i;
      }
      i++;
    }
    return -1;
  }

  @Override
  public int getHeight()
  {
    return sequences.size();
  }

  @Override
  public int getAbsoluteHeight()
  {
    return sequences.size() + getHiddenSequences().getSize();
  }

  @Override
  public int getWidth()
  {
    int maxLength = -1;

    for (int i = 0; i < sequences.size(); i++)
    {
      maxLength = Math.max(maxLength, getSequenceAt(i).getLength());
    }
    return maxLength;
  }

  @Override
  public int getVisibleWidth()
  {
    int w = getWidth();
    if (hiddenCols != null)
    {
      w -= hiddenCols.getSize();
    }
    return w;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param gc
   *          DOCUMENT ME!
   */
  @Override
  public void setGapCharacter(char gc)
  {
    gapCharacter = gc;
    synchronized (sequences)
    {
      for (SequenceI seq : sequences)
      {
        seq.setSequence(seq.getSequenceAsString().replace('.', gc)
                .replace('-', gc).replace(' ', gc));
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public char getGapCharacter()
  {
    return gapCharacter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#isAligned()
   */
  @Override
  public boolean isAligned()
  {
    return isAligned(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AlignmentI#isAligned(boolean)
   */
  @Override
  public boolean isAligned(boolean includeHidden)
  {
    int width = getWidth();
    if (hiddenSequences == null || hiddenSequences.getSize() == 0)
    {
      includeHidden = true; // no hidden sequences to check against.
    }
    for (int i = 0; i < sequences.size(); i++)
    {
      if (includeHidden || !hiddenSequences.isHidden(getSequenceAt(i)))
      {
        if (getSequenceAt(i).getLength() != width)
        {
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public boolean isHidden(int alignmentIndex)
  {
    return (getHiddenSequences().getHiddenSequence(alignmentIndex) != null);
  }

  /**
   * Delete all annotations, including auto-calculated if the flag is set true.
   * Returns true if at least one annotation was deleted, else false.
   * 
   * @param includingAutoCalculated
   * @return
   */
  @Override
  public boolean deleteAllAnnotations(boolean includingAutoCalculated)
  {
    boolean result = false;
    for (AlignmentAnnotation alan : getAlignmentAnnotation())
    {
      if (!alan.autoCalculated || includingAutoCalculated)
      {
        deleteAnnotation(alan);
        result = true;
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.datamodel.AlignmentI#deleteAnnotation(jalview.datamodel.
   * AlignmentAnnotation)
   */
  @Override
  public boolean deleteAnnotation(AlignmentAnnotation aa)
  {
    return deleteAnnotation(aa, true);
  }

  @Override
  public boolean deleteAnnotation(AlignmentAnnotation aa, boolean unhook)
  {
    int aSize = 1;

    if (annotations != null)
    {
      aSize = annotations.length;
    }

    if (aSize < 1)
    {
      return false;
    }

    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize - 1];

    boolean swap = false;
    int tIndex = 0;

    for (int i = 0; i < aSize; i++)
    {
      if (annotations[i] == aa)
      {
        swap = true;
        continue;
      }
      if (tIndex < temp.length)
      {
        temp[tIndex++] = annotations[i];
      }
    }

    if (swap)
    {
      annotations = temp;
      if (unhook)
      {
        unhookAnnotation(aa);
      }
    }
    return swap;
  }

  /**
   * remove any object references associated with this annotation
   * 
   * @param aa
   */
  private void unhookAnnotation(AlignmentAnnotation aa)
  {
    if (aa.sequenceRef != null)
    {
      aa.sequenceRef.removeAlignmentAnnotation(aa);
    }
    if (aa.groupRef != null)
    {
      // probably need to do more here in the future (post 2.5.0)
      aa.groupRef = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.datamodel.AlignmentI#addAnnotation(jalview.datamodel.
   * AlignmentAnnotation)
   */
  @Override
  public void addAnnotation(AlignmentAnnotation aa)
  {
    addAnnotation(aa, -1);
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.datamodel.AlignmentI#addAnnotation(jalview.datamodel.
   * AlignmentAnnotation, int)
   */
  @Override
  public void addAnnotation(AlignmentAnnotation aa, int pos)
  {
    if (aa.getRNAStruc() != null)
    {
      hasRNAStructure = true;
    }

    int aSize = 1;
    if (annotations != null)
    {
      aSize = annotations.length + 1;
    }

    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize];
    int i = 0;
    if (pos == -1 || pos >= aSize)
    {
      temp[aSize - 1] = aa;
    }
    else
    {
      temp[pos] = aa;
    }
    if (aSize > 1)
    {
      int p = 0;
      for (i = 0; i < (aSize - 1); i++, p++)
      {
        if (p == pos)
        {
          p++;
        }
        if (p < temp.length)
        {
          temp[p] = annotations[i];
        }
      }
    }

    annotations = temp;
  }

  @Override
  public void setAnnotationIndex(AlignmentAnnotation aa, int index)
  {
    if (aa == null || annotations == null || annotations.length - 1 < index)
    {
      return;
    }

    int aSize = annotations.length;
    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize];

    temp[index] = aa;

    for (int i = 0; i < aSize; i++)
    {
      if (i == index)
      {
        continue;
      }

      if (i < index)
      {
        temp[i] = annotations[i];
      }
      else
      {
        temp[i] = annotations[i - 1];
      }
    }

    annotations = temp;
  }

  @Override
  /**
   * returns all annotation on the alignment
   */
  public AlignmentAnnotation[] getAlignmentAnnotation()
  {
    return annotations;
  }

  @Override
  public boolean isNucleotide()
  {
    return nucleotide;
  }

  @Override
  public boolean hasRNAStructure()
  {
    // TODO can it happen that structure is removed from alignment?
    return hasRNAStructure;
  }

  @Override
  public void setDataset(AlignmentI data)
  {
    if (dataset == null && data == null)
    {
      createDatasetAlignment();
    }
    else if (dataset == null && data != null)
    {
      if (data == this)
      {
        throw new IllegalArgumentException("Circular dataset reference");
      }
      if (!(data instanceof Alignment))
      {
        throw new Error(
                "Implementation Error: jalview.datamodel.Alignment does not yet support other implementations of AlignmentI as its dataset reference");
      }
      dataset = (Alignment) data;
      for (int i = 0; i < getHeight(); i++)
      {
        SequenceI currentSeq = getSequenceAt(i);
        SequenceI dsq = currentSeq.getDatasetSequence();
        if (dsq == null)
        {
          dsq = currentSeq.createDatasetSequence();
          dataset.addSequence(dsq);
        }
        else
        {
          while (dsq.getDatasetSequence() != null)
          {
            dsq = dsq.getDatasetSequence();
          }
          if (dataset.findIndex(dsq) == -1)
          {
            dataset.addSequence(dsq);
          }
        }
      }
    }
    dataset.addAlignmentRef();
  }

  /**
   * add dataset sequences to seq for currentSeq and any sequences it references
   */
  private void resolveAndAddDatasetSeq(SequenceI currentSeq,
          Set<SequenceI> seqs, boolean createDatasetSequence)
  {
    SequenceI alignedSeq = currentSeq;
    if (currentSeq.getDatasetSequence() != null)
    {
      currentSeq = currentSeq.getDatasetSequence();
    }
    else
    {
      if (createDatasetSequence)
      {
        currentSeq = currentSeq.createDatasetSequence();
      }
    }

    List<SequenceI> toProcess = new ArrayList<>();
    toProcess.add(currentSeq);
    while (toProcess.size() > 0)
    {
      // use a queue ?
      SequenceI curDs = toProcess.remove(0);

      if (!seqs.add(curDs))
      {
        continue;
      }
      // iterate over database references, making sure we add forward referenced
      // sequences
      if (curDs.getDBRefs() != null)
      {
        for (DBRefEntry dbr : curDs.getDBRefs())
        {
          if (dbr.getMap() != null && dbr.getMap().getTo() != null)
          {
            if (dbr.getMap().getTo() == alignedSeq)
            {
              /*
               * update mapping to be to the newly created dataset sequence
               */
              dbr.getMap().setTo(currentSeq);
            }
            if (dbr.getMap().getTo().getDatasetSequence() != null)
            {
              throw new Error("Implementation error: Map.getTo() for dbref "
                      + dbr + " from " + curDs.getName()
                      + " is not a dataset sequence.");
            }
            // we recurse to add all forward references to dataset sequences via
            // DBRefs/etc
            toProcess.add(dbr.getMap().getTo());
          }
        }
      }
    }
  }

  /**
   * Creates a new dataset for this alignment. Can only be done once - if
   * dataset is not null this will not be performed.
   */
  public void createDatasetAlignment()
  {
    if (dataset != null)
    {
      return;
    }
    // try to avoid using SequenceI.equals at this stage, it will be expensive
    Set<SequenceI> seqs = new LinkedIdentityHashSet<>();

    for (int i = 0; i < getHeight(); i++)
    {
      SequenceI currentSeq = getSequenceAt(i);
      resolveAndAddDatasetSeq(currentSeq, seqs, true);
    }

    // verify all mappings are in dataset
    for (AlignedCodonFrame cf : codonFrameList)
    {
      for (SequenceToSequenceMapping ssm : cf.getMappings())
      {
        if (!seqs.contains(ssm.getFromSeq()))
        {
          resolveAndAddDatasetSeq(ssm.getFromSeq(), seqs, false);
        }
        if (!seqs.contains(ssm.getMapping().getTo()))
        {
          resolveAndAddDatasetSeq(ssm.getMapping().getTo(), seqs, false);
        }
      }
    }
    // finally construct dataset
    dataset = new Alignment(seqs.toArray(new SequenceI[seqs.size()]));
    // move mappings to the dataset alignment
    dataset.codonFrameList = this.codonFrameList;
    this.codonFrameList = null;
  }

  /**
   * reference count for number of alignments referencing this one.
   */
  int alignmentRefs = 0;

  /**
   * increase reference count to this alignment.
   */
  private void addAlignmentRef()
  {
    alignmentRefs++;
  }

  @Override
  public Alignment getDataset()
  {
    return dataset;
  }

  @Override
  public boolean padGaps()
  {
    boolean modified = false;

    // Remove excess gaps from the end of alignment
    int maxLength = -1;

    SequenceI current;
    int nseq = sequences.size();
    for (int i = 0; i < nseq; i++)
    {
      current = getSequenceAt(i);
      for (int j = current.getLength(); j > maxLength; j--)
      {
        if (j > maxLength
                && !jalview.util.Comparison.isGap(current.getCharAt(j)))
        {
          maxLength = j;
          break;
        }
      }
    }

    maxLength++;

    int cLength;
    for (int i = 0; i < nseq; i++)
    {
      current = getSequenceAt(i);
      cLength = current.getLength();

      if (cLength < maxLength)
      {
        current.insertCharAt(cLength, maxLength - cLength, gapCharacter);
        modified = true;
      }
      else if (current.getLength() > maxLength)
      {
        current.deleteChars(maxLength, current.getLength());
      }
    }
    return modified;
  }

  /**
   * Justify the sequences to the left or right by deleting and inserting gaps
   * before the initial residue or after the terminal residue
   * 
   * @param right
   *          true if alignment padded to right, false to justify to left
   * @return true if alignment was changed
   */
  @Override
  public boolean justify(boolean right)
  {
    boolean modified = false;

    // Remove excess gaps from the end of alignment
    int maxLength = -1;
    int ends[] = new int[sequences.size() * 2];
    SequenceI current;
    for (int i = 0; i < sequences.size(); i++)
    {
      current = getSequenceAt(i);
      // This should really be a sequence method
      ends[i * 2] = current.findIndex(current.getStart());
      ends[i * 2 + 1] = current
              .findIndex(current.getStart() + current.getLength());
      boolean hitres = false;
      for (int j = 0, rs = 0, ssiz = current.getLength(); j < ssiz; j++)
      {
        if (!jalview.util.Comparison.isGap(current.getCharAt(j)))
        {
          if (!hitres)
          {
            ends[i * 2] = j;
            hitres = true;
          }
          else
          {
            ends[i * 2 + 1] = j;
            if (j - ends[i * 2] > maxLength)
            {
              maxLength = j - ends[i * 2];
            }
          }
        }
      }
    }

    maxLength++;
    // now edit the flanking gaps to justify to either left or right
    int cLength, extent, diff;
    for (int i = 0; i < sequences.size(); i++)
    {
      current = getSequenceAt(i);

      cLength = 1 + ends[i * 2 + 1] - ends[i * 2];
      diff = maxLength - cLength; // number of gaps to indent
      extent = current.getLength();
      if (right)
      {
        // right justify
        if (extent > ends[i * 2 + 1])
        {
          current.deleteChars(ends[i * 2 + 1] + 1, extent);
          modified = true;
        }
        if (ends[i * 2] > diff)
        {
          current.deleteChars(0, ends[i * 2] - diff);
          modified = true;
        }
        else
        {
          if (ends[i * 2] < diff)
          {
            current.insertCharAt(0, diff - ends[i * 2], gapCharacter);
            modified = true;
          }
        }
      }
      else
      {
        // left justify
        if (ends[i * 2] > 0)
        {
          current.deleteChars(0, ends[i * 2]);
          modified = true;
          ends[i * 2 + 1] -= ends[i * 2];
          extent -= ends[i * 2];
        }
        if (extent > maxLength)
        {
          current.deleteChars(maxLength + 1, extent);
          modified = true;
        }
        else
        {
          if (extent < maxLength)
          {
            current.insertCharAt(extent, maxLength - extent, gapCharacter);
            modified = true;
          }
        }
      }
    }
    return modified;
  }

  @Override
  public HiddenSequences getHiddenSequences()
  {
    return hiddenSequences;
  }

  @Override
  public HiddenColumns getHiddenColumns()
  {
    return hiddenCols;
  }

  @Override
  public CigarArray getCompactAlignment()
  {
    synchronized (sequences)
    {
      SeqCigar alseqs[] = new SeqCigar[sequences.size()];
      int i = 0;
      for (SequenceI seq : sequences)
      {
        alseqs[i++] = new SeqCigar(seq);
      }
      CigarArray cal = new CigarArray(alseqs);
      cal.addOperation(CigarArray.M, getWidth());
      return cal;
    }
  }

  @Override
  public void setProperty(Object key, Object value)
  {
    if (alignmentProperties == null)
    {
      alignmentProperties = new Hashtable();
    }

    alignmentProperties.put(key, value);
  }

  @Override
  public Object getProperty(Object key)
  {
    if (alignmentProperties != null)
    {
      return alignmentProperties.get(key);
    }
    else
    {
      return null;
    }
  }

  @Override
  public Hashtable getProperties()
  {
    return alignmentProperties;
  }

  /**
   * Adds the given mapping to the stored set. Note this may be held on the
   * dataset alignment.
   */
  @Override
  public void addCodonFrame(AlignedCodonFrame codons)
  {
    List<AlignedCodonFrame> acfs = getCodonFrames();
    if (codons != null && acfs != null && !acfs.contains(codons))
    {
      acfs.add(codons);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.datamodel.AlignmentI#getCodonFrame(jalview.datamodel.SequenceI)
   */
  @Override
  public List<AlignedCodonFrame> getCodonFrame(SequenceI seq)
  {
    if (seq == null)
    {
      return null;
    }
    List<AlignedCodonFrame> cframes = new ArrayList<>();
    for (AlignedCodonFrame acf : getCodonFrames())
    {
      if (acf.involvesSequence(seq))
      {
        cframes.add(acf);
      }
    }
    return cframes;
  }

  /**
   * Sets the codon frame mappings (replacing any existing mappings). Note the
   * mappings are set on the dataset alignment instead if there is one.
   * 
   * @see jalview.datamodel.AlignmentI#setCodonFrames()
   */
  @Override
  public void setCodonFrames(List<AlignedCodonFrame> acfs)
  {
    if (dataset != null)
    {
      dataset.setCodonFrames(acfs);
    }
    else
    {
      this.codonFrameList = acfs;
    }
  }

  /**
   * Returns the set of codon frame mappings. Any changes to the returned set
   * will affect the alignment. The mappings are held on (and read from) the
   * dataset alignment if there is one.
   * 
   * @see jalview.datamodel.AlignmentI#getCodonFrames()
   */
  @Override
  public List<AlignedCodonFrame> getCodonFrames()
  {
    // TODO: Fix this method to fix failing AlignedCodonFrame tests
    // this behaviour is currently incorrect. method should return codon frames
    // for just the alignment,
    // selected from dataset
    return dataset != null ? dataset.getCodonFrames() : codonFrameList;
  }

  /**
   * Removes the given mapping from the stored set. Note that the mappings are
   * held on the dataset alignment if there is one.
   */
  @Override
  public boolean removeCodonFrame(AlignedCodonFrame codons)
  {
    List<AlignedCodonFrame> acfs = getCodonFrames();
    if (codons == null || acfs == null)
    {
      return false;
    }
    return acfs.remove(codons);
  }

  @Override
  public void append(AlignmentI toappend)
  {
    // TODO JAL-1270 needs test coverage
    // currently tested for use in jalview.gui.SequenceFetcher
    char oldc = toappend.getGapCharacter();
    boolean samegap = oldc == getGapCharacter();
    boolean hashidden = toappend.getHiddenSequences() != null
            && toappend.getHiddenSequences().hiddenSequences != null;
    // get all sequences including any hidden ones
    List<SequenceI> sqs = (hashidden)
            ? toappend.getHiddenSequences().getFullAlignment()
                    .getSequences()
            : toappend.getSequences();
    if (sqs != null)
    {
      // avoid self append deadlock by
      List<SequenceI> toappendsq = new ArrayList<>();
      synchronized (sqs)
      {
        for (SequenceI addedsq : sqs)
        {
          if (!samegap)
          {
            addedsq.replace(oldc, gapCharacter);
          }
          toappendsq.add(addedsq);
        }
      }
      for (SequenceI addedsq : toappendsq)
      {
        addSequence(addedsq);
      }
    }
    AlignmentAnnotation[] alan = toappend.getAlignmentAnnotation();
    for (int a = 0; alan != null && a < alan.length; a++)
    {
      addAnnotation(alan[a]);
    }

    // use add method
    getCodonFrames().addAll(toappend.getCodonFrames());

    List<SequenceGroup> sg = toappend.getGroups();
    if (sg != null)
    {
      for (SequenceGroup _sg : sg)
      {
        addGroup(_sg);
      }
    }
    if (toappend.getHiddenSequences() != null)
    {
      HiddenSequences hs = toappend.getHiddenSequences();
      if (hiddenSequences == null)
      {
        hiddenSequences = new HiddenSequences(this);
      }
      if (hs.hiddenSequences != null)
      {
        for (int s = 0; s < hs.hiddenSequences.length; s++)
        {
          // hide the newly appended sequence in the alignment
          if (hs.hiddenSequences[s] != null)
          {
            hiddenSequences.hideSequence(hs.hiddenSequences[s]);
          }
        }
      }
    }
    if (toappend.getProperties() != null)
    {
      // we really can't do very much here - just try to concatenate strings
      // where property collisions occur.
      Enumeration key = toappend.getProperties().keys();
      while (key.hasMoreElements())
      {
        Object k = key.nextElement();
        Object ourval = this.getProperty(k);
        Object toapprop = toappend.getProperty(k);
        if (ourval != null)
        {
          if (ourval.getClass().equals(toapprop.getClass())
                  && !ourval.equals(toapprop))
          {
            if (ourval instanceof String)
            {
              // append strings
              this.setProperty(k,
                      ((String) ourval) + "; " + ((String) toapprop));
            }
            else
            {
              if (ourval instanceof Vector)
              {
                // append vectors
                Enumeration theirv = ((Vector) toapprop).elements();
                while (theirv.hasMoreElements())
                {
                  ((Vector) ourval).addElement(theirv);
                }
              }
            }
          }
        }
        else
        {
          // just add new property directly
          setProperty(k, toapprop);
        }

      }
    }
  }

  @Override
  public AlignmentAnnotation findOrCreateAnnotation(String name,
          String calcId, boolean autoCalc, SequenceI seqRef,
          SequenceGroup groupRef)
  {
    if (annotations != null)
    {
      for (AlignmentAnnotation annot : getAlignmentAnnotation())
      {
        if (annot.autoCalculated == autoCalc && (name.equals(annot.label))
                && (calcId == null || annot.getCalcId().equals(calcId))
                && annot.sequenceRef == seqRef
                && annot.groupRef == groupRef)
        {
          return annot;
        }
      }
    }
    AlignmentAnnotation annot = new AlignmentAnnotation(name, name,
            new Annotation[1], 0f, 0f, AlignmentAnnotation.BAR_GRAPH);
    annot.hasText = false;
    if (calcId != null)
    {
      annot.setCalcId(new String(calcId));
    }
    annot.autoCalculated = autoCalc;
    if (seqRef != null)
    {
      annot.setSequenceRef(seqRef);
    }
    annot.groupRef = groupRef;
    addAnnotation(annot);

    return annot;
  }

  @Override
  public Iterable<AlignmentAnnotation> findAnnotation(String calcId)
  {
    AlignmentAnnotation[] alignmentAnnotation = getAlignmentAnnotation();
    if (alignmentAnnotation != null)
    {
      return AlignmentAnnotation.findAnnotation(
              Arrays.asList(getAlignmentAnnotation()), calcId);
    }
    return Arrays.asList(new AlignmentAnnotation[] {});
  }

  @Override
  public Iterable<AlignmentAnnotation> findAnnotations(SequenceI seq,
          String calcId, String label)
  {
    return annotations == null ? null
            : AlignmentAnnotation.findAnnotations(
                    Arrays.asList(getAlignmentAnnotation()), seq, calcId,
                    label);
  }

  @Override
  public void moveSelectedSequencesByOne(SequenceGroup sg,
          Map<SequenceI, SequenceCollectionI> map, boolean up)
  {
    synchronized (sequences)
    {
      if (up)
      {

        for (int i = 1, iSize = sequences.size(); i < iSize; i++)
        {
          SequenceI seq = sequences.get(i);
          if (!sg.getSequences(map).contains(seq))
          {
            continue;
          }

          SequenceI temp = sequences.get(i - 1);
          if (sg.getSequences(null).contains(temp))
          {
            continue;
          }

          sequences.set(i, temp);
          sequences.set(i - 1, seq);
        }
      }
      else
      {
        for (int i = sequences.size() - 2; i > -1; i--)
        {
          SequenceI seq = sequences.get(i);
          if (!sg.getSequences(map).contains(seq))
          {
            continue;
          }

          SequenceI temp = sequences.get(i + 1);
          if (sg.getSequences(map).contains(temp))
          {
            continue;
          }

          sequences.set(i, temp);
          sequences.set(i + 1, seq);
        }
      }

    }
  }

  @Override
  public void validateAnnotation(AlignmentAnnotation alignmentAnnotation)
  {
    alignmentAnnotation.validateRangeAndDisplay();
    if (isNucleotide() && alignmentAnnotation.isValidStruc())
    {
      hasRNAStructure = true;
    }
  }

  private SequenceI seqrep = null;

  /**
   * 
   * @return the representative sequence for this group
   */
  @Override
  public SequenceI getSeqrep()
  {
    return seqrep;
  }

  /**
   * set the representative sequence for this group. Note - this affects the
   * interpretation of the Hidereps attribute.
   * 
   * @param seqrep
   *          the seqrep to set (null means no sequence representative)
   */
  @Override
  public void setSeqrep(SequenceI seqrep)
  {
    this.seqrep = seqrep;
  }

  /**
   * 
   * @return true if group has a sequence representative
   */
  @Override
  public boolean hasSeqrep()
  {
    return seqrep != null;
  }

  @Override
  public int getEndRes()
  {
    return getWidth() - 1;
  }

  @Override
  public int getStartRes()
  {
    return 0;
  }

  /*
   * In the case of AlignmentI - returns the dataset for the alignment, if set
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AnnotatedCollectionI#getContext()
   */
  @Override
  public AnnotatedCollectionI getContext()
  {
    return dataset;
  }

  /**
   * Align this alignment like the given (mapped) one.
   */
  @Override
  public int alignAs(AlignmentI al)
  {
    /*
     * Currently retains unmapped gaps (in introns), regaps mapped regions
     * (exons)
     */
    return alignAs(al, false, true);
  }

  /**
   * Align this alignment 'the same as' the given one. Mapped sequences only are
   * realigned. If both of the same type (nucleotide/protein) then align both
   * identically. If this is nucleotide and the other is protein, make 3 gaps
   * for each gap in the protein sequences. If this is protein and the other is
   * nucleotide, insert a gap for each 3 gaps (or part thereof) between
   * nucleotide bases. If this is protein and the other is nucleotide, gaps
   * protein to match the relative ordering of codons in the nucleotide.
   * 
   * Parameters control whether gaps in exon (mapped) and intron (unmapped)
   * regions are preserved. Gaps that connect introns to exons are treated
   * conservatively, i.e. only preserved if both intron and exon gaps are
   * preserved. TODO: check caveats below where the implementation fails
   * 
   * @param al
   *          - must have same dataset, and sequences in al must have equivalent
   *          dataset sequence and start/end bounds under given mapping
   * @param preserveMappedGaps
   *          if true, gaps within and between mapped codons are preserved
   * @param preserveUnmappedGaps
   *          if true, gaps within and between unmapped codons are preserved
   */
  // @Override
  public int alignAs(AlignmentI al, boolean preserveMappedGaps,
          boolean preserveUnmappedGaps)
  {
    // TODO should this method signature be the one in the interface?
    // JBPComment - yes - neither flag is used, so should be deleted.
    boolean thisIsNucleotide = this.isNucleotide();
    boolean thatIsProtein = !al.isNucleotide();
    if (!thatIsProtein && !thisIsNucleotide)
    {
      return AlignmentUtils.alignProteinAsDna(this, al);
    }
    else if (thatIsProtein && thisIsNucleotide)
    {
      return AlignmentUtils.alignCdsAsProtein(this, al);
    }
    return AlignmentUtils.alignAs(this, al);
  }

  /**
   * Returns the alignment in Fasta format. Behaviour of this method is not
   * guaranteed between versions.
   */
  @Override
  public String toString()
  {
    return new FastaFile().print(getSequencesArray(), true);
  }

  /**
   * Returns the set of distinct sequence names. No ordering is guaranteed.
   */
  @Override
  public Set<String> getSequenceNames()
  {
    Set<String> names = new HashSet<>();
    for (SequenceI seq : getSequences())
    {
      names.add(seq.getName());
    }
    return names;
  }

  @Override
  public boolean hasValidSequence()
  {
    boolean hasValidSeq = false;
    for (SequenceI seq : getSequences())
    {
      if ((seq.getEnd() - seq.getStart()) > 0)
      {
        hasValidSeq = true;
        break;
      }
    }
    return hasValidSeq;
  }

  /**
   * Update any mappings to 'virtual' sequences to compatible real ones, if
   * present in the added sequences. Returns a count of mappings updated.
   * 
   * @param seqs
   * @return
   */
  @Override
  public int realiseMappings(List<SequenceI> seqs)
  {
    int count = 0;
    for (SequenceI seq : seqs)
    {
      for (AlignedCodonFrame mapping : getCodonFrames())
      {
        count += mapping.realiseWith(seq);
      }
    }
    return count;
  }

  /**
   * Returns the first AlignedCodonFrame that has a mapping between the given
   * dataset sequences
   * 
   * @param mapFrom
   * @param mapTo
   * @return
   */
  @Override
  public AlignedCodonFrame getMapping(SequenceI mapFrom, SequenceI mapTo)
  {
    for (AlignedCodonFrame acf : getCodonFrames())
    {
      if (acf.getAaForDnaSeq(mapFrom) == mapTo)
      {
        return acf;
      }
    }
    return null;
  }

  @Override
  public boolean setHiddenColumns(HiddenColumns cols)
  {
    boolean changed = cols == null ? hiddenCols != null
            : !cols.equals(hiddenCols);
    hiddenCols = cols;
    return changed;
  }

  @Override
  public void setupJPredAlignment()
  {
    SequenceI repseq = getSequenceAt(0);
    setSeqrep(repseq);
    HiddenColumns cs = new HiddenColumns();
    cs.hideList(repseq.getInsertions());
    setHiddenColumns(cs);
  }

  @Override
  public HiddenColumns propagateInsertions(SequenceI profileseq,
          AlignmentView input)
  {
    int profsqpos = 0;

    char gc = getGapCharacter();
    Object[] alandhidden = input.getAlignmentAndHiddenColumns(gc);
    HiddenColumns nview = (HiddenColumns) alandhidden[1];
    SequenceI origseq = ((SequenceI[]) alandhidden[0])[profsqpos];
    return propagateInsertions(profileseq, origseq, nview);
  }

  /**
   * 
   * @param profileseq
   *          sequence in al which corresponds to origseq
   * @param al
   *          alignment which is to have gaps inserted into it
   * @param origseq
   *          sequence corresponding to profileseq which defines gap map for
   *          modifying al
   */
  private HiddenColumns propagateInsertions(SequenceI profileseq,
          SequenceI origseq, HiddenColumns hc)
  {
    // take the set of hidden columns, and the set of gaps in origseq,
    // and remove all the hidden gaps from hiddenColumns

    // first get the gaps as a Bitset
    // then calculate hidden ^ not(gap)
    BitSet gaps = origseq.gapBitset();
    hc.andNot(gaps);

    // for each sequence in the alignment, except the profile sequence,
    // insert gaps corresponding to each hidden region but where each hidden
    // column region is shifted backwards by the number of preceding visible
    // gaps update hidden columns at the same time
    HiddenColumns newhidden = new HiddenColumns();

    int numGapsBefore = 0;
    int gapPosition = 0;
    Iterator<int[]> it = hc.iterator();
    while (it.hasNext())
    {
      int[] region = it.next();

      // get region coordinates accounting for gaps
      // we can rely on gaps not being *in* hidden regions because we already
      // removed those
      while (gapPosition < region[0])
      {
        gapPosition++;
        if (gaps.get(gapPosition))
        {
          numGapsBefore++;
        }
      }

      int left = region[0] - numGapsBefore;
      int right = region[1] - numGapsBefore;

      newhidden.hideColumns(left, right);
      padGaps(left, right, profileseq);
    }
    return newhidden;
  }

  /**
   * Pad gaps in all sequences in alignment except profileseq
   * 
   * @param left
   *          position of first gap to insert
   * @param right
   *          position of last gap to insert
   * @param profileseq
   *          sequence not to pad
   */
  private void padGaps(int left, int right, SequenceI profileseq)
  {
    char gc = getGapCharacter();

    // make a string with number of gaps = length of hidden region
    StringBuilder sb = new StringBuilder();
    for (int g = 0; g < right - left + 1; g++)
    {
      sb.append(gc);
    }

    // loop over the sequences and pad with gaps where required
    for (int s = 0, ns = getHeight(); s < ns; s++)
    {
      SequenceI sqobj = getSequenceAt(s);
      if ((sqobj != profileseq) && (sqobj.getLength() >= left))
      {
        String sq = sqobj.getSequenceAsString();
        sqobj.setSequence(
                sq.substring(0, left) + sb.toString() + sq.substring(left));
      }
    }
  }

  ////
  //// Contact Matrix Holder Boilerplate
  ////
  ContactMapHolder cmholder = new ContactMapHolder();

  @Override
  public Collection<ContactMatrixI> getContactMaps()
  {
    return cmholder.getContactMaps();
  }

  @Override
  public ContactMatrixI getContactMatrixFor(AlignmentAnnotation _aa)
  {
    ContactMatrixI cm = cmholder.getContactMatrixFor(_aa);
    if (cm == null && _aa.groupRef != null)
    {
      cm = _aa.groupRef.getContactMatrixFor(_aa);
    }
    if (cm == null && _aa.sequenceRef != null)
    {
      cm = _aa.sequenceRef.getContactMatrixFor(_aa);
      if (cm == null)
      {
        // TODO fix up this logic and unify with getContactListFor
        cm = _aa.sequenceRef.getDatasetSequence().getContactMatrixFor(_aa);
      }
    }
    return cm;
  }

  @Override
  public ContactListI getContactListFor(AlignmentAnnotation _aa, int column)
  {
    ContactListI cl = cmholder.getContactListFor(_aa, column);
    if (cl == null && _aa.groupRef != null)
    {
      cl = _aa.groupRef.getContactListFor(_aa, column);
    }
    if (cl == null && _aa.sequenceRef != null)
    {
      if (_aa.annotations[column] != null)
      {
        // sequence associated
        cl = _aa.sequenceRef.getContactListFor(_aa, column);
        if (cl == null && _aa.sequenceRef.getDatasetSequence() != null)
        {
          int spos = _aa.sequenceRef.findPosition(column);
          if (spos >= _aa.sequenceRef.getStart()
                  && spos <= 1 + _aa.sequenceRef.getEnd())
          {
            cl = _aa.sequenceRef.getDatasetSequence().getContactListFor(_aa,
                    spos - _aa.sequenceRef.getStart());
          }
        }
      }
    }
    return cl;
  }

  @Override
  public AlignmentAnnotation addContactList(ContactMatrixI cm)
  {
    AlignmentAnnotation aa = cmholder.addContactList(cm);

    Annotation _aa[] = new Annotation[getWidth()];
    for (int i = 0; i < _aa.length; _aa[i++] = new Annotation(0.0f))
    {
      ;
    }
    aa.annotations = _aa;
    addAnnotation(aa);
    return aa;
  }

  @Override
  public void addContactListFor(AlignmentAnnotation annotation,
          ContactMatrixI cm)
  {
    cmholder.addContactListFor(annotation, cm);

  }
}
