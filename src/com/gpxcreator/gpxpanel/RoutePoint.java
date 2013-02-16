package com.gpxcreator.gpxpanel;

import java.util.Date;

public class RoutePoint {

    private double lat;
    private double lon;
    private double ele;
    private Date time;

    public RoutePoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
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
}
