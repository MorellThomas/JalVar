
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
package jalview.ws.dbsources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.stevesoft.pat.Regex;

import jalview.api.FeatureSettingsModelI;
import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.GroupSet;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.Desktop;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;
import jalview.io.PDBFeatureSettings;
import jalview.structure.StructureImportSettings.TFType;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;
import jalview.ws.utils.UrlDownloadClient;

/**
 * @author JimP
 * 
 */
public class EBIAlfaFold extends EbiFileRetrievedProxy
{
  private static final String SEPARATOR = "|";

  private static final String COLON = ":";

  private static final int PDB_ID_LENGTH = 4;

  private static String AF_VERSION = "3";

  public EBIAlfaFold()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator()
   */
  @Override
  public String getAccessionSeparator()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator()
   */
  @Override
  public Regex getAccessionValidator()
  {
    Regex validator = new Regex("(AF-[A-Z]+[0-9]+[A-Z0-9]+-F1)");
    validator.setIgnoreCase(true);
    return validator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource()
   */
  @Override
  public String getDbSource()
  {
    return "ALPHAFOLD";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    return "1";
  }

  public static String getAlphaFoldCifDownloadUrl(String id, String vnum)
  {
    if (vnum == null || vnum.length() == 0)
    {
      vnum = AF_VERSION;
    }
    return "https://alphafold.ebi.ac.uk/files/" + id + "-model_v" + vnum
            + ".cif";
  }

  public static String getAlphaFoldPaeDownloadUrl(String id, String vnum)
  {
    if (vnum == null || vnum.length() == 0)
    {
      vnum = AF_VERSION;
    }
    return "https://alphafold.ebi.ac.uk/files/" + id
            + "-predicted_aligned_error_v" + vnum + ".json";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getSequenceRecords(java.lang.String[])
   */
  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    return getSequenceRecords(queries, null);
  }

  public AlignmentI getSequenceRecords(String queries, String retrievalUrl)
          throws Exception
  {
    AlignmentI pdbAlignment = null;
    String chain = null;
    String id = null;
    if (queries.indexOf(COLON) > -1)
    {
      chain = queries.substring(queries.indexOf(COLON) + 1);
      id = queries.substring(0, queries.indexOf(COLON));
    }
    else
    {
      id = queries;
    }

    if (!isValidReference(id))
    {
      System.err.println(
              "(AFClient) Ignoring invalid alphafold query: '" + id + "'");
      stopQuery();
      return null;
    }
    String alphaFoldCif = getAlphaFoldCifDownloadUrl(id, AF_VERSION);
    if (retrievalUrl != null)
    {
      alphaFoldCif = retrievalUrl;
    }

    try
    {
      File tmpFile = File.createTempFile(id, ".cif");
      Console.debug("Retrieving structure file for " + id + " from "
              + alphaFoldCif);
      UrlDownloadClient.download(alphaFoldCif, tmpFile);

      // may not need this check ?
      file = tmpFile.getAbsolutePath();
      if (file == null)
      {
        return null;
      }
      // TODO Get the PAE file somewhere around here and remove from JmolParser

      pdbAlignment = importDownloadedStructureFromUrl(alphaFoldCif, tmpFile,
              id, chain, getDbSource(), getDbVersion());

      if (pdbAlignment == null || pdbAlignment.getHeight() < 1)
      {
        throw new Exception(MessageManager.formatMessage(
                "exception.no_pdb_records_for_chain", new String[]
                { id, ((chain == null) ? "' '" : chain) }));
      }
      // done during structure retrieval
      // retrieve_AlphaFold_pAE(id, pdbAlignment, retrievalUrl);

    } catch (Exception ex) // Problem parsing PDB file
    {
      stopQuery();
      throw (ex);
    }
    return pdbAlignment;
  }

  /**
   * get an alphafold pAE for the given id and return the File object of the
   * downloaded (temp) file
   * 
   * @param id
   * @param pdbAlignment
   * @param retrievalUrl
   *          - URL of .mmcif from EBI-AlphaFold - will be used to generate the
   *          pAE URL automatically
   * @throws IOException
   * @throws Exception
   */
  public static File fetchAlphaFoldPAE(String id, String retrievalUrl)
          throws IOException
  {
    // import PAE as contact matrix - assume this will work if there was a
    // model
    String paeURL = getAlphaFoldPaeDownloadUrl(id, AF_VERSION);

    if (retrievalUrl != null)
    {
      // manufacture the PAE url from a url like ...-model-vN.cif
      paeURL = retrievalUrl.replace("model", "predicted_aligned_error")
              .replace(".cif", ".json");
    }

    // check the cache
    File pae = paeDownloadCache.get(paeURL);
    if (pae != null && pae.exists() && (new Date().getTime()
            - pae.lastModified()) < PAE_CACHE_STALE_TIME)
    {
      Console.debug(
              "Using existing file in PAE cache for '" + paeURL + "'");
      return pae;
    }

    try
    {
      pae = File.createTempFile(id == null ? "af_pae" : id, "pae_json");
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    Console.debug("Downloading pae from " + paeURL + " to " + pae.toString()
            + "");
    try
    {
      UrlDownloadClient.download(paeURL, pae);
    } catch (IOException e)
    {
      throw e;
    }
    // cache and it if successful
    paeDownloadCache.put(paeURL, pae);
    return pae;
  }

  /**
   * get an alphafold pAE for the given id, and add it to sequence 0 in
   * pdbAlignment (assuming it came from structurefile parser).
   * 
   * @param id
   * @param pdbAlignment
   * @param retrievalUrl
   *          - URL of .mmcif from EBI-AlphaFold - will be used to generate the
   *          pAE URL automatically
   * @throws IOException
   * @throws Exception
   */
  public static void retrieve_AlphaFold_pAE(String id,
          AlignmentI pdbAlignment, String retrievalUrl) throws IOException
  {
    File pae = fetchAlphaFoldPAE(id, retrievalUrl);
    addAlphaFoldPAE(pdbAlignment, pae, 0, null, false, false, null);
  }

  public static void addAlphaFoldPAE(AlignmentI pdbAlignment, File pae,
          int index, String id, boolean isStruct, boolean isStructId,
          String label)
  {
    FileInputStream paeInput = null;
    try
    {
      paeInput = new FileInputStream(pae);
    } catch (FileNotFoundException e)
    {
      Console.error(
              "Could not find pAE file '" + pae.getAbsolutePath() + "'", e);
      return;
    }

    if (isStruct)
    {
      // ###### WRITE A TEST for this bit of the logic addAlphaFoldPAE with
      // different params.
      StructureSelectionManager ssm = StructureSelectionManager
              .getStructureSelectionManager(Desktop.instance);
      if (ssm != null)
      {
        String structFilename = isStructId ? ssm.findFileForPDBId(id) : id;
        addPAEToStructure(ssm, structFilename, pae, label);
      }

    }
    else
    {
      // attach to sequence?!
      try
      {
        if (!importPaeJSONAsContactMatrixToSequence(pdbAlignment, paeInput,
                index, id, label))
        {
          Console.warn("Could not import contact matrix from '"
                  + pae.getAbsolutePath() + "' to sequence.");
        }
      } catch (IOException e1)
      {
        Console.error("Error when importing pAE file '"
                + pae.getAbsolutePath() + "'", e1);
      } catch (ParseException e2)
      {
        Console.error("Error when parsing pAE file '"
                + pae.getAbsolutePath() + "'", e2);
      }
    }

  }

  public static void addPAEToStructure(StructureSelectionManager ssm,
          String structFilename, File pae, String label)
  {
    FileInputStream paeInput = null;
    try
    {
      paeInput = new FileInputStream(pae);
    } catch (FileNotFoundException e)
    {
      Console.error(
              "Could not find pAE file '" + pae.getAbsolutePath() + "'", e);
      return;
    }
    if (ssm == null)
    {
      ssm = StructureSelectionManager
              .getStructureSelectionManager(Desktop.instance);
    }
    if (ssm != null)
    {
      StructureMapping[] smArray = ssm.getMapping(structFilename);

      try
      {
        if (!importPaeJSONAsContactMatrixToStructure(smArray, paeInput,
                label))
        {
          Console.warn("Could not import contact matrix from '"
                  + pae.getAbsolutePath() + "' to structure.");
        }
      } catch (IOException e1)
      {
        Console.error("Error when importing pAE file '"
                + pae.getAbsolutePath() + "'", e1);
      } catch (ParseException e2)
      {
        Console.error("Error when parsing pAE file '"
                + pae.getAbsolutePath() + "'", e2);
      }
    }
  }

  /**
   * parses the given pAE matrix and adds it to sequence 0 in the given
   * alignment
   * 
   * @param pdbAlignment
   * @param pae_input
   * @return true if there was a pAE matrix added
   * @throws ParseException
   * @throws IOException
   * @throws Exception
   */
  public static boolean importPaeJSONAsContactMatrixToSequence(
          AlignmentI pdbAlignment, InputStream pae_input, int index,
          String seqId, String label) throws IOException, ParseException
  {
    SequenceI sequence = null;
    if (seqId == null)
    {
      int seqToGet = index > 0 ? index : 0;
      sequence = pdbAlignment.getSequenceAt(seqToGet);
    }
    if (sequence == null)
    {
      SequenceI[] sequences = pdbAlignment.findSequenceMatch(seqId);
      if (sequences == null || sequences.length < 1)
      {
        Console.warn("Could not find sequence with id '" + seqId
                + "' to attach pAE matrix to. Ignoring matrix.");
        return false;
      }
      else
      {
        sequence = sequences[0]; // just use the first sequence with this seqId
      }
    }
    if (sequence == null)
    {
      return false;
    }
    return importPaeJSONAsContactMatrixToSequence(pdbAlignment, pae_input,
            sequence, label);
  }

  public static boolean importPaeJSONAsContactMatrixToSequence(
          AlignmentI pdbAlignment, InputStream pae_input,
          SequenceI sequence, String label)
          throws IOException, ParseException
  {
    JSONObject paeDict = parseJSONtoPAEContactMatrix(pae_input);
    if (paeDict == null)
    {
      Console.debug("JSON file did not parse properly.");
      return false;
    }
    ContactMatrixI matrix = new PAEContactMatrix(sequence,
            (Map<String, Object>) paeDict);

    AlignmentAnnotation cmannot = sequence.addContactList(matrix);
    if (label != null)
      cmannot.label = label;
    pdbAlignment.addAnnotation(cmannot);

    return true;
  }

  public static JSONObject parseJSONtoPAEContactMatrix(
          InputStream pae_input) throws IOException, ParseException
  {
    Object paeJson = Platform.parseJSON(pae_input);
    JSONObject paeDict = null;
    if (paeJson instanceof JSONObject)
    {
      paeDict = (JSONObject) paeJson;
    }
    else if (paeJson instanceof JSONArray)
    {
      JSONArray jsonArray = (JSONArray) paeJson;
      if (jsonArray.size() > 0)
        paeDict = (JSONObject) jsonArray.get(0);
    }

    return paeDict;
  }

  // ###### TEST THIS
  public static boolean importPaeJSONAsContactMatrixToStructure(
          StructureMapping[] smArray, InputStream paeInput, String label)
          throws IOException, ParseException
  {
    boolean someDone = false;
    for (StructureMapping sm : smArray)
    {
      boolean thisDone = importPaeJSONAsContactMatrixToStructure(sm,
              paeInput, label);
      someDone |= thisDone;
    }
    return someDone;
  }

  public static boolean importPaeJSONAsContactMatrixToStructure(
          StructureMapping sm, InputStream paeInput, String label)
          throws IOException, ParseException
  {
    JSONObject pae_obj = parseJSONtoPAEContactMatrix(paeInput);
    if (pae_obj == null)
    {
      Console.debug("JSON file did not parse properly.");
      return false;
    }

    SequenceI seq = sm.getSequence();
    ContactMatrixI matrix = new PAEContactMatrix(seq,
            (Map<String, Object>) pae_obj);
    AlignmentAnnotation cmannot = sm.getSequence().addContactList(matrix);
    /* this already happens in Sequence.addContactList()
     seq.addAlignmentAnnotation(cmannot);
     */
    return true;
  }

  /**
   * general purpose structure importer - designed to yield alignment useful for
   * transfer of annotation to associated sequences
   * 
   * @param alphaFoldCif
   * @param tmpFile
   * @param id
   * @param chain
   * @param dbSource
   * @param dbVersion
   * @return
   * @throws Exception
   */
  public static AlignmentI importDownloadedStructureFromUrl(
          String alphaFoldCif, File tmpFile, String id, String chain,
          String dbSource, String dbVersion) throws Exception
  {
    String file = tmpFile.getAbsolutePath();
    // todo get rid of Type and use FileFormatI instead?
    FileFormatI fileFormat = FileFormat.MMCif;
    TFType tempfacType = TFType.PLDDT;
    AlignmentI pdbAlignment = new FormatAdapter().readFile(tmpFile, file,
            DataSourceType.FILE, fileFormat, tempfacType);

    if (pdbAlignment != null)
    {
      List<SequenceI> toremove = new ArrayList<SequenceI>();
      for (SequenceI pdbcs : pdbAlignment.getSequences())
      {
        String chid = null;
        // Mapping map=null;
        for (PDBEntry pid : pdbcs.getAllPDBEntries())
        {
          if (pid.getFile() == file)
          {
            chid = pid.getChainCode();
          }
        }
        if (chain == null || (chid != null && (chid.equals(chain)
                || chid.trim().equals(chain.trim())
                || (chain.trim().length() == 0 && chid.equals("_")))))
        {
          // FIXME seems to result in 'PDB|1QIP|1qip|A' - 1QIP is redundant.
          // TODO: suggest simplify naming to 1qip|A as default name defined
          pdbcs.setName(id + SEPARATOR + pdbcs.getName());
          // Might need to add more metadata to the PDBEntry object
          // like below
          /*
           * PDBEntry entry = new PDBEntry(); // Construct the PDBEntry
           * entry.setId(id); if (entry.getProperty() == null)
           * entry.setProperty(new Hashtable());
           * entry.getProperty().put("chains", pdbchain.id + "=" +
           * sq.getStart() + "-" + sq.getEnd());
           * sq.getDatasetSequence().addPDBId(entry);
           */
          // Add PDB DB Refs
          // We make a DBRefEtntry because we have obtained the PDB file from
          // a
          // verifiable source
          // JBPNote - PDB DBRefEntry should also carry the chain and mapping
          // information
          if (dbSource != null)
          {
            DBRefEntry dbentry = new DBRefEntry(dbSource,

                    dbVersion, (chid == null ? id : id + chid));
            // dbentry.setMap()
            pdbcs.addDBRef(dbentry);
            // update any feature groups
            List<SequenceFeature> allsf = pdbcs.getFeatures()
                    .getAllFeatures();
            List<SequenceFeature> newsf = new ArrayList<SequenceFeature>();
            if (allsf != null && allsf.size() > 0)
            {
              for (SequenceFeature f : allsf)
              {
                if (file.equals(f.getFeatureGroup()))
                {
                  f = new SequenceFeature(f, f.type, f.begin, f.end, id,
                          f.score);
                }
                newsf.add(f);
              }
              pdbcs.setSequenceFeatures(newsf);
            }
          }
        }
        else
        {
          // mark this sequence to be removed from the alignment
          // - since it's not from the right chain
          toremove.add(pdbcs);
        }
      }
      // now remove marked sequences
      for (SequenceI pdbcs : toremove)
      {
        pdbAlignment.deleteSequence(pdbcs);
        if (pdbcs.getAnnotation() != null)
        {
          for (AlignmentAnnotation aa : pdbcs.getAnnotation())
          {
            pdbAlignment.deleteAnnotation(aa);
          }
        }
      }
    }
    return pdbAlignment;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  @Override
  public boolean isValidReference(String accession)
  {
    Regex r = getAccessionValidator();
    return r.search(accession.trim());
  }

  /**
   * human glyoxalase
   */
  @Override
  public String getTestQuery()
  {
    return "AF-O15552-F1";
  }

  @Override
  public String getDbName()
  {
    return "ALPHAFOLD"; // getDbSource();
  }

  @Override
  public int getTier()
  {
    return 0;
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

  // days * 86400000
  private static final long PAE_CACHE_STALE_TIME = 1 * 86400000;

  private static Map<String, File> paeDownloadCache = new HashMap<>();

}
