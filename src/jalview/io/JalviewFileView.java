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

import jalview.util.MessageManager;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

public class JalviewFileView extends FileView
{
  private static Map<String, String> extensions;

  private static Map<String, ImageIcon> icons;

  private void loadExtensions()
  {
    extensions = new HashMap<>();
    for (FileFormatI ff : FileFormats.getInstance().getFormats())
    {
      String desc = ff.getName() + " file";
      String exts = ff.getExtensions();
      for (String ext : exts.split(","))
      {
        ext = ext.trim().toLowerCase(Locale.ROOT);
        extensions.put(ext, desc + ("jar".equals(ext) ? " (old)" : ""));
      }
    }
  }

  @Override
  public String getTypeDescription(File f)
  {
    String extension = getExtension(f);

    String type = getDescriptionForExtension(extension);

    if (extension != null)
    {
      if (extensions.containsKey(extension))
      {
        type = extensions.get(extension).toString();
      }
    }

    return type;
  }

  private String getDescriptionForExtension(String extension)
  {
    synchronized (this)
    {
      if (extensions == null)
      {
        loadExtensions();
      }
    }
    return extensions.get(extension);
  }

  @Override
  public Icon getIcon(File f)
  {
    String extension = getExtension(f);
    Icon icon = null;
    String type = getDescriptionForExtension(extension);

    if (type == null)
    {
      Iterator<String> it = extensions.keySet().iterator();
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
          extension = ext;
          type = getDescriptionForExtension(extension)
                  + MessageManager.getString("label.backup");
          break;
        }
      }
    }

    if (type != null)
    {
      icon = getImageIcon("/images/file.png");
    }

    return icon;
  }

  /**
   * Returns the extension of a file (part of the name after the last period),
   * in lower case, or null if the name ends in or does not include a period.
   */
  public static String getExtension(File f)
  {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if ((i > 0) && (i < (s.length() - 1)))
    {
      ext = s.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    return ext;
  }

  /**
   * Returns an ImageIcon, or null if the file was not found
   * 
   * @param filePath
   */
  protected ImageIcon getImageIcon(String filePath)
  {
    /*
     * we reuse a single icon object per path here
     */
    synchronized (this)
    {
      if (icons == null)
      {
        icons = new HashMap<>();
      }
      if (!icons.containsKey(filePath))
      {
        ImageIcon icon = null;
        URL imgURL = JalviewFileView.class.getResource(filePath);
        if (imgURL != null)
        {
          icon = new ImageIcon(imgURL);
        }
        else
        {
          System.err.println(
                  "JalviewFileView.createImageIcon: Couldn't find file: "
                          + filePath);
        }
        icons.put(filePath, icon);
      }
    }

    /*
     * return the image from the table (which may be null if
     * icon creation failed)
     */
    return icons.get(filePath);
  }
}
