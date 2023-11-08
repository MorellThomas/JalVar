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

import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.gff.SequenceOntologyI;

import java.util.ArrayList;
import java.util.List;

import com.stevesoft.pat.Regex;

/**
 * A client to fetch CDNA sequence from Ensembl (i.e. that part of the genomic
 * sequence that is transcribed to RNA, but not necessarily translated to
 * protein)
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblCdna extends EnsemblSeqProxy
{
  /*
   * accepts ENST or ENSTG with 11 digits
   * or ENSMUST or similar for other species
   * or CCDSnnnnn.nn with at least 3 digits
   */
  private static final Regex ACCESSION_REGEX = new Regex(
          "(ENS([A-Z]{3}|)[TG][0-9]{11}$)" + "|" + "(CCDS[0-9.]{3,}$)");

  /*
   * fetch exon features on genomic sequence (to identify the cdna regions)
   * and cds and variation features (to retain)
   */
  private static final EnsemblFeatureType[] FEATURES_TO_FETCH = {
      EnsemblFeatureType.exon, EnsemblFeatureType.cds,
      EnsemblFeatureType.variation };

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblCdna()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblCdna(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL (CDNA)";
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return EnsemblSeqType.CDNA;
  }

  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    return FEATURES_TO_FETCH;
  }

  /**
   * Answers true unless the feature type is 'transcript' (or a sub-type in the
   * Sequence Ontology).
   */
  @Override
  protected boolean retainFeature(SequenceFeature sf, String accessionId)
  {
    if (isTranscript(sf.getType()))
    {
      return false;
    }
    return featureMayBelong(sf, accessionId);
  }

  /**
   * Answers a list of sequence features (if any) whose type is 'exon' (or a
   * subtype of exon in the Sequence Ontology), and whose Parent is the
   * transcript we are retrieving
   */
  @Override
  protected List<SequenceFeature> getIdentifyingFeatures(SequenceI seq,
          String accId)
  {
    List<SequenceFeature> result = new ArrayList<>();
    List<SequenceFeature> sfs = seq.getFeatures()
            .getFeaturesByOntology(SequenceOntologyI.EXON);
    for (SequenceFeature sf : sfs)
    {
      String parentFeature = (String) sf.getValue(PARENT);
      if (accId.equals(parentFeature))
      {
        result.add(sf);
      }
    }

    return result;
  }

  /**
   * Parameter object_type=Transcaript added to ensure cdna and not peptide is
   * returned (JAL-2529)
   */
  @Override
  protected String getObjectType()
  {
    return OBJECT_TYPE_TRANSCRIPT;
  }

}
