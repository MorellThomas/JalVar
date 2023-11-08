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
package jalview.project;

import static jalview.math.RotatableMatrix.Axis.X;
import static jalview.math.RotatableMatrix.Axis.Y;
import static jalview.math.RotatableMatrix.Axis.Z;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import jalview.analysis.Conservation;
import jalview.analysis.PCA;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.analysis.scoremodels.SimilarityParams;
import jalview.api.FeatureColourI;
import jalview.api.ViewStyleI;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ContactMatrix;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.GeneLocus;
import jalview.datamodel.GraphLine;
import jalview.datamodel.GroupSet;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Point;
import jalview.datamodel.RnaViewerModel;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.StructureViewerModel;
import jalview.datamodel.StructureViewerModel.StructureData;
import jalview.datamodel.features.FeatureMatcher;
import jalview.datamodel.features.FeatureMatcherI;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.ext.varna.RnaModel;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.AlignmentPanel;
import jalview.gui.AppVarna;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.OOMWarning;
import jalview.gui.OverviewPanel;
import jalview.gui.PCAPanel;
import jalview.gui.PaintRefresher;
import jalview.gui.SplitFrame;
import jalview.gui.StructureViewer;
import jalview.gui.StructureViewer.ViewerType;
import jalview.gui.StructureViewerBase;
import jalview.gui.TreePanel;
import jalview.io.BackupFiles;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.NewickFile;
import jalview.math.Matrix;
import jalview.math.MatrixI;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.FeatureColour;
import jalview.schemes.ResidueProperties;
import jalview.schemes.UserColourScheme;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.Format;
import jalview.util.HttpUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.StringUtils;
import jalview.util.jarInputStreamProvider;
import jalview.util.matcher.Condition;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.PCAModel;
import jalview.viewmodel.ViewportRanges;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;
import jalview.viewmodel.seqfeatures.FeatureRendererSettings;
import jalview.viewmodel.seqfeatures.FeaturesDisplayed;
import jalview.ws.datamodel.MappableContactMatrixI;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;
import jalview.ws.jws2.Jws2Discoverer;
import jalview.ws.jws2.dm.AAConSettings;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.ArgumentI;
import jalview.ws.params.AutoCalcSetting;
import jalview.ws.params.WsParamSetI;
import jalview.xml.binding.jalview.AlcodonFrame;
import jalview.xml.binding.jalview.AlcodonFrame.AlcodMap;
import jalview.xml.binding.jalview.Annotation;
import jalview.xml.binding.jalview.Annotation.ThresholdLine;
import jalview.xml.binding.jalview.AnnotationColourScheme;
import jalview.xml.binding.jalview.AnnotationElement;
import jalview.xml.binding.jalview.DoubleMatrix;
import jalview.xml.binding.jalview.DoubleVector;
import jalview.xml.binding.jalview.Feature;
import jalview.xml.binding.jalview.Feature.OtherData;
import jalview.xml.binding.jalview.FeatureMatcherSet.CompoundMatcher;
import jalview.xml.binding.jalview.FilterBy;
import jalview.xml.binding.jalview.JalviewModel;
import jalview.xml.binding.jalview.JalviewModel.FeatureSettings;
import jalview.xml.binding.jalview.JalviewModel.FeatureSettings.Group;
import jalview.xml.binding.jalview.JalviewModel.FeatureSettings.Setting;
import jalview.xml.binding.jalview.JalviewModel.JGroup;
import jalview.xml.binding.jalview.JalviewModel.JSeq;
import jalview.xml.binding.jalview.JalviewModel.JSeq.Pdbids;
import jalview.xml.binding.jalview.JalviewModel.JSeq.Pdbids.StructureState;
import jalview.xml.binding.jalview.JalviewModel.JSeq.RnaViewer;
import jalview.xml.binding.jalview.JalviewModel.JSeq.RnaViewer.SecondaryStructure;
import jalview.xml.binding.jalview.JalviewModel.PcaViewer;
import jalview.xml.binding.jalview.JalviewModel.PcaViewer.Axis;
import jalview.xml.binding.jalview.JalviewModel.PcaViewer.SeqPointMax;
import jalview.xml.binding.jalview.JalviewModel.PcaViewer.SeqPointMin;
import jalview.xml.binding.jalview.JalviewModel.PcaViewer.SequencePoint;
import jalview.xml.binding.jalview.JalviewModel.Tree;
import jalview.xml.binding.jalview.JalviewModel.UserColours;
import jalview.xml.binding.jalview.JalviewModel.Viewport;
import jalview.xml.binding.jalview.JalviewModel.Viewport.CalcIdParam;
import jalview.xml.binding.jalview.JalviewModel.Viewport.HiddenColumns;
import jalview.xml.binding.jalview.JalviewModel.Viewport.Overview;
import jalview.xml.binding.jalview.JalviewUserColours;
import jalview.xml.binding.jalview.JalviewUserColours.Colour;
import jalview.xml.binding.jalview.MapListType;
import jalview.xml.binding.jalview.MapListType.MapListFrom;
import jalview.xml.binding.jalview.MapListType.MapListTo;
import jalview.xml.binding.jalview.Mapping;
import jalview.xml.binding.jalview.MatrixType;
import jalview.xml.binding.jalview.NoValueColour;
import jalview.xml.binding.jalview.ObjectFactory;
import jalview.xml.binding.jalview.PcaDataType;
import jalview.xml.binding.jalview.Pdbentry.Property;
import jalview.xml.binding.jalview.Sequence;
import jalview.xml.binding.jalview.Sequence.DBRef;
import jalview.xml.binding.jalview.SequenceSet;
import jalview.xml.binding.jalview.SequenceSet.SequenceSetProperties;
import jalview.xml.binding.jalview.ThresholdType;
import jalview.xml.binding.jalview.VAMSAS;

/**
 * Write out the current jalview desktop state as a Jalview XML stream.
 * 
 * Note: the vamsas objects referred to here are primitive versions of the
 * VAMSAS project schema elements - they are not the same and most likely never
 * will be :)
 * 
 * @author $author$
 * @version $Revision: 1.134 $
 */
public class Jalview2XML
{

  // BH 2018 we add the .jvp binary extension to J2S so that
  // it will declare that binary when we do the file save from the browser

  static
  {
    Platform.addJ2SBinaryType(".jvp?");
  }

  private static final String VIEWER_PREFIX = "viewer_";

  private static final String RNA_PREFIX = "rna_";

  private static final String UTF_8 = "UTF-8";

  /**
   * used in decision if quit confirmation should be issued
   */
  private static boolean stateSavedUpToDate = false;

  /**
   * prefix for recovering datasets for alignments with multiple views where
   * non-existent dataset IDs were written for some views
   */
  private static final String UNIQSEQSETID = "uniqueSeqSetId.";

  // use this with nextCounter() to make unique names for entities
  private int counter = 0;

  /*
   * SequenceI reference -> XML ID string in jalview XML. Populated as XML reps
   * of sequence objects are created.
   */
  IdentityHashMap<SequenceI, String> seqsToIds = null;

  /**
   * jalview XML Sequence ID to jalview sequence object reference (both dataset
   * and alignment sequences. Populated as XML reps of sequence objects are
   * created.)
   */
  Map<String, SequenceI> seqRefIds = null;

  Map<String, SequenceI> incompleteSeqs = null;

  List<SeqFref> frefedSequence = null;

  boolean raiseGUI = true; // whether errors are raised in dialog boxes or not

  /*
   * Map of reconstructed AlignFrame objects that appear to have come from
   * SplitFrame objects (have a dna/protein complement view).
   */
  private Map<Viewport, AlignFrame> splitFrameCandidates = new HashMap<>();

  /*
   * Map from displayed rna structure models to their saved session state jar
   * entry names
   */
  private Map<RnaModel, String> rnaSessions = new HashMap<>();

  /**
   * A helper method for safely using the value of an optional attribute that
   * may be null if not present in the XML. Answers the boolean value, or false
   * if null.
   * 
   * @param b
   * @return
   */
  public static boolean safeBoolean(Boolean b)
  {
    return b == null ? false : b.booleanValue();
  }

  /**
   * A helper method for safely using the value of an optional attribute that
   * may be null if not present in the XML. Answers the integer value, or zero
   * if null.
   * 
   * @param i
   * @return
   */
  public static int safeInt(Integer i)
  {
    return i == null ? 0 : i.intValue();
  }

  /**
   * A helper method for safely using the value of an optional attribute that
   * may be null if not present in the XML. Answers the float value, or zero if
   * null.
   * 
   * @param f
   * @return
   */
  public static float safeFloat(Float f)
  {
    return f == null ? 0f : f.floatValue();
  }

  /**
   * create/return unique hash string for sq
   * 
   * @param sq
   * @return new or existing unique string for sq
   */
  String seqHash(SequenceI sq)
  {
    if (seqsToIds == null)
    {
      initSeqRefs();
    }
    if (seqsToIds.containsKey(sq))
    {
      return seqsToIds.get(sq);
    }
    else
    {
      // create sequential key
      String key = "sq" + (seqsToIds.size() + 1);
      key = makeHashCode(sq, key); // check we don't have an external reference
      // for it already.
      seqsToIds.put(sq, key);
      return key;
    }
  }

  void initSeqRefs()
  {
    if (seqsToIds == null)
    {
      seqsToIds = new IdentityHashMap<>();
    }
    if (seqRefIds == null)
    {
      seqRefIds = new HashMap<>();
    }
    if (incompleteSeqs == null)
    {
      incompleteSeqs = new HashMap<>();
    }
    if (frefedSequence == null)
    {
      frefedSequence = new ArrayList<>();
    }
  }

  public Jalview2XML()
  {
  }

  public Jalview2XML(boolean raiseGUI)
  {
    this.raiseGUI = raiseGUI;
  }

  /**
   * base class for resolving forward references to sequences by their ID
   * 
   * @author jprocter
   *
   */
  abstract class SeqFref
  {
    String sref;

    String type;

    public SeqFref(String _sref, String type)
    {
      sref = _sref;
      this.type = type;
    }

    public String getSref()
    {
      return sref;
    }

    public SequenceI getSrefSeq()
    {
      return seqRefIds.get(sref);
    }

    public boolean isResolvable()
    {
      return seqRefIds.get(sref) != null;
    }

    public SequenceI getSrefDatasetSeq()
    {
      SequenceI sq = seqRefIds.get(sref);
      if (sq != null)
      {
        while (sq.getDatasetSequence() != null)
        {
          sq = sq.getDatasetSequence();
        }
      }
      return sq;
    }

    /**
     * @return true if the forward reference was fully resolved
     */
    abstract boolean resolve();

    @Override
    public String toString()
    {
      return type + " reference to " + sref;
    }
  }

  /**
   * create forward reference for a mapping
   * 
   * @param sref
   * @param _jmap
   * @return
   */
  public SeqFref newMappingRef(final String sref,
          final jalview.datamodel.Mapping _jmap)
  {
    SeqFref fref = new SeqFref(sref, "Mapping")
    {
      public jalview.datamodel.Mapping jmap = _jmap;

      @Override
      boolean resolve()
      {
        SequenceI seq = getSrefDatasetSeq();
        if (seq == null)
        {
          return false;
        }
        jmap.setTo(seq);
        return true;
      }
    };
    return fref;
  }

  public SeqFref newAlcodMapRef(final String sref,
          final AlignedCodonFrame _cf,
          final jalview.datamodel.Mapping _jmap)
  {

    SeqFref fref = new SeqFref(sref, "Codon Frame")
    {
      AlignedCodonFrame cf = _cf;

      public jalview.datamodel.Mapping mp = _jmap;

      @Override
      public boolean isResolvable()
      {
        return super.isResolvable() && mp.getTo() != null;
      }

      @Override
      boolean resolve()
      {
        SequenceI seq = getSrefDatasetSeq();
        if (seq == null)
        {
          return false;
        }
        cf.addMap(seq, mp.getTo(), mp.getMap());
        return true;
      }
    };
    return fref;
  }

  public void resolveFrefedSequences()
  {
    Iterator<SeqFref> nextFref = frefedSequence.iterator();
    int toresolve = frefedSequence.size();
    int unresolved = 0, failedtoresolve = 0;
    while (nextFref.hasNext())
    {
      SeqFref ref = nextFref.next();
      if (ref.isResolvable())
      {
        try
        {
          if (ref.resolve())
          {
            nextFref.remove();
          }
          else
          {
            failedtoresolve++;
          }
        } catch (Exception x)
        {
          System.err.println(
                  "IMPLEMENTATION ERROR: Failed to resolve forward reference for sequence "
                          + ref.getSref());
          x.printStackTrace();
          failedtoresolve++;
        }
      }
      else
      {
        unresolved++;
      }
    }
    if (unresolved > 0)
    {
      System.err.println("Jalview Project Import: There were " + unresolved
              + " forward references left unresolved on the stack.");
    }
    if (failedtoresolve > 0)
    {
      System.err.println("SERIOUS! " + failedtoresolve
              + " resolvable forward references failed to resolve.");
    }
    if (incompleteSeqs != null && incompleteSeqs.size() > 0)
    {
      System.err.println(
              "Jalview Project Import: There are " + incompleteSeqs.size()
                      + " sequences which may have incomplete metadata.");
      if (incompleteSeqs.size() < 10)
      {
        for (SequenceI s : incompleteSeqs.values())
        {
          System.err.println(s.toString());
        }
      }
      else
      {
        System.err.println(
                "Too many to report. Skipping output of incomplete sequences.");
      }
    }
  }

  /**
   * This maintains a map of viewports, the key being the seqSetId. Important to
   * set historyItem and redoList for multiple views
   */
  Map<String, AlignViewport> viewportsAdded = new HashMap<>();

  Map<String, AlignmentAnnotation> annotationIds = new HashMap<>();

  String uniqueSetSuffix = "";

  /**
   * List of pdbfiles added to Jar
   */
  List<String> pdbfiles = null;

  // SAVES SEVERAL ALIGNMENT WINDOWS TO SAME JARFILE
  public void saveState(File statefile)
  {
    FileOutputStream fos = null;

    try
    {

      fos = new FileOutputStream(statefile);

      JarOutputStream jout = new JarOutputStream(fos);
      saveState(jout);
      fos.close();

    } catch (Exception e)
    {
      Console.error("Couln't write Jalview state to " + statefile, e);
      // TODO: inform user of the problem - they need to know if their data was
      // not saved !
      if (errorMessage == null)
      {
        errorMessage = "Did't write Jalview Archive to output file '"
                + statefile + "' - See console error log for details";
      }
      else
      {
        errorMessage += "(Didn't write Jalview Archive to output file '"
                + statefile + ")";
      }
      e.printStackTrace();
    } finally
    {
      if (fos != null)
      {
        try
        {
          fos.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
    reportErrors();
  }

  /**
   * Writes a jalview project archive to the given Jar output stream.
   * 
   * @param jout
   */
  public void saveState(JarOutputStream jout)
  {
    AlignFrame[] frames = Desktop.getAlignFrames();

    setStateSavedUpToDate(true);

    if (Cache.getDefault("DEBUG_DELAY_SAVE", false))
    {
      int n = debugDelaySave;
      int i = 0;
      while (i < n)
      {
        Console.debug("***** debugging save sleep " + i + "/" + n);
        try
        {
          Thread.sleep(1000);
        } catch (InterruptedException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        i++;
      }
    }

    if (frames == null)
    {
      return;
    }
    saveAllFrames(Arrays.asList(frames), jout);
  }

  /**
   * core method for storing state for a set of AlignFrames.
   * 
   * @param frames
   *          - frames involving all data to be exported (including containing
   *          splitframes)
   * @param jout
   *          - project output stream
   */
  private void saveAllFrames(List<AlignFrame> frames, JarOutputStream jout)
  {
    Hashtable<String, AlignFrame> dsses = new Hashtable<>();

    /*
     * ensure cached data is clear before starting
     */
    // todo tidy up seqRefIds, seqsToIds initialisation / reset
    rnaSessions.clear();
    splitFrameCandidates.clear();

    try
    {

      // NOTE UTF-8 MUST BE USED FOR WRITING UNICODE CHARS
      // //////////////////////////////////////////////////

      List<String> shortNames = new ArrayList<>();
      List<String> viewIds = new ArrayList<>();

      // REVERSE ORDER
      for (int i = frames.size() - 1; i > -1; i--)
      {
        AlignFrame af = frames.get(i);
        // skip ?
        if (skipList != null && skipList
                .containsKey(af.getViewport().getSequenceSetId()))
        {
          continue;
        }

        String shortName = makeFilename(af, shortNames);

        int apSize = af.getAlignPanels().size();

        for (int ap = 0; ap < apSize; ap++)
        {
          AlignmentPanel apanel = (AlignmentPanel) af.getAlignPanels()
                  .get(ap);
          String fileName = apSize == 1 ? shortName : ap + shortName;
          if (!fileName.endsWith(".xml"))
          {
            fileName = fileName + ".xml";
          }

          saveState(apanel, fileName, jout, viewIds);

          String dssid = getDatasetIdRef(
                  af.getViewport().getAlignment().getDataset());
          if (!dsses.containsKey(dssid))
          {
            dsses.put(dssid, af);
          }
        }
      }

      writeDatasetFor(dsses, "" + jout.hashCode() + " " + uniqueSetSuffix,
              jout);

      try
      {
        jout.flush();
      } catch (Exception foo)
      {
      }
      jout.close();
    } catch (Exception ex)
    {
      // TODO: inform user of the problem - they need to know if their data was
      // not saved !
      if (errorMessage == null)
      {
        errorMessage = "Couldn't write Jalview Archive - see error output for details";
      }
      ex.printStackTrace();
    }
  }

  /**
   * Generates a distinct file name, based on the title of the AlignFrame, by
   * appending _n for increasing n until an unused name is generated. The new
   * name (without its extension) is added to the list.
   * 
   * @param af
   * @param namesUsed
   * @return the generated name, with .xml extension
   */
  protected String makeFilename(AlignFrame af, List<String> namesUsed)
  {
    String shortName = af.getTitle();

    if (shortName.indexOf(File.separatorChar) > -1)
    {
      shortName = shortName
              .substring(shortName.lastIndexOf(File.separatorChar) + 1);
    }

    int count = 1;

    while (namesUsed.contains(shortName))
    {
      if (shortName.endsWith("_" + (count - 1)))
      {
        shortName = shortName.substring(0, shortName.lastIndexOf("_"));
      }

      shortName = shortName.concat("_" + count);
      count++;
    }

    namesUsed.add(shortName);

    if (!shortName.endsWith(".xml"))
    {
      shortName = shortName + ".xml";
    }
    return shortName;
  }

  // USE THIS METHOD TO SAVE A SINGLE ALIGNMENT WINDOW
  public boolean saveAlignment(AlignFrame af, String jarFile,
          String fileName)
  {
    try
    {
      // create backupfiles object and get new temp filename destination
      boolean doBackup = BackupFiles.getEnabled();
      BackupFiles backupfiles = doBackup ? new BackupFiles(jarFile) : null;
      FileOutputStream fos = new FileOutputStream(
              doBackup ? backupfiles.getTempFilePath() : jarFile);

      if (Cache.getDefault("DEBUG_DELAY_SAVE", false))
      {
        int n = debugDelaySave;
        int i = 0;
        while (i < n)
        {
          Console.debug("***** debugging save sleep " + i + "/" + n);
          try
          {
            Thread.sleep(1000);
          } catch (InterruptedException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          i++;
        }
      }

      JarOutputStream jout = new JarOutputStream(fos);
      List<AlignFrame> frames = new ArrayList<>();

      // resolve splitframes
      if (af.getViewport().getCodingComplement() != null)
      {
        frames = ((SplitFrame) af.getSplitViewContainer()).getAlignFrames();
      }
      else
      {
        frames.add(af);
      }
      saveAllFrames(frames, jout);
      try
      {
        jout.flush();
      } catch (Exception foo)
      {
      }
      jout.close();
      boolean success = true;

      if (doBackup)
      {
        backupfiles.setWriteSuccess(success);
        success = backupfiles.rollBackupsAndRenameTempFile();
      }

      return success;
    } catch (Exception ex)
    {
      errorMessage = "Couldn't Write alignment view to Jalview Archive - see error output for details";
      ex.printStackTrace();
      return false;
    }
  }

  private void writeDatasetFor(Hashtable<String, AlignFrame> dsses,
          String fileName, JarOutputStream jout)
  {

    for (String dssids : dsses.keySet())
    {
      AlignFrame _af = dsses.get(dssids);
      String jfileName = fileName + " Dataset for " + _af.getTitle();
      if (!jfileName.endsWith(".xml"))
      {
        jfileName = jfileName + ".xml";
      }
      saveState(_af.alignPanel, jfileName, true, jout, null);
    }
  }

  /**
   * create a JalviewModel from an alignment view and marshall it to a
   * JarOutputStream
   * 
   * @param ap
   *          panel to create jalview model for
   * @param fileName
   *          name of alignment panel written to output stream
   * @param jout
   *          jar output stream
   * @param viewIds
   * @param out
   *          jar entry name
   */
  public JalviewModel saveState(AlignmentPanel ap, String fileName,
          JarOutputStream jout, List<String> viewIds)
  {
    return saveState(ap, fileName, false, jout, viewIds);
  }

  /**
   * create a JalviewModel from an alignment view and marshall it to a
   * JarOutputStream
   * 
   * @param ap
   *          panel to create jalview model for
   * @param fileName
   *          name of alignment panel written to output stream
   * @param storeDS
   *          when true, only write the dataset for the alignment, not the data
   *          associated with the view.
   * @param jout
   *          jar output stream
   * @param out
   *          jar entry name
   */
  public JalviewModel saveState(AlignmentPanel ap, String fileName,
          boolean storeDS, JarOutputStream jout, List<String> viewIds)
  {
    if (viewIds == null)
    {
      viewIds = new ArrayList<>();
    }

    initSeqRefs();

    List<UserColourScheme> userColours = new ArrayList<>();

    AlignViewport av = ap.av;
    ViewportRanges vpRanges = av.getRanges();

    final ObjectFactory objectFactory = new ObjectFactory();
    JalviewModel object = objectFactory.createJalviewModel();
    object.setVamsasModel(new VAMSAS());

    // object.setCreationDate(new java.util.Date(System.currentTimeMillis()));
    try
    {
      GregorianCalendar c = new GregorianCalendar();
      DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
      XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(c);// gregorianCalendar);
      object.setCreationDate(now);
    } catch (DatatypeConfigurationException e)
    {
      System.err.println("error writing date: " + e.toString());
    }
    object.setVersion(Cache.getDefault("VERSION", "Development Build"));

    /**
     * rjal is full height alignment, jal is actual alignment with full metadata
     * but excludes hidden sequences.
     */
    jalview.datamodel.AlignmentI rjal = av.getAlignment(), jal = rjal;

    if (av.hasHiddenRows())
    {
      rjal = jal.getHiddenSequences().getFullAlignment();
    }

    SequenceSet vamsasSet = new SequenceSet();
    Sequence vamsasSeq;
    // JalviewModelSequence jms = new JalviewModelSequence();

    vamsasSet.setGapChar(jal.getGapCharacter() + "");

    if (jal.getDataset() != null)
    {
      // dataset id is the dataset's hashcode
      vamsasSet.setDatasetId(getDatasetIdRef(jal.getDataset()));
      if (storeDS)
      {
        // switch jal and the dataset
        jal = jal.getDataset();
        rjal = jal;
      }
    }
    if (jal.getProperties() != null)
    {
      Enumeration en = jal.getProperties().keys();
      while (en.hasMoreElements())
      {
        String key = en.nextElement().toString();
        SequenceSetProperties ssp = new SequenceSetProperties();
        ssp.setKey(key);
        ssp.setValue(jal.getProperties().get(key).toString());
        // vamsasSet.addSequenceSetProperties(ssp);
        vamsasSet.getSequenceSetProperties().add(ssp);
      }
    }

    JSeq jseq;
    Set<String> calcIdSet = new HashSet<>();
    // record the set of vamsas sequence XML POJO we create.
    HashMap<String, Sequence> vamsasSetIds = new HashMap<>();
    // SAVE SEQUENCES
    for (final SequenceI jds : rjal.getSequences())
    {
      final SequenceI jdatasq = jds.getDatasetSequence() == null ? jds
              : jds.getDatasetSequence();
      String id = seqHash(jds);
      if (vamsasSetIds.get(id) == null)
      {
        if (seqRefIds.get(id) != null && !storeDS)
        {
          // This happens for two reasons: 1. multiple views are being
          // serialised.
          // 2. the hashCode has collided with another sequence's code. This
          // DOES
          // HAPPEN! (PF00072.15.stk does this)
          // JBPNote: Uncomment to debug writing out of files that do not read
          // back in due to ArrayOutOfBoundExceptions.
          // System.err.println("vamsasSeq backref: "+id+"");
          // System.err.println(jds.getName()+"
          // "+jds.getStart()+"-"+jds.getEnd()+" "+jds.getSequenceAsString());
          // System.err.println("Hashcode: "+seqHash(jds));
          // SequenceI rsq = (SequenceI) seqRefIds.get(id + "");
          // System.err.println(rsq.getName()+"
          // "+rsq.getStart()+"-"+rsq.getEnd()+" "+rsq.getSequenceAsString());
          // System.err.println("Hashcode: "+seqHash(rsq));
        }
        else
        {
          vamsasSeq = createVamsasSequence(id, jds);
          // vamsasSet.addSequence(vamsasSeq);
          vamsasSet.getSequence().add(vamsasSeq);
          vamsasSetIds.put(id, vamsasSeq);
          seqRefIds.put(id, jds);
        }
      }
      jseq = new JSeq();
      jseq.setStart(jds.getStart());
      jseq.setEnd(jds.getEnd());
      jseq.setColour(av.getSequenceColour(jds).getRGB());

      jseq.setId(id); // jseq id should be a string not a number
      if (!storeDS)
      {
        // Store any sequences this sequence represents
        if (av.hasHiddenRows())
        {
          // use rjal, contains the full height alignment
          jseq.setHidden(
                  av.getAlignment().getHiddenSequences().isHidden(jds));

          if (av.isHiddenRepSequence(jds))
          {
            jalview.datamodel.SequenceI[] reps = av
                    .getRepresentedSequences(jds).getSequencesInOrder(rjal);

            for (int h = 0; h < reps.length; h++)
            {
              if (reps[h] != jds)
              {
                // jseq.addHiddenSequences(rjal.findIndex(reps[h]));
                jseq.getHiddenSequences().add(rjal.findIndex(reps[h]));
              }
            }
          }
        }
        // mark sequence as reference - if it is the reference for this view
        if (jal.hasSeqrep())
        {
          jseq.setViewreference(jds == jal.getSeqrep());
        }
      }

      // TODO: omit sequence features from each alignment view's XML dump if we
      // are storing dataset
      List<SequenceFeature> sfs = jds.getSequenceFeatures();
      for (SequenceFeature sf : sfs)
      {
        // Features features = new Features();
        Feature features = new Feature();

        features.setBegin(sf.getBegin());
        features.setEnd(sf.getEnd());
        features.setDescription(sf.getDescription());
        features.setType(sf.getType());
        features.setFeatureGroup(sf.getFeatureGroup());
        features.setScore(sf.getScore());
        if (sf.links != null)
        {
          for (int l = 0; l < sf.links.size(); l++)
          {
            OtherData keyValue = new OtherData();
            keyValue.setKey("LINK_" + l);
            keyValue.setValue(sf.links.elementAt(l).toString());
            // features.addOtherData(keyValue);
            features.getOtherData().add(keyValue);
          }
        }
        if (sf.otherDetails != null)
        {
          /*
           * save feature attributes, which may be simple strings or
           * map valued (have sub-attributes)
           */
          for (Entry<String, Object> entry : sf.otherDetails.entrySet())
          {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>)
            {
              for (Entry<String, Object> subAttribute : ((Map<String, Object>) value)
                      .entrySet())
              {
                OtherData otherData = new OtherData();
                otherData.setKey(key);
                otherData.setKey2(subAttribute.getKey());
                otherData.setValue(subAttribute.getValue().toString());
                // features.addOtherData(otherData);
                features.getOtherData().add(otherData);
              }
            }
            else
            {
              OtherData otherData = new OtherData();
              otherData.setKey(key);
              otherData.setValue(value.toString());
              // features.addOtherData(otherData);
              features.getOtherData().add(otherData);
            }
          }
        }

        // jseq.addFeatures(features);
        jseq.getFeatures().add(features);
      }

      if (jdatasq.getAllPDBEntries() != null)
      {
        Enumeration<PDBEntry> en = jdatasq.getAllPDBEntries().elements();
        while (en.hasMoreElements())
        {
          Pdbids pdb = new Pdbids();
          jalview.datamodel.PDBEntry entry = en.nextElement();

          String pdbId = entry.getId();
          pdb.setId(pdbId);
          pdb.setType(entry.getType());

          /*
           * Store any structure views associated with this sequence. This
           * section copes with duplicate entries in the project, so a dataset
           * only view *should* be coped with sensibly.
           */
          // This must have been loaded, is it still visible?
          JInternalFrame[] frames = Desktop.desktop.getAllFrames();
          String matchedFile = null;
          for (int f = frames.length - 1; f > -1; f--)
          {
            if (frames[f] instanceof StructureViewerBase)
            {
              StructureViewerBase viewFrame = (StructureViewerBase) frames[f];
              matchedFile = saveStructureViewer(ap, jds, pdb, entry,
                      viewIds, matchedFile, viewFrame);
              /*
               * Only store each structure viewer's state once in the project
               * jar. First time through only (storeDS==false)
               */
              String viewId = viewFrame.getViewId();
              String viewerType = viewFrame.getViewerType().toString();
              if (!storeDS && !viewIds.contains(viewId))
              {
                viewIds.add(viewId);
                File viewerState = viewFrame.saveSession();
                if (viewerState != null)
                {
                  copyFileToJar(jout, viewerState.getPath(),
                          getViewerJarEntryName(viewId), viewerType);
                }
                else
                {
                  Console.error(
                          "Failed to save viewer state for " + viewerType);
                }
              }
            }
          }

          if (matchedFile != null || entry.getFile() != null)
          {
            if (entry.getFile() != null)
            {
              // use entry's file
              matchedFile = entry.getFile();
            }
            pdb.setFile(matchedFile); // entry.getFile());
            if (pdbfiles == null)
            {
              pdbfiles = new ArrayList<>();
            }

            if (!pdbfiles.contains(pdbId))
            {
              pdbfiles.add(pdbId);
              copyFileToJar(jout, matchedFile, pdbId, pdbId);
            }
          }

          Enumeration<String> props = entry.getProperties();
          if (props.hasMoreElements())
          {
            // PdbentryItem item = new PdbentryItem();
            while (props.hasMoreElements())
            {
              Property prop = new Property();
              String key = props.nextElement();
              prop.setName(key);
              prop.setValue(entry.getProperty(key).toString());
              // item.addProperty(prop);
              pdb.getProperty().add(prop);
            }
            // pdb.addPdbentryItem(item);
          }

          // jseq.addPdbids(pdb);
          jseq.getPdbids().add(pdb);
        }
      }

      saveRnaViewers(jout, jseq, jds, viewIds, ap, storeDS);

      // jms.addJSeq(jseq);
      object.getJSeq().add(jseq);
    }

    if (!storeDS && av.hasHiddenRows())
    {
      jal = av.getAlignment();
    }
    // SAVE MAPPINGS
    // FOR DATASET
    if (storeDS && jal.getCodonFrames() != null)
    {
      List<AlignedCodonFrame> jac = jal.getCodonFrames();
      for (AlignedCodonFrame acf : jac)
      {
        AlcodonFrame alc = new AlcodonFrame();
        if (acf.getProtMappings() != null
                && acf.getProtMappings().length > 0)
        {
          boolean hasMap = false;
          SequenceI[] dnas = acf.getdnaSeqs();
          jalview.datamodel.Mapping[] pmaps = acf.getProtMappings();
          for (int m = 0; m < pmaps.length; m++)
          {
            AlcodMap alcmap = new AlcodMap();
            alcmap.setDnasq(seqHash(dnas[m]));
            alcmap.setMapping(
                    createVamsasMapping(pmaps[m], dnas[m], null, false));
            // alc.addAlcodMap(alcmap);
            alc.getAlcodMap().add(alcmap);
            hasMap = true;
          }
          if (hasMap)
          {
            // vamsasSet.addAlcodonFrame(alc);
            vamsasSet.getAlcodonFrame().add(alc);
          }
        }
        // TODO: delete this ? dead code from 2.8.3->2.9 ?
        // {
        // AlcodonFrame alc = new AlcodonFrame();
        // vamsasSet.addAlcodonFrame(alc);
        // for (int p = 0; p < acf.aaWidth; p++)
        // {
        // Alcodon cmap = new Alcodon();
        // if (acf.codons[p] != null)
        // {
        // // Null codons indicate a gapped column in the translated peptide
        // // alignment.
        // cmap.setPos1(acf.codons[p][0]);
        // cmap.setPos2(acf.codons[p][1]);
        // cmap.setPos3(acf.codons[p][2]);
        // }
        // alc.addAlcodon(cmap);
        // }
        // if (acf.getProtMappings() != null
        // && acf.getProtMappings().length > 0)
        // {
        // SequenceI[] dnas = acf.getdnaSeqs();
        // jalview.datamodel.Mapping[] pmaps = acf.getProtMappings();
        // for (int m = 0; m < pmaps.length; m++)
        // {
        // AlcodMap alcmap = new AlcodMap();
        // alcmap.setDnasq(seqHash(dnas[m]));
        // alcmap.setMapping(createVamsasMapping(pmaps[m], dnas[m], null,
        // false));
        // alc.addAlcodMap(alcmap);
        // }
        // }
      }
    }

    // SAVE TREES
    // /////////////////////////////////
    if (!storeDS && av.getCurrentTree() != null)
    {
      // FIND ANY ASSOCIATED TREES
      // NOT IMPLEMENTED FOR HEADLESS STATE AT PRESENT
      if (Desktop.desktop != null)
      {
        JInternalFrame[] frames = Desktop.desktop.getAllFrames();

        for (int t = 0; t < frames.length; t++)
        {
          if (frames[t] instanceof TreePanel)
          {
            TreePanel tp = (TreePanel) frames[t];

            if (tp.getTreeCanvas().getViewport().getAlignment() == jal)
            {
              JalviewModel.Tree tree = new JalviewModel.Tree();
              tree.setTitle(tp.getTitle());
              tree.setCurrentTree((av.getCurrentTree() == tp.getTree()));
              tree.setNewick(tp.getTree().print());
              tree.setThreshold(tp.getTreeCanvas().getThreshold());

              tree.setFitToWindow(tp.fitToWindow.getState());
              tree.setFontName(tp.getTreeFont().getName());
              tree.setFontSize(tp.getTreeFont().getSize());
              tree.setFontStyle(tp.getTreeFont().getStyle());
              tree.setMarkUnlinked(tp.placeholdersMenu.getState());

              tree.setShowBootstrap(tp.bootstrapMenu.getState());
              tree.setShowDistances(tp.distanceMenu.getState());

              tree.setHeight(tp.getHeight());
              tree.setWidth(tp.getWidth());
              tree.setXpos(tp.getX());
              tree.setYpos(tp.getY());
              tree.setId(makeHashCode(tp, null));
              tree.setLinkToAllViews(
                      tp.getTreeCanvas().isApplyToAllViews());

              // columnWiseTree
              if (tp.isColumnWise())
              {
                tree.setColumnWise(true);
                String annId = tp.getAssocAnnotation().annotationId;
                tree.setColumnReference(annId);
              }
              // jms.addTree(tree);
              object.getTree().add(tree);
            }
          }
        }
      }
    }

    /*
     * save PCA viewers
     */
    if (!storeDS && Desktop.desktop != null)
    {
      for (JInternalFrame frame : Desktop.desktop.getAllFrames())
      {
        if (frame instanceof PCAPanel)
        {
          PCAPanel panel = (PCAPanel) frame;
          if (panel.getAlignViewport().getAlignment() == jal)
          {
            savePCA(panel, object);
          }
        }
      }
    }

    // SAVE ANNOTATIONS
    /**
     * store forward refs from an annotationRow to any groups
     */
    IdentityHashMap<SequenceGroup, String> groupRefs = new IdentityHashMap<>();
    if (storeDS)
    {
      for (SequenceI sq : jal.getSequences())
      {
        // Store annotation on dataset sequences only
        AlignmentAnnotation[] aa = sq.getAnnotation();
        if (aa != null && aa.length > 0)
        {
          storeAlignmentAnnotation(aa, groupRefs, av, calcIdSet, storeDS,
                  vamsasSet);
        }
      }
    }
    else
    {
      if (jal.getAlignmentAnnotation() != null)
      {
        // Store the annotation shown on the alignment.
        AlignmentAnnotation[] aa = jal.getAlignmentAnnotation();
        storeAlignmentAnnotation(aa, groupRefs, av, calcIdSet, storeDS,
                vamsasSet);
      }
    }
    // SAVE GROUPS
    if (jal.getGroups() != null)
    {
      JGroup[] groups = new JGroup[jal.getGroups().size()];
      int i = -1;
      for (jalview.datamodel.SequenceGroup sg : jal.getGroups())
      {
        JGroup jGroup = new JGroup();
        groups[++i] = jGroup;

        jGroup.setStart(sg.getStartRes());
        jGroup.setEnd(sg.getEndRes());
        jGroup.setName(sg.getName());
        if (groupRefs.containsKey(sg))
        {
          // group has references so set its ID field
          jGroup.setId(groupRefs.get(sg));
        }
        ColourSchemeI colourScheme = sg.getColourScheme();
        if (colourScheme != null)
        {
          ResidueShaderI groupColourScheme = sg.getGroupColourScheme();
          if (groupColourScheme.conservationApplied())
          {
            jGroup.setConsThreshold(groupColourScheme.getConservationInc());

            if (colourScheme instanceof jalview.schemes.UserColourScheme)
            {
              jGroup.setColour(setUserColourScheme(colourScheme,
                      userColours, object));
            }
            else
            {
              jGroup.setColour(colourScheme.getSchemeName());
            }
          }
          else if (colourScheme instanceof jalview.schemes.AnnotationColourGradient)
          {
            jGroup.setColour("AnnotationColourGradient");
            jGroup.setAnnotationColours(constructAnnotationColours(
                    (jalview.schemes.AnnotationColourGradient) colourScheme,
                    userColours, object));
          }
          else if (colourScheme instanceof jalview.schemes.UserColourScheme)
          {
            jGroup.setColour(
                    setUserColourScheme(colourScheme, userColours, object));
          }
          else
          {
            jGroup.setColour(colourScheme.getSchemeName());
          }

          jGroup.setPidThreshold(groupColourScheme.getThreshold());
        }

        jGroup.setOutlineColour(sg.getOutlineColour().getRGB());
        jGroup.setDisplayBoxes(sg.getDisplayBoxes());
        jGroup.setDisplayText(sg.getDisplayText());
        jGroup.setColourText(sg.getColourText());
        jGroup.setTextCol1(sg.textColour.getRGB());
        jGroup.setTextCol2(sg.textColour2.getRGB());
        jGroup.setTextColThreshold(sg.thresholdTextColour);
        jGroup.setShowUnconserved(sg.getShowNonconserved());
        jGroup.setIgnoreGapsinConsensus(sg.getIgnoreGapsConsensus());
        jGroup.setShowConsensusHistogram(sg.isShowConsensusHistogram());
        jGroup.setShowSequenceLogo(sg.isShowSequenceLogo());
        jGroup.setNormaliseSequenceLogo(sg.isNormaliseSequenceLogo());
        for (SequenceI seq : sg.getSequences())
        {
          // jGroup.addSeq(seqHash(seq));
          jGroup.getSeq().add(seqHash(seq));
        }
      }

      // jms.setJGroup(groups);
      Object group;
      for (JGroup grp : groups)
      {
        object.getJGroup().add(grp);
      }
    }
    if (!storeDS)
    {
      // /////////SAVE VIEWPORT
      Viewport view = new Viewport();
      view.setTitle(ap.alignFrame.getTitle());
      view.setSequenceSetId(
              makeHashCode(av.getSequenceSetId(), av.getSequenceSetId()));
      view.setId(av.getViewId());
      if (av.getCodingComplement() != null)
      {
        view.setComplementId(av.getCodingComplement().getViewId());
      }
      view.setViewName(av.getViewName());
      view.setGatheredViews(av.isGatherViewsHere());

      Rectangle size = ap.av.getExplodedGeometry();
      Rectangle position = size;
      if (size == null)
      {
        size = ap.alignFrame.getBounds();
        if (av.getCodingComplement() != null)
        {
          position = ((SplitFrame) ap.alignFrame.getSplitViewContainer())
                  .getBounds();
        }
        else
        {
          position = size;
        }
      }
      view.setXpos(position.x);
      view.setYpos(position.y);

      view.setWidth(size.width);
      view.setHeight(size.height);

      view.setStartRes(vpRanges.getStartRes());
      view.setStartSeq(vpRanges.getStartSeq());

      OverviewPanel ov = ap.getOverviewPanel();
      if (ov != null)
      {
        Overview overview = new Overview();
        overview.setTitle(ov.getTitle());
        Rectangle bounds = ov.getFrameBounds();
        overview.setXpos(bounds.x);
        overview.setYpos(bounds.y);
        overview.setWidth(bounds.width);
        overview.setHeight(bounds.height);
        overview.setShowHidden(ov.isShowHiddenRegions());
        overview.setGapColour(ov.getCanvas().getGapColour().getRGB());
        overview.setResidueColour(
                ov.getCanvas().getResidueColour().getRGB());
        overview.setHiddenColour(ov.getCanvas().getHiddenColour().getRGB());
        view.setOverview(overview);
      }
      if (av.getGlobalColourScheme() instanceof jalview.schemes.UserColourScheme)
      {
        view.setBgColour(setUserColourScheme(av.getGlobalColourScheme(),
                userColours, object));
      }
      else if (av
              .getGlobalColourScheme() instanceof jalview.schemes.AnnotationColourGradient)
      {
        AnnotationColourScheme ac = constructAnnotationColours(
                (jalview.schemes.AnnotationColourGradient) av
                        .getGlobalColourScheme(),
                userColours, object);

        view.setAnnotationColours(ac);
        view.setBgColour("AnnotationColourGradient");
      }
      else
      {
        view.setBgColour(ColourSchemeProperty
                .getColourName(av.getGlobalColourScheme()));
      }

      ResidueShaderI vcs = av.getResidueShading();
      ColourSchemeI cs = av.getGlobalColourScheme();

      if (cs != null)
      {
        if (vcs.conservationApplied())
        {
          view.setConsThreshold(vcs.getConservationInc());
          if (cs instanceof jalview.schemes.UserColourScheme)
          {
            view.setBgColour(setUserColourScheme(cs, userColours, object));
          }
        }
        view.setPidThreshold(vcs.getThreshold());
      }

      view.setConservationSelected(av.getConservationSelected());
      view.setPidSelected(av.getAbovePIDThreshold());
      view.setCharHeight(av.getCharHeight());
      view.setCharWidth(av.getCharWidth());
      final Font font = av.getFont();
      view.setFontName(font.getName());
      view.setFontSize(font.getSize());
      view.setFontStyle(font.getStyle());
      view.setScaleProteinAsCdna(av.getViewStyle().isScaleProteinAsCdna());
      view.setRenderGaps(av.isRenderGaps());
      view.setShowAnnotation(av.isShowAnnotation());
      view.setShowBoxes(av.getShowBoxes());
      view.setShowColourText(av.getColourText());
      view.setShowFullId(av.getShowJVSuffix());
      view.setRightAlignIds(av.isRightAlignIds());
      view.setShowSequenceFeatures(av.isShowSequenceFeatures());
      view.setShowText(av.getShowText());
      view.setShowUnconserved(av.getShowUnconserved());
      view.setWrapAlignment(av.getWrapAlignment());
      view.setTextCol1(av.getTextColour().getRGB());
      view.setTextCol2(av.getTextColour2().getRGB());
      view.setTextColThreshold(av.getThresholdTextColour());
      view.setShowConsensusHistogram(av.isShowConsensusHistogram());
      view.setShowSequenceLogo(av.isShowSequenceLogo());
      view.setNormaliseSequenceLogo(av.isNormaliseSequenceLogo());
      view.setShowGroupConsensus(av.isShowGroupConsensus());
      view.setShowGroupConservation(av.isShowGroupConservation());
      view.setShowNPfeatureTooltip(av.isShowNPFeats());
      view.setShowDbRefTooltip(av.isShowDBRefs());
      view.setFollowHighlight(av.isFollowHighlight());
      view.setFollowSelection(av.followSelection);
      view.setIgnoreGapsinConsensus(av.isIgnoreGapsConsensus());
      view.setShowComplementFeatures(av.isShowComplementFeatures());
      view.setShowComplementFeaturesOnTop(
              av.isShowComplementFeaturesOnTop());
      if (av.getFeaturesDisplayed() != null)
      {
        FeatureSettings fs = new FeatureSettings();

        FeatureRendererModel fr = ap.getSeqPanel().seqCanvas
                .getFeatureRenderer();
        String[] renderOrder = fr.getRenderOrder().toArray(new String[0]);

        Vector<String> settingsAdded = new Vector<>();
        if (renderOrder != null)
        {
          for (String featureType : renderOrder)
          {
            FeatureSettings.Setting setting = new FeatureSettings.Setting();
            setting.setType(featureType);

            /*
             * save any filter for the feature type
             */
            FeatureMatcherSetI filter = fr.getFeatureFilter(featureType);
            if (filter != null)
            {
              Iterator<FeatureMatcherI> filters = filter.getMatchers()
                      .iterator();
              FeatureMatcherI firstFilter = filters.next();
              setting.setMatcherSet(Jalview2XML.marshalFilter(firstFilter,
                      filters, filter.isAnded()));
            }

            /*
             * save colour scheme for the feature type
             */
            FeatureColourI fcol = fr.getFeatureStyle(featureType);
            if (!fcol.isSimpleColour())
            {
              setting.setColour(fcol.getMaxColour().getRGB());
              setting.setMincolour(fcol.getMinColour().getRGB());
              setting.setMin(fcol.getMin());
              setting.setMax(fcol.getMax());
              setting.setColourByLabel(fcol.isColourByLabel());
              if (fcol.isColourByAttribute())
              {
                String[] attName = fcol.getAttributeName();
                setting.getAttributeName().add(attName[0]);
                if (attName.length > 1)
                {
                  setting.getAttributeName().add(attName[1]);
                }
              }
              setting.setAutoScale(fcol.isAutoScaled());
              setting.setThreshold(fcol.getThreshold());
              Color noColour = fcol.getNoColour();
              if (noColour == null)
              {
                setting.setNoValueColour(NoValueColour.NONE);
              }
              else if (noColour.equals(fcol.getMaxColour()))
              {
                setting.setNoValueColour(NoValueColour.MAX);
              }
              else
              {
                setting.setNoValueColour(NoValueColour.MIN);
              }
              // -1 = No threshold, 0 = Below, 1 = Above
              setting.setThreshstate(fcol.isAboveThreshold() ? 1
                      : (fcol.isBelowThreshold() ? 0 : -1));
            }
            else
            {
              setting.setColour(fcol.getColour().getRGB());
            }

            setting.setDisplay(
                    av.getFeaturesDisplayed().isVisible(featureType));
            float rorder = fr.getOrder(featureType);
            if (rorder > -1)
            {
              setting.setOrder(rorder);
            }
            /// fs.addSetting(setting);
            fs.getSetting().add(setting);
            settingsAdded.addElement(featureType);
          }
        }

        // is groups actually supposed to be a map here ?
        Iterator<String> en = fr.getFeatureGroups().iterator();
        Vector<String> groupsAdded = new Vector<>();
        while (en.hasNext())
        {
          String grp = en.next();
          if (groupsAdded.contains(grp))
          {
            continue;
          }
          Group g = new Group();
          g.setName(grp);
          g.setDisplay(((Boolean) fr.checkGroupVisibility(grp, false))
                  .booleanValue());
          // fs.addGroup(g);
          fs.getGroup().add(g);
          groupsAdded.addElement(grp);
        }
        // jms.setFeatureSettings(fs);
        object.setFeatureSettings(fs);
      }

      if (av.hasHiddenColumns())
      {
        jalview.datamodel.HiddenColumns hidden = av.getAlignment()
                .getHiddenColumns();
        if (hidden == null)
        {
          Console.warn(
                  "REPORT BUG: avoided null columnselection bug (DMAM reported). Please contact Jim about this.");
        }
        else
        {
          Iterator<int[]> hiddenRegions = hidden.iterator();
          while (hiddenRegions.hasNext())
          {
            int[] region = hiddenRegions.next();
            HiddenColumns hc = new HiddenColumns();
            hc.setStart(region[0]);
            hc.setEnd(region[1]);
            // view.addHiddenColumns(hc);
            view.getHiddenColumns().add(hc);
          }
        }
      }
      if (calcIdSet.size() > 0)
      {
        for (String calcId : calcIdSet)
        {
          if (calcId.trim().length() > 0)
          {
            CalcIdParam cidp = createCalcIdParam(calcId, av);
            // Some calcIds have no parameters.
            if (cidp != null)
            {
              // view.addCalcIdParam(cidp);
              view.getCalcIdParam().add(cidp);
            }
          }
        }
      }

      // jms.addViewport(view);
      object.getViewport().add(view);
    }
    // object.setJalviewModelSequence(jms);
    // object.getVamsasModel().addSequenceSet(vamsasSet);
    object.getVamsasModel().getSequenceSet().add(vamsasSet);

    if (jout != null && fileName != null)
    {
      // We may not want to write the object to disk,
      // eg we can copy the alignViewport to a new view object
      // using save and then load
      try
      {
        fileName = fileName.replace('\\', '/');
        System.out.println("Writing jar entry " + fileName);
        JarEntry entry = new JarEntry(fileName);
        jout.putNextEntry(entry);
        PrintWriter pout = new PrintWriter(
                new OutputStreamWriter(jout, UTF_8));
        JAXBContext jaxbContext = JAXBContext
                .newInstance(JalviewModel.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        // jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(
                new ObjectFactory().createJalviewModel(object), pout);

        // jaxbMarshaller.marshal(object, pout);
        // marshaller.marshal(object);
        pout.flush();
        jout.closeEntry();
      } catch (Exception ex)
      {
        // TODO: raise error in GUI if marshalling failed.
        System.err.println("Error writing Jalview project");
        ex.printStackTrace();
      }
    }
    return object;
  }

  /**
   * Writes PCA viewer attributes and computed values to an XML model object and
   * adds it to the JalviewModel. Any exceptions are reported by logging.
   */
  protected void savePCA(PCAPanel panel, JalviewModel object)
  {
    try
    {
      PcaViewer viewer = new PcaViewer();
      viewer.setHeight(panel.getHeight());
      viewer.setWidth(panel.getWidth());
      viewer.setXpos(panel.getX());
      viewer.setYpos(panel.getY());
      viewer.setTitle(panel.getTitle());
      PCAModel pcaModel = panel.getPcaModel();
      viewer.setScoreModelName(pcaModel.getScoreModelName());
      viewer.setXDim(panel.getSelectedDimensionIndex(X));
      viewer.setYDim(panel.getSelectedDimensionIndex(Y));
      viewer.setZDim(panel.getSelectedDimensionIndex(Z));
      viewer.setBgColour(
              panel.getRotatableCanvas().getBackgroundColour().getRGB());
      viewer.setScaleFactor(panel.getRotatableCanvas().getScaleFactor());
      float[] spMin = panel.getRotatableCanvas().getSeqMin();
      SeqPointMin spmin = new SeqPointMin();
      spmin.setXPos(spMin[0]);
      spmin.setYPos(spMin[1]);
      spmin.setZPos(spMin[2]);
      viewer.setSeqPointMin(spmin);
      float[] spMax = panel.getRotatableCanvas().getSeqMax();
      SeqPointMax spmax = new SeqPointMax();
      spmax.setXPos(spMax[0]);
      spmax.setYPos(spMax[1]);
      spmax.setZPos(spMax[2]);
      viewer.setSeqPointMax(spmax);
      viewer.setShowLabels(panel.getRotatableCanvas().isShowLabels());
      viewer.setLinkToAllViews(
              panel.getRotatableCanvas().isApplyToAllViews());
      SimilarityParamsI sp = pcaModel.getSimilarityParameters();
      viewer.setIncludeGaps(sp.includeGaps());
      viewer.setMatchGaps(sp.matchGaps());
      viewer.setIncludeGappedColumns(sp.includeGappedColumns());
      viewer.setDenominateByShortestLength(sp.denominateByShortestLength());

      /*
       * sequence points on display
       */
      for (jalview.datamodel.SequencePoint spt : pcaModel
              .getSequencePoints())
      {
        SequencePoint point = new SequencePoint();
        point.setSequenceRef(seqHash(spt.getSequence()));
        point.setXPos(spt.coord.x);
        point.setYPos(spt.coord.y);
        point.setZPos(spt.coord.z);
        viewer.getSequencePoint().add(point);
      }

      /*
       * (end points of) axes on display
       */
      for (Point p : panel.getRotatableCanvas().getAxisEndPoints())
      {

        Axis axis = new Axis();
        axis.setXPos(p.x);
        axis.setYPos(p.y);
        axis.setZPos(p.z);
        viewer.getAxis().add(axis);
      }

      /*
       * raw PCA data (note we are not restoring PCA inputs here -
       * alignment view, score model, similarity parameters)
       */
      PcaDataType data = new PcaDataType();
      viewer.setPcaData(data);
      PCA pca = pcaModel.getPcaData();

      DoubleMatrix pm = new DoubleMatrix();
      saveDoubleMatrix(pca.getPairwiseScores(), pm);
      data.setPairwiseMatrix(pm);

      DoubleMatrix tm = new DoubleMatrix();
      saveDoubleMatrix(pca.getTridiagonal(), tm);
      data.setTridiagonalMatrix(tm);

      DoubleMatrix eigenMatrix = new DoubleMatrix();
      data.setEigenMatrix(eigenMatrix);
      saveDoubleMatrix(pca.getEigenmatrix(), eigenMatrix);

      object.getPcaViewer().add(viewer);
    } catch (Throwable t)
    {
      Console.error("Error saving PCA: " + t.getMessage());
    }
  }

  /**
   * Stores values from a matrix into an XML element, including (if present) the
   * D or E vectors
   * 
   * @param m
   * @param xmlMatrix
   * @see #loadDoubleMatrix(DoubleMatrix)
   */
  protected void saveDoubleMatrix(MatrixI m, DoubleMatrix xmlMatrix)
  {
    xmlMatrix.setRows(m.height());
    xmlMatrix.setColumns(m.width());
    for (int i = 0; i < m.height(); i++)
    {
      DoubleVector row = new DoubleVector();
      for (int j = 0; j < m.width(); j++)
      {
        row.getV().add(m.getValue(i, j));
      }
      xmlMatrix.getRow().add(row);
    }
    if (m.getD() != null)
    {
      DoubleVector dVector = new DoubleVector();
      for (double d : m.getD())
      {
        dVector.getV().add(d);
      }
      xmlMatrix.setD(dVector);
    }
    if (m.getE() != null)
    {
      DoubleVector eVector = new DoubleVector();
      for (double e : m.getE())
      {
        eVector.getV().add(e);
      }
      xmlMatrix.setE(eVector);
    }
  }

  /**
   * Loads XML matrix data into a new Matrix object, including the D and/or E
   * vectors (if present)
   * 
   * @param mData
   * @return
   * @see Jalview2XML#saveDoubleMatrix(MatrixI, DoubleMatrix)
   */
  protected MatrixI loadDoubleMatrix(DoubleMatrix mData)
  {
    int rows = mData.getRows();
    double[][] vals = new double[rows][];

    for (int i = 0; i < rows; i++)
    {
      List<Double> dVector = mData.getRow().get(i).getV();
      vals[i] = new double[dVector.size()];
      int dvi = 0;
      for (Double d : dVector)
      {
        vals[i][dvi++] = d;
      }
    }

    MatrixI m = new Matrix(vals);

    if (mData.getD() != null)
    {
      List<Double> dVector = mData.getD().getV();
      double[] vec = new double[dVector.size()];
      int dvi = 0;
      for (Double d : dVector)
      {
        vec[dvi++] = d;
      }
      m.setD(vec);
    }
    if (mData.getE() != null)
    {
      List<Double> dVector = mData.getE().getV();
      double[] vec = new double[dVector.size()];
      int dvi = 0;
      for (Double d : dVector)
      {
        vec[dvi++] = d;
      }
      m.setE(vec);
    }

    return m;
  }

  /**
   * Save any Varna viewers linked to this sequence. Writes an rnaViewer element
   * for each viewer, with
   * <ul>
   * <li>viewer geometry (position, size, split pane divider location)</li>
   * <li>index of the selected structure in the viewer (currently shows gapped
   * or ungapped)</li>
   * <li>the id of the annotation holding RNA secondary structure</li>
   * <li>(currently only one SS is shown per viewer, may be more in future)</li>
   * </ul>
   * Varna viewer state is also written out (in native Varna XML) to separate
   * project jar entries. A separate entry is written for each RNA structure
   * displayed, with the naming convention
   * <ul>
   * <li>rna_viewId_sequenceId_annotationId_[gapped|trimmed]</li>
   * </ul>
   * 
   * @param jout
   * @param jseq
   * @param jds
   * @param viewIds
   * @param ap
   * @param storeDataset
   */
  protected void saveRnaViewers(JarOutputStream jout, JSeq jseq,
          final SequenceI jds, List<String> viewIds, AlignmentPanel ap,
          boolean storeDataset)
  {
    if (Desktop.desktop == null)
    {
      return;
    }
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();
    for (int f = frames.length - 1; f > -1; f--)
    {
      if (frames[f] instanceof AppVarna)
      {
        AppVarna varna = (AppVarna) frames[f];
        /*
         * link the sequence to every viewer that is showing it and is linked to
         * its alignment panel
         */
        if (varna.isListeningFor(jds) && ap == varna.getAlignmentPanel())
        {
          String viewId = varna.getViewId();
          RnaViewer rna = new RnaViewer();
          rna.setViewId(viewId);
          rna.setTitle(varna.getTitle());
          rna.setXpos(varna.getX());
          rna.setYpos(varna.getY());
          rna.setWidth(varna.getWidth());
          rna.setHeight(varna.getHeight());
          rna.setDividerLocation(varna.getDividerLocation());
          rna.setSelectedRna(varna.getSelectedIndex());
          // jseq.addRnaViewer(rna);
          jseq.getRnaViewer().add(rna);

          /*
           * Store each Varna panel's state once in the project per sequence.
           * First time through only (storeDataset==false)
           */
          // boolean storeSessions = false;
          // String sequenceViewId = viewId + seqsToIds.get(jds);
          // if (!storeDataset && !viewIds.contains(sequenceViewId))
          // {
          // viewIds.add(sequenceViewId);
          // storeSessions = true;
          // }
          for (RnaModel model : varna.getModels())
          {
            if (model.seq == jds)
            {
              /*
               * VARNA saves each view (sequence or alignment secondary
               * structure, gapped or trimmed) as a separate XML file
               */
              String jarEntryName = rnaSessions.get(model);
              if (jarEntryName == null)
              {

                String varnaStateFile = varna.getStateInfo(model.rna);
                jarEntryName = RNA_PREFIX + viewId + "_" + nextCounter();
                copyFileToJar(jout, varnaStateFile, jarEntryName, "Varna");
                rnaSessions.put(model, jarEntryName);
              }
              SecondaryStructure ss = new SecondaryStructure();
              String annotationId = varna.getAnnotation(jds).annotationId;
              ss.setAnnotationId(annotationId);
              ss.setViewerState(jarEntryName);
              ss.setGapped(model.gapped);
              ss.setTitle(model.title);
              // rna.addSecondaryStructure(ss);
              rna.getSecondaryStructure().add(ss);
            }
          }
        }
      }
    }
  }

  /**
   * Copy the contents of a file to a new entry added to the output jar
   * 
   * @param jout
   * @param infilePath
   * @param jarEntryName
   * @param msg
   *          additional identifying info to log to the console
   */
  protected void copyFileToJar(JarOutputStream jout, String infilePath,
          String jarEntryName, String msg)
  {
    try (InputStream is = new FileInputStream(infilePath))
    {
      File file = new File(infilePath);
      if (file.exists() && jout != null)
      {
        System.out.println(
                "Writing jar entry " + jarEntryName + " (" + msg + ")");
        jout.putNextEntry(new JarEntry(jarEntryName));
        copyAll(is, jout);
        jout.closeEntry();
        // dis = new DataInputStream(new FileInputStream(file));
        // byte[] data = new byte[(int) file.length()];
        // dis.readFully(data);
        // writeJarEntry(jout, jarEntryName, data);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Copies input to output, in 4K buffers; handles any data (text or binary)
   * 
   * @param in
   * @param out
   * @throws IOException
   */
  protected void copyAll(InputStream in, OutputStream out)
          throws IOException
  {
    byte[] buffer = new byte[4096];
    int bytesRead = 0;
    while ((bytesRead = in.read(buffer)) != -1)
    {
      out.write(buffer, 0, bytesRead);
    }
  }

  /**
   * Save the state of a structure viewer
   * 
   * @param ap
   * @param jds
   * @param pdb
   *          the archive XML element under which to save the state
   * @param entry
   * @param viewIds
   * @param matchedFile
   * @param viewFrame
   * @return
   */
  protected String saveStructureViewer(AlignmentPanel ap, SequenceI jds,
          Pdbids pdb, PDBEntry entry, List<String> viewIds,
          String matchedFile, StructureViewerBase viewFrame)
  {
    final AAStructureBindingModel bindingModel = viewFrame.getBinding();

    /*
     * Look for any bindings for this viewer to the PDB file of interest
     * (including part matches excluding chain id)
     */
    for (int peid = 0; peid < bindingModel.getPdbCount(); peid++)
    {
      final PDBEntry pdbentry = bindingModel.getPdbEntry(peid);
      final String pdbId = pdbentry.getId();
      if (!pdbId.equals(entry.getId()) && !(entry.getId().length() > 4
              && entry.getId().toLowerCase(Locale.ROOT)
                      .startsWith(pdbId.toLowerCase(Locale.ROOT))))
      {
        /*
         * not interested in a binding to a different PDB entry here
         */
        continue;
      }
      if (matchedFile == null)
      {
        matchedFile = pdbentry.getFile();
      }
      else if (!matchedFile.equals(pdbentry.getFile()))
      {
        Console.warn(
                "Probably lost some PDB-Sequence mappings for this structure file (which apparently has same PDB Entry code): "
                        + pdbentry.getFile());
      }
      // record the
      // file so we
      // can get at it if the ID
      // match is ambiguous (e.g.
      // 1QIP==1qipA)

      for (int smap = 0; smap < viewFrame.getBinding()
              .getSequence()[peid].length; smap++)
      {
        // if (jal.findIndex(jmol.jmb.sequence[peid][smap]) > -1)
        if (jds == viewFrame.getBinding().getSequence()[peid][smap])
        {
          StructureState state = new StructureState();
          state.setVisible(true);
          state.setXpos(viewFrame.getX());
          state.setYpos(viewFrame.getY());
          state.setWidth(viewFrame.getWidth());
          state.setHeight(viewFrame.getHeight());
          final String viewId = viewFrame.getViewId();
          state.setViewId(viewId);
          state.setAlignwithAlignPanel(viewFrame.isUsedforaligment(ap));
          state.setColourwithAlignPanel(viewFrame.isUsedForColourBy(ap));
          state.setColourByJmol(viewFrame.isColouredByViewer());
          state.setType(viewFrame.getViewerType().toString());
          // pdb.addStructureState(state);
          pdb.getStructureState().add(state);
        }
      }
    }
    return matchedFile;
  }

  /**
   * Populates the AnnotationColourScheme xml for save. This captures the
   * settings of the options in the 'Colour by Annotation' dialog.
   * 
   * @param acg
   * @param userColours
   * @param jm
   * @return
   */
  private AnnotationColourScheme constructAnnotationColours(
          AnnotationColourGradient acg, List<UserColourScheme> userColours,
          JalviewModel jm)
  {
    AnnotationColourScheme ac = new AnnotationColourScheme();
    ac.setAboveThreshold(acg.getAboveThreshold());
    ac.setThreshold(acg.getAnnotationThreshold());
    // 2.10.2 save annotationId (unique) not annotation label
    ac.setAnnotation(acg.getAnnotation().annotationId);
    if (acg.getBaseColour() instanceof UserColourScheme)
    {
      ac.setColourScheme(
              setUserColourScheme(acg.getBaseColour(), userColours, jm));
    }
    else
    {
      ac.setColourScheme(
              ColourSchemeProperty.getColourName(acg.getBaseColour()));
    }

    ac.setMaxColour(acg.getMaxColour().getRGB());
    ac.setMinColour(acg.getMinColour().getRGB());
    ac.setPerSequence(acg.isSeqAssociated());
    ac.setPredefinedColours(acg.isPredefinedColours());
    return ac;
  }

  private void storeAlignmentAnnotation(AlignmentAnnotation[] aa,
          IdentityHashMap<SequenceGroup, String> groupRefs,
          AlignmentViewport av, Set<String> calcIdSet, boolean storeDS,
          SequenceSet vamsasSet)
  {

    for (int i = 0; i < aa.length; i++)
    {
      Annotation an = new Annotation();

      AlignmentAnnotation annotation = aa[i];
      if (annotation.annotationId != null)
      {
        annotationIds.put(annotation.annotationId, annotation);
      }

      an.setId(annotation.annotationId);

      an.setVisible(annotation.visible);

      an.setDescription(annotation.description);

      if (annotation.sequenceRef != null)
      {
        // 2.9 JAL-1781 xref on sequence id rather than name
        an.setSequenceRef(seqsToIds.get(annotation.sequenceRef));
      }
      if (annotation.groupRef != null)
      {
        String groupIdr = groupRefs.get(annotation.groupRef);
        if (groupIdr == null)
        {
          // make a locally unique String
          groupRefs.put(annotation.groupRef,
                  groupIdr = ("" + System.currentTimeMillis()
                          + annotation.groupRef.getName()
                          + groupRefs.size()));
        }
        an.setGroupRef(groupIdr.toString());
      }

      // store all visualization attributes for annotation
      an.setGraphHeight(annotation.graphHeight);
      an.setCentreColLabels(annotation.centreColLabels);
      an.setScaleColLabels(annotation.scaleColLabel);
      an.setShowAllColLabels(annotation.showAllColLabels);
      an.setBelowAlignment(annotation.belowAlignment);

      if (annotation.graph > 0)
      {
        an.setGraph(true);
        an.setGraphType(annotation.graph);
        an.setGraphGroup(annotation.graphGroup);
        if (annotation.getThreshold() != null)
        {
          ThresholdLine line = new ThresholdLine();
          line.setLabel(annotation.getThreshold().label);
          line.setValue(annotation.getThreshold().value);
          line.setColour(annotation.getThreshold().colour.getRGB());
          an.setThresholdLine(line);
        }
        if (annotation.graph == AlignmentAnnotation.CONTACT_MAP)
        {
          if (annotation.sequenceRef.getContactMaps() != null)
          {
            ContactMatrixI cm = annotation.sequenceRef
                    .getContactMatrixFor(annotation);
            if (cm != null)
            {
              MatrixType xmlmat = new MatrixType();
              xmlmat.setType(cm.getType());
              xmlmat.setRows(BigInteger.valueOf(cm.getWidth()));
              xmlmat.setCols(BigInteger.valueOf(cm.getHeight()));
              // consider using an opaque to/from -> allow instance to control
              // its representation ?
              xmlmat.setElements(ContactMatrix.contactToFloatString(cm));
              if (cm.hasGroups())
              {
                for (BitSet gp : cm.getGroups())
                {
                  xmlmat.getGroups().add(stringifyBitset(gp));
                }
              }
              if (cm.hasTree())
              {
                // provenance object for tree ?
                xmlmat.getNewick().add(cm.getNewick());
                xmlmat.setTreeMethod(cm.getTreeMethod());
              }
              if (cm.hasCutHeight())
              {
                xmlmat.setCutHeight(cm.getCutHeight());
              }
              // set/get properties
              if (cm instanceof MappableContactMatrixI)
              {
                jalview.util.MapList mlst = ((MappableContactMatrixI) cm)
                        .getMapFor(annotation.sequenceRef);
                if (mlst != null)
                {
                  MapListType mp = new MapListType();
                  List<int[]> r = mlst.getFromRanges();
                  for (int[] range : r)
                  {
                    MapListFrom mfrom = new MapListFrom();
                    mfrom.setStart(range[0]);
                    mfrom.setEnd(range[1]);
                    // mp.addMapListFrom(mfrom);
                    mp.getMapListFrom().add(mfrom);
                  }
                  r = mlst.getToRanges();
                  for (int[] range : r)
                  {
                    MapListTo mto = new MapListTo();
                    mto.setStart(range[0]);
                    mto.setEnd(range[1]);
                    // mp.addMapListTo(mto);
                    mp.getMapListTo().add(mto);
                  }
                  mp.setMapFromUnit(
                          BigInteger.valueOf(mlst.getFromRatio()));
                  mp.setMapToUnit(BigInteger.valueOf(mlst.getToRatio()));
                  xmlmat.setMapping(mp);
                }
              }
              // and add to model
              an.getContactmatrix().add(xmlmat);
            }
          }
        }
      }
      else
      {
        an.setGraph(false);
      }

      an.setLabel(annotation.label);

      if (annotation == av.getAlignmentQualityAnnot()
              || annotation == av.getAlignmentConservationAnnotation()
              || annotation == av.getAlignmentConsensusAnnotation()
              || annotation.autoCalculated)
      {
        // new way of indicating autocalculated annotation -
        an.setAutoCalculated(annotation.autoCalculated);
      }
      if (annotation.hasScore())
      {
        an.setScore(annotation.getScore());
      }

      if (annotation.getCalcId() != null)
      {
        calcIdSet.add(annotation.getCalcId());
        an.setCalcId(annotation.getCalcId());
      }
      if (annotation.hasProperties())
      {
        for (String pr : annotation.getProperties())
        {
          jalview.xml.binding.jalview.Property prop = new jalview.xml.binding.jalview.Property();
          prop.setName(pr);
          prop.setValue(annotation.getProperty(pr));
          an.getProperty().add(prop);
        }
      }

      AnnotationElement ae;
      if (annotation.annotations != null)
      {
        an.setScoreOnly(false);
        for (int a = 0; a < annotation.annotations.length; a++)
        {
          if ((annotation == null) || (annotation.annotations[a] == null))
          {
            continue;
          }

          ae = new AnnotationElement();
          if (annotation.annotations[a].description != null)
          {
            ae.setDescription(annotation.annotations[a].description);
          }
          if (annotation.annotations[a].displayCharacter != null)
          {
            ae.setDisplayCharacter(
                    annotation.annotations[a].displayCharacter);
          }

          if (!Float.isNaN(annotation.annotations[a].value))
          {
            ae.setValue(annotation.annotations[a].value);
          }

          ae.setPosition(a);
          if (annotation.annotations[a].secondaryStructure > ' ')
          {
            ae.setSecondaryStructure(
                    annotation.annotations[a].secondaryStructure + "");
          }

          if (annotation.annotations[a].colour != null
                  && annotation.annotations[a].colour != java.awt.Color.black)
          {
            ae.setColour(annotation.annotations[a].colour.getRGB());
          }

          // an.addAnnotationElement(ae);
          an.getAnnotationElement().add(ae);
          if (annotation.autoCalculated)
          {
            // only write one non-null entry into the annotation row -
            // sufficient to get the visualization attributes necessary to
            // display data
            continue;
          }
        }
      }
      else
      {
        an.setScoreOnly(true);
      }
      if (!storeDS || (storeDS && !annotation.autoCalculated))
      {
        // skip autocalculated annotation - these are only provided for
        // alignments
        // vamsasSet.addAnnotation(an);
        vamsasSet.getAnnotation().add(an);
      }
    }

  }

  private String stringifyBitset(BitSet gp)
  {
    StringBuilder sb = new StringBuilder();
    for (long val : gp.toLongArray())
    {
      if (sb.length() > 0)
      {
        sb.append(",");
      }
      sb.append(val);
    }
    return sb.toString();
  }

  private BitSet deStringifyBitset(String stringified)
  {
    if ("".equals(stringified) || stringified == null)
    {
      return new BitSet();
    }
    String[] longvals = stringified.split(",");
    long[] newlongvals = new long[longvals.length];
    for (int lv = 0; lv < longvals.length; lv++)
    {
      try
      {
        newlongvals[lv] = Long.valueOf(longvals[lv]);
      } catch (Exception x)
      {
        errorMessage += "Couldn't destringify bitset from: '" + stringified
                + "'";
        newlongvals[lv] = 0;
      }
    }
    return BitSet.valueOf(newlongvals);

  }
  private CalcIdParam createCalcIdParam(String calcId, AlignViewport av)
  {
    AutoCalcSetting settings = av.getCalcIdSettingsFor(calcId);
    if (settings != null)
    {
      CalcIdParam vCalcIdParam = new CalcIdParam();
      vCalcIdParam.setCalcId(calcId);
      // vCalcIdParam.addServiceURL(settings.getServiceURI());
      vCalcIdParam.getServiceURL().add(settings.getServiceURI());
      // generic URI allowing a third party to resolve another instance of the
      // service used for this calculation
      for (String url : settings.getServiceURLs())
      {
        // vCalcIdParam.addServiceURL(urls);
        vCalcIdParam.getServiceURL().add(url);
      }
      vCalcIdParam.setVersion("1.0");
      if (settings.getPreset() != null)
      {
        WsParamSetI setting = settings.getPreset();
        vCalcIdParam.setName(setting.getName());
        vCalcIdParam.setDescription(setting.getDescription());
      }
      else
      {
        vCalcIdParam.setName("");
        vCalcIdParam.setDescription("Last used parameters");
      }
      // need to be able to recover 1) settings 2) user-defined presets or
      // recreate settings from preset 3) predefined settings provided by
      // service - or settings that can be transferred (or discarded)
      vCalcIdParam.setParameters(
              settings.getWsParamFile().replace("\n", "|\\n|"));
      vCalcIdParam.setAutoUpdate(settings.isAutoUpdate());
      // todo - decide if updateImmediately is needed for any projects.

      return vCalcIdParam;
    }
    return null;
  }

  private boolean recoverCalcIdParam(CalcIdParam calcIdParam,
          AlignViewport av)
  {
    if (calcIdParam.getVersion().equals("1.0"))
    {
      final String[] calcIds = calcIdParam.getServiceURL()
              .toArray(new String[0]);
      Jws2Instance service = Jws2Discoverer.getDiscoverer()
              .getPreferredServiceFor(calcIds);
      if (service != null)
      {
        WsParamSetI parmSet = null;
        try
        {
          parmSet = service.getParamStore().parseServiceParameterFile(
                  calcIdParam.getName(), calcIdParam.getDescription(),
                  calcIds,
                  calcIdParam.getParameters().replace("|\\n|", "\n"));
        } catch (IOException x)
        {
          Console.warn("Couldn't parse parameter data for "
                  + calcIdParam.getCalcId(), x);
          return false;
        }
        List<ArgumentI> argList = null;
        if (calcIdParam.getName().length() > 0)
        {
          parmSet = service.getParamStore()
                  .getPreset(calcIdParam.getName());
          if (parmSet != null)
          {
            // TODO : check we have a good match with settings in AACon -
            // otherwise we'll need to create a new preset
          }
        }
        else
        {
          argList = parmSet.getArguments();
          parmSet = null;
        }
        AAConSettings settings = new AAConSettings(
                calcIdParam.isAutoUpdate(), service, parmSet, argList);
        av.setCalcIdSettingsFor(calcIdParam.getCalcId(), settings,
                calcIdParam.isNeedsUpdate());
        return true;
      }
      else
      {
        Console.warn(
                "Cannot resolve a service for the parameters used in this project. Try configuring a JABAWS server.");
        return false;
      }
    }
    throw new Error(MessageManager.formatMessage(
            "error.unsupported_version_calcIdparam", new Object[]
            { calcIdParam.toString() }));
  }

  /**
   * External mapping between jalview objects and objects yielding a valid and
   * unique object ID string. This is null for normal Jalview project IO, but
   * non-null when a jalview project is being read or written as part of a
   * vamsas session.
   */
  IdentityHashMap jv2vobj = null;

  /**
   * Construct a unique ID for jvobj using either existing bindings or if none
   * exist, the result of the hashcode call for the object.
   * 
   * @param jvobj
   *          jalview data object
   * @return unique ID for referring to jvobj
   */
  private String makeHashCode(Object jvobj, String altCode)
  {
    if (jv2vobj != null)
    {
      Object id = jv2vobj.get(jvobj);
      if (id != null)
      {
        return id.toString();
      }
      // check string ID mappings
      if (jvids2vobj != null && jvobj instanceof String)
      {
        id = jvids2vobj.get(jvobj);
      }
      if (id != null)
      {
        return id.toString();
      }
      // give up and warn that something has gone wrong
      Console.warn(
              "Cannot find ID for object in external mapping : " + jvobj);
    }
    return altCode;
  }

  /**
   * return local jalview object mapped to ID, if it exists
   * 
   * @param idcode
   *          (may be null)
   * @return null or object bound to idcode
   */
  private Object retrieveExistingObj(String idcode)
  {
    if (idcode != null && vobj2jv != null)
    {
      return vobj2jv.get(idcode);
    }
    return null;
  }

  /**
   * binding from ID strings from external mapping table to jalview data model
   * objects.
   */
  private Hashtable vobj2jv;

  private Sequence createVamsasSequence(String id, SequenceI jds)
  {
    return createVamsasSequence(true, id, jds, null);
  }

  private Sequence createVamsasSequence(boolean recurse, String id,
          SequenceI jds, SequenceI parentseq)
  {
    Sequence vamsasSeq = new Sequence();
    vamsasSeq.setId(id);
    vamsasSeq.setName(jds.getName());
    vamsasSeq.setSequence(jds.getSequenceAsString());
    vamsasSeq.setDescription(jds.getDescription());
    List<DBRefEntry> dbrefs = null;
    if (jds.getDatasetSequence() != null)
    {
      vamsasSeq.setDsseqid(seqHash(jds.getDatasetSequence()));
    }
    else
    {
      // seqId==dsseqid so we can tell which sequences really are
      // dataset sequences only
      vamsasSeq.setDsseqid(id);
      dbrefs = jds.getDBRefs();
      if (parentseq == null)
      {
        parentseq = jds;
      }
    }

    /*
     * save any dbrefs; special subclass GeneLocus is flagged as 'locus'
     */
    if (dbrefs != null)
    {
      for (int d = 0, nd = dbrefs.size(); d < nd; d++)
      {
        DBRef dbref = new DBRef();
        DBRefEntry ref = dbrefs.get(d);
        dbref.setSource(ref.getSource());
        dbref.setVersion(ref.getVersion());
        dbref.setAccessionId(ref.getAccessionId());
        dbref.setCanonical(ref.isCanonical());
        if (ref instanceof GeneLocus)
        {
          dbref.setLocus(true);
        }
        if (ref.hasMap())
        {
          Mapping mp = createVamsasMapping(ref.getMap(), parentseq, jds,
                  recurse);
          dbref.setMapping(mp);
        }
        vamsasSeq.getDBRef().add(dbref);
      }
    }
    return vamsasSeq;
  }

  private Mapping createVamsasMapping(jalview.datamodel.Mapping jmp,
          SequenceI parentseq, SequenceI jds, boolean recurse)
  {
    Mapping mp = null;
    if (jmp.getMap() != null)
    {
      mp = new Mapping();

      jalview.util.MapList mlst = jmp.getMap();
      List<int[]> r = mlst.getFromRanges();
      for (int[] range : r)
      {
        MapListFrom mfrom = new MapListFrom();
        mfrom.setStart(range[0]);
        mfrom.setEnd(range[1]);
        // mp.addMapListFrom(mfrom);
        mp.getMapListFrom().add(mfrom);
      }
      r = mlst.getToRanges();
      for (int[] range : r)
      {
        MapListTo mto = new MapListTo();
        mto.setStart(range[0]);
        mto.setEnd(range[1]);
        // mp.addMapListTo(mto);
        mp.getMapListTo().add(mto);
      }
      mp.setMapFromUnit(BigInteger.valueOf(mlst.getFromRatio()));
      mp.setMapToUnit(BigInteger.valueOf(mlst.getToRatio()));
      if (jmp.getTo() != null)
      {
        // MappingChoice mpc = new MappingChoice();

        // check/create ID for the sequence referenced by getTo()

        String jmpid = "";
        SequenceI ps = null;
        if (parentseq != jmp.getTo()
                && parentseq.getDatasetSequence() != jmp.getTo())
        {
          // chaining dbref rather than a handshaking one
          jmpid = seqHash(ps = jmp.getTo());
        }
        else
        {
          jmpid = seqHash(ps = parentseq);
        }
        // mpc.setDseqFor(jmpid);
        mp.setDseqFor(jmpid);
        if (!seqRefIds.containsKey(jmpid))
        {
          Console.debug("creatign new DseqFor ID");
          seqRefIds.put(jmpid, ps);
        }
        else
        {
          Console.debug("reusing DseqFor ID");
        }

        // mp.setMappingChoice(mpc);
      }
    }
    return mp;
  }

  String setUserColourScheme(jalview.schemes.ColourSchemeI cs,
          List<UserColourScheme> userColours, JalviewModel jm)
  {
    String id = null;
    jalview.schemes.UserColourScheme ucs = (jalview.schemes.UserColourScheme) cs;
    boolean newucs = false;
    if (!userColours.contains(ucs))
    {
      userColours.add(ucs);
      newucs = true;
    }
    id = "ucs" + userColours.indexOf(ucs);
    if (newucs)
    {
      // actually create the scheme's entry in the XML model
      java.awt.Color[] colours = ucs.getColours();
      UserColours uc = new UserColours();
      // UserColourScheme jbucs = new UserColourScheme();
      JalviewUserColours jbucs = new JalviewUserColours();

      for (int i = 0; i < colours.length; i++)
      {
        Colour col = new Colour();
        col.setName(ResidueProperties.aa[i]);
        col.setRGB(jalview.util.Format.getHexString(colours[i]));
        // jbucs.addColour(col);
        jbucs.getColour().add(col);
      }
      if (ucs.getLowerCaseColours() != null)
      {
        colours = ucs.getLowerCaseColours();
        for (int i = 0; i < colours.length; i++)
        {
          Colour col = new Colour();
          col.setName(ResidueProperties.aa[i].toLowerCase(Locale.ROOT));
          col.setRGB(jalview.util.Format.getHexString(colours[i]));
          // jbucs.addColour(col);
          jbucs.getColour().add(col);
        }
      }

      uc.setId(id);
      uc.setUserColourScheme(jbucs);
      // jm.addUserColours(uc);
      jm.getUserColours().add(uc);
    }

    return id;
  }

  jalview.schemes.UserColourScheme getUserColourScheme(JalviewModel jm,
          String id)
  {
    List<UserColours> uc = jm.getUserColours();
    UserColours colours = null;
    /*
    for (int i = 0; i < uc.length; i++)
    {
      if (uc[i].getId().equals(id))
      {
        colours = uc[i];
        break;
      }
    }
    */
    for (UserColours c : uc)
    {
      if (c.getId().equals(id))
      {
        colours = c;
        break;
      }
    }

    java.awt.Color[] newColours = new java.awt.Color[24];

    for (int i = 0; i < 24; i++)
    {
      newColours[i] = new java.awt.Color(Integer.parseInt(
              // colours.getUserColourScheme().getColour(i).getRGB(), 16));
              colours.getUserColourScheme().getColour().get(i).getRGB(),
              16));
    }

    jalview.schemes.UserColourScheme ucs = new jalview.schemes.UserColourScheme(
            newColours);

    if (colours.getUserColourScheme().getColour().size()/*Count()*/ > 24)
    {
      newColours = new java.awt.Color[23];
      for (int i = 0; i < 23; i++)
      {
        newColours[i] = new java.awt.Color(
                Integer.parseInt(colours.getUserColourScheme().getColour()
                        .get(i + 24).getRGB(), 16));
      }
      ucs.setLowerCaseColours(newColours);
    }

    return ucs;
  }

  /**
   * contains last error message (if any) encountered by XML loader.
   */
  String errorMessage = null;

  /**
   * flag to control whether the Jalview2XML_V1 parser should be deferred to if
   * exceptions are raised during project XML parsing
   */
  public boolean attemptversion1parse = false;

  /**
   * Load a jalview project archive from a jar file
   * 
   * @param file
   *          - HTTP URL or filename
   */
  public AlignFrame loadJalviewAlign(final Object file)
  {

    jalview.gui.AlignFrame af = null;

    try
    {
      // create list to store references for any new Jmol viewers created
      newStructureViewers = new Vector<>();
      // UNMARSHALLER SEEMS TO CLOSE JARINPUTSTREAM, MOST ANNOYING
      // Workaround is to make sure caller implements the JarInputStreamProvider
      // interface
      // so we can re-open the jar input stream for each entry.

      jarInputStreamProvider jprovider = createjarInputStreamProvider(file);
      af = loadJalviewAlign(jprovider);
      if (af != null)
      {
        af.setMenusForViewport();
      }
    } catch (MalformedURLException e)
    {
      errorMessage = "Invalid URL format for '" + file + "'";
      reportErrors();
    } finally
    {
      try
      {
        SwingUtilities.invokeAndWait(new Runnable()
        {
          @Override
          public void run()
          {
            setLoadingFinishedForNewStructureViewers();
          }
        });
      } catch (Exception x)
      {
        System.err.println("Error loading alignment: " + x.getMessage());
      }
    }
    return af;
  }

  @SuppressWarnings("unused")
  private jarInputStreamProvider createjarInputStreamProvider(
          final Object ofile) throws MalformedURLException
  {

    // BH 2018 allow for bytes already attached to File object
    try
    {
      String file = (ofile instanceof File
              ? ((File) ofile).getCanonicalPath()
              : ofile.toString());
      byte[] bytes = Platform.isJS() ? Platform.getFileBytes((File) ofile)
              : null;
      URL url = null;
      errorMessage = null;
      uniqueSetSuffix = null;
      seqRefIds = null;
      viewportsAdded.clear();
      frefedSequence = null;

      if (HttpUtils.startsWithHttpOrHttps(file))
      {
        url = new URL(file);
      }
      final URL _url = url;
      return new jarInputStreamProvider()
      {

        @Override
        public JarInputStream getJarInputStream() throws IOException
        {
          if (bytes != null)
          {
            // System.out.println("Jalview2XML: opening byte jarInputStream for
            // bytes.length=" + bytes.length);
            return new JarInputStream(new ByteArrayInputStream(bytes));
          }
          if (_url != null)
          {
            // System.out.println("Jalview2XML: opening url jarInputStream for "
            // + _url);
            return new JarInputStream(_url.openStream());
          }
          else
          {
            // System.out.println("Jalview2XML: opening file jarInputStream for
            // " + file);
            return new JarInputStream(new FileInputStream(file));
          }
        }

        @Override
        public String getFilename()
        {
          return file;
        }
      };
    } catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Recover jalview session from a jalview project archive. Caller may
   * initialise uniqueSetSuffix, seqRefIds, viewportsAdded and frefedSequence
   * themselves. Any null fields will be initialised with default values,
   * non-null fields are left alone.
   * 
   * @param jprovider
   * @return
   */
  public AlignFrame loadJalviewAlign(final jarInputStreamProvider jprovider)
  {
    errorMessage = null;
    if (uniqueSetSuffix == null)
    {
      uniqueSetSuffix = System.currentTimeMillis() % 100000 + "";
    }
    if (seqRefIds == null)
    {
      initSeqRefs();
    }
    AlignFrame af = null, _af = null;
    IdentityHashMap<AlignmentI, AlignmentI> importedDatasets = new IdentityHashMap<>();
    Map<String, AlignFrame> gatherToThisFrame = new HashMap<>();
    final String file = jprovider.getFilename();
    try
    {
      JarInputStream jin = null;
      JarEntry jarentry = null;
      int entryCount = 1;

      do
      {
        jin = jprovider.getJarInputStream();
        for (int i = 0; i < entryCount; i++)
        {
          jarentry = jin.getNextJarEntry();
        }

        if (jarentry != null && jarentry.getName().endsWith(".xml"))
        {
          JAXBContext jc = JAXBContext
                  .newInstance("jalview.xml.binding.jalview");
          XMLStreamReader streamReader = XMLInputFactory.newInstance()
                  .createXMLStreamReader(jin);
          javax.xml.bind.Unmarshaller um = jc.createUnmarshaller();
          JAXBElement<JalviewModel> jbe = um.unmarshal(streamReader,
                  JalviewModel.class);
          JalviewModel object = jbe.getValue();

          if (true) // !skipViewport(object))
          {
            _af = loadFromObject(object, file, true, jprovider);
            if (_af != null && object.getViewport().size() > 0)
            // getJalviewModelSequence().getViewportCount() > 0)
            {
              if (af == null)
              {
                // store a reference to the first view
                af = _af;
              }
              if (_af.getViewport().isGatherViewsHere())
              {
                // if this is a gathered view, keep its reference since
                // after gathering views, only this frame will remain
                af = _af;
                gatherToThisFrame.put(_af.getViewport().getSequenceSetId(),
                        _af);
              }
              // Save dataset to register mappings once all resolved
              importedDatasets.put(
                      af.getViewport().getAlignment().getDataset(),
                      af.getViewport().getAlignment().getDataset());
            }
          }
          entryCount++;
        }
        else if (jarentry != null)
        {
          // Some other file here.
          entryCount++;
        }
      } while (jarentry != null);
      jin.close();
      resolveFrefedSequences();
    } catch (IOException ex)
    {
      ex.printStackTrace();
      errorMessage = "Couldn't locate Jalview XML file : " + file;
      System.err.println(
              "Exception whilst loading jalview XML file : " + ex + "\n");
    } catch (Exception ex)
    {
      System.err.println("Parsing as Jalview Version 2 file failed.");
      ex.printStackTrace(System.err);
      if (attemptversion1parse)
      {
        // used to attempt to parse as V1 castor-generated xml
      }
      if (Desktop.instance != null)
      {
        Desktop.instance.stopLoading();
      }
      if (af != null)
      {
        System.out.println("Successfully loaded archive file");
        return af;
      }
      ex.printStackTrace();

      System.err.println(
              "Exception whilst loading jalview XML file : " + ex + "\n");
    } catch (OutOfMemoryError e)
    {
      // Don't use the OOM Window here
      errorMessage = "Out of memory loading jalview XML file";
      System.err.println("Out of memory whilst loading jalview XML file");
      e.printStackTrace();
    }

    /*
     * Regather multiple views (with the same sequence set id) to the frame (if
     * any) that is flagged as the one to gather to, i.e. convert them to tabbed
     * views instead of separate frames. Note this doesn't restore a state where
     * some expanded views in turn have tabbed views - the last "first tab" read
     * in will play the role of gatherer for all.
     */
    for (AlignFrame fr : gatherToThisFrame.values())
    {
      Desktop.instance.gatherViews(fr);
    }

    restoreSplitFrames();
    for (AlignmentI ds : importedDatasets.keySet())
    {
      if (ds.getCodonFrames() != null)
      {
        StructureSelectionManager
                .getStructureSelectionManager(Desktop.instance)
                .registerMappings(ds.getCodonFrames());
      }
    }
    if (errorMessage != null)
    {
      reportErrors();
    }

    if (Desktop.instance != null)
    {
      Desktop.instance.stopLoading();
    }

    return af;
  }

  /**
   * Try to reconstruct and display SplitFrame windows, where each contains
   * complementary dna and protein alignments. Done by pairing up AlignFrame
   * objects (created earlier) which have complementary viewport ids associated.
   */
  protected void restoreSplitFrames()
  {
    List<SplitFrame> gatherTo = new ArrayList<>();
    List<AlignFrame> addedToSplitFrames = new ArrayList<>();
    Map<String, AlignFrame> dna = new HashMap<>();

    /*
     * Identify the DNA alignments
     */
    for (Entry<Viewport, AlignFrame> candidate : splitFrameCandidates
            .entrySet())
    {
      AlignFrame af = candidate.getValue();
      if (af.getViewport().getAlignment().isNucleotide())
      {
        dna.put(candidate.getKey().getId(), af);
      }
    }

    /*
     * Try to match up the protein complements
     */
    for (Entry<Viewport, AlignFrame> candidate : splitFrameCandidates
            .entrySet())
    {
      AlignFrame af = candidate.getValue();
      if (!af.getViewport().getAlignment().isNucleotide())
      {
        String complementId = candidate.getKey().getComplementId();
        // only non-null complements should be in the Map
        if (complementId != null && dna.containsKey(complementId))
        {
          final AlignFrame dnaFrame = dna.get(complementId);
          SplitFrame sf = createSplitFrame(dnaFrame, af);
          addedToSplitFrames.add(dnaFrame);
          addedToSplitFrames.add(af);
          dnaFrame.setMenusForViewport();
          af.setMenusForViewport();
          if (af.getViewport().isGatherViewsHere())
          {
            gatherTo.add(sf);
          }
        }
      }
    }

    /*
     * Open any that we failed to pair up (which shouldn't happen!) as
     * standalone AlignFrame's.
     */
    for (Entry<Viewport, AlignFrame> candidate : splitFrameCandidates
            .entrySet())
    {
      AlignFrame af = candidate.getValue();
      if (!addedToSplitFrames.contains(af))
      {
        Viewport view = candidate.getKey();
        Desktop.addInternalFrame(af, view.getTitle(),
                safeInt(view.getWidth()), safeInt(view.getHeight()));
        af.setMenusForViewport();
        System.err.println("Failed to restore view " + view.getTitle()
                + " to split frame");
      }
    }

    /*
     * Gather back into tabbed views as flagged.
     */
    for (SplitFrame sf : gatherTo)
    {
      Desktop.instance.gatherViews(sf);
    }

    splitFrameCandidates.clear();
  }

  /**
   * Construct and display one SplitFrame holding DNA and protein alignments.
   * 
   * @param dnaFrame
   * @param proteinFrame
   * @return
   */
  protected SplitFrame createSplitFrame(AlignFrame dnaFrame,
          AlignFrame proteinFrame)
  {
    SplitFrame splitFrame = new SplitFrame(dnaFrame, proteinFrame);
    String title = MessageManager.getString("label.linked_view_title");
    int width = (int) dnaFrame.getBounds().getWidth();
    int height = (int) (dnaFrame.getBounds().getHeight()
            + proteinFrame.getBounds().getHeight() + 50);

    /*
     * SplitFrame location is saved to both enclosed frames
     */
    splitFrame.setLocation(dnaFrame.getX(), dnaFrame.getY());
    Desktop.addInternalFrame(splitFrame, title, width, height);

    /*
     * And compute cDNA consensus (couldn't do earlier with consensus as
     * mappings were not yet present)
     */
    proteinFrame.getViewport().alignmentChanged(proteinFrame.alignPanel);

    return splitFrame;
  }

  /**
   * check errorMessage for a valid error message and raise an error box in the
   * GUI or write the current errorMessage to stderr and then clear the error
   * state.
   */
  protected void reportErrors()
  {
    reportErrors(false);
  }

  protected void reportErrors(final boolean saving)
  {
    if (errorMessage != null)
    {
      final String finalErrorMessage = errorMessage;
      if (raiseGUI)
      {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                    finalErrorMessage,
                    "Error " + (saving ? "saving" : "loading")
                            + " Jalview file",
                    JvOptionPane.WARNING_MESSAGE);
          }
        });
      }
      else
      {
        System.err.println("Problem loading Jalview file: " + errorMessage);
      }
    }
    errorMessage = null;
  }

  Map<String, String> alreadyLoadedPDB = new HashMap<>();

  /**
   * when set, local views will be updated from view stored in JalviewXML
   * Currently (28th Sep 2008) things will go horribly wrong in vamsas document
   * sync if this is set to true.
   */
  private final boolean updateLocalViews = false;

  /**
   * Returns the path to a temporary file holding the PDB file for the given PDB
   * id. The first time of asking, searches for a file of that name in the
   * Jalview project jar, and copies it to a new temporary file. Any repeat
   * requests just return the path to the file previously created.
   * 
   * @param jprovider
   * @param pdbId
   * @return
   */
  String loadPDBFile(jarInputStreamProvider jprovider, String pdbId,
          String origFile)
  {
    if (alreadyLoadedPDB.containsKey(pdbId))
    {
      return alreadyLoadedPDB.get(pdbId).toString();
    }

    String tempFile = copyJarEntry(jprovider, pdbId, "jalview_pdb",
            origFile);
    if (tempFile != null)
    {
      alreadyLoadedPDB.put(pdbId, tempFile);
    }
    return tempFile;
  }

  /**
   * Copies the jar entry of given name to a new temporary file and returns the
   * path to the file, or null if the entry is not found.
   * 
   * @param jprovider
   * @param jarEntryName
   * @param prefix
   *          a prefix for the temporary file name, must be at least three
   *          characters long
   * @param suffixModel
   *          null or original file - so new file can be given the same suffix
   *          as the old one
   * @return
   */
  protected String copyJarEntry(jarInputStreamProvider jprovider,
          String jarEntryName, String prefix, String suffixModel)
  {
    String suffix = ".tmp";
    if (suffixModel == null)
    {
      suffixModel = jarEntryName;
    }
    int sfpos = suffixModel.lastIndexOf(".");
    if (sfpos > -1 && sfpos < (suffixModel.length() - 1))
    {
      suffix = "." + suffixModel.substring(sfpos + 1);
    }

    try (JarInputStream jin = jprovider.getJarInputStream())
    {
      JarEntry entry = null;
      do
      {
        entry = jin.getNextJarEntry();
      } while (entry != null && !entry.getName().equals(jarEntryName));

      if (entry != null)
      {
        // in = new BufferedReader(new InputStreamReader(jin, UTF_8));
        File outFile = File.createTempFile(prefix, suffix);
        outFile.deleteOnExit();
        try (OutputStream os = new FileOutputStream(outFile))
        {
          copyAll(jin, os);
        }
        String t = outFile.getAbsolutePath();
        return t;
      }
      else
      {
        Console.warn(
                "Couldn't find entry in Jalview Jar for " + jarEntryName);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return null;
  }

  private class JvAnnotRow
  {
    public JvAnnotRow(int i, AlignmentAnnotation jaa)
    {
      order = i;
      template = jaa;
    }

    /**
     * persisted version of annotation row from which to take vis properties
     */
    public jalview.datamodel.AlignmentAnnotation template;

    /**
     * original position of the annotation row in the alignment
     */
    public int order;
  }

  /**
   * Load alignment frame from jalview XML DOM object
   * 
   * @param jalviewModel
   *          DOM
   * @param file
   *          filename source string
   * @param loadTreesAndStructures
   *          when false only create Viewport
   * @param jprovider
   *          data source provider
   * @return alignment frame created from view stored in DOM
   */
  AlignFrame loadFromObject(JalviewModel jalviewModel, String file,
          boolean loadTreesAndStructures, jarInputStreamProvider jprovider)
  {
    SequenceSet vamsasSet = jalviewModel.getVamsasModel().getSequenceSet()
            .get(0);
    List<Sequence> vamsasSeqs = vamsasSet.getSequence();

    // JalviewModelSequence jms = object.getJalviewModelSequence();

    // Viewport view = (jms.getViewportCount() > 0) ? jms.getViewport(0)
    // : null;
    Viewport view = (jalviewModel.getViewport().size() > 0)
            ? jalviewModel.getViewport().get(0)
            : null;

    // ////////////////////////////////
    // INITIALISE ALIGNMENT SEQUENCESETID AND VIEWID
    //
    //
    // If we just load in the same jar file again, the sequenceSetId
    // will be the same, and we end up with multiple references
    // to the same sequenceSet. We must modify this id on load
    // so that each load of the file gives a unique id

    /**
     * used to resolve correct alignment dataset for alignments with multiple
     * views
     */
    String uniqueSeqSetId = null;
    String viewId = null;
    if (view != null)
    {
      uniqueSeqSetId = view.getSequenceSetId() + uniqueSetSuffix;
      viewId = (view.getId() == null ? null
              : view.getId() + uniqueSetSuffix);
    }

    // ////////////////////////////////
    // LOAD SEQUENCES

    List<SequenceI> hiddenSeqs = null;

    List<SequenceI> tmpseqs = new ArrayList<>();

    boolean multipleView = false;
    SequenceI referenceseqForView = null;
    // JSeq[] jseqs = object.getJalviewModelSequence().getJSeq();
    List<JSeq> jseqs = jalviewModel.getJSeq();
    int vi = 0; // counter in vamsasSeq array
    for (int i = 0; i < jseqs.size(); i++)
    {
      JSeq jseq = jseqs.get(i);
      String seqId = jseq.getId();

      SequenceI tmpSeq = seqRefIds.get(seqId);
      if (tmpSeq != null)
      {
        if (!incompleteSeqs.containsKey(seqId))
        {
          // may not need this check, but keep it for at least 2.9,1 release
          if (tmpSeq.getStart() != jseq.getStart()
                  || tmpSeq.getEnd() != jseq.getEnd())
          {
            System.err.println(String.format(
                    "Warning JAL-2154 regression: updating start/end for sequence %s from %d/%d to %d/%d",
                    tmpSeq.getName(), tmpSeq.getStart(), tmpSeq.getEnd(),
                    jseq.getStart(), jseq.getEnd()));
          }
        }
        else
        {
          incompleteSeqs.remove(seqId);
        }
        if (vamsasSeqs.size() > vi
                && vamsasSeqs.get(vi).getId().equals(seqId))
        {
          // most likely we are reading a dataset XML document so
          // update from vamsasSeq section of XML for this sequence
          tmpSeq.setName(vamsasSeqs.get(vi).getName());
          tmpSeq.setDescription(vamsasSeqs.get(vi).getDescription());
          tmpSeq.setSequence(vamsasSeqs.get(vi).getSequence());
          vi++;
        }
        else
        {
          // reading multiple views, so vamsasSeq set is a subset of JSeq
          multipleView = true;
        }
        tmpSeq.setStart(jseq.getStart());
        tmpSeq.setEnd(jseq.getEnd());
        tmpseqs.add(tmpSeq);
      }
      else
      {
        Sequence vamsasSeq = vamsasSeqs.get(vi);
        tmpSeq = new jalview.datamodel.Sequence(vamsasSeq.getName(),
                vamsasSeq.getSequence());
        tmpSeq.setDescription(vamsasSeq.getDescription());
        tmpSeq.setStart(jseq.getStart());
        tmpSeq.setEnd(jseq.getEnd());
        tmpSeq.setVamsasId(uniqueSetSuffix + seqId);
        seqRefIds.put(vamsasSeq.getId(), tmpSeq);
        tmpseqs.add(tmpSeq);
        vi++;
      }

      if (safeBoolean(jseq.isViewreference()))
      {
        referenceseqForView = tmpseqs.get(tmpseqs.size() - 1);
      }

      if (jseq.isHidden() != null && jseq.isHidden().booleanValue())
      {
        if (hiddenSeqs == null)
        {
          hiddenSeqs = new ArrayList<>();
        }

        hiddenSeqs.add(tmpSeq);
      }
    }

    // /
    // Create the alignment object from the sequence set
    // ///////////////////////////////
    SequenceI[] orderedSeqs = tmpseqs
            .toArray(new SequenceI[tmpseqs.size()]);

    AlignmentI al = null;
    // so we must create or recover the dataset alignment before going further
    // ///////////////////////////////
    if (vamsasSet.getDatasetId() == null || vamsasSet.getDatasetId() == "")
    {
      // older jalview projects do not have a dataset - so creat alignment and
      // dataset
      al = new Alignment(orderedSeqs);
      al.setDataset(null);
    }
    else
    {
      boolean isdsal = jalviewModel.getViewport().isEmpty();
      if (isdsal)
      {
        // we are importing a dataset record, so
        // recover reference to an alignment already materialsed as dataset
        al = getDatasetFor(vamsasSet.getDatasetId());
      }
      if (al == null)
      {
        // materialse the alignment
        al = new Alignment(orderedSeqs);
      }
      if (isdsal)
      {
        addDatasetRef(vamsasSet.getDatasetId(), al);
      }

      // finally, verify all data in vamsasSet is actually present in al
      // passing on flag indicating if it is actually a stored dataset
      recoverDatasetFor(vamsasSet, al, isdsal, uniqueSeqSetId);
    }

    if (referenceseqForView != null)
    {
      al.setSeqrep(referenceseqForView);
    }
    // / Add the alignment properties
    for (int i = 0; i < vamsasSet.getSequenceSetProperties().size(); i++)
    {
      SequenceSetProperties ssp = vamsasSet.getSequenceSetProperties()
              .get(i);
      al.setProperty(ssp.getKey(), ssp.getValue());
    }

    // ///////////////////////////////

    Hashtable pdbloaded = new Hashtable(); // TODO nothing writes to this??
    if (!multipleView)
    {
      // load sequence features, database references and any associated PDB
      // structures for the alignment
      //
      // prior to 2.10, this part would only be executed the first time a
      // sequence was encountered, but not afterwards.
      // now, for 2.10 projects, this is also done if the xml doc includes
      // dataset sequences not actually present in any particular view.
      //
      for (int i = 0; i < vamsasSeqs.size(); i++)
      {
        JSeq jseq = jseqs.get(i);
        if (jseq.getFeatures().size() > 0)
        {
          List<Feature> features = jseq.getFeatures();
          for (int f = 0; f < features.size(); f++)
          {
            Feature feat = features.get(f);
            SequenceFeature sf = new SequenceFeature(feat.getType(),
                    feat.getDescription(), feat.getBegin(), feat.getEnd(),
                    safeFloat(feat.getScore()), feat.getFeatureGroup());
            sf.setStatus(feat.getStatus());

            /*
             * load any feature attributes - include map-valued attributes
             */
            Map<String, Map<String, String>> mapAttributes = new HashMap<>();
            for (int od = 0; od < feat.getOtherData().size(); od++)
            {
              OtherData keyValue = feat.getOtherData().get(od);
              String attributeName = keyValue.getKey();
              String attributeValue = keyValue.getValue();
              if (attributeName.startsWith("LINK"))
              {
                sf.addLink(attributeValue);
              }
              else
              {
                String subAttribute = keyValue.getKey2();
                if (subAttribute == null)
                {
                  // simple string-valued attribute
                  sf.setValue(attributeName, attributeValue);
                }
                else
                {
                  // attribute 'key' has sub-attribute 'key2'
                  if (!mapAttributes.containsKey(attributeName))
                  {
                    mapAttributes.put(attributeName, new HashMap<>());
                  }
                  mapAttributes.get(attributeName).put(subAttribute,
                          attributeValue);
                }
              }
            }
            for (Entry<String, Map<String, String>> mapAttribute : mapAttributes
                    .entrySet())
            {
              sf.setValue(mapAttribute.getKey(), mapAttribute.getValue());
            }

            // adds feature to datasequence's feature set (since Jalview 2.10)
            al.getSequenceAt(i).addSequenceFeature(sf);
          }
        }
        if (vamsasSeqs.get(i).getDBRef().size() > 0)
        {
          // adds dbrefs to datasequence's set (since Jalview 2.10)
          addDBRefs(
                  al.getSequenceAt(i).getDatasetSequence() == null
                          ? al.getSequenceAt(i)
                          : al.getSequenceAt(i).getDatasetSequence(),
                  vamsasSeqs.get(i));
        }
        if (jseq.getPdbids().size() > 0)
        {
          List<Pdbids> ids = jseq.getPdbids();
          for (int p = 0; p < ids.size(); p++)
          {
            Pdbids pdbid = ids.get(p);
            jalview.datamodel.PDBEntry entry = new jalview.datamodel.PDBEntry();
            entry.setId(pdbid.getId());
            if (pdbid.getType() != null)
            {
              if (PDBEntry.Type.getType(pdbid.getType()) != null)
              {
                entry.setType(PDBEntry.Type.getType(pdbid.getType()));
              }
              else
              {
                entry.setType(PDBEntry.Type.FILE);
              }
            }
            // jprovider is null when executing 'New View'
            if (pdbid.getFile() != null && jprovider != null)
            {
              if (!pdbloaded.containsKey(pdbid.getFile()))
              {
                entry.setFile(loadPDBFile(jprovider, pdbid.getId(),
                        pdbid.getFile()));
              }
              else
              {
                entry.setFile(pdbloaded.get(pdbid.getId()).toString());
              }
            }
            /*
            if (pdbid.getPdbentryItem() != null)
            {
              for (PdbentryItem item : pdbid.getPdbentryItem())
              {
                for (Property pr : item.getProperty())
                {
                  entry.setProperty(pr.getName(), pr.getValue());
                }
              }
            }
            */
            for (Property prop : pdbid.getProperty())
            {
              entry.setProperty(prop.getName(), prop.getValue());
            }
            StructureSelectionManager
                    .getStructureSelectionManager(Desktop.instance)
                    .registerPDBEntry(entry);
            // adds PDBEntry to datasequence's set (since Jalview 2.10)
            if (al.getSequenceAt(i).getDatasetSequence() != null)
            {
              al.getSequenceAt(i).getDatasetSequence().addPDBId(entry);
            }
            else
            {
              al.getSequenceAt(i).addPDBId(entry);
            }
          }
        }
      }
    } // end !multipleview

    // ///////////////////////////////
    // LOAD SEQUENCE MAPPINGS

    if (vamsasSet.getAlcodonFrame().size() > 0)
    {
      // TODO Potentially this should only be done once for all views of an
      // alignment
      List<AlcodonFrame> alc = vamsasSet.getAlcodonFrame();
      for (int i = 0; i < alc.size(); i++)
      {
        AlignedCodonFrame cf = new AlignedCodonFrame();
        if (alc.get(i).getAlcodMap().size() > 0)
        {
          List<AlcodMap> maps = alc.get(i).getAlcodMap();
          for (int m = 0; m < maps.size(); m++)
          {
            AlcodMap map = maps.get(m);
            SequenceI dnaseq = seqRefIds.get(map.getDnasq());
            // Load Mapping
            jalview.datamodel.Mapping mapping = null;
            // attach to dna sequence reference.
            if (map.getMapping() != null)
            {
              mapping = addMapping(map.getMapping());
              if (dnaseq != null && mapping.getTo() != null)
              {
                cf.addMap(dnaseq, mapping.getTo(), mapping.getMap());
              }
              else
              {
                // defer to later
                frefedSequence
                        .add(newAlcodMapRef(map.getDnasq(), cf, mapping));
              }
            }
          }
          al.addCodonFrame(cf);
        }
      }
    }

    // ////////////////////////////////
    // LOAD ANNOTATIONS
    List<JvAnnotRow> autoAlan = new ArrayList<>();

    /*
     * store any annotations which forward reference a group's ID
     */
    Map<String, List<AlignmentAnnotation>> groupAnnotRefs = new Hashtable<>();

    if (vamsasSet.getAnnotation().size()/*Count()*/ > 0)
    {
      List<Annotation> an = vamsasSet.getAnnotation();

      for (int i = 0; i < an.size(); i++)
      {
        Annotation annotation = an.get(i);

        /**
         * test if annotation is automatically calculated for this view only
         */
        boolean autoForView = false;
        if (annotation.getLabel().equals("Quality")
                || annotation.getLabel().equals("Conservation")
                || annotation.getLabel().equals("Consensus"))
        {
          // Kludge for pre 2.5 projects which lacked the autocalculated flag
          autoForView = true;
          // JAXB has no has() test; schema defaults value to false
          // if (!annotation.hasAutoCalculated())
          // {
          // annotation.setAutoCalculated(true);
          // }
        }
        if (autoForView || annotation.isAutoCalculated())
        {
          // remove ID - we don't recover annotation from other views for
          // view-specific annotation
          annotation.setId(null);
        }

        // set visibility for other annotation in this view
        String annotationId = annotation.getId();
        if (annotationId != null && annotationIds.containsKey(annotationId))
        {
          AlignmentAnnotation jda = annotationIds.get(annotationId);
          // in principle Visible should always be true for annotation displayed
          // in multiple views
          if (annotation.isVisible() != null)
          {
            jda.visible = annotation.isVisible();
          }

          al.addAnnotation(jda);

          continue;
        }
        // Construct new annotation from model.
        List<AnnotationElement> ae = annotation.getAnnotationElement();
        jalview.datamodel.Annotation[] anot = null;
        java.awt.Color firstColour = null;
        int anpos;
        if (!annotation.isScoreOnly())
        {
          anot = new jalview.datamodel.Annotation[al.getWidth()];
          for (int aa = 0; aa < ae.size() && aa < anot.length; aa++)
          {
            AnnotationElement annElement = ae.get(aa);
            anpos = annElement.getPosition();

            if (anpos >= anot.length)
            {
              continue;
            }

            float value = safeFloat(annElement.getValue());
            anot[anpos] = new jalview.datamodel.Annotation(
                    annElement.getDisplayCharacter(),
                    annElement.getDescription(),
                    (annElement.getSecondaryStructure() == null
                            || annElement.getSecondaryStructure()
                                    .length() == 0)
                                            ? ' '
                                            : annElement
                                                    .getSecondaryStructure()
                                                    .charAt(0),
                    value);
            anot[anpos].colour = new Color(safeInt(annElement.getColour()));
            if (firstColour == null)
            {
              firstColour = anot[anpos].colour;
            }
          }
        }
        jalview.datamodel.AlignmentAnnotation jaa = null;

        if (annotation.isGraph())
        {
          float llim = 0, hlim = 0;
          // if (autoForView || an[i].isAutoCalculated()) {
          // hlim=11f;
          // }
          jaa = new jalview.datamodel.AlignmentAnnotation(
                  annotation.getLabel(), annotation.getDescription(), anot,
                  llim, hlim, safeInt(annotation.getGraphType()));

          jaa.graphGroup = safeInt(annotation.getGraphGroup());
          jaa._linecolour = firstColour;
          if (annotation.getThresholdLine() != null)
          {
            jaa.setThreshold(new jalview.datamodel.GraphLine(
                    safeFloat(annotation.getThresholdLine().getValue()),
                    annotation.getThresholdLine().getLabel(),
                    new java.awt.Color(safeInt(
                            annotation.getThresholdLine().getColour()))));
          }
          if (autoForView || annotation.isAutoCalculated())
          {
            // Hardwire the symbol display line to ensure that labels for
            // histograms are displayed
            jaa.hasText = true;
          }
        }
        else
        {
          jaa = new jalview.datamodel.AlignmentAnnotation(
                  annotation.getLabel(), annotation.getDescription(), anot);
          jaa._linecolour = firstColour;
        }
        // register new annotation
        if (annotation.getId() != null)
        {
          annotationIds.put(annotation.getId(), jaa);
          jaa.annotationId = annotation.getId();
        }
        // recover sequence association
        String sequenceRef = annotation.getSequenceRef();
        if (sequenceRef != null)
        {
          // from 2.9 sequenceRef is to sequence id (JAL-1781)
          SequenceI sequence = seqRefIds.get(sequenceRef);
          if (sequence == null)
          {
            // in pre-2.9 projects sequence ref is to sequence name
            sequence = al.findName(sequenceRef);
          }
          if (sequence != null)
          {
            jaa.createSequenceMapping(sequence, 1, true);
            sequence.addAlignmentAnnotation(jaa);
          }
        }
        // and make a note of any group association
        if (annotation.getGroupRef() != null
                && annotation.getGroupRef().length() > 0)
        {
          List<jalview.datamodel.AlignmentAnnotation> aal = groupAnnotRefs
                  .get(annotation.getGroupRef());
          if (aal == null)
          {
            aal = new ArrayList<>();
            groupAnnotRefs.put(annotation.getGroupRef(), aal);
          }
          aal.add(jaa);
        }

        if (annotation.getScore() != null)
        {
          jaa.setScore(annotation.getScore().doubleValue());
        }
        if (annotation.isVisible() != null)
        {
          jaa.visible = annotation.isVisible().booleanValue();
        }

        if (annotation.isCentreColLabels() != null)
        {
          jaa.centreColLabels = annotation.isCentreColLabels()
                  .booleanValue();
        }

        if (annotation.isScaleColLabels() != null)
        {
          jaa.scaleColLabel = annotation.isScaleColLabels().booleanValue();
        }
        if (annotation.isAutoCalculated())
        {
          // newer files have an 'autoCalculated' flag and store calculation
          // state in viewport properties
          jaa.autoCalculated = true; // means annotation will be marked for
          // update at end of load.
        }
        if (annotation.getGraphHeight() != null)
        {
          jaa.graphHeight = annotation.getGraphHeight().intValue();
        }
        jaa.belowAlignment = annotation.isBelowAlignment();
        jaa.setCalcId(annotation.getCalcId());
        if (annotation.getProperty().size() > 0)
        {
          for (jalview.xml.binding.jalview.Property prop : annotation
                  .getProperty())
          {
            jaa.setProperty(prop.getName(), prop.getValue());
          }
        }
        if (jaa.graph == AlignmentAnnotation.CONTACT_MAP)
        {
          if (annotation.getContactmatrix() != null
                  && annotation.getContactmatrix().size() > 0)
          {
            for (MatrixType xmlmat : annotation.getContactmatrix())
            {
              if (PAEContactMatrix.PAEMATRIX.equals(xmlmat.getType()))
              {
                if (!xmlmat.getRows().equals(xmlmat.getCols()))
                {
                  Console.error("Can't handle non square PAE Matrices");
                }
                else
                {
                  float[][] elements = ContactMatrix
                          .fromFloatStringToContacts(xmlmat.getElements(),
                                  xmlmat.getCols().intValue(),
                                  xmlmat.getRows().intValue());
                  jalview.util.MapList mapping = null;
                  if (xmlmat.getMapping() != null)
                  {
                    MapListType m = xmlmat.getMapping();
                    // Mapping m = dr.getMapping();
                    int fr[] = new int[m.getMapListFrom().size() * 2];
                    Iterator<MapListFrom> from = m.getMapListFrom()
                            .iterator();// enumerateMapListFrom();
                    for (int _i = 0; from.hasNext(); _i += 2)
                    {
                      MapListFrom mf = from.next();
                      fr[_i] = mf.getStart();
                      fr[_i + 1] = mf.getEnd();
                    }
                    int fto[] = new int[m.getMapListTo().size() * 2];
                    Iterator<MapListTo> to = m.getMapListTo().iterator();// enumerateMapListTo();
                    for (int _i = 0; to.hasNext(); _i += 2)
                    {
                      MapListTo mf = to.next();
                      fto[_i] = mf.getStart();
                      fto[_i + 1] = mf.getEnd();
                    }

                    mapping = new jalview.util.MapList(fr, fto,
                            m.getMapFromUnit().intValue(),
                            m.getMapToUnit().intValue());
                  }
                  List<BitSet> newgroups = new ArrayList<BitSet>();
                  if (xmlmat.getGroups().size() > 0)
                  {
                    for (String sgroup : xmlmat.getGroups())
                    {
                      newgroups.add(deStringifyBitset(sgroup));
                    }
                  }
                  String nwk = xmlmat.getNewick().size() > 0
                          ? xmlmat.getNewick().get(0)
                          : null;
                  if (xmlmat.getNewick().size() > 1)
                  {
                    Console.log.info(
                            "Ignoring additional clusterings for contact matrix");
                  }
                  String treeMethod = xmlmat.getTreeMethod();
                  double thresh = xmlmat.getCutHeight() != null
                          ? xmlmat.getCutHeight()
                          : 0;
                  GroupSet grpset = new GroupSet();
                  grpset.restoreGroups(newgroups, treeMethod, nwk, thresh);
                  PAEContactMatrix newpae = new PAEContactMatrix(
                          jaa.sequenceRef, mapping, elements, grpset);
                  jaa.sequenceRef.addContactListFor(jaa, newpae);
                }
              }
              else
              {
                Console.error("Ignoring CONTACT_MAP annotation with type "
                        + xmlmat.getType());
              }
            }
          }
        }

        if (jaa.autoCalculated)
        {
          autoAlan.add(new JvAnnotRow(i, jaa));
        }
        else
        // if (!autoForView)
        {
          // add autocalculated group annotation and any user created annotation
          // for the view
          al.addAnnotation(jaa);
        }
      }
    }
    // ///////////////////////
    // LOAD GROUPS
    // Create alignment markup and styles for this view
    if (jalviewModel.getJGroup().size() > 0)
    {
      List<JGroup> groups = jalviewModel.getJGroup();
      boolean addAnnotSchemeGroup = false;
      for (int i = 0; i < groups.size(); i++)
      {
        JGroup jGroup = groups.get(i);
        ColourSchemeI cs = null;
        if (jGroup.getColour() != null)
        {
          if (jGroup.getColour().startsWith("ucs"))
          {
            cs = getUserColourScheme(jalviewModel, jGroup.getColour());
          }
          else if (jGroup.getColour().equals("AnnotationColourGradient")
                  && jGroup.getAnnotationColours() != null)
          {
            addAnnotSchemeGroup = true;
          }
          else
          {
            cs = ColourSchemeProperty.getColourScheme(null, al,
                    jGroup.getColour());
          }
        }
        int pidThreshold = safeInt(jGroup.getPidThreshold());

        Vector<SequenceI> seqs = new Vector<>();

        for (int s = 0; s < jGroup.getSeq().size(); s++)
        {
          String seqId = jGroup.getSeq().get(s);
          SequenceI ts = seqRefIds.get(seqId);

          if (ts != null)
          {
            seqs.addElement(ts);
          }
        }

        if (seqs.size() < 1)
        {
          continue;
        }

        SequenceGroup sg = new SequenceGroup(seqs, jGroup.getName(), cs,
                safeBoolean(jGroup.isDisplayBoxes()),
                safeBoolean(jGroup.isDisplayText()),
                safeBoolean(jGroup.isColourText()),
                safeInt(jGroup.getStart()), safeInt(jGroup.getEnd()));
        sg.getGroupColourScheme().setThreshold(pidThreshold, true);
        sg.getGroupColourScheme()
                .setConservationInc(safeInt(jGroup.getConsThreshold()));
        sg.setOutlineColour(new Color(safeInt(jGroup.getOutlineColour())));

        sg.textColour = new Color(safeInt(jGroup.getTextCol1()));
        sg.textColour2 = new Color(safeInt(jGroup.getTextCol2()));
        sg.setShowNonconserved(safeBoolean(jGroup.isShowUnconserved()));
        sg.thresholdTextColour = safeInt(jGroup.getTextColThreshold());
        // attributes with a default in the schema are never null
        sg.setShowConsensusHistogram(jGroup.isShowConsensusHistogram());
        sg.setshowSequenceLogo(jGroup.isShowSequenceLogo());
        sg.setNormaliseSequenceLogo(jGroup.isNormaliseSequenceLogo());
        sg.setIgnoreGapsConsensus(jGroup.isIgnoreGapsinConsensus());
        if (jGroup.getConsThreshold() != null
                && jGroup.getConsThreshold().intValue() != 0)
        {
          Conservation c = new Conservation("All", sg.getSequences(null), 0,
                  sg.getWidth() - 1);
          c.calculate();
          c.verdict(false, 25);
          sg.cs.setConservation(c);
        }

        if (jGroup.getId() != null && groupAnnotRefs.size() > 0)
        {
          // re-instate unique group/annotation row reference
          List<AlignmentAnnotation> jaal = groupAnnotRefs
                  .get(jGroup.getId());
          if (jaal != null)
          {
            for (AlignmentAnnotation jaa : jaal)
            {
              jaa.groupRef = sg;
              if (jaa.autoCalculated)
              {
                // match up and try to set group autocalc alignment row for this
                // annotation
                if (jaa.label.startsWith("Consensus for "))
                {
                  sg.setConsensus(jaa);
                }
                // match up and try to set group autocalc alignment row for this
                // annotation
                if (jaa.label.startsWith("Conservation for "))
                {
                  sg.setConservationRow(jaa);
                }
              }
            }
          }
        }
        al.addGroup(sg);
        if (addAnnotSchemeGroup)
        {
          // reconstruct the annotation colourscheme
          sg.setColourScheme(
                  constructAnnotationColour(jGroup.getAnnotationColours(),
                          null, al, jalviewModel, false));
        }
      }
    }
    if (view == null)
    {
      // only dataset in this model, so just return.
      return null;
    }
    // ///////////////////////////////
    // LOAD VIEWPORT

    AlignFrame af = null;
    AlignViewport av = null;
    // now check to see if we really need to create a new viewport.
    if (multipleView && viewportsAdded.size() == 0)
    {
      // We recovered an alignment for which a viewport already exists.
      // TODO: fix up any settings necessary for overlaying stored state onto
      // state recovered from another document. (may not be necessary).
      // we may need a binding from a viewport in memory to one recovered from
      // XML.
      // and then recover its containing af to allow the settings to be applied.
      // TODO: fix for vamsas demo
      System.err.println(
              "About to recover a viewport for existing alignment: Sequence set ID is "
                      + uniqueSeqSetId);
      Object seqsetobj = retrieveExistingObj(uniqueSeqSetId);
      if (seqsetobj != null)
      {
        if (seqsetobj instanceof String)
        {
          uniqueSeqSetId = (String) seqsetobj;
          System.err.println(
                  "Recovered extant sequence set ID mapping for ID : New Sequence set ID is "
                          + uniqueSeqSetId);
        }
        else
        {
          System.err.println(
                  "Warning : Collision between sequence set ID string and existing jalview object mapping.");
        }

      }
    }
    /**
     * indicate that annotation colours are applied across all groups (pre
     * Jalview 2.8.1 behaviour)
     */
    boolean doGroupAnnColour = Jalview2XML.isVersionStringLaterThan("2.8.1",
            jalviewModel.getVersion());

    AlignmentPanel ap = null;
    boolean isnewview = true;
    if (viewId != null)
    {
      // Check to see if this alignment already has a view id == viewId
      jalview.gui.AlignmentPanel views[] = Desktop
              .getAlignmentPanels(uniqueSeqSetId);
      if (views != null && views.length > 0)
      {
        for (int v = 0; v < views.length; v++)
        {
          if (views[v].av.getViewId().equalsIgnoreCase(viewId))
          {
            // recover the existing alignpanel, alignframe, viewport
            af = views[v].alignFrame;
            av = views[v].av;
            ap = views[v];
            // TODO: could even skip resetting view settings if we don't want to
            // change the local settings from other jalview processes
            isnewview = false;
          }
        }
      }
    }

    if (isnewview)
    {
      af = loadViewport(file, jseqs, hiddenSeqs, al, jalviewModel, view,
              uniqueSeqSetId, viewId, autoAlan);
      av = af.getViewport();
      ap = af.alignPanel;
    }

    /*
     * Load any trees, PDB structures and viewers, Overview
     * 
     * Not done if flag is false (when this method is used for New View)
     */
    if (loadTreesAndStructures)
    {
      loadTrees(jalviewModel, view, af, av, ap);
      loadPCAViewers(jalviewModel, ap);
      loadPDBStructures(jprovider, jseqs, af, ap);
      loadRnaViewers(jprovider, jseqs, ap);
      loadOverview(view, jalviewModel.getVersion(), af);
    }
    // and finally return.
    return af;
  }

  /**
   * Load Overview window, restoring colours, 'show hidden regions' flag, title
   * and geometry as saved
   * 
   * @param view
   * @param af
   */
  protected void loadOverview(Viewport view, String version, AlignFrame af)
  {
    if (!isVersionStringLaterThan("2.11.3", version)
            && view.getOverview() == null)
    {
      return;
    }
    /*
     * first close any Overview that was opened automatically
     * (if so configured in Preferences) so that the view is
     * restored in the same state as saved
     */
    af.alignPanel.closeOverviewPanel();

    Overview overview = view.getOverview();
    if (overview != null)
    {
      OverviewPanel overviewPanel = af
              .openOverviewPanel(overview.isShowHidden());
      overviewPanel.setTitle(overview.getTitle());
      overviewPanel.setFrameBounds(overview.getXpos(), overview.getYpos(),
              overview.getWidth(), overview.getHeight());
      Color gap = new Color(overview.getGapColour());
      Color residue = new Color(overview.getResidueColour());
      Color hidden = new Color(overview.getHiddenColour());
      overviewPanel.getCanvas().setColours(gap, residue, hidden);
    }
  }

  /**
   * Instantiate and link any saved RNA (Varna) viewers. The state of the Varna
   * panel is restored from separate jar entries, two (gapped and trimmed) per
   * sequence and secondary structure.
   * 
   * Currently each viewer shows just one sequence and structure (gapped and
   * trimmed), however this method is designed to support multiple sequences or
   * structures in viewers if wanted in future.
   * 
   * @param jprovider
   * @param jseqs
   * @param ap
   */
  private void loadRnaViewers(jarInputStreamProvider jprovider,
          List<JSeq> jseqs, AlignmentPanel ap)
  {
    /*
     * scan the sequences for references to viewers; create each one the first
     * time it is referenced, add Rna models to existing viewers
     */
    for (JSeq jseq : jseqs)
    {
      for (int i = 0; i < jseq.getRnaViewer().size(); i++)
      {
        RnaViewer viewer = jseq.getRnaViewer().get(i);
        AppVarna appVarna = findOrCreateVarnaViewer(viewer, uniqueSetSuffix,
                ap);

        for (int j = 0; j < viewer.getSecondaryStructure().size(); j++)
        {
          SecondaryStructure ss = viewer.getSecondaryStructure().get(j);
          SequenceI seq = seqRefIds.get(jseq.getId());
          AlignmentAnnotation ann = this.annotationIds
                  .get(ss.getAnnotationId());

          /*
           * add the structure to the Varna display (with session state copied
           * from the jar to a temporary file)
           */
          boolean gapped = safeBoolean(ss.isGapped());
          String rnaTitle = ss.getTitle();
          String sessionState = ss.getViewerState();
          String tempStateFile = copyJarEntry(jprovider, sessionState,
                  "varna", null);
          RnaModel rna = new RnaModel(rnaTitle, ann, seq, null, gapped);
          appVarna.addModelSession(rna, rnaTitle, tempStateFile);
        }
        appVarna.setInitialSelection(safeInt(viewer.getSelectedRna()));
      }
    }
  }

  /**
   * Locate and return an already instantiated matching AppVarna, or create one
   * if not found
   * 
   * @param viewer
   * @param viewIdSuffix
   * @param ap
   * @return
   */
  protected AppVarna findOrCreateVarnaViewer(RnaViewer viewer,
          String viewIdSuffix, AlignmentPanel ap)
  {
    /*
     * on each load a suffix is appended to the saved viewId, to avoid conflicts
     * if load is repeated
     */
    String postLoadId = viewer.getViewId() + viewIdSuffix;
    for (JInternalFrame frame : getAllFrames())
    {
      if (frame instanceof AppVarna)
      {
        AppVarna varna = (AppVarna) frame;
        if (postLoadId.equals(varna.getViewId()))
        {
          // this viewer is already instantiated
          // could in future here add ap as another 'parent' of the
          // AppVarna window; currently just 1-to-many
          return varna;
        }
      }
    }

    /*
     * viewer not found - make it
     */
    RnaViewerModel model = new RnaViewerModel(postLoadId, viewer.getTitle(),
            safeInt(viewer.getXpos()), safeInt(viewer.getYpos()),
            safeInt(viewer.getWidth()), safeInt(viewer.getHeight()),
            safeInt(viewer.getDividerLocation()));
    AppVarna varna = new AppVarna(model, ap);

    return varna;
  }

  /**
   * Load any saved trees
   * 
   * @param jm
   * @param view
   * @param af
   * @param av
   * @param ap
   */
  protected void loadTrees(JalviewModel jm, Viewport view, AlignFrame af,
          AlignViewport av, AlignmentPanel ap)
  {
    // TODO result of automated refactoring - are all these parameters needed?
    try
    {
      for (int t = 0; t < jm.getTree().size(); t++)
      {

        Tree tree = jm.getTree().get(t);

        TreePanel tp = (TreePanel) retrieveExistingObj(tree.getId());
        if (tp == null)
        {
          if (tree.isColumnWise())
          {
            AlignmentAnnotation aa = (AlignmentAnnotation) annotationIds
                    .get(tree.getColumnReference());
            if (aa == null)
            {
              Console.warn(
                      "Null alignment annotation when restoring columnwise tree");
            }
            tp = af.showColumnWiseTree(new NewickFile(tree.getNewick()), aa,
                    tree.getTitle(), safeInt(tree.getWidth()),
                    safeInt(tree.getHeight()), safeInt(tree.getXpos()),
                    safeInt(tree.getYpos()));

          }
          else
          {
            tp = af.showNewickTree(new NewickFile(tree.getNewick()),
                    tree.getTitle(), safeInt(tree.getWidth()),
                    safeInt(tree.getHeight()), safeInt(tree.getXpos()),
                    safeInt(tree.getYpos()));
          }
          if (tree.getId() != null)
          {
            // perhaps bind the tree id to something ?
          }
        }
        else
        {
          // update local tree attributes ?
          // TODO: should check if tp has been manipulated by user - if so its
          // settings shouldn't be modified
          tp.setTitle(tree.getTitle());
          tp.setBounds(new Rectangle(safeInt(tree.getXpos()),
                  safeInt(tree.getYpos()), safeInt(tree.getWidth()),
                  safeInt(tree.getHeight())));
          tp.setViewport(av); // af.viewport;
          // TODO: verify 'associate with all views' works still
          tp.getTreeCanvas().setViewport(av); // af.viewport;
          tp.getTreeCanvas().setAssociatedPanel(ap); // af.alignPanel;
        }
        tp.getTreeCanvas().setApplyToAllViews(tree.isLinkToAllViews());
        if (tp == null)
        {
          Console.warn(
                  "There was a problem recovering stored Newick tree: \n"
                          + tree.getNewick());
          continue;
        }

        tp.fitToWindow.setState(safeBoolean(tree.isFitToWindow()));
        tp.fitToWindow_actionPerformed(null);

        if (tree.getFontName() != null)
        {
          tp.setTreeFont(
                  new Font(tree.getFontName(), safeInt(tree.getFontStyle()),
                          safeInt(tree.getFontSize())));
        }
        else
        {
          tp.setTreeFont(
                  new Font(view.getFontName(), safeInt(view.getFontStyle()),
                          safeInt(view.getFontSize())));
        }

        tp.showPlaceholders(safeBoolean(tree.isMarkUnlinked()));
        tp.showBootstrap(safeBoolean(tree.isShowBootstrap()));
        tp.showDistances(safeBoolean(tree.isShowDistances()));

        tp.getTreeCanvas().setThreshold(safeFloat(tree.getThreshold()));

        if (safeBoolean(tree.isCurrentTree()))
        {
          af.getViewport().setCurrentTree(tp.getTree());
        }
      }

    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Load and link any saved structure viewers.
   * 
   * @param jprovider
   * @param jseqs
   * @param af
   * @param ap
   */
  protected void loadPDBStructures(jarInputStreamProvider jprovider,
          List<JSeq> jseqs, AlignFrame af, AlignmentPanel ap)
  {
    /*
     * Run through all PDB ids on the alignment, and collect mappings between
     * distinct view ids and all sequences referring to that view.
     */
    Map<String, StructureViewerModel> structureViewers = new LinkedHashMap<>();

    for (int i = 0; i < jseqs.size(); i++)
    {
      JSeq jseq = jseqs.get(i);
      if (jseq.getPdbids().size() > 0)
      {
        List<Pdbids> ids = jseq.getPdbids();
        for (int p = 0; p < ids.size(); p++)
        {
          Pdbids pdbid = ids.get(p);
          final int structureStateCount = pdbid.getStructureState().size();
          for (int s = 0; s < structureStateCount; s++)
          {
            // check to see if we haven't already created this structure view
            final StructureState structureState = pdbid.getStructureState()
                    .get(s);
            String sviewid = (structureState.getViewId() == null) ? null
                    : structureState.getViewId() + uniqueSetSuffix;
            jalview.datamodel.PDBEntry jpdb = new jalview.datamodel.PDBEntry();
            // Originally : pdbid.getFile()
            // : TODO: verify external PDB file recovery still works in normal
            // jalview project load
            jpdb.setFile(
                    loadPDBFile(jprovider, pdbid.getId(), pdbid.getFile()));
            jpdb.setId(pdbid.getId());

            int x = safeInt(structureState.getXpos());
            int y = safeInt(structureState.getYpos());
            int width = safeInt(structureState.getWidth());
            int height = safeInt(structureState.getHeight());

            // Probably don't need to do this anymore...
            // Desktop.desktop.getComponentAt(x, y);
            // TODO: NOW: check that this recovers the PDB file correctly.
            String pdbFile = loadPDBFile(jprovider, pdbid.getId(),
                    pdbid.getFile());
            jalview.datamodel.SequenceI seq = seqRefIds
                    .get(jseq.getId() + "");
            if (sviewid == null)
            {
              sviewid = "_jalview_pre2_4_" + x + "," + y + "," + width + ","
                      + height;
            }
            if (!structureViewers.containsKey(sviewid))
            {
              String viewerType = structureState.getType();
              if (viewerType == null) // pre Jalview 2.9
              {
                viewerType = ViewerType.JMOL.toString();
              }
              structureViewers.put(sviewid,
                      new StructureViewerModel(x, y, width, height, false,
                              false, true, structureState.getViewId(),
                              viewerType));
              // Legacy pre-2.7 conversion JAL-823 :
              // do not assume any view has to be linked for colour by
              // sequence
            }

            // assemble String[] { pdb files }, String[] { id for each
            // file }, orig_fileloc, SequenceI[][] {{ seqs_file 1 }, {
            // seqs_file 2}, boolean[] {
            // linkAlignPanel,superposeWithAlignpanel}} from hash
            StructureViewerModel jmoldat = structureViewers.get(sviewid);
            jmoldat.setAlignWithPanel(jmoldat.isAlignWithPanel()
                    || structureState.isAlignwithAlignPanel());

            /*
             * Default colour by linked panel to false if not specified (e.g.
             * for pre-2.7 projects)
             */
            boolean colourWithAlignPanel = jmoldat.isColourWithAlignPanel();
            colourWithAlignPanel |= structureState.isColourwithAlignPanel();
            jmoldat.setColourWithAlignPanel(colourWithAlignPanel);

            /*
             * Default colour by viewer to true if not specified (e.g. for
             * pre-2.7 projects)
             */
            boolean colourByViewer = jmoldat.isColourByViewer();
            colourByViewer &= structureState.isColourByJmol();
            jmoldat.setColourByViewer(colourByViewer);

            if (jmoldat.getStateData().length() < structureState.getValue()
                    /*Content()*/.length())
            {
              jmoldat.setStateData(structureState.getValue());// Content());
            }
            if (pdbid.getFile() != null)
            {
              File mapkey = new File(pdbid.getFile());
              StructureData seqstrmaps = jmoldat.getFileData().get(mapkey);
              if (seqstrmaps == null)
              {
                jmoldat.getFileData().put(mapkey,
                        seqstrmaps = jmoldat.new StructureData(pdbFile,
                                pdbid.getId()));
              }
              if (!seqstrmaps.getSeqList().contains(seq))
              {
                seqstrmaps.getSeqList().add(seq);
                // TODO and chains?
              }
            }
            else
            {
              errorMessage = ("The Jmol views in this project were imported\nfrom an older version of Jalview.\nPlease review the sequence colour associations\nin the Colour by section of the Jmol View menu.\n\nIn the case of problems, see note at\nhttp://issues.jalview.org/browse/JAL-747");
              Console.warn(errorMessage);
            }
          }
        }
      }
    }
    // Instantiate the associated structure views
    for (Entry<String, StructureViewerModel> entry : structureViewers
            .entrySet())
    {
      try
      {
        createOrLinkStructureViewer(entry, af, ap, jprovider);
      } catch (Exception e)
      {
        System.err.println(
                "Error loading structure viewer: " + e.getMessage());
        // failed - try the next one
      }
    }
  }

  /**
   * 
   * @param viewerData
   * @param af
   * @param ap
   * @param jprovider
   */
  protected void createOrLinkStructureViewer(
          Entry<String, StructureViewerModel> viewerData, AlignFrame af,
          AlignmentPanel ap, jarInputStreamProvider jprovider)
  {
    final StructureViewerModel stateData = viewerData.getValue();

    /*
     * Search for any viewer windows already open from other alignment views
     * that exactly match the stored structure state
     */
    StructureViewerBase comp = findMatchingViewer(viewerData);

    if (comp != null)
    {
      linkStructureViewer(ap, comp, stateData);
      return;
    }

    String type = stateData.getType();
    try
    {
      ViewerType viewerType = ViewerType.valueOf(type);
      createStructureViewer(viewerType, viewerData, af, jprovider);
    } catch (IllegalArgumentException | NullPointerException e)
    {
      // TODO JAL-3619 show error dialog / offer an alternative viewer
      Console.error("Invalid structure viewer type: " + type);
    }
  }

  /**
   * Generates a name for the entry in the project jar file to hold state
   * information for a structure viewer
   * 
   * @param viewId
   * @return
   */
  protected String getViewerJarEntryName(String viewId)
  {
    return VIEWER_PREFIX + viewId;
  }

  /**
   * Returns any open frame that matches given structure viewer data. The match
   * is based on the unique viewId, or (for older project versions) the frame's
   * geometry.
   * 
   * @param viewerData
   * @return
   */
  protected StructureViewerBase findMatchingViewer(
          Entry<String, StructureViewerModel> viewerData)
  {
    final String sviewid = viewerData.getKey();
    final StructureViewerModel svattrib = viewerData.getValue();
    StructureViewerBase comp = null;
    JInternalFrame[] frames = getAllFrames();
    for (JInternalFrame frame : frames)
    {
      if (frame instanceof StructureViewerBase)
      {
        /*
         * Post jalview 2.4 schema includes structure view id
         */
        if (sviewid != null && ((StructureViewerBase) frame).getViewId()
                .equals(sviewid))
        {
          comp = (StructureViewerBase) frame;
          break; // break added in 2.9
        }
        /*
         * Otherwise test for matching position and size of viewer frame
         */
        else if (frame.getX() == svattrib.getX()
                && frame.getY() == svattrib.getY()
                && frame.getHeight() == svattrib.getHeight()
                && frame.getWidth() == svattrib.getWidth())
        {
          comp = (StructureViewerBase) frame;
          // no break in faint hope of an exact match on viewId
        }
      }
    }
    return comp;
  }

  /**
   * Link an AlignmentPanel to an existing structure viewer.
   * 
   * @param ap
   * @param viewer
   * @param oldFiles
   * @param useinViewerSuperpos
   * @param usetoColourbyseq
   * @param viewerColouring
   */
  protected void linkStructureViewer(AlignmentPanel ap,
          StructureViewerBase viewer, StructureViewerModel stateData)
  {
    // NOTE: if the jalview project is part of a shared session then
    // view synchronization should/could be done here.

    final boolean useinViewerSuperpos = stateData.isAlignWithPanel();
    final boolean usetoColourbyseq = stateData.isColourWithAlignPanel();
    final boolean viewerColouring = stateData.isColourByViewer();
    Map<File, StructureData> oldFiles = stateData.getFileData();

    /*
     * Add mapping for sequences in this view to an already open viewer
     */
    final AAStructureBindingModel binding = viewer.getBinding();
    for (File id : oldFiles.keySet())
    {
      // add this and any other pdb files that should be present in the
      // viewer
      StructureData filedat = oldFiles.get(id);
      String pdbFile = filedat.getFilePath();
      SequenceI[] seq = filedat.getSeqList().toArray(new SequenceI[0]);
      binding.getSsm().setMapping(seq, null, pdbFile, DataSourceType.FILE,
              null);
      binding.addSequenceForStructFile(pdbFile, seq);
    }
    // and add the AlignmentPanel's reference to the view panel
    viewer.addAlignmentPanel(ap);
    if (useinViewerSuperpos)
    {
      viewer.useAlignmentPanelForSuperposition(ap);
    }
    else
    {
      viewer.excludeAlignmentPanelForSuperposition(ap);
    }
    if (usetoColourbyseq)
    {
      viewer.useAlignmentPanelForColourbyseq(ap, !viewerColouring);
    }
    else
    {
      viewer.excludeAlignmentPanelForColourbyseq(ap);
    }
  }

  /**
   * Get all frames within the Desktop.
   * 
   * @return
   */
  protected JInternalFrame[] getAllFrames()
  {
    JInternalFrame[] frames = null;
    // TODO is this necessary - is it safe - risk of hanging?
    do
    {
      try
      {
        frames = Desktop.desktop.getAllFrames();
      } catch (ArrayIndexOutOfBoundsException e)
      {
        // occasional No such child exceptions are thrown here...
        try
        {
          Thread.sleep(10);
        } catch (InterruptedException f)
        {
        }
      }
    } while (frames == null);
    return frames;
  }

  /**
   * Answers true if 'version' is equal to or later than 'supported', where each
   * is formatted as major/minor versions like "2.8.3" or "2.3.4b1" for bugfix
   * changes. Development and test values for 'version' are leniently treated
   * i.e. answer true.
   * 
   * @param supported
   *          - minimum version we are comparing against
   * @param version
   *          - version of data being processsed
   * @return true if version is equal to or later than supported
   */
  public static boolean isVersionStringLaterThan(String supported,
          String version)
  {
    if (supported == null || version == null
            || version.equalsIgnoreCase("DEVELOPMENT BUILD")
            || version.equalsIgnoreCase("Test")
            || version.equalsIgnoreCase("AUTOMATED BUILD"))
    {
      System.err.println("Assuming project file with "
              + (version == null ? "null" : version)
              + " is compatible with Jalview version " + supported);
      return true;
    }
    else
    {
      return StringUtils.compareVersions(version, supported, "b") >= 0;
    }
  }

  Vector<JalviewStructureDisplayI> newStructureViewers = null;

  protected void addNewStructureViewer(JalviewStructureDisplayI sview)
  {
    if (newStructureViewers != null)
    {
      sview.getBinding().setFinishedLoadingFromArchive(false);
      newStructureViewers.add(sview);
    }
  }

  protected void setLoadingFinishedForNewStructureViewers()
  {
    if (newStructureViewers != null)
    {
      for (JalviewStructureDisplayI sview : newStructureViewers)
      {
        sview.getBinding().setFinishedLoadingFromArchive(true);
      }
      newStructureViewers.clear();
      newStructureViewers = null;
    }
  }

  AlignFrame loadViewport(String file, List<JSeq> JSEQ,
          List<SequenceI> hiddenSeqs, AlignmentI al, JalviewModel jm,
          Viewport view, String uniqueSeqSetId, String viewId,
          List<JvAnnotRow> autoAlan)
  {
    AlignFrame af = null;
    af = new AlignFrame(al, safeInt(view.getWidth()),
            safeInt(view.getHeight()), uniqueSeqSetId, viewId)
    // {
    //
    // @Override
    // protected void processKeyEvent(java.awt.event.KeyEvent e) {
    // System.out.println("Jalview2XML AF " + e);
    // super.processKeyEvent(e);
    //
    // }
    //
    // }
    ;

    af.setFileName(file, FileFormat.Jalview);

    final AlignViewport viewport = af.getViewport();
    for (int i = 0; i < JSEQ.size(); i++)
    {
      int colour = safeInt(JSEQ.get(i).getColour());
      viewport.setSequenceColour(viewport.getAlignment().getSequenceAt(i),
              new Color(colour));
    }

    if (al.hasSeqrep())
    {
      viewport.setColourByReferenceSeq(true);
      viewport.setDisplayReferenceSeq(true);
    }

    viewport.setGatherViewsHere(safeBoolean(view.isGatheredViews()));

    if (view.getSequenceSetId() != null)
    {
      AlignmentViewport av = viewportsAdded.get(uniqueSeqSetId);

      viewport.setSequenceSetId(uniqueSeqSetId);
      if (av != null)
      {
        // propagate shared settings to this new view
        viewport.setHistoryList(av.getHistoryList());
        viewport.setRedoList(av.getRedoList());
      }
      else
      {
        viewportsAdded.put(uniqueSeqSetId, viewport);
      }
      // TODO: check if this method can be called repeatedly without
      // side-effects if alignpanel already registered.
      PaintRefresher.Register(af.alignPanel, uniqueSeqSetId);
    }
    // apply Hidden regions to view.
    if (hiddenSeqs != null)
    {
      for (int s = 0; s < JSEQ.size(); s++)
      {
        SequenceGroup hidden = new SequenceGroup();
        boolean isRepresentative = false;
        for (int r = 0; r < JSEQ.get(s).getHiddenSequences().size(); r++)
        {
          isRepresentative = true;
          SequenceI sequenceToHide = al
                  .getSequenceAt(JSEQ.get(s).getHiddenSequences().get(r));
          hidden.addSequence(sequenceToHide, false);
          // remove from hiddenSeqs list so we don't try to hide it twice
          hiddenSeqs.remove(sequenceToHide);
        }
        if (isRepresentative)
        {
          SequenceI representativeSequence = al.getSequenceAt(s);
          hidden.addSequence(representativeSequence, false);
          viewport.hideRepSequences(representativeSequence, hidden);
        }
      }

      SequenceI[] hseqs = hiddenSeqs
              .toArray(new SequenceI[hiddenSeqs.size()]);
      viewport.hideSequence(hseqs);

    }
    // recover view properties and display parameters

    viewport.setShowAnnotation(safeBoolean(view.isShowAnnotation()));
    viewport.setAbovePIDThreshold(safeBoolean(view.isPidSelected()));
    final int pidThreshold = safeInt(view.getPidThreshold());
    viewport.setThreshold(pidThreshold);

    viewport.setColourText(safeBoolean(view.isShowColourText()));

    viewport.setConservationSelected(
            safeBoolean(view.isConservationSelected()));
    viewport.setIncrement(safeInt(view.getConsThreshold()));
    viewport.setShowJVSuffix(safeBoolean(view.isShowFullId()));
    viewport.setRightAlignIds(safeBoolean(view.isRightAlignIds()));
    viewport.setFont(
            new Font(view.getFontName(), safeInt(view.getFontStyle()),
                    safeInt(view.getFontSize())),
            (view.getCharWidth() != null) ? false : true);
    if (view.getCharWidth() != null)
    {
      viewport.setCharWidth(view.getCharWidth());
      viewport.setCharHeight(view.getCharHeight());
    }
    ViewStyleI vs = viewport.getViewStyle();
    vs.setScaleProteinAsCdna(view.isScaleProteinAsCdna());
    viewport.setViewStyle(vs);
    // TODO: allow custom charWidth/Heights to be restored by updating them
    // after setting font - which means set above to false
    viewport.setRenderGaps(safeBoolean(view.isRenderGaps()));
    viewport.setWrapAlignment(safeBoolean(view.isWrapAlignment()));
    viewport.setShowAnnotation(safeBoolean(view.isShowAnnotation()));

    viewport.setShowBoxes(safeBoolean(view.isShowBoxes()));

    viewport.setShowText(safeBoolean(view.isShowText()));

    viewport.setTextColour(new Color(safeInt(view.getTextCol1())));
    viewport.setTextColour2(new Color(safeInt(view.getTextCol2())));
    viewport.setThresholdTextColour(safeInt(view.getTextColThreshold()));
    viewport.setShowUnconserved(view.isShowUnconserved());
    viewport.getRanges().setStartRes(safeInt(view.getStartRes()));

    if (view.getViewName() != null)
    {
      viewport.setViewName(view.getViewName());
      af.setInitialTabVisible();
    }
    af.setBounds(safeInt(view.getXpos()), safeInt(view.getYpos()),
            safeInt(view.getWidth()), safeInt(view.getHeight()));
    // startSeq set in af.alignPanel.updateLayout below
    af.alignPanel.updateLayout();
    ColourSchemeI cs = null;
    // apply colourschemes
    if (view.getBgColour() != null)
    {
      if (view.getBgColour().startsWith("ucs"))
      {
        cs = getUserColourScheme(jm, view.getBgColour());
      }
      else if (view.getBgColour().startsWith("Annotation"))
      {
        AnnotationColourScheme viewAnnColour = view.getAnnotationColours();
        cs = constructAnnotationColour(viewAnnColour, af, al, jm, true);

        // annpos

      }
      else
      {
        cs = ColourSchemeProperty.getColourScheme(af.getViewport(), al,
                view.getBgColour());
      }
    }

    /*
     * turn off 'alignment colour applies to all groups'
     * while restoring global colour scheme
     */
    viewport.setColourAppliesToAllGroups(false);
    viewport.setGlobalColourScheme(cs);
    viewport.getResidueShading().setThreshold(pidThreshold,
            view.isIgnoreGapsinConsensus());
    viewport.getResidueShading()
            .setConsensus(viewport.getSequenceConsensusHash());
    if (safeBoolean(view.isConservationSelected()) && cs != null)
    {
      viewport.getResidueShading()
              .setConservationInc(safeInt(view.getConsThreshold()));
    }
    af.changeColour(cs);
    viewport.setColourAppliesToAllGroups(true);

    viewport.setShowSequenceFeatures(
            safeBoolean(view.isShowSequenceFeatures()));

    viewport.setCentreColumnLabels(view.isCentreColumnLabels());
    viewport.setIgnoreGapsConsensus(view.isIgnoreGapsinConsensus(), null);
    viewport.setFollowHighlight(view.isFollowHighlight());
    viewport.followSelection = view.isFollowSelection();
    viewport.setShowConsensusHistogram(view.isShowConsensusHistogram());
    viewport.setShowSequenceLogo(view.isShowSequenceLogo());
    viewport.setNormaliseSequenceLogo(view.isNormaliseSequenceLogo());
    viewport.setShowDBRefs(safeBoolean(view.isShowDbRefTooltip()));
    viewport.setShowNPFeats(safeBoolean(view.isShowNPfeatureTooltip()));
    viewport.setShowGroupConsensus(view.isShowGroupConsensus());
    viewport.setShowGroupConservation(view.isShowGroupConservation());
    viewport.setShowComplementFeatures(view.isShowComplementFeatures());
    viewport.setShowComplementFeaturesOnTop(
            view.isShowComplementFeaturesOnTop());

    // recover feature settings
    if (jm.getFeatureSettings() != null)
    {
      FeatureRendererModel fr = af.alignPanel.getSeqPanel().seqCanvas
              .getFeatureRenderer();
      FeaturesDisplayed fdi;
      viewport.setFeaturesDisplayed(fdi = new FeaturesDisplayed());
      String[] renderOrder = new String[jm.getFeatureSettings().getSetting()
              .size()];
      Map<String, FeatureColourI> featureColours = new Hashtable<>();
      Map<String, Float> featureOrder = new Hashtable<>();

      for (int fs = 0; fs < jm.getFeatureSettings().getSetting()
              .size(); fs++)
      {
        Setting setting = jm.getFeatureSettings().getSetting().get(fs);
        String featureType = setting.getType();

        /*
         * restore feature filters (if any)
         */
        jalview.xml.binding.jalview.FeatureMatcherSet filters = setting
                .getMatcherSet();
        if (filters != null)
        {
          FeatureMatcherSetI filter = Jalview2XML.parseFilter(featureType,
                  filters);
          if (!filter.isEmpty())
          {
            fr.setFeatureFilter(featureType, filter);
          }
        }

        /*
         * restore feature colour scheme
         */
        Color maxColour = new Color(setting.getColour());
        if (setting.getMincolour() != null)
        {
          /*
           * minColour is always set unless a simple colour
           * (including for colour by label though it doesn't use it)
           */
          Color minColour = new Color(setting.getMincolour().intValue());
          Color noValueColour = minColour;
          NoValueColour noColour = setting.getNoValueColour();
          if (noColour == NoValueColour.NONE)
          {
            noValueColour = null;
          }
          else if (noColour == NoValueColour.MAX)
          {
            noValueColour = maxColour;
          }
          float min = safeFloat(safeFloat(setting.getMin()));
          float max = setting.getMax() == null ? 1f
                  : setting.getMax().floatValue();
          FeatureColourI gc = new FeatureColour(maxColour, minColour,
                  maxColour, noValueColour, min, max);
          if (setting.getAttributeName().size() > 0)
          {
            gc.setAttributeName(setting.getAttributeName().toArray(
                    new String[setting.getAttributeName().size()]));
          }
          if (setting.getThreshold() != null)
          {
            gc.setThreshold(setting.getThreshold().floatValue());
            int threshstate = safeInt(setting.getThreshstate());
            // -1 = None, 0 = Below, 1 = Above threshold
            if (threshstate == 0)
            {
              gc.setBelowThreshold(true);
            }
            else if (threshstate == 1)
            {
              gc.setAboveThreshold(true);
            }
          }
          gc.setAutoScaled(true); // default
          if (setting.isAutoScale() != null)
          {
            gc.setAutoScaled(setting.isAutoScale());
          }
          if (setting.isColourByLabel() != null)
          {
            gc.setColourByLabel(setting.isColourByLabel());
          }
          // and put in the feature colour table.
          featureColours.put(featureType, gc);
        }
        else
        {
          featureColours.put(featureType, new FeatureColour(maxColour));
        }
        renderOrder[fs] = featureType;
        if (setting.getOrder() != null)
        {
          featureOrder.put(featureType, setting.getOrder().floatValue());
        }
        else
        {
          featureOrder.put(featureType, Float.valueOf(
                  fs / jm.getFeatureSettings().getSetting().size()));
        }
        if (safeBoolean(setting.isDisplay()))
        {
          fdi.setVisible(featureType);
        }
      }
      Map<String, Boolean> fgtable = new Hashtable<>();
      for (int gs = 0; gs < jm.getFeatureSettings().getGroup().size(); gs++)
      {
        Group grp = jm.getFeatureSettings().getGroup().get(gs);
        fgtable.put(grp.getName(), Boolean.valueOf(grp.isDisplay()));
      }
      // FeatureRendererSettings frs = new FeatureRendererSettings(renderOrder,
      // fgtable, featureColours, jms.getFeatureSettings().hasTransparency() ?
      // jms.getFeatureSettings().getTransparency() : 0.0, featureOrder);
      FeatureRendererSettings frs = new FeatureRendererSettings(renderOrder,
              fgtable, featureColours, 1.0f, featureOrder);
      fr.transferSettings(frs);
    }

    if (view.getHiddenColumns().size() > 0)
    {
      for (int c = 0; c < view.getHiddenColumns().size(); c++)
      {
        final HiddenColumns hc = view.getHiddenColumns().get(c);
        viewport.hideColumns(safeInt(hc.getStart()),
                safeInt(hc.getEnd()) /* +1 */);
      }
    }
    if (view.getCalcIdParam() != null)
    {
      for (CalcIdParam calcIdParam : view.getCalcIdParam())
      {
        if (calcIdParam != null)
        {
          if (recoverCalcIdParam(calcIdParam, viewport))
          {
          }
          else
          {
            Console.warn("Couldn't recover parameters for "
                    + calcIdParam.getCalcId());
          }
        }
      }
    }
    af.setMenusFromViewport(viewport);
    af.setTitle(view.getTitle());
    // TODO: we don't need to do this if the viewport is aready visible.
    /*
     * Add the AlignFrame to the desktop (it may be 'gathered' later), unless it
     * has a 'cdna/protein complement' view, in which case save it in order to
     * populate a SplitFrame once all views have been read in.
     */
    String complementaryViewId = view.getComplementId();
    if (complementaryViewId == null)
    {
      Desktop.addInternalFrame(af, view.getTitle(),
              safeInt(view.getWidth()), safeInt(view.getHeight()));
      // recompute any autoannotation
      af.alignPanel.updateAnnotation(false, true);
      reorderAutoannotation(af, al, autoAlan);
      af.alignPanel.alignmentChanged();
    }
    else
    {
      splitFrameCandidates.put(view, af);
    }

    return af;
  }

  /**
   * Reads saved data to restore Colour by Annotation settings
   * 
   * @param viewAnnColour
   * @param af
   * @param al
   * @param model
   * @param checkGroupAnnColour
   * @return
   */
  private ColourSchemeI constructAnnotationColour(
          AnnotationColourScheme viewAnnColour, AlignFrame af,
          AlignmentI al, JalviewModel model, boolean checkGroupAnnColour)
  {
    boolean propagateAnnColour = false;
    AlignmentI annAlignment = af != null ? af.getViewport().getAlignment()
            : al;
    if (checkGroupAnnColour && al.getGroups() != null
            && al.getGroups().size() > 0)
    {
      // pre 2.8.1 behaviour
      // check to see if we should transfer annotation colours
      propagateAnnColour = true;
      for (SequenceGroup sg : al.getGroups())
      {
        if (sg.getColourScheme() instanceof AnnotationColourGradient)
        {
          propagateAnnColour = false;
        }
      }
    }

    /*
     * 2.10.2- : saved annotationId is AlignmentAnnotation.annotationId
     */
    String annotationId = viewAnnColour.getAnnotation();
    AlignmentAnnotation matchedAnnotation = annotationIds.get(annotationId);

    /*
     * pre 2.10.2: saved annotationId is AlignmentAnnotation.label
     */
    if (matchedAnnotation == null
            && annAlignment.getAlignmentAnnotation() != null)
    {
      for (int i = 0; i < annAlignment.getAlignmentAnnotation().length; i++)
      {
        if (annotationId
                .equals(annAlignment.getAlignmentAnnotation()[i].label))
        {
          matchedAnnotation = annAlignment.getAlignmentAnnotation()[i];
          break;
        }
      }
    }
    if (matchedAnnotation == null)
    {
      System.err.println("Failed to match annotation colour scheme for "
              + annotationId);
      return null;
    }
    // belt-and-braces create a threshold line if the
    // colourscheme needs one but the matchedAnnotation doesn't have one
    if (safeInt(viewAnnColour.getAboveThreshold()) != 0
            && matchedAnnotation.getThreshold() == null)
    {
      matchedAnnotation.setThreshold(
              new GraphLine(safeFloat(viewAnnColour.getThreshold()),
                      "Threshold", Color.black));
    }

    AnnotationColourGradient cs = null;
    if (viewAnnColour.getColourScheme().equals("None"))
    {
      cs = new AnnotationColourGradient(matchedAnnotation,
              new Color(safeInt(viewAnnColour.getMinColour())),
              new Color(safeInt(viewAnnColour.getMaxColour())),
              safeInt(viewAnnColour.getAboveThreshold()));
    }
    else if (viewAnnColour.getColourScheme().startsWith("ucs"))
    {
      cs = new AnnotationColourGradient(matchedAnnotation,
              getUserColourScheme(model, viewAnnColour.getColourScheme()),
              safeInt(viewAnnColour.getAboveThreshold()));
    }
    else
    {
      cs = new AnnotationColourGradient(matchedAnnotation,
              ColourSchemeProperty.getColourScheme(af.getViewport(), al,
                      viewAnnColour.getColourScheme()),
              safeInt(viewAnnColour.getAboveThreshold()));
    }

    boolean perSequenceOnly = safeBoolean(viewAnnColour.isPerSequence());
    boolean useOriginalColours = safeBoolean(
            viewAnnColour.isPredefinedColours());
    cs.setSeqAssociated(perSequenceOnly);
    cs.setPredefinedColours(useOriginalColours);

    if (propagateAnnColour && al.getGroups() != null)
    {
      // Also use these settings for all the groups
      for (int g = 0; g < al.getGroups().size(); g++)
      {
        SequenceGroup sg = al.getGroups().get(g);
        if (sg.getGroupColourScheme() == null)
        {
          continue;
        }

        AnnotationColourGradient groupScheme = new AnnotationColourGradient(
                matchedAnnotation, sg.getColourScheme(),
                safeInt(viewAnnColour.getAboveThreshold()));
        sg.setColourScheme(groupScheme);
        groupScheme.setSeqAssociated(perSequenceOnly);
        groupScheme.setPredefinedColours(useOriginalColours);
      }
    }
    return cs;
  }

  private void reorderAutoannotation(AlignFrame af, AlignmentI al,
          List<JvAnnotRow> autoAlan)
  {
    // copy over visualization settings for autocalculated annotation in the
    // view
    if (al.getAlignmentAnnotation() != null)
    {
      /**
       * Kludge for magic autoannotation names (see JAL-811)
       */
      String[] magicNames = new String[] { "Consensus", "Quality",
          "Conservation" };
      JvAnnotRow nullAnnot = new JvAnnotRow(-1, null);
      Hashtable<String, JvAnnotRow> visan = new Hashtable<>();
      for (String nm : magicNames)
      {
        visan.put(nm, nullAnnot);
      }
      for (JvAnnotRow auan : autoAlan)
      {
        visan.put(auan.template.label
                + (auan.template.getCalcId() == null ? ""
                        : "\t" + auan.template.getCalcId()),
                auan);
      }
      int hSize = al.getAlignmentAnnotation().length;
      List<JvAnnotRow> reorder = new ArrayList<>();
      // work through any autoCalculated annotation already on the view
      // removing it if it should be placed in a different location on the
      // annotation panel.
      List<String> remains = new ArrayList<>(visan.keySet());
      for (int h = 0; h < hSize; h++)
      {
        jalview.datamodel.AlignmentAnnotation jalan = al
                .getAlignmentAnnotation()[h];
        if (jalan.autoCalculated)
        {
          String k;
          JvAnnotRow valan = visan.get(k = jalan.label);
          if (jalan.getCalcId() != null)
          {
            valan = visan.get(k = jalan.label + "\t" + jalan.getCalcId());
          }

          if (valan != null)
          {
            // delete the auto calculated row from the alignment
            al.deleteAnnotation(jalan, false);
            remains.remove(k);
            hSize--;
            h--;
            if (valan != nullAnnot)
            {
              if (jalan != valan.template)
              {
                // newly created autoannotation row instance
                // so keep a reference to the visible annotation row
                // and copy over all relevant attributes
                if (valan.template.graphHeight >= 0)

                {
                  jalan.graphHeight = valan.template.graphHeight;
                }
                jalan.visible = valan.template.visible;
              }
              reorder.add(new JvAnnotRow(valan.order, jalan));
            }
          }
        }
      }
      // Add any (possibly stale) autocalculated rows that were not appended to
      // the view during construction
      for (String other : remains)
      {
        JvAnnotRow othera = visan.get(other);
        if (othera != nullAnnot && othera.template.getCalcId() != null
                && othera.template.getCalcId().length() > 0)
        {
          reorder.add(othera);
        }
      }
      // now put the automatic annotation in its correct place
      int s = 0, srt[] = new int[reorder.size()];
      JvAnnotRow[] rws = new JvAnnotRow[reorder.size()];
      for (JvAnnotRow jvar : reorder)
      {
        rws[s] = jvar;
        srt[s++] = jvar.order;
      }
      reorder.clear();
      jalview.util.QuickSort.sort(srt, rws);
      // and re-insert the annotation at its correct position
      for (JvAnnotRow jvar : rws)
      {
        al.addAnnotation(jvar.template, jvar.order);
      }
      af.alignPanel.adjustAnnotationHeight();
    }
  }

  Hashtable skipList = null;

  /**
   * TODO remove this method
   * 
   * @param view
   * @return AlignFrame bound to sequenceSetId from view, if one exists. private
   *         AlignFrame getSkippedFrame(Viewport view) { if (skipList==null) {
   *         throw new Error("Implementation Error. No skipList defined for this
   *         Jalview2XML instance."); } return (AlignFrame)
   *         skipList.get(view.getSequenceSetId()); }
   */

  /**
   * Check if the Jalview view contained in object should be skipped or not.
   * 
   * @param object
   * @return true if view's sequenceSetId is a key in skipList
   */
  private boolean skipViewport(JalviewModel object)
  {
    if (skipList == null)
    {
      return false;
    }
    String id = object.getViewport().get(0).getSequenceSetId();
    if (skipList.containsKey(id))
    {
      Console.debug("Skipping seuqence set id " + id);
      return true;
    }
    return false;
  }

  public void addToSkipList(AlignFrame af)
  {
    if (skipList == null)
    {
      skipList = new Hashtable();
    }
    skipList.put(af.getViewport().getSequenceSetId(), af);
  }

  public void clearSkipList()
  {
    if (skipList != null)
    {
      skipList.clear();
      skipList = null;
    }
  }

  private void recoverDatasetFor(SequenceSet vamsasSet, AlignmentI al,
          boolean ignoreUnrefed, String uniqueSeqSetId)
  {
    jalview.datamodel.AlignmentI ds = getDatasetFor(
            vamsasSet.getDatasetId());
    AlignmentI xtant_ds = ds;
    if (xtant_ds == null)
    {
      // good chance we are about to create a new dataset, but check if we've
      // seen some of the dataset sequence IDs before.
      // TODO: skip this check if we are working with project generated by
      // version 2.11 or later
      xtant_ds = checkIfHasDataset(vamsasSet.getSequence());
      if (xtant_ds != null)
      {
        ds = xtant_ds;
        addDatasetRef(vamsasSet.getDatasetId(), ds);
      }
    }
    Vector<SequenceI> dseqs = null;
    if (!ignoreUnrefed)
    {
      // recovering an alignment View
      AlignmentI seqSetDS = getDatasetFor(UNIQSEQSETID + uniqueSeqSetId);
      if (seqSetDS != null)
      {
        if (ds != null && ds != seqSetDS)
        {
          Console.warn(
                  "JAL-3171 regression: Overwriting a dataset reference for an alignment"
                          + " - CDS/Protein crossreference data may be lost");
          if (xtant_ds != null)
          {
            // This can only happen if the unique sequence set ID was bound to a
            // dataset that did not contain any of the sequences in the view
            // currently being restored.
            Console.warn(
                    "JAL-3171 SERIOUS!  TOTAL CONFUSION - please consider contacting the Jalview Development team so they can investigate why your project caused this message to be displayed.");
          }
        }
        ds = seqSetDS;
        addDatasetRef(vamsasSet.getDatasetId(), ds);
      }
    }
    if (ds == null)
    {
      // try even harder to restore dataset
      AlignmentI xtantDS = checkIfHasDataset(vamsasSet.getSequence());
      // create a list of new dataset sequences
      dseqs = new Vector<>();
    }
    for (int i = 0, iSize = vamsasSet.getSequence().size(); i < iSize; i++)
    {
      Sequence vamsasSeq = vamsasSet.getSequence().get(i);
      ensureJalviewDatasetSequence(vamsasSeq, ds, dseqs, ignoreUnrefed, i);
    }
    // create a new dataset
    if (ds == null)
    {
      SequenceI[] dsseqs = new SequenceI[dseqs.size()];
      dseqs.copyInto(dsseqs);
      ds = new jalview.datamodel.Alignment(dsseqs);
      Console.debug("Created new dataset " + vamsasSet.getDatasetId()
              + " for alignment " + System.identityHashCode(al));
      addDatasetRef(vamsasSet.getDatasetId(), ds);
    }
    // set the dataset for the newly imported alignment.
    if (al.getDataset() == null && !ignoreUnrefed)
    {
      al.setDataset(ds);
      // register dataset for the alignment's uniqueSeqSetId for legacy projects
      addDatasetRef(UNIQSEQSETID + uniqueSeqSetId, ds);
    }
    updateSeqDatasetBinding(vamsasSet.getSequence(), ds);
  }

  /**
   * XML dataset sequence ID to materialised dataset reference
   */
  HashMap<String, AlignmentI> seqToDataset = new HashMap<>();

  /**
   * @return the first materialised dataset reference containing a dataset
   *         sequence referenced in the given view
   * @param list
   *          - sequences from the view
   */
  AlignmentI checkIfHasDataset(List<Sequence> list)
  {
    for (Sequence restoredSeq : list)
    {
      AlignmentI datasetFor = seqToDataset.get(restoredSeq.getDsseqid());
      if (datasetFor != null)
      {
        return datasetFor;
      }
    }
    return null;
  }

  /**
   * Register ds as the containing dataset for the dataset sequences referenced
   * by sequences in list
   * 
   * @param list
   *          - sequences in a view
   * @param ds
   */
  void updateSeqDatasetBinding(List<Sequence> list, AlignmentI ds)
  {
    for (Sequence restoredSeq : list)
    {
      AlignmentI prevDS = seqToDataset.put(restoredSeq.getDsseqid(), ds);
      if (prevDS != null && prevDS != ds)
      {
        Console.warn("Dataset sequence appears in many datasets: "
                + restoredSeq.getDsseqid());
        // TODO: try to merge!
      }
    }
  }

  /**
   * 
   * @param vamsasSeq
   *          sequence definition to create/merge dataset sequence for
   * @param ds
   *          dataset alignment
   * @param dseqs
   *          vector to add new dataset sequence to
   * @param ignoreUnrefed
   *          - when true, don't create new sequences from vamsasSeq if it's id
   *          doesn't already have an asssociated Jalview sequence.
   * @param vseqpos
   *          - used to reorder the sequence in the alignment according to the
   *          vamsasSeq array ordering, to preserve ordering of dataset
   */
  private void ensureJalviewDatasetSequence(Sequence vamsasSeq,
          AlignmentI ds, Vector<SequenceI> dseqs, boolean ignoreUnrefed,
          int vseqpos)
  {
    // JBP TODO: Check this is called for AlCodonFrames to support recovery of
    // xRef Codon Maps
    SequenceI sq = seqRefIds.get(vamsasSeq.getId());
    boolean reorder = false;
    SequenceI dsq = null;
    if (sq != null && sq.getDatasetSequence() != null)
    {
      dsq = sq.getDatasetSequence();
    }
    else
    {
      reorder = true;
    }
    if (sq == null && ignoreUnrefed)
    {
      return;
    }
    String sqid = vamsasSeq.getDsseqid();
    if (dsq == null)
    {
      // need to create or add a new dataset sequence reference to this sequence
      if (sqid != null)
      {
        dsq = seqRefIds.get(sqid);
      }
      // check again
      if (dsq == null)
      {
        // make a new dataset sequence
        dsq = sq.createDatasetSequence();
        if (sqid == null)
        {
          // make up a new dataset reference for this sequence
          sqid = seqHash(dsq);
        }
        dsq.setVamsasId(uniqueSetSuffix + sqid);
        seqRefIds.put(sqid, dsq);
        if (ds == null)
        {
          if (dseqs != null)
          {
            dseqs.addElement(dsq);
          }
        }
        else
        {
          ds.addSequence(dsq);
        }
      }
      else
      {
        if (sq != dsq)
        { // make this dataset sequence sq's dataset sequence
          sq.setDatasetSequence(dsq);
          // and update the current dataset alignment
          if (ds == null)
          {
            if (dseqs != null)
            {
              if (!dseqs.contains(dsq))
              {
                dseqs.add(dsq);
              }
            }
            else
            {
              if (ds.findIndex(dsq) < 0)
              {
                ds.addSequence(dsq);
              }
            }
          }
        }
      }
    }
    // TODO: refactor this as a merge dataset sequence function
    // now check that sq (the dataset sequence) sequence really is the union of
    // all references to it
    // boolean pre = sq.getStart() < dsq.getStart();
    // boolean post = sq.getEnd() > dsq.getEnd();
    // if (pre || post)
    if (sq != dsq)
    {
      // StringBuffer sb = new StringBuffer();
      String newres = jalview.analysis.AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars, sq.getSequenceAsString());
      if (!newres.equalsIgnoreCase(dsq.getSequenceAsString())
              && newres.length() > dsq.getLength())
      {
        // Update with the longer sequence.
        synchronized (dsq)
        {
          /*
           * if (pre) { sb.insert(0, newres .substring(0, dsq.getStart() -
           * sq.getStart())); dsq.setStart(sq.getStart()); } if (post) {
           * sb.append(newres.substring(newres.length() - sq.getEnd() -
           * dsq.getEnd())); dsq.setEnd(sq.getEnd()); }
           */
          dsq.setSequence(newres);
        }
        // TODO: merges will never happen if we 'know' we have the real dataset
        // sequence - this should be detected when id==dssid
        System.err.println(
                "DEBUG Notice:  Merged dataset sequence (if you see this often, post at http://issues.jalview.org/browse/JAL-1474)"); // ("
        // + (pre ? "prepended" : "") + " "
        // + (post ? "appended" : ""));
      }
    }
    else
    {
      // sequence refs are identical. We may need to update the existing dataset
      // alignment with this one, though.
      if (ds != null && dseqs == null)
      {
        int opos = ds.findIndex(dsq);
        SequenceI tseq = null;
        if (opos != -1 && vseqpos != opos)
        {
          // remove from old position
          ds.deleteSequence(dsq);
        }
        if (vseqpos < ds.getHeight())
        {
          if (vseqpos != opos)
          {
            // save sequence at destination position
            tseq = ds.getSequenceAt(vseqpos);
            ds.replaceSequenceAt(vseqpos, dsq);
            ds.addSequence(tseq);
          }
        }
        else
        {
          ds.addSequence(dsq);
        }
      }
    }
  }

  /*
   * TODO use AlignmentI here and in related methods - needs
   * AlignmentI.getDataset() changed to return AlignmentI instead of Alignment
   */
  Hashtable<String, AlignmentI> datasetIds = null;

  IdentityHashMap<AlignmentI, String> dataset2Ids = null;

  private AlignmentI getDatasetFor(String datasetId)
  {
    if (datasetIds == null)
    {
      datasetIds = new Hashtable<>();
      return null;
    }
    if (datasetIds.containsKey(datasetId))
    {
      return datasetIds.get(datasetId);
    }
    return null;
  }

  private void addDatasetRef(String datasetId, AlignmentI dataset)
  {
    if (datasetIds == null)
    {
      datasetIds = new Hashtable<>();
    }
    datasetIds.put(datasetId, dataset);
  }

  /**
   * make a new dataset ID for this jalview dataset alignment
   * 
   * @param dataset
   * @return
   */
  private String getDatasetIdRef(AlignmentI dataset)
  {
    if (dataset.getDataset() != null)
    {
      Console.warn(
              "Serious issue!  Dataset Object passed to getDatasetIdRef is not a Jalview DATASET alignment...");
    }
    String datasetId = makeHashCode(dataset, null);
    if (datasetId == null)
    {
      // make a new datasetId and record it
      if (dataset2Ids == null)
      {
        dataset2Ids = new IdentityHashMap<>();
      }
      else
      {
        datasetId = dataset2Ids.get(dataset);
      }
      if (datasetId == null)
      {
        datasetId = "ds" + dataset2Ids.size() + 1;
        dataset2Ids.put(dataset, datasetId);
      }
    }
    return datasetId;
  }

  /**
   * Add any saved DBRefEntry's to the sequence. An entry flagged as 'locus' is
   * constructed as a special subclass GeneLocus.
   * 
   * @param datasetSequence
   * @param sequence
   */
  private void addDBRefs(SequenceI datasetSequence, Sequence sequence)
  {
    for (int d = 0; d < sequence.getDBRef().size(); d++)
    {
      DBRef dr = sequence.getDBRef().get(d);
      DBRefEntry entry;
      if (dr.isLocus())
      {
        entry = new GeneLocus(dr.getSource(), dr.getVersion(),
                dr.getAccessionId());
      }
      else
      {
        entry = new DBRefEntry(dr.getSource(), dr.getVersion(),
                dr.getAccessionId());
      }
      if (dr.getMapping() != null)
      {
        entry.setMap(addMapping(dr.getMapping()));
      }
      entry.setCanonical(dr.isCanonical());
      datasetSequence.addDBRef(entry);
    }
  }

  private jalview.datamodel.Mapping addMapping(Mapping m)
  {
    SequenceI dsto = null;
    // Mapping m = dr.getMapping();
    int fr[] = new int[m.getMapListFrom().size() * 2];
    Iterator<MapListFrom> from = m.getMapListFrom().iterator();// enumerateMapListFrom();
    for (int _i = 0; from.hasNext(); _i += 2)
    {
      MapListFrom mf = from.next();
      fr[_i] = mf.getStart();
      fr[_i + 1] = mf.getEnd();
    }
    int fto[] = new int[m.getMapListTo().size() * 2];
    Iterator<MapListTo> to = m.getMapListTo().iterator();// enumerateMapListTo();
    for (int _i = 0; to.hasNext(); _i += 2)
    {
      MapListTo mf = to.next();
      fto[_i] = mf.getStart();
      fto[_i + 1] = mf.getEnd();
    }
    jalview.datamodel.Mapping jmap = new jalview.datamodel.Mapping(dsto, fr,
            fto, m.getMapFromUnit().intValue(),
            m.getMapToUnit().intValue());

    /*
     * (optional) choice of dseqFor or Sequence
     */
    if (m.getDseqFor() != null)
    {
      String dsfor = m.getDseqFor();
      if (seqRefIds.containsKey(dsfor))
      {
        /*
         * recover from hash
         */
        jmap.setTo(seqRefIds.get(dsfor));
      }
      else
      {
        frefedSequence.add(newMappingRef(dsfor, jmap));
      }
    }
    else if (m.getSequence() != null)
    {
      /*
       * local sequence definition
       */
      Sequence ms = m.getSequence();
      SequenceI djs = null;
      String sqid = ms.getDsseqid();
      if (sqid != null && sqid.length() > 0)
      {
        /*
         * recover dataset sequence
         */
        djs = seqRefIds.get(sqid);
      }
      else
      {
        System.err.println(
                "Warning - making up dataset sequence id for DbRef sequence map reference");
        sqid = ((Object) ms).toString(); // make up a new hascode for
        // undefined dataset sequence hash
        // (unlikely to happen)
      }

      if (djs == null)
      {
        /**
         * make a new dataset sequence and add it to refIds hash
         */
        djs = new jalview.datamodel.Sequence(ms.getName(),
                ms.getSequence());
        djs.setStart(jmap.getMap().getToLowest());
        djs.setEnd(jmap.getMap().getToHighest());
        djs.setVamsasId(uniqueSetSuffix + sqid);
        jmap.setTo(djs);
        incompleteSeqs.put(sqid, djs);
        seqRefIds.put(sqid, djs);

      }
      Console.debug("about to recurse on addDBRefs.");
      addDBRefs(djs, ms);

    }

    return jmap;
  }

  /**
   * Provides a 'copy' of an alignment view (on action New View) by 'saving' the
   * view as XML (but not to file), and then reloading it
   * 
   * @param ap
   * @return
   */
  public AlignmentPanel copyAlignPanel(AlignmentPanel ap)
  {
    initSeqRefs();
    JalviewModel jm = saveState(ap, null, null, null);

    addDatasetRef(
            jm.getVamsasModel().getSequenceSet().get(0).getDatasetId(),
            ap.getAlignment().getDataset());

    uniqueSetSuffix = "";
    // jm.getJalviewModelSequence().getViewport(0).setId(null);
    jm.getViewport().get(0).setId(null);
    // we don't overwrite the view we just copied

    if (this.frefedSequence == null)
    {
      frefedSequence = new Vector<>();
    }

    viewportsAdded.clear();

    AlignFrame af = loadFromObject(jm, null, false, null);
    af.getAlignPanels().clear();
    af.closeMenuItem_actionPerformed(true);

    /*
     * if(ap.av.getAlignment().getAlignmentAnnotation()!=null) { for(int i=0;
     * i<ap.av.getAlignment().getAlignmentAnnotation().length; i++) {
     * if(!ap.av.getAlignment().getAlignmentAnnotation()[i].autoCalculated) {
     * af.alignPanel.av.getAlignment().getAlignmentAnnotation()[i] =
     * ap.av.getAlignment().getAlignmentAnnotation()[i]; } } }
     */

    return af.alignPanel;
  }

  private Hashtable jvids2vobj;

  /**
   * set the object to ID mapping tables used to write/recover objects and XML
   * ID strings for the jalview project. If external tables are provided then
   * finalize and clearSeqRefs will not clear the tables when the Jalview2XML
   * object goes out of scope. - also populates the datasetIds hashtable with
   * alignment objects containing dataset sequences
   * 
   * @param vobj2jv
   *          Map from ID strings to jalview datamodel
   * @param jv2vobj
   *          Map from jalview datamodel to ID strings
   * 
   * 
   */
  public void setObjectMappingTables(Hashtable vobj2jv,
          IdentityHashMap jv2vobj)
  {
    this.jv2vobj = jv2vobj;
    this.vobj2jv = vobj2jv;
    Iterator ds = jv2vobj.keySet().iterator();
    String id;
    while (ds.hasNext())
    {
      Object jvobj = ds.next();
      id = jv2vobj.get(jvobj).toString();
      if (jvobj instanceof jalview.datamodel.Alignment)
      {
        if (((jalview.datamodel.Alignment) jvobj).getDataset() == null)
        {
          addDatasetRef(id, (jalview.datamodel.Alignment) jvobj);
        }
      }
      else if (jvobj instanceof jalview.datamodel.Sequence)
      {
        // register sequence object so the XML parser can recover it.
        if (seqRefIds == null)
        {
          seqRefIds = new HashMap<>();
        }
        if (seqsToIds == null)
        {
          seqsToIds = new IdentityHashMap<>();
        }
        seqRefIds.put(jv2vobj.get(jvobj).toString(), (SequenceI) jvobj);
        seqsToIds.put((SequenceI) jvobj, id);
      }
      else if (jvobj instanceof jalview.datamodel.AlignmentAnnotation)
      {
        String anid;
        AlignmentAnnotation jvann = (AlignmentAnnotation) jvobj;
        annotationIds.put(anid = jv2vobj.get(jvobj).toString(), jvann);
        if (jvann.annotationId == null)
        {
          jvann.annotationId = anid;
        }
        if (!jvann.annotationId.equals(anid))
        {
          // TODO verify that this is the correct behaviour
          Console.warn("Overriding Annotation ID for " + anid
                  + " from different id : " + jvann.annotationId);
          jvann.annotationId = anid;
        }
      }
      else if (jvobj instanceof String)
      {
        if (jvids2vobj == null)
        {
          jvids2vobj = new Hashtable();
          jvids2vobj.put(jvobj, jv2vobj.get(jvobj).toString());
        }
      }
      else
      {
        Console.debug("Ignoring " + jvobj.getClass() + " (ID = " + id);
      }
    }
  }

  /**
   * set the uniqueSetSuffix used to prefix/suffix object IDs for jalview
   * objects created from the project archive. If string is null (default for
   * construction) then suffix will be set automatically.
   * 
   * @param string
   */
  public void setUniqueSetSuffix(String string)
  {
    uniqueSetSuffix = string;

  }

  /**
   * uses skipList2 as the skipList for skipping views on sequence sets
   * associated with keys in the skipList
   * 
   * @param skipList2
   */
  public void setSkipList(Hashtable skipList2)
  {
    skipList = skipList2;
  }

  /**
   * Reads the jar entry of given name and returns its contents, or null if the
   * entry is not found.
   * 
   * @param jprovider
   * @param jarEntryName
   * @return
   */
  protected String readJarEntry(jarInputStreamProvider jprovider,
          String jarEntryName)
  {
    String result = null;
    BufferedReader in = null;

    try
    {
      /*
       * Reopen the jar input stream and traverse its entries to find a matching
       * name
       */
      JarInputStream jin = jprovider.getJarInputStream();
      JarEntry entry = null;
      do
      {
        entry = jin.getNextJarEntry();
      } while (entry != null && !entry.getName().equals(jarEntryName));

      if (entry != null)
      {
        StringBuilder out = new StringBuilder(256);
        in = new BufferedReader(new InputStreamReader(jin, UTF_8));
        String data;

        while ((data = in.readLine()) != null)
        {
          out.append(data);
        }
        result = out.toString();
      }
      else
      {
        Console.warn(
                "Couldn't find entry in Jalview Jar for " + jarEntryName);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    } finally
    {
      if (in != null)
      {
        try
        {
          in.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }

    return result;
  }

  /**
   * Returns an incrementing counter (0, 1, 2...)
   * 
   * @return
   */
  private synchronized int nextCounter()
  {
    return counter++;
  }

  /**
   * Loads any saved PCA viewers
   * 
   * @param jms
   * @param ap
   */
  protected void loadPCAViewers(JalviewModel model, AlignmentPanel ap)
  {
    try
    {
      List<PcaViewer> pcaviewers = model.getPcaViewer();
      for (PcaViewer viewer : pcaviewers)
      {
        String modelName = viewer.getScoreModelName();
        SimilarityParamsI params = new SimilarityParams(
                viewer.isIncludeGappedColumns(), viewer.isMatchGaps(),
                viewer.isIncludeGaps(),
                viewer.isDenominateByShortestLength());

        /*
         * create the panel (without computing the PCA)
         */
        PCAPanel panel = new PCAPanel(ap, modelName, params);

        panel.setTitle(viewer.getTitle());
        panel.setBounds(new Rectangle(viewer.getXpos(), viewer.getYpos(),
                viewer.getWidth(), viewer.getHeight()));

        boolean showLabels = viewer.isShowLabels();
        panel.setShowLabels(showLabels);
        panel.getRotatableCanvas().setShowLabels(showLabels);
        panel.getRotatableCanvas()
                .setBgColour(new Color(viewer.getBgColour()));
        panel.getRotatableCanvas()
                .setApplyToAllViews(viewer.isLinkToAllViews());

        /*
         * load PCA output data
         */
        ScoreModelI scoreModel = ScoreModels.getInstance()
                .getScoreModel(modelName, ap);
        PCA pca = new PCA(null, scoreModel, params);
        PcaDataType pcaData = viewer.getPcaData();

        MatrixI pairwise = loadDoubleMatrix(pcaData.getPairwiseMatrix());
        pca.setPairwiseScores(pairwise);

        MatrixI triDiag = loadDoubleMatrix(pcaData.getTridiagonalMatrix());
        pca.setTridiagonal(triDiag);

        MatrixI result = loadDoubleMatrix(pcaData.getEigenMatrix());
        pca.setEigenmatrix(result);

        panel.getPcaModel().setPCA(pca);

        /*
         * we haven't saved the input data! (JAL-2647 to do)
         */
        panel.setInputData(null);

        /*
         * add the sequence points for the PCA display
         */
        List<jalview.datamodel.SequencePoint> seqPoints = new ArrayList<>();
        for (SequencePoint sp : viewer.getSequencePoint())
        {
          String seqId = sp.getSequenceRef();
          SequenceI seq = seqRefIds.get(seqId);
          if (seq == null)
          {
            throw new IllegalStateException(
                    "Unmatched seqref for PCA: " + seqId);
          }
          Point pt = new Point(sp.getXPos(), sp.getYPos(), sp.getZPos());
          jalview.datamodel.SequencePoint seqPoint = new jalview.datamodel.SequencePoint(
                  seq, pt);
          seqPoints.add(seqPoint);
        }
        panel.getRotatableCanvas().setPoints(seqPoints, seqPoints.size());

        /*
         * set min-max ranges and scale after setPoints (which recomputes them)
         */
        panel.getRotatableCanvas().setScaleFactor(viewer.getScaleFactor());
        SeqPointMin spMin = viewer.getSeqPointMin();
        float[] min = new float[] { spMin.getXPos(), spMin.getYPos(),
            spMin.getZPos() };
        SeqPointMax spMax = viewer.getSeqPointMax();
        float[] max = new float[] { spMax.getXPos(), spMax.getYPos(),
            spMax.getZPos() };
        panel.getRotatableCanvas().setSeqMinMax(min, max);

        // todo: hold points list in PCAModel only
        panel.getPcaModel().setSequencePoints(seqPoints);

        panel.setSelectedDimensionIndex(viewer.getXDim(), X);
        panel.setSelectedDimensionIndex(viewer.getYDim(), Y);
        panel.setSelectedDimensionIndex(viewer.getZDim(), Z);

        // is this duplication needed?
        panel.setTop(seqPoints.size() - 1);
        panel.getPcaModel().setTop(seqPoints.size() - 1);

        /*
         * add the axes' end points for the display
         */
        for (int i = 0; i < 3; i++)
        {
          Axis axis = viewer.getAxis().get(i);
          panel.getRotatableCanvas().getAxisEndPoints()[i] = new Point(
                  axis.getXPos(), axis.getYPos(), axis.getZPos());
        }

        Desktop.addInternalFrame(panel, MessageManager.formatMessage(
                "label.calc_title", "PCA", modelName), 475, 450);
      }
    } catch (Exception ex)
    {
      Console.error("Error loading PCA: " + ex.toString());
    }
  }

  /**
   * Creates a new structure viewer window
   * 
   * @param viewerType
   * @param viewerData
   * @param af
   * @param jprovider
   */
  protected void createStructureViewer(ViewerType viewerType,
          final Entry<String, StructureViewerModel> viewerData,
          AlignFrame af, jarInputStreamProvider jprovider)
  {
    final StructureViewerModel viewerModel = viewerData.getValue();
    String sessionFilePath = null;

    if (viewerType == ViewerType.JMOL)
    {
      sessionFilePath = rewriteJmolSession(viewerModel, jprovider);
    }
    else
    {
      String viewerJarEntryName = getViewerJarEntryName(
              viewerModel.getViewId());
      sessionFilePath = copyJarEntry(jprovider, viewerJarEntryName,
              "viewerSession", ".tmp");
    }
    final String sessionPath = sessionFilePath;
    final String sviewid = viewerData.getKey();
    try
    {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        @Override
        public void run()
        {
          JalviewStructureDisplayI sview = null;
          try
          {
            sview = StructureViewer.createView(viewerType, af.alignPanel,
                    viewerModel, sessionPath, sviewid);
            addNewStructureViewer(sview);
          } catch (OutOfMemoryError ex)
          {
            new OOMWarning("Restoring structure view for " + viewerType,
                    (OutOfMemoryError) ex.getCause());
            if (sview != null && sview.isVisible())
            {
              sview.closeViewer(false);
              sview.setVisible(false);
              sview.dispose();
            }
          }
        }
      });
    } catch (InvocationTargetException | InterruptedException ex)
    {
      Console.warn("Unexpected error when opening " + viewerType
              + " structure viewer", ex);
    }
  }

  /**
   * Rewrites a Jmol session script, saves it to a temporary file, and returns
   * the path of the file. "load file" commands are rewritten to change the
   * original PDB file names to those created as the Jalview project is loaded.
   * 
   * @param svattrib
   * @param jprovider
   * @return
   */
  private String rewriteJmolSession(StructureViewerModel svattrib,
          jarInputStreamProvider jprovider)
  {
    String state = svattrib.getStateData(); // Jalview < 2.9
    if (state == null || state.isEmpty()) // Jalview >= 2.9
    {
      String jarEntryName = getViewerJarEntryName(svattrib.getViewId());
      state = readJarEntry(jprovider, jarEntryName);
    }
    // TODO or simpler? for each key in oldFiles,
    // replace key.getPath() in state with oldFiles.get(key).getFilePath()
    // (allowing for different path escapings)
    StringBuilder rewritten = new StringBuilder(state.length());
    int cp = 0, ncp, ecp;
    Map<File, StructureData> oldFiles = svattrib.getFileData();
    while ((ncp = state.indexOf("load ", cp)) > -1)
    {
      do
      {
        // look for next filename in load statement
        rewritten.append(state.substring(cp,
                ncp = (state.indexOf("\"", ncp + 1) + 1)));
        String oldfilenam = state.substring(ncp,
                ecp = state.indexOf("\"", ncp));
        // recover the new mapping data for this old filename
        // have to normalize filename - since Jmol and jalview do
        // filename translation differently.
        StructureData filedat = oldFiles.get(new File(oldfilenam));
        if (filedat == null)
        {
          String reformatedOldFilename = oldfilenam.replaceAll("/", "\\\\");
          filedat = oldFiles.get(new File(reformatedOldFilename));
        }
        rewritten.append(Platform.escapeBackslashes(filedat.getFilePath()));
        rewritten.append("\"");
        cp = ecp + 1; // advance beyond last \" and set cursor so we can
                      // look for next file statement.
      } while ((ncp = state.indexOf("/*file*/", cp)) > -1);
    }
    if (cp > 0)
    {
      // just append rest of state
      rewritten.append(state.substring(cp));
    }
    else
    {
      System.err.print("Ignoring incomplete Jmol state for PDB ids: ");
      rewritten = new StringBuilder(state);
      rewritten.append("; load append ");
      for (File id : oldFiles.keySet())
      {
        // add pdb files that should be present in the viewer
        StructureData filedat = oldFiles.get(id);
        rewritten.append(" \"").append(filedat.getFilePath()).append("\"");
      }
      rewritten.append(";");
    }

    if (rewritten.length() == 0)
    {
      return null;
    }
    final String history = "history = ";
    int historyIndex = rewritten.indexOf(history);
    if (historyIndex > -1)
    {
      /*
       * change "history = [true|false];" to "history = [1|0];"
       */
      historyIndex += history.length();
      String val = rewritten.substring(historyIndex, historyIndex + 5);
      if (val.startsWith("true"))
      {
        rewritten.replace(historyIndex, historyIndex + 4, "1");
      }
      else if (val.startsWith("false"))
      {
        rewritten.replace(historyIndex, historyIndex + 5, "0");
      }
    }

    try
    {
      File tmp = File.createTempFile("viewerSession", ".tmp");
      try (OutputStream os = new FileOutputStream(tmp))
      {
        InputStream is = new ByteArrayInputStream(
                rewritten.toString().getBytes());
        copyAll(is, os);
        return tmp.getAbsolutePath();
      }
    } catch (IOException e)
    {
      Console.error("Error restoring Jmol session: " + e.toString());
    }
    return null;
  }

  /**
   * Populates an XML model of the feature colour scheme for one feature type
   * 
   * @param featureType
   * @param fcol
   * @return
   */
  public static Colour marshalColour(String featureType,
          FeatureColourI fcol)
  {
    Colour col = new Colour();
    if (fcol.isSimpleColour())
    {
      col.setRGB(Format.getHexString(fcol.getColour()));
    }
    else
    {
      col.setRGB(Format.getHexString(fcol.getMaxColour()));
      col.setMin(fcol.getMin());
      col.setMax(fcol.getMax());
      col.setMinRGB(jalview.util.Format.getHexString(fcol.getMinColour()));
      col.setAutoScale(fcol.isAutoScaled());
      col.setThreshold(fcol.getThreshold());
      col.setColourByLabel(fcol.isColourByLabel());
      col.setThreshType(fcol.isAboveThreshold() ? ThresholdType.ABOVE
              : (fcol.isBelowThreshold() ? ThresholdType.BELOW
                      : ThresholdType.NONE));
      if (fcol.isColourByAttribute())
      {
        final String[] attName = fcol.getAttributeName();
        col.getAttributeName().add(attName[0]);
        if (attName.length > 1)
        {
          col.getAttributeName().add(attName[1]);
        }
      }
      Color noColour = fcol.getNoColour();
      if (noColour == null)
      {
        col.setNoValueColour(NoValueColour.NONE);
      }
      else if (noColour == fcol.getMaxColour())
      {
        col.setNoValueColour(NoValueColour.MAX);
      }
      else
      {
        col.setNoValueColour(NoValueColour.MIN);
      }
    }
    col.setName(featureType);
    return col;
  }

  /**
   * Populates an XML model of the feature filter(s) for one feature type
   * 
   * @param firstMatcher
   *          the first (or only) match condition)
   * @param filter
   *          remaining match conditions (if any)
   * @param and
   *          if true, conditions are and-ed, else or-ed
   */
  public static jalview.xml.binding.jalview.FeatureMatcherSet marshalFilter(
          FeatureMatcherI firstMatcher, Iterator<FeatureMatcherI> filters,
          boolean and)
  {
    jalview.xml.binding.jalview.FeatureMatcherSet result = new jalview.xml.binding.jalview.FeatureMatcherSet();

    if (filters.hasNext())
    {
      /*
       * compound matcher
       */
      CompoundMatcher compound = new CompoundMatcher();
      compound.setAnd(and);
      jalview.xml.binding.jalview.FeatureMatcherSet matcher1 = marshalFilter(
              firstMatcher, Collections.emptyIterator(), and);
      // compound.addMatcherSet(matcher1);
      compound.getMatcherSet().add(matcher1);
      FeatureMatcherI nextMatcher = filters.next();
      jalview.xml.binding.jalview.FeatureMatcherSet matcher2 = marshalFilter(
              nextMatcher, filters, and);
      // compound.addMatcherSet(matcher2);
      compound.getMatcherSet().add(matcher2);
      result.setCompoundMatcher(compound);
    }
    else
    {
      /*
       * single condition matcher
       */
      // MatchCondition matcherModel = new MatchCondition();
      jalview.xml.binding.jalview.FeatureMatcher matcherModel = new jalview.xml.binding.jalview.FeatureMatcher();
      matcherModel.setCondition(
              firstMatcher.getMatcher().getCondition().getStableName());
      matcherModel.setValue(firstMatcher.getMatcher().getPattern());
      if (firstMatcher.isByAttribute())
      {
        matcherModel.setBy(FilterBy.BY_ATTRIBUTE);
        // matcherModel.setAttributeName(firstMatcher.getAttribute());
        String[] attName = firstMatcher.getAttribute();
        matcherModel.getAttributeName().add(attName[0]); // attribute
        if (attName.length > 1)
        {
          matcherModel.getAttributeName().add(attName[1]); // sub-attribute
        }
      }
      else if (firstMatcher.isByLabel())
      {
        matcherModel.setBy(FilterBy.BY_LABEL);
      }
      else if (firstMatcher.isByScore())
      {
        matcherModel.setBy(FilterBy.BY_SCORE);
      }
      result.setMatchCondition(matcherModel);
    }

    return result;
  }

  /**
   * Loads one XML model of a feature filter to a Jalview object
   * 
   * @param featureType
   * @param matcherSetModel
   * @return
   */
  public static FeatureMatcherSetI parseFilter(String featureType,
          jalview.xml.binding.jalview.FeatureMatcherSet matcherSetModel)
  {
    FeatureMatcherSetI result = new FeatureMatcherSet();
    try
    {
      parseFilterConditions(result, matcherSetModel, true);
    } catch (IllegalStateException e)
    {
      // mixing AND and OR conditions perhaps
      System.err.println(
              String.format("Error reading filter conditions for '%s': %s",
                      featureType, e.getMessage()));
      // return as much as was parsed up to the error
    }

    return result;
  }

  /**
   * Adds feature match conditions to matcherSet as unmarshalled from XML
   * (possibly recursively for compound conditions)
   * 
   * @param matcherSet
   * @param matcherSetModel
   * @param and
   *          if true, multiple conditions are AND-ed, else they are OR-ed
   * @throws IllegalStateException
   *           if AND and OR conditions are mixed
   */
  protected static void parseFilterConditions(FeatureMatcherSetI matcherSet,
          jalview.xml.binding.jalview.FeatureMatcherSet matcherSetModel,
          boolean and)
  {
    jalview.xml.binding.jalview.FeatureMatcher mc = matcherSetModel
            .getMatchCondition();
    if (mc != null)
    {
      /*
       * single condition
       */
      FilterBy filterBy = mc.getBy();
      Condition cond = Condition.fromString(mc.getCondition());
      String pattern = mc.getValue();
      FeatureMatcherI matchCondition = null;
      if (filterBy == FilterBy.BY_LABEL)
      {
        matchCondition = FeatureMatcher.byLabel(cond, pattern);
      }
      else if (filterBy == FilterBy.BY_SCORE)
      {
        matchCondition = FeatureMatcher.byScore(cond, pattern);

      }
      else if (filterBy == FilterBy.BY_ATTRIBUTE)
      {
        final List<String> attributeName = mc.getAttributeName();
        String[] attNames = attributeName
                .toArray(new String[attributeName.size()]);
        matchCondition = FeatureMatcher.byAttribute(cond, pattern,
                attNames);
      }

      /*
       * note this throws IllegalStateException if AND-ing to a 
       * previously OR-ed compound condition, or vice versa
       */
      if (and)
      {
        matcherSet.and(matchCondition);
      }
      else
      {
        matcherSet.or(matchCondition);
      }
    }
    else
    {
      /*
       * compound condition
       */
      List<jalview.xml.binding.jalview.FeatureMatcherSet> matchers = matcherSetModel
              .getCompoundMatcher().getMatcherSet();
      boolean anded = matcherSetModel.getCompoundMatcher().isAnd();
      if (matchers.size() == 2)
      {
        parseFilterConditions(matcherSet, matchers.get(0), anded);
        parseFilterConditions(matcherSet, matchers.get(1), anded);
      }
      else
      {
        System.err.println("Malformed compound filter condition");
      }
    }
  }

  /**
   * Loads one XML model of a feature colour to a Jalview object
   * 
   * @param colourModel
   * @return
   */
  public static FeatureColourI parseColour(Colour colourModel)
  {
    FeatureColourI colour = null;

    if (colourModel.getMax() != null)
    {
      Color mincol = null;
      Color maxcol = null;
      Color noValueColour = null;

      try
      {
        mincol = new Color(Integer.parseInt(colourModel.getMinRGB(), 16));
        maxcol = new Color(Integer.parseInt(colourModel.getRGB(), 16));
      } catch (Exception e)
      {
        Console.warn("Couldn't parse out graduated feature color.", e);
      }

      NoValueColour noCol = colourModel.getNoValueColour();
      if (noCol == NoValueColour.MIN)
      {
        noValueColour = mincol;
      }
      else if (noCol == NoValueColour.MAX)
      {
        noValueColour = maxcol;
      }

      colour = new FeatureColour(maxcol, mincol, maxcol, noValueColour,
              safeFloat(colourModel.getMin()),
              safeFloat(colourModel.getMax()));
      final List<String> attributeName = colourModel.getAttributeName();
      String[] attributes = attributeName
              .toArray(new String[attributeName.size()]);
      if (attributes != null && attributes.length > 0)
      {
        colour.setAttributeName(attributes);
      }
      if (colourModel.isAutoScale() != null)
      {
        colour.setAutoScaled(colourModel.isAutoScale().booleanValue());
      }
      if (colourModel.isColourByLabel() != null)
      {
        colour.setColourByLabel(
                colourModel.isColourByLabel().booleanValue());
      }
      if (colourModel.getThreshold() != null)
      {
        colour.setThreshold(colourModel.getThreshold().floatValue());
      }
      ThresholdType ttyp = colourModel.getThreshType();
      if (ttyp == ThresholdType.ABOVE)
      {
        colour.setAboveThreshold(true);
      }
      else if (ttyp == ThresholdType.BELOW)
      {
        colour.setBelowThreshold(true);
      }
    }
    else
    {
      Color color = new Color(Integer.parseInt(colourModel.getRGB(), 16));
      colour = new FeatureColour(color);
    }

    return colour;
  }

  public static void setStateSavedUpToDate(boolean s)
  {
    Console.debug("Setting overall stateSavedUpToDate to " + s);
    stateSavedUpToDate = s;
  }

  public static boolean stateSavedUpToDate()
  {
    Console.debug("Returning overall stateSavedUpToDate value: "
            + stateSavedUpToDate);
    return stateSavedUpToDate;
  }

  public static boolean allSavedUpToDate()
  {
    if (stateSavedUpToDate()) // nothing happened since last project save
      return true;

    AlignFrame[] frames = Desktop.getAlignFrames();
    if (frames != null)
    {
      for (int i = 0; i < frames.length; i++)
      {
        if (frames[i] == null)
          continue;
        if (!frames[i].getViewport().savedUpToDate())
          return false; // at least one alignment is not individually saved
      }
    }
    return true;
  }

  // used for debugging and tests
  private static int debugDelaySave = 20;

  public static void setDebugDelaySave(int n)
  {
    debugDelaySave = n;
  }
}
