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
package jalview.datamodel;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import jalview.io.StructureFile;
import jalview.util.CaseInsensitiveString;

public class PDBEntry
{

  /**
   * constant for storing chain code in properties table
   */
  private static final String CHAIN_ID = "chain_code";

  private Hashtable<String, Object> properties;

  private static final int PDB_ID_LENGTH = 4;

  /**
   * property set when id is a 'manufactured' identifier from the structure
   * data's filename
   */
  private static final String FAKED_ID = "faked_pdbid";

  /**
   * property set when the id is authoritative, and should be used in preference
   * to any identifiers in the structure data
   */
  private static final String AUTHORITATIVE_ID = "authoritative_pdbid";

  private String file;

  private String type;

  private String id;

  private StructureFile sf = null;

  public enum Type
  {
    // TODO is FILE needed; if not is this enum needed, or can we
    // use FileFormatI for PDB, MMCIF?
    PDB("pdb", "pdb"), MMCIF("mmcif", "cif"), BCIF("bcif", "bcif"),
    FILE("?", "?");

    /*
     * file extension for cached structure file; must be one that
     * is recognised by Chimera 'open' command
     * @see https://www.cgl.ucsf.edu/chimera/current/docs/UsersGuide/filetypes.html
     */
    String ext;

    /*
     * format specifier used in dbfetch request
     * @see http://www.ebi.ac.uk/Tools/dbfetch/dbfetch/dbfetch.databases#pdb
     */
    String format;

    private Type(String fmt, String ex)
    {
      format = fmt;
      ext = ex;
    }

    public String getFormat()
    {
      return format;
    }

    public String getExtension()
    {
      return ext;
    }

    /**
     * case insensitive matching for Type enum
     * 
     * @param value
     * @return
     */
    public static Type getType(String value)
    {
      for (Type t : Type.values())
      {
        if (t.toString().equalsIgnoreCase(value))
        {
          return t;
        }
      }
      return null;
    }

    /**
     * case insensitive equivalence for strings resolving to PDBEntry type
     * 
     * @param t
     * @return
     */
    public boolean matches(String t)
    {
      return (this.toString().equalsIgnoreCase(t));
    }
  }

  /**
   * Answers true if obj is a PDBEntry with the same id and chain code (both
   * ignoring case), file, type and properties
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof PDBEntry))
    {
      return false;
    }
    if (obj == this)
    {
      return true;
    }
    PDBEntry o = (PDBEntry) obj;

    /*
     * note that chain code is stored as a property wrapped by a 
     * CaseInsensitiveString, so we are in effect doing a 
     * case-insensitive comparison of chain codes
     */
    boolean idMatches = id == o.id
            || (id != null && id.equalsIgnoreCase(o.id));
    boolean fileMatches = file == o.file
            || (file != null && file.equals(o.file));
    boolean typeMatches = type == o.type
            || (type != null && type.equals(o.type));
    if (idMatches && fileMatches && typeMatches)
    {
      return properties == o.properties
              || (properties != null && properties.equals(o.properties));
    }
    return false;
  }

  /**
   * Default constructor
   */
  public PDBEntry()
  {
  }

  public PDBEntry(String pdbId, String chain, PDBEntry.Type type,
          String filePath)
  {
    init(pdbId, chain, type, filePath);
  }

  /**
   * @param pdbId
   * @param chain
   * @param entryType
   * @param filePath
   */
  void init(String pdbId, String chain, PDBEntry.Type entryType,
          String filePath)
  {
    this.id = pdbId;
    this.type = entryType == null ? null : entryType.toString();
    this.file = filePath;
    setChainCode(chain);
  }

  /**
   * Copy constructor.
   * 
   * @param entry
   */
  public PDBEntry(PDBEntry entry)
  {
    file = entry.file;
    type = entry.type;
    id = entry.id;
    if (entry.properties != null)
    {
      properties = (Hashtable<String, Object>) entry.properties.clone();
    }
  }

  /**
   * Make a PDBEntry from a DBRefEntry. The accession code is used for the PDB
   * id, but if it is 5 characters in length, the last character is removed and
   * set as the chain code instead.
   * 
   * @param dbr
   */
  public PDBEntry(DBRefEntry dbr)
  {
    if (!DBRefSource.PDB.equals(dbr.getSource()))
    {
      throw new IllegalArgumentException(
              "Invalid source: " + dbr.getSource());
    }

    String pdbId = dbr.getAccessionId();
    String chainCode = null;
    if (pdbId.length() == PDB_ID_LENGTH + 1)
    {
      char chain = pdbId.charAt(PDB_ID_LENGTH);
      if (('a' <= chain && chain <= 'z') || ('A' <= chain && chain <= 'Z'))
      {
        pdbId = pdbId.substring(0, PDB_ID_LENGTH);
        chainCode = String.valueOf(chain);
      }
    }
    init(pdbId, chainCode, null, null);
  }

  public void setFile(String f)
  {
    this.file = f;
  }

  public String getFile()
  {
    return file;
  }

  public void setType(String t)
  {
    this.type = t;
  }

  public void setType(PDBEntry.Type type)
  {
    this.type = type == null ? null : type.toString();
  }

  public String getType()
  {
    return type;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setProperty(String key, Object value)
  {
    if (this.properties == null)
    {
      this.properties = new Hashtable<String, Object>();
    }
    properties.put(key, value);
  }

  public Object getProperty(String key)
  {
    return properties == null ? null : properties.get(key);
  }

  /**
   * Returns an enumeration of the keys of this object's properties (or an empty
   * enumeration if it has no properties)
   * 
   * @return
   */
  public Enumeration<String> getProperties()
  {
    if (properties == null)
    {
      return Collections.emptyEnumeration();
    }
    return properties.keys();
  }

  /**
   * 
   * @return null or a string for associated chain IDs
   */
  public String getChainCode()
  {
    return (properties == null || properties.get(CHAIN_ID) == null) ? null
            : properties.get(CHAIN_ID).toString();
  }

  /**
   * Sets a non-case-sensitive property for the given chain code. Two PDBEntry
   * objects which differ only in the case of their chain code are considered
   * equal. This avoids duplication of objects in lists of PDB ids.
   * 
   * @param chainCode
   */
  public void setChainCode(String chainCode)
  {
    if (chainCode == null)
    {
      deleteProperty(CHAIN_ID);
    }
    else
    {
      setProperty(CHAIN_ID, new CaseInsensitiveString(chainCode));
    }
  }

  /**
   * Deletes the property with the given key, and returns the deleted value (or
   * null)
   */
  Object deleteProperty(String key)
  {
    Object result = null;
    if (properties != null)
    {
      result = properties.remove(key);
    }
    return result;
  }

  @Override
  public String toString()
  {
    return id;
  }

  /**
   * Getter provided for Castor binding only. Application code should call
   * getProperty() or getProperties() instead.
   * 
   * @deprecated
   * @see #getProperty(String)
   * @see #getProperties()
   * @see jalview.ws.dbsources.Uniprot#getUniprotEntries
   * @return
   */
  @Deprecated
  public Hashtable<String, Object> getProps()
  {
    return properties;
  }

  /**
   * Setter provided for Castor binding only. Application code should call
   * setProperty() instead.
   * 
   * @deprecated
   * @return
   */
  @Deprecated
  public void setProps(Hashtable<String, Object> props)
  {
    properties = props;
  }

  /**
   * Answers true if this object is either equivalent to, or can be 'improved'
   * by, the given entry.
   * <p>
   * If newEntry has the same id (ignoring case), and doesn't have a conflicting
   * file spec or chain code, then update this entry from its file and/or chain
   * code.
   * 
   * @param newEntry
   * @return true if modifications were made
   */
  public boolean updateFrom(PDBEntry newEntry)
  {
    if (this.equals(newEntry))
    {
      return true;
    }

    String newId = newEntry.getId();
    if (newId == null || getId() == null)
    {
      return false; // shouldn't happen
    }

    boolean idMatches = getId().equalsIgnoreCase(newId);

    /*
     * Don't update if associated with different structure files
     */
    String newFile = newEntry.getFile();
    if (newFile != null && getFile() != null)
    {
      if (!newFile.equals(getFile()))
      {
        return false;
      }
      else
      {
        // files match.
        if (!idMatches)
        {
          // this shouldn't happen, but could do if the id from the
          // file is not the same as the id from the authority that provided
          // the file
          if (!newEntry.fakedPDBId() && !isAuthoritative())
          {
            return false;
          } // otherwise we can update
        }
      }
    }
    else
    {
      // one has data, one doesn't ..
      if (!idMatches)
      {
        return false;
      } // otherwise maybe can update
    }

    /*
     * Don't update if associated with different chains (ignoring case)
     */
    String newChain = newEntry.getChainCode();
    if (newChain != null && newChain.length() > 0 && getChainCode() != null
            && getChainCode().length() > 0
            && !getChainCode().equalsIgnoreCase(newChain))
    {
      return false;
    }

    /*
     * set file path if not already set
     */
    String newType = newEntry.getType();
    if (getFile() == null && newFile != null)
    {
      setFile(newFile);
      setType(newType);
    }

    /*
     * set file type if new entry has it and we don't
     * (for the case where file was not updated)
     */
    if (getType() == null && newType != null)
    {
      setType(newType);
    }

    /*
     * set chain if not already set (we excluded differing 
     * chains earlier) (ignoring case change only)
     */
    if (newChain != null && newChain.length() > 0
            && !newChain.equalsIgnoreCase(getChainCode()))
    {
      setChainCode(newChain);
    }

    /*
     * copy any new or modified properties
     */
    Enumeration<String> newProps = newEntry.getProperties();
    while (newProps.hasMoreElements())
    {
      /*
       * copy properties unless value matches; this defends against changing
       * the case of chain_code which is wrapped in a CaseInsensitiveString
       */
      String key = newProps.nextElement();
      Object value = newEntry.getProperty(key);
      if (FAKED_ID.equals(key) || AUTHORITATIVE_ID.equals(key))
      {
        // we never update the fake ID property
        continue;
      }
      if (!value.equals(getProperty(key)))
      {
        setProperty(key, value);
      }
    }
    return true;
  }

  public void setAuthoritative(boolean isAuthoritative)
  {
    setProperty(AUTHORITATIVE_ID, Boolean.valueOf(isAuthoritative));
  }

  /**
   * 
   * @return true if the identifier should be preferred over any identifiers
   *         embedded in the structure data
   */
  public boolean isAuthoritative()
  {
    if (_hasProperty(AUTHORITATIVE_ID))
    {
      return ((Boolean) getProperty(AUTHORITATIVE_ID));
    }
    return false;
  }

  /**
   * set when Jalview has manufactured the ID using a local filename
   * 
   * @return
   */
  public boolean fakedPDBId()
  {
    if (_hasProperty(FAKED_ID))
    {
      return true;
    }
    return false;
  }

  public void setFakedPDBId(boolean faked)
  {
    if (faked)
    {
      setProperty(FAKED_ID, Boolean.TRUE);
    }
    else
    {
      if (properties != null)
      {
        properties.remove(FAKED_ID);
      }
    }
  }

  private boolean _hasProperty(final String key)
  {
    return (properties != null && properties.containsKey(key));
  }

  private static final String RETRIEVE_FROM = "RETRIEVE_FROM";

  private static final String PROVIDER = "PROVIDER";

  private static final String MODELPAGE = "PROVIDERPAGE";

  /**
   * Permanent URI for retrieving the original structure data
   * 
   * @param urlStr
   */
  public void setRetrievalUrl(String urlStr)
  {
    setProperty(RETRIEVE_FROM, urlStr);
  }

  public boolean hasRetrievalUrl()
  {
    return _hasProperty(RETRIEVE_FROM);
  }

  /**
   * get the Permanent URI for retrieving the original structure data
   */
  public String getRetrievalUrl()
  {
    return (String) getProperty(RETRIEVE_FROM);
  }

  /**
   * Data provider name - from 3D Beacons
   * 
   * @param provider
   */
  public void setProvider(String provider)
  {
    setProperty(PROVIDER, provider);
  }

  /**
   * Get Data provider name - from 3D Beacons
   * 
   */
  public String getProvider()
  {
    return (String) getProperty(PROVIDER);
  }

  /**
   * Permanent URI for retrieving the original structure data
   * 
   * @param urlStr
   */
  public void setProviderPage(String urlStr)
  {
    setProperty(MODELPAGE, urlStr);
  }

  /**
   * get the Permanent URI for retrieving the original structure data
   */
  public String getProviderPage()
  {
    return (String) getProperty(MODELPAGE);
  }

  public boolean hasProviderPage()
  {
    return _hasProperty(MODELPAGE);
  }

  public boolean hasProvider()
  {
    return _hasProperty(PROVIDER);
  }

  public StructureFile getStructureFile()
  {
    return sf;
  }

  public void setStructureFile(StructureFile f)
  {
    sf = f;
  }

  public boolean hasStructureFile()
  {
    return sf != null && sf.inFile != null && sf.inFile.exists();
  }
}
