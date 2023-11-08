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

import jalview.api.ComplexAlignFile;
import jalview.api.FeatureSettingsModelI;
import jalview.api.FeaturesDisplayedI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;

import java.io.IOException;
import java.io.StringReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlFile extends AlignFile implements ComplexAlignFile
{
  public static final String FILE_EXT = "html";

  public static final String FILE_DESC = "HTML";

  private String globalColourScheme;

  private boolean showSeqFeatures;

  private HiddenColumns hiddenColumns;

  private SequenceI[] hiddenSequences;

  private FeaturesDisplayedI displayedFeatures;

  public HtmlFile()
  {
    super();
  }

  public HtmlFile(FileParse source) throws IOException
  {
    super(source);
  }

  public HtmlFile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  @Override
  public void parse() throws IOException
  {
    Element content = null;
    Document doc = null;
    try
    {
      StringBuilder htmlData = new StringBuilder();
      String currentLine;
      while ((currentLine = nextLine()) != null)
      {
        htmlData.append(currentLine);
      }
      doc = Jsoup.parse(htmlData.toString());
    } catch (OutOfMemoryError oom)
    {
      errormessage = "Not enough memory to process HTML document";
      throw new IOException(errormessage);
    }

    try
    {
      boolean contentFromDiv = true;
      // search for BioJSON data in div element with id seqData
      content = doc.select("div[id=seqData]").first();
      if (content == null)
      {
        contentFromDiv = false;
        // search for BioJSON data in input element with id seqData
        content = doc.getElementById("seqData");
      }

      if (content == null)
      {
        errormessage = "The html document is not embedded with BioJSON data";
        throw new IOException(errormessage);
      }
      JSONFile jsonFile = new JSONFile().parse(new StringReader(
              contentFromDiv ? content.text() : content.val()));
      this.seqs = jsonFile.getSeqs();
      this.seqGroups = jsonFile.getSeqGroups();
      this.annotations = jsonFile.getAnnotations();
      this.showSeqFeatures = jsonFile.isShowSeqFeatures();
      this.globalColourScheme = jsonFile.getGlobalColourScheme();
      this.hiddenSequences = jsonFile.getHiddenSequences();
      this.hiddenColumns = jsonFile.getHiddenColumns();
      this.displayedFeatures = jsonFile.getDisplayedFeatures();
    } catch (Exception e)
    {
      throw e;
    }
  }

  @Override
  public String print(SequenceI[] sqs, boolean jvsuffix)
  {
    throw new UnsupportedOperationException(
            "Print method of HtmlFile is not supported!");
  }

  @Override
  public boolean isShowSeqFeatures()
  {
    return showSeqFeatures;
  }

  public void setShowSeqFeatures(boolean showSeqFeatures)
  {
    this.showSeqFeatures = showSeqFeatures;
  }

  @Override
  public String getGlobalColourScheme()
  {
    return globalColourScheme;
  }

  public void setColourScheme(String globalColourScheme)
  {
    this.globalColourScheme = globalColourScheme;
  }

  @Override
  public HiddenColumns getHiddenColumns()
  {
    return hiddenColumns;
  }

  public void setHiddenColumns(HiddenColumns hidden)
  {
    this.hiddenColumns = hidden;
  }

  @Override
  public SequenceI[] getHiddenSequences()
  {
    return hiddenSequences;
  }

  public void setHiddenSequences(SequenceI[] hiddenSequences)
  {
    this.hiddenSequences = hiddenSequences;
  }

  @Override
  public FeaturesDisplayedI getDisplayedFeatures()
  {
    return displayedFeatures;
  }

  /**
   * Returns a descriptor for suitable feature display settings with
   * <ul>
   * <li>ResNums or insertions features visible</li>
   * <li>insertions features coloured red</li>
   * <li>ResNum features coloured by label</li>
   * <li>Insertions displayed above (on top of) ResNums</li>
   * </ul>
   */
  @Override
  public FeatureSettingsModelI getFeatureColourScheme()
  {
    return new PDBFeatureSettings();
  }

}
