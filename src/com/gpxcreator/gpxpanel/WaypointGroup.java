package com.gpxcreator.gpxpanel;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.openstreetmap.gui.jmapviewer.OsmMercator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An ad-hoc element representing a group of waypoint elements.<br />
 * In a group of top-level "wpt" elements, these points will be represented discretely.<br />
 * In a "rte" or "trk" element, these points will be represented as paths.
 *
 * @author Matt Hoover
 */
public class WaypointGroup extends GPXObject {

  /**
   * The different types of {@link WaypointGroup}.
   */
  public enum WptGrpType {
    WAYPOINTS,
    ROUTE,
    TRACKSEG
  }

  /**
   * Status messages returned to the calling class after an elevation correction request.<br />
   * A "correction" is an attempt to replace all elevation data with SRTM data from the server.
   */
  public enum EleCorrectedStatus {
    CORRECTED,
    FAILED,
    CORRECTED_WITH_CLEANSE
  }

  /**
   * Status messages returned to the calling class after an elevation cleanse request.<br />
   * A "cleanse" is an attempt to fill in any data that was missing from the server's response (SRTM voids).
   */
  public enum EleCleansedStatus {
    CLEANSED,
    CANNOT_CLEANSE,
    CLEANSE_UNNEEDED
  }

  private WptGrpType wptGrpType;
  private List<Waypoint> waypoints;

  /**
   * Default constructor.
   *
   * @param color The color.
   * @param type  The type of {@link WaypointGroup}.
   */
  public WaypointGroup(Color color, WptGrpType type) {
    super(color);
    switch (type) {
      case WAYPOINTS:
        this.name = "Waypoints";
        break;
      case ROUTE:
        this.name = "Route";
        break;
      case TRACKSEG:
        this.name = "Track segment";
        break;
    }
    this.wptGrpType = type;
    this.waypoints = new ArrayList<Waypoint>();
  }

  public WptGrpType getWptGrpType() {
    return wptGrpType;
  }

  public List<Waypoint> getWaypoints() {
    return waypoints;
  }

  public void setWaypoints(List<Waypoint> waypoints) {
    this.waypoints = waypoints;
  }

  /**
   * Adds a waypoint to the group.
   */
  public void addWaypoint(Waypoint wpt) {
    waypoints.add(wpt);
  }

  /**
   * Adds a waypoint to the group and optionally corrects the elevation.<br />
   * Warning: using this method for repetitive adding of points will generate a large number of HTTP requests.
   */
  public void addWaypoint(Waypoint wpt, boolean correctElevation) {
    if (correctElevation) {
      String url = "http://open.mapquestapi.com/elevation/v1/profile?key=Fmjtd%7Cluub2lu12u%2Ca2%3Do5-96y5qz&";
      String charset = "UTF-8";
      String param1 = String.format("%.6f", wpt.getLat()) + "," + String.format("%.6f", wpt.getLon());
      String param2 = "m";
      String query = null;
      URLConnection connection = null;
      InputStream response = null;
      BufferedReader br = null;
      StringBuilder builder = new StringBuilder();
      try {
        query = String.format("latLngCollection=%s&unit=%s",
            URLEncoder.encode(param1, charset),
            URLEncoder.encode(param2, charset));
        connection = new URL(url + query).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        response = connection.getInputStream();
        br = new BufferedReader((Reader) new InputStreamReader(response, "UTF-8"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          builder.append(line);
          builder.append('\n');
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      String responseStr = builder.toString();
      int heightIndex = responseStr.indexOf("height");
      String height = "";
      for (int i = heightIndex + 8;
           Character.isDigit(responseStr.charAt(i)) || responseStr.charAt(i) == '.'
               || responseStr.charAt(i) == '-';
           i++) {
        height = height + responseStr.charAt(i);
      }
      wpt.setEle(Double.parseDouble(height));
    }
    addWaypoint(wpt);
  }

  /**
   * Removes a waypoint from the group.
   */
  public void removeWaypoint(Waypoint wpt) {
    waypoints.remove(wpt);
    updateBounds();
  }

  public int getNumPts() {
    return waypoints.size();
  }

  public boolean contains(Waypoint waypoint) {
    return waypoints.contains(waypoint);
  }

  public Waypoint getStart() {
    if (waypoints.size() > 0) {
      return waypoints.get(0);
    } else {
      return null;
    }
  }

  public Waypoint getEnd() {
    if (waypoints.size() > 0) {
      return waypoints.get(waypoints.size() - 1);
    } else {
      return null;
    }
  }

  /**
   * Corrects the elevation of each {@link Waypoint} in the group and updates the aggregate group properties.<br />
   * Optionally can do a "cleanse," attempting to fill missing data (SRTM voids) in the response.<br />
   * Note: The MapQuest Open Elevation API has a bug with POST XML, and the useFilter parameter.
   * Because of this, the request must be a POST KVP (key/value pair).  The useFilter parameter returns
   * data of much higher quality.
   *
   * @return The status of the response.
   */
  public EleCorrectedStatus correctElevation(boolean doCleanse) {
    if (waypoints.size() < 1) {
      return EleCorrectedStatus.FAILED;
    }
    String latLngCollection = "";
    Waypoint rtept = getStart();
    latLngCollection += rtept.getLat() + "," + rtept.getLon();
    for (int i = 1; i < waypoints.size(); i++) {
      rtept = waypoints.get(i);
      latLngCollection += "," + String.format("%.6f", rtept.getLat()) +
          "," + String.format("%.6f", rtept.getLon());
    }
    String url = "http://open.mapquestapi.com/elevation/v1/profile?key=Fmjtd%7Cluub2lu12u%2Ca2%3Do5-96y5qz";
    String charset = "UTF-8";
    String param1 = "kvp"; // inFormat
    String param2 = latLngCollection;
    String param3 = "xml"; // outFormat
    String param4 = "true"; // useFilter
    String query = null;
    URLConnection connection = null;
    OutputStream output = null;
    InputStream response = null;
    BufferedReader br = null;
    StringBuilder builder = new StringBuilder();
    try {
      query = String.format("inFormat=%s" + "&latLngCollection=%s" + "&outFormat=%s" + "&useFilter=%s",
          URLEncoder.encode(param1, charset),
          URLEncoder.encode(param2, charset),
          URLEncoder.encode(param3, charset),
          URLEncoder.encode(param4, charset));
      connection = new URL(url).openConnection();
      connection.setDoOutput(true);
      connection.setRequestProperty("Accept-Charset", charset);
      connection.setRequestProperty(
          "Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
      output = connection.getOutputStream();
      output.write(query.getBytes(charset));
      output.close();
      response = connection.getInputStream();
      br = new BufferedReader((Reader) new InputStreamReader(response, "UTF-8"));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        builder.append(line);
        builder.append('\n');
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    String responseStr = builder.toString();

    if (responseStr.contains("Given Route exceeds the maximum allowed distance")) {
      return EleCorrectedStatus.FAILED;
    } else {
      List<Double> eleList = getEleArrayFromXMLResponse(responseStr);
      if (eleList.size() == waypoints.size()) {
        for (int i = 0; i < waypoints.size(); i++) {
          waypoints.get(i).setEle(eleList.get(i));
        }
      } else {
        return EleCorrectedStatus.FAILED;
      }
      EleCleansedStatus cleanseStatus = EleCleansedStatus.CLEANSE_UNNEEDED;
      if (doCleanse) {
        cleanseStatus = cleanseEleData();
      }
      updateEleProps();
      if (cleanseStatus == EleCleansedStatus.CLEANSED) {
        return EleCorrectedStatus.CORRECTED_WITH_CLEANSE;
      } else if (cleanseStatus == EleCleansedStatus.CANNOT_CLEANSE) {
        return EleCorrectedStatus.FAILED;
      } else {
        return EleCorrectedStatus.CORRECTED;
      }
    }
  }

  /**
   * Cleanese the elevation data.  Any {@link Waypoint} with an elevation of -32768 needs to be interpolated.
   *
   * @return The status of the cleanse.
   */
  public EleCleansedStatus cleanseEleData() {
    boolean cleansed = false;
    double eleStart = getStart().getEle();
    double eleEnd = getEnd().getEle();

    if (eleStart == -32768) {
      cleansed = true;
      for (int i = 0; i < waypoints.size(); i++) {
        if (waypoints.get(i).getEle() != -32768) {
          eleStart = waypoints.get(i).getEle();
          break;
        }
      }
    }

    if (eleEnd == -32768) {
      cleansed = true;
      for (int i = waypoints.size() - 1; i >= 0; i--) {
        if (waypoints.get(i).getEle() != -32768) {
          eleEnd = waypoints.get(i).getEle();
          break;
        }
      }
    }

    if (eleStart == -32768 && eleEnd == -32768) {
      return EleCleansedStatus.CANNOT_CLEANSE; // hopeless! (impossible to correct)
    }

    waypoints.get(0).setEle(eleStart);
    waypoints.get(getNumPts() - 1).setEle(eleEnd);

    for (int i = 0; i < waypoints.size(); i++) {
      if (waypoints.get(i).getEle() == -32768) {
        cleansed = true;
        Waypoint neighborBefore = null;
        Waypoint neighborAfter = null;
        double distBefore = 0;
        double distAfter = 0;

        Waypoint curr = waypoints.get(i);
        Waypoint prev = waypoints.get(i);
        for (int j = i - 1; j >= 0; j--) {
          prev = curr;
          curr = waypoints.get(j);
          distBefore +=
              new OsmMercator().getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
          if (waypoints.get(j).getEle() != -32768) {
            neighborBefore = waypoints.get(j);
            break;
          }
        }

        curr = waypoints.get(i);
        prev = waypoints.get(i);
        for (int j = i + 1; j < waypoints.size(); j++) {
          prev = curr;
          curr = waypoints.get(j);
          distAfter +=
              new OsmMercator().getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
          if (waypoints.get(j).getEle() != -32768) {
            neighborAfter = waypoints.get(j);
            break;
          }
        }

        double distDiff = distBefore + distAfter;
        double eleDiff = neighborAfter.getEle() - neighborBefore.getEle();
        double eleCleansed = ((distBefore / distDiff) * eleDiff) + neighborBefore.getEle();

        waypoints.get(i).setEle(eleCleansed);
      }
    }
    if (cleansed) {
      return EleCleansedStatus.CLEANSED;
    } else {
      return EleCleansedStatus.CLEANSE_UNNEEDED;
    }
  }

  /**
   * Parses an XML response string.
   *
   * @return A list of numerical elevation values.
   */
  public static List<Double> getEleArrayFromXMLResponse(String xmlResponse) {
    List<Double> ret = new ArrayList<Double>();
    InputStream is = new ByteArrayInputStream(xmlResponse.getBytes());
    XMLInputFactory xif = XMLInputFactory.newInstance();
    try {
      XMLStreamReader xsr = xif.createXMLStreamReader(is, "ISO-8859-1");
      while (xsr.hasNext()) {
        xsr.next();
        if (xsr.getEventType() == XMLStreamReader.START_ELEMENT) {
          if (xsr.getLocalName().equals("height")) {
            xsr.next();
            if (xsr.isCharacters()) {
              ret.add(Double.parseDouble(xsr.getText()));
            }
          }
        }
      }
      xsr.close();
    } catch (Exception e) {
      System.err.println("There was a problem parsing the XML response.");
      e.printStackTrace();
    }
    return ret;
  }

  /* (non-Javadoc)
   * @see com.gpxcreator.gpxpanel.GPXObject#updateAllProperties()
   */
  @Override
  public void updateAllProperties() {
    if (waypoints.size() > 0) {
      smoothElevation();
      updateDuration();
      updateLength();
      updateMaxSpeed();
      updateEleProps();
      updateBounds();
    }
  }

  private void smoothElevation() {
    double distance = 0;
    List<Double> distances = new ArrayList<>();
    List<Double> elevations = new ArrayList<>();

    Waypoint curr = getStart();
    Waypoint prev;
    for (Waypoint rtept : waypoints) {
      prev = curr;
      curr = rtept;
      double increment =
          new OsmMercator().getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
      if (!Double.isNaN(increment)) {
        distance += increment;
        distances.add(distance);
        elevations.add(curr.getEle());
      }
    }

    for (double b = 0.01; b <= 0.5; b+=0.3) {
      try {
        double[] elevationsSmoothed =
            new LoessInterpolator(b /* 0.3 default */, 20 /* 2 default */).smooth(
                distances.stream().mapToDouble(Double::doubleValue).toArray(),
                elevations.stream().mapToDouble(Double::doubleValue).toArray());
        for (int i = 1; i < waypoints.size(); i++) {
          waypoints.get(i).setEle(elevationsSmoothed[i]);
        }
        System.out.println(b);
        break;
      } catch (Exception e) {
      }
    }
  }

  public void updateDuration() {
    Date startTime = getStart().getTime();
    Date endTime = getEnd().getTime();
    if (startTime != null && endTime != null) {
      duration = getEnd().getTime().getTime() - getStart().getTime().getTime();
    }
  }

  public void updateLength() {
    lengthMeters = 0;
    lengthAscendMeters = 0;
    lengthDescendMeters = 0;
    double sumAscendMeters = 0;
    double sumDescendMeters = 0;
    Waypoint curr = getStart();
    Waypoint prev;
    for (Waypoint rtept : waypoints) {
      prev = curr;
      curr = rtept;
      double increment =
          new OsmMercator().getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon());
      if (!Double.isNaN(increment)) {
        lengthMeters += increment;
        double grade = Math.abs((curr.getEle() - prev.getEle()) / increment);
        if (curr.getEle() > prev.getEle()) {
          lengthAscendMeters += increment;
          sumAscendMeters += curr.getEle() - prev.getEle();
        } else {
          lengthDescendMeters += increment;
          sumDescendMeters += prev.getEle() - curr.getEle();
        }
      }
    }
    lengthMiles = lengthMeters * 0.000621371;
    lengthAscendMiles = lengthAscendMeters * 0.000621371;
    lengthDescendMiles = lengthDescendMeters * 0.000621371;
    avgGradeAscend = sumAscendMeters / lengthAscendMeters;
    avgGradeDescend = sumDescendMeters / lengthDescendMeters;
  }

  public void updateMaxSpeed() {
    maxSpeedKmph = 0;
    double lengthKm;
    long millis;
    double hours;
    if (waypoints.size() < 2) return;
    Waypoint prev = getStart();
    Waypoint curr = waypoints.get(1);
    List<Double> distances = new ArrayList<>();
    List<Double> speeds = new ArrayList<>();
    lengthKm = 0;
    for (int i = 1; i < getNumPts(); i++) {
      curr = waypoints.get(i);
      prev = waypoints.get(i - 1);

      double increment =
          new OsmMercator().getDistance(curr.getLat(), curr.getLon(), prev.getLat(), prev.getLon()) / 1000;
      lengthKm += increment;
      distances.add(lengthKm);

      Date startTime = getEnd().getTime();
      Date endTime = getEnd().getTime();
      if (startTime != null && endTime != null) {
        millis = curr.getTime().getTime() - prev.getTime().getTime();
        hours = (double) millis / 3600000D;
        double candidateMax = lengthKm / hours;
        if (!Double.isNaN(candidateMax)) {
          speeds.add(increment / hours);
        }
      }
    }

    for (double b = 0.01; b <= 0.5; b+=0.3) {
      try {
        double[] speedsSmoothed =
            new LoessInterpolator(b /* 0.3 default */, 20 /* 2 default */).smooth(
                distances.stream().mapToDouble(Double::doubleValue).toArray(),
                speeds.stream().mapToDouble(Double::doubleValue).toArray());
        for (int i = 0; i < speedsSmoothed.length; i++) {
          maxSpeedKmph = Math.max(maxSpeedKmph, speedsSmoothed[i]);
        }
        System.out.println(b);
        break;
      } catch (Exception e) {
      }
    }

    maxSpeedMph = maxSpeedKmph * 0.621371;
  }

  public void updateEleProps() {
    eleStartMeters = getStart().getEle();
    eleStartFeet = eleStartMeters * 3.28084;
    eleEndMeters = getEnd().getEle();
    eleEndFeet = eleEndMeters * 3.28084;
    eleMinMeters = Integer.MAX_VALUE;
    eleMaxMeters = Integer.MIN_VALUE;
    grossRiseMeters = 0;
    grossFallMeters = 0;
    riseTime = 0;
    fallTime = 0;
    Waypoint curr = getStart();
    Waypoint prev;
    Date startTime = getEnd().getTime();
    Date endTime = getEnd().getTime();
    for (Waypoint rtept : waypoints) {
      prev = curr;
      curr = rtept;
      if (curr.getEle() > prev.getEle()) {
        grossRiseMeters += (curr.getEle() - prev.getEle());
        if (startTime != null && endTime != null) {
          riseTime += curr.getTime().getTime() - prev.getTime().getTime();
        }
      } else if (curr.getEle() < prev.getEle()) {
        grossFallMeters += (prev.getEle() - curr.getEle());
        if (startTime != null && endTime != null) {
          fallTime += curr.getTime().getTime() - prev.getTime().getTime();
        }
      }
      eleMinMeters = Math.min(eleMinMeters, curr.getEle());
      eleMaxMeters = Math.max(eleMaxMeters, curr.getEle());
    }
    eleMinFeet = eleMinMeters * 3.28084;
    eleMaxFeet = eleMaxMeters * 3.28084;
    grossRiseFeet = grossRiseMeters * 3.28084;
    grossFallFeet = grossFallMeters * 3.28084;
  }

  public void updateBounds() {
    minLat = 86;
    maxLat = -86;
    minLon = 180;
    maxLon = -180;
    for (Waypoint wpt : waypoints) {
      minLat = Math.min(minLat, wpt.getLat());
      minLon = Math.min(minLon, wpt.getLon());
      maxLat = Math.max(maxLat, wpt.getLat());
      maxLon = Math.max(maxLon, wpt.getLon());
    }
  }
}
