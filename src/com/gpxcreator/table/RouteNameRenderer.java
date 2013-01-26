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
    
    private static final Font BOLD = new Font("Tahoma", Font.BOLD, 11);
    private static final Font PLAIN = new Font("Tahoma", Font.PLAIN, 11);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col) {
        Route route = (Route) value;
        String routeName = route.getName();
        if (route.isActive()) {
            setFont(BOLD);
        } else {
            setFont(PLAIN);
        }
        setText(routeName);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setBorder(null);
        setBackground(Color.white);
        return this;
    }
}
