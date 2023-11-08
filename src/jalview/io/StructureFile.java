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

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import jalview.analysis.AlignSeq;
import jalview.api.FeatureSettingsModelI;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.PDBEntry.Type;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JmolParser;
import jalview.structure.StructureImportSettings;
import jalview.structure.StructureImportSettings.TFType;
import mc_view.PDBChain;

public abstract class StructureFile extends AlignFile
{
  private String id;

  private PDBEntry.Type dbRefType;

  /**
   * set to true to add derived sequence annotations (temp factor read from
   * file, or computed secondary structure) to the alignment
   */
  protected boolean visibleChainAnnotation = false;

  /**
   * Set true to predict secondary structure (using JMol for protein, Annotate3D
   * for RNA)
   */
  protected boolean predictSecondaryStructure = false;

  /**
   * Set true (with predictSecondaryStructure=true) to predict secondary
   * structure using an external service (currently Annotate3D for RNA only)
   */
  protected boolean externalSecondaryStructure = false;

  private Vector<PDBChain> chains;

  private boolean pdbIdAvailable;

  private TFType temperatureFactorType = TFType.DEFAULT;

  private String paeMatrix = null;

  private boolean alphaFoldModel;

  public void setPAEMatrix(String paeFilename)
  {
    paeMatrix = paeFilename;
  }

  public String getPAEMatrix()
  {
    return paeMatrix;
  }

  public boolean hasPAEMatrix()
  {
    return paeMatrix != null;
  }

  public void setTemperatureFactorType(TFType t)
  {
    this.temperatureFactorType = t;
  }

  public TFType getTemperatureFactorType()
  {
    return temperatureFactorType;
  }

  public void setAlphafoldModel(boolean afm)
  {
    alphaFoldModel = afm;
  }

  public boolean isAlphafoldModel()
  {
    return alphaFoldModel;
  }

  public StructureFile(Object inFile, DataSourceType sourceType)
          throws IOException
  {
    this(inFile, sourceType, null);
  }

  public StructureFile(Object inFile, DataSourceType sourceType,
          TFType tempfacType) throws IOException
  {
    super(false, inFile, sourceType);
    this.setTemperatureFactorType(tempfacType);
    doParse();
  }

  public StructureFile(FileParse fp) throws IOException
  {
    this(fp, true);
  }

  public StructureFile(FileParse fp, boolean doXferSettings)
          throws IOException
  {
    super(fp, doXferSettings);
  }

  public void addSettings(boolean addAlignmentAnnotations,
          boolean predictSecondaryStructure, boolean externalSecStr)
  {
    this.visibleChainAnnotation = addAlignmentAnnotations;
    this.predictSecondaryStructure = predictSecondaryStructure;
    this.externalSecondaryStructure = externalSecStr;
  }

  public void xferSettings()
  {
    if (this.getDoXferSettings())
    {
      this.visibleChainAnnotation = StructureImportSettings
              .isVisibleChainAnnotation();
      this.predictSecondaryStructure = StructureImportSettings
              .isProcessSecondaryStructure();
      this.externalSecondaryStructure = StructureImportSettings
              .isExternalSecondaryStructure();
      this.temperatureFactorType = StructureImportSettings
              .getTemperatureFactorType();
    }
  }

  public StructureFile(boolean parseImmediately, Object dataObject,
          DataSourceType sourceType) throws IOException
  {
    super(parseImmediately, dataObject, sourceType);
  }

  public StructureFile(boolean a, FileParse fp) throws IOException
  {
    super(a, fp);
  }

  public StructureFile()
  {
  }

  protected SequenceI postProcessChain(PDBChain chain)
  {
    SequenceI pdbSequence = chain.sequence;
    pdbSequence.setName(getId() + "|" + pdbSequence.getName());
    PDBEntry entry = new PDBEntry();
    entry.setId(getId());
    entry.setFakedPDBId(!isPPDBIdAvailable());
    entry.setType(getStructureFileType());
    if (chain.id != null)
    {
      entry.setChainCode(chain.id);
    }
    if (inFile != null)
    {
      entry.setFile(inFile.getAbsolutePath());
    }
    else
    {
      entry.setFile(getDataName());
    }

    DBRefEntry sourceDBRef = new DBRefEntry();
    sourceDBRef.setAccessionId(getId());
    sourceDBRef.setSource(DBRefSource.PDB);
    // TODO: specify version for 'PDB' database ref if it is read from a file.
    // TODO: decide if jalview.io should be creating primary refs!
    sourceDBRef.setVersion("");
    pdbSequence.addPDBId(entry);
    pdbSequence.addDBRef(sourceDBRef);
    SequenceI chainseq = pdbSequence;
    seqs.addElement(chainseq);
    AlignmentAnnotation[] chainannot = chainseq.getAnnotation();

    if (chainannot != null && visibleChainAnnotation)
    {
      for (int ai = 0; ai < chainannot.length; ai++)
      {
        chainannot[ai].visible = visibleChainAnnotation;
        annotations.addElement(chainannot[ai]);
      }
    }
    return chainseq;
  }

  /**
   * filetype of structure file - default is PDB
   */
  String structureFileType = PDBEntry.Type.PDB.toString();

  protected void setStructureFileType(String structureFileType)
  {
    this.structureFileType = structureFileType;
  }

  /**
   * filetype of last file processed
   * 
   * @return
   */
  public String getStructureFileType()
  {
    return structureFileType;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void processPdbFileWithAnnotate3d(List<SequenceI> rna)
          throws Exception
  {
    // System.out.println("this is a PDB format and RNA sequence");
    // note: we use reflection here so that the applet can compile and run
    // without the HTTPClient bits and pieces needed for accessing Annotate3D
    // web service
    try
    {
      Class cl = Class.forName("jalview.ws.jws1.Annotate3D");
      if (cl != null)
      {
        // TODO: use the PDB ID of the structure if one is available, to save
        // bandwidth and avoid uploading the whole structure to the service
        Object annotate3d = cl.getConstructor(new Class[] {})
                .newInstance(new Object[] {});
        AlignmentI al = ((AlignmentI) cl
                .getMethod("getRNAMLFor", new Class[]
                { FileParse.class })
                .invoke(annotate3d, new Object[]
                { new FileParse(getDataName(), dataSourceType) }));
        for (SequenceI sq : al.getSequences())
        {
          if (sq.getDatasetSequence() != null)
          {
            if (sq.getDatasetSequence().getAllPDBEntries() != null)
            {
              sq.getDatasetSequence().getAllPDBEntries().clear();
            }
          }
          else
          {
            if (sq.getAllPDBEntries() != null)
            {
              sq.getAllPDBEntries().clear();
            }
          }
        }
        replaceAndUpdateChains(rna, al, AlignSeq.DNA, false);
      }
    } catch (ClassNotFoundException x)
    {
      // ignore classnotfounds - occurs in applet
    }
  }

  @SuppressWarnings("unchecked")
  protected void replaceAndUpdateChains(List<SequenceI> prot, AlignmentI al,
          String pep, boolean b)
  {
    List<List<? extends Object>> replaced = AlignSeq
            .replaceMatchingSeqsWith(seqs, annotations, prot, al, pep,
                    false);
    for (PDBChain ch : getChains())
    {
      int p = 0;
      for (SequenceI sq : (List<SequenceI>) replaced.get(0))
      {
        p++;
        if (sq == ch.sequence || sq.getDatasetSequence() == ch.sequence)
        {
          p = -p;
          break;
        }
      }
      if (p < 0)
      {
        p = -p - 1;
        // set shadow entry for chains
        ch.shadow = (SequenceI) replaced.get(1).get(p);
        ch.shadowMap = ((AlignSeq) replaced.get(2).get(p))
                .getMappingFromS1(false);
      }
    }
  }

  /**
   * Predict secondary structure for RNA and/or protein sequences and add as
   * annotations
   * 
   * @param rnaSequences
   * @param proteinSequences
   */
  protected void addSecondaryStructure(List<SequenceI> rnaSequences,
          List<SequenceI> proteinSequences)
  {
    /*
     * Currently using Annotate3D for RNA, but only if the 'use external
     * prediction' flag is set
     */
    if (externalSecondaryStructure && rnaSequences.size() > 0)
    {
      try
      {
        processPdbFileWithAnnotate3d(rnaSequences);
      } catch (Exception x)
      {
        System.err.println("Exceptions when dealing with RNA in pdb file");
        x.printStackTrace();

      }
    }

    /*
     * Currently using JMol PDB parser for peptide
     */
    if (proteinSequences.size() > 0)
    {
      try
      {
        processWithJmolParser(proteinSequences, true);
      } catch (Exception x)
      {
        System.err.println(
                "Exceptions from Jmol when processing data in pdb file");
        x.printStackTrace();
      }
    }
  }

  private void processWithJmolParser(List<SequenceI> prot,
          boolean doXferSettings) throws MalformedURLException, IOException
  {
    FileParse fp = new FileParse(getDataName(), dataSourceType);

    StructureImportSettings.setShowSeqFeatures(false);
    StructureImportSettings.setVisibleChainAnnotation(false);
    StructureImportSettings
            .setProcessSecondaryStructure(predictSecondaryStructure);
    StructureImportSettings
            .setExternalSecondaryStructure(externalSecondaryStructure);
    StructureImportSettings.setTemperatureFactorType(temperatureFactorType);
    JmolParser jmf = new JmolParser(fp, doXferSettings);
    AlignmentI al = new Alignment((SequenceI[]) jmf.getSeqsAsArray());
    jmf.addAnnotations(al);
    for (SequenceI sq : al.getSequences())
    {
      if (sq.getDatasetSequence() != null)
      {
        sq.getDatasetSequence().getAllPDBEntries().clear();
      }
      else
      {
        sq.getAllPDBEntries().clear();
      }
    }
    replaceAndUpdateChains(prot, al, AlignSeq.PEP, false);
    StructureImportSettings.setShowSeqFeatures(true);
  }

  /**
   * Answers the first PDBChain found matching the given id, or null if none is
   * found
   * 
   * @param id
   * @return
   */
  public PDBChain findChain(String id)
  {
    for (PDBChain chain : getChains())
    {
      if (chain.id.equals(id))
      {
        return chain;
      }
    }
    return null;
  }

  public void makeResidueList()
  {
    for (PDBChain chain : getChains())
    {
      chain.makeResidueList(visibleChainAnnotation);
    }
  }

  public void makeCaBondList()
  {
    for (PDBChain chain : getChains())
    {
      chain.makeCaBondList();
    }
  }

  public void setChargeColours()
  {
    for (PDBChain chain : getChains())
    {
      chain.setChargeColours();
    }
  }

  public void setColours(jalview.schemes.ColourSchemeI cs)
  {
    for (PDBChain chain : getChains())
    {
      chain.setChainColours(cs);
    }
  }

  public void setChainColours()
  {
    int i = 0;
    for (PDBChain chain : getChains())
    {
      chain.setChainColours(Color.getHSBColor(1.0f / i++, .4f, 1.0f));
    }
  }

  public static boolean isRNA(SequenceI seq)
  {
    int length = seq.getLength();
    for (int i = 0; i < length; i++)
    {
      char c = seq.getCharAt(i);
      if ((c != 'A') && (c != 'C') && (c != 'G') && (c != 'U'))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * make a friendly ID string.
   * 
   * @param dataName
   * @return truncated dataName to after last '/' and pruned .extension if
   *         present
   */
  protected String safeName(String dataName)
  {
    int p = 0;
    while ((p = dataName.indexOf("/")) > -1 && p < dataName.length())
    {
      dataName = dataName.substring(p + 1);
    }
    if (dataName.indexOf(".") > -1)
    {
      dataName = dataName.substring(0, dataName.lastIndexOf("."));
    }
    return dataName;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public Vector<PDBChain> getChains()
  {
    return chains;
  }

  public void setChains(Vector<PDBChain> chains)
  {
    this.chains = chains;
  }

  public Type getDbRefType()
  {
    return dbRefType;
  }

  public void setDbRefType(String dbRefType)
  {
    this.dbRefType = Type.getType(dbRefType);
  }

  public void setDbRefType(Type dbRefType)
  {
    this.dbRefType = dbRefType;
  }

  /**
   * Returns a descriptor for suitable feature display settings with
   * <ul>
   * <li>ResNums or insertions features visible</li>
   * <li>insertions features coloured red</li>
   * <li>ResNum features coloured by label</li>
   * <li>Insertions displayed above (on top of) ResNums</li>
   * </ul>
   */
  @Override
  public FeatureSettingsModelI getFeatureColourScheme()
  {
    return new PDBFeatureSettings();
  }

  /**
   * Answers true if the structure file has a PDBId
   * 
   * @return
   */
  public boolean isPPDBIdAvailable()
  {
    return pdbIdAvailable;
  }

  public void setPDBIdAvailable(boolean pdbIdAvailable)
  {
    this.pdbIdAvailable = pdbIdAvailable;
  }
}
