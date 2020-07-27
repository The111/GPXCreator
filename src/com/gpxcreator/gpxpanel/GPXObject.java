package com.gpxcreator.gpxpanel;

import com.gpxcreator.gpxpanel.WaypointGroup.WptGrpType;

import java.awt.*;

/**
 * Contains fields and methods common to all GPX element types (files, routes, tracks, waypoints, etc).
 *
 * @author Matt Hoover
 */
public abstract class GPXObject {
  /**
   * Updates the relevant properties of the subclass.
   */
  public abstract void updateAllProperties();

  protected String name;
  protected String desc;
  protected boolean visible;
  protected boolean wptsVisible;
  protected Color color;
  private static Color[] colors = { // some standard random colors
      new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255),
      new Color(255, 255, 0), new Color(255, 0, 255), new Color(0, 255, 255),
      new Color(127, 0, 255), new Color(255, 127, 0), new Color(255, 255, 255)
  };
  private static int currentColor = 0;

  protected double minLat;
  protected double minLon;
  protected double maxLat;
  protected double maxLon;

  protected long duration;
  protected double lengthMeters;
  protected double lengthMiles;
  protected double lengthAscendMeters;
  protected double lengthAscendMiles;
  protected double lengthDescendMeters;
  protected double lengthDescendMiles;
  protected double avgGradeAscend;
  protected double avgGradeDescend;
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

  /**
   * Default superclass constructor.
   */
  public GPXObject() {
    this.name = "";
    this.desc = "";
    this.visible = true;
    this.wptsVisible = true;
    this.color = Color.white;

    this.minLat = 86;
    this.maxLat = -86;
    this.minLon = 180;
    this.maxLon = -180;
  }

  /**
   * Constructs a GPX object with a random color.
   *
   * @param randomColor If true, use a random color.  If false, use white.
   */
  public GPXObject(boolean randomColor) {
    this();
    if (randomColor) {
      this.color = colors[(currentColor++) % colors.length];
    }
  }

  /**
   * Constructs a GPX object with a chosen color.
   *
   * @param color The color.
   */
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

  public double getLengthAscendMeters() {
    return lengthAscendMeters;
  }

  public double getLengthAscendMiles() {
    return lengthAscendMiles;
  }

  public double getLengthDescendMeters() {
    return lengthDescendMeters;
  }

  public double getLengthDescendMiles() {
    return lengthDescendMiles;
  }

  public double getAvgGradeAscend() {
    return avgGradeAscend;
  }

  public double getAvgGradeDescend() {
    return avgGradeDescend;
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

  public boolean isGPXFile() {
    return getClass().equals(GPXFile.class);
  }

  public boolean isGPXFileWithOneRoute() {
    return (isGPXFile() && ((GPXFile) this).getRoutes().size() == 1);
  }

  public boolean isGPXFileWithOneRouteOnly() {
    return (isGPXFile() && ((GPXFile) this).getRoutes().size() == 1 && ((GPXFile) this).getTracks().size() == 0);
  }

  public boolean isGPXFileWithNoRoutes() {
    return (isGPXFile() && ((GPXFile) this).getRoutes().size() == 0);
  }

  public boolean isGPXFileWithOneTrackseg() {
    return (isGPXFile() && ((GPXFile) this).getTracks().size() == 1
        && ((GPXFile) this).getTracks().get(0).getTracksegs().size() == 1);
  }

  public boolean isGPXFileWithOneTrackOnly() {
    return (isGPXFile() && ((GPXFile) this).getTracks().size() == 1
        && ((GPXFile) this).getRoutes().size() == 0);
  }

  public boolean isGPXFileWithOneTracksegOnly() {
    return (isGPXFile() && ((GPXFile) this).getTracks().size() == 1
        && ((GPXFile) this).getTracks().get(0).getTracksegs().size() == 1
        && ((GPXFile) this).getRoutes().size() == 0);
  }

  public boolean isWaypoints() {
    return (isWaypointGroup() && ((WaypointGroup) this).getWptGrpType() == WptGrpType.WAYPOINTS);
  }

  public boolean isRoute() {
    return getClass().equals(Route.class);
  }

  public boolean isTrack() {
    return getClass().equals(Track.class);
  }

  public boolean isTrackWithOneSeg() {
    return (isTrack() && ((Track) this).getTracksegs().size() == 1);
  }

  public boolean isTrackseg() {
    return (isWaypointGroup() && ((WaypointGroup) this).getWptGrpType() == WptGrpType.TRACKSEG);
  }

  public boolean isWaypointGroup() {
    return getClass().equals(WaypointGroup.class);
  }

}
