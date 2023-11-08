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
package jalview.ext.pymol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.ext.rbvi.chimera.ChimeraCommands;
import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsI.AtomSpecType;

public class PymolCommandsTest
{
  private PymolCommands testee;

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    testee = new PymolCommands();
  }

  @Test(groups = { "Functional" })
  public void testColourBySequence()
  {

    Map<Object, AtomSpecModel> map = new LinkedHashMap<>();
    PymolCommands.addAtomSpecRange(map, Color.blue, "0", 2, 5, "A");
    PymolCommands.addAtomSpecRange(map, Color.blue, "0", 7, 7, "B");
    PymolCommands.addAtomSpecRange(map, Color.blue, "0", 9, 23, "A");
    PymolCommands.addAtomSpecRange(map, Color.blue, "1", 1, 1, "A");
    PymolCommands.addAtomSpecRange(map, Color.blue, "1", 4, 7, "B");
    PymolCommands.addAtomSpecRange(map, Color.yellow, "1", 8, 8, "A");
    PymolCommands.addAtomSpecRange(map, Color.yellow, "1", 3, 5, "A");
    PymolCommands.addAtomSpecRange(map, Color.red, "0", 3, 5, "A");
    PymolCommands.addAtomSpecRange(map, Color.red, "0", 6, 9, "A");

    // Colours should appear in the Pymol command in the order in which
    // they were added; within colour, by model, by chain, ranges in start order
    List<StructureCommandI> commands = testee.colourBySequence(map);
    assertEquals(commands.size(), 3);
    assertEquals(commands.get(0), new StructureCommand("color", "0x0000ff",
            "0//A/2-5+9-23/ 0//B/7/ 1//A/1/ 1//B/4-7/"));
    assertEquals(commands.get(1),
            new StructureCommand("color", "0xffff00", "1//A/3-5+8/"));
    assertEquals(commands.get(2),
            new StructureCommand("color", "0xff0000", "0//A/3-9/"));
  }

  @Test(groups = "Functional")
  public void testGetAtomSpec()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY), "");
    model.addRange("1", 2, 4, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "1//A/2-4/");
    model.addRange("1", 8, 8, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "1//A/2-4+8/");
    model.addRange("1", 5, 7, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "1//A/2-4+8/ 1//B/5-7/");
    model.addRange("1", 3, 5, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "1//A/2-5+8/ 1//B/5-7/");
    model.addRange("0", 1, 4, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "0//B/1-4/ 1//A/2-5+8/ 1//B/5-7/");
    model.addRange("0", 5, 9, "C");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "0//B/1-4/ 0//C/5-9/ 1//A/2-5+8/ 1//B/5-7/");
    model.addRange("1", 8, 10, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "0//B/1-4/ 0//C/5-9/ 1//A/2-5+8/ 1//B/5-10/");
    model.addRange("1", 8, 9, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "0//B/1-4/ 0//C/5-9/ 1//A/2-5+8/ 1//B/5-10/");
    model.addRange("0", 3, 10, "C"); // subsumes 5-9
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "0//B/1-4/ 0//C/3-10/ 1//A/2-5+8/ 1//B/5-10/");
    model.addRange("5", 25, 35, " ");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "0//B/1-4/ 0//C/3-10/ 1//A/2-5+8/ 1//B/5-10/ 5///25-35/");

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
    List<StructureCommandI> commands = testee.superposeStructures(ref,
            toAlign, AtomSpecType.ALPHA);
    assertEquals(commands.size(), 4);
    String refSpecCA = "(1//A/12-14/CA 1//B/18+22-23/CA";
    String toAlignSpecCA = "(2//B/15-17+20-21/CA 2//C/22/CA";
    String refSpec = "1//A/12-14/ 1//B/18+22-23/";
    String toAlignSpec = "2//B/15-17+20-21/ 2//C/22/";
    String altLoc = " and (altloc '' or altloc 'a'))";
    // super command: separate arguments for regions to align
    assertEquals(commands.get(1), new StructureCommand("pair_fit",
            toAlignSpecCA + altLoc, refSpecCA + altLoc));
    // show aligned regions: one argument for combined atom specs
    assertEquals(commands.get(3), new StructureCommand("show", "cartoon",
            refSpec + " " + toAlignSpec));
  }

  @Test(groups = "Functional")
  public void testGetAtomSpec_alphaOnly()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA), "");
    model.addRange("1", 2, 4, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "1//A/2-4/CA");
    model.addRange("1", 8, 8, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "1//A/2-4+8/CA");
    model.addRange("1", 5, 7, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "1//A/2-4+8/CA 1//B/5-7/CA");
    model.addRange("1", 3, 5, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "1//A/2-5+8/CA 1//B/5-7/CA");
    model.addRange("0", 1, 4, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "0//B/1-4/CA 1//A/2-5+8/CA 1//B/5-7/CA");
    model.addRange("0", 5, 9, "C");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "0//B/1-4/CA 0//C/5-9/CA 1//A/2-5+8/CA 1//B/5-7/CA");
    model.addRange("1", 8, 10, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "0//B/1-4/CA 0//C/5-9/CA 1//A/2-5+8/CA 1//B/5-10/CA");
    model.addRange("1", 8, 9, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "0//B/1-4/CA 0//C/5-9/CA 1//A/2-5+8/CA 1//B/5-10/CA");
    model.addRange("0", 3, 10, "C"); // subsumes 5-9
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "0//B/1-4/CA 0//C/3-10/CA 1//A/2-5+8/CA 1//B/5-10/CA");
    model.addRange("5", 25, 35, " ");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "0//B/1-4/CA 0//C/3-10/CA 1//A/2-5+8/CA 1//B/5-10/CA 5///25-35/CA");
  }

  @Test(groups = "Functional")
  public void testGetModelStartNo()
  {
    assertEquals(testee.getModelStartNo(), 0);
  }

  @Test(groups = "Functional")
  public void testGetResidueSpec()
  {
    assertEquals(testee.getResidueSpec("ALA"), "resn ALA");
  }

  @Test(groups = "Functional")
  public void testShowBackbone()
  {
    List<StructureCommandI> cmds = testee.showBackbone();
    assertEquals(cmds.size(), 2);
    assertEquals(cmds.get(0), new StructureCommand("hide", "everything"));
    assertEquals(cmds.get(1), new StructureCommand("show", "ribbon"));
  }

  @Test(groups = "Functional")
  public void testColourByCharge()
  {
    List<StructureCommandI> cmds = testee.colourByCharge();
    assertEquals(cmds.size(), 4);
    assertEquals(cmds.get(0), new StructureCommand("color", "white", "*"));
    assertEquals(cmds.get(1),
            new StructureCommand("color", "red", "resn ASP resn GLU"));
    assertEquals(cmds.get(2),
            new StructureCommand("color", "blue", "resn LYS resn ARG"));
    assertEquals(cmds.get(3),
            new StructureCommand("color", "yellow", "resn CYS"));
  }

  @Test(groups = "Functional")
  public void testOpenCommandFile()
  {
    assertEquals(testee.openCommandFile("commands.pml"),
            new StructureCommand("run", "commands.pml"));
  }

  @Test(groups = "Functional")
  public void testSaveSession()
  {
    assertEquals(testee.saveSession("somewhere.pse"),
            new StructureCommand("save", "somewhere.pse"));
  }

  @Test(groups = "Functional")
  public void testOpenSession()
  {
    assertEquals(testee.openSession("/some/path"),
            new StructureCommand("load", "/some/path", "", "0", "pse"));
  }

  @Test(groups = "Functional")
  public void testColourByChain()
  {
    assertEquals(testee.colourByChain(),
            new StructureCommand("spectrum", "chain"));
  }

  @Test(groups = "Functional")
  public void testColourResidues()
  {
    assertEquals(testee.colourResidues("something", Color.MAGENTA),
            new StructureCommand("color", "0xff00ff", "something"));
  }

  @Test(groups = "Functional")
  public void testLoadFile()
  {
    assertEquals(testee.loadFile("/some/path"),
            new StructureCommand("load", "/some/path"));
  }

  @Test(groups = "Functional")
  public void testSetBackgroundColour()
  {
    assertEquals(testee.setBackgroundColour(Color.PINK),
            new StructureCommand("bg_color", "0xffafaf"));
  }

  @Test(groups = "Functional")
  public void testSetAttribute()
  {
    AtomSpecModel model = new AtomSpecModel();
    model.addRange("1", 89, 92, "A");
    model.addRange("2", 12, 20, "B");
    model.addRange("2", 8, 9, "B");
    assertEquals(testee.setAttribute("jv_kd", "27.3", model),
            new StructureCommand("iterate", "1//A/89-92/ 2//B/8-9+12-20/",
                    "p.jv_kd='27.3'"));
  }

  @Test(groups = { "Functional" })
  public void testSetAttributes()
  {
    /*
     * make a map of { featureType, {featureValue, {residue range specification } } }
     */
    Map<String, Map<Object, AtomSpecModel>> featuresMap = new LinkedHashMap<>();
    Map<Object, AtomSpecModel> featureValues = new HashMap<>();

    /*
     * start with just one feature/value...
     */
    featuresMap.put("chain", featureValues);
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 8, 20, "A");

    List<StructureCommandI> commands = testee.setAttributes(featuresMap);
    assertEquals(commands.size(), 1);

    /*
     * feature name gets a jv_ namespace prefix
     */
    assertEquals(commands.get(0), new StructureCommand("iterate",
            "0//A/8-20/", "p.jv_chain='X'"));

    // add same feature value, overlapping range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 3, 9, "A");
    // same feature value, contiguous range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 21, 25, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(commands.size(), 1);
    assertEquals(commands.get(0), new StructureCommand("iterate",
            "0//A/3-25/", "p.jv_chain='X'"));

    // same feature value and model, different chain
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 21, 25, "B");
    // same feature value and chain, different model
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "1", 26, 30, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(commands.size(), 1);
    StructureCommand expected1 = new StructureCommand("iterate",
            "0//A/3-25/ 0//B/21-25/ 1//A/26-30/", "p.jv_chain='X'");
    assertEquals(commands.get(0), expected1);

    // same feature, different value
    ChimeraCommands.addAtomSpecRange(featureValues, "Y", "0", 40, 50, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(2, commands.size());
    // commands are ordered by feature type but not by value
    // so test for the expected command in either order
    StructureCommandI cmd1 = commands.get(0);
    StructureCommandI cmd2 = commands.get(1);
    StructureCommand expected2 = new StructureCommand("iterate",
            "0//A/40-50/", "p.jv_chain='Y'");
    assertTrue(cmd1.equals(expected1) || cmd2.equals(expected1));
    // String expected2 = "setattr #0/A:40-50 res jv_chain 'Y' create true";
    assertTrue(cmd1.equals(expected2) || cmd2.equals(expected2));

    featuresMap.clear();
    featureValues.clear();
    featuresMap.put("side-chain binding!", featureValues);
    ChimeraCommands.addAtomSpecRange(featureValues,
            "<html>metal <a href=\"http:a.b.c/x\"> 'ion!", "0", 7, 15, "A");
    // feature names are sanitised to change non-alphanumeric to underscore
    // feature values are sanitised to encode single quote characters
    commands = testee.setAttributes(featuresMap);
    assertEquals(commands.size(), 1);
    StructureCommandI expected3 = new StructureCommand("iterate",
            "0//A/7-15/",
            "p.jv_side_chain_binding_='<html>metal <a href=\"http:a.b.c/x\"> &#39;ion!'");
    assertEquals(commands.get(0), expected3);
  }

  @Test(groups = "Functional")
  public void testCloseViewer()
  {
    assertEquals(testee.closeViewer(), new StructureCommand("quit"));
  }
}
