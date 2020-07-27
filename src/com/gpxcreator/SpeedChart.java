package com.gpxcreator;

import com.gpxcreator.gpxpanel.Waypoint;
import com.gpxcreator.gpxpanel.WaypointGroup;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openstreetmap.gui.jmapviewer.OsmMercator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A chart for displaying a GPX element's speed profile.
 *
 * @author Matt Hoover
 */
@SuppressWarnings("serial")
public class SpeedChart extends JFrame {

  private double maxRawSpeedMph;

  /**
   * Constructs the {@link SpeedChart} window.
   *
   * @param title         The chart window title.
   * @param headingPrefix The heading for the graphics on the chart.
   * @param wptGrp        The GPX element being plotted.
   */
  public SpeedChart(String title, String headingPrefix, WaypointGroup wptGrp) {
    super(title);
    maxRawSpeedMph = 0;
    XYDataset xydataset = createDataset(wptGrp);
    JFreeChart jfreechart = createChart(xydataset, wptGrp, headingPrefix);
    jfreechart.setRenderingHints(
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    ChartPanel chartpanel = new ChartPanel(jfreechart);
    chartpanel.setMaximumDrawHeight(99999);
    chartpanel.setMaximumDrawWidth(99999);
    chartpanel.setMinimumDrawHeight(1);
    chartpanel.setMinimumDrawWidth(1);
    setContentPane(chartpanel);
  }

  /**
   * Creates the dataset to be used on the chart.
   */
  private XYDataset createDataset(WaypointGroup wptGrp) {
    XYSeries xyseries = new XYSeries(wptGrp.getName());
    double lengthMeters = 0;
    double lengthMiles = 0;
    Waypoint curr = wptGrp.getStart();
    Waypoint prev;
    List<Double> distances = new ArrayList<>();
    List<Double> speeds = new ArrayList<>();
    for (Waypoint wpt : wptGrp.getWaypoints()) {
      prev = curr;
      curr = wpt;
      double incrementMeters =
          new OsmMercator().getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
      double incrementMillis = curr.getTime().getTime() - prev.getTime().getTime();
      double incrementHours = incrementMillis / 3600000D;
      if (!Double.isNaN(incrementMeters) && !Double.isNaN(incrementMillis) && incrementHours > 0) {
        lengthMeters += incrementMeters;
        lengthMiles = lengthMeters * 0.000621371;
        double incrementMiles = incrementMeters * 0.000621371;
        distances.add(lengthMiles);
        speeds.add(incrementMiles / incrementHours);
      }
    }

    maxRawSpeedMph = Double.NEGATIVE_INFINITY;
    for (double b = 0.01; b <= 0.5; b+=0.3) {
      try {
        double[] speedsSmoothed =
            new LoessInterpolator(b /* 0.3 default */, 20 /* 2 default */).smooth(
                distances.stream().mapToDouble(Double::doubleValue).toArray(),
                speeds.stream().mapToDouble(Double::doubleValue).toArray());
        for (int i = 0; i < speedsSmoothed.length; i++) {
          xyseries.add(distances.get(i), new Double(speedsSmoothed[i]));
          maxRawSpeedMph = Math.max(speedsSmoothed[i], maxRawSpeedMph);
        }
        System.out.println(b);
        break;
      } catch (Exception e) {
      }
    }

    XYSeriesCollection xyseriescollection = new XYSeriesCollection();
    xyseriescollection.addSeries(xyseries);
    xyseriescollection.setIntervalWidth(0.0D);
    return xyseriescollection;
  }

  /**
   * Creates the chart to be used in the window frame.
   */
  private JFreeChart createChart(XYDataset xydataset, WaypointGroup wptGrp, String headingPrefix) {
    JFreeChart jfreechart = null;
    jfreechart = ChartFactory.createXYLineChart(
        headingPrefix + " - " + wptGrp.getName(), "Distance (miles)", "Speed (mph)",
        xydataset, PlotOrientation.VERTICAL, false, false, false);

    XYPlot xyplot = (XYPlot) jfreechart.getPlot();
    xyplot.getRenderer().setSeriesPaint(0, new Color(255, 0, 0));
    xyplot.setForegroundAlpha(0.65F);
    xyplot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

    ValueAxis domainAxis = xyplot.getDomainAxis();
    domainAxis.setRange(0, wptGrp.getLengthMiles());

    double padding = maxRawSpeedMph / 10D;
    double rangeMax = maxRawSpeedMph + padding;
    ValueAxis rangeAxis = xyplot.getRangeAxis();
    rangeAxis.setRange(0, rangeMax);

    domainAxis.setTickMarkPaint(Color.black);
    domainAxis.setLowerMargin(0.0D);
    domainAxis.setUpperMargin(0.0D);
    rangeAxis.setTickMarkPaint(Color.black);
    return jfreechart;
  }
}
