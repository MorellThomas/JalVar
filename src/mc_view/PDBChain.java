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
package mc_view;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import jalview.analysis.AlignSeq;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.annotations.AnnotationRowBuilder;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ResidueProperties;
import jalview.structure.StructureImportSettings;
import jalview.structure.StructureMapping;
import jalview.util.Comparison;
import jalview.ws.datamodel.MappableContactMatrixI;

public class PDBChain
{
  public static final String RESNUM_FEATURE = "RESNUM";

  private static final String IEASTATUS = "IEA:jalview";

  public String id;

  public Vector<Bond> bonds = new Vector<>();

  public Vector<Atom> atoms = new Vector<>();

  public Vector<Residue> residues = new Vector<>();

  public int offset;

  /**
   * sequence is the sequence extracted by the chain parsing code
   */
  public SequenceI sequence;

  /**
   * shadow is the sequence created by any other parsing processes (e.g. Jmol,
   * RNAview)
   */
  public SequenceI shadow = null;

  public boolean isNa = false;

  public boolean isVisible = true;

  public int pdbstart = 0;

  public int pdbend = 0;

  public int seqstart = 0;

  public int seqend = 0;

  public String pdbid = "";

  AnnotationRowBuilder tfacTemplate = new AnnotationRowBuilder(
          "Temperature Factor");

  public PDBChain(String thePdbid, String theId,
          AnnotationRowBuilder template)
  {
    this(thePdbid, theId);
    if (template != null)
    {
      tfacTemplate = template;
    }
  }

  /**
   * import chain data assuming Temperature Factor is in the Temperature Factor
   * column
   * 
   * @param thePdbid
   * @param theId
   */
  public PDBChain(String thePdbid, String theId)
  {
    this.pdbid = thePdbid == null ? thePdbid
            : thePdbid.toLowerCase(Locale.ROOT);
    this.id = theId;
  }

  /**
   * character used to write newlines
   */
  protected String newline = System.getProperty("line.separator");

  public Mapping shadowMap;

  public void setNewlineString(String nl)
  {
    newline = nl;
  }

  public String getNewlineString()
  {
    return newline;
  }

  public String print()
  {
    StringBuilder tmp = new StringBuilder(256);

    for (Bond b : bonds)
    {
      tmp.append(b.at1.resName).append(" ").append(b.at1.resNumber)
              .append(" ").append(offset).append(newline);
    }

    return tmp.toString();
  }

  /**
   * Annotate the residues with their corresponding positions in s1 using the
   * alignment in as NOTE: This clears all atom.alignmentMapping values on the
   * structure.
   * 
   * @param as
   * @param s1
   */
  public void makeExactMapping(AlignSeq as, SequenceI s1)
  {
    int pdbpos = as.getSeq2Start() - 2;
    int alignpos = s1.getStart() + as.getSeq1Start() - 3;
    // first clear out any old alignmentMapping values:
    for (Atom atom : atoms)
    {
      atom.alignmentMapping = -1;
    }
    // and now trace the alignment onto the atom set.
    for (int i = 0; i < as.astr1.length(); i++)
    {
      if (as.astr1.charAt(i) != '-')
      {
        alignpos++;
      }

      if (as.astr2.charAt(i) != '-')
      {
        pdbpos++;
      }

      boolean sameResidue = Comparison.isSameResidue(as.astr1.charAt(i),
              as.astr2.charAt(i), false);
      if (sameResidue)
      {
        if (pdbpos >= residues.size())
        {
          continue;
        }
        Residue res = residues.elementAt(pdbpos);
        for (Atom atom : res.atoms)
        {
          atom.alignmentMapping = alignpos;
        }
      }
    }
  }

  /**
   * Annotate the residues with their corresponding positions in s1 using the
   * alignment in as NOTE: This clears all atom.alignmentMapping values on the
   * structure.
   * 
   * @param as
   * @param s1
   */
  public void makeExactMapping(StructureMapping mapping, SequenceI s1)
  {
    // first clear out any old alignmentMapping values:
    for (Atom atom : atoms)
    {
      atom.alignmentMapping = -1;
    }
    SequenceI ds = s1;
    while (ds.getDatasetSequence() != null)
    {
      ds = ds.getDatasetSequence();
    }
    int pdboffset = 0;
    for (Residue res : residues)
    {
      // res.number isn't set correctly for discontinuous/mismapped residues
      int seqpos = mapping.getSeqPos(res.atoms.get(0).resNumber);
      char strchar = sequence.getCharAt(pdboffset++);
      if (seqpos == StructureMapping.UNASSIGNED_VALUE)
      {
        continue;
      }
      char seqchar = ds.getCharAt(seqpos - ds.getStart());

      boolean sameResidue = Comparison.isSameResidue(seqchar, strchar,
              false);
      if (sameResidue)
      {
        for (Atom atom : res.atoms)
        {
          atom.alignmentMapping = seqpos - 1;
        }
      }
    }
  }

  /**
   * Copies over the RESNUM seqfeatures from the internal chain sequence to the
   * mapped sequence
   * 
   * @param seq
   * @param status
   *          The Status of the transferred annotation
   * 
   * @param altPDBID
   *          the group id for the features on the destination sequence (e.g.
   *          the official accession ID)
   */
  public void transferRESNUMFeatures(SequenceI seq, String status,
          String altPDBID)
  {
    if (altPDBID == null)
    {
      altPDBID = pdbid;
    }
    SequenceI sq = seq;
    while (sq != null && sq.getDatasetSequence() != null)
    {
      sq = sq.getDatasetSequence();
      if (sq == sequence)
      {
        return;
      }
    }

    /*
     * Remove any existing features for this chain if they exist ?
     * SequenceFeature[] seqsfeatures=seq.getSequenceFeatures(); int
     * totfeat=seqsfeatures.length; // Remove any features for this exact chain
     * ? for (int i=0; i<seqsfeatures.length; i++) { }
     */
    if (status == null)
    {
      status = PDBChain.IEASTATUS;
    }

    List<SequenceFeature> features = sequence.getSequenceFeatures();
    for (SequenceFeature feature : features)
    {
      if (feature.getFeatureGroup() != null
              && feature.getFeatureGroup().equals(pdbid))
      {
        int newBegin = 1
                + residues.elementAt(feature.getBegin() - offset).atoms
                        .elementAt(0).alignmentMapping;
        int newEnd = 1 + residues.elementAt(feature.getEnd() - offset).atoms
                .elementAt(0).alignmentMapping;
        SequenceFeature tx = new SequenceFeature(feature, newBegin, newEnd,
                altPDBID, feature.getScore());
        tx.setStatus(status
                + ((tx.getStatus() == null || tx.getStatus().length() == 0)
                        ? ""
                        : ":" + tx.getStatus()));
        if (tx.begin != 0 && tx.end != 0)
        {
          sq.addSequenceFeature(tx);
        }
      }
    }
  }

  /**
   * Traverses the list of residues and constructs bonds where CA-to-CA atoms or
   * P-to-P atoms are found. Also sets the 'isNa' flag if more than 99% of
   * residues contain a P not a CA.
   */
  public void makeCaBondList()
  {
    boolean na = false;
    int numNa = 0;
    for (int i = 0; i < (residues.size() - 1); i++)
    {
      Residue tmpres = residues.elementAt(i);
      Residue tmpres2 = residues.elementAt(i + 1);
      Atom at1 = tmpres.findAtom("CA");
      Atom at2 = tmpres2.findAtom("CA");
      na = false;
      if ((at1 == null) && (at2 == null))
      {
        na = true;
        at1 = tmpres.findAtom("P");
        at2 = tmpres2.findAtom("P");
      }
      if ((at1 != null) && (at2 != null))
      {
        if (at1.chain.equals(at2.chain))
        {
          if (na)
          {
            numNa++;
          }
          makeBond(at1, at2);
        }
      }
      else
      {
        System.out.println("not found " + i);
      }
    }

    /*
     * If > 99% 'P', flag as nucleotide; note the count doesn't include the last
     * residue
     */
    if (residues.size() > 1 && (numNa / (residues.size() - 1) > 0.99))
    {
      isNa = true;
    }
  }

  /**
   * Construct a bond from atom1 to atom2 and add it to the list of bonds for
   * this chain
   * 
   * @param at1
   * @param at2
   */
  public void makeBond(Atom at1, Atom at2)
  {
    bonds.addElement(new Bond(at1, at2));
  }

  /**
   * Traverses the list of atoms and
   * <ul>
   * <li>constructs a list of Residues, each containing all the atoms that share
   * the same residue number</li>
   * <li>adds a RESNUM sequence feature for each position</li>
   * <li>creates the sequence string</li>
   * <li>determines if nucleotide</li>
   * <li>saves the residue number of the first atom as 'offset'</li>
   * <li>adds temp factor annotation if the flag is set to do so</li>
   * </ul>
   * 
   * @param visibleChainAnnotation
   */
  public void makeResidueList(boolean visibleChainAnnotation)
  {
    int count = 0;
    Object symbol;
    boolean deoxyn = false;
    boolean nucleotide = false;
    StringBuilder seq = new StringBuilder(256);
    Vector<SequenceFeature> resFeatures = new Vector<>();
    Vector<Annotation> resAnnotation = new Vector<>();
    int iSize = atoms.size() - 1;
    int resNumber = -1;
    char insCode = ' ';

    for (int i = 0; i <= iSize; i++)
    {
      Atom tmp = atoms.elementAt(i);
      resNumber = tmp.resNumber;
      insCode = tmp.insCode;

      int res = resNumber;
      char ins = insCode;

      if (i == 0)
      {
        offset = resNumber;
      }

      Vector<Atom> resAtoms = new Vector<>();
      // Add atoms to a vector while the residue number
      // remains the same as the first atom's resNumber (res)
      while ((resNumber == res) && (ins == insCode) && (i < atoms.size()))
      {
        resAtoms.add(atoms.elementAt(i));
        i++;

        if (i < atoms.size())
        {
          resNumber = atoms.elementAt(i).resNumber;
          insCode = atoms.elementAt(i).insCode;
        }
        else
        {
          resNumber++;
        }
      }

      // We need this to keep in step with the outer for i = loop
      i--;

      // Add inserted residues as features to the base residue
      Atom currAtom = resAtoms.get(0);
      if (currAtom.insCode != ' ' && !residues.isEmpty()
              && residues.lastElement().atoms
                      .get(0).resNumber == currAtom.resNumber)
      {
        String desc = currAtom.resName + ":" + currAtom.resNumIns + " "
                + pdbid + id;
        SequenceFeature sf = new SequenceFeature("INSERTION", desc,
                offset + count - 1, offset + count - 1, "PDB_INS");
        resFeatures.addElement(sf);
        residues.lastElement().atoms.addAll(resAtoms);
      }
      else
      {
        // Make a new Residue object with the new atoms vector
        residues.addElement(new Residue(resAtoms, resNumber - 1, count));

        Residue tmpres = residues.lastElement();
        Atom tmpat = tmpres.atoms.get(0);
        // Make A new SequenceFeature for the current residue numbering
        String desc = tmpat.resName + ":" + tmpat.resNumIns + " " + pdbid
                + id;
        SequenceFeature sf = new SequenceFeature(RESNUM_FEATURE, desc,
                offset + count, offset + count, pdbid);
        resFeatures.addElement(sf);
        resAnnotation.addElement(new Annotation(tmpat.tfactor));
        // Keep totting up the sequence

        if ((symbol = ResidueProperties.getAA3Hash()
                .get(tmpat.resName)) == null)
        {
          String nucname = tmpat.resName.trim();
          // use the aaIndex rather than call 'toLower' - which would take a bit
          // more time.
          deoxyn = nucname.length() == 2
                  && ResidueProperties.aaIndex[nucname
                          .charAt(0)] == ResidueProperties.aaIndex['D'];
          if (tmpat.name.equalsIgnoreCase("CA")
                  || ResidueProperties.nucleotideIndex[nucname
                          .charAt((deoxyn ? 1 : 0))] == -1)
          {
            char r = ResidueProperties.getSingleCharacterCode(
                    ResidueProperties.getCanonicalAminoAcid(tmpat.resName));
            seq.append(r == '0' ? 'X' : r);
            // System.err.println("PDBReader:Null aa3Hash for " +
            // tmpat.resName);
          }
          else
          {
            // nucleotide flag
            nucleotide = true;
            seq.append(nucname.charAt((deoxyn ? 1 : 0)));
          }
        }
        else
        {
          if (nucleotide)
          {
            System.err.println(
                    "Warning: mixed nucleotide and amino acid chain.. its gonna do bad things to you!");
          }
          seq.append(ResidueProperties.aa[((Integer) symbol).intValue()]);
        }
        count++;
      }
    }

    if (id.length() < 1)
    {
      id = " ";
    }
    isNa = nucleotide;
    sequence = new Sequence(id, seq.toString(), offset, resNumber - 1); // Note:
    // resNumber-offset
    // ~=
    // seq.size()
    // Add normalised feature scores to RESNUM indicating start/end of sequence
    // sf.setScore(offset+count);

    // System.out.println("PDB Sequence is :\nSequence = " + seq);
    // System.out.println("No of residues = " + residues.size());

    if (StructureImportSettings.isShowSeqFeatures())
    {
      iSize = resFeatures.size();
      for (int i = 0; i < iSize; i++)
      {
        sequence.addSequenceFeature(resFeatures.elementAt(i));
        resFeatures.setElementAt(null, i);
      }
    }
    if (visibleChainAnnotation)
    {
      Annotation[] annots = new Annotation[resAnnotation.size()];
      float max = 0f;
      float min = 0f;
      iSize = annots.length;
      for (int i = 0; i < iSize; i++)
      {
        annots[i] = resAnnotation.elementAt(i);
        tfacTemplate.processAnnotation(annots[i]);
        max = Math.max(max, annots[i].value);
        min = Math.min(min, annots[i].value);
        resAnnotation.setElementAt(null, i);
      }
      if (tfacTemplate.isHasMinMax())
      {
        max = tfacTemplate.getMax();
        min = tfacTemplate.getMin();
      }

      AlignmentAnnotation tfactorann = new AlignmentAnnotation(
              tfacTemplate.getName(),
              (tfacTemplate.isHasDescription()
                      ? tfacTemplate.getDescription()
                      : tfacTemplate.getName()) + " for " + pdbid + id,
              annots, min, max, AlignmentAnnotation.LINE_GRAPH);
      tfactorann.setTFType(tfacTemplate.getTFType());
      tfactorann.setCalcId(getClass().getName());

      tfactorann.setSequenceRef(sequence);
      sequence.addAlignmentAnnotation(tfactorann);
    }
  }

  /**
   * Colour start/end of bonds by charge
   * <ul>
   * <li>ASP and GLU red</li>
   * <li>LYS and ARG blue</li>
   * <li>CYS yellow</li>
   * <li>others light gray</li>
   * </ul>
   */
  public void setChargeColours()
  {
    for (Bond b : bonds)
    {
      if (b.at1 != null && b.at2 != null)
      {
        b.startCol = getChargeColour(b.at1.resName);
        b.endCol = getChargeColour(b.at2.resName);
      }
      else
      {
        b.startCol = Color.gray;
        b.endCol = Color.gray;
      }
    }
  }

  public static Color getChargeColour(String resName)
  {
    Color result = Color.lightGray;
    if ("ASP".equals(resName) || "GLU".equals(resName))
    {
      result = Color.red;
    }
    else if ("LYS".equals(resName) || "ARG".equals(resName))
    {
      result = Color.blue;
    }
    else if ("CYS".equals(resName))
    {
      result = Color.yellow;
    }
    return result;
  }

  /**
   * Sets the start/end colours of bonds to those of the start/end atoms
   * according to the specified colour scheme. Note: currently only works for
   * peptide residues.
   * 
   * @param cs
   */
  public void setChainColours(ColourSchemeI cs)
  {
    int index;
    for (Bond b : bonds)
    {
      try
      {
        index = ResidueProperties.aa3Hash.get(b.at1.resName).intValue();
        b.startCol = cs.findColour(ResidueProperties.aa[index].charAt(0), 0,
                null, null, 0f);

        index = ResidueProperties.aa3Hash.get(b.at2.resName).intValue();
        b.endCol = cs.findColour(ResidueProperties.aa[index].charAt(0), 0,
                null, null, 0f);

      } catch (Exception e)
      {
        b.startCol = Color.gray;
        b.endCol = Color.gray;
      }
    }
  }

  public void setChainColours(Color col)
  {
    for (Bond b : bonds)
    {
      b.startCol = col;
      b.endCol = col;
    }
  }

  /**
   * copy any sequence annotation onto the sequence mapped using the provided
   * StructureMapping
   * 
   * @param mapping
   *          - positional mapping between destination sequence and pdb resnum
   * @param sqmpping
   *          - mapping between destination sequence and local chain
   */
  public void transferResidueAnnotation(StructureMapping mapping,
          jalview.datamodel.Mapping sqmpping)
  {
    SequenceI sq = mapping.getSequence();
    SequenceI dsq = sq;
    if (sqmpping == null)
    {
      // SIFTS mappings are recorded in the StructureMapping object...

      sqmpping = mapping.getSeqToPdbMapping();
    }
    if (sq != null)
    {
      while (dsq.getDatasetSequence() != null)
      {
        dsq = dsq.getDatasetSequence();
      }
      // any annotation will be transferred onto the dataset sequence

      if (shadow != null && shadow.getAnnotation() != null)
      {

        for (AlignmentAnnotation ana : shadow.getAnnotation())
        {
          // match on calcId, label and description so annotations from
          // different structures are preserved
          List<AlignmentAnnotation> transfer = sq.getAlignmentAnnotations(
                  ana.getCalcId(), ana.label, ana.description);
          if (transfer == null || transfer.size() == 0)
          {
            ContactMatrixI cm = shadow.getContactMatrixFor(ana);
            ana = new AlignmentAnnotation(ana);
            // TODO map contact matrix under mapping
            ana.liftOver(sequence, shadowMap);
            ana.liftOver(dsq, sqmpping);
            dsq.addAlignmentAnnotation(ana);
            if (cm != null)
            {
              dsq.addContactListFor(ana, cm);
            }
          }
          else
          {
            continue;
          }
        }
      }
      else
      {
        if (sequence != null && sequence.getAnnotation() != null)
        {
          for (AlignmentAnnotation ana : sequence.getAnnotation())
          {
            // match on calcId, label and description so annotations from
            // different structures are preserved
            List<AlignmentAnnotation> transfer = dsq
                    .getAlignmentAnnotations(ana.getCalcId(), ana.label,
                            ana.description);
            if (transfer == null || transfer.size() == 0)
            {
              ContactMatrixI cm = sequence.getContactMatrixFor(ana);
              ana = new AlignmentAnnotation(ana);
              ana.liftOver(dsq, sqmpping);
              dsq.addAlignmentAnnotation(ana);
              if (cm != null && cm instanceof MappableContactMatrixI)
              {
                dsq.addContactListFor(ana, ((MappableContactMatrixI) cm)
                        .liftOver(dsq, sqmpping));
              }
            }
            else
            {
              continue;
            }
          }
        }
      }
      if (false)
      {
        // Useful for debugging mappings - adds annotation for mapped position
        float min = -1, max = 0;
        Annotation[] an = new Annotation[sq.getEnd() - sq.getStart() + 1];
        for (int i = sq.getStart(), j = sq
                .getEnd(), k = 0; i <= j; i++, k++)
        {
          int prn = mapping.getPDBResNum(k + 1);

          an[k] = new Annotation(prn);
          if (min == -1)
          {
            min = k;
            max = k;
          }
          else
          {
            if (min > k)
            {
              min = k;
            }
            else if (max < k)
            {
              max = k;
            }
          }
        }
        sq.addAlignmentAnnotation(new AlignmentAnnotation("PDB.RESNUM",
                "PDB Residue Numbering for " + this.pdbid + ":" + this.id,
                an, min, max, AlignmentAnnotation.LINE_GRAPH));
      }
    }
  }
}
