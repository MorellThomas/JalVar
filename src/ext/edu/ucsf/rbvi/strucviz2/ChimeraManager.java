/* vim: set ts=2: */
/**
 * Copyright (c) 2006 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package ext.edu.ucsf.rbvi.strucviz2;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ext.edu.ucsf.rbvi.strucviz2.StructureManager.ModelType;
import ext.edu.ucsf.rbvi.strucviz2.port.ListenerThreads;
import jalview.ws.HttpClientUtils;

/**
 * This object maintains the Chimera communication information.
 */
public class ChimeraManager
{
  private static final int REST_REPLY_TIMEOUT_MS = 15000;

  private static final int CONNECTION_TIMEOUT_MS = 100;

  private static final boolean debug = false;

  private int chimeraRestPort;

  private Process chimera;

  private ListenerThreads chimeraListenerThread;

  private Map<Integer, ChimeraModel> currentModelsMap;

  private Logger logger = LoggerFactory
          .getLogger(ext.edu.ucsf.rbvi.strucviz2.ChimeraManager.class);

  private StructureManager structureManager;

  public ChimeraManager(StructureManager structureManager)
  {
    this.structureManager = structureManager;
    chimera = null;
    chimeraListenerThread = null;
    currentModelsMap = new HashMap<>();

  }

  public List<ChimeraModel> getChimeraModels(String modelName)
  {
    List<ChimeraModel> models = getChimeraModels(modelName,
            ModelType.PDB_MODEL);
    models.addAll(getChimeraModels(modelName, ModelType.SMILES));
    return models;
  }

  public List<ChimeraModel> getChimeraModels(String modelName,
          ModelType modelType)
  {
    List<ChimeraModel> models = new ArrayList<>();
    for (ChimeraModel model : currentModelsMap.values())
    {
      if (modelName.equals(model.getModelName())
              && modelType.equals(model.getModelType()))
      {
        models.add(model);
      }
    }
    return models;
  }

  public Map<String, List<ChimeraModel>> getChimeraModelsMap()
  {
    Map<String, List<ChimeraModel>> models = new HashMap<>();
    for (ChimeraModel model : currentModelsMap.values())
    {
      String modelName = model.getModelName();
      if (!models.containsKey(modelName))
      {
        models.put(modelName, new ArrayList<ChimeraModel>());
      }
      if (!models.get(modelName).contains(model))
      {
        models.get(modelName).add(model);
      }
    }
    return models;
  }

  public ChimeraModel getChimeraModel(Integer modelNumber,
          Integer subModelNumber)
  {
    Integer key = ChimUtils.makeModelKey(modelNumber, subModelNumber);
    if (currentModelsMap.containsKey(key))
    {
      return currentModelsMap.get(key);
    }
    return null;
  }

  public ChimeraModel getChimeraModel()
  {
    return currentModelsMap.values().iterator().next();
  }

  public Collection<ChimeraModel> getChimeraModels()
  {
    // this method is invoked by the model navigator dialog
    return currentModelsMap.values();
  }

  public int getChimeraModelsCount(boolean smiles)
  {
    // this method is invokes by the model navigator dialog
    int counter = currentModelsMap.size();
    if (smiles)
    {
      return counter;
    }

    for (ChimeraModel model : currentModelsMap.values())
    {
      if (model.getModelType() == ModelType.SMILES)
      {
        counter--;
      }
    }
    return counter;
  }

  public boolean hasChimeraModel(Integer modelNubmer)
  {
    return hasChimeraModel(modelNubmer, 0);
  }

  public boolean hasChimeraModel(Integer modelNubmer,
          Integer subModelNumber)
  {
    return currentModelsMap.containsKey(
            ChimUtils.makeModelKey(modelNubmer, subModelNumber));
  }

  public void addChimeraModel(Integer modelNumber, Integer subModelNumber,
          ChimeraModel model)
  {
    currentModelsMap.put(
            ChimUtils.makeModelKey(modelNumber, subModelNumber), model);
  }

  public void removeChimeraModel(Integer modelNumber,
          Integer subModelNumber)
  {
    int modelKey = ChimUtils.makeModelKey(modelNumber, subModelNumber);
    if (currentModelsMap.containsKey(modelKey))
    {
      currentModelsMap.remove(modelKey);
    }
  }

  public List<ChimeraModel> openModel(String modelPath, ModelType type)
  {
    return openModel(modelPath, getFileNameFromPath(modelPath), type);
  }

  /**
   * Overloaded method to allow Jalview to pass in a model name.
   * 
   * @param modelPath
   * @param modelName
   * @param type
   * @return
   */
  public List<ChimeraModel> openModel(String modelPath, String modelName,
          ModelType type)
  {
    logger.info("chimera open " + modelPath);
    // stopListening();
    List<ChimeraModel> modelList = getModelList();
    List<String> response = null;
    // TODO: [Optional] Handle modbase models
    if (type == ModelType.MODBASE_MODEL)
    {
      response = sendChimeraCommand("open modbase:" + modelPath, true);
      // } else if (type == ModelType.SMILES) {
      // response = sendChimeraCommand("open smiles:" + modelName, true);
      // modelName = "smiles:" + modelName;
    }
    else
    {
      response = sendChimeraCommand("open " + modelPath, true);
    }
    if (response == null)
    {
      // something went wrong
      logger.warn("Could not open " + modelPath);
      return null;
    }

    // patch for Jalview - set model name in Chimera
    // TODO: find a variant that works for sub-models
    for (ChimeraModel newModel : getModelList())
    {
      if (!modelList.contains(newModel))
      {
        newModel.setModelName(modelName);
        sendChimeraCommand("setattr M name " + modelName + " #"
                + newModel.getModelNumber(), false);
        modelList.add(newModel);
      }
    }

    // assign color and residues to open models
    for (ChimeraModel chimeraModel : modelList)
    {
      // get model color
      Color modelColor = isChimeraX() ? null : getModelColor(chimeraModel);
      if (modelColor != null)
      {
        chimeraModel.setModelColor(modelColor);
      }

      // Get our properties (default color scheme, etc.)
      // Make the molecule look decent
      // chimeraSend("repr stick "+newModel.toSpec());

      // Create the information we need for the navigator
      if (type != ModelType.SMILES && !isChimeraX())
      {
        addResidues(chimeraModel);
      }
    }

    sendChimeraCommand("focus", false);
    // startListening(); // see ChimeraListener
    return modelList;
  }

  /**
   * Refactored method to extract the last (or only) element delimited by file
   * path separator.
   * 
   * @param modelPath
   * @return
   */
  private String getFileNameFromPath(String modelPath)
  {
    String modelName = modelPath;
    if (modelPath == null)
    {
      return null;
    }
    // TODO: [Optional] Convert path to name in a better way
    if (modelPath.lastIndexOf(File.separator) > 0)
    {
      modelName = modelPath
              .substring(modelPath.lastIndexOf(File.separator) + 1);
    }
    else if (modelPath.lastIndexOf("/") > 0)
    {
      modelName = modelPath.substring(modelPath.lastIndexOf("/") + 1);
    }
    return modelName;
  }

  public void closeModel(ChimeraModel model)
  {
    // int model = structure.modelNumber();
    // int subModel = structure.subModelNumber();
    // Integer modelKey = makeModelKey(model, subModel);
    stopListening();
    logger.info("chimera close model " + model.getModelName());
    if (currentModelsMap.containsKey(ChimUtils.makeModelKey(
            model.getModelNumber(), model.getSubModelNumber())))
    {
      sendChimeraCommand("close " + model.toSpec(), false);
      // currentModelNamesMap.remove(model.getModelName());
      currentModelsMap.remove(ChimUtils.makeModelKey(model.getModelNumber(),
              model.getSubModelNumber()));
      // selectionList.remove(chimeraModel);
    }
    else
    {
      logger.warn("Could not find model " + model.getModelName()
              + " to close.");
    }
    startListening();
  }

  public void startListening()
  {
    sendChimeraCommand("listen start models; listen start selection",
            false);
  }

  public void stopListening()
  {
    String command = "listen stop models ; listen stop selection ";
    sendChimeraCommand(command, false);
  }

  /**
   * Tell Chimera we are listening on the given URI
   * 
   * @param uri
   */
  public void startListening(String uri)
  {
    /*
     * listen for model changes
     */
    String command = "listen start models url " + uri;
    sendChimeraCommand(command, false);

    /*
     * listen for selection changes
     */
    command = "listen start select prefix SelectionChanged url " + uri;
    sendChimeraCommand(command, false);
  }

  /**
   * Select something in Chimera
   * 
   * @param command
   *          the selection command to pass to Chimera
   */
  public void select(String command)
  {
    sendChimeraCommand("listen stop selection; " + command
            + "; listen start selection", false);
  }

  public void focus()
  {
    sendChimeraCommand("focus", false);
  }

  public void clearOnChimeraExit()
  {
    chimera = null;
    currentModelsMap.clear();
    this.chimeraRestPort = 0;
    structureManager.clearOnChimeraExit();
  }

  public void exitChimera()
  {
    if (isChimeraLaunched() && chimera != null)
    {
      sendChimeraCommand("stop really", false);
      try
      {
        // TODO is this too violent? could it force close the process
        // before it has done an orderly shutdown?
        chimera.destroy();
      } catch (Exception ex)
      {
        // ignore
      }
    }
    clearOnChimeraExit();
  }

  public Map<Integer, ChimeraModel> getSelectedModels()
  {
    Map<Integer, ChimeraModel> selectedModelsMap = new HashMap<>();
    List<String> chimeraReply = sendChimeraCommand(
            "list selection level molecule", true);
    if (chimeraReply != null)
    {
      for (String modelLine : chimeraReply)
      {
        ChimeraModel chimeraModel = new ChimeraModel(modelLine);
        Integer modelKey = ChimUtils.makeModelKey(
                chimeraModel.getModelNumber(),
                chimeraModel.getSubModelNumber());
        selectedModelsMap.put(modelKey, chimeraModel);
      }
    }
    return selectedModelsMap;
  }

  /**
   * Sends a 'list selection level residue' command to Chimera and returns the
   * list of selected atomspecs
   * 
   * @return
   */
  public List<String> getSelectedResidueSpecs()
  {
    List<String> selectedResidues = new ArrayList<>();

    String command = "list selection level residue";
    List<String> chimeraReply = sendChimeraCommand(command, true);
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
    return selectedResidues;
  }

  public void getSelectedResidues(
          Map<Integer, ChimeraModel> selectedModelsMap)
  {
    List<String> chimeraReply = sendChimeraCommand(
            "list selection level residue", true);
    if (chimeraReply != null)
    {
      for (String inputLine : chimeraReply)
      {
        ChimeraResidue r = new ChimeraResidue(inputLine);
        Integer modelKey = ChimUtils.makeModelKey(r.getModelNumber(),
                r.getSubModelNumber());
        if (selectedModelsMap.containsKey(modelKey))
        {
          ChimeraModel model = selectedModelsMap.get(modelKey);
          model.addResidue(r);
        }
      }
    }
  }

  /**
   * Return the list of ChimeraModels currently open. Warning: if smiles model
   * name too long, only part of it with "..." is printed.
   * 
   * 
   * @return List of ChimeraModel's
   */
  // TODO: [Optional] Handle smiles names in a better way in Chimera?
  public List<ChimeraModel> getModelList()
  {
    List<ChimeraModel> modelList = new ArrayList<>();
    String command = "list models type "
            + (isChimeraX() ? "AtomicStructure" : "molecule");
    List<String> list = sendChimeraCommand(command, true);
    if (list != null)
    {
      for (String modelLine : list)
      {
        try
        {
          ChimeraModel chimeraModel = new ChimeraModel(modelLine);
          modelList.add(chimeraModel);
        } catch (NullPointerException e)
        {
          // hack for now
        }
      }
    }
    return modelList;
  }

  /**
   * Return the list of depiction presets available from within Chimera. Chimera
   * will return the list as a series of lines with the format: Preset type
   * number "description"
   * 
   * @return list of presets
   */
  public List<String> getPresets()
  {
    ArrayList<String> presetList = new ArrayList<>();
    List<String> output = sendChimeraCommand("preset list", true);
    if (output != null)
    {
      for (String preset : output)
      {
        preset = preset.substring(7); // Skip over the "Preset"
        preset = preset.replaceFirst("\"", "(");
        preset = preset.replaceFirst("\"", ")");
        // string now looks like: type number (description)
        presetList.add(preset);
      }
    }
    return presetList;
  }

  public boolean isChimeraLaunched()
  {
    boolean launched = false;
    if (chimera != null)
    {
      try
      {
        chimera.exitValue();
        // if we get here, process has ended
      } catch (IllegalThreadStateException e)
      {
        // ok - not yet terminated
        launched = true;
      }
    }
    return launched;
  }

  /**
   * Launch Chimera, unless an instance linked to this object is already
   * running. Returns true if chimera is successfully launched, or already
   * running, else false.
   * 
   * @param chimeraPaths
   * @return
   */
  public boolean launchChimera(List<String> chimeraPaths)
  {
    // Do nothing if Chimera is already launched
    if (isChimeraLaunched())
    {
      return true;
    }

    // Try to launch Chimera (eventually using one of the possible paths)
    String error = "Error message: ";
    String workingPath = "";
    // iterate over possible paths for starting Chimera
    for (String chimeraPath : chimeraPaths)
    {
      try
      {
        // ensure symbolic links are resolved
        chimeraPath = Paths.get(chimeraPath).toRealPath().toString();
        File path = new File(chimeraPath);
        // uncomment the next line to simulate Chimera not installed
        // path = new File(chimeraPath + "x");
        if (!path.canExecute())
        {
          error += "File '" + path + "' does not exist.\n";
          continue;
        }
        List<String> args = new ArrayList<>();
        args.add(chimeraPath);
        // shows Chimera output window but suppresses REST responses:
        // args.add("--debug");
        addLaunchArguments(args);
        ProcessBuilder pb = new ProcessBuilder(args);
        chimera = pb.start();
        error = "";
        workingPath = chimeraPath;
        break;
      } catch (Exception e)
      {
        // Chimera could not be started using this path
        error += e.getMessage();
      }
    }
    // If no error, then Chimera was launched successfully
    if (error.length() == 0)
    {
      this.chimeraRestPort = getPortNumber();
      System.out.println(
              "Chimera REST API started on port " + chimeraRestPort);
      // structureManager.initChimTable();
      structureManager.setChimeraPathProperty(workingPath);
      // TODO: [Optional] Check Chimera version and show a warning if below 1.8
      // Ask Chimera to give us updates
      // startListening(); // later - see ChimeraListener
      return (chimeraRestPort > 0);
    }

    // Tell the user that Chimera could not be started because of an error
    logger.warn(error);
    return false;
  }

  /**
   * Adds command-line arguments to start the REST server
   * <p>
   * Method extracted for Jalview to allow override in ChimeraXManager
   * 
   * @param args
   */
  protected void addLaunchArguments(List<String> args)
  {
    args.add("--start");
    args.add("RESTServer");
  }

  /**
   * Read and return the port number returned in the reply to --start RESTServer
   */
  private int getPortNumber()
  {
    int port = 0;
    InputStream readChan = chimera.getInputStream();
    BufferedReader lineReader = new BufferedReader(
            new InputStreamReader(readChan));
    StringBuilder responses = new StringBuilder();
    try
    {
      String response = lineReader.readLine();
      while (response != null)
      {
        responses.append("\n" + response);
        // expect: REST server on host 127.0.0.1 port port_number
        // ChimeraX is the same except "REST server started on host..."
        if (response.startsWith("REST server"))
        {
          String[] tokens = response.split(" ");
          for (int i = 0; i < tokens.length - 1; i++)
          {
            if ("port".equals(tokens[i]))
            {
              port = Integer.parseInt(tokens[i + 1]);
              break;
            }
          }
        }
        if (port > 0)
        {
          break; // hack for hanging readLine()
        }
        response = lineReader.readLine();
      }
    } catch (Exception e)
    {
      logger.error("Failed to get REST port number from " + responses + ": "
              + e.getMessage());
    } finally
    {
      try
      {
        lineReader.close();
      } catch (IOException e2)
      {
      }
    }
    if (port == 0)
    {
      System.err.println(
              "Failed to start Chimera with REST service, response was: "
                      + responses);
    }
    logger.info(
            "Chimera REST service listening on port " + chimeraRestPort);
    return port;
  }

  /**
   * Determine the color that Chimera is using for this model.
   * 
   * @param model
   *          the ChimeraModel we want to get the Color for
   * @return the default model Color for this model in Chimera
   */
  public Color getModelColor(ChimeraModel model)
  {
    List<String> colorLines = sendChimeraCommand(
            "list model spec " + model.toSpec() + " attribute color", true);
    if (colorLines == null || colorLines.size() == 0)
    {
      return null;
    }
    return ChimUtils.parseModelColor(colorLines.get(0));
  }

  /**
   * 
   * Get information about the residues associated with a model. This uses the
   * Chimera listr command. We don't return the resulting residues, but we add
   * the residues to the model.
   * 
   * @param model
   *          the ChimeraModel to get residue information for
   * 
   */
  public void addResidues(ChimeraModel model)
  {
    int modelNumber = model.getModelNumber();
    int subModelNumber = model.getSubModelNumber();
    // Get the list -- it will be in the reply log
    List<String> reply = sendChimeraCommand(
            "list residues spec " + model.toSpec(), true);
    if (reply == null)
    {
      return;
    }
    for (String inputLine : reply)
    {
      ChimeraResidue r = new ChimeraResidue(inputLine);
      if (r.getModelNumber() == modelNumber
              || r.getSubModelNumber() == subModelNumber)
      {
        model.addResidue(r);
      }
    }
  }

  public List<String> getAttrList()
  {
    List<String> attributes = new ArrayList<>();
    String command = (isChimeraX() ? "info " : "list ") + "resattr";
    final List<String> reply = sendChimeraCommand(command, true);
    if (reply != null)
    {
      for (String inputLine : reply)
      {
        String[] lineParts = inputLine.split("\\s");
        if (lineParts.length == 2 && lineParts[0].equals("resattr"))
        {
          attributes.add(lineParts[1]);
        }
      }
    }
    return attributes;
  }

  public Map<ChimeraResidue, Object> getAttrValues(String aCommand,
          ChimeraModel model)
  {
    Map<ChimeraResidue, Object> values = new HashMap<>();
    final List<String> reply = sendChimeraCommand("list residue spec "
            + model.toSpec() + " attribute " + aCommand, true);
    if (reply != null)
    {
      for (String inputLine : reply)
      {
        String[] lineParts = inputLine.split("\\s");
        if (lineParts.length == 5)
        {
          ChimeraResidue residue = ChimUtils.getResidue(lineParts[2],
                  model);
          String value = lineParts[4];
          if (residue != null)
          {
            if (value.equals("None"))
            {
              continue;
            }
            if (value.equals("True") || value.equals("False"))
            {
              values.put(residue, Boolean.valueOf(value));
              continue;
            }
            try
            {
              Double doubleValue = Double.valueOf(value);
              values.put(residue, doubleValue);
            } catch (NumberFormatException ex)
            {
              values.put(residue, value);
            }
          }
        }
      }
    }
    return values;
  }

  private volatile boolean busy = false;

  /**
   * Send a command to Chimera.
   * 
   * @param command
   *          Command string to be send.
   * @param reply
   *          Flag indicating whether the method should return the reply from
   *          Chimera or not.
   * @return List of Strings corresponding to the lines in the Chimera reply or
   *         <code>null</code>.
   */
  public List<String> sendChimeraCommand(String command, boolean reply)
  {
    if (debug)
    {
      System.out.println("chimeradebug>> " + command);
    }
    if (!isChimeraLaunched() || command == null
            || "".equals(command.trim()))
    {
      return null;
    }
    /*
     * set a maximum wait time before trying anyway
     * to avoid hanging indefinitely
     */
    int waited = 0;
    int pause = 25;
    while (busy && waited < 1001)
    {
      try
      {
        Thread.sleep(pause);
        waited += pause;
      } catch (InterruptedException q)
      {
      }
    }
    busy = true;
    long startTime = System.currentTimeMillis();
    try
    {
      return sendRestCommand(command);
    } finally
    {
      /*
       * Make sure busy flag is reset come what may!
       */
      busy = false;
      if (debug)
      {
        System.out.println("Chimera command took "
                + (System.currentTimeMillis() - startTime) + "ms: "
                + command);
      }
    }
  }

  /**
   * Sends the command to Chimera's REST API, and returns any response lines.
   * 
   * @param command
   * @return
   */
  protected List<String> sendRestCommand(String command)
  {
    String restUrl = "http://127.0.0.1:" + this.chimeraRestPort + "/run";
    List<NameValuePair> commands = new ArrayList<>(1);
    String method = getHttpRequestMethod();
    if ("GET".equals(method))
    {
      try
      {
        command = URLEncoder.encode(command, StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException e)
      {
        command = command.replace(" ", "+").replace("#", "%23")
                .replace("|", "%7C").replace(";", "%3B")
                .replace(":", "%3A");
      }
    }
    commands.add(new BasicNameValuePair("command", command));

    List<String> reply = new ArrayList<>();
    BufferedReader response = null;
    try
    {
      response = "GET".equals(method)
              ? HttpClientUtils.doHttpGet(restUrl, commands,
                      CONNECTION_TIMEOUT_MS, REST_REPLY_TIMEOUT_MS)
              : HttpClientUtils.doHttpUrlPost(restUrl, commands,
                      CONNECTION_TIMEOUT_MS, REST_REPLY_TIMEOUT_MS);
      String line = "";
      while ((line = response.readLine()) != null)
      {
        reply.add(line);
      }
    } catch (Exception e)
    {
      logger.error("REST call '" + command + "' failed: " + e.getMessage());
    } finally
    {
      if (response != null)
      {
        try
        {
          response.close();
        } catch (IOException e)
        {
        }
      }
    }
    return reply;
  }

  /**
   * Returns "POST" as the HTTP request method to use for REST service calls to
   * Chimera
   * 
   * @return
   */
  protected String getHttpRequestMethod()
  {
    return "POST";
  }

  /**
   * Send a command to stdin of Chimera process, and optionally read any
   * responses.
   * 
   * @param command
   * @param readReply
   * @return
   */
  protected List<String> sendStdinCommand(String command, boolean readReply)
  {
    chimeraListenerThread.clearResponse(command);
    String text = command.concat("\n");
    try
    {
      // send the command
      chimera.getOutputStream().write(text.getBytes());
      chimera.getOutputStream().flush();
    } catch (IOException e)
    {
      // logger.info("Unable to execute command: " + text);
      // logger.info("Exiting...");
      logger.warn("Unable to execute command: " + text);
      logger.warn("Exiting...");
      clearOnChimeraExit();
      return null;
    }
    if (!readReply)
    {
      return null;
    }
    List<String> rsp = chimeraListenerThread.getResponse(command);
    return rsp;
  }

  public StructureManager getStructureManager()
  {
    return structureManager;
  }

  public boolean isBusy()
  {
    return busy;
  }

  public Process getChimeraProcess()
  {
    return chimera;
  }

  public boolean isChimeraX()
  {
    return false;
  }
}
