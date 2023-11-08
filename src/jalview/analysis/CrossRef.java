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
package jalview.analysis;

import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.DBRefUtils;
import jalview.util.MapList;
import jalview.ws.SequenceFetcherFactory;
import jalview.ws.seqfetcher.ASequenceFetcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Functions for cross-referencing sequence databases.
 * 
 * @author JimP
 * 
 */
public class CrossRef
{
  /*
   * the dataset of the alignment for which we are searching for 
   * cross-references; in some cases we may resolve xrefs by 
   * searching in the dataset
   */
  private AlignmentI dataset;

  /*
   * the sequences for which we are seeking cross-references
   */
  private SequenceI[] fromSeqs;

  /**
   * matcher built from dataset
   */
  SequenceIdMatcher matcher;

  /**
   * sequences found by cross-ref searches to fromSeqs
   */
  List<SequenceI> rseqs;

  /**
   * Constructor
   * 
   * @param seqs
   *          the sequences for which we are seeking cross-references
   * @param ds
   *          the containing alignment dataset (may be searched to resolve
   *          cross-references)
   */
  public CrossRef(SequenceI[] seqs, AlignmentI ds)
  {
    fromSeqs = seqs;
    dataset = ds.getDataset() == null ? ds : ds.getDataset();
  }

  /**
   * Returns a list of distinct database sources for which sequences have either
   * <ul>
   * <li>a (dna-to-protein or protein-to-dna) cross-reference</li>
   * <li>an indirect cross-reference - a (dna-to-protein or protein-to-dna)
   * reference from another sequence in the dataset which has a cross-reference
   * to a direct DBRefEntry on the given sequence</li>
   * </ul>
   * 
   * @param dna
   *          - when true, cross-references *from* dna returned. When false,
   *          cross-references *from* protein are returned
   * @return
   */
  public List<String> findXrefSourcesForSequences(boolean dna)
  {
    List<String> sources = new ArrayList<>();
    for (SequenceI seq : fromSeqs)
    {
      if (seq != null)
      {
        findXrefSourcesForSequence(seq, dna, sources);
      }
    }
    sources.remove(DBRefSource.EMBL); // hack to prevent EMBL xrefs resulting in
                                      // redundant datasets
    if (dna)
    {
      sources.remove(DBRefSource.ENSEMBL); // hack to prevent Ensembl and
                                           // EnsemblGenomes xref option shown
                                           // from cdna panel
      sources.remove(DBRefSource.ENSEMBLGENOMES);
    }
    // redundant datasets
    return sources;
  }

  /**
   * Returns a list of distinct database sources for which a sequence has either
   * <ul>
   * <li>a (dna-to-protein or protein-to-dna) cross-reference</li>
   * <li>an indirect cross-reference - a (dna-to-protein or protein-to-dna)
   * reference from another sequence in the dataset which has a cross-reference
   * to a direct DBRefEntry on the given sequence</li>
   * </ul>
   * 
   * @param seq
   *          the sequence whose dbrefs we are searching against
   * @param fromDna
   *          when true, context is DNA - so sources identifying protein
   *          products will be returned.
   * @param sources
   *          a list of sources to add matches to
   */
  void findXrefSourcesForSequence(SequenceI seq, boolean fromDna,
          List<String> sources)
  {
    /*
     * first find seq's xrefs (dna-to-peptide or peptide-to-dna)
     */
    List<DBRefEntry> rfs = DBRefUtils.selectDbRefs(!fromDna,
            seq.getDBRefs());
    addXrefsToSources(rfs, sources);
    if (dataset != null)
    {
      /*
       * find sequence's direct (dna-to-dna, peptide-to-peptide) xrefs
       */
      List<DBRefEntry> lrfs = DBRefUtils.selectDbRefs(fromDna,
              seq.getDBRefs());
      List<SequenceI> foundSeqs = new ArrayList<>();

      /*
       * find sequences in the alignment which xref one of these DBRefs
       * i.e. is xref-ed to a common sequence identifier
       */
      searchDatasetXrefs(fromDna, seq, lrfs, foundSeqs, null);

      /*
       * add those sequences' (dna-to-peptide or peptide-to-dna) dbref sources
       */
      for (SequenceI rs : foundSeqs)
      {
        List<DBRefEntry> xrs = DBRefUtils.selectDbRefs(!fromDna,
                rs.getDBRefs());
        addXrefsToSources(xrs, sources);
      }
    }
  }

  /**
   * Helper method that adds the source identifiers of some cross-references to
   * a (non-redundant) list of database sources
   * 
   * @param xrefs
   * @param sources
   */
  void addXrefsToSources(List<DBRefEntry> xrefs, List<String> sources)
  {
    if (xrefs != null)
    {
      for (DBRefEntry ref : xrefs)
      {
        /*
         * avoid duplication e.g. ENSEMBL and Ensembl
         */
        String source = DBRefUtils.getCanonicalName(ref.getSource());
        if (!sources.contains(source))
        {
          sources.add(source);
        }
      }
    }
  }

  /**
   * Attempts to find cross-references from the sequences provided in the
   * constructor to the given source database. Cross-references may be found
   * <ul>
   * <li>in dbrefs on the sequence which hold a mapping to a sequence
   * <ul>
   * <li>provided with a fetched sequence (e.g. ENA translation), or</li>
   * <li>populated previously after getting cross-references</li>
   * </ul>
   * <li>as other sequences in the alignment which share a dbref identifier with
   * the sequence</li>
   * <li>by fetching from the remote database</li>
   * </ul>
   * The cross-referenced sequences, and mappings to them, are added to the
   * alignment dataset.
   * 
   * @param source
   * @return cross-referenced sequences (as dataset sequences)
   */
  public Alignment findXrefSequences(String source, boolean fromDna)
  {

    rseqs = new ArrayList<>();
    AlignedCodonFrame cf = new AlignedCodonFrame();
    matcher = new SequenceIdMatcher(dataset.getSequences());

    for (SequenceI seq : fromSeqs)
    {
      SequenceI dss = seq;
      while (dss.getDatasetSequence() != null)
      {
        dss = dss.getDatasetSequence();
      }
      boolean found = false;
      List<DBRefEntry> xrfs = DBRefUtils.selectDbRefs(!fromDna,
              dss.getDBRefs());
      // ENST & ENSP comes in to both Protein and nucleotide, so we need to
      // filter them
      // out later.
      if ((xrfs == null || xrfs.size() == 0) && dataset != null)
      {
        /*
         * found no suitable dbrefs on sequence - look for sequences in the
         * alignment which share a dbref with this one
         */
        List<DBRefEntry> lrfs = DBRefUtils.selectDbRefs(fromDna,
                seq.getDBRefs());

        /*
         * find sequences (except this one!), of complementary type,
         *  which have a dbref to an accession id for this sequence,
         *  and add them to the results
         */
        found = searchDatasetXrefs(fromDna, dss, lrfs, rseqs, cf);
      }
      if (xrfs == null && !found)
      {
        /*
         * no dbref to source on this sequence or matched
         * complementary sequence in the dataset 
         */
        continue;
      }
      List<DBRefEntry> sourceRefs = DBRefUtils.searchRefsForSource(xrfs,
              source);
      Iterator<DBRefEntry> refIterator = sourceRefs.iterator();
      // At this point, if we are retrieving Ensembl, we still don't filter out
      // ENST when looking for protein crossrefs.
      while (refIterator.hasNext())
      {
        DBRefEntry xref = refIterator.next();
        found = false;
        // we're only interested in coding cross-references, not
        // locus->transcript
        if (xref.hasMap() && xref.getMap().getMap().isTripletMap())
        {
          SequenceI mappedTo = xref.getMap().getTo();
          if (mappedTo != null)
          {
            /*
             * dbref contains the sequence it maps to; add it to the
             * results unless we have done so already (could happen if 
             * fetching xrefs for sequences which have xrefs in common)
             * for example: UNIPROT {P0CE19, P0CE20} -> EMBL {J03321, X06707}
             */
            found = true;
            /*
             * problem: matcher.findIdMatch() is lenient - returns a sequence
             * with a dbref to the search arg e.g. ENST for ENSP - wrong
             * but findInDataset() matches ENSP when looking for Uniprot...
             */
            SequenceI matchInDataset = findInDataset(xref);
            if (matchInDataset != null && xref.getMap().getTo() != null
                    && matchInDataset != xref.getMap().getTo())
            {
              System.err.println(
                      "Implementation problem (reopen JAL-2154): CrossRef.findInDataset seems to have recovered a different sequence than the one explicitly mapped for xref."
                              + "Found:" + matchInDataset + "\nExpected:"
                              + xref.getMap().getTo() + "\nFor xref:"
                              + xref);
            }
            /*matcher.findIdMatch(mappedTo);*/
            if (matchInDataset != null)
            {
              if (!rseqs.contains(matchInDataset))
              {
                rseqs.add(matchInDataset);
              }
              // even if rseqs contained matchInDataset - check mappings between
              // these seqs are added
              // need to try harder to only add unique mappings
              if (xref.getMap().getMap().isTripletMap()
                      && dataset.getMapping(seq, matchInDataset) == null
                      && cf.getMappingBetween(seq, matchInDataset) == null)
              {
                // materialise a mapping for highlighting between these
                // sequences
                if (fromDna)
                {
                  cf.addMap(dss, matchInDataset, xref.getMap().getMap(),
                          xref.getMap().getMappedFromId());
                }
                else
                {
                  cf.addMap(matchInDataset, dss,
                          xref.getMap().getMap().getInverse(),
                          xref.getMap().getMappedFromId());
                }
              }

              refIterator.remove();
              continue;
            }
            // TODO: need to determine if this should be a deriveSequence
            SequenceI rsq = new Sequence(mappedTo);
            rseqs.add(rsq);
            if (xref.getMap().getMap().isTripletMap())
            {
              // get sense of map correct for adding to product alignment.
              if (fromDna)
              {
                // map is from dna seq to a protein product
                cf.addMap(dss, rsq, xref.getMap().getMap(),
                        xref.getMap().getMappedFromId());
              }
              else
              {
                // map should be from protein seq to its coding dna
                cf.addMap(rsq, dss, xref.getMap().getMap().getInverse(),
                        xref.getMap().getMappedFromId());
              }
            }
          }
        }

        if (!found)
        {
          SequenceI matchedSeq = matcher.findIdMatch(
                  xref.getSource() + "|" + xref.getAccessionId());
          // if there was a match, check it's at least the right type of
          // molecule!
          if (matchedSeq != null && matchedSeq.isProtein() == fromDna)
          {
            if (constructMapping(seq, matchedSeq, xref, cf, fromDna))
            {
              found = true;
            }
          }
        }

        if (!found)
        {
          // do a bit more work - search for sequences with references matching
          // xrefs on this sequence.
          found = searchDataset(fromDna, dss, xref, rseqs, cf, false,
                  DBRefUtils.SEARCH_MODE_FULL);
        }
        if (found)
        {
          refIterator.remove();
        }
      }

      /*
       * fetch from source database any dbrefs we haven't resolved up to here
       */
      if (!sourceRefs.isEmpty())
      {
        retrieveCrossRef(sourceRefs, seq, xrfs, fromDna, cf);
      }
    }

    Alignment ral = null;
    if (rseqs.size() > 0)
    {
      ral = new Alignment(rseqs.toArray(new SequenceI[rseqs.size()]));
      if (!cf.isEmpty())
      {
        dataset.addCodonFrame(cf);
      }
    }
    return ral;
  }

  private void retrieveCrossRef(List<DBRefEntry> sourceRefs, SequenceI seq,
          List<DBRefEntry> xrfs, boolean fromDna, AlignedCodonFrame cf)
  {
    ASequenceFetcher sftch = SequenceFetcherFactory.getSequenceFetcher();
    SequenceI[] retrieved = null;
    SequenceI dss = seq.getDatasetSequence() == null ? seq
            : seq.getDatasetSequence();
    // first filter in case we are retrieving crossrefs that have already been
    // retrieved. this happens for cases where a database record doesn't yield
    // protein products for CDS
    removeAlreadyRetrievedSeqs(sourceRefs, fromDna);
    if (sourceRefs.size() == 0)
    {
      // no more work to do! We already had all requested sequence records in
      // the dataset.
      return;
    }
    try
    {
      retrieved = sftch.getSequences(sourceRefs, !fromDna);
    } catch (Exception e)
    {
      System.err.println(
              "Problem whilst retrieving cross references for Sequence : "
                      + seq.getName());
      e.printStackTrace();
    }

    if (retrieved != null)
    {
      boolean addedXref = false;
      List<SequenceI> newDsSeqs = new ArrayList<>(),
              doNotAdd = new ArrayList<>();

      for (SequenceI retrievedSequence : retrieved)
      {
        // dataset gets contaminated ccwith non-ds sequences. why ??!
        // try: Ensembl -> Nuc->Ensembl, Nuc->Uniprot-->Protein->EMBL->
        SequenceI retrievedDss = retrievedSequence
                .getDatasetSequence() == null ? retrievedSequence
                        : retrievedSequence.getDatasetSequence();
        addedXref |= importCrossRefSeq(cf, newDsSeqs, doNotAdd, dss,
                retrievedDss);
      }
      // JBPNote: What assumptions are made for dbref structures on
      // retrieved sequences ?
      // addedXref will be true means importCrossRefSeq found
      // sequences with dbrefs with mappings to sequences congruent with dss

      if (!addedXref)
      {
        // try again, after looking for matching IDs
        // shouldn't need to do this unless the dbref mechanism has broken.
        updateDbrefMappings(seq, xrfs, retrieved, cf, fromDna);
        for (SequenceI retrievedSequence : retrieved)
        {
          // dataset gets contaminated ccwith non-ds sequences. why ??!
          // try: Ensembl -> Nuc->Ensembl, Nuc->Uniprot-->Protein->EMBL->
          SequenceI retrievedDss = retrievedSequence
                  .getDatasetSequence() == null ? retrievedSequence
                          : retrievedSequence.getDatasetSequence();
          addedXref |= importCrossRefSeq(cf, newDsSeqs, doNotAdd, dss,
                  retrievedDss);
        }
      }
      for (SequenceI newToSeq : newDsSeqs)
      {
        if (!doNotAdd.contains(newToSeq)
                && dataset.findIndex(newToSeq) == -1)
        {
          dataset.addSequence(newToSeq);
          matcher.add(newToSeq);
        }
      }
    }
  }

  /**
   * Search dataset for sequences with a primary reference contained in
   * sourceRefs.
   * 
   * @param sourceRefs
   *          - list of references to filter.
   * @param fromDna
   *          - type of sequence to search for matching primary reference.
   */
  private void removeAlreadyRetrievedSeqs(List<DBRefEntry> sourceRefs,
          boolean fromDna)
  {
    List<DBRefEntry> dbrSourceSet = new ArrayList<>(sourceRefs);
    List<SequenceI> dsSeqs = dataset.getSequences();
    for (int ids = 0, nds = dsSeqs.size(); ids < nds; ids++)
    {
      SequenceI sq = dsSeqs.get(ids);
      boolean dupeFound = false;
      // !fromDna means we are looking only for nucleotide sequences, not
      // protein
      if (sq.isProtein() == fromDna)
      {
        List<DBRefEntry> sqdbrefs = sq.getPrimaryDBRefs();
        for (int idb = 0, ndb = sqdbrefs.size(); idb < ndb; idb++)
        {
          DBRefEntry dbr = sqdbrefs.get(idb);
          List<DBRefEntry> searchrefs = DBRefUtils.searchRefs(dbrSourceSet,
                  dbr, DBRefUtils.SEARCH_MODE_FULL);
          for (int isr = 0, nsr = searchrefs.size(); isr < nsr; isr++)
          {
            sourceRefs.remove(searchrefs.get(isr));
            dupeFound = true;
          }
        }
      }
      if (dupeFound)
      {
        // rebuild the search array from the filtered sourceRefs list
        dbrSourceSet.clear();
        dbrSourceSet.addAll(sourceRefs);
      }
    }
  }

  /**
   * process sequence retrieved via a dbref on source sequence to resolve and
   * transfer data JBPNote: as of 2022-02-03 - this assumes retrievedSequence
   * has dbRefs with Mapping references to a sequence congruent with
   * sourceSequence
   * 
   * @param cf
   * @param sourceSequence
   * @param retrievedSequence
   * @return true if retrieveSequence was imported
   */
  private boolean importCrossRefSeq(AlignedCodonFrame cf,
          List<SequenceI> newDsSeqs, List<SequenceI> doNotAdd,
          SequenceI sourceSequence, SequenceI retrievedSequence)
  {
    /**
     * set when retrievedSequence has been verified as a crossreference for
     * sourceSequence
     */
    boolean imported = false;
    List<DBRefEntry> dbr = retrievedSequence.getDBRefs();
    if (dbr != null)
    {
      for (int ib = 0, nb = dbr.size(); ib < nb; ib++)
      {

        DBRefEntry dbref = dbr.get(ib);
        // matched will return null if the dbref has no map
        SequenceI matched = findInDataset(dbref);
        if (matched == sourceSequence)
        {
          // verified retrieved and source sequence cross-reference each other
          imported = true;
        }
        // find any entry where we should put in the sequence being
        // cross-referenced into the map
        Mapping map = dbref.getMap();
        if (map != null)
        {
          SequenceI ms = map.getTo();
          if (ms != null && map.getMap() != null)
          {
            if (ms == sourceSequence)
            {
              // already called to import once, and most likely this sequence
              // already imported !
              continue;
            }
            if (matched == null)
            {
              /*
               * sequence is new to dataset, so save a reference so it can be added. 
               */
              newDsSeqs.add(ms);
              continue;
            }

            /*
             * there was a matching sequence in dataset, so now, check to see if we can update the map.getTo() sequence to the existing one.
             */

            try
            {
              // compare ms with dss and replace with dss in mapping
              // if map is congruent
              // TODO findInDataset requires exact sequence match but
              // 'congruent' test is only for the mapped part
              // maybe not a problem in practice since only ENA provide a
              // mapping and it is to the full protein translation of CDS
              // matcher.findIdMatch(map.getTo());
              // TODO addendum: if matched is shorter than getTo, this will fail
              // - when it should really succeed.
              int sf = map.getMap().getToLowest();
              int st = map.getMap().getToHighest();
              SequenceI mappedrg = ms.getSubSequence(sf, st);
              if (mappedrg.getLength() > 0 && ms.getSequenceAsString()
                      .equals(matched.getSequenceAsString()))
              {
                /*
                 * sequences were a match, 
                 */
                String msg = "Mapping updated from " + ms.getName()
                        + " to retrieved crossreference "
                        + matched.getName();
                System.out.println(msg);

                List<DBRefEntry> toRefs = map.getTo().getDBRefs();
                if (toRefs != null)
                {
                  /*
                   * transfer database refs
                   */
                  for (DBRefEntry ref : toRefs)
                  {
                    if (dbref.getSrcAccString()
                            .equals(ref.getSrcAccString()))
                    {
                      continue; // avoid overwriting the ref on source sequence
                    }
                    matched.addDBRef(ref); // add or update mapping
                  }
                }
                doNotAdd.add(map.getTo());
                map.setTo(matched);

                /*
                 * give the reverse reference the inverse mapping 
                 * (if it doesn't have one already)
                 */
                setReverseMapping(matched, dbref, cf);

                /*
                 * copy sequence features as well, avoiding
                 * duplication (e.g. same variation from two 
                 * transcripts)
                 */
                List<SequenceFeature> sfs = ms.getFeatures()
                        .getAllFeatures();
                for (SequenceFeature feat : sfs)
                {
                  /*
                   * make a flyweight feature object which ignores Parent
                   * attribute in equality test; this avoids creating many
                   * otherwise duplicate exon features on genomic sequence
                   */
                  SequenceFeature newFeature = new SequenceFeature(feat)
                  {
                    @Override
                    public boolean equals(Object o)
                    {
                      return super.equals(o, true);
                    }
                  };
                  matched.addSequenceFeature(newFeature);
                }
              }
              cf.addMap(retrievedSequence, map.getTo(), map.getMap());
            } catch (Exception e)
            {
              System.err.println(
                      "Exception when consolidating Mapped sequence set...");
              e.printStackTrace(System.err);
            }
          }
        }
      }
    }
    if (imported)
    {
      retrievedSequence.updatePDBIds();
      rseqs.add(retrievedSequence);
      if (dataset.findIndex(retrievedSequence) == -1)
      {
        dataset.addSequence(retrievedSequence);
        matcher.add(retrievedSequence);
      }
    }
    return imported;
  }

  /**
   * Sets the inverse sequence mapping in the corresponding dbref of the mapped
   * to sequence (if any). This is used after fetching a cross-referenced
   * sequence, if the fetched sequence has a mapping to the original sequence,
   * to set the mapping in the original sequence's dbref.
   * 
   * @param mapFrom
   *          the sequence mapped from
   * @param dbref
   * @param mappings
   */
  void setReverseMapping(SequenceI mapFrom, DBRefEntry dbref,
          AlignedCodonFrame mappings)
  {
    SequenceI mapTo = dbref.getMap().getTo();
    if (mapTo == null)
    {
      return;
    }
    List<DBRefEntry> dbrefs = mapTo.getDBRefs();
    if (dbrefs == null)
    {
      return;
    }
    for (DBRefEntry toRef : dbrefs)
    {
      if (toRef.hasMap() && mapFrom == toRef.getMap().getTo())
      {
        /*
         * found the reverse dbref; update its mapping if null
         */
        if (toRef.getMap().getMap() == null)
        {
          MapList inverse = dbref.getMap().getMap().getInverse();
          toRef.getMap().setMap(inverse);
          mappings.addMap(mapTo, mapFrom, inverse);
        }
      }
    }
  }

  /**
   * Returns null or the first sequence in the dataset which is identical to
   * xref.mapTo, and has a) a primary dbref matching xref, or if none found, the
   * first one with an ID source|xrefacc JBPNote: Could refactor this to
   * AlignmentI/DatasetI
   * 
   * @param xref
   *          with map and mapped-to sequence
   * @return
   */
  SequenceI findInDataset(DBRefEntry xref)
  {
    if (xref == null || !xref.hasMap() || xref.getMap().getTo() == null)
    {
      return null;
    }
    SequenceI mapsTo = xref.getMap().getTo();
    String name = xref.getAccessionId();
    String name2 = xref.getSource() + "|" + name;
    SequenceI dss = mapsTo.getDatasetSequence() == null ? mapsTo
            : mapsTo.getDatasetSequence();
    // first check ds if ds is directly referenced
    if (dataset.findIndex(dss) > -1)
    {
      return dss;
    }
    DBRefEntry template = new DBRefEntry(xref.getSource(), null,
            xref.getAccessionId());
    /**
     * remember the first ID match - in case we don't find a match to template
     */
    SequenceI firstIdMatch = null;
    for (SequenceI seq : dataset.getSequences())
    {
      // first check primary refs.
      List<DBRefEntry> match = DBRefUtils.searchRefs(seq.getPrimaryDBRefs(),
              template, DBRefUtils.SEARCH_MODE_FULL);
      if (match != null && match.size() == 1 && sameSequence(seq, dss))
      {
        return seq;
      }
      /*
       * clumsy alternative to using SequenceIdMatcher which currently
       * returns sequences with a dbref to the matched accession id 
       * which we don't want
       */
      if (firstIdMatch == null && (name.equals(seq.getName())
              || seq.getName().startsWith(name2)))
      {
        if (sameSequence(seq, dss))
        {
          firstIdMatch = seq;
        }
      }
    }
    return firstIdMatch;
  }

  /**
   * Answers true if seq1 and seq2 contain exactly the same characters (ignoring
   * case), else false. This method compares the lengths, then each character in
   * turn, in order to 'fail fast'. For case-sensitive comparison, it would be
   * possible to use Arrays.equals(seq1.getSequence(), seq2.getSequence()).
   * 
   * @param seq1
   * @param seq2
   * @return
   */
  // TODO move to Sequence / SequenceI
  static boolean sameSequence(SequenceI seq1, SequenceI seq2)
  {
    if (seq1 == seq2)
    {
      return true;
    }
    if (seq1 == null || seq2 == null)
    {
      return false;
    }

    if (seq1.getLength() != seq2.getLength())
    {
      return false;
    }
    int length = seq1.getLength();
    for (int i = 0; i < length; i++)
    {
      int diff = seq1.getCharAt(i) - seq2.getCharAt(i);
      /*
       * same char or differ in case only ('a'-'A' == 32)
       */
      if (diff != 0 && diff != 32 && diff != -32)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Updates any empty mappings in the cross-references with one to a compatible
   * retrieved sequence if found, and adds any new mappings to the
   * AlignedCodonFrame JBPNote: TODO: this relies on sequence IDs like
   * UNIPROT|ACCESSION - which do not always happen.
   * 
   * @param mapFrom
   * @param xrefs
   * @param retrieved
   * @param acf
   */
  void updateDbrefMappings(SequenceI mapFrom, List<DBRefEntry> xrefs,
          SequenceI[] retrieved, AlignedCodonFrame acf, boolean fromDna)
  {
    SequenceIdMatcher idMatcher = new SequenceIdMatcher(retrieved);
    for (DBRefEntry xref : xrefs)
    {
      if (!xref.hasMap())
      {
        String targetSeqName = xref.getSource() + "|"
                + xref.getAccessionId();
        SequenceI[] matches = idMatcher.findAllIdMatches(targetSeqName);
        if (matches == null)
        {
          return;
        }
        for (SequenceI seq : matches)
        {
          constructMapping(mapFrom, seq, xref, acf, fromDna);
        }
      }
    }
  }

  /**
   * Tries to make a mapping between sequences. If successful, adds the mapping
   * to the dbref and the mappings collection and answers true, otherwise
   * answers false. The following methods of making are mapping are tried in
   * turn:
   * <ul>
   * <li>if 'mapTo' holds a mapping to 'mapFrom', take the inverse; this is, for
   * example, the case after fetching EMBL cross-references for a Uniprot
   * sequence</li>
   * <li>else check if the dna translates exactly to the protein (give or take
   * start and stop codons></li>
   * <li>else try to map based on CDS features on the dna sequence</li>
   * </ul>
   * 
   * @param mapFrom
   * @param mapTo
   * @param xref
   * @param mappings
   * @return
   */
  boolean constructMapping(SequenceI mapFrom, SequenceI mapTo,
          DBRefEntry xref, AlignedCodonFrame mappings, boolean fromDna)
  {
    MapList mapping = null;
    SequenceI dsmapFrom = mapFrom.getDatasetSequence() == null ? mapFrom
            : mapFrom.getDatasetSequence();
    SequenceI dsmapTo = mapTo.getDatasetSequence() == null ? mapTo
            : mapTo.getDatasetSequence();
    /*
     * look for a reverse mapping, if found make its inverse. 
     * Note - we do this on dataset sequences only.
     */
    if (dsmapTo.getDBRefs() != null)
    {
      for (DBRefEntry dbref : dsmapTo.getDBRefs())
      {
        String name = dbref.getSource() + "|" + dbref.getAccessionId();
        if (dbref.hasMap() && dsmapFrom.getName().startsWith(name))
        {
          /*
           * looks like we've found a map from 'mapTo' to 'mapFrom'
           * - invert it to make the mapping the other way 
           */
          MapList reverse = dbref.getMap().getMap().getInverse();
          xref.setMap(new Mapping(dsmapTo, reverse));
          mappings.addMap(mapFrom, dsmapTo, reverse);
          return true;
        }
      }
    }

    if (fromDna)
    {
      mapping = AlignmentUtils.mapCdnaToProtein(mapTo, mapFrom);
    }
    else
    {
      mapping = AlignmentUtils.mapCdnaToProtein(mapFrom, mapTo);
      if (mapping != null)
      {
        mapping = mapping.getInverse();
      }
    }
    if (mapping == null)
    {
      return false;
    }
    xref.setMap(new Mapping(mapTo, mapping));

    /*
     * and add a reverse DbRef with the inverse mapping
     */
    if (mapFrom.getDatasetSequence() != null && false)
    // && mapFrom.getDatasetSequence().getSourceDBRef() != null)
    {
      // possible need to search primary references... except, why doesn't xref
      // == getSourceDBRef ??
      // DBRefEntry dbref = new DBRefEntry(mapFrom.getDatasetSequence()
      // .getSourceDBRef());
      // dbref.setMap(new Mapping(mapFrom.getDatasetSequence(), mapping
      // .getInverse()));
      // mapTo.addDBRef(dbref);
    }

    if (fromDna)
    {
      // AlignmentUtils.computeProteinFeatures(mapFrom, mapTo, mapping);
      mappings.addMap(mapFrom, mapTo, mapping);
    }
    else
    {
      mappings.addMap(mapTo, mapFrom, mapping.getInverse());
    }

    return true;
  }

  /**
   * find references to lrfs in the cross-reference set of each sequence in
   * dataset (that is not equal to sequenceI) Identifies matching DBRefEntry
   * based on source and accession string only - Map and Version are nulled.
   * 
   * @param fromDna
   *          - true if context was searching from Dna sequences, false if
   *          context was searching from Protein sequences
   * @param sequenceI
   * @param lrfs
   * @param foundSeqs
   * @return true if matches were found.
   */
  private boolean searchDatasetXrefs(boolean fromDna, SequenceI sequenceI,
          List<DBRefEntry> lrfs, List<SequenceI> foundSeqs,
          AlignedCodonFrame cf)
  {
    boolean found = false;
    if (lrfs == null)
    {
      return false;
    }
    for (int i = 0, n = lrfs.size(); i < n; i++)
    {
      // DBRefEntry xref = new DBRefEntry(lrfs.get(i));
      // // add in wildcards
      // xref.setVersion(null);
      // xref.setMap(null);
      found |= searchDataset(fromDna, sequenceI, lrfs.get(i), foundSeqs, cf,
              false, DBRefUtils.SEARCH_MODE_NO_MAP_NO_VERSION);
    }
    return found;
  }

  /**
   * Searches dataset for DBRefEntrys matching the given one (xrf) and adds the
   * associated sequence to rseqs
   * 
   * @param fromDna
   *          true if context was searching for refs *from* dna sequence, false
   *          if context was searching for refs *from* protein sequence
   * @param fromSeq
   *          a sequence to ignore (start point of search)
   * @param xrf
   *          a cross-reference to try to match
   * @param foundSeqs
   *          result list to add to
   * @param mappings
   *          a set of sequence mappings to add to
   * @param direct
   *          - indicates the type of relationship between returned sequences,
   *          xrf, and sequenceI that is required.
   *          <ul>
   *          <li>direct implies xrf is a primary reference for sequenceI AND
   *          the sequences to be located (eg a uniprot ID for a protein
   *          sequence, and a uniprot ref on a transcript sequence).</li>
   *          <li>indirect means xrf is a cross reference with respect to
   *          sequenceI or all the returned sequences (eg a genomic reference
   *          associated with a locus and one or more transcripts)</li>
   *          </ul>
   * @param mode
   *          SEARCH_MODE_FULL for all; SEARCH_MODE_NO_MAP_NO_VERSION optional
   * @return true if relationship found and sequence added.
   */
  boolean searchDataset(boolean fromDna, SequenceI fromSeq, DBRefEntry xrf,
          List<SequenceI> foundSeqs, AlignedCodonFrame mappings,
          boolean direct, int mode)
  {
    boolean found = false;
    if (dataset == null)
    {
      return false;
    }
    if (dataset.getSequences() == null)
    {
      System.err.println("Empty dataset sequence set - NO VECTOR");
      return false;
    }
    List<SequenceI> ds = dataset.getSequences();
    synchronized (ds)
    {
      for (SequenceI nxt : ds)
      {
        if (nxt != null)
        {
          if (nxt.getDatasetSequence() != null)
          {
            System.err.println(
                    "Implementation warning: CrossRef initialised with a dataset alignment with non-dataset sequences in it! ("
                            + nxt.getDisplayId(true) + " has ds reference "
                            + nxt.getDatasetSequence().getDisplayId(true)
                            + ")");
          }
          if (nxt == fromSeq || nxt == fromSeq.getDatasetSequence())
          {
            continue;
          }
          /*
           * only look at same molecule type if 'direct', or
           * complementary type if !direct
           */
          {
            boolean isDna = !nxt.isProtein();
            if (direct ? (isDna != fromDna) : (isDna == fromDna))
            {
              // skip this sequence because it is wrong molecule type
              continue;
            }
          }

          // look for direct or indirect references in common
          List<DBRefEntry> poss = nxt.getDBRefs();
          List<DBRefEntry> cands = null;

          // todo: indirect specifies we select either direct references to nxt
          // that match xrf which is indirect to sequenceI, or indirect
          // references to nxt that match xrf which is direct to sequenceI
          cands = DBRefUtils.searchRefs(poss, xrf, mode);
          // else
          // {
          // poss = DBRefUtils.selectDbRefs(nxt.isProtein()!fromDna, poss);
          // cands = DBRefUtils.searchRefs(poss, xrf);
          // }
          if (!cands.isEmpty())
          {
            if (foundSeqs.contains(nxt))
            {
              continue;
            }
            found = true;
            foundSeqs.add(nxt);
            if (mappings != null && !direct)
            {
              /*
               * if the matched sequence has mapped dbrefs to
               * protein product / cdna, add equivalent mappings to
               * our source sequence
               */
              for (DBRefEntry candidate : cands)
              {
                Mapping mapping = candidate.getMap();
                if (mapping != null)
                {
                  MapList map = mapping.getMap();
                  if (mapping.getTo() != null
                          && map.getFromRatio() != map.getToRatio())
                  {
                    /*
                     * add a mapping, as from dna to peptide sequence
                     */
                    if (map.getFromRatio() == 3)
                    {
                      mappings.addMap(nxt, fromSeq, map);
                    }
                    else
                    {
                      mappings.addMap(nxt, fromSeq, map.getInverse());
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return found;
  }
}
