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
package jalview.appletgui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jalview.api.AlignViewportI;
import jalview.api.FinderI;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;

public class Finder extends Panel implements ActionListener
{
  private AlignViewportI av;

  private AlignmentPanel ap;

  private TextField textfield = new TextField();

  private Button findAll = new Button();

  private Button findNext = new Button();

  private Button createFeatures = new Button();

  private Checkbox caseSensitive = new Checkbox();

  private Checkbox searchDescription = new Checkbox();

  private SearchResultsI searchResults;

  /*
   * Finder agent per viewport searched
   */
  Map<AlignViewportI, FinderI> finders;

  public Finder(final AlignmentPanel ap)
  {
    finders = new HashMap<>();

    try
    {
      jbInit();

    } catch (Exception e)
    {
      e.printStackTrace();
    }

    this.av = ap.av;
    this.ap = ap;
    Frame frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("action.find"), 340, 120);
    frame.repaint();
    frame.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        ap.highlightSearchResults(null);
      }
    });
    textfield.requestFocus();
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == textfield)
    {
      doSearch(false);
    }

    else if (evt.getSource() == findNext)
    {
      doSearch(false);
    }

    else if (evt.getSource() == findAll)
    {
      doSearch(true);
    }
    else if (evt.getSource() == createFeatures)
    {
      createFeatures_actionPerformed();
    }
  }

  public void createFeatures_actionPerformed()
  {
    List<SequenceI> seqs = new ArrayList<>();
    List<SequenceFeature> features = new ArrayList<>();
    String searchString = textfield.getText().trim();

    for (SearchResultMatchI match : searchResults.getResults())
    {
      seqs.add(match.getSequence().getDatasetSequence());
      features.add(new SequenceFeature(searchString, "Search Results",
              match.getStart(), match.getEnd(), "Search Results"));
    }

    if (ap.seqPanel.seqCanvas.getFeatureRenderer().amendFeatures(seqs,
            features, true, ap))
    {
      ap.alignFrame.sequenceFeatures.setState(true);
      av.setShowSequenceFeatures(true);
      ap.highlightSearchResults(null);
    }
  }

  void doSearch(boolean doFindAll)
  {
    if (ap.av.applet.currentAlignFrame != null)
    {
      ap = ap.av.applet.currentAlignFrame.alignPanel;
      av = ap.av;
    }
    createFeatures.setEnabled(false);
    FinderI finder = finders.get(av);
    if (finder == null)
    {
      /*
       * first time we searched this viewport
       */
      finder = new jalview.analysis.Finder(av);
      finders.put(av, finder);
    }

    String searchString = textfield.getText();
    boolean isCaseSensitive = caseSensitive.getState();
    boolean doSearchDescription = searchDescription.getState();
    if (doFindAll)
    {
      finder.findAll(searchString, isCaseSensitive, doSearchDescription,
              false, false);
    }
    else
    {
      finder.findNext(searchString, isCaseSensitive, doSearchDescription,
              false, false);
    }

    searchResults = finder.getSearchResults();

    List<SequenceI> idMatches = finder.getIdMatches();
    ap.idPanel.highlightSearchResults(idMatches);

    if (searchResults.isEmpty())
    {
      searchResults = null;
    }
    else
    {
      createFeatures.setEnabled(true);
    }

    // if allResults is null, this effectively switches displaySearch flag in
    // seqCanvas
    ap.highlightSearchResults(searchResults);
    // TODO: add enablers for 'SelectSequences' or 'SelectColumns' or
    // 'SelectRegion' selection
    if (idMatches.isEmpty() && searchResults == null)
    {
      ap.alignFrame.statusBar.setText(
              MessageManager.getString("label.finished_searching"));
    }
    else
    {
      if (doFindAll)
      {
        String message = (idMatches.size() > 0)
                ? "" + idMatches.size() + " IDs"
                : "";
        if (idMatches.size() > 0 && searchResults != null
                && searchResults.getCount() > 0)
        {
          message += " and ";
        }
        if (searchResults != null)
        {
          message += searchResults.getCount() + " subsequence matches.";
        }
        ap.alignFrame.statusBar.setText(MessageManager
                .formatMessage("label.search_results", new String[]
                { searchString, message }));

      }
      else
      {
        // TODO: indicate sequence and matching position in status bar
        ap.alignFrame.statusBar.setText(MessageManager
                .formatMessage("label.found_match_for", new String[]
                { searchString }));
      }
    }
  }

  private void jbInit() throws Exception
  {
    Label jLabel1 = new Label(MessageManager.getString("action.find"));
    jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel1.setBounds(new Rectangle(3, 30, 34, 15));
    this.setLayout(null);
    textfield.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    textfield.setText("");
    textfield.setBounds(new Rectangle(40, 17, 133, 21));
    textfield.addKeyListener(new java.awt.event.KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent e)
      {
        textfield_keyTyped();
      }
    });
    textfield.addActionListener(this);
    findAll.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    findAll.setLabel(MessageManager.getString("action.find_all"));
    findAll.addActionListener(this);
    findNext.setEnabled(false);
    findNext.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    findNext.setLabel(MessageManager.getString("action.find_next"));
    findNext.addActionListener(this);

    Panel actionsPanel = new Panel();
    actionsPanel.setBounds(new Rectangle(195, 5, 141, 64));
    GridLayout gridLayout1 = new GridLayout();
    actionsPanel.setLayout(gridLayout1);
    gridLayout1.setHgap(0);
    gridLayout1.setRows(3);
    gridLayout1.setVgap(2);
    createFeatures.setEnabled(false);
    createFeatures.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    createFeatures.setLabel(MessageManager.getString("label.new_feature"));
    createFeatures.addActionListener(this);
    caseSensitive.setLabel(MessageManager.getString("label.match_case"));
    caseSensitive.setBounds(new Rectangle(30, 39, 126, 23));

    searchDescription.setLabel(
            MessageManager.getString("label.include_description"));
    searchDescription.setBounds(new Rectangle(30, 59, 170, 23));
    actionsPanel.add(findNext, null);
    actionsPanel.add(findAll, null);
    actionsPanel.add(createFeatures, null);
    this.add(caseSensitive);
    this.add(textfield, null);
    this.add(jLabel1, null);
    this.add(actionsPanel, null);
    this.add(searchDescription);
  }

  void textfield_keyTyped()
  {
    findNext.setEnabled(true);
  }

}
