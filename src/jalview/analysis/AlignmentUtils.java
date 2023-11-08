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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import jalview.bin.Console;
import jalview.commands.RemoveGapColCommand;
import jalview.datamodel.AlignedCodon;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignedCodonFrame.SequenceToSequenceMapping;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.IncompleteCodonException;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.io.gff.SequenceOntologyI;
import jalview.schemes.ResidueProperties;
import jalview.util.Comparison;
import jalview.util.DBRefUtils;
import jalview.util.IntRangeComparator;
import jalview.util.MapList;
import jalview.util.MappingUtils;

/**
 * grab bag of useful alignment manipulation operations Expect these to be
 * refactored elsewhere at some point.
 * 
 * @author jimp
 * 
 */
public class AlignmentUtils
{
  private static final int CODON_LENGTH = 3;

  private static final String SEQUENCE_VARIANT = "sequence_variant:";

  /*
   * the 'id' attribute is provided for variant features fetched from
   * Ensembl using its REST service with JSON format 
   */
  public static final String VARIANT_ID = "id";

  /**
   * A data model to hold the 'normal' base value at a position, and an optional
   * sequence variant feature
   */
  static final class DnaVariant
  {
    final String base;

    SequenceFeature variant;

    DnaVariant(String nuc)
    {
      base = nuc;
      variant = null;
    }

    DnaVariant(String nuc, SequenceFeature var)
    {
      base = nuc;
      variant = var;
    }

    public String getSource()
    {
      return variant == null ? null : variant.getFeatureGroup();
    }

    /**
     * toString for aid in the debugger only
     */
    @Override
    public String toString()
    {
      return base + ":" + (variant == null ? "" : variant.getDescription());
    }
  }

  /**
   * given an existing alignment, create a new alignment including all, or up to
   * flankSize additional symbols from each sequence's dataset sequence
   * 
   * @param core
   * @param flankSize
   * @return AlignmentI
   */
  public static AlignmentI expandContext(AlignmentI core, int flankSize)
  {
    List<SequenceI> sq = new ArrayList<>();
    int maxoffset = 0;
    for (SequenceI s : core.getSequences())
    {
      SequenceI newSeq = s.deriveSequence();
      final int newSeqStart = newSeq.getStart() - 1;
      if (newSeqStart > maxoffset
              && newSeq.getDatasetSequence().getStart() < s.getStart())
      {
        maxoffset = newSeqStart;
      }
      sq.add(newSeq);
    }
    if (flankSize > -1)
    {
      maxoffset = Math.min(maxoffset, flankSize);
    }

    /*
     * now add offset left and right to create an expanded alignment
     */
    for (SequenceI s : sq)
    {
      SequenceI ds = s;
      while (ds.getDatasetSequence() != null)
      {
        ds = ds.getDatasetSequence();
      }
      int s_end = s.findPosition(s.getStart() + s.getLength());
      // find available flanking residues for sequence
      int ustream_ds = s.getStart() - ds.getStart();
      int dstream_ds = ds.getEnd() - s_end;

      // build new flanked sequence

      // compute gap padding to start of flanking sequence
      int offset = maxoffset - ustream_ds;

      // padding is gapChar x ( maxoffset - min(ustream_ds, flank)
      if (flankSize >= 0)
      {
        if (flankSize < ustream_ds)
        {
          // take up to flankSize residues
          offset = maxoffset - flankSize;
          ustream_ds = flankSize;
        }
        if (flankSize <= dstream_ds)
        {
          dstream_ds = flankSize - 1;
        }
      }
      // TODO use Character.toLowerCase to avoid creating String objects?
      char[] upstream = new String(ds
              .getSequence(s.getStart() - 1 - ustream_ds, s.getStart() - 1))
                      .toLowerCase(Locale.ROOT).toCharArray();
      char[] downstream = new String(
              ds.getSequence(s_end - 1, s_end + dstream_ds))
                      .toLowerCase(Locale.ROOT).toCharArray();
      char[] coreseq = s.getSequence();
      char[] nseq = new char[offset + upstream.length + downstream.length
              + coreseq.length];
      char c = core.getGapCharacter();

      int p = 0;
      for (; p < offset; p++)
      {
        nseq[p] = c;
      }

      System.arraycopy(upstream, 0, nseq, p, upstream.length);
      System.arraycopy(coreseq, 0, nseq, p + upstream.length,
              coreseq.length);
      System.arraycopy(downstream, 0, nseq,
              p + coreseq.length + upstream.length, downstream.length);
      s.setSequence(new String(nseq));
      s.setStart(s.getStart() - ustream_ds);
      s.setEnd(s_end + downstream.length);
    }
    AlignmentI newAl = new jalview.datamodel.Alignment(
            sq.toArray(new SequenceI[0]));
    for (SequenceI s : sq)
    {
      if (s.getAnnotation() != null)
      {
        for (AlignmentAnnotation aa : s.getAnnotation())
        {
          aa.adjustForAlignment(); // JAL-1712 fix
          newAl.addAnnotation(aa);
        }
      }
    }
    newAl.setDataset(core.getDataset());
    return newAl;
  }

  /**
   * Returns the index (zero-based position) of a sequence in an alignment, or
   * -1 if not found.
   * 
   * @param al
   * @param seq
   * @return
   */
  public static int getSequenceIndex(AlignmentI al, SequenceI seq)
  {
    int result = -1;
    int pos = 0;
    for (SequenceI alSeq : al.getSequences())
    {
      if (alSeq == seq)
      {
        result = pos;
        break;
      }
      pos++;
    }
    return result;
  }

  /**
   * Returns a map of lists of sequences in the alignment, keyed by sequence
   * name. For use in mapping between different alignment views of the same
   * sequences.
   * 
   * @see jalview.datamodel.AlignmentI#getSequencesByName()
   */
  public static Map<String, List<SequenceI>> getSequencesByName(
          AlignmentI al)
  {
    Map<String, List<SequenceI>> theMap = new LinkedHashMap<>();
    for (SequenceI seq : al.getSequences())
    {
      String name = seq.getName();
      if (name != null)
      {
        List<SequenceI> seqs = theMap.get(name);
        if (seqs == null)
        {
          seqs = new ArrayList<>();
          theMap.put(name, seqs);
        }
        seqs.add(seq);
      }
    }
    return theMap;
  }

  /**
   * Build mapping of protein to cDNA alignment. Mappings are made between
   * sequences where the cDNA translates to the protein sequence. Any new
   * mappings are added to the protein alignment. Returns true if any mappings
   * either already exist or were added, else false.
   * 
   * @param proteinAlignment
   * @param cdnaAlignment
   * @return
   */
  public static boolean mapProteinAlignmentToCdna(
          final AlignmentI proteinAlignment, final AlignmentI cdnaAlignment)
  {
    if (proteinAlignment == null || cdnaAlignment == null)
    {
      return false;
    }

    Set<SequenceI> mappedDna = new HashSet<>();
    Set<SequenceI> mappedProtein = new HashSet<>();

    /*
     * First pass - map sequences where cross-references exist. This include
     * 1-to-many mappings to support, for example, variant cDNA.
     */
    boolean mappingPerformed = mapProteinToCdna(proteinAlignment,
            cdnaAlignment, mappedDna, mappedProtein, true);

    /*
     * Second pass - map sequences where no cross-references exist. This only
     * does 1-to-1 mappings and assumes corresponding sequences are in the same
     * order in the alignments.
     */
    mappingPerformed |= mapProteinToCdna(proteinAlignment, cdnaAlignment,
            mappedDna, mappedProtein, false);
    return mappingPerformed;
  }

  /**
   * Make mappings between compatible sequences (where the cDNA translation
   * matches the protein).
   * 
   * @param proteinAlignment
   * @param cdnaAlignment
   * @param mappedDna
   *          a set of mapped DNA sequences (to add to)
   * @param mappedProtein
   *          a set of mapped Protein sequences (to add to)
   * @param xrefsOnly
   *          if true, only map sequences where xrefs exist
   * @return
   */
  protected static boolean mapProteinToCdna(
          final AlignmentI proteinAlignment, final AlignmentI cdnaAlignment,
          Set<SequenceI> mappedDna, Set<SequenceI> mappedProtein,
          boolean xrefsOnly)
  {
    boolean mappingExistsOrAdded = false;
    List<SequenceI> thisSeqs = proteinAlignment.getSequences();
    for (SequenceI aaSeq : thisSeqs)
    {
      boolean proteinMapped = false;
      AlignedCodonFrame acf = new AlignedCodonFrame();

      for (SequenceI cdnaSeq : cdnaAlignment.getSequences())
      {
        /*
         * Always try to map if sequences have xref to each other; this supports
         * variant cDNA or alternative splicing for a protein sequence.
         * 
         * If no xrefs, try to map progressively, assuming that alignments have
         * mappable sequences in corresponding order. These are not
         * many-to-many, as that would risk mixing species with similar cDNA
         * sequences.
         */
        if (xrefsOnly && !AlignmentUtils.haveCrossRef(aaSeq, cdnaSeq))
        {
          continue;
        }

        /*
         * Don't map non-xrefd sequences more than once each. This heuristic
         * allows us to pair up similar sequences in ordered alignments.
         */
        if (!xrefsOnly && (mappedProtein.contains(aaSeq)
                || mappedDna.contains(cdnaSeq)))
        {
          continue;
        }
        if (mappingExists(proteinAlignment.getCodonFrames(),
                aaSeq.getDatasetSequence(), cdnaSeq.getDatasetSequence()))
        {
          mappingExistsOrAdded = true;
        }
        else
        {
          MapList map = mapCdnaToProtein(aaSeq, cdnaSeq);
          if (map != null)
          {
            acf.addMap(cdnaSeq, aaSeq, map);
            mappingExistsOrAdded = true;
            proteinMapped = true;
            mappedDna.add(cdnaSeq);
            mappedProtein.add(aaSeq);
          }
        }
      }
      if (proteinMapped)
      {
        proteinAlignment.addCodonFrame(acf);
      }
    }
    return mappingExistsOrAdded;
  }

  /**
   * Answers true if the mappings include one between the given (dataset)
   * sequences.
   */
  protected static boolean mappingExists(List<AlignedCodonFrame> mappings,
          SequenceI aaSeq, SequenceI cdnaSeq)
  {
    if (mappings != null)
    {
      for (AlignedCodonFrame acf : mappings)
      {
        if (cdnaSeq == acf.getDnaForAaSeq(aaSeq))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Builds a mapping (if possible) of a cDNA to a protein sequence.
   * <ul>
   * <li>first checks if the cdna translates exactly to the protein
   * sequence</li>
   * <li>else checks for translation after removing a STOP codon</li>
   * <li>else checks for translation after removing a START codon</li>
   * <li>if that fails, inspect CDS features on the cDNA sequence</li>
   * </ul>
   * Returns null if no mapping is determined.
   * 
   * @param proteinSeq
   *          the aligned protein sequence
   * @param cdnaSeq
   *          the aligned cdna sequence
   * @return
   */
  public static MapList mapCdnaToProtein(SequenceI proteinSeq,
          SequenceI cdnaSeq)
  {
    /*
     * Here we handle either dataset sequence set (desktop) or absent (applet).
     * Use only the char[] form of the sequence to avoid creating possibly large
     * String objects.
     */
    final SequenceI proteinDataset = proteinSeq.getDatasetSequence();
    char[] aaSeqChars = proteinDataset != null
            ? proteinDataset.getSequence()
            : proteinSeq.getSequence();
    final SequenceI cdnaDataset = cdnaSeq.getDatasetSequence();
    char[] cdnaSeqChars = cdnaDataset != null ? cdnaDataset.getSequence()
            : cdnaSeq.getSequence();
    if (aaSeqChars == null || cdnaSeqChars == null)
    {
      return null;
    }

    /*
     * cdnaStart/End, proteinStartEnd are base 1 (for dataset sequence mapping)
     */
    final int mappedLength = CODON_LENGTH * aaSeqChars.length;
    int cdnaLength = cdnaSeqChars.length;
    int cdnaStart = cdnaSeq.getStart();
    int cdnaEnd = cdnaSeq.getEnd();
    final int proteinStart = proteinSeq.getStart();
    final int proteinEnd = proteinSeq.getEnd();

    /*
     * If lengths don't match, try ignoring stop codon (if present)
     */
    if (cdnaLength != mappedLength && cdnaLength > 2)
    {
      String lastCodon = String.valueOf(cdnaSeqChars,
              cdnaLength - CODON_LENGTH, CODON_LENGTH)
              .toUpperCase(Locale.ROOT);
      for (String stop : ResidueProperties.STOP_CODONS)
      {
        if (lastCodon.equals(stop))
        {
          cdnaEnd -= CODON_LENGTH;
          cdnaLength -= CODON_LENGTH;
          break;
        }
      }
    }

    /*
     * If lengths still don't match, try ignoring start codon.
     */
    int startOffset = 0;
    if (cdnaLength != mappedLength && cdnaLength > 2
            && String.valueOf(cdnaSeqChars, 0, CODON_LENGTH)
                    .toUpperCase(Locale.ROOT)
                    .equals(ResidueProperties.START))
    {
      startOffset += CODON_LENGTH;
      cdnaStart += CODON_LENGTH;
      cdnaLength -= CODON_LENGTH;
    }

    if (translatesAs(cdnaSeqChars, startOffset, aaSeqChars))
    {
      /*
       * protein is translation of dna (+/- start/stop codons)
       */
      MapList map = new MapList(new int[] { cdnaStart, cdnaEnd },
              new int[]
              { proteinStart, proteinEnd }, CODON_LENGTH, 1);
      return map;
    }

    /*
     * translation failed - try mapping CDS annotated regions of dna
     */
    return mapCdsToProtein(cdnaSeq, proteinSeq);
  }

  /**
   * Test whether the given cdna sequence, starting at the given offset,
   * translates to the given amino acid sequence, using the standard translation
   * table. Designed to fail fast i.e. as soon as a mismatch position is found.
   * 
   * @param cdnaSeqChars
   * @param cdnaStart
   * @param aaSeqChars
   * @return
   */
  protected static boolean translatesAs(char[] cdnaSeqChars, int cdnaStart,
          char[] aaSeqChars)
  {
    if (cdnaSeqChars == null || aaSeqChars == null)
    {
      return false;
    }

    int aaPos = 0;
    int dnaPos = cdnaStart;
    for (; dnaPos < cdnaSeqChars.length - 2
            && aaPos < aaSeqChars.length; dnaPos += CODON_LENGTH, aaPos++)
    {
      String codon = String.valueOf(cdnaSeqChars, dnaPos, CODON_LENGTH);
      final String translated = ResidueProperties.codonTranslate(codon);

      /*
       * allow * in protein to match untranslatable in dna
       */
      final char aaRes = aaSeqChars[aaPos];
      if ((translated == null || ResidueProperties.STOP.equals(translated))
              && aaRes == '*')
      {
        continue;
      }
      if (translated == null || !(aaRes == translated.charAt(0)))
      {
        // debug
        // System.out.println(("Mismatch at " + i + "/" + aaResidue + ": "
        // + codon + "(" + translated + ") != " + aaRes));
        return false;
      }
    }

    /*
     * check we matched all of the protein sequence
     */
    if (aaPos != aaSeqChars.length)
    {
      return false;
    }

    /*
     * check we matched all of the dna except
     * for optional trailing STOP codon
     */
    if (dnaPos == cdnaSeqChars.length)
    {
      return true;
    }
    if (dnaPos == cdnaSeqChars.length - CODON_LENGTH)
    {
      String codon = String.valueOf(cdnaSeqChars, dnaPos, CODON_LENGTH);
      if (ResidueProperties.STOP
              .equals(ResidueProperties.codonTranslate(codon)))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Align sequence 'seq' to match the alignment of a mapped sequence. Note this
   * currently assumes that we are aligning cDNA to match protein.
   * 
   * @param seq
   *          the sequence to be realigned
   * @param al
   *          the alignment whose sequence alignment is to be 'copied'
   * @param gap
   *          character string represent a gap in the realigned sequence
   * @param preserveUnmappedGaps
   * @param preserveMappedGaps
   * @return true if the sequence was realigned, false if it could not be
   */
  public static boolean alignSequenceAs(SequenceI seq, AlignmentI al,
          String gap, boolean preserveMappedGaps,
          boolean preserveUnmappedGaps)
  {
    /*
     * Get any mappings from the source alignment to the target (dataset)
     * sequence.
     */
    // TODO there may be one AlignedCodonFrame per dataset sequence, or one with
    // all mappings. Would it help to constrain this?
    List<AlignedCodonFrame> mappings = al.getCodonFrame(seq);
    if (mappings == null || mappings.isEmpty())
    {
      return false;
    }

    /*
     * Locate the aligned source sequence whose dataset sequence is mapped. We
     * just take the first match here (as we can't align like more than one
     * sequence).
     */
    SequenceI alignFrom = null;
    AlignedCodonFrame mapping = null;
    for (AlignedCodonFrame mp : mappings)
    {
      alignFrom = mp.findAlignedSequence(seq, al);
      if (alignFrom != null)
      {
        mapping = mp;
        break;
      }
    }

    if (alignFrom == null)
    {
      return false;
    }
    alignSequenceAs(seq, alignFrom, mapping, gap, al.getGapCharacter(),
            preserveMappedGaps, preserveUnmappedGaps);
    return true;
  }

  /**
   * Align sequence 'alignTo' the same way as 'alignFrom', using the mapping to
   * match residues and codons. Flags control whether existing gaps in unmapped
   * (intron) and mapped (exon) regions are preserved or not. Gaps between
   * intron and exon are only retained if both flags are set.
   * 
   * @param alignTo
   * @param alignFrom
   * @param mapping
   * @param myGap
   * @param sourceGap
   * @param preserveUnmappedGaps
   * @param preserveMappedGaps
   */
  public static void alignSequenceAs(SequenceI alignTo, SequenceI alignFrom,
          AlignedCodonFrame mapping, String myGap, char sourceGap,
          boolean preserveMappedGaps, boolean preserveUnmappedGaps)
  {
    // TODO generalise to work for Protein-Protein, dna-dna, dna-protein

    // aligned and dataset sequence positions, all base zero
    int thisSeqPos = 0;
    int sourceDsPos = 0;

    int basesWritten = 0;
    char myGapChar = myGap.charAt(0);
    int ratio = myGap.length();

    int fromOffset = alignFrom.getStart() - 1;
    int toOffset = alignTo.getStart() - 1;
    int sourceGapMappedLength = 0;
    boolean inExon = false;
    final int toLength = alignTo.getLength();
    final int fromLength = alignFrom.getLength();
    StringBuilder thisAligned = new StringBuilder(2 * toLength);

    /*
     * Traverse the 'model' aligned sequence
     */
    for (int i = 0; i < fromLength; i++)
    {
      char sourceChar = alignFrom.getCharAt(i);
      if (sourceChar == sourceGap)
      {
        sourceGapMappedLength += ratio;
        continue;
      }

      /*
       * Found a non-gap character. Locate its mapped region if any.
       */
      sourceDsPos++;
      // Note mapping positions are base 1, our sequence positions base 0
      int[] mappedPos = mapping.getMappedRegion(alignTo, alignFrom,
              sourceDsPos + fromOffset);
      if (mappedPos == null)
      {
        /*
         * unmapped position; treat like a gap
         */
        sourceGapMappedLength += ratio;
        // System.err.println("Can't align: no codon mapping to residue "
        // + sourceDsPos + "(" + sourceChar + ")");
        // return;
        continue;
      }

      int mappedCodonStart = mappedPos[0]; // position (1...) of codon start
      int mappedCodonEnd = mappedPos[mappedPos.length - 1]; // codon end pos
      StringBuilder trailingCopiedGap = new StringBuilder();

      /*
       * Copy dna sequence up to and including this codon. Optionally, include
       * gaps before the codon starts (in introns) and/or after the codon starts
       * (in exons).
       * 
       * Note this only works for 'linear' splicing, not reverse or interleaved.
       * But then 'align dna as protein' doesn't make much sense otherwise.
       */
      int intronLength = 0;
      while (basesWritten + toOffset < mappedCodonEnd
              && thisSeqPos < toLength)
      {
        final char c = alignTo.getCharAt(thisSeqPos++);
        if (c != myGapChar)
        {
          basesWritten++;
          int sourcePosition = basesWritten + toOffset;
          if (sourcePosition < mappedCodonStart)
          {
            /*
             * Found an unmapped (intron) base. First add in any preceding gaps
             * (if wanted).
             */
            if (preserveUnmappedGaps && trailingCopiedGap.length() > 0)
            {
              thisAligned.append(trailingCopiedGap.toString());
              intronLength += trailingCopiedGap.length();
              trailingCopiedGap = new StringBuilder();
            }
            intronLength++;
            inExon = false;
          }
          else
          {
            final boolean startOfCodon = sourcePosition == mappedCodonStart;
            int gapsToAdd = calculateGapsToInsert(preserveMappedGaps,
                    preserveUnmappedGaps, sourceGapMappedLength, inExon,
                    trailingCopiedGap.length(), intronLength, startOfCodon);
            for (int k = 0; k < gapsToAdd; k++)
            {
              thisAligned.append(myGapChar);
            }
            sourceGapMappedLength = 0;
            inExon = true;
          }
          thisAligned.append(c);
          trailingCopiedGap = new StringBuilder();
        }
        else
        {
          if (inExon && preserveMappedGaps)
          {
            trailingCopiedGap.append(myGapChar);
          }
          else if (!inExon && preserveUnmappedGaps)
          {
            trailingCopiedGap.append(myGapChar);
          }
        }
      }
    }

    /*
     * At end of model aligned sequence. Copy any remaining target sequence, optionally
     * including (intron) gaps.
     */
    while (thisSeqPos < toLength)
    {
      final char c = alignTo.getCharAt(thisSeqPos++);
      if (c != myGapChar || preserveUnmappedGaps)
      {
        thisAligned.append(c);
      }
      sourceGapMappedLength--;
    }

    /*
     * finally add gaps to pad for any trailing source gaps or
     * unmapped characters
     */
    if (preserveUnmappedGaps)
    {
      while (sourceGapMappedLength > 0)
      {
        thisAligned.append(myGapChar);
        sourceGapMappedLength--;
      }
    }

    /*
     * All done aligning, set the aligned sequence.
     */
    alignTo.setSequence(new String(thisAligned));
  }

  /**
   * Helper method to work out how many gaps to insert when realigning.
   * 
   * @param preserveMappedGaps
   * @param preserveUnmappedGaps
   * @param sourceGapMappedLength
   * @param inExon
   * @param trailingCopiedGap
   * @param intronLength
   * @param startOfCodon
   * @return
   */
  protected static int calculateGapsToInsert(boolean preserveMappedGaps,
          boolean preserveUnmappedGaps, int sourceGapMappedLength,
          boolean inExon, int trailingGapLength, int intronLength,
          final boolean startOfCodon)
  {
    int gapsToAdd = 0;
    if (startOfCodon)
    {
      /*
       * Reached start of codon. Ignore trailing gaps in intron unless we are
       * preserving gaps in both exon and intron. Ignore them anyway if the
       * protein alignment introduces a gap at least as large as the intronic
       * region.
       */
      if (inExon && !preserveMappedGaps)
      {
        trailingGapLength = 0;
      }
      if (!inExon && !(preserveMappedGaps && preserveUnmappedGaps))
      {
        trailingGapLength = 0;
      }
      if (inExon)
      {
        gapsToAdd = Math.max(sourceGapMappedLength, trailingGapLength);
      }
      else
      {
        if (intronLength + trailingGapLength <= sourceGapMappedLength)
        {
          gapsToAdd = sourceGapMappedLength - intronLength;
        }
        else
        {
          gapsToAdd = Math.min(
                  intronLength + trailingGapLength - sourceGapMappedLength,
                  trailingGapLength);
        }
      }
    }
    else
    {
      /*
       * second or third base of codon; check for any gaps in dna
       */
      if (!preserveMappedGaps)
      {
        trailingGapLength = 0;
      }
      gapsToAdd = Math.max(sourceGapMappedLength, trailingGapLength);
    }
    return gapsToAdd;
  }

  /**
   * Realigns the given protein to match the alignment of the dna, using codon
   * mappings to translate aligned codon positions to protein residues.
   * 
   * @param protein
   *          the alignment whose sequences are realigned by this method
   * @param dna
   *          the dna alignment whose alignment we are 'copying'
   * @return the number of sequences that were realigned
   */
  public static int alignProteinAsDna(AlignmentI protein, AlignmentI dna)
  {
    if (protein.isNucleotide() || !dna.isNucleotide())
    {
      System.err.println("Wrong alignment type in alignProteinAsDna");
      return 0;
    }
    List<SequenceI> unmappedProtein = new ArrayList<>();
    Map<AlignedCodon, Map<SequenceI, AlignedCodon>> alignedCodons = buildCodonColumnsMap(
            protein, dna, unmappedProtein);
    return alignProteinAs(protein, alignedCodons, unmappedProtein);
  }

  /**
   * Realigns the given dna to match the alignment of the protein, using codon
   * mappings to translate aligned peptide positions to codons.
   * 
   * Always produces a padded CDS alignment.
   * 
   * @param dna
   *          the alignment whose sequences are realigned by this method
   * @param protein
   *          the protein alignment whose alignment we are 'copying'
   * @return the number of sequences that were realigned
   */
  public static int alignCdsAsProtein(AlignmentI dna, AlignmentI protein)
  {
    if (protein.isNucleotide() || !dna.isNucleotide())
    {
      System.err.println("Wrong alignment type in alignProteinAsDna");
      return 0;
    }
    // todo: implement this
    List<AlignedCodonFrame> mappings = protein.getCodonFrames();
    int alignedCount = 0;
    int width = 0; // alignment width for padding CDS
    for (SequenceI dnaSeq : dna.getSequences())
    {
      if (alignCdsSequenceAsProtein(dnaSeq, protein, mappings,
              dna.getGapCharacter()))
      {
        alignedCount++;
      }
      width = Math.max(dnaSeq.getLength(), width);
    }
    int oldwidth;
    int diff;
    for (SequenceI dnaSeq : dna.getSequences())
    {
      oldwidth = dnaSeq.getLength();
      diff = width - oldwidth;
      if (diff > 0)
      {
        dnaSeq.insertCharAt(oldwidth, diff, dna.getGapCharacter());
      }
    }
    return alignedCount;
  }

  /**
   * Helper method to align (if possible) the dna sequence to match the
   * alignment of a mapped protein sequence. This is currently limited to
   * handling coding sequence only.
   * 
   * @param cdsSeq
   * @param protein
   * @param mappings
   * @param gapChar
   * @return
   */
  static boolean alignCdsSequenceAsProtein(SequenceI cdsSeq,
          AlignmentI protein, List<AlignedCodonFrame> mappings,
          char gapChar)
  {
    SequenceI cdsDss = cdsSeq.getDatasetSequence();
    if (cdsDss == null)
    {
      System.err
              .println("alignCdsSequenceAsProtein needs aligned sequence!");
      return false;
    }

    List<AlignedCodonFrame> dnaMappings = MappingUtils
            .findMappingsForSequence(cdsSeq, mappings);
    for (AlignedCodonFrame mapping : dnaMappings)
    {
      SequenceI peptide = mapping.findAlignedSequence(cdsSeq, protein);
      if (peptide != null)
      {
        final int peptideLength = peptide.getLength();
        Mapping map = mapping.getMappingBetween(cdsSeq, peptide);
        if (map != null)
        {
          MapList mapList = map.getMap();
          if (map.getTo() == peptide.getDatasetSequence())
          {
            mapList = mapList.getInverse();
          }
          final int cdsLength = cdsDss.getLength();
          int mappedFromLength = MappingUtils
                  .getLength(mapList.getFromRanges());
          int mappedToLength = MappingUtils
                  .getLength(mapList.getToRanges());
          boolean addStopCodon = (cdsLength == mappedFromLength
                  * CODON_LENGTH + CODON_LENGTH)
                  || (peptide.getDatasetSequence()
                          .getLength() == mappedFromLength - 1);
          if (cdsLength != mappedToLength && !addStopCodon)
          {
            System.err.println(String.format(
                    "Can't align cds as protein (length mismatch %d/%d): %s",
                    cdsLength, mappedToLength, cdsSeq.getName()));
          }

          /*
           * pre-fill the aligned cds sequence with gaps
           */
          char[] alignedCds = new char[peptideLength * CODON_LENGTH
                  + (addStopCodon ? CODON_LENGTH : 0)];
          Arrays.fill(alignedCds, gapChar);

          /*
           * walk over the aligned peptide sequence and insert mapped 
           * codons for residues in the aligned cds sequence 
           */
          int copiedBases = 0;
          int cdsStart = cdsDss.getStart();
          int proteinPos = peptide.getStart() - 1;
          int cdsCol = 0;

          for (int col = 0; col < peptideLength; col++)
          {
            char residue = peptide.getCharAt(col);

            if (Comparison.isGap(residue))
            {
              cdsCol += CODON_LENGTH;
            }
            else
            {
              proteinPos++;
              int[] codon = mapList.locateInTo(proteinPos, proteinPos);
              if (codon == null)
              {
                // e.g. incomplete start codon, X in peptide
                cdsCol += CODON_LENGTH;
              }
              else
              {
                for (int j = codon[0]; j <= codon[1]; j++)
                {
                  char mappedBase = cdsDss.getCharAt(j - cdsStart);
                  alignedCds[cdsCol++] = mappedBase;
                  copiedBases++;
                }
              }
            }
          }

          /*
           * append stop codon if not mapped from protein,
           * closing it up to the end of the mapped sequence
           */
          if (copiedBases == cdsLength - CODON_LENGTH)
          {
            for (int i = alignedCds.length - 1; i >= 0; i--)
            {
              if (!Comparison.isGap(alignedCds[i]))
              {
                cdsCol = i + 1; // gap just after end of sequence
                break;
              }
            }
            for (int i = cdsLength - CODON_LENGTH; i < cdsLength; i++)
            {
              alignedCds[cdsCol++] = cdsDss.getCharAt(i);
            }
          }
          cdsSeq.setSequence(new String(alignedCds));
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Builds a map whose key is an aligned codon position (3 alignment column
   * numbers base 0), and whose value is a map from protein sequence to each
   * protein's peptide residue for that codon. The map generates an ordering of
   * the codons, and allows us to read off the peptides at each position in
   * order to assemble 'aligned' protein sequences.
   * 
   * @param protein
   *          the protein alignment
   * @param dna
   *          the coding dna alignment
   * @param unmappedProtein
   *          any unmapped proteins are added to this list
   * @return
   */
  protected static Map<AlignedCodon, Map<SequenceI, AlignedCodon>> buildCodonColumnsMap(
          AlignmentI protein, AlignmentI dna,
          List<SequenceI> unmappedProtein)
  {
    /*
     * maintain a list of any proteins with no mappings - these will be
     * rendered 'as is' in the protein alignment as we can't align them
     */
    unmappedProtein.addAll(protein.getSequences());

    List<AlignedCodonFrame> mappings = protein.getCodonFrames();

    /*
     * Map will hold, for each aligned codon position e.g. [3, 5, 6], a map of
     * {dnaSequence, {proteinSequence, codonProduct}} at that position. The
     * comparator keeps the codon positions ordered.
     */
    Map<AlignedCodon, Map<SequenceI, AlignedCodon>> alignedCodons = new TreeMap<>(
            new CodonComparator());

    for (SequenceI dnaSeq : dna.getSequences())
    {
      for (AlignedCodonFrame mapping : mappings)
      {
        SequenceI prot = mapping.findAlignedSequence(dnaSeq, protein);
        if (prot != null)
        {
          Mapping seqMap = mapping.getMappingForSequence(dnaSeq);
          addCodonPositions(dnaSeq, prot, protein.getGapCharacter(), seqMap,
                  alignedCodons);
          unmappedProtein.remove(prot);
        }
      }
    }

    /*
     * Finally add any unmapped peptide start residues (e.g. for incomplete
     * codons) as if at the codon position before the second residue
     */
    // TODO resolve JAL-2022 so this fudge can be removed
    int mappedSequenceCount = protein.getHeight() - unmappedProtein.size();
    addUnmappedPeptideStarts(alignedCodons, mappedSequenceCount);

    return alignedCodons;
  }

  /**
   * Scans for any protein mapped from position 2 (meaning unmapped start
   * position e.g. an incomplete codon), and synthesizes a 'codon' for it at the
   * preceding position in the alignment
   * 
   * @param alignedCodons
   *          the codon-to-peptide map
   * @param mappedSequenceCount
   *          the number of distinct sequences in the map
   */
  protected static void addUnmappedPeptideStarts(
          Map<AlignedCodon, Map<SequenceI, AlignedCodon>> alignedCodons,
          int mappedSequenceCount)
  {
    // TODO delete this ugly hack once JAL-2022 is resolved
    // i.e. we can model startPhase > 0 (incomplete start codon)

    List<SequenceI> sequencesChecked = new ArrayList<>();
    AlignedCodon lastCodon = null;
    Map<SequenceI, AlignedCodon> toAdd = new HashMap<>();

    for (Entry<AlignedCodon, Map<SequenceI, AlignedCodon>> entry : alignedCodons
            .entrySet())
    {
      for (Entry<SequenceI, AlignedCodon> sequenceCodon : entry.getValue()
              .entrySet())
      {
        SequenceI seq = sequenceCodon.getKey();
        if (sequencesChecked.contains(seq))
        {
          continue;
        }
        sequencesChecked.add(seq);
        AlignedCodon codon = sequenceCodon.getValue();
        if (codon.peptideCol > 1)
        {
          System.err.println(
                  "Problem mapping protein with >1 unmapped start positions: "
                          + seq.getName());
        }
        else if (codon.peptideCol == 1)
        {
          /*
           * first position (peptideCol == 0) was unmapped - add it
           */
          if (lastCodon != null)
          {
            AlignedCodon firstPeptide = new AlignedCodon(lastCodon.pos1,
                    lastCodon.pos2, lastCodon.pos3,
                    String.valueOf(seq.getCharAt(0)), 0);
            toAdd.put(seq, firstPeptide);
          }
          else
          {
            /*
             * unmapped residue at start of alignment (no prior column) -
             * 'insert' at nominal codon [0, 0, 0]
             */
            AlignedCodon firstPeptide = new AlignedCodon(0, 0, 0,
                    String.valueOf(seq.getCharAt(0)), 0);
            toAdd.put(seq, firstPeptide);
          }
        }
        if (sequencesChecked.size() == mappedSequenceCount)
        {
          // no need to check past first mapped position in all sequences
          break;
        }
      }
      lastCodon = entry.getKey();
    }

    /*
     * add any new codons safely after iterating over the map
     */
    for (Entry<SequenceI, AlignedCodon> startCodon : toAdd.entrySet())
    {
      addCodonToMap(alignedCodons, startCodon.getValue(),
              startCodon.getKey());
    }
  }

  /**
   * Update the aligned protein sequences to match the codon alignments given in
   * the map.
   * 
   * @param protein
   * @param alignedCodons
   *          an ordered map of codon positions (columns), with sequence/peptide
   *          values present in each column
   * @param unmappedProtein
   * @return
   */
  protected static int alignProteinAs(AlignmentI protein,
          Map<AlignedCodon, Map<SequenceI, AlignedCodon>> alignedCodons,
          List<SequenceI> unmappedProtein)
  {
    /*
     * prefill peptide sequences with gaps 
     */
    int alignedWidth = alignedCodons.size();
    char[] gaps = new char[alignedWidth];
    Arrays.fill(gaps, protein.getGapCharacter());
    Map<SequenceI, char[]> peptides = new HashMap<>();
    for (SequenceI seq : protein.getSequences())
    {
      if (!unmappedProtein.contains(seq))
      {
        peptides.put(seq, Arrays.copyOf(gaps, gaps.length));
      }
    }

    /*
     * Traverse the codons left to right (as defined by CodonComparator)
     * and insert peptides in each column where the sequence is mapped.
     * This gives a peptide 'alignment' where residues are aligned if their
     * corresponding codons occupy the same columns in the cdna alignment.
     */
    int column = 0;
    for (AlignedCodon codon : alignedCodons.keySet())
    {
      final Map<SequenceI, AlignedCodon> columnResidues = alignedCodons
              .get(codon);
      for (Entry<SequenceI, AlignedCodon> entry : columnResidues.entrySet())
      {
        char residue = entry.getValue().product.charAt(0);
        peptides.get(entry.getKey())[column] = residue;
      }
      column++;
    }

    /*
     * and finally set the constructed sequences
     */
    for (Entry<SequenceI, char[]> entry : peptides.entrySet())
    {
      entry.getKey().setSequence(new String(entry.getValue()));
    }

    return 0;
  }

  /**
   * Populate the map of aligned codons by traversing the given sequence
   * mapping, locating the aligned positions of mapped codons, and adding those
   * positions and their translation products to the map.
   * 
   * @param dna
   *          the aligned sequence we are mapping from
   * @param protein
   *          the sequence to be aligned to the codons
   * @param gapChar
   *          the gap character in the dna sequence
   * @param seqMap
   *          a mapping to a sequence translation
   * @param alignedCodons
   *          the map we are building up
   */
  static void addCodonPositions(SequenceI dna, SequenceI protein,
          char gapChar, Mapping seqMap,
          Map<AlignedCodon, Map<SequenceI, AlignedCodon>> alignedCodons)
  {
    Iterator<AlignedCodon> codons = seqMap.getCodonIterator(dna, gapChar);

    /*
     * add codon positions, and their peptide translations, to the alignment
     * map, while remembering the first codon mapped
     */
    while (codons.hasNext())
    {
      try
      {
        AlignedCodon codon = codons.next();
        addCodonToMap(alignedCodons, codon, protein);
      } catch (IncompleteCodonException e)
      {
        // possible incomplete trailing codon - ignore
      } catch (NoSuchElementException e)
      {
        // possibly peptide lacking STOP
      }
    }
  }

  /**
   * Helper method to add a codon-to-peptide entry to the aligned codons map
   * 
   * @param alignedCodons
   * @param codon
   * @param protein
   */
  protected static void addCodonToMap(
          Map<AlignedCodon, Map<SequenceI, AlignedCodon>> alignedCodons,
          AlignedCodon codon, SequenceI protein)
  {
    Map<SequenceI, AlignedCodon> seqProduct = alignedCodons.get(codon);
    if (seqProduct == null)
    {
      seqProduct = new HashMap<>();
      alignedCodons.put(codon, seqProduct);
    }
    seqProduct.put(protein, codon);
  }

  /**
   * Returns true if a cDNA/Protein mapping either exists, or could be made,
   * between at least one pair of sequences in the two alignments. Currently,
   * the logic is:
   * <ul>
   * <li>One alignment must be nucleotide, and the other protein</li>
   * <li>At least one pair of sequences must be already mapped, or mappable</li>
   * <li>Mappable means the nucleotide translation matches the protein
   * sequence</li>
   * <li>The translation may ignore start and stop codons if present in the
   * nucleotide</li>
   * </ul>
   * 
   * @param al1
   * @param al2
   * @return
   */
  public static boolean isMappable(AlignmentI al1, AlignmentI al2)
  {
    if (al1 == null || al2 == null)
    {
      return false;
    }

    /*
     * Require one nucleotide and one protein
     */
    if (al1.isNucleotide() == al2.isNucleotide())
    {
      return false;
    }
    AlignmentI dna = al1.isNucleotide() ? al1 : al2;
    AlignmentI protein = dna == al1 ? al2 : al1;
    List<AlignedCodonFrame> mappings = protein.getCodonFrames();
    for (SequenceI dnaSeq : dna.getSequences())
    {
      for (SequenceI proteinSeq : protein.getSequences())
      {
        if (isMappable(dnaSeq, proteinSeq, mappings))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the dna sequence is mapped, or could be mapped, to the
   * protein sequence.
   * 
   * @param dnaSeq
   * @param proteinSeq
   * @param mappings
   * @return
   */
  protected static boolean isMappable(SequenceI dnaSeq,
          SequenceI proteinSeq, List<AlignedCodonFrame> mappings)
  {
    if (dnaSeq == null || proteinSeq == null)
    {
      return false;
    }

    SequenceI dnaDs = dnaSeq.getDatasetSequence() == null ? dnaSeq
            : dnaSeq.getDatasetSequence();
    SequenceI proteinDs = proteinSeq.getDatasetSequence() == null
            ? proteinSeq
            : proteinSeq.getDatasetSequence();

    for (AlignedCodonFrame mapping : mappings)
    {
      if (proteinDs == mapping.getAaForDnaSeq(dnaDs))
      {
        /*
         * already mapped
         */
        return true;
      }
    }

    /*
     * Just try to make a mapping (it is not yet stored), test whether
     * successful.
     */
    return mapCdnaToProtein(proteinDs, dnaDs) != null;
  }

  /**
   * Finds any reference annotations associated with the sequences in
   * sequenceScope, that are not already added to the alignment, and adds them
   * to the 'candidates' map. Also populates a lookup table of annotation
   * labels, keyed by calcId, for use in constructing tooltips or the like.
   * 
   * @param sequenceScope
   *          the sequences to scan for reference annotations
   * @param labelForCalcId
   *          (optional) map to populate with label for calcId
   * @param candidates
   *          map to populate with annotations for sequence
   * @param al
   *          the alignment to check for presence of annotations
   */
  public static void findAddableReferenceAnnotations(
          List<SequenceI> sequenceScope, Map<String, String> labelForCalcId,
          final Map<SequenceI, List<AlignmentAnnotation>> candidates,
          AlignmentI al)
  {
    if (sequenceScope == null)
    {
      return;
    }

    /*
     * For each sequence in scope, make a list of any annotations on the
     * underlying dataset sequence which are not already on the alignment.
     * 
     * Add to a map of { alignmentSequence, <List of annotations to add> }
     */
    for (SequenceI seq : sequenceScope)
    {
      SequenceI dataset = seq.getDatasetSequence();
      if (dataset == null)
      {
        continue;
      }
      AlignmentAnnotation[] datasetAnnotations = dataset.getAnnotation();
      if (datasetAnnotations == null)
      {
        continue;
      }
      final List<AlignmentAnnotation> result = new ArrayList<>();
      for (AlignmentAnnotation dsann : datasetAnnotations)
      {
        /*
         * Find matching annotations on the alignment. If none is found, then
         * add this annotation to the list of 'addable' annotations for this
         * sequence.
         */
        final Iterable<AlignmentAnnotation> matchedAlignmentAnnotations = al
                .findAnnotations(seq, dsann.getCalcId(), dsann.label);
        boolean found = false;
        if (matchedAlignmentAnnotations != null)
        {
          for (AlignmentAnnotation matched : matchedAlignmentAnnotations)
          {
            if (dsann.description.equals(matched.description))
            {
              found = true;
              break;
            }
          }
        }
        if (!found)
        {
          result.add(dsann);
          if (labelForCalcId != null)
          {
            labelForCalcId.put(dsann.getCalcId(), dsann.label);
          }
        }
      }
      /*
       * Save any addable annotations for this sequence
       */
      if (!result.isEmpty())
      {
        candidates.put(seq, result);
      }
    }
  }

  /**
   * Adds annotations to the top of the alignment annotations, in the same order
   * as their related sequences. If you already have an annotation and want to
   * add it to a sequence in an alignment use {@code addReferenceAnnotationTo}
   * 
   * @param annotations
   *          the annotations to add
   * @param alignment
   *          the alignment to add them to
   * @param selectionGroup
   *          current selection group - may be null, if provided then any added annotation will be trimmed to just those columns in the selection group
   */
  public static void addReferenceAnnotations(
          Map<SequenceI, List<AlignmentAnnotation>> annotations,
          final AlignmentI alignment, final SequenceGroup selectionGroup)
  {
    for (SequenceI seq : annotations.keySet())
    {
      for (AlignmentAnnotation ann : annotations.get(seq))
      {
        addReferenceAnnotationTo(alignment, seq, ann, selectionGroup);
      }
    }
  }

  /**
   * Make a copy of a reference annotation {@code ann} and add it to an
   * alignment sequence {@code seq} in {@code alignment}, optionally limited to
   * the extent of {@code selectionGroup}
   * 
   * @param alignment
   * @param seq
   * @param ann
   * @param selectionGroup
   *          current selection group - may be null, if provided then any added annotation will be trimmed to just those columns in the selection group
   * @return annotation added to {@code seq and {@code alignment}
   */
  public static AlignmentAnnotation addReferenceAnnotationTo(
          final AlignmentI alignment, final SequenceI seq,
          final AlignmentAnnotation ann, final SequenceGroup selectionGroup)
  {
    AlignmentAnnotation copyAnn = new AlignmentAnnotation(ann);
    int startRes = 0;
    int endRes = ann.annotations.length;
    if (selectionGroup != null)
    {
      startRes = -1 + Math.min(seq.getEnd(), Math.max(seq.getStart(),
              seq.findPosition(selectionGroup.getStartRes())));
      endRes = -1 + Math.min(seq.getEnd(),
              seq.findPosition(selectionGroup.getEndRes()));

    }
    copyAnn.restrict(startRes, endRes + 0);

    /*
     * Add to the sequence (sets copyAnn.datasetSequence), unless the
     * original annotation is already on the sequence.
     */
    if (!seq.hasAnnotation(ann))
    {
      ContactMatrixI cm = seq.getDatasetSequence().getContactMatrixFor(ann);
      if (cm != null)
      {
        seq.addContactListFor(copyAnn, cm);
      }
      seq.addAlignmentAnnotation(copyAnn);
    }
    // adjust for gaps
    copyAnn.adjustForAlignment();
    // add to the alignment and set visible
    alignment.addAnnotation(copyAnn);
    copyAnn.visible = true;

    return copyAnn;
  }

  /**
   * Set visibility of alignment annotations of specified types (labels), for
   * specified sequences. This supports controls like "Show all secondary
   * structure", "Hide all Temp factor", etc.
   * 
   * @al the alignment to scan for annotations
   * @param types
   *          the types (labels) of annotations to be updated
   * @param forSequences
   *          if not null, only annotations linked to one of these sequences are
   *          in scope for update; if null, acts on all sequence annotations
   * @param anyType
   *          if this flag is true, 'types' is ignored (label not checked)
   * @param doShow
   *          if true, set visibility on, else set off
   */
  public static void showOrHideSequenceAnnotations(AlignmentI al,
          Collection<String> types, List<SequenceI> forSequences,
          boolean anyType, boolean doShow)
  {
    AlignmentAnnotation[] anns = al.getAlignmentAnnotation();
    if (anns != null)
    {
      for (AlignmentAnnotation aa : anns)
      {
        if (anyType || types.contains(aa.label))
        {
          if ((aa.sequenceRef != null) && (forSequences == null
                  || forSequences.contains(aa.sequenceRef)))
          {
            aa.visible = doShow;
          }
        }
      }
    }
  }

  public static AlignmentAnnotation getFirstSequenceAnnotationOfType(
          AlignmentI al, int graphType)
  {
    AlignmentAnnotation[] anns = al.getAlignmentAnnotation();
    if (anns != null)
    {
      for (AlignmentAnnotation aa : anns)
      {
        if (aa.sequenceRef != null && aa.graph == graphType)
          return aa;
      }
    }
    return null;
  }

  /**
   * Returns true if either sequence has a cross-reference to the other
   * 
   * @param seq1
   * @param seq2
   * @return
   */
  public static boolean haveCrossRef(SequenceI seq1, SequenceI seq2)
  {
    // Note: moved here from class CrossRef as the latter class has dependencies
    // not availability to the applet's classpath
    return hasCrossRef(seq1, seq2) || hasCrossRef(seq2, seq1);
  }

  /**
   * Returns true if seq1 has a cross-reference to seq2. Currently this assumes
   * that sequence name is structured as Source|AccessionId.
   * 
   * @param seq1
   * @param seq2
   * @return
   */
  public static boolean hasCrossRef(SequenceI seq1, SequenceI seq2)
  {
    if (seq1 == null || seq2 == null)
    {
      return false;
    }
    String name = seq2.getName();
    final List<DBRefEntry> xrefs = seq1.getDBRefs();
    if (xrefs != null)
    {
      for (int ix = 0, nx = xrefs.size(); ix < nx; ix++)
      {
        DBRefEntry xref = xrefs.get(ix);
        String xrefName = xref.getSource() + "|" + xref.getAccessionId();
        // case-insensitive test, consistent with DBRefEntry.equalRef()
        if (xrefName.equalsIgnoreCase(name))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Constructs an alignment consisting of the mapped (CDS) regions in the given
   * nucleotide sequences, and updates mappings to match. The CDS sequences are
   * added to the original alignment's dataset, which is shared by the new
   * alignment. Mappings from nucleotide to CDS, and from CDS to protein, are
   * added to the alignment dataset.
   * 
   * @param dna
   *          aligned nucleotide (dna or cds) sequences
   * @param dataset
   *          the alignment dataset the sequences belong to
   * @param products
   *          (optional) to restrict results to CDS that map to specified
   *          protein products
   * @return an alignment whose sequences are the cds-only parts of the dna
   *         sequences (or null if no mappings are found)
   */
  public static AlignmentI makeCdsAlignment(SequenceI[] dna,
          AlignmentI dataset, SequenceI[] products)
  {
    if (dataset == null || dataset.getDataset() != null)
    {
      throw new IllegalArgumentException(
              "IMPLEMENTATION ERROR: dataset.getDataset() must be null!");
    }
    List<SequenceI> foundSeqs = new ArrayList<>();
    List<SequenceI> cdsSeqs = new ArrayList<>();
    List<AlignedCodonFrame> mappings = dataset.getCodonFrames();
    HashSet<SequenceI> productSeqs = null;
    if (products != null)
    {
      productSeqs = new HashSet<>();
      for (SequenceI seq : products)
      {
        productSeqs.add(seq.getDatasetSequence() == null ? seq
                : seq.getDatasetSequence());
      }
    }

    /*
     * Construct CDS sequences from mappings on the alignment dataset.
     * The logic is:
     * - find the protein product(s) mapped to from each dna sequence
     * - if the mapping covers the whole dna sequence (give or take start/stop
     *   codon), take the dna as the CDS sequence
     * - else search dataset mappings for a suitable dna sequence, i.e. one
     *   whose whole sequence is mapped to the protein 
     * - if no sequence found, construct one from the dna sequence and mapping
     *   (and add it to dataset so it is found if this is repeated)
     */
    for (SequenceI dnaSeq : dna)
    {
      SequenceI dnaDss = dnaSeq.getDatasetSequence() == null ? dnaSeq
              : dnaSeq.getDatasetSequence();

      List<AlignedCodonFrame> seqMappings = MappingUtils
              .findMappingsForSequence(dnaSeq, mappings);
      for (AlignedCodonFrame mapping : seqMappings)
      {
        List<Mapping> mappingsFromSequence = mapping
                .getMappingsFromSequence(dnaSeq);

        for (Mapping aMapping : mappingsFromSequence)
        {
          MapList mapList = aMapping.getMap();
          if (mapList.getFromRatio() == 1)
          {
            /*
             * not a dna-to-protein mapping (likely dna-to-cds)
             */
            continue;
          }

          /*
           * skip if mapping is not to one of the target set of proteins
           */
          SequenceI proteinProduct = aMapping.getTo();
          if (productSeqs != null && !productSeqs.contains(proteinProduct))
          {
            continue;
          }

          /*
           * try to locate the CDS from the dataset mappings;
           * guard against duplicate results (for the case that protein has
           * dbrefs to both dna and cds sequences)
           */
          SequenceI cdsSeq = findCdsForProtein(mappings, dnaSeq,
                  seqMappings, aMapping);
          if (cdsSeq != null)
          {
            if (!foundSeqs.contains(cdsSeq))
            {
              foundSeqs.add(cdsSeq);
              SequenceI derivedSequence = cdsSeq.deriveSequence();
              cdsSeqs.add(derivedSequence);
              if (!dataset.getSequences().contains(cdsSeq))
              {
                dataset.addSequence(cdsSeq);
              }
            }
            continue;
          }

          /*
           * didn't find mapped CDS sequence - construct it and add
           * its dataset sequence to the dataset
           */
          cdsSeq = makeCdsSequence(dnaSeq.getDatasetSequence(), aMapping,
                  dataset).deriveSequence();
          // cdsSeq has a name constructed as CDS|<dbref>
          // <dbref> will be either the accession for the coding sequence,
          // marked in the /via/ dbref to the protein product accession
          // or it will be the original nucleotide accession.
          SequenceI cdsSeqDss = cdsSeq.getDatasetSequence();

          cdsSeqs.add(cdsSeq);

          /*
           * build the mapping from CDS to protein
           */
          List<int[]> cdsRange = Collections
                  .singletonList(new int[]
                  { cdsSeq.getStart(),
                      cdsSeq.getLength() + cdsSeq.getStart() - 1 });
          MapList cdsToProteinMap = new MapList(cdsRange,
                  mapList.getToRanges(), mapList.getFromRatio(),
                  mapList.getToRatio());

          if (!dataset.getSequences().contains(cdsSeqDss))
          {
            /*
             * if this sequence is a newly created one, add it to the dataset
             * and made a CDS to protein mapping (if sequence already exists,
             * CDS-to-protein mapping _is_ the transcript-to-protein mapping)
             */
            dataset.addSequence(cdsSeqDss);
            AlignedCodonFrame cdsToProteinMapping = new AlignedCodonFrame();
            cdsToProteinMapping.addMap(cdsSeqDss, proteinProduct,
                    cdsToProteinMap);

            /*
             * guard against duplicating the mapping if repeating this action
             */
            if (!mappings.contains(cdsToProteinMapping))
            {
              mappings.add(cdsToProteinMapping);
            }
          }

          propagateDBRefsToCDS(cdsSeqDss, dnaSeq.getDatasetSequence(),
                  proteinProduct, aMapping);
          /*
           * add another mapping from original 'from' range to CDS
           */
          AlignedCodonFrame dnaToCdsMapping = new AlignedCodonFrame();
          final MapList dnaToCdsMap = new MapList(mapList.getFromRanges(),
                  cdsRange, 1, 1);
          dnaToCdsMapping.addMap(dnaSeq.getDatasetSequence(), cdsSeqDss,
                  dnaToCdsMap);
          if (!mappings.contains(dnaToCdsMapping))
          {
            mappings.add(dnaToCdsMapping);
          }

          /*
           * transfer dna chromosomal loci (if known) to the CDS
           * sequence (via the mapping)
           */
          final MapList cdsToDnaMap = dnaToCdsMap.getInverse();
          transferGeneLoci(dnaSeq, cdsToDnaMap, cdsSeq);

          /*
           * add DBRef with mapping from protein to CDS
           * (this enables Get Cross-References from protein alignment)
           * This is tricky because we can't have two DBRefs with the
           * same source and accession, so need a different accession for
           * the CDS from the dna sequence
           */

          // specific use case:
          // Genomic contig ENSCHR:1, contains coding regions for ENSG01,
          // ENSG02, ENSG03, with transcripts and products similarly named.
          // cannot add distinct dbrefs mapping location on ENSCHR:1 to ENSG01

          // JBPNote: ?? can't actually create an example that demonstrates we
          // need to
          // synthesize an xref.

          List<DBRefEntry> primrefs = dnaDss.getPrimaryDBRefs();
          for (int ip = 0, np = primrefs.size(); ip < np; ip++)
          {
            DBRefEntry primRef = primrefs.get(ip);
            /*
             * create a cross-reference from CDS to the source sequence's
             * primary reference and vice versa
             */
            String source = primRef.getSource();
            String version = primRef.getVersion();
            DBRefEntry cdsCrossRef = new DBRefEntry(source,
                    source + ":" + version, primRef.getAccessionId());
            cdsCrossRef
                    .setMap(new Mapping(dnaDss, new MapList(cdsToDnaMap)));
            cdsSeqDss.addDBRef(cdsCrossRef);

            dnaSeq.addDBRef(new DBRefEntry(source, version,
                    cdsSeq.getName(), new Mapping(cdsSeqDss, dnaToCdsMap)));
            // problem here is that the cross-reference is synthesized -
            // cdsSeq.getName() may be like 'CDS|dnaaccession' or
            // 'CDS|emblcdsacc'
            // assuming cds version same as dna ?!?

            DBRefEntry proteinToCdsRef = new DBRefEntry(source, version,
                    cdsSeq.getName());
            //
            proteinToCdsRef.setMap(
                    new Mapping(cdsSeqDss, cdsToProteinMap.getInverse()));
            proteinProduct.addDBRef(proteinToCdsRef);
          }
          /*
           * transfer any features on dna that overlap the CDS
           */
          transferFeatures(dnaSeq, cdsSeq, dnaToCdsMap, null,
                  SequenceOntologyI.CDS);
        }
      }
    }

    AlignmentI cds = new Alignment(
            cdsSeqs.toArray(new SequenceI[cdsSeqs.size()]));
    cds.setDataset(dataset);

    return cds;
  }

  /**
   * Tries to transfer gene loci (dbref to chromosome positions) from fromSeq to
   * toSeq, mediated by the given mapping between the sequences
   * 
   * @param fromSeq
   * @param targetToFrom
   *          Map
   * @param targetSeq
   */
  protected static void transferGeneLoci(SequenceI fromSeq,
          MapList targetToFrom, SequenceI targetSeq)
  {
    if (targetSeq.getGeneLoci() != null)
    {
      // already have - don't override
      return;
    }
    GeneLociI fromLoci = fromSeq.getGeneLoci();
    if (fromLoci == null)
    {
      return;
    }

    MapList newMap = targetToFrom.traverse(fromLoci.getMapping());

    if (newMap != null)
    {
      targetSeq.setGeneLoci(fromLoci.getSpeciesId(),
              fromLoci.getAssemblyId(), fromLoci.getChromosomeId(), newMap);
    }
  }

  /**
   * A helper method that finds a CDS sequence in the alignment dataset that is
   * mapped to the given protein sequence, and either is, or has a mapping from,
   * the given dna sequence.
   * 
   * @param mappings
   *          set of all mappings on the dataset
   * @param dnaSeq
   *          a dna (or cds) sequence we are searching from
   * @param seqMappings
   *          the set of mappings involving dnaSeq
   * @param aMapping
   *          a transcript-to-peptide mapping
   * @return
   */
  static SequenceI findCdsForProtein(List<AlignedCodonFrame> mappings,
          SequenceI dnaSeq, List<AlignedCodonFrame> seqMappings,
          Mapping aMapping)
  {
    /*
     * TODO a better dna-cds-protein mapping data representation to allow easy
     * navigation; until then this clunky looping around lists of mappings
     */
    SequenceI seqDss = dnaSeq.getDatasetSequence() == null ? dnaSeq
            : dnaSeq.getDatasetSequence();
    SequenceI proteinProduct = aMapping.getTo();

    /*
     * is this mapping from the whole dna sequence (i.e. CDS)?
     * allowing for possible stop codon on dna but not peptide
     */
    int mappedFromLength = MappingUtils
            .getLength(aMapping.getMap().getFromRanges());
    int dnaLength = seqDss.getLength();
    if (mappedFromLength == dnaLength
            || mappedFromLength == dnaLength - CODON_LENGTH)
    {
      /*
       * if sequence has CDS features, this is a transcript with no UTR
       * - do not take this as the CDS sequence! (JAL-2789)
       */
      if (seqDss.getFeatures().getFeaturesByOntology(SequenceOntologyI.CDS)
              .isEmpty())
      {
        return seqDss;
      }
    }

    /*
     * looks like we found the dna-to-protein mapping; search for the
     * corresponding cds-to-protein mapping
     */
    List<AlignedCodonFrame> mappingsToPeptide = MappingUtils
            .findMappingsForSequence(proteinProduct, mappings);
    for (AlignedCodonFrame acf : mappingsToPeptide)
    {
      for (SequenceToSequenceMapping map : acf.getMappings())
      {
        Mapping mapping = map.getMapping();
        if (mapping != aMapping
                && mapping.getMap().getFromRatio() == CODON_LENGTH
                && proteinProduct == mapping.getTo()
                && seqDss != map.getFromSeq())
        {
          mappedFromLength = MappingUtils
                  .getLength(mapping.getMap().getFromRanges());
          if (mappedFromLength == map.getFromSeq().getLength())
          {
            /*
            * found a 3:1 mapping to the protein product which covers
            * the whole dna sequence i.e. is from CDS; finally check the CDS
            * is mapped from the given dna start sequence
            */
            SequenceI cdsSeq = map.getFromSeq();
            // todo this test is weak if seqMappings contains multiple mappings;
            // we get away with it if transcript:cds relationship is 1:1
            List<AlignedCodonFrame> dnaToCdsMaps = MappingUtils
                    .findMappingsForSequence(cdsSeq, seqMappings);
            if (!dnaToCdsMaps.isEmpty())
            {
              return cdsSeq;
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Helper method that makes a CDS sequence as defined by the mappings from the
   * given sequence i.e. extracts the 'mapped from' ranges (which may be on
   * forward or reverse strand).
   * 
   * @param seq
   * @param mapping
   * @param dataset
   *          - existing dataset. We check for sequences that look like the CDS
   *          we are about to construct, if one exists already, then we will
   *          just return that one.
   * @return CDS sequence (as a dataset sequence)
   */
  static SequenceI makeCdsSequence(SequenceI seq, Mapping mapping,
          AlignmentI dataset)
  {
    /*
     * construct CDS sequence name as "CDS|" with 'from id' held in the mapping
     * if set (e.g. EMBL protein_id), else sequence name appended
     */
    String mapFromId = mapping.getMappedFromId();
    final String seqId = "CDS|"
            + (mapFromId != null ? mapFromId : seq.getName());

    SequenceI newSeq = null;

    /*
     * construct CDS sequence by splicing mapped from ranges
     */
    char[] seqChars = seq.getSequence();
    List<int[]> fromRanges = mapping.getMap().getFromRanges();
    int cdsWidth = MappingUtils.getLength(fromRanges);
    char[] newSeqChars = new char[cdsWidth];

    int newPos = 0;
    for (int[] range : fromRanges)
    {
      if (range[0] <= range[1])
      {
        // forward strand mapping - just copy the range
        int length = range[1] - range[0] + 1;
        System.arraycopy(seqChars, range[0] - 1, newSeqChars, newPos,
                length);
        newPos += length;
      }
      else
      {
        // reverse strand mapping - copy and complement one by one
        for (int i = range[0]; i >= range[1]; i--)
        {
          newSeqChars[newPos++] = Dna.getComplement(seqChars[i - 1]);
        }
      }

      newSeq = new Sequence(seqId, newSeqChars, 1, newPos);
    }

    if (dataset != null)
    {
      SequenceI[] matches = dataset.findSequenceMatch(newSeq.getName());
      if (matches != null)
      {
        boolean matched = false;
        for (SequenceI mtch : matches)
        {
          if (mtch.getStart() != newSeq.getStart())
          {
            continue;
          }
          if (mtch.getEnd() != newSeq.getEnd())
          {
            continue;
          }
          if (!Arrays.equals(mtch.getSequence(), newSeq.getSequence()))
          {
            continue;
          }
          if (!matched)
          {
            matched = true;
            newSeq = mtch;
          }
          else
          {
            Console.error(
                    "JAL-2154 regression: warning - found (and ignored) a duplicate CDS sequence:"
                            + mtch.toString());
          }
        }
      }
    }
    // newSeq.setDescription(mapFromId);

    return newSeq;
  }

  /**
   * Adds any DBRefEntrys to cdsSeq from contig that have a Mapping congruent to
   * the given mapping.
   * 
   * @param cdsSeq
   * @param contig
   * @param proteinProduct
   * @param mapping
   * @return list of DBRefEntrys added
   */
  protected static List<DBRefEntry> propagateDBRefsToCDS(SequenceI cdsSeq,
          SequenceI contig, SequenceI proteinProduct, Mapping mapping)
  {

    // gather direct refs from contig congruent with mapping
    List<DBRefEntry> direct = new ArrayList<>();
    HashSet<String> directSources = new HashSet<>();

    List<DBRefEntry> refs = contig.getDBRefs();
    if (refs != null)
    {
      for (int ib = 0, nb = refs.size(); ib < nb; ib++)
      {
        DBRefEntry dbr = refs.get(ib);
        MapList map;
        if (dbr.hasMap() && (map = dbr.getMap().getMap()).isTripletMap())
        {
          // check if map is the CDS mapping
          if (mapping.getMap().equals(map))
          {
            direct.add(dbr);
            directSources.add(dbr.getSource());
          }
        }
      }
    }
    List<DBRefEntry> onSource = DBRefUtils.selectRefs(
            proteinProduct.getDBRefs(),
            directSources.toArray(new String[0]));
    List<DBRefEntry> propagated = new ArrayList<>();

    // and generate appropriate mappings
    for (int ic = 0, nc = direct.size(); ic < nc; ic++)
    {
      DBRefEntry cdsref = direct.get(ic);
      Mapping m = cdsref.getMap();
      // clone maplist and mapping
      MapList cdsposmap = new MapList(
              Arrays.asList(new int[][]
              { new int[] { cdsSeq.getStart(), cdsSeq.getEnd() } }),
              m.getMap().getToRanges(), 3, 1);
      Mapping cdsmap = new Mapping(m.getTo(), m.getMap());

      // create dbref
      DBRefEntry newref = new DBRefEntry(cdsref.getSource(),
              cdsref.getVersion(), cdsref.getAccessionId(),
              new Mapping(cdsmap.getTo(), cdsposmap));

      // and see if we can map to the protein product for this mapping.
      // onSource is the filtered set of accessions on protein that we are
      // tranferring, so we assume accession is the same.
      if (cdsmap.getTo() == null && onSource != null)
      {
        List<DBRefEntry> sourceRefs = DBRefUtils.searchRefs(onSource,
                cdsref.getAccessionId());
        if (sourceRefs != null)
        {
          for (DBRefEntry srcref : sourceRefs)
          {
            if (srcref.getSource().equalsIgnoreCase(cdsref.getSource()))
            {
              // we have found a complementary dbref on the protein product, so
              // update mapping's getTo
              newref.getMap().setTo(proteinProduct);
            }
          }
        }
      }
      cdsSeq.addDBRef(newref);
      propagated.add(newref);
    }
    return propagated;
  }

  /**
   * Transfers co-located features on 'fromSeq' to 'toSeq', adjusting the
   * feature start/end ranges, optionally omitting specified feature types.
   * Returns the number of features copied.
   * 
   * @param fromSeq
   * @param toSeq
   * @param mapping
   *          the mapping from 'fromSeq' to 'toSeq'
   * @param select
   *          if not null, only features of this type are copied (including
   *          subtypes in the Sequence Ontology)
   * @param omitting
   */
  protected static int transferFeatures(SequenceI fromSeq, SequenceI toSeq,
          MapList mapping, String select, String... omitting)
  {
    SequenceI copyTo = toSeq;
    while (copyTo.getDatasetSequence() != null)
    {
      copyTo = copyTo.getDatasetSequence();
    }
    if (fromSeq == copyTo || fromSeq.getDatasetSequence() == copyTo)
    {
      return 0; // shared dataset sequence
    }

    /*
     * get features, optionally restricted by an ontology term
     */
    List<SequenceFeature> sfs = select == null
            ? fromSeq.getFeatures().getPositionalFeatures()
            : fromSeq.getFeatures().getFeaturesByOntology(select);

    int count = 0;
    for (SequenceFeature sf : sfs)
    {
      String type = sf.getType();
      boolean omit = false;
      for (String toOmit : omitting)
      {
        if (type.equals(toOmit))
        {
          omit = true;
        }
      }
      if (omit)
      {
        continue;
      }

      /*
       * locate the mapped range - null if either start or end is
       * not mapped (no partial overlaps are calculated)
       */
      int start = sf.getBegin();
      int end = sf.getEnd();
      int[] mappedTo = mapping.locateInTo(start, end);
      /*
       * if whole exon range doesn't map, try interpreting it
       * as 5' or 3' exon overlapping the CDS range
       */
      if (mappedTo == null)
      {
        mappedTo = mapping.locateInTo(end, end);
        if (mappedTo != null)
        {
          /*
           * end of exon is in CDS range - 5' overlap
           * to a range from the start of the peptide
           */
          mappedTo[0] = 1;
        }
      }
      if (mappedTo == null)
      {
        mappedTo = mapping.locateInTo(start, start);
        if (mappedTo != null)
        {
          /*
           * start of exon is in CDS range - 3' overlap
           * to a range up to the end of the peptide
           */
          mappedTo[1] = toSeq.getLength();
        }
      }
      if (mappedTo != null)
      {
        int newBegin = Math.min(mappedTo[0], mappedTo[1]);
        int newEnd = Math.max(mappedTo[0], mappedTo[1]);
        SequenceFeature copy = new SequenceFeature(sf, newBegin, newEnd,
                sf.getFeatureGroup(), sf.getScore());
        copyTo.addSequenceFeature(copy);
        count++;
      }
    }
    return count;
  }

  /**
   * Returns a mapping from dna to protein by inspecting sequence features of
   * type "CDS" on the dna. A mapping is constructed if the total CDS feature
   * length is 3 times the peptide length (optionally after dropping a trailing
   * stop codon). This method does not check whether the CDS nucleotide sequence
   * translates to the peptide sequence.
   * 
   * @param dnaSeq
   * @param proteinSeq
   * @return
   */
  public static MapList mapCdsToProtein(SequenceI dnaSeq,
          SequenceI proteinSeq)
  {
    List<int[]> ranges = findCdsPositions(dnaSeq);
    int mappedDnaLength = MappingUtils.getLength(ranges);

    /*
     * if not a whole number of codons, truncate mapping
     */
    int codonRemainder = mappedDnaLength % CODON_LENGTH;
    if (codonRemainder > 0)
    {
      mappedDnaLength -= codonRemainder;
      MappingUtils.removeEndPositions(codonRemainder, ranges);
    }

    int proteinLength = proteinSeq.getLength();
    int proteinStart = proteinSeq.getStart();
    int proteinEnd = proteinSeq.getEnd();

    /*
     * incomplete start codon may mean X at start of peptide
     * we ignore both for mapping purposes
     */
    if (proteinSeq.getCharAt(0) == 'X')
    {
      // todo JAL-2022 support startPhase > 0
      proteinStart++;
      proteinLength--;
    }
    List<int[]> proteinRange = new ArrayList<>();

    /*
     * dna length should map to protein (or protein plus stop codon)
     */
    int codesForResidues = mappedDnaLength / CODON_LENGTH;
    if (codesForResidues == (proteinLength + 1))
    {
      // assuming extra codon is for STOP and not in peptide
      // todo: check trailing codon is indeed a STOP codon
      codesForResidues--;
      mappedDnaLength -= CODON_LENGTH;
      MappingUtils.removeEndPositions(CODON_LENGTH, ranges);
    }

    if (codesForResidues == proteinLength)
    {
      proteinRange.add(new int[] { proteinStart, proteinEnd });
      return new MapList(ranges, proteinRange, CODON_LENGTH, 1);
    }
    return null;
  }

  /**
   * Returns a list of CDS ranges found (as sequence positions base 1), i.e. of
   * [start, end] positions of sequence features of type "CDS" (or a sub-type of
   * CDS in the Sequence Ontology). The ranges are sorted into ascending start
   * position order, so this method is only valid for linear CDS in the same
   * sense as the protein product.
   * 
   * @param dnaSeq
   * @return
   */
  protected static List<int[]> findCdsPositions(SequenceI dnaSeq)
  {
    List<int[]> result = new ArrayList<>();

    List<SequenceFeature> sfs = dnaSeq.getFeatures()
            .getFeaturesByOntology(SequenceOntologyI.CDS);
    if (sfs.isEmpty())
    {
      return result;
    }
    SequenceFeatures.sortFeatures(sfs, true);

    for (SequenceFeature sf : sfs)
    {
      int phase = 0;
      try
      {
        String s = sf.getPhase();
        if (s != null)
        {
          phase = Integer.parseInt(s);
        }
      } catch (NumberFormatException e)
      {
        // leave as zero
      }
      /*
       * phase > 0 on first codon means 5' incomplete - skip to the start
       * of the next codon; example ENST00000496384
       */
      int begin = sf.getBegin();
      int end = sf.getEnd();
      if (result.isEmpty() && phase > 0)
      {
        begin += phase;
        if (begin > end)
        {
          // shouldn't happen!
          System.err
                  .println("Error: start phase extends beyond start CDS in "
                          + dnaSeq.getName());
        }
      }
      result.add(new int[] { begin, end });
    }

    /*
     * Finally sort ranges by start position. This avoids a dependency on 
     * keeping features in order on the sequence (if they are in order anyway,
     * the sort will have almost no work to do). The implicit assumption is CDS
     * ranges are assembled in order. Other cases should not use this method,
     * but instead construct an explicit mapping for CDS (e.g. EMBL parsing).
     */
    Collections.sort(result, IntRangeComparator.ASCENDING);
    return result;
  }

  /**
   * Makes an alignment with a copy of the given sequences, adding in any
   * non-redundant sequences which are mapped to by the cross-referenced
   * sequences.
   * 
   * @param seqs
   * @param xrefs
   * @param dataset
   *          the alignment dataset shared by the new copy
   * @return
   */
  public static AlignmentI makeCopyAlignment(SequenceI[] seqs,
          SequenceI[] xrefs, AlignmentI dataset)
  {
    AlignmentI copy = new Alignment(new Alignment(seqs));
    copy.setDataset(dataset);
    boolean isProtein = !copy.isNucleotide();
    SequenceIdMatcher matcher = new SequenceIdMatcher(seqs);
    if (xrefs != null)
    {
      // BH 2019.01.25 recoded to remove iterators

      for (int ix = 0, nx = xrefs.length; ix < nx; ix++)
      {
        SequenceI xref = xrefs[ix];
        List<DBRefEntry> dbrefs = xref.getDBRefs();
        if (dbrefs != null)
        {
          for (int ir = 0, nir = dbrefs.size(); ir < nir; ir++)
          {
            DBRefEntry dbref = dbrefs.get(ir);
            Mapping map = dbref.getMap();
            SequenceI mto;
            if (map == null || (mto = map.getTo()) == null
                    || mto.isProtein() != isProtein)
            {
              continue;
            }
            SequenceI mappedTo = mto;
            SequenceI match = matcher.findIdMatch(mappedTo);
            if (match == null)
            {
              matcher.add(mappedTo);
              copy.addSequence(mappedTo);
            }
          }
        }
      }
    }
    return copy;
  }

  /**
   * Try to align sequences in 'unaligned' to match the alignment of their
   * mapped regions in 'aligned'. For example, could use this to align CDS
   * sequences which are mapped to their parent cDNA sequences.
   * 
   * This method handles 1:1 mappings (dna-to-dna or protein-to-protein). For
   * dna-to-protein or protein-to-dna use alternative methods.
   * 
   * @param unaligned
   *          sequences to be aligned
   * @param aligned
   *          holds aligned sequences and their mappings
   * @return
   */
  public static int alignAs(AlignmentI unaligned, AlignmentI aligned)
  {
    /*
     * easy case - aligning a copy of aligned sequences
     */
    if (alignAsSameSequences(unaligned, aligned))
    {
      return unaligned.getHeight();
    }

    /*
     * fancy case - aligning via mappings between sequences
     */
    List<SequenceI> unmapped = new ArrayList<>();
    Map<Integer, Map<SequenceI, Character>> columnMap = buildMappedColumnsMap(
            unaligned, aligned, unmapped);
    int width = columnMap.size();
    char gap = unaligned.getGapCharacter();
    int realignedCount = 0;
    // TODO: verify this loop scales sensibly for very wide/high alignments

    for (SequenceI seq : unaligned.getSequences())
    {
      if (!unmapped.contains(seq))
      {
        char[] newSeq = new char[width];
        Arrays.fill(newSeq, gap); // JBPComment - doubt this is faster than the
                                  // Integer iteration below
        int newCol = 0;
        int lastCol = 0;

        /*
         * traverse the map to find columns populated
         * by our sequence
         */
        for (Integer column : columnMap.keySet())
        {
          Character c = columnMap.get(column).get(seq);
          if (c != null)
          {
            /*
             * sequence has a character at this position
             * 
             */
            newSeq[newCol] = c;
            lastCol = newCol;
          }
          newCol++;
        }

        /*
         * trim trailing gaps
         */
        if (lastCol < width)
        {
          char[] tmp = new char[lastCol + 1];
          System.arraycopy(newSeq, 0, tmp, 0, lastCol + 1);
          newSeq = tmp;
        }
        // TODO: optimise SequenceI to avoid char[]->String->char[]
        seq.setSequence(String.valueOf(newSeq));
        realignedCount++;
      }
    }
    return realignedCount;
  }

  /**
   * If unaligned and aligned sequences share the same dataset sequences, then
   * simply copies the aligned sequences to the unaligned sequences and returns
   * true; else returns false
   * 
   * @param unaligned
   *          - sequences to be aligned based on aligned
   * @param aligned
   *          - 'guide' alignment containing sequences derived from same dataset
   *          as unaligned
   * @return
   */
  static boolean alignAsSameSequences(AlignmentI unaligned,
          AlignmentI aligned)
  {
    if (aligned.getDataset() == null || unaligned.getDataset() == null)
    {
      return false; // should only pass alignments with datasets here
    }

    // map from dataset sequence to alignment sequence(s)
    Map<SequenceI, List<SequenceI>> alignedDatasets = new HashMap<>();
    for (SequenceI seq : aligned.getSequences())
    {
      SequenceI ds = seq.getDatasetSequence();
      if (alignedDatasets.get(ds) == null)
      {
        alignedDatasets.put(ds, new ArrayList<SequenceI>());
      }
      alignedDatasets.get(ds).add(seq);
    }

    /*
     * first pass - check whether all sequences to be aligned share a 
     * dataset sequence with an aligned sequence; also note the leftmost
     * ungapped column from which to copy
     */
    int leftmost = Integer.MAX_VALUE;
    for (SequenceI seq : unaligned.getSequences())
    {
      final SequenceI ds = seq.getDatasetSequence();
      if (!alignedDatasets.containsKey(ds))
      {
        return false;
      }
      SequenceI alignedSeq = alignedDatasets.get(ds).get(0);
      int startCol = alignedSeq.findIndex(seq.getStart()); // 1..
      leftmost = Math.min(leftmost, startCol);
    }

    /*
     * second pass - copy aligned sequences;
     * heuristic rule: pair off sequences in order for the case where 
     * more than one shares the same dataset sequence 
     */
    final char gapCharacter = aligned.getGapCharacter();
    for (SequenceI seq : unaligned.getSequences())
    {
      List<SequenceI> alignedSequences = alignedDatasets
              .get(seq.getDatasetSequence());
      if (alignedSequences.isEmpty())
      {
        /*
         * defensive check - shouldn't happen! (JAL-3536)
         */
        continue;
      }
      SequenceI alignedSeq = alignedSequences.get(0);

      /*
       * gap fill for leading (5') UTR if any
       */
      // TODO this copies intron columns - wrong!
      int startCol = alignedSeq.findIndex(seq.getStart()); // 1..
      int endCol = alignedSeq.findIndex(seq.getEnd());
      char[] seqchars = new char[endCol - leftmost + 1];
      Arrays.fill(seqchars, gapCharacter);
      char[] toCopy = alignedSeq.getSequence(startCol - 1, endCol);
      System.arraycopy(toCopy, 0, seqchars, startCol - leftmost,
              toCopy.length);
      seq.setSequence(String.valueOf(seqchars));
      if (alignedSequences.size() > 0)
      {
        // pop off aligned sequences (except the last one)
        alignedSequences.remove(0);
      }
    }

    /*
     * finally remove gapped columns (e.g. introns)
     */
    new RemoveGapColCommand("", unaligned.getSequencesArray(), 0,
            unaligned.getWidth() - 1, unaligned);

    return true;
  }

  /**
   * Returns a map whose key is alignment column number (base 1), and whose
   * values are a map of sequence characters in that column.
   * 
   * @param unaligned
   * @param aligned
   * @param unmapped
   * @return
   */
  static SortedMap<Integer, Map<SequenceI, Character>> buildMappedColumnsMap(
          AlignmentI unaligned, AlignmentI aligned,
          List<SequenceI> unmapped)
  {
    /*
     * Map will hold, for each aligned column position, a map of
     * {unalignedSequence, characterPerSequence} at that position.
     * TreeMap keeps the entries in ascending column order. 
     */
    SortedMap<Integer, Map<SequenceI, Character>> map = new TreeMap<>();

    /*
     * record any sequences that have no mapping so can't be realigned
     */
    unmapped.addAll(unaligned.getSequences());

    List<AlignedCodonFrame> mappings = aligned.getCodonFrames();

    for (SequenceI seq : unaligned.getSequences())
    {
      for (AlignedCodonFrame mapping : mappings)
      {
        SequenceI fromSeq = mapping.findAlignedSequence(seq, aligned);
        if (fromSeq != null)
        {
          Mapping seqMap = mapping.getMappingBetween(fromSeq, seq);
          if (addMappedPositions(seq, fromSeq, seqMap, map))
          {
            unmapped.remove(seq);
          }
        }
      }
    }
    return map;
  }

  /**
   * Helper method that adds to a map the mapped column positions of a sequence.
   * <br>
   * For example if aaTT-Tg-gAAA is mapped to TTTAAA then the map should record
   * that columns 3,4,6,10,11,12 map to characters T,T,T,A,A,A of the mapped to
   * sequence.
   * 
   * @param seq
   *          the sequence whose column positions we are recording
   * @param fromSeq
   *          a sequence that is mapped to the first sequence
   * @param seqMap
   *          the mapping from 'fromSeq' to 'seq'
   * @param map
   *          a map to add the column positions (in fromSeq) of the mapped
   *          positions of seq
   * @return
   */
  static boolean addMappedPositions(SequenceI seq, SequenceI fromSeq,
          Mapping seqMap, Map<Integer, Map<SequenceI, Character>> map)
  {
    if (seqMap == null)
    {
      return false;
    }

    /*
     * invert mapping if it is from unaligned to aligned sequence
     */
    if (seqMap.getTo() == fromSeq.getDatasetSequence())
    {
      seqMap = new Mapping(seq.getDatasetSequence(),
              seqMap.getMap().getInverse());
    }

    int toStart = seq.getStart();

    /*
     * traverse [start, end, start, end...] ranges in fromSeq
     */
    for (int[] fromRange : seqMap.getMap().getFromRanges())
    {
      for (int i = 0; i < fromRange.length - 1; i += 2)
      {
        boolean forward = fromRange[i + 1] >= fromRange[i];

        /*
         * find the range mapped to (sequence positions base 1)
         */
        int[] range = seqMap.locateMappedRange(fromRange[i],
                fromRange[i + 1]);
        if (range == null)
        {
          System.err.println("Error in mapping " + seqMap + " from "
                  + fromSeq.getName());
          return false;
        }
        int fromCol = fromSeq.findIndex(fromRange[i]);
        int mappedCharPos = range[0];

        /*
         * walk over the 'from' aligned sequence in forward or reverse
         * direction; when a non-gap is found, record the column position
         * of the next character of the mapped-to sequence; stop when all
         * the characters of the range have been counted
         */
        while (mappedCharPos <= range[1] && fromCol <= fromSeq.getLength()
                && fromCol >= 0)
        {
          if (!Comparison.isGap(fromSeq.getCharAt(fromCol - 1)))
          {
            /*
             * mapped from sequence has a character in this column
             * record the column position for the mapped to character
             */
            Map<SequenceI, Character> seqsMap = map.get(fromCol);
            if (seqsMap == null)
            {
              seqsMap = new HashMap<>();
              map.put(fromCol, seqsMap);
            }
            seqsMap.put(seq, seq.getCharAt(mappedCharPos - toStart));
            mappedCharPos++;
          }
          fromCol += (forward ? 1 : -1);
        }
      }
    }
    return true;
  }

  // strictly temporary hack until proper criteria for aligning protein to cds
  // are in place; this is so Ensembl -> fetch xrefs Uniprot aligns the Uniprot
  public static boolean looksLikeEnsembl(AlignmentI alignment)
  {
    for (SequenceI seq : alignment.getSequences())
    {
      String name = seq.getName();
      if (!name.startsWith("ENSG") && !name.startsWith("ENST"))
      {
        return false;
      }
    }
    return true;
  }
}
