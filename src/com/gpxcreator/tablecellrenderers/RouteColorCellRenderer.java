package com.gpxcreator.tablecellrenderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class RouteColorCellRenderer extends DefaultTableCellRenderer implements Icon {

    private Color color;
    private static final int SIZE = 8;
    
    public RouteColorCellRenderer() {
        this.setIcon(this);
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
    }

    @Override
    protected void setValue(Object value) {
        this.color = (Color) value;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillRect(x, y, SIZE, SIZE);
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, SIZE, SIZE);
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }
}
