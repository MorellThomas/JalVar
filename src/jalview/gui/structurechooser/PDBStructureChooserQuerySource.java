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

import java.util.Locale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableModel;

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
import jalview.fts.service.pdb.PDBFTSRestClient;
import jalview.jbgui.FilterOption;
import jalview.util.MessageManager;

/**
 * logic for querying the PDBe API for structures of sequences
 * 
 * @author jprocter
 */
public class PDBStructureChooserQuerySource
        extends StructureChooserQuerySource
{

  private static int MAX_QLENGTH = 7820;

  protected FTSRestRequest lastPdbRequest;

  protected FTSRestClientI pdbRestClient;

  public PDBStructureChooserQuerySource()
  {
    pdbRestClient = PDBFTSRestClient.getInstance();
    docFieldPrefs = new FTSDataColumnPreferences(
            PreferenceSource.STRUCTURE_CHOOSER,
            PDBFTSRestClient.getInstance());

  }

  /**
   * Builds a query string for a given sequences using its DBRef entries
   * 
   * @param seq
   *          the sequences to build a query for
   * @return the built query string
   */

  public String buildQuery(SequenceI seq)
  {
    boolean isPDBRefsFound = false;
    boolean isUniProtRefsFound = false;
    StringBuilder queryBuilder = new StringBuilder();
    Set<String> seqRefs = new LinkedHashSet<>();

    /*
     * note PDBs as DBRefEntry so they are not duplicated in query
     */
    Set<String> pdbids = new HashSet<>();

    if (seq.getAllPDBEntries() != null
            && queryBuilder.length() < MAX_QLENGTH)
    {
      for (PDBEntry entry : seq.getAllPDBEntries())
      {
        if (isValidSeqName(entry.getId()))
        {
          String id = entry.getId().toLowerCase(Locale.ROOT);
          queryBuilder.append("pdb_id:").append(id).append(" OR ");
          isPDBRefsFound = true;
          pdbids.add(id);
        }
      }
    }

    List<DBRefEntry> refs = seq.getDBRefs();
    if (refs != null && refs.size() != 0)
    {
      for (int ib = 0, nb = refs.size(); ib < nb; ib++)
      {
        DBRefEntry dbRef = refs.get(ib);
        if (isValidSeqName(getDBRefId(dbRef))
                && queryBuilder.length() < MAX_QLENGTH)
        {
          if (dbRef.getSource().equalsIgnoreCase(DBRefSource.UNIPROT))
          {
            queryBuilder.append("uniprot_accession:")
                    .append(getDBRefId(dbRef)).append(" OR ");
            queryBuilder.append("uniprot_id:").append(getDBRefId(dbRef))
                    .append(" OR ");
            isUniProtRefsFound = true;
          }
          else if (dbRef.getSource().equalsIgnoreCase(DBRefSource.PDB))
          {

            String id = getDBRefId(dbRef).toLowerCase(Locale.ROOT);
            if (!pdbids.contains(id))
            {
              queryBuilder.append("pdb_id:").append(id).append(" OR ");
              isPDBRefsFound = true;
              pdbids.add(id);
            }
          }
          else
          {
            seqRefs.add(getDBRefId(dbRef));
          }
        }
      }
    }

    if (!isPDBRefsFound && !isUniProtRefsFound)
    {
      String seqName = seq.getName();
      seqName = sanitizeSeqName(seqName);
      String[] names = seqName.toLowerCase(Locale.ROOT).split("\\|");
      for (String name : names)
      {
        name.trim();
        if (isValidSeqName(name))
        {
          seqRefs.add(name);
        }
      }

      for (String seqRef : seqRefs)
      {
        queryBuilder.append("text:").append(seqRef).append(" OR ");
      }
    }

    int endIndex = queryBuilder.lastIndexOf(" OR ");
    if (queryBuilder.toString().length() < 6)
    {
      return null;
    }
    String query = queryBuilder.toString().substring(0, endIndex);
    return query;
  }

  /**
   * Remove the following special characters from input string +, -, &, !, (, ),
   * {, }, [, ], ^, ", ~, *, ?, :, \
   * 
   * @param seqName
   * @return
   */
  public static String sanitizeSeqName(String seqName)
  {
    Objects.requireNonNull(seqName);
    return seqName.replaceAll("\\[\\d*\\]", "")
            .replaceAll("[^\\dA-Za-z|_]", "").replaceAll("\\s+", "+");
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
  public FTSRestResponse fetchStructuresMetaData(SequenceI seq,
          Collection<FTSDataColumnI> wantedFields,
          FilterOption selectedFilterOpt, boolean b) throws Exception
  {
    FTSRestResponse resultList;
    FTSRestRequest pdbRequest = new FTSRestRequest();
    pdbRequest.setAllowEmptySeq(false);
    pdbRequest.setResponseSize(500);
    pdbRequest.setFieldToSearchBy("(");
    pdbRequest.setFieldToSortBy(selectedFilterOpt.getValue(), b);
    pdbRequest.setWantedFields(wantedFields);
    pdbRequest.setSearchTerm(buildQuery(seq) + ")");
    pdbRequest.setAssociatedSequence(seq);
    resultList = pdbRestClient.executeRequest(pdbRequest);

    lastPdbRequest = pdbRequest;
    return resultList;
  }

  public List<FilterOption> getAvailableFilterOptions(String VIEWS_FILTER)
  {
    List<FilterOption> filters = new ArrayList<FilterOption>();
    filters.add(new FilterOption(
            "PDBe " + MessageManager.getString("label.best_quality"),
            "overall_quality", VIEWS_FILTER, false, this));
    filters.add(new FilterOption(
            "PDBe " + MessageManager.getString("label.best_resolution"),
            "resolution", VIEWS_FILTER, false, this));
    filters.add(new FilterOption(
            "PDBe " + MessageManager.getString("label.most_protein_chain"),
            "number_of_protein_chains", VIEWS_FILTER, false, this));
    filters.add(new FilterOption(
            "PDBe " + MessageManager
                    .getString("label.most_bound_molecules"),
            "number_of_bound_molecules", VIEWS_FILTER, false, this));
    filters.add(new FilterOption(
            "PDBe " + MessageManager
                    .getString("label.most_polymer_residues"),
            "number_of_polymer_residues", VIEWS_FILTER, true, this));

    return filters;
  }

  @Override
  public boolean needsRefetch(FilterOption selectedFilterOpt)
  {
    // PDBe queries never need a refetch first
    return false;
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
  public FTSRestResponse selectFirstRankedQuery(SequenceI seq,
          Collection<FTSData> collectedResults,
          Collection<FTSDataColumnI> wantedFields, String fieldToFilterBy,
          boolean b) throws Exception
  {

    FTSRestResponse resultList;
    FTSRestRequest pdbRequest = new FTSRestRequest();
    if (fieldToFilterBy.equalsIgnoreCase("uniprot_coverage"))
    {
      pdbRequest.setAllowEmptySeq(false);
      pdbRequest.setResponseSize(1);
      pdbRequest.setFieldToSearchBy("(");
      pdbRequest.setSearchTerm(buildQuery(seq) + ")");
      pdbRequest.setWantedFields(wantedFields);
      pdbRequest.setAssociatedSequence(seq);
      pdbRequest.setFacet(true);
      pdbRequest.setFacetPivot(fieldToFilterBy + ",entry_entity");
      pdbRequest.setFacetPivotMinCount(1);
    }
    else
    {
      pdbRequest.setAllowEmptySeq(false);
      pdbRequest.setResponseSize(1);
      pdbRequest.setFieldToSearchBy("(");
      pdbRequest.setFieldToSortBy(fieldToFilterBy, b);
      pdbRequest.setSearchTerm(buildQuery(seq) + ")");
      pdbRequest.setWantedFields(wantedFields);
      pdbRequest.setAssociatedSequence(seq);
    }
    resultList = pdbRestClient.executeRequest(pdbRequest);

    lastPdbRequest = pdbRequest;
    return resultList;
  }

  @Override
  public PDBEntry[] collectSelectedRows(JTable restable, int[] selectedRows,
          List<SequenceI> selectedSeqsToView)
  {
    int refSeqColIndex = restable.getColumn("Ref Sequence").getModelIndex();

    PDBEntry[] pdbEntriesToView = new PDBEntry[selectedRows.length];
    int count = 0;
    int idColumnIndex = -1;
    boolean fromTDB = true;
    idColumnIndex = restable.getColumn("PDB Id").getModelIndex();

    for (int row : selectedRows)
    {

      String pdbIdStr = restable.getValueAt(row, idColumnIndex).toString();
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
        pdbEntry.setType(PDBEntry.Type.MMCIF);
        selectedSeq.getDatasetSequence().addPDBId(pdbEntry);
      }
      pdbEntriesToView[count++] = pdbEntry;
    }
    return pdbEntriesToView;
  }

  @Override
  protected FTSRestRequest getLastFTSRequest()
  {
    return lastPdbRequest;
  }

  public FTSRestResponse executePDBFTSRestRequest(FTSRestRequest pdbRequest)
          throws Exception
  {
    return pdbRestClient.executeRequest(pdbRequest);
  }

}