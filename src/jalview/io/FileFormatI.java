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
package jalview.io;

import jalview.datamodel.AlignmentI;

import java.io.IOException;

public interface FileFormatI
{
  AlignmentFileReaderI getReader(FileParse source) throws IOException;

  AlignmentFileWriterI getWriter(AlignmentI al);

  boolean isComplexAlignFile();

  /**
   * Answers the display name of the file format (as for example shown in menu
   * options). This name should not be locale (language) dependent.
   */
  String getName();

  /**
   * Returns a comma-separated list of file extensions associated with the
   * format
   * 
   * @return
   */
  String getExtensions();

  /**
   * Answers true if the format is one that Jalview can read. This implies that
   * the format provides an implementation for getReader which can parse a data
   * source for sequence data. Readable formats are included in the options in
   * the open file dialogue.
   * 
   * @return
   */
  boolean isReadable();

  /**
   * Answers true if the format is one that Jalview can write. This implies that
   * the object returned by getWriter provides an implementation of the print()
   * method. Writable formats are included in the options in the Save As file
   * dialogue, and the 'output to Textbox' option (if text format).
   * 
   * @return
   */
  boolean isWritable();

  /**
   * Answers true if the format is one that Jalview can output as text, e.g. to
   * a text box
   * 
   * @return
   */
  boolean isTextFormat();

  /**
   * Answers true if the file format is one that provides 3D structure data
   * 
   * @return
   */
  boolean isStructureFile();
}
