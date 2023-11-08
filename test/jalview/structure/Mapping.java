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
package jalview.structure;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileLoader;
import jalview.io.StructureFile;

public class Mapping
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /*
   * more test data
   * 
   * 1QCF|A/101-121 SFQKGDQMVVLEESGEWWKAR Ser 114 jumps to Gly 116 at position
   * 115 in PDB Res Numbering secondary structure numbers in jmol seem to be in
   * msd numbering, not pdb res numbering.
   */
  @Test(groups = { "Functional" }, enabled = false)
  public void pdbEntryPositionMap() throws Exception
  {
    Assert.fail("This test intentionally left to fail");
    for (int offset = 0; offset < 20; offset += 6)
    {
      // check we put the secondary structure in the right position
      Sequence uprot = new Sequence("TheProtSeq",
              "DAWEIPRESLKLEKKLGAGQFGEVWMATYNKHTKVAVKTMKPGSMSVEAFLAEANVMKTL");
      uprot.setStart(offset + 258); // make it harder - create a fake
                                    // relocation problem for jalview to
                                    // deal with
      uprot.setEnd(uprot.getStart() + uprot.getLength() - 1);
      // original numbers taken from
      // http://www.ebi.ac.uk/pdbe-srv/view/entry/1qcf/secondary.html
      // these are in numbering relative to the subsequence above
      int coils[] = { 266, 275, 278, 287, 289, 298, 302, 316 },
              helices[] = new int[]
              { 303, 315 }, sheets[] = new int[] { 267, 268, 269, 270 };

      StructureSelectionManager ssm = new jalview.structure.StructureSelectionManager();
      StructureFile pmap = ssm.setMapping(true, new SequenceI[] { uprot },
              new String[]
              { "A" }, "test/jalview/ext/jmol/1QCF.pdb",
              DataSourceType.FILE, null, null);
      assertTrue(pmap != null);
      SequenceI protseq = pmap.getSeqsAsArray()[0];
      AlignmentAnnotation pstra = protseq
              .getAnnotation("Secondary Structure")[0];
      int pinds, pinde;
      pstra.restrict((pinds = protseq.findIndex(258) - 1),
              pinde = (protseq.findIndex(317) - 1));
      int op;
      System.out.println("PDB Annot");
      for (char c : protseq.getSubSequence(pinds, pinde).getSequence())
      {
        System.out.print(c + ", ");
      }
      System.out.println("\n" + pstra + "\n\nsubsequence\n");
      for (char c : uprot.getSequence())
      {
        System.out.print(c + ", ");
      }
      System.out.println("");
      for (AlignmentAnnotation ss : uprot
              .getAnnotation("Secondary Structure"))
      {
        ss.adjustForAlignment();
        System.out.println("Uniprot Annot\n" + ss);
        assertTrue(ss.hasIcons);
        char expected = 'H';
        for (int p : helices)
        {
          Annotation a = ss.annotations[op = (uprot.findIndex(offset + p)
                  - 1)];
          assertTrue("Expected a helix at position " + p
                  + uprot.getCharAt(op) + " but got coil", a != null);
          assertEquals("Expected a helix at position " + p,
                  a.secondaryStructure, expected);
        }
        expected = 'E';
        for (int p : sheets)
        {
          Annotation a = ss.annotations[uprot.findIndex(offset + p) - 1];
          assertTrue("Expected a strand at position " + p + " but got coil",
                  a != null);
          assertEquals("Expected a strand at position " + p,
                  a.secondaryStructure, expected);
        }
        expected = ' ';
        for (int p : coils)
        {
          Annotation a = ss.annotations[uprot.findIndex(offset + p) - 1];
          assertTrue("Expected coil at position " + p + " but got "
                  + a.secondaryStructure, a == null);
        }
      }
    }
  }

  @Test(groups = { "Functional" }, enabled = false)
  public void testPDBentryMapping() throws Exception
  {
    Assert.fail("This test intentionally left to fail");
    Sequence sq = new Sequence("1GAQ A subseq 126 to 219",
            "EIVKGVCSNFLCDLQPGDNVQITGPVGKEMLMPKDPNATIIMLATGTGIAPFRSFLWKMFFEKHDDYKFNGLGWLFLGVPTSSSLLYKEEFGKM");
    Sequence sq1 = new Sequence(sq);
    String inFile;
    StructureSelectionManager ssm = new jalview.structure.StructureSelectionManager();
    // Associate the 1GAQ pdb file with the subsequence 'imported' from another
    // source
    StructureFile pde = ssm.setMapping(true, new SequenceI[] { sq },
            new String[]
            { "A" }, inFile = "examples/1gaq.txt", DataSourceType.FILE,
            null, null);
    assertTrue("PDB File couldn't be found", pde != null);
    StructureMapping[] mp = ssm.getMapping(inFile);
    assertTrue("No mappings made.", mp != null && mp.length > 0);
    int nsecStr = 0, nsTemp = 0;
    // test for presence of transferred annotation on sequence
    for (AlignmentAnnotation alan : sq.getAnnotation())
    {
      if (alan.hasIcons)
      {
        nsecStr++;
      }
      if (alan.graph == alan.LINE_GRAPH)
      {
        nsTemp++;
      }
    }
    assertEquals(
            "Only one secondary structure should be transferred to associated sequence.",
            1, nsecStr);
    assertEquals(
            "Only two line graphs should be transferred to associated sequence.",
            2, nsTemp);
    // Now test the transfer function and compare annotated positions
    for (StructureMapping origMap : mp)
    {
      if (origMap.getSequence() == sq)
      {
        assertEquals("Mapping was incomplete.", sq.getLength() - 1,
                (origMap.getPDBResNum(sq.getEnd())
                        - origMap.getPDBResNum(sq.getStart())));
        // sanity check - if this fails, mapping from first position in sequence
        // we want to transfer to is not where we expect
        assertEquals(1, origMap.getSeqPos(126));
        SequenceI firstChain = pde.getSeqs().get(0);
        // Compare the annotated positions on the PDB chain sequence with the
        // annotation on the associated sequence
        for (AlignmentAnnotation alan : firstChain.getAnnotation())
        {
          AlignmentAnnotation transfer = origMap.transfer(alan);
          System.out.println("pdb:" + firstChain.getSequenceAsString());
          System.out.println("ann:" + alan.toString());
          System.out.println("pdb:" + sq.getSequenceAsString());
          System.out.println("ann:" + transfer.toString());

          for (int p = 0, pSize = firstChain.getLength(); p < pSize; p++)
          {
            // walk along the pdb chain's jalview sequence
            int rseqpos;
            int fpos = origMap
                    .getSeqPos(rseqpos = firstChain.findPosition(p));
            // only look at positions where there is a corresponding position in
            // mapping
            if (fpos < 1)
            {
              continue;
            }
            // p is index into PDB residue entries
            // rseqpos is pdb sequence position for position p
            // fpos is sequence position for associated position for rseqpos
            // tanpos is the column for the mapped sequence position
            int tanpos = sq.findIndex(fpos) - 1;
            if (tanpos < 0 || transfer.annotations.length <= tanpos)
            {
              // gone beyond mapping to the sequence
              break;
            }

            Annotation a = transfer.annotations[tanpos],
                    b = alan.annotations[p];
            assertEquals(
                    "Non-equivalent annotation element at " + p + "("
                            + rseqpos + ")" + " expected at " + fpos
                            + " (alIndex " + tanpos + ")",
                    a == null ? a : a.toString(),
                    b == null ? b : b.toString());
            System.out.print("(" + a + "|" + b + ")");
          }

        }
      }
    }
  }

  /**
   * corner case for pdb mapping - revealed a problem with the AlignSeq->Mapping
   * transform
   * 
   */
  @Test(groups = { "Functional" })
  public void mapFer1From3W5V() throws Exception
  {
    AlignFrame seqf = new FileLoader(false).LoadFileWaitTillLoaded(
            ">FER1_MAIZE/1-150 Ferredoxin-1, chloroplast precursor\nMATVLGSPRAPAFFFSSSSLRAAPAPTAVALPAAKVGIMGRSASSRRRLRAQATYNVKLITPEGEVELQVPD\nDVYILDQAEEDGIDLPYSCRAGSCSSCAGKVVSGSVDQSDQSYLDDGQIADGWVLTCHAYPTSDVVIETHKE\nEELTGA",
            DataSourceType.PASTE, FileFormat.Fasta);
    SequenceI newseq = seqf.getViewport().getAlignment().getSequenceAt(0);
    StructureSelectionManager ssm = new jalview.structure.StructureSelectionManager();
    StructureFile pmap = ssm.setMapping(true, new SequenceI[] { newseq },
            new String[]
            { null }, "examples/3W5V.pdb", DataSourceType.FILE, null, null);
    if (pmap == null)
    {
      AssertJUnit.fail("Couldn't make a mapping for 3W5V to FER1_MAIZE");
    }
  }

  /**
   * compare reference annotation for imported pdb sequence to identical
   * seuqence with transferred annotation from mapped pdb file
   */
  @Test(groups = { "Functional" })
  public void compareTransferredToRefPDBAnnot() throws Exception
  {
    StructureImportSettings.setProcessSecondaryStructure(true);
    StructureImportSettings.setVisibleChainAnnotation(true);
    StructureImportSettings.setShowSeqFeatures(true);
    AlignFrame ref = new FileLoader(false).LoadFileWaitTillLoaded(
            "test/jalview/ext/jmol/1QCF.pdb", DataSourceType.FILE);
    SequenceI refseq = ref.getViewport().getAlignment().getSequenceAt(0);
    SequenceI newseq = new Sequence(refseq.getName() + "Copy",
            refseq.getSequenceAsString());
    // make it harder by shifting the copy vs the reference
    newseq.setStart(refseq.getStart() + 25);
    newseq.setEnd(refseq.getLength() + 25 + refseq.getStart());
    StructureSelectionManager ssm = new jalview.structure.StructureSelectionManager();
    ssm.setProcessSecondaryStructure(true);
    ssm.setAddTempFacAnnot(true);
    StructureFile pmap = ssm.setMapping(true, new SequenceI[] { newseq },
            new String[]
            { null }, "test/jalview/ext/jmol/1QCF.pdb", DataSourceType.FILE,
            null, null);
    assertTrue(pmap != null);
    assertEquals("Original and copied sequence of different lengths.",
            refseq.getLength(), newseq.getLength());
    assertTrue(refseq.getAnnotation() != null
            && refseq.getAnnotation().length > 0);
    assertTrue(newseq.getAnnotation() != null
            && newseq.getAnnotation().length > 0);
    for (AlignmentAnnotation oannot : refseq.getAnnotation())
    {
      for (AlignmentAnnotation tannot : newseq.getAnnotation(oannot.label))
      {
        for (int p = 0, pSize = refseq.getLength(); p < pSize; p++)
        {
          Annotation orig = oannot.annotations[p],
                  tran = tannot.annotations[p];
          assertTrue("Mismatch: coil and non coil site " + p,
                  orig == tran || orig != null && tran != null);
          if (tran != null)
          {
            assertEquals("Mismatch in secondary structure at site " + p,
                    tran.secondaryStructure, orig.secondaryStructure);
          }
        }
      }
    }
  }
}
