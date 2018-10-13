// Copyright 1998,1999,2000,2001,2018, Henrik Lauritzen.
/*
    This file is part of the Hojo interpreter & toolkit.

    The Hojo interpreter & toolkit is free software: you can redistribute it
    and/or modify it under the terms of the GNU Affero General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    The Hojo interpreter & toolkit is distributed in the hope that it will
    be useful or (at least have historical interest),
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this file.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.xodonex.util.ui.explorer;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.xodonex.util.ui.GuiResource;

/**
 * The Explorer provides a GUI for an {@link Explorable} component.
 */
public class Explorer extends JSplitPane {

    private static final long serialVersionUID = 1L;

    private Explorable _e;

    private GuiResource _rsrc;
    private JScrollPane _right;

    public Explorer(GuiResource rsrc) {
        super(HORIZONTAL_SPLIT);

        if ((_rsrc = rsrc) == null) {
            throw new NullPointerException();
        }

        setDividerSize(4);
        setRightComponent(_right = new JScrollPane());
    }

    /**
     * Begin exploring the given explorable item.
     *
     * @param e
     *            the object to be explored
     */
    public synchronized void explore(Explorable e) {
        _e = e;
        JTree jt = new JTree(e.getModel());
        Collection as = e.getGlobalActions();

        if (as == null || as.size() == 0) {
            setLeftComponent(new JScrollPane(jt));
        }
        else {
            JSplitPane split = new JSplitPane(VERTICAL_SPLIT);
            JPanel p = new JPanel();

            for (Iterator i = as.iterator(); i.hasNext();) {
                Action a = (Action)i.next();
                if (a == null) {
                    p.add(Box.createHorizontalStrut(4));
                }
                else {
                    p.add(new JButton(a));
                }
            }

            split.setDividerSize(2);
            split.setRightComponent(p);
            split.setLeftComponent(new JScrollPane(jt));
            split.setResizeWeight(1.0);
            setLeftComponent(split);
        }

        configureTree(_rsrc, jt, e);
        setView(e.getEmptyView());
    }

    /**
     * @return the currently explored item, or null if nothing is currently
     *         being explored.
     */
    public Explorable getItem() {
        return _e;
    }

    /**
     * This method is responsible for configuration of the explorer tree. The
     * default implementation sets up a mouse listener which produces popup
     * menus, a selection listener which controls the right view component, a
     * cell renderer which allows the explorable to customize its items, and
     * registers the tree with the tool tip manager.
     *
     * @param rsrc
     *            the resource for creating the view
     * @param tree
     *            the tree view for selection/exploration
     * @param e
     *            the component to be browsed/explored
     */
    protected void configureTree(GuiResource rsrc, final JTree tree,
            final Explorable e) {
        // create a popup menu on right click or the system popup trigger
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // verify that a right-click or popup trigger event occurred.
                if (!(e.isPopupTrigger() || ((e.getModifiers() &
                        InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK))) {
                    return;
                }

                // ensure that the selected item was clicked
                Point p = e.getPoint();
                TreePath tp = tree.getClosestPathForLocation(p.x, p.y);
                if (tp.getPathCount() == 0 ||
                        !tp.equals(tree.getSelectionPath())) {
                    return;
                }

                JPopupMenu m = _e.createPopupMenu(tp);
                if (m != null) {
                    m.show(tree, p.x, p.y);
                }
            }
        });

        // update the right-hand view on change of selection
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                setView(tse.isAddedPath() ? e.getView(tse.getPath())
                        : e.getEmptyView());
            }
        });

        // allow custom cell rendition
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value,
                        selected, expanded, leaf, row, hasFocus);
                _e.render(this, value, selected, expanded, hasFocus);
                return this;
            }

        });

        // ensure that tool tips can be shown
        ToolTipManager.sharedInstance().registerComponent(tree);
    }

    /**
     * This method is responsible for showing an explored component.
     *
     * @param comp
     *            the component to be shown
     */
    protected void setView(Component comp) {
        _right.setViewportView(comp);
    }

}
