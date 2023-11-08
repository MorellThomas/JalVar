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
package jalview.structure;

import java.util.Locale;

import jalview.datamodel.PDBEntry;
import jalview.datamodel.PDBEntry.Type;
import jalview.util.MessageManager;

/**
 * bean holding settings for structure IO. TODO: tests for validation of values
 * TODO: tests for race conditions (all fields are static, is that correct ?)
 * 
 * @author tcofoegbu
 *
 */
public class StructureImportSettings
{
  /**
   * set to true to add derived sequence annotations (temp factor read from
   * file, or computed secondary structure) to the alignment
   */
  private static boolean visibleChainAnnotation = false;

  /**
   * Set true to predict secondary structure (using JMol for protein, Annotate3D
   * for RNA)
   */
  private static boolean processSecStr = false;

  /**
   * Set true (with predictSecondaryStructure=true) to predict secondary
   * structure using an external service (currently Annotate3D for RNA only)
   */
  private static boolean externalSecondaryStructure = false;

  private static boolean showSeqFeatures = true;

  public enum StructureParser
  {
    JMOL_PARSER, JALVIEW_PARSER
  }

  public static enum TFType
  {
    DEFAULT, PLDDT;

    @Override
    public String toString()
    {
      return MessageManager.getString(
              "label.tftype_" + this.name().toLowerCase(Locale.ROOT));
    }
  }

  /**
   * Determines the default file format for structure files to be downloaded
   * from the PDB sequence fetcher. Possible options include: PDB|mmCIF
   */
  private static PDBEntry.Type defaultStructureFileFormat = Type.PDB;

  /**
   * Determines the parser used for parsing PDB format file. Possible options
   * are : JMolParser|JalveiwParser
   */
  private static StructureParser defaultPDBFileParser = StructureParser.JMOL_PARSER;

  private static TFType temperatureFactorType;

  public static void addSettings(boolean addAlignmentAnnotations,
          boolean processSecStr, boolean externalSecStr)
  {
    StructureImportSettings.visibleChainAnnotation = addAlignmentAnnotations;
    StructureImportSettings.processSecStr = processSecStr;
    StructureImportSettings.externalSecondaryStructure = externalSecStr;
    StructureImportSettings.showSeqFeatures = true;
  }

  public static boolean isVisibleChainAnnotation()
  {
    return visibleChainAnnotation;
  }

  public static void setTemperatureFactorType(TFType t)
  {
    StructureImportSettings.temperatureFactorType = t;
  }

  public static void setVisibleChainAnnotation(
          boolean visibleChainAnnotation)
  {
    StructureImportSettings.visibleChainAnnotation = visibleChainAnnotation;
  }

  public static boolean isProcessSecondaryStructure()
  {
    return processSecStr;
  }

  public static void setProcessSecondaryStructure(
          boolean processSecondaryStructure)
  {
    StructureImportSettings.processSecStr = processSecondaryStructure;
  }

  public static boolean isExternalSecondaryStructure()
  {
    return externalSecondaryStructure;
  }

  public static void setExternalSecondaryStructure(
          boolean externalSecondaryStructure)
  {
    StructureImportSettings.externalSecondaryStructure = externalSecondaryStructure;
  }

  public static boolean isShowSeqFeatures()
  {
    return showSeqFeatures;
  }

  public static void setShowSeqFeatures(boolean showSeqFeatures)
  {
    StructureImportSettings.showSeqFeatures = showSeqFeatures;
  }

  public static PDBEntry.Type getDefaultStructureFileFormat()
  {
    return defaultStructureFileFormat;
  }

  public static void setDefaultStructureFileFormat(
          String defaultStructureFileFormat)
  {
    StructureImportSettings.defaultStructureFileFormat = PDBEntry.Type
            .valueOf(defaultStructureFileFormat.toUpperCase(Locale.ROOT));
  }

  public static String getDefaultPDBFileParser()
  {
    return defaultPDBFileParser.toString();
  }

  public static void setDefaultPDBFileParser(
          StructureParser defaultPDBFileParser)
  {
    StructureImportSettings.defaultPDBFileParser = defaultPDBFileParser;
  }

  public static void setDefaultPDBFileParser(String defaultPDBFileParser)
  {
    StructureImportSettings.defaultPDBFileParser = StructureParser
            .valueOf(defaultPDBFileParser.toUpperCase(Locale.ROOT));
  }

  public static TFType getTemperatureFactorType()
  {
    return temperatureFactorType;
  }

}
