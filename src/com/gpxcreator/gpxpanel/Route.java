package com.gpxcreator.gpxpanel;

import java.awt.Color;

public class Route extends GPXObject {

    protected int number;
    protected String type;
    
    private WaypointGroup path;

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
    
    public Route(Color color) {
        super(color);
        this.type = "";
    }
    
    public String toString() {
        String str = "Route";
        if (this.name != null && !this.name.equals("")) {
            str = str.concat(" - " + this.name);
        }
        return str;
    }
    
    public void setColor(Color color) {
        super.setColor(color);
        path.setColor(color);
    }

    public void updateAllProperties() {
        path.updateDuration();
        path.updateLength();
        path.updateMaxSpeed();
        path.updateEleProps();
        
        this.duration = path.getDuration();
        this.maxSpeedKmph = path.getMaxSpeedKmph();
        this.maxSpeedMph = path.getMaxSpeedKmph();
        this.lengthMeters = path.getLengthMeters();
        this.lengthMiles = path.getLengthMiles();
        this.eleStartMeters = path.getEleStartMeters();
        this.eleStartFeet = path.getEleStartFeet();
        this.eleEndMeters = path.getEleEndMeters();
        this.eleEndFeet = path.getEleEndFeet();
        this.eleMinMeters = path.getEleMinMeters();
        this.eleMinFeet = path.getEleMinFeet();
        this.eleMaxMeters = path.getEleMaxMeters();
        this.eleMaxFeet = path.getEleMaxFeet();
        this.grossRiseFeet = path.getGrossRiseFeet();
        this.grossRiseMeters = path.getGrossRiseMeters();
        this.grossFallFeet = path.getGrossFallFeet();
        this.grossFallMeters = path.getGrossFallMeters();
        this.riseTime = path.getRiseTime();
        this.fallTime = path.getFallTime();
        
        this.minLat = path.getMinLat();
        this.minLon = path.getMinLon();
        this.maxLat = path.getMaxLat();
        this.maxLon = path.getMaxLon();
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

    public WaypointGroup getPath() {
        return path;
    }

    public void setPath(WaypointGroup path) {
        this.path = path;
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
