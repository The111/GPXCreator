package com.gpxcreator.tree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

public class GPXTreeRenderer implements TreeCellRenderer {
    
    private GPXTreeComponentFactory factory;
    
    public GPXTreeRenderer() {
        factory = new GPXTreeComponentFactory();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        return factory.getComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
