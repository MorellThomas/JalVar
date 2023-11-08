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
package jalview.ws.sifts;

import java.util.Locale;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import jalview.analysis.AlignSeq;
import jalview.analysis.scoremodels.ScoreMatrix;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.api.DBRefEntryI;
import jalview.api.SiftsClientI;
import jalview.bin.Console;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.io.BackupFiles;
import jalview.io.StructureFile;
import jalview.schemes.ResidueProperties;
import jalview.structure.StructureMapping;
import jalview.util.Comparison;
import jalview.util.DBRefUtils;
import jalview.util.Format;
import jalview.util.Platform;
import jalview.xml.binding.sifts.Entry;
import jalview.xml.binding.sifts.Entry.Entity;
import jalview.xml.binding.sifts.Entry.Entity.Segment;
import jalview.xml.binding.sifts.Entry.Entity.Segment.ListMapRegion.MapRegion;
import jalview.xml.binding.sifts.Entry.Entity.Segment.ListResidue.Residue;
import jalview.xml.binding.sifts.Entry.Entity.Segment.ListResidue.Residue.CrossRefDb;
import jalview.xml.binding.sifts.Entry.Entity.Segment.ListResidue.Residue.ResidueDetail;
import mc_view.Atom;
import mc_view.PDBChain;

public class SiftsClient implements SiftsClientI
{
  /*
   * for use in mocking out file fetch for tests only
   * - reset to null after testing!
   */
  private static File mockSiftsFile;

  private Entry siftsEntry;

  private StructureFile pdb;

  private String pdbId;

  private String structId;

  private CoordinateSys seqCoordSys = CoordinateSys.UNIPROT;

  /**
   * PDB sequence position to sequence coordinate mapping as derived from SIFTS
   * record for the identified SeqCoordSys Used for lift-over from sequence
   * derived from PDB (with first extracted PDBRESNUM as 'start' to the sequence
   * being annotated with PDB data
   */
  private jalview.datamodel.Mapping seqFromPdbMapping;

  private static final int BUFFER_SIZE = 4096;

  public static final int UNASSIGNED = Integer.MIN_VALUE;

  private static final int PDB_RES_POS = 0;

  private static final int PDB_ATOM_POS = 1;

  private static final int PDBE_POS = 2;

  private static final String NOT_OBSERVED = "Not_Observed";

  private static final String SIFTS_SPLIT_FTP_BASE_URL = "https://ftp.ebi.ac.uk/pub/databases/msd/sifts/split_xml/";

  private final static String NEWLINE = System.lineSeparator();

  private String curSourceDBRef;

  private HashSet<String> curDBRefAccessionIdsString;

  private enum CoordinateSys
  {
    UNIPROT("UniProt"), PDB("PDBresnum"), PDBe("PDBe");

    private String name;

    private CoordinateSys(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }
  };

  private enum ResidueDetailType
  {
    NAME_SEC_STRUCTURE("nameSecondaryStructure"),
    CODE_SEC_STRUCTURE("codeSecondaryStructure"), ANNOTATION("Annotation");

    private String code;

    private ResidueDetailType(String code)
    {
      this.code = code;
    }

    public String getCode()
    {
      return code;
    }
  };

  /**
   * Fetch SIFTs file for the given PDBfile and construct an instance of
   * SiftsClient
   * 
   * @param pdbId
   * @throws SiftsException
   */
  public SiftsClient(StructureFile pdb) throws SiftsException
  {
    this.pdb = pdb;
    this.pdbId = pdb.getId();
    File siftsFile = getSiftsFile(pdbId);
    siftsEntry = parseSIFTs(siftsFile);
  }

  /**
   * Parse the given SIFTs File and return a JAXB POJO of parsed data
   * 
   * @param siftFile
   *          - the GZipped SIFTs XML file to parse
   * @return
   * @throws Exception
   *           if a problem occurs while parsing the SIFTs XML
   */
  private Entry parseSIFTs(File siftFile) throws SiftsException
  {
    try (InputStream in = new FileInputStream(siftFile);
            GZIPInputStream gzis = new GZIPInputStream(in);)
    {
      // System.out.println("File : " + siftFile.getAbsolutePath());
      JAXBContext jc = JAXBContext.newInstance("jalview.xml.binding.sifts");
      XMLStreamReader streamReader = XMLInputFactory.newInstance()
              .createXMLStreamReader(gzis);
      Unmarshaller um = jc.createUnmarshaller();
      JAXBElement<Entry> jbe = um.unmarshal(streamReader, Entry.class);
      return jbe.getValue();
    } catch (Exception e)
    {
      e.printStackTrace();
      throw new SiftsException(e.getMessage());
    }
  }

  /**
   * Get a SIFTs XML file for a given PDB Id from Cache or download from FTP
   * repository if not found in cache
   * 
   * @param pdbId
   * @return SIFTs XML file
   * @throws SiftsException
   */
  public static File getSiftsFile(String pdbId) throws SiftsException
  {
    /*
     * return mocked file if it has been set
     */
    if (mockSiftsFile != null)
    {
      return mockSiftsFile;
    }

    String siftsFileName = SiftsSettings.getSiftDownloadDirectory()
            + pdbId.toLowerCase(Locale.ROOT) + ".xml.gz";
    File siftsFile = new File(siftsFileName);
    if (siftsFile.exists())
    {
      // The line below is required for unit testing... don't comment it out!!!
      System.out.println(">>> SIFTS File already downloaded for " + pdbId);

      if (isFileOlderThanThreshold(siftsFile,
              SiftsSettings.getCacheThresholdInDays()))
      {
        File oldSiftsFile = new File(siftsFileName + "_old");
        BackupFiles.moveFileToFile(siftsFile, oldSiftsFile);
        try
        {
          siftsFile = downloadSiftsFile(pdbId.toLowerCase(Locale.ROOT));
          oldSiftsFile.delete();
          return siftsFile;
        } catch (IOException e)
        {
          e.printStackTrace();
          BackupFiles.moveFileToFile(oldSiftsFile, siftsFile);
          return new File(siftsFileName);
        }
      }
      else
      {
        return siftsFile;
      }
    }
    try
    {
      siftsFile = downloadSiftsFile(pdbId.toLowerCase(Locale.ROOT));
    } catch (IOException e)
    {
      throw new SiftsException(e.getMessage());
    }
    return siftsFile;
  }

  /**
   * This method enables checking if a cached file has exceeded a certain
   * threshold(in days)
   * 
   * @param file
   *          the cached file
   * @param noOfDays
   *          the threshold in days
   * @return
   */
  public static boolean isFileOlderThanThreshold(File file, int noOfDays)
  {
    Path filePath = file.toPath();
    BasicFileAttributes attr;
    int diffInDays = 0;
    try
    {
      attr = Files.readAttributes(filePath, BasicFileAttributes.class);
      diffInDays = (int) ((new Date().getTime()
              - attr.lastModifiedTime().toMillis())
              / (1000 * 60 * 60 * 24));
      // System.out.println("Diff in days : " + diffInDays);
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    return noOfDays <= diffInDays;
  }

  /**
   * Download a SIFTs XML file for a given PDB Id from an FTP repository
   * 
   * @param pdbId
   * @return downloaded SIFTs XML file
   * @throws SiftsException
   * @throws IOException
   */
  public static File downloadSiftsFile(String pdbId)
          throws SiftsException, IOException
  {
    if (pdbId.contains(".cif"))
    {
      pdbId = pdbId.replace(".cif", "");
    }
    String siftFile = pdbId + ".xml.gz";
    String siftsFileFTPURL = getDownloadUrlFor(siftFile);

    /*
     * Download the file from URL to either
     * Java: directory of cached downloaded SIFTS files
     * Javascript: temporary 'file' (in-memory cache)
     */
    File downloadTo = null;
    if (Platform.isJS())
    {
      downloadTo = File.createTempFile(siftFile, ".xml.gz");
    }
    else
    {
      downloadTo = new File(
              SiftsSettings.getSiftDownloadDirectory() + siftFile);
      File siftsDownloadDir = new File(
              SiftsSettings.getSiftDownloadDirectory());
      if (!siftsDownloadDir.exists())
      {
        siftsDownloadDir.mkdirs();
      }
    }

    // System.out.println(">> Download ftp url : " + siftsFileFTPURL);
    // long now = System.currentTimeMillis();
    URL url = new URL(siftsFileFTPURL);
    URLConnection conn = url.openConnection();
    InputStream inputStream = conn.getInputStream();
    FileOutputStream outputStream = new FileOutputStream(downloadTo);
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead = -1;
    while ((bytesRead = inputStream.read(buffer)) != -1)
    {
      outputStream.write(buffer, 0, bytesRead);
    }
    outputStream.close();
    inputStream.close();
    // System.out.println(">>> File downloaded : " + downloadedSiftsFile
    // + " took " + (System.currentTimeMillis() - now) + "ms");
    return downloadTo;
  }

  public static String getDownloadUrlFor(String siftFile)
  {
    String durl = SIFTS_SPLIT_FTP_BASE_URL+siftFile.substring(1, 3)+"/"+siftFile;
    Console.trace("SIFTS URL for "+siftFile+" is "+durl);
    return durl;
    
  }

  /**
   * Delete the SIFTs file for the given PDB Id in the local SIFTs download
   * directory
   * 
   * @param pdbId
   * @return true if the file was deleted or doesn't exist
   */
  public static boolean deleteSiftsFileByPDBId(String pdbId)
  {
    File siftsFile = new File(SiftsSettings.getSiftDownloadDirectory()
            + pdbId.toLowerCase(Locale.ROOT) + ".xml.gz");
    if (siftsFile.exists())
    {
      return siftsFile.delete();
    }
    return true;
  }

  /**
   * Get a valid SIFTs DBRef for the given sequence current SIFTs entry
   * 
   * @param seq
   *          - the target sequence for the operation
   * @return a valid DBRefEntry that is SIFTs compatible
   * @throws Exception
   *           if no valid source DBRefEntry was found for the given sequences
   */
  public DBRefEntryI getValidSourceDBRef(SequenceI seq)
          throws SiftsException
  {
    List<DBRefEntry> dbRefs = seq.getPrimaryDBRefs();
    if (dbRefs == null || dbRefs.size() < 1)
    {
      throw new SiftsException(
              "Source DBRef could not be determined. DBRefs might not have been retrieved.");
    }

    for (DBRefEntry dbRef : dbRefs)
    {
      if (dbRef == null || dbRef.getAccessionId() == null
              || dbRef.getSource() == null)
      {
        continue;
      }
      String canonicalSource = DBRefUtils
              .getCanonicalName(dbRef.getSource());
      if (isValidDBRefEntry(dbRef)
              && (canonicalSource.equalsIgnoreCase(DBRefSource.UNIPROT)
                      || canonicalSource.equalsIgnoreCase(DBRefSource.PDB)))
      {
        return dbRef;
      }
    }
    throw new SiftsException("Could not get source DB Ref");
  }

  /**
   * Check that the DBRef Entry is properly populated and is available in this
   * SiftClient instance
   * 
   * @param entry
   *          - DBRefEntry to validate
   * @return true validation is successful otherwise false is returned.
   */
  boolean isValidDBRefEntry(DBRefEntryI entry)
  {
    return entry != null && entry.getAccessionId() != null
            && isFoundInSiftsEntry(entry.getAccessionId());
  }

  @Override
  public HashSet<String> getAllMappingAccession()
  {
    HashSet<String> accessions = new HashSet<String>();
    List<Entity> entities = siftsEntry.getEntity();
    for (Entity entity : entities)
    {
      List<Segment> segments = entity.getSegment();
      for (Segment segment : segments)
      {
        List<MapRegion> mapRegions = segment.getListMapRegion()
                .getMapRegion();
        for (MapRegion mapRegion : mapRegions)
        {
          accessions.add(mapRegion.getDb().getDbAccessionId()
                  .toLowerCase(Locale.ROOT));
        }
      }
    }
    return accessions;
  }

  @Override
  public StructureMapping getSiftsStructureMapping(SequenceI seq,
          String pdbFile, String chain) throws SiftsException
  {
    SequenceI aseq = seq;
    while (seq.getDatasetSequence() != null)
    {
      seq = seq.getDatasetSequence();
    }
    structId = (chain == null) ? pdbId : pdbId + "|" + chain;
    System.out.println("Getting SIFTS mapping for " + structId + ": seq "
            + seq.getName());

    final StringBuilder mappingDetails = new StringBuilder(128);
    PrintStream ps = new PrintStream(System.out)
    {
      @Override
      public void print(String x)
      {
        mappingDetails.append(x);
      }

      @Override
      public void println()
      {
        mappingDetails.append(NEWLINE);
      }
    };
    HashMap<Integer, int[]> mapping = getGreedyMapping(chain, seq, ps);

    String mappingOutput = mappingDetails.toString();
    StructureMapping siftsMapping = new StructureMapping(aseq, pdbFile,
            pdbId, chain, mapping, mappingOutput, seqFromPdbMapping);

    return siftsMapping;
  }

  @Override
  public HashMap<Integer, int[]> getGreedyMapping(String entityId,
          SequenceI seq, java.io.PrintStream os) throws SiftsException
  {
    List<Integer> omitNonObserved = new ArrayList<>();
    int nonObservedShiftIndex = 0, pdbeNonObserved = 0;
    // System.out.println("Generating mappings for : " + entityId);
    Entity entity = null;
    entity = getEntityById(entityId);
    String originalSeq = AlignSeq.extractGaps(
            jalview.util.Comparison.GapChars, seq.getSequenceAsString());
    HashMap<Integer, int[]> mapping = new HashMap<Integer, int[]>();
    DBRefEntryI sourceDBRef;
    sourceDBRef = getValidSourceDBRef(seq);
    // TODO ensure sequence start/end is in the same coordinate system and
    // consistent with the choosen sourceDBRef

    // set sequence coordinate system - default value is UniProt
    if (sourceDBRef.getSource().equalsIgnoreCase(DBRefSource.PDB))
    {
      seqCoordSys = CoordinateSys.PDB;
    }

    HashSet<String> dbRefAccessionIdsString = new HashSet<String>();
    for (DBRefEntry dbref : seq.getDBRefs())
    {
      dbRefAccessionIdsString
              .add(dbref.getAccessionId().toLowerCase(Locale.ROOT));
    }
    dbRefAccessionIdsString
            .add(sourceDBRef.getAccessionId().toLowerCase(Locale.ROOT));

    curDBRefAccessionIdsString = dbRefAccessionIdsString;
    curSourceDBRef = sourceDBRef.getAccessionId();

    TreeMap<Integer, String> resNumMap = new TreeMap<Integer, String>();
    List<Segment> segments = entity.getSegment();
    SegmentHelperPojo shp = new SegmentHelperPojo(seq, mapping, resNumMap,
            omitNonObserved, nonObservedShiftIndex, pdbeNonObserved);
    processSegments(segments, shp);
    try
    {
      populateAtomPositions(entityId, mapping);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    if (seqCoordSys == CoordinateSys.UNIPROT)
    {
      padWithGaps(resNumMap, omitNonObserved);
    }
    int seqStart = UNASSIGNED;
    int seqEnd = UNASSIGNED;
    int pdbStart = UNASSIGNED;
    int pdbEnd = UNASSIGNED;

    if (mapping.isEmpty())
    {
      throw new SiftsException("SIFTS mapping failed for "+entityId+" and "+seq.getName());
    }
    // also construct a mapping object between the seq-coord sys and the PDB
    // seq's coord sys

    Integer[] keys = mapping.keySet().toArray(new Integer[0]);
    Arrays.sort(keys);
    seqStart = keys[0];
    seqEnd = keys[keys.length - 1];
    List<int[]> from = new ArrayList<>(), to = new ArrayList<>();
    int[] _cfrom = null, _cto = null;
    String matchedSeq = originalSeq;
    if (seqStart != UNASSIGNED) // fixme! seqStart can map to -1 for a pdb
                                // sequence that starts <-1
    {
      for (int seqps : keys)
      {
        int pdbpos = mapping.get(seqps)[PDBE_POS];
        if (pdbpos == UNASSIGNED)
        {
          // not correct - pdbpos might be -1, but leave it for now
          continue;
        }
        if (_cfrom == null || seqps != _cfrom[1] + 1)
        {
          _cfrom = new int[] { seqps, seqps };
          from.add(_cfrom);
          _cto = null; // discontinuity
        }
        else
        {
          _cfrom[1] = seqps;
        }
        if (_cto == null || pdbpos != 1 + _cto[1])
        {
          _cto = new int[] { pdbpos, pdbpos };
          to.add(_cto);
        }
        else
        {
          _cto[1] = pdbpos;
        }
      }
      _cfrom = new int[from.size() * 2];
      _cto = new int[to.size() * 2];
      int p = 0;
      for (int[] range : from)
      {
        _cfrom[p++] = range[0];
        _cfrom[p++] = range[1];
      }
      ;
      p = 0;
      for (int[] range : to)
      {
        _cto[p++] = range[0];
        _cto[p++] = range[1];
      }
      ;

      seqFromPdbMapping = new jalview.datamodel.Mapping(null, _cto, _cfrom,
              1, 1);
      pdbStart = mapping.get(seqStart)[PDB_RES_POS];
      pdbEnd = mapping.get(seqEnd)[PDB_RES_POS];
      int orignalSeqStart = seq.getStart();
      if (orignalSeqStart >= 1)
      {
        int subSeqStart = (seqStart >= orignalSeqStart)
                ? seqStart - orignalSeqStart
                : 0;
        int subSeqEnd = seqEnd - (orignalSeqStart - 1);
        subSeqEnd = originalSeq.length() < subSeqEnd ? originalSeq.length()
                : subSeqEnd;
        matchedSeq = originalSeq.substring(subSeqStart, subSeqEnd);
      }
      else
      {
        matchedSeq = originalSeq.substring(1, originalSeq.length());
      }
    }

    StringBuilder targetStrucSeqs = new StringBuilder();
    for (String res : resNumMap.values())
    {
      targetStrucSeqs.append(res);
    }

    if (os != null)
    {
      MappingOutputPojo mop = new MappingOutputPojo();
      mop.setSeqStart(seqStart);
      mop.setSeqEnd(seqEnd);
      mop.setSeqName(seq.getName());
      mop.setSeqResidue(matchedSeq);

      mop.setStrStart(pdbStart);
      mop.setStrEnd(pdbEnd);
      mop.setStrName(structId);
      mop.setStrResidue(targetStrucSeqs.toString());

      mop.setType("pep");
      os.print(getMappingOutput(mop).toString());
      os.println();
    }
    return mapping;
  }

  void processSegments(List<Segment> segments, SegmentHelperPojo shp)
  {
    SequenceI seq = shp.getSeq();
    HashMap<Integer, int[]> mapping = shp.getMapping();
    TreeMap<Integer, String> resNumMap = shp.getResNumMap();
    List<Integer> omitNonObserved = shp.getOmitNonObserved();
    int nonObservedShiftIndex = shp.getNonObservedShiftIndex();
    int pdbeNonObservedCount = shp.getPdbeNonObserved();
    int firstPDBResNum = UNASSIGNED;
    for (Segment segment : segments)
    {
      // System.out.println("Mapping segments : " + segment.getSegId() + "\\"s
      // + segStartEnd);
      List<Residue> residues = segment.getListResidue().getResidue();
      for (Residue residue : residues)
      {
        boolean isObserved = isResidueObserved(residue);
        int pdbeIndex = getLeadingIntegerValue(residue.getDbResNum(),
                UNASSIGNED);
        int currSeqIndex = UNASSIGNED;
        List<CrossRefDb> cRefDbs = residue.getCrossRefDb();
        CrossRefDb pdbRefDb = null;
        for (CrossRefDb cRefDb : cRefDbs)
        {
          if (cRefDb.getDbSource().equalsIgnoreCase(DBRefSource.PDB))
          {
            pdbRefDb = cRefDb;
            if (firstPDBResNum == UNASSIGNED)
            {
              firstPDBResNum = getLeadingIntegerValue(cRefDb.getDbResNum(),
                      UNASSIGNED);
            }
            else
            {
              if (isObserved)
              {
                // after we find the first observed residue we just increment
                firstPDBResNum++;
              }
            }
          }
          if (cRefDb.getDbCoordSys().equalsIgnoreCase(seqCoordSys.getName())
                  && isAccessionMatched(cRefDb.getDbAccessionId()))
          {
            currSeqIndex = getLeadingIntegerValue(cRefDb.getDbResNum(),
                    UNASSIGNED);
            if (pdbRefDb != null)
            {
              break;// exit loop if pdb and uniprot are already found
            }
          }
        }
        if (!isObserved)
        {
          ++pdbeNonObservedCount;
        }
        if (seqCoordSys == seqCoordSys.PDB) // FIXME: is seqCoordSys ever PDBe
                                            // ???
        {
          // if the sequence has a primary reference to the PDB, then we are
          // dealing with a sequence extracted directly from the PDB. In that
          // case, numbering is PDBe - non-observed residues
          currSeqIndex = seq.getStart() - 1 + pdbeIndex;
        }
        if (!isObserved)
        {
          if (seqCoordSys != CoordinateSys.UNIPROT) // FIXME: PDB or PDBe only
                                                    // here
          {
            // mapping to PDB or PDBe so we need to bookkeep for the
            // non-observed
            // SEQRES positions
            omitNonObserved.add(currSeqIndex);
            ++nonObservedShiftIndex;
          }
        }
        if (currSeqIndex == UNASSIGNED)
        {
          // change in logic - unobserved residues with no currSeqIndex
          // corresponding are still counted in both nonObservedShiftIndex and
          // pdbeIndex...
          continue;
        }
        // if (currSeqIndex >= seq.getStart() && currSeqIndex <= seqlength) //
        // true
        // numbering
        // is
        // not
        // up
        // to
        // seq.getEnd()
        {

          int resNum = (pdbRefDb == null)
                  ? getLeadingIntegerValue(residue.getDbResNum(),
                          UNASSIGNED)
                  : getLeadingIntegerValue(pdbRefDb.getDbResNum(),
                          UNASSIGNED);

          if (isObserved)
          {
            char resCharCode = ResidueProperties
                    .getSingleCharacterCode(ResidueProperties
                            .getCanonicalAminoAcid(residue.getDbResName()));
            resNumMap.put(currSeqIndex, String.valueOf(resCharCode));

            int[] mappingcols = new int[] { Integer.valueOf(resNum),
                UNASSIGNED, isObserved ? firstPDBResNum : UNASSIGNED };

            mapping.put(currSeqIndex - nonObservedShiftIndex, mappingcols);
          }
        }
      }
    }
  }

  /**
   * Get the leading integer part of a string that begins with an integer.
   * 
   * @param input
   *          - the string input to process
   * @param failValue
   *          - value returned if unsuccessful
   * @return
   */
  static int getLeadingIntegerValue(String input, int failValue)
  {
    if (input == null)
    {
      return failValue;
    }
    String[] parts = input.split("(?=\\D)(?<=\\d)");
    if (parts != null && parts.length > 0 && parts[0].matches("[0-9]+"))
    {
      return Integer.valueOf(parts[0]);
    }
    return failValue;
  }

  /**
   * 
   * @param chainId
   *          Target chain to populate mapping of its atom positions.
   * @param mapping
   *          Two dimension array of residue index versus atom position
   * @throws IllegalArgumentException
   *           Thrown if chainId or mapping is null
   * @throws SiftsException
   */
  void populateAtomPositions(String chainId, Map<Integer, int[]> mapping)
          throws IllegalArgumentException, SiftsException
  {
    try
    {
      PDBChain chain = pdb.findChain(chainId);

      if (chain == null || mapping == null)
      {
        throw new IllegalArgumentException(
                "Chain id or mapping must not be null.");
      }
      for (int[] map : mapping.values())
      {
        if (map[PDB_RES_POS] != UNASSIGNED)
        {
          map[PDB_ATOM_POS] = getAtomIndex(map[PDB_RES_POS], chain.atoms);
        }
      }
    } catch (NullPointerException e)
    {
      throw new SiftsException(e.getMessage());
    } catch (Exception e)
    {
      throw new SiftsException(e.getMessage());
    }
  }

  /**
   * 
   * @param residueIndex
   *          The residue index used for the search
   * @param atoms
   *          A collection of Atom to search
   * @return atom position for the given residue index
   */
  int getAtomIndex(int residueIndex, Collection<Atom> atoms)
  {
    if (atoms == null)
    {
      throw new IllegalArgumentException(
              "atoms collection must not be null!");
    }
    for (Atom atom : atoms)
    {
      if (atom.resNumber == residueIndex)
      {
        return atom.atomIndex;
      }
    }
    return UNASSIGNED;
  }

  /**
   * Checks if the residue instance is marked 'Not_observed' or not
   * 
   * @param residue
   * @return
   */
  private boolean isResidueObserved(Residue residue)
  {
    Set<String> annotations = getResidueAnnotaitons(residue,
            ResidueDetailType.ANNOTATION);
    if (annotations == null || annotations.isEmpty())
    {
      return true;
    }
    for (String annotation : annotations)
    {
      if (annotation.equalsIgnoreCase(NOT_OBSERVED))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Get annotation String for a given residue and annotation type
   * 
   * @param residue
   * @param type
   * @return
   */
  private Set<String> getResidueAnnotaitons(Residue residue,
          ResidueDetailType type)
  {
    HashSet<String> foundAnnotations = new HashSet<String>();
    List<ResidueDetail> resDetails = residue.getResidueDetail();
    for (ResidueDetail resDetail : resDetails)
    {
      if (resDetail.getProperty().equalsIgnoreCase(type.getCode()))
      {
        foundAnnotations.add(resDetail.getContent());
      }
    }
    return foundAnnotations;
  }

  @Override
  public boolean isAccessionMatched(String accession)
  {
    boolean isStrictMatch = true;
    return isStrictMatch ? curSourceDBRef.equalsIgnoreCase(accession)
            : curDBRefAccessionIdsString
                    .contains(accession.toLowerCase(Locale.ROOT));
  }

  private boolean isFoundInSiftsEntry(String accessionId)
  {
    Set<String> siftsDBRefs = getAllMappingAccession();
    return accessionId != null
            && siftsDBRefs.contains(accessionId.toLowerCase(Locale.ROOT));
  }

  /**
   * Pad omitted residue positions in PDB sequence with gaps
   * 
   * @param resNumMap
   */
  void padWithGaps(Map<Integer, String> resNumMap,
          List<Integer> omitNonObserved)
  {
    if (resNumMap == null || resNumMap.isEmpty())
    {
      return;
    }
    Integer[] keys = resNumMap.keySet().toArray(new Integer[0]);
    // Arrays.sort(keys);
    int firstIndex = keys[0];
    int lastIndex = keys[keys.length - 1];
    // System.out.println("Min value " + firstIndex);
    // System.out.println("Max value " + lastIndex);
    for (int x = firstIndex; x <= lastIndex; x++)
    {
      if (!resNumMap.containsKey(x) && !omitNonObserved.contains(x))
      {
        resNumMap.put(x, "-");
      }
    }
  }

  @Override
  public Entity getEntityById(String id) throws SiftsException
  {
    // Determines an entity to process by performing a heuristic matching of all
    // Entities with the given chainId and choosing the best matching Entity
    Entity entity = getEntityByMostOptimalMatchedId(id);
    if (entity != null)
    {
      return entity;
    }
    throw new SiftsException("Entity " + id + " not found");
  }

  /**
   * This method was added because EntityId is NOT always equal to ChainId.
   * Hence, it provides the logic to greedily detect the "true" Entity for a
   * given chainId where discrepancies exist.
   * 
   * @param chainId
   * @return
   */
  public Entity getEntityByMostOptimalMatchedId(String chainId)
  {
    // System.out.println("---> advanced greedy entityId matching block
    // entered..");
    List<Entity> entities = siftsEntry.getEntity();
    SiftsEntitySortPojo[] sPojo = new SiftsEntitySortPojo[entities.size()];
    int count = 0;
    for (Entity entity : entities)
    {
      sPojo[count] = new SiftsEntitySortPojo();
      sPojo[count].entityId = entity.getEntityId();

      List<Segment> segments = entity.getSegment();
      for (Segment segment : segments)
      {
        List<Residue> residues = segment.getListResidue().getResidue();
        for (Residue residue : residues)
        {
          List<CrossRefDb> cRefDbs = residue.getCrossRefDb();
          for (CrossRefDb cRefDb : cRefDbs)
          {
            if (!cRefDb.getDbSource().equalsIgnoreCase("PDB"))
            {
              continue;
            }
            ++sPojo[count].resCount;
            if (cRefDb.getDbChainId().equalsIgnoreCase(chainId))
            {
              ++sPojo[count].chainIdFreq;
            }
          }
        }
      }
      sPojo[count].pid = (100 * sPojo[count].chainIdFreq)
              / sPojo[count].resCount;
      ++count;
    }
    Arrays.sort(sPojo, Collections.reverseOrder());
    // System.out.println("highest matched entity : " + sPojo[0].entityId);
    // System.out.println("highest matched pid : " + sPojo[0].pid);

    if (sPojo[0].entityId != null)
    {
      if (sPojo[0].pid < 1)
      {
        return null;
      }
      for (Entity entity : entities)
      {
        if (!entity.getEntityId().equalsIgnoreCase(sPojo[0].entityId))
        {
          continue;
        }
        return entity;
      }
    }
    return null;
  }

  private class SiftsEntitySortPojo
          implements Comparable<SiftsEntitySortPojo>
  {
    public String entityId;

    public int chainIdFreq;

    public int pid;

    public int resCount;

    @Override
    public int compareTo(SiftsEntitySortPojo o)
    {
      return this.pid - o.pid;
    }
  }

  private class SegmentHelperPojo
  {
    private SequenceI seq;

    private HashMap<Integer, int[]> mapping;

    private TreeMap<Integer, String> resNumMap;

    private List<Integer> omitNonObserved;

    private int nonObservedShiftIndex;

    /**
     * count of number of 'not observed' positions in the PDB record's SEQRES
     * (total number of residues with coordinates == length(SEQRES) -
     * pdbeNonObserved
     */
    private int pdbeNonObserved;

    public SegmentHelperPojo(SequenceI seq, HashMap<Integer, int[]> mapping,
            TreeMap<Integer, String> resNumMap,
            List<Integer> omitNonObserved, int nonObservedShiftIndex,
            int pdbeNonObserved)
    {
      setSeq(seq);
      setMapping(mapping);
      setResNumMap(resNumMap);
      setOmitNonObserved(omitNonObserved);
      setNonObservedShiftIndex(nonObservedShiftIndex);
      setPdbeNonObserved(pdbeNonObserved);

    }

    public void setPdbeNonObserved(int pdbeNonObserved2)
    {
      this.pdbeNonObserved = pdbeNonObserved2;
    }

    public int getPdbeNonObserved()
    {
      return pdbeNonObserved;
    }

    public SequenceI getSeq()
    {
      return seq;
    }

    public void setSeq(SequenceI seq)
    {
      this.seq = seq;
    }

    public HashMap<Integer, int[]> getMapping()
    {
      return mapping;
    }

    public void setMapping(HashMap<Integer, int[]> mapping)
    {
      this.mapping = mapping;
    }

    public TreeMap<Integer, String> getResNumMap()
    {
      return resNumMap;
    }

    public void setResNumMap(TreeMap<Integer, String> resNumMap)
    {
      this.resNumMap = resNumMap;
    }

    public List<Integer> getOmitNonObserved()
    {
      return omitNonObserved;
    }

    public void setOmitNonObserved(List<Integer> omitNonObserved)
    {
      this.omitNonObserved = omitNonObserved;
    }

    public int getNonObservedShiftIndex()
    {
      return nonObservedShiftIndex;
    }

    public void setNonObservedShiftIndex(int nonObservedShiftIndex)
    {
      this.nonObservedShiftIndex = nonObservedShiftIndex;
    }

  }

  @Override
  public StringBuilder getMappingOutput(MappingOutputPojo mp)
          throws SiftsException
  {
    String seqRes = mp.getSeqResidue();
    String seqName = mp.getSeqName();
    int sStart = mp.getSeqStart();
    int sEnd = mp.getSeqEnd();

    String strRes = mp.getStrResidue();
    String strName = mp.getStrName();
    int pdbStart = mp.getStrStart();
    int pdbEnd = mp.getStrEnd();

    String type = mp.getType();

    int maxid = (seqName.length() >= strName.length()) ? seqName.length()
            : strName.length();
    int len = 72 - maxid - 1;

    int nochunks = ((seqRes.length()) / len)
            + ((seqRes.length()) % len > 0 ? 1 : 0);
    // output mappings
    StringBuilder output = new StringBuilder(512);
    output.append(NEWLINE);
    output.append("Sequence \u27f7 Structure mapping details")
            .append(NEWLINE);
    output.append("Method: SIFTS");
    output.append(NEWLINE).append(NEWLINE);

    output.append(new Format("%" + maxid + "s").form(seqName));
    output.append(" :  ");
    output.append(String.valueOf(sStart));
    output.append(" - ");
    output.append(String.valueOf(sEnd));
    output.append(" Maps to ");
    output.append(NEWLINE);
    output.append(new Format("%" + maxid + "s").form(structId));
    output.append(" :  ");
    output.append(String.valueOf(pdbStart));
    output.append(" - ");
    output.append(String.valueOf(pdbEnd));
    output.append(NEWLINE).append(NEWLINE);

    ScoreMatrix pam250 = ScoreModels.getInstance().getPam250();
    int matchedSeqCount = 0;
    for (int j = 0; j < nochunks; j++)
    {
      // Print the first aligned sequence
      output.append(new Format("%" + (maxid) + "s").form(seqName))
              .append(" ");

      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < seqRes.length())
        {
          output.append(seqRes.charAt(i + (j * len)));
        }
      }

      output.append(NEWLINE);
      output.append(new Format("%" + (maxid) + "s").form(" ")).append(" ");

      /*
       * Print out the match symbols:
       * | for exact match (ignoring case)
       * . if PAM250 score is positive
       * else a space
       */
      for (int i = 0; i < len; i++)
      {
        try
        {
          if ((i + (j * len)) < seqRes.length())
          {
            char c1 = seqRes.charAt(i + (j * len));
            char c2 = strRes.charAt(i + (j * len));
            boolean sameChar = Comparison.isSameResidue(c1, c2, false);
            if (sameChar && !Comparison.isGap(c1))
            {
              matchedSeqCount++;
              output.append("|");
            }
            else if (type.equals("pep"))
            {
              if (pam250.getPairwiseScore(c1, c2) > 0)
              {
                output.append(".");
              }
              else
              {
                output.append(" ");
              }
            }
            else
            {
              output.append(" ");
            }
          }
        } catch (IndexOutOfBoundsException e)
        {
          continue;
        }
      }
      // Now print the second aligned sequence
      output = output.append(NEWLINE);
      output = output.append(new Format("%" + (maxid) + "s").form(strName))
              .append(" ");
      for (int i = 0; i < len; i++)
      {
        if ((i + (j * len)) < strRes.length())
        {
          output.append(strRes.charAt(i + (j * len)));
        }
      }
      output.append(NEWLINE).append(NEWLINE);
    }
    float pid = (float) matchedSeqCount / seqRes.length() * 100;
    if (pid < SiftsSettings.getFailSafePIDThreshold())
    {
      throw new SiftsException(">>> Low PID detected for SIFTs mapping...");
    }
    output.append("Length of alignment = " + seqRes.length())
            .append(NEWLINE);
    output.append(new Format("Percentage ID = %2.2f").form(pid));
    return output;
  }

  @Override
  public int getEntityCount()
  {
    return siftsEntry.getEntity().size();
  }

  @Override
  public String getDbAccessionId()
  {
    return siftsEntry.getDbAccessionId();
  }

  @Override
  public String getDbCoordSys()
  {
    return siftsEntry.getDbCoordSys();
  }

  @Override
  public String getDbSource()
  {
    return siftsEntry.getDbSource();
  }

  @Override
  public String getDbVersion()
  {
    return siftsEntry.getDbVersion();
  }

  public static void setMockSiftsFile(File file)
  {
    mockSiftsFile = file;
  }

}
