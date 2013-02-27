package com.gpxcreator.gpxpanel;

import java.awt.Color;

public abstract class GPXObject {
    public abstract void updateAllProperties();
    
    protected String name;
    protected String desc;
    protected boolean visible;
    protected boolean wptsVisible;
    protected Color color;
    private static Color[] colors = { // some standard random colors
        new Color(255,  0,  0), new Color(  0,255,  0), new Color(  0,  0,255),
        new Color(255,255,  0), new Color(255,  0,255), new Color(  0,255,255),
        new Color(127,  0,255), new Color(255,127,  0), new Color(255,255,255)
    };
    private static int currentColor = 0;
    
    protected double minLat;
    protected double minLon;
    protected double maxLat;
    protected double maxLon;
    
    protected long duration;
    protected double lengthMeters;
    protected double lengthMiles;
    protected double maxSpeedKmph;
    protected double maxSpeedMph;
    protected double eleStartMeters;
    protected double eleStartFeet;
    protected double eleEndMeters;
    protected double eleEndFeet;
    protected double eleMinMeters;
    protected double eleMinFeet;
    protected double eleMaxMeters;
    protected double eleMaxFeet;
    protected double grossRiseFeet;
    protected double grossRiseMeters;
    protected double grossFallFeet;
    protected double grossFallMeters;
    protected long riseTime;
    protected long fallTime;
    
    public GPXObject() {
        this.name = "";
        this.desc = "";        
        this.visible = true;
        this.wptsVisible = true;
        this.color = Color.white;
        
        this.eleMinMeters = Integer.MAX_VALUE;
        this.eleMinFeet = Integer.MAX_VALUE;
        this.eleMaxMeters = Integer.MIN_VALUE;
        this.eleMaxFeet = Integer.MIN_VALUE;
        
        this.minLat =  86;
        this.maxLat = -86;
        this.minLon =  180;
        this.maxLon = -180;
    }
    
    public GPXObject(boolean randomColor) {
        this();
        if (randomColor) {
            this.color = colors[(currentColor++) % colors.length];
        }
    }
    
    public GPXObject(Color color) {
        this();
        this.color = color;
    }
    
    public String toString() {
        return this.name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public boolean isWptsVisible() {
        return wptsVisible;
    }

    public void setWptsVisible(boolean wptsVisible) {
        this.wptsVisible = wptsVisible;
    }

    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public long getDuration() {
        return duration;
    }

    public double getLengthMeters() {
        return lengthMeters;
    }

    public double getLengthMiles() {
        return lengthMiles;
    }

    public double getMaxSpeedKmph() {
        return maxSpeedKmph;
    }

    public double getMaxSpeedMph() {
        return maxSpeedMph;
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
