package jalview.bin.argparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jalview.bin.argparser.Arg.Opt;
import jalview.util.ChannelProperties;
import jalview.util.Platform;

public enum Arg
{

  // Initialising arguments (BOOTSTRAP)
  HELP(Type.HELP, "h", "Display basic help", Opt.UNARY, Opt.BOOTSTRAP,
          Opt.HASTYPE, Opt.MULTI),
  /*
   * Other --help-type Args will be added by the static block.
   */
  VERSION(Type.CONFIG, "v",
          "Display the version of "
                  + ChannelProperties.getProperty("app_name"),
          Opt.UNARY, Opt.BOOTSTRAP),
  HEADLESS(Type.CONFIG,
          "Run Jalview in headless mode. No GUI interface will be created and Jalview will quit after all arguments have been processed. "
                  + "Headless mode is assumed if an output file is to be generated, this can be overridden with --noheadless or --gui.",
          Opt.BOOLEAN, Opt.BOOTSTRAP),
  GUI(Type.CONFIG,
          "Do not run Jalview in headless mode.  This overrides the assumption of headless mode when an output file is to be generated.",
          Opt.UNARY, Opt.BOOTSTRAP),
  JABAWS(Type.CONFIG, "Set a different URL to connect to a JABAWS server.",
          Opt.STRING, Opt.BOOTSTRAP),
  NEWS(Type.CONFIG, "Show (or don't show) the news feed.", true,
          Opt.BOOLEAN, Opt.BOOTSTRAP),
  SPLASH(Type.CONFIG,
          "Show (or don't show) the About Jalview splash screen.", true,
          Opt.BOOLEAN, Opt.BOOTSTRAP),
  QUESTIONNAIRE(Type.CONFIG,
          "Show (or don't show) the questionnaire if one is available.",
          true, Opt.BOOLEAN, Opt.BOOTSTRAP),
  NOUSAGESTATS(Type.CONFIG, "Don't send initial launch usage stats.",
          Opt.UNARY, Opt.BOOTSTRAP),
  NOSTARTUPFILE(Type.CONFIG, "Don't show the default startup file.",
          Opt.UNARY, Opt.BOOTSTRAP),
  WEBSERVICEDISCOVERY(Type.CONFIG,
          "Attempt (or don't attempt) to connect to JABAWS web services.",
          true, Opt.BOOLEAN, Opt.BOOTSTRAP),
  PROPS(Type.CONFIG,
          "Use a file as the preferences file instead of the usual ~/"
                  + ChannelProperties.getProperty("preferences.filename")
                  + " file.",
          Opt.STRING, Opt.BOOTSTRAP),
  DEBUG(Type.CONFIG, "d", "Start Jalview in debug log level.", Opt.BOOLEAN,
          Opt.BOOTSTRAP),
  TRACE(Type.CONFIG, "Start Jalview in trace log level.", Opt.BOOLEAN,
          Opt.BOOTSTRAP, Opt.SECRET),
  QUIET(Type.CONFIG, "q",
          "Stop all output to STDOUT (after the Java Virtual Machine has started). Use ‑‑quiet a second time to stop all output to STDERR.",
          Opt.UNARY, Opt.MULTI, Opt.BOOTSTRAP),
  INITSUBSTITUTIONS(Type.CONFIG,
          "Set ‑‑substitutions to be initially enabled (or initially disabled).",
          true, Opt.BOOLEAN, Opt.BOOTSTRAP, Opt.NOACTION, Opt.SECRET),
  P(Type.CONFIG, "Set a Jalview preference value for this session.",
          Opt.PREFIXKEV, Opt.PRESERVECASE, Opt.STRING, Opt.BOOTSTRAP,
          Opt.MULTI, Opt.NOACTION, Opt.SECRET), // keep this secret for now.

  // Opening an alignment
  OPEN(Type.OPENING,
          "Opens one or more alignment files or URLs in new alignment windows.",
          Opt.STRING, Opt.LINKED, Opt.INCREMENTDEFAULTCOUNTER, Opt.MULTI,
          Opt.GLOB, Opt.ALLOWSUBSTITUTIONS, Opt.INPUT, Opt.STORED,
          Opt.PRIMARY),
  APPEND(Type.OPENING,
          "Appends one or more alignment files or URLs to the open alignment window (or opens a new alignment if none already open).",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.GLOB,
          Opt.ALLOWSUBSTITUTIONS, Opt.INPUT, Opt.PRIMARY),
  TITLE(Type.OPENING,
          "Specifies the title for the open alignment window as string.",
          Opt.STRING, Opt.LINKED),
  COLOUR(Type.OPENING, "color", // being a bit soft on the Americans!
          "Applies the colour scheme to the open alignment window. Valid values include:\n"
                  + "clustal,\n" + "blosum62,\n" + "pc-identity,\n"
                  + "zappo,\n" + "taylor,\n" + "gecos-flower,\n"
                  + "gecos-blossom,\n" + "gecos-sunset,\n"
                  + "gecos-ocean,\n" + "hydrophobic,\n"
                  + "helix-propensity,\n" + "strand-propensity,\n"
                  + "turn-propensity,\n" + "buried-index,\n"
                  + "nucleotide,\n" + "nucleotide-ambiguity,\n"
                  + "purine-pyrimidine,\n" + "rna-helices,\n"
                  + "t-coffee-scores,\n" + "sequence-id.\n"
                  +"\n"
                  + "Names of user defined colourschemes will also work,\n"
                 +"and jalview colourscheme specifications like\n"
                  +"--colour=\"D,E=red; K,R,H=0022FF; C,c=yellow\"",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  FEATURES(Type.OPENING, "Add a feature file or URL to the open alignment.",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.ALLOWSUBSTITUTIONS),
  TREE(Type.OPENING, "Add a tree file or URL to the open alignment.",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.ALLOWSUBSTITUTIONS),
  SORTBYTREE(Type.OPENING,
          "Enforces sorting (or not sorting) the open alignment in the order of an attached phylogenetic tree.",
          true, Opt.LINKED, Opt.BOOLEAN, Opt.ALLOWALL),
  ANNOTATIONS(Type.OPENING,
          "Add an annotations file or URL to the open alignment.",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.ALLOWSUBSTITUTIONS),
  SHOWANNOTATIONS(Type.OPENING,
          "Enforces showing (or not showing) alignment annotations.",
          Opt.BOOLEAN, Opt.LINKED, Opt.ALLOWALL),
  WRAP(Type.OPENING,
          "Enforces wrapped (or not wrapped) alignment formatting.",
          Opt.BOOLEAN, Opt.LINKED, Opt.ALLOWALL),
  NOSTRUCTURE(Type.OPENING,
          "Do not open or process any 3D structure in the ‑‑open or ‑‑append files.",
          Opt.UNARY, Opt.LINKED, Opt.ALLOWALL),

  // Adding a 3D structure
  STRUCTURE(Type.STRUCTURE,
          "Load a structure file or URL associated with a sequence in the open alignment.\n"
                  + "The sequence to be associated with can be specified with a following --seqid argument, or the subval modifier seqid=ID can be used. A subval INDEX can also be used to specify the INDEX-th sequence in the open alignment.",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.ALLOWSUBSTITUTIONS,
          Opt.PRIMARY),
  SEQID(Type.STRUCTURE,
          "Specify the sequence name for the preceding --structure to be associated with.",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.ALLOWSUBSTITUTIONS),
  PAEMATRIX(Type.STRUCTURE,
          "Add a PAE json matrix file to the preceding --structure.",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.ALLOWSUBSTITUTIONS),
  TEMPFAC(Type.STRUCTURE,
          "Set the type of temperature factor. Possible values are:\n"
                  + "default,\n" + "plddt.",
          Opt.STRING, Opt.LINKED),
  STRUCTUREVIEWER(Type.STRUCTURE,
          "Set the structure viewer to use to open the 3D structure file specified in previous --structure to name. Possible values of name are:\n"
                  + "none,\n" + "jmol,\n" + "chimera,\n" + "chimerax,\n"
                  + "pymol.",
          Opt.STRING, Opt.LINKED, Opt.MULTI),
  NOTEMPFAC(Type.STRUCTURE,
          "Do not show the temperature factor annotation for the preceding --structure.",
          Opt.UNARY, Opt.LINKED, Opt.ALLOWALL, Opt.SECRET), // keep this secret
                                                            // until it works!
  SHOWSSANNOTATIONS(Type.STRUCTURE, null, Opt.BOOLEAN, Opt.LINKED,
          Opt.ALLOWALL),

  // Outputting files
  IMAGE(Type.IMAGE,
          "Output an image of the open alignment window. Format is specified by the subval modifier, a following --type argument or guessed from the file extension. Valid formats/extensions are:\n"
                  + "svg,\n" + "png,\n" + "eps,\n" + "html,\n" + "biojs.",
          Opt.STRING, Opt.LINKED, Opt.ALLOWSUBSTITUTIONS, Opt.ALLOWALL,
          Opt.REQUIREINPUT, Opt.OUTPUTFILE, Opt.PRIMARY),
  TYPE(Type.IMAGE,
          "Set the image format for the preceding --image. Valid values are:\n"
                  + "svg,\n" + "png,\n" + "eps,\n" + "html,\n" + "biojs.",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  TEXTRENDERER(Type.IMAGE,
          "Sets whether text in a vector image format (SVG, HTML, EPS) should be rendered as text or vector line-art. Possible values are:\n"
                  + "text,\n" + "lineart.",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  SCALE(Type.IMAGE,
          "Sets a scaling for bitmap image format (PNG). Should be given as a floating point number. If used in conjunction with --width and --height then the smallest scaling will be used (scale, width and height provide bounds for the image).",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  WIDTH(Type.IMAGE,
          "Sets a width for bitmap image format (PNG) with the height maintaining the aspect ratio. Should be given as a positive integer. If used in conjunction with --scale and --height then the smallest scaling will be used (scale, width and height provide bounds for the image).",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  HEIGHT(Type.IMAGE,
          "Sets a height for bitmap image format (PNG) with the width maintaining the aspect ratio. Should be given as a positive integer. If used in conjunction with --scale and --width then the smallest scaling will be used (scale, width and height provide bounds for the image).",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  STRUCTUREIMAGE(Type.STRUCTUREIMAGE,
          "Export an image of a 3D structure opened in JMOL", Opt.STRING,
          Opt.LINKED, Opt.MULTI, Opt.OUTPUTFILE),
  STRUCTUREIMAGETYPE(Type.STRUCTUREIMAGE,
          "Set the structure image format for the preceding --structureimage. Valid values are:\n"
                  + "svg,\n" + "png,\n" + "eps,\n" + "html,\n" + "biojs.",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  STRUCTUREIMAGETEXTRENDERER(Type.STRUCTUREIMAGE,
          "Sets whether text in a vector structure image format (SVG, EPS) should be rendered as text or vector line-art. Possible values are:\n"
                  + "text,\n" + "lineart.",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  STRUCTUREIMAGESCALE(Type.STRUCTUREIMAGE,
          "Sets a scaling for bitmap structure image format (PNG). Should be given as a floating point number. If used in conjunction with --structureimagewidth and --structureimageheight then the smallest scaling will be used (structureimagescale, structureimagewidth and structureimageheight provide bounds for the structure image).",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  STRUCTUREIMAGEWIDTH(Type.STRUCTUREIMAGE,
          "Sets a width for bitmap structure image format (PNG) with the height maintaining the aspect ratio. Should be given as a positive integer. If used in conjunction with --structureimagescale and --structureimageheight then the smallest scaling will be used (structureimagescale, structureimagewidth and structureimageheight provide bounds for the structure image).",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  STRUCTUREIMAGEHEIGHT(Type.STRUCTUREIMAGE,
          "Sets a height for bitmap structure image format (PNG) with the width maintaining the aspect ratio. Should be given as a positive integer. If used in conjunction with --structureimagescale and --structureimagewidth then the smallest scaling will be used (structureimagescale, structureimagewidth and structureimageheight provide bounds for the structure image).",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),

  OUTPUT(Type.OUTPUT,
          "Export the open alignment to file filename. The format name is specified by the subval modifier format=name, a following --format name argument or guessed from the file extension. Valid format names (and file extensions) are:\n"
                  + "fasta (fa, fasta, mfa, fastq),\n" + "pfam (pfam),\n"
                  + "stockholm (sto, stk),\n" + "pir (pir),\n"
                  + "blc (blc),\n" + "amsa (amsa),\n" + "json (json),\n"
                  + "pileup (pileup),\n" + "msf (msf),\n"
                  + "clustal (aln),\n" + "phylip (phy),\n"
                  + "jalview (jvp, jar).",
          Opt.STRING, Opt.LINKED, Opt.ALLOWSUBSTITUTIONS, Opt.ALLOWALL,
          Opt.REQUIREINPUT, Opt.OUTPUTFILE, Opt.PRIMARY),
  FORMAT(Type.OUTPUT,
          "Sets the format for the preceding --output file. Valid formats are:\n"
                  + "fasta,\n" + "pfam,\n" + "stockholm,\n" + "pir,\n"
                  + "blc,\n" + "amsa,\n" + "json,\n" + "pileup,\n"
                  + "msf,\n" + "clustal,\n" + "phylip,\n" + "jalview.",
          Opt.STRING, Opt.LINKED, Opt.ALLOWALL),
  GROOVY(Type.PROCESS,
          "Process a groovy script in the file for the open alignment.",
          Opt.STRING, Opt.LINKED, Opt.MULTI, Opt.ALLOWSUBSTITUTIONS,
          Opt.ALLOWALL),
  BACKUPS(Type.OUTPUT,
          "Enable (or disable) writing backup files when saving an ‑‑output file. This applies to the current open alignment.  To apply to all ‑‑output and ‑‑image files, use after ‑‑all.",
          true, Opt.BOOLEAN, Opt.LINKED, Opt.ALLOWALL),
  OVERWRITE(Type.OUTPUT,
          "Enable (or disable) overwriting of output files without backups enabled. This applies to the current open alignment.  To apply to all ‑‑output and ‑‑image files, use after ‑‑all.",
          Opt.BOOLEAN, Opt.LINKED, Opt.ALLOWALL),
  CLOSE(Type.OPENING,
          "Close the current open alignment window. This occurs after other output arguments. This applies to the current open alignment.  To apply to all ‑‑output and ‑‑image files, use after ‑‑all.",
          Opt.UNARY, Opt.LINKED, Opt.ALLOWALL),

  // controlling flow of arguments
  NEW(Type.FLOW,
          "Move on to a new alignment window. This will ensure --append will start a new alignment window and other linked arguments will apply to the new alignment window.",
          Opt.UNARY, Opt.MULTI, Opt.NOACTION, Opt.INCREMENTDEFAULTCOUNTER),
  SUBSTITUTIONS(Type.FLOW,
          "The following argument values allow (or don't allow) subsituting filename parts. This is initially true. Valid substitutions are:\n"
                  + "{basename} - the filename-without-extension of the currently --opened file (or first --appended file),\n"
                  + "{dirname} - the directory (folder) name of the currently --opened file (or first --appended file),\n"
                  + "{argfilebasename} - the filename-without-extension of the current --argfile,\n"
                  + "{argfiledirname} - the directory (folder) name of the current --argfile,\n"
                  + "{n} - the value of the index counter (starting at 0).\n"
                  + "{++n} - increase and substitute the value of the index counter,\n"
                  + "{} - the value of the current alignment window default index.",
          true, Opt.BOOLEAN, Opt.MULTI, Opt.NOACTION),
  ARGFILE(Type.FLOW,
          "Open one or more files filename and read, line-by-line, as arguments to Jalview.\n"
                  + "Values in an argfile should be given with an equals sign (\"=\") separator with no spaces.\n"
                  + "Note that if you use one or more --argfile arguments then all other non-initialising arguments will be ignored.",
          Opt.STRING, Opt.MULTI, Opt.BOOTSTRAP, Opt.GLOB,
          Opt.ALLOWSUBSTITUTIONS),
  NPP(Type.FLOW, "n++",
          "Increase the index counter used in argument value substitutions.",
          Opt.UNARY, Opt.MULTI, Opt.NOACTION),
  ALL(Type.FLOW,
          "Apply the following output arguments to all sets of linked arguments.",
          Opt.BOOLEAN, Opt.MULTI, Opt.NOACTION),
  OPENED(Type.FLOW,
          "Apply the following output arguments to all of the last --open'ed set of linked arguments.",
          Opt.BOOLEAN, Opt.MULTI, Opt.NOACTION),
  QUIT(Type.FLOW,
          "After all files have been opened, appended and output, quit Jalview. In ‑‑headless mode this already happens.",
          Opt.UNARY),
  NOQUIT(Type.FLOW,
          "Secret arg to not quit after --headless mode for tests",
          Opt.UNARY, Opt.SECRET),
  ALLSTRUCTURES(Type.FLOW,
          "Apply the following 3D structure formatting arguments to all structures within the open alignment.",
          Opt.BOOLEAN, Opt.MULTI, Opt.NOACTION),

  // secret options
  TESTOUTPUT(Type.CONFIG,
          "Allow specific stdout information.  For testing purposes only.",
          Opt.UNARY, Opt.BOOTSTRAP, Opt.SECRET), // do not show this to the user
  SETPROP(Type.CONFIG, "Set an individual Java System property.",
          Opt.STRING, Opt.MULTI, Opt.BOOTSTRAP, Opt.SECRET), // not in use yet
  NIL(Type.FLOW,
          "This argument does nothing on its own, but can be used with linkedIds.",
          Opt.UNARY, Opt.LINKED, Opt.MULTI, Opt.NOACTION, Opt.SECRET),

  // private options (inserted during arg processing)
  SETARGFILE(Type.FLOW,
          "Sets the current value of the argfilename.  Inserted before argfilecontents.",
          Opt.UNARY, Opt.LINKED, Opt.STRING, Opt.MULTI, Opt.PRIVATE,
          Opt.NOACTION),
  UNSETARGFILE(Type.FLOW,
          "Unsets the current value of the argfilename.  Inserted after argfile contents.",
          Opt.UNARY, Opt.LINKED, Opt.MULTI, Opt.PRIVATE, Opt.NOACTION),

  // these last two have no purpose in the normal Jalview application but are
  // used by jalview.bin.Launcher to set memory settings. They are not used by
  // argparser but are here for Usage statement reasons.
  JVMMEMPC(Type.CONFIG,
          "Limit maximum heap size (memory) to PERCENT% of total physical memory detected. This defaults to 90 if total physical memory can be detected.\n"
                  + "The equals sign (\"=\") separator must be used with no spaces.",
          Opt.NOACTION, Opt.BOOTSTRAP, Opt.STRING, Opt.LAST),
  JVMMEMMAX(Type.CONFIG,
          "Limit maximum heap size (memory) to MAXMEMORY. MAXMEMORY can be specified in bytes, kilobytes(k), megabytes(m), gigabytes(g) or if you're lucky enough, terabytes(t). This defaults to 32g if total physical memory can be detected, or to 8g if total physical memory cannot be detected.\n"
                  + "The equals sign (\"=\") separator must be used with no spaces.",
          Opt.NOACTION, Opt.BOOTSTRAP, Opt.STRING, Opt.LAST),

  ;

  public static enum Opt
  {
    /*
     * A BOOLEAN Arg can be specified as --arg or --noarg to give true or false.
     * A default can be given with setOptions(bool, Opt....).
     * Use ArgParser.isSet(Arg) to see if this arg was not specified.
     */
    BOOLEAN("can be negated with " + ArgParser.DOUBLEDASH
            + ArgParser.NEGATESTRING + "..."),

    /*
     * A STRING Arg will take a value either through --arg=value or --arg value.
     */
    STRING("expects a value"),
    /*
     * A UNARY Arg is a boolean value, true if present, false if not.
     * Like BOOLEAN but without the --noarg option.
     */
    UNARY(null),
    /*
     * A MULTI Arg can be specified multiple times.
     * Multiple values are stored in the ArgValuesMap (along with their positional index) for each linkedId.
     */
    MULTI("can be specified multiple times"),
    /*
     * A Linked Arg can be linked to others through a --arg[linkedId] or --arg[linkedId]=value.
     * If no linkedId is specified then the current default linkedId will be used.
     */
    LINKED("is linked to an alignment"),
    /*
     * A NODUPLICATES Arg can only have one value (per linkedId).
     * The first value will be used and subsequent values ignored with a warning.
     */
    NODUPLICATEVALUES("cannot have the same value more than once"),
    /*
     * A BOOTSTRAP Arg value(s) can be determined at an earlier stage than non-BOOTSTRAP Args.
     * Substitutions do not happen in BOOTSTRAP Args and they cannot be linked or contain SubVals.
     * See jalview.bin.argparser.BootstrapArgs.
     */
    BOOTSTRAP("a configuration argument"),
    /*
     * A GLOB Arg can expand wildcard filename "globs" (e.g. path/* /filename*).
     * If the Arg value is given as --arg filename* then the shell will have expanded the glob already,
     * but if specified as --arg=filename* then the Java glob expansion method will be used
     * (see FileUtils.getFilenamesFromGlob()).
     * Note that this might be different from the shell expansion rules.
     */
    GLOB("can take multiple filenames with wildcards"),
    /*
     * A NOACTION Arg does not perform a data task,
     * usually used to control flow in ArgParser.parse(args).
     */
    NOACTION(null),
    /*
     * An ALLOWSUBSTITUTIONS Arg allows substitutions in its linkedId,
     * SubVals and values.
     */
    ALLOWSUBSTITUTIONS("values can use substitutions"),
    /*
     * A PRIVATE Arg is used internally, and cannot be specified by the user.
     */
    PRIVATE(null),
    /*
     * A SECRET Arg is used by development processes and although it can be set by the user,
     * it is not displayed to the user.
     */
    SECRET(null),
    /*
     * An ALLOWALL Arg can use the '*' linkedId to apply to all known linkedIds
     */
    ALLOWALL("can be used with " + ArgParser.DOUBLEDASH + "all"),
    /*
     * If an Arg has the INCREMENTDEFAULTCOUNTER option and the default linkedId is used,
     * the defaultLinkedIdCounter is incremented *first*.
     */
    INCREMENTDEFAULTCOUNTER("starts a new default alignment"),
    /*
     * An INPUT Arg counts as an input for REQUIREINPUT
     */
    INPUT(null),
    /*
     * A REQUIREINPUT Arg can only be applied via --all if there is an input
     * (i.e. --open or --append)
     */
    REQUIREINPUT(null),
    /*
     * An OUTPUTFILE Arg provides an output filename. With Opt.ALLOWALL *.ext is shorthand for
     * --all --output={basename}.ext
     */
    OUTPUTFILE("output file --headless will be assumed unless --gui used"),
    /*
     * A STORED Arg resets and creates a new set of "opened" linkedIds
     */
    STORED(null),
    /*
     * A HELP Arg is a --help type arg
     */
    HELP("provides a help statement"),
    /*
     * A PRIMARY Arg is the main Arg for its type
     */
    PRIMARY("is a primary argument for its type"),
    /*
     * A HASTYPE Arg can have an Arg.Type assigned to its ArgValue
     */
    HASTYPE(null),
    /*
     * A FIRST arg gets moved to appear first in the usage statement (within type)
     */
    FIRST(null),
    /*
     * A LAST arg gets moved to appear last in the usage statement (within type)
     */
    LAST(null),
    /*
     * After other args are checked, the following args can prefix a KEY=VALUE argument
     */
    PREFIXKEV("prefixes key=value"),
    /*
     * do not lowercase the name when getting the arg name or arg string
     */
    PRESERVECASE(null),
    //
    ;

    private String description;

    private Opt()
    {
      description = null;
    }

    private Opt(String description)
    {
      this.description = description;
    }

    public String description()
    {
      return description;
    }

  }

  public static enum Type
  {
    // Type restricts argument to certain usage output
    HELP, // --help
    CONFIG("arguments used to configure "
            + ChannelProperties.getProperty("app_name") + " from startup"),
    OPENING("arguments used to open and format alignments"),
    STRUCTURE("arguments used to add and format 3D structure data"),
    PROCESS("arguments used to process an alignment once opened"),
    OUTPUT("arguments used to save data from a processed alignment"),
    IMAGE("arguments used to export an image of an alignment"),
    STRUCTUREIMAGE("arguments used to export an image of an structure"),
    FLOW("arguments that control processing of the other arguments"), //
    ALL("all arguments"), // mostly just a place-holder for --help-all
    NONE, // mostly a place-holder for --help
    INVALID;

    private String description;

    private Type()
    {
      description = null;
    }

    private Type(String description)
    {
      this.description = description;
    }

    public String description()
    {
      return description;
    }
  }

  private final String[] argNames;

  private Opt[] argOptions;

  private boolean defaultBoolValue;

  private String description;

  private Type type;

  private Arg(Type type, String description, Opt... options)
  {
    this(type, null, description, false, options);
  }

  private Arg(Type type, String description, boolean defaultBoolean,
          Opt... options)
  {
    this(type, null, description, defaultBoolean, options);
  }

  private Arg(Type type, String alternativeName, String description,
          Opt... options)
  {
    this(type, alternativeName, description, false, options);
  }

  private Arg(Type type, String alternativeName, String description,
          boolean defaultBoolean, Opt... options)
  {
    this.type = type;
    this.description = description;
    this.defaultBoolValue = defaultBoolean;
    this.setOptions(options);
    this.argNames = alternativeName != null
            ? new String[]
            { this.getName(), alternativeName }
            : new String[]
            { this.getName() };
  }

  public String argString()
  {
    return argString(false);
  }

  public String negateArgString()
  {
    return argString(true);
  }

  private String argString(boolean negate)
  {
    StringBuilder sb = new StringBuilder(ArgParser.DOUBLEDASH);
    if (negate && hasOption(Opt.BOOLEAN))
      sb.append(ArgParser.NEGATESTRING);
    sb.append(getName());
    return sb.toString();
  }

  public String toLongString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getName()).append('.').append(this.name());
    sb.append('(');
    if (getNames().length > 0)
      sb.append('"');
    sb.append(String.join("\", \"", getNames()));
    if (getNames().length > 0)
      sb.append('"');
    sb.append(")\n");
    sb.append("\nType: " + type.name());
    sb.append("\nOpt: ");
    // map List<Opt> to List<String> for the String.join
    List<String> optList = Arrays.asList(argOptions).stream()
            .map(opt -> opt.name()).collect(Collectors.toList());
    sb.append(String.join(", ", optList));
    sb.append("\n");
    return sb.toString();
  }

  public String[] getNames()
  {
    return argNames;
  }

  public String getName()
  {
    String name = hasOption(Opt.PRESERVECASE) ? this.name()
            : this.name().toLowerCase(Locale.ROOT);
    return name.replace('_', '-');
  }

  @Override
  public final String toString()
  {
    return getName();
  }

  public boolean hasOption(Opt o)
  {
    if (argOptions == null)
      return false;
    for (Opt option : argOptions)
    {
      if (o == option)
        return true;
    }
    return false;
  }

  public boolean hasAllOptions(Opt... opts)
  {
    for (Opt o : opts)
    {
      if (!this.hasOption(o))
        return false;
    }
    return true;
  }

  protected Opt[] getOptions()
  {
    return argOptions;
  }

  protected void setOptions(Opt... options)
  {
    this.argOptions = options;
  }

  protected boolean getDefaultBoolValue()
  {
    return defaultBoolValue;
  }

  public Type getType()
  {
    return this.type;
  }

  protected String getDescription()
  {
    return description;
  }

  public static String booleanArgString(Arg a)
  {
    StringBuilder sb = new StringBuilder(a.argString());
    if (a.hasOption(Opt.BOOLEAN))
    {
      sb.append('/');
      sb.append(a.negateArgString());
    }
    return sb.toString();
  }

  public static final String usage()
  {
    return usage(null);
  }

  public static final void appendUsageGeneral(StringBuilder sb,
          int maxArgLength)
  {
    for (Type t : EnumSet.allOf(Type.class))
    {
      if (t.description() != null)
      {
        StringBuilder argSb = new StringBuilder();
        argSb.append(Arg.HELP.argString()).append(ArgParser.SINGLEDASH)
                .append(t.name().toLowerCase(Locale.ROOT));
        appendArgAndDescription(sb, argSb.toString(),
                "Help for " + t.description(), null, maxArgLength);
        sb.append(System.lineSeparator());
      }
    }
  }

  public static final String usage(List<Type> types)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("usage: jalview [" + Arg.HEADLESS.argString() + "] [["
            + Arg.OPEN.argString() + "/" + Arg.APPEND.argString()
            + "] file(s)] [args]");
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());

    if (types == null || types.contains(null))
    {
      // always show --help
      appendArgAndDescription(sb, null, "Display this basic help", Arg.HELP,
              DESCRIPTIONINDENT);
      sb.append(System.lineSeparator());

      appendUsageGeneral(sb, DESCRIPTIONINDENT);
    }
    else
    {
      List<Arg> args = argsSortedForDisplay(types);

      /*
       * just use a set maxArgLength of DESCRIPTIONINDENT
       
      int maxArgLength = 0;
      for (Arg a : args)
      {
        if (a.hasOption(Opt.PRIVATE) || a.hasOption(Opt.SECRET))
          continue;
      
        String argS = argDisplayString(a);
        if (argS.length() > maxArgLength)
          maxArgLength = argS.length();
      }
      */
      int maxArgLength = DESCRIPTIONINDENT;

      // always show --help
      appendArgAndDescription(sb, null, null, Arg.HELP, maxArgLength);
      sb.append(System.lineSeparator());

      if ((args.contains(Arg.HELP) && types.contains(Type.ALL)))
      {
        appendUsageGeneral(sb, maxArgLength);
      }

      Iterator<Arg> argsI = args.iterator();
      Type typeSection = null;
      while (argsI.hasNext())
      {
        Arg a = argsI.next();

        if (a.hasOption(Opt.PRIVATE) || a.hasOption(Opt.SECRET)
                || a == Arg.HELP)
        {
          continue;
        }

        if (a.getType() != typeSection)
        {
          typeSection = a.getType();
          String typeDescription = a.getType().description();
          if (typeDescription != null && typeDescription.length() > 0)
          {
            // typeDescription = typeDescription.substring(0,
            // 1).toUpperCase(Locale.ROOT) + typeDescription.substring(1);
            typeDescription = typeDescription.toUpperCase(Locale.ROOT);
            sb.append(typeDescription);
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
          }
        }

        appendArgUsage(sb, a, maxArgLength, Platform.consoleWidth());

        if (argsI.hasNext())
        {
          sb.append(System.lineSeparator());
        }
      }
    }
    return sb.toString();
  }

  private static void appendArgUsage(StringBuilder sb, Arg a,
          int maxArgLength, int maxWidth)
  {
    boolean first = appendArgAndDescription(sb, null, null, a,
            maxArgLength);
    List<String> options = new ArrayList<>();

    for (Opt o : EnumSet.allOf(Opt.class))
    {
      if (a.hasOption(o) && o.description() != null)
      {
        options.add(o.description());
      }
    }

    final String optDisplaySeparator = "; ";
    if (options.size() > 0)
    {
      int linelength = 0;
      String spacing = String.format("%-"
              + (maxArgLength + ARGDESCRIPTIONSEPARATOR.length()) + "s",
              "");
      if (first)
      {
        sb.append(ARGDESCRIPTIONSEPARATOR);
        linelength += maxArgLength + ARGDESCRIPTIONSEPARATOR.length();
      }
      else
      {
        sb.append(spacing);
        linelength += spacing.length();
      }
      if (options.size() > 0)
      {
        boolean optFirst = true;
        Iterator<String> optionsI = options.listIterator();
        while (optionsI.hasNext())
        {
          String desc = optionsI.next();
          if (optFirst)
          {
            sb.append("(");
            linelength += 1;
          }
          int descLength = desc.length()
                  + (optionsI.hasNext() ? optDisplaySeparator.length() : 0);
          if (linelength + descLength > maxWidth)
          {
            sb.append(System.lineSeparator());
            linelength = 0;
            sb.append(spacing);
            linelength += spacing.length();
          }
          // sb.append(linelength + "+" + desc.length() + " ");
          sb.append(desc);
          linelength += desc.length();
          if (optionsI.hasNext())
          {
            sb.append(optDisplaySeparator);
            linelength += optDisplaySeparator.length();
          }
          optFirst = false;
        }
        sb.append(')');
        sb.append(System.lineSeparator());
      }
    }
  }

  public static String argDisplayString(Arg a)
  {
    StringBuilder argSb = new StringBuilder();
    argSb.append(
            a.hasOption(Opt.BOOLEAN) ? booleanArgString(a) : a.argString());
    if (a.hasOption(Opt.STRING))
    {
      if (a.hasOption(Opt.PREFIXKEV))
      {
        argSb.append("key=value");
      }
      else
      {
        argSb.append("=value");
      }
    }
    return argSb.toString();
  }

  public static boolean appendArgAndDescription(StringBuilder sb,
          String aString, String description, Arg a, int maxArgLength)
  {
    return appendArgAndDescription(sb, aString, description, a,
            maxArgLength, Platform.consoleWidth());
  }

  public static boolean appendArgAndDescription(StringBuilder sb,
          String aString, String description, Arg a, int maxArgLength,
          int maxLength)
  {
    if (aString == null && a != null)
    {
      aString = argDisplayString(a);
    }
    if (description == null && a != null)
    {
      description = a.getDescription();
    }
    sb.append(String.format("%-" + maxArgLength + "s", aString));
    if (aString.length() > maxArgLength)
    {
      sb.append(System.lineSeparator());
      sb.append(String.format("%-" + maxArgLength + "s", ""));
    }

    int descLength = maxLength - maxArgLength
            - ARGDESCRIPTIONSEPARATOR.length();
    // reformat the descriptions lines to the right width
    Iterator<String> descLines = null;
    if (description != null)
    {
      descLines = Arrays.stream(description.split("\\n")).iterator();
    }
    List<String> splitDescLinesList = new ArrayList<>();
    while (descLines != null && descLines.hasNext())
    {
      String line = descLines.next();
      while (line.length() > descLength)
      {
        int splitIndex = line.lastIndexOf(" ", descLength);
        splitDescLinesList.add(line.substring(0, splitIndex));
        line = line.substring(splitIndex + 1);
      }
      splitDescLinesList.add(line);
    }

    Iterator<String> splitDescLines = splitDescLinesList.iterator();
    boolean first = true;
    if (splitDescLines != null)
    {
      while (splitDescLines.hasNext())
      {
        if (first)
        {
          sb.append(ARGDESCRIPTIONSEPARATOR);
        }
        else
        {
          sb.append(String.format("%-"
                  + (maxArgLength + ARGDESCRIPTIONSEPARATOR.length()) + "s",
                  ""));
        }
        sb.append(splitDescLines.next());
        sb.append(System.lineSeparator());
        first = false;
      }
    }
    return first;
  }

  protected static Iterator<Arg> getAllOfType(Type type)
  {
    return getAllOfType(type, new Opt[] {});
  }

  protected static Iterator<Arg> getAllOfType(Type type, Opt... options)
  {
    Opt[] opts = options == null ? new Opt[] {} : options;
    return EnumSet.allOf(Arg.class).stream().filter(a -> {
      if (a.getType() != type)
        return false;
      for (Opt o : opts)
      {
        if (!a.hasOption(o))
          return false;
      }
      return true;
    }).iterator();
  }

  private static List<Arg> argsSortedForDisplay(List<Type> types)
  {
    List<Arg> argsToSort;
    // if no types provided, do all
    if (types == null || types.size() == 0 || types.contains(Type.ALL))
    {
      argsToSort = Arrays
              .asList(EnumSet.allOf(Arg.class).toArray(new Arg[] {}));
    }
    else
    {
      argsToSort = new ArrayList<>();
      for (Type type : types)
      {
        if (type == null)
          continue;
        Arg.getAllOfType(type).forEachRemaining(a -> argsToSort.add(a));
      }
    }

    Collections.sort(argsToSort, new ArgDisplayComparator());
    return argsToSort;
  }

  private static final String ARGDESCRIPTIONSEPARATOR = " - ";

  private static final int DESCRIPTIONINDENT = 20;

}

class ArgDisplayComparator implements Comparator<Arg>
{
  private int compareArgOpts(Arg a, Arg b, Opt o)
  {
    int i = a.hasOption(o) ? (b.hasOption(o) ? 0 : -1)
            : (b.hasOption(o) ? 1 : 0);
    return i;
  }

  private int compareForDisplay(Arg a, Arg b)
  {
    if (b == null)
      return -1;
    // first compare types (in enum order)
    int i = a.getType().compareTo(b.getType());
    if (i != 0)
      return i;
    // do Opt.LAST next (oddly). Reversed args important!
    i = compareArgOpts(b, a, Opt.LAST);
    if (i != 0)
      return i;
    // priority order
    Opt[] optOrder = { Opt.HELP, Opt.FIRST, Opt.PRIMARY, Opt.STRING,
        Opt.BOOLEAN };
    for (Opt o : optOrder)
    {
      i = compareArgOpts(a, b, o);
      if (i != 0)
        return i;
    }
    // finally order of appearance in enum declarations
    return a.compareTo(b);
  }

  @Override
  public int compare(Arg a, Arg b)
  {
    return compareForDisplay(a, b);
  }
}