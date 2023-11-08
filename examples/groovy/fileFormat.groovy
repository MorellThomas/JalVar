import jalview.datamodel.AlignmentI
import jalview.datamodel.SequenceI
import jalview.datamodel.Sequence
import jalview.io.FileFormatI
import jalview.io.FileFormats
import jalview.io.FileParse
import jalview.io.AlignFile

/*
 * Example script that registers a new alignment file format 
 * consisting of lines like
 * !I=<sequence id>
 * !S=<sequence string>
 */

/*
 * A parser class to read or write the format
 */
class MyParser extends AlignFile 
{
  /*
   * Constructor for reading a file; the superclass
   * constructor will call the parse() method
   */
  MyParser(FileParse src) 
  {
    super(src)
  }

  /*
   * Constructor for writing out an alignment
   */
  MyParser(AlignmentI al) 
  {
  }
  
  /*
   * Parse a formatted data file (with no error checking!)
   */
  void parse() 
  {
    String id
    String line
    while ((line = nextLine()) != null) 
    {
      if (line.startsWith('!I='))
      {
        int pos = line.indexOf('/')
        id = line.substring(3, pos == -1 ? line.length() : pos)
      } else if (line.startsWith('!S='))
      {
        String seq = line.substring(3)
        addSequence(new Sequence(id, seq))
      }
    }
  }
  
  /*
   * Print the formatted sequences
   * (addSuffix always defaults to true as no user preference for it)
   */
  String print(SequenceI[] seqs, boolean addSuffix) 
  {
      StringBuilder sb = new StringBuilder()
      for (SequenceI seq : seqs) 
      {
          sb.append('!I=').append(seq.getDisplayId(addSuffix)).append('\n')
          sb.append('!S=').append(seq.getSequenceAsString()).append('\n')
      }
      sb.toString()
  }
}

/*
 * A closure that defines the 'Groovy example' file format,
 * delegating to MyParser for reading and writing
 */
def myFormat = { ->
  [
    getName: { -> 'Groovy example' },
    
    toString: { -> getName() },
    
    getExtensions: { -> 'grv' },
    
    getReader: { FileParse source -> new MyParser(source) },
    
    getWriter: { AlignmentI al -> new MyParser(al) },
    
    isReadable: { -> true },
    
    isWritable: { -> true },
    
    isTextFormat: { -> true },
    
    isStructureFile: { -> false },
    
    isComplexAlignFile: { -> false },

   ] as FileFormatI
}

/*
 * Register the file format. After running this script in Jalview's
 * Groovy console, the new format should be shown in open file,
 * save file, and output to textbox menu options.
 */
FileFormats.instance.registerFileFormat(myFormat())
