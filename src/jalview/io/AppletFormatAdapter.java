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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import jalview.api.AlignExportSettingsI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.PDBEntry.Type;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JmolParser;
import jalview.structure.StructureImportSettings;
import jalview.util.Platform;

/**
 * A low level class for alignment and feature IO with alignment formatting
 * methods used by both applet and application for generating flat alignment
 * files. It also holds the lists of magic format names that the applet and
 * application will allow the user to read or write files with.
 *
 * @author $author$
 * @version $Revision$
 */
public class AppletFormatAdapter
{
  private AlignmentViewPanel viewpanel;

  /**
   * add jalview-derived non-secondary structure annotation from PDB structure
   */
  boolean annotFromStructure = false;

  /**
   * add secondary structure from PDB data with built-in algorithms
   */
  boolean localSecondaryStruct = false;

  /**
   * process PDB data with web services
   */
  boolean serviceSecondaryStruct = false;

  private AlignmentFileReaderI alignFile = null;

  String inFile;

  /**
   * character used to write newlines
   */
  protected String newline = System.getProperty("line.separator");

  private AlignExportSettingsI exportSettings;

  private File selectedFile;

  public static String INVALID_CHARACTERS = "Contains invalid characters";

  /**
   * Returns an error message with a list of supported readable file formats
   * 
   * @return
   */
  public static String getSupportedFormats()
  {
    return "Formats currently supported are\n"
            + prettyPrint(FileFormats.getInstance().getReadableFormats());
  }

  public AppletFormatAdapter()
  {
  }

  public AppletFormatAdapter(AlignmentViewPanel viewpanel)
  {
    this.viewpanel = viewpanel;
  }

  public AppletFormatAdapter(AlignmentViewPanel alignPanel,
          AlignExportSettingsI settings)
  {
    viewpanel = alignPanel;
    exportSettings = settings;
  }

  /**
   * Formats a grammatically correct(ish) list consisting of the given objects
   * 
   * @param things
   * @return
   */
  public static String prettyPrint(List<? extends Object> things)
  {
    StringBuffer list = new StringBuffer();
    for (int i = 0, iSize = things.size() - 1; i < iSize; i++)
    {
      list.append(things.get(i).toString());
      list.append(", ");
    }
    // could i18n 'and' here
    list.append(" and " + things.get(things.size() - 1).toString() + ".");
    return list.toString();
  }

  public void setNewlineString(String nl)
  {
    newline = nl;
  }

  public String getNewlineString()
  {
    return newline;
  }

  /**
   * Constructs the correct filetype parser for a characterised datasource
   *
   * @param inFile
   *          data/data location
   * @param sourceType
   *          type of datasource
   * @param fileFormat
   *
   * @return
   */
  public AlignmentI readFile(String file, DataSourceType sourceType,
          FileFormatI fileFormat) throws IOException
  {
    return readFile(null, file, sourceType, fileFormat);
  }

  public AlignmentI readFile(File selectedFile, String file,
          DataSourceType sourceType, FileFormatI fileFormat)
          throws IOException
  {
    return readFile(selectedFile, file, sourceType, fileFormat, null);
  }

  public AlignmentI readFile(File selectedFile, String file,
          DataSourceType sourceType, FileFormatI fileFormat,
          StructureImportSettings.TFType tempfacType) throws IOException
  {

    this.selectedFile = selectedFile;
    this.inFile = selectedFile != null ? selectedFile.getPath() : file;
    try
    {
      if (fileFormat.isStructureFile())
      {
        String structureParser = StructureImportSettings
                .getDefaultPDBFileParser();
        boolean isParseWithJMOL = structureParser.equalsIgnoreCase(
                StructureImportSettings.StructureParser.JMOL_PARSER
                        .toString());
        StructureImportSettings.addSettings(annotFromStructure,
                localSecondaryStruct, serviceSecondaryStruct);
        if (tempfacType != null)
        {
          StructureImportSettings.setTemperatureFactorType(tempfacType);
        }
        if (isParseWithJMOL)
        {
          // needs a File option
          alignFile = new JmolParser(
                  selectedFile == null ? inFile : selectedFile, sourceType,
                  StructureImportSettings.getTemperatureFactorType());
        }
        else
        {
          // todo is mc_view parsing obsolete yet? JAL-2120
          StructureImportSettings.setShowSeqFeatures(true);
          alignFile = new mc_view.PDBfile(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct, inFile,
                  sourceType);
        }
        ((StructureFile) alignFile).setDbRefType(
                FileFormat.PDB.equals(fileFormat) ? Type.PDB : Type.MMCIF);
      }
      else if (selectedFile != null)
      {
        alignFile = fileFormat
                .getReader(new FileParse(selectedFile, sourceType));
      }
      else
      {
        // alignFile = fileFormat.getAlignmentFile(inFile, sourceType);
        alignFile = fileFormat.getReader(new FileParse(inFile, sourceType));
      }
      return buildAlignmentFromFile();
    } catch (Exception e)
    {
      e.printStackTrace();
      System.err.println("Failed to read alignment using the '" + fileFormat
              + "' reader.\n" + e);

      if (e.getMessage() != null
              && e.getMessage().startsWith(INVALID_CHARACTERS))
      {
        throw new IOException(e.getMessage());
      }

      // Finally test if the user has pasted just the sequence, no id
      if (sourceType == DataSourceType.PASTE)
      {
        try
        {
          // Possible sequence is just residues with no label
          alignFile = new FastaFile(">UNKNOWN\n" + inFile,
                  DataSourceType.PASTE);
          return buildAlignmentFromFile();

        } catch (Exception ex)
        {
          if (ex.toString().startsWith(INVALID_CHARACTERS))
          {
            throw new IOException(e.getMessage());
          }

          ex.printStackTrace();
        }
      }
      if (FileFormat.Html.equals(fileFormat))
      {
        throw new IOException(e.getMessage());
      }
    }
    throw new FileFormatException(getSupportedFormats());
  }

  /**
   * Constructs the correct filetype parser for an already open datasource
   *
   * @param source
   *          an existing datasource
   * @param format
   *          File format of data that will be provided by datasource
   *
   * @return
   */
  public AlignmentI readFromFile(FileParse source, FileFormatI format)
          throws IOException
  {
    this.inFile = source.getInFile();
    DataSourceType type = source.dataSourceType;
    try
    {
      if (FileFormat.PDB.equals(format) || FileFormat.MMCif.equals(format))
      {
        // TODO obtain config value from preference settings
        boolean isParseWithJMOL = false;
        if (isParseWithJMOL)
        {
          StructureImportSettings.addSettings(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct);
          alignFile = new JmolParser(source);
        }
        else
        {
          StructureImportSettings.setShowSeqFeatures(true);
          alignFile = new mc_view.PDBfile(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct, source);
        }
        ((StructureFile) alignFile).setDbRefType(Type.PDB);
      }
      else
      {
        alignFile = format.getReader(source);
      }

      return buildAlignmentFromFile();

    } catch (Exception e)
    {
      e.printStackTrace();
      System.err.println("Failed to read alignment using the '" + format
              + "' reader.\n" + e);

      if (e.getMessage() != null
              && e.getMessage().startsWith(INVALID_CHARACTERS))
      {
        throw new FileFormatException(e.getMessage());
      }

      // Finally test if the user has pasted just the sequence, no id
      if (type == DataSourceType.PASTE)
      {
        try
        {
          // Possible sequence is just residues with no label
          alignFile = new FastaFile(">UNKNOWN\n" + inFile,
                  DataSourceType.PASTE);
          return buildAlignmentFromFile();

        } catch (Exception ex)
        {
          if (ex.toString().startsWith(INVALID_CHARACTERS))
          {
            throw new IOException(e.getMessage());
          }

          ex.printStackTrace();
        }
      }

      // If we get to this stage, the format was not supported
      throw new FileFormatException(getSupportedFormats());
    }
  }

  /**
   * boilerplate method to handle data from an AlignFile and construct a new
   * alignment or import to an existing alignment
   * 
   * @return AlignmentI instance ready to pass to a UI constructor
   */
  private AlignmentI buildAlignmentFromFile()
  {
    // Standard boilerplate for creating alignment from parser
    // alignFile.configureForView(viewpanel);

    AlignmentI al = new Alignment(alignFile.getSeqsAsArray());

    alignFile.addAnnotations(al);

    alignFile.addGroups(al);

    return al;
  }

  /**
   * create an alignment flatfile from a Jalview alignment view
   * 
   * @param format
   * @param jvsuffix
   * @param av
   * @param selectedOnly
   * @return flatfile in a string
   */
  public String formatSequences(FileFormatI format, boolean jvsuffix,
          AlignmentViewPanel ap, boolean selectedOnly)
  {

    AlignmentView selvew = ap.getAlignViewport()
            .getAlignmentView(selectedOnly, false);
    AlignmentI aselview = selvew
            .getVisibleAlignment(ap.getAlignViewport().getGapCharacter());
    List<AlignmentAnnotation> ala = (ap.getAlignViewport()
            .getVisibleAlignmentAnnotation(selectedOnly));
    if (ala != null)
    {
      for (AlignmentAnnotation aa : ala)
      {
        aselview.addAnnotation(aa);
      }
    }
    viewpanel = ap;
    return formatSequences(format, aselview, jvsuffix);
  }

  /**
   * Construct an output class for an alignment in a particular filetype TODO:
   * allow caller to detect errors and warnings encountered when generating
   * output
   *
   * @param format
   *          string name of alignment format
   * @param alignment
   *          the alignment to be written out
   * @param jvsuffix
   *          passed to AlnFile class controls whether /START-END is added to
   *          sequence names
   *
   * @return alignment flat file contents
   */
  public String formatSequences(FileFormatI format, AlignmentI alignment,
          boolean jvsuffix)
  {
    try
    {
      AlignmentFileWriterI afile = format.getWriter(alignment);

      afile.setNewlineString(newline);
      afile.setExportSettings(exportSettings);
      afile.configureForView(viewpanel);

      // check whether we were given a specific alignment to export, rather than
      // the one in the viewpanel
      SequenceI[] seqs = null;
      if (viewpanel == null || viewpanel.getAlignment() == null
              || viewpanel.getAlignment() != alignment)
      {
        seqs = alignment.getSequencesArray();
      }
      else
      {
        seqs = viewpanel.getAlignment().getSequencesArray();
      }

      String afileresp = afile.print(seqs, jvsuffix);
      if (afile.hasWarningMessage())
      {
        System.err.println("Warning raised when writing as " + format
                + " : " + afile.getWarningMessage());
      }
      return afileresp;
    } catch (Exception e)
    {
      System.err.println("Failed to write alignment as a '"
              + format.getName() + "' file\n");
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Determines the protocol (i.e DataSourceType.{FILE|PASTE|URL}) for the input
   * data
   * 
   * BH 2018 allows File or String, and can return RELATIVE_URL
   *
   * @param dataObject
   *          File or String
   * @return the protocol for the input data
   */
  public static DataSourceType checkProtocol(Object dataObject)
  {
    if (dataObject instanceof File)
    {
      return DataSourceType.FILE;
    }

    String data = dataObject.toString();
    DataSourceType protocol = DataSourceType.PASTE;
    String ft = data.toLowerCase(Locale.ROOT).trim();
    if (ft.indexOf("http:") == 0 || ft.indexOf("https:") == 0
            || ft.indexOf("file:") == 0)
    {
      protocol = DataSourceType.URL;
    }
    else if (Platform.isJS())
    {
      protocol = DataSourceType.RELATIVE_URL;
    }
    else if (new File(data).exists())
    {
      protocol = DataSourceType.FILE;
    }
    return protocol;
  }

  /**
   * @param args
   * @j2sIgnore
   */
  public static void main(String[] args)
  {
    int i = 0;
    while (i < args.length)
    {
      File f = new File(args[i]);
      if (f.exists())
      {
        try
        {
          System.out.println("Reading file: " + f);
          AppletFormatAdapter afa = new AppletFormatAdapter();
          Runtime r = Runtime.getRuntime();
          System.gc();
          long memf = -r.totalMemory() + r.freeMemory();
          long t1 = -System.currentTimeMillis();
          AlignmentI al = afa.readFile(args[i], DataSourceType.FILE,
                  new IdentifyFile().identify(args[i],
                          DataSourceType.FILE));
          t1 += System.currentTimeMillis();
          System.gc();
          memf += r.totalMemory() - r.freeMemory();
          if (al != null)
          {
            System.out.println("Alignment contains " + al.getHeight()
                    + " sequences and " + al.getWidth() + " columns.");
            try
            {
              System.out.println(new AppletFormatAdapter()
                      .formatSequences(FileFormat.Fasta, al, true));
            } catch (Exception e)
            {
              System.err.println(
                      "Couln't format the alignment for output as a FASTA file.");
              e.printStackTrace(System.err);
            }
          }
          else
          {
            System.out.println("Couldn't read alignment");
          }
          System.out.println("Read took " + (t1 / 1000.0) + " seconds.");
          System.out.println(
                  "Difference between free memory now and before is "
                          + (memf / (1024.0 * 1024.0) * 1.0) + " MB");
        } catch (Exception e)
        {
          System.err.println("Exception when dealing with " + i
                  + "'th argument: " + args[i] + "\n" + e);
        }
      }
      else
      {
        System.err.println("Ignoring argument '" + args[i] + "' (" + i
                + "'th)- not a readable file.");
      }
      i++;
    }
  }

  /**
   * try to discover how to access the given file as a valid datasource that
   * will be identified as the given type.
   *
   * @param file
   * @param format
   * @return protocol that yields the data parsable as the given type
   */
  public static DataSourceType resolveProtocol(String file,
          FileFormatI format)
  {
    return resolveProtocol(file, format, false);
  }

  public static DataSourceType resolveProtocol(String file,
          FileFormatI format, boolean debug)
  {
    // TODO: test thoroughly!
    DataSourceType protocol = null;
    if (debug)
    {
      System.out.println("resolving datasource started with:\n>>file\n"
              + file + ">>endfile");
    }

    // This might throw a security exception in certain browsers
    // Netscape Communicator for instance.
    try
    {
      boolean rtn = false;
      InputStream is = System.getSecurityManager().getClass()
              .getResourceAsStream("/" + file);
      if (is != null)
      {
        rtn = true;
        is.close();
      }
      if (debug)
      {
        System.err.println("Resource '" + file + "' was "
                + (rtn ? "" : "not") + " located by classloader.");
      }
      if (rtn)
      {
        protocol = DataSourceType.CLASSLOADER;
      }

    } catch (Exception ex)
    {
      System.err
              .println("Exception checking resources: " + file + " " + ex);
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
    FileParse fp = null;
    try
    {
      if (debug)
      {
        System.out.println(
                "Trying to get contents of resource as " + protocol + ":");
      }
      fp = new FileParse(file, protocol);
      if (!fp.isValid())
      {
        fp = null;
      }
      else
      {
        if (debug)
        {
          System.out.println("Successful.");
        }
      }
    } catch (Exception e)
    {
      if (debug)
      {
        System.err.println("Exception when accessing content: " + e);
      }
      fp = null;
    }
    if (fp == null)
    {
      if (debug)
      {
        System.out.println("Accessing as paste.");
      }
      protocol = DataSourceType.PASTE;
      fp = null;
      try
      {
        fp = new FileParse(file, protocol);
        if (!fp.isValid())
        {
          fp = null;
        }
      } catch (Exception e)
      {
        System.err.println("Failed to access content as paste!");
        e.printStackTrace();
        fp = null;
      }
    }
    if (fp == null)
    {
      return null;
    }
    if (format == null)
    {
      return protocol;
    }
    else
    {
      try
      {
        FileFormatI idformat = new IdentifyFile().identify(file, protocol);
        if (idformat == null)
        {
          if (debug)
          {
            System.out.println("Format not identified. Inaccessible file.");
          }
          return null;
        }
        if (debug)
        {
          System.out.println("Format identified as " + idformat
                  + "and expected as " + format);
        }
        if (idformat.equals(format))
        {
          if (debug)
          {
            System.out.println("Protocol identified as " + protocol);
          }
          return protocol;
        }
        else
        {
          if (debug)
          {
            System.out
                    .println("File deemed not accessible via " + protocol);
          }
          fp.close();
          return null;
        }
      } catch (Exception e)
      {
        if (debug)
        {
          System.err.println("File deemed not accessible via " + protocol);
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public AlignmentFileReaderI getAlignFile()
  {
    return alignFile;
  }
}
