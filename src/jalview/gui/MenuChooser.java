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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MenuChooser implements ActionListener
{
  public static boolean protein;

  private JFrame choosemenu = new JFrame("Animation");

  private JButton bouton = new JButton("bouton 1");

  private JButton bouton2 = new JButton("bouton 2");

  private JPanel container = new JPanel();

  private JLabel label = new JLabel("Le JLabel");

  public MenuChooser()
  {

    choosemenu.setSize(300, 300);
    choosemenu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    choosemenu.setLocationRelativeTo(null);

    container.setBackground(Color.white);
    container.setLayout(new BorderLayout());

    // On ajoute notre Fenetre à la liste des auditeurs de notre Bouton
    bouton.addActionListener(this);
    bouton2.addActionListener(this);

    JPanel south = new JPanel();
    south.add(bouton);
    south.add(bouton2);
    container.add(south, BorderLayout.SOUTH);

    // On change la couleur de police
    label.setForeground(Color.blue);
    // Et on change l'alignement du texte grâce aux attributs static de la
    // classe JLabel
    label.setHorizontalAlignment(JLabel.CENTER);

    container.add(label, BorderLayout.NORTH);

    choosemenu.setContentPane(container);
    choosemenu.setVisible(true);

  }

  // ...

  // *******************************************************************************
  // LA VOILAAAAAAAAAAAAAA
  // *******************************************************************************
  /**
   * C'est la méthode qui sera appelée lors d'un clic sur notre bouton
   */
  public void actionPerformed(ActionEvent arg0)
  {

    if (arg0.getSource() == bouton)
      protein = false;
    label.setText("RNA menu");

    if (arg0.getSource() == bouton2)
      label.setText("Protein menu");
    protein = true;
  }

}
