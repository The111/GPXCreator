package com.gpxcreator.gpxpanel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Track extends GPXObject {

    protected int number;
    protected String type;
    
    private List<WaypointGroup> tracksegs;

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
    
    public Track(Color color) {
        super(color);
        tracksegs = new ArrayList<WaypointGroup>();
    }
    
    public String toString() {
        String str = "Track";
        if (this.name != null && !this.name.equals("")) {
            str = str.concat(" - " + this.name);
        }
        return str;
    }
    
    public void setColor(Color color) {
        super.setColor(color);
        for (WaypointGroup trackseg : tracksegs) {
            trackseg.setColor(color);
        }
    }
    
    public void updateAllProperties() {
        maxSpeedKmph = -Double.MAX_VALUE;
        maxSpeedMph = -Double.MAX_VALUE;
        eleMinMeters = Double.MAX_VALUE;
        eleMinFeet = Double.MAX_VALUE;
        eleMaxMeters = -Double.MAX_VALUE;
        eleMaxFeet = -Double.MAX_VALUE;
        minLat =  86;
        maxLat = -86;
        minLon =  180;
        maxLon = -180;
        
        for (WaypointGroup trackseg : tracksegs) {
            trackseg.updateDuration();
            trackseg.updateLength();
            trackseg.updateMaxSpeed();
            trackseg.updateEleProps();
            
            duration += trackseg.getDuration();
            maxSpeedKmph = Math.max(maxSpeedKmph, trackseg.getMaxSpeedKmph());
            maxSpeedMph = Math.max(maxSpeedMph, trackseg.getMaxSpeedKmph());
            lengthMeters += trackseg.getLengthMeters();
            lengthMiles += trackseg.getLengthMiles();
            eleMinMeters = Math.min(eleMinMeters, trackseg.getEleMinMeters());
            eleMinFeet = Math.min(eleMinFeet, trackseg.getEleMinFeet());
            eleMaxMeters = Math.max(eleMaxMeters, trackseg.getEleMaxMeters());
            eleMaxFeet = Math.max(eleMaxFeet, trackseg.getEleMaxFeet());
            grossRiseFeet += trackseg.getGrossRiseFeet();
            grossRiseMeters += trackseg.getGrossRiseMeters();
            grossFallFeet += trackseg.getGrossFallFeet();
            grossFallMeters += trackseg.getGrossFallMeters();
            riseTime += trackseg.getRiseTime();
            fallTime += trackseg.getFallTime();
            
            minLat = Math.min(minLat, trackseg.getMinLat());
            minLon = Math.min(minLon, trackseg.getMinLon());
            maxLat = Math.max(maxLat, trackseg.getMaxLat());
            maxLon = Math.max(maxLon, trackseg.getMaxLon());
        }
        
        eleStartMeters = tracksegs.get(0).getEleStartMeters();
        eleStartFeet = tracksegs.get(0).getEleStartFeet();
        eleEndMeters = tracksegs.get(tracksegs.size() - 1).getEleEndMeters();
        eleEndFeet = tracksegs.get(tracksegs.size() - 1).getEleEndFeet();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<WaypointGroup> getTracksegs() {
        return tracksegs;
    }

    public void setTracksegs(List<WaypointGroup> tracksegs) {
        this.tracksegs = tracksegs;
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
