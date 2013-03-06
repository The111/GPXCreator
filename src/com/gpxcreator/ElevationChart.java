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
import com.gpxcreator.gpxpanel.WaypointGroup.WptGrpType;

@SuppressWarnings("serial")
public class ElevationChart extends JFrame {

    public ElevationChart(String title, String headingPrefix, WaypointGroup wptGrp) {
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

    private XYDataset createDataset(WaypointGroup wptGrp) {
        XYSeries xyseries = new XYSeries(wptGrp.getName());
        double lengthMeters = 0;
        double lengthMiles = 0;
        Waypoint curr = wptGrp.getStart();
        Waypoint prev;
        for (Waypoint wpt : wptGrp.getWaypoints()) {
            prev = curr;
            curr = wpt;
            double increment = OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
            if (!Double.isNaN(increment)) {
                lengthMeters += increment;
                lengthMiles = lengthMeters * 0.000621371;
            }
            xyseries.add(new Double(lengthMiles), new Double(curr.getEle() * 3.28084));
        }
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(xyseries);
        xyseriescollection.setIntervalWidth(0.0D);
        return xyseriescollection;
    }
    
    private JFreeChart createChart(XYDataset xydataset, WaypointGroup wptGrp, String headingPrefix) {
        JFreeChart jfreechart = null;
        if (wptGrp.getWptGrpType() == WptGrpType.WAYPOINTS) {
            jfreechart = ChartFactory.createScatterPlot(
                    headingPrefix + " - " + wptGrp.getName(), "Distance (miles)", "Elevation (ft)",
                    xydataset, PlotOrientation.VERTICAL, false, false, false);
        } else {
            jfreechart = ChartFactory.createXYAreaChart(
                headingPrefix + " - " + wptGrp.getName(), "Distance (miles)", "Elevation (ft)",
                xydataset, PlotOrientation.VERTICAL, false, false, false);
        }
        
        XYPlot xyplot = (XYPlot)jfreechart.getPlot();
        xyplot.getRenderer().setSeriesPaint(0, new Color(38, 128, 224));
        xyplot.setForegroundAlpha(0.65F);
        
        ValueAxis domainAxis = xyplot.getDomainAxis();
        domainAxis.setRange(0, wptGrp.getLengthMiles());
        
        double eleMin = wptGrp.getEleMinFeet();
        double eleMax = wptGrp.getEleMaxFeet();
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
