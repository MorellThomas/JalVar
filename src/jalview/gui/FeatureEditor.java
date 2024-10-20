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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jalview.api.FeatureColourI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.io.FeaturesFile;
import jalview.schemes.FeatureColour;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;

/**
 * Provides a dialog allowing the user to add new features, or amend or delete
 * existing features
 */
public class FeatureEditor
{
  /*
   * defaults for creating a new feature are the last created
   * feature type and group
   */
  static String lastFeatureAdded = "feature_1";

  static String lastFeatureGroupAdded = "Jalview";

  /*
   * the sequence(s) with features to be created / amended
   */
  final List<SequenceI> sequences;

  /*
   * the features (or template features) to be created / amended
   */
  final List<SequenceFeature> features;

  /*
   * true if the dialog is to create a new feature, false if
   * for amend or delete of existing feature(s)
   */
  final boolean forCreate;

  /*
   * index into the list of features
   */
  int featureIndex;

  FeatureColourI oldColour;

  FeatureColourI featureColour;

  FeatureRenderer fr;

  AlignmentPanel ap;

  JTextField name;

  JTextField group;

  JTextArea description;

  JSpinner start;

  JSpinner end;

  JPanel mainPanel;

  /**
   * Constructor
   * 
   * @param alignPanel
   * @param seqs
   * @param feats
   * @param create
   *          if true create a new feature, else amend or delete an existing
   *          feature
   */
  public FeatureEditor(AlignmentPanel alignPanel, List<SequenceI> seqs,
          List<SequenceFeature> feats, boolean create)
  {
    ap = alignPanel;
    fr = alignPanel.getSeqPanel().seqCanvas.fr;
    sequences = seqs;
    features = feats;
    this.forCreate = create;

    init();
  }

  /**
   * Initialise the layout and controls
   */
  protected void init()
  {
    featureIndex = 0;

    mainPanel = new JPanel(new BorderLayout());

    name = new JTextField(25);
    name.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        warnIfTypeHidden(mainPanel, name.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        warnIfTypeHidden(mainPanel, name.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        warnIfTypeHidden(mainPanel, name.getText());
      }
    });

    group = new JTextField(25);
    group.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        warnIfGroupHidden(mainPanel, group.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        warnIfGroupHidden(mainPanel, group.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        warnIfGroupHidden(mainPanel, group.getText());
      }
    });

    description = new JTextArea(3, 25);

    start = new JSpinner();
    end = new JSpinner();
    start.setPreferredSize(new Dimension(80, 20));
    end.setPreferredSize(new Dimension(80, 20));

    /*
     * ensure that start can never be more than end
     */
    start.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent e)
      {
        Integer startVal = (Integer) start.getValue();
        ((SpinnerNumberModel) end.getModel()).setMinimum(startVal);
      }
    });
    end.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent e)
      {
        Integer endVal = (Integer) end.getValue();
        ((SpinnerNumberModel) start.getModel()).setMaximum(endVal);
      }
    });

    final JLabel colour = new JLabel();
    colour.setOpaque(true);
    colour.setMaximumSize(new Dimension(30, 16));
    colour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent evt)
      {
        if (featureColour.isSimpleColour())
        {
          /*
           * open colour chooser on click in colour panel
           */
          String title = MessageManager
                  .getString("label.select_feature_colour");
          ColourChooserListener listener = new ColourChooserListener()
          {
            @Override
            public void colourSelected(Color c)
            {
              featureColour = new FeatureColour(c);
              updateColourButton(mainPanel, colour, featureColour);
            };
          };
          JalviewColourChooser.showColourChooser(Desktop.getDesktop(),
                  title, featureColour.getColour(), listener);
        }
        else
        {
          /*
           * variable colour dialog - on OK, refetch the updated
           * feature colour and update this display
           */
          final String ft = features.get(featureIndex).getType();
          final String type = ft == null ? lastFeatureAdded : ft;
          FeatureTypeSettings fcc = new FeatureTypeSettings(fr, type);
          fcc.setRequestFocusEnabled(true);
          fcc.requestFocus();
          fcc.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              featureColour = fr.getFeatureStyle(ft);
              fr.setColour(type, featureColour);
              updateColourButton(mainPanel, colour, featureColour);
            }
          });
        }
      }
    });
    JPanel gridPanel = new JPanel(new GridLayout(3, 1));

    if (!forCreate && features.size() > 1)
    {
      /*
       * more than one feature at selected position - 
       * add a drop-down to choose the feature to amend
       * space pad text if necessary to make entries distinct
       */
      gridPanel = new JPanel(new GridLayout(4, 1));
      JPanel choosePanel = new JPanel();
      choosePanel.add(new JLabel(
              MessageManager.getString("label.select_feature") + ":"));
      final JComboBox<String> overlaps = new JComboBox<>();
      List<String> added = new ArrayList<>();
      for (SequenceFeature sf : features)
      {
        String text = String.format("%s/%d-%d (%s)", sf.getType(),
                sf.getBegin(), sf.getEnd(), sf.getFeatureGroup());
        while (added.contains(text))
        {
          text += " ";
        }
        overlaps.addItem(text);
        added.add(text);
      }
      choosePanel.add(overlaps);

      overlaps.addItemListener(new ItemListener()
      {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
          int index = overlaps.getSelectedIndex();
          if (index != -1)
          {
            featureIndex = index;
            SequenceFeature sf = features.get(index);
            name.setText(sf.getType());
            description.setText(sf.getDescription());
            group.setText(sf.getFeatureGroup());
            start.setValue(new Integer(sf.getBegin()));
            end.setValue(new Integer(sf.getEnd()));
            ((SpinnerNumberModel) start.getModel()).setMaximum(sf.getEnd());
            ((SpinnerNumberModel) end.getModel()).setMinimum(sf.getBegin());

            SearchResultsI highlight = new SearchResults();
            highlight.addResult(sequences.get(0), sf.getBegin(),
                    sf.getEnd());

            ap.getSeqPanel().seqCanvas.highlightSearchResults(highlight);
          }
          FeatureColourI col = fr.getFeatureStyle(name.getText());
          if (col == null)
          {
            col = new FeatureColour(
                    ColorUtils.createColourFromName(name.getText()));
          }
          oldColour = featureColour = col;
          updateColourButton(mainPanel, colour, col);
        }
      });

      gridPanel.add(choosePanel);
    }

    JPanel namePanel = new JPanel();
    gridPanel.add(namePanel);
    namePanel.add(new JLabel(MessageManager.getString("label.name:"),
            JLabel.RIGHT));
    namePanel.add(name);

    JPanel groupPanel = new JPanel();
    gridPanel.add(groupPanel);
    groupPanel.add(new JLabel(MessageManager.getString("label.group:"),
            JLabel.RIGHT));
    groupPanel.add(group);

    JPanel colourPanel = new JPanel();
    gridPanel.add(colourPanel);
    colourPanel.add(new JLabel(MessageManager.getString("label.colour"),
            JLabel.RIGHT));
    colourPanel.add(colour);
    colour.setPreferredSize(new Dimension(150, 15));
    colour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 9));
    colour.setForeground(Color.black);
    colour.setHorizontalAlignment(SwingConstants.CENTER);
    colour.setVerticalAlignment(SwingConstants.CENTER);
    colour.setHorizontalTextPosition(SwingConstants.CENTER);
    colour.setVerticalTextPosition(SwingConstants.CENTER);
    mainPanel.add(gridPanel, BorderLayout.NORTH);

    JPanel descriptionPanel = new JPanel();
    descriptionPanel.add(new JLabel(
            MessageManager.getString("label.description:"), JLabel.RIGHT));
    description.setFont(JvSwingUtils.getTextAreaFont());
    description.setLineWrap(true);
    descriptionPanel.add(new JScrollPane(description));

    if (!forCreate)
    {
      mainPanel.add(descriptionPanel, BorderLayout.SOUTH);

      JPanel startEndPanel = new JPanel();
      startEndPanel.add(new JLabel(MessageManager.getString("label.start"),
              JLabel.RIGHT));
      startEndPanel.add(start);
      startEndPanel.add(new JLabel(MessageManager.getString("label.end"),
              JLabel.RIGHT));
      startEndPanel.add(end);
      mainPanel.add(startEndPanel, BorderLayout.CENTER);
    }
    else
    {
      mainPanel.add(descriptionPanel, BorderLayout.CENTER);
    }

    /*
     * default feature type and group to that of the first feature supplied,
     * or to the last feature created if not supplied (null value) 
     */
    SequenceFeature firstFeature = features.get(0);
    boolean useLastDefaults = firstFeature.getType() == null;
    final String featureType = useLastDefaults ? lastFeatureAdded
            : firstFeature.getType();
    final String featureGroup = useLastDefaults ? lastFeatureGroupAdded
            : firstFeature.getFeatureGroup();
    name.setText(featureType);
    group.setText(featureGroup);

    start.setValue(new Integer(firstFeature.getBegin()));
    end.setValue(new Integer(firstFeature.getEnd()));
    ((SpinnerNumberModel) start.getModel())
            .setMaximum(firstFeature.getEnd());
    ((SpinnerNumberModel) end.getModel())
            .setMinimum(firstFeature.getBegin());

    description.setText(firstFeature.getDescription());
    featureColour = fr.getFeatureStyle(featureType);
    oldColour = featureColour;
    updateColourButton(mainPanel, colour, oldColour);
  }

  /**
   * Presents a dialog allowing the user to add new features, or amend or delete
   * an existing feature. Currently this can be on
   * <ul>
   * <li>double-click on a sequence - Amend/Delete a selected feature at the
   * position</li>
   * <li>Create sequence feature(s) from pop-up menu on selected region</li>
   * <li>Create features for pattern matches from Find</li>
   * </ul>
   * If the supplied feature type is null, show (and update on confirm) the type
   * and group of the last new feature created (with initial defaults of
   * "feature_1" and "Jalview").
   */
  public void showDialog()
  {
    Runnable okAction = forCreate ? getCreateAction() : getAmendAction();
    Runnable cancelAction = getCancelAction();

    /*
     * set dialog action handlers for OK (create/Amend) and Cancel options
     * also for Delete if applicable (when amending features)
     */
    JvOptionPane dialog = JvOptionPane.newOptionDialog(ap.alignFrame)
            .setResponseHandler(0, okAction)
            .setResponseHandler(2, cancelAction);
    if (!forCreate)
    {
      dialog.setResponseHandler(1, getDeleteAction());
    }

    String title = null;
    Object[] options = null;
    if (forCreate)
    {
      title = MessageManager
              .getString("label.create_new_sequence_features");
      options = new Object[] { MessageManager.getString("action.ok"),
          MessageManager.getString("action.cancel") };
    }
    else
    {
      title = MessageManager.formatMessage("label.amend_delete_features",
              new String[]
              { sequences.get(0).getName() });
      options = new Object[] { MessageManager.getString("label.amend"),
          MessageManager.getString("action.delete"),
          MessageManager.getString("action.cancel") };
    }

    dialog.showInternalDialog(mainPanel, title,
            JvOptionPane.YES_NO_CANCEL_OPTION, JvOptionPane.PLAIN_MESSAGE,
            null, options, MessageManager.getString("action.ok"));
  }

  /**
   * Answers an action to run on Cancel in the dialog. This is just to remove
   * any feature highlighting from the display. Changes in the dialog are not
   * applied until it is dismissed with OK, Amend or Delete, so there are no
   * updates to reset on Cancel.
   * 
   * @return
   */
  protected Runnable getCancelAction()
  {
    Runnable okAction = () -> {
      ap.highlightSearchResults(null);
      ap.paintAlignment(false, false);
    };
    return okAction;
  }

  /**
   * Returns the action to be run on OK in the dialog when creating one or more
   * sequence features. Note these may have a pre-supplied feature type (such as
   * a Find pattern), or none, in which case the feature type and group default
   * to those last added through this dialog. The action includes refreshing the
   * Feature Settings panel (if it is open), to show any new feature type, or
   * amended colour for an existing type.
   * 
   * @return
   */
  protected Runnable getCreateAction()
  {
    Runnable okAction = new Runnable()
    {
      boolean useLastDefaults = features.get(0).getType() == null;

      @Override
      public void run()
      {
        final String enteredType = name.getText().trim();
        final String enteredGroup = group.getText().trim();
        final String enteredDescription = description.getText()
                .replaceAll("\n", " ");
        if (enteredType.length() > 0)
        {
          /*
           * update default values only if creating using default values
           */
          if (useLastDefaults)
          {
            lastFeatureAdded = enteredType;
            lastFeatureGroupAdded = enteredGroup;
            // TODO: determine if the null feature group is valid
            if (lastFeatureGroupAdded.length() < 1)
            {
              lastFeatureGroupAdded = null;
            }
          }
        }

        if (enteredType.length() > 0)
        {
          for (int i = 0; i < sequences.size(); i++)
          {
            SequenceFeature sf = features.get(i);
            SequenceFeature sf2 = new SequenceFeature(enteredType,
                    enteredDescription, sf.getBegin(), sf.getEnd(),
                    enteredGroup);
            new FeaturesFile().parseDescriptionHTML(sf2, false);
            sequences.get(i).addSequenceFeature(sf2);
          }

          fr.setColour(enteredType, featureColour);
          fr.featuresAdded();

          repaintPanel();
        }
      }
    };
    return okAction;
  }

  /**
   * Answers the action to run on Delete in the dialog. Note this includes
   * refreshing the Feature Settings (if open) in case the only instance of a
   * feature type or group has been deleted.
   * 
   * @return
   */
  protected Runnable getDeleteAction()
  {
    Runnable deleteAction = () -> {
      SequenceFeature sf = features.get(featureIndex);
      sequences.get(0).getDatasetSequence().deleteFeature(sf);
      fr.featuresAdded();
      ap.getSeqPanel().seqCanvas.highlightSearchResults(null);
      ap.paintAlignment(true, true);
    };
    return deleteAction;
  }

  /**
   * update the amend feature button dependent on the given style
   * 
   * @param bigPanel
   * @param col
   * @param col
   */
  protected void updateColourButton(JPanel bigPanel, JLabel colour,
          FeatureColourI col)
  {
    colour.removeAll();
    colour.setIcon(null);
    colour.setText("");

    if (col.isSimpleColour())
    {
      colour.setToolTipText(null);
      colour.setBackground(col.getColour());
    }
    else
    {
      colour.setBackground(bigPanel.getBackground());
      colour.setForeground(Color.black);
      colour.setToolTipText(FeatureSettings.getColorTooltip(col, false));
      FeatureSettings.renderGraduatedColor(colour, col);
    }
  }

  /**
   * Show a warning message if the entered group is one that is currently hidden
   * 
   * @param panel
   * @param group
   */
  protected void warnIfGroupHidden(JPanel panel, String group)
  {
    if (!fr.isGroupVisible(group))
    {
      String msg = MessageManager.formatMessage("label.warning_hidden",
              MessageManager.getString("label.group"), group);
      JvOptionPane.showMessageDialog(panel, msg, "",
              JvOptionPane.OK_OPTION);
    }
  }

  /**
   * Show a warning message if the entered type is one that is currently hidden
   * 
   * @param panel
   * @param type
   */
  protected void warnIfTypeHidden(JPanel panel, String type)
  {
    if (fr.getRenderOrder().contains(type))
    {
      if (!fr.showFeatureOfType(type))
      {
        String msg = MessageManager.formatMessage("label.warning_hidden",
                MessageManager.getString("label.feature_type"), type);
        JvOptionPane.showMessageDialog(panel, msg, "",
                JvOptionPane.OK_OPTION);
      }
    }
  }

  /**
   * On closing the dialog - ensure feature display is turned on, to show any
   * new features - remove highlighting of the last selected feature - repaint
   * the panel to show any changes
   */
  protected void repaintPanel()
  {
    ap.alignFrame.showSeqFeatures.setSelected(true);
    ap.av.setShowSequenceFeatures(true);
    ap.av.setSearchResults(null);
    ap.paintAlignment(true, true);
  }

  /**
   * Returns the action to be run on OK in the dialog when amending a feature.
   * Note this may include refreshing the Feature Settings panel (if it is
   * open), if feature type, group or colour has changed (but not for
   * description or extent).
   * 
   * @return
   */
  protected Runnable getAmendAction()
  {
    Runnable okAction = new Runnable()
    {
      boolean useLastDefaults = features.get(0).getType() == null;

      String featureType = name.getText();

      String featureGroup = group.getText();

      @Override
      public void run()
      {
        final String enteredType = name.getText().trim();
        final String enteredGroup = group.getText().trim();
        final String enteredDescription = description.getText()
                .replaceAll("\n", " ");
        if (enteredType.length() > 0)

        {
          /*
           * update default values only if creating using default values
           */
          if (useLastDefaults)
          {
            lastFeatureAdded = enteredType;
            lastFeatureGroupAdded = enteredGroup;
            // TODO: determine if the null feature group is valid
            if (lastFeatureGroupAdded.length() < 1)
            {
              lastFeatureGroupAdded = null;
            }
          }
        }

        SequenceFeature sf = features.get(featureIndex);

        /*
         * Need to refresh Feature Settings if type, group or colour changed;
         * note we don't force the feature to be visible - the user has been
         * warned if a hidden feature type or group was entered
         */
        boolean refreshSettings = (!featureType.equals(enteredType)
                || !featureGroup.equals(enteredGroup));
        refreshSettings |= (featureColour != oldColour);
        fr.setColour(enteredType, featureColour);
        int newBegin = sf.begin;
        int newEnd = sf.end;
        try
        {
          newBegin = ((Integer) start.getValue()).intValue();
          newEnd = ((Integer) end.getValue()).intValue();
        } catch (NumberFormatException ex)
        {
          // JSpinner doesn't accept invalid format data :-)
        }

        /*
         * 'amend' the feature by deleting it and adding a new one
         * (to ensure integrity of SequenceFeatures data store)
         * note this dialog only updates one sequence at a time
         */
        sequences.get(0).deleteFeature(sf);
        SequenceFeature newSf = new SequenceFeature(sf, enteredType,
                newBegin, newEnd, enteredGroup, sf.getScore());
        newSf.setDescription(enteredDescription);
        new FeaturesFile().parseDescriptionHTML(newSf, false);
        sequences.get(0).addSequenceFeature(newSf);

        if (refreshSettings)
        {
          fr.featuresAdded();
        }
        repaintPanel();
      }
    };
    return okAction;
  }

}
