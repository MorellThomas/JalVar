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
package jalview.ext.jmol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.c.CBK;
import org.jmol.c.STR;
import org.jmol.modelset.ModelSet;
import org.jmol.viewer.Viewer;

import com.stevesoft.pat.Regex;

import jalview.bin.Console;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.datamodel.annotations.AlphaFoldAnnotationRowBuilder;
import jalview.datamodel.annotations.AnnotationRowBuilder;
import jalview.io.DataSourceType;
import jalview.io.FileParse;
import jalview.io.StructureFile;
import jalview.schemes.ResidueProperties;
import jalview.structure.StructureImportSettings;
import jalview.util.Format;
import jalview.util.MessageManager;
import jalview.ws.dbsources.EBIAlfaFold;
import mc_view.Atom;
import mc_view.PDBChain;
import mc_view.Residue;

/**
 * Import and process files with Jmol for file like PDB, mmCIF
 * 
 * @author jprocter
 * 
 */
public class JmolParser extends StructureFile implements JmolStatusListener
{
  Viewer viewer = null;

  public JmolParser(boolean immediate, Object inFile,
          DataSourceType sourceType) throws IOException
  {
    // BH 2018 File or String for filename
    super(immediate, inFile, sourceType);

  }

  public JmolParser(Object inFile, DataSourceType sourceType)
          throws IOException
  {
    this(inFile, sourceType, null);
  }

  public JmolParser(Object inFile, DataSourceType sourceType,
          StructureImportSettings.TFType tempfacType) throws IOException
  {
    super(inFile, sourceType, tempfacType);
  }

  public JmolParser(FileParse fp, boolean doXferSettings) throws IOException
  {
    super(fp, doXferSettings);
  }
  public JmolParser(FileParse fp) throws IOException
  {
    super(fp);
  }

  public JmolParser()
  {
  }

  /**
   * Calls the Jmol library to parse the PDB/mmCIF file, and then inspects the
   * resulting object model to generate Jalview-style sequences, with secondary
   * structure annotation added where available (i.e. where it has been computed
   * by Jmol using DSSP).
   * 
   * @see jalview.io.AlignFile#parse()
   */
  @Override
  public void parse() throws IOException
  {
    parse(true);
  }

  @Override
  public void parse(boolean doXferSettings) throws IOException
  {
    setChains(new Vector<PDBChain>());
    Viewer jmolModel = getJmolData();
    jmolModel.openReader(getDataName(), getDataName(), getReader());
    waitForScript(jmolModel);

    /*
     * Convert one or more Jmol Model objects to Jalview sequences
     */
    if (jmolModel.ms.mc > 0)
    {
      // ideally we do this
      // try
      // {
      // setStructureFileType(jmolModel.evalString("show _fileType"));
      // } catch (Exception q)
      // {
      // }
      // ;
      // instead, we distinguish .cif from non-.cif by filename
      setStructureFileType(
              getDataName().toLowerCase(Locale.ROOT).endsWith(".cif")
                      ? PDBEntry.Type.MMCIF.toString()
                      : "PDB");

      transformJmolModelToJalview(jmolModel.ms, doXferSettings);
    }
  }

  /**
   * create a headless jmol instance for dataprocessing
   * 
   * @return
   */
  private Viewer getJmolData()
  {
    if (viewer == null)
    {
      try
      {
        /*
         * params -o (output to sysout) -n (nodisplay) -x (exit when finished)
         * see http://wiki.jmol.org/index.php/Jmol_Application
         */

        viewer = JalviewJmolBinding.getJmolData(this);
        // ensure the 'new' (DSSP) not 'old' (Ramachandran) SS method is used
        viewer.setBooleanProperty("defaultStructureDSSP", true);
      } catch (ClassCastException x)
      {
        throw new Error(MessageManager.formatMessage(
                "error.jmol_version_not_compatible_with_jalview_version",
                new String[]
                { JmolViewer.getJmolVersion() }), x);
      }
    }
    return viewer;
  }

  public static Regex getNewAlphafoldValidator()
  {
    Regex validator = new Regex("(AF-[A-Z]+[0-9]+[A-Z0-9]+-F1)");
    validator.setIgnoreCase(true);
    return validator;
  }

  PDBEntry.Type jmolFiletype = null;

  /**
   * resolve a jmol filetype string and update the jmolFiletype field
   * accordingly
   * 
   * @param jmolIdentifiedFileType
   * @return true if filetype was identified as MMCIF, PDB
   */
  public boolean updateFileType(String jmolIdentifiedFileType)
  {
    if (jmolIdentifiedFileType == null
            || jmolIdentifiedFileType.trim().equals(""))
    {
      return false;
    }
    if ("mmcif".equalsIgnoreCase(jmolIdentifiedFileType))
    {
      jmolFiletype = PDBEntry.Type.MMCIF;
      return true;
    }
    if ("pdb".equalsIgnoreCase(jmolIdentifiedFileType))
    {
      jmolFiletype = PDBEntry.Type.PDB;
      return true;
    }
    return false;
  }

  public void transformJmolModelToJalview(ModelSet ms,
          boolean localDoXferSettings) throws IOException
  {
    try
    {
      Regex alphaFold = getNewAlphafoldValidator();
      String lastID = "";
      List<SequenceI> rna = new ArrayList<SequenceI>();
      List<SequenceI> prot = new ArrayList<SequenceI>();
      PDBChain tmpchain;
      String pdbId = (String) ms.getInfo(0, "title");
      boolean isMMCIF = false;
      String jmolFileType_String = (String) ms.getInfo(0, "fileType");
      if (updateFileType(jmolFileType_String))
      {
        setStructureFileType(jmolFiletype.toString());
      }

      isMMCIF = PDBEntry.Type.MMCIF.equals(jmolFiletype);

      if (pdbId == null)
      {
        setId(safeName(getDataName()));
        setPDBIdAvailable(false);
      }
      else
      {
        setId(pdbId);
        setPDBIdAvailable(true);
        setAlphafoldModel(alphaFold.search(pdbId) && isMMCIF);
      }
      List<Atom> significantAtoms = convertSignificantAtoms(ms);
      for (Atom tmpatom : significantAtoms)
      {
        if (tmpatom.resNumIns.trim().equals(lastID))
        {
          // phosphorylated protein - seen both CA and P..
          continue;
        }
        tmpchain = findChain(tmpatom.chain);
        if (tmpchain != null)
        {
          tmpchain.atoms.addElement(tmpatom);
        }
        else
        {
          AnnotationRowBuilder builder = null;
          if (isAlphafoldModel()
                  || getTemperatureFactorType() == StructureImportSettings.TFType.PLDDT)
          {
            builder = new AlphaFoldAnnotationRowBuilder();
          }

          tmpchain = new PDBChain(getId(), tmpatom.chain, builder);
          getChains().add(tmpchain);
          tmpchain.atoms.addElement(tmpatom);
        }
        lastID = tmpatom.resNumIns.trim();
      }
      if (isParseImmediately() && localDoXferSettings)
      {
        // configure parsing settings from the static singleton
        xferSettings();
      }

      makeResidueList();
      makeCaBondList();

      for (PDBChain chain : getChains())
      {
        SequenceI chainseq = postProcessChain(chain);
        if (isRNA(chainseq))
        {
          rna.add(chainseq);
        }
        else
        {
          prot.add(chainseq);
        }

        // look at local setting for adding secondary tructure
        if (predictSecondaryStructure)
        {
          createAnnotation(chainseq, chain, ms.at);
        }
      }
      // if Alphafold, fetch the PAE matrix if doesn't already have one
      if (isAlphafoldModel() && !hasPAEMatrix())
      {
        try
        {
          Console.info("Retrieving PAE for " + pdbId);
          File paeFile = EBIAlfaFold.fetchAlphaFoldPAE(pdbId, null);
          this.setPAEMatrix(paeFile.getAbsolutePath());
        } catch (Throwable t)
        {
          Console.error("Couldn't get the pAE for " + pdbId, t);
        }
      }
      // add a PAEMatrix if set (either by above or otherwise)
      if (hasPAEMatrix())
      {
        try
        {
          Alignment al = new Alignment(prot.toArray(new SequenceI[0]));
          EBIAlfaFold.addAlphaFoldPAE(al, new File(this.getPAEMatrix()), 0,
                  null, false, false, null);

          if (al.getAlignmentAnnotation() != null)
          {
            for (AlignmentAnnotation alann : al.getAlignmentAnnotation())
            {
              annotations.add(alann);
            }
          }
        } catch (Throwable ff)
        {
          Console.error("Couldn't import PAE Matrix from " + getPAEMatrix(),
                  ff);
          warningMessage += "Couldn't import PAE Matrix"
                  + getNewlineString() + ff.getLocalizedMessage()
                  + getNewlineString();
        }
      }
    } catch (OutOfMemoryError er)
    {
      System.out.println(
              "OUT OF MEMORY LOADING TRANSFORMING JMOL MODEL TO JALVIEW MODEL");
      throw new IOException(MessageManager
              .getString("exception.outofmemory_loading_mmcif_file"));
    }
  }

  private List<Atom> convertSignificantAtoms(ModelSet ms)
  {
    List<Atom> significantAtoms = new ArrayList<Atom>();
    HashMap<String, org.jmol.modelset.Atom> chainTerMap = new HashMap<String, org.jmol.modelset.Atom>();
    org.jmol.modelset.Atom prevAtom = null;
    for (org.jmol.modelset.Atom atom : ms.at)
    {
      if (atom.getAtomName().equalsIgnoreCase("CA")
              || atom.getAtomName().equalsIgnoreCase("P"))
      {
        if (!atomValidated(atom, prevAtom, chainTerMap))
        {
          continue;
        }
        Atom curAtom = new Atom(atom.x, atom.y, atom.z);
        curAtom.atomIndex = atom.getIndex();
        curAtom.chain = atom.getChainIDStr();
        curAtom.insCode = atom.group.getInsertionCode() == '\000' ? ' '
                : atom.group.getInsertionCode();
        curAtom.name = atom.getAtomName();
        curAtom.number = atom.getAtomNumber();
        curAtom.resName = atom.getGroup3(true);
        curAtom.resNumber = atom.getResno();
        curAtom.occupancy = ms.occupancies != null
                ? ms.occupancies[atom.getIndex()]
                : Float.valueOf(atom.getOccupancy100());
        String fmt = new Format("%4i").form(curAtom.resNumber);
        curAtom.resNumIns = (fmt + curAtom.insCode);
        curAtom.tfactor = atom.getBfactor100() / 100f;
        curAtom.type = 0;
        // significantAtoms.add(curAtom);
        // ignore atoms from subsequent models
        if (!significantAtoms.contains(curAtom))
        {
          significantAtoms.add(curAtom);
        }
        prevAtom = atom;
      }
    }
    return significantAtoms;
  }

  private boolean atomValidated(org.jmol.modelset.Atom curAtom,
          org.jmol.modelset.Atom prevAtom,
          HashMap<String, org.jmol.modelset.Atom> chainTerMap)
  {
    // System.out.println("Atom: " + curAtom.getAtomNumber()
    // + " Last atom index " + curAtom.group.lastAtomIndex);
    if (chainTerMap == null || prevAtom == null)
    {
      return true;
    }
    String curAtomChId = curAtom.getChainIDStr();
    String prevAtomChId = prevAtom.getChainIDStr();
    // new chain encoutered
    if (!prevAtomChId.equals(curAtomChId))
    {
      // On chain switch add previous chain termination to xTerMap if not exists
      if (!chainTerMap.containsKey(prevAtomChId))
      {
        chainTerMap.put(prevAtomChId, prevAtom);
      }
      // if current atom belongs to an already terminated chain and the resNum
      // diff < 5 then mark as valid and update termination Atom
      if (chainTerMap.containsKey(curAtomChId))
      {
        if (curAtom.getResno() < chainTerMap.get(curAtomChId).getResno())
        {
          return false;
        }
        if ((curAtom.getResno()
                - chainTerMap.get(curAtomChId).getResno()) < 5)
        {
          chainTerMap.put(curAtomChId, curAtom);
          return true;
        }
        return false;
      }
    }
    // atom with previously terminated chain encountered
    else if (chainTerMap.containsKey(curAtomChId))
    {
      if (curAtom.getResno() < chainTerMap.get(curAtomChId).getResno())
      {
        return false;
      }
      if ((curAtom.getResno()
              - chainTerMap.get(curAtomChId).getResno()) < 5)
      {
        chainTerMap.put(curAtomChId, curAtom);
        return true;
      }
      return false;
    }
    // HETATM with resNum jump > 2
    return !(curAtom.isHetero()
            && ((curAtom.getResno() - prevAtom.getResno()) > 2));
  }

  private void createAnnotation(SequenceI sequence, PDBChain chain,
          org.jmol.modelset.Atom[] jmolAtoms)
  {
    char[] secstr = new char[sequence.getLength()];
    char[] secstrcode = new char[sequence.getLength()];

    // Ensure Residue size equals Seq size
    if (chain.residues.size() != sequence.getLength())
    {
      return;
    }
    int annotIndex = 0;
    for (Residue residue : chain.residues)
    {
      Atom repAtom = residue.getAtoms().get(0);
      STR proteinStructureSubType = jmolAtoms[repAtom.atomIndex].group
              .getProteinStructureSubType();
      setSecondaryStructure(proteinStructureSubType, annotIndex, secstr,
              secstrcode);
      ++annotIndex;
    }
    addSecondaryStructureAnnotation(chain.pdbid, sequence, secstr,
            secstrcode, chain.id, sequence.getStart());
  }

  /**
   * Helper method that adds an AlignmentAnnotation for secondary structure to
   * the sequence, provided at least one secondary structure assignment has been
   * made
   * 
   * @param modelTitle
   * @param seq
   * @param secstr
   * @param secstrcode
   * @param chainId
   * @param firstResNum
   * @return
   */
  protected void addSecondaryStructureAnnotation(String modelTitle,
          SequenceI sq, char[] secstr, char[] secstrcode, String chainId,
          int firstResNum)
  {
    int length = sq.getLength();
    boolean ssFound = false;
    Annotation asecstr[] = new Annotation[length + (firstResNum-sq.getStart())];
    for (int p = 0; p < length; p++)
    {
      if (secstr[p] >= 'A' && secstr[p] <= 'z')
      {
        try
        {
          asecstr[p] = new Annotation(null, null, secstrcode[p], Float.NaN);
          ssFound = true;
        } catch (Exception e)
        {
          // e.printStackTrace();
        }
      }
    }

    if (ssFound)
    {
      String mt = modelTitle == null ? getDataName() : modelTitle;
      mt += chainId;
      AlignmentAnnotation ann = new AlignmentAnnotation(
              "Secondary Structure", "Secondary Structure for " + mt,
              asecstr);
      ann.belowAlignment = true;
      ann.visible = true;
      ann.autoCalculated = false;
      ann.setCalcId(getClass().getName());
      ann.adjustForAlignment();
      ann.validateRangeAndDisplay();
      annotations.add(ann);
      sq.addAlignmentAnnotation(ann);
    }
  }

  private void waitForScript(Viewer jmd)
  {
    while (jmd.isScriptExecuting())
    {
      try
      {
        Thread.sleep(50);

      } catch (InterruptedException x)
      {
      }
    }
  }

  /**
   * Convert Jmol's secondary structure code to Jalview's, and stored it in the
   * secondary structure arrays at the given sequence position
   * 
   * @param proteinStructureSubType
   * @param pos
   * @param secstr
   * @param secstrcode
   */
  protected void setSecondaryStructure(STR proteinStructureSubType, int pos,
          char[] secstr, char[] secstrcode)
  {
    switch (proteinStructureSubType)
    {
    case HELIX310:
      secstr[pos] = '3';
      break;
    case HELIX:
    case HELIXALPHA:
      secstr[pos] = 'H';
      break;
    case HELIXPI:
      secstr[pos] = 'P';
      break;
    case SHEET:
      secstr[pos] = 'E';
      break;
    default:
      secstr[pos] = 0;
    }

    switch (proteinStructureSubType)
    {
    case HELIX310:
    case HELIXALPHA:
    case HELIXPI:
    case HELIX:
      secstrcode[pos] = 'H';
      break;
    case SHEET:
      secstrcode[pos] = 'E';
      break;
    default:
      secstrcode[pos] = 0;
    }
  }

  /**
   * Convert any non-standard peptide codes to their standard code table
   * equivalent. (Initial version only does Selenomethionine MSE->MET.)
   * 
   * @param threeLetterCode
   * @param seq
   * @param pos
   */
  protected void replaceNonCanonicalResidue(String threeLetterCode,
          char[] seq, int pos)
  {
    String canonical = ResidueProperties
            .getCanonicalAminoAcid(threeLetterCode);
    if (canonical != null && !canonical.equalsIgnoreCase(threeLetterCode))
    {
      seq[pos] = ResidueProperties.getSingleCharacterCode(canonical);
    }
  }

  /**
   * Not implemented - returns null
   */
  @Override
  public String print(SequenceI[] seqs, boolean jvSuffix)
  {
    return null;
  }

  /**
   * Not implemented
   */
  @Override
  public void setCallbackFunction(String callbackType,
          String callbackFunction)
  {
  }

  @Override
  public void notifyCallback(CBK cbType, Object[] data)
  {
    String strInfo = (data == null || data[1] == null ? null
            : data[1].toString());
    switch (cbType)
    {
    case ECHO:
      sendConsoleEcho(strInfo);
      break;
    case SCRIPT:
      notifyScriptTermination((String) data[2],
              ((Integer) data[3]).intValue());
      break;
    case MEASURE:
      String mystatus = (String) data[3];
      if (mystatus.indexOf("Picked") >= 0
              || mystatus.indexOf("Sequence") >= 0)
      {
        // Picking mode
        sendConsoleMessage(strInfo);
      }
      else if (mystatus.indexOf("Completed") >= 0)
      {
        sendConsoleEcho(strInfo.substring(strInfo.lastIndexOf(",") + 2,
                strInfo.length() - 1));
      }
      break;
    case MESSAGE:
      sendConsoleMessage(data == null ? null : strInfo);
      break;
    case PICK:
      sendConsoleMessage(strInfo);
      break;
    default:
      break;
    }
  }

  String lastConsoleEcho = "";

  private void sendConsoleEcho(String string)
  {
    lastConsoleEcho += string;
    lastConsoleEcho += "\n";
  }

  String lastConsoleMessage = "";

  private void sendConsoleMessage(String string)
  {
    lastConsoleMessage += string;
    lastConsoleMessage += "\n";
  }

  int lastScriptTermination = -1;

  String lastScriptMessage = "";

  private void notifyScriptTermination(String string, int intValue)
  {
    lastScriptMessage += string;
    lastScriptMessage += "\n";
    lastScriptTermination = intValue;
  }

  @Override
  public boolean notifyEnabled(CBK callbackPick)
  {
    switch (callbackPick)
    {
    case MESSAGE:
    case SCRIPT:
    case ECHO:
    case LOADSTRUCT:
    case ERROR:
      return true;
    default:
      return false;
    }
  }

  /**
   * Not implemented - returns null
   */
  @Override
  public String eval(String strEval)
  {
    return null;
  }

  /**
   * Not implemented - returns null
   */
  @Override
  public float[][] functionXY(String functionName, int x, int y)
  {
    return null;
  }

  /**
   * Not implemented - returns null
   */
  @Override
  public float[][][] functionXYZ(String functionName, int nx, int ny,
          int nz)
  {
    return null;
  }

  /**
   * Not implemented - returns null
   */
  @Override
  public String createImage(String fileName, String imageType,
          Object text_or_bytes, int quality)
  {
    return null;
  }

  /**
   * Not implemented - returns null
   */
  @Override
  public Map<String, Object> getRegistryInfo()
  {
    return null;
  }

  /**
   * Not implemented
   */
  @Override
  public void showUrl(String url)
  {
  }

  /**
   * Not implemented - returns null
   */
  @Override
  public int[] resizeInnerPanel(String data)
  {
    return null;
  }

  @Override
  public Map<String, Object> getJSpecViewProperty(String arg0)
  {
    return null;
  }

  public boolean isPredictSecondaryStructure()
  {
    return predictSecondaryStructure;
  }

  public void setPredictSecondaryStructure(
          boolean predictSecondaryStructure)
  {
    this.predictSecondaryStructure = predictSecondaryStructure;
  }

  public boolean isVisibleChainAnnotation()
  {
    return visibleChainAnnotation;
  }

  public void setVisibleChainAnnotation(boolean visibleChainAnnotation)
  {
    this.visibleChainAnnotation = visibleChainAnnotation;
  }

}
