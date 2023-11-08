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
package jalview.datamodel;

/**
 * An immutable object representing one or more residue and corresponding
 * alignment column positions for a sequence
 */
public class SequenceCursor
{
  /**
   * the aligned sequence this cursor applies to
   */
  public final SequenceI sequence;

  /**
   * residue position in sequence (start...), 0 if undefined
   */
  public final int residuePosition;

  /**
   * column position (1...) corresponding to residuePosition, or 0 if undefined
   */
  public final int columnPosition;

  /**
   * column position (1...) of first residue in the sequence, or 0 if undefined
   */
  public final int firstColumnPosition;

  /**
   * column position (1...) of last residue in the sequence, or 0 if undefined
   */
  public final int lastColumnPosition;

  /**
   * a token which may be used to check whether this cursor is still valid for
   * its sequence (allowing it to be ignored if the sequence has changed)
   */
  public final int token;

  /**
   * Constructor
   * 
   * @param seq
   *          sequence this cursor applies to
   * @param resPos
   *          residue position in sequence (start..)
   * @param column
   *          column position in alignment (1..)
   * @param tok
   *          a token that may be validated by the sequence to check the cursor
   *          is not stale
   */
  public SequenceCursor(SequenceI seq, int resPos, int column, int tok)
  {
    this(seq, resPos, column, 0, 0, tok);
  }

  /**
   * Constructor
   * 
   * @param seq
   *          sequence this cursor applies to
   * @param resPos
   *          residue position in sequence (start..)
   * @param column
   *          column position in alignment (1..)
   * @param firstResCol
   *          column position of the first residue in the sequence (1..), or 0
   *          if not known
   * @param lastResCol
   *          column position of the last residue in the sequence (1..), or 0 if
   *          not known
   * @param tok
   *          a token that may be validated by the sequence to check the cursor
   *          is not stale
   */
  public SequenceCursor(SequenceI seq, int resPos, int column,
          int firstResCol, int lastResCol, int tok)
  {
    sequence = seq;
    residuePosition = resPos;
    columnPosition = column;
    firstColumnPosition = firstResCol;
    lastColumnPosition = lastResCol;
    token = tok;
  }

  @Override
  public int hashCode()
  {
    int hash = 31 * residuePosition;
    hash = 31 * hash + columnPosition;
    hash = 31 * hash + token;
    if (sequence != null)
    {
      hash += sequence.hashCode();
    }
    return hash;
  }

  /**
   * Two cursors are equal if they refer to the same sequence object and have
   * the same residue position, column position and token value
   */
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof SequenceCursor))
    {
      return false;
    }
    SequenceCursor sc = (SequenceCursor) obj;
    return sequence == sc.sequence && residuePosition == sc.residuePosition
            && columnPosition == sc.columnPosition && token == sc.token;
  }

  @Override
  public String toString()
  {
    String name = sequence == null ? "" : sequence.getName();
    return String.format("%s:Pos%d:Col%d:startCol%d:endCol%d:tok%d", name,
            residuePosition, columnPosition, firstColumnPosition,
            lastColumnPosition, token);
  }
}
