package com.gpxcreator.tree;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EventObject;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.gpxcreator.GPXCreator;
import com.gpxcreator.gpxpanel.GPXObject;

public class GPXTreeEditor implements TreeCellEditor {

    private GPXTreeComponentFactory factory;
    private GPXTreeComponent editorComponent;
    
    private DefaultTreeModel treeModel;
    private JTree tree;
    private DefaultMutableTreeNode node;
    private GPXObject gpxObject;
    private JColorChooser colorChooser;
    private JDialog dialog;
    
    public GPXTreeEditor() {
        factory = new GPXTreeComponentFactory();
        
        colorChooser = new JColorChooser();
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gpxObject.setColor(colorChooser.getColor());
                treeModel.nodeChanged((TreeNode) treeModel.getRoot());
            }
        };
        dialog = JColorChooser.createDialog(editorComponent, "Choose a Color", true, colorChooser, al, null);
        BufferedImage img = null;
        InputStream in = GPXCreator.class.getResourceAsStream("/com/gpxcreator/icons/color-palette.png");
        try {
            img = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setIconImage(img);
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row) {
        this.tree = tree;
        treeModel = (DefaultTreeModel) tree.getModel();
        node = (DefaultMutableTreeNode) value; 
        Object userObject = node.getUserObject();
        this.gpxObject = (GPXObject) userObject;

        editorComponent = factory.getComponent(tree, value, isSelected, expanded, leaf, row, false);
        editorComponent.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                GPXTreeEditor.this.stopCellEditing();
                int x = e.getX();
                int y = e.getY();
                if (x >= 0 && x <= 8) {
                    if (y >= 4 && y <= 12) {
                        gpxObject.setVisible(!gpxObject.isVisible());
                        treeModel.nodeChanged(node);
                    }
                } else if (x >= 13 && x <= 18) {
                    if (y >= 4 && y <= 12) {
                        gpxObject.setWptsVisible(!gpxObject.isWptsVisible());
                        treeModel.nodeChanged(node);
                    }
                } else if (x >= 23 && x <= 31) {
                    if (y >= 4 && y <= 12) {
                        colorChooser.setColor(gpxObject.getColor());
                        dialog.setVisible(true);
                    }
                } else if (x > 36) {
                    TreeNode[] nodes = treeModel.getPathToRoot(node);
                    tree.clearSelection();
                    tree.setSelectionPath(new TreePath(nodes));
                }
            }
        });
        return editorComponent;
    }

    @Override public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean stopCellEditing() {
        tree.cancelEditing();
        return false;
    }

    @Override public Object getCellEditorValue() {return null;}
    @Override public void cancelCellEditing() {}
    @Override public void addCellEditorListener(CellEditorListener l) {}
    @Override public void removeCellEditorListener(CellEditorListener l) {}
}
