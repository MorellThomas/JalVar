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
package jalview.ext.pymol;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.gui.Preferences;
import jalview.structure.StructureCommandI;
import jalview.util.Platform;

public class PymolManager
{
  private static final int RPC_REPLY_TIMEOUT_MS = 15000;

  private static final int CONNECTION_TIMEOUT_MS = 100;

  private static final String POST1 = "<methodCall><methodName>";

  private static final String POST2 = "</methodName><params>";

  private static final String POST3 = "</params></methodCall>";

  private Process pymolProcess;

  private int pymolXmlRpcPort;

  /**
   * Returns a list of paths to try for the PyMOL executable. Any user
   * preference is placed first, otherwise 'standard' paths depending on the
   * operating system.
   * 
   * @return
   */
  public static List<String> getPymolPaths()
  {
    return getPymolPaths(System.getProperty("os.name"));
  }

  /**
   * Returns a list of paths to try for the PyMOL executable. Any user
   * preference is placed first, otherwise 'standard' paths depending on the
   * operating system.
   * 
   * @param os
   *          operating system as reported by environment variable
   *          {@code os.name}
   * @return
   */
  protected static List<String> getPymolPaths(String os)
  {
    List<String> pathList = new ArrayList<>();

    String userPath = Cache.getDefault(Preferences.PYMOL_PATH, null);
    if (userPath != null)
    {
      pathList.add(userPath);
    }

    /*
     * add default installation paths
     */
    String pymol = "PyMOL";
    if (os.startsWith("Linux"))
    {
      pathList.add("/usr/local/pymol/bin/" + pymol);
      pathList.add("/usr/local/bin/" + pymol);
      pathList.add("/usr/bin/" + pymol);
      pathList.add(System.getProperty("user.home") + "/opt/bin/" + pymol);
    }
    else if (os.startsWith("Windows"))
    {
      for (String root : new String[] {
          String.format("%s\\AppData\\Local",
                  System.getProperty("user.home")), // default user path
          "\\ProgramData", "C:\\ProgramData", // this is the default install
                                              // path "for everyone"
          System.getProperty("user.home"), "\\Program Files",
          "C:\\Program Files", "\\Program Files (x86)",
          "C:\\Program Files (x86)" })
      {
        for (String path : new String[] { "Schrodinger\\PyMOL2", "PyMOL" })
        {
          for (String binary : new String[] { "PyMOLWinWithConsole.bat",
              "Scripts\\pymol.exe", "PyMOLWin.exe" })
          {
            pathList.add(String.format("%s\\%s\\%s", root, path, binary));
          }
        }
      }
    }
    else if (os.startsWith("Mac"))
    {
      pathList.add("/Applications/PyMOL.app/Contents/MacOS/" + pymol);
    }
    return pathList;
  }

  public boolean isPymolLaunched()
  {
    // TODO pull up generic methods for external viewer processes
    boolean launched = false;
    if (pymolProcess != null)
    {
      try
      {
        pymolProcess.exitValue();
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
   * Sends the command to Pymol; if requested, tries to get and return any
   * replies, else returns null
   * 
   * @param command
   * @param getReply
   * @return
   */
  public List<String> sendCommand(StructureCommandI command,
          boolean getReply)
  {
    String postBody = getPostRequest(command);
    // System.out.println(postBody);// debug
    String rpcUrl = "http://127.0.0.1:" + this.pymolXmlRpcPort;
    PrintWriter out = null;
    BufferedReader in = null;
    List<String> result = getReply ? new ArrayList<>() : null;

    try
    {
      URL realUrl = new URL(rpcUrl);
      HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
      conn.setRequestProperty("accept", "*/*");
      conn.setRequestProperty("content-type", "text/xml");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      out = new PrintWriter(conn.getOutputStream());
      out.print(postBody);
      out.flush();
      int rc = conn.getResponseCode();
      if (rc != HttpURLConnection.HTTP_OK)
      {
        Console.error(
                String.format("Error status from %s: %d", rpcUrl, rc));
        return result;
      }

      InputStream inputStream = conn.getInputStream();
      if (getReply)
      {
        in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = in.readLine()) != null)
        {
          result.add(line);
        }
      }
    } catch (SocketException e)
    {
      // thrown when 'quit' command is sent to PyMol
      Console.warn(String.format("Request to %s returned %s", rpcUrl,
              e.toString()));
    } catch (Exception e)
    {
      e.printStackTrace();
    } finally
    {
      if (out != null)
      {
        out.close();
      }
      if (Console.isTraceEnabled())
      {
        Console.trace("Sent: " + command.toString());
        if (result != null)
        {
          Console.trace("Received: " + result);
        }
      }
    }
    return result;
  }

  /**
   * Builds the body of the XML-RPC format POST request to execute the command
   * 
   * @param command
   * @return
   */
  static String getPostRequest(StructureCommandI command)
  {
    StringBuilder sb = new StringBuilder(64);
    sb.append(POST1).append(command.getCommand()).append(POST2);
    if (command.hasParameters())
    {
      for (String p : command.getParameters())
      {
        /*
         * for now assuming all are string - <string> element is optional
         * refactor in future if other data types needed
         * https://www.tutorialspoint.com/xml-rpc/xml_rpc_data_model.htm
         */
        sb.append("<parameter><value>").append(p)
                .append("</value></parameter>");
      }
    }
    sb.append(POST3);
    return sb.toString();
  }

  public Process launchPymol()
  {
    // todo pull up much of this
    // Do nothing if already launched
    if (isPymolLaunched())
    {
      return pymolProcess;
    }

    String error = "Error message: ";
    for (String pymolPath : getPymolPaths())
    {
      try
      {
        // ensure symbolic links are resolved
        pymolPath = Paths.get(pymolPath).toRealPath().toString();
        File path = new File(pymolPath);
        // uncomment the next line to simulate Pymol not installed
        // path = new File(pymolPath + "x");
        if (!path.canExecute())
        {
          error += "File '" + path + "' does not exist.\n";
          continue;
        }
        List<String> args = new ArrayList<>();
        args.add(pymolPath);

        // Windows PyMOLWin.exe needs an extra argument
        if (Platform.isWin() && pymolPath.toLowerCase(Locale.ROOT)
                .endsWith("\\pymolwin.exe"))
        {
          args.add("+2");
        }
        args.add("-R"); // https://pymolwiki.org/index.php/RPC
        ProcessBuilder pb = new ProcessBuilder(args);
        Console.debug("Running PyMOL as " + String.join(" ", pb.command()));
        pymolProcess = pb.start();
        error = "";
        break;
      } catch (Exception e)
      {
        // Pymol could not be started using this path
        error += e.getMessage();
      }
    }

    if (pymolProcess != null)
    {
      this.pymolXmlRpcPort = getPortNumber();
      if (pymolXmlRpcPort > 0)
      {
        Console.info("PyMOL XMLRPC started on port " + pymolXmlRpcPort);
      }
      else
      {
        error += "Failed to read PyMOL XMLRPC port number";
        Console.error(error);
        pymolProcess.destroy();
        pymolProcess = null;
      }
    }

    return pymolProcess;
  }

  private int getPortNumber()
  {
    // TODO pull up most of this!
    int port = 0;
    InputStream readChan = pymolProcess.getInputStream();
    BufferedReader lineReader = new BufferedReader(
            new InputStreamReader(readChan));
    StringBuilder responses = new StringBuilder();
    try
    {
      String response = lineReader.readLine();
      while (response != null)
      {
        responses.append("\n" + response);
        // expect: xml-rpc server running on host localhost, port 9123
        if (response.contains("xml-rpc"))
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
      Console.error("Failed to get REST port number from " + responses
              + ": " + e.getMessage());
      // logger.error("Failed to get REST port number from " + responses + ": "
      // + e.getMessage());
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
      Console.error("Failed to start PyMOL with XMLRPC, response was: "
              + responses);
    }
    Console.info("PyMOL started with XMLRPC on port " + port);
    return port;
  }

}
