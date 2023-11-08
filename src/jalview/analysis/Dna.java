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

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignedCodon;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.FeatureProperties;
import jalview.datamodel.GraphLine;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.util.Comparison;
import jalview.util.DBRefUtils;
import jalview.util.MapList;
import jalview.util.ShiftList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Dna
{
  private static final String STOP_ASTERIX = "*";

  private static final Comparator<AlignedCodon> comparator = new CodonComparator();

  /*
   * 'final' variables describe the inputs to the translation, which should not
   * be modified.
   */
  private final List<SequenceI> selection;

  private final String[] seqstring;

  private final Iterator<int[]> contigs;

  private final char gapChar;

  private final AlignmentAnnotation[] annotations;

  private final int dnaWidth;

  private final AlignmentI dataset;

  private ShiftList vismapping;

  private int[] startcontigs;

  /*
   * Working variables for the translation.
   * 
   * The width of the translation-in-progress protein alignment.
   */
  private int aaWidth = 0;

  /*
   * This array will be built up so that position i holds the codon positions
   * e.g. [7, 9, 10] that match column i (base 0) in the aligned translation.
   * Note this implies a contract that if two codons do not align exactly, their
   * translated products must occupy different column positions.
   */
  private AlignedCodon[] alignedCodons;

  /**
   * Constructor given a viewport and the visible contigs.
   * 
   * @param viewport
   * @param visibleContigs
   */
  public Dna(AlignViewportI viewport, Iterator<int[]> visibleContigs)
  {
    this.selection = Arrays.asList(viewport.getSequenceSelection());
    this.seqstring = viewport.getViewAsString(true);
    this.contigs = visibleContigs;
    this.gapChar = viewport.getGapCharacter();
    this.annotations = viewport.getAlignment().getAlignmentAnnotation();
    this.dnaWidth = viewport.getAlignment().getWidth();
    this.dataset = viewport.getAlignment().getDataset();
    initContigs();
  }

  /**
   * Initialise contigs used as starting point for translateCodingRegion
   */
  private void initContigs()
  {
    vismapping = new ShiftList(); // map from viscontigs to seqstring
    // intervals

    int npos = 0;
    int[] lastregion = null;
    ArrayList<Integer> tempcontigs = new ArrayList<>();
    while (contigs.hasNext())
    {
      int[] region = contigs.next();
      if (lastregion == null)
      {
        vismapping.addShift(npos, region[0]);
      }
      else
      {
        // hidden region
        vismapping.addShift(npos, region[0] - lastregion[1] + 1);
      }
      lastregion = region;
      tempcontigs.add(region[0]);
      tempcontigs.add(region[1]);
    }

    startcontigs = new int[tempcontigs.size()];
    int i = 0;
    for (Integer val : tempcontigs)
    {
      startcontigs[i] = val;
      i++;
    }
    tempcontigs = null;
  }

  /**
   * Test whether codon positions cdp1 should align before, with, or after cdp2.
   * Returns zero if all positions match (or either argument is null). Returns
   * -1 if any position in the first codon precedes the corresponding position
   * in the second codon. Else returns +1 (some position in the second codon
   * precedes the corresponding position in the first).
   *
   * Note this is not necessarily symmetric, for example:
   * <ul>
   * <li>compareCodonPos([2,5,6], [3,4,5]) returns -1</li>
   * <li>compareCodonPos([3,4,5], [2,5,6]) also returns -1</li>
   * </ul>
   * 
   * @param ac1
   * @param ac2
   * @return
   */
  public static final int compareCodonPos(AlignedCodon ac1,
          AlignedCodon ac2)
  {
    return comparator.compare(ac1, ac2);
    // return jalview_2_8_2compare(ac1, ac2);
  }

  /**
   * Codon comparison up to Jalview 2.8.2. This rule is sequence order dependent
   * - see http://issues.jalview.org/browse/JAL-1635
   * 
   * @param ac1
   * @param ac2
   * @return
   */
  private static int jalview_2_8_2compare(AlignedCodon ac1,
          AlignedCodon ac2)
  {
    if (ac1 == null || ac2 == null || (ac1.equals(ac2)))
    {
      return 0;
    }
    if (ac1.pos1 < ac2.pos1 || ac1.pos2 < ac2.pos2 || ac1.pos3 < ac2.pos3)
    {
      // one base in cdp1 precedes the corresponding base in the other codon
      return -1;
    }
    // one base in cdp1 appears after the corresponding base in the other codon.
    return 1;
  }

  /**
   * Translates cDNA using the specified code table
   * 
   * @return
   */
  public AlignmentI translateCdna(GeneticCodeI codeTable)
  {
    AlignedCodonFrame acf = new AlignedCodonFrame();

    alignedCodons = new AlignedCodon[dnaWidth];

    int s;
    int sSize = selection.size();
    List<SequenceI> pepseqs = new ArrayList<>();
    for (s = 0; s < sSize; s++)
    {
      SequenceI newseq = translateCodingRegion(selection.get(s),
              seqstring[s], acf, pepseqs, codeTable);

      if (newseq != null)
      {
        pepseqs.add(newseq);
        SequenceI ds = newseq;
        if (dataset != null)
        {
          while (ds.getDatasetSequence() != null)
          {
            ds = ds.getDatasetSequence();
          }
          dataset.addSequence(ds);
        }
      }
    }

    SequenceI[] newseqs = pepseqs.toArray(new SequenceI[pepseqs.size()]);
    AlignmentI al = new Alignment(newseqs);
    // ensure we look aligned.
    al.padGaps();
    // link the protein translation to the DNA dataset
    al.setDataset(dataset);
    translateAlignedAnnotations(al, acf);
    al.addCodonFrame(acf);
    return al;
  }

  /**
   * fake the collection of DbRefs with associated exon mappings to identify if
   * a translation would generate distinct product in the currently selected
   * region.
   * 
   * @param selection
   * @param viscontigs
   * @return
   */
  public static boolean canTranslate(SequenceI[] selection,
          int viscontigs[])
  {
    for (int gd = 0; gd < selection.length; gd++)
    {
      SequenceI dna = selection[gd];
      List<DBRefEntry> dnarefs = DBRefUtils.selectRefs(dna.getDBRefs(),
              jalview.datamodel.DBRefSource.DNACODINGDBS);
      if (dnarefs != null)
      {
        // intersect with pep
        List<DBRefEntry> mappedrefs = new ArrayList<>();
        List<DBRefEntry> refs = dna.getDBRefs();
        for (int d = 0, nd = refs.size(); d < nd; d++)
        {
          DBRefEntry ref = refs.get(d);
          if (ref.getMap() != null && ref.getMap().getMap() != null
                  && ref.getMap().getMap().getFromRatio() == 3
                  && ref.getMap().getMap().getToRatio() == 1)
          {
            mappedrefs.add(ref); // add translated protein maps
          }
        }
        dnarefs = mappedrefs;// .toArray(new DBRefEntry[mappedrefs.size()]);
        for (int d = 0, nd = dnarefs.size(); d < nd; d++)
        {
          Mapping mp = dnarefs.get(d).getMap();
          if (mp != null)
          {
            for (int vc = 0, nv = viscontigs.length; vc < nv; vc += 2)
            {
              int[] mpr = mp.locateMappedRange(viscontigs[vc],
                      viscontigs[vc + 1]);
              if (mpr != null)
              {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * Translate nucleotide alignment annotations onto translated amino acid
   * alignment using codon mapping codons
   * 
   * @param al
   *          the translated protein alignment
   */
  protected void translateAlignedAnnotations(AlignmentI al,
          AlignedCodonFrame acf)
  {
    // Can only do this for columns with consecutive codons, or where
    // annotation is sequence associated.

    if (annotations != null)
    {
      for (AlignmentAnnotation annotation : annotations)
      {
        /*
         * Skip hidden or autogenerated annotation. Also (for now), RNA
         * secondary structure annotation. If we want to show this against
         * protein we need a smarter way to 'translate' without generating
         * invalid (unbalanced) structure annotation.
         */
        if (annotation.autoCalculated || !annotation.visible
                || annotation.isRNA())
        {
          continue;
        }

        int aSize = aaWidth;
        Annotation[] anots = (annotation.annotations == null) ? null
                : new Annotation[aSize];
        if (anots != null)
        {
          for (int a = 0; a < aSize; a++)
          {
            // process through codon map.
            if (a < alignedCodons.length && alignedCodons[a] != null
                    && alignedCodons[a].pos1 == (alignedCodons[a].pos3 - 2))
            {
              anots[a] = getCodonAnnotation(alignedCodons[a],
                      annotation.annotations);
            }
          }
        }

        AlignmentAnnotation aa = new AlignmentAnnotation(annotation.label,
                annotation.description, anots);
        aa.graph = annotation.graph;
        aa.graphGroup = annotation.graphGroup;
        aa.graphHeight = annotation.graphHeight;
        if (annotation.getThreshold() != null)
        {
          aa.setThreshold(new GraphLine(annotation.getThreshold()));
        }
        if (annotation.hasScore)
        {
          aa.setScore(annotation.getScore());
        }

        final SequenceI seqRef = annotation.sequenceRef;
        if (seqRef != null)
        {
          SequenceI aaSeq = acf.getAaForDnaSeq(seqRef);
          if (aaSeq != null)
          {
            // aa.compactAnnotationArray(); // throw away alignment annotation
            // positioning
            aa.setSequenceRef(aaSeq);
            // rebuild mapping
            aa.createSequenceMapping(aaSeq, aaSeq.getStart(), true);
            aa.adjustForAlignment();
            aaSeq.addAlignmentAnnotation(aa);
          }
        }
        al.addAnnotation(aa);
      }
    }
  }

  private static Annotation getCodonAnnotation(AlignedCodon is,
          Annotation[] annotations)
  {
    // Have a look at all the codon positions for annotation and put the first
    // one found into the translated annotation pos.
    int contrib = 0;
    Annotation annot = null;
    for (int p = 1; p <= 3; p++)
    {
      int dnaCol = is.getBaseColumn(p);
      if (annotations[dnaCol] != null)
      {
        if (annot == null)
        {
          annot = new Annotation(annotations[dnaCol]);
          contrib = 1;
        }
        else
        {
          // merge with last
          Annotation cpy = new Annotation(annotations[dnaCol]);
          if (annot.colour == null)
          {
            annot.colour = cpy.colour;
          }
          if (annot.description == null || annot.description.length() == 0)
          {
            annot.description = cpy.description;
          }
          if (annot.displayCharacter == null)
          {
            annot.displayCharacter = cpy.displayCharacter;
          }
          if (annot.secondaryStructure == 0)
          {
            annot.secondaryStructure = cpy.secondaryStructure;
          }
          annot.value += cpy.value;
          contrib++;
        }
      }
    }
    if (contrib > 1)
    {
      annot.value /= contrib;
    }
    return annot;
  }

  /**
   * Translate a na sequence
   * 
   * @param selection
   *          sequence displayed under viscontigs visible columns
   * @param seqstring
   *          ORF read in some global alignment reference frame
   * @param acf
   *          Definition of global ORF alignment reference frame
   * @param proteinSeqs
   * @param codeTable
   * @return sequence ready to be added to alignment.
   */
  protected SequenceI translateCodingRegion(SequenceI selection,
          String seqstring, AlignedCodonFrame acf,
          List<SequenceI> proteinSeqs, GeneticCodeI codeTable)
  {
    List<int[]> skip = new ArrayList<>();
    int[] skipint = null;

    int npos = 0;
    int vc = 0;

    int[] scontigs = new int[startcontigs.length];
    System.arraycopy(startcontigs, 0, scontigs, 0, startcontigs.length);

    // allocate a roughly sized buffer for the protein sequence
    StringBuilder protein = new StringBuilder(seqstring.length() / 2);
    String seq = seqstring.replace('U', 'T').replace('u', 'T');
    char codon[] = new char[3];
    int cdp[] = new int[3];
    int rf = 0;
    int lastnpos = 0;
    int nend;
    int aspos = 0;
    int resSize = 0;
    for (npos = 0, nend = seq.length(); npos < nend; npos++)
    {
      if (!Comparison.isGap(seq.charAt(npos)))
      {
        cdp[rf] = npos; // store position
        codon[rf++] = seq.charAt(npos); // store base
      }
      if (rf == 3)
      {
        /*
         * Filled up a reading frame...
         */
        AlignedCodon alignedCodon = new AlignedCodon(cdp[0], cdp[1],
                cdp[2]);
        String aa = codeTable.translate(new String(codon));
        rf = 0;
        final String gapString = String.valueOf(gapChar);
        if (aa == null)
        {
          aa = gapString;
          if (skipint == null)
          {
            skipint = new int[] { alignedCodon.pos1,
                alignedCodon.pos3 /*
                                   * cdp[0],
                                   * cdp[2]
                                   */ };
          }
          skipint[1] = alignedCodon.pos3; // cdp[2];
        }
        else
        {
          if (skipint != null)
          {
            // edit scontigs
            skipint[0] = vismapping.shift(skipint[0]);
            skipint[1] = vismapping.shift(skipint[1]);
            for (vc = 0; vc < scontigs.length;)
            {
              if (scontigs[vc + 1] < skipint[0])
              {
                // before skipint starts
                vc += 2;
                continue;
              }
              if (scontigs[vc] > skipint[1])
              {
                // finished editing so
                break;
              }
              // Edit the contig list to include the skipped region which did
              // not translate
              int[] t;
              // from : s1 e1 s2 e2 s3 e3
              // to s: s1 e1 s2 k0 k1 e2 s3 e3
              // list increases by one unless one boundary (s2==k0 or e2==k1)
              // matches, and decreases by one if skipint intersects whole
              // visible contig
              if (scontigs[vc] <= skipint[0])
              {
                if (skipint[0] == scontigs[vc])
                {
                  // skipint at start of contig
                  // shift the start of this contig
                  if (scontigs[vc + 1] > skipint[1])
                  {
                    scontigs[vc] = skipint[1];
                    vc += 2;
                  }
                  else
                  {
                    if (scontigs[vc + 1] == skipint[1])
                    {
                      // remove the contig
                      t = new int[scontigs.length - 2];
                      if (vc > 0)
                      {
                        System.arraycopy(scontigs, 0, t, 0, vc - 1);
                      }
                      if (vc + 2 < t.length)
                      {
                        System.arraycopy(scontigs, vc + 2, t, vc,
                                t.length - vc + 2);
                      }
                      scontigs = t;
                    }
                    else
                    {
                      // truncate contig to before the skipint region
                      scontigs[vc + 1] = skipint[0] - 1;
                      vc += 2;
                    }
                  }
                }
                else
                {
                  // scontig starts before start of skipint
                  if (scontigs[vc + 1] < skipint[1])
                  {
                    // skipint truncates end of scontig
                    scontigs[vc + 1] = skipint[0] - 1;
                    vc += 2;
                  }
                  else
                  {
                    // divide region to new contigs
                    t = new int[scontigs.length + 2];
                    System.arraycopy(scontigs, 0, t, 0, vc + 1);
                    t[vc + 1] = skipint[0];
                    t[vc + 2] = skipint[1];
                    System.arraycopy(scontigs, vc + 1, t, vc + 3,
                            scontigs.length - (vc + 1));
                    scontigs = t;
                    vc += 4;
                  }
                }
              }
            }
            skip.add(skipint);
            skipint = null;
          }
          if (aa.equals(ResidueProperties.STOP))
          {
            aa = STOP_ASTERIX;
          }
          resSize++;
        }
        boolean findpos = true;
        while (findpos)
        {
          /*
           * Compare this codon's base positions with those currently aligned to
           * this column in the translation.
           */
          final int compareCodonPos = compareCodonPos(alignedCodon,
                  alignedCodons[aspos]);
          switch (compareCodonPos)
          {
          case -1:

            /*
             * This codon should precede the mapped positions - need to insert a
             * gap in all prior sequences.
             */
            insertAAGap(aspos, proteinSeqs);
            findpos = false;
            break;

          case +1:

            /*
             * This codon belongs after the aligned codons at aspos. Prefix it
             * with a gap and try the next position.
             */
            aa = gapString + aa;
            aspos++;
            break;

          case 0:

            /*
             * Exact match - codon 'belongs' at this translated position.
             */
            findpos = false;
          }
        }
        protein.append(aa);
        lastnpos = npos;
        if (alignedCodons[aspos] == null)
        {
          // mark this column as aligning to this aligned reading frame
          alignedCodons[aspos] = alignedCodon;
        }
        else if (!alignedCodons[aspos].equals(alignedCodon))
        {
          throw new IllegalStateException(
                  "Tried to coalign " + alignedCodons[aspos].toString()
                          + " with " + alignedCodon.toString());
        }
        if (aspos >= aaWidth)
        {
          // update maximum alignment width
          aaWidth = aspos;
        }
        // ready for next translated reading frame alignment position (if any)
        aspos++;
      }
    }
    if (resSize > 0)
    {
      SequenceI newseq = new Sequence(selection.getName(),
              protein.toString());
      if (rf != 0)
      {
        final String errMsg = "trimming contigs for incomplete terminal codon.";
        System.err.println(errMsg);
        // map and trim contigs to ORF region
        vc = scontigs.length - 1;
        lastnpos = vismapping.shift(lastnpos); // place npos in context of
        // whole dna alignment (rather
        // than visible contigs)
        // incomplete ORF could be broken over one or two visible contig
        // intervals.
        while (vc >= 0 && scontigs[vc] > lastnpos)
        {
          if (vc > 0 && scontigs[vc - 1] > lastnpos)
          {
            vc -= 2;
          }
          else
          {
            // correct last interval in list.
            scontigs[vc] = lastnpos;
          }
        }

        if (vc > 0 && (vc + 1) < scontigs.length)
        {
          // truncate map list to just vc elements
          int t[] = new int[vc + 1];
          System.arraycopy(scontigs, 0, t, 0, vc + 1);
          scontigs = t;
        }
        if (vc <= 0)
        {
          scontigs = null;
        }
      }
      if (scontigs != null)
      {
        npos = 0;
        // map scontigs to actual sequence positions on selection
        for (vc = 0; vc < scontigs.length; vc += 2)
        {
          scontigs[vc] = selection.findPosition(scontigs[vc]); // not from 1!
          scontigs[vc + 1] = selection.findPosition(scontigs[vc + 1]); // exclusive
          if (scontigs[vc + 1] == selection.getEnd())
          {
            break;
          }
        }
        // trim trailing empty intervals.
        if ((vc + 2) < scontigs.length)
        {
          int t[] = new int[vc + 2];
          System.arraycopy(scontigs, 0, t, 0, vc + 2);
          scontigs = t;
        }
        /*
         * delete intervals in scontigs which are not translated. 1. map skip
         * into sequence position intervals 2. truncate existing ranges and add
         * new ranges to exclude untranslated regions. if (skip.size()>0) {
         * Vector narange = new Vector(); for (vc=0; vc<scontigs.length; vc++) {
         * narange.addElement(new int[] {scontigs[vc]}); } int sint=0,iv[]; vc =
         * 0; while (sint<skip.size()) { skipint = (int[]) skip.elementAt(sint);
         * do { iv = (int[]) narange.elementAt(vc); if (iv[0]>=skipint[0] &&
         * iv[0]<=skipint[1]) { if (iv[0]==skipint[0]) { // delete beginning of
         * range } else { // truncate range and create new one if necessary iv =
         * (int[]) narange.elementAt(vc+1); if (iv[0]<=skipint[1]) { // truncate
         * range iv[0] = skipint[1]; } else { } } } else if (iv[0]<skipint[0]) {
         * iv = (int[]) narange.elementAt(vc+1); } } while (iv[0]) } }
         */
        MapList map = new MapList(scontigs, new int[] { 1, resSize }, 3, 1);

        transferCodedFeatures(selection, newseq, map);

        /*
         * Construct a dataset sequence for our new peptide.
         */
        SequenceI rseq = newseq.deriveSequence();

        /*
         * Store a mapping (between the dataset sequences for the two
         * sequences).
         */
        // SIDE-EFFECT: acf stores the aligned sequence reseq; to remove!
        acf.addMap(selection, rseq, map);
        return rseq;
      }
    }
    // register the mapping somehow
    //
    return null;
  }

  /**
   * Insert a gap into the aligned proteins and the codon mapping array.
   * 
   * @param pos
   * @param proteinSeqs
   * @return
   */
  protected void insertAAGap(int pos, List<SequenceI> proteinSeqs)
  {
    aaWidth++;
    for (SequenceI seq : proteinSeqs)
    {
      seq.insertCharAt(pos, gapChar);
    }

    checkCodonFrameWidth();
    if (pos < aaWidth)
    {
      aaWidth++;

      /*
       * Shift from [pos] to the end one to the right, and null out [pos]
       */
      System.arraycopy(alignedCodons, pos, alignedCodons, pos + 1,
              alignedCodons.length - pos - 1);
      alignedCodons[pos] = null;
    }
  }

  /**
   * Check the codons array can accommodate a single insertion, if not resize
   * it.
   */
  protected void checkCodonFrameWidth()
  {
    if (alignedCodons[alignedCodons.length - 1] != null)
    {
      /*
       * arraycopy insertion would bump a filled slot off the end, so expand.
       */
      AlignedCodon[] c = new AlignedCodon[alignedCodons.length + 10];
      System.arraycopy(alignedCodons, 0, c, 0, alignedCodons.length);
      alignedCodons = c;
    }
  }

  /**
   * Given a peptide newly translated from a dna sequence, copy over and set any
   * features on the peptide from the DNA.
   * 
   * @param dna
   * @param pep
   * @param map
   */
  private static void transferCodedFeatures(SequenceI dna, SequenceI pep,
          MapList map)
  {
    // BH 2019.01.25 nop?
    // List<DBRefEntry> dnarefs = DBRefUtils.selectRefs(dna.getDBRefs(),
    // DBRefSource.DNACODINGDBS);
    // if (dnarefs != null)
    // {
    // // intersect with pep
    // for (int d = 0, nd = dnarefs.size(); d < nd; d++)
    // {
    // Mapping mp = dnarefs.get(d).getMap();
    // if (mp != null)
    // {
    // }
    // }
    // }
    for (SequenceFeature sf : dna.getFeatures().getAllFeatures())
    {
      if (FeatureProperties.isCodingFeature(null, sf.getType()))
      {
        // if (map.intersectsFrom(sf[f].begin, sf[f].end))
        {

        }
      }
    }
  }

  /**
   * Returns an alignment consisting of the reversed (and optionally
   * complemented) sequences set in this object's constructor
   * 
   * @param complement
   * @return
   */
  public AlignmentI reverseCdna(boolean complement)
  {
    int sSize = selection.size();
    List<SequenceI> reversed = new ArrayList<>();
    for (int s = 0; s < sSize; s++)
    {
      SequenceI newseq = reverseSequence(selection.get(s).getName(),
              seqstring[s], complement);

      if (newseq != null)
      {
        reversed.add(newseq);
      }
    }

    SequenceI[] newseqs = reversed.toArray(new SequenceI[reversed.size()]);
    AlignmentI al = new Alignment(newseqs);
    ((Alignment) al).createDatasetAlignment();
    return al;
  }

  /**
   * Returns a reversed, and optionally complemented, sequence. The new
   * sequence's name is the original name with "|rev" or "|revcomp" appended.
   * aAcCgGtT and DNA ambiguity codes are complemented, any other characters are
   * left unchanged.
   * 
   * @param seq
   * @param complement
   * @return
   */
  public static SequenceI reverseSequence(String seqName, String sequence,
          boolean complement)
  {
    String newName = seqName + "|rev" + (complement ? "comp" : "");
    char[] originalSequence = sequence.toCharArray();
    int length = originalSequence.length;
    char[] reversedSequence = new char[length];
    int bases = 0;
    for (int i = 0; i < length; i++)
    {
      char c = complement ? getComplement(originalSequence[i])
              : originalSequence[i];
      reversedSequence[length - i - 1] = c;
      if (!Comparison.isGap(c))
      {
        bases++;
      }
    }
    SequenceI reversed = new Sequence(newName, reversedSequence, 1, bases);
    return reversed;
  }

  /**
   * Answers the reverse complement of the input string
   * 
   * @see #getComplement(char)
   * @param s
   * @return
   */
  public static String reverseComplement(String s)
  {
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = s.length() - 1; i >= 0; i--)
    {
      sb.append(Dna.getComplement(s.charAt(i)));
    }
    return sb.toString();
  }

  /**
   * Returns dna complement (preserving case) for aAcCgGtTuU. Ambiguity codes
   * are treated as on http://reverse-complement.com/. Anything else is left
   * unchanged.
   * 
   * @param c
   * @return
   */
  public static char getComplement(char c)
  {
    char result = c;
    switch (c)
    {
    case '-':
    case '.':
    case ' ':
      break;
    case 'a':
      result = 't';
      break;
    case 'A':
      result = 'T';
      break;
    case 'c':
      result = 'g';
      break;
    case 'C':
      result = 'G';
      break;
    case 'g':
      result = 'c';
      break;
    case 'G':
      result = 'C';
      break;
    case 't':
      result = 'a';
      break;
    case 'T':
      result = 'A';
      break;
    case 'u':
      result = 'a';
      break;
    case 'U':
      result = 'A';
      break;
    case 'r':
      result = 'y';
      break;
    case 'R':
      result = 'Y';
      break;
    case 'y':
      result = 'r';
      break;
    case 'Y':
      result = 'R';
      break;
    case 'k':
      result = 'm';
      break;
    case 'K':
      result = 'M';
      break;
    case 'm':
      result = 'k';
      break;
    case 'M':
      result = 'K';
      break;
    case 'b':
      result = 'v';
      break;
    case 'B':
      result = 'V';
      break;
    case 'v':
      result = 'b';
      break;
    case 'V':
      result = 'B';
      break;
    case 'd':
      result = 'h';
      break;
    case 'D':
      result = 'H';
      break;
    case 'h':
      result = 'd';
      break;
    case 'H':
      result = 'D';
      break;
    }

    return result;
  }
}
