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

public class ChimeraXCommandsTest
{
  private ChimeraXCommands testee;

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    testee = new ChimeraXCommands();
  }

  @Test(groups = { "Functional" })
  public void testColourByCharge()
  {
    List<StructureCommandI> cmd = testee.colourByCharge();
    assertEquals(cmd.size(), 1);
    assertEquals(cmd.get(0).getCommand(),
            "color white;color :ASP,GLU red;color :LYS,ARG blue;color :CYS yellow");
  }

  @Test(groups = { "Functional" })
  public void testColourByChain()
  {
    StructureCommandI cmd = testee.colourByChain();
    assertEquals(cmd.getCommand(), "rainbow chain");
  }

  @Test(groups = { "Functional" })
  public void testFocusView()
  {
    StructureCommandI cmd = testee.focusView();
    assertEquals(cmd.getCommand(), "view");
  }

  @Test(groups = { "Functional" })
  public void testSetBackgroundColour()
  {
    StructureCommandI cmd = testee.setBackgroundColour(Color.PINK);
    assertEquals(cmd.getCommand(), "set bgColor #ffafaf");
  }

  @Test(groups = { "Functional" })
  public void testOpenSession()
  {
    StructureCommandI cmd = testee.openSession("/some/filepath");
    assertEquals(cmd.getCommand(), "open /some/filepath format session");
  }

  @Test(groups = { "Functional" })
  public void testColourBySequence()
  {
    Map<Object, AtomSpecModel> map = new LinkedHashMap<>();
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "1", 2, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "1", 7, 7, "B");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "1", 9, 23, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "2", 1, 1, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.blue, "2", 4, 7, "B");
    ChimeraCommands.addAtomSpecRange(map, Color.yellow, "2", 8, 8, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.yellow, "2", 3, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.red, "1", 3, 5, "A");
    ChimeraCommands.addAtomSpecRange(map, Color.red, "1", 6, 9, "A");

    /*
     * Colours should appear in the Chimera command in the order in which
     * they were added; within colour, by model, by chain, ranges in start order
     */
    List<StructureCommandI> commands = testee.colourBySequence(map);
    assertEquals(commands.size(), 1);
    assertEquals(commands.get(0).getCommand(),
            "color #1/A:2-5,9-23/B:7|#2/A:1/B:4-7 #0000ff;color #2/A:3-5,8 #ffff00;color #1/A:3-9 #ff0000");
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
     * feature value is quoted in case it contains spaces
     */
    assertEquals(commands.get(0).getCommand(),
            "setattr #0/A:8-20 res jv_chain 'X' create true");

    // add same feature value, overlapping range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 3, 9, "A");
    // same feature value, contiguous range
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 21, 25, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(commands.size(), 1);
    assertEquals(commands.get(0).getCommand(),
            "setattr #0/A:3-25 res jv_chain 'X' create true");

    // same feature value and model, different chain
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "0", 21, 25, "B");
    // same feature value and chain, different model
    ChimeraCommands.addAtomSpecRange(featureValues, "X", "1", 26, 30, "A");
    commands = testee.setAttributes(featuresMap);
    assertEquals(commands.size(), 1);
    String expected1 = "setattr #0/A:3-25/B:21-25|#1/A:26-30 res jv_chain 'X' create true";
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
    String expected2 = "setattr #0/A:40-50 res jv_chain 'Y' create true";
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
    String expected3 = "setattr #0/A:7-15 res jv_side_chain_binding_ '<html>metal <a href=\"http:a.b.c/x\"> &#39;ion!' create true";
    assertTrue(commands.get(0).getCommand().equals(expected3));
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
    assertEquals(command.size(), 1);
    String cmd = command.get(0).getCommand();
    String refSpec = "#1/A:12-14/B:18,22-23";
    String toAlignSpec = "#2/B:15-17,20-21/C:22";

    /*
     * superposition arguments include AlphaCarbon restriction,
     * ribbon command does not
     */
    String expected = String.format(
            "align %s@CA toAtoms %s@CA; ribbon %s|%s; view", toAlignSpec,
            refSpec, toAlignSpec, refSpec);
    assertEquals(cmd, expected);
  }

  @Test(groups = "Functional")
  public void testGetAtomSpec()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY), "");
    model.addRange("1", 2, 4, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1/A:2-4");
    model.addRange("1", 8, 8, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1/A:2-4,8");
    model.addRange("1", 5, 7, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1/A:2-4,8/B:5-7");
    model.addRange("1", 3, 5, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#1/A:2-5,8/B:5-7");
    model.addRange("0", 1, 4, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0/B:1-4|#1/A:2-5,8/B:5-7");
    model.addRange("0", 5, 9, "C");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0/B:1-4/C:5-9|#1/A:2-5,8/B:5-7");
    model.addRange("1", 8, 10, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0/B:1-4/C:5-9|#1/A:2-5,8/B:5-10");
    model.addRange("1", 8, 9, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0/B:1-4/C:5-9|#1/A:2-5,8/B:5-10");
    model.addRange("0", 3, 10, "C"); // subsumes 5-9
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0/B:1-4/C:3-10|#1/A:2-5,8/B:5-10");
    model.addRange("5", 25, 35, " ");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.RESIDUE_ONLY),
            "#0/B:1-4/C:3-10|#1/A:2-5,8/B:5-10|#5/:25-35");
  }

  @Test(groups = "Functional")
  public void testGetAtomSpec_alphaOnly()
  {
    AtomSpecModel model = new AtomSpecModel();
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA), "");
    model.addRange("1", 2, 4, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1/A:2-4@CA");
    model.addRange("1", 8, 8, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1/A:2-4,8@CA");
    model.addRange("1", 5, 7, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1/A:2-4,8/B:5-7@CA");
    model.addRange("1", 3, 5, "A");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#1/A:2-5,8/B:5-7@CA");
    model.addRange("0", 1, 4, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0/B:1-4@CA|#1/A:2-5,8/B:5-7@CA");
    model.addRange("0", 5, 9, "C");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0/B:1-4/C:5-9@CA|#1/A:2-5,8/B:5-7@CA");
    model.addRange("1", 8, 10, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0/B:1-4/C:5-9@CA|#1/A:2-5,8/B:5-10@CA");
    model.addRange("1", 8, 9, "B");
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0/B:1-4/C:5-9@CA|#1/A:2-5,8/B:5-10@CA");
    model.addRange("0", 3, 10, "C"); // subsumes 5-9
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0/B:1-4/C:3-10@CA|#1/A:2-5,8/B:5-10@CA");
    model.addRange("5", 25, 35, " "); // empty chain code
    assertEquals(testee.getAtomSpec(model, AtomSpecType.ALPHA),
            "#0/B:1-4/C:3-10@CA|#1/A:2-5,8/B:5-10@CA|#5/:25-35@CA");
  }

  @Test(groups = "Functional")
  public void testGetModelStartNo()
  {
    assertEquals(testee.getModelStartNo(), 1);
  }

  @Test(groups = "Functional")
  public void testGetResidueSpec()
  {
    assertEquals(testee.getResidueSpec("ALA"), ":ALA");
  }

  @Test(groups = "Functional")
  public void testShowBackbone()
  {
    List<StructureCommandI> showBackbone = testee.showBackbone();
    assertEquals(showBackbone.size(), 1);
    assertEquals(showBackbone.get(0).getCommand(),
            "~display all;~ribbon;show @CA|P atoms");
  }

  @Test(groups = "Functional")
  public void testOpenCommandFile()
  {
    assertEquals(testee.openCommandFile("nowhere").getCommand(),
            "open nowhere");
  }

  @Test(groups = "Functional")
  public void testSaveSession()
  {
    assertEquals(testee.saveSession("somewhere").getCommand(),
            "save somewhere format session");
  }

  @Test(groups = "Functional")
  public void testGetColourCommand()
  {
    assertEquals(
            testee.colourResidues("something", Color.MAGENTA).getCommand(),
            "color something #ff00ff");
  }

  @Test(groups = "Functional")
  public void testSetAttribute()
  {
    AtomSpecModel model = new AtomSpecModel();
    model.addRange("1", 89, 92, "A");
    model.addRange("2", 12, 20, "B");
    model.addRange("2", 8, 9, "B");
    assertEquals(testee.setAttribute("jv_kd", "27.3", model).getCommand(),
            "setattr #1/A:89-92|#2/B:8-9,12-20 res jv_kd '27.3' create true");
  }

  @Test(groups = "Functional")
  public void testCloseViewer()
  {
    assertEquals(testee.closeViewer(), new StructureCommand("exit"));
  }

  @Test(groups = "Functional")
  public void testGetSelectedResidues()
  {
    assertEquals(testee.getSelectedResidues(),
            new StructureCommand("info selection level residue"));
  }

  @Test(groups = "Functional")
  public void testStartNotifications()
  {
    List<StructureCommandI> cmds = testee.startNotifications("to here");
    assertEquals(cmds.size(), 2);
    assertEquals(cmds.get(0), new StructureCommand(
            "info notify start models jalview prefix ModelChanged url to here"));
    assertEquals(cmds.get(1), new StructureCommand(
            "info notify start selection jalview prefix SelectionChanged url to here"));
  }

  @Test(groups = "Functional")
  public void testStopNotifications()
  {
    List<StructureCommandI> cmds = testee.stopNotifications();
    assertEquals(cmds.size(), 2);
    assertEquals(cmds.get(0),
            new StructureCommand("info notify stop models jalview"));
    assertEquals(cmds.get(1),
            new StructureCommand("info notify stop selection jalview"));
  }

  @Test(groups = "Functional")
  public void testListResidueAttributes()
  {
    assertEquals(testee.listResidueAttributes(),
            new StructureCommand("info resattr"));
  }
}
