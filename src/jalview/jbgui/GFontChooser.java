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
package jalview.jbgui;

import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GFontChooser extends JPanel
{
  private static final Font VERDANA_11PT = new java.awt.Font("Verdana", 0,
          11);

  protected JComboBox<Integer> fontSize = new JComboBox<Integer>();

  protected JComboBox<String> fontStyle = new JComboBox<String>();

  protected JComboBox<String> fontName = new JComboBox<String>();

  protected JButton defaultButton = new JButton();

  protected JCheckBox smoothFont = new JCheckBox();

  protected JCheckBox monospaced = new JCheckBox();

  protected JCheckBox scaleAsCdna = new JCheckBox();

  protected JCheckBox fontAsCdna = new JCheckBox();

  /**
   * Creates a new GFontChooser object.
   */
  public GFontChooser()
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
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
    this.setLayout(null);
    this.setBackground(Color.white);

    JLabel fontLabel = new JLabel(MessageManager.getString("label.font"));
    fontLabel.setFont(VERDANA_11PT);
    fontLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    fontLabel.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);

    fontSize.setFont(VERDANA_11PT);
    fontSize.setOpaque(false);
    fontSize.setPreferredSize(new Dimension(50, 21));
    fontSize.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        fontSize_actionPerformed();
      }
    });

    fontStyle.setFont(VERDANA_11PT);
    fontStyle.setOpaque(false);
    fontStyle.setPreferredSize(new Dimension(90, 21));
    fontStyle.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        fontStyle_actionPerformed();
      }
    });

    JLabel sizeLabel = new JLabel(MessageManager.getString("label.size"));
    sizeLabel.setFont(VERDANA_11PT);
    sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    sizeLabel.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);

    JLabel styleLabel = new JLabel(MessageManager.getString("label.style"));
    styleLabel.setFont(VERDANA_11PT);
    styleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    styleLabel.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);

    fontName.setFont(VERDANA_11PT);
    fontName.setMaximumSize(new Dimension(32767, 32767));
    fontName.setMinimumSize(new Dimension(300, 21));
    fontName.setOpaque(false);
    fontName.setPreferredSize(new Dimension(180, 21));
    fontName.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        fontName_actionPerformed();
      }
    });

    JButton ok = new JButton(MessageManager.getString("action.ok"));
    ok.setFont(VERDANA_11PT);
    ok.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed();
      }
    });

    JButton cancel = new JButton(MessageManager.getString("action.cancel"));
    cancel.setFont(VERDANA_11PT);
    cancel.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed();
      }
    });

    defaultButton.setFont(JvSwingUtils.getLabelFont());
    defaultButton.setText(MessageManager.getString("label.set_as_default"));
    defaultButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        defaultButton_actionPerformed();
      }
    });

    smoothFont.setFont(JvSwingUtils.getLabelFont());
    smoothFont.setOpaque(false);
    smoothFont.setText(MessageManager.getString("label.anti_alias_fonts"));
    smoothFont.setBounds(new Rectangle(1, 65, 300, 23));
    smoothFont.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        smoothFont_actionPerformed();
      }
    });

    /*
     * Scale protein as cDNA is only visible in SplitFrame
     */
    scaleAsCdna.setVisible(false);
    scaleAsCdna.setFont(JvSwingUtils.getLabelFont());
    scaleAsCdna.setOpaque(false);
    scaleAsCdna.setText(MessageManager.getString("label.scale_as_cdna"));
    scaleAsCdna.setBounds(new Rectangle(1, 85, 300, 23));
    scaleAsCdna.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        scaleAsCdna_actionPerformed();
      }
    });

    /*
     * Same font for cDNA/peptide is only visible in SplitFrame
     */
    fontAsCdna.setVisible(false);
    fontAsCdna.setFont(JvSwingUtils.getLabelFont());
    fontAsCdna.setOpaque(false);
    fontAsCdna.setText(MessageManager.getString("label.font_as_cdna"));
    fontAsCdna.setBounds(new Rectangle(1, 105, 350, 23));
    fontAsCdna.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        mirrorFonts_actionPerformed();
      }
    });

    monospaced.setEnabled(false);
    monospaced.setFont(JvSwingUtils.getLabelFont());
    monospaced.setOpaque(false);
    monospaced.setToolTipText(MessageManager
            .getString("label.monospaced_fonts_faster_to_render"));
    monospaced.setText(MessageManager.getString("label.monospaced_font"));

    /*
     * jPanel1: Font dropdown, Monospaced checkbox
     */
    JPanel jPanel1 = new JPanel();
    jPanel1.setOpaque(false);
    jPanel1.setBounds(new Rectangle(5, 6, 308, 23));
    jPanel1.setLayout(new BorderLayout());
    jPanel1.add(fontLabel, BorderLayout.WEST);
    jPanel1.add(fontName, BorderLayout.CENTER);
    jPanel1.add(monospaced, java.awt.BorderLayout.EAST);

    /*
     * jPanel2: font size dropdown
     */
    JPanel jPanel2 = new JPanel();
    jPanel2.setOpaque(false);
    jPanel2.setBounds(new Rectangle(5, 37, 128, 21));
    jPanel2.setLayout(new BorderLayout());
    jPanel2.add(fontSize, java.awt.BorderLayout.CENTER);
    jPanel2.add(sizeLabel, java.awt.BorderLayout.WEST);

    /*
     * jPanel3: font style dropdown
     */
    JPanel jPanel3 = new JPanel();
    jPanel3.setOpaque(false);
    jPanel3.setBounds(new Rectangle(174, 38, 134, 21));
    jPanel3.setLayout(new BorderLayout());
    jPanel3.add(styleLabel, java.awt.BorderLayout.WEST);
    jPanel3.add(fontStyle, java.awt.BorderLayout.CENTER);

    /*
     * jPanel4: Default and OK buttons
     */
    JPanel jPanel4 = new JPanel();
    jPanel4.setOpaque(false);
    jPanel4.setBounds(new Rectangle(24, 132, 300, 35));
    jPanel4.add(defaultButton);
    jPanel4.add(ok);
    jPanel4.add(cancel);

    this.add(smoothFont);
    this.add(scaleAsCdna);
    this.add(fontAsCdna);
    this.add(jPanel3, null);
    this.add(jPanel2, null);
    this.add(jPanel4);
    this.add(jPanel1, null);
  }

  protected void mirrorFonts_actionPerformed()
  {
  }

  protected void scaleAsCdna_actionPerformed()
  {
  }

  protected void ok_actionPerformed()
  {
  }

  protected void cancel_actionPerformed()
  {
  }

  protected void fontName_actionPerformed()
  {
  }

  protected void fontSize_actionPerformed()
  {
  }

  protected void fontStyle_actionPerformed()
  {
  }

  public void defaultButton_actionPerformed()
  {
  }

  protected void smoothFont_actionPerformed()
  {
  }
}
