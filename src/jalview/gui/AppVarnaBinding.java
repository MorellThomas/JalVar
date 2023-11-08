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

import jalview.datamodel.SequenceI;
import jalview.ext.varna.JalviewVarnaBinding;
import jalview.structure.AtomSpec;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fr.orsay.lri.varna.VARNAPanel;
import fr.orsay.lri.varna.components.ReorderableJList;
import fr.orsay.lri.varna.exceptions.ExceptionLoadingFailed;
import fr.orsay.lri.varna.exceptions.ExceptionNAViewAlgorithm;
import fr.orsay.lri.varna.exceptions.ExceptionNonEqualLength;
import fr.orsay.lri.varna.models.FullBackup;
import fr.orsay.lri.varna.models.VARNAConfig;
import fr.orsay.lri.varna.models.rna.RNA;

public class AppVarnaBinding extends JalviewVarnaBinding
{
  public VARNAPanel vp;

  protected JPanel _listPanel = new JPanel();

  private ReorderableJList _sideList = null;

  private static String errorOpt = "error";

  @SuppressWarnings("unused")
  private boolean _error;

  private Color _backgroundColor = Color.white;

  private static int _nextID = 1;

  @SuppressWarnings("unused")
  private int _algoCode;

  private BackupHolder _rnaList;

  /**
   * Constructor
   */
  public AppVarnaBinding()
  {
    init();
  }

  /**
   * Constructs the VARNAPanel and an (empty) selection list of structures to
   * show in it
   */
  private void init()
  {
    DefaultListModel<FullBackup> dlm = new DefaultListModel<FullBackup>();

    int marginTools = 40;

    DefaultListSelectionModel m = new DefaultListSelectionModel();
    m.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m.setLeadAnchorNotificationEnabled(false);

    _sideList = new ReorderableJList();
    _sideList.setModel(dlm);
    _sideList.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        AppVarnaBinding.this.mouseClicked(e);
      }
    });
    _sideList.setSelectionModel(m);
    _sideList.setPreferredSize(new Dimension(100, 0));
    _sideList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent evt)
      {
        changeSelectedStructure_actionPerformed(evt);
      }
    });
    _rnaList = new BackupHolder(dlm, _sideList);

    try
    {
      vp = new VARNAPanel("0", ".");
    } catch (ExceptionNonEqualLength e)
    {
      vp.errorDialog(e);
    }
    vp.setPreferredSize(new Dimension(400, 400));

    JScrollPane listScroller = new JScrollPane(_sideList);
    listScroller.setPreferredSize(new Dimension(150, 0));

    vp.setBackground(_backgroundColor);

    JLabel j = new JLabel(
            MessageManager.getString("label.structures_manager"),
            JLabel.CENTER);
    _listPanel.setLayout(new BorderLayout());

    _listPanel.add(j, BorderLayout.NORTH);
    _listPanel.add(listScroller, BorderLayout.CENTER);

    new DropTarget(vp, new DropTargetAdapter()
    {
      @Override
      public void drop(DropTargetDropEvent dtde)
      {
        AppVarnaBinding.this.drop(dtde);
      }
    });
  }

  public JPanel getListPanel()
  {
    return _listPanel;
  }

  /**
   * Returns the currently selected RNA, or null if none selected
   * 
   * @return
   */
  public RNA getSelectedRNA()
  {
    int selectedIndex = _sideList.getSelectedIndex();
    if (selectedIndex < 0)
    {
      return null;
    }
    FullBackup selected = _rnaList.getElementAt(selectedIndex);
    return selected.rna;
  }

  /**
   * Substitute currently selected RNA with the edited one
   * 
   * @param rnaEdit
   */
  public void updateSelectedRNA(RNA rnaEdit)
  {
    vp.repaint();
    vp.showRNA(rnaEdit);
  }

  public static String generateDefaultName()
  {
    return "User file #" + _nextID++;
  }

  public String[][] getParameterInfo()
  {
    String[][] info = {
        // Parameter Name Kind of Value Description,
        { "sequenceDBN", "String", "A raw RNA sequence" },
        { "structureDBN", "String",
            "An RNA structure in dot bracket notation (DBN)" },
        { errorOpt, "boolean", "To show errors" }, };
    return info;
  }

  @SuppressWarnings("unused")
  private Color getSafeColor(String col, Color def)
  {
    Color result;
    try
    {
      result = Color.decode(col);
    } catch (Exception e)
    {
      try
      {
        result = Color.getColor(col, def);
      } catch (Exception e2)
      {
        return def;
      }
    }
    return result;
  }

  public VARNAPanel get_varnaPanel()
  {
    return vp;
  }

  public void set_varnaPanel(VARNAPanel surface)
  {
    vp = surface;
  }

  public void drop(DropTargetDropEvent dtde)
  {
    try
    {
      Transferable tr = dtde.getTransferable();
      DataFlavor[] flavors = tr.getTransferDataFlavors();
      for (int i = 0; i < flavors.length; i++)
      {
        if (flavors[i].isFlavorJavaFileListType())
        {
          dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
          Object ob = tr.getTransferData(flavors[i]);
          if (ob instanceof List)
          {
            List list = (List) ob;
            for (int j = 0; j < list.size(); j++)
            {
              Object o = list.get(j);

              if (dtde.getSource() instanceof DropTarget)
              {
                DropTarget dt = (DropTarget) dtde.getSource();
                Component c = dt.getComponent();
                if (c instanceof VARNAPanel)
                {
                  String path = o.toString();
                  VARNAPanel varnaPanel = (VARNAPanel) c;
                  try
                  {
                    FullBackup bck = VARNAPanel.importSession(path);
                    _rnaList.add(bck.config, bck.rna, bck.name, true);
                  } catch (ExceptionLoadingFailed e3)
                  {
                    int mn = 1;
                    Collection<RNA> mdls = fr.orsay.lri.varna.factories.RNAFactory
                            .loadSecStr(path);
                    for (RNA r : mdls)
                    {
                      r.drawRNA(varnaPanel.getConfig());
                      String name = r.getName();
                      if (name.equals(""))
                      {
                        name = path.substring(
                                path.lastIndexOf(File.separatorChar) + 1);
                      }
                      if (mdls.size() > 1)
                      {
                        name += " (Model " + mn++ + ")";
                      }
                      _rnaList.add(varnaPanel.getConfig().clone(), r, name,
                              true);
                      // BH 2018 SwingJS clone of varnaPanel or its config will
                      // be the object itself, not a clone
                    }
                  }
                }
              }
            }
          }
          // If we made it this far, everything worked.
          dtde.dropComplete(true);
          return;
        }
      }
      // Hmm, the user must not have dropped a file list
      dtde.rejectDrop();
    } catch (Exception e)
    {
      e.printStackTrace();
      dtde.rejectDrop();
    }

  }

  private class BackupHolder
  {
    private DefaultListModel<FullBackup> _rnalist;

    private List<RNA> _rnas = new ArrayList<RNA>();

    JList _l;

    public BackupHolder(DefaultListModel<FullBackup> rnaList, JList l)
    {
      _rnalist = rnaList;
      _l = l;
    }

    public void add(VARNAConfig c, RNA r, String name)
    {
      add(c, r, name, false);
    }

    /**
     * Adds an entry to the end of the selection list and (optionally) sets it
     * as selected
     * 
     * @param c
     * @param r
     * @param name
     * @param select
     */
    public void add(VARNAConfig c, RNA r, String name, boolean select)
    {
      if (select)
      {
        _l.removeSelectionInterval(0, _rnalist.size());
      }
      if (name.equals(""))
      {
        name = generateDefaultName();
      }
      FullBackup bck = new FullBackup(c, r, name);
      _rnas.add(r);
      _rnalist.addElement(bck);
      if (select)
      {
        _l.setSelectedIndex(0);
      }
    }

    public FullBackup getElementAt(int i)
    {
      return _rnalist.getElementAt(i);
    }
  }

  public void mouseClicked(MouseEvent e)
  {
    if (e.getClickCount() == 2)
    {
      int index = _sideList.locationToIndex(e.getPoint());
      ListModel<FullBackup> dlm = _sideList.getModel();
      // FullBackup item = dlm.getElementAt(index);

      _sideList.ensureIndexIsVisible(index);
      /*
       * TODO Object newName = JvOptionPane.showInputDialog( this,
       * "Specify a new name for this RNA", "Rename RNA",
       * JvOptionPane.QUESTION_MESSAGE, (Icon)null, null, item.toString()); if
       * (newName!=null) { item.name = newName.toString();
       * this._sideList.repaint(); }
       */
    }
  }

  @Override
  public String[] getStructureFiles()
  {
    return null;
  }

  @Override
  public void releaseReferences(Object svl)
  {
  }

  @Override
  public void updateColours(Object source)
  {
  }

  @Override
  public void componentHidden(ComponentEvent e)
  {
  }

  @Override
  public void componentMoved(ComponentEvent e)
  {
  }

  @Override
  public void componentResized(ComponentEvent e)
  {
  }

  @Override
  public void componentShown(ComponentEvent e)
  {
  }

  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
  }

  @Override
  public boolean isListeningFor(SequenceI seq)
  {
    return true;
  }

  /**
   * Returns the path to a temporary file containing a representation of the
   * state of the Varna display, or null on any error
   * 
   * @param rna
   * @param jds
   * 
   * @return
   */
  public String getStateInfo(RNA rna)
  {
    if (vp == null)
    {
      return null;
    }

    /*
     * we have to show the RNA we want to save in the viewer; get the currently
     * displayed model first so we can restore it
     */
    FullBackup sel = (FullBackup) _sideList.getSelectedValue();

    FullBackup model = null;
    ListModel models = _sideList.getModel();
    for (int i = 0; i < models.getSize(); i++)
    {
      model = (FullBackup) models.getElementAt(i);
      if (model.rna == rna)
      {
        break;
      }
    }
    if (model == null)
    {
      return null;
    }

    /*
     * switch display
     */
    vp.showRNA(model.rna, model.config);

    try
    {
      File temp;
      temp = File.createTempFile("varna", null);
      temp.deleteOnExit();
      String filePath = temp.getAbsolutePath();
      vp.toXML(filePath);

      /*
       * restore the previous display
       */
      vp.showRNA(sel.rna, sel.config);

      return filePath;
    } catch (IOException e)
    {
      return null;
    }
  }

  public int getSelectedIndex()
  {
    return _sideList.getSelectedIndex();
  }

  /**
   * Switch the Varna display to the structure selected in the left hand panel
   * 
   * @param evt
   */
  protected void changeSelectedStructure_actionPerformed(
          ListSelectionEvent evt)
  {
    if (!evt.getValueIsAdjusting())
    {
      showSelectedStructure();
    }
  }

  /**
   * 
   */
  protected void showSelectedStructure()
  {
    FullBackup sel = (FullBackup) _sideList.getSelectedValue();
    if (sel != null)
    {
      vp.showRNA(sel.rna, sel.config);
    }
  }

  /**
   * Set and display the selected item in the list of structures
   * 
   * @param selectedRna
   */
  public void setSelectedIndex(final int selectedRna)
  {
    /*
     * note this does nothing if, say, selecting item 3 when only 1 has been
     * added on load
     */
    _sideList.setSelectedIndex(selectedRna);
    // TODO ? need a worker thread to get this to happen properly
  }

  /**
   * Add an RNA structure to the selection list
   * 
   * @param rna
   */
  public void addStructure(RNA rna)
  {
    VARNAConfig config = vp.getConfig().clone(); // BH 2018 this will NOT be a
                                                 // clone in SwingJS
    addStructure(rna, config);
  }

  /**
   * @param rna
   * @param config
   */
  protected void addStructure(final RNA rna, final VARNAConfig config)
  {
    drawRna(rna, config);
    _rnaList.add(config, rna, rna.getName());
  }

  /**
   * @param rna
   * @param config
   */
  protected void drawRna(final RNA rna, final VARNAConfig config)
  {
    try
    {
      rna.drawRNA(rna.getDrawMode(), config);
    } catch (ExceptionNAViewAlgorithm e)
    {
      // only throwable for draw mode = 3 NAView
      System.err.println("Error drawing RNA: " + e.getMessage());
    }
  }
}
