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
package mc_view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileParse;
import jalview.io.StructureFile;
import jalview.util.MessageManager;

public class PDBfile extends StructureFile
{
  private static String CALC_ID_PREFIX = "JalviewPDB";

  public PDBfile(boolean addAlignmentAnnotations,
          boolean predictSecondaryStructure, boolean externalSecStr)
  {
    super();
    addSettings(addAlignmentAnnotations, predictSecondaryStructure,
            externalSecStr);
  }

  public PDBfile(boolean addAlignmentAnnotations, boolean predictSecStr,
          boolean externalSecStr, String dataObject,
          DataSourceType sourceType) throws IOException
  {
    super(false, dataObject, sourceType);
    addSettings(addAlignmentAnnotations, predictSecStr, externalSecStr);
    doParse();
  }

  public PDBfile(boolean addAlignmentAnnotations, boolean predictSecStr,
          boolean externalSecStr, FileParse source) throws IOException
  {
    super(false, source);
    addSettings(addAlignmentAnnotations, predictSecStr, externalSecStr);
    doParse();
  }

  @Override
  public String print(SequenceI[] seqs, boolean jvSuffix)
  {
    return null;
  }

  @Override
  public void parse() throws IOException
  {
    setDbRefType(DBRefSource.PDB);
    // TODO set the filename sensibly - try using data source name.
    setId(safeName(getDataName()));

    setChains(new Vector<PDBChain>());
    List<SequenceI> rna = new ArrayList<SequenceI>();
    List<SequenceI> prot = new ArrayList<SequenceI>();
    PDBChain tmpchain;
    String line = null;
    boolean modelFlag = false;
    boolean terFlag = false;
    String lastID = "";

    int indexx = 0;
    String atomnam = null;
    try
    {
      while ((line = nextLine()) != null)
      {
        if (line.indexOf("HEADER") == 0)
        {
          if (line.length() > 62)
          {
            String tid;
            if (line.length() > 67)
            {
              tid = line.substring(62, 67).trim();
            }
            else
            {
              tid = line.substring(62).trim();
            }
            if (tid.length() > 0)
            {
              setId(tid);
            }
            continue;
          }
        }
        // Were we to do anything with SEQRES - we start it here
        if (line.indexOf("SEQRES") == 0)
        {
        }

        if (line.indexOf("MODEL") == 0)
        {
          modelFlag = true;
        }

        if (line.indexOf("TER") == 0)
        {
          terFlag = true;
        }

        if (modelFlag && line.indexOf("ENDMDL") == 0)
        {
          break;
        }
        if (line.indexOf("ATOM") == 0
                || (line.indexOf("HETATM") == 0 && !terFlag))
        {
          terFlag = false;

          // Jalview is only interested in CA bonds????
          atomnam = line.substring(12, 15).trim();
          if (!atomnam.equals("CA") && !atomnam.equals("P"))
          {
            continue;
          }

          Atom tmpatom = new Atom(line);
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
            // PDBfile never handles alphafold models
            tmpchain = new PDBChain(getId(), tmpatom.chain);
            getChains().add(tmpchain);
            tmpchain.atoms.addElement(tmpatom);
          }
          lastID = tmpatom.resNumIns.trim();
        }
        index++;
      }

      makeResidueList();
      makeCaBondList();

      if (getId() == null)
      {
        setId(inFile.getName());
      }
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
      }
      if (predictSecondaryStructure)
      {
        addSecondaryStructure(rna, prot);
      }
    } catch (OutOfMemoryError er)
    {
      System.out.println("OUT OF MEMORY LOADING PDB FILE");
      throw new IOException(MessageManager
              .getString("exception.outofmemory_loading_pdb_file"));
    } catch (NumberFormatException ex)
    {
      if (line != null)
      {
        System.err.println("Couldn't read number from line:");
        System.err.println(line);
      }
    }
    markCalcIds();
  }

  /**
   * Process a parsed chain to construct and return a Sequence, and add it to
   * the list of sequences parsed.
   * 
   * @param chain
   * @return
   */

  public static boolean isCalcIdHandled(String calcId)
  {
    return calcId != null && (CALC_ID_PREFIX.equals(calcId));
  }

  public static boolean isCalcIdForFile(AlignmentAnnotation alan,
          String pdbFile)
  {
    return alan.getCalcId() != null
            && CALC_ID_PREFIX.equals(alan.getCalcId())
            && pdbFile.equals(alan.getProperty("PDBID"));
  }

  public static String relocateCalcId(String calcId,
          Hashtable<String, String> alreadyLoadedPDB) throws Exception
  {
    int s = CALC_ID_PREFIX.length(),
            end = calcId.indexOf(CALC_ID_PREFIX, s);
    String between = calcId.substring(s, end - 1);
    return CALC_ID_PREFIX + alreadyLoadedPDB.get(between) + ":"
            + calcId.substring(end);
  }

  private void markCalcIds()
  {
    for (SequenceI sq : seqs)
    {
      if (sq.getAnnotation() != null)
      {
        for (AlignmentAnnotation aa : sq.getAnnotation())
        {
          String oldId = aa.getCalcId();
          if (oldId == null)
          {
            oldId = "";
          }
          aa.setCalcId(CALC_ID_PREFIX);
          aa.setProperty("PDBID", getId());
          aa.setProperty("oldCalcId", oldId);
        }
      }
    }
  }

}
