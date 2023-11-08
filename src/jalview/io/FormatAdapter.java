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

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jalview.api.AlignExportSettingsI;
import jalview.api.AlignmentViewPanel;
import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.structure.StructureImportSettings;
import jalview.structure.StructureImportSettings.TFType;
import jalview.util.Comparison;

/**
 * Additional formatting methods used by the application in a number of places.
 * 
 * @author $author$
 * @version $Revision$
 */
public class FormatAdapter extends AppletFormatAdapter
{
  public FormatAdapter(AlignmentViewPanel viewpanel)
  {
    super(viewpanel);
    init();
  }

  public FormatAdapter()
  {
    super();
    init();
  }

  public FormatAdapter(AlignmentViewPanel alignPanel,
          AlignExportSettingsI settings)
  {
    super(alignPanel, settings);
  }

  private void init()
  {
    if (Cache.getDefault("STRUCT_FROM_PDB", true))
    {
      annotFromStructure = Cache.getDefault("ADD_TEMPFACT_ANN", true);
      localSecondaryStruct = Cache.getDefault("ADD_SS_ANN", true);
      serviceSecondaryStruct = Cache.getDefault("USE_RNAVIEW", true);
    }
    else
    {
      // disable all PDB annotation options
      annotFromStructure = false;
      localSecondaryStruct = false;
      serviceSecondaryStruct = false;
    }
  }

  public String formatSequences(FileFormatI format, SequenceI[] seqs,
          String[] omitHiddenColumns, int[] exportRange)
  {

    return formatSequences(format,
            replaceStrings(seqs, omitHiddenColumns, exportRange));
  }

  /**
   * create sequences with each sequence string replaced with the one given in
   * omitHiddenCOlumns
   * 
   * @param seqs
   * @param omitHiddenColumns
   * @return new sequences
   */
  public SequenceI[] replaceStrings(SequenceI[] seqs,
          String[] omitHiddenColumns, int[] startEnd)
  {
    if (omitHiddenColumns != null)
    {
      SequenceI[] tmp = new SequenceI[seqs.length];

      int startRes;
      int endRes;
      int startIndex;
      int endIndex;
      for (int i = 0; i < seqs.length; i++)
      {
        startRes = seqs[i].getStart();
        endRes = seqs[i].getEnd();
        if (startEnd != null)
        {
          startIndex = startEnd[0];
          endIndex = startEnd[1];
          // get first non-gaped residue start position
          while (Comparison.isGap(seqs[i].getCharAt(startIndex))
                  && startIndex < endIndex)
          {
            startIndex++;
          }

          // get last non-gaped residue end position
          while (Comparison.isGap(seqs[i].getCharAt(endIndex))
                  && endIndex > startIndex)
          {
            endIndex--;
          }

          startRes = seqs[i].findPosition(startIndex);
          endRes = seqs[i].findPosition(endIndex);
        }

        tmp[i] = new Sequence(seqs[i].getName(), omitHiddenColumns[i],
                startRes, endRes);
        tmp[i].setDescription(seqs[i].getDescription());
      }
      seqs = tmp;
    }
    return seqs;
  }

  /**
   * Format a vector of sequences as a flat alignment file. TODO: allow caller
   * to detect errors and warnings encountered when generating output
   * 
   * 
   * @param format
   * @param seqs
   *          vector of sequences to write
   * 
   * @return String containing sequences in desired format
   */
  public String formatSequences(FileFormatI format, SequenceI[] seqs)
  {
    boolean withSuffix = getCacheSuffixDefault(format);
    return format.getWriter(null).print(seqs, withSuffix);
  }

  public boolean getCacheSuffixDefault(FileFormatI format)
  {
    return Cache.getDefault(
            format.getName().toUpperCase(Locale.ROOT) + "_JVSUFFIX", true);
  }

  public String formatSequences(FileFormatI format, AlignmentI alignment,
          String[] omitHidden, int[] exportRange, HiddenColumns hidden)
  {
    return formatSequences(format, alignment, omitHidden, exportRange,
            getCacheSuffixDefault(format), hidden, null);
  }

  /**
   * hack function to replace sequences with visible sequence strings before
   * generating a string of the alignment in the given format.
   * 
   * @param format
   * @param alignment
   * @param omitHidden
   *          sequence strings to write out in order of sequences in alignment
   * @param colSel
   *          defines hidden columns that are edited out of annotation
   * @return string representation of the alignment formatted as format
   */
  public String formatSequences(FileFormatI format, AlignmentI alignment,
          String[] omitHidden, int[] exportRange, boolean suffix,
          HiddenColumns hidden)
  {
    return formatSequences(format, alignment, omitHidden, exportRange,
            suffix, hidden, null);
  }

  public String formatSequences(FileFormatI format, AlignmentI alignment,
          String[] omitHidden, int[] exportRange, boolean suffix,
          HiddenColumns hidden, SequenceGroup selgp)
  {
    if (omitHidden != null)
    {
      // TODO consider using AlignmentView to prune to visible region
      // TODO prune sequence annotation and groups to visible region
      // TODO: JAL-1486 - set start and end for output correctly. basically,
      // AlignmentView.getVisibleContigs does this.
      Alignment alv = new Alignment(replaceStrings(
              alignment.getSequencesArray(), omitHidden, exportRange));
      AlignmentAnnotation[] ala = alignment.getAlignmentAnnotation();
      if (ala != null)
      {
        for (int i = 0; i < ala.length; i++)
        {
          AlignmentAnnotation na = new AlignmentAnnotation(ala[i]);
          if (selgp != null)
          {
            na.makeVisibleAnnotation(selgp.getStartRes(), selgp.getEndRes(),
                    hidden);
          }
          else
          {
            na.makeVisibleAnnotation(hidden);
          }
          alv.addAnnotation(na);
        }
      }
      return this.formatSequences(format, alv, suffix);
    }
    return this.formatSequences(format, alignment, suffix);
  }

  @Override
  public AlignmentI readFile(File selectedFile, String file,
          DataSourceType sourceType, FileFormatI fileFormat,
          StructureImportSettings.TFType tempfacType) throws IOException
  {
    AlignmentI al = super.readFile(selectedFile, file, sourceType,
            fileFormat, tempfacType);
    return al;
  }

  @Override
  public AlignmentI readFile(String file, DataSourceType sourceType,
          FileFormatI fileFormat) throws IOException
  {
    AlignmentI al = super.readFile(file, sourceType, fileFormat);
    return al;
  }

  public AlignmentI readFile(File file, DataSourceType sourceType,
          FileFormatI fileFormat) throws IOException
  {
    AlignmentI al = super.readFile(file, null, sourceType, fileFormat);
    return al;
  }

  @Override
  public AlignmentI readFromFile(FileParse source, FileFormatI format)
          throws IOException
  {
    AlignmentI al = super.readFromFile(source, format);
    return al;
  }

  /**
   * Create a flat file representation of a given view or selected region of a
   * view
   * 
   * @param format
   * @param ap
   *          alignment panel originating the view
   * @return String containing flat file
   */
  public String formatSequences(FileFormatI format, AlignmentViewPanel ap,
          boolean selectedOnly)
  {
    return formatSequences(format, getCacheSuffixDefault(format), ap,
            selectedOnly);
  }

  public AlignmentI readFromFile(AlignmentFileReaderI source,
          FileFormatI format) throws IOException
  {
    FileParse fp = new FileParse(source.getInFile(),
            source.getDataSourceType());
    return readFromFile(fp, format);
  }

}
