import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

/**
 * A file reader that concatenates lines
 * 
 * @author gmcarstairs
 *
 */
public class BufferedLineReader
{
  interface LineCleaner
  {
    String cleanLine(String l);
  }

  /*
   * a reader for the file being read
   */
  private BufferedReader br;
  
  /*
   * optional handler to post-process each line as it is read
   */
  private LineCleaner cleaner;

  /*
   * current buffer of <bufferSize> post-processed input lines
   */
  private String[] buffer;

  private boolean atEof;

  /**
   * Constructor
   * 
   * @param reader
   * @param bufferSize
   *          the number of lines to concatenate at a time while reading
   * @param tidier
   *          an optional callback handler to post-process each line after
   *          reading
   * @throws FileNotFoundException
   */
  public BufferedLineReader(BufferedReader reader, int bufferSize,
          LineCleaner tidier)
          throws IOException
  {
    br = reader;
    buffer = new String[bufferSize];
    cleaner = tidier;

    /*
     * load up the buffer with N-1 lines, ready for the first read
     */
    for (int i = 1; i < bufferSize; i++)
    {
      readLine();
    }

  }

  /**
   * Reads the next line from file, invokes the post-processor if one was
   * provided, and returns the 'cleaned' line, or null at end of file.
   * 
   * @return
   */
  private String readLine() // throws IOException
  {
    if (atEof)
    {
      return null;
    }

    String line = null;
    try
    {
      line = br.readLine();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    if (line == null)
    {
      atEof = true;
      return null;
    }
    if (cleaner != null)
    {
      line = cleaner.cleanLine(line);
    }

    /*
     * shuffle down the lines buffer and add the new line
     * in the last position
     */
    for (int i = 1; i < buffer.length; i++)
    {
      buffer[i - 1] = buffer[i];
    }
    buffer[buffer.length - 1] = line;
    return line;
  }

  /**
   * Returns a number of concatenated lines from the file, or null at end of
   * file.
   * 
   * @return
   */
  public String read()
  {
    if (readLine() == null)
    {
      return null;
    }
    StringBuilder result = new StringBuilder(100 * buffer.length);
    for (String line : buffer)
    {
      if (line != null)
      {
        result.append(line);
      }
    }
    return result.toString();
  }

  /**
   * A main 'test' method!
   * 
   * @throws IOException
   */
  public static void main(String[] args) throws IOException
  {
    String data = "Now is the winter\n" + "Of our discontent\n"
            + "Made glorious summer\n" + "By this sun of York\n";
    BufferedReader br = new BufferedReader(new StringReader(data));
    BufferedLineReader reader = new BufferedLineReader(br, 3,
            new LineCleaner()
            {
              @Override
              public String cleanLine(String l)
              {
                return l.toUpperCase();
              }
            });
    String line = reader.read();
    String expect = "NOW IS THE WINTEROF OUR DISCONTENTMADE GLORIOUS SUMMER";
    if (!line.equals(expect))
    {
      System.err.println("Fail: expected '" + expect + "', found '" + line
              + ";");
    }
    else
    {
      System.out.println("Line one ok!");
    }
    line = reader.read();
    expect = "OF OUR DISCONTENTMADE GLORIOUS SUMMERBY THIS SUN OF YORK";
    if (!line.equals(expect))
    {
      System.err.println("Fail: expected '" + expect + "', found '" + line
              + "'");
    }
    else
    {
      System.out.println("Line two ok!!");
    }
    line = reader.read();
    if (line != null)
    {
      System.err.println("Fail: expected null at eof, got '" + line + "'");
    }
    else
    {
      System.out.println("EOF ok!!!");
    }
  }
}
