package com.gpxcreator.gpxpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmMercator;

@SuppressWarnings("serial")
public class GPXPanel extends JMapViewer {
    
    private List<Route> routes;

    public GPXPanel() {
        super(new MemoryTileCache(), 16);
        DefaultMapController mapController = new DefaultMapController(this);
        mapController.setDoubleClickZoomEnabled(false);
        mapController.setMovementEnabled(true);
        mapController.setWheelZoomEnabled(true);
        mapController.setMovementMouseButton(MouseEvent.BUTTON1);
        this.setScrollWrapEnabled(false); // TODO fix wrap bugs for routes (wrap not implemented at all for routePath)
        this.setZoomButtonStyle(ZOOM_BUTTON_STYLE.VERTICAL);
        routes = new ArrayList<Route>();
    }
    
    public void addRoute(Route route) {
        routes.add(route);
    }
    
    public void removeRoute(Route route) {
        routes.remove(route);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintRoutes(g, routes);
    }
    
    private void paintRoutes(Graphics g, List<Route> routes) {
        for (Route route : routes) {
            g.setColor(route.getColor());
            if ((route.getNumPts()) >= 2 && route.isVisible()) {
                paintRoute(g, route);
            }
        }
    }
    
    private void paintRoute(Graphics g, Route route) {
        List<RoutePoint> routePoints = route.getRoutePoints();
        GeneralPath routePath;
        RoutePoint rtept;
        Point point;
        
        Graphics2D g2d = (Graphics2D) g;
        Stroke saveStroke = g2d.getStroke();
        Color saveColor = g2d.getColor();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // draw black border
        g2d.setStroke(new BasicStroke(5.5f));
        g2d.setColor(Color.BLACK);
        routePath = new GeneralPath();
        rtept = route.getStart();
        point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
        routePath.moveTo(point.x, point.y);
        for (int i = 1; i < routePoints.size(); i++) {
            rtept = routePoints.get(i);
            point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
            routePath.lineTo(point.x, point.y);
        }
        g2d.draw(routePath);

        // draw colored route
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(saveColor);
        routePath = new GeneralPath();
        rtept = route.getStart();
        point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
        routePath.moveTo(point.x, point.y);
        for (int i = 1; i < routePoints.size(); i++) {
            rtept = routePoints.get(i);
            point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
            routePath.lineTo(point.x, point.y);
        }
        g2d.draw(routePath);
        
        g2d.setStroke(saveStroke);
    }

    /*private void paintRoutePoint(Graphics g, RoutePoint curr) {
        Point p = getMapPosition(curr.getLat(), curr.getLon(), false);
        int r = 3;
        int d = 2 * r;
        if (p.x >= 0 && p.y >= 0 && p.x <= getWidth() && p.y <= getHeight()) {
            g.fillOval(p.x - r, p.y - r, d, d);
            if (scrollWrapEnabled) {
                Point pSave = p;
                boolean keepWrapping = true;
                int tileSize = getTileController().getTileSource().getTileSize();
                int mapSize = tileSize << zoom;
                while (keepWrapping) {
                    p.x -= mapSize;
                    g.fillOval(p.x - r, p.y - r, d, d);
                    if (p.x < 0) {
                        keepWrapping = false;
                    }
                }
                p = pSave;
                keepWrapping = true;
                while (keepWrapping) {
                    p.x += mapSize;
                    g.fillOval(p.x - r, p.y - r, d, d);
                    if (p.x > getWidth()) {
                        keepWrapping = false;
                    }
                }
            }
        }
    }*/

    /*private void paintRouteSegment(Graphics g, RoutePoint prev, RoutePoint curr) {
        Point p1 = getMapPosition(curr.getLat(), curr.getLon(), false);
        Point p2 = getMapPosition(prev.getLat(), prev.getLon(), false);
        Graphics2D g2d = (Graphics2D) g;
        Stroke saveStroke = g2d.getStroke();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(3));
        if ( (p1.x >= 0 && p1.y >= 0 && p1.x <= getWidth() && p1.y <= getHeight()) ||
             (p2.x >= 0 && p2.y >= 0 && p2.x <= getWidth() && p2.y <= getHeight()) )  {
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
            if (scrollWrapEnabled) {
                Point p1Save = p1;
                Point p2Save = p2;
                boolean keepWrapping = true;
                int tileSize = getTileController().getTileSource().getTileSize();
                int mapSize = tileSize << zoom;
                while (keepWrapping) {
                    p1.x -= mapSize;
                    p2.x -= mapSize;
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                    if (p1.x < 0 || p2.x < 0) {
                        keepWrapping = false;
                    }
                }
                p1 = p1Save;
                p2 = p2Save;
                keepWrapping = true;
                while (keepWrapping) {
                    p1.x += mapSize;
                    p2.x += mapSize;
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                    if (p1.x > getWidth() || p2.x > getWidth()) {
                        keepWrapping = false;
                    }
                }
            }
        }
        g2d.setStroke(saveStroke);
    }*/
    
    public void fitRouteToPanel(Route route) {
        if (route.getNumPts() > 0 ) {

            int maxZoom = tileController.getTileSource().getMaxZoom();
            int xMin = OsmMercator.LonToX(route.getMinLon(), maxZoom);
            int xMax = OsmMercator.LonToX(route.getMaxLon(), maxZoom);
            int yMin = OsmMercator.LatToY(route.getMaxLat(), maxZoom); // screen y-axis positive is down
            int yMax = OsmMercator.LatToY(route.getMinLat(), maxZoom); // screen y-axis positive is down
            
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
        } else {
            setDisplayPositionByLatLon(36, -98, 4);
        }
    }    
}
