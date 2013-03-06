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

    public enum WptGrpType {
        WAYPOINTS,
        ROUTE,
        TRACKSEG
    }
    
    public enum EleCorrectedStatus {
        CORRECTED,
        FAILED,
        CORRECTED_WITH_CLEANSE
    }
    
    public enum EleCleansedStatus {
        CLEANSED,
        CANNOT_CLEANSE,
        CLEANSE_UNNEEDED
    }
    
    private WptGrpType wptGrpType;
    private List<Waypoint> waypoints;
    
    public WaypointGroup(Color color, WptGrpType type) {
        super(color);
        switch (type) {
            case WAYPOINTS:
                this.name = "Waypoints";
                break;
            case ROUTE:
                this.name = "Route";
                break;
            case TRACKSEG:
                this.name = "Track segment";
                break;
        }
        this.wptGrpType = type;
        this.waypoints = new ArrayList<Waypoint>();
    }
    
    public WptGrpType getWptGrpType() {
        return wptGrpType;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public void addWaypoint(Waypoint wpt) {
        waypoints.add(wpt);
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

    public void removeWaypoint(Waypoint wpt) {
        waypoints.remove(wpt);
        updateBounds();
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

    // returns false if the query fails for any reason
    public EleCorrectedStatus correctElevation(boolean doCleanse) { // POST KVP (remember: had problems with POST XML and useFilter parameter)
        if (waypoints.size() < 1) {
            return EleCorrectedStatus.FAILED;
        }
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
        
        if (responseStr.contains("Given Route exceeds the maximum allowed distance")) {
            return EleCorrectedStatus.FAILED;
        } else {
            List<Double> eleList = getEleArrayFromXMLResponse(responseStr);
            if (eleList.size() == waypoints.size()) {
                for (int i = 0; i < waypoints.size(); i++) {
                    waypoints.get(i).setEle(eleList.get(i));
                }
            } else {
                return EleCorrectedStatus.FAILED;
            }
            EleCleansedStatus cleanseStatus = EleCleansedStatus.CLEANSE_UNNEEDED;
            if (doCleanse) {
                cleanseStatus = cleanseEleData();
            } 
            updateEleProps();
            if (cleanseStatus == EleCleansedStatus.CLEANSED) {
                return EleCorrectedStatus.CORRECTED_WITH_CLEANSE;
            } else if (cleanseStatus == EleCleansedStatus.CANNOT_CLEANSE) {
                return EleCorrectedStatus.FAILED;
            } else {
                return EleCorrectedStatus.CORRECTED;
            }
        }
    }

    public EleCleansedStatus cleanseEleData() {
        boolean cleansed = false;
        double eleStart = getStart().getEle();
        double eleEnd = getEnd().getEle();

        if (eleStart == -32768) {
            cleansed = true;
            for (int i = 0; i < waypoints.size(); i++) {
                if (waypoints.get(i).getEle() != -32768) {
                    eleStart = waypoints.get(i).getEle();
                    break;
                }
            }
        }
        
        if (eleEnd == -32768) {
            cleansed = true;
            for (int i = waypoints.size() - 1; i >= 0; i--) {
                if (waypoints.get(i).getEle() != -32768) {
                    eleEnd = waypoints.get(i).getEle();
                    break;
                }
            }
        }
        
        if (eleStart == -32768 && eleEnd == -32768) {
            return EleCleansedStatus.CANNOT_CLEANSE; // hopeless! (impossible to correct)
        }
        
        waypoints.get(0).setEle(eleStart);
        waypoints.get(getNumPts() - 1).setEle(eleEnd);
        
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).getEle() == -32768) {
                cleansed = true;
                Waypoint neighborBefore = null;
                Waypoint neighborAfter = null;
                double distBefore = 0;
                double distAfter = 0;
                
                Waypoint curr = waypoints.get(i);
                Waypoint prev = waypoints.get(i);
                for (int j = i - 1; j >= 0; j--) {
                    prev = curr;
                    curr = waypoints.get(j);
                    distBefore += OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
                    if (waypoints.get(j).getEle() != -32768) {
                        neighborBefore = waypoints.get(j);
                        break;
                    }
                }
    
                curr = waypoints.get(i);
                prev = waypoints.get(i);
                for (int j = i + 1; j < waypoints.size(); j++) {
                    prev = curr;
                    curr = waypoints.get(j);
                    distAfter += OsmMercator.getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
                    if (waypoints.get(j).getEle() != -32768) {
                        neighborAfter = waypoints.get(j);
                        break;
                    }
                }
                
                double distDiff = distBefore + distAfter;
                double eleDiff = neighborAfter.getEle() - neighborBefore.getEle();
                double eleCleansed = ((distBefore / distDiff) * eleDiff) + neighborBefore.getEle();
                
                waypoints.get(i).setEle(eleCleansed);
            }
        }
        if (cleansed) {
            return EleCleansedStatus.CLEANSED;
        } else {
            return EleCleansedStatus.CLEANSE_UNNEEDED;
        }
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

    @Override
    public void updateAllProperties() {
        if (waypoints.size() > 0) {
            updateDuration();
            updateLength();
            updateMaxSpeed();
            updateEleProps();
            updateBounds();
        }
    }
    
    public void updateDuration() {
        Date startTime = getStart().getTime();
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
                                 // TODO replace this cheap smoothing method below with a Kalman filter?
        int smoothingFactor = 5; // find max avg speed over this many segments to smooth unreliable data and outliers
        if (getNumPts() <= smoothingFactor) {
            return;
        }
        Waypoint segStart = getStart();
        Waypoint segEnd = waypoints.get(smoothingFactor);
        for (int i = smoothingFactor; i < getNumPts(); i++)  {
            segEnd = waypoints.get(i);
            segStart = waypoints.get(i - smoothingFactor);
            
            lengthKm = 0;
            for (int j = 0; j < smoothingFactor; j++) {
                Waypoint w1 = waypoints.get(i - j);
                Waypoint w2 = waypoints.get(i - j - 1);
                lengthKm += OsmMercator.getDistance(w1.getLat(), w1.getLon(), w2.getLat(), w2.getLon()) / 1000;
            }
            
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
    
    public void updateBounds() {
        minLat =  86;
        maxLat = -86;
        minLon =  180;
        maxLon = -180;
        for (Waypoint wpt : waypoints) {
            minLat = Math.min(minLat, wpt.getLat());
            minLon = Math.min(minLon, wpt.getLon());
            maxLat = Math.max(maxLat, wpt.getLat());
            maxLon = Math.max(maxLon, wpt.getLon());
        }
    }
}
