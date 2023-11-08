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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import jalview.api.FeatureRenderer;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceI;
import jalview.io.AnnotationFile;
import jalview.io.FeaturesFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.util.MessageManager;

/**
 * 
 * GUI dialog for exporting features or alignment annotations depending upon
 * which method is called.
 * 
 * @author AMW
 * 
 */
public class AnnotationExporter extends JPanel
{
  private JInternalFrame frame;

  private AlignmentPanel ap;

  /*
   * true if exporting features, false if exporting annotations
   */
  private boolean exportFeatures = true;

  private AlignmentAnnotation[] annotations;

  private boolean wholeView;

  /*
   * option to export linked (CDS/peptide) features when shown 
   * on the alignment, converted to this alignment's coordinates
   */
  private JCheckBox includeLinkedFeatures;

  /*
   * output format option shown for feature export
   */
  JRadioButton GFFFormat = new JRadioButton();

  /*
   * output format option shown for annotation export
   */
  JRadioButton CSVFormat = new JRadioButton();

  private JPanel linkedFeaturesPanel;

  /**
   * Constructor
   * 
   * @param panel
   */
  public AnnotationExporter(AlignmentPanel panel)
  {
    this.ap = panel;
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Dimension preferredSize = frame.getPreferredSize();
    Desktop.addInternalFrame(frame, "", true, preferredSize.width,
            preferredSize.height, true, true);
  }

  /**
   * Configures the dialog for options to export visible features. If from a
   * split frame panel showing linked features, make the option to include these
   * in the export visible.
   */
  public void exportFeatures()
  {
    exportFeatures = true;
    CSVFormat.setVisible(false);
    if (ap.av.isShowComplementFeatures())
    {
      linkedFeaturesPanel.setVisible(true);
      frame.pack();
    }
    frame.setTitle(MessageManager.getString("label.export_features"));
  }

  /**
   * Configures the dialog for options to export all visible annotations
   */
  public void exportAnnotations()
  {
    boolean showAnnotation = ap.av.isShowAnnotation();
    exportAnnotation(showAnnotation ? null
            : ap.av.getAlignment().getAlignmentAnnotation(), true);
  }

  /**
   * Configures the dialog for options to export the given annotation row
   * 
   * @param toExport
   */
  public void exportAnnotation(AlignmentAnnotation toExport)
  {
    exportAnnotation(new AlignmentAnnotation[] { toExport }, false);
  }

  private void exportAnnotation(AlignmentAnnotation[] toExport,
          boolean forWholeView)
  {
    wholeView = forWholeView;
    annotations = toExport;
    exportFeatures = false;
    GFFFormat.setVisible(false);
    CSVFormat.setVisible(true);
    frame.setTitle(MessageManager.getString("label.export_annotations"));
  }

  private void toFile_actionPerformed()
  {
    // TODO: JAL-3048 JalviewFileChooser - Save option
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(exportFeatures
            ? MessageManager.getString("label.save_features_to_file")
            : MessageManager.getString("label.save_annotation_to_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String text = getText();

      try
      {
        PrintWriter out = new PrintWriter(
                new FileWriter(chooser.getSelectedFile()));
        out.print(text);
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    close_actionPerformed();
  }

  /**
   * Answers the text to output for either Features (in GFF or Jalview format)
   * or Annotations (in CSV or Jalview format)
   * 
   * @return
   */
  private String getText()
  {
    return exportFeatures ? getFeaturesText() : getAnnotationsText();
  }

  /**
   * Returns the text contents for output of annotations in either CSV or
   * Jalview format
   * 
   * @return
   */
  private String getAnnotationsText()
  {
    String text;
    if (CSVFormat.isSelected())
    {
      text = new AnnotationFile().printCSVAnnotations(annotations);
    }
    else
    {
      if (wholeView)
      {
        text = new AnnotationFile().printAnnotationsForView(ap.av);
      }
      else
      {
        text = new AnnotationFile().printAnnotations(annotations, null,
                null);
      }
    }
    return text;
  }

  /**
   * Returns the text contents for output of features in either GFF or Jalview
   * format
   * 
   * @return
   */
  private String getFeaturesText()
  {
    String text;
    SequenceI[] sequences = ap.av.getAlignment().getSequencesArray();
    boolean includeNonPositional = ap.av.isShowNPFeats();

    FeaturesFile formatter = new FeaturesFile();
    final FeatureRenderer fr = ap.getFeatureRenderer();
    boolean includeComplement = includeLinkedFeatures.isSelected();

    if (GFFFormat.isSelected())
    {
      text = formatter.printGffFormat(sequences, fr, includeNonPositional,
              includeComplement);
    }
    else
    {
      text = formatter.printJalviewFormat(sequences, fr,
              includeNonPositional, includeComplement);
    }
    return text;
  }

  private void toTextbox_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();

    try
    {
      String text = getText();
      cap.setText(text);
      Desktop.addInternalFrame(cap, (exportFeatures ? MessageManager
              .formatMessage("label.features_for_params", new String[]
              { ap.alignFrame.getTitle() })
              : MessageManager.formatMessage("label.annotations_for_params",
                      new String[]
                      { ap.alignFrame.getTitle() })),
              600, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning((exportFeatures ? MessageManager.formatMessage(
              "label.generating_features_for_params", new String[]
              { ap.alignFrame.getTitle() })
              : MessageManager.formatMessage(
                      "label.generating_annotations_for_params",
                      new String[]
                      { ap.alignFrame.getTitle() })),
              oom);
      cap.dispose();
    }

    close_actionPerformed();
  }

  private void close_actionPerformed()
  {
    try
    {
      frame.setClosed(true);
    } catch (java.beans.PropertyVetoException ex)
    {
    }
  }

  /**
   * Adds widgets to the panel
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setBackground(Color.white);

    JPanel formatPanel = buildFormatOptionsPanel();
    JPanel linkedFeatures = buildLinkedFeaturesPanel();
    JPanel actionsPanel = buildActionsPanel();

    this.add(formatPanel);
    this.add(linkedFeatures);
    this.add(actionsPanel);
  }

  /**
   * Builds a panel with a checkbox for the option to export linked
   * (CDS/peptide) features. This is hidden by default, and only made visible if
   * exporting features from a split frame panel which is configured to show
   * linked features.
   * 
   * @return
   */
  private JPanel buildLinkedFeaturesPanel()
  {
    linkedFeaturesPanel = new JPanel();
    linkedFeaturesPanel.setOpaque(false);

    boolean nucleotide = ap.av.isNucleotide();
    String complement = nucleotide ? MessageManager
            .getString("label.protein").toLowerCase(Locale.ROOT) : "CDS";
    JLabel label = new JLabel(MessageManager
            .formatMessage("label.include_linked_features", complement));
    label.setHorizontalAlignment(SwingConstants.TRAILING);
    String tooltip = MessageManager
            .formatMessage("label.include_linked_tooltip", complement);
    label.setToolTipText(JvSwingUtils.wrapTooltip(true, tooltip));

    includeLinkedFeatures = new JCheckBox();
    linkedFeaturesPanel.add(label);
    linkedFeaturesPanel.add(includeLinkedFeatures);
    linkedFeaturesPanel.setVisible(false);

    return linkedFeaturesPanel;
  }

  /**
   * Builds the panel with to File or Textbox or Close actions
   * 
   * @return
   */
  JPanel buildActionsPanel()
  {
    JPanel actionsPanel = new JPanel();
    actionsPanel.setOpaque(false);

    JButton toFile = new JButton(MessageManager.getString("label.to_file"));
    toFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        toFile_actionPerformed();
      }
    });
    JButton toTextbox = new JButton(
            MessageManager.getString("label.to_textbox"));
    toTextbox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        toTextbox_actionPerformed();
      }
    });
    JButton close = new JButton(MessageManager.getString("action.close"));
    close.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed();
      }
    });

    actionsPanel.add(toFile);
    actionsPanel.add(toTextbox);
    actionsPanel.add(close);

    return actionsPanel;
  }

  /**
   * Builds the panel with options to output in Jalview, GFF or CSV format. GFF
   * is only made visible when exporting features, CSV only when exporting
   * annotation.
   * 
   * @return
   */
  JPanel buildFormatOptionsPanel()
  {
    JPanel formatPanel = new JPanel();
    // formatPanel.setBorder(BorderFactory.createEtchedBorder());
    formatPanel.setOpaque(false);

    JRadioButton jalviewFormat = new JRadioButton("Jalview");
    jalviewFormat.setOpaque(false);
    jalviewFormat.setSelected(true);
    GFFFormat.setOpaque(false);
    GFFFormat.setText("GFF");
    CSVFormat.setOpaque(false);
    CSVFormat.setText(MessageManager.getString("label.csv_spreadsheet"));

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(jalviewFormat);
    buttonGroup.add(GFFFormat);
    buttonGroup.add(CSVFormat);

    JLabel format = new JLabel(
            MessageManager.getString("action.format") + " ");
    format.setHorizontalAlignment(SwingConstants.TRAILING);

    formatPanel.add(format);
    formatPanel.add(jalviewFormat);
    formatPanel.add(GFFFormat);
    formatPanel.add(CSVFormat);

    return formatPanel;
  }
}
