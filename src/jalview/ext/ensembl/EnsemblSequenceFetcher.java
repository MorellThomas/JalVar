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

import jalview.analysis.AlignmentUtils;
import jalview.bin.Cache;
import jalview.datamodel.DBRefSource;
import jalview.ws.seqfetcher.DbSourceProxyImpl;

import com.stevesoft.pat.Regex;

/**
 * A base class for Ensembl sequence fetchers
 * 
 * @author gmcarstairs
 */
abstract class EnsemblSequenceFetcher extends DbSourceProxyImpl
{
  // domain properties lookup keys:
  protected static final String ENSEMBL_BASEURL = "ENSEMBL_BASEURL";

  protected static final String ENSEMBL_GENOMES_BASEURL = "ENSEMBL_GENOMES_BASEURL";

  // domain properties default values:
  protected static final String DEFAULT_ENSEMBL_BASEURL = "https://rest.ensembl.org";

  // ensemblgenomes REST service merged to ensembl 9th April 2019
  protected static final String DEFAULT_ENSEMBL_GENOMES_BASEURL = DEFAULT_ENSEMBL_BASEURL;

  /*
   * accepts ENSG/T/E/P with 11 digits
   * or ENSMUSP or similar for other species
   * or CCDSnnnnn.nn with at least 3 digits
   */
  private static final Regex ACCESSION_REGEX = new Regex(
          "(ENS([A-Z]{3}|)[GTEP]{1}[0-9]{11}$)" + "|"
                  + "(CCDS[0-9.]{3,}$)");

  protected final String ensemblGenomesDomain;

  protected final String ensemblDomain;

  protected static final String OBJECT_TYPE_TRANSLATION = "Translation";

  protected static final String OBJECT_TYPE_TRANSCRIPT = "Transcript";

  protected static final String OBJECT_TYPE_GENE = "Gene";

  protected static final String PARENT = "Parent";

  protected static final String JSON_ID = AlignmentUtils.VARIANT_ID; // "id";

  protected static final String OBJECT_TYPE = "object_type";

  /*
   * possible values for the 'feature' parameter of the /overlap REST service
   * @see http://rest.ensembl.org/documentation/info/overlap_id
   */
  protected enum EnsemblFeatureType
  {
    gene, transcript, cds, exon, repeat, simple, misc, variation,
    somatic_variation, structural_variation, somatic_structural_variation,
    constrained, regulatory
  }

  private String domain;

  /**
   * Constructor
   */
  public EnsemblSequenceFetcher()
  {
    /*
     * the default domain names may be overridden in .jalview_properties;
     * this allows an easy change from http to https in future if needed
     */
    ensemblDomain = Cache
            .getDefault(ENSEMBL_BASEURL, DEFAULT_ENSEMBL_BASEURL).trim();
    ensemblGenomesDomain = Cache.getDefault(ENSEMBL_GENOMES_BASEURL,
            DEFAULT_ENSEMBL_GENOMES_BASEURL).trim();
    domain = ensemblDomain;
  }

  @Override
  public String getDbSource()
  {
    // NB ensure Uniprot xrefs are canonicalised from "Ensembl" to "ENSEMBL"
    return DBRefSource.ENSEMBL;
  }

  @Override
  public String getAccessionSeparator()
  {
    return " ";
  }

  /**
   * Ensembl accession are ENST + 11 digits for human transcript, ENSG for human
   * gene. Other species insert 3 letters e.g. ENSMUST..., ENSMUSG...
   * 
   * @see http://www.ensembl.org/Help/View?id=151
   */
  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  @Override
  public boolean isValidReference(String accession)
  {
    return getAccessionValidator().search(accession);
  }

  @Override
  public int getTier()
  {
    return 0;
  }

  /**
   * Default test query is a transcript
   */
  @Override
  public String getTestQuery()
  {
    // has CDS on reverse strand:
    return "ENST00000288602";
    // ENST00000461457 // forward strand
  }

  @Override
  public boolean isDnaCoding()
  {
    return true;
  }

  /**
   * Returns the domain name to query e.g. http://rest.ensembl.org or
   * http://rest.ensemblgenomes.org
   * 
   * @return
   */
  protected String getDomain()
  {
    return domain;
  }

  protected void setDomain(String d)
  {
    domain = d == null ? null : d.trim();
  }
}
