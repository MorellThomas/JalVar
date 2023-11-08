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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import jalview.api.FeatureSettingsModelI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceI;
import jalview.fts.core.GFTSPanel;
import jalview.fts.service.pdb.PDBFTSPanel;
import jalview.fts.service.threedbeacons.TDBeaconsFTSPanel;
import jalview.fts.service.uniprot.UniprotFTSPanel;
import jalview.io.FileFormatI;
import jalview.io.gff.SequenceOntologyI;
import jalview.util.DBRefUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.seqfetcher.DbSourceProxy;

/**
 * A panel where the use may choose a database source, and enter one or more
 * accessions, to retrieve entries from the database.
 * <p>
 * If the selected source is Uniprot or PDB, a free text search panel is opened
 * instead to perform the search and selection.
 */
public class SequenceFetcher extends JPanel implements Runnable
{
  private class StringPair
  {
    private String key;

    private String display;

    public StringPair(String s1, String s2)
    {
      key = s1;
      display = s2;
    }

    public StringPair(String s)
    {
      this(s, s);
    }

    public String getKey()
    {
      return key;
    }

    public String getDisplay()
    {
      return display;
    }

    @Override
    public String toString()
    {
      return display;
    }

    public boolean equals(StringPair other)
    {
      return other.key == this.key;
    }
  }

  private static jalview.ws.SequenceFetcher sfetch = null;

  JLabel exampleAccession;

  JComboBox<StringPair> database;

  JCheckBox replacePunctuation;

  JButton okBtn;

  JButton exampleBtn;

  JButton closeBtn;

  JButton backBtn;

  JTextArea textArea;

  JInternalFrame frame;

  IProgressIndicator guiWindow;

  AlignFrame alignFrame;

  GFTSPanel parentSearchPanel;

  IProgressIndicator progressIndicator;

  volatile boolean _isConstructing = false;

  /**
   * Returns the shared instance of the SequenceFetcher client
   * 
   * @return
   */
  public static jalview.ws.SequenceFetcher getSequenceFetcherSingleton()
  {
    if (sfetch == null)
    {
      sfetch = new jalview.ws.SequenceFetcher();
    }
    return sfetch;
  }

  /**
   * Constructor given a client to receive any status or progress messages
   * (currently either the Desktop, or an AlignFrame panel)
   * 
   * @param guiIndic
   */
  public SequenceFetcher(IProgressIndicator guiIndic)
  {
    this(guiIndic, null, null);
  }

  /**
   * Constructor with specified database and accession(s) to retrieve
   * 
   * @param guiIndic
   * @param selectedDb
   * @param queryString
   */
  public SequenceFetcher(IProgressIndicator guiIndic,
          final String selectedDb, final String queryString)
  {
    this.progressIndicator = guiIndic;
    getSequenceFetcherSingleton();
    this.guiWindow = progressIndicator;

    if (progressIndicator instanceof AlignFrame)
    {
      alignFrame = (AlignFrame) progressIndicator;
    }

    jbInit(selectedDb);
    textArea.setText(queryString);

    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setFrameIcon(null);
    Desktop.addInternalFrame(frame, getFrameTitle(), true, 400,
            Platform.isAMacAndNotJS() ? 240 : 180);
  }

  private String getFrameTitle()
  {
    return ((alignFrame == null)
            ? MessageManager.getString("label.new_sequence_fetcher")
            : MessageManager
                    .getString("label.additional_sequence_fetcher"));
  }

  private void jbInit(String selectedDb)
  {
    this.setLayout(new BorderLayout());

    database = new JComboBox<>();
    database.setFont(JvSwingUtils.getLabelFont());
    StringPair instructionItem = new StringPair(
            MessageManager.getString("action.select_ddbb"));
    database.setPrototypeDisplayValue(instructionItem);
    String[] sources = new jalview.ws.SequenceFetcher().getSupportedDb();
    Arrays.sort(sources, String.CASE_INSENSITIVE_ORDER);
    database.addItem(instructionItem);
    for (String source : sources)
    {
      List<DbSourceProxy> slist = sfetch.getSourceProxy(source);
      if (slist.size() == 1 && slist.get(0) != null)
      {
        database.addItem(new StringPair(source, slist.get(0).getDbName()));
      }
      else
      {
        database.addItem(new StringPair(source));
      }
    }
    setDatabaseSelectedItem(selectedDb);
    if (database.getSelectedIndex() == -1)
    {
      database.setSelectedIndex(0);
    }
    database.setMaximumRowCount(database.getItemCount());
    database.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String currentSelection = ((StringPair) database.getSelectedItem())
                .getKey();
        updateExampleQuery(currentSelection);

        if ("pdb".equalsIgnoreCase(currentSelection))
        {
          frame.dispose();
          new PDBFTSPanel(SequenceFetcher.this);
        }
        else if ("uniprot".equalsIgnoreCase(currentSelection))
        {
          frame.dispose();
          new UniprotFTSPanel(SequenceFetcher.this);
        }
        else if ("3d-beacons".equalsIgnoreCase(currentSelection))
        {
          frame.dispose();
          new TDBeaconsFTSPanel(SequenceFetcher.this);
        }
        else
        {
          otherSourceAction();
        }
      }
    });

    exampleAccession = new JLabel("");
    exampleAccession.setFont(new Font("Verdana", Font.BOLD, 11));
    JLabel jLabel1 = new JLabel(MessageManager
            .getString("label.separate_multiple_accession_ids"));
    jLabel1.setFont(new Font("Verdana", Font.ITALIC, 11));
    jLabel1.setHorizontalAlignment(SwingConstants.LEFT);

    replacePunctuation = new JCheckBox(
            MessageManager.getString("label.replace_commas_semicolons"));
    replacePunctuation.setHorizontalAlignment(SwingConstants.LEFT);
    replacePunctuation.setFont(new Font("Verdana", Font.ITALIC, 11));
    okBtn = new JButton(MessageManager.getString("action.ok"));
    okBtn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed();
      }
    });
    JButton clear = new JButton(MessageManager.getString("action.clear"));
    clear.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        clear_actionPerformed();
      }
    });

    exampleBtn = new JButton(MessageManager.getString("label.example"));
    exampleBtn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        example_actionPerformed();
      }
    });
    closeBtn = new JButton(MessageManager.getString("action.cancel"));
    closeBtn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed(e);
      }
    });
    backBtn = new JButton(MessageManager.getString("action.back"));
    backBtn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        parentSearchPanel.btn_back_ActionPerformed();
      }
    });
    // back not visible unless embedded
    backBtn.setVisible(false);

    textArea = new JTextArea();
    textArea.setFont(JvSwingUtils.getLabelFont());
    textArea.setLineWrap(true);
    textArea.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          ok_actionPerformed();
        }
      }
    });

    JPanel actionPanel = new JPanel();
    actionPanel.add(backBtn);
    actionPanel.add(exampleBtn);
    actionPanel.add(clear);
    actionPanel.add(okBtn);
    actionPanel.add(closeBtn);

    JPanel databasePanel = new JPanel();
    databasePanel.setLayout(new BorderLayout());
    databasePanel.add(database, BorderLayout.NORTH);
    databasePanel.add(exampleAccession, BorderLayout.CENTER);
    JPanel jPanel2a = new JPanel(new BorderLayout());
    jPanel2a.add(jLabel1, BorderLayout.NORTH);
    jPanel2a.add(replacePunctuation, BorderLayout.SOUTH);
    databasePanel.add(jPanel2a, BorderLayout.SOUTH);

    JPanel idsPanel = new JPanel();
    idsPanel.setLayout(new BorderLayout(0, 5));
    JScrollPane jScrollPane1 = new JScrollPane();
    jScrollPane1.getViewport().add(textArea);
    idsPanel.add(jScrollPane1, BorderLayout.CENTER);

    this.add(actionPanel, BorderLayout.SOUTH);
    this.add(idsPanel, BorderLayout.CENTER);
    this.add(databasePanel, BorderLayout.NORTH);
  }

  private void setDatabaseSelectedItem(String db)
  {
    for (int i = 0; i < database.getItemCount(); i++)
    {
      StringPair sp = database.getItemAt(i);
      if (sp != null && db != null && db.equals(sp.getKey()))
      {
        database.setSelectedIndex(i);
        return;
      }
    }
  }

  /**
   * Answers a semi-colon-delimited string with the example query or queries for
   * the selected database
   * 
   * @param db
   * @return
   */
  protected String getExampleQueries(String db)
  {
    StringBuilder sb = new StringBuilder();
    HashSet<String> hs = new HashSet<>();
    for (DbSourceProxy dbs : sfetch.getSourceProxy(db))
    {
      String tq = dbs.getTestQuery();
      if (hs.add(tq)) // not a duplicate source
      {
        if (sb.length() > 0)
        {
          sb.append(";");
        }
        sb.append(tq);
      }
    }
    return sb.toString();
  }

  /**
   * Action on selecting a database other than Uniprot or PDB is to enable or
   * disable 'Replace commas', and await input in the query field
   */
  protected void otherSourceAction()
  {
    try
    {
      String eq = exampleAccession.getText();
      // TODO this should be a property of the SequenceFetcher whether commas
      // are allowed in the IDs...

      boolean enablePunct = !(eq != null && eq.indexOf(",") > -1);
      replacePunctuation.setEnabled(enablePunct);

    } catch (Exception ex)
    {
      exampleAccession.setText("");
      replacePunctuation.setEnabled(true);
    }
    repaint();
  }

  /**
   * Sets the text of the example query to incorporate the example accession
   * provided by the selected database source
   * 
   * @param selectedDatabase
   * @return
   */
  protected String updateExampleQuery(String selectedDatabase)
  {
    String eq = getExampleQueries(selectedDatabase);
    exampleAccession.setText(MessageManager
            .formatMessage("label.example_query_param", new String[]
            { eq }));
    return eq;
  }

  /**
   * Action on clicking the 'Example' button is to write the example accession
   * as the query text field value
   */
  protected void example_actionPerformed()
  {
    String eq = getExampleQueries(
            ((StringPair) database.getSelectedItem()).getKey());
    textArea.setText(eq);
    repaint();
  }

  /**
   * Clears the query input field
   */
  protected void clear_actionPerformed()
  {
    textArea.setText("");
    repaint();
  }

  /**
   * Action on Close button is to close this frame, and also (if it is embedded
   * in a search panel) to close the search panel
   * 
   * @param e
   */
  protected void close_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
      if (parentSearchPanel != null)
      {
        parentSearchPanel.btn_cancel_ActionPerformed();
      }
    } catch (Exception ex)
    {
    }
  }

  /**
   * Action on OK is to start the fetch for entered accession(s)
   */
  public void ok_actionPerformed()
  {
    /*
     * tidy inputs and check there is something to search for
     */
    String t0 = textArea.getText();
    String text = t0.trim();
    if (replacePunctuation.isEnabled() && replacePunctuation.isSelected())
    {
      text = text.replace(",", ";");
    }
    text = text.replaceAll("(\\s|[; ])+", ";");
    if (!t0.equals(text))
    {
      textArea.setText(text);
    }
    if (text.isEmpty())
    {
      // todo i18n
      showErrorMessage(
              "Please enter a (semi-colon separated list of) database id(s)");
      resetDialog();
      return;
    }
    if (database.getSelectedIndex() == 0)
    {
      // todo i18n
      showErrorMessage("Please choose a database");
      resetDialog();
      return;
    }

    exampleBtn.setEnabled(false);
    textArea.setEnabled(false);
    okBtn.setEnabled(false);
    closeBtn.setEnabled(false);
    backBtn.setEnabled(false);

    Thread worker = new Thread(this);
    worker.start();
  }

  private void resetDialog()
  {
    exampleBtn.setEnabled(true);
    textArea.setEnabled(true);
    okBtn.setEnabled(true);
    closeBtn.setEnabled(true);
    backBtn.setEnabled(parentSearchPanel != null);
  }

  @Override
  public void run()
  {
    boolean addToLast = false;
    List<String> aresultq = new ArrayList<>();
    List<String> presultTitle = new ArrayList<>();
    List<AlignmentI> presult = new ArrayList<>();
    List<AlignmentI> aresult = new ArrayList<>();
    List<DbSourceProxy> sources = sfetch.getSourceProxy(
            ((StringPair) database.getSelectedItem()).getKey());
    Iterator<DbSourceProxy> proxies = sources.iterator();
    String[] qries = textArea.getText().trim().split(";");
    List<String> nextFetch = Arrays.asList(qries);
    Iterator<String> en = Arrays.asList(new String[0]).iterator();
    int nqueries = qries.length;

    FeatureSettingsModelI preferredFeatureColours = null;
    while (proxies.hasNext() && (en.hasNext() || nextFetch.size() > 0))
    {
      if (!en.hasNext() && nextFetch.size() > 0)
      {
        en = nextFetch.iterator();
        nqueries = nextFetch.size();
        // save the remaining queries in the original array
        qries = nextFetch.toArray(new String[nqueries]);
        nextFetch = new ArrayList<>();
      }

      DbSourceProxy proxy = proxies.next();
      try
      {
        // update status
        guiWindow.setProgressBar(MessageManager.formatMessage(
                "status.fetching_sequence_queries_from", new String[]
                { Integer.valueOf(nqueries).toString(),
                    proxy.getDbName() }),
                Thread.currentThread().hashCode());
        if (proxy.getMaximumQueryCount() == 1)
        {
          /*
           * proxy only handles one accession id at a time
           */
          while (en.hasNext())
          {
            String acc = en.next();
            if (!fetchSingleAccession(proxy, acc, aresultq, aresult))
            {
              nextFetch.add(acc);
            }
          }
        }
        else
        {
          /*
           * proxy can fetch multiple accessions at one time
           */
          fetchMultipleAccessions(proxy, en, aresultq, aresult, nextFetch);
        }
      } catch (Exception e)
      {
        showErrorMessage("Error retrieving " + textArea.getText() + " from "
                + ((StringPair) database.getSelectedItem()).getDisplay());
        // error
        // +="Couldn't retrieve sequences from "+database.getSelectedItem();
        System.err.println("Retrieval failed for source ='"
                + ((StringPair) database.getSelectedItem()).getDisplay()
                + "' and query\n'" + textArea.getText() + "'\n");
        e.printStackTrace();
      } catch (OutOfMemoryError e)
      {
        showErrorMessage("Out of Memory when retrieving "
                + textArea.getText() + " from "
                + ((StringPair) database.getSelectedItem()).getDisplay()
                + "\nPlease see the Jalview FAQ for instructions for increasing the memory available to Jalview.\n");
        e.printStackTrace();
      } catch (Error e)
      {
        showErrorMessage("Serious Error retrieving " + textArea.getText()
                + " from "
                + ((StringPair) database.getSelectedItem()).getDisplay());
        e.printStackTrace();
      }

      // Stack results ready for opening in alignment windows
      if (aresult != null && aresult.size() > 0)
      {
        FeatureSettingsModelI proxyColourScheme = proxy
                .getFeatureColourScheme();
        if (proxyColourScheme != null)
        {
          preferredFeatureColours = proxyColourScheme;
        }

        AlignmentI ar = null;
        if (proxy.isAlignmentSource())
        {
          addToLast = false;
          // new window for each result
          while (aresult.size() > 0)
          {
            presult.add(aresult.remove(0));
            presultTitle.add(
                    aresultq.remove(0) + " " + getDefaultRetrievalTitle());
          }
        }
        else
        {
          String titl = null;
          if (addToLast && presult.size() > 0)
          {
            ar = presult.remove(presult.size() - 1);
            titl = presultTitle.remove(presultTitle.size() - 1);
          }
          // concatenate all results in one window
          while (aresult.size() > 0)
          {
            if (ar == null)
            {
              ar = aresult.remove(0);
            }
            else
            {
              ar.append(aresult.remove(0));
            }
          }
          addToLast = true;
          presult.add(ar);
          presultTitle.add(titl);
        }
      }
      guiWindow.setProgressBar(
              MessageManager.getString("status.finshed_querying"),
              Thread.currentThread().hashCode());
    }
    guiWindow
            .setProgressBar(
                    (presult.size() > 0)
                            ? MessageManager
                                    .getString("status.parsing_results")
                            : MessageManager.getString("status.processing"),
                    Thread.currentThread().hashCode());
    // process results
    while (presult.size() > 0)
    {
      parseResult(presult.remove(0), presultTitle.remove(0), null,
              preferredFeatureColours);
    }
    // only remove visual delay after we finished parsing.
    guiWindow.setProgressBar(null, Thread.currentThread().hashCode());
    if (nextFetch.size() > 0)
    {
      StringBuffer sb = new StringBuffer();
      sb.append("Didn't retrieve the following "
              + (nextFetch.size() == 1 ? "query"
                      : nextFetch.size() + " queries")
              + ": \n");
      int l = sb.length(), lr = 0;
      for (String s : nextFetch)
      {
        if (l != sb.length())
        {
          sb.append("; ");
        }
        if (lr - sb.length() > 40)
        {
          sb.append("\n");
        }
        sb.append(s);
      }
      showErrorMessage(sb.toString());
    }
    resetDialog();
  }

  /**
   * Tries to fetch one or more accession ids from the database proxy
   * 
   * @param proxy
   * @param accessions
   *          the queries to fetch
   * @param aresultq
   *          a successful queries list to add to
   * @param aresult
   *          a list of retrieved alignments to add to
   * @param nextFetch
   *          failed queries are added to this list
   * @throws Exception
   */
  void fetchMultipleAccessions(DbSourceProxy proxy,
          Iterator<String> accessions, List<String> aresultq,
          List<AlignmentI> aresult, List<String> nextFetch) throws Exception
  {
    StringBuilder multiacc = new StringBuilder();
    List<String> tosend = new ArrayList<>();
    while (accessions.hasNext())
    {
      String nel = accessions.next();
      tosend.add(nel);
      multiacc.append(nel);
      if (accessions.hasNext())
      {
        multiacc.append(proxy.getAccessionSeparator());
      }
    }

    try
    {
      String query = multiacc.toString();
      AlignmentI rslt = proxy.getSequenceRecords(query);
      if (rslt == null || rslt.getHeight() == 0)
      {
        // no results - pass on all queries to next source
        nextFetch.addAll(tosend);
      }
      else
      {
        aresultq.add(query);
        aresult.add(rslt);
        if (tosend.size() > 1)
        {
          checkResultForQueries(rslt, tosend, nextFetch, proxy);
        }
      }
    } catch (OutOfMemoryError oome)
    {
      new OOMWarning("fetching " + multiacc + " from "
              + ((StringPair) database.getSelectedItem()).getDisplay(),
              oome, this);
    }
  }

  /**
   * Query for a single accession id via the database proxy
   * 
   * @param proxy
   * @param accession
   * @param aresultq
   *          a list of successful queries to add to
   * @param aresult
   *          a list of retrieved alignments to add to
   * @return true if the fetch was successful, else false
   */
  boolean fetchSingleAccession(DbSourceProxy proxy, String accession,
          List<String> aresultq, List<AlignmentI> aresult)
  {
    boolean success = false;
    try
    {
      if (aresult != null)
      {
        try
        {
          // give the server a chance to breathe
          Thread.sleep(5);
        } catch (Exception e)
        {
          //
        }
      }

      AlignmentI indres = null;
      try
      {
        indres = proxy.getSequenceRecords(accession);
      } catch (OutOfMemoryError oome)
      {
        new OOMWarning(
                "fetching " + accession + " from " + proxy.getDbName(),
                oome, this);
      }
      if (indres != null)
      {
        aresultq.add(accession);
        aresult.add(indres);
        success = true;
      }
    } catch (Exception e)
    {
      Console.info("Error retrieving " + accession + " from "
              + proxy.getDbName(), e);
    }
    return success;
  }

  /**
   * Checks which of the queries were successfully retrieved by searching the
   * DBRefs of the retrieved sequences for a match. Any not found are added to
   * the 'nextFetch' list.
   * 
   * @param rslt
   * @param queries
   * @param nextFetch
   * @param proxy
   */
  void checkResultForQueries(AlignmentI rslt, List<String> queries,
          List<String> nextFetch, DbSourceProxy proxy)
  {
    SequenceI[] rs = rslt.getSequencesArray();

    for (String q : queries)
    {
      // BH 2019.01.25 dbr is never used.
      // DBRefEntry dbr = new DBRefEntry();
      // dbr.setSource(proxy.getDbSource());
      // dbr.setVersion(null);
      String accId = proxy.getAccessionIdFromQuery(q);
      // dbr.setAccessionId(accId);
      boolean rfound = false;
      for (int r = 0, nr = rs.length; r < nr; r++)
      {
        if (rs[r] != null)
        {
          List<DBRefEntry> found = DBRefUtils.searchRefs(rs[r].getDBRefs(),
                  accId);
          if (!found.isEmpty())
          {
            rfound = true;
            break;
          }
        }
      }
      if (!rfound)
      {
        nextFetch.add(q);
      }
    }
  }

  /**
   * 
   * @return a standard title for any results retrieved using the currently
   *         selected source and settings
   */
  public String getDefaultRetrievalTitle()
  {
    return "Retrieved from "
            + ((StringPair) database.getSelectedItem()).getDisplay();
  }

  /**
   * constructs an alignment frame given the data and metadata
   * 
   * @param al
   * @param title
   * @param currentFileFormat
   * @param preferredFeatureColours
   * @return the alignment
   */
  public AlignmentI parseResult(AlignmentI al, String title,
          FileFormatI currentFileFormat,
          FeatureSettingsModelI preferredFeatureColours)
  {

    if (al != null && al.getHeight() > 0)
    {
      if (title == null)
      {
        title = getDefaultRetrievalTitle();
      }
      if (alignFrame == null)
      {
        AlignFrame af = new AlignFrame(al, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);
        if (currentFileFormat != null)
        {
          af.currentFileFormat = currentFileFormat;
        }

        List<SequenceI> alsqs = al.getSequences();
        synchronized (alsqs)
        {
          for (SequenceI sq : alsqs)
          {
            if (sq.getFeatures().hasFeatures())
            {
              af.setShowSeqFeatures(true);
              break;
            }
          }
        }

        af.getViewport().applyFeaturesStyle(preferredFeatureColours);
        if (Cache.getDefault("HIDE_INTRONS", true))
        {
          af.hideFeatureColumns(SequenceOntologyI.EXON, false);
        }
        Desktop.addInternalFrame(af, title, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);

        af.setStatus(MessageManager
                .getString("label.successfully_pasted_alignment_file"));

        try
        {
          af.setMaximum(Cache.getDefault("SHOW_FULLSCREEN", false));
        } catch (Exception ex)
        {
        }
      }
      else
      {
        alignFrame.viewport.addAlignment(al, title);
      }
    }
    return al;
  }

  void showErrorMessage(final String error)
  {
    resetDialog();
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JvOptionPane.showInternalMessageDialog(Desktop.desktop, error,
                MessageManager.getString("label.error_retrieving_data"),
                JvOptionPane.WARNING_MESSAGE);
      }
    });
  }

  public IProgressIndicator getProgressIndicator()
  {
    return progressIndicator;
  }

  public void setProgressIndicator(IProgressIndicator progressIndicator)
  {
    this.progressIndicator = progressIndicator;
  }

  /**
   * Hide this panel (on clicking the database button to open the database
   * chooser)
   */
  void hidePanel()
  {
    frame.setVisible(false);
  }

  public void setQuery(String ids)
  {
    textArea.setText(ids);
  }

  /**
   * Called to modify the search panel for embedding as an alternative tab of a
   * free text search panel. The database choice list is hidden (since the
   * choice has been made), and a Back button is made visible (which reopens the
   * Sequence Fetcher panel).
   * 
   * @param parentPanel
   */
  public void embedIn(GFTSPanel parentPanel)
  {
    database.setVisible(false);
    backBtn.setVisible(true);
    parentSearchPanel = parentPanel;
  }
}
