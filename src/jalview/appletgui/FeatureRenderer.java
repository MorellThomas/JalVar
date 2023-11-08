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
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.FeaturesFile;
import jalview.schemes.FeatureColour;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Hashtable;
import java.util.List;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FeatureRenderer
        extends jalview.renderer.seqfeatures.FeatureRenderer
{
  /*
   * creating a new feature defaults to the type and group as
   * the last one created
   */
  static String lastFeatureAdded = "feature_1";

  static String lastFeatureGroupAdded = "Jalview";

  // Holds web links for feature groups and feature types
  // in the form label|link
  Hashtable featureLinks = null;

  /**
   * Creates a new FeatureRenderer object.
   * 
   * @param av
   */
  public FeatureRenderer(AlignmentViewport av)
  {
    super(av);

  }

  int featureIndex = 0;

  boolean deleteFeature = false;

  FeatureColourPanel colourPanel;

  class FeatureColourPanel extends Panel
  {
    String label = "";

    private Color maxCol;

    private boolean isColourByLabel, isGcol;

    /**
     * render a feature style in the amend feature dialog box
     */
    public void updateColor(FeatureColourI newcol)
    {
      Color bg = null;
      String vlabel = "";
      if (newcol.isSimpleColour())
      {
        bg = newcol.getColour();
        setBackground(bg);
      }
      else
      {
        if (newcol.isAboveThreshold())
        {
          vlabel += " (>)";
        }
        else if (newcol.isBelowThreshold())
        {
          vlabel += " (<)";
        }

        if (isColourByLabel = newcol.isColourByLabel())
        {
          setBackground(bg = Color.white);
          vlabel += " (by Label)";
        }
        else
        {
          setBackground(bg = newcol.getMinColour());
          maxCol = newcol.getMaxColour();
        }
      }
      label = vlabel;
      setBackground(bg);
      repaint();
    }

    FeatureColourPanel()
    {
      super(null);
    }

    @Override
    public void paint(Graphics g)
    {
      Dimension d = getSize();
      if (isGcol)
      {
        if (isColourByLabel)
        {
          g.setColor(Color.white);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);
          g.setColor(Color.black);
          Font f = new Font("Verdana", Font.PLAIN, 10);
          g.setFont(f);
          g.drawString(MessageManager.getString("label.label"), 0, 0);
        }
        else
        {
          g.setColor(maxCol);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);

        }
      }
    }

  }

  /**
   * Shows a dialog allowing the user to create, or amend or delete, sequence
   * features. If null in the supplied feature(s), feature type and group
   * default to those for the last feature created (with initial defaults of
   * "feature_1" and "Jalview").
   * 
   * @param sequences
   * @param features
   * @param create
   * @param ap
   * @return
   */
  boolean amendFeatures(final List<SequenceI> sequences,
          final List<SequenceFeature> features, boolean create,
          final AlignmentPanel ap)
  {
    final Panel bigPanel = new Panel(new BorderLayout());
    final TextField name = new TextField(16);
    final TextField group = new TextField(16);
    final TextArea description = new TextArea(3, 35);
    final TextField start = new TextField(8);
    final TextField end = new TextField(8);
    final Choice overlaps;
    Button deleteButton = new Button("Delete");
    deleteFeature = false;

    name.addTextListener(new TextListener()
    {
      @Override
      public void textValueChanged(TextEvent e)
      {
        warnIfTypeHidden(ap.alignFrame, name.getText());
      }
    });
    group.addTextListener(new TextListener()
    {
      @Override
      public void textValueChanged(TextEvent e)
      {
        warnIfGroupHidden(ap.alignFrame, group.getText());
      }
    });
    colourPanel = new FeatureColourPanel();
    colourPanel.setSize(110, 15);
    final FeatureRenderer fr = this;

    Panel panel = new Panel(new GridLayout(3, 1));

    featureIndex = 0; // feature to be amended.
    Panel tmp;

    // /////////////////////////////////////
    // /MULTIPLE FEATURES AT SELECTED RESIDUE
    if (!create && features.size() > 1)
    {
      panel = new Panel(new GridLayout(4, 1));
      tmp = new Panel();
      tmp.add(new Label("Select Feature: "));
      overlaps = new Choice();
      for (SequenceFeature sf : features)
      {
        String item = sf.getType() + "/" + sf.getBegin() + "-"
                + sf.getEnd();
        if (sf.getFeatureGroup() != null)
        {
          item += " (" + sf.getFeatureGroup() + ")";
        }
        overlaps.addItem(item);
      }

      tmp.add(overlaps);

      overlaps.addItemListener(new java.awt.event.ItemListener()
      {
        @Override
        public void itemStateChanged(java.awt.event.ItemEvent e)
        {
          int index = overlaps.getSelectedIndex();
          if (index != -1)
          {
            featureIndex = index;
            SequenceFeature sf = features.get(index);
            name.setText(sf.getType());
            description.setText(sf.getDescription());
            group.setText(sf.getFeatureGroup());
            start.setText(sf.getBegin() + "");
            end.setText(sf.getEnd() + "");

            SearchResultsI highlight = new SearchResults();
            highlight.addResult(sequences.get(0), sf.getBegin(),
                    sf.getEnd());

            ap.seqPanel.seqCanvas.highlightSearchResults(highlight);

          }
          FeatureColourI col = getFeatureStyle(name.getText());
          if (col == null)
          {
            Color generatedColour = ColorUtils
                    .createColourFromName(name.getText());
            col = new FeatureColour(generatedColour);
          }

          colourPanel.updateColor(col);
        }
      });

      panel.add(tmp);
    }
    // ////////
    // ////////////////////////////////////

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label(MessageManager.getString("label.name:"),
            Label.RIGHT));
    tmp.add(name);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label(MessageManager.getString("label.group:"),
            Label.RIGHT));
    tmp.add(group);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label(MessageManager.getString("label.colour"),
            Label.RIGHT));
    tmp.add(colourPanel);

    bigPanel.add(panel, BorderLayout.NORTH);

    panel = new Panel();
    panel.add(new Label(MessageManager.getString("label.description:"),
            Label.RIGHT));
    panel.add(new ScrollPane().add(description));

    if (!create)
    {
      bigPanel.add(panel, BorderLayout.SOUTH);

      panel = new Panel();
      panel.add(new Label(MessageManager.getString("label.start"),
              Label.RIGHT));
      panel.add(start);
      panel.add(new Label(MessageManager.getString("label.end"),
              Label.RIGHT));
      panel.add(end);
      bigPanel.add(panel, BorderLayout.CENTER);
    }
    else
    {
      bigPanel.add(panel, BorderLayout.CENTER);
    }

    /*
     * use defaults for type and group (and update them on Confirm) only
     * if feature type has not been supplied by the caller
     * (e.g. for Amend, or create features from Find) 
     */
    SequenceFeature firstFeature = features.get(0);
    boolean useLastDefaults = firstFeature.getType() == null;
    String featureType = useLastDefaults ? lastFeatureAdded
            : firstFeature.getType();
    String featureGroup = useLastDefaults ? lastFeatureGroupAdded
            : firstFeature.getFeatureGroup();

    String title = create
            ? MessageManager.getString("label.create_new_sequence_features")
            : MessageManager.formatMessage("label.amend_delete_features",
                    new String[]
                    { sequences.get(0).getName() });

    final JVDialog dialog = new JVDialog(ap.alignFrame, title, true, 385,
            240);

    dialog.setMainPanel(bigPanel);

    name.setText(featureType);
    group.setText(featureGroup);

    if (!create)
    {
      dialog.ok.setLabel(MessageManager.getString("label.amend"));
      dialog.buttonPanel.add(deleteButton, 1);
      deleteButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent evt)
        {
          deleteFeature = true;
          dialog.setVisible(false);
        }
      });
    }

    start.setText(firstFeature.getBegin() + "");
    end.setText(firstFeature.getEnd() + "");
    description.setText(firstFeature.getDescription());
    // lookup (or generate) the feature colour
    FeatureColourI fcol = getFeatureStyle(name.getText());
    // simply display the feature color in a box
    colourPanel.updateColor(fcol);
    dialog.setResizable(true);
    // TODO: render the graduated color in the box.
    colourPanel.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent evt)
      {
        if (!colourPanel.isGcol)
        {
          new UserDefinedColours(fr, ap.alignFrame);
        }
        else
        {
          new FeatureColourChooser(ap.alignFrame, name.getText());
          dialog.transferFocus();
        }
      }
    });
    dialog.setVisible(true);

    FeaturesFile ffile = new FeaturesFile();

    /*
     * only update default type and group if we used defaults
     */
    final String enteredType = name.getText().trim();
    final String enteredGroup = group.getText().trim();
    final String enteredDesc = description.getText().replace('\n', ' ');

    if (dialog.accept && useLastDefaults)
    {
      lastFeatureAdded = enteredType;
      lastFeatureGroupAdded = enteredGroup;
    }

    if (!create)
    {
      SequenceFeature sf = features.get(featureIndex);
      if (dialog.accept)
      {
        if (!colourPanel.isGcol)
        {
          // update colour - otherwise its already done.
          setColour(enteredType,
                  new FeatureColour(colourPanel.getBackground()));
        }
        int newBegin = sf.begin;
        int newEnd = sf.end;
        try
        {
          newBegin = Integer.parseInt(start.getText());
          newEnd = Integer.parseInt(end.getText());
        } catch (NumberFormatException ex)
        {
          //
        }

        /*
         * replace the feature by deleting it and adding a new one
         * (to ensure integrity of SequenceFeatures data store)
         */
        sequences.get(0).deleteFeature(sf);
        SequenceFeature newSf = new SequenceFeature(sf, enteredType,
                newBegin, newEnd, enteredGroup, sf.getScore());
        newSf.setDescription(enteredDesc);
        ffile.parseDescriptionHTML(newSf, false);
        // amend features dialog only updates one sequence at a time
        sequences.get(0).addSequenceFeature(newSf);
        boolean typeOrGroupChanged = (!featureType.equals(newSf.getType())
                || !featureGroup.equals(newSf.getFeatureGroup()));

        ffile.parseDescriptionHTML(sf, false);
        if (typeOrGroupChanged)
        {
          featuresAdded();
        }
      }
      if (deleteFeature)
      {
        sequences.get(0).deleteFeature(sf);
        // ensure Feature Settings reflects removal of feature / group
        featuresAdded();
      }
    }
    else
    {
      /*
       * adding feature(s)
       */
      if (dialog.accept && name.getText().length() > 0)
      {
        for (int i = 0; i < sequences.size(); i++)
        {
          SequenceFeature sf = features.get(i);
          SequenceFeature sf2 = new SequenceFeature(enteredType,
                  enteredDesc, sf.getBegin(), sf.getEnd(), enteredGroup);
          ffile.parseDescriptionHTML(sf2, false);
          sequences.get(i).addSequenceFeature(sf2);
        }

        Color newColour = colourPanel.getBackground();
        // setColour(lastFeatureAdded, fcol);

        setColour(enteredType, new FeatureColour(newColour)); // was fcol
        featuresAdded();
      }
      else
      {
        // no update to the alignment
        return false;
      }
    }
    // refresh the alignment and the feature settings dialog
    if (((jalview.appletgui.AlignViewport) av).featureSettings != null)
    {
      ((jalview.appletgui.AlignViewport) av).featureSettings.refreshTable();
    }
    // findAllFeatures();

    ap.paintAlignment(true, true);

    return true;
  }

  protected void warnIfGroupHidden(Frame frame, String group)
  {
    if (featureGroups.containsKey(group) && !featureGroups.get(group))
    {
      String msg = MessageManager.formatMessage("label.warning_hidden",
              MessageManager.getString("label.group"), group);
      showWarning(frame, msg);
    }
  }

  protected void warnIfTypeHidden(Frame frame, String type)
  {
    if (getRenderOrder().contains(type))
    {
      if (!showFeatureOfType(type))
      {
        String msg = MessageManager.formatMessage("label.warning_hidden",
                MessageManager.getString("label.feature_type"), type);
        showWarning(frame, msg);
      }
    }
  }

  /**
   * @param frame
   * @param msg
   */
  protected void showWarning(Frame frame, String msg)
  {
    JVDialog d = new JVDialog(frame, "", true, 350, 200);
    Panel mp = new Panel();
    d.ok.setLabel(MessageManager.getString("action.ok"));
    d.cancel.setVisible(false);
    mp.setLayout(new FlowLayout());
    mp.add(new Label(msg));
    d.setMainPanel(mp);
    d.setVisible(true);
  }
}
