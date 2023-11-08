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
package jalview.io;

import static org.testng.ConversionUtils.wrapDataProvider;

import jalview.analysis.SequenceIdMatcher;
import jalview.analysis.TreeModel;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.gui.JvOptionPane;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import org.junit.runners.Parameterized.Parameters;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author jimp
 * 
 */
public class NewickFileTests
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Factory
  public static Object[] factoryData()
  {
    return wrapDataProvider(NewickFileTests.class, data());
  }

  @Parameters
  public static Collection data()
  {
    return Arrays.asList(new Object[][] {

        new String[]
        { "Simple uniref50 newick",
            "(((FER_BRANA:128.0,FER3_RAPSA:128.0):50.75,FER_CAPAA:178.75):121.94443,(Q93Z60_ARATH:271.45456,((O80429_MAIZE:183.0,FER1_MAIZE:183.0):30.5,((Q7XA98_TRIPR:90.0,FER1_PEA:90.0):83.32143,(((FER2_ARATH:64.0,FER1_ARATH:64.0):94.375,(FER1_SPIOL:124.5,FER1_MESCR:124.5):33.875):6.4166718,((Q93XJ9_SOLTU:33.5,FER1_SOLLC:33.5):49.0,FER_CAPAN:82.5):82.29167):8.529755):40.178574):57.95456):29.239868);" },
        new String[]
        { "Tree with quotes",
            "('Syn_PROSU-1_IIh_3d(CA4)|CK_Syn_PROSU-1_1907':1.0638313,'Syn_MINOS11_5.3_3d(CA4)|CK_Syn_MINOS11_750':1.063831);" },
        new String[]
        { "Tree with double escaped comma in node",
            "('Syn_PROSU-1_IIh_3d(CA4)|CK_Syn_PROSU-1_1907':1.0638313,'Syn_MINOS11_5.3_3d(CA4)''|CK_Syn_MINOS11_750':1.063831);" } });
  };

  String name, testTree;

  public NewickFileTests(String _name, String _testTree)
  {
    this.name = _name;
    this.testTree = _testTree;
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
  }

  @Test(groups = { "Functional" })
  public void testTreeIO() throws Exception
  {
    String stage = "Init", treename = " '" + name + "' :";
    try
    {
      stage = "Parsing testTree " + treename;
      System.out.println(treename + "\n" + testTree);
      NewickFile nf = new NewickFile(testTree, DataSourceType.PASTE);
      nf.parse();
      AssertJUnit.assertTrue(
              stage + "Invalid Tree '" + nf.getWarningMessage() + "'",
              nf.isValid());
      BinaryNode tree = nf.getTree();
      AssertJUnit.assertTrue(stage + "Null Tree", tree != null);
      stage = "Creating newick file from testTree " + treename;
      String gentree = new NewickFile(tree).print(nf.HasBootstrap(),
              nf.HasDistances());
      AssertJUnit.assertTrue(stage + "Empty string generated",
              gentree != null && gentree.trim().length() > 0);
      stage = "Parsing regenerated testTree " + treename;
      NewickFile nf_regen = new NewickFile(gentree, DataSourceType.PASTE);
      nf_regen.parse();
      AssertJUnit.assertTrue(
              stage + "Newick file is invalid ('"
                      + nf_regen.getWarningMessage() + "')",
              nf_regen.isValid());
      BinaryNode tree_regen = nf.getTree();
      AssertJUnit.assertTrue(stage + "Null Tree", tree_regen != null);
      stage = "Compare original and generated tree" + treename;

      Vector<BinaryNode> oseqs, nseqs;
      oseqs = new TreeModel(new SequenceI[0], null, nf)
              .findLeaves(nf.getTree());
      AssertJUnit.assertTrue(stage + "No nodes in original tree.",
              oseqs.size() > 0);
      SequenceI[] olsqs = new SequenceI[oseqs.size()];
      for (int i = 0, iSize = oseqs.size(); i < iSize; i++)
      {
        olsqs[i] = (SequenceI) oseqs.get(i).element();
      }
      nseqs = new TreeModel(new SequenceI[0], null, nf_regen)
              .findLeaves(nf_regen.getTree());
      AssertJUnit.assertTrue(stage + "No nodes in regerated tree.",
              nseqs.size() > 0);
      SequenceI[] nsqs = new SequenceI[nseqs.size()];
      for (int i = 0, iSize = nseqs.size(); i < iSize; i++)
      {
        nsqs[i] = (SequenceI) nseqs.get(i).element();
      }
      AssertJUnit.assertTrue(
              stage + " Different number of leaves (original "
                      + olsqs.length + " and regen " + nsqs.length + ")",
              olsqs.length == nsqs.length);
      SequenceIdMatcher omatcher = new SequenceIdMatcher(olsqs),
              nmatcher = new SequenceIdMatcher(nsqs);

      SequenceI[] osmatches = omatcher.findIdMatch(nsqs);
      SequenceI[] nsmatches = nmatcher.findIdMatch(olsqs);
      String warns = "";
      for (int i = 0, iSize = nseqs.size(); i < iSize; i++)
      {
        if (nsmatches[i] == null)
        {
          warns += "\noriginal sequence ID '" + olsqs[i].getName()
                  + "' wasn't found in regenerated set.";
        }
        if (osmatches[i] == null)
        {
          warns += "\nregenerated sequence ID '" + nsqs[i].getName()
                  + "' wasn't found in original set.";
        }
      }

      if (warns.length() > 0)
      {
        Assert.fail(stage + warns);
      }
    } catch (Exception x)
    {
      throw (new Exception(stage + "Exception raised", x));
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
  }

}
