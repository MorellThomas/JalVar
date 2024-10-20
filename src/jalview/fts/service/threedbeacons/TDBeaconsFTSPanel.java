/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.3.2)
 * Copyright (C) 2023 The Jalview Authors
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
package jalview.fts.service.threedbeacons;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.SwingUtilities;

import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.core.GFTSPanel;
import jalview.gui.SequenceFetcher;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;
import jalview.util.MessageManager;

@SuppressWarnings("serial")
public class TDBeaconsFTSPanel extends GFTSPanel
{
  private static String defaultFTSFrameTitle = MessageManager
          .getString("label.pdb_sequence_fetcher");

  private static Map<String, Integer> tempUserPrefs = new HashMap<>();

  private static final String THREEDB_FTS_CACHE_KEY = "CACHE.THREEDB_FTS";

  private static final String THREEDB_AUTOSEARCH = "FTS.THREEDB.AUTOSEARCH";

  private static HttpURLConnection connection;

  public TDBeaconsFTSPanel(SequenceFetcher fetcher)
  {
    // no ID retrieval option for TD Beacons just now
    super(null);
    pageLimit = TDBeaconsFTSRestClient.getInstance()
            .getDefaultResponsePageSize();
    this.seqFetcher = fetcher;
    this.progressIndicator = (fetcher == null) ? null
            : fetcher.getProgressIndicator();
  }

  @Override
  public void searchAction(boolean isFreshSearch)
  {
    mainFrame.requestFocusInWindow();
    if (isFreshSearch)
    {
      offSet = 0;
    }
    new Thread()
    {
      @Override
      public void run()
      {
        reset();
        boolean allowEmptySequence = false;
        if (getTypedText().length() > 0)
        {
          setSearchInProgress(true);
          long startTime = System.currentTimeMillis();

          String searchTarget = ((FTSDataColumnI) cmb_searchTarget
                  .getSelectedItem()).getCode();
          wantedFields = TDBeaconsFTSRestClient.getInstance()
                  .getAllDefaultDisplayedFTSDataColumns();
          String searchTerm = getTypedText(); // to add : decodeSearchTerm

          FTSRestRequest request = new FTSRestRequest();
          request.setAllowEmptySeq(allowEmptySequence);
          request.setResponseSize(100);
          // expect it to be uniprot accesssion
          request.setSearchTerm(searchTerm + ".json");
          request.setOffSet(offSet);
          request.setWantedFields(wantedFields);
          FTSRestClientI tdbRestClient = TDBeaconsFTSRestClient
                  .getInstance();
          FTSRestResponse resultList;
          try
          {
            resultList = tdbRestClient.executeRequest(request);
          } catch (Exception e)
          {
            setErrorMessage(e.getMessage());
            checkForErrors();
            setSearchInProgress(false);
            return;
          }

          if (resultList.getSearchSummary() != null
                  && resultList.getSearchSummary().size() > 0)
          {
            getResultTable().setModel(FTSRestResponse.getTableModel(request,
                    resultList.getSearchSummary()));
            FTSRestResponse.configureTableColumn(getResultTable(),
                    wantedFields, tempUserPrefs);
            getResultTable().setVisible(true);
          }

          long endTime = System.currentTimeMillis();
          totalResultSetCount = resultList.getNumberOfItemsFound();
          resultSetCount = resultList.getSearchSummary() == null ? 0
                  : resultList.getSearchSummary().size();
          String result = (resultSetCount > 0)
                  ? MessageManager.getString("label.results")
                  : MessageManager.getString("label.result");

          if (isPaginationEnabled() && resultSetCount > 0)
          {
            String f1 = totalNumberformatter
                    .format(Integer.valueOf(offSet + 1));
            String f2 = totalNumberformatter
                    .format(Integer.valueOf(offSet + resultSetCount));
            String f3 = totalNumberformatter
                    .format(Integer.valueOf(totalResultSetCount));
            updateSearchFrameTitle(defaultFTSFrameTitle + " - " + result
                    + " " + f1 + " to " + f2 + " of " + f3 + " " + " ("
                    + (endTime - startTime) + " milli secs)");
          }
          else
          {
            updateSearchFrameTitle(defaultFTSFrameTitle + " - "
                    + resultSetCount + " " + result + " ("
                    + (endTime - startTime) + " milli secs)");
          }

          setSearchInProgress(false);
          refreshPaginatorState();
          updateSummaryTableSelections();
        }
        txt_search.updateCache();
      }
    }.start();
  }

  @Override
  public void okAction()
  {
    // mainFrame.dispose();
    disableActionButtons();
    StringBuilder selectedIds = new StringBuilder();
    final HashSet<String> selectedIdsSet = new HashSet<>();
    int primaryKeyColIndex = 0;
    try
    {
      primaryKeyColIndex = getFTSRestClient()
              .getPrimaryKeyColumIndex(wantedFields, false);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    int[] selectedRows = getResultTable().getSelectedRows();
    String searchTerm = getTypedText();
    for (int summaryRow : selectedRows)
    {
      String idStr = getResultTable()
              .getValueAt(summaryRow, primaryKeyColIndex).toString();
      selectedIdsSet.add(idStr);
    }

    for (String idStr : paginatorCart)
    {
      selectedIdsSet.add(idStr);
    }

    for (String selectedId : selectedIdsSet)
    {
      selectedIds.append(selectedId).append(";");
    }

    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        AlignmentI allSeqs = null;
        FormatAdapter fl = new jalview.io.FormatAdapter();
        for (String tdbURL : selectedIdsSet)
        {
          try
          {
            // retrieve the structure via its URL
            AlignmentI tdbAl = fl.readFile(tdbURL, DataSourceType.URL,
                    FileFormat.MMCif);

            // TODO: pad structure according to its Uniprot Start so all line up
            // w.r.t. the Uniprot reference sequence
            // TODO: give the structure a sensible name (not the giant URL *:o)
            // )
            if (tdbAl != null)
            {
              if (allSeqs != null)
              {
                allSeqs.append(tdbAl);
              }
              else
              {
                allSeqs = tdbAl;
              }
            }
          } catch (Exception x)
          {
            Console.warn("Couldn't retrieve 3d-beacons model for uniprot id"
                    + searchTerm + " : " + tdbURL, x);
          }
        }
        seqFetcher.parseResult(allSeqs,
                "3D-Beacons models for " + searchTerm, FileFormat.MMCif,
                null);

      }
    });
    delayAndEnableActionButtons();
  }

  @Override
  public FTSRestClientI getFTSRestClient()
  {
    return TDBeaconsFTSRestClient.getInstance();
  }

  @Override
  public String getFTSFrameTitle()
  {
    return defaultFTSFrameTitle;
  }

  @Override
  public boolean isPaginationEnabled()
  {
    return true;
  }

  @Override
  public Map<String, Integer> getTempUserPrefs()
  {
    return tempUserPrefs;
  }

  @Override
  public String getCacheKey()
  {
    return THREEDB_FTS_CACHE_KEY;
  }

  @Override
  public String getAutosearchPreference()
  {
    return THREEDB_AUTOSEARCH;
  }

  @Override
  protected void showHelp()
  {
    jalview.bin.Console.outPrintln("No help implemented yet.");

  }

  public static String decodeSearchTerm(String enteredText)
  {
    // no multiple query support yet
    return enteredText;
  }

  public String getDbName()
  {
    return "3D-Beacons";
  }
}
