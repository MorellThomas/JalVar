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
package jalview.schemes;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jalview.api.AlignViewportI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;

public class ColourSchemes
{
  /*
   * singleton instance of this class
   */
  private static ColourSchemes instance = new ColourSchemes();

  /*
   * a map from scheme name (lower-cased) to an instance of it
   */
  private Map<String, ColourSchemeI> schemes;

  /**
   * Returns the singleton instance of this class
   * 
   * @return
   */
  public static ColourSchemes getInstance()
  {
    return instance;
  }

  private ColourSchemes()
  {
    loadColourSchemes();
  }

  /**
   * Loads an instance of each standard or user-defined colour scheme
   * 
   * @return
   */
  void loadColourSchemes()
  {
    /*
     * store in an order-preserving map, so items can be added to menus 
     * in the order in which they are 'discovered'
     */
    schemes = new LinkedHashMap<>();

    for (JalviewColourScheme cs : JalviewColourScheme.values())
    {
      try
      {
        registerColourScheme(
                cs.getSchemeClass().getDeclaredConstructor().newInstance());
      } catch (InstantiationException | IllegalAccessException e)
      {
        System.err.println("Error instantiating colour scheme for "
                + cs.toString() + " " + e.getMessage());
        e.printStackTrace();
      } catch (ReflectiveOperationException roe)
      {
        roe.printStackTrace();
      }
    }
  }

  /**
   * Registers a colour scheme
   * 
   * @param cs
   */
  public void registerColourScheme(ColourSchemeI cs)
  {
    String name = cs.getSchemeName();
    if (name == null)
    {
      System.err.println("ColourScheme name may not be null");
      return;
    }

    /*
     * name is lower-case for non-case-sensitive lookup
     * (name in the colour keeps its true case)
     */
    String lower = getColourSchemeShortName(cs);
    if (schemes.containsKey(lower))
    {
      System.err
              .println("Warning: overwriting colour scheme named " + name);
    }
    schemes.put(lower, cs);
  }

  private String getColourSchemeShortName(ColourSchemeI cs)
  {
    return getColourSchemeShortName(cs.getSchemeName());
  }

  private String getColourSchemeShortName(String name)
  {
    if (name == null)
      return null;
    return name.toLowerCase(Locale.ROOT).replaceAll("%", "pc")
            .replaceAll("[^a-z0-9]", "-").replaceAll("--+", "-");
  }

  /**
   * Removes a colour scheme by name
   * 
   * @param name
   */
  public void removeColourScheme(String name)
  {
    if (name != null)
    {
      schemes.remove(getColourSchemeShortName(name));
    }
  }

  /**
   * Returns an instance of the colour scheme with which the given view may be
   * coloured
   * 
   * @param name
   *          name of the colour scheme
   * @param viewport
   * @param forData
   *          the data to be coloured
   * @param optional
   *          map from hidden representative sequences to the sequences they
   *          represent
   * @return
   */
  public ColourSchemeI getColourScheme(String name, AlignViewportI viewport,
          AnnotatedCollectionI forData,
          Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
  {
    if (name == null)
    {
      return null;
    }
    ColourSchemeI cs = schemes.get(getColourSchemeShortName(name));
    return cs == null ? null : cs.getInstance(viewport, forData);
  }

  /**
   * Returns an instance of the colour scheme with which the given view may be
   * coloured
   * 
   * @param name
   *          name of the colour scheme
   * @param forData
   *          the data to be coloured
   * @return
   */
  public ColourSchemeI getColourScheme(String name,
          AnnotatedCollectionI forData)
  {
    return getColourScheme(name, null, forData, null);
  }

  /**
   * Returns an iterable set of the colour schemes, in the order in which they
   * were added
   * 
   * @return
   */
  public Iterable<ColourSchemeI> getColourSchemes()
  {
    return schemes.values();
  }

  /**
   * Answers true if there is a scheme with the given name, else false. The test
   * is not case-sensitive.
   * 
   * @param name
   * @return
   */
  public boolean nameExists(String name)
  {
    if (name == null)
    {
      return false;
    }
    return schemes.containsKey(getColourSchemeShortName(name));
  }
}
