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
package jalview.analysis;

import java.util.Locale;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Routines for approximate Sequence Id resolution by name using string
 * containment (on word boundaries) rather than equivalence. It also attempts to
 * resolve ties where no exact match is available by picking the the id closest
 * to the query.
 */
public class SequenceIdMatcher
{
  private HashMap<SeqIdName, SequenceI> names;

  public SequenceIdMatcher(List<SequenceI> seqs)
  {
    names = new HashMap<SeqIdName, SequenceI>();
    addAll(seqs);
  }

  /**
   * Adds sequences to this matcher
   * 
   * @param seqs
   */
  public void addAll(List<SequenceI> seqs)
  {
    for (SequenceI seq : seqs)
    {
      add(seq);
    }
  }

  /**
   * Adds one sequence to this matcher
   * 
   * @param seq
   */
  public void add(SequenceI seq)
  {
    // TODO: deal with ID collisions - SequenceI should be appended to list
    // associated with this key.
    names.put(new SeqIdName(seq.getDisplayId(true)), seq);
    SequenceI dbseq = seq;
    while (dbseq.getDatasetSequence() != null)
    {
      dbseq = dbseq.getDatasetSequence();
    }
    // add in any interesting identifiers
    List<DBRefEntry> dbr = dbseq.getDBRefs();
    if (dbr != null)
    {
      SeqIdName sid = null;
      for (int r = 0, nr = dbr.size(); r < nr; r++)
      {
        sid = new SeqIdName(dbr.get(r).getAccessionId());
        if (!names.containsKey(sid))
        {
          names.put(sid, seq);
        }
      }
    }
  }

  /**
   * convenience method to make a matcher from concrete array
   * 
   * @param sequences
   */
  public SequenceIdMatcher(SequenceI[] sequences)
  {
    this(Arrays.asList(sequences));
  }

  /**
   * returns the closest SequenceI in matches to SeqIdName and returns all the
   * matches to the names hash.
   * 
   * @param candName
   *          SeqIdName
   * @param matches
   *          List of SequenceI objects
   * @return SequenceI closest SequenceI to SeqIdName
   */
  private SequenceI pickbestMatch(SeqIdName candName,
          List<SequenceI> matches)
  {
    List<SequenceI> st = pickbestMatches(candName, matches);
    return st == null || st.size() == 0 ? null : st.get(0);
  }

  /**
   * returns the closest SequenceI in matches to SeqIdName and returns all the
   * matches to the names hash.
   * 
   * @param candName
   *          SeqIdName
   * @param matches
   *          Vector of SequenceI objects
   * @return Object[] { SequenceI closest SequenceI to SeqIdName, SequenceI[]
   *         ties }
   */
  private List<SequenceI> pickbestMatches(SeqIdName candName,
          List<SequenceI> matches)
  {
    ArrayList<SequenceI> best = new ArrayList<SequenceI>();
    if (candName == null || matches == null || matches.size() == 0)
    {
      return null;
    }
    SequenceI match = matches.remove(0);
    best.add(match);
    names.put(new SeqIdName(match.getName()), match);
    int matchlen = match.getName().length();
    int namlen = candName.id.length();
    while (matches.size() > 0)
    {
      // look through for a better one.
      SequenceI cand = matches.remove(0);
      names.put(new SeqIdName(cand.getName()), cand);
      int q, w, candlen = cand.getName().length();
      // keep the one with an id 'closer' to the given seqnam string
      if ((q = Math.abs(matchlen - namlen)) > (w = Math
              .abs(candlen - namlen)) && candlen > matchlen)
      {
        best.clear();
        match = cand;
        matchlen = candlen;
        best.add(match);
      }
      if (q == w && candlen == matchlen)
      {
        // record any ties
        best.add(cand);
      }
    }
    if (best.size() == 0)
    {
      return null;
    }
    ;
    return best;
  }

  /**
   * get SequenceI with closest SequenceI.getName() to seq.getName()
   * 
   * @param seq
   *          SequenceI
   * @return SequenceI
   */
  public SequenceI findIdMatch(SequenceI seq)
  {
    SeqIdName nam = new SeqIdName(seq.getName());
    return findIdMatch(nam);
  }

  public SequenceI findIdMatch(String seqnam)
  {
    SeqIdName nam = new SeqIdName(seqnam);
    return findIdMatch(nam);
  }

  /**
   * Find all matches for a given sequence name.
   * 
   * @param seqnam
   *          string to query Matcher with.
   * @return a new array or (possibly) null
   */
  public SequenceI[] findAllIdMatches(String seqnam)
  {

    SeqIdName nam = new SeqIdName(seqnam);
    List<SequenceI> m = findAllIdMatches(nam);
    if (m != null)
    {
      return m.toArray(new SequenceI[m.size()]);
    }
    return null;
  }

  /**
   * findIdMatch
   * 
   * Return pointers to sequences (or sequence object containers) which have
   * same Id as a given set of different sequence objects
   * 
   * @param seqs
   *          SequenceI[]
   * @return SequenceI[]
   */
  public SequenceI[] findIdMatch(SequenceI[] seqs)
  {
    SequenceI[] namedseqs = null;
    int i = 0;
    SeqIdName nam;

    if (seqs.length > 0)
    {
      namedseqs = new SequenceI[seqs.length];
      do
      {
        nam = new SeqIdName(seqs[i].getName());

        if (names.containsKey(nam))
        {
          namedseqs[i] = findIdMatch(nam);
        }
        else
        {
          namedseqs[i] = null;
        }
      } while (++i < seqs.length);
    }

    return namedseqs;
  }

  /**
   * core findIdMatch search method
   * 
   * @param nam
   *          SeqIdName
   * @return SequenceI
   */
  private SequenceI findIdMatch(
          jalview.analysis.SequenceIdMatcher.SeqIdName nam)
  {
    Vector matches = new Vector();
    while (names.containsKey(nam))
    {
      matches.addElement(names.remove(nam));
    }
    return pickbestMatch(nam, matches);
  }

  /**
   * core findIdMatch search method for finding all equivalent matches
   * 
   * @param nam
   *          SeqIdName
   * @return SequenceI[]
   */
  private List<SequenceI> findAllIdMatches(
          jalview.analysis.SequenceIdMatcher.SeqIdName nam)
  {
    ArrayList<SequenceI> matches = new ArrayList<SequenceI>();
    while (names.containsKey(nam))
    {
      matches.add(names.remove(nam));
    }
    List<SequenceI> r = pickbestMatches(nam, matches);
    return r;
  }

  class SeqIdName
  {
    String id;

    SeqIdName(String s)
    {
      if (s != null)
      {
        id = s.toLowerCase(Locale.ROOT);
      }
      else
      {
        id = "";
      }
    }

    @Override
    public int hashCode()
    {
      return ((id.length() >= 4) ? id.substring(0, 4).hashCode()
              : id.hashCode());
    }

    @Override
    public boolean equals(Object s)
    {
      if (s == null)
      {
        return false;
      }
      if (s instanceof SeqIdName)
      {
        return this.stringequals(((SeqIdName) s).id);
      }
      else
      {
        if (s instanceof String)
        {
          return this.stringequals(((String) s).toLowerCase(Locale.ROOT));
        }
      }

      return false;
    }

    /**
     * Characters that define the end of a unique sequence ID at the beginning
     * of an arbitrary ID string JBPNote: This is a heuristic that will fail for
     * arbritrarily extended sequence id's (like portions of an aligned set of
     * repeats from one sequence)
     */
    private String WORD_SEP = "~. |#\\/<>!\"" + ((char) 0x00A4)
            + "$%^*)}[@',?_";

    /**
     * matches if one ID properly contains another at a whitespace boundary.
     * TODO: (JBPNote) These are not efficient. should use char[] for speed
     * todo: (JBPNote) Set separator characters appropriately
     * 
     * @param s
     * @return boolean
     */
    private boolean stringequals(String s)
    {
      if (id.length() > s.length())
      {
        return id.startsWith(s)
                ? (WORD_SEP.indexOf(id.charAt(s.length())) > -1)
                : false;
      }
      else
      {
        return s.startsWith(id)
                ? (s.equals(id) ? true
                        : (WORD_SEP.indexOf(s.charAt(id.length())) > -1))
                : false;
      }
    }

    /**
     * toString method returns the wrapped sequence id. For debugging purposes
     * only, behaviour not guaranteed not to change.
     */
    @Override
    public String toString()
    {
      return id;
    }
  }
}
