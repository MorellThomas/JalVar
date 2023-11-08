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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.GraphLine;
import jalview.datamodel.SequenceGroup;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.ColourSchemeI;
import jalview.util.MessageManager;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class AnnotationColourChooser extends AnnotationRowFilter
{
  private ColourSchemeI oldcs;

  private JButton defColours;

  private Hashtable<SequenceGroup, ColourSchemeI> oldgroupColours;

  private JCheckBox useOriginalColours = new JCheckBox();

  JPanel minColour = new JPanel();

  JPanel maxColour = new JPanel();

  private JCheckBox thresholdIsMin = new JCheckBox();

  protected static final int MIN_WIDTH = 500;

  protected static final int MIN_HEIGHT = 240;

  public AnnotationColourChooser(AlignViewport av, final AlignmentPanel ap)
  {
    this(av,ap,null);
  }
  public AnnotationColourChooser(AlignViewport av, final AlignmentPanel ap,AnnotationColourGradient initSettings)
  {
    super(av, ap);
    oldcs = av.getGlobalColourScheme();
    if (av.getAlignment().getGroups() != null)
    {
      oldgroupColours = new Hashtable<>();
      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        if (sg.getColourScheme() != null)
        {
          oldgroupColours.put(sg, sg.getColourScheme());
        }
      }
    }
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.colour_by_annotation"), 520,
            215);
    frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    addSliderChangeListener();
    addSliderMouseListeners();

    if (av.getAlignment().getAlignmentAnnotation() == null)
    {
      return;
    }

    // Always get default shading from preferences.
    setDefaultMinMax();

    adjusting = true;
    if (oldcs instanceof AnnotationColourGradient && initSettings==null)
    {
      // init from oldcs
      initialiseFrom((AnnotationColourGradient) oldcs);
    } else {
      // use initial colour gradient - if any..
      initialiseFrom(initSettings);
    }
    
    jbInit();
    adjusting = false;

    updateView();
    frame.invalidate();
    frame.pack();
  }
  private void initialiseFrom(AnnotationColourGradient acg)
  {
    if (acg!=null)
    {
      useOriginalColours.setSelected(
            acg.isPredefinedColours() || acg.getBaseColour() != null);
      if (!acg.isPredefinedColours() && acg.getBaseColour() == null)
      {
        minColour.setBackground(acg.getMinColour());
        maxColour.setBackground(acg.getMaxColour());
      }
      seqAssociated.setSelected(acg.isSeqAssociated());

    }
    Vector<String> annotItems = getAnnotationItems(
            seqAssociated.isSelected());
    annotations = new JComboBox<>(annotItems);

    populateThresholdComboBox(threshold);

    if (acg!=null)
    {
      String label = getAnnotationMenuLabel(acg.getAnnotation());
      // TODO: workaround below shouldn't be necessary - there's a bug in getAnnotationMenuLabel!
      if (acg.isSeqAssociated())
      {
        label = acg.getAnnotation().label;
      }
      annotations.setSelectedItem(label);
      switch (acg.getAboveThreshold())
      {
      case AnnotationColourGradient.NO_THRESHOLD:
        getThreshold().setSelectedIndex(0);
        break;
      case AnnotationColourGradient.ABOVE_THRESHOLD:
        getThreshold().setSelectedIndex(1);
        break;
      case AnnotationColourGradient.BELOW_THRESHOLD:
        getThreshold().setSelectedIndex(2);
        break;
      default:
        throw new Error(MessageManager.getString(
                "error.implementation_error_dont_know_about_threshold_setting"));
      }
      thresholdIsMin.setSelected(acg.isThresholdIsMinMax());
      thresholdValue.setText(String.valueOf(acg.getAnnotationThreshold()));
    }
  }

  @Override
  protected void jbInit()
  {
    super.jbInit();

    minColour.setFont(JvSwingUtils.getLabelFont());
    minColour.setBorder(BorderFactory.createEtchedBorder());
    minColour.setPreferredSize(new Dimension(40, 20));
    minColour.setToolTipText(MessageManager.getString("label.min_colour"));
    minColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (minColour.isEnabled())
        {
          showColourChooser(minColour, "label.select_colour_minimum_value");
        }
      }
    });
    maxColour.setFont(JvSwingUtils.getLabelFont());
    maxColour.setBorder(BorderFactory.createEtchedBorder());
    maxColour.setPreferredSize(new Dimension(40, 20));
    maxColour.setToolTipText(MessageManager.getString("label.max_colour"));
    maxColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (maxColour.isEnabled())
        {
          showColourChooser(maxColour, "label.select_colour_maximum_value");
        }
      }
    });

    defColours = new JButton();
    defColours.setOpaque(false);
    defColours.setText(MessageManager.getString("action.set_defaults"));
    defColours.setToolTipText(MessageManager
            .getString("label.reset_min_max_colours_to_defaults"));
    defColours.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        resetColours_actionPerformed();
      }
    });

    useOriginalColours.setFont(JvSwingUtils.getLabelFont());
    useOriginalColours.setOpaque(false);
    useOriginalColours.setText(
            MessageManager.getString("label.use_original_colours"));
    useOriginalColours.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        originalColours_actionPerformed();
      }
    });
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin.setFont(JvSwingUtils.getLabelFont());
    thresholdIsMin
            .setText(MessageManager.getString("label.threshold_minmax"));
    thresholdIsMin.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        thresholdIsMin_actionPerformed();
      }
    });
    seqAssociated.setBackground(Color.white);
    seqAssociated.setFont(JvSwingUtils.getLabelFont());
    seqAssociated
            .setText(MessageManager.getString("label.per_sequence_only"));
    seqAssociated.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        seqAssociated_actionPerformed(annotations);
      }
    });

    this.setLayout(new BorderLayout());
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    jPanel2.setLayout(new MigLayout("", "[left][center][right]", "[][][]"));
    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);

    jPanel1.add(ok);
    jPanel1.add(cancel);
    jPanel2.add(annotations, "grow, wrap");
    jPanel2.add(seqAssociated);
    jPanel2.add(useOriginalColours);
    JPanel colpanel = new JPanel(new FlowLayout());
    colpanel.setBackground(Color.white);
    colpanel.add(minColour);
    colpanel.add(maxColour);
    jPanel2.add(colpanel, "wrap");
    jPanel2.add(getThreshold());
    jPanel2.add(defColours, "skip 1, wrap");
    jPanel2.add(thresholdIsMin);
    jPanel2.add(slider, "grow");
    jPanel2.add(thresholdValue, "grow");
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel2, java.awt.BorderLayout.CENTER);
    this.validate();
  }

  protected void resetColours_actionPerformed()
  {
    setDefaultMinMax();
    updateView();
  }

  private void setDefaultMinMax()
  {
    minColour.setBackground(
            Cache.getDefaultColour("ANNOTATIONCOLOUR_MIN", Color.orange));
    maxColour.setBackground(
            Cache.getDefaultColour("ANNOTATIONCOLOUR_MAX", Color.red));
  }

  protected void showColourChooser(JPanel colourPanel, String titleKey)
  {
    String ttl = MessageManager.getString(titleKey);
    ColourChooserListener listener = new ColourChooserListener()
    {
      @Override
      public void colourSelected(Color c)
      {
        colourPanel.setBackground(c);
        colourPanel.repaint();
        updateView();
      }
    };
    JalviewColourChooser.showColourChooser(Desktop.getDesktop(), ttl,
            colourPanel.getBackground(), listener);
  }

  @Override
  public void reset()
  {
    this.ap.alignFrame.changeColour(oldcs);
    if (av.getAlignment().getGroups() != null)
    {

      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        sg.setColourScheme(oldgroupColours.get(sg));
      }
    }
  }

  @Override
  public void valueChanged(boolean updateAllAnnotation)
  {
    if (slider.isEnabled())
    {
      if (useOriginalColours.isSelected() && !(av
              .getGlobalColourScheme() instanceof AnnotationColourGradient))
      {
        updateView();
      }
      getCurrentAnnotation().threshold.value = getSliderValue();
      propagateSeqAssociatedThreshold(updateAllAnnotation,
              getCurrentAnnotation());
      ap.paintAlignment(false, false);
    }
  }

  public void originalColours_actionPerformed()
  {
    boolean selected = useOriginalColours.isSelected();
    if (selected)
    {
      reset();
    }
    maxColour.setEnabled(!selected);
    minColour.setEnabled(!selected);
    thresholdIsMin.setEnabled(!selected);
    updateView();
  }

  @Override
  public void updateView()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    int selIndex = annotations
            .getSelectedIndex();
    int annIndex = annmap[selIndex];
    setCurrentAnnotation(
            av.getAlignment().getAlignmentAnnotation()[annIndex]);

    int selectedThresholdItem = getSelectedThresholdItem(
            getThreshold().getSelectedIndex());

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    thresholdIsMin.setEnabled(!useOriginalColours.isSelected());

    final AlignmentAnnotation currentAnnotation = getCurrentAnnotation();
    if (selectedThresholdItem == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
      thresholdIsMin.setEnabled(false);
    }
    else if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD
            && currentAnnotation.threshold == null)
    {
      currentAnnotation.setThreshold(new GraphLine(
              (currentAnnotation.graphMax - currentAnnotation.graphMin)
                      / 2f,
              "Threshold", Color.black));
    }

    if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD)
    {
      adjusting = true;
      setSliderModel(currentAnnotation.graphMin, currentAnnotation.graphMax,
              currentAnnotation.threshold.value);
      slider.setEnabled(true);

      setThresholdValueText();
      thresholdValue.setEnabled(true);
      adjusting = false;
    }
    colorAlignmentContaining(currentAnnotation, selectedThresholdItem);

    ap.alignmentChanged();
  }

  protected void colorAlignmentContaining(AlignmentAnnotation currentAnn,
          int selectedThresholdOption)
  {

    AnnotationColourGradient acg = null;
    if (useOriginalColours.isSelected())
    {
      acg = new AnnotationColourGradient(currentAnn,
              av.getGlobalColourScheme(), selectedThresholdOption);
    }
    else
    {
      acg = new AnnotationColourGradient(currentAnn,
              minColour.getBackground(), maxColour.getBackground(),
              selectedThresholdOption);
    }
    acg.setSeqAssociated(seqAssociated.isSelected());

    if (currentAnn.graphMin == 0f && currentAnn.graphMax == 0f)
    {
      acg.setPredefinedColours(true);
    }

    acg.setThresholdIsMinMax(thresholdIsMin.isSelected());

    this.ap.alignFrame.changeColour(acg);

    if (av.getAlignment().getGroups() != null)
    {

      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        if (sg.cs == null)
        {
          continue;
        }
        sg.setColourScheme(acg.getInstance(av, sg));
      }
    }
  }

  @Override
  protected void sliderDragReleased()
  {
    super.sliderDragReleased();
    ap.paintAlignment(true, true);
  }

  /**
   * construct and display a colourchooser for a given annotation row
   * 
   * @param av
   * @param ap
   * @param alignmentAnnotation
   * @param perseq - when true, enable per-sequence if alignment annotation is per sequence 
   */
  public static void displayFor(AlignViewport av, AlignmentPanel ap,
          AlignmentAnnotation alignmentAnnotation, boolean perSeq)
  {
    ColourSchemeI global = av.getGlobalColourScheme();
    AnnotationColourGradient newCS = new AnnotationColourGradient(alignmentAnnotation, global, alignmentAnnotation.threshold!=null ? AnnotationColourGradient.ABOVE_THRESHOLD:AnnotationColourGradient.NO_THRESHOLD);
    if (alignmentAnnotation.sequenceRef!=null)
    {
      newCS.setSeqAssociated(perSeq);
    }
    for (int i=0;i<alignmentAnnotation.annotations.length;i++)
    {
      Annotation ann = alignmentAnnotation.annotations[i];
      if (ann!=null && ann.colour!=null && !ann.colour.equals(Color.white))
      {
        newCS.setPredefinedColours(true);
        break;
      }
    }
    AnnotationColourChooser achooser = new AnnotationColourChooser(av,ap,newCS);
  }
}
