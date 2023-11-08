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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.bin.MemorySetting;
import jalview.fts.core.FTSDataColumnPreferences;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.fts.service.pdb.PDBFTSRestClient;
import jalview.gui.Desktop;
import jalview.gui.JalviewBooleanRadioButtons;
import jalview.gui.JvOptionPane;
import jalview.gui.JvSwingUtils;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.BackupFilenameParts;
import jalview.io.BackupFiles;
import jalview.io.BackupFilesPresetEntry;
import jalview.io.IntKeyStringValueEntry;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.StringUtils;

/**
 * Base class for the Preferences panel.
 * 
 * @author $author$
 * @version $Revision$
 */
public class GPreferences extends JPanel
{
  private static final Font LABEL_FONT = JvSwingUtils.getLabelFont();

  private static final Font LABEL_FONT_ITALIC = JvSwingUtils
          .getLabelFont(false, true);

  private static final Font LABEL_FONT_BOLD = JvSwingUtils
          .getLabelFont(true, false);

  /*
   * Visual tab components
   */
  protected JCheckBox fullScreen = new JCheckBox();

  protected JCheckBox openoverv = new JCheckBox();

  protected JCheckBox seqLimit = new JCheckBox();

  protected JCheckBox rightAlign = new JCheckBox();

  protected JComboBox<String> fontSizeCB = new JComboBox<>();

  protected JComboBox<String> fontStyleCB = new JComboBox<>();

  protected JComboBox<String> fontNameCB = new JComboBox<>();

  protected JCheckBox showOccupancy = new JCheckBox();

  protected JCheckBox showUnconserved = new JCheckBox();

  protected JCheckBox idItalics = new JCheckBox();

  protected JCheckBox smoothFont = new JCheckBox();

  protected JCheckBox scaleProteinToCdna = new JCheckBox();

  protected JComboBox<String> gapSymbolCB = new JComboBox<>();

  protected JCheckBox wrap = new JCheckBox();

  protected JComboBox<String> sortby = new JComboBox<>();

  protected JComboBox<String> sortAnnBy = new JComboBox<>();

  protected JComboBox<String> sortAutocalc = new JComboBox<>();

  protected JCheckBox startupCheckbox = new JCheckBox();

  protected JTextField startupFileTextfield = new JTextField();

  // below are in the 'second column'
  protected JCheckBox annotations = new JCheckBox();

  protected JCheckBox quality = new JCheckBox();

  protected JCheckBox conservation = new JCheckBox();

  protected JCheckBox identity = new JCheckBox();

  protected JCheckBox showGroupConsensus = new JCheckBox();

  protected JCheckBox showGroupConservation = new JCheckBox();

  protected JCheckBox showConsensHistogram = new JCheckBox();

  protected JCheckBox showConsensLogo = new JCheckBox();

  protected JCheckBox showDbRefTooltip = new JCheckBox();

  protected JCheckBox showNpTooltip = new JCheckBox();

  /*
   * Structure tab and components
   */
  protected JPanel structureTab;

  protected JCheckBox structFromPdb = new JCheckBox();

  protected JCheckBox addSecondaryStructure = new JCheckBox();

  protected JCheckBox addTempFactor = new JCheckBox();

  protected JComboBox<String> structViewer = new JComboBox<>();

  protected JLabel structureViewerPathLabel;

  protected JTextField structureViewerPath = new JTextField();

  protected ButtonGroup mappingMethod = new ButtonGroup();

  protected JRadioButton siftsMapping = new JRadioButton();

  protected JRadioButton nwMapping = new JRadioButton();

  /*
   * Colours tab components
   */
  protected JPanel minColour = new JPanel();

  protected JPanel maxColour = new JPanel();

  protected JComboBox<String> protColour = new JComboBox<>();

  protected JComboBox<String> nucColour = new JComboBox<>();

  /*
   * Overview tab components
   */
  protected JPanel gapColour = new JPanel();

  protected JPanel hiddenColour = new JPanel();

  protected JCheckBox useLegacyGap;

  protected JCheckBox showHiddenAtStart;

  protected JLabel gapLabel;

  /*
   * Connections tab components
   */
  protected JPanel connectTab;

  protected JTable linkUrlTable = new JTable();

  protected JButton editLink = new JButton();

  protected JButton deleteLink = new JButton();

  protected JTextField filterTB = new JTextField();

  protected JButton doReset = new JButton();

  protected JButton userOnly = new JButton();

  protected JLabel httpLabel = new JLabel();

  protected JLabel httpsLabel = new JLabel();

  protected JLabel portLabel = new JLabel();

  protected JLabel serverLabel = new JLabel();

  protected JLabel portLabel2 = new JLabel();

  protected JLabel serverLabel2 = new JLabel();

  protected JLabel proxyAuthUsernameLabel = new JLabel();

  protected JLabel proxyAuthPasswordLabel = new JLabel();

  protected JLabel passwordNotStoredLabel = new JLabel();

  protected JTextField proxyServerHttpTB = new JTextField();

  protected JTextField proxyPortHttpTB = new JTextField();

  protected JTextField proxyServerHttpsTB = new JTextField();

  protected JTextField proxyPortHttpsTB = new JTextField();

  protected JCheckBox proxyAuth = new JCheckBox();

  protected JTextField proxyAuthUsernameTB = new JTextField();

  protected JPasswordField proxyAuthPasswordPB = new JPasswordField();

  protected ButtonGroup proxyType = new ButtonGroup();

  protected JRadioButton noProxy = new JRadioButton();

  protected JRadioButton systemProxy = new JRadioButton();

  protected JRadioButton customProxy = new JRadioButton();

  protected JButton applyProxyButton = new JButton();

  protected JCheckBox usagestats = new JCheckBox();

  protected JCheckBox questionnaire = new JCheckBox();

  protected JCheckBox versioncheck = new JCheckBox();

  /*
   * Output tab components
   */
  protected JComboBox<Object> epsRendering = new JComboBox<>();

  protected JComboBox<Object> htmlRendering = new JComboBox<>();

  protected JComboBox<Object> svgRendering = new JComboBox<>();

  protected JLabel userIdWidthlabel = new JLabel();

  protected JCheckBox autoIdWidth = new JCheckBox();

  protected JTextField userIdWidth = new JTextField();

  protected JCheckBox blcjv = new JCheckBox();

  protected JCheckBox pileupjv = new JCheckBox();

  protected JCheckBox clustaljv = new JCheckBox();

  protected JCheckBox msfjv = new JCheckBox();

  protected JCheckBox fastajv = new JCheckBox();

  protected JCheckBox pfamjv = new JCheckBox();

  protected JCheckBox pirjv = new JCheckBox();

  protected JCheckBox modellerOutput = new JCheckBox();

  protected JCheckBox embbedBioJSON = new JCheckBox();

  /*
   * Editing tab components
   */
  protected JCheckBox autoCalculateConsCheck = new JCheckBox();

  protected JCheckBox padGaps = new JCheckBox();

  protected JCheckBox sortByTree = new JCheckBox();

  /*
   * Web Services tab
   */
  protected JPanel wsTab = new JPanel();

  /*
   * Backups tab components
   * a lot of these are member variables instead of local variables only so that they
   * can be enabled/disabled easily in one go
   */

  protected JCheckBox enableBackupFiles = new JCheckBox();

  protected JPanel presetsPanel = new JPanel();

  protected JLabel presetsComboLabel = new JLabel();

  protected JCheckBox customiseCheckbox = new JCheckBox();

  protected JButton revertButton = new JButton();

  protected JComboBox<Object> backupfilesPresetsCombo = new JComboBox<>();

  private int backupfilesPresetsComboLastSelected = 0;

  protected JPanel suffixPanel = new JPanel();

  protected JPanel keepfilesPanel = new JPanel();

  protected JPanel exampleFilesPanel = new JPanel();

  protected JTextField suffixTemplate = new JTextField(null, 8);

  protected JLabel suffixTemplateLabel = new JLabel();

  protected JLabel suffixDigitsLabel = new JLabel();

  protected JSpinner suffixDigitsSpinner = new JSpinner();

  protected JalviewBooleanRadioButtons suffixReverse = new JalviewBooleanRadioButtons();

  protected JalviewBooleanRadioButtons backupfilesKeepAll = new JalviewBooleanRadioButtons();

  public JSpinner backupfilesRollMaxSpinner = new JSpinner();

  protected JLabel oldBackupFilesLabel = new JLabel();

  protected JalviewBooleanRadioButtons backupfilesConfirmDelete = new JalviewBooleanRadioButtons();

  protected JTextArea backupfilesExampleLabel = new JTextArea();

  private final JTabbedPane tabbedPane = new JTabbedPane();

  private JLabel messageLabel = new JLabel("", JLabel.CENTER);

  /*
   * Startup tab components
   */

  protected JCheckBox customiseMemorySetting = new JCheckBox();

  protected JLabel exampleMemoryLabel = new JLabel();

  protected JTextArea exampleMemoryMessageTextArea = new JTextArea();

  protected JLabel maxMemoryLabel = new JLabel();

  protected JLabel jvmMemoryPercentLabel = new JLabel();

  protected JSlider jvmMemoryPercentSlider = new JSlider();

  protected JLabel jvmMemoryPercentDisplay = new JLabel();

  protected JLabel jvmMemoryMaxLabel = new JLabel();

  protected JTextField jvmMemoryMaxTextField = new JTextField(null, 8);

  protected JComboBox<Object> lafCombo = new JComboBox<>();

  /**
   * Creates a new GPreferences object.
   */
  public GPreferences()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Construct the panel and its tabbed sub-panels.
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    // final JTabbedPane tabbedPane = new JTabbedPane();
    this.setLayout(new BorderLayout());

    // message label at top
    this.add(messageLabel, BorderLayout.NORTH);

    JPanel okCancelPanel = initOkCancelPanel();
    this.add(tabbedPane, BorderLayout.CENTER);
    this.add(okCancelPanel, BorderLayout.SOUTH);

    tabbedPane.add(initVisualTab(),
            MessageManager.getString("label.visual"));

    tabbedPane.add(initColoursTab(),
            MessageManager.getString("label.colours"));

    tabbedPane.add(initOverviewTab(),
            MessageManager.getString("label.overview"));

    tabbedPane.add(initStructureTab(),
            MessageManager.getString("label.structure"));

    tabbedPane.add(initConnectionsTab(),
            MessageManager.getString("label.connections"));

    if (!Platform.isJS())
    {
      tabbedPane.add(initBackupsTab(),
              MessageManager.getString("label.backups"));
    }

    tabbedPane.add(initLinksTab(),
            MessageManager.getString("label.urllinks"));

    tabbedPane.add(initOutputTab(),
            MessageManager.getString("label.output"));

    tabbedPane.add(initEditingTab(),
            MessageManager.getString("label.editing"));

    tabbedPane.add(initStartupTab(),
            MessageManager.getString("label.startup"));

    /*
     * See WsPreferences for the real work of configuring this tab.
     */
    if (!Platform.isJS())
    {
      wsTab.setLayout(new BorderLayout());
      tabbedPane.add(wsTab, MessageManager.getString("label.web_services"));
    }

    /*
     * Handler to validate a tab before leaving it - currently only for
     * Structure.
     * Adding a clearMessage() so messages are cleared when changing tabs.
     */
    tabbedPane.addChangeListener(new ChangeListener()
    {
      private Component lastTab;

      @Override
      public void stateChanged(ChangeEvent e)
      {
        if (lastTab == structureTab
                && tabbedPane.getSelectedComponent() != structureTab)
        {
          if (!validateStructure())
          {
            tabbedPane.setSelectedComponent(structureTab);
            return;
          }
        }
        lastTab = tabbedPane.getSelectedComponent();

        clearMessage();
      }

    });
  }

  public void setMessage(String message)
  {
    if (message != null)
    {
      messageLabel.setText(message);
      messageLabel.setFont(LABEL_FONT_BOLD);
      messageLabel.setForeground(Color.RED.darker());
      messageLabel.revalidate();
      messageLabel.repaint();
    }
    // note message not cleared if message is null. call clearMessage()
    // directly.
    this.revalidate();
    this.repaint();
  }

  public void clearMessage()
  {
    // only repaint if message exists
    if (messageLabel.getText() != null
            && messageLabel.getText().length() > 0)
    {
      messageLabel.setText("");
      messageLabel.revalidate();
      messageLabel.repaint();
      this.revalidate();
      this.repaint();
    }
  }

  public static enum TabRef
  {
    CONNECTIONS_TAB, STRUCTURE_TAB
  };

  public void selectTab(TabRef selectTab)
  {
    // select a given tab - currently only for Connections
    switch (selectTab)
    {
    case CONNECTIONS_TAB:
      tabbedPane.setSelectedComponent(connectTab);
      break;
    case STRUCTURE_TAB:
      tabbedPane.setSelectedComponent(structureTab);
      break;
    default:
    }
  }

  /**
   * Initialises the Editing tabbed panel.
   * 
   * @return
   */
  private JPanel initEditingTab()
  {
    JPanel editingTab = new JPanel();
    editingTab.setLayout(null);
    autoCalculateConsCheck.setFont(LABEL_FONT);
    autoCalculateConsCheck.setText(
            MessageManager.getString("label.autocalculate_consensus"));
    autoCalculateConsCheck.setBounds(new Rectangle(21, 52, 209, 23));
    padGaps.setFont(LABEL_FONT);
    padGaps.setText(
            MessageManager.getString("label.pad_gaps_when_editing"));
    padGaps.setBounds(new Rectangle(22, 94, 168, 23));
    sortByTree.setFont(LABEL_FONT);
    sortByTree
            .setText(MessageManager.getString("label.sort_with_new_tree"));
    sortByTree.setToolTipText(MessageManager.getString(
            "label.any_trees_calculated_or_loaded_alignment_automatically_sort"));
    sortByTree.setBounds(new Rectangle(22, 136, 168, 23));
    editingTab.add(autoCalculateConsCheck);
    editingTab.add(padGaps);
    editingTab.add(sortByTree);
    return editingTab;
  }

  /**
   * Initialises the Output tab
   * 
   * @return
   */
  private JPanel initOutputTab()
  {
    JPanel outputTab = new JPanel();
    outputTab.setLayout(null);

    JLabel epsLabel = new JLabel(
            MessageManager.formatMessage("label.rendering_style", "EPS"));
    epsLabel.setFont(LABEL_FONT);
    epsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    epsLabel.setBounds(new Rectangle(9, 31, 160, 24));
    epsRendering.setFont(LABEL_FONT);
    epsRendering.setBounds(new Rectangle(174, 34, 187, 21));
    JLabel htmlLabel = new JLabel(
            MessageManager.formatMessage("label.rendering_style", "HTML"));
    htmlLabel.setFont(LABEL_FONT);
    htmlLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    htmlLabel.setBounds(new Rectangle(9, 55, 160, 24));
    htmlRendering.setFont(LABEL_FONT);
    htmlRendering.setBounds(new Rectangle(174, 58, 187, 21));
    JLabel svgLabel = new JLabel(
            MessageManager.formatMessage("label.rendering_style", "SVG"));
    svgLabel.setFont(LABEL_FONT);
    svgLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    svgLabel.setBounds(new Rectangle(9, 79, 160, 24));
    svgRendering.setFont(LABEL_FONT);
    svgRendering.setBounds(new Rectangle(174, 82, 187, 21));

    JLabel jLabel1 = new JLabel();
    jLabel1.setFont(LABEL_FONT);
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText(MessageManager.getString("label.append_start_end"));
    jLabel1.setFont(LABEL_FONT);

    fastajv.setFont(LABEL_FONT);
    fastajv.setHorizontalAlignment(SwingConstants.LEFT);
    clustaljv.setText(MessageManager.getString("label.clustal") + "     ");
    blcjv.setText(MessageManager.getString("label.blc") + "     ");
    fastajv.setText(MessageManager.getString("label.fasta") + "     ");
    msfjv.setText(MessageManager.getString("label.msf") + "     ");
    pfamjv.setText(MessageManager.getString("label.pfam") + "     ");
    pileupjv.setText(MessageManager.getString("label.pileup") + "     ");
    msfjv.setFont(LABEL_FONT);
    msfjv.setHorizontalAlignment(SwingConstants.LEFT);
    pirjv.setText(MessageManager.getString("label.pir") + "     ");
    JPanel jPanel11 = new JPanel();
    jPanel11.setFont(LABEL_FONT);
    TitledBorder titledBorder2 = new TitledBorder(
            MessageManager.getString("label.file_output"));
    jPanel11.setBorder(titledBorder2);
    jPanel11.setBounds(new Rectangle(30, 120, 196, 182));
    GridLayout gridLayout3 = new GridLayout();
    jPanel11.setLayout(gridLayout3);
    gridLayout3.setRows(8);
    blcjv.setFont(LABEL_FONT);
    blcjv.setHorizontalAlignment(SwingConstants.LEFT);
    clustaljv.setFont(LABEL_FONT);
    clustaljv.setHorizontalAlignment(SwingConstants.LEFT);
    pfamjv.setFont(LABEL_FONT);
    pfamjv.setHorizontalAlignment(SwingConstants.LEFT);
    pileupjv.setFont(LABEL_FONT);
    pileupjv.setHorizontalAlignment(SwingConstants.LEFT);
    pirjv.setFont(LABEL_FONT);
    pirjv.setHorizontalAlignment(SwingConstants.LEFT);
    autoIdWidth.setFont(LABEL_FONT);
    autoIdWidth.setText(
            MessageManager.getString("label.automatically_set_id_width"));
    autoIdWidth.setToolTipText(JvSwingUtils.wrapTooltip(true, MessageManager
            .getString("label.adjusts_width_generated_eps_png")));
    autoIdWidth.setBounds(new Rectangle(228, 144, 320, 23));
    autoIdWidth.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        autoIdWidth_actionPerformed();
      }
    });
    userIdWidthlabel.setFont(LABEL_FONT);
    userIdWidthlabel.setText(
            MessageManager.getString("label.figure_id_column_width"));
    userIdWidth.setToolTipText(JvSwingUtils.wrapTooltip(true, MessageManager
            .getString("label.manually_specify_width_left_column")));
    userIdWidthlabel.setToolTipText(
            JvSwingUtils.wrapTooltip(true, MessageManager.getString(
                    "label.manually_specify_width_left_column")));
    userIdWidthlabel.setBounds(new Rectangle(236, 168, 320, 23));
    userIdWidth.setFont(JvSwingUtils.getTextAreaFont());
    userIdWidth.setText("");
    userIdWidth.setBounds(new Rectangle(232, 192, 84, 23));
    userIdWidth.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        userIdWidth_actionPerformed();
      }
    });
    modellerOutput.setFont(LABEL_FONT);
    modellerOutput
            .setText(MessageManager.getString("label.use_modeller_output"));
    modellerOutput.setBounds(new Rectangle(228, 274, 320, 23));
    embbedBioJSON.setFont(LABEL_FONT);
    embbedBioJSON.setText(MessageManager.getString("label.embbed_biojson"));
    embbedBioJSON.setBounds(new Rectangle(228, 248, 250, 23));

    jPanel11.add(jLabel1);
    jPanel11.add(blcjv);
    jPanel11.add(clustaljv);
    jPanel11.add(fastajv);
    jPanel11.add(msfjv);
    jPanel11.add(pfamjv);
    jPanel11.add(pileupjv);
    jPanel11.add(pirjv);
    outputTab.add(autoIdWidth);
    outputTab.add(userIdWidth);
    outputTab.add(userIdWidthlabel);
    outputTab.add(modellerOutput);
    if (!Platform.isJS())
    {
      /*
       * JalviewJS doesn't support Lineart option or SVG output
       */
      outputTab.add(embbedBioJSON);
      outputTab.add(epsLabel);
      outputTab.add(epsRendering);
      outputTab.add(htmlLabel);
      outputTab.add(htmlRendering);
      outputTab.add(svgLabel);
      outputTab.add(svgRendering);
    }
    outputTab.add(jPanel11);
    return outputTab;
  }

  /**
   * Initialises the Connections tabbed panel.
   * 
   * @return
   */
  private JPanel initConnectionsTab()
  {
    connectTab = new JPanel();
    connectTab.setLayout(new GridBagLayout());

    JPanel proxyPanel = initConnTabProxyPanel();
    initConnTabCheckboxes();

    // Add proxy server panel
    connectTab.add(proxyPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(10, 0, 5, 12), 4, 10));

    // Add usage stats, version check and questionnaire checkboxes
    connectTab.add(usagestats,
            new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 2, 5, 5), 70, 1));
    connectTab.add(questionnaire,
            new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 2, 5, 10), 70, 1));
    connectTab.add(versioncheck,
            new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 2, 5, 5), 70, 1));

    versioncheck.setVisible(false);

    // Add padding so the panel doesn't look ridiculous
    JPanel spacePanel = new JPanel();
    connectTab.add(spacePanel,
            new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 70, 1));

    return connectTab;
  }

  /**
   * Initialises the Links tabbed panel.
   * 
   * @return
   */
  private JPanel initLinksTab()
  {
    JPanel linkTab = new JPanel();
    linkTab.setLayout(new GridBagLayout());

    // Set up table for Url links
    linkUrlTable.getTableHeader().setReorderingAllowed(false);
    linkUrlTable.setFillsViewportHeight(true);
    linkUrlTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    linkUrlTable.setAutoCreateRowSorter(true);
    linkUrlTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // adjust row height so radio buttons actually fit
    // don't do this in the renderer, it causes the awt thread to activate
    // constantly
    JRadioButton temp = new JRadioButton();
    linkUrlTable.setRowHeight(temp.getMinimumSize().height);

    // Table in scrollpane so that the table is given a scrollbar
    JScrollPane linkScrollPane = new JScrollPane(linkUrlTable);
    linkScrollPane.setBorder(null);

    // Panel for links functionality
    JPanel linkPanel = new JPanel(new GridBagLayout());
    linkPanel.setBorder(new TitledBorder(
            MessageManager.getString("label.url_linkfrom_sequence_id")));

    // Put the Url links panel together

    // Buttons go at top right, resizing only resizes the blank space vertically
    JPanel buttonPanel = initLinkTabUrlButtons();
    GridBagConstraints linkConstraints1 = new GridBagConstraints();
    linkConstraints1.insets = new Insets(0, 0, 5, 0);
    linkConstraints1.gridx = 0;
    linkConstraints1.gridy = 0;
    linkConstraints1.weightx = 1.0;
    linkConstraints1.fill = GridBagConstraints.HORIZONTAL;
    linkTab.add(buttonPanel, linkConstraints1);

    // Links table goes at top left, resizing resizes the table
    GridBagConstraints linkConstraints2 = new GridBagConstraints();
    linkConstraints2.insets = new Insets(0, 0, 5, 5);
    linkConstraints2.gridx = 0;
    linkConstraints2.gridy = 1;
    linkConstraints2.weightx = 1.0;
    linkConstraints2.weighty = 1.0;
    linkConstraints2.fill = GridBagConstraints.BOTH;
    linkTab.add(linkScrollPane, linkConstraints2);

    // Filter box and buttons goes at bottom left, resizing resizes the text box
    JPanel filterPanel = initLinkTabFilterPanel();
    GridBagConstraints linkConstraints3 = new GridBagConstraints();
    linkConstraints3.insets = new Insets(0, 0, 0, 5);
    linkConstraints3.gridx = 0;
    linkConstraints3.gridy = 2;
    linkConstraints3.weightx = 1.0;
    linkConstraints3.fill = GridBagConstraints.HORIZONTAL;
    linkTab.add(filterPanel, linkConstraints3);

    return linkTab;
  }

  private JPanel initLinkTabFilterPanel()
  {
    // Filter textbox and reset button
    JLabel filterLabel = new JLabel(
            MessageManager.getString("label.filter"));
    filterLabel.setFont(LABEL_FONT);
    filterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    filterLabel.setHorizontalTextPosition(SwingConstants.LEADING);

    filterTB.setFont(LABEL_FONT);
    filterTB.setText("");

    doReset.setText(MessageManager.getString("action.showall"));
    userOnly.setText(MessageManager.getString("action.customfilter"));

    // Panel for filter functionality
    JPanel filterPanel = new JPanel(new GridBagLayout());
    filterPanel.setBorder(new TitledBorder("Filter"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    filterPanel.add(filterLabel, gbc);

    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.gridx = 1;
    gbc1.gridwidth = 2;
    gbc1.fill = GridBagConstraints.HORIZONTAL;
    gbc1.anchor = GridBagConstraints.WEST;
    gbc1.weightx = 1.0;
    filterPanel.add(filterTB, gbc1);

    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.gridx = 3;
    gbc2.fill = GridBagConstraints.NONE;
    gbc2.anchor = GridBagConstraints.WEST;
    filterPanel.add(doReset, gbc2);

    GridBagConstraints gbc3 = new GridBagConstraints();
    gbc3.gridx = 4;
    gbc3.fill = GridBagConstraints.NONE;
    gbc3.anchor = GridBagConstraints.WEST;
    filterPanel.add(userOnly, gbc3);

    return filterPanel;
  }

  private JPanel initLinkTabUrlButtons()
  {
    // Buttons for new / edit / delete Url links
    JButton newLink = new JButton();
    newLink.setText(MessageManager.getString("action.new"));

    editLink.setText(MessageManager.getString("action.edit"));

    deleteLink.setText(MessageManager.getString("action.delete"));

    // no current selection, so initially disable delete/edit buttons
    editLink.setEnabled(false);
    deleteLink.setEnabled(false);

    newLink.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        newLink_actionPerformed(e);
      }
    });

    editLink.setText(MessageManager.getString("action.edit"));
    editLink.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        editLink_actionPerformed(e);
      }
    });

    deleteLink.setText(MessageManager.getString("action.delete"));
    deleteLink.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        deleteLink_actionPerformed(e);
      }
    });

    JPanel buttonPanel = new JPanel(new GridBagLayout());
    buttonPanel.setBorder(new TitledBorder("Edit links"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.NONE;
    buttonPanel.add(newLink, gbc);

    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.gridx = 1;
    gbc1.gridy = 0;
    gbc1.fill = GridBagConstraints.NONE;
    buttonPanel.add(editLink, gbc1);

    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.gridx = 2;
    gbc2.gridy = 0;
    gbc2.fill = GridBagConstraints.NONE;
    buttonPanel.add(deleteLink, gbc2);

    GridBagConstraints gbc3 = new GridBagConstraints();
    gbc3.gridx = 3;
    gbc3.gridy = 0;
    gbc3.fill = GridBagConstraints.HORIZONTAL;
    gbc3.weightx = 1.0;
    JPanel spacePanel = new JPanel();
    spacePanel.setBorder(null);
    buttonPanel.add(spacePanel, gbc3);

    return buttonPanel;
  }

  /**
   * Initialises the proxy server panel in the Connections tab
   * 
   * @return the proxy server panel
   */
  private JPanel initConnTabProxyPanel()
  {
    // Label for server text box
    serverLabel.setText(MessageManager.getString("label.host") + ": ");
    serverLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    serverLabel.setFont(LABEL_FONT);
    serverLabel2.setText(MessageManager.getString("label.host") + ": ");
    serverLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    serverLabel2.setFont(LABEL_FONT);

    // Proxy server and port text boxes
    proxyServerHttpTB.setFont(LABEL_FONT);
    proxyServerHttpTB.setColumns(40);
    proxyPortHttpTB.setFont(LABEL_FONT);
    proxyPortHttpTB.setColumns(4);
    proxyServerHttpsTB.setFont(LABEL_FONT);
    proxyServerHttpsTB.setColumns(40);
    proxyPortHttpsTB.setFont(LABEL_FONT);
    proxyPortHttpsTB.setColumns(4);
    proxyAuthUsernameTB.setFont(LABEL_FONT);
    proxyAuthUsernameTB.setColumns(30);

    // check for any change to enable applyProxyButton
    DocumentListener d = new DocumentListener()
    {
      @Override
      public void changedUpdate(DocumentEvent e)
      {
        applyProxyButtonEnabled(true);
      }

      @Override
      public void insertUpdate(DocumentEvent e)
      {
        applyProxyButtonEnabled(true);
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        applyProxyButtonEnabled(true);
      }
    };
    proxyServerHttpTB.getDocument().addDocumentListener(d);
    proxyPortHttpTB.getDocument().addDocumentListener(d);
    proxyServerHttpsTB.getDocument().addDocumentListener(d);
    proxyPortHttpsTB.getDocument().addDocumentListener(d);
    proxyAuthUsernameTB.getDocument().addDocumentListener(d);
    proxyAuthPasswordPB.setFont(LABEL_FONT);
    proxyAuthPasswordPB.setColumns(30);
    proxyAuthPasswordPB.getDocument()
            .addDocumentListener(new DocumentListener()
            {
              @Override
              public void changedUpdate(DocumentEvent e)
              {
                proxyAuthPasswordCheckHighlight(true);
                applyProxyButtonEnabled(true);
              }

              @Override
              public void insertUpdate(DocumentEvent e)
              {
                proxyAuthPasswordCheckHighlight(true);
                applyProxyButtonEnabled(true);
              }

              @Override
              public void removeUpdate(DocumentEvent e)
              {
                proxyAuthPasswordCheckHighlight(true);
                applyProxyButtonEnabled(true);
              }

            });

    // Label for Port text box
    portLabel.setFont(LABEL_FONT);
    portLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    portLabel.setText(MessageManager.getString("label.port") + ": ");
    portLabel2.setFont(LABEL_FONT);
    portLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    portLabel2.setText(MessageManager.getString("label.port") + ": ");

    httpLabel.setText("HTTP");
    httpLabel.setFont(LABEL_FONT_BOLD);
    httpLabel.setHorizontalAlignment(SwingConstants.LEFT);
    httpsLabel.setText("HTTPS");
    httpsLabel.setFont(LABEL_FONT_BOLD);
    httpsLabel.setHorizontalAlignment(SwingConstants.LEFT);

    proxyAuthUsernameLabel
            .setText(MessageManager.getString("label.username") + ": ");
    proxyAuthUsernameLabel.setFont(LABEL_FONT);
    proxyAuthUsernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    proxyAuthPasswordLabel
            .setText(MessageManager.getString("label.password") + ": ");
    proxyAuthPasswordLabel.setFont(LABEL_FONT);
    proxyAuthPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    passwordNotStoredLabel.setText(
            "(" + MessageManager.getString("label.not_stored") + ")");
    passwordNotStoredLabel.setFont(LABEL_FONT_ITALIC);
    passwordNotStoredLabel.setHorizontalAlignment(SwingConstants.LEFT);

    // Proxy type radio buttons
    noProxy.setFont(LABEL_FONT);
    noProxy.setHorizontalAlignment(SwingConstants.LEFT);
    noProxy.setText(MessageManager.getString("label.no_proxy"));
    systemProxy.setFont(LABEL_FONT);
    systemProxy.setHorizontalAlignment(SwingConstants.LEFT);
    systemProxy.setText(MessageManager.formatMessage("label.system_proxy",
            displayUserHostPort(Cache.startupProxyProperties[4],
                    Cache.startupProxyProperties[0],
                    Cache.startupProxyProperties[1]),
            displayUserHostPort(Cache.startupProxyProperties[6],
                    Cache.startupProxyProperties[2],
                    Cache.startupProxyProperties[3])));
    customProxy.setFont(LABEL_FONT);
    customProxy.setHorizontalAlignment(SwingConstants.LEFT);
    customProxy.setText(
            MessageManager.getString("label.use_proxy_server") + ":");
    ActionListener al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        proxyType_actionPerformed();
      }
    };
    noProxy.addActionListener(al);
    systemProxy.addActionListener(al);
    customProxy.addActionListener(al);
    proxyType.add(noProxy);
    proxyType.add(systemProxy);
    proxyType.add(customProxy);

    proxyAuth.setFont(LABEL_FONT);
    proxyAuth.setHorizontalAlignment(SwingConstants.LEFT);
    proxyAuth.setText(MessageManager.getString("label.auth_required"));
    proxyAuth.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        proxyAuth_actionPerformed();
      }
    });

    setCustomProxyEnabled();

    // Make proxy server panel
    JPanel proxyPanel = new JPanel();
    TitledBorder titledBorder1 = new TitledBorder(
            MessageManager.getString("label.proxy_servers"));
    proxyPanel.setBorder(titledBorder1);
    proxyPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    GridBagConstraints c = new GridBagConstraints();
    // Proxy type radio buttons (3)
    JPanel ptPanel = new JPanel();
    ptPanel.setLayout(new GridBagLayout());
    c.weightx = 1.0;
    c.gridy = 0;
    c.gridx = 0;
    c.gridwidth = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    ptPanel.add(noProxy, c);
    c.gridy++;
    ptPanel.add(systemProxy, c);
    c.gridy++;
    ptPanel.add(customProxy, c);

    gbc.gridy = 0;
    proxyPanel.add(ptPanel, gbc);

    // host and port text boxes
    JPanel hpPanel = new JPanel();
    hpPanel.setLayout(new GridBagLayout());
    // HTTP host port row
    c.gridy = 0;
    c.gridx = 0;

    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_START;
    hpPanel.add(httpLabel, c);

    c.gridx++;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    hpPanel.add(serverLabel, c);

    c.gridx++;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.LINE_START;
    hpPanel.add(proxyServerHttpTB, c);

    c.gridx++;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    hpPanel.add(portLabel, c);

    c.gridx++;
    c.weightx = 0.2;
    c.anchor = GridBagConstraints.LINE_START;
    hpPanel.add(proxyPortHttpTB, c);

    // HTTPS host port row
    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;

    c.anchor = GridBagConstraints.LINE_START;
    hpPanel.add(httpsLabel, c);

    c.gridx++;
    c.anchor = GridBagConstraints.LINE_END;
    hpPanel.add(serverLabel2, c);

    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    hpPanel.add(proxyServerHttpsTB, c);

    c.gridx++;
    c.anchor = GridBagConstraints.LINE_END;
    hpPanel.add(portLabel2, c);

    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    hpPanel.add(proxyPortHttpsTB, c);

    gbc.gridy++;
    proxyPanel.add(hpPanel, gbc);

    if (!Platform.isJS())
    /**
     * java.net.Authenticator is not implemented in SwingJS. Not displaying the
     * Authentication options in Preferences.
     * 
     * @j2sIgnore
     * 
     */
    {
      // Require authentication checkbox
      gbc.gridy++;
      proxyPanel.add(proxyAuth, gbc);

      // username and password
      JPanel upPanel = new JPanel();
      upPanel.setLayout(new GridBagLayout());
      // username row
      c.gridy = 0;
      c.gridx = 0;
      c.gridwidth = 1;
      c.weightx = 0.4;
      c.anchor = GridBagConstraints.LINE_END;
      upPanel.add(proxyAuthUsernameLabel, c);

      c.gridx++;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.LINE_START;
      upPanel.add(proxyAuthUsernameTB, c);

      // password row
      c.gridy++;
      c.gridx = 0;
      c.weightx = 0.4;
      c.anchor = GridBagConstraints.LINE_END;
      upPanel.add(proxyAuthPasswordLabel, c);

      c.gridx++;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.LINE_START;
      upPanel.add(proxyAuthPasswordPB, c);

      c.gridx++;
      c.weightx = 0.4;
      c.anchor = GridBagConstraints.LINE_START;
      upPanel.add(passwordNotStoredLabel, c);

      gbc.gridy++;
      proxyPanel.add(upPanel, gbc);

    } // end j2sIgnore

    applyProxyButton.setText(MessageManager.getString("action.apply"));
    applyProxyButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveProxySettings();
        applyProxyButton.setEnabled(false);
      }
    });
    gbc.gridy++;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.LINE_END;
    proxyPanel.add(applyProxyButton, gbc);

    return proxyPanel;
  }

  public void proxyAuthPasswordCheckHighlight(boolean enabled)
  {
    proxyAuthPasswordCheckHighlight(enabled, false);
  }

  public void proxyAuthPasswordCheckHighlight(boolean enabled,
          boolean grabFocus)
  {
    if (enabled && proxyType.isSelected(customProxy.getModel())
            && proxyAuth.isSelected()
            && !proxyAuthUsernameTB.getText().isEmpty()
            && proxyAuthPasswordPB.getDocument().getLength() == 0)
    {
      if (grabFocus)
        proxyAuthPasswordPB.grabFocus();
      proxyAuthPasswordPB.setBackground(Color.PINK);
    }
    else
    {
      proxyAuthPasswordPB.setBackground(Color.WHITE);
    }
  }

  public void applyProxyButtonEnabled(boolean enabled)
  {
    applyProxyButton.setEnabled(enabled);
  }

  public void saveProxySettings()
  {
    // overridden in Preferences
  }

  private String displayUserHostPort(String user, String host, String port)
  {
    boolean hostBlank = (host == null || host.isEmpty());
    boolean portBlank = (port == null || port.isEmpty());
    if (hostBlank && portBlank)
    {
      return MessageManager.getString("label.none");
    }

    StringBuilder sb = new StringBuilder();
    if (user != null)
    {
      sb.append(user.isEmpty() || user.indexOf(" ") > -1 ? '"' + user + '"'
              : user);
      sb.append("@");
    }
    sb.append(hostBlank ? "" : host);
    if (!portBlank)
    {
      sb.append(":");
      sb.append(port);
    }
    return sb.toString();
  }

  /**
   * Initialises the checkboxes in the Connections tab
   */
  private void initConnTabCheckboxes()
  {
    // Usage stats checkbox label
    usagestats.setText(
            MessageManager.getString("label.send_usage_statistics"));
    usagestats.setFont(LABEL_FONT);
    usagestats.setHorizontalAlignment(SwingConstants.RIGHT);
    usagestats.setHorizontalTextPosition(SwingConstants.LEADING);

    // Questionnaire checkbox label
    questionnaire.setText(
            MessageManager.getString("label.check_for_questionnaires"));
    questionnaire.setFont(LABEL_FONT);
    questionnaire.setHorizontalAlignment(SwingConstants.RIGHT);
    questionnaire.setHorizontalTextPosition(SwingConstants.LEADING);

    // Check for latest version checkbox label
    versioncheck.setText(
            MessageManager.getString("label.check_for_latest_version"));
    versioncheck.setFont(LABEL_FONT);
    versioncheck.setHorizontalAlignment(SwingConstants.RIGHT);
    versioncheck.setHorizontalTextPosition(SwingConstants.LEADING);
  }

  /**
   * Initialises the parent panel which contains the tabbed sections.
   * 
   * @return
   */
  private JPanel initOkCancelPanel()
  {
    JButton ok = new JButton();
    ok.setText(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    JButton cancel = new JButton();
    cancel.setText(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    JPanel okCancelPanel = new JPanel();
    okCancelPanel.add(ok);
    okCancelPanel.add(cancel);
    return okCancelPanel;
  }

  /**
   * Initialises the Colours tabbed panel.
   * 
   * @return
   */
  private JPanel initColoursTab()
  {
    JPanel coloursTab = new JPanel();
    coloursTab.setBorder(new TitledBorder(
            MessageManager.getString("action.open_new_alignment")));
    coloursTab.setLayout(new FlowLayout());
    JLabel mincolourLabel = new JLabel();
    mincolourLabel.setFont(LABEL_FONT);
    mincolourLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    mincolourLabel.setText(MessageManager.getString("label.min_colour"));
    minColour.setFont(LABEL_FONT);
    minColour.setBorder(BorderFactory.createEtchedBorder());
    minColour.setPreferredSize(new Dimension(40, 20));
    minColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        minColour_actionPerformed(minColour);
      }
    });
    JLabel maxcolourLabel = new JLabel();
    maxcolourLabel.setFont(LABEL_FONT);
    maxcolourLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    maxcolourLabel.setText(MessageManager.getString("label.max_colour"));
    maxColour.setFont(LABEL_FONT);
    maxColour.setBorder(BorderFactory.createEtchedBorder());
    maxColour.setPreferredSize(new Dimension(40, 20));
    maxColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        maxColour_actionPerformed(maxColour);
      }
    });

    protColour.setFont(LABEL_FONT);
    protColour.setBounds(new Rectangle(172, 225, 155, 21));
    JLabel protColourLabel = new JLabel();
    protColourLabel.setFont(LABEL_FONT);
    protColourLabel.setHorizontalAlignment(SwingConstants.LEFT);
    protColourLabel.setText(
            MessageManager.getString("label.prot_alignment_colour") + " ");
    JvSwingUtils.addtoLayout(coloursTab,
            MessageManager
                    .getString("label.default_colour_scheme_for_alignment"),
            protColourLabel, protColour);

    nucColour.setFont(LABEL_FONT);
    nucColour.setBounds(new Rectangle(172, 240, 155, 21));
    JLabel nucColourLabel = new JLabel();
    nucColourLabel.setFont(LABEL_FONT);
    nucColourLabel.setHorizontalAlignment(SwingConstants.LEFT);
    nucColourLabel.setText(
            MessageManager.getString("label.nuc_alignment_colour") + " ");
    JvSwingUtils.addtoLayout(coloursTab,
            MessageManager
                    .getString("label.default_colour_scheme_for_alignment"),
            nucColourLabel, nucColour);

    JPanel annotationShding = new JPanel();
    annotationShding.setBorder(new TitledBorder(
            MessageManager.getString("label.annotation_shading_default")));
    annotationShding.setLayout(new GridLayout(1, 2));
    JvSwingUtils.addtoLayout(annotationShding,
            MessageManager.getString(
                    "label.default_minimum_colour_annotation_shading"),
            mincolourLabel, minColour);
    JvSwingUtils.addtoLayout(annotationShding,
            MessageManager.getString(
                    "label.default_maximum_colour_annotation_shading"),
            maxcolourLabel, maxColour);
    coloursTab.add(annotationShding); // , FlowLayout.LEFT);
    return coloursTab;
  }

  /**
   * Initialises the Overview tabbed panel.
   * 
   * @return
   */
  private JPanel initOverviewTab()
  {
    JPanel overviewPanel = new JPanel();
    overviewPanel.setBorder(new TitledBorder(
            MessageManager.getString("label.overview_settings")));

    gapColour.setFont(LABEL_FONT);
    // fixing the border colours stops apparent colour bleed from the panel
    gapColour.setBorder(
            BorderFactory.createEtchedBorder(Color.white, Color.lightGray));
    gapColour.setPreferredSize(new Dimension(40, 20));
    gapColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        gapColour_actionPerformed(gapColour);
      }
    });

    hiddenColour.setFont(LABEL_FONT);
    // fixing the border colours stops apparent colour bleed from the panel
    hiddenColour.setBorder(
            BorderFactory.createEtchedBorder(Color.white, Color.lightGray));
    hiddenColour.setPreferredSize(new Dimension(40, 20));
    hiddenColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        hiddenColour_actionPerformed(hiddenColour);
      }
    });

    useLegacyGap = new JCheckBox(
            MessageManager.getString("label.ov_legacy_gap"));
    useLegacyGap.setFont(LABEL_FONT);
    useLegacyGap.setHorizontalAlignment(SwingConstants.LEFT);
    useLegacyGap.setVerticalTextPosition(SwingConstants.TOP);
    gapLabel = new JLabel(MessageManager.getString("label.gap_colour"));
    gapLabel.setFont(LABEL_FONT);
    gapLabel.setHorizontalAlignment(SwingConstants.LEFT);
    gapLabel.setVerticalTextPosition(SwingConstants.TOP);
    showHiddenAtStart = new JCheckBox(
            MessageManager.getString("label.ov_show_hide_default"));
    showHiddenAtStart.setFont(LABEL_FONT);
    showHiddenAtStart.setHorizontalAlignment(SwingConstants.LEFT);
    showHiddenAtStart.setVerticalTextPosition(SwingConstants.TOP);
    JLabel hiddenLabel = new JLabel(
            MessageManager.getString("label.hidden_colour"));
    hiddenLabel.setFont(LABEL_FONT);
    hiddenLabel.setHorizontalAlignment(SwingConstants.LEFT);
    hiddenLabel.setVerticalTextPosition(SwingConstants.TOP);

    useLegacyGap.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        useLegacyGaps_actionPerformed(e);
      }
    });

    overviewPanel.setLayout(new GridBagLayout());
    GridBagConstraints c1 = new GridBagConstraints();

    c1.fill = GridBagConstraints.HORIZONTAL;
    c1.gridx = 0;
    c1.gridy = 0;
    c1.weightx = 1;
    c1.ipady = 20;
    c1.anchor = GridBagConstraints.FIRST_LINE_START;
    overviewPanel.add(useLegacyGap, c1);

    GridBagConstraints c2 = new GridBagConstraints();
    c2.fill = GridBagConstraints.HORIZONTAL;
    c2.gridx = 1;
    c2.gridy = 0;
    c2.insets = new Insets(0, 15, 0, 10);
    overviewPanel.add(gapLabel, c2);

    GridBagConstraints c3 = new GridBagConstraints();
    c3.fill = GridBagConstraints.HORIZONTAL;
    c3.gridx = 2;
    c3.gridy = 0;
    c3.insets = new Insets(0, 0, 0, 15);
    overviewPanel.add(gapColour, c3);

    GridBagConstraints c4 = new GridBagConstraints();
    c4.fill = GridBagConstraints.HORIZONTAL;
    c4.gridx = 0;
    c4.gridy = 1;
    c4.weightx = 1;
    overviewPanel.add(showHiddenAtStart, c4);

    GridBagConstraints c5 = new GridBagConstraints();
    c5.fill = GridBagConstraints.HORIZONTAL;
    c5.gridx = 1;
    c5.gridy = 1;
    c5.insets = new Insets(0, 15, 0, 10);
    overviewPanel.add(hiddenLabel, c5);

    GridBagConstraints c6 = new GridBagConstraints();
    c6.fill = GridBagConstraints.HORIZONTAL;
    c6.gridx = 2;
    c6.gridy = 1;
    c6.insets = new Insets(0, 0, 0, 15);
    overviewPanel.add(hiddenColour, c6);

    JButton resetButton = new JButton(
            MessageManager.getString("label.reset_to_defaults"));

    resetButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        resetOvDefaults_actionPerformed(e);
      }
    });

    GridBagConstraints c7 = new GridBagConstraints();
    c7.fill = GridBagConstraints.NONE;
    c7.gridx = 0;
    c7.gridy = 2;
    c7.insets = new Insets(10, 0, 0, 0);
    c7.anchor = GridBagConstraints.WEST;
    overviewPanel.add(resetButton, c7);

    // Add padding so the panel doesn't look ridiculous
    JPanel spacePanel = new JPanel();
    overviewPanel.add(spacePanel,
            new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

    return overviewPanel;
  }

  /**
   * Initialises the Structure tabbed panel.
   * 
   * @return
   */
  private JPanel initStructureTab()
  {
    structureTab = new JPanel();

    structureTab.setBorder(new TitledBorder(
            MessageManager.getString("label.structure_options")));
    structureTab.setLayout(null);
    final int width = 420;
    final int height = 22;
    final int lineSpacing = 25;
    int ypos = 15;

    structFromPdb.setFont(LABEL_FONT);
    structFromPdb
            .setText(MessageManager.getString("label.struct_from_pdb"));
    structFromPdb.setBounds(new Rectangle(5, ypos, width, height));
    structFromPdb.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        boolean selected = structFromPdb.isSelected();
        // enable other options only when the first is checked
        addSecondaryStructure.setEnabled(selected);
        addTempFactor.setEnabled(selected);
      }
    });
    structureTab.add(structFromPdb);

    // indent checkboxes that are conditional on the first one
    ypos += lineSpacing;
    addSecondaryStructure.setFont(LABEL_FONT);
    addSecondaryStructure
            .setText(MessageManager.getString("label.autoadd_secstr"));
    addSecondaryStructure.setBounds(new Rectangle(25, ypos, width, height));
    structureTab.add(addSecondaryStructure);

    ypos += lineSpacing;
    addTempFactor.setFont(LABEL_FONT);
    addTempFactor.setText(MessageManager.getString("label.autoadd_temp"));
    addTempFactor.setBounds(new Rectangle(25, ypos, width, height));
    structureTab.add(addTempFactor);

    ypos += lineSpacing;
    JLabel viewerLabel = new JLabel();
    viewerLabel.setFont(LABEL_FONT);
    viewerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    viewerLabel.setText(MessageManager.getString("label.structure_viewer"));
    viewerLabel.setBounds(new Rectangle(10, ypos, 220, height));
    structureTab.add(viewerLabel);

    /*
     * add all external viewers as options here - check 
     * when selected whether the program is installed
     */
    structViewer.setFont(LABEL_FONT);
    structViewer.setBounds(new Rectangle(190, ypos, 120, height));
    for (ViewerType v : EnumSet.allOf(ViewerType.class))
    {
      structViewer.addItem(v.name());
    }
    /*
    structViewer.addItem(ViewerType.JMOL.name());
    structViewer.addItem(ViewerType.CHIMERA.name());
    structViewer.addItem(ViewerType.CHIMERAX.name());
    structViewer.addItem(ViewerType.PYMOL.name());
    */
    structViewer.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        structureViewer_actionPerformed(
                (String) structViewer.getSelectedItem());
      }
    });
    structureTab.add(structViewer);

    ypos += lineSpacing;
    structureViewerPathLabel = new JLabel();
    structureViewerPathLabel.setFont(LABEL_FONT);// new Font("SansSerif", 0,
                                                 // 11));
    structureViewerPathLabel.setHorizontalAlignment(SwingConstants.LEFT);
    structureViewerPathLabel.setText(MessageManager
            .formatMessage("label.viewer_path", "Chimera(X)"));
    structureViewerPathLabel
            .setBounds(new Rectangle(10, ypos, 170, height));
    structureViewerPathLabel.setEnabled(false);
    structureTab.add(structureViewerPathLabel);

    structureViewerPath.setFont(LABEL_FONT);
    structureViewerPath.setText("");
    structureViewerPath.setEnabled(false);
    final String tooltip = JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.viewer_path_tip"));
    structureViewerPath.setToolTipText(tooltip);
    structureViewerPath.setBounds(new Rectangle(190, ypos, 290, height));
    structureViewerPath.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (structureViewerPath.isEnabled() && e.getClickCount() == 2)
        {
          String chosen = openFileChooser();
          if (chosen != null)
          {
            structureViewerPath.setText(chosen);
          }
        }
      }
    });
    structureTab.add(structureViewerPath);

    ypos += lineSpacing;
    nwMapping.setFont(LABEL_FONT);
    nwMapping.setText(MessageManager.getString("label.nw_mapping"));
    siftsMapping.setFont(LABEL_FONT);
    siftsMapping.setText(MessageManager.getString("label.sifts_mapping"));
    mappingMethod.add(nwMapping);
    mappingMethod.add(siftsMapping);
    JPanel mappingPanel = new JPanel();
    mappingPanel.setFont(LABEL_FONT);
    TitledBorder mmTitledBorder = new TitledBorder(
            MessageManager.getString("label.mapping_method"));
    mmTitledBorder.setTitleFont(LABEL_FONT);
    mappingPanel.setBorder(mmTitledBorder);
    mappingPanel.setBounds(new Rectangle(10, ypos, 472, 45));
    // GridLayout mappingLayout = new GridLayout();
    mappingPanel.setLayout(new GridLayout());
    mappingPanel.add(nwMapping);
    mappingPanel.add(siftsMapping);
    structureTab.add(mappingPanel);

    ypos += lineSpacing;
    ypos += lineSpacing;
    FTSDataColumnPreferences docFieldPref = new FTSDataColumnPreferences(
            PreferenceSource.PREFERENCES, PDBFTSRestClient.getInstance());
    docFieldPref.setBounds(new Rectangle(10, ypos, 470, 120));
    structureTab.add(docFieldPref);

    /*
     * hide Chimera options in JalviewJS
     */
    if (Platform.isJS())
    {
      structureViewerPathLabel.setVisible(false);
      structureViewerPath.setVisible(false);
      viewerLabel.setVisible(false);
      structViewer.setVisible(false);
    }

    return structureTab;
  }

  /**
   * Action on choosing a structure viewer from combobox options.
   * 
   * @param selectedItem
   */
  protected void structureViewer_actionPerformed(String selectedItem)
  {
  }

  /**
   * Show a dialog for the user to choose a file. Returns the chosen path, or
   * null on Cancel.
   * 
   * @return
   */
  protected String openFileChooser()
  {
    String choice = null;
    JFileChooser chooser = new JFileChooser();

    // Enable appBundleIsTraversable in macOS FileChooser to allow selecting
    // hidden executables within .app dirs
    if (Platform.isMac())
    {
      chooser.putClientProperty("JFileChooser.appBundleIsTraversable",
              true);
    }

    // chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.open_local_file"));
    chooser.setToolTipText(MessageManager.getString("action.open"));

    int value = chooser.showOpenDialog(this);

    if (value == JFileChooser.APPROVE_OPTION)
    {
      choice = chooser.getSelectedFile().getPath();
    }
    return choice;
  }

  /**
   * Validate the structure tab preferences; if invalid, set focus on this tab.
   * 
   * @param e
   */
  protected boolean validateStructure(FocusEvent e)
  {
    if (!validateStructure())
    {
      e.getComponent().requestFocusInWindow();
      return false;
    }
    return true;
  }

  protected boolean validateStructure()
  {
    return false;
  }

  /**
   * Initialises the Visual tabbed panel.
   * 
   * @return
   */
  private JPanel initVisualTab()
  {
    JPanel visualTab = new JPanel();
    visualTab.setBorder(new TitledBorder(
            MessageManager.getString("action.open_new_alignment")));
    visualTab.setLayout(null);
    fullScreen.setFont(LABEL_FONT);
    fullScreen.setHorizontalAlignment(SwingConstants.RIGHT);
    fullScreen.setHorizontalTextPosition(SwingConstants.LEFT);
    fullScreen.setText(MessageManager.getString("label.maximize_window"));
    quality.setEnabled(false);
    quality.setFont(LABEL_FONT);
    quality.setHorizontalAlignment(SwingConstants.RIGHT);
    quality.setHorizontalTextPosition(SwingConstants.LEFT);
    quality.setSelected(true);
    quality.setText(MessageManager.getString("label.quality"));
    conservation.setEnabled(false);
    conservation.setFont(LABEL_FONT);
    conservation.setHorizontalAlignment(SwingConstants.RIGHT);
    conservation.setHorizontalTextPosition(SwingConstants.LEFT);
    conservation.setSelected(true);
    conservation.setText(MessageManager.getString("label.conservation"));
    identity.setEnabled(false);
    identity.setFont(LABEL_FONT);
    identity.setHorizontalAlignment(SwingConstants.RIGHT);
    identity.setHorizontalTextPosition(SwingConstants.LEFT);
    identity.setSelected(true);
    identity.setText(MessageManager.getString("label.consensus"));
    showOccupancy.setFont(LABEL_FONT);
    showOccupancy.setEnabled(false);
    showOccupancy.setHorizontalAlignment(SwingConstants.RIGHT);
    showOccupancy.setHorizontalTextPosition(SwingConstants.LEFT);
    showOccupancy.setSelected(true);
    showOccupancy.setText(MessageManager.getString("label.occupancy"));

    JLabel showGroupbits = new JLabel();
    showGroupbits.setFont(LABEL_FONT);
    showGroupbits.setHorizontalAlignment(SwingConstants.RIGHT);
    showGroupbits.setHorizontalTextPosition(SwingConstants.LEFT);
    showGroupbits
            .setText(MessageManager.getString("action.show_group") + ":");
    JLabel showConsensbits = new JLabel();
    showConsensbits.setFont(LABEL_FONT);
    showConsensbits.setHorizontalAlignment(SwingConstants.RIGHT);
    showConsensbits.setHorizontalTextPosition(SwingConstants.LEFT);
    showConsensbits
            .setText(MessageManager.getString("label.consensus") + ":");
    showConsensHistogram.setEnabled(false);
    showConsensHistogram.setFont(LABEL_FONT);
    showConsensHistogram.setHorizontalAlignment(SwingConstants.RIGHT);
    showConsensHistogram.setHorizontalTextPosition(SwingConstants.LEFT);
    showConsensHistogram.setSelected(true);
    showConsensHistogram
            .setText(MessageManager.getString("label.histogram"));
    showConsensLogo.setEnabled(false);
    showConsensLogo.setFont(LABEL_FONT);
    showConsensLogo.setHorizontalAlignment(SwingConstants.RIGHT);
    showConsensLogo.setHorizontalTextPosition(SwingConstants.LEFT);
    showConsensLogo.setSelected(true);
    showConsensLogo.setText(MessageManager.getString("label.logo"));
    showGroupConsensus.setEnabled(false);
    showGroupConsensus.setFont(LABEL_FONT);
    showGroupConsensus.setHorizontalAlignment(SwingConstants.RIGHT);
    showGroupConsensus.setHorizontalTextPosition(SwingConstants.LEFT);
    showGroupConsensus.setSelected(true);
    showGroupConsensus.setText(MessageManager.getString("label.consensus"));
    showGroupConservation.setEnabled(false);
    showGroupConservation.setFont(LABEL_FONT);
    showGroupConservation.setHorizontalAlignment(SwingConstants.RIGHT);
    showGroupConservation.setHorizontalTextPosition(SwingConstants.LEFT);
    showGroupConservation.setSelected(true);
    showGroupConservation
            .setText(MessageManager.getString("label.conservation"));
    showNpTooltip.setEnabled(true);
    showNpTooltip.setFont(LABEL_FONT);
    showNpTooltip.setHorizontalAlignment(SwingConstants.RIGHT);
    showNpTooltip.setHorizontalTextPosition(SwingConstants.LEFT);
    showNpTooltip.setSelected(true);
    showNpTooltip.setText(
            MessageManager.getString("label.non_positional_features"));
    showDbRefTooltip.setEnabled(true);
    showDbRefTooltip.setFont(LABEL_FONT);
    showDbRefTooltip.setHorizontalAlignment(SwingConstants.RIGHT);
    showDbRefTooltip.setHorizontalTextPosition(SwingConstants.LEFT);
    showDbRefTooltip.setSelected(true);
    showDbRefTooltip
            .setText(MessageManager.getString("label.database_references"));
    annotations.setFont(LABEL_FONT);
    annotations.setHorizontalAlignment(SwingConstants.RIGHT);
    annotations.setHorizontalTextPosition(SwingConstants.LEFT);
    annotations.setSelected(true);
    annotations.setText(MessageManager.getString("label.show_annotations"));
    // annotations.setBounds(new Rectangle(169, 12, 200, 23));
    annotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    identity.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    showGroupConsensus.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    showUnconserved.setFont(LABEL_FONT);
    showUnconserved.setHorizontalAlignment(SwingConstants.RIGHT);
    showUnconserved.setHorizontalTextPosition(SwingConstants.LEFT);
    showUnconserved.setSelected(true);
    showUnconserved
            .setText(MessageManager.getString("action.show_unconserved"));
    showUnconserved.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showunconserved_actionPerformed(e);
      }
    });

    // TODO these are not yet added to / action from Preferences
    // JCheckBox shareSelections = new JCheckBox();
    // shareSelections.setFont(verdana11);
    // shareSelections.setHorizontalAlignment(SwingConstants.RIGHT);
    // shareSelections.setHorizontalTextPosition(SwingConstants.LEFT);
    // shareSelections.setSelected(true);
    // shareSelections.setText(MessageManager
    // .getString("label.share_selection_across_views"));
    // JCheckBox followHighlight = new JCheckBox();
    // followHighlight.setFont(verdana11);
    // followHighlight.setHorizontalAlignment(SwingConstants.RIGHT);
    // followHighlight.setHorizontalTextPosition(SwingConstants.LEFT);
    // // showUnconserved.setBounds(new Rectangle(169, 40, 200, 23));
    // followHighlight.setSelected(true);
    // followHighlight.setText(MessageManager
    // .getString("label.scroll_highlighted_regions"));

    seqLimit.setFont(LABEL_FONT);
    seqLimit.setHorizontalAlignment(SwingConstants.RIGHT);
    seqLimit.setHorizontalTextPosition(SwingConstants.LEFT);
    seqLimit.setText(MessageManager.getString("label.full_sequence_id"));
    smoothFont.setFont(LABEL_FONT);
    smoothFont.setHorizontalAlignment(SwingConstants.RIGHT);
    smoothFont.setHorizontalTextPosition(SwingConstants.LEADING);
    smoothFont.setText(MessageManager.getString("label.smooth_font"));
    scaleProteinToCdna.setFont(LABEL_FONT);
    scaleProteinToCdna.setHorizontalAlignment(SwingConstants.RIGHT);
    scaleProteinToCdna.setHorizontalTextPosition(SwingConstants.LEADING);
    scaleProteinToCdna.setText(
            MessageManager.getString("label.scale_protein_to_cdna"));
    scaleProteinToCdna.setToolTipText(
            MessageManager.getString("label.scale_protein_to_cdna_tip"));
    JLabel gapLabel = new JLabel();
    gapLabel.setFont(LABEL_FONT);
    gapLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    gapLabel.setText(MessageManager.getString("label.gap_symbol") + " ");
    JLabel fontLabel = new JLabel();
    fontLabel.setFont(LABEL_FONT);
    fontLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    fontLabel.setText(MessageManager.getString("label.font"));
    fontSizeCB.setFont(LABEL_FONT);
    fontSizeCB.setBounds(new Rectangle(320, 112, 65, 23));
    fontStyleCB.setFont(LABEL_FONT);
    fontStyleCB.setBounds(new Rectangle(382, 112, 80, 23));
    fontNameCB.setFont(LABEL_FONT);
    fontNameCB.setBounds(new Rectangle(172, 112, 147, 23));
    gapSymbolCB.setFont(LABEL_FONT);
    gapSymbolCB.setBounds(new Rectangle(172, 215, 69, 23));
    DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
    dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
    gapSymbolCB.setRenderer(dlcr);

    startupCheckbox.setText(MessageManager.getString("action.open_file"));
    startupCheckbox.setFont(LABEL_FONT);
    startupCheckbox.setHorizontalAlignment(SwingConstants.RIGHT);
    startupCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
    startupCheckbox.setSelected(true);
    startupFileTextfield.setFont(LABEL_FONT);
    startupFileTextfield.setBounds(new Rectangle(172, 310, 330, 20));
    final String tooltip = JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.double_click_to_browse"));
    startupFileTextfield.setToolTipText(tooltip);
    startupFileTextfield.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          startupFileTextfield_mouseClicked();
        }
      }
    });

    sortby.setFont(LABEL_FONT);
    sortby.setBounds(new Rectangle(172, 260, 155, 21));
    JLabel sortLabel = new JLabel();
    sortLabel.setFont(LABEL_FONT);
    sortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    sortLabel.setText(MessageManager.getString("label.sort_by"));
    sortAnnBy.setFont(LABEL_FONT);
    sortAnnBy.setBounds(new Rectangle(172, 285, 110, 21));
    JLabel sortAnnLabel = new JLabel();
    sortAnnLabel.setFont(LABEL_FONT);
    sortAnnLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    sortAnnLabel.setText(MessageManager.getString("label.sort_ann_by"));
    sortAutocalc.setFont(LABEL_FONT);
    sortAutocalc.setBounds(new Rectangle(290, 285, 165, 21));

    JPanel annsettingsPanel = new JPanel();
    annsettingsPanel.setBounds(new Rectangle(173, 13, 320, 96));
    annsettingsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    annsettingsPanel.setBorder(new EtchedBorder());
    visualTab.add(annsettingsPanel);
    Border jb = new EmptyBorder(1, 1, 4, 5);
    annotations.setBorder(jb);
    showOccupancy.setBorder(jb);
    quality.setBorder(jb);
    conservation.setBorder(jb);
    identity.setBorder(jb);
    showConsensbits.setBorder(jb);
    showGroupbits.setBorder(jb);
    showGroupConsensus.setBorder(jb);
    showGroupConservation.setBorder(jb);
    showConsensHistogram.setBorder(jb);
    showConsensLogo.setBorder(jb);

    JPanel autoAnnotSettings = new JPanel();
    annsettingsPanel.add(autoAnnotSettings);
    autoAnnotSettings.setLayout(new GridLayout(0, 2));
    autoAnnotSettings.add(annotations);
    autoAnnotSettings.add(quality);
    // second row of autoannotation box
    autoAnnotSettings = new JPanel();
    annsettingsPanel.add(autoAnnotSettings);

    autoAnnotSettings.setLayout(new GridLayout(0, 3));
    autoAnnotSettings.add(conservation);
    autoAnnotSettings.add(identity);
    autoAnnotSettings.add(showOccupancy);
    autoAnnotSettings.add(showGroupbits);
    autoAnnotSettings.add(showGroupConservation);
    autoAnnotSettings.add(showGroupConsensus);
    autoAnnotSettings.add(showConsensbits);
    autoAnnotSettings.add(showConsensHistogram);
    autoAnnotSettings.add(showConsensLogo);

    JPanel tooltipSettings = new JPanel();
    tooltipSettings.setBorder(new TitledBorder(
            MessageManager.getString("label.sequence_id_tooltip")));
    tooltipSettings.setBounds(173, 140, 220, 62);
    tooltipSettings.setLayout(new GridLayout(2, 1));
    tooltipSettings.add(showDbRefTooltip);
    tooltipSettings.add(showNpTooltip);
    visualTab.add(tooltipSettings);

    wrap.setFont(LABEL_FONT);
    wrap.setHorizontalAlignment(SwingConstants.TRAILING);
    wrap.setHorizontalTextPosition(SwingConstants.LEADING);
    wrap.setText(MessageManager.getString("label.wrap_alignment"));
    rightAlign.setFont(LABEL_FONT);
    rightAlign.setForeground(Color.black);
    rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
    rightAlign.setHorizontalTextPosition(SwingConstants.LEFT);
    rightAlign.setText(MessageManager.getString("label.right_align_ids"));
    idItalics.setFont(LABEL_FONT_ITALIC);
    idItalics.setHorizontalAlignment(SwingConstants.RIGHT);
    idItalics.setHorizontalTextPosition(SwingConstants.LEADING);
    idItalics.setText(
            MessageManager.getString("label.sequence_name_italics"));
    openoverv.setFont(LABEL_FONT);
    openoverv.setActionCommand(
            MessageManager.getString("label.open_overview"));
    openoverv.setHorizontalAlignment(SwingConstants.RIGHT);
    openoverv.setHorizontalTextPosition(SwingConstants.LEFT);
    openoverv.setText(MessageManager.getString("label.open_overview"));
    JPanel jPanel2 = new JPanel();
    jPanel2.setBounds(new Rectangle(7, 17, 158, 310));
    jPanel2.setLayout(new GridLayout(14, 1));
    jPanel2.add(fullScreen);
    jPanel2.add(openoverv);
    jPanel2.add(seqLimit);
    jPanel2.add(rightAlign);
    jPanel2.add(fontLabel);
    jPanel2.add(showUnconserved);
    jPanel2.add(idItalics);
    jPanel2.add(smoothFont);
    jPanel2.add(scaleProteinToCdna);
    jPanel2.add(gapLabel);
    jPanel2.add(wrap);
    jPanel2.add(sortLabel);
    jPanel2.add(sortAnnLabel);
    jPanel2.add(startupCheckbox);
    visualTab.add(jPanel2);
    visualTab.add(startupFileTextfield);
    visualTab.add(sortby);
    visualTab.add(sortAnnBy);
    visualTab.add(sortAutocalc);
    visualTab.add(gapSymbolCB);
    visualTab.add(fontNameCB);
    visualTab.add(fontSizeCB);
    visualTab.add(fontStyleCB);

    if (Platform.isJS())
    {
      startupCheckbox.setVisible(false);
      startupFileTextfield.setVisible(false);
    }

    return visualTab;
  }

  /**
   * Load the saved Backups options EXCEPT "Enabled" and "Scheme"
   */

  protected void loadLastSavedBackupsOptions()
  {
    BackupFilesPresetEntry savedPreset = BackupFilesPresetEntry
            .getSavedBackupEntry();
    enableBackupFiles.setSelected(
            Cache.getDefault(BackupFiles.ENABLED, !Platform.isJS()));

    BackupFilesPresetEntry backupfilesCustomEntry = BackupFilesPresetEntry
            .createBackupFilesPresetEntry(Cache
                    .getDefault(BackupFilesPresetEntry.CUSTOMCONFIG, null));
    if (backupfilesCustomEntry == null)
    {
      backupfilesCustomEntry = BackupFilesPresetEntry.backupfilesPresetEntriesValues
              .get(BackupFilesPresetEntry.BACKUPFILESSCHEMEDEFAULT);
    }
    BackupFilesPresetEntry.backupfilesPresetEntriesValues.put(
            BackupFilesPresetEntry.BACKUPFILESSCHEMECUSTOM,
            backupfilesCustomEntry);

    setComboIntStringKey(backupfilesPresetsCombo,
            Cache.getDefault(BackupFiles.NS + "_PRESET",
                    BackupFilesPresetEntry.BACKUPFILESSCHEMEDEFAULT));

    backupsSetOptions(savedPreset);

    backupsOptionsSetEnabled();
    updateBackupFilesExampleLabel();
  }

  /*
   * Load the saved Memory settings
   */
  protected void loadLastSavedMemorySettings()
  {
    customiseMemorySetting.setSelected(
            Cache.getDefault(MemorySetting.CUSTOMISED_SETTINGS, false));
    jvmMemoryPercentSlider
            .setValue(Cache.getDefault(MemorySetting.MEMORY_JVMMEMPC, 90));
    jvmMemoryMaxTextField.setText(
            Cache.getDefault(MemorySetting.MEMORY_JVMMEMMAX, "32g"));
  }

  private boolean warnAboutSuffixReverseChange()
  {
    BackupFilesPresetEntry bfpe = BackupFilesPresetEntry
            .getSavedBackupEntry();
    boolean savedSuffixReverse = bfpe.reverse;
    int savedSuffixDigits = bfpe.digits;
    String savedSuffixTemplate = bfpe.suffix;

    boolean nowSuffixReverse = suffixReverse.isSelected();
    int nowSuffixDigits = getSpinnerInt(suffixDigitsSpinner, 3);
    String nowSuffixTemplate = suffixTemplate.getText();
    return nowSuffixReverse != savedSuffixReverse
            && nowSuffixDigits == savedSuffixDigits
            && nowSuffixTemplate != null
            && nowSuffixTemplate.equals(savedSuffixTemplate);
  }

  /* Initialises the Startup tabbed panel.
   * 
   * @return
   * */

  private JPanel initStartupTab()
  {
    JPanel startupTab = new JPanel();
    startupTab.setBorder(
            new TitledBorder(MessageManager.getString("label.memory")));
    startupTab.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    gbc.fill = GridBagConstraints.NONE;

    initMemoryPanel();

    gbc.gridheight = 1;
    gbc.gridwidth = 3;

    gbc.gridy = 0; // row 1
    gbc.gridx = 0;
    JLabel memoryText = new JLabel();
    memoryText.setFont(LABEL_FONT_ITALIC);
    memoryText
            .setText(MessageManager.getString("label.memory_setting_text"));
    startupTab.add(memoryText, gbc);

    gbc.gridy++; // row 2
    gbc.gridx = 0;
    JPanel exampleMemoryPanel = new JPanel();
    exampleMemoryPanel
            .setLayout(new BoxLayout(exampleMemoryPanel, BoxLayout.Y_AXIS));
    exampleMemoryPanel.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.memory_example_tooltip")));
    JLabel exampleTextLabel = new JLabel();
    exampleTextLabel
            .setText(MessageManager.getString("label.memory_example_text"));
    exampleTextLabel.setForeground(Color.GRAY);
    exampleTextLabel.setFont(LABEL_FONT);
    exampleMemoryPanel.add(exampleTextLabel);
    exampleMemoryPanel.add(exampleMemoryLabel);
    exampleMemoryPanel.setBackground(Color.WHITE);
    exampleMemoryPanel.setBorder(BorderFactory.createEtchedBorder());
    startupTab.add(exampleMemoryPanel, gbc);

    gbc.gridy++; // row 3
    gbc.gridx = 0;
    startupTab.add(customiseMemorySetting, gbc);

    gbc.gridy += 2; // row 4 with a gap
    gbc.gridx = 0;
    startupTab.add(maxMemoryLabel, gbc);

    gbc.gridy += 2; // row 5
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    startupTab.add(jvmMemoryPercentLabel, gbc);
    gbc.gridx++;
    startupTab.add(jvmMemoryPercentSlider, gbc);
    gbc.gridx++;
    // gbc.weightx = 0.1;
    startupTab.add(jvmMemoryPercentDisplay, gbc);
    // gbc.weightx = 1.0;
    gbc.gridwidth = 3;

    gbc.gridy++; // row 6
    gbc.gridx = 0;
    startupTab.add(jvmMemoryMaxLabel, gbc);
    gbc.gridx++;
    startupTab.add(jvmMemoryMaxTextField, gbc);

    gbc.gridy++; // row 7
    gbc.gridx = 0;
    gbc.gridwidth = 4;
    exampleMemoryMessageTextArea.setBackground(startupTab.getBackground());
    JScrollPane sp = new JScrollPane(exampleMemoryMessageTextArea);
    sp.setBorder(BorderFactory.createEmptyBorder());
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    startupTab.add(sp, gbc);

    // fill empty space to push widget to top left
    gbc.gridy++;
    gbc.weighty = 1.0;
    gbc.gridx = 100;
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    startupTab.add(new JPanel(), gbc);

    setMemoryPercentDisplay();
    memoryOptionsSetEnabled();
    return startupTab;
  }

  private void initMemoryPanel()
  {
    // Enable memory settings checkbox
    customiseMemorySetting.setFont(LABEL_FONT_BOLD);
    customiseMemorySetting.setText(
            MessageManager.getString("label.customise_memory_settings"));
    customiseMemorySetting.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        memoryOptionsSetEnabled();
      }
    });

    loadLastSavedMemorySettings();

    exampleMemoryLabel.setFont(LABEL_FONT_BOLD);
    exampleMemoryLabel.setBackground(Color.WHITE);

    maxMemoryLabel = new JLabel(
            MessageManager.getString("label.maximum_memory_used"));
    maxMemoryLabel.setFont(LABEL_FONT_BOLD);

    // Maximum memory percentage slider
    jvmMemoryPercentLabel.setFont(LABEL_FONT);
    jvmMemoryPercentLabel.setText(
            MessageManager.getString("label.percent_of_physical_memory"));
    jvmMemoryPercentSlider.setPaintLabels(true);
    jvmMemoryPercentSlider.setPaintTicks(true);
    jvmMemoryPercentSlider.setPaintTrack(true);
    jvmMemoryPercentSlider.setMajorTickSpacing(50);
    jvmMemoryPercentSlider.setMinorTickSpacing(10);
    jvmMemoryPercentSlider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent e)
      {
        setMemoryPercentDisplay();
      }
    });
    jvmMemoryPercentDisplay.setFont(LABEL_FONT);
    setMemoryPercentDisplay();

    // Maximum memory cap textbox
    jvmMemoryMaxLabel.setFont(LABEL_FONT);
    jvmMemoryMaxLabel
            .setText(MessageManager.getString("label.maximum_memory"));
    initMemoryMaxTextField();

    exampleMemoryMessageTextArea.setFont(LABEL_FONT_ITALIC);
    exampleMemoryMessageTextArea.setForeground(Color.GRAY);
    exampleMemoryMessageTextArea.setEditable(false);
    exampleMemoryMessageTextArea.setLineWrap(true);
    exampleMemoryMessageTextArea.setWrapStyleWord(true);
    exampleMemoryMessageTextArea.setText(" ");
    exampleMemoryMessageTextArea.setRows(2);
    exampleMemoryMessageTextArea.setColumns(40);

    setExampleMemoryLabel();
  }

  private void initMemoryMaxTextField()
  {
    jvmMemoryMaxTextField.setToolTipText(
            MessageManager.getString("label.maximum_memory_tooltip"));
    jvmMemoryMaxTextField.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        validateMemoryMaxTextField();
        setExampleMemoryLabel();
      }
    });

    jvmMemoryMaxTextField.addKeyListener(new KeyListener()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        validateMemoryMaxTextField();
        setExampleMemoryLabel();
      }

      @Override
      public void keyPressed(KeyEvent e)
      {
      }

      // try and stop invalid typing
      @Override
      public void keyTyped(KeyEvent e)
      {
        char c = Character.toLowerCase(e.getKeyChar());
        String text = jvmMemoryMaxTextField.getText();
        String suffixes = "tgmkb";
        int pos = jvmMemoryMaxTextField.getCaretPosition();
        int suffixPos = StringUtils.firstCharPosIgnoreCase(text, suffixes);
        if (!((('0' <= c && c <= '9')
                && (suffixPos == -1 || pos <= suffixPos)) // digits only allowed
                // before suffix
                || (suffixes.indexOf(Character.toLowerCase(c)) >= 0 // valid
                                                                    // suffix
                        && pos == text.length() // at end of text and
                        && suffixPos == -1) // there isn't already one
        ))
        {
          // don't process
          e.consume();
        }
      }
    });
  }

  private boolean isMemoryMaxTextFieldValid()
  {
    return MemorySetting
            .isValidMemoryString(jvmMemoryMaxTextField.getText());
  }

  private void validateMemoryMaxTextField()
  {
    if (isMemoryMaxTextFieldValid())
    {
      jvmMemoryMaxTextField.setBackground(Color.WHITE);
    }
    else
    {
      jvmMemoryMaxTextField.setBackground(Color.PINK);
    }
  }

  private void setMemoryPercentDisplay()
  {
    jvmMemoryPercentDisplay
            .setText(jvmMemoryPercentSlider.getValue() + "%");
    setExampleMemoryLabel();
  }

  private void setExampleMemoryLabel()
  {
    boolean selected = customiseMemorySetting.isSelected();
    int jvmmempc = jvmMemoryPercentSlider.getValue();
    String jvmmemmax = jvmMemoryMaxTextField.getText();

    long mem;
    if (selected && (0 <= jvmmempc && jvmmempc <= 100)
            && MemorySetting.isValidMemoryString(jvmmemmax))
    {
      mem = MemorySetting.getMemorySetting(jvmmemmax,
              String.valueOf(jvmmempc), false, true);
    }
    else
    {
      mem = MemorySetting.getMemorySetting(null, null, false, true);
    }
    exampleMemoryLabel.setText(MemorySetting.memoryLongToString(mem));
    String message = MemorySetting.getAdjustmentMessage();
    exampleMemoryMessageTextArea.setText(
            MessageManager.getString("label.adjustments_for_this_computer")
                    + ": "
                    + (message == null
                            ? MessageManager.getString("label.none")
                            : message));
  }

  private void memoryOptionsSetEnabled()
  {
    boolean enabled = customiseMemorySetting.isSelected();
    // leave exampleMemoryLabel enabled always
    maxMemoryLabel.setEnabled(enabled);
    jvmMemoryPercentLabel.setEnabled(enabled);
    jvmMemoryPercentSlider.setEnabled(enabled);
    jvmMemoryPercentDisplay.setEnabled(enabled);
    jvmMemoryMaxLabel.setEnabled(enabled);
    jvmMemoryMaxTextField.setEnabled(enabled);
    exampleMemoryMessageTextArea.setEnabled(enabled);
    setExampleMemoryLabel();
  }

  /**
   * Initialises the Backups tabbed panel.
   * 
   * @return
   */
  private JPanel initBackupsTab()
  {
    JPanel backupsTab = new JPanel();
    backupsTab.setBorder(new TitledBorder(
            MessageManager.getString("label.backup_files")));
    backupsTab.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    gbc.fill = GridBagConstraints.NONE;

    initBackupsTabPresetsPanel();
    initBackupsTabSuffixPanel();
    initBackupsTabKeepFilesPanel();
    initBackupsTabFilenameExamplesPanel();

    enableBackupFiles.setFont(LABEL_FONT_BOLD);
    enableBackupFiles
            .setText(MessageManager.getString("label.enable_backupfiles"));
    enableBackupFiles.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        // enable other options only when the first is checked
        backupsOptionsSetEnabled();
      }
    });

    // enable checkbox 1 col
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.gridx = 0;
    gbc.gridy = 0; // row 0
    backupsTab.add(enableBackupFiles, gbc);

    // summary of scheme box (over two rows)
    gbc.gridx = 1;
    gbc.weightx = 0.0;
    gbc.gridheight = 2;
    gbc.anchor = GridBagConstraints.FIRST_LINE_END;
    gbc.fill = GridBagConstraints.BOTH;
    backupsTab.add(exampleFilesPanel, gbc);
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    gbc.fill = GridBagConstraints.NONE;

    // fill empty space on right
    gbc.gridx++;
    gbc.weightx = 1.0;
    backupsTab.add(new JPanel(), gbc);

    // schemes box
    gbc.weightx = 0.0;
    gbc.gridx = 0;
    gbc.gridy++; // row 1
    backupsTab.add(presetsPanel, gbc);

    // now using whole row
    gbc.gridwidth = 2;
    gbc.gridheight = 1;
    // keep files box
    gbc.gridx = 0;
    gbc.gridy++; // row 2
    backupsTab.add(keepfilesPanel, gbc);

    // filename strategy box
    gbc.gridy++; // row 3
    backupsTab.add(suffixPanel, gbc);

    // fill empty space
    gbc.gridy++; // row 4
    gbc.weighty = 1.0;
    backupsTab.add(new JPanel(), gbc);

    backupsOptionsSetEnabled();
    return backupsTab;
  }

  private JPanel initBackupsTabPresetsPanel()
  {

    String title = MessageManager.getString("label.schemes");

    presetsPanel.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.anchor = GridBagConstraints.BASELINE_LEADING;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;

    // "Scheme: "
    gbc.gridx = 0;
    gbc.gridy = 0;

    presetsComboLabel = new JLabel(title + ":");
    presetsPanel.add(presetsComboLabel, gbc);

    List<Object> entries = Arrays.asList(
            (Object[]) BackupFilesPresetEntry.backupfilesPresetEntries);
    List<String> tooltips = Arrays.asList(
            BackupFilesPresetEntry.backupfilesPresetEntryDescriptions);
    backupfilesPresetsCombo = JvSwingUtils.buildComboWithTooltips(entries,
            tooltips);
    /*
    for (int i = 0; i < BackupFilesPresetEntry.backupfilesPresetEntries.length; i++)
    {
      backupfilesPresetsCombo
              .addItem(BackupFilesPresetEntry.backupfilesPresetEntries[i]);
    }
    */

    backupfilesPresetsCombo.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        int key = getComboIntStringKey(backupfilesPresetsCombo);
        if (!customiseCheckbox.isSelected())
        {
          backupfilesPresetsComboLastSelected = key;
        }
        if (key == BackupFilesPresetEntry.BACKUPFILESSCHEMECUSTOM)
        {
          if (customiseCheckbox.isSelected())
          {
            // got here by clicking on customiseCheckbox so don't change the
            // values
            backupfilesCustomOptionsSetEnabled();
          }
          else
          {
            backupsTabUpdatePresets();
            backupfilesCustomOptionsSetEnabled();
          }
        }
        else
        {
          customiseCheckbox.setSelected(false);
          backupsTabUpdatePresets();
          backupfilesCustomOptionsSetEnabled();
        }
      }
    });

    // dropdown list of preset schemes
    gbc.gridx = 1;
    presetsPanel.add(backupfilesPresetsCombo, gbc);

    revertButton.setText(MessageManager.getString("label.cancel_changes"));
    revertButton.setToolTipText(
            MessageManager.getString("label.cancel_changes_description"));
    revertButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        backupsSetOptions(
                BackupFilesPresetEntry.backupfilesPresetEntriesValues.get(
                        BackupFilesPresetEntry.BACKUPFILESSCHEMECUSTOM));
        backupfilesCustomOptionsSetEnabled();
      }

    });
    revertButton.setFont(LABEL_FONT);

    customiseCheckbox.setFont(LABEL_FONT);
    customiseCheckbox.setText(MessageManager.getString("label.customise"));
    customiseCheckbox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        int currently = getComboIntStringKey(backupfilesPresetsCombo);
        if (customiseCheckbox.isSelected())
        {
          backupfilesPresetsComboLastSelected = currently;
          setComboIntStringKey(backupfilesPresetsCombo,
                  BackupFilesPresetEntry.BACKUPFILESSCHEMECUSTOM);
        }
        else
        {
          setComboIntStringKey(backupfilesPresetsCombo,
                  backupfilesPresetsComboLastSelected);

        }
        backupfilesCustomOptionsSetEnabled();
      }
    });
    customiseCheckbox.setToolTipText(
            MessageManager.getString("label.customise_description"));

    // customise checkbox
    gbc.gridx = 0;
    gbc.gridy++;
    presetsPanel.add(customiseCheckbox, gbc);

    // "Cancel changes" button (aligned with combo box above)
    gbc.gridx = 1;
    presetsPanel.add(revertButton, gbc);

    return presetsPanel;
  }

  private JPanel initBackupsTabFilenameExamplesPanel()
  {
    String title = MessageManager.getString("label.scheme_examples");
    TitledBorder tb = new TitledBorder(title);
    exampleFilesPanel.setBorder(tb);
    exampleFilesPanel.setLayout(new GridBagLayout());

    backupfilesExampleLabel.setEditable(false);
    backupfilesExampleLabel
            .setBackground(exampleFilesPanel.getBackground());

    updateBackupFilesExampleLabel();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;

    exampleFilesPanel.add(backupfilesExampleLabel, gbc);
    return exampleFilesPanel;
  }

  private void backupsTabUpdatePresets()
  {
    IntKeyStringValueEntry entry = (IntKeyStringValueEntry) backupfilesPresetsCombo
            .getSelectedItem();
    int key = entry.k;
    String value = entry.v;

    if (BackupFilesPresetEntry.backupfilesPresetEntriesValues
            .containsKey(key))
    {
      backupsSetOptions(
              BackupFilesPresetEntry.backupfilesPresetEntriesValues
                      .get(key));
    }
    else
    {
      Console.error(
              "Preset '" + value + "' [key:" + key + "] not implemented");
    }

    // Custom options will now be enabled when the customiseCheckbox is checked
    // (performed above)
    // backupfilesCustomOptionsSetEnabled();
    updateBackupFilesExampleLabel();
  }

  protected int getComboIntStringKey(
          JComboBox<Object> backupfilesPresetsCombo2)
  {
    IntKeyStringValueEntry e;
    try
    {
      e = (IntKeyStringValueEntry) backupfilesPresetsCombo2
              .getSelectedItem();
    } catch (Exception ex)
    {
      Console.error(
              "Problem casting Combo entry to IntKeyStringValueEntry.");
      e = null;
    }
    return e != null ? e.k : 0;
  }

  protected void setComboIntStringKey(
          JComboBox<Object> backupfilesPresetsCombo2, int key)
  {
    for (int i = 0; i < backupfilesPresetsCombo2.getItemCount(); i++)
    {
      IntKeyStringValueEntry e;
      try
      {
        e = (IntKeyStringValueEntry) backupfilesPresetsCombo2.getItemAt(i);
      } catch (Exception ex)
      {
        Console.error(
                "Problem casting Combo entry to IntKeyStringValueEntry. Skipping item. ");
        continue;
      }
      if (e.k == key)
      {
        backupfilesPresetsCombo2.setSelectedIndex(i);
        break;
      }
    }
    // backupsTabUpdatePresets();
  }

  private JPanel initBackupsTabSuffixPanel()
  {
    suffixPanel.setBorder(new TitledBorder(
            MessageManager.getString("label.backup_filename_strategy")));
    suffixPanel.setLayout(new GridBagLayout());

    suffixTemplateLabel
            .setText(MessageManager.getString("label.append_to_filename"));
    suffixTemplateLabel.setHorizontalAlignment(SwingConstants.LEFT);
    suffixTemplateLabel.setFont(LABEL_FONT);

    final String tooltip = JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.append_to_filename_tooltip"));
    suffixTemplate.setToolTipText(tooltip);
    suffixTemplate.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        updateBackupFilesExampleLabel();
        backupfilesCustomOptionsSetEnabled();
        backupfilesRevertButtonSetEnabled(true);
      }

    });
    suffixTemplate.addKeyListener(new KeyListener()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        updateBackupFilesExampleLabel();
        backupfilesCustomOptionsSetEnabled();
        backupfilesRevertButtonSetEnabled(true);
      }

      @Override
      public void keyPressed(KeyEvent e)
      {
      }

      // disable use of ':' or '/' or '\'
      @Override
      public void keyTyped(KeyEvent e)
      {
        char c = e.getKeyChar();
        if (c == ':' || c == '/' || c == '\\')
        {
          // don't process ':' or '/' or '\'
          e.consume();
        }
      }

    });

    // digits spinner
    suffixDigitsLabel
            .setText(MessageManager.getString("label.index_digits"));
    suffixDigitsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    suffixDigitsLabel.setFont(LABEL_FONT);
    ChangeListener c = new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent e)
      {
        backupfilesRevertButtonSetEnabled(true);
        updateBackupFilesExampleLabel();
      }

    };
    setIntegerSpinner(suffixDigitsSpinner, BackupFilesPresetEntry.DIGITSMIN,
            BackupFilesPresetEntry.DIGITSMAX, 3, c);

    suffixReverse.setLabels(MessageManager.getString("label.reverse_roll"),
            MessageManager.getString("label.increment_index"));
    suffixReverse.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        boolean okay = true;
        if (warnAboutSuffixReverseChange())
        {
          // Warning popup
          okay = confirmSuffixReverseChange();
        }
        if (okay)
        {
          backupfilesRevertButtonSetEnabled(true);
          updateBackupFilesExampleLabel();
        }
        else
        {
          boolean savedSuffixReverse = BackupFilesPresetEntry
                  .getSavedBackupEntry().reverse;
          suffixReverse.setSelected(savedSuffixReverse);
        }
      }
    });

    GridBagConstraints sgbc = new GridBagConstraints();

    // first row (template text box)
    sgbc.anchor = GridBagConstraints.WEST;
    sgbc.gridx = 0;
    sgbc.gridy = 0;
    sgbc.gridwidth = 1;
    sgbc.gridheight = 1;
    sgbc.weightx = 1.0;
    sgbc.weighty = 0.0;
    sgbc.fill = GridBagConstraints.NONE;
    suffixPanel.add(suffixTemplateLabel, sgbc);

    sgbc.gridx = 1;
    sgbc.fill = GridBagConstraints.HORIZONTAL;
    suffixPanel.add(suffixTemplate, sgbc);

    // second row (number of digits spinner)
    sgbc.gridy = 1;

    sgbc.gridx = 0;
    sgbc.fill = GridBagConstraints.NONE;
    suffixPanel.add(suffixDigitsLabel, sgbc);

    sgbc.gridx = 1;
    sgbc.fill = GridBagConstraints.HORIZONTAL;
    suffixPanel.add(suffixDigitsSpinner, sgbc);

    // third row (forward order radio selection)
    sgbc.gridx = 0;
    sgbc.gridy = 2;
    sgbc.gridwidth = GridBagConstraints.REMAINDER;
    sgbc.fill = GridBagConstraints.HORIZONTAL;
    suffixPanel.add(suffixReverse.getFalseButton(), sgbc);

    // fourth row (reverse order radio selection)
    sgbc.gridy = 3;
    suffixPanel.add(suffixReverse.getTrueButton(), sgbc);
    return suffixPanel;
  }

  private boolean confirmSuffixReverseChange()
  {
    boolean ret = false;
    String warningMessage = MessageManager
            .getString("label.warning_confirm_change_reverse");
    int confirm = JvOptionPane.showConfirmDialog(Desktop.desktop,
            warningMessage,
            MessageManager.getString("label.change_increment_decrement"),
            JvOptionPane.YES_NO_OPTION, JvOptionPane.WARNING_MESSAGE);

    ret = (confirm == JvOptionPane.YES_OPTION);
    return ret;
  }

  private JPanel initBackupsTabKeepFilesPanel()
  {
    keepfilesPanel.setBorder(
            new TitledBorder(MessageManager.getString("label.keep_files")));
    keepfilesPanel.setLayout(new GridBagLayout());

    backupfilesKeepAll.setLabels(
            MessageManager.getString("label.keep_all_backup_files"),
            MessageManager.getString(
                    "label.keep_only_this_number_of_backup_files"));
    backupfilesKeepAll.addTrueActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        backupfilesRevertButtonSetEnabled(true);
        updateBackupFilesExampleLabel();
      }
    });
    backupfilesKeepAll.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        backupfilesRevertButtonSetEnabled(true);
        keepRollMaxOptionsEnabled();
        updateBackupFilesExampleLabel();
      }
    });

    ChangeListener c = new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent e)
      {
        backupfilesRevertButtonSetEnabled(true);
        updateBackupFilesExampleLabel();
      }

    };
    setIntegerSpinner(backupfilesRollMaxSpinner,
            BackupFilesPresetEntry.ROLLMAXMIN,
            BackupFilesPresetEntry.ROLLMAXMAX, 4, true, c);

    backupfilesConfirmDelete.setLabels(
            MessageManager.getString("label.always_ask"),
            MessageManager.getString("label.auto_delete"));
    backupfilesConfirmDelete.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        backupfilesRevertButtonSetEnabled(true);
      }
    });
    // update the enabled section
    keepRollMaxOptionsEnabled();

    GridBagConstraints kgbc = new GridBagConstraints();

    // first row (template text box)
    kgbc.anchor = GridBagConstraints.WEST;
    kgbc.gridx = 0;
    kgbc.gridy = 0;
    kgbc.gridwidth = GridBagConstraints.REMAINDER;
    kgbc.gridheight = 1;
    kgbc.weightx = 1.0;
    kgbc.weighty = 0.0;
    kgbc.fill = GridBagConstraints.HORIZONTAL;
    keepfilesPanel.add(backupfilesKeepAll.getTrueButton(), kgbc);

    // second row
    kgbc.gridy = 1;

    kgbc.gridx = 0;
    kgbc.gridwidth = GridBagConstraints.RELATIVE;
    keepfilesPanel.add(backupfilesKeepAll.getFalseButton(), kgbc);

    kgbc.gridx = 1;
    kgbc.gridwidth = GridBagConstraints.REMAINDER;
    keepfilesPanel.add(backupfilesRollMaxSpinner, kgbc);

    // third row (indented)
    kgbc.gridy = 2;
    kgbc.insets = new Insets(0, 20, 0, 0);

    kgbc.gridx = 0;
    kgbc.gridwidth = GridBagConstraints.REMAINDER;
    kgbc.fill = GridBagConstraints.HORIZONTAL;
    kgbc.weightx = 1.0;

    JPanel jp = new JPanel();
    jp.setLayout(new FlowLayout());
    oldBackupFilesLabel.setText(
            MessageManager.getString("label.autodelete_old_backup_files"));
    oldBackupFilesLabel.setFont(LABEL_FONT);
    oldBackupFilesLabel.setHorizontalAlignment(SwingConstants.LEFT);
    jp.add(oldBackupFilesLabel);
    jp.add(backupfilesConfirmDelete.getTrueButton());
    jp.add(backupfilesConfirmDelete.getFalseButton());
    keepfilesPanel.add(jp, kgbc);

    return keepfilesPanel;
  }

  protected void updateBackupFilesExampleLabel()
  {
    int exampleindex = 12;
    String base = MessageManager.getString("label.filename") + ".fa";
    if (base == null || base.length() == 0)
    {
      base = "file_name.fa";
    }

    boolean reverse = suffixReverse.isSelected();
    boolean keepAll = backupfilesKeepAll.isSelected();
    int rollMax = 4;
    String suffix = suffixTemplate.getText();
    int digits = 3;

    backupfilesExampleLabel.setFont(LABEL_FONT_ITALIC);
    if (suffix == null || suffix.length() == 0)
    {
      backupfilesExampleLabel
              .setText(MessageManager.getString("label.no_backup_files"));
      backupfilesExampleLabel.setFont(LABEL_FONT_BOLD);
      return;
    }

    rollMax = getSpinnerInt(backupfilesRollMaxSpinner, 4);
    rollMax = rollMax < 1 ? 1 : rollMax;

    if (suffix.indexOf(BackupFiles.NUM_PLACEHOLDER) == -1)
    {
      rollMax = 1;
    }

    digits = getSpinnerInt(suffixDigitsSpinner, 3);
    digits = digits < 1 ? 1 : digits;

    int lowersurround = 2;
    int uppersurround = 0;
    StringBuilder exampleSB = new StringBuilder();
    boolean firstLine = true;
    int lineNumber = 0;
    if (reverse)
    {

      int min = 1;
      int max = keepAll ? exampleindex : rollMax;
      for (int index = min; index <= max; index++)
      {
        if (index == min + lowersurround && index < max - uppersurround - 1)
        {
          exampleSB.append("\n...");
          lineNumber++;
        }
        else if (index > min + lowersurround && index < max - uppersurround)
        {
          // nothing
        }
        else
        {
          if (firstLine)
          {
            firstLine = false;
          }
          else
          {
            exampleSB.append("\n");
            lineNumber++;
          }
          exampleSB.append(BackupFilenameParts.getBackupFilename(index,
                  base, suffix, digits));
          if (min == max)
          {
            // no extra text needed
          }
          else if (index == min)
          {
            String newest = MessageManager.getString("label.braced_newest");
            if (newest != null && newest.length() > 0)
            {
              exampleSB.append(" " + newest);
            }
          }
          else if (index == max)
          {
            String oldest = MessageManager.getString("label.braced_oldest");
            if (oldest != null && oldest.length() > 0)
            {
              exampleSB.append(" " + oldest);
            }
          }
        }
      }
    }
    else
    {

      int min = (keepAll || exampleindex - rollMax < 0) ? 1
              : exampleindex - rollMax + 1;
      int max = exampleindex;

      for (int index = min; index <= max; index++)
      {

        if (index == min + lowersurround && index < max - uppersurround - 1)
        {
          exampleSB.append("\n...");
          lineNumber++;
        }
        else if (index > min + lowersurround && index < max - uppersurround)
        {
          // nothing
        }
        else
        {
          if (firstLine)
          {
            firstLine = false;
          }
          else
          {
            exampleSB.append("\n");
            lineNumber++;
          }
          exampleSB.append(BackupFilenameParts.getBackupFilename(index,
                  base, suffix, digits));
          if (min == max)
          {
            // no extra text needed
          }
          else if (index == min)
          {
            String oldest = MessageManager.getString("label.braced_oldest");
            if (oldest != null && oldest.length() > 0)
            {
              exampleSB.append(" " + oldest);
            }
          }
          else if (index == max)
          {
            String newest = MessageManager.getString("label.braced_newest");
            if (newest != null && newest.length() > 0)
            {
              exampleSB.append(" " + newest);
            }
          }
        }
      }

    }

    // add some extra empty lines to pad out the example files box. ugh, please
    // tell
    // me how to do this better
    int remainingLines = lowersurround + uppersurround + 1 - lineNumber;
    if (remainingLines > 0)
    {
      for (int i = 0; i < remainingLines; i++)
      {
        exampleSB.append("\n ");
        lineNumber++;
      }
    }

    backupfilesExampleLabel.setText(exampleSB.toString());
  }

  protected void setIntegerSpinner(JSpinner s, int min, int max, int def,
          boolean useExistingVal, ChangeListener c)
  {
    int i = def;
    if (useExistingVal)
    {
      try
      {
        i = ((Integer) s.getValue()).intValue();
      } catch (Exception e)
      {
        Console.error(
                "Exception casting the initial value of s.getValue()");
      }
    }

    setIntegerSpinner(s, min, max, i, c);
  }

  protected void setIntegerSpinner(JSpinner s, int min, int max, int def,
          ChangeListener c)
  {
    // integer spinner for number of digits
    if (def > max)
    {
      max = def;
    }
    if (def < min)
    {
      def = min;
    }
    SpinnerModel sModel = new SpinnerNumberModel(def, min, max, 1);
    s.setModel(sModel);

    s.addChangeListener(c);

  }

  protected static int getSpinnerInt(JSpinner s, int def)
  {
    int i = def;
    try
    {
      s.commitEdit();
      i = (Integer) s.getValue();
    } catch (Exception e)
    {
      Console.error("Failed casting (Integer) JSpinner s.getValue()");
    }
    return i;
  }

  private void keepRollMaxOptionsEnabled()
  {
    boolean enabled = backupfilesKeepAll.isEnabled()
            && !backupfilesKeepAll.isSelected();
    oldBackupFilesLabel.setEnabled(enabled);
    backupfilesRollMaxSpinner.setEnabled(enabled);
    backupfilesConfirmDelete.setEnabled(enabled);
  }

  private void backupfilesKeepAllSetEnabled(boolean tryEnabled)
  {
    boolean enabled = tryEnabled && enableBackupFiles.isSelected()
            && customiseCheckbox.isSelected() && suffixTemplate.getText()
                    .indexOf(BackupFiles.NUM_PLACEHOLDER) > -1;
    keepfilesPanel.setEnabled(enabled);
    backupfilesKeepAll.setEnabled(enabled);
    oldBackupFilesLabel.setEnabled(enabled);
    keepRollMaxOptionsEnabled();
  }

  private void backupfilesSuffixTemplateDigitsSetEnabled()
  {
    boolean enabled = suffixTemplate.isEnabled() && suffixTemplate.getText()
            .indexOf(BackupFiles.NUM_PLACEHOLDER) > -1;
    suffixDigitsLabel.setEnabled(enabled);
    suffixDigitsSpinner.setEnabled(enabled);
    suffixReverse.setEnabled(enabled);
  }

  private void backupfilesSuffixTemplateSetEnabled(boolean tryEnabled)
  {
    boolean enabled = tryEnabled && enableBackupFiles.isSelected()
            && customiseCheckbox.isSelected();
    suffixPanel.setEnabled(enabled);
    suffixTemplateLabel.setEnabled(enabled);
    suffixTemplate.setEnabled(enabled);
    backupfilesSuffixTemplateDigitsSetEnabled();
  }

  private void backupfilesRevertButtonSetEnabled(boolean tryEnabled)
  {
    boolean enabled = tryEnabled && enableBackupFiles.isSelected()
            && customiseCheckbox.isSelected() && backupfilesCustomChanged();
    revertButton.setEnabled(enabled);
  }

  private boolean backupfilesCustomChanged()
  {
    BackupFilesPresetEntry custom = BackupFilesPresetEntry.backupfilesPresetEntriesValues
            .get(BackupFilesPresetEntry.BACKUPFILESSCHEMECUSTOM);
    BackupFilesPresetEntry current = getBackupfilesCurrentEntry();
    return !custom.equals(current);
  }

  protected BackupFilesPresetEntry getBackupfilesCurrentEntry()
  {
    String suffix = suffixTemplate.getText();
    int digits = getSpinnerInt(suffixDigitsSpinner, 3);
    boolean reverse = suffixReverse.isSelected();
    boolean keepAll = backupfilesKeepAll.isSelected();
    int rollMax = getSpinnerInt(backupfilesRollMaxSpinner, 3);
    boolean confirmDelete = backupfilesConfirmDelete.isSelected();

    BackupFilesPresetEntry bfpe = new BackupFilesPresetEntry(suffix, digits,
            reverse, keepAll, rollMax, confirmDelete);

    return bfpe;
  }

  protected void backupfilesCustomOptionsSetEnabled()
  {
    boolean enabled = customiseCheckbox.isSelected();

    backupfilesRevertButtonSetEnabled(enabled);
    backupfilesSuffixTemplateSetEnabled(enabled);
    backupfilesKeepAllSetEnabled(enabled);
  }

  private void backupfilesSummarySetEnabled()
  {
    boolean enabled = enableBackupFiles.isSelected();
    backupfilesExampleLabel.setEnabled(enabled);
    exampleFilesPanel.setEnabled(enabled);
  }

  private void backupfilesPresetsSetEnabled()
  {
    boolean enabled = enableBackupFiles.isSelected();
    presetsPanel.setEnabled(enabled);
    presetsComboLabel.setEnabled(enabled);
    backupfilesPresetsCombo.setEnabled(enabled);
    customiseCheckbox.setEnabled(enabled);
    revertButton.setEnabled(enabled);
  }

  protected void backupsOptionsSetEnabled()
  {
    backupfilesPresetsSetEnabled();
    backupfilesSummarySetEnabled();
    backupfilesCustomOptionsSetEnabled();
  }

  protected void backupsSetOptions(String suffix, int digits,
          boolean reverse, boolean keepAll, int rollMax,
          boolean confirmDelete)
  {
    suffixTemplate.setText(suffix);
    suffixDigitsSpinner.setValue(digits);
    suffixReverse.setSelected(reverse);
    backupfilesKeepAll.setSelected(keepAll);
    backupfilesRollMaxSpinner.setValue(rollMax);
    backupfilesConfirmDelete.setSelected(confirmDelete);
  }

  protected void backupsSetOptions(BackupFilesPresetEntry p)
  {
    backupsSetOptions(p.suffix, p.digits, p.reverse, p.keepAll, p.rollMax,
            p.confirmDelete);
  }

  protected void autoIdWidth_actionPerformed()
  {
    // TODO Auto-generated method stub

  }

  protected void userIdWidth_actionPerformed()
  {
    // TODO Auto-generated method stub

  }

  protected void maxColour_actionPerformed(JPanel panel)
  {
  }

  protected void minColour_actionPerformed(JPanel panel)
  {
  }

  protected void gapColour_actionPerformed(JPanel panel)
  {
  }

  protected void hiddenColour_actionPerformed(JPanel panel)
  {
  }

  protected void showunconserved_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void useLegacyGaps_actionPerformed(ActionEvent e)
  {
  }

  protected void resetOvDefaults_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void annotations_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   */
  public void startupFileTextfield_mouseClicked()
  {
  }

  public void newLink_actionPerformed(ActionEvent e)
  {

  }

  public void editLink_actionPerformed(ActionEvent e)
  {

  }

  public void deleteLink_actionPerformed(ActionEvent e)
  {

  }

  public void linkURLList_keyTyped(KeyEvent e)
  {

  }

  public void setProxyAuthEnabled()
  {
    boolean enabled = proxyAuth.isSelected() && proxyAuth.isEnabled();
    proxyAuthUsernameLabel.setEnabled(enabled);
    proxyAuthPasswordLabel.setEnabled(enabled);
    passwordNotStoredLabel.setEnabled(enabled);
    proxyAuthUsernameTB.setEnabled(enabled);
    proxyAuthPasswordPB.setEnabled(enabled);
  }

  public void setCustomProxyEnabled()
  {
    boolean enabled = customProxy.isSelected();
    portLabel.setEnabled(enabled);
    serverLabel.setEnabled(enabled);
    portLabel2.setEnabled(enabled);
    serverLabel2.setEnabled(enabled);
    httpLabel.setEnabled(enabled);
    httpsLabel.setEnabled(enabled);
    proxyServerHttpTB.setEnabled(enabled);
    proxyPortHttpTB.setEnabled(enabled);
    proxyServerHttpsTB.setEnabled(enabled);
    proxyPortHttpsTB.setEnabled(enabled);
    proxyAuth.setEnabled(enabled);
    setProxyAuthEnabled();
  }

  public void proxyType_actionPerformed()
  {
    setCustomProxyEnabled();
    proxyAuthPasswordCheckHighlight(true);
    applyProxyButtonEnabled(true);
  }

  public void proxyAuth_actionPerformed()
  {
    setProxyAuthEnabled();
    proxyAuthPasswordCheckHighlight(true);
    applyProxyButtonEnabled(true);
  }

  /**
   * Customer renderer for JTable: supports column of radio buttons
   */
  public class RadioButtonRenderer extends JRadioButton
          implements TableCellRenderer
  {
    public RadioButtonRenderer()
    {
      setHorizontalAlignment(CENTER);
      setToolTipText(MessageManager.getString("label.urltooltip"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
      setSelected((boolean) value);

      // set colours to match rest of table
      if (isSelected)
      {
        setBackground(table.getSelectionBackground());
        setForeground(table.getSelectionForeground());
      }
      else
      {
        setBackground(table.getBackground());
        setForeground(table.getForeground());
      }
      return this;
    }
  }

  /**
   * Customer cell editor for JTable: supports column of radio buttons in
   * conjunction with renderer
   */
  public class RadioButtonEditor extends AbstractCellEditor
          implements TableCellEditor
  {
    private JRadioButton button = new JRadioButton();

    public RadioButtonEditor()
    {
      button.setHorizontalAlignment(SwingConstants.CENTER);
      this.button.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          fireEditingStopped();
        }
      });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column)
    {
      button.setSelected((boolean) value);
      return button;
    }

    @Override
    public Object getCellEditorValue()
    {
      return button.isSelected();
    }

  }
}
