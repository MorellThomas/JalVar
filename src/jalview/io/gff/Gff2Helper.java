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
package jalview.io.gff;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Gff2Helper extends GffHelperBase
{
  /**
   * GFF2 uses space character to delimit name/value pairs on column 9
   * 
   * @param text
   * @return
   */
  public static Map<String, List<String>> parseNameValuePairs(String text)
  {
    return parseNameValuePairs(text, ";", ' ', ",");
  }

  /**
   * Default processing if not overridden is just to construct a sequence
   * feature
   */
  @Override
  public SequenceFeature processGff(SequenceI seq, String[] gff,
          AlignmentI align, List<SequenceI> newseqs,
          boolean relaxedIdMatching) throws IOException
  {
    Map<String, List<String>> attributes = null;
    if (gff.length > ATTRIBUTES_COL)
    {
      attributes = parseNameValuePairs(gff[ATTRIBUTES_COL]);
    }
    return buildSequenceFeature(gff, attributes);
  }

}
