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
package jalview.ext.rbvi.chimera;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsI.AtomSpecType;

public class ChimeraCommandsTest
{
  private ChimeraCommands testee;

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    testee = new ChimeraCommands();
  }

  @Test(groups = { "Functional" })
  public void testColourBySequence()
  {

    Map<Object, AtomSpecModel> map = new LinkedHashMap<>();
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "0", 2, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "0", 7, 7, "B");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "0", 9, 23, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "1", 1, 1, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "1", 4, 7, "B");
    ChimeraCommands.addAtomSpecRange(map, Color.yellow, "1", 8, 8, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.yellow, "1", 3, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.red, "0", 3, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.red, "0", 6, 9, "A");

    // Colours should appear in the Chimera command in the order in which
    // they were added; within colour, by model, by chain, ranges in start order
    List<StructureCommandI> commands = testee.colourBySequence(map);
    assertEquals(commands.size(), 1);
    assertEquals(commands.get(0).getCommand(),
            "color #0000ff #0:2-5.A,9-23.A,7.B|#1:1.A,4-7.B;color #ffff00 #1:3-5.A,8.A;color #ff0000 #0:3-9.A");
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
    assertEquals(1, commands.size());

    /*
     * feature name gets a jv_ namespace prefix
     * feature value is quoted in case it contains spaces
     */
    assertEquals(commands.get(0).getCommand(),
            "setattr res jv_chain 'X' #0:8-20.A");

    // add same feature value, overlapping range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 3, 9, "A");
    // same feature value, contiguous range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 21, 25, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(1, commands.size());
    assertEquals(commands.get(0).getCommand(),
            "setattr res jv_chain 'X' #0:3-25.A");

    // same feature value and model, different chain
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 21, 25, "B");
    // same feature value and chain, different model
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "1", 26, 30, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(1, commands.size());
    String expected1 = "setattr res jv_chain 'X' #0:3-25.A,21-25.B|#1:26-30.A";
    assertEquals(commands.get(0).getCommand(), expected1);

    // same feature, different value
    ChimeraCommands.addAtomSpecRange(featureValues, "Y", "0", 40, 50, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(2, commands.size());
    // commands are ordered by feature type but not by value
    // so test for the expected command in either order
    String cmd1 = commands.get(0).getCommand();
    String cmd2 = commands.get(1).getCommand();
    assertTrue(cmd1.equals(expected1) || cmd2.equals(expected1));
    String expected2 = "setattr res jv_chain 'Y' #0:40-50.A";
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
    String expected3 = "setattr res jv_side_chain_binding_ '<html>metal <a href=\"http:a.b.c/x\"> &#39;ion!' #0:7-15.A";
    assertTrue(commands.get(0).getCommand().equals(expected3));
  }

  /**
   * Tests for the method that prefixes and sanitises a feature name so it can
   * be used as a valid, namespaced attribute name in Chimera or PyMol
   */
  @Test(groups = { "Functional" })
  public void testMakeAttributeName()
  {
    assertEquals(testee.makeAttributeName(null), "jv_");
    assertEquals(testee.makeAttributeName(""), "jv_");
    assertEquals(testee.makeAttributeName("helix"), "jv_helix");
    assertEquals(testee.makeAttributeName("Hello World 24"),
            "jv_Hello_World_24");
    assertEquals(testee.makeAttributeName("!this is-a_very*{odd(name"),
            "jv__this_is_a_very__odd_name");
    // name ending in color gets underscore appended
    assertEquals(testee.makeAttributeName("helixColor"), "jv_helixColor_");
  }

  @Test(groups = "Functional")
  public void testGetAtomSpec()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY), "");
    model.addRange("1", 2, 4, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1:2-4.A");
    model.addRange("1", 8, 8, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1:2-4.A,8.A");
    model.addRange("1", 5, 7, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1:2-4.A,8.A,5-7.B");
    model.addRange("1", 3, 5, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1:2-5.A,8.A,5-7.B");
    model.addRange("0", 1, 4, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0:1-4.B|#1:2-5.A,8.A,5-7.B");
    model.addRange("0", 5, 9, "C");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0:1-4.B,5-9.C|#1:2-5.A,8.A,5-7.B");
    model.addRange("1", 8, 10, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0:1-4.B,5-9.C|#1:2-5.A,8.A,5-10.B");
    model.addRange("1", 8, 9, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0:1-4.B,5-9.C|#1:2-5.A,8.A,5-10.B");
    model.addRange("0", 3, 10, "C"); // subsumes 5-9
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0:1-4.B,3-10.C|#1:2-5.A,8.A,5-10.B");
    model.addRange("5", 25, 35, " ");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0:1-4.B,3-10.C|#1:2-5.A,8.A,5-10.B|#5:25-35.");

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
            toAlign, AtomSpecType.ALPHA);
    // qualifier to restrict match to CA and no altlocs
    String carbonAlphas = "@CA&~@.B-Z&~@.2-9";
    String refSpec = "#1:12-14.A,18.B,22-23.B";
    String toAlignSpec = "#2:15-17.B,20-21.B,22.C";
    String expected = String.format("match %s%s %s%s; ribbon %s|%s; focus",
            toAlignSpec, carbonAlphas, refSpec, carbonAlphas, toAlignSpec,
            refSpec);
    assertEquals(command.get(0).getCommand(), expected);
  }

  @Test(groups = "Functional")
  public void testGetAtomSpec_alphaOnly()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA), "");
    model.addRange("1", 2, 4, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1:2-4.A@CA&~@.B-Z&~@.2-9");
    model.addRange("1", 8, 8, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1:2-4.A,8.A@CA&~@.B-Z&~@.2-9");
    model.addRange("1", 5, 7, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1:2-4.A,8.A,5-7.B@CA&~@.B-Z&~@.2-9");
    model.addRange("1", 3, 5, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1:2-5.A,8.A,5-7.B@CA&~@.B-Z&~@.2-9");
    model.addRange("0", 1, 4, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0:1-4.B@CA&~@.B-Z&~@.2-9|#1:2-5.A,8.A,5-7.B@CA&~@.B-Z&~@.2-9");
    model.addRange("0", 5, 9, "C");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0:1-4.B,5-9.C@CA&~@.B-Z&~@.2-9|#1:2-5.A,8.A,5-7.B@CA&~@.B-Z&~@.2-9");
    model.addRange("1", 8, 10, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0:1-4.B,5-9.C@CA&~@.B-Z&~@.2-9|#1:2-5.A,8.A,5-10.B@CA&~@.B-Z&~@.2-9");
    model.addRange("1", 8, 9, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0:1-4.B,5-9.C@CA&~@.B-Z&~@.2-9|#1:2-5.A,8.A,5-10.B@CA&~@.B-Z&~@.2-9");
    model.addRange("0", 3, 10, "C"); // subsumes 5-9
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0:1-4.B,3-10.C@CA&~@.B-Z&~@.2-9|#1:2-5.A,8.A,5-10.B@CA&~@.B-Z&~@.2-9");
    model.addRange("5", 25, 35, " "); // empty chain code
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0:1-4.B,3-10.C@CA&~@.B-Z&~@.2-9|#1:2-5.A,8.A,5-10.B@CA&~@.B-Z&~@.2-9|#5:25-35.@CA&~@.B-Z&~@.2-9");

  }

  @Test(groups = "Functional")
  public void testGetModelStartNo()
  {
    assertEquals(testee.getModelStartNo(), 0);
  }

  @Test(groups = "Functional")
  public void testGetResidueSpec()
  {
    assertEquals(testee.getResidueSpec("ALA"), "::ALA");
  }

  @Test(groups = "Functional")
  public void testShowBackbone()
  {
    List<StructureCommandI> cmds = testee.showBackbone();
    assertEquals(cmds.size(), 1);
    assertEquals(cmds.get(0).getCommand(),
            "~display all;~ribbon;chain @CA|P");
  }

  @Test(groups = "Functional")
  public void testOpenCommandFile()
  {
    assertEquals(testee.openCommandFile("nowhere").getCommand(),
            "open cmd:nowhere");
  }

  @Test(groups = "Functional")
  public void testSaveSession()
  {
    assertEquals(testee.saveSession("somewhere").getCommand(),
            "save somewhere");
  }

  @Test(groups = "Functional")
  public void testColourByChain()
  {
    assertEquals(testee.colourByChain().getCommand(), "rainbow chain");
  }

  @Test(groups = { "Functional" })
  public void testSetBackgroundColour()
  {
    StructureCommandI cmd = testee.setBackgroundColour(Color.PINK);
    assertEquals(cmd.getCommand(), "set bgColor #ffafaf");
  }

  @Test(groups = { "Functional" })
  public void testLoadFile()
  {
    StructureCommandI cmd = testee.loadFile("/some/filepath");
    assertEquals(cmd.getCommand(), "open /some/filepath");
  }

  @Test(groups = { "Functional" })
  public void testOpenSession()
  {
    StructureCommandI cmd = testee.openSession("/some/filepath");
    assertEquals(cmd.getCommand(), "open chimera:/some/filepath");
  }

  @Test(groups = "Functional")
  public void testColourByCharge()
  {
    List<StructureCommandI> cmds = testee.colourByCharge();
    assertEquals(cmds.size(), 1);
    assertEquals(cmds.get(0).getCommand(),
            "color white;color red ::ASP,GLU;color blue ::LYS,ARG;color yellow ::CYS");
  }

  @Test(groups = "Functional")
  public void testGetColourCommand()
  {
    assertEquals(
            testee.colourResidues("something", Color.MAGENTA).getCommand(),
            "color #ff00ff something");
  }

  @Test(groups = "Functional")
  public void testFocusView()
  {
    assertEquals(testee.focusView().getCommand(), "focus");
  }

  @Test(groups = "Functional")
  public void testSetAttribute()
  {
    AtomSpecModel model = new AtomSpecModel();
    model.addRange("1", 89, 92, "A");
    model.addRange("2", 12, 20, "B");
    model.addRange("2", 8, 9, "B");
    assertEquals(testee.setAttribute("jv_kd", "27.3", model).getCommand(),
            "setattr res jv_kd '27.3' #1:89-92.A|#2:8-9.B,12-20.B");
  }

  @Test(groups = "Functional")
  public void testCloseViewer()
  {
    assertEquals(testee.closeViewer(), new StructureCommand("stop really"));
  }

  @Test(groups = "Functional")
  public void testGetSelectedResidues()
  {
    assertEquals(testee.getSelectedResidues(),
            new StructureCommand("list selection level residue"));
  }

  @Test(groups = "Functional")
  public void testListResidueAttributes()
  {
    assertEquals(testee.listResidueAttributes(),
            new StructureCommand("list resattr"));
  }

  @Test(groups = "Functional")
  public void testGetResidueAttributes()
  {
    assertEquals(testee.getResidueAttributes("binding site"),
            new StructureCommand("list residues attr 'binding site'"));
  }

  @Test(groups = "Functional")
  public void testStartNotifications()
  {
    List<StructureCommandI> cmds = testee.startNotifications("to here");
    assertEquals(cmds.size(), 2);
    assertEquals(cmds.get(0),
            new StructureCommand("listen start models url to here"));
    assertEquals(cmds.get(1), new StructureCommand(
            "listen start select prefix SelectionChanged url to here"));
  }

  @Test(groups = "Functional")
  public void testStopNotifications()
  {
    List<StructureCommandI> cmds = testee.stopNotifications();
    assertEquals(cmds.size(), 2);
    assertEquals(cmds.get(0), new StructureCommand("listen stop models"));
    assertEquals(cmds.get(1),
            new StructureCommand("listen stop selection"));
  }
}
