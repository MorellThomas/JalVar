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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import jalview.analysis.AlignmentUtils;
import jalview.analysis.SequenceIdMatcher;
import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.api.FeatureRenderer;
import jalview.api.FeaturesSourceI;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.gui.Desktop;
import jalview.io.gff.GffHelperFactory;
import jalview.io.gff.GffHelperI;
import jalview.schemes.FeatureColour;
import jalview.util.ColorUtils;
import jalview.util.MapList;
import jalview.util.ParseHtmlBodyAndLinks;
import jalview.util.StringUtils;

/**
 * Parses and writes features files, which may be in Jalview, GFF2 or GFF3
 * format. These are tab-delimited formats but with differences in the use of
 * columns.
 * 
 * A Jalview feature file may define feature colours and then declare that the
 * remainder of the file is in GFF format with the line 'GFF'.
 * 
 * GFF3 files may include alignment mappings for features, which Jalview will
 * attempt to model, and may include sequence data following a ##FASTA line.
 * 
 * 
 * @author AMW
 * @author jbprocter
 * @author gmcarstairs
 */
public class FeaturesFile extends AlignFile implements FeaturesSourceI
{
  private static final String EQUALS = "=";

  private static final String TAB_REGEX = "\\t";

  private static final String STARTGROUP = "STARTGROUP";

  private static final String ENDGROUP = "ENDGROUP";

  private static final String STARTFILTERS = "STARTFILTERS";

  private static final String ENDFILTERS = "ENDFILTERS";

  private static final String ID_NOT_SPECIFIED = "ID_NOT_SPECIFIED";

  protected static final String GFF_VERSION = "##gff-version";

  private AlignmentI lastmatchedAl = null;

  private SequenceIdMatcher matcher = null;

  protected AlignmentI dataset;

  protected int gffVersion;

  /**
   * Creates a new FeaturesFile object.
   */
  public FeaturesFile()
  {
  }

  /**
   * Constructor which does not parse the file immediately
   * 
   * @param file
   *          File or String filename
   * @param paste
   * @throws IOException
   */
  public FeaturesFile(Object file, DataSourceType paste) throws IOException
  {
    super(false, file, paste);
  }

  /**
   * @param source
   * @throws IOException
   */
  public FeaturesFile(FileParse source) throws IOException
  {
    super(source);
  }

  /**
   * Constructor that optionally parses the file immediately
   * 
   * @param parseImmediately
   * @param file
   * @param type
   * @throws IOException
   */
  public FeaturesFile(boolean parseImmediately, Object file,
          DataSourceType type) throws IOException
  {
    super(parseImmediately, file, type);
  }

  /**
   * Parse GFF or sequence features file using case-independent matching,
   * discarding URLs
   * 
   * @param align
   *          - alignment/dataset containing sequences that are to be annotated
   * @param colours
   *          - hashtable to store feature colour definitions
   * @param removeHTML
   *          - process html strings into plain text
   * @return true if features were added
   */
  public boolean parse(AlignmentI align,
          Map<String, FeatureColourI> colours, boolean removeHTML)
  {
    return parse(align, colours, removeHTML, false);
  }

  /**
   * Extends the default addProperties by also adding peptide-to-cDNA mappings
   * (if any) derived while parsing a GFF file
   */
  @Override
  public void addProperties(AlignmentI al)
  {
    super.addProperties(al);
    if (dataset != null && dataset.getCodonFrames() != null)
    {
      AlignmentI ds = (al.getDataset() == null) ? al : al.getDataset();
      for (AlignedCodonFrame codons : dataset.getCodonFrames())
      {
        ds.addCodonFrame(codons);
      }
    }
  }

  /**
   * Parse GFF or Jalview format sequence features file
   * 
   * @param align
   *          - alignment/dataset containing sequences that are to be annotated
   * @param colours
   *          - map to store feature colour definitions
   * @param removeHTML
   *          - process html strings into plain text
   * @param relaxedIdmatching
   *          - when true, ID matches to compound sequence IDs are allowed
   * @return true if features were added
   */
  public boolean parse(AlignmentI align,
          Map<String, FeatureColourI> colours, boolean removeHTML,
          boolean relaxedIdmatching)
  {
    return parse(align, colours, null, removeHTML, relaxedIdmatching);
  }

  /**
   * Parse GFF or Jalview format sequence features file
   * 
   * @param align
   *          - alignment/dataset containing sequences that are to be annotated
   * @param colours
   *          - map to store feature colour definitions
   * @param filters
   *          - map to store feature filter definitions
   * @param removeHTML
   *          - process html strings into plain text
   * @param relaxedIdmatching
   *          - when true, ID matches to compound sequence IDs are allowed
   * @return true if features were added
   */
  public boolean parse(AlignmentI align,
          Map<String, FeatureColourI> colours,
          Map<String, FeatureMatcherSetI> filters, boolean removeHTML,
          boolean relaxedIdmatching)
  {
    Map<String, String> gffProps = new HashMap<>();
    /*
     * keep track of any sequences we try to create from the data
     */
    List<SequenceI> newseqs = new ArrayList<>();

    String line = null;
    try
    {
      String[] gffColumns;
      String featureGroup = null;

      while ((line = nextLine()) != null)
      {
        // skip comments/process pragmas
        if (line.length() == 0 || line.startsWith("#"))
        {
          if (line.toLowerCase(Locale.ROOT).startsWith("##"))
          {
            processGffPragma(line, gffProps, align, newseqs);
          }
          continue;
        }

        gffColumns = line.split(TAB_REGEX);
        if (gffColumns.length == 1)
        {
          if (line.trim().equalsIgnoreCase("GFF"))
          {
            /*
             * Jalview features file with appended GFF
             * assume GFF2 (though it may declare ##gff-version 3)
             */
            gffVersion = 2;
            continue;
          }
        }

        if (gffColumns.length > 0 && gffColumns.length < 4)
        {
          /*
           * if 2 or 3 tokens, we anticipate either 'startgroup', 'endgroup' or
           * a feature type colour specification
           */
          String ft = gffColumns[0];
          if (ft.equalsIgnoreCase(STARTFILTERS))
          {
            parseFilters(filters);
            continue;
          }
          if (ft.equalsIgnoreCase(STARTGROUP))
          {
            featureGroup = gffColumns[1];
          }
          else if (ft.equalsIgnoreCase(ENDGROUP))
          {
            // We should check whether this is the current group,
            // but at present there's no way of showing more than 1 group
            featureGroup = null;
          }
          else
          {
            String colscheme = gffColumns[1];
            FeatureColourI colour = FeatureColour
                    .parseJalviewFeatureColour(colscheme);
            if (colour != null)
            {
              colours.put(ft, colour);
            }
          }
          continue;
        }

        /*
         * if not a comment, GFF pragma, startgroup, endgroup or feature
         * colour specification, that just leaves a feature details line
         * in either Jalview or GFF format
         */
        if (gffVersion == 0)
        {
          parseJalviewFeature(line, gffColumns, align, colours, removeHTML,
                  relaxedIdmatching, featureGroup);
        }
        else
        {
          parseGff(gffColumns, align, relaxedIdmatching, newseqs);
        }
      }
      resetMatcher();
    } catch (Exception ex)
    {
      // should report somewhere useful for UI if necessary
      warningMessage = ((warningMessage == null) ? "" : warningMessage)
              + "Parsing error at\n" + line;
      System.out.println("Error parsing feature file: " + ex + "\n" + line);
      ex.printStackTrace(System.err);
      resetMatcher();
      return false;
    }

    /*
     * experimental - add any dummy sequences with features to the alignment
     * - we need them for Ensembl feature extraction - though maybe not otherwise
     */
    for (SequenceI newseq : newseqs)
    {
      if (newseq.getFeatures().hasFeatures())
      {
        align.addSequence(newseq);
      }
    }
    return true;
  }

  /**
   * Reads input lines from STARTFILTERS to ENDFILTERS and adds a feature type
   * filter to the map for each line parsed. After exit from this method,
   * nextLine() should return the line after ENDFILTERS (or we are already at
   * end of file if ENDFILTERS was missing).
   * 
   * @param filters
   * @throws IOException
   */
  protected void parseFilters(Map<String, FeatureMatcherSetI> filters)
          throws IOException
  {
    String line;
    while ((line = nextLine()) != null)
    {
      if (line.toUpperCase(Locale.ROOT).startsWith(ENDFILTERS))
      {
        return;
      }
      String[] tokens = line.split(TAB_REGEX);
      if (tokens.length != 2)
      {
        System.err.println(String.format("Invalid token count %d for %d",
                tokens.length, line));
      }
      else
      {
        String featureType = tokens[0];
        FeatureMatcherSetI fm = FeatureMatcherSet.fromString(tokens[1]);
        if (fm != null && filters != null)
        {
          filters.put(featureType, fm);
        }
      }
    }
  }

  /**
   * Try to parse a Jalview format feature specification and add it as a
   * sequence feature to any matching sequences in the alignment. Returns true
   * if successful (a feature was added), or false if not.
   * 
   * @param line
   * @param gffColumns
   * @param alignment
   * @param featureColours
   * @param removeHTML
   * @param relaxedIdmatching
   * @param featureGroup
   */
  protected boolean parseJalviewFeature(String line, String[] gffColumns,
          AlignmentI alignment, Map<String, FeatureColourI> featureColours,
          boolean removeHTML, boolean relaxedIdMatching,
          String featureGroup)
  {
    /*
     * tokens: description seqid seqIndex start end type [score]
     */
    if (gffColumns.length < 6)
    {
      System.err.println("Ignoring feature line '" + line
              + "' with too few columns (" + gffColumns.length + ")");
      return false;
    }
    String desc = gffColumns[0];
    String seqId = gffColumns[1];
    SequenceI seq = findSequence(seqId, alignment, null, relaxedIdMatching);

    if (!ID_NOT_SPECIFIED.equals(seqId))
    {
      seq = findSequence(seqId, alignment, null, relaxedIdMatching);
    }
    else
    {
      seqId = null;
      seq = null;
      String seqIndex = gffColumns[2];
      try
      {
        int idx = Integer.parseInt(seqIndex);
        seq = alignment.getSequenceAt(idx);
      } catch (NumberFormatException ex)
      {
        System.err.println("Invalid sequence index: " + seqIndex);
      }
    }

    if (seq == null)
    {
      System.out.println("Sequence not found: " + line);
      return false;
    }

    int startPos = Integer.parseInt(gffColumns[3]);
    int endPos = Integer.parseInt(gffColumns[4]);

    String ft = gffColumns[5];

    if (!featureColours.containsKey(ft))
    {
      /* 
       * Perhaps an old style groups file with no colours -
       * synthesize a colour from the feature type
       */
      Color colour = ColorUtils.createColourFromName(ft);
      featureColours.put(ft, new FeatureColour(colour));
    }
    SequenceFeature sf = null;
    if (gffColumns.length > 6)
    {
      float score = Float.NaN;
      try
      {
        score = Float.valueOf(gffColumns[6]).floatValue();
      } catch (NumberFormatException ex)
      {
        sf = new SequenceFeature(ft, desc, startPos, endPos, featureGroup);
      }
      sf = new SequenceFeature(ft, desc, startPos, endPos, score,
              featureGroup);
    }
    else
    {
      sf = new SequenceFeature(ft, desc, startPos, endPos, featureGroup);
    }

    parseDescriptionHTML(sf, removeHTML);

    seq.addSequenceFeature(sf);

    while (seqId != null
            && (seq = alignment.findName(seq, seqId, false)) != null)
    {
      seq.addSequenceFeature(new SequenceFeature(sf));
    }
    return true;
  }

  /**
   * clear any temporary handles used to speed up ID matching
   */
  protected void resetMatcher()
  {
    lastmatchedAl = null;
    matcher = null;
  }

  /**
   * Returns a sequence matching the given id, as follows
   * <ul>
   * <li>strict matching is on exact sequence name</li>
   * <li>relaxed matching allows matching on a token within the sequence name,
   * or a dbxref</li>
   * <li>first tries to find a match in the alignment sequences</li>
   * <li>else tries to find a match in the new sequences already generated while
   * parsing the features file</li>
   * <li>else creates a new placeholder sequence, adds it to the new sequences
   * list, and returns it</li>
   * </ul>
   * 
   * @param seqId
   * @param align
   * @param newseqs
   * @param relaxedIdMatching
   * 
   * @return
   */
  protected SequenceI findSequence(String seqId, AlignmentI align,
          List<SequenceI> newseqs, boolean relaxedIdMatching)
  {
    // TODO encapsulate in SequenceIdMatcher, share the matcher
    // with the GffHelper (removing code duplication)
    SequenceI match = null;
    if (relaxedIdMatching)
    {
      if (lastmatchedAl != align)
      {
        lastmatchedAl = align;
        matcher = new SequenceIdMatcher(align.getSequencesArray());
        if (newseqs != null)
        {
          matcher.addAll(newseqs);
        }
      }
      match = matcher.findIdMatch(seqId);
    }
    else
    {
      match = align.findName(seqId, true);
      if (match == null && newseqs != null)
      {
        for (SequenceI m : newseqs)
        {
          if (seqId.equals(m.getName()))
          {
            return m;
          }
        }
      }

    }
    if (match == null && newseqs != null)
    {
      match = new SequenceDummy(seqId);
      if (relaxedIdMatching)
      {
        matcher.addAll(Arrays.asList(new SequenceI[] { match }));
      }
      // add dummy sequence to the newseqs list
      newseqs.add(match);
    }
    return match;
  }

  public void parseDescriptionHTML(SequenceFeature sf, boolean removeHTML)
  {
    if (sf.getDescription() == null)
    {
      return;
    }
    ParseHtmlBodyAndLinks parsed = new ParseHtmlBodyAndLinks(
            sf.getDescription(), removeHTML, newline);

    if (removeHTML)
    {
      sf.setDescription(parsed.getNonHtmlContent());
    }

    for (String link : parsed.getLinks())
    {
      sf.addLink(link);
    }
  }

  /**
   * Returns contents of a Jalview format features file, for visible features,
   * as filtered by type and group. Features with a null group are displayed if
   * their feature type is visible. Non-positional features may optionally be
   * included (with no check on type or group).
   * 
   * @param sequences
   * @param fr
   * @param includeNonPositional
   *          if true, include non-positional features (regardless of group or
   *          type)
   * @param includeComplement
   *          if true, include visible complementary (CDS/protein) positional
   *          features, with locations converted to local sequence coordinates
   * @return
   */
  public String printJalviewFormat(SequenceI[] sequences,
          FeatureRenderer fr, boolean includeNonPositional,
          boolean includeComplement)
  {
    Map<String, FeatureColourI> visibleColours = fr
            .getDisplayedFeatureCols();
    Map<String, FeatureMatcherSetI> featureFilters = fr.getFeatureFilters();

    /*
     * write out feature colours (if we know them)
     */
    // TODO: decide if feature links should also be written here ?
    StringBuilder out = new StringBuilder(256);
    if (visibleColours != null)
    {
      for (Entry<String, FeatureColourI> featureColour : visibleColours
              .entrySet())
      {
        FeatureColourI colour = featureColour.getValue();
        out.append(colour.toJalviewFormat(featureColour.getKey()))
                .append(newline);
      }
    }

    String[] types = visibleColours == null ? new String[0]
            : visibleColours.keySet()
                    .toArray(new String[visibleColours.keySet().size()]);

    /*
     * feature filters if any
     */
    outputFeatureFilters(out, visibleColours, featureFilters);

    /*
     * output features within groups
     */
    int count = outputFeaturesByGroup(out, fr, types, sequences,
            includeNonPositional);

    if (includeComplement)
    {
      count += outputComplementFeatures(out, fr, sequences);
    }

    return count > 0 ? out.toString() : "No Features Visible";
  }

  /**
   * Outputs any visible complementary (CDS/peptide) positional features as
   * Jalview format, within feature group. The coordinates of the linked
   * features are converted to the corresponding positions of the local
   * sequences.
   * 
   * @param out
   * @param fr
   * @param sequences
   * @return
   */
  private int outputComplementFeatures(StringBuilder out,
          FeatureRenderer fr, SequenceI[] sequences)
  {
    AlignViewportI comp = fr.getViewport().getCodingComplement();
    FeatureRenderer fr2 = Desktop.getAlignFrameFor(comp)
            .getFeatureRenderer();

    /*
     * bin features by feature group and sequence
     */
    Map<String, Map<String, List<SequenceFeature>>> map = new TreeMap<>(
            String.CASE_INSENSITIVE_ORDER);
    int count = 0;

    for (SequenceI seq : sequences)
    {
      /*
       * find complementary features
       */
      List<SequenceFeature> complementary = findComplementaryFeatures(seq,
              fr2);
      String seqName = seq.getName();

      for (SequenceFeature sf : complementary)
      {
        String group = sf.getFeatureGroup();
        if (!map.containsKey(group))
        {
          map.put(group, new LinkedHashMap<>()); // preserves sequence order
        }
        Map<String, List<SequenceFeature>> groupFeatures = map.get(group);
        if (!groupFeatures.containsKey(seqName))
        {
          groupFeatures.put(seqName, new ArrayList<>());
        }
        List<SequenceFeature> foundFeatures = groupFeatures.get(seqName);
        foundFeatures.add(sf);
        count++;
      }
    }

    /*
     * output features by group
     */
    for (Entry<String, Map<String, List<SequenceFeature>>> groupFeatures : map
            .entrySet())
    {
      out.append(newline);
      String group = groupFeatures.getKey();
      if (!"".equals(group))
      {
        out.append(STARTGROUP).append(TAB).append(group).append(newline);
      }
      Map<String, List<SequenceFeature>> seqFeaturesMap = groupFeatures
              .getValue();
      for (Entry<String, List<SequenceFeature>> seqFeatures : seqFeaturesMap
              .entrySet())
      {
        String sequenceName = seqFeatures.getKey();
        for (SequenceFeature sf : seqFeatures.getValue())
        {
          formatJalviewFeature(out, sequenceName, sf);
        }
      }
      if (!"".equals(group))
      {
        out.append(ENDGROUP).append(TAB).append(group).append(newline);
      }
    }

    return count;
  }

  /**
   * Answers a list of mapped features visible in the (CDS/protein) complement,
   * with feature positions translated to local sequence coordinates
   * 
   * @param seq
   * @param fr2
   * @return
   */
  protected List<SequenceFeature> findComplementaryFeatures(SequenceI seq,
          FeatureRenderer fr2)
  {
    /*
     * avoid duplication of features (e.g. peptide feature 
     * at all 3 mapped codon positions)
     */
    List<SequenceFeature> found = new ArrayList<>();
    List<SequenceFeature> complementary = new ArrayList<>();

    for (int pos = seq.getStart(); pos <= seq.getEnd(); pos++)
    {
      MappedFeatures mf = fr2.findComplementFeaturesAtResidue(seq, pos);

      if (mf != null)
      {
        for (SequenceFeature sf : mf.features)
        {
          /*
           * make a virtual feature with local coordinates
           */
          if (!found.contains(sf))
          {
            String group = sf.getFeatureGroup();
            if (group == null)
            {
              group = "";
            }
            found.add(sf);
            int begin = sf.getBegin();
            int end = sf.getEnd();
            int[] range = mf.getMappedPositions(begin, end);
            SequenceFeature sf2 = new SequenceFeature(sf, range[0],
                    range[1], group, sf.getScore());
            complementary.add(sf2);
          }
        }
      }
    }

    return complementary;
  }

  /**
   * Outputs any feature filters defined for visible feature types, sandwiched
   * by STARTFILTERS and ENDFILTERS lines
   * 
   * @param out
   * @param visible
   * @param featureFilters
   */
  void outputFeatureFilters(StringBuilder out,
          Map<String, FeatureColourI> visible,
          Map<String, FeatureMatcherSetI> featureFilters)
  {
    if (visible == null || featureFilters == null
            || featureFilters.isEmpty())
    {
      return;
    }

    boolean first = true;
    for (String featureType : visible.keySet())
    {
      FeatureMatcherSetI filter = featureFilters.get(featureType);
      if (filter != null)
      {
        if (first)
        {
          first = false;
          out.append(newline).append(STARTFILTERS).append(newline);
        }
        out.append(featureType).append(TAB).append(filter.toStableString())
                .append(newline);
      }
    }
    if (!first)
    {
      out.append(ENDFILTERS).append(newline);
    }

  }

  /**
   * Appends output of visible sequence features within feature groups to the
   * output buffer. Groups other than the null or empty group are sandwiched by
   * STARTGROUP and ENDGROUP lines. Answers the number of features written.
   * 
   * @param out
   * @param fr
   * @param featureTypes
   * @param sequences
   * @param includeNonPositional
   * @return
   */
  private int outputFeaturesByGroup(StringBuilder out, FeatureRenderer fr,
          String[] featureTypes, SequenceI[] sequences,
          boolean includeNonPositional)
  {
    List<String> featureGroups = fr.getFeatureGroups();

    /*
     * sort groups alphabetically, and ensure that features with a
     * null or empty group are output after those in named groups
     */
    List<String> sortedGroups = new ArrayList<>(featureGroups);
    sortedGroups.remove(null);
    sortedGroups.remove("");
    Collections.sort(sortedGroups);
    sortedGroups.add(null);
    sortedGroups.add("");

    int count = 0;
    List<String> visibleGroups = fr.getDisplayedFeatureGroups();

    /*
     * loop over all groups (may be visible or not);
     * non-positional features are output even if group is not visible
     */
    for (String group : sortedGroups)
    {
      boolean firstInGroup = true;
      boolean isNullGroup = group == null || "".equals(group);

      for (int i = 0; i < sequences.length; i++)
      {
        String sequenceName = sequences[i].getName();
        List<SequenceFeature> features = new ArrayList<>();

        /*
         * get any non-positional features in this group, if wanted
         * (for any feature type, whether visible or not)
         */
        if (includeNonPositional)
        {
          features.addAll(sequences[i].getFeatures()
                  .getFeaturesForGroup(false, group));
        }

        /*
         * add positional features for visible feature types, but
         * (for named groups) only if feature group is visible
         */
        if (featureTypes.length > 0
                && (isNullGroup || visibleGroups.contains(group)))
        {
          features.addAll(sequences[i].getFeatures()
                  .getFeaturesForGroup(true, group, featureTypes));
        }

        for (SequenceFeature sf : features)
        {
          if (sf.isNonPositional() || fr.isVisible(sf))
          {
            count++;
            if (firstInGroup)
            {
              out.append(newline);
              if (!isNullGroup)
              {
                out.append(STARTGROUP).append(TAB).append(group)
                        .append(newline);
              }
            }
            firstInGroup = false;
            formatJalviewFeature(out, sequenceName, sf);
          }
        }
      }

      if (!isNullGroup && !firstInGroup)
      {
        out.append(ENDGROUP).append(TAB).append(group).append(newline);
      }
    }
    return count;
  }

  /**
   * Formats one feature in Jalview format and appends to the string buffer
   * 
   * @param out
   * @param sequenceName
   * @param sequenceFeature
   */
  protected void formatJalviewFeature(StringBuilder out,
          String sequenceName, SequenceFeature sequenceFeature)
  {
    if (sequenceFeature.description == null
            || sequenceFeature.description.equals(""))
    {
      out.append(sequenceFeature.type).append(TAB);
    }
    else
    {
      if (sequenceFeature.links != null
              && sequenceFeature.getDescription().indexOf("<html>") == -1)
      {
        out.append("<html>");
      }

      out.append(sequenceFeature.description);
      if (sequenceFeature.links != null)
      {
        for (int l = 0; l < sequenceFeature.links.size(); l++)
        {
          String label = sequenceFeature.links.elementAt(l);
          String href = label.substring(label.indexOf("|") + 1);
          label = label.substring(0, label.indexOf("|"));

          if (sequenceFeature.description.indexOf(href) == -1)
          {
            out.append(" <a href=\"").append(href).append("\">")
                    .append(label).append("</a>");
          }
        }

        if (sequenceFeature.getDescription().indexOf("</html>") == -1)
        {
          out.append("</html>");
        }
      }

      out.append(TAB);
    }
    out.append(sequenceName);
    out.append("\t-1\t");
    out.append(sequenceFeature.begin);
    out.append(TAB);
    out.append(sequenceFeature.end);
    out.append(TAB);
    out.append(sequenceFeature.type);
    if (!Float.isNaN(sequenceFeature.score))
    {
      out.append(TAB);
      out.append(sequenceFeature.score);
    }
    out.append(newline);
  }

  /**
   * Parse method that is called when a GFF file is dragged to the desktop
   */
  @Override
  public void parse()
  {
    AlignViewportI av = getViewport();
    if (av != null)
    {
      if (av.getAlignment() != null)
      {
        dataset = av.getAlignment().getDataset();
      }
      if (dataset == null)
      {
        // working in the applet context ?
        dataset = av.getAlignment();
      }
    }
    else
    {
      dataset = new Alignment(new SequenceI[] {});
    }

    Map<String, FeatureColourI> featureColours = new HashMap<>();
    boolean parseResult = parse(dataset, featureColours, false, true);
    if (!parseResult)
    {
      // pass error up somehow
    }
    if (av != null)
    {
      // update viewport with the dataset data ?
    }
    else
    {
      setSeqs(dataset.getSequencesArray());
    }
  }

  /**
   * Implementation of unused abstract method
   * 
   * @return error message
   */
  @Override
  public String print(SequenceI[] sqs, boolean jvsuffix)
  {
    System.out.println("Use printGffFormat() or printJalviewFormat()");
    return null;
  }

  /**
   * Returns features output in GFF2 format
   * 
   * @param sequences
   *          the sequences whose features are to be output
   * @param visible
   *          a map whose keys are the type names of visible features
   * @param visibleFeatureGroups
   * @param includeNonPositionalFeatures
   * @param includeComplement
   * @return
   */
  public String printGffFormat(SequenceI[] sequences, FeatureRenderer fr,
          boolean includeNonPositionalFeatures, boolean includeComplement)
  {
    FeatureRenderer fr2 = null;
    if (includeComplement)
    {
      AlignViewportI comp = fr.getViewport().getCodingComplement();
      fr2 = Desktop.getAlignFrameFor(comp).getFeatureRenderer();
    }

    Map<String, FeatureColourI> visibleColours = fr
            .getDisplayedFeatureCols();

    StringBuilder out = new StringBuilder(256);

    out.append(String.format("%s %d\n", GFF_VERSION,
            gffVersion == 0 ? 2 : gffVersion));

    String[] types = visibleColours == null ? new String[0]
            : visibleColours.keySet()
                    .toArray(new String[visibleColours.keySet().size()]);

    for (SequenceI seq : sequences)
    {
      List<SequenceFeature> seqFeatures = new ArrayList<>();
      List<SequenceFeature> features = new ArrayList<>();
      if (includeNonPositionalFeatures)
      {
        features.addAll(seq.getFeatures().getNonPositionalFeatures());
      }
      if (visibleColours != null && !visibleColours.isEmpty())
      {
        features.addAll(seq.getFeatures().getPositionalFeatures(types));
      }
      for (SequenceFeature sf : features)
      {
        if (sf.isNonPositional() || fr.isVisible(sf))
        {
          /*
           * drop features hidden by group visibility, colour threshold,
           * or feature filter condition
           */
          seqFeatures.add(sf);
        }
      }

      if (includeComplement)
      {
        seqFeatures.addAll(findComplementaryFeatures(seq, fr2));
      }

      /*
       * sort features here if wanted
       */
      for (SequenceFeature sf : seqFeatures)
      {
        formatGffFeature(out, seq, sf);
        out.append(newline);
      }
    }

    return out.toString();
  }

  /**
   * Formats one feature as GFF and appends to the string buffer
   */
  private void formatGffFeature(StringBuilder out, SequenceI seq,
          SequenceFeature sf)
  {
    String source = sf.featureGroup;
    if (source == null)
    {
      source = sf.getDescription();
    }

    out.append(seq.getName());
    out.append(TAB);
    out.append(source);
    out.append(TAB);
    out.append(sf.type);
    out.append(TAB);
    out.append(sf.begin);
    out.append(TAB);
    out.append(sf.end);
    out.append(TAB);
    out.append(sf.score);
    out.append(TAB);

    int strand = sf.getStrand();
    out.append(strand == 1 ? "+" : (strand == -1 ? "-" : "."));
    out.append(TAB);

    String phase = sf.getPhase();
    out.append(phase == null ? "." : phase);

    if (sf.otherDetails != null && !sf.otherDetails.isEmpty())
    {
      Map<String, Object> map = sf.otherDetails;
      formatAttributes(out, map);
    }
  }

  /**
   * A helper method that outputs attributes stored in the map as
   * semicolon-delimited values e.g.
   * 
   * <pre>
   * AC_Male=0;AF_NFE=0.00000e 00;Hom_FIN=0;GQ_MEDIAN=9
   * </pre>
   * 
   * A map-valued attribute is formatted as a comma-delimited list within
   * braces, for example
   * 
   * <pre>
   * jvmap_CSQ={ALLELE_NUM=1,UNIPARC=UPI0002841053,Feature=ENST00000585561}
   * </pre>
   * 
   * The {@code jvmap_} prefix designates a values map and is removed if the
   * value is parsed when read in. (The GFF3 specification allows
   * 'semi-structured data' to be represented provided the attribute name begins
   * with a lower case letter.)
   * 
   * @param sb
   * @param map
   * @see http://gmod.org/wiki/GFF3#GFF3_Format
   */
  void formatAttributes(StringBuilder sb, Map<String, Object> map)
  {
    sb.append(TAB);
    boolean first = true;
    for (String key : map.keySet())
    {
      if (SequenceFeature.STRAND.equals(key)
              || SequenceFeature.PHASE.equals(key))
      {
        /*
         * values stashed in map but output to their own columns
         */
        continue;
      }
      {
        if (!first)
        {
          sb.append(";");
        }
      }
      first = false;
      Object value = map.get(key);
      if (value instanceof Map<?, ?>)
      {
        formatMapAttribute(sb, key, (Map<?, ?>) value);
      }
      else
      {
        String formatted = StringUtils.urlEncode(value.toString(),
                GffHelperI.GFF_ENCODABLE);
        sb.append(key).append(EQUALS).append(formatted);
      }
    }
  }

  /**
   * Formats the map entries as
   * 
   * <pre>
   * key=key1=value1,key2=value2,...
   * </pre>
   * 
   * and appends this to the string buffer
   * 
   * @param sb
   * @param key
   * @param map
   */
  private void formatMapAttribute(StringBuilder sb, String key,
          Map<?, ?> map)
  {
    if (map == null || map.isEmpty())
    {
      return;
    }

    /*
     * AbstractMap.toString would be a shortcut here, but more reliable
     * to code the required format in case toString changes in future
     */
    sb.append(key).append(EQUALS);
    boolean first = true;
    for (Entry<?, ?> entry : map.entrySet())
    {
      if (!first)
      {
        sb.append(",");
      }
      first = false;
      sb.append(entry.getKey().toString()).append(EQUALS);
      String formatted = StringUtils.urlEncode(entry.getValue().toString(),
              GffHelperI.GFF_ENCODABLE);
      sb.append(formatted);
    }
  }

  /**
   * Returns a mapping given list of one or more Align descriptors (exonerate
   * format)
   * 
   * @param alignedRegions
   *          a list of "Align fromStart toStart fromCount"
   * @param mapIsFromCdna
   *          if true, 'from' is dna, else 'from' is protein
   * @param strand
   *          either 1 (forward) or -1 (reverse)
   * @return
   * @throws IOException
   */
  protected MapList constructCodonMappingFromAlign(
          List<String> alignedRegions, boolean mapIsFromCdna, int strand)
          throws IOException
  {
    if (strand == 0)
    {
      throw new IOException(
              "Invalid strand for a codon mapping (cannot be 0)");
    }
    int regions = alignedRegions.size();
    // arrays to hold [start, end] for each aligned region
    int[] fromRanges = new int[regions * 2]; // from dna
    int[] toRanges = new int[regions * 2]; // to protein
    int fromRangesIndex = 0;
    int toRangesIndex = 0;

    for (String range : alignedRegions)
    {
      /* 
       * Align mapFromStart mapToStart mapFromCount
       * e.g. if mapIsFromCdna
       *     Align 11270 143 120
       * means:
       *     120 bases from pos 11270 align to pos 143 in peptide
       * if !mapIsFromCdna this would instead be
       *     Align 143 11270 40 
       */
      String[] tokens = range.split(" ");
      if (tokens.length != 3)
      {
        throw new IOException("Wrong number of fields for Align");
      }
      int fromStart = 0;
      int toStart = 0;
      int fromCount = 0;
      try
      {
        fromStart = Integer.parseInt(tokens[0]);
        toStart = Integer.parseInt(tokens[1]);
        fromCount = Integer.parseInt(tokens[2]);
      } catch (NumberFormatException nfe)
      {
        throw new IOException(
                "Invalid number in Align field: " + nfe.getMessage());
      }

      /*
       * Jalview always models from dna to protein, so adjust values if the
       * GFF mapping is from protein to dna
       */
      if (!mapIsFromCdna)
      {
        fromCount *= 3;
        int temp = fromStart;
        fromStart = toStart;
        toStart = temp;
      }
      fromRanges[fromRangesIndex++] = fromStart;
      fromRanges[fromRangesIndex++] = fromStart + strand * (fromCount - 1);

      /*
       * If a codon has an intron gap, there will be contiguous 'toRanges';
       * this is handled for us by the MapList constructor. 
       * (It is not clear that exonerate ever generates this case)  
       */
      toRanges[toRangesIndex++] = toStart;
      toRanges[toRangesIndex++] = toStart + (fromCount - 1) / 3;
    }

    return new MapList(fromRanges, toRanges, 3, 1);
  }

  /**
   * Parse a GFF format feature. This may include creating a 'dummy' sequence to
   * hold the feature, or for its mapped sequence, or both, to be resolved
   * either later in the GFF file (##FASTA section), or when the user loads
   * additional sequences.
   * 
   * @param gffColumns
   * @param alignment
   * @param relaxedIdMatching
   * @param newseqs
   * @return
   */
  protected SequenceI parseGff(String[] gffColumns, AlignmentI alignment,
          boolean relaxedIdMatching, List<SequenceI> newseqs)
  {
    /*
     * GFF: seqid source type start end score strand phase [attributes]
     */
    if (gffColumns.length < 5)
    {
      System.err.println("Ignoring GFF feature line with too few columns ("
              + gffColumns.length + ")");
      return null;
    }

    /*
     * locate referenced sequence in alignment _or_ 
     * as a forward or external reference (SequenceDummy)
     */
    String seqId = gffColumns[0];
    SequenceI seq = findSequence(seqId, alignment, newseqs,
            relaxedIdMatching);

    SequenceFeature sf = null;
    GffHelperI helper = GffHelperFactory.getHelper(gffColumns);
    if (helper != null)
    {
      try
      {
        sf = helper.processGff(seq, gffColumns, alignment, newseqs,
                relaxedIdMatching);
        if (sf != null)
        {
          seq.addSequenceFeature(sf);
          while ((seq = alignment.findName(seq, seqId, true)) != null)
          {
            seq.addSequenceFeature(new SequenceFeature(sf));
          }
        }
      } catch (IOException e)
      {
        System.err.println("GFF parsing failed with: " + e.getMessage());
        return null;
      }
    }

    return seq;
  }

  /**
   * After encountering ##fasta in a GFF3 file, process the remainder of the
   * file as FAST sequence data. Any placeholder sequences created during
   * feature parsing are updated with the actual sequences.
   * 
   * @param align
   * @param newseqs
   * @throws IOException
   */
  protected void processAsFasta(AlignmentI align, List<SequenceI> newseqs)
          throws IOException
  {
    try
    {
      mark();
    } catch (IOException q)
    {
    }
    // Opening a FastaFile object with the remainder of this object's dataIn.
    // Tell the constructor to NOT close the dataIn when finished.
    FastaFile parser = new FastaFile(this, false);
    List<SequenceI> includedseqs = parser.getSeqs();

    SequenceIdMatcher smatcher = new SequenceIdMatcher(newseqs);

    /*
     * iterate over includedseqs, and replacing matching ones with newseqs
     * sequences. Generic iterator not used here because we modify
     * includedseqs as we go
     */
    for (int p = 0, pSize = includedseqs.size(); p < pSize; p++)
    {
      // search for any dummy seqs that this sequence can be used to update
      SequenceI includedSeq = includedseqs.get(p);
      SequenceI dummyseq = smatcher.findIdMatch(includedSeq);
      if (dummyseq != null && dummyseq instanceof SequenceDummy)
      {
        // probably have the pattern wrong
        // idea is that a flyweight proxy for a sequence ID can be created for
        // 1. stable reference creation
        // 2. addition of annotation
        // 3. future replacement by a real sequence
        // current pattern is to create SequenceDummy objects - a convenience
        // constructor for a Sequence.
        // problem is that when promoted to a real sequence, all references
        // need to be updated somehow. We avoid that by keeping the same object.
        ((SequenceDummy) dummyseq).become(includedSeq);
        dummyseq.createDatasetSequence();

        /*
         * Update mappings so they are now to the dataset sequence
         */
        for (AlignedCodonFrame mapping : align.getCodonFrames())
        {
          mapping.updateToDataset(dummyseq);
        }

        /*
         * replace parsed sequence with the realised forward reference
         */
        includedseqs.set(p, dummyseq);

        /*
         * and remove from the newseqs list
         */
        newseqs.remove(dummyseq);
      }
    }

    /*
     * finally add sequences to the dataset
     */
    for (SequenceI seq : includedseqs)
    {
      // experimental: mapping-based 'alignment' to query sequence
      AlignmentUtils.alignSequenceAs(seq, align,
              String.valueOf(align.getGapCharacter()), false, true);

      // rename sequences if GFF handler requested this
      // TODO a more elegant way e.g. gffHelper.postProcess(newseqs) ?
      List<SequenceFeature> sfs = seq.getFeatures().getPositionalFeatures();
      if (!sfs.isEmpty())
      {
        String newName = (String) sfs.get(0)
                .getValue(GffHelperI.RENAME_TOKEN);
        if (newName != null)
        {
          seq.setName(newName);
        }
      }
      align.addSequence(seq);
    }
  }

  /**
   * Process a ## directive
   * 
   * @param line
   * @param gffProps
   * @param align
   * @param newseqs
   * @throws IOException
   */
  protected void processGffPragma(String line, Map<String, String> gffProps,
          AlignmentI align, List<SequenceI> newseqs) throws IOException
  {
    line = line.trim();
    if ("###".equals(line))
    {
      // close off any open 'forward references'
      return;
    }

    String[] tokens = line.substring(2).split(" ");
    String pragma = tokens[0];
    String value = tokens.length == 1 ? null : tokens[1];

    if ("gff-version".equalsIgnoreCase(pragma))
    {
      if (value != null)
      {
        try
        {
          // value may be e.g. "3.1.2"
          gffVersion = Integer.parseInt(value.split("\\.")[0]);
        } catch (NumberFormatException e)
        {
          // ignore
        }
      }
    }
    else if ("sequence-region".equalsIgnoreCase(pragma))
    {
      // could capture <seqid start end> if wanted here
    }
    else if ("feature-ontology".equalsIgnoreCase(pragma))
    {
      // should resolve against the specified feature ontology URI
    }
    else if ("attribute-ontology".equalsIgnoreCase(pragma))
    {
      // URI of attribute ontology - not currently used in GFF3
    }
    else if ("source-ontology".equalsIgnoreCase(pragma))
    {
      // URI of source ontology - not currently used in GFF3
    }
    else if ("species-build".equalsIgnoreCase(pragma))
    {
      // save URI of specific NCBI taxon version of annotations
      gffProps.put("species-build", value);
    }
    else if ("fasta".equalsIgnoreCase(pragma))
    {
      // process the rest of the file as a fasta file and replace any dummy
      // sequence IDs
      processAsFasta(align, newseqs);
    }
    else
    {
      System.err.println("Ignoring unknown pragma: " + line);
    }
  }
}
