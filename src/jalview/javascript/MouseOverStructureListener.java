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
package jalview.javascript;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.appletgui.AlignFrame;
import jalview.bin.JalviewLite;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JmolCommands;
import jalview.structure.AtomSpec;
import jalview.structure.StructureListener;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.util.HttpUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Propagate events involving PDB structures associated with sequences to a
 * javascript function. Generally, the javascript handler is called with a
 * series of arguments like (eventname, ... ). As of Jalview 2.7, the following
 * different types of events are supported:
 * <ul>
 * <li>mouseover: javascript function called with arguments
 * 
 * <pre>
 * ['mouseover',String(pdb file URI), String(pdb file chain ID), String(residue
 * number moused over), String(atom index corresponding to residue)]
 * </pre>
 * 
 * </li>
 * <li>colourstruct: javascript function called with arguments
 * 
 * <pre>
 * ['colourstruct',String(alignment view id),String(number of javascript message
 * chunks to collect),String(length of first chunk in set of messages - or zero
 * for null message)]
 * </pre>
 * 
 * <br>
 * The message contains a series of Jmol script commands that will colour
 * structures according to their associated sequences in the current view. Use
 * jalview
 * .javascript.JalviewLiteJsApi.getJsMessage('colourstruct',String(alignment
 * view id)) to retrieve successive chunks of the message.</li>
 * </ul>
 * 
 * @author Jim Procter (jprocter)
 * 
 */
public class MouseOverStructureListener extends JSFunctionExec
        implements JsCallBack, StructureListener
{

  String _listenerfn;

  String[] modelSet;

  public MouseOverStructureListener(JalviewLite jalviewLite,
          String listener, String[] modelList)
  {
    super(jalviewLite);
    _listenerfn = listener;
    modelSet = modelList;
    if (modelSet != null)
    {
      for (int i = 0; i < modelSet.length; i++)
      {
        modelSet[i] = resolveModelFile(modelSet[i]);
      }
    }
  }

  /**
   * Returns the first out of: file, file prefixed by document base, or file
   * prefixed by codebase which can be resolved to a valid URL. If none can,
   * returns the input parameter value.
   * 
   * @param file
   */
  public String resolveModelFile(String file)
  {
    // TODO reuse JalviewLite.LoadingThread.addProtocol instead
    if (HttpUtils.isValidUrl(file))
    {
      return file;
    }

    String db = jvlite.getDocumentBase().toString();
    db = db.substring(0, db.lastIndexOf("/"));
    String docBaseFile = db + "/" + file;
    if (HttpUtils.isValidUrl(docBaseFile))
    {
      return docBaseFile;
    }

    String cb = jvlite.getCodeBase() + file;
    if (HttpUtils.isValidUrl(cb))
    {
      return cb;
    }

    return file;
  }

  @Override
  public String[] getStructureFiles()
  {
    return modelSet;
  }

  public void mouseOverStructure(int atomIndex, String strInfo)
  {

    // StructureSelectionManager.getStructureSelectionManager().mouseOverStructure(atomIndex,
    // chain, pdbfile)
    // TODO Auto-generated method stub

  }

  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    for (AtomSpec atom : atoms)
    {
      try
      {
        // TODO is this right? StructureSelectionManager passes pdbFile as the
        // field that is interpreted (in 2.8.2) as pdbId?
        // JBPComment: yep - this is right! the Javascript harness uses the
        // absolute pdbFile URI to locate the PDB file in the external viewer
        executeJavascriptFunction(_listenerfn,
                new String[]
                { "mouseover", "" + atom.getPdbFile(), "" + atom.getChain(),
                    "" + (atom.getPdbResNum()), "" + atom.getAtomIndex() });
      } catch (Exception ex)
      {
        System.err.println("Couldn't execute callback with " + _listenerfn
                + " for atomSpec: " + atom);
        ex.printStackTrace();
      }
    }
  }

  @Override
  public synchronized void updateColours(Object srce)
  {
    final Object source = srce;
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(jvlite);

    if (JalviewLite.debug)
    {
      System.err.println(
              this.getClass().getName() + " modelSet[0]: " + modelSet[0]);
      ssm.reportMapping();
    }

    if (source instanceof jalview.api.AlignmentViewPanel)
    {
      SequenceI[][] sequence = new SequenceI[modelSet.length][];
      for (int m = 0; m < modelSet.length; m++)
      {
        StructureMapping[] sm = ssm.getMapping(modelSet[m]);
        if (sm != null && sm.length > 0)
        {
          sequence[m] = new SequenceI[sm.length];
          for (int i = 0; i < sm.length; i++)
          {
            sequence[m][i] = sm[i].getSequence();
          }
        }
        else
        {
          sequence[m] = new SequenceI[0];
        }
        // if (jvlite.debug)
        // {
        // System.err.println("Mapped '" + modelSet[m] + "' to "
        // + sequence[m].length + " sequences.");
        // }
      }

      SequenceRenderer sr = ((jalview.appletgui.AlignmentPanel) source)
              .getSequenceRenderer();
      FeatureRenderer fr = ((jalview.appletgui.AlignmentPanel) source).av
              .isShowSequenceFeatures()
                      ? new jalview.appletgui.FeatureRenderer(
                              ((jalview.appletgui.AlignmentPanel) source).av)
                      : null;
      if (fr != null)
      {
        ((jalview.appletgui.FeatureRenderer) fr).transferSettings(
                ((jalview.appletgui.AlignmentPanel) source)
                        .getFeatureRenderer());
      }
      ;

      // Form a colour command from the given alignment panel for each distinct
      // structure
      ArrayList<String[]> ccomands = new ArrayList<>();
      ArrayList<String> pdbfn = new ArrayList<>();
      String[] colcommands = new JmolCommands().colourBySequence(ssm,
              modelSet, sequence, sr, (AlignmentViewPanel) source);
      if (colcommands == null)
      {
        return;
      }
      int sz = 0;
      // for (jalview.structure.StructureMappingcommandSet ccset : colcommands)
      for (String command : colcommands)
      {
        // sz += ccset.commands.length;
        // ccomands.add(command); // ccset.commands);
        // pdbfn.add(ccset.mapping);
      }

      String mclass, mhandle;
      String ccomandset[] = new String[sz];
      sz = 0;
      for (String[] ccset : ccomands)
      {
        System.arraycopy(ccset, 0, ccomandset, sz, ccset.length);
        sz += ccset.length;
      }
      if (jvlite.isJsMessageSetChanged(mclass = "colourstruct",
              mhandle = ((jalview.appletgui.AlignmentPanel) source).av
                      .getViewId(),
              ccomandset))
      {
        jvlite.setJsMessageSet(mclass, mhandle, ccomandset);
        // and notify javascript handler
        String st[] = new String[] { "colourstruct",
            "" + ((jalview.appletgui.AlignmentPanel) source).av.getViewId(),
            "" + ccomandset.length, jvlite.arrayToSeparatorList(
                    pdbfn.toArray(new String[pdbfn.size()])) };
        try
        {
          executeJavascriptFunction(true, _listenerfn, st);
        } catch (Exception ex)
        {
          System.err.println("Couldn't execute callback with " + _listenerfn
                  + " using args { " + st[0] + ", " + st[1] + ", " + st[2]
                  + "," + st[3] + "}"); // + ","+st[4]+"\n");
          ex.printStackTrace();

        }
      }
      /*
       * new Thread(new Runnable() { public void run() { // and send to
       * javascript handler String st[] = new String[0]; int i = 0; for (String
       * colcommand : colcommands) { // do sync execution for each chunk try {
       * executeJavascriptFunction( false, _listenerfn, st = new String[] {
       * "colourstruct", "" + ((jalview.appletgui.AlignmentPanel) source).av
       * .getViewId(), handle, "" }); } catch (Exception ex) {
       * System.err.println("Couldn't execute callback with " + _listenerfn +
       * " using args { " + st[0] + ", " + st[1] + ", " + st[2] + "," + st[3] +
       * "\n"); ex.printStackTrace();
       * 
       * } } } }).start();
       */
    }

  }

  @Override
  public AlignFrame getAlignFrame()
  {
    // associated with all alignframes, always.
    return null;
  }

  @Override
  public String getListenerFunction()
  {
    return _listenerfn;
  }

  @Override
  public void releaseReferences(Object svl)
  {

    // TODO Auto-generated method stub

  }

  @Override
  public boolean isListeningFor(SequenceI seq)
  {
    return true;
  }

}
