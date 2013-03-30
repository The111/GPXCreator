package com.gpxcreator.gpxpanel;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.gpxcreator.GPXCreator;
import com.gpxcreator.gpxpanel.WaypointGroup.WptGrpType;

public class GPXFile extends GPXObject {
    
    private String link;
    private Date time;
    
    private WaypointGroup waypointGroup;
    private List<Route> routes;
    private List<Track> tracks;
    
    private boolean inMetadata;
    private boolean inRte;
    private boolean inTrk;
    private boolean inTrkseg;
    private boolean inWpt;
    
    /**
     * Creates an empty {@link #GPXFile}.
     */
    public GPXFile() {
        super(true);
        this.name = "UnnamedFile";
        this.desc = "";
        this.wptsVisible = false;
        
        this.link = "www.gpxcreator.com";
        this.time = new Date();
        this.waypointGroup = new WaypointGroup(color, WptGrpType.WAYPOINTS);
        this.routes = new ArrayList<Route>();
        this.tracks = new ArrayList<Track>();
    }
    
    /**
     * Creates an empty {@link #GPXFile}.
     * 
     * @param name
     *            The name of the route. 
     */
    public GPXFile(String name) {
        this();
        if (!name.equals("")) {
            this.name = name;
        }
    }
    
    /**
     * Creates a {@link #GPXFile} from a GPX file.
     * 
     * @param gpx
     *            The GPX file.
     */
    public GPXFile(File gpx) {
        this();

        Waypoint waypoint = null;
        Route route = null;
        Track track = null;
        WaypointGroup path = null;
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
                String ln;
                switch (xsr.getEventType()) {
                    case XMLStreamReader.START_ELEMENT:
                        ln = xsr.getLocalName();
                        if (ln.equals("gpx")) {
                        } else if (ln.equals("metadata")) {
                            inMetadata = true;
                        } else if (ln.equals("rte")) {
                            inRte = true;
                            route = addRoute();
                            path = route.getPath();
                        } else if (ln.equals("trk")) {
                            inTrk = true;
                            track = new Track(this.color);
                            this.tracks.add(track);
                        } else if (ln.equals("trkseg")) {
                            inTrkseg = true;
                            path = track.addTrackseg();
                        } else if (ln.equals("wpt") || ln.equals("rtept") || ln.equals("trkpt")) {
                            inWpt = true;
                            lat = Double.parseDouble(xsr.getAttributeValue("", "lat"));
                            lon = Double.parseDouble(xsr.getAttributeValue("", "lon"));
                            waypoint = new Waypoint(lat, lon);
                            if (inRte || inTrkseg) {
                                path.addWaypoint(waypoint);
                            } else if (inWpt) {
                                this.waypointGroup.addWaypoint(waypoint);
                            }
                        } else if (ln.equals("name")) {
                            xsr.next();
                            if (xsr.isCharacters()) {
                                String name = xsr.getText();
                                if (inMetadata && !name.equals("")) {
                                    this.name = name;
                                } else if (inRte) {
                                    route.setName(name);
                                } else if (inTrk) {
                                    track.setName(name);
                                } else if (inWpt) {
                                    waypoint.setName(name);
                                }
                            }
                        } else if (ln.equals("desc")) {
                            xsr.next();
                            if (xsr.isCharacters()) {
                                String desc = xsr.getText();
                                if (inMetadata) {
                                    this.desc = desc;
                                } else if (inRte) {
                                    route.setDesc(desc);
                                } else if (inTrk) {
                                    track.setDesc(desc);
                                } else if (inWpt) {
                                    waypoint.setDesc(desc);
                                }
                            }
                        } else if (ln.equals("number")) {
                            xsr.next();
                            if (xsr.isCharacters()) {
                                String number = xsr.getText();
                                int numberInt = Integer.parseInt(number);
                                if (inRte) {
                                    route.setNumber(numberInt);
                                } else if (inTrk) {
                                    track.setNumber(numberInt);
                                }
                            }
                        } else if (ln.equals("type")) {
                            xsr.next();
                            if (xsr.isCharacters()) {
                                String type = xsr.getText();
                                if (inRte) {
                                    route.setType(type);
                                } else if (inTrk) {
                                    track.setType(type);
                                } else if (inWpt) {
                                    waypoint.setType(type);
                                }
                            }
                        } else if (ln.equals("ele")) {
                            xsr.next();
                            if (xsr.isCharacters() && inWpt) {
                                waypoint.setEle(Double.parseDouble(xsr.getText()));
                            }
                        } else if (ln.equals("time")) {
                            xsr.next();
                            if (xsr.isCharacters()) {
                                String time = xsr.getText();
                                Calendar cal = DatatypeConverter.parseDateTime(time);
                                Date date = cal.getTime();
                                if (inWpt) {
                                    waypoint.setTime(date);
                                } else if (inMetadata) {
                                    this.time = date;
                                }
                            }
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        ln = xsr.getLocalName();
                        if (ln.equals("metadata")) {
                            inMetadata = false;
                        } else if (ln.equals("rte")) {
                            inRte = false;
                        } else if (ln.equals("trk")) {
                            inTrk = false;
                        } else if (ln.equals("trkseg")) {
                            inTrkseg = false;
                        } else if (ln.equals("wpt") || ln.equals("rtept") || ln.equals("trkpt")) {
                            inWpt = false;
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
        if (this.name.equals("UnnamedFile")) {
            this.name = gpx.getName();
        }
        if (this.time == null) {
            this.time = new Date();
        }
        
        updateAllProperties();
    }

    /**
     * Saves the {@link #Route} to a GPX file.
     * 
     * @param gpx
     *            The GPX file.
     */
    public void saveToGPXFile(File gpx) {
        // ensure file has correct extension
        String fileName = gpx.getName();
        int lc = fileName.length() - 1;
        if (fileName.charAt(lc--) != 'x' || fileName.charAt(lc--) != 'p'||
                fileName.charAt(lc--) != 'g'|| fileName.charAt(lc) != '.') {
            String dir = gpx.getParent();        
            String newName = dir + "/" + fileName + ".gpx";
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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            xsw = factory.createXMLStreamWriter(fos, "UTF-8");
            
            xsw.writeStartDocument("UTF-8", "1.0");
            xsw.writeCharacters("\n\n");
            
            xsw.writeStartElement("gpx");
            xsw.writeAttribute("version", "1.1");
            xsw.writeAttribute("creator", "www.gpxcreator.com");
            xsw.writeAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
            xsw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xsw.writeAttribute("xsi:schemaLocation",
                    "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
            xsw.writeCharacters("\n\n");
            
            // metadata BEGIN
            xsw.writeStartElement("metadata");
            xsw.writeCharacters("\n");
            if (this.name != null && !this.name.equals("")) {
                xsw.writeStartElement("name");
                xsw.writeCData(this.name);
                xsw.writeEndElement();
                xsw.writeCharacters("\n");
            }
            if (this.desc != null && !this.desc.equals("")) {
                xsw.writeStartElement("desc");
                xsw.writeCData(this.desc);
                xsw.writeEndElement();
                xsw.writeCharacters("\n");
            }
            xsw.writeStartElement("link");
            xsw.writeAttribute("href", "http://www.gpxcreator.com");
            xsw.writeStartElement("text");
            xsw.writeCharacters("GPX Creator");
            xsw.writeEndElement();
            xsw.writeEndElement();
            xsw.writeCharacters("\n");
            if (time != null) {
                xsw.writeStartElement("time");
                String fileTime = df.format(time);
                xsw.writeCharacters(fileTime);
                xsw.writeEndElement();
                xsw.writeCharacters("\n");
            }
            xsw.writeStartElement("bounds");
            xsw.writeAttribute("minlat", String.format("%.8f", minLat));
            xsw.writeAttribute("minlon", String.format("%.8f", minLon));
            xsw.writeAttribute("maxlat", String.format("%.8f", maxLat));
            xsw.writeAttribute("maxlon", String.format("%.8f", maxLon));
            xsw.writeEndElement();
            xsw.writeCharacters("\n");
            xsw.writeEndElement();
            xsw.writeCharacters("\n\n");
            // metadata END
            
            // waypoints BEGIN
            for (Waypoint wpt : waypointGroup.getWaypoints()) {
                xsw.writeStartElement("wpt");
                xsw.writeAttribute("lat", String.format("%.8f", (Double) wpt.getLat()));
                xsw.writeAttribute("lon", String.format("%.8f", (Double) wpt.getLon()));
                xsw.writeCharacters("\n");
                xsw.writeStartElement("ele");
                xsw.writeCharacters(String.format("%.6f", (Double) wpt.getEle()));
                xsw.writeEndElement();
                xsw.writeCharacters("\n");
                if (wpt.getTime() != null) {
                    xsw.writeStartElement("time");
                    String timeString = df.format(wpt.getTime());
                    xsw.writeCharacters(timeString);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (wpt.name != null && !wpt.name.equals("")) {
                    xsw.writeStartElement("name");
                    xsw.writeCData(wpt.name);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (wpt.desc != null && !wpt.desc.equals("")) {
                    xsw.writeStartElement("desc");
                    xsw.writeCData(wpt.desc);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (wpt.type != null && !wpt.type.equals("")) {
                    xsw.writeStartElement("type");
                    xsw.writeCData(wpt.type);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                xsw.writeEndElement();
                xsw.writeCharacters("\n");
            }
            if (waypointGroup.getWaypoints().size() > 0) {
                xsw.writeCharacters("\n");
            }
            // waypoints END
            
            // routes BEGIN
            for (Route route : routes) {
                xsw.writeStartElement("rte");
                xsw.writeCharacters("\n");
                if (route.name != null && !route.name.equals("")) {
                    xsw.writeStartElement("name");
                    xsw.writeCData(route.name);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (route.desc != null && !route.desc.equals("")) {
                    xsw.writeStartElement("desc");
                    xsw.writeCData(route.desc);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (route.number != 0) {
                    xsw.writeStartElement("number");
                    xsw.writeCharacters(Integer.toString(route.number));
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (route.type != null && !route.type.equals("")) {
                    xsw.writeStartElement("type");
                    xsw.writeCData(route.type);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                for (Waypoint rtept : route.getPath().getWaypoints()) {
                    xsw.writeStartElement("rtept");
                    xsw.writeAttribute("lat", String.format("%.8f", (Double) rtept.getLat()));
                    xsw.writeAttribute("lon", String.format("%.8f", (Double) rtept.getLon()));
                    xsw.writeCharacters("\n");
                    xsw.writeStartElement("ele");
                    xsw.writeCharacters(String.format("%.6f", (Double) rtept.getEle()));
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                    if (rtept.getTime() != null) {
                        xsw.writeStartElement("time");
                        String timeString = df.format(rtept.getTime());
                        xsw.writeCharacters(timeString);
                        xsw.writeEndElement();
                        xsw.writeCharacters("\n");
                    }
                    if (rtept.name != null && !rtept.name.equals("")) {
                        xsw.writeStartElement("name");
                        xsw.writeCData(rtept.name);
                        xsw.writeEndElement();
                        xsw.writeCharacters("\n");
                    }
                    if (rtept.desc != null && !rtept.desc.equals("")) {
                        xsw.writeStartElement("desc");
                        xsw.writeCData(rtept.desc);
                        xsw.writeEndElement();
                        xsw.writeCharacters("\n");
                    }
                    if (rtept.type != null && !rtept.type.equals("")) {
                        xsw.writeStartElement("type");
                        xsw.writeCData(rtept.type);
                        xsw.writeEndElement();
                        xsw.writeCharacters("\n");
                    }
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                xsw.writeEndElement();
                xsw.writeCharacters("\n\n");
            }
            // routes END
            
            // tracks BEGIN
            for (Track track : tracks) {
                xsw.writeStartElement("trk");
                xsw.writeCharacters("\n");
                if (track.name != null && !track.name.equals("")) {
                    xsw.writeStartElement("name");
                    xsw.writeCData(track.name);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (track.desc != null && !track.desc.equals("")) {
                    xsw.writeStartElement("desc");
                    xsw.writeCData(track.desc);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (track.number != 0) {
                    xsw.writeStartElement("number");
                    xsw.writeCharacters(Integer.toString(track.number));
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                if (track.type != null && !track.type.equals("")) {
                    xsw.writeStartElement("type");
                    xsw.writeCData(track.type);
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                for (WaypointGroup trackseg : track.getTracksegs()) {
                    xsw.writeStartElement("trkseg");
                    xsw.writeCharacters("\n");
                    for (Waypoint trkpt : trackseg.getWaypoints()) {
                        xsw.writeStartElement("trkpt");
                        xsw.writeAttribute("lat", String.format("%.8f", (Double) trkpt.getLat()));
                        xsw.writeAttribute("lon", String.format("%.8f", (Double) trkpt.getLon()));
                        xsw.writeCharacters("\n");
                        xsw.writeStartElement("ele");
                        xsw.writeCharacters(String.format("%.6f", (Double) trkpt.getEle()));
                        xsw.writeEndElement();
                        xsw.writeCharacters("\n");
                        if (trkpt.getTime() != null) {
                            xsw.writeStartElement("time");
                            String timeString = df.format(trkpt.getTime());
                            xsw.writeCharacters(timeString);
                            xsw.writeEndElement();
                            xsw.writeCharacters("\n");
                        }
                        if (trkpt.name != null && !trkpt.name.equals("")) {
                            xsw.writeStartElement("name");
                            xsw.writeCData(trkpt.name);
                            xsw.writeEndElement();
                            xsw.writeCharacters("\n");
                        }
                        if (trkpt.desc != null && !trkpt.desc.equals("")) {
                            xsw.writeStartElement("desc");
                            xsw.writeCData(trkpt.desc);
                            xsw.writeEndElement();
                            xsw.writeCharacters("\n");
                        }
                        if (trkpt.type != null && !trkpt.type.equals("")) {
                            xsw.writeStartElement("type");
                            xsw.writeCData(trkpt.type);
                            xsw.writeEndElement();
                            xsw.writeCharacters("\n");
                        }
                        xsw.writeEndElement();
                        xsw.writeCharacters("\n");
                    }
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }
                xsw.writeEndElement();
                xsw.writeCharacters("\n\n");
            }
            // tracks END
            
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
    
    public static boolean validateGPXFile(File gpx) {
        URL schemaFile = GPXCreator.class.getResource("/com/gpxcreator/schema/gpx-1.1.xsd");
        Source xmlFile = new StreamSource(gpx);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = schemaFactory.newSchema(schemaFile);
        } catch (SAXException e1) {
            System.out.println("There was a problem with the schema supplied for validation.");
            e1.printStackTrace();
        }
        Validator validator = schema.newValidator();
        try {
            validator.validate(xmlFile);
            return true;
        } catch (Exception e) {
            System.err.println(xmlFile.getSystemId() + " is not valid.");
            System.err.println("Validation error: " + e.getLocalizedMessage() + "\n");
            return false;
        }
    }
    
    public void setColor(Color color) {
        super.setColor(color);
        waypointGroup.setColor(color);
        for (Route route : routes) {
            route.setColor(color);
        }
        for (Track track : tracks) {
            track.setColor(color);
        }
    }

    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public Date getTime() {
        return time;
    }
    
    public void setTime(Date time) {
        this.time = time;
    }

    public WaypointGroup getWaypointGroup() {
        return waypointGroup;
    }

    public List<Route> getRoutes() {
        return routes;
    }
    
    public Route addRoute() {
        Route route = new Route(color);
        route.setName(this.name);
        routes.add(route);
        return route;
    }

    public List<Track> getTracks() {
        return tracks;
    }
    
    @Override
    public void updateAllProperties() {
        if (waypointGroup.getWaypoints().size() > 1) {
            waypointGroup.updateAllProperties();
        }
        for (Route route : routes) {
            route.updateAllProperties();
        }
        for (Track track : tracks) {
            track.updateAllProperties();
        }
        
        minLat =  86;
        maxLat = -86;
        minLon =  180;
        maxLon = -180;
        
        for (Route route : routes) {
            minLat = Math.min(minLat, route.getMinLat());
            minLon = Math.min(minLon, route.getMinLon());
            maxLat = Math.max(maxLat, route.getMaxLat());
            maxLon = Math.max(maxLon, route.getMaxLon());
        }
        for (Track track : tracks) {
            minLat = Math.min(minLat, track.getMinLat());
            minLon = Math.min(minLon, track.getMinLon());
            maxLat = Math.max(maxLat, track.getMaxLat());
            maxLon = Math.max(maxLon, track.getMaxLon());
        }
        for (Waypoint waypoint : waypointGroup.getWaypoints()) {
            minLat = Math.min(minLat, waypoint.getLat());
            minLon = Math.min(minLon, waypoint.getLon());
            maxLat = Math.max(maxLat, waypoint.getLat());
            maxLon = Math.max(maxLon, waypoint.getLon());
        }
    }
}
