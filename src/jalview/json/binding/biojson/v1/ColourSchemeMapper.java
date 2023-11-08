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

import java.util.Locale;

import jalview.datamodel.AnnotatedCollectionI;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ClustalxColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.PurinePyrimidineColourScheme;
import jalview.schemes.RNAHelicesColour;
import jalview.schemes.RNAInteractionColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TCoffeeColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.ZappoColourScheme;

public class ColourSchemeMapper
{
  private static ColourSchemeI csZappo, csTaylor, csNucleotide, csPurine,
          csHelix, csTurn, csStrand, csBuried, csHydro,
          csRNAInteractionType, csPID, csBlosum62 = null;
  static
  {
    csZappo = new ZappoColourScheme();
    csTaylor = new TaylorColourScheme();
    csNucleotide = new NucleotideColourScheme();
    csPurine = new PurinePyrimidineColourScheme();
    csHelix = new HelixColourScheme();
    csTurn = new TurnColourScheme();
    csStrand = new StrandColourScheme();
    csBuried = new BuriedColourScheme();
    csHydro = new HydrophobicColourScheme();
    csRNAInteractionType = new RNAInteractionColourScheme();
    csPID = new PIDColourScheme();
    csBlosum62 = new Blosum62ColourScheme();
  }

  public static ColourSchemeI getJalviewColourScheme(
          String colourSchemeName, AnnotatedCollectionI annotCol)
  {
    switch (colourSchemeName.toUpperCase(Locale.ROOT))
    {
    case "ZAPPO":
      return csZappo;
    case "TAYLOR":
      return csTaylor;
    case "NUCLEOTIDE":
      return csNucleotide;
    case "PURINE":
    case "PURINE/PYRIMIDINE":
      return csPurine;
    case "HELIX":
    case "HELIX PROPENSITY":
      return csHelix;
    case "TURN":
    case "TURN PROPENSITY":
      return csTurn;
    case "STRAND":
    case "STRAND PROPENSITY":
      return csStrand;
    case "BURIED":
    case "BURIED INDEX":
      return csBuried;
    case "HYDRO":
    case "HYDROPHOBIC":
      return csHydro;
    case "RNA INTERACTION TYPE":
      return csRNAInteractionType;
    case "PID":
    case "% IDENTITY":
      return csPID;
    case "BLOSUM62":
      return csBlosum62;
    case "T-COFFEE SCORES":
      return (annotCol != null) ? new TCoffeeColourScheme(annotCol) : null;
    case "RNA HELICES":
      return (annotCol != null) ? new RNAHelicesColour(annotCol) : null;
    case "CLUSTAL":
      return (annotCol != null) ? new ClustalxColourScheme(annotCol, null)
              : null;
    case "USER DEFINED":
      return null;
    default:
      return null;
    }
  }
}
