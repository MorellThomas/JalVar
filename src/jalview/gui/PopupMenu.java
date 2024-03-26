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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;

import jalview.analysis.AAFrequency;
import jalview.analysis.AlignmentAnnotationUtils;
import jalview.analysis.AlignmentUtils;
import jalview.analysis.Conservation;
import jalview.api.AlignViewportI;
import jalview.bin.Console;
import jalview.commands.ChangeCaseCommand;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.ColourMenuHelper.ColourChangeListener;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FormatAdapter;
import jalview.io.SequenceAnnotationReport;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemes;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.ResidueColourScheme;
import jalview.util.Comparison;
import jalview.util.GroupUrlLink;
import jalview.util.GroupUrlLink.UrlStringTooLongException;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.StringUtils;
import jalview.util.UrlLink;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;

/**
 * The popup menu that is displayed on right-click on a sequence id, or in the
 * sequence alignment.
 */
public class PopupMenu extends JPopupMenu implements ColourChangeListener
{
  /*
   * maximum length of feature description to include in popup menu item text
   */
  private static final int FEATURE_DESC_MAX = 40;

  /*
   * true for ID Panel menu, false for alignment panel menu
   */
  private final boolean forIdPanel;

  private final AlignmentPanel ap;

  /*
   * the sequence under the cursor when clicked
   * (additional sequences may be selected)
   */
  private final SequenceI sequence;

  JMenu groupMenu = new JMenu();

  JMenuItem groupName = new JMenuItem();

  protected JCheckBoxMenuItem abovePIDColour = new JCheckBoxMenuItem();

  protected JMenuItem modifyPID = new JMenuItem();

  protected JCheckBoxMenuItem conservationMenuItem = new JCheckBoxMenuItem();

  protected JRadioButtonMenuItem annotationColour;

  protected JMenuItem modifyConservation = new JMenuItem();

  JMenu sequenceMenu = new JMenu();

  JMenuItem makeReferenceSeq = new JMenuItem();

  JMenuItem createGroupMenuItem = new JMenuItem();

  JMenuItem unGroupMenuItem = new JMenuItem();

  JMenu colourMenu = new JMenu();

  JCheckBoxMenuItem showBoxes = new JCheckBoxMenuItem();

  JCheckBoxMenuItem showText = new JCheckBoxMenuItem();

  JCheckBoxMenuItem showColourText = new JCheckBoxMenuItem();

  JCheckBoxMenuItem displayNonconserved = new JCheckBoxMenuItem();

  JMenu editMenu = new JMenu();

  JMenuItem upperCase = new JMenuItem();

  JMenuItem lowerCase = new JMenuItem();

  JMenuItem toggle = new JMenuItem();

  JMenu outputMenu = new JMenu();

  JMenu seqShowAnnotationsMenu = new JMenu();

  JMenu seqHideAnnotationsMenu = new JMenu();

  JMenuItem seqAddReferenceAnnotations = new JMenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  JMenu groupShowAnnotationsMenu = new JMenu();

  JMenu groupHideAnnotationsMenu = new JMenu();

  JMenuItem groupAddReferenceAnnotations = new JMenuItem(
          MessageManager.getString("label.add_reference_annotations"));

  JMenuItem textColour = new JMenuItem();

  JMenu editGroupMenu = new JMenu();

  JMenuItem chooseStructure = new JMenuItem();

  JMenu rnaStructureMenu = new JMenu();

  /**
   * Constructs a menu with sub-menu items for any hyperlinks for the sequence
   * and/or features provided. Hyperlinks may include a lookup by sequence id,
   * or database cross-references, depending on which links are enabled in user
   * preferences.
   * 
   * @param seq
   * @param features
   * @return
   */
  protected static JMenu buildLinkMenu(final SequenceI seq,
          List<SequenceFeature> features)
  {
    JMenu linkMenu = new JMenu(MessageManager.getString("action.link"));

    List<String> nlinks = null;
    if (seq != null)
    {
      nlinks = Preferences.sequenceUrlLinks.getLinksForMenu();
      UrlLink.sort(nlinks);
    }
    else
    {
      nlinks = new ArrayList<>();
    }

    if (features != null)
    {
      for (SequenceFeature sf : features)
      {
        if (sf.links != null)
        {
          for (String link : sf.links)
          {
            nlinks.add(link);
          }
        }
      }
    }

    /*
     * instantiate the hyperlinklink templates from sequence data;
     * note the order of the templates is preserved in the map
     */
    Map<String, List<String>> linkset = new LinkedHashMap<>();
    for (String link : nlinks)
    {
      UrlLink urlLink = null;
      try
      {
        urlLink = new UrlLink(link);
      } catch (Exception foo)
      {
        Console.error("Exception for URLLink '" + link + "'", foo);
        continue;
      }

      if (!urlLink.isValid())
      {
        Console.error(urlLink.getInvalidMessage());
        continue;
      }

      urlLink.createLinksFromSeq(seq, linkset);
    }

    /*
     * construct menu items for the hyperlinks (still preserving
     * the order of the sorted templates)
     */
    addUrlLinks(linkMenu, linkset.values());

    return linkMenu;
  }

  /**
   * A helper method that builds menu items from the given links, with action
   * handlers to open the link URL, and adds them to the linkMenu. Each provided
   * link should be a list whose second item is the menu text, and whose fourth
   * item is the URL to open when the menu item is selected.
   * 
   * @param linkMenu
   * @param linkset
   */
  static private void addUrlLinks(JMenu linkMenu,
          Collection<List<String>> linkset)
  {
    for (List<String> linkstrset : linkset)
    {
      final String url = linkstrset.get(3);
      JMenuItem item = new JMenuItem(linkstrset.get(1));
      item.setToolTipText(MessageManager
              .formatMessage("label.open_url_param", new Object[]
              { url }));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          new Thread(new Runnable()
          {
            @Override
            public void run()
            {
              showLink(url);
            }
          }).start();
        }
      });
      linkMenu.add(item);
    }
  }

  /**
   * Opens the provided url in the default web browser, or shows an error
   * message if this fails
   * 
   * @param url
   */
  static void showLink(String url)
  {
    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    } catch (Exception ex)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.getString("label.web_browser_not_found_unix"),
              MessageManager.getString("label.web_browser_not_found"),
              JvOptionPane.WARNING_MESSAGE);

      ex.printStackTrace();
    }
  }

  /**
   * add a late bound groupURL item to the given linkMenu
   * 
   * @param linkMenu
   * @param label
   *          - menu label string
   * @param urlgenerator
   *          GroupURLLink used to generate URL
   * @param urlstub
   *          Object array returned from the makeUrlStubs function.
   */
  static void addshowLink(JMenu linkMenu, String label,
          final GroupUrlLink urlgenerator, final Object[] urlstub)
  {
    JMenuItem item = new JMenuItem(label);
    item.setToolTipText(MessageManager
            .formatMessage("label.open_url_seqs_param", new Object[]
            { urlgenerator.getUrl_prefix(),
                urlgenerator.getNumberInvolved(urlstub) }));
    // TODO: put in info about what is being sent.
    item.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        new Thread(new Runnable()
        {

          @Override
          public void run()
          {
            try
            {
              showLink(urlgenerator.constructFrom(urlstub));
            } catch (UrlStringTooLongException e2)
            {
            }
          }

        }).start();
      }
    });

    linkMenu.add(item);
  }

  /**
   * Constructor for a PopupMenu for a click in the alignment panel (on a
   * residue)
   * 
   * @param ap
   *          the panel in which the mouse is clicked
   * @param seq
   *          the sequence under the mouse
   * @throws NullPointerException
   *           if seq is null
   */
  public PopupMenu(final AlignmentPanel ap, SequenceI seq, int column)
  {
    this(false, ap, seq, column, null);
  }

  /**
   * Constructor for a PopupMenu for a click in the sequence id panel
   * 
   * @param alignPanel
   *          the panel in which the mouse is clicked
   * @param seq
   *          the sequence under the mouse click
   * @param groupLinks
   *          templates for sequence external links
   * @throws NullPointerException
   *           if seq is null
   */
  public PopupMenu(final AlignmentPanel alignPanel, final SequenceI seq,
          List<String> groupLinks)
  {
    this(true, alignPanel, seq, -1, groupLinks);
  }

  /**
   * Private constructor that constructs a popup menu for either sequence ID
   * Panel, or alignment context
   * 
   * @param fromIdPanel
   * @param alignPanel
   * @param seq
   * @param column
   *          aligned column position (0...)
   * @param groupLinks
   */
  private PopupMenu(boolean fromIdPanel, final AlignmentPanel alignPanel,
          final SequenceI seq, final int column, List<String> groupLinks)
  {
    Objects.requireNonNull(seq);
    this.forIdPanel = fromIdPanel;
    this.ap = alignPanel;
    sequence = seq;

    for (String ff : FileFormats.getInstance().getWritableFormats(true))
    {
      JMenuItem item = new JMenuItem(ff);

      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          outputText_actionPerformed(e);
        }
      });

      outputMenu.add(item);
    }

    /*
     * Build menus for annotation types that may be shown or hidden, and for
     * 'reference annotations' that may be added to the alignment. First for the
     * currently selected sequence (if there is one):
     */
    final List<SequenceI> selectedSequence = (forIdPanel && seq != null
            ? Arrays.asList(seq)
            : Collections.<SequenceI> emptyList());
    buildAnnotationTypesMenus(seqShowAnnotationsMenu,
            seqHideAnnotationsMenu, selectedSequence);
    configureReferenceAnnotationsMenu(seqAddReferenceAnnotations,
            selectedSequence);

    /*
     * And repeat for the current selection group (if there is one):
     */
    final List<SequenceI> selectedGroup = (alignPanel.av
            .getSelectionGroup() == null
                    ? Collections.<SequenceI> emptyList()
                    : alignPanel.av.getSelectionGroup().getSequences());
    buildAnnotationTypesMenus(groupShowAnnotationsMenu,
            groupHideAnnotationsMenu, selectedGroup);
    configureReferenceAnnotationsMenu(groupAddReferenceAnnotations,
            selectedGroup);

    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    if (forIdPanel)
    {
      JMenuItem menuItem;
      sequenceMenu.setText(sequence.getName());
      if (seq == alignPanel.av.getAlignment().getSeqrep())
      {
        makeReferenceSeq.setText(
                MessageManager.getString("action.unmark_as_reference"));
      }
      else
      {
        makeReferenceSeq.setText(
                MessageManager.getString("action.set_as_reference"));
      }

      if (!alignPanel.av.getAlignment().isNucleotide())
      {
        remove(rnaStructureMenu);
      }
      else
      {
        int origCount = rnaStructureMenu.getItemCount();
        /*
         * add menu items to 2D-render any alignment or sequence secondary
         * structure annotation
         */
        AlignmentAnnotation[] aas = alignPanel.av.getAlignment()
                .getAlignmentAnnotation();
        if (aas != null)
        {
          for (final AlignmentAnnotation aa : aas)
          {
            if (aa.isValidStruc() && aa.sequenceRef == null)
            {
              /*
               * valid alignment RNA secondary structure annotation
               */
              menuItem = new JMenuItem();
              menuItem.setText(MessageManager.formatMessage(
                      "label.2d_rna_structure_line", new Object[]
                      { aa.label }));
              menuItem.addActionListener(new ActionListener()
              {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                  new AppVarna(seq, aa, alignPanel);
                }
              });
              rnaStructureMenu.add(menuItem);
            }
          }
        }

        if (seq.getAnnotation() != null)
        {
          AlignmentAnnotation seqAnns[] = seq.getAnnotation();
          for (final AlignmentAnnotation aa : seqAnns)
          {
            if (aa.isValidStruc())
            {
              /*
               * valid sequence RNA secondary structure annotation
               */
              // TODO: make rnastrucF a bit more nice
              menuItem = new JMenuItem();
              menuItem.setText(MessageManager.formatMessage(
                      "label.2d_rna_sequence_name", new Object[]
                      { seq.getName() }));
              menuItem.addActionListener(new ActionListener()
              {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                  // TODO: VARNA does'nt print gaps in the sequence
                  new AppVarna(seq, aa, alignPanel);
                }
              });
              rnaStructureMenu.add(menuItem);
            }
          }
        }
        if (rnaStructureMenu.getItemCount() == origCount)
        {
          remove(rnaStructureMenu);
        }
      }

      menuItem = new JMenuItem(
              MessageManager.getString("action.hide_sequences"));
      menuItem.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          hideSequences(false);
        }
      });
      add(menuItem);

      if (alignPanel.av.getSelectionGroup() != null
              && alignPanel.av.getSelectionGroup().getSize() > 1)
      {
        menuItem = new JMenuItem(MessageManager
                .formatMessage("label.represent_group_with", new Object[]
                { seq.getName() }));
        menuItem.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            hideSequences(true);
          }
        });
        sequenceMenu.add(menuItem);
      }

      if (alignPanel.av.hasHiddenRows())
      {
        final int index = alignPanel.av.getAlignment().findIndex(seq);

        if (alignPanel.av.adjustForHiddenSeqs(index)
                - alignPanel.av.adjustForHiddenSeqs(index - 1) > 1)
        {
          menuItem = new JMenuItem(
                  MessageManager.getString("action.reveal_sequences"));
          menuItem.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              alignPanel.av.showSequence(index);
              if (alignPanel.overviewPanel != null)
              {
                alignPanel.overviewPanel.updateOverviewImage();
              }
            }
          });
          add(menuItem);
        }
      }
    }

    /*
     * offer 'Reveal All'
     * - in the IdPanel (seq not null) if any sequence is hidden
     * - in the IdPanel or SeqPanel if all sequences are hidden (seq is null)
     */
    if (alignPanel.av.hasHiddenRows())
    {
      boolean addOption = seq != null;
      if (!addOption && alignPanel.av.getAlignment().getHeight() == 0)
      {
        addOption = true;
      }
      if (addOption)
      {
        JMenuItem menuItem = new JMenuItem(
                MessageManager.getString("action.reveal_all"));
        menuItem.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            alignPanel.av.showAllHiddenSeqs();
            if (alignPanel.overviewPanel != null)
            {
              alignPanel.overviewPanel.updateOverviewImage();
            }
          }
        });
        add(menuItem);
      }
    }

    SequenceGroup sg = alignPanel.av.getSelectionGroup();
    boolean isDefinedGroup = (sg != null)
            ? alignPanel.av.getAlignment().getGroups().contains(sg)
            : false;

    if (sg != null && sg.getSize() > 0)
    {
      groupName.setText(MessageManager
              .getString("label.edit_name_and_description_current_group"));

      ColourMenuHelper.setColourSelected(colourMenu, sg.getColourScheme());

      conservationMenuItem.setEnabled(!sg.isNucleotide());

      if (sg.cs != null)
      {
        if (sg.cs.conservationApplied())
        {
          conservationMenuItem.setSelected(true);
        }
        if (sg.cs.getThreshold() > 0)
        {
          abovePIDColour.setSelected(true);
        }
      }
      modifyConservation.setEnabled(conservationMenuItem.isSelected());
      modifyPID.setEnabled(abovePIDColour.isSelected());
      displayNonconserved.setSelected(sg.getShowNonconserved());
      showText.setSelected(sg.getDisplayText());
      showColourText.setSelected(sg.getColourText());
      showBoxes.setSelected(sg.getDisplayBoxes());
      // add any groupURLs to the groupURL submenu and make it visible
      if (groupLinks != null && groupLinks.size() > 0)
      {
        buildGroupURLMenu(sg, groupLinks);
      }
      // Add a 'show all structures' for the current selection
      Hashtable<String, PDBEntry> pdbe = new Hashtable<>(),
              reppdb = new Hashtable<>();

      SequenceI sqass = null;
      for (SequenceI sq : alignPanel.av.getSequenceSelection())
      {
        Vector<PDBEntry> pes = sq.getDatasetSequence().getAllPDBEntries();
        if (pes != null && pes.size() > 0)
        {
          reppdb.put(pes.get(0).getId(), pes.get(0));
          for (PDBEntry pe : pes)
          {
            pdbe.put(pe.getId(), pe);
            if (sqass == null)
            {
              sqass = sq;
            }
          }
        }
      }
      if (pdbe.size() > 0)
      {
        final PDBEntry[] pe = pdbe.values()
                .toArray(new PDBEntry[pdbe.size()]),
                pr = reppdb.values().toArray(new PDBEntry[reppdb.size()]);
        final JMenuItem gpdbview, rpdbview;
      }
    }
    else
    {
      groupMenu.setVisible(false);
      editMenu.setVisible(false);
    }

    if (!isDefinedGroup)
    {
      createGroupMenuItem.setVisible(true);
      unGroupMenuItem.setVisible(false);
      editGroupMenu
              .setText(MessageManager.getString("action.edit_new_group"));
    }
    else
    {
      createGroupMenuItem.setVisible(false);
      unGroupMenuItem.setVisible(true);
      editGroupMenu.setText(MessageManager.getString("action.edit_group"));
    }

    if (!forIdPanel)
    {
      sequenceMenu.setVisible(false);
      chooseStructure.setVisible(false);
      rnaStructureMenu.setVisible(false);
    }

    addLinksAndFeatures(seq, column);
  }

  /**
   * Adds
   * <ul>
   * <li>configured sequence database links (ID panel popup menu)</li>
   * <li>non-positional feature links (ID panel popup menu)</li>
   * <li>positional feature links (alignment panel popup menu)</li>
   * <li>feature details links (alignment panel popup menu)</li>
   * </ul>
   * If this panel is also showed complementary (CDS/protein) features, then
   * links to their feature details are also added.
   * 
   * @param seq
   * @param column
   */
  void addLinksAndFeatures(final SequenceI seq, final int column)
  {
    List<SequenceFeature> features = null;
    if (forIdPanel)
    {
      features = sequence.getFeatures().getNonPositionalFeatures();
    }
    else
    {
      features = ap.getFeatureRenderer().findFeaturesAtColumn(sequence,
              column + 1);
    }

    addLinks(seq, features);

    if (!forIdPanel)
    {
      addFeatureDetails(features, seq, column);
    }
  }

  /**
   * Add a menu item to show feature details for each sequence feature. Any
   * linked 'virtual' features (CDS/protein) are also optionally found and
   * included.
   * 
   * @param features
   * @param seq
   * @param column
   */
  protected void addFeatureDetails(List<SequenceFeature> features,
          final SequenceI seq, final int column)
  {
    /*
     * add features in CDS/protein complement at the corresponding
     * position if configured to do so
     */
    MappedFeatures mf = null;
    if (ap.av.isShowComplementFeatures())
    {
      if (!Comparison.isGap(sequence.getCharAt(column)))
      {
        AlignViewportI complement = ap.getAlignViewport()
                .getCodingComplement();
        AlignFrame af = Desktop.getAlignFrameFor(complement);
        FeatureRendererModel fr2 = af.getFeatureRenderer();
        int seqPos = sequence.findPosition(column);
        mf = fr2.findComplementFeaturesAtResidue(sequence, seqPos);
      }
    }

    if (features.isEmpty() && mf == null)
    {
      /*
       * no features to show at this position
       */
      return;
    }

    JMenu details = new JMenu(
            MessageManager.getString("label.feature_details"));
    add(details);

    String name = seq.getName();
    for (final SequenceFeature sf : features)
    {
      addFeatureDetailsMenuItem(details, name, sf, null);
    }

    if (mf != null)
    {
      for (final SequenceFeature sf : mf.features)
      {
        addFeatureDetailsMenuItem(details, name, sf, mf);
      }
    }
  }

  /**
   * A helper method to add one menu item whose action is to show details for
   * one feature. The menu text includes feature description, but this may be
   * truncated.
   * 
   * @param details
   * @param seqName
   * @param sf
   * @param mf
   */
  void addFeatureDetailsMenuItem(JMenu details, final String seqName,
          final SequenceFeature sf, MappedFeatures mf)
  {
    int start = sf.getBegin();
    int end = sf.getEnd();
    if (mf != null)
    {
      /*
       * show local rather than linked feature coordinates
       */
      int[] localRange = mf.getMappedPositions(start, end);
      if (localRange == null)
      {
        // e.g. variant extending to stop codon so not mappable
        return;
      }
      start = localRange[0];
      end = localRange[localRange.length - 1];
    }
    StringBuilder desc = new StringBuilder();
    desc.append(sf.getType()).append(" ").append(String.valueOf(start));
    if (start != end)
    {
      desc.append(sf.isContactFeature() ? ":" : "-");
      desc.append(String.valueOf(end));
    }
    String description = sf.getDescription();
    if (description != null)
    {
      desc.append(" ");
      description = StringUtils.stripHtmlTags(description);

      /*
       * truncate overlong descriptions unless they contain an href
       * (as truncation could leave corrupted html)
       */
      boolean hasLink = description.indexOf("a href") > -1;
      if (description.length() > FEATURE_DESC_MAX && !hasLink)
      {
        description = description.substring(0, FEATURE_DESC_MAX) + "...";
      }
      desc.append(description);
    }
    String featureGroup = sf.getFeatureGroup();
    if (featureGroup != null)
    {
      desc.append(" (").append(featureGroup).append(")");
    }
    String htmlText = JvSwingUtils.wrapTooltip(true, desc.toString());
    JMenuItem item = new JMenuItem(htmlText);
    item.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showFeatureDetails(sf, seqName, mf);
      }
    });
    details.add(item);
  }

  /**
   * Opens a panel showing a text report of feature details
   * 
   * @param sf
   * @param seqName
   * @param mf
   */
  protected void showFeatureDetails(SequenceFeature sf, String seqName,
          MappedFeatures mf)
  {
    JInternalFrame details;
    if (Platform.isJS())
    {
      details = new JInternalFrame();
      JPanel panel = new JPanel(new BorderLayout());
      panel.setOpaque(true);
      panel.setBackground(Color.white);
      // TODO JAL-3026 set style of table correctly for feature details
      JLabel reprt = new JLabel(MessageManager
              .formatMessage("label.html_content", new Object[]
              { sf.getDetailsReport(seqName, mf) }));
      reprt.setBackground(Color.WHITE);
      reprt.setOpaque(true);
      panel.add(reprt, BorderLayout.CENTER);
      details.setContentPane(panel);
      details.pack();
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      CutAndPasteHtmlTransfer cap = new CutAndPasteHtmlTransfer();
      // it appears Java's CSS does not support border-collapse :-(
      cap.addStylesheetRule("table { border-collapse: collapse;}");
      cap.addStylesheetRule("table, td, th {border: 1px solid black;}");
      cap.setText(sf.getDetailsReport(seqName, mf));
      details = cap;
    }
    Desktop.addInternalFrame(details,
            MessageManager.getString("label.feature_details"), 500, 500);
  }

  /**
   * Adds a 'Link' menu item with a sub-menu item for each hyperlink provided.
   * When seq is not null, these are links for the sequence id, which may be to
   * external web sites for the sequence accession, and/or links embedded in
   * non-positional features. When seq is null, only links embedded in the
   * provided features are added. If no links are found, the menu is not added.
   * 
   * @param seq
   * @param features
   */
  void addLinks(final SequenceI seq, List<SequenceFeature> features)
  {
    JMenu linkMenu = buildLinkMenu(forIdPanel ? seq : null, features);

    // only add link menu if it has entries
    if (linkMenu.getItemCount() > 0)
    {
      if (forIdPanel)
      {
        sequenceMenu.add(linkMenu);
      }
      else
      {
        add(linkMenu);
      }
    }
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
  protected void buildAnnotationTypesMenus(JMenu showMenu, JMenu hideMenu,
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
   * Returns a list of sequences - either the current selection group (if there
   * is one), else the specified single sequence.
   * 
   * @param seq
   * @return
   */
  protected List<SequenceI> getSequenceScope(SequenceI seq)
  {
    List<SequenceI> forSequences = null;
    final SequenceGroup selectionGroup = ap.av.getSelectionGroup();
    if (selectionGroup != null && selectionGroup.getSize() > 0)
    {
      forSequences = selectionGroup.getSequences();
    }
    else
    {
      forSequences = seq == null ? Collections.<SequenceI> emptyList()
              : Arrays.asList(seq);
    }
    return forSequences;
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
  protected void addAnnotationTypeToShowHide(JMenu showOrHideMenu,
          final List<SequenceI> forSequences, String calcId,
          final List<String> types, final boolean allTypes,
          final boolean actionIsShow)
  {
    String label = types.toString(); // [a, b, c]
    label = label.substring(1, label.length() - 1); // a, b, c
    final JMenuItem item = new JMenuItem(label);
    item.setToolTipText(calcId);
    item.addActionListener(new ActionListener()
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

  private void buildGroupURLMenu(SequenceGroup sg, List<String> groupLinks)
  {

    // TODO: usability: thread off the generation of group url content so root
    // menu appears asap
    // sequence only URLs
    // ID/regex match URLs
    JMenu groupLinksMenu = new JMenu(
            MessageManager.getString("action.group_link"));
    // three types of url that might be created.
    JMenu[] linkMenus = new JMenu[] { null,
        new JMenu(MessageManager.getString("action.ids")),
        new JMenu(MessageManager.getString("action.sequences")),
        new JMenu(MessageManager.getString("action.ids_sequences")) };

    SequenceI[] seqs = ap.av.getSelectionAsNewSequence();
    String[][] idandseqs = GroupUrlLink.formStrings(seqs);
    Hashtable<String, Object[]> commonDbrefs = new Hashtable<>();
    for (int sq = 0; sq < seqs.length; sq++)
    {

      int start = seqs[sq].findPosition(sg.getStartRes()),
              end = seqs[sq].findPosition(sg.getEndRes());
      // just collect ids from dataset sequence
      // TODO: check if IDs collected from selecton group intersects with the
      // current selection, too
      SequenceI sqi = seqs[sq];
      while (sqi.getDatasetSequence() != null)
      {
        sqi = sqi.getDatasetSequence();
      }
      List<DBRefEntry> dbr = sqi.getDBRefs();
      int nd;
      if (dbr != null && (nd = dbr.size()) > 0)
      {
        for (int d = 0; d < nd; d++)
        {
          DBRefEntry e = dbr.get(d);
          String src = e.getSource(); // jalview.util.DBRefUtils.getCanonicalName(dbr[d].getSource()).toUpperCase(Locale.ROOT);
          Object[] sarray = commonDbrefs.get(src);
          if (sarray == null)
          {
            sarray = new Object[2];
            sarray[0] = new int[] { 0 };
            sarray[1] = new String[seqs.length];

            commonDbrefs.put(src, sarray);
          }

          if (((String[]) sarray[1])[sq] == null)
          {
            if (!e.hasMap()
                    || (e.getMap().locateMappedRange(start, end) != null))
            {
              ((String[]) sarray[1])[sq] = e.getAccessionId();
              ((int[]) sarray[0])[0]++;
            }
          }
        }
      }
    }
    // now create group links for all distinct ID/sequence sets.
    boolean addMenu = false; // indicates if there are any group links to give
                             // to user
    for (String link : groupLinks)
    {
      GroupUrlLink urlLink = null;
      try
      {
        urlLink = new GroupUrlLink(link);
      } catch (Exception foo)
      {
        Console.error("Exception for GroupURLLink '" + link + "'", foo);
        continue;
      }
      if (!urlLink.isValid())
      {
        Console.error(urlLink.getInvalidMessage());
        continue;
      }
      final String label = urlLink.getLabel();
      boolean usingNames = false;
      // Now see which parts of the group apply for this URL
      String ltarget = urlLink.getTarget(); // jalview.util.DBRefUtils.getCanonicalName(urlLink.getTarget());
      Object[] idset = commonDbrefs.get(ltarget.toUpperCase(Locale.ROOT));
      String[] seqstr, ids; // input to makeUrl
      if (idset != null)
      {
        int numinput = ((int[]) idset[0])[0];
        String[] allids = ((String[]) idset[1]);
        seqstr = new String[numinput];
        ids = new String[numinput];
        for (int sq = 0, idcount = 0; sq < seqs.length; sq++)
        {
          if (allids[sq] != null)
          {
            ids[idcount] = allids[sq];
            seqstr[idcount++] = idandseqs[1][sq];
          }
        }
      }
      else
      {
        // just use the id/seq set
        seqstr = idandseqs[1];
        ids = idandseqs[0];
        usingNames = true;
      }
      // and try and make the groupURL!

      Object[] urlset = null;
      try
      {
        urlset = urlLink.makeUrlStubs(ids, seqstr,
                "FromJalview" + System.currentTimeMillis(), false);
      } catch (UrlStringTooLongException e)
      {
      }
      if (urlset != null)
      {
        int type = urlLink.getGroupURLType() & 3;
        // first two bits ofurlLink type bitfield are sequenceids and sequences
        // TODO: FUTURE: ensure the groupURL menu structure can be generalised
        addshowLink(linkMenus[type],
                label + (((type & 1) == 1)
                        ? ("(" + (usingNames ? "Names" : ltarget) + ")")
                        : ""),
                urlLink, urlset);
        addMenu = true;
      }
    }
    if (addMenu)
    {
      groupLinksMenu = new JMenu(
              MessageManager.getString("action.group_link"));
      for (int m = 0; m < linkMenus.length; m++)
      {
        if (linkMenus[m] != null
                && linkMenus[m].getMenuComponentCount() > 0)
        {
          groupLinksMenu.add(linkMenus[m]);
        }
      }

      groupMenu.add(groupLinksMenu);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @throws Exception
   *           DOCUMENT ME!
   */
  private void jbInit() throws Exception
  {
    groupMenu.setText(MessageManager.getString("label.selection"));
    groupName.setText(MessageManager.getString("label.name"));
    groupName.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        groupName_actionPerformed();
      }
    });
    sequenceMenu.setText(MessageManager.getString("label.sequence"));

    JMenuItem sequenceName = new JMenuItem(
            MessageManager.getString("label.edit_name_description"));
    sequenceName.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sequenceName_actionPerformed();
      }
    });
    JMenuItem chooseAnnotations = new JMenuItem(
            MessageManager.getString("action.choose_annotations"));
    chooseAnnotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        chooseAnnotations_actionPerformed(e);
      }
    });
    JMenuItem sequenceDetails = new JMenuItem(
            MessageManager.getString("label.sequence_details"));
    sequenceDetails.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createSequenceDetailsReport(new SequenceI[] { sequence });
      }
    });
    JMenuItem sequenceSelDetails = new JMenuItem(
            MessageManager.getString("label.sequence_details"));
    sequenceSelDetails.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createSequenceDetailsReport(ap.av.getSequenceSelection());
      }
    });

    unGroupMenuItem
            .setText(MessageManager.getString("action.remove_group"));
    unGroupMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        unGroupMenuItem_actionPerformed();
      }
    });
    createGroupMenuItem
            .setText(MessageManager.getString("action.create_group"));
    createGroupMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createGroupMenuItem_actionPerformed();
      }
    });

    JMenuItem outline = new JMenuItem(
            MessageManager.getString("action.border_colour"));
    outline.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        outline_actionPerformed();
      }
    });
    showBoxes.setText(MessageManager.getString("action.boxes"));
    showBoxes.setState(true);
    showBoxes.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showBoxes_actionPerformed();
      }
    });
    showText.setText(MessageManager.getString("action.text"));
    showText.setState(true);
    showText.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showText_actionPerformed();
      }
    });
    showColourText.setText(MessageManager.getString("label.colour_text"));
    showColourText.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showColourText_actionPerformed();
      }
    });
    displayNonconserved
            .setText(MessageManager.getString("label.show_non_conserved"));
    displayNonconserved.setState(true);
    displayNonconserved.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showNonconserved_actionPerformed();
      }
    });
    editMenu.setText(MessageManager.getString("action.edit"));
    JMenuItem cut = new JMenuItem(MessageManager.getString("action.cut"));
    cut.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cut_actionPerformed();
      }
    });
    upperCase.setText(MessageManager.getString("label.to_upper_case"));
    upperCase.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    JMenuItem copy = new JMenuItem(MessageManager.getString("action.copy"));
    copy.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        copy_actionPerformed();
      }
    });
    lowerCase.setText(MessageManager.getString("label.to_lower_case"));
    lowerCase.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    toggle.setText(MessageManager.getString("label.toggle_case"));
    toggle.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    outputMenu.setText(
            MessageManager.getString("label.out_to_textbox") + "...");
    seqShowAnnotationsMenu
            .setText(MessageManager.getString("label.show_annotations"));
    seqHideAnnotationsMenu
            .setText(MessageManager.getString("label.hide_annotations"));
    groupShowAnnotationsMenu
            .setText(MessageManager.getString("label.show_annotations"));
    groupHideAnnotationsMenu
            .setText(MessageManager.getString("label.hide_annotations"));
    JMenuItem sequenceFeature = new JMenuItem(
            MessageManager.getString("label.create_sequence_feature"));
    sequenceFeature.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sequenceFeature_actionPerformed();
      }
    });
    editGroupMenu.setText(MessageManager.getString("label.group"));
    chooseStructure.setText(
            MessageManager.getString("label.show_pdbstruct_dialog"));
    chooseStructure.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        SequenceI[] selectedSeqs = new SequenceI[] { sequence };
        if (ap.av.getSelectionGroup() != null)
        {
          selectedSeqs = ap.av.getSequenceSelection();
        }
        new StructureChooser(selectedSeqs, sequence, ap);
      }
    });

    rnaStructureMenu
            .setText(MessageManager.getString("label.view_rna_structure"));

    // colStructureMenu.setText("Colour By Structure");
    JMenuItem editSequence = new JMenuItem(
            MessageManager.getString("label.edit_sequence") + "...");
    editSequence.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        editSequence_actionPerformed();
      }
    });
    makeReferenceSeq.setText(
            MessageManager.getString("label.mark_as_representative"));
    makeReferenceSeq.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        makeReferenceSeq_actionPerformed(actionEvent);

      }
    });

    groupMenu.add(sequenceSelDetails);
    add(groupMenu);
    add(sequenceMenu);
    add(rnaStructureMenu);
    add(chooseStructure);
    if (forIdPanel)
    {
      JMenuItem hideInsertions = new JMenuItem(
              MessageManager.getString("label.hide_insertions"));
      hideInsertions.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          hideInsertions_actionPerformed(e);
        }
      });
      add(hideInsertions);
    }
    // annotations configuration panel suppressed for now
    // groupMenu.add(chooseAnnotations);

    /*
     * Add show/hide annotations to the Sequence menu, and to the Selection menu
     * (if a selection group is in force).
     */
    sequenceMenu.add(seqShowAnnotationsMenu);
    sequenceMenu.add(seqHideAnnotationsMenu);
    sequenceMenu.add(seqAddReferenceAnnotations);
    groupMenu.add(groupShowAnnotationsMenu);
    groupMenu.add(groupHideAnnotationsMenu);
    groupMenu.add(groupAddReferenceAnnotations);
    groupMenu.add(editMenu);
    groupMenu.add(outputMenu);
    groupMenu.add(sequenceFeature);
    groupMenu.add(createGroupMenuItem);
    groupMenu.add(unGroupMenuItem);
    groupMenu.add(editGroupMenu);
    sequenceMenu.add(sequenceName);
    sequenceMenu.add(sequenceDetails);
    sequenceMenu.add(makeReferenceSeq);

    initColourMenu();
    buildColourMenu();

    editMenu.add(copy);
    editMenu.add(cut);
    editMenu.add(editSequence);
    editMenu.add(upperCase);
    editMenu.add(lowerCase);
    editMenu.add(toggle);
    editGroupMenu.add(groupName);
    editGroupMenu.add(colourMenu);
    editGroupMenu.add(showBoxes);
    editGroupMenu.add(showText);
    editGroupMenu.add(showColourText);
    editGroupMenu.add(outline);
    editGroupMenu.add(displayNonconserved);
  }

  /**
   * Constructs the entries for the colour menu
   */
  protected void initColourMenu()
  {
    colourMenu.setText(MessageManager.getString("label.group_colour"));
    textColour.setText(MessageManager.getString("label.text_colour"));
    textColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        textColour_actionPerformed();
      }
    });

    abovePIDColour.setText(
            MessageManager.getString("label.above_identity_threshold"));
    abovePIDColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        abovePIDColour_actionPerformed(abovePIDColour.isSelected());
      }
    });

    modifyPID.setText(
            MessageManager.getString("label.modify_identity_threshold"));
    modifyPID.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        modifyPID_actionPerformed();
      }
    });

    conservationMenuItem
            .setText(MessageManager.getString("action.by_conservation"));
    conservationMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        conservationMenuItem_actionPerformed(
                conservationMenuItem.isSelected());
      }
    });

    annotationColour = new JRadioButtonMenuItem(
            MessageManager.getString("action.by_annotation"));
    annotationColour.setName(ResidueColourScheme.ANNOTATION_COLOUR);
    annotationColour.setEnabled(false);
    annotationColour.setToolTipText(
            MessageManager.getString("label.by_annotation_tooltip"));

    modifyConservation.setText(MessageManager
            .getString("label.modify_conservation_threshold"));
    modifyConservation.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        modifyConservation_actionPerformed();
      }
    });
  }

  /**
   * Builds the group colour sub-menu, including any user-defined colours which
   * were loaded at startup or during the Jalview session
   */
  protected void buildColourMenu()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg == null)
    {
      /*
       * popup menu with no sequence group scope
       */
      return;
    }
    colourMenu.removeAll();
    colourMenu.add(textColour);
    colourMenu.addSeparator();

    ButtonGroup bg = ColourMenuHelper.addMenuItems(colourMenu, this, sg,
            false);
    bg.add(annotationColour);
    colourMenu.add(annotationColour);

    colourMenu.addSeparator();
    colourMenu.add(conservationMenuItem);
    colourMenu.add(modifyConservation);
    colourMenu.add(abovePIDColour);
    colourMenu.add(modifyPID);
  }

  protected void modifyConservation_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs != null)
    {
      SliderPanel.setConservationSlider(ap, sg.cs, sg.getName());
      SliderPanel.showConservationSlider();
    }
  }

  protected void modifyPID_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs != null)
    {
      // int threshold = SliderPanel.setPIDSliderSource(ap, sg.cs, getGroup()
      // .getName());
      // sg.cs.setThreshold(threshold, ap.av.isIgnoreGapsConsensus());
      SliderPanel.setPIDSliderSource(ap, sg.cs, getGroup().getName());
      SliderPanel.showPIDSlider();
    }
  }

  /**
   * Check for any annotations on the underlying dataset sequences (for the
   * current selection group) which are not 'on the alignment'.If any are found,
   * enable the option to add them to the alignment. The criteria for 'on the
   * alignment' is finding an alignment annotation on the alignment, matched on
   * calcId, label and sequenceRef.
   * 
   * A tooltip is also constructed that displays the source (calcId) and type
   * (label) of the annotations that can be added.
   * 
   * @param menuItem
   * @param forSequences
   */
  protected void configureReferenceAnnotationsMenu(JMenuItem menuItem,
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
       * configure its tooltip and action.
       */
      menuItem.setEnabled(true);
      for (String calcId : tipEntries.keySet())
      {
        tooltip.append("<br/>" + calcId + "/" + tipEntries.get(calcId));
      }
      String tooltipText = JvSwingUtils.wrapTooltip(true,
              tooltip.toString());
      menuItem.setToolTipText(tooltipText);

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
    final AlignmentI alignment = this.ap.getAlignment();
    AlignmentUtils.addReferenceAnnotations(candidates, alignment,
            null);
    refresh();
  }

  protected void makeReferenceSeq_actionPerformed(ActionEvent actionEvent)
  {
    if (!ap.av.getAlignment().hasSeqrep())
    {
      // initialise the display flags so the user sees something happen
      ap.av.setDisplayReferenceSeq(true);
      ap.av.setColourByReferenceSeq(true);
      ap.av.getAlignment().setSeqrep(sequence);
    }
    else
    {
      if (ap.av.getAlignment().getSeqrep() == sequence)
      {
        ap.av.getAlignment().setSeqrep(null);
      }
      else
      {
        ap.av.getAlignment().setSeqrep(sequence);
      }
    }
    refresh();
  }

  protected void hideInsertions_actionPerformed(ActionEvent actionEvent)
  {
    HiddenColumns hidden = ap.av.getAlignment().getHiddenColumns();
    BitSet inserts = new BitSet();

    boolean markedPopup = false;
    // mark inserts in current selection
    if (ap.av.getSelectionGroup() != null)
    {
      // mark just the columns in the selection group to be hidden
      inserts.set(ap.av.getSelectionGroup().getStartRes(),
              ap.av.getSelectionGroup().getEndRes() + 1); // TODO why +1?

      // now clear columns without gaps
      for (SequenceI sq : ap.av.getSelectionGroup().getSequences())
      {
        if (sq == sequence)
        {
          markedPopup = true;
        }
        inserts.and(sq.getInsertionsAsBits());
      }
      hidden.clearAndHideColumns(inserts,
              ap.av.getSelectionGroup().getStartRes(),
              ap.av.getSelectionGroup().getEndRes());
    }

    // now mark for sequence under popup if we haven't already done it
    else if (!markedPopup && sequence != null)
    {
      inserts.or(sequence.getInsertionsAsBits());

      // and set hidden columns accordingly
      hidden.hideColumns(inserts);
    }
    refresh();
  }

  protected void sequenceSelectionDetails_actionPerformed()
  {
    createSequenceDetailsReport(ap.av.getSequenceSelection());
  }

  public void createSequenceDetailsReport(SequenceI[] sequences)
  {
    StringBuilder contents = new StringBuilder(128);
    contents.append("<html><body>");
    for (SequenceI seq : sequences)
    {
      contents.append("<p><h2>" + MessageManager.formatMessage(
              "label.create_sequence_details_report_annotation_for",
              new Object[]
              { seq.getDisplayId(true) }) + "</h2></p>\n<p>");
      new SequenceAnnotationReport(false).createSequenceAnnotationReport(
              contents, seq, true, true, ap.getSeqPanel().seqCanvas.fr);
      contents.append("</p>");
    }
    contents.append("</body></html>");
    String report = contents.toString();

    JInternalFrame frame;
    if (Platform.isJS())
    {
      JLabel textLabel = new JLabel();
      textLabel.setText(report);
      textLabel.setBackground(Color.WHITE);
      JPanel pane = new JPanel(new BorderLayout());
      pane.setOpaque(true);
      pane.setBackground(Color.WHITE);
      pane.add(textLabel, BorderLayout.NORTH);
      frame = new JInternalFrame();
      frame.getContentPane().add(new JScrollPane(pane));
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      CutAndPasteHtmlTransfer cap = new CutAndPasteHtmlTransfer();
      cap.setText(report);
      frame = cap;
    }

    Desktop.addInternalFrame(frame,
            MessageManager.formatMessage("label.sequence_details_for",
                    (sequences.length == 1 ? new Object[]
                    { sequences[0].getDisplayId(true) }
                            : new Object[]
                            { MessageManager
                                    .getString("label.selection") })),
            500, 400);
  }

  protected void showNonconserved_actionPerformed()
  {
    getGroup().setShowNonconserved(displayNonconserved.isSelected());
    refresh();
  }

  /**
   * call to refresh view after settings change
   */
  void refresh()
  {
    ap.updateAnnotation();
    // removed paintAlignment(true) here:
    // updateAnnotation calls paintAlignment already, so don't need to call
    // again

    PaintRefresher.Refresh(this, ap.av.getSequenceSetId());
  }

  /*
   * protected void covariationColour_actionPerformed() { getGroup().cs = new
   * CovariationColourScheme(sequence.getAnnotation()[0]); refresh(); }
   */
  /**
   * DOCUMENT ME!
   * 
   * @param selected
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void abovePIDColour_actionPerformed(boolean selected)
  {
System.out.println(String.format("PopupMenu @abovePIDColour_actionPerformed (%b)", selected));
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (selected)
    {
      sg.cs.setConsensus(AAFrequency.calculate(
              sg.getSequences(ap.av.getHiddenRepSequences()),
              sg.getStartRes(), sg.getEndRes() + 1));

      int threshold = SliderPanel.setPIDSliderSource(ap,
              sg.getGroupColourScheme(), getGroup().getName());

      sg.cs.setThreshold(threshold, ap.av.isIgnoreGapsConsensus());

      SliderPanel.showPIDSlider();
    }
    else
    // remove PIDColouring
    {
      sg.cs.setThreshold(0, ap.av.isIgnoreGapsConsensus());
      SliderPanel.hidePIDSlider();
    }
    modifyPID.setEnabled(selected);

    refresh();
  }

  /**
   * Open a panel where the user can choose which types of sequence annotation
   * to show or hide.
   * 
   * @param e
   */
  protected void chooseAnnotations_actionPerformed(ActionEvent e)
  {
    // todo correct way to guard against opening a duplicate panel?
    new AnnotationChooser(ap);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void conservationMenuItem_actionPerformed(boolean selected)
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (selected)
    {
      // JBPNote: Conservation name shouldn't be i18n translated
      Conservation c = new Conservation("Group",
              sg.getSequences(ap.av.getHiddenRepSequences()),
              sg.getStartRes(), sg.getEndRes() + 1);

      c.calculate();
      c.verdict(false, ap.av.getConsPercGaps());
      sg.cs.setConservation(c);

      SliderPanel.setConservationSlider(ap, sg.getGroupColourScheme(),
              sg.getName());
      SliderPanel.showConservationSlider();
    }
    else
    // remove ConservationColouring
    {
      sg.cs.setConservation(null);
      SliderPanel.hideConservationSlider();
    }
    modifyConservation.setEnabled(selected);

    refresh();
  }

  /**
   * Shows a dialog where group name and description may be edited
   */
  protected void groupName_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    EditNameDialog dialog = new EditNameDialog(sg.getName(),
            sg.getDescription(),
            MessageManager.getString("label.group_name"),
            MessageManager.getString("label.group_description"));
    dialog.showDialog(ap.alignFrame,
            MessageManager.getString("label.edit_group_name_description"),
            () -> {
              sg.setName(dialog.getName());
              sg.setDescription(dialog.getDescription());
              refresh();
            });
  }

  /**
   * Get selection group - adding it to the alignment if necessary.
   * 
   * @return sequence group to operate on
   */
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

  /**
   * Shows a dialog where the sequence name and description may be edited. If a
   * name containing spaces is entered, these are converted to underscores, with
   * a warning message.
   */
  void sequenceName_actionPerformed()
  {
    EditNameDialog dialog = new EditNameDialog(sequence.getName(),
            sequence.getDescription(),
            MessageManager.getString("label.sequence_name"),
            MessageManager.getString("label.sequence_description"));
    dialog.showDialog(ap.alignFrame, MessageManager
            .getString("label.edit_sequence_name_description"), () -> {
              if (dialog.getName() != null)
              {
                if (dialog.getName().indexOf(" ") > -1)
                {
                  String ok = MessageManager.getString("action.ok");
                  String cancel = MessageManager.getString("action.cancel");
                  String message = MessageManager.getString(
                          "label.spaces_converted_to_underscores");
                  String title = MessageManager.getString(
                          "label.no_spaces_allowed_sequence_name");
                  Object[] options = new Object[] { ok, cancel };

                  JvOptionPane.frameDialog(message, title,
                          JvOptionPane.WARNING_MESSAGE, null, null, null,
                          false);
                }
                sequence.setName(dialog.getName().replace(' ', '_'));
                ap.paintAlignment(false, false);
              }
              sequence.setDescription(dialog.getDescription());
              ap.av.firePropertyChange("alignment", null,
                      ap.av.getAlignment().getSequences());
            });
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  void unGroupMenuItem_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    ap.av.getAlignment().deleteGroup(sg);
    ap.av.setSelectionGroup(null);
    refresh();
  }

  void createGroupMenuItem_actionPerformed()
  {
    getGroup(); // implicitly creates group - note - should apply defaults / use
                // standard alignment window logic for this
    refresh();
  }

  /**
   * Offers a colour chooser and sets the selected colour as the group outline
   */
  protected void outline_actionPerformed()
  {
    String title = MessageManager.getString("label.select_outline_colour");
    ColourChooserListener listener = new ColourChooserListener()
    {
      @Override
      public void colourSelected(Color c)
      {
        getGroup().setOutlineColour(c);
        refresh();
      }
    };
    JalviewColourChooser.showColourChooser(Desktop.getDesktop(), title,
            Color.BLUE, listener);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void showBoxes_actionPerformed()
  {
    getGroup().setDisplayBoxes(showBoxes.isSelected());
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void showText_actionPerformed()
  {
    getGroup().setDisplayText(showText.isSelected());
    refresh();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void showColourText_actionPerformed()
  {
    getGroup().setColourText(showColourText.isSelected());
    refresh();
  }

  void hideSequences(boolean representGroup)
  {
    ap.av.hideSequences(sequence, representGroup);
  }

  public void copy_actionPerformed()
  {
    ap.alignFrame.copy_actionPerformed();
  }

  public void cut_actionPerformed()
  {
    ap.alignFrame.cut_actionPerformed();
  }

  void changeCase(ActionEvent e)
  {
    Object source = e.getSource();
    SequenceGroup sg = ap.av.getSelectionGroup();

    if (sg != null)
    {
      List<int[]> startEnd = ap.av.getVisibleRegionBoundaries(
              sg.getStartRes(), sg.getEndRes() + 1);

      String description;
      int caseChange;

      if (source == toggle)
      {
        description = MessageManager.getString("label.toggle_case");
        caseChange = ChangeCaseCommand.TOGGLE_CASE;
      }
      else if (source == upperCase)
      {
        description = MessageManager.getString("label.to_upper_case");
        caseChange = ChangeCaseCommand.TO_UPPER;
      }
      else
      {
        description = MessageManager.getString("label.to_lower_case");
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

  public void outputText_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    cap.setForInput(null);
    Desktop.addInternalFrame(cap, MessageManager
            .formatMessage("label.alignment_output_command", new Object[]
            { e.getActionCommand() }), 600, 500);

    String[] omitHidden = null;

    System.out.println("PROMPT USER HERE"); // TODO: decide if a prompt happens
    // or we simply trust the user wants
    // wysiwig behaviour

    FileFormatI fileFormat = FileFormats.getInstance()
            .forName(e.getActionCommand());
    cap.setText(
            new FormatAdapter(ap).formatSequences(fileFormat, ap, true));
  }

  public void sequenceFeature_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg == null)
    {
      return;
    }

    List<SequenceI> seqs = new ArrayList<>();
    List<SequenceFeature> features = new ArrayList<>();

    /*
     * assemble dataset sequences, and template new sequence features,
     * for the amend features dialog
     */
    int gSize = sg.getSize();
    for (int i = 0; i < gSize; i++)
    {
      int start = sg.getSequenceAt(i).findPosition(sg.getStartRes());
      int end = sg.findEndRes(sg.getSequenceAt(i));
      if (start <= end)
      {
        seqs.add(sg.getSequenceAt(i).getDatasetSequence());
        features.add(new SequenceFeature(null, null, start, end, null));
      }
    }

    /*
     * an entirely gapped region will generate empty lists of sequence / features
     */
    if (!seqs.isEmpty())
    {
      new FeatureEditor(ap, seqs, features, true).showDialog();
    }
  }

  public void textColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg != null)
    {
      new TextColourChooser().chooseColour(ap, sg);
    }
  }

  /**
   * Shows a dialog where sequence characters may be edited. Any changes are
   * applied, and added as an available 'Undo' item in the edit commands
   * history.
   */
  public void editSequence_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();

    SequenceI seq = sequence;
    if (sg != null)
    {
      if (seq == null)
      {
        seq = sg.getSequenceAt(0);
      }

      EditNameDialog dialog = new EditNameDialog(
              seq.getSequenceAsString(sg.getStartRes(), sg.getEndRes() + 1),
              null, MessageManager.getString("label.edit_sequence"), null);
      dialog.showDialog(ap.alignFrame,
              MessageManager.getString("label.edit_sequence"), () -> {
                EditCommand editCommand = new EditCommand(
                        MessageManager.getString("label.edit_sequences"),
                        Action.REPLACE,
                        dialog.getName().replace(' ',
                                ap.av.getGapCharacter()),
                        sg.getSequencesAsArray(
                                ap.av.getHiddenRepSequences()),
                        sg.getStartRes(), sg.getEndRes() + 1,
                        ap.av.getAlignment());
                ap.alignFrame.addHistoryItem(editCommand);
                ap.av.firePropertyChange("alignment", null,
                        ap.av.getAlignment().getSequences());
              });
    }
  }

  /**
   * Action on user selecting an item from the colour menu (that does not have
   * its bespoke action handler)
   * 
   * @return
   */
  @Override
  public void changeColour_actionPerformed(String colourSchemeName)
  {
System.out.println(String.format("PopupMenu @changeColour_actionPerformed (%s)", colourSchemeName));
    SequenceGroup sg = getGroup();
    /*
     * switch to the chosen colour scheme (or null for None)
     */
    ColourSchemeI colourScheme = ColourSchemes.getInstance()
            .getColourScheme(colourSchemeName, ap.av, sg,
                    ap.av.getHiddenRepSequences());
    sg.setColourScheme(colourScheme);
    if (colourScheme instanceof Blosum62ColourScheme
            || colourScheme instanceof PIDColourScheme)
    {
      sg.cs.setConsensus(AAFrequency.calculate(
              sg.getSequences(ap.av.getHiddenRepSequences()),
              sg.getStartRes(), sg.getEndRes() + 1));
    }

    refresh();
  }

}
