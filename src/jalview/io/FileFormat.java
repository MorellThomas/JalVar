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

import java.io.IOException;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.ext.jmol.JmolParser;
import jalview.structure.StructureImportSettings;

public enum FileFormat implements FileFormatI
{
  Fasta("Fasta", "fa,fasta,mfa,fastq", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new FastaFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new FastaFile();
    }
  },
  Pfam("PFAM", "pfam", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new PfamFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new PfamFile();
    }
  },
  Stockholm("Stockholm", "sto,stk", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new StockholmFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new StockholmFile(al);
    }

  },

  PIR("PIR", "pir", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new PIRFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new PIRFile();
    }
  },
  BLC("BLC", "blc", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new BLCFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new BLCFile();
    }
  },
  AMSA("AMSA", "amsa", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new AMSAFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new AMSAFile(al);
    }
  },
  Html("HTML", "html", true, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new HtmlFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new HtmlFile();
    }

    @Override
    public boolean isComplexAlignFile()
    {
      return true;
    }

  },
  Rnaml("RNAML", "xml,rnaml", true, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new RnamlFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new RnamlFile();
    }

  },
  Json("JSON", "json", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new JSONFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new JSONFile();
    }

    @Override
    public boolean isComplexAlignFile()
    {
      return true;
    }

  },
  Pileup("PileUp", "pileup", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new PileUpfile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new PileUpfile();
    }

  },
  MSF("MSF", "msf", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new MSFfile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new MSFfile();
    }

  },
  Clustal("Clustal", "aln", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new ClustalFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new ClustalFile();
    }
  },
  Phylip("PHYLIP", "phy", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new PhylipFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new PhylipFile();
    }
  },
  GenBank("GenBank Flatfile", "gb,gbk", true, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new GenBankFile(source, "GenBank");
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return null;
    }
  },
  Embl("ENA Flatfile", "txt", true, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      // Always assume we import from EMBL for now
      return new EmblFlatFile(source, DBRefSource.EMBL);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return null;
    }
  },
  Jnet("JnetFile", "", false, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      JPredFile af = new JPredFile(source);
      af.removeNonSequences();
      return af;
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return null; // todo is this called?
    }

  },
  Features("GFF or Jalview features", "gff2,gff3", true, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new FeaturesFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new FeaturesFile();
    }
  },
  ScoreMatrix("Substitution matrix", "", false, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new ScoreMatrixFile(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return null;
    }
  },
  PDB("PDB", "pdb,ent", true, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      boolean isParseWithJMOL = StructureImportSettings
              .getDefaultStructureFileFormat() != PDBEntry.Type.PDB;
      if (isParseWithJMOL)
      {
        return new JmolParser(source);
      }
      else
      {
        StructureImportSettings.setShowSeqFeatures(true);
        return new mc_view.PDBfile(
                StructureImportSettings.isVisibleChainAnnotation(),
                StructureImportSettings.isProcessSecondaryStructure(),
                StructureImportSettings.isExternalSecondaryStructure(),
                source);
      }
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new JmolParser(); // todo or null?
    }

    @Override
    public boolean isStructureFile()
    {
      return true;
    }
  },
  MMCif("mmCIF", "cif", true, false)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return new JmolParser(source);
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return new JmolParser(); // todo or null?
    }

    @Override
    public boolean isStructureFile()
    {
      return true;
    }
  },
  Jalview("Jalview", "jvp,jar", true, true)
  {
    @Override
    public AlignmentFileReaderI getReader(FileParse source)
            throws IOException
    {
      return null;
    }

    @Override
    public AlignmentFileWriterI getWriter(AlignmentI al)
    {
      return null;
    }

    @Override
    public boolean isTextFormat()
    {
      return false;
    }

    @Override
    public boolean isIdentifiable()
    {
      return true;
    }
  };

  private boolean writable;

  private boolean readable;

  private String extensions;

  private String name;

  @Override
  public boolean isComplexAlignFile()
  {
    return false;
  }

  @Override
  public boolean isReadable()
  {
    return readable;
  }

  @Override
  public boolean isWritable()
  {
    return writable;
  }

  /**
   * Constructor
   * 
   * @param shortName
   * @param extensions
   *          comma-separated list of file extensions associated with the format
   * @param isReadable
   *          - can be recognised by IdentifyFile and imported with the given
   *          reader
   * @param isWritable
   *          - can be exported with the returned writer
   */
  private FileFormat(String shortName, String extensions,
          boolean isReadable, boolean isWritable)
  {
    this.name = shortName;
    this.extensions = extensions;
    this.readable = isReadable;
    this.writable = isWritable;
  }

  @Override
  public String getExtensions()
  {
    return extensions;
  }

  /**
   * Answers the display name of the file format (as for example shown in menu
   * options). This name should not be locale (language) dependent.
   */
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public boolean isTextFormat()
  {
    return true;
  }

  @Override
  public boolean isStructureFile()
  {
    return false;
  }

  /**
   * By default, answers true, indicating the format is one that can be
   * identified by IdentifyFile. Formats that cannot be identified should
   * override this method to return false.
   */
  public boolean isIdentifiable()
  {
    return true;
  }
}
