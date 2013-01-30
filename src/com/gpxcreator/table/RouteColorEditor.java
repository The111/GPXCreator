package com.gpxcreator.table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EventObject;

import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.gpxcreator.GPXCreator;
import com.gpxcreator.gpxpanel.Route;

@SuppressWarnings("serial")
public class RouteColorEditor extends AbstractCellEditor implements TableCellEditor, MouseListener, ActionListener {
    
    private RouteColorEditorComponent editorComponent;
    private Route route;
    private Color currentColor;
    private JColorChooser colorChooser;
    private JDialog dialog;
    
    public RouteColorEditor() {
        editorComponent = new RouteColorEditorComponent(Color.white);
        editorComponent.addMouseListener(this);
        colorChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(editorComponent, "Choose a Color", true, colorChooser, this, null);
        BufferedImage img = null;
        InputStream in = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/color-palette.png");
        try {
            img = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setIconImage(img);
    }
    
    @Override
    public boolean isCellEditable(EventObject e) {
        return (e instanceof MouseEvent);
    }

    public Object getCellEditorValue() {
        return route;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        route = (Route) value;
        Color color = route.getColor();
        editorComponent.setColor(color);
        return editorComponent;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        colorChooser.setColor(currentColor);
        dialog.setVisible(true);
        this.fireEditingStopped();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        colorChooser.setColor(currentColor);
        dialog.setVisible(true);
        this.fireEditingStopped();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        colorChooser.setColor(currentColor);
        dialog.setVisible(true);
        this.fireEditingStopped();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        currentColor = colorChooser.getColor();
        route.setColor(currentColor);
    }

    private static class RouteColorEditorComponent extends JLabel {
    
        private Color color;
        private static final int SIZE = 8;
        
        public RouteColorEditorComponent(Color color) {
            this.color = color;
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            int x = (this.getWidth() - SIZE) / 2;
            int y = (this.getHeight() - SIZE) / 2;
            g2d.fillRect(x, y, SIZE, SIZE);
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, SIZE, SIZE);
        }
    }
}
