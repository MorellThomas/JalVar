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

package jalview.fts.service.pdb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.help.HelpSetException;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.core.GFTSPanel;
import jalview.gui.Help;
import jalview.gui.Help.HelpId;
import jalview.gui.SequenceFetcher;
import jalview.util.MessageManager;

@SuppressWarnings("serial")
public class PDBFTSPanel extends GFTSPanel
{
  private static String defaultFTSFrameTitle = MessageManager
          .getString("label.pdb_sequence_fetcher");

  private static Map<String, Integer> tempUserPrefs = new HashMap<>();

  private static final String PDB_FTS_CACHE_KEY = "CACHE.PDB_FTS";

  private static final String PDB_AUTOSEARCH = "FTS.PDB.AUTOSEARCH";

  public PDBFTSPanel(SequenceFetcher fetcher)
  {
    super(fetcher);
    pageLimit = PDBFTSRestClient.getInstance().getDefaultResponsePageSize();
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
          wantedFields = PDBFTSRestClient.getInstance()
                  .getAllDefaultDisplayedFTSDataColumns();
          String searchTerm = decodeSearchTerm(getTypedText(),
                  searchTarget);

          FTSRestRequest request = new FTSRestRequest();
          request.setAllowEmptySeq(allowEmptySequence);
          request.setResponseSize(100);
          request.setFieldToSearchBy("(" + searchTarget + ":");
          request.setSearchTerm(searchTerm + ")");
          request.setOffSet(offSet);
          request.setWantedFields(wantedFields);
          FTSRestClientI pdbRestClient = PDBFTSRestClient.getInstance();
          FTSRestResponse resultList;
          try
          {
            resultList = pdbRestClient.executeRequest(request);
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

  public static String decodeSearchTerm(String enteredText,
          String targetField)
  {
    String foundSearchTerms = enteredText;
    StringBuilder foundSearchTermsBuilder = new StringBuilder();
    if (enteredText.contains(";"))
    {
      String[] searchTerms = enteredText.split(";");
      for (String searchTerm : searchTerms)
      {
        if (searchTerm.contains(":"))
        {
          foundSearchTermsBuilder.append(targetField).append(":")
                  .append(searchTerm.split(":")[0]).append(" OR ");
        }
        else
        {
          foundSearchTermsBuilder.append(targetField).append(":")
                  .append(searchTerm).append(" OR ");
        }
      }
      int endIndex = foundSearchTermsBuilder.lastIndexOf(" OR ");
      foundSearchTerms = foundSearchTermsBuilder.toString();
      if (foundSearchTerms.contains(" OR "))
      {
        foundSearchTerms = foundSearchTerms
                .substring(targetField.length() + 1, endIndex);
      }
    }
    else if (enteredText.contains(":"))
    {
      foundSearchTerms = foundSearchTerms.split(":")[0];
    }
    return foundSearchTerms;
  }

  @Override
  public void okAction()
  {
    // mainFrame.dispose();
    disableActionButtons();
    StringBuilder selectedIds = new StringBuilder();
    HashSet<String> selectedIdsSet = new HashSet<>();
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
      selectedIdsSet.add(getPDBIdwithSpecifiedChain(idStr, searchTerm));
    }

    for (String idStr : paginatorCart)
    {
      selectedIdsSet.add(getPDBIdwithSpecifiedChain(idStr, searchTerm));
    }

    for (String selectedId : selectedIdsSet)
    {
      selectedIds.append(selectedId).append(";");
    }

    String ids = selectedIds.toString();
    seqFetcher.setQuery(ids);
    Thread worker = new Thread(seqFetcher);
    worker.start();
    delayAndEnableActionButtons();
  }

  public static String getPDBIdwithSpecifiedChain(String pdbId,
          String searchTerm)
  {
    String pdbIdWithChainCode = "";
    if (searchTerm.contains(";"))
    {
      String[] foundTerms = searchTerm.split(";");
      for (String foundTerm : foundTerms)
      {
        if (foundTerm.contains(pdbId))
        {
          pdbIdWithChainCode = foundTerm;
        }
      }
    }
    else if (searchTerm.contains(pdbId))
    {
      pdbIdWithChainCode = searchTerm;
    }
    else
    {
      pdbIdWithChainCode = pdbId;
    }
    return pdbIdWithChainCode;
  }

  @Override
  public FTSRestClientI getFTSRestClient()
  {
    return PDBFTSRestClient.getInstance();
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
    return PDB_FTS_CACHE_KEY;
  }

  @Override
  public String getAutosearchPreference()
  {
    return PDB_AUTOSEARCH;
  }

  @Override
  protected void showHelp()
  {
    try
    {
      Help.showHelpWindow(HelpId.PdbFts);
    } catch (HelpSetException e1)
    {
      e1.printStackTrace();
    }
  }

  public String getDbName()
  {
    return "PDB";
  }
}