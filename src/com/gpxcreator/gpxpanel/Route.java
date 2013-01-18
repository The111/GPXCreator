package com.gpxcreator.gpxpanel;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class Route {

    private String name;
    private static int unnamedCounter = 0;
    private List<RoutePoint> routePoints;
    private Color color;
    private static Color[] colors = { // some standard random colors
        new Color(255,  0,  0), new Color(  0,255,  0), new Color(  0,  0,255),
        new Color(255,255,  0), new Color(255,  0,255), new Color(  0,255,255),
        new Color(127,  0,255), new Color(255,127,  0), new Color(255,255,255)
    };
    private static int currentColor = 0;
    private boolean visible;
    
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
                                this.addRoutePoint(new RoutePoint(lat, lon));
                                break;
                            case "metadata":
                            case "rte":
                            case "trk":
                                lookForName = true;
                                break;
                            case "name":
                                if (lookForName && this.name.equals("")) {
                                    xsr.next();
                                    this.name = xsr.getText();
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
        }  catch (XMLStreamException e) {
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
    
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }
    
    public int getLength() {
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
}
