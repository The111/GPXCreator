package com.gpxcreator;

import java.awt.AWTEvent;
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
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TemplatedTMSTileSource;

import com.gpxcreator.gpxpanel.GPXFile;
import com.gpxcreator.gpxpanel.GPXObject;
import com.gpxcreator.gpxpanel.GPXPanel;
import com.gpxcreator.gpxpanel.Route;
import com.gpxcreator.gpxpanel.Track;
import com.gpxcreator.gpxpanel.Waypoint;
import com.gpxcreator.gpxpanel.WaypointGroup;
import com.gpxcreator.gpxpanel.WaypointGroup.EleCorrectedStatus;
import com.gpxcreator.gpxpanel.WaypointGroup.WptGrpType;
import com.gpxcreator.tree.GPXTreeEditor;
import com.gpxcreator.tree.GPXTreeRenderer;

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
            private JLabel lblLon;
            private JTextField textFieldLat;
            private JLabel lblLat;
            private JTextField textFieldLon;
            private JToggleButton btnLatLonFocus;
        private JSplitPane splitPaneMain;   // CENTER
            private JSplitPane splitPaneSidebar;    // LEFT
                private JPanel containerLeftSidebarTop;        // TOP
                    private JPanel containerRoutesHeading;
                        private JLabel labelRoutesHeading;
                        private JLabel labelRoutesSubheading;
                    private JScrollPane scrollPaneRoutes;
                        private DefaultMutableTreeNode root;
                        private DefaultTreeModel treeModel;
                        private JTree tree;
                        private DefaultMutableTreeNode previouslySelected;
                        

                        
                        
                        
                        
                        //private RouteTableModel tableModelRoutes;
                        //private JTable tableRoutes;
                private JPanel containerLeftSidebarBottom;    // BOTTOM
                    private JPanel containerRouteProps;
                        private JLabel labelRoutePropsHeading;
                    private JScrollPane scrollPaneRouteProps;
                        private DefaultTableModel tableModelRouteProps;
                        private JTable tableRouteProps;
                        private SimpleDateFormat sdf;
            private GPXPanel mapPanel;              // RIGHT
            private GPXObject activeGPXObject;
            private Cursor mapCursor;
            private WaypointGroup wptGrpToDeleteFrom;
            private Waypoint wptToDelete;
            private boolean okToDeleteWpt;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GPXCreator window = new GPXCreator();
                    window.frame.setVisible(true);
                    window.frame.requestFocusInWindow();
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
        
        splitPaneMain.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updatePropTableWidths();
                    }
                });
            }
        });

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
        
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mapPanel.getAttribution().handleAttribution(e.getPoint(), true);
                }
            }
        });
        
        mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        mapPanel.setCursor(mapCursor);
        mapPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean cursorHand = mapPanel.getAttribution().handleAttributionCursor(e.getPoint());
                if (cursorHand) {
                    mapPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    mapPanel.setCursor(mapCursor);
                }
            }
        });
        mapPanel.setZoomContolsVisible(false);
        
        /* ------------------------------------------ SIDEBAR SPLIT PANE ------------------------------------------- */
        splitPaneSidebar = new JSplitPane();
        splitPaneSidebar.setMinimumSize(new Dimension(240, 25));
        splitPaneSidebar.setPreferredSize(new Dimension(240, 25));
        splitPaneSidebar.setContinuousLayout(true);
        splitPaneSidebar.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneMain.setLeftComponent(splitPaneSidebar);
        splitPaneSidebar.setDividerLocation(210);
        
        /* -------------------------------------- LEFT SIDEBAR TOP CONTAINER --------------------------------------- */
        containerLeftSidebarTop = new JPanel();
        containerLeftSidebarTop.setPreferredSize(new Dimension(10, 100));
        containerLeftSidebarTop.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebarTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebarTop.setLayout(new BoxLayout(containerLeftSidebarTop, BoxLayout.Y_AXIS));
        splitPaneSidebar.setTopComponent(containerLeftSidebarTop);
        
        /* --------------------------------------- ROUTES HEADING CONTAINER ---------------------------------------- */
        containerRoutesHeading = new JPanel();
        containerRoutesHeading.setPreferredSize(new Dimension(10, 23));
        containerRoutesHeading.setMinimumSize(new Dimension(10, 23));
        containerRoutesHeading.setMaximumSize(new Dimension(32767, 23));
        containerRoutesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        containerRoutesHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerRoutesHeading.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        containerRoutesHeading.setLayout(new BoxLayout(containerRoutesHeading, BoxLayout.Y_AXIS));
        containerRoutesHeading.setBorder(new CompoundBorder(
                new MatteBorder(0, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebarTop.add(containerRoutesHeading);
        
        /* -------------------------------------------- ROUTES HEADING --------------------------------------------- */
        labelRoutesHeading = new JLabel("Explorer");
        labelRoutesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesHeading.setMaximumSize(new Dimension(32767, 14));
        labelRoutesHeading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutesHeading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutesHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        containerRoutesHeading.add(labelRoutesHeading);
        labelRoutesSubheading = new JLabel("");
        labelRoutesSubheading.setBackground(Color.BLUE);
        labelRoutesSubheading.setMinimumSize(new Dimension(100, 14));
        labelRoutesSubheading.setPreferredSize(new Dimension(100, 14));
        labelRoutesSubheading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesSubheading.setMaximumSize(new Dimension(32767, 14));
        labelRoutesSubheading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutesSubheading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutesSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        containerRoutesHeading.add(labelRoutesSubheading);
        
        /* ------------------------------------------- ROUTE TREE/MODEL -------------------------------------------- */
        root = new DefaultMutableTreeNode("GPX Files");
        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new GPXTreeRenderer());
        tree.setCellEditor(new GPXTreeEditor());
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.setBackground(Color.white);
        
        // set selected object active in map panel
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode currentlySelected =
                        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (currentlySelected != null) {
                    setActiveGPXObject((GPXObject) currentlySelected.getUserObject());
                }
            }
        });
        
        ImageIcon collapsed = new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/tree-collapsed.png"));
        ImageIcon expanded = new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/tree-expanded.png"));
        UIManager.put("Tree.collapsedIcon", collapsed);
        UIManager.put("Tree.expandedIcon", expanded);
        
        // give Java look and feel to tree only (to get rid of dotted line handles/connectors)
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            SwingUtilities.updateComponentTreeUI(tree);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // necessary hack if bold selection style is used in GPXTreeComponentFactory (keeps labels sized correctly)
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode currentlySelected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                treeModel.nodeChanged(currentlySelected);
                if (previouslySelected != null) {
                    treeModel.nodeChanged(previouslySelected);
                }
                previouslySelected = currentlySelected;
                if (currentlySelected != null) {
                    setActiveGPXObject((GPXObject) currentlySelected.getUserObject());
                }
            }
        });
        
        treeModel.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeStructureChanged(TreeModelEvent e) {}
            @Override
            public void treeNodesRemoved(TreeModelEvent e) {}
            @Override
            public void treeNodesInserted(TreeModelEvent e) {}
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                mapPanel.repaint();
            }
        });
        
        /* ----------------------------------------- ROUTE TREE SCROLLPANE ----------------------------------------- */
        scrollPaneRoutes = new JScrollPane(tree);
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
        containerRouteProps.setMaximumSize(new Dimension(32767, 23));
        containerRouteProps.setMinimumSize(new Dimension(10, 23));
        containerRouteProps.setPreferredSize(new Dimension(10, 23));
        containerRouteProps.setAlignmentY(Component.TOP_ALIGNMENT);
        containerRouteProps.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerRouteProps.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        containerRouteProps.setLayout(new BoxLayout(containerRouteProps, BoxLayout.Y_AXIS));
        containerRouteProps.setBorder(new CompoundBorder(
                new MatteBorder(0, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebarBottom.add(containerRouteProps);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        
        /* --------------------------------------- ROUTE PROPERTIES HEADING ---------------------------------------- */
        labelRoutePropsHeading = new JLabel("Properties");
        labelRoutePropsHeading.setMaximumSize(new Dimension(32767, 14));
        labelRoutePropsHeading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelRoutePropsHeading.setHorizontalAlignment(SwingConstants.LEFT);
        labelRoutePropsHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelRoutePropsHeading.setAlignmentY(0.0f);
        containerRouteProps.add(labelRoutePropsHeading);
        
        /* ------------------------------------- ROUTE PROPERTIES TABLE/MODEL -------------------------------------- */
        tableModelRouteProps = new DefaultTableModel(new Object[]{"Name", "Value"},0);
        tableRouteProps = new JTable(tableModelRouteProps);
        tableRouteProps.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tableRouteProps.setAlignmentY(Component.TOP_ALIGNMENT);
        tableRouteProps.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableRouteProps.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableRouteProps.setFillsViewportHeight(true);
        tableRouteProps.setTableHeader(null);
        tableRouteProps.setEnabled(false);
        tableRouteProps.getColumn("Name").setPreferredWidth(100);
        tableRouteProps.getColumn("Name").setMinWidth(100);
        tableRouteProps.getColumn("Name").setMaxWidth(100);
        
        tableRouteProps.getColumnModel().setColumnMargin(0);
        
        /* -------------------------------------- ROUTE PROPERTIES SCROLLPANE -------------------------------------- */

        scrollPaneRouteProps = new JScrollPane(tableRouteProps);
        scrollPaneRouteProps.setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPaneRouteProps.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPaneRouteProps.setBorder(new LineBorder(new Color(0, 0, 0)));
        containerLeftSidebarBottom.add(scrollPaneRouteProps);
        
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
        btnRouteNew.setToolTipText("<html>Create new GPX route<br>[CTRL+N]</html>");
        btnRouteNew.setFocusable(false);
        btnRouteNew.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-new.png")));
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
        chooserFileOpen = new JFileChooser();              // TODO change dir before deployment
        chooserFileOpen.setCurrentDirectory(new File("C:\\eclipse\\workspace\\GPXCreator\\IO"));
        chooserFileOpen.addChoosableFileFilter(gpxFilter);
        chooserFileOpen.setFileFilter(gpxFilter);
        btnFileOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fileOpen();
            }
        });
        btnFileOpen.setToolTipText("<html>Open GPX file<br>[CTRL+O]</html>");
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
        chooserFileSave = new JFileChooser();              // TODO change dir before deployment
        chooserFileSave.setCurrentDirectory(new File("C:\\eclipse\\workspace\\GPXCreator\\IO"));
        chooserFileSave.addChoosableFileFilter(gpxFilter);
        chooserFileSave.setFileFilter(gpxFilter);
        btnFileSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fileSave();
            }
        });
        btnFileSave.setToolTipText("<html>Save selected GPX file<br>[CTRL+S]</html>");
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
                deleteActiveGPXObject();
            }
        });
        btnRouteDelete.setToolTipText("<html>Delete selected object<br>[CTRL+D]</html>");
        btnRouteDelete.setFocusable(false);
        btnRouteDelete.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-delete.png")));
        String ctrlDelete = "CTRL+D";
        mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK), ctrlDelete);
        mapPanel.getActionMap().put(ctrlDelete, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteActiveGPXObject();
            }
        });
        toolBarMain.add(btnRouteDelete);
        
        toolBarMain.addSeparator();
        
        /* ---------------------------------------- EDIT PROPERTIES BUTTON ----------------------------------------- */
        btnEditRouteProperties = new JButton("");
        btnEditRouteProperties.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // need a new method
            }
        });
        btnEditRouteProperties.setToolTipText("Edit route properties");
        btnEditRouteProperties.setFocusable(false);
        btnEditRouteProperties.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/edit-properties.png")));
        toolBarMain.add(btnEditRouteProperties);
        
        /* ------------------------------------------- ADD POINTS BUTTON ------------------------------------------- */
        btnEditRouteAddPoints = new JToggleButton("");
        btnEditRouteAddPoints.setToolTipText("Add points");
        btnEditRouteAddPoints.setFocusable(false);
        btnEditRouteAddPoints.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/edit-route-add-points.png")));
        toolBarMain.add(btnEditRouteAddPoints);
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (btnEditRouteAddPoints.isSelected() && activeGPXObject != null
                        && mapPanel.getCursor().getType() == Cursor.CROSSHAIR_CURSOR) {
                    int zoom = mapPanel.getZoom();
                    int x = e.getX();
                    int y = e.getY();
                    Point mapCenter = mapPanel.getCenter();
                    int xStart = mapCenter.x - mapPanel.getWidth() / 2;
                    int yStart = mapCenter.y - mapPanel.getHeight() / 2;
                    double lat = OsmMercator.YToLat(yStart + y, zoom);
                    double lon = OsmMercator.XToLon(xStart + x, zoom);
                    Waypoint wpt = new Waypoint(lat, lon);
                    
                    if (activeGPXObject.getClass().equals(GPXFile.class)) {
                        Route route = ((GPXFile) activeGPXObject).getRoutes().get(0);
                        route.getPath().addWaypoint(wpt, false);
                    } else if (activeGPXObject.getClass().equals(Route.class)) {
                        Route route = (Route) activeGPXObject;
                        route.getPath().addWaypoint(wpt, false);
                    } else if (activeGPXObject.getClass().equals(WaypointGroup.class)
                            && ((WaypointGroup) activeGPXObject).getWptGrpType() == WptGrpType.WAYPOINTS) {
                        WaypointGroup wptGrp = (WaypointGroup) activeGPXObject;
                        wptGrp.addWaypoint(wpt, false);
                    }
                    DefaultMutableTreeNode currentlySelected =
                            (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    while (!currentlySelected.getUserObject().getClass().equals(GPXFile.class)) {
                        currentlySelected = (DefaultMutableTreeNode) currentlySelected.getParent();
                    }
                    Object gpxFileObject = currentlySelected.getUserObject();
                    GPXFile gpxFile = (GPXFile) gpxFileObject;
                    gpxFile.updateAllProperties();
                    
                    mapPanel.repaint();
                    resetRoutePropsTable();
                }
            }
        });
        btnEditRouteAddPoints.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (btnEditRouteDelPoints.isSelected()) {
                        btnEditRouteDelPoints.setSelected(false);
                    }
                    if (btnLatLonFocus.isSelected()) {
                        btnLatLonFocus.setSelected(false);
                    }
                    if (activeGPXObject == null) {
                        JOptionPane.showMessageDialog(frame,
                                "Select a route before adding points.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        btnEditRouteAddPoints.setSelected(false);
                        return;
                    }
                    if (activeGPXObject.getClass().equals(GPXFile.class)) {
                        if (((GPXFile) activeGPXObject).getRoutes().size() == 0) {
                            Route route = ((GPXFile) activeGPXObject).addRoute();
                            DefaultMutableTreeNode selected =
                                    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();                            
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(route);
                            selected.add(newNode);
                            int[] ints = {selected.getIndex(newNode)};
                            treeModel.nodesWereInserted(selected, ints);
                        } else if (((GPXFile) activeGPXObject).getRoutes().size() > 1) {
                            JOptionPane.showMessageDialog(frame,
                                    "<html>There are multiple routes in the selected file. Select a single<br>" +
                                    "route or waypoint group before attempting to add points.</html>",
                                    "Warning",
                                    JOptionPane.WARNING_MESSAGE);
                            btnEditRouteAddPoints.setSelected(false);
                        }
                    } else if (activeGPXObject.getClass().equals(Track.class)
                            || (activeGPXObject.getClass().equals(WaypointGroup.class)
                                && ((WaypointGroup) activeGPXObject).getWptGrpType() == WptGrpType.TRACKSEG)) {
                        JOptionPane.showMessageDialog(frame,
                                "<html>GPX Creator cannot add points to a track or track segment.<br>" +
                                "A GPS logger should be used instead.<br></html>",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        btnEditRouteAddPoints.setSelected(false);
                    }
                    mapCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
                } else {
                    mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                }
            }
        });
        
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                btnEditRouteAddPoints.setSelected(false);
                btnEditRouteDelPoints.setSelected(false);
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
                    if (btnLatLonFocus.isSelected()) {
                        btnLatLonFocus.setSelected(false);
                    }
                    if (activeGPXObject != null) {
                        WaypointGroup dummy = getActiveWptGrp();
                        if (dummy == null) {
                            btnEditRouteDelPoints.setSelected(false);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                "Select a route, track segment, or group of waypoints first.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        btnEditRouteDelPoints.setSelected(false);
                    }
                    mapCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
                } else {
                    mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                }
            }
        });
        
        mapPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMapDeleteSymbol(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                wptGrpToDeleteFrom = null;
                wptToDelete = null;
                okToDeleteWpt = false;
                mapPanel.setShownPoint(null);
                mapPanel.repaint();
            }
        });
        
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                wptGrpToDeleteFrom = null;
                wptToDelete = null;
                okToDeleteWpt = false;
                mapPanel.setShownPoint(null);
                mapPanel.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (okToDeleteWpt && wptToDelete != null && wptGrpToDeleteFrom != null) {
                    wptGrpToDeleteFrom.removeWaypoint(wptToDelete);
                    wptGrpToDeleteFrom = null;
                    wptToDelete = null;
                    okToDeleteWpt = false;
                    mapPanel.setShownPoint(null);
                    mapPanel.repaint();
                    
                    DefaultMutableTreeNode currentlySelected =
                            (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    while (!currentlySelected.getUserObject().getClass().equals(GPXFile.class)) {
                        currentlySelected = (DefaultMutableTreeNode) currentlySelected.getParent();
                    }
                    Object gpxFileObject = currentlySelected.getUserObject();
                    GPXFile gpxFile = (GPXFile) gpxFileObject;
                    gpxFile.updateAllProperties();
                    resetRoutePropsTable();
                    
                    updateMapDeleteSymbol(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateMapDeleteSymbol(e);
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
                if (activeGPXObject != null) {
                    WaypointGroup wptGrp = getActiveWptGrp();
                    if (wptGrp != null) {
                        EleCorrectedStatus corrected = wptGrp.correctElevation(true);
                        if (corrected == EleCorrectedStatus.FAILED) {
                            JOptionPane.showMessageDialog(frame,
                                    "<html>There was a problem correcting the elevation.  Possible causes:<br>" +
                                    " - an empty set of points was submitted<br>" +
                                    " - the route/track submitted was too long (limit of ~150 miles)<br>" +
                                    " - the response from the server contained errors or was empty</html>",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (corrected == EleCorrectedStatus.CORRECTED_WITH_CLEANSE) {
                            JOptionPane.showMessageDialog(frame,
                                    "<html>The elevation response from the server had missing data segments.<br>" +
                                    "These have been filled in by linear interpolation.</html>",
                                    "Information",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    resetRoutePropsTable();
                }
            }
        });
        toolBarMain.add(btnCorrectEle);
        
        /* ---------------------------------------- ELEVATION CHART BUTTON ----------------------------------------- */
        btnEleChart = new JButton("");
        btnEleChart.setToolTipText("View elevation profile");
        btnEleChart.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/elevation-chart.png")));
        btnEleChart.setFocusable(false);
        btnEleChart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeGPXObject != null) {
                    WaypointGroup wptGrp = getActiveWptGrp();
                    if (wptGrp != null) {
                        DefaultMutableTreeNode currentlySelected =
                                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        while (!currentlySelected.getUserObject().getClass().equals(GPXFile.class)) {
                            currentlySelected = (DefaultMutableTreeNode) currentlySelected.getParent();
                        }
                        GPXFile gpxFile = (GPXFile) currentlySelected.getUserObject();
                        JFrame f = new ElevationChart("Elevation profile", gpxFile.getName(), wptGrp);
                        InputStream in = GPXCreator.class.getResourceAsStream(
                                "/com/gpxcreator/icons/elevation-chart.png");
                        if (in != null) {
                            try {
                                f.setIconImage(ImageIO.read(in));
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        f.setSize(frame.getWidth() - 150, frame.getHeight() - 100);
                        f.setLocationRelativeTo(frame);
                        f.setVisible(true);
                    }
                }
            }
        });
        toolBarMain.add(btnEleChart);
        
        /* ----------------------------------------- TILE SOURCE SELECTOR ------------------------------------------ */
        toolBarMain.add(Box.createHorizontalGlue());
        final TileSource openStreetMap = new OsmTileSource.Mapnik();
        final TileSource openCycleMap = new OsmTileSource.CycleMap(); 
        final TileSource bingAerial = new BingAerialTileSource();
        final TileSource mapQuestOsm = new MapQuestOsmTileSource();
        final TileSource mapQuestOpenAerial = new MapQuestOpenAerialTileSource();
        final TileSource googleMaps = new TemplatedTMSTileSource(
                "Google Maps",
                "http://mt{switch:0,1,2,3}.google.com/vt/lyrs=m&x={x}&y={y}&z={zoom}", 22);
        final TileSource googleSat = new TemplatedTMSTileSource(
                "Google Satellite",
                "http://mt{switch:0,1,2,3}.google.com/vt/lyrs=s&x={x}&y={y}&z={zoom}", 21);
        final TileSource googleSatMap = new TemplatedTMSTileSource(
                "Google Satellite + Labels",
                "http://mt{switch:0,1,2,3}.google.com/vt/lyrs=y&x={x}&y={y}&z={zoom}", 21);
        final TileSource googleTerrain = new TemplatedTMSTileSource(
                "Google Terrain",
                "http://mt{switch:0,1,2,3}.google.com/vt/lyrs=p&x={x}&y={y}&z={zoom}", 15);
        final TileSource esriTopoUSA = new TemplatedTMSTileSource(
                "Esri Topo USA",
                "http://server.arcgisonline.com/ArcGIS/rest/services/" +
                "USA_Topo_Maps/MapServer/tile/{zoom}/{y}/{x}.jpg", 15);
        final TileSource esriTopoWorld = new TemplatedTMSTileSource(
                "Esri Topo World",
                "http://server.arcgisonline.com/ArcGIS/rest/services/" +
                "World_Topo_Map/MapServer/tile/{zoom}/{y}/{x}.jpg", 19);
        
        comboBoxTileSource = new JComboBox<String>();
        comboBoxTileSource.setMaximumRowCount(18);
        comboBoxTileSource.addItem("OpenStreetMap");
        comboBoxTileSource.addItem("OpenCycleMap");
        comboBoxTileSource.addItem("Bing Aerial");
        comboBoxTileSource.addItem("MapQuest-OSM");
        comboBoxTileSource.addItem("MapQuest Open Aerial");
        comboBoxTileSource.addItem("Google Maps");
        comboBoxTileSource.addItem("Google Satellite");
        comboBoxTileSource.addItem("Google Satellite + Labels");
        comboBoxTileSource.addItem("Google Terrain");
        comboBoxTileSource.addItem("Esri Topo USA");
        comboBoxTileSource.addItem("Esri Topo World");
        
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
                    case "Google Maps":
                        mapPanel.setTileSource(googleMaps);
                        break;
                    case "Google Satellite":
                        mapPanel.setTileSource(googleSat);
                        break;
                    case "Google Satellite + Labels":
                        mapPanel.setTileSource(googleSatMap);
                        break;
                    case "Google Terrain":
                        mapPanel.setTileSource(googleTerrain);
                        break;
                    case "Esri Topo USA":
                        mapPanel.setTileSource(esriTopoUSA);
                        break;
                    case "Esri Topo World":
                        mapPanel.setTileSource(esriTopoWorld);
                        break;
                }
            }
        });
        
        comboBoxTileSource.setFocusable(false);
        comboBoxTileSource.setPreferredSize(new Dimension(150, 24));
        comboBoxTileSource.setMinimumSize(new Dimension(50, 24));
        comboBoxTileSource.setAlignmentX(Component.RIGHT_ALIGNMENT);
        comboBoxTileSource.setMaximumSize(new Dimension(20, 24));
        toolBarMain.add(comboBoxTileSource);
        
        /* ----------------------------------------- LAT/LON INPUT/SEEKER ------------------------------------------ */
        toolBarMain.addSeparator();
        
        lblLat = new JLabel(" Lat ");
        lblLat.setFont(new Font("Tahoma", Font.PLAIN, 11));
        toolBarMain.add(lblLat);
        
        textFieldLat = new JTextField();
        textFieldLat.setPreferredSize(new Dimension(80, 24));
        textFieldLat.setMinimumSize(new Dimension(25, 24));
        textFieldLat.setMaximumSize(new Dimension(80, 24));
        textFieldLat.setColumns(9);
        textFieldLat.setFocusable(false);
        textFieldLat.setFocusTraversalKeysEnabled(false);
        textFieldLat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    textFieldLat.setFocusable(false);
                    textFieldLon.setFocusable(true);
                    textFieldLon.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnLatLonFocus.setSelected(false);
                    btnLatLonFocus.setSelected(true);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    btnLatLonFocus.setSelected(false);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if (btnLatLonFocus.isSelected()) {
                    btnLatLonFocus.setSelected(false);
                    btnLatLonFocus.setSelected(true);
                }
            }
        });
        toolBarMain.add(textFieldLat);
        
        lblLon = new JLabel(" Lon ");
        lblLon.setFont(new Font("Tahoma", Font.PLAIN, 11));
        toolBarMain.add(lblLon);
        
        textFieldLon = new JTextField();
        textFieldLon.setPreferredSize(new Dimension(80, 24));
        textFieldLon.setMinimumSize(new Dimension(25, 24));
        textFieldLon.setMaximumSize(new Dimension(80, 24));
        textFieldLon.setColumns(9);
        textFieldLon.setFocusable(false);
        textFieldLon.setFocusTraversalKeysEnabled(false);
        textFieldLon.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    textFieldLat.setFocusable(true);
                    textFieldLon.setFocusable(false);
                    textFieldLat.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnLatLonFocus.setSelected(false);
                    btnLatLonFocus.setSelected(true);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    btnLatLonFocus.setSelected(false);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if (btnLatLonFocus.isSelected()) {
                    btnLatLonFocus.setSelected(false);
                    btnLatLonFocus.setSelected(true);
                }
            }
        });
        toolBarMain.add(textFieldLon);
        
        long eventMask = AWTEvent.MOUSE_EVENT_MASK;  
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {  
            public void eventDispatched(AWTEvent e) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    if (e.getSource() == (Object) textFieldLat) {
                        textFieldLat.setFocusable(true);
                    } else {
                        textFieldLat.setFocusable(false);
                    }
                    if (e.getSource() == (Object) textFieldLon) {
                        textFieldLon.setFocusable(true);
                    } else {
                        textFieldLon.setFocusable(false);
                    }
                }
            }
        }, eventMask);
        
        btnLatLonFocus = new JToggleButton("");
        btnLatLonFocus.setToolTipText("Focus on latitude/longitude");
        btnLatLonFocus.setIcon(new ImageIcon(
                GPXCreator.class.getResource("/com/gpxcreator/icons/crosshair.png")));
        btnLatLonFocus.setFocusable(false);
        btnLatLonFocus.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (btnEditRouteAddPoints.isSelected()) {
                        btnEditRouteAddPoints.setSelected(false);
                    }
                    if (btnEditRouteDelPoints.isSelected()) {
                        btnEditRouteDelPoints.setSelected(false);
                    }
                    
                    mapCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
                    String latString = textFieldLat.getText();
                    String lonString = textFieldLon.getText();
                    try {
                        double latDouble = Double.parseDouble(latString);
                        double lonDouble = Double.parseDouble(lonString);
                        mapPanel.setShowCrosshair(true);
                        mapPanel.setCrosshairLat(latDouble);
                        mapPanel.setCrosshairLon(lonDouble);
                        Point p = new Point(mapPanel.getWidth() / 2, mapPanel.getHeight() / 2); 
                        mapPanel.setDisplayPositionByLatLon(p, latDouble, lonDouble, mapPanel.getZoom());
                    } catch (Exception e1) {
                        // nothing
                    }
                    mapPanel.repaint();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                    mapPanel.setShowCrosshair(false);
                    mapPanel.repaint();
                }
            }
        });
        
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (btnLatLonFocus.isSelected()) {
                    int zoom = mapPanel.getZoom();
                    int x = e.getX();
                    int y = e.getY();
                    Point mapCenter = mapPanel.getCenter();
                    int xStart = mapCenter.x - mapPanel.getWidth() / 2;
                    int yStart = mapCenter.y - mapPanel.getHeight() / 2;
                    double lat = OsmMercator.YToLat(yStart + y, zoom);
                    double lon = OsmMercator.XToLon(xStart + x, zoom);
                    textFieldLat.setText(String.format("%.6f", lat));
                    textFieldLon.setText(String.format("%.6f", lon));
                    mapPanel.setShowCrosshair(true);
                    mapPanel.setCrosshairLat(lat);
                    mapPanel.setCrosshairLon(lon);
                    mapPanel.repaint();
                }
            }
        });
        
        Component horizontalGlue = Box.createHorizontalGlue();
        horizontalGlue.setPreferredSize(new Dimension(2, 0));
        horizontalGlue.setMinimumSize(new Dimension(2, 0));
        horizontalGlue.setMaximumSize(new Dimension(2, 0));
        toolBarMain.add(horizontalGlue);
        toolBarMain.add(btnLatLonFocus);
        
        /* --------------------------------------------------------------------------------------------------------- */
        
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
            GPXFile gpxFile = new GPXFile(name);
            gpxFile.addRoute();

            mapPanel.addGPXFile(gpxFile);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(gpxFile);
            root.add(node);
            node.add(new DefaultMutableTreeNode(gpxFile.getRoutes().get(0)));
            
            int[] ints = {root.getIndex(node)};
            treeModel.nodesWereInserted(root, ints);
            setActiveGPXObject((GPXObject) gpxFile);
            TreeNode[] nodes = treeModel.getPathToRoot(node);
            tree.setSelectionPath(new TreePath(nodes));
            
            tree.scrollRectToVisible(new Rectangle(0, 999999999, 1, 1));
        }
    }
    
    public void fileOpen() {
        chooserFileOpen.setSize(mapPanel.getWidth(), mapPanel.getHeight());
        int returnVal = chooserFileOpen.showOpenDialog(mapPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileOpened = chooserFileOpen.getSelectedFile();
            boolean valid = GPXFile.validateGPXFile(fileOpened);
            // TODO re-enable validation warnings
            if (!valid) {
                /*JOptionPane.showMessageDialog(frame,
                        "<html>The selected file does not validate against the GPX schema version 1.1.<br>" +
                        "There is a chance that the file will not load properly.</html>",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);*/
            }
            GPXFile file = new GPXFile(fileOpened);
            mapPanel.addGPXFile(file);
            
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(file); 
            ((DefaultMutableTreeNode)treeModel.getRoot()).add(node);
            if (file.getWaypointGroup().getWaypoints().size() > 0) {
                DefaultMutableTreeNode wpts = new DefaultMutableTreeNode(file.getWaypointGroup());
                node.add(wpts);
            }
            for (Route route : file.getRoutes()) {
                DefaultMutableTreeNode rte = new DefaultMutableTreeNode(route);
                node.add(rte);
            }
            for (Track track : file.getTracks()) {
                DefaultMutableTreeNode trk = new DefaultMutableTreeNode(track);
                node.add(trk);
                for (WaypointGroup trackseg : track.getTracksegs()) {
                    DefaultMutableTreeNode trkseg = new DefaultMutableTreeNode(trackseg);
                    trk.add(trkseg);
                }
            }
            int[] ints = {root.getIndex(node)};
            treeModel.nodesWereInserted(root, ints);
            setActiveGPXObject((GPXObject) file);
            TreeNode[] nodes = treeModel.getPathToRoot(node);
            tree.setSelectionPath(new TreePath(nodes));
            
            tree.scrollRectToVisible(new Rectangle(0, 999999999, 1, 1));
        }
    }
    
    public void fileSave() {
        DefaultMutableTreeNode currentlySelected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        while (!currentlySelected.getUserObject().getClass().equals(GPXFile.class)) {
            currentlySelected = (DefaultMutableTreeNode) currentlySelected.getParent();
        }
        TreeNode[] nodes = treeModel.getPathToRoot(currentlySelected);
        tree.setSelectionPath(new TreePath(nodes));
        
        int returnVal = chooserFileSave.showSaveDialog(mapPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileSave = chooserFileSave.getSelectedFile();
            ((GPXFile) activeGPXObject).saveToGPXFile(fileSave);
        }
    }
    
    public void deleteActiveGPXObject() {
        if (activeGPXObject != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();
            TreeNode[] parentPath = treeModel.getPathToRoot(parentNode);
            Object parentObject = parentNode.getUserObject();
            
            treeModel.removeNodeFromParent(currentNode);
            treeModel.nodesWereRemoved(parentNode, null, null);
            
            if (activeGPXObject.getClass().equals(GPXFile.class)) { // this is a GPX file
                mapPanel.removeGPXFile((GPXFile) activeGPXObject);
                activeGPXObject = null;
                clearRoutePropsTable();
            } else {
                if (activeGPXObject.getClass().equals(Route.class)) { // this is a route
                    ((GPXFile) parentObject).getRoutes().remove((Route) activeGPXObject);
                } else if (activeGPXObject.getClass().equals(Track.class)) { // this is a track
                    ((GPXFile) parentObject).getTracks().remove((Track) activeGPXObject);
                } else if (activeGPXObject.getClass().equals(WaypointGroup.class)) {
                    if (((WaypointGroup) currentNode.getUserObject()).getWptGrpType() == WptGrpType.TRACKSEG) { // track seg
                        ((Track) parentObject).getTracksegs().remove((WaypointGroup) currentNode.getUserObject());
                    } else { // this is a top-level waypoint group
                        ((GPXFile) parentObject).getWaypointGroup().getWaypoints().clear();
                    }
                }
                tree.setSelectionPath(new TreePath(parentPath));
            } 
            mapPanel.repaint();
        }
    }
    
    public GPXObject getActiveGPXObject() {
        return activeGPXObject;
    }
    
    public void setActiveGPXObject(GPXObject gpxObject) {
        btnLatLonFocus.setSelected(false);
        activeGPXObject = gpxObject;
        gpxObject.setVisible(true);
        mapPanel.fitGPXObjectToPanel(gpxObject);
        resetRoutePropsTable();
    }
    
    public void resetRoutePropsTable() {
        tableModelRouteProps.setRowCount(0);
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (activeGPXObject.getClass().equals(GPXFile.class)) { // this is a GPX file
            GPXFile gpxFile = (GPXFile) activeGPXObject;
            if (gpxFile.getRoutes().size() == 1 && gpxFile.getTracks().size() == 0) { // display single route details
                tableModelRouteProps.addRow(new Object[]{"GPX name", gpxFile.getName()});
                if (!gpxFile.getDesc().equals("")) {
                    tableModelRouteProps.addRow(new Object[]{"GPX desc", gpxFile.getDesc()});
                }
                String timeString = "";
                if (gpxFile.getTime() != null) {
                    Date time = gpxFile.getTime();
                    timeString = sdf.format(time);
                }
                tableModelRouteProps.addRow(new Object[]{"GPX time", timeString});
                
                Route rte = gpxFile.getRoutes().get(0);
                propsDisplayRoute(rte);
            } else if (gpxFile.getRoutes().size() == 0 && gpxFile.getTracks().size() == 1
                    && gpxFile.getTracks().get(0).getTracksegs().size() == 1) { // display single track details
                tableModelRouteProps.addRow(new Object[]{"GPX name", gpxFile.getName()});
                if (!gpxFile.getDesc().equals("")) {
                    tableModelRouteProps.addRow(new Object[]{"GPX desc", gpxFile.getDesc()});
                }
                String timeString = "";
                if (gpxFile.getTime() != null) {
                    Date time = gpxFile.getTime();
                    timeString = sdf.format(time);
                }
                tableModelRouteProps.addRow(new Object[]{"GPX time", timeString});
                
                Track trk = gpxFile.getTracks().get(0);
                WaypointGroup trkpts = trk.getTracksegs().get(0);
                propsDisplayTrackseg(trk, trkpts);
            } else { // display file top-level container info
                tableModelRouteProps.addRow(new Object[]{"GPX name", gpxFile.getName()});
                if (!gpxFile.getDesc().equals("")) {
                    tableModelRouteProps.addRow(new Object[]{"GPX desc", gpxFile.getDesc()});
                }
                String timeString = "";
                if (gpxFile.getTime() != null) {
                    Date time = gpxFile.getTime();
                    timeString = sdf.format(time);
                }
                tableModelRouteProps.addRow(new Object[]{"GPX time", timeString});
                tableModelRouteProps.addRow(new Object[]{"waypoints",
                        gpxFile.getWaypointGroup().getWaypoints().size()});
                tableModelRouteProps.addRow(new Object[]{"routes", gpxFile.getRoutes().size()});
                tableModelRouteProps.addRow(new Object[]{"tracks", gpxFile.getTracks().size()});
            }
            
        } else { // this is not a GPX file
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();
            Object parentObject = parentNode.getUserObject();
            
            if (activeGPXObject.getClass().equals(Route.class)) { /// this is a route
                Route rte = (Route) activeGPXObject;
                propsDisplayRoute(rte);
            } else if (activeGPXObject.getClass().equals(Track.class)) { // this is a track
                Track trk = (Track) activeGPXObject;
                if (trk.getTracksegs().size() == 1) { // display single trackseg details
                    WaypointGroup trkpts = trk.getTracksegs().get(0);
                    propsDisplayTrackseg(trk, trkpts);
                } else { // display track container info
                    if (!trk.getName().equals("")) {
                        tableModelRouteProps.addRow(new Object[]{"track name", trk.getName()});
                    }
                    if (!trk.getDesc().equals("")) {
                        tableModelRouteProps.addRow(new Object[]{"track desc", trk.getDesc()});
                    }
                    if (trk.getNumber() != 0) {
                        tableModelRouteProps.addRow(new Object[]{"track number", trk.getNumber()});
                    }
                    if (!trk.getType().equals("")) {
                        tableModelRouteProps.addRow(new Object[]{"track type", trk.getType()});
                    }
                    tableModelRouteProps.addRow(new Object[]{"segments", trk.getTracksegs().size()});
                }
            } else if (activeGPXObject.getClass().equals(WaypointGroup.class)) {
                WaypointGroup wptGrp = (WaypointGroup) activeGPXObject;
                if (wptGrp.getWptGrpType() == WptGrpType.WAYPOINTS) { // this is a top level waypoint collection
                    tableModelRouteProps.addRow(new Object[]{"waypoints", wptGrp.getWaypoints().size()});
                    tableModelRouteProps.addRow(
                            new Object[]{"min elevation", String.format("%.0f ft", wptGrp.getEleMinFeet())});
                    tableModelRouteProps.addRow(
                            new Object[]{"max elevation", String.format("%.0f ft", wptGrp.getEleMaxFeet())});
                    
                } else if (wptGrp.getWptGrpType() == WptGrpType.TRACKSEG) { // this is a trackseg
                    Track trk = (Track) parentObject;
                    propsDisplayTrackseg(trk, wptGrp);
                }
            }
        }
        updatePropTableWidths();
    }
    
    public void propsDisplayRoute(Route rte) {
        if (!rte.getName().equals("")) {
            tableModelRouteProps.addRow(new Object[]{"route name", rte.getName()});
        }
        if (!rte.getDesc().equals("")) {
            tableModelRouteProps.addRow(new Object[]{"route desc", rte.getDesc()});
        }
        if (rte.getNumber() != 0) {
            tableModelRouteProps.addRow(new Object[]{"route number", rte.getNumber()});
        }
        if (!rte.getType().equals("")) {
            tableModelRouteProps.addRow(new Object[]{"route type", rte.getType()});
        }
        WaypointGroup rtepts = rte.getPath();
        propsDisplayPathDetails(rtepts);
    }
    
    public void propsDisplayTrackseg(Track trk, WaypointGroup trkpts) {
        if (!trk.getName().equals("")) {
            tableModelRouteProps.addRow(new Object[]{"track name", trk.getName()});
        }
        if (!trk.getDesc().equals("")) {
            tableModelRouteProps.addRow(new Object[]{"track desc", trk.getDesc()});
        }
        if (trk.getNumber() != 0) {
            tableModelRouteProps.addRow(new Object[]{"track number", trk.getNumber()});
        }
        if (!trk.getType().equals("")) {
            tableModelRouteProps.addRow(new Object[]{"track type", trk.getType()});
        }
        propsDisplayPathDetails(trkpts);
    }
    
    public void propsDisplayPathDetails(WaypointGroup path) {
        tableModelRouteProps.addRow(new Object[]{"# of pts", path.getNumPts()});
        if (path.getStart() != null && path.getEnd() != null) {
            Date startTimeDate = path.getStart().getTime();
            Date endTimeDate = path.getEnd().getTime();
            if (startTimeDate != null && endTimeDate != null) {
                String startTimeString = "";
                String endTimeString = "";
                startTimeString = sdf.format(startTimeDate);
                endTimeString = sdf.format(endTimeDate);
                tableModelRouteProps.addRow(new Object[]{"start time", startTimeString});
                tableModelRouteProps.addRow(new Object[]{"end time", endTimeString});
            }
        }
        long duration = path.getDuration();
        long hours = duration / 3600000;
        long minutes = (duration - hours * 3600000) / 60000;
        long seconds = (duration - hours * 3600000 - minutes * 60000) / 1000;
        if (duration != 0) {
            tableModelRouteProps.addRow(new Object[]{"duration", hours + "hr " + minutes + "min " + seconds + "sec"});
        }
        double lengthMiles = path.getLengthMiles();
        tableModelRouteProps.addRow(new Object[]{"length", String.format("%.2f mi", lengthMiles)});
        
        double avgSpeedMph = (lengthMiles / duration) * 3600000;
        if (Double.isNaN(avgSpeedMph) || Double.isInfinite(avgSpeedMph)) {
            avgSpeedMph = 0;
        }
        if (avgSpeedMph != 0) {
            tableModelRouteProps.addRow(new Object[]{"avg speed", String.format("%.1f mph", avgSpeedMph)});
        }
        if (path.getMaxSpeedMph() != 0) {
            tableModelRouteProps.addRow(new Object[]{"max speed", String.format("%.1f mph", path.getMaxSpeedMph())});            
        }
        
        tableModelRouteProps.addRow(
                new Object[]{"elevation (start)", String.format("%.0f ft", path.getEleStartFeet())});
        tableModelRouteProps.addRow(
                new Object[]{"elevation (end)", String.format("%.0f ft", path.getEleEndFeet())});
        tableModelRouteProps.addRow(
                new Object[]{"min elevation", String.format("%.0f ft", path.getEleMinFeet())});
        tableModelRouteProps.addRow(
                new Object[]{"max elevation", String.format("%.0f ft", path.getEleMaxFeet())});
        double grossRiseFeet = path.getGrossRiseFeet();
        double grossFallFeet = path.getGrossFallFeet();
        tableModelRouteProps.addRow(new Object[]{"gross rise", String.format("%.0f ft", grossRiseFeet)});
        tableModelRouteProps.addRow(new Object[]{"gross fall", String.format("%.0f ft", grossFallFeet)});
        
        long riseTime = path.getRiseTime();
        hours = riseTime / 3600000;
        minutes = (riseTime - hours * 3600000) / 60000;
        seconds = (riseTime - hours * 3600000 - minutes * 60000) / 1000;
        if (riseTime != 0) {
            tableModelRouteProps.addRow(new Object[]{"rise time", hours + "hr " + minutes + "min " + seconds + "sec"});
        }
        long fallTime = path.getFallTime();
        hours = fallTime / 3600000;
        minutes = (fallTime - hours * 3600000) / 60000;
        seconds = (fallTime - hours * 3600000 - minutes * 60000) / 1000;
        if (fallTime != 0) {
            tableModelRouteProps.addRow(new Object[]{"fall time", hours + "hr " + minutes + "min " + seconds + "sec"});
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
        }
        if (avgFallSpeedFph != 0) {
            tableModelRouteProps.addRow(new Object[]{"avg fall speed", String.format("%.0f ft/hr", avgFallSpeedFph)});
        }
    }
    
    public void updatePropTableWidths() {
        int width = 0;
        for (int row = 0; row < tableRouteProps.getRowCount(); row++) {
            TableCellRenderer renderer = tableRouteProps.getCellRenderer(row, 1);
            Component comp = tableRouteProps.prepareRenderer(renderer, row, 1);
            width = Math.max (comp.getPreferredSize().width, width);
        }
        width += tableRouteProps.getIntercellSpacing().width;
        int tableWidth = width + 100;
        if (tableWidth > scrollPaneRouteProps.getWidth()) {
            tableRouteProps.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            tableRouteProps.getColumn("Value").setPreferredWidth(width);
            tableRouteProps.getColumn("Value").setMinWidth(width);
        } else {
            tableRouteProps.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        }
    }
    
    public void clearRoutePropsTable() {
        tableModelRouteProps.setRowCount(0);
    }
    
    public WaypointGroup getActiveWptGrp() {
        WaypointGroup wptGrp = null;
        boolean warn = false;
        if (activeGPXObject.getClass().equals(WaypointGroup.class)) {
            wptGrp = (WaypointGroup) activeGPXObject; 
        } else if (activeGPXObject.getClass().equals(Route.class)) {
            wptGrp = ((Route) activeGPXObject).getPath(); 
        } else if (activeGPXObject.getClass().equals(Track.class)) {
            Track trk = (Track) activeGPXObject;
            if (trk.getTracksegs().size() == 1) {
                wptGrp = trk.getTracksegs().get(0); 
            } else {
                warn = true;
            }
        } else if (activeGPXObject.getClass().equals(GPXFile.class)) {
            GPXFile gpxFile = (GPXFile) activeGPXObject;
            if (gpxFile.getRoutes().size() == 1 && gpxFile.getTracks().size() == 0) { // one route
                wptGrp = gpxFile.getRoutes().get(0).getPath(); 
            } else if (gpxFile.getRoutes().size() == 0 && gpxFile.getTracks().size() == 1) { // one track
                Track trk = gpxFile.getTracks().get(0);
                if (trk.getTracksegs().size() == 1) { // one trackseg
                    wptGrp = trk.getTracksegs().get(0);
                } else {
                    warn = true;
                }
            } else {
                warn = true;
            }
        } else {
            warn = true;
        }
        if (warn) {
            JOptionPane.showMessageDialog(frame,
                    "Select a route, track segment, or group of waypoints first.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
        return wptGrp;
    }
    
    private void updateMapDeleteSymbol(MouseEvent e) { // common function used by 2 mouse listeners 3 times
        if (btnEditRouteDelPoints.isSelected()) {
            wptGrpToDeleteFrom = getActiveWptGrp();
            wptToDelete = null;
            if (wptGrpToDeleteFrom != null) {
                Point p = e.getPoint();
                boolean found = false;
                okToDeleteWpt = false;
                double minDistance = Double.MAX_VALUE;
                for (Waypoint wpt : wptGrpToDeleteFrom.getWaypoints()) {
                    Point w = mapPanel.getMapPosition(wpt.getLat(), wpt.getLon(), false);
                    int dx = w.x - p.x;
                    int dy = w.y - p.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance < 10 && distance < minDistance) {
                        minDistance = distance;
                        wptToDelete = wpt;
                        mapPanel.setShownPoint(w);
                        found = true;
                    }
                }
                okToDeleteWpt = true;
                if (!found) {
                    wptGrpToDeleteFrom = null;
                    wptToDelete = null;
                    okToDeleteWpt = false;
                    mapPanel.setShownPoint(null);
                }
                mapPanel.repaint();
            } else {
                btnEditRouteDelPoints.setSelected(false);
            }
        }        
    }
}
