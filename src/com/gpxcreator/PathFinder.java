package com.gpxcreator;

import com.gpxcreator.gpxpanel.Waypoint;

import java.util.List;

/**
 * Interface to be implemented by any pathfinders.
 *
 * @author Matt Hoover
 */
public interface PathFinder {

  /**
   * The different types of pathfinding.
   */
  public enum PathFindType {
    FOOT,
    BIKE
  }

  /**
   * Make the HTTP request for a pathfinding query.  Return the result as an XML string.
   */
  public abstract String getXMLResponse(PathFindType type, double lat1, double lon1, double lat2, double lon2);

  /**
   * Parse the XML string and return a List of {@link Waypoint}s.
   */
  public abstract List<Waypoint> parseXML(String xml);

}
