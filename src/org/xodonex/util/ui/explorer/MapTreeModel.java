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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.xodonex.util.StringUtils;
import org.xodonex.util.beans.GenericBean;

/**
 * The MapTreeModel is a TreeModel view of a tree built from {@link Map}
 * instances.
 */
public class MapTreeModel implements TreeModel {

    public final static ClipboardOwner dummyClipboardOwner = new ClipboardOwner() {
        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
        }
    };

    /**
     * Clone a map-based tree of data.
     *
     * @param tree
     *            the map representing the tree
     * @param contentsKey
     *            the key used to store the children of a node in the map.
     * @return a copy of the original data in which all contained values are the
     *         same as those of the original, whereas every container (Map or
     *         List instance) has been cloned.
     */
    public static Map cloneTree(Map tree, Object contentsKey) {
        if (tree == null) {
            return null;
        }

        Map result;
        try {
            result = tree.getClass().newInstance();
            result.putAll(tree);
        }
        catch (Throwable t) {
            result = new HashMap(tree);
        }

        List contents = (List)tree.get(contentsKey);
        if (contents == null) {
            return result;
        }

        List cs;
        try {
            cs = contents.getClass().newInstance();
        }
        catch (Throwable t) {
            cs = new ArrayList(contents.size());
        }

        result.put(contentsKey, cs);
        for (Iterator i = contents.iterator(); i.hasNext();) {
            cs.add(cloneTree((Map)i.next(), contentsKey));
        }

        return result;
    }

    private Node _root;
    private List _listeners;

    public MapTreeModel() {
        this(null);
    }

    public MapTreeModel(Node n) {
        _root = n;
    }

    /**
     * Set the root of this tree model.
     *
     * @param root
     *            the new root node
     */
    public void setRoot(Node root) {
        _root = root;
        fireTreeStructureChanged(new Object[] { root });
    }

    /**
     * Return the path to a given node, as an array of Node instances.
     *
     * @param obj
     *            the content node
     * @return null if the given object is not a node contained in this tree
     *         model. Otherwise the node's path is returned.
     */
    public Object[] getPath(Object obj) {
        if (!(obj instanceof Node)) {
            return null;
        }

        Node mtn = (Node)obj;
        if (mtn.getRoot() != _root) {
            return null;
        }

        return mtn.createPath();
    }

    public synchronized void fireTreeStructureChanged(Object[] p) {
        if (_listeners == null) {
            return;
        }

        TreeModelEvent e = new TreeModelEvent(this, p);
        for (Iterator i = _listeners.iterator(); i.hasNext();) {
            ((TreeModelListener)i.next()).treeStructureChanged(e);
        }
    }

    public synchronized void fireTreeNodesChanged(Object[] p) {
        if (_listeners == null) {
            return;
        }

        TreeModelEvent e = new TreeModelEvent(this, p);
        for (Iterator i = _listeners.iterator(); i.hasNext();) {
            ((TreeModelListener)i.next()).treeNodesChanged(e);
        }
    }

    public synchronized void fireTreeNodesRemoved(Object[] p) {
        if (_listeners == null) {
            return;
        }

        TreeModelEvent e = new TreeModelEvent(this, p);
        for (Iterator i = _listeners.iterator(); i.hasNext();) {
            ((TreeModelListener)i.next()).treeNodesRemoved(e);
        }
    }

    public synchronized void fireTreeNodesInserted(Object[] p) {
        if (_listeners == null) {
            return;
        }

        TreeModelEvent e = new TreeModelEvent(this, p);
        for (Iterator i = _listeners.iterator(); i.hasNext();) {
            ((TreeModelListener)i.next()).treeNodesInserted(e);
        }
    }

    /**
     * Create a new node.
     *
     * @param parent
     *            the parent node of the new node
     * @param data
     *            the backing data
     * @return a new Node instance, which is created from the given parent and
     *         data. This is called from {@link Node#build(Map)} to allow a
     *         specific Node instance to be created. The default implementation
     *         returns <code>new Node(parent).build(data)</code>
     */
    public Node createNode(Node parent, Map data) {
        return new Node(parent).build(data);
    }

    /**
     * @return the key under which a node's user object is stored in the map.
     *         The default implementation returns "name".
     */
    public Object userObjectKey() {
        return "name";
    }

    /**
     * @return the key under which the children of a node are stored in the map;
     *         the children should be stored in a List instance containing Map
     *         instances. The default implementation returns "contents".
     */
    public Object contentsKey() {
        return "contents";
    }

    @Override
    public boolean isLeaf(Object p1) {
        return ((Node)p1).isLeaf();
    }

    @Override
    public synchronized void removeTreeModelListener(TreeModelListener p1) {
        if (_listeners != null) {
            _listeners.remove(p1);
            if (_listeners.size() == 0) {
                _listeners = null;
            }
        }
    }

    @Override
    public java.lang.Object getRoot() {
        return _root;
    }

    @Override
    public synchronized void addTreeModelListener(TreeModelListener p1) {
        if (p1 == null) {
            return;
        }

        if (_listeners == null) {
            _listeners = new ArrayList(2);
        }
        _listeners.add(p1);
    }

    @Override
    public Object getChild(Object p1, int p2) {
        return ((Node)p1).getChild(p2);
    }

    @Override
    public int getChildCount(Object p1) {
        return ((Node)p1).getChildCount();
    }

    @Override
    public int getIndexOfChild(Object p1, Object p2) {
        return ((Node)p1).getIndexOfChild((Node)p2);
    }

    @Override
    public void valueForPathChanged(TreePath p1, Object p2) {
        Node mtn = (Node)p1.getLastPathComponent();
        mtn.setUserObject(p2);
        fireTreeNodesChanged(p1.getPath());
    }

    /**
     * An operation (used for DFS traversal of {@link Node}).
     */
    public static interface Operation {

        /**
         * Perform the operation on a single node
         *
         * @param m
         *            the node
         * @return null iff the traversal should continue. Otherwise, the
         *         traversal ends with the first non-null return value, which
         *         will be the result of the entire operation.
         */
        public Object invoke(Node m);
    }

    /**
     * A tree model is composed of Nodes.
     */
    public class Node implements Transferable {

        private Map _data;
        private List _contents;
        private Node _parent;
        private Object _userObject;

        // indicates the level of the node, the root being at level 0
        private int _level;

        /**
         * Construct an empty root node
         */
        public Node() {
            this(null);
        }

        /**
         * Construct an empty node having the given parent.
         *
         * @param parent
         *            the parent of the new node
         */
        public Node(Node parent) {
            _level = ((_parent = parent) == null) ? 0 : parent._level + 1;
            _data = Collections.EMPTY_MAP;
            _contents = Collections.EMPTY_LIST;
        }

        /**
         * @return the MapTreeModel from which this node was constructed
         */
        public MapTreeModel getOwnerModel() {
            return MapTreeModel.this;
        }

        /**
         * @return the level of this node. The root of a model has level 0, its
         *         children level 1 and so forth.
         */
        public int getLevel() {
            return _level;
        }

        /**
         * @return the data set for this node
         */
        public Map getData() {
            return _data;
        }

        /**
         * @param key
         *            a key for which to return attibutes
         * @return a specific attribute from the data set.
         */
        public Object getAttribute(Object key) {
            return _data.get(key);
        }

        /**
         * Override or add a new attribute.
         *
         * @param key
         *            the attribute key
         * @param value
         *            the contents of the attribute
         * @return the old attribute value for the given key.
         */
        public Object putAttribute(Object key, Object value) {
            return _data.put(key, value);
        }

        /**
         * @return the user object for this node
         */
        public Object getUserObject() {
            return _userObject;
        }

        /**
         * Changes the user object for this node.
         *
         * @param obj
         *            the user object to be set
         */
        public void setUserObject(Object obj) {
            fireNodesChanged();
            _userObject = obj;
        }

        /**
         * @return the parent of this node. The return value is null only when
         *         the receiver is a root node
         */
        public Node getParent() {
            return _parent;
        }

        /**
         * @return the node which is the root of the tree containing this node.
         */
        public Node getRoot() {
            Node result = this;
            for (int i = _level; i > 0; i--) {
                result = result._parent;
            }

            return result;
        }

        /**
         * Create a collection containing this node and all its contained nodes.
         * The nodes will be added in a depth-first manner.
         *
         * @param c
         *            the collection to which the nodes should be added. If this
         *            is null, a new collection will be created.
         * @param preOrder
         *            whether the parent node should be added before its
         *            children.
         * @return the parameter c, or the newly created collection, in case the
         *         parameter c contained null.
         */
        public Collection getNodesDepthFirst(Collection c, boolean preOrder) {
            if (c == null) {
                c = new ArrayList();
            }

            if (preOrder) {
                c.add(this);
            }

            for (Iterator i = _contents.iterator(); i.hasNext();) {
                ((Node)i.next()).getNodesDepthFirst(c, preOrder);
            }

            if (!preOrder) {
                c.add(this);
            }

            return c;
        }

        /**
         * @return a list of the siblings preceding this node
         */
        public List getPrecedingSiblings() {
            Node parent = getParent();
            if (parent == null) {
                return Collections.EMPTY_LIST;
            }

            List siblings = parent._contents;
            int idx = siblings.indexOf(this);
            return idx < 0 ? Collections.EMPTY_LIST : siblings.subList(0, idx);
        }

        /**
         * @param idx
         *            an index
         * @return the child at the given index
         */
        public Node getChild(int idx) {
            return (Node)_contents.get(idx);
        }

        /**
         * @return the children of this node
         */
        public List getChildren() {
            return _contents;
        }

        /**
         * @return the number of children of this node
         */
        public int getChildCount() {
            return _contents.size();
        }

        /**
         * @param child
         *            a child node
         * @return the index of the given child.
         */
        public int getIndexOfChild(Node child) {
            return _contents.indexOf(child);
        }

        /**
         * Deletes a child from this node
         *
         * @param idx
         *            the index of the child node to be deleted
         */
        public void deleteChild(int idx) {
            Node n = (Node)_contents.remove(idx);

            // mutate the node such that it is a root node
            n._parent = null;
            n._level = 0;

            // send a notification
            fireNodesRemoved();
        }

        /**
         * Deletes a child from this node
         *
         * @param n
         *            the child node to be deleted
         */
        public void deleteChild(Node n) {
            int idx = getIndexOfChild(n);
            if (idx >= 0) {
                deleteChild(idx);
            }
        }

        /**
         * @return true iff the node supports child nodes. The default
         *         implementation returns true.
         */
        public boolean allowsChildren() {
            return true;
        }

        /**
         * Add a new child at the end of this node
         *
         * @param data
         *            the data for the new child node
         * @see #addChild(int, Map)
         * @exception UnsupportedOperationException
         *                if the node does not support child nodes.
         */
        public void addChild(Map data) throws UnsupportedOperationException {
            addChild(getChildCount(), data);
        }

        /**
         * Add a new child to this node
         *
         * @param idx
         *            the index at which the child should be added
         * @param data
         *            the data for the new child
         * @exception UnsupportedOperationException
         *                if the node does not support child nodes.
         */
        public void addChild(int idx, Map data)
                throws UnsupportedOperationException {
            if (!allowsChildren()) {
                throw new UnsupportedOperationException();
            }
            _contents.add(idx, createNode(this, data));
            fireNodesInserted();
        }

        /**
         * @return true iff the node is a leaf, i.e. if it does not contain any
         *         children
         */
        public boolean isLeaf() {
            return getChildCount() == 0;
        }

        /**
         * @return the path of Node instances from the root to the receiving
         *         node.
         */
        public Object[] createPath() {
            Object[] result = new Object[_level + 1];
            Node n = this;

            for (int i = _level; i >= 0;) {
                result[i--] = n;
                n = n._parent;
            }

            return result;
        }

        /**
         * Perform an operation on every node contained under this node, using a
         * depth-first traversal.
         *
         * @param op
         *            the operation to be performed
         * @param useRoot
         *            whether the operation should be applied to the root node
         *            (the node on which this method is first applied), or only
         *            to the children and their children.
         * @return the last operation result
         */
        public Object depthFirstOperation(Operation op, boolean useRoot) {
            Object result;

            if (useRoot && (result = op.invoke(this)) != null) {
                return result;
            }
            else {
                result = null;
            }

            for (Iterator i = _contents.iterator(); i.hasNext();) {
                if ((result = op.invoke((Node)i.next())) != null) {
                    return result;
                }
            }

            return result;
        }

        /**
         * Build (initialize) the node from the map data. This also creates any
         * child nodes, using {@link MapTreeModel#createNode(Node, Map)}.
         *
         * @param data
         *            the data for the node <BR>
         *            <em>N.B.</em>The map instance will be modified by this.
         *            The original map can be reconstructed by issuing a call to
         *            {@link #destroy destroy()}
         * @return the Node instance on which the method is invoked.
         */
        public Node build(Map data) {
            _data = data;
            _userObject = _data.remove(userObjectKey());

            if ((_contents = (List)_data.remove(contentsKey())) == null) {
                _contents = new ArrayList(2);
                return this;
            }
            else if (!allowsChildren()) {
                _contents = Collections.EMPTY_LIST;
                return this;
            }

            for (ListIterator i = _contents.listIterator(); i.hasNext();) {
                Map m = (Map)i.next();
                i.set(createNode(this, m));
            }

            addDefaultData(_data);
            return this;
        }

        /**
         * This is called from {@link #build(Map)} using the data of the node,
         * once the build process has finished. The default implementation does
         * nothing.
         *
         * @param data
         *            the data of the node
         */
        protected void addDefaultData(Map data) {
        }

        /**
         * @return a Map instance containing the data in this component and its
         *         subcomponents. This will effectively create a cloned copy of
         *         the original data map.
         * @see #destroy()
         */
        public Map cloneData() {
            Map result;

            try {
                result = _data.getClass().newInstance();
                result.putAll(_data);
            }
            catch (Throwable t) {
                result = new HashMap(_data);
            }

            result.put(userObjectKey(), getUserObject());

            int count = _contents.size();
            if (count > 0) {
                List l;

                try {
                    l = _contents.getClass().newInstance();
                }
                catch (Throwable t) {
                    l = new ArrayList(count);
                }

                for (int i = 0; i < count; i++) {
                    l.add(((Node)_contents.get(i)).cloneData());
                }

                result.put(contentsKey(), l);
            }

            return result;
        }

        /**
         * Destroys this node, effectively reversing the effect of the
         * constructor, such that the original Map from which the node was
         * created will be reconstructed.
         *
         * @return the Map representation of this node
         */
        public Map destroy() {
            // lose the reference to _data, which will be the result
            Map result = _data;
            _data = Collections.EMPTY_MAP;

            if (_contents.size() > 0) {
                // destroy each of the children, and replace the values in the
                // contents
                for (ListIterator i = _contents.listIterator(); i.hasNext();) {
                    Node n = (Node)i.next();
                    i.set(n.destroy());
                }

                // replace the contents in the data
                result.put(contentsKey(), _contents);
            }

            // lose the reference to the contents
            _contents = Collections.EMPTY_LIST;

            // add the user object, if any
            if (_userObject != null) {
                result.put(userObjectKey(), _userObject);
                // lose the reference
                _userObject = null;
            }

            // modify this node to be a root node
            _parent = null;
            _level = 0;

            // return the modified map
            return result;
        }

        /**
         * @return a textual representation of the data in this component and
         *         its subcomponents.
         */
        public String toExternalString() {
            StringBuffer buf = new StringBuffer();
            toExternalString(buf, "", this);
            return buf.toString();
        }

        protected void startExternalString(StringBuffer buf) {
            buf.append("[.");
        }

        protected void endExternalString(StringBuffer buf) {
            buf.append(".]");
        }

        protected void addExternalString(StringBuffer buf,
                Object key, Object value) {
            buf.append(key).append(" = ").append(StringUtils.any2String(
                    getUserObject(), GenericBean.FORMAT, ""));
        }

        private void toExternalString(StringBuffer buf, String indent,
                Node n) {
            startExternalString(buf);
            buf.append("\n");

            String indent2 = indent + "\t";

            buf.append(indent2);
            addExternalString(buf, userObjectKey(), n.getUserObject());

            Map m = n.getData();
            for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Map.Entry)i.next();

                buf.append(",\n").append(indent2);
                addExternalString(buf, e.getKey(), e.getValue());
            }

            Iterator i = n.getChildren().iterator();
            if (i.hasNext()) {
                buf.append(",\n").append(indent2).append(contentsKey())
                        .append(" = [ ");

                while (true) {
                    toExternalString(buf, indent2, (Node)i.next());
                    if (!i.hasNext()) {
                        break;
                    }
                    buf.append(",\n").append(indent2);
                }

                buf.append(" ]");
            }
            ;

            buf.append('\n').append(indent);
            endExternalString(buf);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            if (flavor.equals(DataFlavor.stringFlavor)) {
                return toExternalString();
            }
            else {
                return null;
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.stringFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            /*
             * for (int i = 0; i < flavors.length; i++) { if
             * (flavors[i].equals(flavor)) { return true; } } return false;
             */

            return flavor.equals(DataFlavor.stringFlavor);
        }

        /**
         * Notify the tree model that this node's properties have changed
         */
        public void fireNodesChanged() {
            fireTreeNodesChanged(createPath());
        }

        /**
         * Notify the tree model that a new node has been added (this is invoked
         * automatically on node addition)
         */
        protected void fireNodesInserted() {
            fireTreeStructureChanged(createPath());
        }

        /**
         * Notify the tree model that a node owned by this node has been
         * deleted(this is invoked automatically on node removal)
         */
        protected void fireNodesRemoved() {
            fireTreeStructureChanged(createPath());
        }

        /**
         * Notify the tree model that the tree rooted by this node has been
         * restructured.
         */
        protected void fireStructureChanged() {
            fireTreeStructureChanged(createPath());
        }

        /**
         * @return a suitable String representation of the given node. This is
         *         used when a node is being displayed in a tree. The default
         *         implementation returns the string value of the node's user
         *         object.
         */
        @Override
        public String toString() {
            Object obj = getUserObject();
            return obj == null ? "" : obj.toString();
        }

    }
}
