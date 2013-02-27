package com.gpxcreator;

import java.awt.Color;
import java.awt.RenderingHints;

import javax.swing.JFrame;

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

import com.gpxcreator.gpxpanel.Waypoint;
import com.gpxcreator.gpxpanel.WaypointGroup;

@SuppressWarnings("serial")
public class SpeedChart extends JFrame {

    public SpeedChart(String title, String headingPrefix, WaypointGroup wptGrp) {
        super(title);
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

    private static XYDataset createDataset(WaypointGroup wptGrp) {
        XYSeries xyseries = new XYSeries(wptGrp.getName());
        double lengthMeters = 0;
        double lengthMiles = 0;
        Waypoint curr = wptGrp.getStart();
        Waypoint prev;
        for (Waypoint wpt : wptGrp.getWaypoints()) {
            prev = curr;
            curr = wpt;
            double incrementMeters = OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
            double incrementMillis = curr.getTime().getTime() - prev.getTime().getTime();
            double incrementHours = incrementMillis / 3600000D;
            if (!Double.isNaN(incrementMeters)) {
                lengthMeters += incrementMeters;
                lengthMiles = lengthMeters * 0.000621371;
            }
            double incrementMiles = incrementMeters * 0.000621371;
            xyseries.add(new Double(lengthMiles), new Double(incrementMiles / incrementHours));
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(xyseries);
        xyseriescollection.setIntervalWidth(0.0D);
        return xyseriescollection;
    }
    
    private static JFreeChart createChart(XYDataset xydataset, WaypointGroup wptGrp, String headingPrefix) {
        JFreeChart jfreechart = null;
        jfreechart = ChartFactory.createXYAreaChart(
            headingPrefix + " - " + wptGrp.getName(), "Distance (miles)", "Speed (mph)",
            xydataset, PlotOrientation.VERTICAL, false, false, false);
        
        XYPlot xyplot = (XYPlot)jfreechart.getPlot();
        xyplot.getRenderer().setSeriesPaint(0, new Color(38, 128, 224));
        xyplot.setForegroundAlpha(0.65F);
        
        ValueAxis domainAxis = xyplot.getDomainAxis();
        domainAxis.setRange(0, wptGrp.getLengthMiles());
        
        double speedMax = wptGrp.getMaxSpeedMph();
        double padding = speedMax / 10D;
        double rangeMax = speedMax + padding;
        ValueAxis rangeAxis = xyplot.getRangeAxis();
        rangeAxis.setRange(0, rangeMax);

        domainAxis.setTickMarkPaint(Color.black);
        domainAxis.setLowerMargin(0.0D);
        domainAxis.setUpperMargin(0.0D);
        rangeAxis.setTickMarkPaint(Color.black);
        return jfreechart;
    }
}
