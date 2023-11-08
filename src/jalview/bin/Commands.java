package jalview.bin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jalview.analysis.AlignmentUtils;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.argparser.Arg;
import jalview.bin.argparser.ArgParser;
import jalview.bin.argparser.ArgParser.Position;
import jalview.bin.argparser.ArgValue;
import jalview.bin.argparser.ArgValuesMap;
import jalview.bin.argparser.SubVals;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.datamodel.annotations.AlphaFoldAnnotationRowBuilder;
import jalview.gui.AlignFrame;
import jalview.gui.AlignmentPanel;
import jalview.gui.AppJmol;
import jalview.gui.Desktop;
import jalview.gui.Preferences;
import jalview.gui.StructureChooser;
import jalview.gui.StructureViewer;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.AppletFormatAdapter;
import jalview.io.BackupFiles;
import jalview.io.BioJsHTMLOutput;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileLoader;
import jalview.io.HtmlSvgOutput;
import jalview.io.IdentifyFile;
import jalview.io.NewickFile;
import jalview.io.exceptions.ImageOutputException;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.structure.StructureImportSettings.TFType;
import jalview.structure.StructureSelectionManager;
import jalview.util.FileUtils;
import jalview.util.HttpUtils;
import jalview.util.ImageMaker;
import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.imagemaker.BitmapImageSizing;

public class Commands
{
  Desktop desktop;

  private boolean headless;

  private ArgParser argParser;

  private Map<String, AlignFrame> afMap;

  private boolean commandArgsProvided = false;

  private boolean argsWereParsed = false;

  public Commands(ArgParser argparser, boolean headless)
  {
    this(Desktop.instance, argparser, headless);
  }

  public Commands(Desktop d, ArgParser argparser, boolean h)
  {
    argParser = argparser;
    headless = h;
    desktop = d;
    afMap = new HashMap<>();
    if (argparser != null)
    {
      processArgs(argparser, headless);
    }
  }

  private boolean processArgs(ArgParser argparser, boolean h)
  {
    argParser = argparser;
    headless = h;
    boolean theseArgsWereParsed = false;

    if (argParser != null && argParser.getLinkedIds() != null)
    {
      for (String id : argParser.getLinkedIds())
      {
        ArgValuesMap avm = argParser.getLinkedArgs(id);
        theseArgsWereParsed = true;
        theseArgsWereParsed &= processLinked(id);
        processGroovyScript(id);
        boolean processLinkedOkay = theseArgsWereParsed;
        
        // wait around until alignFrame isn't busy
        AlignFrame af=afMap.get(id);
        while (af!=null && af.getViewport().isCalcInProgress())
        {
          try {
            Thread.sleep(25);
          } catch (Exception q) {};
        }
        
        theseArgsWereParsed &= processImages(id);
        if (processLinkedOkay)
          theseArgsWereParsed &= processOutput(id);

        // close ap
        if (avm.getBoolean(Arg.CLOSE))
        {
          af = afMap.get(id);
          if (af != null)
          {
            af.closeMenuItem_actionPerformed(true);
          }
        }

      }

    }
    if (argParser.getBoolean(Arg.QUIT))
    {
      Jalview.getInstance().quit();
      return true;
    }
    // carry on with jalview.bin.Jalview
    argsWereParsed = theseArgsWereParsed;
    return argsWereParsed;
  }

  public boolean commandArgsProvided()
  {
    return commandArgsProvided;
  }

  public boolean argsWereParsed()
  {
    return argsWereParsed;
  }

  protected boolean processUnlinked(String id)
  {
    return processLinked(id);
  }

  protected boolean processLinked(String id)
  {
    boolean theseArgsWereParsed = false;
    ArgValuesMap avm = argParser.getLinkedArgs(id);
    if (avm == null)
      return true;

    /*
     * // script to execute after all loading is completed one way or another String
     * groovyscript = m.get(Arg.GROOVY) == null ? null :
     * m.get(Arg.GROOVY).getValue(); String file = m.get(Arg.OPEN) == null ? null :
     * m.get(Arg.OPEN).getValue(); String data = null; FileFormatI format = null;
     * DataSourceType protocol = null;
     */
    if (avm.containsArg(Arg.APPEND) || avm.containsArg(Arg.OPEN))
    {
      commandArgsProvided = true;
      long progress = -1;

      boolean first = true;
      boolean progressBarSet = false;
      AlignFrame af;
      // Combine the APPEND and OPEN files into one list, along with whether it
      // was APPEND or OPEN
      List<ArgValue> openAvList = new ArrayList<>();
      openAvList.addAll(avm.getArgValueList(Arg.OPEN));
      openAvList.addAll(avm.getArgValueList(Arg.APPEND));
      // sort avlist based on av.getArgIndex()
      Collections.sort(openAvList);
      for (ArgValue av : openAvList)
      {
        Arg a = av.getArg();
        SubVals sv = av.getSubVals();
        String openFile = av.getValue();
        if (openFile == null)
          continue;

        theseArgsWereParsed = true;
        if (first)
        {
          first = false;
          if (!headless && desktop != null)
          {
            desktop.setProgressBar(
                    MessageManager.getString(
                            "status.processing_commandline_args"),
                    progress = System.currentTimeMillis());
            progressBarSet = true;
          }
        }

        if (!Platform.isJS())
        /**
         * ignore in JavaScript -- can't just file existence - could load it?
         * 
         * @j2sIgnore
         */
        {
          if (!HttpUtils.startsWithHttpOrHttps(openFile))
          {
            if (!(new File(openFile)).exists())
            {
              Console.warn("Can't find file '" + openFile + "'");
            }
          }
        }

        DataSourceType protocol = AppletFormatAdapter
                .checkProtocol(openFile);

        FileFormatI format = null;
        try
        {
          format = new IdentifyFile().identify(openFile, protocol);
        } catch (FileFormatException e1)
        {
          Console.error("Unknown file format for '" + openFile + "'");
        }

        af = afMap.get(id);
        // When to open a new AlignFrame
        if (af == null || "true".equals(av.getSubVal("new"))
                || a == Arg.OPEN || format == FileFormat.Jalview)
        {
          if (a == Arg.OPEN)
          {
            Jalview.testoutput(argParser, Arg.OPEN, "examples/uniref50.fa",
                    openFile);
          }

          Console.debug(
                  "Opening '" + openFile + "' in new alignment frame");
          FileLoader fileLoader = new FileLoader(!headless);

          af = fileLoader.LoadFileWaitTillLoaded(openFile, protocol,
                  format);

          // wrap alignment?
          boolean wrap = ArgParser.getFromSubValArgOrPref(avm, Arg.WRAP, sv,
                  null, "WRAP_ALIGNMENT", false);
          af.getCurrentView().setWrapAlignment(wrap);

          // colour alignment?
          String colour = ArgParser.getFromSubValArgOrPref(avm, av,
                  Arg.COLOUR, sv, null, "DEFAULT_COLOUR_PROT", "");
          if ("" != colour)
          {
            ColourSchemeI cs = ColourSchemeProperty.getColourScheme(
                    af.getViewport(), af.getViewport().getAlignment(), colour);
            
            if (cs==null && !"None".equals(colour))
            {
              Console.warn("Couldn't parse '"+colour+"' as a colourscheme.");
            } else {
              af.changeColour(cs);
            }
            Jalview.testoutput(argParser, Arg.COLOUR, "zappo", colour);
          }

          // Change alignment frame title
          String title = ArgParser.getFromSubValArgOrPref(avm, av,
                  Arg.TITLE, sv, null, null, null);
          if (title != null)
          {
            af.setTitle(title);
            Jalview.testoutput(argParser, Arg.TITLE, "test title", title);
          }

          // Add features
          String featuresfile = ArgParser.getValueFromSubValOrArg(avm, av,
                  Arg.FEATURES, sv);
          if (featuresfile != null)
          {
            af.parseFeaturesFile(featuresfile,
                    AppletFormatAdapter.checkProtocol(featuresfile));
            Jalview.testoutput(argParser, Arg.FEATURES,
                    "examples/testdata/plantfdx.features", featuresfile);
          }

          // Add annotations from file
          String annotationsfile = ArgParser.getValueFromSubValOrArg(avm,
                  av, Arg.ANNOTATIONS, sv);
          if (annotationsfile != null)
          {
            af.loadJalviewDataFile(annotationsfile, null, null, null);
            Jalview.testoutput(argParser, Arg.ANNOTATIONS,
                    "examples/testdata/plantfdx.annotations",
                    annotationsfile);
          }

          // Set or clear the sortbytree flag
          boolean sortbytree = ArgParser.getBoolFromSubValOrArg(avm,
                  Arg.SORTBYTREE, sv);
          if (sortbytree)
          {
            af.getViewport().setSortByTree(true);
            Jalview.testoutput(argParser, Arg.SORTBYTREE);
          }

          // Load tree from file
          String treefile = ArgParser.getValueFromSubValOrArg(avm, av,
                  Arg.TREE, sv);
          if (treefile != null)
          {
            try
            {
              NewickFile nf = new NewickFile(treefile,
                      AppletFormatAdapter.checkProtocol(treefile));
              af.getViewport().setCurrentTree(
                      af.showNewickTree(nf, treefile).getTree());
              Jalview.testoutput(argParser, Arg.TREE,
                      "examples/testdata/uniref50_test_tree", treefile);
            } catch (IOException e)
            {
              Console.warn("Couldn't add tree " + treefile, e);
            }
          }

          // Show secondary structure annotations?
          boolean showSSAnnotations = ArgParser.getFromSubValArgOrPref(avm,
                  Arg.SHOWSSANNOTATIONS, av.getSubVals(), null,
                  "STRUCT_FROM_PDB", true);
          af.setAnnotationsVisibility(showSSAnnotations, true, false);

          // Show sequence annotations?
          boolean showAnnotations = ArgParser.getFromSubValArgOrPref(avm,
                  Arg.SHOWANNOTATIONS, av.getSubVals(), null,
                  "SHOW_ANNOTATIONS", true);
          af.setAnnotationsVisibility(showAnnotations, false, true);

          // show temperature factor annotations?
          if (avm.getBoolean(Arg.NOTEMPFAC))
          {
            // do this better (annotation types?)
            List<String> hideThese = new ArrayList<>();
            hideThese.add("Temperature Factor");
            hideThese.add(AlphaFoldAnnotationRowBuilder.LABEL);
            AlignmentUtils.showOrHideSequenceAnnotations(
                    af.getCurrentView().getAlignment(), hideThese, null,
                    false, false);
          }

          // store the AlignFrame for this id
          afMap.put(id, af);

          // is it its own structure file?
          if (format.isStructureFile())
          {
            StructureSelectionManager ssm = StructureSelectionManager
                    .getStructureSelectionManager(Desktop.instance);
            SequenceI seq = af.alignPanel.getAlignment().getSequenceAt(0);
            ssm.computeMapping(false, new SequenceI[] { seq }, null,
                    openFile, DataSourceType.FILE, null, null, null, false);
          }
        }
        else
        {
          Console.debug(
                  "Opening '" + openFile + "' in existing alignment frame");
          DataSourceType dst = HttpUtils.startsWithHttpOrHttps(openFile)
                  ? DataSourceType.URL
                  : DataSourceType.FILE;
          FileLoader fileLoader = new FileLoader(!headless);
          fileLoader.LoadFile(af.getCurrentView(), openFile, dst, null,
                  false);
        }

        Console.debug("Command " + Arg.APPEND + " executed successfully!");

      }
      if (first) // first=true means nothing opened
      {
        if (headless)
        {
          Jalview.exit("Could not open any files in headless mode", 1);
        }
        else
        {
          Console.warn("No more files to open");
        }
      }
      if (progressBarSet && desktop != null)
        desktop.setProgressBar(null, progress);

    }

    // open the structure (from same PDB file or given PDBfile)
    if (!avm.getBoolean(Arg.NOSTRUCTURE))
    {
      AlignFrame af = afMap.get(id);
      if (avm.containsArg(Arg.STRUCTURE))
      {
        commandArgsProvided = true;
        for (ArgValue av : avm.getArgValueList(Arg.STRUCTURE))
        {
          String val = av.getValue();
          SubVals subVals = av.getSubVals();
          SequenceI seq = getSpecifiedSequence(af, avm, av);
          if (seq == null)
          {
            // Could not find sequence from subId, let's assume the first
            // sequence in the alignframe
            AlignmentI al = af.getCurrentView().getAlignment();
            seq = al.getSequenceAt(0);
          }

          if (seq == null)
          {
            Console.warn("Could not find sequence for argument "
                    + Arg.STRUCTURE.argString() + "=" + val);
            // you probably want to continue here, not break
            // break;
            continue;
          }
          File structureFile = null;
          if (subVals.getContent() != null
                  && subVals.getContent().length() != 0)
          {
            structureFile = new File(subVals.getContent());
            Console.debug("Using structure file (from argument) '"
                    + structureFile.getAbsolutePath() + "'");
          }
          // TRY THIS
          /*
           * PDBEntry fileEntry = new AssociatePdbFileWithSeq()
           * .associatePdbWithSeq(selectedPdbFileName, DataSourceType.FILE,
           * selectedSequence, true, Desktop.instance);
           * 
           * sViewer = launchStructureViewer(ssm, new PDBEntry[] { fileEntry }, ap, new
           * SequenceI[] { selectedSequence });
           * 
           */
          /* THIS DOESN'T WORK */
          else if (seq.getAllPDBEntries() != null
                  && seq.getAllPDBEntries().size() > 0)
          {
            structureFile = new File(
                    seq.getAllPDBEntries().elementAt(0).getFile());
            Console.debug("Using structure file (from sequence) '"
                    + structureFile.getAbsolutePath() + "'");
          }

          if (structureFile == null)
          {
            Console.warn("Not provided structure file with '" + val + "'");
            continue;
          }

          if (!structureFile.exists())
          {
            Console.warn("Structure file '"
                    + structureFile.getAbsoluteFile() + "' not found.");
            continue;
          }

          Console.debug("Using structure file "
                  + structureFile.getAbsolutePath());

          // open structure view
          AlignmentPanel ap = af.alignPanel;
          if (headless)
          {
            Cache.setProperty(Preferences.STRUCTURE_DISPLAY,
                    StructureViewer.ViewerType.JMOL.toString());
          }

          String structureFilepath = structureFile.getAbsolutePath();

          // get PAEMATRIX file and label from subvals or Arg.PAEMATRIX
          String paeFilepath = ArgParser
                  .getFromSubValArgOrPrefWithSubstitutions(argParser, avm,
                          Arg.PAEMATRIX, Position.AFTER, av, subVals, null,
                          null, null);
          if (paeFilepath != null)
          {
            File paeFile = new File(paeFilepath);

            try
            {
              paeFilepath = paeFile.getCanonicalPath();
            } catch (IOException e)
            {
              paeFilepath = paeFile.getAbsolutePath();
              Console.warn("Problem with the PAE file path: '"
                      + paeFile.getPath() + "'");
            }
          }

          // showing annotations from structure file or not
          boolean ssFromStructure = ArgParser.getFromSubValArgOrPref(avm,
                  Arg.SHOWSSANNOTATIONS, subVals, null, "STRUCT_FROM_PDB",
                  true);

          // get TEMPFAC type from subvals or Arg.TEMPFAC in case user Adds
          // reference annotations
          String tftString = ArgParser
                  .getFromSubValArgOrPrefWithSubstitutions(argParser, avm,
                          Arg.TEMPFAC, Position.AFTER, av, subVals, null,
                          null, null);
          boolean notempfac = ArgParser.getFromSubValArgOrPref(avm,
                  Arg.NOTEMPFAC, subVals, null, "ADD_TEMPFACT_ANN", false,
                  true);
          TFType tft = notempfac ? null : TFType.DEFAULT;
          if (tftString != null && !notempfac)
          {
            // get kind of temperature factor annotation
            try
            {
              tft = TFType.valueOf(tftString.toUpperCase(Locale.ROOT));
              Console.debug("Obtained Temperature Factor type of '" + tft
                      + "' for structure '" + structureFilepath + "'");
            } catch (IllegalArgumentException e)
            {
              // Just an error message!
              StringBuilder sb = new StringBuilder().append("Cannot set ")
                      .append(Arg.TEMPFAC.argString()).append(" to '")
                      .append(tft)
                      .append("', ignoring.  Valid values are: ");
              Iterator<TFType> it = Arrays.stream(TFType.values())
                      .iterator();
              while (it.hasNext())
              {
                sb.append(it.next().toString().toLowerCase(Locale.ROOT));
                if (it.hasNext())
                  sb.append(", ");
              }
              Console.warn(sb.toString());
            }
          }

          String sViewer = ArgParser.getFromSubValArgOrPref(avm,
                  Arg.STRUCTUREVIEWER, Position.AFTER, av, subVals, null,
                  null, "jmol");
          ViewerType viewerType = null;
          if (!"none".equals(sViewer))
          {
            for (ViewerType v : EnumSet.allOf(ViewerType.class))
            {
              String name = v.name().toLowerCase(Locale.ROOT)
                      .replaceAll(" ", "");
              if (sViewer.equals(name))
              {
                viewerType = v;
                break;
              }
            }
          }

          // TODO use ssFromStructure
          StructureViewer sv = StructureChooser
                  .openStructureFileForSequence(null, null, ap, seq, false,
                          structureFilepath, tft, paeFilepath, false,
                          ssFromStructure, false, viewerType);

          if (sv==null)
          {
            Console.error("Failed to import and open structure view.");
            continue;
          }
          try
          {
            long tries=1000;
            while (sv.isBusy() && tries>0)
            {
              Thread.sleep(25);
              if (sv.isBusy())
              {
                tries--;
                Console.debug(
                        "Waiting for viewer for " + structureFilepath);
              }
            }
            if (tries==0 && sv.isBusy())
            {
              Console.warn("Gave up waiting for structure viewer to load. Something may have gone wrong.");
            }
          } catch (Exception x)
          {
            Console.warn("Exception whilst waiting for structure viewer "+structureFilepath,x);
          }
          Console.debug("Successfully opened viewer for "+structureFilepath);
          String structureImageFilename = ArgParser.getValueFromSubValOrArg(
                  avm, av, Arg.STRUCTUREIMAGE, subVals);
          if (sv != null && structureImageFilename != null)
          {
            ArgValue siAv = avm.getClosestNextArgValueOfArg(av,
                    Arg.STRUCTUREIMAGE);
            SubVals sisv = null;
            if (structureImageFilename.equals(siAv.getValue()))
            {
              sisv = siAv.getSubVals();
            }
            File structureImageFile = new File(structureImageFilename);
            String width = ArgParser.getValueFromSubValOrArg(avm, av,
                    Arg.STRUCTUREIMAGEWIDTH, sisv);
            String height = ArgParser.getValueFromSubValOrArg(avm, av,
                    Arg.STRUCTUREIMAGEHEIGHT, sisv);
            String scale = ArgParser.getValueFromSubValOrArg(avm, av,
                    Arg.STRUCTUREIMAGESCALE, sisv);
            String renderer = ArgParser.getValueFromSubValOrArg(avm, av,
                    Arg.STRUCTUREIMAGETEXTRENDERER, sisv);
            String typeS = ArgParser.getValueFromSubValOrArg(avm, av,
                    Arg.STRUCTUREIMAGETYPE, sisv);
            if (typeS == null || typeS.length() == 0)
            {
              typeS = FileUtils.getExtension(structureImageFile);
            }
            TYPE imageType;
            try
            {
              imageType = Enum.valueOf(TYPE.class,
                      typeS.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e)
            {
              Console.warn("Do not know image format '" + typeS
                      + "', using PNG");
              imageType = TYPE.PNG;
            }
            BitmapImageSizing userBis = ImageMaker
                    .parseScaleWidthHeightStrings(scale, width, height);
            // TODO MAKE THIS VIEWER INDEPENDENT!!
            switch (StructureViewer.getViewerType())
            {
            case JMOL:
              try
              {
                Thread.sleep(1000); // WHY ???
              } catch (InterruptedException e)
              {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              JalviewStructureDisplayI sview = sv
                      .getJalviewStructureDisplay();
              if (sview instanceof AppJmol)
              {
                AppJmol jmol = (AppJmol) sview;
                try { 
                  Console.debug("Rendering image to "+structureImageFile);
                  jmol.makePDBImage(structureImageFile, imageType, renderer,
                        userBis);
                  Console.debug("Finished Rendering image to "+structureImageFile);

                }
                catch (ImageOutputException ioexc)
                {
                  Console.warn("Unexpected error whilst exporting image to "+structureImageFile,ioexc);
                }

              }
              break;
            default:
              Console.warn("Cannot export image for structure viewer "
                      + sv.getViewerType() + " yet");
              break;
            }
          }
        }
      }
    }

    /*
    boolean doShading = avm.getBoolean(Arg.TEMPFAC_SHADING);
    if (doShading)
    {
      AlignFrame af = afMap.get(id);
      for (AlignmentAnnotation aa : af.alignPanel.getAlignment()
              .findAnnotation(PDBChain.class.getName().toString()))
      {
        AnnotationColourGradient acg = new AnnotationColourGradient(aa,
                af.alignPanel.av.getGlobalColourScheme(), 0);
        acg.setSeqAssociated(true);
        af.changeColour(acg);
        Console.info("Changed colour " + acg.toString());
      }
    }
    */

    return theseArgsWereParsed;
  }

  protected void processGroovyScript(String id)
  {
    ArgValuesMap avm = argParser.getLinkedArgs(id);
    AlignFrame af = afMap.get(id);

    if (af == null)
    {
      Console.warn("Did not have an alignment window for id=" + id);
      return;
    }

    if (avm.containsArg(Arg.GROOVY))
    {
      String groovyscript = avm.getValue(Arg.GROOVY);
      if (groovyscript != null)
      {
        // Execute the groovy script after we've done all the rendering stuff
        // and before any images or figures are generated.
        Console.info("Executing script " + groovyscript);
        Jalview.getInstance().executeGroovyScript(groovyscript, af);
      }
    }
  }

  protected boolean processImages(String id)
  {
    ArgValuesMap avm = argParser.getLinkedArgs(id);
    AlignFrame af = afMap.get(id);

    if (af == null)
    {
      Console.warn("Did not have an alignment window for id=" + id);
      return false;
    }

    if (avm.containsArg(Arg.IMAGE))
    {
      for (ArgValue av : avm.getArgValueList(Arg.IMAGE))
      {
        String val = av.getValue();
        SubVals subVal = av.getSubVals();
        String fileName = subVal.getContent();
        File file = new File(fileName);
        String name = af.getName();
        String renderer = ArgParser.getValueFromSubValOrArg(avm, av,
                Arg.TEXTRENDERER, subVal);
        if (renderer == null)
          renderer = "text";
        String type = "png"; // default

        String scale = ArgParser.getValueFromSubValOrArg(avm, av, Arg.SCALE,
                subVal);
        String width = ArgParser.getValueFromSubValOrArg(avm, av, Arg.WIDTH,
                subVal);
        String height = ArgParser.getValueFromSubValOrArg(avm, av,
                Arg.HEIGHT, subVal);
        BitmapImageSizing userBis = ImageMaker
                .parseScaleWidthHeightStrings(scale, width, height);

        type = ArgParser.getValueFromSubValOrArg(avm, av, Arg.TYPE, subVal);
        if (type == null && fileName != null)
        {
          for (String ext : new String[] { "svg", "png", "html", "eps" })
          {
            if (fileName.toLowerCase(Locale.ROOT).endsWith("." + ext))
            {
              type = ext;
            }
          }
        }
        // for moment we disable JSON export
        Cache.setPropsAreReadOnly(true);
        Cache.setProperty("EXPORT_EMBBED_BIOJSON", "false");

        Console.info("Writing " + file);
        try {
        switch (type)
        {

        case "svg":
          Console.debug("Outputting type '" + type + "' to " + fileName);
          af.createSVG(file, renderer);
          break;

        case "png":
          Console.debug("Outputting type '" + type + "' to " + fileName);
          af.createPNG(file, null, userBis);
          break;

        case "html":
          Console.debug("Outputting type '" + type + "' to " + fileName);
          HtmlSvgOutput htmlSVG = new HtmlSvgOutput(af.alignPanel);
          htmlSVG.exportHTML(fileName, renderer);
          break;

        case "biojs":
          Console.debug("Creating BioJS MSA Viwer HTML file: " + fileName);
          try
          {
            BioJsHTMLOutput.refreshVersionInfo(
                    BioJsHTMLOutput.BJS_TEMPLATES_LOCAL_DIRECTORY);
          } catch (URISyntaxException e)
          {
            e.printStackTrace();
          }
          BioJsHTMLOutput bjs = new BioJsHTMLOutput(af.alignPanel);
          bjs.exportHTML(fileName);
          break;

        case "eps":
          Console.debug("Creating EPS file: " + fileName);
          af.createEPS(file, name);
          break;

        case "imagemap":
          Console.debug("Creating ImageMap file: " + fileName);
          af.createImageMap(file, name);
          break;

        default:
          Console.warn(Arg.IMAGE.argString() + " type '" + type
                  + "' not known. Ignoring");
          break;
        }
        } catch (Exception ioex) {
          Console.warn("Unexpected error during export",ioex);
        }
      }
    }
    return true;
  }

  protected boolean processOutput(String id)
  {
    ArgValuesMap avm = argParser.getLinkedArgs(id);
    AlignFrame af = afMap.get(id);

    if (af == null)
    {
      Console.warn("Did not have an alignment window for id=" + id);
      return false;
    }

    if (avm.containsArg(Arg.OUTPUT))
    {
      for (ArgValue av : avm.getArgValueList(Arg.OUTPUT))
      {
        String val = av.getValue();
        SubVals subVals = av.getSubVals();
        String fileName = subVals.getContent();
        File file = new File(fileName);
        boolean overwrite = ArgParser.getFromSubValArgOrPref(avm,
                Arg.OVERWRITE, subVals, null, "OVERWRITE_OUTPUT", false);
        // backups. Use the Arg.BACKUPS or subval "backups" setting first,
        // otherwise if headless assume false, if not headless use the user
        // preference with default true.
        boolean backups = ArgParser.getFromSubValArgOrPref(avm, Arg.BACKUPS,
                subVals, null,
                Platform.isHeadless() ? null : BackupFiles.ENABLED,
                !Platform.isHeadless());

        // if backups is not true then --overwrite must be specified
        if (file.exists() && !(overwrite || backups))
        {
          Console.error("Won't overwrite file '" + fileName + "' without "
                  + Arg.OVERWRITE.argString() + " or "
                  + Arg.BACKUPS.argString() + " set");
          return false;
        }

        String name = af.getName();
        String format = ArgParser.getValueFromSubValOrArg(avm, av,
                Arg.FORMAT, subVals);
        FileFormats ffs = FileFormats.getInstance();
        List<String> validFormats = ffs.getWritableFormats(false);

        FileFormatI ff = null;
        if (format == null && fileName != null)
        {
          FORMAT: for (String fname : validFormats)
          {
            FileFormatI tff = ffs.forName(fname);
            String[] extensions = tff.getExtensions().split(",");
            for (String ext : extensions)
            {
              if (fileName.toLowerCase(Locale.ROOT).endsWith("." + ext))
              {
                ff = tff;
                format = ff.getName();
                break FORMAT;
              }
            }
          }
        }
        if (ff == null && format != null)
        {
          ff = ffs.forName(format);
        }
        if (ff == null)
        {
          StringBuilder validSB = new StringBuilder();
          for (String f : validFormats)
          {
            if (validSB.length() > 0)
              validSB.append(", ");
            validSB.append(f);
            FileFormatI tff = ffs.forName(f);
            validSB.append(" (");
            validSB.append(tff.getExtensions());
            validSB.append(")");
          }

          Jalview.exit("No valid format specified for "
                  + Arg.OUTPUT.argString() + ". Valid formats are "
                  + validSB.toString() + ".", 1);
          // this return really shouldn't happen
          return false;
        }

        String savedBackupsPreference = Cache
                .getDefault(BackupFiles.ENABLED, null);
        Console.debug("Setting backups to " + backups);
        Cache.applicationProperties.put(BackupFiles.ENABLED,
                Boolean.toString(backups));

        Console.info("Writing " + fileName);

        af.saveAlignment(fileName, ff);
        Console.debug("Returning backups to " + savedBackupsPreference);
        if (savedBackupsPreference != null)
          Cache.applicationProperties.put(BackupFiles.ENABLED,
                  savedBackupsPreference);
        if (af.isSaveAlignmentSuccessful())
        {
          Console.debug("Written alignment '" + name + "' in "
                  + ff.getName() + " format to " + file);
        }
        else
        {
          Console.warn("Error writing file " + file + " in " + ff.getName()
                  + " format!");
        }

      }
    }
    return true;
  }

  private SequenceI getSpecifiedSequence(AlignFrame af, ArgValuesMap avm,
          ArgValue av)
  {
    SubVals subVals = av.getSubVals();
    ArgValue idAv = avm.getClosestNextArgValueOfArg(av, Arg.SEQID);
    SequenceI seq = null;
    if (subVals == null && idAv == null)
      return null;
    if (af == null || af.getCurrentView() == null)
    {
      return null;
    }
    AlignmentI al = af.getCurrentView().getAlignment();
    if (al == null)
    {
      return null;
    }
    if (subVals != null)
    {
      if (subVals.has(Arg.SEQID.getName()))
      {
        seq = al.findName(subVals.get(Arg.SEQID.getName()));
      }
      else if (-1 < subVals.getIndex()
              && subVals.getIndex() < al.getSequences().size())
      {
        seq = al.getSequenceAt(subVals.getIndex());
      }
    }
    else if (idAv != null)
    {
      seq = al.findName(idAv.getValue());
    }
    return seq;
  }
}
