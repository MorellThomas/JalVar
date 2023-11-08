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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jalview.api.FeatureColourI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;
import jalview.util.StringUtils;
import jalview.util.UrlLink;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;

/**
 * generate HTML reports for a sequence
 * 
 * @author jimp
 */
public class SequenceAnnotationReport
{
  private static final int MAX_DESCRIPTION_LENGTH = 40;

  private static final String COMMA = ",";

  private static final String ELLIPSIS = "...";

  private static final int MAX_REFS_PER_SOURCE = 4;

  private static final int MAX_SOURCES = 40;

  private static String linkImageURL;

  // public static final String[][] PRIMARY_SOURCES moved to DBRefSource.java

  /*
   * Comparator to order DBRefEntry by Source + accession id (case-insensitive),
   * with 'Primary' sources placed before others, and 'chromosome' first of all
   */
  private static Comparator<DBRefEntry> comparator = new Comparator<DBRefEntry>()
  {

    @Override
    public int compare(DBRefEntry ref1, DBRefEntry ref2)
    {
      if (ref1 instanceof GeneLociI)
      {
        return -1;
      }
      if (ref2 instanceof GeneLociI)
      {
        return 1;
      }
      String s1 = ref1.getSource();
      String s2 = ref2.getSource();
      boolean s1Primary = DBRefSource.isPrimarySource(s1);
      boolean s2Primary = DBRefSource.isPrimarySource(s2);
      if (s1Primary && !s2Primary)
      {
        return -1;
      }
      if (!s1Primary && s2Primary)
      {
        return 1;
      }
      int comp = s1 == null ? -1
              : (s2 == null ? 1 : s1.compareToIgnoreCase(s2));
      if (comp == 0)
      {
        String a1 = ref1.getAccessionId();
        String a2 = ref2.getAccessionId();
        comp = a1 == null ? -1
                : (a2 == null ? 1 : a1.compareToIgnoreCase(a2));
      }
      return comp;
    }

    // private boolean isPrimarySource(String source)
    // {
    // for (String[] primary : DBRefSource.PRIMARY_SOURCES)
    // {
    // for (String s : primary)
    // {
    // if (source.equals(s))
    // {
    // return true;
    // }
    // }
    // }
    // return false;
    // }
  };

  private boolean forTooltip;

  /**
   * Constructor given a flag which affects behaviour
   * <ul>
   * <li>if true, generates feature details suitable to show in a tooltip</li>
   * <li>if false, generates feature details in a form suitable for the sequence
   * details report</li>
   * </ul>
   * 
   * @param isForTooltip
   */
  public SequenceAnnotationReport(boolean isForTooltip)
  {
    this.forTooltip = isForTooltip;
    if (linkImageURL == null)
    {
      linkImageURL = getClass().getResource("/images/link.gif").toString();
    }
  }

  /**
   * Append text for the list of features to the tooltip. Returns the number of
   * features not added if maxlength limit is (or would have been) reached.
   * 
   * @param sb
   * @param residuePos
   * @param features
   * @param minmax
   * @param maxlength
   */
  public int appendFeatures(final StringBuilder sb, int residuePos,
          List<SequenceFeature> features, FeatureRendererModel fr,
          int maxlength)
  {
    for (int i = 0; i < features.size(); i++)
    {
      SequenceFeature feature = features.get(i);
      if (appendFeature(sb, residuePos, fr, feature, null, maxlength))
      {
        return features.size() - i;
      }
    }
    return 0;
  }

  /**
   * Appends text for mapped features (e.g. CDS feature for peptide or vice
   * versa) Returns number of features left if maxlength limit is (or would have
   * been) reached.
   * 
   * @param sb
   * @param residuePos
   * @param mf
   * @param fr
   * @param maxlength
   */
  public int appendFeatures(StringBuilder sb, int residuePos,
          MappedFeatures mf, FeatureRendererModel fr, int maxlength)
  {
    for (int i = 0; i < mf.features.size(); i++)
    {
      SequenceFeature feature = mf.features.get(i);
      if (appendFeature(sb, residuePos, fr, feature, mf, maxlength))
      {
        return mf.features.size() - i;
      }
    }
    return 0;
  }

  /**
   * Appends the feature at rpos to the given buffer
   * 
   * @param sb
   * @param rpos
   * @param minmax
   * @param feature
   */
  boolean appendFeature(final StringBuilder sb0, int rpos,
          FeatureRendererModel fr, SequenceFeature feature,
          MappedFeatures mf, int maxlength)
  {
    int begin = feature.getBegin();
    int end = feature.getEnd();

    /*
     * if this is a virtual features, convert begin/end to the
     * coordinates of the sequence it is mapped to
     */
    int[] beginRange = null; // feature start in local coordinates
    int[] endRange = null; // feature end in local coordinates
    if (mf != null)
    {
      if (feature.isContactFeature())
      {
        /*
         * map start and end points individually
         */
        beginRange = mf.getMappedPositions(begin, begin);
        endRange = begin == end ? beginRange
                : mf.getMappedPositions(end, end);
      }
      else
      {
        /*
         * map the feature extent
         */
        beginRange = mf.getMappedPositions(begin, end);
        endRange = beginRange;
      }
      if (beginRange == null || endRange == null)
      {
        // something went wrong
        return false;
      }
      begin = beginRange[0];
      end = endRange[endRange.length - 1];
    }

    StringBuilder sb = new StringBuilder();
    if (feature.isContactFeature())
    {
      /*
       * include if rpos is at start or end position of [mapped] feature
       */
      boolean showContact = (mf == null) && (rpos == begin || rpos == end);
      boolean showMappedContact = (mf != null) && ((rpos >= beginRange[0]
              && rpos <= beginRange[beginRange.length - 1])
              || (rpos >= endRange[0]
                      && rpos <= endRange[endRange.length - 1]));
      if (showContact || showMappedContact)
      {
        if (sb0.length() > 6)
        {
          sb.append("<br/>");
        }
        sb.append(feature.getType()).append(" ").append(begin).append(":")
                .append(end);
      }
      return appendText(sb0, sb, maxlength);
    }

    if (sb0.length() > 6)
    {
      sb.append("<br/>");
    }
    // TODO: remove this hack to display link only features
    boolean linkOnly = feature.getValue("linkonly") != null;
    if (!linkOnly)
    {
      sb.append(feature.getType()).append(" ");
      if (rpos != 0)
      {
        // we are marking a positional feature
        sb.append(begin);
        if (begin != end)
        {
          sb.append(" ").append(end);
        }
      }

      String description = feature.getDescription();
      if (description != null && !description.equals(feature.getType()))
      {
        description = StringUtils.stripHtmlTags(description);

        /*
         * truncate overlong descriptions unless they contain an href
         * before the truncation point (as truncation could leave corrupted html)
         */
        int linkindex = description.toLowerCase(Locale.ROOT).indexOf("<a ");
        boolean hasLink = linkindex > -1
                && linkindex < MAX_DESCRIPTION_LENGTH;
        if (description.length() > MAX_DESCRIPTION_LENGTH && !hasLink)
        {
          description = description.substring(0, MAX_DESCRIPTION_LENGTH)
                  + ELLIPSIS;
        }

        sb.append("; ").append(description);
      }

      if (showScore(feature, fr))
      {
        sb.append(" Score=").append(String.valueOf(feature.getScore()));
      }
      String status = (String) feature.getValue("status");
      if (status != null && status.length() > 0)
      {
        sb.append("; (").append(status).append(")");
      }

      /*
       * add attribute value if coloured by attribute
       */
      if (fr != null)
      {
        FeatureColourI fc = fr.getFeatureColours().get(feature.getType());
        if (fc != null && fc.isColourByAttribute())
        {
          String[] attName = fc.getAttributeName();
          String attVal = feature.getValueAsString(attName);
          if (attVal != null)
          {
            sb.append("; ").append(String.join(":", attName)).append("=")
                    .append(attVal);
          }
        }
      }

      if (mf != null)
      {
        String variants = mf.findProteinVariants(feature);
        if (!variants.isEmpty())
        {
          sb.append(" ").append(variants);
        }
      }
    }
    return appendText(sb0, sb, maxlength);
  }

  /**
   * Appends sb to sb0, and returns false, unless maxlength is not zero and
   * appending would make the result longer than or equal to maxlength, in which
   * case the append is not done and returns true
   * 
   * @param sb0
   * @param sb
   * @param maxlength
   * @return
   */
  private static boolean appendText(StringBuilder sb0, StringBuilder sb,
          int maxlength)
  {
    if (maxlength == 0 || sb0.length() + sb.length() < maxlength)
    {
      sb0.append(sb);
      return false;
    }
    return true;
  }

  /**
   * Answers true if score should be shown, else false. Score is shown if it is
   * not NaN, and the feature type has a non-trivial min-max score range
   */
  boolean showScore(SequenceFeature feature, FeatureRendererModel fr)
  {
    if (Float.isNaN(feature.getScore()))
    {
      return false;
    }
    if (fr == null)
    {
      return true;
    }
    float[][] minMax = fr.getMinMax().get(feature.getType());

    /*
     * minMax[0] is the [min, max] score range for positional features
     */
    if (minMax == null || minMax[0] == null || minMax[0][0] == minMax[0][1])
    {
      return false;
    }
    return true;
  }

  /**
   * Format and appends any hyperlinks for the sequence feature to the string
   * buffer
   * 
   * @param sb
   * @param feature
   */
  void appendLinks(final StringBuffer sb, SequenceFeature feature)
  {
    if (feature.links != null)
    {
      if (linkImageURL != null)
      {
        sb.append(" <img src=\"" + linkImageURL + "\">");
      }
      else
      {
        for (String urlstring : feature.links)
        {
          try
          {
            for (List<String> urllink : createLinksFrom(null, urlstring))
            {
              sb.append("<br/> <a href=\"" + urllink.get(3) + "\" target=\""
                      + urllink.get(0) + "\">"
                      + (urllink.get(0).toLowerCase(Locale.ROOT).equals(
                              urllink.get(1).toLowerCase(Locale.ROOT))
                                      ? urllink.get(0)
                                      : (urllink.get(0) + ":"
                                              + urllink.get(1)))
                      + "</a><br/>");
            }
          } catch (Exception x)
          {
            System.err.println(
                    "problem when creating links from " + urlstring);
            x.printStackTrace();
          }
        }
      }

    }
  }

  /**
   * 
   * @param seq
   * @param link
   * @return Collection< List<String> > { List<String> { link target, link
   *         label, dynamic component inserted (if any), url }}
   */
  Collection<List<String>> createLinksFrom(SequenceI seq, String link)
  {
    Map<String, List<String>> urlSets = new LinkedHashMap<>();
    UrlLink urlLink = new UrlLink(link);
    if (!urlLink.isValid())
    {
      System.err.println(urlLink.getInvalidMessage());
      return null;
    }

    urlLink.createLinksFromSeq(seq, urlSets);

    return urlSets.values();
  }

  public void createSequenceAnnotationReport(final StringBuilder tip,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          FeatureRendererModel fr)
  {
    createSequenceAnnotationReport(tip, sequence, showDbRefs, showNpFeats,
            fr, false);
  }

  /**
   * Builds an html formatted report of sequence details and appends it to the
   * provided buffer.
   * 
   * @param sb
   *          buffer to append report to
   * @param sequence
   *          the sequence the report is for
   * @param showDbRefs
   *          whether to include database references for the sequence
   * @param showNpFeats
   *          whether to include non-positional sequence features
   * @param fr
   * @param summary
   * @return
   */
  int createSequenceAnnotationReport(final StringBuilder sb,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          FeatureRendererModel fr, boolean summary)
  {
    String tmp;
    sb.append("<i>");

    int maxWidth = 0;
    if (sequence.getDescription() != null)
    {
      tmp = sequence.getDescription();
      sb.append(tmp);
      maxWidth = Math.max(maxWidth, tmp.length());
    }
    sb.append("\n");
    SequenceI ds = sequence;
    while (ds.getDatasetSequence() != null)
    {
      ds = ds.getDatasetSequence();
    }

    if (showDbRefs)
    {
      maxWidth = Math.max(maxWidth, appendDbRefs(sb, ds, summary));
    }
    sb.append("\n");

    /*
     * add non-positional features if wanted
     */
    if (showNpFeats)
    {
      for (SequenceFeature sf : sequence.getFeatures()
              .getNonPositionalFeatures())
      {
        int sz = -sb.length();
        appendFeature(sb, 0, fr, sf, null, 0);
        sz += sb.length();
        maxWidth = Math.max(maxWidth, sz);
      }
    }
    sb.append("</i>");
    return maxWidth;
  }

  /**
   * A helper method that appends any DBRefs, returning the maximum line length
   * added
   * 
   * @param sb
   * @param ds
   * @param summary
   * @return
   */
  protected int appendDbRefs(final StringBuilder sb, SequenceI ds,
          boolean summary)
  {
    List<DBRefEntry> dbrefs, dbrefset = ds.getDBRefs();

    if (dbrefset == null)
    {
      return 0;
    }

    // PATCH for JAL-3980 defensive copy

    dbrefs = new ArrayList<DBRefEntry>();

    dbrefs.addAll(dbrefset);

    // note this sorts the refs held on the sequence!
    dbrefs.sort(comparator);
    boolean ellipsis = false;
    String source = null;
    String lastSource = null;
    int countForSource = 0;
    int sourceCount = 0;
    boolean moreSources = false;
    int maxLineLength = 0;
    int lineLength = 0;

    for (DBRefEntry ref : dbrefs)
    {
      source = ref.getSource();
      if (source == null)
      {
        // shouldn't happen
        continue;
      }
      boolean sourceChanged = !source.equals(lastSource);
      if (sourceChanged)
      {
        lineLength = 0;
        countForSource = 0;
        sourceCount++;
      }
      if (sourceCount > MAX_SOURCES && summary)
      {
        ellipsis = true;
        moreSources = true;
        break;
      }
      lastSource = source;
      countForSource++;
      if (countForSource == 1 || !summary)
      {
        sb.append("<br/>\n");
      }
      if (countForSource <= MAX_REFS_PER_SOURCE || !summary)
      {
        String accessionId = ref.getAccessionId();
        lineLength += accessionId.length() + 1;
        if (countForSource > 1 && summary)
        {
          sb.append(",\n ").append(accessionId);
          lineLength++;
        }
        else
        {
          sb.append(source).append(" ").append(accessionId);
          lineLength += source.length();
        }
        maxLineLength = Math.max(maxLineLength, lineLength);
      }
      if (countForSource == MAX_REFS_PER_SOURCE && summary)
      {
        sb.append(COMMA).append(ELLIPSIS);
        ellipsis = true;
      }
    }
    if (moreSources)
    {
      sb.append("<br/>\n").append(source).append(COMMA).append(ELLIPSIS);
    }
    if (ellipsis)
    {
      sb.append("<br/>\n(");
      sb.append(MessageManager.getString("label.output_seq_details"));
      sb.append(")");
    }

    return maxLineLength;
  }

  public void createTooltipAnnotationReport(final StringBuilder tip,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          FeatureRendererModel fr)
  {
    int maxWidth = createSequenceAnnotationReport(tip, sequence, showDbRefs,
            showNpFeats, fr, true);

    if (maxWidth > 60)
    {
      // ? not sure this serves any useful purpose
      // tip.insert(0, "<table width=350 border=0><tr><td>");
      // tip.append("</td></tr></table>");
    }
  }
}
