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
package jalview.json.binding.biojson.v1;

import com.github.reinert.jjschema.Attributes;

public class SequencePojo
{
  @Attributes(
    required = true,
    minLength = 3,
    maxLength = 2147483647,
    description = "Sequence residue characters. An aligned sequence may contain <br>one of the following gap characters &#x201c;.&#x201d;, &#x201c;-&#x201d; or &#x201c;&nbsp;&#x201d;")
  private String seq;

  @Attributes(required = true, description = "Sequence name")
  private String name;

  @Attributes(
    required = false,
    description = "Sequence type",
    enums =
    { "DNA", "RNA", "Protein" })
  private String type;

  @Attributes(
    required = true,
    description = "Unique identifier for a given Sequence")
  private String id;

  @Attributes(
    required = false,
    description = "The order/position of a sequence in the alignment space")
  private int order;

  @Attributes(
    required = true,
    description = "The index of the sequence’s first residue in its source database, <br>using a one-based numbering index system")
  private int start;

  @Attributes(
    required = true,
    description = "The index of the sequence’s last residue in its source database, <br>using a one-based numbering index system")
  private int end;

  public SequencePojo()
  {
  }

  public SequencePojo(int start, int end, String id, String name,
          String seq)
  {
    this.id = id;
    this.name = name;
    this.seq = seq;
  }

  public String getSeq()
  {
    return seq;
  }

  public void setSeq(String seq)
  {
    this.seq = seq;
  }

  public String getName()
  {

    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public int getStart()
  {
    return start;
  }

  public void setStart(int start)
  {
    this.start = start;
  }

  public int getEnd()
  {
    return end;
  }

  public void setEnd(int end)
  {
    this.end = end;
  }

  public int getOrder()
  {
    return order;
  }

  public void setOrder(int order)
  {
    this.order = order;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

}
