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

import java.util.Locale;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jalview.io.gff.Gff3Helper;
import jalview.schemes.ResidueProperties;
import jalview.util.MapList;
import jalview.util.MappingUtils;
import jalview.util.StringUtils;

/**
 * A data bean to hold a list of mapped sequence features (e.g. CDS features
 * mapped from protein), and the mapping between the sequences. It also provides
 * a method to derive peptide variants from codon variants.
 * 
 * @author gmcarstairs
 */
public class MappedFeatures
{
  /*
   * VEP CSQ:HGVSp (if present) is a short-cut to the protein variant consequence
   */
  private static final String HGV_SP = "HGVSp";

  private static final String CSQ = "CSQ";

  /*
   * the sequence the mapped features are on
   */
  private final SequenceI featureSequence;

  /*
   * the mapping between sequences;
   * NB this could be in either sense (from or to featureSequence)
   */
  private final Mapping mapping;

  /*
   * features on featureSequence that overlap the mapped positions
   */
  public final List<SequenceFeature> features;

  /*
   * the residue position in the sequence mapped to
   */
  private final int toPosition;

  /*
   * the residue at toPosition 
   */
  private final char toResidue;

  /*
   * if the mapping is 3:1 or 1:3 (peptide to CDS), this holds the
   * mapped positions i.e. codon base positions in CDS; to
   * support calculation of peptide variants from alleles
   */
  private final int[] codonPos;

  private final char[] baseCodon;

  /**
   * Constructor
   * 
   * @param theMapping
   *          sequence mapping (which may be either to, or from, the sequence
   *          holding the linked features)
   * @param featureSeq
   *          the sequence hosting the virtual features
   * @param pos
   *          the residue position in the sequence mapped to
   * @param res
   *          the residue character at position pos
   * @param theFeatures
   *          list of mapped features found in the 'featureSeq' sequence at the
   *          mapped position(s)
   */
  public MappedFeatures(Mapping theMapping, SequenceI featureSeq, int pos,
          char res, List<SequenceFeature> theFeatures)
  {
    mapping = theMapping;
    featureSequence = featureSeq;
    toPosition = pos;
    toResidue = res;
    features = theFeatures;

    /*
     * determine codon positions and canonical codon
     * for a peptide-to-CDS mapping
     */
    int[] codonIntervals = mapping.getMap().locateInFrom(toPosition,
            toPosition);
    int[] codonPositions = codonIntervals == null ? null
            : MappingUtils.flattenRanges(codonIntervals);
    if (codonPositions != null && codonPositions.length == 3)
    {
      codonPos = codonPositions;
      baseCodon = new char[3];
      int cdsStart = featureSequence.getStart();
      baseCodon[0] = Character.toUpperCase(
              featureSequence.getCharAt(codonPos[0] - cdsStart));
      baseCodon[1] = Character.toUpperCase(
              featureSequence.getCharAt(codonPos[1] - cdsStart));
      baseCodon[2] = Character.toUpperCase(
              featureSequence.getCharAt(codonPos[2] - cdsStart));
    }
    else
    {
      codonPos = null;
      baseCodon = null;
    }
  }

  /**
   * Computes and returns comma-delimited HGVS notation peptide variants derived
   * from codon allele variants. If no variants are found, answers an empty
   * string. The peptide variant is either simply read from the "CSQ:HGVSp"
   * attribute if present, else computed based on the "alleles" attribute if
   * present. If neither attribute is found, no variant (empty string) is
   * returned.
   * 
   * @param sf
   *          a sequence feature (which must be one of those held in this
   *          object)
   * @return
   */
  public String findProteinVariants(SequenceFeature sf)
  {
    if (!features.contains(sf) || baseCodon == null)
    {
      return "";
    }

    /*
     * VCF data may already contain the protein consequence
     */
    String hgvsp = sf.getValueAsString(CSQ, HGV_SP);
    if (hgvsp != null)
    {
      int colonPos = hgvsp.lastIndexOf(':');
      if (colonPos >= 0)
      {
        String var = hgvsp.substring(colonPos + 1);
        if (var.contains("p.")) // sanity check
        {
          return var;
        }
      }
    }

    /*
     * otherwise, compute codon and peptide variant
     */
    int cdsPos = sf.getBegin();
    if (cdsPos != sf.getEnd())
    {
      // not handling multi-locus variant features
      return "";
    }
    if (cdsPos != codonPos[0] && cdsPos != codonPos[1]
            && cdsPos != codonPos[2])
    {
      // e.g. feature on intron within spliced codon!
      return "";
    }

    String alls = (String) sf.getValue(Gff3Helper.ALLELES);
    if (alls == null)
    {
      return "";
    }

    String from3 = StringUtils.toSentenceCase(
            ResidueProperties.aa2Triplet.get(String.valueOf(toResidue)));

    /*
     * make a peptide variant for each SNP allele 
     * e.g. C,G,T gives variants G and T for base C
     */
    Set<String> variantPeptides = new HashSet<>();
    String[] alleles = alls.toUpperCase(Locale.ROOT).split(",");
    StringBuilder vars = new StringBuilder();

    for (String allele : alleles)
    {
      allele = allele.trim().toUpperCase(Locale.ROOT);
      if (allele.length() > 1 || "-".equals(allele))
      {
        continue; // multi-locus variant
      }
      char[] variantCodon = new char[3];
      variantCodon[0] = baseCodon[0];
      variantCodon[1] = baseCodon[1];
      variantCodon[2] = baseCodon[2];

      /*
       * poke variant base into canonical codon;
       * ignore first 'allele' (canonical base)
       */
      final int i = cdsPos == codonPos[0] ? 0
              : (cdsPos == codonPos[1] ? 1 : 2);
      variantCodon[i] = allele.toUpperCase(Locale.ROOT).charAt(0);
      if (variantCodon[i] == baseCodon[i])
      {
        continue;
      }
      String codon = new String(variantCodon);
      String peptide = ResidueProperties.codonTranslate(codon);
      boolean synonymous = toResidue == peptide.charAt(0);
      StringBuilder var = new StringBuilder();
      if (synonymous)
      {
        /*
         * synonymous variant notation e.g. c.1062C>A(p.=)
         */
        var.append("c.").append(String.valueOf(cdsPos))
                .append(String.valueOf(baseCodon[i])).append(">")
                .append(String.valueOf(variantCodon[i])).append("(p.=)");
      }
      else
      {
        /*
         * missense variant notation e.g. p.Arg355Met
         */
        String to3 = ResidueProperties.STOP.equals(peptide) ? "Ter"
                : StringUtils.toSentenceCase(
                        ResidueProperties.aa2Triplet.get(peptide));
        var.append("p.").append(from3).append(String.valueOf(toPosition))
                .append(to3);
      }
      if (!variantPeptides.contains(peptide)) // duplicate consequence
      {
        variantPeptides.add(peptide);
        if (vars.length() > 0)
        {
          vars.append(",");
        }
        vars.append(var);
      }
    }

    return vars.toString();
  }

  /**
   * Answers the name of the linked sequence holding any mapped features
   * 
   * @return
   */
  public String getLinkedSequenceName()
  {
    return featureSequence == null ? null : featureSequence.getName();
  }

  /**
   * Answers the mapped ranges (as one or more [start, end] positions) which
   * correspond to the given [begin, end] range of the linked sequence.
   * 
   * <pre>
   * Example: MappedFeatures with CDS features mapped to peptide 
   * CDS/200-220 gtc aac TGa acGt att AAC tta
   * mapped to PEP/6-7 WN by mapping [206, 207, 210, 210, 215, 217] to [6, 7]
   * getMappedPositions(206, 206) should return [6, 6]
   * getMappedPositions(200, 214) should return [6, 6]
   * getMappedPositions(210, 215) should return [6, 7]
   * </pre>
   * 
   * @param begin
   * @param end
   * @return
   */
  public int[] getMappedPositions(int begin, int end)
  {
    MapList map = mapping.getMap();
    return mapping.to == featureSequence ? map.getOverlapsInFrom(begin, end)
            : map.getOverlapsInTo(begin, end);
  }

  /**
   * Answers true if the linked features are on coding sequence, false if on
   * peptide
   * 
   * @return
   */
  public boolean isFromCds()
  {
    if (mapping.getMap().getFromRatio() == 3)
    {
      return mapping.to != featureSequence;
    }
    return mapping.to == featureSequence;
  }
}
