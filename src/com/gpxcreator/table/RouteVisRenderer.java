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
    
    private ImageIcon visible;
    private ImageIcon invisible;
    
    public RouteVisRenderer() {
        try {
            visible = (new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/route-visible.png"))));
            invisible = (new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/route-invisible.png"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col) {
        Route route = (Route) value;
        boolean isVisible = route.isVisible();
        if (isVisible) {
            setIcon(visible);
        } else {
            setIcon(invisible);
        }
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setBorder(null);
        setBackground(Color.white);
        return this;
    }
}
