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

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jalview.analysis.AAFrequency;
import jalview.analysis.RepeatingVariance;
import jalview.analysis.Conservation;
import jalview.renderer.ResidueShader;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.ColourSchemeI;
import jalview.viewmodel.AlignmentViewport;

/**
 * Collects a set contiguous ranges on a set of sequences
 * 
 * @author $author$
 * @version $Revision$
 */
public class SequenceGroup implements AnnotatedCollectionI
{
  // TODO ideally this event notification functionality should be separated into
  // a
  // subclass of ViewportProperties similarly to ViewportRanges. Done here as
  // quick fix for JAL-2665
  public static final String SEQ_GROUP_CHANGED = "Sequence group changed";

  protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(
          this);

  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }
  // end of event notification functionality initialisation

  String groupName;

  String description;

  Conservation conserve;

  boolean displayBoxes = true;

  boolean displayText = true;

  boolean colourText = false;

  /**
   * True if the group is defined as a group on the alignment, false if it is
   * just a selection.
   */
  boolean isDefined = false;

  /**
   * after Olivier's non-conserved only character display
   */
  boolean showNonconserved = false;

  /**
   * group members
   */
  private List<SequenceI> sequences;

  /**
   * representative sequence for this group (if any)
   */
  private SequenceI seqrep = null;

  int width = -1;

  /**
   * Colourscheme applied to group if any
   */
  public ResidueShaderI cs;

  /**
   * start column (base 0)
   */
  private int startRes = 0;

  /**
   * end column (base 0)
   */
  private int endRes = 0;

  public Color outlineColour = Color.black;

  public Color idColour = null;

  public int thresholdTextColour = 0;

  public Color textColour = Color.black;

  public Color textColour2 = Color.white;

  /**
   * consensus calculation property
   */
  private boolean ignoreGapsInConsensus = true;

  /**
   * consensus calculation property
   */
  private boolean showSequenceLogo = false;

  /**
   * flag indicating if logo should be rendered normalised
   */
  private boolean normaliseSequenceLogo;

  /*
   * visibility of rows or represented rows covered by group
   */
  private boolean hidereps = false;

  /*
   * visibility of columns intersecting this group
   */
  private boolean hidecols = false;

  AlignmentAnnotation consensus = null;

  AlignmentAnnotation conservation = null;

  private boolean showConsensusHistogram;
  
  AlignmentAnnotation variance = null;
  
  private boolean showVarianceHistogram;
  
  private AlignmentI alignment = null;

  private AnnotatedCollectionI context;

  /**
   * Creates a new SequenceGroup object.
   */
  public SequenceGroup()
  {
    groupName = "JGroup:" + this.hashCode();
    cs = new ResidueShader();
    sequences = new ArrayList<>();
  }

  /**
   * Creates a new SequenceGroup object.
   * 
   * @param sequences
   * @param groupName
   * @param scheme
   * @param displayBoxes
   * @param displayText
   * @param colourText
   * @param start
   *          first column of group
   * @param end
   *          last column of group
   */
  public SequenceGroup(List<SequenceI> sequences, String groupName,
          ColourSchemeI scheme, boolean displayBoxes, boolean displayText,
          boolean colourText, int start, int end)
  {
    this(sequences, groupName, scheme, displayBoxes, displayText, colourText, start, end, null);
  }
  public SequenceGroup(List<SequenceI> sequences, String groupName,
          ColourSchemeI scheme, boolean displayBoxes, boolean displayText,
          boolean colourText, int start, int end, AlignmentI al)
  {
    this();
    this.sequences = sequences;
    this.groupName = groupName;
    this.displayBoxes = displayBoxes;
    this.displayText = displayText;
    this.colourText = colourText;
    this.cs = new ResidueShader(scheme);
    startRes = start;
    endRes = end;
    recalcConservation();
    if (al != null)
    {
      this.alignment = al;
      recalcVariance(alignment);
    }
  }

  /**
   * copy constructor
   * 
   * @param seqsel
   */
  public SequenceGroup(SequenceGroup seqsel)
  {
    this();
    if (seqsel != null)
    {
      sequences = new ArrayList<>();
      sequences.addAll(seqsel.sequences);
      if (seqsel.groupName != null)
      {
        groupName = new String(seqsel.groupName);
      }
      displayBoxes = seqsel.displayBoxes;
      displayText = seqsel.displayText;
      colourText = seqsel.colourText;

      startRes = seqsel.startRes;
      endRes = seqsel.endRes;
      cs = new ResidueShader((ResidueShader) seqsel.cs);
      if (seqsel.description != null)
      {
        description = new String(seqsel.description);
      }
      hidecols = seqsel.hidecols;
      hidereps = seqsel.hidereps;
      showNonconserved = seqsel.showNonconserved;
      showSequenceLogo = seqsel.showSequenceLogo;
      normaliseSequenceLogo = seqsel.normaliseSequenceLogo;
      showConsensusHistogram = seqsel.showConsensusHistogram;
      showVarianceHistogram = seqsel.showVarianceHistogram;
      idColour = seqsel.idColour;
      outlineColour = seqsel.outlineColour;
      seqrep = seqsel.seqrep;
      textColour = seqsel.textColour;
      textColour2 = seqsel.textColour2;
      thresholdTextColour = seqsel.thresholdTextColour;
      width = seqsel.width;
      ignoreGapsInConsensus = seqsel.ignoreGapsInConsensus;
      if (seqsel.conserve != null)
      {
        recalcConservation(); // safer than
        // aaFrequency = (Vector) seqsel.aaFrequency.clone(); // ??
      }
      if (seqsel.variance != null && seqsel.alignment != null)
      {
        recalcVariance(seqsel.alignment);
      }
    }
  }

  /**
   * Constructor that copies the given list of sequences
   * 
   * @param seqs
   */
  public SequenceGroup(List<SequenceI> seqs)
  {
    this();
    this.sequences.addAll(seqs);
  }

  public boolean isShowSequenceLogo()
  {
    return showSequenceLogo;
  }

  public SequenceI[] getSelectionAsNewSequences(AlignmentI align)
  {
    int iSize = sequences.size();
    SequenceI[] seqs = new SequenceI[iSize];
    SequenceI[] inorder = getSequencesInOrder(align);

    for (int i = 0, ipos = 0; i < inorder.length; i++)
    {
      SequenceI seq = inorder[i];
      SequenceI seqipos = seqs[ipos] = seq.getSubSequence(startRes,
              endRes + 1);
      if (seqipos != null)
      {
        if (seq.getAnnotation() != null)
        {
          AlignmentAnnotation[] alann = align.getAlignmentAnnotation();
          // Only copy annotation that is either a score or referenced by the
          // alignment's annotation vector
          for (int a = 0; a < seq.getAnnotation().length; a++)
          {
            AlignmentAnnotation tocopy = seq.getAnnotation()[a];
            if (alann != null)
            {
              boolean found = false;
              for (int pos = 0, np = alann.length; pos < np; pos++)
              {
                if (alann[pos] == tocopy)
                {
                  found = true;
                  break;
                }
              }
              if (!found)
              {
                continue;
              }
            }
            AlignmentAnnotation newannot = new AlignmentAnnotation(
                    seq.getAnnotation()[a]);
            newannot.restrict(startRes, endRes);
            newannot.setSequenceRef(seqs[ipos]);
            newannot.adjustForAlignment();
            ContactMatrixI cm = seq.getContactMatrixFor(seq.getAnnotation()[a]);
            if (cm!=null)
            {
              seqs[ipos].addContactListFor(newannot, cm);
            }
            seqipos.addAlignmentAnnotation(newannot);
          }
        }
        ipos++;
      }
      else
      {
        iSize--;
      }
    }
    if (iSize != inorder.length)
    {
      SequenceI[] nseqs = new SequenceI[iSize];
      System.arraycopy(seqs, 0, nseqs, 0, iSize);
      seqs = nseqs;
    }
    return seqs;

  }

  /**
   * If sequence ends in gaps, the end residue can be correctly calculated here
   * 
   * @param seq
   *          SequenceI
   * @return int
   */
  public int findEndRes(SequenceI seq)
  {
    int eres = 0;
    char ch;

    for (int j = 0; j < endRes + 1 && j < seq.getLength(); j++)
    {
      ch = seq.getCharAt(j);
      if (!jalview.util.Comparison.isGap((ch)))
      {
        eres++;
      }
    }

    if (eres > 0)
    {
      eres += seq.getStart() - 1;
    }

    return eres;
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
    if (hiddenReps == null)
    {
      // TODO: need a synchronizedCollection here ?
      return sequences;
    }
    else
    {
      List<SequenceI> allSequences = new ArrayList<>();
      for (SequenceI seq : sequences)
      {
        allSequences.add(seq);
        if (hiddenReps.containsKey(seq))
        {
          SequenceCollectionI hsg = hiddenReps.get(seq);
          for (SequenceI seq2 : hsg.getSequences())
          {
            if (seq2 != seq && !allSequences.contains(seq2))
            {
              allSequences.add(seq2);
            }
          }
        }
      }

      return allSequences;
    }
  }

  public SequenceI[] getSequencesAsArray(
          Map<SequenceI, SequenceCollectionI> map)
  {
    List<SequenceI> tmp = getSequences(map);
    if (tmp == null)
    {
      return null;
    }
    return tmp.toArray(new SequenceI[tmp.size()]);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param col
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean adjustForRemoveLeft(int col)
  {
    // return value is true if the group still exists
    if (startRes >= col)
    {
      startRes = startRes - col;
    }

    if (endRes >= col)
    {
      endRes = endRes - col;

      if (startRes > endRes)
      {
        startRes = 0;
      }
    }
    else
    {
      // must delete this group!!
      return false;
    }

    return true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param col
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean adjustForRemoveRight(int col)
  {
    if (startRes > col)
    {
      // delete this group
      return false;
    }

    if (endRes >= col)
    {
      endRes = col;
    }

    return true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getName()
  {
    return groupName;
  }

  public String getDescription()
  {
    return description;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param name
   *          DOCUMENT ME!
   */
  public void setName(String name)
  {
    groupName = name;
    // TODO: URGENT: update dependent objects (annotation row)
  }

  public void setDescription(String desc)
  {
    description = desc;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Conservation getConservation()
  {
    return conserve;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param c
   *          DOCUMENT ME!
   */
  public void setConservation(Conservation c)
  {
    conserve = c;
  }

  /**
   * Add s to this sequence group. If aligment sequence is already contained in
   * group, it will not be added again, but recalculation may happen if the flag
   * is set.
   * 
   * @param s
   *          alignment sequence to be added
   * @param recalc
   *          true means Group's conservation should be recalculated
   */
  public void addSequence(SequenceI s, boolean recalc)
  {
    synchronized (sequences)
    {
      if (s != null && !sequences.contains(s))
      {
        sequences.add(s);
        changeSupport.firePropertyChange(SEQ_GROUP_CHANGED,
                sequences.size() - 1, sequences.size());
      }

      if (recalc)
      {
        recalcConservation();
        if (alignment != null)
        {
          recalcVariance(alignment);
        }
      }
    }
  }

  /**
   * Max Gaps Threshold (percent) for performing a conservation calculation
   */
  private int consPercGaps = 25;

  /**
   * @return Max Gaps Threshold for performing a conservation calculation
   */
  public int getConsPercGaps()
  {
    return consPercGaps;
  }

  /**
   * set Max Gaps Threshold (percent) for performing a conservation calculation
   * 
   * @param consPercGaps
   */
  public void setConsPercGaps(int consPercGaps)
  {
    this.consPercGaps = consPercGaps;
  }

  /**
   * calculate residue conservation and colourschemes for group - but only if
   * necessary. returns true if the calculation resulted in a visible change to
   * group
   */
  public boolean recalcConservation()
  {
    return recalcConservation(false);
  }

  /**
   * calculate residue conservation for group - but only if necessary. returns
   * true if the calculation resulted in a visible change to group
   * 
   * @param defer
   *          when set, colourschemes for this group are not refreshed after
   *          recalculation
   */
  public boolean recalcConservation(boolean defer)
  {
    if (cs == null && consensus == null && conservation == null)
    {
      return false;
    }
    // TODO: try harder to detect changes in state in order to minimise
    // recalculation effort
    boolean upd = false;
    try
    {
      ProfilesI cnsns = AAFrequency.calculate(sequences, startRes,
              endRes + 1, showSequenceLogo);
      if (consensus != null)
      {
        _updateConsensusRow(cnsns, sequences.size());
        upd = true;
      }
      if (cs != null)
      {
        cs.setConsensus(cnsns);
        upd = true;
      }

      if ((conservation != null)
              || (cs != null && cs.conservationApplied()))
      {
        Conservation c = new Conservation(groupName, sequences, startRes,
                endRes + 1);
        c.calculate();
        c.verdict(false, consPercGaps);
        if (conservation != null)
        {
          _updateConservationRow(c);
        }
        if (cs != null)
        {
          if (cs.conservationApplied())
          {
            cs.setConservation(c);
          }
        }
        // eager update - will cause a refresh of overview regardless
        upd = true;
      }
      if (cs != null && !defer)
      {
        // TODO: JAL-2034 should cs.alignmentChanged modify return state
        cs.alignmentChanged(context != null ? context : this, null);
        return true;
      }
      else
      {
        return upd;
      }
    } catch (java.lang.OutOfMemoryError err)
    {
      // TODO: catch OOM
      System.out.println("Out of memory loading groups: " + err);
    }
    return upd;
  }

  /**
   * calculate residue variance and colourschemes for group - but only if
   * necessary. returns true if the calculation resulted in a visible change to
   * group
   */
  public boolean recalcVariance()
  {
    return recalcVariance(alignment);
  }
  public boolean recalcVariance(final AlignmentI al)
  {
    return recalcVariance(al, false);
  }

  /**
   * calculate residue variance for group - but only if necessary. returns
   * true if the calculation resulted in a visible change to group
   * 
   * @param defer
   *          when set, colourschemes for this group are not refreshed after
   *          recalculation
   */
  public boolean recalcVariance(final AlignmentI al, boolean defer)
  {
    //System.out.println(String.format("SequenceGroup @recalcVariance (%b) startRes %d, endRes+1 %d", defer, startRes, endRes+1));
    if (cs == null && variance == null)
    {
      return false;
    }
    // TODO: try harder to detect changes in state in order to minimise
    // recalculation effort
    boolean upd = false;
    try
    {
      ProfilesI vrns = RepeatingVariance.calculate(al);
      if (variance != null)
      {
        _updateVarianceRow(vrns, al);
        upd = true;
      }
      if (cs != null)
      {
        cs.setVariance(vrns);
        upd = true;
      }

      if (cs != null && !defer)
      {
        // TODO: JAL-2034 should cs.alignmentChanged modify return state
        cs.alignmentChanged(context != null ? context : this, null);
        return true;
      }
      else
      {
        return upd;
      }
    } catch (java.lang.OutOfMemoryError err)
    {
      // TODO: catch OOM
      System.out.println("Out of memory loading groups: " + err);
    }
    return upd;
  }

  private void _updateConservationRow(Conservation c)
  {
    if (conservation == null)
    {
      getConservation();
    }
    // update Labels
    conservation.label = "Conservation for " + getName();
    conservation.description = "Conservation for group " + getName()
            + " less than " + consPercGaps + "% gaps";
    // preserve width if already set
    int aWidth = (conservation.annotations != null)
            ? (endRes < conservation.annotations.length
                    ? conservation.annotations.length
                    : endRes + 1)
            : endRes + 1;
    conservation.annotations = null;
    conservation.annotations = new Annotation[aWidth]; // should be alignment
                                                       // width
    c.completeAnnotations(conservation, null, startRes, endRes + 1);
  }

  public ProfilesI consensusData = null;
  

  private void _updateConsensusRow(ProfilesI cnsns, long nseq)
  {
    if (consensus == null)
    {
      getConsensus();
    }
    consensus.label = "Consensus for " + getName();
    consensus.description = "Percent Identity";
    consensusData = cnsns;
    // preserve width if already set
    int aWidth = (consensus.annotations != null)
            ? (endRes < consensus.annotations.length
                    ? consensus.annotations.length
                    : endRes + 1)
            : endRes + 1;
    consensus.annotations = null;
    consensus.annotations = new Annotation[aWidth]; // should be alignment width

    AAFrequency.completeConsensus(consensus, cnsns, startRes, endRes + 1,
            ignoreGapsInConsensus, showSequenceLogo, nseq); // TODO: setting
                                                            // container
    // for
    // ignoreGapsInConsensusCalculation);
  }

  public ProfilesI varianceData = null;

  private void _updateVarianceRow(ProfilesI vrns, AlignmentI al)
  {
    if (variance == null)
    {
      getVariance();
    }
    long nseq = (long) al.getHeight();
    variance.label = "Variance for " + getName();
    variance.description = "Percent Identity";
    varianceData = vrns;
    // preserve width if already set
    int aWidth = (variance.annotations != null)
            ? (endRes < variance.annotations.length
                    ? variance.annotations.length
                    : endRes + 1)
            : endRes + 1;
    variance.annotations = null;
    variance.annotations = new Annotation[aWidth]; // should be alignment width

    RepeatingVariance.completeVariance(al);
            // TODO: setting
            // container
    // for
    // ignoreGapsInConsensusCalculation);
  }

  /**
   * @param s
   *          sequence to either add or remove from group
   * @param recalc
   *          flag passed to delete/addSequence to indicate if group properties
   *          should be recalculated
   */
  public void addOrRemove(SequenceI s, boolean recalc)
  {
    synchronized (sequences)
    {
      if (sequences.contains(s))
      {
        deleteSequence(s, recalc);
      }
      else
      {
        addSequence(s, recalc);
      }
    }
  }

  /**
   * remove
   * 
   * @param s
   *          to be removed
   * @param recalc
   *          true means recalculate conservation
   */
  public void deleteSequence(SequenceI s, boolean recalc)
  {
    synchronized (sequences)
    {
      sequences.remove(s);
      changeSupport.firePropertyChange(SEQ_GROUP_CHANGED,
              sequences.size() + 1, sequences.size());

      if (recalc)
      {
        recalcConservation();
        if (alignment != null)
        {
          recalcVariance(alignment);
        }
      }
    }
  }

  /**
   * 
   * 
   * @return the first column selected by this group. Runs from 0<=i<N_cols
   */
  @Override
  public int getStartRes()
  {
    return startRes;
  }

  /**
   * 
   * @return the groups last selected column. Runs from 0<=i<N_cols
   */
  @Override
  public int getEndRes()
  {
    return endRes;
  }

  /**
   * Set the first column selected by this group. Runs from 0<=i<N_cols
   * 
   * @param newStart
   */
  public void setStartRes(int newStart)
  {
    int before = startRes;
    startRes = Math.max(0, newStart); // sanity check for negative start column
                                      // positions
    changeSupport.firePropertyChange(SEQ_GROUP_CHANGED, before, startRes);

  }

  /**
   * Set the groups last selected column. Runs from 0<=i<N_cols
   * 
   * @param i
   */
  public void setEndRes(int i)
  {
    int before = endRes;
    endRes = i;
    changeSupport.firePropertyChange(SEQ_GROUP_CHANGED, before, endRes);
  }

  /**
   * @return number of sequences in group
   */
  public int getSize()
  {
    return sequences.size();
  }

  /**
   * @param i
   * @return the ith sequence
   */
  public SequenceI getSequenceAt(int i)
  {
    return sequences.get(i);
  }

  /**
   * @param state
   *          colourText
   */
  public void setColourText(boolean state)
  {
    colourText = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getColourText()
  {
    return colourText;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setDisplayText(boolean state)
  {
    displayText = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getDisplayText()
  {
    return displayText;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setDisplayBoxes(boolean state)
  {
    displayBoxes = state;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean getDisplayBoxes()
  {
    return displayBoxes;
  }

  /**
   * computes the width of current set of sequences and returns it
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public int getWidth()
  {
    synchronized (sequences)
    {
      // MC This needs to get reset when characters are inserted and deleted
      boolean first = true;
      for (SequenceI seq : sequences)
      {
        if (first || seq.getLength() > width)
        {
          width = seq.getLength();
          first = false;
        }
      }
      return width;
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param c
   *          DOCUMENT ME!
   */
  public void setOutlineColour(Color c)
  {
    outlineColour = c;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Color getOutlineColour()
  {
    return outlineColour;
  }

  /**
   * 
   * returns the sequences in the group ordered by the ordering given by al.
   * this used to return an array with null entries regardless, new behaviour is
   * below. TODO: verify that this does not affect use in applet or application
   * 
   * @param al
   *          Alignment
   * @return SequenceI[] intersection of sequences in group with al, ordered by
   *         al, or null if group does not intersect with al
   */
  public SequenceI[] getSequencesInOrder(AlignmentI al)
  {
    return getSequencesInOrder(al, true);
  }

  /**
   * return an array representing the intersection of the group with al,
   * optionally returning an array the size of al.getHeight() where nulls mark
   * the non-intersected sequences
   * 
   * @param al
   * @param trim
   * @return null or array
   */
  public SequenceI[] getSequencesInOrder(AlignmentI al, boolean trim)
  {
    synchronized (sequences)
    {
      int sSize = sequences.size();
      int alHeight = al.getHeight();

      SequenceI[] seqs = new SequenceI[(trim) ? sSize : alHeight];

      int index = 0;
      for (int i = 0; i < alHeight && index < sSize; i++)
      {
        if (sequences.contains(al.getSequenceAt(i)))
        {
          seqs[(trim) ? index : i] = al.getSequenceAt(i);
          index++;
        }
      }
      if (index == 0)
      {
        return null;
      }
      if (!trim)
      {
        return seqs;
      }
      if (index < seqs.length)
      {
        SequenceI[] dummy = seqs;
        seqs = new SequenceI[index];
        while (--index >= 0)
        {
          seqs[index] = dummy[index];
          dummy[index] = null;
        }
      }
      return seqs;
    }
  }

  /**
   * @return the idColour
   */
  public Color getIdColour()
  {
    return idColour;
  }

  /**
   * @param idColour
   *          the idColour to set
   */
  public void setIdColour(Color idColour)
  {
    this.idColour = idColour;
  }

  /**
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

  /**
   * set visibility of sequences covered by (if no sequence representative is
   * defined) or represented by this group.
   * 
   * @param visibility
   */
  public void setHidereps(boolean visibility)
  {
    hidereps = visibility;
  }

  /**
   * 
   * @return true if sequences represented (or covered) by this group should be
   *         hidden
   */
  public boolean isHidereps()
  {
    return hidereps;
  }

  /**
   * set intended visibility of columns covered by this group
   * 
   * @param visibility
   */
  public void setHideCols(boolean visibility)
  {
    hidecols = visibility;
  }

  /**
   * 
   * @return true if columns covered by group should be hidden
   */
  public boolean isHideCols()
  {
    return hidecols;
  }

  /**
   * create a new sequence group from the intersection of this group with an
   * alignment Hashtable of hidden representatives
   * 
   * @param alignment
   *          (may not be null)
   * @param map
   *          (may be null)
   * @return new group containing sequences common to this group and alignment
   */
  public SequenceGroup intersect(AlignmentI alignment,
          Map<SequenceI, SequenceCollectionI> map)
  {
    SequenceGroup sgroup = new SequenceGroup(this);
    SequenceI[] insect = getSequencesInOrder(alignment);
    sgroup.sequences = new ArrayList<>();
    for (int s = 0; insect != null && s < insect.length; s++)
    {
      if (map == null || map.containsKey(insect[s]))
      {
        sgroup.sequences.add(insect[s]);
      }
    }
    return sgroup;
  }

  /**
   * @return the showUnconserved
   */
  public boolean getShowNonconserved()
  {
    return showNonconserved;
  }

  /**
   * @param showNonconserved
   *          the showUnconserved to set
   */
  public void setShowNonconserved(boolean displayNonconserved)
  {
    this.showNonconserved = displayNonconserved;
  }

  /**
   * set this alignmentAnnotation object as the one used to render consensus
   * annotation
   * 
   * @param aan
   */
  public void setConsensus(AlignmentAnnotation aan)
  {
    if (consensus == null)
    {
      consensus = aan;
    }
  }
  
  /**
   * set this alignmentAnnotation object as the one used to render variance
   * annotation
   * 
   * @param aan
   */
  public void setVariance(AlignmentAnnotation aan)
  {
    if (variance == null)
    {
      variance = aan;
    }
  }
  /**
   * 
   * @return automatically calculated consensus row note: the row is a stub if a
   *         consensus calculation has not yet been performed on the group
   */
  public AlignmentAnnotation getConsensus()
  {
    // TODO get or calculate and get consensus annotation row for this group
    int aWidth = this.getWidth();
    // pointer
    // possibility
    // here.
    if (aWidth < 0)
    {
      return null;
    }
    if (consensus == null)
    {
      consensus = new AlignmentAnnotation("", "", new Annotation[1], 0f,
              100f, AlignmentAnnotation.BAR_GRAPH);
      consensus.hasText = true;
      consensus.autoCalculated = true;
      consensus.groupRef = this;
      consensus.label = "Consensus for " + getName();
      consensus.description = "Percent Identity";
    }
    return consensus;
  }

  /**
   * 
   * @return automatically calculated variance row note: the row is a stub if a
   *         variance calculation has not yet been performed on the group
   */
  public AlignmentAnnotation getVariance()
  {
    System.out.println("SequenceGroup @getVariance null? " + String.valueOf(variance == null));
    // TODO get or calculate and get consensus annotation row for this group
    int aWidth = this.getWidth();
    // pointer
    // possibility
    // here.
    if (aWidth < 0)
    {
      return null;
    }
    if (variance == null)
    {
      variance = new AlignmentAnnotation("", "", new Annotation[1], 0f,
              100f, AlignmentAnnotation.BAR_GRAPH);
      variance.hasText = true;
      variance.autoCalculated = true;
      variance.groupRef = this;
      variance.label = "Consensus for " + getName();
      variance.description = "Percent Identity";
    }
    return variance;
  }

  /**
   * set this alignmentAnnotation object as the one used to render consensus
   * annotation
   * 
   * @param aan
   */
  public void setConservationRow(AlignmentAnnotation aan)
  {
    if (conservation == null)
    {
      conservation = aan;
    }
  }

  /**
   * get the conservation annotation row for this group
   * 
   * @return autoCalculated annotation row
   */
  public AlignmentAnnotation getConservationRow()
  {
    if (conservation == null)
    {
      conservation = new AlignmentAnnotation("", "", new Annotation[1], 0f,
              11f, AlignmentAnnotation.BAR_GRAPH);
    }

    conservation.hasText = true;
    conservation.autoCalculated = true;
    conservation.groupRef = this;
    conservation.label = "Conservation for " + getName();
    conservation.description = "Conservation for group " + getName()
            + " less than " + consPercGaps + "% gaps";
    return conservation;
  }

  /**
   * 
   * @return true if annotation rows have been instantiated for this group
   */
  public boolean hasAnnotationRows()
  {
    return consensus != null || conservation != null || variance != null;
  }

  public SequenceI getConsensusSeq()
  {
    getConsensus();
    StringBuffer seqs = new StringBuffer();
    for (int i = 0; i < consensus.annotations.length; i++)
    {
      if (consensus.annotations[i] != null)
      {
        String desc = consensus.annotations[i].description;
        if (desc.length() > 1 && desc.charAt(0) == '[')
        {
          seqs.append(desc.charAt(1));
        }
        else
        {
          seqs.append(consensus.annotations[i].displayCharacter);
        }
      }
    }

    SequenceI sq = new Sequence("Group" + getName() + " Consensus",
            seqs.toString());
    sq.setDescription("Percentage Identity Consensus "
            + ((ignoreGapsInConsensus) ? " without gaps" : ""));
    return sq;
  }

  public void setIgnoreGapsConsensus(boolean state)
  {
    if (this.ignoreGapsInConsensus != state && consensus != null)
    {
      ignoreGapsInConsensus = state;
      recalcConservation();
    }
    ignoreGapsInConsensus = state;
  }

  public boolean getIgnoreGapsConsensus()
  {
    return ignoreGapsInConsensus;
  }

  /**
   * @param showSequenceLogo
   *          indicates if a sequence logo is shown for consensus annotation
   */
  public void setshowSequenceLogo(boolean showSequenceLogo)
  {
    // TODO: decouple calculation from settings update
    if (this.showSequenceLogo != showSequenceLogo && consensus != null)
    {
      this.showSequenceLogo = showSequenceLogo;
      recalcConservation();
    }
    this.showSequenceLogo = showSequenceLogo;
  }

  /**
   * 
   * @param showConsHist
   *          flag indicating if the consensus histogram for this group should
   *          be rendered
   */
  public void setShowConsensusHistogram(boolean showConsHist)
  {

    if (showConsensusHistogram != showConsHist && consensus != null)
    {
      this.showConsensusHistogram = showConsHist;
      recalcConservation();
    }
    this.showConsensusHistogram = showConsHist;
  }

  /**
   * @return the showConsensusHistogram
   */
  public boolean isShowConsensusHistogram()
  {
    return showConsensusHistogram;
  }
  
  /**
   * @param showVarHist
   *          flag indicating if the variance histogram for this group should
   *           be rendered
   */
  public void setShowVarianceHistogram(boolean showVarHist)
  {
    if (showVarianceHistogram != showVarHist && variance != null)
    {
      this.showVarianceHistogram = showVarHist;
      recalcVariance();
    }
    this.showVarianceHistogram = showVarHist;
  }
  
  /**
   * @return the showVarianceHistogram
   */
  public boolean isShowVarianceHistogram()
  {
    return showVarianceHistogram;
  }

  /**
   * set flag indicating if logo should be normalised when rendered
   * 
   * @param norm
   */
  public void setNormaliseSequenceLogo(boolean norm)
  {
    normaliseSequenceLogo = norm;
  }

  public boolean isNormaliseSequenceLogo()
  {
    return normaliseSequenceLogo;
  }

  @Override
  /**
   * returns a new array with all annotation involving this group
   */
  public AlignmentAnnotation[] getAlignmentAnnotation()
  {
    // TODO add in other methods like 'getAlignmentAnnotation(String label),
    // etc'
    ArrayList<AlignmentAnnotation> annot = new ArrayList<>();
    synchronized (sequences)
    {
      for (SequenceI seq : sequences)
      {
        AlignmentAnnotation[] aa = seq.getAnnotation();
        if (aa != null)
        {
          for (AlignmentAnnotation al : aa)
          {
            if (al.groupRef == this)
            {
              annot.add(al);
            }
          }
        }
      }
      if (consensus != null)
      {
        annot.add(consensus);
      }
      if (conservation != null)
      {
        annot.add(conservation);
      }
      if (variance != null)
      {
        annot.add(variance);
      }
    }
    return annot.toArray(new AlignmentAnnotation[0]);
  }

  @Override
  public Iterable<AlignmentAnnotation> findAnnotation(String calcId)
  {
    return AlignmentAnnotation.findAnnotation(
            Arrays.asList(getAlignmentAnnotation()), calcId);
  }

  @Override
  public Iterable<AlignmentAnnotation> findAnnotations(SequenceI seq,
          String calcId, String label)
  {
    return AlignmentAnnotation.findAnnotations(
            Arrays.asList(getAlignmentAnnotation()), seq, calcId, label);
  }

  /**
   * Answer true if any annotation matches the calcId passed in (if not null).
   * 
   * @param calcId
   * @return
   */
  public boolean hasAnnotation(String calcId)
  {
    return AlignmentAnnotation
            .hasAnnotation(Arrays.asList(getAlignmentAnnotation()), calcId);
  }

  /**
   * Remove all sequences from the group (leaving other properties unchanged).
   */
  public void clear()
  {
    synchronized (sequences)
    {
      int before = sequences.size();
      sequences.clear();
      changeSupport.firePropertyChange(SEQ_GROUP_CHANGED, before,
              sequences.size());
    }
  }

  /**
   * Sets the alignment or group context for this group, and whether it is
   * defined as a group
   * 
   * @param ctx
   *          the context for the group
   * @param defined
   *          whether the group is defined on the alignment or is just a
   *          selection
   * @throws IllegalArgumentException
   *           if setting the context would result in a circular reference chain
   */
  public void setContext(AnnotatedCollectionI ctx, boolean defined)
  {
    setContext(ctx);
    this.isDefined = defined;
  }

  /**
   * Sets the alignment or group context for this group
   * 
   * @param ctx
   *          the context for the group
   * @throws IllegalArgumentException
   *           if setting the context would result in a circular reference chain
   */
  public void setContext(AnnotatedCollectionI ctx)
  {
    AnnotatedCollectionI ref = ctx;
    while (ref != null)
    {
      if (ref == this || ref.getContext() == ctx)
      {
        throw new IllegalArgumentException(
                "Circular reference in SequenceGroup.context");
      }
      ref = ref.getContext();
    }
    this.context = ctx;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.datamodel.AnnotatedCollectionI#getContext()
   */
  @Override
  public AnnotatedCollectionI getContext()
  {
    return context;
  }

  public boolean isDefined()
  {
    return isDefined;
  }

  public void setColourScheme(ColourSchemeI scheme)
  {
    if (cs == null)
    {
      cs = new ResidueShader();
    }
    cs.setColourScheme(scheme);
  }

  public void setGroupColourScheme(ResidueShaderI scheme)
  {
    cs = scheme;
  }

  public ColourSchemeI getColourScheme()
  {
    return cs == null ? null : cs.getColourScheme();
  }

  public ResidueShaderI getGroupColourScheme()
  {
    return cs;
  }

  @Override
  public boolean isNucleotide()
  {
    if (context != null)
    {
      return context.isNucleotide();
    }
    return false;
  }

  /**
   * @param seq
   * @return true if seq is a member of the group
   */

  public boolean contains(SequenceI seq1)
  {
    return sequences.contains(seq1);
  }

  /**
   * @param seq
   * @param apos
   * @return true if startRes<=apos and endRes>=apos and seq is in the group
   */
  public boolean contains(SequenceI seq, int apos)
  {
    return (startRes <= apos && endRes >= apos) && sequences.contains(seq);
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
  public ContactMatrixI getContactMatrixFor(AlignmentAnnotation ann)
  {
    return cmholder.getContactMatrixFor(ann);
  }

  @Override
  public ContactListI getContactListFor(AlignmentAnnotation _aa, int column)
  {
    return cmholder.getContactListFor(_aa, column);
  }

  @Override
  public AlignmentAnnotation addContactList(ContactMatrixI cm)
  {
    AlignmentAnnotation aa = cmholder.addContactList(cm);

    Annotation _aa[] = new Annotation[getWidth()];
    Annotation dummy = new Annotation(0.0f);
    for (int i = 0; i < _aa.length; _aa[i++] = dummy)
    {
      ;
    }
    aa.annotations = _aa;
    // TODO passing annotations back to context to be added
    return aa;
  }

  @Override
  public void addContactListFor(AlignmentAnnotation annotation,
          ContactMatrixI cm)
  {
    cmholder.addContactListFor(annotation, cm);
  }

}
