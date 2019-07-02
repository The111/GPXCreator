package com.gpxcreator.tree;

import javax.swing.*;

/**
 * The tree component rendered by a {@link GPXTreeRenderer}.
 *
 * @author hooverm
 */
@SuppressWarnings("serial")
public class GPXTreeComponent extends JPanel {

  /**
   * Default constructor.
   *
   * @param visIcon   The visibility icon.
   * @param colorIcon The color change icon.
   * @param wptIcon   The waypoint display icon.
   * @param text      The text description of the GPX element.
   */
  public GPXTreeComponent(JLabel visIcon, JLabel colorIcon, JLabel wptIcon, JLabel text) {
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(visIcon);
    this.add(wptIcon);
    this.add(colorIcon);
    this.add(text);
  }
}
