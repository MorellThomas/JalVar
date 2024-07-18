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

import jalview.bin.Cache;
import jalview.datamodel.SequenceI;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.io.vcf.VCFLoader;
import jalview.util.MapList;
import jalview.util.MessageManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * A dialog where a user has to input information necessary for the equivalent position calculation
 * called by CustomChooser
 */
public class VcfInfoInput extends JPanel
{
  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);

  AlignFrame af;
  
  AlignViewport av;
  
  int width;

  JTextField speciesName;
  
  String speciesId;
  
  JTextField assemblyName;
  
  String assemblyId;

  JTextField chromosomeName;
  
  String chromosomeId;

  JTextField fromOrig;
  
  int origStart;

  JTextField toOrig;
  
  int origEnd;

  JTextField fromVcf;
  
  int vcfStart;

  JTextField toVcf;
  
  int vcfEnd;

  JTextField ratioOrig;
  
  int origRatio;

  JTextField ratioVcf;
  
  int vcfRatio;

  JButton calculate;

  private JInternalFrame frame;

  final ComboBoxTooltipRenderer renderer = new ComboBoxTooltipRenderer();

  List<String> tips = new ArrayList<>();

  private JalviewFileChooser chooser;
  
  private String[] mappingInfo;
  
  private MapList mappingMap;

  /**
   * Constructor
   * 
   * @param af
   */
  public VcfInfoInput(AlignFrame alignFrame)
  {
    this.af = alignFrame;
    this.av = alignFrame.getViewport();
    this.width = this.av.getAlignment().getWidth();
    init();
    //af.alignPanel.setVcfInfoInput(this);
  }
  
  /**
   * Lays out the panel and adds it to the desktop
   */
  void init()
  {
    setLayout(new BorderLayout());
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    this.setBackground(Color.white);
    frame.addFocusListener(new FocusListener()
    {

      @Override
      public void focusLost(FocusEvent e)
      {
      }

      @Override
      public void focusGained(FocusEvent e)
      {
      }
    });
    
    //creates the buttons and fields
    speciesName = new JTextField("HOMO_SAPIENS", 10);
    speciesName.setOpaque(false);
  
    assemblyName = new JTextField("GRCh38", 5);
    assemblyName.setOpaque(false);

    chromosomeName = new JTextField("2", 3);
    chromosomeName.setOpaque(false);

    fromOrig = new JTextField("1", 4);
    fromOrig.setOpaque(false);

    toOrig = new JTextField("281435", 4);
    toOrig.setOpaque(false);

    fromVcf = new JTextField("178807423", 7);   //ncbi number
    //fromVcf = new JTextField("178830802", 7);     //gnomad number
    fromVcf.setOpaque(false);

    toVcf = new JTextField("178525989", 7);     //ncbi number
    //toVcf = new JTextField("178525989", 7);     //gnomad number
    toVcf.setOpaque(false);

    ratioOrig = new JTextField("1", 2);
    ratioOrig.setOpaque(false);

    ratioVcf = new JTextField("1", 2);
    ratioVcf.setOpaque(false);

    JPanel parentTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    parentTextPanel.setOpaque(false);
    JPanel parentMapPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    parentMapPanel.setOpaque(false);

    //-- --

    JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    textPanel.setOpaque(false);
    JvSwingUtils.createTitledBorder(textPanel,
          MessageManager.getString("label.species_assembly_chromosome"), true);    //set text border
    textPanel.add(speciesName);
    textPanel.add(assemblyName);
    textPanel.add(chromosomeName);

    
    //--
    JPanel mapPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    mapPanel.setOpaque(false);

    JvSwingUtils.createTitledBorder(mapPanel,
            MessageManager.getString("label.map_orig_to_vcf"), true);
    mapPanel.add(fromOrig);
    mapPanel.add(toOrig);
    mapPanel.add(fromVcf);
    mapPanel.add(toVcf);
    
    //--
    JPanel ratioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ratioPanel.setOpaque(false);
    JvSwingUtils.createTitledBorder(ratioPanel,
          MessageManager.getString("label.orig_to_vcf_ratio"), true);    //set text border
    ratioPanel.add(ratioOrig);
    ratioPanel.add(ratioVcf);

    
    //--
    parentTextPanel.add(textPanel);
    //calcChoicePanel.add(textPanel);
    parentMapPanel.add(mapPanel);
    parentMapPanel.add(ratioPanel);
    
    
   //-- --

    /*
     * OK / Cancel buttons
     */
    calculate = new JButton(MessageManager.getString("action.load"));
    calculate.setFont(VERDANA_11PT);
    calculate.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        calculate_actionPerformed();
      }
    });
    JButton close = new JButton(MessageManager.getString("action.close"));
    close.setFont(VERDANA_11PT);
    close.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed();
      }
    });
    JPanel actionPanel = new JPanel();
    actionPanel.setOpaque(false);
    actionPanel.add(calculate);
    actionPanel.add(close);

    //this.add(calcChoicePanel, BorderLayout.NORTH);
    this.add(parentTextPanel, BorderLayout.NORTH);
    this.add(parentMapPanel, BorderLayout.CENTER);
    //this.add(mapPanel, BorderLayout.WEST);
    //this.add(ratioPanel, BorderLayout.EAST);
    this.add(actionPanel, BorderLayout.SOUTH);

    int height = 281;
    int width = 400;

    setMinimumSize(new Dimension(325, height - 10));
    String title = MessageManager.getString("label.choose_calculation");
    if (af.getViewport().getViewName() != null)
    {
      title = title + " (" + af.getViewport().getViewName() + ")";
    }

    Desktop.addInternalFrame(frame, title, width, height, false);
    parentTextPanel.doLayout();
    parentMapPanel.doLayout();
    revalidate();
    /*
     * null the AlignmentPanel's reference to the dialog when it is closed
     */
    frame.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosed(InternalFrameEvent evt)
      {
        af.alignPanel.setVcfInput(null);
      };
    });

    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    
  }


  /**
   * Open and calculate the selected 
   */
  protected void calculate_actionPerformed()
  {
    speciesId = speciesName.getText();
    assemblyId = assemblyName.getText();
    chromosomeId = chromosomeName.getText();

    origStart = Integer.parseInt(fromOrig.getText());
    origEnd = Integer.parseInt(toOrig.getText());
    vcfStart = Integer.parseInt(fromVcf.getText());
    vcfEnd = Integer.parseInt(toVcf.getText());
    
    origRatio = Integer.parseInt(ratioOrig.getText());
    vcfRatio = Integer.parseInt(ratioVcf.getText());
    
    String[] info = new String[]{speciesId, assemblyId, chromosomeId};
    MapList mapping = new MapList(new int[] {origStart, origEnd}, new int[] {vcfStart, vcfEnd}, origRatio, vcfRatio);
    
    closeFrame();
    
    //check if orig and vcf is 1:1
    int origLength = origEnd - origStart;
    int vcfLength = vcfEnd > vcfStart ? vcfEnd - vcfStart : vcfStart - vcfEnd;
    if (origLength != vcfLength)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop, String.format("Gene sequence and VCF length do not fit! (Gene: %d, VCF: %d)", origLength, vcfLength), "Gene and VCF Length Error", JvOptionPane.ERROR_MESSAGE);
      throw new RuntimeException();
    }
    
    chooser = new JalviewFileChooser("vcf", "VCF");
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.getString("label.load_vcf_file"));
    chooser.setToolTipText(MessageManager.getString("label.load_vcf_file"));
    final AlignFrame us = af;
    chooser.setResponseHandler(0, () -> {
      String choice = chooser.getSelectedFile().getPath();
      Cache.setProperty("LAST_DIRECTORY", choice);
      SequenceI[] seqs = av.getAlignment().getSequencesArray();
      new VCFLoader(choice).loadVCF(seqs, us, mapping, info);
    });
    chooser.showOpenDialog(null);
  }


  /**
   * 
   */
  protected void closeFrame()
  {
    try
    {
      frame.setClosed(true);
    } catch (PropertyVetoException ex)
    {
    }
  }


  /**
   * Closes dialog on Close button press
   */
  protected void close_actionPerformed()
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }
  
  protected String[] getMapInfo()
  {
    return mappingInfo;
  }
  
  protected MapList getMapMap()
  {
    return mappingMap;
  }

}