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

import java.util.Locale;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileFilter;

public class JalviewFileFilter extends FileFilter
{
  public static Hashtable suffixHash = new Hashtable();

  private Map<String, JalviewFileFilter> filters = null;

  private String description = "no description";

  private String fullDescription = "full description";

  private boolean useExtensionsInDescription = true;

  private JalviewFileChooser parentJFC = null;

  public JalviewFileFilter(String extension, String description)
  {
    StringTokenizer st = new StringTokenizer(extension, ",");

    while (st.hasMoreElements())
    {
      addExtension(st.nextToken().trim());
    }

    setDescription(description);
  }

  public JalviewFileFilter(String[] filts)
  {
    this(filts, null);
  }

  public JalviewFileFilter(String[] filts, String description)
  {
    for (int i = 0; i < filts.length; i++)
    {
      // add filters one by one
      addExtension(filts[i]);
    }

    if (description != null)
    {
      setDescription(description);
    }
  }

  public String getAcceptableExtension()
  {
    return filters.keySet().iterator().next().toString();
  }

  // takes account of the fact that database is a directory
  @Override
  public boolean accept(File f)
  {

    if (f != null)
    {
      String extension = getExtension(f);

      if (f.isDirectory())
      {
        return true;
      }

      if ((extension != null) && (filters.get(extension) != null))
      {
        return true;
      }

    }

    if (parentJFC != null && parentJFC.includeBackupFiles)
    {
      Iterator<String> it = filters.keySet().iterator();
      EXTENSION: while (it.hasNext())
      {
        String ext = it.next();

        // quick negative test
        if (!f.getName().contains(ext))
        {
          continue EXTENSION;
        }

        BackupFilenameParts bfp = BackupFilenameParts
                .currentBackupFilenameParts(f.getName(), ext, true);
        if (bfp.isBackupFile())
        {
          return true;
        }
      }
    }

    return false;
  }

  public String getExtension(File f)
  {
    if (f != null)
    {
      String filename = f.getName();
      int i = filename.lastIndexOf('.');

      if ((i > 0) && (i < (filename.length() - 1)))
      {
        return filename.substring(i + 1).toLowerCase(Locale.ROOT);
      }

      ;
    }

    return "";
  }

  public void addExtension(String extension)
  {
    if (filters == null)
    {
      filters = new LinkedHashMap<>(5);
    }

    filters.put(extension.toLowerCase(Locale.ROOT), this);
    fullDescription = null;
  }

  @Override
  public String getDescription()
  {
    if (fullDescription == null)
    {
      if ((description == null) || isExtensionListInDescription())
      {
        fullDescription = (description == null) ? "("
                : (description + " (");

        // build the description from the extension list
        Iterator<String> extensions = filters.keySet().iterator();

        if (extensions != null)
        {
          fullDescription += ("." + extensions.next());

          while (extensions.hasNext())
          {
            fullDescription += (", " + extensions.next());
          }
        }

        fullDescription += ")";
      }
      else
      {
        fullDescription = description;
      }
    }

    return fullDescription;
  }

  public void setDescription(String description)
  {
    this.description = description;
    fullDescription = null;
  }

  public void setExtensionListInDescription(boolean b)
  {
    useExtensionsInDescription = b;
    fullDescription = null;
  }

  public boolean isExtensionListInDescription()
  {
    return useExtensionsInDescription;
  }

  protected void setParentJFC(JalviewFileChooser p)
  {
    this.parentJFC = p;
  }

}
