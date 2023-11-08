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
package jalview.gui.structurechooser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JTable;

import jalview.bin.Console;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSDataColumnPreferences;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.service.threedbeacons.TDB_FTSData;
import jalview.fts.service.threedbeacons.TDBeaconsFTSRestClient;
import jalview.jbgui.FilterOption;

/**
 * logic for querying the 3DBeacons API for structures of sequences
 * 
 * @author jprocter
 */
public class ThreeDBStructureChooserQuerySource
        extends StructureChooserQuerySource
{

  private Set<String> tdBeaconsFilters = null, defaultFilters = null;

  public static final String FILTER_TDBEACONS_COVERAGE = "3d_beacons_coverage";

  public static final String FILTER_FIRST_BEST_COVERAGE = "3d_beacons_first_best_coverage";

  private static final String FILTER_SOURCE_PREFIX = "only_";

  protected FTSRestRequest lastTdbRequest;

  protected FTSRestClientI tdbRestClient;

  private FTSRestRequest lastPdbRequest;

  public ThreeDBStructureChooserQuerySource()
  {
    defaultFilters = new LinkedHashSet<String>();
    defaultFilters.add(FILTER_TDBEACONS_COVERAGE);
    defaultFilters.add(FILTER_FIRST_BEST_COVERAGE);

    tdbRestClient = TDBeaconsFTSRestClient.getInstance();
    docFieldPrefs = new FTSDataColumnPreferences(
            PreferenceSource.STRUCTURE_CHOOSER,
            TDBeaconsFTSRestClient.getInstance());
  }

  /**
   * Builds a query string for a given sequences using its DBRef entries 3d
   * Beacons is only useful for uniprot IDs
   * 
   * @param seq
   *          the sequences to build a query for
   * @return the built query string
   */

  @Override
  public String buildQuery(SequenceI seq)
  {
    List<DBRefEntry> refs = seq.getDBRefs();
    int ib = checkUniprotRefs(refs);
    if (ib > -1)
    {
      return getDBRefId(refs.get(ib));
    }
    return null;
  }

  /**
   * Searches DBRefEntry for uniprot refs
   * 
   * @param seq
   * @return -2 if no uniprot refs, -1 if no canonical ref., otherwise index of
   *         Uniprot canonical DBRefEntry
   */
  public static int checkUniprotRefs(List<DBRefEntry> refs)
  {
    boolean hasUniprot = false;
    if (refs != null && refs.size() != 0)
    {
      for (int ib = 0, nb = refs.size(); ib < nb; ib++)
      {
        DBRefEntry dbRef = refs.get(ib);
        if (dbRef.getSource().equalsIgnoreCase(DBRefSource.UNIPROT))
        {
          hasUniprot = true;
          if (dbRef.isCanonical())
          {
            return ib;
          }
        }
      }
    }
    return hasUniprot ? -1 : -2;
  }

  /**
   * Ensures sequence ref names are not less than 3 characters and does not
   * contain a database name
   * 
   * @param seqName
   * @return
   */
  static boolean isValidSeqName(String seqName)
  {
    String ignoreList = "pdb,uniprot,swiss-prot";
    if (seqName.length() < 3)
    {
      return false;
    }
    if (seqName.contains(":"))
    {
      return false;
    }
    seqName = seqName.toLowerCase(Locale.ROOT);
    for (String ignoredEntry : ignoreList.split(","))
    {
      if (seqName.contains(ignoredEntry))
      {
        return false;
      }
    }
    return true;
  }

  static String getDBRefId(DBRefEntry dbRef)
  {
    String ref = dbRef.getAccessionId().replaceAll("GO:", "");
    return ref;
  }

  /**
   * FTSRestClient specific query builder to recover associated structure data
   * records for a sequence
   * 
   * @param seq
   *          - seq to generate a query for
   * @param wantedFields
   *          - fields to retrieve
   * @param selectedFilterOpt
   *          - criterion for ranking results (e.g. resolution)
   * @param b
   *          - sort ascending or descending
   * @return
   * @throws Exception
   */
  @Override
  public FTSRestResponse fetchStructuresMetaData(SequenceI seq,
          Collection<FTSDataColumnI> wantedFields,
          FilterOption selectedFilterOpt, boolean b) throws Exception
  {
    FTSRestResponse resultList;
    if (selectedFilterOpt != null
            && tdBeaconsFilter(selectedFilterOpt.getValue()))
    {
      FTSRestRequest tdbRequest = getTDBeaconsRequest(seq, wantedFields);
      resultList = tdbRestClient.executeRequest(tdbRequest);

      lastTdbRequest = tdbRequest;
      if (resultList != null)
      { // Query the PDB and add additional metadata
        List<FTSRestResponse> pdbResponse = fetchStructuresMetaDataFor(
                getPDBQuerySource(), resultList);

        resultList = joinResponses(resultList, pdbResponse);
      }
      return resultList;
    }
    // use the PDBFTS directly
    resultList = getPDBQuerySource().fetchStructuresMetaData(seq,
            wantedFields, selectedFilterOpt, b);
    lastTdbRequest = getPDBQuerySource().lastPdbRequest;
    lastPdbRequest = lastTdbRequest; // both queries the same - indicates we
    // rank using PDBe
    return resultList;

  }

  PDBStructureChooserQuerySource pdbQuerySource = null;

  private PDBStructureChooserQuerySource getPDBQuerySource()
  {
    if (pdbQuerySource == null)
    {
      pdbQuerySource = new PDBStructureChooserQuerySource();
    }
    return pdbQuerySource;
  }

  private FTSRestRequest getTDBeaconsRequest(SequenceI seq,
          Collection<FTSDataColumnI> wantedFields)
  {
    FTSRestRequest pdbRequest = new FTSRestRequest();
    pdbRequest.setAllowEmptySeq(false);
    pdbRequest.setResponseSize(500);
    pdbRequest.setWantedFields(wantedFields);
    String query = buildQuery(seq);
    if (query == null)
    {
      return null;
    }
    pdbRequest.setSearchTerm(query + ".json");
    pdbRequest.setAssociatedSequence(seq);
    return pdbRequest;
  }

  @Override
  public List<FilterOption> getAvailableFilterOptions(String VIEWS_FILTER)
  {
    List<FilterOption> filters = getPDBQuerySource()
            .getAvailableFilterOptions(VIEWS_FILTER);
    tdBeaconsFilters = new LinkedHashSet<String>();
    tdBeaconsFilters.addAll(defaultFilters);
    filters.add(0, new FilterOption("Best 3D-Beacons Coverage",
            FILTER_FIRST_BEST_COVERAGE, VIEWS_FILTER, false, this));
    filters.add(1, new FilterOption("Multiple 3D-Beacons Coverage",
            FILTER_TDBEACONS_COVERAGE, VIEWS_FILTER, true, this));

    return filters;
  }

  @Override
  public void updateAvailableFilterOptions(String VIEWS_FILTER,
          List<FilterOption> xtantOptions, Collection<FTSData> tdbEntries)
  {
    if (tdbEntries != null && lastTdbRequest != null)
    {
      boolean hasPDBe = false;
      for (FTSData _row : tdbEntries)
      {
        // tdb returns custom object
        TDB_FTSData row = (TDB_FTSData) _row;
        String provider = row.getProvider();
        FilterOption providerOpt = new FilterOption(
                "3DB Provider - " + provider,
                FILTER_SOURCE_PREFIX + provider, VIEWS_FILTER, false, this);
        if (!xtantOptions.contains(providerOpt))
        {
          xtantOptions.add(1, providerOpt);
          tdBeaconsFilters.add(FILTER_SOURCE_PREFIX + provider);
          if ("PDBe".equalsIgnoreCase(provider))
          {
            hasPDBe = true;
          }
        }
      }
      if (!hasPDBe)
      {
        // remove the PDBe options from the available filters
        int op = 0;
        while (op < xtantOptions.size())
        {
          FilterOption filter = xtantOptions.get(op);
          if (filter
                  .getQuerySource() instanceof PDBStructureChooserQuerySource)
          {
            xtantOptions.remove(op);
          }
          else
          {
            op++;
          }
        }
      }
    }

  }

  private boolean tdBeaconsFilter(String fieldToFilterBy)
  {
    return tdBeaconsFilters != null
            && tdBeaconsFilters.contains(fieldToFilterBy);
  }

  private String remove_prefix(String fieldToFilterBy)
  {
    if (tdBeaconsFilters != null
            && tdBeaconsFilters.contains(fieldToFilterBy)
            && !defaultFilters.contains(fieldToFilterBy))
    {
      return fieldToFilterBy.substring(FILTER_SOURCE_PREFIX.length());
    }
    else
    {
      return null;
    }
  }

  @Override
  public boolean needsRefetch(FilterOption selectedFilterOpt)
  {
    return selectedFilterOpt == null
            || !tdBeaconsFilter(selectedFilterOpt.getValue())
                    && lastPdbRequest != lastTdbRequest;
  }

  /**
   * FTSRestClient specific query builder to pick top ranked entry from a
   * fetchStructuresMetaData query
   * 
   * @param seq
   *          - seq to generate a query for
   * @param wantedFields
   *          - fields to retrieve
   * @param selectedFilterOpt
   *          - criterion for ranking results (e.g. resolution)
   * @param b
   *          - sort ascending or descending
   * @return
   * @throws Exception
   */
  @Override
  public FTSRestResponse selectFirstRankedQuery(SequenceI seq,
          Collection<FTSData> collectedResults,
          Collection<FTSDataColumnI> wantedFields, String fieldToFilterBy,
          boolean b) throws Exception
  {
    if (fieldToFilterBy != null && tdBeaconsFilter(fieldToFilterBy))
    {
      TDBResultAnalyser analyser = new TDBResultAnalyser(seq,
              collectedResults, lastTdbRequest, fieldToFilterBy,
              remove_prefix(fieldToFilterBy));

      FTSRestResponse resultList = new FTSRestResponse();

      List<FTSData> filteredResponse = analyser.getFilteredResponse();

      List<FTSData> selectedStructures = analyser
              .selectStructures(filteredResponse);
      resultList.setNumberOfItemsFound(selectedStructures.size());
      resultList.setSearchSummary(selectedStructures);
      return resultList;
    }
    // Fall back to PDBe rankings
    return getPDBQuerySource().selectFirstRankedQuery(seq, collectedResults,
            wantedFields, fieldToFilterBy, b);
  }

  @Override
  public PDBEntry[] collectSelectedRows(JTable restable, int[] selectedRows,
          List<SequenceI> selectedSeqsToView)
  {
    int refSeqColIndex = restable.getColumn("Ref Sequence").getModelIndex();

    PDBEntry[] pdbEntriesToView = new PDBEntry[selectedRows.length];
    int count = 0;
    int idColumnIndex = restable.getColumn("Model id").getModelIndex();
    int urlColumnIndex = restable.getColumn("Url").getModelIndex();
    int typeColumnIndex = restable.getColumn("Provider").getModelIndex();
    int humanUrl = restable.getColumn("Page URL").getModelIndex();
    int modelformat = restable.getColumn("Model Format").getModelIndex();
    final int up_start_idx = restable.getColumn("Uniprot Start")
            .getModelIndex();
    final int up_end_idx = restable.getColumn("Uniprot End")
            .getModelIndex();
    int i = 0;

    // bleugh!
    Integer[] sellist = new Integer[selectedRows.length];
    for (Integer row : selectedRows)
    {
      sellist[i++] = row;
    }
    // Sort rows by coverage
    Arrays.sort(sellist, new Comparator<Integer>()
    {
      @Override
      public int compare(Integer o1, Integer o2)
      {
        int o1_xt = ((Integer) restable.getValueAt(o1, up_end_idx))
                - (Integer) restable.getValueAt(o1, up_start_idx);
        int o2_xt = ((Integer) restable.getValueAt(o2, up_end_idx))
                - (Integer) restable.getValueAt(o2, up_start_idx);
        return o2_xt - o1_xt;
      }
    });

    for (int row : sellist)
    {
      // unique id - could be a horrible hash

      String pdbIdStr = restable.getValueAt(row, idColumnIndex).toString();
      String urlStr = restable.getValueAt(row, urlColumnIndex).toString();
      String typeColumn = restable.getValueAt(row, typeColumnIndex)
              .toString();
      String modelPage = humanUrl < 1 ? null
              : (String) restable.getValueAt(row, humanUrl);
      String strucFormat = restable.getValueAt(row, modelformat).toString();

      SequenceI selectedSeq = (SequenceI) restable.getValueAt(row,
              refSeqColIndex);
      selectedSeqsToView.add(selectedSeq);
      PDBEntry pdbEntry = selectedSeq.getPDBEntry(pdbIdStr);
      if (pdbEntry == null)
      {
        pdbEntry = getFindEntry(pdbIdStr, selectedSeq.getAllPDBEntries());
      }

      if (pdbEntry == null)
      {
        pdbEntry = new PDBEntry();
        pdbEntry.setId(pdbIdStr);
        pdbEntry.setAuthoritative(true);
        try
        {
          pdbEntry.setType(PDBEntry.Type.valueOf(strucFormat));
        } catch (Exception q)
        {
          Console.warn("Unknown filetype for 3D Beacons Model from: "
                  + strucFormat + " - " + pdbIdStr + " - " + modelPage);
        }

        if (!"PDBe".equalsIgnoreCase(typeColumn))
        {
          pdbEntry.setRetrievalUrl(urlStr);
        }
        pdbEntry.setProvider(typeColumn);
        if (modelPage != null)
        {
          pdbEntry.setProviderPage(modelPage);
        }
        selectedSeq.getDatasetSequence().addPDBId(pdbEntry);
      }
      pdbEntriesToView[count++] = pdbEntry;
    }
    return pdbEntriesToView;
  }

  @Override
  protected FTSRestRequest getLastFTSRequest()
  {
    return lastTdbRequest;
  }

  /**
   * generate a query for PDBFTS to retrieve structure metadata
   * 
   * @param ftsRestRequest
   * @param upResponse
   * @return
   */

  public List<String> buildPDBFTSQueryFor(FTSRestResponse upResponse)
  {
    List<String> ftsQueries = new ArrayList<String>();
    Set<String> pdbIds = new HashSet<String>();
    int idx_modelId = getLastFTSRequest().getFieldIndex("Model id");
    int idx_provider = getLastFTSRequest().getFieldIndex("Provider");
    for (FTSData row : upResponse.getSearchSummary())
    {
      String id = (String) row.getSummaryData()[idx_modelId];
      String provider = (String) row.getSummaryData()[idx_provider];
      if ("PDBe".equalsIgnoreCase(provider))
      {
        pdbIds.add(id);
      }
    }
    StringBuilder sb = new StringBuilder();
    for (String pdbId : pdbIds)
    {
      if (sb.length() > 2500)
      {
        ftsQueries.add(sb.toString());
        sb.setLength(0);
      }
      if (sb.length() > 0)
      {
        sb.append(" OR ");
      }
      sb.append(pdbId);
    }
    if (sb.length() > 0)
    {
      ftsQueries.add(sb.toString());
    }
    return ftsQueries;
  }

  /**
   * query PDBe for structure metadata
   * 
   * @param pdbquery
   * @param upResponse
   * @return FTSRestResponse via PDBStructureChooserQuerySource
   */
  public List<FTSRestResponse> fetchStructuresMetaDataFor(
          PDBStructureChooserQuerySource pdbquery,
          FTSRestResponse upResponse) throws Exception
  {
    List<String> pdb_Queries = buildPDBFTSQueryFor(upResponse);
    if (pdb_Queries.size() == 0)
    {
      return null;
    }
    List<FTSRestResponse> results = new ArrayList<FTSRestResponse>();

    for (String pdb_Query : pdb_Queries)
    {
      FTSRestResponse resultList;
      FTSRestRequest pdbRequest = new FTSRestRequest();
      pdbRequest.setAllowEmptySeq(false);
      pdbRequest.setResponseSize(500);
      pdbRequest.setFieldToSearchBy("(");
      // pdbRequest.setFieldToSortBy("pdb_id");
      pdbRequest.setWantedFields(
              pdbquery.getDocFieldPrefs().getStructureSummaryFields());
      pdbRequest.setSearchTerm(pdb_Query + ")");

      // handle exceptions like server errors here - means the threedbeacons
      // discovery isn't broken by issues to do with the PDBe SOLR api
      try
      {
        resultList = pdbquery.executePDBFTSRestRequest(pdbRequest);
        if (resultList.getNumberOfItemsFound() == 0)
        {
          Console.info("Unexpectedly returned no results for pdbe query: "
                  + pdb_Query);
        }
        results.add(resultList);
        lastPdbRequest = pdbRequest;
      } catch (Exception ex)
      {
        Console.error("PDBFTSQuery failed", ex);
      }

    }

    return results;
  }

  public FTSRestResponse joinResponses(FTSRestResponse upResponse,
          List<FTSRestResponse> pdbResponses)
  {
    boolean hasPdbResp = lastPdbRequest != null;

    int idx_provider = getLastFTSRequest().getFieldIndex("Provider");
    // join on
    int idx_modelId = getLastFTSRequest().getFieldIndex("Model id");
    int pdbIdx = hasPdbResp ? lastPdbRequest.getFieldIndex("PDB Id") : -1;
    int pdbTitle_idx = hasPdbResp ? lastPdbRequest.getFieldIndex("Title")
            : -1;
    int tdbTitle_idx = getLastFTSRequest().getFieldIndex("Title");

    for (final FTSData row : upResponse.getSearchSummary())
    {
      String id = (String) row.getSummaryData()[idx_modelId];
      String provider = (String) row.getSummaryData()[idx_provider];
      if ("PDBe".equalsIgnoreCase(provider))
      {
        if (!hasPdbResp)
        {
          System.out.println(
                  "Warning: seems like we couldn't get to the PDBe search interface.");
        }
        else
        {
          for (final FTSRestResponse pdbResponse : pdbResponses)
          {
            for (final FTSData pdbrow : pdbResponse.getSearchSummary())
            {
              String pdbid = (String) pdbrow.getSummaryData()[pdbIdx];
              if (id.equalsIgnoreCase(pdbid))
              {
                row.getSummaryData()[tdbTitle_idx] = pdbrow
                        .getSummaryData()[pdbTitle_idx];
              }
            }
          }
        }

      }
      else
      {
        row.getSummaryData()[tdbTitle_idx] = "Model from TDB";
      }
    }
    return upResponse;
  }

  public TDB_FTSData getFTSDataFor(JTable restable, int selectedRow,
          Collection<FTSData> discoveredStructuresSet)
  {
    int idColumnIndex = restable.getColumn("Model id").getModelIndex();

    String modelId = (String) restable.getValueAt(selectedRow,
            idColumnIndex);
    for (FTSData row : discoveredStructuresSet)
    {
      if (row instanceof TDB_FTSData
              && ((TDB_FTSData) row).getModelId().equals(modelId))
      {
        return ((TDB_FTSData) row);
      }
    }
    return null;
  }

}