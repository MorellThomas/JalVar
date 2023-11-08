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
package jalview.bin;

import jalview.datamodel.AlignmentI;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileParse;
import jalview.io.IdentifyFile;

import java.applet.Applet;
import java.io.InputStream;

public class JalviewLiteURLRetrieve extends Applet
{

  private static final long serialVersionUID = 1L;

  /**
   * This is the default constructor
   */
  public JalviewLiteURLRetrieve()
  {
    super();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  @Override
  public void init()
  {
    this.setSize(300, 200);
    String file = getParameter("file");
    if (file == null)
    {
      System.out
              .println("Specify a resource to read on the file parameter");
      return;
    }
    DataSourceType protocol = null;
    try
    {
      System.out.println("Loading thread started with:\n>>file\n" + file
              + ">>endfile");
      // This might throw a security exception in certain browsers
      // Netscape Communicator for instance.
      try
      {
        boolean rtn = false;
        InputStream is = getClass().getResourceAsStream("/" + file);
        if (is != null)
        {
          rtn = true;
          is.close();
        }
        System.err.println("Resource '" + file + "' was "
                + (rtn ? "" : "not") + " located by classloader.");
        if (rtn)
        {
          protocol = DataSourceType.CLASSLOADER;
        }

      } catch (Exception ex)
      {
        System.out.println(
                "Exception checking resources: " + file + " " + ex);
      }
      if (file.indexOf("://") > -1)
      {
        protocol = DataSourceType.URL;
      }
      else
      {
        // skipping codebase prepend check.
        protocol = DataSourceType.FILE;
      }

      System.out.println("Trying to get contents of resource:");
      FileParse fp = new FileParse(file, protocol);
      if (fp.isValid())
      {
        String ln = null;
        while ((ln = fp.nextLine()) != null)
        {
          System.out.print(ln);
        }
        fp.close();
      }
      else
      {
        System.out.println("Resource at " + file
                + " cannot be read with protocol==" + protocol);
        return;
      }
      FileFormatI format = FileFormats.getInstance()
              .forName(getParameter("format"));
      if (format == null)
      {
        format = new IdentifyFile().identify(file, protocol);
        System.out.println("Format is " + format);
      }
      else
      {
        System.out.println("User specified Format is " + format);
      }
      AlignmentI al = null;
      try
      {
        al = new AppletFormatAdapter().readFile(file, protocol, format);
      } catch (java.io.IOException ex)
      {
        System.err.println("Failed to open the file.");
        ex.printStackTrace();
      }
      if (al != null)
      {
        System.out.println(new AppletFormatAdapter()
                .formatSequences(FileFormat.Fasta, al, false));
      }
    } catch (Exception e)
    {
      System.err.println("bailing out : Unexpected exception:");
      e.printStackTrace();
    }
  }

}
