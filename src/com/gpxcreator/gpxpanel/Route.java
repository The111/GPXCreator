package com.gpxcreator.gpxpanel;

import com.gpxcreator.gpxpanel.WaypointGroup.WptGrpType;

import java.awt.*;

/**
 * The GPX "rte" element.
 *
 * @author Matt Hoover
 */
public class Route extends GPXObject {

  protected int number;
  protected String type;

  private WaypointGroup path;

  /**
   * Constructs a {@link Route} with the chosen color.
   *
   * @param color The color.
   */
  public Route(Color color) {
    super(color);
    this.type = "";
    this.path = new WaypointGroup(this.color, WptGrpType.ROUTE);
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

  /* (non-Javadoc)
   * @see com.gpxcreator.gpxpanel.GPXObject#updateAllProperties()
   */
  @Override
  public void updateAllProperties() {
    path.updateAllProperties();

    duration = path.getDuration();
    maxSpeedKmph = path.getMaxSpeedKmph();
    maxSpeedMph = path.getMaxSpeedKmph();
    lengthMeters = path.getLengthMeters();
    lengthMiles = path.getLengthMiles();
    eleStartMeters = path.getEleStartMeters();
    eleStartFeet = path.getEleStartFeet();
    eleEndMeters = path.getEleEndMeters();
    eleEndFeet = path.getEleEndFeet();
    eleMinMeters = path.getEleMinMeters();
    eleMinFeet = path.getEleMinFeet();
    eleMaxMeters = path.getEleMaxMeters();
    eleMaxFeet = path.getEleMaxFeet();
    grossRiseFeet = path.getGrossRiseFeet();
    grossRiseMeters = path.getGrossRiseMeters();
    grossFallFeet = path.getGrossFallFeet();
    grossFallMeters = path.getGrossFallMeters();
    riseTime = path.getRiseTime();
    fallTime = path.getFallTime();

    minLat = path.getMinLat();
    minLon = path.getMinLon();
    maxLat = path.getMaxLat();
    maxLon = path.getMaxLon();
  }
}
