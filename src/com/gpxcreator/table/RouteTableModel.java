package com.gpxcreator.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.gpxcreator.gpxpanel.Route;

@SuppressWarnings("serial")
public class RouteTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Visible", "Name", "Color"};
    private List<Route> routes;

    public RouteTableModel() {
        routes = new ArrayList<Route>();
    }
    
    @Override
    public int getRowCount() {
        return routes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return routes.get(rowIndex); // all columns use Route reference as value, but render it differently
    }
    
    @Override
    public void setValueAt(Object aValue, int row, int col) {
        routes.set(row, (Route) aValue);
        this.fireTableDataChanged();
        this.fireTableCellUpdated(row, col);
    }
    
    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }
    
    public void addRoute(Route route) {
        routes.add(route);
        this.fireTableDataChanged();
    }
    
    public void removeRoute(Route route) {
        routes.remove(route);
        this.fireTableDataChanged();
    }
}
