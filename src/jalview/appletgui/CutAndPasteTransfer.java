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

import jalview.analysis.AlignmentUtils;
import jalview.api.ComplexAlignFile;
import jalview.api.FeaturesSourceI;
import jalview.bin.JalviewLite;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.AlignmentFileReaderI;
import jalview.io.AnnotationFile;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.FileFormatI;
import jalview.io.IdentifyFile;
import jalview.io.NewickFile;
import jalview.io.TCoffeeScoreFile;
import jalview.json.binding.biojson.v1.ColourSchemeMapper;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.TCoffeeColourScheme;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class CutAndPasteTransfer extends Panel
        implements ActionListener, MouseListener
{
  boolean pdbImport = false;

  boolean treeImport = false;

  boolean annotationImport = false;

  SequenceI seq;

  AlignFrame alignFrame;

  AlignmentFileReaderI source = null;

  public CutAndPasteTransfer(boolean forImport, AlignFrame alignFrame)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    this.alignFrame = alignFrame;

    if (!forImport)
    {
      buttonPanel.setVisible(false);
    }
  }

  public String getText()
  {
    return textarea.getText();
  }

  public void setText(String text)
  {
    textarea.setText(text);
  }

  public void setPDBImport(SequenceI seq)
  {
    this.seq = seq;
    accept.setLabel(MessageManager.getString("action.accept"));
    addSequences.setVisible(false);
    pdbImport = true;
  }

  public void setTreeImport()
  {
    treeImport = true;
    accept.setLabel(MessageManager.getString("action.accept"));
    addSequences.setVisible(false);
  }

  public void setAnnotationImport()
  {
    annotationImport = true;
    accept.setLabel(MessageManager.getString("action.accept"));
    addSequences.setVisible(false);
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == accept)
    {
      ok(true);
    }
    else if (evt.getSource() == addSequences)
    {
      ok(false);
    }
    else if (evt.getSource() == cancel)
    {
      cancel();
    }
  }

  protected void ok(boolean newWindow)
  {
    String text = getText();
    int length = text.length();
    textarea.append("\n");
    if (textarea.getText().length() == length)
    {
      String warning = "\n\n#################################################\n"
              + "WARNING!! THIS IS THE MAXIMUM SIZE OF TEXTAREA!!\n"
              + "\nCAN'T INPUT FULL ALIGNMENT"
              + "\n\nYOU MUST DELETE THIS WARNING TO CONTINUE"
              + "\n\nMAKE SURE LAST SEQUENCE PASTED IS COMPLETE"
              + "\n#################################################\n";
      textarea.setText(text.substring(0, text.length() - warning.length())
              + warning);

      textarea.setCaretPosition(text.length());
    }

    if (pdbImport)
    {
      openPdbViewer(text);

    }
    else if (treeImport)
    {
      if (!loadTree())
      {
        return;
      }
    }
    else if (annotationImport)
    {
      loadAnnotations();
    }
    else if (alignFrame != null)
    {
      loadAlignment(text, newWindow, alignFrame.getAlignViewport());
    }

    // TODO: dialog should indicate if data was parsed correctly or not - see
    // JAL-1102
    if (this.getParent() instanceof Frame)
    {
      ((Frame) this.getParent()).setVisible(false);
    }
    else
    {
      ((Dialog) this.getParent()).setVisible(false);
    }
  }

  /**
   * Parses text as Newick Tree format, and loads on to the alignment. Returns
   * true if successful, else false.
   */
  protected boolean loadTree()
  {
    try
    {
      NewickFile fin = new NewickFile(textarea.getText(),
              DataSourceType.PASTE);

      fin.parse();
      if (fin.getTree() != null)
      {
        alignFrame.loadTree(fin, "Pasted tree file");
        return true;
      }
    } catch (Exception ex)
    {
      // TODO: JAL-1102 - should have a warning message in dialog, not simply
      // overwrite the broken input data with the exception
      textarea.setText(MessageManager.formatMessage(
              "label.could_not_parse_newick_file", new Object[]
              { ex.getMessage() }));
      return false;
    }
    return false;
  }

  /**
   * Parse text as an alignment file and add to the current or a new window.
   * 
   * @param text
   * @param newWindow
   */
  protected void loadAlignment(String text, boolean newWindow,
          AlignViewport viewport)
  {
    AlignmentI al = null;

    try
    {
      FileFormatI format = new IdentifyFile().identify(text,
              DataSourceType.PASTE);
      AppletFormatAdapter afa = new AppletFormatAdapter(
              alignFrame.alignPanel);
      al = afa.readFile(text, DataSourceType.PASTE, format);
      source = afa.getAlignFile();

      if (al != null)
      {
        al.setDataset(null); // set dataset on alignment/sequences

        /*
         * SplitFrame option dependent on applet parameter for now.
         */
        boolean allowSplitFrame = alignFrame.viewport.applet
                .getDefaultParameter("enableSplitFrame", false);
        if (allowSplitFrame && openSplitFrame(al, format))
        {
          return;
        }
        if (newWindow)
        {
          AlignFrame af;

          if (source instanceof ComplexAlignFile)
          {
            HiddenColumns colSel = ((ComplexAlignFile) source)
                    .getHiddenColumns();
            SequenceI[] hiddenSeqs = ((ComplexAlignFile) source)
                    .getHiddenSequences();
            boolean showSeqFeatures = ((ComplexAlignFile) source)
                    .isShowSeqFeatures();
            String colourSchemeName = ((ComplexAlignFile) source)
                    .getGlobalColourScheme();
            af = new AlignFrame(al, hiddenSeqs, colSel,
                    alignFrame.viewport.applet,
                    "Cut & Paste input - " + format, false);
            af.getAlignViewport().setShowSequenceFeatures(showSeqFeatures);
            ColourSchemeI cs = ColourSchemeMapper
                    .getJalviewColourScheme(colourSchemeName, al);
            if (cs != null)
            {
              af.changeColour(cs);
            }
          }
          else
          {
            af = new AlignFrame(al, alignFrame.viewport.applet,
                    "Cut & Paste input - " + format, false);
            if (source instanceof FeaturesSourceI)
            {
              af.getAlignViewport().setShowSequenceFeatures(true);
            }
          }

          af.statusBar.setText(MessageManager.getString(
                  "label.successfully_pasted_annotation_to_alignment"));
        }
        else
        {
          alignFrame.addSequences(al.getSequencesArray());
          alignFrame.statusBar.setText(MessageManager
                  .getString("label.successfully_pasted_alignment_file"));
        }
      }
    } catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Check whether the new alignment could be mapped to the current one as
   * cDNA/protein, if so offer the option to open as split frame view. Returns
   * true if a split frame view is opened, false if not.
   * 
   * @param al
   * @return
   */
  protected boolean openSplitFrame(AlignmentI al, FileFormatI format)
  {
    final AlignmentI thisAlignment = this.alignFrame.getAlignViewport()
            .getAlignment();
    if (thisAlignment.isNucleotide() == al.isNucleotide())
    {
      // both nucleotide or both protein
      return false;
    }
    AlignmentI protein = thisAlignment.isNucleotide() ? al : thisAlignment;
    AlignmentI dna = thisAlignment.isNucleotide() ? thisAlignment : al;
    boolean mapped = AlignmentUtils.mapProteinAlignmentToCdna(protein, dna);
    if (!mapped)
    {
      return false;
    }

    /*
     * A mapping is possible; ask user if they want a split frame.
     */
    String title = MessageManager.getString("label.open_split_window");
    final JVDialog dialog = new JVDialog((Frame) this.getParent(), title,
            true, 100, 400);
    dialog.ok.setLabel(MessageManager.getString("action.yes"));
    dialog.cancel.setLabel(MessageManager.getString("action.no"));
    Panel question = new Panel(new BorderLayout());
    final String text = MessageManager
            .getString("label.open_split_window?");
    question.add(new Label(text, Label.CENTER), BorderLayout.CENTER);
    dialog.setMainPanel(question);
    dialog.setVisible(true);
    dialog.toFront();

    if (!dialog.accept)
    {
      return false;
    }

    /*
     * 'align' the added alignment to match the current one
     */
    al.alignAs(thisAlignment);

    /*
     * Open SplitFrame with DNA above and protein below, including the alignment
     * from textbox and a copy of the original.
     */
    final JalviewLite applet = this.alignFrame.viewport.applet;
    AlignFrame copyFrame = new AlignFrame(
            this.alignFrame.viewport.getAlignment(), applet,
            alignFrame.getTitle(), false, false);
    AlignFrame newFrame = new AlignFrame(al, alignFrame.viewport.applet,
            "Cut & Paste input - " + format, false, false);
    AlignFrame dnaFrame = al.isNucleotide() ? newFrame : copyFrame;
    AlignFrame proteinFrame = al.isNucleotide() ? copyFrame : newFrame;
    SplitFrame sf = new SplitFrame(dnaFrame, proteinFrame);
    sf.addToDisplay(false, applet);
    return true;
  }

  /**
   * Parse the text as a TCoffee score file, if successful add scores as
   * alignment annotations.
   */
  protected void loadAnnotations()
  {
    TCoffeeScoreFile tcf = null;
    try
    {
      tcf = new TCoffeeScoreFile(textarea.getText(),
              jalview.io.DataSourceType.PASTE);
      if (tcf.isValid())
      {
        if (tcf.annotateAlignment(alignFrame.viewport.getAlignment(), true))
        {
          alignFrame.tcoffeeColour.setEnabled(true);
          alignFrame.alignPanel.fontChanged();
          alignFrame.changeColour(new TCoffeeColourScheme(
                  alignFrame.viewport.getAlignment()));
          alignFrame.statusBar.setText(MessageManager.getString(
                  "label.successfully_pasted_tcoffee_scores_to_alignment"));
        }
        else
        {
          // file valid but didn't get added to alignment for some reason
          alignFrame.statusBar.setText(MessageManager.formatMessage(
                  "label.failed_add_tcoffee_scores", new Object[]
                  { (tcf.getWarningMessage() != null
                          ? tcf.getWarningMessage()
                          : "") }));
        }
      }
      else
      {
        tcf = null;
      }
    } catch (Exception x)
    {
      tcf = null;
    }
    if (tcf == null)
    {
      if (new AnnotationFile().annotateAlignmentView(alignFrame.viewport,
              textarea.getText(), jalview.io.DataSourceType.PASTE))
      {
        alignFrame.alignPanel.fontChanged();
        alignFrame.alignPanel.setScrollValues(0, 0);
        alignFrame.statusBar.setText(MessageManager.getString(
                "label.successfully_pasted_annotation_to_alignment"));

      }
      else
      {
        if (!alignFrame.parseFeaturesFile(textarea.getText(),
                jalview.io.DataSourceType.PASTE))
        {
          alignFrame.statusBar.setText(MessageManager.getString(
                  "label.couldnt_parse_pasted_text_as_valid_annotation_feature_GFF_tcoffee_file"));
        }
      }
    }
  }

  /**
   * Open a Jmol viewer (if available), failing that the built-in PDB viewer,
   * passing the input text as the PDB file data.
   * 
   * @param text
   */
  protected void openPdbViewer(String text)
  {
    PDBEntry pdb = new PDBEntry();
    pdb.setFile(text);

    if (alignFrame.alignPanel.av.applet.jmolAvailable)
    {
      new jalview.appletgui.AppletJmol(pdb, new SequenceI[] { seq }, null,
              alignFrame.alignPanel, DataSourceType.PASTE);
    }
    else
    {
      new mc_view.AppletPDBViewer(pdb, new SequenceI[] { seq }, null,
              alignFrame.alignPanel, DataSourceType.PASTE);
    }
  }

  protected void cancel()
  {
    textarea.setText("");
    if (this.getParent() instanceof Frame)
    {
      ((Frame) this.getParent()).setVisible(false);
    }
    else
    {
      ((Dialog) this.getParent()).setVisible(false);
    }
  }

  protected TextArea textarea = new TextArea();

  Button accept = new Button("New Window");

  Button addSequences = new Button("Add to Current Alignment");

  Button cancel = new Button("Close");

  protected Panel buttonPanel = new Panel();

  BorderLayout borderLayout1 = new BorderLayout();

  private void jbInit() throws Exception
  {
    textarea.setFont(new java.awt.Font("Monospaced", Font.PLAIN, 10));
    textarea.setText(
            MessageManager.getString("label.paste_your_alignment_file"));
    textarea.addMouseListener(this);
    this.setLayout(borderLayout1);
    accept.addActionListener(this);
    addSequences.addActionListener(this);
    cancel.addActionListener(this);
    this.add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(accept, null);
    buttonPanel.add(addSequences);
    buttonPanel.add(cancel, null);
    this.add(textarea, java.awt.BorderLayout.CENTER);
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    if (textarea.getText()
            .startsWith(MessageManager.getString("label.paste_your")))
    {
      textarea.setText("");
    }
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
  }
}
