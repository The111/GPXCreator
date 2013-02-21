package com.gpxcreator.gpxpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.gpxcreator.GPXCreator;

@SuppressWarnings("serial")
public class GPXPanel extends JMapViewer {
    
    private List<GPXFile> gpxFiles;
    private Image imgPathStart;
    private Image imgPathPt;
    private Image imgPathEnd;
    private Image imgCrosshair;
    private double crosshairLat;
    private double crosshairLon;
    private boolean showCrosshair;

    public GPXPanel() {
        super(new MemoryTileCache(), 16);
        this.setTileSource(new OsmTileSource.Mapnik());
        DefaultMapController mapController = new DefaultMapController(this);
        mapController.setDoubleClickZoomEnabled(false);
        mapController.setMovementEnabled(true);
        mapController.setWheelZoomEnabled(true);
        mapController.setMovementMouseButton(MouseEvent.BUTTON1);
        this.setScrollWrapEnabled(false); // TODO make everything work with wrapping?
        this.setZoomButtonStyle(ZOOM_BUTTON_STYLE.VERTICAL);
        gpxFiles = new ArrayList<GPXFile>();
        InputStream in1 = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/path-start.png");
        if (in1 != null) {
            try {
                imgPathStart = ImageIO.read(in1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InputStream in2 = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/waypoint.png");
        if (in2 != null) {
            try {
                imgPathPt = ImageIO.read(in2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InputStream in3 = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/path-end.png");
        if (in3 != null) {
            try {
                imgPathEnd = ImageIO.read(in3);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InputStream in4 = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/crosshair-map.png");
        if (in4 != null) {
            try {
                imgCrosshair = ImageIO.read(in4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void addGPXFile(GPXFile gpxFile) {
        gpxFiles.add(gpxFile);
        repaint();
    }
    
    public void removeGPXFile(GPXFile gpxFile) {
        gpxFiles.remove(gpxFile);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintFiles(g, gpxFiles);
        if (showCrosshair) {
            Point p = this.getMapPosition(new Coordinate(crosshairLat, crosshairLon), false);
            int offset = imgCrosshair.getWidth(null) / 2;
            g.drawImage(imgCrosshair, p.x - offset, p.y - offset, null);
        }
    }
    
    private void paintFiles(Graphics g, List<GPXFile> files) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (GPXFile file: files) {
            if (file.isVisible()) {
                for (Route route : file.getRoutes()) {
                    if (route.isVisible()) {
                        paintPath(g, route.getPath());
                    }
                }
                for (Track track : file.getTracks()) {
                    if (track.isVisible()) {
                        for (WaypointGroup path : track.getTracksegs()) {
                            if (path.isVisible()) {
                                paintPath(g, path);
                            }
                        }
                    }
                }
                if (file.isWptsVisible()) {
                    paintWaypointGroup(g, file.getWaypointGroup());
                    for (Route route : file.getRoutes()) {
                        if (route.isWptsVisible() && route.isVisible()) {
                            paintWaypointGroup(g, route.getPath());
                        }
                    }
                    for (Track track : file.getTracks()) {
                        if (track.isWptsVisible() && track.isVisible()) {
                            for (WaypointGroup wptGrp : track.getTracksegs()) {
                                paintWaypointGroup(g, wptGrp);
                            }
                        }
                    }
                }
                for (Route route : file.getRoutes()) {
                    if (route.isVisible()) {
                        paintStartAndEnd(g, route.getPath());
                    }
                }
                for (Track track : file.getTracks()) {
                    if (track.isVisible()) {
                        for (WaypointGroup path : track.getTracksegs()) {
                            if (path.isVisible()) {
                                paintStartAndEnd(g, path);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void paintPath(Graphics g, WaypointGroup waypointPath) {
        g.setColor(waypointPath.getColor());
        if (waypointPath.getNumPts() >= 2) {
            List<Waypoint> waypoints = waypointPath.getWaypoints();
            GeneralPath path;
            Waypoint rtept;
            Point point;
            
            Graphics2D g2d = (Graphics2D) g;
            Stroke saveStroke = g2d.getStroke();
            Color saveColor = g2d.getColor();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // draw black border
            g2d.setStroke(new BasicStroke(5.5f));
            g2d.setColor(Color.BLACK);
            path = new GeneralPath();
            rtept = waypointPath.getStart();
            point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
            path.moveTo(point.x, point.y);
            for (int i = 1; i < waypoints.size(); i++) {
                rtept = waypoints.get(i);
                point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
                path.lineTo(point.x, point.y);
            }
            g2d.draw(path);
    
            // draw colored route
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(saveColor);
            path = new GeneralPath();
            rtept = waypointPath.getStart();
            point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
            path.moveTo(point.x, point.y);
            for (int i = 1; i < waypoints.size(); i++) {
                rtept = waypoints.get(i);
                point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
                path.lineTo(point.x, point.y);
            }
            g2d.draw(path);
            g2d.setStroke(saveStroke);
        }
    }
    
    private void paintWaypointGroup(Graphics g, WaypointGroup wptGrp) {
        if (wptGrp.isVisible() && wptGrp.isWptsVisible()) {
            List<Waypoint> wpts = wptGrp.getWaypoints();
            for (Waypoint wpt : wpts) {
                Point point = getMapPosition(wpt.getLat(), wpt.getLon(), false);
                g.drawImage(imgPathPt, point.x - 9, point.y - 28, null);
            }
        }
    }
    
    private void paintStartAndEnd(Graphics g, WaypointGroup waypointPath) {
        if (waypointPath.getNumPts() >= 2) {
            Waypoint rteptEnd = waypointPath.getEnd(); 
            Point end = getMapPosition(rteptEnd.getLat(), rteptEnd.getLon(), false);
            g.setColor(Color.BLACK);
            //g.fillOval(end.x - 5, end.y - 5, 11, 11);
            g.drawImage(imgPathEnd, end.x - 9, end.y - 28, null);
        }
        if (waypointPath.getNumPts() >= 1) {
            Waypoint rteptStart = waypointPath.getStart(); 
            Point start = getMapPosition(rteptStart.getLat(), rteptStart.getLon(), false);
            g.setColor(Color.BLACK);
            //g.fillOval(start.x - 5, start.y - 5, 11, 11);
            g.drawImage(imgPathStart, start.x - 9, start.y - 28, null);
        }
    }

    public void fitGPXObjectToPanel(GPXObject gpxObject) {
        int maxZoom = tileController.getTileSource().getMaxZoom();
        int xMin = OsmMercator.LonToX(gpxObject.getMinLon(), maxZoom);
        int xMax = OsmMercator.LonToX(gpxObject.getMaxLon(), maxZoom);
        int yMin = OsmMercator.LatToY(gpxObject.getMaxLat(), maxZoom); // screen y-axis positive is down
        int yMax = OsmMercator.LatToY(gpxObject.getMinLat(), maxZoom); // screen y-axis positive is down
        
        if (xMin > xMax || yMin > yMax) {
            //setDisplayPositionByLatLon(36, -98, 4); // U! S! A!
        } else {
            int width = Math.max(0, getWidth());
            int height = Math.max(0, getHeight());
            int zoom = maxZoom;
            int x = xMax - xMin;
            int y = yMax - yMin;
            while (x > width || y > height) {
                zoom--;
                x >>= 1;
                y >>= 1;
            }
            x = xMin + (xMax - xMin) / 2;
            y = yMin + (yMax - yMin) / 2;
            int z = 1 << (maxZoom - zoom);
            x /= z;
            y /= z;
            setDisplayPosition(x, y, zoom);
        }
    }

    public List<GPXFile> getGPXFiles() {
        return gpxFiles;
    }

    public void setCrosshairLat(double crosshairLat) {
        this.crosshairLat = crosshairLat;
    }

    public void setCrosshairLon(double crosshairLon) {
        this.crosshairLon = crosshairLon;
    }

    public void setShowCrosshair(boolean showCrosshair) {
        this.showCrosshair = showCrosshair;
    }
}
