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
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.VamsasAppDatastore;
import jalview.structure.SelectionListener;
import jalview.structure.SelectionSource;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasListener;
import jalview.structure.VamsasSource;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;

import javax.swing.JInternalFrame;

import uk.ac.vamsas.client.ClientHandle;
import uk.ac.vamsas.client.IClient;
import uk.ac.vamsas.client.IClientDocument;
import uk.ac.vamsas.client.InvalidSessionDocumentException;
import uk.ac.vamsas.client.UserHandle;
import uk.ac.vamsas.client.VorbaId;
import uk.ac.vamsas.client.picking.IMessageHandler;
import uk.ac.vamsas.client.picking.IPickManager;
import uk.ac.vamsas.client.picking.Message;
import uk.ac.vamsas.client.picking.MouseOverMessage;
import uk.ac.vamsas.client.picking.SelectionMessage;
import uk.ac.vamsas.objects.core.Entry;
import uk.ac.vamsas.objects.core.Input;
import uk.ac.vamsas.objects.core.Pos;
import uk.ac.vamsas.objects.core.Seg;

/**
 * @author jimp
 * 
 */
public class VamsasApplication implements SelectionSource, VamsasSource
{
  IClient vclient = null;

  ClientHandle app = null;

  UserHandle user = null;

  Desktop jdesktop = null; // our jalview desktop reference

  private boolean inInitialUpdate = true;

  // Cache.preferences for vamsas client session arena
  // preferences for check for default session at startup.
  // user and organisation stuff.
  public VamsasApplication(Desktop jdesktop, File sessionPath,
          String sessionName)
  {
    // JBPNote:
    // we should create a session URI from the sessionPath and pass it to
    // the clientFactory - but the vamsas api doesn't cope with that yet.
    this.jdesktop = jdesktop;
    initClientSession(null, sessionPath, sessionName);
  }

  private static uk.ac.vamsas.client.IClientFactory getClientFactory()
          throws IOException
  {
    return new uk.ac.vamsas.client.simpleclient.SimpleClientFactory();
  }

  /**
   * Start a new vamsas session
   * 
   * @param jdesktop
   */
  public VamsasApplication(Desktop jdesktop)
  {
    this.jdesktop = jdesktop;
    initClientSession(null, null);
  }

  /**
   * init a connection to the session at the given url
   * 
   * @param jdesktop
   * @param sessionUrl
   */
  public VamsasApplication(Desktop jdesktop, String sessionUrl)
  {
    this.jdesktop = jdesktop;
    initClientSession(sessionUrl, null);
  }

  /**
   * @throws IOException
   *           or other if clientfactory instantiation failed.
   * @return list of current sessions or null if no session exists.
   */
  public static String[] getSessionList() throws Exception
  {
    return getClientFactory().getCurrentSessions();
  }

  /**
   * initialise, possibly with either a valid session url or a file for a new
   * session
   * 
   * @param sess
   *          null or a valid session url
   * @param vamsasDocument
   *          null or a valid vamsas document file
   * @return false if no vamsas connection was made
   */
  private void initClientSession(String sess, File vamsasDocument)
  {
    initClientSession(sess, vamsasDocument, null);
  }

  private boolean initClientSession(String sess, File vamsasDocument,
          String newDocSessionName)
  {
    try
    {
      // Only need to tell the library what the application is here
      app = getJalviewHandle();
      uk.ac.vamsas.client.IClientFactory clientfactory = getClientFactory();
      if (vamsasDocument != null)
      {
        if (sess != null)
        {
          throw new Error(MessageManager.getString(
                  "error.implementation_error_cannot_import_vamsas_doc"));
        }
        try
        {
          if (newDocSessionName != null)
          {
            vclient = clientfactory.openAsNewSessionIClient(app,
                    vamsasDocument, newDocSessionName);
          }
          else
          {
            vclient = clientfactory.openAsNewSessionIClient(app,
                    vamsasDocument);
          }
        } catch (InvalidSessionDocumentException e)
        {
          JvOptionPane.showInternalMessageDialog(Desktop.desktop,

                  MessageManager.getString(
                          "label.vamsas_doc_couldnt_be_opened_as_new_session"),
                  MessageManager
                          .getString("label.vamsas_document_import_failed"),
                  JvOptionPane.ERROR_MESSAGE);

        }
      }
      else
      {
        // join existing or create a new session
        if (sess == null)
        {
          vclient = clientfactory.getNewSessionIClient(app);
        }
        else
        {
          vclient = clientfactory.getIClient(app, sess);
        }
      }
      // set some properties for our VAMSAS interaction
      setVclientConfig();
      user = vclient.getUserHandle();

    } catch (Exception e)
    {
      Console.error("Couldn't instantiate vamsas client !", e);
      return false;
    }
    return true;
  }

  private void setVclientConfig()
  {
    if (vclient == null)
    {
      return;
    }
    try
    {
      if (vclient instanceof uk.ac.vamsas.client.simpleclient.SimpleClient)
      {
        uk.ac.vamsas.client.simpleclient.SimpleClientConfig cfg = ((uk.ac.vamsas.client.simpleclient.SimpleClient) vclient)
                .getSimpleClientConfig();
        cfg._validatemergedroots = false;
        cfg._validateupdatedroots = true; // we may write rubbish otherwise.
      }
    } catch (Error e)
    {
      Console.warn(
              "Probable SERIOUS VAMSAS client incompatibility - carrying on regardless",
              e);
    } catch (Exception e)
    {
      Console.warn(
              "Probable VAMSAS client incompatibility - carrying on regardless",
              e);
    }
  }

  /**
   * make the appHandle for Jalview
   * 
   * @return
   */
  private ClientHandle getJalviewHandle()
  {
    return new ClientHandle("jalview.bin.Jalview",
            Cache.getProperty("VERSION"));
  }

  /**
   * 
   * @return true if we are registered in a vamsas session
   */
  public boolean inSession()
  {
    return (vclient != null);
  }

  /**
   * called to connect to session inits handlers, does an initial document
   * update.
   */
  public void initial_update()
  {
    if (!inSession())
    {
      throw new Error(
              "Implementation error! Vamsas Operations when client not initialised and connected");
    }
    addDocumentUpdateHandler();
    addStoreDocumentHandler();
    startSession();
    inInitialUpdate = true;
    Console.debug("Jalview loading the Vamsas Session for the first time.");
    dealWithDocumentUpdate(false); // we don't push an update out to the
    inInitialUpdate = false;
    // document yet.
    Console.debug("... finished update for the first time.");
  }

  /**
   * Update all windows after a vamsas datamodel change. this could go on the
   * desktop object!
   * 
   */
  protected void updateJalviewGui()
  {
    JInternalFrame[] frames = jdesktop.getAllFrames();

    if (frames == null)
    {
      return;
    }

    try
    {
      // REVERSE ORDER
      for (int i = frames.length - 1; i > -1; i--)
      {
        if (frames[i] instanceof AlignFrame)
        {
          AlignFrame af = (AlignFrame) frames[i];
          af.alignPanel.alignmentChanged();
        }
      }
    } catch (Exception e)
    {
      Console.warn(
              "Exception whilst refreshing jalview windows after a vamsas document update.",
              e);
    }
  }

  public void push_update()
  {
    Thread udthread = new Thread(new Runnable()
    {

      @Override
      public void run()
      {
        Console.info("Jalview updating to the Vamsas Session.");

        dealWithDocumentUpdate(true);
        Console.info("Jalview finished updating to the Vamsas Session.");
      }

    });
    udthread.start();
  }

  /**
   * leave a session, prompting the user to save if necessary
   */
  public void end_session()
  {
    end_session(true);
  }

  private boolean promptUser = true;

  /**
   * leave a session, optionally prompting the user to save if necessary
   * 
   * @param promptUser
   *          when true enable prompting by this application
   */

  public void end_session(boolean promptUser)
  {
    if (!inSession())
    {
      throw new Error("Jalview not connected to Vamsas session");
    }
    Console.info("Jalview disconnecting from the Vamsas Session.");
    try
    {
      if (joinedSession)
      {
        boolean ourprompt = this.promptUser;
        this.promptUser = promptUser;
        vclient.finalizeClient();
        Console.info("Jalview has left the session.");
        this.promptUser = ourprompt; // restore default value
      }
      else
      {
        Console.warn(
                "JV Client leaving a session that's its not joined yet.");
      }
      joinedSession = false;
      vclient = null;
      app = null;
      user = null;
      jv2vobj = null;
      vobj2jv = null;
    } catch (Exception e)
    {
      Console.error("Vamsas Session finalization threw exceptions!", e);
    }
  }

  public void updateJalview(IClientDocument cdoc)
  {
    Console.debug("Jalview updating from sesion document ..");
    ensureJvVamsas();
    VamsasAppDatastore vds = new VamsasAppDatastore(cdoc, vobj2jv, jv2vobj,
            baseProvEntry(), alRedoState);
    try
    {
      vds.updateToJalview();
    } catch (Exception e)
    {
      Console.error("Failed to update Jalview from vamsas document.", e);
    }
    try
    {
      if (firstUpdate)
      {
        vds.updateJalviewFromAppdata();
        // Comment this out to repeatedly read in data from JalviewAppData
        // firstUpdate=false;
      }
    } catch (Exception e)
    {
      Console.error(
              "Exception when updating Jalview settings from Appdata.", e);
    }
    Console.debug(".. finished updating from sesion document.");

  }

  boolean firstUpdate = false;

  private void ensureJvVamsas()
  {
    if (jv2vobj == null)
    {
      jv2vobj = new IdentityHashMap();
      vobj2jv = new Hashtable();
      alRedoState = new Hashtable();
      firstUpdate = true;
    }
  }

  /**
   * jalview object binding to VorbaIds
   */
  IdentityHashMap jv2vobj = null;

  Hashtable vobj2jv = null;

  Hashtable alRedoState = null;

  boolean errorsDuringUpdate = false;

  boolean errorsDuringAppUpdate = false;

  /**
   * update the document accessed through doc. A backup of the current object
   * bindings is made.
   * 
   * @param doc
   * @return number of views stored in document (updated and new views)
   */
  public int updateVamsasDocument(IClientDocument doc)
  {
    int storedviews = 0;
    ensureJvVamsas();
    errorsDuringUpdate = false;
    errorsDuringAppUpdate = false;
    backup_objectMapping();
    VamsasAppDatastore vds = new VamsasAppDatastore(doc, vobj2jv, jv2vobj,
            baseProvEntry(), alRedoState);
    // wander through frames
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return 0;
    }
    Hashtable skipList = new Hashtable();
    Hashtable viewset = new Hashtable();

    try
    {
      // REVERSE ORDER
      for (int i = frames.length - 1; i > -1; i--)
      {
        if (frames[i] instanceof AlignFrame)
        {
          AlignFrame af = (AlignFrame) frames[i];
          if (!viewset.containsKey(af.getViewport().getSequenceSetId()))
          {
            // update alignment and root from frame.
            boolean stored = false;
            try
            {
              stored = vds.storeVAMSAS(af.getViewport(), af.getTitle());
            } catch (Exception e)
            {
              errorsDuringUpdate = true;
              Console.error("Exception synchronizing " + af.getTitle() + " "
                      + (af.getViewport().getViewName() == null ? ""
                              : " view " + af.getViewport().getViewName())
                      + " to document.", e);
              stored = false;
            }
            if (!stored)
            { // record skip in skipList
              skipList.put(af.getViewport().getSequenceSetId(), af);
            }
            else
            {
              storedviews++;
              // could try to eliminate sequenceSetId from skiplist ..
              // (skipList.containsKey(af.getViewport().getSequenceSetId()))
              // remember sequenceSetId so we can skip all the other views on
              // same alignment
              viewset.put(af.getViewport().getSequenceSetId(), af);
            }
          }
        }
      }
      // REVERSE ORDER
      // for (int i = frames.length - 1; i > -1; i--)
      // {
      // if (frames[i] instanceof AlignFrame)
      // {
      // AlignFrame af = (AlignFrame) frames[i];
      Iterator aframes = viewset.values().iterator();
      while (aframes.hasNext())
      {
        AlignFrame af = (AlignFrame) aframes.next();
        // add any AlignedCodonFrame mappings on this alignment to any other.
        vds.storeSequenceMappings(af.getViewport(), af.getTitle());
      }
    } catch (Exception e)
    {
      Console.error("Exception synchronizing Views to Document :", e);
      errorsDuringUpdate = true;
    }

    try
    {
      if (viewset.size() > 0)
      {
        // Alignment views were synchronized, so store their state in the
        // appData, too.
        // The skipList ensures we don't write out any alignments not actually
        // in the document.
        vds.setSkipList(skipList);
        vds.updateJalviewClientAppdata();
      }
    } catch (Exception e)
    {
      Console.error("Client Appdata Write exception", e);
      errorsDuringAppUpdate = true;
    }
    vds.clearSkipList();
    return storedviews;
  }

  private Entry baseProvEntry()
  {
    uk.ac.vamsas.objects.core.Entry pentry = new uk.ac.vamsas.objects.core.Entry();
    pentry.setUser(user.getFullName());
    pentry.setApp(app.getClientUrn());
    pentry.setDate(new java.util.Date());
    pentry.setAction("created");
    return pentry;
  }

  /**
   * do a vamsas document update or update jalview from the vamsas document
   * 
   * @param fromJalview
   *          true to update from jalview to the vamsas document
   * @return total number of stored alignments in the document after the update
   */
  protected int dealWithDocumentUpdate(boolean fromJalview)
  {
    int storedviews = 0;
    // called by update handler for document update.
    Console.debug("Updating jalview from changed vamsas document.");
    disableGui(true);
    try
    {
      long time = System.currentTimeMillis();
      IClientDocument cdoc = vclient.getClientDocument();
      if (Console.isDebugEnabled())
      {
        Console.debug("Time taken to get ClientDocument = "
                + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
      }
      if (fromJalview)
      {
        storedviews += updateVamsasDocument(cdoc);
        if (Console.isDebugEnabled())
        {
          Console.debug(
                  "Time taken to update Vamsas Document from jalview\t= "
                          + (System.currentTimeMillis() - time));
          time = System.currentTimeMillis();
        }
        cdoc.setVamsasRoots(cdoc.getVamsasRoots());
        if (Console.isDebugEnabled())
        {
          Console.debug("Time taken to set Document Roots\t\t= "
                  + (System.currentTimeMillis() - time));
          time = System.currentTimeMillis();
        }
      }
      else
      {
        updateJalview(cdoc);
        if (Console.isDebugEnabled())
        {
          Console.debug(
                  "Time taken to update Jalview from vamsas document Roots\t= "
                          + (System.currentTimeMillis() - time));
          time = System.currentTimeMillis();
        }

      }
      vclient.updateDocument(cdoc);
      if (Console.isDebugEnabled())
      {
        Console.debug("Time taken to update Session Document\t= "
                + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
      }
      cdoc = null;
    } catch (Exception ee)
    {
      System.err.println("Exception whilst updating :");
      ee.printStackTrace(System.err);
      // recover object map backup, since its probably corrupted with references
      // to Vobjects that don't exist anymore.
      recover_objectMappingBackup();
      storedviews = 0;
    }
    Console.debug("Finished updating from document change.");
    disableGui(false);
    return storedviews;
  }

  private void addDocumentUpdateHandler()
  {
    final VamsasApplication client = this;
    vclient.addDocumentUpdateHandler(new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        Console.debug("Dealing with document update event.");
        client.dealWithDocumentUpdate(false);
        Console.debug("finished dealing with event.");
      }
    });
    Console.debug("Added Jalview handler for vamsas document updates.");
  }

  private void addStoreDocumentHandler()
  {
    final VamsasApplication client = this;
    vclient.addVorbaEventHandler(
            uk.ac.vamsas.client.Events.DOCUMENT_REQUESTTOCLOSE,
            new PropertyChangeListener()
            {
              @Override
              public void propertyChange(PropertyChangeEvent evt)
              {
                if (client.promptUser)
                {
                  Console.debug(
                          "Asking user if the vamsas session should be stored.");
                  int reply = JvOptionPane.showInternalConfirmDialog(
                          Desktop.desktop,
                          "The current VAMSAS session has unsaved data - do you want to save it ?",
                          "VAMSAS Session Shutdown",
                          JvOptionPane.YES_NO_OPTION,
                          JvOptionPane.QUESTION_MESSAGE);

                  if (reply == JvOptionPane.YES_OPTION)
                  {
                    Console.debug("Prompting for vamsas store filename.");
                    Desktop.instance.vamsasSave_actionPerformed(null);
                    Console.debug("Finished attempt at storing document.");
                  }
                  Console.debug(
                          "finished dealing with REQUESTTOCLOSE event.");
                }
                else
                {
                  Console.debug(
                          "Ignoring store document request (promptUser==false)");
                }
              }
            });
    Console.debug("Added Jalview handler for vamsas document updates.");
  }

  public void disableGui(boolean b)
  {
    // JAL-3311 TODO: remove this class!
    // Desktop.instance.setVamsasUpdate(b);
  }

  Hashtable _backup_vobj2jv;

  IdentityHashMap _backup_jv2vobj;

  /**
   * make a backup of the object mappings (vobj2jv and jv2vobj)
   */
  public void backup_objectMapping()
  {
    _backup_vobj2jv = new Hashtable(vobj2jv);
    _backup_jv2vobj = new IdentityHashMap(jv2vobj);
  }

  /**
   * recover original object mappings from the object mapping backup if document
   * IO failed
   * 
   * @throws Error
   *           if backup_objectMapping was not called.
   */
  public void recover_objectMappingBackup()
  {
    if (_backup_vobj2jv == null)
    {
      if (inInitialUpdate)
      {
        // nothing to recover so just
        return;
      }

      throw new Error(
              "IMPLEMENTATION ERROR: Cannot recover vamsas object mappings - no backup was made");
    }
    jv2vobj.clear();
    Iterator el = _backup_jv2vobj.entrySet().iterator();
    while (el.hasNext())
    {
      java.util.Map.Entry mp = (java.util.Map.Entry) el.next();
      jv2vobj.put(mp.getKey(), mp.getValue());
    }
    el = _backup_vobj2jv.entrySet().iterator();
    while (el.hasNext())
    {
      java.util.Map.Entry mp = (java.util.Map.Entry) el.next();
      vobj2jv.put(mp.getKey(), mp.getValue());
    }
  }

  private boolean joinedSession = false;

  private VamsasListener picker = null;

  private SelectionListener selecter;

  private void startSession()
  {
    if (inSession())
    {
      try
      {
        vclient.joinSession();
        joinedSession = true;
      } catch (Exception e)
      {
        // Complain to GUI
        Console.error("Failed to join vamsas session.", e);
        vclient = null;
      }
      try
      {
        final IPickManager pm = vclient.getPickManager();
        final StructureSelectionManager ssm = StructureSelectionManager
                .getStructureSelectionManager(Desktop.instance);
        final VamsasApplication me = this;
        pm.registerMessageHandler(new IMessageHandler()
        {
          String last = null;

          @Override
          public void handleMessage(Message message)
          {
            if (vobj2jv == null)
            {
              // we are not in a session yet.
              return;
            }
            if (message instanceof MouseOverMessage)
            {
              MouseOverMessage mm = (MouseOverMessage) message;
              String mstring = mm.getVorbaID() + " " + mm.getPosition();
              if (last != null && mstring.equals(last))
              {
                return;
              }
              // if (Cache.isDebugEnabled())
              // {
              // Cache.debug("Received MouseOverMessage "+mm.getVorbaID()+"
              // "+mm.getPosition());
              // }
              Object jvobj = vobj2jv.get(mm.getVorbaID());
              if (jvobj != null && jvobj instanceof SequenceI)
              {
                last = mstring;
                // Cache.debug("Handling Mouse over "+mm.getVorbaID()+"
                // bound to "+jvobj+" at "+mm.getPosition());
                // position is character position in aligned sequence
                ssm.mouseOverVamsasSequence((SequenceI) jvobj,
                        mm.getPosition(), me);
              }
            }
            if (message instanceof uk.ac.vamsas.client.picking.SelectionMessage)
            {
              // we only care about AlignmentSequence selections
              SelectionMessage sm = (SelectionMessage) message;
              sm.validate();
              System.err.println("Received\n" + sm.getRawMessage());
              Object[] jvobjs = sm.getVorbaIDs() == null ? null
                      : new Object[sm.getVorbaIDs().length];
              if (jvobjs == null)
              {
                // TODO: rationalise : can only clear a selection over a
                // referred to object
                ssm.sendSelection(null, null, null, me);
                return;
              }
              Class type = null;
              boolean send = true;
              for (int o = 0; o < jvobjs.length; o++)
              {
                jvobjs[o] = vobj2jv.get(sm.getVorbaIDs()[o]);
                if (jvobjs[o] == null)
                {
                  // can't cope with selections for unmapped objects
                  continue;
                }
                if (type == null)
                {
                  type = jvobjs[o].getClass();
                }
                ;
                if (type != jvobjs[o].getClass())
                {
                  send = false;
                  // discard - can't cope with selections over mixed objects
                  // continue;
                }
              }
              SequenceGroup jselection = null;
              ColumnSelection colsel = null;
              if (type == jalview.datamodel.Alignment.class)
              {
                if (jvobjs.length == 1)
                {
                  // TODO if (sm.isNone())// send a message to select the
                  // specified columns over the
                  // given
                  // alignment

                  send = true;
                }
              }
              if (type == jalview.datamodel.Sequence.class)
              {

                SequenceI seq;
                boolean aligned = ((jalview.datamodel.Sequence) jvobjs[0])
                        .getDatasetSequence() != null;
                int maxWidth = 0;
                if (aligned)
                {
                  jselection = new SequenceGroup();
                  jselection.addSequence(
                          seq = (jalview.datamodel.Sequence) jvobjs[0],
                          false);
                  maxWidth = seq.getLength();
                }
                for (int c = 1; aligned && jvobjs.length > 1
                        && c < jvobjs.length; c++)
                {
                  if (((jalview.datamodel.Sequence) jvobjs[c])
                          .getDatasetSequence() == null)
                  {
                    aligned = false;
                    continue;
                  }
                  else
                  {
                    jselection.addSequence(
                            seq = (jalview.datamodel.Sequence) jvobjs[c],
                            false);
                    if (maxWidth < seq.getLength())
                    {
                      maxWidth = seq.getLength();
                    }

                  }
                }
                if (!aligned)
                {
                  jselection = null;
                  // if cardinality is greater than one then verify all
                  // sequences are alignment sequences.
                  if (jvobjs.length == 1)
                  {
                    // find all instances of this dataset sequence in the
                    // displayed alignments containing the associated range and
                    // select them.
                  }
                }
                else
                {
                  jselection.setStartRes(0);
                  jselection.setEndRes(maxWidth);
                  // locate the alignment containing the given sequences and
                  // select the associated ranges on them.
                  if (sm.getRanges() != null)
                  {
                    int[] prange = uk.ac.vamsas.objects.utils.Range
                            .getBounds(sm.getRanges());
                    jselection.setStartRes(prange[0] - 1);
                    jselection.setEndRes(prange[1] - 1);
                    prange = uk.ac.vamsas.objects.utils.Range
                            .getIntervals(sm.getRanges());
                    colsel = new ColumnSelection();
                    for (int p = 0; p < prange.length; p += 2)
                    {
                      int d = (prange[p] <= prange[p + 1]) ? 1 : -1;
                      // try to join up adjacent columns to make a larger
                      // selection
                      // lower and upper bounds
                      int l = (d < 0) ? 1 : 0;
                      int u = (d > 0) ? 1 : 0;

                      if (jselection.getStartRes() > 0
                              && prange[p + l] == jselection.getStartRes())
                      {
                        jselection.setStartRes(prange[p + l] - 1);
                      }
                      if (jselection.getEndRes() <= maxWidth && prange[p
                              + u] == (jselection.getEndRes() + 2))
                      {
                        jselection.setEndRes(prange[p + u] - 1);
                      }
                      // mark all the columns in the range.
                      for (int sr = prange[p], er = prange[p + 1], de = er
                              + d; sr != de; sr += d)
                      {
                        colsel.addElement(sr - 1);
                      }
                    }
                  }
                  send = true;
                }
              }
              if (send)
              {
                ssm.sendSelection(jselection, colsel, null, me);
              }
              // discard message.
              for (int c = 0; c < jvobjs.length; c++)
              {
                jvobjs[c] = null;
              }
              ;
              jvobjs = null;
              return;
            }
          }
        });
        picker = new VamsasListener()
        {
          SequenceI last = null;

          int i = -1;

          @Override
          public void mouseOverSequence(SequenceI seq, int index,
                  VamsasSource source)
          {
            if (jv2vobj == null)
            {
              return;
            }
            if (seq != last || i != index)
            {
              VorbaId v = (VorbaId) jv2vobj.get(seq);
              if (v != null)
              {
                // this should really be a trace message.
                // Cache.debug("Mouse over " + v.getId() + " bound to "
                // + seq + " at " + index);
                last = seq;
                i = index;
                MouseOverMessage message = new MouseOverMessage(v.getId(),
                        index);
                pm.sendMessage(message);
              }
            }
          }
        };
        selecter = new SelectionListener()
        {

          @Override
          public void selection(SequenceGroup seqsel,
                  ColumnSelection colsel, HiddenColumns hidden,
                  SelectionSource source)
          {
            if (vobj2jv == null)
            {
              Console.warn(
                      "Selection listener still active for dead session.");
              // not in a session.
              return;
            }
            if (source != me)
            {
              AlignmentI visal = null;
              if (source instanceof AlignViewport)
              {
                visal = ((AlignmentViewport) source).getAlignment();
              }
              SelectionMessage sm = null;
              if ((seqsel == null || seqsel.getSize() == 0)
                      && (colsel == null || colsel.getSelected() == null
                              || colsel.getSelected().size() == 0))
              {
                if (source instanceof AlignViewport)
                {
                  // the empty selection.
                  sm = new SelectionMessage("jalview",
                          new String[]
                          { ((AlignmentViewport) source)
                                  .getSequenceSetId() },
                          null, true);
                }
                else
                {
                  // the empty selection.
                  sm = new SelectionMessage("jalview", null, null, true);
                }
              }
              else
              {
                String[] vobj = new String[seqsel.getSize()];
                int o = 0;
                for (SequenceI sel : seqsel.getSequences(null))
                {
                  VorbaId v = (VorbaId) jv2vobj.get(sel);
                  if (v != null)
                  {
                    vobj[o++] = v.toString();
                  }
                }
                if (o < vobj.length)
                {
                  String t[] = vobj;
                  vobj = new String[o];
                  System.arraycopy(t, 0, vobj, 0, o);
                  t = null;
                }
                Input range = null;
                if (seqsel != null && colsel != null)
                {
                  // deparse the colsel into positions on the vamsas alignment
                  // sequences
                  range = new Input();
                  if (colsel.getSelected() != null
                          && colsel.getSelected().size() > 0
                          && visal != null
                          && seqsel.getSize() == visal.getHeight())
                  {
                    // gather selected columns outwith the sequence positions
                    // too
                    for (Integer ival : colsel.getSelected())
                    {
                      Pos p = new Pos();
                      p.setI(ival.intValue() + 1);
                      range.addPos(p);
                    }
                  }
                  else
                  {
                    Iterator<int[]> intervals = hidden
                            .getVisContigsIterator(seqsel.getStartRes(),
                                    seqsel.getEndRes() + 1, false);
                    while (intervals.hasNext())
                    {
                      int[] region = intervals.next();
                      Seg s = new Seg();
                      s.setStart(region[0] + 1); // vamsas indices begin at 1,
                                                 // not zero.
                      s.setEnd(region[1] + 1);
                      s.setInclusive(true);
                      range.addSeg(s);
                    }
                  }
                }
                if (vobj.length > 0)
                {
                  sm = new SelectionMessage("jalview", vobj, range);
                }
                else
                {
                  sm = null;
                }
              }
              if (sm != null)
              {
                sm.validate(); // debug
                Console.debug("Selection Message\n" + sm.getRawMessage());
                pm.sendMessage(sm);
              }
            }
          }

        };
        ssm.addStructureViewerListener(picker); // better method here
        ssm.addSelectionListener(selecter);
      } catch (Exception e)
      {
        Console.error("Failed to init Vamsas Picking", e);
      }
    }
  }

  public String getCurrentSession()
  {
    if (vclient != null)
    {
      return (vclient.getSessionUrn());
    }
    return null;
  }
}
