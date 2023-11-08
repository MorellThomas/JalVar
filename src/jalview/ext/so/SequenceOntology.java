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
package jalview.ext.so;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.biojava.nbio.ontology.Ontology;
import org.biojava.nbio.ontology.Term;
import org.biojava.nbio.ontology.Term.Impl;
import org.biojava.nbio.ontology.Triple;
import org.biojava.nbio.ontology.io.OboParser;
import org.biojava.nbio.ontology.utils.Annotation;

import jalview.bin.Console;
import jalview.io.gff.SequenceOntologyI;

/**
 * A wrapper class that parses the Sequence Ontology and exposes useful access
 * methods. This version uses the BioJava parser.
 */
public class SequenceOntology implements SequenceOntologyI
{
  /*
   * the parsed Ontology data as modelled by BioJava
   */
  private Ontology ontology;

  /*
   * the ontology term for the isA relationship
   */
  private Term isA;

  /*
   * lookup of terms by user readable name (NB not guaranteed unique)
   */
  private Map<String, Term> termsByDescription;

  /*
   * Map where key is a Term and value is a (possibly empty) list of 
   * all Terms to which the key has an 'isA' relationship, either
   * directly or indirectly (A isA B isA C)
   */
  private Map<Term, List<Term>> termIsA;

  private List<String> termsFound;

  private List<String> termsNotFound;

  /**
   * Package private constructor to enforce use of singleton. Parses and caches
   * the SO OBO data file.
   */
  public SequenceOntology()
  {
    termsFound = new ArrayList<String>();
    termsNotFound = new ArrayList<String>();
    termsByDescription = new HashMap<String, Term>();
    termIsA = new HashMap<Term, List<Term>>();

    loadOntologyZipFile("so-xp-simple.obo");
  }

  /**
   * Loads the given ontology file from a zip file with ".zip" appended
   * 
   * @param ontologyFile
   */
  protected void loadOntologyZipFile(String ontologyFile)
  {
    long now = System.currentTimeMillis();
    ZipInputStream zipStream = null;
    try
    {
      String zipFile = ontologyFile + ".zip";
      InputStream inStream = this.getClass()
              .getResourceAsStream("/" + zipFile);
      zipStream = new ZipInputStream(new BufferedInputStream(inStream));
      ZipEntry entry;
      while ((entry = zipStream.getNextEntry()) != null)
      {
        if (entry.getName().equals(ontologyFile))
        {
          loadOboFile(zipStream);
        }
      }
      long elapsed = System.currentTimeMillis() - now;
      Console.info("Loaded Sequence Ontology from " + zipFile + " ("
              + elapsed + "ms)");
    } catch (Exception e)
    {
      e.printStackTrace();
    } finally
    {
      closeStream(zipStream);
    }
  }

  /**
   * Closes the input stream, swallowing all exceptions
   * 
   * @param is
   */
  protected void closeStream(InputStream is)
  {
    if (is != null)
    {
      try
      {
        is.close();
      } catch (IOException e)
      {
        // ignore
      }
    }
  }

  /**
   * Reads, parses and stores the OBO file data
   * 
   * @param is
   * @throws ParseException
   * @throws IOException
   */
  protected void loadOboFile(InputStream is)
          throws ParseException, IOException
  {
    BufferedReader oboFile = new BufferedReader(new InputStreamReader(is));
    OboParser parser = new OboParser();
    ontology = parser.parseOBO(oboFile, "SO", "the SO ontology");
    isA = ontology.getTerm("is_a");
    storeTermNames();
  }

  /**
   * Stores a lookup table of terms by description. Note that description is not
   * guaranteed unique. Where duplicate descriptions are found, try to discard
   * the term that is flagged as obsolete. However we do store obsolete terms
   * where there is no duplication of description.
   */
  protected void storeTermNames()
  {
    for (Term term : ontology.getTerms())
    {
      if (term instanceof Impl)
      {
        String description = term.getDescription();
        if (description != null)
        {
          Term replaced = termsByDescription.get(description);
          if (replaced != null)
          {
            boolean newTermIsObsolete = isObsolete(term);
            boolean oldTermIsObsolete = isObsolete(replaced);
            if (newTermIsObsolete && !oldTermIsObsolete)
            {
              Console.debug("Ignoring " + term.getName()
                      + " as obsolete and duplicated by "
                      + replaced.getName());
              term = replaced;
            }
            else if (!newTermIsObsolete && oldTermIsObsolete)
            {
              Console.debug("Ignoring " + replaced.getName()
                      + " as obsolete and duplicated by " + term.getName());
            }
            else
            {
              Console.debug("Warning: " + term.getName() + " has replaced "
                      + replaced.getName() + " for lookup of '"
                      + description + "'");
            }
          }
          termsByDescription.put(description, term);
        }
      }
    }
  }

  /**
   * Answers true if the term has property "is_obsolete" with value true, else
   * false
   * 
   * @param term
   * @return
   */
  public static boolean isObsolete(Term term)
  {
    Annotation ann = term.getAnnotation();
    if (ann != null)
    {
      try
      {
        if (Boolean.TRUE.equals(ann.getProperty("is_obsolete")))
        {
          return true;
        }
      } catch (NoSuchElementException e)
      {
        // fall through to false
      }
    }
    return false;
  }

  /**
   * Test whether the given Sequence Ontology term is nucleotide_match (either
   * directly or via is_a relationship)
   * 
   * @param soTerm
   *          SO name or description
   * @return
   */
  public boolean isNucleotideMatch(String soTerm)
  {
    return isA(soTerm, NUCLEOTIDE_MATCH);
  }

  /**
   * Test whether the given Sequence Ontology term is protein_match (either
   * directly or via is_a relationship)
   * 
   * @param soTerm
   *          SO name or description
   * @return
   */
  public boolean isProteinMatch(String soTerm)
  {
    return isA(soTerm, PROTEIN_MATCH);
  }

  /**
   * Test whether the given Sequence Ontology term is polypeptide (either
   * directly or via is_a relationship)
   * 
   * @param soTerm
   *          SO name or description
   * @return
   */
  public boolean isPolypeptide(String soTerm)
  {
    return isA(soTerm, POLYPEPTIDE);
  }

  /**
   * Returns true if the given term has a (direct or indirect) 'isA'
   * relationship with the parent
   * 
   * @param child
   * @param parent
   * @return
   */
  @Override
  public boolean isA(String child, String parent)
  {
    if (child == null || parent == null)
    {
      return false;
    }
    /*
     * optimise trivial checks like isA("CDS", "CDS")
     */
    if (child.equals(parent))
    {
      termFound(child);
      return true;
    }

    Term childTerm = getTerm(child);
    if (childTerm != null)
    {
      termFound(child);
    }
    else
    {
      termNotFound(child);
    }
    Term parentTerm = getTerm(parent);

    return termIsA(childTerm, parentTerm);
  }

  /**
   * Records a valid term queried for, for reporting purposes
   * 
   * @param term
   */
  private void termFound(String term)
  {
    synchronized (termsFound)
    {
      if (!termsFound.contains(term))
      {
        termsFound.add(term);
      }
    }
  }

  /**
   * Records an invalid term queried for, for reporting purposes
   * 
   * @param term
   */
  private void termNotFound(String term)
  {
    synchronized (termsNotFound)
    {
      if (!termsNotFound.contains(term))
      {
        Console.error("SO term " + term + " invalid");
        termsNotFound.add(term);
      }
    }
  }

  /**
   * Returns true if the childTerm 'isA' parentTerm (directly or indirectly).
   * 
   * @param childTerm
   * @param parentTerm
   * @return
   */
  protected synchronized boolean termIsA(Term childTerm, Term parentTerm)
  {
    /*
     * null term could arise from a misspelled SO description
     */
    if (childTerm == null || parentTerm == null)
    {
      return false;
    }

    /*
     * recursive search endpoint:
     */
    if (childTerm == parentTerm)
    {
      return true;
    }

    /*
     * lazy initialisation - find all of a term's parents (recursively) 
     * the first time this is called, and save them in a map.
     */
    if (!termIsA.containsKey(childTerm))
    {
      findParents(childTerm);
    }

    List<Term> parents = termIsA.get(childTerm);
    for (Term parent : parents)
    {
      if (termIsA(parent, parentTerm))
      {
        /*
         * add (great-)grandparents to parents list as they are discovered,
         * for faster lookup next time
         */
        if (!parents.contains(parentTerm))
        {
          parents.add(parentTerm);
        }
        return true;
      }
    }

    return false;
  }

  /**
   * Finds all the 'isA' parents of the childTerm and stores them as a (possibly
   * empty) list.
   * 
   * @param childTerm
   */
  protected synchronized void findParents(Term childTerm)
  {
    List<Term> result = new ArrayList<Term>();
    for (Triple triple : ontology.getTriples(childTerm, null, isA))
    {
      Term parent = triple.getObject();
      result.add(parent);

      /*
       * and search for the parent's parents recursively
       */
      findParents(parent);
    }
    termIsA.put(childTerm, result);
  }

  /**
   * Returns the Term for a given name (e.g. "SO:0000735") or description (e.g.
   * "sequence_location"), or null if not found.
   * 
   * @param child
   * @return
   */
  protected Term getTerm(String nameOrDescription)
  {
    Term t = termsByDescription.get(nameOrDescription);
    if (t == null)
    {
      try
      {
        t = ontology.getTerm(nameOrDescription);
      } catch (NoSuchElementException e)
      {
        // not found
      }
    }
    return t;
  }

  public boolean isSequenceVariant(String soTerm)
  {
    return isA(soTerm, SEQUENCE_VARIANT);
  }

  /**
   * Sorts (case-insensitive) and returns the list of valid terms queried for
   */
  @Override
  public List<String> termsFound()
  {
    synchronized (termsFound)
    {
      Collections.sort(termsFound, String.CASE_INSENSITIVE_ORDER);
      return termsFound;
    }
  }

  /**
   * Sorts (case-insensitive) and returns the list of invalid terms queried for
   */
  @Override
  public List<String> termsNotFound()
  {
    synchronized (termsNotFound)
    {
      Collections.sort(termsNotFound, String.CASE_INSENSITIVE_ORDER);
      return termsNotFound;
    }
  }
}
