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

import jalview.api.AlignViewportI;
import jalview.api.FeatureSettingsModelI;
import jalview.bin.JalviewLite;
import jalview.commands.CommandI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceGroup;
import jalview.renderer.ResidueShader;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.UserColourScheme;
import jalview.structure.SelectionSource;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.viewmodel.AlignmentViewport;

import java.awt.Font;
import java.awt.FontMetrics;

public class AlignViewport extends AlignmentViewport
        implements SelectionSource
{
  boolean cursorMode = false;

  Font font = new Font("SansSerif", Font.PLAIN, 10);

  boolean validCharWidth = true;

  public jalview.bin.JalviewLite applet;

  private AnnotationColumnChooser annotationColumnSelectionState;

  public AlignViewport(AlignmentI al, JalviewLite applet)
  {
    super(al);
    calculator = new jalview.workers.AlignCalcManager();
    this.applet = applet;

    // we always pad gaps
    this.setPadGaps(true);

    if (applet != null)
    {
      // get the width and height scaling factors if they were specified
      String param = applet.getParameter("widthScale");
      if (param != null)
      {
        try
        {
          widthScale = Float.valueOf(param).floatValue();
        } catch (Exception e)
        {
        }
        if (widthScale <= 1.0)
        {
          System.err.println(
                  "Invalid alignment character width scaling factor ("
                          + widthScale + "). Ignoring.");
          widthScale = 1;
        }
        if (JalviewLite.debug)
        {
          System.err.println(
                  "Alignment character width scaling factor is now "
                          + widthScale);
        }
      }
      param = applet.getParameter("heightScale");
      if (param != null)
      {
        try
        {
          heightScale = Float.valueOf(param).floatValue();
        } catch (Exception e)
        {
        }
        if (heightScale <= 1.0)
        {
          System.err.println(
                  "Invalid alignment character height scaling factor ("
                          + heightScale + "). Ignoring.");
          heightScale = 1;
        }
        if (JalviewLite.debug)
        {
          System.err.println(
                  "Alignment character height scaling factor is now "
                          + heightScale);
        }
      }
    }
    setFont(font, true);

    if (applet != null)
    {
      setShowJVSuffix(
              applet.getDefaultParameter("showFullId", getShowJVSuffix()));

      setShowAnnotation(applet.getDefaultParameter("showAnnotation",
              isShowAnnotation()));

      showConservation = applet.getDefaultParameter("showConservation",
              showConservation);

      showQuality = applet.getDefaultParameter("showQuality", showQuality);

      showConsensus = applet.getDefaultParameter("showConsensus",
              showConsensus);

      showOccupancy = applet.getDefaultParameter("showOccupancy",
              showOccupancy);

      setShowUnconserved(applet.getDefaultParameter("showUnconserved",
              getShowUnconserved()));

      setScaleProteinAsCdna(applet.getDefaultParameter("scaleProteinAsCdna",
              isScaleProteinAsCdna()));

      String param = applet.getParameter("upperCase");
      if (param != null)
      {
        if (param.equalsIgnoreCase("bold"))
        {
          setUpperCasebold(true);
        }
      }
      sortByTree = applet.getDefaultParameter("sortByTree", sortByTree);

      setFollowHighlight(applet.getDefaultParameter("automaticScrolling",
              isFollowHighlight()));
      followSelection = isFollowHighlight();

      showSequenceLogo = applet.getDefaultParameter("showSequenceLogo",
              showSequenceLogo);

      normaliseSequenceLogo = applet.getDefaultParameter(
              "normaliseSequenceLogo", applet.getDefaultParameter(
                      "normaliseLogo", normaliseSequenceLogo));

      showGroupConsensus = applet.getDefaultParameter("showGroupConsensus",
              showGroupConsensus);

      showGroupConservation = applet.getDefaultParameter(
              "showGroupConservation", showGroupConservation);

      showConsensusHistogram = applet.getDefaultParameter(
              "showConsensusHistogram", showConsensusHistogram);

    }

    if (applet != null)
    {
      String colour = al.isNucleotide()
              ? applet.getParameter("defaultColourNuc")
              : applet.getParameter("defaultColourProt");
      if (colour == null)
      {
        colour = applet.getParameter("defaultColour");
      }
      if (colour == null)
      {
        colour = applet.getParameter("userDefinedColour");
        if (colour != null)
        {
          colour = "User Defined";
        }
      }

      if (colour != null)
      {
        residueShading = new ResidueShader(ColourSchemeProperty
                .getColourScheme(this, alignment, colour));
        if (residueShading != null)
        {
          residueShading.setConsensus(hconsensus);
        }
      }

      if (applet.getParameter("userDefinedColour") != null)
      {
        residueShading = new ResidueShader(new UserColourScheme(
                applet.getParameter("userDefinedColour")));
      }
    }
    initAutoAnnotation();

  }

  java.awt.Frame nullFrame;

  protected FeatureSettings featureSettings = null;

  private float heightScale = 1, widthScale = 1;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFont(Font f, boolean setGrid)
  {
    font = f;
    if (nullFrame == null)
    {
      nullFrame = new java.awt.Frame();
      nullFrame.addNotify();
    }

    if (setGrid)
    {
      FontMetrics fm = nullFrame.getGraphics().getFontMetrics(font);
      setCharHeight((int) (heightScale * fm.getHeight()));
      setCharWidth((int) (widthScale * fm.charWidth('M')));
    }

    if (isUpperCasebold())
    {
      Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
      FontMetrics fm = nullFrame.getGraphics().getFontMetrics(f2);
      setCharWidth(
              (int) (widthScale * (fm.stringWidth("MMMMMMMMMMM") / 10)));
    }
  }

  public Font getFont()
  {
    return font;
  }

  public void resetSeqLimits(int height)
  {
    ranges.setEndSeq(height / getCharHeight() - 1); // BH 2019.04.18
  }

  boolean centreColumnLabels;

  public boolean getCentreColumnLabels()
  {
    return centreColumnLabels;
  }

  public boolean followSelection = true;

  /**
   * @return true if view selection should always follow the selections
   *         broadcast by other selection sources
   */
  public boolean getFollowSelection()
  {
    return followSelection;
  }

  @Override
  public void sendSelection()
  {
    getStructureSelectionManager().sendSelection(
            new SequenceGroup(getSelectionGroup()),
            new ColumnSelection(getColumnSelection()),
            new HiddenColumns(getAlignment().getHiddenColumns()), this);
  }

  /**
   * Returns an instance of the StructureSelectionManager scoped to this applet
   * instance.
   * 
   * @return
   */
  @Override
  public StructureSelectionManager getStructureSelectionManager()
  {
    return jalview.structure.StructureSelectionManager
            .getStructureSelectionManager(applet);
  }

  @Override
  public boolean isNormaliseSequenceLogo()
  {
    return normaliseSequenceLogo;
  }

  public void setNormaliseSequenceLogo(boolean state)
  {
    normaliseSequenceLogo = state;
  }

  /**
   * 
   * @return true if alignment characters should be displayed
   */
  @Override
  public boolean isValidCharWidth()
  {
    return validCharWidth;
  }

  public AnnotationColumnChooser getAnnotationColumnSelectionState()
  {
    return annotationColumnSelectionState;
  }

  public void setAnnotationColumnSelectionState(
          AnnotationColumnChooser annotationColumnSelectionState)
  {
    this.annotationColumnSelectionState = annotationColumnSelectionState;
  }

  @Override
  public void mirrorCommand(CommandI command, boolean undo,
          StructureSelectionManager ssm, VamsasSource source)
  {
    // TODO refactor so this can be pulled up to superclass or controller
    /*
     * Do nothing unless we are a 'complement' of the source. May replace this
     * with direct calls not via SSM.
     */
    if (source instanceof AlignViewportI
            && ((AlignViewportI) source).getCodingComplement() == this)
    {
      // ok to continue;
    }
    else
    {
      return;
    }

    CommandI mappedCommand = ssm.mapCommand(command, undo, getAlignment(),
            getGapCharacter());
    if (mappedCommand != null)
    {
      mappedCommand.doCommand(null);
      firePropertyChange("alignment", null, getAlignment().getSequences());

      // ap.scalePanelHolder.repaint();
      // ap.repaint();
    }
  }

  @Override
  public VamsasSource getVamsasSource()
  {
    return this;
  }

  /**
   * If this viewport has a (Protein/cDNA) complement, then scroll the
   * complementary alignment to match this one.
   */
  public void scrollComplementaryAlignment(AlignmentPanel complementPanel)
  {
    if (complementPanel == null)
    {
      return;
    }

    /*
     * Populate a SearchResults object with the mapped location to scroll to. If
     * there is no complement, or it is not following highlights, or no mapping
     * is found, the result will be empty.
     */
    SearchResultsI sr = new SearchResults();
    int seqOffset = findComplementScrollTarget(sr);
    if (!sr.isEmpty())
    {
      complementPanel.setToScrollComplementPanel(false);
      complementPanel.scrollToCentre(sr, seqOffset);
      complementPanel.setToScrollComplementPanel(true);
    }
  }

  /**
   * Applies the supplied feature settings descriptor to currently known
   * features. This supports an 'initial configuration' of feature colouring
   * based on a preset or user favourite. This may then be modified in the usual
   * way using the Feature Settings dialogue. NOT IMPLEMENTED FOR APPLET
   * 
   * @param featureSettings
   */
  @Override
  public void applyFeaturesStyle(FeatureSettingsModelI featureSettings)
  {
    // TODO implement for applet
  }

  /**
   * Merges the supplied feature settings descriptor with existing feature
   * styles. This supports an 'initial configuration' of feature colouring based
   * on a preset or user favourite. This may then be modified in the usual way
   * using the Feature Settings dialogue. NOT IMPLEMENTED FOR APPLET
   * 
   * @param featureSettings
   */
  @Override
  public void mergeFeaturesStyle(FeatureSettingsModelI featureSettings)
  {
    // TODO Auto-generated method stub

  }
}
