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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.help.HelpSetException;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

//import edu.stanford.ejalbert.launching.IBrowserLaunching;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager;
import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.bin.MemorySetting;
import jalview.ext.pymol.PymolManager;
import jalview.gui.Help.HelpId;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.BackupFiles;
import jalview.io.BackupFilesPresetEntry;
import jalview.io.FileFormatI;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GPreferences;
import jalview.jbgui.GSequenceLink;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemes;
import jalview.schemes.ResidueColourScheme;
import jalview.urls.UrlLinkTableModel;
import jalview.urls.api.UrlProviderFactoryI;
import jalview.urls.api.UrlProviderI;
import jalview.urls.desktop.DesktopUrlProviderFactory;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.UrlConstants;
import jalview.ws.sifts.SiftsSettings;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
/*
 * for merge with Jalview-JS
 public class Preferences extends GPreferences implements ApplicationSingletonI
 */
public class Preferences extends GPreferences
{
  public static final String ENABLE_SPLIT_FRAME = "ENABLE_SPLIT_FRAME";

  public static final String SCALE_PROTEIN_TO_CDNA = "SCALE_PROTEIN_TO_CDNA";

  public static final String DEFAULT_COLOUR = "DEFAULT_COLOUR";

  public static final String DEFAULT_COLOUR_PROT = "DEFAULT_COLOUR_PROT";

  public static final String DEFAULT_COLOUR_NUC = "DEFAULT_COLOUR_NUC";

  public static final String ADD_TEMPFACT_ANN = "ADD_TEMPFACT_ANN";

  public static final String ADD_SS_ANN = "ADD_SS_ANN";

  public static final String USE_RNAVIEW = "USE_RNAVIEW";

  public static final String STRUCT_FROM_PDB = "STRUCT_FROM_PDB";

  public static final String STRUCTURE_DISPLAY = "STRUCTURE_DISPLAY";

  public static final String CHIMERA_PATH = "CHIMERA_PATH";

  public static final String CHIMERAX_PATH = "CHIMERAX_PATH";

  public static final String PYMOL_PATH = "PYMOL_PATH";

  public static final String SORT_ANNOTATIONS = "SORT_ANNOTATIONS";

  public static final String SHOW_AUTOCALC_ABOVE = "SHOW_AUTOCALC_ABOVE";

  public static final String SHOW_OCCUPANCY = "SHOW_OCCUPANCY";

  public static final String SHOW_OV_HIDDEN_AT_START = "SHOW_OV_HIDDEN_AT_START";

  public static final String USE_LEGACY_GAP = "USE_LEGACY_GAP";

  public static final String GAP_COLOUR = "GAP_COLOUR";

  public static final String HIDDEN_COLOUR = "HIDDEN_COLOUR";

  private static final int MIN_FONT_SIZE = 1;

  private static final int MAX_FONT_SIZE = 30;

  private String previousProxyType;

  private static Preferences INSTANCE = null; // add "final"

  /**
   * Holds name and link separated with | character. Sequence ID must be
   * $SEQUENCE_ID$ or $SEQUENCE_ID=/.possible | chars ./=$
   */
  public static UrlProviderI sequenceUrlLinks;

  public static UrlLinkTableModel dataModel;

  /**
   * Holds name and link separated with | character. Sequence IDS and Sequences
   * must be $SEQUENCEIDS$ or $SEQUENCEIDS=/.possible | chars ./=$ and
   * $SEQUENCES$ or $SEQUENCES=/.possible | chars ./=$ and separation character
   * for first and second token specified after a pipe character at end |,|.
   * (TODO: proper escape for using | to separate ids or sequences
   */

  public static List<String> groupURLLinks;
  static
  {
    // don't populate with session properties
    Cache.disableSessionProperties();

    // get links selected to be in the menu (SEQUENCE_LINKS)
    // and links entered by the user but not selected (STORED_LINKS)
    String inMenuString = Cache.getDefault("SEQUENCE_LINKS", "");
    String notInMenuString = Cache.getDefault("STORED_LINKS", "");
    String defaultUrl = Cache.getDefault("DEFAULT_URL",
            UrlConstants.DEFAULT_LABEL);

    // if both links lists are empty, add the DEFAULT_URL link
    // otherwise we assume the default link is in one of the lists
    if (inMenuString.isEmpty() && notInMenuString.isEmpty())
    {
      inMenuString = UrlConstants.DEFAULT_STRING;
    }
    UrlProviderFactoryI factory = new DesktopUrlProviderFactory(defaultUrl,
            inMenuString, notInMenuString);
    sequenceUrlLinks = factory.createUrlProvider();
    dataModel = new UrlLinkTableModel(sequenceUrlLinks);

    /**
     * TODO: reformulate groupURL encoding so two or more can be stored in the
     * .properties file as '|' separated strings
     */

    groupURLLinks = new ArrayList<>();

    // reenable
    Cache.enableSessionProperties();
  }

  JInternalFrame frame;

  private WsPreferences wsPrefs;

  private OptionsParam promptEachTimeOpt = new OptionsParam(
          MessageManager.getString("label.prompt_each_time"),
          LineartOptions.PROMPT_EACH_TIME);

  private OptionsParam lineArtOpt = new OptionsParam(
          MessageManager.getString("label.lineart"), "Lineart");

  private OptionsParam textOpt = new OptionsParam(
          MessageManager.getString("action.text"), "Text");

  // get singleton Preferences instance
  public static Preferences getInstance()
  {
    if (INSTANCE == null || INSTANCE.frame == null
            || INSTANCE.frame.isClosed())
    {
      INSTANCE = new Preferences();
    }
    return INSTANCE;

    /*
     * Replace code with the following for Jalvew-JS
    Preferences INSTANCE = ApplicationSingletonProvider.getInstance(Preferences.class);
    if (INSTANCE == null || INSTANCE.frame == null
            || INSTANCE.frame.isClosed())
    {
      ApplicationSingletonProvider.remove(Preferences.class);
      INSTANCE = ApplicationSingletonProvider.getInstance(Preferences.class);
    }
    return INSTANCE;
    */
  }

  public static void openPreferences()
  {
    openPreferences(null, null);
  }

  public static void openPreferences(TabRef selectTab, String message)
  {
    Preferences p = getInstance();
    if (selectTab != null)
      p.selectTab(selectTab, message);
    p.frame.show();
    p.frame.moveToFront();
    p.frame.grabFocus();
  }

  public void selectTab(TabRef selectTab, String message)
  {
    this.selectTab(selectTab);
    if (message != null)
      this.setMessage(message);
    this.frame.show();
  }

  /**
   * Creates a new Preferences object.
   */
  private Preferences()
  {
    super();
    // don't populate with session properties
    Cache.disableSessionProperties();

    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    if (!Platform.isJS())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      wsPrefs = new WsPreferences();
      wsTab.add(wsPrefs, BorderLayout.CENTER);
    }
    int width = 500, height = 450;
    if (Platform.isAMacAndNotJS())
    {
      width = 570;
      height = 480;
    }

    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.preferences"), width, height);
    frame.setMinimumSize(new Dimension(width, height));

    /*
     * Set Visual tab defaults
     */
    seqLimit.setSelected(Cache.getDefault("SHOW_JVSUFFIX", true));
    rightAlign.setSelected(Cache.getDefault("RIGHT_ALIGN_IDS", false));
    fullScreen.setSelected(Cache.getDefault("SHOW_FULLSCREEN", false));
    annotations.setSelected(Cache.getDefault("SHOW_ANNOTATIONS", true));

    conservation.setSelected(Cache.getDefault("SHOW_CONSERVATION", true));
    quality.setSelected(Cache.getDefault("SHOW_QUALITY", true));
    identity.setSelected(Cache.getDefault("SHOW_IDENTITY", true));
    openoverv.setSelected(Cache.getDefault("SHOW_OVERVIEW", false));
    showUnconserved
            .setSelected(Cache.getDefault("SHOW_UNCONSERVED", false));
    showOccupancy.setSelected(Cache.getDefault(SHOW_OCCUPANCY, false));
    showGroupConsensus
            .setSelected(Cache.getDefault("SHOW_GROUP_CONSENSUS", false));
    showGroupConservation.setSelected(
            Cache.getDefault("SHOW_GROUP_CONSERVATION", false));
    showConsensHistogram.setSelected(
            Cache.getDefault("SHOW_CONSENSUS_HISTOGRAM", true));
    showConsensLogo
            .setSelected(Cache.getDefault("SHOW_CONSENSUS_LOGO", false));
    showNpTooltip
            .setSelected(Cache.getDefault("SHOW_NPFEATS_TOOLTIP", true));
    showDbRefTooltip
            .setSelected(Cache.getDefault("SHOW_DBREFS_TOOLTIP", true));

    String[] fonts = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (int i = 0; i < fonts.length; i++)
    {
      fontNameCB.addItem(fonts[i]);
    }

    for (int i = MIN_FONT_SIZE; i <= MAX_FONT_SIZE; i++)
    {
      fontSizeCB.addItem(i + "");
    }

    fontStyleCB.addItem("plain");
    fontStyleCB.addItem("bold");
    fontStyleCB.addItem("italic");

    fontNameCB.setSelectedItem(Cache.getDefault("FONT_NAME", "SansSerif"));
    fontSizeCB.setSelectedItem(Cache.getDefault("FONT_SIZE", "10"));
    fontStyleCB.setSelectedItem(
            Cache.getDefault("FONT_STYLE", Font.PLAIN + ""));

    smoothFont.setSelected(Cache.getDefault("ANTI_ALIAS", true));
    scaleProteinToCdna
            .setSelected(Cache.getDefault(SCALE_PROTEIN_TO_CDNA, false));

    idItalics.setSelected(Cache.getDefault("ID_ITALICS", true));

    wrap.setSelected(Cache.getDefault("WRAP_ALIGNMENT", false));

    gapSymbolCB.addItem("-");
    gapSymbolCB.addItem(".");

    gapSymbolCB.setSelectedItem(Cache.getDefault("GAP_SYMBOL", "-"));

    sortby.addItem("No sort");
    sortby.addItem("Id");
    sortby.addItem("Pairwise Identity");
    sortby.setSelectedItem(Cache.getDefault("SORT_ALIGNMENT", "No sort"));

    sortAnnBy.addItem(SequenceAnnotationOrder.NONE.toString());
    sortAnnBy
            .addItem(SequenceAnnotationOrder.SEQUENCE_AND_LABEL.toString());
    sortAnnBy
            .addItem(SequenceAnnotationOrder.LABEL_AND_SEQUENCE.toString());
    SequenceAnnotationOrder savedSort = SequenceAnnotationOrder
            .valueOf(Cache.getDefault(SORT_ANNOTATIONS,
                    SequenceAnnotationOrder.NONE.name()));
    sortAnnBy.setSelectedItem(savedSort.toString());

    sortAutocalc.addItem("Autocalculated first");
    sortAutocalc.addItem("Autocalculated last");
    final boolean showAbove = Cache.getDefault(SHOW_AUTOCALC_ABOVE, true);
    sortAutocalc.setSelectedItem(showAbove ? sortAutocalc.getItemAt(0)
            : sortAutocalc.getItemAt(1));
    startupCheckbox
            .setSelected(Cache.getDefault("SHOW_STARTUP_FILE", true));
    startupFileTextfield.setText(Cache.getDefault("STARTUP_FILE",
            Cache.getDefault("www.jalview.org", "https://www.jalview.org")
                    + "/examples/exampleFile_2_7.jvp"));

    /*
     * Set Colours tab defaults
     */
    protColour.addItem(ResidueColourScheme.NONE);
    nucColour.addItem(ResidueColourScheme.NONE);
    for (ColourSchemeI cs : ColourSchemes.getInstance().getColourSchemes())
    {
      String name = cs.getSchemeName();
      protColour.addItem(name);
      nucColour.addItem(name);
    }
    String oldProp = Cache.getDefault(DEFAULT_COLOUR,
            ResidueColourScheme.NONE);
    String newProp = Cache.getDefault(DEFAULT_COLOUR_PROT, null);
    protColour.setSelectedItem(newProp != null ? newProp : oldProp);
    newProp = Cache.getDefault(DEFAULT_COLOUR_NUC, null);
    nucColour.setSelectedItem(newProp != null ? newProp : oldProp);
    minColour.setBackground(
            Cache.getDefaultColour("ANNOTATIONCOLOUR_MIN", Color.orange));
    maxColour.setBackground(
            Cache.getDefaultColour("ANNOTATIONCOLOUR_MAX", Color.red));

    /*
     * Set overview panel defaults
     */
    gapColour.setBackground(Cache.getDefaultColour(GAP_COLOUR,
            OverviewCanvas.OVERVIEW_DEFAULT_GAP));
    hiddenColour.setBackground(Cache.getDefaultColour(HIDDEN_COLOUR,
            OverviewCanvas.OVERVIEW_DEFAULT_HIDDEN));
    useLegacyGap.setSelected(Cache.getDefault(USE_LEGACY_GAP, false));
    gapLabel.setEnabled(!useLegacyGap.isSelected());
    gapColour.setEnabled(!useLegacyGap.isSelected());
    showHiddenAtStart
            .setSelected(Cache.getDefault(SHOW_OV_HIDDEN_AT_START, false));

    /*
     * Set Structure tab defaults
     */
    final boolean structSelected = Cache.getDefault(STRUCT_FROM_PDB, true);
    structFromPdb.setSelected(structSelected);
    addSecondaryStructure.setSelected(Cache.getDefault(ADD_SS_ANN, true));
    addSecondaryStructure.setEnabled(structSelected);
    addTempFactor.setSelected(Cache.getDefault(ADD_TEMPFACT_ANN, true));
    addTempFactor.setEnabled(structSelected);

    /*
     * set choice of structure viewer, and path if saved as a preference;
     * default to Jmol (first choice) if an unexpected value is found
     */
    String viewerType = Cache.getDefault(STRUCTURE_DISPLAY,
            ViewerType.JMOL.name());
    structViewer.setSelectedItem(viewerType);
    String viewerPath = "";
    ViewerType type = null;
    try
    {
      type = ViewerType.valueOf(viewerType);
      switch (type)
      {
      case JMOL:
        break;
      case CHIMERA:
        viewerPath = Cache.getDefault(CHIMERA_PATH, "");
        break;
      case CHIMERAX:
        viewerPath = Cache.getDefault(CHIMERAX_PATH, "");
        break;
      case PYMOL:
        viewerPath = Cache.getDefault(PYMOL_PATH, "");
        break;
      }
    } catch (IllegalArgumentException e)
    {
      Console.error("Unknown structure viewer type: " + viewerType
              + ", defaulting to Jmol");
      type = ViewerType.JMOL;
    }
    structureViewerPath.setText(viewerPath);

    structureViewerPath.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (validateViewerPath())
        {
          String path = structureViewerPath.getText();
          try
          {
            ViewerType type = ViewerType.valueOf(viewerType);
            switch (type)
            {
            case JMOL:
              break;
            case CHIMERA:
              Cache.setProperty(CHIMERA_PATH, path);
              break;
            case CHIMERAX:
              Cache.setProperty(CHIMERAX_PATH, path);
              break;
            case PYMOL:
              Cache.setProperty(PYMOL_PATH, path);
              break;
            }
          } catch (IllegalArgumentException x)
          {
            Console.error("Failed to set path - unknown viewer type", x);
          }
        }
      }
    });

    if (Cache.getDefault("MAP_WITH_SIFTS", false))
    {
      siftsMapping.setSelected(true);
    }
    else
    {
      nwMapping.setSelected(true);
    }

    SiftsSettings
            .setMapWithSifts(Cache.getDefault("MAP_WITH_SIFTS", false));

    /*
     * Set Connections tab defaults
     */

    // set up sorting
    linkUrlTable.setModel(dataModel);
    final TableRowSorter<TableModel> sorter = new TableRowSorter<>(
            linkUrlTable.getModel());
    linkUrlTable.setRowSorter(sorter);
    List<RowSorter.SortKey> sortKeys = new ArrayList<>();

    UrlLinkTableModel m = (UrlLinkTableModel) linkUrlTable.getModel();
    sortKeys.add(new RowSorter.SortKey(m.getPrimaryColumn(),
            SortOrder.DESCENDING));
    sortKeys.add(new RowSorter.SortKey(m.getSelectedColumn(),
            SortOrder.DESCENDING));
    sortKeys.add(
            new RowSorter.SortKey(m.getNameColumn(), SortOrder.ASCENDING));

    sorter.setSortKeys(sortKeys);
    // BH 2018 setSortKeys will do the sort
    // sorter.sort();

    // set up filtering
    ActionListener onReset;
    onReset = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        filterTB.setText("");
        sorter.setRowFilter(RowFilter.regexFilter(""));
      }

    };
    doReset.addActionListener(onReset);

    // filter to display only custom urls
    final RowFilter<TableModel, Object> customUrlFilter = new RowFilter<TableModel, Object>()
    {
      @Override
      public boolean include(
              Entry<? extends TableModel, ? extends Object> entry)
      {
        return ((UrlLinkTableModel) entry.getModel()).isUserEntry(entry);
      }
    };

    final TableRowSorter<TableModel> customSorter = new TableRowSorter<>(
            linkUrlTable.getModel());
    customSorter.setRowFilter(customUrlFilter);

    ActionListener onCustomOnly;
    onCustomOnly = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        filterTB.setText("");
        sorter.setRowFilter(customUrlFilter);
      }
    };
    userOnly.addActionListener(onCustomOnly);

    filterTB.getDocument().addDocumentListener(new DocumentListener()
    {
      String caseInsensitiveFlag = "(?i)";

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        sorter.setRowFilter(RowFilter
                .regexFilter(caseInsensitiveFlag + filterTB.getText()));
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        sorter.setRowFilter(RowFilter
                .regexFilter(caseInsensitiveFlag + filterTB.getText()));
      }

      @Override
      public void insertUpdate(DocumentEvent e)
      {
        sorter.setRowFilter(RowFilter
                .regexFilter(caseInsensitiveFlag + filterTB.getText()));
      }
    });

    // set up list selection functionality
    linkUrlTable.getSelectionModel()
            .addListSelectionListener(new UrlListSelectionHandler());

    // set up radio buttons
    int onClickCol = ((UrlLinkTableModel) linkUrlTable.getModel())
            .getPrimaryColumn();
    String onClickName = linkUrlTable.getColumnName(onClickCol);
    linkUrlTable.getColumn(onClickName)
            .setCellRenderer(new RadioButtonRenderer());
    linkUrlTable.getColumn(onClickName)
            .setCellEditor(new RadioButtonEditor());

    // get boolean columns and resize those to min possible
    for (int column = 0; column < linkUrlTable.getColumnCount(); column++)
    {
      if (linkUrlTable.getModel().getColumnClass(column)
              .equals(Boolean.class))
      {
        TableColumn tableColumn = linkUrlTable.getColumnModel()
                .getColumn(column);
        int preferredWidth = tableColumn.getMinWidth();

        TableCellRenderer cellRenderer = linkUrlTable.getCellRenderer(0,
                column);
        Component c = linkUrlTable.prepareRenderer(cellRenderer, 0, column);
        int cwidth = c.getPreferredSize().width
                + linkUrlTable.getIntercellSpacing().width;
        preferredWidth = Math.max(preferredWidth, cwidth);

        tableColumn.setPreferredWidth(preferredWidth);
      }
    }

    String proxyTypeString = Cache.getDefault("USE_PROXY", "false");
    previousProxyType = proxyTypeString;
    switch (proxyTypeString)
    {
    case Cache.PROXYTYPE_NONE:
      proxyType.setSelected(noProxy.getModel(), true);
      break;
    case Cache.PROXYTYPE_SYSTEM:
      proxyType.setSelected(systemProxy.getModel(), true);
      break;
    case Cache.PROXYTYPE_CUSTOM:
      proxyType.setSelected(customProxy.getModel(), true);
      break;
    default:
      Console.warn(
              "Incorrect PROXY_TYPE - should be 'none' (clear proxy properties), 'false' (system settings), 'true' (custom settings): "
                      + proxyTypeString);
    }
    proxyServerHttpTB.setText(Cache.getDefault("PROXY_SERVER", ""));
    proxyPortHttpTB.setText(Cache.getDefault("PROXY_PORT", ""));
    proxyServerHttpsTB.setText(Cache.getDefault("PROXY_SERVER_HTTPS", ""));
    proxyPortHttpsTB.setText(Cache.getDefault("PROXY_PORT_HTTPS", ""));
    proxyAuth.setSelected(Cache.getDefault("PROXY_AUTH", false));
    proxyAuthUsernameTB
            .setText(Cache.getDefault("PROXY_AUTH_USERNAME", ""));
    // we are not storing or retrieving proxy password from .jalview_properties
    proxyAuthPasswordPB.setText(Cache.proxyAuthPassword == null ? ""
            : new String(Cache.proxyAuthPassword));
    setCustomProxyEnabled();
    applyProxyButtonEnabled(false);

    usagestats.setSelected(Cache.getDefault("USAGESTATS", false));
    // note antisense here: default is true
    questionnaire
            .setSelected(Cache.getProperty("NOQUESTIONNAIRES") == null);
    versioncheck.setSelected(Cache.getDefault("VERSION_CHECK", true));

    /*
     * Set Output tab defaults
     */
    setupOutputCombo(epsRendering, "EPS_RENDERING");
    setupOutputCombo(htmlRendering, "HTML_RENDERING");
    setupOutputCombo(svgRendering, "SVG_RENDERING");
    autoIdWidth.setSelected(Cache.getDefault("FIGURE_AUTOIDWIDTH", false));
    userIdWidth.setEnabled(!autoIdWidth.isSelected());
    userIdWidthlabel.setEnabled(!autoIdWidth.isSelected());
    Integer wi = Cache.getIntegerProperty("FIGURE_FIXEDIDWIDTH");
    userIdWidth.setText(wi == null ? "" : wi.toString());
    // TODO: refactor to use common enum via FormatAdapter and allow extension
    // for new flat file formats
    blcjv.setSelected(Cache.getDefault("BLC_JVSUFFIX", true));
    clustaljv.setSelected(Cache.getDefault("CLUSTAL_JVSUFFIX", true));
    fastajv.setSelected(Cache.getDefault("FASTA_JVSUFFIX", true));
    msfjv.setSelected(Cache.getDefault("MSF_JVSUFFIX", true));
    pfamjv.setSelected(Cache.getDefault("PFAM_JVSUFFIX", true));
    pileupjv.setSelected(Cache.getDefault("PILEUP_JVSUFFIX", true));
    pirjv.setSelected(Cache.getDefault("PIR_JVSUFFIX", true));
    modellerOutput.setSelected(Cache.getDefault("PIR_MODELLER", false));
    embbedBioJSON
            .setSelected(Cache.getDefault("EXPORT_EMBBED_BIOJSON", true));

    /*
     * Set Editing tab defaults
     */
    autoCalculateConsCheck
            .setSelected(Cache.getDefault("AUTO_CALC_CONSENSUS", true));
    padGaps.setSelected(Cache.getDefault("PAD_GAPS", false));
    sortByTree.setSelected(Cache.getDefault("SORT_BY_TREE", false));

    annotations_actionPerformed(null); // update the display of the annotation
                                       // settings

    /*
     * Set Backups tab defaults
     */
    loadLastSavedBackupsOptions();

    /*
     * Set Startup tab defaults
     */

    // re-enable
    Cache.enableSessionProperties();
  }

  /**
   * A helper method that sets the items and initial selection in a character
   * rendering option list (Prompt each time/Lineart/Text)
   * 
   * @param comboBox
   * @param propertyKey
   */
  protected void setupOutputCombo(JComboBox<Object> comboBox,
          String propertyKey)
  {
    Cache.disableSessionProperties();

    comboBox.addItem(promptEachTimeOpt);
    comboBox.addItem(lineArtOpt);
    comboBox.addItem(textOpt);

    /*
     * JalviewJS doesn't support Lineart so force it to Text
     */
    String defaultOption = Platform.isJS() ? "Text"
            : Cache.getDefault(propertyKey, "Prompt each time");
    if (defaultOption.equalsIgnoreCase("Text"))
    {
      comboBox.setSelectedItem(textOpt);
    }
    else if (defaultOption.equalsIgnoreCase("Lineart"))
    {
      comboBox.setSelectedItem(lineArtOpt);
    }
    else
    {
      comboBox.setSelectedItem(promptEachTimeOpt);
    }

    Cache.enableSessionProperties();
  }

  /**
   * Save user selections on the Preferences tabs to the Cache and write out to
   * file.
   * 
   * @param e
   */
  @Override
  public void ok_actionPerformed(ActionEvent e)
  {
    Cache.disableSessionProperties();

    if (!validateSettings())
    {
      return;
    }

    /* 
     * Set proxy settings first (to be before web services refresh)
     */
    saveProxySettings();

    /*
     * Save Visual settings
     */

    Cache.applicationProperties.setProperty("SHOW_JVSUFFIX",
            Boolean.toString(seqLimit.isSelected()));
    Cache.applicationProperties.setProperty("RIGHT_ALIGN_IDS",
            Boolean.toString(rightAlign.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_FULLSCREEN",
            Boolean.toString(fullScreen.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_OVERVIEW",
            Boolean.toString(openoverv.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS",
            Boolean.toString(annotations.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION",
            Boolean.toString(conservation.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_QUALITY",
            Boolean.toString(quality.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.toString(identity.isSelected()));

    Cache.applicationProperties.setProperty("GAP_SYMBOL",
            gapSymbolCB.getSelectedItem().toString());

    Cache.applicationProperties.setProperty("FONT_NAME",
            fontNameCB.getSelectedItem().toString());
    Cache.applicationProperties.setProperty("FONT_STYLE",
            fontStyleCB.getSelectedItem().toString());
    Cache.applicationProperties.setProperty("FONT_SIZE",
            fontSizeCB.getSelectedItem().toString());

    Cache.applicationProperties.setProperty("ID_ITALICS",
            Boolean.toString(idItalics.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_UNCONSERVED",
            Boolean.toString(showUnconserved.isSelected()));
    Cache.applicationProperties.setProperty(SHOW_OCCUPANCY,
            Boolean.toString(showOccupancy.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_GROUP_CONSENSUS",
            Boolean.toString(showGroupConsensus.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_GROUP_CONSERVATION",
            Boolean.toString(showGroupConservation.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_CONSENSUS_HISTOGRAM",
            Boolean.toString(showConsensHistogram.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_CONSENSUS_LOGO",
            Boolean.toString(showConsensLogo.isSelected()));
    Cache.applicationProperties.setProperty("ANTI_ALIAS",
            Boolean.toString(smoothFont.isSelected()));
    Cache.applicationProperties.setProperty(SCALE_PROTEIN_TO_CDNA,
            Boolean.toString(scaleProteinToCdna.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_NPFEATS_TOOLTIP",
            Boolean.toString(showNpTooltip.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_DBREFS_TOOLTIP",
            Boolean.toString(showDbRefTooltip.isSelected()));

    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT",
            Boolean.toString(wrap.isSelected()));

    Cache.applicationProperties.setProperty("STARTUP_FILE",
            startupFileTextfield.getText());
    Cache.applicationProperties.setProperty("SHOW_STARTUP_FILE",
            Boolean.toString(startupCheckbox.isSelected()));

    Cache.applicationProperties.setProperty("SORT_ALIGNMENT",
            sortby.getSelectedItem().toString());

    // convert description of sort order to enum name for save
    SequenceAnnotationOrder annSortOrder = SequenceAnnotationOrder
            .forDescription(sortAnnBy.getSelectedItem().toString());
    if (annSortOrder != null)
    {
      Cache.applicationProperties.setProperty(SORT_ANNOTATIONS,
              annSortOrder.name());
    }

    final boolean showAutocalcFirst = sortAutocalc.getSelectedIndex() == 0;
    Cache.applicationProperties.setProperty(SHOW_AUTOCALC_ABOVE,
            Boolean.valueOf(showAutocalcFirst).toString());

    /*
     * Save Colours settings
     */
    Cache.applicationProperties.setProperty(DEFAULT_COLOUR_PROT,
            protColour.getSelectedItem().toString());
    Cache.applicationProperties.setProperty(DEFAULT_COLOUR_NUC,
            nucColour.getSelectedItem().toString());
    Cache.setColourProperty("ANNOTATIONCOLOUR_MIN",
            minColour.getBackground());
    Cache.setColourProperty("ANNOTATIONCOLOUR_MAX",
            maxColour.getBackground());

    /*
     * Save Overview settings
     */
    Cache.setColourProperty(GAP_COLOUR, gapColour.getBackground());
    Cache.setColourProperty(HIDDEN_COLOUR, hiddenColour.getBackground());
    Cache.applicationProperties.setProperty(USE_LEGACY_GAP,
            Boolean.toString(useLegacyGap.isSelected()));
    Cache.applicationProperties.setProperty(SHOW_OV_HIDDEN_AT_START,
            Boolean.toString(showHiddenAtStart.isSelected()));

    /*
     * Save Structure settings
     */
    Cache.applicationProperties.setProperty(ADD_TEMPFACT_ANN,
            Boolean.toString(addTempFactor.isSelected()));
    Cache.applicationProperties.setProperty(ADD_SS_ANN,
            Boolean.toString(addSecondaryStructure.isSelected()));
    Cache.applicationProperties.setProperty(STRUCT_FROM_PDB,
            Boolean.toString(structFromPdb.isSelected()));
    String viewer = structViewer.getSelectedItem().toString();
    String viewerPath = structureViewerPath.getText();
    Cache.applicationProperties.setProperty(STRUCTURE_DISPLAY, viewer);
    if (viewer.equals(ViewerType.CHIMERA.name()))
    {
      Cache.setOrRemove(CHIMERA_PATH, viewerPath);
    }
    else if (viewer.equals(ViewerType.CHIMERAX.name()))
    {
      Cache.setOrRemove(CHIMERAX_PATH, viewerPath);
    }
    else if (viewer.equals(ViewerType.PYMOL.name()))
    {
      Cache.setOrRemove(PYMOL_PATH, viewerPath);
    }
    Cache.applicationProperties.setProperty("MAP_WITH_SIFTS",
            Boolean.toString(siftsMapping.isSelected()));
    SiftsSettings.setMapWithSifts(siftsMapping.isSelected());

    /*
     * Save Output settings
     */
    Cache.applicationProperties.setProperty("EPS_RENDERING",
            ((OptionsParam) epsRendering.getSelectedItem()).getCode());
    Cache.applicationProperties.setProperty("HTML_RENDERING",
            ((OptionsParam) htmlRendering.getSelectedItem()).getCode());
    Cache.applicationProperties.setProperty("SVG_RENDERING",
            ((OptionsParam) svgRendering.getSelectedItem()).getCode());

    /*
     * Save Connections settings
     */
    // Proxy settings set first (to catch web services)

    // save user-defined and selected links
    String menuLinks = sequenceUrlLinks.writeUrlsAsString(true);
    if (menuLinks.isEmpty())
    {
      Cache.applicationProperties.remove("SEQUENCE_LINKS");
    }
    else
    {
      Cache.applicationProperties.setProperty("SEQUENCE_LINKS",
              menuLinks.toString());
    }

    String nonMenuLinks = sequenceUrlLinks.writeUrlsAsString(false);
    if (nonMenuLinks.isEmpty())
    {
      Cache.applicationProperties.remove("STORED_LINKS");
    }
    else
    {
      Cache.applicationProperties.setProperty("STORED_LINKS",
              nonMenuLinks.toString());
    }

    Cache.applicationProperties.setProperty("DEFAULT_URL",
            sequenceUrlLinks.getPrimaryUrlId());

    Cache.setProperty("VERSION_CHECK",
            Boolean.toString(versioncheck.isSelected()));
    if (Cache.getProperty("USAGESTATS") != null || usagestats.isSelected())
    {
      // default is false - we only set this if the user has actively agreed
      Cache.setProperty("USAGESTATS",
              Boolean.toString(usagestats.isSelected()));
    }
    if (!questionnaire.isSelected())
    {
      Cache.setProperty("NOQUESTIONNAIRES", "true");
    }
    else
    {
      // special - made easy to edit a property file to disable questionnaires
      // by just adding the given line
      Cache.removeProperty("NOQUESTIONNAIRES");
    }

    /*
     * Save Output settings
     */
    Cache.applicationProperties.setProperty("BLC_JVSUFFIX",
            Boolean.toString(blcjv.isSelected()));
    Cache.applicationProperties.setProperty("CLUSTAL_JVSUFFIX",
            Boolean.toString(clustaljv.isSelected()));
    Cache.applicationProperties.setProperty("FASTA_JVSUFFIX",
            Boolean.toString(fastajv.isSelected()));
    Cache.applicationProperties.setProperty("MSF_JVSUFFIX",
            Boolean.toString(msfjv.isSelected()));
    Cache.applicationProperties.setProperty("PFAM_JVSUFFIX",
            Boolean.toString(pfamjv.isSelected()));
    Cache.applicationProperties.setProperty("PILEUP_JVSUFFIX",
            Boolean.toString(pileupjv.isSelected()));
    Cache.applicationProperties.setProperty("PIR_JVSUFFIX",
            Boolean.toString(pirjv.isSelected()));
    Cache.applicationProperties.setProperty("PIR_MODELLER",
            Boolean.toString(modellerOutput.isSelected()));
    Cache.applicationProperties.setProperty("EXPORT_EMBBED_BIOJSON",
            Boolean.toString(embbedBioJSON.isSelected()));
    jalview.io.PIRFile.useModellerOutput = modellerOutput.isSelected();

    Cache.applicationProperties.setProperty("FIGURE_AUTOIDWIDTH",
            Boolean.toString(autoIdWidth.isSelected()));
    userIdWidth_actionPerformed();
    Cache.applicationProperties.setProperty("FIGURE_FIXEDIDWIDTH",
            userIdWidth.getText());

    /*
     * Save Editing settings
     */
    Cache.applicationProperties.setProperty("AUTO_CALC_CONSENSUS",
            Boolean.toString(autoCalculateConsCheck.isSelected()));
    Cache.applicationProperties.setProperty("SORT_BY_TREE",
            Boolean.toString(sortByTree.isSelected()));
    Cache.applicationProperties.setProperty("PAD_GAPS",
            Boolean.toString(padGaps.isSelected()));

    if (!Platform.isJS())
    {
      wsPrefs.updateAndRefreshWsMenuConfig(false);
    }

    /*
     * Save Backups settings
     */
    Cache.applicationProperties.setProperty(BackupFiles.ENABLED,
            Boolean.toString(enableBackupFiles.isSelected()));
    int preset = getComboIntStringKey(backupfilesPresetsCombo);
    Cache.applicationProperties.setProperty(BackupFiles.NS + "_PRESET",
            Integer.toString(preset));

    if (preset == BackupFilesPresetEntry.BACKUPFILESSCHEMECUSTOM)
    {
      BackupFilesPresetEntry customBFPE = getBackupfilesCurrentEntry();
      BackupFilesPresetEntry.backupfilesPresetEntriesValues.put(
              BackupFilesPresetEntry.BACKUPFILESSCHEMECUSTOM, customBFPE);
      Cache.applicationProperties.setProperty(
              BackupFilesPresetEntry.CUSTOMCONFIG, customBFPE.toString());
    }

    BackupFilesPresetEntry savedBFPE = BackupFilesPresetEntry.backupfilesPresetEntriesValues
            .get(preset);
    Cache.applicationProperties.setProperty(
            BackupFilesPresetEntry.SAVEDCONFIG, savedBFPE.toString());

    /*
     * Save Memory Settings
     */
    Cache.applicationProperties.setProperty(
            MemorySetting.CUSTOMISED_SETTINGS,
            Boolean.toString(customiseMemorySetting.isSelected()));
    Cache.applicationProperties.setProperty(MemorySetting.MEMORY_JVMMEMPC,
            Integer.toString(jvmMemoryPercentSlider.getValue()));
    Cache.applicationProperties.setProperty(MemorySetting.MEMORY_JVMMEMMAX,
            jvmMemoryMaxTextField.getText());

    /*
     * save and close Preferences
     */

    Cache.saveProperties();
    Desktop.instance.doConfigureStructurePrefs();
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }

    Cache.enableSessionProperties();
  }

  public void saveProxySettings()
  {
    Cache.disableSessionProperties();

    String newProxyType = customProxy.isSelected() ? Cache.PROXYTYPE_CUSTOM
            : noProxy.isSelected() ? Cache.PROXYTYPE_NONE
                    : Cache.PROXYTYPE_SYSTEM;
    Cache.applicationProperties.setProperty("USE_PROXY", newProxyType);
    Cache.setOrRemove("PROXY_SERVER", proxyServerHttpTB.getText());
    Cache.setOrRemove("PROXY_PORT", proxyPortHttpTB.getText());
    Cache.setOrRemove("PROXY_SERVER_HTTPS", proxyServerHttpsTB.getText());
    Cache.setOrRemove("PROXY_PORT_HTTPS", proxyPortHttpsTB.getText());
    Cache.setOrRemove("PROXY_AUTH",
            Boolean.toString(proxyAuth.isSelected()));
    Cache.setOrRemove("PROXY_AUTH_USERNAME", proxyAuthUsernameTB.getText());
    Cache.proxyAuthPassword = proxyAuthPasswordPB.getPassword();
    Cache.setProxyPropertiesFromPreferences(previousProxyType);
    if (newProxyType.equals(Cache.PROXYTYPE_CUSTOM)
            || !newProxyType.equals(previousProxyType))
    {
      // force a re-lookup of ws if proxytype is custom or has changed
      wsPrefs.update++;
    }
    previousProxyType = newProxyType;

    Cache.enableSessionProperties();
  }

  /**
   * Do any necessary validation before saving settings. Return focus to the
   * first tab which fails validation.
   * 
   * @return
   */
  private boolean validateSettings()
  {
    if (!validateStructure())
    {
      structureTab.requestFocusInWindow();
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateStructure()
  {
    return validateViewerPath();

  }

  /**
   * DOCUMENT ME!
   */
  @Override
  public void startupFileTextfield_mouseClicked()
  {
    Cache.disableSessionProperties();

    // TODO: JAL-3048 not needed for Jalview-JS
    String fileFormat = Cache.getProperty("DEFAULT_FILE_FORMAT");
    JalviewFileChooser chooser = JalviewFileChooser
            .forRead(Cache.getProperty("LAST_DIRECTORY"), fileFormat);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.select_startup_file"));

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      FileFormatI format = chooser.getSelectedFormat();
      if (format != null)
      {
        Cache.applicationProperties.setProperty("DEFAULT_FILE_FORMAT",
                format.getName());
      }
      startupFileTextfield
              .setText(chooser.getSelectedFile().getAbsolutePath());
    }

    Cache.enableSessionProperties();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void cancel_actionPerformed(ActionEvent e)
  {
    try
    {
      if (!Platform.isJS())
      {
        wsPrefs.updateWsMenuConfig(true);
        wsPrefs.refreshWs_actionPerformed(e);
      }
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void annotations_actionPerformed(ActionEvent e)
  {
    conservation.setEnabled(annotations.isSelected());
    quality.setEnabled(annotations.isSelected());
    identity.setEnabled(annotations.isSelected());
    showOccupancy.setEnabled(annotations.isSelected());
    showGroupConsensus.setEnabled(annotations.isSelected());
    showGroupConservation.setEnabled(annotations.isSelected());
    showConsensHistogram.setEnabled(annotations.isSelected()
            && (identity.isSelected() || showGroupConsensus.isSelected()));
    showConsensLogo.setEnabled(annotations.isSelected()
            && (identity.isSelected() || showGroupConsensus.isSelected()));
  }

  @Override
  public void newLink_actionPerformed(ActionEvent e)
  {
    GSequenceLink link = new GSequenceLink();
    boolean valid = false;
    while (!valid)
    {
      if (JvOptionPane.showInternalConfirmDialog(Desktop.desktop, link,
              MessageManager.getString("label.new_sequence_url_link"),
              JvOptionPane.OK_CANCEL_OPTION, -1,
              null) == JvOptionPane.OK_OPTION)
      {
        if (link.checkValid())
        {
          if (((UrlLinkTableModel) linkUrlTable.getModel())
                  .isUniqueName(link.getName()))
          {
            ((UrlLinkTableModel) linkUrlTable.getModel())
                    .insertRow(link.getName(), link.getURL());
            valid = true;
          }
          else
          {
            link.notifyDuplicate();
            continue;
          }
        }
      }
      else
      {
        break;
      }
    }
  }

  @Override
  public void editLink_actionPerformed(ActionEvent e)
  {
    GSequenceLink link = new GSequenceLink();

    int index = linkUrlTable.getSelectedRow();
    if (index == -1)
    {
      // button no longer enabled if row is not selected
      Console.debug("Edit with no row selected in linkUrlTable");
      return;
    }

    int nameCol = ((UrlLinkTableModel) linkUrlTable.getModel())
            .getNameColumn();
    int urlCol = ((UrlLinkTableModel) linkUrlTable.getModel())
            .getUrlColumn();
    String oldName = linkUrlTable.getValueAt(index, nameCol).toString();
    link.setName(oldName);
    link.setURL(linkUrlTable.getValueAt(index, urlCol).toString());

    boolean valid = false;
    while (!valid)
    {
      if (JvOptionPane.showInternalConfirmDialog(Desktop.desktop, link,
              MessageManager.getString("label.edit_sequence_url_link"),
              JvOptionPane.OK_CANCEL_OPTION, -1,
              null) == JvOptionPane.OK_OPTION)
      {
        if (link.checkValid())
        {
          if ((oldName.equals(link.getName()))
                  || (((UrlLinkTableModel) linkUrlTable.getModel())
                          .isUniqueName(link.getName())))
          {
            linkUrlTable.setValueAt(link.getName(), index, nameCol);
            linkUrlTable.setValueAt(link.getURL(), index, urlCol);
            valid = true;
          }
          else
          {
            link.notifyDuplicate();
            continue;
          }
        }
      }
      else
      {
        break;
      }
    }
  }

  @Override
  public void deleteLink_actionPerformed(ActionEvent e)
  {
    int index = linkUrlTable.getSelectedRow();
    int modelIndex = -1;
    if (index == -1)
    {
      // button no longer enabled if row is not selected
      Console.debug("Delete with no row selected in linkUrlTable");
      return;
    }
    else
    {
      modelIndex = linkUrlTable.convertRowIndexToModel(index);
    }

    // make sure we use the model index to delete, and not the table index
    ((UrlLinkTableModel) linkUrlTable.getModel()).removeRow(modelIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GPreferences#showunconserved_actionPerformed(java.awt.event
   * .ActionEvent)
   */
  @Override
  protected void showunconserved_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub
    super.showunconserved_actionPerformed(e);
  }

  public static List<String> getGroupURLLinks()
  {
    return groupURLLinks;
  }

  @Override
  public void minColour_actionPerformed(JPanel panel)
  {
    JalviewColourChooser.showColourChooser(this,
            MessageManager.getString("label.select_colour_minimum_value"),
            panel);
  }

  @Override
  public void maxColour_actionPerformed(JPanel panel)
  {
    JalviewColourChooser.showColourChooser(this,
            MessageManager.getString("label.select_colour_maximum_value"),
            panel);
  }

  @Override
  public void gapColour_actionPerformed(JPanel gap)
  {
    if (!useLegacyGap.isSelected())
    {
      JalviewColourChooser.showColourChooser(this,
              MessageManager.getString("label.select_gap_colour"), gap);
    }
  }

  @Override
  public void hiddenColour_actionPerformed(JPanel hidden)
  {
    JalviewColourChooser.showColourChooser(this,
            MessageManager.getString("label.select_hidden_colour"), hidden);
  }

  @Override
  protected void useLegacyGaps_actionPerformed(ActionEvent e)
  {
    boolean enabled = useLegacyGap.isSelected();
    if (enabled)
    {
      gapColour.setBackground(OverviewCanvas.OVERVIEW_DEFAULT_LEGACY_GAP);
    }
    else
    {
      gapColour.setBackground(OverviewCanvas.OVERVIEW_DEFAULT_GAP);
    }
    gapColour.setEnabled(!enabled);
    gapLabel.setEnabled(!enabled);
  }

  @Override
  protected void resetOvDefaults_actionPerformed(ActionEvent e)
  {
    useLegacyGap.setSelected(false);
    useLegacyGaps_actionPerformed(null);
    showHiddenAtStart.setSelected(false);
    hiddenColour.setBackground(OverviewCanvas.OVERVIEW_DEFAULT_HIDDEN);
  }

  @Override
  protected void userIdWidth_actionPerformed()
  {
    try
    {
      String val = userIdWidth.getText().trim();
      if (val.length() > 0)
      {
        Integer iw = Integer.parseInt(val);
        if (iw.intValue() < 12)
        {
          throw new NumberFormatException();
        }
        userIdWidth.setText(iw.toString());
      }
    } catch (NumberFormatException x)
    {
      userIdWidth.setText("");
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager
                      .getString("warn.user_defined_width_requirements"),
              MessageManager.getString("label.invalid_id_column_width"),
              JvOptionPane.WARNING_MESSAGE);
    }
  }

  @Override
  protected void autoIdWidth_actionPerformed()
  {
    userIdWidth.setEnabled(!autoIdWidth.isSelected());
    userIdWidthlabel.setEnabled(!autoIdWidth.isSelected());
  }

  /**
   * Returns true if structure viewer path is to a valid executable, else shows
   * an error dialog. Does nothing if the path is empty, as is the case for Jmol
   * (built in to Jalview) or when Jalview is left to try default paths.
   */
  private boolean validateViewerPath()
  {
    if (structureViewerPath.getText().trim().length() > 0)
    {
      File f = new File(structureViewerPath.getText());
      if (!f.canExecute())
      {
        JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                MessageManager.getString("label.invalid_viewer_path"),
                MessageManager.getString("label.invalid_viewer_path"),
                JvOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    return true;
  }

  /**
   * If Chimera or ChimeraX or Pymol is selected, check it can be found on
   * default or user-specified path, if not show a warning/help dialog
   */
  @Override
  protected void structureViewer_actionPerformed(String selectedItem)
  {
    if (selectedItem.equals(ViewerType.JMOL.name()))
    {
      structureViewerPath.setEnabled(false);
      structureViewerPathLabel.setEnabled(false);
      return;
    }
    boolean found = false;
    structureViewerPath.setEnabled(true);
    structureViewerPathLabel.setEnabled(true);
    structureViewerPathLabel.setText(MessageManager
            .formatMessage("label.viewer_path", selectedItem));

    /*
     * Try user-specified and standard paths for structure viewer executable
     */
    String viewerPath = "";
    List<String> paths = null;
    Cache.disableSessionProperties();
    try
    {
      ViewerType viewerType = ViewerType.valueOf(selectedItem);
      switch (viewerType)
      {
      case JMOL:
        // dealt with above
        break;
      case CHIMERA:
        viewerPath = Cache.getDefault(CHIMERA_PATH, "");
        paths = StructureManager.getChimeraPaths(false);
        break;
      case CHIMERAX:
        viewerPath = Cache.getDefault(CHIMERAX_PATH, "");
        paths = StructureManager.getChimeraPaths(true);
        break;
      case PYMOL:
        viewerPath = Cache.getDefault(PYMOL_PATH, "");
        paths = PymolManager.getPymolPaths();
        break;
      }
    } catch (IllegalArgumentException e)
    {
      // only valid entries should be in the drop-down
    }
    Cache.enableSessionProperties();
    structureViewerPath.setText(viewerPath);

    paths.add(0, structureViewerPath.getText());
    for (String path : paths)
    {
      if (new File(path.trim()).canExecute())
      {
        found = true;
        break;
      }
    }

    if (!found)
    {
      String[] options = { "OK", "Help" };
      int showHelp = JvOptionPane.showInternalOptionDialog(Desktop.desktop,
              JvSwingUtils.wrapTooltip(true,
                      MessageManager.getString("label.viewer_missing")),
              "", JvOptionPane.YES_NO_OPTION, JvOptionPane.WARNING_MESSAGE,
              null, options, options[0]);

      if (showHelp == JvOptionPane.NO_OPTION)
      {
        this.selectTab(Preferences.TabRef.STRUCTURE_TAB, null);
        try
        {
          Help.showHelpWindow(HelpId.StructureViewer);
        } catch (HelpSetException e)
        {
          e.printStackTrace();
        }
      }
      else if (showHelp == JvOptionPane.OK_OPTION)
      {
        this.selectTab(Preferences.TabRef.STRUCTURE_TAB, null);
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
          try
          {
            for (int i = 0; i < 3; i++)
            {
              structureViewerPath.setBackground(Color.PINK);
              Thread.sleep(500);
              structureViewerPath.setBackground(Color.WHITE);
              Thread.sleep(500);
            }
          } catch (InterruptedException e)
          {
          }
        });
      }
    }
  }

  public class OptionsParam
  {
    private String name;

    private String code;

    public OptionsParam(String name, String code)
    {
      this.name = name;
      this.code = code;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }

    @Override
    public String toString()
    {
      return name;
    }

    @Override
    public boolean equals(Object that)
    {
      if (!(that instanceof OptionsParam))
      {
        return false;
      }
      return this.code.equalsIgnoreCase(((OptionsParam) that).code);
    }

    @Override
    public int hashCode()
    {
      return name.hashCode() + code.hashCode();
    }
  }

  private class UrlListSelectionHandler implements ListSelectionListener
  {

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel lsm = (ListSelectionModel) e.getSource();

      int index = lsm.getMinSelectionIndex();
      if (index == -1)
      {
        // no selection, so disable delete/edit buttons
        editLink.setEnabled(false);
        deleteLink.setEnabled(false);
        return;
      }
      int modelIndex = linkUrlTable.convertRowIndexToModel(index);

      // enable/disable edit and delete link buttons
      if (((UrlLinkTableModel) linkUrlTable.getModel())
              .isRowDeletable(modelIndex))
      {
        deleteLink.setEnabled(true);
      }
      else
      {
        deleteLink.setEnabled(false);
      }

      if (((UrlLinkTableModel) linkUrlTable.getModel())
              .isRowEditable(modelIndex))
      {
        editLink.setEnabled(true);
      }
      else
      {
        editLink.setEnabled(false);
      }
    }
  }
}
