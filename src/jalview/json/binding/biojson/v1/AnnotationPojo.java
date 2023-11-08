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
package jalview.json.binding.biojson.v1;

import com.github.reinert.jjschema.Attributes;

public class AnnotationPojo
{
  @Attributes(
    required = false,
    description = "Display character for the given annotation")
  private String displayCharacter;

  @Attributes(
    required = false,
    description = "Description for the annotation")
  private String description;

  @Attributes(
    required = true,
    enums =
    { "E", "H", "\u0000", ")", "(" },
    description = "Determines what is rendered for the secondary </br>structure <ul><li>’E’ - indicates Beta Sheet/Strand <li>’H’ - indicates alpha helix </li><li> ‘\\u0000’ - indicates blank</li></ul></br>For RNA Helix (only shown when working with</br> nucleotide sequences): <ul><li> ‘(’ - indicates bases pair with columns upstream</br> (to right) </li><li> ’(’ - indicate region pairs with bases to the left</li></ul>")
  private char secondaryStructure;

  @Attributes(required = false, description = "Value of the annotation")
  private float value;

  @Attributes(
    required = false,
    description = "Colour of the annotation position in hex string.")
  private String colour;

  public String getDisplayCharacter()
  {
    return displayCharacter;
  }

  public void setDisplayCharacter(String displayCharacter)
  {
    this.displayCharacter = displayCharacter;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public char getSecondaryStructure()
  {
    return secondaryStructure;
  }

  public void setSecondaryStructure(char secondaryStructure)
  {
    this.secondaryStructure = secondaryStructure;
  }

  public float getValue()
  {
    return value;
  }

  public void setValue(float value)
  {
    this.value = value;
  }

  public String getColour()
  {
    return colour;
  }

  public void setColour(String colour)
  {
    this.colour = colour;
  }

}
