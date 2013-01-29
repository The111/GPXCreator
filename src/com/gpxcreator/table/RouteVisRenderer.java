package com.gpxcreator.table;

import java.awt.Color;
import java.awt.Component;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.gpxcreator.GPXCreator;
import com.gpxcreator.gpxpanel.Route;

@SuppressWarnings("serial")
public class RouteVisRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    
    private static ImageIcon visible;
    private static ImageIcon invisible;
    
    public RouteVisRenderer() {
        try {
            visible = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
                    "/com/gpxcreator/icons/route-visible.png")));
            invisible = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
                    "/com/gpxcreator/icons/route-invisible.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col) {
        Route route = (Route) value;
        if (route.isVisible()) {
            setIcon(visible);
            setToolTipText("Hide route");
        } else {
            setIcon(invisible);
            setToolTipText("Show route");
        }
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setBorder(null);
        if (route.isTableHighlight()) {
            setBackground(new Color(164, 205, 255));
        } else {
            setBackground(Color.white);
        }
        return this;
    }
}
