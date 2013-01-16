package com.gpxcreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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

    private JSplitPane splitPane;
    
    private GPXPanel gpxPanel;
    
    private JPanel containerLeftSidebar;
    
    private JPanel containerLeftSidebar1;
    private JLabel labelRoutesHeading;
    private JLabel labelRoutesSubheading;
    
    private JPanel containerLeftSidebar2;
    private DefaultTableModel routeTableModel;
    private JTable table;
    
    private JToolBar mainToolBar;

    private JButton btnFileSave;
    private JFileChooser chooserFileSave;
    private File fileSave;
    
    private JButton btnFileOpen;
    private JFileChooser chooserFileOpen;
    private File fileOpened;
    
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
        frame = new JFrame("GPX Creator");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(GPXCreator.class.getResource("/com/gpxcreator/icons/gpxcreator.png")));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        frame.setBounds(300, 188, (int) (width - 600), (int) (height - 376));
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mainToolBar = new JToolBar();
        mainToolBar.setFloatable(false);
        mainToolBar.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        frame.getContentPane().add(mainToolBar, BorderLayout.NORTH);
        
        gpxPanel = new GPXPanel();
        gpxPanel.setDisplayPositionByLatLon(36, -98, 4);
        try {
            gpxPanel.setTileLoader(new OsmFileCacheTileLoader(gpxPanel));
        } catch (Exception e) {
            System.err.println("There was a problem constructing the tile cache on disk.");
            e.printStackTrace();
        }
        
        splitPane = new JSplitPane();
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        splitPane.setRightComponent(gpxPanel);
        
        FileNameExtensionFilter gpxFilter = new FileNameExtensionFilter("GPX files (*.gpx)", "gpx");
        
        /* ---------------------------------------------- OPEN BUTTON ---------------------------------------------- */
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
        mainToolBar.add(btnFileOpen);
        String ctrlOpen = "CTRL+O";
        gpxPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), ctrlOpen);
        gpxPanel.getActionMap().put(ctrlOpen, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileOpen();
            }
        });
        
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
        mainToolBar.add(btnFileSave);
        String ctrlSave = "CTRL+S";
        gpxPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), ctrlSave);
        gpxPanel.getActionMap().put(ctrlSave, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSave();
            }
        });
        
        
        
        containerLeftSidebar = new JPanel();
        splitPane.setLeftComponent(containerLeftSidebar);
        containerLeftSidebar.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebar.setLayout(new BoxLayout(containerLeftSidebar, BoxLayout.Y_AXIS));
        
        containerLeftSidebar1 = new JPanel();
        containerLeftSidebar1.setMaximumSize(new Dimension(32767, 38));
        containerLeftSidebar1.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebar1.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebar1.setBorder(new CompoundBorder(new MatteBorder(0, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebar.add(containerLeftSidebar1);
        containerLeftSidebar1.setLayout(new BoxLayout(containerLeftSidebar1, BoxLayout.Y_AXIS));
        
        labelRoutesHeading = new JLabel("Routes");
        labelRoutesHeading.setMaximumSize(new Dimension(32767, 20));
        labelRoutesHeading.setVerticalAlignment(SwingConstants.BOTTOM);
        labelRoutesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        containerLeftSidebar1.add(labelRoutesHeading);
        
        labelRoutesSubheading = new JLabel("(active route shown in bold)");
        labelRoutesSubheading.setMaximumSize(new Dimension(32767, 16));
        labelRoutesSubheading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelRoutesSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        containerLeftSidebar1.add(labelRoutesSubheading);
        
        containerLeftSidebar2 = new JPanel();
        containerLeftSidebar2.setMaximumSize(new Dimension(32767, 100));
        containerLeftSidebar2.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebar2.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebar2.setBackground(Color.GREEN);
        containerLeftSidebar2.setBorder(new EmptyBorder(0, 0, 0, 0));
        containerLeftSidebar.add(containerLeftSidebar2);
        containerLeftSidebar2.setLayout(new BoxLayout(containerLeftSidebar2, BoxLayout.Y_AXIS));
        
        routeTableModel = new DefaultTableModel(new Object[]{"Route Names"},0);
        table = new JTable(routeTableModel);
        table.setBorder(new EmptyBorder(0, 0, 0, 0));
        table.setMaximumSize(new Dimension(32767, 32767));
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setTableHeader(null);
        table.setEnabled(false); // TODO <-- this is only temporary... until table can be developed further!

        JScrollPane listScrollPane = new JScrollPane(table);
        listScrollPane.setBorder(new LineBorder(new Color(0, 0, 0)));
        listScrollPane.setPreferredSize(new Dimension(0, 0));
        listScrollPane.setMinimumSize(new Dimension(0, 0));
        containerLeftSidebar2.add(listScrollPane);
        
        
        
        
        
        
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
        int returnVal = chooserFileOpen.showOpenDialog(gpxPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileOpened = chooserFileOpen.getSelectedFile();
            Route route = new Route(fileOpened);
            gpxPanel.addRoute(route);
            routeTableModel.addRow(new Object[]{route.getName()});
            gpxPanel.fitRouteToPanel(route);
        }
    }
    
    public void fileSave() {
        int returnVal = chooserFileSave.showSaveDialog(gpxPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileSave = chooserFileSave.getSelectedFile();
            gpxPanel.getActiveRoute().saveToGPXFile(fileSave);
        }
    }
}
