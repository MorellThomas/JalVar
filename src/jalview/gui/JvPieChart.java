package jalview.gui;

import org.knowm.xchart.style.PieStyler.LabelType;
import org.knowm.xchart.style.Styler.ChartTheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;

public class JvPieChart
{
  //private final String title;
  
  private final PieChart pie;

  private final HashMap<Character, Integer> mapAAtoColourIndex = new HashMap<Character, Integer>();

  private final Color[] referenceColourScheme = jalview.schemes.ResidueProperties.ocean;

  private final LinkedHashMap<String, Float> dataMapping;
  
  private Color[] seriesColours;
  
  private String selectedSequence;
  
  private boolean value;  // show value instead of %
  
  private JFrame frame;
  
  private JPanel piePanel;
  
  public JvPieChart(String title, HashMap<? extends Object, ? extends Number> map, String selSeq)
  {
    this(title, map, selSeq, false);
  }
  public JvPieChart(String title, HashMap<? extends Object, ? extends Number> map, String selSeq, boolean val)
  {
    this.dataMapping = new LinkedHashMap<String, Float>();
    for (Object o : map.keySet())
    {
      if (o instanceof String)
      {
        dataMapping.put((String) o, (Float) map.get(o).floatValue());
      } else if (o instanceof Character) {
        dataMapping.put(Character.toString((Character) o), (Float) map.get(o).floatValue());
      }
    }
    this.selectedSequence = selSeq;
    pie = new PieChartBuilder().width(800).height(700).title(title).theme(ChartTheme.GGPlot2).build();
    
    this.value = val;
    
    init();
  }
  
  private void init()
  {
    int i = 0;
    for (char aa : new char[]{'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', 'B', 'Z', 'X', '-', '*', '.'})
    {
      mapAAtoColourIndex.put(aa, i++);
    }

    seriesColours = new Color[dataMapping.keySet().size()];
    
    pie.getStyler().setLegendVisible(false);
    pie.getStyler().setPlotContentSize(0.8);
    if (value)
    {
      pie.getStyler().setLabelType(LabelType.NameAndValue);
    } else {
      pie.getStyler().setLabelType(LabelType.NameAndPercentage);
    }
    pie.getStyler().setLabelsDistance(1.12);
    pie.getStyler().setLabelsFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    pie.getStyler().setPlotBackgroundColor(Color.white);
    pie.getStyler().setForceAllLabelsVisible(true);
    //pie.getStyler().setStartAngleInDegrees(90);
    
    //sort mapping and replace old one
    sort();

    i = 0;
    for (String aa : dataMapping.keySet())
    {
      pie.addSeries(aa, dataMapping.get(aa));
      seriesColours[i++] = referenceColourScheme[mapAAtoColourIndex.get(aa.charAt(0))];
    }

    pie.getStyler().setSeriesColors(seriesColours);
    
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        frame = new JFrame(selectedSequence);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        piePanel = new XChartPanel<PieChart>(pie);
        frame.add(piePanel, BorderLayout.CENTER);
        
        //JLabel label = new JLabel("alsjdlf", SwingConstants.CENTER);
        //frame.add(label, BorderLayout.SOUTH);
        
        frame.pack();
        frame.setVisible(true);
      }
    });
    
    //new SwingWrapper(pie).displayChart();
  }
  
  private void sort()
  {
    //sorting arrays...
    Object[] aasUnsorted = dataMapping.keySet().toArray();
    String[] aasSorted = new String[dataMapping.size()];
    Float[] percentUnsorted = new Float[dataMapping.size()];
    Float[] percentSorted = new Float[dataMapping.size()];
    int i = 0;
    for (String aa : dataMapping.keySet())
    {
      percentUnsorted[i] = dataMapping.get(aa);
      percentSorted[i++] = dataMapping.get(aa);
    }
    Arrays.sort(percentSorted, Collections.reverseOrder());
    
    HashSet<Integer> ignore = new HashSet<Integer>();
    if (percentSorted.length > 0)
    {
      for (int l = 0; l < aasSorted.length; l++)
      {
        for (int k = 0; k < aasSorted.length; k++)
        {
          if (percentUnsorted[l] == percentSorted[k] && !ignore.contains(k))
          {
            aasSorted[k] = (String) aasUnsorted[l];
            ignore.add(k);
            break;
          }
        }
      }
    }
    
    dataMapping.clear();
    for (int j = 0; j < aasSorted.length; j++)
    {
      dataMapping.put(aasSorted[j], percentSorted[j]);
    }

  }
  
}
