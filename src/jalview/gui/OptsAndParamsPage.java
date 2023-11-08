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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jalview.util.MessageManager;
import jalview.ws.params.ArgumentI;
import jalview.ws.params.OptionI;
import jalview.ws.params.ParameterI;
import jalview.ws.params.ValueConstrainI;
import jalview.ws.params.ValueConstrainI.ValueType;
import net.miginfocom.swing.MigLayout;

/**
 * GUI generator/manager for options and parameters. Originally abstracted from
 * the WsJobParameters dialog box.
 * 
 * @author jprocter
 * 
 */
public class OptsAndParamsPage
{
  /**
   * compact or verbose style parameters
   */
  boolean compact = false;

  public class OptionBox extends JPanel
          implements MouseListener, ActionListener
  {
    JCheckBox enabled = new JCheckBox();

    final URL finfo;

    boolean hasLink = false;

    boolean initEnabled = false;

    String initVal = null;

    OptionI option;

    JLabel optlabel = new JLabel();

    JComboBox<String> val = new JComboBox<>();

    public OptionBox(OptionI opt)
    {
      option = opt;
      setLayout(new BorderLayout());
      enabled.setSelected(opt.isRequired()); // TODO: lock required options
      enabled.setFont(new Font("Verdana", Font.PLAIN, 11));
      enabled.setText("");
      enabled.setText(opt.getName());
      enabled.addActionListener(this);
      finfo = option.getFurtherDetails();
      String desc = opt.getDescription();
      if (finfo != null)
      {
        hasLink = true;

        enabled.setToolTipText(JvSwingUtils.wrapTooltip(true,
                ((desc == null || desc.trim().length() == 0)
                        ? MessageManager.getString(
                                "label.opt_and_params_further_details")
                        : desc) + "<br><img src=\"" + linkImageURL
                        + "\"/>"));
        enabled.addMouseListener(this);
      }
      else
      {
        if (desc != null && desc.trim().length() > 0)
        {
          enabled.setToolTipText(
                  JvSwingUtils.wrapTooltip(true, opt.getDescription()));
        }
      }
      add(enabled, BorderLayout.NORTH);
      for (String str : opt.getPossibleValues())
      {
        val.addItem(str);
      }
      val.setSelectedItem(opt.getValue());
      if (opt.getPossibleValues().size() > 1)
      {
        setLayout(new GridLayout(1, 2));
        val.addActionListener(this);
        add(val, BorderLayout.SOUTH);
      }
      // TODO: add actionListeners for popup (to open further info),
      // and to update list of parameters if an option is enabled
      // that takes a value. JBPNote: is this TODO still valid ?
      setInitialValue();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (e.getSource() != enabled)
      {
        enabled.setSelected(true);
      }
      checkIfModified();
    }

    private void checkIfModified()
    {
      boolean notmod = (initEnabled == enabled.isSelected());
      if (enabled.isSelected())
      {
        if (initVal != null)
        {
          notmod &= initVal.equals(val.getSelectedItem());
        }
        else
        {
          // compare against default service setting
          notmod &= option.getValue() == null
                  || option.getValue().equals(val.getSelectedItem());
        }
      }
      else
      {
        notmod &= (initVal != null) ? initVal.equals(val.getSelectedItem())
                : val.getSelectedItem() != initVal;
      }
      poparent.argSetModified(this, !notmod);
    }

    public OptionI getOptionIfEnabled()
    {
      if (!enabled.isSelected())
      {
        return null;
      }
      OptionI opt = option.copy();
      if (opt.getPossibleValues() != null
              && opt.getPossibleValues().size() == 1)
      {
        // Hack to make sure the default value for an enabled option with only
        // one value is actually returned
        opt.setValue(opt.getPossibleValues().get(0));
      }
      if (val.getSelectedItem() != null)
      {
        opt.setValue((String) val.getSelectedItem());
      }
      else
      {
        if (option.getValue() != null)
        {
          opt.setValue(option.getValue());
        }
      }
      return opt;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
      if (e.isPopupTrigger()) // for Windows
      {
        showUrlPopUp(this, finfo.toString(), e.getX(), e.getY());
      }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      if (e.isPopupTrigger()) // Mac
      {
        showUrlPopUp(this, finfo.toString(), e.getX(), e.getY());
      }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    public void resetToDefault(boolean setDefaultParams)
    {
      enabled.setSelected(false);
      if (option.isRequired()
              || (setDefaultParams && option.getValue() != null))
      {
        // Apply default value
        selectOption(option, option.getValue());
      }
    }

    public void setInitialValue()
    {
      initEnabled = enabled.isSelected();
      if (option.getPossibleValues() != null
              && option.getPossibleValues().size() > 1)
      {
        initVal = (String) val.getSelectedItem();
      }
      else
      {
        initVal = (initEnabled) ? (String) val.getSelectedItem() : null;
      }
    }

  }

  public class ParamBox extends JPanel
          implements ChangeListener, ActionListener, MouseListener
  {
    boolean adjusting = false;

    boolean choice = false;

    JComboBox<String> choicebox;

    JPanel controlPanel = new JPanel();

    boolean descisvisible = false;

    JScrollPane descPanel = new JScrollPane();

    final URL finfo;

    boolean integ = false;

    String lastVal;

    ParameterI parameter;

    final OptsParametersContainerI pmdialogbox;

    JPanel settingPanel = new JPanel();

    JButton showDesc = new JButton();

    Slider slider = null;

    JTextArea string = new JTextArea();

    ValueConstrainI validator = null;

    JTextField valueField = null;

    public ParamBox(final OptsParametersContainerI pmlayout,
            ParameterI parm)
    {
      pmdialogbox = pmlayout;
      finfo = parm.getFurtherDetails();
      validator = parm.getValidValue();
      parameter = parm;
      if (validator != null)
      {
        integ = validator.getType() == ValueType.Integer;
      }
      else
      {
        if (parameter.getPossibleValues() != null)
        {
          choice = true;
        }
      }

      if (!compact)
      {
        makeExpanderParam(parm);
      }
      else
      {
        makeCompactParam(parm);

      }
    }

    private void makeCompactParam(ParameterI parm)
    {
      setLayout(new MigLayout("", "[][grow]"));

      String ttipText = null;

      controlPanel.setLayout(new BorderLayout());

      if (parm.getDescription() != null
              && parm.getDescription().trim().length() > 0)
      {
        // Only create description boxes if there actually is a description.
        ttipText = (JvSwingUtils.wrapTooltip(true,
                parm.getDescription() + (finfo != null ? "<br><img src=\""
                        + linkImageURL + "\"/>"
                        + MessageManager.getString(
                                "label.opt_and_params_further_details")
                        : "")));
      }

      JvSwingUtils.mgAddtoLayout(this, ttipText, new JLabel(parm.getName()),
              controlPanel, "");
      updateControls(parm);
      validate();
    }

    private void makeExpanderParam(final ParameterI parm)
    {
      setPreferredSize(new Dimension(PARAM_WIDTH, PARAM_CLOSEDHEIGHT));
      setBorder(new TitledBorder(parm.getName()));
      setLayout(null);
      showDesc.setFont(new Font("Verdana", Font.PLAIN, 6));
      showDesc.setText("+");
      string.setFont(new Font("Verdana", Font.PLAIN, 11));
      string.setBackground(getBackground());

      string.setEditable(false);
      descPanel.getViewport().setView(string);

      descPanel.setVisible(false);

      JPanel firstrow = new JPanel();
      firstrow.setLayout(null);
      controlPanel.setLayout(new BorderLayout());
      controlPanel.setBounds(new Rectangle(39, 10, PARAM_WIDTH - 70,
              PARAM_CLOSEDHEIGHT - 50));
      firstrow.add(controlPanel);
      firstrow.setBounds(new Rectangle(10, 20, PARAM_WIDTH - 30,
              PARAM_CLOSEDHEIGHT - 30));

      final ParamBox me = this;

      if (parm.getDescription() != null
              && parm.getDescription().trim().length() > 0)
      {
        // Only create description boxes if there actually is a description.
        if (finfo != null)
        {
          showDesc.setToolTipText(JvSwingUtils.wrapTooltip(true,
                  MessageManager.formatMessage(
                          "label.opt_and_params_show_brief_desc_image_link",
                          new String[]
                          { linkImageURL.toExternalForm() })));
          showDesc.addMouseListener(this);
        }
        else
        {
          showDesc.setToolTipText(
                  JvSwingUtils.wrapTooltip(true, MessageManager.getString(
                          "label.opt_and_params_show_brief_desc")));
        }
        showDesc.addActionListener(new ActionListener()
        {

          @Override
          public void actionPerformed(ActionEvent e)
          {
            descisvisible = !descisvisible;
            descPanel.setVisible(descisvisible);
            descPanel.getVerticalScrollBar().setValue(0);
            me.setPreferredSize(new Dimension(PARAM_WIDTH,
                    (descisvisible) ? PARAM_HEIGHT : PARAM_CLOSEDHEIGHT));
            me.validate();
            pmdialogbox.refreshParamLayout();
          }
        });
        string.setWrapStyleWord(true);
        string.setLineWrap(true);
        string.setColumns(32);
        string.setText(parm.getDescription());
        showDesc.setBounds(new Rectangle(10, 10, 16, 16));
        firstrow.add(showDesc);
      }
      add(firstrow);
      validator = parm.getValidValue();
      parameter = parm;
      if (validator != null)
      {
        integ = validator.getType() == ValueType.Integer;
      }
      else
      {
        if (parameter.getPossibleValues() != null)
        {
          choice = true;
        }
      }
      updateControls(parm);
      descPanel.setBounds(new Rectangle(10, PARAM_CLOSEDHEIGHT,
              PARAM_WIDTH - 20, PARAM_HEIGHT - PARAM_CLOSEDHEIGHT - 5));
      add(descPanel);
      validate();
    }

    /**
     * Action on input in text field
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (adjusting)
      {
        return;
      }
      if (!choice)
      {
        updateSliderFromValueField();
      }
      checkIfModified();
    }

    private void checkIfModified()
    {
      Object cstate = getCurrentValue();
      boolean modified = !cstate.equals(lastVal);
      pmdialogbox.argSetModified(this, modified);
    }

    /**
     * Answers the current value of the parameter, as text
     * 
     * @return
     */
    private String getCurrentValue()
    {
      return choice ? (String) choicebox.getSelectedItem()
              : valueField.getText();
    }

    @Override
    public int getBaseline(int width, int height)
    {
      return 0;
    }

    // from
    // http://stackoverflow.com/questions/2743177/top-alignment-for-flowlayout
    // helpful hint of using the Java 1.6 alignBaseLine property of FlowLayout
    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior()
    {
      return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    public int getBoxHeight()
    {
      return (descisvisible ? PARAM_HEIGHT : PARAM_CLOSEDHEIGHT);
    }

    public ParameterI getParameter()
    {
      ParameterI prm = parameter.copy();
      if (choice)
      {
        prm.setValue((String) choicebox.getSelectedItem());
      }
      else
      {
        prm.setValue(valueField.getText());
      }
      return prm;
    }

    public void init()
    {
      // reset the widget's initial value.
      lastVal = null;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
      if (e.isPopupTrigger()) // for Windows
      {
        showUrlPopUp(this, finfo.toString(), e.getX(), e.getY());
      }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      if (e.isPopupTrigger()) // for Mac
      {
        showUrlPopUp(this, finfo.toString(), e.getX(), e.getY());
      }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      // TODO Auto-generated method stub

    }

    /**
     * Action on change of slider value
     */
    @Override
    public void stateChanged(ChangeEvent e)
    {
      if (!adjusting)
      {
        float value = slider.getSliderValue();
        valueField.setText(integ ? Integer.toString((int) value)
                : Float.toString(value));
        checkIfModified();
      }
    }

    public void updateControls(ParameterI parm)
    {
      adjusting = true;
      boolean init = (choicebox == null && valueField == null);
      if (init)
      {
        if (choice)
        {
          choicebox = new JComboBox<>();
          choicebox.addActionListener(this);
          controlPanel.add(choicebox, BorderLayout.CENTER);
        }
        else
        {
          valueField = new JTextField();
          valueField.addActionListener(this);
          valueField.addKeyListener(new KeyListener()
          {

            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
              if (e.isActionKey())
              {
                if (valueField.getText().trim().length() > 0)
                {
                  actionPerformed(null);
                }
              }
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }
          });
          valueField.addFocusListener(new FocusAdapter()
          {

            @Override
            public void focusLost(FocusEvent e)
            {
              actionPerformed(null);
            }

          });
          valueField.setPreferredSize(new Dimension(60, 25));
          valueField.setText(parm.getValue());
          slider = makeSlider(parm.getValidValue());
          updateSliderFromValueField();
          slider.addChangeListener(this);

          controlPanel.add(slider, BorderLayout.WEST);
          controlPanel.add(valueField, BorderLayout.EAST);
        }
      }

      if (parm != null)
      {
        if (choice)
        {
          if (init)
          {
            List<String> vals = parm.getPossibleValues();
            for (String val : vals)
            {
              choicebox.addItem(val);
            }
          }

          if (parm.getValue() != null)
          {
            choicebox.setSelectedItem(parm.getValue());
          }
        }
        else
        {
          valueField.setText(parm.getValue());
        }
      }
      lastVal = getCurrentValue();
      adjusting = false;
    }

    private Slider makeSlider(ValueConstrainI validValue)
    {
      if (validValue != null)
      {
        final Number minValue = validValue.getMin();
        final Number maxValue = validValue.getMax();
        if (minValue != null && maxValue != null)
        {
          return new Slider(minValue.floatValue(), maxValue.floatValue(),
                  minValue.floatValue());
        }
      }

      /*
       * otherwise, a nominal slider which will not be visible
       */
      return new Slider(0, 100, 50);
    }

    public void updateSliderFromValueField()
    {
      if (validator != null)
      {
        final Number minValue = validator.getMin();
        final Number maxValue = validator.getMax();
        if (integ)
        {
          int iVal = 0;
          try
          {
            valueField.setText(valueField.getText().trim());
            iVal = Integer.valueOf(valueField.getText());
            if (minValue != null && minValue.intValue() > iVal)
            {
              iVal = minValue.intValue();
              // TODO: provide visual indication that hard limit was reached for
              // this parameter
            }
            if (maxValue != null && maxValue.intValue() < iVal)
            {
              iVal = maxValue.intValue();
            }
          } catch (NumberFormatException e)
          {
            System.err.println(e.toString());
          }
          if (minValue != null || maxValue != null)
          {
            valueField.setText(String.valueOf(iVal));
            slider.setSliderValue(iVal);
          }
          else
          {
            slider.setVisible(false);
          }
        }
        else
        {
          float fVal = 0f;
          try
          {
            valueField.setText(valueField.getText().trim());
            fVal = Float.valueOf(valueField.getText());
            if (minValue != null && minValue.floatValue() > fVal)
            {
              fVal = minValue.floatValue();
              // TODO: provide visual indication that hard limit was reached for
              // this parameter
              // update value field to reflect any bound checking we performed.
              valueField.setText("" + fVal);
            }
            if (maxValue != null && maxValue.floatValue() < fVal)
            {
              fVal = maxValue.floatValue();
              // TODO: provide visual indication that hard limit was reached for
              // this parameter
              // update value field to reflect any bound checking we performed.
              valueField.setText("" + fVal);
            }
          } catch (NumberFormatException e)
          {
            System.err.println(e.toString());
          }
          if (minValue != null && maxValue != null)
          {
            slider.setSliderModel(minValue.floatValue(),
                    maxValue.floatValue(), fVal);
          }
          else
          {
            slider.setVisible(false);
          }
        }
      }
      else
      {
        if (!choice)
        {
          slider.setVisible(false);
        }
      }
    }
  }

  public static final int PARAM_WIDTH = 340;

  public static final int PARAM_HEIGHT = 150;

  public static final int PARAM_CLOSEDHEIGHT = 80;

  public OptsAndParamsPage(OptsParametersContainerI paramContainer)
  {
    this(paramContainer, false);
  }

  public OptsAndParamsPage(OptsParametersContainerI paramContainer,
          boolean compact)
  {
    poparent = paramContainer;
    this.compact = compact;
  }

  public static void showUrlPopUp(JComponent invoker, final String finfo,
          int x, int y)
  {

    JPopupMenu mnu = new JPopupMenu();
    JMenuItem mitem = new JMenuItem(
            MessageManager.formatMessage("label.view_params", new String[]
            { finfo }));
    mitem.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        Desktop.showUrl(finfo);

      }
    });
    mnu.add(mitem);
    mnu.show(invoker, x, y);
  }

  URL linkImageURL = getClass().getResource("/images/link.gif");

  Map<String, OptionBox> optSet = new java.util.LinkedHashMap<>();

  Map<String, ParamBox> paramSet = new java.util.LinkedHashMap<>();

  public Map<String, OptionBox> getOptSet()
  {
    return optSet;
  }

  public void setOptSet(Map<String, OptionBox> optSet)
  {
    this.optSet = optSet;
  }

  public Map<String, ParamBox> getParamSet()
  {
    return paramSet;
  }

  public void setParamSet(Map<String, ParamBox> paramSet)
  {
    this.paramSet = paramSet;
  }

  OptsParametersContainerI poparent;

  OptionBox addOption(OptionI opt)
  {
    OptionBox cb = optSet.get(opt.getName());
    if (cb == null)
    {
      cb = new OptionBox(opt);
      optSet.put(opt.getName(), cb);
      // jobOptions.add(cb, FlowLayout.LEFT);
    }
    return cb;
  }

  ParamBox addParameter(ParameterI arg)
  {
    ParamBox pb = paramSet.get(arg.getName());
    if (pb == null)
    {
      pb = new ParamBox(poparent, arg);
      paramSet.put(arg.getName(), pb);
      // paramList.add(pb);
    }
    pb.init();
    // take the defaults from the parameter
    pb.updateControls(arg);
    return pb;
  }

  void selectOption(OptionI option, String string)
  {
    OptionBox cb = optSet.get(option.getName());
    if (cb == null)
    {
      cb = addOption(option);
    }
    cb.enabled.setSelected(string != null); // initial state for an option.
    if (string != null)
    {
      if (option.getPossibleValues().contains(string))
      {
        cb.val.setSelectedItem(string);
      }
      else
      {
        throw new Error(MessageManager.formatMessage(
                "error.invalid_value_for_option", new String[]
                { string, option.getName() }));
      }

    }
    if (option.isRequired() && !cb.enabled.isSelected())
    {
      // TODO: indicate paramset is not valid.. option needs to be selected!
    }
    cb.setInitialValue();
  }

  void setParameter(ParameterI arg)
  {
    ParamBox pb = paramSet.get(arg.getName());
    if (pb == null)
    {
      addParameter(arg);
    }
    else
    {
      pb.updateControls(arg);
    }

  }

  /**
   * recover options and parameters from GUI
   * 
   * @return
   */
  public List<ArgumentI> getCurrentSettings()
  {
    List<ArgumentI> argSet = new ArrayList<>();
    for (OptionBox opts : getOptSet().values())
    {
      OptionI opt = opts.getOptionIfEnabled();
      if (opt != null)
      {
        argSet.add(opt);
      }
    }
    for (ParamBox parambox : getParamSet().values())
    {
      ParameterI parm = parambox.getParameter();
      if (parm != null)
      {
        argSet.add(parm);
      }
    }

    return argSet;
  }

}
