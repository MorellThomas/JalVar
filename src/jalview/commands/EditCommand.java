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
package jalview.commands;

import java.util.Locale;

import jalview.analysis.AlignSeq;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ContiguousI;
import jalview.datamodel.Range;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeaturesI;
import jalview.util.Comparison;
import jalview.util.ReverseListIterator;
import jalview.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 
 * <p>
 * Title: EditCommmand
 * </p>
 * 
 * <p>
 * Description: Essential information for performing undo and redo for cut/paste
 * insert/delete gap which can be stored in the HistoryList
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: Dundee University
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class EditCommand implements CommandI
{
  public enum Action
  {
    INSERT_GAP
    {
      @Override
      public Action getUndoAction()
      {
        return DELETE_GAP;
      }
    },
    DELETE_GAP
    {
      @Override
      public Action getUndoAction()
      {
        return INSERT_GAP;
      }
    },
    CUT
    {
      @Override
      public Action getUndoAction()
      {
        return PASTE;
      }
    },
    PASTE
    {
      @Override
      public Action getUndoAction()
      {
        return CUT;
      }
    },
    REPLACE
    {
      @Override
      public Action getUndoAction()
      {
        return REPLACE;
      }
    },
    INSERT_NUC
    {
      @Override
      public Action getUndoAction()
      {
        return null;
      }
    };

    public abstract Action getUndoAction();
  };

  private List<Edit> edits = new ArrayList<>();

  String description;

  public EditCommand()
  {
  }

  public EditCommand(String desc)
  {
    this.description = desc;
  }

  public EditCommand(String desc, Action command, SequenceI[] seqs,
          int position, int number, AlignmentI al)
  {
    this.description = desc;
    if (command == Action.CUT || command == Action.PASTE)
    {
      setEdit(new Edit(command, seqs, position, number, al));
    }

    performEdit(0, null);
  }

  public EditCommand(String desc, Action command, String replace,
          SequenceI[] seqs, int position, int number, AlignmentI al)
  {
    this.description = desc;
    if (command == Action.REPLACE)
    {
      setEdit(new Edit(command, seqs, position, number, al, replace));
    }

    performEdit(0, null);
  }

  /**
   * Set the list of edits to the specified item (only).
   * 
   * @param e
   */
  protected void setEdit(Edit e)
  {
    edits.clear();
    edits.add(e);
  }

  /**
   * Add the given edit command to the stored list of commands. If simply
   * expanding the range of the last command added, then modify it instead of
   * adding a new command.
   * 
   * @param e
   */
  public void addEdit(Edit e)
  {
    if (!expandEdit(edits, e))
    {
      edits.add(e);
    }
  }

  /**
   * Returns true if the new edit is incorporated by updating (expanding the
   * range of) the last edit on the list, else false. We can 'expand' the last
   * edit if the new one is the same action, on the same sequences, and acts on
   * a contiguous range. This is the case where a mouse drag generates a series
   * of contiguous gap insertions or deletions.
   * 
   * @param edits
   * @param e
   * @return
   */
  protected static boolean expandEdit(List<Edit> edits, Edit e)
  {
    if (edits == null || edits.isEmpty())
    {
      return false;
    }
    Edit lastEdit = edits.get(edits.size() - 1);
    Action action = e.command;
    if (lastEdit.command != action)
    {
      return false;
    }

    /*
     * Both commands must act on the same sequences - compare the underlying
     * dataset sequences, rather than the aligned sequences, which change as
     * they are edited.
     */
    if (lastEdit.seqs.length != e.seqs.length)
    {
      return false;
    }
    for (int i = 0; i < e.seqs.length; i++)
    {
      if (lastEdit.seqs[i].getDatasetSequence() != e.seqs[i]
              .getDatasetSequence())
      {
        return false;
      }
    }

    /**
     * Check a contiguous edit; either
     * <ul>
     * <li>a new Insert <n> positions to the right of the last <insert n>,
     * or</li>
     * <li>a new Delete <n> gaps which is <n> positions to the left of the last
     * delete.</li>
     * </ul>
     */
    boolean contiguous = (action == Action.INSERT_GAP
            && e.position == lastEdit.position + lastEdit.number)
            || (action == Action.DELETE_GAP
                    && e.position + e.number == lastEdit.position);
    if (contiguous)
    {
      /*
       * We are just expanding the range of the last edit. For delete gap, also
       * moving the start position left.
       */
      lastEdit.number += e.number;
      lastEdit.seqs = e.seqs;
      if (action == Action.DELETE_GAP)
      {
        lastEdit.position--;
      }
      return true;
    }
    return false;
  }

  /**
   * Clear the list of stored edit commands.
   * 
   */
  protected void clearEdits()
  {
    edits.clear();
  }

  /**
   * Returns the i'th stored Edit command.
   * 
   * @param i
   * @return
   */
  protected Edit getEdit(int i)
  {
    if (i >= 0 && i < edits.size())
    {
      return edits.get(i);
    }
    return null;
  }

  @Override
  final public String getDescription()
  {
    return description;
  }

  @Override
  public int getSize()
  {
    return edits.size();
  }

  /**
   * Return the alignment for the first edit (or null if no edit).
   * 
   * @return
   */
  final public AlignmentI getAlignment()
  {
    return (edits.isEmpty() ? null : edits.get(0).al);
  }

  /**
   * append a new editCommand Note. this shouldn't be called if the edit is an
   * operation affects more alignment objects than the one referenced in al (for
   * example, cut or pasting whole sequences). Use the form with an additional
   * AlignmentI[] views parameter.
   * 
   * @param command
   * @param seqs
   * @param position
   * @param number
   * @param al
   * @param performEdit
   */
  final public void appendEdit(Action command, SequenceI[] seqs,
          int position, int number, AlignmentI al, boolean performEdit)
  {
    appendEdit(command, seqs, position, number, al, performEdit, null);
  }

  /**
   * append a new edit command with a set of alignment views that may be
   * operated on
   * 
   * @param command
   * @param seqs
   * @param position
   * @param number
   * @param al
   * @param performEdit
   * @param views
   */
  final public void appendEdit(Action command, SequenceI[] seqs,
          int position, int number, AlignmentI al, boolean performEdit,
          AlignmentI[] views)
  {
    Edit edit = new Edit(command, seqs, position, number, al);
    appendEdit(edit, al, performEdit, views);
  }

  /**
   * Overloaded method that accepts an Edit object with additional parameters.
   * 
   * @param edit
   * @param al
   * @param performEdit
   * @param views
   */
  final public void appendEdit(Edit edit, AlignmentI al,
          boolean performEdit, AlignmentI[] views)
  {
    if (al.getHeight() == edit.seqs.length)
    {
      edit.al = al;
      edit.fullAlignmentHeight = true;
    }

    addEdit(edit);

    if (performEdit)
    {
      performEdit(edit, views);
    }
  }

  /**
   * Execute all the edit commands, starting at the given commandIndex
   * 
   * @param commandIndex
   * @param views
   */
  public final void performEdit(int commandIndex, AlignmentI[] views)
  {
    ListIterator<Edit> iterator = edits.listIterator(commandIndex);
    while (iterator.hasNext())
    {
      Edit edit = iterator.next();
      performEdit(edit, views);
    }
  }

  /**
   * Execute one edit command in all the specified alignment views
   * 
   * @param edit
   * @param views
   */
  protected static void performEdit(Edit edit, AlignmentI[] views)
  {
    switch (edit.command)
    {
    case INSERT_GAP:
      insertGap(edit);
      break;
    case DELETE_GAP:
      deleteGap(edit);
      break;
    case CUT:
      cut(edit, views);
      break;
    case PASTE:
      paste(edit, views);
      break;
    case REPLACE:
      replace(edit);
      break;
    case INSERT_NUC:
      // TODO:add deleteNuc for UNDO
      // case INSERT_NUC:
      // insertNuc(edits[e]);
      break;
    default:
      break;
    }
  }

  @Override
  final public void doCommand(AlignmentI[] views)
  {
    performEdit(0, views);
  }

  /**
   * Undo the stored list of commands, in reverse order.
   */
  @Override
  final public void undoCommand(AlignmentI[] views)
  {
    ListIterator<Edit> iterator = edits.listIterator(edits.size());
    while (iterator.hasPrevious())
    {
      Edit e = iterator.previous();
      switch (e.command)
      {
      case INSERT_GAP:
        deleteGap(e);
        break;
      case DELETE_GAP:
        insertGap(e);
        break;
      case CUT:
        paste(e, views);
        break;
      case PASTE:
        cut(e, views);
        break;
      case REPLACE:
        replace(e);
        break;
      case INSERT_NUC:
        // not implemented
        break;
      default:
        break;
      }
    }
  }

  /**
   * Insert gap(s) in sequences as specified by the command, and adjust
   * annotations.
   * 
   * @param command
   */
  final private static void insertGap(Edit command)
  {

    for (int s = 0; s < command.seqs.length; s++)
    {
      command.seqs[s].insertCharAt(command.position, command.number,
              command.gapChar);
      // System.out.println("pos: "+command.position+" number:
      // "+command.number);
    }

    adjustAnnotations(command, true, false, null);
  }

  //
  // final void insertNuc(Edit command)
  // {
  //
  // for (int s = 0; s < command.seqs.length; s++)
  // {
  // System.out.println("pos: "+command.position+" number: "+command.number);
  // command.seqs[s].insertCharAt(command.position, command.number,'A');
  // }
  //
  // adjustAnnotations(command, true, false, null);
  // }

  /**
   * Delete gap(s) in sequences as specified by the command, and adjust
   * annotations.
   * 
   * @param command
   */
  final static private void deleteGap(Edit command)
  {
    for (int s = 0; s < command.seqs.length; s++)
    {
      command.seqs[s].deleteChars(command.position,
              command.position + command.number);
    }

    adjustAnnotations(command, false, false, null);
  }

  /**
   * Carry out a Cut action. The cut characters are saved in case Undo is
   * requested.
   * 
   * @param command
   * @param views
   */
  static void cut(Edit command, AlignmentI[] views)
  {
    boolean seqDeleted = false;
    command.string = new char[command.seqs.length][];

    for (int i = 0; i < command.seqs.length; i++)
    {
      final SequenceI sequence = command.seqs[i];
      if (sequence.getLength() > command.position)
      {
        command.string[i] = sequence.getSequence(command.position,
                command.position + command.number);
        SequenceI oldds = sequence.getDatasetSequence();
        ContiguousI cutPositions = sequence.findPositions(
                command.position + 1, command.position + command.number);
        boolean cutIsInternal = cutPositions != null
                && sequence.getStart() != cutPositions.getBegin()
                && sequence.getEnd() != cutPositions.getEnd();

        /*
         * perform the cut; if this results in a new dataset sequence, add
         * that to the alignment dataset
         */
        SequenceI ds = sequence.getDatasetSequence();
        sequence.deleteChars(command.position,
                command.position + command.number);

        if (command.oldds != null && command.oldds[i] != null)
        {
          /*
           * we are Redoing a Cut, or Undoing a Paste - so
           * oldds entry contains the cut dataset sequence,
           * with sequence features in expected place
           */
          sequence.setDatasetSequence(command.oldds[i]);
          command.oldds[i] = oldds;
        }
        else
        {
          /* 
           * new cut operation: save the dataset sequence 
           * so it can be restored in an Undo
           */
          if (command.oldds == null)
          {
            command.oldds = new SequenceI[command.seqs.length];
          }
          command.oldds[i] = oldds;// todo not if !cutIsInternal?

          // do we need to edit sequence features for new sequence ?
          if (oldds != sequence.getDatasetSequence() || (cutIsInternal
                  && sequence.getFeatures().hasFeatures()))
          // todo or just test cutIsInternal && cutPositions != null ?
          {
            if (cutPositions != null)
            {
              cutFeatures(command, sequence, cutPositions.getBegin(),
                      cutPositions.getEnd(), cutIsInternal);
            }
          }
        }
        SequenceI newDs = sequence.getDatasetSequence();
        if (newDs != ds && command.al != null
                && command.al.getDataset() != null
                && !command.al.getDataset().getSequences().contains(newDs))
        {
          command.al.getDataset().addSequence(newDs);
        }
      }

      if (sequence.getLength() < 1)
      {
        command.al.deleteSequence(sequence);
        seqDeleted = true;
      }
    }

    adjustAnnotations(command, false, seqDeleted, views);
  }

  /**
   * Perform the given Paste command. This may be to add cut or copied sequences
   * to an alignment, or to undo a 'Cut' action on a region of the alignment.
   * 
   * @param command
   * @param views
   */
  static void paste(Edit command, AlignmentI[] views)
  {
    boolean seqWasDeleted = false;

    for (int i = 0; i < command.seqs.length; i++)
    {
      boolean newDSNeeded = false;
      boolean newDSWasNeeded = command.oldds != null
              && command.oldds[i] != null;
      SequenceI sequence = command.seqs[i];
      if (sequence.getLength() < 1)
      {
        /*
         * sequence was deleted; re-add it to the alignment
         */
        if (command.alIndex[i] < command.al.getHeight())
        {
          List<SequenceI> sequences = command.al.getSequences();
          synchronized (sequences)
          {
            if (!(command.alIndex[i] < 0))
            {
              sequences.add(command.alIndex[i], sequence);
            }
          }
        }
        else
        {
          command.al.addSequence(sequence);
        }
        seqWasDeleted = true;
      }
      int newStart = sequence.getStart();
      int newEnd = sequence.getEnd();

      StringBuilder tmp = new StringBuilder();
      tmp.append(sequence.getSequence());
      // Undo of a delete does not replace original dataset sequence on to
      // alignment sequence.

      int start = 0;
      int length = 0;

      if (command.string != null && command.string[i] != null)
      {
        if (command.position >= tmp.length())
        {
          // This occurs if padding is on, and residues
          // are removed from end of alignment
          int len = command.position - tmp.length();
          while (len > 0)
          {
            tmp.append(command.gapChar);
            len--;
          }
        }
        tmp.insert(command.position, command.string[i]);
        for (int s = 0; s < command.string[i].length; s++)
        {
          if (!Comparison.isGap(command.string[i][s]))
          {
            length++;
            if (!newDSNeeded)
            {
              newDSNeeded = true;
              start = sequence.findPosition(command.position);
              // end = sequence
              // .findPosition(command.position + command.number);
            }
            if (sequence.getStart() == start)
            {
              newStart--;
            }
            else
            {
              newEnd++;
            }
          }
        }
        command.string[i] = null;
      }

      sequence.setSequence(tmp.toString());
      sequence.setStart(newStart);
      sequence.setEnd(newEnd);

      /*
       * command and Undo share the same dataset sequence if cut was
       * at start or end of sequence
       */
      boolean sameDatasetSequence = false;
      if (newDSNeeded)
      {
        if (sequence.getDatasetSequence() != null)
        {
          SequenceI ds;
          if (newDSWasNeeded)
          {
            ds = command.oldds[i];
          }
          else
          {
            // make a new DS sequence
            // use new ds mechanism here
            String ungapped = AlignSeq.extractGaps(Comparison.GapChars,
                    sequence.getSequenceAsString());
            ds = new Sequence(sequence.getName(), ungapped,
                    sequence.getStart(), sequence.getEnd());
            ds.setDescription(sequence.getDescription());
          }
          if (command.oldds == null)
          {
            command.oldds = new SequenceI[command.seqs.length];
          }
          command.oldds[i] = sequence.getDatasetSequence();
          sameDatasetSequence = ds == sequence.getDatasetSequence();
          ds.setSequenceFeatures(sequence.getSequenceFeatures());
          if (!sameDatasetSequence && command.al.getDataset() != null)
          {
            // delete 'undone' sequence from alignment dataset
            command.al.getDataset()
                    .deleteSequence(sequence.getDatasetSequence());
          }
          sequence.setDatasetSequence(ds);
        }
        undoCutFeatures(command, command.seqs[i], start, length,
                sameDatasetSequence);
      }
    }
    adjustAnnotations(command, true, seqWasDeleted, views);

    command.string = null;
  }

  static void replace(Edit command)
  {
    StringBuilder tmp;
    String oldstring;
    int start = command.position;
    int end = command.number;
    // TODO TUTORIAL - Fix for replacement with different length of sequence (or
    // whole sequence)
    // TODO Jalview 2.4 bugfix change to an aggregate command - original
    // sequence string is cut, new string is pasted in.
    command.number = start + command.string[0].length;
    for (int i = 0; i < command.seqs.length; i++)
    {
      boolean newDSWasNeeded = command.oldds != null
              && command.oldds[i] != null;
      boolean newStartEndWasNeeded = command.oldStartEnd != null
              && command.oldStartEnd[i] != null;

      /**
       * cut addHistoryItem(new EditCommand("Cut Sequences", EditCommand.CUT,
       * cut, sg.getStartRes(), sg.getEndRes()-sg.getStartRes()+1,
       * viewport.alignment));
       * 
       */
      /**
       * then addHistoryItem(new EditCommand( "Add sequences",
       * EditCommand.PASTE, sequences, 0, alignment.getWidth(), alignment) );
       * 
       */
      ContiguousI beforeEditedPositions = command.seqs[i].findPositions(1,
              start);
      ContiguousI afterEditedPositions = command.seqs[i]
              .findPositions(end + 1, command.seqs[i].getLength());

      oldstring = command.seqs[i].getSequenceAsString();
      tmp = new StringBuilder(oldstring.substring(0, start));
      tmp.append(command.string[i]);
      String nogaprep = AlignSeq.extractGaps(Comparison.GapChars,
              new String(command.string[i]));
      if (end < oldstring.length())
      {
        tmp.append(oldstring.substring(end));
      }
      // stash end prior to updating the sequence object so we can save it if
      // need be.
      Range oldstartend = new Range(command.seqs[i].getStart(),
              command.seqs[i].getEnd());
      command.seqs[i].setSequence(tmp.toString());
      command.string[i] = oldstring
              .substring(start, Math.min(end, oldstring.length()))
              .toCharArray();
      String nogapold = AlignSeq.extractGaps(Comparison.GapChars,
              new String(command.string[i]));

      if (!nogaprep.toLowerCase(Locale.ROOT)
              .equals(nogapold.toLowerCase(Locale.ROOT)))
      {
        // we may already have dataset and limits stashed...
        if (newDSWasNeeded || newStartEndWasNeeded)
        {
          if (newDSWasNeeded)
          {
            // then just switch the dataset sequence
            SequenceI oldds = command.seqs[i].getDatasetSequence();
            command.seqs[i].setDatasetSequence(command.oldds[i]);
            command.oldds[i] = oldds;
          }
          if (newStartEndWasNeeded)
          {
            Range newStart = command.oldStartEnd[i];
            command.oldStartEnd[i] = oldstartend;
            command.seqs[i].setStart(newStart.getBegin());
            command.seqs[i].setEnd(newStart.getEnd());
          }
        }
        else
        {
          // decide if we need a new dataset sequence or modify start/end
          // first edit the original dataset sequence string
          SequenceI oldds = command.seqs[i].getDatasetSequence();
          String osp = oldds.getSequenceAsString();
          int beforeStartOfEdit = -oldds.getStart() + 1
                  + (beforeEditedPositions == null
                          ? ((afterEditedPositions != null)
                                  ? afterEditedPositions.getBegin() - 1
                                  : oldstartend.getBegin()
                                          + nogapold.length())
                          : beforeEditedPositions.getEnd());
          int afterEndOfEdit = -oldds.getStart() + 1
                  + ((afterEditedPositions == null) ? oldstartend.getEnd()
                          : afterEditedPositions.getBegin() - 1);
          String fullseq = osp.substring(0, beforeStartOfEdit) + nogaprep
                  + osp.substring(afterEndOfEdit);

          // and check if new sequence data is different..
          if (!fullseq.equalsIgnoreCase(osp))
          {
            // old ds and edited ds are different, so
            // create the new dataset sequence
            SequenceI newds = new Sequence(oldds);
            newds.setSequence(fullseq.toUpperCase(Locale.ROOT));

            if (command.oldds == null)
            {
              command.oldds = new SequenceI[command.seqs.length];
            }
            command.oldds[i] = command.seqs[i].getDatasetSequence();

            // And preserve start/end for good-measure

            if (command.oldStartEnd == null)
            {
              command.oldStartEnd = new Range[command.seqs.length];
            }
            command.oldStartEnd[i] = oldstartend;
            // TODO: JAL-1131 ensure newly created dataset sequence is added to
            // the set of
            // dataset sequences associated with the alignment.
            // TODO: JAL-1131 fix up any annotation associated with new dataset
            // sequence to ensure that original sequence/annotation
            // relationships
            // are preserved.
            command.seqs[i].setDatasetSequence(newds);
          }
          else
          {
            if (command.oldStartEnd == null)
            {
              command.oldStartEnd = new Range[command.seqs.length];
            }
            command.oldStartEnd[i] = new Range(command.seqs[i].getStart(),
                    command.seqs[i].getEnd());
            if (beforeEditedPositions != null
                    && afterEditedPositions == null)
            {
              // modification at end
              command.seqs[i].setEnd(beforeEditedPositions.getEnd()
                      + nogaprep.length() - nogapold.length());
            }
            else if (afterEditedPositions != null
                    && beforeEditedPositions == null)
            {
              // modification at start
              command.seqs[i].setStart(
                      afterEditedPositions.getBegin() - nogaprep.length());
            }
            else
            {
              // edit covered both start and end. Here we can only guess the
              // new
              // start/end
              String nogapalseq = AlignSeq.extractGaps(Comparison.GapChars,
                      command.seqs[i].getSequenceAsString()
                              .toUpperCase(Locale.ROOT));
              int newStart = command.seqs[i].getDatasetSequence()
                      .getSequenceAsString().indexOf(nogapalseq);
              if (newStart == -1)
              {
                throw new Error(
                        "Implementation Error: could not locate start/end "
                                + "in dataset sequence after an edit of the sequence string");
              }
              int newEnd = newStart + nogapalseq.length() - 1;
              command.seqs[i].setStart(newStart);
              command.seqs[i].setEnd(newEnd);
            }
          }
        }
      }
      tmp = null;
      oldstring = null;
    }
  }

  final static void adjustAnnotations(Edit command, boolean insert,
          boolean modifyVisibility, AlignmentI[] views)
  {
    AlignmentAnnotation[] annotations = null;

    if (modifyVisibility && !insert)
    {
      // only occurs if a sequence was added or deleted.
      command.deletedAnnotationRows = new Hashtable<>();
    }
    if (command.fullAlignmentHeight)
    {
      annotations = command.al.getAlignmentAnnotation();
    }
    else
    {
      int aSize = 0;
      AlignmentAnnotation[] tmp;
      for (int s = 0; s < command.seqs.length; s++)
      {
        command.seqs[s].sequenceChanged();

        if (modifyVisibility)
        {
          // Rows are only removed or added to sequence object.
          if (!insert)
          {
            // remove rows
            tmp = command.seqs[s].getAnnotation();
            if (tmp != null)
            {
              int alen = tmp.length;
              for (int aa = 0; aa < tmp.length; aa++)
              {
                if (!command.al.deleteAnnotation(tmp[aa]))
                {
                  // strip out annotation not in the current al (will be put
                  // back on insert in all views)
                  tmp[aa] = null;
                  alen--;
                }
              }
              command.seqs[s].setAlignmentAnnotation(null);
              if (alen != tmp.length)
              {
                // save the non-null annotation references only
                AlignmentAnnotation[] saved = new AlignmentAnnotation[alen];
                for (int aa = 0, aapos = 0; aa < tmp.length; aa++)
                {
                  if (tmp[aa] != null)
                  {
                    saved[aapos++] = tmp[aa];
                    tmp[aa] = null;
                  }
                }
                tmp = saved;
                command.deletedAnnotationRows.put(command.seqs[s], saved);
                // and then remove any annotation in the other views
                for (int alview = 0; views != null
                        && alview < views.length; alview++)
                {
                  if (views[alview] != command.al)
                  {
                    AlignmentAnnotation[] toremove = views[alview]
                            .getAlignmentAnnotation();
                    if (toremove == null || toremove.length == 0)
                    {
                      continue;
                    }
                    // remove any alignment annotation on this sequence that's
                    // on that alignment view.
                    for (int aa = 0; aa < toremove.length; aa++)
                    {
                      if (toremove[aa].sequenceRef == command.seqs[s])
                      {
                        views[alview].deleteAnnotation(toremove[aa]);
                      }
                    }
                  }
                }
              }
              else
              {
                // save all the annotation
                command.deletedAnnotationRows.put(command.seqs[s], tmp);
              }
            }
          }
          else
          {
            // recover rows
            if (command.deletedAnnotationRows != null
                    && command.deletedAnnotationRows
                            .containsKey(command.seqs[s]))
            {
              AlignmentAnnotation[] revealed = command.deletedAnnotationRows
                      .get(command.seqs[s]);
              command.seqs[s].setAlignmentAnnotation(revealed);
              if (revealed != null)
              {
                for (int aa = 0; aa < revealed.length; aa++)
                {
                  // iterate through al adding original annotation
                  command.al.addAnnotation(revealed[aa]);
                }
                for (int aa = 0; aa < revealed.length; aa++)
                {
                  command.al.setAnnotationIndex(revealed[aa], aa);
                }
                // and then duplicate added annotation on every other alignment
                // view
                for (int vnum = 0; views != null
                        && vnum < views.length; vnum++)
                {
                  if (views[vnum] != command.al)
                  {
                    int avwidth = views[vnum].getWidth() + 1;
                    // duplicate in this view
                    for (int a = 0; a < revealed.length; a++)
                    {
                      AlignmentAnnotation newann = new AlignmentAnnotation(
                              revealed[a]);
                      command.seqs[s].addAlignmentAnnotation(newann);
                      newann.padAnnotation(avwidth);
                      views[vnum].addAnnotation(newann);
                      views[vnum].setAnnotationIndex(newann, a);
                    }
                  }
                }
              }
            }
          }
          continue;
        }

        if (command.seqs[s].getAnnotation() == null)
        {
          continue;
        }

        if (aSize == 0)
        {
          annotations = command.seqs[s].getAnnotation();
        }
        else
        {
          tmp = new AlignmentAnnotation[aSize
                  + command.seqs[s].getAnnotation().length];

          System.arraycopy(annotations, 0, tmp, 0, aSize);

          System.arraycopy(command.seqs[s].getAnnotation(), 0, tmp, aSize,
                  command.seqs[s].getAnnotation().length);

          annotations = tmp;
        }
        aSize = annotations.length;
      }
    }

    if (annotations == null)
    {
      return;
    }

    if (!insert)
    {
      command.deletedAnnotations = new Hashtable<>();
    }

    int aSize;
    Annotation[] temp;
    for (int a = 0; a < annotations.length; a++)
    {
      if (annotations[a].autoCalculated
              || annotations[a].annotations == null)
      {
        continue;
      }

      int tSize = 0;

      aSize = annotations[a].annotations.length;
      if (insert)
      {
        temp = new Annotation[aSize + command.number];
        if (annotations[a].padGaps)
        {
          for (int aa = 0; aa < temp.length; aa++)
          {
            temp[aa] = new Annotation(command.gapChar + "", null, ' ', 0);
          }
        }
      }
      else
      {
        if (command.position < aSize)
        {
          if (command.position + command.number >= aSize)
          {
            tSize = aSize;
          }
          else
          {
            tSize = aSize - command.number;
          }
        }
        else
        {
          tSize = aSize;
        }

        if (tSize < 0)
        {
          tSize = aSize;
        }
        temp = new Annotation[tSize];
      }

      if (insert)
      {
        if (command.position < annotations[a].annotations.length)
        {
          System.arraycopy(annotations[a].annotations, 0, temp, 0,
                  command.position);

          if (command.deletedAnnotations != null
                  && command.deletedAnnotations
                          .containsKey(annotations[a].annotationId))
          {
            Annotation[] restore = command.deletedAnnotations
                    .get(annotations[a].annotationId);

            System.arraycopy(restore, 0, temp, command.position,
                    command.number);

          }

          System.arraycopy(annotations[a].annotations, command.position,
                  temp, command.position + command.number,
                  aSize - command.position);
        }
        else
        {
          if (command.deletedAnnotations != null
                  && command.deletedAnnotations
                          .containsKey(annotations[a].annotationId))
          {
            Annotation[] restore = command.deletedAnnotations
                    .get(annotations[a].annotationId);

            temp = new Annotation[annotations[a].annotations.length
                    + restore.length];
            System.arraycopy(annotations[a].annotations, 0, temp, 0,
                    annotations[a].annotations.length);
            System.arraycopy(restore, 0, temp,
                    annotations[a].annotations.length, restore.length);
          }
          else
          {
            temp = annotations[a].annotations;
          }
        }
      }
      else
      {
        if (tSize != aSize || command.position < 2)
        {
          int copylen = Math.min(command.position,
                  annotations[a].annotations.length);
          if (copylen > 0)
          {
            System.arraycopy(annotations[a].annotations, 0, temp, 0,
                    copylen); // command.position);
          }

          Annotation[] deleted = new Annotation[command.number];
          if (copylen >= command.position)
          {
            copylen = Math.min(command.number,
                    annotations[a].annotations.length - command.position);
            if (copylen > 0)
            {
              System.arraycopy(annotations[a].annotations, command.position,
                      deleted, 0, copylen); // command.number);
            }
          }

          command.deletedAnnotations.put(annotations[a].annotationId,
                  deleted);
          if (annotations[a].annotations.length > command.position
                  + command.number)
          {
            System.arraycopy(annotations[a].annotations,
                    command.position + command.number, temp,
                    command.position, annotations[a].annotations.length
                            - command.position - command.number); // aSize
          }
        }
        else
        {
          int dSize = aSize - command.position;

          if (dSize > 0)
          {
            Annotation[] deleted = new Annotation[command.number];
            System.arraycopy(annotations[a].annotations, command.position,
                    deleted, 0, dSize);

            command.deletedAnnotations.put(annotations[a].annotationId,
                    deleted);

            tSize = Math.min(annotations[a].annotations.length,
                    command.position);
            temp = new Annotation[tSize];
            System.arraycopy(annotations[a].annotations, 0, temp, 0, tSize);
          }
          else
          {
            temp = annotations[a].annotations;
          }
        }
      }

      annotations[a].annotations = temp;
    }
  }

  /**
   * Restores features to the state before a Cut.
   * <ul>
   * <li>re-add any features deleted by the cut</li>
   * <li>remove any truncated features created by the cut</li>
   * <li>shift right any features to the right of the cut</li>
   * </ul>
   * 
   * @param command
   *          the Cut command
   * @param seq
   *          the sequence the Cut applied to
   * @param start
   *          the start residue position of the cut
   * @param length
   *          the number of residues cut
   * @param sameDatasetSequence
   *          true if dataset sequence and frame of reference were left
   *          unchanged by the Cut
   */
  final static void undoCutFeatures(Edit command, SequenceI seq,
          final int start, final int length, boolean sameDatasetSequence)
  {
    SequenceI sequence = seq.getDatasetSequence();
    if (sequence == null)
    {
      sequence = seq;
    }

    /*
     * shift right features that lie to the right of the restored cut (but not 
     * if dataset sequence unchanged - so coordinates were changed by Cut)
     */
    if (!sameDatasetSequence)
    {
      /*
       * shift right all features right of and not 
       * contiguous with the cut position
       */
      seq.getFeatures().shiftFeatures(start + 1, length);

      /*
       * shift right any features that start at the cut position,
       * unless they were truncated
       */
      List<SequenceFeature> sfs = seq.getFeatures().findFeatures(start,
              start);
      for (SequenceFeature sf : sfs)
      {
        if (sf.getBegin() == start)
        {
          if (!command.truncatedFeatures.containsKey(seq)
                  || !command.truncatedFeatures.get(seq).contains(sf))
          {
            /*
             * feature was shifted left to cut position (not truncated),
             * so shift it back right
             */
            SequenceFeature shifted = new SequenceFeature(sf,
                    sf.getBegin() + length, sf.getEnd() + length,
                    sf.getFeatureGroup(), sf.getScore());
            seq.addSequenceFeature(shifted);
            seq.deleteFeature(sf);
          }
        }
      }
    }

    /*
     * restore any features that were deleted or truncated
     */
    if (command.deletedFeatures != null
            && command.deletedFeatures.containsKey(seq))
    {
      for (SequenceFeature deleted : command.deletedFeatures.get(seq))
      {
        sequence.addSequenceFeature(deleted);
      }
    }

    /*
     * delete any truncated features
     */
    if (command.truncatedFeatures != null
            && command.truncatedFeatures.containsKey(seq))
    {
      for (SequenceFeature amended : command.truncatedFeatures.get(seq))
      {
        sequence.deleteFeature(amended);
      }
    }
  }

  /**
   * Returns the list of edit commands wrapped by this object.
   * 
   * @return
   */
  public List<Edit> getEdits()
  {
    return this.edits;
  }

  /**
   * Returns a map whose keys are the dataset sequences, and values their
   * aligned sequences before the command edit list was applied. The aligned
   * sequences are copies, which may be updated without affecting the originals.
   * 
   * The command holds references to the aligned sequences (after editing). If
   * the command is an 'undo',then the prior state is simply the aligned state.
   * Otherwise, we have to derive the prior state by working backwards through
   * the edit list to infer the aligned sequences before editing.
   * 
   * Note: an alternative solution would be to cache the 'before' state of each
   * edit, but this would be expensive in space in the common case that the
   * original is never needed (edits are not mirrored).
   * 
   * @return
   * @throws IllegalStateException
   *           on detecting an edit command of a type that can't be unwound
   */
  public Map<SequenceI, SequenceI> priorState(boolean forUndo)
  {
    Map<SequenceI, SequenceI> result = new HashMap<>();
    if (getEdits() == null)
    {
      return result;
    }
    if (forUndo)
    {
      for (Edit e : getEdits())
      {
        for (SequenceI seq : e.getSequences())
        {
          SequenceI ds = seq.getDatasetSequence();
          // SequenceI preEdit = result.get(ds);
          if (!result.containsKey(ds))
          {
            /*
             * copy sequence including start/end (but don't use copy constructor
             * as we don't need annotations)
             */
            SequenceI preEdit = new Sequence("", seq.getSequenceAsString(),
                    seq.getStart(), seq.getEnd());
            preEdit.setDatasetSequence(ds);
            result.put(ds, preEdit);
          }
        }
      }
      return result;
    }

    /*
     * Work backwards through the edit list, deriving the sequences before each
     * was applied. The final result is the sequence set before any edits.
     */
    Iterator<Edit> editList = new ReverseListIterator<>(getEdits());
    while (editList.hasNext())
    {
      Edit oldEdit = editList.next();
      Action action = oldEdit.getAction();
      int position = oldEdit.getPosition();
      int number = oldEdit.getNumber();
      final char gap = oldEdit.getGapCharacter();
      for (SequenceI seq : oldEdit.getSequences())
      {
        SequenceI ds = seq.getDatasetSequence();
        SequenceI preEdit = result.get(ds);
        if (preEdit == null)
        {
          preEdit = new Sequence("", seq.getSequenceAsString(),
                  seq.getStart(), seq.getEnd());
          preEdit.setDatasetSequence(ds);
          result.put(ds, preEdit);
        }
        /*
         * 'Undo' this edit action on the sequence (updating the value in the
         * map).
         */
        if (ds != null)
        {
          if (action == Action.DELETE_GAP)
          {
            preEdit.setSequence(new String(StringUtils.insertCharAt(
                    preEdit.getSequence(), position, number, gap)));
          }
          else if (action == Action.INSERT_GAP)
          {
            preEdit.setSequence(new String(StringUtils.deleteChars(
                    preEdit.getSequence(), position, position + number)));
          }
          else
          {
            System.err.println("Can't undo edit action " + action);
            // throw new IllegalStateException("Can't undo edit action " +
            // action);
          }
        }
      }
    }
    return result;
  }

  public class Edit
  {
    SequenceI[] oldds;

    /**
     * start and end of sequence prior to edit
     */
    Range[] oldStartEnd;

    boolean fullAlignmentHeight = false;

    Map<SequenceI, AlignmentAnnotation[]> deletedAnnotationRows;

    Map<String, Annotation[]> deletedAnnotations;

    /*
     * features deleted by the cut (re-add on Undo)
     * (including the original of any shortened features)
     */
    Map<SequenceI, List<SequenceFeature>> deletedFeatures;

    /*
     * shortened features added by the cut (delete on Undo)
     */
    Map<SequenceI, List<SequenceFeature>> truncatedFeatures;

    AlignmentI al;

    final Action command;

    char[][] string;

    SequenceI[] seqs;

    int[] alIndex;

    int position;

    int number;

    char gapChar;

    /*
     * flag that identifies edits inserted to balance 
     * user edits in a 'locked editing' region
     */
    private boolean systemGenerated;

    public Edit(Action cmd, SequenceI[] sqs, int pos, int count, char gap)
    {
      this.command = cmd;
      this.seqs = sqs;
      this.position = pos;
      this.number = count;
      this.gapChar = gap;
    }

    Edit(Action cmd, SequenceI[] sqs, int pos, int count, AlignmentI align)
    {
      this(cmd, sqs, pos, count, align.getGapCharacter());

      this.al = align;

      alIndex = new int[sqs.length];
      for (int i = 0; i < sqs.length; i++)
      {
        alIndex[i] = align.findIndex(sqs[i]);
      }

      fullAlignmentHeight = (align.getHeight() == sqs.length);
    }

    /**
     * Constructor given a REPLACE command and the replacement string
     * 
     * @param cmd
     * @param sqs
     * @param pos
     * @param count
     * @param align
     * @param replace
     */
    Edit(Action cmd, SequenceI[] sqs, int pos, int count, AlignmentI align,
            String replace)
    {
      this(cmd, sqs, pos, count, align);

      string = new char[sqs.length][];
      for (int i = 0; i < sqs.length; i++)
      {
        string[i] = replace.toCharArray();
      }
    }

    public SequenceI[] getSequences()
    {
      return seqs;
    }

    public int getPosition()
    {
      return position;
    }

    public Action getAction()
    {
      return command;
    }

    public int getNumber()
    {
      return number;
    }

    public char getGapCharacter()
    {
      return gapChar;
    }

    public void setSystemGenerated(boolean b)
    {
      systemGenerated = b;
    }

    public boolean isSystemGenerated()
    {
      return systemGenerated;
    }
  }

  /**
   * Returns an iterator over the list of edit commands which traverses the list
   * either forwards or backwards.
   * 
   * @param forwards
   * @return
   */
  public Iterator<Edit> getEditIterator(boolean forwards)
  {
    if (forwards)
    {
      return getEdits().iterator();
    }
    else
    {
      return new ReverseListIterator<>(getEdits());
    }
  }

  /**
   * Adjusts features for Cut, and saves details of changes made to allow Undo
   * <ul>
   * <li>features left of the cut are unchanged</li>
   * <li>features right of the cut are shifted left</li>
   * <li>features internal to the cut region are deleted</li>
   * <li>features that overlap or span the cut are shortened</li>
   * <li>the originals of any deleted or shortened features are saved, to re-add
   * on Undo</li>
   * <li>any added (shortened) features are saved, to delete on Undo</li>
   * </ul>
   * 
   * @param command
   * @param seq
   * @param fromPosition
   * @param toPosition
   * @param cutIsInternal
   */
  protected static void cutFeatures(Edit command, SequenceI seq,
          int fromPosition, int toPosition, boolean cutIsInternal)
  {
    /* 
     * if the cut is at start or end of sequence
     * then we don't modify the sequence feature store
     */
    if (!cutIsInternal)
    {
      return;
    }
    List<SequenceFeature> added = new ArrayList<>();
    List<SequenceFeature> removed = new ArrayList<>();

    SequenceFeaturesI featureStore = seq.getFeatures();
    if (toPosition < fromPosition || featureStore == null)
    {
      return;
    }

    int cutStartPos = fromPosition;
    int cutEndPos = toPosition;
    int cutWidth = cutEndPos - cutStartPos + 1;

    synchronized (featureStore)
    {
      /*
       * get features that overlap the cut region
       */
      List<SequenceFeature> toAmend = featureStore.findFeatures(cutStartPos,
              cutEndPos);

      /*
       * add any contact features that span the cut region
       * (not returned by findFeatures)
       */
      for (SequenceFeature contact : featureStore.getContactFeatures())
      {
        if (contact.getBegin() < cutStartPos
                && contact.getEnd() > cutEndPos)
        {
          toAmend.add(contact);
        }
      }

      /*
       * adjust start-end of overlapping features;
       * delete features enclosed by the cut;
       * delete partially overlapping contact features
       */
      for (SequenceFeature sf : toAmend)
      {
        int sfBegin = sf.getBegin();
        int sfEnd = sf.getEnd();
        int newBegin = sfBegin;
        int newEnd = sfEnd;
        boolean toDelete = false;
        boolean follows = false;

        if (sfBegin >= cutStartPos && sfEnd <= cutEndPos)
        {
          /*
           * feature lies within cut region - delete it
           */
          toDelete = true;
        }
        else if (sfBegin < cutStartPos && sfEnd > cutEndPos)
        {
          /*
           * feature spans cut region - left-shift the end
           */
          newEnd -= cutWidth;
        }
        else if (sfEnd <= cutEndPos)
        {
          /*
           * feature overlaps left of cut region - truncate right
           */
          newEnd = cutStartPos - 1;
          if (sf.isContactFeature())
          {
            toDelete = true;
          }
        }
        else if (sfBegin >= cutStartPos)
        {
          /*
           * remaining case - feature overlaps right
           * truncate left, adjust end of feature
           */
          newBegin = cutIsInternal ? cutStartPos : cutEndPos + 1;
          newEnd = newBegin + sfEnd - cutEndPos - 1;
          if (sf.isContactFeature())
          {
            toDelete = true;
          }
        }

        seq.deleteFeature(sf);
        if (!follows)
        {
          removed.add(sf);
        }
        if (!toDelete)
        {
          SequenceFeature copy = new SequenceFeature(sf, newBegin, newEnd,
                  sf.getFeatureGroup(), sf.getScore());
          seq.addSequenceFeature(copy);
          if (!follows)
          {
            added.add(copy);
          }
        }
      }

      /*
       * and left shift any features lying to the right of the cut region
       */

      featureStore.shiftFeatures(cutEndPos + 1, -cutWidth);
    }

    /*
     * save deleted and amended features, so that Undo can 
     * re-add or delete them respectively
     */
    if (command.deletedFeatures == null)
    {
      command.deletedFeatures = new HashMap<>();
    }
    if (command.truncatedFeatures == null)
    {
      command.truncatedFeatures = new HashMap<>();
    }
    command.deletedFeatures.put(seq, removed);
    command.truncatedFeatures.put(seq, added);
  }
}
