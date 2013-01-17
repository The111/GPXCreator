package com.gpxcreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;

import com.gpxcreator.gpxpanel.GPXPanel;
import com.gpxcreator.gpxpanel.Route;

public class GPXCreator {
    
    private JFrame frame;
        private JToolBar toolBarMain;           // NORTH
            private JButton btnFileSave;
            private JFileChooser chooserFileSave;
            private File fileSave;
            private JButton btnFileOpen;
            private JFileChooser chooserFileOpen;
            private File fileOpened;
        private JSplitPane splitPaneMain;       // CENTER
            private JSplitPane splitPaneSidebar;    // LEFT
                private JPanel containerLeftSidebarTop;        // TOP
                    private JPanel containerRoutesHeading;
                        private JLabel labelRoutesHeading;
                        private JLabel labelRoutesSubheading;
                    private JPanel containerRoutes; // necessary???
                        private JScrollPane scrollPaneRoutes;
                            private DefaultTableModel routeTableModel;
                            private JTable tableRoutes;
                private JPanel containerLeftSidebarBottom;    // BOTTOM
            private GPXPanel mapPanel;              // RIGHT

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
                    GPXCreator window = new GPXCreator();
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
    public GPXCreator() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    @SuppressWarnings("serial")
    private void initialize() {

        /* ---------------------------------------------- MAIN FRAME ----------------------------------------------- */
        frame = new JFrame("GPX Creator");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(GPXCreator.class.getResource("/com/gpxcreator/icons/gpxcreator.png")));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        frame.setBounds(300, 188, (int) (width - 600), (int) (height - 376));
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        /* -------------------------------------------- MAIN SPLIT PANE -------------------------------------------- */
        splitPaneMain = new JSplitPane();
        splitPaneMain.setContinuousLayout(true);
        frame.getContentPane().add(splitPaneMain, BorderLayout.CENTER);

        /* ----------------------------------------------- MAP PANEL ----------------------------------------------- */
        mapPanel = new GPXPanel();
        mapPanel.setDisplayPositionByLatLon(36, -98, 4); // U! S! A!
        try {
            mapPanel.setTileLoader(new OsmFileCacheTileLoader(mapPanel));
        } catch (Exception e) {
            System.err.println("There was a problem constructing the tile cache on disk.");
            e.printStackTrace();
        }
        splitPaneMain.setRightComponent(mapPanel);
        
        /* ---------------------------------------- LEFT SIDEBAR CONTAINER ----------------------------------------- */
        
        /*containerLeftSidebar = new JPanel();
        containerLeftSidebar.setLayout(new BoxLayout(containerLeftSidebar, BoxLayout.Y_AXIS));
        splitPaneMain.setLeftComponent(containerLeftSidebar);*/
        
        /* ------------------------------------------ SIDEBAR SPLIT PANE ------------------------------------------- */
        splitPaneSidebar = new JSplitPane();
        splitPaneSidebar.setContinuousLayout(true);
        splitPaneSidebar.setOrientation(JSplitPane.VERTICAL_SPLIT);
        //containerLeftSidebar.add(splitPaneSidebar);
        splitPaneMain.setLeftComponent(splitPaneSidebar);
        
        /* -------------------------------------- LEFT SIDEBAR TOP CONTAINER --------------------------------------- */
        containerLeftSidebarTop = new JPanel();
        containerLeftSidebarTop.setMinimumSize(new Dimension(10, 100));
        containerLeftSidebarTop.setMaximumSize(new Dimension(32767, 200));
        containerLeftSidebarTop.setPreferredSize(new Dimension(10, 100));
        containerLeftSidebarTop.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebarTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebarTop.setLayout(new BoxLayout(containerLeftSidebarTop, BoxLayout.Y_AXIS));
        splitPaneSidebar.setTopComponent(containerLeftSidebarTop);
        
        /* --------------------------------------- ROUTES HEADING CONTAINER ---------------------------------------- */
        containerRoutesHeading = new JPanel();
        containerRoutesHeading.setPreferredSize(new Dimension(10, 35));
        containerRoutesHeading.setMinimumSize(new Dimension(10, 35));
        containerRoutesHeading.setMaximumSize(new Dimension(32767, 35));
        containerRoutesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        containerRoutesHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerRoutesHeading.setBackground(Color.GREEN);
        containerRoutesHeading.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        containerRoutesHeading.setLayout(new BoxLayout(containerRoutesHeading, BoxLayout.Y_AXIS));
        containerRoutesHeading.setBorder(new CompoundBorder(new MatteBorder(0, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebarTop.add(containerRoutesHeading);
        
        /* -------------------------------------------- ROUTES HEADING --------------------------------------------- */
        labelRoutesHeading = new JLabel("Routes");
        labelRoutesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesHeading.setMaximumSize(new Dimension(32767, 14));
        labelRoutesHeading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutesHeading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutesHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        containerRoutesHeading.add(labelRoutesHeading);
        labelRoutesSubheading = new JLabel("(active route shown in bold)");
        labelRoutesSubheading.setMinimumSize(new Dimension(100, 14));
        labelRoutesSubheading.setPreferredSize(new Dimension(100, 14));
        labelRoutesSubheading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesSubheading.setMaximumSize(new Dimension(32767, 14));
        labelRoutesSubheading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutesSubheading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutesSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        containerRoutesHeading.add(labelRoutesSubheading);
        
        /* ----------------------------------------- ROUTE TABLE CONTAINER ----------------------------------------- */
        containerRoutes = new JPanel();
        containerRoutes.setAlignmentY(Component.TOP_ALIGNMENT);
        containerRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerRoutes.setLayout(new BoxLayout(containerRoutes, BoxLayout.Y_AXIS));
        containerRoutes.setBorder(new EmptyBorder(0, 0, 0, 0));
        containerLeftSidebarTop.add(containerRoutes);
        

        
        /* ------------------------------------------- ROUTE TABLE/MODEL ------------------------------------------- */
        routeTableModel = new DefaultTableModel(new Object[]{"Route Names"},0);
        tableRoutes = new JTable(routeTableModel);
        tableRoutes.setPreferredScrollableViewportSize(new Dimension(100, 50));
        tableRoutes.setMaximumSize(new Dimension(32767, 32767));
        tableRoutes.setPreferredSize(new Dimension(100, 50));
        tableRoutes.setMinimumSize(new Dimension(100, 25));
        tableRoutes.setAlignmentY(Component.TOP_ALIGNMENT);
        tableRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableRoutes.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableRoutes.setFillsViewportHeight(true);
        tableRoutes.setTableHeader(null);
        tableRoutes.setEnabled(false); // TODO <-- this is only temporary... until table can be developed further!
        //scrollPaneRoutes.add(tableRoutes);
        
        
        
        /* ----------------------------------------- ROUTE TABLE SCROLLPANE ---------------------------------------- */
        scrollPaneRoutes = new JScrollPane(tableRoutes);
        scrollPaneRoutes.setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPaneRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPaneRoutes.setBorder(new LineBorder(new Color(0, 0, 0)));
        containerRoutes.add(scrollPaneRoutes);
        
        
        
        /* ------------------------------------ LEFT SIDEBAR BOTTOM CONTAINER -------------------------------------- */
        containerLeftSidebarBottom = new JPanel();
        containerLeftSidebarBottom.setMinimumSize(new Dimension(150, 20));
        containerLeftSidebarBottom.setPreferredSize(new Dimension(150, 300));
        containerLeftSidebarBottom.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebarBottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebarBottom.setLayout(new BoxLayout(containerLeftSidebarBottom, BoxLayout.Y_AXIS));
        splitPaneSidebar.setBottomComponent(containerLeftSidebarBottom);
        
        
        
        
        /* --------------------------------------------- MAIN TOOLBAR ---------------------------------------------- */
        toolBarMain = new JToolBar();
        toolBarMain.setFloatable(false);
        toolBarMain.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        frame.getContentPane().add(toolBarMain, BorderLayout.NORTH);
        
        /* ---------------------------------------------- OPEN BUTTON ---------------------------------------------- */
        FileNameExtensionFilter gpxFilter = new FileNameExtensionFilter("GPX files (*.gpx)", "gpx");
        btnFileOpen = new JButton("");
        chooserFileOpen = new JFileChooser();
        chooserFileOpen.setCurrentDirectory(new File("C:\\eclipse\\workspace\\GPXCreator\\IO")); // TODO change dir before deployment
        chooserFileOpen.addChoosableFileFilter(gpxFilter);
        chooserFileOpen.setFileFilter(gpxFilter);
        btnFileOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileOpen();
            }
        });
        btnFileOpen.setToolTipText("Open GPX file as route");
        btnFileOpen.setFocusable(false);
        btnFileOpen.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-open.png")));
        String ctrlOpen = "CTRL+O";
        mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), ctrlOpen);
        mapPanel.getActionMap().put(ctrlOpen, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileOpen();
            }
        });
        toolBarMain.add(btnFileOpen);
        
        /* ---------------------------------------------- SAVE BUTTON ---------------------------------------------- */
        btnFileSave = new JButton("");
        chooserFileSave = new JFileChooser();
        chooserFileSave.setCurrentDirectory(new File("C:\\eclipse\\workspace\\GPXCreator\\IO")); // TODO change dir before deployment
        chooserFileSave.addChoosableFileFilter(gpxFilter);
        chooserFileSave.setFileFilter(gpxFilter);
        btnFileSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileSave();
            }
        });
        btnFileSave.setToolTipText("Save route to GPX file");
        btnFileSave.setFocusable(false);
        btnFileSave.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-save.png")));
        String ctrlSave = "CTRL+S";
        mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), ctrlSave);
        mapPanel.getActionMap().put(ctrlSave, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSave();
            }
        });
        toolBarMain.add(btnFileSave);
        
        
        
        
        
        
        

        

        

        

        
        

        
        
        
        
        
        
        
        
        
        
        
        
        

        


        



        
        
        
        



        

        
        
        
        // bogus event generator for quick testing of event handling
        /*mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int count = routeTableModel.getRowCount()+1;
                routeTableModel.addRow(new Object[]{"Route" + count});
                System.out.println("yah");
            }
        });*/
        
        java.util.Properties systemProperties = System.getProperties();
        systemProperties.setProperty("http.proxyHost", "proxy1.lmco.com");
        systemProperties.setProperty("http.proxyPort", "80");
    }
    
    public void fileOpen() {
        int returnVal = chooserFileOpen.showOpenDialog(mapPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileOpened = chooserFileOpen.getSelectedFile();
            Route route = new Route(fileOpened);
            mapPanel.addRoute(route);
            routeTableModel.addRow(new Object[]{route.getName()});
            mapPanel.fitRouteToPanel(route);
        }
    }
    
    public void fileSave() {
        int returnVal = chooserFileSave.showSaveDialog(mapPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileSave = chooserFileSave.getSelectedFile();
            mapPanel.getActiveRoute().saveToGPXFile(fileSave);
        }
    }
}
