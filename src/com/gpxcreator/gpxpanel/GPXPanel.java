package com.gpxcreator.gpxpanel;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
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

/**
 * 
 * An extension of {@link JMapViewer} to include the display of GPX elements and related functionality.
 * 
 * @author Matt Hoover
 *
 */
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
    private Point shownPoint;
    private Color activeColor;

    /**
     * Constructs a new {@link GPXPanel} instance.
     */
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
    
    /**
     * Adds the chosen {@link GPXFile} to the panel.
     */
    public void addGPXFile(GPXFile gpxFile) {
        gpxFiles.add(gpxFile);
        repaint();
    }
    
    /**
     * Removes the chosen {@link GPXFile} to the panel.
     */
    public void removeGPXFile(GPXFile gpxFile) {
        gpxFiles.remove(gpxFile);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        paintFiles(g2d, gpxFiles);
        if (showCrosshair) {
            Point p = null;
            if (crosshairLon > -180) { // hack fix for bug in JMapViewer.getMapPosition
                p = this.getMapPosition(new Coordinate(crosshairLat, crosshairLon), false);
            } else {
                p = this.getMapPosition(new Coordinate(crosshairLat, -180), false);
            }
            int offset = imgCrosshair.getWidth(null) / 2;
            g2d.drawImage(imgCrosshair, p.x - offset, p.y - offset, null);
        }
        if (shownPoint != null) {
            Stroke saveStroke = g2d.getStroke();
            Color saveColor = g2d.getColor();
            
            // square mark (with transparency)
            g2d.setColor(Color.black);
            g2d.drawRect(shownPoint.x - 9, shownPoint.y - 9, 17, 17);
            g2d.setColor(Color.white);
            g2d.drawRect(shownPoint.x - 8, shownPoint.y - 8, 15, 15);
            g2d.setColor(Color.black);
            g2d.drawRect(shownPoint.x - 7, shownPoint.y - 7, 13, 13);
            int red = activeColor.getRed();
            int green = activeColor.getGreen();
            int blue = activeColor.getBlue();
            AlphaComposite ac = AlphaComposite.SrcOver;
            g2d.setComposite(ac);
            g2d.setColor(new Color(255 - red, 255 - green, 255 - blue, 160));
            g2d.fill(new Rectangle(shownPoint.x - 6, shownPoint.y - 6, 11, 11));
            
            // X mark
            /*g2d.setStroke(new BasicStroke(5.5f));
            g2d.setColor(Color.black);
            g2d.drawLine(shownPoint.x - 8, shownPoint.y - 8, shownPoint.x + 8, shownPoint.y + 8);
            g2d.drawLine(shownPoint.x - 8, shownPoint.y + 8, shownPoint.x + 8, shownPoint.y - 8);
            g2d.setStroke(new BasicStroke(3));
            int red = activeColor.getRed();
            int green = activeColor.getGreen();
            int blue = activeColor.getBlue();
            g2d.setColor(new Color(255 - red, 255 - green, 255 - blue));
            g2d.drawLine(shownPoint.x - 8, shownPoint.y - 8, shownPoint.x + 8, shownPoint.y + 8);
            g2d.drawLine(shownPoint.x - 8, shownPoint.y + 8, shownPoint.x + 8, shownPoint.y - 8);*/
            
            g2d.setStroke(saveStroke);
            g2d.setColor(saveColor);
        }
    }
    
    /**
     * Paints each file.
     */
    private void paintFiles(Graphics2D g2d, List<GPXFile> files) {
        for (GPXFile file: files) {
            if (file.isVisible()) {
                for (Route route : file.getRoutes()) {
                    if (route.isVisible()) {
                        paintPath(g2d, route.getPath());
                    }
                }
                for (Track track : file.getTracks()) {
                    if (track.isVisible()) {
                        for (WaypointGroup path : track.getTracksegs()) {
                            if (path.isVisible()) {
                                paintPath(g2d, path);
                            }
                        }
                    }
                }
                if (file.isWptsVisible()) {
                    paintWaypointGroup(g2d, file.getWaypointGroup());
                    for (Route route : file.getRoutes()) {
                        if (route.isWptsVisible() && route.isVisible()) {
                            paintWaypointGroup(g2d, route.getPath());
                        }
                    }
                    for (Track track : file.getTracks()) {
                        if (track.isWptsVisible() && track.isVisible()) {
                            for (WaypointGroup wptGrp : track.getTracksegs()) {
                                paintWaypointGroup(g2d, wptGrp);
                            }
                        }
                    }
                }
                for (Route route : file.getRoutes()) {
                    if (route.isVisible()) {
                        paintStartAndEnd(g2d, route.getPath());
                    }
                }
                for (Track track : file.getTracks()) {
                    if (track.isVisible()) {
                        for (WaypointGroup path : track.getTracksegs()) {
                            if (path.isVisible()) {
                                paintStartAndEnd(g2d, path);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Paints a single path contained in a {@link WaypointGroup}.
     */
    private void paintPath(Graphics2D g2d, WaypointGroup waypointPath) {
        Point maxXY = getMapPosition(waypointPath.getMinLat(), waypointPath.getMaxLon(), false);
        Point minXY = getMapPosition(waypointPath.getMaxLat(), waypointPath.getMinLon(), false);
        if (maxXY.x < 0 || maxXY.y < 0 || minXY.x > getWidth() || minXY.y > getHeight()) {
            return; // don't paint paths that are completely off screen
        }

        g2d.setColor(waypointPath.getColor());
        if (waypointPath.getNumPts() >= 2) {
            List<Waypoint> waypoints = waypointPath.getWaypoints();
            GeneralPath path;
            Waypoint rtept;
            Point point;
            
            Stroke saveStroke = g2d.getStroke();
            Color saveColor = g2d.getColor();
            
            // draw black border
            g2d.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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
            
            // hack to fix zero degree angle join rounds (begin)
            Waypoint w1, w2, w3;
            Point p1, p2, p3;
            double d1, d2;
            w1 = waypoints.get(0);
            w2 = waypoints.get(1);
            p1 = getMapPosition(w1.getLat(), w1.getLon(), false);
            p2 = getMapPosition(w2.getLat(), w2.getLon(), false);
            for (int i = 2; i < waypoints.size(); i++) {
                w3 = waypoints.get(i);
                p3 = getMapPosition(w3.getLat(), w3.getLon(), false);
                d1 = Math.sqrt(Math.pow((p2.x - p3.x), 2) + Math.pow((p2.y - p3.y), 2));
                d2 = Math.sqrt(Math.pow((p1.x - p3.x), 2) + Math.pow((p1.y - p3.y), 2)); 
                if ((d1 / d2) > 99) {
                    path.moveTo(p2.x, p2.y);
                    path.lineTo(p2.x, p2.y);
                }
                w1 = w2;
                w2 = w3;
                p1 = p2;
                p2 = p3;
            }
            // hack (end)
            g2d.draw(path);
    
            // draw colored route
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(saveColor);
            g2d.draw(path);
            g2d.setStroke(saveStroke);
        }
    }
    
    /**
     * Paints the waypoints in {@link WaypointGroup}.
     */
    private void paintWaypointGroup(Graphics2D g2d, WaypointGroup wptGrp) {
        if (wptGrp.isVisible() && wptGrp.isWptsVisible()) {
            List<Waypoint> wpts = wptGrp.getWaypoints();
            for (Waypoint wpt : wpts) {
                Point point = getMapPosition(wpt.getLat(), wpt.getLon(), false);
                g2d.drawImage(imgPathPt, point.x - 9, point.y - 28, null);
            }
        }
    }
    
    /**
     * Paints the start/end markers of a {@link Route} or {@link Track}.
     */
    private void paintStartAndEnd(Graphics2D g2d, WaypointGroup waypointPath) {
        if (waypointPath.getNumPts() >= 2) {
            Waypoint rteptEnd = waypointPath.getEnd(); 
            Point end = getMapPosition(rteptEnd.getLat(), rteptEnd.getLon(), false);
            g2d.setColor(Color.BLACK);
            g2d.drawImage(imgPathEnd, end.x - 9, end.y - 28, null);
        }
        if (waypointPath.getNumPts() >= 1) {
            Waypoint rteptStart = waypointPath.getStart(); 
            Point start = getMapPosition(rteptStart.getLat(), rteptStart.getLon(), false);
            g2d.setColor(Color.BLACK);
            g2d.drawImage(imgPathStart, start.x - 9, start.y - 28, null);
        }
    }

    /**
     * Centers the {@link GPXObject} and sets zoom for best fit to panel.
     */
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

    public Point getShownPoint() {
        return shownPoint;
    }

    public void setShownPoint(Point shownPoint) {
        this.shownPoint = shownPoint;
    }

    public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
    }
}
