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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jalview.analysis.AlignmentSorter;
import jalview.api.AlignViewportI;
import jalview.bin.Console;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.EditCommand.Edit;
import jalview.commands.OrderCommand;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignedCodonFrame.SequenceToSequenceMapping;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentOrder;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Mapping;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;

/**
 * Helper methods for manipulations involving sequence mappings.
 * 
 * @author gmcarstairs
 *
 */
public final class MappingUtils
{

  /**
   * Helper method to map a CUT or PASTE command.
   * 
   * @param edit
   *          the original command
   * @param undo
   *          if true, the command is to be undone
   * @param targetSeqs
   *          the mapped sequences to apply the mapped command to
   * @param result
   *          the mapped EditCommand to add to
   * @param mappings
   */
  protected static void mapCutOrPaste(Edit edit, boolean undo,
          List<SequenceI> targetSeqs, EditCommand result,
          List<AlignedCodonFrame> mappings)
  {
    Action action = edit.getAction();
    if (undo)
    {
      action = action.getUndoAction();
    }
    // TODO write this
    Console.error("MappingUtils.mapCutOrPaste not yet implemented");
  }

  /**
   * Returns a new EditCommand representing the given command as mapped to the
   * given sequences. If there is no mapping, returns null.
   * 
   * @param command
   * @param undo
   * @param mapTo
   * @param gapChar
   * @param mappings
   * @return
   */
  public static EditCommand mapEditCommand(EditCommand command,
          boolean undo, final AlignmentI mapTo, char gapChar,
          List<AlignedCodonFrame> mappings)
  {
    /*
     * For now, only support mapping from protein edits to cDna
     */
    if (!mapTo.isNucleotide())
    {
      return null;
    }

    /*
     * Cache a copy of the target sequences so we can mimic successive edits on
     * them. This lets us compute mappings for all edits in the set.
     */
    Map<SequenceI, SequenceI> targetCopies = new HashMap<>();
    for (SequenceI seq : mapTo.getSequences())
    {
      SequenceI ds = seq.getDatasetSequence();
      if (ds != null)
      {
        final SequenceI copy = new Sequence(seq);
        copy.setDatasetSequence(ds);
        targetCopies.put(ds, copy);
      }
    }

    /*
     * Compute 'source' sequences as they were before applying edits:
     */
    Map<SequenceI, SequenceI> originalSequences = command.priorState(undo);

    EditCommand result = new EditCommand();
    Iterator<Edit> edits = command.getEditIterator(!undo);
    while (edits.hasNext())
    {
      Edit edit = edits.next();
      if (edit.getAction() == Action.CUT
              || edit.getAction() == Action.PASTE)
      {
        mapCutOrPaste(edit, undo, mapTo.getSequences(), result, mappings);
      }
      else if (edit.getAction() == Action.INSERT_GAP
              || edit.getAction() == Action.DELETE_GAP)
      {
        mapInsertOrDelete(edit, undo, originalSequences,
                mapTo.getSequences(), targetCopies, gapChar, result,
                mappings);
      }
    }
    return result.getSize() > 0 ? result : null;
  }

  /**
   * Helper method to map an edit command to insert or delete gaps.
   * 
   * @param edit
   *          the original command
   * @param undo
   *          if true, the action is to undo the command
   * @param originalSequences
   *          the sequences the command acted on
   * @param targetSeqs
   * @param targetCopies
   * @param gapChar
   * @param result
   *          the new EditCommand to add mapped commands to
   * @param mappings
   */
  protected static void mapInsertOrDelete(Edit edit, boolean undo,
          Map<SequenceI, SequenceI> originalSequences,
          final List<SequenceI> targetSeqs,
          Map<SequenceI, SequenceI> targetCopies, char gapChar,
          EditCommand result, List<AlignedCodonFrame> mappings)
  {
    Action action = edit.getAction();

    /*
     * Invert sense of action if an Undo.
     */
    if (undo)
    {
      action = action.getUndoAction();
    }
    final int count = edit.getNumber();
    final int editPos = edit.getPosition();
    for (SequenceI seq : edit.getSequences())
    {
      /*
       * Get residue position at (or to right of) edit location. Note we use our
       * 'copy' of the sequence before editing for this.
       */
      SequenceI ds = seq.getDatasetSequence();
      if (ds == null)
      {
        continue;
      }
      final SequenceI actedOn = originalSequences.get(ds);
      final int seqpos = actedOn.findPosition(editPos);

      /*
       * Determine all mappings from this position to mapped sequences.
       */
      SearchResultsI sr = buildSearchResults(seq, seqpos, mappings);

      if (!sr.isEmpty())
      {
        for (SequenceI targetSeq : targetSeqs)
        {
          ds = targetSeq.getDatasetSequence();
          if (ds == null)
          {
            continue;
          }
          SequenceI copyTarget = targetCopies.get(ds);
          final int[] match = sr.getResults(copyTarget, 0,
                  copyTarget.getLength());
          if (match != null)
          {
            final int ratio = 3; // TODO: compute this - how?
            final int mappedCount = count * ratio;

            /*
             * Shift Delete start position left, as it acts on positions to its
             * right.
             */
            int mappedEditPos = action == Action.DELETE_GAP
                    ? match[0] - mappedCount
                    : match[0];
            Edit e = result.new Edit(action, new SequenceI[] { targetSeq },
                    mappedEditPos, mappedCount, gapChar);
            result.addEdit(e);

            /*
             * and 'apply' the edit to our copy of its target sequence
             */
            if (action == Action.INSERT_GAP)
            {
              copyTarget.setSequence(new String(
                      StringUtils.insertCharAt(copyTarget.getSequence(),
                              mappedEditPos, mappedCount, gapChar)));
            }
            else if (action == Action.DELETE_GAP)
            {
              copyTarget.setSequence(new String(
                      StringUtils.deleteChars(copyTarget.getSequence(),
                              mappedEditPos, mappedEditPos + mappedCount)));
            }
          }
        }
      }
      /*
       * and 'apply' the edit to our copy of its source sequence
       */
      if (action == Action.INSERT_GAP)
      {
        actedOn.setSequence(new String(StringUtils.insertCharAt(
                actedOn.getSequence(), editPos, count, gapChar)));
      }
      else if (action == Action.DELETE_GAP)
      {
        actedOn.setSequence(new String(StringUtils.deleteChars(
                actedOn.getSequence(), editPos, editPos + count)));
      }
    }
  }

  /**
   * Returns a SearchResults object describing the mapped region corresponding
   * to the specified sequence position.
   * 
   * @param seq
   * @param index
   * @param seqmappings
   * @return
   */
  public static SearchResultsI buildSearchResults(SequenceI seq, int index,
          List<AlignedCodonFrame> seqmappings)
  {
    SearchResultsI results = new SearchResults();
    addSearchResults(results, seq, index, seqmappings);
    return results;
  }

  /**
   * Adds entries to a SearchResults object describing the mapped region
   * corresponding to the specified sequence position.
   * 
   * @param results
   * @param seq
   * @param index
   * @param seqmappings
   */
  public static void addSearchResults(SearchResultsI results, SequenceI seq,
          int index, List<AlignedCodonFrame> seqmappings)
  {
    if (index >= seq.getStart() && index <= seq.getEnd())
    {
      for (AlignedCodonFrame acf : seqmappings)
      {
        acf.markMappedRegion(seq, index, results);
      }
    }
  }

  /**
   * Returns a (possibly empty) SequenceGroup containing any sequences in the
   * mapped viewport corresponding to the given group in the source viewport.
   * 
   * @param sg
   * @param mapFrom
   * @param mapTo
   * @return
   */
  public static SequenceGroup mapSequenceGroup(final SequenceGroup sg,
          final AlignViewportI mapFrom, final AlignViewportI mapTo)
  {
    /*
     * Note the SequenceGroup holds aligned sequences, the mappings hold dataset
     * sequences.
     */
    boolean targetIsNucleotide = mapTo.isNucleotide();
    AlignViewportI protein = targetIsNucleotide ? mapFrom : mapTo;
    List<AlignedCodonFrame> codonFrames = protein.getAlignment()
            .getCodonFrames();
    /*
     * Copy group name, colours etc, but not sequences or sequence colour scheme
     */
    SequenceGroup mappedGroup = new SequenceGroup(sg);
    mappedGroup.setColourScheme(mapTo.getGlobalColourScheme());
    mappedGroup.clear();

    int minStartCol = -1;
    int maxEndCol = -1;
    final int selectionStartRes = sg.getStartRes();
    final int selectionEndRes = sg.getEndRes();
    for (SequenceI selected : sg.getSequences())
    {
      /*
       * Find the widest range of non-gapped positions in the selection range
       */
      int firstUngappedPos = selectionStartRes;
      while (firstUngappedPos <= selectionEndRes
              && Comparison.isGap(selected.getCharAt(firstUngappedPos)))
      {
        firstUngappedPos++;
      }

      /*
       * If this sequence is only gaps in the selected range, skip it
       */
      if (firstUngappedPos > selectionEndRes)
      {
        continue;
      }

      int lastUngappedPos = selectionEndRes;
      while (lastUngappedPos >= selectionStartRes
              && Comparison.isGap(selected.getCharAt(lastUngappedPos)))
      {
        lastUngappedPos--;
      }

      /*
       * Find the selected start/end residue positions in sequence
       */
      int startResiduePos = selected.findPosition(firstUngappedPos);
      int endResiduePos = selected.findPosition(lastUngappedPos);
      for (SequenceI seq : mapTo.getAlignment().getSequences())
      {
        int mappedStartResidue = 0;
        int mappedEndResidue = 0;
        for (AlignedCodonFrame acf : codonFrames)
        {
          // rather than use acf.getCoveringMapping() we iterate through all
          // mappings to make sure all CDS are selected for a protein
          for (SequenceToSequenceMapping map : acf.getMappings())
          {
            if (map.covers(selected) && map.covers(seq))
            {
              /*
               * Found a sequence mapping. Locate the start/end mapped residues.
               */
              List<AlignedCodonFrame> mapping = Arrays
                      .asList(new AlignedCodonFrame[]
                      { acf });
              // locate start
              SearchResultsI sr = buildSearchResults(selected,
                      startResiduePos, mapping);
              for (SearchResultMatchI m : sr.getResults())
              {
                mappedStartResidue = m.getStart();
                mappedEndResidue = m.getEnd();
              }
              // locate end - allowing for adjustment of start range
              sr = buildSearchResults(selected, endResiduePos, mapping);
              for (SearchResultMatchI m : sr.getResults())
              {
                mappedStartResidue = Math.min(mappedStartResidue,
                        m.getStart());
                mappedEndResidue = Math.max(mappedEndResidue, m.getEnd());
              }

              /*
               * Find the mapped aligned columns, save the range. Note findIndex
               * returns a base 1 position, SequenceGroup uses base 0
               */
              int mappedStartCol = seq.findIndex(mappedStartResidue) - 1;
              minStartCol = minStartCol == -1 ? mappedStartCol
                      : Math.min(minStartCol, mappedStartCol);
              int mappedEndCol = seq.findIndex(mappedEndResidue) - 1;
              maxEndCol = maxEndCol == -1 ? mappedEndCol
                      : Math.max(maxEndCol, mappedEndCol);
              mappedGroup.addSequence(seq, false);
              break;
            }
          }
        }
      }
    }
    mappedGroup.setStartRes(minStartCol < 0 ? 0 : minStartCol);
    mappedGroup.setEndRes(maxEndCol < 0 ? 0 : maxEndCol);
    return mappedGroup;
  }

  /**
   * Returns an OrderCommand equivalent to the given one, but acting on mapped
   * sequences as described by the mappings, or null if no mapping can be made.
   * 
   * @param command
   *          the original order command
   * @param undo
   *          if true, the action is to undo the sort
   * @param mapTo
   *          the alignment we are mapping to
   * @param mappings
   *          the mappings available
   * @return
   */
  public static CommandI mapOrderCommand(OrderCommand command, boolean undo,
          AlignmentI mapTo, List<AlignedCodonFrame> mappings)
  {
    SequenceI[] sortOrder = command.getSequenceOrder(undo);
    List<SequenceI> mappedOrder = new ArrayList<>();
    int j = 0;

    /*
     * Assumption: we are only interested in a cDNA/protein mapping; refactor in
     * future if we want to support sorting (c)dna as (c)dna or protein as
     * protein
     */
    boolean mappingToNucleotide = mapTo.isNucleotide();
    for (SequenceI seq : sortOrder)
    {
      for (AlignedCodonFrame acf : mappings)
      {
        for (SequenceI seq2 : mapTo.getSequences())
        {
          /*
           * the corresponding peptide / CDS is the one for which there is
           * a complete ('covering') mapping to 'seq'
           */
          SequenceI peptide = mappingToNucleotide ? seq2 : seq;
          SequenceI cds = mappingToNucleotide ? seq : seq2;
          SequenceToSequenceMapping s2s = acf.getCoveringMapping(cds,
                  peptide);
          if (s2s != null)
          {
            mappedOrder.add(seq2);
            j++;
            break;
          }
        }
      }
    }

    /*
     * Return null if no mappings made.
     */
    if (j == 0)
    {
      return null;
    }

    /*
     * Add any unmapped sequences on the end of the sort in their original
     * ordering.
     */
    if (j < mapTo.getHeight())
    {
      for (SequenceI seq : mapTo.getSequences())
      {
        if (!mappedOrder.contains(seq))
        {
          mappedOrder.add(seq);
        }
      }
    }

    /*
     * Have to sort the sequences before constructing the OrderCommand - which
     * then resorts them?!?
     */
    final SequenceI[] mappedOrderArray = mappedOrder
            .toArray(new SequenceI[mappedOrder.size()]);
    SequenceI[] oldOrder = mapTo.getSequencesArray();
    AlignmentSorter.sortBy(mapTo, new AlignmentOrder(mappedOrderArray));
    final OrderCommand result = new OrderCommand(command.getDescription(),
            oldOrder, mapTo);
    return result;
  }

  /**
   * Returns a ColumnSelection in the 'mapTo' view which corresponds to the
   * given selection in the 'mapFrom' view. We assume one is nucleotide, the
   * other is protein (and holds the mappings from codons to protein residues).
   * 
   * @param colsel
   * @param mapFrom
   * @param mapTo
   * @return
   */
  public static void mapColumnSelection(ColumnSelection colsel,
          HiddenColumns hiddencols, AlignViewportI mapFrom,
          AlignViewportI mapTo, ColumnSelection newColSel,
          HiddenColumns newHidden)
  {
    boolean targetIsNucleotide = mapTo.isNucleotide();
    AlignViewportI protein = targetIsNucleotide ? mapFrom : mapTo;
    List<AlignedCodonFrame> codonFrames = protein.getAlignment()
            .getCodonFrames();

    if (colsel == null)
    {
      return;
    }

    char fromGapChar = mapFrom.getAlignment().getGapCharacter();

    /*
     * For each mapped column, find the range of columns that residues in that
     * column map to.
     */
    List<SequenceI> fromSequences = mapFrom.getAlignment().getSequences();
    List<SequenceI> toSequences = mapTo.getAlignment().getSequences();

    for (Integer sel : colsel.getSelected())
    {
      mapColumn(sel.intValue(), codonFrames, newColSel, fromSequences,
              toSequences, fromGapChar);
    }

    Iterator<int[]> regions = hiddencols.iterator();
    while (regions.hasNext())
    {
      mapHiddenColumns(regions.next(), codonFrames, newHidden,
              fromSequences, toSequences, fromGapChar);
    }
    return;
  }

  /**
   * Helper method that maps a [start, end] hidden column range to its mapped
   * equivalent
   * 
   * @param hidden
   * @param mappings
   * @param mappedColumns
   * @param fromSequences
   * @param toSequences
   * @param fromGapChar
   */
  protected static void mapHiddenColumns(int[] hidden,
          List<AlignedCodonFrame> mappings, HiddenColumns mappedColumns,
          List<SequenceI> fromSequences, List<SequenceI> toSequences,
          char fromGapChar)
  {
    for (int col = hidden[0]; col <= hidden[1]; col++)
    {
      int[] mappedTo = findMappedColumns(col, mappings, fromSequences,
              toSequences, fromGapChar);

      /*
       * Add the range of hidden columns to the mapped selection (converting
       * base 1 to base 0).
       */
      if (mappedTo != null)
      {
        mappedColumns.hideColumns(mappedTo[0] - 1, mappedTo[1] - 1);
      }
    }
  }

  /**
   * Helper method to map one column selection
   * 
   * @param col
   *          the column number (base 0)
   * @param mappings
   *          the sequence mappings
   * @param mappedColumns
   *          the mapped column selections to add to
   * @param fromSequences
   * @param toSequences
   * @param fromGapChar
   */
  protected static void mapColumn(int col, List<AlignedCodonFrame> mappings,
          ColumnSelection mappedColumns, List<SequenceI> fromSequences,
          List<SequenceI> toSequences, char fromGapChar)
  {
    int[] mappedTo = findMappedColumns(col, mappings, fromSequences,
            toSequences, fromGapChar);

    /*
     * Add the range of mapped columns to the mapped selection (converting
     * base 1 to base 0). Note that this may include intron-only regions which
     * lie between the start and end ranges of the selection.
     */
    if (mappedTo != null)
    {
      for (int i = mappedTo[0]; i <= mappedTo[1]; i++)
      {
        mappedColumns.addElement(i - 1);
      }
    }
  }

  /**
   * Helper method to find the range of columns mapped to from one column.
   * Returns the maximal range of columns mapped to from all sequences in the
   * source column, or null if no mappings were found.
   * 
   * @param col
   * @param mappings
   * @param fromSequences
   * @param toSequences
   * @param fromGapChar
   * @return
   */
  protected static int[] findMappedColumns(int col,
          List<AlignedCodonFrame> mappings, List<SequenceI> fromSequences,
          List<SequenceI> toSequences, char fromGapChar)
  {
    int[] mappedTo = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE };
    boolean found = false;

    /*
     * For each sequence in the 'from' alignment
     */
    for (SequenceI fromSeq : fromSequences)
    {
      /*
       * Ignore gaps (unmapped anyway)
       */
      if (fromSeq.getCharAt(col) == fromGapChar)
      {
        continue;
      }

      /*
       * Get the residue position and find the mapped position.
       */
      int residuePos = fromSeq.findPosition(col);
      SearchResultsI sr = buildSearchResults(fromSeq, residuePos, mappings);
      for (SearchResultMatchI m : sr.getResults())
      {
        int mappedStartResidue = m.getStart();
        int mappedEndResidue = m.getEnd();
        SequenceI mappedSeq = m.getSequence();

        /*
         * Locate the aligned sequence whose dataset is mappedSeq. TODO a
         * datamodel that can do this efficiently.
         */
        for (SequenceI toSeq : toSequences)
        {
          if (toSeq.getDatasetSequence() == mappedSeq
                  && mappedStartResidue >= toSeq.getStart()
                  && mappedEndResidue <= toSeq.getEnd())
          {
            int mappedStartCol = toSeq.findIndex(mappedStartResidue);
            int mappedEndCol = toSeq.findIndex(mappedEndResidue);
            mappedTo[0] = Math.min(mappedTo[0], mappedStartCol);
            mappedTo[1] = Math.max(mappedTo[1], mappedEndCol);
            found = true;
            break;
            // note: remove break if we ever want to map one to many sequences
          }
        }
      }
    }
    return found ? mappedTo : null;
  }

  /**
   * Returns the mapped codon or codons for a given aligned sequence column
   * position (base 0).
   * 
   * @param seq
   *          an aligned peptide sequence
   * @param col
   *          an aligned column position (base 0)
   * @param mappings
   *          a set of codon mappings
   * @return the bases of the mapped codon(s) in the cDNA dataset sequence(s),
   *         or an empty list if none found
   */
  public static List<char[]> findCodonsFor(SequenceI seq, int col,
          List<AlignedCodonFrame> mappings)
  {
    List<char[]> result = new ArrayList<>();
    int dsPos = seq.findPosition(col);
    for (AlignedCodonFrame mapping : mappings)
    {
      if (mapping.involvesSequence(seq))
      {
        List<char[]> codons = mapping
                .getMappedCodons(seq.getDatasetSequence(), dsPos);
        if (codons != null)
        {
          result.addAll(codons);
        }
      }
    }
    return result;
  }

  /**
   * Converts a series of [start, end] range pairs into an array of individual
   * positions. This also caters for 'reverse strand' (start > end) cases.
   * 
   * @param ranges
   * @return
   */
  public static int[] flattenRanges(int[] ranges)
  {
    /*
     * Count how many positions altogether
     */
    int count = 0;
    for (int i = 0; i < ranges.length - 1; i += 2)
    {
      count += Math.abs(ranges[i + 1] - ranges[i]) + 1;
    }

    int[] result = new int[count];
    int k = 0;
    for (int i = 0; i < ranges.length - 1; i += 2)
    {
      int from = ranges[i];
      final int to = ranges[i + 1];
      int step = from <= to ? 1 : -1;
      do
      {
        result[k++] = from;
        from += step;
      } while (from != to + step);
    }
    return result;
  }

  /**
   * Returns a list of any mappings that are from or to the given (aligned or
   * dataset) sequence.
   * 
   * @param sequence
   * @param mappings
   * @return
   */
  public static List<AlignedCodonFrame> findMappingsForSequence(
          SequenceI sequence, List<AlignedCodonFrame> mappings)
  {
    return findMappingsForSequenceAndOthers(sequence, mappings, null);
  }

  /**
   * Returns a list of any mappings that are from or to the given (aligned or
   * dataset) sequence, optionally limited to mappings involving one of a given
   * list of sequences.
   * 
   * @param sequence
   * @param mappings
   * @param filterList
   * @return
   */
  public static List<AlignedCodonFrame> findMappingsForSequenceAndOthers(
          SequenceI sequence, List<AlignedCodonFrame> mappings,
          List<SequenceI> filterList)
  {
    List<AlignedCodonFrame> result = new ArrayList<>();
    if (sequence == null || mappings == null)
    {
      return result;
    }
    for (AlignedCodonFrame mapping : mappings)
    {
      if (mapping.involvesSequence(sequence))
      {
        if (filterList != null)
        {
          for (SequenceI otherseq : filterList)
          {
            SequenceI otherDataset = otherseq.getDatasetSequence();
            if (otherseq == sequence
                    || otherseq == sequence.getDatasetSequence()
                    || (otherDataset != null && (otherDataset == sequence
                            || otherDataset == sequence
                                    .getDatasetSequence())))
            {
              // skip sequences in subset which directly relate to sequence
              continue;
            }
            if (mapping.involvesSequence(otherseq))
            {
              // selected a mapping contained in subselect alignment
              result.add(mapping);
              break;
            }
          }
        }
        else
        {
          result.add(mapping);
        }
      }
    }
    return result;
  }

  /**
   * Returns the total length of the supplied ranges, which may be as single
   * [start, end] or multiple [start, end, start, end ...]
   * 
   * @param ranges
   * @return
   */
  public static int getLength(List<int[]> ranges)
  {
    if (ranges == null)
    {
      return 0;
    }
    int length = 0;
    for (int[] range : ranges)
    {
      if (range.length % 2 != 0)
      {
        Console.error(
                "Error unbalance start/end ranges: " + ranges.toString());
        return 0;
      }
      for (int i = 0; i < range.length - 1; i += 2)
      {
        length += Math.abs(range[i + 1] - range[i]) + 1;
      }
    }
    return length;
  }

  /**
   * Answers true if any range includes the given value
   * 
   * @param ranges
   * @param value
   * @return
   */
  public static boolean contains(List<int[]> ranges, int value)
  {
    if (ranges == null)
    {
      return false;
    }
    for (int[] range : ranges)
    {
      if (range[1] >= range[0] && value >= range[0] && value <= range[1])
      {
        /*
         * value within ascending range
         */
        return true;
      }
      if (range[1] < range[0] && value <= range[0] && value >= range[1])
      {
        /*
         * value within descending range
         */
        return true;
      }
    }
    return false;
  }

  /**
   * Removes a specified number of positions from the start of a ranges list.
   * For example, could be used to adjust cds ranges to allow for an incomplete
   * start codon. Subranges are removed completely, or their start positions
   * adjusted, until the required number of positions has been removed from the
   * range. Reverse strand ranges are supported. The input array is not
   * modified.
   * 
   * @param removeCount
   * @param ranges
   *          an array of [start, end, start, end...] positions
   * @return a new array with the first removeCount positions removed
   */
  public static int[] removeStartPositions(int removeCount,
          final int[] ranges)
  {
    if (removeCount <= 0)
    {
      return ranges;
    }

    int[] copy = Arrays.copyOf(ranges, ranges.length);
    int sxpos = -1;
    int cdspos = 0;
    for (int x = 0; x < copy.length && sxpos == -1; x += 2)
    {
      cdspos += Math.abs(copy[x + 1] - copy[x]) + 1;
      if (removeCount < cdspos)
      {
        /*
         * we have removed enough, time to finish
         */
        sxpos = x;

        /*
         * increment start of first exon, or decrement if reverse strand
         */
        if (copy[x] <= copy[x + 1])
        {
          copy[x] = copy[x + 1] - cdspos + removeCount + 1;
        }
        else
        {
          copy[x] = copy[x + 1] + cdspos - removeCount - 1;
        }
        break;
      }
    }

    if (sxpos > 0)
    {
      /*
       * we dropped at least one entire sub-range - compact the array
       */
      int[] nxon = new int[copy.length - sxpos];
      System.arraycopy(copy, sxpos, nxon, 0, copy.length - sxpos);
      return nxon;
    }
    return copy;
  }

  /**
   * Answers true if range's start-end positions include those of queryRange,
   * where either range might be in reverse direction, else false
   * 
   * @param range
   *          a start-end range
   * @param queryRange
   *          a candidate subrange of range (start2-end2)
   * @return
   */
  public static boolean rangeContains(int[] range, int[] queryRange)
  {
    if (range == null || queryRange == null || range.length != 2
            || queryRange.length != 2)
    {
      /*
       * invalid arguments
       */
      return false;
    }

    int min = Math.min(range[0], range[1]);
    int max = Math.max(range[0], range[1]);

    return (min <= queryRange[0] && max >= queryRange[0]
            && min <= queryRange[1] && max >= queryRange[1]);
  }

  /**
   * Removes the specified number of positions from the given ranges. Provided
   * to allow a stop codon to be stripped from a CDS sequence so that it matches
   * the peptide translation length.
   * 
   * @param positions
   * @param ranges
   *          a list of (single) [start, end] ranges
   * @return
   */
  public static void removeEndPositions(int positions, List<int[]> ranges)
  {
    int toRemove = positions;
    Iterator<int[]> it = new ReverseListIterator<>(ranges);
    while (toRemove > 0)
    {
      int[] endRange = it.next();
      if (endRange.length != 2)
      {
        /*
         * not coded for [start1, end1, start2, end2, ...]
         */
        Console.error(
                "MappingUtils.removeEndPositions doesn't handle multiple  ranges");
        return;
      }

      int length = endRange[1] - endRange[0] + 1;
      if (length <= 0)
      {
        /*
         * not coded for a reverse strand range (end < start)
         */
        Console.error(
                "MappingUtils.removeEndPositions doesn't handle reverse strand");
        return;
      }
      if (length > toRemove)
      {
        endRange[1] -= toRemove;
        toRemove = 0;
      }
      else
      {
        toRemove -= length;
        it.remove();
      }
    }
  }

  /**
   * Converts a list of {@code start-end} ranges to a single array of
   * {@code start1, end1, start2, ... } ranges
   * 
   * @param ranges
   * @return
   */
  public static int[] rangeListToArray(List<int[]> ranges)
  {
    int rangeCount = ranges.size();
    int[] result = new int[rangeCount * 2];
    int j = 0;
    for (int i = 0; i < rangeCount; i++)
    {
      int[] range = ranges.get(i);
      result[j++] = range[0];
      result[j++] = range[1];
    }
    return result;
  }

  /*
   * Returns the maximal start-end positions in the given (ordered) list of
   * ranges which is overlapped by the given begin-end range, or null if there
   * is no overlap.
   * 
   * <pre>
   * Examples:
   *   if ranges is {[4, 8], [10, 12], [16, 19]}
   * then
   *   findOverlap(ranges, 1, 20) == [4, 19]
   *   findOverlap(ranges, 6, 11) == [6, 11]
   *   findOverlap(ranges, 9, 15) == [10, 12]
   *   findOverlap(ranges, 13, 15) == null
   * </pre>
   * 
   * @param ranges
   * @param begin
   * @param end
   * @return
   */
  protected static int[] findOverlap(List<int[]> ranges, final int begin,
          final int end)
  {
    boolean foundStart = false;
    int from = 0;
    int to = 0;

    /*
     * traverse the ranges to find the first position (if any) >= begin,
     * and the last position (if any) <= end
     */
    for (int[] range : ranges)
    {
      if (!foundStart)
      {
        if (range[0] >= begin)
        {
          /*
           * first range that starts with, or follows, begin
           */
          foundStart = true;
          from = Math.max(range[0], begin);
        }
        else if (range[1] >= begin)
        {
          /*
           * first range that contains begin
           */
          foundStart = true;
          from = begin;
        }
      }

      if (range[0] <= end)
      {
        to = Math.min(end, range[1]);
      }
    }

    return foundStart && to >= from ? new int[] { from, to } : null;
  }
}
