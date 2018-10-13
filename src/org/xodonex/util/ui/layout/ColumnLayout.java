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

/**
 * The column layout lays out a container as a single vertical column.
 *
 * @author Henrik Lauritzen
 */
public class ColumnLayout implements LayoutManager, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public final static int CENTER = -1;
    public final static int LEFT = -2;
    public final static int RIGHT = -3;

    // the gap size
    private int _gap;

    // the layout width or alignment.
    private int _width;

    // the row height
    private int _height;

    public ColumnLayout() {
        this(5, CENTER, -1);
    }

    public ColumnLayout(int width, int height) {
        this(5, width, height);
    }

    public ColumnLayout(int gapsize, int width, int height) {
        _gap = gapsize;
        _width = width;
        _height = height;
    }

    @Override
    public java.awt.Dimension preferredLayoutSize(java.awt.Container pl) {
        Insets insets = pl.getInsets();
        Dimension result = new Dimension(_width > 0 ? _width : 0, 0);
        int size = pl.getComponentCount();

        for (int i = 0; i < size; i++) {
            Component c = pl.getComponent(i);
            if (!c.isVisible()) {
                continue;
            }

            Dimension d = c.getPreferredSize();
            if (d.width > result.width) {
                // use the maximal preferred width
                result.width = d.width;
            }

            if (i > 0) {
                // add the gap length
                result.height += _gap;
            }
            if (_height > 0) {
                // use the specified height
                result.height += _height;
            }
            else {
                // use the preferred height
                result.height += d.height;
            }
        }

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
        Insets insets = pl.getInsets();
        Dimension result = new Dimension();

        int size = pl.getComponentCount();

        for (int i = 0; i < size; i++) {
            Component c = pl.getComponent(i);
            if (!c.isVisible()) {
                continue;
            }

            Dimension d = c.getMinimumSize();
            if (_width != 0 && d.width > result.width) {
                // use the preferred width
                result.width = d.width;
            }

            if (i > 0) {
                // add the gap length
                result.height += _gap;
            }
            if (_height > 0) {
                // use the specified height
                result.height += _height;
            }
            else {
                result.height += d.height;
            }
        }

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
        Insets insets = pl.getInsets();
        Dimension bounds = pl.getSize();
        bounds.width -= (insets.left + insets.right);
        int size = pl.getComponentCount();

        int x = insets.left;
        int y = insets.top;

        for (int i = 0; i < size; i++) {
            Component c = pl.getComponent(i);
            if (!c.isVisible()) {
                continue;
            }

            Dimension d = c.getPreferredSize();
            int h = _height >= 0 ? _height : d.height;
            int w;

            if (_width == 0) {
                // resize to the container width
                c.setBounds(x, y, bounds.width, h);
            }
            else if (_width > 0) {
                // resize to a fixed (maximal) size
                c.setBounds(x, y,
                        w = _width < bounds.width ? _width : bounds.width, h);
            }
            else {
                // resize to the preferred (maximal) size, and align
                w = d.width > bounds.width ? bounds.width : d.width;
                if (_width == CENTER) {
                    c.setBounds(x + (bounds.width - w) / 2, y, w, h);
                }
                else if (_width == RIGHT) {
                    c.setBounds(x + bounds.width - w, y, w, h);
                }
                else {
                    c.setBounds(x, y, w, h);
                }
            }

            y += h + _gap;
        }
    }

}
