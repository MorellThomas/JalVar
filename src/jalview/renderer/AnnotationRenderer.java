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
package jalview.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.util.BitSet;
import java.util.Hashtable;

import jalview.analysis.AAFrequency;
import jalview.analysis.CodingUtils;
import jalview.analysis.RepeatingVariance;
import jalview.analysis.Rna;
import jalview.analysis.StructureFrequency;
import jalview.api.AlignViewportI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.ProfilesI;
import jalview.renderer.api.AnnotationRendererFactoryI;
import jalview.renderer.api.AnnotationRowRendererI;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.ResidueProperties;
import jalview.schemes.ZappoColourScheme;
import jalview.util.Platform;

public class AnnotationRenderer
{
  private static final int UPPER_TO_LOWER = 'a' - 'A'; // 32

  private static final int CHAR_A = 'A'; // 65

  private static final int CHAR_Z = 'Z'; // 90

  /**
   * flag indicating if timing and redraw parameter info should be output
   */
  private final boolean debugRedraw;

  private int charWidth, endRes, charHeight;

  private boolean validCharWidth, hasHiddenColumns;

  private FontMetrics fm;

  private final boolean USE_FILL_ROUND_RECT = Platform.isAMacAndNotJS();

  boolean av_renderHistogram = true, av_renderProfile = true,
          av_normaliseProfile = false;

  ResidueShaderI profcolour = null;

  private ColumnSelection columnSelection;

  private HiddenColumns hiddenColumns;

  private ProfilesI hconsensus;
  
  private ProfilesI hvariance;

  private Hashtable<String, Object>[] complementConsensus;

  private Hashtable<String, Object>[] hStrucConsensus;

  private boolean av_ignoreGapsConsensus;

  /**
   * attributes set from AwtRenderPanelI
   */
  /**
   * old image used when data is currently being calculated and cannot be
   * rendered
   */
  private Image fadedImage;

  /**
   * panel being rendered into
   */
  private ImageObserver annotationPanel;

  /**
   * width of image to render in panel
   */
  private int imgWidth;

  /**
   * offset to beginning of visible area
   */
  private int sOffset;

  /**
   * offset to end of visible area
   */
  private int visHeight;

  /**
   * indicate if the renderer should only render the visible portion of the
   * annotation given the current view settings
   */
  private boolean useClip = true;

  /**
   * master flag indicating if renderer should ever try to clip. not enabled for
   * jalview 2.8.1
   */
  private boolean canClip = false;

  public AnnotationRenderer()
  {
    this(false);
  }

  /**
   * Create a new annotation Renderer
   * 
   * @param debugRedraw
   *          flag indicating if timing and redraw parameter info should be
   *          output
   */
  public AnnotationRenderer(boolean debugRedraw)
  {
    this.debugRedraw = debugRedraw;
  }

  /**
   * Remove any references and resources when this object is no longer required
   */
  public void dispose()
  {
    hiddenColumns = null;
    hconsensus = null;
    hvariance = null;
    complementConsensus = null;
    hStrucConsensus = null;
    fadedImage = null;
    annotationPanel = null;
    rendererFactoryI = null;
  }

  void drawStemAnnot(Graphics g, Annotation[] row_annotations, int lastSSX,
          int x, int y, int iconOffset, int startRes, int column,
          boolean validRes, boolean validEnd)
  {
    g.setColor(STEM_COLOUR);
    int sCol = (lastSSX / charWidth)
            + hiddenColumns.visibleToAbsoluteColumn(startRes);
    int x1 = lastSSX;
    int x2 = (x * charWidth);

    char dc = (column == 0 || row_annotations[column - 1] == null) ? ' '
            : row_annotations[column - 1].secondaryStructure;

    boolean diffupstream = sCol == 0 || row_annotations[sCol - 1] == null
            || dc != row_annotations[sCol - 1].secondaryStructure;
    boolean diffdownstream = !validRes || !validEnd
            || row_annotations[column] == null
            || dc != row_annotations[column].secondaryStructure;

    if (column > 0 && Rna.isClosingParenthesis(dc))
    {
      if (diffupstream)
      // if (validRes && column>1 && row_annotations[column-2]!=null &&
      // dc.equals(row_annotations[column-2].displayCharacter))
      {
        /*
         * if new annotation with a closing base pair half of the stem, 
         * display a backward arrow
         */
        g.fillPolygon(new int[] { lastSSX + 5, lastSSX + 5, lastSSX },
                new int[]
                { y + iconOffset, y + 14 + iconOffset, y + 8 + iconOffset },
                3);
        x1 += 5;
      }
      if (diffdownstream)
      {
        x2 -= 1;
      }
    }
    else
    {
      // display a forward arrow
      if (diffdownstream)
      {
        /*
         * if annotation ending with an opeing base pair half of the stem, 
         * display a forward arrow
         */
        g.fillPolygon(new int[] { x2 - 5, x2 - 5, x2 },
                new int[]
                { y + iconOffset, y + 14 + iconOffset, y + 8 + iconOffset },
                3);
        x2 -= 5;
      }
      if (diffupstream)
      {
        x1 += 1;
      }
    }
    // draw arrow body
    g.fillRect(x1, y + 4 + iconOffset, x2 - x1, 7);
  }

  void drawNotCanonicalAnnot(Graphics g, Color nonCanColor,
          Annotation[] row_annotations, int lastSSX, int x, int y,
          int iconOffset, int startRes, int column, boolean validRes,
          boolean validEnd)
  {
    // System.out.println(nonCanColor);

    g.setColor(nonCanColor);
    int sCol = (lastSSX / charWidth)
            + hiddenColumns.visibleToAbsoluteColumn(startRes);
    int x1 = lastSSX;
    int x2 = (x * charWidth);

    String dc = (column == 0 || row_annotations[column - 1] == null) ? ""
            : row_annotations[column - 1].displayCharacter;

    boolean diffupstream = sCol == 0 || row_annotations[sCol - 1] == null
            || !dc.equals(row_annotations[sCol - 1].displayCharacter);
    boolean diffdownstream = !validRes || !validEnd
            || row_annotations[column] == null
            || !dc.equals(row_annotations[column].displayCharacter);
    // System.out.println("Column "+column+" diff up: "+diffupstream+"
    // down:"+diffdownstream);
    // If a closing base pair half of the stem, display a backward arrow
    if (column > 0 && Rna.isClosingParenthesis(dc))
    {

      if (diffupstream)
      // if (validRes && column>1 && row_annotations[column-2]!=null &&
      // dc.equals(row_annotations[column-2].displayCharacter))
      {
        g.fillPolygon(new int[] { lastSSX + 5, lastSSX + 5, lastSSX },
                new int[]
                { y + iconOffset, y + 14 + iconOffset, y + 8 + iconOffset },
                3);
        x1 += 5;
      }
      if (diffdownstream)
      {
        x2 -= 1;
      }
    }
    else
    {

      // display a forward arrow
      if (diffdownstream)
      {
        g.fillPolygon(new int[] { x2 - 5, x2 - 5, x2 },
                new int[]
                { y + iconOffset, y + 14 + iconOffset, y + 8 + iconOffset },
                3);
        x2 -= 5;
      }
      if (diffupstream)
      {
        x1 += 1;
      }
    }
    // draw arrow body
    g.fillRect(x1, y + 4 + iconOffset, x2 - x1, 7);
  }

  // public void updateFromAnnotationPanel(FontMetrics annotFM, AlignViewportI
  // av)
  public void updateFromAwtRenderPanel(AwtRenderPanelI annotPanel,
          AlignViewportI av)
  {
    fm = annotPanel.getFontMetrics();
    annotationPanel = annotPanel;
    fadedImage = annotPanel.getFadedImage();
    imgWidth = annotPanel.getFadedImageWidth();
    // visible area for rendering
    int[] bounds = annotPanel.getVisibleVRange();
    if (bounds != null)
    {
      sOffset = bounds[0];
      visHeight = bounds[1];
      if (visHeight == 0)
      {
        useClip = false;
      }
      else
      {
        useClip = canClip;
      }
    }
    else
    {
      useClip = false;
    }

    rendererFactoryI = AnnotationRendererFactory.getRendererFactory();
    updateFromAlignViewport(av);
  }

  public void updateFromAlignViewport(AlignViewportI av)
  {
    charWidth = av.getCharWidth();
    endRes = av.getRanges().getEndRes();
    charHeight = av.getCharHeight();
    hasHiddenColumns = av.hasHiddenColumns();
    validCharWidth = av.isValidCharWidth();
    av_renderHistogram = av.isShowConsensusHistogram();
    av_renderProfile = av.isShowSequenceLogo();
    av_normaliseProfile = av.isNormaliseSequenceLogo();
    profcolour = av.getResidueShading();
    if (profcolour == null || profcolour.getColourScheme() == null)
    {
      /*
       * Use default colour for sequence logo if 
       * the alignment has no colourscheme set
       * (would like to use user preference but n/a for applet)
       */
      ColourSchemeI col = av.getAlignment().isNucleotide()
              ? new NucleotideColourScheme()
              : new ZappoColourScheme();
      profcolour = new ResidueShader(col);
    }
    columnSelection = av.getColumnSelection();
    hiddenColumns = av.getAlignment().getHiddenColumns();
    hconsensus = av.getSequenceConsensusHash();
    hvariance = av.getSequenceVarianceHash();
    complementConsensus = av.getComplementConsensusHash();
    hStrucConsensus = av.getRnaStructureConsensusHash();
    av_ignoreGapsConsensus = av.isIgnoreGapsConsensus();
  }

  /**
   * Returns profile data; the first element is the profile type, the second is
   * the number of distinct values, the third the total count, and the remainder
   * depend on the profile type.
   * 
   * @param aa
   * @param column
   * @return
   */
  int[] getProfileFor(AlignmentAnnotation aa, int column)
  {
    // TODO : consider refactoring the global alignment calculation
    // properties/rendering attributes as a global 'alignment group' which holds
    // all vis settings for the alignment as a whole rather than a subset
    //
    if (aa.autoCalculated && (aa.label.startsWith("Consensus")
            || aa.label.startsWith("cDNA Consensus")))
    {
      boolean forComplement = aa.label.startsWith("cDNA Consensus");
      if (aa.groupRef != null && aa.groupRef.consensusData != null
              && aa.groupRef.isShowSequenceLogo())
      {
        // TODO? group consensus for cDNA complement
        return AAFrequency.extractProfile(
                aa.groupRef.consensusData.get(column),
                aa.groupRef.getIgnoreGapsConsensus());
      }
      // TODO extend annotation row to enable dynamic and static profile data to
      // be stored
      if (aa.groupRef == null && aa.sequenceRef == null)
      {
        if (forComplement)
        {
          return AAFrequency.extractCdnaProfile(complementConsensus[column],
                  av_ignoreGapsConsensus);
        }
        else
        {
          return AAFrequency.extractProfile(hconsensus.get(column),
                  av_ignoreGapsConsensus);
        }
      }
    }
    else
    {
      if (aa.autoCalculated && aa.label.startsWith("StrucConsensus"))
      {
        // TODO implement group structure consensus
        /*
         * if (aa.groupRef != null && aa.groupRef.consensusData != null &&
         * aa.groupRef.isShowSequenceLogo()) { //TODO check what happens for
         * group selections return StructureFrequency.extractProfile(
         * aa.groupRef.consensusData[column], aa.groupRef
         * .getIgnoreGapsConsensus()); }
         */
        // TODO extend annotation row to enable dynamic and static profile data
        // to
        // be stored
        if (aa.groupRef == null && aa.sequenceRef == null
                && hStrucConsensus != null
                && hStrucConsensus.length > column)
        {
          return StructureFrequency.extractProfile(hStrucConsensus[column],
                  av_ignoreGapsConsensus);
        }
        
        if (aa.autoCalculated && aa.label.startsWith("Variance"))
        {
          return RepeatingVariance.extractProfile(hvariance.get(column), true);
        }
      }
    }
    return null;
  }

  boolean rna = false;

  private AnnotationRendererFactoryI rendererFactoryI;

  /**
   * Render the annotation rows associated with an alignment.
   * 
   * @param annotPanel
   *          container frame
   * @param av
   *          data and view settings to render
   * @param g
   *          destination for graphics
   * @param activeRow
   *          row where a mouse event occured (or -1)
   * @param startRes
   *          first column that will be drawn
   * @param endRes
   *          last column that will be drawn
   * @return true if the fadedImage was used for any alignment annotation rows
   *         currently being calculated
   */
  public boolean drawComponent(AwtRenderPanelI annotPanel,
          AlignViewportI av, Graphics g, int activeRow, int startRes,
          int endRes)
  {
    long stime = System.currentTimeMillis();
    boolean usedFaded = false;
    // NOTES:
    // AnnotationPanel needs to implement: ImageObserver, access to
    // AlignViewport
    updateFromAwtRenderPanel(annotPanel, av);
    fm = g.getFontMetrics();
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    // int temp = 0;
    if (aa == null)
    {
      return false;
    }
    int x = 0, y = 0;
    int column = 0;
    char lastSS;
    int lastSSX;
    int iconOffset = 0;
    boolean validRes = false;
    boolean validEnd = false;
    boolean labelAllCols = false;
    // boolean centreColLabels;
    // boolean centreColLabelsDef = av.isCentreColumnLabels();
    boolean scaleColLabel = false;
    final AlignmentAnnotation consensusAnnot = av
            .getAlignmentConsensusAnnotation();
    final AlignmentAnnotation structConsensusAnnot = av
            .getAlignmentStrucConsensusAnnotation();
    final AlignmentAnnotation complementConsensusAnnot = av
            .getComplementConsensusAnnotation();

    BitSet graphGroupDrawn = new BitSet();
    int charOffset = 0; // offset for a label
    // \u03B2 \u03B1
    // debug ints
    int yfrom = 0, f_i = 0, yto = 0, f_to = 0;
    boolean clipst = false, clipend = false;
    for (int i = 0; i < aa.length; i++)
    {
      AlignmentAnnotation row = aa[i];
      boolean renderHistogram = true;
      boolean renderProfile = false;
      boolean normaliseProfile = false;
      boolean isRNA = row.isRNA();

      // check if this is a consensus annotation row and set the display
      // settings appropriately
      // TODO: generalise this to have render styles for consensus/profile
      // data
      if (row.groupRef != null && row == row.groupRef.getConsensus())
      {
        renderHistogram = row.groupRef.isShowConsensusHistogram();
        renderProfile = row.groupRef.isShowSequenceLogo();
        normaliseProfile = row.groupRef.isNormaliseSequenceLogo();
      }
      else if (row == consensusAnnot || row == structConsensusAnnot
              || row == complementConsensusAnnot)
      {
        renderHistogram = av_renderHistogram;
        renderProfile = av_renderProfile;
        normaliseProfile = av_normaliseProfile;
      }

      Annotation[] row_annotations = row.annotations;
      if (!row.visible)
      {
        continue;
      }
      // centreColLabels = row.centreColLabels || centreColLabelsDef;
      labelAllCols = row.showAllColLabels;
      scaleColLabel = row.scaleColLabel;
      lastSS = ' ';
      lastSSX = 0;

      if (!useClip || ((y - charHeight) < visHeight
              && (y + row.height + charHeight * 2) >= sOffset))
      {// if_in_visible_region
        if (!clipst)
        {
          clipst = true;
          yfrom = y;
          f_i = i;
        }
        yto = y;
        f_to = i;
        if (row.graph > 0)
        {
          if (row.graphGroup > -1 && graphGroupDrawn.get(row.graphGroup))
          {
            continue;
          }

          // this is so that we draw the characters below the graph
          y += row.height;

          if (row.hasText)
          {
            iconOffset = charHeight - fm.getDescent();
            y -= charHeight;
          }
        }
        else if (row.hasText)
        {
          iconOffset = charHeight - fm.getDescent();

        }
        else
        {
          iconOffset = 0;
        }

        if (row.autoCalculated && av.isCalculationInProgress(row))
        {
          y += charHeight;
          usedFaded = true;
          g.drawImage(fadedImage, 0, y - row.height, imgWidth, y, 0,
                  y - row.height, imgWidth, y, annotationPanel);
          g.setColor(Color.black);
          // g.drawString("Calculating "+aa[i].label+"....",20, y-row.height/2);

          continue;
        }

        /*
         * else if (annotationPanel.av.updatingConservation &&
         * aa[i].label.equals("Conservation")) {
         * 
         * y += charHeight; g.drawImage(annotationPanel.fadedImage, 0, y -
         * row.height, annotationPanel.imgWidth, y, 0, y - row.height,
         * annotationPanel.imgWidth, y, annotationPanel);
         * 
         * g.setColor(Color.black); //
         * g.drawString("Calculating Conservation.....",20, y-row.height/2);
         * 
         * continue; } else if (annotationPanel.av.updatingConservation &&
         * aa[i].label.equals("Quality")) {
         * 
         * y += charHeight; g.drawImage(annotationPanel.fadedImage, 0, y -
         * row.height, annotationPanel.imgWidth, y, 0, y - row.height,
         * annotationPanel.imgWidth, y, annotationPanel);
         * g.setColor(Color.black); // /
         * g.drawString("Calculating Quality....",20, y-row.height/2);
         * 
         * continue; }
         */
        // first pass sets up state for drawing continuation from left-hand
        // column
        // of startRes
        x = (startRes == 0) ? 0 : -1;
        while (x < endRes - startRes)
        {
          if (hasHiddenColumns)
          {
            column = hiddenColumns.visibleToAbsoluteColumn(startRes + x);
            if (column > row_annotations.length - 1)
            {
              break;
            }
          }
          else
          {
            column = startRes + x;
          }

          if ((row_annotations == null)
                  || (row_annotations.length <= column)
                  || (row_annotations[column] == null))
          {
            validRes = false;
          }
          else
          {
            validRes = true;
          }
          final String displayChar = validRes
                  ? row_annotations[column].displayCharacter
                  : null;
          if (x > -1)
          {
            if (activeRow == i)
            {
              g.setColor(Color.red);

              if (columnSelection != null)
              {
                if (columnSelection.contains(column))
                {
                  g.fillRect(x * charWidth, y, charWidth, charHeight);
                }
              }
            }
            if (row.getInvalidStrucPos() > x)
            {
              g.setColor(Color.orange);
              g.fillRect(x * charWidth, y, charWidth, charHeight);
            }
            else if (row.getInvalidStrucPos() == x)
            {
              g.setColor(Color.orange.darker());
              g.fillRect(x * charWidth, y, charWidth, charHeight);
            }
            if (validCharWidth && validRes && displayChar != null
                    && (displayChar.length() > 0))
            {
              Graphics2D gg = ((Graphics2D) g);
              float fmWidth = fm.charsWidth(displayChar.toCharArray(), 0,
                      displayChar.length());

              /*
               * shrink label width to fit in column, if that is
               * both configured and necessary
               */
              boolean scaledToFit = false;
              float fmScaling = 1f;
              if (scaleColLabel && fmWidth > charWidth)
              {
                scaledToFit = true;
                fmScaling = charWidth;
                fmScaling /= fmWidth;
                // and update the label's width to reflect the scaling.
                fmWidth = charWidth;
              }

              charOffset = (int) ((charWidth - fmWidth) / 2f);

              if (row_annotations[column].colour == null)
              {
                gg.setColor(Color.black);
              }
              else
              {
                gg.setColor(row_annotations[column].colour);
              }

              /*
               * draw the label, unless it is the same secondary structure
               * symbol (excluding RNA Helix) as the previous column
               */
              final int xPos = (x * charWidth) + charOffset;
              final int yPos = y + iconOffset;

              /*
               * translate to drawing position _before_ applying any scaling
               */
              gg.translate(xPos, yPos);
              if (scaledToFit)
              {
                /*
                 * use a scaling transform to make the label narrower
                 * (JalviewJS doesn't have Font.deriveFont(AffineTransform))
                 */
                gg.transform(
                        AffineTransform.getScaleInstance(fmScaling, 1.0));
              }
              if (column == 0 || row.graph > 0)
              {
                gg.drawString(displayChar, 0, 0);
              }
              else if (row_annotations[column - 1] == null || (labelAllCols
                      || !displayChar.equals(
                              row_annotations[column - 1].displayCharacter)
                      || (displayChar.length() < 2
                              && row_annotations[column].secondaryStructure == ' ')))
              {
                gg.drawString(displayChar, 0, 0);
              }
              if (scaledToFit)
              {
                /*
                 * undo scaling before translating back 
                 * (restoring saved transform does NOT work in JS PDFGraphics!)
                 */
                gg.transform(AffineTransform
                        .getScaleInstance(1D / fmScaling, 1.0));
              }
              gg.translate(-xPos, -yPos);
            }
          }
          if (row.hasIcons)
          {
            char ss = validRes ? row_annotations[column].secondaryStructure
                    : '-';

            if (ss == '(')
            {
              // distinguish between forward/backward base-pairing
              if (displayChar.indexOf(')') > -1)
              {

                ss = ')';

              }
            }
            if (ss == '[')
            {
              if ((displayChar.indexOf(']') > -1))
              {
                ss = ']';

              }
            }
            if (ss == '{')
            {
              // distinguish between forward/backward base-pairing
              if (displayChar.indexOf('}') > -1)
              {
                ss = '}';

              }
            }
            if (ss == '<')
            {
              // distinguish between forward/backward base-pairing
              if (displayChar.indexOf('<') > -1)
              {
                ss = '>';

              }
            }
            if (isRNA && (ss >= CHAR_A) && (ss <= CHAR_Z))
            {
              // distinguish between forward/backward base-pairing
              int ssLowerCase = ss + UPPER_TO_LOWER;
              // TODO would .equals() be safer here? or charAt(0)?
              if (displayChar.indexOf(ssLowerCase) > -1)
              {
                ss = (char) ssLowerCase;
              }
            }

            if (!validRes || (ss != lastSS))
            {

              if (x > -1)
              {

                // int nb_annot = x - temp;
                // System.out.println("\t type :"+lastSS+"\t x :"+x+"\t nbre
                // annot :"+nb_annot);
                switch (lastSS)
                {
                case '(': // Stem case for RNA secondary structure
                case ')': // and opposite direction
                  drawStemAnnot(g, row_annotations, lastSSX, x, y,
                          iconOffset, startRes, column, validRes, validEnd);
                  // temp = x;
                  break;

                case 'H':
                  if (!isRNA)
                  {
                    drawHelixAnnot(g, row_annotations, lastSSX, x, y,
                            iconOffset, startRes, column, validRes,
                            validEnd);
                    break;
                  }
                  // no break if isRNA - falls through to drawNotCanonicalAnnot!
                case 'E':
                  if (!isRNA)
                  {
                    drawSheetAnnot(g, row_annotations, lastSSX, x, y,
                            iconOffset, startRes, column, validRes,
                            validEnd);
                    break;
                  }
                  // no break if isRNA - fall through to drawNotCanonicalAnnot!

                case '{':
                case '}':
                case '[':
                case ']':
                case '>':
                case '<':
                case 'A':
                case 'a':
                case 'B':
                case 'b':
                case 'C':
                case 'c':
                case 'D':
                case 'd':
                case 'e':
                case 'F':
                case 'f':
                case 'G':
                case 'g':
                case 'h':
                case 'I':
                case 'i':
                case 'J':
                case 'j':
                case 'K':
                case 'k':
                case 'L':
                case 'l':
                case 'M':
                case 'm':
                case 'N':
                case 'n':
                case 'O':
                case 'o':
                case 'P':
                case 'p':
                case 'Q':
                case 'q':
                case 'R':
                case 'r':
                case 'S':
                case 's':
                case 'T':
                case 't':
                case 'U':
                case 'u':
                case 'V':
                case 'v':
                case 'W':
                case 'w':
                case 'X':
                case 'x':
                case 'Y':
                case 'y':
                case 'Z':
                case 'z':

                  Color nonCanColor = getNotCanonicalColor(lastSS);
                  drawNotCanonicalAnnot(g, nonCanColor, row_annotations,
                          lastSSX, x, y, iconOffset, startRes, column,
                          validRes, validEnd);
                  // temp = x;
                  break;
                default:
                  g.setColor(Color.gray);
                  g.fillRect(lastSSX, y + 6 + iconOffset,
                          (x * charWidth) - lastSSX, 2);
                  // temp = x;
                  break;
                }
              }
              if (validRes)
              {
                lastSS = ss;
              }
              else
              {
                lastSS = ' ';
              }
              if (x > -1)
              {
                lastSSX = (x * charWidth);
              }
            }
          }
          column++;
          x++;
        }
        if (column >= row_annotations.length)
        {
          column = row_annotations.length - 1;
          validEnd = false;
        }
        else
        {
          validEnd = true;
        }
        if ((row_annotations == null) || (row_annotations.length <= column)
                || (row_annotations[column] == null))
        {
          validRes = false;
        }
        else
        {
          validRes = true;
        }
        // x ++;

        if (row.hasIcons)
        {
          switch (lastSS)
          {

          case 'H':
            if (!isRNA)
            {
              drawHelixAnnot(g, row_annotations, lastSSX, x, y, iconOffset,
                      startRes, column, validRes, validEnd);
              break;
            }
            // no break if isRNA - fall through to drawNotCanonicalAnnot!

          case 'E':
            if (!isRNA)
            {
              drawSheetAnnot(g, row_annotations, lastSSX, x, y, iconOffset,
                      startRes, column, validRes, validEnd);
              break;
            }
            // no break if isRNA - fall through to drawNotCanonicalAnnot!

          case '(':
          case ')': // Stem case for RNA secondary structure

            drawStemAnnot(g, row_annotations, lastSSX, x, y, iconOffset,
                    startRes, column, validRes, validEnd);

            break;
          case '{':
          case '}':
          case '[':
          case ']':
          case '>':
          case '<':
          case 'A':
          case 'a':
          case 'B':
          case 'b':
          case 'C':
          case 'c':
          case 'D':
          case 'd':
          case 'e':
          case 'F':
          case 'f':
          case 'G':
          case 'g':
          case 'h':
          case 'I':
          case 'i':
          case 'J':
          case 'j':
          case 'K':
          case 'k':
          case 'L':
          case 'l':
          case 'M':
          case 'm':
          case 'N':
          case 'n':
          case 'O':
          case 'o':
          case 'P':
          case 'p':
          case 'Q':
          case 'q':
          case 'R':
          case 'r':
          case 'T':
          case 't':
          case 'U':
          case 'u':
          case 'V':
          case 'v':
          case 'W':
          case 'w':
          case 'X':
          case 'x':
          case 'Y':
          case 'y':
          case 'Z':
          case 'z':
            // System.out.println(lastSS);
            Color nonCanColor = getNotCanonicalColor(lastSS);
            drawNotCanonicalAnnot(g, nonCanColor, row_annotations, lastSSX,
                    x, y, iconOffset, startRes, column, validRes, validEnd);
            break;
          default:
            drawGlyphLine(g, row_annotations, lastSSX, x, y, iconOffset,
                    startRes, column, validRes, validEnd);
            break;
          }
        }

        if (row.graph > 0 && row.graphHeight > 0)
        {
          if (row.graph == AlignmentAnnotation.LINE_GRAPH)
          {
            if (row.graphGroup > -1 && !graphGroupDrawn.get(row.graphGroup))
            {
              // TODO: JAL-1291 revise rendering model so the graphGroup map is
              // computed efficiently for all visible labels
              float groupmax = -999999, groupmin = 9999999;
              for (int gg = 0; gg < aa.length; gg++)
              {
                if (aa[gg].graphGroup != row.graphGroup)
                {
                  continue;
                }

                if (aa[gg] != row)
                {
                  aa[gg].visible = false;
                }
                if (aa[gg].graphMax > groupmax)
                {
                  groupmax = aa[gg].graphMax;
                }
                if (aa[gg].graphMin < groupmin)
                {
                  groupmin = aa[gg].graphMin;
                }
              }

              for (int gg = 0; gg < aa.length; gg++)
              {
                if (aa[gg].graphGroup == row.graphGroup)
                {
                  drawLineGraph(g, aa[gg], aa[gg].annotations, startRes,
                          endRes, y, groupmin, groupmax, row.graphHeight);
                }
              }

              graphGroupDrawn.set(row.graphGroup);
            }
            else
            {
              drawLineGraph(g, row, row_annotations, startRes, endRes, y,
                      row.graphMin, row.graphMax, row.graphHeight);
            }
          }
          else if (row.graph == AlignmentAnnotation.BAR_GRAPH)
          {
            drawBarGraph(g, row, row_annotations, startRes, endRes,
                    row.graphMin, row.graphMax, y, renderHistogram,
                    renderProfile, normaliseProfile);
          }
          else
          {
            AnnotationRowRendererI renderer = rendererFactoryI
                    .getRendererFor(row);
            if (renderer != null)
            {
              renderer.renderRow(g, charWidth, charHeight, hasHiddenColumns,
                      av, hiddenColumns, columnSelection, row,
                      row_annotations, startRes, endRes, row.graphMin,
                      row.graphMax, y);
            }
            if (debugRedraw)
            {
              if (renderer == null)
              {
                System.err
                        .println("No renderer found for " + row.toString());
              }
              else
              {
                System.err.println(
                        "rendered with " + renderer.getClass().toString());
              }
            }

          }
        }
      }
      else
      {
        if (clipst && !clipend)
        {
          clipend = true;
        }
      } // end if_in_visible_region
      if (row.graph > 0 && row.hasText)
      {
        y += charHeight;
      }

      if (row.graph == 0)
      {
        y += aa[i].height;
      }
    }
    if (debugRedraw)
    {
      if (canClip)
      {
        if (clipst)
        {
          System.err.println(
                  "Start clip at : " + yfrom + " (index " + f_i + ")");
        }
        if (clipend)
        {
          System.err.println(
                  "End clip at : " + yto + " (index " + f_to + ")");
        }
      }
      ;
      System.err.println("Annotation Rendering time:"
              + (System.currentTimeMillis() - stime));
    }
    ;

    return !usedFaded;
  }

  public static final Color GLYPHLINE_COLOR = Color.gray;

  public static final Color SHEET_COLOUR = Color.green;

  public static final Color HELIX_COLOUR = Color.red;

  public static final Color STEM_COLOUR = Color.blue;

  // private Color sdNOTCANONICAL_COLOUR;

  void drawGlyphLine(Graphics g, Annotation[] row, int lastSSX, int x,
          int y, int iconOffset, int startRes, int column, boolean validRes,
          boolean validEnd)
  {
    g.setColor(GLYPHLINE_COLOR);
    g.fillRect(lastSSX, y + 6 + iconOffset, (x * charWidth) - lastSSX, 2);
  }

  void drawSheetAnnot(Graphics g, Annotation[] row,

          int lastSSX, int x, int y, int iconOffset, int startRes,
          int column, boolean validRes, boolean validEnd)
  {
    g.setColor(SHEET_COLOUR);

    if (!validEnd || !validRes || row == null || row[column] == null
            || row[column].secondaryStructure != 'E')
    {
      g.fillRect(lastSSX, y + 4 + iconOffset, (x * charWidth) - lastSSX - 4,
              7);
      g.fillPolygon(
              new int[]
              { (x * charWidth) - 4, (x * charWidth) - 4, (x * charWidth) },
              new int[]
              { y + iconOffset, y + 14 + iconOffset, y + 7 + iconOffset },
              3);
    }
    else
    {
      g.fillRect(lastSSX, y + 4 + iconOffset, (x + 1) * charWidth - lastSSX,
              7);
    }

  }

  void drawHelixAnnot(Graphics g, Annotation[] row, int lastSSX, int x,
          int y, int iconOffset, int startRes, int column, boolean validRes,
          boolean validEnd)
  {
    g.setColor(HELIX_COLOUR);

    int sCol = (lastSSX / charWidth)
            + hiddenColumns.visibleToAbsoluteColumn(startRes);
    int x1 = lastSSX;
    int x2 = (x * charWidth);

    if (USE_FILL_ROUND_RECT)
    {
      int ofs = charWidth / 2;
      // Off by 1 offset when drawing rects and ovals
      // to offscreen image on the MAC
      g.fillRoundRect(lastSSX, y + 4 + iconOffset, x2 - x1, 8, 8, 8);
      if (sCol == 0 || row[sCol - 1] == null
              || row[sCol - 1].secondaryStructure != 'H')
      {
      }
      else
      {
        // g.setColor(Color.orange);
        g.fillRoundRect(lastSSX, y + 4 + iconOffset, x2 - x1 - ofs + 1, 8,
                0, 0);
      }
      if (!validRes || row[column] == null
              || row[column].secondaryStructure != 'H')
      {

      }
      else
      {
        // g.setColor(Color.magenta);
        g.fillRoundRect(lastSSX + ofs, y + 4 + iconOffset,
                x2 - x1 - ofs + 1, 8, 0, 0);

      }

      return;
    }

    if (sCol == 0 || row[sCol - 1] == null
            || row[sCol - 1].secondaryStructure != 'H')
    {
      g.fillArc(lastSSX, y + 4 + iconOffset, charWidth, 8, 90, 180);
      x1 += charWidth / 2;
    }

    if (!validRes || row[column] == null
            || row[column].secondaryStructure != 'H')
    {
      g.fillArc((x * charWidth) - charWidth, y + 4 + iconOffset, charWidth,
              8, 270, 180);
      x2 -= charWidth / 2;
    }

    g.fillRect(x1, y + 4 + iconOffset, x2 - x1, 8);
  }

  void drawLineGraph(Graphics g, AlignmentAnnotation _aa,
          Annotation[] aa_annotations, int sRes, int eRes, int y, float min,
          float max, int graphHeight)
  {
    if (sRes > aa_annotations.length)
    {
      return;
    }

    int x = 0;

    // Adjustment for fastpaint to left
    if (eRes < endRes)
    {
      eRes++;
    }

    eRes = Math.min(eRes, aa_annotations.length);

    if (sRes == 0)
    {
      x++;
    }

    int y1 = y, y2 = y;
    float range = max - min;

    // //Draw origin
    if (min < 0)
    {
      y2 = y - (int) ((0 - min / range) * graphHeight);
    }

    g.setColor(Color.gray);
    g.drawLine(x - charWidth, y2, (eRes - sRes + 1) * charWidth, y2);

    eRes = Math.min(eRes, aa_annotations.length);

    int column;
    int aaMax = aa_annotations.length - 1;

    while (x < eRes - sRes)
    {
      column = sRes + x;
      if (hasHiddenColumns)
      {
        column = hiddenColumns.visibleToAbsoluteColumn(column);
      }

      if (column > aaMax)
      {
        break;
      }

      if (aa_annotations[column] == null)
      {
        x++;
        continue;
      }

      if (aa_annotations[column].colour == null)
      {
        g.setColor(Color.black);
      }
      else
      {
        g.setColor(aa_annotations[column].colour);
      }

      if (aa_annotations[column - 1] == null
              && aa_annotations.length > column + 1
              && aa_annotations[column + 1] == null)
      {
        // standalone value
        y1 = y - (int) (((aa_annotations[column].value - min) / range)
                * graphHeight);
        g.drawLine(x * charWidth + charWidth / 4, y1,
                x * charWidth + 3 * charWidth / 4, y1);
        x++;
        continue;
      }

      if (aa_annotations[column - 1] == null)
      {
        x++;
        continue;
      }

      y1 = y - (int) (((aa_annotations[column - 1].value - min) / range)
              * graphHeight);
      y2 = y - (int) (((aa_annotations[column].value - min) / range)
              * graphHeight);

      g.drawLine(x * charWidth - charWidth / 2, y1,
              x * charWidth + charWidth / 2, y2);
      x++;
    }

    if (_aa.threshold != null)
    {
      g.setColor(_aa.threshold.colour);
      Graphics2D g2 = (Graphics2D) g;
      g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
              BasicStroke.JOIN_ROUND, 3f, new float[]
              { 5f, 3f }, 0f));

      y2 = (int) (y - ((_aa.threshold.value - min) / range) * graphHeight);
      g.drawLine(0, y2, (eRes - sRes) * charWidth, y2);
      g2.setStroke(new BasicStroke());
    }
  }

  @SuppressWarnings("unused")
  void drawBarGraph(Graphics g, AlignmentAnnotation _aa,
          Annotation[] aa_annotations, int sRes, int eRes, float min,
          float max, int y, boolean renderHistogram, boolean renderProfile,
          boolean normaliseProfile)
  {
    if (sRes > aa_annotations.length)
    {
      return;
    }
    Font ofont = g.getFont();
    eRes = Math.min(eRes, aa_annotations.length);

    int x = 0, y1 = y, y2 = y;

    float range = max - min;

    if (min < 0)
    {
      y2 = y - (int) ((0 - min / (range)) * _aa.graphHeight);
    }

    g.setColor(Color.gray);

    g.drawLine(x, y2, (eRes - sRes) * charWidth, y2);

    int column;
    int aaMax = aa_annotations.length - 1;
    while (x < eRes - sRes)
    {
      column = sRes + x;
      if (hasHiddenColumns)
      {
        column = hiddenColumns.visibleToAbsoluteColumn(column);
      }

      if (column > aaMax)
      {
        break;
      }

      if (aa_annotations[column] == null)
      {
        x++;
        continue;
      }
      if (aa_annotations[column].colour == null)
      {
        g.setColor(Color.black);
      }
      else
      {
        g.setColor(aa_annotations[column].colour);
      }

      y1 = y - (int) (((aa_annotations[column].value - min) / (range))
              * _aa.graphHeight);

      if (renderHistogram)
      {
        if (y1 - y2 > 0)
        {
          g.fillRect(x * charWidth, y2, charWidth, y1 - y2);
        }
        else
        {
          g.fillRect(x * charWidth, y1, charWidth, y2 - y1);
        }
      }
      // draw profile if available
      if (renderProfile)
      {

        /*
         * {profile type, #values, total count, char1, pct1, char2, pct2...}
         */
        int profl[] = getProfileFor(_aa, column);

        // just try to draw the logo if profl is not null
        if (profl != null && profl[2] != 0)
        {
          boolean isStructureProfile = profl[0] == AlignmentAnnotation.STRUCTURE_PROFILE;
          boolean isCdnaProfile = profl[0] == AlignmentAnnotation.CDNA_PROFILE;
          float ht = normaliseProfile ? y - _aa.graphHeight : y1;
          final double normaliseFactor = normaliseProfile ? _aa.graphHeight
                  : (y2 - y1);

          /**
           * Render a single base for a sequence profile, a base pair for
           * structure profile, and a triplet for a cdna profile
           */
          char[] dc = new char[isStructureProfile ? 2
                  : (isCdnaProfile ? 3 : 1)];

          // lm is not necessary - we can just use fm - could be off by no more
          // than 0.5 px
          // LineMetrics lm = g.getFontMetrics(ofont).getLineMetrics("Q", g);
          // System.out.println(asc + " " + dec + " " + (asc - lm.getAscent())
          // + " " + (dec - lm.getDescent()));

          double asc = fm.getAscent();
          double dec = fm.getDescent();
          double fht = fm.getHeight();

          double scale = 1f / (normaliseProfile ? profl[2] : 100f);
          // float ofontHeight = 1f / fm.getAscent();// magnify to fill box

          /*
           * Traverse the character(s)/percentage data in the array
           */

          float ht2 = ht;

          // profl[1] is the number of values in the profile
          for (int i = 0, c = 3, last = profl[1]; i < last; i++)
          {

            String s;
            if (isStructureProfile)
            {
              // todo can we encode a structure pair as an int, like codons?
              dc[0] = (char) profl[c++];
              dc[1] = (char) profl[c++];
              s = new String(dc);
            }
            else if (isCdnaProfile)
            {
              CodingUtils.decodeCodon2(profl[c++], dc);
              s = new String(dc);
            }
            else
            {
              dc[0] = (char) profl[c++];
              s = new String(dc);
            }
            // next profl[] position is profile % for the character(s)

            int percent = profl[c++];
            if (percent == 0)
            {
              // failsafe in case a count rounds down to 0%
              continue;
            }
            double newHeight = normaliseFactor * scale * percent;

            /*
             * Set character colour as per alignment colour scheme; use the
             * codon translation if a cDNA profile
             */
            Color colour = null;
            if (isCdnaProfile)
            {
              final String codonTranslation = ResidueProperties
                      .codonTranslate(s);
              colour = profcolour.findColour(codonTranslation.charAt(0),
                      column, null);
            }
            else
            {
              colour = profcolour.findColour(dc[0], column, null);
            }
            g.setColor(colour == Color.white ? Color.lightGray : colour);

            // Debug - render boxes around characters
            // g.setColor(Color.red);
            // g.drawRect(x*av.charWidth, (int)ht, av.charWidth,
            // (int)(scl));
            // g.setColor(profcolour.findColour(dc[0]).darker());

            double sx = 1f * charWidth / fm.charsWidth(dc, 0, dc.length);
            double sy = newHeight / asc;
            double newAsc = asc * sy;
            double newDec = dec * sy;
            // it is not necessary to recalculate lm for the new font.
            // note: lm.getBaselineOffsets()[lm.getBaselineIndex()]) must be 0
            // by definition. Was:
            // int hght = (int) (ht + (newAsc - newDec);
            // - lm.getBaselineOffsets()[lm.getBaselineIndex()]));

            if (Platform.isJS())
            {
              /*
               * SwingJS does not implement font.deriveFont()
               * so use a scaling transform to draw instead,
               * this is off by a very small amount
               */
              final int hght = (int) (ht2 + (newAsc - newDec));
              Graphics2D gg = (Graphics2D) g;
              int xShift = (int) Math.round(x * charWidth / sx);
              int yShift = (int) Math.round(hght / sy);
              gg.transform(AffineTransform.getScaleInstance(sx, sy));
              gg.drawString(s, xShift, yShift);
              gg.transform(
                      AffineTransform.getScaleInstance(1D / sx, 1D / sy));
              ht2 += newHeight;
            }
            else
            /**
             * Java only
             * 
             * @j2sIgnore
             */
            {
              // Java ('normal') method is to scale the font to fit

              final int hght = (int) (ht + (newAsc - newDec));
              Font font = ofont
                      .deriveFont(AffineTransform.getScaleInstance(sx, sy));
              g.setFont(font);
              g.drawChars(dc, 0, dc.length, x * charWidth, hght);
              g.setFont(ofont);

              ht += newHeight;
            }
          }
        }
      }
      x++;
    }
    if (_aa.threshold != null)
    {
      g.setColor(_aa.threshold.colour);
      Graphics2D g2 = (Graphics2D) g;
      g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
              BasicStroke.JOIN_ROUND, 3f, new float[]
              { 5f, 3f }, 0f));

      y2 = (int) (y
              - ((_aa.threshold.value - min) / range) * _aa.graphHeight);
      g.drawLine(0, y2, (eRes - sRes) * charWidth, y2);
      g2.setStroke(new BasicStroke());
    }
  }

  // used by overview window
  public void drawGraph(Graphics g, AlignmentAnnotation _aa,
          Annotation[] aa_annotations, int width, int y, int sRes, int eRes)
  {
    eRes = Math.min(eRes, aa_annotations.length);
    g.setColor(Color.white);
    g.fillRect(0, 0, width, y);
    g.setColor(new Color(0, 0, 180));

    int x = 0, height;

    for (int j = sRes; j < eRes; j++)
    {
      if (aa_annotations[j] != null)
      {
        if (aa_annotations[j].colour == null)
        {
          g.setColor(Color.black);
        }
        else
        {
          g.setColor(aa_annotations[j].colour);
        }

        height = (int) ((aa_annotations[j].value / _aa.graphMax) * y);
        if (height > y)
        {
          height = y;
        }

        g.fillRect(x, y - height, charWidth, height);
      }
      x += charWidth;
    }
  }

  Color getNotCanonicalColor(char lastss)
  {
    switch (lastss)
    {
    case '{':
    case '}':
      return new Color(255, 125, 5);

    case '[':
    case ']':
      return new Color(245, 115, 10);

    case '>':
    case '<':
      return new Color(235, 135, 15);

    case 'A':
    case 'a':
      return new Color(225, 105, 20);

    case 'B':
    case 'b':
      return new Color(215, 145, 30);

    case 'C':
    case 'c':
      return new Color(205, 95, 35);

    case 'D':
    case 'd':
      return new Color(195, 155, 45);

    case 'E':
    case 'e':
      return new Color(185, 85, 55);

    case 'F':
    case 'f':
      return new Color(175, 165, 65);

    case 'G':
    case 'g':
      return new Color(170, 75, 75);

    case 'H':
    case 'h':
      return new Color(160, 175, 85);

    case 'I':
    case 'i':
      return new Color(150, 65, 95);

    case 'J':
    case 'j':
      return new Color(140, 185, 105);

    case 'K':
    case 'k':
      return new Color(130, 55, 110);

    case 'L':
    case 'l':
      return new Color(120, 195, 120);

    case 'M':
    case 'm':
      return new Color(110, 45, 130);

    case 'N':
    case 'n':
      return new Color(100, 205, 140);

    case 'O':
    case 'o':
      return new Color(90, 35, 150);

    case 'P':
    case 'p':
      return new Color(85, 215, 160);

    case 'Q':
    case 'q':
      return new Color(75, 25, 170);

    case 'R':
    case 'r':
      return new Color(65, 225, 180);

    case 'S':
    case 's':
      return new Color(55, 15, 185);

    case 'T':
    case 't':
      return new Color(45, 235, 195);

    case 'U':
    case 'u':
      return new Color(35, 5, 205);

    case 'V':
    case 'v':
      return new Color(25, 245, 215);

    case 'W':
    case 'w':
      return new Color(15, 0, 225);

    case 'X':
    case 'x':
      return new Color(10, 255, 235);

    case 'Y':
    case 'y':
      return new Color(5, 150, 245);

    case 'Z':
    case 'z':
      return new Color(0, 80, 255);

    default:
      System.out.println("This is not a interaction : " + lastss);
      return null;

    }
  }
}
