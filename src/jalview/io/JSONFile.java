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

import jalview.api.AlignExportSettingsI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.ComplexAlignFile;
import jalview.api.FeatureRenderer;
import jalview.api.FeatureSettingsModelI;
import jalview.api.FeaturesDisplayedI;
import jalview.bin.BuildDetails;
import jalview.datamodel.AlignExportSettingsAdapter;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.HiddenSequences;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.json.binding.biojson.v1.AlignmentAnnotationPojo;
import jalview.json.binding.biojson.v1.AlignmentPojo;
import jalview.json.binding.biojson.v1.AnnotationDisplaySettingPojo;
import jalview.json.binding.biojson.v1.AnnotationPojo;
import jalview.json.binding.biojson.v1.ColourSchemeMapper;
import jalview.json.binding.biojson.v1.SequenceFeaturesPojo;
import jalview.json.binding.biojson.v1.SequenceGrpPojo;
import jalview.json.binding.biojson.v1.SequencePojo;
import jalview.renderer.seqfeatures.FeatureColourFinder;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.JalviewColourScheme;
import jalview.schemes.ResidueColourScheme;
import jalview.util.ColorUtils;
import jalview.util.Format;
import jalview.util.JSONUtils;
import jalview.viewmodel.seqfeatures.FeaturesDisplayed;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class JSONFile extends AlignFile implements ComplexAlignFile
{
  private static String version = new BuildDetails().getVersion();

  private String webstartUrl = "https://www.jalview.org/services/launchApp";

  private String application = "Jalview";

  private String globalColourScheme;

  private boolean showSeqFeatures;

  private Hashtable<String, Sequence> seqMap;

  private FeaturesDisplayedI displayedFeatures;

  private FeatureRenderer fr;

  private HiddenColumns hiddenColumns;

  private List<String> hiddenSeqRefs;

  private ArrayList<SequenceI> hiddenSequences;

  private final static String TCOFFEE_SCORE = "TCoffeeScore";

  public JSONFile()
  {
    super();
  }

  public JSONFile(FileParse source) throws IOException
  {
    super(source);
  }

  public JSONFile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  @Override
  public void parse() throws IOException
  {
    parse(getReader());

  }

  @Override
  public String print(SequenceI[] sqs, boolean jvsuffix)
  {
    String jsonOutput = null;
    try
    {
      AlignmentPojo jsonAlignmentPojo = new AlignmentPojo();
      AlignExportSettingsI exportSettings = getExportSettings();

      /*
       * if no export settings were supplied, provide an 'export all' setting
       */
      if (exportSettings == null)
      {
        exportSettings = new AlignExportSettingsAdapter(true);
      }

      int count = 0;
      for (SequenceI seq : sqs)
      {
        StringBuilder name = new StringBuilder();
        name.append(seq.getName()).append("/").append(seq.getStart())
                .append("-").append(seq.getEnd());
        SequencePojo jsonSeqPojo = new SequencePojo();
        jsonSeqPojo.setId(String.valueOf(seq.hashCode()));
        jsonSeqPojo.setOrder(++count);
        jsonSeqPojo.setEnd(seq.getEnd());
        jsonSeqPojo.setStart(seq.getStart());
        jsonSeqPojo.setName(name.toString());
        jsonSeqPojo.setSeq(seq.getSequenceAsString());
        jsonAlignmentPojo.getSeqs().add(jsonSeqPojo);
      }
      jsonAlignmentPojo.setGlobalColorScheme(globalColourScheme);
      jsonAlignmentPojo.getAppSettings().put("application", application);
      jsonAlignmentPojo.getAppSettings().put("version", version);
      jsonAlignmentPojo.getAppSettings().put("webStartUrl", webstartUrl);
      jsonAlignmentPojo.getAppSettings().put("showSeqFeatures",
              String.valueOf(showSeqFeatures));

      String[] hiddenSections = getHiddenSections();
      if (hiddenSections != null)
      {
        if (hiddenSections[0] != null
                && exportSettings.isExportHiddenColumns())
        {
          jsonAlignmentPojo.getAppSettings().put("hiddenCols",
                  String.valueOf(hiddenSections[0]));
        }
        if (hiddenSections[1] != null
                && exportSettings.isExportHiddenSequences())
        {
          jsonAlignmentPojo.getAppSettings().put("hiddenSeqs",
                  String.valueOf(hiddenSections[1]));
        }
      }

      if (exportSettings.isExportAnnotations())
      {
        jsonAlignmentPojo
                .setAlignAnnotation(annotationToJsonPojo(annotations));
      }
      else
      {
        // These color schemes require annotation, disable them if annotations
        // are not exported
        if (globalColourScheme
                .equalsIgnoreCase(JalviewColourScheme.RNAHelices.toString())
                || globalColourScheme.equalsIgnoreCase(
                        JalviewColourScheme.TCoffee.toString()))
        {
          jsonAlignmentPojo.setGlobalColorScheme(ResidueColourScheme.NONE);
        }
      }

      if (exportSettings.isExportFeatures())
      {
        jsonAlignmentPojo.setSeqFeatures(sequenceFeatureToJsonPojo(sqs));
      }

      if (exportSettings.isExportGroups() && seqGroups != null
              && seqGroups.size() > 0)
      {
        for (SequenceGroup seqGrp : seqGroups)
        {
          SequenceGrpPojo seqGrpPojo = new SequenceGrpPojo();
          seqGrpPojo.setGroupName(seqGrp.getName());
          seqGrpPojo.setColourScheme(ColourSchemeProperty
                  .getColourName(seqGrp.getColourScheme()));
          seqGrpPojo.setColourText(seqGrp.getColourText());
          seqGrpPojo.setDescription(seqGrp.getDescription());
          seqGrpPojo.setDisplayBoxes(seqGrp.getDisplayBoxes());
          seqGrpPojo.setDisplayText(seqGrp.getDisplayText());
          seqGrpPojo.setEndRes(seqGrp.getEndRes());
          seqGrpPojo.setStartRes(seqGrp.getStartRes());
          seqGrpPojo.setShowNonconserved(seqGrp.getShowNonconserved());
          for (SequenceI seq : seqGrp.getSequences())
          {
            seqGrpPojo.getSequenceRefs()
                    .add(String.valueOf(seq.hashCode()));
          }
          jsonAlignmentPojo.getSeqGroups().add(seqGrpPojo);
        }
      }

      jsonOutput = JSONUtils.stringify(jsonAlignmentPojo);
      return jsonOutput.replaceAll("xstart", "xStart").replaceAll("xend",
              "xEnd");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return jsonOutput;
  }

  public String[] getHiddenSections()
  {
    String[] hiddenSections = new String[2];
    if (getViewport() == null)
    {
      return null;
    }

    // hidden column business
    if (getViewport().hasHiddenColumns())
    {
      hiddenSections[0] = getViewport().getAlignment().getHiddenColumns()
              .regionsToString(";", "-");
    }

    // hidden rows/seqs business
    HiddenSequences hiddenSeqsObj = getViewport().getAlignment()
            .getHiddenSequences();
    if (hiddenSeqsObj == null || hiddenSeqsObj.hiddenSequences == null)
    {
      return hiddenSections;
    }

    SequenceI[] hiddenSeqs = hiddenSeqsObj.hiddenSequences;
    StringBuilder hiddenSeqsBuilder = new StringBuilder();
    for (SequenceI hiddenSeq : hiddenSeqs)
    {
      if (hiddenSeq != null)
      {
        hiddenSeqsBuilder.append(";").append(hiddenSeq.hashCode());
      }
    }
    if (hiddenSeqsBuilder.length() > 0)
    {
      hiddenSeqsBuilder.deleteCharAt(0);
    }
    hiddenSections[1] = hiddenSeqsBuilder.toString();

    return hiddenSections;
  }

  protected List<SequenceFeaturesPojo> sequenceFeatureToJsonPojo(
          SequenceI[] sqs)
  {
    displayedFeatures = (fr == null) ? null : fr.getFeaturesDisplayed();
    List<SequenceFeaturesPojo> sequenceFeaturesPojo = new ArrayList<>();
    if (sqs == null)
    {
      return sequenceFeaturesPojo;
    }

    FeatureColourFinder finder = new FeatureColourFinder(fr);

    String[] visibleFeatureTypes = displayedFeatures == null ? null
            : displayedFeatures.getVisibleFeatures().toArray(
                    new String[displayedFeatures.getVisibleFeatureCount()]);

    for (SequenceI seq : sqs)
    {
      /*
       * get all features currently visible (and any non-positional features)
       */
      List<SequenceFeature> seqFeatures = seq.getFeatures()
              .getAllFeatures(visibleFeatureTypes);
      for (SequenceFeature sf : seqFeatures)
      {
        SequenceFeaturesPojo jsonFeature = new SequenceFeaturesPojo(
                String.valueOf(seq.hashCode()));

        String featureColour = (fr == null) ? null
                : Format.getHexString(finder.findFeatureColour(Color.white,
                        seq, seq.findIndex(sf.getBegin())));
        int xStart = sf.getBegin() == 0 ? 0
                : seq.findIndex(sf.getBegin()) - 1;
        int xEnd = sf.getEnd() == 0 ? 0 : seq.findIndex(sf.getEnd());
        jsonFeature.setXstart(xStart);
        jsonFeature.setXend(xEnd);
        jsonFeature.setType(sf.getType());
        jsonFeature.setDescription(sf.getDescription());
        jsonFeature.setLinks(sf.links);
        jsonFeature.setOtherDetails(sf.otherDetails);
        jsonFeature.setScore(sf.getScore());
        jsonFeature.setFillColor(featureColour);
        jsonFeature.setFeatureGroup(sf.getFeatureGroup());
        sequenceFeaturesPojo.add(jsonFeature);
      }
    }
    return sequenceFeaturesPojo;
  }

  public static List<AlignmentAnnotationPojo> annotationToJsonPojo(
          Vector<AlignmentAnnotation> annotations)
  {
    List<AlignmentAnnotationPojo> jsonAnnotations = new ArrayList<>();
    if (annotations == null)
    {
      return jsonAnnotations;
    }
    for (AlignmentAnnotation annot : annotations)
    {
      AlignmentAnnotationPojo alignAnnotPojo = new AlignmentAnnotationPojo();
      alignAnnotPojo.setDescription(annot.description);
      alignAnnotPojo.setLabel(annot.label);
      if (!Double.isNaN(annot.score))
      {
        alignAnnotPojo.setScore(annot.score);
      }
      alignAnnotPojo.setCalcId(annot.getCalcId());
      alignAnnotPojo.setGraphType(annot.graph);

      AnnotationDisplaySettingPojo annotSetting = new AnnotationDisplaySettingPojo();
      annotSetting.setBelowAlignment(annot.belowAlignment);
      annotSetting.setCentreColLabels(annot.centreColLabels);
      annotSetting.setScaleColLabel(annot.scaleColLabel);
      annotSetting.setShowAllColLabels(annot.showAllColLabels);
      annotSetting.setVisible(annot.visible);
      annotSetting.setHasIcon(annot.hasIcons);
      alignAnnotPojo.setAnnotationSettings(annotSetting);
      SequenceI refSeq = annot.sequenceRef;
      if (refSeq != null)
      {
        alignAnnotPojo.setSequenceRef(String.valueOf(refSeq.hashCode()));
      }
      for (Annotation annotation : annot.annotations)
      {
        AnnotationPojo annotationPojo = new AnnotationPojo();
        if (annotation != null)
        {
          annotationPojo.setDescription(annotation.description);
          annotationPojo.setValue(annotation.value);
          annotationPojo
                  .setSecondaryStructure(annotation.secondaryStructure);
          String displayChar = annotation.displayCharacter == null ? null
                  : annotation.displayCharacter;
          // System.out.println("--------------------->[" + displayChar + "]");
          annotationPojo.setDisplayCharacter(displayChar);
          if (annotation.colour != null)
          {
            annotationPojo.setColour(
                    jalview.util.Format.getHexString(annotation.colour));
          }
          alignAnnotPojo.getAnnotations().add(annotationPojo);
        }
        else
        {
          if (annot.getCalcId() != null
                  && annot.getCalcId().equalsIgnoreCase(TCOFFEE_SCORE))
          {
            // do nothing
          }
          else
          {
            alignAnnotPojo.getAnnotations().add(annotationPojo);
          }
        }
      }
      jsonAnnotations.add(alignAnnotPojo);
    }
    return jsonAnnotations;
  }

  @SuppressWarnings("unchecked")
  public JSONFile parse(Reader jsonAlignmentString)
  {
    try
    {
      Map<String, Object> alignmentJsonObj = (Map<String, Object>) JSONUtils
              .parse(jsonAlignmentString);
      List<Object> seqJsonArray = (List<Object>) alignmentJsonObj
              .get("seqs");
      List<Object> alAnnotJsonArray = (List<Object>) alignmentJsonObj
              .get("alignAnnotation");
      List<Object> jsonSeqArray = (List<Object>) alignmentJsonObj
              .get("seqFeatures");
      List<Object> seqGrpJsonArray = (List<Object>) alignmentJsonObj
              .get("seqGroups");
      Map<String, Object> jvSettingsJsonObj = (Map<String, Object>) alignmentJsonObj
              .get("appSettings");

      if (jvSettingsJsonObj != null)
      {
        globalColourScheme = (String) jvSettingsJsonObj
                .get("globalColorScheme");
        Boolean showFeatures = Boolean.valueOf(
                jvSettingsJsonObj.get("showSeqFeatures").toString());
        setShowSeqFeatures(showFeatures);
        parseHiddenSeqRefsAsList(jvSettingsJsonObj);
        parseHiddenCols(jvSettingsJsonObj);
      }

      hiddenSequences = new ArrayList<>();
      seqMap = new Hashtable<>();
      for (Iterator<Object> sequenceIter = seqJsonArray
              .iterator(); sequenceIter.hasNext();)
      {
        Map<String, Object> sequence = (Map<String, Object>) sequenceIter
                .next();
        String sequcenceString = sequence.get("seq").toString();
        String sequenceName = sequence.get("name").toString();
        String seqUniqueId = sequence.get("id").toString();
        int start = Integer.valueOf(sequence.get("start").toString());
        int end = Integer.valueOf(sequence.get("end").toString());
        Sequence seq = new Sequence(sequenceName, sequcenceString, start,
                end);
        if (hiddenSeqRefs != null && hiddenSeqRefs.contains(seqUniqueId))
        {
          hiddenSequences.add(seq);
        }
        seqs.add(seq);
        seqMap.put(seqUniqueId, seq);
      }

      parseFeatures(jsonSeqArray);

      for (Iterator<Object> seqGrpIter = seqGrpJsonArray
              .iterator(); seqGrpIter.hasNext();)
      {
        Map<String, Object> seqGrpObj = (Map<String, Object>) seqGrpIter
                .next();
        String grpName = seqGrpObj.get("groupName").toString();
        String colourScheme = seqGrpObj.get("colourScheme").toString();
        String description = (seqGrpObj.get("description") == null) ? null
                : seqGrpObj.get("description").toString();
        boolean displayBoxes = Boolean
                .valueOf(seqGrpObj.get("displayBoxes").toString());
        boolean displayText = Boolean
                .valueOf(seqGrpObj.get("displayText").toString());
        boolean colourText = Boolean
                .valueOf(seqGrpObj.get("colourText").toString());
        boolean showNonconserved = Boolean
                .valueOf(seqGrpObj.get("showNonconserved").toString());
        int startRes = Integer
                .valueOf(seqGrpObj.get("startRes").toString());
        int endRes = Integer.valueOf(seqGrpObj.get("endRes").toString());
        List<Object> sequenceRefs = (List<Object>) seqGrpObj
                .get("sequenceRefs");

        ArrayList<SequenceI> grpSeqs = new ArrayList<>();
        if (sequenceRefs.size() > 0)
        {
          Iterator<Object> seqHashIter = sequenceRefs.iterator();
          while (seqHashIter.hasNext())
          {
            Sequence sequence = seqMap.get(seqHashIter.next());
            if (sequence != null)
            {
              grpSeqs.add(sequence);
            }
          }
        }
        SequenceGroup seqGrp = new SequenceGroup(grpSeqs, grpName, null,
                displayBoxes, displayText, colourText, startRes, endRes);
        seqGrp.setColourScheme(ColourSchemeMapper
                .getJalviewColourScheme(colourScheme, seqGrp));
        seqGrp.setShowNonconserved(showNonconserved);
        seqGrp.setDescription(description);
        this.seqGroups.add(seqGrp);

      }

      for (Iterator<Object> alAnnotIter = alAnnotJsonArray
              .iterator(); alAnnotIter.hasNext();)
      {
        Map<String, Object> alAnnot = (Map<String, Object>) alAnnotIter
                .next();
        List<Object> annotJsonArray = (List<Object>) alAnnot
                .get("annotations");
        Annotation[] annotations = new Annotation[annotJsonArray.size()];
        int count = 0;
        for (Iterator<Object> annotIter = annotJsonArray
                .iterator(); annotIter.hasNext();)
        {
          Map<String, Object> annot = (Map<String, Object>) annotIter
                  .next();
          if (annot == null)
          {
            annotations[count] = null;
          }
          else
          {
            float val = annot.get("value") == null ? null
                    : Float.valueOf(annot.get("value").toString());
            String desc = annot.get("description") == null ? null
                    : annot.get("description").toString();
            char ss = annot.get("secondaryStructure") == null
                    || annot.get("secondaryStructure").toString()
                            .equalsIgnoreCase("u0000") ? ' '
                                    : annot.get("secondaryStructure")
                                            .toString().charAt(0);
            String displayChar = annot.get("displayCharacter") == null ? ""
                    : annot.get("displayCharacter").toString();

            annotations[count] = new Annotation(displayChar, desc, ss, val);
            if (annot.get("colour") != null)
            {
              Color color = ColorUtils
                      .parseColourString(annot.get("colour").toString());
              annotations[count].colour = color;
            }
          }
          ++count;
        }

        AlignmentAnnotation alignAnnot = new AlignmentAnnotation(
                alAnnot.get("label").toString(),
                alAnnot.get("description").toString(), annotations);
        alignAnnot.graph = (alAnnot.get("graphType") == null) ? 0
                : Integer.valueOf(alAnnot.get("graphType").toString());

        Map<String, Object> diplaySettings = (Map<String, Object>) alAnnot
                .get("annotationSettings");
        if (diplaySettings != null)
        {

          alignAnnot.scaleColLabel = (diplaySettings
                  .get("scaleColLabel") == null) ? false
                          : Boolean.valueOf(diplaySettings
                                  .get("scaleColLabel").toString());
          alignAnnot.showAllColLabels = (diplaySettings
                  .get("showAllColLabels") == null) ? true
                          : Boolean.valueOf(diplaySettings
                                  .get("showAllColLabels").toString());
          alignAnnot.centreColLabels = (diplaySettings
                  .get("centreColLabels") == null) ? true
                          : Boolean.valueOf(diplaySettings
                                  .get("centreColLabels").toString());
          alignAnnot.belowAlignment = (diplaySettings
                  .get("belowAlignment") == null) ? false
                          : Boolean.valueOf(diplaySettings
                                  .get("belowAlignment").toString());
          alignAnnot.visible = (diplaySettings.get("visible") == null)
                  ? true
                  : Boolean.valueOf(
                          diplaySettings.get("visible").toString());
          alignAnnot.hasIcons = (diplaySettings.get("hasIcon") == null)
                  ? true
                  : Boolean.valueOf(
                          diplaySettings.get("hasIcon").toString());

        }
        if (alAnnot.get("score") != null)
        {
          alignAnnot.score = Double
                  .valueOf(alAnnot.get("score").toString());
        }

        String calcId = (alAnnot.get("calcId") == null) ? ""
                : alAnnot.get("calcId").toString();
        alignAnnot.setCalcId(calcId);
        String seqHash = (alAnnot.get("sequenceRef") != null)
                ? alAnnot.get("sequenceRef").toString()
                : null;

        Sequence sequence = (seqHash != null) ? seqMap.get(seqHash) : null;
        if (sequence != null)
        {
          alignAnnot.sequenceRef = sequence;
          sequence.addAlignmentAnnotation(alignAnnot);
          if (alignAnnot.label.equalsIgnoreCase("T-COFFEE"))
          {
            alignAnnot.createSequenceMapping(sequence, sequence.getStart(),
                    false);
            sequence.addAlignmentAnnotation(alignAnnot);
            alignAnnot.adjustForAlignment();
          }
        }
        alignAnnot.validateRangeAndDisplay();
        this.annotations.add(alignAnnot);

      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return this;
  }

  public void parseHiddenSeqRefsAsList(Map<String, Object> jvSettingsJson)
  {
    hiddenSeqRefs = new ArrayList<>();
    String hiddenSeqs = (String) jvSettingsJson.get("hiddenSeqs");
    if (hiddenSeqs != null && !hiddenSeqs.isEmpty())
    {
      String[] seqRefs = hiddenSeqs.split(";");
      for (int i = 0, n = seqRefs.length; i < n; i++)
      {
        hiddenSeqRefs.add(seqRefs[i]);
      }
    }
  }

  public void parseHiddenCols(Map<String, Object> jvSettingsJson)
  {
    String hiddenCols = (String) jvSettingsJson.get("hiddenCols");
    if (hiddenCols != null && !hiddenCols.isEmpty())
    {
      hiddenColumns = new HiddenColumns();
      String[] rangeStrings = hiddenCols.split(";");
      for (int i = 0, n = rangeStrings.length; i < n; i++)
      {
        String[] range = rangeStrings[i].split("-");
        hiddenColumns.hideColumns(Integer.valueOf(range[0]),
                Integer.valueOf(range[1]));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void parseFeatures(List<Object> jsonSeqFeatures)
  {
    if (jsonSeqFeatures != null)
    {
      displayedFeatures = new FeaturesDisplayed();
      for (Object o : jsonSeqFeatures)
      {
        Map<String, Object> jsonFeature = (Map<String, Object>) o;
        Long begin = (Long) jsonFeature.get("xStart");
        Long end = (Long) jsonFeature.get("xEnd");
        String type = (String) jsonFeature.get("type");
        String featureGrp = (String) jsonFeature.get("featureGroup");
        String description = (String) jsonFeature.get("description");
        String seqRef = (String) jsonFeature.get("sequenceRef");
        Float score = Float.valueOf(jsonFeature.get("score").toString());

        Sequence seq = seqMap.get(seqRef);

        /*
         * begin/end of 0 is for a non-positional feature
         */
        int featureBegin = begin.intValue() == 0 ? 0
                : seq.findPosition(begin.intValue());
        int featureEnd = end.intValue() == 0 ? 0
                : seq.findPosition(end.intValue()) - 1;

        SequenceFeature sequenceFeature = new SequenceFeature(type,
                description, featureBegin, featureEnd, score, featureGrp);

        List<Object> linksJsonArray = (List<Object>) jsonFeature
                .get("links");
        if (linksJsonArray != null && linksJsonArray.size() > 0)
        {
          Iterator<Object> linkList = linksJsonArray.iterator();
          while (linkList.hasNext())
          {
            sequenceFeature.addLink((String) linkList.next());
          }
        }

        seq.addSequenceFeature(sequenceFeature);
        displayedFeatures.setVisible(type);
      }
    }
  }

  @Override
  public String getGlobalColourScheme()
  {
    return globalColourScheme;
  }

  public void setGlobalColorScheme(String globalColourScheme)
  {
    this.globalColourScheme = globalColourScheme;
  }

  @Override
  public FeaturesDisplayedI getDisplayedFeatures()
  {
    return displayedFeatures;
  }

  public void setDisplayedFeatures(FeaturesDisplayedI displayedFeatures)
  {
    this.displayedFeatures = displayedFeatures;
  }

  @Override
  public void configureForView(AlignmentViewPanel avpanel)
  {
    if (avpanel == null)
    {
      return;
    }
    super.configureForView(avpanel);
    AlignViewportI viewport = avpanel.getAlignViewport();
    AlignmentI alignment = viewport.getAlignment();
    AlignmentAnnotation[] annots = alignment.getAlignmentAnnotation();

    seqGroups = alignment.getGroups();
    fr = avpanel.cloneFeatureRenderer();

    // Add non auto calculated annotation to AlignFile
    if (annots != null)
    {
      for (AlignmentAnnotation annot : annots)
      {
        if (annot != null && !annot.autoCalculated)
        {
          annotations.add(annot);
        }
      }
    }
    globalColourScheme = ColourSchemeProperty
            .getColourName(viewport.getGlobalColourScheme());
    setDisplayedFeatures(viewport.getFeaturesDisplayed());
    showSeqFeatures = viewport.isShowSequenceFeatures();

  }

  @Override
  public boolean isShowSeqFeatures()
  {
    return showSeqFeatures;
  }

  public void setShowSeqFeatures(boolean showSeqFeatures)
  {
    this.showSeqFeatures = showSeqFeatures;
  }

  public Vector<AlignmentAnnotation> getAnnotations()
  {
    return annotations;
  }

  @Override
  public HiddenColumns getHiddenColumns()
  {
    return hiddenColumns;
  }

  public void setHiddenColumns(HiddenColumns hidden)
  {
    this.hiddenColumns = hidden;
  }

  @Override
  public SequenceI[] getHiddenSequences()
  {
    if (hiddenSequences == null || hiddenSequences.isEmpty())
    {
      return new SequenceI[] {};
    }
    synchronized (hiddenSequences)
    {
      return hiddenSequences.toArray(new SequenceI[hiddenSequences.size()]);
    }
  }

  public void setHiddenSequences(ArrayList<SequenceI> hiddenSequences)
  {
    this.hiddenSequences = hiddenSequences;
  }

  public class JSONExportSettings
  {
    private boolean exportSequence;

    private boolean exportSequenceFeatures;

    private boolean exportAnnotations;

    private boolean exportGroups;

    private boolean exportJalviewSettings;

    public boolean isExportSequence()
    {
      return exportSequence;
    }

    public void setExportSequence(boolean exportSequence)
    {
      this.exportSequence = exportSequence;
    }

    public boolean isExportSequenceFeatures()
    {
      return exportSequenceFeatures;
    }

    public void setExportSequenceFeatures(boolean exportSequenceFeatures)
    {
      this.exportSequenceFeatures = exportSequenceFeatures;
    }

    public boolean isExportAnnotations()
    {
      return exportAnnotations;
    }

    public void setExportAnnotations(boolean exportAnnotations)
    {
      this.exportAnnotations = exportAnnotations;
    }

    public boolean isExportGroups()
    {
      return exportGroups;
    }

    public void setExportGroups(boolean exportGroups)
    {
      this.exportGroups = exportGroups;
    }

    public boolean isExportJalviewSettings()
    {
      return exportJalviewSettings;
    }

    public void setExportJalviewSettings(boolean exportJalviewSettings)
    {
      this.exportJalviewSettings = exportJalviewSettings;
    }
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
