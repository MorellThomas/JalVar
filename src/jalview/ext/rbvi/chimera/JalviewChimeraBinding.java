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
package jalview.ext.rbvi.chimera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ext.edu.ucsf.rbvi.strucviz2.ChimeraManager;
import ext.edu.ucsf.rbvi.strucviz2.ChimeraModel;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager;
import ext.edu.ucsf.rbvi.strucviz2.StructureManager.ModelType;
import jalview.api.AlignmentViewPanel;
import jalview.bin.Console;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.StructureViewer.ViewerType;
import jalview.httpserver.AbstractRequestHandler;
import jalview.io.DataSourceType;
import jalview.structure.AtomSpec;
import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommand;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;

public abstract class JalviewChimeraBinding extends AAStructureBindingModel
{
  public static final String CHIMERA_SESSION_EXTENSION = ".py";

  public static final String CHIMERA_FEATURE_GROUP = "Chimera";

  /*
   * Object through which we talk to Chimera
   */
  private ChimeraManager chimeraManager;

  /*
   * Object which listens to Chimera notifications
   */
  private AbstractRequestHandler chimeraListener;

  /*
   * Map of ChimeraModel objects keyed by PDB full local file name
   */
  protected Map<String, List<ChimeraModel>> chimeraMaps = new LinkedHashMap<>();

  String lastHighlightCommand;

  /**
   * Returns a model of the structure positions described by the Chimera format
   * atomspec
   * 
   * @param atomSpec
   * @return
   */
  protected AtomSpec parseAtomSpec(String atomSpec)
  {
    return AtomSpec.fromChimeraAtomspec(atomSpec);
  }

  /**
   * Open a PDB structure file in Chimera and set up mappings from Jalview.
   * 
   * We check if the PDB model id is already loaded in Chimera, if so don't
   * reopen it. This is the case if Chimera has opened a saved session file.
   * 
   * @param pe
   * @return
   */
  public boolean openFile(PDBEntry pe)
  {
    String file = pe.getFile();
    try
    {
      List<ChimeraModel> modelsToMap = new ArrayList<>();
      List<ChimeraModel> oldList = chimeraManager.getModelList();
      boolean alreadyOpen = false;

      /*
       * If Chimera already has this model, don't reopen it, but do remap it.
       */
      for (ChimeraModel open : oldList)
      {
        if (open.getModelName().equals(pe.getId()))
        {
          alreadyOpen = true;
          modelsToMap.add(open);
        }
      }

      /*
       * If Chimera doesn't yet have this model, ask it to open it, and retrieve
       * the model name(s) added by Chimera.
       */
      if (!alreadyOpen)
      {
        chimeraManager.openModel(file, pe.getId(), ModelType.PDB_MODEL);
        addChimeraModel(pe, modelsToMap);
      }

      chimeraMaps.put(file, modelsToMap);

      if (getSsm() != null)
      {
        getSsm().addStructureViewerListener(this);
      }
      return true;
    } catch (Exception q)
    {
      log("Exception when trying to open model " + file + "\n"
              + q.toString());
      q.printStackTrace();
    }
    return false;
  }

  /**
   * Adds the ChimeraModel corresponding to the given PDBEntry, based on model
   * name matching PDB id
   * 
   * @param pe
   * @param modelsToMap
   */
  protected void addChimeraModel(PDBEntry pe,
          List<ChimeraModel> modelsToMap)
  {
    /*
     * Chimera: query for actual models and find the one with
     * matching model name - already set in viewer.openModel()
     */
    List<ChimeraModel> newList = chimeraManager.getModelList();
    // JAL-1728 newList.removeAll(oldList) does not work
    for (ChimeraModel cm : newList)
    {
      if (cm.getModelName().equals(pe.getId()))
      {
        modelsToMap.add(cm);
      }
    }
  }

  /**
   * Constructor
   * 
   * @param ssm
   * @param pdbentry
   * @param sequenceIs
   * @param protocol
   */
  public JalviewChimeraBinding(StructureSelectionManager ssm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs,
          DataSourceType protocol)
  {
    super(ssm, pdbentry, sequenceIs, protocol);
    boolean chimeraX = ViewerType.CHIMERAX.equals(getViewerType());
    chimeraManager = chimeraX
            ? new ChimeraXManager(new StructureManager(true))
            : new ChimeraManager(new StructureManager(true));
    setStructureCommands(
            chimeraX ? new ChimeraXCommands() : new ChimeraCommands());
  }

  @Override
  protected ViewerType getViewerType()
  {
    return ViewerType.CHIMERA;
  }

  /**
   * Start a dedicated HttpServer to listen for Chimera notifications, and tell
   * it to start listening
   */
  public void startChimeraListener()
  {
    try
    {
      chimeraListener = new ChimeraListener(this);
      startListening(chimeraListener.getUri());
    } catch (BindException e)
    {
      System.err.println(
              "Failed to start Chimera listener: " + e.getMessage());
    }
  }

  /**
   * Close down the Jalview viewer and listener, and (optionally) the associated
   * Chimera window.
   */
  @Override
  public void closeViewer(boolean closeChimera)
  {
    super.closeViewer(closeChimera);
    if (this.chimeraListener != null)
    {
      chimeraListener.shutdown();
      chimeraListener = null;
    }

    /*
     * the following call is added to avoid a stack trace error in Chimera
     * after "stop really" is sent; Chimera > 1.14 will not need it; see also 
     * http://plato.cgl.ucsf.edu/trac/chimera/ticket/17597
     */
    if (closeChimera && (getViewerType() == ViewerType.CHIMERA))
    {
      chimeraManager.getChimeraProcess().destroy();
    }

    chimeraManager.clearOnChimeraExit();
    chimeraManager = null;
  }

  /**
   * Helper method to construct model spec in Chimera format:
   * <ul>
   * <li>#0 (#1 etc) for a PDB file with no sub-models</li>
   * <li>#0.1 (#1.1 etc) for a PDB file with sub-models</li>
   * <ul>
   * Note for now we only ever choose the first of multiple models. This
   * corresponds to the hard-coded Jmol equivalent (compare {1.1}). Refactor in
   * future if there is a need to select specific sub-models.
   * 
   * @param pdbfnum
   * @return
   */
  protected String getModelSpec(int pdbfnum)
  {
    if (pdbfnum < 0 || pdbfnum >= getPdbCount())
    {
      return "#" + pdbfnum; // temp hack for ChimeraX
    }

    /*
     * For now, the test for having sub-models is whether multiple Chimera
     * models are mapped for the PDB file; the models are returned as a response
     * to the Chimera command 'list models type molecule', see
     * ChimeraManager.getModelList().
     */
    List<ChimeraModel> maps = chimeraMaps.get(getStructureFiles()[pdbfnum]);
    boolean hasSubModels = maps != null && maps.size() > 1;
    return "#" + String.valueOf(pdbfnum) + (hasSubModels ? ".1" : "");
  }

  /**
   * Launch Chimera, unless an instance linked to this object is already
   * running. Returns true if Chimera is successfully launched, or already
   * running, else false.
   * 
   * @return
   */
  public boolean launchChimera()
  {
    if (chimeraManager.isChimeraLaunched())
    {
      return true;
    }

    boolean launched = chimeraManager.launchChimera(getChimeraPaths());
    if (launched)
    {
      startExternalViewerMonitor(chimeraManager.getChimeraProcess());
    }
    else
    {
      log("Failed to launch Chimera!");
    }
    return launched;
  }

  /**
   * Returns a list of candidate paths to the Chimera program executable
   * 
   * @return
   */
  protected List<String> getChimeraPaths()
  {
    return StructureManager.getChimeraPaths(false);
  }

  /**
   * Answers true if the Chimera process is still running, false if ended or not
   * started.
   * 
   * @return
   */
  @Override
  public boolean isViewerRunning()
  {
    return chimeraManager != null && chimeraManager.isChimeraLaunched();
  }

  /**
   * Send a command to Chimera, and optionally log and return any responses.
   * 
   * @param command
   * @param getResponse
   */
  @Override
  public List<String> executeCommand(final StructureCommandI command,
          boolean getResponse)
  {
    if (chimeraManager == null || command == null)
    {
      // ? thread running after viewer shut down
      return null;
    }
    List<String> reply = null;
    // trim command or it may never find a match in the replyLog!!
    String cmd = command.getCommand().trim();
    List<String> lastReply = chimeraManager.sendChimeraCommand(cmd,
            getResponse);
    if (getResponse)
    {
      reply = lastReply;
      if (Console.isDebugEnabled())
      {
        Console.debug(
                "Response from command ('" + cmd + "') was:\n" + lastReply);
      }
    }
    else
    {
      if (Console.isDebugEnabled())
      {
        Console.debug("Command executed: " + cmd);
      }
    }

    return reply;
  }

  @Override
  public synchronized String[] getStructureFiles()
  {
    if (chimeraManager == null)
    {
      return new String[0];
    }

    return chimeraMaps.keySet()
            .toArray(modelFileNames = new String[chimeraMaps.size()]);
  }

  /**
   * Construct and send a command to highlight zero, one or more atoms. We do
   * this by sending an "rlabel" command to show the residue label at that
   * position.
   */
  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    if (atoms == null || atoms.size() == 0)
    {
      return;
    }

    boolean forChimeraX = chimeraManager.isChimeraX();
    StringBuilder cmd = new StringBuilder(128);
    boolean first = true;
    boolean found = false;

    for (AtomSpec atom : atoms)
    {
      int pdbResNum = atom.getPdbResNum();
      String chain = atom.getChain();
      String pdbfile = atom.getPdbFile();
      List<ChimeraModel> cms = chimeraMaps.get(pdbfile);
      if (cms != null && !cms.isEmpty())
      {
        if (first)
        {
          cmd.append(forChimeraX ? "label #" : "rlabel #");
        }
        else
        {
          cmd.append(",");
        }
        first = false;
        if (forChimeraX)
        {
          cmd.append(cms.get(0).getModelNumber()).append("/").append(chain)
                  .append(":").append(pdbResNum);
        }
        else
        {
          cmd.append(cms.get(0).getModelNumber()).append(":")
                  .append(pdbResNum);
          if (!chain.equals(" ") && !forChimeraX)
          {
            cmd.append(".").append(chain);
          }
        }
        found = true;
      }
    }
    String command = cmd.toString();

    /*
     * avoid repeated commands for the same residue
     */
    if (command.equals(lastHighlightCommand))
    {
      return;
    }
    if (!found)
    {
      // not a valid residue label command, so clear
      cmd.setLength(0);
    }
    /*
     * prepend with command
     * to unshow the label for the previous residue
     */
    if (lastHighlightCommand != null)
    {
      cmd.insert(0, ";");
      cmd.insert(0, lastHighlightCommand);
      cmd.insert(0, "~");

    }
    if (cmd.length() > 0)
    {
      executeCommand(true, null, new StructureCommand(cmd.toString()));
    }

    if (found)
    {
      this.lastHighlightCommand = command;
    }
  }

  /**
   * Query Chimera for its current selection, and highlight it on the alignment
   */
  public void highlightChimeraSelection()
  {
    /*
     * Ask Chimera for its current selection
     */
    StructureCommandI command = getCommandGenerator().getSelectedResidues();

    Runnable action = new Runnable()
    {
      @Override
      public void run()
      {
        List<String> chimeraReply = executeCommand(command, true);

        List<String> selectedResidues = new ArrayList<>();
        if (chimeraReply != null)
        {
          /*
           * expect 0, 1 or more lines of the format either
           * Chimera:
           * residue id #0:43.A type GLY
           * ChimeraX:
           * residue id /A:89 name THR index 88
           * We are only interested in the atomspec (third token of the reply)
           */
          for (String inputLine : chimeraReply)
          {
            String[] inputLineParts = inputLine.split("\\s+");
            if (inputLineParts.length >= 5)
            {
              selectedResidues.add(inputLineParts[2]);
            }
          }
        }

        /*
         * Parse model number, residue and chain for each selected position,
         * formatted as #0:123.A or #1.2:87.B (#model.submodel:residue.chain)
         */
        List<AtomSpec> atomSpecs = convertStructureResiduesToAlignment(
                selectedResidues);

        /*
         * Broadcast the selection (which may be empty, if the user just cleared all
         * selections)
         */
        getSsm().mouseOverStructure(atomSpecs);

      }
    };
    new Thread(action).start();
  }

  /**
   * Converts a list of Chimera(X) atomspecs to a list of AtomSpec representing
   * the corresponding residues (if any) in Jalview
   * 
   * @param structureSelection
   * @return
   */
  protected List<AtomSpec> convertStructureResiduesToAlignment(
          List<String> structureSelection)
  {
    List<AtomSpec> atomSpecs = new ArrayList<>();
    for (String atomSpec : structureSelection)
    {
      try
      {
        AtomSpec spec = parseAtomSpec(atomSpec);
        String pdbfilename = getPdbFileForModel(spec.getModelNumber());
        spec.setPdbFile(pdbfilename);
        atomSpecs.add(spec);
      } catch (IllegalArgumentException e)
      {
        Console.error("Failed to parse atomspec: " + atomSpec);
      }
    }
    return atomSpecs;
  }

  /**
   * @param modelId
   * @return
   */
  protected String getPdbFileForModel(int modelId)
  {
    /*
     * Work out the pdbfilename from the model number
     */
    String pdbfilename = modelFileNames[0];
    findfileloop: for (String pdbfile : this.chimeraMaps.keySet())
    {
      for (ChimeraModel cm : chimeraMaps.get(pdbfile))
      {
        if (cm.getModelNumber() == modelId)
        {
          pdbfilename = pdbfile;
          break findfileloop;
        }
      }
    }
    return pdbfilename;
  }

  private void log(String message)
  {
    System.err.println("## Chimera log: " + message);
  }

  /**
   * Constructs and send commands to Chimera to set attributes on residues for
   * features visible in Jalview.
   * <p>
   * The syntax is: setattr r &lt;attName&gt; &lt;attValue&gt; &lt;atomSpec&gt;
   * <p>
   * For example: setattr r jv_chain "Ferredoxin-1, Chloroplastic" #0:94.A
   * 
   * @param avp
   * @return
   */
  public int sendFeaturesToViewer(AlignmentViewPanel avp)
  {
    // TODO refactor as required to pull up to an interface

    Map<String, Map<Object, AtomSpecModel>> featureValues = buildFeaturesMap(
            avp);
    List<StructureCommandI> commands = getCommandGenerator()
            .setAttributes(featureValues);
    if (commands.size() > 10)
    {
      sendCommandsByFile(commands);
    }
    else
    {
      executeCommands(commands, false, null);
    }
    return commands.size();
  }

  /**
   * Write commands to a temporary file, and send a command to Chimera to open
   * the file as a commands script. For use when sending a large number of
   * separate commands would overload the REST interface mechanism.
   * 
   * @param commands
   */
  protected void sendCommandsByFile(List<StructureCommandI> commands)
  {
    try
    {
      File tmp = File.createTempFile("chim", getCommandFileExtension());
      tmp.deleteOnExit();
      PrintWriter out = new PrintWriter(new FileOutputStream(tmp));
      for (StructureCommandI command : commands)
      {
        out.println(command.getCommand());
      }
      out.flush();
      out.close();
      String path = tmp.getAbsolutePath();
      StructureCommandI command = getCommandGenerator()
              .openCommandFile(path);
      executeCommand(false, null, command);
    } catch (IOException e)
    {
      System.err.println("Sending commands to Chimera via file failed with "
              + e.getMessage());
    }
  }

  /**
   * Returns the file extension required for a file of commands to be read by
   * the structure viewer
   * 
   * @return
   */
  protected String getCommandFileExtension()
  {
    return ".com";
  }

  /**
   * Create features in Jalview for the given attribute name and structure
   * residues.
   * 
   * <pre>
   * The residue list should be 0, 1 or more reply lines of the format: 
   *     residue id #0:5.A isHelix -155.000836316 index 5 
   * or 
   *     residue id #0:6.A isHelix None
   * </pre>
   * 
   * @param attName
   * @param residues
   * @return the number of features added
   */
  protected int createFeaturesForAttributes(String attName,
          List<String> residues)
  {
    int featuresAdded = 0;
    String featureGroup = getViewerFeatureGroup();

    for (String residue : residues)
    {
      AtomSpec spec = null;
      String[] tokens = residue.split(" ");
      if (tokens.length < 5)
      {
        continue;
      }
      String atomSpec = tokens[2];
      String attValue = tokens[4];

      /*
       * ignore 'None' (e.g. for phi) or 'False' (e.g. for isHelix)
       */
      if ("None".equalsIgnoreCase(attValue)
              || "False".equalsIgnoreCase(attValue))
      {
        continue;
      }

      try
      {
        spec = parseAtomSpec(atomSpec);
      } catch (IllegalArgumentException e)
      {
        Console.error("Problem parsing atomspec " + atomSpec);
        continue;
      }

      String chainId = spec.getChain();
      String description = attValue;
      float score = Float.NaN;
      try
      {
        score = Float.valueOf(attValue);
        description = chainId;
      } catch (NumberFormatException e)
      {
        // was not a float value
      }

      String pdbFile = getPdbFileForModel(spec.getModelNumber());
      spec.setPdbFile(pdbFile);

      List<AtomSpec> atoms = Collections.singletonList(spec);

      /*
       * locate the mapped position in the alignment (if any)
       */
      SearchResultsI sr = getSsm()
              .findAlignmentPositionsForStructurePositions(atoms);

      /*
       * expect one matched alignment position, or none 
       * (if the structure position is not mapped)
       */
      for (SearchResultMatchI m : sr.getResults())
      {
        SequenceI seq = m.getSequence();
        int start = m.getStart();
        int end = m.getEnd();
        SequenceFeature sf = new SequenceFeature(attName, description,
                start, end, score, featureGroup);
        // todo: should SequenceFeature have an explicit property for chain?
        // note: repeating the action shouldn't duplicate features
        if (seq.addSequenceFeature(sf))
        {
          featuresAdded++;
        }
      }
    }
    return featuresAdded;
  }

  /**
   * Answers the feature group name to apply to features created in Jalview from
   * Chimera attributes
   * 
   * @return
   */
  protected String getViewerFeatureGroup()
  {
    // todo pull up to interface
    return CHIMERA_FEATURE_GROUP;
  }

  @Override
  public String getModelIdForFile(String pdbFile)
  {
    List<ChimeraModel> foundModels = chimeraMaps.get(pdbFile);
    if (foundModels != null && !foundModels.isEmpty())
    {
      return String.valueOf(foundModels.get(0).getModelNumber());
    }
    return "";
  }

  /**
   * Answers a (possibly empty) list of attribute names in Chimera[X], excluding
   * any which were added from Jalview
   * 
   * @return
   */
  public List<String> getChimeraAttributes()
  {
    List<String> attributes = new ArrayList<>();
    StructureCommandI command = getCommandGenerator()
            .listResidueAttributes();
    final List<String> reply = executeCommand(command, true);
    if (reply != null)
    {
      for (String inputLine : reply)
      {
        String[] lineParts = inputLine.split("\\s");
        if (lineParts.length == 2 && lineParts[0].equals("resattr"))
        {
          String attName = lineParts[1];
          /*
           * exclude attributes added from Jalview
           */
          if (!attName.startsWith(ChimeraCommands.NAMESPACE_PREFIX))
          {
            attributes.add(attName);
          }
        }
      }
    }
    return attributes;
  }

  /**
   * Returns the file extension to use for a saved viewer session file (.py)
   * 
   * @return
   */
  @Override
  public String getSessionFileExtension()
  {
    return CHIMERA_SESSION_EXTENSION;
  }

  @Override
  public String getHelpURL()
  {
    return "https://www.cgl.ucsf.edu/chimera/docs/UsersGuide";
  }
}
