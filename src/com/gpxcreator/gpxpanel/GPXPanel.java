package com.gpxcreator.gpxpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmMercator;

@SuppressWarnings("serial")
public class GPXPanel extends JMapViewer {
    
    private List<Route> routes;
    private Route activeRoute;

    public GPXPanel() {
        super(new MemoryTileCache(), 16);
        DefaultMapController mapController = new DefaultMapController(this);
        mapController.setDoubleClickZoomEnabled(false);
        mapController.setMovementEnabled(true);
        mapController.setWheelZoomEnabled(true);
        mapController.setMovementMouseButton(MouseEvent.BUTTON1);
        this.setScrollWrapEnabled(false); // TODO fix paint methods to be faster when scrollwrap is enabled
        this.setZoomButtonStyle(ZOOM_BUTTON_STYLE.VERTICAL);
        routes = new ArrayList<Route>();
    }
    
    public void addRoute(Route route) {
        routes.add(route);
        activeRoute = route;
    }
    
    public Route getActiveRoute() {
        return activeRoute;
    }
    
    public void setActiveRoute(Route route) {
        activeRoute = route;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintRoutes(g, routes);
    }
    
    private void paintRoutes(Graphics g, List<Route> routes) { // TODO stop painting off-screen routes!
        List<RoutePoint> routePoints;
        RoutePoint curr;
        RoutePoint prev;
        for (Route route : routes) {
            if ((route.getLength()) >= 2) {
                routePoints = route.getRoutePoints();
                paintRoutePoint(g, curr = route.getStart());
                for (int i = 1; i < routePoints.size(); i++) {
                    prev = curr;
                    curr = routePoints.get(i);
                    paintRoutePoint(g, curr);
                    paintRouteSegment(g, prev, curr);
                }
            }
        }
    }

    private void paintRoutePoint(Graphics g, RoutePoint curr) {
        Point p = getMapPosition(curr.getLat(), curr.getLon(), false);
        int r = 3;
        int d = 2 * r;
        g.setColor(Color.RED);
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

    private void paintRouteSegment(Graphics g, RoutePoint prev, RoutePoint curr) {
        Point p1 = getMapPosition(curr.getLat(), curr.getLon(), false);
        Point p2 = getMapPosition(prev.getLat(), prev.getLon(), false);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
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
    
    public void fitRouteToPanel(Route route) {
        int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMax = Integer.MIN_VALUE;
        int maxZoom = tileController.getTileSource().getMaxZoom();
        for (RoutePoint rtept : route.getRoutePoints()) {
            int x = OsmMercator.LonToX(rtept.getLon(), maxZoom);
            int y = OsmMercator.LatToY(rtept.getLat(), maxZoom);
            xMax = Math.max(xMax, x);
            yMax = Math.max(yMax, y);
            xMin = Math.min(xMin, x);
            yMin = Math.min(yMin, y);
        }
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
