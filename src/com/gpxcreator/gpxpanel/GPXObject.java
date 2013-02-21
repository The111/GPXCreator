package com.gpxcreator.gpxpanel;

import java.awt.Color;

public class GPXObject {
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
    
    public GPXObject() {
        this.visible = true;
        this.wptsVisible = true;
        this.minLat =  86;
        this.maxLat = -86;
        this.minLon =  180;
        this.maxLon = -180;
    }
    
    public GPXObject(boolean randomColor) {
        this();
        if (randomColor) {
            this.color = colors[(currentColor++) % colors.length];
        } else {
            this.color = Color.WHITE;
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

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public void setMinLon(double minLon) {
        this.minLon = minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public void setMaxLon(double maxLon) {
        this.maxLon = maxLon;
    }
}
