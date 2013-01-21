package com.gpxcreator.gpxpanel;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

public class Route {

    private String name;
    private static int unnamedCounter = 0;
    private List<RoutePoint> routePoints;
    
    // fields for GPX Creator
    private boolean visible;
    private boolean active;    
    private Color color;
    private static Color[] colors = { // some standard random colors
        new Color(255,  0,  0), new Color(  0,255,  0), new Color(  0,  0,255),
        new Color(255,255,  0), new Color(255,  0,255), new Color(  0,255,255),
        new Color(127,  0,255), new Color(255,127,  0), new Color(255,255,255)
    };
    private static int currentColor = 0;

    private String type;
    private long duration;
    private double maxSpeedKmph;
    private double maxSpeedMph;
    private double lengthMeters;
    private double lengthMiles;
    private double eleStartMeters;
    private double eleStartFeet;
    private double eleEndMeters;
    private double eleEndFeet;
    private double eleMinMeters;
    private double eleMinFeet;
    private double eleMaxMeters;
    private double eleMaxFeet;
    private double grossRiseFeet;
    private double grossRiseMeters;
    private double grossFallFeet;
    private double grossFallMeters;
    private long riseTime;
    private long fallTime;
    
    /**
     * Creates an empty {@link #Route}.
     * 
     * @param routeName
     *            The name of the route. 
     */
    public Route(String routeName) {
        this.visible = true;
        this.name = routeName;
        this.routePoints = new ArrayList<RoutePoint>();
        this.color = colors[(currentColor++) % colors.length];
    }
    
    /**
     * Creates a {@link #Route} from a GPX file.
     * 
     * @param gpx
     *            The GPX file.
     */
    public Route(File gpx) {
        boolean lookForName = false;
        this.visible = true;
        this.name = "";
        this.routePoints = new ArrayList<RoutePoint>();
        this.color = colors[(currentColor++) % colors.length];
        RoutePoint curr = null;
        double lat, lon;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(gpx);
        } catch (FileNotFoundException e) {
            System.err.println("GPX file not found.");
            e.printStackTrace();
        }
        XMLInputFactory xif = XMLInputFactory.newInstance();
        try {
            XMLStreamReader xsr = xif.createXMLStreamReader(fis);
            while (xsr.hasNext()) {
                xsr.next();
                switch (xsr.getEventType()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (xsr.getLocalName()) {
                            case "rtept":
                            case "trkpt":
                                lat = Double.parseDouble(xsr.getAttributeValue("", "lat"));
                                lon = Double.parseDouble(xsr.getAttributeValue("", "lon"));
                                curr = new RoutePoint(lat, lon);
                                this.addRoutePoint(curr);
                                break;
                            case "metadata":
                            case "rte":
                            case "trk":
                                lookForName = true;
                                break;
                            case "type":
                                xsr.next();
                                this.type = xsr.getText();
                                break;
                            case "name":
                                if (lookForName && this.name.equals("")) {
                                    xsr.next();
                                    this.name = xsr.getText();
                                }
                                break;
                            case "ele":
                                xsr.next();
                                curr.setEle(Double.parseDouble(xsr.getText()));
                                break;
                            case "time":
                                if (curr != null) {
                                    xsr.next();
                                    String time = xsr.getText();
                                    Calendar cal = DatatypeConverter.parseDateTime(time);
                                    Date date = cal.getTime();
                                    curr.setTime(date);
                                }
                                break;
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        switch (xsr.getLocalName()) {
                            case "metadata":
                            case "rte":
                            case "trk":
                                lookForName = false;
                                break;
                        }
                        break;
                }
            }
            xsr.close();
        }  catch (Exception e) {
            System.err.println("There was a problem parsing the GPX file.");
            e.printStackTrace();
        }
        try {
            fis.close();
        } catch (IOException e) {
            System.err.println("There was a problem closing the GPX file.");
            e.printStackTrace();
        }
        if (this.name.equals("")) {
            this.name = "NewRoute" + unnamedCounter++;
        }
        this.updateAllProperties();
    }
    
    public void addRoutePoint(RoutePoint wpt) {
        routePoints.add(wpt);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }
    
    public int getNumPts() {
        return routePoints.size();
    }

    public RoutePoint getStart() {
        return routePoints.get(0);
    }

    public RoutePoint getEnd() {
        return routePoints.get(routePoints.size() - 1);
    }

    /**
     * Saves the {@link #Route} to a GPX file.
     * 
     * @param gpx
     *            The GPX file.
     */
    public void saveToGPXFile(File gpx) {
        String name = gpx.getName();
        int lc = name.length() - 1;
        if (name.charAt(lc--) != 'x' || name.charAt(lc--) != 'p'|| name.charAt(lc--) != 'g'|| name.charAt(lc) != '.') {
            String dir = gpx.getParent();        
            String newName = dir + "/" + name + ".gpx";
            gpx = new File(newName);
        }
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(gpx);
        } catch (FileNotFoundException e) {
            System.err.println("Error creating GPX file.");
            e.printStackTrace();
        }
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter xsw;
        try {
            xsw = factory.createXMLStreamWriter(fos);
            
            xsw.writeStartDocument("1.0");
            xsw.writeCharacters("\n\n");
            
            xsw.writeStartElement("gpx");
            xsw.writeCharacters("\n\n");

            for (RoutePoint rtept : routePoints) {
                xsw.writeStartElement("rtept");
                xsw.writeAttribute("lat", ((Double) rtept.getLat()).toString());
                xsw.writeAttribute("lon", ((Double) rtept.getLon()).toString());
                xsw.writeCharacters("\n");
                
                xsw.writeEndElement();
                xsw.writeCharacters("\n");
            }
            xsw.writeCharacters("\n");
            xsw.writeEndElement();
            xsw.writeEndDocument();

            xsw.flush();
            xsw.close();
        } catch (XMLStreamException e) {
            System.err.println("Error while writing GPX file.");
            e.printStackTrace();
        }

        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.err.println("Error saving GPX file.");
            e.printStackTrace();
        }
    }
    
    public void updateAllProperties() {
        updateDuration();
        updateLength();
        updateMaxSpeed();
        updateEleProps();
    }
    
    public void updateDuration() {
        duration = getEnd().getTime().getTime()- getStart().getTime().getTime();
    }
    
    public void updateLength() {
        lengthMeters = 0;
        RoutePoint curr = getStart();
        RoutePoint prev;
        for (RoutePoint rtept : routePoints) {
            prev = curr;
            curr = rtept;
            double increment = OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
            if (!Double.isNaN(increment)) {
                lengthMeters += OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
            }
        }
        lengthMiles = lengthMeters * 0.000621371;
    }
    
    public void updateMaxSpeed() {
        maxSpeedKmph = 0;
        double lengthKm;
        long millis;
        double hours;
        int smoothingFactor = 1; // find max speed over this many segments to smooth unreliable data and outliers
        if (getNumPts() <= smoothingFactor) {
            return;
        }
        RoutePoint segStart = getStart();
        RoutePoint segEnd = routePoints.get(smoothingFactor);
        for (int i = smoothingFactor; i < getNumPts(); i++)  {
            segEnd = routePoints.get(i);
            segStart = routePoints.get(i - smoothingFactor);
            lengthKm = OsmMercator.getDistance(segStart.getLat(), segStart.getLon(), segEnd.getLat(), segEnd.getLon()) / 1000;
            millis = segEnd.getTime().getTime() - segStart.getTime().getTime();
            hours = (double) millis / 3600000D;
            double candidateMax = lengthKm / hours;
            if (!Double.isNaN(candidateMax)) {
                maxSpeedKmph = Math.max(maxSpeedKmph, lengthKm / hours);
            }
        }
        maxSpeedMph = maxSpeedKmph * 0.621371;
    }
    
    public void updateEleProps() {
        eleStartMeters = getStart().getEle();
        eleStartFeet = eleStartMeters * 3.28084;
        eleEndMeters = getEnd().getEle();
        eleEndFeet = eleEndMeters * 3.28084;
        eleMinMeters = Integer.MAX_VALUE;
        eleMaxMeters = Integer.MIN_VALUE;
        grossRiseMeters = 0;
        grossFallMeters = 0;
        riseTime = 0;
        fallTime = 0;
        RoutePoint curr = getStart();
        RoutePoint prev;
        for (RoutePoint rtept : routePoints) {
            prev = curr;
            curr = rtept;
            if (curr.getEle() > prev.getEle()) {
                grossRiseMeters += (curr.getEle() - prev.getEle());
                riseTime += curr.getTime().getTime() - prev.getTime().getTime();
            } else {
                grossFallMeters += (prev.getEle() - curr.getEle());
                fallTime += curr.getTime().getTime() - prev.getTime().getTime();
            }
            eleMinMeters = Math.min(eleMinMeters, curr.getEle());
            eleMaxMeters = Math.max(eleMaxMeters, curr.getEle());
        }
        eleMinFeet = eleMinMeters * 3.28084;
        eleMaxFeet = eleMaxMeters * 3.28084;
        grossRiseFeet = grossRiseMeters * 3.28084;
        grossFallFeet = grossFallMeters * 3.28084;
    }
    
    public String getType() {
        return type;
    }
    
    public long getDuration() {
        return duration;
    }

    public double getMaxSpeedKmph() {
        return maxSpeedKmph;
    }

    public double getMaxSpeedMph() {
        return maxSpeedMph;
    }

    public double getLengthMeters() {
        return lengthMeters;
    }
    
    public double getLengthMiles() {
        return lengthMiles;
    }

    public double getEleStartMeters() {
        return eleStartMeters;
    }

    public double getEleStartFeet() {
        return eleStartFeet;
    }

    public double getEleEndMeters() {
        return eleEndMeters;
    }

    public double getEleEndFeet() {
        return eleEndFeet;
    }

    public double getEleMinMeters() {
        return eleMinMeters;
    }

    public double getEleMinFeet() {
        return eleMinFeet;
    }

    public double getEleMaxMeters() {
        return eleMaxMeters;
    }

    public double getEleMaxFeet() {
        return eleMaxFeet;
    }

    public double getGrossRiseFeet() {
        return grossRiseFeet;
    }

    public double getGrossRiseMeters() {
        return grossRiseMeters;
    }

    public double getGrossFallFeet() {
        return grossFallFeet;
    }

    public double getGrossFallMeters() {
        return grossFallMeters;
    }

    public long getRiseTime() {
        return riseTime;
    }

    public long getFallTime() {
        return fallTime;
    }
}
