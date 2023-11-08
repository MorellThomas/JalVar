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

package jalview.urls;

import static jalview.util.UrlConstants.DELIM;
import static jalview.util.UrlConstants.SEP;
import static jalview.util.UrlConstants.SEQUENCE_ID;

import jalview.urls.api.UrlProviderI;
import jalview.util.MessageManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UrlLinkTableModelTest
{

  private static final String inmenu = "TEST|http://someurl.blah/$DB_ACCESSION$|"
          + "ANOTHER|http://test/t$SEQUENCE_ID$|"
          + "TEST2|http://address/$SEQUENCE_ID$|SRS|"
          + "http://theSRSlink/$SEQUENCE_ID$|"
          + "MIR:00000005|MIR:00000011|MIR:00000372";

  private static final String notinmenu = "Not1|http://not.in.menu/$DB_ACCESSION$|"
          + "Not2|http://not.in.menu.either/$DB_ACCESSION$";

  private static final String testIdOrgString = "{\"Local\": [{\"id\":\"MIR:00000002\",\"name\":\"ChEBI\",\"pattern\":\"^CHEBI:\\d+$\","
          + "\"definition\":\"Chemical Entities of Biological Interest (ChEBI)\",\"prefix\":\"chebi\","
          + "\"url\":\"http://identifiers.org/chebi\"},{\"id\":\"MIR:00000005\",\"name\":\"UniProt Knowledgebase\","
          + "\"pattern\":\"^([A-N,R-Z][0-9]([A-Z][A-Z, 0-9][A-Z, 0-9][0-9]){1,2})|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])(\\.\\d+)?$\","
          + "\"definition\":\"The UniProt Knowledgebase (UniProtKB)\",\"prefix\":\"uniprot\",\"url\":\"http://identifiers.org/uniprot\"},"
          + "{\"id\":\"MIR:00000011\",\"name\":\"InterPro\",\"pattern\":\"^IPR\\d{6}$\",\"definition\":\"InterPro\",\"prefix\":\"interpro\","
          + "\"url\":\"http://identifiers.org/interpro\"},"
          + "{\"id\":\"MIR:00000372\",\"name\":\"ENA\",\"pattern\":\"^[A-Z]+[0-9]+(\\.\\d+)?$\",\"definition\":\"The European Nucleotide Archive (ENA),\""
          + "\"prefix\":\"ena.embl\",\"url\":\"http://identifiers.org/ena.embl\"}]}";

  private UrlProviderI prov;

  @BeforeMethod(alwaysRun = true)
  public void setup()
  {
    // set up UrlProvider data as the source for the TableModel
    // the data gets updated by the TableModel, so needs to be reinitialised for
    // each test

    // make a dummy identifiers.org download file
    File temp = null;
    try
    {
      temp = File.createTempFile("tempfile", ".tmp");
      temp.deleteOnExit();
      BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
      bw.write(testIdOrgString);
      bw.close();
    } catch (IOException e)
    {
      System.out.println("Error initialising UrlLinkTableModel test: "
              + e.getMessage());
    }

    // set up custom and identifiers.org url providers
    IdOrgSettings.setDownloadLocation(temp.getPath());
    IdentifiersUrlProvider idprov = new IdentifiersUrlProvider(inmenu);
    CustomUrlProvider cprov = new CustomUrlProvider(inmenu, notinmenu);
    List<UrlProviderI> provlist = new ArrayList<UrlProviderI>();
    provlist.add(idprov);
    provlist.add(cprov);

    prov = new UrlProvider("TEST2", provlist);
  }

  /*
   * Test that the table model is correctly initialised
   * Display columns and default row are set; data provider listening event set up
   */
  @Test(groups = { "Functional" })
  public void testInitialisation()
  {
    int defaultCol = 4;
    int dbCol = 0;
    int descCol = 1;

    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    // exactly one table model listener
    TableModelListener[] listeners = m
            .getListeners(TableModelListener.class);
    Assert.assertEquals(listeners.length, 1);

    // default row exists, there is exactly 1, and it matches the supplied
    // default
    int count = 0;
    for (int row = 0; row < m.getRowCount(); row++)
    {
      boolean isDefault = (boolean) m.getValueAt(row, defaultCol);
      if (isDefault)
      {
        count++;
        String defaultDBName = (String) m.getValueAt(row, dbCol);
        Assert.assertEquals(defaultDBName, "TEST2");

        String defaultDesc = (String) m.getValueAt(row, descCol);
        Assert.assertEquals(defaultDesc, "TEST2");
      }
    }
    Assert.assertEquals(count, 1);
  }

  /*
   * Test row and column counts
   */
  @Test(groups = { "Functional" })
  public void testCounts()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    // correct numbers of column and rows
    Assert.assertEquals(m.getColumnCount(), 5);
    Assert.assertEquals(m.getRowCount(), 10);
  }

  /*
   * Test column access
   */
  @Test(groups = { "Functional" })
  public void testColumns()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    // check column names
    Assert.assertEquals(m.getColumnName(0),
            MessageManager.formatMessage("label.database"));
    Assert.assertEquals(m.getColumnName(1),
            MessageManager.formatMessage("label.name"));
    Assert.assertEquals(m.getColumnName(2),
            MessageManager.formatMessage("label.url"));
    Assert.assertEquals(m.getColumnName(3),
            MessageManager.formatMessage("label.inmenu"));
    Assert.assertEquals(m.getColumnName(4),
            MessageManager.formatMessage("label.primary"));

    // check column classes
    Assert.assertEquals(m.getColumnClass(0), String.class);
    Assert.assertEquals(m.getColumnClass(1), String.class);
    Assert.assertEquals(m.getColumnClass(2), String.class);
    Assert.assertEquals(m.getColumnClass(3), Boolean.class);
    Assert.assertEquals(m.getColumnClass(4), Boolean.class);
  }

  /*
   * Test row insertion
   */
  @Test(groups = { "Functional" })
  public void testRowInsert()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    m.insertRow("newname", "newurl");

    // check table has new row inserted
    Assert.assertEquals(m.getValueAt(10, 0), "newname");
    Assert.assertEquals(m.getValueAt(10, 1), "newname");
    Assert.assertEquals(m.getValueAt(10, 2), "newurl");
    Assert.assertEquals(m.getValueAt(10, 3), true);
    Assert.assertEquals(m.getValueAt(10, 4), false);

    // check data source has new row insrte
    Assert.assertTrue(
            prov.getLinksForMenu().contains("newname" + SEP + "newurl"));
  }

  /*
   * Test row deletion
   */
  @Test(groups = { "Functional" })
  public void testRowDelete()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    // get name and url at row 0
    String name = (String) m.getValueAt(0, 0);
    String url = (String) m.getValueAt(0, 1);

    m.removeRow(0);

    // check table no longer has row 0 elements in it
    for (int row = 0; row < m.getRowCount(); row++)
    {
      Assert.assertNotEquals(m.getValueAt(row, 0), name);
    }

    // check data source likewise
    Assert.assertFalse(prov.getLinksForMenu().contains(name + SEP + url));
  }

  /*
   * Test value setting and getting
   */
  @Test(groups = { "Functional" })
  public void testValues()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    // get original default
    int olddefault;
    boolean isDefault = false;
    for (olddefault = 0; olddefault < m.getRowCount()
            && !isDefault; olddefault++)
    {
      isDefault = (boolean) m.getValueAt(olddefault, 3);
    }

    // set new values, one in each row
    m.setValueAt("dbnamechanged", 6, 0);
    m.setValueAt("descchanged", 6, 1);
    m.setValueAt("urlchanged", 7, 2);
    m.setValueAt(false, 8, 3);
    m.setValueAt(true, 6, 4);

    m.setValueAt("dbnamechanged", 5, 0);

    // check values updated in table
    Assert.assertEquals(m.getValueAt(6, 0), "descchanged"); // custom url can't
                                                            // change db name
    Assert.assertEquals(m.getValueAt(6, 1), "descchanged");
    Assert.assertEquals(m.getValueAt(7, 2), "urlchanged");
    Assert.assertFalse((boolean) m.getValueAt(8, 3));
    Assert.assertTrue((boolean) m.getValueAt(6, 4));
    Assert.assertFalse((boolean) m.getValueAt(olddefault, 4));

    Assert.assertEquals(m.getValueAt(5, 0), "dbnamechanged");

    // check default row is exactly one row still
    for (int row = 0; row < m.getRowCount(); row++)
    {
      isDefault = (boolean) m.getValueAt(row, 4);

      // if isDefault is true, row is 9
      // if isDefault is false, row is not 9
      Assert.assertFalse(isDefault && !(row == 6));
    }

    // check table updated
    Assert.assertTrue(prov.writeUrlsAsString(true)
            .contains("descchanged" + SEP + m.getValueAt(6, 2)));
    Assert.assertTrue(prov.writeUrlsAsString(true)
            .contains(m.getValueAt(7, 1) + SEP + "urlchanged"));
    Assert.assertTrue(prov.writeUrlsAsString(false)
            .contains((String) m.getValueAt(8, 1)));
    Assert.assertEquals(prov.getPrimaryUrl("seqid"), m.getValueAt(6, 2)
            .toString().replace(DELIM + SEQUENCE_ID + DELIM, "seqid"));
  }

  /*
   * Test cell editability
   */
  @Test(groups = { "Functional" })
  public void testEditable()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    for (int row = 0; row < m.getRowCount(); row++)
    {
      Assert.assertFalse(m.isCellEditable(row, 0));
      Assert.assertFalse(m.isCellEditable(row, 1));
      Assert.assertFalse(m.isCellEditable(row, 2));
      Assert.assertTrue(m.isCellEditable(row, 3));

      if ((row == 4) || (row == 6) || (row == 7))
      {
        Assert.assertTrue(m.isCellEditable(row, 4));
      }
      else
      {
        Assert.assertFalse(m.isCellEditable(row, 4));
      }
    }
  }

  /*
   * Test row 'deletability'
   */
  @Test(groups = { "Functional" })
  public void testDeletable()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    for (int row = 0; row < m.getRowCount(); row++)
    {
      if (row > 4)
      {
        Assert.assertTrue(m.isRowDeletable(row));
      }
      else
      {
        Assert.assertFalse(m.isRowDeletable(row));
      }
    }
  }

  /*
   * Test indirect row editability
   */
  @Test(groups = { "Functional" })
  public void testRowEditable()
  {
    UrlLinkTableModel m = new UrlLinkTableModel(prov);

    for (int row = 0; row < m.getRowCount(); row++)
    {
      if (row > 3)
      {
        Assert.assertTrue(m.isRowEditable(row));
      }
      else
      {
        Assert.assertFalse(m.isRowEditable(row));
      }
    }
  }
}
