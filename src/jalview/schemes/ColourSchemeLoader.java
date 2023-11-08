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

import java.util.Locale;

import jalview.xml.binding.jalview.JalviewUserColours;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class ColourSchemeLoader
{

  /**
   * Loads a user defined colour scheme from file. The file should contain a
   * definition of residue colours in XML format as defined in
   * JalviewUserColours.xsd.
   * 
   * @param filePath
   * 
   * @return
   */
  public static UserColourScheme loadColourScheme(String filePath)
  {
    UserColourScheme ucs = null;
    Color[] newColours = null;
    File file = new File(filePath);
    try
    {
      InputStreamReader in = new InputStreamReader(
              new FileInputStream(file), "UTF-8");

      JAXBContext jc = JAXBContext
              .newInstance("jalview.xml.binding.jalview");
      javax.xml.bind.Unmarshaller um = jc.createUnmarshaller();
      XMLStreamReader streamReader = XMLInputFactory.newInstance()
              .createXMLStreamReader(in);
      JAXBElement<JalviewUserColours> jbe = um.unmarshal(streamReader,
              JalviewUserColours.class);
      JalviewUserColours jucs = jbe.getValue();

      /*
       * non-case-sensitive colours are for 20 amino acid codes,
       * B, Z, X and Gap
       * optionally, lower-case alternatives for all except Gap
       */
      newColours = new Color[24];
      Color[] lowerCase = new Color[23];
      boolean caseSensitive = false;

      String name;
      int index;
      for (int i = 0; i < jucs.getColour().size(); i++)
      {
        name = jucs.getColour().get(i).getName();
        if (ResidueProperties.aa3Hash.containsKey(name))
        {
          index = ResidueProperties.aa3Hash.get(name).intValue();
        }
        else
        {
          index = ResidueProperties.aaIndex[name.charAt(0)];
        }
        if (index == -1)
        {
          continue;
        }

        Color color = new Color(
                Integer.parseInt(jucs.getColour().get(i).getRGB(), 16));
        if (name.toLowerCase(Locale.ROOT).equals(name))
        {
          caseSensitive = true;
          lowerCase[index] = color;
        }
        else
        {
          newColours[index] = color;
        }
      }

      /*
       * instantiate the colour scheme
       */
      ucs = new UserColourScheme(newColours);
      ucs.setName(jucs.getSchemeName());
      if (caseSensitive)
      {
        ucs.setLowerCaseColours(lowerCase);
      }
    } catch (Exception ex)
    {
      // used to try to parse a V1 Castor generated colours file
      System.err.println("Failed to read colour scheme from " + filePath
              + " : " + ex.toString());
    }

    return ucs;
  }

}
