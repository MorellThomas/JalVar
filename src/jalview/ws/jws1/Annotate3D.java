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
package jalview.ws.jws1;

import jalview.datamodel.AlignmentI;
import jalview.io.FileFormat;
import jalview.io.FileParse;
import jalview.io.FormatAdapter;
import jalview.io.InputStreamParser;
import jalview.util.MessageManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class Annotate3D
{
  // protected BufferedReader in;
  // protected BufferedWriter out;

  public Annotate3D()
  {
    System.out.println("Annotate3D");
    // try {
    // Create a URL for the desired page
    // String id = "1HR2";
    // URL url = new
    // URL("http://paradise-ibmc.u-strasbg.fr/webservices/annotate3d?pdbid="+id);
    // in = new BufferedReader(new InputStreamReader(url.openStream()));
    // String str;
    // OutputStream out1 = null;
    // out = new BufferedWriter(new OutputStreamWriter(out1, "temp.rnaml"));
    // while ((str = in.readLine()) != null) {
    // System.out.println(str);
    // out.write(str);
    // }
    // in.close();
    // out.close();
    // } catch (MalformedURLException e) {
    // } catch (IOException e) {
    // }
  }

  public AlignmentI getRNAMLFor(final FileParse source) throws IOException
  {
    try
    {
      StringBuffer sb = new StringBuffer();

      Reader fpr = source.getReader();
      int p = 0;
      char[] cbuff = new char[2048];
      while ((p = fpr.read(cbuff)) > 0)
      {
        for (int i = 0; i < p; i++)
        {
          sb.append(cbuff[i]);
        }
      }
      Iterator<Reader> r = jalview.ext.paradise.Annotate3D
              .getRNAMLForPDBFileAsString(sb.toString());
      AlignmentI al = null;
      while (r.hasNext())
      {
        FileParse fp = new InputStreamParser(r.next(),
                source.getDataName());
        AlignmentI nal = new FormatAdapter().readFromFile(fp,
                FileFormat.Rnaml);
        if (al == null)
        {
          al = nal;
        }
        else
        {
          al.append(nal);
        }
      }
      return al;
    } catch (Throwable x)
    {
      if (x instanceof IOException)
      {
        throw ((IOException) x);
      }
      else
      {
        throw new IOException(MessageManager.getString(
                "exception.unexpected_handling_rnaml_translation_for_pdb"),
                x);
      }
    }
  }

  public Annotate3D(String path) throws InterruptedException
  {
    System.out.println("Annotate3D");
    try
    {
      // //URL url = new
      // URL("http://paradise-ibmc.u-strasbg.fr/webservices/annotate3d?data="+inFile);
      // System.out.println("Step1");
      // FileReader r = new FileReader(inFile);
      // BufferedReader in = new BufferedReader(r);
      // StringBuffer content = new StringBuffer();
      // System.out.println("Step2");
      // while(in.readLine()!=null){
      // content.append(in.readLine());
      // //System.out.println("Step3"+in.readLine());
      // }
      //
      // String data = URLEncoder.encode("data", "UTF-8") + "=" +
      // URLEncoder.encode(content.toString(), "UTF-8");
      // for (int i=0;i<data.length();i++)
      // {
      // System.out.print(data.charAt(i));
      // }

      // String data = "width=50&height=100";

      // // Send the request
      // FileReader r = new FileReader(path);
      // BufferedReader in = new BufferedReader(r);
      // StringBuffer content = new StringBuffer();
      // System.out.println("Step1");
      // while(in.readLine()!=null){
      // content.append(in.readLine());
      //
      // }
      // System.out.println("Step2");
      // String data = URLEncoder.encode("data", "UTF-8") + "=" +
      // URLEncoder.encode(content.toString(), "UTF-8");
      // System.out.println("Step2");
      // URL url = new
      // URL("http://paradise-ibmc.u-strasbg.fr/webservices/annotate3d?data="+data);
      // DataInputStream is = new DataInputStream(url.openStream());
      // String str;
      // while ((str = is.readLine()) != null) {
      // System.out.println(str);
      // //out.write(str);
      // }
      FileReader r = new FileReader(path);
      BufferedReader in = new BufferedReader(r);
      String content = "";
      String str;

      while ((str = in.readLine()) != null)
      {
        // System.out.println(str);

        content = content + str;
      }
      System.out.println("pdbfile=" + content.toString());
      System.out.println("capacité=" + content.length());
      String paramfile = URLEncoder.encode(content.toString(), "UTF-8");
      System.out.println("param=" + paramfile);
      URL url = new URL(
              "http://paradise-ibmc.u-strasbg.fr/webservices/annotate3d?data="
                      + content);
      BufferedReader is = new BufferedReader(
              new InputStreamReader(url.openStream()));
      String str4;
      while ((str4 = is.readLine()) != null)
      {
        System.out.println(str4);
        // out.write(str);
      }
      in.close();
      is.close();

      // HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      // connection.setRequestMethod("POST" );
      // connection.setRequestProperty("data", path );
      // //connection.setRequestProperty("nomDuChamp2", "valeurDuChamp2" );
      // BufferedReader input = new BufferedReader(new
      // InputStreamReader(connection.getInputStream()));
      // //DataInputStream input = new
      // DataInputStream(connection.getInputStream());
      // String c;
      // while((c=input.readLine())!=null){
      // System.out.print(c);
      // }
      // input.close();
      // BufferedReader in1 = new BufferedReader(is);

      // OutputStream out1 = null;
      // System.out.println("Step3");
      // BufferedWriter out = new BufferedWriter(new OutputStreamWriter(out1,
      // "temp.rnaml"));
      //
      // in.close();
      // out.close();

      // return;

      // System.out.println(data.length());
      // System.out.println("step2");
      // URL url = new
      // URL("http://paradise-ibmc.u-strasbg.fr/webservices/annotate3d?data="+data);
      // System.out.println("step3");
      // URLConnection conn = url.openConnection();
      // conn.setDoOutput(true);
      // OutputStreamWriter writer = new
      // OutputStreamWriter(conn.getOutputStream());

      // write parameters
      // writer.write(data);
      // writer.flush();

      // Get the response
      // StringBuffer answer = new StringBuffer();
      // //BufferedReader reader = new BufferedReader(new
      // InputStreamReader(conn.getInputStream()));
      // //String line;
      // while ((line = reader.readLine()) != null) {
      // answer.append(line);
      // System.out.println(line);
      // }
      // writer.close();
      // reader.close();

      // Output the response

    } catch (MalformedURLException ex)
    {
      ex.printStackTrace();
    } catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }

  // in = new BufferedReader(new InputStreamReader(url.openStream()));

  // String str;

  // out = new FileOutputStream("temp.rnaml");
  // out = new BufferedWriter(new FileWriter("temp.rnaml"));

  // while ((str = in.readLine()) != null) {
  // System.out.println(str);
  // out.write(str);
  // System.out.println(str);
  // in.close();

  // out.close();
  // } catch (MalformedURLException e) {
  // } catch (IOException e) {
  // }
  //
  // }

  // public BufferedWriter getReader()
  // {
  // System.out.println("The buffer");

  // return out;

  // }

}
