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
package jalview.ext.jmol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.SequenceRenderer;
import jalview.schemes.JalviewColourScheme;
import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsI.AtomSpecType;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;

public class JmolCommandsTest
{
  private JmolCommands testee;

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    testee = new JmolCommands();
  }

  @Test(groups = { "Functional" })
  public void testGetColourBySequenceCommands_hiddenColumns()
  {
    /*
     * load these sequences, coloured by Strand propensity,
     * with columns 2-4 hidden
     */
    SequenceI seq1 = new Sequence("seq1", "MHRSQSSSGG");
    SequenceI seq2 = new Sequence("seq2", "MVRSNGGSSS");
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });
    AlignFrame af = new AlignFrame(al, 800, 500);
    af.changeColour_actionPerformed(JalviewColourScheme.Strand.toString());
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(2);
    cs.addElement(3);
    cs.addElement(4);
    af.getViewport().setColumnSelection(cs);
    af.hideSelColumns_actionPerformed(null);
    SequenceRenderer sr = new SequenceRenderer(af.getViewport());
    SequenceI[][] seqs = new SequenceI[][] { { seq1 }, { seq2 } };
    String[] files = new String[] { "seq1.pdb", "seq2.pdb" };
    StructureSelectionManager ssm = new StructureSelectionManager();

    /*
     * map residues 1-10 to residues 21-30 (atoms 105-150) in structures
     */
    HashMap<Integer, int[]> map = new HashMap<>();
    for (int pos = 1; pos <= seq1.getLength(); pos++)
    {
      map.put(pos, new int[] { 20 + pos, 5 * (20 + pos) });
    }
    StructureMapping sm1 = new StructureMapping(seq1, "seq1.pdb", "pdb1",
            "A", map, null);
    ssm.addStructureMapping(sm1);
    StructureMapping sm2 = new StructureMapping(seq2, "seq2.pdb", "pdb2",
            "B", map, null);
    ssm.addStructureMapping(sm2);

    String[] commands = testee.colourBySequence(ssm, files, seqs, sr,
            af.alignPanel);
    assertEquals(commands.length, 2);

    String chainACommand = commands[0];
    // M colour is #82827d == (130, 130, 125) (see strand.html help page)
    assertTrue(
            chainACommand.contains("select 21:A/1.1;color[130,130,125]")); // first
                                                                           // one
    // H colour is #60609f == (96, 96, 159)
    assertTrue(chainACommand.contains(";select 22:A/1.1;color[96,96,159]"));
    // hidden columns are Gray (128, 128, 128)
    assertTrue(chainACommand
            .contains(";select 23-25:A/1.1;color[128,128,128]"));
    // S and G are both coloured #4949b6 == (73, 73, 182)
    assertTrue(
            chainACommand.contains(";select 26-30:A/1.1;color[73,73,182]"));

    String chainBCommand = commands[1];
    // M colour is #82827d == (130, 130, 125)
    assertTrue(
            chainBCommand.contains("select 21:B/2.1;color[130,130,125]"));
    // V colour is #ffff00 == (255, 255, 0)
    assertTrue(chainBCommand.contains(";select 22:B/2.1;color[255,255,0]"));
    // hidden columns are Gray (128, 128, 128)
    assertTrue(chainBCommand
            .contains(";select 23-25:B/2.1;color[128,128,128]"));
    // S and G are both coloured #4949b6 == (73, 73, 182)
    assertTrue(
            chainBCommand.contains(";select 26-30:B/2.1;color[73,73,182]"));
  }

  @Test(groups = "Functional")
  public void testGetAtomSpec()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY), "");
    model.addRange("1", 2, 4, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-4:A/1.1");
    model.addRange("1", 8, 8, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-4:A/1.1|8:A/1.1");
    model.addRange("1", 5, 7, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-4:A/1.1|8:A/1.1|5-7:B/1.1");
    model.addRange("1", 3, 5, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-5:A/1.1|8:A/1.1|5-7:B/1.1");
    model.addRange("2", 1, 4, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-5:A/1.1|8:A/1.1|5-7:B/1.1|1-4:B/2.1");
    model.addRange("2", 5, 9, "C");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-5:A/1.1|8:A/1.1|5-7:B/1.1|1-4:B/2.1|5-9:C/2.1");
    model.addRange("1", 8, 10, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-5:A/1.1|8:A/1.1|5-10:B/1.1|1-4:B/2.1|5-9:C/2.1");
    model.addRange("1", 8, 9, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-5:A/1.1|8:A/1.1|5-10:B/1.1|1-4:B/2.1|5-9:C/2.1");
    model.addRange("2", 3, 10, "C"); // subsumes 5-9
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-5:A/1.1|8:A/1.1|5-10:B/1.1|1-4:B/2.1|3-10:C/2.1");
    model.addRange("5", 25, 35, " ");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "2-5:A/1.1|8:A/1.1|5-10:B/1.1|1-4:B/2.1|3-10:C/2.1|25-35:/5.1");

  }

  @Test(groups = { "Functional" })
  public void testColourBySequence()
  {
    Map<Object, AtomSpecModel> map = new LinkedHashMap<>();
    JmolCommands.addAtomSpecRange(map, Color.blue, "1", 2, 5, "A");
    JmolCommands.addAtomSpecRange(map, Color.blue, "1", 7, 7, "B");
    JmolCommands.addAtomSpecRange(map, Color.blue, "1", 9, 23, "A");
    JmolCommands.addAtomSpecRange(map, Color.blue, "2", 1, 1, "A");
    JmolCommands.addAtomSpecRange(map, Color.blue, "2", 4, 7, "B");
    JmolCommands.addAtomSpecRange(map, Color.yellow, "2", 8, 8, "A");
    JmolCommands.addAtomSpecRange(map, Color.yellow, "2", 3, 5, "A");
    JmolCommands.addAtomSpecRange(map, Color.red, "1", 3, 5, "A");
    JmolCommands.addAtomSpecRange(map, Color.red, "1", 6, 9, "A");

    // Colours should appear in the Jmol command in the order in which
    // they were added; within colour, by model, by chain, ranges in start order
    List<StructureCommandI> commands = testee.colourBySequence(map);
    assertEquals(commands.size(), 1);
    String expected1 = "select 2-5:A/1.1|9-23:A/1.1|7:B/1.1|1:A/2.1|4-7:B/2.1;color[0,0,255]";
    String expected2 = "select 3-5:A/2.1|8:A/2.1;color[255,255,0]";
    String expected3 = "select 3-9:A/1.1;color[255,0,0]";
    assertEquals(commands.get(0).getCommand(),
            expected1 + ";" + expected2 + ";" + expected3);
  }

  @Test(groups = { "Functional" })
  public void testSuperposeStructures()
  {
    AtomSpecModel ref = new AtomSpecModel();
    ref.addRange("1", 12, 14, "A");
    ref.addRange("1", 18, 18, "B");
    ref.addRange("1", 22, 23, "B");
    AtomSpecModel toAlign = new AtomSpecModel();
    toAlign.addRange("2", 15, 17, "B");
    toAlign.addRange("2", 20, 21, "B");
    toAlign.addRange("2", 22, 22, "C");
    List<StructureCommandI> command = testee.superposeStructures(ref,
            toAlign, AtomSpecType.ALPHA); // doesn't matter for Jmol whether nuc
                                          // or protein
    assertEquals(command.size(), 1);
    String refSpec = "12-14:A/1.1|18:B/1.1|22-23:B/1.1";
    String toAlignSpec = "15-17:B/2.1|20-21:B/2.1|22:C/2.1";
    String expected = String.format(
            "compare {2.1} {1.1} SUBSET {(*.CA | *.P) and conformation=1} ATOMS {%s}{%s} ROTATE TRANSLATE ;select %s|%s;cartoons",
            toAlignSpec, refSpec, toAlignSpec, refSpec);
    assertEquals(command.get(0).getCommand(), expected);
  }

  @Test(groups = "Functional")
  public void testGetModelStartNo()
  {
    assertEquals(testee.getModelStartNo(), 1);
  }

  @Test(groups = "Functional")
  public void testColourByChain()
  {
    StructureCommandI cmd = testee.colourByChain();
    assertEquals(cmd.getCommand(), "select *;color chain");
  }

  @Test(groups = "Functional")
  public void testColourByCharge()
  {
    List<StructureCommandI> cmds = testee.colourByCharge();
    assertEquals(cmds.size(), 1);
    assertEquals(cmds.get(0).getCommand(),
            "select *;color white;select ASP,GLU;color red;"
                    + "select LYS,ARG;color blue;select CYS;color yellow");
  }

  @Test(groups = "Functional")
  public void testSetBackgroundColour()
  {
    StructureCommandI cmd = testee.setBackgroundColour(Color.PINK);
    assertEquals(cmd.getCommand(), "background [255,175,175]");
  }

  @Test(groups = "Functional")
  public void testFocusView()
  {
    StructureCommandI cmd = testee.focusView();
    assertEquals(cmd.getCommand(), "zoom 0");
  }

  @Test(groups = "Functional")
  public void testSaveSession()
  {
    StructureCommandI cmd = testee.saveSession("/some/filepath");
    assertEquals(cmd.getCommand(), "write STATE \"/some/filepath\"");
  }

  @Test(groups = "Functional")
  public void testShowBackbone()
  {
    List<StructureCommandI> cmds = testee.showBackbone();
    assertEquals(cmds.size(), 1);
    assertEquals(cmds.get(0).getCommand(),
            "select *; cartoons off; backbone");
  }

  @Test(groups = "Functional")
  public void testLoadFile()
  {
    StructureCommandI cmd = testee.loadFile("/some/filepath");
    assertEquals(cmd.getCommand(), "load FILES \"/some/filepath\"");

    // single backslash gets escaped to double
    cmd = testee.loadFile("\\some\\filepath");
    assertEquals(cmd.getCommand(), "load FILES \"\\\\some\\\\filepath\"");
  }

  @Test(groups = "Functional")
  public void testOpenSession()
  {
    StructureCommandI cmd = testee.openSession("/some/filepath");
    assertEquals(cmd.getCommand(), "load FILES \"/some/filepath\"");

    // single backslash gets escaped to double
    cmd = testee.openSession("\\some\\filepath");
    assertEquals(cmd.getCommand(), "load FILES \"\\\\some\\\\filepath\"");
  }
}
