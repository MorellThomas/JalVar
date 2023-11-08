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
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.core.FTSRestRequest;

public class TDBResultAnalyser
{

  /**
   * model categories - update as needed. warnings output if unknown types
   * encountered.
   * 
   * Order denotes 'trust'
   */
  private static List<String> EXP_CATEGORIES = Arrays
          .asList(new String[]
          { "EXPERIMENTALLY DETERMINED", "DEEP-LEARNING", "AB-INITIO",
              "TEMPLATE-BASED", "CONFORMATIONAL ENSEMBLE" });

  private SequenceI seq;

  private Collection<FTSData> collectedResults;

  private FTSRestRequest lastTdbRequest;

  private int idx_ups;

  private int idx_upe;

  private int idx_mcat;

  private int idx_mqual;

  private int idx_mqualtype;

  private int idx_resol;

  /**
   * selection model
   */
  private String filter = null;

  /**
   * limit to particular source
   */
  private String sourceFilter = null;

  private int idx_mprov;

  public TDBResultAnalyser(SequenceI seq,
          Collection<FTSData> collectedResults,
          FTSRestRequest lastTdbRequest, String fieldToFilterBy,
          String string)
  {
    this.seq = seq;
    this.collectedResults = collectedResults;
    this.lastTdbRequest = lastTdbRequest;
    this.filter = fieldToFilterBy;
    this.sourceFilter = string;
    idx_ups = lastTdbRequest.getFieldIndex("Uniprot Start");
    idx_upe = lastTdbRequest.getFieldIndex("Uniprot End");
    idx_mcat = lastTdbRequest.getFieldIndex("Model Category");
    idx_mprov = lastTdbRequest.getFieldIndex("Provider");
    idx_mqual = lastTdbRequest.getFieldIndex("Confidence");
    idx_resol = lastTdbRequest.getFieldIndex("Resolution");
    idx_mqualtype = lastTdbRequest.getFieldIndex("Confidence Score Type");
  }

  /**
   * maintain and resolve categories to 'trust order' TODO: change the trust
   * scheme to something comprehensible.
   * 
   * @param cat
   * @return 0 for null cat, less than zero for others
   */
  public final int scoreCategory(String cat)
  {
    if (cat == null)
    {
      return 0;
    }
    String upper_cat = cat.toUpperCase(Locale.ROOT);
    int idx = EXP_CATEGORIES.indexOf(upper_cat);
    if (idx == -1)
    {
      System.out.println("Unknown category: '" + cat + "'");
      EXP_CATEGORIES.add(upper_cat);
      idx = EXP_CATEGORIES.size() - 1;
    }
    return -EXP_CATEGORIES.size() - idx;
  }

  /**
   * sorts records discovered by 3D beacons and excludes any that don't
   * intersect with the sequence's start/end rage
   * 
   * @return
   */
  public List<FTSData> getFilteredResponse()
  {
    List<FTSData> filteredResponse = new ArrayList<FTSData>();

    // ignore anything outside the sequence region
    for (FTSData row : collectedResults)
    {
      if (row.getSummaryData() != null
              && row.getSummaryData()[idx_ups] != null)
      {
        int up_s = (Integer) row.getSummaryData()[idx_ups];
        int up_e = (Integer) row.getSummaryData()[idx_upe];
        String provider = (String) row.getSummaryData()[idx_mprov];
        String mcat = (String) row.getSummaryData()[idx_mcat];
        // this makes sure all new categories are in the score array.
        int scorecat = scoreCategory(mcat);
        if (sourceFilter == null || sourceFilter.equals(provider))
        {
          if (seq == row.getSummaryData()[0] && up_e > seq.getStart()
                  && up_s < seq.getEnd())
          {
            filteredResponse.add(row);
          }
        }
      }
    }
    // sort according to decreasing length,
    // increasing start
    Collections.sort(filteredResponse, new Comparator<FTSData>()
    {
      @Override
      public int compare(FTSData o1, FTSData o2)
      {
        Object[] o1data = o1.getSummaryData();
        Object[] o2data = o2.getSummaryData();
        int o1_s = (Integer) o1data[idx_ups];
        int o1_e = (Integer) o1data[idx_upe];
        int o1_cat = scoreCategory((String) o1data[idx_mcat]);
        String o1_prov = ((String) o1data[idx_mprov])
                .toUpperCase(Locale.ROOT);
        int o2_s = (Integer) o2data[idx_ups];
        int o2_e = (Integer) o2data[idx_upe];
        int o2_cat = scoreCategory((String) o2data[idx_mcat]);
        String o2_prov = ((String) o2data[idx_mprov])
                .toUpperCase(Locale.ROOT);
        String o1_qualtype = (String) o1data[idx_mqualtype],
                o2_qualtype = (String) o2data[idx_mqualtype];

        if (o1_cat == o2_cat)
        {
          if (o1_s == o2_s)
          {
            int o1_xtent = o1_e - o1_s;
            int o2_xtent = o2_e - o2_s;
            if (o1_xtent == o2_xtent)
            {
              // EXPERIMENTAL DATA ALWAYS TRUMPS MODELS
              if (o1_cat == scoreCategory(EXP_CATEGORIES.get(0)))
              {
                if (o1_prov.equals(o2_prov))
                {
                  if ("PDBE".equals(o1_prov))
                  {
                    if (eitherNull(idx_resol, o1data, o2data))
                    {
                      return nonNullFirst(idx_resol, o1data, o2data);
                    }
                    // experimental structures, so rank on quality
                    double o1_res = (Double) o1data[idx_resol];
                    double o2_res = (Double) o2data[idx_resol];
                    return (o2_res < o1_res) ? 1
                            : (o2_res == o1_res) ? 0 : -1;
                  }
                  else
                  {
                    return 0; // no change in order
                  }
                }
                else
                {
                  // PDBe always ranked above all other experimentally
                  // determined categories
                  return "PDBE".equals(o1_prov) ? -1
                          : "PDBE".equals(o2_prov) ? 1 : 0;
                }
              }
              else
              {
                // RANK ON QUALITY - DOWNRANK THOSE WITH NO QUALITY MEASURE
                if (eitherNull(idx_mqualtype, o1data, o2data))
                {
                  return nonNullFirst(idx_mqualtype, o1data, o2data);
                }
                // ONLY COMPARE LIKE QUALITY SCORES
                if (!o1_qualtype.equals(o2_qualtype))
                {
                  // prefer LDDT measure over others
                  return "pLDDT".equals(o1_prov) ? -1
                          : "pLDDT".equals(o2_prov) ? 1 : 0;
                }
                // OR NO VALUE FOR THE QUALITY
                if (eitherNull(idx_mqual, o1data, o2data))
                {
                  return nonNullFirst(idx_mqual, o1data, o2data);
                }
                // models, so rank on qmean - b
                double o1_mq = (Double) o1data[idx_mqual];
                double o2_mq = (Double) o2data[idx_mqual];
                return (o2_mq < o1_mq) ? 1 : (o2_mq == o1_mq) ? 0 : -1;
              }
            }
            else
            {
              return o1_xtent - o2_xtent;
            }
          }
          else
          {
            return o1_s - o2_s;
          }
        }
        else
        {
          // if both are not experimental, then favour alphafold
          if (o2_cat > 0 && o1_cat > 0)
          {
            return "ALPHAFOLD DB".equals(o1_prov) ? -1
                    : "ALPHAFOLD DB".equals(o2_prov) ? 1 : 0;
          }
          return o2_cat - o1_cat;
        }
      }

      private int nonNullFirst(int idx_resol, Object[] o1data,
              Object[] o2data)
      {
        return o1data[idx_resol] == o2data[idx_resol] ? 0
                : o1data[idx_resol] != null ? -1 : 1;
      }

      private boolean eitherNull(int idx_resol, Object[] o1data,
              Object[] o2data)
      {
        return (o1data[idx_resol] == null || o2data[idx_resol] == null);
      }

      @Override
      public boolean equals(Object obj)
      {
        return super.equals(obj);
      }
    });
    return filteredResponse;
  }

  /**
   * return list of structures to be marked as selected for this sequence
   * according to given criteria
   * 
   * @param filteredStructures
   *          - sorted, filtered structures from getFilteredResponse
   * 
   */
  public List<FTSData> selectStructures(List<FTSData> filteredStructures)
  {
    List<FTSData> selected = new ArrayList<FTSData>();
    BitSet cover = new BitSet();
    cover.set(seq.getStart(), seq.getEnd());
    // walk down the list of structures, selecting some to add to selected
    // TODO: could do simple DP - double loop to select largest number of
    // structures covering largest number of sites
    for (FTSData structure : filteredStructures)
    {
      Object[] odata = structure.getSummaryData();
      int o1_s = (Integer) odata[idx_ups];
      int o1_e = (Integer) odata[idx_upe];
      int o1_cat = scoreCategory((String) odata[idx_mcat]);
      BitSet scover = new BitSet();
      // measure intersection
      scover.set(o1_s, o1_e);
      scover.and(cover);
      if (scover.cardinality() > 4)
      {
        selected.add(structure);
        // clear the range covered by this structure
        cover.andNot(scover);
      }
    }
    if (selected.size() == 0)
    {
      return selected;
    }
    // final step is to sort on length - this might help the superposition
    // process
    Collections.sort(selected, new Comparator<FTSData>()
    {
      @Override
      public int compare(FTSData o1, FTSData o2)
      {
        Object[] o1data = o1.getSummaryData();
        Object[] o2data = o2.getSummaryData();
        int o1_xt = ((Integer) o1data[idx_upe])
                - ((Integer) o1data[idx_ups]);
        int o1_cat = scoreCategory((String) o1data[idx_mcat]);
        int o2_xt = ((Integer) o2data[idx_upe] - (Integer) o2data[idx_ups]);
        int o2_cat = scoreCategory((String) o2data[idx_mcat]);
        return o2_xt - o1_xt;
      }
    });
    if (filter.equals(
            ThreeDBStructureChooserQuerySource.FILTER_FIRST_BEST_COVERAGE))
    {
      return selected.subList(0, 1);
    }
    return selected;
  }

}
