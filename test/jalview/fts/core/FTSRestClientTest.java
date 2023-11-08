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
package jalview.fts.core;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSDataColumnI.FTSDataColumnGroupI;
import jalview.gui.JvOptionPane;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FTSRestClientTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private FTSRestClient ftsRestClient;

  @BeforeMethod(alwaysRun = true)
  public void setup()
  {
    ftsRestClient = new FTSRestClient()
    {

      @Override
      public String getColumnDataConfigFileName()
      {
        return "/fts/uniprot_data_columns.txt";
      }

      @Override
      public FTSRestResponse executeRequest(FTSRestRequest ftsRequest)
              throws Exception
      {
        return null;
      }
    };
  }

  @Test(groups = { "Functional" })
  public void getPrimaryKeyColumIndexTest()
  {
    Collection<FTSDataColumnI> wantedFields = ftsRestClient
            .getAllDefaultDisplayedFTSDataColumns();
    int foundIndex = -1;
    try
    {
      Assert.assertEquals(foundIndex, -1);
      foundIndex = ftsRestClient.getPrimaryKeyColumIndex(wantedFields,
              false);
      Assert.assertEquals(foundIndex, 0);
      foundIndex = ftsRestClient.getPrimaryKeyColumIndex(wantedFields,
              true);
      Assert.assertEquals(foundIndex, 1);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getAllDefaulDisplayedDataColumns()
  {
    Assert.assertNotNull(
            ftsRestClient.getAllDefaultDisplayedFTSDataColumns());
    Assert.assertTrue(!ftsRestClient.getAllDefaultDisplayedFTSDataColumns()
            .isEmpty());
    Assert.assertEquals(
            ftsRestClient.getAllDefaultDisplayedFTSDataColumns().size(), 7);
  }

  @Test(groups = { "Functional" })
  public void getDataColumnsFieldsAsCommaDelimitedString()
  {
    Collection<FTSDataColumnI> wantedFields = ftsRestClient
            .getAllDefaultDisplayedFTSDataColumns();
    String actual = ftsRestClient
            .getDataColumnsFieldsAsCommaDelimitedString(wantedFields);
    Assert.assertEquals(actual,
            "id,entry name,protein names,genes,organism,reviewed,length");
  }

  @Test(groups = { "Functional" })
  public void getAllFTSDataColumns()
  {
    Collection<FTSDataColumnI> allFields = ftsRestClient
            .getAllFTSDataColumns();
    Assert.assertNotNull(allFields);
    Assert.assertEquals(allFields.size(), 117);
  }

  @Test(groups = { "Functional" })
  public void getSearchableDataColumns()
  {
    Collection<FTSDataColumnI> searchalbeFields = ftsRestClient
            .getSearchableDataColumns();
    Assert.assertNotNull(searchalbeFields);
    Assert.assertEquals(searchalbeFields.size(), 22);
  }

  @Test(groups = { "Functional" })
  public void getPrimaryKeyColumn()
  {
    FTSDataColumnI expectedPKColumn;
    try
    {
      expectedPKColumn = ftsRestClient
              .getDataColumnByNameOrCode("Uniprot Id");
      Assert.assertNotNull(ftsRestClient.getPrimaryKeyColumn());
      Assert.assertEquals(ftsRestClient.getPrimaryKeyColumn(),
              expectedPKColumn);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getDataColumnByNameOrCode()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("genes");
      Assert.assertNotNull(foundDataCol);
      Assert.assertEquals(foundDataCol.getName(), "Gene Names");
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getDataColumnGroupById()
  {
    FTSDataColumnGroupI foundDataColGroup;
    try
    {
      foundDataColGroup = ftsRestClient.getDataColumnGroupById("g3");
      Assert.assertNotNull(foundDataColGroup);
      Assert.assertEquals(foundDataColGroup.getName(), "Names & Taxonomy");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Test(groups = { "Functional" })
  public void getDefaultResponsePageSize()
  {
    int defaultResSize = ftsRestClient.getDefaultResponsePageSize();
    Assert.assertEquals(defaultResSize, 500);
  }

  @Test(groups = { "Functional" })
  public void getColumnMinWidthTest()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("Protein names");
      Assert.assertNotNull(foundDataCol);
      int actualColMinWidth = foundDataCol.getMinWidth();
      Assert.assertEquals(actualColMinWidth, 300);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getColumnMaxWidthTest()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("Protein names");
      Assert.assertNotNull(foundDataCol);
      int actualColMinWidth = foundDataCol.getMaxWidth();
      Assert.assertEquals(actualColMinWidth, 1500);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getColumnPreferredWidthTest()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("Protein names");
      Assert.assertNotNull(foundDataCol);
      int actualColMinWidth = foundDataCol.getPreferredWidth();
      Assert.assertEquals(actualColMinWidth, 500);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void getColumnClassTest()
  {
    try
    {
      FTSDataColumnI foundDataCol = ftsRestClient
              .getDataColumnByNameOrCode("Protein names");
      Assert.assertNotNull(foundDataCol);
      Assert.assertEquals(foundDataCol.getDataType().getDataTypeClass(),
              String.class);
      foundDataCol = ftsRestClient.getDataColumnByNameOrCode("length");
      Assert.assertNotNull(foundDataCol);
      Assert.assertEquals(foundDataCol.getDataType().getDataTypeClass(),
              Integer.class);
      // foundDataCol = ftsRestClient.getDataColumnByNameOrCode("length");
      // Assert.assertNotNull(foundDataCol);
      // Assert.assertEquals(foundDataCol.getDataColumnClass(), Double.class);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Exception thrown while testing...");
    }
  }

  @Test(groups = { "Functional" })
  public void coverageForEqualsAndHashFunction()
  {
    Set<FTSDataColumnI> uniqueSet = new HashSet<FTSDataColumnI>();
    Collection<FTSDataColumnI> searchableCols = ftsRestClient
            .getSearchableDataColumns();
    for (FTSDataColumnI foundCol : searchableCols)
    {
      System.out.println(foundCol.toString());
      uniqueSet.add(foundCol);
      uniqueSet.add(foundCol);
    }
    Assert.assertTrue(!uniqueSet.isEmpty());
    Assert.assertEquals(uniqueSet.size(), 22);
  }

  @Test(groups = { "Functional" })
  public void coverageForMiscellaneousBranches()
  {
    String actual = ftsRestClient.getPrimaryKeyColumn().toString();
    Assert.assertEquals(actual, "Uniprot Id");

    String actualGroupStr;
    try
    {
      actualGroupStr = ftsRestClient.getDataColumnGroupById("g4")
              .toString();
      Assert.assertEquals(actualGroupStr, "Procedures & Softwares");
      actualGroupStr = ftsRestClient
              .getDataColumnGroupById("unavailable group").toString();
    } catch (Exception e)
    {
      Assert.assertTrue(true);
    }

    String actualResourseFile = ftsRestClient
            .getResourceFile("/fts/uniprot_data_columns.txt");
    Assert.assertNotNull(actualResourseFile);
    Assert.assertTrue(actualResourseFile.length() > 31);
  }

  @Test(groups = { "Functional" }, expectedExceptions = Exception.class)
  public void coverageForExceptionBranches() throws Exception
  {
    try
    {
      ftsRestClient.getDataColumnByNameOrCode("unavailable column");
    } catch (Exception e)
    {
      System.out.println(e.getMessage());
      String expectedMessage = "Couldn't find data column with name : unavailable column";
      Assert.assertEquals(e.getMessage(), expectedMessage);
    }
    try
    {
      ftsRestClient.getDataColumnGroupById("unavailable column group Id");
    } catch (Exception e)
    {
      System.out.println(e.getMessage());
      String expectedMessage = "Couldn't find data column group with id : unavailable column group Id";
      Assert.assertEquals(e.getMessage(), expectedMessage);
    }

    ftsRestClient.getDataColumnByNameOrCode("unavailable column");

    ftsRestClient.getResourceFile("unavailable resource file");

  }

}
