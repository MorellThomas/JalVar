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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jalview.api.AlignViewportI;
import jalview.api.FinderI;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.jbgui.GFinder;
import jalview.util.MessageManager;

/**
 * Performs the menu option for searching the alignment, for the next or all
 * matches. If matches are found, they are highlighted, and the user has the
 * option to create a new feature on the alignment for the matched positions.
 * 
 * Searches can be for a simple base sequence, or may use a regular expression.
 * Any gaps are ignored.
 * 
 * @author $author$
 * @version $Revision$
 */
public class Finder extends GFinder
{
  private static final int MIN_WIDTH = 350;

  private static final int MIN_HEIGHT = 120;

  private static final int MY_HEIGHT = 150;

  private static final int MY_WIDTH = 400;

  private AlignViewportI av;

  private AlignmentPanel ap;

  private JInternalFrame frame;

  /*
   * Finder agent per viewport searched
   */
  private Map<AlignViewportI, FinderI> finders;

  private SearchResultsI searchResults;

  /*
   * true if Finder always acts on the same alignment,
   * false if it acts on the alignment with focus
   */
  private boolean focusFixed;

  /**
   * Constructor given an associated alignment panel. Constructs and displays an
   * internal frame where the user can enter a search string. The Finder may
   * have 'fixed focus' (always act the panel for which it is constructed), or
   * not (acts on the alignment that has focus). An optional 'scope' may be
   * added to be shown in the title of the Finder frame.
   * 
   * @param alignPanel
   * @param fixedFocus
   * @param scope
   */
  public Finder(AlignmentPanel alignPanel, boolean fixedFocus, String scope)
  {
    av = alignPanel.getAlignViewport();
    ap = alignPanel;
    focusFixed = fixedFocus;
    finders = new HashMap<>();
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    frame.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(InternalFrameEvent e)
      {
        closeAction();
      }
    });
    frame.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        /*
         * ensure 'ignore hidden columns' is only enabled
         * if the alignment with focus has hidden columns
         */
        getFocusedViewport();
      }
    });

    addEscapeHandler();

    String title = MessageManager.getString("label.find");
    if (scope != null)
    {
      title += " " + scope;
    }
    Desktop.addInternalFrame(frame, title, MY_WIDTH, MY_HEIGHT);
    frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    searchBox.getComponent().requestFocus();
  }

  /**
   * Add a handler for the Escape key when the window has focus
   */
  private void addEscapeHandler()
  {
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
    getRootPane().getActionMap().put("Cancel", new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeAction();
      }
    });
  }

  /**
   * Performs the 'Find Next' action on the alignment panel with focus
   */
  @Override
  public void findNext_actionPerformed()
  {
    if (getFocusedViewport())
    {
      doSearch(false);
    }
  }

  /**
   * Performs the 'Find All' action on the alignment panel with focus
   */
  @Override
  public void findAll_actionPerformed()
  {
    if (getFocusedViewport())
    {
      doSearch(true);
    }
  }

  /**
   * if !focusfixed and not in a desktop environment, checks that av and ap are
   * valid. Otherwise, gets the topmost alignment window and sets av and ap
   * accordingly. Also sets the 'ignore hidden' checkbox disabled if the
   * viewport has no hidden columns.
   * 
   * @return false if no alignment window was found
   */
  boolean getFocusedViewport()
  {
    if (focusFixed || Desktop.desktop == null)
    {
      if (ap != null && av != null)
      {
        ignoreHidden.setEnabled(av.hasHiddenColumns());
        return true;
      }
      // we aren't in a desktop environment, so give up now.
      return false;
    }
    // now checks further down the window stack to fix bug
    // https://mantis.lifesci.dundee.ac.uk/view.php?id=36008
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();
    for (int f = 0; f < frames.length; f++)
    {
      JInternalFrame alignFrame = frames[f];
      if (alignFrame != null && alignFrame instanceof AlignFrame
              && !alignFrame.isIcon())
      {
        av = ((AlignFrame) alignFrame).viewport;
        ap = ((AlignFrame) alignFrame).alignPanel;
        ignoreHidden.setEnabled(av.hasHiddenColumns());
        return true;
      }
    }
    return false;
  }

  /**
   * Opens a dialog that allows the user to create sequence features for the
   * find match results
   */
  @Override
  public void createFeatures_actionPerformed()
  {
    if (searchResults.isEmpty())
    {
      return; // shouldn't happen
    }
    List<SequenceI> seqs = new ArrayList<>();
    List<SequenceFeature> features = new ArrayList<>();

    String searchString = searchBox.getUserInput();
    String desc = "Search Results";

    /*
     * assemble dataset sequences, and template new sequence features,
     * for the amend features dialog
     */
    for (SearchResultMatchI match : searchResults.getResults())
    {
      seqs.add(match.getSequence().getDatasetSequence());
      features.add(new SequenceFeature(searchString, desc, match.getStart(),
              match.getEnd(), desc));
    }

    new FeatureEditor(ap, seqs, features, true).showDialog();
  }

  @Override
  protected void copyToClipboard_actionPerformed()
  {
    if (searchResults.isEmpty())
    {
      return; // shouldn't happen
    }
    // assume viewport controller has same searchResults as we do...
    ap.alignFrame.avc.copyHighlightedRegionsToClipboard();
  }

  /**
   * Search the alignment for the next or all matches. If 'all matches', a
   * dialog is shown with the number of sequence ids and subsequences matched.
   * 
   * @param doFindAll
   */
  void doSearch(boolean doFindAll)
  {
    createFeatures.setEnabled(false);
    copyToClipboard.setEnabled(false);

    String searchString = searchBox.getUserInput();

    if (isInvalidSearchString(searchString))
    {
      return;
    }
    // TODO: extend finder to match descriptions, features and annotation, and
    // other stuff
    // TODO: add switches to control what is searched - sequences, IDS,
    // descriptions, features
    FinderI finder = finders.get(av);
    if (finder == null)
    {
      /*
       * first time we've searched this viewport
       */
      finder = new jalview.analysis.Finder(av);
      finders.put(av, finder);
    }
    finder.setFeatureRenderer(ap.getFeatureRenderer());

    boolean isCaseSensitive = caseSensitive.isSelected();
    boolean doSearchDescription = searchDescription.isSelected();
    boolean doSearchfeatures = searchFeatures.isSelected();
    boolean skipHidden = ignoreHidden.isSelected();
    if (doFindAll)
    {
      finder.findAll(searchString, isCaseSensitive, doSearchDescription,
              doSearchfeatures, skipHidden);
    }
    else
    {
      finder.findNext(searchString, isCaseSensitive, doSearchDescription,
              doSearchfeatures, skipHidden);
    }

    searchResults = finder.getSearchResults();
    List<SequenceI> idMatch = finder.getIdMatches();
    ap.getIdPanel().highlightSearchResults(idMatch);

    if (searchResults.isEmpty())
    {
      searchResults = null;
    }
    else
    {
      createFeatures.setEnabled(true);
      copyToClipboard.setEnabled(true);
    }

    searchBox.updateCache();

    ap.highlightSearchResults(searchResults);
    // TODO: add enablers for 'SelectSequences' or 'SelectColumns' or
    // 'SelectRegion' selection
    if (idMatch.isEmpty() && searchResults == null)
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.getString("label.finished_searching"), null,
              JvOptionPane.PLAIN_MESSAGE);
    }
    else
    {
      if (doFindAll)
      {
        // then we report the matches that were found
        StringBuilder message = new StringBuilder();
        if (idMatch.size() > 0)
        {
          message.append(idMatch.size()).append(" IDs");
        }
        if (searchResults != null)
        {
          if (idMatch.size() > 0 && searchResults.getCount() > 0)
          {
            message.append(" ").append(MessageManager.getString("label.and")
                    .toLowerCase(Locale.ROOT)).append(" ");
          }
          message.append(MessageManager.formatMessage(
                  "label.subsequence_matches_found",
                  searchResults.getCount()));
        }
        JvOptionPane.showInternalMessageDialog(this, message.toString(),
                null, JvOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  /**
   * Displays an error dialog, and answers false, if the search string is
   * invalid, else answers true.
   * 
   * @param searchString
   * @return
   */
  protected boolean isInvalidSearchString(String searchString)
  {
    String error = getSearchValidationError(searchString);
    if (error == null)
    {
      return false;
    }
    JvOptionPane.showInternalMessageDialog(this, error,
            MessageManager.getString("label.invalid_search"), // $NON-NLS-1$
            JvOptionPane.ERROR_MESSAGE);
    return true;
  }

  /**
   * Returns an error message string if the search string is invalid, else
   * returns null.
   * 
   * Currently validation is limited to checking the string is not empty, and is
   * a valid regular expression (simple searches for base sub-sequences will
   * pass this test). Additional validations may be added in future if the
   * search syntax is expanded.
   * 
   * @param searchString
   * @return
   */
  protected String getSearchValidationError(String searchString)
  {
    String error = null;
    if (searchString == null || searchString.length() == 0)
    {
      error = MessageManager.getString("label.invalid_search");
    }
    try
    {
      Pattern.compile(searchString);
    } catch (PatternSyntaxException e)
    {
      error = MessageManager.getString("error.invalid_regex") + ": "
              + e.getDescription();
    }
    return error;
  }

  protected void closeAction()
  {
    frame.setVisible(false);
    frame.dispose();
    searchBox.persistCache();
    if (getFocusedViewport())
    {
      ap.alignFrame.requestFocus();
    }
  }
}
