package com.gpxcreator.table;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.gpxcreator.GPXCreator;
import com.gpxcreator.gpxpanel.Route;

@SuppressWarnings("serial")
public class RouteVisEditor extends AbstractCellEditor implements TableCellEditor, MouseListener {

    private RouteVisEditorComponent editorComponent;
    private Route route;

    public RouteVisEditor() {
        editorComponent = new RouteVisEditorComponent(false);
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
        editorComponent.setValue(route.isVisible());
        editorComponent.setOpaque(isSelected);
        editorComponent.setBackground(table.getSelectionBackground());
        return editorComponent;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        route.setVisible(!route.isVisible());
        this.fireEditingStopped();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        route.setVisible(!route.isVisible());
        this.fireEditingStopped();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        route.setVisible(!route.isVisible());
        this.fireEditingStopped();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    private class RouteVisEditorComponent extends JLabel {
        
        private ImageIcon visible;
        private ImageIcon invisible;
        
        public RouteVisEditorComponent(boolean value) {
            try {
                visible = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
                        "/com/gpxcreator/icons/route-visible.png")));
                invisible = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
                        "/com/gpxcreator/icons/route-invisible.png")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void setValue(boolean value) {
            if (value) {
                setIcon(visible);
            } else{
                setIcon(invisible);
            }
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }
    }
}
