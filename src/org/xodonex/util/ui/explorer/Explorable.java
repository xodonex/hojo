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
import java.util.Collection;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A view for tree-model based data.
 */
public interface Explorable {

    /**
     * @return the tree model to be used as the basis of the exploration
     */
    public TreeModel getModel();

    /**
     * @return true iff the explorer tree should be editable
     */
    public boolean isTreeEditable();

    /**
     * @return a collection of actions which should be accessible from the
     *         explorer. It is permissible to return null here.
     */
    public Collection getGlobalActions();

    /**
     * Creates and/or configures the component to be used when no item is
     * selected.
     *
     * @return the created view
     */
    public Component getEmptyView();

    /**
     * Creates and/or configures the component to be used displaying a selected
     * item.
     *
     * @param item
     *            the path into the tree model at which the item to be explored
     *            is found
     * @return the component representing the given item
     */
    public Component getView(TreePath item);

    /**
     * This method is invoked from the explorer for every explored item which is
     * to be rendered.
     *
     * @param renderer
     *            the component used to render the item. The properties of this
     *            component may be modified by the explorable in order to obtain
     *            a custom rendition, but are set to their defaults prior to
     *            this call.
     * @param value
     *            the value to be rendered. This will be an item obtained from
     *            the tree model.
     * @param selected
     *            whether the item should be rendered as selected.
     * @param expanded
     *            whether the item is an expanded branch
     * @param hasFocus
     *            whether the item currently has focus.
     */
    public void render(DefaultTreeCellRenderer renderer, Object value,
            boolean selected, boolean expanded, boolean hasFocus);

    /**
     * @param item
     *            a model item
     * @return a popup menu to be used for a model item.
     */
    public JPopupMenu createPopupMenu(TreePath item);

}
