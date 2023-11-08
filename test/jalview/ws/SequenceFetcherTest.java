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
package jalview.ws;

import jalview.analysis.CrossRef;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.ws.seqfetcher.ASequenceFetcher;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SequenceFetcherTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testNoDuplicatesInFetchDbRefs()
  {
    Map<String, List<DbSourceProxy>> seen = new HashMap<>();
    jalview.ws.SequenceFetcher sfetcher = new jalview.ws.SequenceFetcher();
    String dupes = "";
    // for (String src : sfetcher.getOrderedSupportedSources())
    for (String src : sfetcher.getNonAlignmentSources())
    {
      List<DbSourceProxy> seenitem = seen.get(src);
      if (seenitem != null)
      {
        dupes += (dupes.length() > 0 ? "," : "") + src;
      }
      else
      {
        seen.put(src, sfetcher.getSourceProxy(src));
      }
    }
    if (dupes.length() > 0)
    {
      Assert.fail("Duplicate sources : " + dupes);
    }
  }

  /**
   * simple run method to test dbsources.
   * 
   * @param argv
   * @j2sIgnore
   */
  public static void main(String[] argv)
  {
    // TODO: extracted from SequenceFetcher - convert to network dependent
    // functional integration test with
    // assertions

    String usage = "SequenceFetcher.main [-nodas] [<DBNAME> [<ACCNO>]]\n"
            + "With no arguments, all DbSources will be queried with their test Accession number.\n"
            + "With one argument, the argument will be resolved to one or more db sources and each will be queried with their test accession only.\n"
            + "If given two arguments, SequenceFetcher will try to find the DbFetcher corresponding to <DBNAME> and retrieve <ACCNO> from it.";

    if (argv != null && argv.length > 0)
    {
      String targs[] = new String[argv.length - 1];
      System.arraycopy(argv, 1, targs, 0, targs.length);
      argv = targs;
    }
    if (argv != null && argv.length > 0)
    {
      List<DbSourceProxy> sps = new SequenceFetcher()
              .getSourceProxy(argv[0]);

      if (sps != null)
      {
        for (DbSourceProxy sp : sps)
        {
          AlignmentI al = null;
          try
          {
            testRetrieval(argv[0], sp,
                    argv.length > 1 ? argv[1] : sp.getTestQuery());
          } catch (Exception e)
          {
            e.printStackTrace();
            System.err.println("Error when retrieving "
                    + (argv.length > 1 ? argv[1] : sp.getTestQuery())
                    + " from " + argv[0] + "\nUsage: " + usage);
          }
        }
        return;
      }
      else
      {
        System.err.println("Can't resolve " + argv[0]
                + " as a database name. Allowed values are :\n"
                + new SequenceFetcher().getSupportedDb());
      }
      System.out.println(usage);
      return;
    }
    ASequenceFetcher sfetcher = new SequenceFetcher();
    String[] dbSources = sfetcher.getSupportedDb();
    for (int dbsource = 0; dbsource < dbSources.length; dbsource++)
    {
      String db = dbSources[dbsource];
      // skip me
      if (db.equals(DBRefSource.PDB))
      {
        continue;
      }
      for (DbSourceProxy sp : sfetcher.getSourceProxy(db))
      {
        testRetrieval(db, sp, sp.getTestQuery());
      }
    }

  }

  private static void testRetrieval(String db, DbSourceProxy sp,
          String testQuery)
  {
    AlignmentI ds = null;
    Vector<Object[]> noProds = new Vector<>();
    System.out.println("Source: " + sp.getDbName() + " (" + db
            + "): retrieving test:" + sp.getTestQuery());
    {
      AlignmentI al = null;
      try
      {
        al = sp.getSequenceRecords(testQuery);
        if (al != null && al.getHeight() > 0)
        {
          boolean dna = sp.isDnaCoding();
          al.setDataset(null);
          AlignmentI alds = al.getDataset();
          // try and find products
          CrossRef crossRef = new CrossRef(al.getSequencesArray(), alds);
          List<String> types = crossRef.findXrefSourcesForSequences(dna);
          if (types != null)
          {
            System.out.println("Xref Types for: " + (dna ? "dna" : "prot"));
            for (String source : types)
            {
              System.out.println("Type: " + source);
              SequenceI[] prod = crossRef.findXrefSequences(source, dna)
                      .getSequencesArray();
              System.out.println(
                      "Found " + ((prod == null) ? "no" : "" + prod.length)
                              + " products");
              if (prod != null)
              {
                for (int p = 0; p < prod.length; p++)
                {
                  System.out.println(
                          "Prod " + p + ": " + prod[p].getDisplayId(true));
                }
              }
            }
          }
          else
          {
            noProds.addElement(
                    (dna ? new Object[]
                    { al, al } : new Object[] { al }));
          }

        }
      } catch (Exception ex)
      {
        System.out.println("ERROR:Failed to retrieve test query.");
        ex.printStackTrace(System.out);
      }

      if (al == null)
      {
        System.out.println("ERROR:No alignment retrieved.");
        StringBuffer raw = sp.getRawRecords();
        if (raw != null)
        {
          System.out.println(raw.toString());
        }
        else
        {
          System.out.println("ERROR:No Raw results.");
        }
      }
      else
      {
        System.out.println("Retrieved " + al.getHeight() + " sequences.");
        if (ds == null)
        {
          ds = al.getDataset();
        }
        else
        {
          ds.append(al.getDataset());
          al.setDataset(ds);
        }
      }
      System.out.flush();
      System.err.flush();
    }
    if (noProds.size() > 0)
    {
      Enumeration<Object[]> ts = noProds.elements();
      while (ts.hasMoreElements())

      {
        Object[] typeSq = ts.nextElement();
        boolean dna = (typeSq.length > 1);
        AlignmentI al = (AlignmentI) typeSq[0];
        System.out.println("Trying getProducts for "
                + al.getSequenceAt(0).getDisplayId(true));
        System.out.println("Search DS Xref for: " + (dna ? "dna" : "prot"));
        // have a bash at finding the products amongst all the retrieved
        // sequences.
        SequenceI[] seqs = al.getSequencesArray();
        Alignment prodal = new CrossRef(seqs, ds).findXrefSequences(null,
                dna);
        System.out.println("Found "
                + ((prodal == null) ? "no" : "" + prodal.getHeight())
                + " products");
        if (prodal != null)
        {
          SequenceI[] prod = prodal.getSequencesArray(); // note
          // should
          // test
          // rather
          // than
          // throw
          // away
          // codon
          // mapping
          // (if
          // present)
          for (int p = 0; p < prod.length; p++)
          {
            System.out.println(
                    "Prod " + p + ": " + prod[p].getDisplayId(true));
          }
        }
      }
    }
  }
}
