package com.gpxcreator.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.gpxcreator.GPXCreator;
import com.gpxcreator.gpxpanel.Route;

@SuppressWarnings("serial")
public class RouteNameEditor extends AbstractCellEditor implements TableCellEditor, MouseListener {

    private RouteNameEditorComponent editorComponent;
    private Route route;
    
    public RouteNameEditor(GPXCreator gui) {
        editorComponent = new RouteNameEditorComponent();
        editorComponent.addMouseListener(this);
    }
    
    @Override
    public boolean isCellEditable(EventObject e) {
        return (e instanceof MouseEvent);
    }
    
    @Override
    public Object getCellEditorValue() {
        return route;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        route = (Route) value;
        boolean isActive = route.isActive();
        editorComponent.setName(route.getName());
        editorComponent.setActive(isActive);
        editorComponent.setOpaque(isSelected);
        editorComponent.setBackground(table.getSelectionBackground());
        return editorComponent;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        route.setActive(true);
        this.fireEditingStopped();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        route.setActive(true);
        this.fireEditingStopped();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        route.setActive(true);
        this.fireEditingStopped();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    private static class RouteNameEditorComponent extends JLabel {
        
        private static final Font BOLD = new Font("Tahoma", Font.BOLD, 11);
        private static final Font PLAIN = new Font("Tahoma", Font.PLAIN, 11);
        
        public void setName(String name) {
            setText(name);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setBorder(null);
            setBackground(Color.white);
        }
        
        public void setActive(boolean active) {
            if (active) {
                setFont(BOLD);
            } else {
                setFont(PLAIN);
            }
        }
    }
}