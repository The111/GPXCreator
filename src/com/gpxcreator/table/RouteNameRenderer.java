package com.gpxcreator.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.gpxcreator.gpxpanel.Route;

@SuppressWarnings("serial")
public class RouteNameRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    
    private Font activeBold;
    private Font inactivePlain;
    
    public RouteNameRenderer() {
        activeBold = new Font("Tahoma", Font.BOLD, 11);
        inactivePlain = new Font("Tahoma", Font.PLAIN, 11);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col) {
        Route route = (Route) value;
        String routeName = route.getName();
        Font font = null;
        if (route.isActive()) {
            font = activeBold;
        } else {
            font = inactivePlain;
        }
        setFont(font);
        setText(routeName);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setBorder(null);
        setBackground(Color.white);
        return this;
    }
}
