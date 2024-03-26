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
package jalview.viewmodel;

import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jalview.analysis.Analysis;
import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.analysis.Connectivity;
import jalview.analysis.Conservation;
import jalview.analysis.RepeatingVariance;
import jalview.analysis.TreeModel;
import jalview.api.AlignCalcManagerI;
import jalview.api.AlignExportSettingsI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeaturesDisplayedI;
import jalview.api.ViewStyleI;
import jalview.bin.Console;
import jalview.commands.CommandI;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentExportData;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.HiddenSequences;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.QuitHandler;
import jalview.project.Jalview2XML;
import jalview.renderer.ResidueShader;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.ColourSchemeI;
import jalview.structure.CommandListener;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.util.Comparison;
import jalview.util.MapList;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.viewmodel.styles.ViewStyle;
import jalview.workers.AlignCalcManager;
import jalview.workers.ComplementConsensusThread;
import jalview.workers.ConsensusThread;
import jalview.workers.StrucConsensusThread;
import jalview.workers.VarianceThread;

/**
 * base class holding visualization and analysis attributes and common logic for
 * an active alignment view displayed in the GUI
 * 
 * @author jimp
 * 
 */
public abstract class AlignmentViewport
        implements AlignViewportI, CommandListener, VamsasSource
{
  protected ViewportRanges ranges;

  protected ViewStyleI viewStyle = new ViewStyle();

  /**
   * A viewport that hosts the cDna view of this (protein), or vice versa (if
   * set).
   */
  AlignViewportI codingComplement = null;

  FeaturesDisplayedI featuresDisplayed = null;

  protected Deque<CommandI> historyList = new ArrayDeque<>();

  protected Deque<CommandI> redoList = new ArrayDeque<>();

  //protected GPCAPanel progressPanel;
  protected Analysis analysis;
  /**
   * used to determine if quit should be confirmed
   */
  private boolean savedUpToDate = false;

  /**
   * alignment displayed in the viewport. Please use get/setter
   */
  protected AlignmentI alignment;

  public AlignmentViewport(AlignmentI al)
  {
    setAlignment(al);
    ranges = new ViewportRanges(al);
  }

  /**
   * @param name
   * @see jalview.api.ViewStyleI#setFontName(java.lang.String)
   */
  @Override
  public void setFontName(String name)
  {
    viewStyle.setFontName(name);
  }

  /**
   * @param style
   * @see jalview.api.ViewStyleI#setFontStyle(int)
   */
  @Override
  public void setFontStyle(int style)
  {
    viewStyle.setFontStyle(style);
  }

  /**
   * @param size
   * @see jalview.api.ViewStyleI#setFontSize(int)
   */
  @Override
  public void setFontSize(int size)
  {
    viewStyle.setFontSize(size);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getFontStyle()
   */
  @Override
  public int getFontStyle()
  {
    return viewStyle.getFontStyle();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getFontName()
   */
  @Override
  public String getFontName()
  {
    return viewStyle.getFontName();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getFontSize()
   */
  @Override
  public int getFontSize()
  {
    return viewStyle.getFontSize();
  }

  /**
   * @param upperCasebold
   * @see jalview.api.ViewStyleI#setUpperCasebold(boolean)
   */
  @Override
  public void setUpperCasebold(boolean upperCasebold)
  {
    viewStyle.setUpperCasebold(upperCasebold);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isUpperCasebold()
   */
  @Override
  public boolean isUpperCasebold()
  {
    return viewStyle.isUpperCasebold();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isSeqNameItalics()
   */
  @Override
  public boolean isSeqNameItalics()
  {
    return viewStyle.isSeqNameItalics();
  }

  /**
   * @param colourByReferenceSeq
   * @see jalview.api.ViewStyleI#setColourByReferenceSeq(boolean)
   */
  @Override
  public void setColourByReferenceSeq(boolean colourByReferenceSeq)
  {
    viewStyle.setColourByReferenceSeq(colourByReferenceSeq);
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setColourAppliesToAllGroups(boolean)
   */
  @Override
  public void setColourAppliesToAllGroups(boolean b)
  {
    viewStyle.setColourAppliesToAllGroups(b);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getColourAppliesToAllGroups()
   */
  @Override
  public boolean getColourAppliesToAllGroups()
  {
    return viewStyle.getColourAppliesToAllGroups();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getAbovePIDThreshold()
   */
  @Override
  public boolean getAbovePIDThreshold()
  {
    return viewStyle.getAbovePIDThreshold();
  }

  /**
   * @param inc
   * @see jalview.api.ViewStyleI#setIncrement(int)
   */
  @Override
  public void setIncrement(int inc)
  {
    viewStyle.setIncrement(inc);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getIncrement()
   */
  @Override
  public int getIncrement()
  {
    return viewStyle.getIncrement();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setConservationSelected(boolean)
   */
  @Override
  public void setConservationSelected(boolean b)
  {
    viewStyle.setConservationSelected(b);
  }

  /**
   * @param show
   * @see jalview.api.ViewStyleI#setShowHiddenMarkers(boolean)
   */
  @Override
  public void setShowHiddenMarkers(boolean show)
  {
    viewStyle.setShowHiddenMarkers(show);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowHiddenMarkers()
   */
  @Override
  public boolean getShowHiddenMarkers()
  {
    return viewStyle.getShowHiddenMarkers();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setScaleRightWrapped(boolean)
   */
  @Override
  public void setScaleRightWrapped(boolean b)
  {
    viewStyle.setScaleRightWrapped(b);
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setScaleLeftWrapped(boolean)
   */
  @Override
  public void setScaleLeftWrapped(boolean b)
  {
    viewStyle.setScaleLeftWrapped(b);
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setScaleAboveWrapped(boolean)
   */
  @Override
  public void setScaleAboveWrapped(boolean b)
  {
    viewStyle.setScaleAboveWrapped(b);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getScaleLeftWrapped()
   */
  @Override
  public boolean getScaleLeftWrapped()
  {
    return viewStyle.getScaleLeftWrapped();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getScaleAboveWrapped()
   */
  @Override
  public boolean getScaleAboveWrapped()
  {
    return viewStyle.getScaleAboveWrapped();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getScaleRightWrapped()
   */
  @Override
  public boolean getScaleRightWrapped()
  {
    return viewStyle.getScaleRightWrapped();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setAbovePIDThreshold(boolean)
   */
  @Override
  public void setAbovePIDThreshold(boolean b)
  {
    viewStyle.setAbovePIDThreshold(b);
  }

  /**
   * @param thresh
   * @see jalview.api.ViewStyleI#setThreshold(int)
   */
  @Override
  public void setThreshold(int thresh)
  {
    viewStyle.setThreshold(thresh);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getThreshold()
   */
  @Override
  public int getThreshold()
  {
    return viewStyle.getThreshold();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowJVSuffix()
   */
  @Override
  public boolean getShowJVSuffix()
  {
    return viewStyle.getShowJVSuffix();
  }

  /**
   * @param b
   * @see jalview.api.ViewStyleI#setShowJVSuffix(boolean)
   */
  @Override
  public void setShowJVSuffix(boolean b)
  {
    viewStyle.setShowJVSuffix(b);
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setWrapAlignment(boolean)
   */
  @Override
  public void setWrapAlignment(boolean state)
  {
    viewStyle.setWrapAlignment(state);
    ranges.setWrappedMode(state);
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setShowText(boolean)
   */
  @Override
  public void setShowText(boolean state)
  {
    viewStyle.setShowText(state);
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setRenderGaps(boolean)
   */
  @Override
  public void setRenderGaps(boolean state)
  {
    viewStyle.setRenderGaps(state);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getColourText()
   */
  @Override
  public boolean getColourText()
  {
    return viewStyle.getColourText();
  }

  /**
   * @param state
   * @see jalview.api.ViewStyleI#setColourText(boolean)
   */
  @Override
  public void setColourText(boolean state)
  {
    viewStyle.setColourText(state);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getWrapAlignment()
   */
  @Override
  public boolean getWrapAlignment()
  {
    return viewStyle.getWrapAlignment();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowText()
   */
  @Override
  public boolean getShowText()
  {
    return viewStyle.getShowText();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getWrappedWidth()
   */
  @Override
  public int getWrappedWidth()
  {
    return viewStyle.getWrappedWidth();
  }

  /**
   * @param w
   * @see jalview.api.ViewStyleI#setWrappedWidth(int)
   */
  @Override
  public void setWrappedWidth(int w)
  {
    viewStyle.setWrappedWidth(w);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getCharHeight()
   */
  @Override
  public int getCharHeight()
  {
    return viewStyle.getCharHeight();
  }

  /**
   * @param h
   * @see jalview.api.ViewStyleI#setCharHeight(int)
   */
  @Override
  public void setCharHeight(int h)
  {
    viewStyle.setCharHeight(h);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getCharWidth()
   */
  @Override
  public int getCharWidth()
  {
    return viewStyle.getCharWidth();
  }

  /**
   * @param w
   * @see jalview.api.ViewStyleI#setCharWidth(int)
   */
  @Override
  public void setCharWidth(int w)
  {
    viewStyle.setCharWidth(w);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowBoxes()
   */
  @Override
  public boolean getShowBoxes()
  {
    return viewStyle.getShowBoxes();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getShowUnconserved()
   */
  @Override
  public boolean getShowUnconserved()
  {
    return viewStyle.getShowUnconserved();
  }

  /**
   * @param showunconserved
   * @see jalview.api.ViewStyleI#setShowUnconserved(boolean)
   */
  @Override
  public void setShowUnconserved(boolean showunconserved)
  {
    viewStyle.setShowUnconserved(showunconserved);
  }

  /**
   * @param default1
   * @see jalview.api.ViewStyleI#setSeqNameItalics(boolean)
   */
  @Override
  public void setSeqNameItalics(boolean default1)
  {
    viewStyle.setSeqNameItalics(default1);
  }

  @Override
  public AlignmentI getAlignment()
  {
    return alignment;
  }

  @Override
  public char getGapCharacter()
  {
    return alignment.getGapCharacter();
  }

  protected String sequenceSetID;

  /**
   * probably unused indicator that view is of a dataset rather than an
   * alignment
   */
  protected boolean isDataset = false;

  public void setDataset(boolean b)
  {
    isDataset = b;
  }

  public boolean isDataset()
  {
    return isDataset;
  }

  private Map<SequenceI, SequenceCollectionI> hiddenRepSequences;

  protected ColumnSelection colSel = new ColumnSelection();

  public boolean autoCalculateConsensus = true;

  public boolean autoCalculateVariance = false;

  protected boolean autoCalculateStrucConsensus = true;

  protected boolean ignoreGapsInConsensusCalculation = false;
  
  protected boolean ignoreGapsInVarianceCalculation = false;

  protected ResidueShaderI residueShading = new ResidueShader();

  @Override
  public void setGlobalColourScheme(ColourSchemeI cs)
  {
    // TODO: logic refactored from AlignFrame changeColour -
    // TODO: autorecalc stuff should be changed to rely on the worker system
    // check to see if we should implement a changeColour(cs) method rather than
    // put the logic in here
    // - means that caller decides if they want to just modify state and defer
    // calculation till later or to do all calculations in thread.
    // via changecolour

    /*
     * only instantiate alignment colouring once, thereafter update it;
     * this means that any conservation or PID threshold settings
     * persist when the alignment colour scheme is changed
     */
    if (residueShading == null)
    {
      residueShading = new ResidueShader(viewStyle);
    }
    residueShading.setColourScheme(cs);

    // TODO: do threshold and increment belong in ViewStyle or ResidueShader?
    // ...problem: groups need these, but do not currently have a ViewStyle

    if (cs != null)
    {
      if (getConservationSelected())
      {
        residueShading.setConservation(hconservation);
      }
      /*
       * reset conservation flag in case just set to false if
       * Conservation was null (calculation still in progress)
       */
      residueShading.setConservationApplied(getConservationSelected());
      residueShading.alignmentChanged(alignment, hiddenRepSequences);
    }

    /*
     * if 'apply colour to all groups' is selected... do so
     * (but don't transfer any colour threshold settings to groups)
     */
    if (getColourAppliesToAllGroups())
    {
      for (SequenceGroup sg : getAlignment().getGroups())
      {
        /*
         * retain any colour thresholds per group while
         * changing choice of colour scheme (JAL-2386)
         */
        sg.setColourScheme(cs == null ? null : cs.getInstance(this, sg));
        if (cs != null)
        {
          sg.getGroupColourScheme().alignmentChanged(sg,
                  hiddenRepSequences);
        }
      }
    }
  }

  @Override
  public ColourSchemeI getGlobalColourScheme()
  {
    return residueShading == null ? null : residueShading.getColourScheme();
  }

  @Override
  public ResidueShaderI getResidueShading()
  {
    return residueShading;
  }

  protected AlignmentAnnotation consensus;
  
  protected AlignmentAnnotation variance;

  protected AlignmentAnnotation complementConsensus;

  protected AlignmentAnnotation gapcounts;

  protected AlignmentAnnotation strucConsensus;

  protected AlignmentAnnotation conservation;

  protected AlignmentAnnotation quality;

  protected AlignmentAnnotation[] groupConsensus;
  
  protected AlignmentAnnotation[] groupVariance;

  protected AlignmentAnnotation[] groupConservation;

  /**
   * results of alignment consensus analysis for visible portion of view
   */
  protected ProfilesI hconsensus = null;

  /**
   * results of alignment variance analysis for visible portion of view
   */
  protected ProfilesI hvariance = null;

  /**
   * results of cDNA complement consensus visible portion of view
   */
  protected Hashtable<String, Object>[] hcomplementConsensus = null;

  /**
   * results of secondary structure base pair consensus for visible portion of
   * view
   */
  protected Hashtable<String, Object>[] hStrucConsensus = null;

  protected Conservation hconservation = null;

  @Override
  public void setConservation(Conservation cons)
  {
    hconservation = cons;
  }

  /**
   * percentage gaps allowed in a column before all amino acid properties should
   * be considered unconserved
   */
  int ConsPercGaps = 25; // JBPNote : This should be a scalable property!

  @Override
  public int getConsPercGaps()
  {
    return ConsPercGaps;
  }

  @Override
  public void setSequenceConsensusHash(ProfilesI hconsensus)
  {
    this.hconsensus = hconsensus;
  }

  @Override
  public void setSequenceVarianceHash(ProfilesI hvariance)
  {
    this.hvariance = hvariance;
  }

  @Override
  public void setComplementConsensusHash(
          Hashtable<String, Object>[] hconsensus)
  {
    this.hcomplementConsensus = hconsensus;
  }

  @Override
  public ProfilesI getSequenceConsensusHash()
  {
    return hconsensus;
  }

  @Override
  public ProfilesI getSequenceVarianceHash()
  {
    return hvariance;
  }

  @Override
  public Hashtable<String, Object>[] getComplementConsensusHash()
  {
    return hcomplementConsensus;
  }

  @Override
  public Hashtable<String, Object>[] getRnaStructureConsensusHash()
  {
    return hStrucConsensus;
  }

  @Override
  public void setRnaStructureConsensusHash(
          Hashtable<String, Object>[] hStrucConsensus)
  {
    this.hStrucConsensus = hStrucConsensus;

  }

  @Override
  public AlignmentAnnotation getAlignmentQualityAnnot()
  {
    return quality;
  }

  @Override
  public AlignmentAnnotation getAlignmentConservationAnnotation()
  {
    return conservation;
  }

  @Override
  public AlignmentAnnotation getAlignmentVarianceAnnotation()
  {
    return variance;
  }

  @Override
  public AlignmentAnnotation getAlignmentConsensusAnnotation()
  {
    return consensus;
  }

  @Override
  public AlignmentAnnotation getAlignmentGapAnnotation()
  {
    return gapcounts;
  }

  @Override
  public AlignmentAnnotation getComplementConsensusAnnotation()
  {
    return complementConsensus;
  }

  @Override
  public AlignmentAnnotation getAlignmentStrucConsensusAnnotation()
  {
    return strucConsensus;
  }

  protected AlignCalcManagerI calculator = new AlignCalcManager();

  /**
   * trigger update of conservation annotation
   */
  public void updateConservation(final AlignmentViewPanel ap)
  {
    // see note in mantis : issue number 8585
    if (alignment.isNucleotide()
            || (conservation == null && quality == null)
            || !autoCalculateConsensus)
    {
      return;
    }
    if (calculator.getRegisteredWorkersOfClass(
            jalview.workers.ConservationThread.class) == null)
    {
      calculator.registerWorker(
              new jalview.workers.ConservationThread(this, ap));
    }
  }

  /**
   * trigger update of consensus annotation
   */
  public void updateConsensus(final AlignmentViewPanel ap)
  {
    // see note in mantis : issue number 8585
    if (consensus == null || !autoCalculateConsensus)
    {
      return;
    }
    if (calculator
            .getRegisteredWorkersOfClass(ConsensusThread.class) == null)
    {
      calculator.registerWorker(new ConsensusThread(this, ap));
    }

    /*
     * A separate thread to compute cDNA consensus for a protein alignment
     * which has mapping to cDNA
     */
    final AlignmentI al = this.getAlignment();
    if (!al.isNucleotide() && al.getCodonFrames() != null
            && !al.getCodonFrames().isEmpty())
    {
      /*
       * fudge - check first for protein-to-nucleotide mappings
       * (we don't want to do this for protein-to-protein)
       */
      boolean doConsensus = false;
      for (AlignedCodonFrame mapping : al.getCodonFrames())
      {
        // TODO hold mapping type e.g. dna-to-protein in AlignedCodonFrame?
        MapList[] mapLists = mapping.getdnaToProt();
        // mapLists can be empty if project load has not finished resolving seqs
        if (mapLists.length > 0 && mapLists[0].getFromRatio() == 3)
        {
          doConsensus = true;
          break;
        }
      }
      if (doConsensus)
      {
        if (calculator.getRegisteredWorkersOfClass(
                ComplementConsensusThread.class) == null)
        {
          calculator
                  .registerWorker(new ComplementConsensusThread(this, ap));
        }
      }
    }
  }

  /**
   * trigger update of variance annotation
   */
  public void updateVariance(final AlignmentViewPanel ap)
  {
    // see note in mantis : issue number 8585
    if (variance == null || !autoCalculateVariance)
    {
      return;
    }
    if (calculator
            .getRegisteredWorkersOfClass(VarianceThread.class) == null)
    {
      calculator.registerWorker(new VarianceThread(this, ap));
    }

  }

  // --------START Structure Conservation
  public void updateStrucConsensus(final AlignmentViewPanel ap)
  {
    if (autoCalculateStrucConsensus && strucConsensus == null
            && alignment.isNucleotide() && alignment.hasRNAStructure())
    {
      // secondary structure has been added - so init the consensus line
      initRNAStructure();
    }

    // see note in mantis : issue number 8585
    if (strucConsensus == null || !autoCalculateStrucConsensus)
    {
      return;
    }
    if (calculator.getRegisteredWorkersOfClass(
            StrucConsensusThread.class) == null)
    {
      calculator.registerWorker(new StrucConsensusThread(this, ap));
    }
  }

  public boolean isCalcInProgress()
  {
    return calculator.isWorking();
  }

  @Override
  public boolean isCalculationInProgress(
          AlignmentAnnotation alignmentAnnotation)
  {
    if (!alignmentAnnotation.autoCalculated)
    {
      return false;
    }
    if (calculator.workingInvolvedWith(alignmentAnnotation))
    {
      // System.err.println("grey out ("+alignmentAnnotation.label+")");
      return true;
    }
    return false;
  }

  public void setAlignment(AlignmentI align)
  {
    this.alignment = align;
  }

  /**
   * Clean up references when this viewport is closed
   */
  @Override
  public void dispose()
  {
    /*
     * defensively null out references to large objects in case
     * this object is not garbage collected (as if!)
     */
    consensus = null;
    complementConsensus = null;
    strucConsensus = null;
    conservation = null;
    quality = null;
    groupConsensus = null;
    groupConservation = null;
    groupVariance = null;
    hconsensus = null;
    hvariance = null;
    hconservation = null;
    hcomplementConsensus = null;
    gapcounts = null;
    calculator = null;
    residueShading = null; // may hold a reference to Consensus
    changeSupport = null;
    ranges = null;
    currentTree = null;
    selectionGroup = null;
    colSel = null;
    setAlignment(null);
    variance = null;
    hvariance = null;
  }

  @Override
  public boolean isClosed()
  {
    // TODO: check that this isClosed is only true after panel is closed, not
    // before it is fully constructed.
    return alignment == null;
  }

  @Override
  public AlignCalcManagerI getCalcManager()
  {
    return calculator;
  }

  /**
   * should conservation rows be shown for groups
   */
  protected boolean showGroupConservation = false;

  /**
   * should consensus rows be shown for groups
   */
  protected boolean showGroupConsensus = false;
  
  /**
   * should variance rows be shown for groups
   */
  protected boolean showGroupVariance = false;

  /**
   * should consensus profile be rendered by default
   */
  protected boolean showSequenceLogo = false;

  /**
   * should consensus profile be rendered normalised to row height
   */
  protected boolean normaliseSequenceLogo = false;

  /**
   * should consensus histograms be rendered by default
   */
  protected boolean showConsensusHistogram = true;

  protected boolean showVarianceHistogram = false;

  /**
   * @return the showConsensusProfile
   */
  @Override
  public boolean isShowSequenceLogo()
  {
    return showSequenceLogo;
  }

  /**
   * @param showSequenceLogo
   *          the new value
   */
  public void setShowSequenceLogo(boolean showSequenceLogo)
  {
    if (showSequenceLogo != this.showSequenceLogo)
    {
      // TODO: decouple settings setting from calculation when refactoring
      // annotation update method from alignframe to viewport
      this.showSequenceLogo = showSequenceLogo;
      calculator.updateAnnotationFor(ConsensusThread.class);
      calculator.updateAnnotationFor(ComplementConsensusThread.class);
      calculator.updateAnnotationFor(StrucConsensusThread.class);
      calculator.updateAnnotationFor(VarianceThread.class);
    }
    this.showSequenceLogo = showSequenceLogo;
  }

  /**
   * @param showConsensusHistogram
   *          the showConsensusHistogram to set
   */
  public void setShowConsensusHistogram(boolean showConsensusHistogram)
  {
    this.showConsensusHistogram = showConsensusHistogram;
  }
  
  public void setShowVarianceHistogram(boolean svh)
  {
    this.showVarianceHistogram = svh;
  }
  public void setShowVariance(boolean sv)
  {
    this.showVariance = sv;
  }
  public boolean isShowVarianceHistogram()
  {
    return this.showVarianceHistogram;
  }
  public boolean isShowVariance()
  {
    return this.showVariance;
  }

  /**
   * @return the showGroupConservation
   */
  public boolean isShowGroupConservation()
  {
    return showGroupConservation;
  }

  /**
   * @param showGroupConservation
   *          the showGroupConservation to set
   */
  public void setShowGroupConservation(boolean showGroupConservation)
  {
    this.showGroupConservation = showGroupConservation;
  }

  /**
   * @return the showGroupConsensus
   */
  public boolean isShowGroupConsensus()
  {
    return showGroupConsensus;
  }

  /**
   * @param showGroupConsensus
   *          the showGroupConsensus to set
   */
  public void setShowGroupConsensus(boolean showGroupConsensus)
  {
    this.showGroupConsensus = showGroupConsensus;
  }
  
  /**
   * @return the showGroupVariance
   */
  public boolean isShowGroupVariance()
  {
    return showGroupVariance;
  }
  
  /**
   * @param showGroupVariance
   */
  public void setShowGroupVariance(boolean showGroupVariance)
  {
    this.showGroupVariance = showGroupVariance;
  }

  /**
   * 
   * @return flag to indicate if the consensus histogram should be rendered by
   *         default
   */
  @Override
  public boolean isShowConsensusHistogram()
  {
    return this.showConsensusHistogram;
  }

  /**
   * when set, updateAlignment will always ensure sequences are of equal length
   */
  private boolean padGaps = false;

  /**
   * when set, alignment should be reordered according to a newly opened tree
   */
  public boolean sortByTree = false;

  /**
   * 
   * 
   * @return null or the currently selected sequence region
   */
  @Override
  public SequenceGroup getSelectionGroup()
  {
    return selectionGroup;
  }

  /**
   * Set the selection group for this window. Also sets the current alignment as
   * the context for the group, if it does not already have one.
   * 
   * @param sg
   *          - group holding references to sequences in this alignment view
   * 
   */
  @Override
  public void setSelectionGroup(SequenceGroup sg)
  {
    if (analysis != null)
      analysis.recalc(sg, this);

    selectionGroup = sg;
    if (sg != null && sg.getContext() == null)
    {
      sg.setContext(alignment);
    }
  }

  public void setHiddenColumns(HiddenColumns hidden)
  {
    this.alignment.setHiddenColumns(hidden);
  }

  @Override
  public ColumnSelection getColumnSelection()
  {
    return colSel;
  }

  @Override
  public void setColumnSelection(ColumnSelection colSel)
  {
    this.colSel = colSel;
    if (colSel != null)
    {
      updateHiddenColumns();
    }
    isColSelChanged(true);
  }

  /**
   * 
   * @return
   */
  @Override
  public Map<SequenceI, SequenceCollectionI> getHiddenRepSequences()
  {
    return hiddenRepSequences;
  }

  @Override
  public void setHiddenRepSequences(
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    this.hiddenRepSequences = hiddenRepSequences;
  }

  @Override
  public boolean hasSelectedColumns()
  {
    ColumnSelection columnSelection = getColumnSelection();
    return columnSelection != null && columnSelection.hasSelectedColumns();
  }

  @Override
  public boolean hasHiddenColumns()
  {
    return alignment.getHiddenColumns() != null
            && alignment.getHiddenColumns().hasHiddenColumns();
  }

  public void updateHiddenColumns()
  {
    // this method doesn't really do anything now. But - it could, since a
    // column Selection could be in the process of modification
    // hasHiddenColumns = colSel.hasHiddenColumns();
  }

  @Override
  public boolean hasHiddenRows()
  {
    return alignment.getHiddenSequences().getSize() > 0;
  }

  protected SequenceGroup selectionGroup;

  public void setSequenceSetId(String newid)
  {
    if (sequenceSetID != null)
    {
      System.err.println(
              "Warning - overwriting a sequenceSetId for a viewport!");
    }
    sequenceSetID = new String(newid);
  }

  @Override
  public String getSequenceSetId()
  {
    if (sequenceSetID == null)
    {
      sequenceSetID = alignment.hashCode() + "";
    }

    return sequenceSetID;
  }

  /**
   * unique viewId for synchronizing state (e.g. with stored Jalview Project)
   * 
   */
  protected String viewId = null;

  @Override
  public String getViewId()
  {
    if (viewId == null)
    {
      viewId = this.getSequenceSetId() + "." + this.hashCode() + "";
    }
    return viewId;
  }

  public void setIgnoreGapsConsensus(boolean b, AlignmentViewPanel ap)
  {
    ignoreGapsInConsensusCalculation = b;
    if (ap != null)
    {
      updateConsensus(ap);
      if (residueShading != null)
      {
        residueShading.setThreshold(residueShading.getThreshold(),
                ignoreGapsInConsensusCalculation);
      }
    }

  }

  private long sgrouphash = -1, colselhash = -1;

  /**
   * checks current SelectionGroup against record of last hash value, and
   * updates record.
   * 
   * @param b
   *          update the record of last hash value
   * 
   * @return true if SelectionGroup changed since last call (when b is true)
   */
  public boolean isSelectionGroupChanged(boolean b)
  {
    int hc = (selectionGroup == null || selectionGroup.getSize() == 0) ? -1
            : selectionGroup.hashCode();
    if (hc != -1 && hc != sgrouphash)
    {
      if (b)
      {
        sgrouphash = hc;
      }
      return true;
    }
    return false;
  }

  /**
   * checks current colsel against record of last hash value, and optionally
   * updates record.
   * 
   * @param b
   *          update the record of last hash value
   * @return true if colsel changed since last call (when b is true)
   */
  public boolean isColSelChanged(boolean b)
  {
    int hc = (colSel == null || colSel.isEmpty()) ? -1 : colSel.hashCode();
    if (hc != -1 && hc != colselhash)
    {
      if (b)
      {
        colselhash = hc;
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean isIgnoreGapsConsensus()
  {
    return ignoreGapsInConsensusCalculation;
  }

  @Override
  public boolean isIgnoreGapsVariance()
  {
    return ignoreGapsInVarianceCalculation;
  }

  // property change stuff
  // JBPNote Prolly only need this in the applet version.
  private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
          this);

  protected boolean showConservation = true;

  protected boolean showQuality = true;

  protected boolean showConsensus = true;
  
  protected boolean showVariance = false;

  protected boolean showOccupancy = true;

  private Map<SequenceI, Color> sequenceColours = new HashMap<>();

  protected SequenceAnnotationOrder sortAnnotationsBy = null;

  protected boolean showAutocalculatedAbove;

  /**
   * when set, view will scroll to show the highlighted position
   */
  private boolean followHighlight = true;

  /**
   * Property change listener for changes in alignment
   * 
   * @param listener
   *          DOCUMENT ME!
   */
  public void addPropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param listener
   *          DOCUMENT ME!
   */
  public void removePropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    if (changeSupport != null)
    {
      changeSupport.removePropertyChangeListener(listener);
    }
  }

  /**
   * Property change listener for changes in alignment
   * 
   * @param prop
   *          DOCUMENT ME!
   * @param oldvalue
   *          DOCUMENT ME!
   * @param newvalue
   *          DOCUMENT ME!
   */
  public void firePropertyChange(String prop, Object oldvalue,
          Object newvalue)
  {
    changeSupport.firePropertyChange(prop, oldvalue, newvalue);
  }

  // common hide/show column stuff

  public void hideSelectedColumns()
  {
    if (colSel.isEmpty())
    {
      return;
    }

    colSel.hideSelectedColumns(alignment);
    setSelectionGroup(null);
    isColSelChanged(true);
  }

  public void hideColumns(int start, int end)
  {
    if (start == end)
    {
      colSel.hideSelectedColumns(start, alignment.getHiddenColumns());
    }
    else
    {
      alignment.getHiddenColumns().hideColumns(start, end);
    }
    isColSelChanged(true);
  }

  public void showColumn(int col)
  {
    alignment.getHiddenColumns().revealHiddenColumns(col, colSel);
    isColSelChanged(true);
  }

  public void showAllHiddenColumns()
  {
    alignment.getHiddenColumns().revealAllHiddenColumns(colSel);
    isColSelChanged(true);
  }

  // common hide/show seq stuff
  public void showAllHiddenSeqs()
  {
    int startSeq = ranges.getStartSeq();
    int endSeq = ranges.getEndSeq();

    if (alignment.getHiddenSequences().getSize() > 0)
    {
      if (selectionGroup == null)
      {
        selectionGroup = new SequenceGroup();
        selectionGroup.setEndRes(alignment.getWidth() - 1);
      }
      List<SequenceI> tmp = alignment.getHiddenSequences()
              .showAll(hiddenRepSequences);
      for (SequenceI seq : tmp)
      {
        selectionGroup.addSequence(seq, false);
        setSequenceAnnotationsVisible(seq, true);
      }

      hiddenRepSequences = null;

      ranges.setStartEndSeq(startSeq, endSeq + tmp.size());

      firePropertyChange("alignment", null, alignment.getSequences());
      // used to set hasHiddenRows/hiddenRepSequences here, after the property
      // changed event
      sendSelection();
    }
  }

  public void showSequence(int index)
  {
    int startSeq = ranges.getStartSeq();
    int endSeq = ranges.getEndSeq();

    List<SequenceI> tmp = alignment.getHiddenSequences().showSequence(index,
            hiddenRepSequences);
    if (tmp.size() > 0)
    {
      if (selectionGroup == null)
      {
        selectionGroup = new SequenceGroup();
        selectionGroup.setEndRes(alignment.getWidth() - 1);
      }

      for (SequenceI seq : tmp)
      {
        selectionGroup.addSequence(seq, false);
        setSequenceAnnotationsVisible(seq, true);
      }

      ranges.setStartEndSeq(startSeq, endSeq + tmp.size());

      firePropertyChange("alignment", null, alignment.getSequences());
      sendSelection();
    }
  }

  public void hideAllSelectedSeqs()
  {
    if (selectionGroup == null || selectionGroup.getSize() < 1)
    {
      return;
    }

    SequenceI[] seqs = selectionGroup.getSequencesInOrder(alignment);

    hideSequence(seqs);

    setSelectionGroup(null);
  }

  public void hideSequence(SequenceI[] seq)
  {
    /*
     * cache offset to first visible sequence
     */
    int startSeq = ranges.getStartSeq();

    if (seq != null)
    {
      for (int i = 0; i < seq.length; i++)
      {
        alignment.getHiddenSequences().hideSequence(seq[i]);
        setSequenceAnnotationsVisible(seq[i], false);
      }
      ranges.setStartSeq(startSeq);
      firePropertyChange("alignment", null, alignment.getSequences());
    }
  }

  /**
   * Hides the specified sequence, or the sequences it represents
   * 
   * @param sequence
   *          the sequence to hide, or keep as representative
   * @param representGroup
   *          if true, hide the current selection group except for the
   *          representative sequence
   */
  public void hideSequences(SequenceI sequence, boolean representGroup)
  {
    if (selectionGroup == null || selectionGroup.getSize() < 1)
    {
      hideSequence(new SequenceI[] { sequence });
      return;
    }

    if (representGroup)
    {
      hideRepSequences(sequence, selectionGroup);
      setSelectionGroup(null);
      return;
    }

    int gsize = selectionGroup.getSize();
    SequenceI[] hseqs = selectionGroup.getSequences()
            .toArray(new SequenceI[gsize]);

    hideSequence(hseqs);
    setSelectionGroup(null);
    sendSelection();
  }

  /**
   * Set visibility for any annotations for the given sequence.
   * 
   * @param sequenceI
   */
  protected void setSequenceAnnotationsVisible(SequenceI sequenceI,
          boolean visible)
  {
    AlignmentAnnotation[] anns = alignment.getAlignmentAnnotation();
    if (anns != null)
    {
      for (AlignmentAnnotation ann : anns)
      {
        if (ann.sequenceRef == sequenceI)
        {
          ann.visible = visible;
        }
      }
    }
  }

  public void hideRepSequences(SequenceI repSequence, SequenceGroup sg)
  {
    int sSize = sg.getSize();
    if (sSize < 2)
    {
      return;
    }

    if (hiddenRepSequences == null)
    {
      hiddenRepSequences = new Hashtable<>();
    }

    hiddenRepSequences.put(repSequence, sg);

    // Hide all sequences except the repSequence
    SequenceI[] seqs = new SequenceI[sSize - 1];
    int index = 0;
    for (int i = 0; i < sSize; i++)
    {
      if (sg.getSequenceAt(i) != repSequence)
      {
        if (index == sSize - 1)
        {
          return;
        }

        seqs[index++] = sg.getSequenceAt(i);
      }
    }
    sg.setSeqrep(repSequence); // note: not done in 2.7applet
    sg.setHidereps(true); // note: not done in 2.7applet
    hideSequence(seqs);

  }

  /**
   * 
   * @return null or the current reference sequence
   */
  public SequenceI getReferenceSeq()
  {
    return alignment.getSeqrep();
  }

  /**
   * @param seq
   * @return true iff seq is the reference for the alignment
   */
  public boolean isReferenceSeq(SequenceI seq)
  {
    return alignment.getSeqrep() == seq;
  }

  /**
   * 
   * @param seq
   * @return true if there are sequences represented by this sequence that are
   *         currently hidden
   */
  public boolean isHiddenRepSequence(SequenceI seq)
  {
    return (hiddenRepSequences != null
            && hiddenRepSequences.containsKey(seq));
  }

  /**
   * 
   * @param seq
   * @return null or a sequence group containing the sequences that seq
   *         represents
   */
  public SequenceGroup getRepresentedSequences(SequenceI seq)
  {
    return (SequenceGroup) (hiddenRepSequences == null ? null
            : hiddenRepSequences.get(seq));
  }

  @Override
  public int adjustForHiddenSeqs(int alignmentIndex)
  {
    return alignment.getHiddenSequences()
            .adjustForHiddenSeqs(alignmentIndex);
  }

  @Override
  public void invertColumnSelection()
  {
    colSel.invertColumnSelection(0, alignment.getWidth(), alignment);
    isColSelChanged(true);
  }

  @Override
  public SequenceI[] getSelectionAsNewSequence()
  {
    SequenceI[] sequences;
    // JBPNote: Need to test jalviewLite.getSelectedSequencesAsAlignmentFrom -
    // this was the only caller in the applet for this method
    // JBPNote: in applet, this method returned references to the alignment
    // sequences, and it did not honour the presence/absence of annotation
    // attached to the alignment (probably!)
    if (selectionGroup == null || selectionGroup.getSize() == 0)
    {
      sequences = alignment.getSequencesArray();
      AlignmentAnnotation[] annots = alignment.getAlignmentAnnotation();
      for (int i = 0; i < sequences.length; i++)
      {
        // construct new sequence with subset of visible annotation
        sequences[i] = new Sequence(sequences[i], annots);
      }
    }
    else
    {
      sequences = selectionGroup.getSelectionAsNewSequences(alignment);
    }

    return sequences;
  }

  @Override
  public SequenceI[] getSequenceSelection()
  {
    SequenceI[] sequences = null;
    if (selectionGroup != null)
    {
      sequences = selectionGroup.getSequencesInOrder(alignment);
    }
    if (sequences == null)
    {
      sequences = alignment.getSequencesArray();
    }
    return sequences;
  }

  @Override
  public jalview.datamodel.AlignmentView getAlignmentView(
          boolean selectedOnly)
  {
    return getAlignmentView(selectedOnly, false);
  }

  @Override
  public jalview.datamodel.AlignmentView getAlignmentView(
          boolean selectedOnly, boolean markGroups)
  {
    return new AlignmentView(alignment, alignment.getHiddenColumns(),
            selectionGroup,
            alignment.getHiddenColumns() != null
                    && alignment.getHiddenColumns().hasHiddenColumns(),
            selectedOnly, markGroups);
  }

  @Override
  public String[] getViewAsString(boolean selectedRegionOnly)
  {
    return getViewAsString(selectedRegionOnly, true);
  }

  @Override
  public String[] getViewAsString(boolean selectedRegionOnly,
          boolean exportHiddenSeqs)
  {
    String[] selection = null;
    SequenceI[] seqs = null;
    int i, iSize;
    int start = 0, end = 0;
    if (selectedRegionOnly && selectionGroup != null)
    {
      iSize = selectionGroup.getSize();
      seqs = selectionGroup.getSequencesInOrder(alignment);
      start = selectionGroup.getStartRes();
      end = selectionGroup.getEndRes() + 1;
    }
    else
    {
      if (hasHiddenRows() && exportHiddenSeqs)
      {
        AlignmentI fullAlignment = alignment.getHiddenSequences()
                .getFullAlignment();
        iSize = fullAlignment.getHeight();
        seqs = fullAlignment.getSequencesArray();
        end = fullAlignment.getWidth();
      }
      else
      {
        iSize = alignment.getHeight();
        seqs = alignment.getSequencesArray();
        end = alignment.getWidth();
      }
    }

    selection = new String[iSize];
    if (alignment.getHiddenColumns() != null
            && alignment.getHiddenColumns().hasHiddenColumns())
    {
      for (i = 0; i < iSize; i++)
      {
        Iterator<int[]> blocks = alignment.getHiddenColumns()
                .getVisContigsIterator(start, end + 1, false);
        selection[i] = seqs[i].getSequenceStringFromIterator(blocks);
      }
    }
    else
    {
      for (i = 0; i < iSize; i++)
      {
        selection[i] = seqs[i].getSequenceAsString(start, end);
      }

    }
    return selection;
  }

  @Override
  public List<int[]> getVisibleRegionBoundaries(int min, int max)
  {
    ArrayList<int[]> regions = new ArrayList<>();
    int start = min;
    int end = max;

    do
    {
      HiddenColumns hidden = alignment.getHiddenColumns();
      if (hidden != null && hidden.hasHiddenColumns())
      {
        if (start == 0)
        {
          start = hidden.visibleToAbsoluteColumn(start);
        }

        end = hidden.getNextHiddenBoundary(false, start);
        if (start == end)
        {
          end = max;
        }
        if (end > max)
        {
          end = max;
        }
      }

      regions.add(new int[] { start, end });

      if (hidden != null && hidden.hasHiddenColumns())
      {
        start = hidden.visibleToAbsoluteColumn(end);
        start = hidden.getNextHiddenBoundary(true, start) + 1;
      }
    } while (end < max);

    // int[][] startEnd = new int[regions.size()][2];

    return regions;
  }

  @Override
  public List<AlignmentAnnotation> getVisibleAlignmentAnnotation(
          boolean selectedOnly)
  {
    ArrayList<AlignmentAnnotation> ala = new ArrayList<>();
    AlignmentAnnotation[] aa;
    if ((aa = alignment.getAlignmentAnnotation()) != null)
    {
      for (AlignmentAnnotation annot : aa)
      {
        AlignmentAnnotation clone = new AlignmentAnnotation(annot);
        if (selectedOnly && selectionGroup != null)
        {
          clone.makeVisibleAnnotation(selectionGroup.getStartRes(),
                  selectionGroup.getEndRes(), alignment.getHiddenColumns());
        }
        else
        {
          clone.makeVisibleAnnotation(alignment.getHiddenColumns());
        }
        ala.add(clone);
      }
    }
    return ala;
  }

  @Override
  public boolean isPadGaps()
  {
    return padGaps;
  }

  @Override
  public void setPadGaps(boolean padGaps)
  {
    this.padGaps = padGaps;
  }

  /**
   * apply any post-edit constraints and trigger any calculations needed after
   * an edit has been performed on the alignment
   * 
   * @param ap
   */
  @Override
  public void alignmentChanged(AlignmentViewPanel ap)
  {
    if (isPadGaps())
    {
      alignment.padGaps();
    }
    if (autoCalculateConsensus)
    {
      updateConsensus(ap);
    }
    if (hconsensus != null && autoCalculateConsensus)
    {
      updateConservation(ap);
    }
    if (autoCalculateStrucConsensus)
    {
      updateStrucConsensus(ap);
    }
    if (hvariance != null && autoCalculateVariance)
    {
      updateVariance(ap);
    }

    // Reset endRes of groups if beyond alignment width
    int alWidth = alignment.getWidth();
    List<SequenceGroup> groups = alignment.getGroups();
    if (groups != null)
    {
      for (SequenceGroup sg : groups)
      {
        if (sg.getEndRes() > alWidth)
        {
          sg.setEndRes(alWidth - 1);
        }
      }
    }

    if (selectionGroup != null && selectionGroup.getEndRes() > alWidth)
    {
      selectionGroup.setEndRes(alWidth - 1);
    }

    updateAllColourSchemes();
    calculator.restartWorkers();
    // alignment.adjustSequenceAnnotations();
  }

  /**
   * reset scope and do calculations for all applied colourschemes on alignment
   */
  void updateAllColourSchemes()
  {
    ResidueShaderI rs = residueShading;
    if (rs != null)
    {
      rs.alignmentChanged(alignment, hiddenRepSequences);

      rs.setConsensus(hconsensus);
      //&!
      rs.setVariance(hvariance);
      if (rs.conservationApplied())
      {
        rs.setConservation(Conservation.calculateConservation("All",
                alignment.getSequences(), 0, alignment.getWidth(), false,
                getConsPercGaps(), false));
      }
    }

    for (SequenceGroup sg : alignment.getGroups())
    {
      if (sg.cs != null)
      {
        sg.cs.alignmentChanged(sg, hiddenRepSequences);
      }
      sg.recalcConservation();
    }
  }

  protected void initAutoAnnotation()
  {
    // TODO: add menu option action that nulls or creates consensus object
    // depending on if the user wants to see the annotation or not in a
    // specific alignment

    if (hconsensus == null && !isDataset)
    {
      if (!alignment.isNucleotide())
      {
        initConservation();
        initQuality();
      }
      else
      {
        initRNAStructure();
      }
      consensus = new AlignmentAnnotation("Consensus",
              MessageManager.getString("label.consensus_descr"),
              new Annotation[1], 0f, 100f, AlignmentAnnotation.BAR_GRAPH);
      initConsensus(consensus);
      initGapCounts();

      initComplementConsensus();
    }
    if (hvariance == null && !isDataset)
    {
      variance = new AlignmentAnnotation("Variance",
              //MessageManager.getString("label.consensus_descr"),
              "Most occurring variance per column",
              new Annotation[1], 0f, 100f, AlignmentAnnotation.BAR_GRAPH);
      initVariance(variance);
    }
  }

  /**
   * If this is a protein alignment and there are mappings to cDNA, adds the
   * cDNA consensus annotation and returns true, else returns false.
   */
  public boolean initComplementConsensus()
  {
    if (!alignment.isNucleotide())
    {
      final List<AlignedCodonFrame> codonMappings = alignment
              .getCodonFrames();
      if (codonMappings != null && !codonMappings.isEmpty())
      {
        boolean doConsensus = false;
        for (AlignedCodonFrame mapping : codonMappings)
        {
          // TODO hold mapping type e.g. dna-to-protein in AlignedCodonFrame?
          MapList[] mapLists = mapping.getdnaToProt();
          // mapLists can be empty if project load has not finished resolving
          // seqs
          if (mapLists.length > 0 && mapLists[0].getFromRatio() == 3)
          {
            doConsensus = true;
            break;
          }
        }
        if (doConsensus)
        {
          complementConsensus = new AlignmentAnnotation("cDNA Consensus",
                  MessageManager
                          .getString("label.complement_consensus_descr"),
                  new Annotation[1], 0f, 100f,
                  AlignmentAnnotation.BAR_GRAPH);
          initConsensus(complementConsensus);
          return true;
        }
      }
    }
    return false;
  }

  private void initConsensus(AlignmentAnnotation aa)
  {
    aa.hasText = true;
    aa.autoCalculated = true;

    if (showConsensus)
    {
      alignment.addAnnotation(aa);
    }
  }

  private void initVariance(AlignmentAnnotation aa)
  {
    aa.hasText = true;
    aa.autoCalculated = true;

    if (showVariance)
    {
      alignment.addAnnotation(aa);
    }
  }

  // these should be extracted from the view model - style and settings for
  // derived annotation
  private void initGapCounts()
  {
    if (showOccupancy)
    {
      gapcounts = new AlignmentAnnotation("Occupancy",
              MessageManager.getString("label.occupancy_descr"),
              new Annotation[1], 0f, alignment.getHeight(),
              AlignmentAnnotation.BAR_GRAPH);
      gapcounts.hasText = true;
      gapcounts.autoCalculated = true;
      gapcounts.scaleColLabel = true;
      gapcounts.graph = AlignmentAnnotation.BAR_GRAPH;

      alignment.addAnnotation(gapcounts);
    }
  }

  private void initConservation()
  {
    if (showConservation)
    {
      if (conservation == null)
      {
        conservation = new AlignmentAnnotation("Conservation",
                MessageManager.formatMessage("label.conservation_descr",
                        getConsPercGaps()),
                new Annotation[1], 0f, 11f, AlignmentAnnotation.BAR_GRAPH);
        conservation.hasText = true;
        conservation.autoCalculated = true;
        alignment.addAnnotation(conservation);
      }
    }
  }

  private void initQuality()
  {
    if (showQuality)
    {
      if (quality == null)
      {
        quality = new AlignmentAnnotation("Quality",
                MessageManager.getString("label.quality_descr"),
                new Annotation[1], 0f, 11f, AlignmentAnnotation.BAR_GRAPH);
        quality.hasText = true;
        quality.autoCalculated = true;
        alignment.addAnnotation(quality);
      }
    }
  }

  private void initRNAStructure()
  {
    if (alignment.hasRNAStructure() && strucConsensus == null)
    {
      strucConsensus = new AlignmentAnnotation("StrucConsensus",
              MessageManager.getString("label.strucconsensus_descr"),
              new Annotation[1], 0f, 100f, AlignmentAnnotation.BAR_GRAPH);
      strucConsensus.hasText = true;
      strucConsensus.autoCalculated = true;

      if (showConsensus)
      {
        alignment.addAnnotation(strucConsensus);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.api.AlignViewportI#calcPanelHeight()
   */
  @Override
  public int calcPanelHeight()
  {
    // setHeight of panels
    AlignmentAnnotation[] anns = getAlignment().getAlignmentAnnotation();
    int height = 0;
    int charHeight = getCharHeight();
    if (anns != null)
    {
      BitSet graphgrp = new BitSet();
      for (AlignmentAnnotation aa : anns)
      {
        if (aa == null)
        {
          System.err.println("Null annotation row: ignoring.");
          continue;
        }
        if (!aa.visible)
        {
          continue;
        }
        if (aa.graphGroup > -1)
        {
          if (graphgrp.get(aa.graphGroup))
          {
            continue;
          }
          else
          {
            graphgrp.set(aa.graphGroup);
          }
        }
        aa.height = 0;

        if (aa.hasText)
        {
          aa.height += charHeight;
        }

        if (aa.hasIcons)
        {
          aa.height += 16;
        }

        if (aa.graph > 0)
        {
          aa.height += aa.graphHeight+20;
        }

        if (aa.height == 0)
        {
          aa.height = 20;
        }

        height += aa.height;
      }
    }
    if (height == 0)
    {
      // set minimum
      height = 20;
    }
    return height;
  }

  @Override
  public void updateGroupAnnotationSettings(boolean applyGlobalSettings,
          boolean preserveNewGroupSettings)
  {
    boolean updateCalcs = false;
    boolean conv = isShowGroupConservation();
    boolean cons = isShowGroupConsensus();
    boolean showprf = isShowSequenceLogo();
    boolean showConsHist = isShowConsensusHistogram();
    boolean normLogo = isNormaliseSequenceLogo();
    boolean vari = isShowVariance();
    boolean showVarHist = isShowVarianceHistogram();

    /**
     * TODO reorder the annotation rows according to group/sequence ordering on
     * alignment
     */
    // boolean sortg = true;

    // remove old automatic annotation
    // add any new annotation

    // intersect alignment annotation with alignment groups

    AlignmentAnnotation[] aan = alignment.getAlignmentAnnotation();
    List<SequenceGroup> oldrfs = new ArrayList<>();
    if (aan != null)
    {
      for (int an = 0; an < aan.length; an++)
      {
        if (aan[an].autoCalculated && aan[an].groupRef != null)
        {
          oldrfs.add(aan[an].groupRef);
          alignment.deleteAnnotation(aan[an], false);
        }
      }
    }
    if (alignment.getGroups() != null)
    {
      for (SequenceGroup sg : alignment.getGroups())
      {
        updateCalcs = false;
        if (applyGlobalSettings
                || (!preserveNewGroupSettings && !oldrfs.contains(sg)))
        {
          // set defaults for this group's conservation/consensus
          sg.setshowSequenceLogo(showprf);
          sg.setShowConsensusHistogram(showConsHist);
          sg.setNormaliseSequenceLogo(normLogo);
          sg.setShowVarianceHistogram(showVarHist);
        }
        if (conv)
        {
          updateCalcs = true;
          alignment.addAnnotation(sg.getConservationRow(), 0);
        }
        if (cons)
        {
          updateCalcs = true;
          alignment.addAnnotation(sg.getConsensus(), 0);
        }
        //if (vari && sg.getSequences().size() == alignment.getHeight())
        //{
          //alignment.addAnnotation(sg.getVariance(), 0);
          //sg.recalcVariance(this);
        //}
        // refresh the annotation rows
        if (updateCalcs)
        {
          sg.recalcConservation();
        }
      }
    }
    if (vari)
    {
      /*
      List<SequenceI> bigGroupL = new ArrayList<SequenceI>();
      for (SequenceI seq : alignment.getSequencesArray())
      {
        bigGroupL.add(seq);
      }
      SequenceGroup bigGroup = new SequenceGroup(bigGroupL, "", null, false, true, false, alignment.getStartRes(), alignment.getEndRes(), alignment);
      //alignment.addGroup(bigGroup);
      alignment.addAnnotation(bigGroup.getVariance(), 0);
      bigGroup.recalcVariance(alignment);
      */
      RepeatingVariance.completeVariance(alignment);
      //alignment.addAnnotation(RepeatingVariance.getAnnotation(alignment));
    }
    oldrfs.clear();
  }

  @Override
  public boolean isDisplayReferenceSeq()
  {
    return alignment.hasSeqrep() && viewStyle.isDisplayReferenceSeq();
  }

  @Override
  public void setDisplayReferenceSeq(boolean displayReferenceSeq)
  {
    viewStyle.setDisplayReferenceSeq(displayReferenceSeq);
  }

  @Override
  public boolean isColourByReferenceSeq()
  {
    return alignment.hasSeqrep() && viewStyle.isColourByReferenceSeq();
  }

  @Override
  public Color getSequenceColour(SequenceI seq)
  {
    Color sqc = sequenceColours.get(seq);
    return (sqc == null ? Color.white : sqc);
  }

  @Override
  public void setSequenceColour(SequenceI seq, Color col)
  {
    if (col == null)
    {
      sequenceColours.remove(seq);
    }
    else
    {
      sequenceColours.put(seq, col);
    }
  }

  @Override
  public void updateSequenceIdColours()
  {
    for (SequenceGroup sg : alignment.getGroups())
    {
      if (sg.idColour != null)
      {
        for (SequenceI s : sg.getSequences(getHiddenRepSequences()))
        {
          sequenceColours.put(s, sg.idColour);
        }
      }
    }
  }

  @Override
  public void clearSequenceColours()
  {
    sequenceColours.clear();
  }

  @Override
  public AlignViewportI getCodingComplement()
  {
    return this.codingComplement;
  }

  /**
   * Set this as the (cDna/protein) complement of the given viewport. Also
   * ensures the reverse relationship is set on the given viewport.
   */
  @Override
  public void setCodingComplement(AlignViewportI av)
  {
    if (this == av)
    {
      System.err.println("Ignoring recursive setCodingComplement request");
    }
    else
    {
      this.codingComplement = av;
      // avoid infinite recursion!
      if (av.getCodingComplement() != this)
      {
        av.setCodingComplement(this);
      }
    }
  }

  @Override
  public boolean isNucleotide()
  {
    return getAlignment() == null ? false : getAlignment().isNucleotide();
  }

  @Override
  public FeaturesDisplayedI getFeaturesDisplayed()
  {
    return featuresDisplayed;
  }

  @Override
  public void setFeaturesDisplayed(FeaturesDisplayedI featuresDisplayedI)
  {
    featuresDisplayed = featuresDisplayedI;
  }

  @Override
  public boolean areFeaturesDisplayed()
  {
    return featuresDisplayed != null
            && featuresDisplayed.getRegisteredFeaturesCount() > 0;
  }

  /**
   * set the flag
   * 
   * @param b
   *          features are displayed if true
   */
  @Override
  public void setShowSequenceFeatures(boolean b)
  {
    viewStyle.setShowSequenceFeatures(b);
  }

  @Override
  public boolean isShowSequenceFeatures()
  {
    return viewStyle.isShowSequenceFeatures();
  }

  @Override
  public void setShowSequenceFeaturesHeight(boolean selected)
  {
    viewStyle.setShowSequenceFeaturesHeight(selected);
  }

  @Override
  public boolean isShowSequenceFeaturesHeight()
  {
    return viewStyle.isShowSequenceFeaturesHeight();
  }

  @Override
  public void setShowAnnotation(boolean b)
  {
    viewStyle.setShowAnnotation(b);
  }

  @Override
  public boolean isShowAnnotation()
  {
    return viewStyle.isShowAnnotation();
  }

  @Override
  public boolean isRightAlignIds()
  {
    return viewStyle.isRightAlignIds();
  }

  @Override
  public void setRightAlignIds(boolean rightAlignIds)
  {
    viewStyle.setRightAlignIds(rightAlignIds);
  }

  @Override
  public boolean getConservationSelected()
  {
    return viewStyle.getConservationSelected();
  }

  @Override
  public void setShowBoxes(boolean state)
  {
    viewStyle.setShowBoxes(state);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getTextColour()
   */
  @Override
  public Color getTextColour()
  {
    return viewStyle.getTextColour();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getTextColour2()
   */
  @Override
  public Color getTextColour2()
  {
    return viewStyle.getTextColour2();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getThresholdTextColour()
   */
  @Override
  public int getThresholdTextColour()
  {
    return viewStyle.getThresholdTextColour();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isConservationColourSelected()
   */
  @Override
  public boolean isConservationColourSelected()
  {
    return viewStyle.isConservationColourSelected();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isRenderGaps()
   */
  @Override
  public boolean isRenderGaps()
  {
    return viewStyle.isRenderGaps();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isShowColourText()
   */
  @Override
  public boolean isShowColourText()
  {
    return viewStyle.isShowColourText();
  }

  /**
   * @param conservationColourSelected
   * @see jalview.api.ViewStyleI#setConservationColourSelected(boolean)
   */
  @Override
  public void setConservationColourSelected(
          boolean conservationColourSelected)
  {
    viewStyle.setConservationColourSelected(conservationColourSelected);
  }

  /**
   * @param showColourText
   * @see jalview.api.ViewStyleI#setShowColourText(boolean)
   */
  @Override
  public void setShowColourText(boolean showColourText)
  {
    viewStyle.setShowColourText(showColourText);
  }

  /**
   * @param textColour
   * @see jalview.api.ViewStyleI#setTextColour(java.awt.Color)
   */
  @Override
  public void setTextColour(Color textColour)
  {
    viewStyle.setTextColour(textColour);
  }

  /**
   * @param thresholdTextColour
   * @see jalview.api.ViewStyleI#setThresholdTextColour(int)
   */
  @Override
  public void setThresholdTextColour(int thresholdTextColour)
  {
    viewStyle.setThresholdTextColour(thresholdTextColour);
  }

  /**
   * @param textColour2
   * @see jalview.api.ViewStyleI#setTextColour2(java.awt.Color)
   */
  @Override
  public void setTextColour2(Color textColour2)
  {
    viewStyle.setTextColour2(textColour2);
  }

  @Override
  public ViewStyleI getViewStyle()
  {
    return new ViewStyle(viewStyle);
  }

  @Override
  public void setViewStyle(ViewStyleI settingsForView)
  {
    viewStyle = new ViewStyle(settingsForView);
    if (residueShading != null)
    {
      residueShading.setConservationApplied(
              settingsForView.isConservationColourSelected());
    }
  }

  @Override
  public boolean sameStyle(ViewStyleI them)
  {
    return viewStyle.sameStyle(them);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#getIdWidth()
   */
  @Override
  public int getIdWidth()
  {
    return viewStyle.getIdWidth();
  }

  /**
   * @param i
   * @see jalview.api.ViewStyleI#setIdWidth(int)
   */
  @Override
  public void setIdWidth(int i)
  {
    viewStyle.setIdWidth(i);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isCentreColumnLabels()
   */
  @Override
  public boolean isCentreColumnLabels()
  {
    return viewStyle.isCentreColumnLabels();
  }

  /**
   * @param centreColumnLabels
   * @see jalview.api.ViewStyleI#setCentreColumnLabels(boolean)
   */
  @Override
  public void setCentreColumnLabels(boolean centreColumnLabels)
  {
    viewStyle.setCentreColumnLabels(centreColumnLabels);
  }

  /**
   * @param showdbrefs
   * @see jalview.api.ViewStyleI#setShowDBRefs(boolean)
   */
  @Override
  public void setShowDBRefs(boolean showdbrefs)
  {
    viewStyle.setShowDBRefs(showdbrefs);
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isShowDBRefs()
   */
  @Override
  public boolean isShowDBRefs()
  {
    return viewStyle.isShowDBRefs();
  }

  /**
   * @return
   * @see jalview.api.ViewStyleI#isShowNPFeats()
   */
  @Override
  public boolean isShowNPFeats()
  {
    return viewStyle.isShowNPFeats();
  }

  /**
   * @param shownpfeats
   * @see jalview.api.ViewStyleI#setShowNPFeats(boolean)
   */
  @Override
  public void setShowNPFeats(boolean shownpfeats)
  {
    viewStyle.setShowNPFeats(shownpfeats);
  }

  public abstract StructureSelectionManager getStructureSelectionManager();

  /**
   * Add one command to the command history list.
   * 
   * @param command
   */
  public void addToHistoryList(CommandI command)
  {
    if (this.historyList != null)
    {
      this.historyList.push(command);
      broadcastCommand(command, false);
      setSavedUpToDate(false);
      Jalview2XML.setStateSavedUpToDate(false);
    }
  }

  protected void broadcastCommand(CommandI command, boolean undo)
  {
    getStructureSelectionManager().commandPerformed(command, undo,
            getVamsasSource());
  }

  /**
   * Add one command to the command redo list.
   * 
   * @param command
   */
  public void addToRedoList(CommandI command)
  {
    if (this.redoList != null)
    {
      this.redoList.push(command);
    }
    broadcastCommand(command, true);
  }

  /**
   * Clear the command redo list.
   */
  public void clearRedoList()
  {
    if (this.redoList != null)
    {
      this.redoList.clear();
    }
  }

  public void setHistoryList(Deque<CommandI> list)
  {
    this.historyList = list;
  }

  public Deque<CommandI> getHistoryList()
  {
    return this.historyList;
  }

  public void setRedoList(Deque<CommandI> list)
  {
    this.redoList = list;
  }

  public Deque<CommandI> getRedoList()
  {
    return this.redoList;
  }

  @Override
  public VamsasSource getVamsasSource()
  {
    return this;
  }

  public SequenceAnnotationOrder getSortAnnotationsBy()
  {
    return sortAnnotationsBy;
  }

  public void setSortAnnotationsBy(
          SequenceAnnotationOrder sortAnnotationsBy)
  {
    this.sortAnnotationsBy = sortAnnotationsBy;
  }

  public boolean isShowAutocalculatedAbove()
  {
    return showAutocalculatedAbove;
  }

  public void setShowAutocalculatedAbove(boolean showAutocalculatedAbove)
  {
    this.showAutocalculatedAbove = showAutocalculatedAbove;
  }

  @Override
  public boolean isScaleProteinAsCdna()
  {
    return viewStyle.isScaleProteinAsCdna();
  }

  @Override
  public void setScaleProteinAsCdna(boolean b)
  {
    viewStyle.setScaleProteinAsCdna(b);
  }

  @Override
  public boolean isProteinFontAsCdna()
  {
    return viewStyle.isProteinFontAsCdna();
  }

  @Override
  public void setProteinFontAsCdna(boolean b)
  {
    viewStyle.setProteinFontAsCdna(b);
  }

  @Override
  public void setShowComplementFeatures(boolean b)
  {
    viewStyle.setShowComplementFeatures(b);
  }

  @Override
  public boolean isShowComplementFeatures()
  {
    return viewStyle.isShowComplementFeatures();
  }

  @Override
  public void setShowComplementFeaturesOnTop(boolean b)
  {
    viewStyle.setShowComplementFeaturesOnTop(b);
  }

  @Override
  public boolean isShowComplementFeaturesOnTop()
  {
    return viewStyle.isShowComplementFeaturesOnTop();
  }

  /**
   * @return true if view should scroll to show the highlighted region of a
   *         sequence
   * @return
   */
  @Override
  public final boolean isFollowHighlight()
  {
    return followHighlight;
  }

  @Override
  public final void setFollowHighlight(boolean b)
  {
    this.followHighlight = b;
  }

  @Override
  public ViewportRanges getRanges()
  {
    return ranges;
  }

  /**
   * Helper method to populate the SearchResults with the location in the
   * complementary alignment to scroll to, in order to match this one.
   * 
   * @param sr
   *          the SearchResults to add to
   * @return the offset (below top of visible region) of the matched sequence
   */
  protected int findComplementScrollTarget(SearchResultsI sr)
  {
    final AlignViewportI complement = getCodingComplement();
    if (complement == null || !complement.isFollowHighlight())
    {
      return 0;
    }
    boolean iAmProtein = !getAlignment().isNucleotide();
    AlignmentI proteinAlignment = iAmProtein ? getAlignment()
            : complement.getAlignment();
    if (proteinAlignment == null)
    {
      return 0;
    }
    final List<AlignedCodonFrame> mappings = proteinAlignment
            .getCodonFrames();

    /*
     * Heuristic: find the first mapped sequence (if any) with a non-gapped
     * residue in the middle column of the visible region. Scroll the
     * complementary alignment to line up the corresponding residue.
     */
    int seqOffset = 0;
    SequenceI sequence = null;

    /*
     * locate 'middle' column (true middle if an odd number visible, left of
     * middle if an even number visible)
     */
    int middleColumn = ranges.getStartRes()
            + (ranges.getEndRes() - ranges.getStartRes()) / 2;
    final HiddenSequences hiddenSequences = getAlignment()
            .getHiddenSequences();

    /*
     * searching to the bottom of the alignment gives smoother scrolling across
     * all gapped visible regions
     */
    int lastSeq = alignment.getHeight() - 1;
    List<AlignedCodonFrame> seqMappings = null;
    for (int seqNo = ranges
            .getStartSeq(); seqNo <= lastSeq; seqNo++, seqOffset++)
    {
      sequence = getAlignment().getSequenceAt(seqNo);
      if (hiddenSequences != null && hiddenSequences.isHidden(sequence))
      {
        continue;
      }
      if (Comparison.isGap(sequence.getCharAt(middleColumn)))
      {
        continue;
      }
      seqMappings = MappingUtils.findMappingsForSequenceAndOthers(sequence,
              mappings,
              getCodingComplement().getAlignment().getSequences());
      if (!seqMappings.isEmpty())
      {
        break;
      }
    }

    if (sequence == null || seqMappings == null || seqMappings.isEmpty())
    {
      /*
       * No ungapped mapped sequence in middle column - do nothing
       */
      return 0;
    }
    MappingUtils.addSearchResults(sr, sequence,
            sequence.findPosition(middleColumn), seqMappings);
    return seqOffset;
  }

  /**
   * synthesize a column selection if none exists so it covers the given
   * selection group. if wholewidth is false, no column selection is made if the
   * selection group covers the whole alignment width.
   * 
   * @param sg
   * @param wholewidth
   */
  public void expandColSelection(SequenceGroup sg, boolean wholewidth)
  {
    int sgs, sge;
    if (sg != null && (sgs = sg.getStartRes()) >= 0
            && sg.getStartRes() <= (sge = sg.getEndRes())
            && !this.hasSelectedColumns())
    {
      if (!wholewidth && alignment.getWidth() == (1 + sge - sgs))
      {
        // do nothing
        return;
      }
      if (colSel == null)
      {
        colSel = new ColumnSelection();
      }
      for (int cspos = sg.getStartRes(); cspos <= sg.getEndRes(); cspos++)
      {
        colSel.addElement(cspos);
      }
    }
  }

  /**
   * hold status of current selection group - defined on alignment or not.
   */
  private boolean selectionIsDefinedGroup = false;

  @Override
  public boolean isSelectionDefinedGroup()
  {
    if (selectionGroup == null)
    {
      return false;
    }
    if (isSelectionGroupChanged(true))
    {
      selectionIsDefinedGroup = false;
      List<SequenceGroup> gps = alignment.getGroups();
      if (gps == null || gps.size() == 0)
      {
        selectionIsDefinedGroup = false;
      }
      else
      {
        selectionIsDefinedGroup = gps.contains(selectionGroup);
      }
    }
    return selectionGroup.isDefined() || selectionIsDefinedGroup;
  }

  /**
   * null, or currently highlighted results on this view
   */
  private SearchResultsI searchResults = null;

  protected TreeModel currentTree = null;

  @Override
  public boolean hasSearchResults()
  {
    return searchResults != null;
  }

  @Override
  public void setSearchResults(SearchResultsI results)
  {
    searchResults = results;
  }

  @Override
  public SearchResultsI getSearchResults()
  {
    return searchResults;
  }

  @Override
  public ContactListI getContactList(AlignmentAnnotation _aa, int column)
  {
    return alignment.getContactListFor(_aa, column);
  }

  @Override
  public ContactMatrixI getContactMatrix(
          AlignmentAnnotation alignmentAnnotation)
  {
    return alignment.getContactMatrixFor(alignmentAnnotation);
  }

  /**
   * get the consensus sequence as displayed under the PID consensus annotation
   * row.
   * 
   * @return consensus sequence as a new sequence object
   */
  public SequenceI getConsensusSeq()
  {
    if (consensus == null)
    {
      updateConsensus(null);
    }
    if (consensus == null)
    {
      return null;
    }
    StringBuffer seqs = new StringBuffer();
    for (int i = 0; i < consensus.annotations.length; i++)
    {
      Annotation annotation = consensus.annotations[i];
      if (annotation != null)
      {
        String description = annotation.description;
        if (description != null && description.startsWith("["))
        {
          // consensus is a tie - just pick the first one
          seqs.append(description.charAt(1));
        }
        else
        {
          seqs.append(annotation.displayCharacter);
        }
      }
    }

    SequenceI sq = new Sequence("Consensus", seqs.toString());
    sq.setDescription("Percentage Identity Consensus "
            + ((ignoreGapsInConsensusCalculation) ? " without gaps" : ""));
    return sq;
  }

  @Override
  public void setCurrentTree(TreeModel tree)
  {
    currentTree = tree;
  }

  @Override
  public TreeModel getCurrentTree()
  {
    return currentTree;
  }

  @Override
  public AlignmentExportData getAlignExportData(
          AlignExportSettingsI options)
  {
    AlignmentI alignmentToExport = null;
    String[] omitHidden = null;
    alignmentToExport = null;

    if (hasHiddenColumns() && !options.isExportHiddenColumns())
    {
      omitHidden = getViewAsString(false,
              options.isExportHiddenSequences());
    }

    int[] alignmentStartEnd = new int[2];
    if (hasHiddenRows() && options.isExportHiddenSequences())
    {
      alignmentToExport = getAlignment().getHiddenSequences()
              .getFullAlignment();
    }
    else
    {
      alignmentToExport = getAlignment();
    }
    alignmentStartEnd = getAlignment().getHiddenColumns()
            .getVisibleStartAndEndIndex(alignmentToExport.getWidth());
    AlignmentExportData ed = new AlignmentExportData(alignmentToExport,
            omitHidden, alignmentStartEnd);
    return ed;
  }

  /**
   * flag set to indicate if structure views might be out of sync with sequences
   * in the alignment
   */

  private boolean needToUpdateStructureViews = false;

  @Override
  public boolean isUpdateStructures()
  {
    return needToUpdateStructureViews;
  }

  @Override
  public void setUpdateStructures(boolean update)
  {
    needToUpdateStructureViews = update;
  }

  @Override
  public boolean needToUpdateStructureViews()
  {
    boolean update = needToUpdateStructureViews;
    needToUpdateStructureViews = false;
    return update;
  }

  @Override
  public void addSequenceGroup(SequenceGroup sequenceGroup)
  {
    alignment.addGroup(sequenceGroup);

    Color col = sequenceGroup.idColour;
    if (col != null)
    {
      col = col.brighter();

      for (SequenceI sq : sequenceGroup.getSequences())
      {
        setSequenceColour(sq, col);
      }
    }

    if (codingComplement != null)
    {
      SequenceGroup mappedGroup = MappingUtils
              .mapSequenceGroup(sequenceGroup, this, codingComplement);
      if (mappedGroup.getSequences().size() > 0)
      {
        codingComplement.getAlignment().addGroup(mappedGroup);

        if (col != null)
        {
          for (SequenceI seq : mappedGroup.getSequences())
          {
            codingComplement.setSequenceColour(seq, col);
          }
        }
      }
      // propagate the structure view update flag according to our own setting
      codingComplement.setUpdateStructures(needToUpdateStructureViews);
    }
  }

  @Override
  public Iterator<int[]> getViewAsVisibleContigs(boolean selectedRegionOnly)
  {
    int start = 0;
    int end = 0;
    if (selectedRegionOnly && selectionGroup != null)
    {
      start = selectionGroup.getStartRes();
      end = selectionGroup.getEndRes() + 1;
    }
    else
    {
      end = alignment.getWidth();
    }
    return (alignment.getHiddenColumns().getVisContigsIterator(start, end,
            false));
  }

  public void setSavedUpToDate(boolean s)
  {
    setSavedUpToDate(s, QuitHandler.Message.UNSAVED_CHANGES);
  }

  public void setSavedUpToDate(boolean s, QuitHandler.Message m)
  {
    Console.debug(
            "Setting " + this.getViewId() + " setSavedUpToDate to " + s);
    savedUpToDate = s;
    QuitHandler.setMessage(m);
  }

  public boolean savedUpToDate()
  {
    Console.debug("Returning " + this.getViewId() + " savedUpToDate value: "
            + savedUpToDate);
    return savedUpToDate;
  }

  public Hashtable<SequenceI, Integer> calculateConnectivity(float[][] scores, byte dim)
  {
    return Connectivity.getConnectivity(this, scores, dim);
  }
  
  public Analysis getAnalysis()
  {
    return this.analysis;
  }
  
  public void setAnalysis(Analysis ana)
  {
    this.analysis = ana;
  }
}
