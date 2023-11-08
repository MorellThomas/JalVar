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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jalview.api.AlignViewControllerGuiI;
import jalview.api.FeatureSettingsControllerI;
import jalview.api.SplitContainerI;
import jalview.controller.FeatureSettingsControllerGuiI;
import jalview.datamodel.AlignmentI;
import jalview.jbgui.GAlignFrame;
import jalview.jbgui.GSplitFrame;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.AlignmentViewport;

/**
 * An internal frame on the desktop that hosts a horizontally split view of
 * linked DNA and Protein alignments. Additional views can be created in linked
 * pairs, expanded to separate split frames, or regathered into a single frame.
 * <p>
 * (Some) operations on each alignment are automatically mirrored on the other.
 * These include mouseover (highlighting), sequence and column selection,
 * sequence ordering and sorting, and grouping, colouring and sorting by tree.
 * 
 * @author gmcarstairs
 *
 */
public class SplitFrame extends GSplitFrame implements SplitContainerI
{
  private static final int WINDOWS_INSETS_WIDTH = 28; // tbc

  private static final int MAC_INSETS_WIDTH = 28;

  private static final int WINDOWS_INSETS_HEIGHT = 50; // tbc

  private static final int MAC_INSETS_HEIGHT = 50;

  private static final int DESKTOP_DECORATORS_HEIGHT = 65;

  private static final long serialVersionUID = 1L;

  /**
   * geometry for Feature Settings Holder
   */
  private static final int FS_MIN_WIDTH = 400;

  private static final int FS_MIN_HEIGHT = 400;

  public SplitFrame(GAlignFrame top, GAlignFrame bottom)
  {
    super(top, bottom);
    init();
  }

  /**
   * Initialise this frame.
   */
  protected void init()
  {
    setFrameIcon(null);
    getTopFrame().setSplitFrame(this);
    getBottomFrame().setSplitFrame(this);
    getTopFrame().setVisible(true);
    getBottomFrame().setVisible(true);

    ((AlignFrame) getTopFrame()).getViewport().setCodingComplement(
            ((AlignFrame) getBottomFrame()).getViewport());

    /*
     * estimate width and height of SplitFrame; this.getInsets() doesn't seem to
     * give the full additional size (a few pixels short)
     */
    int widthFudge = Platform.isAMacAndNotJS() ? MAC_INSETS_WIDTH
            : WINDOWS_INSETS_WIDTH;
    int heightFudge = Platform.isAMacAndNotJS() ? MAC_INSETS_HEIGHT
            : WINDOWS_INSETS_HEIGHT;
    int width = ((AlignFrame) getTopFrame()).getWidth() + widthFudge;
    int height = ((AlignFrame) getTopFrame()).getHeight()
            + ((AlignFrame) getBottomFrame()).getHeight() + DIVIDER_SIZE
            + heightFudge;
    height = fitHeightToDesktop(height);
    setSize(width, height);

    adjustLayout();

    addCloseFrameListener();

    addKeyListener();

    addKeyBindings();

    addCommandListeners();
  }

  /**
   * Reduce the height if too large to fit in the Desktop. Also adjust the
   * divider location in proportion.
   * 
   * @param height
   *          in pixels
   * @return original or reduced height
   */
  public int fitHeightToDesktop(int height)
  {
    // allow about 65 pixels for Desktop decorators on Windows

    int newHeight = Math.min(height,
            Desktop.instance.getHeight() - DESKTOP_DECORATORS_HEIGHT);
    if (newHeight != height)
    {
      int oldDividerLocation = getDividerLocation();
      setDividerLocation(oldDividerLocation * newHeight / height);
    }
    return newHeight;
  }

  /**
   * Set the top and bottom frames to listen to each others Commands (e.g. Edit,
   * Order).
   */
  protected void addCommandListeners()
  {
    // TODO if CommandListener is only ever 1:1 for complementary views,
    // may change broadcast pattern to direct messaging (more efficient)
    final StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    ssm.addCommandListener(((AlignFrame) getTopFrame()).getViewport());
    ssm.addCommandListener(((AlignFrame) getBottomFrame()).getViewport());
  }

  /**
   * Do any tweaking and twerking of the layout wanted.
   */
  public void adjustLayout()
  {
    final AlignViewport topViewport = ((AlignFrame) getTopFrame()).viewport;
    final AlignViewport bottomViewport = ((AlignFrame) getBottomFrame()).viewport;

    /*
     * Ensure sequence ids are the same width so sequences line up
     */
    int w1 = topViewport.getIdWidth();
    int w2 = bottomViewport.getIdWidth();
    int w3 = Math.max(w1, w2);
    topViewport.setIdWidth(w3);
    bottomViewport.setIdWidth(w3);

    /*
     * Scale protein to either 1 or 3 times character width of dna
     */
    final AlignmentI topAlignment = topViewport.getAlignment();
    final AlignmentI bottomAlignment = bottomViewport.getAlignment();
    AlignmentViewport cdna = topAlignment.isNucleotide() ? topViewport
            : (bottomAlignment.isNucleotide() ? bottomViewport : null);
    AlignmentViewport protein = !topAlignment.isNucleotide() ? topViewport
            : (!bottomAlignment.isNucleotide() ? bottomViewport : null);
    if (protein != null && cdna != null)
    {
      int scale = protein.isScaleProteinAsCdna() ? 3 : 1;
      protein.setCharWidth(scale * cdna.getViewStyle().getCharWidth());
    }
  }

  /**
   * Adjusts the divider for a sensible split of the real estate (for example,
   * when many transcripts are shown with a single protein). This should only be
   * called after the split pane has been laid out (made visible) so it has a
   * height. The aim is to avoid unnecessary vertical scroll bars, while
   * ensuring that at least 2 sequences are visible in each panel.
   * <p>
   * Once laid out, the user may choose to customise as they wish, so this
   * method is not called again after the initial layout.
   */
  protected void adjustInitialLayout()
  {
    AlignFrame topFrame = (AlignFrame) getTopFrame();
    AlignFrame bottomFrame = (AlignFrame) getBottomFrame();

    /*
     * recompute layout of top and bottom panels to reflect their
     * actual (rather than requested) height
     */
    topFrame.alignPanel.adjustAnnotationHeight();
    bottomFrame.alignPanel.adjustAnnotationHeight();

    final AlignViewport topViewport = topFrame.viewport;
    final AlignViewport bottomViewport = bottomFrame.viewport;
    final AlignmentI topAlignment = topViewport.getAlignment();
    final AlignmentI bottomAlignment = bottomViewport.getAlignment();
    boolean topAnnotations = topViewport.isShowAnnotation();
    boolean bottomAnnotations = bottomViewport.isShowAnnotation();
    // TODO need number of visible sequences here, not #sequences - how?
    int topCount = topAlignment.getHeight();
    int bottomCount = bottomAlignment.getHeight();
    int topCharHeight = topViewport.getViewStyle().getCharHeight();
    int bottomCharHeight = bottomViewport.getViewStyle().getCharHeight();

    /*
     * calculate the minimum ratio that leaves at least the height 
     * of two sequences (after rounding) visible in the top panel
     */
    int topPanelHeight = topFrame.getHeight();
    int bottomPanelHeight = bottomFrame.getHeight();
    int topSequencesHeight = topFrame.alignPanel.getSeqPanel().seqCanvas
            .getHeight();
    int topPanelMinHeight = topPanelHeight
            - Math.max(0, topSequencesHeight - 3 * topCharHeight);
    double totalHeight = (double) topPanelHeight + bottomPanelHeight;
    double minRatio = topPanelMinHeight / totalHeight;

    /*
     * calculate the maximum ratio that leaves at least the height 
     * of two sequences (after rounding) visible in the bottom panel
     */
    int bottomSequencesHeight = bottomFrame.alignPanel
            .getSeqPanel().seqCanvas.getHeight();
    int bottomPanelMinHeight = bottomPanelHeight
            - Math.max(0, bottomSequencesHeight - 3 * bottomCharHeight);
    double maxRatio = (totalHeight - bottomPanelMinHeight) / totalHeight;

    /*
     * estimate ratio of (topFrameContent / bottomFrameContent)
     */
    int insets = Platform.isAMacAndNotJS() ? MAC_INSETS_HEIGHT
            : WINDOWS_INSETS_HEIGHT;
    // allow 3 'rows' for scale, scrollbar, status bar
    int topHeight = insets + (3 + topCount) * topCharHeight
            + (topAnnotations ? topViewport.calcPanelHeight() : 0);
    int bottomHeight = insets + (3 + bottomCount) * bottomCharHeight
            + (bottomAnnotations ? bottomViewport.calcPanelHeight() : 0);
    double ratio = ((double) topHeight)
            / (double) (topHeight + bottomHeight);

    /*
     * limit ratio to avoid concealing all sequences
     */
    ratio = Math.min(ratio, maxRatio);
    ratio = Math.max(ratio, minRatio);
    setRelativeDividerLocation(ratio);
  }

  /**
   * Add a listener to tidy up when the frame is closed.
   */
  protected void addCloseFrameListener()
  {
    addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosed(InternalFrameEvent evt)
      {
        close();
      };
    });
  }

  /**
   * Add a key listener that delegates to whichever split component the mouse is
   * in (or does nothing if neither).
   */
  protected void addKeyListener()
  {
    addKeyListener(new KeyAdapter()
    {

      @Override
      public void keyPressed(KeyEvent e)
      {
        AlignFrame af = (AlignFrame) getFrameAtMouse();

        /*
         * Intercept and override any keys here if wanted.
         */
        if (!overrideKey(e, af))
        {
          if (af != null)
          {
            for (KeyListener kl : af.getKeyListeners())
            {
              kl.keyPressed(e);
            }
          }
        }
      }

      @Override
      public void keyReleased(KeyEvent e)
      {
        Component c = getFrameAtMouse();
        if (c != null)
        {
          for (KeyListener kl : c.getKeyListeners())
          {
            kl.keyReleased(e);
          }
        }
      }

    });
  }

  /**
   * Returns true if the key event is overriden and actioned (or ignored) here,
   * else returns false, indicating it should be delegated to the AlignFrame's
   * usual handler.
   * <p>
   * We can't handle Cmd-Key combinations here, instead this is done by
   * overriding key bindings.
   * 
   * @see addKeyOverrides
   * @param e
   * @param af
   * @return
   */
  protected boolean overrideKey(KeyEvent e, AlignFrame af)
  {
    boolean actioned = false;
    int keyCode = e.getKeyCode();
    switch (keyCode)
    {
    case KeyEvent.VK_DOWN:
      if (e.isAltDown() || !af.viewport.cursorMode)
      {
        /*
         * Key down (or Alt-key-down in cursor mode) - move selected sequences
         */
        ((AlignFrame) getTopFrame()).moveSelectedSequences(false);
        ((AlignFrame) getBottomFrame()).moveSelectedSequences(false);
        actioned = true;
        e.consume();
      }
      break;
    case KeyEvent.VK_UP:
      if (e.isAltDown() || !af.viewport.cursorMode)
      {
        /*
         * Key up (or Alt-key-up in cursor mode) - move selected sequences
         */
        ((AlignFrame) getTopFrame()).moveSelectedSequences(true);
        ((AlignFrame) getBottomFrame()).moveSelectedSequences(true);
        actioned = true;
        e.consume();
      }
      break;
    default:
    }
    return actioned;
  }

  /**
   * Set key bindings (recommended for Swing over key accelerators).
   */
  private void addKeyBindings()
  {
    overrideDelegatedKeyBindings();

    overrideImplementedKeyBindings();
  }

  /**
   * Override key bindings with alternative action methods implemented in this
   * class.
   */
  protected void overrideImplementedKeyBindings()
  {
    overrideFind();
    overrideNewView();
    overrideCloseView();
    overrideExpandViews();
    overrideGatherViews();
  }

  /**
   * Replace Cmd-W close view action with our version.
   */
  protected void overrideCloseView()
  {
    AbstractAction action;
    /*
     * Ctrl-W / Cmd-W - close view or window
     */
    KeyStroke key_cmdW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
            jalview.util.ShortcutKeyMaskExWrapper
                    .getMenuShortcutKeyMaskEx(),
            false);
    action = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeView_actionPerformed();
      }
    };
    overrideKeyBinding(key_cmdW, action);
  }

  /**
   * Replace Cmd-T new view action with our version.
   */
  protected void overrideNewView()
  {
    /*
     * Ctrl-T / Cmd-T open new view
     */
    KeyStroke key_cmdT = KeyStroke.getKeyStroke(KeyEvent.VK_T,
            jalview.util.ShortcutKeyMaskExWrapper
                    .getMenuShortcutKeyMaskEx(),
            false);
    AbstractAction action = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        newView_actionPerformed();
      }
    };
    overrideKeyBinding(key_cmdT, action);
  }

  /**
   * For now, delegates key events to the corresponding key accelerator for the
   * AlignFrame that the mouse is in. Hopefully can be simplified in future if
   * AlignFrame is changed to use key bindings rather than accelerators.
   */
  protected void overrideDelegatedKeyBindings()
  {
    if (getTopFrame() instanceof AlignFrame)
    {
      /*
       * Get all accelerator keys in the top frame (the bottom should be
       * identical) and override each one.
       */
      for (Entry<KeyStroke, JMenuItem> acc : ((AlignFrame) getTopFrame())
              .getAccelerators().entrySet())
      {
        overrideKeyBinding(acc);
      }
    }
  }

  /**
   * Overrides an AlignFrame key accelerator with our version which delegates to
   * the action listener in whichever frame has the mouse (and does nothing if
   * neither has).
   * 
   * @param acc
   */
  private void overrideKeyBinding(Entry<KeyStroke, JMenuItem> acc)
  {
    final KeyStroke ks = acc.getKey();
    InputMap inputMap = this.getInputMap(JComponent.WHEN_FOCUSED);
    inputMap.put(ks, ks);
    this.getActionMap().put(ks, new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        Component c = getFrameAtMouse();
        if (c != null && c instanceof AlignFrame)
        {
          for (ActionListener a : ((AlignFrame) c).getAccelerators().get(ks)
                  .getActionListeners())
          {
            a.actionPerformed(null);
          }
        }
      }
    });
  }

  /**
   * Replace an accelerator key's action with the specified action.
   * 
   * @param ks
   */
  protected void overrideKeyBinding(KeyStroke ks, AbstractAction action)
  {
    this.getActionMap().put(ks, action);
    overrideMenuItem(ks, action);
  }

  /**
   * Create and link new views (with matching names) in both panes.
   * <p>
   * Note this is _not_ multiple tabs, each hosting a split pane view, rather it
   * is a single split pane with each split holding multiple tabs which are
   * linked in pairs.
   * <p>
   * TODO implement instead with a tabbed holder in the SplitView, each tab
   * holding a single JSplitPane. Would avoid a duplicated tab, at the cost of
   * some additional coding.
   */
  protected void newView_actionPerformed()
  {
    AlignFrame topFrame = (AlignFrame) getTopFrame();
    AlignFrame bottomFrame = (AlignFrame) getBottomFrame();
    final boolean scaleProteinAsCdna = topFrame.viewport
            .isScaleProteinAsCdna();

    AlignmentPanel newTopPanel = topFrame.newView(null, true);
    AlignmentPanel newBottomPanel = bottomFrame.newView(null, true);

    /*
     * This currently (for the first new view only) leaves the top pane on tab 0
     * but the bottom on tab 1. This results from 'setInitialTabVisible' echoing
     * from the bottom back to the first frame. Next line is a fudge to work
     * around this. TODO find a better way.
     */
    if (topFrame.getTabIndex() != bottomFrame.getTabIndex())
    {
      topFrame.setDisplayedView(newTopPanel);
    }

    newBottomPanel.av.setViewName(newTopPanel.av.getViewName());
    newTopPanel.av.setCodingComplement(newBottomPanel.av);

    /*
     * These lines can be removed once scaleProteinAsCdna is added to element
     * Viewport in jalview.xsd, as Jalview2XML.copyAlignPanel will then take
     * care of it
     */
    newTopPanel.av.setScaleProteinAsCdna(scaleProteinAsCdna);
    newBottomPanel.av.setScaleProteinAsCdna(scaleProteinAsCdna);

    /*
     * Line up id labels etc
     */
    adjustLayout();

    final StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    ssm.addCommandListener(newTopPanel.av);
    ssm.addCommandListener(newBottomPanel.av);
  }

  /**
   * Close the currently selected view in both panes. If there is only one view,
   * close this split frame.
   */
  protected void closeView_actionPerformed()
  {
    int viewCount = ((AlignFrame) getTopFrame()).getAlignPanels().size();
    if (viewCount < 2)
    {
      close();
      return;
    }

    AlignmentPanel topPanel = ((AlignFrame) getTopFrame()).alignPanel;
    AlignmentPanel bottomPanel = ((AlignFrame) getBottomFrame()).alignPanel;

    ((AlignFrame) getTopFrame()).closeView(topPanel);
    ((AlignFrame) getBottomFrame()).closeView(bottomPanel);

  }

  /**
   * Close child frames and this split frame.
   */
  public void close()
  {
    ((AlignFrame) getTopFrame()).closeMenuItem_actionPerformed(true);
    ((AlignFrame) getBottomFrame()).closeMenuItem_actionPerformed(true);
    try
    {
      this.setClosed(true);
    } catch (PropertyVetoException e)
    {
      // ignore
    }
  }

  /**
   * Replace AlignFrame 'expand views' action with SplitFrame version.
   */
  protected void overrideExpandViews()
  {
    KeyStroke key_X = KeyStroke.getKeyStroke(KeyEvent.VK_X, 0, false);
    AbstractAction action = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        expandViews_actionPerformed();
      }
    };
    overrideMenuItem(key_X, action);
  }

  /**
   * Replace AlignFrame 'gather views' action with SplitFrame version.
   */
  protected void overrideGatherViews()
  {
    KeyStroke key_G = KeyStroke.getKeyStroke(KeyEvent.VK_G, 0, false);
    AbstractAction action = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        gatherViews_actionPerformed();
      }
    };
    overrideMenuItem(key_G, action);
  }

  /**
   * Override the menu action associated with the keystroke in the child frames,
   * replacing it with the given action.
   * 
   * @param ks
   * @param action
   */
  private void overrideMenuItem(KeyStroke ks, AbstractAction action)
  {
    overrideMenuItem(ks, action, getTopFrame());
    overrideMenuItem(ks, action, getBottomFrame());
  }

  /**
   * Override the menu action associated with the keystroke in one child frame,
   * replacing it with the given action. Mwahahahaha.
   * 
   * @param key
   * @param action
   * @param comp
   */
  private void overrideMenuItem(KeyStroke key, final AbstractAction action,
          JComponent comp)
  {
    if (comp instanceof AlignFrame)
    {
      JMenuItem mi = ((AlignFrame) comp).getAccelerators().get(key);
      if (mi != null)
      {
        for (ActionListener al : mi.getActionListeners())
        {
          mi.removeActionListener(al);
        }
        mi.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            action.actionPerformed(e);
          }
        });
      }
    }
  }

  /**
   * Expand any multiple views (which are always in pairs) into separate split
   * frames.
   */
  protected void expandViews_actionPerformed()
  {
    Desktop.instance.explodeViews(this);
  }

  /**
   * Gather any other SplitFrame views of this alignment back in as multiple
   * (pairs of) views in this SplitFrame.
   */
  protected void gatherViews_actionPerformed()
  {
    Desktop.instance.gatherViews(this);
  }

  /**
   * Returns the alignment in the complementary frame to the one given.
   */
  @Override
  public AlignmentI getComplement(Object alignFrame)
  {
    if (alignFrame == this.getTopFrame())
    {
      return ((AlignFrame) getBottomFrame()).viewport.getAlignment();
    }
    else if (alignFrame == this.getBottomFrame())
    {
      return ((AlignFrame) getTopFrame()).viewport.getAlignment();
    }
    return null;
  }

  /**
   * Returns the title of the complementary frame to the one given.
   */
  @Override
  public String getComplementTitle(Object alignFrame)
  {
    if (alignFrame == this.getTopFrame())
    {
      return ((AlignFrame) getBottomFrame()).getTitle();
    }
    else if (alignFrame == this.getBottomFrame())
    {
      return ((AlignFrame) getTopFrame()).getTitle();
    }
    return null;
  }

  /**
   * Set the 'other half' to hidden / revealed.
   */
  @Override
  public void setComplementVisible(Object alignFrame, boolean show)
  {
    /*
     * Hiding the AlignPanel suppresses unnecessary repaints
     */
    if (alignFrame == getTopFrame())
    {
      ((AlignFrame) getBottomFrame()).alignPanel.setVisible(show);
    }
    else if (alignFrame == getBottomFrame())
    {
      ((AlignFrame) getTopFrame()).alignPanel.setVisible(show);
    }
    super.setComplementVisible(alignFrame, show);
  }

  /**
   * return the AlignFrames held by this container
   * 
   * @return { Top alignFrame (Usually CDS), Bottom AlignFrame (Usually
   *         Protein)}
   */
  public List<AlignFrame> getAlignFrames()
  {
    return Arrays
            .asList(new AlignFrame[]
            { (AlignFrame) getTopFrame(), (AlignFrame) getBottomFrame() });
  }

  @Override
  public AlignFrame getComplementAlignFrame(
          AlignViewControllerGuiI alignFrame)
  {
    if (getTopFrame() == alignFrame)
    {
      return (AlignFrame) getBottomFrame();
    }
    if (getBottomFrame() == alignFrame)
    {
      return (AlignFrame) getTopFrame();
    }
    // we didn't know anything about this frame...
    return null;
  }

  /**
   * Replace Cmd-F Find action with our version. This is necessary because the
   * 'default' Finder searches in the first AlignFrame it finds. We need it to
   * search in the half of the SplitFrame that has the mouse.
   */
  protected void overrideFind()
  {
    /*
     * Ctrl-F / Cmd-F open Finder dialog, 'focused' on the right alignment
     */
    KeyStroke key_cmdF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
            jalview.util.ShortcutKeyMaskExWrapper
                    .getMenuShortcutKeyMaskEx(),
            false);
    AbstractAction action = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        Component c = getFrameAtMouse();
        if (c != null && c instanceof AlignFrame)
        {
          AlignFrame af = (AlignFrame) c;
          boolean dna = af.getViewport().getAlignment().isNucleotide();
          String scope = MessageManager.getString("label.in") + " "
                  + (dna ? MessageManager.getString("label.nucleotide")
                          : MessageManager.getString("label.protein"));
          new Finder(af.alignPanel, true, scope);
        }
      }
    };
    overrideKeyBinding(key_cmdF, action);
  }

  /**
   * Override to do nothing if triggered from one of the child frames
   */
  @Override
  public void setSelected(boolean selected) throws PropertyVetoException
  {
    JDesktopPane desktopPane = getDesktopPane();
    JInternalFrame fr = desktopPane == null ? null
            : desktopPane.getSelectedFrame();
    if (fr == getTopFrame() || fr == getBottomFrame())
    {
      /* 
       * patch for JAL-3288 (deselecting top/bottom frame closes popup menu); 
       * it may be possible to remove this method in future
       * if the underlying Java behaviour changes
       */
      if (selected)
      {
        moveToFront();
      }
      return;
    }
    super.setSelected(selected);
  }

  /**
   * holds the frame for feature settings, so Protein and DNA tabs can be
   * managed
   */
  JInternalFrame featureSettingsUI;

  JTabbedPane featureSettingsPanels;

  @Override
  public void addFeatureSettingsUI(
          FeatureSettingsControllerGuiI featureSettings)
  {
    boolean showInternalFrame = false;
    if (featureSettingsUI == null || featureSettingsPanels == null)
    {
      showInternalFrame = true;
      featureSettingsPanels = new JTabbedPane();
      featureSettingsPanels.addChangeListener(new ChangeListener()
      {

        @Override
        public void stateChanged(ChangeEvent e)
        {
          if (e.getSource() != featureSettingsPanels
                  || featureSettingsUI == null
                  || featureSettingsUI.isClosed()
                  || !featureSettingsUI.isVisible())
          {
            // not our tabbed pane
            return;
          }
          int tab = featureSettingsPanels.getSelectedIndex();
          if (tab < 0 || featureSettingsPanels
                  .getSelectedComponent() instanceof FeatureSettingsControllerGuiI)
          {
            // no tab selected or already showing a feature settings GUI
            return;
          }
          getAlignFrames().get(tab).showFeatureSettingsUI();
        }
      });
      featureSettingsUI = new JInternalFrame(MessageManager.getString(
              "label.sequence_feature_settings_for_CDS_and_Protein"));
      featureSettingsPanels.setOpaque(true);

      JPanel dialog = new JPanel();
      dialog.setOpaque(true);
      dialog.setLayout(new BorderLayout());
      dialog.add(featureSettingsPanels, BorderLayout.CENTER);
      JPanel buttons = new JPanel();
      JButton ok = new JButton(MessageManager.getString("action.ok"));
      ok.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          try
          {
            featureSettingsUI.setClosed(true);
          } catch (PropertyVetoException pv)
          {
            pv.printStackTrace();
          }
        }
      });
      JButton cancel = new JButton(
              MessageManager.getString("action.cancel"));
      cancel.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          try
          {
            for (Component fspanel : featureSettingsPanels.getComponents())
            {
              if (fspanel instanceof FeatureSettingsControllerGuiI)
              {
                ((FeatureSettingsControllerGuiI) fspanel).revert();
              }
            }
            featureSettingsUI.setClosed(true);
          } catch (Exception pv)
          {
            pv.printStackTrace();
          }
        }
      });
      buttons.add(ok);
      buttons.add(cancel);
      dialog.add(buttons, BorderLayout.SOUTH);
      featureSettingsUI.setContentPane(dialog);
      createDummyTabs();
    }
    if (featureSettingsPanels
            .indexOfTabComponent((Component) featureSettings) > -1)
    {
      // just show the feature settings !
      featureSettingsPanels
              .setSelectedComponent((Component) featureSettings);
      return;
    }
    // otherwise replace the dummy tab with the given feature settings
    int pos = getAlignFrames().indexOf(featureSettings.getAlignframe());
    // if pos==-1 then alignFrame isn't managed by this splitframe
    if (pos == 0)
    {
      featureSettingsPanels.removeTabAt(0);
      featureSettingsPanels.insertTab(tabName[0], null,
              (Component) featureSettings,
              MessageManager.formatMessage(
                      "label.sequence_feature_settings_for", tabName[0]),
              0);
    }
    if (pos == 1)
    {
      featureSettingsPanels.removeTabAt(1);
      featureSettingsPanels.insertTab(tabName[1], null,
              (Component) featureSettings,
              MessageManager.formatMessage(
                      "label.sequence_feature_settings_for", tabName[1]),
              1);
    }
    featureSettingsPanels.setSelectedComponent((Component) featureSettings);

    // TODO: JAL-3535 - construct a feature settings title including names of
    // currently selected CDS and Protein names

    if (showInternalFrame)
    {
      if (Platform.isAMacAndNotJS())
      {
        Desktop.addInternalFrame(featureSettingsUI,
                MessageManager.getString(
                        "label.sequence_feature_settings_for_CDS_and_Protein"),
                600, 480);
      }
      else
      {
        Desktop.addInternalFrame(featureSettingsUI,
                MessageManager.getString(
                        "label.sequence_feature_settings_for_CDS_and_Protein"),
                600, 450);
      }
      featureSettingsUI
              .setMinimumSize(new Dimension(FS_MIN_WIDTH, FS_MIN_HEIGHT));

      featureSettingsUI.addInternalFrameListener(
              new javax.swing.event.InternalFrameAdapter()
              {
                @Override
                public void internalFrameClosed(
                        javax.swing.event.InternalFrameEvent evt)
                {
                  for (int tab = 0; tab < featureSettingsPanels
                          .getTabCount();)
                  {
                    FeatureSettingsControllerGuiI fsettings = (FeatureSettingsControllerGuiI) featureSettingsPanels
                            .getTabComponentAt(tab);
                    if (fsettings != null)
                    {
                      featureSettingsPanels.removeTabAt(tab);
                      fsettings.featureSettings_isClosed();
                    }
                    else
                    {
                      tab++;
                    }
                  }
                  featureSettingsPanels = null;
                  featureSettingsUI = null;
                };
              });
      featureSettingsUI.setLayer(JLayeredPane.PALETTE_LAYER);
    }
  }

  /**
   * tab names for feature settings
   */
  private String[] tabName = new String[] {
      MessageManager.getString("label.CDS"),
      MessageManager.getString("label.protein") };

  /**
   * create placeholder tabs which materialise the feature settings for a given
   * view. Also reinitialises any tabs containing stale feature settings
   */
  private void createDummyTabs()
  {
    for (int tabIndex = 0; tabIndex < 2; tabIndex++)
    {
      JPanel dummyTab = new JPanel();
      featureSettingsPanels.addTab(tabName[tabIndex], dummyTab);
    }
  }

  private void replaceWithDummyTab(FeatureSettingsControllerI toClose)
  {
    Component dummyTab = null;
    for (int tabIndex = 0; tabIndex < 2; tabIndex++)
    {
      if (featureSettingsPanels.getTabCount() > tabIndex)
      {
        dummyTab = featureSettingsPanels.getTabComponentAt(tabIndex);
        if (dummyTab instanceof FeatureSettingsControllerGuiI
                && !dummyTab.isVisible())
        {
          featureSettingsPanels.removeTabAt(tabIndex);
          // close the feature Settings tab
          ((FeatureSettingsControllerGuiI) dummyTab)
                  .featureSettings_isClosed();
          // create a dummy tab in its place
          dummyTab = new JPanel();
          featureSettingsPanels.insertTab(tabName[tabIndex], null, dummyTab,
                  MessageManager.formatMessage(
                          "label.sequence_feature_settings_for",
                          tabName[tabIndex]),
                  tabIndex);
        }
      }
    }
  }

  @Override
  public void closeFeatureSettings(
          FeatureSettingsControllerI featureSettings,
          boolean closeContainingFrame)
  {
    if (featureSettingsUI != null)
    {
      if (closeContainingFrame)
      {
        try
        {
          featureSettingsUI.setClosed(true);
        } catch (Exception x)
        {
        }
        featureSettingsUI = null;
      }
      else
      {
        replaceWithDummyTab(featureSettings);
      }
    }
  }

  @Override
  public boolean isFeatureSettingsOpen()
  {
    return featureSettingsUI != null && !featureSettingsUI.isClosed();
  }
}