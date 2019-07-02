package com.gpxcreator.tree;

import com.gpxcreator.GPXCreator;
import com.gpxcreator.gpxpanel.GPXObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * A factory class to create {@link GPXTreeComponent}s.
 *
 * @author hooverm
 */
public class GPXTreeComponentFactory {

  private static ImageIcon visible;
  private static ImageIcon invisible;
  private static ImageIcon wptShow;
  private static ImageIcon wptHide;
  private static final Font BOLD = new Font("Tahoma", Font.BOLD, 11);
  private static final Font PLAIN = new Font("Tahoma", Font.PLAIN, 11);
  private static boolean boldSelectionStyle = false;

  /**
   * Default constructor for the factory.
   */
  public GPXTreeComponentFactory() {
    try {
      visible = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
          "/com/gpxcreator/icons/tree-visible.png")));
      invisible = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
          "/com/gpxcreator/icons/tree-invisible.png")));
      wptShow = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
          "/com/gpxcreator/icons/tree-wpt-show.png")));
      wptHide = new ImageIcon(ImageIO.read(GPXCreator.class.getResourceAsStream(
          "/com/gpxcreator/icons/tree-wpt-hide.png")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Creates a new {@link GPXTreeComponent}.
   */
  public GPXTreeComponent getComponent(JTree tree, Object value,
                                       boolean selected, boolean expanded, boolean leaf,
                                       int row, boolean hasFocus) {
    JLabel visIcon = new JLabel();
    visIcon.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    visIcon.setAlignmentY(JLabel.CENTER_ALIGNMENT);
    visIcon.setBorder(new EmptyBorder(0, 0, 0, 4));

    JLabel wptIcon = new JLabel();
    wptIcon.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    wptIcon.setAlignmentY(JLabel.CENTER_ALIGNMENT);
    wptIcon.setBorder(new EmptyBorder(0, 0, 0, 4));

    JLabel colorIcon = new JLabel();
    colorIcon.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    colorIcon.setAlignmentY(JLabel.CENTER_ALIGNMENT);

    JLabel text = new JLabel();

    GPXTreeComponent comp = new GPXTreeComponent(visIcon, colorIcon, wptIcon, text);

    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
    if (userObject instanceof GPXObject) {
      GPXObject gpxObject = (GPXObject) userObject;
      text.setText(gpxObject.toString());
      if (gpxObject.isVisible()) {
        visIcon.setIcon(visible);
      } else {
        visIcon.setIcon(invisible);
      }
      if (gpxObject.isWptsVisible()) {
        wptIcon.setIcon(wptShow);
      } else {
        wptIcon.setIcon(wptHide);
      }
      colorIcon.setIcon(new GPXColorIcon(gpxObject.getColor()));
      colorIcon.setOpaque(true);
      colorIcon.setBorder(new CompoundBorder(
          new EmptyBorder(0, 0, 0, 4),
          new LineBorder(Color.black, 1, false)));
      colorIcon.setBackground(Color.white);
    }

    text.setFont(PLAIN);
    text.setOpaque(true);
    if (selected) {
      text.setBackground(new Color(209, 230, 255));
      text.setBorder(new LineBorder(new Color(132, 172, 221), 1, false));
      if (boldSelectionStyle) {
        text.setFont(BOLD);
      }
    } else {
      text.setBackground(Color.white);
      text.setBorder(new LineBorder(Color.white, 1, false));
    }
    comp.setFocusable(false);
    comp.setBackground(Color.white);
    return comp;
  }

  /**
   * A square color icon for display in a {@link GPXTreeComponent}.
   */
  public class GPXColorIcon implements Icon {
    private Color color;
    private static final int SIZE = 7;

    public GPXColorIcon(Color color) {
      this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setColor(color);
      g2d.fillRect(x, y, SIZE, SIZE);
    }

    @Override
    public int getIconWidth() {
      return SIZE;
    }

    @Override
    public int getIconHeight() {
      return SIZE;
    }
  }
}
