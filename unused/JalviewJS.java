package jalview.bin;

import jalview.analysis.AlignmentUtils;
import jalview.datamodel.AlignmentI;
import jalview.gui.AlignFrame;
import jalview.gui.SplitFrame;
import jalview.io.DataSourceType;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileLoader;
import jalview.io.IdentifyFile;
import jalview.structure.StructureSelectionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * 
 * early work -- deprecated
 * 
 * Entry point for Jalview as Javascript. Expects parameter names as for the
 * JalviewLite applet, formatted as for the Jalview application, for example
 * 
 * <pre>
 *   JalviewJS file /examples/uniref50.fa features /examples/exampleFeatures.txt \
 *       PDBFile "/examples/pdb1.txt seq1"
 * </pre>
 * 
 * Note that (unlike the applet) parameter names are case sensitive
 */
// TODO or format as file=/examples/uniref50.fa (etc)?
public class JalviewJS
{

  static
  {
    /**
     * @j2sNative
     * 
     *            thisApplet.__Info.args =
     *            ["file","examples/uniref50.fa","features",
     *            "examples/exampleFeatures.txt",
     *            "props","/Users/gmcarstairs/.jalview_properties"];
     */

  }

  private static final String PARAM_FILE = "file";

  private static final String PARAM_FILE2 = "file2";

  private static final String PARAM_TREE = "tree";

  private static final String PARAM_FEATURES = "features";

  private static final String PARAM_ANNOTATIONS = "annotations";

  private static final String PARAM_SHOW_ANNOTATION = "showAnnotation";

  private static final String PARAM_PDBFILE = "PDBFile";

  private static final String PARAM_PROPS = "props";

  private Map<String, String> params;

  private List<String> pdbFileParams;

  public static void main(String[] args) throws Exception
  {
    new JalviewJS().doMain(args);
  }

  /**
   * Parses parameters and shows the frame and any loaded panels
   * 
   * @throws FileFormatException
   */
  void doMain(String[] args) throws FileFormatException
  {
    loadParameters(args);
    if (getParameter(PARAM_FILE) == null)
    {
      usage();
    }
    else
    {
      showFrame();
    }
  }

  /**
   * Answers the value of the given runtime parameter, or null if not provided
   * 
   * @param paramName
   * @return
   */
  private String getParameter(String paramName)
  {
    return params.get(paramName);
  }

  /**
   * Prints a chastising, yet helpful, error message on syserr
   */
  private void usage()
  {
    System.err.println(
            "Usage: JalviewJS file <alignmentFile> [features <featuresFile>]");
    System.err.println("See documentation for full parameter list");
  }

  /**
   * Parses any supplied parameters. Note that (unlike for the applet),
   * parameter names are case sensitive.
   * 
   * @param args
   * 
   * @see http://www.jalview.org/examples/index.html#appletParameters
   */
  void loadParameters(String[] args)
  {
    ArgsParser parser = new ArgsParser(args);
    params = new HashMap<>();

    // TODO javascript-friendly source of properties
    Cache.loadProperties(parser.getValue(PARAM_PROPS));
    loadParameter(parser, PARAM_FILE);
    loadParameter(parser, PARAM_FILE2);
    loadParameter(parser, PARAM_TREE);
    loadParameter(parser, PARAM_FEATURES);
    loadParameter(parser, PARAM_ANNOTATIONS);
    loadParameter(parser, PARAM_SHOW_ANNOTATION);
    pdbFileParams = loadPdbParameters(parser);
  }

  /**
   * Reads one command line parameter value and saves it against the parameter
   * name. Note the saved value is null if the parameter is not present.
   * 
   * @param parser
   * @param param
   */
  protected void loadParameter(ArgsParser parser, String param)
  {
    params.put(param, parser.getValue(param));
  }

  /**
   * Reads parameter PDBFile, PDBFile1, PDFile2, ... and saves the value(s) (if
   * any)
   * 
   * @param parser
   * @return
   */
  List<String> loadPdbParameters(ArgsParser parser)
  {
    List<String> values = new ArrayList<>();
    String value = parser.getValue(PARAM_PDBFILE);
    if (value != null)
    {
      values.add(value);
    }
    int i = 1;
    while (true)
    {
      value = parser.getValue(PARAM_PDBFILE + String.valueOf(i));
      if (value != null)
      {
        values.add(value);
      }
      else
      {
        break;
      }
    }
    return values;
  }

  /**
   * Constructs and displays a JFrame containing an alignment panel (and any
   * additional panels depending on parameters supplied)
   * 
   * @throws FileFormatException
   */
  void showFrame() throws FileFormatException
  {
    JFrame frame = new JFrame(getParameter(PARAM_FILE));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    /*
     * construct an AlignFrame (optionally with features)
     */
    AlignFrame alignFrame = createAlignFrame(PARAM_FILE);
    loadFeatures(alignFrame, getParameter(PARAM_FEATURES));

    JInternalFrame internalFrame = alignFrame;

    /*
     * convert to SplitFrame if a valid file2 is supplied
     */
    AlignFrame alignFrame2 = createAlignFrame(PARAM_FILE2);
    if (alignFrame2 != null)
    {
      SplitFrame splitFrame = loadSplitFrame(alignFrame, alignFrame2);
      if (splitFrame != null)
      {
        internalFrame = splitFrame;
      }
    }

    /*
     * move AlignFrame (or SplitFrame) menu bar and content pane to our frame
     * TODO there may be a less obscure way to do this
     */
    frame.setContentPane(internalFrame.getContentPane());
    frame.setJMenuBar(internalFrame.getJMenuBar());

    // fudge so that dialogs can be opened with this frame as parent
    // todo JAL-3031 also override Desktop.addInternalFrame etc
    // Desktop.parent = frame.getContentPane();

    frame.pack();
    frame.setSize(AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
    frame.setVisible(true);
  }

  /**
   * Constructs a SplitFrame if cdna-protein mappings can be made between the
   * given alignment frames, else returns null. Any mappings made are registered
   * with StructureSelectionManager to enable broadcast to listeners.
   * 
   * @param alignFrame
   * @param alignFrame2
   * @return
   */
  protected SplitFrame loadSplitFrame(AlignFrame alignFrame,
          AlignFrame alignFrame2)
  {
    // code borrowed from AlignViewport.openLinkedAlignment
    AlignmentI al1 = alignFrame.getViewport().getAlignment();
    AlignmentI al2 = alignFrame2.getViewport().getAlignment();
    boolean al1Nuc = al1.isNucleotide();
    if (al1Nuc == al2.isNucleotide())
    {
      System.err.println("Can't make split frame as alignments are both "
              + (al1Nuc ? "nucleotide" : "protein"));
      return null;
    }
    AlignmentI protein = al1Nuc ? al2 : al1;
    AlignmentI cdna = al1Nuc ? al1 : al2;
    boolean mapped = AlignmentUtils.mapProteinAlignmentToCdna(protein,
            cdna);
    if (!mapped)
    {
      System.err.println("Can't make any mappings for split frame");
      return null;
    }

    /*
     * register sequence mappings
     */
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(null);
    ssm.registerMappings(protein.getCodonFrames());

    cdna.alignAs(protein);

    SplitFrame splitFrame = new SplitFrame(
            al1Nuc ? alignFrame : alignFrame2,
            al1Nuc ? alignFrame2 : alignFrame);

    return splitFrame;
  }

  /**
   * Loads on a features file if one was specified as a parameter
   * 
   * @param alignFrame
   * @param featureFile
   */
  protected void loadFeatures(AlignFrame alignFrame, String featureFile)
  {
    if (featureFile != null)
    {
      // todo extract helper for protocol resolution from JalviewLite
      DataSourceType sourceType = featureFile.startsWith("http")
              ? DataSourceType.URL
              : DataSourceType.RELATIVE_URL;
      alignFrame.parseFeaturesFile(featureFile, sourceType);
    }
  }

  /**
   * Constructs and returns the frame containing the alignment and its
   * annotations. Returns null if the specified parameter value is not set.
   * 
   * @param fileParam
   * 
   * @return
   * @throws FileFormatException
   */
  AlignFrame createAlignFrame(String fileParam) throws FileFormatException
  {
    AlignFrame af = null;
    String file = getParameter(fileParam);
    if (file != null)
    {
      DataSourceType protocol = file.startsWith("http") ? DataSourceType.URL
              : DataSourceType.RELATIVE_URL;
      // DataSourceType protocol = AppletFormatAdapter.checkProtocol(file);
      FileFormatI format = new IdentifyFile().identify(file, protocol);
      FileLoader fileLoader = new FileLoader(false);
      af = fileLoader.LoadFileWaitTillLoaded(file, protocol, format);
    }

    return af;
  }

}
