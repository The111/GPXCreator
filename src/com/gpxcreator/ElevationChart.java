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

import com.gpxcreator.gpxpanel.Route;

@SuppressWarnings("serial")
public class ElevationChart extends JFrame {

    public ElevationChart(String s, Route route) {
        super(s);
        XYDataset xydataset = createDataset(route);
        JFreeChart jfreechart = createChart(xydataset, route);
        jfreechart.setRenderingHints(
                new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        ChartPanel chartpanel = new ChartPanel(jfreechart);
        chartpanel.setMaximumDrawHeight(99999);
        chartpanel.setMaximumDrawWidth(99999);
        chartpanel.setMinimumDrawHeight(1);
        chartpanel.setMinimumDrawWidth(1);
        setContentPane(chartpanel);
    }

    private static XYDataset createDataset(Route route) {
        /*XYSeries xyseries = new XYSeries(route.getName());
        double lengthMeters = 0;
        double lengthMiles = 0;
        Waypoint curr = route.getStart();
        Waypoint prev;
        for (Waypoint rtept : route.getRoutePoints()) {
            prev = curr;
            curr = rtept;
            double increment = OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
            if (!Double.isNaN(increment)) {
                lengthMeters += OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
                lengthMiles = lengthMeters * 0.000621371;
            }
            xyseries.add(new Double(lengthMiles), new Double(curr.getEle() * 3.28084));
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(xyseries);
        xyseriescollection.setIntervalWidth(0.0D);
        return xyseriescollection;*/
        return null; ///////////////////////////////////////////////////// REMOVE THIS LINE! //////////////////////
    }

    private static JFreeChart createChart(XYDataset xydataset, Route route) {
        JFreeChart jfreechart = ChartFactory.createXYAreaChart(
                route.getName(), "Distance (miles)", "Elevation (ft)",
                xydataset, PlotOrientation.VERTICAL, false, false, false);
        XYPlot xyplot = (XYPlot)jfreechart.getPlot();
        xyplot.getRenderer().setSeriesPaint(0, new Color(38, 128, 224));
        xyplot.setForegroundAlpha(0.65F);
        
        ValueAxis domainAxis = xyplot.getDomainAxis();
        domainAxis.setRange(0, route.getLengthMiles());
        
        double eleMin = route.getEleMinFeet();
        double eleMax = route.getEleMaxFeet();
        double eleChange = eleMax - eleMin;
        double padding = eleChange / 10D;
        double rangeMin = eleMin - padding;
        if (eleMin >= 0 & rangeMin < 0) {
            rangeMin = 0;
        }
        double rangeMax = eleMax + padding;
        ValueAxis rangeAxis = xyplot.getRangeAxis();
        rangeAxis.setRange(rangeMin, rangeMax);

        domainAxis.setTickMarkPaint(Color.black);
        domainAxis.setLowerMargin(0.0D);
        domainAxis.setUpperMargin(0.0D);
        rangeAxis.setTickMarkPaint(Color.black);
        return jfreechart;
    }
}
