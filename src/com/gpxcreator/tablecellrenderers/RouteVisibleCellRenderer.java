package com.gpxcreator.tablecellrenderers;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.table.DefaultTableCellRenderer;

import com.gpxcreator.GPXCreator;

@SuppressWarnings("serial")
public class RouteVisibleCellRenderer extends DefaultTableCellRenderer implements Icon {

    private String iconResource;
    private BufferedImage image = null;
    
    public RouteVisibleCellRenderer() {
        this.setIcon(this);
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
    }
    
    @Override
    protected void setValue(Object value) {
        if ((boolean) value) {
            iconResource = "/com/gpxcreator/icons/route-visible.png";
        } else {
            // TODO add route-invisible icon!
        }
        
        try {
            image = ImageIO.read(GPXCreator.class.getResourceAsStream(iconResource));
        } catch (Exception e) {
            System.err.println("Failed to load route visibility icon!");
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(image, x, y, null);
    }

    @Override
    public int getIconWidth() {
        if (image != null) {
            return image.getWidth();
        } else {
            return 0;
        }
    }

    @Override
    public int getIconHeight() {
        if (image != null) {
            return image.getHeight();
        } else {
            return 0;
        }
    }
}
