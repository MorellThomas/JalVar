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

package jalview.jbgui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.TableColumn;

import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.core.FTSDataColumnPreferences;
import jalview.gui.AlignmentPanel;
import jalview.gui.Desktop;
import jalview.gui.JvSwingUtils;
import jalview.gui.StructureViewer;
import jalview.structure.StructureImportSettings;
import jalview.structure.StructureImportSettings.TFType;
import jalview.util.MessageManager;
import jalview.util.Platform;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
/**
 * GUI layout for structure chooser
 * 
 * @author tcnofoegbu
 *
 */
public abstract class GStructureChooser extends JPanel
        implements ItemListener
{
  private static final Font VERDANA_12 = new Font("Verdana", 0, 12);

  private static final Font VERDANA_10 = new Font("Verdana", 0, 10);

  public static final String VIEWS_FILTER = "VIEWS_FILTER";

  protected static final String VIEWS_FROM_FILE = "VIEWS_FROM_FILE";

  protected static final String VIEWS_ENTER_ID = "VIEWS_ENTER_ID";

  /*
   * 'cached' structure view
   */
  protected static final String VIEWS_LOCAL_PDB = "VIEWS_LOCAL_PDB";

  protected JPanel actionsPanel;

  protected JPanel statusPanel = new JPanel();

  public JLabel statusBar = new JLabel();

  protected String frameTitle = MessageManager
          .getString("label.structure_chooser");

  protected JInternalFrame mainFrame = new JInternalFrame(frameTitle);

  protected JComboBox<FilterOption> cmb_filterOption = new JComboBox<>();

  protected AlignmentPanel ap;

  protected StringBuilder errorWarning = new StringBuilder();

  protected JButton btn_cancel;

  protected JButton btn_add;

  protected JButton btn_newView;

  protected JButton btn_pdbFromFile = new JButton();

  protected JLabel lbl_pdbFile = new JLabel();

  protected JLabel lbl_chooseTempFacType = new JLabel();

  protected JComboBox<StructureImportSettings.TFType> combo_tempFacAs = new JComboBox<>();

  protected boolean tempFacAsChanged = false;

  protected JButton btn_paeMatrixFile = new JButton();

  protected JLabel lbl_paeFile = new JLabel();

  // holder for icon and button
  protected JPanel pnl_queryTDB;

  protected JButton btn_queryTDB = new JButton();

  protected JCheckBox chk_superpose = new JCheckBox(
          MessageManager.getString("label.superpose_structures"));

  protected JTextField txt_search = new JTextField(14);

  protected JPanel pnl_switchableViews = new JPanel(new CardLayout());

  protected CardLayout layout_switchableViews = (CardLayout) (pnl_switchableViews
          .getLayout());

  protected JCheckBox chk_invertFilter = new JCheckBox(
          MessageManager.getString("label.invert"));

  protected ImageIcon loadingImage = new ImageIcon(
          getClass().getResource("/images/loading.gif"));

  protected ImageIcon goodImage = new ImageIcon(
          getClass().getResource("/images/good.png"));

  protected ImageIcon errorImage = new ImageIcon(
          getClass().getResource("/images/error.png"));

  protected ImageIcon warningImage = new ImageIcon(
          getClass().getResource("/images/warning.gif"));

  protected ImageIcon tdbImage = new ImageIcon(getClass()
          .getResource("/images/3d-beacons-logo-transparent.png"));

  protected JLabel lbl_loading = new JLabel(loadingImage);

  protected JLabel lbl_pdbManualFetchStatus = new JLabel(errorImage);

  // protected JLabel lbl_fromFileStatus = new JLabel(errorImage);

  protected AssociateSeqPanel idInputAssSeqPanel = new AssociateSeqPanel();

  protected AssociateSeqPanel fileChooserAssSeqPanel = new AssociateSeqPanel();

  protected JComboBox<StructureViewer> targetView = new JComboBox<>();

  protected JTable tbl_local_pdb = new JTable();

  protected JTabbedPane pnl_filter = new JTabbedPane();

  protected abstract FTSDataColumnPreferences getFTSDocFieldPrefs();

  protected abstract void setFTSDocFieldPrefs(
          FTSDataColumnPreferences newPrefs);

  protected FTSDataColumnI[] previousWantedFields;

  protected static Map<String, Integer> tempUserPrefs = new HashMap<>();

  private JTable tbl_summary = new JTable()
  {
    private boolean inLayout;

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
      return hasExcessWidth();

    }

    @Override
    public void doLayout()
    {
      if (hasExcessWidth())
      {
        autoResizeMode = AUTO_RESIZE_SUBSEQUENT_COLUMNS;
      }
      inLayout = true;
      super.doLayout();
      inLayout = false;
      autoResizeMode = AUTO_RESIZE_OFF;
    }

    protected boolean hasExcessWidth()
    {
      return getPreferredSize().width < getParent().getWidth();
    }

    @Override
    public void columnMarginChanged(ChangeEvent e)
    {
      if (isEditing())
      {
        removeEditor();
      }
      TableColumn resizingColumn = getTableHeader().getResizingColumn();
      // Need to do this here, before the parent's
      // layout manager calls getPreferredSize().
      if (resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF
              && !inLayout)
      {
        resizingColumn.setPreferredWidth(resizingColumn.getWidth());
        String colHeader = resizingColumn.getHeaderValue().toString();
        tempUserPrefs.put(colHeader, resizingColumn.getWidth());
      }
      resizeAndRepaint();
    }

    @Override
    public String getToolTipText(MouseEvent evt)
    {
      String toolTipText = null;
      java.awt.Point pnt = evt.getPoint();
      int rowIndex = rowAtPoint(pnt);
      int colIndex = columnAtPoint(pnt);

      try
      {
        if (getValueAt(rowIndex, colIndex) == null)
        {
          return null;
        }
        toolTipText = getValueAt(rowIndex, colIndex).toString();
      } catch (Exception e)
      {
        // e.printStackTrace();
      }
      toolTipText = (toolTipText == null ? null
              : (toolTipText.length() > 500
                      ? JvSwingUtils.wrapTooltip(true,
                              "\"" + toolTipText.subSequence(0, 500)
                                      + "...\"")
                      : JvSwingUtils.wrapTooltip(true, toolTipText)));
      return toolTipText;
    }
  };

  public GStructureChooser()
  {
  }

  protected void initDialog()
  {

    try
    {
      jbInit();
      mainFrame.setFrameIcon(null);
      mainFrame.setVisible(false);
      mainFrame.invalidate();
      mainFrame.pack();
    } catch (Exception e)
    {
      System.out.println(e); // for JavaScript TypeError
      e.printStackTrace();
    }
  }

  // BH SwingJS optimization
  // (a) 100-ms interruptable timer for text entry -- BH 1/10/2019
  // (b) two-character minimum, at least for JavaScript.

  private Timer timer;

  protected void txt_search_ActionPerformedDelayed()
  {
    if (timer != null)
    {
      timer.stop();
    }
    timer = new Timer(300, new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        txt_search_ActionPerformed();
      }
    });
    timer.setRepeats(false);
    timer.start();
  }
  //

  /**
   * Initializes the GUI default properties
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    Integer width = tempUserPrefs.get("structureChooser.width") == null
            ? 800
            : tempUserPrefs.get("structureChooser.width");
    Integer height = tempUserPrefs.get("structureChooser.height") == null
            ? 400
            : tempUserPrefs.get("structureChooser.height");
    tbl_summary.setAutoCreateRowSorter(true);
    tbl_summary.getTableHeader().setReorderingAllowed(false);
    tbl_summary.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (!popupAction(e))
        {
          super.mousePressed(e);
        }
      }

      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (!popupAction(e))
        {
          validateSelections();
        }
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        if (!popupAction(e))
        {
          validateSelections();
        }
      }

      boolean popupAction(MouseEvent e)
      {
        if (e.isPopupTrigger())
        {
          Point pt = e.getPoint();
          int selectedRow = tbl_summary.rowAtPoint(pt);
          if (showPopupFor(selectedRow, pt.x, pt.y))
          {
            return true;
          }
        }
        return false;
      }
    });
    tbl_summary.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        validateSelections();
        switch (evt.getKeyCode())
        {
        case KeyEvent.VK_ESCAPE: // escape key
          mainFrame.dispose();
          break;
        case KeyEvent.VK_ENTER: // enter key
          if (btn_add.isEnabled())
          {
            add_ActionPerformed();
          }
          break;
        case KeyEvent.VK_TAB: // tab key
          if (evt.isShiftDown())
          {
            pnl_filter.requestFocus();
          }
          else
          {
            btn_add.requestFocus();
          }
          evt.consume();
          break;
        default:
          return;
        }
      }
    });

    btn_cancel = new JButton(MessageManager.getString("action.cancel"));
    btn_cancel.setFont(VERDANA_12);
    btn_cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeAction(pnl_filter.getHeight());
      }
    });
    btn_cancel.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          closeAction(pnl_filter.getHeight());
        }
      }
    });

    tbl_local_pdb.setAutoCreateRowSorter(true);
    tbl_local_pdb.getTableHeader().setReorderingAllowed(false);
    tbl_local_pdb.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        validateSelections();
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        validateSelections();
      }
    });
    tbl_local_pdb.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        validateSelections();
        switch (evt.getKeyCode())
        {
        case KeyEvent.VK_ESCAPE: // escape key
          mainFrame.dispose();
          break;
        case KeyEvent.VK_ENTER: // enter key
          if (btn_add.isEnabled())
          {
            add_ActionPerformed();
          }
          break;
        case KeyEvent.VK_TAB: // tab key
          if (evt.isShiftDown())
          {
            cmb_filterOption.requestFocus();
          }
          else
          {
            if (btn_add.isEnabled())
            {
              btn_add.requestFocus();
            }
            else
            {
              btn_cancel.requestFocus();
            }
          }
          evt.consume();
          break;
        default:
          return;
        }
      }
    });

    btn_newView = new JButton(
            MessageManager.formatMessage("action.new_structure_view_with",
                    StructureViewer.getViewerType().toString()));
    btn_newView.setFont(VERDANA_12);
    btn_newView.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        newView_ActionPerformed();
      }
    });
    btn_newView.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          newView_ActionPerformed();
        }
      }
    });

    // TODO: JAL-3898 - get list of available external programs to view
    // structures with

    btn_add = new JButton(MessageManager.getString("action.add"));
    btn_add.setFont(VERDANA_12);
    btn_add.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        add_ActionPerformed();
      }
    });
    btn_add.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          add_ActionPerformed();
        }
      }
    });

    btn_pdbFromFile.setFont(VERDANA_12);
    String btn_title = MessageManager.getString("label.select_pdb_file");
    btn_pdbFromFile.setText(btn_title + "              ");
    btn_pdbFromFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        pdbFromFile_actionPerformed();
      }
    });
    btn_pdbFromFile.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          pdbFromFile_actionPerformed();
        }
      }
    });
    lbl_pdbFile.setFont(VERDANA_10);
    lbl_pdbFile.setFont(VERDANA_10);

    lbl_chooseTempFacType.setFont(VERDANA_12);
    lbl_chooseTempFacType.setText(
            MessageManager.getString("label.interpret_tempfac_as"));

    combo_tempFacAs.setFont(VERDANA_12);
    for (TFType t : TFType.values())
    {
      combo_tempFacAs.addItem(t);
    }
    combo_tempFacAs.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        // used to determine if we should guess pLDDT or leave a user-choice
        tempFacAsChanged = true;
      }
    });

    btn_paeMatrixFile.setFont(VERDANA_12);
    btn_paeMatrixFile
            .setText(MessageManager.getString("label.add_pae_matrix_file"));
    btn_paeMatrixFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        paeMatrixFile_actionPerformed();
      }
    });
    btn_paeMatrixFile.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          paeMatrixFile_actionPerformed();
        }
      }
    });
    lbl_paeFile.setFont(VERDANA_10);

    JScrollPane scrl_foundStructures = new JScrollPane(tbl_summary);
    scrl_foundStructures.setPreferredSize(new Dimension(width, height));

    JScrollPane scrl_localPDB = new JScrollPane(tbl_local_pdb);
    scrl_localPDB.setPreferredSize(new Dimension(width, height));
    scrl_localPDB.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    chk_invertFilter.setFont(VERDANA_12);
    txt_search.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.enter_pdb_id_tip")));
    txt_search.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformedDelayed();
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformedDelayed();
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformedDelayed();
      }
    });

    cmb_filterOption.setFont(VERDANA_12);
    cmb_filterOption.setToolTipText(
            MessageManager.getString("info.select_filter_option"));
    cmb_filterOption.addItemListener(this);
    // add CustomComboSeparatorsRenderer to filter option combo-box
    cmb_filterOption.setRenderer(new CustomComboSeparatorsRenderer(
            (ListCellRenderer<Object>) cmb_filterOption.getRenderer())
    {
      @Override
      protected boolean addSeparatorAfter(JList list, FilterOption value,
              int index)
      {
        return value.isAddSeparatorAfter();
      }
    });

    chk_invertFilter.addItemListener(this);
    btn_queryTDB = new JButton();
    if (Platform.isMac())
    {
      // needed to make icon button have round corners in vaqua
      btn_queryTDB.putClientProperty("JButton.buttonType", "bevel");
    }
    btn_queryTDB.setMargin(new Insets(0, 16, 0, 20));
    btn_queryTDB
            .setText(MessageManager.getString("label.search_3dbeacons"));
    btn_queryTDB.setIconTextGap(12);
    btn_queryTDB.setIcon(tdbImage);
    btn_queryTDB.setVerticalTextPosition(SwingConstants.CENTER);
    btn_queryTDB.setHorizontalTextPosition(SwingConstants.TRAILING);
    btn_queryTDB.setFont(VERDANA_12);
    btn_queryTDB.setToolTipText(
            MessageManager.getString("label.find_models_from_3dbeacons"));
    // btn_queryTDB.setPreferredSize(new Dimension(200, 32));
    btn_queryTDB.setVisible(false);

    targetView.setVisible(false);

    actionsPanel = new JPanel(new MigLayout());
    actionsPanel.add(targetView, "left");
    actionsPanel.add(btn_add, "wrap");
    actionsPanel.add(chk_superpose, "left");
    actionsPanel.add(btn_newView);
    actionsPanel.add(btn_cancel, "right");

    JPanel pnl_main = new JPanel(new BorderLayout());
    JPanel pnl_controls = new JPanel();
    pnl_queryTDB = new JPanel();
    pnl_queryTDB.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
    pnl_queryTDB.setBackground(getBackground());
    pnl_queryTDB.add(btn_queryTDB);

    pnl_queryTDB.setVisible(false);
    pnl_main.add(pnl_queryTDB, BorderLayout.NORTH);
    pnl_controls.add(cmb_filterOption);
    pnl_controls.add(lbl_loading);
    pnl_controls.add(chk_invertFilter);
    pnl_main.add(pnl_controls, BorderLayout.CENTER);
    lbl_loading.setVisible(false);

    // JPanel pnl_fileChooser = new JPanel(new FlowLayout());
    // pnl_fileChooser.add(btn_pdbFromFile);
    // pnl_fileChooser.add(lbl_fromFileStatus);

    JPanel pnl_fileOptions = new JPanel();
    pnl_fileOptions.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.insets = new Insets(0, 0, 2, 0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    // pnl_fileOptions.add(pnl_fileChooser, gbc);
    pnl_fileOptions.add(btn_pdbFromFile, gbc);
    gbc.gridy++;
    gbc.insets = new Insets(0, 0, 18, 0);
    pnl_fileOptions.add(lbl_pdbFile, gbc);
    gbc.gridy++;

    gbc.insets = new Insets(0, 0, 2, 0);
    pnl_fileOptions.add(new JLabel(
            MessageManager.getString("label.structure_import_options")),
            gbc);
    gbc.gridy++;

    gbc.insets = new Insets(0, 0, 6, 0);
    pnl_fileOptions.add(lbl_chooseTempFacType, gbc);
    gbc.gridy++;
    gbc.insets = new Insets(0, 0, 18, 0);
    pnl_fileOptions.add(combo_tempFacAs, gbc);
    gbc.gridy++;
    gbc.insets = new Insets(0, 0, 6, 0);
    pnl_fileOptions.add(btn_paeMatrixFile, gbc);
    gbc.gridy++;
    gbc.insets = new Insets(0, 0, 2, 0);
    gbc.weighty = 1.0;
    pnl_fileOptions.add(lbl_paeFile, gbc);

    JPanel pnl_fileChooserBL = new JPanel(new BorderLayout());
    pnl_fileChooserBL.add(fileChooserAssSeqPanel, BorderLayout.PAGE_START);
    pnl_fileChooserBL.add(pnl_fileOptions, BorderLayout.CENTER);

    JPanel pnl_idInput = new JPanel(new FlowLayout());
    pnl_idInput.add(txt_search);
    pnl_idInput.add(lbl_pdbManualFetchStatus);

    JPanel pnl_idInputBL = new JPanel(new BorderLayout());
    pnl_idInputBL.add(idInputAssSeqPanel, BorderLayout.NORTH);
    pnl_idInputBL.add(pnl_idInput, BorderLayout.CENTER);

    final String foundStructureSummary = MessageManager
            .getString("label.found_structures_summary");
    final String configureCols = MessageManager
            .getString("label.configure_displayed_columns");
    ChangeListener changeListener = new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent changeEvent)
      {
        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
                .getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        btn_add.setVisible(targetView.isVisible());
        btn_newView.setVisible(true);
        btn_cancel.setVisible(true);
        if (sourceTabbedPane.getTitleAt(index).equals(configureCols))
        {
          btn_add.setEnabled(false);
          btn_cancel.setEnabled(false);
          btn_add.setVisible(false);
          btn_newView.setEnabled(false);
          btn_cancel.setVisible(false);
          previousWantedFields = getFTSDocFieldPrefs()
                  .getStructureSummaryFields()
                  .toArray(new FTSDataColumnI[0]);
        }
        if (sourceTabbedPane.getTitleAt(index)
                .equals(foundStructureSummary))
        {
          btn_cancel.setEnabled(true);
          if (wantedFieldsUpdated())
          {
            tabRefresh();
          }
          else
          {
            validateSelections();
          }
        }
      }
    };
    pnl_filter.addChangeListener(changeListener);
    pnl_filter.setPreferredSize(new Dimension(width, height));
    pnl_filter.add(foundStructureSummary, scrl_foundStructures);
    pnl_filter.add(configureCols, getFTSDocFieldPrefs());

    JPanel pnl_locPDB = new JPanel(new BorderLayout());
    pnl_locPDB.add(scrl_localPDB);

    pnl_switchableViews.add(pnl_fileChooserBL, VIEWS_FROM_FILE);
    pnl_switchableViews.add(pnl_idInputBL, VIEWS_ENTER_ID);
    pnl_switchableViews.add(pnl_filter, VIEWS_FILTER);
    pnl_switchableViews.add(pnl_locPDB, VIEWS_LOCAL_PDB);

    this.setLayout(new BorderLayout());
    this.add(pnl_main, java.awt.BorderLayout.NORTH);
    this.add(pnl_switchableViews, java.awt.BorderLayout.CENTER);
    // this.add(pnl_actions, java.awt.BorderLayout.SOUTH);
    statusPanel.setLayout(new GridLayout());

    JPanel pnl_actionsAndStatus = new JPanel(new BorderLayout());
    pnl_actionsAndStatus.add(actionsPanel, BorderLayout.CENTER);
    pnl_actionsAndStatus.add(statusPanel, BorderLayout.SOUTH);
    statusPanel.add(statusBar, null);
    this.add(pnl_actionsAndStatus, java.awt.BorderLayout.SOUTH);

    mainFrame.addInternalFrameListener(
            new javax.swing.event.InternalFrameAdapter()
            {
              @Override
              public void internalFrameClosing(InternalFrameEvent e)
              {
                closeAction(pnl_filter.getHeight());
              }
            });
    mainFrame.setVisible(true);
    mainFrame.setContentPane(this);
    mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Integer x = tempUserPrefs.get("structureChooser.x");
    Integer y = tempUserPrefs.get("structureChooser.y");
    if (x != null && y != null)
    {
      mainFrame.setLocation(x, y);
    }
    Desktop.addInternalFrame(mainFrame, frameTitle, width, height);
  }

  protected abstract boolean showPopupFor(int selectedRow, int x, int y);

  protected void closeAction(int preferredHeight)
  {
    // System.out.println(">>>>>>>>>> closing internal frame!!!");
    // System.out.println("width : " + mainFrame.getWidth());
    // System.out.println("heigh : " + mainFrame.getHeight());
    // System.out.println("x : " + mainFrame.getX());
    // System.out.println("y : " + mainFrame.getY());
    tempUserPrefs.put("structureChooser.width", pnl_filter.getWidth());
    tempUserPrefs.put("structureChooser.height", preferredHeight);
    tempUserPrefs.put("structureChooser.x", mainFrame.getX());
    tempUserPrefs.put("structureChooser.y", mainFrame.getY());
    mainFrame.dispose();
  }

  public boolean wantedFieldsUpdated()
  {
    if (previousWantedFields == null)
    {
      return true;
    }

    FTSDataColumnI[] currentWantedFields = getFTSDocFieldPrefs()
            .getStructureSummaryFields().toArray(new FTSDataColumnI[0]);
    return Arrays.equals(currentWantedFields, previousWantedFields) ? false
            : true;

  }

  @Override
  /**
   * Event listener for the 'filter' combo-box and 'invert' check-box
   */
  public void itemStateChanged(ItemEvent e)
  {
    stateChanged(e);
  }

  /**
   * This inner class provides the provides the data model for associate
   * sequence combo-box - cmb_assSeq
   * 
   * @author tcnofoegbu
   *
   */
  public class AssociateSeqOptions
  {
    private SequenceI sequence;

    private String name;

    public AssociateSeqOptions(SequenceI seq)
    {
      this.sequence = seq;
      this.name = (seq.getName().length() >= 23)
              ? seq.getName().substring(0, 23)
              : seq.getName();
    }

    public AssociateSeqOptions(String name, SequenceI seq)
    {
      this.name = name;
      this.sequence = seq;
    }

    @Override
    public String toString()
    {
      return name;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public SequenceI getSequence()
    {
      return sequence;
    }

    public void setSequence(SequenceI sequence)
    {
      this.sequence = sequence;
    }

  }

  /**
   * This inner class holds the Layout and configuration of the panel which
   * handles association of manually fetched structures to a unique sequence
   * when more than one sequence selection is made
   * 
   * @author tcnofoegbu
   *
   */
  public class AssociateSeqPanel extends JPanel implements ItemListener
  {
    private JComboBox<AssociateSeqOptions> cmb_assSeq = new JComboBox<>();

    private JLabel lbl_associateSeq = new JLabel();

    public AssociateSeqPanel()
    {
      this.setLayout(new FlowLayout());
      this.add(cmb_assSeq);
      this.add(lbl_associateSeq);
      cmb_assSeq.setToolTipText(
              MessageManager.getString("info.associate_wit_sequence"));
      cmb_assSeq.addItemListener(this);
    }

    public void loadCmbAssSeq()
    {
      populateCmbAssociateSeqOptions(cmb_assSeq, lbl_associateSeq);
    }

    public JComboBox<AssociateSeqOptions> getCmb_assSeq()
    {
      return cmb_assSeq;
    }

    public void setCmb_assSeq(JComboBox<AssociateSeqOptions> cmb_assSeq)
    {
      this.cmb_assSeq = cmb_assSeq;
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
        cmbAssSeqStateChanged();
      }
    }
  }

  public JTable getResultTable()
  {
    return tbl_summary;
  }

  public JComboBox<FilterOption> getCmbFilterOption()
  {
    return cmb_filterOption;
  }

  /**
   * Custom ListCellRenderer for adding a separator between different categories
   * of structure chooser filter option drop-down.
   * 
   * @author tcnofoegbu
   *
   */
  public abstract class CustomComboSeparatorsRenderer
          implements ListCellRenderer<Object>
  {
    private ListCellRenderer<Object> regent;

    private JPanel separatorPanel = new JPanel(new BorderLayout());

    private JSeparator jSeparator = new JSeparator();

    public CustomComboSeparatorsRenderer(
            ListCellRenderer<Object> listCellRenderer)
    {
      this.regent = listCellRenderer;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
    {

      Component comp = regent.getListCellRendererComponent(list, value,
              index, isSelected, cellHasFocus);
      if (index != -1
              && addSeparatorAfter(list, (FilterOption) value, index))
      {
        separatorPanel.removeAll();
        separatorPanel.add(comp, BorderLayout.CENTER);
        separatorPanel.add(jSeparator, BorderLayout.SOUTH);
        return separatorPanel;
      }
      else
      {
        return comp;
      }
    }

    protected abstract boolean addSeparatorAfter(JList list,
            FilterOption value, int index);
  }

  protected abstract void stateChanged(ItemEvent e);

  protected abstract void add_ActionPerformed();

  protected abstract void newView_ActionPerformed();

  protected abstract void pdbFromFile_actionPerformed();

  protected abstract void paeMatrixFile_actionPerformed();

  protected abstract void txt_search_ActionPerformed();

  protected abstract void populateCmbAssociateSeqOptions(
          JComboBox<AssociateSeqOptions> cmb_assSeq,
          JLabel lbl_associateSeq);

  protected abstract void cmbAssSeqStateChanged();

  protected abstract void tabRefresh();

  protected abstract void validateSelections();

  public JInternalFrame getFrame()
  {
    return mainFrame;
  }

  public void setPdbOptionsEnabled(boolean b)
  {
    lbl_chooseTempFacType.setEnabled(b);
    combo_tempFacAs.setEnabled(b);
    btn_paeMatrixFile.setEnabled(b);
    lbl_paeFile.setEnabled(b);
  }
}
