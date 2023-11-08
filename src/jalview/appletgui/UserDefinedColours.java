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

import jalview.analysis.AAFrequency;
import jalview.api.FeatureColourI;
import jalview.datamodel.SequenceGroup;
import jalview.renderer.ResidueShader;
import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.FeatureColour;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.ResidueProperties;
import jalview.schemes.UserColourScheme;
import jalview.util.MessageManager;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class UserDefinedColours extends Panel
        implements ActionListener, AdjustmentListener, FocusListener
{

  AlignmentPanel ap;

  SequenceGroup seqGroup;

  Button selectedButton;

  Vector<Color> oldColours = new Vector<>();

  ColourSchemeI oldColourScheme;

  Frame frame;

  mc_view.AppletPDBCanvas pdbcanvas;

  AppletJmol jmol;

  Dialog dialog;

  Object caller;

  String originalLabel;

  FeatureColourI originalColour;

  int R = 0, G = 0, B = 0;

  public ColourSchemeI loadDefaultColours()
  {
    // NOT IMPLEMENTED YET IN APPLET VERSION
    return null;
  }

  public UserDefinedColours(AlignmentPanel ap, SequenceGroup sg)
  {
    this.ap = ap;
    seqGroup = sg;

    if (seqGroup != null)
    {
      oldColourScheme = seqGroup.getColourScheme();
    }
    else
    {
      oldColourScheme = ap.av.getGlobalColourScheme();
    }

    init();
  }

  public UserDefinedColours(mc_view.AppletPDBCanvas pdb)
  {
    this.pdbcanvas = pdb;
    init();
  }

  public UserDefinedColours(AppletJmol jmol)
  {
    this.jmol = jmol;
    init();
  }

  public UserDefinedColours(FeatureRenderer fr, Frame alignframe)
  {
    caller = fr;
    originalColour = new FeatureColour(fr.colourPanel.getBackground());
    originalLabel = "Feature Colour";
    setForDialog("Select Feature Colour", alignframe);
    setTargetColour(fr.colourPanel.getBackground());
    dialog.setVisible(true);
  }

  public UserDefinedColours(Component caller, Color col1, Frame alignframe)
  {
    this(caller, col1, alignframe, "Select Colour");
  }

  /**
   * Makes a dialog to choose the colour
   * 
   * @param caller
   *          - handles events
   * @param col
   *          - original colour
   * @param alignframe
   *          - the parent Frame for the dialog
   * @param title
   *          - window title
   */
  public UserDefinedColours(Component caller, Color col, Frame alignframe,
          String title)
  {
    this.caller = caller;
    originalColour = new FeatureColour(col);
    originalLabel = title;
    setForDialog(title, alignframe);
    setTargetColour(col);
    dialog.setVisible(true);
  }

  /**
   * feature colour chooser
   * 
   * @param caller
   * @param label
   * @param colour
   */
  public UserDefinedColours(Object caller, String label, Color colour)
  {
    this(caller, label, new FeatureColour(colour), colour);
  }

  /**
   * feature colour chooser when changing style to single color
   * 
   * @param me
   * @param type
   * @param graduatedColor
   */
  public UserDefinedColours(FeatureSettings me, String type,
          FeatureColourI graduatedColor)
  {
    this(me, type, graduatedColor, graduatedColor.getMaxColour());
  }

  private UserDefinedColours(Object caller, String label,
          FeatureColourI ocolour, Color colour)
  {
    this.caller = caller;
    originalColour = ocolour;
    originalLabel = label;
    init();
    remove(buttonPanel);

    setTargetColour(colour);

    okcancelPanel.setBounds(new Rectangle(0, 113, 400, 35));
    frame.setTitle(MessageManager.getString("label.user_defined_colours")
            + " - " + label);
    frame.setSize(420, 200);
  }

  void setForDialog(String title, Container alignframe)
  {
    init();
    frame.setVisible(false);
    remove(buttonPanel);
    if (alignframe instanceof Frame)
    {
      dialog = new Dialog((Frame) alignframe, title, true);
    }
    else
    {
      // if (alignframe instanceof JVDialog){
      // // not 1.1 compatible!
      // dialog = new Dialog(((JVDialog)alignframe), title, true);
      // } else {
      throw new Error(MessageManager.getString(
              "label.error_unsupported_owwner_user_colour_scheme"));
    }

    dialog.add(this);
    this.setSize(400, 123);
    okcancelPanel.setBounds(new Rectangle(0, 123, 400, 35));
    int height = 160 + alignframe.getInsets().top + getInsets().bottom;
    int width = 400;

    dialog.setBounds(
            alignframe.getBounds().x
                    + (alignframe.getSize().width - width) / 2,
            alignframe.getBounds().y
                    + (alignframe.getSize().height - height) / 2,
            width, height);

  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    final Object source = evt.getSource();
    if (source == okButton)
    {
      okButton_actionPerformed();
    }
    else if (source == applyButton)
    {
      applyButton_actionPerformed();
    }
    else if (source == cancelButton)
    {
      cancelButton_actionPerformed();
    }
    else if (source == rText)
    {
      rText_actionPerformed();
    }
    else if (source == gText)
    {
      gText_actionPerformed();
    }
    else if (source == bText)
    {
      bText_actionPerformed();
    }
  }

  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (evt.getSource() == rScroller)
    {
      rScroller_adjustmentValueChanged();
    }
    else if (evt.getSource() == gScroller)
    {
      gScroller_adjustmentValueChanged();
    }
    else if (evt.getSource() == bScroller)
    {
      bScroller_adjustmentValueChanged();
    }
  }

  void init()
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.user_defined_colours"), 420,
            345);

    if (seqGroup != null)
    {
      frame.setTitle(frame.getTitle() + " (" + seqGroup.getName() + ")");
    }

    for (int i = 0; i < 20; i++)
    {
      makeButton(ResidueProperties.aa2Triplet.get(ResidueProperties.aa[i])
              + "", ResidueProperties.aa[i]);
    }

    makeButton("B", "B");
    makeButton("Z", "Z");
    makeButton("X", "X");
    makeButton("Gap", "'.','-',' '");

    validate();
  }

  protected void rText_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(rText.getText());
      rScroller.setValue(i);
      rScroller_adjustmentValueChanged();
    } catch (NumberFormatException ex)
    {
    }
  }

  protected void gText_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(gText.getText());
      gScroller.setValue(i);
      gScroller_adjustmentValueChanged();
    } catch (NumberFormatException ex)
    {
    }

  }

  protected void bText_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(bText.getText());
      bScroller.setValue(i);
      bScroller_adjustmentValueChanged();
    } catch (NumberFormatException ex)
    {
    }

  }

  protected void rScroller_adjustmentValueChanged()
  {
    R = rScroller.getValue();
    rText.setText(R + "");
    colourChanged();
  }

  protected void gScroller_adjustmentValueChanged()
  {
    G = gScroller.getValue();
    gText.setText(G + "");
    colourChanged();
  }

  protected void bScroller_adjustmentValueChanged()
  {
    B = bScroller.getValue();
    bText.setText(B + "");
    colourChanged();
  }

  public void colourChanged()
  {
    Color col = new Color(R, G, B);
    target.setBackground(col);
    target.repaint();

    if (selectedButton != null)
    {
      selectedButton.setBackground(col);
      selectedButton.repaint();
    }
  }

  void setTargetColour(Color col)
  {
    R = col.getRed();
    G = col.getGreen();
    B = col.getBlue();

    rScroller.setValue(R);
    gScroller.setValue(G);
    bScroller.setValue(B);
    rText.setText(R + "");
    gText.setText(G + "");
    bText.setText(B + "");
    colourChanged();
  }

  public void colourButtonPressed(MouseEvent e)
  {
    selectedButton = (Button) e.getSource();
    setTargetColour(selectedButton.getBackground());
  }

  void makeButton(String label, String aa)
  {
    final Button button = new Button();
    Color col = Color.white;
    if (oldColourScheme != null && oldColourScheme.isSimple())
    {
      col = oldColourScheme.findColour(aa.charAt(0), 0, null, null, 0f);
    }
    button.setBackground(col);
    oldColours.addElement(col);
    button.setLabel(label);
    button.setForeground(col.darker().darker().darker());
    button.setFont(new java.awt.Font("Verdana", 1, 10));
    button.addMouseListener(new java.awt.event.MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        colourButtonPressed(e);
      }
    });

    buttonPanel.add(button, null);
  }

  protected void okButton_actionPerformed()
  {
    applyButton_actionPerformed();
    if (dialog != null)
    {
      dialog.setVisible(false);
    }

    frame.setVisible(false);
  }

  public Color getColor()
  {
    return new Color(R, G, B);
  }

  protected void applyButton_actionPerformed()
  {
    if (caller != null)
    {
      if (caller instanceof FeatureSettings)
      {
        ((FeatureSettings) caller).setUserColour(originalLabel,
                new FeatureColour(getColor()));
      }
      else if (caller instanceof AnnotationColourChooser)
      {
        if (originalLabel.equals("Min Colour"))
        {
          ((AnnotationColourChooser) caller)
                  .minColour_actionPerformed(getColor());
        }
        else
        {
          ((AnnotationColourChooser) caller)
                  .maxColour_actionPerformed(getColor());
        }
      }
      else if (caller instanceof FeatureRenderer)
      {
        ((FeatureRenderer) caller).colourPanel
                .updateColor(new FeatureColour(getColor()));
      }
      else if (caller instanceof FeatureColourChooser)
      {
        if (originalLabel.indexOf("inimum") > -1)
        {
          ((FeatureColourChooser) caller)
                  .minColour_actionPerformed(getColor());
        }
        else
        {
          ((FeatureColourChooser) caller)
                  .maxColour_actionPerformed(getColor());
        }
      }

      return;
    }

    Color[] newColours = new Color[24];
    for (int i = 0; i < 24; i++)
    {
      Button button = (Button) buttonPanel.getComponent(i);
      newColours[i] = button.getBackground();
    }

    UserColourScheme ucs = new UserColourScheme(newColours);
    // if (ap != null)
    // {
    // ucs.setThreshold(0, ap.av.isIgnoreGapsConsensus());
    // }

    if (ap != null)
    {
      if (seqGroup != null)
      {
        seqGroup.cs = new ResidueShader(ucs);
        seqGroup.getGroupColourScheme().setThreshold(0,
                ap.av.isIgnoreGapsConsensus());
      }
      else
      {
        ap.av.setGlobalColourScheme(ucs);
        ap.av.getResidueShading().setThreshold(0,
                ap.av.isIgnoreGapsConsensus());
      }
      ap.seqPanel.seqCanvas.img = null;
      ap.paintAlignment(true, true);
    }
    else if (jmol != null)
    {
      jmol.colourByJalviewColourScheme(ucs);
    }
    else if (pdbcanvas != null)
    {
      pdbcanvas.setColours(ucs);
    }
  }

  protected void cancelButton_actionPerformed()
  {
    if (caller != null)
    {
      if (caller instanceof FeatureSettings)
      {
        ((FeatureSettings) caller).setUserColour(originalLabel,
                originalColour);
      }
      else if (caller instanceof AnnotationColourChooser)
      {
        if (originalLabel.equals("Min Colour"))
        {
          ((AnnotationColourChooser) caller)
                  .minColour_actionPerformed(originalColour.getColour());
        }
        else
        {
          ((AnnotationColourChooser) caller)
                  .maxColour_actionPerformed(originalColour.getColour());
        }
      }
      else if (caller instanceof FeatureRenderer)
      {
        ((FeatureRenderer) caller).colourPanel.updateColor(originalColour);

      }

      else if (caller instanceof FeatureColourChooser)
      {
        if (originalLabel.indexOf("inimum") > -1)
        {
          ((FeatureColourChooser) caller)
                  .minColour_actionPerformed(originalColour.getColour());
        }
        else
        {
          ((FeatureColourChooser) caller)
                  .maxColour_actionPerformed(originalColour.getColour());
        }
      }
      if (dialog != null)
      {
        dialog.setVisible(false);
      }

      frame.setVisible(false);
      return;
    }

    if (ap != null)
    {
      if (seqGroup != null)
      {
        seqGroup.cs = new ResidueShader(oldColourScheme);
        if (oldColourScheme instanceof PIDColourScheme
                || oldColourScheme instanceof Blosum62ColourScheme)
        {
          seqGroup.cs.setConsensus(AAFrequency.calculate(
                  seqGroup.getSequences(ap.av.getHiddenRepSequences()), 0,
                  ap.av.getAlignment().getWidth()));
        }
      }
      else
      {
        ap.av.setGlobalColourScheme(oldColourScheme);
      }
      ap.paintAlignment(true, true);
    }

    frame.setVisible(false);
  }

  protected Panel buttonPanel = new Panel();

  protected GridLayout gridLayout = new GridLayout();

  Panel okcancelPanel = new Panel();

  protected Button okButton = new Button();

  protected Button applyButton = new Button();

  protected Button cancelButton = new Button();

  protected Scrollbar rScroller = new Scrollbar();

  Label label1 = new Label();

  protected TextField rText = new TextField();

  Label label4 = new Label();

  protected Scrollbar gScroller = new Scrollbar();

  protected TextField gText = new TextField();

  Label label5 = new Label();

  protected Scrollbar bScroller = new Scrollbar();

  protected TextField bText = new TextField();

  protected Panel target = new Panel();

  private void jbInit() throws Exception
  {
    this.setLayout(null);
    buttonPanel.setLayout(gridLayout);
    gridLayout.setColumns(6);
    gridLayout.setRows(4);
    okButton.setFont(new java.awt.Font("Verdana", 0, 11));
    okButton.setLabel(MessageManager.getString("action.ok"));
    okButton.addActionListener(this);
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setLabel(MessageManager.getString("action.apply"));
    applyButton.addActionListener(this);
    cancelButton.setFont(new java.awt.Font("Verdana", 0, 11));
    cancelButton.setLabel(MessageManager.getString("action.cancel"));
    cancelButton.addActionListener(this);
    this.setBackground(new Color(212, 208, 223));
    okcancelPanel.setBounds(new Rectangle(0, 265, 400, 35));
    buttonPanel.setBounds(new Rectangle(0, 123, 400, 142));
    rScroller.setMaximum(256);
    rScroller.setMinimum(0);
    rScroller.setOrientation(0);
    rScroller.setUnitIncrement(1);
    rScroller.setVisibleAmount(1);
    rScroller.setBounds(new Rectangle(36, 27, 119, 19));
    rScroller.addAdjustmentListener(this);
    label1.setAlignment(Label.RIGHT);
    label1.setText("R");
    label1.setBounds(new Rectangle(19, 30, 16, 15));
    rText.setFont(new java.awt.Font("Dialog", Font.PLAIN, 10));
    rText.setText("0        ");
    rText.setBounds(new Rectangle(156, 27, 53, 19));
    rText.addActionListener(this);
    rText.addFocusListener(this);
    label4.setAlignment(Label.RIGHT);
    label4.setText("G");
    label4.setBounds(new Rectangle(15, 56, 20, 15));
    gScroller.setMaximum(256);
    gScroller.setMinimum(0);
    gScroller.setOrientation(0);
    gScroller.setUnitIncrement(1);
    gScroller.setVisibleAmount(1);
    gScroller.setBounds(new Rectangle(35, 52, 120, 20));
    gScroller.addAdjustmentListener(this);
    gText.setFont(new java.awt.Font("Dialog", Font.PLAIN, 10));
    gText.setText("0        ");
    gText.setBounds(new Rectangle(156, 52, 53, 20));
    gText.addActionListener(this);
    gText.addFocusListener(this);
    label5.setAlignment(Label.RIGHT);
    label5.setText("B");
    label5.setBounds(new Rectangle(14, 82, 20, 15));
    bScroller.setMaximum(256);
    bScroller.setMinimum(0);
    bScroller.setOrientation(0);
    bScroller.setUnitIncrement(1);
    bScroller.setVisibleAmount(1);
    bScroller.setBounds(new Rectangle(35, 78, 120, 20));
    bScroller.addAdjustmentListener(this);
    bText.setFont(new java.awt.Font("Dialog", Font.PLAIN, 10));
    bText.setText("0        ");
    bText.setBounds(new Rectangle(157, 78, 52, 20));
    bText.addActionListener(this);
    bText.addFocusListener(this);
    target.setBackground(Color.black);
    target.setBounds(new Rectangle(229, 26, 134, 79));
    this.add(okcancelPanel, null);
    okcancelPanel.add(okButton, null);
    okcancelPanel.add(applyButton, null);
    okcancelPanel.add(cancelButton, null);
    this.add(rText);
    this.add(gText);
    this.add(bText);
    this.add(buttonPanel, null);
    this.add(target, null);
    this.add(gScroller);
    this.add(rScroller);
    this.add(bScroller);
    this.add(label5);
    this.add(label4);
    this.add(label1);
  }

  @Override
  public void focusGained(FocusEvent e)
  {
    // noop
  }

  /**
   * This method applies any change to an RGB value if the user tabs out of the
   * field instead of pressing Enter
   */
  @Override
  public void focusLost(FocusEvent e)
  {
    Component c = e.getComponent();
    if (c == rText)
    {
      rText_actionPerformed();
    }
    else
    {
      if (c == gText)
      {
        gText_actionPerformed();
      }
      else
      {
        if (c == bText)
        {
          bText_actionPerformed();
        }
      }
    }
  }

}
