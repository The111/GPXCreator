package com.gpxcreator;

import java.util.List;

import com.gpxcreator.gpxpanel.Waypoint;

public interface PathFinder {
    
    public enum PathFindType {
        FOOT,
        BIKE
    }
    
    public abstract String getXMLResponse(PathFindType type, double lat1, double lon1, double lat2, double lon2);
    
    public abstract List<Waypoint> parseXML(String xml);
    
}
