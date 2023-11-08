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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import jalview.analysis.AlignSeq;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.TaylorColourScheme;
import jalview.structure.StructureImportSettings;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PDBChainTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  PDBChain c;

  final Atom a1 = new Atom(1f, 2f, 3f);

  final Atom a2 = new Atom(5f, 6f, 4f);

  final Atom a3 = new Atom(2f, 5f, 6f);

  final Atom a4 = new Atom(2f, 1f, 7f);

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    System.out.println("setup");
    StructureImportSettings.setShowSeqFeatures(true);
    c = new PDBChain("1GAQ", "A");
  }

  @Test(groups = { "Functional" })
  public void testGetNewlineString()
  {
    assertEquals(System.lineSeparator(), c.getNewlineString());
    c.setNewlineString("gaga");
    assertEquals("gaga", c.getNewlineString());
  }

  @Test(groups = { "Functional" })
  public void testPrint()
  {
    c.offset = 7;

    a1.resName = "GLY";
    a1.resNumber = 23;
    a2.resName = "GLU";
    a2.resNumber = 34;
    a3.resName = "ASP";
    a3.resNumber = 41;

    Vector<Bond> v = new Vector<>();
    v.add(new Bond(a1, a2));
    v.add(new Bond(a2, a3));
    v.add(new Bond(a3, a1));
    c.bonds = v;

    String printed = c.print();
    String nl = System.lineSeparator();
    assertEquals("GLY 23 7" + nl + "GLU 34 7" + nl + "ASP 41 7" + nl,
            printed);
  }

  /**
   * Test the method that constructs a Bond between two atoms and adds it to the
   * chain's list of bonds
   */
  @Test(groups = { "Functional" })
  public void testMakeBond()
  {
    /*
     * Add a bond from a1 to a2
     */
    c.makeBond(a1, a2);
    assertEquals(1, c.bonds.size());
    Bond b1 = c.bonds.get(0);
    assertSame(a1, b1.at1);
    assertSame(a2, b1.at2);
    assertEquals(1f, b1.start[0], 0.0001f);
    assertEquals(2f, b1.start[1], 0.0001f);
    assertEquals(3f, b1.start[2], 0.0001f);
    assertEquals(5f, b1.end[0], 0.0001f);
    assertEquals(6f, b1.end[1], 0.0001f);
    assertEquals(4f, b1.end[2], 0.0001f);

    /*
     * Add another bond from a2 to a1
     */
    c.makeBond(a2, a1);
    assertEquals(2, c.bonds.size());
    assertSame(b1, c.bonds.get(0));
    Bond b2 = c.bonds.get(1);
    assertSame(a2, b2.at1);
    assertSame(a1, b2.at2);
    assertEquals(5f, b2.start[0], 0.0001f);
    assertEquals(6f, b2.start[1], 0.0001f);
    assertEquals(4f, b2.start[2], 0.0001f);
    assertEquals(1f, b2.end[0], 0.0001f);
    assertEquals(2f, b2.end[1], 0.0001f);
    assertEquals(3f, b2.end[2], 0.0001f);
  }

  @Test(groups = { "Functional" })
  public void testSetChainColours_colour()
  {
    c.makeBond(a1, a2);
    c.makeBond(a2, a3);
    c.setChainColours(Color.PINK);
    assertEquals(2, c.bonds.size());
    assertEquals(Color.PINK, c.bonds.get(0).startCol);
    assertEquals(Color.PINK, c.bonds.get(0).endCol);
    assertEquals(Color.PINK, c.bonds.get(1).startCol);
    assertEquals(Color.PINK, c.bonds.get(1).endCol);
  }

  /**
   * Test setting bond start/end colours based on a colour scheme i.e. colour by
   * residue
   */
  @Test(groups = { "Functional" })
  public void testSetChainColours_colourScheme()
  {
    Color alaColour = new Color(204, 255, 0);
    Color glyColour = new Color(255, 153, 0);
    a1.resName = "ALA";
    a2.resName = "GLY";
    a3.resName = "XXX"; // no colour defined
    c.makeBond(a1, a2);
    c.makeBond(a2, a1);
    c.makeBond(a2, a3);
    ColourSchemeI cs = new TaylorColourScheme();
    c.setChainColours(cs);
    // bond a1 to a2
    Bond b = c.bonds.get(0);
    assertEquals(alaColour, b.startCol);
    assertEquals(glyColour, b.endCol);
    // bond a2 to a1
    b = c.bonds.get(1);
    assertEquals(glyColour, b.startCol);
    assertEquals(alaColour, b.endCol);
    // bond a2 to a3 - no colour found for a3
    // exception handling defaults to gray
    b = c.bonds.get(2);
    assertEquals(Color.gray, b.startCol);
    assertEquals(Color.gray, b.endCol);
  }

  @Test(groups = { "Functional" })
  public void testGetChargeColour()
  {
    assertEquals(Color.red, PDBChain.getChargeColour("ASP"));
    assertEquals(Color.red, PDBChain.getChargeColour("GLU"));
    assertEquals(Color.blue, PDBChain.getChargeColour("LYS"));
    assertEquals(Color.blue, PDBChain.getChargeColour("ARG"));
    assertEquals(Color.yellow, PDBChain.getChargeColour("CYS"));
    assertEquals(Color.lightGray, PDBChain.getChargeColour("ALA"));
    assertEquals(Color.lightGray, PDBChain.getChargeColour(null));
  }

  /**
   * Test the method that sets bond start/end colours by residue charge property
   */
  @Test(groups = { "Functional" })
  public void testSetChargeColours()
  {
    a1.resName = "ASP"; // red
    a2.resName = "LYS"; // blue
    a3.resName = "CYS"; // yellow
    a4.resName = "ALA"; // no colour (light gray)
    c.makeBond(a1, a2);
    c.makeBond(a2, a3);
    c.makeBond(a3, a4);
    c.setChargeColours();
    assertEquals(3, c.bonds.size());
    // bond a1 to a2
    Bond b = c.bonds.get(0);
    assertEquals(Color.red, b.startCol);
    assertEquals(Color.blue, b.endCol);
    // bond a2 to a3
    b = c.bonds.get(1);
    assertEquals(Color.blue, b.startCol);
    assertEquals(Color.yellow, b.endCol);
    // bond a3 to a4
    b = c.bonds.get(2);
    assertEquals(Color.yellow, b.startCol);
    assertEquals(Color.lightGray, b.endCol);
  }

  /**
   * Test the method that converts the raw list of atoms to a list of residues
   */
  @Test(groups = { "Functional" })
  public void testMakeResidueList_noAnnotation()
  {
    Vector<Atom> atoms = new Vector<>();
    c.atoms = atoms;
    c.isNa = true;
    atoms.add(makeAtom(4, "N", "MET"));
    atoms.add(makeAtom(4, "CA", "MET"));
    atoms.add(makeAtom(4, "C", "MET"));
    atoms.add(makeAtom(5, "O", "LYS"));
    atoms.add(makeAtom(5, "N", "LYS"));
    atoms.add(makeAtom(5, "CA", "LYS"));
    atoms.add(makeAtom(6, "O", "LEU"));
    atoms.add(makeAtom(6, "N", "LEU"));
    atoms.add(makeAtom(6, "CA", "LEU"));

    c.makeResidueList(false);

    /*
     * check sequence constructed
     */
    assertEquals("MKL", c.sequence.getSequenceAsString());
    assertFalse(c.isNa);
    assertEquals(3, c.residues.size());

    /*
     * check sequence features
     */
    List<SequenceFeature> sfs = c.sequence.getSequenceFeatures();
    assertEquals(3, sfs.size());
    assertEquals("RESNUM", sfs.get(0).type);
    assertEquals("MET:4 1gaqA", sfs.get(0).description);
    assertEquals(4, sfs.get(0).begin);
    assertEquals(4, sfs.get(0).end);
    assertEquals("RESNUM", sfs.get(0).type);
    assertEquals("LYS:5 1gaqA", sfs.get(1).description);
    assertEquals(5, sfs.get(1).begin);
    assertEquals(5, sfs.get(1).end);
    assertEquals("LEU:6 1gaqA", sfs.get(2).description);
    assertEquals(6, sfs.get(2).begin);
    assertEquals(6, sfs.get(2).end);
  }

  private Atom makeAtom(int resnum, String name, String resname)
  {
    Atom a = new Atom(1f, 2f, 3f);
    a.resNumber = resnum;
    a.resNumIns = String.valueOf(resnum);
    a.name = name;
    a.resName = resname;
    a.chain = "A";
    return a;
  }

  /**
   * Test the method that converts the raw list of atoms to a list of residues,
   * including parsing of tempFactor to an alignment annotation
   */
  @Test(groups = { "Functional" })
  public void testMakeResidueList_withTempFactor()
  {
    Vector<Atom> atoms = new Vector<>();
    c.atoms = atoms;
    atoms.add(makeAtom(4, "N", "MET"));
    atoms.get(atoms.size() - 1).tfactor = 1f;
    atoms.add(makeAtom(4, "CA", "MET"));
    atoms.get(atoms.size() - 1).tfactor = 2f;
    atoms.add(makeAtom(4, "C", "MET"));
    atoms.get(atoms.size() - 1).tfactor = 3f;
    atoms.add(makeAtom(5, "O", "LYS"));
    atoms.get(atoms.size() - 1).tfactor = 7f;
    atoms.add(makeAtom(5, "N", "LYS"));
    atoms.get(atoms.size() - 1).tfactor = 8f;
    atoms.add(makeAtom(5, "CA", "LYS"));
    atoms.get(atoms.size() - 1).tfactor = 9f;
    atoms.add(makeAtom(6, "O", "LEU"));
    atoms.get(atoms.size() - 1).tfactor = -4f;
    atoms.add(makeAtom(6, "N", "LEU"));
    atoms.get(atoms.size() - 1).tfactor = 5f;
    atoms.add(makeAtom(6, "CA", "LEU"));
    atoms.get(atoms.size() - 1).tfactor = 6f;

    /*
     * make residues including temp factor annotation
     */
    c.makeResidueList(true);

    /*
     * Verify annotations; note the tempFactor is read from the first atom in
     * each residue i.e. we expect values 1, 7, -4 for the residues
     */
    AlignmentAnnotation[] ann = c.sequence.getAnnotation();
    assertEquals(1, ann.length);
    assertEquals("Temperature Factor", ann[0].label);
    assertEquals("Temperature Factor for 1gaqA", ann[0].description);
    assertSame(c.sequence, ann[0].sequenceRef);
    assertEquals(AlignmentAnnotation.LINE_GRAPH, ann[0].graph);
    assertEquals(-4f, ann[0].graphMin, 0.001f);
    assertEquals(7f, ann[0].graphMax, 0.001f);
    assertEquals(3, ann[0].annotations.length);
    assertEquals(1f, ann[0].annotations[0].value, 0.001f);
    assertEquals(7f, ann[0].annotations[1].value, 0.001f);
    assertEquals(-4f, ann[0].annotations[2].value, 0.001f);
  }

  /**
   * Test the method that constructs bonds between successive residues' CA or P
   * atoms
   */
  @Test(groups = { "Functional" })
  public void testMakeCaBondList()
  {
    c.isNa = true;
    Vector<Atom> atoms = new Vector<>();
    c.atoms = atoms;
    atoms.add(makeAtom(4, "N", "MET"));
    atoms.add(makeAtom(4, "CA", "MET"));
    atoms.add(makeAtom(5, "CA", "ASP"));
    atoms.add(makeAtom(5, "O", "ASP"));
    atoms.add(makeAtom(6, "CA", "GLY"));
    atoms.add(makeAtom(6, "N", "GLY"));

    // have to make residue list first!
    c.makeResidueList(false);
    assertFalse(c.isNa);
    c.isNa = true;

    c.makeCaBondList();
    assertEquals(2, c.bonds.size());
    Bond b = c.bonds.get(0);
    assertSame(c.atoms.get(1), b.at1);
    assertSame(c.atoms.get(2), b.at2);
    b = c.bonds.get(1);
    assertSame(c.atoms.get(2), b.at1);
    assertSame(c.atoms.get(4), b.at2);

    // isNa flag is _not_ reset by this method!
    assertTrue(c.isNa);
  }

  @Test(groups = { "Functional" })
  public void testMakeCaBondList_nucleotide()
  {
    c.isNa = false;
    Vector<Atom> atoms = new Vector<>();
    c.atoms = atoms;
    atoms.add(makeAtom(4, "N", "G"));
    atoms.add(makeAtom(4, "P", "G"));
    atoms.add(makeAtom(5, "P", "C"));
    atoms.add(makeAtom(5, "O", "C"));
    atoms.add(makeAtom(6, "P", "T"));
    atoms.add(makeAtom(6, "N", "T"));

    // have to make residue list first!
    c.makeResidueList(false);
    assertEquals("GCT", c.sequence.getSequenceAsString());

    c.makeCaBondList();
    assertEquals(2, c.bonds.size());
    Bond b = c.bonds.get(0);
    assertSame(c.atoms.get(1), b.at1);
    assertSame(c.atoms.get(2), b.at2);
    b = c.bonds.get(1);
    assertSame(c.atoms.get(2), b.at1);
    assertSame(c.atoms.get(4), b.at2);

    assertTrue(c.isNa);
  }

  /**
   * Test the method that updates atoms with their alignment positions
   */
  @Test(groups = { "Functional" })
  public void testMakeExactMapping()
  {
    Vector<Atom> atoms = new Vector<>();
    c.atoms = atoms;
    atoms.add(makeAtom(4, "N", "MET"));
    atoms.add(makeAtom(4, "CA", "MET"));
    atoms.add(makeAtom(5, "CA", "ASP"));
    atoms.add(makeAtom(5, "O", "ASP"));
    atoms.add(makeAtom(6, "CA", "GLY"));
    atoms.add(makeAtom(6, "N", "GLY"));
    c.makeResidueList(false);
    assertEquals("MDG", c.sequence.getSequenceAsString());
    SequenceI s1 = new Sequence("Seq1", "MDG");
    SequenceI s2 = new Sequence("Seq2", "MDG");
    AlignSeq alignSeq = AlignSeq.doGlobalNWAlignment(s1, s2, AlignSeq.PEP);
    SequenceI seq3 = new Sequence("Seq3", "--M-DG");
    c.makeExactMapping(alignSeq, seq3);

    int pos = 0;
    for (Residue res : c.residues)
    {
      for (Atom a : res.atoms)
      {
        assertEquals(pos, a.alignmentMapping);
      }
      pos++;
    }
  }
}
