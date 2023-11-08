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
package jalview.ws.seqfetcher;

import jalview.api.FeatureSettingsModelI;
import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceI;
import jalview.util.DBRefUtils;
import jalview.util.MessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

public class ASequenceFetcher
{

  /*
   * set of databases we can retrieve entries from
   */
  protected Hashtable<String, Map<String, DbSourceProxy>> fetchableDbs;

  /*
   * comparator to sort by tier (0/1/2) and name
   */
  private Comparator<DbSourceProxy> proxyComparator;

  /**
   * Constructor
   */
  protected ASequenceFetcher()
  {
    super();

    /*
     * comparator to sort proxies by tier and name
     */
    proxyComparator = new Comparator<DbSourceProxy>()
    {
      @Override
      public int compare(DbSourceProxy o1, DbSourceProxy o2)
      {
        /*
         * Tier 0 precedes 1 precedes 2
         */
        int compared = Integer.compare(o1.getTier(), o2.getTier());
        if (compared == 0)
        {
          // defend against NullPointer - should never happen
          String o1Name = o1.getDbName();
          String o2Name = o2.getDbName();
          if (o1Name != null && o2Name != null)
          {
            compared = o1Name.compareToIgnoreCase(o2Name);
          }
        }
        return compared;
      }
    };
  }

  /**
   * get array of supported Databases
   * 
   * @return database source string for each database - only the latest version
   *         of a source db is bound to each source.
   */
  public String[] getSupportedDb()
  {
    if (fetchableDbs == null)
    {
      return null;
    }
    String[] sf = fetchableDbs.keySet()
            .toArray(new String[fetchableDbs.size()]);
    return sf;
  }

  public boolean isFetchable(String source)
  {
    for (String db : fetchableDbs.keySet())
    {
      if (source.equalsIgnoreCase(db))
      {
        return true;
      }
    }
    Console.warn("isFetchable doesn't know about '" + source + "'");
    return false;
  }

  /**
   * Fetch sequences for the given cross-references
   * 
   * @param refs
   * @param dna
   *          if true, only fetch from nucleotide data sources, else peptide
   * @return
   */
  public SequenceI[] getSequences(List<DBRefEntry> refs, boolean dna)
  {
    Vector<SequenceI> rseqs = new Vector<>();
    Hashtable<String, List<String>> queries = new Hashtable<>();
    for (DBRefEntry ref : refs)
    {
      String canonical = DBRefUtils.getCanonicalName(ref.getSource());
      if (!queries.containsKey(canonical))
      {
        queries.put(canonical, new ArrayList<String>());
      }
      List<String> qset = queries.get(canonical);
      if (!qset.contains(ref.getAccessionId()))
      {
        qset.add(ref.getAccessionId());
      }
    }
    Enumeration<String> e = queries.keys();
    while (e.hasMoreElements())
    {
      List<String> query = null;
      String db = null;
      db = e.nextElement();
      query = queries.get(db);
      if (!isFetchable(db))
      {
        reportStdError(db, query, new Exception(
                "Don't know how to fetch from this database :" + db));
        continue;
      }

      Stack<String> queriesLeft = new Stack<>();
      queriesLeft.addAll(query);

      List<DbSourceProxy> proxies = getSourceProxy(db);
      for (DbSourceProxy fetcher : proxies)
      {
        List<String> queriesMade = new ArrayList<>();
        HashSet<String> queriesFound = new HashSet<>();
        try
        {
          if (fetcher.isDnaCoding() != dna)
          {
            continue; // wrong sort of data
          }
          boolean doMultiple = fetcher.getMaximumQueryCount() > 1;
          while (!queriesLeft.isEmpty())
          {
            StringBuffer qsb = new StringBuffer();
            do
            {
              if (qsb.length() > 0)
              {
                qsb.append(fetcher.getAccessionSeparator());
              }
              String q = queriesLeft.pop();
              queriesMade.add(q);
              qsb.append(q);
            } while (doMultiple && !queriesLeft.isEmpty());

            AlignmentI seqset = null;
            try
            {
              // create a fetcher and go to it
              seqset = fetcher.getSequenceRecords(qsb.toString());
            } catch (Exception ex)
            {
              System.err.println(
                      "Failed to retrieve the following from " + db);
              System.err.println(qsb);
              ex.printStackTrace(System.err);
            }
            // TODO: Merge alignment together - perhaps
            if (seqset != null)
            {
              SequenceI seqs[] = seqset.getSequencesArray();
              if (seqs != null)
              {
                for (int is = 0; is < seqs.length; is++)
                {
                  rseqs.addElement(seqs[is]);
                  // BH 2015.01.25 check about version/accessid being null here
                  List<DBRefEntry> frefs = DBRefUtils.searchRefs(
                          seqs[is].getDBRefs(),
                          new DBRefEntry(db, null, null),
                          DBRefUtils.SEARCH_MODE_FULL);
                  for (DBRefEntry dbr : frefs)
                  {
                    queriesFound.add(dbr.getAccessionId());
                    queriesMade.remove(dbr.getAccessionId());
                  }
                  seqs[is] = null;
                }
              }
              else
              {
                if (fetcher.getRawRecords() != null)
                {
                  System.out.println(
                          "# Retrieved from " + db + ":" + qsb.toString());
                  StringBuffer rrb = fetcher.getRawRecords();
                  /*
                   * for (int rr = 0; rr<rrb.length; rr++) {
                   */
                  String hdr;
                  // if (rr<qs.length)
                  // {
                  hdr = "# " + db + ":" + qsb.toString();
                  /*
                   * } else { hdr = "# part "+rr; }
                   */
                  System.out.println(hdr);
                  if (rrb != null)
                  {
                    System.out.println(rrb);
                  }
                  System.out.println("# end of " + hdr);
                }

              }
            }

          }
        } catch (Exception ex)
        {
          reportStdError(db, queriesMade, ex);
        }
        if (queriesMade.size() > 0)
        {
          System.out.println("# Adding " + queriesMade.size()
                  + " ids back to queries list for searching again (" + db
                  + ")");
          queriesLeft.addAll(queriesMade);
        }
      }
    }

    SequenceI[] result = null;
    if (rseqs.size() > 0)
    {
      result = new SequenceI[rseqs.size()];
      int si = 0;
      for (SequenceI s : rseqs)
      {
        result[si++] = s;
        s.updatePDBIds();
      }
    }
    return result;
  }

  public void reportStdError(String db, List<String> queriesMade,
          Exception ex)
  {

    System.err.println(
            "Failed to retrieve the following references from " + db);
    int n = 0;
    for (String qv : queriesMade)
    {
      System.err.print(" " + qv + ";");
      if (n++ > 10)
      {
        System.err.println();
        n = 0;
      }
    }
    System.err.println();
    ex.printStackTrace();
  }

  /**
   * Returns a list of proxies for the given source
   * 
   * @param db
   *          database source string TODO: add version string/wildcard for
   *          retrieval of specific DB source/version combinations.
   * @return a list of DbSourceProxy for the db
   */
  public List<DbSourceProxy> getSourceProxy(String db)
  {
    db = DBRefUtils.getCanonicalName(db);
    Map<String, DbSourceProxy> dblist = fetchableDbs.get(db);
    if (dblist == null)
    {
      return new ArrayList<>();
    }

    /*
     * sort so that primary sources precede secondary
     */
    List<DbSourceProxy> dbs = new ArrayList<>(dblist.values());
    Collections.sort(dbs, proxyComparator);
    return dbs;
  }

  /**
   * constructs an instance of the proxy and registers it as a valid dbrefsource
   * 
   * @param dbSourceProxy
   *          reference for class implementing
   *          jalview.ws.seqfetcher.DbSourceProxy
   */
  protected void addDBRefSourceImpl(
          Class<? extends DbSourceProxy> dbSourceProxy)
          throws IllegalArgumentException
  {
    DbSourceProxy proxy = null;
    try
    {
      DbSourceProxy proxyObj = dbSourceProxy.getConstructor().newInstance();
      proxy = proxyObj;
    } catch (IllegalArgumentException e)
    {
      throw e;
    } catch (Exception e)
    {
      // Serious problems if this happens.
      throw new Error(MessageManager
              .getString("error.dbrefsource_implementation_exception"), e);
    }
    addDbRefSourceImpl(proxy);
  }

  /**
   * add the properly initialised DbSourceProxy object 'proxy' to the list of
   * sequence fetchers
   * 
   * @param proxy
   */
  protected void addDbRefSourceImpl(DbSourceProxy proxy)
  {
    if (proxy != null)
    {
      if (fetchableDbs == null)
      {
        fetchableDbs = new Hashtable<>();
      }
      Map<String, DbSourceProxy> slist = fetchableDbs
              .get(proxy.getDbSource());
      if (slist == null)
      {
        fetchableDbs.put(proxy.getDbSource(), slist = new Hashtable<>());
      }
      slist.put(proxy.getDbName(), proxy);
    }
  }

  /**
   * select sources which are implemented by instances of the given class
   * 
   * @param class
   *          that implements DbSourceProxy
   * @return null or vector of source names for fetchers
   */
  public String[] getDbInstances(Class class1)
  {
    if (!DbSourceProxy.class.isAssignableFrom(class1))
    {
      throw new Error(MessageManager.formatMessage(
              "error.implementation_error_dbinstance_must_implement_interface",
              new String[]
              { class1.toString() }));
    }
    if (fetchableDbs == null)
    {
      return null;
    }
    String[] sources = null;
    Vector<String> src = new Vector<>();
    Enumeration<String> dbs = fetchableDbs.keys();
    while (dbs.hasMoreElements())
    {
      String dbn = dbs.nextElement();
      for (DbSourceProxy dbp : fetchableDbs.get(dbn).values())
      {
        if (class1.isAssignableFrom(dbp.getClass()))
        {
          src.addElement(dbn);
        }
      }
    }
    if (src.size() > 0)
    {
      src.copyInto(sources = new String[src.size()]);
    }
    return sources;
  }

  public DbSourceProxy[] getDbSourceProxyInstances(Class class1)
  {
    List<DbSourceProxy> prlist = new ArrayList<>();
    for (String fetchable : getSupportedDb())
    {
      for (DbSourceProxy pr : getSourceProxy(fetchable))
      {
        if (class1.isInstance(pr))
        {
          prlist.add(pr);
        }
      }
    }
    if (prlist.size() == 0)
    {
      return null;
    }
    return prlist.toArray(new DbSourceProxy[0]);
  }

  /**
   * Returns a preferred feature colouring scheme for the given source, or null
   * if none is defined.
   * 
   * @param source
   * @return
   */
  public FeatureSettingsModelI getFeatureColourScheme(String source)
  {
    /*
     * return the first non-null colour scheme for any proxy for
     * this database source
     */
    for (DbSourceProxy proxy : getSourceProxy(source))
    {
      FeatureSettingsModelI preferredColours = proxy
              .getFeatureColourScheme();
      if (preferredColours != null)
      {
        return preferredColours;
      }
    }
    return null;
  }
}
