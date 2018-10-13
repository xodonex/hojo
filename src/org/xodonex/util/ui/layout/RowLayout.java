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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;

/**
 * The row layout lays out a container as a single horizontal row. The width of
 * the components is determined as follows:
 * <ul>
 * <li>The preferred width of each component is determined.
 * <li>If the sum of the preferred widths exceeds the the available width, then
 * the width of each component will be reduced towards the minimum width. If
 * this still exceeds the allowed width, then all components are shrunk
 * proportionally.
 * <li>If the sum of the preferred withds exceeds the available width, then the
 * width of each component will be extended towards the maximum size.
 * </ul>
 *
 * @author Henrik Lauritzen
 */
public class RowLayout implements LayoutManager, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public final static int RESIZE = -1;
    public final static int CENTER = -2;
    public final static int TOP = -3;
    public final static int BOTTOM = -4;

    // the gap size
    private int _gap;

    // the vertical alignment of the layout.
    private int _valign;

    // the minimal and maximal height of the layout
    private int _minHeight, _maxHeight;

    public RowLayout() {
        this(5, CENTER, 0, -1);
    }

    public RowLayout(int valign) {
        this(5, valign, 0, -1);
    }

    public RowLayout(int gapsize, int valign, int minHeight, int maxHeight) {
        _gap = gapsize;
        _valign = valign;
        _minHeight = minHeight;
        _maxHeight = maxHeight;
    }

    @Override
    public java.awt.Dimension preferredLayoutSize(java.awt.Container pl) {
        int size = pl.getComponentCount();
        Dimension result = new Dimension(0, _minHeight);

        for (int i = 0; i < size; i++) {
            Component c = pl.getComponent(i);
            if (!c.isVisible()) {
                continue;
            }

            Dimension d = c.getPreferredSize();
            Dimension d2 = c.getMinimumSize();
            int w = d.width > d2.width ? d.width : d2.width;
            int h = d.height > d2.height ? d.height : d2.height;

            // enlarge the width
            result.width += w;

            if (i > 0) {
                // add the gap length
                result.width += _gap;
            }

            if (h > result.height) {
                // enlarge the height as necessary
                result.height = h;
            }
        }

        if (result.height > _maxHeight) {
            result.height = _maxHeight;
        }

        Insets insets = pl.getInsets();
        result.width += (insets.left + insets.right);
        result.height += (insets.top + insets.bottom);
        return result;
    }

    @Override
    public void removeLayoutComponent(java.awt.Component p1) {
        // do nothing
    }

    @Override
    public java.awt.Dimension minimumLayoutSize(java.awt.Container pl) {
        int size = pl.getComponentCount();
        Dimension result = new Dimension(0, _minHeight);

        for (int i = 0; i < size; i++) {
            Component c = pl.getComponent(i);
            if (!c.isVisible()) {
                continue;
            }

            Dimension d = c.getMinimumSize();

            // enlarge the width
            result.width += d.width;

            if (i > 0) {
                // add the gap length
                result.width += _gap;
            }
            if (d.height > result.height) {
                // enlarge the height as necessary
                result.height = d.height;
            }
        }

        if (result.height > _maxHeight) {
            result.height = _maxHeight;
        }

        Insets insets = pl.getInsets();
        result.width += (insets.left + insets.right);
        result.height += (insets.top + insets.bottom);
        return result;
    }

    @Override
    public void addLayoutComponent(java.lang.String p1, java.awt.Component p2) {
        // do nothing
    }

    @Override
    public void layoutContainer(java.awt.Container pl) {
        // determine which components are to be laid out, and get the size
        // preferences from the components
        int size = pl.getComponentCount();
        ArrayList l = new ArrayList(size);

        for (int i = 0; i < size; i++) {
            Component c = pl.getComponent(i);
            if (!c.isVisible()) {
                continue;
            }
            l.add(c);
        }

        if (size == 0) {
            // do nothing
            return;
        }

        // determine the preferred widths and heights, as well as the
        // required width
        Component[] cs = (Component[])l
                .toArray(new Component[size = l.size()]);
        int[] prefW = new int[size];
        int[] prefH = new int[size];

        int w = 0;
        for (int i = 0; i < size; i++) {
            Dimension d = cs[i].getPreferredSize();
            Dimension d2 = cs[i].getMinimumSize();
            prefH[i] = d.height > d2.height ? d.height : d2.height;
            w += prefW[i] = d.width > d2.width ? d.width : d2.width;
        }

        // determine the available width (excluding the gap) and height
        Insets insets = pl.getInsets();
        int availW = pl.getWidth() - insets.left - insets.right
                - (size - 1) * _gap;
        int availH = pl.getHeight() - insets.top - insets.bottom;

        // calculate the widhts of the components
        int[] ws = new int[size];

        int delta = 0;
        int[] deltas = new int[size];

        if (availW >= w) {
            // there is enough space. Calculate how much each component can
            // be enlarged
            for (int i = 0; i < size; i++) {
                Dimension d = cs[i].getMaximumSize();
                delta += deltas[i] = d.width - prefW[i];
            }

            if (delta == 0) {
                // can't enlarge - use the preferred widhts and leave a space
                System.arraycopy(prefW, 0, ws, 0, size);
            }
            else {
                // scale relative to the amount of extra space allowed
                float fac = ((float)(availW - w)) / delta;
                for (int i = 0; i < size; i++) {
                    ws[i] = prefW[i] + Math.round(fac * deltas[i]);
                }
            }
        }
        else {
            // there is not enough space. Calculate how much each component can
            // be shrunk.
            for (int i = 0; i < size; i++) {
                Dimension d = cs[i].getMinimumSize();
                delta += deltas[i] = d.width - prefW[i];
            }

            if (delta == 0) {
                // shrink all components proportionally
                float fac = (float)availW / w;
                for (int i = 0; i < size; i++) {
                    ws[i] = Math.round(fac * prefW[i]);
                }
            }
            else {
                // scale relative to the amount of extra space allowed
                float fac = ((float)(w - availW)) / delta;
                for (int i = 0; i < size; i++) {
                    ws[i] = prefW[i] - Math.round(fac * deltas[i]);
                }
            }
        }

        // now lay out the container
        int x = insets.left;
        int y;

        for (int i = 0; i < size; i++) {
            // determine the height to be used
            int h = prefH[i] > availH ? availH : prefH[i];

            switch (_valign) {
            case TOP:
                y = insets.top;
                break;
            case BOTTOM:
                y = insets.top + availH - h;
                break;
            case CENTER:
                y = insets.top + (availH - h) / 2;
                break;
            default:
                y = insets.top;
                h = prefH[i];
            }

            cs[i].setBounds(x, y, ws[i], h);
            x += ws[i] + _gap;
        }
    }

}
