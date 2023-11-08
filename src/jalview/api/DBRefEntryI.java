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
package jalview.api;

import jalview.datamodel.Mapping;

//JBPComment: this is a datamodel API - so it should be in datamodel (it's a peer of SequenceI)

public interface DBRefEntryI
{
  public boolean equalRef(DBRefEntryI entry);

  /**
   * 
   * @return Source DB name for this entry
   */
  public String getSource();

  /**
   * 
   * @return Accession Id for this entry
   */
  public String getAccessionId();

  /**
   * 
   * @param accessionId
   *          Accession Id for this entry
   */
  public void setAccessionId(String accessionId);

  /**
   * 
   * @param source
   *          Source DB name for this entry
   */
  public void setSource(String source);

  /**
   * 
   * @return Source DB version for this entry
   */
  public String getVersion();

  /**
   * 
   * @param version
   *          Source DB version for this entry
   */
  public void setVersion(String version);

  /**
   * access a mapping, if present that can be used to map positions from the
   * associated dataset sequence to the DBRef's sequence frame.
   * 
   * @return null or a valid mapping.
   */
  public Mapping getMap();

  /**
   * Answers true if this object is either equivalent to, or can be 'improved'
   * by, the given entry. Specifically, answers true if
   * <ul>
   * <li>source and accession are identical</li>
   * <li>version is identical, or this version is of the format "someSource:0",
   * in which case the version for the other entry replaces it</li>
   * <li>mappings are not compared but if this entry has no mapping, replace
   * with that for the other entry</li>
   * </ul>
   * 
   * @param otherEntry
   * @return
   */
  public boolean updateFrom(DBRefEntryI otherEntry);

  /**
   * Answers true if the ref looks like a primary (direct) database reference.
   * <br>
   * The only way a dbref's mappings can be fully verified is via the local
   * sequence frame, so rather than use isPrimaryCandidate directly, please use
   * SequenceI.getPrimaryDbRefs(). <br>
   * Primary references indicate the local sequence data directly corresponds
   * with the database record. All other references are secondary. Direct
   * references indicate that part or all of the local sequence data can be
   * mapped with another sequence, enabling annotation transfer.
   * Cross-references indicate the local sequence data can be corresponded to
   * some other linear coordinate system via a transformation. <br>
   * This method is also sufficient to distinguish direct DBRefEntry mappings
   * from other relationships - e.g. coding relationships (imply a 1:3/3:1
   * mapping), but not transcript relationships, which imply a (possibly
   * non-contiguous) 1:1 mapping.
   *
   * @return true if this reference provides a primary accession for the
   *         associated sequence object
   */
  public boolean isPrimaryCandidate();

  public boolean isCanonical();
}
