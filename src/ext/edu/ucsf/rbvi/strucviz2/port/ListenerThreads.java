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
package ext.edu.ucsf.rbvi.strucviz2.port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ext.edu.ucsf.rbvi.strucviz2.StructureManager;

/***************************************************
 *                 Thread Classes                  *
 **************************************************/

/**
 * Reply listener thread
 */
public class ListenerThreads extends Thread
{
  private BufferedReader lineReader = null;

  private Process chimera = null;

  private Map<String, List<String>> replyLog = null;

  private Logger logger;

  private StructureManager structureManager = null;

  private boolean stopMe = false;

  /**
   * Create a new listener thread to read the responses from Chimera
   * 
   * @param chimera
   *          a handle to the Chimera Process
   * @param structureManager
   *          a handle to the Chimera structure manager
   */
  public ListenerThreads(Process chimera, StructureManager structureManager)
  {
    this.chimera = chimera;
    this.structureManager = structureManager;
    replyLog = new HashMap<String, List<String>>();
    // Get a line-oriented reader
    InputStream readChan = chimera.getInputStream();
    lineReader = new BufferedReader(new InputStreamReader(readChan));
    logger = LoggerFactory.getLogger(
            ext.edu.ucsf.rbvi.strucviz2.port.ListenerThreads.class);
  }

  /**
   * Start the thread running
   */
  public void run()
  {
    // System.out.println("ReplyLogListener running");
    while (!stopMe)
    {
      try
      {
        chimeraRead();
      } catch (IOException e)
      {
        logger.warn("UCSF Chimera has exited: " + e.getMessage());
        return;
      } finally
      {
        if (lineReader != null)
        {
          try
          {
            lineReader.close();
          } catch (IOException e)
          {
          }
        }
      }
    }
  }

  public List<String> getResponse(String command)
  {
    List<String> reply;
    // System.out.println("getResponse: "+command);
    // TODO do we need a maximum wait time before aborting?
    while (!replyLog.containsKey(command))
    {
      try
      {
        Thread.currentThread().sleep(100);
      } catch (InterruptedException e)
      {
      }
    }

    synchronized (replyLog)
    {
      reply = replyLog.get(command);
      // System.out.println("getResponse ("+command+") = "+reply);
      replyLog.remove(command);
    }
    return reply;
  }

  public void clearResponse(String command)
  {
    try
    {
      Thread.currentThread().sleep(100);
    } catch (InterruptedException e)
    {
    }
    if (replyLog.containsKey(command))
    {
      replyLog.remove(command);
    }
    return;
  }

  /**
   * Read input from Chimera
   * 
   * @return a List containing the replies from Chimera
   */
  private void chimeraRead() throws IOException
  {
    if (chimera == null)
    {
      return;
    }

    String line = null;
    while ((line = lineReader.readLine()) != null)
    {
      // System.out.println("From Chimera-->" + line);
      if (line.startsWith("CMD"))
      {
        chimeraCommandRead(line.substring(4));
      }
      else if (line.startsWith("ModelChanged: "))
      {
        (new ModelUpdater()).start();
      }
      else if (line.startsWith("SelectionChanged: "))
      {
        (new SelectionUpdater()).start();
      }
      else if (line.startsWith("Trajectory residue network info:"))
      {
        (new NetworkUpdater(line)).start();
      }
    }
    return;
  }

  private void chimeraCommandRead(String command) throws IOException
  {
    // Generally -- looking for:
    // CMD command
    // ........
    // END
    // We return the text in between
    List<String> reply = new ArrayList<String>();
    boolean updateModels = false;
    boolean updateSelection = false;
    boolean importNetwork = false;
    String line = null;

    synchronized (replyLog)
    {
      while ((line = lineReader.readLine()) != null)
      {
        // System.out.println("From Chimera (" + command + ") -->" + line);
        if (line.startsWith("CMD"))
        {
          logger.warn("Got unexpected command from Chimera: " + line);

        }
        else if (line.startsWith("END"))
        {
          break;
        }
        if (line.startsWith("ModelChanged: "))
        {
          updateModels = true;
        }
        else if (line.startsWith("SelectionChanged: "))
        {
          updateSelection = true;
        }
        else if (line.length() == 0)
        {
          continue;
        }
        else if (!line.startsWith("CMD"))
        {
          reply.add(line);
        }
        else if (line.startsWith("Trajectory residue network info:"))
        {
          importNetwork = true;
        }
      }
      replyLog.put(command, reply);
    }
    if (updateModels)
    {
      (new ModelUpdater()).start();
    }
    if (updateSelection)
    {
      (new SelectionUpdater()).start();
    }
    if (importNetwork)
    {
      (new NetworkUpdater(line)).start();
    }
    return;
  }

  /**
   * Model updater thread
   */
  class ModelUpdater extends Thread
  {

    public ModelUpdater()
    {
    }

    public void run()
    {
      structureManager.updateModels();
      structureManager.modelChanged();
    }
  }

  /**
   * Selection updater thread
   */
  class SelectionUpdater extends Thread
  {

    public SelectionUpdater()
    {
    }

    public void run()
    {
      try
      {
        logger.info("Responding to chimera selection");
        structureManager.chimeraSelectionChanged();
      } catch (Exception e)
      {
        logger.warn("Could not update selection", e);
      }
    }
  }

  /**
   * Selection updater thread
   */
  class NetworkUpdater extends Thread
  {

    private String line;

    public NetworkUpdater(String line)
    {
      this.line = line;
    }

    public void run()
    {
      try
      {
        // ((TaskManager<?, ?>) structureManager.getService(TaskManager.class))
        // .execute(new ImportTrajectoryRINTaskFactory(structureManager, line)
        // .createTaskIterator());
      } catch (Exception e)
      {
        logger.warn("Could not import trajectory network", e);
      }
    }
  }

  /**
   * Set a flag that this thread should clean up and exit.
   */
  public void requestStop()
  {
    this.stopMe = true;
  }
}
