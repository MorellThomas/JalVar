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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.stevesoft.pat.Regex;

import jalview.api.AlignViewportI;
import jalview.api.FeatureRenderer;
import jalview.api.FinderI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeaturesI;
import jalview.util.Comparison;
import jalview.util.MapList;

/**
 * Implements the search algorithm for the Find dialog
 */
public class Finder implements FinderI
{
  /*
   * matched residue locations
   */
  private SearchResultsI searchResults;

  /*
   * sequences matched by id or description
   */
  private List<SequenceI> idMatches;

  /*
   * the viewport to search over
   */
  private AlignViewportI viewport;

  /*
   * feature renderer model - if available
   */
  FeatureRenderer frm = null;

  /*
   * sequence index in alignment to search from
   */
  private int sequenceIndex;

  /*
   * position offset in sequence to search from, base 0
   * (position after start of last match for a 'find next')
   */
  private int residueIndex;

  /*
   * last feature matched when incrementally searching sequence features
   */
  private SequenceFeature lastFeature;

  /*
   * last sequenceIndex used when lastFeature was discovered
   */
  private int lastFeatureSequenceIndex;

  /*
   * the true sequence position of the start of the 
   * last sequence searched (when 'ignore hidden regions' does not apply)
   */
  private int searchedSequenceStartPosition;

  /*
   * when 'ignore hidden regions' applies, this holds the mapping from
   * the visible sequence positions (1, 2, ...) to true sequence positions
   */
  private MapList searchedSequenceMap;

  private String seqToSearch;

  /**
   * Constructor for searching a viewport
   * 
   * @param av
   */
  public Finder(AlignViewportI av)
  {
    this.viewport = av;
    this.sequenceIndex = 0;
    this.residueIndex = -1;
  }

  @Override
  public void findAll(String theSearchString, boolean matchCase,
          boolean searchDescription, boolean searchFeatureDesc,
          boolean ignoreHidden)
  {
    /*
     * search from the start
     */
    lastFeature = null;
    lastFeatureSequenceIndex = 0;
    sequenceIndex = 0;
    residueIndex = -1;

    doFind(theSearchString, matchCase, searchDescription, searchFeatureDesc,
            true, ignoreHidden);

    /*
     * reset to start for next search
     */
    sequenceIndex = 0;
    residueIndex = -1;
    lastFeature = null;
    lastFeatureSequenceIndex = 0;
  }

  @Override
  public void findNext(String theSearchString, boolean matchCase,
          boolean searchDescription, boolean searchFeatureDesc,
          boolean ignoreHidden)
  {
    doFind(theSearchString, matchCase, searchDescription, searchFeatureDesc,
            false, ignoreHidden);

    if (searchResults.isEmpty() && idMatches.isEmpty())
    {
      /*
       * search failed - reset to start for next search
       */
      sequenceIndex = 0;
      residueIndex = -1;
      lastFeature = null;
      lastFeatureSequenceIndex = 0;
    }
  }

  /**
   * Performs a 'find next' or 'find all'
   * 
   * @param theSearchString
   * @param matchCase
   * @param searchDescription
   * @param findAll
   * @param ignoreHidden
   */
  protected void doFind(String theSearchString, boolean matchCase,
          boolean searchDescription, boolean searchFeatureDesc,
          boolean findAll, boolean ignoreHidden)
  {
    searchResults = new SearchResults();
    idMatches = new ArrayList<>();

    String searchString = matchCase ? theSearchString
            : theSearchString.toUpperCase(Locale.ROOT);
    Regex searchPattern = new Regex(searchString);
    searchPattern.setIgnoreCase(!matchCase);

    SequenceGroup selection = viewport.getSelectionGroup();
    if (selection != null && selection.getSize() < 1)
    {
      selection = null; // ? ignore column-only selection
    }

    AlignmentI alignment = viewport.getAlignment();
    int end = alignment.getHeight();

    getSequence(ignoreHidden);

    boolean found = false;
    while ((!found || findAll) && sequenceIndex < end)
    {
      found = findNextMatch(searchString, searchPattern, searchDescription,
              searchFeatureDesc, ignoreHidden);
    }
  }

  /**
   * Calculates and saves the sequence string to search. The string is
   * restricted to the current selection region if there is one, and is saved
   * with all gaps removed.
   * <p>
   * If there are hidden columns, and option {@ignoreHidden} is selected, then
   * only visible positions of the sequence are included, and a mapping is also
   * constructed from the returned string positions to the true sequence
   * positions.
   * <p>
   * Note we have to do this each time {@code findNext} or {@code findAll} is
   * called, in case the alignment, selection group or hidden columns have
   * changed. In particular, if the sequence at offset {@code sequenceIndex} in
   * the alignment is (no longer) in the selection group, search is advanced to
   * the next sequence that is.
   * <p>
   * Sets sequence string to the empty string if there are no more sequences (in
   * selection group if any) at or after {@code sequenceIndex}.
   * <p>
   * Returns true if a sequence could be found, false if end of alignment was
   * reached
   * 
   * @param ignoreHidden
   * @return
   */
  private boolean getSequence(boolean ignoreHidden)
  {
    AlignmentI alignment = viewport.getAlignment();
    if (sequenceIndex >= alignment.getHeight())
    {
      seqToSearch = "";
      return false;
    }
    SequenceI seq = alignment.getSequenceAt(sequenceIndex);
    SequenceGroup selection = viewport.getSelectionGroup();
    if (selection != null && !selection.contains(seq))
    {
      if (!nextSequence(ignoreHidden))
      {
        return false;
      }
      seq = alignment.getSequenceAt(sequenceIndex);
    }

    String seqString = null;
    if (ignoreHidden)
    {
      seqString = getVisibleSequence(seq);
      this.searchedSequenceStartPosition = 1;
    }
    else
    {
      int startCol = 0;
      int endCol = seq.getLength() - 1;
      this.searchedSequenceStartPosition = seq.getStart();
      if (selection != null)
      {
        startCol = selection.getStartRes();
        endCol = Math.min(endCol, selection.getEndRes());
        this.searchedSequenceStartPosition = seq.findPosition(startCol);
      }
      seqString = seq.getSequenceAsString(startCol, endCol + 1);
    }

    /*
     * remove gaps; note that even if this leaves an empty string, we 'search'
     * the sequence anyway (for possible match on name or description)
     */
    String ungapped = AlignSeq.extractGaps(Comparison.GapChars, seqString);
    this.seqToSearch = ungapped;

    return true;
  }

  /**
   * Returns a string consisting of only the visible residues of {@code seq}
   * from alignment column {@ fromColumn}, restricted to the current selection
   * region if there is one.
   * <p>
   * As a side-effect, also computes the mapping from the true sequence
   * positions to the positions (1, 2, ...) of the returned sequence. This is to
   * allow search matches in the visible sequence to be converted to sequence
   * positions.
   * 
   * @param seq
   * @return
   */
  private String getVisibleSequence(SequenceI seq)
  {
    /*
     * get start / end columns of sequence and convert to base 0
     * (so as to match the visible column ranges)
     */
    int seqStartCol = seq.findIndex(seq.getStart()) - 1;
    int seqEndCol = seq.findIndex(seq.getStart() + seq.getLength() - 1) - 1;
    Iterator<int[]> visibleColumns = viewport.getViewAsVisibleContigs(true);
    StringBuilder visibleSeq = new StringBuilder(seqEndCol - seqStartCol);
    List<int[]> fromRanges = new ArrayList<>();

    while (visibleColumns.hasNext())
    {
      int[] range = visibleColumns.next();
      if (range[0] > seqEndCol)
      {
        // beyond the end of the sequence
        break;
      }
      if (range[1] < seqStartCol)
      {
        // before the start of the sequence
        continue;
      }
      String subseq = seq.getSequenceAsString(range[0], range[1] + 1);
      String ungapped = AlignSeq.extractGaps(Comparison.GapChars, subseq);
      visibleSeq.append(ungapped);
      if (!ungapped.isEmpty())
      {
        /*
         * visible region includes at least one non-gap character,
         * so add the range to the mapping being constructed
         */
        int seqResFrom = seq.findPosition(range[0]);
        int seqResTo = seqResFrom + ungapped.length() - 1;
        fromRanges.add(new int[] { seqResFrom, seqResTo });
      }
    }

    /*
     * construct the mapping
     * from: visible sequence positions 1..length
     * to:   true residue positions of the alignment sequence
     */
    List<int[]> toRange = Arrays
            .asList(new int[]
            { 1, visibleSeq.length() });
    searchedSequenceMap = new MapList(fromRanges, toRange, 1, 1);

    return visibleSeq.toString();
  }

  /**
   * Advances the search to the next sequence in the alignment. Sequences not in
   * the current selection group (if there is one) are skipped. The
   * (sub-)sequence to be searched is extracted, gaps removed, and saved, or set
   * to null if there are no more sequences to search.
   * <p>
   * Returns true if a sequence could be found, false if end of alignment was
   * reached
   * 
   * @param ignoreHidden
   */
  private boolean nextSequence(boolean ignoreHidden)
  {
    sequenceIndex++;
    residueIndex = -1;

    return getSequence(ignoreHidden);
  }

  /**
   * Finds the next match in the given sequence, starting at offset
   * {@code residueIndex}. Answers true if a match is found, else false.
   * <p>
   * If a match is found, {@code residueIndex} is advanced to the position after
   * the start of the matched region, ready for the next search.
   * <p>
   * If no match is found, {@code sequenceIndex} is advanced ready to search the
   * next sequence.
   * 
   * @param seqToSearch
   * @param searchString
   * @param searchPattern
   * @param matchDescription
   * @param ignoreHidden
   * @return
   */
  protected boolean findNextMatch(String searchString, Regex searchPattern,
          boolean matchDescription, boolean matchFeatureDesc,
          boolean ignoreHidden)
  {
    if (residueIndex < 0)
    {
      /*
       * at start of sequence; try find by residue number, in sequence id,
       * or (optionally) in sequence description
       */
      if (doNonMotifSearches(searchString, searchPattern, matchDescription))
      {
        return true;
      }
    }

    /*
     * search for next match in sequence string
     */
    int end = seqToSearch.length();
    while (residueIndex < end)
    {
      boolean matched = searchPattern.searchFrom(seqToSearch, residueIndex);
      if (matched)
      {
        if (recordMatch(searchPattern, ignoreHidden))
        {
          return true;
        }
      }
      else
      {
        if (matchFeatureDesc)
        {
          matched = searchSequenceFeatures(residueIndex, searchPattern);
          if (matched)
          {
            return true;
          }
          lastFeature = null;
        }
        residueIndex = Integer.MAX_VALUE;
      }
    }

    nextSequence(ignoreHidden);
    return false;
  }

  /**
   * Adds the match held in the <code>searchPattern</code> Regex to the
   * <code>searchResults</code>, unless it is a subregion of the last match
   * recorded. <code>residueIndex</code> is advanced to the position after the
   * start of the matched region, ready for the next search. Answers true if a
   * match was added, else false.
   * <p>
   * Matches that lie entirely within hidden regions of the alignment are not
   * added.
   * 
   * @param searchPattern
   * @param ignoreHidden
   * @return
   */
  protected boolean recordMatch(Regex searchPattern, boolean ignoreHidden)
  {
    SequenceI seq = viewport.getAlignment().getSequenceAt(sequenceIndex);

    /*
     * convert start/end of the match to sequence coordinates
     */
    int offset = searchPattern.matchedFrom();
    int matchStartPosition = this.searchedSequenceStartPosition + offset;
    int matchEndPosition = matchStartPosition + searchPattern.charsMatched()
            - 1;

    /*
     * update residueIndex to next position after the start of the match
     * (findIndex returns a value base 1, columnIndex is held base 0)
     */
    residueIndex = searchPattern.matchedFrom() + 1;

    /*
     * return false if the match is entirely in a hidden region
     */
    if (allHidden(seq, matchStartPosition, matchEndPosition))
    {
      return false;
    }

    /*
     * check that this match is not a subset of the previous one (JAL-2302)
     */
    List<SearchResultMatchI> matches = searchResults.getResults();
    SearchResultMatchI lastMatch = matches.isEmpty() ? null
            : matches.get(matches.size() - 1);

    if (lastMatch == null || !lastMatch.contains(seq, matchStartPosition,
            matchEndPosition))
    {
      addMatch(seq, matchStartPosition, matchEndPosition, ignoreHidden);
      return true;
    }

    return false;
  }

  /**
   * Adds one match to the stored list. If hidden residues are being skipped,
   * then the match may need to be split into contiguous positions of the
   * sequence (so it does not include skipped residues).
   * 
   * @param seq
   * @param matchStartPosition
   * @param matchEndPosition
   * @param ignoreHidden
   */
  private void addMatch(SequenceI seq, int matchStartPosition,
          int matchEndPosition, boolean ignoreHidden)
  {
    if (!ignoreHidden)
    {
      /*
       * simple case
       */
      searchResults.addResult(seq, matchStartPosition, matchEndPosition);
      return;
    }

    /*
     *  get start-end contiguous ranges in underlying sequence
     */
    int[] truePositions = searchedSequenceMap
            .locateInFrom(matchStartPosition, matchEndPosition);
    searchResults.addResult(seq, truePositions);
  }

  /**
   * Returns true if all residues are hidden, else false
   * 
   * @param seq
   * @param fromPos
   * @param toPos
   * @return
   */
  private boolean allHidden(SequenceI seq, int fromPos, int toPos)
  {
    if (!viewport.hasHiddenColumns())
    {
      return false;
    }
    for (int res = fromPos; res <= toPos; res++)
    {
      if (isVisible(seq, res))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Does searches other than for residue patterns. Currently this includes
   * <ul>
   * <li>find residue by position (if search string is a number)</li>
   * <li>match search string to sequence id</li>
   * <li>match search string to sequence description (optional)</li>
   * </ul>
   * Answers true if a match is found, else false.
   * 
   * @param searchString
   * @param searchPattern
   * @param includeDescription
   * @return
   */
  protected boolean doNonMotifSearches(String searchString,
          Regex searchPattern, boolean includeDescription)
  {
    SequenceI seq = viewport.getAlignment().getSequenceAt(sequenceIndex);

    /*
     * position sequence search to start of sequence
     */
    residueIndex = 0;
    try
    {
      int res = Integer.parseInt(searchString);
      return searchForResidueNumber(seq, res);
    } catch (NumberFormatException ex)
    {
      // search pattern is not a number
    }

    if (searchSequenceName(seq, searchPattern))
    {
      return true;
    }
    if (includeDescription && searchSequenceDescription(seq, searchPattern))
    {
      return true;
    }
    return false;
  }

  /**
   * Searches for a match with the sequence features, and if found, adds the
   * sequence to the list of match ids, (but not as a duplicate). Answers true
   * if a match was added, else false.
   * 
   * @param seq
   * @param searchPattern
   * @return
   */
  protected boolean searchSequenceFeatures(int from, Regex searchPattern)
  {
    if (lastFeatureSequenceIndex != sequenceIndex)
    {
      lastFeatureSequenceIndex = sequenceIndex;
      lastFeature = null;
    }
    SequenceI seq = viewport.getAlignment().getSequenceAt(sequenceIndex);
    SequenceFeaturesI sf = seq.getFeatures();

    // TODO - stash feature list and search incrementally
    List<SequenceFeature> allFeatures = null;
    if (frm != null)
    {
      allFeatures = frm.findFeaturesAtResidue(seq, seq.getStart(),
              seq.getEnd());
    }
    else
    {
      //allFeatures = sf.getAllFeatures(null);
      allFeatures = sf.getAllFeatures();
    }
    // so we can check we are advancing when debugging
    long fpos = 0;

    for (SequenceFeature feature : allFeatures)
    {
      fpos++;
      if (lastFeature != null)
      {
        // iterate till we find last feature matched
        if (lastFeature != feature)
        {
          continue;
        }
        else
        {
          lastFeature = null;
          continue;
        }
      }

      if (searchPattern.search(feature.type) || (feature.description != null
              && searchPattern.search(feature.description)))
      {
        searchResults.addResult(seq, feature.getBegin(), feature.getEnd());
        lastFeature = feature;
        return true;
      }
    }
    residueIndex = Integer.MAX_VALUE;
    lastFeature = null;
    return false;
  }

  /**
   * Searches for a match with the sequence description, and if found, adds the
   * sequence to the list of match ids (but not as a duplicate). Answers true if
   * a match was added, else false.
   * 
   * @param seq
   * @param searchPattern
   * @return
   */
  protected boolean searchSequenceDescription(SequenceI seq,
          Regex searchPattern)
  {
    String desc = seq.getDescription();
    if (desc != null && searchPattern.search(desc)
            && !idMatches.contains(seq))
    {
      idMatches.add(seq);
      return true;
    }
    return false;
  }

  /**
   * Searches for a match with the sequence name, and if found, adds the
   * sequence to the list of match ids (but not as a duplicate). Answers true if
   * a match was added, else false.
   * 
   * @param seq
   * @param searchPattern
   * @return
   */
  protected boolean searchSequenceName(SequenceI seq, Regex searchPattern)
  {
    if (searchPattern.search(seq.getName()) && !idMatches.contains(seq))
    {
      idMatches.add(seq);
      return true;
    }
    return false;
  }

  /**
   * If the residue position is valid for the sequence, and in a visible column,
   * adds the position to the search results and returns true, else answers
   * false.
   * 
   * @param seq
   * @param resNo
   * @return
   */
  protected boolean searchForResidueNumber(SequenceI seq, int resNo)
  {
    if (seq.getStart() <= resNo && seq.getEnd() >= resNo)
    {
      if (isVisible(seq, resNo))
      {
        searchResults.addResult(seq, resNo, resNo);
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the residue is in a visible column, else false
   * 
   * @param seq
   * @param res
   * @return
   */
  private boolean isVisible(SequenceI seq, int res)
  {
    if (!viewport.hasHiddenColumns())
    {
      return true;
    }
    int col = seq.findIndex(res); // base 1
    return viewport.getAlignment().getHiddenColumns().isVisible(col - 1); // base
                                                                          // 0
  }

  @Override
  public List<SequenceI> getIdMatches()
  {
    return idMatches;
  }

  @Override
  public SearchResultsI getSearchResults()
  {
    return searchResults;
  }

  @Override
  public void setFeatureRenderer(FeatureRenderer featureRenderer)
  {
    frm = featureRenderer;
  }
}
