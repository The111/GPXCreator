package com.gpxcreator.gpxpanel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.gpxcreator.gpxpanel.WaypointGroup.WptGrpType;

public class Track extends GPXObject {

    protected int number;
    protected String type;
    
    private List<WaypointGroup> tracksegs;
    
    public Track(Color color) {
        super(color);
        this.type = "";
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
    
    public WaypointGroup addTrackseg() {
        WaypointGroup trackseg = new WaypointGroup(this.color, WptGrpType.TRACKSEG);
        tracksegs.add(trackseg);
        return trackseg;
    }

    @Override
    public void updateAllProperties() {
        maxSpeedKmph = 0;
        maxSpeedMph = 0;
        eleMinMeters = Integer.MAX_VALUE;
        eleMinFeet = Integer.MAX_VALUE;
        eleMaxMeters = Integer.MIN_VALUE;
        eleMaxFeet = Integer.MIN_VALUE;
        minLat =  86;
        maxLat = -86;
        minLon =  180;
        maxLon = -180;
        
        for (WaypointGroup trackseg : tracksegs) {
            trackseg.updateAllProperties();
            
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
}
