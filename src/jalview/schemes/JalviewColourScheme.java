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

/**
 * An enum with the colour schemes supported by Jalview.
 */
public enum JalviewColourScheme
{
  /*
   * the order of declaration is the default order in which 
   * items are added to Colour menus
   */
  Clustal("Clustal", ClustalxColourScheme.class),
  Blosum62("Blosum62", Blosum62ColourScheme.class),
  PID("% Identity", PIDColourScheme.class),
  Zappo("Zappo", ZappoColourScheme.class),
  Taylor("Taylor", TaylorColourScheme.class),
  Flower("gecos-flower", FlowerColourScheme.class),
  Blossom("gecos-blossom", BlossomColourScheme.class),
  Sunset("gecos-sunset", SunsetColourScheme.class),
  Ocean("gecos-ocean", OceanColourScheme.class),
  Hydrophobic("Hydrophobic", HydrophobicColourScheme.class),
  Helix("Helix Propensity", HelixColourScheme.class),
  Strand("Strand Propensity", StrandColourScheme.class),
  Turn("Turn Propensity", TurnColourScheme.class),
  Buried("Buried Index", BuriedColourScheme.class),
  Nucleotide("Nucleotide", NucleotideColourScheme.class),
  NucleotideAmbiguity("Nucleotide Ambiguity",
          NucleotideAmbiguityColourScheme.class),
  PurinePyrimidine("Purine/Pyrimidine", PurinePyrimidineColourScheme.class),
  RNAHelices("RNA Helices", RNAHelicesColour.class),
  TCoffee("T-Coffee Scores", TCoffeeColourScheme.class),
  IdColour("Sequence ID", IdColourScheme.class),;
  // RNAInteraction("RNA Interaction type", RNAInteractionColourScheme.class)

  private String name;

  private Class<? extends ColourSchemeI> myClass;

  /**
   * Constructor given the name of the colour scheme (as used in Jalview
   * parameters). Note this is not necessarily the same as the 'display name'
   * used in menu options (as this may be language-dependent).
   * 
   * @param s
   */
  JalviewColourScheme(String s, Class<? extends ColourSchemeI> cl)
  {
    name = s;
    myClass = cl;
  }

  /**
   * Returns the class of the colour scheme
   * 
   * @return
   */
  public Class<? extends ColourSchemeI> getSchemeClass()
  {
    return myClass;
  }

  /**
   * Returns the 'official' name of this colour scheme. This is the name that
   * identifies the colour scheme as a start-up parameter for the Jalview
   * application or applet. Note that it may not be the name shown in menu
   * options, as these may be internationalised.
   */
  @Override
  public String toString()
  {
    return name;
  }
}
