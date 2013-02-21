package com.gpxcreator.gpxpanel;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

public class WaypointGroup extends GPXObject {

    private List<Waypoint> waypoints;
    private boolean isPath;
    private boolean isTrackseg;

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
    
    public WaypointGroup(Color color, boolean isPath) {
        super(color);
        this.setPath(isPath);
        if (!isPath) {
            this.name = "Waypoints";
        } else {
            this.name = "Track segment";
        }
        waypoints = new ArrayList<Waypoint>();
    }
    
    public void addWaypoint(Waypoint wpt) {
        waypoints.add(wpt);
        if (getNumPts() > 1) {
            updateAllProperties(); // TODO make this less expensive?
        }
        checkMinMaxLatLon(wpt.getLat(), wpt.getLon());
    }
    
    public boolean isPath() {
        return isPath;
    }

    public void setPath(boolean isPath) {
        this.isPath = isPath;
    }

    public boolean isTrackseg() {
        return isTrackseg;
    }

    public void setTrackseg(boolean isTrackseg) {
        this.isTrackseg = isTrackseg;
    }

    public void addWaypoint(Waypoint wpt, boolean correctElevation) {
        if (correctElevation) {
            String url = "http://open.mapquestapi.com/elevation/v1/profile";
            String charset = "UTF-8";
            String param1 = String.format("%.6f", wpt.getLat()) + "," + String.format("%.6f", wpt.getLon());
            String param2 = "m";
            String query = null;
            URLConnection connection = null;
            InputStream response = null;
            BufferedReader br = null;
            StringBuilder builder = new StringBuilder();
            try {
                query = String.format("latLngCollection=%s&unit=%s",
                        URLEncoder.encode(param1, charset),
                        URLEncoder.encode(param2, charset));
                connection = new URL(url + "?" + query).openConnection();
                connection.setRequestProperty("Accept-Charset", charset);
                response = connection.getInputStream();
                br = new BufferedReader((Reader) new InputStreamReader(response, "UTF-8"));
                for(String line=br.readLine(); line!=null; line=br.readLine()) {
                    builder.append(line);
                    builder.append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String responseStr = builder.toString();
            int heightIndex = responseStr.indexOf("height");
            String height = "";
            for (int i = heightIndex + 8;
                    Character.isDigit(responseStr.charAt(i)) || responseStr.charAt(i) == '.'
                    || responseStr.charAt(i) == '-';
                    i++) {
                height = height + responseStr.charAt(i);
            }
            wpt.setEle(Double.parseDouble(height));
        }
        addWaypoint(wpt);
    }
    
    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public static List<Double> getEleArrayFromXMLResponse(String xmlResponse) {
        List<Double> ret = new ArrayList<Double>();
        InputStream is = new ByteArrayInputStream(xmlResponse.getBytes());
        XMLInputFactory xif = XMLInputFactory.newInstance();
        try {
            XMLStreamReader xsr = xif.createXMLStreamReader(is);
            while (xsr.hasNext()) {
                xsr.next();
                if (xsr.getEventType() == XMLStreamReader.START_ELEMENT) {
                    if (xsr.getLocalName().equals("height")) {
                        xsr.next();
                        if (xsr.isCharacters()) {
                            ret.add(Double.parseDouble(xsr.getText()));
                        }
                    }
                }
            }
            xsr.close();
        }  catch (Exception e) {
            System.err.println("There was a problem parsing the XML response.");
            e.printStackTrace();
        }
        return ret;
    }
    
    public void correctElevation() { // POST KVP (remember: had problems with POST XML and useFilter parameter)
        String latLngCollection = "";
        Waypoint rtept = getStart();
        latLngCollection += rtept.getLat() + "," + rtept.getLon();
        for (int i = 1; i < waypoints.size(); i++) {
            rtept = waypoints.get(i);
            latLngCollection += "," + rtept.getLat() + "," + rtept.getLon();
        }
        String url = "http://open.mapquestapi.com/elevation/v1/profile";
        String charset = "UTF-8";
        String param1 = "kvp"; // inFormat
        String param2 = latLngCollection;
        String param3 = "xml"; // outFormat
        String param4 = "true"; // useFilter
        String query = null;
        URLConnection connection = null;
        OutputStream output = null;
        InputStream response = null;
        BufferedReader br = null;
        StringBuilder builder = new StringBuilder();
        try {
            query = String.format("&inFormat=%s" + "&latLngCollection=%s" + "&outFormat=%s" + "&useFilter=%s",
                    URLEncoder.encode(param1, charset),
                    URLEncoder.encode(param2, charset),
                    URLEncoder.encode(param3, charset),
                    URLEncoder.encode(param4, charset));
                    connection = new URL(url).openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Accept-Charset", charset);
                    connection.setRequestProperty(
                            "Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
                    output = connection.getOutputStream();
                    output.write(query.getBytes(charset));
                    output.close();
                    response = connection.getInputStream();
                    br = new BufferedReader((Reader) new InputStreamReader(response, "UTF-8"));
                    for(String line=br.readLine(); line!=null; line=br.readLine()) {
                        builder.append(line);
                        builder.append('\n');
                    }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String responseStr = builder.toString();
        List<Double> eleList = getEleArrayFromXMLResponse(responseStr);
        if (eleList.size() == waypoints.size()) {
            for (int i = 0; i < waypoints.size(); i++) {
                waypoints.get(i).setEle(eleList.get(i));
            }
        } else {
            // TODO inform user that elevation request was no good
        }
        updateEleProps();
    }
    
    public int getNumPts() {
        return waypoints.size();
    }

    public Waypoint getStart() {
        if (waypoints.size() > 0) {
            return waypoints.get(0);
        } else {
            return null;
        }
    }

    public Waypoint getEnd() {
        if (waypoints.size() > 0) {
            return waypoints.get(waypoints.size() - 1);
        } else {
            return null;
        }
    }

    public void updateAllProperties() {
        updateDuration();
        updateLength();
        updateMaxSpeed();
        updateEleProps();
    }
    
    public void updateDuration() {
        Date startTime = getEnd().getTime();
        Date endTime = getEnd().getTime();
        if (startTime != null && endTime != null) {
            duration = getEnd().getTime().getTime()- getStart().getTime().getTime();
        }
    }
    
    public void updateLength() {
        lengthMeters = 0;
        Waypoint curr = getStart();
        Waypoint prev;
        for (Waypoint rtept : waypoints) {
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
        Waypoint segStart = getStart();
        Waypoint segEnd = waypoints.get(smoothingFactor);
        for (int i = smoothingFactor; i < getNumPts(); i++)  {
            segEnd = waypoints.get(i);
            segStart = waypoints.get(i - smoothingFactor);
            lengthKm = OsmMercator.getDistance(
                    segStart.getLat(), segStart.getLon(), segEnd.getLat(), segEnd.getLon()) / 1000;
            Date startTime = getEnd().getTime();
            Date endTime = getEnd().getTime();
            if (startTime != null && endTime != null) {
                millis = segEnd.getTime().getTime() - segStart.getTime().getTime();
                hours = (double) millis / 3600000D;
                double candidateMax = lengthKm / hours;
                if (!Double.isNaN(candidateMax)) {
                    maxSpeedKmph = Math.max(maxSpeedKmph, lengthKm / hours);
                }
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
        Waypoint curr = getStart();
        Waypoint prev;
        Date startTime = getEnd().getTime();
        Date endTime = getEnd().getTime();
        for (Waypoint rtept : waypoints) {
            prev = curr;
            curr = rtept;
            if (curr.getEle() > prev.getEle()) {
                grossRiseMeters += (curr.getEle() - prev.getEle());
                if (startTime != null && endTime != null) {
                    riseTime += curr.getTime().getTime() - prev.getTime().getTime();
                }
            } else if (curr.getEle() < prev.getEle()) {
                grossFallMeters += (prev.getEle() - curr.getEle());
                if (startTime != null && endTime != null) {
                    fallTime += curr.getTime().getTime() - prev.getTime().getTime();
                }
            }
            eleMinMeters = Math.min(eleMinMeters, curr.getEle());
            eleMaxMeters = Math.max(eleMaxMeters, curr.getEle());
        }
        eleMinFeet = eleMinMeters * 3.28084;
        eleMaxFeet = eleMaxMeters * 3.28084;
        grossRiseFeet = grossRiseMeters * 3.28084;
        grossFallFeet = grossFallMeters * 3.28084;
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

    public void checkMinMaxLatLon(double lat, double lon) {
        minLat = Math.min(minLat, lat);
        maxLat = Math.max(maxLat, lat);
        minLon = Math.min(minLon, lon);
        maxLon = Math.max(maxLon, lon);
    }
}
