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

import jalview.api.FeatureColourI;
import jalview.api.FeatureSettingsControllerI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;
import jalview.viewmodel.seqfeatures.FeatureRendererModel.FeatureSettingsBean;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureSettings extends Panel
        implements ItemListener, MouseListener, MouseMotionListener,
        AdjustmentListener, FeatureSettingsControllerI
{
  FeatureRenderer fr;

  AlignmentPanel ap;

  AlignViewport av;

  Frame frame;

  Panel groupPanel;

  Panel featurePanel = new Panel();

  ScrollPane scrollPane;

  Image linkImage;

  Scrollbar transparency;

  public FeatureSettings(final AlignmentPanel ap)
  {
    this.ap = ap;
    this.av = ap.av;
    ap.av.featureSettings = this;
    fr = ap.seqPanel.seqCanvas.getFeatureRenderer();

    transparency = new Scrollbar(Scrollbar.HORIZONTAL,
            100 - (int) (fr.getTransparency() * 100), 1, 1, 100);

    transparency.addAdjustmentListener(this);

    java.net.URL url = getClass().getResource("/images/link.gif");
    if (url != null)
    {
      linkImage = java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }

    if (av.isShowSequenceFeatures() || !fr.hasRenderOrder())
    {
      fr.findAllFeatures(true); // was default - now true to make all visible
    }
    groupPanel = new Panel();

    discoverAllFeatureData();

    this.setLayout(new BorderLayout());
    scrollPane = new ScrollPane();
    scrollPane.add(featurePanel);
    if (fr.getAllFeatureColours() != null
            && fr.getAllFeatureColours().size() > 0)
    {
      add(scrollPane, BorderLayout.CENTER);
    }

    Button invert = new Button(
            MessageManager.getString("label.invert_selection"));
    invert.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        invertSelection();
      }
    });

    Panel lowerPanel = new Panel(new GridLayout(2, 1, 5, 10));
    lowerPanel.add(invert);

    Panel tPanel = new Panel(new BorderLayout());

    tPanel.add(transparency, BorderLayout.CENTER);
    tPanel.add(new Label("Transparency"), BorderLayout.EAST);

    lowerPanel.add(tPanel, BorderLayout.SOUTH);

    add(lowerPanel, BorderLayout.SOUTH);

    groupPanel.setLayout(
            new GridLayout((fr.getFeatureGroupsSize()) / 4 + 1, 4)); // JBPNote
                                                                     // - this
                                                                     // was
                                                                     // scaled
                                                                     // on
                                                                     // number
                                                                     // of
                                                                     // visible
                                                                     // groups.
                                                                     // seems
                                                                     // broken
    groupPanel.validate();

    add(groupPanel, BorderLayout.NORTH);

    frame = new Frame();
    frame.add(this);
    final FeatureSettings me = this;
    frame.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        if (me.av.featureSettings == me)
        {
          me.av.featureSettings = null;
          me.ap = null;
          me.av = null;
        }
      }
    });
    int height = featurePanel.getComponentCount() * 50 + 60;

    height = Math.max(200, height);
    height = Math.min(400, height);
    int width = 300;
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.sequence_feature_settings"),
            width, height);
  }

  @Override
  public void paint(Graphics g)
  {
    g.setColor(Color.black);
    g.drawString(MessageManager.getString(
            "label.no_features_added_to_this_alignment"), 10, 20);
    g.drawString(MessageManager.getString(
            "label.features_can_be_added_from_searches_1"), 10, 40);
    g.drawString(MessageManager.getString(
            "label.features_can_be_added_from_searches_2"), 10, 60);
  }

  protected void popupSort(final MyCheckbox check,
          final Map<String, float[][]> minmax, int x, int y)
  {
    final String type = check.type;
    final FeatureColourI typeCol = fr.getFeatureStyle(type);
    PopupMenu men = new PopupMenu(MessageManager
            .formatMessage("label.settings_for_type", new String[]
            { type }));
    java.awt.MenuItem scr = new MenuItem(
            MessageManager.getString("label.sort_by_score"));
    men.add(scr);
    final FeatureSettings me = this;
    scr.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        me.ap.alignFrame.avc
                .sortAlignmentByFeatureScore(Arrays.asList(new String[]
                { type }));
      }

    });
    MenuItem dens = new MenuItem(
            MessageManager.getString("label.sort_by_density"));
    dens.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        me.ap.alignFrame.avc
                .sortAlignmentByFeatureDensity(Arrays.asList(new String[]
                { type }));
      }

    });
    men.add(dens);

    if (minmax != null)
    {
      final float[][] typeMinMax = minmax.get(type);
      /*
       * final java.awt.CheckboxMenuItem chb = new
       * java.awt.CheckboxMenuItem("Vary Height"); // this is broken at the
       * moment chb.setState(minmax.get(type) != null);
       * chb.addActionListener(new ActionListener() {
       * 
       * public void actionPerformed(ActionEvent e) {
       * chb.setState(chb.getState()); if (chb.getState()) { minmax.put(type,
       * null); } else { minmax.put(type, typeMinMax); } }
       * 
       * }); men.add(chb);
       */
      if (typeMinMax != null && typeMinMax[0] != null)
      {
        // graduated colourschemes for those where minmax exists for the
        // positional features
        MenuItem mxcol = new MenuItem(
                (typeCol.isSimpleColour()) ? "Graduated Colour"
                        : "Single Colour");
        men.add(mxcol);
        mxcol.addActionListener(new ActionListener()
        {

          @Override
          public void actionPerformed(ActionEvent e)
          {
            if (typeCol.isSimpleColour())
            {
              new FeatureColourChooser(me, type);
              // write back the current colour object to update the table
              check.updateColor(fr.getFeatureStyle(type));
            }
            else
            {
              new UserDefinedColours(me, check.type, typeCol);
            }
          }

        });
      }
    }

    MenuItem selectContaining = new MenuItem(
            MessageManager.getString("label.select_columns_containing"));
    selectContaining.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        me.ap.alignFrame.avc.markColumnsContainingFeatures(false, false,
                false, type);
      }
    });
    men.add(selectContaining);

    MenuItem selectNotContaining = new MenuItem(MessageManager
            .getString("label.select_columns_not_containing"));
    selectNotContaining.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        me.ap.alignFrame.avc.markColumnsContainingFeatures(true, false,
                false, type);
      }
    });
    men.add(selectNotContaining);

    MenuItem hideContaining = new MenuItem(
            MessageManager.getString("label.hide_columns_containing"));
    hideContaining.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideFeatureColumns(type, true);
      }
    });
    men.add(hideContaining);

    MenuItem hideNotContaining = new MenuItem(
            MessageManager.getString("label.hide_columns_not_containing"));
    hideNotContaining.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideFeatureColumns(type, false);
      }
    });
    men.add(hideNotContaining);

    this.featurePanel.add(men);
    men.show(this.featurePanel, x, y);
  }

  @Override
  public void discoverAllFeatureData()
  {
    if (fr.getAllFeatureColours() != null
            && fr.getAllFeatureColours().size() > 0)
    {
      // rebuildGroups();

    }
    resetTable(false);
  }

  /**
   * Answers the visibility of the given group, and adds a checkbox for it if
   * there is not one already
   */
  public boolean checkGroupState(String group)
  {
    boolean visible = fr.checkGroupVisibility(group, true);

    /*
     * is there already a checkbox for this group?
     */
    for (int g = 0; g < groupPanel.getComponentCount(); g++)
    {
      if (((Checkbox) groupPanel.getComponent(g)).getLabel().equals(group))
      {
        ((Checkbox) groupPanel.getComponent(g)).setState(visible);
        return visible;
      }
    }

    /*
     * add a new checkbox
     */
    Checkbox check = new MyCheckbox(group, visible, false);
    check.addMouseListener(this);
    check.setFont(new Font("Serif", Font.BOLD, 12));
    check.addItemListener(groupItemListener);
    groupPanel.add(check);

    groupPanel.validate();
    return visible;
  }

  // This routine adds and removes checkboxes depending on
  // Group selection states
  void resetTable(boolean groupsChanged)
  {
    List<String> displayableTypes = new ArrayList<>();
    Set<String> foundGroups = new HashSet<>();

    AlignmentI alignment = av.getAlignment();

    for (int i = 0; i < alignment.getHeight(); i++)
    {
      SequenceI seq = alignment.getSequenceAt(i);

      /*
       * get the sequence's groups for positional features
       * and keep track of which groups are visible
       */
      Set<String> groups = seq.getFeatures().getFeatureGroups(true);
      Set<String> visibleGroups = new HashSet<>();
      for (String group : groups)
      {
        // if (group == null || fr.checkGroupVisibility(group, true))
        if (group == null || checkGroupState(group))
        {
          visibleGroups.add(group);
        }
      }
      foundGroups.addAll(groups);

      /*
       * get distinct feature types for visible groups
       * record distinct visible types
       */
      Set<String> types = seq.getFeatures().getFeatureTypesForGroups(true,
              visibleGroups.toArray(new String[visibleGroups.size()]));
      displayableTypes.addAll(types);
    }

    /*
     * remove any checkboxes for groups not present
     */
    pruneGroups(foundGroups);

    Component[] comps;
    int cSize = featurePanel.getComponentCount();
    MyCheckbox check;
    // This will remove any checkboxes which shouldn't be
    // visible
    for (int i = 0; i < cSize; i++)
    {
      comps = featurePanel.getComponents();
      check = (MyCheckbox) comps[i];
      if (!displayableTypes.contains(check.type))
      {
        featurePanel.remove(i);
        cSize--;
        i--;
      }
    }

    if (fr.getRenderOrder() != null)
    {
      // First add the checks in the previous render order,
      // in case the window has been closed and reopened
      List<String> rol = fr.getRenderOrder();
      for (int ro = rol.size() - 1; ro > -1; ro--)
      {
        String item = rol.get(ro);

        if (!displayableTypes.contains(item))
        {
          continue;
        }

        displayableTypes.remove(item);

        addCheck(false, item);
      }
    }

    /*
     * now add checkboxes which should be visible,
     * if they have not already been added
     */
    for (String type : displayableTypes)
    {
      addCheck(groupsChanged, type);
    }

    featurePanel.setLayout(
            new GridLayout(featurePanel.getComponentCount(), 1, 10, 5));
    featurePanel.validate();

    if (scrollPane != null)
    {
      scrollPane.validate();
    }

    itemStateChanged(null);
  }

  /**
   * Remove from the groups panel any checkboxes for groups that are not in the
   * foundGroups set. This enables removing a group from the display when the
   * last feature in that group is deleted.
   * 
   * @param foundGroups
   */
  protected void pruneGroups(Set<String> foundGroups)
  {
    for (int g = 0; g < groupPanel.getComponentCount(); g++)
    {
      Checkbox checkbox = (Checkbox) groupPanel.getComponent(g);
      if (!foundGroups.contains(checkbox.getLabel()))
      {
        groupPanel.remove(checkbox);
      }
    }
  }

  /**
   * update the checklist of feature types with the given type
   * 
   * @param groupsChanged
   *          true means if the type is not in the display list then it will be
   *          added and displayed
   * @param type
   *          feature type to be checked for in the list.
   */
  void addCheck(boolean groupsChanged, String type)
  {
    boolean addCheck;
    Component[] comps = featurePanel.getComponents();
    MyCheckbox check;
    addCheck = true;
    for (int i = 0; i < featurePanel.getComponentCount(); i++)
    {
      check = (MyCheckbox) comps[i];
      if (check.type.equals(type))
      {
        addCheck = false;
        break;
      }
    }

    if (addCheck)
    {
      boolean selected = false;
      if (groupsChanged || av.getFeaturesDisplayed().isVisible(type))
      {
        selected = true;
      }

      check = new MyCheckbox(type, selected, false,
              fr.getFeatureStyle(type));

      check.addMouseListener(this);
      check.addMouseMotionListener(this);
      check.addItemListener(this);
      if (groupsChanged)
      {
        // add at beginning of stack.
        featurePanel.add(check, 0);
      }
      else
      {
        // add at end of stack.
        featurePanel.add(check);
      }
    }
  }

  protected void invertSelection()
  {
    for (int i = 0; i < featurePanel.getComponentCount(); i++)
    {
      Checkbox check = (Checkbox) featurePanel.getComponent(i);
      check.setState(!check.getState());
    }
    selectionChanged(true);
  }

  private ItemListener groupItemListener = new ItemListener()
  {
    @Override
    public void itemStateChanged(ItemEvent evt)
    {
      Checkbox source = (Checkbox) evt.getSource();
      fr.setGroupVisibility(source.getLabel(), source.getState());
      ap.seqPanel.seqCanvas.repaint();
      if (ap.overviewPanel != null)
      {
        ap.overviewPanel.updateOverviewImage();
      }
      resetTable(true);
      return;
    };
  };

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    selectionChanged(true);
  }

  void selectionChanged(boolean updateOverview)
  {
    Component[] comps = featurePanel.getComponents();
    int cSize = comps.length;
    FeatureSettingsBean[] rowData = new FeatureSettingsBean[cSize];
    int i = 0;
    for (Component comp : comps)
    {
      MyCheckbox check = (MyCheckbox) comp;
      // feature filter set to null as not (yet) offered in applet
      FeatureColourI colour = fr.getFeatureStyle(check.type);
      rowData[i] = new FeatureSettingsBean(check.type, colour, null,
              check.getState());
      i++;
    }

    fr.setFeaturePriority(rowData);

    ap.paintAlignment(updateOverview, updateOverview);
  }

  MyCheckbox selectedCheck;

  boolean dragging = false;

  @Override
  public void mouseDragged(MouseEvent evt)
  {
    if (((Component) evt.getSource()).getParent() != featurePanel)
    {
      return;
    }
    dragging = true;
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    if (((Component) evt.getSource()).getParent() != featurePanel)
    {
      return;
    }

    Component comp = null;
    Checkbox target = null;

    int height = evt.getY() + evt.getComponent().getLocation().y;

    if (height > featurePanel.getSize().height)
    {

      comp = featurePanel
              .getComponent(featurePanel.getComponentCount() - 1);
    }
    else if (height < 0)
    {
      comp = featurePanel.getComponent(0);
    }
    else
    {
      comp = featurePanel.getComponentAt(evt.getX(),
              evt.getY() + evt.getComponent().getLocation().y);
    }

    if (comp != null && comp instanceof Checkbox)
    {
      target = (Checkbox) comp;
    }

    if (selectedCheck != null && target != null && selectedCheck != target)
    {
      int targetIndex = -1;
      for (int i = 0; i < featurePanel.getComponentCount(); i++)
      {
        if (target == featurePanel.getComponent(i))
        {
          targetIndex = i;
          break;
        }
      }

      featurePanel.remove(selectedCheck);
      featurePanel.add(selectedCheck, targetIndex);
      featurePanel.validate();
      itemStateChanged(null);
    }
  }

  public void setUserColour(String feature, FeatureColourI originalColour)
  {
    fr.setColour(feature, originalColour);
    refreshTable();
  }

  public void refreshTable()
  {
    featurePanel.removeAll();
    resetTable(false);
    ap.paintAlignment(true, true);
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
    MyCheckbox check = (MyCheckbox) evt.getSource();
    if ((evt.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0)
    {
      this.popupSort(check, fr.getMinMax(), evt.getX(), evt.getY());
    }

    if (check.getParent() != featurePanel)
    {
      return;
    }

    if (evt.getClickCount() > 1)
    {
      FeatureColourI fcol = fr.getFeatureStyle(check.type);
      if (fcol.isSimpleColour())
      {
        new UserDefinedColours(this, check.type, fcol.getColour());
      }
      else
      {
        new FeatureColourChooser(this, check.type);
        // write back the current colour object to update the table
        check.updateColor(fr.getFeatureStyle(check.type));
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
  }

  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    fr.setTransparency((100 - transparency.getValue()) / 100f);
    ap.paintAlignment(true, true);
  }

  class MyCheckbox extends Checkbox
  {
    public String type;

    public int stringWidth;

    boolean hasLink;

    FeatureColourI col;

    public void updateColor(FeatureColourI newcol)
    {
      col = newcol;
      if (col.isSimpleColour())
      {
        setBackground(col.getColour());
      }
      else
      {
        String vlabel = type;
        if (col.isAboveThreshold())
        {
          vlabel += " (>)";
        }
        else if (col.isBelowThreshold())
        {
          vlabel += " (<)";
        }
        if (col.isColourByLabel())
        {
          setBackground(Color.white);
          vlabel += " (by Label)";
        }
        else
        {
          setBackground(col.getMinColour());
        }
        this.setLabel(vlabel);
      }
      repaint();
    }

    public MyCheckbox(String label, boolean checked, boolean haslink)
    {
      super(label, checked);
      type = label;
      FontMetrics fm = av.nullFrame.getFontMetrics(av.nullFrame.getFont());
      stringWidth = fm.stringWidth(label);
      this.hasLink = haslink;
    }

    public MyCheckbox(String type, boolean selected, boolean b,
            FeatureColourI featureStyle)
    {
      this(type, selected, b);
      updateColor(featureStyle);
    }

    @Override
    public void paint(Graphics g)
    {
      Dimension d = getSize();
      if (col != null)
      {
        if (col.isColourByLabel())
        {
          g.setColor(Color.white);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);
          /*
           * g.setColor(Color.black); Font f=g.getFont().deriveFont(9);
           * g.setFont(f);
           * 
           * // g.setFont(g.getFont().deriveFont( //
           * AffineTransform.getScaleInstance( //
           * width/g.getFontMetrics().stringWidth("Label"), //
           * height/g.getFontMetrics().getHeight()))); g.drawString("Label",
           * width/2, 0);
           */

        }
        else if (col.isGraduatedColour())
        {
          Color maxCol = col.getMaxColour();
          g.setColor(maxCol);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);

        }
      }

      if (hasLink)
      {
        g.drawImage(linkImage, stringWidth + 25,
                (getSize().height - linkImage.getHeight(this)) / 2, this);
      }
    }
  }

  /**
   * Hide columns containing (or not containing) a given feature type
   * 
   * @param type
   * @param columnsContaining
   */
  void hideFeatureColumns(final String type, boolean columnsContaining)
  {
    if (ap.alignFrame.avc.markColumnsContainingFeatures(columnsContaining,
            false, false, type))
    {
      if (ap.alignFrame.avc.markColumnsContainingFeatures(
              !columnsContaining, false, false, type))
      {
        ap.alignFrame.viewport.hideSelectedColumns();
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

}
