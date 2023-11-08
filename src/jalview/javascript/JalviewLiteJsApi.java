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
package jalview.javascript;

import jalview.appletgui.AlignFrame;

/**
 * The following public methods may be called
 * externally, eg via javascript in an HTML page.
 * 
 * <br><em>TODO: introduce abstract interface for jalview.appletgui.AlignFrame</em><br>
 * 
 * Most function arguments are strings, which contain serialised versions of lists.
 * Lists of things are separated by a separator character - either the default or a user supplied one.
 * Ranges and positions on an alignment or sequence can be specified as a list, where an item containing a single number is a single position, and an item like 1-2 specifies columns 1 and 2 as a range.
 */

/**
 * @author jimp
 * 
 */
public interface JalviewLiteJsApi
{

  /**
   * @return String list of selected sequence IDs, each terminated by the
   *         'boolean not' character (""+0x00AC) or (&#172;)
   */
  public abstract String getSelectedSequences();

  /**
   * @param sep
   *          separator string or null for default
   * @return String list of selected sequence IDs, each terminated by given
   *         separator string
   */
  public abstract String getSelectedSequences(String sep);

  /**
   * @param alf
   *          alignframe containing selection
   * @return String list of selected sequence IDs, each terminated by current
   *         default separator sequence
   * 
   */
  public abstract String getSelectedSequencesFrom(AlignFrame alf);

  /**
   * get list of selected sequence IDs separated by given separator
   * 
   * @param alf
   *          window containing selection
   * @param sep
   *          separator string to use - default is 'boolean not'
   * @return String list of selected sequence IDs, each terminated by the given
   *         separator
   */
  public abstract String getSelectedSequencesFrom(AlignFrame alf,
          String sep);

  /**
   * 
   * @param sequenceId
   *          id of sequence to highlight
   * @param position
   *          integer position [ tobe implemented or range ] on sequence
   * @param alignedPosition
   *          true/false/empty string - indicate if position is an alignment
   *          column or unaligned sequence position
   */
  public abstract void highlight(String sequenceId, String position,
          String alignedPosition);

  /**
   * 
   * @param sequenceId
   *          id of sequence to highlight
   * @param position
   *          integer position [ tobe implemented or range ] on sequence
   * @param alignedPosition
   *          false, blank or something else - indicate if position is an
   *          alignment column or unaligned sequence position
   */
  public abstract void highlightIn(AlignFrame alf, String sequenceId,
          String position, String alignedPosition);

  /**
   * select regions of the currrent alignment frame
   * 
   * @param sequenceIds
   *          String separated list of sequence ids or empty string
   * @param columns
   *          String separated list { column range or column, ..} or empty
   *          string
   */
  public abstract void select(String sequenceIds, String columns);

  /**
   * select regions of the currrent alignment frame
   * 
   * @param toselect
   *          String separated list { column range, seq1...seqn sequence ids }
   * @param sep
   *          separator between toselect fields
   */
  public abstract void select(String sequenceIds, String columns,
          String sep);

  /**
   * select regions of the given alignment frame
   * 
   * @param alf
   * @param toselect
   *          String separated list { column range, seq1...seqn sequence ids }
   * @param sep
   *          separator between toselect fields
   */
  public abstract void selectIn(AlignFrame alf, String sequenceIds,
          String columns);

  /**
   * select regions of the given alignment frame
   * 
   * @param alf
   * @param toselect
   *          String separated list { column range, seq1...seqn sequence ids }
   * @param sep
   *          separator between toselect fields
   */
  public abstract void selectIn(AlignFrame alf, String sequenceIds,
          String columns, String sep);

  /**
   * get sequences selected in current alignFrame and return their alignment in
   * format 'format' either with or without suffix
   * 
   * @param alf
   *          - where selection is
   * @param format
   *          - format of alignment file
   * @param suffix
   *          - "true" to append /start-end string to each sequence ID
   * @return selected sequences as flat file or empty string if there was no
   *         current selection
   */
  public abstract String getSelectedSequencesAsAlignment(String format,
          String suffix);

  /**
   * get sequences selected in alf and return their alignment in format 'format'
   * either with or without suffix
   * 
   * @param alf
   *          - where selection is
   * @param format
   *          - format of alignment file
   * @param suffix
   *          - "true" to append /start-end string to each sequence ID
   * @return selected sequences as flat file or empty string if there was no
   *         current selection
   */
  public abstract String getSelectedSequencesAsAlignmentFrom(AlignFrame alf,
          String format, String suffix);

  /**
   * get a separator separated list of sequence IDs reflecting the order of the
   * current alignment
   * 
   * @return
   */
  public abstract String getAlignmentOrder();

  /**
   * get a separator separated list of sequence IDs reflecting the order of the
   * alignment in alf
   * 
   * @param alf
   * @return
   */
  public abstract String getAlignmentOrderFrom(AlignFrame alf);

  /**
   * get a sep separated list of sequence IDs reflecting the order of the
   * alignment in alf
   * 
   * @param alf
   * @param sep
   *          - separator to use
   * @return
   */
  public abstract String getAlignmentOrderFrom(AlignFrame alf, String sep);

  /**
   * re-order the current alignment using the given list of sequence IDs
   * 
   * @param order
   *          - sep separated list
   * @param undoName
   *          - string to use when referring to ordering action in undo buffer
   * @return 'true' if alignment was actually reordered. empty string if
   *         alignment did not contain sequences.
   */
  public abstract String orderBy(String order, String undoName);

  /**
   * re-order the current alignment using the given list of sequence IDs
   * separated by sep
   * 
   * @param order
   *          - sep separated list
   * @param undoName
   *          - string to use when referring to ordering action in undo buffer
   * @param sep
   * @return 'true' if alignment was actually reordered. empty string if
   *         alignment did not contain sequences.
   */
  public abstract String orderBy(String order, String undoName, String sep);

  /**
   * re-order the given alignment using the given list of sequence IDs separated
   * by sep
   * 
   * @param alf
   * @param order
   *          - sep separated list
   * @param undoName
   *          - string to use when referring to ordering action in undo buffer
   * @param sep
   * @return 'true' if alignment was actually reordered. empty string if
   *         alignment did not contain sequences.
   */
  public abstract String orderAlignmentBy(AlignFrame alf, String order,
          String undoName, String sep);

  /**
   * get alignment as format (format names FASTA, BLC, CLUSTAL, MSF, PILEUP,
   * PFAM - see jalview.io.AppletFormatAdapter for full list)
   * 
   * @param format
   * @return
   */
  public abstract String getAlignment(String format);

  /**
   * get alignment displayed in alf as format
   * 
   * @param alf
   * @param format
   * @return
   */
  public abstract String getAlignmentFrom(AlignFrame alf, String format);

  /**
   * get alignment as format with jalview start-end sequence suffix appended
   * 
   * @param format
   * @param suffix
   * @return
   */
  public abstract String getAlignment(String format, String suffix);

  /**
   * get alignment displayed in alf as format with or without the jalview
   * start-end sequence suffix appended
   * 
   * @param alf
   * @param format
   * @param suffix
   * @return
   */
  public abstract String getAlignmentFrom(AlignFrame alf, String format,
          String suffix);

  /**
   * add the given features or annotation to the current alignment
   * 
   * @param annotation
   */
  public abstract void loadAnnotation(String annotation);

  /**
   * add the given features or annotation to the given alignment view
   * 
   * @param alf
   * @param annotation
   */
  public abstract void loadAnnotationFrom(AlignFrame alf,
          String annotation);

  /**
   * parse the given string as a jalview feature or GFF annotation file and
   * optionally enable feature display on the current alignFrame
   * 
   * @param features
   *          - gff or features file
   * @param autoenabledisplay
   *          - when true, feature display will be enabled if any features can
   *          be parsed from the string.
   */
  public abstract void loadFeatures(String features,
          boolean autoenabledisplay);

  /**
   * parse the given string as a jalview feature or GFF annotation file and
   * optionally enable feature display on the given alignFrame.
   * 
   * @param alf
   * @param features
   *          - gff or features file
   * @param autoenabledisplay
   *          - when true, feature display will be enabled if any features can
   *          be parsed from the string.
   * @return true if data parsed as features
   */
  public abstract boolean loadFeaturesFrom(AlignFrame alf, String features,
          boolean autoenabledisplay);

  /**
   * get the sequence features in the given format (Jalview or GFF)
   * 
   * @param format
   * @return
   */
  public abstract String getFeatures(String format);

  /**
   * get the sequence features in alf in the given format (Jalview or GFF)
   * 
   * @param alf
   * @param format
   * @return
   */
  public abstract String getFeaturesFrom(AlignFrame alf, String format);

  /**
   * get current alignment's annotation as an annotation file
   * 
   * @return
   */
  public abstract String getAnnotation();

  /**
   * get alignment view alf's annotation as an annotation file
   * 
   * @param alf
   * @return
   */
  public abstract String getAnnotationFrom(AlignFrame alf);

  /**
   * create a new view and return the alignFrame instance
   * 
   * @return
   */
  public abstract AlignFrame newView();

  /**
   * create a new view named name and return the alignFrame instance
   * 
   * @param name
   * @return
   */
  public abstract AlignFrame newView(String name);

  /**
   * create a new view on alf and return the alignFrame instance
   * 
   * @param alf
   * @return
   */
  public abstract AlignFrame newViewFrom(AlignFrame alf);

  /**
   * create a new view named name on alf
   * 
   * @param alf
   * @param name
   * @return
   */
  public abstract AlignFrame newViewFrom(AlignFrame alf, String name);

  /**
   * 
   * @param text
   *          alignment file as a string
   * @param title
   *          window title
   * @return null or new alignment frame
   */
  public abstract AlignFrame loadAlignment(String text, String title);

  /**
   * register a javascript function to handle any alignment mouseover events
   * 
   * @param listener
   *          name of javascript function (called with arguments
   *          [jalview.appletgui.AlignFrame,String(sequence id),String(column in
   *          alignment), String(position in sequence)]
   */
  public abstract void setMouseoverListener(String listener);

  /**
   * register a javascript function to handle mouseover events
   * 
   * @param af
   *          (null or specific alignframe for which events are to be listened
   *          for)
   * @param listener
   *          name of javascript function
   */
  public abstract void setMouseoverListener(AlignFrame af, String listener);

  /**
   * register a javascript function to handle any alignment selection events.
   * Events are generated when the user completes a selection event, or when the
   * user deselects all selected regions.
   * 
   * @param listener
   *          name of javascript function (called with arguments
   *          [jalview.appletgui.AlignFrame, String(sequence set id),
   *          String(separator separated list of sequences which were selected),
   *          String(separator separated list of column ranges (i.e. single
   *          number or hyphenated range) that were selected)]
   */
  public abstract void setSelectionListener(String listener);

  public abstract void setSelectionListener(AlignFrame af, String listener);

  /**
   * register a javascript function to handle events normally routed to a Jmol
   * structure viewer.
   * 
   * @param listener
   *          - javascript function (arguments are variable, see
   *          jalview.javascript.MouseOverStructureListener for full details)
   * @param modelSet
   *          - separator separated list of PDB file URIs that this viewer is
   *          handling. These files must be in the same order they appear in
   *          Jmol (e.g. first one is frame 1, second is frame 2, etc).
   * @see jalview.javascript.MouseOverStructureListener
   */
  public abstract void setStructureListener(String listener,
          String modelSet);

  /**
   * remove any callback using the given listener function and associated with
   * the given alignFrame (or null for all callbacks)
   * 
   * @param af
   *          (may be null)
   * @param listener
   *          (may be null)
   */
  public abstract void removeJavascriptListener(AlignFrame af,
          String listener);

  /**
   * send a mouseover message to all the alignment windows associated with the
   * given residue in the pdbfile
   * 
   * @param pdbResNum
   * @param chain
   * @param pdbfile
   */
  public abstract void mouseOverStructure(String pdbResNum, String chain,
          String pdbfile);

  /**
   * bind a pdb file to a sequence in the given alignFrame.
   * 
   * @param alFrame
   *          - null or specific alignFrame. This specifies the dataset that
   *          will be searched for a seuqence called sequenceId
   * @param sequenceId
   *          - sequenceId within the dataset.
   * @param pdbEntryString
   *          - the short name for the PDB file
   * @param pdbFile
   *          - pdb file - either a URL or a valid PDB file.
   * @return true if binding was as success TODO: consider making an exception
   *         structure for indicating when PDB parsing or sequenceId location
   *         fails.
   */
  public abstract boolean addPdbFile(AlignFrame alFrame, String sequenceId,
          String pdbEntryString, String pdbFile);

  /**
   * adjust horizontal/vertical scroll to make the given location the top left
   * hand corner for the given view
   * 
   * @param alf
   * @param topRow
   * @param leftHandColumn
   */
  public abstract void scrollViewToIn(AlignFrame alf, String topRow,
          String leftHandColumn);

  /**
   * adjust vertical scroll to make the given row the top one for given view
   * 
   * @param alf
   * @param topRow
   */
  public abstract void scrollViewToRowIn(AlignFrame alf, String topRow);

  /**
   * adjust horizontal scroll to make the given column the left one in the given
   * view
   * 
   * @param alf
   * @param leftHandColumn
   */
  public abstract void scrollViewToColumnIn(AlignFrame alf,
          String leftHandColumn);

  /**
   * 
   * @return
   * @see jalview.appletgui.AlignFrame#getFeatureGroups()
   */
  public abstract String getFeatureGroups();

  /**
   * @param alf
   *          alignframe to get feature groups on
   * @return
   * @see jalview.appletgui.AlignFrame#getFeatureGroups()
   */
  public abstract String getFeatureGroupsOn(AlignFrame alf);

  /**
   * @param visible
   * @return
   * @see jalview.appletgui.AlignFrame#getFeatureGroupsOfState(boolean)
   */
  public abstract String getFeatureGroupsOfState(boolean visible);

  /**
   * @param alf
   *          align frame to get groups of state visible
   * @param visible
   * @return
   * @see jalview.appletgui.AlignFrame#getFeatureGroupsOfState(boolean)
   */
  public abstract String getFeatureGroupsOfStateOn(AlignFrame alf,
          boolean visible);

  /**
   * @param groups
   *          tab separated list of group names
   * @param state
   *          true or false
   * @see jalview.appletgui.AlignFrame#setFeatureGroupState(java.lang.String[],
   *      boolean)
   */
  public abstract void setFeatureGroupStateOn(AlignFrame alf, String groups,
          boolean state);

  public abstract void setFeatureGroupState(String groups, boolean state);

  /**
   * List separator string
   * 
   * @return the separator
   */
  public abstract String getSeparator();

  /**
   * List separator string
   * 
   * @param separator
   *          the separator to set. empty string will reset separator to default
   */
  public abstract void setSeparator(String separator);

  /**
   * Retrieve fragments of a large packet of data made available by JalviewLite.
   * 
   * @param messageclass
   * @param viewId
   * @return next chunk of message
   */
  public abstract String getJsMessage(String messageclass, String viewId);

}
