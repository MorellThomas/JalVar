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
package jalview.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JSplitPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import fr.orsay.lri.varna.VARNAPanel;
import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionLoadingFailed;
import fr.orsay.lri.varna.exceptions.ExceptionUnmatchedClosingParentheses;
import fr.orsay.lri.varna.interfaces.InterfaceVARNASelectionListener;
import fr.orsay.lri.varna.models.BaseList;
import fr.orsay.lri.varna.models.FullBackup;
import fr.orsay.lri.varna.models.annotations.HighlightRegionAnnotation;
import fr.orsay.lri.varna.models.rna.ModeleBase;
import fr.orsay.lri.varna.models.rna.RNA;
import jalview.analysis.AlignSeq;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.RnaViewerModel;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.ext.varna.RnaModel;
import jalview.structure.SecondaryStructureListener;
import jalview.structure.SelectionListener;
import jalview.structure.SelectionSource;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.util.Comparison;
import jalview.util.MessageManager;
import jalview.util.ShiftList;

public class AppVarna extends JInternalFrame
        implements SelectionListener, SecondaryStructureListener,
        InterfaceVARNASelectionListener, VamsasSource
{
  private static final byte[] PAIRS = new byte[] { '(', ')', '[', ']', '{',
      '}', '<', '>' };

  private AppVarnaBinding vab;

  private AlignmentPanel ap;

  private String viewId;

  private StructureSelectionManager ssm;

  /*
   * Lookup for sequence and annotation mapped to each RNA in the viewer. Using
   * a linked hashmap means that order is preserved when saved to the project.
   */
  private Map<RNA, RnaModel> models = new LinkedHashMap<RNA, RnaModel>();

  private Map<RNA, ShiftList> offsets = new Hashtable<RNA, ShiftList>();

  private Map<RNA, ShiftList> offsetsInv = new Hashtable<RNA, ShiftList>();

  private JSplitPane split;

  private VarnaHighlighter mouseOverHighlighter = new VarnaHighlighter();

  private VarnaHighlighter selectionHighlighter = new VarnaHighlighter();

  private class VarnaHighlighter
  {
    private HighlightRegionAnnotation _lastHighlight;

    private RNA _lastRNAhighlighted = null;

    public VarnaHighlighter()
    {

    }

    /**
     * Constructor when restoring from Varna session, including any highlight
     * state
     * 
     * @param rna
     */
    public VarnaHighlighter(RNA rna)
    {
      // TODO nice try but doesn't work; do we need a highlighter per model?
      _lastRNAhighlighted = rna;
      List<HighlightRegionAnnotation> highlights = rna.getHighlightRegion();
      if (highlights != null && !highlights.isEmpty())
      {
        _lastHighlight = highlights.get(0);
      }
    }

    /**
     * highlight a region from start to end (inclusive) on rna
     * 
     * @param rna
     * @param start
     *          - first base pair index (from 0)
     * @param end
     *          - last base pair index (from 0)
     */
    public void highlightRegion(RNA rna, int start, int end)
    {
      clearLastSelection();
      HighlightRegionAnnotation highlight = new HighlightRegionAnnotation(
              rna.getBasesBetween(start, end));
      rna.addHighlightRegion(highlight);
      _lastHighlight = highlight;
      _lastRNAhighlighted = rna;
    }

    public HighlightRegionAnnotation getLastHighlight()
    {
      return _lastHighlight;
    }

    /**
     * Clears all structure selection and refreshes the display
     */
    public void clearSelection()
    {
      if (_lastRNAhighlighted != null)
      {
        _lastRNAhighlighted.getHighlightRegion().clear();
        vab.updateSelectedRNA(_lastRNAhighlighted);
        _lastRNAhighlighted = null;
        _lastHighlight = null;
      }
    }

    /**
     * Clear the last structure selection
     */
    public void clearLastSelection()
    {
      if (_lastRNAhighlighted != null)
      {
        _lastRNAhighlighted.removeHighlightRegion(_lastHighlight);
        _lastRNAhighlighted = null;
        _lastHighlight = null;
      }
    }
  }

  /**
   * Constructor
   * 
   * @param seq
   *          the RNA sequence
   * @param aa
   *          the annotation with the secondary structure string
   * @param ap
   *          the AlignmentPanel creating this object
   */
  public AppVarna(SequenceI seq, AlignmentAnnotation aa, AlignmentPanel ap)
  {
    this(ap);

    String sname = aa.sequenceRef == null
            ? "secondary structure (alignment)"
            : seq.getName() + " structure";
    String theTitle = sname
            + (aa.sequenceRef == null ? " trimmed to " + seq.getName()
                    : "");
    theTitle = MessageManager.formatMessage("label.varna_params",
            new String[]
            { theTitle });
    setTitle(theTitle);

    String gappedTitle = sname + " (with gaps)";
    RnaModel gappedModel = new RnaModel(gappedTitle, aa, seq, null, true);
    addModel(gappedModel, gappedTitle);

    String trimmedTitle = "trimmed " + sname;
    RnaModel trimmedModel = new RnaModel(trimmedTitle, aa, seq, null,
            false);
    addModel(trimmedModel, trimmedTitle);
    vab.setSelectedIndex(0);
  }

  /**
   * Constructor that links the viewer to a parent panel (but has no structures
   * yet - use addModel to add them)
   * 
   * @param ap
   */
  protected AppVarna(AlignmentPanel ap)
  {
    this.setFrameIcon(null);
    this.ap = ap;
    this.viewId = System.currentTimeMillis() + "." + this.hashCode();
    vab = new AppVarnaBinding();
    initVarna();

    this.ssm = ap.getStructureSelectionManager();
    ssm.addStructureViewerListener(this);
    ssm.addSelectionListener(this);
    addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosed(InternalFrameEvent evt)
      {
        close();
      }
    });
  }

  /**
   * Constructor given viewer data read from a saved project file
   * 
   * @param model
   * @param ap
   *          the (or a) parent alignment panel
   */
  public AppVarna(RnaViewerModel model, AlignmentPanel ap)
  {
    this(ap);
    setTitle(model.title);
    this.viewId = model.viewId;
    setBounds(model.x, model.y, model.width, model.height);
    this.split.setDividerLocation(model.dividerLocation);
  }

  /**
   * Constructs a split pane with an empty selection list and display panel, and
   * adds it to the desktop
   */
  public void initVarna()
  {
    VARNAPanel varnaPanel = vab.get_varnaPanel();
    setBackground(Color.white);
    split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
            vab.getListPanel(), varnaPanel);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(split, BorderLayout.CENTER);

    varnaPanel.addSelectionListener(this);
    jalview.gui.Desktop.addInternalFrame(this, "", getBounds().width,
            getBounds().height);
    this.pack();
    showPanel(true);
  }

  /**
   * Constructs a new RNA model from the given one, without gaps. Also
   * calculates and saves a 'shift list'
   * 
   * @param rna
   * @param name
   * @return
   */
  public RNA trimRNA(RNA rna, String name)
  {
    ShiftList offset = new ShiftList();

    RNA rnaTrim = new RNA(name);
    try
    {
      String structDBN = rna.getStructDBN(true);
      rnaTrim.setRNA(rna.getSeq(), replaceOddGaps(structDBN));
    } catch (ExceptionUnmatchedClosingParentheses e2)
    {
      e2.printStackTrace();
    } catch (ExceptionFileFormatOrSyntax e3)
    {
      e3.printStackTrace();
    }

    String seq = rnaTrim.getSeq();
    StringBuilder struc = new StringBuilder(256);
    struc.append(rnaTrim.getStructDBN(true));
    int ofstart = -1;
    int sleng = seq.length();

    for (int i = 0; i < sleng; i++)
    {
      if (Comparison.isGap(seq.charAt(i)))
      {
        if (ofstart == -1)
        {
          ofstart = i;
        }
        /*
         * mark base or base & pair in the structure with *
         */
        if (!rnaTrim.findPair(i).isEmpty())
        {
          int m = rnaTrim.findPair(i).get(1);
          int l = rnaTrim.findPair(i).get(0);

          struc.replace(m, m + 1, "*");
          struc.replace(l, l + 1, "*");
        }
        else
        {
          struc.replace(i, i + 1, "*");
        }
      }
      else
      {
        if (ofstart > -1)
        {
          offset.addShift(offset.shift(ofstart), ofstart - i);
          ofstart = -1;
        }
      }
    }
    // final gap
    if (ofstart > -1)
    {
      offset.addShift(offset.shift(ofstart), ofstart - sleng);
      ofstart = -1;
    }

    /*
     * remove the marked gaps from the structure
     */
    String newStruc = struc.toString().replace("*", "");

    /*
     * remove gaps from the sequence
     */
    String newSeq = AlignSeq.extractGaps(Comparison.GapChars, seq);

    try
    {
      rnaTrim.setRNA(newSeq, newStruc);
      registerOffset(rnaTrim, offset);
    } catch (ExceptionUnmatchedClosingParentheses e)
    {
      e.printStackTrace();
    } catch (ExceptionFileFormatOrSyntax e)
    {
      e.printStackTrace();
    }
    return rnaTrim;
  }

  /**
   * Save the sequence to structure mapping, and also its inverse.
   * 
   * @param rnaTrim
   * @param offset
   */
  private void registerOffset(RNA rnaTrim, ShiftList offset)
  {
    offsets.put(rnaTrim, offset);
    offsetsInv.put(rnaTrim, offset.getInverse());
  }

  public void showPanel(boolean show)
  {
    this.setVisible(show);
  }

  /**
   * If a mouseOver event from the AlignmentPanel is noticed the currently
   * selected RNA in the VARNA window is highlighted at the specific position.
   * To be able to remove it before the next highlight it is saved in
   * _lastHighlight
   * 
   * @param sequence
   * @param index
   *          the aligned sequence position (base 0)
   * @param position
   *          the dataset sequence position (base 1)
   */
  @Override
  public void mouseOverSequence(SequenceI sequence, final int index,
          final int position)
  {
    RNA rna = vab.getSelectedRNA();
    if (rna == null)
    {
      return;
    }
    RnaModel rnaModel = models.get(rna);
    if (rnaModel.seq == sequence)
    {
      int highlightPos = rnaModel.gapped ? index
              : position - sequence.getStart();
      mouseOverHighlighter.highlightRegion(rna, highlightPos, highlightPos);
      vab.updateSelectedRNA(rna);
    }
  }

  @Override
  public void selection(SequenceGroup seqsel, ColumnSelection colsel,
          HiddenColumns hidden, SelectionSource source)
  {
    if (source != ap.av)
    {
      // ignore events from anything but our parent alignpanel
      // TODO - reuse many-one panel-view system in jmol viewer
      return;
    }
    RNA rna = vab.getSelectedRNA();
    if (rna == null)
    {
      return;
    }

    RnaModel rnaModel = models.get(rna);

    if (seqsel != null && seqsel.getSize() > 0
            && seqsel.contains(rnaModel.seq))
    {
      int start = seqsel.getStartRes(), end = seqsel.getEndRes();
      if (rnaModel.gapped)
      {
        ShiftList shift = offsets.get(rna);
        if (shift != null)
        {
          start = shift.shift(start);
          end = shift.shift(end);
        }
      }
      else
      {
        start = rnaModel.seq.findPosition(start) - rnaModel.seq.getStart();
        end = rnaModel.seq.findPosition(end) - rnaModel.seq.getStart();
      }

      selectionHighlighter.highlightRegion(rna, start, end);
      selectionHighlighter.getLastHighlight()
              .setOutlineColor(seqsel.getOutlineColour());
      // TODO - translate column markings to positions on structure if present.
      vab.updateSelectedRNA(rna);
    }
    else
    {
      selectionHighlighter.clearSelection();
    }
  }

  /**
   * Respond to a change of the base hovered over in the Varna viewer
   */
  @Override
  public void onHoverChanged(ModeleBase previousBase, ModeleBase newBase)
  {
    RNA rna = vab.getSelectedRNA();
    ShiftList shift = offsetsInv.get(rna);
    SequenceI seq = models.get(rna).seq;
    if (newBase != null && seq != null)
    {
      if (shift != null)
      {
        int i = shift.shift(newBase.getIndex());
        // System.err.println("shifted "+(arg1.getIndex())+" to "+i);
        ssm.mouseOverVamsasSequence(seq, i, this);
      }
      else
      {
        ssm.mouseOverVamsasSequence(seq, newBase.getIndex(), this);
      }
    }
  }

  @Override
  public void onSelectionChanged(BaseList arg0, BaseList arg1,
          BaseList arg2)
  {
    // TODO translate selected regions in VARNA to a selection on the
    // alignpanel.

  }

  /**
   * Returns the path to a temporary file containing a representation of the
   * state of one Varna display
   * 
   * @param rna
   * 
   * @return
   */
  public String getStateInfo(RNA rna)
  {
    return vab.getStateInfo(rna);
  }

  public AlignmentPanel getAlignmentPanel()
  {
    return ap;
  }

  public String getViewId()
  {
    return viewId;
  }

  /**
   * Returns true if any of the viewer's models (not necessarily the one
   * currently displayed) is for the given sequence
   * 
   * @param seq
   * @return
   */
  public boolean isListeningFor(SequenceI seq)
  {
    for (RnaModel model : models.values())
    {
      if (model.seq == seq)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a value representing the horizontal split divider location
   * 
   * @return
   */
  public int getDividerLocation()
  {
    return split == null ? 0 : split.getDividerLocation();
  }

  /**
   * Tidy up as necessary when the viewer panel is closed
   */
  protected void close()
  {
    /*
     * Deregister as a listener, to release references to this object
     */
    if (ssm != null)
    {
      ssm.removeStructureViewerListener(AppVarna.this, null);
      ssm.removeSelectionListener(AppVarna.this);
    }
  }

  /**
   * Returns the secondary structure annotation that this viewer displays for
   * the given sequence
   * 
   * @return
   */
  public AlignmentAnnotation getAnnotation(SequenceI seq)
  {
    for (RnaModel model : models.values())
    {
      if (model.seq == seq)
      {
        return model.ann;
      }
    }
    return null;
  }

  public int getSelectedIndex()
  {
    return this.vab.getSelectedIndex();
  }

  /**
   * Returns the set of models shown by the viewer
   * 
   * @return
   */
  public Collection<RnaModel> getModels()
  {
    return models.values();
  }

  /**
   * Add a model (e.g. loaded from project file)
   * 
   * @param rna
   * @param modelName
   */
  public RNA addModel(RnaModel model, String modelName)
  {
    if (!model.ann.isValidStruc())
    {
      throw new IllegalArgumentException(
              "Invalid RNA structure annotation");
    }

    /*
     * opened on request in Jalview session
     */
    RNA rna = new RNA(modelName);
    String struc = model.ann.getRNAStruc();
    struc = replaceOddGaps(struc);

    String strucseq = model.seq.getSequenceAsString();
    try
    {
      rna.setRNA(strucseq, struc);
    } catch (ExceptionUnmatchedClosingParentheses e2)
    {
      e2.printStackTrace();
    } catch (ExceptionFileFormatOrSyntax e3)
    {
      e3.printStackTrace();
    }

    if (!model.gapped)
    {
      rna = trimRNA(rna, modelName);
    }
    models.put(rna, new RnaModel(modelName, model.ann, model.seq, rna,
            model.gapped));
    vab.addStructure(rna);
    return rna;
  }

  /**
   * Constructs a shift list that describes the gaps in the sequence
   * 
   * @param seq
   * @return
   */
  protected ShiftList buildOffset(SequenceI seq)
  {
    // TODO refactor to avoid duplication with trimRNA()
    // TODO JAL-1789 bugs in use of ShiftList here
    ShiftList offset = new ShiftList();
    int ofstart = -1;
    int sleng = seq.getLength();

    for (int i = 0; i < sleng; i++)
    {
      if (Comparison.isGap(seq.getCharAt(i)))
      {
        if (ofstart == -1)
        {
          ofstart = i;
        }
      }
      else
      {
        if (ofstart > -1)
        {
          offset.addShift(offset.shift(ofstart), ofstart - i);
          ofstart = -1;
        }
      }
    }
    // final gap
    if (ofstart > -1)
    {
      offset.addShift(offset.shift(ofstart), ofstart - sleng);
      ofstart = -1;
    }
    return offset;
  }

  /**
   * Set the selected index in the model selection list
   * 
   * @param selectedIndex
   */
  public void setInitialSelection(final int selectedIndex)
  {
    /*
     * empirically it needs a second for Varna/AWT to finish loading/drawing
     * models for this to work; SwingUtilities.invokeLater _not_ a solution;
     * explanation and/or better solution welcome!
     */
    synchronized (this)
    {
      try
      {
        wait(1000);
      } catch (InterruptedException e)
      {
        // meh
      }
    }
    vab.setSelectedIndex(selectedIndex);
  }

  /**
   * Add a model with associated Varna session file
   * 
   * @param rna
   * @param modelName
   */
  public RNA addModelSession(RnaModel model, String modelName,
          String sessionFile)
  {
    if (!model.ann.isValidStruc())
    {
      throw new IllegalArgumentException(
              "Invalid RNA structure annotation");
    }

    try
    {
      FullBackup fromSession = vab.vp.loadSession(sessionFile);
      vab.addStructure(fromSession.rna, fromSession.config);
      RNA rna = fromSession.rna;
      // copy the model, but now including the RNA object
      RnaModel newModel = new RnaModel(model.title, model.ann, model.seq,
              rna, model.gapped);
      if (!model.gapped)
      {
        registerOffset(rna, buildOffset(model.seq));
      }
      models.put(rna, newModel);
      // capture rna selection state when saved
      selectionHighlighter = new VarnaHighlighter(rna);
      return fromSession.rna;
    } catch (ExceptionLoadingFailed e)
    {
      System.err
              .println("Error restoring Varna session: " + e.getMessage());
      return null;
    }
  }

  /**
   * Replace everything except RNA secondary structure characters with a period
   * 
   * @param s
   * @return
   */
  public static String replaceOddGaps(String s)
  {
    if (s == null)
    {
      return null;
    }

    // this is measured to be 10 times faster than a regex replace
    boolean changed = false;
    byte[] bytes = s.getBytes();
    for (int i = 0; i < bytes.length; i++)
    {
      boolean ok = false;
      // todo check for ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z')) if
      // wanted also
      for (int j = 0; !ok && (j < PAIRS.length); j++)
      {
        if (bytes[i] == PAIRS[j])
        {
          ok = true;
        }
      }
      if (!ok)
      {
        bytes[i] = '.';
        changed = true;
      }
    }
    return changed ? new String(bytes) : s;
  }
}
