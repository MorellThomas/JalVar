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
package jalview.datamodel;

/**
 * BH 2018 SwingJS note: If additional final static Strings are added to this
 * file, they should be added to public static final String[] allTypes.
 * 
 * Defines internal constants for unambiguous annotation of DbRefEntry source
 * strings and describing the data retrieved from external database sources (see
 * jalview.ws.DbSourcProxy) <br/>
 * TODO: replace with ontology to allow recognition of particular attributes
 * (e.g. protein coding, alignment (ortholog db, paralog db, domain db),
 * genomic, transcriptomic, 3D structure providing (PDB, MODBASE, etc) ..).
 * 
 * 
 * 
 * @author JimP
 * 
 */
import java.util.Locale;

public class DBRefSource
{

  public static final String UNIPROT = "UNIPROT";

  public static final String UP_NAME = "UNIPROT_NAME"
          .toUpperCase(Locale.ROOT);

  /**
   * Uniprot Knowledgebase/TrEMBL as served from EMBL protein products.
   */
  public static final String UNIPROTKB = "UniProtKB/TrEMBL"
          .toUpperCase(Locale.ROOT);

  public static final String ENSEMBL = "ENSEMBL";

  public static final String ENSEMBLGENOMES = "ENSEMBLGENOMES";

  public static final String EMBL = "EMBL";

  public static final String EMBLCDS = "EMBLCDS";

  public static final String EMBLCDSProduct = "EMBLCDSProtein"
          .toUpperCase(Locale.ROOT);

  public static final String PDB = "PDB";

  public static final String PFAM = "PFAM";

  public static final String RFAM = "RFAM";

  public static final String GENEDB = "GeneDB".toUpperCase(Locale.ROOT);

  public static final String PDB_CANONICAL_NAME = PDB;

  public static final String[] allSources = new String[] { UNIPROT, UP_NAME,
      UNIPROTKB, ENSEMBL, ENSEMBLGENOMES, EMBL, EMBLCDS, EMBLCDSProduct,
      PDB, PFAM, RFAM, GENEDB };

  public static final int UNIPROT_MASK = 1 << 0;

  public static final int UP_NAME_MASK = 1 << 1;

  public static final int UNIPROT_KB_MASK = 1 << 2;

  public static final int ENSEMBL_MASK = 1 << 3;

  public static final int ENSEMBL_GENOMES_MASK = 1 << 4;

  public static final int EMBL_MASK = 1 << 5;

  public static final int EMBL_CDS_MASK = 1 << 6;

  public static final int EMBL_CDS_PRODUCT_MASK = 1 << 7;

  public static final int PDB_MASK = 1 << 8;

  public static final int PFAM_MASK = 1 << 9;

  public static final int RFAM_MASK = 1 << 10;

  public static final int GENE_DB_MASK = 1 << 11;

  public static final int MASK_COUNT = 12;

  public static final int ALL_MASKS = (1 << MASK_COUNT) - 1;

  public static int getSourceKey(String name)
  {
    for (int i = 0; i < MASK_COUNT; i++)
    {
      if (name.equals(allSources[i]))
      {
        return 1 << i;
      }
    }
    return 0;
  }

  public static final int PRIMARY_MASK = UNIPROT_MASK | ENSEMBL_MASK;

  /**
   * List of databases whose sequences might have coding regions annotated
   */
  public static final String[] DNACODINGDBS = { ENSEMBL, ENSEMBLGENOMES,
      EMBL, EMBLCDS, GENEDB };

  public static final int DNA_CODING_MASK = ENSEMBL_MASK
          | ENSEMBL_GENOMES_MASK | EMBL_MASK | EMBL_CDS_MASK | GENE_DB_MASK;

  public static final String[] CODINGDBS = { EMBLCDS, GENEDB, ENSEMBL };

  public static final int CODING_MASK = EMBL_CDS_MASK | GENE_DB_MASK
          | ENSEMBL_MASK;

  public static final String[] PROTEINDBS = { UNIPROT, UNIPROTKB, ENSEMBL,
      EMBLCDSProduct }; // Ensembl ENSP* entries are protein

  public static final int PROTEIN_MASK = UNIPROT_MASK | UNIPROT_KB_MASK
          | ENSEMBL_MASK | EMBL_CDS_PRODUCT_MASK;

  // for SequenceAnnotationReport only

  // public static final String[][] PRIMARY_SOURCES = new String[][] {
  // CODINGDBS, DNACODINGDBS, PROTEINDBS };
  //
  public static final int PRIMARY_SOURCES_MASK = CODING_MASK
          | DNA_CODING_MASK | PROTEIN_MASK;

  public static boolean isPrimarySource(String source)
  {
    return ((PRIMARY_SOURCES_MASK & getSourceKey(source)) != 0);
  }

  public static boolean isPrimaryCandidate(String ucversion)
  {
    if (ucversion==null)
    {
      // Null/empty version is not a real reference ?
      return false;
    }
    // tricky - this test really needs to search the sequence's set of dbrefs to
    // see if there is a primary reference that derived this reference.
    for (int i = allSources.length; --i >= 0;)
    {
      if (ucversion.startsWith(allSources[i])) // BH 2019.01.25
                                               // .toUpperCase(Locale.ROOT)
                                               // unnecessary here for
                                               // allSources
      {
        // by convention, many secondary references inherit the primary
        // reference's
        // source string as a prefix for any version information from the
        // secondary reference.
        return false;
      }
    }
    return true;
  }

}
