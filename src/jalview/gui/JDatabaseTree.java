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

import java.util.Locale;

import jalview.util.MessageManager;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class JDatabaseTree extends JalviewDialog implements KeyListener
{
  boolean allowMultiSelections = false;

  public int action;

  JButton getDatabaseSelectorButton()
  {
    final JButton viewdbs = new JButton(
            MessageManager.getString("action.select_ddbb"));
    viewdbs.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        showDialog();
      }
    });
    return viewdbs;
  }

  JScrollPane svp;

  JTree dbviews;

  private jalview.ws.SequenceFetcher sfetcher;

  private JLabel dbstatus, dbstatex;

  private JPanel mainPanel = new JPanel(new BorderLayout());

  public JDatabaseTree(jalview.ws.SequenceFetcher sfetch)
  {
    mainPanel.add(this);
    initDialogFrame(mainPanel, true, false, MessageManager
            .getString("label.select_database_retrieval_source"), 650, 490);
    /*
     * Dynamically generated database list will need a translation function from
     * internal source to externally distinct names. UNIPROT and UP_NAME are
     * identical DB sources, and should be collapsed.
     */
    DefaultMutableTreeNode tn = null, root = new DefaultMutableTreeNode();
    Hashtable<String, DefaultMutableTreeNode> source = new Hashtable<>();
    sfetcher = sfetch;
    String dbs[] = sfetch.getSupportedDb();
    Hashtable<String, String> ht = new Hashtable<>();
    for (int i = 0; i < dbs.length; i++)
    {
      tn = source.get(dbs[i]);
      List<DbSourceProxy> srcs = sfetch.getSourceProxy(dbs[i]);
      if (tn == null)
      {
        source.put(dbs[i], tn = new DefaultMutableTreeNode(dbs[i], true));
      }
      for (DbSourceProxy dbp : srcs)
      {
        if (ht.get(dbp.getDbName()) == null)
        {
          tn.add(new DefaultMutableTreeNode(dbp, false));
          ht.put(dbp.getDbName(), dbp.getDbName());
        }
        else
        {
          System.err.println("dupe ig for : " + dbs[i] + " \t"
                  + dbp.getDbName() + " (" + dbp.getDbSource() + ")");
          source.remove(tn);
        }
      }
    }
    for (int i = 0; i < dbs.length; i++)
    {
      tn = source.get(dbs[i]);
      if (tn == null)
      {
        continue;
      }
      if (tn.getChildCount() == 1)
      {
        DefaultMutableTreeNode ttn = (DefaultMutableTreeNode) tn
                .getChildAt(0);
        // remove nodes with only one child
        tn.setUserObject(ttn.getUserObject());
        tn.removeAllChildren();
        source.put(dbs[i], tn);
        tn.setAllowsChildren(false);
      }
      root.add(tn);
    }
    // and sort the tree
    sortTreeNodes(root);
    dbviews = new JTree(new DefaultTreeModel(root, false));
    dbviews.setCellRenderer(new DbTreeRenderer(this));

    dbviews.getSelectionModel()
            .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    svp = new JScrollPane(dbviews);
    svp.setMinimumSize(new Dimension(100, 200));
    svp.setPreferredSize(new Dimension(200, 400));
    svp.setMaximumSize(new Dimension(300, 600));

    JPanel panel = new JPanel(new BorderLayout());
    panel.setSize(new Dimension(350, 220));
    panel.add(svp);
    dbviews.addTreeSelectionListener(new TreeSelectionListener()
    {

      @Override
      public void valueChanged(TreeSelectionEvent arg0)
      {
        _setSelectionState();
      }
    });
    dbviews.addMouseListener(new MouseAdapter()
    {

      @Override
      public void mousePressed(MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          okPressed();
          closeDialog();
        }
      }
    });
    JPanel jc = new JPanel(new BorderLayout()),
            j = new JPanel(new FlowLayout());
    jc.add(svp, BorderLayout.CENTER);

    java.awt.Font f;
    // TODO: make the panel stay a fixed size for longest dbname+example set.
    JPanel dbstat = new JPanel(new GridLayout(2, 1));
    dbstatus = new JLabel(" "); // set the height correctly for layout
    dbstatus.setFont(f = JvSwingUtils.getLabelFont(false, true));
    dbstatus.setSize(new Dimension(290, 50));
    dbstatex = new JLabel(" ");
    dbstatex.setFont(f);
    dbstatex.setSize(new Dimension(290, 50));
    dbstat.add(dbstatus);
    dbstat.add(dbstatex);
    jc.add(dbstat, BorderLayout.SOUTH);
    jc.validate();
    add(jc, BorderLayout.CENTER);
    ok.setEnabled(false);
    j.add(ok);
    j.add(cancel);
    add(j, BorderLayout.SOUTH);
    dbviews.addKeyListener(this);
    validate();
  }

  private void sortTreeNodes(DefaultMutableTreeNode root)
  {
    if (root.getChildCount() == 0)
    {
      return;
    }
    int count = root.getChildCount();
    String[] names = new String[count];
    DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[count];
    for (int i = 0; i < count; i++)
    {
      TreeNode node = root.getChildAt(i);
      if (node instanceof DefaultMutableTreeNode)
      {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) node;
        nodes[i] = child;
        if (child.getUserObject() instanceof DbSourceProxy)
        {
          names[i] = ((DbSourceProxy) child.getUserObject()).getDbName()
                  .toLowerCase(Locale.ROOT);
        }
        else
        {
          names[i] = ((String) child.getUserObject())
                  .toLowerCase(Locale.ROOT);
          sortTreeNodes(child);
        }
      }
      else
      {
        throw new Error(MessageManager
                .getString("error.implementation_error_cant_reorder_tree"));
      }
    }
    jalview.util.QuickSort.sort(names, nodes);
    root.removeAllChildren();
    for (int i = count - 1; i >= 0; i--)
    {
      root.add(nodes[i]);
    }
  }

  private class DbTreeRenderer extends DefaultTreeCellRenderer
          implements TreeCellRenderer
  {
    JDatabaseTree us;

    public DbTreeRenderer(JDatabaseTree me)
    {
      us = me;
      ToolTipManager.sharedInstance().registerComponent(dbviews);
    }

    private Component returnLabel(String txt)
    {
      JLabel jl = new JLabel(txt);
      jl.setFont(JvSwingUtils.getLabelFont());
      return jl;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus)
    {
      String val = "";
      if (value != null && value instanceof DefaultMutableTreeNode)
      {
        DefaultMutableTreeNode vl = (DefaultMutableTreeNode) value;
        value = vl.getUserObject();
        if (value instanceof DbSourceProxy)
        {
          val = ((DbSourceProxy) value).getDbName();
          if (((DbSourceProxy) value).getDescription() != null)
          { // getName()
            this.setToolTipText(((DbSourceProxy) value).getDescription());
          }
        }
        else
        {
          if (value instanceof String)
          {
            val = (String) value;
          }
        }
      }
      if (value == null)
      {
        val = "";
      }
      return super.getTreeCellRendererComponent(tree, val, selected,
              expanded, leaf, row, hasFocus);

    }
  }

  List<DbSourceProxy> oldselection, selection = null;

  TreePath[] tsel = null, oldtsel = null;

  @Override
  protected void raiseClosed()
  {
    for (ActionListener al : lstners)
    {
      al.actionPerformed(null);
    }
  }

  @Override
  protected void okPressed()
  {
    _setSelectionState();
  }

  @Override
  protected void cancelPressed()
  {
    selection = oldselection;
    tsel = oldtsel;
    _revertSelectionState();
    closeDialog();
  }

  void showDialog()
  {
    oldselection = selection;
    oldtsel = tsel;
    validate();
    waitForInput();
  }

  public boolean hasSelection()
  {
    return selection == null ? false : selection.size() == 0 ? false : true;
  }

  public List<DbSourceProxy> getSelectedSources()
  {
    return selection;
  }

  /**
   * disable or enable selection handler
   */
  boolean handleSelections = true;

  private void _setSelectionState()
  {
    if (!handleSelections)
    {
      return;
    }
    ok.setEnabled(false);
    if (dbviews.getSelectionCount() == 0)
    {
      selection = null;
    }

    tsel = dbviews.getSelectionPaths();
    boolean forcedFirstChild = false;
    List<DbSourceProxy> srcs = new ArrayList<>();
    if (tsel != null)
    {
      for (TreePath tp : tsel)
      {
        DefaultMutableTreeNode admt,
                dmt = (DefaultMutableTreeNode) tp.getLastPathComponent();
        if (dmt.getUserObject() != null)
        {
          /*
           * enable OK button once a selection has been made
           */
          ok.setEnabled(true);
          if (dmt.getUserObject() instanceof DbSourceProxy)
          {
            srcs.add((DbSourceProxy) dmt.getUserObject());
          }
          else
          {
            if (allowMultiSelections)
            {
              srcs.addAll(sfetcher
                      .getSourceProxy((String) dmt.getUserObject()));
            }
            else
            {
              srcs.add(sfetcher.getSourceProxy((String) dmt.getUserObject())
                      .get(0));
              forcedFirstChild = true;
            }
          }
        }
      }
    }
    updateDbStatus(srcs, forcedFirstChild);
    selection = srcs;
  }

  private void _revertSelectionState()
  {
    handleSelections = false;
    if (selection == null || selection.size() == 0)
    {
      dbviews.clearSelection();
    }
    else
    {
      dbviews.setSelectionPaths(tsel);
    }
    handleSelections = true;
  }

  private void updateDbStatus(List<DbSourceProxy> srcs,
          boolean forcedFirstChild)
  {
    int x = 0;
    String nm = "", qr = "";
    for (DbSourceProxy dbs : srcs)
    {
      String tq = dbs.getTestQuery();
      nm = dbs.getDbName();
      if (tq != null && tq.trim().length() > 0 && dbs.isValidReference(tq))
      {
        qr = tq;
        x++;
      }
    }

    dbstatex.setText(" ");
    if (allowMultiSelections)
    {
      dbstatus.setText(MessageManager.formatMessage(
              "label.selected_database_to_fetch_from", new String[]
              { Integer.valueOf(srcs.size()).toString(),
                  (srcs.size() == 1 ? "" : "s"),
                  (srcs.size() > 0
                          ? " with " + x + " test quer"
                                  + (x == 1 ? "y" : "ies")
                          : ".") }));
    }
    else
    {
      if (nm.length() > 0)
      {
        dbstatus.setText(MessageManager
                .formatMessage("label.database_param", new String[]
                { nm }));
        if (qr.length() > 0)
        {
          dbstatex.setText(MessageManager
                  .formatMessage("label.example_param", new String[]
                  { qr }));
        }
      }
      else
      {
        dbstatus.setText(" ");
      }
    }
    dbstatus.invalidate();
    dbstatex.invalidate();
  }

  public String getSelectedItem()
  {
    if (hasSelection())
    {
      return getSelectedSources().get(0).getDbName();
    }
    return null;
  }

  public String getExampleQueries()
  {
    if (!hasSelection())
    {
      return null;
    }
    StringBuffer sb = new StringBuffer();
    HashSet<String> hs = new HashSet<>();
    for (DbSourceProxy dbs : getSelectedSources())
    {
      String tq = dbs.getTestQuery();
      ;
      if (hs.add(tq))
      {
        if (sb.length() > 0)
        {
          sb.append(";");
        }
        sb.append(tq);
      }
    }
    return sb.toString();
  }

  List<ActionListener> lstners = new Vector<>();

  public void addActionListener(ActionListener actionListener)
  {
    lstners.add(actionListener);
  }

  public void removeActionListener(ActionListener actionListener)
  {
    lstners.remove(actionListener);
  }

  @Override
  public void keyPressed(KeyEvent arg0)
  {
    if (!arg0.isConsumed() && arg0.getKeyCode() == KeyEvent.VK_ENTER)
    {
      action = arg0.getKeyCode();
      okPressed();
      closeDialog();
    }
    if (!arg0.isConsumed() && arg0.getKeyChar() == KeyEvent.VK_ESCAPE)
    {
      action = arg0.getKeyCode();
      cancelPressed();
    }
  }

  @Override
  public void keyReleased(KeyEvent arg0)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void keyTyped(KeyEvent arg0)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void setVisible(boolean arg0)
  {
    System.out.println("setVisible: " + arg0);
    super.setVisible(arg0);
  }
}
