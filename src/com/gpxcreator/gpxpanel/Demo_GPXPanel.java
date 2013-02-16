package com.gpxcreator.gpxpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

public class Demo_GPXPanel {

    private JFrame frame;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Demo_GPXPanel window = new Demo_GPXPanel();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public Demo_GPXPanel() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame("GPXPanel Demo");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        frame.setBounds((int) (width/10), (int) (height/10), (int) (width * 0.8), (int) (height * 0.8));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GPXPanel panel = new GPXPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setZoom(14);
        panel.setCenter(new Point(OsmMercator.LonToX(-122.7693, 14), OsmMercator.LatToY(37.9525, 14)));
        
        Route wildcat = new Route(new File("IO/Wildcat to Palomarin__20120608_1111.gpx"));
        panel.addRoute(wildcat);
        wildcat.saveToGPXFile(new File("IO/wildcat-out.gpx"));
    }
}
