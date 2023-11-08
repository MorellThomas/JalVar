
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

import jalview.api.FeatureSettingsModelI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.PDBEntry.Type;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;
import jalview.io.PDBFeatureSettings;
import jalview.structure.StructureImportSettings;
import jalview.util.MessageManager;
import jalview.ws.ebi.EBIFetchClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.stevesoft.pat.Regex;

/**
 * @author JimP
 * 
 */
public class Pdb extends EbiFileRetrievedProxy
{
  private static final String SEPARATOR = "|";

  private static final String COLON = ":";

  private static final int PDB_ID_LENGTH = 4;

  public Pdb()
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
    return new Regex("([1-9][0-9A-Za-z]{3}):?([ _A-Za-z0-9]?)");
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource()
   */
  @Override
  public String getDbSource()
  {
    return DBRefSource.PDB;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    return "0";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getSequenceRecords(java.lang.String[])
   */
  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
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

    /*
     * extract chain code if it is appended to the id and we
     * don't already have one
     */
    if (queries.length() > PDB_ID_LENGTH && chain == null)
    {
      chain = queries.substring(PDB_ID_LENGTH, PDB_ID_LENGTH + 1);
      id = queries.substring(0, PDB_ID_LENGTH);
    }

    if (!isValidReference(id))
    {
      System.err.println("Ignoring invalid pdb query: '" + id + "'");
      stopQuery();
      return null;
    }

    /*
     * ensure that an mmCIF format structure file is saved with extension.cif,
     * because the Chimera "open" command recognises this extension
     */
    Type pdbFileFormat = StructureImportSettings
            .getDefaultStructureFileFormat();
    String ext = pdbFileFormat.getExtension();
    String fetchFormat = pdbFileFormat.getFormat();

    EBIFetchClient ebi = new EBIFetchClient();
    File tmpFile = ebi.fetchDataAsFile("pdb:" + id, fetchFormat, ext);
    file = tmpFile.getAbsolutePath();
    stopQuery();
    if (file == null)
    {
      return null;
    }
    try
    {
      // todo get rid of Type and use FileFormatI instead?
      FileFormatI fileFormat = (pdbFileFormat == Type.PDB) ? FileFormat.PDB
              : FileFormat.MMCif;
      pdbAlignment = new FormatAdapter().readFile(tmpFile,
              DataSourceType.FILE, fileFormat);
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
            pdbcs.setName(jalview.datamodel.DBRefSource.PDB + SEPARATOR + id
                    + SEPARATOR + pdbcs.getName());
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
            DBRefEntry dbentry = new DBRefEntry(getDbSource(),
                    getDbVersion(), (chid == null ? id : id + chid));
            // dbentry.setMap()
            pdbcs.addDBRef(dbentry);
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

      if (pdbAlignment == null || pdbAlignment.getHeight() < 1)
      {
        throw new Exception(MessageManager.formatMessage(
                "exception.no_pdb_records_for_chain", new String[]
                { id, ((chain == null) ? "' '" : chain) }));
      }

    } catch (Exception ex) // Problem parsing PDB file
    {
      stopQuery();
      throw (ex);
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
    return "1QIP";
  }

  @Override
  public String getDbName()
  {
    return "PDB"; // getDbSource();
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
}
