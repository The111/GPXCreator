package com.gpxcreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.gpxcreator.gpxpanel.GPXPanel;
import com.gpxcreator.gpxpanel.Route;
import com.gpxcreator.gpxpanel.RoutePoint;
import com.gpxcreator.table.RouteColorEditor;
import com.gpxcreator.table.RouteColorRenderer;
import com.gpxcreator.table.RouteNameEditor;
import com.gpxcreator.table.RouteNameRenderer;
import com.gpxcreator.table.RouteTableModel;
import com.gpxcreator.table.RouteVisEditor;
import com.gpxcreator.table.RouteVisRenderer;

@SuppressWarnings("serial")
public class GPXCreator extends JComponent {
    
    // temporary ghetto indent style to show layout hierarchy
    private JFrame frame;
        private JToolBar toolBarMain;       // NORTH
            private JButton btnRouteNew;    
            private JButton btnFileSave;
            private JFileChooser chooserFileSave;
            private File fileSave;
            private JButton btnFileOpen;
            private JFileChooser chooserFileOpen;
            private File fileOpened;
            private JButton btnRouteDelete;
            private JButton btnEditRouteProperties;
            private JToggleButton btnEditRouteAddPoints;
            private JToggleButton btnEditRouteDelPoints;
            private JButton btnEleChart;
            private JButton btnCorrectEle;
            private JComboBox<String> comboBoxTileSource;
        private JSplitPane splitPaneMain;   // CENTER
            private JSplitPane splitPaneSidebar;    // LEFT
                private JPanel containerLeftSidebarTop;        // TOP
                    private JPanel containerRoutesHeading;
                        private JLabel labelRoutesHeading;
                        private JLabel labelRoutesSubheading;
                    private JScrollPane scrollPaneRoutes;
                        private RouteTableModel tableModelRoutes;
                        private JTable tableRoutes;
                private JPanel containerLeftSidebarBottom;    // BOTTOM
                    private JPanel containerRouteProps;
                        private JLabel labelRoutePropsHeading;
                    private JScrollPane scrollPaneRouteProps;
                        private DefaultTableModel tableModelRouteProps;
                        private JTable tableRouteProps;
            private GPXPanel mapPanel;              // RIGHT
            private Route activeRoute;
            private MouseAdapter mapClickListener;

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
    private void initialize() {

        /* ---------------------------------------------- MAIN FRAME ----------------------------------------------- */
        frame = new JFrame("GPX Creator");
        InputStream in = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/gpx-creator.png");
        BufferedImage bufImg = null;
        if (in != null) {
            try {
                bufImg = ImageIO.read(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        frame.setIconImage(bufImg);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        frame.setBounds(300, 188, (int) (width - 600), (int) (height - 376));
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
        
        /* ------------------------------------------ SIDEBAR SPLIT PANE ------------------------------------------- */
        splitPaneSidebar = new JSplitPane();
        splitPaneSidebar.setMinimumSize(new Dimension(240, 25));
        splitPaneSidebar.setPreferredSize(new Dimension(240, 25));
        splitPaneSidebar.setContinuousLayout(true);
        splitPaneSidebar.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneMain.setLeftComponent(splitPaneSidebar);
        
        /* -------------------------------------- LEFT SIDEBAR TOP CONTAINER --------------------------------------- */
        containerLeftSidebarTop = new JPanel();
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
        containerRoutesHeading.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        containerRoutesHeading.setLayout(new BoxLayout(containerRoutesHeading, BoxLayout.Y_AXIS));
        containerRoutesHeading.setBorder(new CompoundBorder(
                new MatteBorder(0, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebarTop.add(containerRoutesHeading);
        
        /* -------------------------------------------- ROUTES HEADING --------------------------------------------- */
        labelRoutesHeading = new JLabel("Routes");
        labelRoutesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesHeading.setMaximumSize(new Dimension(32767, 14));
        labelRoutesHeading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutesHeading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutesHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        containerRoutesHeading.add(labelRoutesHeading);
        labelRoutesSubheading = new JLabel("(active route shown in bold - click to activate/center)");
        labelRoutesSubheading.setBackground(Color.BLUE);
        labelRoutesSubheading.setMinimumSize(new Dimension(100, 14));
        labelRoutesSubheading.setPreferredSize(new Dimension(100, 14));
        labelRoutesSubheading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesSubheading.setMaximumSize(new Dimension(32767, 14));
        labelRoutesSubheading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutesSubheading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutesSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        containerRoutesHeading.add(labelRoutesSubheading);
        
        /* ------------------------------------------- ROUTE TABLE/MODEL ------------------------------------------- */
        tableModelRoutes = new RouteTableModel();
        tableRoutes = new JTable(tableModelRoutes);
        tableRoutes.setAlignmentY(Component.TOP_ALIGNMENT);
        tableRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableRoutes.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableRoutes.setFillsViewportHeight(true);
        tableRoutes.setShowVerticalLines(false);
        tableRoutes.setTableHeader(null);
        tableRoutes.setSelectionBackground(Color.white);
        tableRoutes.getColumn("Visible").setCellRenderer(new RouteVisRenderer());
        tableRoutes.getColumn("Visible").setCellEditor(new RouteVisEditor());
        tableRoutes.getColumn("Name").setCellRenderer(new RouteNameRenderer());
        tableRoutes.getColumn("Name").setCellEditor(new RouteNameEditor(this));
        tableRoutes.getColumn("Color").setCellRenderer(new RouteColorRenderer());
        tableRoutes.getColumn("Color").setCellEditor(new RouteColorEditor());
        tableRoutes.getColumn("Visible").setPreferredWidth(14);
        tableRoutes.getColumn("Visible").setMinWidth(14);
        tableRoutes.getColumn("Visible").setMaxWidth(14);
        tableRoutes.getColumn("Color").setPreferredWidth(14);
        tableRoutes.getColumn("Color").setMinWidth(14);
        tableRoutes.getColumn("Color").setMaxWidth(14);
        tableModelRoutes.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 1) { // update active route
                    Route route = (Route) tableModelRoutes.getValueAt(e.getLastRow(), e.getColumn());
                    setActiveRoute(route);
                }
                mapPanel.repaint(); // update route visibility on map
            }
        });
        tableRoutes.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tableRoutes.rowAtPoint(new Point(e.getX(), e.getY()));
                int column = tableRoutes.columnAtPoint(new Point(e.getX(), e.getY()));
                for (Route rte : mapPanel.getRoutes()) {
                    rte.setTableHighlight(false);
                }
                if (row != -1) {
                    Route route = (Route) tableRoutes.getValueAt(row, column);
                    route.setTableHighlight(true);
                }
                tableModelRoutes.fireTableDataChanged();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            }
        });
        tableRoutes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                for (Route rte : mapPanel.getRoutes()) {
                    rte.setTableHighlight(false);
                }
                tableModelRoutes.fireTableDataChanged();
            }
        });
        
        /* ----------------------------------------- ROUTE TABLE SCROLLPANE ---------------------------------------- */
        scrollPaneRoutes = new JScrollPane(tableRoutes);
        scrollPaneRoutes.setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPaneRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPaneRoutes.setBorder(new LineBorder(new Color(0, 0, 0)));
        containerLeftSidebarTop.add(scrollPaneRoutes);
        
        /* ------------------------------------ LEFT SIDEBAR BOTTOM CONTAINER -------------------------------------- */
        containerLeftSidebarBottom = new JPanel();
        containerLeftSidebarBottom.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebarBottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebarBottom.setLayout(new BoxLayout(containerLeftSidebarBottom, BoxLayout.Y_AXIS));
        splitPaneSidebar.setBottomComponent(containerLeftSidebarBottom);
        
        /* -------------------------------------- ROUTE PROPERTIES CONTAINER --------------------------------------- */
        containerRouteProps = new JPanel();
        containerRouteProps.setMaximumSize(new Dimension(32767, 35));
        containerRouteProps.setMinimumSize(new Dimension(10, 35));
        containerRouteProps.setPreferredSize(new Dimension(10, 35));
        containerRouteProps.setAlignmentY(Component.TOP_ALIGNMENT);
        containerRouteProps.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerRouteProps.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        containerRouteProps.setLayout(new BoxLayout(containerRouteProps, BoxLayout.Y_AXIS));
        containerRouteProps.setBorder(new CompoundBorder(
                new MatteBorder(0, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebarBottom.add(containerRouteProps);
        
        /* --------------------------------------- ROUTE PROPERTIES HEADING ---------------------------------------- */
        labelRoutePropsHeading = new JLabel("Route Properties");
        labelRoutePropsHeading.setMaximumSize(new Dimension(32767, 14));
        labelRoutePropsHeading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutePropsHeading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutePropsHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelRoutePropsHeading.setAlignmentY(0.0f);
        containerRouteProps.add(labelRoutePropsHeading);
        
        /* ------------------------------------- ROUTE PROPERTIES TABLE/MODEL -------------------------------------- */
        tableModelRouteProps = new DefaultTableModel(new Object[]{"Name", "Value"},0);
        tableRouteProps = new JTable(tableModelRouteProps);
        tableRouteProps.setAlignmentY(Component.TOP_ALIGNMENT);
        tableRouteProps.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableRouteProps.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableRouteProps.setFillsViewportHeight(true);
        tableRouteProps.setTableHeader(null);
        tableRouteProps.setEnabled(false);
        tableRouteProps.getColumn("Name").setPreferredWidth(100);
        tableRouteProps.getColumn("Name").setMinWidth(100);
        tableRouteProps.getColumn("Value").setPreferredWidth(140);
        tableRouteProps.getColumn("Value").setMinWidth(140);
        
        /* -------------------------------------- ROUTE PROPERTIES SCROLLPANE -------------------------------------- */
        scrollPaneRouteProps = new JScrollPane(tableRouteProps);
        scrollPaneRouteProps.setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPaneRouteProps.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPaneRouteProps.setBorder(new LineBorder(new Color(0, 0, 0)));
        containerLeftSidebarBottom.add(scrollPaneRouteProps);
        splitPaneSidebar.setDividerLocation(160);
        
        /* --------------------------------------------- MAIN TOOLBAR ---------------------------------------------- */
        toolBarMain = new JToolBar();
        toolBarMain.setFloatable(false);
        toolBarMain.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        frame.getContentPane().add(toolBarMain, BorderLayout.NORTH);
        
        /* ------------------------------------------- NEW ROUTE BUTTON -------------------------------------------- */
        btnRouteNew = new JButton("");
        btnRouteNew.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                routeNew();
            }
        });
        btnRouteNew.setToolTipText("<html>Create new route<br>[CTRL+N]</html>");
        btnRouteNew.setFocusable(false);
        btnRouteNew.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/route-new.png")));
        String ctrlNew = "CTRL+N";
        mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), ctrlNew);
        mapPanel.getActionMap().put(ctrlNew, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                routeNew();
            }
        });
        toolBarMain.add(btnRouteNew);
        
        /* ---------------------------------------------- OPEN BUTTON ---------------------------------------------- */
        FileNameExtensionFilter gpxFilter = new FileNameExtensionFilter("GPX files (*.gpx)", "gpx");
        btnFileOpen = new JButton("");
        chooserFileOpen = new JFileChooser();
        chooserFileOpen.setCurrentDirectory(new File("C:\\eclipse\\workspace\\GPXCreator\\IO")); // TODO
                                                                                        // change dir before deployment
        chooserFileOpen.addChoosableFileFilter(gpxFilter);
        chooserFileOpen.setFileFilter(gpxFilter);
        btnFileOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fileOpen();
            }
        });
        btnFileOpen.setToolTipText("<html>Open GPX file as route<br>[CTRL+O]</html>");
        btnFileOpen.setFocusable(false);
        btnFileOpen.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-open.png")));
        String ctrlOpen = "CTRL+O";
        mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), ctrlOpen);
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
        chooserFileSave.setCurrentDirectory(new File("C:\\eclipse\\workspace\\GPXCreator\\IO")); // TODO
                                                                                        // change dir before deployment
        chooserFileSave.addChoosableFileFilter(gpxFilter);
        chooserFileSave.setFileFilter(gpxFilter);
        btnFileSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fileSave();
            }
        });
        btnFileSave.setToolTipText("<html>Save active route to GPX file<br>[CTRL+S]</html>");
        btnFileSave.setFocusable(false);
        btnFileSave.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-save.png")));
        String ctrlSave = "CTRL+S";
        mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), ctrlSave);
        mapPanel.getActionMap().put(ctrlSave, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSave();
            }
        });
        toolBarMain.add(btnFileSave);
        
        /* ------------------------------------------ ROUTE DELETE BUTTON ------------------------------------------ */
        btnRouteDelete = new JButton("");
        btnRouteDelete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                deleteActiveRoute();
            }
        });
        btnRouteDelete.setToolTipText("<html>Delete active route<br>[CTRL+D]</html>");
        btnRouteDelete.setFocusable(false);
        btnRouteDelete.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/route-delete.png")));
        String ctrlDelete = "CTRL+D";
        mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK), ctrlDelete);
        mapPanel.getActionMap().put(ctrlDelete, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteActiveRoute();
            }
        });
        toolBarMain.add(btnRouteDelete);
        
        toolBarMain.addSeparator();
        
        /* ---------------------------------------- EDIT PROPERTIES BUTTON ----------------------------------------- */
        btnEditRouteProperties = new JButton("");
        btnEditRouteProperties.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                //deleteActiveRoute();
            }
        });
        btnEditRouteProperties.setToolTipText("Edit route properties");
        btnEditRouteProperties.setFocusable(false);
        btnEditRouteProperties.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/edit-route-properties.png")));
        toolBarMain.add(btnEditRouteProperties);
        
        /* ------------------------------------------- ADD POINTS BUTTON ------------------------------------------- */
        btnEditRouteAddPoints = new JToggleButton("");
        btnEditRouteAddPoints.setToolTipText("Add points");
        btnEditRouteAddPoints.setFocusable(false);
        btnEditRouteAddPoints.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/edit-route-add-points.png")));
        toolBarMain.add(btnEditRouteAddPoints);
        mapClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (btnEditRouteAddPoints.isSelected() && activeRoute != null) {
                    int zoom = mapPanel.getZoom();
                    int x = e.getX();
                    int y = e.getY();
                    Point mapCenter = mapPanel.getCenter();
                    int xStart = mapCenter.x - mapPanel.getWidth() / 2;
                    int yStart = mapCenter.y - mapPanel.getHeight() / 2;
                    double lat = OsmMercator.YToLat(yStart + y, zoom);
                    double lon = OsmMercator.XToLon(xStart + x, zoom);
                    RoutePoint rtept = new RoutePoint(lat, lon);
                    activeRoute.addRoutePoint(rtept, true);
                    mapPanel.repaint();
                    resetRoutePropsTable();
                }
            }
        };
        mapPanel.addMouseListener(mapClickListener);
        btnEditRouteAddPoints.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (btnEditRouteDelPoints.isSelected()) {
                        btnEditRouteDelPoints.setSelected(false);
                    }
                }
            }
        });
        
        /* ----------------------------------------- DELETE POINTS BUTTON ------------------------------------------ */
        btnEditRouteDelPoints = new JToggleButton("");
        btnEditRouteDelPoints.setToolTipText("Delete points");
        btnEditRouteDelPoints.setFocusable(false);
        btnEditRouteDelPoints.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/edit-route-delete-points.png")));
        toolBarMain.add(btnEditRouteDelPoints);
        btnEditRouteDelPoints.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (btnEditRouteAddPoints.isSelected()) {
                        btnEditRouteAddPoints.setSelected(false);
                    }
                }
            }
        });
        
        /* --------------------------------------- CORRECT ELEVATION BUTTON ---------------------------------------- */
        btnCorrectEle = new JButton("");
        btnCorrectEle.setToolTipText("Correct elevation");
        btnCorrectEle.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/correct-elevation.png")));
        btnCorrectEle.setFocusable(false);
        btnCorrectEle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeRoute != null) {
                    activeRoute.correctElevation();
                    resetRoutePropsTable();
                }
            }
        });
        toolBarMain.add(btnCorrectEle);
        
        /* ---------------------------------------- ELEVATION CHART BUTTON ----------------------------------------- */
        btnEleChart = new JButton("");
        btnEleChart.setToolTipText("View elevation profile chart");
        btnEleChart.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/elevation-chart.png")));
        btnEleChart.setFocusable(false);
        btnEleChart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeRoute != null) {
                    BufferedImage image = (BufferedImage) activeRoute.getElevationChartGET();
                    JLabel label = new JLabel(new ImageIcon(image));
                    JFrame f = new JFrame("Elevation profile for \"" +
                            activeRoute.getName() + "\" (elevation data and chart provided by MapQuest.com)");
                    InputStream in = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/elevation-chart.png");
                    if (in != null) {
                        try {
                            f.setIconImage(ImageIO.read(in));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    f.getContentPane().add(label);
                    f.pack();
                    f.setLocationRelativeTo(frame);
                    f.setVisible(true);
                }
            }
        });
        toolBarMain.add(btnEleChart);
        
        toolBarMain.add(Box.createHorizontalGlue());
        
        /* ----------------------------------------- TILE SOURCE SELECTOR ------------------------------------------ */
        final TileSource openStreetMap = new OsmTileSource.Mapnik();
        final TileSource openCycleMap = new OsmTileSource.CycleMap(); 
        final TileSource bingAerial = new BingAerialTileSource();
        final TileSource mapQuestOsm = new MapQuestOsmTileSource();
        final TileSource mapQuestOpenAerial = new MapQuestOpenAerialTileSource();
        comboBoxTileSource = new JComboBox<String>();
        comboBoxTileSource.addItem("OpenStreetMap");
        comboBoxTileSource.addItem("OpenCycleMap");
        comboBoxTileSource.addItem("Bing Aerial");
        comboBoxTileSource.addItem("MapQuest-OSM");
        comboBoxTileSource.addItem("MapQuest Open Aerial");
        comboBoxTileSource.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) comboBoxTileSource.getSelectedItem();
                switch (selected) {
                    case "OpenStreetMap":
                        mapPanel.setTileSource(openStreetMap);
                        break;
                    case "OpenCycleMap":
                        mapPanel.setTileSource(openCycleMap);
                        break;
                    case "Bing Aerial":
                        mapPanel.setTileSource(bingAerial);
                        break;
                    case "MapQuest-OSM":
                        mapPanel.setTileSource(mapQuestOsm);
                        break;
                    case "MapQuest Open Aerial":
                        mapPanel.setTileSource(mapQuestOpenAerial);
                        break;
                }
            }
        });
        comboBoxTileSource.setFocusable(false);
        comboBoxTileSource.setPreferredSize(new Dimension(150, 22));
        comboBoxTileSource.setMinimumSize(new Dimension(50, 22));
        comboBoxTileSource.setAlignmentX(Component.RIGHT_ALIGNMENT);
        comboBoxTileSource.setMaximumSize(new Dimension(20, 24));
        toolBarMain.add(comboBoxTileSource);
        
        // button for quick easy debugging
        /*JButton debug = new JButton("debug something");
        debug.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // do a thing
            }
        });
        toolBarMain.add(debug);*/
        
        /*java.util.Properties systemProperties = System.getProperties();
        systemProperties.setProperty("http.proxyHost", "proxy1.lmco.com");
        systemProperties.setProperty("http.proxyPort", "80");*/
    }
    
    public void routeNew() {
        String name = (String)JOptionPane.showInputDialog(frame, "Please type a name for the new route:",
                "New route", JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (name != null) {
            Route route = new Route(name);
            mapPanel.addRoute(route);
            tableModelRoutes.addRoute(route);
            int last = tableRoutes.getModel().getRowCount() - 1;
            Rectangle r = tableRoutes.getCellRect(last, 0, true);
            tableRoutes.scrollRectToVisible(r);
            setActiveRoute(route);
        }
    }
    
    public void fileOpen() {
        int returnVal = chooserFileOpen.showOpenDialog(mapPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileOpened = chooserFileOpen.getSelectedFile();
            Route route = new Route(fileOpened);
            mapPanel.addRoute(route);
            tableModelRoutes.addRoute(route);
            int last = tableRoutes.getModel().getRowCount() - 1;
            Rectangle r = tableRoutes.getCellRect(last, 0, true);
            tableRoutes.scrollRectToVisible(r);
            setActiveRoute(route);
        }
    }
    
    public void fileSave() {
        int returnVal = chooserFileSave.showSaveDialog(mapPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileSave = chooserFileSave.getSelectedFile();
            activeRoute.saveToGPXFile(fileSave);
        }
    }
    
    public void deleteActiveRoute() {
        if (activeRoute != null) {
            mapPanel.removeRoute(activeRoute);
            tableModelRoutes.removeRoute(activeRoute);
            this.activeRoute = null;
            clearRoutePropsTable();
        }
    }
    
    public Route getActiveRoute() {
        return activeRoute;
    }
    
    public void setActiveRoute(Route route) {
        if (activeRoute != null) {
            activeRoute.setActive(false);
        }
        route.setActive(true);
        route.setVisible(true);
        activeRoute = route;
        mapPanel.fitRouteToPanel(route);
        resetRoutePropsTable();
    }
    
    public void resetRoutePropsTable() {
        tableModelRouteProps.setRowCount(0);
        tableModelRouteProps.addRow(new Object[]{"type", activeRoute.getType()});
        tableModelRouteProps.addRow(new Object[]{"# of pts", activeRoute.getNumPts()});
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        String startTimeString = "";
        String endTimeString = "";
        if (activeRoute.getStart() != null && activeRoute.getEnd() != null) {
            Date startTimeDate = activeRoute.getEnd().getTime();
            Date endTimeDate = activeRoute.getEnd().getTime();
            if (startTimeDate != null && endTimeDate != null) {
                startTimeString = sdf.format(activeRoute.getStart().getTime());
                endTimeString = sdf.format(activeRoute.getEnd().getTime());
            }
        }
        tableModelRouteProps.addRow(new Object[]{"start time", startTimeString});
        tableModelRouteProps.addRow(new Object[]{"end time", endTimeString});
        long duration = activeRoute.getDuration();
        long hours = duration / 3600000;
        long minutes = (duration - hours * 3600000) / 60000;
        long seconds = (duration - hours * 3600000 - minutes * 60000) / 1000;
        if (duration != 0) {
            tableModelRouteProps.addRow(new Object[]{"duration", hours + "hr " + minutes + "min " + seconds + "sec"});
        } else {
            tableModelRouteProps.addRow(new Object[]{"duration", ""});
        }
        double lengthMiles = activeRoute.getLengthMiles();
        tableModelRouteProps.addRow(new Object[]{"length", String.format("%.2f mi", lengthMiles)});
        tableModelRouteProps.addRow(
                new Object[]{"elevation (start)", String.format("%.0f ft", activeRoute.getEleStartFeet())});
        tableModelRouteProps.addRow(
                new Object[]{"elevation (end)", String.format("%.0f ft", activeRoute.getEleEndFeet())});
        tableModelRouteProps.addRow(
                new Object[]{"min elevation", String.format("%.0f ft", activeRoute.getEleMinFeet())});
        tableModelRouteProps.addRow(
                new Object[]{"max elevation", String.format("%.0f ft", activeRoute.getEleMaxFeet())});
        double grossRiseFeet = activeRoute.getGrossRiseFeet();
        double grossFallFeet = activeRoute.getGrossFallFeet();
        tableModelRouteProps.addRow(new Object[]{"gross rise", String.format("%.0f ft", grossRiseFeet)});
        tableModelRouteProps.addRow(new Object[]{"gross fall", String.format("%.0f ft", grossFallFeet)});
        double avgSpeedMph = (lengthMiles / duration) * 3600000;
        if (Double.isNaN(avgSpeedMph) || Double.isInfinite(avgSpeedMph)) {
            avgSpeedMph = 0;
        }
        if (avgSpeedMph != 0) {
            tableModelRouteProps.addRow(new Object[]{"avg speed", String.format("%.1f mph", avgSpeedMph)});
        } else {
            tableModelRouteProps.addRow(new Object[]{"avg speed", ""});
        }
        if (activeRoute.getMaxSpeedMph() != 0) {
            tableModelRouteProps.addRow(
                    new Object[]{"max speed", String.format("%.1f mph", activeRoute.getMaxSpeedMph())});            
        } else {
            tableModelRouteProps.addRow(new Object[]{"max speed", ""});
        }
        
        long riseTime = activeRoute.getRiseTime();
        hours = riseTime / 3600000;
        minutes = (riseTime - hours * 3600000) / 60000;
        seconds = (riseTime - hours * 3600000 - minutes * 60000) / 1000;
        if (riseTime != 0) {
            tableModelRouteProps.addRow(new Object[]{"rise time", hours + "hr " + minutes + "min " + seconds + "sec"});
        } else {
            tableModelRouteProps.addRow(new Object[]{"rise time", ""});
        }
        long fallTime = activeRoute.getFallTime();
        hours = fallTime / 3600000;
        minutes = (fallTime - hours * 3600000) / 60000;
        seconds = (fallTime - hours * 3600000 - minutes * 60000) / 1000;
        if (fallTime != 0) {
            tableModelRouteProps.addRow(new Object[]{"fall time", hours + "hr " + minutes + "min " + seconds + "sec"});
        } else {
            tableModelRouteProps.addRow(new Object[]{"fall time", ""});
        }
        double avgRiseSpeedFph = (grossRiseFeet / riseTime) * 3600000;
        double avgFallSpeedFph = (grossFallFeet / fallTime) * 3600000;
        if (Double.isNaN(avgRiseSpeedFph) || Double.isInfinite(avgRiseSpeedFph)) {
            avgRiseSpeedFph = 0;
        }
        if (Double.isNaN(avgFallSpeedFph) || Double.isInfinite(avgFallSpeedFph)) {
            avgFallSpeedFph = 0;
        }
        if (avgRiseSpeedFph != 0) {
            tableModelRouteProps.addRow(new Object[]{"avg rise speed", String.format("%.0f ft/hr", avgRiseSpeedFph)});
        } else {
            tableModelRouteProps.addRow(new Object[]{"avg rise speed", ""});
        }
        if (avgFallSpeedFph != 0) {
            tableModelRouteProps.addRow(new Object[]{"avg fall speed", String.format("%.0f ft/hr", avgFallSpeedFph)});
        } else {
            tableModelRouteProps.addRow(new Object[]{"avg fall speed", ""});
        }
    }
    
    public void clearRoutePropsTable() {
        tableModelRouteProps.setRowCount(0);
    }
}
