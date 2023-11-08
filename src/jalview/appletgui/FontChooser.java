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

import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This dialog allows the user to try different font settings and related
 * options. Changes are immediately visible on the alignment or tree. The user
 * can dismiss the dialog by confirming changes with 'OK', or reverting to
 * previous settings with 'Cancel'.
 */
@SuppressWarnings("serial")
public class FontChooser extends Panel implements ItemListener
{
  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);

  private Choice fontSize = new Choice();

  private Choice fontStyle = new Choice();

  private Choice fontName = new Choice();

  private Checkbox scaleAsCdna = new Checkbox();

  private Checkbox fontAsCdna = new Checkbox();

  private Button ok = new Button();

  private Button cancel = new Button();

  private AlignmentPanel ap;

  private TreePanel tp;

  private Font oldFont;

  private Font oldComplementFont;

  private int oldCharWidth = 0;

  /*
   * the state of 'scale protein to cDNA' on opening the dialog
   */
  private boolean oldScaleProtein = false;

  /*
   * the state of 'same font for protein and cDNA' on opening the dialog
   */
  boolean oldMirrorFont;

  private Font lastSelected = null;

  private int lastSelStyle = 0;

  private int lastSelSize = 0;

  private boolean init = true;

  private Frame frame;

  boolean inSplitFrame = false;

  /**
   * Constructor for a TreePanel font chooser
   * 
   * @param tp
   */
  public FontChooser(TreePanel tp)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    this.tp = tp;
    oldFont = tp.getTreeFont();
    init();
  }

  /**
   * Constructor for an AlignmentPanel font chooser
   * 
   * @param ap
   */
  public FontChooser(AlignmentPanel ap)
  {
    this.ap = ap;
    oldFont = ap.av.getFont();
    oldCharWidth = ap.av.getCharWidth();
    oldScaleProtein = ap.av.isScaleProteinAsCdna();
    oldMirrorFont = ap.av.isProteinFontAsCdna();

    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    init();
  }

  /**
   * Populate choice lists and open this dialog
   */
  void init()
  {
    // String fonts[] = Toolkit.getDefaultToolkit().getFontList();
    String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
    for (int i = 0; i < fonts.length; i++)
    {
      fontName.addItem(fonts[i]);
    }

    for (int i = 1; i < 31; i++)
    {
      fontSize.addItem(i + "");
    }

    fontStyle.addItem("plain");
    fontStyle.addItem("bold");
    fontStyle.addItem("italic");

    fontName.select(oldFont.getName());
    fontSize.select(oldFont.getSize() + "");
    fontStyle.select(oldFont.getStyle());

    this.frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("action.change_font"), 440, 145);

    init = false;
  }

  /**
   * Actions on change of font name, size or style.
   */
  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    final Object source = evt.getSource();
    if (source == fontName)
    {
      fontName_actionPerformed();
    }
    else if (source == fontSize)
    {
      fontSize_actionPerformed();
    }
    else if (source == fontStyle)
    {
      fontStyle_actionPerformed();
    }
    else if (source == scaleAsCdna)
    {
      scaleAsCdna_actionPerformed();
    }
    else if (source == fontAsCdna)
    {
      mirrorFont_actionPerformed();
    }
  }

  /**
   * Action on checking or unchecking 'use same font across split screen'
   * option. When checked, the font settings are copied to the other half of the
   * split screen. When unchecked, the other half is restored to its initial
   * settings.
   */
  protected void mirrorFont_actionPerformed()
  {
    boolean selected = fontAsCdna.getState();
    ap.av.setProteinFontAsCdna(selected);
    ap.av.getCodingComplement().setProteinFontAsCdna(selected);

    if (!selected)
    {
      ap.av.getCodingComplement().setFont(oldComplementFont, true);
    }
    changeFont();
  }

  /**
   * Close this dialog on OK to confirm any changes. Also updates the overview
   * window if displayed.
   */
  protected void ok_actionPerformed()
  {
    frame.setVisible(false);
    if (ap != null)
    {
      if (ap.getOverviewPanel() != null)
      {
        ap.getOverviewPanel().updateOverviewImage();
      }
    }
  }

  /**
   * Close this dialog on Cancel, reverting to previous font settings.
   */
  protected void cancel_actionPerformed()
  {
    if (ap != null)
    {
      ap.av.setScaleProteinAsCdna(oldScaleProtein);
      ap.av.setProteinFontAsCdna(oldMirrorFont);

      if (ap.av.getCodingComplement() != null)
      {
        ap.av.getCodingComplement().setScaleProteinAsCdna(oldScaleProtein);
        ap.av.getCodingComplement().setProteinFontAsCdna(oldMirrorFont);
        ap.av.getCodingComplement().setFont(oldComplementFont, true);
        SplitFrame splitFrame = ap.alignFrame.getSplitFrame();
        splitFrame.adjustLayout();
        splitFrame.getComplement(ap.alignFrame).alignPanel.fontChanged();
        splitFrame.repaint();
      }

      ap.av.setFont(oldFont, true);
      if (ap.av.getCharWidth() != oldCharWidth)
      {
        ap.av.setCharWidth(oldCharWidth);
      }
      ap.paintAlignment(true, false);
    }
    else if (tp != null)
    {
      tp.setTreeFont(oldFont);
      tp.treeCanvas.repaint();
    }

    fontName.select(oldFont.getName());
    fontSize.select(oldFont.getSize() + "");
    fontStyle.select(oldFont.getStyle());

    frame.setVisible(false);
  }

  /**
   * DOCUMENT ME!
   */
  void changeFont()
  {
    if (lastSelected == null)
    {
      // initialise with original font
      lastSelected = oldFont;
      lastSelSize = oldFont.getSize();
      lastSelStyle = oldFont.getStyle();
    }

    Font newFont = new Font(fontName.getSelectedItem().toString(),
            fontStyle.getSelectedIndex(),
            Integer.parseInt(fontSize.getSelectedItem().toString()));
    FontMetrics fm = getGraphics().getFontMetrics(newFont);
    double mw = fm.getStringBounds("M", getGraphics()).getWidth(),
            iw = fm.getStringBounds("I", getGraphics()).getWidth();
    if (mw < 1 || iw < 1)
    {
      // TODO: JAL-1100
      fontName.select(lastSelected.getName());
      fontStyle.select(lastSelStyle);
      fontSize.select("" + lastSelSize);
      JVDialog d = new JVDialog(this.frame,
              MessageManager.getString("label.invalid_font"), true, 350,
              200);
      Panel mp = new Panel();
      d.cancel.setVisible(false);
      mp.setLayout(new FlowLayout());
      mp.add(new Label(
              "Font doesn't have letters defined\nso cannot be used\nwith alignment data."));
      d.setMainPanel(mp);
      d.setVisible(true);
      return;
    }
    if (tp != null)
    {
      tp.setTreeFont(newFont);
    }
    else if (ap != null)
    {
      ap.av.setFont(newFont, true);
      ap.fontChanged();

      /*
       * and change font in other half of split frame if any
       */
      if (inSplitFrame)
      {
        if (fontAsCdna.getState())
        {
          ap.av.getCodingComplement().setFont(newFont, true);
        }
        SplitFrame splitFrame = ap.alignFrame.getSplitFrame();
        splitFrame.adjustLayout();
        splitFrame.getComplement(ap.alignFrame).alignPanel.fontChanged();
        splitFrame.repaint();
      }
    }
    // remember last selected
    lastSelected = newFont;
  }

  protected void fontName_actionPerformed()
  {
    if (init)
    {
      return;
    }
    changeFont();
  }

  protected void fontSize_actionPerformed()
  {
    if (init)
    {
      return;
    }
    changeFont();
  }

  protected void fontStyle_actionPerformed()
  {
    if (init)
    {
      return;
    }
    changeFont();
  }

  /**
   * Construct this panel's contents
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    this.setLayout(new BorderLayout());
    this.setBackground(Color.white);

    Label fontLabel = new Label(MessageManager.getString("label.font"));
    fontLabel.setFont(VERDANA_11PT);
    fontLabel.setAlignment(Label.RIGHT);
    fontSize.setFont(VERDANA_11PT);
    fontSize.addItemListener(this);
    fontStyle.setFont(VERDANA_11PT);
    fontStyle.addItemListener(this);

    Label sizeLabel = new Label(MessageManager.getString("label.size"));
    sizeLabel.setAlignment(Label.RIGHT);
    sizeLabel.setFont(VERDANA_11PT);

    Label styleLabel = new Label(MessageManager.getString("label.style"));
    styleLabel.setAlignment(Label.RIGHT);
    styleLabel.setFont(VERDANA_11PT);

    fontName.setFont(VERDANA_11PT);
    fontName.addItemListener(this);

    scaleAsCdna.setLabel(MessageManager.getString("label.scale_as_cdna"));
    scaleAsCdna.setFont(VERDANA_11PT);
    scaleAsCdna.addItemListener(this);
    scaleAsCdna.setState(ap.av.isScaleProteinAsCdna());

    fontAsCdna.setLabel(MessageManager.getString("label.font_as_cdna"));
    fontAsCdna.setFont(VERDANA_11PT);
    fontAsCdna.addItemListener(this);
    fontAsCdna.setState(ap.av.isProteinFontAsCdna());

    ok.setFont(VERDANA_11PT);
    ok.setLabel(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed();
      }
    });
    cancel.setFont(VERDANA_11PT);
    cancel.setLabel(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed();
      }
    });

    Panel fontPanel = new Panel();
    fontPanel.setLayout(new BorderLayout());
    Panel stylePanel = new Panel();
    stylePanel.setLayout(new BorderLayout());
    Panel sizePanel = new Panel();
    sizePanel.setLayout(new BorderLayout());
    Panel scalePanel = new Panel();
    scalePanel.setLayout(new BorderLayout());
    Panel okCancelPanel = new Panel();
    Panel optionsPanel = new Panel();

    fontPanel.setBackground(Color.white);
    stylePanel.setBackground(Color.white);
    sizePanel.setBackground(Color.white);
    okCancelPanel.setBackground(Color.white);
    optionsPanel.setBackground(Color.white);

    fontPanel.add(fontLabel, BorderLayout.WEST);
    fontPanel.add(fontName, BorderLayout.CENTER);
    stylePanel.add(styleLabel, BorderLayout.WEST);
    stylePanel.add(fontStyle, BorderLayout.CENTER);
    sizePanel.add(sizeLabel, BorderLayout.WEST);
    sizePanel.add(fontSize, BorderLayout.CENTER);
    scalePanel.add(scaleAsCdna, BorderLayout.NORTH);
    scalePanel.add(fontAsCdna, BorderLayout.SOUTH);
    okCancelPanel.add(ok, null);
    okCancelPanel.add(cancel, null);

    optionsPanel.add(fontPanel, null);
    optionsPanel.add(sizePanel, null);
    optionsPanel.add(stylePanel, null);

    /*
     * Only show 'scale protein as cDNA' in a SplitFrame
     */
    this.add(optionsPanel, BorderLayout.NORTH);
    if (ap.alignFrame.getSplitFrame() != null)
    {
      inSplitFrame = true;
      oldComplementFont = ((AlignViewport) ap.av.getCodingComplement())
              .getFont();
      this.add(scalePanel, BorderLayout.CENTER);
    }
    this.add(okCancelPanel, BorderLayout.SOUTH);
  }

  /**
   * Turn on/off scaling of protein characters to 3 times the width of cDNA
   * characters
   */
  protected void scaleAsCdna_actionPerformed()
  {
    ap.av.setScaleProteinAsCdna(scaleAsCdna.getState());
    ap.av.getCodingComplement()
            .setScaleProteinAsCdna(scaleAsCdna.getState());
    changeFont();
  }

}
