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

import java.awt.CheckboxMenuItem;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import jalview.analysis.AAFrequency;
import jalview.analysis.AlignmentAnnotationUtils;
import jalview.analysis.AlignmentUtils;
import jalview.analysis.Conservation;
import jalview.bin.JalviewLite;
import jalview.commands.ChangeCaseCommand;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.SequenceAnnotationReport;
import jalview.renderer.ResidueShader;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ClustalxColourScheme;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.JalviewColourScheme;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.PurinePyrimidineColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.util.MessageManager;
import jalview.util.UrlLink;

public class APopupMenu extends java.awt.PopupMenu
        implements ActionListener, ItemListener
{
  Menu groupMenu = new Menu();

  MenuItem editGroupName = new MenuItem();

  CheckboxMenuItem noColour = new CheckboxMenuItem();

  protected CheckboxMenuItem clustalColour = new CheckboxMenuItem();

  protected CheckboxMenuItem zappoColour = new CheckboxMenuItem();

  protected CheckboxMenuItem taylorColour = new CheckboxMenuItem();

  protected CheckboxMenuItem hydrophobicityColour = new CheckboxMenuItem();

  protected CheckboxMenuItem helixColour = new CheckboxMenuItem();

  protected CheckboxMenuItem strandColour = new CheckboxMenuItem();

  protected CheckboxMenuItem turnColour = new CheckboxMenuItem();

  protected CheckboxMenuItem buriedColour = new CheckboxMenuItem();

  protected CheckboxMenuItem PIDColour = new CheckboxMenuItem();

  protected CheckboxMenuItem BLOSUM62Colour = new CheckboxMenuItem();

  CheckboxMenuItem nucleotideColour = new CheckboxMenuItem();

  CheckboxMenuItem purinePyrimidineColour = new CheckboxMenuItem();

  protected MenuItem userDefinedColour = new MenuItem();

  protected CheckboxMenuItem abovePIDColour = new CheckboxMenuItem();

  MenuItem modifyPID = new MenuItem();

  protected CheckboxMenuItem conservationColour = new CheckboxMenuItem();

  MenuItem modifyConservation = new MenuItem();

  MenuItem noColourmenuItem = new MenuItem();

  final AlignmentPanel ap;

  MenuItem unGroupMenuItem = new MenuItem();

  MenuItem createGroupMenuItem = new MenuItem();

  Menu colourMenu = new Menu();

  CheckboxMenuItem showBoxes = new CheckboxMenuItem();

  CheckboxMenuItem showText = new CheckboxMenuItem();

  CheckboxMenuItem showColourText = new CheckboxMenuItem();

  CheckboxMenuItem displayNonconserved = new CheckboxMenuItem();

  Menu seqShowAnnotationsMenu = new Menu(
          MessageManager.getString("label.show_annotations"));

  Menu seqHideAnnotationsMenu = new Menu(
          MessageManager.getString("label.hide_annotations"));

  MenuItem seqAddReferenceAnnotations = new MenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  Menu groupShowAnnotationsMenu = new Menu(
          MessageManager.getString("label.show_annotations"));

  Menu groupHideAnnotationsMenu = new Menu(
          MessageManager.getString("label.hide_annotations"));

  MenuItem groupAddReferenceAnnotations = new MenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  Menu editMenu = new Menu(MessageManager.getString("action.edit"));

  MenuItem copy = new MenuItem(MessageManager.getString("action.copy"));

  MenuItem cut = new MenuItem(MessageManager.getString("action.cut"));

  MenuItem toUpper = new MenuItem(
          MessageManager.getString("label.to_upper_case"));

  MenuItem toLower = new MenuItem(
          MessageManager.getString("label.to_lower_case"));

  MenuItem toggleCase = new MenuItem(
          MessageManager.getString("label.toggle_case"));

  Menu outputmenu = new Menu();

  Menu seqMenu = new Menu();

  MenuItem pdb = new MenuItem();

  MenuItem hideSeqs = new MenuItem();

  MenuItem repGroup = new MenuItem();

  MenuItem sequenceName = new MenuItem(
          MessageManager.getString("label.edit_name_description"));

  MenuItem sequenceFeature = new MenuItem(
          MessageManager.getString("label.create_sequence_feature"));

  MenuItem editSequence = new MenuItem(
          MessageManager.getString("label.edit_sequence"));

  MenuItem sequenceDetails = new MenuItem(
          MessageManager.getString("label.sequence_details"));

  MenuItem selSeqDetails = new MenuItem(
          MessageManager.getString("label.sequence_details"));

  MenuItem makeReferenceSeq = new MenuItem();

  SequenceI seq;

  MenuItem revealAll = new MenuItem();

  MenuItem revealSeq = new MenuItem();

  /**
   * index of sequence to be revealed
   */
  int revealSeq_index = -1;

  Menu menu1 = new Menu();

  public APopupMenu(AlignmentPanel apanel, final SequenceI seq,
          List<String> links)
  {
    // /////////////////////////////////////////////////////////
    // If this is activated from the sequence panel, the user may want to
    // edit or annotate a particular residue. Therefore display the residue menu
    //
    // If from the IDPanel, we must display the sequence menu
    // ////////////////////////////////////////////////////////

    this.ap = apanel;
    this.seq = seq;

    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    for (String ff : FileFormats.getInstance().getWritableFormats(true))
    {
      MenuItem item = new MenuItem(ff);

      item.addActionListener(this);
      outputmenu.add(item);
    }

    buildAnnotationSubmenus();

    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg != null && sg.getSize() > 0)
    {
      if (sg.isNucleotide())
      {
        conservationColour.setEnabled(false);
        clustalColour.setEnabled(false);
        BLOSUM62Colour.setEnabled(false);
        zappoColour.setEnabled(false);
        taylorColour.setEnabled(false);
        hydrophobicityColour.setEnabled(false);
        helixColour.setEnabled(false);
        strandColour.setEnabled(false);
        turnColour.setEnabled(false);
        buriedColour.setEnabled(false);
      }
      else
      {
        purinePyrimidineColour.setEnabled(false);
        nucleotideColour.setEnabled(false);
      }
      editGroupName.setLabel(
              MessageManager.formatMessage("label.name_param", new Object[]
              { sg.getName() }));
      showText.setState(sg.getDisplayText());
      showColourText.setState(sg.getColourText());
      showBoxes.setState(sg.getDisplayBoxes());
      displayNonconserved.setState(sg.getShowNonconserved());
      if (!ap.av.getAlignment().getGroups().contains(sg))
      {
        menu1.setLabel(MessageManager.getString("action.edit_new_group"));
        groupMenu.remove(unGroupMenuItem);
      }
      else
      {
        menu1.setLabel(MessageManager.getString("action.edit_group"));
        groupMenu.remove(createGroupMenuItem);
        if (sg.cs != null)
        {
          abovePIDColour.setState(sg.cs.getThreshold() > 0);
          conservationColour.setState(sg.cs.conservationApplied());
          modifyPID.setEnabled(abovePIDColour.getState());
          modifyConservation.setEnabled(conservationColour.getState());
        }
      }
      setSelectedColour(sg.cs);
    }
    else
    {
      remove(hideSeqs);
      remove(groupMenu);
    }

    if (links != null && links.size() > 0)
    {
      addFeatureLinks(seq, links);
    }

    // TODO: add group link menu entry here
    if (seq != null)
    {
      seqMenu.setLabel(seq.getName());
      if (seq == ap.av.getAlignment().getSeqrep())
      {
        makeReferenceSeq.setLabel(
                MessageManager.getString("action.unmark_as_reference"));// Unmark
                                                                        // representative");
      }
      else
      {
        makeReferenceSeq.setLabel(
                MessageManager.getString("action.set_as_reference")); // );
      }
      repGroup.setLabel(MessageManager
              .formatMessage("label.represent_group_with", new Object[]
              { seq.getName() }));
    }
    else
    {
      remove(seqMenu);
    }

    if (!ap.av.hasHiddenRows())
    {
      remove(revealAll);
      remove(revealSeq);
    }
    else
    {
      final int index = ap.av.getAlignment().findIndex(seq);

      if (ap.av.adjustForHiddenSeqs(index)
              - ap.av.adjustForHiddenSeqs(index - 1) > 1)
      {
        revealSeq_index = index;
      }
      else
      {
        remove(revealSeq);
      }
    }
  }

  /**
   * Select the menu item (if any) matching the current colour scheme. This
   * works by matching the menu item name (not display text) to the canonical
   * name of the colour scheme.
   * 
   * @param cs
   */
  protected void setSelectedColour(ResidueShaderI cs)
  {
    if (cs == null || cs.getColourScheme() == null)
    {
      noColour.setState(true);
    }
    else
    {
      String name = cs.getColourScheme().getSchemeName();
      for (int i = 0; i < colourMenu.getItemCount(); i++)
      {
        MenuItem item = colourMenu.getItem(i);
        if (item instanceof CheckboxMenuItem)
        {
          if (name.equals(item.getName()))
          {
            ((CheckboxMenuItem) item).setState(true);
          }
        }
      }
    }
  }

  /**
   * Adds a 'Link' menu item with a sub-menu item for each hyperlink provided.
   * 
   * @param seq
   * @param links
   */
  void addFeatureLinks(final SequenceI seq, List<String> links)
  {
    Menu linkMenu = new Menu(MessageManager.getString("action.link"));
    Map<String, List<String>> linkset = new LinkedHashMap<>();

    for (String link : links)
    {
      UrlLink urlLink = null;
      try
      {
        urlLink = new UrlLink(link);
      } catch (Exception foo)
      {
        System.err.println("Exception for URLLink '" + link + "': "
                + foo.getMessage());
        continue;
      }

      if (!urlLink.isValid())
      {
        System.err.println(urlLink.getInvalidMessage());
        continue;
      }

      urlLink.createLinksFromSeq(seq, linkset);
    }

    addshowLinks(linkMenu, linkset.values());

    // disable link menu if there are no valid entries
    if (linkMenu.getItemCount() > 0)
    {
      linkMenu.setEnabled(true);
    }
    else
    {
      linkMenu.setEnabled(false);
    }

    if (seq != null)
    {
      seqMenu.add(linkMenu);
    }
    else
    {
      add(linkMenu);
    }

  }

  private void addshowLinks(Menu linkMenu, Collection<List<String>> linkset)
  {
    for (List<String> linkstrset : linkset)
    {
      // split linkstr into label and url
      addshowLink(linkMenu, linkstrset.get(1), linkstrset.get(3));
    }
  }

  /**
   * Build menus for annotation types that may be shown or hidden, and for
   * 'reference annotations' that may be added to the alignment.
   */
  private void buildAnnotationSubmenus()
  {
    /*
     * First for the currently selected sequence (if there is one):
     */
    final List<SequenceI> selectedSequence = (seq == null
            ? Collections.<SequenceI> emptyList()
            : Arrays.asList(seq));
    buildAnnotationTypesMenus(seqShowAnnotationsMenu,
            seqHideAnnotationsMenu, selectedSequence);
    configureReferenceAnnotationsMenu(seqAddReferenceAnnotations,
            selectedSequence);

    /*
     * and repeat for the current selection group (if there is one):
     */
    final List<SequenceI> selectedGroup = (ap.av.getSelectionGroup() == null
            ? Collections.<SequenceI> emptyList()
            : ap.av.getSelectionGroup().getSequences());
    buildAnnotationTypesMenus(groupShowAnnotationsMenu,
            groupHideAnnotationsMenu, selectedGroup);
    configureReferenceAnnotationsMenu(groupAddReferenceAnnotations,
            selectedGroup);
  }

  /**
   * Determine whether or not to enable 'add reference annotations' menu item.
   * It is enable if there are any annotations, on any of the selected
   * sequences, which are not yet on the alignment (visible or not).
   * 
   * @param menu
   * @param forSequences
   */
  private void configureReferenceAnnotationsMenu(MenuItem menuItem,
          List<SequenceI> forSequences)
  {
    menuItem.setEnabled(false);

    /*
     * Temporary store to hold distinct calcId / type pairs for the tooltip.
     * Using TreeMap means calcIds are shown in alphabetical order.
     */
    SortedMap<String, String> tipEntries = new TreeMap<>();
    final Map<SequenceI, List<AlignmentAnnotation>> candidates = new LinkedHashMap<>();
    AlignmentI al = this.ap.av.getAlignment();
    AlignmentUtils.findAddableReferenceAnnotations(forSequences, tipEntries,
            candidates, al);
    if (!candidates.isEmpty())
    {
      StringBuilder tooltip = new StringBuilder(64);
      tooltip.append(MessageManager.getString("label.add_annotations_for"));

      /*
       * Found annotations that could be added. Enable the menu item, and
       * configure its action.
       */
      menuItem.setEnabled(true);

      menuItem.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          addReferenceAnnotations_actionPerformed(candidates);
        }
      });
    }
  }

  /**
   * Add annotations to the sequences and to the alignment.
   * 
   * @param candidates
   *          a map whose keys are sequences on the alignment, and values a list
   *          of annotations to add to each sequence
   */
  protected void addReferenceAnnotations_actionPerformed(
          Map<SequenceI, List<AlignmentAnnotation>> candidates)
  {
    final SequenceGroup selectionGroup = this.ap.av.getSelectionGroup();
    final AlignmentI alignment = this.ap.getAlignment();
    AlignmentUtils.addReferenceAnnotations(candidates, alignment,
            selectionGroup);
    refresh();
  }

  /**
   * add a show URL menu item to the given linkMenu
   * 
   * @param linkMenu
   * @param target
   *          - menu label string
   * @param url
   *          - url to open
   */
  private void addshowLink(Menu linkMenu, final String target,
          final String url)
  {
    addshowLink(linkMenu, target, target, url);
  }

  /**
   * add a show URL menu item to the given linkMenu
   * 
   * @param linkMenu
   * @param target
   *          - URL target window
   * @param label
   *          - menu label string
   * @param url
   *          - url to open
   */
  private void addshowLink(Menu linkMenu, final String target,
          final String label, final String url)
  {
    MenuItem item = new MenuItem(label);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ap.alignFrame.showURL(url, target);
      }
    });
    linkMenu.add(item);
  }

  /**
   * Actions on selecting / unselecting a checkbox menu item
   */
  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    Object source = evt.getSource();
    if (source == noColour)
    {
      noColourmenuItem_actionPerformed();
    }
    else if (source == clustalColour)
    {
      clustalColour_actionPerformed();
    }
    else if (source == BLOSUM62Colour)
    {
      BLOSUM62Colour_actionPerformed();
    }
    else if (evt.getSource() == PIDColour)
    {
      PIDColour_actionPerformed();
    }
    else if (source == zappoColour)
    {
      zappoColour_actionPerformed();
    }
    else if (source == taylorColour)
    {
      taylorColour_actionPerformed();
    }
    else if (source == hydrophobicityColour)
    {
      hydrophobicityColour_actionPerformed();
    }
    else if (source == helixColour)
    {
      helixColour_actionPerformed();
    }
    else if (source == strandColour)
    {
      strandColour_actionPerformed();
    }
    else if (source == turnColour)
    {
      turnColour_actionPerformed();
    }
    else if (source == buriedColour)
    {
      buriedColour_actionPerformed();
    }
    else if (source == nucleotideColour)
    {
      nucleotideMenuItem_actionPerformed();
    }
    else if (source == purinePyrimidineColour)
    {
      purinePyrimidineColour_actionPerformed();
    }
    else if (source == abovePIDColour)
    {
      abovePIDColour_itemStateChanged();
    }
    else if (source == conservationColour)
    {
      conservationMenuItem_itemStateChanged();
    }
    else if (source == showColourText)
    {
      showColourText_itemStateChanged();
    }
    else if (source == showText)
    {
      showText_itemStateChanged();
    }
    else if (source == showBoxes)
    {
      showBoxes_itemStateChanged();
    }
    else if (source == displayNonconserved)
    {
      this.showNonconserved_itemStateChanged();
    }
  }

  /**
   * Actions on clicking a menu item
   */
  @Override
  public void actionPerformed(ActionEvent evt)
  {
    Object source = evt.getSource();
    if (source == userDefinedColour)
    {
      userDefinedColour_actionPerformed();
    }
    else if (source == modifyConservation)
    {
      conservationMenuItem_itemStateChanged();
    }
    else if (source == modifyPID)
    {
      abovePIDColour_itemStateChanged();
    }
    else if (source == unGroupMenuItem)
    {
      unGroupMenuItem_actionPerformed();
    }

    else if (source == createGroupMenuItem)
    {
      createGroupMenuItem_actionPerformed();
    }

    else if (source == sequenceName)
    {
      editName();
    }
    else if (source == makeReferenceSeq)
    {
      makeReferenceSeq_actionPerformed();
    }
    else if (source == sequenceDetails)
    {
      showSequenceDetails();
    }
    else if (source == selSeqDetails)
    {
      showSequenceSelectionDetails();
    }
    else if (source == pdb)
    {
      addPDB();
    }
    else if (source == hideSeqs)
    {
      hideSequences(false);
    }
    else if (source == repGroup)
    {
      hideSequences(true);
    }
    else if (source == revealSeq)
    {
      ap.av.showSequence(revealSeq_index);
    }
    else if (source == revealAll)
    {
      ap.av.showAllHiddenSeqs();
    }

    else if (source == editGroupName)
    {
      EditNameDialog dialog = new EditNameDialog(getGroup().getName(),
              getGroup().getDescription(), "       Group Name",
              "Group Description", ap.alignFrame,
              "Edit Group Name / Description", 500, 100, true);

      if (dialog.accept)
      {
        getGroup().setName(dialog.getName().replace(' ', '_'));
        getGroup().setDescription(dialog.getDescription());
      }

    }
    else if (source == copy)
    {
      ap.alignFrame.copy_actionPerformed();
    }
    else if (source == cut)
    {
      ap.alignFrame.cut_actionPerformed();
    }
    else if (source == editSequence)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();

      if (sg != null)
      {
        if (seq == null)
        {
          seq = sg.getSequenceAt(0);
        }

        EditNameDialog dialog = new EditNameDialog(
                seq.getSequenceAsString(sg.getStartRes(),
                        sg.getEndRes() + 1),
                null, "Edit Sequence ", null,

                ap.alignFrame, "Edit Sequence", 500, 100, true);

        if (dialog.accept)
        {
          EditCommand editCommand = new EditCommand(
                  MessageManager.getString("label.edit_sequences"),
                  Action.REPLACE,
                  dialog.getName().replace(' ', ap.av.getGapCharacter()),
                  sg.getSequencesAsArray(ap.av.getHiddenRepSequences()),
                  sg.getStartRes(), sg.getEndRes() + 1,
                  ap.av.getAlignment());

          ap.alignFrame.addHistoryItem(editCommand);

          ap.av.firePropertyChange("alignment", null,
                  ap.av.getAlignment().getSequences());
        }
      }
    }
    else if (source == toUpper || source == toLower || source == toggleCase)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();
      if (sg != null)
      {
        List<int[]> startEnd = ap.av.getVisibleRegionBoundaries(
                sg.getStartRes(), sg.getEndRes() + 1);

        String description;
        int caseChange;

        if (source == toggleCase)
        {
          description = "Toggle Case";
          caseChange = ChangeCaseCommand.TOGGLE_CASE;
        }
        else if (source == toUpper)
        {
          description = "To Upper Case";
          caseChange = ChangeCaseCommand.TO_UPPER;
        }
        else
        {
          description = "To Lower Case";
          caseChange = ChangeCaseCommand.TO_LOWER;
        }

        ChangeCaseCommand caseCommand = new ChangeCaseCommand(description,
                sg.getSequencesAsArray(ap.av.getHiddenRepSequences()),
                startEnd, caseChange);

        ap.alignFrame.addHistoryItem(caseCommand);

        ap.av.firePropertyChange("alignment", null,
                ap.av.getAlignment().getSequences());

      }
    }
    else if (source == sequenceFeature)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();
      if (sg == null)
      {
        return;
      }

      int gSize = sg.getSize();
      List<SequenceI> seqs = new ArrayList<>();
      List<SequenceFeature> features = new ArrayList<>();

      for (int i = 0; i < gSize; i++)
      {
        int start = sg.getSequenceAt(i).findPosition(sg.getStartRes());
        int end = sg.findEndRes(sg.getSequenceAt(i));
        if (start <= end)
        {
          seqs.add(sg.getSequenceAt(i));
          features.add(
                  new SequenceFeature(null, null, start, end, "Jalview"));
        }
      }

      if (!seqs.isEmpty())
      {
        if (ap.seqPanel.seqCanvas.getFeatureRenderer().amendFeatures(seqs,
                features, true, ap))
        {
          ap.alignFrame.sequenceFeatures.setState(true);
          ap.av.setShowSequenceFeatures(true);
          ap.av.setSearchResults(null); // clear highlighting
          ap.repaint(); // draw new/amended features
        }
      }
    }
    else
    {
      outputText(evt);
    }

  }

  void outputText(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, ap.alignFrame);

    Frame frame = new Frame();
    frame.add(cap);
    JalviewLite.addFrame(frame, MessageManager
            .formatMessage("label.selection_output_command", new Object[]
            { e.getActionCommand() }), 600, 500);
    // JBPNote: getSelectionAsNewSequence behaviour has changed - this method
    // now returns a full copy of sequence data
    // TODO consider using getSequenceSelection instead here

    FileFormatI fileFormat = FileFormats.getInstance()
            .forName(e.getActionCommand());
    cap.setText(new AppletFormatAdapter().formatSequences(fileFormat,
            ap.av.getShowJVSuffix(), ap, true));

  }

  protected void showSequenceSelectionDetails()
  {
    createSequenceDetailsReport(ap.av.getSequenceSelection());
  }

  protected void showSequenceDetails()
  {
    createSequenceDetailsReport(new SequenceI[] { seq });
  }

  public void createSequenceDetailsReport(SequenceI[] sequences)
  {

    CutAndPasteTransfer cap = new CutAndPasteTransfer(false, ap.alignFrame);

    StringBuilder contents = new StringBuilder(128);
    for (SequenceI seq : sequences)
    {
      contents.append(MessageManager
              .formatMessage("label.annotation_for_displayid", new Object[]
              { seq.getDisplayId(true) }));
      new SequenceAnnotationReport(false).createSequenceAnnotationReport(
              contents, seq, true, true, ap.seqPanel.seqCanvas.fr);
      contents.append("</p>");
    }
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame,
            "Sequence Details for " + (sequences.length == 1
                    ? sequences[0].getDisplayId(true)
                    : "Selection"),
            600, 500);
    cap.setText(
            MessageManager.formatMessage("label.html_content", new Object[]
            { contents.toString() }));
  }

  void editName()
  {
    EditNameDialog dialog = new EditNameDialog(seq.getName(),
            seq.getDescription(), "       Sequence Name",
            "Sequence Description", ap.alignFrame,
            "Edit Sequence Name / Description", 500, 100, true);

    if (dialog.accept)
    {
      seq.setName(dialog.getName());
      seq.setDescription(dialog.getDescription());
      ap.paintAlignment(false, false);
    }
  }

  void addPDB()
  {
    Vector<PDBEntry> pdbs = seq.getAllPDBEntries();
    if (pdbs != null && !pdbs.isEmpty())
    {
      PDBEntry entry = pdbs.firstElement();

      if (ap.av.applet.jmolAvailable)
      {
        new AppletJmol(entry, new SequenceI[] { seq }, null, ap,
                DataSourceType.URL);
      }
      else
      {
        new mc_view.AppletPDBViewer(entry, new SequenceI[] { seq }, null,
                ap, DataSourceType.URL);
      }

    }
    else
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(true,
              ap.alignFrame);
      cap.setText(MessageManager.getString("label.paste_pdb_file"));
      cap.setPDBImport(seq);
      Frame frame = new Frame();
      frame.add(cap);
      JalviewLite.addFrame(frame, MessageManager.formatMessage(
              "label.paste_pdb_file_for_sequence", new Object[]
              { seq.getName() }), 400, 300);
    }
  }

  private void jbInit() throws Exception
  {
    groupMenu.setLabel(MessageManager.getString("label.selection"));
    sequenceFeature.addActionListener(this);

    editGroupName.addActionListener(this);
    unGroupMenuItem
            .setLabel(MessageManager.getString("action.remove_group"));
    unGroupMenuItem.addActionListener(this);

    createGroupMenuItem
            .setLabel(MessageManager.getString("action.create_group"));
    createGroupMenuItem.addActionListener(this);

    modifyPID.setEnabled(abovePIDColour.getState());
    modifyConservation.setEnabled(conservationColour.getState());
    colourMenu.setLabel(MessageManager.getString("label.group_colour"));
    showBoxes.setLabel(MessageManager.getString("action.boxes"));
    showBoxes.setState(true);
    showBoxes.addItemListener(this);
    sequenceName.addActionListener(this);
    sequenceDetails.addActionListener(this);
    selSeqDetails.addActionListener(this);
    displayNonconserved
            .setLabel(MessageManager.getString("label.show_non_conserved"));
    displayNonconserved.setState(false);
    displayNonconserved.addItemListener(this);
    showText.setLabel(MessageManager.getString("action.text"));
    showText.addItemListener(this);
    showColourText.setLabel(MessageManager.getString("label.colour_text"));
    showColourText.addItemListener(this);
    outputmenu.setLabel(MessageManager.getString("label.out_to_textbox"));
    seqMenu.setLabel(MessageManager.getString("label.sequence"));
    pdb.setLabel(MessageManager.getString("label.view_pdb_structure"));
    hideSeqs.setLabel(MessageManager.getString("action.hide_sequences"));
    repGroup.setLabel(MessageManager
            .formatMessage("label.represent_group_with", new Object[]
            { "" }));
    revealAll.setLabel(MessageManager.getString("action.reveal_all"));
    revealSeq.setLabel(MessageManager.getString("action.reveal_sequences"));
    menu1.setLabel(MessageManager.getString("label.group:"));
    add(groupMenu);
    this.add(seqMenu);
    this.add(hideSeqs);
    this.add(revealSeq);
    this.add(revealAll);
    // groupMenu.add(selSeqDetails);
    groupMenu.add(groupShowAnnotationsMenu);
    groupMenu.add(groupHideAnnotationsMenu);
    groupMenu.add(groupAddReferenceAnnotations);
    groupMenu.add(editMenu);
    groupMenu.add(outputmenu);
    groupMenu.add(sequenceFeature);
    groupMenu.add(createGroupMenuItem);
    groupMenu.add(unGroupMenuItem);
    groupMenu.add(menu1);

    colourMenu.add(noColour);
    colourMenu.add(clustalColour);
    colourMenu.add(BLOSUM62Colour);
    colourMenu.add(PIDColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydrophobicityColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(nucleotideColour);
    colourMenu.add(purinePyrimidineColour);
    colourMenu.add(userDefinedColour);
    colourMenu.addSeparator();
    colourMenu.add(conservationColour);
    colourMenu.add(modifyConservation);
    colourMenu.add(abovePIDColour);
    colourMenu.add(modifyPID);

    noColour.setLabel(MessageManager.getString("label.none"));
    noColour.addItemListener(this);

    /*
     * setName allows setSelectedColour to do its thing
     */
    clustalColour.setLabel(
            MessageManager.getString("label.colourScheme_clustal"));
    clustalColour.setName(JalviewColourScheme.Clustal.toString());
    clustalColour.addItemListener(this);
    BLOSUM62Colour.setLabel(
            MessageManager.getString("label.colourScheme_blosum62"));
    BLOSUM62Colour.setName(JalviewColourScheme.Blosum62.toString());
    BLOSUM62Colour.addItemListener(this);
    PIDColour.setLabel(
            MessageManager.getString("label.colourScheme_%identity"));
    PIDColour.setName(JalviewColourScheme.PID.toString());
    PIDColour.addItemListener(this);
    zappoColour
            .setLabel(MessageManager.getString("label.colourScheme_zappo"));
    zappoColour.setName(JalviewColourScheme.Zappo.toString());
    zappoColour.addItemListener(this);
    taylorColour.setLabel(
            MessageManager.getString("label.colourScheme_taylor"));
    taylorColour.setName(JalviewColourScheme.Taylor.toString());
    taylorColour.addItemListener(this);
    hydrophobicityColour.setLabel(
            MessageManager.getString("label.colourScheme_hydrophobic"));
    hydrophobicityColour
            .setName(JalviewColourScheme.Hydrophobic.toString());
    hydrophobicityColour.addItemListener(this);
    helixColour.setLabel(
            MessageManager.getString("label.colourScheme_helixpropensity"));
    helixColour.setName(JalviewColourScheme.Helix.toString());
    helixColour.addItemListener(this);
    strandColour.setLabel(MessageManager
            .getString("label.colourScheme_strandpropensity"));
    strandColour.setName(JalviewColourScheme.Strand.toString());
    strandColour.addItemListener(this);
    turnColour.setLabel(
            MessageManager.getString("label.colourScheme_turnpropensity"));
    turnColour.setName(JalviewColourScheme.Turn.toString());
    turnColour.addItemListener(this);
    buriedColour.setLabel(
            MessageManager.getString("label.colourScheme_buriedindex"));
    buriedColour.setName(JalviewColourScheme.Buried.toString());
    buriedColour.addItemListener(this);
    nucleotideColour.setLabel(
            MessageManager.getString("label.colourScheme_nucleotide"));
    nucleotideColour.setName(JalviewColourScheme.Nucleotide.toString());
    nucleotideColour.addItemListener(this);
    purinePyrimidineColour.setLabel(MessageManager
            .getString("label.colourScheme_purine/pyrimidine"));
    purinePyrimidineColour
            .setName(JalviewColourScheme.PurinePyrimidine.toString());
    purinePyrimidineColour.addItemListener(this);

    userDefinedColour
            .setLabel(MessageManager.getString("action.user_defined"));
    userDefinedColour.addActionListener(this);

    abovePIDColour.setLabel(
            MessageManager.getString("label.above_identity_threshold"));
    abovePIDColour.addItemListener(this);
    modifyPID.setLabel(
            MessageManager.getString("label.modify_identity_threshold"));
    modifyPID.addActionListener(this);
    conservationColour
            .setLabel(MessageManager.getString("action.by_conservation"));
    conservationColour.addItemListener(this);
    modifyConservation.setLabel(MessageManager
            .getString("label.modify_conservation_threshold"));
    modifyConservation.addActionListener(this);

    PIDColour.addActionListener(this);
    BLOSUM62Colour.addActionListener(this);

    editMenu.add(copy);
    copy.addActionListener(this);
    editMenu.add(cut);
    cut.addActionListener(this);

    editMenu.add(editSequence);
    editSequence.addActionListener(this);

    editMenu.add(toUpper);
    toUpper.addActionListener(this);
    editMenu.add(toLower);
    toLower.addActionListener(this);
    editMenu.add(toggleCase);
    seqMenu.add(seqShowAnnotationsMenu);
    seqMenu.add(seqHideAnnotationsMenu);
    seqMenu.add(seqAddReferenceAnnotations);
    seqMenu.add(sequenceName);
    seqMenu.add(makeReferenceSeq);
    // seqMenu.add(sequenceDetails);

    if (!ap.av.applet.useXtrnalSviewer)
    {
      seqMenu.add(pdb);
    }
    seqMenu.add(repGroup);
    menu1.add(editGroupName);
    menu1.add(colourMenu);
    menu1.add(showBoxes);
    menu1.add(showText);
    menu1.add(showColourText);
    menu1.add(displayNonconserved);
    toggleCase.addActionListener(this);
    pdb.addActionListener(this);
    hideSeqs.addActionListener(this);
    repGroup.addActionListener(this);
    revealAll.addActionListener(this);
    revealSeq.addActionListener(this);
    makeReferenceSeq.addActionListener(this);
  }

  void refresh()
  {
    ap.paintAlignment(true, true);
  }

  protected void clustalColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new ResidueShader(
            new ClustalxColourScheme(sg, ap.av.getHiddenRepSequences()));
    refresh();
  }

  protected void zappoColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new ZappoColourScheme());
    refresh();
  }

  protected void taylorColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new TaylorColourScheme());
    refresh();
  }

  protected void hydrophobicityColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new HydrophobicColourScheme());
    refresh();
  }

  protected void helixColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new HelixColourScheme());
    refresh();
  }

  protected void strandColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new StrandColourScheme());
    refresh();
  }

  protected void turnColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new TurnColourScheme());
    refresh();
  }

  protected void buriedColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new BuriedColourScheme());
    refresh();
  }

  public void nucleotideMenuItem_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new NucleotideColourScheme());
    refresh();
  }

  public void purinePyrimidineColour_actionPerformed()
  {
    getGroup().cs = new ResidueShader(new PurinePyrimidineColourScheme());
    refresh();
  }

  protected void abovePIDColour_itemStateChanged()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (abovePIDColour.getState())
    {
      sg.cs.setConsensus(AAFrequency.calculate(
              sg.getSequences(ap.av.getHiddenRepSequences()), 0,
              ap.av.getAlignment().getWidth()));
      int threshold = SliderPanel.setPIDSliderSource(ap, sg.cs,
              getGroup().getName());

      sg.cs.setThreshold(threshold, ap.av.isIgnoreGapsConsensus());

      SliderPanel.showPIDSlider();

    }
    else
    // remove PIDColouring
    {
      SliderPanel.hidePIDSlider();
      sg.cs.setThreshold(0, ap.av.isIgnoreGapsConsensus());
    }
    modifyPID.setEnabled(abovePIDColour.getState());
    refresh();
  }

  protected void userDefinedColour_actionPerformed()
  {
    new UserDefinedColours(ap, getGroup());
  }

  protected void PIDColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new ResidueShader(new PIDColourScheme());
    sg.cs.setConsensus(AAFrequency.calculate(
            sg.getSequences(ap.av.getHiddenRepSequences()), 0,
            ap.av.getAlignment().getWidth()));
    refresh();
  }

  protected void BLOSUM62Colour_actionPerformed()
  {
    SequenceGroup sg = getGroup();

    sg.cs = new ResidueShader(new Blosum62ColourScheme());

    sg.cs.setConsensus(AAFrequency.calculate(
            sg.getSequences(ap.av.getHiddenRepSequences()), 0,
            ap.av.getAlignment().getWidth()));

    refresh();
  }

  protected void noColourmenuItem_actionPerformed()
  {
    getGroup().cs = null;
    refresh();
  }

  protected void conservationMenuItem_itemStateChanged()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (conservationColour.getState())
    {
      Conservation conservation = Conservation.calculateConservation(
              "Group", sg.getSequences(ap.av.getHiddenRepSequences()), 0,
              ap.av.getAlignment().getWidth(), false,
              ap.av.getConsPercGaps(), false);
      sg.getGroupColourScheme().setConservation(conservation);
      SliderPanel.setConservationSlider(ap, sg.cs, sg.getName());
      SliderPanel.showConservationSlider();
    }
    else
    // remove ConservationColouring
    {
      SliderPanel.hideConservationSlider();
      sg.cs.setConservation(null);
    }
    modifyConservation.setEnabled(conservationColour.getState());
    refresh();
  }

  SequenceGroup getGroup()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();

    // this method won't add a new group if it already exists
    if (sg != null)
    {
      ap.av.getAlignment().addGroup(sg);
    }

    return sg;
  }

  void unGroupMenuItem_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    ap.av.getAlignment().deleteGroup(sg);
    ap.av.setSelectionGroup(null);
    ap.paintAlignment(true, true);
  }

  void createGroupMenuItem_actionPerformed()
  {
    getGroup(); // implicitly create group
    refresh();
  }

  public void showColourText_itemStateChanged()
  {
    getGroup().setColourText(showColourText.getState());
    refresh();
  }

  public void showText_itemStateChanged()
  {
    getGroup().setDisplayText(showText.getState());
    refresh();
  }

  public void makeReferenceSeq_actionPerformed()
  {
    if (!ap.av.getAlignment().hasSeqrep())
    {
      // initialise the display flags so the user sees something happen
      ap.av.setDisplayReferenceSeq(true);
      ap.av.setColourByReferenceSeq(true);
      ap.av.getAlignment().setSeqrep(seq);
    }
    else
    {
      if (ap.av.getAlignment().getSeqrep() == seq)
      {
        ap.av.getAlignment().setSeqrep(null);
      }
      else
      {
        ap.av.getAlignment().setSeqrep(seq);
      }
    }
    refresh();
  }

  public void showNonconserved_itemStateChanged()
  {
    getGroup().setShowNonconserved(this.displayNonconserved.getState());
    refresh();
  }

  public void showBoxes_itemStateChanged()
  {
    getGroup().setDisplayBoxes(showBoxes.getState());
    refresh();
  }

  void hideSequences(boolean representGroup)
  {
    ap.av.hideSequences(seq, representGroup);
  }

  /**
   * Add annotation types to 'Show annotations' and/or 'Hide annotations' menus.
   * "All" is added first, followed by a separator. Then add any annotation
   * types associated with the current selection. Separate menus are built for
   * the selected sequence group (if any), and the selected sequence.
   * <p>
   * Some annotation rows are always rendered together - these can be identified
   * by a common graphGroup property > -1. Only one of each group will be marked
   * as visible (to avoid duplication of the display). For such groups we add a
   * composite type name, e.g.
   * <p>
   * IUPredWS (Long), IUPredWS (Short)
   * 
   * @param seq
   */
  protected void buildAnnotationTypesMenus(Menu showMenu, Menu hideMenu,
          List<SequenceI> forSequences)
  {
    showMenu.removeAll();
    hideMenu.removeAll();

    final List<String> all = Arrays
            .asList(new String[]
            { MessageManager.getString("label.all") });
    addAnnotationTypeToShowHide(showMenu, forSequences, "", all, true,
            true);
    addAnnotationTypeToShowHide(hideMenu, forSequences, "", all, true,
            false);
    showMenu.addSeparator();
    hideMenu.addSeparator();

    final AlignmentAnnotation[] annotations = ap.getAlignment()
            .getAlignmentAnnotation();

    /*
     * Find shown/hidden annotations types, distinguished by source (calcId),
     * and grouped by graphGroup. Using LinkedHashMap means we will retrieve in
     * the insertion order, which is the order of the annotations on the
     * alignment.
     */
    Map<String, List<List<String>>> shownTypes = new LinkedHashMap<>();
    Map<String, List<List<String>>> hiddenTypes = new LinkedHashMap<>();
    AlignmentAnnotationUtils.getShownHiddenTypes(shownTypes, hiddenTypes,
            AlignmentAnnotationUtils.asList(annotations), forSequences);

    for (String calcId : hiddenTypes.keySet())
    {
      for (List<String> type : hiddenTypes.get(calcId))
      {
        addAnnotationTypeToShowHide(showMenu, forSequences, calcId, type,
                false, true);
      }
    }
    // grey out 'show annotations' if none are hidden
    showMenu.setEnabled(!hiddenTypes.isEmpty());

    for (String calcId : shownTypes.keySet())
    {
      for (List<String> type : shownTypes.get(calcId))
      {
        addAnnotationTypeToShowHide(hideMenu, forSequences, calcId, type,
                false, false);
      }
    }
    // grey out 'hide annotations' if none are shown
    hideMenu.setEnabled(!shownTypes.isEmpty());
  }

  /**
   * Add one annotation type to the 'Show Annotations' or 'Hide Annotations'
   * menus.
   * 
   * @param showOrHideMenu
   *          the menu to add to
   * @param forSequences
   *          the sequences whose annotations may be shown or hidden
   * @param calcId
   * @param types
   *          the label to add
   * @param allTypes
   *          if true this is a special label meaning 'All'
   * @param actionIsShow
   *          if true, the select menu item action is to show the annotation
   *          type, else hide
   */
  protected void addAnnotationTypeToShowHide(Menu showOrHideMenu,
          final List<SequenceI> forSequences, String calcId,
          final List<String> types, final boolean allTypes,
          final boolean actionIsShow)
  {
    String label = types.toString(); // [a, b, c]
    label = label.substring(1, label.length() - 1);
    final MenuItem item = new MenuItem(label);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        AlignmentUtils.showOrHideSequenceAnnotations(ap.getAlignment(),
                types, forSequences, allTypes, actionIsShow);
        refresh();
      }
    });
    showOrHideMenu.add(item);
  }

}
