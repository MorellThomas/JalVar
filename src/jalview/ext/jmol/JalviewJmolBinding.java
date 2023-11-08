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
package jalview.ext.jmol;

import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAppConsoleInterface;
import org.jmol.api.JmolSelectionListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.c.CBK;
import org.jmol.viewer.Viewer;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.FeatureSettingsModelI;
import jalview.api.SequenceRenderer;
import jalview.bin.Console;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.AppJmol;
import jalview.gui.IProgressIndicator;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.structure.AtomSpec;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;
import jalview.ws.dbsources.Pdb;
import javajs.util.BS;

public abstract class JalviewJmolBinding extends AAStructureBindingModel
        implements JmolStatusListener, JmolSelectionListener,
        ComponentListener
{
  private String lastMessage;

  /*
   * when true, try to search the associated datamodel for sequences that are
   * associated with any unknown structures in the Jmol view.
   */
  private boolean associateNewStructs = false;

  private Vector<String> atomsPicked = new Vector<>();

  private String lastCommand;

  private boolean loadedInline;

  private StringBuffer resetLastRes = new StringBuffer();

  public Viewer jmolViewer;

  public JalviewJmolBinding(StructureSelectionManager ssm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs,
          DataSourceType protocol)
  {
    super(ssm, pdbentry, sequenceIs, protocol);
    setStructureCommands(new JmolCommands());
    /*
     * viewer = JmolViewer.allocateViewer(renderPanel, new SmarterJmolAdapter(),
     * "jalviewJmol", ap.av.applet .getDocumentBase(), ap.av.applet.getCodeBase(),
     * "", this);
     * 
     * jmolpopup = JmolPopup.newJmolPopup(viewer, true, "Jmol", true);
     */
  }

  public JalviewJmolBinding(StructureSelectionManager ssm,
          SequenceI[][] seqs, Viewer theViewer)
  {
    super(ssm, seqs);

    jmolViewer = theViewer;
    jmolViewer.setJmolStatusListener(this);
    jmolViewer.addSelectionListener(this);
    setStructureCommands(new JmolCommands());
  }

  /**
   * construct a title string for the viewer window based on the data jalview
   * knows about
   * 
   * @return
   */
  public String getViewerTitle()
  {
    return getViewerTitle("Jmol", true);
  }

  private String jmolScript(String script)
  {
    return jmolScript(script, false);
  }

  private String jmolScript(String script, boolean useScriptWait)
  {
    Console.debug(">>Jmol>> " + script);
    String s;
    if (useScriptWait)
    {
      s = jmolViewer.scriptWait(script);
    }
    else
    {
      s = jmolViewer.evalStringQuiet(script); // scriptWait(script); BH
    }
    Console.debug("<<Jmol<< " + s);

    return s;
  }

  @Override
  public List<String> executeCommand(StructureCommandI command,
          boolean getReply)
  {
    if (command == null)
    {
      return null;
    }
    String cmd = command.getCommand();
    jmolHistory(false);
    if (lastCommand == null || !lastCommand.equals(cmd))
    {
      jmolScript(cmd + "\n");
    }
    jmolHistory(true);
    lastCommand = cmd;
    return null;
  }

  public void createImage(String file, String type, int quality)
  {
    System.out.println("JMOL CREATE IMAGE");
  }

  @Override
  public String createImage(String fileName, String type,
          Object textOrBytes, int quality)
  {
    System.out.println("JMOL CREATE IMAGE");
    return null;
  }

  @Override
  public String eval(String strEval)
  {
    // System.out.println(strEval);
    // "# 'eval' is implemented only for the applet.";
    return null;
  }

  // End StructureListener
  // //////////////////////////

  @Override
  public float[][] functionXY(String functionName, int x, int y)
  {
    return null;
  }

  @Override
  public float[][][] functionXYZ(String functionName, int nx, int ny,
          int nz)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * map between index of model filename returned from getPdbFile and the first
   * index of models from this file in the viewer. Note - this is not trimmed -
   * use getPdbFile to get number of unique models.
   */
  private int _modelFileNameMap[];

  @Override
  public synchronized String[] getStructureFiles()
  {
    if (jmolViewer == null)
    {
      return new String[0];
    }

    if (modelFileNames == null)
    {
      int modelCount = jmolViewer.ms.mc;
      String filePath = null;
      List<String> mset = new ArrayList<>();
      for (int i = 0; i < modelCount; ++i)
      {
        /*
         * defensive check for null as getModelFileName can return null even when model
         * count ms.mc is > 0
         */
        filePath = jmolViewer.ms.getModelFileName(i);
        if (filePath != null && !mset.contains(filePath))
        {
          mset.add(filePath);
        }
      }
      if (!mset.isEmpty())
      {
        modelFileNames = mset.toArray(new String[mset.size()]);
      }
    }

    return modelFileNames;
  }

  /**
   * map from string to applet
   */
  @Override
  public Map<String, Object> getRegistryInfo()
  {
    // TODO Auto-generated method stub
    return null;
  }

  // ///////////////////////////////
  // JmolStatusListener

  public void handlePopupMenu(int x, int y)
  {
    // jmolpopup.show(x, y);
    // jmolpopup.jpiShow(x, y);
  }

  /**
   * Highlight zero, one or more atoms on the structure
   */
  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    if (atoms != null)
    {
      boolean useScriptWait = atoms.size() > 1;
      if (resetLastRes.length() > 0)
      {
        jmolScript(resetLastRes.toString(), useScriptWait);
        resetLastRes.setLength(0);
      }
      for (AtomSpec atom : atoms)
      {
        highlightAtom(atom.getAtomIndex(), atom.getPdbResNum(),
                atom.getChain(), atom.getPdbFile(), useScriptWait);
      }
      // Highlight distances between atoms with a 'measure' command - not yet
      // working
      // if (atoms.size() >= 2)
      // {
      // StringBuilder sb = new StringBuilder();
      // for (int a = 0; a < atoms.size(); a++)
      // {
      // AtomSpec speca = atoms.get(a);
      // String a_model = getModelIdForFile(speca.getPdbFile());
      // for (int b = a + 1; b < atoms.size(); b++)
      // {
      // AtomSpec specb = atoms.get(b);
      // String b_model = getModelIdForFile(speca.getPdbFile());
      // sb.append("measure ALL (" + speca.getAtomIndex() + " and */"
      // + a_model + ") (" + specb.getAtomIndex() + " and */"
      // + b_model + ");");
      // }
      // }
      // jmolHistory(false, useScriptWait);
      // jmolScript(sb.toString(), useScriptWait);
      // jmolHistory(true, useScriptWait);
      // }

    }

  }

  // jmol/ssm only
  public void highlightAtom(int atomIndex, int pdbResNum, String chain,
          String pdbfile, boolean useScriptWait)
  {
    String modelId = getModelIdForFile(pdbfile);
    if (modelId.isEmpty())
    {
      return;
    }

    jmolHistory(false, useScriptWait);

    StringBuilder selection = new StringBuilder(32);
    StringBuilder cmd = new StringBuilder(64);
    selection.append("select ").append(String.valueOf(pdbResNum));
    selection.append(":");
    if (!chain.equals(" "))
    {
      selection.append(chain);
    }
    selection.append(" /").append(modelId);

    cmd.append(selection).append(";wireframe 100;").append(selection)
            .append(" and not hetero;").append("spacefill 200;select none");

    resetLastRes.append(selection).append(";wireframe 0;").append(selection)
            .append(" and not hetero; spacefill 0;");

    jmolScript(cmd.toString(), useScriptWait);
    jmolHistory(true, useScriptWait);
  }

  private boolean debug = true;

  private void jmolHistory(boolean enable)
  {
    jmolHistory(enable, false);
  }

  private void jmolHistory(boolean enable, boolean useScriptWait)
  {
    jmolScript("History " + ((debug || enable) ? "on" : "off"),
            useScriptWait);
  }

  public void loadInline(String string)
  {
    loadedInline = true;
    // TODO: re JAL-623
    // viewer.loadInline(strModel, isAppend);
    // could do this:
    // construct fake fullPathName and fileName so we can identify the file
    // later.
    // Then, construct pass a reader for the string to Jmol.
    // ((org.jmol.Viewer.Viewer) viewer).loadModelFromFile(fullPathName,
    // fileName, null, reader, false, null, null, 0);
    jmolViewer.openStringInline(string);
  }

  protected void mouseOverStructure(int atomIndex, final String strInfo)
  {
    int pdbResNum;
    int alocsep = strInfo.indexOf("^");
    int mdlSep = strInfo.indexOf("/");
    int chainSeparator = strInfo.indexOf(":"), chainSeparator1 = -1;

    if (chainSeparator == -1)
    {
      chainSeparator = strInfo.indexOf(".");
      if (mdlSep > -1 && mdlSep < chainSeparator)
      {
        chainSeparator1 = chainSeparator;
        chainSeparator = mdlSep;
      }
    }
    // handle insertion codes
    if (alocsep != -1)
    {
      pdbResNum = Integer.parseInt(
              strInfo.substring(strInfo.indexOf("]") + 1, alocsep));

    }
    else
    {
      pdbResNum = Integer.parseInt(
              strInfo.substring(strInfo.indexOf("]") + 1, chainSeparator));
    }
    String chainId;

    if (strInfo.indexOf(":") > -1)
    {
      chainId = strInfo.substring(strInfo.indexOf(":") + 1,
              strInfo.indexOf("."));
    }
    else
    {
      chainId = " ";
    }

    String pdbfilename = modelFileNames[0]; // default is first model
    if (mdlSep > -1)
    {
      if (chainSeparator1 == -1)
      {
        chainSeparator1 = strInfo.indexOf(".", mdlSep);
      }
      String mdlId = (chainSeparator1 > -1)
              ? strInfo.substring(mdlSep + 1, chainSeparator1)
              : strInfo.substring(mdlSep + 1);
      try
      {
        // recover PDB filename for the model hovered over.
        int mnumber = Integer.valueOf(mdlId).intValue() - 1;
        if (_modelFileNameMap != null)
        {
          int _mp = _modelFileNameMap.length - 1;

          while (mnumber < _modelFileNameMap[_mp])
          {
            _mp--;
          }
          pdbfilename = modelFileNames[_mp];
        }
        else
        {
          if (mnumber >= 0 && mnumber < modelFileNames.length)
          {
            pdbfilename = modelFileNames[mnumber];
          }

          if (pdbfilename == null)
          {
            pdbfilename = new File(jmolViewer.ms.getModelFileName(mnumber))
                    .getAbsolutePath();
          }
        }
      } catch (Exception e)
      {
      }
    }

    /*
     * highlight position on alignment(s); if some text is returned, show this as a
     * second line on the structure hover tooltip
     */
    String label = getSsm().mouseOverStructure(pdbResNum, chainId,
            pdbfilename);
    if (label != null)
    {
      // change comma to pipe separator (newline token for Jmol)
      label = label.replace(',', '|');
      StringTokenizer toks = new StringTokenizer(strInfo, " ");
      StringBuilder sb = new StringBuilder();
      sb.append("select ").append(String.valueOf(pdbResNum)).append(":")
              .append(chainId).append("/1");
      sb.append(";set hoverLabel \"").append(toks.nextToken()).append(" ")
              .append(toks.nextToken());
      sb.append("|").append(label).append("\"");
      executeCommand(new StructureCommand(sb.toString()), false);
    }
  }

  public void notifyAtomHovered(int atomIndex, String strInfo, String data)
  {
    if (strInfo.equals(lastMessage))
    {
      return;
    }
    lastMessage = strInfo;
    if (data != null)
    {
      System.err.println("Ignoring additional hover info: " + data
              + " (other info: '" + strInfo + "' pos " + atomIndex + ")");
    }
    mouseOverStructure(atomIndex, strInfo);
  }

  /*
   * { if (history != null && strStatus != null &&
   * !strStatus.equals("Script completed")) { history.append("\n" + strStatus); }
   * }
   */

  public void notifyAtomPicked(int atomIndex, String strInfo,
          String strData)
  {
    /**
     * this implements the toggle label behaviour copied from the original
     * structure viewer, mc_view
     */
    if (strData != null)
    {
      System.err.println("Ignoring additional pick data string " + strData);
    }
    int chainSeparator = strInfo.indexOf(":");
    int p = 0;
    if (chainSeparator == -1)
    {
      chainSeparator = strInfo.indexOf(".");
    }

    String picked = strInfo.substring(strInfo.indexOf("]") + 1,
            chainSeparator);
    String mdlString = "";
    if ((p = strInfo.indexOf(":")) > -1)
    {
      picked += strInfo.substring(p, strInfo.indexOf("."));
    }

    if ((p = strInfo.indexOf("/")) > -1)
    {
      mdlString += strInfo.substring(p, strInfo.indexOf(" #"));
    }
    picked = "((" + picked + ".CA" + mdlString + ")|(" + picked + ".P"
            + mdlString + "))";
    jmolHistory(false);

    if (!atomsPicked.contains(picked))
    {
      jmolScript("select " + picked + ";label %n %r:%c");
      atomsPicked.addElement(picked);
    }
    else
    {
      jmolViewer.evalString("select " + picked + ";label off");
      atomsPicked.removeElement(picked);
    }
    jmolHistory(true);
    // TODO: in application this happens
    //
    // if (scriptWindow != null)
    // {
    // scriptWindow.sendConsoleMessage(strInfo);
    // scriptWindow.sendConsoleMessage("\n");
    // }

  }

  @Override
  public void notifyCallback(CBK type, Object[] data)
  {
    /*
     * ensure processed in AWT thread to avoid risk of deadlocks
     */
    SwingUtilities.invokeLater(new Runnable()
    {

      @Override
      public void run()
      {
        processCallback(type, data);
      }
    });
  }

  /**
   * Processes one callback notification from Jmol
   * 
   * @param type
   * @param data
   */
  protected void processCallback(CBK type, Object[] data)
  {
    try
    {
      switch (type)
      {
      case LOADSTRUCT:
        notifyFileLoaded((String) data[1], (String) data[2],
                (String) data[3], (String) data[4],
                ((Integer) data[5]).intValue());

        break;
      case PICK:
        notifyAtomPicked(((Integer) data[2]).intValue(), (String) data[1],
                (String) data[0]);
        // also highlight in alignment
        // deliberate fall through
      case HOVER:
        notifyAtomHovered(((Integer) data[2]).intValue(), (String) data[1],
                (String) data[0]);
        break;
      case SCRIPT:
        notifyScriptTermination((String) data[2],
                ((Integer) data[3]).intValue());
        break;
      case ECHO:
        sendConsoleEcho((String) data[1]);
        break;
      case MESSAGE:
        sendConsoleMessage(
                (data == null) ? ((String) null) : (String) data[1]);
        break;
      case ERROR:
        // System.err.println("Ignoring error callback.");
        break;
      case SYNC:
      case RESIZE:
        refreshGUI();
        break;
      case MEASURE:

      case CLICK:
      default:
        System.err.println(
                "Unhandled callback " + type + " " + data[1].toString());
        break;
      }
    } catch (Exception e)
    {
      System.err.println("Squashed Jmol callback handler error:");
      e.printStackTrace();
    }
  }

  @Override
  public boolean notifyEnabled(CBK callbackPick)
  {
    switch (callbackPick)
    {
    case ECHO:
    case LOADSTRUCT:
    case MEASURE:
    case MESSAGE:
    case PICK:
    case SCRIPT:
    case HOVER:
    case ERROR:
      return true;
    default:
      return false;
    }
  }

  // incremented every time a load notification is successfully handled -
  // lightweight mechanism for other threads to detect when they can start
  // referrring to new structures.
  private long loadNotifiesHandled = 0;

  public long getLoadNotifiesHandled()
  {
    return loadNotifiesHandled;
  }

  public void notifyFileLoaded(String fullPathName, String fileName2,
          String modelName, String errorMsg, int modelParts)
  {
    if (errorMsg != null)
    {
      fileLoadingError = errorMsg;
      refreshGUI();
      return;
    }
    // TODO: deal sensibly with models loaded inLine:
    // modelName will be null, as will fullPathName.

    // the rest of this routine ignores the arguments, and simply interrogates
    // the Jmol view to find out what structures it contains, and adds them to
    // the structure selection manager.
    fileLoadingError = null;
    String[] oldmodels = modelFileNames;
    modelFileNames = null;
    boolean notifyLoaded = false;
    String[] modelfilenames = getStructureFiles();
    if (modelfilenames == null)
    {
      // Jmol is still loading files!
      return;
    }
    // first check if we've lost any structures
    if (oldmodels != null && oldmodels.length > 0)
    {
      int oldm = 0;
      for (int i = 0; i < oldmodels.length; i++)
      {
        for (int n = 0; n < modelfilenames.length; n++)
        {
          if (modelfilenames[n] == oldmodels[i])
          {
            oldmodels[i] = null;
            break;
          }
        }
        if (oldmodels[i] != null)
        {
          oldm++;
        }
      }
      if (oldm > 0)
      {
        String[] oldmfn = new String[oldm];
        oldm = 0;
        for (int i = 0; i < oldmodels.length; i++)
        {
          if (oldmodels[i] != null)
          {
            oldmfn[oldm++] = oldmodels[i];
          }
        }
        // deregister the Jmol instance for these structures - we'll add
        // ourselves again at the end for the current structure set.
        getSsm().removeStructureViewerListener(this, oldmfn);
      }
    }
    refreshPdbEntries();
    for (int modelnum = 0; modelnum < modelfilenames.length; modelnum++)
    {
      String fileName = modelfilenames[modelnum];
      boolean foundEntry = false;
      StructureFile pdb = null;
      String pdbfile = null;
      // model was probably loaded inline - so check the pdb file hashcode
      if (loadedInline)
      {
        // calculate essential attributes for the pdb data imported inline.
        // prolly need to resolve modelnumber properly - for now just use our
        // 'best guess'
        pdbfile = jmolViewer.getData(
                "" + (1 + _modelFileNameMap[modelnum]) + ".0", "PDB");
      }
      // search pdbentries and sequences to find correct pdbentry for this
      // model
      for (int pe = 0; pe < getPdbCount(); pe++)
      {
        boolean matches = false;
        addSequence(pe, getSequence()[pe]);
        if (fileName == null)
        {
          if (false)
          // see JAL-623 - need method of matching pasted data up
          {
            pdb = getSsm().setMapping(getSequence()[pe], getChains()[pe],
                    pdbfile, DataSourceType.PASTE, getIProgressIndicator());
            getPdbEntry(modelnum).setFile("INLINE" + pdb.getId());
            matches = true;
            foundEntry = true;
          }
        }
        else
        {
          File fl = new File(getPdbEntry(pe).getFile());
          matches = fl.equals(new File(fileName));
          if (matches)
          {
            foundEntry = true;
            // TODO: Jmol can in principle retrieve from CLASSLOADER but
            // this
            // needs
            // to be tested. See mantis bug
            // https://mantis.lifesci.dundee.ac.uk/view.php?id=36605
            DataSourceType protocol = DataSourceType.URL;
            try
            {
              if (fl.exists())
              {
                protocol = DataSourceType.FILE;
              }
            } catch (Exception e)
            {
            } catch (Error e)
            {
            }
            // Explicitly map to the filename used by Jmol ;
            pdb = getSsm().setMapping(getSequence()[pe], getChains()[pe],
                    fileName, protocol, getIProgressIndicator());
            // pdbentry[pe].getFile(), protocol);

          }
        }
        if (matches)
        {
          stashFoundChains(pdb, fileName);
          notifyLoaded = true;
        }
      }

      if (!foundEntry && associateNewStructs)
      {
        // this is a foreign pdb file that jalview doesn't know about - add
        // it to the dataset and try to find a home - either on a matching
        // sequence or as a new sequence.
        String pdbcontent = jmolViewer.getData("/" + (modelnum + 1) + ".1",
                "PDB");
        // parse pdb file into a chain, etc.
        // locate best match for pdb in associated views and add mapping to
        // ssm
        // if properly registered then
        notifyLoaded = true;

      }
    }
    // FILE LOADED OK
    // so finally, update the jmol bits and pieces
    // if (jmolpopup != null)
    // {
    // // potential for deadlock here:
    // // jmolpopup.updateComputedMenus();
    // }
    if (!isLoadingFromArchive())
    {
      jmolScript(
              "model *; select backbone;restrict;cartoon;wireframe off;spacefill off");
    }
    // register ourselves as a listener and notify the gui that it needs to
    // update itself.
    getSsm().addStructureViewerListener(this);
    if (notifyLoaded)
    {
      FeatureRenderer fr = getFeatureRenderer(null);
      if (fr != null)
      {
        FeatureSettingsModelI colours = new Pdb().getFeatureColourScheme();
        ((AppJmol) getViewer()).getAlignmentPanel().av
                .applyFeaturesStyle(colours);
      }
      refreshGUI();
      loadNotifiesHandled++;
    }
    setLoadingFromArchive(false);
  }

  protected IProgressIndicator getIProgressIndicator()
  {
    return null;
  }

  public void notifyNewPickingModeMeasurement(int iatom, String strMeasure)
  {
    notifyAtomPicked(iatom, strMeasure, null);
  }

  public abstract void notifyScriptTermination(String strStatus,
          int msWalltime);

  /**
   * display a message echoed from the jmol viewer
   * 
   * @param strEcho
   */
  public abstract void sendConsoleEcho(String strEcho); /*
                                                         * { showConsole(true);
                                                         * 
                                                         * history.append("\n" + strEcho); }
                                                         */

  // /End JmolStatusListener
  // /////////////////////////////

  /**
   * @param strStatus
   *          status message - usually the response received after a script
   *          executed
   */
  public abstract void sendConsoleMessage(String strStatus);

  @Override
  public void setCallbackFunction(String callbackType,
          String callbackFunction)
  {
    System.err.println("Ignoring set-callback request to associate "
            + callbackType + " with function " + callbackFunction);

  }

  public void showHelp()
  {
    showUrl("http://wiki.jmol.org"
    // BH 2018 "http://jmol.sourceforge.net/docs/JmolUserGuide/"
            , "jmolHelp");
  }

  /**
   * open the URL somehow
   * 
   * @param target
   */
  public abstract void showUrl(String url, String target);

  /**
   * called to show or hide the associated console window container.
   * 
   * @param show
   */
  public abstract void showConsole(boolean show);

  public static Viewer getJmolData(JmolParser jmolParser)
  {
    return (Viewer) JmolViewer.allocateViewer(null, null, null, null, null,
            "-x -o -n", jmolParser);
  }

  /**
   * 
   * 
   * 
   * @param renderPanel
   * @param jmolfileio
   *          - when true will initialise jmol's file IO system (should be false
   *          in applet context)
   * @param htmlName
   * @param documentBase
   * @param codeBase
   * @param commandOptions
   */
  public void allocateViewer(Container renderPanel, boolean jmolfileio,
          String htmlName, URL documentBase, URL codeBase,
          String commandOptions)
  {
    allocateViewer(renderPanel, jmolfileio, htmlName, documentBase,
            codeBase, commandOptions, null, null);
  }

  /**
   * 
   * @param renderPanel
   * @param jmolfileio
   *          - when true will initialise jmol's file IO system (should be false
   *          in applet context)
   * @param htmlName
   * @param documentBase
   * @param codeBase
   * @param commandOptions
   * @param consolePanel
   *          - panel to contain Jmol console
   * @param buttonsToShow
   *          - buttons to show on the console, in order
   */
  public void allocateViewer(Container renderPanel, boolean jmolfileio,
          String htmlName, URL documentBase, URL codeBase,
          String commandOptions, final Container consolePanel,
          String buttonsToShow)
  {

    System.err.println("Allocating Jmol Viewer: " + commandOptions);

    if (commandOptions == null)
    {
      commandOptions = "";
    }
    jmolViewer = (Viewer) JmolViewer.allocateViewer(renderPanel,
            (jmolfileio ? new SmarterJmolAdapter() : null),
            htmlName + ((Object) this).toString(), documentBase, codeBase,
            commandOptions, this);

    jmolViewer.setJmolStatusListener(this); // extends JmolCallbackListener

    try
    {
      console = createJmolConsole(consolePanel, buttonsToShow);
    } catch (Throwable e)
    {
      System.err.println("Could not create Jmol application console. "
              + e.getMessage());
      e.printStackTrace();
    }
    if (consolePanel != null)
    {
      consolePanel.addComponentListener(this);

    }

  }

  protected abstract JmolAppConsoleInterface createJmolConsole(
          Container consolePanel, String buttonsToShow);

  // BH 2018 -- Jmol console is not working due to problems with styled
  // documents.

  protected org.jmol.api.JmolAppConsoleInterface console = null;

  @Override
  public int[] resizeInnerPanel(String data)
  {
    // Jalview doesn't honour resize panel requests
    return null;
  }

  /**
   * 
   */
  protected void closeConsole()
  {
    if (console != null)
    {
      try
      {
        console.setVisible(false);
      } catch (Error e)
      {
      } catch (Exception x)
      {
      }
      ;
      console = null;
    }
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentMoved(ComponentEvent e)
  {
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentResized(ComponentEvent e)
  {
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentShown(ComponentEvent e)
  {
    showConsole(true);
  }

  /**
   * ComponentListener method
   */
  @Override
  public void componentHidden(ComponentEvent e)
  {
    showConsole(false);
  }

  @Override
  protected String getModelIdForFile(String pdbFile)
  {
    if (modelFileNames == null)
    {
      return "";
    }
    for (int i = 0; i < modelFileNames.length; i++)
    {
      if (modelFileNames[i].equalsIgnoreCase(pdbFile))
      {
        return String.valueOf(i + 1);
      }
    }
    return "";
  }

  @Override
  protected ViewerType getViewerType()
  {
    return ViewerType.JMOL;
  }

  @Override
  protected String getModelId(int pdbfnum, String file)
  {
    return String.valueOf(pdbfnum + 1);
  }

  /**
   * Returns ".spt" - the Jmol session file extension
   * 
   * @return
   * @see https://chemapps.stolaf.edu/jmol/docs/#writemodel
   */
  @Override
  public String getSessionFileExtension()
  {
    return ".spt";
  }

  @Override
  public void selectionChanged(BS arg0)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public SequenceRenderer getSequenceRenderer(AlignmentViewPanel avp)
  {
    return new jalview.gui.SequenceRenderer(avp.getAlignViewport());
  }

  @Override
  public String getHelpURL()
  {
    return "http://wiki.jmol.org"; // BH 2018
  }
}
