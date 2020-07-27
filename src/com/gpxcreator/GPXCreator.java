// TODO
// 1) Replace YOURS with ORS?
// 2) Find better way to omit lifts from MTB tracks

package com.gpxcreator;

import com.gpxcreator.PathFinder.PathFindType;
import com.gpxcreator.gpxpanel.*;
import com.gpxcreator.gpxpanel.WaypointGroup.EleCorrectedStatus;
import com.gpxcreator.gpxpanel.WaypointGroup.WptGrpType;
import com.gpxcreator.tree.GPXTree;
import com.gpxcreator.tree.GPXTreeRenderer;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * The main application class for GPX Creator, a GUI for manipulating GPX files.<br />
 * More info at www.gpxcreator.com.
 *
 * @author hooverm
 */
@SuppressWarnings("serial")
public class GPXCreator extends JComponent {

    // indents show layout hierarchy
    private JFrame frame;
    private JPanel glassPane;
    private JLabel glassPaneStatus;
    private static String lookAndFeel;
      private JToolBar toolBarMain;                              // NORTH
        private boolean fileIOHappening;
        private JButton btnFileNew;
        private JButton btnFileOpen;
        private JFileChooser chooserFileOpen;
        private File fileOpened;
        private GPXFile gpxFileOpened;
        private JButton btnFileSave;
        private JFileChooser chooserFileSave;
        private File fileSave;
        private JButton btnObjectDelete;
        private JButton btnEditProperties;
        private JToggleButton tglPathFinder;
        private SwingWorker<Void, Void> pathFindWorker;
        private JToggleButton tglAddPoints;
        private JToggleButton tglDelPoints;
        private JToggleButton tglSplitTrackseg;
        private JButton btnCorrectEle;
        private JButton btnEleChart;
        private JButton btnSpeedChart;
        protected JComboBox<String> comboBoxTileSource;
        private JLabel lblLat;
        private JTextField textFieldLat;
        private JLabel lblLon;
        private JTextField textFieldLon;
        private JToggleButton tglLatLonFocus;
      private JSplitPane splitPaneMain;                          // CENTER
        private JSplitPane splitPaneSidebar;                     // LEFT
          private JPanel containerLeftSidebarTop;                // TOP
            private JPanel containerExplorerHeading;
              private JLabel labelExplorerHeading;
            private JScrollPane scrollPaneExplorer;
              private DefaultMutableTreeNode root;
              private DefaultTreeModel treeModel;
              private JTree tree;
              private DefaultMutableTreeNode currSelection;
              // private DefaultMutableTreeNode prevSelection;
          private JPanel containerLeftSidebarBottom;             // BOTTOM
            private JPanel containerPropertiesHeading;
              private JLabel labelPropertiesHeading;
            private JScrollPane scrollPaneProperties;
              private DefaultTableModel tableModelProperties;
              private JTable tableProperties;
              private SimpleDateFormat sdf;
        protected GPXPanel mapPanel;                             // RIGHT
          private JPanel panelRoutingOptions;
          private JLabel lblMapQuestFoot;
          private JLabel lblMapQuestBike;
          private JLabel lblYOURSFoot;
          private JLabel lblYOURSBike;
          private List<JLabel> lblsRoutingOptions;
          private PathFinder pathFinder;
          private PathFinder pathFinderMapquest;
          private PathFinder pathFinderYOURS;
          private PathFinder.PathFindType pathFindType;
          private JPanel panelRoutingCancel;
          private JLabel lblRoutingCancel;
    private GPXObject activeGPXObject;
    private Cursor mapCursor;
    private boolean mouseOverLink;
    private WaypointGroup activeWptGrp;
    private Track activeTrack;
    private DefaultMutableTreeNode activeTrackNode;
    private DefaultMutableTreeNode activeTracksegNode;
    private Waypoint activeWpt;
    private Color transparentYellow;
    private Color transparentGrey;
    private Color transparentRed;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          GPXCreator window = new GPXCreator();
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
  protected void initialize() {
    try {
      lookAndFeel = UIManager.getSystemLookAndFeelClassName();
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    /* MAIN FRAME
     * --------------------------------------------------------------------------------------------------------- */
    frame = new JFrame("GPX Creator");
    transparentGrey = new Color(160, 160, 160, 192);
    transparentYellow = new Color(177, 177, 25, 192);
    transparentRed = new Color(177, 25, 25, 192);
    glassPane = new JPanel();
    glassPane.setOpaque(false);
    frame.setGlassPane(glassPane);
    glassPane.setLayout(new BorderLayout());
    glassPaneStatus = new JLabel();
    glassPaneStatus.setHorizontalAlignment(SwingConstants.CENTER);
    glassPaneStatus.setFont(new Font("Segoe UI", Font.PLAIN, 22));
    glassPaneStatus.setOpaque(true);
    glassPaneStatus.setBackground(transparentYellow);
    glassPaneStatus.setForeground(Color.BLACK);
    glassPane.add(glassPaneStatus, BorderLayout.SOUTH);
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
    int screenWidth = (int) screenSize.getWidth();
    int screenHeight = (int) screenSize.getHeight();
    int frameWidth = (screenWidth * 2) / 3;
    int frameHeight = (screenHeight * 2) / 3;
    frame.setBounds(((screenWidth - frameWidth) / 2), ((screenHeight - frameHeight) / 2), frameWidth, frameHeight);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    /* MAIN SPLIT PANE
     * --------------------------------------------------------------------------------------------------------- */
    splitPaneMain = new JSplitPane();
    splitPaneMain.setContinuousLayout(true);
    frame.getContentPane().add(splitPaneMain, BorderLayout.CENTER);

    splitPaneMain.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // must be scheduled with invokeLater, or if user moves divider fast enough, the update won't happen
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            updatePropTableWidths();
          }
        });
      }
    });

    /* MAP PANEL
     * --------------------------------------------------------------------------------------------------------- */
    mapPanel = new GPXPanel();
    mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.X_AXIS));
    mapPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mapPanel.setDisplayPosition(1000, 1600, 4); // U! S! A!
    mapPanel.setZoomContolsVisible(false);
    try {
      mapPanel.setTileLoader(new OsmTileLoader(mapPanel));
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
          mouseOverLink = true;
        } else {
          mapPanel.setCursor(mapCursor);
          mouseOverLink = false;
        }
      }
    });

    MouseListener routingControlsHoverListener = new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        mapPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (e.getSource().equals(lblRoutingCancel)) {
          glassPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        mapPanel.setCursor(mapCursor);
        if (e.getSource().equals(lblRoutingCancel)) {
          glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }
      }
    };

    MouseListener routingControlsClickListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        JLabel clicked = (JLabel) e.getSource();
        if (clicked.equals(lblRoutingCancel)) {
          try {
            pathFindWorker.cancel(true);
            updateButtonVisibility();
            glassPane.setVisible(false);
            glassPaneStatus.setText("");
            glassPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mapPanel.repaint();
            updatePropsTable();
          } catch (Exception e1) {

          }
          panelRoutingCancel.repaint();
          return;
        }

        for (JLabel lbl : lblsRoutingOptions) {
          lbl.setBackground(transparentGrey);
        }
        clicked.setBackground(transparentYellow);
        panelRoutingOptions.repaint();
        if (clicked.equals(lblMapQuestFoot)) {
          pathFinder = pathFinderMapquest;
          pathFindType = PathFindType.FOOT;
        } else if (clicked.equals(lblMapQuestBike)) {
          pathFinder = pathFinderMapquest;
          pathFindType = PathFindType.BIKE;
        } else if (clicked.equals(lblYOURSFoot)) {
          pathFinder = pathFinderYOURS;
          pathFindType = PathFindType.FOOT;
        } else if (clicked.equals(lblYOURSBike)) {
          pathFinder = pathFinderYOURS;
          pathFindType = PathFindType.BIKE;
        }
      }
    };

    // pathfinding cancel panel (begin)
    panelRoutingCancel = new JPanel();
    panelRoutingCancel.setLayout(new BoxLayout(panelRoutingCancel, BoxLayout.Y_AXIS));
    panelRoutingCancel.setOpaque(false);
    panelRoutingCancel.setBorder(new CompoundBorder(
        new EmptyBorder(10, 10, 10, 10), new LineBorder(new Color(105, 105, 105))));
    panelRoutingCancel.setAlignmentY(Component.TOP_ALIGNMENT);
    mapPanel.add(panelRoutingCancel);

    lblRoutingCancel = new JLabel("Cancel Pathfinding Operation");
    lblRoutingCancel.setBorder(new CompoundBorder(
        new LineBorder(new Color(105, 105, 105)), new EmptyBorder(2, 4, 2, 4)));
    lblRoutingCancel.setAlignmentY(Component.TOP_ALIGNMENT);
    lblRoutingCancel.setOpaque(true);
    lblRoutingCancel.setBackground(transparentRed);
    panelRoutingCancel.add(lblRoutingCancel);

    Dimension dim = new Dimension(
        lblRoutingCancel.getPreferredSize().width, lblRoutingCancel.getPreferredSize().height);
    lblRoutingCancel.setMaximumSize(dim);
    lblRoutingCancel.setMinimumSize(dim);
    lblRoutingCancel.setPreferredSize(dim);
    lblRoutingCancel.addMouseListener(routingControlsHoverListener);
    lblRoutingCancel.addMouseListener(routingControlsClickListener);

    panelRoutingCancel.setVisible(false);
    // pathfinding cancel panel (end)

    // pathfinding options panel (begin)
    panelRoutingOptions = new JPanel();
    panelRoutingOptions.setLayout(new BoxLayout(panelRoutingOptions, BoxLayout.Y_AXIS));
    panelRoutingOptions.setOpaque(false);
    panelRoutingOptions.setBorder(new CompoundBorder(
        new EmptyBorder(10, 10, 10, 10), new LineBorder(new Color(105, 105, 105))));
    panelRoutingOptions.setAlignmentY(Component.TOP_ALIGNMENT);
    mapPanel.add(Box.createHorizontalGlue());
    mapPanel.add(panelRoutingOptions);

    lblMapQuestFoot = new JLabel("MapQuest (foot)");
    lblMapQuestBike = new JLabel("MapQuest (bike)");
    lblYOURSFoot = new JLabel("YOURS (foot)");
    lblYOURSBike = new JLabel("YOURS (bike)");

    lblsRoutingOptions = new ArrayList<JLabel>();
    lblsRoutingOptions.add(lblMapQuestFoot);
    lblsRoutingOptions.add(lblMapQuestBike);
    lblsRoutingOptions.add(lblYOURSFoot);
    lblsRoutingOptions.add(lblYOURSBike);

    for (JLabel lbl : lblsRoutingOptions) {
      lbl.setBorder(new CompoundBorder(
          new LineBorder(new Color(105, 105, 105)), new EmptyBorder(2, 4, 2, 4)));
      lbl.setAlignmentY(Component.TOP_ALIGNMENT);
      lbl.setOpaque(true);
      lbl.setBackground(transparentGrey);
      panelRoutingOptions.add(lbl);
    }
    int maxWidth = 0;
    int maxHeight = 0;
    for (JLabel lbl : lblsRoutingOptions) {
      maxWidth = Math.max(maxWidth, lbl.getPreferredSize().width);
      maxHeight = Math.max(maxHeight, lbl.getPreferredSize().height);
    }
    dim = new Dimension(maxWidth, maxHeight);
    for (JLabel lbl : lblsRoutingOptions) {
      lbl.setMaximumSize(dim);
      lbl.setMinimumSize(dim);
      lbl.setPreferredSize(dim);
      lbl.addMouseListener(routingControlsHoverListener);
      lbl.addMouseListener(routingControlsClickListener);
    }

    lblMapQuestFoot.setBackground(transparentYellow);
    panelRoutingOptions.setVisible(false);

    pathFinderMapquest = new PathFinderMapQuest();
    pathFinderYOURS = new PathFinderYOURS();
    pathFinder = pathFinderMapquest;
    pathFindType = PathFindType.FOOT;
    // pathfinding options panel (end)

    // up and down keys will also zoom the map in and out
    String zoomIn = "zoom in";
    mapPanel.getInputMap(JComponent.WHEN_FOCUSED).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), zoomIn);
    mapPanel.getActionMap().put(zoomIn, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mapPanel.zoomIn();
      }
    });

    String zoomOut = "zoom out";
    mapPanel.getInputMap(JComponent.WHEN_FOCUSED).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), zoomOut);
    mapPanel.getActionMap().put(zoomOut, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mapPanel.zoomOut();
      }
    });

    mapPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        mapPanel.requestFocus();
      }
    });
        
        /* SIDEBAR SPLIT PANE
        /* --------------------------------------------------------------------------------------------------------- */
    splitPaneSidebar = new JSplitPane();
    splitPaneSidebar.setMinimumSize(new Dimension(240, 25));
    splitPaneSidebar.setPreferredSize(new Dimension(240, 25));
    splitPaneSidebar.setContinuousLayout(true);
    splitPaneSidebar.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPaneMain.setLeftComponent(splitPaneSidebar);
    splitPaneSidebar.setDividerLocation(210);

    splitPaneSidebar.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // must be scheduled with invokeLater, or if user moves divider fast enough, the update won't happen
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            updatePropTableWidths();
          }
        });
      }
    });

    /* LEFT SIDEBAR TOP CONTAINER
     * --------------------------------------------------------------------------------------------------------- */
    containerLeftSidebarTop = new JPanel();
    containerLeftSidebarTop.setPreferredSize(new Dimension(10, 100));
    containerLeftSidebarTop.setAlignmentY(Component.TOP_ALIGNMENT);
    containerLeftSidebarTop.setAlignmentX(Component.LEFT_ALIGNMENT);
    containerLeftSidebarTop.setLayout(new BoxLayout(containerLeftSidebarTop, BoxLayout.Y_AXIS));
    splitPaneSidebar.setTopComponent(containerLeftSidebarTop);

    /* EXPLORER HEADING CONTAINER
     * --------------------------------------------------------------------------------------------------------- */
    containerExplorerHeading = new JPanel();
    containerExplorerHeading.setPreferredSize(new Dimension(10, 23));
    containerExplorerHeading.setMinimumSize(new Dimension(10, 23));
    containerExplorerHeading.setMaximumSize(new Dimension(32767, 23));
    containerExplorerHeading.setAlignmentY(Component.TOP_ALIGNMENT);
    containerExplorerHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
    containerExplorerHeading.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    containerExplorerHeading.setLayout(new BoxLayout(containerExplorerHeading, BoxLayout.Y_AXIS));
    containerExplorerHeading.setBorder(new CompoundBorder(
        new MatteBorder(1, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
    containerLeftSidebarTop.add(containerExplorerHeading);

    /* EXPLORER HEADING
     * --------------------------------------------------------------------------------------------------------- */
    labelExplorerHeading = new JLabel("Explorer");
    labelExplorerHeading.setAlignmentY(Component.TOP_ALIGNMENT);
    labelExplorerHeading.setMaximumSize(new Dimension(32767, 14));
    labelExplorerHeading.setHorizontalTextPosition(SwingConstants.LEFT);
    labelExplorerHeading.setHorizontalAlignment(SwingConstants.LEFT);
    labelExplorerHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
    containerExplorerHeading.add(labelExplorerHeading);

    /* EXPLORER TREE/MODEL
     * --------------------------------------------------------------------------------------------------------- */
    root = new DefaultMutableTreeNode("GPX Files");
    treeModel = new DefaultTreeModel(root);
    tree = new GPXTree(treeModel);
    tree.setEditable(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new GPXTreeRenderer());
    tree.putClientProperty("JTree.lineStyle", "None");
    tree.setBackground(Color.white);
    tree.setToggleClickCount(0);

    ImageIcon collapsed = new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/tree-collapsed.png"));
    ImageIcon expanded = new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/tree-expanded.png"));
    UIManager.put("Tree.collapsedIcon", collapsed);
    UIManager.put("Tree.expandedIcon", expanded);

    // give Java look and feel to tree only (to get rid of dotted line handles/connectors)
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      SwingUtilities.updateComponentTreeUI(tree);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Exception e) {
      e.printStackTrace();
    }

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        deselectAllToggles(null);

        // set selected object as current selection and active in map panel
        currSelection = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (currSelection != null) {
          setActiveGPXObject((GPXObject) currSelection.getUserObject());
        }

        // necessary hack if bold selection style used in GPXTreeComponentFactory (keeps label sizes correct)
        /*treeModel.nodeChanged(currSelection);
        if (prevSelection != null) {
          treeModel.nodeChanged(prevSelection);
        }
        prevSelection = currSelection;*/

        updateButtonVisibility();
      }
    });

    treeModel.addTreeModelListener(new TreeModelListener() {
      @Override
      public void treeStructureChanged(TreeModelEvent e) {
      }

      @Override
      public void treeNodesRemoved(TreeModelEvent e) {
      }

      @Override
      public void treeNodesInserted(TreeModelEvent e) {
      }

      @Override
      public void treeNodesChanged(TreeModelEvent e) { // necessary for changed color, vis, waypoint vis
        mapPanel.repaint();
      }
    });

    /* EXPLORER TREE SCROLLPANE
     * --------------------------------------------------------------------------------------------------------- */
    UIManager.put("ScrollBar.minimumThumbSize", new Dimension(16, 16)); // prevent Windows L&F scroll thumb bug
    scrollPaneExplorer = new JScrollPane(tree);
    scrollPaneExplorer.setAlignmentY(Component.TOP_ALIGNMENT);
    scrollPaneExplorer.setAlignmentX(Component.LEFT_ALIGNMENT);
    scrollPaneExplorer.setBorder(new LineBorder(new Color(0, 0, 0)));
    containerLeftSidebarTop.add(scrollPaneExplorer);

    /* LEFT SIDEBAR BOTTOM CONTAINER
     * --------------------------------------------------------------------------------------------------------- */
    containerLeftSidebarBottom = new JPanel();
    containerLeftSidebarBottom.setAlignmentY(Component.TOP_ALIGNMENT);
    containerLeftSidebarBottom.setAlignmentX(Component.LEFT_ALIGNMENT);
    containerLeftSidebarBottom.setLayout(new BoxLayout(containerLeftSidebarBottom, BoxLayout.Y_AXIS));
    splitPaneSidebar.setBottomComponent(containerLeftSidebarBottom);

    /* PROPERTIES CONTAINER
     * --------------------------------------------------------------------------------------------------------- */
    containerPropertiesHeading = new JPanel();
    containerPropertiesHeading.setMaximumSize(new Dimension(32767, 23));
    containerPropertiesHeading.setMinimumSize(new Dimension(10, 23));
    containerPropertiesHeading.setPreferredSize(new Dimension(10, 23));
    containerPropertiesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
    containerPropertiesHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
    containerPropertiesHeading.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    containerPropertiesHeading.setLayout(new BoxLayout(containerPropertiesHeading, BoxLayout.Y_AXIS));
    containerPropertiesHeading.setBorder(new CompoundBorder(
        new MatteBorder(1, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
    containerLeftSidebarBottom.add(containerPropertiesHeading);
    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    /* PROPERTIES HEADING
     * --------------------------------------------------------------------------------------------------------- */
    labelPropertiesHeading = new JLabel("Properties");
    labelPropertiesHeading.setMaximumSize(new Dimension(32767, 14));
    labelPropertiesHeading.setHorizontalTextPosition(SwingConstants.LEFT);
    labelPropertiesHeading.setHorizontalAlignment(SwingConstants.LEFT);
    labelPropertiesHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
    labelPropertiesHeading.setAlignmentY(0.0f);
    containerPropertiesHeading.add(labelPropertiesHeading);

    /* PROPERTIES TABLE/MODEL
     * --------------------------------------------------------------------------------------------------------- */
    tableModelProperties = new DefaultTableModel(new Object[]{"Name", "Value"}, 0);
    tableProperties = new JTable(tableModelProperties);
    tableProperties.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    tableProperties.setAlignmentY(Component.TOP_ALIGNMENT);
    tableProperties.setAlignmentX(Component.LEFT_ALIGNMENT);
    tableProperties.setBorder(new EmptyBorder(0, 0, 0, 0));
    tableProperties.setFillsViewportHeight(true);
    tableProperties.setTableHeader(null);
    tableProperties.setEnabled(false);
    tableProperties.getColumnModel().setColumnMargin(0);

    /* PROPERTIES TABLE SCROLLPANE
     * --------------------------------------------------------------------------------------------------------- */
    scrollPaneProperties = new JScrollPane(tableProperties);
    scrollPaneProperties.setAlignmentY(Component.TOP_ALIGNMENT);
    scrollPaneProperties.setAlignmentX(Component.LEFT_ALIGNMENT);
    scrollPaneProperties.setBorder(new LineBorder(new Color(0, 0, 0)));
    containerLeftSidebarBottom.add(scrollPaneProperties);

    /* MAIN TOOLBAR
     * --------------------------------------------------------------------------------------------------------- */
    toolBarMain = new JToolBar();
    toolBarMain.setLayout(new BoxLayout(toolBarMain, BoxLayout.X_AXIS));
    toolBarMain.setFloatable(false);
    toolBarMain.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    frame.getContentPane().add(toolBarMain, BorderLayout.NORTH);

    /* NEW FILE BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    btnFileNew = new JButton("");
    btnFileNew.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fileNew();
      }
    });

    btnFileNew.setToolTipText("<html>Create new GPX file<br>[CTRL+N]</html>");
    btnFileNew.setFocusable(false);
    btnFileNew.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-new.png")));
    btnFileNew.setDisabledIcon(
        new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-new-disabled.png")));
    String ctrlNew = "CTRL+N";
    mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), ctrlNew);
    mapPanel.getActionMap().put(ctrlNew, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fileNew();
      }
    });
    toolBarMain.add(btnFileNew);

    /* OPEN FILE BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    int chooserWidth = (frame.getWidth() * 8) / 10;
    int chooserHeight = (frame.getHeight() * 8) / 10;
    chooserWidth = Math.min(864, chooserWidth);
    chooserHeight = Math.min(539, chooserHeight);

    FileNameExtensionFilter gpxFilter = new FileNameExtensionFilter("GPX files (*.gpx)", "gpx");
    btnFileOpen = new JButton("");
    chooserFileOpen = new JFileChooser() {
      @Override
      protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        InputStream in = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/file-open.png");
        BufferedImage img = null;
        try {
          img = ImageIO.read(in);
        } catch (Exception e) {
          e.printStackTrace();
        }
        dialog.setIconImage(img);
        return dialog;
      }
    };
    chooserFileOpen.setCurrentDirectory(
        new File("C:\\eclipse\\workspace\\GPXCreator\\IO")); // TODO change this before deployment
    chooserFileOpen.addChoosableFileFilter(gpxFilter);
    chooserFileOpen.setFileFilter(gpxFilter);
    chooserFileOpen.setPreferredSize(new Dimension(chooserWidth, chooserHeight));
    btnFileOpen.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fileOpen();
      }
    });
    btnFileOpen.setToolTipText("<html>Open GPX file<br>[CTRL+O]</html>");
    btnFileOpen.setFocusable(false);
    btnFileOpen.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-open.png")));
    btnFileOpen.setDisabledIcon(
        new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-open-disabled.png")));
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

    /* SAVE FILE BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    btnFileSave = new JButton("");
    chooserFileSave = new JFileChooser() {
      @Override
      protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        InputStream in = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/file-save.png");
        BufferedImage img = null;
        try {
          img = ImageIO.read(in);
        } catch (Exception e) {
          e.printStackTrace();
        }
        dialog.setIconImage(img);
        return dialog;
      }
    };
    chooserFileSave.setCurrentDirectory(
        new File("C:\\eclipse\\workspace\\GPXCreator\\IO")); // TODO change this before deployment
    chooserFileSave.addChoosableFileFilter(gpxFilter);
    chooserFileSave.setFileFilter(gpxFilter);
    chooserFileSave.setPreferredSize(new Dimension(chooserWidth, chooserHeight));
    btnFileSave.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fileSave();
      }
    });

    btnFileSave.setToolTipText("<html>Save selected GPX file<br>[CTRL+S]</html>");
    btnFileSave.setFocusable(false);
    btnFileSave.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-save.png")));
    btnFileSave.setDisabledIcon(
        new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/file-save-disabled.png")));
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
    btnFileSave.setEnabled(false);

    /* OBJECT DELETE BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    btnObjectDelete = new JButton("");
    btnObjectDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        deleteActiveGPXObject();
      }
    });
    btnObjectDelete.setToolTipText("<html>Delete selected object<br>[CTRL+D]</html>");
    btnObjectDelete.setFocusable(false);
    btnObjectDelete.setIcon(
        new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/object-delete.png")));
    btnObjectDelete.setDisabledIcon(
        new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/object-delete-disabled.png")));
    String ctrlDelete = "CTRL+D";
    mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK), ctrlDelete);
    mapPanel.getActionMap().put(ctrlDelete, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        deleteActiveGPXObject();
      }
    });
    toolBarMain.add(btnObjectDelete);
    toolBarMain.addSeparator();
    btnObjectDelete.setEnabled(false);

    /* EDIT PROPERTIES BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    btnEditProperties = new JButton("");
    btnEditProperties.setToolTipText("Edit properties");
    btnEditProperties.setFocusable(false);
    btnEditProperties.setIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/edit-properties.png")));
    btnEditProperties.setEnabled(false);
    btnEditProperties.setDisabledIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/edit-properties-disabled.png")));
    btnEditProperties.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        editProperties();
      }
    });
    toolBarMain.add(btnEditProperties);

    /* PATHFINDER BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    tglPathFinder = new JToggleButton("");
    tglPathFinder.setToolTipText("Find path");
    tglPathFinder.setFocusable(false);
    tglPathFinder.setIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/path-find.png")));
    tglPathFinder.setEnabled(false);
    tglPathFinder.setDisabledIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/path-find-disabled.png")));
    mapPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (tglPathFinder.isSelected() && activeGPXObject != null && !mouseOverLink) {
          int zoom = mapPanel.getZoom();
          int x = e.getX();
          int y = e.getY();
          Point mapCenter = mapPanel.getCenter();
          int xStart = mapCenter.x - mapPanel.getWidth() / 2;
          int yStart = mapCenter.y - mapPanel.getHeight() / 2;
          OsmMercator osmMercator = new OsmMercator();
          final double lat = osmMercator.yToLat(yStart + y, zoom);
          final double lon = osmMercator.xToLon(xStart + x, zoom);

          Route route = null;
          DefaultMutableTreeNode gpxFileNode = null;
          if (activeGPXObject.isGPXFileWithOneRoute()) {
            route = ((GPXFile) activeGPXObject).getRoutes().get(0);
            gpxFileNode = currSelection;
          } else if (activeGPXObject.isRoute()) {
            route = (Route) activeGPXObject;
            gpxFileNode = (DefaultMutableTreeNode) currSelection.getParent();
          }
          final Route finalRoute = route;
          final DefaultMutableTreeNode finalGPXFileNode = gpxFileNode;

          if (route.getPath().getNumPts() == 0) { // route is empty, so add first point
            Waypoint wpt = new Waypoint(lat, lon);
            route.getPath().addWaypoint(wpt, false);
          } else { // route is not empty, so find path from current end to the point that was clicked
            pathFindWorker = new SwingWorker<Void, Void>() {
              @Override
              public Void doInBackground() {
                glassPane.setVisible(true);
                glassPaneStatus.setText("finding path...");
                glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                panelRoutingCancel.setVisible(true);
                tglPathFinder.setEnabled(false);
                btnCorrectEle.setEnabled(false);
                SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    frame.repaint();
                  }
                });
                Waypoint pathfindStart = finalRoute.getPath().getEnd();
                double startLat = pathfindStart.getLat();
                double startLon = pathfindStart.getLon();

                String xml = pathFinder.getXMLResponse(pathFindType, startLat, startLon, lat, lon);
                if (isCancelled()) {
                  return null;
                }
                panelRoutingCancel.setVisible(false);
                List<Waypoint> newPathFound = pathFinder.parseXML(xml);

                for (Waypoint wpt : newPathFound) {
                  finalRoute.getPath().addWaypoint(wpt, false);
                }

                finalRoute.getPath().updateLength();
                if (finalRoute.getPath().getLengthMiles() < 250) {
                  finalRoute.getPath().correctElevation(true);
                }
                return null;
              }

              @Override
              protected void done() {
                panelRoutingCancel.setVisible(false);
                if (isCancelled()) {
                  return;
                }

                Object gpxFileObject = finalGPXFileNode.getUserObject();
                GPXFile gpxFile = (GPXFile) gpxFileObject;
                gpxFile.updateAllProperties();

                updateButtonVisibility();
                glassPane.setVisible(false);
                glassPaneStatus.setText("");
                glassPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                mapPanel.repaint();
                updatePropsTable();
              }
            };
            pathFindWorker.execute();
          }
          mapPanel.repaint();
          updatePropsTable();
        }
      }
    });
    tglPathFinder.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          deselectAllToggles(tglPathFinder);
          mapCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

          if (activeGPXObject.isGPXFileWithNoRoutes()) {
            Route route = ((GPXFile) activeGPXObject).addRoute();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(route);
            treeModel.insertNodeInto(newNode, currSelection, 0);
            updateButtonVisibility();
          }
          panelRoutingOptions.setVisible(true);
        } else {
          mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
          panelRoutingOptions.setVisible(false);
        }
        mapPanel.repaint();
      }
    });
    toolBarMain.add(tglPathFinder);

    /* ADD POINTS BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    tglAddPoints = new JToggleButton("");
    tglAddPoints.setToolTipText("Add points");
    tglAddPoints.setFocusable(false);
    tglAddPoints.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/add-points.png")));
    tglAddPoints.setEnabled(false);
    tglAddPoints.setDisabledIcon(
        new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/add-points-disabled.png")));
    toolBarMain.add(tglAddPoints);
    mapPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (tglAddPoints.isSelected() && activeGPXObject != null && !mouseOverLink) {
          int zoom = mapPanel.getZoom();
          int x = e.getX();
          int y = e.getY();
          Point mapCenter = mapPanel.getCenter();
          int xStart = mapCenter.x - mapPanel.getWidth() / 2;
          int yStart = mapCenter.y - mapPanel.getHeight() / 2;
          OsmMercator osmMercator = new OsmMercator();
          double lat = osmMercator.yToLat(yStart + y, zoom);
          double lon = osmMercator.xToLon(xStart + x, zoom);
          Waypoint wpt = new Waypoint(lat, lon);

          if (activeGPXObject.isGPXFileWithOneRoute()) {
            Route route = ((GPXFile) activeGPXObject).getRoutes().get(0);
            route.getPath().addWaypoint(wpt, false);
          } else if (activeGPXObject.isRoute()) {
            Route route = (Route) activeGPXObject;
            route.getPath().addWaypoint(wpt, false);
          } else if (activeGPXObject.isWaypointGroup()
              && ((WaypointGroup) activeGPXObject).getWptGrpType() == WptGrpType.WAYPOINTS) {
            WaypointGroup wptGrp = (WaypointGroup) activeGPXObject;
            wptGrp.addWaypoint(wpt, false);
          }
          DefaultMutableTreeNode gpxFileNode = currSelection;
          while (!((GPXObject) gpxFileNode.getUserObject()).isGPXFile()) {
            gpxFileNode = (DefaultMutableTreeNode) gpxFileNode.getParent();
          }
          Object gpxFileObject = gpxFileNode.getUserObject();
          GPXFile gpxFile = (GPXFile) gpxFileObject;
          gpxFile.updateAllProperties();

          mapPanel.repaint();
          updatePropsTable();
        }
      }
    });
    tglAddPoints.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          deselectAllToggles(tglAddPoints);
          mapCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

          if (activeGPXObject.isGPXFileWithNoRoutes()) {
            Route route = ((GPXFile) activeGPXObject).addRoute();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(route);
            treeModel.insertNodeInto(newNode, currSelection, 0);
            updateButtonVisibility();
          }
        } else {
          mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        }
      }
    });

    /* DELETE POINTS BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    tglDelPoints = new JToggleButton("");
    tglDelPoints.setToolTipText("Delete points");
    tglDelPoints.setFocusable(false);
    tglDelPoints.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/delete-points.png")));
    tglDelPoints.setEnabled(false);
    tglDelPoints.setDisabledIcon(
        new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/delete-points-disabled.png")));
    toolBarMain.add(tglDelPoints);
    tglDelPoints.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          deselectAllToggles(tglDelPoints);
          mapCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        } else {
          mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        }
      }
    });

    // the 3 listeners below are shared by multiple functionalities (delete points, split trackseg)
    mapPanel.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        updateActiveWpt(e);
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        activeWptGrp = null;
        activeWpt = null;
        mapPanel.setShownPoint(null);
        mapPanel.repaint();
      }
    });

    mapPanel.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        updateActiveWpt(e);
      }
    });

    mapPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        activeWptGrp = null;
        activeWpt = null;
        mapPanel.setShownPoint(null);
        mapPanel.repaint();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        boolean isActiveWptGrp = activeWpt != null && activeWptGrp != null;
        boolean isActiveWptGrpOrTrack = isActiveWptGrp || activeTrack != null;
        if (isActiveWptGrpOrTrack && !mouseOverLink) {
          DefaultMutableTreeNode findFile = currSelection;
          while (!((GPXObject) findFile.getUserObject()).isGPXFile()) {
            findFile = (DefaultMutableTreeNode) findFile.getParent();
          }
          GPXFile gpxFile = (GPXFile) findFile.getUserObject();

          if (tglDelPoints.isSelected()) {
            if (!isActiveWptGrp) {
              return;
            }
            activeWptGrp.removeWaypoint(activeWpt);
            gpxFile.updateAllProperties();
          } else if (tglSplitTrackseg.isSelected()) {
            WaypointGroup tracksegBeforeSplit = null;
            if (activeWptGrp != null) {
              tracksegBeforeSplit = activeWptGrp;
            } else {
              int i;
              for (i = 0; i < activeTrack.getTracksegs().size(); i++) {
                WaypointGroup trackseg = activeTrack.getTracksegs().get(i);
                if (trackseg.contains(activeWpt)) {
                  tracksegBeforeSplit = trackseg;
                  activeTracksegNode = (DefaultMutableTreeNode) activeTrackNode.getChildAt(i);
                }
              }
            }

            List<Waypoint> trackptsBeforeSplit = tracksegBeforeSplit.getWaypoints();
            int splitIndex = trackptsBeforeSplit.indexOf(activeWpt);

            List<Waypoint> trackptsAfterSplit1 = new ArrayList<Waypoint>(
                trackptsBeforeSplit.subList(0, splitIndex + 1));
            List<Waypoint> trackptsAfterSplit2 = new ArrayList<Waypoint>(
                trackptsBeforeSplit.subList(splitIndex, trackptsBeforeSplit.size()));
            WaypointGroup tracksegAfterSplit1 =
                new WaypointGroup(tracksegBeforeSplit.getColor(), WptGrpType.TRACKSEG);
            WaypointGroup tracksegAfterSplit2 =
                new WaypointGroup(tracksegBeforeSplit.getColor(), WptGrpType.TRACKSEG);
            tracksegAfterSplit1.setWaypoints(trackptsAfterSplit1);
            tracksegAfterSplit2.setWaypoints(trackptsAfterSplit2);

            DefaultMutableTreeNode oldTracksegNode = activeTracksegNode;
            DefaultMutableTreeNode trackNode = (DefaultMutableTreeNode) oldTracksegNode.getParent();

            Object trackObject = trackNode.getUserObject();

            Track track = (Track) trackObject;
            int insertIndex = track.getTracksegs().indexOf(tracksegBeforeSplit);
            track.getTracksegs().remove(tracksegBeforeSplit);
            tracksegAfterSplit2.updateAllProperties();
            tracksegAfterSplit1.updateAllProperties();

            treeModel.removeNodeFromParent(oldTracksegNode);
            if (tracksegAfterSplit2.getLengthMiles() > 0.5) {
              track.getTracksegs().add(insertIndex, tracksegAfterSplit2);
              treeModel.insertNodeInto(new DefaultMutableTreeNode(tracksegAfterSplit2), trackNode, insertIndex);
            }
            if (tracksegAfterSplit1.getLengthMiles() > 0.5) {
              track.getTracksegs().add(insertIndex, tracksegAfterSplit1);
              treeModel.insertNodeInto(new DefaultMutableTreeNode(tracksegAfterSplit1), trackNode, insertIndex);
            }

            gpxFile.updateAllProperties();
            tglSplitTrackseg.setSelected(true);
          }

          activeWptGrp = null;
          activeWpt = null;
          activeTrack = null;
          mapPanel.setShownPoint(null);
          mapPanel.repaint();
          updatePropsTable();
          updateActiveWpt(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        updateActiveWpt(e);
      }
    });

    /* SPLIT TRACKSEG BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    tglSplitTrackseg = new JToggleButton("");
    tglSplitTrackseg.setToolTipText("Split track segment");
    tglSplitTrackseg.setFocusable(false);
    tglSplitTrackseg.setIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/trackseg-split.png")));
    tglSplitTrackseg.setEnabled(false);
    tglSplitTrackseg.setDisabledIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/trackseg-split-disabled.png")));
    toolBarMain.add(tglSplitTrackseg);

    tglSplitTrackseg.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          deselectAllToggles(tglSplitTrackseg);
          mapCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        } else {
          mapCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        }
      }
    });

    /* CORRECT ELEVATION BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    btnCorrectEle = new JButton("");
    btnCorrectEle.setToolTipText("Correct elevation");
    btnCorrectEle.setIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/correct-elevation.png")));
    btnCorrectEle.setEnabled(false);
    btnCorrectEle.setDisabledIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/correct-elevation-disabled.png")));
    btnCorrectEle.setFocusable(false);
    btnCorrectEle.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (activeGPXObject != null) {
          updateActiveWptGrp();
          if (activeWptGrp != null) {
            if (activeWptGrp.getLengthMiles() > 250) {
              JOptionPane.showMessageDialog(frame,
                  "Cannot correct elevation for paths longer than 250 miles.",
                  "Error",
                  JOptionPane.ERROR_MESSAGE);
              return;
            }

            SwingWorker<EleCorrectedStatus, Void> eleCorrWorker =
                new SwingWorker<EleCorrectedStatus, Void>() {
                  @Override
                  public EleCorrectedStatus doInBackground() {
                    glassPane.setVisible(true);
                    glassPaneStatus.setText("correcting elevation...");
                    glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    btnCorrectEle.setEnabled(false);
                    SwingUtilities.invokeLater(new Runnable() {
                      @Override
                      public void run() {
                        frame.repaint();
                      }
                    });
                    return activeWptGrp.correctElevation(true);
                  }

                  @Override
                  protected void done() {
                    EleCorrectedStatus corrected = null;
                    try {
                      corrected = get();
                    } catch (Exception e) {
                      e.printStackTrace();
                    } finally {
                      if (corrected == EleCorrectedStatus.FAILED) {
                        JOptionPane.showMessageDialog(frame,
                            "<html>There was a problem correcting the elevation." +
                                "  Possible causes:<br>" +
                                " - an empty set of points was submitted<br>" +
                                " - the route/track submitted was too long (limit of ~150 miles)<br>" +
                                " - the response from the server contained errors or was empty</html>",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                      } else if (corrected == EleCorrectedStatus.CORRECTED_WITH_CLEANSE) {
                        JOptionPane.showMessageDialog(frame,
                            "<html>The elevation response from the server" +
                                " had missing data segments.<br>" +
                                "These have been filled in by linear interpolation.</html>",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                      } else if (corrected == EleCorrectedStatus.CORRECTED) {
                        JOptionPane.showMessageDialog(frame,
                            "<html>Elevation data successfully corrected.</html>",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                      }
                      updateButtonVisibility();
                      glassPane.setVisible(false);
                      glassPaneStatus.setText("");
                      glassPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                      updatePropsTable();
                    }
                  }
                };
            eleCorrWorker.execute();
          }
        }
      }
    });
    toolBarMain.add(btnCorrectEle);

    /* ELEVATION CHART BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    btnEleChart = new JButton("");
    btnEleChart.setToolTipText("View elevation profile");
    btnEleChart.setIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/elevation-chart.png")));
    btnEleChart.setEnabled(false);
    btnEleChart.setDisabledIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/elevation-chart-disabled.png")));
    btnEleChart.setFocusable(false);
    btnEleChart.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        buildChart("Elevation profile", "/com/gpxcreator/icons/elevation-chart.png");
      }
    });
    toolBarMain.add(btnEleChart);

    /* SPEED CHART BUTTON
     * --------------------------------------------------------------------------------------------------------- */
    btnSpeedChart = new JButton("");
    btnSpeedChart.setToolTipText("View speed profile");
    btnSpeedChart.setIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/speed-chart.png")));
    btnSpeedChart.setEnabled(false);
    btnSpeedChart.setDisabledIcon(new ImageIcon(
        GPXCreator.class.getResource("/com/gpxcreator/icons/speed-chart-disabled.png")));
    btnSpeedChart.setFocusable(false);
    btnSpeedChart.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        buildChart("Speed profile", "/com/gpxcreator/icons/speed-chart.png");
      }
    });
    toolBarMain.add(btnSpeedChart);

    /* TILE SOURCE SELECTOR
     * --------------------------------------------------------------------------------------------------------- */
    toolBarMain.add(Box.createHorizontalGlue());

    final TileSource openStreetMap = new OsmTileSource.Mapnik();
    // Needs an API key.
    final TileSource openCycleMap = new OsmTileSource.CycleMap() {
      @Override
      protected String getApiKey() {
        return "c21f339f5a3f48b295e31ea2ccf77434";
      }
    };
    final TileSource bingAerial = new BingAerialTileSource();
    // MapQuest sources seem to have been removed from OSM. :-(
    // final TileSource mapQuestOsm = new MapQuestOsmTileSource();
    // final TileSource mapQuestOpenAerial = new MapQuestOpenAerialTileSource();

    comboBoxTileSource = new JComboBox<String>();
    comboBoxTileSource.setMaximumRowCount(18);

    comboBoxTileSource.addItem("OpenStreetMap");
    comboBoxTileSource.addItem("OpenCycleMap");
    comboBoxTileSource.addItem("Bing Aerial");
    // comboBoxTileSource.addItem("MapQuest-OSM");
    // comboBoxTileSource.addItem("MapQuest Open Aerial");

    comboBoxTileSource.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selected = (String) comboBoxTileSource.getSelectedItem();
        if (selected.equals("OpenStreetMap")) {
          mapPanel.setTileSource(openStreetMap);
        } else if (selected.equals("OpenCycleMap")) {
            mapPanel.setTileSource(openCycleMap);
        } else if (selected.equals("Bing Aerial")) {
          mapPanel.setTileSource(bingAerial);
//                } else if (selected.equals("MapQuest-OSM")) {
//                    mapPanel.setTileSource(mapQuestOsm);
//                } else if (selected.equals("MapQuest Open Aerial")) {
//                    mapPanel.setTileSource(mapQuestOpenAerial);
        }
      }
    });

    comboBoxTileSource.setFocusable(false);
    toolBarMain.add(comboBoxTileSource);
    comboBoxTileSource.setMaximumSize(comboBoxTileSource.getPreferredSize());

    /* LAT/LON INPUT/SEEKER
     * --------------------------------------------------------------------------------------------------------- */
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
          tglLatLonFocus.setSelected(false);
          tglLatLonFocus.setSelected(true);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          tglLatLonFocus.setSelected(false);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (tglLatLonFocus.isSelected()) {
          tglLatLonFocus.setSelected(false);
          tglLatLonFocus.setSelected(true);
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
          tglLatLonFocus.setSelected(false);
          tglLatLonFocus.setSelected(true);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          tglLatLonFocus.setSelected(false);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (tglLatLonFocus.isSelected()) {
          tglLatLonFocus.setSelected(false);
          tglLatLonFocus.setSelected(true);
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

    tglLatLonFocus = new JToggleButton("");
    tglLatLonFocus.setToolTipText("Focus on latitude/longitude");
    tglLatLonFocus.setIcon(new ImageIcon(GPXCreator.class.getResource("/com/gpxcreator/icons/crosshair.png")));
    tglLatLonFocus.setFocusable(false);
    tglLatLonFocus.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          deselectAllToggles(tglLatLonFocus);
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
            mapPanel.setDisplayPosition(p, (int) latDouble, (int) lonDouble, mapPanel.getZoom());
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
        if (tglLatLonFocus.isSelected() && !mouseOverLink) {
          int zoom = mapPanel.getZoom();
          int x = e.getX();
          int y = e.getY();
          Point mapCenter = mapPanel.getCenter();
          int xStart = mapCenter.x - mapPanel.getWidth() / 2;
          int yStart = mapCenter.y - mapPanel.getHeight() / 2;
          OsmMercator osmMercator = new OsmMercator();
          double lat = osmMercator.yToLat(yStart + y, zoom);
          double lon = osmMercator.xToLon(xStart + x, zoom);
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
    horizontalGlue.setMaximumSize(new Dimension(2, 0));
    horizontalGlue.setMinimumSize(new Dimension(2, 0));
    horizontalGlue.setPreferredSize(new Dimension(2, 0));
    toolBarMain.add(horizontalGlue);
    toolBarMain.add(tglLatLonFocus);

    frame.setVisible(true);
    frame.requestFocusInWindow();

    /* DEBUG / PROXY
     * --------------------------------------------------------------------------------------------------------- */

    // button for quick easy debugging
        /*JButton debug = new JButton("debug");
        debug.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // do something
            }
        });
        debug.setFocusable(false);
        toolBarMain.addSeparator();
        toolBarMain.add(debug);*/

  }

  /**
   * Creates a new GPX file and loads it into the application.
   */
  public void fileNew() {
    if (fileIOHappening) {
      return;
    }

    String name = (String) JOptionPane.showInputDialog(frame, "Please type a name for the new route:",
        "New route", JOptionPane.PLAIN_MESSAGE, null, null, null);
    if (name != null) {
      GPXFile gpxFile = new GPXFile(name);
      gpxFile.addRoute();

      mapPanel.addGPXFile(gpxFile);
      DefaultMutableTreeNode gpxFileNode = new DefaultMutableTreeNode(gpxFile);

      treeModel.insertNodeInto(gpxFileNode, root, root.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode(gpxFile.getRoutes().get(0)), gpxFileNode, 0);

      setActiveGPXObject((GPXObject) gpxFile);
      TreeNode[] pathToFileNode = treeModel.getPathToRoot(gpxFileNode);
      tree.setSelectionPath(new TreePath(pathToFileNode));
      tree.scrollRectToVisible(new Rectangle(0, 999999999, 1, 1));
    }
  }

  /**
   * Loads a GPX file into the application.
   */
  public void fileOpen() {
    if (fileIOHappening) {
      return;
    }

    int returnVal = chooserFileOpen.showOpenDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      fileOpened = chooserFileOpen.getSelectedFile();

      SwingWorker<Void, Void> fileOpenWorker = new SwingWorker<Void, Void>() {
        @Override
        public Void doInBackground() {
          boolean valid = GPXFile.validateGPXFile(fileOpened);
          if (!valid) {
            JOptionPane.showMessageDialog(frame,
                "<html>The selected file does not validate against the GPX schema version 1.1.<br>" +
                    "There is a chance that the file will not load properly.</html>",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
          }
          gpxFileOpened = new GPXFile(fileOpened);
          return null;
        }

        @Override
        protected void done() {
          mapPanel.addGPXFile(gpxFileOpened);

          DefaultMutableTreeNode gpxFileNode = new DefaultMutableTreeNode(gpxFileOpened);
          treeModel.insertNodeInto(gpxFileNode, root, root.getChildCount());
          if (gpxFileOpened.getWaypointGroup().getWaypoints().size() > 0) {
            DefaultMutableTreeNode wptsNode = new DefaultMutableTreeNode(gpxFileOpened.getWaypointGroup());
            treeModel.insertNodeInto(wptsNode, gpxFileNode, gpxFileNode.getChildCount());
          }
          for (Route route : gpxFileOpened.getRoutes()) {
            DefaultMutableTreeNode rteNode = new DefaultMutableTreeNode(route);
            treeModel.insertNodeInto(rteNode, gpxFileNode, gpxFileNode.getChildCount());
          }
          for (Track track : gpxFileOpened.getTracks()) {
            DefaultMutableTreeNode trkNode = new DefaultMutableTreeNode(track);
            treeModel.insertNodeInto(trkNode, gpxFileNode, gpxFileNode.getChildCount());
            for (WaypointGroup trackseg : track.getTracksegs()) {
              DefaultMutableTreeNode trksegNode = new DefaultMutableTreeNode(trackseg);
              treeModel.insertNodeInto(trksegNode, trkNode, trkNode.getChildCount());
            }
          }

          setActiveGPXObject((GPXObject) gpxFileOpened);
          TreeNode[] nodes = treeModel.getPathToRoot(gpxFileNode);
          tree.setSelectionPath(new TreePath(nodes));
          tree.scrollRectToVisible(new Rectangle(0, 999999999, 1, 1));

          setFileIOHappening(false);
          glassPane.setVisible(false);
          glassPaneStatus.setText("");
          glassPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          frame.repaint();
        }
      };
      glassPane.setVisible(true);
      glassPaneStatus.setText("opening file...");
      glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
      setFileIOHappening(true);
      frame.repaint();
      fileOpenWorker.execute();
    }
  }

  /**
   * Saves the active {@link GPXFile} to disk.
   */
  public void fileSave() {
    if (fileIOHappening) {
      return;
    }

    if (currSelection == null) {
      return;
    }

    while (!((GPXObject) currSelection.getUserObject()).isGPXFile()) {
      currSelection = (DefaultMutableTreeNode) currSelection.getParent();
    }
    TreeNode[] nodes = treeModel.getPathToRoot(currSelection);
    tree.setSelectionPath(new TreePath(nodes));

    int returnVal = chooserFileSave.showSaveDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      fileSave = chooserFileSave.getSelectedFile();
      String fileName = fileSave.getName();
      int lc = fileName.length() - 1;
      if (fileName.charAt(lc--) != 'x' || fileName.charAt(lc--) != 'p' ||
          fileName.charAt(lc--) != 'g' || fileName.charAt(lc) != '.') {
        String dir = fileSave.getParent();
        String newName = dir + "/" + fileName + ".gpx";
        fileSave = new File(newName);
      }
      if (fileSave.exists()) {
        int response = JOptionPane.showConfirmDialog(frame, "<html>" + fileSave.getName() +
                " already exists.<br>Do you want to replace it?</html>",
            "Confirm file overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
          return; // cancel the save operation
        }
      }

      SwingWorker<Void, Void> fileSaveWorker = new SwingWorker<Void, Void>() {
        @Override
        public Void doInBackground() {
          ((GPXFile) activeGPXObject).saveToGPXFile(fileSave);
          return null;
        }

        @Override
        protected void done() {
          setFileIOHappening(false);
          glassPane.setVisible(false);
          glassPaneStatus.setText("");
          glassPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
      };
      glassPane.setVisible(true);
      glassPaneStatus.setText("saving file...");
      glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
      setFileIOHappening(true);
      frame.repaint();
      fileSaveWorker.execute();
    }
  }

  /**
   * Removes the active {@link GPXObject} from its parent container.
   */
  public void deleteActiveGPXObject() {
    if (fileIOHappening) {
      return;
    }

    if (activeGPXObject != null) {
      DefaultMutableTreeNode currentNode = currSelection;
      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();
      TreeNode[] parentPath = treeModel.getPathToRoot(parentNode);
      Object parentObject = parentNode.getUserObject();

      DefaultMutableTreeNode gpxFileNode = currSelection;
      while (!((GPXObject) gpxFileNode.getUserObject()).isGPXFile()) {
        gpxFileNode = (DefaultMutableTreeNode) gpxFileNode.getParent();
      }
      GPXFile gpxFile = (GPXFile) gpxFileNode.getUserObject();

      treeModel.removeNodeFromParent(currentNode);

      if (activeGPXObject.isGPXFile()) { // this is a GPX file
        mapPanel.removeGPXFile((GPXFile) activeGPXObject);
        activeGPXObject = null;
        clearPropsTable();
      } else {
        if (activeGPXObject.isRoute()) { // this is a route
          ((GPXFile) parentObject).getRoutes().remove((Route) activeGPXObject);
        } else if (activeGPXObject.isTrack()) { // this is a track
          ((GPXFile) parentObject).getTracks().remove((Track) activeGPXObject);
        } else if (activeGPXObject.isWaypointGroup()) {
          WaypointGroup wptGrp = (WaypointGroup) currentNode.getUserObject();
          if (wptGrp.getWptGrpType() == WptGrpType.TRACKSEG) { // track seg
            ((Track) parentObject).getTracksegs().remove((WaypointGroup) currentNode.getUserObject());
          } else { // this is a top-level waypoint group
            ((GPXFile) parentObject).getWaypointGroup().getWaypoints().clear();
          }
        }
        gpxFile.updateAllProperties();
        tree.setSelectionPath(new TreePath(parentPath));
      }
      mapPanel.repaint();
    }
  }

  public GPXObject getActiveGPXObject() {
    return activeGPXObject;
  }

  public void setActiveGPXObject(GPXObject gpxObject) {
    activeGPXObject = gpxObject;
    gpxObject.setVisible(true);
    mapPanel.fitGPXObjectToPanel(gpxObject);
    updatePropsTable();
  }

  /**
   * Updates the data displayed in the properties table.
   */
  public void updatePropsTable() {
    tableModelProperties.setRowCount(0);

    if (activeGPXObject.isGPXFile()) { // this is a GPX file
      GPXFile gpxFile = (GPXFile) activeGPXObject;
      tableModelProperties.addRow(new Object[]{"GPX name", gpxFile.getName()});
      if (!gpxFile.getDesc().equals("")) {
        tableModelProperties.addRow(new Object[]{"GPX desc", gpxFile.getDesc()});
      }
      String timeString = "";
      if (gpxFile.getTime() != null) {
        Date time = gpxFile.getTime();
        timeString = sdf.format(time);
      }
      tableModelProperties.addRow(new Object[]{"GPX time", timeString});

      if (gpxFile.isGPXFileWithOneRouteOnly()) { // display single route details
        Route rte = gpxFile.getRoutes().get(0);
        propsDisplayRoute(rte);
      } else if (gpxFile.isGPXFileWithOneTracksegOnly()) { // display single track(seg) details
        Track trk = gpxFile.getTracks().get(0);
        propsDisplayTrack(trk, trk.getTracksegs().get(0));
      } else { // display file top-level container info
        if (gpxFile.getWaypointGroup().getNumPts() > 0) {
          tableModelProperties.addRow(new Object[]{"waypoints", gpxFile.getWaypointGroup().getNumPts()});
        }
        if (gpxFile.getRoutes().size() > 0) {
          tableModelProperties.addRow(new Object[]{"routes", gpxFile.getRoutes().size()});
        }
        if (gpxFile.getTracks().size() > 0) {
          tableModelProperties.addRow(new Object[]{"tracks", gpxFile.getTracks().size()});
          if (gpxFile.getTracks().size() == 1) {
            propsDisplayTrack(gpxFile.getTracks().get(0), null);
          }
        }
      }

    } else { // this is not a GPX file
      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currSelection.getParent();
      Object parentObject = parentNode.getUserObject();

      if (activeGPXObject.isRoute()) { /// this is a route
        Route rte = (Route) activeGPXObject;
        propsDisplayRoute(rte);
      } else if (activeGPXObject.isTrack()) { // this is a track
        Track trk = (Track) activeGPXObject;
        if (trk.getTracksegs().size() == 1) { // display single trackseg details
          propsDisplayTrack(trk, trk.getTracksegs().get(0));
        } else { // display track container info
          propsDisplayTrack(trk, null);
        }
      } else if (activeGPXObject.isWaypointGroup()) {
        WaypointGroup wptGrp = (WaypointGroup) activeGPXObject;
        if (wptGrp.getWptGrpType() == WptGrpType.WAYPOINTS) { // this is a top level waypoint collection
          tableModelProperties.addRow(new Object[]{"waypoints", wptGrp.getWaypoints().size()});
          tableModelProperties.addRow(
              new Object[]{"min elevation", String.format("%.0f ft", wptGrp.getEleMinFeet())});
          tableModelProperties.addRow(
              new Object[]{"max elevation", String.format("%.0f ft", wptGrp.getEleMaxFeet())});

        } else if (wptGrp.getWptGrpType() == WptGrpType.TRACKSEG) { // this is a trackseg
          Track trk = (Track) parentObject;
          propsDisplayTrack(trk, wptGrp);
        }
      }
    }
    updatePropTableWidths();
  }

  /**
   * Displays details for a {@link Route} in the properties table.
   */
  public void propsDisplayRoute(Route rte) {
    if (!rte.getName().equals("")) {
      tableModelProperties.addRow(new Object[]{"route name", rte.getName()});
    }
    if (!rte.getDesc().equals("")) {
      tableModelProperties.addRow(new Object[]{"route desc", rte.getDesc()});
    }
    if (rte.getNumber() != 0) {
      tableModelProperties.addRow(new Object[]{"route number", rte.getNumber()});
    }
    if (!rte.getType().equals("")) {
      tableModelProperties.addRow(new Object[]{"route type", rte.getType()});
    }
    propsDisplayPathDetails(Arrays.asList(rte.getPath()));
  }

  /**
   * Displays details for a track segment in the properties table.
   */
  public void propsDisplayTrack(Track trk, WaypointGroup maybeNullTrackseg) {
    if (!trk.getName().equals("")) {
      tableModelProperties.addRow(new Object[]{"track name", trk.getName()});
    }
    if (!trk.getDesc().equals("")) {
      tableModelProperties.addRow(new Object[]{"track desc", trk.getDesc()});
    }
    if (trk.getNumber() != 0) {
      tableModelProperties.addRow(new Object[]{"track number", trk.getNumber()});
    }
    if (!trk.getType().equals("")) {
      tableModelProperties.addRow(new Object[]{"track type", trk.getType()});
    }
    propsDisplayPathDetails(
        maybeNullTrackseg == null ?
            trk.getTracksegs() :
            Arrays.asList(maybeNullTrackseg));
  }

  /**
   * Displays details common to all path types ({@link WptGrpType#ROUTE} and {@link WptGrpType#TRACKSEG}).
   */
  public void propsDisplayPathDetails(List<WaypointGroup> paths) {
    int numPts = 0;
    long duration = 0;
    double lengthMiles = 0;
    double lengthAscendMiles = 0;
    double lengthDescendMiles = 0;
    double maxSpeedMph = 0;
    double eleMinFeet = Double.MAX_VALUE;
    double eleMaxFeet = Double.MIN_VALUE;
    double grossRiseFeet = 0;
    double grossFallFeet = 0;
    long riseTime = 0;
    long fallTime = 0;
    for (WaypointGroup path : paths) {
      numPts += path.getNumPts();
      duration += path.getDuration();
      lengthMiles += path.getLengthMiles();
      lengthAscendMiles += path.getLengthAscendMiles();
      lengthDescendMiles += path.getLengthDescendMiles();
      maxSpeedMph = Math.max(maxSpeedMph, path.getMaxSpeedMph());
      eleMinFeet = Math.min(eleMinFeet, path.getEleMinFeet());
      eleMaxFeet = Math.max(eleMaxFeet, path.getEleMaxFeet());
      grossRiseFeet += path.getGrossRiseFeet();
      grossFallFeet += path.getGrossFallFeet();
      riseTime += path.getRiseTime();
      fallTime += path.getFallTime();
    }

    tableModelProperties.addRow(new Object[]{"# of pts", numPts});

    Waypoint start = paths.get(0).getStart();
    Waypoint end = paths.get(paths.size() - 1).getEnd();
    if (start != null && end != null) {
      Date startTimeDate = start.getTime();
      Date endTimeDate = end.getTime();
      if (startTimeDate != null && endTimeDate != null) {
        String startTimeString = "";
        String endTimeString = "";
        startTimeString = sdf.format(startTimeDate);
        endTimeString = sdf.format(endTimeDate);
        tableModelProperties.addRow(new Object[]{"start time", startTimeString});
        tableModelProperties.addRow(new Object[]{"end time", endTimeString});
      }
    }
    long hours = duration / 3600000;
    long minutes = (duration - hours * 3600000) / 60000;
    long seconds = (duration - hours * 3600000 - minutes * 60000) / 1000;
    if (duration != 0) {
      tableModelProperties.addRow(new Object[]{"duration", hours + "hr " + minutes + "min " + seconds + "sec"});
    }
    tableModelProperties.addRow(new Object[]{"length", String.format("%.2f mi", lengthMiles)});
    tableModelProperties.addRow(new Object[]{"length ascend", String.format("%.2f mi", lengthAscendMiles)});
    tableModelProperties.addRow(new Object[]{"length descend", String.format("%.2f mi", lengthDescendMiles)});

    double avgSpeedMph = (lengthMiles / duration) * 3600000;
    if (Double.isNaN(avgSpeedMph) || Double.isInfinite(avgSpeedMph)) {
      avgSpeedMph = 0;
    }
    if (avgSpeedMph != 0) {
      tableModelProperties.addRow(new Object[]{"avg speed", String.format("%.1f mph", avgSpeedMph)});
    }
    if (maxSpeedMph != 0) {
      tableModelProperties.addRow(new Object[]{"max speed", String.format("%.1f mph", maxSpeedMph)});
    }

    tableModelProperties.addRow(
        new Object[]{"elevation (start)", String.format("%.0f ft", paths.get(0).getEleStartFeet())});
    tableModelProperties.addRow(
        new Object[]{"elevation (end)", String.format("%.0f ft", paths.get(paths.size() - 1).getEleEndFeet())});
    tableModelProperties.addRow(
        new Object[]{"min elevation", String.format("%.0f ft", eleMinFeet)});
    tableModelProperties.addRow(
        new Object[]{"max elevation", String.format("%.0f ft", eleMaxFeet)});
    tableModelProperties.addRow(new Object[]{"gross rise", String.format("%.0f ft", grossRiseFeet)});
    tableModelProperties.addRow(new Object[]{"gross fall", String.format("%.0f ft", grossFallFeet)});

    hours = riseTime / 3600000;
    minutes = (riseTime - hours * 3600000) / 60000;
    seconds = (riseTime - hours * 3600000 - minutes * 60000) / 1000;
    if (riseTime != 0) {
      tableModelProperties.addRow(new Object[]{"rise time", hours + "hr " + minutes + "min " + seconds + "sec"});
    }
    hours = fallTime / 3600000;
    minutes = (fallTime - hours * 3600000) / 60000;
    seconds = (fallTime - hours * 3600000 - minutes * 60000) / 1000;
    if (fallTime != 0) {
      tableModelProperties.addRow(new Object[]{"fall time", hours + "hr " + minutes + "min " + seconds + "sec"});
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
      tableModelProperties.addRow(new Object[]{"avg rise speed", String.format("%.0f ft/hr", avgRiseSpeedFph)});
    }
    if (avgFallSpeedFph != 0) {
      tableModelProperties.addRow(new Object[]{"avg fall speed", String.format("%.0f ft/hr", avgFallSpeedFph)});
    }

    tableModelProperties.addRow(
        new Object[]{
            "avg grade ascend", String.format("%.1f%s", grossRiseFeet / (lengthAscendMiles * 5280) * 100, "%")});
    tableModelProperties.addRow(
        new Object[]{
            "avg grade descend", String.format("%.1f%s", grossFallFeet / (lengthDescendMiles * 5280) * 100, "%")});
  }

  /**
   * Dynamically adjusts the widths of the columns in the properties table for optimal display.
   */
  public void updatePropTableWidths() {
    int nameWidth = 0;
    for (int row = 0; row < tableProperties.getRowCount(); row++) {
      TableCellRenderer renderer = tableProperties.getCellRenderer(row, 0);
      Component comp = tableProperties.prepareRenderer(renderer, row, 0);
      nameWidth = Math.max(comp.getPreferredSize().width, nameWidth);
    }
    nameWidth += tableProperties.getIntercellSpacing().width;
    nameWidth += 10;
    tableProperties.getColumn("Name").setMaxWidth(nameWidth);
    tableProperties.getColumn("Name").setMinWidth(nameWidth);
    tableProperties.getColumn("Name").setPreferredWidth(nameWidth);

    int valueWidth = 0;
    for (int row = 0; row < tableProperties.getRowCount(); row++) {
      TableCellRenderer renderer = tableProperties.getCellRenderer(row, 1);
      Component comp = tableProperties.prepareRenderer(renderer, row, 1);
      valueWidth = Math.max(comp.getPreferredSize().width, valueWidth);
    }
    valueWidth += tableProperties.getIntercellSpacing().width;
    int tableWidth = valueWidth + nameWidth;
    if (scrollPaneProperties.getVerticalScrollBar().isVisible()) {
      tableWidth += scrollPaneProperties.getVerticalScrollBar().getWidth();
    }
    if (tableWidth > scrollPaneProperties.getWidth()) {
      tableProperties.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      valueWidth += 10;
    } else {
      tableProperties.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      valueWidth = scrollPaneProperties.getWidth() + nameWidth;
    }
    tableProperties.getColumn("Value").setMinWidth(valueWidth);
    tableProperties.getColumn("Value").setPreferredWidth(valueWidth);
  }

  /**
   * Clears the properties table.
   */
  public void clearPropsTable() {
    tableModelProperties.setRowCount(0);
  }

  /**
   * Common function used by multiple mouse listeners.  An "active waypoint" is one that is moused over
   * as a candidate for an action (for example, deletion or usage as a splitting point).
   */
  public void updateActiveWpt(MouseEvent e) {
    if (tglDelPoints.isSelected() || tglSplitTrackseg.isSelected()) {
      updateActiveWptGrp();
      activeWpt = null;
      if (activeWptGrp != null) {
        mapPanel.setActiveColor(activeWptGrp.getColor());
        Point p = e.getPoint();
        boolean found = false;
        double minDistance = Double.MAX_VALUE;

        int start, end;
        if (tglSplitTrackseg.isSelected()) { // don't allow splitting at endpoints
          start = 1;
          end = activeWptGrp.getNumPts() - 1;
        } else {
          start = 0;
          end = activeWptGrp.getNumPts();
        }
        for (int i = start; i < end; i++) {
          Waypoint wpt = activeWptGrp.getWaypoints().get(i);
          Point w = mapPanel.getMapPosition(wpt.getLat(), wpt.getLon(), false);
          int dx = w.x - p.x;
          int dy = w.y - p.y;
          double distance = Math.sqrt(dx * dx + dy * dy);
          if (distance < 10 && distance < minDistance) {
            minDistance = distance;
            activeWpt = wpt;
            mapPanel.setShownPoint(w);
            found = true;
          }
        }

        if (!found) {
          activeWptGrp = null;
          activeWpt = null;
          mapPanel.setShownPoint(null);
        }
        mapPanel.repaint();
      } else if (activeTrack != null) {
        mapPanel.setActiveColor(activeTrack.getColor());
        Point p = e.getPoint();
        boolean found = false;
        double minDistance = Double.MAX_VALUE;

        int start, end;
        for (WaypointGroup trackseg : activeTrack.getTracksegs()) {
          if (tglSplitTrackseg.isSelected()) { // don't allow splitting at endpoints
            start = 1;
            end = trackseg.getNumPts() - 1;
          } else {
            start = 0;
            end = trackseg.getNumPts();
          }
          for (int i = start; i < end; i++) {
            Waypoint wpt = trackseg.getWaypoints().get(i);
            Point w = mapPanel.getMapPosition(wpt.getLat(), wpt.getLon(), false);
            int dx = w.x - p.x;
            int dy = w.y - p.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < 10 && distance < minDistance) {
              minDistance = distance;
              activeWpt = wpt;
              mapPanel.setShownPoint(w);
              found = true;
            }
          }
        }

        if (!found) {
          activeWptGrp = null;
          activeWpt = null;
          mapPanel.setShownPoint(null);
        }
        mapPanel.repaint();
      } else {
        tglDelPoints.setSelected(false);
        tglSplitTrackseg.setSelected(false);
      }
    }
  }

  /**
   * Determines which {@link GPXObject}s are active and sets the appropriate variables.
   */
  public void updateActiveWptGrp() {
    activeWptGrp = null;
    activeTrack = null;
    activeTracksegNode = null;
    if (activeGPXObject.isWaypointGroup()) {
      activeWptGrp = (WaypointGroup) activeGPXObject;
      if (activeWptGrp.getWptGrpType() == WptGrpType.TRACKSEG) {
        activeTracksegNode = (DefaultMutableTreeNode) currSelection;
      }
    } else if (activeGPXObject.isRoute()) {
      activeWptGrp = ((Route) activeGPXObject).getPath();
    } else if (activeGPXObject.isTrackWithOneSeg()) {
      Track trk = (Track) activeGPXObject;
      activeWptGrp = trk.getTracksegs().get(0);
      activeTracksegNode = (DefaultMutableTreeNode) currSelection.getFirstChild();
    } else if (activeGPXObject.isTrack()) {
      activeTrack = (Track) activeGPXObject;
      activeTrackNode = currSelection;
    } else if (activeGPXObject.isGPXFile()) {
      GPXFile gpxFile = (GPXFile) activeGPXObject;
      if (gpxFile.isGPXFileWithOneRouteOnly()) { // one route only
        activeWptGrp = gpxFile.getRoutes().get(0).getPath();
      } else if (gpxFile.isGPXFileWithOneTracksegOnly()) { // one trackseg only
        Track trk = gpxFile.getTracks().get(0);
        activeWptGrp = trk.getTracksegs().get(0);

        DefaultMutableTreeNode trackNode = null;
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> children = currSelection.children();
        while (children.hasMoreElements()) {
          trackNode = (DefaultMutableTreeNode) children.nextElement();
          if (((GPXObject) trackNode.getUserObject()).isTrackseg()) {
            break;
          }
        }
        activeTracksegNode = (DefaultMutableTreeNode) trackNode.getFirstChild();
      } else if (gpxFile.isGPXFileWithOneTrackOnly()) {
        activeTrack = gpxFile.getTracks().get(0);
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> children = currSelection.children();
        while (children.hasMoreElements()) {
          activeTrackNode = (DefaultMutableTreeNode) children.nextElement();
          if (((GPXObject) activeTrackNode.getUserObject()).isTrack()) {
            break;
          }
        }
      }
    }
  }

  /**
   * Registers a list of {@link JToggleButton}s and deselects them all, optionally leaving one selected.
   * Used to prevent multiple toggles from being selected simultaneously.
   */
  public void deselectAllToggles(JToggleButton exceptThisOne) {
    List<JToggleButton> toggles = new ArrayList<JToggleButton>();
    toggles.add(tglAddPoints);
    toggles.add(tglDelPoints);
    toggles.add(tglSplitTrackseg);
    toggles.add(tglLatLonFocus);
    toggles.add(tglPathFinder);

    for (JToggleButton toggle : toggles) {
      if (toggle != exceptThisOne && toggle.isSelected()) {
        toggle.setSelected(false);
      }
    }
  }

  /**
   * Dynamically enables/disables certain toolbar buttons dependent on which type of {@link GPXObject} is active
   * and what operations are allowed on that type of element.
   */
  public void updateButtonVisibility() {
    btnFileNew.setEnabled(true);
    btnFileOpen.setEnabled(true);
    btnFileSave.setEnabled(false);
    btnObjectDelete.setEnabled(false);
    tglAddPoints.setEnabled(false);
    tglDelPoints.setEnabled(false);
    tglSplitTrackseg.setEnabled(false);
    btnCorrectEle.setEnabled(false);
    btnEleChart.setEnabled(false);
    btnSpeedChart.setEnabled(false);
    btnEditProperties.setEnabled(false);
    tglPathFinder.setEnabled(false);

    if (currSelection != null) {
      btnFileSave.setEnabled(true);
      btnObjectDelete.setEnabled(true);
      GPXObject o = activeGPXObject;

      if (o.isRoute() || o.isWaypoints() || o.isGPXFileWithOneRoute() || o.isGPXFileWithNoRoutes()) {
        tglAddPoints.setEnabled(true);
      }
      if (o.isTrackseg() || o.isTrackWithOneSeg() || o.isRoute() || o.isWaypoints()
          || o.isGPXFileWithOneTracksegOnly() || o.isGPXFileWithOneRouteOnly()) {
        tglDelPoints.setEnabled(true);
        btnCorrectEle.setEnabled(true);
        btnEleChart.setEnabled(true);
      }
      if (o.isTrackseg() || o.isTrackWithOneSeg() || o.isGPXFileWithOneTracksegOnly()
          || o.isTrack() || o.isGPXFileWithOneTrackOnly()) {
        tglSplitTrackseg.setEnabled(true);
      }
      if (o.isTrackseg() || o.isTrackWithOneSeg() || o.isGPXFileWithOneTracksegOnly()
          || o.isTrack() || o.isGPXFileWithOneTrackOnly()) {
        btnSpeedChart.setEnabled(true);
      }
      if (o.isGPXFile() || o.isRoute() || o.isTrack()) {
        btnEditProperties.setEnabled(true);
      }
      if (o.isRoute() || o.isGPXFileWithOneRoute() || o.isGPXFileWithNoRoutes()) {
        tglPathFinder.setEnabled(true);
      }
    }

    if (fileIOHappening) {
      btnFileNew.setEnabled(false);
      btnFileOpen.setEnabled(false);
      btnFileSave.setEnabled(false);
      btnObjectDelete.setEnabled(false);
    }
  }

  /**
   * Builds the selected chart type and displays the new window frame.
   */
  public void buildChart(String chartName, String iconPath) {
    if (activeGPXObject != null) {
      updateActiveWptGrp();
      if (activeWptGrp != null) {
        DefaultMutableTreeNode gpxFileNode = currSelection;
        while (!((GPXObject) gpxFileNode.getUserObject()).isGPXFile()) {
          gpxFileNode = (DefaultMutableTreeNode) gpxFileNode.getParent();
        }
        GPXFile gpxFile = (GPXFile) gpxFileNode.getUserObject();
        JFrame f = null;
        if (chartName.equals("Elevation profile")) {
          f = new ElevationChart(chartName, gpxFile.getName(), activeWptGrp);
        } else if (chartName.equals("Speed profile")) {
          f = new SpeedChart(chartName, gpxFile.getName(), activeWptGrp);
        } else {
          return; // invalid chart name given
        }
        InputStream in = GPXCreator.class.getResourceAsStream(iconPath);
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

  /**
   * Displays the edit properties dialog and saves the user-selected values to the active {@link GPXObject}.
   */
  public void editProperties() {
    EditPropsDialog dlg = new EditPropsDialog(frame, "Edit properties", activeGPXObject);
    dlg.setVisible(true);
    if (dlg.getName() != null) {
      activeGPXObject.setName(dlg.getName());
    }
    if (dlg.getDesc() != null) {
      activeGPXObject.setDesc(dlg.getDesc());
    }
    if (activeGPXObject.isRoute()) {
      if (dlg.getGPXType() != null) {
        ((Route) activeGPXObject).setType(dlg.getGPXType());
      }
      if (dlg.getNumber() != null) {
        ((Route) activeGPXObject).setNumber(dlg.getNumber());
      }
    }
    if (activeGPXObject.isTrack()) {
      if (dlg.getGPXType() != null) {
        ((Track) activeGPXObject).setType(dlg.getGPXType());
      }
      if (dlg.getNumber() != null) {
        ((Track) activeGPXObject).setNumber(dlg.getNumber());
      }
    }
    treeModel.nodeChanged(currSelection);
    updatePropsTable();
  }

  /**
   * Sets a flag to synchronize I/O operations with {@link GPXFile}s.  Must be called before and after each I/O.
   */
  public void setFileIOHappening(boolean happening) {
    fileIOHappening = happening;
    updateButtonVisibility();
  }
}
