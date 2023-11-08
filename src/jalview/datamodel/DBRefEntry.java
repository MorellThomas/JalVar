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

import java.util.Locale;

import jalview.api.DBRefEntryI;
import jalview.util.DBRefUtils;
import jalview.util.MapList;

import java.util.List;

public class DBRefEntry implements DBRefEntryI
{
  String source = "";

  private String version = "";

  private String ucversion;

  private String accessionId = "";

  int sourceKey = Integer.MIN_VALUE;

  String canonicalSourceName;

  boolean isCanonicalAccession = false;

  /*
   * maps from associated sequence to the database sequence's coordinate system
   */
  Mapping map = null;

  public DBRefEntry()
  {

  }

  /**
   * 
   * @param source
   *          may not be null
   * @param version
   *          may be null
   * @param accessionId
   *          may be null
   */
  public DBRefEntry(String source, String version, String accessionId)
  {
    this(source, version, accessionId, null, false);
  }

  /**
   * 
   * @param source
   *          may not be null
   * @param version
   *          may be null
   * @param accessionId
   *          may be null
   */
  public DBRefEntry(String source, String version, String accessionId,
          Mapping map)
  {
    this(source, version, accessionId, map, false);
  }

  /**
   * 
   * @param source
   *          canonical source (turned to uppercase; cannot be null)
   * @param version
   *          (source dependent version string or null)
   * @param accessionId
   *          (source dependent accession number string or null)
   * @param map
   *          (mapping from local sequence numbering to source accession
   *          numbering or null)
   */
  public DBRefEntry(String source, String version, String accessionId,
          Mapping map, boolean isCanonical)
  {

    this.source = source.toUpperCase(Locale.ROOT);
    setVersion(version);
    this.accessionId = accessionId;
    this.map = map;
    this.isCanonicalAccession = isCanonical;
  }

  /**
   * Clone an entry, this time not allowing any null fields except map.
   * 
   */
  public DBRefEntry(DBRefEntryI entry)
  {
    this((entry.getSource() == null ? "" : new String(entry.getSource())),
            (entry.getVersion() == null ? ""
                    : new String(entry.getVersion())),
            (entry.getAccessionId() == null ? ""
                    : new String(entry.getAccessionId())),
            (entry.getMap() == null ? null : new Mapping(entry.getMap())),
            entry.isCanonical());
  }

  @Override
  public boolean equals(Object o)
  {
    // TODO should also override hashCode to ensure equal objects have equal
    // hashcodes

    // if (o == null || !(o instanceof DBRefEntry))
    // {
    // return false;
    // }
    // DBRefEntry entry = (DBRefEntry) o;
    // if (entry == this)
    // {
    // return true;
    // }
    Mapping em;
    return (o != null && o instanceof DBRefEntry && (o == this || equalRef(
            (DBRefEntry) o)
            && (map == null) == ((em = ((DBRefEntry) o).map) == null)
            && (map == null || map.equals(em))));
    //
    // {
    // return true;
    // }
    // return false;
  }

  /**
   * Answers true if this object is either equivalent to, or can be 'improved'
   * by, the given entry. Specifically, answers true if
   * <ul>
   * <li>source and accession are identical (ignoring case)</li>
   * <li>version is identical (ignoring case), or this version is of the format
   * "someSource:0", in which case the version for the other entry replaces
   * it</li>
   * <li>mappings are not compared but if this entry has no mapping, replace
   * with that for the other entry</li>
   * </ul>
   * 
   * @param other
   * @return
   */
  @Override
  public boolean updateFrom(DBRefEntryI other)
  {
    if (other == null)
    {
      return false;
    }
    if (other == this)
    {
      return true;
    }

    boolean improved = false;
    /*
     * source must either match or be both null
     */
    String otherSource = other.getSource();
    if ((source == null && otherSource != null)
            || (source != null && otherSource == null)
            || (source != null && !source.equalsIgnoreCase(otherSource)))
    {
      return false;
    }

    /*
     * accession id must either match or be both null
     */
    String otherAccession = other.getAccessionId();
    if ((accessionId == null && otherAccession != null)
            || (accessionId != null && otherAccession == null)
            || (accessionId != null
                    && !accessionId.equalsIgnoreCase(otherAccession)))
    {
      return false;
    }

    if (!isCanonicalAccession && other.isCanonical())
    {
      isCanonicalAccession = true;
      improved = true;
    }
    else
    {
      if (isCanonicalAccession && !other.isCanonical())
      {
        // other is not an authoritative source of canonical accessions
        return false;
      }
    }
    /*
     * if my version is null, "0" or "source:0" then replace with other version,
     * otherwise the versions have to match
     */
    String otherVersion = other.getVersion();

    if ((version == null || version.equals("0") || version.endsWith(":0"))
            && otherVersion != null)
    {
      setVersion(otherVersion);
    }
    else
    {
      if (version != null && (otherVersion == null
              || !version.equalsIgnoreCase(otherVersion)))
      {
        // FIXME: there may be a problem with old version strings not allowing
        // updating of dbrefentries
        return improved;
      }
    }

    /*
     * if I have no mapping, take that of the other dbref 
     * - providing it had a version and so do I
     */
    if (map == null)
    {
      setMap(other.getMap());
    }
    return true;
  }

  /**
   * test for similar DBRef attributes, except for the map object.
   * 
   * @param entry
   * @return true if source, accession and version are equal with those of entry
   */
  @Override
  public boolean equalRef(DBRefEntryI entry)
  {
    // TODO is this method and equals() not needed?
    if (entry == null)
    {
      return false;
    }
    if (entry == this)
    {
      return true;
    }

    // BH 2019.01.25/2019.02.04 source cannot/should not be null.
    // for example, StructureChooser has dbRef.getSource().equalsIgnoreCase...

    return (entry != null
            && (source != null && entry.getSource() != null
                    && source.equalsIgnoreCase(entry.getSource()))
            && (accessionId != null && entry.getAccessionId() != null
                    && accessionId.equalsIgnoreCase(entry.getAccessionId()))
            && (version != null && entry.getVersion() != null
                    && version.equalsIgnoreCase(entry.getVersion())));
  }

  @Override
  public String getSource()
  {
    return source;
  }

  public int getSourceKey()
  {
    return (sourceKey == Integer.MIN_VALUE
            ? (sourceKey = DBRefSource
                    .getSourceKey(getCanonicalSourceName()))
            : sourceKey);
  }

  /**
   * can be null
   */
  @Override
  public String getVersion()
  {
    return version;
  }

  /**
   * can be null
   */
  @Override
  public String getAccessionId()
  {
    return accessionId;
  }

  @Override
  public void setAccessionId(String accessionId)
  {
    this.accessionId = accessionId;
    // this.accessionId = (accessionId == null ? "" :
    // accessionId).toUpperCase(Locale.ROOT);
  }

  /**
   * CAUTION! allows setting source null or not uppercase!
   */
  @Override
  public void setSource(String source)
  {
    this.source = source;

    // this.source = (source == null ? "" : source).toUpperCase(Locale.ROOT);
    // this.canonicalSourceName = DBRefUtils.getCanonicalName(this.source);
    // this.sourceKey = DBRefSource.getSourceKey(this.canonicalSourceName);
  }

  @Override
  public void setVersion(String version)
  {
    this.version = version;
    this.ucversion = (version == null ? null
            : version.toUpperCase(Locale.ROOT));
  }

  @Override
  public Mapping getMap()
  {
    return map;
  }

  /**
   * @param map
   *          the map to set
   */
  public void setMap(Mapping map)
  {
    this.map = map;
  }

  public boolean hasMap()
  {
    return map != null;
  }

  /**
   * 
   * @return source+":"+accessionId
   */
  public String getSrcAccString()
  {
    return ((source != null) ? source : "") + ":"
            + ((accessionId != null) ? accessionId : "");
  }

  @Override
  public String toString()
  {
    return getSrcAccString();
  }

  @Override
  public boolean isPrimaryCandidate()
  {
    /*
     * if a map is present, unless it is 1:1 and has no SequenceI mate, it cannot be a primary reference.  
     */
    if (map != null)
    {
      SequenceI mto = map.getTo();
      if (mto != null)
      {
        return false;
      }
      MapList ml = map.getMap();
      if (ml.getFromRatio() != ml.getToRatio() || ml.getFromRatio() != 1)
      {
        return false;
      }
      // check map is between identical single contiguous ranges
      List<int[]> fromRanges, toRanges;
      if ((fromRanges = ml.getFromRanges()).size() != 1
              || (toRanges = ml.getToRanges()).size() != 1)
      {
        return false;
      }
      if (fromRanges.get(0)[0] != toRanges.get(0)[0]
              || fromRanges.get(0)[1] != toRanges.get(0)[1])
      {
        return false;
      }
    }
    if (version == null)
    {
      // no version string implies the reference has not been verified at all.
      return false;
    }

    return DBRefSource.isPrimaryCandidate(ucversion);
  }

  /**
   * stores the upper-case canonical name of the source for use in
   * Sequence.getPrimaryDBRefs().
   * 
   * @author Bob Hanson
   * 
   * @return
   */
  public String getCanonicalSourceName()
  {
    return (canonicalSourceName == null
            ? (canonicalSourceName = DBRefUtils
                    .getCanonicalName(this.source))
            : canonicalSourceName);
  }

  /**
   * 
   * @param canonical
   */
  public void setCanonical(boolean canonical)
  {
    isCanonicalAccession = canonical;
  }

  /**
   * 
   * @return true if this is the primary canonical accession for the database
   *         source
   */
  public boolean isCanonical()
  {
    // TODO Auto-generated method stub
    return isCanonicalAccession;
  }
}
