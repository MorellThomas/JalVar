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

import jalview.datamodel.ContactMatrix;
import jalview.datamodel.SequenceI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A file parser for contact prediction files.
 * 
 * An example file is the following
 * 
 * <pre>
 * 
 * </pre>
 * 
 * 
 * @author jim procter
 * 
 */
public class PContactPredictionFile extends AlignFile
{
  protected static final String CONTACT_PREDICTION = "CONTACT_PREDICTION";

  public PContactPredictionFile(String inFile,
          DataSourceType fileSourceType) throws IOException
  {
    super(inFile, fileSourceType);

  }

  public PContactPredictionFile(FileParse source) throws IOException
  {
    super(source);
  }

  Integer fWidth;

  List<ContactMatrix> models = new ArrayList<ContactMatrix>();

  public List<ContactMatrix> getContactMatrices()
  {
    return models;
  }

  /*
   * RaptorX pattern:
   * for a contact prediction
   * Target sequence
   * alignment for target sequence
   * contact matrix for sequence
   * models generated that fit matrix
   */

  /*
   * TODO: create annotation rows from contact matrices.
   * (non-Javadoc)
   * @see jalview.io.AlignFile#parse()
   */
  @Override
  public void parse() throws IOException
  {
    String line;
    /*
     * stash any header lines if we've been given a CASP-RR file 
     */
    Map<String, String> header = new HashMap<String, String>();
    ContactMatrix cm = null;

    while ((line = nextLine()) != null)
    {
      int left, right;
      double strength = Float.NaN;
      String parts[] = line.split("\\s+");

      // check for header tokens in parts[0]

      // others - stash details
      // MODEL - start a new matrix
      // skip comments ?

      if (parts.length == 3) // and all are integers
      {

        if (cm == null)
        {
          cm = new ContactMatrix(true)
          {
            @Override
            public String getType()
            {
              return CONTACT_PREDICTION;
            }

            @Override
            public int getHeight()
            {
              // TODO Auto-generated method stub
              // return maximum contact height
              return 0;
            }

            @Override
            public int getWidth()
            {
              // TODO Auto-generated method stub
              // return total number of residues with contacts
              return 0;
            }
          };
          models.add(cm);
        }

        try
        {
          left = Integer.parseInt(parts[0]);
          right = Integer.parseInt(parts[1]);
          strength = Double.parseDouble(parts[2]);
        } catch (Exception x)
        {
          error = true;
          errormessage = "Couldn't process line: " + x.getLocalizedMessage()
                  + "\n" + line;
          return;
        }
        cm.addContact(left, right, (float) strength);
      }
    }
    // TODO COMPLETE
    throw (new Error("Not Implemented yet."));
  }

  @Override
  public String print(SequenceI[] sqs, boolean jvsuffix)
  {
    // TODO Auto-generated method stub
    return "Not valid.";
  }
}
