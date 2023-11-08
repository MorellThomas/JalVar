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

import jalview.api.AlignExportSettingsI;

/**
 * Export options that may be constructed as 'all' or 'none' (and further
 * adjusted if wanted)
 */
public class AlignExportSettingsAdapter implements AlignExportSettingsI
{
  private boolean exportHiddenSeqs;

  private boolean exportHiddenCols;

  private boolean exportAnnotations;

  private boolean exportFeatures;

  private boolean exportGroups;

  /**
   * Constructor sets all options to either true or false
   * 
   * @param defaultOption
   */
  public AlignExportSettingsAdapter(boolean defaultOption)
  {
    exportAnnotations = defaultOption;
    exportFeatures = defaultOption;
    exportGroups = defaultOption;
    exportHiddenCols = defaultOption;
    exportHiddenSeqs = defaultOption;
  }

  @Override
  public boolean isExportHiddenSequences()
  {
    return exportHiddenSeqs;
  }

  @Override
  public boolean isExportHiddenColumns()
  {
    return exportHiddenCols;
  }

  @Override
  public boolean isExportAnnotations()
  {
    return exportAnnotations;
  }

  @Override
  public boolean isExportFeatures()
  {
    return exportFeatures;
  }

  @Override
  public boolean isExportGroups()
  {
    return exportGroups;
  }

  public void setExportHiddenSequences(boolean exportHiddenSeqs)
  {
    this.exportHiddenSeqs = exportHiddenSeqs;
  }

  public void setExportHiddenColumns(boolean exportHiddenCols)
  {
    this.exportHiddenCols = exportHiddenCols;
  }

  public void setExportAnnotations(boolean exportAnnotations)
  {
    this.exportAnnotations = exportAnnotations;
  }

  public void setExportFeatures(boolean exportFeatures)
  {
    this.exportFeatures = exportFeatures;
  }

  public void setExportGroups(boolean exportGroups)
  {
    this.exportGroups = exportGroups;
  }

}
