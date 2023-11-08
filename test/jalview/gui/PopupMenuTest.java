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

import static jalview.util.UrlConstants.DB_ACCESSION;
import static jalview.util.UrlConstants.SEQUENCE_ID;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;
import jalview.urls.api.UrlProviderFactoryI;
import jalview.urls.desktop.DesktopUrlProviderFactory;
import jalview.util.MessageManager;
import jalview.util.UrlConstants;

public class PopupMenuTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // 4 sequences x 13 positions
  final static String TEST_DATA = ">FER_CAPAA Ferredoxin\n"
          + "TIETHKEAELVG-\n"
          + ">FER_CAPAN Ferredoxin, chloroplast precursor\n"
          + "TIETHKEAELVG-\n"
          + ">FER1_SOLLC Ferredoxin-1, chloroplast precursor\n"
          + "TIETHKEEELTA-\n" + ">Q93XJ9_SOLTU Ferredoxin I precursor\n"
          + "TIETHKEEELTA-\n";

  AlignmentI alignment;

  AlignmentPanel parentPanel;

  PopupMenu testee = null;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws IOException
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Console.initLogger();

    String inMenuString = ("EMBL-EBI Search | http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$"
            + SEQUENCE_ID + "$" + "|"
            + "UNIPROT | http://www.uniprot.org/uniprot/$" + DB_ACCESSION
            + "$") + "|"
            + ("INTERPRO | http://www.ebi.ac.uk/interpro/entry/$"
                    + DB_ACCESSION + "$")
            + "|" +
            // Gene3D entry tests for case (in)sensitivity
            ("Gene3D | http://gene3d.biochem.ucl.ac.uk/Gene3D/search?sterm=$"
                    + DB_ACCESSION + "$&mode=protein");

    UrlProviderFactoryI factory = new DesktopUrlProviderFactory(
            UrlConstants.DEFAULT_LABEL, inMenuString, "");
    Preferences.sequenceUrlLinks = factory.createUrlProvider();

    alignment = new FormatAdapter().readFile(TEST_DATA,
            DataSourceType.PASTE, FileFormat.Fasta);
    AlignFrame af = new AlignFrame(alignment, 700, 500);
    parentPanel = new AlignmentPanel(af, af.getViewport());
    testee = new PopupMenu(parentPanel, alignment.getSequenceAt(0), null);
    int i = 0;
    for (SequenceI seq : alignment.getSequences())
    {
      final AlignmentAnnotation annotation = new AlignmentAnnotation(
              "label" + i, "desc" + i, i);
      annotation.setCalcId("calcId" + i);
      seq.addAlignmentAnnotation(annotation);
      annotation.setSequenceRef(seq);
    }
  }

  @Test(groups = { "Functional" })
  public void testConfigureReferenceAnnotationsMenu_noSequenceSelected()
  {
    JMenuItem menu = new JMenuItem();
    List<SequenceI> seqs = new ArrayList<>();
    testee.configureReferenceAnnotationsMenu(menu, seqs);
    assertFalse(menu.isEnabled());
    // now try null list
    menu.setEnabled(true);
    testee.configureReferenceAnnotationsMenu(menu, null);
    assertFalse(menu.isEnabled());
  }

  /**
   * Test building the 'add reference annotations' menu for the case where there
   * are no reference annotations to add to the alignment. The menu item should
   * be disabled.
   */
  @Test(groups = { "Functional" })
  public void testConfigureReferenceAnnotationsMenu_noReferenceAnnotations()
  {
    JMenuItem menu = new JMenuItem();

    /*
     * Initial state is that sequences have annotations, and have dataset
     * sequences, but the dataset sequences have no annotations. Hence nothing
     * to add.
     */
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    testee.configureReferenceAnnotationsMenu(menu, seqs);
    assertFalse(menu.isEnabled());
  }

  /**
   * Test building the 'add reference annotations' menu for the case where all
   * reference annotations are already on the alignment. The menu item should be
   * disabled.
   */
  @Test(groups = { "Functional" })
  public void testConfigureReferenceAnnotationsMenu_alreadyAdded()
  {
    JMenuItem menu = new JMenuItem();
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    // make up new annotations and add to dataset sequences, sequences and
    // alignment
    attachReferenceAnnotations(seqs, true, true);

    testee.configureReferenceAnnotationsMenu(menu, seqs);
    assertFalse(menu.isEnabled());
  }

  /**
   * Test building the 'add reference annotations' menu for the case where
   * several reference annotations are on the dataset but not on the sequences.
   * The menu item should be enabled, and acquire a tooltip which lists the
   * annotation sources (calcIds) and type (labels).
   */
  @Test(groups = { "Functional" })
  public void testConfigureReferenceAnnotationsMenu()
  {
    JMenuItem menu = new JMenuItem();
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    // make up new annotations and add to dataset sequences
    attachReferenceAnnotations(seqs, false, false);

    testee.configureReferenceAnnotationsMenu(menu, seqs);
    assertTrue(menu.isEnabled());
    String s = MessageManager.getString("label.add_annotations_for");
    String expected = "<html><style> div.ttip {width:350px;white-space:pre-wrap;padding:2px;overflow-wrap:break-word;}</style>"
            + "<div class=\"ttip\">" + s
            + "<br/>Jmol/secondary structure<br/>PDB/Temp </div></html>";
    assertEquals(expected, menu.getToolTipText());
  }

  /**
   * Test building the 'add reference annotations' menu for the case where
   * several reference annotations are on the dataset and the sequences but not
   * on the alignment. The menu item should be enabled, and acquire a tooltip
   * which lists the annotation sources (calcIds) and type (labels).
   */
  @Test(groups = { "Functional" })
  public void testConfigureReferenceAnnotationsMenu_notOnAlignment()
  {
    JMenuItem menu = new JMenuItem();
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    // make up new annotations and add to dataset sequences and sequences
    attachReferenceAnnotations(seqs, true, false);

    testee.configureReferenceAnnotationsMenu(menu, seqs);
    assertTrue(menu.isEnabled());
    String s = MessageManager.getString("label.add_annotations_for");
    String expected = "<html><style> div.ttip {width:350px;white-space:pre-wrap;padding:2px;overflow-wrap:break-word;}</style>"
            + "<div class=\"ttip\">" + s
            + "<br/>Jmol/secondary structure<br/>PDB/Temp </div></html>";
    assertEquals(expected, menu.getToolTipText());
  }

  /**
   * Generate annotations and add to dataset sequences and (optionally)
   * sequences and/or alignment
   * 
   * @param seqs
   * @param addToSequence
   * @param addToAlignment
   */
  private void attachReferenceAnnotations(List<SequenceI> seqs,
          boolean addToSequence, boolean addToAlignment)
  {
    // PDB.secondary structure on Sequence0
    AlignmentAnnotation annotation = new AlignmentAnnotation(
            "secondary structure", "", 0);
    annotation.setCalcId("PDB");
    seqs.get(0).getDatasetSequence().addAlignmentAnnotation(annotation);
    if (addToSequence)
    {
      seqs.get(0).addAlignmentAnnotation(annotation);
    }
    if (addToAlignment)
    {
      this.alignment.addAnnotation(annotation);
    }

    // PDB.Temp on Sequence1
    annotation = new AlignmentAnnotation("Temp", "", 0);
    annotation.setCalcId("PDB");
    seqs.get(1).getDatasetSequence().addAlignmentAnnotation(annotation);
    if (addToSequence)
    {
      seqs.get(1).addAlignmentAnnotation(annotation);
    }
    if (addToAlignment)
    {
      this.alignment.addAnnotation(annotation);
    }

    // JMOL.secondary structure on Sequence0
    annotation = new AlignmentAnnotation("secondary structure", "", 0);
    annotation.setCalcId("Jmol");
    seqs.get(0).getDatasetSequence().addAlignmentAnnotation(annotation);
    if (addToSequence)
    {
      seqs.get(0).addAlignmentAnnotation(annotation);
    }
    if (addToAlignment)
    {
      this.alignment.addAnnotation(annotation);
    }
  }

  /**
   * Test building the 'add reference annotations' menu for the case where there
   * are two alignment views:
   * <ul>
   * <li>in one view, reference annotations have been added (are on the
   * datasets, sequences and alignment)</li>
   * <li>in the current view, reference annotations are on the dataset and
   * sequence, but not the alignment</li>
   * </ul>
   * The menu item should be enabled, and acquire a tooltip which lists the
   * annotation sources (calcIds) and type (labels).
   */
  @Test(groups = { "Functional" })
  public void testConfigureReferenceAnnotationsMenu_twoViews()
  {
  }

  /**
   * Test for building menu options including 'show' and 'hide' annotation
   * types.
   */
  @Test(groups = { "Functional" })
  public void testBuildAnnotationTypesMenus()
  {
    JMenu showMenu = new JMenu();
    JMenu hideMenu = new JMenu();
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    // make up new annotations and add to sequences and to the alignment

    // PDB.secondary structure on Sequence0
    AlignmentAnnotation annotation = new AlignmentAnnotation(
            "secondary structure", "", new Annotation[] {});
    annotation.setCalcId("PDB");
    annotation.visible = true;
    seqs.get(0).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    // JMOL.secondary structure on Sequence0 - hidden
    annotation = new AlignmentAnnotation("secondary structure", "",
            new Annotation[] {});
    annotation.setCalcId("JMOL");
    annotation.visible = false;
    seqs.get(0).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    // Jpred.SSP on Sequence0 - hidden
    annotation = new AlignmentAnnotation("SSP", "", new Annotation[] {});
    annotation.setCalcId("JPred");
    annotation.visible = false;
    seqs.get(0).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    // PDB.Temp on Sequence1
    annotation = new AlignmentAnnotation("Temp", "", new Annotation[] {});
    annotation.setCalcId("PDB");
    annotation.visible = true;
    seqs.get(1).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    /*
     * Expect menu options to show "secondary structure" and "SSP", and to hide
     * "secondary structure" and "Temp". Tooltip should be calcId.
     */
    testee.buildAnnotationTypesMenus(showMenu, hideMenu, seqs);

    assertTrue(showMenu.isEnabled());
    assertTrue(hideMenu.isEnabled());

    Component[] showOptions = showMenu.getMenuComponents();
    Component[] hideOptions = hideMenu.getMenuComponents();

    assertEquals(4, showOptions.length); // includes 'All' and separator
    assertEquals(4, hideOptions.length);
    String all = MessageManager.getString("label.all");
    assertEquals(all, ((JMenuItem) showOptions[0]).getText());
    assertTrue(showOptions[1] instanceof JPopupMenu.Separator);
    assertEquals(JSeparator.HORIZONTAL,
            ((JSeparator) showOptions[1]).getOrientation());
    assertEquals("secondary structure",
            ((JMenuItem) showOptions[2]).getText());
    assertEquals("JMOL", ((JMenuItem) showOptions[2]).getToolTipText());
    assertEquals("SSP", ((JMenuItem) showOptions[3]).getText());
    assertEquals("JPred", ((JMenuItem) showOptions[3]).getToolTipText());

    assertEquals(all, ((JMenuItem) hideOptions[0]).getText());
    assertTrue(hideOptions[1] instanceof JPopupMenu.Separator);
    assertEquals(JSeparator.HORIZONTAL,
            ((JSeparator) hideOptions[1]).getOrientation());
    assertEquals("secondary structure",
            ((JMenuItem) hideOptions[2]).getText());
    assertEquals("PDB", ((JMenuItem) hideOptions[2]).getToolTipText());
    assertEquals("Temp", ((JMenuItem) hideOptions[3]).getText());
    assertEquals("PDB", ((JMenuItem) hideOptions[3]).getToolTipText());
  }

  /**
   * Test for building menu options with only 'hide' annotation types enabled.
   */
  @Test(groups = { "Functional" })
  public void testBuildAnnotationTypesMenus_showDisabled()
  {
    JMenu showMenu = new JMenu();
    JMenu hideMenu = new JMenu();
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    // make up new annotations and add to sequences and to the alignment

    // PDB.secondary structure on Sequence0
    AlignmentAnnotation annotation = new AlignmentAnnotation(
            "secondary structure", "", new Annotation[] {});
    annotation.setCalcId("PDB");
    annotation.visible = true;
    seqs.get(0).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    // PDB.Temp on Sequence1
    annotation = new AlignmentAnnotation("Temp", "", new Annotation[] {});
    annotation.setCalcId("PDB");
    annotation.visible = true;
    seqs.get(1).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    /*
     * Expect menu options to hide "secondary structure" and "Temp". Tooltip
     * should be calcId. 'Show' menu should be disabled.
     */
    testee.buildAnnotationTypesMenus(showMenu, hideMenu, seqs);

    assertFalse(showMenu.isEnabled());
    assertTrue(hideMenu.isEnabled());

    Component[] showOptions = showMenu.getMenuComponents();
    Component[] hideOptions = hideMenu.getMenuComponents();

    assertEquals(2, showOptions.length); // includes 'All' and separator
    assertEquals(4, hideOptions.length);
    String all = MessageManager.getString("label.all");
    assertEquals(all, ((JMenuItem) showOptions[0]).getText());
    assertTrue(showOptions[1] instanceof JPopupMenu.Separator);
    assertEquals(JSeparator.HORIZONTAL,
            ((JSeparator) showOptions[1]).getOrientation());

    assertEquals(all, ((JMenuItem) hideOptions[0]).getText());
    assertTrue(hideOptions[1] instanceof JPopupMenu.Separator);
    assertEquals(JSeparator.HORIZONTAL,
            ((JSeparator) hideOptions[1]).getOrientation());
    assertEquals("secondary structure",
            ((JMenuItem) hideOptions[2]).getText());
    assertEquals("PDB", ((JMenuItem) hideOptions[2]).getToolTipText());
    assertEquals("Temp", ((JMenuItem) hideOptions[3]).getText());
    assertEquals("PDB", ((JMenuItem) hideOptions[3]).getToolTipText());
  }

  /**
   * Test for building menu options with only 'show' annotation types enabled.
   */
  @Test(groups = { "Functional" })
  public void testBuildAnnotationTypesMenus_hideDisabled()
  {
    JMenu showMenu = new JMenu();
    JMenu hideMenu = new JMenu();
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    // make up new annotations and add to sequences and to the alignment

    // PDB.secondary structure on Sequence0
    AlignmentAnnotation annotation = new AlignmentAnnotation(
            "secondary structure", "", new Annotation[] {});
    annotation.setCalcId("PDB");
    annotation.visible = false;
    seqs.get(0).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    // PDB.Temp on Sequence1
    annotation = new AlignmentAnnotation("Temp", "", new Annotation[] {});
    annotation.setCalcId("PDB2");
    annotation.visible = false;
    seqs.get(1).addAlignmentAnnotation(annotation);
    parentPanel.getAlignment().addAnnotation(annotation);

    /*
     * Expect menu options to show "secondary structure" and "Temp". Tooltip
     * should be calcId. 'hide' menu should be disabled.
     */
    testee.buildAnnotationTypesMenus(showMenu, hideMenu, seqs);

    assertTrue(showMenu.isEnabled());
    assertFalse(hideMenu.isEnabled());

    Component[] showOptions = showMenu.getMenuComponents();
    Component[] hideOptions = hideMenu.getMenuComponents();

    assertEquals(4, showOptions.length); // includes 'All' and separator
    assertEquals(2, hideOptions.length);
    String all = MessageManager.getString("label.all");
    assertEquals(all, ((JMenuItem) showOptions[0]).getText());
    assertTrue(showOptions[1] instanceof JPopupMenu.Separator);
    assertEquals(JSeparator.HORIZONTAL,
            ((JSeparator) showOptions[1]).getOrientation());
    assertEquals("secondary structure",
            ((JMenuItem) showOptions[2]).getText());
    assertEquals("PDB", ((JMenuItem) showOptions[2]).getToolTipText());
    assertEquals("Temp", ((JMenuItem) showOptions[3]).getText());
    assertEquals("PDB2", ((JMenuItem) showOptions[3]).getToolTipText());

    assertEquals(all, ((JMenuItem) hideOptions[0]).getText());
    assertTrue(hideOptions[1] instanceof JPopupMenu.Separator);
    assertEquals(JSeparator.HORIZONTAL,
            ((JSeparator) hideOptions[1]).getOrientation());
  }

  /**
   * Test for adding sequence id, dbref and feature links
   */
  @Test(groups = { "Functional" })
  public void testBuildLinkMenu()
  {
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();
    final SequenceI seq0 = seqs.get(0);
    final SequenceI seq1 = seqs.get(1);
    final List<SequenceFeature> noFeatures = Collections
            .<SequenceFeature> emptyList();
    final String linkText = MessageManager.getString("action.link");

    seq0.addDBRef(new DBRefEntry(DBRefSource.UNIPROT, "1", "P83527"));
    seq0.addDBRef(new DBRefEntry("INTERPRO", "1", "IPR001041"));
    seq0.addDBRef(new DBRefEntry("INTERPRO", "1", "IPR012675"));
    seq0.addDBRef(new DBRefEntry("INTERPRO", "1", "IPR006058"));
    seq1.addDBRef(new DBRefEntry(DBRefSource.UNIPROT, "1", "Q9ZTS2"));
    seq1.addDBRef(new DBRefEntry("GENE3D", "1", "3.10.20.30"));

    /*
     * check the Link Menu for the first sequence
     */
    JMenu linkMenu = PopupMenu.buildLinkMenu(seq0, noFeatures);
    assertEquals(linkText, linkMenu.getText());
    Component[] linkItems = linkMenu.getMenuComponents();

    /*
     * menu items are ordered: SEQUENCE_ID search first, then dbrefs in order
     * of database name (and within that by order of dbref addition)
     */
    assertEquals(5, linkItems.length);
    assertEquals("EMBL-EBI Search", ((JMenuItem) linkItems[0]).getText());
    assertEquals("INTERPRO|IPR001041",
            ((JMenuItem) linkItems[1]).getText());
    assertEquals("INTERPRO|IPR012675",
            ((JMenuItem) linkItems[2]).getText());
    assertEquals("INTERPRO|IPR006058",
            ((JMenuItem) linkItems[3]).getText());
    assertEquals("UNIPROT|P83527", ((JMenuItem) linkItems[4]).getText());

    /*
     * check the Link Menu for the second sequence
     * note dbref GENE3D is matched to link Gene3D, the latter is displayed
     */
    linkMenu = PopupMenu.buildLinkMenu(seq1, noFeatures);
    linkItems = linkMenu.getMenuComponents();
    assertEquals(3, linkItems.length);
    assertEquals("EMBL-EBI Search", ((JMenuItem) linkItems[0]).getText());
    assertEquals("Gene3D|3.10.20.30", ((JMenuItem) linkItems[1]).getText());
    assertEquals("UNIPROT|Q9ZTS2", ((JMenuItem) linkItems[2]).getText());

    /*
     * if there are no valid links the Links submenu is still shown, but
     * reduced to the EMBL-EBI lookup only (inserted by 
     * CustomUrlProvider.choosePrimaryUrl())
     */
    String unmatched = "NOMATCH|http://www.uniprot.org/uniprot/$"
            + DB_ACCESSION + "$";
    UrlProviderFactoryI factory = new DesktopUrlProviderFactory(null,
            unmatched, "");
    Preferences.sequenceUrlLinks = factory.createUrlProvider();

    linkMenu = PopupMenu.buildLinkMenu(seq1, noFeatures);
    linkItems = linkMenu.getMenuComponents();
    assertEquals(1, linkItems.length);
    assertEquals("EMBL-EBI Search", ((JMenuItem) linkItems[0]).getText());

    /*
     * if sequence is null, only feature links are shown (alignment popup submenu)
     */
    linkMenu = PopupMenu.buildLinkMenu(null, noFeatures);
    linkItems = linkMenu.getMenuComponents();
    assertEquals(0, linkItems.length);

    List<SequenceFeature> features = new ArrayList<>();
    SequenceFeature sf = new SequenceFeature("type", "desc", 1, 20, null);
    features.add(sf);
    linkMenu = PopupMenu.buildLinkMenu(null, features);
    linkItems = linkMenu.getMenuComponents();
    assertEquals(0, linkItems.length); // feature has no links

    sf.addLink("Pfam family|http://pfam.xfam.org/family/PF00111");
    linkMenu = PopupMenu.buildLinkMenu(null, features);
    linkItems = linkMenu.getMenuComponents();
    assertEquals(1, linkItems.length);
    JMenuItem item = (JMenuItem) linkItems[0];
    assertEquals("Pfam family", item.getText());
    // ? no way to verify URL, compiled into link's actionListener
  }

  @Test(groups = { "Functional" })
  public void testHideInsertions()
  {
    // get sequences from the alignment
    List<SequenceI> seqs = parentPanel.getAlignment().getSequences();

    // add our own seqs to avoid problems with changes to existing sequences
    // (gap at end of sequences varies depending on how tests are run!)
    Sequence seqGap1 = new Sequence("GappySeq",
            "AAAA----AA-AAAAAAA---AAA-----------AAAAAAAAAA--");
    seqGap1.createDatasetSequence();
    seqs.add(seqGap1);
    Sequence seqGap2 = new Sequence("LessGappySeq",
            "AAAAAA-AAAAA---AAA--AAAAA--AAAAAAA-AAAAAA");
    seqGap2.createDatasetSequence();
    seqs.add(seqGap2);
    Sequence seqGap3 = new Sequence("AnotherGapSeq",
            "AAAAAA-AAAAAA--AAAAAA-AAAAAAAAAAA---AAAAAAAA");
    seqGap3.createDatasetSequence();
    seqs.add(seqGap3);
    Sequence seqGap4 = new Sequence("NoGaps",
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    seqGap4.createDatasetSequence();
    seqs.add(seqGap4);

    ColumnSelection sel = new ColumnSelection();
    parentPanel.av.getAlignment().getHiddenColumns()
            .revealAllHiddenColumns(sel);

    // get the Popup Menu for 7th sequence - no insertions
    testee = new PopupMenu(parentPanel, seqs.get(7), null);
    testee.hideInsertions_actionPerformed(null);

    HiddenColumns hidden = parentPanel.av.getAlignment().getHiddenColumns();
    Iterator<int[]> it = hidden.iterator();
    assertFalse(it.hasNext());

    // get the Popup Menu for GappySeq - this time we have insertions
    testee = new PopupMenu(parentPanel, seqs.get(4), null);
    testee.hideInsertions_actionPerformed(null);
    hidden = parentPanel.av.getAlignment().getHiddenColumns();
    it = hidden.iterator();

    assertTrue(it.hasNext());
    int[] region = it.next();
    assertEquals(region[0], 4);
    assertEquals(region[1], 7);

    assertTrue(it.hasNext());
    region = it.next();
    assertEquals(region[0], 10);
    assertEquals(region[1], 10);

    assertTrue(it.hasNext());
    region = it.next();
    assertEquals(region[0], 18);
    assertEquals(region[1], 20);

    assertTrue(it.hasNext());
    region = it.next();
    assertEquals(region[0], 24);
    assertEquals(region[1], 34);

    assertTrue(it.hasNext());
    region = it.next();
    assertEquals(region[0], 45);
    assertEquals(region[1], 46);

    assertFalse(it.hasNext());

    sel = new ColumnSelection();
    hidden.revealAllHiddenColumns(sel);

    // make a sequence group and hide insertions within the group
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(8);
    sg.setEndRes(42);
    sg.addSequence(seqGap2, false);
    sg.addSequence(seqGap3, false);
    parentPanel.av.setSelectionGroup(sg);

    // hide columns outside and within selection
    // only hidden columns outside the collection will be retained (unless also
    // gaps in the selection)
    hidden.hideColumns(1, 10);
    hidden.hideColumns(31, 40);

    // get the Popup Menu for LessGappySeq in the sequence group
    testee = new PopupMenu(parentPanel, seqs.get(5), null);
    testee.hideInsertions_actionPerformed(null);
    hidden = parentPanel.av.getAlignment().getHiddenColumns();
    it = hidden.iterator();

    assertTrue(it.hasNext());
    region = it.next();
    assertEquals(region[0], 1);
    assertEquals(region[1], 7);

    assertTrue(it.hasNext());
    region = it.next();
    assertEquals(region[0], 13);
    assertEquals(region[1], 14);

    assertTrue(it.hasNext());
    region = it.next();
    assertEquals(region[0], 34);
    assertEquals(region[1], 34);
  }

  @Test(groups = { "Functional" })
  public void testAddFeatureDetails()
  {
    String menuText = MessageManager.getString("label.feature_details");

    /*
     * with no features, sub-menu should not be created
     */
    List<SequenceFeature> features = new ArrayList<>();
    SequenceI seq = this.alignment.getSequenceAt(0); // FER_CAPAA/1-12
    testee.addFeatureDetails(features, seq, 10);
    JMenu menu = findMenu(testee, menuText);
    assertNull(menu);

    /*
     * add some features; the menu item text is wrapped in html, and includes
     * feature type, position, description, group (if not null)
     */
    SequenceFeature sf1 = new SequenceFeature("helix", "curly", 2, 6, null);
    SequenceFeature sf2 = new SequenceFeature("chain", "straight", 1, 1,
            "uniprot");
    features.add(sf1);
    features.add(sf2);
    testee.addFeatureDetails(features, seq, 10);
    menu = findMenu(testee, menuText);
    assertNotNull(menu);
    assertEquals(2, menu.getItemCount());
    JMenuItem item = menu.getItem(0);
    assertEquals("<html>helix 2-6 curly</html>", item.getText());
    item = menu.getItem(1);
    assertEquals("<html>chain 1 straight (uniprot)</html>", item.getText());

    /*
     * long feature descriptions are truncated to 40 characters
     */
    sf1.setDescription("this is a quite extraordinarily long description");
    testee.remove(menu); // don't create the sub-menu twice
    testee.addFeatureDetails(features, seq, 10);
    menu = findMenu(testee, menuText);
    item = menu.getItem(0);
    assertEquals(
            "<html>helix 2-6 this is a quite extraordinarily long des...</html>",
            item.getText());
  }

  /**
   * Returns the first component which is a JMenu with the given text
   * 
   * @param c
   * @param text
   * @return
   */
  private JMenu findMenu(Container c, String text)
  {
    for (int i = 0; i < c.getComponentCount(); i++)
    {
      Component comp = c.getComponent(i);
      if ((comp instanceof JMenu) && ((JMenu) comp).getText().equals(text))
      {
        return (JMenu) comp;
      }
    }
    return null;
  }

  @Test(groups = { "Functional" })
  public void testAddFeatureDetails_linkedFeatures()
  {
    // todo tests that verify menu items for complement features
  }
}
