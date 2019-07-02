package com.gpxcreator.tree;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * The standard cell renderer for a {@link GPXTree}.
 *
 * @author hooverm
 */
public class GPXTreeRenderer implements TreeCellRenderer {

  private GPXTreeComponentFactory factory;

  /**
   * Default constructor.
   */
  public GPXTreeRenderer() {
    factory = new GPXTreeComponentFactory();
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
   */
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                boolean selected, boolean expanded, boolean leaf, int row,
                                                boolean hasFocus) {
    return factory.getComponent(tree, value, selected, expanded, leaf, row, hasFocus);
  }
}
