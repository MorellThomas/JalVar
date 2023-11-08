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
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class getJavaVersion
{
  /**
   * Takes a set of Jars and/or class files as arguments. Reports the java
   * version for the classes
   */

  public static void main(String[] args) throws IOException
  {
    Hashtable observed = new Hashtable();
    for (int i = 0; i < args.length; i++)
    {
      checkClassVersion(args[i], observed);
    }
    printVersions(observed, System.out);
  }

  public static void printVersions(Hashtable observed,
          java.io.PrintStream outs)
  {
    if (observed.size() > 0)
    {
      int space = 0;
      String key = null;
      for (Enumeration keys = observed.keys(); keys.hasMoreElements();)
      {
        key = (String) keys.nextElement();
        if (space++ > 0)
        {
          outs.print(" ");
        }
        outs.print(key);
      }
      outs.print("\n");
    }
  }

  private static void checkClassVersion(String filename, Hashtable observed)
          throws IOException
  {
    String version = checkClassVersion(filename);
    if (version == null)
    {
      // System.err.println("Reading "+filename+" as  jar:");
      try
      {
        JarInputStream jis = new JarInputStream(new FileInputStream(
                filename));
        JarEntry entry;
        Hashtable perjar = new Hashtable();
        while ((entry = jis.getNextJarEntry()) != null)
        {
          if (entry != null)
          {
            if (entry.getName().endsWith(".class"))
            {
              try
              {
                version = getVersion(new DataInputStream(jis));
                if (version != null)
                {
                  addVersion(version, observed);
                  addVersion(version, perjar);
                }
              } catch (Exception e)
              {

              }
            }
          }
        }
        System.err.println("Jar : " + filename);
        printVersions(perjar, System.err);
      } catch (Exception e)
      {

      }
    }
    else
    {
      addVersion(version, observed);
    }
  }

  private static void addVersion(String version, Hashtable observed)
  {
    if (version != null)
    {
      // System.err.println("Version is '"+version+"'");
      int[] vrs = (int[]) observed.get(version);
      if (vrs == null)
      {
        vrs = new int[] { 0 };
      }
      vrs[0]++;
      observed.put(version, vrs);
    }
  }

  private static String checkClassVersion(String filename)
          throws IOException
  {
    DataInputStream in = new DataInputStream(new FileInputStream(filename));
    return getVersion(in);
  }

  private static Hashtable versions = null;

  private static String parseVersions(int minor, int major)
  {
    if (versions == null)
    {
      versions = new Hashtable();
      versions.put("45.3", "1.0.2");
      versions.put("45.3", "1.1");
      versions.put("46.0", "1.2");
      versions.put("47.0", "1.3");
      versions.put("48.0", "1.4");
      versions.put("49.0", "1.5");
      versions.put("50.0", "1.6");
      versions.put("51.0", "1.7");
      versions.put("52.0", "1.8");
      versions.put("53.0", "9");
      versions.put("54.0", "10");
      versions.put("55.0", "11");

    }
    String version = (String) versions.get(major + "." + minor);
    if (version == null)
    {
      // get nearest known version
      version = (String) versions.get(major + ".0");
    }
    // System.err.println("Version "+version);
    if (version == null)
    {
      versions.put(major + "." + minor, "Class v" + major + ".0");
    }
    return version;
  }

  private static String getVersion(DataInputStream in) throws IOException
  {
    int magic = in.readInt();
    if (magic != 0xcafebabe)
    {
      return null;
    }
    int minor = in.readUnsignedShort();
    int major = in.readUnsignedShort();
    // System.err.println("Version "+major+"."+minor);
    return parseVersions(minor, major);
  }

}
