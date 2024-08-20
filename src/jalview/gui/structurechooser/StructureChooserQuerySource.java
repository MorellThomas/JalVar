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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSDataColumnPreferences;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.jbgui.FilterOption;

/**
 * logic for querying sources of structural data for structures of sequences
 * 
 * @author jprocter
 *
 * @param <T>
 */
public abstract class StructureChooserQuerySource
{

  protected FTSDataColumnPreferences docFieldPrefs;

  /**
   * max length of a GET URL (probably :( )
   */
  protected static int MAX_QLENGTH = 7820;

  public StructureChooserQuerySource()
  {
  }

  public static StructureChooserQuerySource getPDBfts()
  {
    return new PDBStructureChooserQuerySource();
  }

  public static StructureChooserQuerySource getTDBfts()
  {
    return new ThreeDBStructureChooserQuerySource();
  }

  public FTSDataColumnPreferences getDocFieldPrefs()
  {
    return docFieldPrefs;
  }

  public void setDocFieldPrefs(FTSDataColumnPreferences docFieldPrefs)
  {
    this.docFieldPrefs = docFieldPrefs;
  }

  public FTSDataColumnPreferences getInitialFieldPreferences()
  {
    return docFieldPrefs;
  }

  /**
   * Builds a query string for a given sequences using its DBRef entries
   * 
   * @param seq
   *          the sequences to build a query for
   * @return the built query string
   */

  public abstract String buildQuery(SequenceI seq);

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

  static PDBEntry getFindEntry(String id, Vector<PDBEntry> pdbEntries)
  {
    Objects.requireNonNull(id);
    Objects.requireNonNull(pdbEntries);
    PDBEntry foundEntry = null;
    for (PDBEntry entry : pdbEntries)
    {
      if (entry.getId().equalsIgnoreCase(id))
      {
        return entry;
      }
    }
    return foundEntry;
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
  public abstract FTSRestResponse fetchStructuresMetaData(SequenceI seq,
          Collection<FTSDataColumnI> wantedFields,
          FilterOption selectedFilterOpt, boolean b) throws Exception;

  /**
   * FTSRestClient specific query builder to pick top ranked entry from a
   * fetchStructuresMetaData query
   * 
   * @param seq
   *          - seq to generate a query for
   * @param discoveredStructuresSet
   *          - existing set of entries - allows client side selection
   * @param wantedFields
   *          - fields to retrieve
   * @param selectedFilterOpt
   *          - criterion for ranking results (e.g. resolution)
   * @param b
   *          - sort ascending or descending
   * @return
   * @throws Exception
   */
  public abstract FTSRestResponse selectFirstRankedQuery(SequenceI seq,
          Collection<FTSData> discoveredStructuresSet,
          Collection<FTSDataColumnI> wantedFields, String fieldToFilterBy,
          boolean b) throws Exception;

  /**
   * 
   * @param discoveredStructuresSet
   * @return the table model for the given result set for this engine
   */
  public TableModel getTableModel(
          Collection<FTSData> discoveredStructuresSet)
  {
    return FTSRestResponse.getTableModel(getLastFTSRequest(),
            discoveredStructuresSet);
  }

  protected abstract FTSRestRequest getLastFTSRequest();

  public abstract PDBEntry[] collectSelectedRows(JTable restable,
          int[] selectedRows, List<SequenceI> selectedSeqsToView);

  /**
   * @param VIEWS_FILTER
   *          - a String key that can be used by the caller to tag the returned
   *          filter options to distinguish them in a collection
   * @return list of FilterOption - convention is that the last one in the list
   *         will be constructed with 'addSeparator==true'
   */
  public abstract List<FilterOption> getAvailableFilterOptions(
          String VIEWS_FILTER);

  /**
   * construct a structure chooser query source for the given set of sequences
   * 
   * @param selectedSeqs
   * @return PDBe or 3DB query source
   */
  public static StructureChooserQuerySource getQuerySourceFor(
          SequenceI[] selectedSeqs)
  {
    ThreeDBStructureChooserQuerySource tdbSource = new ThreeDBStructureChooserQuerySource();
    boolean hasUniprot = false, hasCanonical = false;
    boolean hasNA = false, hasProtein = false;
    int protWithoutUni = 0;
    int protWithoutCanon = 0;
    for (SequenceI seq : selectedSeqs)
    {
      hasNA |= !seq.isProtein();
      hasProtein |= seq.isProtein();
      if (seq.isProtein())
      {
        int refsAvailable = ThreeDBStructureChooserQuerySource
                .checkUniprotRefs(seq.getDBRefs());
        if (refsAvailable > -2)
        {
          if (refsAvailable > -1)
          {
            hasCanonical = true;
          }
          else
          {
            protWithoutCanon++;
          }
          hasUniprot = true;
        }
        else
        {
          protWithoutUni++;

        }
      }
    }
    //
    // logic: all canonicals - no fetchdb
    // some uniprot no canonicals: defer to PDB, user can optionally fetch
    //
    if (hasProtein && hasCanonical && !hasNA && protWithoutCanon == 0
            && protWithoutUni == 0)

    {
      return tdbSource;
    }
    return new PDBStructureChooserQuerySource();
  }

  /**
   * some filter options may mean the original query needs to be executed again.
   * 
   * @param selectedFilterOpt
   * @return true if the fetchStructuresMetadata method needs to be called again
   */
  public abstract boolean needsRefetch(FilterOption selectedFilterOpt);

  public void updateAvailableFilterOptions(String VIEWS_FILTER,
          List<FilterOption> xtantOptions, Collection<FTSData> lastFTSData)
  {
    // TODO Auto-generated method stub

  }
}