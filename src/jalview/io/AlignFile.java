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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;
import jalview.util.StringUtils;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public abstract class AlignFile extends FileParse
        implements AlignmentFileReaderI, AlignmentFileWriterI
{
  int noSeqs = 0;

  int maxLength = 0;

  /**
   * Sequences to be added to form a new alignment. TODO: remove vector in this
   * class
   */
  protected Vector<SequenceI> seqs;

  /**
   * annotation to be added to generated alignment object
   */
  protected Vector<AlignmentAnnotation> annotations;

  /**
   * SequenceGroups to be added to the alignment object
   */
  protected List<SequenceGroup> seqGroups;

  /**
   * Properties to be added to generated alignment object
   */
  private Hashtable properties;

  long start;

  long end;

  /**
   * true if parse() has been called
   */
  private boolean parseCalled = false;

  private boolean parseImmediately = true;

  private boolean dataClosed = false;

  private boolean doXferSettings = true;

  /**
   * @return if doParse() was called at construction time
   */
  protected boolean isParseImmediately()
  {
    return parseImmediately;
  }

  /**
   * Creates a new AlignFile object.
   */
  public AlignFile()
  {
    // Shouldn't we init data structures (JBPNote: not sure - initData is for
    // initialising the structures used for reading from a datasource, and the
    // bare constructor hasn't got any datasource)
    initData();
  }

  public AlignFile(SequenceI[] seqs)
  {
    this();
    setSeqs(seqs);
  }

  /**
   * Constructor which parses the data from a file of some specified type.
   * 
   * @param dataObject
   *          Filename, URL or Pasted String to read from.
   * @param sourceType
   *          What type of file to read from (File, URL, Pasted String)
   */
  public AlignFile(Object dataObject, DataSourceType sourceType)
          throws IOException
  {
    this(true, dataObject, sourceType);
  }

  /**
   * Constructor which (optionally delays) parsing of data from a file of some
   * specified type.
   * 
   * @param parseImmediately
   *          if false, need to call 'doParse()' to begin parsing data
   * @param dataObject
   *          Filename, URL or Pasted String to read from.
   * @param sourceType
   *          What type of file to read from (File, URL)
   * @throws IOException
   */
  public AlignFile(boolean parseImmediately, Object dataObject,
          DataSourceType sourceType) throws IOException
  {
    // BH allows File or String
    super(dataObject, sourceType);
    initData();
    if (parseImmediately)
    {
      doParse();
    }
  }

  /**
   * Attempt to read from the position where some other parsing process left
   * off.
   * 
   * @param source
   * @throws IOException
   */
  public AlignFile(FileParse source, boolean doXferSettings)
          throws IOException
  {
    this(true, source, true, doXferSettings);
  }

  public AlignFile(FileParse source) throws IOException
  {
    this(true, source);
  }

  /**
   * Construct a new parser to read from the position where some other parsing
   * process left
   * 
   * @param parseImmediately
   *          if false, need to call 'doParse()' to begin parsing data
   * @param source
   */
  public AlignFile(boolean parseImmediately, FileParse source)
          throws IOException
  {
    this(parseImmediately, source, true);
  }

  public AlignFile(boolean parseImmediately, FileParse source,
          boolean closeData) throws IOException
  {
    this(parseImmediately, source, closeData, true);
  }

  public AlignFile(boolean parseImmediately, FileParse source,
          boolean closeData, boolean doXferSettings) throws IOException
  {
    super(source);
    initData();

    // stash flag in case parse needs to know if it has to autoconfigure or was
    // configured after construction
    this.parseImmediately = parseImmediately;
    this.doXferSettings = doXferSettings;

    if (parseImmediately)
    {
      doParse(closeData);
    }
  }

  /**
   * called if parsing was delayed till after parser was constructed
   * 
   * @throws IOException
   */
  public void doParse() throws IOException
  {
    doParse(true);
  }

  public void doParse(boolean closeData) throws IOException
  {
    if (parseCalled)
    {
      throw new IOException(
              "Implementation error: Parser called twice for same data.\n"
                      + "Need to call initData() again before parsing can be reattempted.");
    }
    parseCalled = true;
    parse(this.doXferSettings);
    if (closeData && !dataClosed)
    {
      dataIn.close();
      dataClosed = true;
    }
  }

  /**
   * Return the seqs Vector
   */
  public Vector<SequenceI> getSeqs()
  {
    return seqs;
  }

  public List<SequenceGroup> getSeqGroups()
  {
    return seqGroups;
  }

  /**
   * Return the Sequences in the seqs Vector as an array of Sequences
   */
  @Override
  public SequenceI[] getSeqsAsArray()
  {
    SequenceI[] s = new SequenceI[seqs.size()];

    for (int i = 0; i < seqs.size(); i++)
    {
      s[i] = seqs.elementAt(i);
    }

    return s;
  }

  /**
   * called by AppletFormatAdapter to generate an annotated alignment, rather
   * than bare sequences.
   * 
   * @param al
   */
  @Override
  public void addAnnotations(AlignmentI al)
  {
    addProperties(al);
    for (int i = 0; i < annotations.size(); i++)
    {
      // detect if annotations.elementAt(i) rna secondary structure
      // if so then do:
      /*
       * SequenceFeature[] pairArray =
       * Rna.GetBasePairsFromAlignmentAnnotation(annotations.elementAt(i));
       * Rna.HelixMap(pairArray);
       */
      AlignmentAnnotation an = annotations.elementAt(i);
      an.validateRangeAndDisplay();
      al.addAnnotation(an);
    }

  }

  /**
   * register sequence groups on the alignment for **output**
   * 
   * @param al
   */
  public void addSeqGroups(AlignmentI al)
  {
    this.seqGroups = al.getGroups();

  }

  /**
   * Add any additional information extracted from the file to the alignment
   * properties.
   * 
   * @note implicitly called by addAnnotations()
   * @param al
   */
  public void addProperties(AlignmentI al)
  {
    if (properties != null && properties.size() > 0)
    {
      Enumeration keys = properties.keys();
      Enumeration vals = properties.elements();
      while (keys.hasMoreElements())
      {
        al.setProperty(keys.nextElement(), vals.nextElement());
      }
    }
  }

  /**
   * Store a non-null key-value pair in a hashtable used to set alignment
   * properties note: null keys will raise an error, null values will result in
   * the key/value pair being silently ignored.
   * 
   * @param key
   *          - non-null key object
   * @param value
   *          - non-null value
   */
  protected void setAlignmentProperty(Object key, Object value)
  {
    if (key == null)
    {
      throw new Error(MessageManager.getString(
              "error.implementation_error_cannot_have_null_alignment"));
    }
    if (value == null)
    {
      return; // null properties are ignored.
    }
    if (properties == null)
    {
      properties = new Hashtable();
    }
    properties.put(key, value);
  }

  protected Object getAlignmentProperty(Object key)
  {
    if (properties != null && key != null)
    {
      return properties.get(key);
    }
    return null;
  }

  /**
   * Initialise objects to store sequence data in.
   */
  protected void initData()
  {
    seqs = new Vector<SequenceI>();
    annotations = new Vector<AlignmentAnnotation>();
    seqGroups = new ArrayList<SequenceGroup>();
    parseCalled = false;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param s
   *          DOCUMENT ME!
   */
  @Override
  public void setSeqs(SequenceI[] s)
  {
    seqs = new Vector<SequenceI>();

    for (int i = 0; i < s.length; i++)
    {
      seqs.addElement(s[i]);
    }
  }

  /**
   * This method must be implemented to parse the contents of the file.
   */
  public abstract void parse() throws IOException;

  /**
   * This method is only overridden by JmolParser because of its use in
   * StructureFile to parse annotations
   * 
   * @param doXferSettings
   * @throws IOException
   */
  public void parse(boolean doXferSettings) throws IOException
  {
    parse();
  }

  /**
   * A general parser for ids.
   * 
   * @String id Id to be parsed
   */
  Sequence parseId(String id)
  {
    Sequence seq = null;
    id = id.trim();
    int space = StringUtils.indexOfFirstWhitespace(id);
    if (space > -1)
    {
      seq = new Sequence(id.substring(0, space), "");
      String desc = id.substring(space + 1);
      seq.setDescription(desc);

      /*
       * it is tempting to parse Ensembl style gene description e.g.
       * chromosome:GRCh38:7:140696688:140721955:1 and set the
       * start position of the sequence, but this causes much confusion
       * for reverse strand feature locations
       */
    }
    else
    {
      seq = new Sequence(id, "");
    }

    return seq;
  }

  /**
   * Creates the output id. Adds prefix Uniprot format source|id and optionally
   * suffix Jalview /start-end
   * 
   * @param jvsuffix
   * 
   * @String id Id to be parsed
   */
  String printId(SequenceI seq, boolean jvsuffix)
  {
    return seq.getDisplayId(jvsuffix);
  }

  String printId(SequenceI seq)
  {
    return printId(seq, true);
  }

  /**
   * vector of String[] treeName, newickString pairs
   */
  Vector<String[]> newickStrings = null;

  protected void addNewickTree(String treeName, String newickString)
  {
    if (newickStrings == null)
    {
      newickStrings = new Vector<String[]>();
    }
    newickStrings.addElement(new String[] { treeName, newickString });
  }

  protected int getTreeCount()
  {
    return newickStrings == null ? 0 : newickStrings.size();
  }

  @Override
  public void addGroups(AlignmentI al)
  {

    for (SequenceGroup sg : getSeqGroups())
    {
      al.addGroup(sg);
    }
  }

  protected void addSequence(SequenceI seq)
  {
    seqs.add(seq);
  }

  public void setDoXferSettings(boolean b)
  {
    doXferSettings = b;
  }

  public boolean getDoXferSettings()
  {
    return doXferSettings;
  }
}
