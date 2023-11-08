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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.help.HelpSetException;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import jalview.api.AlignViewControllerGuiI;
import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.api.FeatureSettingsControllerI;
import jalview.api.SplitContainerI;
import jalview.api.ViewStyleI;
import jalview.controller.FeatureSettingsControllerGuiI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcher;
import jalview.datamodel.features.FeatureMatcherI;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.gui.Help.HelpId;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.schemes.FeatureColour;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.seqfeatures.FeatureRendererModel.FeatureSettingsBean;
import jalview.viewmodel.styles.ViewStyle;
import jalview.xml.binding.jalview.JalviewUserColours;
import jalview.xml.binding.jalview.JalviewUserColours.Colour;
import jalview.xml.binding.jalview.JalviewUserColours.Filter;
import jalview.xml.binding.jalview.ObjectFactory;

public class FeatureSettings extends JPanel
        implements FeatureSettingsControllerI, FeatureSettingsControllerGuiI
{
  private static final String SEQUENCE_FEATURE_COLOURS = MessageManager
          .getString("label.sequence_feature_colours");

  /*
   * column indices of fields in Feature Settings table
   */
  static final int TYPE_COLUMN = 0;

  static final int COLOUR_COLUMN = 1;

  static final int FILTER_COLUMN = 2;

  static final int SHOW_COLUMN = 3;

  private static final int COLUMN_COUNT = 4;

  private static final int MIN_WIDTH = 400;

  private static final int MIN_HEIGHT = 400;

  private final static String BASE_TOOLTIP = MessageManager
          .getString("label.click_to_edit");

  final FeatureRenderer fr;

  public final AlignFrame af;

  /*
   * 'original' fields hold settings to restore on Cancel
   */
  Object[][] originalData;

  private float originalTransparency;

  private ViewStyleI originalViewStyle;

  private Map<String, FeatureMatcherSetI> originalFilters;

  final JInternalFrame frame;

  JScrollPane scrollPane = new JScrollPane();

  JTable table;

  JPanel groupPanel;

  JSlider transparency = new JSlider();

  private JCheckBox showComplementOnTop;

  private JCheckBox showComplement;

  /*
   * when true, constructor is still executing - so ignore UI events
   */
  protected volatile boolean inConstruction = true;

  int selectedRow = -1;

  boolean resettingTable = false;

  /*
   * true when Feature Settings are updating from feature renderer
   */
  private boolean handlingUpdate = false;

  /*
   * a change listener to ensure the dialog is updated if
   * FeatureRenderer discovers new features
   */
  private PropertyChangeListener change;

  /*
   * holds {featureCount, totalExtent} for each feature type
   */
  Map<String, float[]> typeWidth = null;

  private void storeOriginalSettings()
  {
    // save transparency for restore on Cancel
    originalTransparency = fr.getTransparency();

    updateTransparencySliderFromFR();

    originalFilters = new HashMap<>(fr.getFeatureFilters()); // shallow copy
    originalViewStyle = new ViewStyle(af.viewport.getViewStyle());
  }

  private void updateTransparencySliderFromFR()
  {
    boolean incon = inConstruction;
    inConstruction = true;

    int transparencyAsPercent = (int) (fr.getTransparency() * 100);
    transparency.setValue(100 - transparencyAsPercent);
    inConstruction = incon;
  }

  /**
   * Constructor
   * 
   * @param af
   */
  public FeatureSettings(AlignFrame alignFrame)
  {
    this.af = alignFrame;
    fr = af.getFeatureRenderer();

    storeOriginalSettings();

    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    table = new JTable()
    {
      @Override
      public String getToolTipText(MouseEvent e)
      {
        String tip = null;
        int column = table.columnAtPoint(e.getPoint());
        int row = table.rowAtPoint(e.getPoint());

        switch (column)
        {
        case TYPE_COLUMN:
          tip = JvSwingUtils.wrapTooltip(true, MessageManager
                  .getString("label.feature_settings_click_drag"));
          break;
        case COLOUR_COLUMN:
          FeatureColourI colour = (FeatureColourI) table.getValueAt(row,
                  column);
          tip = getColorTooltip(colour, true);
          break;
        case FILTER_COLUMN:
          FeatureMatcherSet o = (FeatureMatcherSet) table.getValueAt(row,
                  column);
          tip = o.isEmpty()
                  ? MessageManager
                          .getString("label.configure_feature_tooltip")
                  : o.toString();
          break;
        default:
          break;
        }

        return tip;
      }

      /**
       * Position the tooltip near the bottom edge of, and half way across, the
       * current cell
       */
      @Override
      public Point getToolTipLocation(MouseEvent e)
      {
        Point point = e.getPoint();
        int column = table.columnAtPoint(point);
        int row = table.rowAtPoint(point);
        Rectangle r = getCellRect(row, column, false);
        Point loc = new Point(r.x + r.width / 2, r.y + r.height - 3);
        return loc;
      }
    };
    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setFont(new Font("Verdana", Font.PLAIN, 12));
    tableHeader.setReorderingAllowed(false);
    table.setFont(new Font("Verdana", Font.PLAIN, 12));
    ToolTipManager.sharedInstance().registerComponent(table);
    table.setDefaultEditor(FeatureColour.class, new ColorEditor());
    table.setDefaultRenderer(FeatureColour.class, new ColorRenderer());

    table.setDefaultEditor(FeatureMatcherSet.class, new FilterEditor());
    table.setDefaultRenderer(FeatureMatcherSet.class, new FilterRenderer());

    TableColumn colourColumn = new TableColumn(COLOUR_COLUMN, 75,
            new ColorRenderer(), new ColorEditor());
    table.addColumn(colourColumn);

    TableColumn filterColumn = new TableColumn(FILTER_COLUMN, 75,
            new FilterRenderer(), new FilterEditor());
    table.addColumn(filterColumn);

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    table.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent evt)
      {
        Point pt = evt.getPoint();
        selectedRow = table.rowAtPoint(pt);
        String type = (String) table.getValueAt(selectedRow, TYPE_COLUMN);
        if (evt.isPopupTrigger())
        {
          Object colour = table.getValueAt(selectedRow, COLOUR_COLUMN);
          showPopupMenu(selectedRow, type, colour, evt.getPoint());
        }
        else if (evt.getClickCount() == 2
                && table.columnAtPoint(pt) == TYPE_COLUMN)
        {
          boolean invertSelection = evt.isAltDown();
          boolean toggleSelection = Platform.isControlDown(evt);
          boolean extendSelection = evt.isShiftDown();
          fr.ap.alignFrame.avc.markColumnsContainingFeatures(
                  invertSelection, extendSelection, toggleSelection, type);
          fr.ap.av.sendSelection();
        }
      }

      // isPopupTrigger fires on mouseReleased on Windows
      @Override
      public void mouseReleased(MouseEvent evt)
      {
        selectedRow = table.rowAtPoint(evt.getPoint());
        if (evt.isPopupTrigger())
        {
          String type = (String) table.getValueAt(selectedRow, TYPE_COLUMN);
          Object colour = table.getValueAt(selectedRow, COLOUR_COLUMN);
          showPopupMenu(selectedRow, type, colour, evt.getPoint());
        }
      }
    });

    table.addMouseMotionListener(new MouseMotionAdapter()
    {
      @Override
      public void mouseDragged(MouseEvent evt)
      {
        int newRow = table.rowAtPoint(evt.getPoint());
        if (newRow != selectedRow && selectedRow != -1 && newRow != -1)
        {
          /*
           * reposition 'selectedRow' to 'newRow' (the dragged to location)
           * this could be more than one row away for a very fast drag action
           * so just swap it with adjacent rows until we get it there
           */
          Object[][] data = ((FeatureTableModel) table.getModel())
                  .getData();
          int direction = newRow < selectedRow ? -1 : 1;
          for (int i = selectedRow; i != newRow; i += direction)
          {
            Object[] temp = data[i];
            data[i] = data[i + direction];
            data[i + direction] = temp;
          }
          updateFeatureRenderer(data);
          table.repaint();
          selectedRow = newRow;
        }
      }
    });
    // table.setToolTipText(JvSwingUtils.wrapTooltip(true,
    // MessageManager.getString("label.feature_settings_click_drag")));
    scrollPane.setViewportView(table);

    if (af.getViewport().isShowSequenceFeatures() || !fr.hasRenderOrder())
    {
      fr.findAllFeatures(true); // display everything!
    }

    discoverAllFeatureData();
    final FeatureSettings fs = this;
    fr.addPropertyChangeListener(change = new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (!fs.resettingTable && !fs.handlingUpdate)
        {
          fs.handlingUpdate = true;
          fs.resetTable(null);
          // new groups may be added with new sequence feature types only
          fs.handlingUpdate = false;
        }
      }

    });

    SplitContainerI splitframe = af.getSplitViewContainer();
    if (splitframe != null)
    {
      frame = null; // keeps eclipse happy
      splitframe.addFeatureSettingsUI(this);
    }
    else
    {
      frame = new JInternalFrame();
      frame.setContentPane(this);
      frame.setFrameIcon(null);
      Rectangle bounds = af.getFeatureSettingsGeometry();
      String title;
      if (af.getAlignPanels().size() > 1 || Desktop.getAlignmentPanels(
              af.alignPanel.av.getSequenceSetId()).length > 1)
      {
        title = MessageManager.formatMessage(
                "label.sequence_feature_settings_for_view",
                af.alignPanel.getViewName());
      }
      else
      {
        title = MessageManager.getString("label.sequence_feature_settings");
      }
      if (bounds == null)
      {
        if (Platform.isAMacAndNotJS())
        {
          Desktop.addInternalFrame(frame, title, 600, 480);
        }
        else
        {
          Desktop.addInternalFrame(frame, title, 600, 450);
        }
      }
      else
      {
        Desktop.addInternalFrame(frame, title, false, bounds.width,
                bounds.height);
        frame.setBounds(bounds);
        frame.setVisible(true);
      }
      frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

      frame.addInternalFrameListener(
              new javax.swing.event.InternalFrameAdapter()
              {
                @Override
                public void internalFrameClosed(
                        javax.swing.event.InternalFrameEvent evt)
                {
                  featureSettings_isClosed();
                };
              });
      frame.setLayer(JLayeredPane.PALETTE_LAYER);
    }
    inConstruction = false;
  }

  /**
   * Sets the state of buttons to show complement features from viewport
   * settings
   */
  private void updateComplementButtons()
  {
    showComplement.setSelected(af.getViewport().isShowComplementFeatures());
    showComplementOnTop
            .setSelected(af.getViewport().isShowComplementFeaturesOnTop());
  }

  @Override
  public AlignViewControllerGuiI getAlignframe()
  {
    return af;
  }

  @Override
  public void featureSettings_isClosed()
  {
    fr.removePropertyChangeListener(change);
    change = null;
  }

  /**
   * Constructs and shows a popup menu of possible actions on the selected row
   * and feature type
   * 
   * @param rowSelected
   * @param type
   * @param typeCol
   * @param pt
   */
  protected void showPopupMenu(final int rowSelected, final String type,
          final Object typeCol, final Point pt)
  {
    JPopupMenu men = new JPopupMenu(MessageManager
            .formatMessage("label.settings_for_param", new String[]
            { type }));

    JMenuItem scr = new JMenuItem(
            MessageManager.getString("label.sort_by_score"));
    men.add(scr);
    scr.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortByScore(Arrays.asList(new String[] { type }));
      }
    });
    JMenuItem dens = new JMenuItem(
            MessageManager.getString("label.sort_by_density"));
    dens.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortByDensity(Arrays.asList(new String[] { type }));
      }
    });
    men.add(dens);

    JMenuItem selCols = new JMenuItem(
            MessageManager.getString("label.select_columns_containing"));
    selCols.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        fr.ap.alignFrame.avc.markColumnsContainingFeatures(false, false,
                false, type);
        fr.ap.av.sendSelection();
      }
    });
    JMenuItem clearCols = new JMenuItem(MessageManager
            .getString("label.select_columns_not_containing"));
    clearCols.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        fr.ap.alignFrame.avc.markColumnsContainingFeatures(true, false,
                false, type);
        fr.ap.av.sendSelection();
      }
    });
    JMenuItem hideCols = new JMenuItem(
            MessageManager.getString("label.hide_columns_containing"));
    hideCols.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        fr.ap.alignFrame.hideFeatureColumns(type, true);
        fr.ap.av.sendSelection();
      }
    });
    JMenuItem hideOtherCols = new JMenuItem(
            MessageManager.getString("label.hide_columns_not_containing"));
    hideOtherCols.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        fr.ap.alignFrame.hideFeatureColumns(type, false);
        fr.ap.av.sendSelection();
      }
    });
    men.add(selCols);
    men.add(clearCols);
    men.add(hideCols);
    men.add(hideOtherCols);
    men.show(table, pt.x, pt.y);
  }

  /**
   * Sort the sequences in the alignment by the number of features for the given
   * feature types (or all features if null)
   * 
   * @param featureTypes
   */
  protected void sortByDensity(List<String> featureTypes)
  {
    af.avc.sortAlignmentByFeatureDensity(featureTypes);
  }

  /**
   * Sort the sequences in the alignment by average score for the given feature
   * types (or all features if null)
   * 
   * @param featureTypes
   */
  protected void sortByScore(List<String> featureTypes)
  {
    af.avc.sortAlignmentByFeatureScore(featureTypes);
  }

  /**
   * Returns true if at least one feature type is visible. Else shows a warning
   * dialog and returns false.
   * 
   * @param title
   * @return
   */
  private boolean canSortBy(String title)
  {
    if (fr.getDisplayedFeatureTypes().isEmpty())
    {
      JvOptionPane.showMessageDialog(this,
              MessageManager.getString("label.no_features_to_sort_by"),
              title, JvOptionPane.OK_OPTION);
      return false;
    }
    return true;
  }

  @Override
  synchronized public void discoverAllFeatureData()
  {
    Set<String> allGroups = new HashSet<>();
    AlignmentI alignment = af.getViewport().getAlignment();

    for (int i = 0; i < alignment.getHeight(); i++)
    {
      SequenceI seq = alignment.getSequenceAt(i);
      for (String group : seq.getFeatures().getFeatureGroups(true))
      {
        if (group != null && !allGroups.contains(group))
        {
          allGroups.add(group);
          checkGroupState(group);
        }
      }
    }

    resetTable(null);

    validate();
  }

  /**
   * Synchronise gui group list and check visibility of group
   * 
   * @param group
   * @return true if group is visible
   */
  private boolean checkGroupState(String group)
  {
    boolean visible = fr.checkGroupVisibility(group, true);

    for (int g = 0; g < groupPanel.getComponentCount(); g++)
    {
      if (((JCheckBox) groupPanel.getComponent(g)).getText().equals(group))
      {
        ((JCheckBox) groupPanel.getComponent(g)).setSelected(visible);
        return visible;
      }
    }

    final String grp = group;
    final JCheckBox check = new JCheckBox(group, visible);
    check.setFont(new Font("Serif", Font.BOLD, 12));
    check.setToolTipText(group);
    check.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        fr.setGroupVisibility(check.getText(), check.isSelected());
        resetTable(new String[] { grp });
        refreshDisplay();
      }
    });
    groupPanel.add(check);
    return visible;
  }

  synchronized void resetTable(String[] groupChanged)
  {
    if (resettingTable)
    {
      return;
    }
    resettingTable = true;
    typeWidth = new Hashtable<>();
    // TODO: change avWidth calculation to 'per-sequence' average and use long
    // rather than float

    Set<String> displayableTypes = new HashSet<>();
    Set<String> foundGroups = new HashSet<>();

    /*
     * determine which feature types may be visible depending on 
     * which groups are selected, and recompute average width data
     */
    for (int i = 0; i < af.getViewport().getAlignment().getHeight(); i++)
    {

      SequenceI seq = af.getViewport().getAlignment().getSequenceAt(i);

      /*
       * get the sequence's groups for positional features
       * and keep track of which groups are visible
       */
      Set<String> groups = seq.getFeatures().getFeatureGroups(true);
      Set<String> visibleGroups = new HashSet<>();
      for (String group : groups)
      {
        if (group == null || checkGroupState(group))
        {
          visibleGroups.add(group);
        }
      }
      foundGroups.addAll(groups);

      /*
       * get distinct feature types for visible groups
       * record distinct visible types, and their count and total length
       */
      Set<String> types = seq.getFeatures().getFeatureTypesForGroups(true,
              visibleGroups.toArray(new String[visibleGroups.size()]));
      for (String type : types)
      {
        displayableTypes.add(type);
        float[] avWidth = typeWidth.get(type);
        if (avWidth == null)
        {
          avWidth = new float[2];
          typeWidth.put(type, avWidth);
        }
        // todo this could include features with a non-visible group
        // - do we greatly care?
        // todo should we include non-displayable features here, and only
        // update when features are added?
        avWidth[0] += seq.getFeatures().getFeatureCount(true, type);
        avWidth[1] += seq.getFeatures().getTotalFeatureLength(type);
      }
    }

    Object[][] data = new Object[displayableTypes.size()][COLUMN_COUNT];
    int dataIndex = 0;

    if (fr.hasRenderOrder())
    {
      if (!handlingUpdate)
      {
        fr.findAllFeatures(groupChanged != null); // prod to update
        // colourschemes. but don't
        // affect display
        // First add the checks in the previous render order,
        // in case the window has been closed and reopened
      }
      List<String> frl = fr.getRenderOrder();
      for (int ro = frl.size() - 1; ro > -1; ro--)
      {
        String type = frl.get(ro);

        if (!displayableTypes.contains(type))
        {
          continue;
        }

        data[dataIndex][TYPE_COLUMN] = type;
        data[dataIndex][COLOUR_COLUMN] = fr.getFeatureStyle(type);
        FeatureMatcherSetI featureFilter = fr.getFeatureFilter(type);
        data[dataIndex][FILTER_COLUMN] = featureFilter == null
                ? new FeatureMatcherSet()
                : featureFilter;
        data[dataIndex][SHOW_COLUMN] = Boolean.valueOf(
                af.getViewport().getFeaturesDisplayed().isVisible(type));
        dataIndex++;
        displayableTypes.remove(type);
      }
    }

    /*
     * process any extra features belonging only to 
     * a group which was just selected
     */
    while (!displayableTypes.isEmpty())
    {
      String type = displayableTypes.iterator().next();
      data[dataIndex][TYPE_COLUMN] = type;

      data[dataIndex][COLOUR_COLUMN] = fr.getFeatureStyle(type);
      if (data[dataIndex][COLOUR_COLUMN] == null)
      {
        // "Colour has been updated in another view!!"
        fr.clearRenderOrder();
        return;
      }
      FeatureMatcherSetI featureFilter = fr.getFeatureFilter(type);
      data[dataIndex][FILTER_COLUMN] = featureFilter == null
              ? new FeatureMatcherSet()
              : featureFilter;
      data[dataIndex][SHOW_COLUMN] = Boolean.valueOf(true);
      dataIndex++;
      displayableTypes.remove(type);
    }

    if (originalData == null)
    {
      originalData = new Object[data.length][COLUMN_COUNT];
      for (int i = 0; i < data.length; i++)
      {
        System.arraycopy(data[i], 0, originalData[i], 0, COLUMN_COUNT);
      }
    }
    else
    {
      updateOriginalData(data);
    }

    table.setModel(new FeatureTableModel(data));
    table.getColumnModel().getColumn(0).setPreferredWidth(200);

    groupPanel.setLayout(
            new GridLayout(fr.getFeatureGroupsSize() / 4 + 1, 4));
    pruneGroups(foundGroups);
    groupPanel.validate();

    updateFeatureRenderer(data, groupChanged != null);
    resettingTable = false;
  }

  /**
   * Updates 'originalData' (used for restore on Cancel) if we detect that
   * changes have been made outwith this dialog
   * <ul>
   * <li>a new feature type added (and made visible)</li>
   * <li>a feature colour changed (in the Amend Features dialog)</li>
   * </ul>
   * 
   * @param foundData
   */
  protected void updateOriginalData(Object[][] foundData)
  {
    // todo LinkedHashMap instead of Object[][] would be nice

    Object[][] currentData = ((FeatureTableModel) table.getModel())
            .getData();
    for (Object[] row : foundData)
    {
      String type = (String) row[TYPE_COLUMN];
      boolean found = false;
      for (Object[] current : currentData)
      {
        if (type.equals(current[TYPE_COLUMN]))
        {
          found = true;
          /*
           * currently dependent on object equality here;
           * really need an equals method on FeatureColour
           */
          if (!row[COLOUR_COLUMN].equals(current[COLOUR_COLUMN]))
          {
            /*
             * feature colour has changed externally - update originalData
             */
            for (Object[] original : originalData)
            {
              if (type.equals(original[TYPE_COLUMN]))
              {
                original[COLOUR_COLUMN] = row[COLOUR_COLUMN];
                break;
              }
            }
          }
          break;
        }
      }
      if (!found)
      {
        /*
         * new feature detected - add to original data (on top)
         */
        Object[][] newData = new Object[originalData.length
                + 1][COLUMN_COUNT];
        for (int i = 0; i < originalData.length; i++)
        {
          System.arraycopy(originalData[i], 0, newData[i + 1], 0,
                  COLUMN_COUNT);
        }
        newData[0] = row;
        originalData = newData;
      }
    }
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
      JCheckBox checkbox = (JCheckBox) groupPanel.getComponent(g);
      if (!foundGroups.contains(checkbox.getText()))
      {
        groupPanel.remove(checkbox);
      }
    }
  }

  /**
   * reorder data based on the featureRenderers global priority list.
   * 
   * @param data
   */
  private void ensureOrder(Object[][] data)
  {
    boolean sort = false;
    float[] order = new float[data.length];
    for (int i = 0; i < order.length; i++)
    {
      order[i] = fr.getOrder(data[i][0].toString());
      if (order[i] < 0)
      {
        order[i] = fr.setOrder(data[i][0].toString(), i / order.length);
      }
      if (i > 1)
      {
        sort = sort || order[i - 1] > order[i];
      }
    }
    if (sort)
    {
      jalview.util.QuickSort.sort(order, data);
    }
  }

  /**
   * Offers a file chooser dialog, and then loads the feature colours and
   * filters from file in XML format and unmarshals to Jalview feature settings
   */
  void load()
  {
    JalviewFileChooser chooser = new JalviewFileChooser("fc",
            SEQUENCE_FEATURE_COLOURS);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.load_feature_colours"));
    chooser.setToolTipText(MessageManager.getString("action.load"));
    chooser.setResponseHandler(0, () -> {
      File file = chooser.getSelectedFile();
      load(file);
    });
    chooser.showOpenDialog(this);
  }

  /**
   * Loads feature colours and filters from XML stored in the given file
   * 
   * @param file
   */
  void load(File file)
  {
    try
    {
      InputStreamReader in = new InputStreamReader(
              new FileInputStream(file), "UTF-8");

      JAXBContext jc = JAXBContext
              .newInstance("jalview.xml.binding.jalview");
      javax.xml.bind.Unmarshaller um = jc.createUnmarshaller();
      XMLStreamReader streamReader = XMLInputFactory.newInstance()
              .createXMLStreamReader(in);
      JAXBElement<JalviewUserColours> jbe = um.unmarshal(streamReader,
              JalviewUserColours.class);
      JalviewUserColours jucs = jbe.getValue();

      // JalviewUserColours jucs = JalviewUserColours.unmarshal(in);

      /*
       * load feature colours
       */
      for (int i = jucs.getColour().size() - 1; i >= 0; i--)
      {
        Colour newcol = jucs.getColour().get(i);
        FeatureColourI colour = jalview.project.Jalview2XML
                .parseColour(newcol);
        fr.setColour(newcol.getName(), colour);
        fr.setOrder(newcol.getName(), i / (float) jucs.getColour().size());
      }

      /*
       * load feature filters; loaded filters will replace any that are
       * currently defined, other defined filters are left unchanged 
       */
      for (int i = 0; i < jucs.getFilter().size(); i++)
      {
        Filter filterModel = jucs.getFilter().get(i);
        String featureType = filterModel.getFeatureType();
        FeatureMatcherSetI filter = jalview.project.Jalview2XML
                .parseFilter(featureType, filterModel.getMatcherSet());
        if (!filter.isEmpty())
        {
          fr.setFeatureFilter(featureType, filter);
        }
      }

      /*
       * update feature settings table
       */
      if (table != null)
      {
        resetTable(null);
        Object[][] data = ((FeatureTableModel) table.getModel()).getData();
        ensureOrder(data);
        updateFeatureRenderer(data, false);
        table.repaint();
      }
    } catch (Exception ex)
    {
      System.out.println("Error loading User Colour File\n" + ex);
    }
  }

  /**
   * Offers a file chooser dialog, and then saves the current feature colours
   * and any filters to the selected file in XML format
   */
  void save()
  {
    JalviewFileChooser chooser = new JalviewFileChooser("fc",
            SEQUENCE_FEATURE_COLOURS);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.save_feature_colours"));
    chooser.setToolTipText(MessageManager.getString("action.save"));
    int option = chooser.showSaveDialog(this);
    if (option == JalviewFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile();
      save(file);
    }
  }

  /**
   * Saves feature colours and filters to the given file
   * 
   * @param file
   */
  void save(File file)
  {
    JalviewUserColours ucs = new JalviewUserColours();
    ucs.setSchemeName("Sequence Features");
    try
    {
      PrintWriter out = new PrintWriter(
              new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

      /*
       * sort feature types by colour order, from 0 (highest)
       * to 1 (lowest)
       */
      Set<String> fr_colours = fr.getAllFeatureColours();
      String[] sortedTypes = fr_colours
              .toArray(new String[fr_colours.size()]);
      Arrays.sort(sortedTypes, new Comparator<String>()
      {
        @Override
        public int compare(String type1, String type2)
        {
          return Float.compare(fr.getOrder(type1), fr.getOrder(type2));
        }
      });

      /*
       * save feature colours
       */
      for (String featureType : sortedTypes)
      {
        FeatureColourI fcol = fr.getFeatureStyle(featureType);
        Colour col = jalview.project.Jalview2XML.marshalColour(featureType,
                fcol);
        ucs.getColour().add(col);
      }

      /*
       * save any feature filters
       */
      for (String featureType : sortedTypes)
      {
        FeatureMatcherSetI filter = fr.getFeatureFilter(featureType);
        if (filter != null && !filter.isEmpty())
        {
          Iterator<FeatureMatcherI> iterator = filter.getMatchers()
                  .iterator();
          FeatureMatcherI firstMatcher = iterator.next();
          jalview.xml.binding.jalview.FeatureMatcherSet ms = jalview.project.Jalview2XML
                  .marshalFilter(firstMatcher, iterator, filter.isAnded());
          Filter filterModel = new Filter();
          filterModel.setFeatureType(featureType);
          filterModel.setMatcherSet(ms);
          ucs.getFilter().add(filterModel);
        }
      }
      JAXBContext jaxbContext = JAXBContext
              .newInstance(JalviewUserColours.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.marshal(
              new ObjectFactory().createJalviewUserColours(ucs), out);

      // jaxbMarshaller.marshal(object, pout);
      // marshaller.marshal(object);
      out.flush();

      // ucs.marshal(out);
      out.close();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void invertSelection()
  {
    Object[][] data = ((FeatureTableModel) table.getModel()).getData();
    for (int i = 0; i < data.length; i++)
    {
      data[i][SHOW_COLUMN] = !(Boolean) data[i][SHOW_COLUMN];
    }
    updateFeatureRenderer(data, true);
    table.repaint();
  }

  public void orderByAvWidth()
  {
    if (table == null || table.getModel() == null)
    {
      return;
    }
    Object[][] data = ((FeatureTableModel) table.getModel()).getData();
    float[] width = new float[data.length];
    float[] awidth;
    float max = 0;

    for (int i = 0; i < data.length; i++)
    {
      awidth = typeWidth.get(data[i][TYPE_COLUMN]);
      if (awidth[0] > 0)
      {
        width[i] = awidth[1] / awidth[0];// *awidth[0]*awidth[2]; - better
        // weight - but have to make per
        // sequence, too (awidth[2])
        // if (width[i]==1) // hack to distinguish single width sequences.
      }
      else
      {
        width[i] = 0;
      }
      if (max < width[i])
      {
        max = width[i];
      }
    }
    boolean sort = false;
    for (int i = 0; i < width.length; i++)
    {
      // awidth = (float[]) typeWidth.get(data[i][0]);
      if (width[i] == 0)
      {
        width[i] = fr.getOrder(data[i][TYPE_COLUMN].toString());
        if (width[i] < 0)
        {
          width[i] = fr.setOrder(data[i][TYPE_COLUMN].toString(),
                  i / data.length);
        }
      }
      else
      {
        width[i] /= max; // normalize
        fr.setOrder(data[i][TYPE_COLUMN].toString(), width[i]); // store for
                                                                // later
      }
      if (i > 0)
      {
        sort = sort || width[i - 1] > width[i];
      }
    }
    if (sort)
    {
      jalview.util.QuickSort.sort(width, data);
      // update global priority order
    }

    updateFeatureRenderer(data, false);
    table.repaint();
  }

  /**
   * close ourselves but leave any existing UI handlers (e.g a CDS/Protein
   * tabbed feature settings dialog) intact
   */
  public void closeOldSettings()
  {
    closeDialog(false);
  }

  /**
   * close the feature settings dialog (and any containing frame)
   */
  public void close()
  {
    closeDialog(true);
  }

  private void closeDialog(boolean closeContainingFrame)
  {
    try
    {
      if (frame != null)
      {
        af.setFeatureSettingsGeometry(frame.getBounds());
        frame.setClosed(true);
      }
      else
      {
        SplitContainerI sc = af.getSplitViewContainer();
        sc.closeFeatureSettings(this, closeContainingFrame);
        af.featureSettings = null;
      }
    } catch (Exception exe)
    {
    }

  }

  public void updateFeatureRenderer(Object[][] data)
  {
    updateFeatureRenderer(data, true);
  }

  /**
   * Update the priority order of features; only repaint if this changed the
   * order of visible features
   * 
   * @param data
   * @param visibleNew
   */
  void updateFeatureRenderer(Object[][] data, boolean visibleNew)
  {
    FeatureSettingsBean[] rowData = getTableAsBeans(data);

    if (fr.setFeaturePriority(rowData, visibleNew))
    {
      refreshDisplay();
    }
  }

  /**
   * Converts table data into an array of data beans
   */
  private FeatureSettingsBean[] getTableAsBeans(Object[][] data)
  {
    FeatureSettingsBean[] rowData = new FeatureSettingsBean[data.length];
    for (int i = 0; i < data.length; i++)
    {
      String type = (String) data[i][TYPE_COLUMN];
      FeatureColourI colour = (FeatureColourI) data[i][COLOUR_COLUMN];
      FeatureMatcherSetI theFilter = (FeatureMatcherSetI) data[i][FILTER_COLUMN];
      Boolean isShown = (Boolean) data[i][SHOW_COLUMN];
      rowData[i] = new FeatureSettingsBean(type, colour, theFilter,
              isShown);
    }
    return rowData;
  }

  private void jbInit() throws Exception
  {
    this.setLayout(new BorderLayout());

    final boolean hasComplement = af.getViewport()
            .getCodingComplement() != null;

    JPanel settingsPane = new JPanel();
    settingsPane.setLayout(new BorderLayout());

    JPanel bigPanel = new JPanel();
    bigPanel.setLayout(new BorderLayout());

    groupPanel = new JPanel();
    bigPanel.add(groupPanel, BorderLayout.NORTH);

    JButton invert = new JButton(
            MessageManager.getString("label.invert_selection"));
    invert.setFont(JvSwingUtils.getLabelFont());
    invert.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        invertSelection();
      }
    });

    JButton optimizeOrder = new JButton(
            MessageManager.getString("label.optimise_order"));
    optimizeOrder.setFont(JvSwingUtils.getLabelFont());
    optimizeOrder.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        orderByAvWidth();
      }
    });

    final String byScoreLabel = MessageManager
            .getString("label.seq_sort_by_score");
    JButton sortByScore = new JButton(byScoreLabel);
    sortByScore.setFont(JvSwingUtils.getLabelFont());
    sortByScore.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (canSortBy(byScoreLabel))
        {
          sortByScore(null);
        }
      }
    });
    final String byDensityLabel = MessageManager
            .getString("label.sequence_sort_by_density");
    JButton sortByDens = new JButton(byDensityLabel);
    sortByDens.setFont(JvSwingUtils.getLabelFont());
    sortByDens.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (canSortBy(byDensityLabel))
        {
          sortByDensity(null);
        }
      }
    });

    JButton help = new JButton(MessageManager.getString("action.help"));
    help.setFont(JvSwingUtils.getLabelFont());
    help.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          Help.showHelpWindow(HelpId.SequenceFeatureSettings);
        } catch (HelpSetException e1)
        {
          e1.printStackTrace();
        }
      }
    });
    // Cancel for a SplitFrame should just revert changes to the currently
    // displayed
    // settings. May want to do this for either or both - so need a splitview
    // feature settings cancel/OK.
    JButton cancel = new JButton(MessageManager
            .getString(hasComplement ? "action.revert" : "action.cancel"));
    cancel.setToolTipText(MessageManager.getString(hasComplement
            ? "action.undo_changes_to_feature_settings"
            : "action.undo_changes_to_feature_settings_and_close_the_dialog"));
    cancel.setFont(JvSwingUtils.getLabelFont());
    // TODO: disable cancel (and apply!) until current settings are different
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        revert();
        refreshDisplay();
        if (!hasComplement)
        {
          close();
        }
      }
    });
    // Cancel for the whole dialog should cancel both CDS and Protein.
    // OK for an individual feature settings just applies changes, but dialog
    // remains open
    JButton ok = new JButton(MessageManager
            .getString(hasComplement ? "action.apply" : "action.ok"));
    ok.setFont(JvSwingUtils.getLabelFont());
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (!hasComplement)
        {
          close();
        }
        else
        {
          storeOriginalSettings();
        }
      }
    });

    JButton loadColours = new JButton(
            MessageManager.getString("label.load_colours"));
    loadColours.setFont(JvSwingUtils.getLabelFont());
    loadColours.setToolTipText(
            MessageManager.getString("label.load_colours_tooltip"));
    loadColours.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        load();
      }
    });

    JButton saveColours = new JButton(
            MessageManager.getString("label.save_colours"));
    saveColours.setFont(JvSwingUtils.getLabelFont());
    saveColours.setToolTipText(
            MessageManager.getString("label.save_colours_tooltip"));
    saveColours.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        save();
      }
    });
    transparency.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        if (!inConstruction)
        {
          fr.setTransparency((100 - transparency.getValue()) / 100f);
          refreshDisplay();
        }
      }
    });

    transparency.setMaximum(70);
    transparency.setToolTipText(
            MessageManager.getString("label.transparency_tip"));

    boolean nucleotide = af.getViewport().getAlignment().isNucleotide();
    String text = MessageManager
            .formatMessage("label.show_linked_features",
                    nucleotide
                            ? MessageManager.getString("label.protein")
                                    .toLowerCase(Locale.ROOT)
                            : "CDS");
    showComplement = new JCheckBox(text);
    showComplement.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        af.getViewport()
                .setShowComplementFeatures(showComplement.isSelected());
        refreshDisplay();
      }
    });

    showComplementOnTop = new JCheckBox(
            MessageManager.getString("label.on_top"));
    showComplementOnTop.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        af.getViewport().setShowComplementFeaturesOnTop(
                showComplementOnTop.isSelected());
        refreshDisplay();
      }
    });

    updateComplementButtons();

    JPanel lowerPanel = new JPanel(new GridLayout(1, 2));
    bigPanel.add(lowerPanel, BorderLayout.SOUTH);

    JPanel transbuttons = new JPanel(new GridLayout(5, 1));
    transbuttons.add(optimizeOrder);
    transbuttons.add(invert);
    transbuttons.add(sortByScore);
    transbuttons.add(sortByDens);
    transbuttons.add(help);

    JPanel transPanelLeft = new JPanel(
            new GridLayout(hasComplement ? 4 : 2, 1));
    transPanelLeft.add(new JLabel(" Colour transparency" + ":"));
    transPanelLeft.add(transparency);
    if (hasComplement)
    {
      JPanel cp = new JPanel(new FlowLayout(FlowLayout.LEFT));
      cp.add(showComplement);
      cp.add(showComplementOnTop);
      transPanelLeft.add(cp);
    }
    lowerPanel.add(transPanelLeft);
    lowerPanel.add(transbuttons);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(ok);
    buttonPanel.add(cancel);
    buttonPanel.add(loadColours);
    buttonPanel.add(saveColours);
    bigPanel.add(scrollPane, BorderLayout.CENTER);
    settingsPane.add(bigPanel, BorderLayout.CENTER);
    settingsPane.add(buttonPanel, BorderLayout.SOUTH);
    this.add(settingsPane);
  }

  /**
   * Repaints alignment, structure and overview (if shown). If there is a
   * complementary view which is showing this view's features, then also
   * repaints that.
   */
  void refreshDisplay()
  {
    af.alignPanel.paintAlignment(true, true);
    AlignViewportI complement = af.getViewport().getCodingComplement();
    if (complement != null && complement.isShowComplementFeatures())
    {
      AlignFrame af2 = Desktop.getAlignFrameFor(complement);
      af2.alignPanel.paintAlignment(true, true);
    }
  }

  /**
   * Answers a suitable tooltip to show on the colour cell of the table
   * 
   * @param fcol
   * @param withHint
   *          if true include 'click to edit' and similar text
   * @return
   */
  public static String getColorTooltip(FeatureColourI fcol,
          boolean withHint)
  {
    if (fcol == null)
    {
      return null;
    }
    if (fcol.isSimpleColour())
    {
      return withHint ? BASE_TOOLTIP : null;
    }
    String description = fcol.getDescription();
    description = description.replaceAll("<", "&lt;");
    description = description.replaceAll(">", "&gt;");
    StringBuilder tt = new StringBuilder(description);
    if (withHint)
    {
      tt.append("<br>").append(BASE_TOOLTIP).append("</br>");
    }
    return JvSwingUtils.wrapTooltip(true, tt.toString());
  }

  public static void renderGraduatedColor(JLabel comp, FeatureColourI gcol,
          int w, int h)
  {
    boolean thr = false;
    StringBuilder tx = new StringBuilder();

    if (gcol.isColourByAttribute())
    {
      tx.append(FeatureMatcher
              .toAttributeDisplayName(gcol.getAttributeName()));
    }
    else if (!gcol.isColourByLabel())
    {
      tx.append(MessageManager.getString("label.score"));
    }
    tx.append(" ");
    if (gcol.isAboveThreshold())
    {
      thr = true;
      tx.append(">");
    }
    if (gcol.isBelowThreshold())
    {
      thr = true;
      tx.append("<");
    }
    if (gcol.isColourByLabel())
    {
      if (thr)
      {
        tx.append(" ");
      }
      if (!gcol.isColourByAttribute())
      {
        tx.append("Label");
      }
      comp.setIcon(null);
    }
    else
    {
      Color newColor = gcol.getMaxColour();
      comp.setBackground(newColor);
      // System.err.println("Width is " + w / 2);
      Icon ficon = new FeatureIcon(gcol, comp.getBackground(), w, h, thr);
      comp.setIcon(ficon);
      // tt+="RGB value: Max (" + newColor.getRed() + ", "
      // + newColor.getGreen() + ", " + newColor.getBlue()
      // + ")\nMin (" + minCol.getRed() + ", " + minCol.getGreen()
      // + ", " + minCol.getBlue() + ")");
    }
    comp.setHorizontalAlignment(SwingConstants.CENTER);
    comp.setText(tx.toString());
  }

  // ///////////////////////////////////////////////////////////////////////
  // http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
  // ///////////////////////////////////////////////////////////////////////
  class FeatureTableModel extends AbstractTableModel
  {
    private String[] columnNames = {
        MessageManager.getString("label.feature_type"),
        MessageManager.getString("action.colour"),
        MessageManager.getString("label.configuration"),
        MessageManager.getString("label.show") };

    private Object[][] data;

    FeatureTableModel(Object[][] data)
    {
      this.data = data;
    }

    public Object[][] getData()
    {
      return data;
    }

    public void setData(Object[][] data)
    {
      this.data = data;
    }

    @Override
    public int getColumnCount()
    {
      return columnNames.length;
    }

    public Object[] getRow(int row)
    {
      return data[row];
    }

    @Override
    public int getRowCount()
    {
      return data.length;
    }

    @Override
    public String getColumnName(int col)
    {
      return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col)
    {
      return data[row][col];
    }

    /**
     * Answers the class of column c of the table
     */
    @Override
    public Class<?> getColumnClass(int c)
    {
      switch (c)
      {
      case TYPE_COLUMN:
        return String.class;
      case COLOUR_COLUMN:
        return FeatureColour.class;
      case FILTER_COLUMN:
        return FeatureMatcherSet.class;
      default:
        return Boolean.class;
      }
    }

    @Override
    public boolean isCellEditable(int row, int col)
    {
      return col == 0 ? false : true;
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
      data[row][col] = value;
      fireTableCellUpdated(row, col);
      updateFeatureRenderer(data);
    }

  }

  class ColorRenderer extends JLabel implements TableCellRenderer
  {
    Border unselectedBorder = null;

    Border selectedBorder = null;

    public ColorRenderer()
    {
      setOpaque(true); // MUST do this for background to show up.
      setHorizontalTextPosition(SwingConstants.CENTER);
      setVerticalTextPosition(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable tbl, Object color,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
      FeatureColourI cellColour = (FeatureColourI) color;
      setOpaque(true);
      setBackground(tbl.getBackground());
      if (!cellColour.isSimpleColour())
      {
        Rectangle cr = tbl.getCellRect(row, column, false);
        FeatureSettings.renderGraduatedColor(this, cellColour,
                (int) cr.getWidth(), (int) cr.getHeight());
      }
      else
      {
        this.setText("");
        this.setIcon(null);
        setBackground(cellColour.getColour());
      }
      if (isSelected)
      {
        if (selectedBorder == null)
        {
          selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                  tbl.getSelectionBackground());
        }
        setBorder(selectedBorder);
      }
      else
      {
        if (unselectedBorder == null)
        {
          unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                  tbl.getBackground());
        }
        setBorder(unselectedBorder);
      }

      return this;
    }
  }

  class FilterRenderer extends JLabel implements TableCellRenderer
  {
    javax.swing.border.Border unselectedBorder = null;

    javax.swing.border.Border selectedBorder = null;

    public FilterRenderer()
    {
      setOpaque(true); // MUST do this for background to show up.
      setHorizontalTextPosition(SwingConstants.CENTER);
      setVerticalTextPosition(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable tbl,
            Object filter, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
      FeatureMatcherSetI theFilter = (FeatureMatcherSetI) filter;
      setOpaque(true);
      String asText = theFilter.toString();
      setBackground(tbl.getBackground());
      this.setText(asText);
      this.setIcon(null);

      if (isSelected)
      {
        if (selectedBorder == null)
        {
          selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                  tbl.getSelectionBackground());
        }
        setBorder(selectedBorder);
      }
      else
      {
        if (unselectedBorder == null)
        {
          unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                  tbl.getBackground());
        }
        setBorder(unselectedBorder);
      }

      return this;
    }
  }

  /**
   * update comp using rendering settings from gcol
   * 
   * @param comp
   * @param gcol
   */
  public static void renderGraduatedColor(JLabel comp, FeatureColourI gcol)
  {
    int w = comp.getWidth(), h = comp.getHeight();
    if (w < 20)
    {
      w = (int) comp.getPreferredSize().getWidth();
      h = (int) comp.getPreferredSize().getHeight();
      if (w < 20)
      {
        w = 80;
        h = 12;
      }
    }
    renderGraduatedColor(comp, gcol, w, h);
  }

  @SuppressWarnings("serial")
  class ColorEditor extends AbstractCellEditor
          implements TableCellEditor, ActionListener
  {
    FeatureColourI currentColor;

    FeatureTypeSettings chooser;

    String type;

    JButton button;

    protected static final String EDIT = "edit";

    int rowSelected = 0;

    public ColorEditor()
    {
      // Set up the editor (from the table's point of view),
      // which is a button.
      // This button brings up the color chooser dialog,
      // which is the editor from the user's point of view.
      button = new JButton();
      button.setActionCommand(EDIT);
      button.addActionListener(this);
      button.setBorderPainted(false);
    }

    /**
     * Handles events from the editor button, and from the colour/filters
     * dialog's OK button
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (button == e.getSource())
      {
        if (currentColor.isSimpleColour())
        {
          /*
           * simple colour chooser
           */
          String ttl = MessageManager
                  .formatMessage("label.select_colour_for", type);
          ColourChooserListener listener = new ColourChooserListener()
          {
            @Override
            public void colourSelected(Color c)
            {
              currentColor = new FeatureColour(c);
              table.setValueAt(currentColor, rowSelected, COLOUR_COLUMN);
              fireEditingStopped();
            }

            @Override
            public void cancel()
            {
              fireEditingStopped();
            }
          };
          JalviewColourChooser.showColourChooser(button, ttl,
                  currentColor.getColour(), listener);
        }
        else
        {
          /*
           * variable colour and filters dialog
           */
          chooser = new FeatureTypeSettings(fr, type);
          if (!Platform.isJS())
          /**
           * Java only
           * 
           * @j2sIgnore
           */
          {
            chooser.setRequestFocusEnabled(true);
            chooser.requestFocus();
          }
          chooser.addActionListener(this);
          fireEditingStopped();
        }
      }
      else
      {
        /*
         * after OK in variable colour dialog, any changes to colour 
         * (or filters!) are already set in FeatureRenderer, so just
         * update table data without triggering updateFeatureRenderer
         */
        currentColor = fr.getFeatureColours().get(type);
        FeatureMatcherSetI currentFilter = fr.getFeatureFilter(type);
        if (currentFilter == null)
        {
          currentFilter = new FeatureMatcherSet();
        }
        Object[] data = ((FeatureTableModel) table.getModel())
                .getData()[rowSelected];
        data[COLOUR_COLUMN] = currentColor;
        data[FILTER_COLUMN] = currentFilter;
        fireEditingStopped();
        // SwingJS needs an explicit repaint() here,
        // rather than relying upon no validation having
        // occurred since the stopEditing call was made.
        // Its laying out has not been stopped by the modal frame
        table.validate();
        table.repaint();
      }
    }

    /**
     * Override allows access to this method from anonymous inner classes
     */
    @Override
    protected void fireEditingStopped()
    {
      super.fireEditingStopped();
    }

    // Implement the one CellEditor method that AbstractCellEditor doesn't.
    @Override
    public Object getCellEditorValue()
    {
      return currentColor;
    }

    // Implement the one method defined by TableCellEditor.
    @Override
    public Component getTableCellEditorComponent(JTable theTable,
            Object value, boolean isSelected, int row, int column)
    {
      currentColor = (FeatureColourI) value;
      this.rowSelected = row;
      type = table.getValueAt(row, TYPE_COLUMN).toString();
      button.setOpaque(true);
      button.setBackground(FeatureSettings.this.getBackground());
      if (!currentColor.isSimpleColour())
      {
        JLabel btn = new JLabel();
        btn.setSize(button.getSize());
        FeatureSettings.renderGraduatedColor(btn, currentColor);
        button.setBackground(btn.getBackground());
        button.setIcon(btn.getIcon());
        button.setText(btn.getText());
      }
      else
      {
        button.setText("");
        button.setIcon(null);
        button.setBackground(currentColor.getColour());
      }
      return button;
    }
  }

  /**
   * The cell editor for the Filter column. It displays the text of any filters
   * for the feature type in that row (in full as a tooltip, possible
   * abbreviated as display text). On click in the cell, opens the Feature
   * Display Settings dialog at the Filters tab.
   */
  @SuppressWarnings("serial")
  class FilterEditor extends AbstractCellEditor
          implements TableCellEditor, ActionListener
  {

    FeatureMatcherSetI currentFilter;

    Point lastLocation;

    String type;

    JButton button;

    protected static final String EDIT = "edit";

    int rowSelected = 0;

    public FilterEditor()
    {
      button = new JButton();
      button.setActionCommand(EDIT);
      button.addActionListener(this);
      button.setBorderPainted(false);
    }

    /**
     * Handles events from the editor button
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (button == e.getSource())
      {
        FeatureTypeSettings chooser = new FeatureTypeSettings(fr, type);
        chooser.addActionListener(this);
        chooser.setRequestFocusEnabled(true);
        chooser.requestFocus();
        if (lastLocation != null)
        {
          // todo open at its last position on screen
          chooser.setBounds(lastLocation.x, lastLocation.y,
                  chooser.getWidth(), chooser.getHeight());
          chooser.validate();
        }
        fireEditingStopped();
      }
      else if (e.getSource() instanceof Component)
      {

        /*
         * after OK in variable colour dialog, any changes to filter
         * (or colours!) are already set in FeatureRenderer, so just
         * update table data without triggering updateFeatureRenderer
         */
        FeatureColourI currentColor = fr.getFeatureColours().get(type);
        currentFilter = fr.getFeatureFilter(type);
        if (currentFilter == null)
        {
          currentFilter = new FeatureMatcherSet();
        }

        Object[] data = ((FeatureTableModel) table.getModel())
                .getData()[rowSelected];
        data[COLOUR_COLUMN] = currentColor;
        data[FILTER_COLUMN] = currentFilter;
        fireEditingStopped();
        // SwingJS needs an explicit repaint() here,
        // rather than relying upon no validation having
        // occurred since the stopEditing call was made.
        // Its laying out has not been stopped by the modal frame
        table.validate();
        table.repaint();
      }
    }

    @Override
    public Object getCellEditorValue()
    {
      return currentFilter;
    }

    @Override
    public Component getTableCellEditorComponent(JTable theTable,
            Object value, boolean isSelected, int row, int column)
    {
      currentFilter = (FeatureMatcherSetI) value;
      this.rowSelected = row;
      type = table.getValueAt(row, TYPE_COLUMN).toString();
      button.setOpaque(true);
      button.setBackground(FeatureSettings.this.getBackground());
      button.setText(currentFilter.toString());
      button.setIcon(null);
      return button;
    }
  }

  public boolean isOpen()
  {
    if (af.getSplitViewContainer() != null)
    {
      return af.getSplitViewContainer().isFeatureSettingsOpen();
    }
    return frame != null && !frame.isClosed();
  }

  @Override
  public void revert()
  {
    fr.setTransparency(originalTransparency);
    fr.setFeatureFilters(originalFilters);
    updateFeatureRenderer(originalData);
    af.getViewport().setViewStyle(originalViewStyle);
    updateTransparencySliderFromFR();
    updateComplementButtons();
    refreshDisplay();
  }
}

class FeatureIcon implements Icon
{
  FeatureColourI gcol;

  Color backg;

  boolean midspace = false;

  int width = 50, height = 20;

  int s1, e1; // start and end of midpoint band for thresholded symbol

  Color mpcolour = Color.white;

  FeatureIcon(FeatureColourI gfc, Color bg, int w, int h, boolean mspace)
  {
    gcol = gfc;
    backg = bg;
    width = w;
    height = h;
    midspace = mspace;
    if (midspace)
    {
      s1 = width / 3;
      e1 = s1 * 2;
    }
    else
    {
      s1 = width / 2;
      e1 = s1;
    }
  }

  @Override
  public int getIconWidth()
  {
    return width;
  }

  @Override
  public int getIconHeight()
  {
    return height;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y)
  {

    if (gcol.isColourByLabel())
    {
      g.setColor(backg);
      g.fillRect(0, 0, width, height);
      // need an icon here.
      g.setColor(gcol.getMaxColour());

      g.setFont(new Font("Verdana", Font.PLAIN, 9));

      // g.setFont(g.getFont().deriveFont(
      // AffineTransform.getScaleInstance(
      // width/g.getFontMetrics().stringWidth("Label"),
      // height/g.getFontMetrics().getHeight())));

      g.drawString(MessageManager.getString("label.label"), 0, 0);

    }
    else
    {
      Color minCol = gcol.getMinColour();
      g.setColor(minCol);
      g.fillRect(0, 0, s1, height);
      if (midspace)
      {
        g.setColor(Color.white);
        g.fillRect(s1, 0, e1 - s1, height);
      }
      g.setColor(gcol.getMaxColour());
      // g.fillRect(0, e1, width - e1, height); // BH 2018
      g.fillRect(e1, 0, width - e1, height);
    }
  }
}
