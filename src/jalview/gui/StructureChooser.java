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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.stevesoft.pat.Regex;

import jalview.analysis.AlignmentUtils;
import jalview.api.AlignmentViewPanel;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JmolParser;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSDataColumnPreferences;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.service.pdb.PDBFTSRestClient;
import jalview.fts.service.threedbeacons.TDB_FTSData;
import jalview.gui.StructureViewer.ViewerType;
import jalview.gui.structurechooser.PDBStructureChooserQuerySource;
import jalview.gui.structurechooser.StructureChooserQuerySource;
import jalview.gui.structurechooser.ThreeDBStructureChooserQuerySource;
import jalview.io.DataSourceType;
import jalview.io.FileFormatException;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.FilterOption;
import jalview.jbgui.GStructureChooser;
import jalview.structure.StructureImportSettings.TFType;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.StringUtils;
import jalview.ws.DBRefFetcher;
import jalview.ws.DBRefFetcher.FetchFinishedListenerI;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;
import jalview.ws.dbsources.EBIAlfaFold;
import jalview.ws.seqfetcher.DbSourceProxy;
import jalview.ws.sifts.SiftsSettings;

/**
 * Provides the behaviors for the Structure chooser Panel
 * 
 * @author tcnofoegbu
 *
 */
@SuppressWarnings("serial")
public class StructureChooser extends GStructureChooser
        implements IProgressIndicator
{
  private static final String AUTOSUPERIMPOSE = "AUTOSUPERIMPOSE";

  /**
   * warn user if need to fetch more than this many uniprot records at once
   */
  private static final int THRESHOLD_WARN_UNIPROT_FETCH_NEEDED = 20;

  private SequenceI selectedSequence;

  private SequenceI[] selectedSequences;

  private IProgressIndicator progressIndicator;

  private Collection<FTSData> discoveredStructuresSet;

  private StructureChooserQuerySource data;

  @Override
  protected FTSDataColumnPreferences getFTSDocFieldPrefs()
  {
    return data.getDocFieldPrefs();
  }

  private String selectedPdbFileName;

  private TFType localPdbTempfacType;

  private String localPdbPaeMatrixFileName;

  private boolean isValidPBDEntry;

  private boolean cachedPDBExists;

  private Collection<FTSData> lastDiscoveredStructuresSet;

  private boolean canQueryTDB = false;

  private boolean notQueriedTDBYet = true;

  List<SequenceI> seqsWithoutSourceDBRef = null;

  private boolean showChooserGUI = true;

  private static StructureViewer lastTargetedView = null;

  public StructureChooser(SequenceI[] selectedSeqs, SequenceI selectedSeq,
          AlignmentPanel ap)
  {
    this(selectedSeqs, selectedSeq, ap, true);
  }

  public StructureChooser(SequenceI[] selectedSeqs, SequenceI selectedSeq,
          AlignmentPanel ap, boolean showGUI)
  {
    // which FTS engine to use
    data = StructureChooserQuerySource.getQuerySourceFor(selectedSeqs);
    initDialog();

    this.ap = ap;
    this.selectedSequence = selectedSeq;
    this.selectedSequences = selectedSeqs;
    this.progressIndicator = (ap == null) ? null : ap.alignFrame;
    this.showChooserGUI = showGUI;
    init();

  }

  /**
   * sets canQueryTDB if protein sequences without a canonical uniprot ref or at
   * least one structure are discovered.
   */
  private void populateSeqsWithoutSourceDBRef()
  {
    seqsWithoutSourceDBRef = new ArrayList<SequenceI>();
    boolean needCanonical = false;
    for (SequenceI seq : selectedSequences)
    {
      if (seq.isProtein())
      {
        int dbRef = ThreeDBStructureChooserQuerySource
                .checkUniprotRefs(seq.getDBRefs());
        if (dbRef < 0)
        {
          if (dbRef == -1)
          {
            // need to retrieve canonicals
            needCanonical = true;
            seqsWithoutSourceDBRef.add(seq);
          }
          else
          {
            // could be a sequence with pdb ref
            if (seq.getAllPDBEntries() == null
                    || seq.getAllPDBEntries().size() == 0)
            {
              seqsWithoutSourceDBRef.add(seq);
            }
          }
        }
      }
    }
    // retrieve database refs for protein sequences
    if (!seqsWithoutSourceDBRef.isEmpty())
    {
      canQueryTDB = true;
      if (needCanonical)
      {
        // triggers display of the 'Query TDB' button
        notQueriedTDBYet = true;
      }
    }
  };

  /**
   * Initializes parameters used by the Structure Chooser Panel
   */
  protected void init()
  {
    if (!Jalview.isHeadlessMode())
    {
      progressBar = new ProgressBar(this.statusPanel, this.statusBar);
    }

    chk_superpose.setSelected(Cache.getDefault(AUTOSUPERIMPOSE, true));
    btn_queryTDB.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        promptForTDBFetch(false);
      }
    });

    Executors.defaultThreadFactory().newThread(new Runnable()
    {
      @Override
      public void run()
      {
        populateSeqsWithoutSourceDBRef();
        initialStructureDiscovery();
      }

    }).start();

  }

  // called by init
  private void initialStructureDiscovery()
  {
    // check which FTS engine to use
    data = StructureChooserQuerySource.getQuerySourceFor(selectedSequences);

    // ensure a filter option is in force for search
    populateFilterComboBox(true, cachedPDBExists);

    // looks for any existing structures already loaded
    // for the sequences (the cached ones)
    // then queries the StructureChooserQuerySource to
    // discover more structures.
    //
    // Possible optimisation is to only begin querying
    // the structure chooser if there are no cached structures.

    long startTime = System.currentTimeMillis();
    updateProgressIndicator(
            MessageManager.getString("status.loading_cached_pdb_entries"),
            startTime);
    loadLocalCachedPDBEntries();
    updateProgressIndicator(null, startTime);
    updateProgressIndicator(
            MessageManager.getString("status.searching_for_pdb_structures"),
            startTime);
    fetchStructuresMetaData();
    // revise filter options if no results were found
    populateFilterComboBox(isStructuresDiscovered(), cachedPDBExists);
    discoverStructureViews();
    updateProgressIndicator(null, startTime);
    mainFrame.setVisible(showChooserGUI);
    updateCurrentView();
  }

  /**
   * raises dialog for Uniprot fetch followed by 3D beacons search
   * 
   * @param ignoreGui
   *          - when true, don't ask, just fetch
   */
  public void promptForTDBFetch(boolean ignoreGui)
  {
    final long progressId = System.currentTimeMillis();

    // final action after prompting and discovering db refs
    final Runnable strucDiscovery = new Runnable()
    {
      @Override
      public void run()
      {
        mainFrame.setEnabled(false);
        cmb_filterOption.setEnabled(false);
        progressBar.setProgressBar(
                MessageManager.getString("status.searching_3d_beacons"),
                progressId);
        btn_queryTDB.setEnabled(false);
        // TODO: warn if no accessions discovered
        populateSeqsWithoutSourceDBRef();
        // redo initial discovery - this time with 3d beacons
        // Executors.
        previousWantedFields = null;
        lastSelected = (FilterOption) cmb_filterOption.getSelectedItem();
        cmb_filterOption.setSelectedItem(null);
        cachedPDBExists = false; // reset to initial
        initialStructureDiscovery();
        if (!isStructuresDiscovered())
        {
          progressBar.setProgressBar(MessageManager.getString(
                  "status.no_structures_discovered_from_3d_beacons"),
                  progressId);
          btn_queryTDB.setToolTipText(MessageManager.getString(
                  "status.no_structures_discovered_from_3d_beacons"));
          btn_queryTDB.setEnabled(false);
          pnl_queryTDB.setVisible(false);
        }
        else
        {
          cmb_filterOption.setSelectedIndex(0); // select 'best'
          btn_queryTDB.setVisible(false);
          pnl_queryTDB.setVisible(false);
          progressBar.setProgressBar(null, progressId);
        }
        mainFrame.setEnabled(true);
        cmb_filterOption.setEnabled(true);
      }
    };

    final FetchFinishedListenerI afterDbRefFetch = new FetchFinishedListenerI()
    {

      @Override
      public void finished()
      {
        // filter has been selected, so we set flag to remove ourselves
        notQueriedTDBYet = false;
        // new thread to discover structures - via 3d beacons
        Executors.defaultThreadFactory().newThread(strucDiscovery).start();

      }
    };

    // fetch db refs if OK pressed
    final Runnable discoverCanonicalDBrefs = () -> {
      btn_queryTDB.setEnabled(false);
      populateSeqsWithoutSourceDBRef();

      final int y = seqsWithoutSourceDBRef.size();
      if (y > 0)
      {
        final SequenceI[] seqWithoutSrcDBRef = seqsWithoutSourceDBRef
                .toArray(new SequenceI[y]);
        DBRefFetcher dbRefFetcher = new DBRefFetcher(seqWithoutSrcDBRef,
                progressBar, new DbSourceProxy[]
                { new jalview.ws.dbsources.Uniprot() }, null, false);
        dbRefFetcher.addListener(afterDbRefFetch);
        // ideally this would also gracefully run with callbacks

        dbRefFetcher.fetchDBRefs(true);
      }
      else
      {
        // call finished action directly
        afterDbRefFetch.finished();
      }
    };
    final Runnable revertview = () -> {
      if (lastSelected != null)
      {
        cmb_filterOption.setSelectedItem(lastSelected);
      }
    };
    int threshold = Cache.getDefault("UNIPROT_AUTOFETCH_THRESHOLD",
            THRESHOLD_WARN_UNIPROT_FETCH_NEEDED);
    Console.debug("Using Uniprot fetch threshold of " + threshold);
    if (ignoreGui || seqsWithoutSourceDBRef.size() < threshold)
    {
      Executors.newSingleThreadExecutor().submit(discoverCanonicalDBrefs);
      return;
    }
    // need cancel and no to result in the discoverPDB action - mocked is
    // 'cancel' TODO: mock should be OK

    StructureChooser thisSC = this;
    JvOptionPane.newOptionDialog(thisSC.getFrame())
            .setResponseHandler(JvOptionPane.OK_OPTION,
                    discoverCanonicalDBrefs)
            .setResponseHandler(JvOptionPane.CANCEL_OPTION, revertview)
            .setResponseHandler(JvOptionPane.NO_OPTION, revertview)
            .showDialog(
                    MessageManager.formatMessage(
                            "label.fetch_references_for_3dbeacons",
                            seqsWithoutSourceDBRef.size()),
                    MessageManager.getString("label.3dbeacons"),
                    JvOptionPane.YES_NO_OPTION, JvOptionPane.PLAIN_MESSAGE,
                    null, new Object[]
                    { MessageManager.getString("action.ok"),
                        MessageManager.getString("action.cancel") },
                    MessageManager.getString("action.ok"), false);
  }

  /**
   * Builds a drop-down choice list of existing structure viewers to which new
   * structures may be added. If this list is empty then it, and the 'Add'
   * button, are hidden.
   */
  private void discoverStructureViews()
  {
    if (Desktop.instance != null)
    {
      targetView.removeAllItems();
      if (lastTargetedView != null && !lastTargetedView.isVisible())
      {
        lastTargetedView = null;
      }
      int linkedViewsAt = 0;
      for (StructureViewerBase view : Desktop.instance
              .getStructureViewers(null, null))
      {
        StructureViewer viewHandler = (lastTargetedView != null
                && lastTargetedView.sview == view) ? lastTargetedView
                        : StructureViewer.reconfigure(view);

        if (view.isLinkedWith(ap))
        {
          targetView.insertItemAt(viewHandler, linkedViewsAt++);
        }
        else
        {
          targetView.addItem(viewHandler);
        }
      }

      /*
       * show option to Add to viewer if at least 1 viewer found
       */
      targetView.setVisible(false);
      if (targetView.getItemCount() > 0)
      {
        targetView.setVisible(true);
        if (lastTargetedView != null)
        {
          targetView.setSelectedItem(lastTargetedView);
        }
        else
        {
          targetView.setSelectedIndex(0);
        }
      }
      btn_add.setVisible(targetView.isVisible());
    }
  }

  /**
   * Updates the progress indicator with the specified message
   * 
   * @param message
   *          displayed message for the operation
   * @param id
   *          unique handle for this indicator
   */
  protected void updateProgressIndicator(String message, long id)
  {
    if (progressIndicator != null)
    {
      progressIndicator.setProgressBar(message, id);
    }
  }

  /**
   * Retrieve meta-data for all the structure(s) for a given sequence(s) in a
   * selection group
   */
  void fetchStructuresMetaData()
  {
    long startTime = System.currentTimeMillis();
    Collection<FTSDataColumnI> wantedFields = data.getDocFieldPrefs()
            .getStructureSummaryFields();

    discoveredStructuresSet = new LinkedHashSet<>();
    HashSet<String> errors = new HashSet<>();

    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());

    for (SequenceI seq : selectedSequences)
    {

      FTSRestResponse resultList;
      try
      {
        resultList = data.fetchStructuresMetaData(seq, wantedFields,
                selectedFilterOpt, !chk_invertFilter.isSelected());
        // null response means the FTSengine didn't yield a query for this
        // consider designing a special exception if we really wanted to be
        // OOCrazy
        if (resultList == null)
        {
          continue;
        }
      } catch (Exception e)
      {
        e.printStackTrace();
        errors.add(e.getMessage());
        continue;
      }
      if (resultList.getSearchSummary() != null
              && !resultList.getSearchSummary().isEmpty())
      {
        discoveredStructuresSet.addAll(resultList.getSearchSummary());
      }
    }

    int noOfStructuresFound = 0;
    String totalTime = (System.currentTimeMillis() - startTime)
            + " milli secs";
    if (discoveredStructuresSet != null
            && !discoveredStructuresSet.isEmpty())
    {
      getResultTable()
              .setModel(data.getTableModel(discoveredStructuresSet));

      noOfStructuresFound = discoveredStructuresSet.size();
      lastDiscoveredStructuresSet = discoveredStructuresSet;
      mainFrame.setTitle(MessageManager.formatMessage(
              "label.structure_chooser_no_of_structures",
              noOfStructuresFound, totalTime));
    }
    else
    {
      mainFrame.setTitle(MessageManager
              .getString("label.structure_chooser_manual_association"));
      if (errors.size() > 0)
      {
        StringBuilder errorMsg = new StringBuilder();
        for (String error : errors)
        {
          errorMsg.append(error).append("\n");
        }
        JvOptionPane.showMessageDialog(this, errorMsg.toString(),
                MessageManager.getString("label.pdb_web-service_error"),
                JvOptionPane.ERROR_MESSAGE);
      }
    }
  }

  protected void loadLocalCachedPDBEntries()
  {
    ArrayList<CachedPDB> entries = new ArrayList<>();
    for (SequenceI seq : selectedSequences)
    {
      if (seq.getDatasetSequence() != null
              && seq.getDatasetSequence().getAllPDBEntries() != null)
      {
        for (PDBEntry pdbEntry : seq.getDatasetSequence()
                .getAllPDBEntries())
        {
          if (pdbEntry.getFile() != null)
          {
            entries.add(new CachedPDB(seq, pdbEntry));
          }
        }
      }
    }
    cachedPDBExists = !entries.isEmpty();
    PDBEntryTableModel tableModelx = new PDBEntryTableModel(entries);
    tbl_local_pdb.setModel(tableModelx);
  }

  /**
   * Filters a given list of discovered structures based on supplied argument
   * 
   * @param fieldToFilterBy
   *          the field to filter by
   */
  void filterResultSet(final String fieldToFilterBy)
  {
    Thread filterThread = new Thread(new Runnable()
    {

      @Override
      public void run()
      {
        long startTime = System.currentTimeMillis();
        lbl_loading.setVisible(true);
        Collection<FTSDataColumnI> wantedFields = data.getDocFieldPrefs()
                .getStructureSummaryFields();
        Collection<FTSData> filteredResponse = new HashSet<>();
        HashSet<String> errors = new HashSet<>();

        for (SequenceI seq : selectedSequences)
        {

          FTSRestResponse resultList;
          try
          {
            resultList = data.selectFirstRankedQuery(seq,
                    discoveredStructuresSet, wantedFields, fieldToFilterBy,
                    !chk_invertFilter.isSelected());

          } catch (Exception e)
          {
            e.printStackTrace();
            errors.add(e.getMessage());
            continue;
          }
          if (resultList.getSearchSummary() != null
                  && !resultList.getSearchSummary().isEmpty())
          {
            filteredResponse.addAll(resultList.getSearchSummary());
          }
        }

        String totalTime = (System.currentTimeMillis() - startTime)
                + " milli secs";
        if (!filteredResponse.isEmpty())
        {
          final int filterResponseCount = filteredResponse.size();
          Collection<FTSData> reorderedStructuresSet = new LinkedHashSet<>();
          reorderedStructuresSet.addAll(filteredResponse);
          reorderedStructuresSet.addAll(discoveredStructuresSet);
          getResultTable()
                  .setModel(data.getTableModel(reorderedStructuresSet));

          FTSRestResponse.configureTableColumn(getResultTable(),
                  wantedFields, tempUserPrefs);
          getResultTable().getColumn("Ref Sequence").setPreferredWidth(120);
          getResultTable().getColumn("Ref Sequence").setMinWidth(100);
          getResultTable().getColumn("Ref Sequence").setMaxWidth(200);
          // Update table selection model here
          getResultTable().addRowSelectionInterval(0,
                  filterResponseCount - 1);
          mainFrame.setTitle(MessageManager.formatMessage(
                  "label.structure_chooser_filter_time", totalTime));
        }
        else
        {
          mainFrame.setTitle(MessageManager.formatMessage(
                  "label.structure_chooser_filter_time", totalTime));
          if (errors.size() > 0)
          {
            StringBuilder errorMsg = new StringBuilder();
            for (String error : errors)
            {
              errorMsg.append(error).append("\n");
            }
            JvOptionPane.showMessageDialog(null, errorMsg.toString(),
                    MessageManager.getString("label.pdb_web-service_error"),
                    JvOptionPane.ERROR_MESSAGE);
          }
        }

        lbl_loading.setVisible(false);

        validateSelections();
      }
    });
    filterThread.start();
  }

  /**
   * Handles action event for btn_pdbFromFile
   */
  @Override
  protected void pdbFromFile_actionPerformed()
  {
    // TODO: JAL-3048 not needed for Jalview-JS until JSmol dep and
    // StructureChooser
    // works
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.formatMessage("label.select_pdb_file_for",
                    selectedSequence.getDisplayId(false)));
    chooser.setToolTipText(MessageManager.formatMessage(
            "label.load_pdb_file_associate_with_sequence",
            selectedSequence.getDisplayId(false)));

    int value = chooser.showOpenDialog(null);
    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      selectedPdbFileName = chooser.getSelectedFile().getPath();
      Cache.setProperty("LAST_DIRECTORY", selectedPdbFileName);
      boolean guessTFType = localPdbPaeMatrixFileName == null;
      localPdbPaeMatrixFileName = guessPAEFilename();
      guessTFType |= localPdbPaeMatrixFileName != null;
      Regex alphaFold = JmolParser.getNewAlphafoldValidator();
      if (guessTFType
              && alphaFold.search(new File(selectedPdbFileName).getName())
              && !tempFacAsChanged)
      {
        // localPdbPaeMatrixFileName was null and now isn't and filename could
        // well be AlphaFold and user hasn't adjusted the tempFacType
        combo_tempFacAs.setSelectedItem(TFType.PLDDT);
      }
      validateSelections();
    }
  }

  /**
   * Handles action event for btn_paeMatrixFile
   */
  @Override
  protected void paeMatrixFile_actionPerformed()
  {
    File pdbFile = new File(selectedPdbFileName);
    String setFile = Cache.getProperty("LAST_DIRECTORY");
    if (localPdbPaeMatrixFileName != null)
    {
      File paeFile = new File(localPdbPaeMatrixFileName);
      if (paeFile.exists())
        setFile = paeFile.getAbsolutePath();
      else if (paeFile.getParentFile().exists())
        setFile = paeFile.getParentFile().getAbsolutePath();
    }
    else
    {
      String guess = guessPAEFilename();
      if (guess != null)
        setFile = guess;
    }
    JalviewFileChooser chooser = new JalviewFileChooser(setFile);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.formatMessage(
            "label.select_pae_matrix_file_for", pdbFile.getName()));
    chooser.setToolTipText(MessageManager.formatMessage(
            "label.load_pae_matrix_file_associate_with_structure",
            pdbFile.getName()));

    // TODO convert to Callable/Promise
    int value = chooser.showOpenDialog(null);
    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String fileName = chooser.getSelectedFile().getPath();
      try
      {
        PAEContactMatrix.validateContactMatrixFile(fileName);
      } catch (Exception thr)
      {
        JvOptionPane.showInternalMessageDialog(this, MessageManager
                .formatMessage("label.couldnt_load_file", new Object[]
                { fileName }) + "<br>" + thr.getLocalizedMessage(),
                MessageManager.getString("label.error_loading_file"),
                JvOptionPane.WARNING_MESSAGE);
        Console.error("Couldn't import " + fileName + " as a PAE matrix",
                thr);
        return;
      }
      localPdbPaeMatrixFileName = fileName;
      Cache.setProperty("LAST_DIRECTORY", localPdbPaeMatrixFileName);
    }
    validateAssociationFromFile();
  }

  private String guessPAEFilename()
  {
    if (selectedPdbFileName.toLowerCase(Locale.ROOT).endsWith(".pdb")
            || selectedPdbFileName.toLowerCase(Locale.ROOT)
                    .endsWith(".cif"))
    {
      String jsonExt = selectedPdbFileName.substring(0,
              selectedPdbFileName.length() - 4) + ".json";
      // AlphaFold naming scheme
      String guessFile1 = StringUtils.replaceLast(jsonExt, "model",
              "predicted_aligned_error");
      // nf-core mode naming scheme
      String guessFile2 = StringUtils.replaceLast(jsonExt, ".json",
              "_scores.json");
      if (new File(guessFile1).exists())
      {
        return guessFile1;
      }
      else if (new File(jsonExt).exists())
      {
        return jsonExt;
      }
      else if (new File(guessFile2).exists())
      {
        return guessFile2;
      }
    }
    return null;
  }

  /**
   * Populates the filter combo-box options dynamically depending on discovered
   * structures
   */
  protected void populateFilterComboBox(boolean haveData,
          boolean cachedPDBExist)
  {
    populateFilterComboBox(haveData, cachedPDBExist, null);
  }

  /**
   * Populates the filter combo-box options dynamically depending on discovered
   * structures
   */
  protected void populateFilterComboBox(boolean haveData,
          boolean cachedPDBExist, FilterOption lastSel)
  {

    /*
     * temporarily suspend the change listener behaviour
     */
    cmb_filterOption.removeItemListener(this);
    int selSet = -1;
    cmb_filterOption.removeAllItems();
    if (haveData)
    {
      List<FilterOption> filters = data
              .getAvailableFilterOptions(VIEWS_FILTER);
      data.updateAvailableFilterOptions(VIEWS_FILTER, filters,
              lastDiscoveredStructuresSet);
      int p = 0;
      for (FilterOption filter : filters)
      {
        if (lastSel != null && filter.equals(lastSel))
        {
          selSet = p;
        }
        p++;
        cmb_filterOption.addItem(filter);
      }
    }

    cmb_filterOption.addItem(
            new FilterOption(MessageManager.getString("label.enter_pdb_id"),
                    "-", VIEWS_ENTER_ID, false, null));
    cmb_filterOption.addItem(
            new FilterOption(MessageManager.getString("label.from_file"),
                    "-", VIEWS_FROM_FILE, false, null));
    if (canQueryTDB && notQueriedTDBYet)
    {
      btn_queryTDB.setVisible(true);
      pnl_queryTDB.setVisible(true);
    }

    if (cachedPDBExist)
    {
      FilterOption cachedOption = new FilterOption(
              MessageManager.getString("label.cached_structures"), "-",
              VIEWS_LOCAL_PDB, false, null);
      cmb_filterOption.addItem(cachedOption);
      if (selSet == -1)
      {
        cmb_filterOption.setSelectedItem(cachedOption);
      }
    }
    if (selSet > -1)
    {
      cmb_filterOption.setSelectedIndex(selSet);
    }
    cmb_filterOption.addItemListener(this);
  }

  /**
   * Updates the displayed view based on the selected filter option
   */
  protected void updateCurrentView()
  {
    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());

    if (lastSelected == selectedFilterOpt)
    {
      // don't need to do anything, probably
      return;
    }
    // otherwise, record selection
    // and update the layout and dialog accordingly
    lastSelected = selectedFilterOpt;

    layout_switchableViews.show(pnl_switchableViews,
            selectedFilterOpt.getView());
    String filterTitle = mainFrame.getTitle();
    mainFrame.setTitle(frameTitle);
    chk_invertFilter.setVisible(false);

    if (selectedFilterOpt.getView() == VIEWS_FILTER)
    {
      mainFrame.setTitle(filterTitle);
      // TDB Query has no invert as yet
      chk_invertFilter.setVisible(selectedFilterOpt
              .getQuerySource() instanceof PDBStructureChooserQuerySource);

      if (data != selectedFilterOpt.getQuerySource()
              || data.needsRefetch(selectedFilterOpt))
      {
        data = selectedFilterOpt.getQuerySource();
        // rebuild the views completely, since prefs will also change
        tabRefresh();
        return;
      }
      else
      {
        filterResultSet(selectedFilterOpt.getValue());
      }
    }
    else if (selectedFilterOpt.getView() == VIEWS_ENTER_ID
            || selectedFilterOpt.getView() == VIEWS_FROM_FILE)
    {
      mainFrame.setTitle(MessageManager
              .getString("label.structure_chooser_manual_association"));
      idInputAssSeqPanel.loadCmbAssSeq();
      fileChooserAssSeqPanel.loadCmbAssSeq();
    }
    validateSelections();
  }

  /**
   * Validates user selection and enables the 'Add' and 'New View' buttons if
   * all parameters are correct (the Add button will only be visible if there is
   * at least one existing structure viewer open). This basically means at least
   * one structure selected and no error messages.
   * <p>
   * The 'Superpose Structures' option is enabled if either more than one
   * structure is selected, or the 'Add' to existing view option is enabled, and
   * disabled if the only option is to open a new view of a single structure.
   */
  @Override
  protected void validateSelections()
  {
    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());
    btn_add.setEnabled(false);
    String currentView = selectedFilterOpt.getView();
    int selectedCount = 0;
    if (currentView == VIEWS_FILTER)
    {
      selectedCount = getResultTable().getSelectedRows().length;
      if (selectedCount > 0)
      {
        btn_add.setEnabled(true);
      }
    }
    else if (currentView == VIEWS_LOCAL_PDB)
    {
      selectedCount = tbl_local_pdb.getSelectedRows().length;
      if (selectedCount > 0)
      {
        btn_add.setEnabled(true);
      }
    }
    else if (currentView == VIEWS_ENTER_ID)
    {
      validateAssociationEnterPdb();
    }
    else if (currentView == VIEWS_FROM_FILE)
    {
      validateAssociationFromFile();
    }

    btn_newView.setEnabled(btn_add.isEnabled());

    /*
     * enable 'Superpose' option if more than one structure is selected,
     * or there are view(s) available to add structure(s) to
     */
    chk_superpose
            .setEnabled(selectedCount > 1 || targetView.getItemCount() > 0);
  }

  @Override
  protected boolean showPopupFor(int selectedRow, int x, int y)
  {
    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());
    String currentView = selectedFilterOpt.getView();

    if (currentView == VIEWS_FILTER
            && data instanceof ThreeDBStructureChooserQuerySource)
    {

      TDB_FTSData row = ((ThreeDBStructureChooserQuerySource) data)
              .getFTSDataFor(getResultTable(), selectedRow,
                      discoveredStructuresSet);
      String pageUrl = row.getModelViewUrl();
      JPopupMenu popup = new JPopupMenu("3D Beacons");
      JMenuItem viewUrl = new JMenuItem("View model web page");
      viewUrl.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          Desktop.showUrl(pageUrl);
        }
      });
      popup.add(viewUrl);
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          popup.show(getResultTable(), x, y);
        }
      });
      return true;
    }
    // event not handled by us
    return false;
  }

  /**
   * Validates inputs from the Manual PDB entry panel
   */
  protected void validateAssociationEnterPdb()
  {
    AssociateSeqOptions assSeqOpt = (AssociateSeqOptions) idInputAssSeqPanel
            .getCmb_assSeq().getSelectedItem();
    lbl_pdbManualFetchStatus.setIcon(errorImage);
    lbl_pdbManualFetchStatus.setToolTipText("");
    if (txt_search.getText().length() > 0)
    {
      lbl_pdbManualFetchStatus.setToolTipText(JvSwingUtils.wrapTooltip(true,
              MessageManager.formatMessage("info.no_pdb_entry_found_for",
                      txt_search.getText())));
    }

    if (errorWarning.length() > 0)
    {
      lbl_pdbManualFetchStatus.setIcon(warningImage);
      lbl_pdbManualFetchStatus.setToolTipText(
              JvSwingUtils.wrapTooltip(true, errorWarning.toString()));
    }

    if (selectedSequences.length == 1 || !assSeqOpt.getName()
            .equalsIgnoreCase("-Select Associated Seq-"))
    {
      txt_search.setEnabled(true);
      if (isValidPBDEntry)
      {
        btn_add.setEnabled(true);
        lbl_pdbManualFetchStatus.setToolTipText("");
        lbl_pdbManualFetchStatus.setIcon(goodImage);
      }
    }
    else
    {
      txt_search.setEnabled(false);
      lbl_pdbManualFetchStatus.setIcon(errorImage);
    }
  }

  /**
   * Validates inputs for the manual PDB file selection options
   */
  protected void validateAssociationFromFile()
  {
    AssociateSeqOptions assSeqOpt = (AssociateSeqOptions) fileChooserAssSeqPanel
            .getCmb_assSeq().getSelectedItem();
    // lbl_fromFileStatus.setIcon(errorImage);
    String pdbFileString = "";
    String pdbFileTooltip = "";
    if (selectedSequences.length == 1 || (assSeqOpt != null && !assSeqOpt
            .getName().equalsIgnoreCase("-Select Associated Seq-")))
    {
      btn_pdbFromFile.setEnabled(true);
      if (selectedPdbFileName != null && selectedPdbFileName.length() > 0)
      {
        btn_add.setEnabled(true);
        // lbl_fromFileStatus.setIcon(goodImage);
        pdbFileString = new File(selectedPdbFileName).getName();
        pdbFileTooltip = new File(selectedPdbFileName).getAbsolutePath();
        setPdbOptionsEnabled(true);
      }
      else
      {
        pdbFileString = MessageManager.getString("label.none");
        pdbFileTooltip = MessageManager.getString("label.nothing_selected");
        setPdbOptionsEnabled(false);
      }
    }
    else
    {
      btn_pdbFromFile.setEnabled(false);
      setPdbOptionsEnabled(false);
      // lbl_fromFileStatus.setIcon(errorImage);
      pdbFileString = MessageManager.getString("label.none");
      pdbFileTooltip = MessageManager.getString("label.nothing_selected");
    }
    lbl_pdbFile.setText(pdbFileString);
    lbl_pdbFile.setToolTipText(pdbFileTooltip);

    // PAE file choice
    String paeFileString = "";
    String paeFileTooltip = "";
    if (localPdbPaeMatrixFileName != null
            && localPdbPaeMatrixFileName.length() > 0)
    {
      paeFileString = new File(localPdbPaeMatrixFileName).getName();
      paeFileTooltip = new File(localPdbPaeMatrixFileName)
              .getAbsolutePath();
    }
    else
    {
      paeFileString = MessageManager.getString("label.none");
      paeFileTooltip = MessageManager.getString("label.nothing_selected");
    }
    lbl_paeFile.setText(paeFileString);
    lbl_paeFile.setToolTipText(paeFileTooltip);
  }

  @Override
  protected void cmbAssSeqStateChanged()
  {
    validateSelections();
  }

  private FilterOption lastSelected = null;

  /**
   * Handles the state change event for the 'filter' combo-box and 'invert'
   * check-box
   */
  @Override
  protected void stateChanged(ItemEvent e)
  {
    if (e.getSource() instanceof JCheckBox)
    {
      updateCurrentView();
    }
    else
    {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
        updateCurrentView();
      }
    }

  }

  /**
   * select structures for viewing by their PDB IDs
   * 
   * @param pdbids
   * @return true if structures were found and marked as selected
   */
  public boolean selectStructure(String... pdbids)
  {
    boolean found = false;

    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());
    String currentView = selectedFilterOpt.getView();
    JTable restable = (currentView == VIEWS_FILTER) ? getResultTable()
            : (currentView == VIEWS_LOCAL_PDB) ? tbl_local_pdb : null;

    if (restable == null)
    {
      // can't select (enter PDB ID, or load file - need to also select which
      // sequence to associate with)
      return false;
    }

    int pdbIdColIndex = restable.getColumn("PDB Id").getModelIndex();
    for (int r = 0; r < restable.getRowCount(); r++)
    {
      for (int p = 0; p < pdbids.length; p++)
      {
        if (String.valueOf(restable.getValueAt(r, pdbIdColIndex))
                .equalsIgnoreCase(pdbids[p]))
        {
          restable.setRowSelectionInterval(r, r);
          found = true;
        }
      }
    }
    return found;
  }

  /**
   * Handles the 'New View' action
   */
  @Override
  protected void newView_ActionPerformed()
  {
    targetView.setSelectedItem(null);
    showStructures(false);
  }

  /**
   * Handles the 'Add to existing viewer' action
   */
  @Override
  protected void add_ActionPerformed()
  {
    showStructures(false);
  }

  /**
   * structure viewer opened by this dialog, or null
   */
  private StructureViewer sViewer = null;

  public void showStructures(boolean waitUntilFinished)
  {

    final StructureSelectionManager ssm = ap.getStructureSelectionManager();

    final int preferredHeight = pnl_filter.getHeight();
    btn_add.setEnabled(false);
    btn_newView.setEnabled(false);
    btn_cancel.setEnabled(false);
    actionsPanel.setEnabled(false);

    final String progress = MessageManager
            .getString("label.working_ellipsis");
    setProgressBar(progress, progress.hashCode());
    Runnable viewStruc = new Runnable()
    {
      @Override
      public void run()
      {
        FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
                .getSelectedItem());
        String currentView = selectedFilterOpt.getView();
        JTable restable = (currentView == VIEWS_FILTER) ? getResultTable()
                : tbl_local_pdb;

        if (currentView == VIEWS_FILTER)
        {
          int[] selectedRows = restable.getSelectedRows();
          PDBEntry[] pdbEntriesToView = new PDBEntry[selectedRows.length];
          List<SequenceI> selectedSeqsToView = new ArrayList<>();
          pdbEntriesToView = data.collectSelectedRows(restable,
                  selectedRows, selectedSeqsToView);

          SequenceI[] selectedSeqs = selectedSeqsToView
                  .toArray(new SequenceI[selectedSeqsToView.size()]);
          sViewer = launchStructureViewer(ssm, pdbEntriesToView, ap,
                  selectedSeqs);
        }
        else if (currentView == VIEWS_LOCAL_PDB)
        {
          int[] selectedRows = tbl_local_pdb.getSelectedRows();
          PDBEntry[] pdbEntriesToView = new PDBEntry[selectedRows.length];
          int count = 0;
          int pdbIdColIndex = tbl_local_pdb.getColumn("PDB Id")
                  .getModelIndex();
          int refSeqColIndex = tbl_local_pdb.getColumn("Ref Sequence")
                  .getModelIndex();
          List<SequenceI> selectedSeqsToView = new ArrayList<>();
          for (int row : selectedRows)
          {
            PDBEntry pdbEntry = ((PDBEntryTableModel) tbl_local_pdb
                    .getModel()).getPDBEntryAt(row).getPdbEntry();

            pdbEntriesToView[count++] = pdbEntry;
            SequenceI selectedSeq = (SequenceI) tbl_local_pdb
                    .getValueAt(row, refSeqColIndex);
            selectedSeqsToView.add(selectedSeq);
          }
          SequenceI[] selectedSeqs = selectedSeqsToView
                  .toArray(new SequenceI[selectedSeqsToView.size()]);
          sViewer = launchStructureViewer(ssm, pdbEntriesToView, ap,
                  selectedSeqs);
        }
        else if (currentView == VIEWS_ENTER_ID)
        {
          SequenceI userSelectedSeq = ((AssociateSeqOptions) idInputAssSeqPanel
                  .getCmb_assSeq().getSelectedItem()).getSequence();
          if (userSelectedSeq != null)
          {
            selectedSequence = userSelectedSeq;
          }
          String pdbIdStr = txt_search.getText();
          PDBEntry pdbEntry = selectedSequence.getPDBEntry(pdbIdStr);
          if (pdbEntry == null)
          {
            pdbEntry = new PDBEntry();
            if (pdbIdStr.split(":").length > 1)
            {
              pdbEntry.setId(pdbIdStr.split(":")[0]);
              pdbEntry.setChainCode(
                      pdbIdStr.split(":")[1].toUpperCase(Locale.ROOT));
            }
            else
            {
              pdbEntry.setId(pdbIdStr);
            }
            pdbEntry.setType(PDBEntry.Type.PDB);
            selectedSequence.getDatasetSequence().addPDBId(pdbEntry);
          }

          PDBEntry[] pdbEntriesToView = new PDBEntry[] { pdbEntry };
          sViewer = launchStructureViewer(ssm, pdbEntriesToView, ap,
                  new SequenceI[]
                  { selectedSequence });
        }
        else if (currentView == VIEWS_FROM_FILE)
        {
          StructureChooser sc = StructureChooser.this;
          TFType tft = (TFType) sc.combo_tempFacAs.getSelectedItem();
          String paeFilename = sc.localPdbPaeMatrixFileName;
          AssociateSeqOptions assSeqOpt = (AssociateSeqOptions) fileChooserAssSeqPanel
                  .getCmb_assSeq().getSelectedItem();
          SequenceI userSelectedSeq = assSeqOpt.getSequence();
          if (userSelectedSeq != null)
          {
            selectedSequence = userSelectedSeq;
          }
          String pdbFilename = selectedPdbFileName;

          StructureChooser.openStructureFileForSequence(ssm, sc, ap,
                  selectedSequence, true, pdbFilename, tft, paeFilename,
                  true);
        }
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            setProgressBar("Complete.", progress.hashCode());
            closeAction(preferredHeight);
            mainFrame.dispose();
          }
        });
      }
    };
    Thread runner = new Thread(viewStruc);
    runner.start();
    if (waitUntilFinished)
    {
      while (sViewer == null ? runner.isAlive()
              : (sViewer.sview == null ? true
                      : !sViewer.sview.hasMapping()))
      {
        try
        {
          Thread.sleep(300);
        } catch (InterruptedException ie)
        {

        }
      }
    }
  }

  /**
   * Answers a structure viewer (new or existing) configured to superimpose
   * added structures or not according to the user's choice
   * 
   * @param ssm
   * @return
   */
  StructureViewer getTargetedStructureViewer(StructureSelectionManager ssm)
  {
    Object sv = targetView.getSelectedItem();

    return sv == null ? new StructureViewer(ssm) : (StructureViewer) sv;
  }

  /**
   * Adds PDB structures to a new or existing structure viewer
   * 
   * @param ssm
   * @param pdbEntriesToView
   * @param alignPanel
   * @param sequences
   * @return
   */
  private StructureViewer launchStructureViewer(
          StructureSelectionManager ssm, final PDBEntry[] pdbEntriesToView,
          final AlignmentPanel alignPanel, SequenceI[] sequences)
  {
    return launchStructureViewer(ssm, pdbEntriesToView, alignPanel,
            sequences, null);
  }

  private StructureViewer launchStructureViewer(
          StructureSelectionManager ssm, final PDBEntry[] pdbEntriesToView,
          final AlignmentPanel alignPanel, SequenceI[] sequences,
          ViewerType viewerType)
  {
    long progressId = sequences.hashCode();
    setProgressBar(MessageManager
            .getString("status.launching_3d_structure_viewer"), progressId);
    final StructureViewer theViewer = getTargetedStructureViewer(ssm);
    boolean superimpose = chk_superpose.isSelected();
    theViewer.setSuperpose(superimpose);

    /*
     * remember user's choice of superimpose or not
     */
    Cache.setProperty(AUTOSUPERIMPOSE,
            Boolean.valueOf(superimpose).toString());

    setProgressBar(null, progressId);
    if (SiftsSettings.isMapWithSifts())
    {
      List<SequenceI> seqsWithoutSourceDBRef = new ArrayList<>();
      int p = 0;
      // TODO: skip PDBEntry:Sequence pairs where PDBEntry doesn't look like a
      // real PDB ID. For moment, we can also safely do this if there is already
      // a known mapping between the PDBEntry and the sequence.
      for (SequenceI seq : sequences)
      {
        PDBEntry pdbe = pdbEntriesToView[p++];
        if (pdbe != null && pdbe.getFile() != null)
        {
          StructureMapping[] smm = ssm.getMapping(pdbe.getFile());
          if (smm != null && smm.length > 0)
          {
            for (StructureMapping sm : smm)
            {
              if (sm.getSequence() == seq)
              {
                continue;
              }
            }
          }
        }
        if (seq.getPrimaryDBRefs().isEmpty())
        {
          seqsWithoutSourceDBRef.add(seq);
          continue;
        }
      }
      if (!seqsWithoutSourceDBRef.isEmpty())
      {
        int y = seqsWithoutSourceDBRef.size();
        setProgressBar(MessageManager.formatMessage(
                "status.fetching_dbrefs_for_sequences_without_valid_refs",
                y), progressId);
        SequenceI[] seqWithoutSrcDBRef = seqsWithoutSourceDBRef
                .toArray(new SequenceI[y]);
        DBRefFetcher dbRefFetcher = new DBRefFetcher(seqWithoutSrcDBRef);
        dbRefFetcher.fetchDBRefs(true);

        setProgressBar("Fetch complete.", progressId); // todo i18n
      }
    }
    if (pdbEntriesToView.length > 1)
    {
      setProgressBar(
              MessageManager.getString(
                      "status.fetching_3d_structures_for_selected_entries"),
              progressId);
      theViewer.viewStructures(pdbEntriesToView, sequences, alignPanel,
              viewerType);
    }
    else
    {
      setProgressBar(MessageManager.formatMessage(
              "status.fetching_3d_structures_for",
              pdbEntriesToView[0].getId()), progressId);
      // Can we pass a pre-computeMappinged pdbFile?
      theViewer.viewStructures(pdbEntriesToView[0], sequences, alignPanel,
              viewerType);
    }
    setProgressBar(null, progressId);
    // remember the last viewer we used...
    lastTargetedView = theViewer;
    return theViewer;
  }

  /**
   * Populates the combo-box used in associating manually fetched structures to
   * a unique sequence when more than one sequence selection is made.
   */
  @Override
  protected void populateCmbAssociateSeqOptions(
          JComboBox<AssociateSeqOptions> cmb_assSeq,
          JLabel lbl_associateSeq)
  {
    cmb_assSeq.removeAllItems();
    cmb_assSeq.addItem(
            new AssociateSeqOptions("-Select Associated Seq-", null));
    lbl_associateSeq.setVisible(false);
    if (selectedSequences.length > 1)
    {
      for (SequenceI seq : selectedSequences)
      {
        cmb_assSeq.addItem(new AssociateSeqOptions(seq));
      }
    }
    else
    {
      String seqName = selectedSequence.getDisplayId(false);
      seqName = seqName.length() <= 40 ? seqName : seqName.substring(0, 39);
      lbl_associateSeq.setText(seqName);
      lbl_associateSeq.setVisible(true);
      cmb_assSeq.setVisible(false);
    }
  }

  protected boolean isStructuresDiscovered()
  {
    return discoveredStructuresSet != null
            && !discoveredStructuresSet.isEmpty();
  }

  protected int PDB_ID_MIN = 3;// or: (Jalview.isJS() ? 3 : 1); // Bob proposes
                               // this.
  // Doing a search for "1" or "1c" is valuable?
  // Those work but are enormously slow.

  @Override
  protected void txt_search_ActionPerformed()
  {
    String text = txt_search.getText().trim();
    if (text.length() >= PDB_ID_MIN)
      new Thread()
      {

        @Override
        public void run()
        {
          errorWarning.setLength(0);
          isValidPBDEntry = false;
          if (text.length() > 0)
          {
            // TODO move this pdb id search into the PDB specific
            // FTSSearchEngine
            // for moment, it will work fine as is because it is self-contained
            String searchTerm = text.toLowerCase(Locale.ROOT);
            searchTerm = searchTerm.split(":")[0];
            // System.out.println(">>>>> search term : " + searchTerm);
            List<FTSDataColumnI> wantedFields = new ArrayList<>();
            FTSRestRequest pdbRequest = new FTSRestRequest();
            pdbRequest.setAllowEmptySeq(false);
            pdbRequest.setResponseSize(1);
            pdbRequest.setFieldToSearchBy("(pdb_id:");
            pdbRequest.setWantedFields(wantedFields);
            pdbRequest.setSearchTerm(searchTerm + ")");
            pdbRequest.setAssociatedSequence(selectedSequence);
            FTSRestClientI pdbRestClient = PDBFTSRestClient.getInstance();
            wantedFields.add(pdbRestClient.getPrimaryKeyColumn());
            FTSRestResponse resultList;
            try
            {
              resultList = pdbRestClient.executeRequest(pdbRequest);
            } catch (Exception e)
            {
              errorWarning.append(e.getMessage());
              return;
            } finally
            {
              validateSelections();
            }
            if (resultList.getSearchSummary() != null
                    && resultList.getSearchSummary().size() > 0)
            {
              isValidPBDEntry = true;
            }
          }
          validateSelections();
        }
      }.start();
  }

  @Override
  protected void tabRefresh()
  {
    if (selectedSequences != null)
    {
      lbl_loading.setVisible(true);
      Thread refreshThread = new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          fetchStructuresMetaData();
          // populateFilterComboBox(true, cachedPDBExists);

          filterResultSet(
                  ((FilterOption) cmb_filterOption.getSelectedItem())
                          .getValue());
          lbl_loading.setVisible(false);
        }
      });
      refreshThread.start();
    }
  }

  public class PDBEntryTableModel extends AbstractTableModel
  {
    String[] columns = { "Ref Sequence", "PDB Id", "Chain", "Type",
        "File" };

    private List<CachedPDB> pdbEntries;

    public PDBEntryTableModel(List<CachedPDB> pdbEntries)
    {
      this.pdbEntries = new ArrayList<>(pdbEntries);
    }

    @Override
    public String getColumnName(int columnIndex)
    {
      return columns[columnIndex];
    }

    @Override
    public int getRowCount()
    {
      return pdbEntries.size();
    }

    @Override
    public int getColumnCount()
    {
      return columns.length;
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
      return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      Object value = "??";
      CachedPDB entry = pdbEntries.get(rowIndex);
      switch (columnIndex)
      {
      case 0:
        value = entry.getSequence();
        break;
      case 1:
        value = entry.getQualifiedId();
        break;
      case 2:
        value = entry.getPdbEntry().getChainCode() == null ? "_"
                : entry.getPdbEntry().getChainCode();
        break;
      case 3:
        value = entry.getPdbEntry().getType();
        break;
      case 4:
        value = entry.getPdbEntry().getFile();
        break;
      }
      return value;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return columnIndex == 0 ? SequenceI.class : PDBEntry.class;
    }

    public CachedPDB getPDBEntryAt(int row)
    {
      return pdbEntries.get(row);
    }

  }

  private class CachedPDB
  {
    private SequenceI sequence;

    private PDBEntry pdbEntry;

    public CachedPDB(SequenceI sequence, PDBEntry pdbEntry)
    {
      this.sequence = sequence;
      this.pdbEntry = pdbEntry;
    }

    public String getQualifiedId()
    {
      if (pdbEntry.hasProvider())
      {
        return pdbEntry.getProvider() + ":" + pdbEntry.getId();
      }
      return pdbEntry.toString();
    }

    public SequenceI getSequence()
    {
      return sequence;
    }

    public PDBEntry getPdbEntry()
    {
      return pdbEntry;
    }

  }

  private IProgressIndicator progressBar;

  @Override
  public void setProgressBar(String message, long id)
  {
    if (!Platform.isHeadless() && progressBar != null)
      progressBar.setProgressBar(message, id);
  }

  @Override
  public void registerHandler(long id, IProgressIndicatorHandler handler)
  {
    if (progressBar != null)
      progressBar.registerHandler(id, handler);
  }

  @Override
  public boolean operationInProgress()
  {
    return progressBar == null ? false : progressBar.operationInProgress();
  }

  public JalviewStructureDisplayI getOpenedStructureViewer()
  {
    return sViewer == null ? null : sViewer.sview;
  }

  @Override
  protected void setFTSDocFieldPrefs(FTSDataColumnPreferences newPrefs)
  {
    data.setDocFieldPrefs(newPrefs);

  }

  /**
   * 
   * @return true when all initialisation threads have finished and dialog is
   *         visible
   */
  public boolean isDialogVisible()
  {
    return mainFrame != null && data != null && cmb_filterOption != null
            && mainFrame.isVisible()
            && cmb_filterOption.getSelectedItem() != null;
  }

  /**
   * 
   * @return true if the 3D-Beacons query button will/has been displayed
   */
  public boolean isCanQueryTDB()
  {
    return canQueryTDB;
  }

  public boolean isNotQueriedTDBYet()
  {
    return notQueriedTDBYet;
  }

  /**
   * Open a single structure file for a given sequence
   */
  public static void openStructureFileForSequence(
          StructureSelectionManager ssm, StructureChooser sc,
          AlignmentPanel ap, SequenceI seq, boolean prompt,
          String sFilename, TFType tft, String paeFilename,
          boolean doXferSettings)
  {
    openStructureFileForSequence(ssm, sc, ap, seq, prompt, sFilename, tft,
            paeFilename, false, true, doXferSettings, null);
  }

  public static StructureViewer openStructureFileForSequence(
          StructureSelectionManager ssm, StructureChooser sc,
          AlignmentPanel ap, SequenceI seq, boolean prompt,
          String sFilename, TFType tft, String paeFilename,
          boolean forceHeadless, boolean showRefAnnotations,
          boolean doXferSettings, ViewerType viewerType)
  {
    StructureViewer sv = null;
    boolean headless = forceHeadless;
    if (sc == null)
    {
      // headless = true;
      prompt = false;
      sc = new StructureChooser(new SequenceI[] { seq }, seq, ap, false);
    }
    if (ssm == null)
    {
      ssm = ap.getStructureSelectionManager();
    }

    PDBEntry fileEntry = new AssociatePdbFileWithSeq().associatePdbWithSeq(
            sFilename, DataSourceType.FILE, seq, prompt, Desktop.instance,
            tft, paeFilename, doXferSettings);

    // if headless, "false" in the sc constructor above will avoid GUI behaviour
    // in sc.launchStructureViewer()
    if (!headless && !(viewerType == null))
    {
      sv = sc.launchStructureViewer(ssm, new PDBEntry[] { fileEntry }, ap,
              new SequenceI[]
              { seq }, viewerType);
    }

    sc.mainFrame.dispose();

    if (showRefAnnotations)
      showReferenceAnnotationsForSequence(ap.alignFrame, seq);

    return sv;
  }

  public static void showReferenceAnnotationsForSequence(AlignFrame af,
          SequenceI sequence)
  {
    AlignViewport av = af.getCurrentView();
    AlignmentI al = av.getAlignment();

    List<SequenceI> forSequences = new ArrayList<>();
    forSequences.add(sequence);
    final Map<SequenceI, List<AlignmentAnnotation>> candidates = new LinkedHashMap<>();
    AlignmentUtils.findAddableReferenceAnnotations(forSequences, null,
            candidates, al);
    final SequenceGroup selectionGroup = av.getSelectionGroup();
    AlignmentUtils.addReferenceAnnotations(candidates, al, selectionGroup);
    for (AlignmentViewPanel ap : af.getAlignPanels())
    {
      // required to readjust the height and position of the PAE
      // annotation
      ap.adjustAnnotationHeight();
    }

  }
  
  @Override
  public JProgressBar getProgressBar(long id)
  {
    return progressBar.getProgressBar(id);
  }
}
