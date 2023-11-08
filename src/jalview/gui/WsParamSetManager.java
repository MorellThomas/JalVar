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
package jalview.gui;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.util.MessageManager;
import jalview.ws.params.ParamDatastoreI;
import jalview.ws.params.ParamManager;
import jalview.ws.params.WsParamSetI;
import jalview.xml.binding.jalview.ObjectFactory;
import jalview.xml.binding.jalview.WebServiceParameterSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * store and retrieve web service parameter sets.
 * 
 * @author JimP
 * 
 */
public class WsParamSetManager implements ParamManager
{
  Hashtable<String, ParamDatastoreI> paramparsers = new Hashtable<>();

  @Override
  public WsParamSetI[] getParameterSet(String name, String serviceUrl,
          boolean modifiable, boolean unmodifiable)
  {
    String files = Cache.getProperty("WS_PARAM_FILES");
    if (files == null)
    {
      return null;
    }
    StringTokenizer st = new StringTokenizer(files, "|");
    String pfile = null;
    List<WsParamSetI> params = new ArrayList<>();
    while (st.hasMoreTokens())
    {
      pfile = st.nextToken();
      try
      {
        WsParamSetI[] pset = parseParamFile(pfile);
        for (WsParamSetI p : pset)
        {
          boolean add = false;
          if (serviceUrl != null)
          {
            for (String url : p.getApplicableUrls())
            {
              if (url.equals(serviceUrl))
              {
                add = true;
              }
            }
          }
          else
          {
            add = true;
          }
          add &= (modifiable == p.isModifiable()
                  || unmodifiable == !p.isModifiable());
          add &= name == null || p.getName().equals(name);

          if (add)
          {

            params.add(p);
          }

        }
      } catch (IOException e)
      {
        Console.info("Failed to parse parameter file " + pfile
                + " (Check that all JALVIEW_WSPARAMFILES entries are valid!)",
                e);
      }
    }
    return params.toArray(new WsParamSetI[0]);
  }

  private WsParamSetI[] parseParamFile(String filename) throws IOException
  {
    List<WsParamSetI> psets = new ArrayList<>();
    InputStreamReader is = new InputStreamReader(
            new FileInputStream(new File(filename)), "UTF-8");

    WebServiceParameterSet wspset = null;
    try
    {
      JAXBContext jc = JAXBContext
              .newInstance("jalview.xml.binding.jalview");
      javax.xml.bind.Unmarshaller um = jc.createUnmarshaller();
      XMLStreamReader streamReader = XMLInputFactory.newInstance()
              .createXMLStreamReader(is);
      JAXBElement<WebServiceParameterSet> jbe = um.unmarshal(streamReader,
              WebServiceParameterSet.class);
      wspset = jbe.getValue();
    } catch (Exception ex)
    {
      throw new IOException(ex);
    }

    if (wspset != null && wspset.getParameters().length() > 0)
    {
      List<String> urls = wspset.getServiceURL();
      final String[] urlArray = urls.toArray(new String[urls.size()]);

      for (String url : urls)
      {
        ParamDatastoreI parser = paramparsers.get(url);
        if (parser != null)
        {
          WsParamSetI pset = parser.parseServiceParameterFile(
                  wspset.getName(), wspset.getDescription(), urlArray,
                  wspset.getParameters());
          if (pset != null)
          {
            pset.setSourceFile(filename);
            psets.add(pset);
            break;
          }
        }
      }
    }

    return psets.toArray(new WsParamSetI[0]);
  }

  @Override
  public void storeParameterSet(WsParamSetI parameterSet)
  {
    String filename = parameterSet.getSourceFile();
    File outfile = null;
    try
    {
      if (filename != null && !((outfile = new File(filename)).canWrite()))
      {
        Console.warn("Can't write to " + filename
                + " - Prompting for new file to write to.");
        filename = null;
      }
    } catch (Exception e)
    {
      filename = null;
    }

    ParamDatastoreI parser = null;
    for (String urls : parameterSet.getApplicableUrls())
    {
      if (parser == null)
      {
        parser = paramparsers.get(urls);
      }
    }
    if (parser == null)
    {
      throw new Error(MessageManager.getString(
              "error.implementation_error_cannot_find_marshaller_for_param_set"));
    }
    if (filename == null)
    {
      // TODO: JAL-3048 webservice - not required for Jalview-JS

      JalviewFileChooser chooser = new JalviewFileChooser("wsparams",
              "Web Service Parameter File");
      chooser.setFileView(new JalviewFileView());
      chooser.setDialogTitle(MessageManager
              .getString("label.choose_filename_for_param_file"));
      chooser.setToolTipText(MessageManager.getString("action.save"));
      int value = chooser.showSaveDialog(Desktop.instance);
      if (value == JalviewFileChooser.APPROVE_OPTION)
      {
        outfile = chooser.getSelectedFile();
        Cache.setProperty("LAST_DIRECTORY", outfile.getParent());
        filename = outfile.getAbsolutePath();
        if (!filename.endsWith(".wsparams"))
        {
          filename = filename.concat(".wsparams");
          outfile = new File(filename);
        }
      }
    }
    if (outfile != null)
    {
      String paramFiles = Cache.getDefault("WS_PARAM_FILES", filename);
      if (paramFiles.indexOf(filename) == -1)
      {
        if (paramFiles.length() > 0)
        {
          paramFiles = paramFiles.concat("|");
        }
        paramFiles = paramFiles.concat(filename);
      }
      Cache.setProperty("WS_PARAM_FILES", paramFiles);

      WebServiceParameterSet paramxml = new WebServiceParameterSet();

      paramxml.setName(parameterSet.getName());
      paramxml.setDescription(parameterSet.getDescription());
      for (String url : parameterSet.getApplicableUrls())
      {
        paramxml.getServiceURL().add(url);
      }
      paramxml.setVersion("1.0");
      try
      {
        paramxml.setParameters(
                parser.generateServiceParameterFile(parameterSet));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(outfile), "UTF-8"));
        JAXBContext jaxbContext = JAXBContext
                .newInstance(WebServiceParameterSet.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal(
                new ObjectFactory().createWebServiceParameterSet(paramxml),
                out);
        out.close();
        parameterSet.setSourceFile(filename);
      } catch (Exception e)
      {
        Console.error("Couldn't write parameter file to " + outfile, e);
      }
    }
  }

  /*
   * 
   * JalviewFileChooser chooser = new JalviewFileChooser(Cache
   * .getProperty("LAST_DIRECTORY"), new String[] { "jc" }, new String[] {
   * "Jalview User Colours" }, "Jalview User Colours"); chooser.setFileView(new
   * jalview.io.JalviewFileView());
   * chooser.setDialogTitle("Load colour scheme");
   * chooser.setToolTipText("Load");
   * 
   * int value = chooser.showOpenDialog(this);
   * 
   * if (value == JalviewFileChooser.APPROVE_OPTION) { File choice =
   * chooser.getSelectedFile(); Cache.setProperty("LAST_DIRECTORY",
   * choice.getParent()); String defaultColours = Cache.getDefault(
   * "USER_DEFINED_COLOURS", choice.getPath()); if
   * (defaultColours.indexOf(choice.getPath()) == -1) { defaultColours =
   * defaultColours.concat("|") .concat(choice.getPath()); } (non-Javadoc)
   * 
   * @see
   * jalview.ws.params.ParamManager#deleteParameterSet(jalview.ws.params.WsParamSetI
   * )
   */
  @Override
  public void deleteParameterSet(WsParamSetI parameterSet)
  {
    String filename = parameterSet.getSourceFile();
    if (filename == null || filename.trim().length() < 1)
    {
      return;
    }
    String paramFiles = Cache.getDefault("WS_PARAM_FILES", "");
    if (paramFiles.indexOf(filename) > -1)
    {
      String nparamFiles = new String();
      StringTokenizer st = new StringTokenizer(paramFiles, "|");
      while (st.hasMoreElements())
      {
        String fl = st.nextToken();
        if (!fl.equals(filename))
        {
          nparamFiles = nparamFiles.concat("|").concat(fl);
        }
      }
      Cache.setProperty("WS_PARAM_FILES", nparamFiles);
    }

    try
    {
      File pfile = new File(filename);
      if (pfile.exists() && pfile.canWrite())
      {
        if (JvOptionPane.showConfirmDialog(Desktop.instance,
                "Delete the preset's file, too ?", "Delete User Preset ?",
                JvOptionPane.OK_CANCEL_OPTION) == JvOptionPane.OK_OPTION)
        {
          pfile.delete();
        }
      }
    } catch (Exception e)
    {
      Console.error(
              "Exception when trying to delete webservice user preset: ",
              e);
    }
  }

  @Override
  public void registerParser(String hosturl, ParamDatastoreI paramdataStore)
  {
    paramparsers.put(hosturl, paramdataStore);
  }

}
