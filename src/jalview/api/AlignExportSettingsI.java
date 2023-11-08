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

package jalview.api;

/**
 * An interface describing settings for including or excluding data when an
 * alignment is output
 *
 */
public interface AlignExportSettingsI
{
  /**
   * Answers true if hidden sequences should be exported, false if not
   * 
   * @return
   */
  boolean isExportHiddenSequences();

  /**
   * Answers true if hidden columns should be exported, false if not
   * 
   * @return
   */
  boolean isExportHiddenColumns();

  /**
   * Answers true if Annotations should be exported. This is available for
   * complex flat file exports like JSON, HTML, GFF.
   * 
   * @return
   */
  boolean isExportAnnotations();

  /**
   * Answers true if Sequence Features should be exported. This is available for
   * complex flat file exports like JSON, HTML, GFF.
   * 
   * @return
   */
  boolean isExportFeatures();

  /**
   * Answers true if Sequence Groups should be exported. This is available for
   * complex flat file exports like JSON, HTML, GFF.
   * 
   * @return
   */
  boolean isExportGroups();

  void setExportHiddenSequences(boolean b);

  void setExportHiddenColumns(boolean b);

  void setExportAnnotations(boolean b);

  void setExportFeatures(boolean b);

  void setExportGroups(boolean b);
}
