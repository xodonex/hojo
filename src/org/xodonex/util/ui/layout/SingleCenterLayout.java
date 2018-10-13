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
package org.xodonex.util.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * This layout manager is used layout a container that has (at most) one visible
 * component. If more than one visible component exists in the container, the
 * behaviour is undefined.
 */
public class SingleCenterLayout implements LayoutManager, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /** @serial */
    boolean allowEnlarge;

    /** @serial */
    Dimension minSize;

    /**
     * Constructor equivalent to <code>SingleCenterLayout(true, null)</code>
     *
     * @see #SingleCenterLayout(boolean, java.awt.Dimension)
     */
    public SingleCenterLayout() {
        this(true, null);
    }

    /**
     * Constructor equivalent to <code>SingleCenterLayout(allowEnlarge, null)
     * </code>
     *
     * @param allowEnlarge
     *            whether the minimal layout size is bounded by the component's
     *            size (<code>false</code>) or the container's size
     *            (<code>true</code>).
     * @see #SingleCenterLayout(boolean, java.awt.Dimension)
     */
    public SingleCenterLayout(boolean allowEnlarge) {
        this(allowEnlarge, null);
    }

    /**
     * Constructor equivalent to <code>SingleCenterLayout(true, minSize)
     * </code>
     *
     * @param minSize
     *            the minimum size for this layout
     * @see #SingleCenterLayout(boolean, java.awt.Dimension)
     */
    public SingleCenterLayout(Dimension minSize) {
        this(false, minSize);
    }

    /**
     * Creates a new <code>SingleCenterLayout</code> with the specified
     * behaviour.
     *
     * @param allowEnlarge
     *            whether the minimal layout size is bounded by the component's
     *            size (<code>false</code>) or the container's size
     *            (<code>true</code>).
     * @param minSize
     *            if not <code>null</code>, this value bounds the component's
     *            preferred size.
     */
    public SingleCenterLayout(boolean allowEnlarge, Dimension minSize) {
        this.allowEnlarge = allowEnlarge;
        this.minSize = minSize;
    }

    /**
     * Sets the layout strategy for this layout.
     *
     * @param allowEnlarge
     *            whether the component can cause a minimal or preferred layout
     *            size that is greater than its container.
     */
    public void setAllowEnlarge(boolean allowEnlarge) {
        this.allowEnlarge = allowEnlarge;
    }

    /**
     * Determines the behaviour for this layout.
     *
     * @return <code>true</code> if the component can cause a preferred layout
     *         size that is greater than its container, <code>false</code> false
     *         otherwise
     */
    public boolean allowEnlarge() {
        return allowEnlarge;
    }

    /**
     * Sets the forced layout size for this layout.
     *
     * @param minSize
     *            the size that will be used as a bound for the component's
     *            minimum size (its not <code>allowEnlarge()</code>). If it is
     *            <code>
     * null</code>, no minimal preferred size will be used.
     */
    public void setMinSize(Dimension minSize) {
        this.minSize = minSize;
    }

    /**
     * Returns the minimal size for this layout.
     *
     * @return the minimal size for this layout.
     * @see #setMinSize(java.awt.Dimension)
     */
    public Dimension minSize() {
        return minSize;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    // Returns the first visible component in the container
    private static Component getSingleComponent(Container parent) {
        Component singleC = null;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            if ((singleC = parent.getComponent(i)).isVisible()) {
                break;
            }
        }
        return singleC;
    }

    // Returns the internal size of a container
    private static Dimension getSizeOf(Container c) {
        Dimension d = c.getSize();
        Insets i = c.getInsets();
        d.width -= (i.left + i.right);
        d.height -= (i.top + i.bottom);
        return d;
    }

    // Returns the least possible size for a container
    private Dimension leastSize(Container c) {
        Insets i = c.getInsets();
        Dimension d = new Dimension(i.left + i.right, i.top + i.bottom);
        if (minSize != null) {
            maxDim(d, minSize);
        }
        return d;
    }

    // Finds the least common Dimension
    private static Dimension minDim(Dimension dest, Dimension source) {
        dest.width = (source.width < dest.width) ? source.width : dest.width;
        dest.height = (source.height < dest.height) ? source.height
                : dest.height;
        return dest;
    }

    // Finds the largest common Dimension
    private static Dimension maxDim(Dimension dest, Dimension source) {
        dest.width = (source.width > dest.width) ? source.width : dest.width;
        dest.height = (source.height > dest.height) ? source.height
                : dest.height;
        return dest;
    }

    /**
     * Calculates the preferred size for the container, using the configured
     * strategy.
     *
     * @return the calculated preferred size
     * @see #setAllowEnlarge(boolean)
     * @see #setMinSize(java.awt.Dimension)
     */
    @Override
    public Dimension preferredLayoutSize(Container c) {
        Component comp = getSingleComponent(c);
        if (comp == null) {
            return leastSize(c);
        }

        Dimension result = getSizeOf(c);
        Container parent = c.getParent();
        if (parent != null) {
            minDim(result, getSizeOf(parent));
        }
        if (allowEnlarge) {
            minDim(result, comp.getMaximumSize());
        }
        else {
            minDim(result, comp.getPreferredSize());
        }
        if (minSize != null) {
            maxDim(result, minSize);
        }
        return result;
    }

    /**
     * Calculates the minimum size for the container, using the configured
     * strategy.
     *
     * @return the calculated preferred size
     * @see #setAllowEnlarge(boolean)
     */
    @Override
    public Dimension minimumLayoutSize(Container c) {
        Component comp = getSingleComponent(c);
        if (comp == null) {
            return leastSize(c);
        }

        Dimension result = getSizeOf(c);
        Container parent = c.getParent();
        if (parent != null) {
            minDim(result, getSizeOf(parent));
        }
        minDim(result, comp.getMinimumSize());
        if (minSize != null) {
            maxDim(result, minSize);
        }
        return result;
    }

    /**
     * Lays out the container. Its single (visible) component will be sized as
     * returned by <code>preferredLayoutSize()</code> when <code>
     * (allowEnlarge() == false)</code>, then centered in the container.
     *
     * @see #preferredLayoutSize(java.awt.Container)
     */
    @Override
    public void layoutContainer(Container c) {
        // Find the available and resulting size for the component
        Component comp = getSingleComponent(c);
        if (comp == null) {
            return;
        }

        Dimension containerDim = getSizeOf(c);
        Container parent = c.getParent();
        if (parent == null) {
            return;
        }

        minDim(containerDim, getSizeOf(parent));
        Dimension componentDim = (Dimension)containerDim.clone();

        if (allowEnlarge) {
            minDim(componentDim, comp.getMaximumSize());
        }
        else {
            minDim(componentDim, comp.getPreferredSize());
        }
        if (minSize != null) {
            maxDim(componentDim, minSize);
        }

        // Calculate position for the center
        int startX = (containerDim.width - componentDim.width) >> 1;
        int startY = (containerDim.height - componentDim.height) >> 1;

        // Move the component in place
        Insets insets = parent.getInsets();
        comp.setBounds(insets.left + startX, insets.top + startY,
                componentDim.width, componentDim.height);
    }

}
