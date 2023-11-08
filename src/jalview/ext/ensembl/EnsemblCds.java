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
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyI;

import java.util.ArrayList;
import java.util.List;

/**
 * A client for direct fetching of CDS sequences from Ensembl (i.e. that part of
 * the genomic sequence that is translated to protein)
 * 
 * TODO: not currently used as CDS sequences are computed from CDS features on
 * transcripts - delete this class?
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblCds extends EnsemblSeqProxy
{
  /*
   * fetch cds features on genomic sequence (to identify the CDS regions)
   * and exon and variation features (to retain for display)
   */
  private static final EnsemblFeatureType[] FEATURES_TO_FETCH = {
      EnsemblFeatureType.cds, EnsemblFeatureType.exon,
      EnsemblFeatureType.variation };

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblCds()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblCds(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL (CDS)";
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return EnsemblSeqType.CDS;
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    return FEATURES_TO_FETCH;
  }

  /**
   * Answers true unless the feature type is 'CDS' (or a sub-type of CDS in the
   * Sequence Ontology). CDS features are only retrieved in order to identify
   * the cds sequence range, and are redundant information on the cds sequence
   * itself.
   */
  @Override
  protected boolean retainFeature(SequenceFeature sf, String accessionId)
  {
    if (SequenceOntologyFactory.getInstance().isA(sf.getType(),
            SequenceOntologyI.CDS))
    {
      return false;
    }
    return featureMayBelong(sf, accessionId);
  }

  /**
   * Answers a list of sequence features (if any) whose type is 'CDS' (or a
   * subtype of CDS in the Sequence Ontology), and whose Parent is the
   * transcript we are retrieving
   */
  @Override
  protected List<SequenceFeature> getIdentifyingFeatures(SequenceI seq,
          String accId)
  {
    List<SequenceFeature> result = new ArrayList<>();
    List<SequenceFeature> sfs = seq.getFeatures()
            .getFeaturesByOntology(SequenceOntologyI.CDS);
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
   * Overrides this method to trivially return a range which is the whole of the
   * nucleotide sequence. This is both faster than scanning for CDS features,
   * and also means we don't need to keep CDS features on CDS sequence (where
   * they are redundant information).
   */
  protected List<int[]> getCdsRanges(SequenceI dnaSeq)
  {
    int len = dnaSeq.getLength();
    List<int[]> ranges = new ArrayList<>();
    ranges.add(new int[] { 1, len });
    return ranges;
  }

}
