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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import jalview.api.AlignExportSettingsI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureSettingsModelI;
import jalview.bin.Console;
import jalview.util.MessageManager;
import jalview.util.Platform;

/**
 * implements a random access wrapper around a particular datasource, for
 * passing to identifyFile and AlignFile objects.
 */
public class FileParse
{
  protected static final String SPACE = " ";

  protected static final String TAB = "\t";

  /**
   * text specifying source of data. usually filename or url.
   */
  private String dataName = "unknown source";

  public File inFile = null;

  private byte[] bytes; // from JavaScript

  public byte[] getBytes()
  {
    return bytes;
  }

  /**
   * a viewport associated with the current file operation. May be null. May
   * move to different object.
   */
  private AlignViewportI viewport;

  /**
   * specific settings for exporting data from the current context
   */
  private AlignExportSettingsI exportSettings;

  /**
   * sequence counter for FileParse object created from same data source
   */
  public int index = 1;

  /**
   * separator for extracting specific 'frame' of a datasource for formats that
   * support multiple records (e.g. BLC, Stockholm, etc)
   */
  protected char suffixSeparator = '#';

  /**
   * character used to write newlines
   */
  protected String newline = System.getProperty("line.separator");

  public void setNewlineString(String nl)
  {
    newline = nl;
  }

  public String getNewlineString()
  {
    return newline;
  }

  /**
   * '#' separated string tagged on to end of filename or url that was clipped
   * off to resolve to valid filename
   */
  protected String suffix = null;

  protected DataSourceType dataSourceType = null;

  protected BufferedReader dataIn = null;

  protected String errormessage = "UNINITIALISED SOURCE";

  protected boolean error = true;

  protected String warningMessage = null;

  /**
   * size of readahead buffer used for when initial stream position is marked.
   */
  final int READAHEAD_LIMIT = 2048;

  public FileParse()
  {
  }

  /**
   * Create a new FileParse instance reading from the same datasource starting
   * at the current position. WARNING! Subsequent reads from either object will
   * affect the read position of the other, but not the error state.
   * 
   * @param from
   */
  public FileParse(FileParse from) throws IOException
  {
    if (from == null)
    {
      throw new Error(MessageManager
              .getString("error.implementation_error_null_fileparse"));
    }
    if (from == this)
    {
      return;
    }
    index = ++from.index;
    inFile = from.inFile;
    suffixSeparator = from.suffixSeparator;
    suffix = from.suffix;
    errormessage = from.errormessage; // inherit potential error messages
    error = false; // reset any error condition.
    dataSourceType = from.dataSourceType;
    dataIn = from.dataIn;
    if (dataIn != null)
    {
      mark();
    }
    dataName = from.dataName;
  }

  /**
   * Attempt to open a file as a datasource. Sets error and errormessage if
   * fileStr was invalid.
   * 
   * @param fileStr
   * @return this.error (true if the source was invalid)
   */
  private boolean checkFileSource(String fileStr) throws IOException
  {
    error = false;
    this.inFile = new File(fileStr);
    // check to see if it's a Jar file in disguise.
    if (!inFile.exists())
    {
      errormessage = "FILE NOT FOUND";
      error = true;
    }
    if (!inFile.canRead())
    {
      errormessage = "FILE CANNOT BE OPENED FOR READING";
      error = true;
    }
    if (inFile.isDirectory())
    {
      // this is really a 'complex' filetype - but we don't handle directory
      // reads yet.
      errormessage = "FILE IS A DIRECTORY";
      error = true;
    }
    if (!error)
    {
      try
      {
        dataIn = checkForGzipStream(new FileInputStream(fileStr));
        dataName = fileStr;
      } catch (Exception x)
      {
        warningMessage = "Failed to resolve " + fileStr
                + " as a data source. (" + x.getMessage() + ")";
        // x.printStackTrace();
        error = true;
      }
      ;
    }
    return error;
  }

  /**
   * Recognise the 2-byte magic header for gzip streams
   * 
   * https://recalll.co/ask/v/topic/java-How-to-check-if-InputStream-is-Gzipped/555aadd62bd27354438b90f6
   * 
   * @param bytes
   *          - at least two bytes
   * @return
   * @throws IOException
   */
  public static boolean isGzipStream(InputStream input) throws IOException
  {
    if (!input.markSupported())
    {
      Console.error(
              "FileParse.izGzipStream: input stream must support mark/reset");
      return false;
    }
    input.mark(4);

    // get first 2 bytes or return false
    byte[] bytes = new byte[2];
    int read = input.read(bytes);
    input.reset();
    if (read != bytes.length)
    {
      return false;
    }

    int header = (bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
    return (GZIPInputStream.GZIP_MAGIC == header);
  }

  /**
   * Returns a Reader for the given input after wrapping it in a buffered input
   * stream, and then checking if it needs to be wrapped by a GZipInputStream
   * 
   * @param input
   * @return
   */
  private BufferedReader checkForGzipStream(InputStream input)
          throws Exception
  {
    // NB: stackoverflow
    // https://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped
    // could use a PushBackInputStream rather than a BufferedInputStream
    if (!input.markSupported())
    {
      input = new BufferedInputStream(input, 16);
    }
    if (isGzipStream(input))
    {
      return getGzipReader(input);
    }
    // return a buffered reader for the stream.
    InputStreamReader isReader = new InputStreamReader(input);
    BufferedReader toReadFrom = new BufferedReader(isReader);
    return toReadFrom;
  }

  /**
   * Returns a {@code BufferedReader} which wraps the input stream with a
   * GZIPInputStream. Throws a {@code ZipException} if a GZIP format error
   * occurs or the compression method used is unsupported.
   * 
   * @param inputStream
   * @return
   * @throws Exception
   */
  private BufferedReader getGzipReader(InputStream inputStream)
          throws Exception
  {
    BufferedReader inData = new BufferedReader(
            new InputStreamReader(new GZIPInputStream(inputStream)));
    inData.mark(2048);
    inData.read();
    inData.reset();
    return inData;
  }

  /**
   * Tries to read from the given URL. If successful, saves a reader to the
   * response in field {@code dataIn}, otherwise (on exception, or HTTP response
   * status not 200), throws an exception.
   * <p>
   * If the response status includes
   * 
   * <pre>
   * Content-Type : application/x-gzip
   * </pre>
   * 
   * then tries to read as gzipped content.
   * 
   * @param urlStr
   * @throws IOException
   * @throws MalformedURLException
   */
  private void checkURLSource(String urlStr)
          throws IOException, MalformedURLException
  {
    errormessage = "URL NOT FOUND";
    URL url = new URL(urlStr);
    URLConnection _conn = url.openConnection();
    if (_conn instanceof HttpURLConnection)
    {
      HttpURLConnection conn = (HttpURLConnection) _conn;
      int rc = conn.getResponseCode();
      if (rc != HttpURLConnection.HTTP_OK)
      {
        throw new IOException(
                "Response status from " + urlStr + " was " + rc);
      }
    }
    else
    {
      try
      {
        dataIn = checkForGzipStream(_conn.getInputStream());
        dataName = urlStr;
      } catch (IOException ex)
      {
        throw new IOException("Failed to handle non-HTTP URI stream", ex);
      } catch (Exception ex)
      {
        throw new IOException(
                "Failed to determine type of input stream for given URI",
                ex);
      }
      return;
    }
    String encoding = _conn.getContentEncoding();
    String contentType = _conn.getContentType();
    boolean isgzipped = "application/x-gzip".equalsIgnoreCase(contentType)
            || contentType.endsWith("gzip") || "gzip".equals(encoding);
    Exception e = null;
    InputStream inputStream = _conn.getInputStream();
    if (isgzipped)
    {
      try
      {
        dataIn = getGzipReader(inputStream);
        dataName = urlStr;
      } catch (Exception e1)
      {
        throw new IOException(MessageManager
                .getString("exception.failed_to_resolve_gzip_stream"), e);
      }
      return;
    }

    dataIn = new BufferedReader(new InputStreamReader(inputStream));
    dataName = urlStr;
    return;
  }

  /**
   * sets the suffix string (if any) and returns remainder (if suffix was
   * detected)
   * 
   * @param fileStr
   * @return truncated fileStr or null
   */
  private String extractSuffix(String fileStr)
  {
    // first check that there wasn't a suffix string tagged on.
    int sfpos = fileStr.lastIndexOf(suffixSeparator);
    if (sfpos > -1 && sfpos < fileStr.length() - 1)
    {
      suffix = fileStr.substring(sfpos + 1);
      // System.err.println("DEBUG: Found Suffix:"+suffix);
      return fileStr.substring(0, sfpos);
    }
    return null;
  }

  /**
   * not for general use, creates a fileParse object for an existing reader with
   * configurable values for the origin and the type of the source
   */
  public FileParse(BufferedReader source, String originString,
          DataSourceType sourceType)
  {
    dataSourceType = sourceType;
    error = false;
    inFile = null;
    dataName = originString;
    dataIn = source;
    try
    {
      if (dataIn.markSupported())
      {
        dataIn.mark(READAHEAD_LIMIT);
      }
    } catch (IOException q)
    {

    }
  }

  /**
   * Create a datasource for input to Jalview. See AppletFormatAdapter for the
   * types of sources that are handled.
   * 
   * @param file
   *          - datasource locator/content as File or String
   * @param sourceType
   *          - protocol of source
   * @throws MalformedURLException
   * @throws IOException
   */
  public FileParse(Object file, DataSourceType sourceType)
          throws MalformedURLException, IOException
  {
    if (file instanceof File)
    {
      parse((File) file, ((File) file).getPath(), sourceType, true);
    }
    else
    {
      parse(null, file.toString(), sourceType, false);
    }
  }

  private void parse(File file, String fileStr, DataSourceType sourceType,
          boolean isFileObject) throws IOException
  {
    bytes = Platform.getFileBytes(file);
    dataSourceType = sourceType;
    error = false;

    if (sourceType == DataSourceType.FILE)
    {

      if (bytes != null)
      {
        // this will be from JavaScript
        inFile = file;
        dataIn = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(bytes)));
        dataName = fileStr;
      }
      else if (checkFileSource(fileStr))
      {
        String suffixLess = extractSuffix(fileStr);
        if (suffixLess != null)
        {
          if (checkFileSource(suffixLess))
          {
            throw new IOException(MessageManager.formatMessage(
                    "exception.problem_opening_file_also_tried",
                    new String[]
                    { inFile.getName(), suffixLess, errormessage }));
          }
        }
        else
        {
          throw new IOException(MessageManager.formatMessage(
                  "exception.problem_opening_file", new String[]
                  { inFile.getName(), errormessage }));
        }
      }
    }
    else if (sourceType == DataSourceType.RELATIVE_URL)
    {
      // BH 2018 hack for no support for access-origin
      bytes = Platform.getFileAsBytes(fileStr);
      dataIn = new BufferedReader(
              new InputStreamReader(new ByteArrayInputStream(bytes)));
      dataName = fileStr;

    }
    else if (sourceType == DataSourceType.URL)
    {
      try
      {
        try
        {
          checkURLSource(fileStr);
          if (suffixSeparator == '#')
          {
            extractSuffix(fileStr); // URL lref is stored for later reference.
          }
        } catch (IOException e)
        {
          String suffixLess = extractSuffix(fileStr);
          if (suffixLess == null)
          {
            throw (e);
          }
          else
          {
            try
            {
              checkURLSource(suffixLess);
            } catch (IOException e2)
            {
              errormessage = "BAD URL WITH OR WITHOUT SUFFIX";
              throw (e); // just pass back original - everything was wrong.
            }
          }
        }
      } catch (Exception e)
      {
        errormessage = "CANNOT ACCESS DATA AT URL '" + fileStr + "' ("
                + e.getMessage() + ")";
        error = true;
      }
    }
    else if (sourceType == DataSourceType.PASTE)
    {
      errormessage = "PASTE INACCESSIBLE!";
      dataIn = new BufferedReader(new StringReader(fileStr));
      dataName = "Paste";
    }
    else if (sourceType == DataSourceType.CLASSLOADER)
    {
      errormessage = "RESOURCE CANNOT BE LOCATED";
      InputStream is = getClass().getResourceAsStream("/" + fileStr);
      if (is == null)
      {
        String suffixLess = extractSuffix(fileStr);
        if (suffixLess != null)
        {
          is = getClass().getResourceAsStream("/" + suffixLess);
        }
      }
      if (is != null)
      {
        dataIn = new BufferedReader(new InputStreamReader(is));
        dataName = fileStr;
      }
      else
      {
        error = true;
      }
    }
    else
    {
      errormessage = "PROBABLE IMPLEMENTATION ERROR : Datasource Type given as '"
              + (sourceType != null ? sourceType : "null") + "'";
      error = true;
    }
    if (dataIn == null || error)
    {
      // pass up the reason why we have no source to read from
      throw new IOException(MessageManager.formatMessage(
              "exception.failed_to_read_data_from_source", new String[]
              { errormessage }));
    }
    error = false;
    dataIn.mark(READAHEAD_LIMIT);
  }

  /**
   * mark the current position in the source as start for the purposes of it
   * being analysed by IdentifyFile().identify
   * 
   * @throws IOException
   */
  public void mark() throws IOException
  {
    if (dataIn != null)
    {
      dataIn.mark(READAHEAD_LIMIT);
    }
    else
    {
      throw new IOException(
              MessageManager.getString("exception.no_init_source_stream"));
    }
  }

  public String nextLine() throws IOException
  {
    if (!error)
    {
      return dataIn.readLine();
    }
    throw new IOException(MessageManager
            .formatMessage("exception.invalid_source_stream", new String[]
            { errormessage }));
  }

  /**
   * 
   * @return true if this FileParse is configured for Export only
   */
  public boolean isExporting()
  {
    return !error && dataIn == null;
  }

  /**
   * 
   * @return true if the data source is valid
   */
  public boolean isValid()
  {
    return !error;
  }

  /**
   * closes the datasource and tidies up. source will be left in an error state
   */
  public void close() throws IOException
  {
    errormessage = "EXCEPTION ON CLOSE";
    error = true;
    dataIn.close();
    dataIn = null;
    errormessage = "SOURCE IS CLOSED";
  }

  /**
   * Rewinds the datasource to the marked point if possible
   * 
   * @param bytesRead
   * 
   */
  public void reset(int bytesRead) throws IOException
  {
    if (bytesRead >= READAHEAD_LIMIT)
    {
      System.err.println(String.format(
              "File reset error: read %d bytes but reset limit is %d",
              bytesRead, READAHEAD_LIMIT));
    }
    if (dataIn != null && !error)
    {
      dataIn.reset();
    }
    else
    {
      throw new IOException(MessageManager.getString(
              "error.implementation_error_reset_called_for_invalid_source"));
    }
  }

  /**
   * 
   * @return true if there is a warning for the user
   */
  public boolean hasWarningMessage()
  {
    return (warningMessage != null && warningMessage.length() > 0);
  }

  /**
   * 
   * @return empty string or warning message about file that was just parsed.
   */
  public String getWarningMessage()
  {
    return warningMessage;
  }

  public String getInFile()
  {
    if (inFile != null)
    {
      return inFile.getAbsolutePath() + " (" + index + ")";
    }
    else
    {
      return "From Paste + (" + index + ")";
    }
  }

  /**
   * @return the dataName
   */
  public String getDataName()
  {
    return dataName;
  }

  /**
   * set the (human readable) name or URI for this datasource
   * 
   * @param dataname
   */
  protected void setDataName(String dataname)
  {
    dataName = dataname;
  }

  /**
   * get the underlying bufferedReader for this data source.
   * 
   * @return null if no reader available
   * @throws IOException
   */
  public Reader getReader()
  {
    if (dataIn != null) // Probably don't need to test for readiness &&
                        // dataIn.ready())
    {
      return dataIn;
    }
    return null;
  }

  public AlignViewportI getViewport()
  {
    return viewport;
  }

  public void setViewport(AlignViewportI viewport)
  {
    this.viewport = viewport;
  }

  /**
   * @return the currently configured exportSettings for writing data.
   */
  public AlignExportSettingsI getExportSettings()
  {
    return exportSettings;
  }

  /**
   * Set configuration for export of data.
   * 
   * @param exportSettings
   *          the exportSettings to set
   */
  public void setExportSettings(AlignExportSettingsI exportSettings)
  {
    this.exportSettings = exportSettings;
  }

  /**
   * method overridden by complex file exporter/importers which support
   * exporting visualisation and layout settings for a view
   * 
   * @param avpanel
   */
  public void configureForView(AlignmentViewPanel avpanel)
  {
    if (avpanel != null)
    {
      setViewport(avpanel.getAlignViewport());
    }
    // could also set export/import settings
  }

  /**
   * Returns the preferred feature colour configuration if there is one, else
   * null
   * 
   * @return
   */
  public FeatureSettingsModelI getFeatureColourScheme()
  {
    return null;
  }

  public DataSourceType getDataSourceType()
  {
    return dataSourceType;
  }

  /**
   * Returns a buffered reader for the input object. Returns null, or throws
   * IOException, on failure.
   * 
   * @param file
   *          a File, or a String which is a name of a file
   * @param sourceType
   * @return
   * @throws IOException
   */
  public BufferedReader getBufferedReader(Object file,
          DataSourceType sourceType) throws IOException
  {
    BufferedReader in = null;
    byte[] bytes;

    switch (sourceType)
    {
    case FILE:
      if (file instanceof String)
      {
        return new BufferedReader(new FileReader((String) file));
      }
      bytes = Platform.getFileBytes((File) file);
      if (bytes != null)
      {
        return new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(bytes)));
      }
      return new BufferedReader(new FileReader((File) file));
    case URL:
      URL url = new URL(file.toString());
      in = new BufferedReader(new InputStreamReader(url.openStream()));
      break;
    case RELATIVE_URL: // JalviewJS only
      bytes = Platform.getFileAsBytes(file.toString());
      if (bytes != null)
      {
        in = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(bytes)));
      }
      break;
    case PASTE:
      in = new BufferedReader(new StringReader(file.toString()));
      break;
    case CLASSLOADER:
      InputStream is = getClass().getResourceAsStream("/" + file);
      if (is != null)
      {
        in = new BufferedReader(new InputStreamReader(is));
      }
      break;
    }

    return in;
  }
}
