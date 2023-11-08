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
package jalview.ext.ensembl;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.List;

import com.stevesoft.pat.Regex;

/**
 * A client to fetch protein translated sequence for an Ensembl identifier
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblProtein extends EnsemblSeqProxy
{
  /*
   * accepts ENSP with 11 digits
   * or ENSMUSP or similar for other species
   * or CCDSnnnnn.nn with at least 3 digits
   */
  private static final Regex ACCESSION_REGEX = new Regex(
          "(ENS([A-Z]{3}|)P[0-9]{11}$)" + "|" + "(CCDS[0-9.]{3,}$)");

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblProtein()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblProtein(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL (Protein)";
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return EnsemblSeqType.PROTEIN;
  }

  /**
   * Returns false, as this fetcher does not retrieve DNA sequences.
   */
  @Override
  public boolean isDnaCoding()
  {
    return false;
  }

  /**
   * Test query is to the protein translation of transcript ENST00000288602
   */
  @Override
  public String getTestQuery()
  {
    return "ENSP00000288602";
  }

  /**
   * Overrides base class method to do nothing - genomic features are not
   * applicable to the protein product sequence
   */
  @Override
  protected void addFeaturesAndProduct(String accId, AlignmentI alignment)
  {
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    // not applicable - can't fetch genomic features for a protein sequence
    return null;
  }

  @Override
  protected List<SequenceFeature> getIdentifyingFeatures(SequenceI seq,
          String accId)
  {
    return new ArrayList<>();
  }

  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  /**
   * Returns an accession id for a query, including conversion of ENST* to
   * ENSP*. This supports querying for the protein sequence for a transcript
   * (ENST identifier) and returning the ENSP identifier.
   */
  @Override
  public String getAccessionIdFromQuery(String query)
  {
    String accId = super.getAccessionIdFromQuery(query);

    /*
     * ensure last character before (11) digits is P
     * ENST00000288602 -> ENSP00000288602
     * ENSMUST00000288602 -> ENSMUSP00000288602
     */
    if (accId != null && accId.length() >= 12)
    {
      char[] chars = accId.toCharArray();
      chars[chars.length - 12] = 'P';
      accId = new String(chars);
    }
    return accId;
  }

}
