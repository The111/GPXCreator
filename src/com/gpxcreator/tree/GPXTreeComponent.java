package com.gpxcreator.tree;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GPXTreeComponent extends JPanel {
    private JLabel visIcon;
    private JLabel wptIcon;
    private JLabel colorIcon;
    private JLabel text;
    
    public GPXTreeComponent(JLabel visIcon, JLabel colorIcon, JLabel wptIcon, JLabel text) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.visIcon = visIcon;
        this.wptIcon = wptIcon;
        this.colorIcon = colorIcon;
        this.text = text;
        this.add(visIcon);
        this.add(wptIcon);
        this.add(colorIcon);
        this.add(text);
    }

    public JLabel getVisIcon() {
        return visIcon;
    }

    public JLabel getWptIcon() {
        return wptIcon;
    }

    public JLabel getColorIcon() {
        return colorIcon;
    }
    
    public JLabel getText() {
        return text;
    }
}
