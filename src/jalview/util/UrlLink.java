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
package jalview.util;

import static jalview.util.UrlConstants.DB_ACCESSION;
import static jalview.util.UrlConstants.DELIM;
import static jalview.util.UrlConstants.SEP;
import static jalview.util.UrlConstants.SEQUENCE_ID;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceI;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A helper class to parse URL Link strings taken from applet parameters or
 * jalview properties file using the com.stevesoft.pat.Regex implementation.
 * Jalview 2.4 extension allows regular expressions to be used to parse ID
 * strings and replace the result in the URL. Regex's operate on the whole ID
 * string given to the matchURL method, if no regex is supplied, then only text
 * following the first pipe symbol will be substituted. Usage documentation
 * todo.
 */
public class UrlLink
{
  private static final String SEQUENCEID_PLACEHOLDER = DELIM + SEQUENCE_ID
          + DELIM;

  private static final String ACCESSION_PLACEHOLDER = DELIM + DB_ACCESSION
          + DELIM;

  /**
   * A comparator that puts SEQUENCE_ID template links before DB_ACCESSION
   * links, and otherwise orders by link name + url (not case sensitive). It
   * expects to compare strings formatted as "Name|URLTemplate" where the
   * template may include $SEQUENCE_ID$ or $DB_ACCESSION$ or neither.
   */
  public static final Comparator<String> LINK_COMPARATOR = new Comparator<String>()
  {
    @Override
    public int compare(String link1, String link2)
    {
      if (link1 == null || link2 == null)
      {
        return 0; // for failsafe only
      }
      if (link1.contains(SEQUENCEID_PLACEHOLDER)
              && link2.contains(ACCESSION_PLACEHOLDER))
      {
        return -1;
      }
      if (link2.contains(SEQUENCEID_PLACEHOLDER)
              && link1.contains(ACCESSION_PLACEHOLDER))
      {
        return 1;
      }
      return String.CASE_INSENSITIVE_ORDER.compare(link1, link2);
    }
  };

  private static final String EQUALS = "=";

  private static final String SPACE = " ";

  private String urlSuffix;

  private String urlPrefix;

  private String target;

  private String label;

  private String dbname;

  private String regexReplace;

  private boolean dynamic = false;

  private boolean usesDBaccession = false;

  private String invalidMessage = null;

  /**
   * parse the given linkString of the form '<label>SEP<url>' into parts url may
   * contain a string $SEQUENCE_ID<=optional regex=>$ where <=optional regex=>
   * must be of the form =/<perl style regex>/=$
   * 
   * @param link
   */
  public UrlLink(String link)
  {
    int sep = link.indexOf(SEP);
    int psqid = link.indexOf(DELIM + DB_ACCESSION);
    int nsqid = link.indexOf(DELIM + SEQUENCE_ID);
    if (psqid > -1)
    {
      dynamic = true;
      usesDBaccession = true;

      sep = parseLabel(sep, psqid, link);

      int endOfRegex = parseUrl(link, DB_ACCESSION, psqid, sep);
      parseTarget(link, sep, endOfRegex);
    }
    else if (nsqid > -1)
    {
      dynamic = true;
      sep = parseLabel(sep, nsqid, link);

      int endOfRegex = parseUrl(link, SEQUENCE_ID, nsqid, sep);

      parseTarget(link, sep, endOfRegex);
    }
    else
    {
      label = link.substring(0, sep).trim();

      // if there's a third element in the url link string
      // it is the target name, otherwise target=label
      int lastsep = link.lastIndexOf(SEP);
      if (lastsep != sep)
      {
        urlPrefix = link.substring(sep + 1, lastsep).trim();
        target = link.substring(lastsep + 1).trim();
      }
      else
      {
        urlPrefix = link.substring(sep + 1).trim();
        target = label;
      }

      regexReplace = null; // implies we trim any prefix if necessary //
      urlSuffix = null;
    }

    label = label.trim();
    target = target.trim();
  }

  /**
   * Alternative constructor for separate name, link and description
   * 
   * @param name
   *          The string used to match the link to a DB reference id
   * @param url
   *          The url to link to
   * @param desc
   *          The description of the associated target DB
   */
  public UrlLink(String name, String url, String desc)
  {
    this(name + SEP + url + SEP + desc);
  }

  /**
   * @return the url_suffix
   */
  public String getUrlSuffix()
  {
    return urlSuffix;
  }

  /**
   * @return the url_prefix
   */
  public String getUrlPrefix()
  {
    return urlPrefix;
  }

  /**
   * @return the target
   */
  public String getTarget()
  {
    return target;
  }

  /**
   * @return the label
   */
  public String getLabel()
  {
    return label;
  }

  public String getUrlWithToken()
  {
    String var = (usesDBaccession ? DB_ACCESSION : SEQUENCE_ID);

    return urlPrefix
            + (dynamic
                    ? (DELIM + var
                            + ((regexReplace != null)
                                    ? EQUALS + regexReplace + EQUALS + DELIM
                                    : DELIM))
                    : "")
            + ((urlSuffix == null) ? "" : urlSuffix);
  }

  /**
   * @return the regexReplace
   */
  public String getRegexReplace()
  {
    return regexReplace;
  }

  /**
   * @return the invalidMessage
   */
  public String getInvalidMessage()
  {
    return invalidMessage;
  }

  /**
   * Check if URL string was parsed properly.
   * 
   * @return boolean - if false then <code>getInvalidMessage</code> returns an
   *         error message
   */
  public boolean isValid()
  {
    return invalidMessage == null;
  }

  /**
   * 
   * @return whether link is dynamic
   */
  public boolean isDynamic()
  {
    return dynamic;
  }

  /**
   * 
   * @return whether link uses DB Accession id
   */
  public boolean usesDBAccession()
  {
    return usesDBaccession;
  }

  /**
   * Set the label
   * 
   * @param newlabel
   */
  public void setLabel(String newlabel)
  {
    this.label = newlabel;
  }

  /**
   * Set the target
   * 
   * @param desc
   */
  public void setTarget(String desc)
  {
    target = desc;
  }

  /**
   * return one or more URL strings by applying regex to the given idstring
   * 
   * @param idstring
   * @param onlyIfMatches
   *          - when true url strings are only made if regex is defined and
   *          matches
   * @return String[] { part of idstring substituted, full substituted url , ..
   *         next part, next url..}
   */
  public String[] makeUrls(String idstring, boolean onlyIfMatches)
  {
    if (dynamic)
    {
      if (regexReplace != null)
      {
        com.stevesoft.pat.Regex rg = com.stevesoft.pat.Regex
                .perlCode("/" + regexReplace + "/");
        if (rg.search(idstring))
        {
          int ns = rg.numSubs();
          if (ns == 0)
          {
            // take whole regex
            return new String[] { rg.stringMatched(),
                urlPrefix + rg.stringMatched() + urlSuffix };
          } /*
             * else if (ns==1) { // take only subgroup match return new String[]
             * { rg.stringMatched(1), url_prefix+rg.stringMatched(1)+url_suffix
             * }; }
             */
          else
          {
            // debug
            for (int s = 0; s <= rg.numSubs(); s++)
            {
              System.err.println("Sub " + s + " : " + rg.matchedFrom(s)
                      + " : " + rg.matchedTo(s) + " : '"
                      + rg.stringMatched(s) + "'");
            }
            // try to collate subgroup matches
            Vector<String> subs = new Vector<>();
            // have to loop through submatches, collating them at top level
            // match
            int s = 0; // 1;
            while (s <= ns)
            {
              if (s + 1 <= ns && rg.matchedTo(s) > -1
                      && rg.matchedTo(s + 1) > -1
                      && rg.matchedTo(s + 1) < rg.matchedTo(s))
              {
                // s is top level submatch. search for submatches enclosed by
                // this one
                int r = s + 1;
                String mtch = "";
                while (r <= ns && rg.matchedTo(r) <= rg.matchedTo(s))
                {
                  if (rg.matchedFrom(r) > -1)
                  {
                    mtch += rg.stringMatched(r);
                  }
                  r++;
                }
                if (mtch.length() > 0)
                {
                  subs.addElement(mtch);
                  subs.addElement(urlPrefix + mtch + urlSuffix);
                }
                s = r;
              }
              else
              {
                if (rg.matchedFrom(s) > -1)
                {
                  subs.addElement(rg.stringMatched(s));
                  subs.addElement(
                          urlPrefix + rg.stringMatched(s) + urlSuffix);
                }
                s++;
              }
            }

            String[] res = new String[subs.size()];
            for (int r = 0, rs = subs.size(); r < rs; r++)
            {
              res[r] = subs.elementAt(r);
            }
            subs.removeAllElements();
            return res;
          }
        }
        if (onlyIfMatches)
        {
          return null;
        }
      }
      /* Otherwise - trim off any 'prefix' - pre 2.4 Jalview behaviour */
      if (idstring.indexOf(SEP) > -1)
      {
        idstring = idstring.substring(idstring.lastIndexOf(SEP) + 1);
      }

      // just return simple url substitution.
      return new String[] { idstring, urlPrefix + idstring + urlSuffix };
    }
    else
    {
      return new String[] { "", urlPrefix };
    }
  }

  @Override
  public String toString()
  {
    return label + SEP + getUrlWithToken();
  }

  /**
   * @return delimited string containing label, url and target
   */
  public String toStringWithTarget()
  {
    return label + SEP + getUrlWithToken() + SEP + target;
  }

  /**
   * Parse the label from the link string
   * 
   * @param firstSep
   *          Location of first occurrence of separator in link string
   * @param psqid
   *          Position of sequence id or name in link string
   * @param link
   *          Link string containing database name and url
   * @return Position of last separator symbol prior to any regex symbols
   */
  protected int parseLabel(int firstSep, int psqid, String link)
  {
    int p = firstSep;
    int sep = firstSep;
    do
    {
      sep = p;
      p = link.indexOf(SEP, sep + 1);
    } while (p > sep && p < psqid);
    // Assuming that the URL itself does not contain any SEP symbols
    // sep now contains last pipe symbol position prior to any regex symbols
    label = link.substring(0, sep);

    return sep;
  }

  /**
   * Parse the target from the link string
   * 
   * @param link
   *          Link string containing database name and url
   * @param sep
   *          Location of first separator symbol
   * @param endOfRegex
   *          Location of end of any regular expression in link string
   */
  protected void parseTarget(String link, int sep, int endOfRegex)
  {
    int lastsep = link.lastIndexOf(SEP);

    if ((lastsep != sep) && (lastsep > endOfRegex))
    {
      // final element in link string is the target
      target = link.substring(lastsep + 1).trim();
    }
    else
    {
      target = label;
    }

    if (target.indexOf(SEP) > -1)
    {
      // SEP terminated database name / www target at start of Label
      target = target.substring(0, target.indexOf(SEP));
    }
    else if (target.indexOf(SPACE) > 2)
    {
      // space separated label - first word matches database name
      target = target.substring(0, target.indexOf(SPACE));
    }
  }

  /**
   * Parse the URL part of the link string
   * 
   * @param link
   *          Link string containing database name and url
   * @param varName
   *          Name of variable in url string (e.g. SEQUENCE_ID, SEQUENCE_NAME)
   * @param sqidPos
   *          Position of id or name in link string
   * @param sep
   *          Position of separator in link string
   * @return Location of end of any regex in link string
   */
  protected int parseUrl(String link, String varName, int sqidPos, int sep)
  {
    urlPrefix = link.substring(sep + 1, sqidPos).trim();

    // delimiter at start of regex: e.g. $SEQUENCE_ID=/
    String startDelimiter = DELIM + varName + "=/";

    // delimiter at end of regex: /=$
    String endDelimiter = "/=" + DELIM;

    int startLength = startDelimiter.length();

    // Parse URL : Whole URL string first
    int p = link.indexOf(endDelimiter, sqidPos + startLength);

    if (link.indexOf(startDelimiter) == sqidPos
            && (p > sqidPos + startLength))
    {
      // Extract Regex and suffix
      urlSuffix = link.substring(p + endDelimiter.length());
      regexReplace = link.substring(sqidPos + startLength, p);
      try
      {
        com.stevesoft.pat.Regex rg = com.stevesoft.pat.Regex
                .perlCode("/" + regexReplace + "/");
        if (rg == null)
        {
          invalidMessage = "Invalid Regular Expression : '" + regexReplace
                  + "'\n";
        }
      } catch (Exception e)
      {
        invalidMessage = "Invalid Regular Expression : '" + regexReplace
                + "'\n";
      }
    }
    else
    {
      // no regex
      regexReplace = null;
      // verify format is really correct.
      if (link.indexOf(DELIM + varName + DELIM) == sqidPos)
      {
        int lastsep = link.lastIndexOf(SEP);
        if (lastsep < sqidPos + startLength - 1)
        {
          // the last SEP character was before the regex, ignore
          lastsep = link.length();
        }
        urlSuffix = link.substring(sqidPos + startLength - 1, lastsep)
                .trim();
        regexReplace = null;
      }
      else
      {
        invalidMessage = "Warning: invalid regex structure for URL link : "
                + link;
      }
    }

    return p;
  }

  /**
   * Create a set of URL links for a sequence
   * 
   * @param seq
   *          The sequence to create links for
   * @param linkset
   *          Map of links: key = id + SEP + link, value = [target, label, id,
   *          link]
   */
  public void createLinksFromSeq(final SequenceI seq,
          Map<String, List<String>> linkset)
  {
    if (seq != null && dynamic)
    {
      createDynamicLinks(seq, linkset);
    }
    else
    {
      createStaticLink(linkset);
    }
  }

  /**
   * Create a static URL link
   * 
   * @param linkset
   *          Map of links: key = id + SEP + link, value = [target, label, id,
   *          link]
   */
  protected void createStaticLink(Map<String, List<String>> linkset)
  {
    if (!linkset.containsKey(label + SEP + getUrlPrefix()))
    {
      // Add a non-dynamic link
      linkset.put(label + SEP + getUrlPrefix(),
              Arrays.asList(target, label, null, getUrlPrefix()));
    }
  }

  /**
   * Create dynamic URL links
   * 
   * @param seq
   *          The sequence to create links for
   * @param linkset
   *          Map of links: key = id + SEP + link, value = [target, label, id,
   *          link]
   */
  protected void createDynamicLinks(final SequenceI seq,
          Map<String, List<String>> linkset)
  {
    // collect id string too
    String id = seq.getName();
    String descr = seq.getDescription();
    if (descr != null && descr.length() < 1)
    {
      descr = null;
    }

    if (usesDBAccession()) // link is ID
    {
      // collect matching db-refs
      List<DBRefEntry> dbr = DBRefUtils.selectRefs(seq.getDBRefs(),
              new String[]
              { target });

      // if there are any dbrefs which match up with the link
      if (dbr != null)
      {
        for (int r = 0, nd = dbr.size(); r < nd; r++)
        {
          // create Bare ID link for this URL
          createBareURLLink(dbr.get(r).getAccessionId(), true, linkset);
        }
      }
    }
    else if (!usesDBAccession() && id != null) // link is name
    {
      // create Bare ID link for this URL
      createBareURLLink(id, false, linkset);
    }

    // Create urls from description but only for URL links which are regex
    // links
    if (descr != null && getRegexReplace() != null)
    {
      // create link for this URL from description where regex matches
      createBareURLLink(descr, false, linkset);
    }
  }

  /*
   * Create a bare URL Link
   * Returns map where key = id + SEP + link, and value = [target, label, id, link]
   */
  protected void createBareURLLink(String id, Boolean combineLabel,
          Map<String, List<String>> linkset)
  {
    String[] urls = makeUrls(id, true);
    if (urls != null)
    {
      for (int u = 0; u < urls.length; u += 2)
      {
        if (!linkset.containsKey(urls[u] + SEP + urls[u + 1]))
        {
          String thisLabel = label;
          if (combineLabel)
          {
            // incorporate label with idstring
            thisLabel = label + SEP + urls[u];
          }

          linkset.put(urls[u] + SEP + urls[u + 1],
                  Arrays.asList(target, thisLabel, urls[u], urls[u + 1]));
        }
      }
    }
  }

  /**
   * Sorts links (formatted as LinkName|LinkPattern) suitable for display in a
   * menu
   * <ul>
   * <li>SEQUENCE_ID links precede DB_ACCESSION links (i.e. canonical lookup
   * before cross-references)</li>
   * <li>otherwise by Link name (case insensitive)</li>
   * </ul>
   * 
   * @param nlinks
   */
  public static void sort(List<String> nlinks)
  {
    Collections.sort(nlinks, LINK_COMPARATOR);
  }
}
