package com.gpxcreator.gpxpanel;

import java.util.Date;

public class Waypoint extends GPXObject {

    private double ele;
    private Date time;
    protected String type;
    private double lat;
    private double lon;
    
    public Waypoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.name = "waypoint";
    }

    public double getEle() {
        return ele;
    }

    public void setEle(double ele) {
        if (ele == -32768) {
            this.ele = 0; // TODO do something smarter here?
        } else {
            this.ele = ele;
        }
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
