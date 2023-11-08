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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.stevesoft.pat.Regex;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.util.StringUtils;
import jalview.ws.seqfetcher.DbSourceProxyImpl;
import jalview.xml.binding.uniprot.DbReferenceType;
import jalview.xml.binding.uniprot.Entry;
import jalview.xml.binding.uniprot.FeatureType;
import jalview.xml.binding.uniprot.LocationType;
import jalview.xml.binding.uniprot.PositionType;
import jalview.xml.binding.uniprot.PropertyType;

/**
 * This class queries the Uniprot database for sequence data, unmarshals the
 * returned XML, and converts it to Jalview Sequence records (including attached
 * database references and sequence features)
 * 
 * @author JimP
 * 
 */
public class Uniprot extends DbSourceProxyImpl
{
  private static final String DEFAULT_UNIPROT_DOMAIN = "https://www.uniprot.org";

  private static final String BAR_DELIMITER = "|";

  /**
   * Constructor
   */
  public Uniprot()
  {
    super();
  }

  private String getDomain()
  {
    return Cache.getDefault("UNIPROT_DOMAIN", DEFAULT_UNIPROT_DOMAIN);
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
    return new Regex("([A-Z]+[0-9]+[A-Z0-9]+|[A-Z0-9]+_[A-Z0-9]+)");
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource()
   */
  @Override
  public String getDbSource()
  {
    return DBRefSource.UNIPROT;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    return "0"; // we really don't know what version we're on.
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getSequenceRecords(java.lang.String[])
   */
  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    startQuery();
    try
    {
      queries = queries.toUpperCase(Locale.ROOT).replaceAll(
              "(UNIPROT\\|?|UNIPROT_|UNIREF\\d+_|UNIREF\\d+\\|?)", "");
      AlignmentI al = null;

      String downloadstring = getDomain() + "/uniprot/" + queries + ".xml";

      URL url = new URL(downloadstring);
      HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
      // anything other than 200 means we don't have data
      // TODO: JAL-3882 reuse the EnsemblRestClient's fair
      // use/backoff logic to retry when the server tells us to go away
      if (urlconn.getResponseCode() == 200)
      {
        InputStream istr = urlconn.getInputStream();
        List<Entry> entries = getUniprotEntries(istr);
        if (entries != null)
        {
          List<SequenceI> seqs = new ArrayList<>();
          for (Entry entry : entries)
          {
            seqs.add(uniprotEntryToSequence(entry));
          }
          al = new Alignment(seqs.toArray(new SequenceI[seqs.size()]));
        }
      }
      stopQuery();
      return al;

    } catch (Exception e)
    {
      throw (e);
    } finally
    {
      stopQuery();
    }
  }

  /**
   * Converts an Entry object (bound from Uniprot XML) to a Jalview Sequence
   * 
   * @param entry
   * @return
   */
  SequenceI uniprotEntryToSequence(Entry entry)
  {
    String id = getUniprotEntryId(entry);
    /*
     * Sequence should not include any whitespace, but JAXB leaves these in
     */
    String seqString = entry.getSequence().getValue().replaceAll("\\s*",
            "");

    SequenceI sequence = new Sequence(id, seqString);
    sequence.setDescription(getUniprotEntryDescription(entry));
    final String uniprotRecordVersion = "" + entry.getVersion();
    /*
     * add a 'self' DBRefEntry for each accession
     */
    final String dbVersion = getDbVersion();
    List<DBRefEntry> dbRefs = new ArrayList<>();
    boolean canonical = true;
    for (String accessionId : entry.getAccession())
    {
      DBRefEntry dbRef = new DBRefEntry(DBRefSource.UNIPROT,
              uniprotRecordVersion, accessionId, null, canonical);
      canonical = false;
      dbRefs.add(dbRef);
    }

    /*
     * add a DBRefEntry for each dbReference element in the XML;
     * also add a PDBEntry if type="PDB";
     * also add an EMBLCDS dbref if protein sequence id is given
     * also add an Ensembl dbref " " " " " "
     */
    Vector<PDBEntry> pdbRefs = new Vector<>();
    for (DbReferenceType dbref : entry.getDbReference())
    {
      String type = dbref.getType();
      DBRefEntry dbr = new DBRefEntry(type,
              DBRefSource.UNIPROT + ":" + dbVersion, dbref.getId());
      dbRefs.add(dbr);
      if ("PDB".equals(type))
      {
        pdbRefs.add(new PDBEntry(dbr));
      }
      if ("EMBL".equals(type))
      {
        /*
         * e.g. Uniprot accession Q9BXM7 has
         * <dbReference type="EMBL" id="M19359">
         *   <property type="protein sequence ID" value="AAA40981.1"/>
         *   <property type="molecule type" value="Genomic_DNA"/>
         * </dbReference> 
         */
        String cdsId = getProperty(dbref.getProperty(),
                "protein sequence ID");
        if (cdsId != null && cdsId.trim().length() > 0)
        {
          // remove version
          String[] vrs = cdsId.split("\\.");
          String version = vrs.length > 1 ? vrs[1]
                  : DBRefSource.UNIPROT + ":" + uniprotRecordVersion;
          dbr = new DBRefEntry(DBRefSource.EMBLCDS, version, vrs[0]);
          // TODO: process VARIANT features to allow EMBLCDS record's product to
          // match Uniprot
          dbr.setCanonical(true);
          dbRefs.add(dbr);
        }
      }
      if (type != null
              && type.toLowerCase(Locale.ROOT).startsWith("ensembl"))
      {
        // remove version
        String[] vrs = dbref.getId().split("\\.");
        String version = vrs.length > 1 ? vrs[1]
                : DBRefSource.UNIPROT + ":" + uniprotRecordVersion;
        dbr.setAccessionId(vrs[0]);
        dbr.setVersion(version);
        /*
         * e.g. Uniprot accession Q9BXM7 has
         * <dbReference type="Ensembl" id="ENST00000321556">
         *   <molecule id="Q9BXM7-1"/>
         *   <property type="protein sequence ID" value="ENSP00000364204"/>
         *   <property type="gene ID" value="ENSG00000158828"/>
         * </dbReference> 
         */
        String cdsId = getProperty(dbref.getProperty(),
                "protein sequence ID");
        if (cdsId != null && cdsId.trim().length() > 0)
        {
          // remove version
          String[] cdsVrs = cdsId.split("\\.");
          String cdsVersion = cdsVrs.length > 1 ? cdsVrs[1]
                  : DBRefSource.UNIPROT + ":" + uniprotRecordVersion;
          dbr = new DBRefEntry(DBRefSource.ENSEMBL,
                  DBRefSource.UNIPROT + ":" + cdsVersion, cdsVrs[0]);
          dbRefs.add(dbr);
        }
      }
    }

    /*
     * create features; they have either begin and end, or position, in XML
     */
    sequence.setPDBId(pdbRefs);
    if (entry.getFeature() != null)
    {
      for (FeatureType uf : entry.getFeature())
      {
        LocationType location = uf.getLocation();
        int start = 0;
        int end = 0;
        if (location.getPosition() != null)
        {
          start = location.getPosition().getPosition().intValue();
          end = start;
        }
        else
        {
          start = location.getBegin().getPosition().intValue();
          end = location.getEnd().getPosition().intValue();
        }
        SequenceFeature sf = new SequenceFeature(uf.getType(),
                getDescription(uf), start, end, "Uniprot");
        sf.setStatus(uf.getStatus());
        sequence.addSequenceFeature(sf);
      }
    }
    for (DBRefEntry dbr : dbRefs)
    {
      sequence.addDBRef(dbr);
    }
    return sequence;
  }

  /**
   * A helper method that builds a sequence feature description
   * 
   * @param feature
   * @return
   */
  static String getDescription(FeatureType feature)
  {
    String orig = feature.getOriginal();
    List<String> variants = feature.getVariation();
    StringBuilder sb = new StringBuilder();

    /*
     * append variant in standard format if present
     * e.g. p.Arg59Lys
     * multiple variants are split over lines using <br>
     */
    boolean asHtml = false;
    if (orig != null && !orig.isEmpty() && variants != null
            && !variants.isEmpty())
    {
      int p = 0;
      for (String var : variants)
      {
        // TODO proper HGVS nomenclature for delins structural variations
        // http://varnomen.hgvs.org/recommendations/protein/variant/delins/
        // for now we are pragmatic - any orig/variant sequence longer than
        // three characters is shown with single-character notation rather than
        // three-letter notation
        sb.append("p.");
        if (orig.length() < 4)
        {
          for (int c = 0, clen = orig.length(); c < clen; c++)
          {
            char origchar = orig.charAt(c);
            String orig3 = ResidueProperties.aa2Triplet.get("" + origchar);
            sb.append(orig3 == null ? origchar
                    : StringUtils.toSentenceCase(orig3));
          }
        }
        else
        {
          sb.append(orig);
        }

        LocationType location = feature.getLocation();
        PositionType start = location.getPosition() == null
                ? location.getBegin()
                : location.getPosition();
        sb.append(Integer.toString(start.getPosition().intValue()));

        if (var.length() < 4)
        {
          for (int c = 0, clen = var.length(); c < clen; c++)
          {
            char varchar = var.charAt(c);
            String var3 = ResidueProperties.aa2Triplet.get("" + varchar);

            sb.append(var3 != null ? StringUtils.toSentenceCase(var3)
                    : "" + varchar);
          }
        }
        else
        {
          sb.append(var);
        }
        if (++p != variants.size())
        {
          sb.append("<br/>&nbsp;&nbsp;");
          asHtml = true;
        }
        else
        {
          sb.append(" ");
        }
      }
    }
    String description = feature.getDescription();
    if (description != null)
    {
      sb.append(description);
    }
    if (asHtml)
    {
      sb.insert(0, "<html>");
      sb.append("</html>");
    }

    return sb.toString();
  }

  /**
   * A helper method that searches the list of properties for one with the given
   * key, and if found returns the property value, else returns null
   * 
   * @param properties
   * @param key
   * @return
   */
  static String getProperty(List<PropertyType> properties, String key)
  {
    String value = null;
    if (properties != null)
    {
      for (PropertyType prop : properties)
      {
        if (key.equals(prop.getType()))
        {
          value = prop.getValue();
          break;
        }
      }
    }
    return value;
  }

  /**
   * Extracts xml element entry/protein/recommendedName/fullName
   * 
   * @param entry
   * @return
   */
  static String getUniprotEntryDescription(Entry entry)
  {
    String desc = "";
    if (entry.getProtein() != null
            && entry.getProtein().getRecommendedName() != null)
    {
      // fullName is mandatory if recommendedName is present
      desc = entry.getProtein().getRecommendedName().getFullName()
              .getValue();
    }
    return desc;
  }

  /**
   * Constructs a sequence id by concatenating all entry/name elements with '|'
   * separator
   * 
   * @param entry
   * @return
   */
  static String getUniprotEntryId(Entry entry)
  {
    StringBuilder name = new StringBuilder(32);
    for (String n : entry.getName())
    {
      if (name.length() > 0)
      {
        name.append(BAR_DELIMITER);
      }
      name.append(n);
    }
    return name.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  @Override
  public boolean isValidReference(String accession)
  {
    // TODO: make the following a standard validator
    return (accession == null || accession.length() < 2) ? false
            : getAccessionValidator().search(accession);
  }

  /**
   * return LDHA_CHICK uniprot entry
   */
  @Override
  public String getTestQuery()
  {
    return "P00340";
  }

  @Override
  public String getDbName()
  {
    return "Uniprot"; // getDbSource();
  }

  @Override
  public int getTier()
  {
    return 0;
  }

  /**
   * Reads the reply to the EBI Fetch Uniprot data query, unmarshals it to an
   * Uniprot object, and returns the enclosed Entry objects, or null on any
   * failure
   * 
   * @param is
   * @return
   */
  public List<Entry> getUniprotEntries(InputStream is)
  {
    List<Entry> entries = null;
    try
    {
      JAXBContext jc = JAXBContext
              .newInstance("jalview.xml.binding.uniprot");
      XMLStreamReader streamReader = XMLInputFactory.newInstance()
              .createXMLStreamReader(is);
      javax.xml.bind.Unmarshaller um = jc.createUnmarshaller();
      JAXBElement<jalview.xml.binding.uniprot.Uniprot> uniprotElement = um
              .unmarshal(streamReader,
                      jalview.xml.binding.uniprot.Uniprot.class);
      jalview.xml.binding.uniprot.Uniprot uniprot = uniprotElement
              .getValue();

      if (uniprot != null && !uniprot.getEntry().isEmpty())
      {
        entries = uniprot.getEntry();
      }
    } catch (JAXBException | XMLStreamException
            | FactoryConfigurationError e)
    {
      if (e instanceof javax.xml.bind.UnmarshalException
              && e.getCause() != null
              && e.getCause() instanceof XMLStreamException
              && e.getCause().getMessage().contains("[row,col]:[1,1]"))
      {
        // trying to parse an empty stream
        return null;
      }
      e.printStackTrace();
    }
    return entries;
  }
}
