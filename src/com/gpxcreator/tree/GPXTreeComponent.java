package com.gpxcreator.tree;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GPXTreeComponent extends JPanel {
    
    public GPXTreeComponent(JLabel visIcon, JLabel colorIcon, JLabel wptIcon, JLabel text) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(visIcon);
        this.add(wptIcon);
        this.add(colorIcon);
        this.add(text);
    }
}
