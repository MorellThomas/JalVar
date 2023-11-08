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

import jalview.gui.JvOptionPane;
import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;
import jalview.util.UrlLink;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class GSequenceLink extends JPanel
{

  JTextField nameTB = new JTextField();

  JTextField urlTB = new JTextField();

  JButton insertSeq = new JButton();

  JButton insertDBAcc = new JButton();

  JLabel insert = new JLabel();

  JLabel jLabel1 = new JLabel();

  JLabel jLabel2 = new JLabel();

  JLabel jLabel3 = new JLabel();

  JLabel jLabel4 = new JLabel();

  JLabel jLabel5 = new JLabel();

  JLabel jLabel6 = new JLabel();

  JPanel jPanel1 = new JPanel();

  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public GSequenceLink()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    this.setLayout(gridBagLayout1);
    nameTB.setFont(JvSwingUtils.getLabelFont());
    nameTB.setBounds(new Rectangle(77, 10, 310, 23));
    nameTB.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent e)
      {
        nameTB_keyTyped(e);
      }
    });
    urlTB.setFont(JvSwingUtils.getLabelFont());
    urlTB.setText("http://");
    urlTB.setBounds(new Rectangle(78, 40, 309, 23));
    urlTB.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyTyped(KeyEvent e)
      {
        urlTB_keyTyped(e);
      }
    });

    insertSeq.setLocation(77, 75);
    insertSeq.setSize(141, 24);
    insertSeq.setText(MessageManager.getString("action.seq_id"));
    insertSeq.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        insertSeq_action(e);
      }
    });

    insertDBAcc.setLocation(210, 75);
    insertDBAcc.setSize(141, 24);
    insertDBAcc.setText(MessageManager.getString("action.db_acc"));
    insertDBAcc.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        insertDBAcc_action(e);
      }
    });

    insert.setText(MessageManager.getString("label.insert"));
    insert.setFont(JvSwingUtils.getLabelFont());
    insert.setHorizontalAlignment(SwingConstants.RIGHT);
    insert.setBounds(17, 78, 58, 16);

    jLabel1.setFont(JvSwingUtils.getLabelFont());
    jLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel1.setText(MessageManager.getString("label.link_name"));
    jLabel1.setBounds(new Rectangle(4, 10, 71, 24));
    jLabel2.setFont(JvSwingUtils.getLabelFont());
    jLabel2.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel2.setText(MessageManager.getString("label.url:"));
    jLabel2.setBounds(new Rectangle(17, 37, 54, 27));
    jLabel3.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel3.setText(MessageManager.getString("label.use_sequence_id_1"));
    jLabel3.setBounds(new Rectangle(21, 102, 351, 15));
    jLabel4.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel4.setText(MessageManager.getString("label.use_sequence_id_2"));
    jLabel4.setBounds(new Rectangle(21, 118, 351, 15));
    jLabel5.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel5.setText(MessageManager.getString("label.use_sequence_id_3"));
    jLabel5.setBounds(new Rectangle(21, 136, 351, 15));

    String lastLabel = MessageManager.getString("label.use_sequence_id_4");
    if (lastLabel.length() > 0)
    {
      // e.g. Spanish version has longer text
      jLabel6.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
      jLabel6.setText(lastLabel);
      jLabel6.setBounds(new Rectangle(21, 152, 351, 15));
    }

    jPanel1.setBorder(BorderFactory.createEtchedBorder());
    jPanel1.setLayout(null);
    jPanel1.add(jLabel1);
    jPanel1.add(nameTB);
    jPanel1.add(urlTB);
    jPanel1.add(insertSeq);
    jPanel1.add(insertDBAcc);
    jPanel1.add(insert);
    jPanel1.add(jLabel2);
    jPanel1.add(jLabel3);
    jPanel1.add(jLabel4);
    jPanel1.add(jLabel5);

    int height = 160;
    if (lastLabel.length() > 0)
    {
      jPanel1.add(jLabel6);
      height = 176;
    }

    this.add(jPanel1,
            new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(5, 4, 6, 5), 390, height));
  }

  @Override
  public void setName(String name)
  {
    nameTB.setText(name);
  }

  public void setURL(String url)
  {
    urlTB.setText(url);
  }

  @Override
  public String getName()
  {
    return nameTB.getText();
  }

  public String getURL()
  {
    return urlTB.getText();
  }

  public boolean checkValid()
  {
    UrlLink ul = new UrlLink("foo|" + urlTB.getText().trim());
    if (ul.isValid() && ul.isDynamic())
    {
      return true;
    }

    JvOptionPane.showInternalMessageDialog(jalview.gui.Desktop.desktop,
            MessageManager.getString("warn.url_must_contain"),
            MessageManager.getString("label.invalid_url"),
            JvOptionPane.WARNING_MESSAGE);
    return false;
  }

  public void notifyDuplicate()
  {
    JvOptionPane.showInternalMessageDialog(jalview.gui.Desktop.desktop,
            MessageManager.getString("warn.name_cannot_be_duplicate"),
            MessageManager.getString("label.invalid_name"),
            JvOptionPane.WARNING_MESSAGE);
  }

  public void nameTB_keyTyped(KeyEvent e)
  {
    if (e.getKeyChar() == '|')
    {
      e.consume();
    }
  }

  public void urlTB_keyTyped(KeyEvent e)
  {
    // URLLink object validation takes care of incorrect regexes.
    // if (e.getKeyChar() == '|' || e.getKeyChar() == ' ')
    // {
    // e.consume();
    // }

  }

  public void insertSeq_action(ActionEvent e)
  {
    insertIntoUrl(insertSeq.getText());
  }

  public void insertDBAcc_action(ActionEvent e)
  {
    insertIntoUrl(insertDBAcc.getText());
  }

  private void insertIntoUrl(String insertion)
  {
    int pos = urlTB.getCaretPosition();
    String text = urlTB.getText();
    String newText = text.substring(0, pos) + insertion
            + text.substring(pos);
    urlTB.setText(newText);
  }
}
