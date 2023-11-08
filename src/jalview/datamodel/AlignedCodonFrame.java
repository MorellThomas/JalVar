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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import jalview.util.MapList;
import jalview.util.MappingUtils;

/**
 * Stores mapping between the columns of a protein alignment and a DNA alignment
 * and a list of individual codon to amino acid mappings between sequences.
 */
public class AlignedCodonFrame
{

  /*
   * Data bean to hold mappings from one sequence to another
   */
  public class SequenceToSequenceMapping
  {
    private SequenceI fromSeq;

    private Mapping mapping;

    SequenceToSequenceMapping(SequenceI from, Mapping map)
    {
      this.fromSeq = from;
      this.mapping = map;
    }

    /**
     * Readable representation for debugging only, not guaranteed not to change
     */
    @Override
    public String toString()
    {
      return String.format("From %s %s", fromSeq.getName(),
              mapping.toString());
    }

    /**
     * Returns a hashCode derived from the hashcodes of the mappings and fromSeq
     * 
     * @see SequenceToSequenceMapping#hashCode()
     */
    @Override
    public int hashCode()
    {
      return (fromSeq == null ? 0 : fromSeq.hashCode() * 31)
              + mapping.hashCode();
    }

    /**
     * Answers true if the objects hold the same mapping between the same two
     * sequences
     * 
     * @see Mapping#equals
     */
    @Override
    public boolean equals(Object obj)
    {
      if (!(obj instanceof SequenceToSequenceMapping))
      {
        return false;
      }
      SequenceToSequenceMapping that = (SequenceToSequenceMapping) obj;
      if (this.mapping == null)
      {
        return that.mapping == null;
      }
      // TODO: can simplify by asserting fromSeq is a dataset sequence
      return (this.fromSeq == that.fromSeq
              || (this.fromSeq != null && that.fromSeq != null
                      && this.fromSeq.getDatasetSequence() != null
                      && this.fromSeq.getDatasetSequence() == that.fromSeq
                              .getDatasetSequence()))
              && this.mapping.equals(that.mapping);
    }

    public SequenceI getFromSeq()
    {
      return fromSeq;
    }

    public Mapping getMapping()
    {
      return mapping;
    }

    /**
     * Returns true if the mapping covers the full length of the given sequence.
     * This allows us to distinguish the CDS that codes for a protein from
     * another overlapping CDS in the parent dna sequence.
     * 
     * @param seq
     * @return
     */
    public boolean covers(SequenceI seq)
    {
      return covers(seq, false, false);
    }

    /**
     * 
     * @param seq
     * @param localCover
     *          - when true - compare extent of seq's dataset sequence rather
     *          than the local extent
     * @param either
     *          - when true coverage is required for either seq or the mapped
     *          sequence
     * @return true if mapping covers full length of given sequence (or the
     *         other if either==true)
     */
    public boolean covers(SequenceI seq, boolean localCover, boolean either)
    {
      List<int[]> mappedRanges = null, otherRanges = null;
      MapList mapList = mapping.getMap();
      int mstart = seq.getStart(), mend = seq.getEnd(), ostart, oend;
      ;
      if (fromSeq == seq || fromSeq == seq.getDatasetSequence())
      {
        if (localCover && fromSeq != seq)
        {
          mstart = fromSeq.getStart();
          mend = fromSeq.getEnd();
        }
        mappedRanges = mapList.getFromRanges();
        otherRanges = mapList.getToRanges();
        ostart = mapping.to.getStart();
        oend = mapping.to.getEnd();
      }
      else if (mapping.to == seq || mapping.to == seq.getDatasetSequence())
      {
        if (localCover && mapping.to != seq)
        {
          mstart = mapping.to.getStart();
          mend = mapping.to.getEnd();
        }
        mappedRanges = mapList.getToRanges();
        otherRanges = mapList.getFromRanges();
        ostart = fromSeq.getStart();
        oend = fromSeq.getEnd();
      }
      else
      {
        return false;
      }

      /*
       * check that each mapped range lies within the sequence range
       * (necessary for circular CDS - example EMBL:J03321:AAA91567)
       * and mapped length covers (at least) sequence length
       */
      int length = countRange(mappedRanges, mstart, mend);

      if (length != -1)
      {
        // add 3 to mapped length to allow for a mapped stop codon
        if (length + 3 >= (mend - mstart + 1))
        {
          return true;
        }
      }
      if (either)
      {
        // also check coverage of the other range
        length = countRange(otherRanges, ostart, oend);
        if (length != -1)
        {
          if (length + 1 >= (oend - ostart + 1))
          {
            return true;
          }
        }
      }
      return false;
    }

    private int countRange(List<int[]> mappedRanges, int mstart, int mend)
    {
      int length = 0;
      for (int[] range : mappedRanges)
      {
        int from = Math.min(range[0], range[1]);
        int to = Math.max(range[0], range[1]);
        if (from < mstart || to > mend)
        {
          return -1;
        }
        length += (to - from + 1);
      }
      return length;
    }

    /**
     * Adds any regions mapped to or from position {@code pos} in sequence
     * {@code seq} to the given search results Note: recommend first using the
     * .covers(,true,true) to ensure mapping covers both sequences
     * 
     * @param seq
     * @param pos
     * @param sr
     */
    public void markMappedRegion(SequenceI seq, int pos, SearchResultsI sr)
    {
      int[] codon = null;
      SequenceI mappedSeq = null;
      SequenceI ds = seq.getDatasetSequence();
      if (ds == null)
      {
        ds = seq;
      }

      if (this.fromSeq == seq || this.fromSeq == ds)
      {
        codon = this.mapping.map.locateInTo(pos, pos);
        mappedSeq = this.mapping.to;
      }
      else if (this.mapping.to == seq || this.mapping.to == ds)
      {
        codon = this.mapping.map.locateInFrom(pos, pos);
        mappedSeq = this.fromSeq;
      }

      if (codon != null)
      {
        for (int i = 0; i < codon.length; i += 2)
        {
          sr.addResult(mappedSeq, codon[i], codon[i + 1]);
        }
      }
    }
  }

  private List<SequenceToSequenceMapping> mappings;

  /**
   * Constructor
   */
  public AlignedCodonFrame()
  {
    mappings = new ArrayList<>();
  }

  /**
   * Adds a mapping between the dataset sequences for the associated dna and
   * protein sequence objects
   * 
   * @param dnaseq
   * @param aaseq
   * @param map
   */
  public void addMap(SequenceI dnaseq, SequenceI aaseq, MapList map)
  {
    addMap(dnaseq, aaseq, map, null);
  }

  /**
   * Adds a mapping between the dataset sequences for the associated dna and
   * protein sequence objects
   * 
   * @param dnaseq
   * @param aaseq
   * @param map
   * @param mapFromId
   */
  public void addMap(SequenceI dnaseq, SequenceI aaseq, MapList map,
          String mapFromId)
  {
    // JBPNote DEBUG! THIS !
    // dnaseq.transferAnnotation(aaseq, mp);
    // aaseq.transferAnnotation(dnaseq, new Mapping(map.getInverse()));

    SequenceI fromSeq = (dnaseq.getDatasetSequence() == null) ? dnaseq
            : dnaseq.getDatasetSequence();
    SequenceI toSeq = (aaseq.getDatasetSequence() == null) ? aaseq
            : aaseq.getDatasetSequence();

    /*
     * if we already hold a mapping between these sequences, just add to it 
     * note that 'adding' a duplicate map does nothing; this protects against
     * creating duplicate mappings in AlignedCodonFrame
     */
    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.fromSeq == fromSeq && ssm.mapping.to == toSeq)
      {
        ssm.mapping.map.addMapList(map);
        return;
      }
    }

    /*
     * otherwise, add a new sequence mapping
     */
    Mapping mp = new Mapping(toSeq, map);
    mp.setMappedFromId(mapFromId);
    mappings.add(new SequenceToSequenceMapping(fromSeq, mp));
  }

  public SequenceI[] getdnaSeqs()
  {
    // TODO return a list instead?
    // return dnaSeqs;
    List<SequenceI> seqs = new ArrayList<>();
    for (SequenceToSequenceMapping ssm : mappings)
    {
      seqs.add(ssm.fromSeq);
    }
    return seqs.toArray(new SequenceI[seqs.size()]);
  }

  public SequenceI[] getAaSeqs()
  {
    // TODO not used - remove?
    List<SequenceI> seqs = new ArrayList<>();
    for (SequenceToSequenceMapping ssm : mappings)
    {
      seqs.add(ssm.mapping.to);
    }
    return seqs.toArray(new SequenceI[seqs.size()]);
  }

  public MapList[] getdnaToProt()
  {
    List<MapList> maps = new ArrayList<>();
    for (SequenceToSequenceMapping ssm : mappings)
    {
      maps.add(ssm.mapping.map);
    }
    return maps.toArray(new MapList[maps.size()]);
  }

  public Mapping[] getProtMappings()
  {
    List<Mapping> maps = new ArrayList<>();
    for (SequenceToSequenceMapping ssm : mappings)
    {
      maps.add(ssm.mapping);
    }
    return maps.toArray(new Mapping[maps.size()]);
  }

  /**
   * Returns the first mapping found which is to or from the given sequence, or
   * null if none is found
   * 
   * @param seq
   * @return
   */
  public Mapping getMappingForSequence(SequenceI seq)
  {
    SequenceI seqDs = seq.getDatasetSequence();
    seqDs = seqDs != null ? seqDs : seq;

    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.fromSeq == seqDs || ssm.mapping.to == seqDs)
      {
        return ssm.mapping;
      }
    }
    return null;
  }

  /**
   * Return the corresponding aligned or dataset aa sequence for given dna
   * sequence, null if not found.
   * 
   * @param sequenceRef
   * @return
   */
  public SequenceI getAaForDnaSeq(SequenceI dnaSeqRef)
  {
    SequenceI dnads = dnaSeqRef.getDatasetSequence();
    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.fromSeq == dnaSeqRef || ssm.fromSeq == dnads)
      {
        return ssm.mapping.to;
      }
    }
    return null;
  }

  /**
   * Return the corresponding aligned or dataset dna sequence for given amino
   * acid sequence, or null if not found. returns the sequence from the first
   * mapping found that involves the protein sequence.
   * 
   * @param aaSeqRef
   * @return
   */
  public SequenceI getDnaForAaSeq(SequenceI aaSeqRef)
  {
    SequenceI aads = aaSeqRef.getDatasetSequence();
    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.mapping.to == aaSeqRef || ssm.mapping.to == aads)
      {
        return ssm.fromSeq;
      }
    }
    return null;
  }

  /**
   * test to see if codon frame involves seq in any way
   * 
   * @param seq
   *          a nucleotide or protein sequence
   * @return true if a mapping exists to or from this sequence to any translated
   *         sequence
   */
  public boolean involvesSequence(SequenceI seq)
  {
    return getAaForDnaSeq(seq) != null || getDnaForAaSeq(seq) != null;
  }

  /**
   * Add search results for regions in other sequences that translate or are
   * translated from a particular position in seq (which may be an aligned or
   * dataset sequence)
   * 
   * @param seq
   * @param index
   *          position in seq
   * @param results
   *          where highlighted regions go
   */
  public void markMappedRegion(SequenceI seq, int index,
          SearchResultsI results)
  {
    SequenceI ds = seq.getDatasetSequence();
    if (ds == null)
    {
      ds = seq;
    }
    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.covers(seq, true, true))
      {
        ssm.markMappedRegion(ds, index, results);
      }
    }
  }

  /**
   * Convenience method to return the first aligned sequence in the given
   * alignment whose dataset has a mapping with the given (aligned or dataset)
   * sequence.
   * 
   * @param seq
   * 
   * @param al
   * @return
   */
  public SequenceI findAlignedSequence(SequenceI seq, AlignmentI al)
  {
    /*
     * Search mapped protein ('to') sequences first.
     */
    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.fromSeq == seq || ssm.fromSeq == seq.getDatasetSequence())
      {
        for (SequenceI sourceAligned : al.getSequences())
        {
          if (ssm.mapping.to == sourceAligned.getDatasetSequence()
                  || ssm.mapping.to == sourceAligned)
          {
            return sourceAligned;
          }
        }
      }
    }

    /*
     * Then try mapped dna sequences.
     */
    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.mapping.to == seq
              || ssm.mapping.to == seq.getDatasetSequence())
      {
        for (SequenceI sourceAligned : al.getSequences())
        {
          if (ssm.fromSeq == sourceAligned.getDatasetSequence())
          {
            return sourceAligned;
          }
        }
      }
    }

    return null;
  }

  /**
   * Returns the region in the target sequence's dataset that is mapped to the
   * given position (base 1) in the query sequence's dataset. The region is a
   * set of start/end position pairs.
   * 
   * @param target
   * @param query
   * @param queryPos
   * @return
   */
  public int[] getMappedRegion(SequenceI target, SequenceI query,
          int queryPos)
  {
    SequenceI targetDs = target.getDatasetSequence() == null ? target
            : target.getDatasetSequence();
    SequenceI queryDs = query.getDatasetSequence() == null ? query
            : query.getDatasetSequence();
    if (targetDs == null || queryDs == null /*|| dnaToProt == null*/)
    {
      return null;
    }
    for (SequenceToSequenceMapping ssm : mappings)
    {
      /*
       * try mapping from target to query
       */
      if (ssm.fromSeq == targetDs && ssm.mapping.to == queryDs)
      {
        int[] codon = ssm.mapping.map.locateInFrom(queryPos, queryPos);
        if (codon != null)
        {
          return codon;
        }
      }
      /*
       * else try mapping from query to target
       */
      else if (ssm.fromSeq == queryDs && ssm.mapping.to == targetDs)
      {
        int[] codon = ssm.mapping.map.locateInTo(queryPos, queryPos);
        if (codon != null)
        {
          return codon;
        }
      }
    }
    return null;
  }

  /**
   * Returns the mapped DNA codons for the given position in a protein sequence,
   * or null if no mapping is found. Returns a list of (e.g.) ['g', 'c', 't']
   * codons. There may be more than one codon mapped to the protein if (for
   * example), there are mappings to cDNA variants.
   * 
   * @param protein
   *          the peptide dataset sequence
   * @param aaPos
   *          residue position (base 1) in the peptide sequence
   * @return
   */
  public List<char[]> getMappedCodons(SequenceI protein, int aaPos)
  {
    MapList ml = null;
    SequenceI dnaSeq = null;
    List<char[]> result = new ArrayList<>();

    for (SequenceToSequenceMapping ssm : mappings)
    {
      if (ssm.mapping.to == protein
              && ssm.mapping.getMap().getFromRatio() == 3)
      {
        ml = ssm.mapping.map;
        dnaSeq = ssm.fromSeq;

        int[] codonPos = ml.locateInFrom(aaPos, aaPos);
        if (codonPos == null)
        {
          return null;
        }

        /*
         * Read off the mapped nucleotides (converting to position base 0)
         */
        codonPos = MappingUtils.flattenRanges(codonPos);
        int start = dnaSeq.getStart();
        char c1 = dnaSeq.getCharAt(codonPos[0] - start);
        char c2 = dnaSeq.getCharAt(codonPos[1] - start);
        char c3 = dnaSeq.getCharAt(codonPos[2] - start);
        result.add(new char[] { c1, c2, c3 });
      }
    }
    return result.isEmpty() ? null : result;
  }

  /**
   * Returns any mappings found which are from the given sequence, and to
   * distinct sequences.
   * 
   * @param seq
   * @return
   */
  public List<Mapping> getMappingsFromSequence(SequenceI seq)
  {
    List<Mapping> result = new ArrayList<>();
    List<SequenceI> related = new ArrayList<>();
    SequenceI seqDs = seq.getDatasetSequence();
    seqDs = seqDs != null ? seqDs : seq;

    for (SequenceToSequenceMapping ssm : mappings)
    {
      final Mapping mapping = ssm.mapping;
      if (ssm.fromSeq == seqDs)
      {
        if (!related.contains(mapping.to))
        {
          result.add(mapping);
          related.add(mapping.to);
        }
      }
    }
    return result;
  }

  /**
   * Test whether the given sequence is substitutable for one or more dummy
   * sequences in this mapping
   * 
   * @param map
   * @param seq
   * @return
   */
  public boolean isRealisableWith(SequenceI seq)
  {
    return realiseWith(seq, false) > 0;
  }

  /**
   * Replace any matchable mapped dummy sequences with the given real one.
   * Returns the count of sequence mappings instantiated.
   * 
   * @param seq
   * @return
   */
  public int realiseWith(SequenceI seq)
  {
    return realiseWith(seq, true);
  }

  /**
   * Returns the number of mapped dummy sequences that could be replaced with
   * the given real sequence.
   * 
   * @param seq
   *          a dataset sequence
   * @param doUpdate
   *          if true, performs replacements, else only counts
   * @return
   */
  protected int realiseWith(SequenceI seq, boolean doUpdate)
  {
    SequenceI ds = seq.getDatasetSequence() != null
            ? seq.getDatasetSequence()
            : seq;
    int count = 0;

    /*
     * check for replaceable DNA ('map from') sequences
     */
    for (SequenceToSequenceMapping ssm : mappings)
    {
      SequenceI dna = ssm.fromSeq;
      if (dna instanceof SequenceDummy
              && dna.getName().equals(ds.getName()))
      {
        Mapping mapping = ssm.mapping;
        int mapStart = mapping.getMap().getFromLowest();
        int mapEnd = mapping.getMap().getFromHighest();
        boolean mappable = couldRealiseSequence(dna, ds, mapStart, mapEnd);
        if (mappable)
        {
          count++;
          if (doUpdate)
          {
            // TODO: new method ? ds.realise(dna);
            // might want to copy database refs as well
            ds.setSequenceFeatures(dna.getSequenceFeatures());
            // dnaSeqs[i] = ds;
            ssm.fromSeq = ds;
            System.out.println("Realised mapped sequence " + ds.getName());
          }
        }
      }

      /*
       * check for replaceable protein ('map to') sequences
       */
      Mapping mapping = ssm.mapping;
      SequenceI prot = mapping.getTo();
      int mapStart = mapping.getMap().getToLowest();
      int mapEnd = mapping.getMap().getToHighest();
      boolean mappable = couldRealiseSequence(prot, ds, mapStart, mapEnd);
      if (mappable)
      {
        count++;
        if (doUpdate)
        {
          // TODO: new method ? ds.realise(dna);
          // might want to copy database refs as well
          ds.setSequenceFeatures(dna.getSequenceFeatures());
          ssm.mapping.setTo(ds);
        }
      }
    }
    return count;
  }

  /**
   * Helper method to test whether a 'real' sequence could replace a 'dummy'
   * sequence in the map. The criteria are that they have the same name, and
   * that the mapped region overlaps the candidate sequence.
   * 
   * @param existing
   * @param replacement
   * @param mapStart
   * @param mapEnd
   * @return
   */
  protected static boolean couldRealiseSequence(SequenceI existing,
          SequenceI replacement, int mapStart, int mapEnd)
  {
    if (existing instanceof SequenceDummy
            && !(replacement instanceof SequenceDummy)
            && existing.getName().equals(replacement.getName()))
    {
      int start = replacement.getStart();
      int end = replacement.getEnd();
      boolean mappingOverlapsSequence = (mapStart >= start
              && mapStart <= end) || (mapEnd >= start && mapEnd <= end);
      if (mappingOverlapsSequence)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Change any mapping to the given sequence to be to its dataset sequence
   * instead. For use when mappings are created before their referenced
   * sequences are instantiated, for example when parsing GFF data.
   * 
   * @param seq
   */
  public void updateToDataset(SequenceI seq)
  {
    if (seq == null || seq.getDatasetSequence() == null)
    {
      return;
    }
    SequenceI ds = seq.getDatasetSequence();

    for (SequenceToSequenceMapping ssm : mappings)
    /*
     * 'from' sequences
     */
    {
      if (ssm.fromSeq == seq)
      {
        ssm.fromSeq = ds;
      }

      /*
       * 'to' sequences
       */
      if (ssm.mapping.to == seq)
      {
        ssm.mapping.to = ds;
      }
    }
  }

  /**
   * Answers true if this object contains no mappings
   * 
   * @return
   */
  public boolean isEmpty()
  {
    return mappings.isEmpty();
  }

  /**
   * Method for debug / inspection purposes only, may change in future
   */
  @Override
  public String toString()
  {
    return mappings == null ? "null" : mappings.toString();
  }

  /**
   * Returns the first mapping found that is between 'fromSeq' and 'toSeq', or
   * null if none found
   * 
   * @param fromSeq
   *          aligned or dataset sequence
   * @param toSeq
   *          aligned or dataset sequence
   * @return
   */
  public Mapping getMappingBetween(SequenceI fromSeq, SequenceI toSeq)
  {
    SequenceI dssFrom = fromSeq.getDatasetSequence() == null ? fromSeq
            : fromSeq.getDatasetSequence();
    SequenceI dssTo = toSeq.getDatasetSequence() == null ? toSeq
            : toSeq.getDatasetSequence();

    for (SequenceToSequenceMapping mapping : mappings)
    {
      SequenceI from = mapping.fromSeq;
      SequenceI to = mapping.mapping.to;
      if ((from == dssFrom && to == dssTo)
              || (from == dssTo && to == dssFrom))
      {
        return mapping.mapping;
      }
    }
    return null;
  }

  /**
   * Returns a hashcode derived from the list of sequence mappings
   * 
   * @see SequenceToSequenceMapping#hashCode()
   * @see AbstractList#hashCode()
   */
  @Override
  public int hashCode()
  {
    return this.mappings.hashCode();
  }

  /**
   * Two AlignedCodonFrame objects are equal if they hold the same ordered list
   * of mappings
   * 
   * @see SequenceToSequenceMapping#equals
   */
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof AlignedCodonFrame))
    {
      return false;
    }
    return this.mappings.equals(((AlignedCodonFrame) obj).mappings);
  }

  public List<SequenceToSequenceMapping> getMappings()
  {
    return mappings;
  }

  /**
   * Returns the first mapping found which is between the two given sequences,
   * and covers the full extent of both.
   * 
   * @param seq1
   * @param seq2
   * @return
   */
  public SequenceToSequenceMapping getCoveringMapping(SequenceI seq1,
          SequenceI seq2)
  {
    for (SequenceToSequenceMapping mapping : mappings)
    {
      if (mapping.covers(seq2) && mapping.covers(seq1))
      {
        return mapping;
      }
    }
    return null;
  }

  /**
   * Returns the first mapping found which is between the given dataset sequence
   * and another, is a triplet mapping (3:1 or 1:3), and covers the full extent
   * of both sequences involved
   * 
   * @param seq
   * @return
   */
  public SequenceToSequenceMapping getCoveringCodonMapping(SequenceI seq)
  {
    for (SequenceToSequenceMapping mapping : mappings)
    {
      if (mapping.getMapping().getMap().isTripletMap()
              && mapping.covers(seq))
      {
        if (mapping.fromSeq == seq
                && mapping.covers(mapping.getMapping().getTo()))
        {
          return mapping;
        }
        else if (mapping.getMapping().getTo() == seq
                && mapping.covers(mapping.fromSeq))
        {
          return mapping;
        }
      }
    }
    return null;
  }
}
