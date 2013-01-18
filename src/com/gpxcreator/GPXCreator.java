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
import com.gpxcreator.tablecellrenderers.RouteColorCellRenderer;
import com.gpxcreator.tablecellrenderers.RouteVisibleCellRenderer;

@SuppressWarnings("serial")
public class GPXCreator extends JComponent {
    
    // temporary ghetto indent style to show layout hierarchy
    private JFrame frame;
        private JToolBar toolBarMain;       // NORTH
            private JButton btnFileSave;
            private JFileChooser chooserFileSave;
            private File fileSave;
            private JButton btnFileOpen;
            private JFileChooser chooserFileOpen;
            private File fileOpened;
        private JSplitPane splitPaneMain;   // CENTER
            private JSplitPane splitPaneSidebar;    // LEFT
                private JPanel containerLeftSidebarTop;        // TOP
                    private JPanel containerRoutesHeading;
                        private JLabel labelRoutesHeading;
                        private JLabel labelRoutesSubheading;
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
        
        /* ------------------------------------------ SIDEBAR SPLIT PANE ------------------------------------------- */
        splitPaneSidebar = new JSplitPane();
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
        routeTableModel = new DefaultTableModel(new Object[]{"Visible", "Name", "Color"},0);
        tableRoutes = new JTable(routeTableModel);
        tableRoutes.setAlignmentY(Component.TOP_ALIGNMENT);
        tableRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableRoutes.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableRoutes.setFillsViewportHeight(true);
        
        tableRoutes.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        tableRoutes.setShowVerticalLines(false);
        tableRoutes.setTableHeader(null);
        tableRoutes.setEnabled(false); // TODO <-- this is only temporary... until table can be developed further!
        
        tableRoutes.getColumn("Visible").setCellRenderer(new RouteVisibleCellRenderer());
        tableRoutes.getColumn("Color").setCellRenderer(new RouteColorCellRenderer());

        tableRoutes.getColumn("Visible").setPreferredWidth(14);
        tableRoutes.getColumn("Visible").setMinWidth(14);
        tableRoutes.getColumn("Visible").setMaxWidth(14);
        tableRoutes.getColumn("Color").setPreferredWidth(14);
        tableRoutes.getColumn("Color").setMinWidth(14);
        tableRoutes.getColumn("Color").setMaxWidth(14);
        
        
        
        
        /* ----------------------------------------- ROUTE TABLE SCROLLPANE ---------------------------------------- */
        scrollPaneRoutes = new JScrollPane(tableRoutes);
        scrollPaneRoutes.setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPaneRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPaneRoutes.setBorder(new LineBorder(new Color(0, 0, 0)));
        containerLeftSidebarTop.add(scrollPaneRoutes);
        
        /* ------------------------------------ LEFT SIDEBAR BOTTOM CONTAINER -------------------------------------- */
        containerLeftSidebarBottom = new JPanel();
        containerLeftSidebarBottom.setMinimumSize(new Dimension(150, 10));
        containerLeftSidebarBottom.setPreferredSize(new Dimension(150, 10));
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
        
        
        
        

        
        
        //resizeRouteTableWidth();

        /*splitPaneSidebar.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent changeEvent) {
                JSplitPane sourceSplitPane = (JSplitPane)changeEvent.getSource();
                String propertyName = changeEvent.getPropertyName();
                if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
                    int current = sourceSplitPane.getDividerLocation();
                    System.out.println ("Current: " + current);
                    Integer last = (Integer)changeEvent.getNewValue();
                    System.out.println ("Last: " + last);
                    Integer priorLast = (Integer)changeEvent.getOldValue();
                    System.out.println ("Prior last: " + priorLast);
                }
                //resizeRouteTableWidth();
            }
        });*/

        
        
        // bogus event generator for quick testing of event handling
        /*mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int count = routeTableModel.getRowCount()+1;
                routeTableModel.addRow(new Object[]{"Route" + count});
                System.out.println("yah");
            }
        });*/
        
        /*java.util.Properties systemProperties = System.getProperties();
        systemProperties.setProperty("http.proxyHost", "proxy1.lmco.com");
        systemProperties.setProperty("http.proxyPort", "80");*/
    }
    
    public void fileOpen() {
        int returnVal = chooserFileOpen.showOpenDialog(mapPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileOpened = chooserFileOpen.getSelectedFile();
            Route route = new Route(fileOpened);
            mapPanel.addRoute(route);
            routeTableModel.addRow(new Object[]{route.isVisible(), route.getName(), route.getColor()});
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
    
    /*public void resizeRouteTableWidth() {
        int width = 0;
        for (int row = 0; row < tableRoutes.getRowCount(); row++) {
            TableCellRenderer renderer = tableRoutes.getCellRenderer(row, 0);
            Component comp = tableRoutes.prepareRenderer(renderer, row, 0);
            width = Math.max (comp.getPreferredSize().width, width);
        }
        width = Math.max(width, scrollPaneRoutes.getWidth());
        width -= 10;
        System.out.println(width);
    }*/
}
